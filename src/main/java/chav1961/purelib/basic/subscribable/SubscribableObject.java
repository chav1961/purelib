package chav1961.purelib.basic.subscribable;

/**
 * <p>This class describes listenable long value. When it's value changes, all the listeners in the given instance will receive events.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @param <T> any referenced type to keep in the container
 */
@SuppressWarnings("rawtypes")
public class SubscribableObject<T> extends Subscribable<SubscribableObjectListener>{
	private final boolean	multithread;
	private volatile T		value = null;

	/**
	 * <p>Constructor of the class</p>
	 */
	public SubscribableObject() {
		this(false);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param multithread use multithreaded version of the container
	 */
	public SubscribableObject(final boolean multithread) {
		super(SubscribableObjectListener.class);
		this.multithread = multithread;
	}
	
	/**
	 * <p>Get current value of the container content</p>
	 * @return current value of the content;
	 */
	public T get() {
		return value;
	}
	
	/**
	 * <p>Set new content value for the given container</p>
	 * @param newValue new value to set
	 */
	public void set(final T newValue) {
		if (multithread) {
			synchronized(this) {
				innerSet(newValue);
			}
		}
		else {
			innerSet(newValue);
		}
	}
	
	@Override
	public void refresh() {
		innerSet(value);
	}
	
	@SuppressWarnings("unchecked")
	private void innerSet(final T newValue) {
		final T 	oldValue = value;
		
		value = newValue;
		fireChange((listener)->listener.process(oldValue,value));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SubscribableObject<?> other = (SubscribableObject<?>) obj;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubscribableObject [value=" + value + "]";
	}
}
