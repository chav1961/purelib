package chav1961.purelib.streams.char2byte.asm;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.streams.char2byte.asm.macro.MacroClassLoader;
import chav1961.purelib.streams.char2byte.asm.macro.MacroCompiler;
import chav1961.purelib.streams.char2byte.asm.macro.Macros;

class LineParser implements LineByLineProcessorCallback {
	private static final int						EXPONENT_BASE = 305;
	private static final double[]					EXPONENTS;

	private static final char[]						TRUE = "true".toCharArray();
	private static final char[]						FALSE = "false".toCharArray();
	
	private static final long						DEFAULT_MARK = Integer.MAX_VALUE+1L;
	private static final Comparator<Entry<Long,Long>>	SORT_COMPARATOR = new Comparator<Entry<Long,Long>>(){
															@Override
															public int compare(final Entry<Long,Long> e1, final Entry<Long,Long> e2) {
																return (int)(e1.getKey() - e2.getKey());
															}
														};
	
	private static final int						DIR_CLASS = 1;
	private static final int						DIR_METHOD = 2;
	private static final int						DIR_FIELD = 3;
	private static final int						DIR_END = 4;
	private static final int						DIR_PACKAGE = 5;
	private static final int						DIR_IMPORT = 6;
	private static final int						DIR_INCLUDE = 7;
	private static final int						DIR_INTERFACE = 8;
	private static final int						DIR_PARAMETER = 9;
	private static final int						DIR_VAR = 10;
	private static final int						DIR_BEGIN = 11;
	private static final int						DIR_STACK = 12;
	private static final int						DIR_TRY = 13;
	private static final int						DIR_CATCH = 14;
	private static final int						DIR_DEFAULT = 15;
	private static final int						DIR_END_TRY = 16;
	private static final int						DIR_MACRO = 17;
	private static final int						DIR_VERSION = 18;
	private static final int						DIR_SOURCE = 19;
	private static final int						DIR_LINE = 20;
	
	private static final int						OPTION_PUBLIC = 101;
	private static final int						OPTION_FINAL = 102;
	private static final int						OPTION_ABSTRACT = 103;
	private static final int						OPTION_SYNTHETIC = 104;
	private static final int						OPTION_ENUM = 105;
	private static final int						OPTION_EXTENDS = 106;
	private static final int						OPTION_IMPLEMENTS = 107;
	private static final int						OPTION_PRIVATE = 108;
	private static final int						OPTION_PROTECTED = 109;
	private static final int						OPTION_STATIC = 110;
	private static final int						OPTION_VOLATILE = 111;
	private static final int						OPTION_TRANSIENT = 112;
	private static final int						OPTION_SYNCHRONIZED = 113;
	private static final int						OPTION_BRIDGE = 114;
	private static final int						OPTION_VARARGS = 115;
	private static final int						OPTION_NATIVE = 116;
	private static final int						OPTION_STRICT = 117;
	private static final int						OPTION_THROWS = 118;
	
	private static final int						T_BASE = 200;
	private static final int						T_BOOLEAN = T_BASE+4;
	private static final int						T_CHAR = T_BASE+5;
	private static final int						T_FLOAT = T_BASE+6;
	private static final int						T_DOUBLE = T_BASE+7;
	private static final int						T_BYTE = T_BASE+8;
	private static final int						T_SHORT = T_BASE+9;
	private static final int						T_INT = T_BASE+10;
	private static final int						T_LONG = T_BASE+11;	
	private static final int						T_BASE_END = T_LONG;

	private static final int						STACK_PESSIMISTIC = 300;
	private static final int						STACK_OPTIMISTIC = 301;

	private static final int						CMD_LOAD = 400;
	private static final int						CMD_STORE = 401;
	private static final int						CMD_EVAL = 402;
	private static final int						CMD_CALL = 403;

	private static final int						LINE_NONE = 500;
	private static final int						LINE_AUTO = 501;
	private static final int						LINE_MANUAL = 502;
	
	private static final String[]					ALOAD_SPECIAL = {"aload_0","aload_1","aload_2","aload_3"}; 
	private static final String[]					ASTORE_SPECIAL = {"astore_0","astore_1","astore_2","astore_3"}; 
	private static final String[]					DLOAD_SPECIAL = {"dload_0","dload_1","dload_2","dload_3"}; 
	private static final String[]					DSTORE_SPECIAL = {"dstore_0","dstore_1","dstore_2","dstore_3"}; 
	private static final String[]					FLOAD_SPECIAL = {"fload_0","fload_1","fload_2","fload_3"}; 
	private static final String[]					FSTORE_SPECIAL = {"fstore_0","fstore_1","fstore_2","fstore_3"}; 
	private static final String[]					ILOAD_SPECIAL = {"iload_0","iload_1","iload_2","iload_3"}; 
	private static final String[]					ISTORE_SPECIAL = {"istore_0","istore_1","istore_2","istore_3"}; 
	private static final String[]					LLOAD_SPECIAL = {"lload_0","lload_1","lload_2","lload_3"}; 
	private static final String[]					LSTORE_SPECIAL = {"lstore_0","lstore_1","lstore_2","lstore_3"}; 
	private static final String[]					BIPUSH_SPECIAL = {"iconst_m1","iconst_0","iconst_1","iconst_2","iconst_3","iconst_4","iconst_5"}; 
	
	private static final char[]						CLASS_SUFFIX = ".class".toCharArray(); 
	private static final char[]						PROTECTED_KEYWORD = "protected".toCharArray(); 

	private static final SyntaxTreeInterface<DirectiveDescriptor>	staticDirectiveTree = new AndOrTree<>(2,16);
	private static final SyntaxTreeInterface<CommandDescriptor>		staticCommandTree = new AndOrTree<>(3,16);

	private enum EvalState {
		term, unary, multiplicational, additional 
	}
	
	private enum ParserState {
		beforePackage, insideMacros, beforeImport, insideClass, insideInterface, insideInterfaceAbstractMethod, insideClassAbstractMethod, insideClassMethod, insideClassBody, insideBegin, insideMethodLookup, insideMethodTable, afterClass
	}
	
	private enum CommandFormat{
		single, byteIndex, extendableByteIndex, byteValue, shortValue, byteType, byteIndexAndByteValue, shortIndexAndByteValue, classShortIndex, valueByteIndex, valueShortIndex, valueShortIndex2, shortBrunch, longBrunch, shortGlobalIndex, call, callDynamic, callInterface, lookupSwitch, tableSwitch, restricted
	}
	
