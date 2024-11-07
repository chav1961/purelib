package chav1961.purelib.matrix;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.matrix.AbstractMatrix.Command;
import chav1961.purelib.matrix.AbstractMatrix.CommandTypes;
import chav1961.purelib.matrix.AbstractMatrix.FunctionType;
import chav1961.purelib.matrix.interfaces.Matrix;
import chav1961.purelib.matrix.interfaces.Matrix.Piece;
import chav1961.purelib.matrix.interfaces.Matrix.Type;
import chav1961.purelib.matrix.interfaces.MatrixCalc;

public class AbstractMatrixTest {

	@Test
	public void basicTest() {
		try(final AbstractMatrix	am = new PseudoMarix(Type.REAL_INT, 10, 20)) {
			
			Assert.assertEquals(Type.REAL_INT, am.getType());
			Assert.assertEquals(10, am.numberOfRows());
			Assert.assertEquals(20, am.numberOfColumns());
			Assert.assertEquals(Piece.of(0, 0, 10, 20), am.totalPiece());
			Assert.assertTrue(!am.toHumanReadableString().isEmpty());
			
			Assert.assertTrue(am.areAllAsyncCompleted());
			
			try {
				am.ensureTransactionCompleted();
			} catch (Throwable t) {
				Assert.fail("Unwaited exception detected (transaction is not started)");
			}
			
			am.beginTransaction();
			Assert.assertFalse(am.areAllAsyncCompleted());

			try {
				am.ensureTransactionCompleted();
				Assert.fail("Mandatory exception is not detected detected (call inside transaction)");
			} catch (IllegalStateException t) {
			}
			
			am.done();
			Assert.assertTrue(am.areAllAsyncCompleted());

		}

		try {
			new PseudoMarix(null, 10, 20);
			Assert.fail("Mandatory exception is not detected detected (null 1-st argument)");
		} catch (NullPointerException t) {
		}
	}

	@Test
	public void parsingTest() throws SyntaxException {
		try(final PseudoMarix	am = new PseudoMarix(Type.REAL_INT, 10, 20)) {
			am.prepare("10+3i");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.LOAD_VALUE, new double[] {0,3}), new Command(CommandTypes.ADD)}, am.cmds);
			am.prepare("10-2i");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.LOAD_VALUE, new double[] {0,2}), new Command(CommandTypes.SUBTRACT)}, am.cmds);
			am.prepare("-10");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.NEGATE)}, am.cmds);
			am.prepare("-3i");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {0,3}), new Command(CommandTypes.NEGATE)}, am.cmds);
			am.prepare("%1");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1))}, am.cmds);
			am.prepare("-%1");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.MATRIX_NEGATE)}, am.cmds);
			
			am.prepare("%1+%2");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(2)), new Command(CommandTypes.MATRIX_ADD)}, am.cmds);
			am.prepare("%1+10");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.MATRIX_VALUE_ADD_RIGHT)}, am.cmds);
			am.prepare("10+%1");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.MATRIX_VALUE_ADD_LEFT)}, am.cmds);

			am.prepare("%1-%2");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(2)), new Command(CommandTypes.MATRIX_SUBTRACT)}, am.cmds);
			am.prepare("%1-10");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.MATRIX_VALUE_SUBTRACT_RIGHT)}, am.cmds);
			am.prepare("10-%1");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.MATRIX_VALUE_SUBTRACT_LEFT)}, am.cmds);

			am.prepare("%1*%2");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(2)), new Command(CommandTypes.MATRIX_MUL)}, am.cmds);
			am.prepare("%1*10");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.MATRIX_VALUE_MUL_RIGHT)}, am.cmds);
			am.prepare("10*%1");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.MATRIX_VALUE_MUL_LEFT)}, am.cmds);
			am.prepare("10*20");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.LOAD_VALUE, new double[] {20,0}), new Command(CommandTypes.MULTIPLY)}, am.cmds);

			am.prepare("%1/10");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.MATRIX_VALUE_DIV_RIGHT)}, am.cmds);
			am.prepare("10/%1");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.MATRIX_VALUE_DIV_LEFT)}, am.cmds);
			am.prepare("10/20");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.LOAD_VALUE, new double[] {20,0}), new Command(CommandTypes.DIVIDE)}, am.cmds);

			am.prepare("%1x%2");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(2)), new Command(CommandTypes.HADAMARD_MUL)}, am.cmds);
			am.prepare("%1^%2");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(2)), new Command(CommandTypes.KRONEKER_MUL)}, am.cmds);

			am.prepare("(complex_float)%1");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.CAST, Type.COMPLEX_FLOAT)}, am.cmds);

			am.prepare("sin(%1)");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_MATRIX, Integer.valueOf(1)), new Command(CommandTypes.CALL_FUNCTION, FunctionType.SIN)}, am.cmds);
			am.prepare("zero(2,3)");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {2,0}), new Command(CommandTypes.LOAD_VALUE, new double[] {3,0}), new Command(CommandTypes.CALL_FUNCTION, FunctionType.ZERO)}, am.cmds);

			am.prepare("(10+3i)*(20-6i)");
			Assert.assertArrayEquals(new Command[] {new Command(CommandTypes.LOAD_VALUE, new double[] {10,0}), new Command(CommandTypes.LOAD_VALUE, new double[] {0,3}), new Command(CommandTypes.ADD), new Command(CommandTypes.LOAD_VALUE, new double[] {20,0}), new Command(CommandTypes.LOAD_VALUE, new double[] {0,6}), new Command(CommandTypes.SUBTRACT), new Command(CommandTypes.MULTIPLY)}, am.cmds);
			
			try{am.prepare(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{am.prepare("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{am.prepare("@");
				Assert.fail("Mandatory exception was not detected (unknown lexema)");
			} catch (SyntaxException exc) {
			}
			try{am.prepare("xx");
				Assert.fail("Mandatory exception was not detected (unknown function)");
			} catch (SyntaxException exc) {
			}
			try{am.prepare("1+");
				Assert.fail("Mandatory exception was not detected (missing operand)");
			} catch (SyntaxException exc) {
			}
			try{am.prepare("(1+2");
				Assert.fail("Mandatory exception was not detected (missing close bracket)");
			} catch (SyntaxException exc) {
			}
			try{am.prepare("sin(1");
				Assert.fail("Mandatory exception was not detected (missing close bracket)");
			} catch (SyntaxException exc) {
			}
			try{am.prepare("sin(1,2)");
				Assert.fail("Mandatory exception was not detected (too many arguments)");
			} catch (SyntaxException exc) {
			}
			try{am.prepare("zero(1)");
				Assert.fail("Mandatory exception was not detected (too few arguments)");
			} catch (SyntaxException exc) {
			}
			try{am.prepare("zero(1,%1)");
				Assert.fail("Mandatory exception was not detected (illgal argument types)");
			} catch (SyntaxException exc) {
			}
			try{am.prepare("%1/%2");
				Assert.fail("Mandatory exception was not detected (illegal operand types)");
			} catch (SyntaxException exc) {
			}
		}
	}
}

