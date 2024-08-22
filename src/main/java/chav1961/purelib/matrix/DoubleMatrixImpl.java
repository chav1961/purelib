package chav1961.purelib.matrix;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.matrix.interfaces.DoubleMatrix;
import chav1961.purelib.matrix.interfaces.FloatMatrix;
import chav1961.purelib.matrix.interfaces.IntMatrix;
import chav1961.purelib.matrix.interfaces.LongMatrix;
import chav1961.purelib.matrix.interfaces.OldMatrix;

public class DoubleMatrixImpl implements DoubleMatrix {
	private final int		sizeX, sizeY;
	private final double[]	content;
	private double			epsilon = 1e-10;

	public DoubleMatrixImpl(final int sizeX, final int sizeY) {
		if (sizeX <= 0) {
			throw new IllegalArgumentException("X size ["+sizeX+"] must be greater than 0"); 
		}
		else if (sizeY <= 0) {
			throw new IllegalArgumentException("Y size ["+sizeY+"] must be greater than 0"); 
		}
		else {
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			this.content = new double[sizeX*sizeY];
		}
	}
	
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
	public DoubleMatrix add(final OldMatrix<?> another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null"); 
		}
		else if (!MatrixUtils.areDimensions2AddValid(this,another)) {
			throw new IllegalArgumentException("Matrix to add has dimensions ["+MatrixUtils.printDimensions(another)+"] uncompatible with current matrix ["+MatrixUtils.printDimensions(this)+"]"); 
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
			return new DoubleMatrixImpl(getSize(0), getSize(1), false, result);
		}
	}

	@Override
	public DoubleMatrix sub(final OldMatrix<?> another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null"); 
		}
		else if (!MatrixUtils.areDimensions2AddValid(this,another)) {
			throw new IllegalArgumentException("Matrix to subtract has dimensions ["+MatrixUtils.printDimensions(another)+"] uncompatible with current matrix ["+MatrixUtils.printDimensions(this)+"]"); 
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
			return new DoubleMatrixImpl(getSize(0), getSize(1), false, result);
		}
	}

	@Override
	public DoubleMatrix mul(final OldMatrix<?> another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null"); 
		}
		else if (!MatrixUtils.areDimensions2MulValid(this,another)) {
			throw new IllegalArgumentException("Matrix to multiply has dimensions ["+MatrixUtils.printDimensions(another)+"] uncompatible with current matrix ["+MatrixUtils.printDimensions(this)+"]"); 
		}
		else {
			final int		anotherSize = another.getSize(0) * another.getSize(1);  
			final int		currentY = getSize(0);
			final int		anotherX = another.getSize(1);
			final int		size = getSize(1);
			final double[]	sum = new double[currentY * anotherX];
					
			switch (CompilerUtils.defineClassType(another.getContentType())) {
				case CompilerUtils.CLASSTYPE_INT	:
					final int[]			anotherInt = new int[anotherSize];
					
					((IntMatrix)another).get(0, anotherInt, 0, anotherSize);
					for (int y = 0; y < currentY; y++) {
					    for (int x = 0; x < anotherX; x++) {
					    	double temp = 0;
					    	
							for (int index = 0; index < size; index++) {
							    temp += content[index + size * y] * anotherInt[x + anotherX * index];
							}
							sum[x + anotherX * y] = temp;
					    }
					}
					break;
				case CompilerUtils.CLASSTYPE_LONG	:	
					final long[]			anotherLong = new long[anotherSize];
					
					((LongMatrix)another).get(0, anotherLong, 0, anotherSize);
					for (int y = 0; y < currentY; y++) {
					    for (int x = 0; x < anotherX; x++) {
					    	double temp = 0;
					    	
							for (int index = 0; index < size; index++) {
							    temp += content[index + size * y] * anotherLong[x + anotherX * index];
							}
							sum[x + anotherX * y] = temp;
					    }
					}
					break;
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					final float[]			anotherFloat = new float[anotherSize];
					
					((FloatMatrix)another).get(0, anotherFloat, 0, anotherSize);
					for (int y = 0; y < currentY; y++) {
					    for (int x = 0; x < anotherX; x++) {
					    	double temp = 0;
					    	
							for (int index = 0; index < size; index++) {
							    temp += content[index + size * y] * anotherFloat[x + anotherX * index];
							}
							sum[x + anotherX * y] = temp;
					    }
					}
					break;
				case CompilerUtils.CLASSTYPE_DOUBLE	:
					final double[]			anotherDouble = new double[anotherSize];
					
					((DoubleMatrix)another).get(0, anotherDouble, 0, anotherSize);
					for (int y = 0; y < currentY; y++) {
					    for (int x = 0; x < anotherX; x++) {
					    	double temp = 0;
					    	
							for (int index = 0; index < size; index++) {
							    temp += content[index + size * y] * anotherDouble[x + anotherX * index];
							}
							sum[x + anotherX * y] = temp;
					    }
					}
					break;
				default : throw new IllegalArgumentException("Matrix to add has unsupported type ["+another.getContentType().getCanonicalName()+"]");
			}
			return new DoubleMatrixImpl(currentY, anotherX, false, sum);
		}
	}

	@Override
	public DoubleMatrix h_mul(OldMatrix<?> another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null"); 
		}
		else if (!MatrixUtils.areDimensions2AddValid(this,another)) {
			throw new IllegalArgumentException("Matrix to multiply has dimensions ["+MatrixUtils.printDimensions(another)+"] uncompatible with current matrix ["+MatrixUtils.printDimensions(this)+"]"); 
		}
		else {
			final int		anotherSize = another.getSize(0) * another.getSize(1);  
			final double[]	sum = content.clone();
					
			switch (CompilerUtils.defineClassType(another.getContentType())) {
				case CompilerUtils.CLASSTYPE_INT	:
					final int[]			anotherInt = new int[anotherSize];
					
					((IntMatrix)another).get(0, anotherInt, 0, anotherSize);
					for (int x = 0; x < sum.length; x++) {
						sum[x] *= anotherInt[x];
					}
					break;
				case CompilerUtils.CLASSTYPE_LONG	:	
					final long[]			anotherLong = new long[anotherSize];
					
					((LongMatrix)another).get(0, anotherLong, 0, anotherSize);
					for (int x = 0; x < sum.length; x++) {
						sum[x] *= anotherLong[x];
					}
					break;
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					final float[]			anotherFloat = new float[anotherSize];
					
					((FloatMatrix)another).get(0, anotherFloat, 0, anotherSize);
					for (int x = 0; x < sum.length; x++) {
						sum[x] *= anotherFloat[x];
					}
					break;
				case CompilerUtils.CLASSTYPE_DOUBLE	:
					final double[]			anotherDouble = new double[anotherSize];
					
					((DoubleMatrix)another).get(0, anotherDouble, 0, anotherSize);
					for (int x = 0; x < sum.length; x++) {
						sum[x] *= anotherDouble[x];
					}
					break;
				default : throw new IllegalArgumentException("Matrix to add has unsupported type ["+another.getContentType().getCanonicalName()+"]");
			}
			return new DoubleMatrixImpl(getSize(0), getSize(1), false, sum);
		}
	}
	
	@Override
	// see https://github.com/vkostyukov/la4j
	public DoubleMatrix inv() throws CalculationException {
		if (sizeX != sizeY) {
			throw new CalculationException("Matrix to invert is not a square matrix");
		}
		else {
			final double[]	result = content.clone();
			double 			var, diagonalTerm;

	        for (int k = 0; k < sizeX; k++) {
	            diagonalTerm = result[k + k * sizeX];

	            if (Math.abs(diagonalTerm) <= Double.MIN_VALUE) {
	                throw new CalculationException("This matrix cannot be inverted with a non-pivoting Gauss elimination method (contains zeroes on main diagonal).");
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
	public DoubleMatrix transp() {
		if (getSize(0) == 1 || getSize(1) == 1) {
			return new DoubleMatrixImpl(getSize(1), getSize(0), content);
		}
		else {
			final double[]	result = new double[content.length];
			final int		X = getSize(1), Y = getSize(0); 
			int				target = 0;
			
			for (int x = 0; x < X; x++) {
				for (int y = 0; y < Y; y++) {
					result[target++] = content[y * X + x];
				}
			}
			return new DoubleMatrixImpl(getSize(1), getSize(0), false, result);
		}
	}

	@Override
	public DoubleMatrix add(final Number number) {
		if (number == null) {
			throw new NullPointerException("Number to add can't be null"); 
		}
		else {
			final double val = number.doubleValue();
			
			return function((t)->t + val);
		}
	}

	@Override
	public DoubleMatrix mul(final Number number) {
		if (number == null) {
			throw new NullPointerException("Number to moltiply can't be null"); 
		}
		else {
			final double val = number.doubleValue();
			
			return function((t)->t * val);
		}
	}

	@Override
	public double getEpsilon() {
		return epsilon;
	}

	@Override
	public DoubleMatrix setEpsilon(final double epsilon) {
		this.epsilon = Math.abs(epsilon);
		return this;
	}

	@Override
	public DoubleMatrix function(final DoubleUnaryOperator op) {
		if (op == null) {
			throw new NullPointerException("Function operator can't be null"); 
		}
		else {
			final double[]	result = content.clone();
			
			for (int index = 0, maxIndex = result.length; index < maxIndex; index++) {
				result[index] = op.applyAsDouble(result[index]);
			}
			return new DoubleMatrixImpl(getSize(0), getSize(1), false, result);
		}
	}

	@Override
	public double get(final int... indices) {
		return content[MatrixUtils.indices2Displ(this, indices)];
	}

	@Override
	public void set(final double value, final int... indices) {
		content[MatrixUtils.indices2Displ(this, indices)] = value;
	}
	
	@Override
	public void get(final int from, final double[] content, final int to, final int length) {
		if (from < 0 || from >= this.content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(this.content.length-1)); 
		}
		else if (content == null) {
			throw new NullPointerException("Content to copy to can't be null"); 
		}
		else if (to < 0 || to >= content.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+(content.length-1)); 
		}
		else if (length < 0 || to + length > content.length) {
			throw new IllegalArgumentException("Length ["+to+"] is negative or (to+length) out of range 0.."+(content.length)); 
		}
		else if (from + length > this.content.length) {
			throw new IllegalArgumentException("Length ["+to+"]: (from+length) out of range 0.."+(this.content.length)); 
		}
		else {
			System.arraycopy(this.content, from, content, to, length);
		}
	}

	@Override
	public void set(final double[] content, final int from, final int to, final int length) {
		if (content == null) {
			throw new NullPointerException("Content to copy to can't be null"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length)); 
		}
		else if (to < 0 || to >= this.content.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+(this.content.length-1)); 
		}
		else if (length < 0 || to + length > this.content.length) {
			throw new IllegalArgumentException("Length ["+to+"] is negative or (to+length) out of range 0.."+(this.content.length)); 
		}
		else if (from + length > content.length) {
			throw new IllegalArgumentException("Length ["+to+"]: (from+length) out of range 0.."+(content.length)); 
		}
		else {
			System.arraycopy(content, from, this.content, to, length);
		}
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