	static {
		EXPONENTS = new double[2 * EXPONENT_BASE + 1];
		
		for (int index = -EXPONENT_BASE; index <= EXPONENT_BASE; index++) {
			EXPONENTS[index+EXPONENT_BASE] = Double.valueOf("1e"+index);
		}
		
		final DirectiveMarker	m = new DirectiveMarker(); 
		
		placeStaticDirective(DIR_CLASS,m,".class");
		placeStaticDirective(DIR_METHOD,m,".method");
		placeStaticDirective(DIR_FIELD,m,".field");
		placeStaticDirective(DIR_END,m,".end");
		placeStaticDirective(DIR_PACKAGE,m,".package");
		placeStaticDirective(DIR_IMPORT,m,".import");
		placeStaticDirective(DIR_INCLUDE,m,".include");
		placeStaticDirective(DIR_INTERFACE,m,".interface");
		placeStaticDirective(DIR_PARAMETER,m,".parameter");
		placeStaticDirective(DIR_VAR,m,".var");
		placeStaticDirective(DIR_BEGIN,m,".begin");
		placeStaticDirective(DIR_STACK,m,".stack");
		placeStaticDirective(DIR_TRY,m,".try");
		placeStaticDirective(DIR_CATCH,m,".catch");
		placeStaticDirective(DIR_DEFAULT,m,".default");
		placeStaticDirective(DIR_END_TRY,m,".endtry");
		placeStaticDirective(DIR_MACRO,m,".macro");
		placeStaticDirective(DIR_VERSION,m,".version");
		placeStaticDirective(DIR_SOURCE,m,".source");
		placeStaticDirective(DIR_LINE,m,".line");
		
		placeStaticDirective(OPTION_PUBLIC,new DirectiveOption(Constants.ACC_PUBLIC),"public");
		placeStaticDirective(OPTION_FINAL,new DirectiveOption(Constants.ACC_FINAL),"final");
		placeStaticDirective(OPTION_ABSTRACT,new DirectiveOption(Constants.ACC_ABSTRACT),"abstract");
		placeStaticDirective(OPTION_SYNTHETIC,new DirectiveOption(Constants.ACC_SYNTHETIC),"synthetic");
		placeStaticDirective(OPTION_ENUM,new DirectiveOption(Constants.ACC_ENUM),"enum");
		placeStaticDirective(OPTION_EXTENDS,new DirectiveClassOption(),"extends");
		placeStaticDirective(OPTION_IMPLEMENTS,new DirectiveInterfacesOption(),"implements");
		placeStaticDirective(OPTION_PRIVATE,new DirectiveOption(Constants.ACC_PRIVATE),"private");
		placeStaticDirective(OPTION_PROTECTED,new DirectiveOption(Constants.ACC_PROTECTED),"protected");
		placeStaticDirective(OPTION_STATIC,new DirectiveOption(Constants.ACC_STATIC),"static");
		placeStaticDirective(OPTION_VOLATILE,new DirectiveOption(Constants.ACC_VOLATILE),"volatile");
		placeStaticDirective(OPTION_TRANSIENT,new DirectiveOption(Constants.ACC_TRANSIENT),"transient");
		placeStaticDirective(OPTION_SYNCHRONIZED,new DirectiveOption(Constants.ACC_SYNCHRONIZED),"synchronized");
		placeStaticDirective(OPTION_BRIDGE,new DirectiveOption(Constants.ACC_BRIDGE),"bridge");
		placeStaticDirective(OPTION_VARARGS,new DirectiveOption(Constants.ACC_VARARGS),"varargs");
		placeStaticDirective(OPTION_NATIVE,new DirectiveOption(Constants.ACC_NATIVE),"native");
		placeStaticDirective(OPTION_STRICT,new DirectiveOption(Constants.ACC_STRICT),"strictfp");
		placeStaticDirective(OPTION_THROWS,new DirectiveExceptionsOption(),"throws");

		placeStaticDirective(T_BOOLEAN,m,"boolean");
		placeStaticDirective(T_CHAR,m,"char");
		placeStaticDirective(T_FLOAT,m,"float");
		placeStaticDirective(T_DOUBLE,m,"double");
		placeStaticDirective(T_BYTE,m,"byte");
		placeStaticDirective(T_SHORT,m,"short");
		placeStaticDirective(T_INT,m,"int");
		placeStaticDirective(T_LONG,m,"long");	

		placeStaticDirective(STACK_OPTIMISTIC,m,"optimistic");	
		placeStaticDirective(STACK_PESSIMISTIC,m,"pessimistic");	

		placeStaticDirective(CMD_LOAD,m,"*load");	
		placeStaticDirective(CMD_STORE,m,"*store");	
		placeStaticDirective(CMD_EVAL,m,"*eval");	
		placeStaticDirective(CMD_CALL,m,"*call");	

		placeStaticDirective(LINE_NONE,m,"none");	
		placeStaticDirective(LINE_AUTO,m,"auto");	
		placeStaticDirective(LINE_MANUAL,m,"manual");	
		
		placeStaticCommand(0x32,1,"aaload");
		placeStaticCommand(0x53,-1,"aastore");
		placeStaticCommand(0x01,1,"aconst_null");
		placeStaticCommand(0x19,1,"aload",CommandFormat.extendableByteIndex);
		placeStaticCommand(0x2a,1,"aload_0");
		placeStaticCommand(0x2b,1,"aload_1");
		placeStaticCommand(0x2c,1,"aload_2");
		placeStaticCommand(0x2d,1,"aload_3");
		placeStaticCommand(0xbd,1,"anewarray",CommandFormat.classShortIndex);
		placeStaticCommand(0xb0,1,"areturn");
		placeStaticCommand(0xbe,1,"arraylength");
		placeStaticCommand(0x3a,1,"astore",CommandFormat.extendableByteIndex);
		placeStaticCommand(0x4b,1,"astore_0");
		placeStaticCommand(0x4c,1,"astore_1");
		placeStaticCommand(0x4d,1,"astore_2");
		placeStaticCommand(0x4e,1,"astore_3");
		placeStaticCommand(0xbf,1,"athrow");
		placeStaticCommand(0x33,1,"baload");
		placeStaticCommand(0x54,1,"bastore");
		placeStaticCommand(0x10,1,"bipush",CommandFormat.byteValue);
		placeStaticCommand(0x34,1,"caload");
		placeStaticCommand(0x55,1,"castore");
		placeStaticCommand(0xc0,1,"checkcast",CommandFormat.classShortIndex);
		placeStaticCommand(0x90,1,"d2f");
		placeStaticCommand(0x8e,1,"d2i");
		placeStaticCommand(0x8f,1,"d2l");
		placeStaticCommand(0x63,1,"dadd");
		placeStaticCommand(0x31,1,"daload");
		placeStaticCommand(0x52,1,"dastore");
		placeStaticCommand(0x98,1,"dcmpg");
		placeStaticCommand(0x97,1,"dcmpl");
		placeStaticCommand(0x0e,1,"dconst_0");
		placeStaticCommand(0x0f,1,"dconst_1");
		placeStaticCommand(0x6f,1,"ddiv");
		placeStaticCommand(0x18,1,"dload",CommandFormat.extendableByteIndex);
		placeStaticCommand(0x26,1,"dload_0");
		placeStaticCommand(0x27,1,"dload_1");
		placeStaticCommand(0x28,1,"dload_2");
		placeStaticCommand(0x29,1,"dload_3");
		placeStaticCommand(0x6b,1,"dmul");
		placeStaticCommand(0x77,1,"dneg");
		placeStaticCommand(0x73,1,"drem");
		placeStaticCommand(0xaf,1,"dreturn");
		placeStaticCommand(0x39,1,"dstore",CommandFormat.extendableByteIndex);
		placeStaticCommand(0x47,1,"dstore_0");
		placeStaticCommand(0x48,1,"dstore_1");
		placeStaticCommand(0x49,1,"dstore_2");
		placeStaticCommand(0x4a,1,"dstore_3");
		placeStaticCommand(0x67,1,"dsub");
		placeStaticCommand(0x59,1,"dup");
		placeStaticCommand(0x5a,1,"dup_x1");
		placeStaticCommand(0x5b,1,"dup_x2");
		placeStaticCommand(0x5c,1,"dup2");
		placeStaticCommand(0x5d,1,"dup2_x1");
		placeStaticCommand(0x5e,1,"dup2_x2");
		placeStaticCommand(0x8d,1,"f2d");
		placeStaticCommand(0x8b,1,"f2i");
		placeStaticCommand(0x8c,1,"f2l");
		placeStaticCommand(0x62,1,"fadd");
		placeStaticCommand(0x30,1,"faload");
		placeStaticCommand(0x51,1,"fastore");
		placeStaticCommand(0x96,1,"fcmpg");
		placeStaticCommand(0x95,1,"fcmpl");
		placeStaticCommand(0x0b,1,"fconst_0");
		placeStaticCommand(0x0c,1,"fconst_1");
		placeStaticCommand(0x0d,1,"fconst_2");
		placeStaticCommand(0x6e,1,"fdiv");
		placeStaticCommand(0x17,1,"fload",CommandFormat.extendableByteIndex);
		placeStaticCommand(0x22,1,"fload_0");
		placeStaticCommand(0x23,1,"fload_1");
		placeStaticCommand(0x24,1,"fload_2");
		placeStaticCommand(0x25,1,"fload_3");
		placeStaticCommand(0x6a,1,"fmul");
		placeStaticCommand(0x76,1,"fneg");
		placeStaticCommand(0x72,1,"frem");
		placeStaticCommand(0xae,1,"freturn");
		placeStaticCommand(0x38,1,"fstore",CommandFormat.extendableByteIndex);
		placeStaticCommand(0x43,1,"fstore_0");
		placeStaticCommand(0x44,1,"fstore_1");
		placeStaticCommand(0x45,1,"fstore_2");
		placeStaticCommand(0x46,1,"fstore_3");
		placeStaticCommand(0x66,1,"fsub");
		placeStaticCommand(0xb4,1,"getfield",CommandFormat.shortGlobalIndex);
		placeStaticCommand(0xb2,1,"getstatic",CommandFormat.shortGlobalIndex);
		placeStaticCommand(0xa7,1,"goto",CommandFormat.shortBrunch);
		placeStaticCommand(0xc8,1,"goto_w",CommandFormat.longBrunch);
		placeStaticCommand(0x91,1,"i2b");
		placeStaticCommand(0x92,1,"i2c");
		placeStaticCommand(0x87,1,"i2d");
		placeStaticCommand(0x86,1,"i2f");
		placeStaticCommand(0x85,1,"i2l");
		placeStaticCommand(0x93,1,"i2s");
		placeStaticCommand(0x60,1,"iadd");
		placeStaticCommand(0x2e,1,"iaload");
		placeStaticCommand(0x7e,1,"iand");
		placeStaticCommand(0x4f,1,"iastore");
		placeStaticCommand(0x02,1,"iconst_m1");
		placeStaticCommand(0x03,1,"iconst_0");
		placeStaticCommand(0x04,1,"iconst_1");
		placeStaticCommand(0x05,1,"iconst_2");
		placeStaticCommand(0x06,1,"iconst_3");
		placeStaticCommand(0x07,1,"iconst_4");
		placeStaticCommand(0x08,1,"iconst_5");
		placeStaticCommand(0x6c,1,"idiv");
		placeStaticCommand(0x9f,1,"if_icmpeq",CommandFormat.shortBrunch);
		placeStaticCommand(0xa0,1,"if_icmpne",CommandFormat.shortBrunch);
		placeStaticCommand(0xa1,1,"if_icmplt",CommandFormat.shortBrunch);
		placeStaticCommand(0xa2,1,"if_icmpge",CommandFormat.shortBrunch);
		placeStaticCommand(0xa3,1,"if_icmpgt",CommandFormat.shortBrunch);
		placeStaticCommand(0xa4,1,"if_icmple",CommandFormat.shortBrunch);
		placeStaticCommand(0x99,1,"ifeq",CommandFormat.shortBrunch);
		placeStaticCommand(0x9a,1,"ifne",CommandFormat.shortBrunch);
		placeStaticCommand(0x9b,1,"iflt",CommandFormat.shortBrunch);
		placeStaticCommand(0x9c,1,"ifge",CommandFormat.shortBrunch);
		placeStaticCommand(0x9d,1,"ifgt",CommandFormat.shortBrunch);
		placeStaticCommand(0x9e,1,"ifle",CommandFormat.shortBrunch);
		placeStaticCommand(0xc7,1,"ifnonnull",CommandFormat.shortBrunch);
		placeStaticCommand(0xc6,1,"ifnull",CommandFormat.shortBrunch);
		placeStaticCommand(0x84,1,"iinc",CommandFormat.byteIndexAndByteValue);
		placeStaticCommand(0x15,1,"iload",CommandFormat.extendableByteIndex);
		placeStaticCommand(0x1a,1,"iload_0");
		placeStaticCommand(0x1b,1,"iload_1");
		placeStaticCommand(0x1c,1,"iload_2");
		placeStaticCommand(0x1d,1,"iload_3");
		placeStaticCommand(0x68,1,"imul");
		placeStaticCommand(0x74,1,"ineg");
		placeStaticCommand(0xc1,1,"instanceof",CommandFormat.classShortIndex);
		placeStaticCommand(0xba,1,"invokedynamic",CommandFormat.restricted);
		placeStaticCommand(0xb9,1,"invokeinterface",CommandFormat.callInterface);
		placeStaticCommand(0xb7,1,"invokespecial",CommandFormat.call);
		placeStaticCommand(0xb8,1,"invokestatic",CommandFormat.call);
		placeStaticCommand(0xb6,1,"invokevirtual",CommandFormat.call);
		placeStaticCommand(0x80,1,"ior");
		placeStaticCommand(0x70,1,"irem");
		placeStaticCommand(0xac,1,"ireturn");
		placeStaticCommand(0x78,1,"ishl");
		placeStaticCommand(0x7a,1,"ishr");
		placeStaticCommand(0x36,1,"istore",CommandFormat.extendableByteIndex);
		placeStaticCommand(0x3b,1,"istore_0");
		placeStaticCommand(0x3c,1,"istore_1");
		placeStaticCommand(0x3d,1,"istore_2");
		placeStaticCommand(0x3e,1,"istore_3");
		placeStaticCommand(0x64,1,"isub");
		placeStaticCommand(0x7c,1,"iushr");
		placeStaticCommand(0x82,1,"ixor");
		placeStaticCommand(0xa8,1,"jsr",CommandFormat.restricted);
		placeStaticCommand(0xc9,1,"jsr_w",CommandFormat.restricted);
		placeStaticCommand(0x8a,1,"l2d");
		placeStaticCommand(0x89,1,"l2f");
		placeStaticCommand(0x88,1,"l2i");
		placeStaticCommand(0x61,1,"ladd");
		placeStaticCommand(0x2f,1,"laload");
		placeStaticCommand(0x7f,1,"land");
		placeStaticCommand(0x50,1,"lastore");
		placeStaticCommand(0x94,1,"lcmp");
		placeStaticCommand(0x09,1,"lconst_0");
		placeStaticCommand(0x0a,1,"lconst_1");
		placeStaticCommand(0x12,1,"ldc",CommandFormat.valueByteIndex);
		placeStaticCommand(0x13,1,"ldc_w",CommandFormat.valueShortIndex);
		placeStaticCommand(0x14,1,"ldc2_w",CommandFormat.valueShortIndex2);
		placeStaticCommand(0x6d,1,"ldiv");
		placeStaticCommand(0x16,1,"lload",CommandFormat.extendableByteIndex);
		placeStaticCommand(0x1e,1,"lload_0");
		placeStaticCommand(0x1f,1,"lload_1");
		placeStaticCommand(0x20,1,"lload_2");
		placeStaticCommand(0x21,1,"lload_3");
		placeStaticCommand(0x69,1,"lmul");
		placeStaticCommand(0x75,1,"lneg");
		placeStaticCommand(0xab,1,"lookupswitch",CommandFormat.lookupSwitch);
		placeStaticCommand(0x81,1,"lor");
		placeStaticCommand(0x71,1,"lrem");
		placeStaticCommand(0xad,1,"lreturn");
		placeStaticCommand(0x79,1,"lshl");
		placeStaticCommand(0x7b,1,"lshr");
		placeStaticCommand(0x37,1,"lstore",CommandFormat.extendableByteIndex);
		placeStaticCommand(0x3f,1,"lstore_0");
		placeStaticCommand(0x40,1,"lstore_1");
		placeStaticCommand(0x41,1,"lstore_2");
		placeStaticCommand(0x42,1,"lstore_3");
		placeStaticCommand(0x65,1,"lsub");
		placeStaticCommand(0x7d,1,"lushr");
		placeStaticCommand(0x83,1,"lxor");
		placeStaticCommand(0xc2,1,"monitorenter");
		placeStaticCommand(0xc3,1,"monitorexit");
		placeStaticCommand(0xc5,1,"multianewarray",CommandFormat.shortIndexAndByteValue);
		placeStaticCommand(0xbb,1,"new",CommandFormat.classShortIndex);
		placeStaticCommand(0xbc,1,"newarray",CommandFormat.byteType);
		placeStaticCommand(0x00,1,"nop");
		placeStaticCommand(0x57,1,"pop");
		placeStaticCommand(0x58,1,"pop2");
		placeStaticCommand(0xb5,1,"putfield",CommandFormat.shortGlobalIndex);
		placeStaticCommand(0xb3,1,"putstatic",CommandFormat.shortGlobalIndex);
		placeStaticCommand(0xa9,1,"ret",CommandFormat.restricted);
		placeStaticCommand(0xb1,1,"return");
		placeStaticCommand(0x35,1,"saload");
		placeStaticCommand(0x56,1,"sastore");
		placeStaticCommand(0x11,1,"sipush",CommandFormat.shortValue);
		placeStaticCommand(0x5f,1,"swap");
		placeStaticCommand(0xaa,1,"tableswitch",CommandFormat.tableSwitch);
		placeStaticCommand(0xc4,1,"wide",CommandFormat.restricted);
	}
	
	
	private final ClassDescriptionRepo					cdr;
	private final SyntaxTreeInterface<Macros>			macros;
	private final MacroClassLoader						loader;
	private final ClassContainer						cc;
	private final SyntaxTreeInterface<NameDescriptor>	tree;
	private final Class<?>[]							forClass = new Class<?>[1];
	private final EntityDescriptor						forEntity = new EntityDescriptor();
	private final Writer								diagnostics;

	private ParserState									state = ParserState.beforePackage;
	private long										packageId = -1, constructorId, classConstructorId, voidId;
	private long										classNameId, joinedClassNameId;
	private long										methodNameId;
	private MethodDescriptor							methodDescriptor;
	private short										classFlags = 0;
	private Map<Long,Long>								jumps = new HashMap<>();
	private int											switchAddress;
	private int											beginLevel = 0;
	private int											classConstructorCount = 0;
	private ParserState									beforeBegin;
	private List<int[]>									tryList = new ArrayList<int[]>();
	private Macros										currentMacros = null;
	private boolean										addLines2Class = true;
	private boolean										addLines2ClassManually = false;
	
	LineParser(final ClassContainer cc, final ClassDescriptionRepo cdr, final SyntaxTreeInterface<Macros> macros, final MacroClassLoader loader) throws IOException, ContentException {
		this.cc = cc;
		this.cdr = cdr;
		this.macros = macros;
		this.loader = loader;
		this.tree = cc.getNameTree();
		this.diagnostics = null;
		constructorId = tree.placeOrChangeName("<init>",new NameDescriptor());
		classConstructorId = tree.placeOrChangeName("<clinit>",new NameDescriptor());
		voidId = tree.placeOrChangeName("void",new NameDescriptor());
	}

	LineParser(final ClassContainer cc, final ClassDescriptionRepo cdr, final SyntaxTreeInterface<Macros> macros, final MacroClassLoader loader, final Writer diagnostics) throws IOException, ContentException {
		this.cc = cc;
		this.cdr = cdr;
		this.macros = macros;
		this.loader = loader;
		this.tree = cc.getNameTree();				
		this.diagnostics = diagnostics;
		constructorId = tree.placeOrChangeName("<init>",new NameDescriptor());
		classConstructorId = tree.placeOrChangeName("<clinit>",new NameDescriptor());
		voidId = tree.placeOrChangeName("void",new NameDescriptor());
	}
	
