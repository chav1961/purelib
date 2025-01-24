package chav1961.purelib.matrix.interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.CalculationException;

public interface BaseMatrix<T extends BaseMatrix<?>> extends Cloneable, AutoCloseable {
	ApplyCallback	ZERO_BIT = new BitApplyCallback() {
									@Override
									public boolean apply(int x, int y, boolean value) throws CalculationException {
										return false;
									}
								};
	ApplyCallback	ZERO_INT = new IntApplyCallback() {
									@Override
									public int apply(int x, int y, int value) throws CalculationException {
										return 0;
									}
								};
	ApplyCallback	ZERO_LONG = new LongApplyCallback() {
									@Override
									public long apply(int x, int y, long value) throws CalculationException {
										return 0;
									}
								};
	ApplyCallback	ZERO_FLOAT = new FloatApplyCallback() {
									@Override
									public float apply(int x, int y, float value) throws CalculationException {
										return 0;
									}
								};
	ApplyCallback	ZERO_DOUBLE = new DoubleApplyCallback() {
									@Override
									public double apply(int x, int y, double value) throws CalculationException {
										return 0;
									}
								};
	ApplyCallback	ZERO_COMPLEX_FLOAT = new ComplexFloatApplyCallback() {
									@Override
									public void apply(int x, int y, float[] value) throws CalculationException {
										value[0] = 1;
										value[1] = 0;
									}
								};
	ApplyCallback	ZERO_COMPLEX_DOUBLE = new ComplexDoubleApplyCallback() {
									@Override
									public void apply(int x, int y, double[] value) throws CalculationException {
										value[0] = 1;
										value[1] = 0;
									}
								};
	ApplyCallback	IDENTITY_BIT = new BitApplyCallback() {
									@Override
									public boolean apply(int x, int y, boolean value) throws CalculationException {
										return x == y;
									}
								};
	ApplyCallback	IDENTITY_INT = new IntApplyCallback() {
									@Override
									public int apply(int x, int y, int value) throws CalculationException {
										return x == y ? 1 : 0;
									}
								};
	ApplyCallback	IDENTITY_LONG = new LongApplyCallback() {
									@Override
									public long apply(int x, int y, long value) throws CalculationException {
										return x == y ? 1 : 0;
									}
								};
	ApplyCallback	IDENTITY_FLOAT = new FloatApplyCallback() {
									@Override
									public float apply(int x, int y, float value) throws CalculationException {
										return x == y ? 1 : 0;
									}
								};
	ApplyCallback	IDENTITY_DOUBLE = new DoubleApplyCallback() {
									@Override
									public double apply(int x, int y, double value) throws CalculationException {
										return x == y ? 1 : 0;
									}
								};
	ApplyCallback	IDENTITY_COMPLEX_FLOAT = new ComplexFloatApplyCallback() {
									@Override
									public void apply(int x, int y, float[] value) throws CalculationException {
										value[0] = x == y ? 1 : 0;
										value[1] = 0;
									}
								};
	ApplyCallback	IDENTITY_COMPLEX_DOUBLE = new ComplexDoubleApplyCallback() {
									@Override
									public void apply(int x, int y, double[] value) throws CalculationException {
										value[0] = x == y ? 1 : 0;
										value[1] = 0;
									}
								};
	
	public static enum ContentType {
		BIT,
		REAL_INT,
		REAL_LONG,
		REAL_FLOAT,
		REAL_DOUBLE,
		COMPLEX_FLOAT,
		COMPLEX_DOUBLE
	}
	
	public static enum FormatType {
		PLAIN,
		BITMAP,
		PACKED_LINE,
		LIST
	}
	
	public static enum StoreType {
		IN_MEMORY,
		EXTERNAL
	}
	
	public static enum AggregateDirection {
		BY_ROWS,
		BY_COLUMNS,
		TOTAL
	}

	public static enum AggregateType {
		SUM,
		AVG,
		MIN,
		MAX;
	}
	
	public interface Piece {
		int getX();
		int getY();
		int getWidth();
		int getHeight();
		