class PseudoMarix extends AbstractMatrix {
	Command[]	cmds;

	PseudoMarix(Type type, int rows, int cols) {
		super(type, rows, cols);
	}

	@Override public void close() throws RuntimeException {}
	@Override public boolean deepEquals(Matrix another) {return false;}
	@Override public int[] extractInts(Piece piece) {return null;}
	@Override public void extractInts(Piece piece, DataOutput dataOutput) throws IOException {}
	@Override public long[] extractLongs(Piece piece) {return null;}
	@Override public void extractLongs(Piece piece, DataOutput dataOutput) throws IOException {}
	@Override public float[] extractFloats(Piece piece) {return null;}
	@Override public void extractFloats(Piece piece, DataOutput dataOutput) throws IOException {}
	@Override public double[] extractDoubles(Piece piece) {return null;}

	@Override 
	public void extractDoubles(Piece piece, DataOutput dataOutput) throws IOException {
		for(int index = 0, maxIndex= numberOfRows() * numberOfColumns(); index < maxIndex; index++) {
			dataOutput.writeDouble(index);
		}
	}

	@Override public Matrix assign(Piece piece, int... content) {return null;}
	@Override public Matrix assign(Piece piece, long... content) {return null;}
	@Override public Matrix assign(Piece piece, float... content) {return null;}
	@Override public Matrix assign(Piece piece, double... content) {return null;}
	@Override public Matrix assign(Piece piece, Matrix content) {return null;}
	@Override public Matrix assign(Piece piece, DataInput content, Type type) throws IOException {return null;}
	@Override public Matrix fill(Piece piece, int value) {return null;}
	@Override public Matrix fill(Piece piece, long value) {return null;}
	@Override public Matrix fill(Piece piece, float value) {return null;}
	@Override public Matrix fill(Piece piece, float real, float image) {return null;}
	@Override public Matrix fill(Piece piece, double value) {return null;}
	@Override public Matrix fill(Piece piece, double real, double image) {return null;}
	@Override public Matrix cast(Type type) {return null;}
	@Override public Matrix add(int... content) {return null;}
	@Override public Matrix add(long... content) {return null;}
	@Override public Matrix add(float... content) {return null;}
 	@Override public Matrix add(double... content) {return null;}
	@Override public Matrix add(Matrix content) {return null;}
	@Override public Matrix addValue(int value) {return null;}
	@Override public Matrix addValue(long value) {return null;}
	@Override public Matrix addValue(float value) {return null;}
	@Override public Matrix addValue(float real, float image) {return null;}
	@Override public Matrix addValue(double value) {return null;}
	@Override public Matrix addValue(double real, double image) {return null;}
	@Override public Matrix subtract(int... content) {return null;}
	@Override public Matrix subtract(long... content) {return null;}
 	@Override public Matrix subtract(float... content) {return null;}
	@Override public Matrix subtract(double... content) {return null;}
	@Override public Matrix subtract(Matrix content) {return null;}
	@Override public Matrix subtractValue(int value) {return null;}
	@Override public Matrix subtractValue(long value) {return null;}
	@Override public Matrix subtractValue(float value) {return null;}
	@Override public Matrix subtractValue(float real, float image) {return null;}
	@Override public Matrix subtractValue(double value) {return null;}
	@Override public Matrix subtractValue(double real, double image) {return null;}
	@Override public Matrix subtractFrom(int... content) {return null;}
	@Override public Matrix subtractFrom(long... content) {return null;}
	@Override public Matrix subtractFrom(float... content) {return null;}
	@Override public Matrix subtractFrom(double... content) {return null;}
	@Override public Matrix subtractFrom(Matrix content) {return null;}
	@Override public Matrix subtractFromValue(int value) {return null;}
	@Override public Matrix subtractFromValue(long value) {return null;}
	@Override public Matrix subtractFromValue(float value) {return null;}
	@Override public Matrix subtractFromValue(float real, float image) {return null;}
	@Override public Matrix subtractFromValue(double value) {return null;}
 	@Override public Matrix subtractFromValue(double real, double image) {return null;}
 	@Override public Matrix mul(Matrix content) {return null;}
	@Override public Matrix mulFrom(Matrix content) {return null;}
	@Override public Matrix mulValue(int value) {return null;}
	@Override public Matrix mulValue(long value) {return null;}
	@Override public Matrix mulValue(float value) {return null;}
	@Override public Matrix mulValue(float real, float image) {return null;}
	@Override public Matrix mulValue(double value) {return null;}
	@Override public Matrix mulValue(double real, double image) {return null;}
	@Override public Matrix divValue(int value) {return null;}
	@Override public Matrix divValue(long value) {return null;}
	@Override public Matrix divValue(float value) {return null;}
	@Override public Matrix divValue(float real, float image) {return null;}
	@Override public Matrix divValue(double value) {return null;}
 	@Override public Matrix divValue(double real, double image) {return null;}
	@Override public Matrix divFromValue(int value) {return null;}
	@Override public Matrix divFromValue(long value) {return null;}
	@Override public Matrix divFromValue(float value) {return null;}
	@Override public Matrix divFromValue(float real, float image) {return null;}
	@Override public Matrix divFromValue(double value) {return null;}
	@Override public Matrix divFromValue(double real, double image) {return null;}
	@Override public Matrix mulHadamard(int... content) {return null;}
	@Override public Matrix mulHadamard(long... content) {return null;}
	@Override public Matrix mulHadamard(float... content) {return null;}
	@Override public Matrix mulHadamard(double... content) {return null;}
	@Override public Matrix mulHadamard(Matrix content) {return null;}
	@Override public Matrix mulInvHadamard(int... content) {return null;}
	@Override public Matrix mulInvHadamard(long... content) {return null;}
	@Override public Matrix mulInvHadamard(float... content) {return null;}
	@Override public Matrix mulInvHadamard(double... content) {return null;}
	@Override public Matrix mulInvHadamard(Matrix content) {return null;}
	@Override public Matrix mulInvFromHadamard(int... content) {return null;}
	@Override public Matrix mulInvFromHadamard(long... content) {return null;}
	@Override public Matrix mulInvFromHadamard(float... content) {return null;}
	@Override public Matrix mulInvFromHadamard(double... content) {return null;}
	@Override public Matrix mulInvFromHadamard(Matrix content) {return null;}
	@Override public Matrix tensorMul(Matrix content) {return null;}
 	@Override public Matrix tensorMulFrom(Matrix content) {return null;}
	@Override public Matrix invert() {return null;}
	@Override public Matrix transpose() {return null;}
	@Override public Matrix aggregate(AggregateDirection dir, AggregateType aggType) {return null;}
	@Override public Number det() {return null;}
	@Override public Number track() {return null;}
	@Override public Number[] det2() {return null;}
	@Override public Number[] track2() {return null;}
	@Override protected void lastCall() {}
	
	@Override 
	protected MatrixCalc buildMatrixCalc(final Command... cmds) throws SyntaxException {
		this.cmds = cmds;
		return null;
	}
}