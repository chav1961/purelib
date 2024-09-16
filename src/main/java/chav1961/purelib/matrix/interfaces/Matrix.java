package chav1961.purelib.matrix.interfaces;

/**
 * <p>This interface describes matrices. All implementations of this interface must follow some conventions described below.</p>
 * <ul>
 * <li>Internal matrix representation must be one-dimensional array to increase performance.</p></li>
 * <li>All implementations must support asynchronous/parallel operations inside to increase performance. Start of asynchronous/parallel operations is any 
 * call to arithmetic operations and end of asynchronous/parallel operations is calling {@linkplain #done()} method. Neither getting nor setting matrix values
 * can't be executed before all the asynchronous/parallel operations will be completed, otherwise {@linkplain IllegalStateException} must be fired</li>
 * </ul>
 * <p>Matrix implementation is not required to be thread-safe</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public interface Matrix extends AutoCloseable, Cloneable {
	/**
	 * <p>This enumeration describes matrix content type</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static enum Type {
		/**
		 * <p>Matrix content is bit (zeroes and ones). This type is used for "connection matrices" only</p>
		 */
		BIT(1, 1, "BL"),
		/**
		 * <p>Matrix content is real int</p> 
		 */
		REAL_INT(1, 4*8, "RI"),
		/**
		 * <p>MAtrix content is real long</p>
		 */
		REAL_LONG(1, 8*8, "RL"),
		/**
		 * <p>Matrix content is real float</p>
		 */
		REAL_FLOAT(1, 4*8, "RF"),
		/**
		 * <p>Matrix content is complex float</p>
		 */
		COMPLEX_FLOAT(2, 4*8, "CF"),
		/**
		 * <p>Matrix content is real double</p> 
		 */
		REAL_DOUBLE(1, 8*8, "RD"),
		/**
		 * <p>Matrix content is complex double</p>
		 */
		COMPLEX_DOUBLE(2, 8*8, "CD");
		
		private final int		numberOfItems;
		private final int		itemSize;
		private final String	suffix;
		
		private Type(final int numberOfItems, final int itemSize, final String suffix) {
			this.numberOfItems = numberOfItems;
			this.itemSize = itemSize;
			this.suffix = suffix;
		}
		
		/**
		 * <p>Get number of items for every value.
		 * @return 1 for bit and real content, 2 for complex content
		 */
		public int getNumberOfItems() {
			return numberOfItems;
		}
		
		/**
		 * <p>Get item size in bits</p>
		 * @return item size in bits
		 */
		public int getItemSize() {
			return itemSize;
		}
		
		/**
		 * <p>Get program suffix associated with the type</p>
		 * @return program suffix associated. Can't be null or enpty</p>
		 */
		public String getProgramSuffix() {
			return suffix;
		}
	}
	
	/**
	 * <p>This enumeration describes aggregate direction for {@linkplain Matrix#aggregate(AggregateDirection, AggregateType)} method</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static enum AggregateDirection {
		/**
		 * <p>Aggregate content by rows</p>
		 */
		ByRows,
		/**
		 * <p>Aggregate content by columns</p>
		 */
		ByColumns,
		/**
		 * <p>Aggregate content by whole matrix</p>
		 */
		Total
	}
	
	/**
	 * <p>This enumeration describes aggregate function for {@linkplain Matrix#aggregate(AggregateDirection, AggregateType)} method</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static enum AggregateType {
		/**
		 * <p>Calculate aggregate sum</p>
		 */
		Sum,
		/**
		 * <p>Calculate aggregate average</p>
		 */
		Avg, 
		/**
		 * <p>Find minimum value</p>
		 */
		Min,
		/**
		 * <p>Find maximum value</p>
		 */
		Max
	}

	/**
	 * <p>This interface describes piece of the matrix to extract/assign</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static interface Piece {
		/**
		 * <p>Get piece top</p>
		 * @return piece top
		 */
		int getTop();
		/**
		 * <p>Get piece left</p>
		 * @return piece left
		 */
		int getLeft();
		/**
		 * <p>Get piece width</p>
		 * @return piece width
		 */
		int getWidth();
		/**
		 * <p>Get piece height</p>
		 * @return piece height
		 */
		int getHeight();
		
		/**
		 * <p>Create piece instance</p>
		 * @param top piece top. Can't be negative
		 * @param left piece left. Can't be negative
		 * @param height piece height. Must be greater than 0
		 * @param width piece width. Must be greater than 0
		 * @return piece created. Can't be null
		 * @throws IllegalArgumentException if any constraint failed
		 */
		public static Piece of(final int top, final int left, final int height, final int width) throws IllegalArgumentException {
			if (top < 0) {
				throw new IllegalArgumentException("Negative top value ["+top+"]");
			}
			else if (left < 0) {
				throw new IllegalArgumentException("Negative left value ["+left+"]");
			}
			else if (height < 0) {
				throw new IllegalArgumentException("Negative height value ["+height+"]");
			}
			else if (width < 0) {
				throw new IllegalArgumentException("Negative width value ["+width+"]");
			}
			else {
				return new Piece() {
					@Override
					public int getWidth() {
						return width;
					}
					
					@Override
					public int getTop() {
						return top;
					}
					
					@Override
					public int getLeft() {
						return left;
					}
					
					@Override
					public int getHeight() {
						return height;
					}
					
					@Override
					public String toString() {
						return "Piece[top="+top+",left="+left+",height="+height+",width="+width+"]";
					}
				};
			}
		}
	}

	/**
	 * <p>This interface is used to apply changes for bit matrix. Can be used in lambdas.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public static interface ApplyBit {
		/**
		 * <p>Apply changes for the given item</p>
		 * @param row item row
		 * @param col item column
		 * @param value item value
		 * @return item value changed
		 */
		boolean apply(int row, int col, boolean value);
	}
	
	/**
	 * <p>This interface is used to apply changes for int matrix. Can be used in lambdas.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public static interface ApplyInt {
		/**
		 * <p>Apply changes for the given item</p>
		 * @param row item row
		 * @param col item column
		 * @param value item value
		 * @return item value changed
		 */
		int apply(int row, int col, int value);
	}

	/**
	 * <p>This interface is used to apply changes for long matrix. Can be used in lambdas.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public static interface ApplyLong {
		/**
		 * <p>Apply changes for the given item</p>
		 * @param row item row
		 * @param col item column
		 * @param value item value
		 * @return item value changed
		 */
		long apply(int row, int col, long value);
	}

	/**
	 * <p>This interface is used to apply changes for float matrix. Can be used in lambdas.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public static interface ApplyFloat {
		/**
		 * <p>Apply changes for the given item</p>
		 * @param row item row
		 * @param col item column
		 * @param value item value
		 * @return item value changed
		 */
		float apply(int row, int col, float value);
	}

	/**
	 * <p>This interface is used to apply changes for double matrix. Can be used in lambdas.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public static interface ApplyDouble {
		/**
		 * <p>Apply changes for the given item</p>
		 * @param row item row
		 * @param col item column
		 * @param value item value
		 * @return item value changed
		 */
		double apply(int row, int col, double value);
	}

	/**
	 * <p>This interface is used to apply changes for complex float matrix. Can be used in lambdas.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public static interface ApplyFloat2  {
		/**
		 * <p>Apply changes for the given item</p>
		 * @param row item row
		 * @param col item column
		 * @param real real part of item value
		 * @param image image part of item value
		 * @param result place to store changed result. Can't be null and will have at least 2 elements
		 */
		void apply(int row, int col, float real, float image, float[] result);
	}

	/**
	 * <p>This interface is used to apply changes for complex double matrix. Can be used in lambdas.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public static interface ApplyDouble2  {
		/**
		 * <p>Apply changes for the given item</p>
		 * @param row item row
		 * @param col item column
		 * @param real real part of item value
		 * @param image image part of item value
		 * @param result place to store changed result. Can't be null and will have at least 2 elements
		 */
		void apply(int row, int col, double real, double image, double[] result);
	}

	/**
	 * <p>Clone current matrix</p>
	 * @return matrix cloned
	 * @throws CloneNotSupportedException if clone not supported
	 */
	public Object clone() throws CloneNotSupportedException;
	
	@Override
	public void close() throws RuntimeException;
	
	/**
	 * <p>Get matrix type.</p>
	 * @return matrix type. Can't be null
	 */
	public Type getType();
	
	/**
	 * <p>Get number of rows</p>
	 * @return number of rows
	 */
	public int numberOfRows();
	
	/**
	 * <p>Get number of columns</p>
	 * @return number of columns
	 */
	public int numberOfColumns();
	
	/**
	 * <p>Test matrix equality</p>
	 * @param another matrix to test
	 * @return true if matrix type, matrix size and matrix content are identical, false otherwise
	 */
	public boolean deepEquals(final Matrix another);
	
	/**
	 * <p>Extract matrix content as integer array.</p>
	 * @return matrix content Can't be null or empty. Complex matrices will return 2 sequential elements for every source item. Value conversion will be executed if required. 
	 */
	public int[] extractInts();
	
	/**
	 * <p>Extract matrix content as integer array.</p>
	 * @param piece piece to extract content from. Can't be null
	 * @return matrix content Can't be null or empty. Complex matrices will return 2 sequential elements for every source item. Value conversion will be executed if required. 
	 */
	public int[] extractInts(Piece piece);
	
	/**
	 * <p>Extract matrix content as long array.</p>
	 * @return matrix content Can't be null or empty. Complex matrices will return 2 sequential elements for every source item. Value conversion will be executed if required. 
	 */
	public long[] extractLongs();
	
	/**
	 * <p>Extract matrix content as long array.</p>
	 * @param piece piece to extract content from. Can't be null
	 * @return matrix content Can't be null or empty. Complex matrices will return 2 sequential elements for every source item. Value conversion will be executed if required. 
	 */
	public long[] extractLongs(Piece piece);

	/**
	 * <p>Extract matrix content as float array.</p>
	 * @return matrix content Can't be null or empty. Complex matrices will return 2 sequential elements for every source item. Value conversion will be executed if required. 
	 */
	public float[] extractFloats();
	
	/**
	 * <p>Extract matrix content as float array.</p>
	 * @param piece piece to extract content from. Can't be null
	 * @return matrix content Can't be null or empty. Complex matrices will return 2 sequential elements for every source item. Value conversion will be executed if required. 
	 */
	public float[] extractFloats(Piece piece);
	
	/**
	 * <p>Extract matrix content as double array.</p>
	 * @return matrix content Can't be null or empty. Complex matrices will return 2 sequential elements for every source item. Value conversion will be executed if required. 
	 */
	public double[] extractDoubles();
	
	/**
	 * <p>Extract matrix content as double array.</p>
	 * @param piece piece to extract content from. Can't be null
	 * @return matrix content Can't be null or empty. Complex matrices will return 2 sequential elements for every source item. Value conversion will be executed if required. 
	 */
	public double[] extractDoubles(Piece piece);

	/**
	 * <p>Assign integer content to matrix. Content will be filled from left to right and from top to bottom. If content size is too short, only first matrix items will be replaced. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats zero values as 0, and non-zero values as 1.
	 * Complex matrices will pack 2 sequential elements into one matrix item.</p> 
	 * @param content content to fill. Can't be null
	 * @return this matrix. Can't be null.
	 */
	public Matrix assign(int... content);
	
	/**
	 * <p>Assign integer content to matrix. Content will be filled from left to right and from top to bottom. If content size is too short, only first matrix items will be replaced. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats zero values as 0, and non-zero values as 1.
	 * Complex matrices will pack 2 sequential elements into one matrix item.</p> 
	 * @param piece piece to fill content into. Can't be null
	 * @param content content to fill. Can't be null
	 * @return this matrix. Can't be null.
	 */
	public Matrix assign(Piece piece, int... content);
	
	/**
	 * <p>Assign long content to matrix. Content will be filled from left to right and from top to bottom. If content size is too short, only first matrix items will be replaced. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats zero values as 0, and non-zero values as 1.
	 * Complex matrices will pack 2 sequential elements into one matrix item.</p> 
	 * @param content content to fill. Can't be null
	 * @return this matrix. Can't be null.
	 */
	public Matrix assign(long... content);

	/**
	 * <p>Assign long content to matrix. Content will be filled from left to right and from top to bottom. If content size is too short, only first matrix items will be replaced. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats zero values as 0, and non-zero values as 1.
	 * Complex matrices will pack 2 sequential elements into one matrix item.</p> 
	 * @param piece piece to fill content into. Can't be null
	 * @param content content to fill. Can't be null
	 * @return this matrix. Can't be null.
	 */
	public Matrix assign(Piece piece, long... content);
	
	/**
	 * <p>Assign float content to matrix. Content will be filled from left to right and from top to bottom. If content size is too short, only first matrix items will be replaced. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats zero values as 0, and non-zero values as 1.
	 * Complex matrices will pack 2 sequential elements into one matrix item.</p> 
	 * @param content content to fill. Can't be null
	 * @return this matrix. Can't be null.
	 */
	public Matrix assign(float... content);
	
	/**
	 * <p>Assign float content to matrix. Content will be filled from left to right and from top to bottom. If content size is too short, only first matrix items will be replaced. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats zero values as 0, and non-zero values as 1.
	 * Complex matrices will pack 2 sequential elements into one matrix item.</p> 
	 * @param piece piece to fill content into. Can't be null
	 * @param content content to fill. Can't be null
	 * @return this matrix. Can't be null.
	 */
	public Matrix assign(Piece piece, float... content);

	/**
	 * <p>Assign double content to matrix. Content will be filled from left to right and from top to bottom. If content size is too short, only first matrix items will be replaced. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats zero values as 0, and non-zero values as 1.
	 * Complex matrices will pack 2 sequential elements into one matrix item.</p> 
	 * @param content content to fill. Can't be null
	 * @return this matrix. Can't be null.
	 */
	public Matrix assign(double... content);
	
	/**
	 * <p>Assign double content to matrix. Content will be filled from left to right and from top to bottom. If content size is too short, only first matrix items will be replaced. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats zero values as 0, and non-zero values as 1.
	 * Complex matrices will pack 2 sequential elements into one matrix item.</p> 
	 * @param piece piece to fill content into. Can't be null
	 * @param content content to fill. Can't be null
	 * @return this matrix. Can't be null.
	 */
	public Matrix assign(Piece piece, double... content);
	
	/**
	 * <p>Assign matrix content to matrix. Content will be filled from left to right and from top to bottom. If content size is too short, only first matrix items will be replaced. Extra
	 * values will be truncated without any notice. Conversion will be executed if required</p> 
	 * @param content matrix to fill content from. Can't be null
	 * @return this matrix. Can't be null.
	 */
	public Matrix assign(Matrix content);

	/**
	 * <p>Assign matrix content to matrix. Content will be filled from left to right and from top to bottom. If content size is too short, only first matrix items will be replaced. Extra
	 * values will be truncated without any notice. Conversion will be executed if required</p> 
	 * @param piece piece to fill content into. Can't be null
	 * @param content matrix to fill content from. Can't be null
	 * @return this matrix. Can't be null.
	 */
	public Matrix assign(Piece piece, Matrix content);
	
	/**
	 * <p>Fill matrix content with integer value typed. Conversion will be executed if required. Bit matrix treats zero values as o, non-zero values as 1. 
	 * Complex matrices treats this value as complex number with zero image part</p>
	 * @param value value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(int value);
	
	/**
	 * <p>Fill matrix content with integer value typed. Conversion will be executed if required. Bit matrix treats zero values as o, non-zero values as 1. 
	 * Complex matrices treats this value as complex number with zero image part</p>
	 * @param piece piece to fill value into. Can't be null
	 * @param value value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(Piece piece, int value);
	
	/**
	 * <p>Fill matrix content with long value typed. Conversion will be executed if required. Bit matrix treats zero values as o, non-zero values as 1. 
	 * Complex matrices treats this value as complex number with zero image part</p>
	 * @param value value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(long value);
	
	/**
	 * <p>Fill matrix content with long value typed. Conversion will be executed if required. Bit matrix treats zero values as o, non-zero values as 1. 
	 * Complex matrices treats this value as complex number with zero image part</p>
	 * @param piece piece to fill value into. Can't be null
	 * @param value value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(Piece piece, long value);
	
	/**
	 * <p>Fill matrix content with float value typed. Conversion will be executed if required. Bit matrix treats zero values as o, non-zero values as 1. 
	 * Complex matrices treats this value as complex number with zero image part</p>
	 * @param value value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(float value);

	/**
	 * <p>Fill matrix content with float value typed. Conversion will be executed if required. Bit matrix treats zero values as o, non-zero values as 1. 
	 * Complex matrices treats this value as complex number with zero image part</p>
	 * @param piece piece to fill value into. Can't be null
	 * @param value value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(Piece piece, float value);
	
	/**
	 * <p>Fill matrix content with complex float value typed. Conversion will be executed if required. Both bit and real matrices don't support this method</p>
	 * @param real real value to fill
	 * @param image image value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(float real, float image);
	
	/**
	 * <p>Fill matrix content with complex float value typed. Conversion will be executed if required. Both bit and real matrices don't support this method</p>
	 * @param piece piece to fill value into. Can't be null
	 * @param real real value to fill
	 * @param image image value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(Piece piece, float real, float image);
	
	/**
	 * <p>Fill matrix content with double value typed. Conversion will be executed if required. Bit matrix treats zero values as o, non-zero values as 1. 
	 * Complex matrices treats this value as complex number with zero image part</p>
	 * @param value value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(double value);
	
	/**
	 * <p>Fill matrix content with double value typed. Conversion will be executed if required. Bit matrix treats zero values as o, non-zero values as 1. 
	 * Complex matrices treats this value as complex number with zero image part</p>
	 * @param piece piece to fill value into. Can't be null
	 * @param value value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(Piece piece, double value);
	
	/**
	 * <p>Fill matrix content with complex double value typed. Conversion will be executed if required. Both bit and real matrices don't support this method</p>
	 * @param real real value to fill
	 * @param image image value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(double real, double image);
	
	/**
	 * <p>Fill matrix content with complex double value typed. Conversion will be executed if required. Both bit and real matrices don't support this method</p>
	 * @param piece piece to fill value into. Can't be null
	 * @param real real value to fill
	 * @param image image value to fill
	 * @return this matrix. Can't be null.
	 */
	public Matrix fill(Piece piece, double real, double image);
	
	/**
	 * <p>Cast matrix to type required. Complex matrices can be casted when all the image parts of complex numbers inside them are zeroes only. Bit and real matrices 
	 * will be casted to complex matrices with zero image parts</p> 
	 * @param type target type of matrix casted. Can't be null.
	 * @return new matrix casted. Can't be null
	 */
	public Matrix cast(Type type);	

	/**
	 * <p>Add integer content to matrix content. Content will be added from left to right and from top to bottom. If content size is too short, only first matrix items will be added. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit OR and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to add. Can't be null
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix add(int... content);
	
	/**
	 * <p>Add long content to matrix content. Content will be added from left to right and from top to bottom. If content size is too short, only first matrix items will be added. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit OR and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to add. Can't be null
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix add(long... content);
	
	/**
	 * <p>Add float content to matrix content. Content will be added from left to right and from top to bottom. If content size is too short, only first matrix items will be added. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit OR and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to add. Can't be null
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix add(float... content);
	
	/**
	 * <p>Add double content to matrix content. Content will be added from left to right and from top to bottom. If content size is too short, only first matrix items will be added. Extra
	 * values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit OR and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to add. Can't be null
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix add(double... content);
	
	/**
	 * <p>Add another matrix content to matrix content. Another matrix dimensions must be the same as current ones. Conversion will be executed if required. 
	 * Bit matrix treats this operation as bit OR and treats zero values as 0, and non-zero values as 1. Complex matrices treat bit and real matrices as complex matrices with zero image parts 
	 * of the complex numbers. Real and bit matrices don't support this method for complex matrices.</p> 
	 * @param content content to add. Can't be null
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix add(Matrix content);

	/**
	 * <p>Add integer scalar value to matrix content. Bit matrix treats this operation as fill, and treats zero values as 0, and non-zero values as 1. Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required. </p> 
	 * @param value scalar value to add.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix addValue(int value);
	
	/**
	 * <p>Add long scalar value to matrix content. Bit matrix treats this operation as fill, and treats zero values as 0, and non-zero values as 1. Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required.</p> 
	 * @param value scalar value to add.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix addValue(long value);
	
	/**
	 * <p>Add float scalar value to matrix content. Bit matrix treats this operation as fill, and treats zero values as 0, and non-zero values as 1. Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required.</p> 
	 * @param value scalar value to add.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix addValue(float value);

	/**
	 * <p>Add complex float scalar value to matrix content. Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param real real part of scalar value to add.
	 * @param image image part of scalar value to add.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix addValue(float real, float image);
	
	/**
	 * <p>Add double scalar value to matrix content. Bit matrix treats this operation as fill, and treats zero values as 0, and non-zero values as 1. Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required.</p> 
	 * @param value scalar value to add.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix addValue(double value);

	/**
	 * <p>Add complex double scalar value to matrix content. Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param real real part of scalar value to add.
	 * @param image image part of scalar value to add.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix addValue(double real, double image);

	/**
	 * <p>Subtract integer content from matrix content. Content will be subtracted from left to right and from top to bottom. If content size is too short, only first matrix items will be subtracted.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit MINUS and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to subtract. Can't be null
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtract(int... content);
	
	/**
	 * <p>Subtract long content from matrix content. Content will be subtracted from left to right and from top to bottom. If content size is too short, only first matrix items will be subtracted.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit MINUS and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to subtract. Can't be null
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtract(long... content);
	
	/**
	 * <p>Subtract float content from matrix content. Content will be subtracted from left to right and from top to bottom. If content size is too short, only first matrix items will be subtracted.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit MINUS and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to subtract. Can't be null
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtract(float... content);

	/**
	 * <p>Subtract double content from matrix content. Content will be subtracted from left to right and from top to bottom. If content size is too short, only first matrix items will be subtracted.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit MINUS and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to subtract. Can't be null
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtract(double... content);
	
	/**
	 * <p>Subtract another matrix content from current matrix content. Conversion will be executed if required. 
	 * Bit matrix treats this operation as bit MINUS and treats zero values as 0, and non-zero values as 1. Complex matrices treat bit and real matrices as complex matrices with zero image parts 
	 * of the complex numbers. Real and bit matrices don't support this method for complex matrices.</p> 
	 * @param content content to subtract. Can't be null
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtract(Matrix content);

	/**
	 * <p>Subtract integer scalar value from matrix content. Bit matrix treats this operation as conditional fill (non-zero value clears matrix content, zero remains content unchanged). Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required. </p> 
	 * @param value scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractValue(int value);
	
	/**
	 * <p>Subtract long scalar value from matrix content. Bit matrix treats this operation as conditional fill (non-zero value clears matrix content, zero remains content unchanged). Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required. </p> 
	 * @param value scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractValue(long value);
	
	/**
	 * <p>Subtract float scalar value from matrix content. Bit matrix treats this operation as conditional fill (non-zero value clears matrix content, zero remains content unchanged). Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required. </p> 
	 * @param value scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractValue(float value);
	
	/**
	 * <p>Subtract float scalar value from matrix content. Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param real real part of scalar value to subtract.
	 * @param image image part of scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractValue(float real, float image);
	
	/**
	 * <p>Subtract double scalar value from matrix content. Bit matrix treats this operation as conditional fill (non-zero value clears matrix content, zero remains content unchanged). Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required. </p> 
	 * @param value scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractValue(double value);

	/**
	 * <p>Subtract double scalar value from matrix content. Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param real real part of scalar value to subtract.
	 * @param image image part of scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractValue(double real, double image);
	
	/**
	 * <p>Subtract matrix content from integer content. Content will be subtracted from left to right and from top to bottom. If content size is too short, only first matrix items will be subtracted.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit MINUS and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to subtract from. Can't be null
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractFrom(int... content);
	
	/**
	 * <p>Subtract matrix content from long content. Content will be subtracted from left to right and from top to bottom. If content size is too short, only first matrix items will be subtracted.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit MINUS and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to subtract from. Can't be null
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractFrom(long... content);

	/**
	 * <p>Subtract matrix content from float content. Content will be subtracted from left to right and from top to bottom. If content size is too short, only first matrix items will be subtracted.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit MINUS and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to subtract from. Can't be null
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractFrom(float... content);

	/**
	 * <p>Subtract matrix content from double content. Content will be subtracted from left to right and from top to bottom. If content size is too short, only first matrix items will be subtracted.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit MINUS and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p> 
	 * @param content content to subtract from. Can't be null
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractFrom(double... content);

	/**
	 * <p>Subtract current matrix content from another matrix content. Conversion will be executed if required. 
	 * Bit matrix treats this operation as bit MINUS and treats zero values as 0, and non-zero values as 1. Complex matrices treat bit and real matrices as complex matrices with zero image parts 
	 * of the complex numbers. Real and bit matrices don't support this method for complex matrices.</p> 
	 * @param content content to subtract. Can't be null
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractFrom(Matrix content);

	/**
	 * <p>Subtract matrix content from integer scalar value. Bit matrix treats this operation as NOT (non-zero value inverts matrix content, zero remains content unchanged). Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required. </p> 
	 * @param value scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractFromValue(int value);
	
	/**
	 * <p>Subtract matrix content from long scalar value. Bit matrix treats this operation as NOT (non-zero value inverts matrix content, zero remains content unchanged). Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required. </p> 
	 * @param value scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractFromValue(long value);

	/**
	 * <p>Subtract matrix content from float scalar value. Bit matrix treats this operation as NOT (non-zero value inverts matrix content, zero remains content unchanged). Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required. </p> 
	 * @param value scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractFromValue(float value);
	
	/**
	 * <p>Subtract matrix content from float scalar value. Bit matrix treats this operation as NOT (non-zero value inverts matrix content, zero remains content unchanged). 
	 * Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param real real part of scalar value to subtract.
	 * @param image image part of scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractFromValue(float real, float image);

	/**
	 * <p>Subtract matrix content from double scalar value. Bit matrix treats this operation as NOT (non-zero value inverts matrix content, zero remains content unchanged). Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required. </p> 
	 * @param value scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractFromValue(double value);

	/**
	 * <p>Subtract matrix content from double scalar value. Bit matrix treats this operation as NOT (non-zero value inverts matrix content, zero remains content unchanged). 
	 * Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param real real part of scalar value to subtract.
	 * @param image image part of scalar value to subtract.
	 * @return new matrix with subtraction. Can't be null
	 */
	public Matrix subtractFromValue(double real, double image);
	
	public Matrix mul(Matrix content);
	public Matrix mulFrom(Matrix content);
	
	/**
	 * <p>Multiply integer scalar value with matrix content. Bit matrix treats this operation as AND, and treats zero values as 0, and non-zero values as 1. Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required.</p> 
	 * @param value scalar value to multiply.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix mulValue(int value);

	/**
	 * <p>Multiply long scalar value with matrix content. Bit matrix treats this operation as AND, and treats zero values as 0, and non-zero values as 1. Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required.</p> 
	 * @param value scalar value to multiply.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix mulValue(long value);
	
	/**
	 * <p>Multiply float scalar value with matrix content. Bit matrix treats this operation as AND, and treats zero values as 0, and non-zero values as 1. Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required.</p> 
	 * @param value scalar value to multiply.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix mulValue(float value);

	/**
	 * <p>Multiply float scalar value with matrix content. Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param real real part of scalar value to multiply. 
	 * @param image image part of scalar value to multiply.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix mulValue(float real, float image);
	
	/**
	 * <p>Multiply double scalar value with matrix content. Bit matrix treats this operation as AND, and treats zero values as 0, and non-zero values as 1. Complex matrix treats this value
	 * as complex number with zero image part. Conversion will be executed if required.</p> 
	 * @param value scalar value to multiply.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix mulValue(double value);

	/**
	 * <p>Multiply double scalar value with matrix content. Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param real real part of scalar value to multiply. 
	 * @param image image part of scalar value to multiply.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix mulValue(double real, double image);

	/**
	 * <p>Divide matrix content with integer scalar value. Complex matrix treats this value as complex number with zero image part. 
	 * Conversion will be executed if required. Bit matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divValue(int value);
	
	/**
	 * <p>Divide matrix content with long scalar value. Complex matrix treats this value as complex number with zero image part. 
	 * Conversion will be executed if required. Bit matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divValue(long value);

	/**
	 * <p>Divide matrix content with float scalar value. Complex matrix treats this value as complex number with zero image part. 
	 * Conversion will be executed if required. Bit matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divValue(float value);
	
	/**
	 * <p>Divide matrix content with float scalar value. Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divValue(float real, float image);
	
	/**
	 * <p>Divide matrix content with double scalar value. Complex matrix treats this value as complex number with zero image part. 
	 * Conversion will be executed if required. Bit matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divValue(double value);

	/**
	 * <p>Divide matrix content with double scalar value. Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divValue(double real, double image);

	/**
	 * <p>Divide integer scalar value with matrix content. Complex matrix treats this value as complex number with zero image part. 
	 * Conversion will be executed if required. Bit matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divFromValue(int value);
	
	/**
	 * <p>Divide long scalar value with matrix content. Complex matrix treats this value as complex number with zero image part. 
	 * Conversion will be executed if required. Bit matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divFromValue(long value);
	
	/**
	 * <p>Divide float scalar value with matrix content. Complex matrix treats this value as complex number with zero image part. 
	 * Conversion will be executed if required. Bit matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divFromValue(float value);

	/**
	 * <p>Divide float scalar value with matrix content. Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divFromValue(float real, float image);
	
	/**
	 * <p>Divide double scalar value with matrix content. Complex matrix treats this value as complex number with zero image part. 
	 * Conversion will be executed if required. Bit matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divFromValue(double value);

	/**
	 * <p>Divide double scalar value with matrix content. Conversion will be executed if required. Bit and real matrices don't support this operation.</p> 
	 * @param value scalar value to divide.
	 * @return new matrix with sum. Can't be null
	 */
	public Matrix divFromValue(double real, double image);
	
	/**
	 * <p>Multiply current matrix content with integer content element-by-element. Content will be multiplied from left to right and from top to bottom. If content size is too short, only first matrix items will be multiplied.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit AND and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to multiply. Can't be null
	 * @return new matrix with multiplication. Can't be null
	 */
	public Matrix mulHadamard(int... content);
	
	/**
	 * <p>Multiply current matrix content with long content element-by-element. Content will be multiplied from left to right and from top to bottom. If content size is too short, only first matrix items will be multiplied.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit AND and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to multiply. Can't be null
	 * @return new matrix with multiplication. Can't be null
	 */
	public Matrix mulHadamard(long... content);

	/**
	 * <p>Multiply current matrix content with float content element-by-element. Content will be multiplied from left to right and from top to bottom. If content size is too short, only first matrix items will be multiplied.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit AND and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to multiply. Can't be null
	 * @return new matrix with multiplication. Can't be null
	 */
	public Matrix mulHadamard(float... content);

	/**
	 * <p>Multiply current matrix content with double content element-by-element. Content will be multiplied from left to right and from top to bottom. If content size is too short, only first matrix items will be multiplied.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix treats this operation as bit AND and treats zero values as 0, and non-zero values as 1.
	 * Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to multiply. Can't be null
	 * @return new matrix with multiplication. Can't be null
	 */
	public Matrix mulHadamard(double... content);
	
	public Matrix mulHadamard(Matrix content);

	/**
	 * <p>Divide current matrix content with integer content element-by-element. Content will be divided from left to right and from top to bottom. If content size is too short, only first matrix items will be divided.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix doesn't support this operation. Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to divide. Can't be null
	 * @return new matrix with division. Can't be null
	 */
	public Matrix mulInvHadamard(int... content);

	/**
	 * <p>Divide current matrix content with long content element-by-element. Content will be divided from left to right and from top to bottom. If content size is too short, only first matrix items will be divided.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix doesn't support this operation. Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to divide. Can't be null
	 * @return new matrix with division. Can't be null
	 */
	public Matrix mulInvHadamard(long... content);
	
	/**
	 * <p>Divide current matrix content with float content element-by-element. Content will be divided from left to right and from top to bottom. If content size is too short, only first matrix items will be divided.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix doesn't support this operation. Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to divide. Can't be null
	 * @return new matrix with division. Can't be null
	 */
	public Matrix mulInvHadamard(float... content);

	/**
	 * <p>Divide current matrix content with double content element-by-element. Content will be divided from left to right and from top to bottom. If content size is too short, only first matrix items will be divided.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix doesn't support this operation. Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to divide. Can't be null
	 * @return new matrix with division. Can't be null
	 */
	public Matrix mulInvHadamard(double... content);
	
	public Matrix mulInvHadamard(Matrix content);

	/**
	 * <p>Divide integer content with current matrix content element-by-element. Content will be divided from left to right and from top to bottom. If content size is too short, only first matrix items will be divided.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix doesn't support this operation. Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to divide. Can't be null
	 * @return new matrix with division. Can't be null
	 */
	public Matrix mulInvFromHadamard(int... content);
	
	/**
	 * <p>Divide long content with current matrix content element-by-element. Content will be divided from left to right and from top to bottom. If content size is too short, only first matrix items will be divided.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix doesn't support this operation. Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to divide. Can't be null
	 * @return new matrix with division. Can't be null
	 */
	public Matrix mulInvFromHadamard(long... content);

	/**
	 * <p>Divide float content with current matrix content element-by-element. Content will be divided from left to right and from top to bottom. If content size is too short, only first matrix items will be divided.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix doesn't support this operation. Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to divide. Can't be null
	 * @return new matrix with division. Can't be null
	 */
	public Matrix mulInvFromHadamard(float... content);
	
	/**
	 * <p>Divide double content with current matrix content element-by-element. Content will be divided from left to right and from top to bottom. If content size is too short, only first matrix items will be divided.
	 * Extra values will be truncated without any notice. Conversion will be executed if required. Bit matrix doesn't support this operation. Complex matrix treats 2 sequential elements as one complex number.</p>
	 * @param content content to divide. Can't be null
	 * @return new matrix with division. Can't be null
	 */
	public Matrix mulInvFromHadamard(double... content);
	
	public Matrix mulInvFromHadamard(Matrix content);
	
	public Matrix tensorMul(Matrix content);
	public Matrix tensorMulFrom(Matrix content);

	/**
	 * <p>Calculate matrix inversion. Bit, integer and long matrices don't support this operation. Matrix must be square</p>
	 * @return new inverted matrix. Can't be null
	 */
	public Matrix invert();
	
	/**
	 * <p>Transpose matrix</p> 
	 * @return new transposed matrix. Can't be null.
	 */
	public Matrix transpose();
	
	public Matrix aggregate(AggregateDirection dir, AggregateType aggType);
	
	/**
	 * <p>Calculate matrix determinant. Bit and complex matrices don't support this operation</p>
	 * @return determinant calculated. Can't be null.
	 * @see #det2()
	 */
	public Number det();
	
	/**
	 * <p>Calculate matrix track. Bit and complex matrices don't support this operation</p>
	 * @return track calculated. Can't be null.
	 * @see #track2()
	 */
	public Number track();
	
	/**
	 * <p>Calculate complex matrix determinant</p>
	 * @return complex matrix determinant. Can't be null and contains exactly 2 elements (x[0] - real part, x[1] - image part)
	 * @see #det() 
	 */
	public Number[] det2();
	
	/**
	 * <p>Calculate complex matrix track</p>
	 * @return complex matrix track. Can't be null and contains exactly 2 elements (x[0] - real part, x[1] - image part)
	 * @see #track() 
	 */
	public Number[] track2();

	/**
	 * <p>Apply changes for bit matrix content.</p>
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(ApplyBit callback);
	
	/**
	 * <p>Apply changes for bit matrix content.</p>
	 * @param piece matrix piece to make changes for. Can't be null
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(Piece piece, ApplyBit callback);
	
	/**
	 * <p>Apply changes for integer matrix content.</p>
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(ApplyInt callback);
	
	/**
	 * <p>Apply changes for integer matrix content.</p>
	 * @param piece matrix piece to make changes for. Can't be null
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(Piece piece, ApplyInt callback);

	/**
	 * <p>Apply changes for long matrix content.</p>
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(ApplyLong callback);
	
	/**
	 * <p>Apply changes for long matrix content.</p>
	 * @param piece matrix piece to make changes for. Can't be null
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(Piece piece, ApplyLong callback);
	
	/**
	 * <p>Apply changes for float real matrix content.</p>
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(ApplyFloat callback);
	
	/**
	 * <p>Apply changes for float real matrix content.</p>
	 * @param piece matrix piece to make changes for. Can't be null
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(Piece piece, ApplyFloat callback);
	
	/**
	 * <p>Apply changes for double real matrix content.</p>
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(ApplyDouble callback);

	/**
	 * <p>Apply changes for double real matrix content.</p>
	 * @param piece matrix piece to make changes for. Can't be null
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(Piece piece, ApplyDouble callback);

	/**
	 * <p>Apply changes for float complex matrix content.</p>
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(ApplyFloat2 callback);

	/**
	 * <p>Apply changes for float complex matrix content.</p>
	 * @param piece matrix piece to make changes for. Can't be null
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(Piece piece, ApplyFloat2 callback);
	
	/**
	 * <p>Apply changes for double complex matrix content.</p>
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(ApplyDouble2 callback);

	/**
	 * <p>Apply changes for double complex matrix content.</p>
	 * @param piece matrix piece to make changes for. Can't be null
	 * @param callback callback to process each matrix element. Can't be null
	 * @return new matrix with data processed. Can't be null
	 */
	public Matrix apply(Piece piece, ApplyDouble2 callback);
	
	/**
	 * <p>Convert matrix content to human-readable format</p>
	 * @return
	 */
	public String toHumanReadableString();
	
	/**
	 * <p>Completes all internal operations (for example, multi-thread calculations).</p> 
	 * @return this matrix
	 */
	public Matrix done();
}
