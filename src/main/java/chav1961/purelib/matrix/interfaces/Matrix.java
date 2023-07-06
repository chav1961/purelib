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
public interface Matrix<M extends Matrix<?>> {
	Class<?> getContentType();
	int getDimensions();
	int getSize(int dimension);
	M add(Matrix<?> another);
	M sub(Matrix<?> another);
	M mul(Matrix<?> another);
	M h_mul(Matrix<?> another);
	M inv() throws CalculationException;
	M transp();
	M add(Number number);
	M mul(Number number);
	double getEpsilon();
	M setEpsilon(double epsilon);
}
