package chav1961.purelib.basic.subscribable;

/**
 * <p>This class describes listenable int value. When it's value changes, all the listeners in the given instance will receive events.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class SubscribableInt extends PrimitiveSubscribable<SubscribableIntListener>{
	private final boolean	multithread;
	private volatile int	value = 0;

	/**
	 * <p>Constructor of the class</p>
	 */
	public SubscribableInt() {
		this(false);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param multithread use multithreaded version of the container
	 */
	public SubscribableInt(final boolean multithread) {
		super(SubscribableIntListener.class);
		this.multithread = multithread;
	}
	
	/**
	 * <p>Get current value of the container content</p>
	 * @return current value of the content;
	 */
	public int get() {
		return value;
	}
	
	/**
	 * <p>Set new content value for the given container</p>
	 * @param newValue new value to set
	 */
	public void set(final int newValue) {
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
	
	
	private void innerSet(final int newValue) {
		final int 	oldValue = value;
		
		value = newValue;
		fireChange((listener)->listener.process(oldValue,value));
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
		if (getClass() != obj.getClass())
			return false;
		SubscribableInt other = (SubscribableInt) obj;
		if (value != other.value) return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubscribableInt [value=" + value + "]";
	}
}
