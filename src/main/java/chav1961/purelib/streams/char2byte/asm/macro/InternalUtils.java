package chav1961.purelib.streams.char2byte.asm.macro;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.intern.UnsafedCharUtils;

class InternalUtils {
	private static final SyntaxTreeInterface<FuncDecription>	FUNCTIONS = new AndOrTree<>();
	
	static final char[]				FALSE = "false".toCharArray();
	static final char[]				TRUE = "true".toCharArray();
	
	static final char[]				TYPE_INT = "int".toCharArray();
	static final char[]				TYPE_REAL = "real".toCharArray();
	static final char[]				TYPE_STRING = "str".toCharArray();
	static final char[]				TYPE_BOOLEAN = "bool".toCharArray();
	static final char[]				TYPE_ARRAY = "[]".toCharArray();

	static final int				ORDER_TERM = 0;
	static final int				ORDER_UNARY = 1;
	static final int				ORDER_MUL = 2;
	static final int				ORDER_ADD = 3;
	static final int				ORDER_CAT = 4;
	static final int				ORDER_TERNARY = 5;
	static final int				ORDER_COMPARE = 6;
	static final int				ORDER_NOT = 7;
	static final int				ORDER_AND = 8;
	static final int				ORDER_OR = 9;

	private static final int		FUNC_UNIQUEL = 0;
	private static final int		FUNC_UNIQUEG = 1;
	private static final int		FUNC_EXISTS = 2;
	private static final int		FUNC_INT = 3;
	private static final int		FUNC_REAL = 4;
	private static final int		FUNC_STR = 5;
	private static final int		FUNC_BOOL = 6;
	private static final int		FUNC_LEN = 7;
	private static final int		FUNC_ENVIRONMENT = 8;
	
	static {
		FUNCTIONS.placeName("uniqueL",FUNC_UNIQUEL,new FuncDecription(ExpressionNodeOperator.F_UL,0,ExpressionNodeValue.INTEGER));
		FUNCTIONS.placeName("uniqueG",FUNC_UNIQUEG,new FuncDecription(ExpressionNodeOperator.F_UG,0,ExpressionNodeValue.INTEGER));
		FUNCTIONS.placeName("exists",FUNC_EXISTS,new FuncDecription(ExpressionNodeOperator.F_EXISTS,1,ExpressionNodeValue.BOOLEAN));
		FUNCTIONS.placeName("int",FUNC_INT,new FuncDecription(ExpressionNodeOperator.F_TO_INT,1,ExpressionNodeValue.INTEGER));
		FUNCTIONS.placeName("real",FUNC_REAL,new FuncDecription(ExpressionNodeOperator.F_TO_REAL,1,ExpressionNodeValue.REAL));
		FUNCTIONS.placeName("str",FUNC_STR,new FuncDecription(ExpressionNodeOperator.F_TO_STR,1,ExpressionNodeValue.STRING));
		FUNCTIONS.placeName("bool",FUNC_BOOL,new FuncDecription(ExpressionNodeOperator.F_TO_BOOL,1,ExpressionNodeValue.BOOLEAN));
		FUNCTIONS.placeName("len",FUNC_LEN,new FuncDecription(ExpressionNodeOperator.F_LEN,1,ExpressionNodeValue.INTEGER));
		FUNCTIONS.placeName("environment",FUNC_ENVIRONMENT,new FuncDecription(ExpressionNodeOperator.F_ENVIRONMENT,1,ExpressionNodeValue.STRING));
	}
	
	static int skipBlank(final char[] data, int from) {
		char	symbol;
		
		while ((symbol = data[from]) <= ' ' && symbol != '\n') {
			from++;
		}
		if (symbol == '/' && data[from+1] == '/') {
			from += 2;
			while (data[from] != '\n') {
				from++;
			}
		}
		return from;
	}

	static int skipNonBlank(final char[] data, int from) {
		while (data[from] > ' ') {
			from++;
		}
		return from;
	} 

