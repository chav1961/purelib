package chav1961.purelib.streams.char2byte.asm;

import java.util.HashMap;
import java.util.Map;

class Constants {
	/**
	 * <p>Class file header constants</p>
	 */
	public static final int				MAGIC = 0xCAFEBABE;
	public static final short			MINOR_1_7 = 0x0000;
	public static final short			MAJOR_1_7 = 0x0031;
	public static final short			MINOR_1_8 = 0x0000;
	public static final short			MAJOR_1_8 = 0x0032;
	public static final char[]			OBJECT_NAME = "java/lang/Object".toCharArray();

	
	/**
	 * <p>Access flag class/field/method constants</p>
	 */
	public static final short			ACC_PUBLIC = 0x0001;
	public static final short			ACC_PRIVATE = 0x0002;
	public static final short			ACC_PROTECTED = 0x0004;
	public static final short			ACC_STATIC = 0x0008;
	public static final short			ACC_FINAL = 0x0010;
	public static final short			ACC_SUPER =	0x0020;
	public static final short			ACC_VOLATILE = 0x0040;
	public static final short			ACC_TRANSIENT = 0x0080;
	public static final short			ACC_INTERFACE = 0x0200;
	public static final short			ACC_ABSTRACT = 0x0400;
	public static final short			ACC_SYNTHETIC = 0x1000;
	public static final short			ACC_ANNOTATION = 0x2000;
	public static final short			ACC_ENUM = 0x4000;
	public static final short			ACC_SYNCHRONIZED = 0x0020;
	public static final short			ACC_BRIDGE = 0x0040;
	public static final short			ACC_VARARGS = 0x0080;
	public static final short			ACC_NATIVE = 0x0100;
	public static final short			ACC_STRICT = 0x0800;


	public static final String			ACC_PUBLIC_NAME = "public";
	public static final String			ACC_FINAL_NAME = "final";
	public static final String			ACC_ABSTRACT_NAME = "abstract";
	public static final String			ACC_ANNOTATION_NAME = "anotation";
	public static final String			ACC_SYNTHETIC_NAME = "synthetic";
	public static final String			ACC_ENUM_NAME = "enum";
	public static final String			ACC_PRIVATE_NAME = "private";
	public static final String			ACC_PROTECTED_NAME = "protected";
	public static final String			ACC_STATIC_NAME = "static";
	public static final String			ACC_VOLATILE_NAME = "volatile";
	public static final String			ACC_TRANSIENT_NAME = "transient";
	public static final String			ACC_SYNCHRONIZED_NAME = "synchronized";
	public static final String			ACC_INTERFACE_NAME = "interface";
	public static final String			ACC_SUPER_NAME = "super";
	public static final String			ACC_BRIDGE_NAME = "bridge";
	public static final String			ACC_VARARGS_NAME = "varargs";
	public static final String			ACC_NATIVE_NAME = "native";
	public static final String			ACC_STRICT_NAME = "strictfp";
	
	
	/**
	 * <p>Constant pool types</p>
	 */
	public static final byte			CONSTANT_Class = 7;
	public static final byte			CONSTANT_Fieldref = 9;
	public static final byte			CONSTANT_Methodref = 10;
	public static final byte			CONSTANT_InterfaceMethodref = 11;
	public static final byte			CONSTANT_String = 8;
	public static final byte			CONSTANT_Integer = 3;
	public static final byte			CONSTANT_Float = 4;
	public static final byte			CONSTANT_Long = 5;
	public static final byte			CONSTANT_Double = 6;
	public static final byte			CONSTANT_NameAndType = 12;
	public static final byte			CONSTANT_Utf8 = 1;
	public static final byte			CONSTANT_MethodHandle = 15;
	public static final byte			CONSTANT_MethodType = 16;
	public static final byte			CONSTANT_InvokeDynamic = 18;
	
	/**
	 * <p>Attribute names</p>
	 */
	public static final char[]			ATTRIBUTE_Exceptions = "Exceptions".toCharArray();
	public static final char[]			ATTRIBUTE_Code = "Code".toCharArray();
	public static final char[]			ATTRIBUTE_StackMapTable = "StackMapTable".toCharArray();
	public static final char[]			ATTRIBUTE_LocalVariableTable = "LocalVariableTable".toCharArray();
	public static final char[]			ATTRIBUTE_LineNumberTable = "LineNumberTable".toCharArray();
	public static final char[]			ATTRIBUTE_Signature = "Signature".toCharArray();
	public static final char[]			ATTRIBUTE_ConstantValue = "ConstantValue".toCharArray();
	
	/**
	 * <p>Source names for ACC_ZZZ attributes
	 */
	private static Map<Short,String>		ACC_NAMES = new HashMap<>();
	
	static {
		ACC_NAMES.put(ACC_PUBLIC,ACC_PUBLIC_NAME);
		ACC_NAMES.put(ACC_PRIVATE,ACC_PRIVATE_NAME);
		ACC_NAMES.put(ACC_PROTECTED,ACC_PROTECTED_NAME);
		ACC_NAMES.put(ACC_STATIC,ACC_STATIC_NAME);
		ACC_NAMES.put(ACC_FINAL,ACC_FINAL_NAME);
		ACC_NAMES.put(ACC_SUPER,ACC_SUPER_NAME);
		ACC_NAMES.put(ACC_VOLATILE,ACC_VOLATILE_NAME);
		ACC_NAMES.put(ACC_TRANSIENT,ACC_TRANSIENT_NAME);
		ACC_NAMES.put(ACC_INTERFACE,ACC_INTERFACE_NAME);
		ACC_NAMES.put(ACC_ABSTRACT,ACC_ABSTRACT_NAME);
		ACC_NAMES.put(ACC_SYNTHETIC,ACC_SYNTHETIC_NAME);
		ACC_NAMES.put(ACC_ANNOTATION,ACC_ANNOTATION_NAME);
		ACC_NAMES.put(ACC_ENUM,ACC_ENUM_NAME);
		ACC_NAMES.put(ACC_SYNCHRONIZED,ACC_SYNCHRONIZED_NAME);
		ACC_NAMES.put(ACC_BRIDGE,ACC_BRIDGE_NAME);
		ACC_NAMES.put(ACC_VARARGS,ACC_VARARGS_NAME);
		ACC_NAMES.put(ACC_NATIVE,ACC_NATIVE_NAME);
		ACC_NAMES.put(ACC_STRICT,ACC_STRICT_NAME);
	}
	
	static String getAccNameByOption(final short accName) {
		return ACC_NAMES.get(accName);
	}
}
