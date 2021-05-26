package chav1961.purelib.matrix;

import java.util.Arrays;

import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.matrix.interfaces.DoubleMatrix;
import chav1961.purelib.matrix.interfaces.FloatMatrix;
import chav1961.purelib.matrix.interfaces.IntMatrix;
import chav1961.purelib.matrix.interfaces.LongMatrix;
import chav1961.purelib.matrix.interfaces.Matrix;

public class DoubleMatrixImpl implements DoubleMatrix {
	private final int		sizeX, sizeY;
	private final double[]	content;
	private double			epsilon = 1e-10;
	
	public DoubleMatrixImpl(final int sizeX, final int sizeY, final double... filled) {
		this(sizeX,sizeY,true,filled);
	}

	protected DoubleMatrixImpl(final int sizeX, final int sizeY, final boolean cloneContent, final double... filled) {
		if (sizeX <= 0) {
			throw new IllegalArgumentException("X size ["+sizeX+"] must be greater than 0"); 
		}
		else if (sizeY <= 0) {
			throw new IllegalArgumentException("Y size ["+sizeY+"] must be greater than 0"); 
		}
		else if (filled == null) {
			throw new NullPointerException("Initial values can't be null");
		}
		else if (filled.length != sizeX*sizeY) {
			throw new NullPointerException("Initial values contain ["+filled.length+"] items, mut must contain ["+sizeX*sizeY+"]");
		}
		else {
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			this.content = cloneContent ? filled.clone() : filled;
		}
	}
	
	@Override
	public Class<?> getContentType() {
		return double.class;
	}

	@Override
	public int getDimensions() {
		return 2;
	}

	@Override
	public int getSize(final int dimension) {
		switch (dimension) {
			case 0	: return sizeX;
			case 1	: return sizeY;
			default : throw new IllegalArgumentException("Dimension number ["+dimension+"] can be 0 or 1 only");  
		}
	}