	static int parseConstant(final char[] data, int from, final boolean treatUnknownAsString, final boolean convert2PreferredType, final ExpressionNodeValue preferredType, final ExpressionNode[] result) throws CalculationException, SyntaxException {
		switch (data[from = skipBlank(data,from)]) {
			case '\"' :
				final StringBuilder	sb = new StringBuilder();
				
				try{from = UnsafedCharUtils.uncheckedParseString(data,from+1,'\"',sb);
				} catch (IOException e) {
					throw new IllegalArgumentException(e.getLocalizedMessage());
				}
				if (convert2PreferredType) {
					switch (preferredType) {
						case BOOLEAN	:
							result[0] = new ConstantNode(Boolean.valueOf(sb.toString()));
							break;
						case INTEGER	:
							result[0] = new ConstantNode(Long.valueOf(sb.toString()));
							break;
						case REAL		:
							result[0] = new ConstantNode(Double.valueOf(sb.toString()));
							break;
						case STRING		:
							result[0] = new ConstantNode(sb.toString().toCharArray());
							break;
						case BOOLEAN_ARRAY	:
							result[0] = new ConstantNode(new boolean[] {Boolean.valueOf(sb.toString())});
							break;
						case INTEGER_ARRAY	:
							result[0] = new ConstantNode(new long[] {Long.valueOf(sb.toString())});
							break;
						case REAL_ARRAY		:
							result[0] = new ConstantNode(new double[] {Double.valueOf(sb.toString())});
							break;
						case STRING_ARRAY		:
							result[0] = new ConstantNode(new char[][] {sb.toString().toCharArray()});
							break;
						default : throw new UnsupportedOperationException("Conversion from String to ["+preferredType+"] is not supported yet");
					}
				}
				else {
					result[0] = new ConstantNode(sb.toString().toCharArray());
				}
				return from;
			case '-' :
				from = parseConstant(data, skipBlank(data,from+1), treatUnknownAsString, convert2PreferredType,  preferredType, result);
				switch (result[0].getValueType()) {
					case INTEGER	: result[0] = new ConstantNode(-result[0].getLong()); break;
					case REAL		: result[0] = new ConstantNode(-result[0].getDouble()); break;
					default : throw new IllegalArgumentException("Minus sign (-) is not applicable for the constant type ["+result[0].getValueType()+"]!");
				}
				return from;
			case '+' :
				from = parseConstant(data,skipBlank(data,from+1), treatUnknownAsString, convert2PreferredType,  preferredType, result);
				if (result[0].getValueType() != ExpressionNodeValue.INTEGER && result[0].getValueType() != ExpressionNodeValue.REAL) {
					throw new IllegalArgumentException("Plus sign (+) is not applicable for the constant type ["+result[0].getValueType()+"]!");
				}
				else {
					return from;
				}
			case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
				final long[]	numbers = new long[2];
				
				from = UnsafedCharUtils.uncheckedParseNumber(data,from,numbers,CharUtils.PREF_ANY, true);
				switch ((int)numbers[1]) {
					case CharUtils.PREF_INT		: case CharUtils.PREF_LONG	: 
						if (convert2PreferredType) {
							switch (preferredType) {
								case INTEGER	:
									result[0] = new ConstantNode(numbers[0]);
									break;
								case REAL		:
									result[0] = new ConstantNode((double)numbers[0]); 
									break;
								case STRING		:
									result[0] = new ConstantNode(String.valueOf(numbers[0]).toCharArray());
									break;
								case INTEGER_ARRAY 	:
									result[0] = new ConstantNode(new long[] {numbers[0]});
									break;
								case REAL_ARRAY		:
									result[0] = new ConstantNode(new double[] {(double)numbers[0]});
									break;
								case STRING_ARRAY	:
									result[0] = new ConstantNode(new char[][] {String.valueOf(numbers[0]).toCharArray()});
									break;
								default : throw new UnsupportedOperationException("Conversion from long to ["+preferredType+"] is not supported yet");
							}
						}
						else {
							result[0] = new ConstantNode(numbers[0]); 
						}
						break;
					case CharUtils.PREF_FLOAT	: 
						double	floatVal = Float.intBitsToFloat((int)numbers[0]);
						
						if (convert2PreferredType) {
							switch (preferredType) {
								case INTEGER	:
									result[0] = new ConstantNode((long)floatVal);
									break;
								case REAL		:
									result[0] = new ConstantNode(floatVal); 
									break;
								case STRING		:
									result[0] = new ConstantNode(String.valueOf(floatVal).toCharArray());
									break;
								case INTEGER_ARRAY 	:
									result[0] = new ConstantNode(new long[] {(long)floatVal});
									break;
								case REAL_ARRAY		:
									result[0] = new ConstantNode(new double[] {floatVal});
									break;
								case STRING_ARRAY	:
									result[0] = new ConstantNode(new char[][] {String.valueOf(floatVal).toCharArray()});
									break;
								default : throw new UnsupportedOperationException("Conversion from real to ["+preferredType+"] is not supported yet");
							}
						}
						else {
							result[0] = new ConstantNode(floatVal); 
						}
						break;
					case CharUtils.PREF_DOUBLE	:
						double	doubleVal = Double.longBitsToDouble(numbers[0]);
						
						if (convert2PreferredType) {
							switch (preferredType) {
								case INTEGER	:
									result[0] = new ConstantNode((long)doubleVal);
									break;
								case REAL		:
									result[0] = new ConstantNode(doubleVal); 
									break;
								case STRING		:
									result[0] = new ConstantNode(String.valueOf(doubleVal).toCharArray());
									break;
								case INTEGER_ARRAY 	:
									result[0] = new ConstantNode(new long[] {(long)doubleVal});
									break;
								case REAL_ARRAY		:
									result[0] = new ConstantNode(new double[] {doubleVal});
									break;
								case STRING_ARRAY	:
									result[0] = new ConstantNode(new char[][] {String.valueOf(doubleVal).toCharArray()});
									break;
								default : throw new UnsupportedOperationException("Conversion from real to ["+preferredType+"] is not supported yet");
							}
						}
						else {
							result[0] = new ConstantNode(doubleVal); 
						}
						break;
					default : throw new UnsupportedOperationException("Numeric value type ["+numbers[1]+"] is not supported yet");
				}
				return from;
			case 't' : case 'f' :
				if (UnsafedCharUtils.uncheckedCompare(data,from,FALSE,0,FALSE.length)) {
					if (convert2PreferredType) {
						switch (preferredType) {
							case BOOLEAN	:
								result[0] = new ConstantNode(false);
								break;
							case STRING		:
								result[0] = new ConstantNode("false".toCharArray());
								break;
							default : throw new UnsupportedOperationException("Conversion from String to ["+preferredType+"] is not supported yet");
						}
					}
					else {
						result[0] = new ConstantNode(false);
					}
					return from + FALSE.length;
				}
				else if (UnsafedCharUtils.uncheckedCompare(data,from,TRUE,0,TRUE.length)) {
					if (convert2PreferredType) {
						switch (preferredType) {
							case BOOLEAN	:
								result[0] = new ConstantNode(true);
								break;
							case STRING		:
								result[0] = new ConstantNode("true".toCharArray());
								break;
							default : throw new UnsupportedOperationException("Conversion from String to ["+preferredType+"] is not supported yet");
						}
					}
					else {
						result[0] = new ConstantNode(true);
					}
					return from + TRUE.length;
				}
				else if (treatUnknownAsString) {
					final int	start = from;
					int			to;
					
					while (data[from] != '\n' && data[from] != '\r' && data[from] != ',' && data[from] != '=') {
						from++;
					}
					if (data[from] == ',' || data[from] == '=') {
						to = from;
					}
					else {
						to = from-1;
					}
					while (to > start && data[to] <= ' ') {
						to--;
					}
					if (convert2PreferredType) {
						switch (preferredType) {
							case BOOLEAN	:
								result[0] = new ConstantNode(Boolean.valueOf(new String(Arrays.copyOfRange(data,start,to))));
								break;
							case INTEGER	:
								result[0] = new ConstantNode(Long.valueOf(new String(Arrays.copyOfRange(data,start,to))));
								break;
							case REAL		:
								result[0] = new ConstantNode(Double.valueOf(new String(Arrays.copyOfRange(data,start,to))));
								break;
							case STRING		:
								result[0] = new ConstantNode(Arrays.copyOfRange(data,start,to));
								break;
							default : throw new UnsupportedOperationException("Conversion from String to ["+preferredType+"] is not supported yet");
						}
					}
					else {
						result[0] = new ConstantNode(Arrays.copyOfRange(data,start,to));
					}
					return from;
				}
				else {
					throw new IllegalArgumentException("Invalid constant value: neither 'true' nor 'false' was detected");
				}
			case '{' :
				final List<ExpressionNode>	itemList = new ArrayList<>();
				from = skipBlank(data,from+1);
				
				if (data[from] == '}') {
					result[0] = new ConstantNode(convert2PreferredType ? preferredType : ExpressionNodeValue.STRING_ARRAY, 0);
					return from + 1;
				}
				else {
					from--;
				}
				
				do {from = skipBlank(data,parseConstant(data, from+1, treatUnknownAsString, convert2PreferredType, ExpressionNodeValue.typeByArray(preferredType), result));
					itemList.add(result[0]);
				} while (data[from] == ',');
				
				if (data[from] == '}') {
					final ExpressionNodeValue	arrayValueType = itemList.isEmpty() 
																? (convert2PreferredType ? preferredType : ExpressionNodeValue.STRING_ARRAY) 
																: ExpressionNodeValue.arrayByType(itemList.get(0).getValueType());
					final ExpressionNodeValue	valueType = ExpressionNodeValue.typeByArray(arrayValueType);
					
					result[0] = new ConstantNode(arrayValueType, itemList.size());
					for (int index = 0; index < itemList.size(); index++) {
						switch (valueType) {
							case BOOLEAN	:
								result[0].getBooleanContent()[index] = itemList.get(index).getBoolean();
								break;
							case INTEGER	:
								result[0].getLongContent()[index] = itemList.get(index).getLong();
								break;
							case REAL		:
								result[0].getDoubleContent()[index] = itemList.get(index).getDouble();
								break;
							case STRING		:
								result[0].getStringContent()[index] = itemList.get(index).getString();
								break;
							default :
								throw new UnsupportedOperationException("Value type ["+arrayValueType+"] is not supported yet");
						}
					}
					from++;
				}
				else {
					throw new IllegalArgumentException("Missing '}' in the array list");
				}
				return from;
			default : throw new IllegalArgumentException("Invalid constant value (constant can't be started with ["+data[from]+"])");
		}
	}

