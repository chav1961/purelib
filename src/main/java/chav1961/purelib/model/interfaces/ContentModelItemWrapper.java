package chav1961.purelib.model.interfaces;


public interface ContentModelItemWrapper<T> extends NodeMetadataOwner {
	String getItemName();
	T getItem();
	void setItem(T item);
	Iterable<T> availableItems(CharSequence pattern);
	
	default Iterable<T> availableItems() {
		return availableItems(".*");
	}
}
