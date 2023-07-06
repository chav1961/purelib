package chav1961.purelib.matrix.interfaces;

import java.util.function.LongUnaryOperator;

public interface LongMatrix extends Matrix<LongMatrix> {
	LongMatrix function(LongUnaryOperator op);
	void get(int from, long[] content, int to, int length);
	void set(long[] content, int from, int to, int length);
}
