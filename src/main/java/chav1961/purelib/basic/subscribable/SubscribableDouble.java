package chav1961.purelib.basic.subscribable;

/**
 * <p>This class describes listenable double value. When it's value changes, all the listeners in the given instance will receive events.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class SubscribableDouble extends Subscribable<SubscribableDoubleListener>{
	private final boolean	multithread;
	private volatile double	value = 0;

	/**
	 * <p>Constructor of the class</p>
	 */
	public SubscribableDouble() {
		this(false);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param multithread use multithreaded version of the container
	 */
	public SubscribableDouble(final boolean multithread) {
		super(SubscribableDoubleListener.class);
		this.multithread = multithread;
	}
	
	/**
	 * <p>Get current value of the container content</p>
	 * @return current value of the content;
	 */
	public double get() {
		return value;
	}
	
	/**
	 * <p>Set new content value for the given container</p>
	 * @param newValue new value to set
	 */
	public void set(final double newValue) {
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
	
	private void innerSet(final double newValue) {
		final double 	oldValue = value;
		
		value = newValue;
		fireChange((listener)->listener.process(oldValue,value));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SubscribableDouble other = (SubscribableDouble) obj;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubscribableDouble [value=" + value + "]";
	}
}
