package chav1961.purelib.matrix.internal;

import java.io.DataInput;
import java.io.IOException;
import java.util.Arrays;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.ApplyBit;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;

public class BitMatrix implements Matrix {
	private final int		rows;
	private final int		cols;
	private final long[]	content;
	private boolean			completed = true;

	public BitMatrix(final int rows, final int columns) {
		if (rows <= 0) {
			throw new IllegalArgumentException("Rows ["+rows+"] must be greater than 0");
		}
		else if (columns <= 0) {
			throw new IllegalArgumentException("Columns ["+columns+"] must be greater than 0");
		}
		else {
			this.rows = rows;
			this.cols = columns;
			this.content = new long[(rows * columns + 63)/64];
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		final BitMatrix	result = new BitMatrix(rows, cols);
		
		System.arraycopy(this.content, 0, result.content, 0, result.content.length);
		return result;
	}
	
	@Override
	public void close() throws RuntimeException {
	}

	@Override
	public Type getType() {
		return Type.BIT;
	}

	@Override
	public int numberOfRows() {
		return rows;
	}

	@Override
	public int numberOfColumns() {
		return cols;
	}

	@Override
	public boolean deepEquals(final Matrix another) {
		if (another == this) {
			return true;
		}
		else if (another == null) {
			return false;
		}
		else if (another.getType() != this.getType() || this.numberOfRows() != another.numberOfRows() || this.numberOfColumns() != another.numberOfColumns()) {
			return false;
		}
		else {
			ensureCompleted();
			return Arrays.equals(content, fromIntArray(another.extractInts()));
		}
	}

	@Override
	public int[] extractInts(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			ensureInside(piece);
			final long[]	source = this.content;
			final int[]		result = new int[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					final int 	index = (y0 + y)*numberOfColumns() + (x0 + x);
					final long 	value = source[index >> 6]; 
					
					result[where++] = (value & (1 << (index & 0x3F))) != 0 ? 1 : 0;
				}
			}
			return result;
		}
	}

	@Override
	public long[] extractLongs(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			ensureInside(piece);
			final long[]	source = this.content;
			final long[]	result = new long[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					final int 	index = (y0 + y)*numberOfColumns() + (x0 + x);
					final long 	value = source[index >> 6]; 
					
					result[where++] = (value & (1 << (index & 0x3F))) != 0 ? 1L : 0L;
				}
			}
			return result;
		}
	}

	@Override
	public float[] extractFloats(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			ensureInside(piece);
			final long[]	source = this.content;
			final float[]	result = new float[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					final int 	index = (y0 + y)*numberOfColumns() + (x0 + x);
					final long 	value = source[index >> 6]; 
					
					result[where++] = (value & (1 << (index & 0x3F))) != 0 ? 1.0f : 0.0f;
				}
			}
			return result;
		}
	}

	@Override
	public double[] extractDoubles(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			ensureInside(piece);
			final long[]	source = this.content;
			final double[]	result = new double[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					final int 	index = (y0 + y)*numberOfColumns() + (x0 + x);
					final long 	value = source[index >> 6]; 
					
					result[where++] = (value & (1 << (index & 0x3F))) != 0 ? 1.0d : 0.0d;
				}
			}
			return result;
		}
	}

	@Override
	public Matrix assign(final Piece piece, final int... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			ensureInside(piece);
			final long[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
loop:		for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					if (where >= content.length) {
						break loop;
					}
					else {
						final int	index = (y0 + y)*numberOfColumns() + (x0 + x);
						
						if (content[where++] != 0) {
							result[index >> 6] |= ~(1L << (index & 0x3F));
						}
						else {
							result[index >> 6] &= (1L << (index & 0x3F));
						}
					}
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final Piece piece, final long... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			ensureInside(piece);
			final long[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
loop:		for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					if (where >= content.length) {
						break loop;
					}
					else {
						final int	index = (y0 + y)*numberOfColumns() + (x0 + x);
						
						if (content[where++] != 0) {
							result[index >> 6] |= ~(1L << (index & 0x3F));
						}
						else {
							result[index >> 6] &= (1L << (index & 0x3F));
						}
					}
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final Piece piece, final float... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			ensureInside(piece);
			final long[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
loop:		for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					if (where >= content.length) {
						break loop;
					}
					else {
						final int	index = (y0 + y)*numberOfColumns() + (x0 + x);
						
						if (content[where++] != 0) {
							result[index >> 6] |= ~(1L << (index & 0x3F));
						}
						else {
							result[index >> 6] &= (1L << (index & 0x3F));
						}
					}
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final Piece piece, final double... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			ensureInside(piece);
			final long[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
loop:		for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					if (where >= content.length) {
						break loop;
					}
					else {
						final int	index = (y0 + y)*numberOfColumns() + (x0 + x);
						
						if (content[where++] != 0) {
							result[index >> 6] |= ~(1L << (index & 0x3F));
						}
						else {
							result[index >> 6] &= (1L << (index & 0x3F));
						}
					}
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else if (content.getType() == this.getType()) {
			if (content instanceof BitMatrix) {
				return assign(content.extractInts());
			}
			else {
				return assign(content.extractInts());
			}
		}
		else {
			return assign(getTotalPiece(), content);
		}
	}

	@Override
	public Matrix assign(final Piece piece, final Matrix content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			return assign(piece, content.extractInts());
		}
	}

	@Override
	public Matrix assign(Piece piece, DataInput content, Type type) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}	
	
	@Override
	public Matrix fill(final int value) {
		return fill((long)value);
	}

	@Override
	public Matrix fill(final Piece piece, final int value) {
		return fill(piece, (long)value);
	}

	@Override
	public Matrix fill(final Piece piece, final long value) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			ensureInside(piece);
			final long[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					final int	index = (y0 + y)*numberOfColumns() + (x0 + x);

					if (value != 0) {
						result[index >> 6] |= (1L << (index & 0x3F));
					}
					else {
						result[index >> 6] &= ~(1L << (index & 0x3F));
					}
				}
			}
			return this;
		}
	}

	@Override
	public Matrix fill(final float value) {
		return fill((long)value);
	}

	@Override
	public Matrix fill(final Piece piece, final float value) {
		return fill(piece, (long)value);
	}

	@Override
	public Matrix fill(final float real, final float image) {
		throw new UnsupportedOperationException("Complex assignment is not supported for real matrix");
	}

	@Override
	public Matrix fill(final Piece piece, final float real, final float image) {
		throw new UnsupportedOperationException("Complex assignment is not supported for real matrix");
	}

	@Override
	public Matrix fill(final double value) {
		return fill((long)value);
	}

	@Override
	public Matrix fill(final Piece piece, final double value) {
		return fill(piece, (long)value);
	}

	@Override
	public Matrix fill(final double real, final double image) {
		throw new UnsupportedOperationException("Complex assignment is not supported for real matrix");
	}

	@Override
	public Matrix fill(Piece piece, double real, double image) {
		throw new UnsupportedOperationException("Complex assignment is not supported for real matrix");
	}

	@Override
	public Matrix cast(final Type type) {
		if (type == null) {
			throw new NullPointerException("Cast type can't be null");
		}
		else {
			switch (type) {
				case COMPLEX_DOUBLE	:
					break;
				case COMPLEX_FLOAT	:
					final FloatComplexMatrix	fcm = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
					final long[]				sourceCF = this.content;
					final float[]				targetCF = fcm.extractFloats();
					
					for(int index = 0, maxIndex = targetCF.length; index < maxIndex; index++) {
						targetCF[2 * index] = (int)sourceCF[index];
						targetCF[2 * index + 1] = 0;
					}
					return fcm;
				case REAL_DOUBLE	:
					final DoubleRealMatrix	drm = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
					final long[]			sourceD = this.content;
					final double[]			targetD = drm.extractDoubles();
					
					for(int index = 0, maxIndex = targetD.length; index < maxIndex; index++) {
						targetD[index] = (float)sourceD[index];
					}
					return drm;
				case REAL_FLOAT		:
					final FloatRealMatrix	frm = new FloatRealMatrix(numberOfRows(), numberOfColumns());
					final long[]			sourceF = this.content;
					final float[]			targetF = frm.extractFloats();
					
					for(int index = 0, maxIndex = targetF.length; index < maxIndex; index++) {
						targetF[index] = (float)sourceF[index];
					}
					return frm;
				case REAL_INT		:
					return this;
				case REAL_LONG		:
					final LongRealMatrix	lrm = new LongRealMatrix(numberOfRows(), numberOfColumns());
					final long[]			sourceL = this.content;
					final long[]			targetL = lrm.extractLongs();
					
					for(int index = 0, maxIndex = targetL.length; index < maxIndex; index++) {
						targetL[index] = (long)sourceL[index];
					}
					return lrm;
				default:
					throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
			}
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public Matrix add(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			return or(this.content, fromIntArray(content));
		}
	}

	@Override
	public Matrix add(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			return or(this.content, fromLongArray(content));
		}
	}

	@Override
	public Matrix add(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			return or(this.content, fromFloatArray(content));
		}
	}

	@Override
	public Matrix add(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			return or(this.content, fromDoubleArray(content));
		}
	}

	@Override
	public Matrix add(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to add real and complex matrices");
				case REAL_DOUBLE	:
					return add(content.extractDoubles());
				case REAL_FLOAT		:
					return add(content.extractFloats());
				case REAL_INT		:
					return add(content.extractInts());
				case REAL_LONG		:
					return add(content.extractLongs());
				case BIT			:
					if (content instanceof BitMatrix) {
						return or(this.content, ((BitMatrix)content).content);
					}
					else {
						return add(content.extractInts());
					}
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix addValue(final int value) {
		return addValue((long)value);
	}

	@Override
	public Matrix addValue(final long value) {
		final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
		final long[]	target = result.content;

		if (value != 0) {
			for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
				target[index >> 6] |= (1L << (index & 0x3F));
			}
		}
		else {
			for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
				target[index >> 6] &= ~(1L << (index & 0x3F));
			}
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix addValue(final float value) {
		return addValue((long)value);
	}

	@Override
	public Matrix addValue(float real, float image) {
		throw new UnsupportedOperationException("Complex addition is not supported for real matrix");
	}

	@Override
	public Matrix addValue(final double value) {
		return addValue((long)value);
	}

	@Override
	public Matrix addValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex addition is not supported for real matrix");
	}

	@Override
	public Matrix subtract(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			return minus(this.content, fromIntArray(content));
		}
	}

	@Override
	public Matrix subtract(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			return minus(this.content, fromLongArray(content));
		}
	}

	@Override
	public Matrix subtract(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			return minus(this.content, fromFloatArray(content));
		}
	}

	@Override
	public Matrix subtract(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			return minus(this.content, fromDoubleArray(content));
		}
	}

	@Override
	public Matrix subtract(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to subtract real and complex matrices");
				case REAL_DOUBLE	:
					return subtract(content.extractDoubles());
				case REAL_FLOAT		:
					return subtract(content.extractFloats());
				case REAL_INT		:
					return subtract(content.extractInts());
				case REAL_LONG		:
					return subtract(content.extractLongs());
				case BIT			:
					if (content instanceof BitMatrix) {
						return minus(this.content, ((BitMatrix)content).content);
					}
					else {
						return subtract(content.extractInts());
					}
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix subtractValue(final int value) {
		return subtractValue((long)value);
	}

	@Override
	public Matrix subtractValue(final long value) {
		final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
		final long[]		target = result.content;
		
		if (value != 0) {
			for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
				target[index >> 6] &= ~(1L << (index & 0x3F));
			}
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix subtractValue(final float value) {
		return subtractValue((long)value);
	}

	@Override
	public Matrix subtractValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex subtraction is not supported for real matrix");
	}

	@Override
	public Matrix subtractValue(final double value) {
		return subtractValue((long)value);
	}

	@Override
	public Matrix subtractValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex subtraction is not supported for real matrix");
	}

	@Override
	public Matrix subtractFrom(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			return minus(fromIntArray(content), this.content);
		}
	}

	@Override
	public Matrix subtractFrom(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			return minus(fromLongArray(content), this.content);
		}
	}

	@Override
	public Matrix subtractFrom(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			return minus(fromFloatArray(content), this.content);
		}
	}

	@Override
	public Matrix subtractFrom(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			return minus(fromDoubleArray(content), this.content);
		}
	}

	@Override
	public Matrix subtractFrom(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to subtract real and complex matrices");
				case REAL_DOUBLE	:
					return subtractFrom(content.extractDoubles());
				case REAL_FLOAT		:
					return subtractFrom(content.extractFloats());
				case REAL_INT		:
					return subtractFrom(content.extractInts());
				case REAL_LONG		:
					return subtractFrom(content.extractLongs());
				case BIT			:
					if (content instanceof BitMatrix) {
						return minus(((BitMatrix)content).content, this.content);
					}
					else {
						return subtractFrom(content.extractInts());
					}
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix subtractFromValue(final int value) {
		final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
		final long[]		source = this.content;
		final long[]		target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = value - source[index]; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix subtractFromValue(final long value) {
		return subtractFromValue((int)value);
	}

	@Override
	public Matrix subtractFromValue(final float value) {
		return subtractFromValue((int)value);
	}

	@Override
	public Matrix subtractFromValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex subtraction is not supported for real matrix");
	}

	@Override
	public Matrix subtractFromValue(final double value) {
		return subtractFromValue((int)value);
	}

	@Override
	public Matrix subtractFromValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex subtraction is not supported for real matrix");
	}

	@Override
	public Matrix mul(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else if (content.numberOfRows() != this.numberOfColumns()) {
			throw new IllegalArgumentException("Content number of rows ["+content.numberOfRows()+"] differ from current number of columns ["+this.numberOfColumns()+"]");
		}
		else {
			final BitMatrix	result = new BitMatrix(this.numberOfRows(), content.numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			final int		maxY = this.numberOfRows(), maxX = content.numberOfColumns();
			final int		colSize = this.numberOfColumns(), maxK = content.numberOfRows(); 
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					final double[]	tempD = content.extractDoubles();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							int	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += source[y * colSize + k] * tempD[k * maxX + x];
							}
							target[y * maxX + x] = sum;
						}
					}
					break;
				case REAL_FLOAT		:
					final float[]	tempF = content.extractFloats();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							int	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += source[y * colSize + k] * tempF[k * maxX + x];
							}
							target[y * maxX + x] = sum;
						}
					}
					break;
				case REAL_INT		:
					final int[]		tempI = content.extractInts();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							int	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += source[y * colSize + k] * tempI[k * maxX + x];
							}
							target[y * maxX + x] = sum;
						}
					}
					break;
				case REAL_LONG		:
					final long[]	tempL = content.extractLongs();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							int	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += source[y * colSize + k] * tempL[k * maxX + x];
							}
							target[y * maxX + x] = sum;
						}
					}
					break;
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulFrom(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else if (this.numberOfRows() != content.numberOfColumns()) {
			throw new IllegalArgumentException("Content number of columns ["+content.numberOfColumns()+"] differ from current number of rows ["+this.numberOfRows()+"]");
		}
		else {
			final BitMatrix	result = new BitMatrix(content.numberOfRows(), this.numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			final int		maxY = content.numberOfRows(), maxX = this.numberOfColumns();
			final int		colSize = content.numberOfColumns(), maxK = this.numberOfRows(); 
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					final double[]	tempD = content.extractDoubles();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							int	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += tempD[y * colSize + k] * source[k * maxX + x];
							}
							target[y * maxX + x] = sum;
						}
					}
					break;
				case REAL_FLOAT		:
					final float[]	tempF = content.extractFloats();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							int	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += tempF[y * colSize + k] * source[k * maxX + x];
							}
							target[y * maxX + x] = sum;
						}
					}
					break;
				case REAL_INT		:
					final int[]		tempI = content.extractInts();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							int	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += tempI[y * colSize + k] * source[k * maxX + x];
							}
							target[y * maxX + x] = sum;
						}
					}
					break;
				case REAL_LONG		:
					final long[]	tempL = content.extractLongs();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							int	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += tempL[y * colSize + k] * source[k * maxX + x];
							}
							target[y * maxX + x] = sum;
						}
					}
					break;
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulValue(final int value) {
		final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
		final long[]	source = this.content;
		final long[]	target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] * value; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix mulValue(final long value) {
		return mulValue((int)value);
	}
 
	@Override
	public Matrix mulValue(final float value) {
		return mulValue((int)value);
	}

	@Override
	public Matrix mulValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex multiplication is not supported for real matrix");
	}

	@Override
	public Matrix mulValue(final double value) {
		return mulValue((int)value);
	}

	@Override
	public Matrix mulValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex multiplication is not supported for real matrix");
	}

	@Override
	public Matrix divValue(final int value) {
		final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
		final long[]	source = this.content;
		final long[]	target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] / value; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix divValue(final long value) {
		return divValue((int)value);
	}

	@Override
	public Matrix divValue(final float value) {
		return divValue((int)value);
	}

	@Override
	public Matrix divValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex division is not supported for real matrix");
	}

	@Override
	public Matrix divValue(final double value) {
		return divValue((int)value);
	}

	@Override
	public Matrix divValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex division is not supported for real matrix");
	}

	@Override
	public Matrix divFromValue(final int value) {
		final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
		final long[]	source = this.content;
		final long[]	target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = value / source[index]; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix divFromValue(final long value) {
		return divFromValue((int)value);
	}

	@Override
	public Matrix divFromValue(final float value) {
		return divFromValue((int)value);
	}

	@Override
	public Matrix divFromValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex division is not supported for real matrix");
	}

	@Override
	public Matrix divFromValue(final double value) {
		return divFromValue((int)value);
	}

	@Override
	public Matrix divFromValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex division is not supported for real matrix");
	}

	@Override
	public Matrix mulHadamard(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			return and(this.content, fromIntArray(content));
		}
	}

	@Override
	public Matrix mulHadamard(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			return and(this.content, fromLongArray(content));
		}
	}

	@Override
	public Matrix mulHadamard(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			return and(this.content, fromFloatArray(content));
		}
	}

	@Override
	public Matrix mulHadamard(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			return and(this.content, fromDoubleArray(content));
		}
	}

	@Override
	public Matrix mulHadamard(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					return mulHadamard(content.extractDoubles());
				case REAL_FLOAT		:
					return mulHadamard(content.extractFloats());
				case REAL_INT		:
					return mulHadamard(content.extractInts());
				case REAL_LONG		:
					return mulHadamard(content.extractLongs());
				case BIT			:
					if (content instanceof BitMatrix) {
						return and(this.content, ((BitMatrix)content).content);
					}
					else {
						return mulHadamard(content.extractInts());
					}
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix mulInvHadamard(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] / content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvHadamard(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (source[index] / content[index]); 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvHadamard(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (source[index] / content[index]); 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvHadamard(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (source[index] / content[index]); 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvHadamard(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					return mulInvHadamard(content.extractDoubles());
				case REAL_FLOAT		:
					return mulInvHadamard(content.extractFloats());
				case REAL_INT		:
					return mulInvHadamard(content.extractInts());
				case REAL_LONG		:
					return mulInvHadamard(content.extractLongs());
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] / source[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (content[index] / source[index]); 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (content[index] / source[index]); 
			}
			result.completed = false;
			return result;
		}
	}
 
	@Override
	public Matrix mulInvFromHadamard(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (content[index] / source[index]); 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					return mulInvFromHadamard(content.extractDoubles());
				case REAL_FLOAT		:
					return mulInvFromHadamard(content.extractFloats());
				case REAL_INT		:
					return mulInvFromHadamard(content.extractInts());
				case REAL_LONG		:
					return mulInvFromHadamard(content.extractLongs());
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix tensorMul(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Matrix content can't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(this.numberOfRows() * content.numberOfRows(), this.numberOfColumns() * content.numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			final int 		maxY1 = this.numberOfRows(), maxY2 = content.numberOfRows();
			final int 		maxX1 = this.numberOfColumns(), maxX2 = content.numberOfColumns();
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new UnsupportedOperationException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					final double[]	tempD = content.extractDoubles();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final long	k = source[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = (int) (k * tempD[sourceIndex]);
									}
								}
							}
						}
					}
					break;
				case REAL_FLOAT		:
					final float[]	tempF = content.extractFloats();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final long	k = source[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = (int) (k * tempF[sourceIndex]);
									}
								}
							}
						}
					}
					break;
				case REAL_INT		:
					final int[]		tempI = content.extractInts();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final long	k = source[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = k * tempI[sourceIndex];
									}
								}
							}
						}
					}
					break;
				case REAL_LONG		:
					final long[]	tempL = content.extractLongs();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final long	k = source[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = (int) (k * tempL[sourceIndex]);
									}
								}
							}
						}
					}
					break;
				default:
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix tensorMulFrom(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Matrix content can't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(this.numberOfRows() * content.numberOfRows(), this.numberOfColumns() * content.numberOfColumns());
			final long[]	source = this.content;
			final long[]	target = result.content;
			final int 		maxY1 = content.numberOfRows(), maxY2 = this.numberOfRows();
			final int 		maxX1 = content.numberOfColumns(), maxX2 = this.numberOfColumns();
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new UnsupportedOperationException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					final double[]	tempD = content.extractDoubles();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final int	k = (int) tempD[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = (int) (k * source[sourceIndex]);
									}
								}
							}
						}
					}
					break;
				case REAL_FLOAT		:
					final float[]	tempF = content.extractFloats();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final int	k = (int) tempF[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = k * source[sourceIndex];
									}
								}
							}
						}
					}
					break;
				case REAL_INT		:
					final int[]		tempI = content.extractInts();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final int	k = tempI[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = k * source[sourceIndex];
									}
								}
							}
						}
					}
					break;
				case REAL_LONG		:
					final long[]	tempL = content.extractLongs();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final int	k = (int) tempL[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = k * source[sourceIndex];
									}
								}
							}
						}
					}
					break;
				default:
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
			result.completed = false;
			return result;
		}
	}
	
	@Override
	public Matrix invert() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Only square matrix can be inverted");
		}
		else {
			throw new UnsupportedOperationException("Inversion of int matrix is not supported. Cast this matrix content to float");
		}
	}

	@Override
	public Matrix transpose() {
		ensureCompleted();
		final BitMatrix	result = new BitMatrix(numberOfColumns(), numberOfRows());
		final long[]	source = this.content;
		final long[]	target = result.content;
		final int		rows = numberOfRows(), cols = numberOfColumns();  
		
		for(int y = 0; y < rows; y++) {
			for(int x = 0; x < cols; x++) {
				target[x*rows + y] = source[y*cols + x]; 
			}
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix aggregate(final AggregateDirection dir, final AggregateType aggType) {
		if (dir == null) {
			throw new NullPointerException("Aggregate direction can't be null");
		}
		else if (aggType == null) {
			throw new NullPointerException("Aggregate type can't be null");
		}
		else {
			ensureCompleted();
			switch (aggType) {
				case Avg	:
					return aggregateAvg(dir); 
				case Max	:
					return aggregateMax(dir); 
				case Min	:
					return aggregateMin(dir); 
				case Sum	:
					return aggregateSum(dir); 
				default:
					throw new UnsupportedOperationException("Aggregation type ["+aggType+"] is not supported yet");
			}
		}
	}


	@Override
	public Number det() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Only square matrix can be inverted");
		}
		else {
			throw new UnsupportedOperationException("Determinant of int matrix is not supported. Cast this matrix content to float");
		}
	}

	@Override
	public Number track() {
		ensureCompleted();
		final long[]	source = this.content;
		final int		colSize = numberOfColumns();
		int		sum = 0;
		
		ensureCompleted();
		for(int index = 0; index < colSize; index++) {	// Calculate diagonal sum
			sum += source[index * (colSize + 1)];
		}
		return sum;
	}

	@Override
	public Number[] det2() {
		return new Number[] {det(), 0};
	}

	@Override
	public Number[] track2() {
		return new Number[] {track(), 0};
	}
	
	
	@Override
	public String toHumanReadableString() {
		final StringBuilder	sb = new StringBuilder();
		
		sb.append("Matrix: type=").append(getType()).append(", size=").append(numberOfRows()).append('x').append(numberOfColumns()).append(":\n");
		for(int y = 0; y < numberOfRows(); y++) {
			for(int x = 0; x < numberOfColumns(); x++) {
				sb.append(String.format(" %1$15e",content[y*numberOfColumns()+x]));
			}
		}
		return sb.toString();
	}

	@Override
	public Matrix done() {
		completed = true;
		return this;
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyBit callback) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Ccan't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns()); 
			final long[]	source = this.content;
			final long[]	target = result.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					target[(y0 + y)*numberOfColumns() + (x0 + x)] = callback.apply(y0 + y, x0 + x, source[(y0 + y)*numberOfColumns() + (x0 + x)] == 0 ? false : true) ? 1 : 0;
				}
			}
			result.completed = false;
			return result;
		}
	}
	
	@Override
	public Matrix apply(final Piece piece, final ApplyInt callback) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Ccan't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns()); 
			final long[]	source = this.content;
			final long[]	target = result.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					target[(y0 + y)*numberOfColumns() + (x0 + x)] = callback.apply(y0 + y, x0 + x, (int)source[(y0 + y)*numberOfColumns() + (x0 + x)]);
				}
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyLong callback) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Ccan't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns()); 
			final long[]	source = this.content;
			final long[]	target = result.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					target[(y0 + y)*numberOfColumns() + (x0 + x)] = (int) callback.apply(y0 + y, x0 + x, source[(y0 + y)*numberOfColumns() + (x0 + x)]);
				}
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyFloat callback) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Ccan't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns()); 
			final long[]	source = this.content;
			final long[]	target = result.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					target[(y0 + y)*numberOfColumns() + (x0 + x)] = (int)callback.apply(y0 + y, x0 + x, source[(y0 + y)*numberOfColumns() + (x0 + x)]);
				}
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyDouble callback) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Ccan't be null");
		}
		else {
			final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns()); 
			final long[]	source = this.content;
			final long[]	target = result.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					target[(y0 + y)*numberOfColumns() + (x0 + x)] = (int)callback.apply(y0 + y, x0 + x, source[(y0 + y)*numberOfColumns() + (x0 + x)]);
				}
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyFloat2 callback) {
		throw new UnsupportedOperationException("Complex apply is not supported for real matrix");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyDouble2 callback) {
		throw new UnsupportedOperationException("Complex apply is not supported for real matrix");
	}
	
	private Piece getTotalPiece() {
		return Piece.of(0, 0, numberOfRows(), numberOfColumns());
	}
	
	private void ensureCompleted() {
		if (!completed) {
			throw new IllegalStateException("Matrix is not completed after previous operations. Call done() method before");
		}
	}

	private void ensureInside(final Piece piece) {
		if (piece.getLeft() >= numberOfColumns()) {
			throw new IllegalArgumentException("Left piece location ["+piece.getLeft()+"] outside number of columns ["+numberOfColumns()+"]");
		}
		else if (piece.getTop() >= numberOfRows()) {
			throw new IllegalArgumentException("Top piece location ["+piece.getTop()+"] outside number of rows ["+numberOfRows()+"]");
		}
		else if (piece.getLeft() + piece.getWidth() > numberOfColumns()) {
			throw new IllegalArgumentException("Right piece location ["+(piece.getLeft()+piece.getWidth())+"] outside number of columns ["+numberOfColumns()+"]");
		}
		else if (piece.getTop() + piece.getHeight() > numberOfRows()) {
			throw new IllegalArgumentException("Bottom piece location ["+(piece.getTop()+piece.getHeight())+"] outside number of rows ["+numberOfRows()+"]");
		}
	}
	
	private Matrix aggregateAvg(final AggregateDirection dir) {
		final BitMatrix	result;
		final long[]	source = this.content;
		final long[]	target;
		int	val;
		
		switch (dir) {
			case ByColumns	:
				result = new BitMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					val = 0;
					for(int x = 0; x < numberOfColumns(); x++) {
						val += source[y * numberOfColumns() + x];
					}
					target[y] = val / numberOfColumns();
				}
				break;
			case ByRows		:
				result = new BitMatrix(1, numberOfColumns()); 
				target = result.content;
				
				for(int x = 0; x < numberOfColumns(); x++) {
					val = 0;
					for(int y = 0; y < numberOfRows(); y++) {
						val += source[y * numberOfColumns() + x];
					}
					target[x] = val / numberOfRows();
				}
				break;
			case Total		:
				result = new BitMatrix(1, 1); 
				target = result.content;
				
				val = 0;
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < numberOfColumns(); x++) {
						val += source[y * numberOfColumns() + x];
					}
				}
				target[0] = val / (numberOfRows() * numberOfColumns());
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.completed = false;
		return result;
	}
	
	private Matrix aggregateMax(final AggregateDirection dir) {
		final BitMatrix	result;
		final long[]	source = this.content;
		final long[]	target;
		long	val;
		
		switch (dir) {
			case ByColumns	:
				result = new BitMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					val = source[y * numberOfColumns() + 0];
					for(int x = 0; x < numberOfColumns(); x++) {
						if (source[y * numberOfColumns() + x] > val) {
							val = source[y * numberOfColumns() + x]; 
						}
					}
					target[y] = val;
				}
				break;
			case ByRows		:
				result = new BitMatrix(1, numberOfColumns()); 
				target = result.content;
				
				for(int x = 0; x < numberOfColumns(); x++) {
					val = source[x + 0];
					for(int y = 0; y < numberOfRows(); y++) {
						if (source[y * numberOfColumns() + x] > val) {
							val = source[y * numberOfColumns() + x]; 
						}
					}
					target[x] = val;
				}
				break;
			case Total		:
				result = new BitMatrix(1, 1); 
				target = result.content;
				
				val = source[0];
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < numberOfColumns(); x++) {
						if (source[y * numberOfColumns() + x] > val) {
							val = source[y * numberOfColumns() + x]; 
						}
					}
				}
				target[0] = val;
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.completed = false;
		return result;
	}

	private Matrix aggregateMin(final AggregateDirection dir) {
		final BitMatrix	result;
		final long[]	source = this.content;
		final long[]	target;
		long	val;
		
		switch (dir) {
			case ByColumns	:
				result = new BitMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					val = source[y * numberOfColumns() + 0];
					for(int x = 0; x < numberOfColumns(); x++) {
						if (source[y * numberOfColumns() + x] < val) {
							val = source[y * numberOfColumns() + x]; 
						}
					}
					target[y] = val;
				}
				break;
			case ByRows		:
				result = new BitMatrix(1, numberOfColumns()); 
				target = result.content;
				
				for(int x = 0; x < numberOfColumns(); x++) {
					val = source[x + 0];
					for(int y = 0; y < numberOfRows(); y++) {
						if (source[y * numberOfColumns() + x] < val) {
							val = source[y * numberOfColumns() + x]; 
						}
					}
					target[x] = val;
				}
				break;
			case Total		:
				result = new BitMatrix(1, 1); 
				target = result.content;
				
				val = source[0];
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < numberOfColumns(); x++) {
						if (source[y * numberOfColumns() + x] < val) {
							val = source[y * numberOfColumns() + x]; 
						}
					}
				}
				target[0] = val;
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.completed = false;
		return result;
	}

	private Matrix aggregateSum(final AggregateDirection dir) {
		final BitMatrix	result;
		final long[]	source = this.content;
		final long[]	target;
		long	val;
		
		switch (dir) {
			case ByColumns	:
				result = new BitMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					val = 0;
					for(int x = 0; x < numberOfColumns(); x++) {
						val += source[y * numberOfColumns() + x];
					}
					target[y] = val;
				}
				break;
			case ByRows		:
				result = new BitMatrix(1, numberOfColumns()); 
				target = result.content;
				
				for(int x = 0; x < numberOfColumns(); x++) {
					val = 0;
					for(int y = 0; y < numberOfRows(); y++) {
						val += source[y * numberOfColumns() + x];
					}
					target[x] = val;
				}
				break;
			case Total		:
				result = new BitMatrix(1, 1); 
				target = result.content;
				
				val = 0;
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < numberOfColumns(); x++) {
						val += source[y * numberOfColumns() + x];
					}
				}
				target[0] = val;
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.completed = false;
		return result;
	}
	
	private static long[] fromIntArray(final int... content) {
		final long[]	result = new long[(content.length + 63)/64];
		
		for(int index = 0; index < content.length; index++) {
			if (content[index] != 0) {
				result[index >> 6] |= (1L << (index & 0x3F));  
			}
		}
		return result;
	}

	private static long[] fromLongArray(final long... content) {
		final long[]	result = new long[(content.length + 63)/64];
		
		for(int index = 0; index < content.length; index++) {
			if (content[index] != 0) {
				result[index >> 6] |= (1L << (index & 0x3F));  
			}
		}
		return result;
	}

	private static long[] fromFloatArray(final float... content) {
		final long[]	result = new long[(content.length + 63)/64];
		
		for(int index = 0; index < content.length; index++) {
			if (content[index] != 0) {
				result[index >> 6] |= (1L << (index & 0x3F));  
			}
		}
		return result;
	}

	private static long[] fromDoubleArray(final double... content) {
		final long[]	result = new long[(content.length + 63)/64];
		
		for(int index = 0; index < content.length; index++) {
			if (content[index] != 0) {
				result[index >> 6] |= (1L << (index & 0x3F));  
			}
		}
		return result;
	}

	private Matrix or(final long[] left, final long[] right) {
		final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
		final long[]	target = result.content;
		
		for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
			target[index] = left[index] | right[index];
		}
		result.completed = false;
		return result;
	}	

	private Matrix minus(final long[] left, final long[] right) {
		final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
		final long[]	target = result.content;
		
		for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
			target[index] = left[index] ^ (left[index] & right[index]);
		}
		result.completed = false;
		return result;
	}	

	private Matrix and(final long[] left, final long[] right) {
		final BitMatrix	result = new BitMatrix(numberOfRows(), numberOfColumns());
		final long[]	target = result.content;
		
		for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
			target[index] = left[index] & right[index];
		}
		result.completed = false;
		return result;
	}
}
