package chav1961.purelib.streams.char2byte.asm;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;


class ClassContainer implements Closeable {
	
	private final SyntaxTreeInterface<NameDescriptor>	tree = new AndOrTree<>(1,16);
	private final ClassConstantsRepo			ccr = new ClassConstantsRepo(tree);
	private final ByteArrayOutputStream			interf = new ByteArrayOutputStream(32);
	private final DataOutputStream				interfDos = new DataOutputStream(interf);
	private final ByteArrayOutputStream			fields = new ByteArrayOutputStream(64);
	private final DataOutputStream				fieldsDos = new DataOutputStream(fields);
	private final List<MethodDescriptor>		methods = new ArrayList<>();
	
	private short	classModifiers = 0, thisId = 0, superId = 0, interfCount = 0, fieldCount = 0;
	private long	joinedClassName = 0;
	private boolean methodBodyAwait = false;
	
	public ClassContainer() {
	}

	@Override
	public void close() throws IOException {
		tree.clear();				ccr.close();
		interfDos.close();			interf.close();
		fieldsDos.close();			fields.close();
		for (MethodDescriptor item : methods) {
			item.close();
		}
		methods.clear();
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
	
	long setClassName(final short modifiers, final long packageId, final long classId) throws IOException, ContentException {
		joinedClassName = joinClassName(packageId,classId); 
		thisId = getConstantPool().asClassDescription(this.joinedClassName);
		classModifiers = modifiers;
		return joinedClassName; 
	}
	
	void setExtendsClassName(final long classId) throws IOException, ContentException {
		superId = getConstantPool().asClassDescription(convertTypeName(classId));
	}

	void addInterfaceName(final long interfaceId) throws IOException, ContentException {
		interfDos.writeShort(getConstantPool().asClassDescription(convertTypeName(interfaceId)));
		interfCount++;
	}

	void addFieldDescription(final short modifiers, final long fieldId, final long typeId) throws IOException, ContentException {
		fieldsDos.writeShort(modifiers);
		fieldsDos.writeShort(getConstantPool().asUTF(fieldId));
		fieldsDos.writeShort(getConstantPool().asUTF(typeId));
		fieldsDos.writeShort(0);
		fieldCount++;
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
			superId = getConstantPool().asClassDescription(tree.placeName(Constants.OBJECT_NAME,0,Constants.OBJECT_NAME.length,null));
		}
		
		try(final DataOutputStream	dos = new DataOutputStream(os)) {
			dos.writeInt(Constants.MAGIC);		// Magic
			dos.writeShort(Constants.MINOR);	// File minor version
			dos.writeShort(Constants.MAJOR);	// File major version
			dos.writeShort(getConstantPool().getPoolSize());	// Constant pool size
			dos.flush();
			getConstantPool().dump(os);			// Constant pool
			dos.writeShort(classModifiers);		// access_flags;
			dos.writeShort(thisId);				// this_class;
			dos.writeShort(superId);			// super_class;
			dos.writeShort(interfCount);		// interfaces_count;
			if (interfCount != 0) {
				dos.flush();
				interfDos.flush();
				interf.writeTo(os);				// Inteface list
			}
			dos.writeShort(fieldCount);			// fields_count;
			if (fieldCount != 0) {
				dos.flush();
				fieldsDos.flush();
				fields.writeTo(os);				// Fields list
			}
			dos.writeShort(methods.size());		// methods_count;
			dos.flush();
			for (MethodDescriptor item : methods) {
				item.dump(os);
			}
			dos.writeShort(0);					// attributes_count;
			dos.flush();
		}
	}

	SyntaxTreeInterface<NameDescriptor> getNameTree() {
		return tree;
	}
	
	ClassConstantsRepo getConstantPool() {
		return ccr;
	}

	private long convertTypeName(final long typeId) {
		final char[]	result = new char[getNameTree().getNameLength(typeId)];
		
		getNameTree().getName(typeId,result,0);
		if (decode(result,'.','/')) {
			return getNameTree().placeName(result,0,result.length,null);
		}
		else {
			return typeId;
		}
	}
	
	private long joinClassName(final long packageId, final long classId) {
		final int	packageLen = getNameTree().getNameLength(packageId), classLen = getNameTree().getNameLength(classId); 
		
		if (packageLen != -1) {
			final char[]	forName = new char[packageLen+classLen+1];
			int				curs = getNameTree().getName(packageId,forName,0);
			
			forName[curs] = '.';	
			getNameTree().getName(classId,forName,curs+1);
			decode(forName,'.','/');
			return getNameTree().placeName(forName,0,forName.length,null);
		}
		else {
			return classId;
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
