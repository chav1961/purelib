package chav1961.purelib.basic.subscribable;

/**
 * <p>This class describes listenable float value. When it's value changes, all the listeners in the given instance will receive events.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class SubscribableFloat extends PrimitiveSubscribable<SubscribableFloatListener>{
	private final boolean	multithread;
	private volatile float	value = 0;

	/**
	 * <p>Constructor of the class</p>
	 */
	public SubscribableFloat() {
		this(false);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param multithread use multithreaded version of the container
	 */
	public SubscribableFloat(final boolean multithread) {
		super(SubscribableFloatListener.class);
		this.multithread = multithread;
	}
	
	/**
	 * <p>Get current value of the container content</p>
	 * @return current value of the content;
	 */
	public float get() {
		return value;
	}
	
	/**
	 * <p>Set new content value for the given container</p>
	 * @param newValue new value to set
	 */
	public void set(final float newValue) {
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
	
	private void innerSet(final float newValue) {
		final float 	oldValue = value;
		
		value = newValue;
		fireChange((listener)->listener.process(oldValue,value));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SubscribableFloat other = (SubscribableFloat) obj;
		if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubscribableFloat [value=" + value + "]";
	}
}
