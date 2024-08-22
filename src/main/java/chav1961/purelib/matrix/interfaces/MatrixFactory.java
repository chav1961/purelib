package chav1961.purelib.matrix.interfaces;

import java.net.URI;
import java.util.ServiceLoader;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.SpiService;

/**
 * <p>This interface describes matrix factory to support matrix arithmetics.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public interface MatrixFactory {
	/**
	 * <p>Scheme name for the matrix factories</p>
	 */
	public static final String	MATRIX_FACTORY_SCHEME = "matrix";
	
	/**
	 * <p>Create zero matrix with the given rows and columns and given content type.</p>  
	 * @param content content type. Can't be null. Only int.class, ling.class, float.class and double.class are available
	 * @param rows number of rows in the matrix. Must be at least 1
	 * @param cols number of rows in the matrix. Must be at least 1
	 * @return matrix created
	 */
	OldMatrix<?> newMatrix(final Class<?> content, final int rows, final int cols);

	/**
	 * <p>Create int matrix with the given rows and columns and fill it with initial values.</p>  
	 * @param rows number of rows in the matrix. Must be at least 1
	 * @param cols number of rows in the matrix. Must be at least 1
	 * @param content content to fill. Can't be null, but can contain less data than required
	 * @return matrix created
	 */
	OldMatrix<?> newMatrix(final int rows, final int cols, final int... content);
	
	/**
	 * <p>Create long matrix with the given rows and columns and fill it with initial values.</p>  
	 * @param rows number of rows in the matrix. Must be at least 1
	 * @param cols number of rows in the matrix. Must be at least 1
	 * @param content content to fill. Can't be null, but can contain less data than required
	 * @return matrix created
	 */
	OldMatrix<?> newMatrix(final int rows, final int cols, final long... content);
	
	/**
	 * <p>Create float matrix with the given rows and columns and fill it with initial values.</p>  
	 * @param rows number of rows in the matrix. Must be at least 1
	 * @param cols number of rows in the matrix. Must be at least 1
	 * @param content content to fill. Can't be null, but can contain less data than required
	 * @return matrix created
	 */
	OldMatrix<?> newMatrix(final int rows, final int cols, final float... content);
	
	/**
	 * <p>Create double matrix with the given rows and columns and fill it with initial values.</p>  
	 * @param rows number of rows in the matrix. Must be at least 1
	 * @param cols number of rows in the matrix. Must be at least 1
	 * @param content content to fill. Can't be null, but can contain less data than required
	 * @return matrix created
	 */
	OldMatrix<?> newMatrix(final int rows, final int cols, final double... content);

	/**
	 * <p>This class is a factory to get matrix factory by it's URI. It implements a 'Factory' template.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public final static class Factory {
		private Factory() {}

		/**
		 * <p>Can matrix factory support given URI?</p> 
		 * @param matrixFactoryUri matrix factory URI to test. Can't be null
		 * @return true if can, false otherwise
		 * @throws IllegalArgumentException when localizer URI is null or doesn't have {@value Localizer#LOCALIZER_SCHEME} scheme
		 * @throws LocalizationException on any errors on creation localizer
		 */
		public static boolean canServe(final URI matrixFactoryUri) throws IllegalArgumentException, PreparationException {
			if (matrixFactoryUri == null) {
				throw new IllegalArgumentException("Matrix URI can't be null"); 
			}
			else {
				for (MatrixFactory item : ServiceLoader.load(MatrixFactory.class)) {
					if ((item instanceof SpiService) && ((SpiService<MatrixFactory>)item).canServe(matrixFactoryUri)) {
						return true;
					}
				}				
				return false;
			}
		}
		
		/**
		 * <p>Get matrix factory by URI.</p> 
		 * @param matrixFactoryUri matrix factory URI to get matrix factory for. Can't be null and must have scheme {@value MatrixFactory#MATRIX_FACTORY_SCHEME}
		 * @return matrix factory found
		 * @throws IllegalArgumentException when localizer URI is null or doesn't have {@value Localizer#LOCALIZER_SCHEME} scheme
		 * @throws LocalizationException on any errors on creation localizer
		 */
		public static MatrixFactory newInstance(final URI matrixFactoryUri) throws IllegalArgumentException, PreparationException {
			if (matrixFactoryUri == null || !MatrixFactory.MATRIX_FACTORY_SCHEME.equals(matrixFactoryUri.getScheme())) {
				throw new IllegalArgumentException("Matrix URI can't be null and must have scheme ["+MatrixFactory.MATRIX_FACTORY_SCHEME+"]"); 
			}
			else {
				for (MatrixFactory item : ServiceLoader.load(MatrixFactory.class)) {
					if ((item instanceof SpiService) && ((SpiService<MatrixFactory>)item).canServe(matrixFactoryUri)) {
						try {
							return ((SpiService<MatrixFactory>)item).newInstance(matrixFactoryUri);
						} catch (EnvironmentException e) {
							throw new PreparationException("Error creating matrix factory instance for the URI ["+matrixFactoryUri+"]: "+e.getLocalizedMessage());
						}
					}
				}				
				throw new PreparationException("No any MatrixFactory instances found for URI ["+matrixFactoryUri+"]");
			}
		}
	}
}
