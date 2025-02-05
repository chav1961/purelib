package chav1961.purelib.matrix.internal.complexfloat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.Cleaner;
import java.util.Arrays;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.matrix.AbstractBaseMatrix;
import chav1961.purelib.matrix.interfaces.BaseMatrix;

public class PlainComplexFloatMatrixX extends AbstractBaseMatrix<PlainComplexFloatMatrixX>{
	private static final long		ITEM_SIZE = 8L;
	private final Cleaner.Cleanable	cc;
	private final RandomAccessFile	raf;

	public PlainComplexFloatMatrixX(final int width, final int height) throws IOException {
		super(ContentType.COMPLEX_FLOAT, FormatType.PLAIN, StoreType.EXTERNAL, width, height);
		final File	f = File.createTempFile("PlainComplexFloat", ".mat");
		final RandomAccessFile	raf = new RandomAccessFile(f, "rw");
		
		this.cc = PureLibSettings.COMMON_CLEANER.register(this, ()->{
							try {
								raf.close();
							} catch (IOException e) {
							}
							f.delete();
						});
		this.raf = raf;
		raf.seek(ITEM_SIZE*getHeight()*getWidth());
	}

	@Override
	public void close() throws CalculationException {
		super.close();
		cc.clean();
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		final PlainComplexFloatMatrix	result = new PlainComplexFloatMatrix(getWidth(), getHeight());
		
		return result;
	}

	@Override
	public PlainComplexFloatMatrixX transpose() throws CalculationException {
		// TODO Auto-generated method stub
		return null;
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
	protected PlainComplexFloatMatrixX downloadInternal(final Piece piece, final DataInput in) throws IOException {
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			raf.seek(ITEM_SIZE * (y * getHeight() + piece.getX()));
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				raf.writeFloat(in.readFloat());
				raf.writeFloat(in.readFloat());
			}
		}
		return this;
	}

	@Override
	protected PlainComplexFloatMatrixX downloadInternal(Piece piece, PlainComplexFloatMatrixX in) throws IOException {
		// TODO Auto-generated method stub
		if (in instanceof PlainComplexFloatMatrixX) {
			final byte[]	buffer = new byte[(int) (ITEM_SIZE * piece.getWidth())];
			final RandomAccessFile	source = in.raf;
					
			for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
				source.seek(ITEM_SIZE * (y * getHeight() + piece.getX()));
				raf.seek(ITEM_SIZE * (y * getHeight() + piece.getX()));
				source.read(buffer);
				raf.write(buffer);
			}
		}
		else {
//			for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
//				raf.seek(ITEM_SIZE * (y * getHeight() + piece.getX()));
//				for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
//					raf.writeFloat(in.readFloat());
//					raf.writeFloat(in.readFloat());
//				}
//			}
		}
		return this;
	}

	@Override
	protected PlainComplexFloatMatrixX uploadInternal(final Piece piece, final DataOutput out) throws IOException {
		for(int y = piece.getY(), maxY = y + piece.getHeight(); y < maxY; y++) {
			raf.seek(ITEM_SIZE * (y * getHeight() + piece.getX()));
			for(int x = piece.getX(), maxX = x + piece.getWidth(); x < maxX; x++) {
				out.writeFloat(raf.readFloat());
				out.writeFloat(raf.readFloat());
			}
		}
		return this;
	}

	@Override
	protected PlainComplexFloatMatrixX uploadInternal(Piece piece, PlainComplexFloatMatrixX out) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX cropInternal(Piece piece) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected <AC extends ApplyCallback> PlainComplexFloatMatrixX applyInternal(Piece piece, AC callback)
			throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected BaseMatrix<?> castInternal(ContentType type) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected BaseMatrix<?> castInternal(FormatType type) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected BaseMatrix<?> castInternal(StoreType type) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX addInternal(PlainComplexFloatMatrixX another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX addInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX subtractInternal(PlainComplexFloatMatrixX another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX subtractInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX subtractRevInternal(PlainComplexFloatMatrixX another)
			throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX subtractRevInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX mulInternal(PlainComplexFloatMatrixX another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX mulTInternal(PlainComplexFloatMatrixX another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX mulRevInternal(PlainComplexFloatMatrixX another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX mulRevTInternal(PlainComplexFloatMatrixX another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX mulHadamardInternal(PlainComplexFloatMatrixX another)
			throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX mulKronekerInternal(PlainComplexFloatMatrixX another)
			throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX mulKronekerRevInternal(PlainComplexFloatMatrixX another)
			throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX mulInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX divInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX divRevInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX aggregateInternal(Piece piece, AggregateDirection dir, AggregateType type)
			throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainComplexFloatMatrixX invertInternal() throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

}
