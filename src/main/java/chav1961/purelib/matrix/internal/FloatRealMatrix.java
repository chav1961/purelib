package chav1961.purelib.matrix.internal;

import java.util.Arrays;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.ApplyBit;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;

public class FloatRealMatrix implements Matrix {
	private final int		rows;
	private final int		cols;
	private final float[]	content;
	private boolean			completed = true;

	public FloatRealMatrix(final int rows, final int columns) {
		if (rows <= 0) {
			throw new IllegalArgumentException("Rows ["+rows+"] must be greater than 0");
		}
		else if (columns <= 0) {
			throw new IllegalArgumentException("Columns ["+columns+"] must be greater than 0");
		}
		else {
			this.rows = rows;
			this.cols = columns;
			this.content = new float[rows * columns];
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		final FloatRealMatrix	result = new FloatRealMatrix(rows, cols);
		
		System.arraycopy(this.content, 0, result.content, 0, result.content.length);
		return result;
	}
	
	@Override
	public void close() throws RuntimeException {
	}

	@Override
	public Type getType() {
		return Type.REAL_FLOAT;
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
			return Arrays.equals(content, another.extractFloats());
		}
	}

	@Override
	public int[] extractInts() {
		return extractInts(getTotalPiece());
	}

	@Override
	public int[] extractInts(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			ensureInside(piece);
			final float[]	source = this.content;
			final int[]		result = new int[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (int)source[(y0 + y)*numberOfColumns() + (x0 + x)];
				}
			}
			return result;
		}
	}

	@Override
	public long[] extractLongs() {
		return extractLongs(getTotalPiece());
	}

	@Override
	public long[] extractLongs(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			ensureInside(piece);
			final float[]	source = this.content;
			final long[]	result = new long[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (long)source[(y0 + y)*numberOfColumns() + (x0 + x)];
				}
			}
			return result;
		}
	}

	@Override
	public float[] extractFloats() {
		ensureCompleted();
		return content;
	}

	@Override
	public float[] extractFloats(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			ensureInside(piece);
			final float[]	source = this.content;
			final float[]	result = new float[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = source[(y0 + y)*numberOfColumns() + (x0 + x)];
				}
			}
			return result;
		}
	}

	@Override
	public double[] extractDoubles() {
		return extractDoubles(getTotalPiece());
	}

	@Override
	public double[] extractDoubles(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			ensureInside(piece);
			final float[]	source = this.content;
			final double[]	result = new double[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (double)source[(y0 + y)*numberOfColumns() + (x0 + x)];
				}
			}
			return result;
		}
	}

	@Override
	public Matrix assign(final int... content) {
		return assign(getTotalPiece(), content);
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
			final float[]	result = this.content;
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
						result[(y0 + y)*numberOfColumns() + (x0 + x)] = (float)content[where++];
					}
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final long... content) {
		return assign(getTotalPiece(), content);
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
			final float[]	result = this.content;
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
						result[(y0 + y)*numberOfColumns() + (x0 + x)] = (float)content[where++];
					}
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			ensureCompleted();
			System.arraycopy(content, 0, this.content, 0, Math.min(content.length, this.content.length));
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
			final float[]	result = this.content;
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
						result[(y0 + y)*numberOfColumns() + (x0 + x)] = (float)content[where++];
					}
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final double... content) {
		return assign(getTotalPiece(), content);
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
			final float[]	result = this.content;
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
						result[(y0 + y)*numberOfColumns() + (x0 + x)] = (float)content[where++];
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
			return assign(content.extractFloats());
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
			return assign(piece, content.extractFloats());
		}
	}

	@Override
	public Matrix fill(final int value) {
		return fill((float)value);
	}

	@Override
	public Matrix fill(final Piece piece, final int value) {
		return fill(piece, (float)value);
	}

	@Override
	public Matrix fill(final long value) {
		return fill((float)value);
	}

	@Override
	public Matrix fill(final Piece piece, final long value) {
		return fill(piece, (float)value);
	}

	@Override
	public Matrix fill(final float value) {
		Utils.fillArray(content, value);
		return this;
	}

	@Override
	public Matrix fill(final Piece piece, final float value) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			ensureInside(piece);
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[(y0 + y)*numberOfColumns() + (x0 + x)] = value;
				}
			}
			return this;
		}
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
		return fill((float)value);
	}

	@Override
	public Matrix fill(final Piece piece, final double value) {
		return fill(piece, (float)value);
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
					final float[]				sourceCF = this.content;
					final float[]				targetCF = fcm.extractFloats();
					
					for(int index = 0, maxIndex = targetCF.length; index < maxIndex; index++) {
						targetCF[2 * index] = (float)sourceCF[index];
						targetCF[2 * index + 1] = 0;
					}
					return fcm;
				case REAL_DOUBLE	:
					final DoubleRealMatrix	drm = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
					final float[]			sourceD = this.content;
					final double[]			targetD = drm.extractDoubles();
					
					for(int index = 0, maxIndex = targetD.length; index < maxIndex; index++) {
						targetD[index] = sourceD[index];
					}
					return drm;
				case REAL_FLOAT		:
					return this;
				case REAL_INT		:
					final IntRealMatrix		irm = new IntRealMatrix(numberOfRows(), numberOfColumns());
					final float[]			sourceI = this.content;
					final int[]				targetI = irm.extractInts();
					
					for(int index = 0, maxIndex = targetI.length; index < maxIndex; index++) {
						targetI[index] = (int) sourceI[index];
					}
					return irm;
				case REAL_LONG		:
					break;
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] += content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix add(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] += content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix add(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] += content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix add(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] += content[index]; 
			}
			result.completed = false;
			return result;
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
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix addValue(final int value) {
		return addValue((float)value);
	}

	@Override
	public Matrix addValue(final long value) {
		return addValue((float)value);
	}

	@Override
	public Matrix addValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] + value; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix addValue(float real, float image) {
		throw new UnsupportedOperationException("Complex addition is not supported for real matrix");
	}

	@Override
	public Matrix addValue(final double value) {
		return addValue((float)value);
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtract(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtract(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtract(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
			}
			result.completed = false;
			return result;
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
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix subtractValue(final int value) {
		return subtractValue((float)value);
	}

	@Override
	public Matrix subtractValue(final long value) {
		return subtractValue((float)value);
	}

	@Override
	public Matrix subtractValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] - value; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix subtractValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex subtraction is not supported for real matrix");
	}

	@Override
	public Matrix subtractValue(final double value) {
		return subtractValue((float)value);
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] - target[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtractFrom(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] - target[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtractFrom(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] - target[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix subtractFrom(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (float)(content[index] - target[index]); 
			}
			result.completed = false;
			return result;
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
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix subtractFromValue(final int value) {
		return subtractFromValue((float)value);
	}

	@Override
	public Matrix subtractFromValue(final long value) {
		return subtractFromValue((float)value);
	}

	@Override
	public Matrix subtractFromValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = value - source[index]; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix subtractFromValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex subtraction is not supported for real matrix");
	}

	@Override
	public Matrix subtractFromValue(final double value) {
		return subtractFromValue((float)value);
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
			final FloatRealMatrix	result = new FloatRealMatrix(this.numberOfRows(), content.numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			final int				maxY = this.numberOfRows(), maxX = content.numberOfColumns();
			final int				colSize = this.numberOfColumns(), maxK = content.numberOfRows(); 
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					final double[]	tempD = content.extractDoubles();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							float	sum = 0;
							
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
							float	sum = 0;
							
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
							float	sum = 0;
							
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
							float	sum = 0;
							
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
			final FloatRealMatrix	result = new FloatRealMatrix(content.numberOfRows(), this.numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			final int				maxY = content.numberOfRows(), maxX = this.numberOfColumns();
			final int				colSize = content.numberOfColumns(), maxK = this.numberOfRows(); 
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					final double[]	tempD = content.extractDoubles();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							float	sum = 0;
							
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
							float	sum = 0;
							
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
							float	sum = 0;
							
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
							float	sum = 0;
							
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
		return mulValue((float)value);
	}

	@Override
	public Matrix mulValue(final long value) {
		return mulValue((float)value);
	}
 
	@Override
	public Matrix mulValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] * value; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix mulValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex multiplication is not supported for real matrix");
	}

	@Override
	public Matrix mulValue(final double value) {
		return mulValue((float)value);
	}

	@Override
	public Matrix mulValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex multiplication is not supported for real matrix");
	}

	@Override
	public Matrix divValue(final int value) {
		return divValue((float)value);
	}

	@Override
	public Matrix divValue(final long value) {
		return divValue((float)value);
	}

	@Override
	public Matrix divValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		final float				inv = 1 / value;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] * inv; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix divValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex division is not supported for real matrix");
	}

	@Override
	public Matrix divValue(final double value) {
		return divValue((float)value);
	}

	@Override
	public Matrix divValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex division is not supported for real matrix");
	}

	@Override
	public Matrix divFromValue(final int value) {
		return divFromValue((float)value);
	}

	@Override
	public Matrix divFromValue(final long value) {
		return divFromValue((float)value);
	}

	@Override
	public Matrix divFromValue(final float value) {
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = value / source[index]; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix divFromValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex division is not supported for real matrix");
	}

	@Override
	public Matrix divFromValue(final double value) {
		return divFromValue((float)value);
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] * content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulHadamard(final long... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] * content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulHadamard(final float... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] * content[index]; 
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix mulHadamard(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (float) (source[index] * content[index]); 
			}
			result.completed = false;
			return result;
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] / content[index]; 
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] / content[index]; 
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (float) (source[index] / content[index]); 
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] / source[index]; 
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] / source[index]; 
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (float) (content[index] / source[index]); 
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
			final FloatRealMatrix	result = new FloatRealMatrix(this.numberOfRows() * content.numberOfRows(), this.numberOfColumns() * content.numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			final int 				maxY1 = this.numberOfRows(), maxY2 = content.numberOfRows();
			final int 				maxX1 = this.numberOfColumns(), maxX2 = content.numberOfColumns();
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new UnsupportedOperationException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					final double[]	tempD = content.extractDoubles();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final float	k = source[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = (float) (k * tempD[sourceIndex]);
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
							final float	k = source[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = k * tempF[sourceIndex];
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
							final float	k = source[y1 * maxX1 + x1];
							
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
							final float	k = source[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = k * tempL[sourceIndex];
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
			final FloatRealMatrix	result = new FloatRealMatrix(this.numberOfRows() * content.numberOfRows(), this.numberOfColumns() * content.numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			final int 				maxY1 = content.numberOfRows(), maxY2 = this.numberOfRows();
			final int 				maxX1 = content.numberOfColumns(), maxX2 = this.numberOfColumns();
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new UnsupportedOperationException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE	:
					final double[]	tempD = content.extractDoubles();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	k = tempD[y1 * maxX1 + x1];
							
							if (k != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = (float) (k * source[sourceIndex]);
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
							final float	k = tempF[y1 * maxX1 + x1];
							
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
							final float	k = tempI[y1 * maxX1 + x1];
							
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
							final float	k = tempL[y1 * maxX1 + x1];
							
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
			ensureCompleted();
			final FloatRealMatrix	result = new FloatRealMatrix(this.numberOfRows(), this.numberOfColumns());
			final float[]	identity = result.content;
			final float[]	source = this.content.clone();
			final int		colSize = numberOfColumns();
			
			for(int index = 0; index < colSize; index++) {	// Make identity matrix
				identity[index * (colSize + 1)] = 1;
			}
			for(int y = 0; y < colSize; y++) {
				final float	k = 1/ source[y * (colSize + 1)];	// Take diagonal element.
				
				if (k == 0) {
					throw new IllegalArgumentException("Matrix has zero element on diagonal");
				}
				
				for(int x = 0; x < colSize; x++) {		// divide all line by diagonal element
					source[y * colSize + x] *= k;
					identity[y * colSize + x] *= k;
				}
				for(int i = y + 1; i < colSize; i++) {	// subtract current line from all lines below to make zeroes at the current column
					final float	k2 = source[i * colSize + y];
					
					for(int x = 0; x < colSize; x++) {
						source[i * colSize + x] -= k2 * source[y * colSize + x];
						identity[i * colSize + x] -= k2 * identity[y * colSize + x];
					}
				}
			}
			for(int y = colSize-1; y >= 0; y--) {	// subtract current line from all lines above to make zeroes at the current column 
				for(int i = y - 1; i >= 0; i--) {
					final float	k2 = source[i * colSize + y];
					
					for(int x = 0; x < colSize; x++) {
						source[i * colSize + x] -= k2 * source[y * colSize + x];
						identity[i * colSize + x] -= k2 * identity[y * colSize + x];
					}
				}
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix transpose() {
		ensureCompleted();
		final FloatRealMatrix	result = new FloatRealMatrix(numberOfColumns(), numberOfRows());
		final float[]			source = this.content;
		final float[]			target = result.content;
		final int				rows = numberOfRows(), cols = numberOfColumns();  
		
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
			final float[]	source = this.content.clone();
			final int		colSize = numberOfColumns();
			float			det = 1;

			ensureCompleted();
			for(int y = 0; y < colSize; y++) {
				final float	k = source[y * (colSize + 1)];		// Take diagonal element.
				final float	invK = 1 / k;

				det *= k;
				for(int x = 0; x < colSize; x++) {		// divide all line by diagonal element
					source[y * colSize + x] *= invK;
				}
				for(int i = y + 1; i < colSize; i++) {	// subtract current line from all lines below to make zeroes at the current column
					final float	k2 = source[i * colSize + y];
					
					for(int x = 0; x < colSize; x++) {
						source[i * colSize + x] -= k2 * source[y * colSize + x];
					}
				}
			}
			return det;
		}
	}

	@Override
	public Number track() {
		ensureCompleted();
		final float[]	source = this.content;
		final int		colSize = numberOfColumns();
		float	sum = 0;
		
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
	public Matrix apply(final ApplyBit callback) {
		return apply(getTotalPiece(),callback);
	}
	
	@Override
	public Matrix apply(final Piece piece, final ApplyBit callback) {
		throw new UnsupportedOperationException("Bit apply can't be used for non-bit matrices");
	}
	
	@Override
	public Matrix apply(final ApplyInt callback) {
		return apply(getTotalPiece(),callback);
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns()); 
			final float[]	source = this.content;
			final float[]	target = result.content;
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
	public Matrix apply(final ApplyLong callback) {
		return apply(getTotalPiece(),callback);
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns()); 
			final float[]	source = this.content;
			final float[]	target = result.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					target[(y0 + y)*numberOfColumns() + (x0 + x)] = callback.apply(y0 + y, x0 + x, (long)source[(y0 + y)*numberOfColumns() + (x0 + x)]);
				}
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix apply(final ApplyFloat callback) {
		return apply(getTotalPiece(),callback);
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns()); 
			final float[]	source = this.content;
			final float[]	target = result.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					target[(y0 + y)*numberOfColumns() + (x0 + x)] = callback.apply(y0 + y, x0 + x, source[(y0 + y)*numberOfColumns() + (x0 + x)]);
				}
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix apply(final ApplyDouble callback) {
		return apply(getTotalPiece(),callback);
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
			final FloatRealMatrix	result = new FloatRealMatrix(numberOfRows(), numberOfColumns()); 
			final float[]	source = this.content;
			final float[]	target = result.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					target[(y0 + y)*numberOfColumns() + (x0 + x)] = (float)callback.apply(y0 + y, x0 + x, source[(y0 + y)*numberOfColumns() + (x0 + x)]);
				}
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix apply(final ApplyFloat2 callback) {
		return apply(getTotalPiece(),callback);
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyFloat2 callback) {
		throw new UnsupportedOperationException("Complex apply is not supported for real matrix");
	}

	@Override
	public Matrix apply(final ApplyDouble2 callback) {
		return apply(getTotalPiece(),callback);
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
		final FloatRealMatrix	result;
		final float[]			source = this.content;
		final float[]			target;
		float	val;
		
		switch (dir) {
			case ByColumns	:
				result = new FloatRealMatrix(numberOfRows(), 1);
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
				result = new FloatRealMatrix(1, numberOfColumns()); 
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
				result = new FloatRealMatrix(1, 1); 
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
		final FloatRealMatrix	result;
		final float[]			source = this.content;
		final float[]			target;
		float	val;
		
		switch (dir) {
			case ByColumns	:
				result = new FloatRealMatrix(numberOfRows(), 1);
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
				result = new FloatRealMatrix(1, numberOfColumns()); 
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
				result = new FloatRealMatrix(1, 1); 
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
		final FloatRealMatrix	result;
		final float[]			source = this.content;
		final float[]			target;
		float	val;
		
		switch (dir) {
			case ByColumns	:
				result = new FloatRealMatrix(numberOfRows(), 1);
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
				result = new FloatRealMatrix(1, numberOfColumns()); 
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
				result = new FloatRealMatrix(1, 1); 
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
		final FloatRealMatrix	result;
		final float[]			source = this.content;
		final float[]			target;
		float	val;
		
		switch (dir) {
			case ByColumns	:
				result = new FloatRealMatrix(numberOfRows(), 1);
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
				result = new FloatRealMatrix(1, numberOfColumns()); 
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
				result = new FloatRealMatrix(1, 1); 
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
}
