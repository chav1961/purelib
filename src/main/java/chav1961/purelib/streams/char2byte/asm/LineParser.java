package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import chav1961.purelib.basic.CharsUtil;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.AsmSyntaxException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class LineParser implements LineByLineProcessorCallback {
	private static final int						EXPONENT_BASE = 305;
	private static final double[]					EXPONENTS;
	
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

	private static final SyntaxTreeInterface<Object>			staticDirectiveTree = new AndOrTree<>(2,16);
	private static final SyntaxTreeInterface<CommandDescriptor>	staticCommandTree = new AndOrTree<>(3,16);

	private static final Class<?>[]					PRELOADED_CLASSES = new Class<?>[]{boolean.class,byte.class,char.class,double.class,float.class,int.class,long.class,short.class,void.class,
																					   boolean[].class,byte[].class,char[].class,double[].class,float[].class,int[].class,long[].class,short[].class,
																					   boolean[][].class,byte[][].class,char[][].class,double[][].class,float[][].class,int[][].class,long[][].class,short[][].class,
																					   Object.class,String.class,Throwable.class,
																					   Object[].class,String[].class,Throwable[].class,
																					   Object[][].class,String[][].class,Throwable[][].class
																					   };

	private enum EvalState {
		term, unary, multiplicational, additional 
	}
	
	private enum ParserState {
		beforePackage, beforeImport, insideClass, insideInterface, insideInterfaceAbstractMethod, insideClassAbstractMethod, insideClassMethod, insideClassBody, insideBegin, insideMethodLookup, insideMethodTable, afterCLass
	}
	
	private enum CommandFormat{
		single, byteIndex, byteValue, shortValue, byteType, byteIndexAndByteValue, shortIndexAndByteValue, classShortIndex, valueByteIndex, valueShortIndex, valueShortIndex2, shortBrunch, longBrunch, shortGlobalIndex, call, callDynamic, callInterface, lookupSwitch, tableSwitch, wide
	}
	
	static {
		EXPONENTS = new double[2 * EXPONENT_BASE + 1];
		
		for (int index = -EXPONENT_BASE; index <= EXPONENT_BASE; index++) {
			EXPONENTS[index+EXPONENT_BASE] = Double.valueOf("1e"+index);
		}
		
		placeStaticDirective(DIR_CLASS,".class");
		placeStaticDirective(DIR_METHOD,".method");
		placeStaticDirective(DIR_FIELD,".field");
		placeStaticDirective(DIR_END,".end");
		placeStaticDirective(DIR_PACKAGE,".package");
		placeStaticDirective(DIR_IMPORT,".import");
		placeStaticDirective(DIR_INCLUDE,".include");
		placeStaticDirective(DIR_INTERFACE,".interface");
		placeStaticDirective(DIR_PARAMETER,".parameter");
		placeStaticDirective(DIR_VAR,".var");
		placeStaticDirective(DIR_BEGIN,".begin");
		placeStaticDirective(DIR_STACK,".stack");
		placeStaticDirective(DIR_TRY,".try");
		placeStaticDirective(DIR_CATCH,".catch");
		placeStaticDirective(DIR_DEFAULT,".default");
		placeStaticDirective(DIR_END_TRY,".endtry");
		
		placeStaticDirective(OPTION_PUBLIC,"public");
		placeStaticDirective(OPTION_FINAL,"final");
		placeStaticDirective(OPTION_ABSTRACT,"abstract");
		placeStaticDirective(OPTION_SYNTHETIC,"synthetic");
		placeStaticDirective(OPTION_ENUM,"enum");
		placeStaticDirective(OPTION_EXTENDS,"extends");
		placeStaticDirective(OPTION_IMPLEMENTS,"implements");
		placeStaticDirective(OPTION_PRIVATE,"private");
		placeStaticDirective(OPTION_PROTECTED,"protected");
		placeStaticDirective(OPTION_STATIC,"static");
		placeStaticDirective(OPTION_VOLATILE,"volatile");
		placeStaticDirective(OPTION_TRANSIENT,"transient");
		placeStaticDirective(OPTION_SYNCHRONIZED,"synchronized");
		placeStaticDirective(OPTION_BRIDGE,"bridge");
		placeStaticDirective(OPTION_VARARGS,"varargs");
		placeStaticDirective(OPTION_NATIVE,"native");
		placeStaticDirective(OPTION_STRICT,"strict");
		placeStaticDirective(OPTION_THROWS,"throws");

		placeStaticDirective(T_BOOLEAN,"boolean");
		placeStaticDirective(T_CHAR,"char");
		placeStaticDirective(T_FLOAT,"float");
		placeStaticDirective(T_DOUBLE,"double");
		placeStaticDirective(T_BYTE,"byte");
		placeStaticDirective(T_SHORT,"short");
		placeStaticDirective(T_INT,"int");
		placeStaticDirective(T_LONG,"long");	

		placeStaticDirective(STACK_OPTIMISTIC,"optimistic");	
		placeStaticDirective(STACK_PESSIMISTIC,"pessimistic");	

		placeStaticDirective(CMD_LOAD,"*load");	
		placeStaticDirective(CMD_STORE,"*store");	
		placeStaticDirective(CMD_EVAL,"*eval");	
		placeStaticDirective(CMD_CALL,"*call");	
		
		placeStaticCommand(0x32,1,"aaload");
		placeStaticCommand(0x53,-1,"aastore");
		placeStaticCommand(0x01,1,"aconst_null");
		placeStaticCommand(0x19,1,"aload",CommandFormat.byteIndex);
		placeStaticCommand(0x2a,1,"aload_0");
		placeStaticCommand(0x2b,1,"aload_1");
		placeStaticCommand(0x2c,1,"aload_2");
		placeStaticCommand(0x2d,1,"aload_3");
		placeStaticCommand(0xbd,1,"anewarray",CommandFormat.classShortIndex);
		placeStaticCommand(0xb0,1,"areturn");
		placeStaticCommand(0xbe,1,"arraylength");
		placeStaticCommand(0x3a,1,"astore",CommandFormat.byteIndex);
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
		placeStaticCommand(0x18,1,"dload",CommandFormat.byteIndex);
		placeStaticCommand(0x26,1,"dload_0");
		placeStaticCommand(0x27,1,"dload_1");
		placeStaticCommand(0x28,1,"dload_2");
		placeStaticCommand(0x29,1,"dload_3");
		placeStaticCommand(0x6b,1,"dmul");
		placeStaticCommand(0x77,1,"dneg");
		placeStaticCommand(0x73,1,"drem");
		placeStaticCommand(0xaf,1,"dreturn");
		placeStaticCommand(0x39,1,"dstore",CommandFormat.byteIndex);
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
		placeStaticCommand(0x17,1,"fload",CommandFormat.byteIndex);
		placeStaticCommand(0x22,1,"fload_0");
		placeStaticCommand(0x23,1,"fload_1");
		placeStaticCommand(0x24,1,"fload_2");
		placeStaticCommand(0x25,1,"fload_3");
		placeStaticCommand(0x6a,1,"fmul");
		placeStaticCommand(0x76,1,"fneg");
		placeStaticCommand(0x72,1,"frem");
		placeStaticCommand(0xae,1,"freturn");
		placeStaticCommand(0x38,1,"fstore",CommandFormat.byteIndex);
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
		placeStaticCommand(0x9f,1,"if_acmpeq");
		placeStaticCommand(0xa0,1,"if_acmpne");
		placeStaticCommand(0xa1,1,"if_icmplt");
		placeStaticCommand(0xa2,1,"if_icmpge");
		placeStaticCommand(0x03,1,"if_icmpgt");
		placeStaticCommand(0x04,1,"if_icmple");
		placeStaticCommand(0x99,1,"ifeq",CommandFormat.shortBrunch);
		placeStaticCommand(0x9a,1,"ifne",CommandFormat.shortBrunch);
		placeStaticCommand(0x9b,1,"iflt",CommandFormat.shortBrunch);
		placeStaticCommand(0x9c,1,"ifge",CommandFormat.shortBrunch);
		placeStaticCommand(0x9d,1,"ifgt",CommandFormat.shortBrunch);
		placeStaticCommand(0x9e,1,"ifle",CommandFormat.shortBrunch);
		placeStaticCommand(0xc7,1,"ifnonnull",CommandFormat.shortBrunch);
		placeStaticCommand(0xc6,1,"ifnull",CommandFormat.shortBrunch);
		placeStaticCommand(0x84,1,"iinc",CommandFormat.byteIndexAndByteValue);
		placeStaticCommand(0x15,1,"iload",CommandFormat.byteIndex);
		placeStaticCommand(0x1a,1,"iload_0");
		placeStaticCommand(0x1b,1,"iload_1");
		placeStaticCommand(0x1c,1,"iload_2");
		placeStaticCommand(0x1d,1,"iload_3");
		placeStaticCommand(0x68,1,"imul");
		placeStaticCommand(0x74,1,"ineg");
		placeStaticCommand(0xc1,1,"instanceof",CommandFormat.classShortIndex);
		placeStaticCommand(0xba,1,"invokedynamic",CommandFormat.callDynamic);
		placeStaticCommand(0xb9,1,"invokeinterface",CommandFormat.callInterface);
		placeStaticCommand(0xb7,1,"invokespecial",CommandFormat.call);
		placeStaticCommand(0xb8,1,"invokestatic",CommandFormat.call);
		placeStaticCommand(0xb6,1,"invokevirtual",CommandFormat.call);
		placeStaticCommand(0x80,1,"ior");
		placeStaticCommand(0x70,1,"irem");
		placeStaticCommand(0xac,1,"ireturn");
		placeStaticCommand(0x78,1,"ishl");
		placeStaticCommand(0x7a,1,"ishr");
		placeStaticCommand(0x36,1,"istore",CommandFormat.byteIndex);
		placeStaticCommand(0x3b,1,"istore_0");
		placeStaticCommand(0x3c,1,"istore_1");
		placeStaticCommand(0x3d,1,"istore_2");
		placeStaticCommand(0x3e,1,"istore_3");
		placeStaticCommand(0x64,1,"isub");
		placeStaticCommand(0x7c,1,"iushr");
		placeStaticCommand(0x82,1,"ixor");
		placeStaticCommand(0xa8,1,"jsr",CommandFormat.shortBrunch);
		placeStaticCommand(0xc9,1,"jsr_w",CommandFormat.longBrunch);
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
		placeStaticCommand(0x16,1,"lload",CommandFormat.byteIndex);
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
		placeStaticCommand(0x37,1,"lstore",CommandFormat.byteIndex);
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
		placeStaticCommand(0xa9,1,"ret",CommandFormat.byteIndex);
		placeStaticCommand(0xb1,1,"return");
		placeStaticCommand(0x35,1,"saload");
		placeStaticCommand(0x56,1,"sastore");
		placeStaticCommand(0x11,1,"sipush",CommandFormat.shortValue);
		placeStaticCommand(0x5f,1,"swap");
		placeStaticCommand(0xaa,1,"tableswitch",CommandFormat.tableSwitch);
		placeStaticCommand(0xc4,1,"wide",CommandFormat.wide);
	}
	
	
	private final ClassDescriptionRepo				cdr;
	private final SyntaxTreeInterface<NameDescriptor>		tree;
	private final ClassContainer					cc;

	private ParserState								state = ParserState.beforePackage;
	private long									packageId = -1, constructorId, voidId;
	private long									classNameId, joinedClassNameId;
	private long									methodNameId;
	private MethodDescriptor						methodDescriptor;
	private short									classFlags = 0;
	private Map<Long,Long>							jumps = new HashMap<>();
	private int										switchAddress;
	private List<int[]>								tryList = new ArrayList<int[]>();
	
	LineParser(final ClassContainer cc) throws AsmSyntaxException {
		this.cc = cc;
		this.tree = cc.getNameTree();				this.cdr = new ClassDescriptionRepo();
		for (Class<?> item : PRELOADED_CLASSES) {
			cdr.addDescription(item);
		}
		constructorId = tree.placeName("<init>",null);
		voidId = tree.placeName("void",null);
	}

	@Override
	public void processLine(int lineNo, char[] data, int from, int len) throws IOException {
		int		start = from, end = Math.min(from+len,data.length);
		int		startName, endName, startDir, endDir;
		long	id = -1;
		
		if (data[start] > ' ') {
			startName = start;
			start = skipSimpleName(data,start);
			endName = start;
			id = tree.placeName(data,startName,endName,null);
			
			start = skipBlank(data,start);
			if (data[start] == ':') {
				switch (state) {
					case insideClassBody :
					case insideBegin :
						putLabel(id);
						break;
					default :
						throw new AsmSyntaxException("Branch label outside the method body!");
				}
				start++;
				id = -1;
			}
		}
		
		startDir = start = skipBlank(data,start);
		
		switch (data[start]) {
			case '.' :
				endDir = start = skipSimpleName(data,start+1);

				switch ((int)staticDirectiveTree.seekName(data,startDir,endDir)) {
					case DIR_CLASS	:
						checkLabel(id,true);
						switch (state) {
							case beforePackage :
							case beforeImport :
								processClassDir(id,data,skipBlank(data,start));
								state = ParserState.insideClass;
								break;
							case afterCLass :
								throw new AsmSyntaxException("Second class directive in the same stream. Use separate streams for each class/interface!");
							default :
								throw new AsmSyntaxException("Nested class directive!");
						}						
						break;
					case DIR_INTERFACE:
						checkLabel(id,true);
						switch (state) {
							case beforePackage :
							case beforeImport :
								processInterfaceDir(id,data,skipBlank(data,start));
								state = ParserState.insideInterface;
								break;
							case afterCLass :
								throw new AsmSyntaxException("Second interface directive in the same stream. Use separate streams for each class/interface!");
							default :
								throw new AsmSyntaxException("Nested interface directive!");
						}						
						break;
					case DIR_FIELD	:
						checkLabel(id,true);
						switch (state) {
							case insideClass :
								processClassFieldDir(id,data,skipBlank(data,start));
								break;
							case insideInterface :
								processInterfaceFieldDir(id,data,skipBlank(data,start));
								break;
							case beforePackage :
							case beforeImport :
							case afterCLass :
								throw new AsmSyntaxException("Field directive outside the class/interface!");
							default :
								throw new AsmSyntaxException("Field directive inside the method!");
						}
						break;
					case DIR_METHOD	:
						checkLabel(id,true);
						switch (state) {
							case insideClass :
								processClassMethodDir(id,data,skipBlank(data,start));
								state = methodDescriptor.isAbstract() ? ParserState.insideClassAbstractMethod : ParserState.insideClassMethod;
								break;
							case insideInterface :
								processInterfaceMethodDir(id,data,skipBlank(data,start));
								state = ParserState.insideInterfaceAbstractMethod;
								break;
							case beforePackage :
							case beforeImport :
							case afterCLass :
								throw new AsmSyntaxException("Method directive outside the class/interface!");
							default :
								throw new AsmSyntaxException("Nested method directive!");
						}
						break;
					case DIR_PARAMETER	:
						checkLabel(id,true);
						switch (state) {
							case insideClassAbstractMethod :
							case insideInterfaceAbstractMethod :
							case insideClassMethod :
								processParameterDir(id,data,skipBlank(data,start));
								break;
							default :
								throw new AsmSyntaxException("Parameter directive is used outside the method description!");
						}
						break;
					case DIR_VAR	:
						checkLabel(id,true);
						switch (state) {
							case insideClassBody :
							case insideBegin :
								processVarDir(id,data,skipBlank(data,start));
								break;
							default :
								throw new AsmSyntaxException("Var directive is used outside the method description!");
						}
						break;
					case DIR_BEGIN	:
						switch (state) {
							case insideBegin :
							case insideClassMethod :
								break;
							default :
								throw new AsmSyntaxException("Begin directive is valid in the method body only!");
						}
						break;
					case DIR_END	:
						switch (state) {
							case insideClass :
							case insideInterface :
								checkLabel(id,true);
								if (id != classNameId) {
									throw new AsmSyntaxException("End directive closes class description, but it's label ["+tree.getName(id)+"] is differ from class name ["+tree.getName(classNameId)+"]");
								}
								else {
									state = ParserState.afterCLass;
								}
								break;
							case insideClassAbstractMethod :
								checkLabel(id,true);
								if (id != methodNameId) {
									throw new AsmSyntaxException("End directive closes method description, but it's label ["+tree.getName(id)+"] is differ from method name ["+tree.getName(methodNameId)+"]");
								}
								else {
									methodDescriptor.complete();
									state = ParserState.insideClass;
								}
								break;
							case insideClassMethod :
								throw new AsmSyntaxException("Class method body in not defined!");
							case insideClassBody :
								checkLabel(id,true);
								if (id != methodNameId && id != classNameId) {
									throw new AsmSyntaxException("End directive closes method description, but it's label ["+tree.getName(id)+"] is differ from method name ["+tree.getName(methodNameId)+"]");
								}
								else if (!areTryBlocksClosed()) {
									throw new AsmSyntaxException("Unclosed try blocks inside the method body ["+tree.getName(methodNameId)+"]");
								}
								else {
									methodDescriptor.complete();
									state = ParserState.insideClass;
								}
								break;
							case insideInterfaceAbstractMethod :
								checkLabel(id,true);
								if (id != methodNameId) {
									throw new AsmSyntaxException("End directive closes method description, but it's label ["+tree.getName(id)+"] is differ from method name ["+tree.getName(methodNameId)+"]");
								}
								else {
									methodDescriptor.complete();
									state = ParserState.insideInterface;
								}
								break;
							case insideBegin :
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
								throw new AsmSyntaxException("End directive out of context!");
						}
						skip2line(data,start);
						break;
					case DIR_STACK	:
						checkLabel(id,false);
						switch (state) {
							case insideClassMethod :
								processStackDir(data,skipBlank(data,start));
								state = ParserState.insideClassBody;
								break;
							case insideClassBody :
							case insideBegin :
								throw new AsmSyntaxException("Duplicate Stack directive detected!");
							default :
								throw new AsmSyntaxException("Stack directive is used outside the method description!");
						}
						break;
					case DIR_PACKAGE:
						checkLabel(id,false);
						switch (state) {
							case beforePackage :
								processPackageDir(data,skipBlank(data,start));
								state = ParserState.beforeImport;
								break;
							case beforeImport :
								throw new AsmSyntaxException("Duplicate package directive!");
							default :
								throw new AsmSyntaxException("Package directive is used inside the class or interface description! Use import before the class/interface directive only!");
						}
						break;
					case DIR_IMPORT:
						checkLabel(id,false);
						switch (state) {
							case beforePackage :
							case beforeImport :
								processImportDir(data,skipBlank(data,start));
								break;
							default :
								throw new AsmSyntaxException("Package directive is used inside the class or interface description! Use import before the class/interface directive only!");
						}
						break;
					case DIR_INCLUDE:
						checkLabel(id,false);
						processIncludeDir(data,skipBlank(data,start),end);
						break;
					case DIR_TRY	:
						switch (state) {
							case insideClassBody :
							case insideBegin :
								pushTryBlock();
								break;
							default :
								throw new AsmSyntaxException("Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside method body only");
						}
						break;
					case DIR_CATCH	:
						switch (state) {
							case insideClassBody :
							case insideBegin :
								processCatch(data,skipBlank(data,start),end);
								break;
							default :
								throw new AsmSyntaxException("Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside method body only");
						}
						break;
					case DIR_END_TRY	:
						switch (state) {
							case insideClassBody :
							case insideBegin :
								popTryBlock(lineNo);
								break;
							default :
								throw new AsmSyntaxException("Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside method body only");
						}
						break;
					case DIR_DEFAULT:
						switch (state) {
							case insideMethodLookup : 
							case insideMethodTable : 
								processJumps(data,skipBlank(data,start),end,false);
								break;
							default :
								throw new AsmSyntaxException("Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside loowkuswitch/tableswitch command body only");
						}
						break;
					default :
						throw new AsmSyntaxException("Unknown directive : "+new String(data,startDir-1,endDir-startDir+1));
				}
				break;
			case '*' :
				switch (state) {
					case insideClassBody : 
					case insideBegin : 
						endDir = start = skipSimpleName(data,start+1);
		
						switch ((int)staticDirectiveTree.seekName(data,startDir,endDir)) {
							case CMD_LOAD	: processLoadAuto(data,skipBlank(data,start)); break;
							case CMD_STORE	: processStoreAuto(data,skipBlank(data,start)); break;
							case CMD_EVAL	: processEvalAuto(data,skipBlank(data,start)); break;
							case CMD_CALL	: processCallAuto(data,skipBlank(data,start)); break;
							default : throw new AsmSyntaxException("Unknown automation : "+new String(data,startDir-1,endDir-startDir+1));
						}
						break;
					default :
						throw new AsmSyntaxException("Automation can be used on the method body only!");
				}
				break;
			default :
				switch (state) {
					case insideClassBody : 
					case insideBegin : 
						endDir = start = skipSimpleName(data,start);
						final long	opCode = staticCommandTree.seekName(data,startDir,endDir);
	
						if (opCode != -1) {
							final CommandDescriptor	desc = staticCommandTree.getCargo(opCode);

							methodDescriptor.addLineNoRecord(lineNo);
							switch (desc.commandFormat) {
								case single					: processSingleCommand(desc,data,skipBlank(data,start)); break; 
								case byteIndex				: processByteIndexCommand(desc,data,skipBlank(data,start),false); break;
								case byteValue				: processByteValueCommand(desc,data,skipBlank(data,start),false); break;
								case shortValue				: processShortValueCommand(desc,data,skipBlank(data,start),false); break;
								case byteType				: processByteTypeCommand(desc,data,skipBlank(data,start)); break;
								case byteIndexAndByteValue	: processByteIndexAndByteValueCommand(desc,data,skipBlank(data,start),false); break;
								case shortIndexAndByteValue	: processShortIndexAndByteValueCommand(desc,data,skipBlank(data,start)); break;
								case classShortIndex		: processClassShortIndexCommand(desc,data,skipBlank(data,start)); break;
								case valueByteIndex			: processValueByteIndexCommand(desc,data,skipBlank(data,start)); break;
								case valueShortIndex		: processValueShortIndexCommand(desc,data,skipBlank(data,start)); break;
								case valueShortIndex2		: processValueShortIndex2Command(desc,data,skipBlank(data,start)); break;
								case shortBrunch			: processShortBrunchCommand(desc,data,skipBlank(data,start),end); break;
								case longBrunch				: processLongBrunchCommand(desc,data,skipBlank(data,start),end); break;
								case shortGlobalIndex		: processShortGlobalIndexCommand(desc,data,skipBlank(data,start),end); break;
								case call					: processCallCommand(desc,data,skipBlank(data,start),end); break;
								case callDynamic			: processDynamicCallCommand(desc,data,skipBlank(data,start),end); break;
								case callInterface			: processInterfaceCallCommand(desc,data,skipBlank(data,start),end); break;
								case lookupSwitch			: switchAddress = getPC(); putCommand((byte)opCode); alignPC(); jumps.clear(); state = ParserState.insideMethodLookup; break;
								case tableSwitch			: switchAddress = getPC(); putCommand((byte)opCode); alignPC(); jumps.clear(); state = ParserState.insideMethodTable; break;
								case wide					: processWideCommand(desc,data,skipBlank(data,start),end); break;
								default : throw new UnsupportedOperationException("Command format ["+desc.commandFormat+"] is not supported yet");
							}
						}
						else {
							throw new AsmSyntaxException("Unknown command : "+new String(data,startDir,endDir-startDir));
						}
						break;
					case insideMethodLookup : 
					case insideMethodTable : 
						processJumps(data,startDir,end,true);
						break;
					default :
						throw new AsmSyntaxException("Unknown line: "+new String(data,from,len));
				}
		}
	}

	private void checkLabel(final long labelId, final boolean present) throws AsmSyntaxException {
		if (present) {
			if (labelId == -1) {
				throw new AsmSyntaxException("Missing mandatory name before directive!");
			}
		}
		else {
			if (labelId != -1) {
				throw new AsmSyntaxException("Name before this directive is not supported!");
			}
		}
	}
	
	private void putLabel(final long labelId) throws IOException {
		methodDescriptor.getBody().putLabel(labelId);
	}

	private int getPC() throws IOException {
		return methodDescriptor.getBody().getPC();
	}
	
	private void putCommand(final byte... command) throws IOException {
		methodDescriptor.getBody().putCommand(1,command);
	}

	private void alignPC() throws IOException {
		methodDescriptor.getBody().alignPC();
	}

	private void registerBranch(final long forResult, final boolean shortBrunch) throws IOException {
		methodDescriptor.getBody().registerBrunch(forResult,shortBrunch);
	}

	private void registerBranch(final int address, final int placement, final long forResult, final boolean shortBrunch) throws IOException {
		methodDescriptor.getBody().registerBrunch(address,placement,forResult,shortBrunch);
	}
	
	/*
	 * Process directives
	 */
	private void processClassDir(final long id, final char[] data, int start) throws IOException {
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
						throw new AsmSyntaxException("Duplicate option 'extends' in the 'class' directive!");
					}
					else {
						int		startName = start = skipBlank(data,start), endName = start = skipQualifiedName(data,start);
						
						final Class<?>	parent = cdr.getClassDescription(data,startName,endName);

						if ((parent.getModifiers() & Constants.ACC_FINAL) != 0) {
							throw new AsmSyntaxException("Attempt to extends final class ["+new String(data,startName,endName-startName)+"]!"); 
						}
						else {
							extendsId = tree.placeName(parent.getName(),null);
						}
					}
					break;
				case OPTION_IMPLEMENTS	:
					if (implementsNames != null) {
						throw new AsmSyntaxException("Duplicate option 'implements' in the 'class' directive!");
					}
					else {
						implementsNames = new ArrayList<>();
						
						do {start++;
							int		startName = start = skipBlank(data,start), endName = start = skipQualifiedName(data,start);

							final Class<?>	member = cdr.getClassDescription(data,startName,endName);

							if ((member.getModifiers() & Constants.ACC_INTERFACE) == 0) {
								throw new AsmSyntaxException("Implements item ["+new String(data,startName,endName-startName)+"] references to the class, not interface!"); 
							}
							else {
								implementsNames.add(tree.placeName(member.getName(),null));
							}
							start = skipBlank(data,start);
						} while(data[start] == ',');
					}
					break;
				default :
					throw new AsmSyntaxException("'Class' definition contains unknown or unsupported option ["+new String(data,startOption,endOption)+"]!");
			}
			startOption = start = skipBlank(data,start);
		}
		
		if  (checkMutualPrivateProtectedPublic(classFlags)) {
			throw new AsmSyntaxException("Mutually exclusive options (public/protected/private) in the 'class' directive!");
		}
		else if (checkMutualAbstractFinal(classFlags)) {
			throw new AsmSyntaxException("Mutually exclusive options (abstract/final) in the 'class' directive!");
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

	private void processInterfaceDir(final long id, final char[] data, int start) throws IOException {
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
						throw new AsmSyntaxException("Duplicate option 'implements' in the 'class' directive!");
					}
					else {
						implementsNames = new ArrayList<>();
						
						do {start++;
							int		startName = start = skipBlank(data,start), endName = start = skipQualifiedName(data,start);

							final Class<?>	parent = cdr.getClassDescription(data,startName,endName);

							if ((parent.getModifiers() & Constants.ACC_INTERFACE) == 0) {
								throw new AsmSyntaxException("Extends item ["+new String(data,startName,endName-startName)+"] references to the class, not interface!"); 
							}
							else {
								implementsNames.add(tree.placeName(parent.getName(),null));
							}
							start = skipBlank(data,start);
						} while(data[start] == ',');
					}
					break;
				default :
					throw new AsmSyntaxException("'Interface' definition contains unknown or unsupported option ["+new String(data,startOption,endOption-startOption)+"]!");
			}
			startOption = start = skipBlank(data,start);
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
	
	private void processClassFieldDir(final long id, final char[] data, int start) throws IOException {
		short			flags = 0;
		final int		startName = start = skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		
		if (type == void.class) {
			throw new AsmSyntaxException("Type 'void' is invalid for using with fields"); 
		}
		else {
			final long	typeId = tree.placeName(InternalUtils.buildFieldSignature(tree,tree.placeName(type.getName(),null)),null);
			int			startOption = start = skipBlank(data,start), endOption;
			
			while (data[start] != '\n') {
				endOption = start = skipSimpleName(data,start);
				switch ((int)staticDirectiveTree.seekName(data,startOption,endOption)) {
					case OPTION_PUBLIC		: flags = addAndCheckDuplicates(flags,Constants.ACC_PUBLIC,"public","field"); break;
					case OPTION_PROTECTED	: flags = addAndCheckDuplicates(flags,Constants.ACC_PROTECTED,"protected","field"); break;
					case OPTION_PRIVATE		: flags = addAndCheckDuplicates(flags,Constants.ACC_PRIVATE,"private","field"); break;
					case OPTION_STATIC		: flags = addAndCheckDuplicates(flags,Constants.ACC_STATIC,"static","field"); break;
					case OPTION_FINAL		: flags = addAndCheckDuplicates(flags,Constants.ACC_FINAL,"final","field"); break;
					case OPTION_VOLATILE	: flags = addAndCheckDuplicates(flags,Constants.ACC_VOLATILE,"volatile","field"); break;
					case OPTION_TRANSIENT	: flags = addAndCheckDuplicates(flags,Constants.ACC_TRANSIENT,"transient","field"); break;
					case OPTION_SYNTHETIC	: flags = addAndCheckDuplicates(flags,Constants.ACC_SYNTHETIC,"synthetic","field"); break;
					default : throw new AsmSyntaxException("'Field' definition for class field contains unknown or unsupported option ["+new String(data,startOption,endOption-startOption)+"]!");
				}
				startOption = start = skipBlank(data,start);
			}
			if  (checkMutualPrivateProtectedPublic(flags)) {
				throw new AsmSyntaxException("Mutually exclusive options (public/protected/private) in the 'class' directive!");
			}
			else {
				cc.addFieldDescription(flags,id,typeId);
				cc.getConstantPool().asFieldRefDescription(joinedClassNameId,id,typeId);
			}
		}
	}

	private void processInterfaceFieldDir(final long id, final char[] data, int start) throws IOException {
		short			flags = 0;
		final int		startName = start = skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		
		if (type == void.class) {
			throw new AsmSyntaxException("Type 'void' is invalid for using with fields"); 
		}
		else {
			final long	typeId = tree.placeName(cdr.getClassDescription(data,startName,endName).getName(),null);
			int			startOption = start = skipBlank(data,start), endOption;
			
			while (data[start] != '\n') {
				endOption = start = skipSimpleName(data,start);
				switch ((int)staticDirectiveTree.seekName(data,startOption,endOption)) {
					case OPTION_PUBLIC		: flags = addAndCheckDuplicates(flags,Constants.ACC_PUBLIC,"public","field"); break;
					case OPTION_STATIC		: flags = addAndCheckDuplicates(flags,Constants.ACC_STATIC,"static","field"); break;
					case OPTION_FINAL		: flags = addAndCheckDuplicates(flags,Constants.ACC_FINAL,"final","field"); break;
					default : throw new AsmSyntaxException("'Field' definition for class field contains unknown or unsupported option ["+new String(data,startOption,endOption-startOption)+"]!");
				}
				startOption = start = skipBlank(data,start);
			}
			cc.addFieldDescription((short) (flags | Constants.ACC_PUBLIC | Constants.ACC_STATIC | Constants.ACC_FINAL),id,typeId);
		}
	}
	
	private void processClassMethodDir(final long id, final char[] data, int start) throws IOException {
		short			flags = 0;
		final int		startName = start = skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		final long		typeId = tree.placeName(type.getName(),null);
		int				startOption = start = skipBlank(data,start), endOption;
		List<Long>		throwsNames = null;
		
		while (data[start] != '\n') {
			endOption = start = skipSimpleName(data,start);
			switch ((int)staticDirectiveTree.seekName(data,startOption,endOption)) {
				case OPTION_PUBLIC		: flags = addAndCheckDuplicates(flags,Constants.ACC_PUBLIC,"public","method"); break;
				case OPTION_PROTECTED	: flags = addAndCheckDuplicates(flags,Constants.ACC_PROTECTED,"protected","method"); break;
				case OPTION_PRIVATE		: flags = addAndCheckDuplicates(flags,Constants.ACC_PRIVATE,"private","method"); break;
				case OPTION_STATIC		: flags = addAndCheckDuplicates(flags,Constants.ACC_STATIC,"static","method"); break;
				case OPTION_FINAL		: flags = addAndCheckDuplicates(flags,Constants.ACC_FINAL,"final","method"); break;
				case OPTION_SYNCHRONIZED: flags = addAndCheckDuplicates(flags,Constants.ACC_SYNCHRONIZED,"synchronized","method"); break;
				case OPTION_BRIDGE		: flags = addAndCheckDuplicates(flags,Constants.ACC_BRIDGE,"bridge","method"); break;
				case OPTION_VARARGS		: flags = addAndCheckDuplicates(flags,Constants.ACC_VARARGS,"varargs","method"); break;
				case OPTION_NATIVE		: flags = addAndCheckDuplicates(flags,Constants.ACC_NATIVE,"native","method"); break;
				case OPTION_ABSTRACT	: flags = addAndCheckDuplicates(flags,Constants.ACC_ABSTRACT,"abstract","method"); break;
				case OPTION_STRICT		: flags = addAndCheckDuplicates(flags,Constants.ACC_STRICT,"strict","method"); break;
				case OPTION_SYNTHETIC	: flags = addAndCheckDuplicates(flags,Constants.ACC_SYNTHETIC,"synthetic","method"); break;
				case OPTION_THROWS	:
					if (throwsNames != null) {
						throw new AsmSyntaxException("Duplicate option 'throws' in the 'method' directive!");
					}
					else {
						throwsNames = new ArrayList<>();
						
						do {start++;
							int		startException = start = skipBlank(data,start), endException = start = skipQualifiedName(data,start);

							final Class<?>	exception = cdr.getClassDescription(data,startException,endException);
							
							if (Throwable.class.isAssignableFrom(exception)) {
								throw new AsmSyntaxException("Throws item ["+new String(data,startException,endException-startException)+"] - class referenced is not a Throwable or it's child!"); 
							}
							else {
								throwsNames.add(tree.placeName(exception.getName(),null));
							}
							start = skipBlank(data,start);
						} while(data[start] == ',');
					}
					break;
				default : throw new AsmSyntaxException("'Field' definition contains unknown or unsupported option ["+new String(data,startOption,endOption)+"]!");
			}
			startOption = start = skipBlank(data,start);
		}
		if  (checkMutualPrivateProtectedPublic(flags)) {
			throw new AsmSyntaxException("Mutually exclusive options (public/protected/private) in the 'class' directive!");
		}
		else if  (checkMutualAbstractFinal(flags)) {
			throw new AsmSyntaxException("Mutually exclusive options (abstract/final) in the 'class' directive!");
		}
		else if  (checkMutualStaticAbstract(flags)) {
			throw new AsmSyntaxException("Mutually exclusive options (static/abstract) in the 'class' directive!");
		}
		else if ((classFlags & Constants.ACC_ABSTRACT) == 0 && (flags & Constants.ACC_ABSTRACT) != 0) {
			throw new AsmSyntaxException("Attempt to add abstract method to the non-abstract class!");
		}
		else {
			if (id == classNameId) {	// This is a constructor!
				if (typeId != voidId) {
					throw new AsmSyntaxException("Constructor method need return void type!");
				}
				else {
					methodNameId = constructorId;
				}
			}
			else {
				methodNameId = id;
			}
			if (throwsNames != null) {
				final long[] 	throwsList = new long[throwsNames.size()];
				
				for (int index = 0; index < throwsList.length; index++) {
					throwsList[index] = throwsNames.get(index);
				}
				methodDescriptor = cc.addMethodDescription(flags,methodNameId,typeId,throwsList);
			}
			else {
				methodDescriptor = cc.addMethodDescription(flags,methodNameId,typeId);
			}
		}
	}
	
	private void processInterfaceMethodDir(final long id, final char[] data, int start) throws IOException {
		short			flags = 0;
		final int		startName = start = skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		final long		typeId = tree.placeName(type.getName(),null);
		int				startOption = start = skipBlank(data,start), endOption;
		List<Long>		throwsNames = null;
		
		while (data[start] != '\n') {
			endOption = start = skipSimpleName(data,start);
			switch ((int)staticDirectiveTree.seekName(data,startOption,endOption)) {
				case OPTION_PUBLIC		: flags = addAndCheckDuplicates(flags,Constants.ACC_PUBLIC,"public","method"); break;
				case OPTION_VARARGS		: flags = addAndCheckDuplicates(flags,Constants.ACC_VARARGS,"varargs","method"); break;
				case OPTION_SYNTHETIC	: flags = addAndCheckDuplicates(flags,Constants.ACC_SYNTHETIC,"synthetic","method"); break;
				case OPTION_THROWS	:
					if (throwsNames != null) {
						throw new AsmSyntaxException("Duplicate option 'throws' in the 'method' directive!");
					}
					else {
						throwsNames = new ArrayList<>();
						
						do {start++;
							int		startException = start = skipBlank(data,start), endException = start = skipQualifiedName(data,start);

							final Class<?>	exception = cdr.getClassDescription(data,startException,endException);
							
							if (Throwable.class.isAssignableFrom(exception)) {
								throw new AsmSyntaxException("Throws item ["+new String(data,startException,endException-startException)+"] - class referenced is not a Throwable or it's child!"); 
							}
							else {
								throwsNames.add(tree.placeName(exception.getName(),null));
							}
							start = skipBlank(data,start);
						} while(data[start] == ',');
					}
					break;
				default : throw new AsmSyntaxException("'Field' definition contains unknown or unsupported option ["+new String(data,startOption,endOption)+"]!");
			}
			startOption = start = skipBlank(data,start);
		}
		if  (checkMutualPrivateProtectedPublic(flags)) {
			throw new AsmSyntaxException("Mutually exclusive options (public/protected/private) in the 'class' directive!");
		}
		else if  (checkMutualAbstractFinal(flags)) {
			throw new AsmSyntaxException("Mutually exclusive options (abstract/final) in the 'class' directive!");
		}
		else {
			flags |= Constants.ACC_ABSTRACT | Constants.ACC_PUBLIC; 
			if (throwsNames != null) {
				final long[] 	throwsList = new long[throwsNames.size()];
				
				for (int index = 0; index < throwsList.length; index++) {
					throwsList[index] = throwsNames.get(index);
				}
				methodDescriptor = cc.addMethodDescription(flags,id,typeId,throwsList);
			}
			else {
				methodDescriptor = cc.addMethodDescription(flags,id,typeId);
			}
			methodNameId = id;
		}
	}

	private void processParameterDir(final long id, final char[] data, int start) throws AsmSyntaxException {
		short			flags = 0;
		final int		startName = start = skipBlank(data,start), endName = start = skipQualifiedName(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		
		if (type == void.class) {
			throw new AsmSyntaxException("Type 'void' is invalid for using with parameters"); 
		}
		else {
			final long	typeId = tree.placeName(type.getName(),null);
			int			startOption = start = skipBlank(data,start), endOption;
		
			while (data[start] != '\n') {
				endOption = start = skipSimpleName(data,start);
				switch ((int)staticDirectiveTree.seekName(data,startOption,endOption)) {
					case OPTION_FINAL		: flags = addAndCheckDuplicates(flags,Constants.ACC_FINAL,"final","parameter"); break;
					case OPTION_SYNTHETIC	: flags = addAndCheckDuplicates(flags,Constants.ACC_SYNTHETIC,"synthetic","parameter"); break;
					default : throw new AsmSyntaxException("'Parameter' definition contains unknown or unsupported option ["+new String(data,startOption,endOption)+"]!");
				}
				startOption = start = skipBlank(data,start);
			}
			methodDescriptor.addParameterDeclaration(flags,id,typeId);
		}
	}

	private void processVarDir(final long id, final char[] data, int start) throws IOException {
		short			flags = 0;
		final int		startName = start = skipBlank(data,start), endName = start = skipQualifiedName(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		
		if (type == void.class) {
			throw new AsmSyntaxException("Type 'void' is invalid for using with parameters"); 
		}
		else {
			final long	typeId = tree.placeName(type.getName(),null);
			int			startOption = start = skipBlank(data,start), endOption;
		
			while (data[start] != '\n') {
				endOption = start = skipSimpleName(data,start);
				switch ((int)staticDirectiveTree.seekName(data,startOption,endOption)) {
					case OPTION_FINAL		: flags = addAndCheckDuplicates(flags,Constants.ACC_FINAL,"final","var"); break;
					case OPTION_SYNTHETIC	: flags = addAndCheckDuplicates(flags,Constants.ACC_SYNTHETIC,"synthetic","var"); break;
					default : throw new AsmSyntaxException("'Var' definition contains unknown or unsupported option ["+new String(data,startOption,endOption)+"]!");
				}
				startOption = start = skipBlank(data,start);
			}
			methodDescriptor.addVarDeclaration(flags,id,typeId);
		}
	}

	private void processStackDir(final char[] data, int start) throws AsmSyntaxException {
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
				
				start = CharsUtil.parseNumber(data,startName,size,CharsUtil.PREF_INT,true);
				if (size[1] == CharsUtil.PREF_INT) {
					methodDescriptor.setStackSize((short) size[0]);
				}
				else {
					throw new AsmSyntaxException("Stack size is not an integer constant (possibly it's size is long, float or double)");
				}
				break;
		}
		skip2line(data,start);
	}
	
	private void processPackageDir(final char[] data, int start) throws AsmSyntaxException {
		final int	endPackage = skipQualifiedName(data,start);
			
		packageId = tree.placeName(data,start,endPackage,null);
		skip2line(data,endPackage);
	}

	private void processImportDir(final char[] data, int start) throws AsmSyntaxException {
		final String	className = new String(data,start,skipQualifiedName(data,start) - start);  

		try{cdr.addDescription(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new AsmSyntaxException("Class description ["+className+"] is unknown in the actual class loader. Test the class name you want to import and/or make it aacessible for the class loader");
		}				
		skip2line(data,start+className.length());
	}

	private void processIncludeDir(final char[] data, int start, final int end) throws AsmSyntaxException {
		if (start < end && data[start] == '\"') {
			int	startQuoted = start + 1;
			
			start = skipQuoted(data,startQuoted);
			skip2line(data,start+1);
			
			final String	ref = new String(data,startQuoted,start-startQuoted-1);
			URL		refUrl;
			
			try{refUrl = new URL(ref);
			} catch (MalformedURLException exc) {
				refUrl = this.getClass().getResource(ref);
			}
			if (refUrl == null) {
				throw new AsmSyntaxException("Resource URL ["+ref+"] is missing or malformed!");
			}
			else {
				int lineNo = 0;
				
				try(final LineByLineProcessor	lbl = new LineByLineProcessor(this);
					final InputStream		is = refUrl.openStream();
					final Reader			rdr = new InputStreamReader(is)) {

					lbl.write(rdr);
				}
				catch (IOException | SyntaxException exc) {
					throw new AsmSyntaxException("Source ["+refUrl+"], line "+lineNo+": I/O error reading data ("+exc.getMessage()+")");
				}
			}
		}
		else {
			throw new AsmSyntaxException("Missing quota!");
		}
	}

	/*
	 * Process try blocks
	 */
	
	private boolean areTryBlocksClosed() {
		return tryList.size() == 0;
	}

	private void pushTryBlock() throws IOException {
		tryList.add(0,new int[]{getPC(),-1});
	}

	private void processCatch(final char[] data, int from, final int to) throws IOException {
		if (tryList.size() == 0) {
			throw new AsmSyntaxException(".catch without .try");
		}
		else {
			if (tryList.get(0)[1] == -1) {
				tryList.get(0)[1] = getPC();
			}
			if (Character.isJavaIdentifierStart(data[from])) {
				from--;
				do {final int		startException = from+1, endException = skipQualifiedName(data,startException);
					final Class<?>	exception = cdr.getClassDescription(data,startException,endException);
					final long		exceptionId = tree.placeName(exception.getName().replace('.','/'),null);

					methodDescriptor.addExceptionRecord((short)tryList.get(0)[0],(short)tryList.get(0)[1],cc.getConstantPool().asClassDescription(exceptionId),(short)getPC());
					from = skipBlank(data,endException);
				} while (data[from] == ',');
			}
			else {
				methodDescriptor.addExceptionRecord((short)tryList.get(0)[0],(short)tryList.get(0)[1],(short)0,(short)getPC());
			}
			skip2line(data, from);
		}
	}

	private void popTryBlock(int lineNo) throws AsmSyntaxException {
		if (tryList.size() == 0) {
			throw new AsmSyntaxException(".endtry without .try");
		}
		else {
			tryList.remove(0);
		}
	}
	
	/*
	 * Process commands
	 */
	
	private void processSingleCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException {
		putCommand((byte)desc.operation);
		skip2line(data,start);
	}

	private void processByteIndexCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandAddress) throws IOException {
		final long	forResult[] = new long[2];
		
		start = calculateLocalAddress(data,start,forResult);
		if (forResult[0] > methodDescriptor.getLocalFrameSize()) {
			throw new AsmSyntaxException("Calculated address value ["+forResult[0]+"] is outside the method local frame size ["+methodDescriptor.getLocalFrameSize()+"]");
		}
		else if (forResult[0] >= 256) {
			if (!expandAddress) {
				throw new AsmSyntaxException("Calculated address value ["+forResult[0]+"] occupies more tham 1 byte! Use 'wide' command for those addresses");
			}
			else {
				putCommand((byte)desc.operation,(byte)(forResult[0] >> 8),(byte)forResult[0]);
			}
		}
		else {
			putCommand((byte)desc.operation,(byte)forResult[0]);
		}
		skip2line(data,start);
	}

	private void processByteValueCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandValue) throws IOException {
		final long	forResult[] = new long[2];
		
		start = calculateValue(data,start,EvalState.additional,forResult);
		if (forResult[0] < Byte.MIN_VALUE || forResult[0] >= 256) {
			if (!expandValue) {
				throw new AsmSyntaxException("Calculated value value ["+forResult[0]+"] occupies more tham 1 byte! Use 'wide' command for those values");
			}
			else {
				putCommand((byte)desc.operation,(byte)(forResult[0] >> 8),(byte)forResult[0]);
			}
		}
		else {
			putCommand((byte)desc.operation,(byte)forResult[0]);
		}
		skip2line(data,start);
	}

	private void processByteIndexAndByteValueCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandAddress) throws IOException {
		final long[]	forIndex= new long[2], forValue = new long[2];
		
		start = skipBlank(data, calculateLocalAddress(data,start,forIndex));
		if (data[start] == ',') {
			start = calculateValue(data,skipBlank(data,start+1),EvalState.additional,forValue);
		}

		if (forIndex[0] > methodDescriptor.getLocalFrameSize()) {
			throw new AsmSyntaxException("Calculated address value ["+forIndex[0]+"] is outside the method local frame size ["+methodDescriptor.getLocalFrameSize()+"]");
		}
		else if (forIndex[0] >= 256) {
			if (!expandAddress) {
				throw new AsmSyntaxException("Calculated address value ["+forIndex[0]+"] occupies more tham 1 byte! Use 'wide' command for those addresses");
			}
		}
		if (forValue[0] < Byte.MIN_VALUE || forValue[0] >= 256) {
			if (!expandAddress) {
				throw new AsmSyntaxException("Calculated value value ["+forValue[0]+"] occupies more tham 1 byte! Use 'wide' command for those values");
			}
		}
		if (expandAddress) {
			putCommand((byte)desc.operation,(byte)(forIndex[0]>>8),(byte)forIndex[0],(byte)(forValue[0]>>8),(byte)forValue[0]);
		}
		else {
			putCommand((byte)desc.operation,(byte)forIndex[0],(byte)forValue[0]);
		}
		skip2line(data,start);
	}
	
	private void processByteTypeCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException {
		final int	startType = start, endType = start = skipSimpleName(data,start);
		final long	typeId = staticDirectiveTree.seekName(data,startType,endType);

		if (typeId >= T_BASE && typeId <= T_BASE_END) {
			putCommand((byte)desc.operation,(byte)(typeId-T_BASE));
			skip2line(data,start);
		}
		else {
			throw new AsmSyntaxException("Unknown primitive type ["+new String(data,startType,endType-startType)+"]"); 
		}
	}

	private void processClassShortIndexCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException {
		final int		startName = start = skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		final long		typeId = tree.placeName(type.getName().replace('.','/'),null);
		final short		typeDispl = cc.getConstantPool().asClassDescription(typeId);

		putCommand((byte)desc.operation,(byte)((typeDispl >> 8) & 0xFF),(byte)(typeDispl & 0xFF));
		skip2line(data,start);		
	}

	private void processValueByteIndexCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException {
		short		displ[] = new short[1];
		
		start = processValueShortIndexCommand(data,start,displ);

		if (displ[0] < 0 || displ[0] > 2*Byte.MAX_VALUE) {
			throw new AsmSyntaxException("Calculated value ["+displ[0]+"] is too long for byte index");
		}
		else {
			putCommand((byte)desc.operation,(byte)(displ[0] & 0xFF));
			skip2line(data,start);
		}
	}
	
	private void processValueShortIndexCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException {
		short		displ[] = new short[1];
		
		start = processValueShortIndexCommand(data,start,displ);

		if (displ[0] < 0 || displ[0] > 2*Short.MAX_VALUE) {
			throw new AsmSyntaxException("Calculated value ["+displ[0]+"] is too long for short index");
		}
		else {
			putCommand((byte)desc.operation,(byte)((displ[0] >> 8) & 0xFF),(byte)(displ[0] & 0xFF));
			skip2line(data,start);
		}
	}

	private int processValueShortIndexCommand(final char[] data, int start, final short[] result) throws IOException {
		short		displ;

		try{switch (data[start]) {
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' : case '-' :
					final long	forResult[] = new long[]{0,0};
					int			sign = 1;
					
					if (data[start] == '-') {
						sign = -1;
						start++;
					}
					start = CharsUtil.parseNumber(data,start,forResult,CharsUtil.PREF_INT|CharsUtil.PREF_FLOAT,true);
					
					if (forResult[1] == CharsUtil.PREF_FLOAT) {
						displ = cc.getConstantPool().asFloatDescription(sign*Float.intBitsToFloat((int) forResult[0]));
					}
					else if (forResult[1] == CharsUtil.PREF_INT) {
						displ = cc.getConstantPool().asIntegerDescription((int)(sign*forResult[0]));
					}
					else {
						throw new AsmSyntaxException("Illegal numeric constant size (only int and float are available here)");
					}		
					break;
				case '\"' :
					final int	startString = start + 1, endString = start = skipQuoted(data,startString);
					
					start++;
					displ = cc.getConstantPool().asStringDescription(cc.getNameTree().placeName(data,startString,endString,null));
					break;
				case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' : case 'g' : case 'h' :  case 'i' : case 'j' :
				case 'k' : case 'l' : case 'm' : case 'n' : case 'o' : case 'p' : case 'q' : case 'r' :  case 's' : case 't' :
				case 'u' : case 'v' : case 'w' : case 'x' : case 'y' : case 'z' :
				case 'A' : case 'B' : case 'C' : case 'D' : case 'E' : case 'F' : case 'G' : case 'H' :  case 'I' : case 'J' :
				case 'K' : case 'L' : case 'M' : case 'N' : case 'O' : case 'P' : case 'Q' : case 'R' :  case 'S' : case 'T' :
				case 'U' : case 'V' : case 'W' : case 'X' : case 'Y' : case 'Z' :
					displ = 0;
					break;
				default :
					throw new AsmSyntaxException("Illegal lexema. Only int/float constant, string literal, class or method reference are available here");
			}
		} catch (NumberFormatException exc) {
			throw new AsmSyntaxException("Illegal number: "+exc);
		}
		result[0] = displ;
		return start;
	}
	
	private void processValueShortIndex2Command(final CommandDescriptor desc, final char[] data, int start) throws IOException {
		final long	forResult[] = new long[]{0,0};
		int			sign = 1;
		short		displ;

		try{if (data[start] == '-') {
				sign = -1;
				start++;
			}
			start = CharsUtil.parseNumber(data, start, forResult, CharsUtil.PREF_LONG|CharsUtil.PREF_DOUBLE,true);
			if (forResult[1] == CharsUtil.PREF_DOUBLE) {
				displ = cc.getConstantPool().asDoubleDescription(sign*Double.longBitsToDouble(forResult[0]));
			}
			else if (forResult[1] == CharsUtil.PREF_LONG) {
				displ = cc.getConstantPool().asLongDescription(sign*forResult[0]);
			}
			else {
				throw new AsmSyntaxException("Illegal numeric constant size (only long and double are available here)");
			}
			if (displ < 0 || displ > 2*Short.MAX_VALUE) {
				throw new AsmSyntaxException("Calculated value ["+displ+"] is too long for short index");
			}
			else {
				putCommand((byte)desc.operation,(byte)((displ >> 8) & 0xFF),(byte)(displ & 0xFF));
				skip2line(data,start);
			}
		} catch (NumberFormatException exc) {
			throw new AsmSyntaxException("Illegal number: "+exc);
		}
	}
	
	private void processShortValueCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandValue) throws IOException {
		final long	forResult[] = new long[1];
		
		start = calculateValue(data,start,EvalState.additional,forResult);
		if (forResult[0] < Short.MIN_VALUE || forResult[0] >= 65536) {
			if (!expandValue) {
				throw new AsmSyntaxException("Calculated value value ["+forResult[0]+"] occupies more than 2 byte! Use 'wide' command for those values");
			}
			else {
				putCommand((byte)desc.operation,(byte)(forResult[0] >> 24),(byte)(forResult[0] >> 16),(byte)(forResult[0] >> 8),(byte)forResult[0]);
			}
		}
		else {
			putCommand((byte)desc.operation,(byte)(forResult[0]>>8),(byte)forResult[0]);
		}
		skip2line(data,start);
	}

	private void processShortIndexAndByteValueCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException {
		final int		startName = start = skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		final long		typeId = tree.placeName(type.getName().replace('.','/'),null);
		final short		typeDispl = cc.getConstantPool().asClassDescription(typeId);
		final long[]	forValue = new long[1];
		
		start = skipBlank(data,endName);
		if (data[start] == ',') {
			start = calculateValue(data,skipBlank(data,start+1),EvalState.additional,forValue);
	 		if (forValue[0] <= 0 || forValue[0] >= 2 * Byte.MAX_VALUE) {
				throw new AsmSyntaxException("Calculated value ["+forValue[0]+"] is too long for byte value");
			}
			else {
				putCommand((byte)desc.operation,(byte)((typeDispl >> 8) & 0xFF),(byte)(typeDispl & 0xFF),(byte)forValue[0]);
				skip2line(data,start);
			}
		}
		else {
			throw new AsmSyntaxException("Missing comma and dimension parameter");
		}
	}

	private void processShortGlobalIndexCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException {
		final int	forResult[] = new int[1];
		
		start = calculateFieldAddress(data,start,end,forResult);
		if (forResult[0] <= 0 || forResult[0] > Short.MAX_VALUE) {
			throw new AsmSyntaxException("Calculated value ["+forResult[0]+"] is too long for short index");
		}
		else {
			putCommand((byte)desc.operation,(byte)((forResult[0] >> 8) & 0xFF),(byte)(forResult[0] & 0xFF));
			skip2line(data,start);
		}
	}

	private void processShortBrunchCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException {
		final long	forResult[] = new long[1];
		
		start = calculateBranchAddress(data,start,forResult);
		registerBranch(forResult[0],true);
		putCommand((byte)desc.operation,(byte)0,(byte)0);
		skip2line(data,start);
	}

	private void processLongBrunchCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException {
		final long	forResult[] = new long[2];
		
		start = calculateBranchAddress(data,start,forResult);
		registerBranch(forResult[0],false);
		putCommand((byte)desc.operation,(byte)0,(byte)0,(byte)0,(byte)0);
		skip2line(data,start);
	}

	private void processCallCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException {
		final int	forResult[] = new int[1];
		
		start = calculateMethodAddress(data,start,end,forResult);
		if (forResult[0] <= 0 || forResult[0] > 2*Short.MAX_VALUE) {
			throw new AsmSyntaxException("Calculated value ["+forResult[0]+"] is too long for short index");
		}
		else {
			putCommand((byte)desc.operation,(byte)((forResult[0] >> 8) & 0xFF),(byte)(forResult[0] & 0xFF));
			skip2line(data,start);
		}
	}	

	private void processDynamicCallCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException {
		throw new AsmSyntaxException("Don't use invokedynamic connand! Use direct link to the methods you need instead"); 
	}

	private void processInterfaceCallCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException {
		processCallCommand(desc,data,start,end);
		putCommand((byte)1,(byte)0);
	}

	private void processWideCommand(final CommandDescriptor desc, final char[] data, int start, final int end) {
		throw new UnsupportedOperationException("Wide command is not implemented yet!"); 
	}

	private void processJumps(final char[] data, int start, final int end, final boolean explicitValue) throws AsmSyntaxException {
		final long[]	forLabel = new long[1];
		final long[]	forValue = new long[1];
		
		if (explicitValue) {
			start = calculateValue(data,start,EvalState.additional,forValue);
			if (start < end && data[start] == ',') {
				start = skipBlank(data, calculateBranchAddress(data,start+1,forLabel));
			}
			else {
				throw new AsmSyntaxException("Missing comma in branch list");
			}
		}
		else {
			forValue[0] = DEFAULT_MARK;
			start = skipBlank(data, calculateBranchAddress(data,start,forLabel));
		}
		if ((forValue[0] < Integer.MIN_VALUE  || forValue[0] > Integer.MAX_VALUE) && forValue[0] != DEFAULT_MARK) {
			throw new AsmSyntaxException("Calculated value ["+forValue[0]+"] is too long for byte value");
		}
		else {
			if (jumps.containsKey(forValue[0])) {
				throw new AsmSyntaxException("Duplicate switch value ["+(forValue[0] == DEFAULT_MARK ? "default" : ""+forValue[0])+"] in the lookupswitch/tableswitch command");
			}
			else {
				jumps.put(forValue[0],forLabel[0]);
			}
			skip2line(data,start);
		}
	}

	private void fillTable() throws IOException {
		if (jumps.size() == 0) {
			throw new AsmSyntaxException("No any jumps for the tableswitch command were defined!");
		}
		else if (!jumps.containsKey(DEFAULT_MARK)) {
			throw new AsmSyntaxException("No default jump for the tableswitch command was defined!");
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
				throw new AsmSyntaxException("jump table utilizied less than 5%! Use lookupswitch instead of tableswitch command!");
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

	private void fillLookup() throws IOException {
		if (jumps.size() == 0) {
			throw new AsmSyntaxException("No any jumps for the tableswitch command were defined!");
		}
		else if (!jumps.containsKey(DEFAULT_MARK)) {
			throw new AsmSyntaxException("No default jump for the tableswitch command was defined!");
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

	private int calculateLocalAddress(final char[] data, int start, final long[] result) throws AsmSyntaxException {
		if (data[start] >= '0' && data[start] <= '9') {
			return CharsUtil.parseNumber(data,start,result,CharsUtil.PREF_INT,true);
		}
		else {
			final int	startName = start, endName = start = skipSimpleName(data,start);
			
			result[0] = methodDescriptor.getVarDispl(tree.placeName(data,startName,endName,null));
			return endName;
		}
	}
	
	private int calculateFieldAddress(final char[] data, int start, final int end, final int[] result) throws IOException {
		int	startName = start, endName = start = skipSimpleName(data,start);
		
		if (data[start] != '.') {
			if ((result[0] = cc.getConstantPool().asFieldRefDescription(joinedClassNameId,tree.placeName(data,startName,endName,null))) != 0) {
				return start;
			}
		}
		endName = start = skipQualifiedName(data,start);
		final Field	f = cdr.getFieldDescription(data,startName,endName);

		result[0] = cc.getConstantPool().asFieldRefDescription(
							tree.placeName(f.getDeclaringClass().getName(),null),
							tree.placeName(f.getName(),null),
							tree.placeName(f.getType().getName(),null)
					);	
		return start;
	}

	private int calculateMethodAddress(final char[] data, int start, final int end, final int[] result) throws IOException {
		int			startName = start, endName = start = skipSimpleName(data,start);
		
		if (data[start] == '.') {
			endName = start = skipQualifiedName(data,startName);
			
			if (data[start] == '(') {
				final int		endSignature = start = skipSignature(data,start);
				
				try{final Method	m = cdr.getMethodDescription(data,startName,endSignature);
				
					if (m.getDeclaringClass().isInterface()) {
						result[0] = cc.getConstantPool().asInterfaceMethodRefDescription(
								tree.placeName(m.getDeclaringClass().getCanonicalName().replace('.','/'),null),
								tree.placeName(m.getName(),null),
								tree.placeName(InternalUtils.buildSignature(m),null)
						);
					}
					else {
						result[0] = cc.getConstantPool().asMethodRefDescription(
								tree.placeName(m.getDeclaringClass().getCanonicalName().replace('.','/'),null),
								tree.placeName(m.getName(),null),
								tree.placeName(InternalUtils.buildSignature(m),null)
						);
					}
				} catch (AsmSyntaxException exc) {
					final Constructor<?>	c = cdr.getConstructorDescription(data,startName,endSignature);
					
					result[0] = cc.getConstantPool().asMethodRefDescription(
							tree.placeName(c.getDeclaringClass().getCanonicalName().replace('.','/'),null),
							tree.placeName("<init>",null),
							tree.placeName(InternalUtils.buildSignature(c),null)
					);
				}
			}
			else {
				throw new AsmSyntaxException("Missing method signature!");
			}
		}
		else {
			if (data[start] == '(') {
				final int	startSignature = start, endSignature = start = skipSignature(data,start);
				
				result[0] = cc.getConstantPool().asMethodRefDescription(
						joinedClassNameId,
						tree.placeName(data,startName,endName,null),
						tree.placeName(data,startSignature,endSignature,null)						
				);
			}
			else {
				throw new AsmSyntaxException("Missing method signature!");
			}
		}
		return start;
	}
	
	private int calculateValue(final char[] data, int start, final EvalState state, final long[] result) throws AsmSyntaxException {
		long		value[] = new long[]{0,0};
		char		symbol;
		
		switch (state) {
			case term				:
				switch (data[start]) {
					case '0' : 
						return CharsUtil.parseLongExtended(data,start,result,true);
					case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						return CharsUtil.parseLong(data,start,result,true);
					case '(' :
						if (data[start = skipBlank(data,calculateValue(data,skipBlank(data,start+1),EvalState.additional,result))]==')') {
							return skipBlank(data,start+1);
						}
						else {
							throw new AsmSyntaxException("Unclosed ')' in the expression");
						}
					default :
						if (Character.isJavaIdentifierStart(data[start])) {
							start = skipSimpleName(data,start);
						}
						throw new AsmSyntaxException("Non-constant value in the expression");
				}
			case unary				:
				if (data[start] == '-') {
					start = skipBlank(data,calculateValue(data,skipBlank(data,start+1),EvalState.term,result));
					result[0] = -result[0];
				}
				else {
					start = skipBlank(data,calculateValue(data,start,EvalState.term,result));
				}
				return start;
			case multiplicational	:
				start = skipBlank(data,calculateValue(data,start,EvalState.unary,result));
				while ((symbol = data[start]) == '*' || symbol == '/' || symbol == '%') {
					start = skipBlank(data,calculateValue(data,skipBlank(data,start+1),EvalState.unary,value));
					switch (symbol) {
						case '*' : result[0] *= value[0]; break;
						case '/' : result[0] /= value[0]; break;
						case '%' : result[0] %= value[0]; break;
					}
				}
				return start;
			case additional			:
				start = skipBlank(data,calculateValue(data,start,EvalState.multiplicational,result));
				while ((symbol = data[start]) == '+' || symbol == '-') {
					start = skipBlank(data,calculateValue(data,skipBlank(data,start+1),EvalState.multiplicational,value));
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
		
		result[0] = cc.getNameTree().placeName(data,startName,endName,null);
		return start;
	}
	
	/*
	 * Utility methods
	 */
	private static short addAndCheckDuplicates(final short source, final short added, final String parameter, final String directive) throws AsmSyntaxException {
		if ((source & added) != 0) {
			throw new AsmSyntaxException("Duplicate option ["+parameter+"] in the ["+directive+"] directive");
		}
		else {
			return (short) (source | added);
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
	
	private static int skipBlank(final char[] data, int from) {
		char	symbol;
		
		while ((symbol = data[from]) <= ' ' && symbol != '\n') {
			if (symbol == '/' && data[from+1] == '/') {
				from += 2;
				while (data[from] != '\n') {
					from++;
				}
				return from;
			}
			else {
				from++;
			}
		}
		return from;
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

	private static int skipQuoted(final char[] data, int from) throws AsmSyntaxException {
		while (data[from] != '\n' && data[from] != '\"' ) {
			from++;
		}
		if (data[from] == '\"') {
			return from;
		}
		else {
			throw new AsmSyntaxException("Missing close quota!");
		}
	}

	private int skipSignature(final char[] data, int from) throws AsmSyntaxException {
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
				throw new AsmSyntaxException("Unclosed bracket ')' in the method signature");
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
						throw new AsmSyntaxException("Missing ';' in the class signature descriptor");
					}
				default :
					throw new AsmSyntaxException("Illegal signature symbol '"+data[from]+"' in the method signature");
			}
		}
	}

	private static void skip2line(final char[] data, int from) throws AsmSyntaxException {
		while (data[from] <= ' ' && data[from] != '\n') {
			from++;
		}
		if (data[from] == '\n') {
			return;
		}
		else if (data[from] == '/' && data[from+1] == '/') {
			return;
		}
		else {
			throw new AsmSyntaxException("Unparsed tail in the input string");
		}
	}

	/*
	 * Parser preparation methods
	 */
	
	private static void placeStaticDirective(final int code, final String mnemonics) {
		final char[]	mnemonicsChar = mnemonics.toCharArray();
		
		staticDirectiveTree.placeName(mnemonicsChar,0,mnemonicsChar.length,code,null);
	}

	private static void placeStaticCommand(final int operation, final int stackDelta, final String mnemonics) {
		placeStaticCommand(operation, stackDelta, mnemonics, CommandFormat.single);
	}
	
	private static void placeStaticCommand(final int operation, final int stackDelta, final String mnemonics, final CommandFormat format) {
		final char[]	mnemonicsChar = mnemonics.toCharArray();
		
		staticCommandTree.placeName(mnemonicsChar,0,mnemonicsChar.length,operation,new CommandDescriptor(operation,stackDelta,format));
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
	}
}
