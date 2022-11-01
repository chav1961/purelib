package chav1961.purelib.streams;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import chav1961.purelib.streams.byte2byte.MultipartInputStream;
import chav1961.purelib.streams.byte2byte.MultipartOutputStream;

/**
 * <p>This class describes part content for "multipart/form-data" stream. It is used in conjunction with {@linkplain MultipartInputStream} and {@linkplain MultipartOutputStream}.
 * It contains part name (extracted from ) and part properties. Keys of the part properties are both header keys and header attribute keys. Header attribute keys are concatenated
 * of header key and attribute key with colon (:):</p>
 * <code>
 * Content-type:text/plain encoding="UTF-8"
 * -- produces two keys:
 * Content-type=text/plain
 * Content-type:encoding=UTF-8
 * </code>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class MultipartEntry implements Iterable<Map.Entry<String, String>> {
	private final String		name;
	private final Properties	props = new Properties();
	
	/**
	 * <p>Constructor of the class</p>
	 * @param name part name
	 * @param props part properties
	 * @throws NullPointerException properties are null
	 * @throws IllegalArgumentException name is null or empty
	 */
	public MultipartEntry(final String name, final Properties props) throws NullPointerException, IllegalArgumentException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Part name can't be null or empty");
		}
		else if (props == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else {
			this.name = name;
			this.props.putAll(props);
		}
	}
	
	/**
	 * <p>Get part name</p>
	 * @return part name. Can't be null or empty
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Does the entry have the given property</p>
	 * @param key property to test. Can't be null or empty
	 * @return true if the property presents
	 * @throws IllegalArgumentException property name is null or empty
	 */
	public boolean contains(final String key) throws IllegalArgumentException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Part name can't be null or empty");
		}
		else {
			return props.containsKey(key);
		}
	}

	/**
	 * <p>Extract property from the entry</p>
	 * @param key property to extract. Can't be null or empty
	 * @return property value or null if missing
	 * @throws IllegalArgumentException property name is null or empty
	 */
	public String getProperty(final String key) throws IllegalArgumentException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Part name can't be null or empty");
		}
		else {
			return props.getProperty(key);
		}
	}

	/**
	 * <p>Extract property from the entry</p>
	 * @param key property to extract. Can't be null or empty
	 * @param defaultValue default property value if is missing
	 * @return property value or default value if missing
	 * @throws IllegalArgumentException property name is null or empty
	 */
	public String getProperty(final String key, final String defaultValue) throws IllegalArgumentException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Part name can't be null or empty");
		}
		else {
			return props.getProperty(key, defaultValue);
		}
	}
	
	/**
	 * <p>Set property in entry</p>
	 * @param key property to extract. Can't be null or empty
	 * @param value value to set. Null removes property by it's key
	 * @throws IllegalArgumentException property name is null or empty
	 */
	public void setProperty(final String key, final String value) throws IllegalArgumentException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Part name can't be null or empty");
		}
		else if (value == null) {
			props.remove(key);
		}
		else {
			props.setProperty(key, value);
		}
	}

	@Override
	public Iterator<Entry<String, String>> iterator() {
		return (Iterator<Entry<String, String>>) props.entrySet();
	}
}
