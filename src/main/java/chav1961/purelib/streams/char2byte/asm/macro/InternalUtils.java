package chav1961.purelib.streams.char2byte.asm.macro;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.char2byte.asm.CompilerUtils;
import chav1961.purelib.streams.char2byte.asm.macro.AndNode;
import chav1961.purelib.streams.char2byte.asm.macro.ArithmeticNode;
import chav1961.purelib.streams.char2byte.asm.macro.AssignableExpressionNode;
import chav1961.purelib.streams.char2byte.asm.macro.CatNode;
import chav1961.purelib.streams.char2byte.asm.macro.ComparisonNode;
import chav1961.purelib.streams.char2byte.asm.macro.ConstantNode;
import chav1961.purelib.streams.char2byte.asm.macro.ExpressionNode;
import chav1961.purelib.streams.char2byte.asm.macro.ExpressionNodeOperator;
import chav1961.purelib.streams.char2byte.asm.macro.FuncExistsNode;
import chav1961.purelib.streams.char2byte.asm.macro.FuncNode;
import chav1961.purelib.streams.char2byte.asm.macro.FuncToBooleanNode;
import chav1961.purelib.streams.char2byte.asm.macro.FuncToIntNode;
import chav1961.purelib.streams.char2byte.asm.macro.FuncToRealNode;
import chav1961.purelib.streams.char2byte.asm.macro.FuncToStringNode;
import chav1961.purelib.streams.char2byte.asm.macro.MacroCommand;
import chav1961.purelib.streams.char2byte.asm.macro.NegNode;
import chav1961.purelib.streams.char2byte.asm.macro.NotNode;
import chav1961.purelib.streams.char2byte.asm.macro.OperatorListNode;
import chav1961.purelib.streams.char2byte.asm.macro.OperatorNode;
import chav1961.purelib.streams.char2byte.asm.macro.OrNode;
import chav1961.purelib.streams.char2byte.asm.macro.TernaryOperatorNode;

class InternalUtils {
	private static final SyntaxTreeInterface<FuncDecription>	FUNCTIONS = new AndOrTree<>();
	
	static final char[]				FALSE = "false".toCharArray();
	static final char[]				TRUE = "true".toCharArray();
	
	static final char[]				TYPE_INT = "int".toCharArray();
	static final char[]				TYPE_REAL = "real".toCharArray();
	static final char[]				TYPE_STRING = "str".toCharArray();
	static final char[]				TYPE_BOOLEAN = "bool".toCharArray();

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
	
	static {
		FUNCTIONS.placeName("uniqueL",FUNC_UNIQUEL,new FuncDecription(ExpressionNodeOperator.F_UL,0,ExpressionNodeValue.INTEGER));
		FUNCTIONS.placeName("uniqueG",FUNC_UNIQUEG,new FuncDecription(ExpressionNodeOperator.F_UG,0,ExpressionNodeValue.INTEGER));
		FUNCTIONS.placeName("exists",FUNC_EXISTS,new FuncDecription(ExpressionNodeOperator.F_EXISTS,1,ExpressionNodeValue.BOOLEAN));
		FUNCTIONS.placeName("int",FUNC_INT,new FuncDecription(ExpressionNodeOperator.F_TO_INT,1,ExpressionNodeValue.INTEGER));
		FUNCTIONS.placeName("real",FUNC_REAL,new FuncDecription(ExpressionNodeOperator.F_TO_REAL,1,ExpressionNodeValue.REAL));
		FUNCTIONS.placeName("str",FUNC_STR,new FuncDecription(ExpressionNodeOperator.F_TO_STR,1,ExpressionNodeValue.STRING));
		FUNCTIONS.placeName("bool",FUNC_BOOL,new FuncDecription(ExpressionNodeOperator.F_TO_BOOL,1,ExpressionNodeValue.BOOLEAN));
	}
	

	/**
	 * <p>Build field signature by it's description</p>
	 * @param field field to build signature for
	 * @return signature built
	 */
	static final String buildSignature(final Field field) {
		if (field == null) {
			throw new IllegalArgumentException("Field can't be null");
		}
		else {
			return buildSignature(field.getType());
		}
	}	
	
	/**
	 * <p>Build method signature by it's description</p>
	 * @param method method to build signature for
	 * @return signature built
	 */
	static final String buildSignature(final Method method) {
		if (method == null) {
			throw new IllegalArgumentException("Method can't be null");
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			
			sb.append('(');
			for (Class<?> item : method.getParameterTypes()) {
				sb.append(buildSignature(item));
			}
			return sb.append(')').append(buildSignature(method.getReturnType())).toString();
		}
	}

