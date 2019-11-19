package chav1961.purelib.basic.subscribable;

public class SubscribableBoolean extends Subscribable<SubscribableBooleanListener>{
	private final boolean		multithread;
	private volatile boolean	value = false;

	public SubscribableBoolean() {
		this(false);
	}
	
	public SubscribableBoolean(final boolean multithread) {
		super(SubscribableBooleanListener.class);
		this.multithread = multithread;
	}
	
	public boolean get() {
		return value;
	}
	
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
