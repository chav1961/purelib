package chav1961.purelib.cdb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.streams.char2byte.AsmWriter;

/**
 * <p>This class contains a lot of useful methods to use in the wide range of compilers. In conjunction with {@linkplain AsmWriter}, 
 * it allows to build class, field and method signatures, construct methods calls, getting and setting class and instance fields and 
 * so on.</p>  
 * @see Class
 * @see Field
 * @see Method
 * @see Constructor
 * @see AsmWriter
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.5
 */

public class CompilerUtils {
	/**
	 * <p>This class is a reference class</p>
	 */
	public static final int		CLASSTYPE_REFERENCE = 0;
	
	/**
	 * <p>This class is a primitive byte</p>
	 */
	public static final int		CLASSTYPE_BYTE = 1;
	
	/**
	 * <p>This class is a primitive short</p>
	 */
	public static final int		CLASSTYPE_SHORT = 2;
	
	/**
	 * <p>This class is a primitive char</p>
	 */
	public static final int		CLASSTYPE_CHAR = 3;	
	
	/**
	 * <p>This class is a primitive int</p>
	 */
	public static final int		CLASSTYPE_INT = 4;	
	
	/**
	 * <p>This class is a primitive long</p>
	 */
	public static final int		CLASSTYPE_LONG = 5;	
	
	/**
	 * <p>This class is a primitive float</p>
	 */
	public static final int		CLASSTYPE_FLOAT = 6;	
	
	/**
	 * <p>This class is a primitive double</p>
	 */
	public static final int		CLASSTYPE_DOUBLE = 7;	
	
	/**
	 * <p>This class is a primitive boolean</p>
	 */
	public static final int		CLASSTYPE_BOOLEAN = 8;	

	/**
	 * <p>This class is a primitive void</p>
	 */
	public static final int		CLASSTYPE_VOID = 9;	

	
	
	
	/**
	 * <p>Classify the given class by it's primitive type</p>
	 * @param clazz class to classify
	 * @return one of the CLASSTYPE_ZZZ constants (see description) 
	 */
	public static int defineClassType(final Class<?> clazz) {
		if (clazz == null) {
			throw new NullPointerException("Class to define can't be null"); 
		}
		else if (!clazz.isPrimitive()) {
			return CLASSTYPE_REFERENCE;
		}
		else if (clazz == byte.class) {
			return CLASSTYPE_BYTE;
		}
		else if (clazz == short.class) {
			return CLASSTYPE_SHORT;
		}
		else if (clazz == char.class) {
			return CLASSTYPE_CHAR;
		}
		else if (clazz == int.class) {
			return CLASSTYPE_INT;
		}
		else if (clazz == long.class) {
			return CLASSTYPE_LONG;
		}
		else if (clazz == float.class) {
			return CLASSTYPE_FLOAT;
		}
		else if (clazz == double.class) {
			return CLASSTYPE_DOUBLE;
		}
		else if (clazz == boolean.class) {
			return CLASSTYPE_BOOLEAN;
		}
		else {
			return CLASSTYPE_VOID;
		}
	}

	/**
	 * <p>Convert class type returned by {@linkplain #defineClassType(Class) method to it's string representation</p>
	 * @param classType class type to convert
	 * @return string converted or null if the class type is unknown
	 * @since 0.0.4
	 */
	public static String getClassTypeRepresentation(final int classType) {
		switch (classType) {
			case CompilerUtils.CLASSTYPE_REFERENCE	: return "ref";
			case CompilerUtils.CLASSTYPE_BYTE		: return "byte";
			case CompilerUtils.CLASSTYPE_SHORT		: return "short";
			case CompilerUtils.CLASSTYPE_CHAR		: return "char";	
			case CompilerUtils.CLASSTYPE_INT		: return "int";	
			case CompilerUtils.CLASSTYPE_LONG		: return "long";
			case CompilerUtils.CLASSTYPE_FLOAT		: return "float";
			case CompilerUtils.CLASSTYPE_DOUBLE		: return "double";
			case CompilerUtils.CLASSTYPE_BOOLEAN	: return "boolean";
			case CompilerUtils.CLASSTYPE_VOID		: return "void";
			default 								: return null;
		}
	}	
	
