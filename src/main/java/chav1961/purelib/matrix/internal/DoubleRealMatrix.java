package chav1961.purelib.matrix.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.matrix.AbstractMatrix;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.MatrixCalc;

public class DoubleRealMatrix extends AbstractMatrix {
	final double[]	content;

	public DoubleRealMatrix(final int rows, final int columns) {
		super(Type.REAL_DOUBLE, rows, columns);
		this.content = new double[rows * columns];
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
		
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
			return Arrays.equals(content, (another instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)another).content : another.extractDoubles());
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
			final double[]	source = this.content;
			final int[]		result = new int[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
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
			final double[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
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
			final double[]	source = this.content;
			final long[]	result = new long[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
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
			final double[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
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
			final double[]	source = this.content;
			final float[]	result = new float[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
					result[where++] = (float) source[index++];
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
			final double[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
					dataOutput.writeFloat((float) source[index++]);
				}
			}
		}
	}

	@Override
	public double[] extractDoubles() {
		if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			return content.clone();
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
			final double[]	source = this.content;
			final double[]	result = new double[piece.getWidth() * piece.getHeight()];
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
					result[where++] = source[index++];
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
			final double[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
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
			final double[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
					result[index++] = content[where++];
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
			final double[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
					result[index++] = content[where++];
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
			final double[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
					result[index++] = content[where++];
				}
			}
			return this;
		}
	}

	@Override
	public Matrix assign(final double... content) {
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
			final double[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			int				where = 0;
			
			for(int y = 0; y < maxY; y++) {
				int index = ((y0 + y)*cols + x0);
				
				for(int x = 0; x < maxX; x++) {
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
				case COMPLEX_DOUBLE : case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to assign real matrix with complex matrix. Use cast() before");
				case REAL_DOUBLE	:
					assign(piece, (matrix instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)matrix).content : matrix.extractDoubles());
					break;
				case REAL_FLOAT		:
					assign(piece, (matrix instanceof FloatRealMatrix) ? ((FloatRealMatrix)matrix).content : matrix.extractFloats());
					break;
				case REAL_INT		:
					assign(piece, (matrix instanceof IntRealMatrix) ? ((IntRealMatrix)matrix).content : matrix.extractInts());
					break;
				case REAL_LONG		:
					assign(piece, (matrix instanceof LongRealMatrix) ? ((LongRealMatrix)matrix).content : matrix.extractLongs());
					break;
				case BIT 			:
					assign(matrix.extractInts());
					break;
				default :
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
			return this;
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
			final double[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					try {
						switch (type) {
							case BIT			:
								result[((y0 + y)*cols + (x0 + x))] = content.readBoolean() ? 1 : 0;
								break;
							case COMPLEX_DOUBLE	:
							case REAL_DOUBLE	:
								result[((y0 + y)*cols + (x0 + x))] = (float)content.readDouble();
								break;
							case COMPLEX_FLOAT	:
							case REAL_FLOAT		:
								result[((y0 + y)*cols + (x0 + x))] = content.readFloat();
								break;
							case REAL_INT		:
								result[((y0 + y)*cols + (x0 + x))] = content.readInt();
								break;
							case REAL_LONG		:
								result[((y0 + y)*cols + (x0 + x))] = content.readLong();
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
		return fill(piece, (double)value);
	}

	@Override
	public Matrix fill(final Piece piece, final float real, final float image) {
		throw new UnsupportedOperationException("Complex filling is not supported for real matrices");
	}

	@Override
	public Matrix fill(final Piece piece, final double value) {
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
			final double[]	result = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), col = numberOfColumns();
			
			for(int y = 0; y < maxY; y++) {
				for(int x = 0; x < maxX; x++) {
					result[((y0 + y)*col + (x0 + x))] = value;
				}
			}
			return this;
		}
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
			final double[]	sourceD = this.content;
			
			switch (type) {
				case COMPLEX_DOUBLE	:
					final DoubleComplexMatrix	dcm = new DoubleComplexMatrix(numberOfRows(), numberOfColumns());
					final double[]				targetCD = dcm.content;
					
					for(int index = 0, maxIndex = sourceD.length; index < maxIndex; index++) {
						targetCD[2 * index] = sourceD[index];
						targetCD[2 * index + 1] = 0;
					}
					return dcm;
				case COMPLEX_FLOAT	:
					final FloatComplexMatrix	fcm = new FloatComplexMatrix(numberOfRows(), numberOfColumns());
					final float[]				targetCF = fcm.content;
					
					for(int index = 0, maxIndex = sourceD.length; index < maxIndex; index++) {
						targetCF[2 * index] = (float)sourceD[index];
						targetCF[2 * index + 1] = 0;
					}
					return fcm;
				case REAL_DOUBLE	:
					try {
						return (Matrix) this.clone();
					} catch (CloneNotSupportedException e) {
						return this;
					}
				case REAL_FLOAT		:
					final FloatRealMatrix	frm = new FloatRealMatrix(numberOfRows(), numberOfColumns());
					final float[]			targetF = frm.content;
					
					for(int index = 0, maxIndex = targetF.length; index < maxIndex; index++) {
						targetF[index] = (float)sourceD[index];
					}
					return frm;
				case REAL_INT		:
					final IntRealMatrix	irm = new IntRealMatrix(numberOfRows(), numberOfColumns());
					final int[]			targetI = irm.content;
					
					for(int index = 0, maxIndex = targetI.length; index < maxIndex; index++) {
						targetI[index] = (int)sourceD[index];
					}
					return irm;
				case REAL_LONG		:
					final LongRealMatrix	lrm = new LongRealMatrix(numberOfRows(), numberOfColumns());
					final long[]			targetL = lrm.content;
					
					for(int index = 0, maxIndex = targetL.length; index < maxIndex; index++) {
						targetL[index] = (long)sourceD[index];
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] + content[index]; 
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
				case COMPLEX_FLOAT 	:
					throw new IllegalArgumentException("Attempt to add real and complex matrix. Use cast() before");
				case REAL_DOUBLE	:
					return add((matrix instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)matrix).content : matrix.extractDoubles());
				case REAL_FLOAT		:
					return add((matrix instanceof FloatRealMatrix) ? ((FloatRealMatrix)matrix).content : matrix.extractFloats());
				case REAL_INT		:
					return add((matrix instanceof IntRealMatrix) ? ((IntRealMatrix)matrix).content : matrix.extractInts());
				case REAL_LONG		:
					return add((matrix instanceof LongRealMatrix) ? ((LongRealMatrix)matrix).content : matrix.extractLongs());
				case BIT			:
					return add(matrix.extractInts());
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix addValue(final int value) {
		return addValue((double)value);
	}

	@Override
	public Matrix addValue(final long value) {
		return addValue((double)value);
	}

	@Override
	public Matrix addValue(final float value) {
		return addValue((double)value);
	}

	@Override
	public Matrix addValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex adding is not supported for real matrices");
	}

	@Override
	public Matrix addValue(final double value) {
		final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
		final double[]			source = this.content;
		final double[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] + value; 
		}
		result.beginTransaction();
		return result;
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] - content[index]; 
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
				case COMPLEX_FLOAT :
					throw new IllegalArgumentException("Attempt to subtract real and complex matrix. Use cast(0 before");
				case REAL_DOUBLE	:
					return subtract((matrix instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)matrix).content : matrix.extractDoubles());
				case REAL_FLOAT		:
					return subtract((matrix instanceof FloatRealMatrix) ? ((FloatRealMatrix)matrix).content : matrix.extractFloats());
				case REAL_INT		:
					return subtract((matrix instanceof IntRealMatrix) ? ((IntRealMatrix)matrix).content : matrix.extractInts());
				case REAL_LONG		:
					return subtract((matrix instanceof LongRealMatrix) ? ((LongRealMatrix)matrix).content : matrix.extractLongs());
				case BIT			:
					return subtract(matrix.extractInts());
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix subtractValue(final int value) {
		return subtractValue((double)value);
	}

	@Override
	public Matrix subtractValue(final long value) {
		return subtractValue((double)value);
	}

	@Override
	public Matrix subtractValue(final float value) {
		return subtractValue((double)value);
	}

	@Override
	public Matrix subtractValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex subtracting is not supported for real matrices");
	}

	@Override
	public Matrix subtractValue(final double value) {
		final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
		final double[]			source = this.content;
		final double[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] - value; 
		}
		result.beginTransaction();
		return result;
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			target = result.content;
			final double[]			source = this.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] - source[index]; 
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
				case COMPLEX_FLOAT 	:
					throw new IllegalArgumentException("Attempt to subtract real and complex matrix. Use cast() before");
				case REAL_DOUBLE	:
					return subtractFrom((matrix instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)matrix).content : matrix.extractDoubles());
				case REAL_FLOAT		:
					return subtractFrom((matrix instanceof FloatRealMatrix) ? ((FloatRealMatrix)matrix).content : matrix.extractFloats());
				case REAL_INT		:
					return subtractFrom((matrix instanceof IntRealMatrix) ? ((IntRealMatrix)matrix).content : matrix.extractInts());
				case REAL_LONG		:
					return subtractFrom((matrix instanceof LongRealMatrix) ? ((LongRealMatrix)matrix).content : matrix.extractLongs());
				case BIT :
					return subtractFrom(matrix.extractInts());
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix subtractFromValue(final int value) {
		return subtractFromValue((double)value);
	}

	@Override
	public Matrix subtractFromValue(final long value) {
		return subtractFromValue((double)value);
	}

	@Override
	public Matrix subtractFromValue(final float value) {
		return subtractFromValue((double)value);
	}

	@Override
	public Matrix subtractFromValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex subtracting is not supported for real matrices");
	}