	/**
	 * <p>Build constructor signature by it's description</p>
	 * @param constructor constructor to build signature for
	 * @return signature built
	 */
	static final String buildSignature(final Constructor<?> constructor) {
		if (constructor == null) {
			throw new IllegalArgumentException("Method can't be null");
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			
			sb.append('(');
			for (Class<?> item : constructor.getParameterTypes()) {
				sb.append(buildSignature(item));
			}
			return sb.append(")V").toString();
		}
	}

	/**
	 * <p>Build field signature by it's description</p>
	 * @param tree name tree containing names
	 * @param item name id in the tree
	 * @return signature
	 */
	static final String buildFieldSignature(final SyntaxTreeInterface<?> tree, final long item) {
		if (tree == null) {
			throw new IllegalArgumentException("Names tree can't be null");
		}
		else if (!tree.contains(item)) {
			throw new IllegalArgumentException("Item ["+item+"] is missing in the names tree");
		}
		else {
			return fieldSignature(tree.getName(item));
		}
	}

	/**
	 * <p>Build method signature by description of it's parameters and returned type</p>
	 * @param tree name tree containing names
	 * @param retType method returned type id
	 * @param parameters method parameter type ids
	 * @return signature
	 */
	static final String buildMethodSignature(final SyntaxTreeInterface<?> tree, final boolean staticMethod, final long retType, final long... parameters) {
		if (tree == null) {
			throw new IllegalArgumentException("Names tree can't be null");
		}
		else if (parameters == null) {
			throw new IllegalArgumentException("Parameters can't be null");
		}
		else if (!tree.contains(retType)) {
			throw new IllegalArgumentException("Returned type item ["+retType+"] is missing in the names tree");
		}
		else {
			final StringBuilder	sb = new StringBuilder("(");
			
			for (int index = staticMethod ? 0 : 1; index < parameters.length; index++) {
				if (!tree.contains(parameters[index])) {
					throw new IllegalArgumentException("Parameter["+index+"] = "+parameters[index]+" is missing in the names tree");
				}
				else {
					sb.append(fieldSignature(tree.getName(parameters[index])));
				}
			}
			return sb.append(')').append(fieldSignature(tree.getName(retType))).toString();
		}
	}
	
	
	private static String buildSignature(final Class<?> item) {
		if (item.isArray()) {
			return '['+buildSignature(item.getComponentType());
		} 
		else {
			switch (CompilerUtils.defineClassType(item)) {
				case CompilerUtils.CLASSTYPE_REFERENCE	: return 'L'+item.getName().replace('.','/')+';';
				case CompilerUtils.CLASSTYPE_BOOLEAN	: return "Z";
				case CompilerUtils.CLASSTYPE_BYTE		: return "B";
				case CompilerUtils.CLASSTYPE_CHAR		: return "C";
				case CompilerUtils.CLASSTYPE_DOUBLE		: return "D";
				case CompilerUtils.CLASSTYPE_FLOAT		: return "F";
				case CompilerUtils.CLASSTYPE_INT		: return "I";
				case CompilerUtils.CLASSTYPE_LONG		: return "J";
				case CompilerUtils.CLASSTYPE_SHORT		: return "S";
				case CompilerUtils.CLASSTYPE_VOID		: return "V";
				default : throw new UnsupportedOperationException("Primitive class ["+item.getSimpleName()+"] is not supported yet");
			}
		}
	}

