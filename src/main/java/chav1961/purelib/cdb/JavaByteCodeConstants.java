package chav1961.purelib.cdb;

/**
 * <p>This class keeps a set of constants used in the Java Byte Code</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */

public class JavaByteCodeConstants {
	/**
	 * <p>Class file header constants</p>
	 */
	public static final int		MAGIC = 0xCAFEBABE;
	public static final short	MAJOR_21 = 0x0041;
	public static final short	MINOR_21 = 0x0000;
	public static final short	MAJOR_20 = 0x0040;
	public static final short	MINOR_20 = 0x0000;
	public static final short	MAJOR_19 = 0x003F;
	public static final short	MINOR_19 = 0x0000;
	public static final short	MAJOR_18 = 0x003E;
	public static final short	MINOR_18 = 0x0000;
	public static final short	MAJOR_17 = 0x003D;
	public static final short	MINOR_17 = 0x0000;
	public static final short	MAJOR_16 = 0x003C;
	public static final short	MINOR_16 = 0x0000;
	public static final short	MAJOR_15 = 0x003B;
	public static final short	MINOR_15 = 0x0000;
	public static final short	MAJOR_14 = 0x003A;
	public static final short	MINOR_14 = 0x0000;
	public static final short	MAJOR_13 = 0x0039;
	public static final short	MINOR_13 = 0x0000;
	public static final short	MAJOR_12 = 0x0038;
	public static final short	MINOR_12 = 0x0000;
	public static final short	MAJOR_11 = 0x0037;
	public static final short	MINOR_11 = 0x0000;
	public static final short	MAJOR_10 = 0x0036;
	public static final short	MINOR_10 = 0x0000;
	public static final short	MAJOR_9 = 0x0035;
	public static final short	MINOR_9 = 0x0000;
	public static final short	MAJOR_8 = 0x0034;
	public static final short	MINOR_8 = 0x0000;
	public static final short	MAJOR_1_7 = 0x0033;
	public static final short	MINOR_1_7 = 0x0000;
	public static final short	MAJOR_1_6 = 0x0032;
	public static final short	MINOR_1_6 = 0x0000;
	public static final short	MAJOR_1_5 = 0x0031;
	public static final short	MINOR_1_5 = 0x0000;
	public static final short	MAJOR_1_4 = 0x0030;
	public static final short	MINOR_1_4 = 0x0000;
	public static final short	MAJOR_1_3 = 0x002F;
	public static final short	MINOR_1_3 = 0x0000;
	public static final short	MAJOR_1_2 = 0x002E;
	public static final short	MINOR_1_2 = 0x0000;
	public static final short	MAJOR_1_0_2 = 0x002D;
	public static final short	MINOR_1_0_2 = 0x0003;
	public static final short	MAJOR_1_1 = MAJOR_1_0_2;
	public static final short	MINOR_1_1 = MINOR_1_0_2;
	public static final char[]	OBJECT_NAME = "java/lang/Object".toCharArray();

	
	/**
	 * <p>Access flag class/field/method constants</p>
	 */
	public static final short	ACC_PUBLIC = 0x0001;
	public static final short	ACC_PRIVATE = 0x0002;
	public static final short	ACC_PROTECTED = 0x0004;
	public static final short	ACC_STATIC = 0x0008;
	public static final short	ACC_FINAL = 0x0010;
	public static final short	ACC_SUPER =	0x0020;
	public static final short	ACC_VOLATILE = 0x0040;
	public static final short	ACC_TRANSIENT = 0x0080;
	public static final short	ACC_NATIVE = 0x0100;
	public static final short	ACC_INTERFACE = 0x0200;
	public static final short	ACC_ABSTRACT = 0x0400;
	public static final short	ACC_STRICT = 0x0800;
	public static final short	ACC_SYNTHETIC = 0x1000;
	public static final short	ACC_ANNOTATION = 0x2000;
	public static final short	ACC_ENUM = 0x4000;
	public static final short	ACC_MODULE = (short)0x8000;
	public static final short	ACC_SYNCHRONIZED = 0x0020;
	public static final short	ACC_BRIDGE = 0x0040;
	public static final short	ACC_VARARGS = 0x0080;


