package chav1961.purelib.basic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.UnsupportedAddressTypeException;

import chav1961.purelib.basic.growablearrays.GrowableArraysTest;
//A set of useful methods for CompilerUtils:
//- buildCall method to build invoke call (static, virtual or interface). Selects and builds appropriative command, builds class signature, name and method signature.
//- buildLocalCall method to build invoke call to own class methods.
//- buildGetter method to build getstatic/getfield command. Selects and builds appropriative command, builds class signature and field name.
//- buildLocalGetter method to build getstatic/getfield command to own class fields. Selects and builds appropriative command, builds class signature and field name.
//- buildSetter method to build putstatic/putfield command. Selects and builds appropriative command, builds class signature and field name.
//- buildLocalSetter method to build putstatic/putfield command to own class fields. Selects and builds appropriative command, builds class signature and field name.
//- buildClassSignature method to build class signature
//- buildClassPath method to build class path with packages
//- buildFieldSignature method to build field signature
//- buildFieldPath method to build field path with class and packages
//- buildMethodSignature method to build method signature
//- buildMethodPath method to build method path
//- buildMethodHeader method to build method header and it's parameter list

public class CompilerUtils {
	public static String buildClassSignature(final Class<?> clazz) throws NullPointerException {
		if (clazz == null) {
			throw new NullPointerException("Class to build signature for can't be null"); 
		}
		else if (clazz.isArray()) {
			return '['+buildClassSignature(clazz.getComponentType());
		}
		else {
			switch (Utils.defineClassType(clazz)) {
				case Utils.CLASSTYPE_REFERENCE	:
					final StringBuilder	sb = new StringBuilder("L");
					
					for (String item : CharUtils.split(clazz.getPackage().getName(),'.')) {
						sb.append(item).append('/');
					}
					return sb.append(clazz.getSimpleName()).append(';').toString();
				case Utils.CLASSTYPE_BYTE		: return "B";
				case Utils.CLASSTYPE_SHORT		: return "S";
				case Utils.CLASSTYPE_CHAR		: return "C";	
				case Utils.CLASSTYPE_INT		: return "I";	
				case Utils.CLASSTYPE_LONG		: return "J";	
				case Utils.CLASSTYPE_FLOAT		: return "F";	
				case Utils.CLASSTYPE_DOUBLE		: return "D";	
				case Utils.CLASSTYPE_BOOLEAN	: return "Z";
				case Utils.CLASSTYPE_VOID		: return "V";
				default : throw new UnsupportedOperationException("Class ["+clazz.getCanonicalName()+"] is not supporet yet");
			}
		}
	}

	public static String buildFieldPath(final Field field) throws NullPointerException {
		if (field == null) {
			throw new NullPointerException("Field to build path for can't be null"); 
		}
		else {
			return buildClassSignature(field.getType());
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
			return null;
		}
	}
	
	public static String buildMethodSignature(final Method method) {
		if (method == null) {
			throw new NullPointerException("Method to build signature for can't be null"); 
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			
			return null;
		}
	}
}
