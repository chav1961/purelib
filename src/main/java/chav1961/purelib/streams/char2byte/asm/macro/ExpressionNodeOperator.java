package chav1961.purelib.streams.char2byte.asm.macro;

public enum ExpressionNodeOperator {
	OR, AND, NOT,
	EQ, NE, GE, GT, LE, LT,
	TERNARY,
	CAT,
	ADD, SUB, 
	MUL, DIV, MOD,
	NEG,
	ARR_GET,
	F_UG, F_UL, F_EXISTS, 
	F_TO_INT, F_TO_REAL, F_TO_STR, F_TO_BOOL,
	F_TO_LIST,
	F_LEN
}