		public static Piece of(final int x, final int y, final int width, final int height) {
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

	public static interface ApplyCallback {
	}
	
	public static interface BitApplyCallback extends ApplyCallback {
		boolean apply(int x, int y, boolean value) throws CalculationException;
	}

	public static interface IntApplyCallback extends ApplyCallback {
		int apply(int x, int y, int value) throws CalculationException;
	}

	public static interface LongApplyCallback extends ApplyCallback {
		long apply(int x, int y, long value) throws CalculationException;
	}

	public static interface FloatApplyCallback extends ApplyCallback {
		float apply(int x, int y, float value) throws CalculationException;
	}

	public static interface DoubleApplyCallback extends ApplyCallback {
		double apply(int x, int y, double value) throws CalculationException;
	}

	public static interface ComplexFloatApplyCallback extends ApplyCallback {
		void apply(int x, int y, float[] value) throws CalculationException;
	}

	public static interface ComplexDoubleApplyCallback extends ApplyCallback {
		void apply(int x, int y, double[] value) throws CalculationException;
	}
	
	public static interface Operand {
		ContentType getContentType();
		boolean getBoolean();
		int getInt();
		long getLong();
		float getFloat();
		double getDouble();
		float[] getComplexFloat();
		double[] getComplexDouble();		
		
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

		static Operand of(final float real, final float image) {
			return new Operand() {
				@Override public ContentType getContentType() {return ContentType.REAL_FLOAT;}
				@Override public boolean getBoolean() {return real != 0;}
				@Override public float getFloat() {return real;}
				@Override public double getDouble() {return real;}
				@Override public float[] getComplexFloat() {return new float[] {real, image};}
				@Override public double[] getComplexDouble() {return new double[] {real, image};}
				@Override public long getLong() {return (long)real;}
				@Override public int getInt() {return (int)real;}
			};
		}

		static Operand of(final double real, final double image) {
			return new Operand() {
				@Override public ContentType getContentType() {return ContentType.REAL_FLOAT;}
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
	
	ContentType getContentType();
	FormatType getFormatType();
	StoreType getStoreType();
	
	int getWidth();	
	int getHeight();
	
	@Override
	void close() throws CalculationException;
	
	T download(Piece piece, DataInput in) throws IOException;
	default T download(DataInput in) throws IOException {
		return download(Piece.of(0, 0, getWidth(), getHeight()), in);
	}

	T download(Piece piece, T in) throws IOException;
	default T download(T in) throws IOException {
		return download(Piece.of(0, 0, getWidth(), getHeight()), in);
	}
	
	T upload(Piece piece, DataOutput out) throws IOException;
	default T upload(DataOutput out) throws IOException {
		return upload(Piece.of(0, 0, getWidth(), getHeight()), out);
	}

	T upload(Piece piece, T out) throws IOException;
	default T upload(T out) throws IOException {
		return upload(Piece.of(0, 0, getWidth(), getHeight()), out);
	}
	
	T crop(Piece piece) throws CalculationException;
	
	T apply(Piece piece, ApplyCallback callback) throws CalculationException;
	default T apply(ApplyCallback callback) throws CalculationException {
		return apply(Piece.of(0, 0, getWidth(), getHeight()), callback);
	}
	
	T cast(ContentType type) throws CalculationException;
	T cast(FormatType type) throws CalculationException;
	T cast(StoreType type) throws CalculationException;
	
	T add(T another) throws CalculationException;
	T add(Operand another) throws CalculationException;
	T subtract(T another) throws CalculationException;
	T subtract(Operand another) throws CalculationException;
	T subtractRev(T another) throws CalculationException;
	T subtractRev(Operand another) throws CalculationException;
	T mul(T another) throws CalculationException;
	T mulRev(T another) throws CalculationException;
	T mulHadamard(T another) throws CalculationException;
	T mulKroneker(T another) throws CalculationException;
	T mulKronekerRev(T another) throws CalculationException;
	T mul(Operand another) throws CalculationException;
	T div(Operand another) throws CalculationException;
	T divRev(Operand another) throws CalculationException;
	
	T transpose() throws CalculationException;
	T invert() throws CalculationException;
	Operand det() throws CalculationException;
	Operand track() throws CalculationException;
	
	T aggregate(Piece piece, AggregateDirection dir, AggregateType type) throws CalculationException;
	
	default T aggregate(AggregateDirection dir, AggregateType type) throws CalculationException {
		return aggregate(Piece.of(0, 0, getWidth(), getHeight()), dir, type);
	}
}
