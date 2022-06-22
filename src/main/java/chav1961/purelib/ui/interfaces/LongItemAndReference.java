package chav1961.purelib.ui.interfaces;

public interface LongItemAndReference<P> extends ItemAndReference<P> {
	long getValue();
	void setValue(long value);
}
