package chav1961.purelib.streams.char2byte.asm;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.CompilerUtils;

class InternalUtils {
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

	static int methodSignature2Stack(final String methodSignature, int[][] result) {
		int		toStore = 0, currentType;
		
		for (int index = 1, maxIndex = methodSignature.length(); index < maxIndex; index++) {
			char	currentChar = methodSignature.charAt(index);
			
			if (currentChar == ')') {
				break;
			}
			else {
				currentType = signatureByLetter(currentChar);
				
				while (currentChar == '[') {
					currentChar = methodSignature.charAt(++index);
				}
				if (currentChar == 'L') {
					while (currentChar != ';') {
						currentChar = methodSignature.charAt(++index);
					}
				}
				if (currentType == CompilerUtils.CLASSTYPE_DOUBLE || currentType == CompilerUtils.CLASSTYPE_LONG) {
					if (toStore < result.length) {
						result[toStore] = new int[] {currentType, 0};
					}
					toStore++;
					if (toStore < result.length) {
						result[toStore] = new int[] {StackAndVarRepoNew.SPECIAL_TYPE_TOP, 0};
					}
					toStore++;
				}
				else if (currentType == CompilerUtils.CLASSTYPE_REFERENCE) {
					if (toStore < result.length) {
						result[toStore] = new int[] {currentType, -1};
					}
					toStore++;
				}
				else {
					if (toStore < result.length) {
						result[toStore] = new int[] {currentType, 0};
					}
					toStore++;
				}
			}
		}
		return toStore >= result.length ? -toStore : toStore;
	}
	
	static int methodSignature2Type(final String methodSignature) {
		int from = 0;
		
		while (methodSignature.charAt(from) != ')') {
			from++;
		}
		return signatureByLetter(methodSignature.charAt(from+1));
	}
	
	static int fieldSignature2Type(final CharSequence fieldSignature) {
		return signatureByLetter(fieldSignature.charAt(0));
	}	

	static int methodSignature2Stack(final Method method, int[][] result) {
		return methodSignature2Stack(CompilerUtils.buildMethodSignature(method), result);
	}

	static int constructorSignature2Stack(final Constructor<?> constructor, int[][] result) {
		return methodSignature2Stack(CompilerUtils.buildConstructorSignature(constructor),result);
	}
	
	static int methodSignature2Type(final Method method) {
		return methodSignature2Type(CompilerUtils.buildMethodSignature(method));
	}
	
	static int constructorSignature2Type(final Constructor<?> constructor) {
		return methodSignature2Type(CompilerUtils.buildConstructorSignature(constructor));
	}
	
	private static int signatureByLetter(final char letter) {
		switch(letter) {
			case '[' : case 'L' :
				return CompilerUtils.CLASSTYPE_REFERENCE;
			case 'B' :
				return CompilerUtils.CLASSTYPE_BYTE;
			case 'C' :
				return CompilerUtils.CLASSTYPE_CHAR;
			case 'D' :
				return CompilerUtils.CLASSTYPE_DOUBLE;
			case 'F' :
				return CompilerUtils.CLASSTYPE_FLOAT;
			case 'I' :
				return CompilerUtils.CLASSTYPE_INT;
			case 'J' :
				return CompilerUtils.CLASSTYPE_LONG;
			case 'S' :
				return CompilerUtils.CLASSTYPE_SHORT;
			case 'V' :
				return CompilerUtils.CLASSTYPE_VOID;
			case 'Z' :
				return CompilerUtils.CLASSTYPE_BOOLEAN;
			default : 
				throw new UnsupportedOperationException(); 
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

}
