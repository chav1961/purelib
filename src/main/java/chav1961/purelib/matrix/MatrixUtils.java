package chav1961.purelib.matrix;

import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.matrix.interfaces.Matrix;

public class MatrixUtils {
	public static Matrix<?> zero(final Class<?> type, int size) {
		return zero(type,size,size);
	}

	public static Matrix<?> zero(final Class<?> type, final int sizeX, final int sizeY) {
		if (type == null) {
			throw new NullPointerException("Class type can't be null");
		}
		else if (sizeX <= 0) {
			throw new IllegalArgumentException("X size ["+sizeX+"] must be positive");
		}
		else if (sizeY <= 0) {
			throw new IllegalArgumentException("Y size ["+sizeY+"] must be positive");
		}
		else {
			switch (CompilerUtils.defineClassType(type)) {
				case CompilerUtils.CLASSTYPE_INT	:
					final int[]		intContent = new int[sizeX*sizeY];
					
					return new IntMatrixImpl(sizeX, sizeY, intContent);
				case CompilerUtils.CLASSTYPE_LONG	:	
					final long[]	longContent = new long[sizeX*sizeY];
					
					return new LongMatrixImpl(sizeX, sizeY, longContent);
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					final float[]	floatContent = new float[sizeX*sizeY];
					
					return new FloatMatrixImpl(sizeX, sizeY, floatContent);
				case CompilerUtils.CLASSTYPE_DOUBLE	:
					final double[]	doubleContent = new double[sizeX*sizeY];
					
					return new DoubleMatrixImpl(sizeX, sizeY, doubleContent);
				default : throw new IllegalArgumentException("Matrix to create has unsupported type ["+type.getCanonicalName()+"]");
			}
		}
	}
	
	public static Matrix<?> identity(final Class<?> type, final int size) {
		if (type == null) {
			throw new NullPointerException("Class type can't be null");
		}
		else if (size <= 0) {
			throw new IllegalArgumentException("Size ["+size+"] must be positive");
		}
		else {
			switch (CompilerUtils.defineClassType(type)) {
				case CompilerUtils.CLASSTYPE_INT	:
					final int[]		intContent = new int[size*size];
					
					for (int index = 0, maxIndex = intContent.length; index < maxIndex; index += size+1) {
						intContent[index] = 1;
					}
					return new IntMatrixImpl(size, size, intContent);
				case CompilerUtils.CLASSTYPE_LONG	:	
					final long[]	longContent = new long[size*size];
					
					for (int index = 0, maxIndex = longContent.length; index < maxIndex; index += size+1) {
						longContent[index] = 1;
					}
					return new LongMatrixImpl(size, size, longContent);
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					final float[]	floatContent = new float[size*size];
					
					for (int index = 0, maxIndex = floatContent.length; index < maxIndex; index += size+1) {
						floatContent[index] = 1;
					}
					return new FloatMatrixImpl(size, size, floatContent);
				case CompilerUtils.CLASSTYPE_DOUBLE	:
					final double[]	doubleContent = new double[size*size];
					
					for (int index = 0, maxIndex = doubleContent.length; index < maxIndex; index += size+1) {
						doubleContent[index] = 1;
					}
					return new DoubleMatrixImpl(size, size, doubleContent);
				default : throw new IllegalArgumentException("Matrix to create has unsupported type ["+type.getCanonicalName()+"]");
			}
		}
	}
	
