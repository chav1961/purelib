package chav1961.purelib.matrix.internal;

import java.io.DataInput;
import java.io.IOException;
import java.util.Arrays;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.ApplyBit;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;

public class FloatComplexMatrix implements Matrix {
	private final int		rows;
	private final int		cols;
	private final float[]	content;
	private boolean			completed = true;

	public FloatComplexMatrix(final int rows, final int columns) {
		if (rows <= 0) {
			throw new IllegalArgumentException("Rows ["+rows+"] must be greater than 0");
		}
		else if (columns <= 0) {
			throw new IllegalArgumentException("Columns ["+columns+"] must be greater than 0");
		}
		else {
			this.rows = rows;
			this.cols = columns;
			this.content = new float[2 * rows * columns];
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		final FloatComplexMatrix	result = new FloatComplexMatrix(rows, cols);
		
		System.arraycopy(this.content, 0, result.content, 0, result.content.length);
		return result;
	}
	
	@Override
	public void close() throws RuntimeException {
	}

	@Override
	public Type getType() {
		return Type.COMPLEX_FLOAT;
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
	public int[] extractInts(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			ensureInside(piece);
			final float[]	source = this.content;
			final int[]		result = new int[2 * piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (int)source[2 * ((y0 + y)*numberOfColumns() + (x0 + x))];
					result[where++] = (int)source[2 * ((y0 + y)*numberOfColumns() + (x0 + x)) + 1];
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
			final float[]	source = this.content;
			final long[]	result = new long[2 * piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (long)source[2 * ((y0 + y)*numberOfColumns() + (x0 + x))];
					result[where++] = (long)source[2 * ((y0 + y)*numberOfColumns() + (x0 + x)) + 1];
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
			final float[]	result = new float[2 * piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = source[2 * ((y0 + y)*numberOfColumns() + (x0 + x))];
					result[where++] = source[2 * ((y0 + y)*numberOfColumns() + (x0 + x)) + 1];
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
			final float[]	source = this.content;
			final double[]	result = new double[2 * piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (double)source[2 * ((y0 + y)*numberOfColumns() + (x0 + x))];
					result[where++] = (double)source[2 * ((y0 + y)*numberOfColumns() + (x0 + x)) + 1];
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
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
loop:		for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					if (where >= content.length-1) {
						break loop;
					}
					else {
						result[2 * ((y0 + y)*numberOfColumns() + (x0 + x))] = (float)content[where++];
						result[2 * ((y0 + y)*numberOfColumns() + (x0 + x)) + 1] = (float)content[where++];
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
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
loop:		for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					if (where >= content.length-1) {
						break loop;
					}
					else {
						result[2 * ((y0 + y)*numberOfColumns() + (x0 + x))] = (float)content[where++];
						result[2 * ((y0 + y)*numberOfColumns() + (x0 + x)) + 1] = (float)content[where++];
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
					if (where >= content.length-1) {
						break loop;
					}
					else {
						result[2 * ((y0 + y)*numberOfColumns() + (x0 + x))] = (float)content[where++];
						result[2 * ((y0 + y)*numberOfColumns() + (x0 + x)) + 1] = (float)content[where++];
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
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			int				where = 0;
			
			ensureCompleted();
loop:		for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					if (where >= content.length-1) {
						break loop;
					}
					else {
						result[2 * ((y0 + y)*numberOfColumns() + (x0 + x))] = (float)content[where++];
						result[2 * ((y0 + y)*numberOfColumns() + (x0 + x)) + 1] = (float)content[where++];
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
	public Matrix assign(Piece piece, DataInput content, Type type) throws IOException {
		// TODO Auto-generated method stub
		return null;
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
		return fill(value, 0);
	}

	@Override
	public Matrix fill(final Piece piece, final float value) {
		return fill(piece, value, 0);
	}

	@Override
	public Matrix fill(final Piece piece, final float real, final float image) {
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
					result[2 * ((y0 + y)*numberOfColumns() + (x0 + x))] = real;
					result[2 * ((y0 + y)*numberOfColumns() + (x0 + x)) + 1] = image;
				}
			}
			return this;
		}
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
	public Matrix fill(Piece piece, double real, double image) {
		return fill(piece, (float)real, (float)image);
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
					break;
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
				case COMPLEX_DOUBLE : 
					return add(content.extractDoubles());
				case COMPLEX_FLOAT 	:
					return add(content.extractFloats());
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
		return addValue(value, 0);
	}

	@Override
	public Matrix addValue(float real, float image) {
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] + ((index & 0x01) == 0 ? real : image); 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix addValue(final double value) {
		return addValue((float)value);
	}

	@Override
	public Matrix addValue(final double real, final double image) {
		return addValue((float)real, (float)image);
	}

	@Override
	public Matrix subtract(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
				case COMPLEX_DOUBLE : 
					return subtract(content.extractDoubles());
				case COMPLEX_FLOAT :
					return subtract(content.extractFloats());
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
		return subtractValue(value, 0);
	}

	@Override
	public Matrix subtractValue(final float real, final float image) {
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] - ((index & 0x01) == 0 ? real : image); 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix subtractValue(final double value) {
		return subtractValue((float)value);
	}

	@Override
	public Matrix subtractValue(final double real, final double image) {
		return subtractValue((float)real, (float)image);
	}

	@Override
	public Matrix subtractFrom(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				target = result.content;
			
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
				case COMPLEX_DOUBLE : 
					return subtractFrom(content.extractDoubles());
				case COMPLEX_FLOAT 	:
					return subtractFrom(content.extractFloats());
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
		return subtractFromValue(value, 0);
	}

	@Override
	public Matrix subtractFromValue(final float real, final float image) {
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = ((index & 0x01) == 0 ? real : image) - source[index]; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix subtractFromValue(final double value) {
		return subtractFromValue((float)value);
	}

	@Override
	public Matrix subtractFromValue(final double real, final double image) {
		return subtractFromValue((float)real, (float)image);
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(this.numberOfRows(), content.numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			final int					maxY = this.numberOfRows(), maxX = content.numberOfColumns();
			final int					colSize = this.numberOfColumns(), maxK = content.numberOfRows(); 
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : 
					final double[]	tempD = content.extractDoubles();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							float	real = 0, image = 0;
							
							for(int k = 0; k < maxK; k++) {
								real += source[2 * (y * colSize + k)] * tempD[2 * (k * maxX + x)] - source[2 * (y * colSize + k) + 1] * tempD[2 * (k * maxX + x) + 1];
								image += source[2 * (y * colSize + k) + 1] * tempD[2 * (k * maxX + x)] + source[2 * (y * colSize + k)] * tempD[2 * (k * maxX + x) + 1];
							}
							target[2 * (y * maxX + x)] = real;
							target[2 * (y * maxX + x) + 1] = image;
						}
					}
					break;
				case COMPLEX_FLOAT 	:
					final float[]	tempF = content.extractFloats();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							float	real = 0, image = 0;
							
							for(int k = 0; k < maxK; k++) {
								real += source[2 * (y * colSize + k)] * tempF[2 * (k * maxX + x)] - source[2 * (y * colSize + k) + 1] * tempF[2 * (k * maxX + x) + 1];
								image += source[2 * (y * colSize + k) + 1] * tempF[2 * (k * maxX + x)] + source[2 * (y * colSize + k)] * tempF[2 * (k * maxX + x) + 1];
							}
							target[2 * (y * maxX + x)] = real;
							target[2 * (y * maxX + x) + 1] = image;
						}
					}
					break;
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(content.numberOfRows(), this.numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			final int				maxY = content.numberOfRows(), maxX = this.numberOfColumns();
			final int				colSize = content.numberOfColumns(), maxK = this.numberOfRows(); 
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : 
					final double[]	tempD = content.extractDoubles();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							float	real = 0, image = 0;
							
							for(int k = 0; k < maxK; k++) {
								real += tempD[2 * (y * colSize + k)] * source[2 * (k * maxX + x)] - tempD[2 * (y * colSize + k) + 1] * source[2 * (k * maxX + x) + 1];
								image += tempD[2 * (y * colSize + k) + 1] * source[2 * (k * maxX + x)] + tempD[2 * (y * colSize + k)] * source[2 * (k * maxX + x) + 1];
							}
							target[2 * (y * maxX + x)] = real;
							target[2 * (y * maxX + x) + 1] = image;
						}
					}
					break;
				case COMPLEX_FLOAT 	:
					final float[]	tempF = content.extractFloats();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							float	real = 0, image = 0;
							
							for(int k = 0; k < maxK; k++) {
								real += tempF[2 * (y * colSize + k)] * source[2 * (k * maxX + x)] - tempF[2 * (y * colSize + k) + 1] * source[2 * (k * maxX + x) + 1];
								image += tempF[2 * (y * colSize + k) + 1] * source[2 * (k * maxX + x)] + tempF[2 * (y * colSize + k)] * source[2 * (k * maxX + x) + 1];
							}
							target[2 * (y * maxX + x)] = real;
							target[2 * (y * maxX + x) + 1] = image;
						}
					}
					break;
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
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
		return mulValue(value, 0);
	}

	@Override
	public Matrix mulValue(final float real, final float image) {
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
		final float[]				source = this.content;
		final float[]				target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index += 2) {
			target[index] = source[index] * real - source[index + 1] * image; 
			target[index + 1] = source[index + 1] * real + source[index] * image; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix mulValue(final double value) {
		return mulValue((float)value);
	}

	@Override
	public Matrix mulValue(final double real, final double image) {
		return mulValue((float)real, (float)image);
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
		return divValue(value, 0);
	}

	@Override
	public Matrix divValue(final float real, final float image) {
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
		final float[]				source = this.content;
		final float[]				target = result.content;
		final float					quad = 1 / (real * real + image * image);
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index += 2) {
			target[index] = (source[index] * real + source[index + 1] * image) * quad; 
			target[index + 1] = (source[index + 1] * real - source[index] * image) * quad; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix divValue(final double value) {
		return divValue((float)value);
	}

	@Override
	public Matrix divValue(final double real, final double image) {
		return divValue((float)real, (float)image);
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
		return divFromValue(value, 0);
	}

	@Override
	public Matrix divFromValue(final float real, final float image) {
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
		final float[]			source = this.content;
		final float[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index += 2) {
			final float	quad = 1 / (source[index] * source[index] + source[index + 1] * source[index + 1]);
			
			target[index] = (real * source[index] + image * source[index + 1]) * quad; 
			target[index + 1] = (image * source[index] - real * source[index + 1]) * quad; 
		}
		result.completed = false;
		return result;
	}

	@Override
	public Matrix divFromValue(final double value) {
		return divFromValue((float)value);
	}

	@Override
	public Matrix divFromValue(final double real, final double image) {
		return divFromValue((float)real, (float)image);
	}

	@Override
	public Matrix mulHadamard(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				target[2 * index] = source[2 * index] * content[2 * index] - source[2 * index + 1] * content[2 * index + 1]; 
				target[2 * index + 1] = source[2 * index + 1] * content[2 * index] + source[2 * index] * content[2 * index + 1]; 
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				target[2 * index] = source[2 * index] * content[2 * index] - source[2 * index + 1] * content[2 * index + 1]; 
				target[2 * index + 1] = source[2 * index + 1] * content[2 * index] + source[2 * index] * content[2 * index + 1]; 
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				target[2 * index] = source[2 * index] * content[2 * index] - source[2 * index + 1] * content[2 * index + 1]; 
				target[2 * index + 1] = source[2 * index + 1] * content[2 * index] + source[2 * index] * content[2 * index + 1]; 
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				target[2 * index] = (float) (source[2 * index] * content[2 * index] - source[2 * index + 1] * content[2 * index + 1]); 
				target[2 * index + 1] = (float) (source[2 * index + 1] * content[2 * index] + source[2 * index] * content[2 * index + 1]); 
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
				case COMPLEX_DOUBLE : 
					return mulHadamard(content.extractDoubles());
				case COMPLEX_FLOAT 	:
					return mulHadamard(content.extractFloats());
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				final float	real = content[2 * index];
				final float	image = content[2 * index + 1];
				final float	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (source[2 * index] * real + source[2 * index + 1] * image) * quad;  
				target[2 * index + 1] =  (source[2 * index] * image - source[2 * index + 1] * real) * quad; 
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				final float	real = content[2 * index];
				final float	image = content[2 * index + 1];
				final float	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (source[2 * index] * real + source[2 * index + 1] * image) * quad;  
				target[2 * index + 1] =  (source[2 * index] * image - source[2 * index + 1] * real) * quad; 
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				final float	real = content[2 * index];
				final float	image = content[2 * index + 1];
				final float	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (source[2 * index] * real + source[2 * index + 1] * image) * quad;  
				target[2 * index + 1] =  (source[2 * index] * image - source[2 * index + 1] * real) * quad; 
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				final double	real = content[2 * index];
				final double	image = content[2 * index + 1];
				final double	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (float) ((source[2 * index] * real + source[2 * index + 1] * image) * quad);  
				target[2 * index + 1] =  (float) ((source[2 * index] * image - source[2 * index + 1] * real) * quad); 
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
				case COMPLEX_DOUBLE : 
					return mulInvHadamard(content.extractDoubles());
				case COMPLEX_FLOAT 	:
					return mulInvHadamard(content.extractFloats());
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				final float	real = source[2 * index];
				final float	image = source[2 * index + 1];
				final float	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (content[2 * index] * real + content[2 * index + 1] * image) * quad;  
				target[2 * index + 1] =  (content[2 * index] * image - content[2 * index + 1] * real) * quad; 
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				final float	real = source[2 * index];
				final float	image = source[2 * index + 1];
				final float	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (content[2 * index] * real + content[2 * index + 1] * image) * quad;  
				target[2 * index + 1] =  (content[2 * index] * image - content[2 * index + 1] * real) * quad; 
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				final float	real = source[2 * index];
				final float	image = source[2 * index + 1];
				final float	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (content[2 * index] * real + content[2 * index + 1] * image) * quad;  
				target[2 * index + 1] =  (content[2 * index] * image - content[2 * index + 1] * real) * quad; 
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length) / 2; index < maxIndex; index++) {
				final double	real = source[2 * index];
				final double	image = source[2 * index + 1];
				final double	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (float) ((content[2 * index] * real + content[2 * index + 1] * image) * quad);  
				target[2 * index + 1] =  (float) ((content[2 * index] * image - content[2 * index + 1] * real) * quad); 
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
				case COMPLEX_DOUBLE : 
					return mulInvFromHadamard(content.extractDoubles());
				case COMPLEX_FLOAT 	:
					return mulInvFromHadamard(content.extractFloats());
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(this.numberOfRows() * content.numberOfRows(), this.numberOfColumns() * content.numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			final int 					maxY1 = this.numberOfRows(), maxY2 = content.numberOfRows();
			final int 					maxX1 = this.numberOfColumns(), maxX2 = content.numberOfColumns();
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : 
					final double[]	tempD = content.extractDoubles();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final float	real = source[2 * (y1 * maxX1 + x1)];
							final float	image = source[2 * (y1 * maxX1 + x1) + 1];
							
							if (real != 0 || image != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[2 * targetIndex] = (float) (real * tempD[2 * sourceIndex] - image * tempD[2 * sourceIndex + 1]);
										target[2 * targetIndex + 1] = (float) (real * tempD[2 * sourceIndex + 1] + image * tempD[2 * sourceIndex]);
									}
								}
							}
						}
					}
					break;
				case COMPLEX_FLOAT 	:
					final float[]	tempF = content.extractFloats();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final float	real = source[2 * (y1 * maxX1 + x1)];
							final float	image = source[2 * (y1 * maxX1 + x1) + 1];
							
							if (real != 0 || image != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[2 * targetIndex] = (float) (real * tempF[2 * sourceIndex] - image * tempF[2 * sourceIndex + 1]);
										target[2 * targetIndex + 1] = (float) (real * tempF[2 * sourceIndex + 1] + image * tempF[2 * sourceIndex]);
									}
								}
							}
						}
					}
					break;
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(this.numberOfRows() * content.numberOfRows(), this.numberOfColumns() * content.numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			final int 					maxY1 = content.numberOfRows(), maxY2 = this.numberOfRows();
			final int 					maxX1 = content.numberOfColumns(), maxX2 = this.numberOfColumns();
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : 
					final double[]	tempD = content.extractDoubles();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	real = tempD[2 * (y1 * maxX1 + x1)];
							final double	image = tempD[2 * (y1 * maxX1 + x1) + 1];
							
							if (real != 0 || image != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[2 * targetIndex] = (float) (real * source[2 * sourceIndex] - image * source[2 * sourceIndex + 1]);
										target[2 * targetIndex + 1] = (float) (real * source[2 * sourceIndex + 1] + image * source[2 * sourceIndex]);
									}
								}
							}
						}
					}
					break;
				case COMPLEX_FLOAT 	:
					final float[]	tempF = content.extractFloats();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final float	real = tempF[2 * (y1 * maxX1 + x1)];
							final float	image = tempF[2 * (y1 * maxX1 + x1) + 1];
							
							if (real != 0 || image != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[2 * targetIndex] = (float) (real * source[2 * sourceIndex] - image * source[2 * sourceIndex + 1]);
										target[2 * targetIndex + 1] = (float) (real * source[2 * sourceIndex + 1] + image * source[2 * sourceIndex]);
									}
								}
							}
						}
					}
					break;
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG :
					throw new UnsupportedOperationException("Attempt to multiply real and complex matrices");
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
			final FloatComplexMatrix	result = new FloatComplexMatrix(this.numberOfRows(), this.numberOfColumns());
			final float[]				identity = result.content;
			final float[]				source = this.content.clone();
			final int					colSize = numberOfColumns();
			
			for(int index = 0; index < colSize; index++) {	// Make identity matrix
				identity[2 * index * (colSize + 1)] = 1;
				identity[2 * index * (colSize + 1) + 1] = 0;
			}
			for(int y = 0; y < colSize; y++) {
				final float	real = source[2 * (y * (colSize + 1))];	// Take diagonal element.
				final float	image = source[2 * (y * (colSize + 1)) + 1];
				final float	quad = 1 / (real * real + image * image);
				
				if (quad == 0) {
					throw new IllegalArgumentException("Matrix has zero element on diagonal");
				}
				
				for(int x = 0; x < colSize; x++) {		// divide all line by diagonal element
					source[2 * (y * colSize + x)] = (source[2 * (y * colSize + x)] * real + source[2 * (y * colSize + x) + 1] * image) * quad; 
					source[2 * (y * colSize + x) + 1] = (source[2 * (y * colSize + x) + 1] * real - source[2 * (y * colSize + x)] * image) * quad; 
					identity[2 * (y * colSize + x)] = (identity[2 * (y * colSize + x)] * real + identity[2 * (y * colSize + x) + 1] * image) * quad;
					identity[2 * (y * colSize + x) + 1] = (identity[2 * (y * colSize + x) + 1] * real - identity[2 * (y * colSize + x)] * image) * quad;
				}
				for(int i = y + 1; i < colSize; i++) {	// subtract current line from all lines below to make zeroes at the current column
					final float	real2 = source[2 * (i * colSize + y)];
					final float	image2 = source[2 * (i * colSize + y) + 1];
					
					for(int x = 0; x < colSize; x++) {
						source[2 * (i * colSize + x)] -= real2 * source[2 * (y * colSize + x)] - image2 * source[2 * (y * colSize + x) + 1];
						source[2 * (i * colSize + x) + 1] -= real2 * source[2 * (y * colSize + x) + 1] + image2 * source[2 * (y * colSize + x)];
						identity[2 * (i * colSize + x)] -= real2 * identity[2 * (y * colSize + x)] - image2 * identity[2 * (y * colSize + x) + 1];
						identity[2 * (i * colSize + x) + 1] -= real2 * identity[2 * (y * colSize + x) + 1] + image2 * identity[2 * (y * colSize + x)];
					}
				}
			}
			for(int y = colSize-1; y >= 0; y--) {	// subtract current line from all lines above to make zeroes at the current column 
				for(int i = y - 1; i >= 0; i--) {
					final float	real2 = source[2 * (i * colSize + y)];
					final float	image2 = source[2 * (i * colSize + y) + 1];
					
					for(int x = 0; x < colSize; x++) {
						source[2 * (i * colSize + x)] -= real2 * source[2 * (y * colSize + x)] - image2 * source[2 * (y * colSize + x) + 1];
						source[2 * (i * colSize + x) + 1] -= real2 * source[2 * (y * colSize + x) + 1] + image2 * source[2 * (y * colSize + x)];
						identity[2 * (i * colSize + x)] -= real2 * identity[2 * (y * colSize + x)] - image2 * identity[2 * (y * colSize + x) + 1];
						identity[2 * (i * colSize + x) + 1] -= real2 * identity[2 * (y * colSize + x) + 1] + image2 * identity[2 * (y * colSize + x)];
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
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfColumns(), numberOfRows());
		final float[]				source = this.content;
		final float[]				target = result.content;
		final int					rows = numberOfRows(), cols = numberOfColumns();  
		
		for(int y = 0; y < rows; y++) {
			for(int x = 0; x < cols; x++) {
				target[2 * (x*rows + y)] = source[2 * (y*cols + x)]; 
				target[2 * (x*rows + y) + 1] = source[2 * (y*cols + x) + 1]; 
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
		throw new IllegalStateException("Attempt to get real determinant for complex matrix");
	}

	@Override
	public Number track() {
		throw new IllegalStateException("Attempt to get real track for complex matrix");
	}
	
	@Override
	public Number[] det2() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Only square matrix can be inverted");
		}
		else {
			final float[]	source = this.content.clone();
			final int		colSize = numberOfColumns();
			float			detReal = 1, detImage = 0;

			ensureCompleted();
			for(int y = 0; y < colSize; y++) {
				final float	real = source[2 * (y * (colSize + 1))];		// Take diagonal element.
				final float	image = source[2 * (y * (colSize + 1)) + 1];		// Take diagonal element.
				final float	quad = 1 / (real * real + image * image);

				detReal = detReal * real - detImage * image;
				detImage = detReal * image + detImage * real;
				for(int x = 0; x < colSize; x++) {		// divide all line by diagonal element
					source[2 * (y * colSize + x)] = (source[2 * (y * colSize + x)] * real + source[2 * (y * colSize + x) + 1] * image) * quad;
					source[2 * (y * colSize + x) + 1] =  (source[2 * (y * colSize + x)] * image - source[2 * (y * colSize + x) + 1] * real) * quad;
				}
				for(int i = y + 1; i < colSize; i++) {	// subtract current line from all lines below to make zeroes at the current column
					final float	real2 = source[2 * (i * colSize + y)];
					final float	image2 = source[2 * (i * colSize + y) + 1];
					
					for(int x = 0; x < colSize; x++) {
						source[2 * (i * colSize + x)] -= source[2 * (y * colSize + x)] * real2 - source[2 * (y * colSize + x) + 1] * image2;
						source[2 * (i * colSize + x) + 1] -= source[2 * (y * colSize + x) + 1] * real2 + source[2 * (y * colSize + x)] * image2;
					}
				}
			}
			return new Number[] {detReal, detImage};
		}
	}

	@Override
	public Number[] track2() {
		ensureCompleted();
		final float[]	source = this.content;
		final int		colSize = numberOfColumns();
		float	real = 0, image = 0;
		
		ensureCompleted();
		for(int index = 0; index < colSize; index++) {	// Calculate diagonal sum
			real += source[2 * (index * (colSize + 1))];
			image += source[2 * (index * (colSize + 1)) + 1];
		}
		return new Number[] {real, image};
	}

	@Override
	public String toHumanReadableString() {
		final StringBuilder	sb = new StringBuilder();
		
		sb.append("Matrix: type=").append(getType()).append(", size=").append(numberOfRows()).append('x').append(numberOfColumns()).append(":\n");
		for(int y = 0; y < numberOfRows(); y++) {
			for(int x = 0; x < numberOfColumns(); x++) {
				sb.append(String.format(" %1$15e %2$15ei", content[2 * (y*numberOfColumns()+x)], content[2 * (y*numberOfColumns()+x)]));
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
		throw new UnsupportedOperationException("Bit apply can't be used for non-bit matrices");
	}
	
	@Override
	public Matrix apply(final Piece piece, final ApplyInt callback) {
		throw new UnsupportedOperationException("Real apply is not supported for complex matrix");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyLong callback) {
		throw new UnsupportedOperationException("Real apply is not supported for complex matrix");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyFloat callback) {
		throw new UnsupportedOperationException("Real apply is not supported for complex matrix");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyDouble callback) {
		throw new UnsupportedOperationException("Real apply is not supported for complex matrix");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyFloat2 callback) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Ccan't be null");
		}
		else {
			final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns()); 
			final float[]	source = this.content;
			final float[]	target = result.content;
			final float[]	temp = new float[2];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight();
			
			ensureCompleted();
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					temp[0] = source[2 * ((y0 + y)*numberOfColumns() + (x0 + x))];
					temp[1] = source[2 * ((y0 + y)*numberOfColumns() + (x0 + x)) + 1];
					callback.apply(y0 + y, x0 + x, temp);
					target[2 * ((y0 + y)*numberOfColumns() + (x0 + x))] = temp[0];
					target[2 * ((y0 + y)*numberOfColumns() + (x0 + x)) + 1] = temp[1];
				}
			}
			result.completed = false;
			return result;
		}
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyDouble2 callback) {
		throw new UnsupportedOperationException("Double complex apply is not supported for float complex matrix");
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
		final FloatComplexMatrix	result;
		final float[]			source = this.content;
		final float[]			target;
		float	real, image;
		
		switch (dir) {
			case ByColumns	:
				result = new FloatComplexMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					real = 0;
					image = 0;
					for(int x = 0; x < numberOfColumns(); x++) {
						real += source[2 * (y * numberOfColumns() + x)];
						image += source[2 * (y * numberOfColumns() + x) + 1];
					}
					target[2 * y] = real / numberOfColumns();
					target[2 * y + 1] = image / numberOfColumns();
				}
				break;
			case ByRows		:
				result = new FloatComplexMatrix(1, numberOfColumns()); 
				target = result.content;
				
				for(int x = 0; x < numberOfColumns(); x++) {
					real = 0;
					image = 0;
					for(int y = 0; y < numberOfRows(); y++) {
						real += source[2 * (y * numberOfColumns() + x)];
						image += source[2 * (y * numberOfColumns() + x) + 1];
					}
					target[2 * x] = real / numberOfRows();
					target[2 * x + 1] = image / numberOfRows();
				}
				break;
			case Total		:
				result = new FloatComplexMatrix(1, 1); 
				target = result.content;
				
				real = 0;
				image = 0;
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < numberOfColumns(); x++) {
						real += source[2 * (y * numberOfColumns() + x)];
						image += source[2 * (y * numberOfColumns() + x) + 1];
					}
				}
				target[0] = real / (numberOfRows() * numberOfColumns());
				target[1] = image / (numberOfRows() * numberOfColumns());
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.completed = false;
		return result;
	}
	
	private Matrix aggregateMax(final AggregateDirection dir) {
		final FloatComplexMatrix	result;
		final float[]			source = this.content;
		final float[]			target;
		float	val, real, image;
		
		switch (dir) {
			case ByColumns	:
				result = new FloatComplexMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					real = source[2 * (y * numberOfColumns() + 0)]; 
					image = source[2 * (y * numberOfColumns() + 0) + 1]; 
					val = real * real + image * image ;
					for(int x = 0; x < numberOfColumns(); x++) {
						final float	tempR = source[2 * (y * numberOfColumns() + x)]; 
						final float	tempI = source[2 * (y * numberOfColumns() + x) + 1];
						
						if (tempR * tempR + tempI * tempI > val) {
							real = tempR;
							image = tempI;
							val = tempR * tempR + tempI * tempI;
						}
					}
					target[2 * y] = real;
					target[2 * y + 1] = image;
				}
				break;
			case ByRows		:
				result = new FloatComplexMatrix(1, numberOfColumns()); 
				target = result.content;
				
				for(int x = 0; x < numberOfColumns(); x++) {
					real = source[2 * (x * numberOfColumns() + 0)]; 
					image = source[2 * (x * numberOfColumns() + 0) + 1]; 
					val = real * real + image * image ;
					for(int y = 0; y < numberOfRows(); y++) {
						final float	tempR = source[2 * (y * numberOfColumns() + x)]; 
						final float	tempI = source[2 * (y * numberOfColumns() + x) + 1];
						
						if (tempR * tempR + tempI * tempI > val) {
							real = tempR;
							image = tempI;
							val = tempR * tempR + tempI * tempI;
						}
					}
					target[2 * x] = real;
					target[2 * x + 1] = image;
				}
				break;
			case Total		:
				result = new FloatComplexMatrix(1, 1); 
				target = result.content;
				
				real = source[0]; 
				image = source[1]; 
				val = real * real + image * image ;
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < numberOfColumns(); x++) {
						final float	tempR = source[2 * (y * numberOfColumns() + x)]; 
						final float	tempI = source[2 * (y * numberOfColumns() + x) + 1];
						
						if (tempR * tempR + tempI * tempI > val) {
							real = tempR;
							image = tempI;
							val = tempR * tempR + tempI * tempI;
						}
					}
				}
				target[0] = real;
				target[1] = image;
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.completed = false;
		return result;
	}

	private Matrix aggregateMin(final AggregateDirection dir) {
		final FloatComplexMatrix	result;
		final float[]			source = this.content;
		final float[]			target;
		float	val, real, image;
		
		switch (dir) {
			case ByColumns	:
				result = new FloatComplexMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					real = source[2 * (y * numberOfColumns() + 0)]; 
					image = source[2 * (y * numberOfColumns() + 0) + 1]; 
					val = real * real + image * image ;
					for(int x = 0; x < numberOfColumns(); x++) {
						final float	tempR = source[2 * (y * numberOfColumns() + x)]; 
						final float	tempI = source[2 * (y * numberOfColumns() + x) + 1];
						
						if (tempR * tempR + tempI * tempI < val) {
							real = tempR;
							image = tempI;
							val = tempR * tempR + tempI * tempI;
						}
					}
					target[2 * y] = real;
					target[2 * y + 1] = image;
				}
				break;
			case ByRows		:
				result = new FloatComplexMatrix(1, numberOfColumns()); 
				target = result.content;
				
				for(int x = 0; x < numberOfColumns(); x++) {
					real = source[2 * (x * numberOfColumns() + 0)]; 
					image = source[2 * (x * numberOfColumns() + 0) + 1]; 
					val = real * real + image * image ;
					for(int y = 0; y < numberOfRows(); y++) {
						final float	tempR = source[2 * (y * numberOfColumns() + x)]; 
						final float	tempI = source[2 * (y * numberOfColumns() + x) + 1];
						
						if (tempR * tempR + tempI * tempI < val) {
							real = tempR;
							image = tempI;
							val = tempR * tempR + tempI * tempI;
						}
					}
					target[2 * x] = real;
					target[2 * x + 1] = image;
				}
				break;
			case Total		:
				result = new FloatComplexMatrix(1, 1); 
				target = result.content;
				
				real = source[0]; 
				image = source[1]; 
				val = real * real + image * image ;
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < numberOfColumns(); x++) {
						final float	tempR = source[2 * (y * numberOfColumns() + x)]; 
						final float	tempI = source[2 * (y * numberOfColumns() + x) + 1];
						
						if (tempR * tempR + tempI * tempI < val) {
							real = tempR;
							image = tempI;
							val = tempR * tempR + tempI * tempI;
						}
					}
				}
				target[0] = real;
				target[1] = image;
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.completed = false;
		return result;
	}

	private Matrix aggregateSum(final AggregateDirection dir) {
		final FloatComplexMatrix	result;
		final float[]			source = this.content;
		final float[]			target;
		float	real, image;
		
		switch (dir) {
			case ByColumns	:
				result = new FloatComplexMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					real = 0;
					image = 0;
					for(int x = 0; x < numberOfColumns(); x++) {
						real += source[2 * (y * numberOfColumns() + x)];
						image += source[2 * (y * numberOfColumns() + x) + 1];
					}
					target[2 * y] = real;
					target[2 * y + 1] = image;
				}
				break;
			case ByRows		:
				result = new FloatComplexMatrix(1, numberOfColumns()); 
				target = result.content;
				
				for(int x = 0; x < numberOfColumns(); x++) {
					real = 0;
					image = 0;
					for(int y = 0; y < numberOfRows(); y++) {
						real += source[2 * (y * numberOfColumns() + x)];
						image += source[2 * (y * numberOfColumns() + x) + 1];
					}
					target[2 * x] = real;
					target[2 * x + 1] = image;
				}
				break;
			case Total		:
				result = new FloatComplexMatrix(1, 1); 
				target = result.content;
				
				real = 0;
				image = 0;
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < numberOfColumns(); x++) {
						real += source[2 * (y * numberOfColumns() + x)];
						image += source[2 * (y * numberOfColumns() + x) + 1];
					}
				}
				target[0] = real;
				target[1] = image;
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.completed = false;
		return result;
	}
}
