package chav1961.purelib.matrix;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.matrix.interfaces.DoubleMatrix;
import chav1961.purelib.matrix.interfaces.FloatMatrix;
import chav1961.purelib.matrix.interfaces.IntMatrix;
import chav1961.purelib.matrix.interfaces.LongMatrix;
import chav1961.purelib.matrix.interfaces.Matrix;

public class IntMatrixImpl implements IntMatrix {
	private final int		sizeX, sizeY;
	private final int[]		content;
	private double			epsilon = 1e-5;

	public IntMatrixImpl(final int sizeY, final int sizeX) {
		if (sizeX <= 0) {
			throw new IllegalArgumentException("X size ["+sizeX+"] must be greater than 0"); 
		}
		else if (sizeY <= 0) {
			throw new IllegalArgumentException("Y size ["+sizeY+"] must be greater than 0"); 
		}
		else {
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			this.content = new int[sizeX*sizeY];
		}
	}
	
	public IntMatrixImpl(final int sizeY, final int sizeX, final int... filled) {
		this(sizeY, sizeX, true, filled);
	}

	protected IntMatrixImpl(final int sizeY, final int sizeX, final boolean cloneContent, final int... filled) {
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
		return int.class;
	}

	@Override
	public int getDimensions() {
		return 2;
	}

	@Override
	public int getSize(final int dimension) {
		switch (dimension) {
			case 0	: return sizeY;
			case 1	: return sizeX;
			default : throw new IllegalArgumentException("Dimension number ["+dimension+"] can be 0 or 1 only");  
		}
	}

