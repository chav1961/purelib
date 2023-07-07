package chav1961.purelib.matrix.interfaces;

import java.util.function.DoubleUnaryOperator;

public interface FloatMatrix extends Matrix<FloatMatrix> {
	FloatMatrix function(DoubleUnaryOperator op);
	float get(int... indices);
	void set(float value, int... indices);
	void get(int from, float[] content, int to, int length);
	void set(float[] content, int from, int to, int length);
}