	@Override
	public void processLine(final long displacement, final int lineNo, final char[] data, final int from, final int len) throws IOException {
		int		start = from, end = Math.min(from+len,data.length);
		int		startName, endName, startDir, endDir;
		long	id = -1;

		if (diagnostics != null) {
			diagnostics.write("\t"+new String(data,from,len));
		}
		
		try{if (state == ParserState.insideMacros) {	// Redirect macros code into macros
				currentMacros.processLine(displacement,lineNo,data,from,len);
				if (currentMacros.isPrepared()) {
					final String	macroName = new String(currentMacros.getName());
					
					if (macros.seekName(macroName) >= 0) {
						throw new IOException("Duplicate macros ["+macroName+"] in the input stream");
					}
					else {
						final GrowableCharArray	writer = new GrowableCharArray(true), stringRepo = new GrowableCharArray(false);
						final String			className = this.getClass().getPackage().getName()+'.'+new String(currentMacros.getName()); 
						
						try{MacroCompiler.compile(className,currentMacros.getRoot(),writer,stringRepo);
							currentMacros.compile(loader.createClass(className,writer).getConstructor(char[].class).newInstance(stringRepo.extract()));
							macros.placeOrChangeName(macroName,currentMacros);
						} catch (CalculationException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
							e.printStackTrace();
							throw new IOException(e.getLocalizedMessage(),e); 
						}
						state = ParserState.beforePackage;
						currentMacros = null;
					}
				}
				return;
			}
			
			if (data[start] > ' ') {
				if (data[start] == ';') {	// Comment line
					return;
				}
				
				startName = start;
				start = skipSimpleName(data,start);
				endName = start;
				if (endName == startName) {
					throw new IOException(new SyntaxException(lineNo,0,"Illegal label/entity name"));
				}
				
				id = tree.placeOrChangeName(data,startName,endName,new NameDescriptor());
				
				start = InternalUtils.skipBlank(data,start);
				if (data[start] == ':') {
					switch (state) {
						case insideClassBody :
						case insideBegin :
							putLabel(id);
							break;
						default :
							throw new ContentException("Branch label outside the method body!");
					}
					start++;
					id = -1;
				}
			}
			
			startDir = start = InternalUtils.skipBlank(data,start);
			
			switch (data[start]) {
				case '.' :
					endDir = start = skipSimpleName(data,start+1);
	
					switch ((int)staticDirectiveTree.seekName(data,startDir,endDir)) {
						case DIR_CLASS	:
							checkLabel(id,true);
							switch (state) {
								case beforePackage :
								case beforeImport :
									processClassDir(id,data,InternalUtils.skipBlank(data,start));
									state = ParserState.insideClass;
									break;
								case afterClass :
									throw new ContentException("Second class directive in the same stream. Use separate streams for each class/interface!");
								default :
									throw new ContentException("Nested class directive!");
							}						
							break;
						case DIR_INTERFACE:
							checkLabel(id,true);
							switch (state) {
								case beforePackage :
								case beforeImport :
									processInterfaceDir(id,data,InternalUtils.skipBlank(data,start));
									state = ParserState.insideInterface;
									break;
								case afterClass :
									throw new ContentException("Second interface directive in the same stream. Use separate streams for each class/interface!");
								default :
									throw new ContentException("Nested interface directive!");
							}						
							break;
						case DIR_FIELD	:
							checkLabel(id,true);
							switch (state) {
								case insideClass :
									processClassFieldDir(id,data,InternalUtils.skipBlank(data,start));
									break;
								case insideInterface :
									processInterfaceFieldDir(id,data,InternalUtils.skipBlank(data,start));
									break;
								case beforePackage :
								case beforeImport :
								case afterClass :
									throw new ContentException("Field directive outside the class/interface!");
								default :
									throw new ContentException("Field directive inside the method!");
							}
							break;
						case DIR_METHOD	:
							checkLabel(id,true);
							switch (state) {
								case insideClass :
									processClassMethodDir(id,data,InternalUtils.skipBlank(data,start));
									state = methodDescriptor.isAbstract() ? ParserState.insideClassAbstractMethod : ParserState.insideClassMethod;
									break;
								case insideInterface :
									processInterfaceMethodDir(id,data,InternalUtils.skipBlank(data,start));
									state = ParserState.insideInterfaceAbstractMethod;
									break;
								case beforePackage :
								case beforeImport :
								case afterClass :
									throw new ContentException("Method directive outside the class/interface!");
								default :
									throw new ContentException("Nested method directive!");
							}
							break;
						case DIR_PARAMETER	:
							checkLabel(id,true);
							switch (state) {
								case insideClassAbstractMethod :
								case insideInterfaceAbstractMethod :
								case insideClassMethod :
									processParameterDir(id,data,InternalUtils.skipBlank(data,start));
									break;
								default :
									throw new ContentException("Parameter directive is used outside the method description!");
							}
							break;
						case DIR_VAR	:
							checkLabel(id,true);
							switch (state) {
								case insideClassBody :
								case insideBegin :
									processVarDir(id,data,InternalUtils.skipBlank(data,start));
									break;
								default :
									throw new ContentException("Var directive is used outside the method description!");
							}
							break;
						case DIR_BEGIN	:
							switch (state) {
								case insideBegin :
									beginLevel++;
									methodDescriptor.push();
									break;
								case insideClassMethod :
								case insideClassBody :
									beforeBegin = state;
									state = ParserState.insideBegin;
									beginLevel++;
									methodDescriptor.push();
									break;
								default :
									throw new ContentException("Begin directive is valid in the method body only!");
							}
							break;
						case DIR_END	:
							switch (state) {
								case insideClass :
								case insideInterface :
									checkLabel(id,true);
									if (id != classNameId) {
										throw new ContentException("End directive closes class description, but it's label ["+tree.getName(id)+"] is differ from class name ["+tree.getName(classNameId)+"]");
									}
									else {
										state = ParserState.afterClass;
									}
									break;
								case insideClassAbstractMethod :
									checkLabel(id,true);
									if (id != methodNameId) {
										throw new ContentException("End directive closes method description, but it's label ["+tree.getName(id)+"] is differ from method name ["+tree.getName(methodNameId)+"]");
									}
									else {
										methodDescriptor.complete();
										state = ParserState.insideClass;
									}
									break;
								case insideClassMethod :
									throw new ContentException("Class method body is not defined!");
								case insideClassBody :
									checkLabel(id,true);
									if (id != methodNameId && id != classNameId) {
										throw new ContentException("End directive closes method description, but it's label ["+tree.getName(id)+"] is differ from method name ["+tree.getName(methodNameId)+"]");
									}
									else if (!areTryBlocksClosed()) {
										throw new ContentException("Unclosed try blocks inside the method body ["+tree.getName(methodNameId)+"]");
									}
									else {
										methodDescriptor.complete();
										state = ParserState.insideClass;
									}
									break;
								case insideInterfaceAbstractMethod :
									checkLabel(id,true);
									if (id != methodNameId) {
										throw new ContentException("End directive closes method description, but it's label ["+tree.getName(id)+"] is differ from method name ["+tree.getName(methodNameId)+"]");
									}
									else {
										methodDescriptor.complete();
										state = ParserState.insideInterface;
									}
									break;
								case insideBegin :
									if (beginLevel > 0) {
										methodDescriptor.pop();
										beginLevel--;
									}
									else {
										state = beforeBegin;
										checkLabel(id,true);
										if (id != methodNameId) {
											throw new ContentException("End directive closes method description, but it's label ["+tree.getName(id)+"] is differ from method name ["+tree.getName(methodNameId)+"]");
										}
										else {
											methodDescriptor.complete();
											state = ParserState.insideClass;
										}
									}
									break;
								case insideMethodLookup :
									fillLookup();	jumps.clear();
									state = ParserState.insideBegin; 
									break;
								case insideMethodTable :
									fillTable();	jumps.clear();
									state = ParserState.insideBegin; 
									break;
								default :
									throw new ContentException("End directive out of context!");
							}
							skip2line(data,start);
							break;
						case DIR_STACK	:
							checkLabel(id,false);
							switch (state) {
								case insideClassMethod :
									processStackDir(data,InternalUtils.skipBlank(data,start));
									state = ParserState.insideClassBody;
									break;
								case insideClassBody :
								case insideBegin :
									throw new ContentException("Duplicate Stack directive detected!");
								default :
									throw new ContentException("Stack directive is used outside the method description!");
							}
							break;
						case DIR_PACKAGE:
							checkLabel(id,false);
							switch (state) {
								case beforePackage :
									processPackageDir(data,InternalUtils.skipBlank(data,start));
									state = ParserState.beforeImport;
									break;
								case beforeImport :
									throw new ContentException("Duplicate package directive!");
								default :
									throw new ContentException("Package directive is used inside the class or interface description! Use import before the class/interface directive only!");
							}
							break;
						case DIR_IMPORT:
							checkLabel(id,false);
							switch (state) {
								case beforePackage :
								case beforeImport :
									processImportDir(data,InternalUtils.skipBlank(data,start));
									break;
								default :
									throw new ContentException("Package directive is used inside the class or interface description! Use import before the class/interface directive only!");
							}
							break;
						case DIR_INCLUDE:
							checkLabel(id,false);
							processIncludeDir(data,InternalUtils.skipBlank(data,start),end);
							break;
						case DIR_TRY	:
							switch (state) {
								case insideClassBody :
								case insideBegin :
									pushTryBlock();
									break;
								default :
									throw new ContentException("Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside method body only");
							}
							break;
						case DIR_CATCH	:
							switch (state) {
								case insideClassBody :
								case insideBegin :
									processCatch(data,InternalUtils.skipBlank(data,start),end);
									break;
								default :
									throw new ContentException("Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside method body only");
							}
							break;
						case DIR_END_TRY	:
							switch (state) {
								case insideClassBody :
								case insideBegin :
									popTryBlock(lineNo);
									break;
								default :
									throw new ContentException("Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside method body only");
							}
							break;
						case DIR_MACRO	:
							switch (state) {
								case beforePackage :
									state = ParserState.insideMacros;
									currentMacros = new Macros();
									currentMacros.processLine(displacement,lineNo,data,from,len);
									break;
								default :
									throw new ContentException("Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used before package description only");
							}
							break;
						case DIR_DEFAULT:
							switch (state) {
								case insideMethodLookup : 
								case insideMethodTable : 
									processJumps(data,InternalUtils.skipBlank(data,start),end,false);
									break;
								default :
									throw new ContentException("Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside loowkuswitch/tableswitch command body only");
							}
							break;
						case DIR_VERSION	:
							checkLabel(id,false);
							processVersionDir(data,InternalUtils.skipBlank(data,start),end);
							break;
						case DIR_LINE	:
							checkLabel(id,false);
							processLineDir(lineNo,data,InternalUtils.skipBlank(data,start),end);
							break;
						case DIR_SOURCE	:
							checkLabel(id,false);
							processSourceDir(data,InternalUtils.skipBlank(data,start),end);
							break;
						default :
							throw new ContentException("Unknown directive : "+new String(data,startDir-1,endDir-startDir+1));
					}
					break;
				case '*' :
					switch (state) {
						case insideClassBody : 
						case insideBegin : 
							endDir = start = skipSimpleName(data,start+1);
			
							switch ((int)staticDirectiveTree.seekName(data,startDir,endDir)) {
								case CMD_LOAD	: processLoadAuto(data,InternalUtils.skipBlank(data,start)); break;
								case CMD_STORE	: processStoreAuto(data,InternalUtils.skipBlank(data,start)); break;
								case CMD_EVAL	: processEvalAuto(data,InternalUtils.skipBlank(data,start)); break;
								case CMD_CALL	: processCallAuto(data,InternalUtils.skipBlank(data,start)); break;
								default : throw new ContentException("Unknown automation : "+new String(data,startDir-1,endDir-startDir+1));
							}
							break;
						default :
							throw new ContentException("Automation can be used on the method body only!");
					}
					break;
				case '\r' : case '\n':
					break;
				default :
					final int	possiblyMacroStart = start, possiblyMacroEnd = skipSimpleName(data,start);
					final long	macroId;
					
					if (possiblyMacroEnd > possiblyMacroStart && (macroId = macros.seekName(data,possiblyMacroStart,possiblyMacroEnd)) >= 0) {	// Process macro call
						final Macros	m = macros.getCargo(macroId);
						
						try(final LineByLineProcessor	lbl = new LineByLineProcessor(diagnostics != null && PureLibSettings.instance().getProperty(PureLibSettings.PRINT_EXPANDED_MACROS,boolean.class,"false") 
																? new LineByLineProcessorCallback(){
																		@Override
																		public void processLine(long displacement, int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
																			diagnostics.write("\t> "+new String(data,from,length));
																			try{LineParser.this.processLine(displacement,lineNo, data, from, len);
																			} catch (Exception  exc) {
																				diagnostics.flush();
																				throw exc;
																			}
																		}
																	}  
																: this);
							final Reader	rdr = m.processCall(lineNo,data,possiblyMacroEnd,len-(possiblyMacroEnd-from)+1)) {

							lbl.write(rdr);
						}
						catch (IOException | SyntaxException exc) {
							if (diagnostics != null) {
								diagnostics.flush();
							}
							exc.printStackTrace();
						}
						return;
					}
					
					switch (state) {
						case insideClassBody : 
						case insideBegin : 
							endDir = start = skipSimpleName(data,start);
							
							if (startDir == endDir) {
								throw new ContentException("Unknown command");
							}
							final long	opCode = staticCommandTree.seekName(data,startDir,endDir);
		
							if (opCode >= 0) {
								final CommandDescriptor	desc = staticCommandTree.getCargo(opCode);
								
								if (addLines2Class) {
									methodDescriptor.addLineNoRecord(lineNo);
								}
								start = InternalUtils.skipBlank(data,start);
								switch (desc.commandFormat) {
									case single					: processSingleCommand(desc,data,start); break; 
									case byteIndex				: processByteIndexCommand(desc,data,start,false); break;
									case extendableByteIndex	: processByteIndexCommand(desc,data,start,true); break;
									case byteValue				: processByteValueCommand(desc,data,start,false); break;
									case shortValue				: processShortValueCommand(desc,data,start,false); break;
									case byteType				: processByteTypeCommand(desc,data,start); break;
									case byteIndexAndByteValue	: processByteIndexAndByteValueCommand(desc,data,start,true); break;
									case shortIndexAndByteValue	: processShortIndexAndByteValueCommand(desc,data,start); break;
									case classShortIndex		: processClassShortIndexCommand(desc,data,start); break;
									case valueByteIndex			: processValueByteIndexCommand(desc,data,start); break;
									case valueShortIndex		: processValueShortIndexCommand(desc,data,start); break;
									case valueShortIndex2		: processValueShortIndex2Command(desc,data,start); break;
									case shortBrunch			: processShortBrunchCommand(desc,data,start,end); break;
									case longBrunch				: processLongBrunchCommand(desc,data,start,end); break;
									case shortGlobalIndex		: processShortGlobalIndexCommand(desc,data,start,end); break;
									case call					: processCallCommand(desc,data,start,end); break;
									case callDynamic			: processDynamicCallCommand(desc,data,start,end); break;
									case callInterface			: processInterfaceCallCommand(desc,data,start,end); break;
									case lookupSwitch			: switchAddress = getPC(); putCommand((byte)opCode); alignPC(); jumps.clear(); state = ParserState.insideMethodLookup; break;
									case tableSwitch			: switchAddress = getPC(); putCommand((byte)opCode); alignPC(); jumps.clear(); state = ParserState.insideMethodTable; break;
									case restricted				: throw new ContentException("Restricted command in the input stream!");
									default : throw new UnsupportedOperationException("Command format ["+desc.commandFormat+"] is not supported yet");
								}
							}
							else {
								throw new ContentException("Unknown command : "+new String(data,startDir,endDir-startDir));
							}
							break;
						case insideMethodLookup : 
						case insideMethodTable : 
							processJumps(data,startDir,end,true);
							break;
						default :
							if (data[start] > ' ') {
								endDir = skipSimpleName(data,start);

								if (endDir == start) {
									throw new SyntaxException(lineNo,0,"Unparsed line in the input: ["+new String(data,from,len-1)+"] is unknown or illegal for the ["+state+"]"); 
								}
								else {
									if (staticCommandTree.seekName(data,start,endDir) >= 0) {
										throw new SyntaxException(lineNo,0,"Valid command outside context. Check that .stack or .begin directive was typed earlier"); 
									}
									else {
										throw new SyntaxException(lineNo,0,"Unparsed line in the input: ["+new String(data,from,len-1)+"] is unknown or illegal for the ["+state+"]"); 
									}
								}
							}
					}
			}
		} catch (SyntaxException exc) {
			exc.printStackTrace();
			throw new IOException(exc.getLocalizedMessage(),exc);
		} catch (ContentException exc) {
			exc.printStackTrace();
			final SyntaxException	synt = new SyntaxException(lineNo,start-from,new String(data,from,len)+exc.getMessage(),exc);
			throw new IOException(synt.getLocalizedMessage(),synt);
		}
	}

