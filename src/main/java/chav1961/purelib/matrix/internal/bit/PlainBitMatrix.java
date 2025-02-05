package chav1961.purelib.matrix.internal.bit;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.matrix.AbstractBaseMatrix;
import chav1961.purelib.matrix.interfaces.BaseMatrix;
import chav1961.purelib.streams.DataOutputAdapter;

public class PlainBitMatrix extends AbstractBaseMatrix<PlainBitMatrix>{
	private final boolean[][]	content;
	
	public PlainBitMatrix(final int width, final int height) {
		super(ContentType.BIT, FormatType.PLAIN, StoreType.IN_MEMORY, width, height);
		this.content = new boolean[height][width];
	}

	@Override
	public void close() throws CalculationException {
		super.close();
		Arrays.fill(content, null);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		final PlainBitMatrix 	result = new PlainBitMatrix(getWidth(), getHeight());
		final boolean[][]		target = result.content;
		final boolean[][]		source = content;

		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			System.arraycopy(source[y], 0, target[y], 0, getWidth());
		}
		return result;
	}

	@Override
	public PlainBitMatrix transpose() throws CalculationException {
		final PlainBitMatrix	result = new PlainBitMatrix(getHeight(), getWidth());
		final boolean[][]		target = result.content;
		final boolean[][]		source = content;

		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			for(int x = 0, maxX = getWidth(); x < maxX; x++) {
				target[x][y] = source[y][x];
			}
		}
		return result;
	}

	@Override
	public Operand det() throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Operand track() throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix downloadInternal(final Piece piece, final DataInput in) throws IOException {
		final boolean[][]	target = content;
		
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				target[y][x] = in.readBoolean();
			}
		}
		return this;
	}

	@Override
	protected PlainBitMatrix downloadInternal(final Piece piece, final PlainBitMatrix in) throws IOException {
		final boolean[][]	source = in.content;
		final boolean[][]	target = content;
		
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				target[y][x] = source[y][x];
			}
		}
		return this;
	}

	@Override
	protected PlainBitMatrix uploadInternal(final Piece piece, final DataOutput out) throws IOException {
		final boolean[][]	source = content;
		
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				out.writeBoolean(source[y][x]);
			}
		}
		return this;
	}

	@Override
	protected PlainBitMatrix uploadInternal(final Piece piece, final PlainBitMatrix out) throws IOException {
		final boolean[][]	source = content;
		final boolean[][]	target = out.content;
		
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				target[y][x] = source[y][x];
			}
		}
		return this;
	}

	@Override
	protected PlainBitMatrix cropInternal(final Piece piece) throws CalculationException {
		final PlainBitMatrix	result = new PlainBitMatrix(piece.getWidth(), piece.getHeight());
		final boolean[][]		source = content;
		final boolean[][]		target = result.content;
		
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				target[y][x] = source[y][x];
			}
		}
		return result;
	}

	@Override
	protected <AC extends ApplyCallback> PlainBitMatrix applyInternal(final Piece piece, final AC callback) throws CalculationException {
		final boolean[][]		source = content;
		final BitApplyCallback	bac = (BitApplyCallback)callback;
		
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				source[y][x] = bac.apply(x, y, source[y][x]);
			}
		}
		return this;
	}

	@Override
	protected BaseMatrix<?> castInternal(ContentType type) throws CalculationException {
		// TODO Auto-generated method stub
		switch (type) {
			case BIT			:
				try {
					return (BaseMatrix<?>) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new CalculationException();
				}
			case COMPLEX_DOUBLE	:
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
				throw new UnsupportedOperationException("Content type ["+type+"] is not supported yet");
		}
		return null;
	}

	@Override
	protected BaseMatrix<?> castInternal(FormatType type) throws CalculationException {
		// TODO Auto-generated method stub
		switch (type) {
			case BITMAP		:
				break;
			case LIST		:
				break;
			case PACKED_LINE:
				break;
			case PLAIN		:
				try {
					return (BaseMatrix<?>) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new CalculationException();
				}
			default:
				throw new UnsupportedOperationException("Format type ["+type+"] is not supported yet");
		}
		return null;
	}

	@Override
	protected BaseMatrix<?> castInternal(final StoreType type) throws CalculationException {
		// TODO Auto-generated method stub
		switch (type) {
			case EXTERNAL	:
				break;
			case IN_MEMORY	:
				try {
					return (BaseMatrix<?>) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new CalculationException();
				}
			default:
				throw new UnsupportedOperationException("Store type ["+type+"] is not supported yet");
		}
		return null;
	}

	@Override
	protected PlainBitMatrix addInternal(final PlainBitMatrix another) throws CalculationException {
		final PlainBitMatrix	result = new PlainBitMatrix(getWidth(), getHeight());
		final boolean[][]		target = result.content;

		if (another instanceof PlainBitMatrix) {
			final boolean[][]	source = content;
			
			for(int y = 0, maxY = getHeight(); y < maxY; y++) {
				for(int x = 0, maxX = getWidth(); x < maxX; x++) {
					target[y][x] |= source[y][x];
				}
			}
			return result;
		}
		else {
			try {
				another.upload(new DataOutputAdapter() {
					int	y = 0;
					int x = 0;
					
					@Override
					public void writeBoolean(boolean v) throws IOException {
						target[y][x] |= v;
						if (++x >= getWidth()) {
							y++;
							x = 0;
						}
					}
				});
				return result;
			} catch (IOException e) {
				throw new CalculationException(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	protected PlainBitMatrix addInternal(final Operand another) throws CalculationException {
		final PlainBitMatrix	result = new PlainBitMatrix(getWidth(), getHeight());
		final boolean[][]		source = content;
		final boolean[][]		target = result.content;
		final boolean			value = another.getBoolean();
		
		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			for(int x = 0, maxX = getWidth(); x < maxX; x++) {
				target[y][x] = source[y][x] | value;
			}
		}
		return result;
	}

	@Override
	protected PlainBitMatrix subtractInternal(final PlainBitMatrix another) throws CalculationException {
		final PlainBitMatrix	result = new PlainBitMatrix(getWidth(), getHeight());
		final boolean[][]		target = result.content;

		if (another instanceof PlainBitMatrix) {
			final boolean[][]	source = content;
			
			for(int y = 0, maxY = getHeight(); y < maxY; y++) {
				for(int x = 0, maxX = getWidth(); x < maxX; x++) {
					target[y][x] ^= source[y][x];
				}
			}
			return result;
		}
		else {
			try {
				another.upload(new DataOutputAdapter() {
					int	y = 0;
					int x = 0;
					
					@Override
					public void writeBoolean(boolean v) throws IOException {
						target[y][x] ^= v;
						if (++x >= getWidth()) {
							y++;
							x = 0;
						}
					}
				});
				return result;
			} catch (IOException e) {
				throw new CalculationException(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	protected PlainBitMatrix subtractInternal(final Operand another) throws CalculationException {
		final PlainBitMatrix	result = new PlainBitMatrix(getWidth(), getHeight());
		final boolean[][]		source = content;
		final boolean[][]		target = result.content;
		final boolean			value = another.getBoolean();
		
		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			for(int x = 0, maxX = getWidth(); x < maxX; x++) {
				target[y][x] = source[y][x] ^ value;
			}
		}
		return result;
	}

	@Override
	protected PlainBitMatrix subtractRevInternal(final PlainBitMatrix another) throws CalculationException {
		return subtractInternal(another);
	}

	@Override
	protected PlainBitMatrix subtractRevInternal(final Operand another) throws CalculationException {
		return subtractInternal(another);
	}

	@Override
	protected PlainBitMatrix mulInternal(PlainBitMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix mulTInternal(PlainBitMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix mulRevInternal(final PlainBitMatrix another) throws CalculationException {
		return another.mulInternal(this);
	}

	@Override
	protected PlainBitMatrix mulRevTInternal(final PlainBitMatrix another) throws CalculationException {
		return another.mulTInternal(this);
	}
	
	@Override
	protected PlainBitMatrix mulHadamardInternal(PlainBitMatrix another) throws CalculationException {
		final PlainBitMatrix	result = new PlainBitMatrix(getWidth(), getHeight());
		final boolean[][]		target = result.content;

		if (another instanceof PlainBitMatrix) {
			final boolean[][]	source = content;
			
			for(int y = 0, maxY = getHeight(); y < maxY; y++) {
				for(int x = 0, maxX = getWidth(); x < maxX; x++) {
					target[y][x] &= source[y][x];
				}
			}
			return result;
		}
		else {
			try {
				another.upload(new DataOutputAdapter() {
					int	y = 0;
					int x = 0;
					
					@Override
					public void writeBoolean(boolean v) throws IOException {
						target[y][x] &= v;
						if (++x >= getWidth()) {
							y++;
							x = 0;
						}
					}
				});
				return result;
			} catch (IOException e) {
				throw new CalculationException(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	protected PlainBitMatrix mulKronekerInternal(PlainBitMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix mulKronekerRevInternal(PlainBitMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix mulInternal(final Operand another) throws CalculationException {
		final PlainBitMatrix	result = new PlainBitMatrix(getWidth(), getHeight());
		final boolean[][]		source = content;
		final boolean[][]		target = result.content;
		final boolean			value = another.getBoolean();
		
		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			for(int x = 0, maxX = getWidth(); x < maxX; x++) {
				target[y][x] = source[y][x] & value;
			} 
		}
		return result;
	}

	@Override
	protected PlainBitMatrix divInternal(final Operand another) throws CalculationException {
		return mulInternal(another);
	}

	@Override
	protected PlainBitMatrix divRevInternal(final Operand another) throws CalculationException {
		return mulInternal(another);
	}

	@Override
	protected PlainBitMatrix aggregateInternal(final Piece piece, final AggregateDirection dir, final AggregateType type) throws CalculationException {
		final boolean[][]		source = content;
		final BitGroupFunction	bgf;
		
		switch (type) {
			case AVG	:
				bgf = new AvgBitGroup();
				break;
			case MAX	:
				bgf = new MaxBitGroup();
				break;
			case MIN	:
				bgf = new MinBitGroup();
				break;
			case SUM	:
				bgf = new SumBitGroup();
				break;
			default:
				throw new UnsupportedOperationException("Aggregation type ["+type+"] is nto supported yet"); 
		}
		final Object	total;
		
		switch (dir) {
			case BY_COLUMNS	:
				total = Array.newInstance(bgf.getResultClass(), piece.getHeight());
				for(int x = piece.getY(), where = 0, maxX = x + piece.getWidth(); x < maxX; x++, where++) {
					bgf.init();
					for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
						bgf.add(source[y][x]);
					}
					Array.set(total, where, extractValue(bgf.total()));
				}
				break;
			case BY_ROWS	:
				total = Array.newInstance(bgf.getResultClass(), piece.getWidth());
				for(int y = piece.getY(), where = 0, maxY = y + piece.getHeight(); y < maxY; y++, where++) {
					bgf.init();
					for(int x = piece.getY(), maxX = x + piece.getWidth(); x < maxX; x++) {
						bgf.add(source[y][x]);
					}
					Array.set(total, where, extractValue(bgf.total()));
				}
				break;
			case TOTAL		:
				total = Array.newInstance(bgf.getResultClass(), 1);
				bgf.init();
				for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
					for(int x = piece.getY(), maxX = x + piece.getWidth(); x < maxX; x++) {
						bgf.add(source[y][x]);
					}
				}
				Array.set(total, 0, extractValue(bgf.total()));
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is nto supported yet"); 
		}
		return null;
	}

	@Override
	protected PlainBitMatrix invertInternal() throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	private Object extractValue(final Operand value) {
		switch (value.getContentType()) {
			case BIT		:
				return Boolean.valueOf(value.getBoolean());
			case REAL_DOUBLE:
				return Double.valueOf(value.getDouble());
			case REAL_LONG	:
				return Long.valueOf(value.getLong());
			default:
				throw new UnsupportedOperationException("Unsupported type ["+value.getContentType()+"] for aggregation");
		}
	}
	
	private static interface BitGroupFunction {
		Class<?> getResultClass();
		void init();
		void add(boolean item);
		Operand total();
	}

	private static class SumBitGroup implements BitGroupFunction {
		long	sum;
		
		@Override
		public Class<?> getResultClass() {
			return long.class;
		}

		@Override
		public void init() {
			sum = 0;
		}

		@Override
		public void add(final boolean item) {
			if (item) {
				sum++;
			}
		}

		@Override
		public Operand total() {
			return Operand.of(sum);
		}
	}

	private static class AvgBitGroup implements BitGroupFunction {
		long 	count;
		long	sum;
		
		@Override
		public Class<?> getResultClass() {
			return double.class;
		}

		@Override
		public void init() {
			sum = 0;
			count = 0;
		}

		@Override
		public void add(final boolean item) {
			if (item) {
				sum++;
			}
			count++;
		}

		@Override
		public Operand total() {
			return count != 0 ? Operand.of(1.0*sum/count) : Operand.of(0.0);
		}
	}

	private static class MinBitGroup implements BitGroupFunction {
		boolean min;
		
		@Override
		public Class<?> getResultClass() {
			return boolean.class;
		}

		@Override
		public void init() {
			min = true;
		}

		@Override
		public void add(final boolean item) {
			if (!item) {
				min = false;
			}
		}

		@Override
		public Operand total() {
			return Operand.of(min);
		}
	}

	private static class MaxBitGroup implements BitGroupFunction {
		boolean max;
		
		@Override
		public Class<?> getResultClass() {
			return boolean.class;
		}

		@Override
		public void init() {
			max = false;
		}

		@Override
		public void add(final boolean item) {
			if (item) {
				max = true;
			}
		}

		@Override
		public Operand total() {
			return Operand.of(max);
		}
	}
}
