package chav1961.purelib.streams.char2byte.asm;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;


class ClassContainer implements Closeable {
	
	private final SyntaxTreeInterface<NameDescriptor>	tree = new AndOrTree<>(1,16);
	private final ClassConstantsRepo			ccr = new ClassConstantsRepo(tree);
	private final InOutGrowableByteArray		interfGba = new InOutGrowableByteArray(false); 
	private final InOutGrowableByteArray		fieldsGba = new InOutGrowableByteArray(false); 
	private final List<MethodDescriptor>		methods = new ArrayList<>();
	
	private short	classModifiers = 0, thisId = 0, superId = 0, interfCount = 0, fieldCount = 0;
	private short	currentMajor = Constants.MAJOR_1_7, currentMinor = Constants.MINOR_1_7;  
	private long	joinedClassName = 0;
	private boolean methodBodyAwait = false;
	private URL		sourceRef = null;
	
	public ClassContainer() {
	}

	@Override
	public void close() throws IOException {
		tree.clear();				
		ccr.close();
		for (MethodDescriptor item : methods) {
			item.close();
		}
		methods.clear();
	}
	
	void changeClassFormatVersion(final short newMajor, final short newMinor) {
		this.currentMajor = newMajor;
		this.currentMinor = newMinor;
	}
	
	String getClassName() {
		if (joinedClassName == 0) {
			throw new IllegalStateException("Attempt to call getClassName() before setClassName(...)"); 
		}
		else {
			final char[]	result = new char[getNameTree().getNameLength(joinedClassName)];
			
			getNameTree().getName(joinedClassName,result,0);
			decode(result,'/','.');
			return new String(result);
		}
	}
	
	short getClassId() {
		return thisId;
	}
	
	long setClassName(final short modifiers, final long packageId, final long classId) throws IOException, ContentException {
		joinedClassName = packageId < 0 ? classId : joinClassName(packageId,classId); 
		thisId = getConstantPool().asClassDescription(this.joinedClassName);
		classModifiers = modifiers;
		return joinedClassName; 
	}
	
	void setExtendsClassName(final long classId) throws IOException, ContentException {
		superId = getConstantPool().asClassDescription(convertTypeName(classId));
	}

	void addInterfaceName(final long interfaceId) throws IOException, ContentException {
		interfGba.writeShort(getConstantPool().asClassDescription(convertTypeName(interfaceId)));
		interfCount++;
	}

	void addFieldDescription(final short modifiers, final long fieldId, final long typeId) throws IOException, ContentException {
		fieldsGba.writeShort(modifiers);
		fieldsGba.writeShort(getConstantPool().asUTF(fieldId));
		fieldsGba.writeShort(getConstantPool().asUTF(typeId));
		fieldsGba.writeShort(0);
		fieldCount++;
	}

	void setSourceAttribute(final URL source) {
		sourceRef = source;
	}
	
	MethodDescriptor addMethodDescription(final short modifiers, final long methodId, final long typeId, final long... throwsId) throws IOException, ContentException {
		if (joinedClassName == 0) {
			throw new IllegalStateException("Call to addMethodDescription(...) before setClassName(...)!");
		}
		else if (methodBodyAwait) {
			throw new IllegalStateException("Previous call to addMethodDescription(...) not commited with addMethodBody(...)!");
		}
		else if ((modifiers & Constants.ACC_ABSTRACT) != 0 && (classModifiers & Constants.ACC_ABSTRACT) == 0) {
			throw new IllegalStateException("Attempt to add abstract method to non-abstract class!");
		}
		else {
			final MethodDescriptor	md = new MethodDescriptor(getNameTree(),getConstantPool(),modifiers,joinedClassName,methodId,typeId,throwsId);

			joinClassName(joinedClassName,methodId);
			methods.add(md);
			return md;  
		}
	}

	void addMethodBody(final MethodBody body) {
		methodBodyAwait = false;
	}
	
	void dump(final OutputStream os) throws IOException, ContentException {
		if (methodBodyAwait) {
			throw new IllegalStateException("Last call to addMethodDescription(...) not commited with addMethodBody(...)!");
		}
		if (superId == 0) {						// Defaults for java.lang.Object extension
			superId = getConstantPool().asClassDescription(tree.placeOrChangeName(Constants.OBJECT_NAME,0,Constants.OBJECT_NAME.length,new NameDescriptor()));
		}
		
		short	attr_sourcefile = 0, attr_sourcefile_text = 0; 
		
		if (sourceRef != null) {
			attr_sourcefile = ccr.asUTF(tree.placeName("SourceFile",null));
			attr_sourcefile_text = ccr.asUTF(tree.placeName(sourceRef.toExternalForm(),null));
		}		
		
		final InOutGrowableByteArray	result = new InOutGrowableByteArray(false); 
		
		result.writeInt(Constants.MAGIC);		// Magic
		result.writeShort(currentMinor);		// File minor version
		result.writeShort(currentMajor);		// File major version
		result.writeShort(getConstantPool().getPoolSize());	// Constant pool size
		getConstantPool().dump(result);			// Constant pool
		result.writeShort(classModifiers);		// access_flags;
		result.writeShort(thisId);				// this_class;
		result.writeShort(superId);				// super_class;
		result.writeShort(interfCount);			// interfaces_count;
		if (interfCount != 0) {
			result.write(interfGba);			// Inteface list
			interfGba.length(0);
		}
		result.writeShort(fieldCount);			// fields_count;
		if (fieldCount != 0) {
			result.write(fieldsGba);			// Fields list
			fieldsGba.length(0);
		}
		result.writeShort(methods.size());		// methods_count;
		for (MethodDescriptor item : methods) {
			item.dump(result);
		}
		if (sourceRef == null) {
			result.writeShort(0);				// attributes_count;
		}
		else {
			result.writeShort(1);				// attributes_count;
			result.writeShort(attr_sourcefile);
			result.writeInt(2);
			result.writeShort(attr_sourcefile_text);
		}

		try(final InputStream	is = result.getInputStream()) {
			Utils.copyStream(is, os);
		}
	}

	SyntaxTreeInterface<NameDescriptor> getNameTree() {
		return tree;
	}
	
	ClassConstantsRepo getConstantPool() {
		return ccr;
	}

	private long joinClassName(final long packageId, final long classId) {
		final int	packageLen = getNameTree().getNameLength(packageId), classLen = getNameTree().getNameLength(classId); 
		
		if (packageLen != -1) {
			final char[]	forName = new char[packageLen+classLen+1];
			int				curs = getNameTree().getName(packageId,forName,0);
			
			forName[curs] = '.';	
			getNameTree().getName(classId,forName,curs+1);
			decode(forName,'.','/');
			return getNameTree().placeOrChangeName(forName,0,forName.length,new NameDescriptor());
		}
		else {
			return classId;
		}
	}

	private long convertTypeName(final long typeId) {
		final char[]	result = new char[getNameTree().getNameLength(typeId)];
		
		getNameTree().getName(typeId,result,0);
		if (decode(result,'.','/')) {
			return getNameTree().placeOrChangeName(result,0,result.length,new NameDescriptor());
		}
		else {
			return typeId;
		}
	}
	
	private boolean decode(final char[] data, final char fromChar, final char toChar) {
		boolean	replaced = false;
		
		for (int index = 0, maxIndex = data.length; index < maxIndex; index++) {
			if (data[index] == fromChar) {
				data[index] = toChar;
				replaced = true;
			}
		}
		return replaced;
	}
}
