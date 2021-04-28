package chav1961.purelib.basic.subscribable;

/**
 * <p>This class describes listenable boolean value. When it's value changes, all the listeners in the given instance will receive events.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class SubscribableBoolean extends PrimitiveSubscribable<SubscribableBooleanListener>{
	private final boolean		multithread;
	private volatile boolean	value = false;

	/**
	 * <p>Constructor of the class</p>
	 */
	public SubscribableBoolean() {
		this(false);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param multithread use multithreaded version of the container
	 */
	public SubscribableBoolean(final boolean multithread) {
		super(SubscribableBooleanListener.class);
		this.multithread = multithread;
	}
	
	/**
	 * <p>Get current value of the container content</p>
	 * @return current value of the content;
	 */
	public boolean get() {
		return value;
	}
	
	/**
	 * <p>Set new content value for the given container</p>
	 * @param newValue new value to set
	 */
	public void set(final boolean newValue) {
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
	
	private void innerSet(final boolean newValue) {
		final boolean 	oldValue = value;
		
		value = newValue;
		fireChange((listener)->listener.process(oldValue,value));
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
		SubscribableBoolean other = (SubscribableBoolean) obj;
		if (value != other.value) return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubscribableBoolean [value=" + value + "]";
	}
}