	static int parseExpressionList(final int order, final int lineNo, final char[] data, final int begin, int from, final MacroCommand macro, final ExpressionNodeValue valueType, final ExpressionNode[] result) throws SyntaxException {
		final int				pos[] = new int[]{from};
		final ExpressionNode	node;
		
		try{pos[0] = InternalUtils.skipBlank(data,pos[0]);
			
			if (data[pos[0]] == '{') {
				final List<ExpressionNode>	itemList = new ArrayList<>();
				
				do {pos[0]++;
					itemList.add(parseExpression(order,data,pos,macro)); 
					pos[0] = InternalUtils.skipBlank(data,pos[0]);
				} while (data[pos[0]] == ',');
				
				if (data[pos[0]] == '}') {
					node = new ConstantNode(valueType,itemList.toArray(new ExpressionNode[itemList.size()])); 
					pos[0]++;
				}
				else {
					throw new SyntaxException(lineNo,pos[0]-from,"Missing '}'");
				}
			}
			else {
				node = new ConstantNode(valueType,result); 
			}
			result[0] = node;
			return pos[0];
		} catch (IllegalArgumentException | CalculationException exc) {
			exc.printStackTrace();
			throw new SyntaxException(lineNo,pos[0]-begin,exc.getLocalizedMessage()); 
		}
	}
	
