package chav1961.purelib.matrix.interfaces;

import java.util.function.IntUnaryOperator;

public interface IntMatrix extends Matrix<IntMatrix> {
	IntMatrix function(IntUnaryOperator op);
	int get(int... indices);
	void set(int value, int... indices);
	void get(int from, int[] content, int to, int length);
	void set(int[] content, int from, int to, int length);
}
