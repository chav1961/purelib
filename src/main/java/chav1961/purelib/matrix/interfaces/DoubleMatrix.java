package chav1961.purelib.matrix.interfaces;

public interface DoubleMatrix extends Matrix {
	void get(int from, double[] content, int to, int length);
	void set(double[] content, int from, int to, int length);
}