	static int parseExpression(final int order, final int lineNo, final char[] data, final int begin, final int from, final MacroCommand macro, final ExpressionNode[] result) throws SyntaxException {
		final int	pos[] = new int[]{from};
		
		try{result[0] = parseExpression(order,data,pos,macro); 
			return pos[0];
		} catch (IllegalArgumentException exc) {
			exc.printStackTrace();
			throw new SyntaxException(lineNo,pos[0]-begin,exc.getLocalizedMessage()); 
		}
	}

	static ExpressionNodeValue defineType(final char[] data, final int[] bounds, final boolean isArray) {
		if (UnsafedCharUtils.uncheckedCompare(data,bounds[0],InternalUtils.TYPE_INT,0,InternalUtils.TYPE_INT.length)) {
			return isArray ? ExpressionNodeValue.INTEGER_ARRAY : ExpressionNodeValue.INTEGER;
		}
		else if (UnsafedCharUtils.uncheckedCompare(data,bounds[0],InternalUtils.TYPE_REAL,0,InternalUtils.TYPE_REAL.length)) {
			return isArray ? ExpressionNodeValue.REAL_ARRAY : ExpressionNodeValue.REAL;
		}
		else if (UnsafedCharUtils.uncheckedCompare(data,bounds[0],InternalUtils.TYPE_STRING,0,InternalUtils.TYPE_STRING.length)) {
			return isArray ? ExpressionNodeValue.STRING_ARRAY : ExpressionNodeValue.STRING;
		}
		else if (UnsafedCharUtils.uncheckedCompare(data,bounds[0],InternalUtils.TYPE_BOOLEAN,0,InternalUtils.TYPE_BOOLEAN.length)) {
			return isArray ? ExpressionNodeValue.BOOLEAN_ARRAY : ExpressionNodeValue.BOOLEAN;
		}
		else {
			throw new IllegalArgumentException("Unknown data type ["+new String(data,bounds[0],bounds[1]-bounds[0]+1)+"]");
		}
	}

