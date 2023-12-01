package chav1961.purelib.cdb;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>This class keeps a set of constants used in the Java Byte Code</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */

public class JavaByteCodeConstants {
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
	public static final short			ACC_NATIVE = 0x0100;
	public static final short			ACC_INTERFACE = 0x0200;
	public static final short			ACC_ABSTRACT = 0x0400;
	public static final short			ACC_STRICT = 0x0800;
	public static final short			ACC_SYNTHETIC = 0x1000;
	public static final short			ACC_ANNOTATION = 0x2000;
	public static final short			ACC_ENUM = 0x4000;
	public static final short			ACC_MODULE = (short)0x8000;
	public static final short			ACC_SYNCHRONIZED = 0x0020;
	public static final short			ACC_BRIDGE = 0x0040;
	public static final short			ACC_VARARGS = 0x0080;


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

	/**
	 * <p>This enumeration contains officially supported attribute names inside Java Byte Code</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static enum AnnotationType {
		ConstantValue(new JavaClassVersion(45,3)),
		Code(new JavaClassVersion(45,3)),
		StackMapTable(new JavaClassVersion(50,0)),
		Exceptions(new JavaClassVersion(45,3)),
		InnerClasses(new JavaClassVersion(45,3)),
		EnclosingMethod(new JavaClassVersion(49,0)),
		Synthetic(new JavaClassVersion(45,3)),
		Signature(new JavaClassVersion(49,0)),
		SourceFile(new JavaClassVersion(45,3)),
		SourceDebugExtension(new JavaClassVersion(49,0)),
		LineNumberTable(new JavaClassVersion(45,3)),
		LocalVariableTable(new JavaClassVersion(45,3)),
		LocalVariableTypeTable(new JavaClassVersion(47,0)),
		Deprecated(new JavaClassVersion(45,3)),
		RuntimeVisibleAnnotations(new JavaClassVersion(49,0)),
		RuntimeInvisibleAnnotations(new JavaClassVersion(49,0)),
		RuntimeVisibleParameterAnnotations(new JavaClassVersion(49,0)),
		RuntimeInvisibleParameterAnnotations(new JavaClassVersion(49,0)),
		RuntimeVisibleTypeAnnotations(new JavaClassVersion(52,0)),
		RuntimeInvisibleTypeAnnotations(new JavaClassVersion(52,0)),
		AnnotationDefault(new JavaClassVersion(49,0)),
		BootstrapMethods(new JavaClassVersion(51,0)),
		MethodParameters(new JavaClassVersion(52,0)),
		Module(new JavaClassVersion(53,0)),
		ModulePackages(new JavaClassVersion(53,0)),
		ModuleMainClass(new JavaClassVersion(53,0)),
		NestHost(new JavaClassVersion(55,0)),
		NestMembers(new JavaClassVersion(55,0)),
		Record(new JavaClassVersion(60,0)),
		PermittedSubclasses(new JavaClassVersion(61,0));
		
		private final JavaClassVersion	fromVersion;
		
		AnnotationType(final JavaClassVersion	fromVersion) {
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
	
	
	public static final char[]			ATTRIBUTE_ConstantValue = AnnotationType.ConstantValue.name().toCharArray();
	public static final char[]			ATTRIBUTE_Code = AnnotationType.Code.name().toCharArray();
	public static final char[]			ATTRIBUTE_StackMapTable = AnnotationType.StackMapTable.name().toCharArray();
	public static final char[]			ATTRIBUTE_Exceptions = AnnotationType.Exceptions.name().toCharArray();
	public static final char[]			ATTRIBUTE_InnerClasses = AnnotationType.InnerClasses.name().toCharArray();
	public static final char[]			ATTRIBUTE_EnclosingMethod = AnnotationType.EnclosingMethod.name().toCharArray();
	public static final char[]			ATTRIBUTE_Synthetic = AnnotationType.Synthetic.name().toCharArray();
	public static final char[]			ATTRIBUTE_Signature = AnnotationType.Signature.name().toCharArray();
	public static final char[]			ATTRIBUTE_SourceFile = AnnotationType.SourceFile.name().toCharArray();
	public static final char[]			ATTRIBUTE_SourceDebugExtension = AnnotationType.SourceDebugExtension.name().toCharArray();
	public static final char[]			ATTRIBUTE_LineNumberTable = AnnotationType.LineNumberTable.name().toCharArray();
	public static final char[]			ATTRIBUTE_LocalVariableTable = AnnotationType.LocalVariableTable.name().toCharArray();
	public static final char[]			ATTRIBUTE_LocalVariableTypeTable = AnnotationType.LocalVariableTypeTable.name().toCharArray();
	public static final char[]			ATTRIBUTE_Deprecated = AnnotationType.Deprecated.name().toCharArray();
	public static final char[]			ATTRIBUTE_RuntimeVisibleTypeAnnotations = AnnotationType.RuntimeVisibleTypeAnnotations.name().toCharArray();
	public static final char[]			ATTRIBUTE_RuntimeInvisibleTypeAnnotations = AnnotationType.RuntimeInvisibleTypeAnnotations.name().toCharArray();
	public static final char[]			ATTRIBUTE_AnnotationDefault = AnnotationType.AnnotationDefault.name().toCharArray();
	public static final char[]			ATTRIBUTE_BootstrapMethods = AnnotationType.BootstrapMethods.name().toCharArray();
	public static final char[]			ATTRIBUTE_MethodParameters = AnnotationType.MethodParameters.name().toCharArray();
	public static final char[]			ATTRIBUTE_Module = AnnotationType.Module.name().toCharArray();
	public static final char[]			ATTRIBUTE_ModulePackages = AnnotationType.ModulePackages.name().toCharArray();
	public static final char[]			ATTRIBUTE_ModuleMainClass = AnnotationType.ModuleMainClass.name().toCharArray();
	public static final char[]			ATTRIBUTE_NestHost = AnnotationType.NestHost.name().toCharArray();
	public static final char[]			ATTRIBUTE_NestMembers = AnnotationType.NestMembers.name().toCharArray();
	public static final char[]			ATTRIBUTE_Record = AnnotationType.Record.name().toCharArray();
	public static final char[]			ATTRIBUTE_PermittedSubclasses = AnnotationType.PermittedSubclasses.name().toCharArray();
}
