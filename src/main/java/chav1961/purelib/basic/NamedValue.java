package chav1961.purelib.basic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>This class is a wrapper for key/value pairs</p>
 * <p>This class is thread-safe</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @thread.safe
 * @param <T> value type
 */
public class NamedValue<T> {
	private final String	name;
	private final T			value;
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param name key name. Can't be null or empty
	 * @param value value associated. Can't be null
	 * @throws IllegalArgumentException name is null or empty
	 * @throws NullPointerException value is null
	 */
	public NamedValue(final String name, final T value) throws NullPointerException, IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name can't be null or empty string");
		}
		else if (value == null) {
			throw new NullPointerException("Value can't be null");
		}
		else {
			this.name = name;
			this.value = value;
		}
	}

	/**
	 * <p>Get key name.</p>
	 * @return key name. Can't be null or empty
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Get value associated.</p>
	 * @return value associated. Can't be null.
	 */
	public T getValue() {
		return value;
	}

	/**
	 * <p>Create named value instance.</p>
	 * @param <T> value type.
	 * @param name key name. Can't be null or empty
	 * @param value value associated. Can't be null
	 * @return instance created. Can't be null
	 * @throws IllegalArgumentException name is null or empty
	 * @throws NullPointerException value is null
	 */
	public static <T> NamedValue<T> of(final String name, final T value) {
		return new NamedValue<T>(name, value);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		NamedValue other = (NamedValue) obj;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamedValue [name=" + name + ", value=" + value + "]";
	}
}
