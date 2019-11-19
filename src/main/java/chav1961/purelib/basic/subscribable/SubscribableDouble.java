package chav1961.purelib.basic.subscribable;

public class SubscribableDouble extends Subscribable<SubscribableDoubleListener>{
	private final boolean	multithread;
	private volatile double	value = 0;

	public SubscribableDouble() {
		this(false);
	}
	
	public SubscribableDouble(final boolean multithread) {
		super(SubscribableDoubleListener.class);
		this.multithread = multithread;
	}
	
	public double get() {
		return value;
	}
	
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
