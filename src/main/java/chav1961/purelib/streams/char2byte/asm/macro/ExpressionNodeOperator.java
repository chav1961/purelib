package chav1961.purelib.streams.char2byte.asm.macro;

public enum ExpressionNodeOperator {
	OR(true), AND(true), NOT(true),
	EQ(true), NE(true), GE(true), GT(true), LE(true), LT(true),
	TERNARY(true),
	CAT(true),
	ADD(true), SUB(true), 
	MUL(true), DIV(true), MOD(true),
	NEG(true),
	ARR_GET(true),
	F_UG(false), F_UL(false), F_ENVIRONMENT(false), F_EXISTS(true), 
	F_TO_INT(true), F_TO_REAL(true), F_TO_STR(true), F_TO_BOOL(true),
	F_TO_LIST(true),
	F_LEN(true);
	
	private final boolean inDeterministic;
	
	private ExpressionNodeOperator(final boolean inDeterministic) {
		this.inDeterministic = inDeterministic;				
	}
	
	public boolean isDeterministic() {
		return inDeterministic;
	}
}