	private static ExpressionNode parseExpression(final int order, final char[] data, final int[] from, final MacroCommand macro) throws IllegalArgumentException, SyntaxException {
		ExpressionNode	operand;
		OperatorNode	operator;
		
		from[0] = skipBlank(data,from[0]);
		switch (order) {
			case ORDER_OR		:
				operand = parseExpression(order-1,data,from,macro);
				from[0] = skipBlank(data,from[0]);
				if (data[from[0]] == '|' && data[from[0]+1] == '|') {
					if (operand.getValueType() == ExpressionNodeValue.BOOLEAN) {
						operator = new OrNode().addOperand(operand);
						do {from[0] = skipBlank(data,from[0] + 2);
							operand = parseExpression(order-1,data,from,macro);
							if (operand.getValueType() == ExpressionNodeValue.BOOLEAN) {
								((OperatorListNode)operator).addOperand(operand);
							}
							else {
								throw new IllegalArgumentException("Non-boolean operand in the '||' operator. Use conversion functions!"); 
							}
							from[0] = skipBlank(data,from[0]);
						} while (data[from[0]] == '|' && data[from[0]+1] == '|');
						return operator;
					}
					else {
						throw new IllegalArgumentException("Non-boolean operand in the '||' operator. Use conversion functions!"); 
					}
				}
				else {
					return operand;
				}
			case ORDER_AND		:
				operand = parseExpression(order-1,data,from,macro);
				from[0] = skipBlank(data,from[0]);
				if (data[from[0]] == '&' && data[from[0]+1] == '&') {
					if (operand.getValueType() == ExpressionNodeValue.BOOLEAN) {
						operator = new AndNode().addOperand(operand);
						do {from[0] = skipBlank(data,from[0] + 2);
							operand = parseExpression(order-1,data,from,macro);
							if (operand.getValueType() == ExpressionNodeValue.BOOLEAN) {
								((OperatorListNode)operator).addOperand(operand);
							}
							else {
								throw new IllegalArgumentException("Non-boolean operand in the '&&' operator. Use conversion functions!"); 
							}
							from[0] = skipBlank(data,from[0]);
						} while (data[from[0]] == '&' && data[from[0]+1] == '&');
						return operator;
					}
					else {
						throw new IllegalArgumentException("Non-boolean operand in the '&&' operator. Use conversion functions!"); 
					}
				}
				else {
					return operand;
				}
			case ORDER_NOT		:
				if (data[from[0]] == '!' && data[from[0]+1] != '=') {
					from[0] = skipBlank(data,from[0]+1);
					operand = parseExpression(order-1,data,from,macro);
					
					if (operand.getValueType() == ExpressionNodeValue.BOOLEAN) {
						return new NotNode(operand);
					}
					else {
						throw new IllegalArgumentException("Non-boolean operand in the '!' operator. Use conversion functions!"); 
					}
				}
				else {
					return parseExpression(order-1,data,from,macro); 
				}
			case ORDER_COMPARE	:
				ExpressionNodeOperator	op;
				
				operand = parseExpression(order-1,data,from,macro);
				from[0] = skipBlank(data,from[0]);
				switch (data[from[0]]) {
					case '>' 	:
						if (data[from[0]+1] == '=') {
							from[0] += 2;
							op = ExpressionNodeOperator.GE;
						}
						else {
							from[0]++;
							op = ExpressionNodeOperator.GT;
						}
						break;
					case '<' 	:
						if (data[from[0]+1] == '=') {
							from[0] += 2;
							op = ExpressionNodeOperator.LE;
						}
						else {
							from[0]++;
							op = ExpressionNodeOperator.LT;
						}
						break;
					case '=' 	:
						if (data[from[0]+1] == '=') {
							from[0] += 2;
							op = ExpressionNodeOperator.EQ;
						}
						else {
							throw new IllegalArgumentException("Unknown comparison operator"); 
							
						}
						break;
					case '!' 	:
						if (data[from[0]+1] == '=') {
							from[0] += 2;
							op = ExpressionNodeOperator.NE;
						}
						else {
							throw new IllegalArgumentException("Unknown comparison operator"); 
						}
						break;
					default 	: return operand;
				}
				from[0] = skipBlank(data,from[0]);
				
				final ExpressionNode	right = parseExpression(order-1,data,from,macro);
				if (operand.getValueType() != right.getValueType()) {
					throw new IllegalArgumentException("Comparison error: different value types for left and right argument! Use conversion functions!"); 
				}
				else if (operand.getValueType() == ExpressionNodeValue.BOOLEAN && op != ExpressionNodeOperator.EQ && op != ExpressionNodeOperator.NE) {
					throw new IllegalArgumentException("Comparison error: illegal comparison for booleans. Only (==) and (!=) are available!"); 
				}
				else {
					return new ComparisonNode(op,operand,right);
				}
			case ORDER_TERNARY	:
				operand = parseExpression(order-1,data,from,macro);
				from[0] = skipBlank(data,from[0]);
				if (data[from[0]] == '?') {
					if (operand.getValueType() == ExpressionNodeValue.BOOLEAN) {
						ExpressionNode 	onTrue, onFalse;
						
						from[0] = skipBlank(data,from[0]+1);
						onTrue = parseExpression(ORDER_OR,data,from,macro);
						from[0] = skipBlank(data,from[0]);

						if (data[from[0]] == ':') {
							from[0] = skipBlank(data,from[0]+1);
							onFalse = parseExpression(ORDER_OR,data,from,macro);
							from[0] = skipBlank(data,from[0]);
							
							if (onTrue.getValueType() != onFalse.getValueType()) {
								throw new IllegalArgumentException("Different value types for true and false clauses! Use conversion functions!"); 
							}
							else {
								return new TernaryOperatorNode(operand,onTrue,onFalse);
							}
						}
						else {
							throw new IllegalArgumentException("Colon (:) is missing!"); 
						}
					}
					else {
						throw new IllegalArgumentException("Non-boolean operand in the '?' operator. Use conversion functions!"); 
					}
				}
				else {
					return operand; 
				}
			case ORDER_CAT		:
				operand = parseExpression(order-1,data,from,macro);
				from[0] = skipBlank(data,from[0]);
				if (data[from[0]] == '#') {
					if (operand.getValueType() != ExpressionNodeValue.STRING) {
						operand = buildConvertNode(ExpressionNodeValue.STRING,operand);
					}
					operator = new CatNode().addOperand(operand);
					do {from[0] = skipBlank(data,from[0] + 1);
						operand = parseExpression(order-1,data,from,macro);
						if (operand.getValueType() != ExpressionNodeValue.STRING) {
							operand = buildConvertNode(ExpressionNodeValue.STRING,operand);
						}
						((OperatorListNode)operator).addOperand(operand);
						from[0] = skipBlank(data,from[0]);
					} while (data[from[0]] == '#');
					return operator;
				}
				else {
					return operand;
				}
			case ORDER_ADD		:
				operand = parseExpression(order-1,data,from,macro);
				from[0] = skipBlank(data,from[0]);
				if (data[from[0]] == '+' || data[from[0]] == '-') {
					if (operand.getValueType() == ExpressionNodeValue.INTEGER || operand.getValueType() == ExpressionNodeValue.REAL) {
						ExpressionNode	left = operand;
						char			oper;
						
						do {oper = data[from[0]];
							from[0] = skipBlank(data,from[0]+1);
							operand = parseExpression(order-1,data,from,macro);
							if (operand.getValueType() == ExpressionNodeValue.INTEGER || operand.getValueType() == ExpressionNodeValue.REAL) {
								if (left.getValueType() != operand.getValueType()) {
									if (left.getValueType() == ExpressionNodeValue.INTEGER) {
										left = buildConvertNode(ExpressionNodeValue.REAL,left);
									}
									else {
										operand = buildConvertNode(ExpressionNodeValue.REAL,operand);
									}
								}
								left = new ArithmeticNode(oper == '+' ? ExpressionNodeOperator.ADD : ExpressionNodeOperator.SUB,left,operand);
							}
							else {
								throw new IllegalArgumentException("Non-numeric operand in the '+','-' operator. Use conversion functions!"); 
							}
							from[0] = skipBlank(data,from[0]);
						} while (data[from[0]] == '+' || data[from[0]] == '-');
						return left;
					}
					else {
						throw new IllegalArgumentException("Non-numeric operand in the '+','-' operator. Use conversion functions!"); 
					}
				}
				else {
					return operand;
				}
			case ORDER_MUL		:
				operand = parseExpression(order-1,data,from,macro);
				from[0] = skipBlank(data,from[0]);
				if (data[from[0]] == '*' || data[from[0]] == '/' || data[from[0]] == '%') {
					if (operand.getValueType() == ExpressionNodeValue.INTEGER || operand.getValueType() == ExpressionNodeValue.REAL) {
						ExpressionNode	left = operand;
						char			oper;
						
						do {oper = data[from[0]];
							from[0] = skipBlank(data,from[0]+1);
							operand = parseExpression(order-1,data,from,macro);
							if (operand.getValueType() == ExpressionNodeValue.INTEGER || operand.getValueType() == ExpressionNodeValue.REAL) {
								if (left.getValueType() != operand.getValueType()) {
									if (left.getValueType() == ExpressionNodeValue.INTEGER) {
										left = buildConvertNode(ExpressionNodeValue.REAL,left);
									}
									else {
										operand = buildConvertNode(ExpressionNodeValue.REAL,operand);
									}
								}
								left = new ArithmeticNode(oper == '*' ? ExpressionNodeOperator.MUL : (oper == '/' ? ExpressionNodeOperator.DIV : ExpressionNodeOperator.MOD),left,operand);
							}
							else {
								throw new IllegalArgumentException("Non-numeric operand in the '*','/','%' operator. Use conversion functions!"); 
							}
							from[0] = skipBlank(data,from[0]);
						} while (data[from[0]] == '*' || data[from[0]] == '/' || data[from[0]] == '%');
						return left;
					}
					else {
						throw new IllegalArgumentException("Non-numeric operand in the '*','/','%' operator. Use conversion functions!"); 
					}
				}
				else {
					return operand;
				}
			case ORDER_UNARY	:
				if (data[from[0]] == '-') {
					from[0] = skipBlank(data,from[0]+1);
					operand = parseExpression(order-1,data,from,macro);
					
					if (operand.getValueType() == ExpressionNodeValue.INTEGER || operand.getValueType() == ExpressionNodeValue.REAL) {
						return new NegNode(operand);
					}
					else {
						throw new IllegalArgumentException("Non-numeric operand in the '-' operator. Use conversion functions!"); 
					}
				}
				else {
					return parseExpression(order-1,data,from,macro); 
				}
			case ORDER_TERM		:
				switch (data[from[0]]) {
					case '\"' 	:
						final StringBuilder	sb = new StringBuilder();
						
						try{from[0] = UnsafedCharUtils.uncheckedParseString(data,from[0]+1,'\"',sb);
						} catch (IOException e) {
							throw new IllegalArgumentException(e.getLocalizedMessage());
						}
						return new ConstantNode(sb.toString().toCharArray());
					case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						final long[]	numbers = new long[2];
						
						from[0] = UnsafedCharUtils.uncheckedParseNumber(data,from[0],numbers,CharUtils.PREF_ANY, true);
						switch ((int)numbers[1]) {
							case CharUtils.PREF_INT		: return new ConstantNode(numbers[0]);
							case CharUtils.PREF_LONG	: return new ConstantNode(numbers[0]);
							case CharUtils.PREF_FLOAT	: return new ConstantNode(Float.intBitsToFloat((int)numbers[0]));
							case CharUtils.PREF_DOUBLE	: return new ConstantNode(Double.longBitsToDouble(numbers[0]));
							default : throw new UnsupportedOperationException("Numeric value type ["+numbers[1]+"] is not supported yet");
						}
					case '('	:
						from[0]++;
						operand = parseExpression(ORDER_OR,data,from,macro);
						from[0] = skipBlank(data,from[0]);
						if (data[from[0]] == ')') {
							from[0]++;
							return operand;
						}
						else {
							throw new IllegalArgumentException("Missing ')'"); 
						}
					default :
						if (Character.isJavaIdentifierStart(data[from[0]])) {
							final int[]	bounds = new int[2];
							
							from[0] = UnsafedCharUtils.uncheckedParseName(data,from[0],bounds);
							from[0] = skipBlank(data,from[0]);
							
							if (data[from[0]] == '(') {
								final long	id = FUNCTIONS.seekName(data,bounds[0],bounds[1]+1);
								
								if (id < 0) {
									throw new IllegalArgumentException("Unknown function name ["+new String(data,bounds[0],bounds[1]-bounds[0])+"]!"); 
								}
								else {
									return parseFunction(FUNCTIONS.getCargo(id),data,from,macro);
								}
							}
							else {
								if (UnsafedCharUtils.uncheckedCompare(data,bounds[0],FALSE,0,FALSE.length) && !Character.isJavaIdentifierPart(data[bounds[0]+FALSE.length])) {
									return new ConstantNode(false);
								}
								else if (UnsafedCharUtils.uncheckedCompare(data,bounds[0],TRUE,0,TRUE.length)  && !Character.isJavaIdentifierPart(data[bounds[0]+TRUE.length])) {
									return new ConstantNode(true);
								}
								else {
									for (AssignableExpressionNode item : macro.getDeclarations()) {
										final char[] name = item.getName();
										
										if (UnsafedCharUtils.uncheckedCompare(data,bounds[0],name,0,name.length)) {
											if (data[from[0]] == '[') {
												from[0]++;
												
												final ExpressionNode	indexNode = parseExpression(ORDER_OR,data,from,macro);
												from[0] = skipBlank(data,from[0]);
												
												if (data[from[0]] == ']') {
													from[0]++;
													return new ArrayAccessNode(item,indexNode);
												}
												else {
													throw new IllegalArgumentException("Missing ']'"); 
												}
											}
											else {
												return item;
											}
										}
									}
									throw new IllegalArgumentException("Undeclared name ["+new String(data,bounds[0],bounds[1]-bounds[0]+1)+"]!"); 
								}
							}							
						}
						else {
							throw new IllegalArgumentException("Unwaited symbol ("+data[from[0]]+")!"); 
						}
						
				}
			default : throw new UnsupportedOperationException("Expression order ["+order+"] is not supported yet");
		}
	}

