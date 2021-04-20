package chav1961.purelib.streams.char2byte.asm;

import chav1961.purelib.basic.exceptions.CalculationException;

public interface AssignableExpressionNodeInterface extends ExpressionNodeInterface, Cloneable {
	void assign(long value) throws CalculationException;
	void assign(double value) throws CalculationException;
	void assign(char[] value) throws CalculationException;
	void assign(boolean value) throws CalculationException;
	void assign(long index, long value) throws CalculationException;
	void assign(long index, double value) throws CalculationException;
	void assign(long index, char[] value) throws CalculationException;
	void assign(long index, boolean value) throws CalculationException;
	boolean hasValue();
	boolean hasValue(long index);
	AssignableExpressionNodeInterface clone();
	char[] getName();
}
