package chav1961.purelib.streams.char2byte.asm;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.cdb.JavaByteCodeConstants;


class ClassContainer implements Closeable {
	
	private final SyntaxTreeInterface<NameDescriptor>	tree = new AndOrTree<>(1,16);
	private final ClassConstantsRepo			ccr = new ClassConstantsRepo(tree);
	private final InOutGrowableByteArray		interfGba = new InOutGrowableByteArray(false); 
	private final InOutGrowableByteArray		fieldsGba = new InOutGrowableByteArray(false); 
	private final List<MethodDescriptor>		methods = new ArrayList<>();
	private final List<long[]>					forwards = new ArrayList<>();
	
	private short	classModifiers = 0, thisId = 0, superId = 0, interfCount = 0, fieldCount = 0;
	private short	currentMajor = JavaByteCodeConstants.MAJOR_1_7, currentMinor = JavaByteCodeConstants.MINOR_1_7;
	private long	joinedClassName = 0, constantValueId;
	private boolean methodBodyAwait = false;
	private URI		sourceRef = null;
	
	public ClassContainer() {
		tree.placeName((CharSequence)"long",null);			// To use in LocalVarTable descriptors
		tree.placeName((CharSequence)"double",null);
		constantValueId = tree.placeName(JavaByteCodeConstants.ATTRIBUTE_ConstantValue,0,JavaByteCodeConstants.ATTRIBUTE_ConstantValue.length,null);	// To use in initials
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
		fieldsGba.writeShort(0);		// Field has no attributes
		fieldCount++;
	}

	void addFieldDescription(final short modifiers, final long fieldId, final long typeId, final short valueId) throws IOException, ContentException {
		fieldsGba.writeShort(modifiers);
		fieldsGba.writeShort(getConstantPool().asUTF(fieldId));
		fieldsGba.writeShort(getConstantPool().asUTF(typeId));
		fieldsGba.writeShort(1);		// Field has one attribute
		fieldsGba.writeShort(getConstantPool().asUTF(constantValueId));
		fieldsGba.writeInt(2);		
		fieldsGba.writeShort(valueId);	// Reference to initial value in the constant pool
		fieldCount++;
	}
	
	void setSourceAttribute(final URI source) {
		sourceRef = source;
	}
	
	MethodDescriptor addMethodDescription(final short modifiers, final short specialFlags, final long methodId, final long typeId, final long... throwsId) throws IOException, ContentException {
		if (joinedClassName == 0) {
			throw new IllegalStateException("Call to addMethodDescription(...) before setClassName(...)!");
		}
		else if (methodBodyAwait) {
			throw new IllegalStateException("Previous call to addMethodDescription(...) not commited with addMethodBody(...)!");
		}
		else if ((modifiers & JavaByteCodeConstants.ACC_ABSTRACT) != 0 && (classModifiers & JavaByteCodeConstants.ACC_ABSTRACT) == 0) {
			throw new IllegalStateException("Attempt to add abstract method to non-abstract class!");
		}
		else {
			final MethodDescriptor	md = new MethodDescriptor(currentMajor, currentMinor, getNameTree(), getConstantPool(), modifiers, specialFlags, joinedClassName, methodId, typeId, throwsId);

			joinClassName(joinedClassName,methodId);
			methods.add(md);
			return md;  
		}
	}

	void addMethodBody(final MethodBody body) {
		methodBodyAwait = false;
	}

	void addForward(final long methodId, final long typeId) {
		for (long[] item : forwards) {
			if (item[0] == methodId && item[1] == typeId) {
				return;
			}
		}
		forwards.add(new long[] {methodId, typeId});
	}
	
	void dump(final OutputStream os) throws IOException, ContentException {
		if (methodBodyAwait) {
			throw new IllegalStateException("Last call to addMethodDescription(...) not commited with addMethodBody(...)!");
		}
		if (superId == 0) {						// Defaults for java.lang.Object extension
			superId = getConstantPool().asClassDescription(tree.placeOrChangeName(JavaByteCodeConstants.OBJECT_NAME,0,JavaByteCodeConstants.OBJECT_NAME.length,new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)));
		}
		
		short	attr_sourcefile = 0, attr_sourcefile_text = 0; 
		
		if (sourceRef != null) {
			attr_sourcefile = ccr.asUTF(tree.placeName((CharSequence)"SourceFile",null));
			attr_sourcefile_text = ccr.asUTF(tree.placeName((CharSequence)sourceRef.toString(),null));
		}		
		
		final InOutGrowableByteArray	result = new InOutGrowableByteArray(false); 
		
		result.writeInt(JavaByteCodeConstants.MAGIC);		// Magic
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
			return getNameTree().placeOrChangeName(forName,0,forName.length,new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
		}
		else {
			return classId;
		}
	}

	private long convertTypeName(final long typeId) {
		final char[]	result = new char[getNameTree().getNameLength(typeId)];
		
		getNameTree().getName(typeId,result,0);
		if (decode(result,'.','/')) {
			return getNameTree().placeOrChangeName(result,0,result.length,new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
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
