package chav1961.purelib.model;

import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.OrdinalSyntaxTree;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class ModelManagedMap {
	private final ContentMetadataInterface			model;
	private final SyntaxTreeInterface<ContentClass>	tree = new OrdinalSyntaxTree<>();
	private final String[]							forNames;
	private final long[]							forIds;
	
	public ModelManagedMap(final ContentMetadataInterface cmi) throws NullPointerException, IllegalArgumentException {
		if (cmi == null) {
			throw new NullPointerException("Content model interface can't be null");
		}
		else {
			this.model = cmi;
			final List<String>	names = new ArrayList<>();
			final List<Long>	ids = new ArrayList<>();
			
			model.walkDown((mode,appPath,uiPath,node) -> {
						if (mode == NodeEnterMode.ENTER) {
							if (URIUtils.hasSubScheme(appPath,ContentMetadataInterface.APPLICATION_SCHEME) && URIUtils.hasSubScheme(appPath,Constants.MODEL_APPLICATION_SCHEME_FIELD)) {
								final Class<?>	fieldType = node.getType();
								final String	name = node.getName();
								final int		type;
								
								switch (type = CompilerUtils.defineClassType(fieldType)) {
									case CompilerUtils.CLASSTYPE_REFERENCE : case CompilerUtils.CLASSTYPE_BYTE : case CompilerUtils.CLASSTYPE_SHORT :
									case CompilerUtils.CLASSTYPE_CHAR : case CompilerUtils.CLASSTYPE_INT : case CompilerUtils.CLASSTYPE_LONG :	
									case CompilerUtils.CLASSTYPE_FLOAT : case CompilerUtils.CLASSTYPE_DOUBLE : case CompilerUtils.CLASSTYPE_BOOLEAN :	
										ids.add(tree.placeName(name,createContainer(type,fieldType,node)));
										names.add(name);
										break;
									case CompilerUtils.CLASSTYPE_VOID		:
										throw new IllegalArgumentException("Field ["+name+"] in the model has type 'void' and can't be processed");
									default : throw new UnsupportedOperationException(); 
								}
							}
						}
						return ContinueMode.CONTINUE; 
					}
					,model.getRoot().getUIPath()
			);
			if (names.isEmpty()) {
				throw new IllegalArgumentException("No any field were detected in the model. Check model type");
			}
			this.forNames = names.toArray(new String[names.size()]);
			this.forIds = Utils.unwrapArray(ids.toArray(new Long[ids.size()]));
			names.clear();
			ids.clear();
		}
	}
	
	public ContentMetadataInterface getModel() {
		return model;
	}
	
	public String[] names() {
		return forNames;
	}

	public long[] nameIds() {
		return forIds;
	}
	
	public String nameById(final long nameId) throws ContentException {
		if (tree.contains(nameId)) {
			return tree.getName(nameId);
		}
		else {
			throw new ContentException("Long id ["+nameId+"] is unknown in this instance"); 
		}
	}

	public long idByName(final String name) throws ContentException, IllegalArgumentException {
		return name2Id(name);
	}
	
	public ContentNodeMetadata getMetadata(final String name) throws ContentException, IllegalArgumentException {
		return getMetadata(name2Id(name));
	}

	public ContentNodeMetadata getMetadata(final long nameId) throws ContentException, IllegalArgumentException {
		return getById(nameId).metadata;
	}
	
	public byte getByte(final String name) throws ContentException, IllegalArgumentException {
		return getByte(name2Id(name));
	}

	public byte getByte(final long nameId) throws ContentException, IllegalArgumentException {
		return ((ByteContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_BYTE)).value;
	}

	public ModelManagedMap setByte(final String name, final byte value) throws ContentException, IllegalArgumentException {
		return setByte(name2Id(name),value);
	}

	public ModelManagedMap setByte(final long nameId, final byte value) throws ContentException, IllegalArgumentException {
		((ByteContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_BYTE)).value = value;
		return this;
	}

	public short getShort(final String name) throws ContentException, IllegalArgumentException {
		return getShort(name2Id(name));
	}

	public short getShort(final long nameId) throws ContentException, IllegalArgumentException {
		return ((ShortContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_SHORT)).value;
	}

	public ModelManagedMap setShort(final String name, final short value) throws ContentException, IllegalArgumentException {
		return setShort(name2Id(name),value);
	}

	public ModelManagedMap setShort(final long nameId, final short value) throws ContentException, IllegalArgumentException {
		((ShortContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_SHORT)).value = value;
		return this;
	}

	public int getInt(final String name) throws ContentException, IllegalArgumentException {
		return getInt(name2Id(name));
	}

	public int getInt(final long nameId) throws ContentException, IllegalArgumentException {
		return ((IntContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_INT)).value;
	}

	public ModelManagedMap setInt(final String name, final int value) throws ContentException, IllegalArgumentException {
		return setInt(name2Id(name),value);
	}

	public ModelManagedMap setInt(final long nameId, final int value) throws ContentException, IllegalArgumentException {
		((IntContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_INT)).value = value;
		return this;
	}

	public long getLong(final String name) throws ContentException, IllegalArgumentException {
		return getLong(name2Id(name));
	}

	public long getLong(final long nameId) throws ContentException, IllegalArgumentException {
		return ((LongContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_LONG)).value;
	}

	public ModelManagedMap setLong(final String name, final long value) throws ContentException, IllegalArgumentException {
		return setLong(name2Id(name),value);
	}

	public ModelManagedMap setLong(final long nameId, final long value) throws ContentException, IllegalArgumentException {
		((LongContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_LONG)).value = value;
		return this;
	}

	public float getFloat(final String name) throws ContentException, IllegalArgumentException {
		return getFloat(name2Id(name));
	}

	public float getFloat(final long nameId) throws ContentException, IllegalArgumentException {
		return ((FloatContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_FLOAT)).value;
	}

	public ModelManagedMap setFloat(final String name, final float value) throws ContentException, IllegalArgumentException {
		return setFloat(name2Id(name),value);
	}

	public ModelManagedMap setFloat(final long nameId, final float value) throws ContentException, IllegalArgumentException {
		((FloatContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_FLOAT)).value = value;
		return this;
	}

	public double getDouble(final String name) throws ContentException, IllegalArgumentException {
		return getDouble(name2Id(name));
	}

	public double getDouble(final long nameId) throws ContentException, IllegalArgumentException {
		return ((DoubleContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_DOUBLE)).value;
	}

	public ModelManagedMap setDouble(final String name, final double value) throws ContentException, IllegalArgumentException {
		return setDouble(name2Id(name),value);
	}

	public ModelManagedMap setDouble(final long nameId, final double value) throws ContentException, IllegalArgumentException {
		((DoubleContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_DOUBLE)).value = value;
		return this;
	}

	public char getChar(final String name) throws ContentException, IllegalArgumentException {
		return getChar(name2Id(name));
	}

	public char getChar(final long nameId) throws ContentException, IllegalArgumentException {
		return ((CharContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_CHAR)).value;
	}

	public ModelManagedMap setChar(final String name, final char value) throws ContentException, IllegalArgumentException {
		return setChar(name2Id(name),value);
	}

	public ModelManagedMap setChar(final long nameId, final char value) throws ContentException, IllegalArgumentException {
		((CharContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_CHAR)).value = value;
		return this;
	}

	public boolean getBoolean(final String name) throws ContentException, IllegalArgumentException {
		return getBoolean(name2Id(name));
	}

	public boolean getBoolean(final long nameId) throws ContentException, IllegalArgumentException {
		return ((BooleanContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_BOOLEAN)).value;
	}

	public ModelManagedMap setBoolean(final String name, final boolean value) throws ContentException, IllegalArgumentException {
		return setBoolean(name2Id(name),value);
	}

	public ModelManagedMap setBoolean(final long nameId, final boolean value) throws ContentException, IllegalArgumentException {
		((BooleanContentClass)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_BOOLEAN)).value = value;
		return this;
	}

	public <T> T get(final String name, final Class<T> clazz) throws ContentException, IllegalArgumentException {
		return get(name2Id(name),clazz);
	}

	public <T> T get(final long nameId, final Class<T> clazz) throws ContentException, IllegalArgumentException {
		@SuppressWarnings("unchecked")
		final ReferenceContentClass<T>	content = (ReferenceContentClass<T>)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_REFERENCE);
		
		if (content.clazz.isAssignableFrom(clazz)) {
			return content.value;
		}
		else {
			throw new ContentException("Incompatible types: name ["+tree.getName(nameId)+"] has type ["+content.clazz+"], but ["+clazz+"] is awaited"); 
		}
	}

	public <T> ModelManagedMap set(final String name, final Class<T> clazz, final T value) throws ContentException, IllegalArgumentException {
		return set(name2Id(name),clazz,value);
	}

	public <T> ModelManagedMap set(final long nameId, final Class<T> clazz, final T value) throws ContentException, IllegalArgumentException {
		@SuppressWarnings("unchecked")
		final ReferenceContentClass<T>	content = (ReferenceContentClass<T>)getByIdAndType(nameId,CompilerUtils.CLASSTYPE_REFERENCE);
		
		if (content.clazz.isAssignableFrom(clazz)) {
			content.value = value;
			return this;
		}
		else {
			throw new ContentException("Incompatible types: name ["+tree.getName(nameId)+"] has type ["+content.clazz+"], but ["+clazz+"] is awaited"); 
		}
	}

	@Override
	public String toString() {
		try{final StringBuilder	sb = new StringBuilder();
	
			tree.walk((name,len,id,cargo)->{
				sb.append(" - ").append(name,0,len).append('[').append(id).append("]: ").append(cargo).append('\n');
				return true;
			});
			
			return "ModelManagedMap:\n"+sb.toString()+"end\n";
		} catch (RuntimeException exc) {
			return super.toString();
		}
	}

	private long name2Id(final String name) throws ContentException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name to seek can't be null or empty");
		}
		else {
			final long	id = tree.seekName(name);
			
			if (id >= 0) {
				return id;
			}
			else {
				throw new ContentException("Name ["+name+"] is unknown in this instance"); 
			}
		}
	}

	private ContentClass getById(final long nameId) throws IllegalArgumentException, ContentException {
		final ContentClass	cargo = tree.getCargo(nameId);
		
		if (cargo == null) {
			throw new ContentException("Long id ["+nameId+"] for name is unknown in the instance"); 
		}
		else {
			return cargo;
		}
	}
	
	private ContentClass getByIdAndType(final long nameId, final int type) throws IllegalArgumentException, ContentException {
		final ContentClass	cargo = getById(nameId);
		
		if (cargo.contentType != type) {
			throw new ContentException("Incompatible types: name ["+tree.getName(nameId)+"] has type ["+cargo.contentType+"], but ["+type+"] is awaited"); 
		}
		else {
			return cargo;
		}
	}
	
	private static <T> ContentClass createContainer(final int type, final Class<T> clazz, final ContentNodeMetadata metadata) {
		switch (type) {
			case CompilerUtils.CLASSTYPE_REFERENCE	:
				return new ReferenceContentClass<T>(clazz,metadata);
			case CompilerUtils.CLASSTYPE_BYTE		:
				return new ByteContentClass(metadata);
			case CompilerUtils.CLASSTYPE_SHORT		:
				return new ShortContentClass(metadata);
			case CompilerUtils.CLASSTYPE_CHAR		:
				return new CharContentClass(metadata);
			case CompilerUtils.CLASSTYPE_INT		:
				return new IntContentClass(metadata);
			case CompilerUtils.CLASSTYPE_LONG		:
				return new LongContentClass(metadata);
			case CompilerUtils.CLASSTYPE_FLOAT		:
				return new FloatContentClass(metadata);
			case CompilerUtils.CLASSTYPE_DOUBLE		:	
				return new DoubleContentClass(metadata);
			case CompilerUtils.CLASSTYPE_BOOLEAN	:	
				return new BooleanContentClass(metadata);
			default :
				throw new UnsupportedOperationException(); 
		}
	}
	
	private static class ContentClass {
		final int	contentType;
		final ContentNodeMetadata	metadata; 

		public ContentClass(final int contentType, final ContentNodeMetadata metadata) {
			this.contentType = contentType;
			this.metadata = metadata;
		}
	}
	
	private static class ByteContentClass extends ContentClass {
		byte		value;
		
		ByteContentClass(final ContentNodeMetadata metadata) {
			super(CompilerUtils.CLASSTYPE_BYTE,metadata);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ByteContentClass other = (ByteContentClass) obj;
			if (value != other.value) return false;
			return true;
		}

		@Override
		public String toString() {
			return "ByteContentClass [value=" + value + "]";
		}
	}

	private static class ShortContentClass extends ContentClass {
		short		value;
		
		ShortContentClass(final ContentNodeMetadata metadata) {
			super(CompilerUtils.CLASSTYPE_SHORT,metadata);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ShortContentClass other = (ShortContentClass) obj;
			if (value != other.value) return false;
			return true;
		}

		@Override
		public String toString() {
			return "ShortContentClass [value=" + value + "]";
		}
	}

	private static class IntContentClass extends ContentClass {
		int			value;
		
		IntContentClass(final ContentNodeMetadata metadata) {
			super(CompilerUtils.CLASSTYPE_INT,metadata);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			IntContentClass other = (IntContentClass) obj;
			if (value != other.value) return false;
			return true;
		}

		@Override
		public String toString() {
			return "IntContentClass [value=" + value + "]";
		}
	}

	private static class LongContentClass extends ContentClass {
		long		value;
		
		LongContentClass(final ContentNodeMetadata metadata) {
			super(CompilerUtils.CLASSTYPE_LONG,metadata);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (value ^ (value >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			LongContentClass other = (LongContentClass) obj;
			if (value != other.value) return false;
			return true;
		}

		@Override
		public String toString() {
			return "LongContentClass [value=" + value + "]";
		}
	}

	private static class FloatContentClass extends ContentClass {
		float		value;
		
		FloatContentClass(final ContentNodeMetadata metadata) {
			super(CompilerUtils.CLASSTYPE_FLOAT,metadata);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Float.floatToIntBits(value);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true; 
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			FloatContentClass other = (FloatContentClass) obj;
			if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value)) return false;
			return true;
		}

		@Override
		public String toString() {
			return "FloatContentClass [value=" + value + "]";
		}
	}

	private static class DoubleContentClass extends ContentClass {
		double		value;
		
		DoubleContentClass(final ContentNodeMetadata metadata) {
			super(CompilerUtils.CLASSTYPE_DOUBLE,metadata);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(value);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			DoubleContentClass other = (DoubleContentClass) obj;
			if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) return false;
			return true;
		}

		@Override
		public String toString() {
			return "DoubleContentClass [value=" + value + "]";
		}
	}

	private static class CharContentClass extends ContentClass {
		char		value;
		
		CharContentClass(final ContentNodeMetadata metadata) {
			super(CompilerUtils.CLASSTYPE_CHAR,metadata);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CharContentClass other = (CharContentClass) obj; 
			if (value != other.value) return false;
			return true;
		}

		@Override
		public String toString() {
			return "CharContentClass [value=" + value + "]";
		}
	}

	private static class BooleanContentClass extends ContentClass {
		boolean		value;
		
		BooleanContentClass(final ContentNodeMetadata metadata) {
			super(CompilerUtils.CLASSTYPE_BOOLEAN,metadata);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (value ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			BooleanContentClass other = (BooleanContentClass) obj;
			if (value != other.value) return false;
			return true;
		}

		@Override
		public String toString() {
			return "BooleanContentClass [value=" + value + "]";
		}
	}

	private static class ReferenceContentClass<T> extends ContentClass {
		final Class<T>	clazz;
		T				value;
		
		ReferenceContentClass(final Class<T> clazz,final ContentNodeMetadata metadata) {
			super(CompilerUtils.CLASSTYPE_REFERENCE,metadata);
			this.clazz = clazz;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ReferenceContentClass<?> other = (ReferenceContentClass<?>) obj;
			if (clazz == null) {
				if (other.clazz != null) return false;
			} else if (!clazz.equals(other.clazz)) return false;
			if (value == null) {
				if (other.value != null) return false;
			} else if (!value.equals(other.value)) return false;
			return true;
		}

		@Override
		public String toString() {
			return "ReferenceContentClass [clazz=" + clazz + ", value=" + value + "]";
		}
	}
}
