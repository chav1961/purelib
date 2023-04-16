package chav1961.purelib.matrix.interfaces;

import chav1961.purelib.basic.exceptions.CalculationException;

/**
 * <p>This interface describes a matrix of floating-point values and a set of operations for it.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @see IntMatrix
 * @see LongMatrix
 * @see FloatMatrix
 * @see DoubleMatrix
 */
public interface Matrix {
	Class<?> getContentType();
	int getDimensions();
	int getSize(int dimension);
	Matrix add(Matrix another);
	Matrix sub(Matrix another);
	Matrix mul(Matrix another);
	Matrix inv() throws CalculationException;
	Matrix transp();
	Matrix add(Number number);
	Matrix mul(Number number);
	double getEpsilon();
	Matrix setEpsilon(double epsilon);
}
