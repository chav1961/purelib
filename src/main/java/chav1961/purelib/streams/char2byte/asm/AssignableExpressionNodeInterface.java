package chav1961.purelib.streams.char2byte.asm;

import chav1961.purelib.basic.exceptions.CalculationException;

public interface AssignableExpressionNodeInterface extends ExpressionNodeInterface, Cloneable {
	void assign(final long value) throws CalculationException;
	void assign(final double value) throws CalculationException;
	void assign(final char[] value) throws CalculationException;
	void assign(final boolean value) throws CalculationException;
	boolean hasValue();
	AssignableExpressionNodeInterface clone();
}
