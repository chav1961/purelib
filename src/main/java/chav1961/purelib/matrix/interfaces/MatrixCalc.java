package chav1961.purelib.matrix.interfaces;

import chav1961.purelib.basic.exceptions.CalculationException;

/**
 * <p>This interface describes matrix calculator. Implementation is used for repeatable matrix calculations.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * 
 */
public interface MatrixCalc extends AutoCloseable {
	/**
	 * <p>Calculate result with source parameters typed</p>
	 * @param parameters
	 * @return matrix calculated. Can't be null
	 * @throws CalculationException on any calculation errors
	 * @see Matrix#prepare(String)
	 */
	Matrix execute(Object... parameters) throws CalculationException;
	@Override
	void close() throws RuntimeException;
}