	public static final String	ACC_PUBLIC_NAME = "public";
	public static final String	ACC_FINAL_NAME = "final";
	public static final String	ACC_ABSTRACT_NAME = "abstract";
	public static final String	ACC_ANNOTATION_NAME = "anotation";
	public static final String	ACC_SYNTHETIC_NAME = "synthetic";
	public static final String	ACC_ENUM_NAME = "enum";
	public static final String	ACC_PRIVATE_NAME = "private";
	public static final String	ACC_PROTECTED_NAME = "protected";
	public static final String	ACC_STATIC_NAME = "static";
	public static final String	ACC_VOLATILE_NAME = "volatile";
	public static final String	ACC_TRANSIENT_NAME = "transient";
	public static final String	ACC_SYNCHRONIZED_NAME = "synchronized";
	public static final String	ACC_INTERFACE_NAME = "interface";
	public static final String	ACC_SUPER_NAME = "super";
	public static final String	ACC_BRIDGE_NAME = "bridge";
	public static final String	ACC_VARARGS_NAME = "varargs";
	public static final String	ACC_NATIVE_NAME = "native";
	public static final String	ACC_STRICT_NAME = "strictfp";
	
	
	/**
	 * <p>Constant pool types</p>
	 */
	public static final byte	CONSTANT_Class = 7;
	public static final byte	CONSTANT_Fieldref = 9;
	public static final byte	CONSTANT_Methodref = 10;
	public static final byte	CONSTANT_InterfaceMethodref = 11;
	public static final byte	CONSTANT_String = 8;
	public static final byte	CONSTANT_Integer = 3;
	public static final byte	CONSTANT_Float = 4;
	public static final byte	CONSTANT_Long = 5;
	public static final byte	CONSTANT_Double = 6;
	public static final byte	CONSTANT_NameAndType = 12;
	public static final byte	CONSTANT_Utf8 = 1;
	public static final byte	CONSTANT_MethodHandle = 15;
	public static final byte	CONSTANT_MethodType = 16;
	public static final byte	CONSTANT_Dynamic = 17;
	public static final byte	CONSTANT_InvokeDynamic = 18;
	public static final byte	CONSTANT_Module = 19;
	public static final byte	CONSTANT_Package = 20;
	
	/**
	 * <p>Attribute names</p>
	 */

