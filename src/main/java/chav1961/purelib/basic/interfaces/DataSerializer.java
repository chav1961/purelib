package chav1961.purelib.basic.interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface DataSerializer<T> {
	void serialize(T source, DataOutput target) throws IOException;
	T deserialize(DataInput source) throws IOException;
}