	private void checkLabel(final long labelId, final boolean present) throws ContentException {
		if (present) {
			if (labelId == -1) {
				throw new ContentException("Missing mandatory name before directive!");
			}
		}
		else {
			if (labelId != -1) {
				throw new ContentException("Name before this directive is not supported!");
			}
		}
	}
	
	private void putLabel(final long labelId) throws ContentException, IOException {
		methodDescriptor.getBody().putLabel(labelId);
	}

	private int getPC() throws ContentException, IOException {
		return methodDescriptor.getBody().getPC();
	}
	
	private void putCommand(final byte... command) throws ContentException, IOException {
		methodDescriptor.getBody().putCommand(1,command);
	}

	private void alignPC() throws ContentException, IOException {
		methodDescriptor.getBody().alignPC();
	}

	private void registerBranch(final long forResult, final boolean shortBrunch) throws ContentException, IOException {
		methodDescriptor.getBody().registerBrunch(forResult,shortBrunch);
	}

	private void registerBranch(final int address, final int placement, final long forResult, final boolean shortBrunch) throws ContentException, IOException {
		methodDescriptor.getBody().registerBrunch(address,placement,forResult,shortBrunch);
	}
	
	/*
	 * Process directives
	 */
	private void processClassDir(final long id, final char[] data, int start) throws IOException, ContentException {
		int				startOption = start, endOption;
		long			extendsId = -1;
		List<Long>		implementsNames = null;
		
		while (data[start] != '\n') {
			endOption = start = skipSimpleName(data,start);
			switch ((int)staticDirectiveTree.seekName(data,startOption,endOption)) {
				case OPTION_PUBLIC		: classFlags = addAndCheckDuplicates(classFlags,Constants.ACC_PUBLIC,"public","class"); break;
				case OPTION_FINAL		: classFlags = addAndCheckDuplicates(classFlags,Constants.ACC_FINAL,"final","class"); break;
				case OPTION_ABSTRACT	: classFlags = addAndCheckDuplicates(classFlags,Constants.ACC_ABSTRACT,"abstract","class"); break;
				case OPTION_SYNTHETIC	: classFlags = addAndCheckDuplicates(classFlags,Constants.ACC_SYNTHETIC,"synthetic","class"); break;
				case OPTION_EXTENDS		:
					if (extendsId != -1) {
						throw new ContentException("Duplicate option 'extends' in the 'class' directive!");
					}
					else {
						int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedName(data,start);
						
						final Class<?>	parent = cdr.getClassDescription(data,startName,endName);

						if ((parent.getModifiers() & Constants.ACC_FINAL) != 0) {
							throw new ContentException("Attempt to extends final class ["+new String(data,startName,endName-startName)+"]!"); 
						}
						else {
							extendsId = tree.placeOrChangeName(parent.getName(),new NameDescriptor());
						}
					}
					break;
				case OPTION_IMPLEMENTS	:
					if (implementsNames != null) {
						throw new ContentException("Duplicate option 'implements' in the 'class' directive!");
					}
					else {
						implementsNames = new ArrayList<>();
						
						do {start++;
							int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedName(data,start);

							final Class<?>	member = cdr.getClassDescription(data,startName,endName);

							if ((member.getModifiers() & Constants.ACC_INTERFACE) == 0) {
								throw new ContentException("Implements item ["+new String(data,startName,endName-startName)+"] references to the class, not interface!"); 
							}
							else {
								implementsNames.add(tree.placeOrChangeName(member.getName(),new NameDescriptor()));
							}
							start = InternalUtils.skipBlank(data,start);
						} while(data[start] == ',');
					}
					break;
				default :
					throw new ContentException("'Class' definition contains unknown or unsupported option ["+new String(data,startOption,endOption)+"]!");
			}
			startOption = start = InternalUtils.skipBlank(data,start);
		}
		
		if  (checkMutualPrivateProtectedPublic(classFlags)) {
			throw new ContentException("Mutually exclusive options (public/protected/private) in the 'class' directive!");
		}
		else if (checkMutualAbstractFinal(classFlags)) {
			throw new ContentException("Mutually exclusive options (abstract/final) in the 'class' directive!");
		}
		else {
			classNameId = id;
			joinedClassNameId = cc.setClassName(classFlags,packageId,id);
			if (extendsId != -1) {
				cc.setExtendsClassName(extendsId);
			}
			if (implementsNames != null) {
				for (Long item : implementsNames) {
					cc.addInterfaceName(item);
				}
			}
		}
	}

	private void processInterfaceDir(final long id, final char[] data, int start) throws IOException, ContentException {
		int				startOption = start, endOption;
		List<Long>		implementsNames = null;

		classFlags |= Constants.ACC_INTERFACE | Constants.ACC_ABSTRACT;
		while (data[start] != '\n') {
			endOption = start = skipSimpleName(data,start);
			switch ((int)staticDirectiveTree.seekName(data,startOption,endOption)) {
				case OPTION_PUBLIC		: classFlags = addAndCheckDuplicates(classFlags,Constants.ACC_PUBLIC,"public","class"); break;
				case OPTION_SYNTHETIC	: classFlags = addAndCheckDuplicates(classFlags,Constants.ACC_SYNTHETIC,"synthetic","class"); break;
				case OPTION_EXTENDS	:
					if (implementsNames != null) {
						throw new ContentException("Duplicate option 'implements' in the 'class' directive!");
					}
					else {
						implementsNames = new ArrayList<>();
						
						do {start++;
							int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedName(data,start);

							final Class<?>	parent = cdr.getClassDescription(data,startName,endName);

							if ((parent.getModifiers() & Constants.ACC_INTERFACE) == 0) {
								throw new ContentException("Extends item ["+new String(data,startName,endName-startName)+"] references to the class, not interface!"); 
							}
							else {
								implementsNames.add(tree.placeOrChangeName(parent.getName(),new NameDescriptor()));
							}
							start = InternalUtils.skipBlank(data,start);
						} while(data[start] == ',');
					}
					break;
				default :
					throw new ContentException("'Interface' definition contains unknown or unsupported option ["+new String(data,startOption,endOption-startOption)+"]!");
			}
			startOption = start = InternalUtils.skipBlank(data,start);
		}
		classNameId = id;
		classFlags |= Constants.ACC_PUBLIC;
		cc.setClassName(classFlags,packageId,id);
		if (implementsNames != null) {
			for (Long item : implementsNames) {
				cc.addInterfaceName(item);
			}
		}
	}
	
	private void processClassFieldDir(final long id, final char[] data, int start) throws IOException, ContentException {
		final int			startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>		type = cdr.getClassDescription(data,startName,endName);
		
		if (type == void.class) {
			throw new ContentException("Type 'void' is invalid for using with fields"); 
		}
		else {
			final long		typeId = tree.placeOrChangeName(InternalUtils.buildFieldSignature(tree,tree.placeOrChangeName(type.getName(),new NameDescriptor())),new NameDescriptor());
			
			start = processOptions(data,InternalUtils.skipBlank(data,start),forEntity,"field",cdr,false,OPTION_PUBLIC,OPTION_PROTECTED,OPTION_PRIVATE,OPTION_STATIC,OPTION_FINAL,OPTION_VOLATILE,OPTION_TRANSIENT,OPTION_SYNTHETIC);
			start = InternalUtils.skipBlank(data,start);
			
			if (data[start] == '=') {	// Initial values. 
				if ((forEntity.options & OPTION_STATIC) == 0) {
					throw new ContentException("Initial values can be typed for static fields only!"); 
				}
				else if (!type.isPrimitive() && type != String.class) {
					throw new ContentException("Initial values can be typed for primitive types and string only!"); 
				}
				else {
					final short		valueId;
					
					start = InternalUtils.skipBlank(data,start+1);
					switch (CompilerUtils.defineClassType(type)) {
						case CompilerUtils.CLASSTYPE_REFERENCE	:
							if (data[start] != '\"') {
								throw new ContentException("Illegal initial value for String!"); 
							}
							else {
								final int[]		places = new int[2];
								
								if ((UnsafedCharUtils.uncheckedParseUnescapedString(data,start+1,'\"',true,places)) < 0) {
									final StringBuilder	sb = new StringBuilder();
									
									UnsafedCharUtils.uncheckedParseStringExtended(data,start+1,'\"',sb);
									valueId = cc.getConstantPool().asStringDescription(cc.getNameTree().placeOrChangeName(sb.toString(),new NameDescriptor()));
								}
								else {
									valueId = cc.getConstantPool().asStringDescription(cc.getNameTree().placeOrChangeName(data,places[0],places[1]-places[0],new NameDescriptor()));
								}
							}							
							break;
						case CompilerUtils.CLASSTYPE_BOOLEAN :
							if (UnsafedCharUtils.uncheckedCompare(data,start,TRUE,0,TRUE.length)) {
								valueId = cc.getConstantPool().asIntegerDescription(1); 
							}
							else if (UnsafedCharUtils.uncheckedCompare(data,start,FALSE,0,FALSE.length)) {
								valueId = cc.getConstantPool().asIntegerDescription(0); 
							}
							else {
								throw new ContentException("Illegal initial value for boolean!"); 
							}
							break;
						case CompilerUtils.CLASSTYPE_BYTE : case CompilerUtils.CLASSTYPE_SHORT : case CompilerUtils.CLASSTYPE_CHAR : case CompilerUtils.CLASSTYPE_INT :
							final int[]		intValues = new int[1];
							
							UnsafedCharUtils.uncheckedParseSignedInt(data,start,intValues,true);
							valueId = cc.getConstantPool().asIntegerDescription(intValues[0]);
							break;
						case CompilerUtils.CLASSTYPE_FLOAT	:
							final float[]		floatValues = new float[1];
							
							CharUtils.parseSignedFloat(data,start,floatValues,true);
							valueId = cc.getConstantPool().asFloatDescription(floatValues[0]);
							break;
						case CompilerUtils.CLASSTYPE_LONG	:
							final long[]		longValues = new long[1];
							
							UnsafedCharUtils.uncheckedParseSignedLong(data,start,longValues,true);
							valueId = cc.getConstantPool().asLongDescription(longValues[0]);
							break;
						case CompilerUtils.CLASSTYPE_DOUBLE	:
							final double[]		doubleValues = new double[1];
							
							UnsafedCharUtils.uncheckedParseSignedDouble(data,start,doubleValues,true);
							valueId = cc.getConstantPool().asDoubleDescription(doubleValues[0]);
							break;
						default :
							throw new UnsupportedOperationException();
					}
					cc.addFieldDescription(forEntity.options,id,typeId,(short)0);
				}
			}
			else {
				cc.addFieldDescription(forEntity.options,id,typeId);
			}
			cc.getConstantPool().asFieldRefDescription(joinedClassNameId,id,typeId);

			final int		classNameLen = tree.getNameLength(joinedClassNameId), fieldLen = tree.getNameLength(id);
			final char[]	forLongName = new char[classNameLen+1+fieldLen];
			
			tree.getName(joinedClassNameId,forLongName,0);
			forLongName[classNameLen] = '.';
			tree.getName(id,forLongName,classNameLen+1);
			tree.placeName(forLongName,0,forLongName.length,new NameDescriptor());
			
		}
	}

	private void processInterfaceFieldDir(final long id, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		
		if (type == void.class) {
			throw new ContentException("Type 'void' is invalid for using with fields"); 
		}
		else {
			final long	typeId = tree.placeOrChangeName(cdr.getClassDescription(data,startName,endName).getName(),new NameDescriptor());
			
			start = processOptions(data,InternalUtils.skipBlank(data,start),forEntity,"field",cdr,false,OPTION_PUBLIC,OPTION_STATIC,OPTION_FINAL);
			start = InternalUtils.skipBlank(data,start);
			
			if (data[start] == '=') {	// Initial values.
				if (!type.isPrimitive() && type != String.class) {
					throw new ContentException("Initial values can be typed for primitive types and string only!"); 
				}
				else {
					final short		valueId;
					
					start = InternalUtils.skipBlank(data,start+1);
					switch (CompilerUtils.defineClassType(type)) {
						case CompilerUtils.CLASSTYPE_REFERENCE	:
							if (data[start] != '\"') {
								throw new ContentException("Illegal initial value for String!"); 
							}
							else {
								final int[]		places = new int[2];
								
								if ((UnsafedCharUtils.uncheckedParseUnescapedString(data,start+1,'\"',true,places)) < 0) {
									final StringBuilder	sb = new StringBuilder();
									
									UnsafedCharUtils.uncheckedParseStringExtended(data,start+1,'\"',sb);
									valueId = cc.getConstantPool().asStringDescription(cc.getNameTree().placeOrChangeName(sb.toString(),new NameDescriptor()));
								}
								else {
									valueId = cc.getConstantPool().asStringDescription(cc.getNameTree().placeOrChangeName(data,places[0],places[1]-places[0],new NameDescriptor()));
								}
							}							
							break;
						case CompilerUtils.CLASSTYPE_BOOLEAN :
							if (UnsafedCharUtils.uncheckedCompare(data,start,TRUE,0,TRUE.length)) {
								valueId = cc.getConstantPool().asIntegerDescription(1); 
							}
							else if (UnsafedCharUtils.uncheckedCompare(data,start,FALSE,0,FALSE.length)) {
								valueId = cc.getConstantPool().asIntegerDescription(0); 
							}
							else {
								throw new ContentException("Illegal initial value for boolean!"); 
							}
							break;
						case CompilerUtils.CLASSTYPE_BYTE : case CompilerUtils.CLASSTYPE_SHORT : case CompilerUtils.CLASSTYPE_CHAR : case CompilerUtils.CLASSTYPE_INT :
							final int[]		intValues = new int[1];
							
							UnsafedCharUtils.uncheckedParseSignedInt(data,start,intValues,true);
							valueId = cc.getConstantPool().asIntegerDescription(intValues[0]);
							break;
						case CompilerUtils.CLASSTYPE_FLOAT	:
							final float[]		floatValues = new float[1];
							
							CharUtils.parseSignedFloat(data,start,floatValues,true);
							valueId = cc.getConstantPool().asFloatDescription(floatValues[0]);
							break;
						case CompilerUtils.CLASSTYPE_LONG	:
							final long[]		longValues = new long[1];
							
							UnsafedCharUtils.uncheckedParseSignedLong(data,start,longValues,true);
							valueId = cc.getConstantPool().asLongDescription(longValues[0]);
							break;
						case CompilerUtils.CLASSTYPE_DOUBLE	:
							final double[]		doubleValues = new double[1];
							
							UnsafedCharUtils.uncheckedParseSignedDouble(data,start,doubleValues,true);
							valueId = cc.getConstantPool().asDoubleDescription(doubleValues[0]);
							break;
						default :
							throw new UnsupportedOperationException();
					}
					cc.addFieldDescription((short)(forEntity.options| Constants.ACC_PUBLIC | Constants.ACC_STATIC | Constants.ACC_FINAL),id,typeId,valueId);
				}
			}
			else {
				cc.addFieldDescription((short)(forEntity.options| Constants.ACC_PUBLIC | Constants.ACC_STATIC | Constants.ACC_FINAL),id,typeId);
			}
		}
	}
	
