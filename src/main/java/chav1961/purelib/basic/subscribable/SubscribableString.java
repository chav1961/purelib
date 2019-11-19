package chav1961.purelib.basic.subscribable;

public class SubscribableString extends Subscribable<SubscribableStringListener>{
	private final boolean	multithread;
	private volatile String	value = null;

	public SubscribableString() {
		this(false);
	}
	
	public SubscribableString(final boolean multithread) {
		super(SubscribableStringListener.class);
		this.multithread = multithread;
	}
	
	public String get() {
		return value;
	}
	
	public void set(final String newValue) {
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
	
	private void innerSet(final String newValue) {
		final String	oldValue = value;
		
		value = newValue;
		fireChange((listener)->listener.process(oldValue,value));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SubscribableString other = (SubscribableString) obj;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubscribableString [value=" + value + "]";
	}
}
