package chav1961.purelib.basic.interfaces;

public interface JsonIntCallbackInterface {
	void process(final int[] name, final char[] value, final int fromValue, final int valueLen);
	void process(final int[] name, final String value);
	void process(final int[] name, final int value);
	void process(final int[] name, final long value);
	void process(final int[] name, final double value);
	void process(final int[] name, final boolean value);
	void process(final int[] name);
}
