package chav1961.purelib.basic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import chav1961.purelib.streams.char2byte.AsmWriter;

/**
 * <p>This class is a wrapper to class loader for the current thread. It can create new class by it's definition (as byte array with class file content,
 * so reader with the bytecode assembler inside {@linkplain AsmWriter}.)</p>
 * <p>The class is not thread-safe</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public class ClassLoaderWrapper extends ClassLoader {
	private final ClassLoader	parent;
	private final MethodHandle	mhDefine;
	private final MethodHandle	mhDefineDeprecated;
	private final MethodHandle	mhResolve;
	/**
	 * <p>Create class loader instance</p>
	 */
	public ClassLoaderWrapper() {
		this(Thread.currentThread().getContextClassLoader());
	}

	/**
	 * <p>Create class loader instance</p>
	 * @param parent parent class loader to use
	 */
	public ClassLoaderWrapper(final ClassLoader parent) {
		super(parent);
		this.parent = parent;
		try{mhDefine = buildHandle(parent.getClass(),"defineClass", String.class, byte[].class, int.class, int.class);
			mhDefineDeprecated = buildHandle(parent.getClass(),"defineClass", byte[].class, int.class, int.class);
			mhResolve = buildHandle(parent.getClass(),"resolveClass", Class.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}		
	}
	
	MethodHandle buildHandle(final Class<?> node, final String name, final Class<?>... parameters) throws NoSuchMethodException {
		if (node == null) {
			throw new NoSuchMethodException(name);
		}
		else {
			try{final Method	m = node.getDeclaredMethod(name,parameters);
			
				m.setAccessible(true);
				return MethodHandles.lookup().unreflect(m);
			} catch (NoSuchMethodException | IllegalAccessException exc) {
				return buildHandle(node.getSuperclass(),name,parameters);
			}
		}
	}
	
	/**
	 * <p>Create class by it's name and content</p>
	 * @param className class name to create
	 * @param content class file content to create class from
	 * @return class created
	 * @throws IllegalArgumentException class name is null or empty or content is null or too short
	 */

	public Class<?> createClass(final String className, final byte[] content) throws IllegalArgumentException {
		if (className == null || className.isEmpty()) {
			throw new IllegalArgumentException("Class name can't be null or empty");
		}
		else if (content == null || content.length < className.length()) {
			throw new IllegalArgumentException("Reader can't be null");
		}
		else {
			try{final Class<?> 	cl = (Class<?>) mhDefine.invokeExact(parent,className,content,0,content.length);
			
				mhResolve.invokeExact(parent,cl);			
				return cl;
			} catch (Throwable e) {
				throw new IllegalArgumentException(e);
			}
			
		}
	}

	@Deprecated
	public Class<?> createClass(final byte[] content) throws IllegalArgumentException {
		if (content == null || content.length== 0) {
			throw new IllegalArgumentException("COntent can't be null");
		}
		else {
			try{final Class<?> 	cl = (Class<?>) mhDefineDeprecated.invokeExact(parent,content,0,content.length);
			
				mhResolve.invokeExact(parent,cl);			
				return cl;
			} catch (Throwable e) {
				throw new IllegalArgumentException(e);
			}
			
		}
	}
	
	/**
	 * <p>Create class by it's name and bytecode assembler content</p>
	 * @param className class name to create
	 * @param content reader containing bytecode assembler
	 * @return class created
	 * @throws NullPointerException reader is null
	 * @throws IllegalArgumentException class name is null or empty
	 * @throws IOException when any problems during class loading and definition were detected
	 */
	public Class<?> createClass(final String className, final Reader content) throws NullPointerException, IllegalArgumentException, IOException {
		if (className == null || className.isEmpty()) {
			throw new IllegalArgumentException("Class name can't be null or empty");
		}
		else if (content == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else {
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				try(final Writer			wr = new AsmWriter(baos)) {
					
					Utils.copyStream(content,wr);
					wr.flush();
				}
				return createClass(className,baos.toByteArray());
			}
		}
	}
	
	/**
	 * <p>Print class content in human-readable form</p> 
	 * @param clazz class to print
	 * @return class content in the human-readable form. Can't be neither null nor empty
	 * @throws NullPointerException when class to print is null
	 */
	public static String toString(final Class<?> clazz) throws NullPointerException {
		if (clazz == null) {
			throw new NullPointerException("Class to print can't be null");
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			
			for (Annotation anno : clazz.getAnnotations()) {
				sb.append("@").append(anno.getClass().getSimpleName()).append('\n');
			}
			putModifiers(clazz.getModifiers(),sb);
			
			sb.append(Modifier.isInterface(clazz.getModifiers()) ? "interface " : "class ").append(clazz.getCanonicalName()).append(' ');
			
			if (clazz.getSuperclass() != Object.class) {
				sb.append("extends ").append(clazz.getSuperclass().getCanonicalName());
			}
			String	prefix = "implements ";
			for (Class<?> item : clazz.getInterfaces()) {
				sb.append(prefix).append(item.getCanonicalName());
				prefix = ", ";
			}
			sb.append("{\n// Fields:\n");
			
			for (Field item : clazz.getDeclaredFields()) {
				for (Annotation anno : item.getAnnotations()) {
					sb.append("\t@").append(anno.getClass().getSimpleName()).append('\n');
				}
				sb.append("\t");
				putModifiers(item.getModifiers(),sb);
				sb.append(item.getType().getCanonicalName()).append(' ').append(item.getName()).append(";\n");
			}

			sb.append("\n// Constructors\n");
			
			for (Constructor<?> item : clazz.getDeclaredConstructors()) {
				for (Annotation anno : item.getAnnotations()) {
					sb.append("\t@").append(anno.getClass().getSimpleName()).append('\n');
				}
				sb.append("\t");
				putModifiers(item.getModifiers(),sb);
				sb.append(item.getName());
				
				prefix = "(";
				for (Parameter entity : item.getParameters()) {
					sb.append(prefix);
					for (Annotation anno : entity.getAnnotations()) {
						sb.append("@").append(anno.getClass().getSimpleName()).append(' ');
					}
					putModifiers(entity.getModifiers(),sb);
					sb.append(entity.getType().getCanonicalName()).append(' ').append(entity.getName());
					prefix = ",";
				}
				sb.append("(".equals(prefix) ? "()" : ")").append(";\n");
			}
			
			sb.append("\n// Methods\n");
			
			for (Method item : clazz.getDeclaredMethods()) {
				for (Annotation anno : item.getAnnotations()) {
					sb.append("\t@").append(anno.getClass().getSimpleName()).append('\n');
				}
				sb.append("\t");
				putModifiers(item.getModifiers(),sb);
				sb.append(item.getReturnType().getCanonicalName()).append(' ');
				sb.append(item.getName());
				
				prefix = "(";
				for (Parameter entity : item.getParameters()) {
					sb.append(prefix);
					for (Annotation anno : entity.getAnnotations()) {
						sb.append("@").append(anno.getClass().getSimpleName()).append(' ');
					}
					putModifiers(entity.getModifiers(),sb);
					sb.append(entity.getType().getCanonicalName()).append(' ').append(entity.getName());
					prefix = ",";
				}
				sb.append("(".equals(prefix) ? "()" : ")").append(";\n");
			}

			return sb.append("}\n").toString();
		}
	}
	
	private static void putModifiers(final int modifiers, final StringBuilder sb) {
		if (Modifier.isPublic(modifiers)) {
			sb.append("public  ");
		}
		else if (Modifier.isProtected(modifiers)) {
			sb.append("protected  ");
		} 
		else if (Modifier.isPrivate(modifiers)) {
			sb.append("private  ");
		}
		if (Modifier.isAbstract(modifiers)) {
			sb.append("abstract ");
		}
		if (Modifier.isStatic(modifiers)) {
			sb.append("static  ");
		}
		if (Modifier.isFinal(modifiers)) {
			sb.append("final  ");
		}
		if (Modifier.isSynchronized(modifiers)) {
			sb.append("synchronized  ");
		}
		if (Modifier.isVolatile(modifiers)) {
			sb.append("volatile  ");
		}
		if (Modifier.isTransient(modifiers)) {
			sb.append("trancient  ");
		}
		if (Modifier.isNative(modifiers)) {
			sb.append("native  ");
		}
		if (Modifier.isStrict(modifiers)) {
			sb.append("strictfp  ");
		}
	}
}
