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
public interface OldMatrix<M extends OldMatrix<?>> {
	/**
	 * <p>Get content of the matrix.</p>
	 * @return content type. Can't be null. Can be primitive class
	 */
	Class<?> getContentType();
	
	/**
	 * <p>Get matrix dimensions.</p>
	 * @return number of matrix dimensions. Always greater than 0
	 */
	int getDimensions();
	
	/**
	 * <p>Get matrix size on i-dimension</p>
	 * @param dimension dimension number. Must be greater than 0 and less than {{@link #getDimensions()} value
	 * @return dimension size. Always greater than 0
	 */
	int getSize(int dimension);
	
	/**
	 * <p>Add two matrix and return it's sum</p>
	 * @param another matrix to add
	 * @return new matrix instance with calculated values
	 */
	M add(OldMatrix<?> another);
	
	/**
	 * <p>Subtract two matrix and return it's subtraction</p>
	 * @param another matrix to subtract
	 * @return new matrix instance with calculated values
	 */
	M sub(OldMatrix<?> another);
	
	/**
	 * <p>Multiply two matrix and return it's production</p>
	 * @param another matrix to multiply
	 * @return new matrix instance with calculated values
	 */
	M mul(OldMatrix<?> another);
	
	/**
	 * <p>Multiply two matrix as Hadamard multiplication and return it's production</p>
	 * @param another matrix to multiply
	 * @return new matrix instance with calculated values
	 */
	M h_mul(OldMatrix<?> another);
	
	/**
	 * <p>Calculate inverse matrix.</p>
	 * @return new matrix instance with calculated values
	 * @throws CalculationException determinant is zero or matrix contains non-float content 
	 */
	M inv() throws CalculationException;
	
	/**
	 * <p>Transpose matrix</p>
	 * @return new matrix instance with calculated values
	 */
	M transp();
	
	/**
	 * <p>Add scalar to all the matrix items</p>
	 * @param number scalar to add
	 * @return new matrix instance with calculated values
	 */
	M add(Number number);
	
	/**
	 * <p>Multiply scalat with all the matrix items</p>
	 * @param number scalar to multiply
	 * @return new matrix instance with calculated values
	 */
	M mul(Number number);

	/**
	 * <p>Get accuracy for special matrix operations</p>
	 * @return accuracy. Can't be less than 0
	 */
	double getEpsilon();
	
	/**
	 * <o>Set accuracy for special matrix operation</p>
	 * @param epsilon accuracy to set. Can't be less than 0 
	 * @return self
	 */
	M setEpsilon(double epsilon);
}