	/**
	 * <p>Find field description in the given class</p>
	 * @param clazz class to find field in
	 * @param name field name
	 * @param publicOnly find only public fields
	 * @return field found
	 * @throws NullPointerException if class to find in is null
	 * @throws IllegalArgumentException if field name is null or empty
	 * @throws ContentException if field not found anywhere
	 */
	public static Field findField(final Class<?> clazz, final String name, final boolean publicOnly) throws NullPointerException, IllegalArgumentException, ContentException {
		if (clazz == null) {
			throw new NullPointerException("Class to get field from can't be null"); 
		}
		else if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Field name can't be null or empty"); 
		}
		else if (publicOnly) {
			try{return clazz.getField(name);
			} catch (NoSuchFieldException | SecurityException e) {
				throw new ContentException("Field ["+name+"] is missing in the class ["+clazz.getCanonicalName()+"]");
			}
		}
		else {
			try{return clazz.getDeclaredField(name);
			} catch (NoSuchFieldException | SecurityException e) {
				if (clazz == Object.class) {
					throw new ContentException("Field ["+name+"] is missing in the class ["+clazz.getCanonicalName()+"]");
				}
				else {
					return findField(clazz.getSuperclass(),name,publicOnly);
				}
			}
		}
	}

	/**
	 * <p>Find method description in the given class</p>
	 * @param clazz class to find method in
	 * @param name method name
	 * @param publicOnly find only public methods
	 * @param parameters parameter types of the method
	 * @return method found
	 * @throws NullPointerException if class to find in is null
	 * @throws IllegalArgumentException if method name is null or empty
	 * @throws ContentException if method not found anywhere
	 */
	public static Method findMethod(final Class<?> clazz, final String name, final boolean publicOnly, final Class<?>... parameters) throws NullPointerException, IllegalArgumentException, ContentException {
		if (clazz == null) {
			throw new NullPointerException("Class to get field from can't be null"); 
		}
		else if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Field name can't be null or empty"); 
		}
		else if (publicOnly) {
			try{return clazz.getMethod(name,parameters);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new ContentException("Method ["+name+"] with parameters "+Arrays.toString(parameters)+" is missing in the class ["+clazz.getCanonicalName()+"]");
			}
		}
		else {
			try{return clazz.getDeclaredMethod(name,parameters);
			} catch (NoSuchMethodException | SecurityException e) {
				if (clazz == Object.class) {
					throw new ContentException("Method ["+name+"] with parameters "+Arrays.toString(parameters)+" is missing in the class ["+clazz.getCanonicalName()+"]");
				}
				else {
					return findMethod(clazz.getSuperclass(),name,publicOnly,parameters);
				}
			}
		}
	}

	/**
	 * <p>Find constructor description in the given class</p>
	 * @param clazz class to find method in
	 * @param publicOnly find only public methods
	 * @param parameters parameter types of the method
	 * @return constructor found
	 * @throws NullPointerException if class to find in is null
	 * @throws ContentException if constructor not found anywhere
	 */
	public static Constructor<?> findConstructor(final Class<?> clazz, final boolean publicOnly, final Class<?>... parameters) throws ContentException {
		if (clazz == null) {
			throw new NullPointerException("Class to get field from can't be null"); 
		}
		else if (publicOnly) {
			try{return clazz.getConstructor(parameters);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new ContentException("Constructor with parameters "+Arrays.toString(parameters)+" is missing in the class ["+clazz.getCanonicalName()+"]");
			}
		}
		else {
			try{return clazz.getDeclaredConstructor(parameters);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new ContentException("Constructor with parameters "+Arrays.toString(parameters)+" is missing in the class ["+clazz.getCanonicalName()+"]");
			}
		}
	}

	/**
	 * <p>This interface describes callback for {@linkplain CompilerUtils#walkFields(Class, FieldWalker)} method</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface FieldWalker {
		/**
		 * <p>Process field</p>
		 * @param clazz field owner descriptor
		 * @param field field descriptor
		 */
		void process(final Class<?> clazz, final Field field);
	}
	
	/**
	 * <p>Walk all fields in the class and it's parents</p>
	 * @param clazz class to walk fields in
	 * @param walker callback to process field
	 * @throws NullPointerException any parameters are null
	 * @since 0.0.3
	 */
	public static void walkFields(final Class<?> clazz, final FieldWalker walker) throws NullPointerException {
		if (clazz == null) {
			throw new NullPointerException("Clazz to walk fields can't be null");
		}
		else if (walker == null) {
			throw new NullPointerException("Field walker can't be null");
		}
		else {
			Class<?>	temp = clazz;
			
			while (temp != null) {
				for (Field f : temp.getDeclaredFields()) {
					walker.process(temp,f);
				}
				temp = temp.getSuperclass();
			}
		}
	}

	/**
	 * <p>Filtered walk all fields in the class</p>
	 * @param clazz class or interface to walk fields in
	 * @param walker callback to process fields
	 * @param useWildcards field name and types contains wildcards
	 * @param recursive need walk parents of the class
	 * @param fieldName field name template
	 * @param availableTypes available types of the field (any of the list). Empty list means any type 
	 * @throws NullPointerException on any null parameters
	 * @throws IllegalArgumentException on null or empty fiend name
	 * @since 0.0.5
	 */
	public static void walkFields(final Class<?> clazz, final FieldWalker walker, final boolean useWildcards, final boolean recursive, final String fieldName, final Class<?>... availableTypes) throws NullPointerException, IllegalArgumentException {
		if (clazz == null) {
			throw new NullPointerException("Class to walk in can't be null");
		}
		else if (walker == null) {
			throw new NullPointerException("Field walker callback can't be null");
		}
		else if (fieldName == null || fieldName.isEmpty()) {
			throw new IllegalArgumentException("Field name can't be null or empty");
		}
		else if (availableTypes == null || Utils.checkArrayContent4Nulls(availableTypes) >= 0) {
			throw new NullPointerException("Available types can't be null or contain nulls inside");
		}
		else if (clazz.isInterface()) {
			if (useWildcards) {
				walkInterfaceFields(clazz, walker, recursive, Pattern.compile(fieldName), availableTypes);
			}
			else {
				walkInterfaceFields(clazz, walker, recursive, fieldName, availableTypes);
			}
		}
		else {
			if (useWildcards) {
				walkClassFields(clazz, walker, recursive, Pattern.compile(fieldName), availableTypes);
			}
			else {
				walkClassFields(clazz, walker, recursive, fieldName, availableTypes);
			}
		}
	}	

	/**
	 * <p>This interface describes callback for {@linkplain CompilerUtils#walkMethods(Class, MethodWalker)} method</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface MethodWalker {
		/**
		 * <p>Process method</p>
		 * @param clazz method owner descriptor
		 * @param method method descriptor
		 */
		void process(final Class<?> clazz, final Method method);
	}
	
	/**
	 * <p>Walk all methods in the class</p>
	 * @param clazz class to walk methods in
	 * @param walker callback to process method
	 * @throws NullPointerException any parameters are null
	 * @since 0.0.3
	 */
	public static void walkMethods(final Class<?> clazz, final MethodWalker walker) throws NullPointerException {
		if (clazz == null) {
			throw new NullPointerException("Clazz to walk methods can't be null");
		}
		else if (walker == null) {
			throw new NullPointerException("Method walker can't be null");
		}
		else {
			Class<?>	temp = clazz;
			
			while (temp != null) {
				for (Method m : temp.getDeclaredMethods()) {
					walker.process(temp,m);
				}
				temp = temp.getSuperclass();
			}
		}
	}

	/**
	 * <p>Filtered walk all methods in the class</p>
	 * @param clazz class or interface to walk method in
	 * @param walker callback to process method
	 * @param useWildcards method name and types contain wildcards
	 * @param recursive need walk parent(s) of the class or interface
	 * @param retType returned type of the method. Use {@linkplain AnyType} means any type returned when useWildcards == true
	 * @param methodName method name template
	 * @param parameters method parameters. Use {@linkplain AnyType} inside means exactly one parameters of any type, use {@linkplain AnyTypeList} inside means any number of parameters of any type when useWildcards == true  
	 * @throws NullPointerException on any null parameters
	 * @since 0.0.5
	 */
	public static void walkMethods(final Class<?> clazz, final MethodWalker walker, final boolean useWildcards, final boolean recursive, final Class<?> retType, final String methodName, final Class<?>... parameters) throws NullPointerException {
		if (clazz == null) {
			throw new NullPointerException("Class to walk in can't be null");
		}
		else if (walker == null) {
			throw new NullPointerException("Field walker callback can't be null");
		}
		else if (retType == null) {
			throw new NullPointerException("Returned type can't be null. If returned type doesn't mean, type CompilerUtils.AnyType.class instead");
		}
		else if (methodName == null || methodName.isEmpty()) {
			throw new IllegalArgumentException("Method name can't be null or empty");
		}
		else if (Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new NullPointerException("Available types can't be null or contain nulls inside");
		}
		else if (clazz.isInterface()) {
			if (useWildcards) {
				walkInterfaceMethods(clazz, walker, recursive, Pattern.compile(methodName), retType, parameters);
			}
			else {
				walkInterfaceMethods(clazz, walker, recursive, methodName, retType, parameters);
			}
		}
		else {
			if (useWildcards) {
				walkClassMethods(clazz, walker, recursive, Pattern.compile(methodName), retType, parameters);
			}
			else {
				walkClassMethods(clazz, walker, recursive, methodName, retType, parameters);
			}
		}
	}	
	
	/**
	 * <p>This interface describes callback for {@linkplain CompilerUtils#walkConstructors(Class, ConstructorWalker)} method</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface ConstructorWalker {
		/**
		 * <p>Process constructor</p>
		 * @param clazz constructor owner descriptor
		 * @param constructor constructor descriptor
		 */
		void process(final Class<?> clazz, final Constructor<?> constructor);
	}
	
	/**
	 * <p>Walk all constructors in the class</p>
	 * @param clazz class to walk constructors in
	 * @param walker callback to process constructor
	 * @throws NullPointerException any parameters are null
	 * @since 0.0.3
	 */
	public static void walkConstructors(final Class<?> clazz, final ConstructorWalker walker) {
		if (clazz == null) {
			throw new NullPointerException("Clazz to walk constructors can't be null");
		}
		else if (walker == null) {
			throw new NullPointerException("Constructor walker can't be null");
		}
		else {
			Class<?>	temp = clazz;
			
			while (temp != null) {
				for (Constructor<?> c : temp.getDeclaredConstructors()) {
					walker.process(temp,c);
				}
				temp = temp.getSuperclass();
			}
		}
	}
	
	/**
	 * <p>Build class path in the form &lt;package_path&gt;.&lt;ClassName&gt;</p>
	 * @param clazz class to build path for
	 * @return path built
	 * @throws NullPointerException if class to build path for is null
	 */
	public static String buildClassPath(final Class<?> clazz) throws NullPointerException {
		if (clazz == null) {
			throw new NullPointerException("Class to build path for can't be null"); 
		}
		else if (!clazz.isArray()) {
			return clazz.getName();
		}
		else {
			return buildClassPath(clazz.getComponentType())+"[]";
		}
	}

	/**
	 * <p>Build class signature according to JVM specification</p>
	 * @param clazz class to build signature for
	 * @return signature built. Can't be null
	 * @throws NullPointerException if class to build path for is null
	 */
	public static String buildClassSignature(final Class<?> clazz) throws NullPointerException {
		if (clazz == null) {
			throw new NullPointerException("Class to build signature for can't be null"); 
		}
		else if (clazz.isArray()) {
			return '['+buildClassSignature(clazz.getComponentType());
		}
		else {
			switch (defineClassType(clazz)) {
				case CLASSTYPE_REFERENCE	:
					final StringBuilder	sb = new StringBuilder("L");
					
					for (String item : CharUtils.split(clazz.getPackageName(),'.')) {
						sb.append(item).append('/');
					}
					if (clazz.getName().contains("$")) {
						return sb.append(clazz.getName().substring(clazz.getName().lastIndexOf('.')+1)).append(';').toString();
					}
					else {
						return sb.append(clazz.getSimpleName()).append(';').toString();
					}
				case CLASSTYPE_BYTE		: return "B";
				case CLASSTYPE_SHORT	: return "S";
				case CLASSTYPE_CHAR		: return "C";	
				case CLASSTYPE_INT		: return "I";	
				case CLASSTYPE_LONG		: return "J";	
				case CLASSTYPE_FLOAT	: return "F";	
				case CLASSTYPE_DOUBLE	: return "D";	
				case CLASSTYPE_BOOLEAN	: return "Z";
				case CLASSTYPE_VOID		: return "V";
				default : throw new UnsupportedOperationException("Class type for ["+clazz.getCanonicalName()+"] is not supported yet");
			}
		}
	}

	/**
	 * <p>Build field path in the form &lt;package_path&gt;.&lt;ClassName&gt;.&lt;FieldName&gt;</p>
	 * @param field field to build signature for
	 * @return path built. Can't be null
	 * @throws NullPointerException if field to build path for is null
	 */
	public static String buildFieldPath(final Field field) throws NullPointerException {
		if (field == null) {
			throw new NullPointerException("Field to build path for can't be null"); 
		}
		else {
			return buildClassPath(field.getDeclaringClass())+"."+field.getName();
		}
	}
	
	/**
	 * <p>Build field signature according to JVM specifications</p>
	 * @param field field to build signature for
	 * @return signature built. Can't be null 
	 * @throws NullPointerException if field to build signature for is null
	 */
	public static String buildFieldSignature(final Field field) throws NullPointerException {
		if (field == null) {
			throw new NullPointerException("Field to build signature for can't be null"); 
		}
		else {
			return buildClassSignature(field.getType());
		}
	}

	/**
	 * <p>Build method path in the form &lt;package_path&gt;.&lt;ClassName&gt;.&lt;MethodName&gt;</p>
	 * @param method method to build path for
	 * @return path built. Can't be null
	 * @throws NullPointerException if method to build path for is null
	 */
	public static String buildMethodPath(final Method method) throws NullPointerException {
		if (method == null) {
			throw new NullPointerException("Method to build path for can't be null"); 
		}
		else {
			return buildClassPath(method.getDeclaringClass())+"."+method.getName();
		}
	}

	/**
	 * <p>Build method signature according to JVM specifications</p>
	 * @param method method to build signature for
	 * @return signature built. Can't be null
	 * @throws NullPointerException if method to build signature for is null
	 */
	public static String buildMethodSignature(final Method method) throws NullPointerException {
		if (method == null) {
			throw new NullPointerException("Method to build signature for can't be null"); 
		}
		else {
			final StringBuilder	sb = new StringBuilder("(");
			
			for (Class<?> item : method.getParameterTypes()) {
				sb.append(buildClassSignature(item));
			}
			return sb.append(")").append(buildClassSignature(method.getReturnType())).toString();
		}
	}

	/**
	 * <p>Build constructor path in the form &lt;package_path&gt;.&lt;ClassName&gt;.&lt;init&gt</p>
	 * @param constructor constructor to build path for
	 * @return path built. Can't be null
	 * @throws NullPointerException if constructor to build path for is null
	 */
	public static String buildConstructorPath(final Constructor<?> constructor) throws NullPointerException {
		if (constructor == null) {
			throw new NullPointerException("Constructor to build path for can't be null"); 
		}
		else {
			return buildClassPath(constructor.getDeclaringClass())+"."+constructor.getDeclaringClass().getSimpleName();
		}
	}
	
	/**
	 * <p>Build constructor signature according to JVM specifications</p>
	 * @param constructor constructor to build signature for 
	 * @return signature built. Can't be null
	 * @throws NullPointerException if constructor to build signature for is null
	 */
	public static String buildConstructorSignature(final Constructor<?> constructor) throws NullPointerException {
		if (constructor == null) {
			throw new NullPointerException("Constructor to build signature for can't be null"); 
		}
		else {
			final StringBuilder	sb = new StringBuilder("(");
			
			for (Class<?> item : constructor.getParameterTypes()) {
				sb.append(buildClassSignature(item));
			}
			return sb.append(")V").toString();
		}
	}
	
	/**
	 * <p>Build method header</p>
	 * @param method method to build header for
	 * @return header built. Can't be null
	 * @throws NullPointerException if method to build header for is null
	 */
	public static String buildMethodHeader(final Method method) throws NullPointerException {
		if (method == null) {
			throw new NullPointerException("Method to build signature for can't be null"); 
		}
		else {
			final String[]	names = new String[method.getParameterCount()];
			
			for (int index = 0; index < names.length; index++) {
				names[index] = "arg"+index;
			}
			
			return buildMethodHeader(method,names);
		}
	}

	/**
	 * <p>Build method header</p>
	 * @param method method to build header for
	 * @param parameterNames names of method parameters
	 * @return header built. Can't be null
	 * @throws NullPointerException on any nulls inside parameters
	 * @throws IllegalArgumentException on any differences between method parameters and parameter names
	 */
	public static String buildMethodHeader(final Method method, final String... parameterNames) throws NullPointerException, IllegalArgumentException {
		if (method == null) {
			throw new NullPointerException("Method to build signature for can't be null"); 
		}
		else if (parameterNames == null) {
			throw new NullPointerException("Parameter names can't be null array"); 
		}
		else if (method.getParameterCount() != parameterNames.length) {
			throw new IllegalArgumentException("Parameters count ["+parameterNames.length+"] differ to required ["+method.getParameterCount()+"] for the given method"); 
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			final int			methodFlags = method.getModifiers();
			
			sb.append(method.getName()).append(" .method ").append(buildClassPath(method.getReturnType()));
			if (Modifier.isPublic(methodFlags)) {
				sb.append(" public");
			}
			else if (Modifier.isProtected(methodFlags)) {
				sb.append(" protected");
			}
			else if (Modifier.isPrivate(methodFlags)) {
				sb.append(" private");
			};
			if (Modifier.isStatic(methodFlags)) {
				sb.append(" static");
			}
			if (Modifier.isSynchronized(methodFlags)) {
				sb.append(" synchronized");
			}
			if (Modifier.isStrict(methodFlags)) {
				sb.append(" strictfp");
			}
			if (method.getExceptionTypes() != null && method.getExceptionTypes().length > 0) {
				String	prefix = " throws ";
				for (Class<?> item : method.getExceptionTypes()) {
					sb.append(prefix).append(buildClassPath(item));
					prefix = ", ";
				}
			}
			sb.append("\n");
			
			for (int index = 0; index < method.getParameterCount(); index++) {
				if (parameterNames[index] == null || parameterNames[index].isEmpty()) {
					throw new IllegalArgumentException("Parameter names contins null or empty string at index ["+index+"]"); 
				}
				else {
					sb.append(parameterNames[index]).append(" .parameter ").append(buildClassPath(method.getParameterTypes()[index])).append(" final\n");
				}
			}
			return sb.toString();
		}
	}

	/**
	 * <p>Build method call</p>
	 * @param method method to build call for
	 * @return method call built. Can't be null
	 * @throws NullPointerException when method to build call for is null
	 */
	public static String buildMethodCall(final Method method) throws NullPointerException {
		if (method == null) {
			throw new NullPointerException("Method to build signature for can't be null"); 
		}
		else {
			return buildMethodCall(method,method.getDeclaringClass());
		}
	}

	/**
	 * <p>Build interface method call</p>
	 * @param method method to build call for
	 * @param interfaceClass interface where the method was described
	 * @return method call built. Can't be null
	 * @throws NullPointerException when any parameter is null
	 */
	public static String buildMethodCall(final Method method, final Class<?> interfaceClass) throws NullPointerException {
		if (method == null) {
			throw new NullPointerException("Method to build signature for can't be null"); 
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			final int			methodFlags = method.getModifiers();
			
			if (Modifier.isStatic(methodFlags)) {
				sb.append(" invokestatic ").append(buildMethodPath(method));
			}
			else if (interfaceClass.isInterface()) {
				sb.append(" invokeinterface ").append(buildClassPath(interfaceClass)).append('.').append(method.getName());
			}
			else {
				sb.append(" invokevirtual ").append(buildMethodPath(method));
			}
			return sb.append(buildMethodSignature(method)).append("\n").toString();
		}
	}

	/**
	 * <p>Build constructor call</p>
	 * @param method constructor to build call for
	 * @return constructor call built. Can't be null
	 * @throws NullPointerException when constructor to build call for is null
	 */
	public static String buildConstructorCall(final Constructor<?> method) throws NullPointerException {
		if (method == null) {
			throw new NullPointerException("Method to build signature for can't be null"); 
		}
		else {
			final StringBuilder	sb = new StringBuilder(" invokespecial ");
			
			return sb.append(buildConstructorPath(method)).append(buildConstructorSignature(method)).append("\n").toString();
		}
	}
	
	/**
	 * <p>Build field getter</p>
	 * @param field field to build getter for
	 * @return getter built. Can't be null
	 * @throws NullPointerException when field to build getter for is null
	 */
	public static String buildGetter(final Field field) throws NullPointerException {
		if (field == null) {
			throw new NullPointerException("Method to build signature for can't be null"); 
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			final int			fieldFlags = field.getModifiers();
			
			if (Modifier.isStatic(fieldFlags)) {
				sb.append(" getstatic ");
			}
			else {
				sb.append(" getfield ");
			}
			return sb.append(buildClassPath(field.getDeclaringClass())).append('.').append(field.getName()).append("\n").toString();
		}
	}

	/**
	 * <p>Build field setter</p>
	 * @param field field to build setter for
	 * @return setter built. Can't be null
	 * @throws NullPointerException when field to build setter for is null
	 */
	public static String buildSetter(final Field field) throws NullPointerException {
		if (field == null) {
			throw new NullPointerException("Method to build signature for can't be null"); 
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			final int			fieldFlags = field.getModifiers();
			
			if (Modifier.isStatic(fieldFlags)) {
				sb.append(" putstatic ");
			}
			else {
				sb.append(" putfield ");
			}
			return sb.append(buildClassPath(field.getDeclaringClass())).append('.').append(field.getName()).append("\n").toString();
		}
	}

	/**
	 * <p>Collect all classes used in the given field</p> 
	 * @param types set of classes to collect into
	 * @param f field to analyze
	 * @throws NullPointerException on any parameter is null
	 * @since 0.0.5 
	 */
	public static void collectTypes(final Set<Class<?>> types, final Field f) throws NullPointerException {
		if (types == null) {
			throw new NullPointerException("Set of types can't be null");
		}
		else if (f == null) {
			throw new NullPointerException("Field to collect type from can't be null");
		}
		else {
			types.add(resolveArrayClass(f.getType()));
		}
	}

	/**
	 * <p>Collect all classes used in the given method</p> 
	 * @param types set of classes to collect into
	 * @param m method to analyze
	 * @throws NullPointerException on any parameter is null
	 * @since 0.0.5 
	 */
	public static void collectTypes(final Set<Class<?>> types, final Method m) throws NullPointerException {
		if (types == null) {
			throw new NullPointerException("Set of types can't be null");
		}
		else if (m == null) {
			throw new NullPointerException("Method to collect type from can't be null");
		}
		else {
			types.add(resolveArrayClass(m.getReturnType()));
			for (Class<?> item : m.getParameterTypes()) {
				types.add(resolveArrayClass(item));
			}
		}
	}

	/**
	 * <p>Collect all classes used in the given constructor</p>
	 * @param types set of classes to collect into
	 * @param c constructor to analyze
	 * @throws NullPointerException on any parameter is null
	 * @since 0.0.5 
	 */
	public static void collectTypes(final Set<Class<?>> types, final Constructor<?> c) throws NullPointerException {
		if (types == null) {
			throw new NullPointerException("Set of types can't be null");
		}
		else if (c == null) {
			throw new NullPointerException("Constructor to collect type from can't be null");
		}
		else {
			for (Class<?> item : c.getParameterTypes()) {
				types.add(resolveArrayClass(item));
			}
			for (Class<?> item : c.getExceptionTypes()) {
				types.add(resolveArrayClass(item));
			}
		}
	}

	/**
	 * <p>Collect all classes used in the given class</p>
	 * @param types set of classes to collect into
	 * @param cl class to analyze
	 * @throws NullPointerException on any parameter is null
	 * @since 0.0.5 
	 */
	public static void collectTypes(final Set<Class<?>> types, final Class<?> cl) throws NullPointerException {
		if (types == null) {
			throw new NullPointerException("Set of types can't be null");
		}
		else if (cl == null) {
			throw new NullPointerException("Class to collect type from can't be null");
		}
		else if (cl.isInterface()) {
			gatherClassTypes(types,cl);
			if (cl.getSuperclass() != null) {
				collectTypes(types,cl.getSuperclass());
			}
		}
		else {
			gatherClassTypes(types,cl);
			for (Class<?> item : cl.getInterfaces()) {
				collectTypes(types,item);
			}
		}
	}
	
	/**
	 * <p>Build signature by class name</p>
	 * @param className canonical class name. Can't be null or empty
	 * @return class signature. Can't be null or empty
	 * @throws IllegalArgumentException on any argument errors
	 * @since 0.0.5
	 */
	public static String buildClassNameSignature(final String className) throws IllegalArgumentException {
		if (className == null || className.isEmpty()) {
			throw new IllegalArgumentException("Class name to build signature for can't be null");
		}
		else if (className.contains("[]")) {
			return '['+buildClassNameSignature(className.substring(0,className.lastIndexOf("[]")));
		}
		else {
			switch (className) {
				case "byte" 	: return "B";
				case "short" 	: return "S";
				case "int" 		: return "I";
				case "long" 	: return "J";
				case "float" 	: return "F";
				case "double" 	: return "D";
				case "char" 	: return "C";
				case "boolean" 	: return "Z";
				case "void" 	: return "V";
				default : return "L"+className.replace('.', '/')+';';
			}
		}
	}

	/**
	 * <p>Build parameter signature</p>
	 * @param parameters parameter of the method. Can't be null and can't contain nulls inside
	 * @param retType returned type of the method. Can't be null
	 * @return string built. Can't be null
	 * @throws IllegalArgumentException om any argument exception
	 * @since 0.0.5
	 */
	public static String buildParametersSignature(final String[] parameters, final String retType) throws IllegalArgumentException {
		if (parameters == null || Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Parameter list to build signature for is null or contains nulls inside");
		}
		else if (retType == null || retType.isEmpty()) {
			throw new IllegalArgumentException("Returned type to build signature for can't be null");
		}
		else if (parameters.length == 0) {
			return "()"+buildClassNameSignature(retType);
		}
		else {
			final StringBuilder	sb = new StringBuilder('(');
			
			for (String item : parameters) {
				sb.append(buildClassNameSignature(item));
			}
			return sb.append(')').append(buildClassNameSignature(retType)).toString();
		}
	}

	/**
	 * <p>Convert collection content to string for using in macro parameters</p>
	 * @param <T> content type
	 * @param iterable iterable on content. Can't be null
	 * @param convertor converter lambda. Can't be null
	 * @return content content built. Can't be null or empty;
	 * @throws NullPointerException any argument is null
	 * @since 0.0.5
	 */
	public static <T> String content2String(final Iterable<T> iterable, final Function<T,String> convertor) throws NullPointerException {
		return content2String(iterable, convertor, true);
	}
	
	/**
	 * <p>Convert collection content to string for using in macro parameters</p>
	 * @param <T> content type
	 * @param iterable iterable on content. Can't be null
	 * @param convertor converter lambda. Can't be null
	 * @param useQuotes place converted value into quotes
	 * @return content content built. Can't be null or empty;
	 * @throws NullPointerException any argument is null
	 * @since 0.0.5
	 */
	public static <T> String content2String(final Iterable<T> iterable, final Function<T,String> convertor, final boolean useQuotes) throws NullPointerException {
		if (iterable == null) {
			throw new NullPointerException("Iterable instance can't be null"); 
		}
		else if (convertor == null) {
			throw new NullPointerException("Convertor can't be null"); 
		}
		else {
			final StringBuilder sb = new StringBuilder();
			char				prefix = '{';
			
			if (useQuotes) {
				for (T item : iterable) {
					sb.append(prefix).append('\"').append(convertor.apply(item)).append('\"');
					prefix = ',';
				}
			}
			else {
				for (T item : iterable) {
					sb.append(prefix).append(convertor.apply(item));
					prefix = ',';
				}
			}
			if (prefix == '{') {
				return "{}";
			}
			else {
				return sb.append('}').toString();
			}
		}
	}
	
	private static void gatherClassTypes(final Set<Class<?>> types, final Class<?> cl) {
		for (Field f : cl.getDeclaredFields()) {
			if (Modifier.isPublic(f.getModifiers()) || Modifier.isProtected(f.getModifiers())) {
				collectTypes(types,f);
			}
		}
		for (Method m : cl.getDeclaredMethods()) {
			if (Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers())) {
				collectTypes(types,m);
			}
		}
		for (Constructor<?> c : cl.getDeclaredConstructors()) {
			if (Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers())) {
				collectTypes(types,c);
			}
		}
	}

	private static Class<?> resolveArrayClass(Class<?> source) {
		while (source.isArray()) {
			source = source.getComponentType();
		}
		return source;
	}
	
	/**
	 * <p>Convert file name to class name</p>
	 * @param fileName file name to convert to class name
	 * @return class name. Can't be null
	 * @throws IllegalArgumentException when file name is null or empty
	 */
	public static String fileName2Class(final String fileName) throws IllegalArgumentException {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty");
		}
		else {
			final char[]	content = fileName.toCharArray();
			int				firstPoint = content[0] == '/' ? 1 : 0, lastPoint = content.length-1;
			
			while (lastPoint > 0 && content[lastPoint] != '.') {
				lastPoint--;
			}
			for (int index = firstPoint; index < lastPoint; index++) {
				if (content[index] == '/') {
					content[index] = '.';
				}
			}
			return new String(content,firstPoint,lastPoint-firstPoint);
		}
	}

	private static void walkInterfaceFields(final Class<?> clazz, final FieldWalker walker, final boolean recursive, final Pattern pattern, final Class<?>[] availableTypes) {
		for (Field item : clazz.getDeclaredFields()) {
			if (pattern.matcher(item.getName()).matches() && fieldTypeInList(item.getType(),availableTypes)) {
				walker.process(clazz,item);
			}
		}
		if (recursive) {
			for (Class <?>item : clazz.getInterfaces()) {
				walkInterfaceFields(item, walker, recursive, pattern, availableTypes);
			}
		}
	}

	private static void walkInterfaceFields(final Class<?> clazz, final FieldWalker walker, final boolean recursive, final String fieldName, final Class<?>[] availableTypes) {
		for (Field item : clazz.getDeclaredFields()) {
			if (item.getName().equals(fieldName) && fieldTypeInList(item.getType(),availableTypes)) {
				walker.process(clazz,item);
			}
		}
		if (recursive) {
			for (Class <?>item : clazz.getInterfaces()) {
				walkInterfaceFields(item, walker, recursive, fieldName, availableTypes);
			}
		}
	}
	
	private static void walkClassFields(final Class<?> clazz, final FieldWalker walker, final boolean recursive, final Pattern pattern, final Class<?>[] availableTypes) {
		for (Field item : clazz.getDeclaredFields()) {
			if (pattern.matcher(item.getName()).matches() && fieldTypeInList(item.getType(),availableTypes)) {
				walker.process(clazz,item);
			}
		}
		if (recursive && clazz.getSuperclass() != null) {
			walkClassFields(clazz.getSuperclass(), walker, recursive, pattern, availableTypes);
		}
	}

	private static void walkClassFields(final Class<?> clazz, final FieldWalker walker, final boolean recursive, final String fieldName, final Class<?>[] availableTypes) {
		for (Field item : clazz.getDeclaredFields()) {
			if (item.getName().equals(fieldName) && fieldTypeInList(item.getType(),availableTypes)) {
				walker.process(clazz,item);
			}
		}
		if (recursive && clazz.getSuperclass() != null) {
			walkClassFields(clazz.getSuperclass(), walker, recursive, fieldName, availableTypes);
		}
	}

	private static boolean fieldTypeInList(final Class<?> type, final Class<?>[] availableTypes) {
		if (availableTypes.length == 0) {
			return true;
		}
		else {
			for (Class<?> item : availableTypes) {
				if (type.equals(item) || item.equals(AnyType.class)) {
					return true;
				}
			}
			return false;
		}
	}

	private static void walkInterfaceMethods(final Class<?> clazz, final MethodWalker walker, final boolean recursive, final Pattern pattern, final Class<?> retType, final Class<?>[] parameters) {
		for (Method item : clazz.getDeclaredMethods()) {
			if (pattern.matcher(item.getName()).matches() && methodTypesMatch(item.getParameterTypes(), 0, parameters, 0) && (item.getReturnType().equals(retType) || retType.equals(AnyType.class))) {
				walker.process(clazz, item);
			}
		}
		if (recursive) {
			for (Class<?> item : clazz.getInterfaces()) {
				walkInterfaceMethods(item, walker, recursive, pattern, retType, parameters);
			}
		}
	}

	private static void walkInterfaceMethods(final Class<?> clazz, final MethodWalker walker, final boolean recursive, final String methodName, final Class<?> retType, final Class<?>[] parameters) {
		for (Method item : clazz.getDeclaredMethods()) {
			if (item.getName().equals(methodName) && (item.getReturnType().equals(retType) || retType.equals(AnyType.class)) && Arrays.deepEquals(item.getParameterTypes(), parameters)) {
				walker.process(clazz, item);
			}
		}
		if (recursive) {
			for (Class<?> item : clazz.getInterfaces()) {
				walkInterfaceMethods(item, walker, recursive, methodName, retType, parameters);
			}
		}
	}

	private static void walkClassMethods(final Class<?> clazz, final MethodWalker walker, final boolean recursive, final Pattern pattern, final Class<?> retType, final Class<?>[] parameters) {
		for (Method item : clazz.getDeclaredMethods()) {
			if (pattern.matcher(item.getName()).matches() && methodTypesMatch(item.getParameterTypes(), 0, parameters, 0) && (item.getReturnType().equals(retType) || retType.equals(AnyType.class))) {
				walker.process(clazz, item);
			}
		}
		if (recursive && clazz.getSuperclass() != null) {
			walkClassMethods(clazz.getSuperclass(), walker, recursive, pattern, retType, parameters);
		}
	}

	private static void walkClassMethods(final Class<?> clazz, final MethodWalker walker, final boolean recursive, final String methodName, final Class<?> retType, final Class<?>[] parameters) {
		for (Method item : clazz.getDeclaredMethods()) {
			if (item.getName().equals(methodName) && (item.getReturnType().equals(retType) || retType.equals(AnyType.class)) && Arrays.deepEquals(item.getParameterTypes(), parameters)) {
				walker.process(clazz, item);
			}
		}
		if (recursive && clazz.getSuperclass() != null) {
			walkClassMethods(clazz.getSuperclass(), walker, recursive, methodName, retType, parameters);
		}
	}

	private static boolean methodTypesMatch(final Class<?>[] left, final int fromLeft, final Class<?>[] right, final int fromRight) {
		if (fromLeft < left.length && fromRight < right.length) {
			if (left[fromLeft].equals(right[fromRight]) || right[fromRight].equals(AnyType.class)) {
				return methodTypesMatch(left, fromLeft + 1, right, fromRight + 1);
			}
			else if (right[fromRight].equals(AnyTypeList.class)) {
				for (int index = fromLeft + 1; index <= left.length; index++) {	// EXACTLY <=, NOT < !!!
					if (methodTypesMatch(left, index, right, fromRight + 1)) {
						return true;
					}
				}
				return false;
			}
			else {
				return false;
			}
		}
		else if (fromLeft >= left.length && fromRight >= right.length) {
			return true;
		}
		else if (fromLeft >= left.length) {
			return right[fromRight].equals(AnyTypeList.class);
		}
		else {
			return false;
		}
	}

	/**
	 * <p>This interface is a marker for any type of parameter</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.5
	 */
	public interface AnyType {}
	
	/**
	 * <p>This interface is a marker for a list of any type of parameter</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.5
	 */
	public interface AnyTypeList {}
}
