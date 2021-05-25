package chav1961.purelib.matrix.interfaces;

public interface FloatMatrix extends Matrix {
	void get(int from, float[] content, int to, int length);
	void set(float[] content, int from, int to, int length);
}