	/**
	 * <p>This enumeration contains officially supported attribute names inside Java Byte Code</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static enum JavaAttributeType {
		ConstantValue(new JavaClassVersion(MAJOR_1_0_2, MINOR_1_0_2)),
		Code(new JavaClassVersion(MAJOR_1_0_2, MINOR_1_0_2)),
		StackMapTable(new JavaClassVersion(MAJOR_1_6, MINOR_1_6)),
		Exceptions(new JavaClassVersion(MAJOR_1_0_2, MINOR_1_0_2)),
		InnerClasses(new JavaClassVersion(MAJOR_1_0_2, MINOR_1_0_2)),
		EnclosingMethod(new JavaClassVersion(MAJOR_1_5, MINOR_1_5)),
		Synthetic(new JavaClassVersion(MAJOR_1_0_2, MINOR_1_0_2)),
		Signature(new JavaClassVersion(MAJOR_1_5, MINOR_1_5)),
		SourceFile(new JavaClassVersion(MAJOR_1_0_2, MINOR_1_0_2)),
		SourceDebugExtension(new JavaClassVersion(MAJOR_1_5, MINOR_1_5)),
		LineNumberTable(new JavaClassVersion(MAJOR_1_0_2, MINOR_1_0_2)),
		LocalVariableTable(new JavaClassVersion(MAJOR_1_0_2, MINOR_1_0_2)),
		LocalVariableTypeTable(new JavaClassVersion(MAJOR_1_5, MINOR_1_5)),
		Deprecated(new JavaClassVersion(MAJOR_1_0_2, MINOR_1_0_2)),
		RuntimeVisibleAnnotations(new JavaClassVersion(MAJOR_1_5, MINOR_1_5)),
		RuntimeInvisibleAnnotations(new JavaClassVersion(MAJOR_1_5, MINOR_1_5)),
		RuntimeVisibleParameterAnnotations(new JavaClassVersion(MAJOR_1_5, MINOR_1_5)),
		RuntimeInvisibleParameterAnnotations(new JavaClassVersion(MAJOR_1_5, MINOR_1_5)),
		RuntimeVisibleTypeAnnotations(new JavaClassVersion(MAJOR_8, MINOR_8)),
		RuntimeInvisibleTypeAnnotations(new JavaClassVersion(MAJOR_8, MINOR_8)),
		AnnotationDefault(new JavaClassVersion(MAJOR_1_5, MINOR_1_5)),
		BootstrapMethods(new JavaClassVersion(MAJOR_1_7, MAJOR_1_7)),
		MethodParameters(new JavaClassVersion(MAJOR_8, MINOR_8)),
		Module(new JavaClassVersion(MAJOR_9, MINOR_9)),
		ModulePackages(new JavaClassVersion(MAJOR_9, MINOR_9)),
		ModuleMainClass(new JavaClassVersion(MAJOR_9, MINOR_9)),
		NestHost(new JavaClassVersion(MAJOR_11, MINOR_11)),
		NestMembers(new JavaClassVersion(MAJOR_11, MINOR_11)),
		Record(new JavaClassVersion(MAJOR_16, MINOR_16)),
		PermittedSubclasses(new JavaClassVersion(MAJOR_17, MINOR_17));
		
		private final JavaClassVersion	fromVersion;
		
		JavaAttributeType(final JavaClassVersion	fromVersion) {
			this.fromVersion = fromVersion;
		}
		
		public int getFromMajor() {
			return fromVersion.major;
		}

		public int getFromMinor() {
			return fromVersion.minor;
		}
		
		public JavaClassVersion getFromClassVersion() {
			return fromVersion;
		}
	}
	

	/**
	 * <p>Representation of {@linkplain JavaAttributeType} constants in the char[] format</p> 
	 */
	public static final char[]	ATTRIBUTE_ConstantValue = toCharArray(JavaAttributeType.ConstantValue);
	public static final char[]	ATTRIBUTE_Code = toCharArray(JavaAttributeType.Code);
	public static final char[]	ATTRIBUTE_StackMapTable = toCharArray(JavaAttributeType.StackMapTable);
	public static final char[]	ATTRIBUTE_Exceptions = toCharArray(JavaAttributeType.Exceptions);
	public static final char[]	ATTRIBUTE_InnerClasses = toCharArray(JavaAttributeType.InnerClasses);
	public static final char[]	ATTRIBUTE_EnclosingMethod = toCharArray(JavaAttributeType.EnclosingMethod);
	public static final char[]	ATTRIBUTE_Synthetic = toCharArray(JavaAttributeType.Synthetic);
	public static final char[]	ATTRIBUTE_Signature = toCharArray(JavaAttributeType.Signature);
	public static final char[]	ATTRIBUTE_SourceFile = toCharArray(JavaAttributeType.SourceFile);
	public static final char[]	ATTRIBUTE_SourceDebugExtension = toCharArray(JavaAttributeType.SourceDebugExtension);
	public static final char[]	ATTRIBUTE_LineNumberTable = toCharArray(JavaAttributeType.LineNumberTable);
	public static final char[]	ATTRIBUTE_LocalVariableTable = toCharArray(JavaAttributeType.LocalVariableTable);
	public static final char[]	ATTRIBUTE_LocalVariableTypeTable = toCharArray(JavaAttributeType.LocalVariableTypeTable);
	public static final char[]	ATTRIBUTE_Deprecated = toCharArray(JavaAttributeType.Deprecated);
	public static final char[]	ATTRIBUTE_RuntimeVisibleTypeAnnotations = toCharArray(JavaAttributeType.RuntimeVisibleTypeAnnotations);
	public static final char[]	ATTRIBUTE_RuntimeInvisibleTypeAnnotations = toCharArray(JavaAttributeType.RuntimeInvisibleTypeAnnotations);
	public static final char[]	ATTRIBUTE_AnnotationDefault = toCharArray(JavaAttributeType.AnnotationDefault);
	public static final char[]	ATTRIBUTE_BootstrapMethods = toCharArray(JavaAttributeType.BootstrapMethods);
	public static final char[]	ATTRIBUTE_MethodParameters = toCharArray(JavaAttributeType.MethodParameters);
	public static final char[]	ATTRIBUTE_Module = toCharArray(JavaAttributeType.Module);
	public static final char[]	ATTRIBUTE_ModulePackages = toCharArray(JavaAttributeType.ModulePackages);
	public static final char[]	ATTRIBUTE_ModuleMainClass = toCharArray(JavaAttributeType.ModuleMainClass);
	public static final char[]	ATTRIBUTE_NestHost = toCharArray(JavaAttributeType.NestHost);
	public static final char[]	ATTRIBUTE_NestMembers = toCharArray(JavaAttributeType.NestMembers);
	public static final char[]	ATTRIBUTE_Record = toCharArray(JavaAttributeType.Record);
	public static final char[]	ATTRIBUTE_PermittedSubclasses = toCharArray(JavaAttributeType.PermittedSubclasses);

	private static char[] toCharArray(final JavaAttributeType attr) {
		return attr.name().toCharArray();
	}
}
