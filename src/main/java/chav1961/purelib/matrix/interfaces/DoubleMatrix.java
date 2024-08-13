package chav1961.purelib.matrix.interfaces;

import java.util.function.DoubleUnaryOperator;

public interface DoubleMatrix extends OldMatrix<DoubleMatrix> {
	DoubleMatrix function(DoubleUnaryOperator op);
	double get(int... indices);
	void set(double value, int... indices);
	void get(int from, double[] content, int to, int length);
	void set(double[] content, int from, int to, int length);
}
