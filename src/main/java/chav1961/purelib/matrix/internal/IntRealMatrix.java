package chav1961.purelib.matrix.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import chav1961.purelib.matrix.AbstractMatrix;
import chav1961.purelib.matrix.interfaces.Matrix;

public class IntRealMatrix extends AbstractMatrix {
	final int[]	content;

	public IntRealMatrix(final int rows, final int columns) {
		super(Type.REAL_INT, rows, columns);
		this.content = new int[rows * columns];
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
		
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
			return Arrays.equals(content, another.extractInts());
		}
	}

	@Override
	public int[] extractInts() {
		if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			return content.clone();
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
			final int[]	source = this.content;
			final int[]		result = new int[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (int)source[((y0 + y)*cols + (x0 + x))];
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
			final int[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					dataOutput.writeInt((int)source[((y0 + y)*cols + (x0 + x))]);
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
			final int[]	source = this.content;
			final long[]	result = new long[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (long)source[((y0 + y)*cols + (x0 + x))];
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
			final int[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					dataOutput.writeLong((long)source[((y0 + y)*cols + (x0 + x))]);
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
			final int[]	source = this.content;
			final float[]	result = new float[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (float) source[((y0 + y)*cols + (x0 + x))];
				}
			}
			return result;
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
			final int[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					dataOutput.writeFloat((float) source[((y0 + y)*cols + (x0 + x))]);
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
			final int[]	source = this.content;
			final double[]	result = new double[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[where++] = (double)source[((y0 + y)*cols + (x0 + x))];
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
			final int[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					dataOutput.writeDouble((double)source[((y0 + y)*cols + (x0 + x))]);
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
			final int[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[((y0 + y)*cols + (x0 + x))] = content[where++];
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
			final int[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[((y0 + y)*cols + (x0 + x))] = (int) content[where++];
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final int... content) {
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
			final int[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[((y0 + y)*cols + (x0 + x))] = (int) content[where++];
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
			final int[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[((y0 + y)*cols + (x0 + x))] = (int)content[where++];
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final Piece piece, final Matrix content) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (isOverlaps(piece)) {
			throw overlapsError(piece);
		}
		else if (content == null) {
			throw new NullPointerException("Content matrix can't be null");
		}
		else {
			return assign(piece, content.extractFloats());
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
			final int[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					try {
						switch (type) {
							case BIT			:
								break;
							case COMPLEX_DOUBLE	:
							case REAL_DOUBLE	:
								result[((y0 + y)*cols + (x0 + x))] = (int)content.readDouble();
								break;
							case COMPLEX_FLOAT	:
							case REAL_FLOAT		:
								result[((y0 + y)*cols + (x0 + x))] = (int)content.readFloat();
								break;
							case REAL_INT		:
								result[((y0 + y)*cols + (x0 + x))] = (int)content.readInt();
								break;
							case REAL_LONG		:
								result[((y0 + y)*cols + (x0 + x))] = (int)content.readLong();
								break;
							default:
								break;
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
		return fill(piece, (float)value);
	}

	@Override
	public Matrix fill(final Piece piece, final long value) {
		return fill(piece, (float)value);
	}

	@Override
	public Matrix fill(final Piece piece, final float value) {
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
			final int[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), col = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[((y0 + y)*col + (x0 + x))] = (int) value;
				}
			}
			return this;
		}
	}

	@Override
	public Matrix fill(final Piece piece, final float real, final float image) {
		throw new UnsupportedOperationException("Complex filling is not supported for real matrices");
	}

	@Override
	public Matrix fill(final Piece piece, final double value) {
		return fill(piece, (float)value);
	}

	@Override
	public Matrix fill(final Piece piece, final double real, double image) {
		throw new UnsupportedOperationException("Complex filling is not supported for real matrices");
	}

	@Override
	public Matrix cast(final Type type) {
		if (type == null) {
			throw new NullPointerException("Cast type can't be null");
		}
		else {
			final int[]				sourceI = this.content;
			
			switch (type) {
				case COMPLEX_DOUBLE	:
					final DoubleComplexMatrix	dcm = new DoubleComplexMatrix(numberOfRows(), numberOfColumns());
					final double[]				targetCD = dcm.content;
					
					for(int index = 0, maxIndex = sourceI.length; index < maxIndex; index++) {
						targetCD[2 * index] = sourceI[index];
						targetCD[2 * index + 1] = 0;
					}
					return dcm;
				case COMPLEX_FLOAT	:
					final FloatComplexMatrix	fcm = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
					final float[]				targetCF = fcm.content;
					
					for(int index = 0, maxIndex = sourceI.length; index < maxIndex; index++) {
						targetCF[2 * index] = sourceI[index];
						targetCF[2 * index + 1] = 0;
					}
					return fcm;
				case REAL_DOUBLE	:
					final DoubleRealMatrix	drm = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
					final double[]			targetD = drm.content;
					
					for(int index = 0, maxIndex = targetD.length; index < maxIndex; index++) {
						targetD[index] = sourceI[index];
					}
					return drm;
				case REAL_FLOAT		:
					final FloatRealMatrix	frm = new FloatRealMatrix(numberOfRows(), numberOfColumns());
					final float[]			targetF = frm.content;
					
					for(int index = 0, maxIndex = targetF.length; index < maxIndex; index++) {
						targetF[index] = sourceI[index];
					}
					return frm;
				case REAL_INT		:
					try {
						return (Matrix) this.clone();
					} catch (CloneNotSupportedException e) {
						return this;
					}
				case REAL_LONG		:
					final LongRealMatrix	lrm = new LongRealMatrix(numberOfRows(), numberOfColumns());
					final long[]			targetI = lrm.content;
					
					for(int index = 0, maxIndex = targetI.length; index < maxIndex; index++) {
						targetI[index] = sourceI[index];
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			final int[]			source = this.content;
			
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] += content[index]; 
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] += content[index]; 
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] += content[index]; 
			}
			result.beginTransaction();
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
		final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
		final int[]			source = this.content;
		final int[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = (int) (source[index] + value); 
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix addValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex adding is not supported for real matrices");
	}

	@Override
	public Matrix addValue(final double value) {
		return addValue((float)value);
	}

	@Override
	public Matrix addValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex adding is not supported for real matrices");
	}

	@Override
	public Matrix subtract(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] -= content[index]; 
			}
			result.beginTransaction();
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
		final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
		final int[]			source = this.content;
		final int[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = (int) (source[index] - value); 
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix subtractValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex subtracting is not supported for real matrices");
	}

	@Override
	public Matrix subtractValue(final double value) {
		return subtractValue((float)value);
	}

	@Override
	public Matrix subtractValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex subtracting is not supported for real matrices");
	}

	@Override
	public Matrix subtractFrom(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else {
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] - target[index]; 
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (content[index] - target[index]); 
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (content[index] - target[index]); 
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			target = result.content;
			
			System.arraycopy(this.content, 0, target, 0, target.length);
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (content[index] - target[index]); 
			}
			result.beginTransaction();
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
		final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
		final int[]			source = this.content;
		final int[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = (int) (value - source[index]); 
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix subtractFromValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex subtracting is not supported for real matrices");
	}

	@Override
	public Matrix subtractFromValue(final double value) {
		return subtractFromValue((float)value);
	}

	@Override
	public Matrix subtractFromValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex subtracting is not supported for real matrices");
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
			final IntRealMatrix	result = new IntRealMatrix(this.numberOfRows(), content.numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			final int			maxY = this.numberOfRows(), maxX = content.numberOfColumns();
			final int			colSize = this.numberOfColumns(), maxK = content.numberOfRows(); 
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : 
				case COMPLEX_FLOAT 	:
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG :
					final int[]	tempI = content.extractInts();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							int	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += source[(y * colSize + k)] * tempI[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
			result.beginTransaction();
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
			final IntRealMatrix	result = new IntRealMatrix(content.numberOfRows(), this.numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			final int			maxY = content.numberOfRows(), maxX = this.numberOfColumns();
			final int			colSize = content.numberOfColumns(), maxK = this.numberOfRows(); 
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : 
				case COMPLEX_FLOAT 	:
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG :
					final int[]	tempI = content.extractInts();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							int	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += tempI[(y * colSize + k)] * source[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
				default : 
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix mulValue(final int value) {
		final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
		final int[]			source = this.content;
		final int[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = (int) (source[index] * value); 
		}
		result.beginTransaction();
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
		throw new UnsupportedOperationException("Complex multiplication is not supported for real matrices");
	}

	@Override
	public Matrix mulValue(final double value) {
		return mulValue((int)value);
	}

	@Override
	public Matrix mulValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex multiplication is not supported for real matrices");
	}

	@Override
	public Matrix divValue(final int value) {
		final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
		final int[]			source = this.content;
		final int[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] / value; 
		}
		result.beginTransaction();
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
		throw new UnsupportedOperationException("Complex dividion is not supported for real matrices");
	}

	@Override
	public Matrix divValue(final double value) {
		return divValue((int)value);
	}

	@Override
	public Matrix divValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex dividion is not supported for real matrices");
	}

	@Override
	public Matrix divFromValue(final int value) {
		final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
		final int[]			source = this.content;
		final int[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = value / source[index]; 
		}
		result.beginTransaction();
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
		throw new UnsupportedOperationException("Complex dividion is not supported for real matrices");
	}

	@Override
	public Matrix divFromValue(final double value) {
		return divFromValue((int)value);
	}

	@Override
	public Matrix divFromValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex dividion is not supported for real matrices");
	}

	@Override
	public Matrix mulHadamard(final int... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else {
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] * content[index]; 
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (source[index] * content[index]); 
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (source[index] * content[index]); 
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (source[index] * content[index]); 
			}
			result.beginTransaction();
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] / content[index];  
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (source[index] / content[index]);  
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (source[index] / content[index]);  
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (source[index] / content[index]);  
			}
			result.beginTransaction();
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] / source[index];  
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (content[index] / source[index]);  
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (content[index] / source[index]);  
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
			final IntRealMatrix	result = new IntRealMatrix(numberOfRows(), numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = (int) (content[index] / source[index]);  
			}
			result.beginTransaction();
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
			final IntRealMatrix	result = new IntRealMatrix(this.numberOfRows() * content.numberOfRows(), this.numberOfColumns() * content.numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			final int 			maxY1 = this.numberOfRows(), maxY2 = content.numberOfRows();
			final int 			maxX1 = this.numberOfColumns(), maxX2 = content.numberOfColumns();
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : 
				case COMPLEX_FLOAT 	:
					throw new UnsupportedOperationException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG :
					final int[]	tempI = content.extractInts();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final int	value = source[(y1 * maxX1 + x1)];
							
							if (value != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = value * tempI[sourceIndex];
									}
								}
							}
						}
					}
					break;
				default:
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix tensorMulFrom(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Matrix content can't be null");
		}
		else {
			final IntRealMatrix	result = new IntRealMatrix(this.numberOfRows() * content.numberOfRows(), this.numberOfColumns() * content.numberOfColumns());
			final int[]			source = this.content;
			final int[]			target = result.content;
			final int 			maxY1 = content.numberOfRows(), maxY2 = this.numberOfRows();
			final int 			maxX1 = content.numberOfColumns(), maxX2 = this.numberOfColumns();
			
			switch (content.getType()) {
				case COMPLEX_DOUBLE : 
				case COMPLEX_FLOAT 	:
					throw new UnsupportedOperationException("Attempt to multiply real and complex matrices");
				case REAL_DOUBLE : case REAL_FLOAT : case REAL_INT : case REAL_LONG :
					final int[]	tempI = content.extractInts();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final int	value = tempI[(y1 * maxX1 + x1)];
							
							if (value != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = value * source[sourceIndex];
									}
								}
							}
						}
					}
					break;
				default:
					throw new UnsupportedOperationException("Matrix type ["+content.getType()+"] is not supported yet");
			}
			result.beginTransaction();
			return result;
		}
	}
	
	@Override
	public Matrix invert() {
		throw new UnsupportedOperationException("Matrix inversion doesn't support for long matricees");
	}

	@Override
	public Matrix transpose() {
		final IntRealMatrix	result = new IntRealMatrix(numberOfColumns(), numberOfRows());
		final int[]			source = this.content;
		final int[]			target = result.content;
		final int				rows = numberOfRows(), cols = numberOfColumns();  
		
		for(int y = 0; y < rows; y++) {
			for(int x = 0; x < cols; x++) {
				target[(x*rows + y)] = source[(y*cols + x)]; 
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
					throw new UnsupportedOperationException("Aggregation type ["+aggType+"] is not supported for integer matrices");
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
		throw new UnsupportedOperationException("Attempt to get determinant for integer matrix");
	}

	@Override
	public Number track() {
		final int[]	source = this.content;
		final int	colSize = numberOfColumns();
		int sum = 0;
		
		areAllAsyncCompleted();
		for(int index = 0; index < colSize; index++) {	// Calculate diagonal sum
			sum += source[(index * (colSize + 1))];
		}
		return sum;
	}
	
	@Override
	public Number[] det2() {
		throw new UnsupportedOperationException("Attempt to get complex determinant for real matrix");
	}

	@Override
	public Number[] track2() {
		throw new UnsupportedOperationException("Attempt to get complex track for real matrix");
	}
	
	@Override
	public Matrix done() {
		completeTransaction();
		return this;
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyInt callback) {
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
			final int[]	source = this.content;
			final int	x0 = piece.getLeft(), y0 = piece.getTop();
			final int	maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					source[((y0 + y)*cols + (x0 + x))] = callback.apply(y0 + y, x0 + x, source[((y0 + y)*cols + (x0 + x))]); 
				}
			}
			return this;
		}
	}

	@Override
	protected void lastCall() {
	}
	
	private Matrix aggregateMax(final AggregateDirection dir) {
		final IntRealMatrix	result;
		final int[]			source = this.content;
		final int[]			target;
		final int			cols = numberOfColumns();
		int	val;
		
		switch (dir) {
			case ByColumns	:
				result = new IntRealMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					val = source[(y * cols + 0)]; 
					
					for(int x = 0; x < cols; x++) {
						final int	current = source[(y * cols + x)]; 
						
						if (current > val) {
							val = current;
						}
					}
					target[y] = val;
				}
				break;
			case ByRows		:
				result = new IntRealMatrix(1, cols); 
				target = result.content;
				
				for(int x = 0; x < cols; x++) {
					val = source[(x * cols + 0)]; 
					
					for(int y = 0; y < numberOfRows(); y++) {
						final int	current = source[(y * cols + x)]; 
						
						if (current > val) {
							val = current;
						}
					}
					target[x] = val;
				}
				break;
			case Total		:
				result = new IntRealMatrix(1, 1); 
				target = result.content;
				
				val = source[0]; 
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < cols; x++) {
						final int	current = source[(y * cols + x)]; 
						
						if (current > val) {
							val = current;
						}
					}
				}
				target[0] = val;
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.beginTransaction();
		return result;
	}

	private Matrix aggregateMin(final AggregateDirection dir) {
		final IntRealMatrix	result;
		final int[]			source = this.content;
		final int[]			target;
		final int			cols = numberOfColumns();
		int	val;
		
		switch (dir) {
			case ByColumns	:
				result = new IntRealMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					val = source[(y * cols + 0)]; 
					
					for(int x = 0; x < cols; x++) {
						final int	current = source[(y * cols + x)]; 
						
						if (current < val) {
							val = current;
						}
					}
					target[y] = val;
				}
				break;
			case ByRows		:
				result = new IntRealMatrix(1, cols); 
				target = result.content;
				
				for(int x = 0; x < cols; x++) {
					val = source[(x * cols + 0)]; 
					
					for(int y = 0; y < numberOfRows(); y++) {
						final int	current = source[(y * cols + x)]; 
						
						if (current < val) {
							val = current;
						}
					}
					target[x] = val;
				}
				break;
			case Total		:
				result = new IntRealMatrix(1, 1); 
				target = result.content;
				
				val = source[0]; 
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < cols; x++) {
						final int	current = source[(y * cols + x)]; 
						
						if (current < val) {
							val = current;
						}
					}
				}
				target[0] = val;
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.beginTransaction();
		return result;
	}

	private Matrix aggregateSum(final AggregateDirection dir) {
		final IntRealMatrix	result;
		final int[]			source = this.content;
		final int[]			target;
		final int			cols = numberOfColumns();
		int	sum;
		
		switch (dir) {
			case ByColumns	:
				result = new IntRealMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					sum = 0;
					for(int x = 0; x < cols; x++) {
						sum += source[(y * cols + x)];
					}
					target[y] = sum;
				}
				break;
			case ByRows		:
				result = new IntRealMatrix(1, cols); 
				target = result.content;
				
				for(int x = 0; x < cols; x++) {
					sum = 0;
					for(int y = 0; y < numberOfRows(); y++) {
						sum += source[(y * cols + x)];
					}
					target[x] = sum;
				}
				break;
			case Total		:
				result = new IntRealMatrix(1, 1); 
				target = result.content;
				
				sum = 0;
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < cols; x++) {
						sum += source[(y * cols + x)];
					}
				}
				target[0] = sum;
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.beginTransaction();
		return result;
	}
}
