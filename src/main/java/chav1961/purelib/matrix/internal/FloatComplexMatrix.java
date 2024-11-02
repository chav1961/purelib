package chav1961.purelib.matrix.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.matrix.AbstractMatrix;
import chav1961.purelib.matrix.interfaces.Matrix;

public class FloatComplexMatrix extends AbstractMatrix {
	final float[]	content;

	public FloatComplexMatrix(final int rows, final int columns) {
		super(Type.COMPLEX_FLOAT, rows, columns);
		this.content = new float[2 * rows * columns];
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
		
		System.arraycopy(this.content, 0, result.content, 0, result.content.length);
		return result;
	}
	
	@Override
	public void close() throws RuntimeException {
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
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			return Arrays.equals(content, (another instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)another).content : another.extractFloats());
		}
	}

	@Override
	public int[] extractInts(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	source = this.content;
			final int[]		result = new int[2 * piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
					result[where++] = (int)source[index++];
					result[where++] = (int)source[index++];
				}
			}
			return result;
		}
	}

	@Override
	public void extractInts(final Piece piece, final DataOutput dataOutput) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (dataOutput == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
					dataOutput.writeInt((int)source[index++]);
					dataOutput.writeInt((int)source[index++]);
				}
			}
		}
	}

	@Override
	public long[] extractLongs(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	source = this.content;
			final long[]	result = new long[2 * piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
					result[where++] = (long)source[index++];
					result[where++] = (long)source[index++];
				}
			}
			return result;
		}
	}

	@Override
	public void extractLongs(final Piece piece, final DataOutput dataOutput) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (dataOutput == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);

				for(int x = 0; x < maxX; x++) {
					dataOutput.writeLong((long)source[index++]);
					dataOutput.writeLong((long)source[index++]);
				}
			}
		}
	}

	@Override
	public float[] extractFloats(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	source = this.content;
			final float[]	result = new float[2 * piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);

				for(int x = 0; x < maxX; x++) {
					result[where++] = (float) source[index++];
					result[where++] = (float) source[index++];
				}
			}
			return result;
		}
	}

	@Override
	public float[] extractFloats() {
		if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			return content.clone();
		}
	}

	@Override
	public void extractFloats(final Piece piece, final DataOutput dataOutput) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (dataOutput == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);

				for(int x = 0; x < maxX; x++) {
					dataOutput.writeFloat((float) source[index++]);
					dataOutput.writeFloat((float) source[index++]);
				}
			}
		}
	}

	@Override
	public double[] extractDoubles(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	source = this.content;
			final double[]	result = new double[2 * piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);

				for(int x = 0; x < maxX; x++) {
					result[where++] = (double)source[index++];
					result[where++] = (double)source[index++];
				}
			}
			return result;
		}
	}

	@Override
	public void extractDoubles(final Piece piece, final DataOutput dataOutput) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);

				for(int x = 0; x < maxX; x++) {
					dataOutput.writeDouble((double)source[index++]);
					dataOutput.writeDouble((double)source[index++]);
				}
			}
		}
	}
	
	@Override
	public Matrix assign(final Piece piece, final int... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);

				for(int x = 0; x < maxX; x++) {
					result[index++] = (float)content[where++];
					result[index++] = (float)content[where++];
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
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);

				for(int x = 0; x < maxX; x++) {
					result[index++] = (float)content[where++];
					result[index++] = (float)content[where++];
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
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			System.arraycopy(content, 0, this.content, 0, Math.min(content.length, this.content.length));
			return this;
		}		
	}

	@Override
	public Matrix assign(final Piece piece, final float... content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);

				for(int x = 0; x < maxX; x++) {
					result[index++] = content[where++];
					result[index++] = content[where++];
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
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);

				for(int x = 0; x < maxX; x++) {
					result[index++] = (float)content[where++];
					result[index++] = (float)content[where++];
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final Piece piece, final Matrix matrix) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (matrix == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE	:
					return assign(piece, (matrix instanceof DoubleComplexMatrix) ? ((DoubleComplexMatrix)matrix).content : matrix.extractDoubles());
				case COMPLEX_FLOAT	:
					return assign(piece, (matrix instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)matrix).content : matrix.extractFloats());
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG : case BIT :
					throw new IllegalArgumentException("Attempl to assign real matrix to complex matrix. Use cast() before");
				default:
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix assign(final Piece piece, final DataInput content, final Type type) throws IOException {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (type == null) {
			throw new NullPointerException("Type can't be null");
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					try {
						switch (type) {
							case COMPLEX_DOUBLE	:
							case REAL_DOUBLE	:
								result[2 * ((y0 + y)*cols + (x0 + x))] = (float)content.readDouble();
								result[2 * ((y0 + y)*cols + (x0 + x)) + 1] = (float)content.readDouble();
								break;
							case COMPLEX_FLOAT	:
							case REAL_FLOAT		:
								result[2 * ((y0 + y)*cols + (x0 + x))] = content.readFloat();
								result[2 * ((y0 + y)*cols + (x0 + x)) + 1] = content.readFloat();
								break;
							case REAL_INT		:
								result[2 * ((y0 + y)*cols + (x0 + x))] = content.readInt();
								result[2 * ((y0 + y)*cols + (x0 + x)) + 1] = content.readInt();
								break;
							case REAL_LONG		:
								result[2 * ((y0 + y)*cols + (x0 + x))] = content.readLong();
								result[2 * ((y0 + y)*cols + (x0 + x)) + 1] = content.readLong();
								break;
							case BIT			:
								result[2 * ((y0 + y)*cols + (x0 + x))] = content.readBoolean() ? 1 : 0;
								result[2 * ((y0 + y)*cols + (x0 + x)) + 1] = 0;
								break;
							default:
								throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
						}
					} catch (EOFException exc) {
					}
				}
			}
			return this;
		}
	}	
	
	@Override
	public Matrix fill(final Piece piece, final int value) {
		return fill(piece, (double)value);
	}

	@Override
	public Matrix fill(final Piece piece, final long value) {
		return fill(piece, (double)value);
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
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				int index = 2 * ((y0 + y)*cols + x0);

				for(int x = 0; x < maxX; x++) {
					result[index++] = real;
					result[index++] = image;
				}
			}
			return this;
		}
	}

	@Override
	public Matrix fill(final Piece piece, final double value) {
		return fill(piece, value, 0);
	}

	@Override
	public Matrix fill(final Piece piece, final double real, double image) {
		return fill(piece, (float)real, (float)image);
	}

	@Override
	public Matrix cast(final Type type) {
		if (type == null) {
			throw new NullPointerException("Cast type can't be null");
		}
		else {
			final float[]	sourceF = this.content;
			
			switch (type) {
				case COMPLEX_DOUBLE	:
					final DoubleComplexMatrix	dcm = new DoubleComplexMatrix(numberOfRows(), numberOfColumns());
					final double[]				targetCD = dcm.content;
					
					for(int index = 0, maxIndex = sourceF.length; index < maxIndex; index++) {
						targetCD[index] = sourceF[index];
					}
					return dcm;
				case COMPLEX_FLOAT	:
					try {
						return (Matrix) this.clone();
					} catch (CloneNotSupportedException e) {
						return this;
					}
				case REAL_DOUBLE	:
					final DoubleRealMatrix	drm = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
					final double[]			targetD = drm.content;
					
					for(int index = 0, maxIndex = targetD.length; index < maxIndex; index++) {
						targetD[index] = sourceF[2 * index];
					}
					return drm;
				case REAL_FLOAT		:
					final FloatRealMatrix	frm = new FloatRealMatrix(numberOfRows(), numberOfColumns());
					final float[]			targetF = frm.content;
					
					for(int index = 0, maxIndex = targetF.length; index < maxIndex; index++) {
						targetF[index] = (float)sourceF[2 * index];
					}
					return frm;
				case REAL_INT		:
					final IntRealMatrix	irm = new IntRealMatrix(numberOfRows(), numberOfColumns());
					final int[]			targetI = irm.content;
					
					for(int index = 0, maxIndex = targetI.length; index < maxIndex; index++) {
						targetI[index] = (int)sourceF[2 * index];
					}
					return irm;
				case REAL_LONG		:
					final LongRealMatrix	lrm = new LongRealMatrix(numberOfRows(), numberOfColumns());
					final long[]			targetL = lrm.content;
					
					for(int index = 0, maxIndex = targetL.length; index < maxIndex; index++) {
						targetL[index] = (long)sourceF[2 * index];
					}
					return lrm;
				default:
					throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
			}
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] + content[index]; 
			}
			result.beginTransaction();
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] + content[index]; 
			}
			result.beginTransaction();
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] + content[index]; 
			}
			result.beginTransaction();
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (float) (source[index] + content[index]); 
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix add(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					return add((matrix instanceof DoubleComplexMatrix) ? ((DoubleComplexMatrix)matrix).content : matrix.extractDoubles());
				case COMPLEX_FLOAT 	:
					return add((matrix instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)matrix).content : matrix.extractFloats());
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG : case BIT :
					throw new IllegalArgumentException("Attempt to add real and complex matrix. Use cast() before");
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix addValue(final int value) {
		return addValue((float)value, 0f);
	}

	@Override
	public Matrix addValue(final long value) {
		return addValue((float)value, 0f);
	}

	@Override
	public Matrix addValue(final float value) {
		return addValue(value, 0f);
	}

	@Override
	public Matrix addValue(final float real, final float image) {
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
		final float[]				source = this.content;
		final float[]				target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] + ((index & 0x01) == 0 ? real : image); 
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix addValue(final double value) {
		return addValue((float)value, 0f);
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] - content[index]; 
			}
			result.beginTransaction();
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] - content[index]; 
			}
			result.beginTransaction();
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] - content[index]; 
			}
			result.beginTransaction();
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (float) (source[index] - content[index]); 
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix subtract(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					return subtract((matrix instanceof DoubleComplexMatrix) ? ((DoubleComplexMatrix)matrix).content : matrix.extractDoubles());
				case COMPLEX_FLOAT :
					return subtract((matrix instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)matrix).content : matrix.extractFloats());
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG : case BIT :
					throw new IllegalArgumentException("Attempt to subtract real and complex matrix. Use cast() before");
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix subtractValue(final int value) {
		return subtractValue((float)value, 0f);
	}

	@Override
	public Matrix subtractValue(final long value) {
		return subtractValue((float)value, 0f);
	}

	@Override
	public Matrix subtractValue(final float value) {
		return subtractValue(value, 0f);
	}

	@Override
	public Matrix subtractValue(final float real, final float image) {
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
		final float[]				source = this.content;
		final float[]				target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] - ((index & 0x01) == 0 ? real : image); 
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix subtractValue(final double value) {
		return subtractValue((float)value, 0f);
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] - source[index]; 
			}
			result.beginTransaction();
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] - source[index]; 
			}
			result.beginTransaction();
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] - source[index]; 
			}
			result.beginTransaction();
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
			final float[]				source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (float)(content[index] - source[index]); 
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix subtractFrom(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					return subtractFrom((matrix instanceof DoubleComplexMatrix) ? ((DoubleComplexMatrix)matrix).content : matrix.extractDoubles());
				case COMPLEX_FLOAT 	:
					return subtractFrom((matrix instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)matrix).content : matrix.extractFloats());
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG : case BIT :
					throw new IllegalArgumentException(); 
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix subtractFromValue(final int value) {
		return subtractFromValue((float)value, 0f);
	}

	@Override
	public Matrix subtractFromValue(final long value) {
		return subtractFromValue((float)value, 0f);
	}

	@Override
	public Matrix subtractFromValue(final float value) {
		return subtractFromValue(value, 0f);
	}

	@Override
	public Matrix subtractFromValue(final float real, final float image) {
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
		final float[]				source = this.content;
		final float[]				target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = ((index & 0x01) == 0 ? real : image) - source[index]; 
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix subtractFromValue(final double value) {
		return subtractFromValue((float)value, 0f);
	}

	@Override
	public Matrix subtractFromValue(final double real, final double image) {
		return subtractFromValue((float)real, (float)image);
	}

	@Override
	public Matrix mul(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else if (matrix.numberOfRows() != this.numberOfColumns()) {
			throw new IllegalArgumentException("Content number of rows ["+matrix.numberOfRows()+"] differ from current number of columns ["+this.numberOfColumns()+"]");
		}
		else {
			final FloatComplexMatrix	result = new FloatComplexMatrix(this.numberOfRows(), matrix.numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			final int					maxY = this.numberOfRows(), maxX = matrix.numberOfColumns();
			final int					colSize = this.numberOfColumns(), maxK = matrix.numberOfRows(); 
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					final double[]	tempD = (matrix instanceof DoubleComplexMatrix) ? ((DoubleComplexMatrix)matrix).content : matrix.extractDoubles();

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
					final float[]	tempF = (matrix instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)matrix).content : matrix.extractFloats();

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
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices. Use cast() before");
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix mulFrom(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else if (this.numberOfRows() != matrix.numberOfColumns()) {
			throw new IllegalArgumentException("Content number of columns ["+matrix.numberOfColumns()+"] differ from current number of rows ["+this.numberOfRows()+"]");
		}
		else {
			final FloatComplexMatrix	result = new FloatComplexMatrix(matrix.numberOfRows(), this.numberOfColumns());
			final float[]			source = this.content;
			final float[]			target = result.content;
			final int				maxY = matrix.numberOfRows(), maxX = this.numberOfColumns();
			final int				colSize = matrix.numberOfColumns(), maxK = this.numberOfRows(); 
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					final double[]	tempD = (matrix instanceof DoubleComplexMatrix) ? ((DoubleComplexMatrix)matrix).content : matrix.extractDoubles();

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
					final float[]	tempF = (matrix instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)matrix).content : matrix.extractFloats();

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
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices. Use cast() before");
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix mulValue(final int value) {
		return mulValue((float)value, 0f);
	}

	@Override
	public Matrix mulValue(final long value) {
		return mulValue((float)value, 0f);
	}
 
	@Override
	public Matrix mulValue(final float value) {
		return mulValue(value, 0f);
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
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix mulValue(final double value) {
		return mulValue((float)value, 0f);
	}

	@Override
	public Matrix mulValue(final double real, final double image) {
		return mulValue((float)real, (float)image);
	}

	@Override
	public Matrix divValue(final int value) {
		return divValue((float)value, 0f);
	}

	@Override
	public Matrix divValue(final long value) {
		return divValue((float)value, 0f);
	}

	@Override
	public Matrix divValue(final float value) {
		return divValue(value, 0f);
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
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix divValue(final double value) {
		return divValue((float)value, 0f);
	}

	@Override
	public Matrix divValue(final double real, final double image) {
		return divValue((float)real, (float)image);
	}

	@Override
	public Matrix divFromValue(final int value) {
		return divFromValue((float)value, 0f);
	}

	@Override
	public Matrix divFromValue(final long value) {
		return divFromValue((float)value, 0f);
	}

	@Override
	public Matrix divFromValue(final float value) {
		return divFromValue(value, 0f);
	}

	@Override
	public Matrix divFromValue(final float real, final float image) {
		final FloatComplexMatrix	result = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
		final float[]				source = this.content;
		final float[]				target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index += 2) {
			final float		quad = 1 / (source[index] * source[index] + source[index + 1] * source[index + 1]);
			
			target[index] = (real * source[index] + image * source[index + 1]) * quad; 
			target[index + 1] = (image * source[index] - real * source[index + 1]) * quad; 
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix divFromValue(final double value) {
		return divFromValue((float)value, 0f);
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
			result.beginTransaction();
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
			result.beginTransaction();
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
			result.beginTransaction();
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
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix mulHadamard(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					return mulHadamard((matrix instanceof DoubleComplexMatrix) ? ((DoubleComplexMatrix)matrix).content : matrix.extractDoubles());
				case COMPLEX_FLOAT 	:
					return mulHadamard((matrix instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)matrix).content : matrix.extractFloats());
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG : case BIT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrix. Use cast() before"); 
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
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
				final double	real = content[2 * index];
				final double	image = content[2 * index + 1];
				final double	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (float) ((source[2 * index] * real + source[2 * index + 1] * image) * quad);  
				target[2 * index + 1] =  (float) ((source[2 * index] * image - source[2 * index + 1] * real) * quad); 
			}
			result.beginTransaction();
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
				final double	real = content[2 * index];
				final double	image = content[2 * index + 1];
				final double	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (float) ((source[2 * index] * real + source[2 * index + 1] * image) * quad);  
				target[2 * index + 1] =  (float) ((source[2 * index] * image - source[2 * index + 1] * real) * quad); 
			}
			result.beginTransaction();
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
				final double	real = content[2 * index];
				final double	image = content[2 * index + 1];
				final double	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (float) ((source[2 * index] * real + source[2 * index + 1] * image) * quad);  
				target[2 * index + 1] =  (float) ((source[2 * index] * image - source[2 * index + 1] * real) * quad); 
			}
			result.beginTransaction();
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
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix mulInvHadamard(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					return mulInvHadamard((matrix instanceof DoubleComplexMatrix) ? ((DoubleComplexMatrix)matrix).content : matrix.extractDoubles());
				case COMPLEX_FLOAT 	:
					return mulInvHadamard((matrix instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)matrix).content : matrix.extractFloats());
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG : case BIT :
					throw new IllegalArgumentException("Attempt to multiply real ans complex matrix. Use cast() before");
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
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
				final double	real = source[2 * index];
				final double	image = source[2 * index + 1];
				final double	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (float) ((content[2 * index] * real + content[2 * index + 1] * image) * quad);  
				target[2 * index + 1] =  (float) ((content[2 * index] * image - content[2 * index + 1] * real) * quad); 
			}
			result.beginTransaction();
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
				final double	real = source[2 * index];
				final double	image = source[2 * index + 1];
				final double	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (float) ((content[2 * index] * real + content[2 * index + 1] * image) * quad);  
				target[2 * index + 1] =  (float) ((content[2 * index] * image - content[2 * index + 1] * real) * quad); 
			}
			result.beginTransaction();
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
				final double	real = source[2 * index];
				final double	image = source[2 * index + 1];
				final double	quad = 1 / (real * real + image * image);
				
				target[2 * index] = (float) ((content[2 * index] * real + content[2 * index + 1] * image) * quad);  
				target[2 * index + 1] =  (float) ((content[2 * index] * image - content[2 * index + 1] * real) * quad); 
			}
			result.beginTransaction();
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
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					return mulInvFromHadamard((matrix instanceof DoubleComplexMatrix) ? ((DoubleComplexMatrix)matrix).content : matrix.extractDoubles());
				case COMPLEX_FLOAT 	:
					return mulInvFromHadamard((matrix instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)matrix).content : matrix.extractFloats());
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG : case BIT :
					throw new IllegalArgumentException("Attempt to multiply real and complex matrix. Use cast() before"); 
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix tensorMul(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Matrix content can't be null");
		}
		else {
			final FloatComplexMatrix	result = new FloatComplexMatrix(this.numberOfRows() * matrix.numberOfRows(), this.numberOfColumns() * matrix.numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			final int 					maxY1 = this.numberOfRows(), maxY2 = matrix.numberOfRows();
			final int 					maxX1 = this.numberOfColumns(), maxX2 = matrix.numberOfColumns();
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					final double[]	tempD = (matrix instanceof DoubleComplexMatrix) ? ((DoubleComplexMatrix)matrix).content : matrix.extractDoubles();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	real = source[2 * (y1 * maxX1 + x1)];
							final double	image = source[2 * (y1 * maxX1 + x1) + 1];
							
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
					final float[]	tempF = (matrix instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)matrix).content : matrix.extractFloats();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	real = source[2 * (y1 * maxX1 + x1)];
							final double	image = source[2 * (y1 * maxX1 + x1) + 1];
							
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
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices. Use cast() before");
				default:
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix tensorMulFrom(final Matrix matrix) {
		if (matrix == null) {
			throw new NullPointerException("Matrix content can't be null");
		}
		else {
			final FloatComplexMatrix	result = new FloatComplexMatrix(this.numberOfRows() * matrix.numberOfRows(), this.numberOfColumns() * matrix.numberOfColumns());
			final float[]				source = this.content;
			final float[]				target = result.content;
			final int 					maxY1 = matrix.numberOfRows(), maxY2 = this.numberOfRows();
			final int 					maxX1 = matrix.numberOfColumns(), maxX2 = this.numberOfColumns();
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
					final double[]	tempD = (matrix instanceof DoubleComplexMatrix) ? ((DoubleComplexMatrix)matrix).content : matrix.extractDoubles();
					
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
					final float[]	tempF = (matrix instanceof FloatComplexMatrix) ? ((FloatComplexMatrix)matrix).content : matrix.extractFloats();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	real = tempF[2 * (y1 * maxX1 + x1)];
							final double	image = tempF[2 * (y1 * maxX1 + x1) + 1];
							
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
					throw new UnsupportedOperationException("Attempt to multiply real and complex matrices. Use cast() before");
				default:
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
			result.beginTransaction();
			return result;
		}
	}
	
	@Override
	public Matrix invert() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Only square matrix can be inverted");
		}
		else {
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
					final double	real2 = source[2 * (i * colSize + y)];
					final double	image2 = source[2 * (i * colSize + y) + 1];
					
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
					final double	real2 = source[2 * (i * colSize + y)];
					final double	image2 = source[2 * (i * colSize + y) + 1];
					
					for(int x = 0; x < colSize; x++) {
						source[2 * (i * colSize + x)] -= real2 * source[2 * (y * colSize + x)] - image2 * source[2 * (y * colSize + x) + 1];
						source[2 * (i * colSize + x) + 1] -= real2 * source[2 * (y * colSize + x) + 1] + image2 * source[2 * (y * colSize + x)];
						identity[2 * (i * colSize + x)] -= real2 * identity[2 * (y * colSize + x)] - image2 * identity[2 * (y * colSize + x) + 1];
						identity[2 * (i * colSize + x) + 1] -= real2 * identity[2 * (y * colSize + x) + 1] + image2 * identity[2 * (y * colSize + x)];
					}
				}
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix transpose() {
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
		result.beginTransaction();
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
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
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
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	source = this.content.clone();
			final int		colSize = numberOfColumns();
			double			detReal = 1, detImage = 0;

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
					final double	real2 = source[2 * (i * colSize + y)];
					final double	image2 = source[2 * (i * colSize + y) + 1];
					
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
		final float[]	source = this.content;
		final int		colSize = numberOfColumns();
		double	real = 0, image = 0;
		
		areAllAsyncCompleted();
		for(int index = 0; index < colSize; index++) {	// Calculate diagonal sum
			real += source[2 * (index * (colSize + 1))];
			image += source[2 * (index * (colSize + 1)) + 1];
		}
		return new Number[] {real, image};
	}
	
	@Override
	public Matrix done() {
		completeTransaction();
		return this;
	}

	@Override
	public Matrix apply2(final Piece piece, final ApplyFloat2 callback) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final float[]	source = this.content;
			final float[]	temp = new float[2];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					temp[0] = source[2 * ((y0 + y)*cols + (x0 + x))];
					temp[1] = source[2 * ((y0 + y)*cols + (x0 + x)) + 1];
					callback.apply(y0 + y, x0 + x, temp);
					source[2 * ((y0 + y)*cols + (x0 + x))] = (float)temp[0];
					source[2 * ((y0 + y)*cols + (x0 + x)) + 1] = (float)temp[1];
				}
			}
			return this;
		}
	}
	
	@Override
	protected void lastCall() {
	}
	
	private Matrix aggregateAvg(final AggregateDirection dir) {
		final FloatComplexMatrix	result;
		final float[]		source = this.content;
		final float[]		target;
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
		result.beginTransaction();
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
		result.beginTransaction();
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
		result.beginTransaction();
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
		result.beginTransaction();
		return result;
	}
}