	@Override
	public IntMatrix add(final Matrix<?> another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null"); 
		}
		else if (!MatrixUtils.areDimensions2AddValid(this,another)) {
			throw new IllegalArgumentException("Matrix to add has dimensions ["+MatrixUtils.printDimensions(another)+"] uncompatible with current matrix ["+MatrixUtils.printDimensions(this)+"]"); 
		}
		else {
			final int[]	result = content.clone();
		
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
			return new IntMatrixImpl(getSize(0), getSize(1), false, result);
		}
	}

	@Override
	public IntMatrix sub(final Matrix<?> another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null"); 
		}
		else if (!MatrixUtils.areDimensions2AddValid(this,another)) {
			throw new IllegalArgumentException("Matrix to subtract has dimensions ["+MatrixUtils.printDimensions(another)+"] uncompatible with current matrix ["+MatrixUtils.printDimensions(this)+"]"); 
		}
		else {
			final int[]	result = content.clone();
			
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
			return new IntMatrixImpl(getSize(0), getSize(1), false, result);
		}
	}

	@Override
	public IntMatrix mul(final Matrix<?> another) {
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
			final int[]		sum = new int[currentY * anotherX];
					
			switch (CompilerUtils.defineClassType(another.getContentType())) {
				case CompilerUtils.CLASSTYPE_INT	:
					final int[]			anotherInt = new int[anotherSize];
					
					((IntMatrix)another).get(0, anotherInt, 0, anotherSize);
					for (int y = 0; y < currentY; y++) {
					    for (int x = 0; x < anotherX; x++) {
					    	int temp = 0;
					    	
							for (int index = 0; index < size; index++) {
							    temp += content[index + size * y] * anotherInt[x + anotherX * index];
							}
							sum[x + currentY * y] = temp;
					    }
					}
					break;
				case CompilerUtils.CLASSTYPE_LONG	:	
					final long[]			anotherLong = new long[anotherSize];
					
					((LongMatrix)another).get(0, anotherLong, 0, anotherSize);
					for (int y = 0; y < currentY; y++) {
					    for (int x = 0; x < anotherX; x++) {
					    	long temp = 0;
					    	
							for (int index = 0; index < size; index++) {
							    temp += content[index + size * y] * anotherLong[x + anotherX * index];
							}
							sum[x + currentY * y] = (int)temp;
					    }
					}
					break;
				case CompilerUtils.CLASSTYPE_FLOAT	:	
					final float[]			anotherFloat = new float[anotherSize];
					
					((FloatMatrix)another).get(0, anotherFloat, 0, anotherSize);
					for (int y = 0; y < currentY; y++) {
					    for (int x = 0; x < anotherX; x++) {
					    	float temp = 0;
					    	
							for (int index = 0; index < size; index++) {
							    temp += content[index + size * y] * anotherFloat[x + anotherX * index];
							}
							sum[x + currentY * y] = (int)temp;
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
							sum[x + currentY * y] = (int)temp;
					    }
					}
					break;
				default : throw new IllegalArgumentException("Matrix to add has unsupported type ["+another.getContentType().getCanonicalName()+"]");
			}
			return new IntMatrixImpl(currentY, anotherX, false, sum);
		}
	}

	@Override
	public IntMatrix h_mul(final Matrix<?> another) {
		if (another == null) {
			throw new NullPointerException("Matrix to add can't be null"); 
		}
		else if (!MatrixUtils.areDimensions2AddValid(this,another)) {
			throw new IllegalArgumentException("Matrix to Hadamard multiply has dimensions ["+MatrixUtils.printDimensions(another)+"] uncompatible with current matrix ["+MatrixUtils.printDimensions(this)+"]"); 
		}
		else {
			final int		anotherSize = another.getSize(0) * another.getSize(1);  
			final int[]		sum = content.clone();
					
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
			return new IntMatrixImpl(getSize(0), getSize(1), false, sum);
		}
	}
	
	@Override
	public IntMatrix inv() throws CalculationException {
		throw new CalculationException("Matrix inversion doen't support for int matrix");
	}

	@Override
	public IntMatrix transp() {
		if (getSize(0) == 1 || getSize(1) == 1) {
			return new IntMatrixImpl(getSize(1), getSize(0), content);
		}
		else {
			final int[]	result = new int[content.length];
			final int	X = getSize(1), Y = getSize(0); 
			int			target = 0;
			
			for (int x = 0; x < X; x++) {
				for (int y = 0; y < Y; y++) {
					result[target++] = content[y * X + x];
				}
			}
			return new IntMatrixImpl(getSize(1), getSize(0), false, result);
		}
	}

	@Override
	public IntMatrix add(final Number number) {
		if (number == null) {
			throw new NullPointerException("Number to add can't be null"); 
		}
		else {
			final int val = number.intValue();
			
			return function((t)->t + val);
		}
	}

	@Override
	public IntMatrix mul(final Number number) {
		if (number == null) {
			throw new NullPointerException("Number to moltiply can't be null"); 
		}
		else {
			final int val = number.intValue();
			
			return function((t)->t * val);
		}
	}

	@Override
	public double getEpsilon() {
		return epsilon;
	}

	@Override
	public IntMatrix setEpsilon(final double epsilon) {
		this.epsilon = Math.abs(epsilon);
		return this;
	}

	@Override
	public IntMatrix function(final IntUnaryOperator op) {
		if (op == null) {
			throw new NullPointerException("Function operator can't be null"); 
		}
		else {
			final int[]	result = content.clone();
			
			for (int index = 0, maxIndex = result.length; index < maxIndex; index++) {
				result[index] = op.applyAsInt(result[index]);
			}
			return new IntMatrixImpl(getSize(0), getSize(1), false, result);
		}
	}
	
	@Override
	public void get(final int from, final int[] content, final int to, final int length) {
		if (from < 0 || from >= this.content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(this.content.length)); 
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
	public void set(final int[] content, final int from, final int to, final int length) {
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
		IntMatrixImpl other = (IntMatrixImpl) obj;
		if (!Arrays.equals(content, other.content)) return false;
		if (sizeX != other.sizeX) return false;
		if (sizeY != other.sizeY) return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder	sb = new StringBuilder("IntMatrixImpl [").append(MatrixUtils.printDimensions(this)).append("] :\n");

		for (int y = 0; y < sizeY; y++) {
			sb.append("|");
			for (int x = 0; x < sizeX; x++) {
				sb.append(content[y * sizeX + x]).append(' ');
			}
			sb.append("|\n");
		}
		return sb.toString();
	}
}
