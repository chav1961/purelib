package chav1961.purelib.streams.char2byte.asm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import com.sun.glass.ui.GestureSupport;

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
	 * <p>This interface describes callback for {@linkplain CompilerUtils#walkMethods(Class, MethodWalker)} method</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface MethodWalker {
		/**
		 * <p>Process method</p>
		 * @param clazz method owner descriptor
		 * @param field method descriptor
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
	 * <p>This interface describes callback for {@linkplain CompilerUtils#walkConstructors(Class, ConstructorWalker)} method</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface ConstructorWalker {
		/**
		 * <p>Process constructor</p>
		 * @param clazz constructor owner descriptor
		 * @param field constructor descriptor
		 */
		void process(final Class<?> clazz, final Constructor<?> method);
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
	 * <p>Build class path in the form &lt;package_path&gt;.&lt;ClassName&gt;
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
	 * @return signature built
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
					
					for (String item : CharUtils.split(clazz.getPackage().getName(),'.')) {
						sb.append(item).append('/');
					}
					return sb.append(clazz.getSimpleName()).append(';').toString();
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

	public static String buildFieldPath(final Field field) throws NullPointerException {
		if (field == null) {
			throw new NullPointerException("Field to build path for can't be null"); 
		}
		else {
			return buildClassPath(field.getDeclaringClass())+"."+field.getName();
		}
	}
	
	public static String buildFieldSignature(final Field field) throws NullPointerException {
		if (field == null) {
			throw new NullPointerException("Field to build signature for can't be null"); 
		}
		else {
			return buildClassSignature(field.getType());
		}
	}

	public static String buildMethodPath(final Method method) {
		if (method == null) {
			throw new NullPointerException("Method to build path for can't be null"); 
		}
		else {
			return buildClassPath(method.getDeclaringClass())+"."+method.getName();
		}
	}
	
	public static String buildMethodSignature(final Method method) {
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

	public static String buildConstructorPath(final Constructor<?> constructor) {
		if (constructor == null) {
			throw new NullPointerException("Constructor to build path for can't be null"); 
		}
		else {
			return buildClassPath(constructor.getDeclaringClass())+"."+constructor.getDeclaringClass().getSimpleName();
		}
	}
	
	public static String buildConstructorSignature(final Constructor<?> constructor) {
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
	
	public static String buildMethodHeader(final Method method) {
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

	public static String buildMethodHeader(final Method method, final String... parameterNames) {
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

	public static String buildMethodCall(final Method method) {
		if (method == null) {
			throw new NullPointerException("Method to build signature for can't be null"); 
		}
		else {
			return buildMethodCall(method,method.getDeclaringClass());
		}
	}

	public static String buildMethodCall(final Method method, final Class<?> interfaceClass) {
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

	public static String buildConstructorCall(final Constructor<?> method) {
		if (method == null) {
			throw new NullPointerException("Method to build signature for can't be null"); 
		}
		else {
			final StringBuilder	sb = new StringBuilder(" invokespecial ");
			
			return sb.append(buildConstructorPath(method)).append(buildConstructorSignature(method)).append("\n").toString();
		}
	}
	
	public static String buildGetter(final Field field) {
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

	public static String buildSetter(final Field field) {
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
	
	public static String fileName2Class(final String fileName) {
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

//	public static String class2FileName(final String clazz) {
//		
//	}
}
