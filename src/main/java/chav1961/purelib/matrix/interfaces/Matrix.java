package chav1961.purelib.matrix.interfaces;

public interface Matrix extends AutoCloseable {
	public static enum Type {
		REAL_INT(1, 4, "RI"),
		REAL_LONG(1, 8, "RL"),
		REAL_FLOAT(1, 4, "RF"),
		COMPLEX_FLOAT(2, 4, "CF"),
		REAL_DOUBLE(1, 8, "RD"),
		COMPLEX_DOUBLE(2, 8, "CD");
		
		private final int		numberOfItems;
		private final int		itemSize;
		private final String	suffix;
		
		private Type(final int numberOfItems, final int itemSize, final String suffix) {
			this.numberOfItems = numberOfItems;
			this.itemSize = itemSize;
			this.suffix = suffix;
		}
		
		public int getNumberOfItems() {
			return numberOfItems;
		}
		
		public int getItemSize() {
			return itemSize;
		}
		
		public String getProgramSuffix() {
			return suffix;
		}
	}
	
	public static enum AggregateDirection {
		ByRows,
		ByColumns,
		Total
	}
	
	public static enum AggregateType {
		Sum,
		Avg, 
		Min,
		Max
	}

	public static interface Piece {
		int getTop();
		int getLeft();
		int getWidth();
		int getHeight();
		
		public static Piece of(final int top, final int left, final int height, final int width) {
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
	
	@FunctionalInterface
	public static interface ApplyInt {
		int apply(int row, int col, int value);
	}

	@FunctionalInterface
	public static interface ApplyLong {
		long apply(int row, int col, long value);
	}

	@FunctionalInterface
	public static interface ApplyFloat {
		float apply(int row, int col, float value);
	}

	@FunctionalInterface
	public static interface ApplyDouble {
		double apply(int row, int col, double value);
	}

	@FunctionalInterface
	public static interface ApplyFloat2  {
		void apply(int row, int col, float real, float image, float[] result);
	}

	@FunctionalInterface
	public static interface ApplyDouble2  {
		void apply(int row, int col, double real, double image, double[] result);
	}
	
	@Override
	public void close() throws RuntimeException;
	
	public Type getType();
	public int numberOfRows();
	public int numberOfColumns();
	public boolean deepEquals(final Matrix another);
	
	public int[] extractInts();
	public int[] extractInts(Piece piece);
	public long[] extractLongs();
	public long[] extractLongs(Piece piece);
	public float[] extractFloats();
	public float[] extractFloats(Piece piece);
	public double[] extractDoubles();
	public double[] extractDoubles(Piece piece);
	
	public Matrix assign(int... content);
	public Matrix assign(Piece piece, int... content);
	public Matrix assign(long... content);
	public Matrix assign(Piece piece, long... content);
	public Matrix assign(float... content);
	public Matrix assign(Piece piece, float... content);
	public Matrix assign(double... content);
	public Matrix assign(Piece piece, double... content);
	public Matrix assign(Matrix content);
	public Matrix assign(Piece piece, Matrix content);
	
	public Matrix fill(int value);
	public Matrix fill(Piece piece, int value);
	public Matrix fill(long value);
	public Matrix fill(Piece piece, long value);
	public Matrix fill(float value);
	public Matrix fill(Piece piece, float value);
	public Matrix fill(float real, float image);
	public Matrix fill(Piece piece, float real, float image);
	public Matrix fill(double value);
	public Matrix fill(Piece piece, double value);
	public Matrix fill(double real, double image);
	public Matrix fill(Piece piece, double real, double image);
	
	public Matrix cast(Type type);	
	
	public Matrix add(int... content);
	public Matrix add(long... content);
	public Matrix add(float... content);
	public Matrix add(double... content);
	public Matrix add(Matrix content);

	public Matrix addValue(int value);
	public Matrix addValue(long value);
	public Matrix addValue(float value);
	public Matrix addValue(float real, float image);
	public Matrix addValue(double value);
	public Matrix addValue(double real, double image);

	public Matrix subtract(int... content);
	public Matrix subtract(long... content);
	public Matrix subtract(float... content);
	public Matrix subtract(double... content);
	public Matrix subtract(Matrix content);

	public Matrix subtractValue(int value);
	public Matrix subtractValue(long value);
	public Matrix subtractValue(float value);
	public Matrix subtractValue(float real, float image);
	public Matrix subtractValue(double value);
	public Matrix subtractValue(double real, double image);
	
	public Matrix subtractFrom(int... content);
	public Matrix subtractFrom(long... content);
	public Matrix subtractFrom(float... content);
	public Matrix subtractFrom(double... content);
	public Matrix subtractFrom(Matrix content);

	public Matrix subtractFromValue(int value);
	public Matrix subtractFromValue(long value);
	public Matrix subtractFromValue(float value);
	public Matrix subtractFromValue(float real, float image);
	public Matrix subtractFromValue(double value);
	public Matrix subtractFromValue(double real, double image);
	
	public Matrix mul(Matrix content);
//	public Matrix mulInv(Matrix content);
	public Matrix mulFrom(Matrix content);
//	public Matrix mulInvFrom(Matrix content);
	
	public Matrix mulValue(int value);
	public Matrix mulValue(long value);
	public Matrix mulValue(float value);
	public Matrix mulValue(float real, float image);
	public Matrix mulValue(double value);
	public Matrix mulValue(double real, double image);

	public Matrix divValue(int value);
	public Matrix divValue(long value);
	public Matrix divValue(float value);
	public Matrix divValue(float real, float image);
	public Matrix divValue(double value);
	public Matrix divValue(double real, double image);

	public Matrix divFromValue(int value);
	public Matrix divFromValue(long value);
	public Matrix divFromValue(float value);
	public Matrix divFromValue(float real, float image);
	public Matrix divFromValue(double value);
	public Matrix divFromValue(double real, double image);
	
	public Matrix mulHadamard(int... content);
	public Matrix mulHadamard(long... content);
	public Matrix mulHadamard(float... content);
	public Matrix mulHadamard(double... content);
	public Matrix mulHadamard(Matrix content);

	public Matrix mulInvHadamard(int... content);
	public Matrix mulInvHadamard(long... content);
	public Matrix mulInvHadamard(float... content);
	public Matrix mulInvHadamard(double... content);
	public Matrix mulInvHadamard(Matrix content);

	public Matrix mulInvFromHadamard(int... content);
	public Matrix mulInvFromHadamard(long... content);
	public Matrix mulInvFromHadamard(float... content);
	public Matrix mulInvFromHadamard(double... content);
	public Matrix mulInvFromHadamard(Matrix content);
	
	public Matrix tensorMul(Matrix content);
	public Matrix tensorMulFrom(Matrix content);

	public Matrix invert();
	public Matrix transpose();
	public Matrix aggregate(AggregateDirection dir, AggregateType aggType);
	public Number det();
	public Number track();

	public Matrix apply(ApplyInt callback);
	public Matrix apply(Piece piece, ApplyInt callback);
	public Matrix apply(ApplyLong callback);
	public Matrix apply(Piece piece, ApplyLong callback);
	public Matrix apply(ApplyFloat callback);
	public Matrix apply(Piece piece, ApplyFloat callback);
	public Matrix apply(ApplyDouble callback);
	public Matrix apply(Piece piece, ApplyDouble callback);
	public Matrix apply(ApplyFloat2 callback);
	public Matrix apply(Piece piece, ApplyFloat2 callback);
	public Matrix apply(ApplyDouble2 callback);
	public Matrix apply(Piece piece, ApplyDouble2 callback);
	
	public String toHumanReadableString();
	
	public Matrix done();
}
