package chav1961.purelib.basic.interfaces;

public interface EnumRepoGetter<T extends Enum<?>,Z> {
	byte getByte(T name);
	short getShort(T name);
	int getInt(T name);
	long getLong(T name);
	float getFloat(T name);
	double getDouble(T name);
	char getChar(T name);
	boolean getBoolean(T name);
	Z get(T name);
	Z get(Class<Z> clazz, T name);
}