	private void processClassMethodDir(final long id, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		
		if (startName == endName) {
			throw new ContentException("Required return type for the method/constructor is missing!");
		}
		else {
			final Class<?>	type = cdr.getClassDescription(data,startName,endName);
			final long		typeId = tree.placeOrChangeName(type.getName(),new NameDescriptor());
			
			start = processOptions(data,InternalUtils.skipBlank(data,start),forEntity,"method",cdr,false,OPTION_PUBLIC,OPTION_PROTECTED,OPTION_PRIVATE,OPTION_STATIC,OPTION_FINAL,OPTION_SYNCHRONIZED,OPTION_BRIDGE,OPTION_VARARGS,OPTION_NATIVE,OPTION_ABSTRACT,OPTION_SYNTHETIC,OPTION_THROWS);
			if ((classFlags & Constants.ACC_ABSTRACT) == 0 && (forEntity.options & Constants.ACC_ABSTRACT) != 0) {
				throw new ContentException("Attempt to add abstract method to the non-abstract class!");
			}
			else {
				if (id == classNameId) {	// This is a constructor!
					if (typeId != voidId) {
						throw new ContentException("Constructor method need return void type!");
					}
					else if ((forEntity.options & Constants.ACC_STATIC) != 0) {	// This is a <clinit>
						if (classConstructorCount == 0) {
							methodNameId = classConstructorId;
							classConstructorCount++;
						}
						else {
							throw new ContentException("Class can't have more than one <clinit> methods!");
						}
					}
					else {
						methodNameId = constructorId;
					}
				}
				else {
					methodNameId = id;
				}
				if (forEntity.throwsList.size() > 0) {
					final long[] 	throwsList = new long[forEntity.throwsList.size()];
					
					for (int index = 0; index < throwsList.length; index++) {
						throwsList[index] = tree.placeOrChangeName(forEntity.throwsList.get(index).getName(),new NameDescriptor());
					}
					methodDescriptor = cc.addMethodDescription(forEntity.options,methodNameId,typeId,throwsList);
				}
				else {
					methodDescriptor = cc.addMethodDescription(forEntity.options,methodNameId,typeId);
				}
			}
		}
	}
	
	private void processInterfaceMethodDir(final long id, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		final long		typeId = tree.placeOrChangeName(type.getName(),new NameDescriptor());
		
		start = processOptions(data,InternalUtils.skipBlank(data,start),forEntity,"method",cdr,false,OPTION_PUBLIC,OPTION_STATIC,OPTION_FINAL,OPTION_SYNCHRONIZED,OPTION_BRIDGE,OPTION_VARARGS,OPTION_NATIVE,OPTION_ABSTRACT,OPTION_SYNTHETIC,OPTION_THROWS);
		
		forEntity.options |= Constants.ACC_ABSTRACT | Constants.ACC_PUBLIC; 
		if (forEntity.throwsList.size() > 0) {
			final long[] 	throwsList = new long[forEntity.throwsList.size()];
			
			for (int index = 0; index < throwsList.length; index++) {
				throwsList[index] = tree.placeOrChangeName(forEntity.throwsList.get(index).getName(),new NameDescriptor());
			}
			methodDescriptor = cc.addMethodDescription(forEntity.options,id,typeId,throwsList);
		}
		else {
			methodDescriptor = cc.addMethodDescription(forEntity.options,id,typeId);
		}
		methodNameId = id;
	}

	private void processParameterDir(final long id, final char[] data, int start) throws ContentException {
		start = extractClassWithPossibleArray(data,start, cdr, forClass);

		if (methodNameId == classConstructorId) {
			throw new ContentException("Class initialization method <clinit> should not have any parameters!"); 
		}
		else if (forClass[0] == void.class) {
			throw new ContentException("Type 'void' is invalid for using with parameters"); 
		}
		else {
			final long	typeId = tree.placeOrChangeName(toCanonicalName(forClass[0]),new NameDescriptor());
			
			start = processOptions(data,InternalUtils.skipBlank(data,start),forEntity,"parameter",cdr,false,OPTION_FINAL,OPTION_SYNTHETIC);
			methodDescriptor.addParameterDeclaration(forEntity.options,id,typeId);
		}
	}

	private void processVarDir(final long id, final char[] data, int start) throws IOException, ContentException {
		start = extractClassWithPossibleArray(data,start, cdr, forClass);
		
		if (forClass[0] == void.class) {
			throw new ContentException("Type 'void' is invalid for using with parameters"); 
		}
		else {
			final long	typeId = tree.placeOrChangeName(toCanonicalName(forClass[0]),new NameDescriptor());
		
			start = processOptions(data,InternalUtils.skipBlank(data,start),forEntity,"var",cdr,false,OPTION_FINAL,OPTION_SYNTHETIC);
			methodDescriptor.addVarDeclaration(forEntity.options,id,typeId);
		}
	}

	private void processStackDir(final char[] data, int start) throws ContentException {
		final  int	startName = start, endName = start = skipSimpleName(data, start);
		
		switch ((int)staticDirectiveTree.seekName(data,startName,endName)) {
			case STACK_OPTIMISTIC :
				methodDescriptor.setStackSize(MethodBody.STACK_CALCULATION_OPTIMISTIC);
				break;
			case STACK_PESSIMISTIC :
				methodDescriptor.setStackSize(MethodBody.STACK_CALCULATION_PESSIMISTIC);
				break;
			default :
				final long[]	size = new long[]{0,0};
				
				try{start = UnsafedCharUtils.uncheckedParseNumber(data,startName,size,CharUtils.PREF_INT,true);
					if (size[1] == CharUtils.PREF_INT) {
						methodDescriptor.setStackSize((short) size[0]);
					}
					else {
						throw new ContentException("Stack size is neither integer constant nor 'optimistic'/'pessimistic' (possibly it's size is long, float or double)");
					}
				} catch (NumberFormatException exc) {
					throw new ContentException("Stack size is neither integer constant nor 'optimistic'/'pessimistic' (possibly it's size is long, float or double)");
				}
				break;
		}
		skip2line(data,start);
	}
	
	private void processPackageDir(final char[] data, int start) throws ContentException {
		final int	endPackage = skipQualifiedName(data,start);
			
		if (start == endPackage) {
			throw new ContentException("Package name is missing!");
		}
		else {
			packageId = tree.placeOrChangeName(data,start,endPackage,new NameDescriptor());
			skip2line(data,endPackage);
		}
	}

	private void processImportDir(final char[] data, int start) throws ContentException {
		final int		endName = skipQualifiedName(data,start);
		final String	className = new String(data,start,endName - start);  

		try{final int	possibleProtected = InternalUtils.skipBlank(data,endName);
			
			if (UnsafedCharUtils.uncheckedCompare(data,possibleProtected,PROTECTED_KEYWORD,0,PROTECTED_KEYWORD.length)) {
				cdr.addDescription(loader != null ? Class.forName(className,true,loader) : Class.forName(className),true);
				start = possibleProtected + PROTECTED_KEYWORD.length;
			}
			else {
				cdr.addDescription(loader != null ? Class.forName(className,true,loader) : Class.forName(className),false);
				start = endName;
			}
			skip2line(data,start);
		} catch (ClassNotFoundException e) {
			throw new ContentException("Class description ["+className+"] is unknown in the actual class loader. Test the class name you want to import and/or make it aacessible for the class loader",e);
		}				
	}

	private void processIncludeDir(final char[] data, int start, final int end) throws ContentException {
		if (start < end && data[start] == '\"') {
			int	startQuoted = start + 1;
			
			start = skipQuoted(data,startQuoted,'\"');
			skip2line(data,start+1);
			
			final String	ref = new String(data,startQuoted,start-startQuoted-1);
			URL		refUrl;
			
			try{refUrl = new URL(ref);
			} catch (MalformedURLException exc) {
				refUrl = this.getClass().getResource(ref);
			}
			if (refUrl == null) {
				throw new ContentException("Resource URL ["+ref+"] is missing or malformed!");
			}
			else {
				int lineNo = 0;
				
				try(final LineByLineProcessor	lbl = new LineByLineProcessor(this);
					final InputStream		is = refUrl.openStream();
					final Reader			rdr = new InputStreamReader(is)) {

					lbl.write(rdr);
				}
				catch (IOException | SyntaxException exc) {
					throw new ContentException("Source ["+refUrl+"], line "+lineNo+": I/O error reading data ("+exc.getMessage()+")");
				}
			}
		}
		else {
			throw new ContentException("Missing quota!");
		}
	}

	private void processVersionDir(final char[] data, int start, final int end) throws ContentException {
		int		parm[] = new int[1], from = start, major = 0, minor = 0;
		
		if (start < end && data[start] >= '0' && data[start] <= '9') {
			start = UnsafedCharUtils.uncheckedParseInt(data,from,parm,true);
			major = parm[0];
			if (data[start] == '.') {
				UnsafedCharUtils.uncheckedParseInt(data,from = start + 1,parm,true);
				minor = parm[0];
			}
			if (major == 1 && minor == 7) {
				cc.changeClassFormatVersion(Constants.MAJOR_1_7,Constants.MINOR_1_7);
			}
			else if (major == 1 && minor == 8) {
				cc.changeClassFormatVersion(Constants.MAJOR_1_8,Constants.MINOR_1_8);
			}
			else {
				throw new ContentException("Version number "+major+"."+minor+" is not supported. Only 1.7 and 1.8 are available!");
			}
		}
		else {
			throw new ContentException("Missing version number!");
		}
	}
	
	private void processLineDir(final int lineNo, final char[] data, int start, final int end) throws ContentException, IOException {
		int		parm[] = new int[1], from = start;
		
		if (start < end && data[start] >= '0' && data[start] <= '9') {
			UnsafedCharUtils.uncheckedParseInt(data,start,parm,true);
			if (addLines2ClassManually) {
				if (state == ParserState.insideClassBody || state == ParserState.insideBegin) {
					methodDescriptor.addLineNoRecord(parm[0]);
				}
				else {
					throw new ContentException(".lines <NNN> command is available inside the method body only!");
				}
			}
			else {
				throw new ContentException("To control lines manually, set '.lines manual' firstly!");
			}
		}
		else if (Character.isLetter(data[start])) {
			start = skipSimpleName(data,from);
			
			switch ((int)staticDirectiveTree.seekName(data,from,start)) {
				case LINE_NONE	:
					addLines2Class = false;
					addLines2ClassManually = false;
					break;
				case LINE_AUTO	:
					addLines2Class = true;
					addLines2ClassManually = false;
					break;
				case LINE_MANUAL:
					addLines2Class = true;
					addLines2ClassManually = true;
					break;
				default :
					throw new ContentException("Unknown option in pseudocommant. Only (none, auto, manual) area available!");
			}
		}
		else {
			throw new ContentException("Missing line number or one of the options (none, auto, manual)!");
		}
	}

	private void processSourceDir(final char[] data, int start, final int end) throws ContentException {
		int		from = start;
		
		if (data[start] == '\"') {
			start = skipQuoted(data,start+1,'\"');
			skip2line(data,start+1);
			
			final String	ref = new String(data,from+1,start-from);
			URL		refUrl;
			
			try{refUrl = new URL(ref);
			} catch (MalformedURLException exc) {
				refUrl = this.getClass().getResource(ref);
			}
			if (refUrl == null) {
				throw new ContentException("Resource URL ["+ref+"] is missing or malformed!");
			}
			else {
				cc.setSourceAttribute(refUrl);
			}
		}
		else {
			throw new ContentException("Missing quoted source URL!");
		}
	}
	
	/*
	 * Process try blocks
	 */
	
	private boolean areTryBlocksClosed() {
		return tryList.size() == 0;
	}

	private void pushTryBlock() throws IOException, ContentException {
		tryList.add(0,new int[]{getPC(),-1});
	}

	private void processCatch(final char[] data, int from, final int to) throws IOException, ContentException {
		if (tryList.size() == 0) {
			throw new ContentException(".catch without .try");
		}
		else {
			if (tryList.get(0)[1] == -1) {
				tryList.get(0)[1] = getPC();
			}
			if (Character.isJavaIdentifierStart(data[from])) {
				from--;
				do {final int		startException = from+1, endException = skipQualifiedName(data,startException);
					final Class<?>	exception = cdr.getClassDescription(data,startException,endException);
					final long		exceptionId = tree.placeOrChangeName(exception.getName().replace('.','/'),new NameDescriptor());

					methodDescriptor.addExceptionRecord((short)tryList.get(0)[0],(short)tryList.get(0)[1],cc.getConstantPool().asClassDescription(exceptionId),(short)getPC());
					from = InternalUtils.skipBlank(data,endException);
				} while (data[from] == ',');
			}
			else {
				methodDescriptor.addExceptionRecord((short)tryList.get(0)[0],(short)tryList.get(0)[1],(short)0,(short)getPC());
			}
			skip2line(data, from);
		}
	}

