package chav1961.purelib.matrix.internal;

import java.net.URI;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.Type;
import chav1961.purelib.matrix.interfaces.MatrixFactory;

public class OrdinalMatrixFactory implements MatrixFactory, SpiService<MatrixFactory> {
	public static final String	SUBSCHEME = "standalone";
	public static final URI		FACTORY_URI =  URI.create(MATRIX_FACTORY_SCHEME+":"+SUBSCHEME+":/");
	
	@Override
	public boolean canServe(URI resource) throws NullPointerException {
		if (resource == null) {
			throw new NullPointerException("Resource to test can;t be null");
		}
		else {
			return URIUtils.canServeURI(resource, FACTORY_URI);
		}
	}

	@Override
	public MatrixFactory newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (resource == null) {
			throw new NullPointerException("Resource to test can;t be null");
		}
		else if (!canServe(resource)) {
			throw new IllegalArgumentException("This service cant serve URI ["+resource+"]");
		}
		else {
			return this;
		}
	}

	@Override
	public Matrix newMatrix(final Type type, final int rows, final int cols) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of rows ["+cols+"] must be greater than 0");
		}
		else if (1L * rows * cols * type.getNumberOfItems() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Matrix size ["+rows+"x"+cols+"] is too long");
		}
		else {
			switch (type) {
				case BIT			:
					return new DoubleComplexMatrix(rows, cols);
				case COMPLEX_DOUBLE	:
					return new DoubleComplexMatrix(rows, cols);
				case COMPLEX_FLOAT	:
					return new FloatComplexMatrix(rows, cols);
				case REAL_DOUBLE	:
					return new DoubleRealMatrix(rows, cols);
				case REAL_FLOAT		:
					return new FloatRealMatrix(rows, cols);
				case REAL_INT		:
					return new DoubleComplexMatrix(rows, cols);
				case REAL_LONG		:
					return new DoubleComplexMatrix(rows, cols);
				default :
					throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix newMatrix(final Type type, final int rows, final int cols, final int... values) {
		if (values == null) {
			throw new NullPointerException("Values to assign can't be null");
		}
		else {
			final Matrix	m = newMatrix(type, rows, cols);
			
			m.assign(values);
			return m;
		}
	}

	@Override
	public Matrix newMatrix(final Type type, final int rows, final int cols, final long... values) {
		if (values == null) {
			throw new NullPointerException("Values to assign can't be null");
		}
		else {
			final Matrix	m = newMatrix(type, rows, cols);
			
			m.assign(values);
			return m;
		}
	}

	@Override
	public Matrix newMatrix(final Type type, final int rows, final int cols, final float... values) {
		if (values == null) {
			throw new NullPointerException("Values to assign can't be null");
		}
		else {
			final Matrix	m = newMatrix(type, rows, cols);
			
			m.assign(values);
			return m;
		}
	}

	@Override
	public Matrix newMatrix(final Type type, final int rows, final int cols, final double... values) {
		if (values == null) {
			throw new NullPointerException("Values to assign can't be null");
		}
		else {
			final Matrix	m = newMatrix(type, rows, cols);
			
			m.assign(values);
			return m;
		}
	}

}
