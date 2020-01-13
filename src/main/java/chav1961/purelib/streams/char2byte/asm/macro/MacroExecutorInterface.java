package chav1961.purelib.streams.char2byte.asm.macro;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.streams.char2byte.asm.AssignableExpressionNodeInterface;

public interface MacroExecutorInterface {
	void exec(AssignableExpressionNodeInterface[] memory, GrowableCharArray target) throws CalculationException;
}
