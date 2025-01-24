package chav1961.purelib.matrix.internal.bit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.matrix.AbstractBaseMatrix;

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
		final PlainBitMatrix result = new PlainBitMatrix(getWidth(), getHeight());
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlainBitMatrix transpose() throws CalculationException {
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
	protected PlainBitMatrix downloadInternal(Piece piece, DataInput in) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix downloadInternal(Piece piece, PlainBitMatrix in) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix uploadInternal(Piece piece, DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix uploadInternal(Piece piece, PlainBitMatrix out) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix cropInternal(Piece piece) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected <AC extends ApplyCallback> PlainBitMatrix applyInternal(Piece piece, AC callback)
			throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix castInternal(ContentType type) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix castInternal(FormatType type) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix castInternal(StoreType type) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix addInternal(PlainBitMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix addInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix subtractInternal(PlainBitMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix subtractInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix subtractRevInternal(PlainBitMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix subtractRevInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix mulInternal(PlainBitMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix mulRevInternal(PlainBitMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix mulHadamardInternal(PlainBitMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
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
	protected PlainBitMatrix mulInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix divInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix divRevInternal(Operand another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix aggregateInternal(Piece piece, AggregateDirection dir, AggregateType type)
			throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PlainBitMatrix invertInternal() throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

}
