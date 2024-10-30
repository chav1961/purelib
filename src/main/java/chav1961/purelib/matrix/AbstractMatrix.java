package chav1961.purelib.matrix;

import java.io.IOException;

import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.streams.DataOutputAdapter;

public abstract class AbstractMatrix implements Matrix {
	private final Type	type; 
	private final int	rows; 
	private final int	cols; 

	protected AbstractMatrix(final Type type, final int rows, final int cols) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (rows <= 0) {
			throw new IllegalArgumentException("Number of rows ["+rows+"] must be greater than 0");
		}
		else if (cols <= 0) {
			throw new IllegalArgumentException("Number of columns ["+cols+"] must be greater than 0");
		}
		else {
			this.type = type;
			this.rows = rows;
			this.cols = cols;
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public Type getType() {
		return type;
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
	public Matrix apply(final Piece piece, final ApplyBit callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyInt callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyLong callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyFloat callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyDouble callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyFloat2 callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public Matrix apply(final Piece piece, final ApplyDouble2 callback) {
		throw new UnsupportedOperationException("This method is not applicable for matrix type ["+getType()+"]");
	}

	@Override
	public String toHumanReadableString() {
		final StringBuilder	sb = new StringBuilder();
		
		sb.append("=== Matrix: type=").append(getType()).append(", size=").append(numberOfRows()).append('x').append(numberOfColumns()).append(":\n");
		try {
			switch (getType()) {
				case BIT	:
					extractInts(new DataOutputAdapter() {
						long	count = 0;
						@Override
						public void writeInt(int v) throws IOException {
							sb.append(String.format("%1$1d ", v));
							if (count++ % numberOfColumns() == 0) {
								sb.append("\n");
							}
						}
					});
					break;
				case COMPLEX_DOUBLE	:
					extractDoubles(new DataOutputAdapter() {
						long	count = 0;
						double	real;
						@Override
						public void writeDouble(double v) throws IOException {
							if (count % 2 == 0) {
								real = v;
							}
							else {
								sb.append(AbstractMatrix.this.toString(real, v));
								if (count++ % numberOfColumns() == 0) {
									sb.append("\n");
								}
							}
						}
					});
					break;
				case COMPLEX_FLOAT	:
					extractDoubles(new DataOutputAdapter() {
						long	count = 0;
						float	real;
						@Override
						public void writeFloat(float v) throws IOException {
							if (count % 2 == 0) {
								real = v;
							}
							else {
								sb.append(AbstractMatrix.this.toString(real, v));
								if (count++ % numberOfColumns() == 0) {
									sb.append("\n");
								}
							}
						}
					});
					break;
				case REAL_DOUBLE	:
					extractDoubles(new DataOutputAdapter() {
						long	count = 0;
						@Override
						public void writeDouble(double v) throws IOException {
							sb.append(String.format("%1$20.15E ", v));
							if (count++ % numberOfColumns() == 0) {
								sb.append("\n");
							}
						}
					});
					break;
				case REAL_FLOAT		:
					extractFloats(new DataOutputAdapter() {
						long	count = 0;
						@Override
						public void writeFloat(float v) throws IOException {
							sb.append(String.format("%1$10.6E ", v));
							if (count++ % numberOfColumns() == 0) {
								sb.append("\n");
							}
						}
					});
					break;
				case REAL_INT		:
					extractInts(new DataOutputAdapter() {
						long	count = 0;
						@Override
						public void writeInt(int v) throws IOException {
							sb.append(String.format("%1$10d ", v));
							if (count++ % numberOfColumns() == 0) {
								sb.append("\n");
							}
						}
					});
					break;
				case REAL_LONG		:
					extractLongs(new DataOutputAdapter() {
						long	count = 0;
						@Override
						public void writeLong(long v) throws IOException {
							sb.append(String.format("%1$20d ", v));
							if (count++ % numberOfColumns() == 0) {
								sb.append("\n");
							}
						}
					});
					break;
				default:
					break;
			}
		} catch (IOException e) {
			sb.append(e.getLocalizedMessage());
		}
		sb.append("\n=== End matrix\n");
		return sb.toString();
	}

	protected Piece totalPiece() {
		return Piece.of(0, 0, rows, cols);
	}
	
	protected String toString(final float real, final float image) {
		if (real == 0) {
			return String.format("%1$10.6E", image);
		}
		else if (image == 0) {
			return String.format("%1$10.6E", real);
		}
		else {
			return String.format("%1$10.6E%1$+10.6E", real, image);
		}
	}
	
	protected String toString(final double real, final double image) {
		if (real == 0) {
			return String.format("%1$20.15E", image);
		}
		else if (image == 0) {
			return String.format("%1$20.15E", real);
		}
		else {
			return String.format("%1$20.15E%1$+20.15E", real, image);
		}
	}
}
