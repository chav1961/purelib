package chav1961.purelib.matrix.interfaces;

public interface LongMatrix extends Matrix {
	void get(int from, long[] content, int to, int length);
	void set(long[] content, int from, int to, int length);
}
