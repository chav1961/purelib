package chav1961.purelib.matrix.interfaces;

import java.util.function.DoubleUnaryOperator;

public interface DoubleMatrix extends Matrix<DoubleMatrix> {
	DoubleMatrix function(DoubleUnaryOperator op);
	void get(int from, double[] content, int to, int length);
	void set(double[] content, int from, int to, int length);
}
