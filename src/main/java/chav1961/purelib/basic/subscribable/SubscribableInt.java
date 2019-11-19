package chav1961.purelib.basic.subscribable;

public class SubscribableInt extends Subscribable<SubscribableIntListener>{
	private final boolean	multithread;
	private volatile int	value = 0;

	public SubscribableInt() {
		this(false);
	}
	
	public SubscribableInt(final boolean multithread) {
		super(SubscribableIntListener.class);
		this.multithread = multithread;
	}
	
	public int get() {
		return value;
	}
	
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