	private static ExpressionNode parseFunction(final FuncDecription desc, final char[] data, final int[] from, final MacroCommand macro) throws SyntaxException, IllegalArgumentException {
		from[0] = skipBlank(data,from[0]+1);
		if (desc.numberOfParameters == 0) {
			if (data[from[0]] == ')') {
				from[0]++;
				switch (desc.operator) {
					case F_UG 	: 
						return new FuncNode(desc.operator,desc.returned,new FuncNode.IntegerCallback() {
								@Override
								public long calculate(final ExpressionNode[] list) {
									return macro.uniqueG;
								}
							}
						);
					case F_UL 	:
						return new FuncNode(desc.operator,desc.returned,new FuncNode.IntegerCallback() {
								@Override
								public long calculate(final ExpressionNode[] list) {
									return ++macro.uniqueL;
								}
							}
						);
					default 	: throw new UnsupportedOperationException("Internal error!");
				}
			}
			else {
				throw new IllegalArgumentException("Wrong number of arguments to this function"); 
			}
		}
		else {
			final FuncNode	node;
			
			switch (desc.operator) {
				case F_EXISTS 		: node = new FuncExistsNode(); break;
				case F_TO_INT 		: node = new FuncToIntNode(); break;
				case F_TO_REAL		: node = new FuncToRealNode(); break;
				case F_TO_STR		: node = new FuncToStringNode(); break;
				case F_TO_BOOL		: node = new FuncToBooleanNode(); break;
				case F_LEN			: node = new FuncLenNode(); break;
				case F_ENVIRONMENT	: node = new FuncEnvironmentNode(); break;
				default 	: throw new UnsupportedOperationException("Internal error!");
			}
			
			int		parmCount = 0;
			
			from[0]--;
			do {from[0] = InternalUtils.skipBlank(data,from[0]+1);
				final ExpressionNode	operand = parseExpression(ORDER_OR,data,from,macro);
				
				node.addOperand(operand);
				from[0] = InternalUtils.skipBlank(data,from[0]);
				parmCount++;
			} while (data[from[0]] == ',');
			
			if (data[from[0]] == ')') {
				from[0]++;
				if (parmCount != desc.numberOfParameters) {
					throw new IllegalArgumentException("Wrong number of arguments to this function"); 
				}
				else {
					return node;
				}
			}
			else {
				throw new IllegalArgumentException("Missing ')'"); 
			}
		}
	}
	