	private static String fieldSignature(final String name) {
		final int	trunc = name.lastIndexOf("[]"); 
		
		if (trunc >= 0) {
			return "["+fieldSignature(name.substring(0,trunc));
		}
		else {
			switch (name) {
				case "boolean"	: return "Z";
				case "byte"		: return "B";
				case "char"		: return "C";
				case "double"	: return "D";
				case "float"	: return "F";
				case "int"		: return "I";
				case "long"		: return "J";
				case "short"	: return "S";
				case "void"		: return "V";
				default : 
					if (name.startsWith("[")) {
						return name.replace('.','/');
					}
					else {
						return "L"+name.replace('.','/')+";";
					}
			}
		}
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

	static int parseConstant(final char[] data, int from, final boolean treatUnknownAsString, final ExpressionNode[] result) throws CalculationException, SyntaxException {
		switch (data[from = skipBlank(data,from)]) {
			case '\"' :
				final StringBuilder	sb = new StringBuilder();
				
				from = CharUtils.parseString(data,from+1,'\"',sb);
				result[0] = new ConstantNode(sb.toString().toCharArray());
				return from;
			case '-' :
				from = parseConstant(data,skipBlank(data,from+1),treatUnknownAsString,result);
				switch (result[0].getValueType()) {
					case INTEGER	: result[0] = new ConstantNode(-result[0].getLong()); break;
					case REAL		: result[0] = new ConstantNode(-result[0].getDouble()); break;
					default : throw new IllegalArgumentException("Minus sign (-) is not applicable for the given constant type!");
				}
				return from;
			case '+' :
				from = parseConstant(data,skipBlank(data,from+1),treatUnknownAsString,result);
				if (result[0].getValueType() != ExpressionNodeValue.INTEGER && result[0].getValueType() != ExpressionNodeValue.REAL) {
					throw new IllegalArgumentException("Plus sign (+) is not applicable for the given constant type!");
				}
				else {
					return from;
				}
			case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
				final long[]	numbers = new long[2];
				
				from = CharUtils.parseNumber(data,from,numbers,CharUtils.PREF_ANY, true);
				switch ((int)numbers[1]) {
					case CharUtils.PREF_INT		: result[0] = new ConstantNode(numbers[0]); break;
					case CharUtils.PREF_LONG	: result[0] = new ConstantNode(numbers[0]); break;
					case CharUtils.PREF_FLOAT	: result[0] = new ConstantNode(Float.intBitsToFloat((int)numbers[0])); break;
					case CharUtils.PREF_DOUBLE	: result[0] = new ConstantNode(Double.longBitsToDouble(numbers[0])); break;
					default : throw new UnsupportedOperationException("Numeric value type ["+numbers[1]+"] is not supported yet");
				}
				return from;
			case 't' : case 'f' :
				if (CharUtils.compare(data,from,FALSE)) {
					result[0] = new ConstantNode(false);
					return from + FALSE.length - 1;
				}
				else if (CharUtils.compare(data,from,TRUE)) {
					result[0] = new ConstantNode(true);
					return from + TRUE.length - 1;
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
					result[0] = new ConstantNode(Arrays.copyOfRange(data,start,to));
				}
				else {
					throw new IllegalArgumentException("Invalid constant value: neither 'true' nor 'false' was detected");
				}
			default : throw new IllegalArgumentException("Invalid constant value (constant can't be started with ["+data[from]+"])");
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

	static ExpressionNodeValue defineType(final char[] data, final int[] bounds) {
		if (CharUtils.compare(data,bounds[0],InternalUtils.TYPE_INT)) {
			return ExpressionNodeValue.INTEGER;
		}
		else if (CharUtils.compare(data,bounds[0],InternalUtils.TYPE_REAL)) {
			return ExpressionNodeValue.REAL;
		}
		else if (CharUtils.compare(data,bounds[0],InternalUtils.TYPE_STRING)) {
			return ExpressionNodeValue.STRING;
		}
		else if (CharUtils.compare(data,bounds[0],InternalUtils.TYPE_BOOLEAN)) {
			return ExpressionNodeValue.BOOLEAN;
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
						from[0] = CharUtils.parseString(data,from[0]+1,'\"',sb);
						return new ConstantNode(sb.toString().toCharArray());
					case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						final long[]	numbers = new long[2];
						
						from[0] = CharUtils.parseNumber(data,from[0],numbers,CharUtils.PREF_ANY, true);
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
							
							from[0] = CharUtils.parseName(data,from[0],bounds);
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
								if (CharUtils.compare(data,bounds[0],FALSE)) {
									return new ConstantNode(false);
								}
								else if (CharUtils.compare(data,bounds[0],TRUE)) {
									return new ConstantNode(true);
								}
								else {
									for (AssignableExpressionNode item : macro.getDeclarations()) {
										if (CharUtils.compare(data,bounds[0],item.getName())) {
											return item;
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
				case F_EXISTS 	: node = new FuncExistsNode(); break;
				case F_TO_INT 	: node = new FuncToIntNode(); break;
				case F_TO_REAL	: node = new FuncToRealNode(); break;
				case F_TO_STR	: node = new FuncToStringNode(); break;
				case F_TO_BOOL	: node = new FuncToBooleanNode(); break;
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
				bounds[1] = from = CharUtils.parseString(data,from+1,'\"',new StringBuilder());
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
