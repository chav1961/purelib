package chav1961.purelib.matrix;

import java.util.Arrays;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.matrix.interfaces.Matrix;

class RDMatrixImpl implements Matrix {
	private final int		rows;
	private final int		cols;
	private final double[]	content;

	RDMatrixImpl(final int rows, final int cols) {
		this.rows = rows;
		this.cols = cols;
		this.content = new double[rows * cols];
	}
	
	@Override
	public void close() throws RuntimeException {
	}

	@Override
	public void done() {
	}
	
	@Override
	public Type getType() {
		return Type.REAL_DOUBLE;
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
		else if (another.getType() != getType() || another.numberOfRows() != numberOfRows() || another.numberOfColumns() != numberOfColumns()) {
			return false;
		}
		else {
			return Arrays.equals(content, another.extractDoubles());
		}
	}

	@Override
	public int[] extractInts() {
		throw new UnsupportedOperationException("Can't extract ints because matrix content is double"); 
	}

	@Override
	public int[] extractInts(final Piece piece) {
		throw new UnsupportedOperationException("Can't extract ints because matrix content is double"); 
	}

	@Override
	public long[] extractLongs() {
		throw new UnsupportedOperationException("Can't extract longs because matrix content is double"); 
	}

	@Override
	public long[] extractLongs(final Piece piece) {
		throw new UnsupportedOperationException("Can't extract longs because matrix content is double"); 
	}

	@Override
	public float[] extractFloats() {
		throw new UnsupportedOperationException("Can't extract floats because matrix content is double"); 
	}

	@Override
	public float[] extractFloats(final Piece piece) {
		throw new UnsupportedOperationException("Can't extract floats because matrix content is double"); 
	}

	@Override
	public double[] extractDoubles() {
		return content;
	}

