package chav1961.purelib.streams.char2byte.asm;

import java.io.Closeable;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.JavaByteCodeConstants;


class ClassConstantsRepo implements Closeable {
	public static final int					CPIDS_SIZE = 19; 

	private final InOutGrowableByteArray	iogba = new InOutGrowableByteArray(false); 
	private SyntaxTreeInterface<NameDescriptor>	names;
	private LongIdTree<Short>				classes = new LongIdTree<>(1);
	private LongIdTree<Short>				entityRefs = new LongIdTree<>(3);
	private LongIdTree<Short>				fieldRefs = new LongIdTree<>(2);
	private LongIdTree<Short>				strings = new LongIdTree<>(1);
	private LongIdTree<Short>				integers = new LongIdTree<>(1);
	private LongIdTree<Short>				longs = new LongIdTree<>(1);
	private LongIdTree<Short>				floats = new LongIdTree<>(1);
	private LongIdTree<Short>				doubles = new LongIdTree<>(1);
	private LongIdTree<Short>				nameAndType = new LongIdTree<>(2);
	private LongIdTree<Short>				utfs = new LongIdTree<>(1);
	private short							sequence = 1;
	
	ClassConstantsRepo(final SyntaxTreeInterface<NameDescriptor> names) {
		this.names = names;
	}

	@Override
	public void close() throws IOException {
		classes.clear();		entityRefs.clear();
		strings.clear();		integers.clear();
		longs.clear();			floats.clear();
		doubles.clear();		nameAndType.clear();
		utfs.clear();			fieldRefs.clear();
	}

	short getPoolSize() {
		return sequence;
	}
	
	short asClassDescription(final long classId) throws IOException, ContentException {
		short	result = classes.getRef(classId);
		
		if (result == 0) {
			final short	utfId = asUTF(classId); 
			
			iogba.writeByte(JavaByteCodeConstants.CONSTANT_Class);
			iogba.writeShort(utfId);
			classes.addRef(result = nextVal(),classId);
			names.getCargo(classId).cpIds[JavaByteCodeConstants.CONSTANT_Class] = result;
		}
		
		return result;
	}

	short asFieldRefDescription(final long classId, final long fieldId, final long typeId) throws IOException, ContentException {
		short	result = entityRefs.getRef(classId,fieldId,typeId);
		
		if (result == 0) {
			final short	forClass = asClassDescription(classId), forField = asNameAndTypeDescription(fieldId,typeId);  
			
			iogba.writeByte(JavaByteCodeConstants.CONSTANT_Fieldref);
			iogba.writeShort(forClass);
			iogba.writeShort(forField);
			entityRefs.addRef(result = nextVal(),classId,fieldId,typeId);
			fieldRefs.addRef(result,classId,fieldId);
		}
		return result;
	}

	short asFieldRefDescription(final long classId, final long fieldId) throws IOException, ContentException {
		return fieldRefs.getRef(classId,fieldId);
	}

	short asMethodRefDescription(final long classId, final long methodId, final long signatureId) throws IOException, ContentException {
		short	result = entityRefs.getRef(classId,methodId,signatureId);
		
		if (result == 0) {
			final short	forClass = asClassDescription(classId), forField = asNameAndTypeDescription(methodId,signatureId);  
			
			iogba.writeByte(JavaByteCodeConstants.CONSTANT_Methodref);
			iogba.writeShort(forClass);
			iogba.writeShort(forField);
			entityRefs.addRef(result = nextVal(),classId,methodId,signatureId);
		}
		return result;
	}

	short asInterfaceMethodRefDescription(final long classId, final long methodId, final long signatureId) throws IOException, ContentException {
		short	result = entityRefs.getRef(classId,methodId,signatureId);
		
		if (result == 0) {
			final short	forClass = asClassDescription(classId), forField = asNameAndTypeDescription(methodId,signatureId);  
			
			iogba.writeByte(JavaByteCodeConstants.CONSTANT_InterfaceMethodref);
			iogba.writeShort(forClass);
			iogba.writeShort(forField);
			entityRefs.addRef(result = nextVal(),classId,methodId,signatureId);
		}
		return result;
	}
	
	short asStringDescription(final long stringId) throws IOException, ContentException {
		short	result = strings.getRef(stringId);
		
		if (result == 0) {
			final short	utf = asUTF(stringId);
			
			iogba.writeByte(JavaByteCodeConstants.CONSTANT_String);
			iogba.writeShort(utf);
			strings.addRef(result = nextVal(),stringId);
		}
		return result;
	}		

	short asIntegerDescription(final int value) throws IOException, ContentException {
		short	result = integers.getRef(value);
		
		if (result == 0) {
			iogba.writeByte(JavaByteCodeConstants.CONSTANT_Integer);
			iogba.writeInt(value);
			integers.addRef(result = nextVal(), value);
		}
		return result;
	}

	short asLongDescription(final long value) throws IOException, ContentException {
		short	result = longs.getRef(value);
		
		if (result == 0) {
			iogba.writeByte(JavaByteCodeConstants.CONSTANT_Long);
			iogba.writeLong(value);
			longs.addRef(result = nextVal(),value);
			nextVal();	// Placed 2 slots!
		}
		return result;
	}

	short asFloatDescription(final float value) throws IOException, ContentException {
		int		bytes = Float.floatToIntBits(value);
		short	result = floats.getRef(bytes);
		
		if (result == 0) {
			iogba.writeByte(JavaByteCodeConstants.CONSTANT_Float);
			iogba.writeInt(bytes);
			floats.addRef(result = nextVal(),bytes);
		}
		return result;
	}

	short asDoubleDescription(final double value) throws IOException, ContentException {
		long	bytes = Double.doubleToLongBits(value);
		short	result = doubles.getRef(bytes);
		
		if (result == 0) {
			iogba.writeByte(JavaByteCodeConstants.CONSTANT_Double);
			iogba.writeLong(bytes);
			doubles.addRef(result = nextVal(),bytes);
			nextVal();	// Placed 2 slots!
		}
		return result;
	}

	short asNameAndTypeDescription(final long fieldId, final long typeId) throws IOException, ContentException {
		short	result = nameAndType.getRef(fieldId,typeId);
		
		if (result == 0) {
			short	forName = asUTF(fieldId), forType = asUTF(typeId);
			
			iogba.writeByte(JavaByteCodeConstants.CONSTANT_NameAndType);
			iogba.writeShort(forName);
			iogba.writeShort(forType);
			nameAndType.addRef(result = nextVal(),fieldId,typeId);
		}
		return result;
	}

	short asUTF(final long stringId) throws IOException, ContentException {
		short	result = utfs.getRef(stringId);
		
		if (result == 0) {
			iogba.writeByte(JavaByteCodeConstants.CONSTANT_Utf8);
			iogba.writeUTF(names.getName(stringId));
			utfs.addRef(result = nextVal(),stringId);
		}
		return result;
	}

	int dump(final InOutGrowableByteArray os) throws IOException {
		final byte[] content = iogba.extract();
		
		os.write(content,0,content.length);
		return content.length;
	}
	
	private short nextVal() throws ContentException {
		if (sequence == -1) {
			throw new ContentException("Class file restrictoin: constant pool is greater than 65536 items. Simplify your class code!");
		}
		else {
			return sequence++;
		}
	}	
}
