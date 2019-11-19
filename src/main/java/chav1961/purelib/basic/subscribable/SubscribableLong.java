package chav1961.purelib.basic.subscribable;

public class SubscribableLong extends Subscribable<SubscribableLongListener>{
	private final boolean	multithread;
	private volatile long	value = 0;

	public SubscribableLong() {
		this(false);
	}
	
	public SubscribableLong(final boolean multithread) {
		super(SubscribableLongListener.class);
		this.multithread = multithread;
	}
	
	public long get() {
		return value;
	}
	
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
