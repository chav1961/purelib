package chav1961.purelib.streams.char2byte.asm;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;


class ClassConstantsRepo implements Closeable {
	public static final int				CPIDS_SIZE = 19; 
	

	private final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
	private final DataOutputStream		dos = new DataOutputStream(baos);
	private SyntaxTreeInterface<NameDescriptor>	names;
	private LongIdTree					classes = new LongIdTree(1);
	private LongIdTree					entityRefs = new LongIdTree(3);
	private LongIdTree					fieldRefs = new LongIdTree(2);
	private LongIdTree					strings = new LongIdTree(1);
	private LongIdTree					integers = new LongIdTree(1);
	private LongIdTree					longs = new LongIdTree(1);
	private LongIdTree					floats = new LongIdTree(1);
	private LongIdTree					doubles = new LongIdTree(1);
	private LongIdTree					nameAndType = new LongIdTree(2);
	private LongIdTree					utfs = new LongIdTree(1);
	private short						sequence = 1;
	
	ClassConstantsRepo(final SyntaxTreeInterface<NameDescriptor> names) {
		this.names = names;
	}

	@Override
	public void close() throws IOException {
		dos.flush();			dos.close();
		baos.flush();			baos.close();
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
			
			dos.writeByte(Constants.CONSTANT_Class);
			dos.writeShort(utfId);
			classes.addRef(result = nextVal(),classId);
		}
		return result;
	}

	short asFieldRefDescription(final long classId, final long fieldId, final long typeId) throws IOException, ContentException {
		short	result = entityRefs.getRef(classId,fieldId,typeId);
		
		if (result == 0) {
			final short	forClass = asClassDescription(classId), forField = asNameAndTypeDescription(fieldId,typeId);  
			
			dos.writeByte(Constants.CONSTANT_Fieldref);
			dos.writeShort(forClass);
			dos.writeShort(forField);
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
			
			dos.writeByte(Constants.CONSTANT_Methodref);
			dos.writeShort(forClass);
			dos.writeShort(forField);
			entityRefs.addRef(result = nextVal(),classId,methodId,signatureId);
		}
		return result;
	}

	short asInterfaceMethodRefDescription(final long classId, final long methodId, final long signatureId) throws IOException, ContentException {
		short	result = entityRefs.getRef(classId,methodId,signatureId);
		
		if (result == 0) {
			final short	forClass = asClassDescription(classId), forField = asNameAndTypeDescription(methodId,signatureId);  
			
			dos.writeByte(Constants.CONSTANT_InterfaceMethodref);
			dos.writeShort(forClass);
			dos.writeShort(forField);
			entityRefs.addRef(result = nextVal(),classId,methodId,signatureId);
		}
		return result;
	}
	
	short asStringDescription(final long stringId) throws IOException, ContentException {
		short	result = strings.getRef(stringId);
		
		if (result == 0) {
			final short	utf = asUTF(stringId);
			
			dos.writeByte(Constants.CONSTANT_String);
			dos.writeShort(utf);
			strings.addRef(result = nextVal(),stringId);
		}
		return result;
	}		

	short asIntegerDescription(final int value) throws IOException, ContentException {
		short	result = integers.getRef(value);
		
		if (result == 0) {
			dos.writeByte(Constants.CONSTANT_Integer);
			dos.writeInt(value);
			integers.addRef(result = nextVal(),value);
		}
		return result;
	}

	short asLongDescription(final long value) throws IOException, ContentException {
		short	result = longs.getRef(value);
		
		if (result == 0) {
			dos.writeByte(Constants.CONSTANT_Long);
			dos.writeLong(value);
			longs.addRef(result = nextVal(),value);
			nextVal();	// Placed 2 slots!
		}
		return result;
	}

	short asFloatDescription(final float value) throws IOException, ContentException {
		int		bytes = Float.floatToIntBits(value);
		short	result = floats.getRef(bytes);
		
		if (result == 0) {
			dos.writeByte(Constants.CONSTANT_Float);
			dos.writeInt(bytes);
			floats.addRef(result = nextVal(),bytes);
		}
		return result;
	}

	short asDoubleDescription(final double value) throws IOException, ContentException {
		long	bytes = Double.doubleToLongBits(value);
		short	result = doubles.getRef(bytes);
		
		if (result == 0) {
			dos.writeByte(Constants.CONSTANT_Double);
			dos.writeLong(bytes);
			doubles.addRef(result = nextVal(),bytes);
			nextVal();	// Placed 2 slots!
		}
		return result;
	}

	short asNameAndTypeDescription(final long fieldId, final long typeId) throws IOException, ContentException {
		short	result = nameAndType.getRef(fieldId,typeId);
		
		if (result == 0) {
			short	forName = asUTF(fieldId), forType = asUTF(typeId);
			
			dos.writeByte(Constants.CONSTANT_NameAndType);
			dos.writeShort(forName);
			dos.writeShort(forType);
			nameAndType.addRef(result = nextVal(),fieldId,typeId);
		}
		return result;
	}

	short asUTF(final long stringId) throws IOException, ContentException {
		short	result = utfs.getRef(stringId);
		
		if (result == 0) {
			dos.writeByte(Constants.CONSTANT_Utf8);
			dos.writeUTF(names.getName(stringId));
			utfs.addRef(result = nextVal(),stringId);
		}
		return result;
	}

	int dump(final OutputStream os) throws IOException {
		baos.writeTo(os);	os.flush();
		return baos.size();
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