	private static ExpressionNode buildConvertNode(final ExpressionNodeValue awaited, final ExpressionNode operand) {
		switch (awaited) {
			case INTEGER	: return new FuncToIntNode().addOperand(operand);
			case REAL 		: return new FuncToRealNode().addOperand(operand);
			case STRING		: return new FuncToStringNode().addOperand(operand);
			case BOOLEAN	: return new FuncToBooleanNode().addOperand(operand);
			default 	: throw new UnsupportedOperationException("Internal error!");
		}
	}

	private static class FuncDecription {
		final ExpressionNodeOperator	operator;
		final ExpressionNodeValue		returned;
		final int						numberOfParameters;
		
		public FuncDecription(ExpressionNodeOperator operator, int numberOfParameters, final ExpressionNodeValue returned) {
			this.operator = operator;
			this.numberOfParameters = numberOfParameters;
			this.returned = returned;
		}

		@Override
		public String toString() {
			return "FuncDecription [operator=" + operator + ", returned=" + returned + ", numberOfParameters=" + numberOfParameters + "]";
		}
	}

	static int skipCallEntity(final char[] data, int from, final int[] bounds) {
		final int	start = from, end = data.length;
		
		bounds[0] = from;
		
		while (from < end && data[from] != '\r' && data[from] != '\n') {
			if (data[from] == '\"') {
				try{bounds[1] = from = UnsafedCharUtils.uncheckedParseString(data,from+1,'\"',new StringBuilder());
				} catch (IOException e) {
					throw new IllegalArgumentException(e.getLocalizedMessage());
				}
			}
			else if (data[from] == ',' || data[from] == '=') {
				bounds[1] = from - 1;
				break;
			}
			else {
				bounds[1] = from++;
			}
		}
		for (;bounds[1] > start && data[bounds[1]] <= ' '; bounds[1]--);	// Trunc trailing blanks
		return from;
	}
}