	@Override
	public double[] extractDoubles(final Piece piece) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else if (numberOfRows() == 1 || numberOfColumns() == 1) {
			final double[]	result = new double[piece.getWidth() * piece.getHeight()];
			
			MatrixUtils.ensurePieceIsValid(piece, numberOfRows(), numberOfColumns());
			System.arraycopy(content, piece.getTop() * numberOfColumns() + piece.getLeft(), result, 0, result.length);
			return result;
		}
		else {
			final double[]	result = new double[piece.getWidth() * piece.getHeight()];
			final int		targetWidth = piece.getWidth();

			MatrixUtils.ensurePieceIsValid(piece, numberOfRows(), numberOfColumns());
			for(int x = piece.getTop(), maxX = piece.getTop() + piece.getHeight(), to = 0; x < maxX; x++, to += targetWidth) {
				System.arraycopy(content, x * numberOfColumns() + piece.getLeft(), result, to, targetWidth);
			}
			return result;
		}
	}

	@Override
	public Matrix assign(final int... content) {
		throw new UnsupportedOperationException("Can't assign ints because matrix content is double"); 
	}

	@Override
	public Matrix assign(final Piece piece, final int... content) {
		throw new UnsupportedOperationException("Can't assign ints because matrix content is double"); 
	}

	@Override
	public Matrix assign(final long... content) {
		throw new UnsupportedOperationException("Can't assign longs because matrix content is double"); 
	}

	@Override
	public Matrix assign(final Piece piece, final long... content) {
		throw new UnsupportedOperationException("Can't assign longs because matrix content is double"); 
	}

	@Override
	public Matrix assign(final float... content) {
		throw new UnsupportedOperationException("Can't assign floats because matrix content is double"); 
	}

	@Override
	public Matrix assign(final Piece piece, final float... content) {
		throw new UnsupportedOperationException("Can't assign floats because matrix content is double"); 
	}

	@Override
	public Matrix assign(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (content.length != this.content.length) {
			throw new IllegalArgumentException("Different content length: awaited = ["+this.content.length+"], but passed = ["+content.length+"]");
		}
		else {
			System.arraycopy(content, 0, this.content, 0, content.length);
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
		else if (numberOfRows() == 1 || numberOfColumns() == 1) {
			MatrixUtils.ensurePieceIsValid(piece, numberOfRows(), numberOfColumns());
			
			if (piece.getWidth() * piece.getHeight() != content.length) {
				throw new IllegalArgumentException("Content size ["+content.length+"] is differ from piece width * piece.height ["+(piece.getWidth() * piece.getHeight())+"]");
			}
			else {
				System.arraycopy(content, 0, this.content, piece.getTop() * numberOfColumns() + piece.getLeft(), content.length);
				return this;
			}
		}
		else {
			MatrixUtils.ensurePieceIsValid(piece, numberOfRows(), numberOfColumns());
			final int	targetWidth = piece.getWidth();

			if (piece.getWidth() * piece.getHeight() != content.length) {
				throw new IllegalArgumentException("Content size ["+content.length+"] is differ from piece width * piece.height ["+(piece.getWidth() * piece.getHeight())+"]");
			}
			else {
				for(int x = piece.getTop(), maxX = piece.getTop() + piece.getHeight(), from = 0; x < maxX; x++, from += targetWidth) {
					System.arraycopy(content, from, this.content, x * numberOfColumns() + piece.getLeft(), targetWidth);
				}
				return this;
			}
		}
	}

	@Override
	public Matrix assign(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to assign can't be null");
		}
		else if (content.getType() != getType()) {
			throw new IllegalArgumentException("Content type ["+content.getType()+"] is differ with this type ["+getType()+"]");
		}
		else if (content.numberOfRows() != numberOfRows() || content.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Content size ["+content.numberOfRows()+"x"+content.numberOfColumns()+"] is differ with this size ["+numberOfRows()+"x"+numberOfColumns()+"]");
		}
		else {
			System.arraycopy(content.extractDoubles(), 0, this.content, 0, this.content.length);
			return this;
		}
	}

	@Override
	public Matrix assign(final Piece piece, final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to assign can't be null");
		}
		else if (content.getType() != getType()) {
			throw new IllegalArgumentException("Content type ["+content.getType()+"] is differ with this type ["+getType()+"]");
		}
		else {
			return assign(piece, content.extractDoubles());
		}
	}

	@Override
	public Matrix fill(final int value) {
		throw new UnsupportedOperationException("Can't fill ints because matrix content is double"); 
	}

	@Override
	public Matrix fill(final Piece piece, final int value) {
		throw new UnsupportedOperationException("Can't fill ints because matrix content is double"); 
	}

	@Override
	public Matrix fill(final long value) {
		throw new UnsupportedOperationException("Can't fill longs because matrix content is double"); 
	}

	@Override
	public Matrix fill(final Piece piece, final long value) {
		throw new UnsupportedOperationException("Can't fill longs because matrix content is double"); 
	}

	@Override
	public Matrix fill(final float value) {
		throw new UnsupportedOperationException("Can't fill floats because matrix content is double"); 
	}

	@Override
	public Matrix fill(final Piece piece, final float value) {
		throw new UnsupportedOperationException("Can't fill floats because matrix content is double"); 
	}

	@Override
	public Matrix fill(final float real, final float image) {
		throw new UnsupportedOperationException("Can't fill floats because matrix content is double"); 
	}

	@Override
	public Matrix fill(final Piece piece, final float real, final float image) {
		throw new UnsupportedOperationException("Can't fill floats because matrix content is double"); 
	}

	@Override
	public Matrix fill(final double value) {
		Utils.fillArray(content, value);
		return this;
	}

	@Override
	public Matrix fill(final Piece piece, final double value) {
		if (piece == null) {
			throw new NullPointerException("Piece can't be null");
		}
		else {
			MatrixUtils.ensurePieceIsValid(piece, numberOfRows(), numberOfColumns());
			
			for(int x = piece.getTop(), maxX = piece.getTop() + piece.getHeight(); x < maxX; x++) {
				for(int y = piece.getLeft(), maxY = piece.getLeft() + piece.getWidth(); y < maxY; y++) {
					this.content[x * numberOfColumns() + y] = value;
				}
			}
			return this;
		}
	}

	@Override
	public Matrix fill(final double real, final double image) {
		throw new UnsupportedOperationException("Can't fill doubles because matrix content is double"); 
	}

	@Override
	public Matrix fill(final Piece piece, final double real, final double image) {
		throw new UnsupportedOperationException("Can't fill doubles because matrix content is double"); 
	}

	@Override
	public Matrix cast(final Type type) {
		if (type == null) {
			throw new NullPointerException("Type to cast to can't be null"); 
		}
		else if (type == getType()) {
			return this;
		}
		else {
			switch (getType()) {
				case COMPLEX_DOUBLE	:
					return castComplexDouble(type, extractDoubles());
				case COMPLEX_FLOAT	:
					return castComplexFloat(type, extractFloats());
				case REAL_DOUBLE	:
					return castRealDouble(type, extractDoubles());
				case REAL_FLOAT		:
					return castRealFloat(type, extractFloats());
				case REAL_INT		:
					return castRealInt(type, extractInts());
				case REAL_LONG		:
					return castRealLong(type, extractLongs());
				default :
					throw new UnsupportedOperationException("Matrix type ["+getType()+"] is not supported yet");
			}
		}
	}

	@Override
	public Matrix add(final int... content) {
		throw new UnsupportedOperationException("Can't add ints because matrix content is double"); 
	}

	@Override
	public Matrix add(final long... content) {
		throw new UnsupportedOperationException("Can't add longs because matrix content is double"); 
	}

	@Override
	public Matrix add(final float... content) {
		throw new UnsupportedOperationException("Can't add floats because matrix content is double"); 
	}

	@Override
	public Matrix add(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else if (numberOfRows() * numberOfColumns() != content.length) {
			throw new IllegalArgumentException("Content size ["+content.length+"] is differ to awaited ["+(numberOfRows()*numberOfColumns())+"]");
		}
		else {
			final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
			final double[]	temp = result.extractDoubles();
			final double[]	source = this.content;
			
			for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
				temp[index] = source[index] + content[index];
			}
			return result;
		}
	}

	@Override
	public Matrix add(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null");
		}
		else if (content.getType() != getType()) {
			throw new IllegalArgumentException("Content type ["+content.getType()+"] is differ to awaited content ["+getType()+"]");
		}
		else if (content.numberOfRows() != numberOfRows() || content.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Content size ["+content.numberOfRows()+"x"+content.numberOfColumns()+"] is differ to awaited content size ["+numberOfRows()+"x"+numberOfColumns()+"]");
		}
		else {
			return add(content.extractDoubles());
		}
	}

	@Override
	public Matrix addValue(final int value) {
		throw new UnsupportedOperationException("Can't add ints because matrix content is double"); 
	}

	@Override
	public Matrix addValue(final long value) {
		throw new UnsupportedOperationException("Can't add long because matrix content is double"); 
	}

	@Override
	public Matrix addValue(final float value) {
		throw new UnsupportedOperationException("Can't add floats because matrix content is double"); 
	}

	@Override
	public Matrix addValue(final float real, final float image) {
		throw new UnsupportedOperationException("Can't add floats because matrix content is double"); 
	}

	@Override
	public Matrix addValue(final double value) {
		final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
		final double[]	temp = result.extractDoubles();
		final double[]	source = this.content;
		
		for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
			temp[index] = source[index] + value;
		}
		return result;
	}

	@Override
	public Matrix addValue(final double real, final double image) {
		throw new UnsupportedOperationException("Can't add doubles because matrix content is double"); 
	}

	@Override
	public Matrix subtract(final int... content) {
		throw new UnsupportedOperationException("Can't subtract ints because matrix content is double"); 
	}

	@Override
	public Matrix subtract(final long... content) {
		throw new UnsupportedOperationException("Can't subtract longs because matrix content is double"); 
	}

	@Override
	public Matrix subtract(final float... content) {
		throw new UnsupportedOperationException("Can't subtract floats because matrix content is double"); 
	}

	@Override
	public Matrix subtract(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else if (numberOfRows() * numberOfColumns() != content.length) {
			throw new IllegalArgumentException("Content size ["+content.length+"] is differ to awaited ["+(numberOfRows()*numberOfColumns())+"]");
		}
		else {
			final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
			final double[]	temp = result.extractDoubles();
			final double[]	source = this.content;
			
			for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
				temp[index] = source[index] - content[index];
			}
			return result;
		}
	}

	@Override
	public Matrix subtract(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else if (content.getType() != getType()) {
			throw new IllegalArgumentException("Content type ["+content.getType()+"] is differ to awaited content ["+getType()+"]");
		}
		else if (content.numberOfRows() != numberOfRows() || content.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Content size ["+content.numberOfRows()+"x"+content.numberOfColumns()+"] is differ to awaited content size ["+numberOfRows()+"x"+numberOfColumns()+"]");
		}
		else {
			return subtract(content.extractDoubles());
		}
	}

	@Override
	public Matrix subtractValue(final int value) {
		throw new UnsupportedOperationException("Can't subtract ints because matrix content is double"); 
	}

	@Override
	public Matrix subtractValue(final long value) {
		throw new UnsupportedOperationException("Can't subtract longs because matrix content is double"); 
	}

	@Override
	public Matrix subtractValue(final float value) {
		throw new UnsupportedOperationException("Can't subtract floats because matrix content is double"); 
	}

	@Override
	public Matrix subtractValue(final float real, final float image) {
		throw new UnsupportedOperationException("Can't subtract floats because matrix content is double"); 
	}

	@Override
	public Matrix subtractValue(final double value) {
		final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
		final double[]	temp = result.extractDoubles();
		final double[]	source = this.content;
		
		for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
			temp[index] = source[index] - value;
		}
		return result;
	}

	@Override
	public Matrix subtractValue(final double real, final double image) {
		throw new UnsupportedOperationException("Can't subtract doubles because matrix content is double"); 
	}

	@Override
	public Matrix subtractFrom(final int... content) {
		throw new UnsupportedOperationException("Can't subtract ints because matrix content is double"); 
	}

	@Override
	public Matrix subtractFrom(final long... content) {
		throw new UnsupportedOperationException("Can't subtract longs because matrix content is double"); 
	}

	@Override
	public Matrix subtractFrom(final float... content) {
		throw new UnsupportedOperationException("Can't subtract floats because matrix content is double"); 
	}

	@Override
	public Matrix subtractFrom(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract from can't be null");
		}
		else if (numberOfRows() * numberOfColumns() != content.length) {
			throw new IllegalArgumentException("Content size ["+content.length+"] is differ to awaited ["+(numberOfRows()*numberOfColumns())+"]");
		}
		else {
			final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
			final double[]	temp = result.extractDoubles();
			final double[]	source = this.content;
			
			for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
				temp[index] = content[index] - source[index];
			}
			return result;
		}
	}

	@Override
	public Matrix subtractFrom(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else if (content.getType() != getType()) {
			throw new IllegalArgumentException("Content type ["+content.getType()+"] is differ to awaited content ["+getType()+"]");
		}
		else if (content.numberOfRows() != numberOfRows() || content.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Content size ["+content.numberOfRows()+"x"+content.numberOfColumns()+"] is differ to awaited content size ["+numberOfRows()+"x"+numberOfColumns()+"]");
		}
		else {
			return subtractFrom(content.extractDoubles());
		}
	}

	@Override
	public Matrix subtractFromValue(final int value) {
		throw new UnsupportedOperationException("Can't subtract ints because matrix content is double"); 
	}

	@Override
	public Matrix subtractFromValue(final long value) {
		throw new UnsupportedOperationException("Can't subtract longs because matrix content is double"); 
	}

	@Override
	public Matrix subtractFromValue(final float value) {
		throw new UnsupportedOperationException("Can't subtract floats because matrix content is double"); 
	}

	@Override
	public Matrix subtractFromValue(final float real, final float image) {
		throw new UnsupportedOperationException("Can't subtract floats because matrix content is double"); 
	}

	@Override
	public Matrix subtractFromValue(final double value) {
		final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
		final double[]	temp = result.extractDoubles();
		final double[]	source = this.content;
		
		for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
			temp[index] = value - source[index];
		}
		return result;
	}

	@Override
	public Matrix subtractFromValue(final double real, final double image) {
		throw new UnsupportedOperationException("Can't subtract doubles because matrix content is double"); 
	}

	@Override
	public Matrix mul(Matrix content) {
		throw new UnsupportedOperationException("Can't add longs because matrix content is double"); 
	}

	@Override
	public Matrix mulInv(Matrix content) {
		throw new UnsupportedOperationException("Can't add longs because matrix content is double"); 
	}

	@Override
	public Matrix mulInvFrom(Matrix content) {
		throw new UnsupportedOperationException("Can't add longs because matrix content is double"); 
	}

	@Override
	public Matrix mulValue(final int value) {
		throw new UnsupportedOperationException("Can't multiply ints because matrix content is double"); 
	}

	@Override
	public Matrix mulValue(final long value) {
		throw new UnsupportedOperationException("Can't multiply longs because matrix content is double"); 
	}

	@Override
	public Matrix mulValue(final float value) {
		throw new UnsupportedOperationException("Can't multiply floats because matrix content is double"); 
	}

	@Override
	public Matrix mulValue(final float real, final float image) {
		throw new UnsupportedOperationException("Can't multiply floats because matrix content is double"); 
	}

	@Override
	public Matrix mulValue(final double value) {
		final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
		final double[]	temp = result.extractDoubles();
		final double[]	source = this.content;
		
		for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
			temp[index] = value * source[index];
		}
		return result;
	}

	@Override
	public Matrix mulValue(final double real, final double image) {
		throw new UnsupportedOperationException("Can't multiply doubles because matrix content is double"); 
	}

	@Override
	public Matrix divValue(final int value) {
		throw new UnsupportedOperationException("Can't divide ints because matrix content is double"); 
	}

	@Override
	public Matrix divValue(final long value) {
		throw new UnsupportedOperationException("Can't divide longs because matrix content is double"); 
	}

	@Override
	public Matrix divValue(final float value) {
		throw new UnsupportedOperationException("Can't divide floats because matrix content is double"); 
	}

	@Override
	public Matrix divValue(final float real, final float image) {
		throw new UnsupportedOperationException("Can't divide floats because matrix content is double"); 
	}

	@Override
	public Matrix divValue(final double value) {
		final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
		final double[]	temp = result.extractDoubles();
		final double[]	source = this.content;
		
		for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
			temp[index] = source[index] / value;
		}
		return result;
	}

	@Override
	public Matrix divValue(final double real, final double image) {
		throw new UnsupportedOperationException("Can't divide doubles because matrix content is double"); 
	}

	@Override
	public Matrix divFromValue(final int value) {
		throw new UnsupportedOperationException("Can't divide ints because matrix content is double"); 
	}

	@Override
	public Matrix divFromValue(final long value) {
		throw new UnsupportedOperationException("Can't divide longs because matrix content is double"); 
	}

	@Override
	public Matrix divFromValue(final float value) {
		throw new UnsupportedOperationException("Can't divide floats because matrix content is double"); 
	}

	@Override
	public Matrix divFromValue(final float real, final float image) {
		throw new UnsupportedOperationException("Can't divide floats because matrix content is double"); 
	}

	@Override
	public Matrix divFromValue(final double value) {
		final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
		final double[]	temp = result.extractDoubles();
		final double[]	source = this.content;
		
		for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
			temp[index] = value / source[index];
		}
		return result;
	}

	@Override
	public Matrix divFromValue(final double real, final double image) {
		throw new UnsupportedOperationException("Can't divide doubles because matrix content is double"); 
	}

	@Override
	public Matrix mulHadamard(final int... content) {
		throw new UnsupportedOperationException("Can't multiply ints because matrix content is double"); 
	}

	@Override
	public Matrix mulHadamard(final long... content) {
		throw new UnsupportedOperationException("Can't multiply longs because matrix content is double"); 
	}

	@Override
	public Matrix mulHadamard(final float... content) {
		throw new UnsupportedOperationException("Can't multiply floats because matrix content is double"); 
	}

	@Override
	public Matrix mulHadamard(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else if (numberOfRows() * numberOfColumns() != content.length) {
			throw new IllegalArgumentException("Content size ["+content.length+"] is differ to awaited ["+(numberOfRows()*numberOfColumns())+"]");
		}
		else {
			final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
			final double[]	temp = result.extractDoubles();
			final double[]	source = this.content;
			
			for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
				temp[index] = source[index] * content[index];
			}
			return result;
		}
	}

	@Override
	public Matrix mulHadamard(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else if (content.getType() != getType()) {
			throw new IllegalArgumentException("Content type ["+content.getType()+"] is differ to awaited content ["+getType()+"]");
		}
		else if (content.numberOfRows() != numberOfRows() || content.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Content size ["+content.numberOfRows()+"x"+content.numberOfColumns()+"] is differ to awaited content size ["+numberOfRows()+"x"+numberOfColumns()+"]");
		}
		else {
			return mulHadamard(content.extractDoubles());
		}
	}

	@Override
	public Matrix mulInvHadamard(final int... content) {
		throw new UnsupportedOperationException("Can't multiply ints because matrix content is double"); 
	}

	@Override
	public Matrix mulInvHadamard(final long... content) {
		throw new UnsupportedOperationException("Can't multiply longs because matrix content is double"); 
	}

	@Override
	public Matrix mulInvHadamard(final float... content) {
		throw new UnsupportedOperationException("Can't multiply floats because matrix content is double"); 
	}

	@Override
	public Matrix mulInvHadamard(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else if (numberOfRows() * numberOfColumns() != content.length) {
			throw new IllegalArgumentException("Content size ["+content.length+"] is differ to awaited ["+(numberOfRows()*numberOfColumns())+"]");
		}
		else {
			final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
			final double[]	temp = result.extractDoubles();
			final double[]	source = this.content;
			
			for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
				temp[index] = source[index] / content[index];
			}
			return result;
		}
	}

	@Override
	public Matrix mulInvHadamard(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else if (content.getType() != getType()) {
			throw new IllegalArgumentException("Content type ["+content.getType()+"] is differ to awaited content ["+getType()+"]");
		}
		else if (content.numberOfRows() != numberOfRows() || content.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Content size ["+content.numberOfRows()+"x"+content.numberOfColumns()+"] is differ to awaited content size ["+numberOfRows()+"x"+numberOfColumns()+"]");
		}
		else {
			return mulInvHadamard(content.extractDoubles());
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final int... content) {
		throw new UnsupportedOperationException("Can't multiply doubles because matrix content is double"); 
	}

	@Override
	public Matrix mulInvFromHadamard(final long... content) {
		throw new UnsupportedOperationException("Can't multiply longs because matrix content is double"); 
	}

	@Override
	public Matrix mulInvFromHadamard(final float... content) {
		throw new UnsupportedOperationException("Can't multiply floats because matrix content is double"); 
	}

	@Override
	public Matrix mulInvFromHadamard(final double... content) {
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else if (numberOfRows() * numberOfColumns() != content.length) {
			throw new IllegalArgumentException("Content size ["+content.length+"] is differ to awaited ["+(numberOfRows()*numberOfColumns())+"]");
		}
		else {
			final Matrix	result = new RDMatrixImpl(numberOfRows(), numberOfColumns());
			final double[]	temp = result.extractDoubles();
			final double[]	source = this.content;
			
			for(int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
				temp[index] = content[index] / source[index];
			}
			return result;
		}
	}

	@Override
	public Matrix mulInvFromHadamard(final Matrix content) {
		if (content == null) {
			throw new NullPointerException("Content to subtract can't be null");
		}
		else if (content.getType() != getType()) {
			throw new IllegalArgumentException("Content type ["+content.getType()+"] is differ to awaited content ["+getType()+"]");
		}
		else if (content.numberOfRows() != numberOfRows() || content.numberOfColumns() != numberOfColumns()) {
			throw new IllegalArgumentException("Content size ["+content.numberOfRows()+"x"+content.numberOfColumns()+"] is differ to awaited content size ["+numberOfRows()+"x"+numberOfColumns()+"]");
		}
		else {
			return mulInvFromHadamard(content.extractDoubles());
		}
	}
	
	@Override
	public Matrix tensorMul(final Matrix content) {
		// TODO Auto-generated method stub
		if (content == null) {
			throw new NullPointerException("Content to multiply can't be null");
		}
		else if (1L * numberOfRows() * numberOfColumns() * content.numberOfRows() * content.numberOfColumns() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Traget matrix size ["+(1L * numberOfRows() * numberOfColumns() * content.numberOfRows() * content.numberOfColumns())+"] extends available maximum ["+Integer.MAX_VALUE+"]");
		}
		else {
			final int		rowSize = numberOfRows() * content.numberOfRows();
			final int		colSize = numberOfColumns() * content.numberOfColumns();
			final Matrix	result = new RDMatrixImpl(rowSize, colSize);
			final double[]	left = this.content, right = content.extractDoubles(), target = result.extractDoubles();
			
			for(int xLeft = 0, maxXLeft = numberOfRows(); xLeft < maxXLeft; xLeft++) {
				for(int yLeft = 0, maxYLeft = numberOfColumns(); yLeft < maxYLeft; yLeft++) {
					final double	multiplier = left[xLeft * numberOfColumns() + yLeft];
					
					for(int xRight = 0, maxXRight = numberOfRows(); xRight < maxXRight; xRight++) {
						for(int yRight = 0, maxYRight = numberOfColumns(); yRight < maxYRight; yRight++) {
							target[0] = multiplier * right[xRight * content.numberOfColumns() + yRight];
						}
					}
				}
			}
			return result;
		}
	}

	@Override
	public Matrix invert() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Non-square matrix can't be inverted");
		}
		else {
			return cast(Type.REAL_FLOAT).invert();
		}
	}

	@Override
	public Matrix transpose() {
		// TODO Auto-generated method stub
		if (numberOfRows() == 1 || numberOfColumns() == 1) {
			final Matrix	result = new RDMatrixImpl(numberOfColumns(), numberOfRows());
			
			System.arraycopy(content, 0, result.extractInts(), 0, numberOfColumns() * numberOfRows());
			return result;
		}
		else {
			
			return null;
		}
	}

	@Override
	public Matrix aggregate(AggregateDirection dir, AggregateType aggType) {
		// TODO Auto-generated method stub
		if (dir == null) {
			throw new NullPointerException("Aggregate direction can't be null");
		}
		else if (aggType == null) {
			throw new NullPointerException("Aggregate type can't be null");
		}
		else {
			final Matrix	result;
			final int		loops, displ, step, amount;
			
			switch (dir) {
				case ByColumns	:
					loops = numberOfColumns();
					step = numberOfColumns();
					displ = numberOfColumns();
					amount = numberOfRows();
					result = new RDMatrixImpl(1, numberOfColumns());
					break;
				case ByRows		:
					loops = numberOfRows();
					step = 1;
					displ = numberOfRows();
					amount = numberOfColumns();
					result = new RDMatrixImpl(numberOfRows(), 1);
					break;
				default:
					throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is not supported yet");
			}
			final int[]	temp = result.extractInts();
					
			switch (aggType) {
				case Avg	:
					for(int index = 0; index < loops; index++) {
						long	sum = 0;
						temp[index] = (int)(sum / 1);
					}
					break;
				case Max	:
					for(int index = 0; index < loops; index++) {
						int		max = 0;
						temp[index] = max;
					}
					break;
				case Min	:
					for(int index = 0; index < loops; index++) {
						int		min = 0;
						temp[index] = min;
					}
					break;
				case Sum	:
					for(int index = 0; index < loops; index++) {
						long	sum = 0;
						temp[index] = (int)sum;
					}
					break;
				default :
					throw new UnsupportedOperationException("Aggregate type ["+aggType+"] is not supported yet");
			}
			return result;
		}
	}

	@Override
	public Number det() {
		if (numberOfRows() != numberOfColumns()) {
			throw new IllegalStateException("Non-square matrix can't have determinant");
		}
		else {
			return cast(Type.REAL_FLOAT).det();
		}
	}

	@Override
	public Number track() {
		final double[]	temp = content;
		final int		displ = numberOfColumns() + 1;
		double			result = 1;
		
		for(int index = 0, maxIndex = Math.min(numberOfRows(), numberOfColumns()); index < maxIndex; index++) {
			result += temp[index * displ];
		}
		return result;
	}

	@Override
	public String toHumanReadableString() {
		final StringBuilder	sb = new StringBuilder();
		
		sb.append(getType()).append(' ').append(numberOfRows()).append('x').append(numberOfColumns()).append(":\n");
		for(int x = 0, maxX = numberOfRows(); x < maxX; x++) {
			sb.append('\t');
			for(int y = 0, maxY = numberOfColumns(); y < maxY; y++) {
				sb.append(String.format("%1$25g ", this.content[x * maxY + y]));
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	private Matrix castRealInt(final Type type, final int[] ints) {
		// TODO Auto-generated method stub
		switch (type) {
			case COMPLEX_DOUBLE :
				break;
			case COMPLEX_FLOAT	:
				break;
			case REAL_DOUBLE	:
				break;
			case REAL_FLOAT		:
				break;
			case REAL_INT		:
				break;
			case REAL_LONG		:
				break;
			default:
				throw new UnsupportedOperationException("Cast type ["+type+"] is not supported yet");
		}
		return null;
	}

	private Matrix castRealLong(final Type type, final long[] longs) {
		// TODO Auto-generated method stub
		switch (type) {
			case COMPLEX_DOUBLE:
				break;
			case COMPLEX_FLOAT:
				break;
			case REAL_DOUBLE:
				break;
			case REAL_FLOAT:
				break;
			case REAL_INT:
				break;
			case REAL_LONG:
				break;
			default:
				throw new UnsupportedOperationException("Cast type ["+type+"] is not supported yet");
		}
		return null;
	}

	
	private Matrix castRealFloat(final Type type, final float[] floats) {
		// TODO Auto-generated method stub
		switch (type) {
			case COMPLEX_DOUBLE:
				break;
			case COMPLEX_FLOAT:
				break;
			case REAL_DOUBLE:
				break;
			case REAL_FLOAT:
				break;
			case REAL_INT:
				break;
			case REAL_LONG:
				break;
			default:
				throw new UnsupportedOperationException("Cast type ["+type+"] is not supported yet");
		}
		return null;
	}

	private Matrix castRealDouble(final Type type, final double[] doubles) {
		// TODO Auto-generated method stub
		switch (type) {
			case COMPLEX_DOUBLE:
				break;
			case COMPLEX_FLOAT:
				break;
			case REAL_DOUBLE:
				break;
			case REAL_FLOAT:
				break;
			case REAL_INT:
				break;
			case REAL_LONG:
				break;
			default:
				throw new UnsupportedOperationException("Cast type ["+type+"] is not supported yet");
		}
		return null;
	}

	private Matrix castComplexFloat(final Type type, final float[] floats) {
		// TODO Auto-generated method stub
		switch (type) {
			case COMPLEX_DOUBLE:
				break;
			case COMPLEX_FLOAT:
				break;
			case REAL_DOUBLE:
				break;
			case REAL_FLOAT:
				break;
			case REAL_INT:
				break;
			case REAL_LONG:
				break;
			default:
				throw new UnsupportedOperationException("Cast type ["+type+"] is not supported yet");
		}
		return null;
	}

	private Matrix castComplexDouble(final Type type, final double[] doubles) {
		// TODO Auto-generated method stub
		switch (type) {
			case COMPLEX_DOUBLE:
				break;
			case COMPLEX_FLOAT:
				break;
			case REAL_DOUBLE:
				break;
			case REAL_FLOAT:
				break;
			case REAL_INT:
				break;
			case REAL_LONG:
				break;
			default:
				throw new UnsupportedOperationException("Cast type ["+type+"] is not supported yet");
		}
		return null;
	}
}
