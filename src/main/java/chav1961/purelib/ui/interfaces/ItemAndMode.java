package chav1961.purelib.ui.interfaces;

public interface ItemAndMode<M extends Enum<?>, T> {
	M getMode();
	void setMode(M mode);
	T getItem();
	void setItem(T item);
}
