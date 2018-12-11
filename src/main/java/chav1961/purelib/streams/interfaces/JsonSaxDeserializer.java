package chav1961.purelib.streams.interfaces;

/**
 * <p>This interface extends {@link JsonSaxHandler} interface to support automatic deserialization from JSON to Java classes.</p>
 * 
 * 
 * @param <T> class instance to manipulate with the interface
 * 
 * @see chav1961.purelib.streams.JsonSaxParser
 * @see <a href="http://www.rfc-base.org/rfc-7159.html">RFC 7159</a> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface JsonSaxDeserializer<T> extends JsonSaxHandler {
	/**
	 * <p>Use the given instance of the class to fill deserialized data to.</p>
	 * @param instance instance to fill data from the JSON. Call with the <b>null</b> parameter guarantees, that the new class
	 * instance will be created instead of filling existent class instance
	 */
	void use(T instance);
	
	/**
	 * <p>Get class instance with the deserialized data. It can be new instance on the same first parsing or calling {@link #use(Object)} with
	 * the <b>null</b> parameter, or class instance returned on previous parsing, or class instance explicitly passed by calling {@link #use(Object)}.
	 * @return instance deserialized
	 */
	T getInstance();
}
