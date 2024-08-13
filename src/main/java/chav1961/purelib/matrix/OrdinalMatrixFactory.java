package chav1961.purelib.matrix;

import java.net.URI;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.matrix.interfaces.OldMatrix;
import chav1961.purelib.matrix.interfaces.MatrixFactory;

public class OrdinalMatrixFactory implements MatrixFactory, SpiService<MatrixFactory> {
	private static final String			SUBSCHEME = "ordinal";
	private static final URI			SERVE = URI.create(MATRIX_FACTORY_SCHEME+":"+SUBSCHEME+":/");
	
	public OrdinalMatrixFactory() {
	}
	
	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		return URIUtils.canServeURI(resource, SERVE); 
	}

	@Override
	public MatrixFactory newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		return this;
	}

	@Override
	public OldMatrix<?> newMatrix(final Class<?> content, final int rows, final int cols) {
		if (content == null) {
			throw new NullPointerException("Content class can't be null"); 
		}
		else if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0"); 
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0"); 
		}
		else {
			switch (CompilerUtils.defineClassType(content)) {
				case CompilerUtils.CLASSTYPE_INT	:
					return new IntMatrixImpl(rows, cols);
				case CompilerUtils.CLASSTYPE_LONG	:
					return new LongMatrixImpl(rows, cols);
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					return new FloatMatrixImpl(rows, cols);
				case CompilerUtils.CLASSTYPE_DOUBLE	:
					return new DoubleMatrixImpl(rows, cols);
				default :
					throw new UnsupportedOperationException("Matrix content class ["+content.getCanonicalName()+"] is not supported yet"); 
			}
		}
	}

	@Override
	public OldMatrix newMatrix(final int rows, final int cols, final int... content) {
		if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0"); 
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0"); 
		}
		else if (content == null) {
			throw new NullPointerException("Int content to fill can't be null"); 
		}
		else {
			return new IntMatrixImpl(rows, cols, content);
		}
	}

	@Override
	public OldMatrix newMatrix(final int rows, final int cols, final long... content) {
		if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0"); 
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0"); 
		}
		else if (content == null) {
			throw new NullPointerException("Long content to fill can't be null"); 
		}
		else {
			return new LongMatrixImpl(rows, cols, content);
		}
	}

	@Override
	public OldMatrix newMatrix(final int rows, final int cols, final float... content) {
		if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0"); 
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0"); 
		}
		else if (content == null) {
			throw new NullPointerException("Float content to fill can't be null"); 
		}
		else {
			return new FloatMatrixImpl(rows, cols, content);
		}
	}

	@Override
	public OldMatrix newMatrix(final int rows, final int cols, final double... content) {
		if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0"); 
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0"); 
		}
		else if (content == null) {
			throw new NullPointerException("Double content to fill can't be null"); 
		}
		else {
			return new DoubleMatrixImpl(rows, cols, content);
		}
	}
}