	@Override
	public Matrix subtractFromValue(final double value) {
		final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
		final double[]			source = this.content;
		final double[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = value - source[index]; 
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix subtractFromValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex subtracting is not supported for real matrices");
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(this.numberOfRows(), matrix.numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			final int				maxY = this.numberOfRows(), maxX = matrix.numberOfColumns();
			final int				colSize = this.numberOfColumns(), maxK = matrix.numberOfRows(); 
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
				case COMPLEX_FLOAT 	:
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices. Use cast() before");
				case REAL_DOUBLE : 
					final double[]	tempD = (matrix instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)matrix).content : matrix.extractDoubles();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							double	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += source[(y * colSize + k)] * tempD[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
				case REAL_FLOAT : 
					final float[]	tempF = (matrix instanceof FloatRealMatrix) ? ((FloatRealMatrix)matrix).content : matrix.extractFloats();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							double	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += source[(y * colSize + k)] * tempF[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
				case REAL_INT : 
					final int[]		tempI = (matrix instanceof IntRealMatrix) ? ((IntRealMatrix)matrix).content : matrix.extractInts();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							double	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += source[(y * colSize + k)] * tempI[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
				case REAL_LONG :
					final long[]	tempL = (matrix instanceof LongRealMatrix) ? ((LongRealMatrix)matrix).content : matrix.extractLongs();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							double	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += source[(y * colSize + k)] * tempL[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
				case BIT	:
					final int[]		tempB = matrix.extractInts();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							double	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += source[(y * colSize + k)] * tempB[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(matrix.numberOfRows(), this.numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			final int				maxY = matrix.numberOfRows(), maxX = this.numberOfColumns();
			final int				colSize = matrix.numberOfColumns(), maxK = this.numberOfRows(); 
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
				case COMPLEX_FLOAT 	:
					throw new IllegalArgumentException("Attempt to multiply real and complex matrices. Use cast() before");
				case REAL_DOUBLE 	: 
					final double[]	tempD = (matrix instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)matrix).content : matrix.extractDoubles();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							double	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += tempD[(y * colSize + k)] * source[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
				case REAL_FLOAT 	: 
					final float[]	tempF = (matrix instanceof FloatRealMatrix) ? ((FloatRealMatrix)matrix).content : matrix.extractFloats();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							double	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += tempF[(y * colSize + k)] * source[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
				case REAL_INT 		: 
					final int[]		tempI = (matrix instanceof IntRealMatrix) ? ((IntRealMatrix)matrix).content : matrix.extractInts();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							double	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += tempI[(y * colSize + k)] * source[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
				case REAL_LONG 		:
					final long[]	tempL = (matrix instanceof LongRealMatrix) ? ((LongRealMatrix)matrix).content : matrix.extractLongs();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							double	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += tempL[(y * colSize + k)] * source[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
				case BIT	:
					final int[]		tempB = matrix.extractInts();

					for(int y = 0; y < maxY; y++) {
						for(int x = 0; x < maxX; x++) {
							double	sum = 0;
							
							for(int k = 0; k < maxK; k++) {
								sum += tempB[(y * colSize + k)] * source[(k * maxX + x)];
							}
							target[(y * maxX + x)] = sum;
						}
					}
					break;
				default : 
					throw new UnsupportedOperationException("Matrix type ["+matrix.getType()+"] is not supported yet");
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix mulValue(final int value) {
		return mulValue((double)value);
	}

	@Override
	public Matrix mulValue(final long value) {
		return mulValue((double)value);
	}
 
	@Override
	public Matrix mulValue(final float value) {
		return mulValue((double)value);
	}

	@Override
	public Matrix mulValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex multiplication is not supported for real matrices");
	}

	@Override
	public Matrix mulValue(final double value) {
		final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
		final double[]			source = this.content;
		final double[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] * value; 
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix mulValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex multiplication is not supported for real matrices");
	}

	@Override
	public Matrix divValue(final int value) {
		return divValue((double)value);
	}

	@Override
	public Matrix divValue(final long value) {
		return divValue((double)value);
	}

	@Override
	public Matrix divValue(final float value) {
		return divValue((double)value);
	}

	@Override
	public Matrix divValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex dividion is not supported for real matrices");
	}

	@Override
	public Matrix divValue(final double value) {
		final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
		final double[]			source = this.content;
		final double[]			target = result.content;
		final double			invValue = 1 / value;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = source[index] * invValue; 
		}
		result.beginTransaction();
		return result;
	}

	@Override
	public Matrix divValue(final double real, final double image) {
		throw new UnsupportedOperationException("Complex dividion is not supported for real matrices");
	}

	@Override
	public Matrix divFromValue(final int value) {
		return divFromValue((double)value);
	}

	@Override
	public Matrix divFromValue(final long value) {
		return divFromValue((double)value);
	}

	@Override
	public Matrix divFromValue(final float value) {
		return divFromValue((double)value);
	}

	@Override
	public Matrix divFromValue(final float real, final float image) {
		throw new UnsupportedOperationException("Complex dividion is not supported for real matrices");
	}

	@Override
	public Matrix divFromValue(final double value) {
		final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
		final double[]			source = this.content;
		final double[]			target = result.content;
		
		for(int index = 0, maxIndex = target.length; index < maxIndex; index++) {
			target[index] = value / source[index]; 
		}
		result.beginTransaction();
		return result;
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] * content[index]; 
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] * content[index]; 
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] * content[index]; 
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
				case COMPLEX_FLOAT 	:
					throw new UnsupportedOperationException("Attempt to multiply real and compex matrix. Use cast() before");
				case REAL_DOUBLE	:
					return mulHadamard((matrix instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)matrix).content : matrix.extractDoubles());
				case REAL_FLOAT		:
					return mulHadamard((matrix instanceof FloatRealMatrix) ? ((FloatRealMatrix)matrix).content : matrix.extractFloats());
				case REAL_INT		:
					return mulHadamard((matrix instanceof IntRealMatrix) ? ((IntRealMatrix)matrix).content : matrix.extractInts());
				case REAL_LONG		:
					return mulHadamard((matrix instanceof LongRealMatrix) ? ((LongRealMatrix)matrix).content : matrix.extractLongs());
				case BIT 	:
					return mulHadamard(matrix.extractInts());
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] / content[index];  
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] / content[index];  
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = source[index] / content[index];  
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
				case COMPLEX_FLOAT 	:
					throw new UnsupportedOperationException("Attempt to multiply real and compex matrix. Use cast() before");
				case REAL_DOUBLE	:
					return mulInvHadamard((matrix instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)matrix).content : matrix.extractDoubles());
				case REAL_FLOAT		:
					return mulInvHadamard((matrix instanceof FloatRealMatrix) ? ((FloatRealMatrix)matrix).content : matrix.extractFloats());
				case REAL_INT		:
					return mulInvHadamard((matrix instanceof IntRealMatrix) ? ((IntRealMatrix)matrix).content : matrix.extractInts());
				case REAL_LONG		:
					return mulInvHadamard((matrix instanceof LongRealMatrix) ? ((LongRealMatrix)matrix).content : matrix.extractLongs());
				case BIT :
					return mulInvHadamard(matrix.extractInts());
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] / source[index];  
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] / source[index];  
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfRows(), numberOfColumns());
			final double[]			source = this.content;
			final double[]			target = result.content;
			
			for(int index = 0, maxIndex = Math.min(content.length, target.length); index < maxIndex; index++) {
				target[index] = content[index] / source[index];  
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
				case COMPLEX_FLOAT 	:
					throw new UnsupportedOperationException("Attempt to multiply real and compex matrix. Use cast() before");
				case REAL_DOUBLE	:
					return mulInvFromHadamard((matrix instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)matrix).content : matrix.extractDoubles());
				case REAL_FLOAT		:
					return mulInvFromHadamard((matrix instanceof FloatRealMatrix) ? ((FloatRealMatrix)matrix).content : matrix.extractFloats());
				case REAL_INT		:
					return mulInvFromHadamard((matrix instanceof IntRealMatrix) ? ((IntRealMatrix)matrix).content : matrix.extractInts());
				case REAL_LONG		:
					return mulInvFromHadamard((matrix instanceof LongRealMatrix) ? ((LongRealMatrix)matrix).content : matrix.extractLongs());
				case BIT :
					return mulInvFromHadamard(matrix.extractInts());
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(this.numberOfRows() * matrix.numberOfRows(), this.numberOfColumns() * matrix.numberOfColumns());
			final double[]				source = this.content;
			final double[]				target = result.content;
			final int 					maxY1 = this.numberOfRows(), maxY2 = matrix.numberOfRows();
			final int 					maxX1 = this.numberOfColumns(), maxX2 = matrix.numberOfColumns();
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
				case COMPLEX_FLOAT 	:
					throw new UnsupportedOperationException("Attempt to multiply real and complex matrices. Use cast() before");
				case REAL_DOUBLE 	: 
					final double[]	tempD = (matrix instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)matrix).content : matrix.extractDoubles();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	value = source[(y1 * maxX1 + x1)];
							
							if (value != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = value * tempD[sourceIndex];
									}
								}
							}
						}
					}
					break;
				case REAL_FLOAT 	: 
					final float[]	tempF = (matrix instanceof FloatRealMatrix) ? ((FloatRealMatrix)matrix).content : matrix.extractFloats();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	value = source[(y1 * maxX1 + x1)];
							
							if (value != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = value * tempF[sourceIndex];
									}
								}
							}
						}
					}
					break;
				case REAL_INT 		: 
					final int[]		tempI = (matrix instanceof IntRealMatrix) ? ((IntRealMatrix)matrix).content : matrix.extractInts();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	value = source[(y1 * maxX1 + x1)];
							
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
				case REAL_LONG 		:
					final long[]	tempL = (matrix instanceof LongRealMatrix) ? ((LongRealMatrix)matrix).content : matrix.extractLongs();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	value = source[(y1 * maxX1 + x1)];
							
							if (value != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = value * tempL[sourceIndex];
									}
								}
							}
						}
					}
					break;
				case BIT :
					final int[]		tempB = matrix.extractInts();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	value = source[(y1 * maxX1 + x1)];
							
							if (value != 0) {
								for (int y2 = 0; y2 < maxY2; y2++) {
									for (int x2 = 0; x2 < maxX2; x2++) {
										final int 	sourceIndex = y2 * maxX2 + x2; 
										final int	targetIndex = y1 * maxX2 * maxX1 * maxY2 + y2 * maxX1 * maxY2 + x1 * maxX2 + x2; 
										
										target[targetIndex] = value * tempB[sourceIndex];
									}
								}
							}
						}
					}
					break;
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(this.numberOfRows() * matrix.numberOfRows(), this.numberOfColumns() * matrix.numberOfColumns());
			final double[]				source = this.content;
			final double[]				target = result.content;
			final int 					maxY1 = matrix.numberOfRows(), maxY2 = this.numberOfRows();
			final int 					maxX1 = matrix.numberOfColumns(), maxX2 = this.numberOfColumns();
			
			switch (matrix.getType()) {
				case COMPLEX_DOUBLE : 
				case COMPLEX_FLOAT 	:
					throw new UnsupportedOperationException("Attempt to multiply real and complex matrices. Use cast() before");
				case REAL_DOUBLE 	: 
					final double[]	tempD = (matrix instanceof DoubleRealMatrix) ? ((DoubleRealMatrix)matrix).content : matrix.extractDoubles();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	value = tempD[(y1 * maxX1 + x1)];
							
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
				case REAL_FLOAT 	: 
					final float[]	tempF = (matrix instanceof FloatRealMatrix) ? ((FloatRealMatrix)matrix).content : matrix.extractFloats();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	value = tempF[(y1 * maxX1 + x1)];
							
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
				case REAL_INT 		: 
					final int[]		tempI = (matrix instanceof IntRealMatrix) ? ((IntRealMatrix)matrix).content : matrix.extractInts();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	value = tempI[(y1 * maxX1 + x1)];
							
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
				case REAL_LONG 		:
					final long[]	tempL = (matrix instanceof LongRealMatrix) ? ((LongRealMatrix)matrix).content : matrix.extractLongs();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	value = tempL[(y1 * maxX1 + x1)];
							
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
				case BIT	:
					final int[]		tempB = matrix.extractInts();
					
					for (int y1 = 0; y1 < maxY1; y1++) {
						for (int x1 = 0; x1 < maxX1; x1++) {
							final double	value = tempB[(y1 * maxX1 + x1)];
							
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
			final DoubleRealMatrix	result = new DoubleRealMatrix(this.numberOfRows(), this.numberOfColumns());
			final double[]			identity = result.content;
			final double[]			source = this.content.clone();
			final int				colSize = numberOfColumns();
			
			for(int index = 0; index < colSize; index++) {	// Make identity matrix
				identity[index * (colSize + 1)] = 1;
			}
			for(int y = 0; y < colSize; y++) {
				final double	diag = source[(y * (colSize + 1))];	// Take diagonal element.
				
				if (diag == 0) {
					throw new IllegalArgumentException("Matrix has zero element on diagonal");
				}
				else {
					final double	inv = 1 / diag;
					
					for(int x = 0; x < colSize; x++) {		// divide all line by diagonal element
						source[(y * colSize + x)] = source[(y * colSize + x)] * inv; 
						identity[(y * colSize + x)] = identity[(y * colSize + x)] * inv;
					}
					for(int i = y + 1; i < colSize; i++) {	// subtract current line from all lines below to make zeroes at the current column
						final double	value = source[(i * colSize + y)];
						
						for(int x = 0; x < colSize; x++) {
							source[(i * colSize + x)] -= value * source[(y * colSize + x)];
							identity[(i * colSize + x)] -= value * identity[(y * colSize + x)];
						}
					}
				}
			}
			for(int y = colSize-1; y >= 0; y--) {	// subtract current line from all lines above to make zeroes at the current column 
				for(int i = y - 1; i >= 0; i--) {
					final double	value = source[(i * colSize + y)];
					
					for(int x = 0; x < colSize; x++) {
						source[(i * colSize + x)] -= value * source[(y * colSize + x)];
						identity[(i * colSize + x)] -= value * identity[(y * colSize + x)];
					}
				}
			}
			result.beginTransaction();
			return result;
		}
	}

	@Override
	public Matrix transpose() {
		final DoubleRealMatrix	result = new DoubleRealMatrix(numberOfColumns(), numberOfRows());
		final double[]			source = this.content;
		final double[]			target = result.content;
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
		else if (!areAllAsyncCompleted()) {
			throw new IllegalStateException("Calling this method inside transaction. Call done() before.");
		}
		else {
			final double[]	source = this.content.clone();
			final int		colSize = numberOfColumns();
			double			detReal = 1;

			for(int y = 0; y < colSize; y++) {
				final double	diag = source[(y * (colSize + 1))];		// Take diagonal element.
				
				if (diag == 0) {
					return 0;
				}
				else {
					final double	inv = 1 / diag;
					
					detReal = detReal * diag;
					for(int x = 0; x < colSize; x++) {		// divide all line by diagonal element
						source[(y * colSize + x)] = source[(y * colSize + x)] * inv;
					}
					for(int i = y + 1; i < colSize; i++) {	// subtract current line from all lines below to make zeroes at the current column
						final double	value = source[(i * colSize + y)];
						
						for(int x = 0; x < colSize; x++) {
							source[(i * colSize + x)] -= source[(y * colSize + x)] * value;
						}
					}
				}
			}
			return detReal;
		}
	}

	@Override
	public Number track() {
		final double[]	source = this.content;
		final int		colSize = numberOfColumns();
		double	sum = 0;
		
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
	public Matrix apply(final Piece piece, final ApplyDouble callback) {
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
			final double[]	source = this.content;
			final int		x0 = piece.getLeft(), y0 = piece.getTop();
			final int		maxX = piece.getWidth(), maxY = piece.getHeight(), cols = numberOfColumns();
			
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
	
	@Override
	protected MatrixCalc buildMatrixCalc(final Command... cmds) throws SyntaxException {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	private Matrix aggregateAvg(final AggregateDirection dir) {
		final DoubleRealMatrix	result;
		final double[]		source = this.content;
		final double[]		target;
		final int			cols = numberOfColumns();
		double	value;
		
		switch (dir) {
			case ByColumns	:
				result = new DoubleRealMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					value = 0;
					
					for(int x = 0; x < cols; x++) {
						value += source[(y * cols + x)];
					}
					target[y] = value / cols;
				}
				break;
			case ByRows		:
				result = new DoubleRealMatrix(1, cols); 
				target = result.content;
				
				for(int x = 0; x < cols; x++) {
					value = 0;
					for(int y = 0; y < numberOfRows(); y++) {
						value += source[(y * cols + x)];
					}
					target[x] = value / numberOfRows();
				}
				break;
			case Total		:
				result = new DoubleRealMatrix(1, 1); 
				target = result.content;
				
				value = 0;
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < cols; x++) {
						value += source[(y * cols + x)];
					}
				}
				target[0] = value / (numberOfRows() * cols);
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
		}
		result.beginTransaction();
		return result;
	}
	
	private Matrix aggregateMax(final AggregateDirection dir) {
		final DoubleRealMatrix	result;
		final double[]			source = this.content;
		final double[]			target;
		final int				cols = numberOfColumns();
		double	val;
		
		switch (dir) {
			case ByColumns	:
				result = new DoubleRealMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					val = source[(y * cols + 0)]; 
					
					for(int x = 0; x < cols; x++) {
						final double	current = source[(y * cols + x)]; 
						
						if (current > val) {
							val = current;
						}
					}
					target[y] = val;
				}
				break;
			case ByRows		:
				result = new DoubleRealMatrix(1, cols); 
				target = result.content;
				
				for(int x = 0; x < cols; x++) {
					val = source[(x * cols + 0)]; 
					
					for(int y = 0; y < numberOfRows(); y++) {
						final double	current = source[(y * cols + x)]; 
						
						if (current > val) {
							val = current;
						}
					}
					target[x] = val;
				}
				break;
			case Total		:
				result = new DoubleRealMatrix(1, 1); 
				target = result.content;
				
				val = source[0]; 
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < cols; x++) {
						final double	current = source[(y * cols + x)]; 
						
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
		final DoubleRealMatrix	result;
		final double[]			source = this.content;
		final double[]			target;
		final int				cols = numberOfColumns();
		double	val;
		
		switch (dir) {
			case ByColumns	:
				result = new DoubleRealMatrix(numberOfRows(), 1);
				target = result.content;
				
				for(int y = 0; y < numberOfRows(); y++) {
					val = source[(y * cols + 0)]; 
					
					for(int x = 0; x < cols; x++) {
						final double	current = source[(y * cols + x)]; 
						
						if (current < val) {
							val = current;
						}
					}
					target[y] = val;
				}
				break;
			case ByRows		:
				result = new DoubleRealMatrix(1, cols); 
				target = result.content;
				
				for(int x = 0; x < cols; x++) {
					val = source[(x * cols + 0)]; 
					
					for(int y = 0; y < numberOfRows(); y++) {
						final double	current = source[(y * cols + x)]; 
						
						if (current < val) {
							val = current;
						}
					}
					target[x] = val;
				}
				break;
			case Total		:
				result = new DoubleRealMatrix(1, 1); 
				target = result.content;
				
				val = source[0]; 
				for(int y = 0; y < numberOfRows(); y++) {
					for(int x = 0; x < cols; x++) {
						final double	current = source[(y * cols + x)]; 
						
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
		final DoubleRealMatrix	result;
		final double[]			source = this.content;
		final double[]			target;
		final int				cols = numberOfColumns();
		double	sum;
		
		switch (dir) {
			case ByColumns	:
				result = new DoubleRealMatrix(numberOfRows(), 1);
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
				result = new DoubleRealMatrix(1, cols); 
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
				result = new DoubleRealMatrix(1, 1); 
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
