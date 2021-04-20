package chav1961.purelib.streams.char2byte.asm;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.streams.char2byte.asm.macro.ExpressionNodeValue;

public interface ExpressionNodeInterface {
	ExpressionNodeType getType();
	ExpressionNodeValue getValueType();
	long getLong() throws CalculationException;
	double getDouble() throws CalculationException;
	char[] getString() throws CalculationException;
	boolean getBoolean() throws CalculationException;
	long getLong(long index) throws CalculationException;
	double getDouble(long index) throws CalculationException;
	char[] getString(long index) throws CalculationException;
	boolean getBoolean(long index) throws CalculationException;
	int getSize() throws CalculationException;
}