	public static Matrix<?> filled(final Class<?> type, final int sizeX, final int sizeY, final Number filled) {
		if (type == null) {
			throw new NullPointerException("Class type can't be null");
		}
		else if (sizeX <= 0) {
			throw new IllegalArgumentException("X xize ["+sizeX+"] must be positive");
		}
		else if (sizeY <= 0) {
			throw new IllegalArgumentException("Y xize ["+sizeY+"] must be positive");
		}
		else if (filled == null) {
			throw new NullPointerException("Value to fill can't be null");
		}
		else {
			switch (CompilerUtils.defineClassType(type)) {
				case CompilerUtils.CLASSTYPE_INT	:
					final int[]		intContent = new int[sizeX*sizeY];
					final int		intVal = filled.intValue();
					
					for (int index = 0, maxIndex = intContent.length; index < maxIndex; index++) {
						intContent[index] = intVal;
					}
					
					return new IntMatrixImpl(sizeX, sizeY, intContent);
				case CompilerUtils.CLASSTYPE_LONG	:	
					final long[]	longContent = new long[sizeX*sizeY];
					final long		longVal = filled.longValue();
					
					for (int index = 0, maxIndex = longContent.length; index < maxIndex; index++) {
						longContent[index] = longVal;
					}
					
					return new LongMatrixImpl(sizeX, sizeY, longContent);
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					final float[]	floatContent = new float[sizeX*sizeY];
					final float		floatVal = filled.floatValue();
					
					for (int index = 0, maxIndex = floatContent.length; index < maxIndex; index++) {
						floatContent[index] = floatVal;
					}
					
					return new FloatMatrixImpl(sizeX, sizeY, floatContent);
				case CompilerUtils.CLASSTYPE_DOUBLE	:
					final double[]	doubleContent = new double[sizeX*sizeY];
					final double	doubleVal = filled.doubleValue();
					
					for (int index = 0, maxIndex = doubleContent.length; index < maxIndex; index++) {
						doubleContent[index] = doubleVal;
					}
					
					return new DoubleMatrixImpl(sizeX, sizeY, doubleContent);
				default : throw new IllegalArgumentException("Matrix to create has unsupported type ["+type.getCanonicalName()+"]");
			}
		}
	}

	static boolean areDimensions2AddValid(final Matrix<?> first, final Matrix<?> second) {
		final int	firstDim = first.getDimensions(), secondDim = second.getDimensions();
		
		if (firstDim != secondDim) {
			return false;
		}
		else {
			for (int index = 0; index < firstDim; index++) {
				if (first.getSize(index) != second.getSize(index)) {
					return false;
				}
			}
			return true;
		}
	}

	static boolean areDimensions2MulValid(final Matrix<?> first, final Matrix<?> second) {
		return first.getDimensions() == 2 && second.getDimensions() == 2 && first.getSize(1) == second.getSize(0);
	}

	static String printDimensions(final Matrix<?> matrix) {
		final StringBuilder	sb = new StringBuilder();
		
		for (int index = 0, maxIndex = matrix.getDimensions(); index < maxIndex; index++) {
			sb.append('x').append(matrix.getSize(index));
		}
		return sb.substring(1);
	}

	static boolean floatArraysEquals(final float[] first, final float[] second, final double epsilon) {
		if (first != null && second != null && first.length == second.length) {
			for (int index = 0, maxIndex = first.length; index < maxIndex; index++) {
				if (Math.abs(first[index]-second[index]) > epsilon) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	static boolean doubleArraysEquals(final double[] first, final double[] second, final double epsilon) {
		if (first != null && second != null && first.length == second.length) {
			for (int index = 0, maxIndex = first.length; index < maxIndex; index++) {
				if (Math.abs(first[index]-second[index]) > epsilon) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	static int indices2Displ(final Matrix<?> matrix, final int... indices) {
		if (indices == null) {
			throw new NullPointerException("Indices can't be null");
		}
		else if (matrix.getDimensions() != indices.length) {
			throw new IllegalArgumentException("Illegal number of indices ["+indices.length+"], awaited is ["+matrix.getDimensions()+"]");
		}
		else {
			int displ = indices[indices.length - 1];
			
			for(int dim = 0; dim < matrix.getDimensions()-1; dim++) {
				int current = indices[dim];
				
				if (current < 0 || current >= matrix.getSize(dim)) {
					throw new IllegalArgumentException("Index number ["+dim+"]: index value ["+current+"] out of range 0.."+(matrix.getSize(dim) - 1));
				}
				else {
					displ += current*matrix.getSize(dim + 1);
				}
			}
			return displ;
		}
	}
}
