package chav1961.purelib.ui.interfaces;

public interface ObjectItemAndReference<K, P> extends ItemAndReference<P> {
	K getValue();
	void setValue(K value);
}
