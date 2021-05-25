package chav1961.purelib.matrix.interfaces;

public interface IntMatrix extends Matrix {
	void get(int from, int[] content, int to, int length);
	void set(int[] content, int from, int to, int length);
}