	@Override
	public Matrix add(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null"); 
		}
		else if (!MatrixUtils.areDimensions2AddValid(this,another)) {
			throw new IllegalArgumentException("Matrix to add has dimensions ["+MatrixUtils.printDimensions(another)+"] differ with current matrix ["+MatrixUtils.printDimensions(this)+"]"); 
		}
		else {
			final double[]	result = content.clone();
		
			switch (CompilerUtils.defineClassType(another.getContentType())) {
				case CompilerUtils.CLASSTYPE_INT	:
					final int				intSize = content.length;
					final int[]				anotherInt = new int[intSize];
					
					((IntMatrix)another).get(0, anotherInt, 0, intSize);
					for (int index = 0; index < intSize; index++) {
						result[index] += anotherInt[index];
					}
					break;
				case CompilerUtils.CLASSTYPE_LONG	:	
					final int				longSize = content.length;
					final long[]			anotherLong = new long[longSize];
					
					((LongMatrix)another).get(0, anotherLong, 0, longSize);
					for (int index = 0; index < longSize; index++) {
						result[index] += anotherLong[index];
					}
					break;
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					final int				floatSize = content.length;
					final float[]			anotherFloat = new float[floatSize];
					
					((FloatMatrix)another).get(0, anotherFloat, 0, floatSize);
					for (int index = 0; index < floatSize; index++) {
						result[index] += anotherFloat[index];
					}
					break;
				case CompilerUtils.CLASSTYPE_DOUBLE	:
					final int				doubleSize = content.length;
					final double[]			anotherDouble = new double[doubleSize];
					
					((DoubleMatrix)another).get(0, anotherDouble, 0, doubleSize);
					for (int index = 0; index < doubleSize; index++) {
						result[index] += anotherDouble[index];
					}
					break;
				default : throw new IllegalArgumentException("Matrix to add has unsupporte type ["+another.getContentType().getCanonicalName()+"]");
			}
			return new DoubleMatrixImpl(sizeX, sizeY, false, result);
		}
	}

	@Override
	public Matrix sub(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null"); 
		}
		else if (!MatrixUtils.areDimensions2AddValid(this,another)) {
			throw new IllegalArgumentException("Matrix to subtract has dimensions ["+MatrixUtils.printDimensions(another)+"] differ with current matrix ["+MatrixUtils.printDimensions(this)+"]"); 
		}
		else {
			final double[]	result = content.clone();
			
			switch (CompilerUtils.defineClassType(another.getContentType())) {
				case CompilerUtils.CLASSTYPE_INT	:
					final int				intSize = content.length;
					final int[]				anotherInt = new int[intSize];
					
					((IntMatrix)another).get(0, anotherInt, 0, intSize);
					for (int index = 0; index < intSize; index++) {
						result[index] -= anotherInt[index];
					}
					break;
				case CompilerUtils.CLASSTYPE_LONG	:	
					final int				longSize = content.length;
					final long[]			anotherLong = new long[longSize];
					
					((LongMatrix)another).get(0, anotherLong, 0, longSize);
					for (int index = 0; index < longSize; index++) {
						result[index] -= anotherLong[index];
					}
					break;
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					final int				floatSize = content.length;
					final float[]			anotherFloat = new float[floatSize];
					
					((FloatMatrix)another).get(0, anotherFloat, 0, floatSize);
					for (int index = 0; index < floatSize; index++) {
						result[index] -= anotherFloat[index];
					}
					break;
				case CompilerUtils.CLASSTYPE_DOUBLE	:
					final int				doubleSize = content.length;
					final double[]			anotherDouble = new double[doubleSize];
					
					((DoubleMatrix)another).get(0, anotherDouble, 0, doubleSize);
					for (int index = 0; index < doubleSize; index++) {
						result[index] -= anotherDouble[index];
					}
					break;
				default : throw new IllegalArgumentException("Matrix to add has unsupporte type ["+another.getContentType().getCanonicalName()+"]");
			}
			return new DoubleMatrixImpl(sizeX, sizeY, false, result);
		}
	}

	@Override
	public Matrix mul(final Matrix another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null"); 
		}
		else if (!MatrixUtils.areDimensions2MulValid(this,another)) {
			throw new IllegalArgumentException("Matrix to multiply has dimensions ["+MatrixUtils.printDimensions(another)+"] differ with current matrix ["+MatrixUtils.printDimensions(this)+"]"); 
		}
		else {
			final int		anotherSize = another.getSize(0) * another.getSize(1);  
			final int		anotherX = another.getSize(0);
			final double[]	sum = new double[sizeY * anotherX];
					
			switch (CompilerUtils.defineClassType(another.getContentType())) {
				case CompilerUtils.CLASSTYPE_INT	:
					final int[]			anotherInt = new int[anotherSize];
					
					((IntMatrix)another).get(0, anotherInt, 0, anotherSize);
					for (int x = 0; x < sizeY; x++) {
					    for (int y = 0; y < anotherX; y++) {
						      for (int index = 0; index < sizeX; index++) {
							      sum[y + sizeY * x] += content[index + sizeX * x] * anotherInt[y + anotherX * index];
						      }
					    }
					}
					break;
				case CompilerUtils.CLASSTYPE_LONG	:	
					final long[]			anotherLong = new long[anotherSize];
					
					((LongMatrix)another).get(0, anotherLong, 0, anotherSize);
					for (int x = 0; x < sizeY; x++) {
					    for (int y = 0; y < anotherX; y++) {
						      for (int index = 0; index < sizeX; index++) {
							      sum[y + sizeY * x] += content[index + sizeX * x] * anotherLong[y + anotherX * index];
						      }
					    }
					}
					break;
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					final float[]			anotherFloat = new float[anotherSize];
					
					((FloatMatrix)another).get(0, anotherFloat, 0, anotherSize);
					for (int x = 0; x < sizeY; x++) {
					    for (int y = 0; y < anotherX; y++) {
						      for (int index = 0; index < sizeX; index++) {
							      sum[y + sizeY * x] += content[index + sizeX * x] * anotherFloat[y + anotherX * index];
						      }
					    }
					}
					break;
				case CompilerUtils.CLASSTYPE_DOUBLE	:
					final double[]			anotherDouble = new double[anotherSize];
					
					((DoubleMatrix)another).get(0, anotherDouble, 0, anotherSize);
					for (int x = 0; x < sizeY; x++) {
					    for (int y = 0; y < anotherX; y++) {
						      for (int index = 0; index < sizeX; index++) {
							      sum[y + sizeY * x] += content[index + sizeX * x] * anotherDouble[y + anotherX * index];
						      }
					    }
					}
					break;
				default : throw new IllegalArgumentException("Matrix to add has unsupported type ["+another.getContentType().getCanonicalName()+"]");
			}
			return new DoubleMatrixImpl(sizeY, anotherX, false, sum);
		}
	}

	@Override
	// see https://github.com/vkostyukov/la4j
	public Matrix inv() {
		if (sizeX != sizeY) {
			throw new IllegalStateException("Matrix to invert is not a square matrix");
		}
		else {
			final double[]	result = content.clone();
			double 			var, diagonalTerm;

	        for (int k = 0; k < sizeX; k++) {
	            diagonalTerm = result[k + k * sizeX];

	            if (Math.abs(diagonalTerm) <= Double.MIN_VALUE) {
	                throw new IllegalArgumentException("This matrix cannot be inverted with a non-pivoting Gauss elimination method (contains zeroes on main diagonal).");
	            }

	            var = 1.0 / diagonalTerm;
	            result[k + k * sizeX] = 1;

	            for (int j = 0; j < sizeY; j++) {
	            	result[k + j * sizeX] *= var;
	            }

	            for (int i = 0; i < sizeY; i++) {
	                if (i != k) {
		                var = result[i + k * sizeX];
		                result[i + k * sizeX] = 0;
		                
		                for (int j = 0; j < sizeY; j++) {
		                	result[i + j * sizeX] -= var * result[k + j * sizeX];
		                }
	                }
	            }
	        }			
			return new DoubleMatrixImpl(sizeX, sizeY, false, result);
		}
	}

	@Override
	public Matrix transp() {
		if (sizeX == 1 || sizeY == 1) {
			return new DoubleMatrixImpl(sizeY, sizeX, content);
		}
		else {
			final double[]	result = new double[content.length];
			int				target = 0;
			
			for (int x = 0; x < sizeX; x++) {
				for (int y = 0; y < sizeY; y++) {
					result[target++] = content[y * sizeX + x];
				}
			}
			return new DoubleMatrixImpl(sizeY, sizeX, false, result);
		}
	}

	@Override
	public Matrix add(final Number number) {
		if (number == null) {
			throw new NullPointerException("Number to add can't be null"); 
		}
		else {
			final double[]	result = content.clone();
			final double	val = number.doubleValue();
			
			for (int index = 0, maxIndex = result.length; index < maxIndex; index++) {
				result[index] += val;
			}
			return new DoubleMatrixImpl(sizeX, sizeY, false, result);
		}
	}

	@Override
	public Matrix mul(final Number number) {
		if (number == null) {
			throw new NullPointerException("Number to moltiply can't be null"); 
		}
		else {
			final double[]	result = content.clone();
			final double	val = number.doubleValue();
			
			for (int index = 0, maxIndex = result.length; index < maxIndex; index++) {
				result[index] *= val;
			}
			return new DoubleMatrixImpl(sizeX, sizeY, false, result);
		}
	}

	@Override
	public double getEpsilon() {
		return epsilon;
	}

	@Override
	public Matrix setEpsilon(final double epsilon) {
		this.epsilon = Math.abs(epsilon);
		return this;
	}

	
	@Override
	public void get(final int from, final double[] content, final int to, final int length) {
		System.arraycopy(this.content, from, content, to, length);
	}

	@Override
	public void set(final double[] content, final int from, final int to, final int length) {
		System.arraycopy(content, from, this.content, to, length);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(content);
		result = prime * result + sizeX;
		result = prime * result + sizeY;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DoubleMatrixImpl other = (DoubleMatrixImpl) obj;
		if (!MatrixUtils.doubleArraysEquals(content, other.content, epsilon)) return false;
		if (sizeX != other.sizeX) return false;
		if (sizeY != other.sizeY) return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder	sb = new StringBuilder("DoubleMatrixImpl [").append(MatrixUtils.printDimensions(this)).append("] :\n");

		for (int x = 0; x < sizeX; x++) {
			sb.append('|');
			for (int y = 0; y < sizeY; y++) {
				sb.append(content[x + y*sizeX]).append(' ');
			}
			sb.append("|\n");
		}
		return sb.toString();
	}
}
