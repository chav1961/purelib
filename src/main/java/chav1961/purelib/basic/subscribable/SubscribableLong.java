package chav1961.purelib.basic.subscribable;

/**
 * <p>This class describes listenable long value. When it's value changes, all the listeners in the given instance will receive events.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class SubscribableLong extends PrimitiveSubscribable<SubscribableLongListener>{
	private final boolean	multithread;
	private volatile long	value = 0;

	/**
	 * <p>Constructor of the class</p>
	 */
	public SubscribableLong() {
		this(false);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param multithread use multithreaded version of the container
	 */
	public SubscribableLong(final boolean multithread) {
		super(SubscribableLongListener.class);
		this.multithread = multithread;
	}
	
	/**
	 * <p>Get current value of the container content</p>
	 * @return current value of the content;
	 */
	public long get() {
		return value;
	}
	
	/**
	 * <p>Set new content value for the given container</p>
	 * @param newValue new value to set
	 */
	public void set(final long newValue) {
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
	
	private void innerSet(final long newValue) {
		final long 	oldValue = value;
		
		value = newValue;
		fireChange((listener)->listener.process(oldValue,value));
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
		SubscribableLong other = (SubscribableLong) obj;
		if (value != other.value) return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubscribableLong [value=" + value + "]";
	}
}
