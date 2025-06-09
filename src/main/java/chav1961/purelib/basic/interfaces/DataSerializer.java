package chav1961.purelib.basic.interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * <p>This interface describes data serialization/deserialization ability for the class.</p>
 * @param <T> class to serialize/deserialize.
 */
public interface DataSerializer<T> {
	/**
	 * <p>Serialize class content to the data output</p>
	 * @param source source content to serialize. Can't be null.
	 * @param target target data output to serialize. Can't be null.
	 * @throws IOException on any I/O errors
	 */
	void serialize(T source, DataOutput target) throws IOException;
	
	/**
	 * <p>Deserialize class content from the data input</p>
	 * @param source data input to deserialize content from. Can't be null</p>
	 * @return class instance deserialized. Can't be null.
	 * @throws IOException on any I/O errors
	 */
	T deserialize(DataInput source) throws IOException;
}
