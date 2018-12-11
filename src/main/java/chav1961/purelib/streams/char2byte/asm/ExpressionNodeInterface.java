package chav1961.purelib.streams.char2byte.asm;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.streams.char2byte.asm.macro.ExpressionNodeValue;

public interface ExpressionNodeInterface {
	abstract ExpressionNodeType getType();
	abstract ExpressionNodeValue getValueType();
	abstract long getLong() throws CalculationException;
	abstract double getDouble() throws CalculationException;
	abstract char[] getString() throws CalculationException;
	abstract boolean getBoolean() throws CalculationException;
}
