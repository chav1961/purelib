package chav1961.purelib.matrix.internal.complexfloat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.Arrays;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.matrix.AbstractBaseMatrix;
import chav1961.purelib.matrix.interfaces.BaseMatrix;
import chav1961.purelib.streams.DataInputAdapter;
import chav1961.purelib.streams.DataOutputAdapter;

public class PlainComplexFloatMatrix extends AbstractBaseMatrix<PlainComplexFloatMatrix>{
	private final float[][]	content;
	private final Cleaner.Cleanable	cc;

	public PlainComplexFloatMatrix(final int width,final int height) {
		super(ContentType.COMPLEX_FLOAT, FormatType.PLAIN, StoreType.IN_MEMORY, width, height);
		this.content = new float[height][2*width];
		this.cc = PureLibSettings.COMMON_CLEANER.register(this, ()->{Arrays.fill(content, null);});
	}

	@Override
	public void close() throws CalculationException {
		super.close();
		cc.clean();
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth(), getHeight());
		final float[][]	target = result.content;
		final float[][]	source = this.content;
		
		for(int index = 0; index < target.length; index++) {
			System.arraycopy(source[index], 0, target[index], 0, source[index].length);
		}
		return result;
	}

	@Override
	public PlainComplexFloatMatrix transpose() throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getHeight(), getWidth());
		final float[][]	target = result.content;
		final float[][]	source = content;

		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			for(int x = 0, maxX = getWidth(); x < maxX; x++) {
				target[x][y] = source[y][x];
			}
		}
		return result;
	}

	@Override
	protected PlainComplexFloatMatrix downloadInternal(final Piece piece, final DataInput in) throws IOException {
		final float[][]	target = content;
		
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				target[y][2*x] = in.readFloat();
				target[y][2*x+1] = in.readFloat();
			}
		}
		return this;
	}

	@Override
	protected PlainComplexFloatMatrix downloadInternal(final Piece piece, final PlainComplexFloatMatrix in) throws IOException {
		if (in instanceof PlainComplexFloatMatrix) {
			final float[][]	target = content;
			final float[][]	source = ((PlainComplexFloatMatrix)in).content;
			
			for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
				for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
					target[y][2*x] = source[y][2*x];
					target[y][2*x+1] = source[y][2*x+1];
				}
			}
		}
		else {
			in.upload(new DataOutputAdapter() {
				final float[][]	target = content;
				int	y = piece.getY();
				int x = piece.getX();
				
				@Override
				public void writeFloat(final float v) throws IOException {
					target[y][x] = v;
					if (++x >= 2 * piece.getWidth()) {
						y++;
						x = piece.getX();
					}
				}
			});
		}
		return this;
	}

	@Override
	protected PlainComplexFloatMatrix uploadInternal(final Piece piece, final DataOutput out) throws IOException {
		final float[][]	source = content;
		
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				out.writeFloat(source[y][2*x]);
				out.writeFloat(source[y][2*x+1]);
			}
		}
		return this;
	}

	@Override
	protected PlainComplexFloatMatrix uploadInternal(final Piece piece, final PlainComplexFloatMatrix out) throws IOException {
		if (out instanceof PlainComplexFloatMatrix) {
			final float[][]	source = content;
			final float[][]	target = ((PlainComplexFloatMatrix)out).content;
			
			for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
				for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
					target[y][2*x] = source[y][2*x];
					target[y][2*x+1] = source[y][2*x+1];
				}
			}
		}
		else {
			out.downloadInternal(piece, new DataInputAdapter() {
				final float[][]	source = content;
				int	y = piece.getY();
				int x = piece.getX();
				
				@Override
				public float readFloat() throws IOException {
					final float	val = source[y][x]; 

					if (++x >= 2*(piece.getX()+piece.getWidth())) {
						y++;
						x = piece.getX();
					}
					return val;
				}
			});
		}
		return this;
	}

	@Override
	protected PlainComplexFloatMatrix cropInternal(final Piece piece) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(piece.getWidth(), piece.getHeight());
		final float[][]	source = content;
		final float[][]	target = result.content;
		
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				target[y][x] = source[y][x];
			}
		}
		return result;
	}

	@Override
	protected <AC extends ApplyCallback> PlainComplexFloatMatrix applyInternal(final Piece piece, final AC callback) throws CalculationException {
		final float[][]		source = content;
		final float[]		temp = new float[2];
		final ComplexFloatApplyCallback	cfac = (ComplexFloatApplyCallback)callback;
		
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				temp[0] = source[y][2*x]; 
				temp[1] = source[y][2*x+1]; 
				cfac.apply(x, y, temp);
				source[y][2*x] = temp[0]; 
				source[y][2*x+1] = temp[1]; 
			}
		}
		return this;
	}

	@Override
	protected BaseMatrix<?> castInternal(final ContentType type) throws CalculationException {
		// TODO Auto-generated method stub
		switch (type) {
			case BIT			:
				break;
			case COMPLEX_DOUBLE	:
				break;
			case COMPLEX_FLOAT	:
				try {
					return (BaseMatrix<?>) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new CalculationException();
				}
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
				throw new IllegalArgumentException("Format ["+type+"] is not applicable for ["+getClass().getCanonicalName()+"] matrix");
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
		switch (type) {
			case EXTERNAL	:
				try {
					final PlainComplexFloatMatrixX	result = new PlainComplexFloatMatrixX(getWidth(), getHeight());
					
					result.download(new DataInputAdapter() {
						final float[][]	source = content;
						int	y = 0;
						int x = 0;
						
						@Override
						public float readFloat() throws IOException {
							final float	value = source[y][x];
							
							if (++x >= 2*getWidth()) {
								x = 0;
								y++;
							}
							return value;
						}
					});
					return result;
				} catch (IOException e) {
					throw new CalculationException();
				}
			case IN_MEMORY	:
				try {
					return (BaseMatrix<?>) this.clone();
				} catch (CloneNotSupportedException e) {
					throw new CalculationException();
				}
			default:
				throw new UnsupportedOperationException("Store type ["+type+"] is not supported yet");
		}
	}

	@Override
	protected PlainComplexFloatMatrix addInternal(final PlainComplexFloatMatrix another) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth(), getHeight());
		final float[][]		target = result.content;

		if (another instanceof PlainComplexFloatMatrix) {
			final float[][]	source1 = content;
			final float[][]	source2 = another.content;
			
			for(int y = 0, maxY = getHeight(); y < maxY; y++) {
				for(int x = 0, maxX = getWidth(); x < maxX; x++) {
					target[y][2*x] = source1[y][2*x] + source2[y][2*x];
					target[y][2*x+1] = source1[y][2*x+1] + source2[y][2*x+1];
				}
			}
			return result;
		}
		else {
			try {
				another.upload(new DataOutputAdapter() {
					final float[][]	source = content;
					int	y = 0;
					int x = 0;
					
					@Override
					public void writeFloat(float v) throws IOException {
						target[y][x] = source[y][x] + v;
						if (++x >= 2*getWidth()) {
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
	protected PlainComplexFloatMatrix addInternal(final Operand another) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth(), getHeight());
		final float[][]		source = content;
		final float[][]		target = result.content;
		final float[]		add = another.getComplexFloat();
		final float			realA = add[0], imageA = add[1];
		
		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			for(int x = 0, maxX = getWidth(); x < maxX; x++) {
				target[y][2*x] = source[y][2*x] + realA;
				target[y][2*x+1] = source[y][2*x+1] + imageA;
			}
		}
		return result;
	}

	@Override
	protected PlainComplexFloatMatrix subtractInternal(final PlainComplexFloatMatrix another) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth(), getHeight());
		final float[][]		target = result.content;

		if (another instanceof PlainComplexFloatMatrix) {
			final float[][]	source1 = content;
			final float[][]	source2 = another.content;
			
			for(int y = 0, maxY = getHeight(); y < maxY; y++) {
				for(int x = 0, maxX = getWidth(); x < maxX; x++) {
					target[y][2*x] = source1[y][2*x] - source2[y][2*x];
					target[y][2*x+1] = source1[y][2*x+1] - source2[y][2*x+1];
				}
			}
			return result;
		}
		else {
			try {
				another.upload(new DataOutputAdapter() {
					final float[][]	source = content;
					int	y = 0;
					int x = 0;
					
					@Override
					public void writeFloat(float v) throws IOException {
						target[y][x] = source[y][x] - v;
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
	protected PlainComplexFloatMatrix subtractInternal(final Operand another) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth(), getHeight());
		final float[][]		source = content;
		final float[][]		target = result.content;
		final float[]		subtract = another.getComplexFloat();
		final float			realS = subtract[0], imageS = subtract[1];
		
		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			for(int x = 0, maxX = getWidth(); x < maxX; x++) {
				target[y][2*x] = source[y][2*x] - realS;
				target[y][2*x+1] = source[y][2*x+1] - imageS;
			}
		}
		return result;
	}

	@Override
	protected PlainComplexFloatMatrix subtractRevInternal(final PlainComplexFloatMatrix another) throws CalculationException {
		return another.subtractInternal(this);
	}

	@Override
	protected PlainComplexFloatMatrix subtractRevInternal(final Operand another) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth(), getHeight());
		final float[][]		source = content;
		final float[][]		target = result.content;
		final float[]		subtract = another.getComplexFloat();
		final float			realS = subtract[0], imageS = subtract[1];
		
		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			for(int x = 0, maxX = getWidth(); x < maxX; x++) {
				target[y][2*x] = realS - source[y][2*x];
				target[y][2*x+1] = imageS - source[y][2*x+1];
			}
		}
		return result;
	}

	@Override
	protected PlainComplexFloatMatrix mulInternal(final PlainComplexFloatMatrix another) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(another.getWidth(), getHeight());
		final float[][]		target = result.content;

		if (another instanceof PlainComplexFloatMatrix) {
			final float[][]	source1 = content;
			final float[][]	source2 = another.content;
			final float[]	tempReal = new float[getWidth()];
			final float[]	tempImage = new float[getWidth()];
			
			for(int y = 0, maxY = getHeight(); y < maxY; y++) {
				for(int x = 0, maxX = another.getWidth(); x < maxX; x++) {
					for(int k = 0, maxK = getWidth(); k < maxK; k++) {
						tempReal[k] = source1[y][2*k] * source2[k][2*y] - source1[y][2*k+1] * source2[k][2*y+1]; 
						tempImage[k] = source1[y][2*k] * source2[k][2*y+1] + source1[y][2*k+1] * source2[k][2*y]; 
					}
					target[y][2*x] = sum(tempReal);
					target[y][2*x+1] = sum(tempImage);
				}
			}
			return result;
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	protected PlainComplexFloatMatrix mulRevInternal(final PlainComplexFloatMatrix another) throws CalculationException {
		return another.mulInternal(this);
	}

	@Override
	protected PlainComplexFloatMatrix mulHadamardInternal(final PlainComplexFloatMatrix another) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth(), getHeight());
		final float[][]		target = result.content;

		if (another instanceof PlainComplexFloatMatrix) {
			final float[][]	source1 = content;
			final float[][]	source2 = another.content;
			
			for(int y = 0, maxY = getHeight(); y < maxY; y++) {
				for(int x = 0, maxX = getWidth(); x < maxX; x++) {
					target[y][2*x] = source1[y][2*x] * source2[y][2*x] - source1[y][2*x+1] * source2[y][2*x+1];
					target[y][2*x+1] = source1[y][2*x] * source2[y][2*x+1] + source1[y][2*x+1] * source2[y][2*x];
				}
			}
			return result;
		}
		else {
			try {
				another.upload(new DataOutputAdapter() {
					final float[][]	source = content;
					int	y = 0;
					int x = 0;
					float real;
					
					@Override
					public void writeFloat(float v) throws IOException {
						if (x % 2 == 0) {
							real = v;
						}
						else {
							target[y][2*x] = source[y][2*x] * real - source[y][2*x+1] * v;
							target[y][2*x+1] = source[y][2*x] * v + source[y][2*x+1] * real;
						}
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
	protected PlainComplexFloatMatrix mulKronekerInternal(final PlainComplexFloatMatrix another) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth()*another.getWidth(), getHeight()*another.getHeight());
		final float[][]		source1 = content;
		final float[][]		target = result.content;

		if (another instanceof PlainComplexFloatMatrix) {
			final float[][]	source2 = another.content;

			for(int y1 = 0, maxY1 = getHeight(); y1 < maxY1; y1++) {
				for(int x1 = 0, maxX1 = getWidth(); x1 < maxX1; x1++) {
					final float	real = source1[y1][2*x1];
					final float	image = source1[y1][2*x1+1];

					if (real != 0 || image != 0) {
						for(int y2 = 0, maxY2 = another.getHeight(); y2 < maxY2; y2++) {
							for(int x2 = 0, maxX2 = another.getWidth(); x2 < maxX2; x2++) {
								target[y1*maxY2 + y2][2*(x1*maxX2 + x2)] = source2[y2][2*x2] * real - source2[y2][2*x2+1] * image; 
								target[y1*maxY2 + y2][2*(x1*maxX2 + x2) + 1] = source2[y2][2*x2] * image + source2[y2][2*x2+1] * real; 
							}
						}
					}
				}
			}
			return result;
		}
		else {
			throw new UnsupportedOperationException();
		}
			
	}

	@Override
	protected PlainComplexFloatMatrix mulKronekerRevInternal(final PlainComplexFloatMatrix another) throws CalculationException {
		return another.mulKroneker(this);
	}

	@Override
	protected PlainComplexFloatMatrix mulInternal(final Operand another) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth(), getHeight());
		final float[][]		source = content;
		final float[][]		target = result.content;
		final float[]		mul = another.getComplexFloat();
		final float			realM = mul[0], imageM = mul[1];
		
		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			for(int x = 0, maxX = getWidth(); x < maxX; x++) {
				final float real = source[y][2*x];
				final float image = source[y][2*x+1];
				
				target[y][2*x] = real * realM - image * imageM;
				target[y][2*x+1] = real * imageM + image * realM;
			}
		}
		return result;
	}

	@Override
	protected PlainComplexFloatMatrix divInternal(final Operand another) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth(), getHeight());
		final float[][]		source = content;
		final float[][]		target = result.content;
		final float[]		div = another.getComplexFloat();
		final float			realD = div[0], imageD = div[1];
		final float			znam = 1 / (realD * realD + imageD * imageD);
		
		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			for(int x = 0, maxX = getWidth(); x < maxX; x++) {
				final float real = source[y][2*x];
				final float image = source[y][2*x+1];
				
				target[y][2*x] = znam * (real * realD + image * imageD);
				target[y][2*x+1] = znam * (real * imageD - image * realD);
			}
		}
		return result;
	}

	@Override
	protected PlainComplexFloatMatrix divRevInternal(final Operand another) throws CalculationException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth(), getHeight());
		final float[][]		source = content;
		final float[][]		target = result.content;
		final float[]		div = another.getComplexFloat();
		final float			realD = div[0], imageD = div[1];
		
		for(int y = 0, maxY = getHeight(); y < maxY; y++) {
			for(int x = 0, maxX = getWidth(); x < maxX; x++) {
				final float real = source[y][2*x];
				final float image = source[y][2*x+1];
				final float	znam = 1 / (real * real + image * image);
				
				target[y][2*x] = znam * (realD * real + imageD * image);
				target[y][2*x+1] = znam * (realD * image - imageD * real);
			}
		}
		return result;
	}

	@Override
	protected PlainComplexFloatMatrix aggregateInternal(Piece piece, AggregateDirection dir, AggregateType type) throws CalculationException {
		final float[][]		source = content;
		final ComplexFloatGroupFunction	cfgf;
		
		switch (type) {
			case AVG	:
				cfgf = new AvgGroupFunction();
				break;
			case MAX	:
				cfgf = new MaxGroupFunction();
				break;
			case MIN	:
				cfgf = new MinGroupFunction();
				break;
			case SUM	:
				cfgf = new SumGroupFunction();
				break;
			default:
				throw new UnsupportedOperationException("Aggregation type ["+type+"] is nto supported yet"); 
		}
		final float[][]	total;
		
		switch (dir) {
			case BY_COLUMNS	:
				total = new float[piece.getHeight()][];
				for(int x = piece.getY(), where = 0, maxX = x + piece.getWidth(); x < maxX; x++, where++) {
					cfgf.init();
					for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
						cfgf.add(source[y][2*x], source[y][2*x+1]);
					}
					total[where++] = cfgf.total().getComplexFloat();
				}
				break;
			case BY_ROWS	:
				total = new float[1][2*piece.getWidth()];
				for(int y = piece.getY(), where = 0, maxY = y + piece.getHeight(); y < maxY; y++, where++) {
					cfgf.init();
					for(int x = piece.getY(), maxX = x + piece.getWidth(); x < maxX; x++) {
						cfgf.add(source[y][2*x], source[y][2*x+1]);
					}
					System.arraycopy(cfgf.total().getComplexFloat(), 0, total[0], 2*where, 2);
					where++;
				}
				break;
			case TOTAL		:
				total = new float[1][];
				cfgf.init();
				for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
					for(int x = piece.getY(), maxX = x + piece.getWidth(); x < maxX; x++) {
						cfgf.add(source[y][2*x], source[y][2*x+1]);
					}
				}
				total[0] = cfgf.total().getComplexFloat();
				break;
			default:
				throw new UnsupportedOperationException("Aggregate direction ["+dir+"] is nto supported yet"); 
		}
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(2*total[0].length, total.length);
		final float[][]	target = result.content;
		
		for(int index = 0; index < target.length; index++) {
			System.arraycopy(total[index], 0, target[index], 0, total[index].length);
		}
		return result;
	}

	@Override
	public Operand det() throws CalculationException {
		final float[][]	source = this.content.clone();
		final int		colSize = getWidth();
		double			detReal = 1, detImage = 0;
		
		for(int index = 0; index < source.length; index++) {
			source[index] = source[index].clone(); 
		}

		for(int y = 0; y < colSize; y++) {
			final float	real = source[y][2*y];		// Take diagonal element.
			final float	image = source[y][2*y+1];	// Take diagonal element.
			final float	znam = 1 / (real * real + image * image);

			detReal = detReal * real - detImage * image;
			detImage = detReal * image + detImage * real;
			for(int x = 0; x < colSize; x++) {		// divide all line by diagonal element
				source[y][2*x] = znam * (source[y][2*x] * real + source[y][2*x+1] * image);
				source[y][2*x+1] = znam * (source[y][2*x] * image - source[y][2*x+1] * real);
			}
			for(int i = y + 1; i < colSize; i++) {	// subtract current line from all lines below to make zeroes at the current column
				final double	real2 = source[i][2*y];
				final double	image2 = source[i][2*y+1];
				
				for(int x = 0; x < colSize; x++) {
					source[i][2*x] -= source[y][2*x] * real2 - source[y][2*x+1] * image2;
					source[i][2*x+1] -= source[y][2*x+1] * real2 + source[y][2*x] * image2;
				}
			}
		}
		return Operand.of((float)detReal,(float)detImage);
	}

	@Override
	public Operand track() throws CalculationException {
		final float[]	real = new float[getWidth()], image = new float[getWidth()];
		final float[][]	source = content;
		
		for(int index = 0, maxIndex = source.length; index < maxIndex; index++) {
			real[index] = source[index][2*index];
			image[index] = source[index][2*index+1];
		}
		return Operand.of(sum(real), sum(image));
	}

	@Override
	protected PlainComplexFloatMatrix invertInternal() throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	private float sum(final float[] content) {
		Arrays.sort(content);
		int 	zero = Arrays.binarySearch(content, 0f);
		double	negSum = 0, posSum = 0;
		
		if (zero < 0) {
			zero = -1 - zero;
		}
		for(int index = zero, maxIndex = content.length; index < maxIndex; index++) {
			posSum += content[index];
		}
		for(int index = zero-1; index >= 0; index--) {
			negSum += content[index];
		}
		return (float) (posSum + negSum);
	}

	private static interface ComplexFloatGroupFunction {
		void init();
		void add(float real, float image);
		Operand total();
	}

	private static class SumGroupFunction implements ComplexFloatGroupFunction {
		float	sumReal, sumImage;
		
		@Override
		public void init() {
			sumReal = 0;
			sumImage = 0;
		}

		@Override
		public void add(float real, float image) {
			sumReal += real;
			sumImage += image;
		}

		@Override
		public Operand total() {
			return Operand.of(sumReal, sumImage);
		}
	}

	private static class AvgGroupFunction implements ComplexFloatGroupFunction {
		int		count;
		float	sumReal, sumImage;
		
		@Override
		public void init() {
			count = 0;
			sumReal = 0;
			sumImage = 0;
		}

		@Override
		public void add(float real, float image) {
			count++;
			sumReal += real;
			sumImage += image;
		}

		@Override
		public Operand total() {
			return count > 0 ? Operand.of(sumReal / count, sumImage / count) : Operand.of(0f, 0f);
		}
	}

	private static class MinGroupFunction implements ComplexFloatGroupFunction {
		float	minReal, minImage;
		float 	module;
		
		@Override
		public void init() {
			module = Float.MAX_VALUE;
			minReal = Float.MAX_VALUE;
			minImage = Float.MAX_VALUE;
		}

		@Override
		public void add(float real, float image) {
			float	currentModule = real * real + image * image; 
			
			if (currentModule <= module) {
				minReal = real;
				minImage = image;
				module = currentModule;
			}
		}

		@Override
		public Operand total() {
			return Operand.of(minReal, minImage);
		}
	}

	private static class MaxGroupFunction implements ComplexFloatGroupFunction {
		float	maxReal, maxImage;
		float 	module;
		
		@Override
		public void init() {
			module = 0;
			maxReal = 0;
			maxImage = 0;
		}

		@Override
		public void add(float real, float image) {
			float	currentModule = real * real + image * image; 
			
			if (currentModule >= module) {
				maxReal = real;
				maxImage = image;
				module = currentModule;
			}
		}

		@Override
		public Operand total() {
			return Operand.of(maxReal, maxImage);
		}
	}
}
