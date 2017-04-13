package chav1961.purelib.streams.char2byte.asm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class InternalUtils {
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
		else if (tree.getNameLength(item) < 0) {
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
		else if (tree.getNameLength(retType) <= 0) {
			throw new IllegalArgumentException("Returned type item ["+retType+"] is missing in the names tree");
		}
		else {
			final StringBuilder	sb = new StringBuilder("(");
			
			for (int index = staticMethod ? 0 : 1; index < parameters.length; index++) {
				if (tree.getNameLength(parameters[index]) <= 0) {
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
		else if (item.isPrimitive()) {
			switch (item.getSimpleName()) {
				case "boolean"	: return "Z";
				case "byte"		: return "B";
				case "char"		: return "C";
				case "double"	: return "D";
				case "float"	: return "F";
				case "int"		: return "I";
				case "long"		: return "J";
				case "short"	: return "S";
				case "void"		: return "V";
				default : throw new UnsupportedOperationException("Primitive class ["+item.getSimpleName()+"] is not supported yet");
			}
		}
		else {
			return 'L'+item.getName().replace('.','/')+';';
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
}
