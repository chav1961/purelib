package chav1961.purelib.basic.subscribable;

public class SubscribableFloat extends Subscribable<SubscribableFloatListener>{
	private final boolean	multithread;
	private volatile float	value = 0;

	public SubscribableFloat() {
		this(false);
	}
	
	public SubscribableFloat(final boolean multithread) {
		super(SubscribableFloatListener.class);
		this.multithread = multithread;
	}
	
	public float get() {
		return value;
	}
	
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
