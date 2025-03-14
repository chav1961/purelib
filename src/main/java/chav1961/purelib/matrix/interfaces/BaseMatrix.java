package chav1961.purelib.matrix.interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.CalculationException;

/**
 * <p>This interface supports base matrix functionality</p>
 * @param <T> matrix type
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
public interface BaseMatrix<T extends BaseMatrix<?>> extends Cloneable, AutoCloseable {
	/**
	 * <p>Bit callback to fill zero matrix.</p>
	 */
	ApplyCallback	ZERO_BIT = new BitApplyCallback() {
									@Override
									public boolean apply(int x, int y, boolean value) throws CalculationException {
										return false;
									}
								};
	/**
	 * <p>Integer callback to fill zero matrix.</p>
	 */
	ApplyCallback	ZERO_INT = new IntApplyCallback() {
									@Override
									public int apply(int x, int y, int value) throws CalculationException {
										return 0;
									}
								};
	/**
	 * <p>Long callback to fill zero matrix.</p>
	 */
	ApplyCallback	ZERO_LONG = new LongApplyCallback() {
									@Override
									public long apply(int x, int y, long value) throws CalculationException {
										return 0;
									}
								};
	/**
	 * <p>Float callback to fill zero matrix.</p>
	 */
	ApplyCallback	ZERO_FLOAT = new FloatApplyCallback() {
									@Override
									public float apply(int x, int y, float value) throws CalculationException {
										return 0;
									}
								};
	/**
	 * <p>Double callback to fill zero matrix.</p>
	 */
	ApplyCallback	ZERO_DOUBLE = new DoubleApplyCallback() {
									@Override
									public double apply(int x, int y, double value) throws CalculationException {
										return 0;
									}
								};
	/**
	 * <p>Complex float callback to fill zero matrix.</p>
	 */
	ApplyCallback	ZERO_COMPLEX_FLOAT = new ComplexFloatApplyCallback() {
									@Override
									public void apply(int x, int y, float[] value) throws CalculationException {
										value[0] = 1;
										value[1] = 0;
									}
								};
	/**
	 * <p>Complex double callback to fill zero matrix.</p>
	 */
	ApplyCallback	ZERO_COMPLEX_DOUBLE = new ComplexDoubleApplyCallback() {
									@Override
									public void apply(int x, int y, double[] value) throws CalculationException {
										value[0] = 1;
										value[1] = 0;
									}
								};
	/**
	 * <p>Bit callback to fill identity matrix.</p>
	 */
	ApplyCallback	IDENTITY_BIT = new BitApplyCallback() {
									@Override
									public boolean apply(int x, int y, boolean value) throws CalculationException {
										return x == y;
									}
								};
	/**
	 * <p>Integer callback to fill identity matrix.</p>
	 */
	ApplyCallback	IDENTITY_INT = new IntApplyCallback() {
									@Override
									public int apply(int x, int y, int value) throws CalculationException {
										return x == y ? 1 : 0;
									}
								};
	/**
	 * <p>Long callback to fill identity matrix.</p>
	 */
	ApplyCallback	IDENTITY_LONG = new LongApplyCallback() {
									@Override
									public long apply(int x, int y, long value) throws CalculationException {
										return x == y ? 1 : 0;
									}
								};
	/**
	 * <p>Float callback to fill identity matrix.</p>
	 */
	ApplyCallback	IDENTITY_FLOAT = new FloatApplyCallback() {
									@Override
									public float apply(int x, int y, float value) throws CalculationException {
										return x == y ? 1 : 0;
									}
								};
	/**
	 * <p>Double callback to fill identity matrix.</p>
	 */
	ApplyCallback	IDENTITY_DOUBLE = new DoubleApplyCallback() {
									@Override
									public double apply(int x, int y, double value) throws CalculationException {
										return x == y ? 1 : 0;
									}
								};
	/**
	 * <p>Complex float callback to fill identity matrix.</p>
	 */
	ApplyCallback	IDENTITY_COMPLEX_FLOAT = new ComplexFloatApplyCallback() {
									@Override
									public void apply(int x, int y, float[] value) throws CalculationException {
										value[0] = x == y ? 1 : 0;
										value[1] = 0;
									}
								};
	/**
	 * <p>Complex double callback to fill identity matrix.</p>
	 */
	ApplyCallback	IDENTITY_COMPLEX_DOUBLE = new ComplexDoubleApplyCallback() {
									@Override
									public void apply(int x, int y, double[] value) throws CalculationException {
										value[0] = x == y ? 1 : 0;
										value[1] = 0;
									}
								};
	
	/**
	 * <p>This enumeration describes matrix content type.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	public static enum ContentType {
		BIT,
		REAL_INT,
		REAL_LONG,
		REAL_FLOAT,
		REAL_DOUBLE,
		COMPLEX_FLOAT,
		COMPLEX_DOUBLE
	}
	
	/**
	 * <p>This enumeration describes internal matrix format type.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	public static enum FormatType {
		PLAIN,
		BITMAP,
		PACKED_LINE,
		LIST
	}
	
	/**
	 * <p>This enumeration describes matrix data location.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	public static enum StoreType {
		IN_MEMORY,
		EXTERNAL
	}
	
	/**
	 * <p>This enumeration describes direction to calculate aggregate matrix functions.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	public static enum AggregateDirection {
		BY_ROWS,
		BY_COLUMNS,
		TOTAL
	}

	/**
	 * <p>This enumeration describes aggregate matrix functions to calculate.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	public static enum AggregateType {
		SUM,
		AVG,
		MIN,
		MAX;
	}
	
	/**
	 * <p>This interface describes a <i>piece</i> of matrix content to use in operations</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	public interface Piece {
		/**
		 * <p>Get zero-based start column of the matrix piece</p>
		 * @return zero-based start column of the matrix piece
		 */
		int getX();
		
		/**
		 * <p>Get zero-based start row of the matrix piece</p>
		 * @return zero-based start row of the matrix piece
		 */
		int getY();
		
		/**
		 * <p>Get number of columns in the matrix piece</p>
		 * @return number of columns in the matrix piece
		 */
		int getWidth();

		/**
		 * <p>Get number of rows in the matrix piece</p>
		 * @return number of rows in the matrix piece
		 */
		int getHeight();
		
		/**
		 * <p>Create simple implementation of the interface.</p>
		 * @param x zero-based start column of the piece.
		 * @param y zero-based start row of the piece
		 * @param width number of columns in the piece
		 * @param height number of rows in the piece
		 * @return piece implementation created. Can't be null
		 * @throws IllegalArgumentException on any logical errors in the arguments
		 */
		public static Piece of(final int x, final int y, final int width, final int height) throws IllegalArgumentException {
			if (x < 0) {
				throw new IllegalArgumentException("X value ["+x+"] must get greater or equals than 0");
			}
			else if (y < 0) {
				throw new IllegalArgumentException("Y value ["+y+"] must get greater or equals than 0");
			}
			else if (width <= 0) {
				throw new IllegalArgumentException("Width value ["+width+"] must get greater than 0");
			}
			else if (height <= 0) {
				throw new IllegalArgumentException("Height value ["+height+"] must get greater than 0");
			}
			else {
				return new Piece() {
					@Override public int getX() {return x;}
					@Override public int getY() {return y;}
					@Override public int getWidth() {return width;}
					@Override public int getHeight() {return height;}
					
					@Override
					public String toString() {
						return "Piece[x="+x+",y="+y+",width="+width+",height="+height+"]";
					}
				};
			}
		}
	}

	/**
	 * <p>Basic marker interface for apply functions</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	public static interface ApplyCallback {
	}
	
	/**
	 * <p>Bit apply callback interface</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	@FunctionalInterface
	public static interface BitApplyCallback extends ApplyCallback {
		/**
		 * <p>Process bit value from cell</p>
		 * @param x zero-based cell column number.
		 * @param y zero-based cell row number.
		 * @param value cell value
		 * @return new cell value to replace
		 * @throws CalculationException any calculation error
		 */
		boolean apply(int x, int y, boolean value) throws CalculationException;
	}

	/**
	 * <p>Integer apply callback interface</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	@FunctionalInterface
	public static interface IntApplyCallback extends ApplyCallback {
		/**
		 * <p>Process integer value from cell</p>
		 * @param x zero-based cell column number.
		 * @param y zero-based cell row number.
		 * @param value cell value
		 * @return new cell value to replace
		 * @throws CalculationException any calculation error
		 */
		int apply(int x, int y, int value) throws CalculationException;
	}

	/**
	 * <p>Long apply callback interface</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	@FunctionalInterface
	public static interface LongApplyCallback extends ApplyCallback {
		/**
		 * <p>Process long value from cell</p>
		 * @param x zero-based cell column number.
		 * @param y zero-based cell row number.
		 * @param value cell value
		 * @return new cell value to replace
		 * @throws CalculationException any calculation error
		 */
		long apply(int x, int y, long value) throws CalculationException;
	}

	/**
	 * <p>Float apply callback interface</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	@FunctionalInterface
	public static interface FloatApplyCallback extends ApplyCallback {
		/**
		 * <p>Process float value from cell</p>
		 * @param x zero-based cell column number.
		 * @param y zero-based cell row number.
		 * @param value cell value
		 * @return new cell value to replace
		 * @throws CalculationException any calculation error
		 */
		float apply(int x, int y, float value) throws CalculationException;
	}

	/**
	 * <p>Double apply callback interface</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	@FunctionalInterface
	public static interface DoubleApplyCallback extends ApplyCallback {
		/**
		 * <p>Process double value from cell</p>
		 * @param x zero-based cell column number.
		 * @param y zero-based cell row number.
		 * @param value cell value
		 * @return new cell value to replace
		 * @throws CalculationException any calculation error
		 */
		double apply(int x, int y, double value) throws CalculationException;
	}

	/**
	 * <p>Complex float apply callback interface</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	@FunctionalInterface
	public static interface ComplexFloatApplyCallback extends ApplyCallback {
		/**
		 * <p>Process complex float value from cell</p>
		 * @param x zero-based cell column number.
		 * @param y zero-based cell row number.
		 * @param value float[2] array with real and image cell value. To return new value, simply assign it to array items.
		 * @throws CalculationException any calculation error
		 */
		void apply(int x, int y, float[] value) throws CalculationException;
	}

	/**
	 * <p>Complex double apply callback interface</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	@FunctionalInterface
	public static interface ComplexDoubleApplyCallback extends ApplyCallback {
		/**
		 * <p>Process complex double value from cell</p>
		 * @param x zero-based cell column number.
		 * @param y zero-based cell row number.
		 * @param value double[2] array with real and image cell value. To return new value, simply assign it to array items.
		 * @throws CalculationException any calculation error
		 */
		void apply(int x, int y, double[] value) throws CalculationException;
	}
	
	/**
	 * <p>This interface describes immutable scalar operand to use in the matrix arithmetics</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	public static interface Operand {
		/**
		 * <p>Get operand content type</p>
		 * @return operand content type. Can't be null.
		 */
		ContentType getContentType();
		/**
		 * <p>Get operand value as boolean. Real types return 0 for zeroes, 1 for non-zeroes. Complex types return 0 for zeroes, 1 for non-zeroes</p>
		 * @return boolean operand value.
		 */
		boolean getBoolean();
		/**
		 * <p>Get operand value as integer. Real types return (int) conversions. Complex types return module of complex exponent</p>
		 * @return integer operand value.
		 */
		int getInt();
		/**
		 * <p>Get operand value as long. Real types return (long) conversions. Complex types return module of complex exponent</p>
		 * @return long operand value.
		 */
		long getLong();
		/**
		 * <p>Get operand value as float. Complex types return module of complex exponent</p>
		 * @return float operand value.
		 */
		float getFloat();
		/**
		 * <p>Get operand value as double. Complex types return module of complex exponent</p>
		 * @return double operand value.
		 */
		double getDouble();
		/**
		 * <p>Get operand value as complex float. Real types return value with zero image part</p>
		 * @return complex float operand value. Can't be null and always have two elements.
		 */
		float[] getComplexFloat();
		/**
		 * <p>Get operand value as complex double. Real types return value with zero image part</p>
		 * @return complex double operand value. Can't be null and always have two elements.
		 */
		double[] getComplexDouble();		
		
		/**
		 * <p>Make bit operand instance</p>
		 * @param value bit operand value.
		 * @return bit operand. Can't be null.
		 */
		static Operand of(final boolean value) {
			return new Operand() {
				@Override public ContentType getContentType() {return ContentType.BIT;}
				@Override public boolean getBoolean() {return value;}
				@Override public float getFloat() {return value ? 1 : 0;}
				@Override public double getDouble() {return value ? 1 : 0;}
				@Override public float[] getComplexFloat() {return new float[] {value ? 1 : 0, 0};}
				@Override public double[] getComplexDouble() {return new double[] {value ? 1 : 0, 0};}
				@Override public long getLong() {return value ? 1 : 0;}
				@Override public int getInt() {return value ? 1 : 0;}
			};
		}

		/**
		 * <p>Make integer operand instance</p>
		 * @param value integer operand value.
		 * @return integer operand. Can't be null.
		 */
		static Operand of(final int value) {
			return new Operand() {
				@Override public ContentType getContentType() {return ContentType.REAL_INT;}
				@Override public boolean getBoolean() {return value != 0;}
				@Override public float getFloat() {return value;}
				@Override public double getDouble() {return value;}
				@Override public float[] getComplexFloat() {return new float[] {value, 0};}
				@Override public double[] getComplexDouble() {return new double[] {value, 0};}
				@Override public long getLong() {return value;}
				@Override public int getInt() {return value;}
			};
		}

		/**
		 * <p>Make long operand instance</p>
		 * @param value long operand value.
		 * @return long operand. Can't be null.
		 */
		static Operand of(final long value) {
			return new Operand() {
				@Override public ContentType getContentType() {return ContentType.REAL_LONG;}
				@Override public boolean getBoolean() {return value != 0;}
				@Override public float getFloat() {return value;}
				@Override public double getDouble() {return value;}
				@Override public float[] getComplexFloat() {return new float[] {value, 0};}
				@Override public double[] getComplexDouble() {return new double[] {value, 0};}
				@Override public long getLong() {return value;}
				@Override public int getInt() {return (int)value;}
			};
		}

		/**
		 * <p>Make float operand instance</p>
		 * @param value float operand value.
		 * @return float operand. Can't be null.
		 */
		static Operand of(final float value) {
			return new Operand() {
				@Override public ContentType getContentType() {return ContentType.REAL_FLOAT;}
				@Override public boolean getBoolean() {return value != 0;}
				@Override public float getFloat() {return value;}
				@Override public double getDouble() {return value;}
				@Override public float[] getComplexFloat() {return new float[] {value, 0};}
				@Override public double[] getComplexDouble() {return new double[] {value, 0};}
				@Override public long getLong() {return (long)value;}
				@Override public int getInt() {return (int)value;}
			};
		}

		/**
		 * <p>Make double operand instance</p>
		 * @param value double operand value.
		 * @return double operand. Can't be null.
		 */
		static Operand of(final double value) {
			return new Operand() {
				@Override public ContentType getContentType() {return ContentType.REAL_FLOAT;}
				@Override public boolean getBoolean() {return value != 0;}
				@Override public float getFloat() {return (float)value;}
				@Override public double getDouble() {return value;}
				@Override public float[] getComplexFloat() {return new float[] {(float)value, 0};}
				@Override public double[] getComplexDouble() {return new double[] {value, 0};}
				@Override public long getLong() {return (long)value;}
				@Override public int getInt() {return (int)value;}
			};
		}

		/**
		 * <p>Make complex float operand instance</p>
		 * @param real real part of the operand value.
		 * @param image image part of the operand value.
		 * @return complex float operand. Can't be null.
		 */
		static Operand of(final float real, final float image) {
			return new Operand() {
				@Override public ContentType getContentType() {return ContentType.COMPLEX_FLOAT;}
				@Override public boolean getBoolean() {return real != 0;}
				@Override public float getFloat() {return real;}
				@Override public double getDouble() {return real;}
				@Override public float[] getComplexFloat() {return new float[] {real, image};}
				@Override public double[] getComplexDouble() {return new double[] {real, image};}
				@Override public long getLong() {return (long)real;}
				@Override public int getInt() {return (int)real;}
			};
		}

		/**
		 * <p>Make complex double operand instance</p>
		 * @param real real part of the operand value.
		 * @param image image part of the operand value.
		 * @return complex double operand. Can't be null.
		 */
		static Operand of(final double real, final double image) {
			return new Operand() {
				@Override public ContentType getContentType() {return ContentType.COMPLEX_DOUBLE;}
				@Override public boolean getBoolean() {return real != 0;}
				@Override public float getFloat() {return (float)real;}
				@Override public double getDouble() {return real;}
				@Override public float[] getComplexFloat() {return new float[] {(float)real, (float)image};}
				@Override public double[] getComplexDouble() {return new double[] {real, image};}
				@Override public long getLong() {return (long)real;}
				@Override public int getInt() {return (int)real;}
			};
		}
	}

	/**
	 * <p>Get matrix content type</p>
	 * @return matrix content type. Can't be null.
	 */
	ContentType getContentType();
	
	/**
	 * <p>Get internal matrix format type</p>
	 * @return internal matrix format type. Can't be null.
	 */
	FormatType getFormatType();
	
	/**
	 * <p>Get matrix data location type.</p>
	 * @return matrix data location type. Can't be null.
	 */
	StoreType getStoreType();
	
	/**
	 * <p>Get number of columns in the matrix</p>
	 * @return number of columns in the matrix. Always greater than 0
	 */
	int getWidth();	
	/**
	 * <p>Get number of rows in the matrix</p>
	 * @return number of rows in the matrix. Always greater than 0
	 */
	int getHeight();
	
	@Override
	void close() throws CalculationException;
	
	/**
	 * <p>Download matrix content from source. Used the only method from {@linkplain DataInput} source due to matrix content type.
	 * Complex numbers treated as two sequential real values.</p>
	 * @param piece piece to download content to. Can't be null.
	 * @param in source to download content. Can't be null.
	 * @return self. Can be used in chained calls.
	 * @throws IOException on any I/O errors.
	 * @throws NullPointerException any argument is null
	 */
	T download(Piece piece, DataInput in) throws IOException, NullPointerException;
	/**
	 * <p>Download matrix content from source. Used the only method from {@linkplain DataInput} source due to matrix content type.
	 * Complex numbers treated as two sequential real values.</p>
	 * @param in source to download content. Can't be null.
	 * @return self. Can be used in chained calls.
	 * @throws IOException on any I/O errors.
	 * @throws NullPointerException any argument is null
	 */
	default T download(DataInput in) throws IOException, NullPointerException {
		return download(Piece.of(0, 0, getWidth(), getHeight()), in);
	}

	/**
	 * <p>Download matrix content from another matrix.</p>
	 * @param piece piece to download content both from and to. Can't be null.
	 * @param in another matrix to download content. Can't be null.
	 * @return self. Can be used in chained calls.
	 * @throws IOException on any I/O errors.
	 * @throws NullPointerException any argument is null
	 */
	T download(Piece piece, T in) throws IOException, NullPointerException;
	/**
	 * <p>Download matrix content from another matrix.</p>
	 * @param in another matrix to download content. Can't be null.
	 * @return self. Can be used in chained calls.
	 * @throws IOException on any I/O errors.
	 * @throws NullPointerException any argument is null
	 */
	default T download(T in) throws IOException, NullPointerException {
		return download(Piece.of(0, 0, getWidth(), getHeight()), in);
	}
	
	/**
	 * <p>Upload matrix content to target. Used the only method in {@linkplain DataOutput} target due to matrix content type.
	 * Complex numbers treated as two sequential real values.</p>
	 * @param piece piece to upload content from. Can't be null.
	 * @param out target to upload content. Can't be null.
	 * @return self. Can be used in chained calls.
	 * @throws IOException on any I/O errors.
	 * @throws NullPointerException any argument is null
	 */
	T upload(Piece piece, DataOutput out) throws IOException, NullPointerException;
	/**
	 * <p>Upload matrix content to target. Used the only method in {@linkplain DataOutput} target due to matrix content type.
	 * Complex numbers treated as two sequential real values.</p>
	 * @param out target to upload content. Can't be null.
	 * @return self. Can be used in chained calls.
	 * @throws IOException on any I/O errors.
	 * @throws NullPointerException any argument is null
	 */
	default T upload(DataOutput out) throws IOException, NullPointerException {
		return upload(Piece.of(0, 0, getWidth(), getHeight()), out);
	}

	/**
	 * <p>Upload matrix content to another matrix.</p>
	 * @param piece piece to download content both from and to. Can't be null.
	 * @param out another matrix to upload content. Can't be null.
	 * @return self. Can be used in chained calls.
	 * @throws IOException on any I/O errors.
	 * @throws NullPointerException any argument is null
	 */
	T upload(Piece piece, T out) throws IOException, NullPointerException;
	/**
	 * <p>Upload matrix content to another matrix.</p>
	 * @param out another matrix to upload content. Can't be null.
	 * @return self. Can be used in chained calls.
	 * @throws IOException on any I/O errors.
	 * @throws NullPointerException any argument is null
	 */
	default T upload(T out) throws IOException, NullPointerException {
		return upload(Piece.of(0, 0, getWidth(), getHeight()), out);
	}

	/**
	 * <p>Crop matrix content</p>
	 * @param piece piece to crop matrix content. Can't be null
	 * @return new matrix with cropped content. Can't be null
	 * @throws CalculationException on any calculation errors
	 * @throws NullPointerException any argument is null
	 */
	T crop(Piece piece) throws CalculationException, NullPointerException;
	
	/**
	 * <p>Scan matrix content and process/replace each element with callback method</p>
	 * @param piece piece to process content. Can't be null
	 * @param callback callback to process content. Can't be null.
	 * @return self. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null.
	 * @throws IllegalArgumentException callback type doesn't correlate with matrix content type
	 */
	T apply(Piece piece, ApplyCallback callback) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Scan matrix content and process/replace each element with callback method</p>
	 * @param callback callback to process content. Can't be null.
	 * @return self. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null.
	 * @throws IllegalArgumentException callback type doesn't correlate with matrix content type
	 */
	default T apply(ApplyCallback callback) throws CalculationException, NullPointerException, IllegalArgumentException {
		return apply(Piece.of(0, 0, getWidth(), getHeight()), callback);
	}

	/**
	 * <p>Cast matrix content type. Cast from real to complex treated real as complex with zero image part. Cast from complex to real
	 * treated real as module of complex exponent. Cast to bit treated zero values ad zero, any other values as one.</p>
	 * @param type new matrix content type to cast. Can't be null.
	 * @return new matrix with converted content. Can't be null
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null.
	 */
	BaseMatrix<?> cast(ContentType type) throws CalculationException, NullPointerException;
	/**
	 * <p>Cast internal matrix format type.</p> 
	 * @param type new internal matrix format to cast. Can't be null.
	 * @return new matrix with converted content. Can't be null
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null.
	 */
	BaseMatrix<?> cast(FormatType type) throws CalculationException, NullPointerException;
	/**
	 * <p>Cast matrix location type.</p> 
	 * @param type new matrix location type to cast. Can't be null.
	 * @return new matrix with converted content. Can't be null
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null.
	 */
	BaseMatrix<?> cast(StoreType type) throws CalculationException, NullPointerException;
	
	/**
	 * <p>Add matrices.</p>
	 * @param another matrix to add. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException matrix type or dimensions are incompatible.
	 */
	T add(T another) throws CalculationException, NullPointerException, IllegalArgumentException;
	
	/**
	 * <p>Add matrix content with scalar operand</p>
	 * @param another operand to add. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException operand type is incompatible.
	 */
	T add(Operand another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Subtract matrices.</p>
	 * @param another matrix to subtract it from current one. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException matrix type or dimensions are incompatible.
	 */
	T subtract(T another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Subtract matrix content with scalar operand</p>
	 * @param another operand to subtract it from current matrix. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException operand type is incompatible.
	 */
	T subtract(Operand another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Subtract matrices.</p>
	 * @param another matrix to subtract current from it. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException matrix type or dimensions are incompatible.
	 */
	T subtractRev(T another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Subtract scalar operand with matrix content</p>
	 * @param another operand to subtract current matrix from it. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException operand type is incompatible.
	 */
	T subtractRev(Operand another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Multiply matrices.</p>
	 * @param another matrix to multiply it with current. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException matrix type or dimensions are incompatible.
	 */
	T mul(T another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Multiply and transpose matrices.</p>
	 * @param another matrix to multiply it with current. Can't be null.
	 * @return new calculated and transposed matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException matrix type or dimensions are incompatible.
	 */
	T mulT(T another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Multiply matrices.</p>
	 * @param another matrix to multiply current with it. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException matrix type or dimensions are incompatible.
	 */
	T mulRev(T another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Multiply and transpose matrices.</p>
	 * @param another matrix to multiply current with it. Can't be null.
	 * @return new calculated and transposed matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException matrix type or dimensions are incompatible.
	 */
	T mulRevT(T another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Multiply matrices by Hadamard.</p>
	 * @param another matrix to multiply current with it. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException matrix type or dimensions are incompatible.
	 */
	T mulHadamard(T another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Multiply matrices by Kroneker.</p>
	 * @param another matrix to multiply current with it. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException matrix type or dimensions are incompatible.
	 */
	T mulKroneker(T another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Multiply matrices by Kroneker.</p>
	 * @param another matrix to multiply it with current. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException matrix type or dimensions are incompatible.
	 */
	T mulKronekerRev(T another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Multiply matrix with scalar operand</p>
	 * @param another operand to multiply matrix content with. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException operand type is incompatible.
	 */
	T mul(Operand another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Divide matrix with scalar operand</p>
	 * @param another operand to divide matrix content with it. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException operand type is incompatible.
	 */
	T div(Operand another) throws CalculationException, NullPointerException, IllegalArgumentException;
	/**
	 * <p>Divide scalar operand with matrix</p>
	 * @param another operand to divide it with matrix content. Can't be null.
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 * @throws IllegalArgumentException operand type is incompatible.
	 */
	T divRev(Operand another) throws CalculationException, NullPointerException, IllegalArgumentException;
	
	/**
	 * <p>Transpose matrix</p>
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 */
	T transpose() throws CalculationException;
	/**
	 * <p>Invert matrix</p>
	 * @return new calculated matrix. Can be used in chained calls.
	 * @throws CalculationException on any calculation errors.
	 * @throws IllegalStateException matrix is not square
	 */
	T invert() throws CalculationException, IllegalStateException;
	/**
	 * <p>Calculate matrix determinant</p>
	 * @return determinant calculated. Can't be null.
	 * @throws CalculationException on any calculation errors.
	 * @throws IllegalStateException matrix is not square
	 */
	Operand det() throws CalculationException, IllegalStateException;
	/**
	 * <p>Calculate matrix track</p>
	 * @return track calculated. Can't be null
	 * @throws CalculationException on any calculation errors.
	 * @throws IllegalStateException matrix is not square
	 */
	Operand track() throws CalculationException, IllegalStateException;

	/**
	 * <p>Calculate aggregate functions. Due to aggregate direction returns matrix with 1xN, Nx1 or 1x1 size.</p>
	 * @param piece piece to aggregate content. Can't be null.
	 * @param dir direction to aggregate content. Can't be null.
	 * @param type aggregation type. Can't be null.
	 * @return new matrix with aggregation result.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 */
	T aggregate(Piece piece, AggregateDirection dir, AggregateType type) throws CalculationException, NullPointerException;
	
	/**
	 * <p>Calculate aggregate functions. Due to aggregate direction returns matrix with 1xN, Nx1 or 1x1 size.</p>
	 * @param dir direction to aggregate content. Can't be null.
	 * @param type aggregation type. Can't be null.
	 * @return new matrix with aggregation result.
	 * @throws CalculationException on any calculation errors.
	 * @throws NullPointerException any argument is null
	 */
	default T aggregate(AggregateDirection dir, AggregateType type) throws CalculationException {
		return aggregate(Piece.of(0, 0, getWidth(), getHeight()), dir, type);
	}
}
