package chav1961.purelib.basic.subscribable;

public class SubscribableObject<T> extends Subscribable<SubscribableObjectListener>{
	private final boolean	multithread;
	private volatile T		value = null;

	public SubscribableObject() {
		this(false);
	}
	
	public SubscribableObject(final boolean multithread) {
		super(SubscribableObjectListener.class);
		this.multithread = multithread;
	}
	
	public T get() {
		return value;
	}
	
	public void set(final T newValue) {
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
	
	private void innerSet(final T newValue) {
		final T 	oldValue = value;
		
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
		SubscribableObject other = (SubscribableObject) obj;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubscribableObject [value=" + value + "]";
	}
}
