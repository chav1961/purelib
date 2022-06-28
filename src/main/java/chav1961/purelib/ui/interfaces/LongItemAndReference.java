package chav1961.purelib.ui.interfaces;

public interface LongItemAndReference<P> extends ItemAndReference<P> {
	long getValue();
	long getValue(int position);
	void setValue(long value);
}