	private void popTryBlock(int lineNo) throws ContentException {
		if (tryList.size() == 0) {
			throw new ContentException(".endtry without .try");
		}
		else {
			tryList.remove(0);
		}
	}
	
	/*
	 * Process commands
	 */
	
	private void processSingleCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		putCommand((byte)desc.operation);
		skip2line(data,start);
	}

	private void processByteIndexCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandAddress) throws IOException, ContentException {
		final long	forResult[] = new long[2];
		
		start = calculateLocalAddress(data,start,forResult);
		if (forResult[0] < 0 || forResult[0] > methodDescriptor.getLocalFrameSize()) {
			throw new ContentException("Calculated address value ["+forResult[0]+"] is outside the method local frame size ["+methodDescriptor.getLocalFrameSize()+"]");
		}
		else if (forResult[0] >= 256) {
			if (!expandAddress) {
				throw new ContentException("Calculated address value ["+forResult[0]+"] occupies more than 1 byte");
			}
			else {
				putCommand((byte)extractStaticCommand("wide").operation,(byte)desc.operation,(byte)(forResult[0] >> 8),(byte)forResult[0]);
			}
		}
		else {
			if (forResult[0] <= 3) {	// Optimization to use short forms of the byte code command
				switch (staticCommandTree.getName(desc.operation)) {
					case "aload" 	: putCommand((byte)extractStaticCommand(ALOAD_SPECIAL[(int)forResult[0]]).operation); break;
					case "astore"	: putCommand((byte)extractStaticCommand(ASTORE_SPECIAL[(int)forResult[0]]).operation); break;
					case "dload"	: putCommand((byte)extractStaticCommand(DLOAD_SPECIAL[(int)forResult[0]]).operation); break;
					case "dstore"	: putCommand((byte)extractStaticCommand(DSTORE_SPECIAL[(int)forResult[0]]).operation); break;
					case "fload"	: putCommand((byte)extractStaticCommand(FLOAD_SPECIAL[(int)forResult[0]]).operation); break;
					case "fstore"	: putCommand((byte)extractStaticCommand(FSTORE_SPECIAL[(int)forResult[0]]).operation); break;
					case "iload"	: putCommand((byte)extractStaticCommand(ILOAD_SPECIAL[(int)forResult[0]]).operation); break;
					case "istore"	: putCommand((byte)extractStaticCommand(ISTORE_SPECIAL[(int)forResult[0]]).operation); break;
					case "lload"	: putCommand((byte)extractStaticCommand(LLOAD_SPECIAL[(int)forResult[0]]).operation); break;
					case "lstore"	: putCommand((byte)extractStaticCommand(LSTORE_SPECIAL[(int)forResult[0]]).operation); break;
					default : throw new UnsupportedOperationException("Special command ["+staticCommandTree.getName(desc.operation)+"] is not supprted yet");
				}
			}
			else {
				putCommand((byte)desc.operation,(byte)forResult[0]);
			}
		}
		skip2line(data,start);
	}

	private void processByteValueCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandValue) throws IOException, ContentException {
		final long	forResult[] = new long[2];
		
		start = calculateValue(data,start,EvalState.additional,forResult);
		if (forResult[0] < Byte.MIN_VALUE || forResult[0] >= 256) {
			if (!expandValue) {
				throw new ContentException("Calculated value value ["+forResult[0]+"] occupies more than 1 byte!");
			}
			else {
				putCommand((byte)desc.operation,(byte)(forResult[0] >> 8),(byte)forResult[0]);
			}
		}
		else {
			if (forResult[0] >= -1 && forResult[0] <= 5) {	// Replace bipush with the special commands
				putCommand((byte)extractStaticCommand(BIPUSH_SPECIAL[(int) (forResult[0]+1)]).operation);
			}
			else {
				putCommand((byte)desc.operation,(byte)forResult[0]);
			}
		}
		skip2line(data,start);
	}

	private void processByteIndexAndByteValueCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandAddress) throws IOException, ContentException {
		final long[]	forIndex= new long[2], forValue = new long[2];
		boolean			needExpand = false;
		
		start = InternalUtils.skipBlank(data, calculateLocalAddress(data,start,forIndex));
		if (data[start] == ',') {
			start = calculateValue(data,InternalUtils.skipBlank(data,start+1),EvalState.additional,forValue);
		}

		if (forIndex[0] > methodDescriptor.getLocalFrameSize()) {
			throw new ContentException("Calculated address value ["+forIndex[0]+"] is outside the method local frame size ["+methodDescriptor.getLocalFrameSize()+"]");
		}
		else if (forIndex[0] >= 256) {
			if (!expandAddress) {
				throw new ContentException("Calculated address value ["+forIndex[0]+"] occupies more than 1 byte! Use 'wide' command for those addresses");
			}
			else {
				needExpand = true;
			}
		}
		if (forValue[0] < Byte.MIN_VALUE || forValue[0] >= 256) {
			if (!expandAddress) {
				throw new ContentException("Calculated value value ["+forValue[0]+"] occupies more than 1 byte! Use 'wide' command for those values");
			}
			else {
				needExpand = true;
			}
		}
		if (needExpand) {
			putCommand((byte)extractStaticCommand("wide").operation,(byte)desc.operation,(byte)(forIndex[0]>>8),(byte)forIndex[0],(byte)(forValue[0]>>8),(byte)forValue[0]);
		}
		else {
			putCommand((byte)desc.operation,(byte)forIndex[0],(byte)forValue[0]);
		}
		skip2line(data,start);
	}
	
	private void processByteTypeCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final int	startType = start, endType = start = skipSimpleName(data,start);
		final long	typeId = staticDirectiveTree.seekName(data,startType,endType);

		if (typeId >= T_BASE && typeId <= T_BASE_END) {
			putCommand((byte)desc.operation,(byte)(typeId-T_BASE));
			skip2line(data,start);
		}
		else {
			throw new ContentException("Unknown primitive type ["+new String(data,startType,endType-startType)+"]"); 
		}
	}

	private void processClassShortIndexCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		
		if (startName == endName) {
			throw new ContentException("Required class name is missing in the command parameter");
		}
		else {
			try{final Class<?>	type = cdr.getClassDescription(data,startName,endName);
				final long		typeId = tree.placeOrChangeName(type.getName().replace('.','/'),new NameDescriptor());
				final short		typeDispl = cc.getConstantPool().asClassDescription(typeId);
		
				putCommand((byte)desc.operation,(byte)((typeDispl >> 8) & 0xFF),(byte)(typeDispl & 0xFF));
				skip2line(data,start);
			} catch (ContentException exc) {
				final long		typeId = cc.getNameTree().seekName(data,startName,endName);
				
				if (typeId >= 0) {
					final NameDescriptor	nd = cc.getNameTree().getCargo(typeId);
					
					if (nd != null) {
						final short			typeDispl = nd.cpIds[Constants.CONSTANT_Class];
					
						if (typeDispl != Short.MAX_VALUE) {
							putCommand((byte)desc.operation,(byte)((typeDispl >> 8) & 0xFF),(byte)(typeDispl & 0xFF));
							skip2line(data,start);
						}
						else {
							throw exc;
						}
					}
					else {
						throw exc;
					}
				}
				else {
					throw exc;
				}
			}
		}
	}

	private void processValueByteIndexCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		short		displ[] = new short[1];
		
		start = processValueShortIndexCommand(data,start,displ);

		if (displ[0] < 0 || displ[0] > 2*Byte.MAX_VALUE) {
			throw new ContentException("Calculated value ["+displ[0]+"] is too long for byte index");
		}
		else {
			putCommand((byte)desc.operation,(byte)(displ[0] & 0xFF));
			skip2line(data,start);
		}
	}
	
	private void processValueShortIndexCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		short		displ[] = new short[1];
		
		start = processValueShortIndexCommand(data,start,displ);

		if (displ[0] < 0 || displ[0] > 2*Short.MAX_VALUE) {
			throw new ContentException("Calculated value ["+displ[0]+"] is too long for short index");
		}
		else {
			putCommand((byte)desc.operation,(byte)((displ[0] >> 8) & 0xFF),(byte)(displ[0] & 0xFF));
			skip2line(data,start);
		}
	}

	private int processValueShortIndexCommand(final char[] data, int start, final short[] result) throws IOException, ContentException {
		short		displ;

		try{switch (data[start]) {
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' : case '-' :
					final long	forResult[] = new long[]{0,0};
					int			sign = 1;
					
					if (data[start] == '-') {
						sign = -1;
						start++;
					}
					start = UnsafedCharUtils.uncheckedParseNumber(data,start,forResult,CharUtils.PREF_INT|CharUtils.PREF_FLOAT,true);
					
					if (forResult[1] == CharUtils.PREF_FLOAT) {
						displ = cc.getConstantPool().asFloatDescription(sign*Float.intBitsToFloat((int) forResult[0]));
					}
					else if (forResult[1] == CharUtils.PREF_INT) {
						displ = cc.getConstantPool().asIntegerDescription((int)(sign*forResult[0]));
					}
					else {
						throw new ContentException("Illegal numeric constant size (only int and float are available here)");
					}		
					break;
				case '\'' :
					final int	startChar = start + 1, endChar = start = skipQuoted(data,startChar,'\'');
					
					if (endChar != startChar + 1) {
						if (data[startChar] == '\\') {
							char	value = 0;
							
							for (int index = startChar+1; index < endChar; index++) {
								if (data[index] >= '0' && data[index] <= '7') {
									value = (char) (value * 8 + data[index] - '0');
								}
								else {
									throw new ContentException("Illegal escaped char constant content");
								}
							}
							displ = cc.getConstantPool().asIntegerDescription(value);
						}
						else {
							throw new ContentException("Illegal char constant length. Need be exactly one char inside char constant");
						}
					}
					else {
						displ = cc.getConstantPool().asIntegerDescription(data[startChar]);
					}
					start++;
					break;
				case '\"' :
					final int	startString = start + 1, endString = start = skipQuoted(data,startString,'\"');
					
					start++;
					displ = cc.getConstantPool().asStringDescription(cc.getNameTree().placeOrChangeName(data,startString,endString,new NameDescriptor()));
					break;
				case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' : case 'g' : case 'h' :  case 'i' : case 'j' :
				case 'k' : case 'l' : case 'm' : case 'n' : case 'o' : case 'p' : case 'q' : case 'r' :  case 's' : case 't' :
				case 'u' : case 'v' : case 'w' : case 'x' : case 'y' : case 'z' :
				case 'A' : case 'B' : case 'C' : case 'D' : case 'E' : case 'F' : case 'G' : case 'H' :  case 'I' : case 'J' :
				case 'K' : case 'L' : case 'M' : case 'N' : case 'O' : case 'P' : case 'Q' : case 'R' :  case 'S' : case 'T' :
				case 'U' : case 'V' : case 'W' : case 'X' : case 'Y' : case 'Z' :
					final int	startName = start, endName = start = skipQualifiedName(data,startName);
					
					if (UnsafedCharUtils.uncheckedCompare(data,endName-CLASS_SUFFIX.length,CLASS_SUFFIX,0,CLASS_SUFFIX.length)) {
						for (int index = startName; index <= endName; index++) {
							if (data[index] == '.') {
								data[index] = '/';
							}
						}
						displ = cc.getConstantPool().asClassDescription(cc.getNameTree().placeOrChangeName(data,startName,endName-CLASS_SUFFIX.length,new NameDescriptor()));
					}
					else {
						throw new ContentException("Illegal name. Only zzz.class is available here");
					}
					break;
				default :
					throw new ContentException("Illegal lexema. Only int/float constant, string literal, class or method reference are available here");
			}
		} catch (NumberFormatException exc) {
			throw new ContentException("Illegal number: "+exc);
		}
		result[0] = displ;
		return start;
	}
	
	private void processValueShortIndex2Command(final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final long	forResult[] = new long[]{0,0};
		int			sign = 1;
		short		displ;

		try{if (data[start] == '-') {
				sign = -1;
				start++;
			}
			start = UnsafedCharUtils.uncheckedParseNumber(data, start, forResult, CharUtils.PREF_LONG|CharUtils.PREF_DOUBLE,true);
			if (forResult[1] == CharUtils.PREF_DOUBLE) {
				displ = cc.getConstantPool().asDoubleDescription(sign*Double.longBitsToDouble(forResult[0]));
			}
			else if (forResult[1] == CharUtils.PREF_LONG) {
				displ = cc.getConstantPool().asLongDescription(sign*forResult[0]);
			}
			else {
				throw new ContentException("Illegal numeric constant size (only long and double are available here)");
			}
			if (displ < 0 || displ > 2*Short.MAX_VALUE) {
				throw new ContentException("Calculated value ["+displ+"] is too long for short index");
			}
			else {
				putCommand((byte)desc.operation,(byte)((displ >> 8) & 0xFF),(byte)(displ & 0xFF));
				skip2line(data,start);
			}
		} catch (NumberFormatException exc) {
			throw new ContentException("Illegal number: "+exc);
		}
	}
	
	private void processShortValueCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandValue) throws IOException, ContentException {
		final long	forResult[] = new long[1];
		
		start = calculateValue(data,start,EvalState.additional,forResult);
		if (forResult[0] < Short.MIN_VALUE || forResult[0] >= 65536) {
			if (!expandValue) {
				throw new ContentException("Calculated value value ["+forResult[0]+"] occupies more than 2 byte!");
			}
			else {
				putCommand((byte)desc.operation,(byte)(forResult[0] >> 24),(byte)(forResult[0] >> 16),(byte)(forResult[0] >> 8),(byte)forResult[0]);
			}
		}
		else {
			if (forResult[0] >= -1 && forResult[0] <= 5) {	// Replace sipush with the special commands
				putCommand((byte)extractStaticCommand(BIPUSH_SPECIAL[(int)(forResult[0]+1)]).operation);
			}
			else {
				putCommand((byte)desc.operation,(byte)(forResult[0]>>8),(byte)forResult[0]);
			}
		}
		skip2line(data,start);
	}

	private void processShortIndexAndByteValueCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		final long		typeId = tree.placeOrChangeName(type.getName().replace('.','/'),new NameDescriptor());
		final short		typeDispl = cc.getConstantPool().asClassDescription(typeId);
		final long[]	forValue = new long[1];
		
		start = InternalUtils.skipBlank(data,endName);
		if (data[start] == ',') {
			start = calculateValue(data,InternalUtils.skipBlank(data,start+1),EvalState.additional,forValue);
	 		if (forValue[0] <= 0 || forValue[0] >= 2 * Byte.MAX_VALUE) {
				throw new ContentException("Calculated value ["+forValue[0]+"] is too long for byte value");
			}
			else {
				putCommand((byte)desc.operation,(byte)((typeDispl >> 8) & 0xFF),(byte)(typeDispl & 0xFF),(byte)forValue[0]);
				skip2line(data,start);
			}
		}
		else {
			throw new ContentException("Missing comma and dimension parameter");
		}
	}

	private void processShortGlobalIndexCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final int	forResult[] = new int[1]; 
		
		start = calculateFieldAddress(data,start,end,forResult);
		if (forResult[0] <= 0 || forResult[0] > Short.MAX_VALUE) {
			throw new ContentException("Calculated value ["+forResult[0]+"] is too long for short index");
		}
		else {
			putCommand((byte)desc.operation,(byte)((forResult[0] >> 8) & 0xFF),(byte)(forResult[0] & 0xFF));
			skip2line(data,start);
		}
	}

	private void processShortBrunchCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final long	forResult[] = new long[1];
		
		start = calculateBranchAddress(data,start,forResult);
		registerBranch(forResult[0],true);
		putCommand((byte)desc.operation,(byte)0,(byte)0);
		skip2line(data,start);
	}

	private void processLongBrunchCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final long	forResult[] = new long[2];
		
		start = calculateBranchAddress(data,start,forResult);
		registerBranch(forResult[0],false);
		putCommand((byte)desc.operation,(byte)0,(byte)0,(byte)0,(byte)0);
		skip2line(data,start);
	}

	private int processCallCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final int	forResult[] = new int[1], forArgs[] = new int[1];
		
		start = calculateMethodAddress(data,start,end,forResult,forArgs);
		if (forResult[0] <= 0 || forResult[0] > 2*Short.MAX_VALUE) {
			throw new ContentException("Calculated value ["+forResult[0]+"] is too long for short index");
		}
		else {
			putCommand((byte)desc.operation,(byte)((forResult[0] >> 8) & 0xFF),(byte)(forResult[0] & 0xFF));
			skip2line(data,start);
		}
		return forArgs[0];
	}	

	private void processDynamicCallCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		throw new ContentException("Don't use invokedynamic connand! Use direct link to the methods you need instead"); 
	}

	private void processInterfaceCallCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final int	argsSize = processCallCommand(desc,data,start,end)+1;
		putCommand((byte)(argsSize & 0xFF),(byte)((argsSize >> 8)& 0xFF));
	}

	private void processJumps(final char[] data, int start, final int end, final boolean explicitValue) throws ContentException {
		final long[]	forLabel = new long[1];
		final long[]	forValue = new long[1];
		
		if (explicitValue) {
			start = calculateValue(data,start,EvalState.additional,forValue);
			if (start < end && data[start] == ',') {
				start = InternalUtils.skipBlank(data, calculateBranchAddress(data,start+1,forLabel));
			}
			else {
				throw new ContentException("Missing comma in branch list");
			}
		}
		else {
			forValue[0] = DEFAULT_MARK;
			start = InternalUtils.skipBlank(data, calculateBranchAddress(data,start,forLabel));
		}
		if ((forValue[0] < Integer.MIN_VALUE  || forValue[0] > Integer.MAX_VALUE) && forValue[0] != DEFAULT_MARK) {
			throw new ContentException("Calculated value ["+forValue[0]+"] is too long for byte value");
		}
		else {
			if (jumps.containsKey(forValue[0])) {
				throw new ContentException("Duplicate switch value ["+(forValue[0] == DEFAULT_MARK ? "default" : ""+forValue[0])+"] in the lookupswitch/tableswitch command");
			}
			else {
				jumps.put(forValue[0],forLabel[0]);
			}
			skip2line(data,start);
		}
	}

	private void fillTable() throws IOException, ContentException {
		if (jumps.size() == 0) {
			throw new ContentException("No any jumps for the tableswitch command were defined!");
		}
		else if (!jumps.containsKey(DEFAULT_MARK)) {
			throw new ContentException("No default jump for the tableswitch command was defined!");
		}
		else {
			int		minValue = Integer.MAX_VALUE, maxValue = Integer.MIN_VALUE;
			
			for (Entry<Long,Long> item : jumps.entrySet()) {
				if (item.getKey().longValue() != DEFAULT_MARK) {
					minValue = Math.min(minValue,item.getKey().intValue());
					maxValue = Math.max(maxValue,item.getKey().intValue());
				}
			}
			if (minValue != maxValue && Math.abs((maxValue - minValue) / jumps.size()) > 20) {
				throw new ContentException("jump table utilizied less than 5%! Use lookupswitch instead of tableswitch command!");
			}
			else {
				final long					defaultLabel = jumps.remove(DEFAULT_MARK);
				@SuppressWarnings("unchecked")
				final Entry<Long,Long>[]	table = jumps.entrySet().toArray(new Entry[jumps.size()]);
				
				Arrays.sort(table,SORT_COMPARATOR);
				registerBranch(switchAddress,getPC(),defaultLabel,false);
				putCommand((byte)0,(byte)0,(byte)0,(byte)0
						  ,(byte)((minValue >> 24) & 0xFF),(byte)((minValue >> 16) & 0xFF),(byte)((minValue >> 8) & 0xFF),(byte)(minValue & 0xFF)
						  ,(byte)((maxValue >> 24) & 0xFF),(byte)((maxValue >> 16) & 0xFF),(byte)((maxValue >> 8) & 0xFF),(byte)(maxValue & 0xFF)
						  );
				
				for (int index = minValue, cursor = 0; index <= maxValue; index++) {
					if (table[cursor].getKey() == index) {
						registerBranch(switchAddress,getPC(),table[cursor].getValue(),false);
						cursor++;
					}
					else {
						registerBranch(switchAddress,getPC(),defaultLabel,false);
					}
					putCommand((byte)0,(byte)0,(byte)0,(byte)0);
				}
			}
		}
	}

	private void fillLookup() throws IOException, ContentException {
		if (jumps.size() == 0) {
			throw new ContentException("No any jumps for the tableswitch command were defined!");
		}
		else if (!jumps.containsKey(DEFAULT_MARK)) {
			throw new ContentException("No default jump for the tableswitch command was defined!");
		}
		else {
			int	minValue = Integer.MAX_VALUE, maxValue = Integer.MIN_VALUE;
			
			for (Entry<Long,Long> item : jumps.entrySet()) {
				if (item.getKey() != DEFAULT_MARK) {
					minValue = Math.min(minValue,item.getKey().intValue());
					maxValue = Math.min(maxValue,item.getKey().intValue());
				}
			}
			final long					defaultLabel = jumps.remove(DEFAULT_MARK);
			@SuppressWarnings("unchecked")
			final Entry<Long,Long>[]	table = jumps.entrySet().toArray(new Entry[jumps.size()]);
			
			Arrays.sort(table,SORT_COMPARATOR);
			registerBranch(switchAddress,getPC(),defaultLabel,false);
			putCommand((byte)0,(byte)0,(byte)0,(byte)0
					  ,(byte)((table.length >> 24) & 0xFF),(byte)((table.length >> 16) & 0xFF),(byte)((table.length >> 8) & 0xFF),(byte)(table.length & 0xFF)
					  );
			
			for (Entry<Long,Long> item : table){
				final int	key = item.getKey().intValue();
				
				putCommand((byte)((key >> 24) & 0xFF),(byte)((key >> 16) & 0xFF),(byte)((key >> 8) & 0xFF),(byte)(key & 0xFF));
				registerBranch(switchAddress,getPC(),item.getValue(),false);
				putCommand((byte)0,(byte)0,(byte)0,(byte)0);
			}
		}
	}

	/*
	 * Automation processing
	 */

	private void processLoadAuto(char[] data, int skipBlank) {
		// TODO Auto-generated method stub
		
	}

	private void processStoreAuto(char[] data, int skipBlank) {
		// TODO Auto-generated method stub
		
	}

	private void processEvalAuto(char[] data, int skipBlank) {
		// TODO Auto-generated method stub
		
	}
	
	private void processCallAuto(char[] data, int skipBlank) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * Evaluation methods
	 */

	private int calculateLocalAddress(final char[] data, int start, final long[] result) throws ContentException {
		if (data[start] >= '0' && data[start] <= '9') {
			return UnsafedCharUtils.uncheckedParseNumber(data,start,result,CharUtils.PREF_INT,true);
		}
		else {
			final int	startName = start, endName;
			
			endName = skipSimpleName(data,start);
			result[0] = methodDescriptor.getVarDispl(tree.placeOrChangeName(data,startName,endName,new NameDescriptor()));
			return endName;
		}
	}
	
	private int calculateFieldAddress(final char[] data, int start, final int end, final int[] result) throws IOException, ContentException {
		int	startName = start, endName = start = skipSimpleName(data,start);
		
		if (data[start] != '.') {
			if ((result[0] = cc.getConstantPool().asFieldRefDescription(joinedClassNameId,tree.placeOrChangeName(data,startName,endName,new NameDescriptor()))) != 0) {
				return start;
			}
		}
		endName = start = skipQualifiedName(data,start);
		final Field		f = cdr.getFieldDescription(data,startName,endName);
		final long		type = tree.placeOrChangeName(f.getType().getName().replace('.','/'),new NameDescriptor());
		
		result[0] = cc.getConstantPool().asFieldRefDescription(
							tree.placeOrChangeName(f.getDeclaringClass().getName().replace('.','/'),new NameDescriptor()),
							tree.placeOrChangeName(f.getName(),new NameDescriptor()),
							tree.placeOrChangeName(InternalUtils.buildFieldSignature(tree,type),new NameDescriptor())
					);	
		return start;
	}

	private int calculateMethodAddress(final char[] data, int start, final int end, final int[] result, final int[] argsLength) throws IOException, ContentException {
		int			startName = start, endName = start = skipSimpleName(data,start);
		
		if (data[start] == '.') {
			endName = start = skipQualifiedName(data,startName);
			
			if (data[start] == '(') {
				final int			endSignature = start = skipSignature(data,start);
				final long			possibleId = cc.getNameTree().seekName(data,startName,endSignature);
				
				if (possibleId >= 0) {
					final String	classAndMethod = new String(data,startName,endName-startName);
					final String	classOnly = classAndMethod.substring(0,classAndMethod.lastIndexOf('.'));
					final String	methodOnly = classAndMethod.substring(classAndMethod.lastIndexOf('.')+1);
					
					result[0] = cc.getConstantPool().asMethodRefDescription(
							tree.seekName(classOnly.replace('.','/')),
							tree.seekName(classOnly.endsWith(methodOnly) ? "<init>" : methodOnly),
							tree.seekName(data,endName,endSignature)
					);
				}
				else {
					try{final Method	m = cdr.getMethodDescription(data,startName,endSignature);
					
						if (m.getDeclaringClass().isInterface()) {
							result[0] = cc.getConstantPool().asInterfaceMethodRefDescription(
									tree.placeOrChangeName(toCanonicalName(m.getDeclaringClass()).replace('.','/'),new NameDescriptor()),
									tree.placeOrChangeName(m.getName(),new NameDescriptor()),
									tree.placeOrChangeName(InternalUtils.buildSignature(m),new NameDescriptor())
							);
							argsLength[0] = 0;
							for (Parameter item : m.getParameters()) {
								argsLength[0] += item.getType() == long.class || item.getType() == double.class ? 2 : 1;
							}
						}
						else {
							result[0] = cc.getConstantPool().asMethodRefDescription(
									tree.placeOrChangeName(toCanonicalName(m.getDeclaringClass()).replace('.','/'),new NameDescriptor()),
									tree.placeOrChangeName(m.getName(),new NameDescriptor()),
									tree.placeOrChangeName(InternalUtils.buildSignature(m),new NameDescriptor())
							);
						}
					} catch (ContentException exc) {
						final Constructor<?>	c = cdr.getConstructorDescription(data,startName,endSignature);
						
						result[0] = cc.getConstantPool().asMethodRefDescription(
								tree.placeOrChangeName(c.getDeclaringClass().getName().replace('.','/'),new NameDescriptor()),
								tree.placeOrChangeName("<init>",new NameDescriptor()),
								tree.placeOrChangeName(InternalUtils.buildSignature(c),new NameDescriptor())
						);
					}
				}
			}
			else {
				throw new ContentException("Missing method signature!");
			}
		}
		else {
			if (data[start] == '(') {
				final int	startSignature = start, endSignature = start = skipSignature(data,start);
				
				result[0] = cc.getConstantPool().asMethodRefDescription(
						joinedClassNameId,
						tree.placeOrChangeName(data,startName,endName,new NameDescriptor()),
						tree.placeOrChangeName(data,startSignature,endSignature,new NameDescriptor())						
				);
			}
			else {
				throw new ContentException("Missing method signature!");
			}
		}
		return start;
	}
	
	private int calculateValue(final char[] data, int start, final EvalState state, final long[] result) throws ContentException {
		long		value[] = new long[]{0,0};
		char		symbol;
		
		switch (state) {
			case term				:
				switch (data[start]) {
					case '0' : 
						return UnsafedCharUtils.uncheckedParseLongExtended(data,start,result,true);
					case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						return UnsafedCharUtils.uncheckedParseLong(data,start,result,true);
					case '(' :
						if (data[start = InternalUtils.skipBlank(data,calculateValue(data,InternalUtils.skipBlank(data,start+1),EvalState.additional,result))]==')') {
							return InternalUtils.skipBlank(data,start+1);
						}
						else {
							throw new ContentException("Unclosed ')' in the expression");
						}
					default :
						if (Character.isJavaIdentifierStart(data[start])) {
							start = skipSimpleName(data,start);
						}
						throw new ContentException("Non-constant value in the expression");
				}
			case unary				:
				if (data[start] == '-') {
					start = InternalUtils.skipBlank(data,calculateValue(data,InternalUtils.skipBlank(data,start+1),EvalState.term,result));
					result[0] = -result[0];
				}
				else {
					start = InternalUtils.skipBlank(data,calculateValue(data,start,EvalState.term,result));
				}
				return start;
			case multiplicational	:
				start = InternalUtils.skipBlank(data,calculateValue(data,start,EvalState.unary,result));
				while ((symbol = data[start]) == '*' || symbol == '/' || symbol == '%') {
					start = InternalUtils.skipBlank(data,calculateValue(data,InternalUtils.skipBlank(data,start+1),EvalState.unary,value));
					switch (symbol) {
						case '*' : result[0] *= value[0]; break;
						case '/' : result[0] /= value[0]; break;
						case '%' : result[0] %= value[0]; break;
					}
				}
				return start;
			case additional			:
				start = InternalUtils.skipBlank(data,calculateValue(data,start,EvalState.multiplicational,result));
				while ((symbol = data[start]) == '+' || symbol == '-') {
					start = InternalUtils.skipBlank(data,calculateValue(data,InternalUtils.skipBlank(data,start+1),EvalState.multiplicational,value));
					switch (symbol) {
						case '+' : result[0] += value[0]; break;
						case '-' : result[0] -= value[0]; break;
					}
				}
				return start;
			default : throw new UnsupportedOperationException("Evaluation state ["+state+"] is not supported yet");
		}
	}

	private int calculateBranchAddress(final char[] data, int start, final long[] result) {
		final int	startName = start, endName = start = skipSimpleName(data,start);
		
		result[0] = cc.getNameTree().placeOrChangeName(data,startName,endName,new NameDescriptor());
		return start;
	}
	
	/*
	 * Utility methods
	 */
	private static short addAndCheckDuplicates(final short source, final short added, final String parameter, final String directive) throws ContentException {
		if ((source & added) != 0) {
			throw new ContentException("Duplicate option ["+parameter+"] in the ["+directive+"] directive");
		}
		else {
			return (short) (source | added);
		}
	}

	private static int extractClass(final char[] data, int start, final ClassDescriptionRepo cdr, final Class<?>[] result) throws ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedName(data,start);
		
		result[0] = cdr.getClassDescription(data,startName,endName);
		return InternalUtils.skipBlank(data,start);
	}
	
	private static int extractClassWithPossibleArray(final char[] data, int start, final ClassDescriptionRepo cdr, final Class<?>[] result) throws ContentException {
		start = extractClass(data,start,cdr,result);
		
		if (data[start] == '[') {	// Create class for the arrays "on-the-fly"
			int	dimension = 0, step = 0;
			
			while (data[start] == '[' || data[start] == ']') {
				if (data[start] == '[') {
					dimension++;
				}
				else {
					step++;
				}
				start++;
			}
			if (dimension != step) {
				throw new ContentException("Unpaired brackets in the class description");
			}
			else {
				result[0] = Array.newInstance(result[0],new int[dimension]).getClass();
			}
		}
		return start;
	}

	private static int processOptions(final char[] data, int start, final EntityDescriptor desc, final String location, final ClassDescriptionRepo cdr, final boolean treatExtendsAsImplements, final int... availableOptions) throws ContentException {
		final boolean[]		parsed = new boolean[availableOptions.length]; 
		int					startOption, endOption, optionCode;
		boolean				validOption;
		
		desc.clear();
		while (data[start] != '\n') {
			startOption = start = InternalUtils.skipBlank(data,start);
			endOption = start = skipSimpleName(data,start);
			if (startOption != endOption) {
				optionCode = (int)staticDirectiveTree.seekName(data,startOption,endOption);
				validOption = false;
				for (int index = 0, maxIndex = availableOptions.length; index < maxIndex; index++) {
					if (availableOptions[index] == optionCode) {
						if (parsed[index]) {
							throw new ContentException("Duplicate option ["+new String(data,startOption,endOption-startOption)+"] for  ["+location+"] descriptor");
						}
						else {
							parsed[index] = true;
							validOption = true;
							break;
						}
					}
				}
				
				if (!validOption) {
					throw new ContentException("Invalid option ["+new String(data,startOption,endOption-startOption)+"] for  ["+location+"] descriptor");
				}
				else {
					final DirectiveDescriptor	dd = staticDirectiveTree.getCargo(optionCode);
					
					if (dd.getType() == DirectiveType.OPTION) {
						desc.options |= ((DirectiveOption)dd).getOption();
					}
					else if ((dd instanceof DirectiveClassOption) && treatExtendsAsImplements) {
						start = new DirectiveInterfacesOption().processList(data,start,cdr,desc);
					}
					else {
						start = dd.processList(data,start,cdr,desc);
					}
				}
			}
		}
		
		if  (checkMutualPrivateProtectedPublic(desc.options)) {
			throw new ContentException("Mutually exclusive options (public/protected/private) in the '"+location+"' directive!");
		}
		else if  (checkMutualAbstractFinal(desc.options)) {
			throw new ContentException("Mutually exclusive options (abstract/final) in the '"+location+"' directive!");
		}
		else if  (checkMutualStaticAbstract(desc.options)) {
			throw new ContentException("Mutually exclusive options (static/abstract) in the '"+location+"' directive!");
		}
		else {
			return start;
		}		
	}
	
	private static boolean checkMutualPrivateProtectedPublic(final short flags) {
		final int	result = flags & (Constants.ACC_PUBLIC | Constants.ACC_PROTECTED | Constants.ACC_PRIVATE);
		
		return result == 3 || result == 5 || result == 6 || result == 7;
	}

	private static boolean checkMutualAbstractFinal(final short flags) {
		return (flags & Constants.ACC_ABSTRACT) != 0 && (flags & Constants.ACC_FINAL) != 0;
	}

	private static boolean checkMutualStaticAbstract(final short flags) {
		return (flags & Constants.ACC_ABSTRACT) != 0 && (flags & Constants.ACC_STATIC) != 0;
	}

	private static int skipSimpleName(final char[] data, int from) {
		char	symbol;
		
		while (((symbol = data[from]) >= 'a' && symbol <= 'z' || symbol >= 'A' && symbol <= 'Z' || symbol >= '0' && symbol <= '9' || symbol == '_' || symbol == '$')) {
			from++;
		}
		return from;
	}

	private static int skipQualifiedName(final char[] data, int from) {
		char	symbol;
		
		while (((symbol = data[from]) >= 'a' && symbol <= 'z' || symbol >= 'A' && symbol <= 'Z' || symbol >= '0' && symbol <= '9' || symbol == '_' || symbol == '$' || symbol == '.' || symbol == '<' || symbol == '>')) {
			from++;
		}
		return from;
	}

	private static int skipQualifiedNameWithArray(final char[] data, int from) {
		char	symbol;
		
		while (((symbol = data[from]) >= 'a' && symbol <= 'z' || symbol >= 'A' && symbol <= 'Z' || symbol >= '0' && symbol <= '9' || symbol == '_' || symbol == '$' || symbol == '.' || symbol == '[' || symbol == ']')) {
			from++;
		}
		return from;
	}

	private static int skipQuoted(final char[] data, int from, final char terminal) throws ContentException {
		while (data[from] != '\n' && data[from] != terminal) {
			from++;
		}
		if (data[from] == terminal) {
			return from;
		}
		else {
			throw new ContentException("Missing close quota ("+terminal+")!");
		}
	}

	private int skipSignature(final char[] data, int from) throws ContentException {
		while (data[from] <= ' ' && data[from] != '\n') {
			from++;
		}
		
		if (data[from] == '(') {
			from++;
			while(data[from] != ')' && data[from] != '\n') {
				from = skipSignature(data,from);
			}
			
			if (data[from] == ')') {
				return skipSignature(data,from+1);
			}
			else {
				throw new ContentException("Unclosed bracket ')' in the method signature");
			}
		}
		else {
			switch (data[from]) {
				case '['	:
					while (data[from] == '[') {
						from++;
					}
					return skipSignature(data,from);
				case 'Z' : case 'B' : case 'C' : case 'D' : case 'F' : case 'I' : case 'J' : case 'S' : case 'V' :
					return from+1;
				case 'L'	:
					while (data[from] != ';' && data[from] != '\n') {
						from++;
					}
					if (data[from] == ';') {
						return from + 1;
					}
					else {
						throw new ContentException("Missing ';' in the class signature descriptor");
					}
				default :
					throw new ContentException("Illegal signature symbol '"+data[from]+"' in the method signature");
			}
		}
	}

	private static void skip2line(final char[] data, int from) throws ContentException {
		while (data[from] <= ' ' && data[from] != '\n' && data[from] != '\r') {
			from++;
		}
		if (data[from] == '\n' || data[from] == '\r') {
			return;
		}
		else if (data[from] == '/' && data[from+1] == '/') {
			return;
		}
		else {
			throw new ContentException("Unparsed tail in the input string");
		}
	}

	/*
	 * Parser preparation methods
	 */
	
	private static void placeStaticDirective(final int code, final DirectiveDescriptor desc, final String mnemonics) {
		if (staticDirectiveTree.contains(code)) {
			throw new IllegalArgumentException("Duplicate directive code ["+code+"]: "+mnemonics+", already exists "+staticDirectiveTree.getName(code));
		}
		else {
			staticDirectiveTree.placeOrChangeName(mnemonics,code,desc);
		}
	}

	private static void placeStaticCommand(final int operation, final int stackDelta, final String mnemonics) {
		placeStaticCommand(operation, stackDelta, mnemonics, CommandFormat.single);
	}
	
	private static void placeStaticCommand(final int operation, final int stackDelta, final String mnemonics, final CommandFormat format) {
		if (staticCommandTree.contains(operation)) {
			throw new IllegalArgumentException("Duplicate opcode ["+operation+"]: "+mnemonics+", already exists "+staticCommandTree.getName(operation));
		}
		else {
			staticCommandTree.placeOrChangeName(mnemonics,operation,new CommandDescriptor(operation,stackDelta,format));
		}
	}

	private static CommandDescriptor extractStaticCommand(final String mnemonics) {
		return staticCommandTree.getCargo(staticCommandTree.seekName(mnemonics));
	}
	
	private static String toCanonicalName(final Class<?> clazz) {
		final char[]	result = clazz.getCanonicalName().toCharArray();
		
		Class<?>		dollarClazz = clazz.getEnclosingClass();
		int				index = result.length-1;
		
		while (dollarClazz != null) {
			 while (result[index] != '.') {
				 index--;
			 }
			 result[index] = '$';
			 dollarClazz = dollarClazz.getEnclosingClass();
		}
		return new String(result);
	}
	
	private static class EntityDescriptor {
		public short			options;
		@SuppressWarnings("unused")
		public Class<?>			extendsItem;
		public List<Class<?>>	implementsList = new ArrayList<>();
		public List<Class<?>>	throwsList = new ArrayList<>();
		
		public void clear() {
			options = 0;
			extendsItem = null;
			implementsList.clear();
			throwsList.clear();
		}
	}
	
	private enum DirectiveType {
		OPTION, LIST
	}
	
	private abstract static class DirectiveDescriptor {
		private final DirectiveType	type;
		
		public DirectiveDescriptor(DirectiveType type) {
			this.type = type;
		}

		public DirectiveType getType() {
			return type;
		}
		
		public abstract int processList(final char[] data, final int from, final ClassDescriptionRepo cdr, final EntityDescriptor result) throws ContentException; 
	}
	
	private static class DirectiveOption extends DirectiveDescriptor {
		private final short		option;

		public DirectiveOption(short option) {
			super(DirectiveType.OPTION);
			this.option = option;
		}

		public short getOption() {
			return option;
		}

		@Override
		public int processList(char[] data, int from, ClassDescriptionRepo cdr, EntityDescriptor result) throws ContentException {
			return from;
		}
	}

	private static class DirectiveMarker extends DirectiveDescriptor {
		public DirectiveMarker() {
			super(DirectiveType.OPTION);
		}

		@Override
		public int processList(char[] data, int from, ClassDescriptionRepo cdr, EntityDescriptor result) throws ContentException {
			return from;
		}
	}
	
	private static class DirectiveInterfacesOption extends DirectiveDescriptor {
		public DirectiveInterfacesOption() {
			super(DirectiveType.LIST);
		}

		@Override
		public int processList(char[] data, int from, ClassDescriptionRepo cdr, EntityDescriptor result) throws ContentException {
			final Class<?>[]	forClass = new Class<?>[1];

			from--;
			do {from = extractClass(data,from+1,cdr,forClass);
				if (!forClass[0].isInterface()) {
					throw new ContentException("Class referenced ["+forClass[0]+"] must be interface!");
				}
				else {
					result.implementsList.add(forClass[0]);
				}
			} while (data[from] == ',');
			return from;
		}
	}
	
	private static class DirectiveClassOption extends DirectiveDescriptor {
		public DirectiveClassOption() {
			super(DirectiveType.LIST);
		}

		@Override
		public int processList(char[] data, int from, ClassDescriptionRepo cdr, EntityDescriptor result) throws ContentException {
			final Class<?>[]	forClass = new Class<?>[1];
			
			from = extractClass(data,from,cdr,forClass);
			if (forClass[0].isInterface()) {
				throw new ContentException("Class referenced ["+forClass[0]+"] can't be interface!");
			}
			else {
				result.extendsItem = forClass[0];
				return from;
			}
		}
	}

	private static class DirectiveExceptionsOption extends DirectiveDescriptor {
		public DirectiveExceptionsOption() {
			super(DirectiveType.LIST);
		}

		@Override
		public int processList(char[] data, int from, ClassDescriptionRepo cdr, EntityDescriptor result) throws ContentException {
			final Class<?>[]	forClass = new Class<?>[1];

			from--;
			do {from = extractClass(data,from+1,cdr,forClass);
				if (!Throwable.class.isAssignableFrom(forClass[0])) {
					throw new ContentException("Class referenced ["+forClass[0]+"] must be compatible with ["+Throwable.class+"]!");
				}
				else {
					result.throwsList.add(forClass[0]);
				}
			} while (data[from] == ',');
			return from;
		}
	}
	
	private static class CommandDescriptor {
		public int				operation;
		public int				stackDelta;
		public CommandFormat	commandFormat;
		
		public CommandDescriptor(final int operation, final int stackDelta, final CommandFormat commandFormat) {
			this.operation = operation;
			this.stackDelta = stackDelta;
			this.commandFormat = commandFormat;
		}

		@Override
		public String toString() {
			return "CommandDescriptor [operation=" + operation + ", stackDelta=" + stackDelta + ", commandFormat=" + commandFormat + "]";
		}
	}
}
