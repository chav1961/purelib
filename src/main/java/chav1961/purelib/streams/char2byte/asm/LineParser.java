package chav1961.purelib.streams.char2byte.asm;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URI;
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
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.cdb.JavaByteCodeConstants;
import chav1961.purelib.streams.char2byte.asm.StackAndVarRepo.StackChanges;
import chav1961.purelib.streams.char2byte.asm.macro.MacroClassLoader;
import chav1961.purelib.streams.char2byte.asm.macro.MacroCompiler;
import chav1961.purelib.streams.char2byte.asm.macro.Macros;

class LineParser implements LineByLineProcessorCallback {
	static final char[]								TRUE = "true".toCharArray();
	static final char[]								FALSE = "false".toCharArray();
	static final char[]								VOID = "void".toCharArray();
	static final char[]								LONG = "long".toCharArray();
	static final char[]								DOUBLE = "double".toCharArray();
	static final char[]								THIS = "this".toCharArray();
	static final char[]								CALLSITE = CallSite.class.getCanonicalName().toCharArray();
	static final char[]								METHODHANDLE = MethodHandle.class.getCanonicalName().toCharArray();
	static final char[]								METHODHANDLESLOOKUP = MethodHandles.Lookup.class.getName().toCharArray();
	static final char[]								STRING = String.class.getName().toCharArray();
	static final char[]								METHODTYPE = MethodType.class.getCanonicalName().toCharArray();
	static final char[]								CONSTRUCTOR = "<init>".toCharArray();
	static final char[]								CLASS_CONSTRUCTOR = "<clinit>".toCharArray();

	static final short								SPECIAL_FLAG_BOOTSTRAP = 0x0001;
	
	private static final int						EXPONENT_BASE = 305;
	private static final double[]					EXPONENTS;
	private static final int						DONT_CHECK_LOCAL_TYPES = -1;

	
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
	private static final int						DIR_VARTABLE = 21;
	private static final int						DIR_FORWARD = 22;
	
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
	private static final int						OPTION_BOOTSTRAP = 119;
	
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
		placeStaticDirective(DIR_VARTABLE,m,".vartable");
		placeStaticDirective(DIR_FORWARD,m,".forward");
		
		placeStaticDirective(OPTION_PUBLIC,new DirectiveOption(JavaByteCodeConstants.ACC_PUBLIC),JavaByteCodeConstants.ACC_PUBLIC_NAME);
		placeStaticDirective(OPTION_FINAL,new DirectiveOption(JavaByteCodeConstants.ACC_FINAL),JavaByteCodeConstants.ACC_FINAL_NAME);
		placeStaticDirective(OPTION_ABSTRACT,new DirectiveOption(JavaByteCodeConstants.ACC_ABSTRACT),JavaByteCodeConstants.ACC_ABSTRACT_NAME);
		placeStaticDirective(OPTION_SYNTHETIC,new DirectiveOption(JavaByteCodeConstants.ACC_SYNTHETIC),JavaByteCodeConstants.ACC_SYNTHETIC_NAME);
		placeStaticDirective(OPTION_ENUM,new DirectiveOption(JavaByteCodeConstants.ACC_ENUM),JavaByteCodeConstants.ACC_ENUM_NAME);
		placeStaticDirective(OPTION_EXTENDS,new DirectiveClassOption(),"extends");
		placeStaticDirective(OPTION_IMPLEMENTS,new DirectiveInterfacesOption(),"implements");
		placeStaticDirective(OPTION_PRIVATE,new DirectiveOption(JavaByteCodeConstants.ACC_PRIVATE),JavaByteCodeConstants.ACC_PRIVATE_NAME);
		placeStaticDirective(OPTION_PROTECTED,new DirectiveOption(JavaByteCodeConstants.ACC_PROTECTED),JavaByteCodeConstants.ACC_PROTECTED_NAME);
		placeStaticDirective(OPTION_STATIC,new DirectiveOption(JavaByteCodeConstants.ACC_STATIC),JavaByteCodeConstants.ACC_STATIC_NAME);
		placeStaticDirective(OPTION_VOLATILE,new DirectiveOption(JavaByteCodeConstants.ACC_VOLATILE),JavaByteCodeConstants.ACC_VOLATILE_NAME);
		placeStaticDirective(OPTION_TRANSIENT,new DirectiveOption(JavaByteCodeConstants.ACC_TRANSIENT),JavaByteCodeConstants.ACC_TRANSIENT_NAME);
		placeStaticDirective(OPTION_SYNCHRONIZED,new DirectiveOption(JavaByteCodeConstants.ACC_SYNCHRONIZED),JavaByteCodeConstants.ACC_SYNCHRONIZED_NAME);
		placeStaticDirective(OPTION_BRIDGE,new DirectiveOption(JavaByteCodeConstants.ACC_BRIDGE),JavaByteCodeConstants.ACC_BRIDGE_NAME);
		placeStaticDirective(OPTION_VARARGS,new DirectiveOption(JavaByteCodeConstants.ACC_VARARGS),JavaByteCodeConstants.ACC_VARARGS_NAME);
		placeStaticDirective(OPTION_NATIVE,new DirectiveOption(JavaByteCodeConstants.ACC_NATIVE),JavaByteCodeConstants.ACC_NATIVE_NAME);
		placeStaticDirective(OPTION_STRICT,new DirectiveOption(JavaByteCodeConstants.ACC_STRICT),JavaByteCodeConstants.ACC_STRICT_NAME);
		placeStaticDirective(OPTION_THROWS,new DirectiveExceptionsOption(),"throws");
		placeStaticDirective(OPTION_BOOTSTRAP,new DirectiveSpecialFlag(SPECIAL_FLAG_BOOTSTRAP),"bootstrap");

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
		
		placeStaticCommand(0x32,false,"aaload",StackChanges.pop2AndPushReference,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x53,false,"aastore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x01,false,"aconst_null",StackChanges.pushReference);
		placeStaticCommand(0x19,false,"aload",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pushReference);
		placeStaticCommand(0x2a,false,"aload_0",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pushReference);
		placeStaticCommand(0x2b,false,"aload_1",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pushReference);
		placeStaticCommand(0x2c,false,"aload_2",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pushReference);
		placeStaticCommand(0x2d,false,"aload_3",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pushReference);
		placeStaticCommand(0xbd,false,"anewarray",CommandFormat.classShortIndex,StackChanges.popAndPushReference,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xb0,true,"areturn",StackChanges.clear,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xbe,false,"arraylength",StackChanges.popAndPushInt,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x3a,false,"astore",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x4b,false,"astore_0",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x4c,false,"astore_1",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x4d,false,"astore_2",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x4e,false,"astore_3",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xbf,true,"athrow",StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x33,false,"baload",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x54,false,"bastore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x10,false,"bipush",CommandFormat.byteValue,StackChanges.pushInt);
		placeStaticCommand(0x34,false,"caload",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x55,false,"castore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xc0,false,"checkcast",CommandFormat.classShortIndex,StackChanges.none,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x90,false,"d2f",StackChanges.changeDouble2Float,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x8e,false,"d2i",StackChanges.changeDouble2Int,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x8f,false,"d2l",StackChanges.changeDouble2Long,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x63,false,"dadd",StackChanges.pop4AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x31,false,"daload",StackChanges.pop2AndPushDouble,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x52,false,"dastore",StackChanges.pop4,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x98,false,"dcmpg",StackChanges.pop4AndPushInt,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x97,false,"dcmpl",StackChanges.pop4AndPushInt,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x0e,false,"dconst_0",StackChanges.pushDouble);
		placeStaticCommand(0x0f,false,"dconst_1",StackChanges.pushDouble);
		placeStaticCommand(0x6f,false,"ddiv",StackChanges.pop4AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x18,false,"dload",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pushDouble);
		placeStaticCommand(0x26,false,"dload_0",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pushDouble);
		placeStaticCommand(0x27,false,"dload_1",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pushDouble);
		placeStaticCommand(0x28,false,"dload_2",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pushDouble);
		placeStaticCommand(0x29,false,"dload_3",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pushDouble);
		placeStaticCommand(0x6b,false,"dmul",StackChanges.pop4AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x77,false,"dneg",StackChanges.pop2AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x73,false,"drem",StackChanges.pop4AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0xaf,true,"dreturn",StackChanges.clear,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x39,false,"dstore",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pop2,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x47,false,"dstore_0",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pop2,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x48,false,"dstore_1",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pop2,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x49,false,"dstore_2",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pop2,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x4a,false,"dstore_3",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pop2,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x67,false,"dsub",StackChanges.pop4AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x59,false,"dup",StackChanges.dup);
		placeStaticCommand(0x5a,false,"dup_x1",StackChanges.dup_x1);
		placeStaticCommand(0x5b,false,"dup_x2",StackChanges.dup_x2);
		placeStaticCommand(0x5c,false,"dup2",StackChanges.dup2);
		placeStaticCommand(0x5d,false,"dup2_x1",StackChanges.dup2_x1);
		placeStaticCommand(0x5e,false,"dup2_x2",StackChanges.dup2_x2);
		placeStaticCommand(0x8d,false,"f2d",StackChanges.changeFloat2Double,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x8b,false,"f2i",StackChanges.changeFloat2Int,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x8c,false,"f2l",StackChanges.changeFloat2Long,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x62,false,"fadd",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x30,false,"faload",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x51,false,"fastore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x96,false,"fcmpg",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x95,false,"fcmpl",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x0b,false,"fconst_0",StackChanges.pushFloat);
		placeStaticCommand(0x0c,false,"fconst_1",StackChanges.pushFloat);
		placeStaticCommand(0x0d,false,"fconst_2",StackChanges.pushFloat);
		placeStaticCommand(0x6e,false,"fdiv",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x17,false,"fload",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pushFloat);
		placeStaticCommand(0x22,false,"fload_0",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pushFloat);
		placeStaticCommand(0x23,false,"fload_1",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pushFloat);
		placeStaticCommand(0x24,false,"fload_2",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pushFloat);
		placeStaticCommand(0x25,false,"fload_3",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pushFloat);
		placeStaticCommand(0x6a,false,"fmul",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x76,false,"fneg",StackChanges.popAndPushFloat,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x72,false,"frem",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0xae,true,"freturn",StackChanges.clear,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x38,false,"fstore",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pop,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x43,false,"fstore_0",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pop,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x44,false,"fstore_1",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pop,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x45,false,"fstore_2",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pop,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x46,false,"fstore_3",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pop,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x66,false,"fsub",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0xb4,false,"getfield",CommandFormat.shortGlobalIndex,StackChanges.pushField);
		placeStaticCommand(0xb2,false,"getstatic",CommandFormat.shortGlobalIndex,StackChanges.pushStatic);
		placeStaticCommand(0xa7,true,"goto",CommandFormat.shortBrunch,StackChanges.none);
		placeStaticCommand(0xc8,true,"goto_w",CommandFormat.longBrunch,StackChanges.none);
		placeStaticCommand(0x91,false,"i2b",StackChanges.none,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x92,false,"i2c",StackChanges.none,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x87,false,"i2d",StackChanges.changeInt2Double,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x86,false,"i2f",StackChanges.changeInt2Float,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x85,false,"i2l",StackChanges.changeInt2Long,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x93,false,"i2s",StackChanges.none,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x60,false,"iadd",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x2e,false,"iaload",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x7e,false,"iand",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x4f,false,"iastore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x02,false,"iconst_m1",StackChanges.pushInt);
		placeStaticCommand(0x03,false,"iconst_0",StackChanges.pushInt);
		placeStaticCommand(0x04,false,"iconst_1",StackChanges.pushInt);
		placeStaticCommand(0x05,false,"iconst_2",StackChanges.pushInt);
		placeStaticCommand(0x06,false,"iconst_3",StackChanges.pushInt);
		placeStaticCommand(0x07,false,"iconst_4",StackChanges.pushInt);
		placeStaticCommand(0x08,false,"iconst_5",StackChanges.pushInt);
		placeStaticCommand(0x6c,false,"idiv",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9f,false,"if_icmpeq",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa0,false,"if_icmpne",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa1,false,"if_icmplt",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa2,false,"if_icmpge",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa3,false,"if_icmpgt",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa4,false,"if_icmple",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x99,false,"ifeq",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9a,false,"ifne",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9b,false,"iflt",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9c,false,"ifge",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9d,false,"ifgt",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9e,false,"ifle",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xc7,false,"ifnonnull",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xc6,false,"ifnull",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x84,false,"iinc",CommandFormat.byteIndexAndByteValue,StackChanges.none);
		placeStaticCommand(0x15,false,"iload",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_INT,StackChanges.pushInt);
		placeStaticCommand(0x1a,false,"iload_0",CompilerUtils.CLASSTYPE_INT,StackChanges.pushInt);
		placeStaticCommand(0x1b,false,"iload_1",CompilerUtils.CLASSTYPE_INT,StackChanges.pushInt);
		placeStaticCommand(0x1c,false,"iload_2",CompilerUtils.CLASSTYPE_INT,StackChanges.pushInt);
		placeStaticCommand(0x1d,false,"iload_3",CompilerUtils.CLASSTYPE_INT,StackChanges.pushInt);
		placeStaticCommand(0x68,false,"imul",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x74,false,"ineg",StackChanges.popAndPushInt,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xc1,false,"instanceof",CommandFormat.classShortIndex,StackChanges.popAndPushInt,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xba,false,"invokedynamic",CommandFormat.callDynamic,StackChanges.none);
		placeStaticCommand(0xb9,false,"invokeinterface",CommandFormat.callInterface,StackChanges.callAndPush);
		placeStaticCommand(0xb7,false,"invokespecial",CommandFormat.call,StackChanges.callAndPush);
		placeStaticCommand(0xb8,false,"invokestatic",CommandFormat.call,StackChanges.callStaticAndPush);
		placeStaticCommand(0xb6,false,"invokevirtual",CommandFormat.call,StackChanges.callAndPush);
		placeStaticCommand(0x80,false,"ior",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x70,false,"irem",StackChanges.popAndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xac,true,"ireturn",StackChanges.clear,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x78,false,"ishl",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x7a,false,"ishr",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x36,false,"istore",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_INT,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x3b,false,"istore_0",CompilerUtils.CLASSTYPE_INT,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x3c,false,"istore_1",CompilerUtils.CLASSTYPE_INT,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x3d,false,"istore_2",CompilerUtils.CLASSTYPE_INT,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x3e,false,"istore_3",CompilerUtils.CLASSTYPE_INT,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x64,false,"isub",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x7c,false,"iushr",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x82,false,"ixor",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa8,false,"jsr",CommandFormat.restricted,StackChanges.none);
		placeStaticCommand(0xc9,false,"jsr_w",CommandFormat.restricted,StackChanges.none);
		placeStaticCommand(0x8a,false,"l2d",StackChanges.changeLong2Double,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x89,false,"l2f",StackChanges.changeLong2Float,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x88,false,"l2i",StackChanges.changeLong2Int,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x61,false,"ladd",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x2f,false,"laload",StackChanges.pop2AndPushLong,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x7f,false,"land",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x50,false,"lastore",StackChanges.pop2,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x94,false,"lcmp",StackChanges.pop4AndPushInt,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x09,false,"lconst_0",StackChanges.pushLong);
		placeStaticCommand(0x0a,false,"lconst_1",StackChanges.pushLong);
		placeStaticCommand(0x12,false,"ldc",CommandFormat.valueByteIndex,StackChanges.none);
		placeStaticCommand(0x13,false,"ldc_w",CommandFormat.valueShortIndex,StackChanges.none);
		placeStaticCommand(0x14,false,"ldc2_w",CommandFormat.valueShortIndex2,StackChanges.none);
		placeStaticCommand(0x6d,false,"ldiv",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x16,false,"lload",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_LONG,StackChanges.pushLong);
		placeStaticCommand(0x1e,false,"lload_0",CompilerUtils.CLASSTYPE_LONG,StackChanges.pushLong);
		placeStaticCommand(0x1f,false,"lload_1",CompilerUtils.CLASSTYPE_LONG,StackChanges.pushLong);
		placeStaticCommand(0x20,false,"lload_2",CompilerUtils.CLASSTYPE_LONG,StackChanges.pushLong);
		placeStaticCommand(0x21,false,"lload_3",CompilerUtils.CLASSTYPE_LONG,StackChanges.pushLong);
		placeStaticCommand(0x69,false,"lmul",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x75,false,"lneg",StackChanges.pop2AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0xab,true,"lookupswitch",CommandFormat.lookupSwitch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x81,false,"lor",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x71,false,"lrem",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0xad,true,"lreturn",StackChanges.clear,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x79,false,"lshl",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x7b,false,"lshr",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x37,false,"lstore",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_LONG,StackChanges.pop2,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x3f,false,"lstore_0",CompilerUtils.CLASSTYPE_LONG,StackChanges.pop2,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x40,false,"lstore_1",CompilerUtils.CLASSTYPE_LONG,StackChanges.pop2,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x41,false,"lstore_2",CompilerUtils.CLASSTYPE_LONG,StackChanges.pop2,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x42,false,"lstore_3",CompilerUtils.CLASSTYPE_LONG,StackChanges.pop2,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x65,false,"lsub",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x7d,false,"lushr",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x83,false,"lxor",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP);
		placeStaticCommand(0xc2,false,"monitorenter",StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xc3,false,"monitorexit",StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xc5,false,"multianewarray",CommandFormat.shortIndexAndByteValue,StackChanges.multiarrayAndPushReference);
		placeStaticCommand(0xbb,false,"new",CommandFormat.classShortIndex,StackChanges.pushReference);
		placeStaticCommand(0xbc,false,"newarray",CommandFormat.byteType,StackChanges.popAndPushReference);
		placeStaticCommand(0x00,false,"nop",StackChanges.none);
		placeStaticCommand(0x57,false,"pop",StackChanges.pop);
		placeStaticCommand(0x58,false,"pop2",StackChanges.pop2);
		placeStaticCommand(0xb5,false,"putfield",CommandFormat.shortGlobalIndex,StackChanges.popField);
		placeStaticCommand(0xb3,false,"putstatic",CommandFormat.shortGlobalIndex,StackChanges.popStatic);
		placeStaticCommand(0xa9,true,"ret",CommandFormat.restricted,StackChanges.none);
		placeStaticCommand(0xb1,true,"return",StackChanges.clear);
		placeStaticCommand(0x35,false,"saload",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x56,false,"sastore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x11,false,"sipush",CommandFormat.shortValue,StackChanges.pushInt);
		placeStaticCommand(0x5f,false,"swap",StackChanges.swap);
		placeStaticCommand(0xaa,true,"tableswitch",CommandFormat.tableSwitch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xc4,false,"wide",CommandFormat.restricted,StackChanges.none);
	}
	
	private final ClassDescriptionRepo					cdr;
	private final SyntaxTreeInterface<Macros>			macros;
	private final ClassLoader							owner;
	private final MacroClassLoader						loader;
	private final ClassContainer						cc;
	private final SyntaxTreeInterface<NameDescriptor>	tree;
	private final Class<?>[]							forClass = new Class<?>[1];
	private final EntityDescriptor						forEntity = new EntityDescriptor();
	private final Writer								diagnostics;
	private final long									constructorId, classConstructorId, voidId, doubleId, longId, thisId;
	private final long									callSiteId, methodHandlesLookupId, stringId, methodHandleId, methodTypeId;
	private final long									longArray[] = new long[2];	// Temporary arrays to use in different calls
	private final int									intArray[] = new int[2];
	private final short									shortArray[] = new short[2];
	private final boolean								printAssembler;
	private final boolean								printExpandedMacros;
	
	private ParserState									state = ParserState.beforePackage;
	private long										packageId = -1;
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
	private boolean										addVarTable = false, addVarTableInMethod = false;
	private int[]										forMethodTypes = new int[16];
	private boolean 									needStackMapRecord = false;
	private short										stackSize4CurrentMethod = 0;
	private int											methodLineNo;
	private int											nestingDepth = 0;
	
	LineParser(final ClassLoader owner, final ClassContainer cc, final ClassDescriptionRepo cdr, final SyntaxTreeInterface<Macros> macros, final MacroClassLoader loader) throws IOException, ContentException {
		this(owner,cc,cdr,macros,loader,null);
	}

	LineParser(final ClassLoader owner, final ClassContainer cc, final ClassDescriptionRepo cdr, final SyntaxTreeInterface<Macros> macros, final MacroClassLoader loader, final Writer diagnostics) throws IOException, ContentException {
		this.owner = owner;
		this.cc = cc;
		this.cdr = cdr;
		this.macros = macros;
		this.loader = loader;
		this.tree = cc.getNameTree();				
		this.diagnostics = diagnostics;
		this.constructorId = tree.placeOrChangeName(CONSTRUCTOR,0,CONSTRUCTOR.length,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		this.classConstructorId = tree.placeOrChangeName(CLASS_CONSTRUCTOR,0,CLASS_CONSTRUCTOR.length,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		this.voidId = tree.placeOrChangeName(VOID,0,VOID.length,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		this.doubleId = tree.placeOrChangeName(DOUBLE,0,DOUBLE.length,new NameDescriptor(CompilerUtils.CLASSTYPE_DOUBLE));
		this.longId = tree.placeOrChangeName(LONG,0,LONG.length,new NameDescriptor(CompilerUtils.CLASSTYPE_LONG));
		this.thisId = tree.placeOrChangeName(THIS,0,THIS.length,new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
		this.callSiteId = tree.placeOrChangeName(CALLSITE,0,CALLSITE.length,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		this.methodHandleId = tree.placeOrChangeName(METHODHANDLE,0,METHODHANDLE.length,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		this.methodHandlesLookupId = tree.placeOrChangeName(METHODHANDLESLOOKUP,0,METHODHANDLESLOOKUP.length,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		this.stringId = tree.placeOrChangeName(STRING,0,STRING.length,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		this.methodTypeId = tree.placeOrChangeName(METHODTYPE,0,METHODTYPE.length,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		this.printAssembler = !PureLibSettings.instance().getProperty(PureLibSettings.SUPPRESS_PRINT_ASSEMBLER,boolean.class,"true") && diagnostics != null;
		this.printExpandedMacros = printAssembler && PureLibSettings.instance().getProperty(PureLibSettings.PRINT_EXPANDED_MACROS,boolean.class,"false");		
	}
	
	@Override
	public void processLine(final long displacement, final int lineNo, final char[] data, final int from, final int len) throws IOException {
		int		start = from, end = Math.min(from+len,data.length);
		int		startName, endName, startDir, endDir;
		long	id = -1;

		if (printAssembler) {
			printDiagnostics('\t'+new String(data,from,len));
		}
		
		try{
			if (state == ParserState.insideMacros) {	// Redirect macros code into macros
			    currentMacros.processLine(displacement,lineNo,data,from,len);
				
				if (currentMacros.isPrepared()) {
					final String	macroName = new String(currentMacros.getName());
					
					if (macros.seekName((CharSequence)macroName) >= 0) {
						throw new SyntaxException(lineNo, 0, "Duplicate macros ["+macroName+"] in the input stream");
					}
					else {
						final GrowableCharArray<?>	writer = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
						final String				className = this.getClass().getPackage().getName()+'.'+new String(currentMacros.getName()); 
						
						if ("chav1961.purelib.streams.char2byte.asm.printProxyImportClass".equals(className)) {
							int x = 10;						
						}
						
						try{MacroCompiler.compile(className, currentMacros.getRoot(), writer, stringRepo);
							currentMacros.compile(loader.createClass(className,writer).getConstructor(char[].class).newInstance(stringRepo.extract()));
							macros.placeOrChangeName((CharSequence)macroName,currentMacros);
						} catch (CalculationException | InstantiationException | RuntimeException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
							throw new SyntaxException(lineNo, 0, e.getLocalizedMessage(),e); 
						} catch (VerifyError | ClassFormatError e) {
							throw new SyntaxException(lineNo, 0, e.getLocalizedMessage()+"\nClass content:\n"+new String(writer.extract()), e); 
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
				endName = start = skipSimpleName(data,start);
				if (endName == startName) {
					throw new SyntaxException(lineNo, 0, "Illegal label/entity name");
				}
				
				id = tree.placeOrChangeName(data,startName,endName,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
				
				start = InternalUtils.skipBlank(data,start);
				if (data[start] == ':') {
					switch (state) {
						case insideClassBody :
						case insideBegin :
							putLabel(id);
							break;
						default :
							throw new SyntaxException(lineNo, 0, "Branch label outside the method body!");
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
									throw new SyntaxException(lineNo, 0, "Second class directive in the same stream. Use separate streams for each class/interface!");
								default :
									throw new SyntaxException(lineNo, 0, "Nested class directive!");
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
									throw new SyntaxException(lineNo, 0, "Second interface directive in the same stream. Use separate streams for each class/interface!");
								default :
									throw new SyntaxException(lineNo, 0, "Nested interface directive!");
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
									throw new SyntaxException(lineNo, 0, "Field directive outside the class/interface!");
								default :
									throw new SyntaxException(lineNo, 0, "Field directive inside the method!");
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
									throw new SyntaxException(lineNo, 0, "Method directive outside the class/interface!");
								default :
									throw new SyntaxException(lineNo, 0, "Nested method directive!");
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
									throw new SyntaxException(lineNo, 0, "Parameter directive is used outside the method description!");
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
									throw new SyntaxException(lineNo, 0, "Var directive is used outside the method description!");
							}
							break;
						case DIR_BEGIN	:
							switch (state) {
								case insideBegin :
									beginLevel++;
									methodDescriptor.push();
									methodDescriptor.getBody().getStackAndVarRepo().startVarFrame();
									break;
								case insideClassMethod :
								case insideClassBody :
									beforeBegin = state;
									state = ParserState.insideBegin;
									beginLevel++;
									methodDescriptor.push();
									methodDescriptor.getBody().getStackAndVarRepo().startVarFrame();
									break;
								default :
									throw new SyntaxException(lineNo, 0, "Begin directive is valid in the method body only!");
							}
							break;
						case DIR_END	:
							switch (state) {
								case insideClass :
								case insideInterface :
									checkLabel(id,true);
									if (id != classNameId) {
										throw new SyntaxException(lineNo, 0, "End directive closes class description, but it's label ["+tree.getName(id)+"] is differ from class name ["+tree.getName(classNameId)+"]");
									}
									else {
										state = ParserState.afterClass;
									}
									break;
								case insideClassAbstractMethod :
									checkLabel(id,true);
									if (id != methodNameId) {
										throw new SyntaxException(lineNo, 0, "End directive closes method description, but it's label ["+tree.getName(id)+"] is differ from method name ["+tree.getName(methodNameId)+"]");
									}
									else {
										methodDescriptor.complete();
										addVarTableInMethod = false;
										state = ParserState.insideClass;
									}
									break;
								case insideClassMethod :
									throw new SyntaxException(lineNo, 0, "Class method body is not defined!");
								case insideClassBody :
									checkLabel(id,true);
									if (id != methodNameId && id != classNameId) {
										throw new SyntaxException(lineNo, 0, "End directive closes method description, but it's label ["+tree.getName(id)+"] is differ from method name ["+tree.getName(methodNameId)+"]");
									}
									else if (!areTryBlocksClosed()) {
										throw new SyntaxException(lineNo, 0, "Unclosed try blocks inside the method body ["+tree.getName(methodNameId)+"]");
									}
									else {
										methodDescriptor.complete();
										addVarTableInMethod = false;
										state = ParserState.insideClass;
									}
									break;
								case insideInterfaceAbstractMethod :
									checkLabel(id,true);
									if (id != methodNameId) {
										throw new SyntaxException(lineNo, 0, "End directive closes method description, but it's label ["+tree.getName(id)+"] is differ from method name ["+tree.getName(methodNameId)+"]");
									}
									else {
										methodDescriptor.complete();
										addVarTableInMethod = false;
										state = ParserState.insideInterface;
									}
									break;
								case insideBegin :
									if (beginLevel > 0) {
										methodDescriptor.pop();
										beginLevel--;
										if (stackSize4CurrentMethod == STACK_OPTIMISTIC) {
											methodDescriptor.getBody().getStackAndVarRepo().stopVarFrame();
										}
									}
									else {
										state = beforeBegin;
										checkLabel(id,true);
										if (id != methodNameId) {
											throw new SyntaxException(lineNo, 0, "End directive closes method description, but it's label ["+tree.getName(id)+"] is differ from method name ["+tree.getName(methodNameId)+"]");
										}
										else {
											methodDescriptor.complete();
											addVarTableInMethod = false;
											state = ParserState.insideClass;
										}
									}
									break;
								case insideMethodLookup :
									fillLookup();	jumps.clear();
									state = ParserState.insideBegin;
									markLabelRequired(true);
									break;
								case insideMethodTable :
									fillTable();	jumps.clear();
									state = ParserState.insideBegin; 
									markLabelRequired(true);
									break;
								default :
									throw new SyntaxException(lineNo, 0, "End directive out of context!");
							}
							skip2line(data,start);
							break;
						case DIR_STACK	:
							checkLabel(id,false);
							switch (state) {
								case insideClassMethod :
									processStackDir(data,InternalUtils.skipBlank(data,start));
									state = ParserState.insideClassBody;
									methodLineNo = 0;
									break;
								case insideClassBody :
								case insideBegin :
									throw new SyntaxException(lineNo, 0, "Duplicate Stack directive detected!");
								default :
									throw new SyntaxException(lineNo, 0, "Stack directive is used outside the method description!");
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
									throw new SyntaxException(lineNo, 0, "Duplicate package directive!");
								default :
									throw new SyntaxException(lineNo, 0, "Package directive is used inside the class or interface description! Use import before the class/interface directive only!");
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
									throw new SyntaxException(lineNo, 0, "Package directive is used inside the class or interface description! Use import before the class/interface directive only!");
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
									throw new SyntaxException(lineNo, 0, "Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside method body only");
							}
							break;
						case DIR_CATCH	:
							switch (state) {
								case insideClassBody :
								case insideBegin :
									processCatch(data,InternalUtils.skipBlank(data,start),end);
									break;
								default :
									throw new SyntaxException(lineNo, 0, "Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside method body only");
							}
							break;
						case DIR_END_TRY	:
							switch (state) {
								case insideClassBody :
								case insideBegin :
									popTryBlock(lineNo);
									break;
								default :
									throw new SyntaxException(lineNo, 0, "Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside method body only");
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
									throw new SyntaxException(lineNo, 0, "Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used before package description only");
							}
							break;
						case DIR_DEFAULT:
							switch (state) {
								case insideMethodLookup : 
								case insideMethodTable : 
									processJumps(data,InternalUtils.skipBlank(data,start),end,false);
									break;
								default :
									throw new SyntaxException(lineNo, 0, "Directive : "+new String(data,startDir-1,endDir-startDir+1)+" can be used inside loowkuswitch/tableswitch command body only");
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
						case DIR_VARTABLE	:
							checkLabel(id,false);
							processVarTableDir(lineNo,data,InternalUtils.skipBlank(data,start),end);
							break;
						case DIR_FORWARD	:
							checkLabel(id,true);
							processForwardDir(lineNo,id,data,InternalUtils.skipBlank(data,start),end);
							break;
						case DIR_SOURCE	:
							checkLabel(id,false);
							processSourceDir(data,InternalUtils.skipBlank(data,start),end);
							break;
						default :
							throw new SyntaxException(lineNo, 0, "Unknown directive : "+new String(data,startDir-1,endDir-startDir+1));
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
								default : throw new SyntaxException(lineNo, 0, "Unknown automation : "+new String(data,startDir-1,endDir-startDir+1));
							}
							break;
						default :
							throw new SyntaxException(lineNo, 0, "Automation can be used on the method body only!");
					}
					break;
				case '\r' : case '\n':
					break;
				default :
					final int	possiblyMacroStart = start, possiblyMacroEnd = skipSimpleName(data,start);
					final long	macroId;
					
					if (possiblyMacroEnd > possiblyMacroStart && (macroId = macros.seekName(data,possiblyMacroStart,possiblyMacroEnd)) >= 0) {	// Process macro call
						final Macros	m = macros.getCargo(macroId);
						
						nestingDepth++;
						try(final LineByLineProcessor	lbl = new LineByLineProcessor(printExpandedMacros
																? new LineByLineProcessorCallback(){
																		@Override
																		public void processLine(long displacement, int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
																	//		printDiagnostics("\n\t> "+new String(data,from,length));
																			try{
																				LineParser.this.processLine(displacement,lineNo, data, from, len);
																			} catch (Exception  exc) {
																				diagnostics.flush();
																				throw exc;
																			}
																		}
																	}  
																: this);
							final Reader	rdr = m.processCall(lineNo,data,possiblyMacroEnd,len-(possiblyMacroEnd-from)+1)) {

							lbl.write(rdr);
						} catch (SyntaxException exc) {
							if (diagnostics != null) {
								diagnostics.flush();
							}
							throw new SyntaxException(lineNo, 0, "Error in ["+new String(m.getName())+"] macros: "+exc.getLocalizedMessage(), exc); 
						} catch (IOException exc) {
							if (diagnostics != null) {
								diagnostics.flush();
							}
							exc.printStackTrace();
						} catch (Exception exc) {
							throw exc;
						} finally {
							nestingDepth--;
						}
						
						return;
					}
					
					switch (state) {
						case insideClassBody : 
						case insideBegin : 
							endDir = start = skipSimpleName(data,start);
							
							if (startDir == endDir) {
								throw new SyntaxException(lineNo, 0, "Unknown command");
							}
							final long	opCode = staticCommandTree.seekName(data,startDir,endDir);
		
							if (opCode >= 0) {
								final CommandDescriptor	desc = staticCommandTree.getCargo(opCode);
								
								if (addLines2Class && !addLines2ClassManually) {
									methodDescriptor.addLineNoRecord(methodLineNo++);
								}
								start = InternalUtils.skipBlank(data,start);

								if (desc.checkedTypes != null && desc.checkedTypes.length > 0) {
									if (!methodDescriptor.getBody().getStackAndVarRepo().compareStack(desc.checkedTypes)) {
										if (!methodDescriptor.getBody().getStackAndVarRepo().compareStack(desc.checkedTypes)) {
											throw new SyntaxException(lineNo,0,"Illegal data types at the top of stack to use this command!");
										}
									}
								}								
								
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
									case lookupSwitch			:
										prepareSwitchCommand((byte)opCode,desc.stackChanges);
										state = ParserState.insideMethodLookup; 
										break;
									case tableSwitch			: 
										prepareSwitchCommand((byte)opCode,desc.stackChanges);
										state = ParserState.insideMethodTable; 
										break;
									case restricted				: 
										throw new SyntaxException(lineNo, 0, "Restricted command in the input stream!");
									default : 
										throw new UnsupportedOperationException("Command format ["+desc.commandFormat+"] is not supported yet");
								}
							}
							else {
								throw new SyntaxException(lineNo, 0, "Unknown command : "+new String(data,startDir,endDir-startDir));
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
										throw new SyntaxException(lineNo,0,"Unparsed line in the input: ["+new String(data,from,len-1)+"] is unknown or illegal for the ["+state+"] context"); 
									}
								}
							}
					}
			}
		} catch (SyntaxException exc) {
			exc.printStackTrace();
			final SyntaxException	synt = new SyntaxException(lineNo,start-from,new String(data,from,len)+exc.getMessage(),exc);
			throw new IOException(synt.getLocalizedMessage(),synt);
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
	
	private boolean isLabelExists(final long labelId) throws ContentException, IOException {
		return methodDescriptor.getBody().isLabelExists(labelId);
	}
	
	private void markLabelRequired(final boolean required) throws ContentException, IOException {
		methodDescriptor.getBody().markLabelRequired(required);
		needStackMapRecord = required;
	}
	
	private void putLabel(final long labelId) throws ContentException, IOException {
		if (!isLabelExists(labelId)) {
			methodDescriptor.getBody().putLabel(labelId,methodDescriptor.getBody().getStackAndVarRepo().makeStackSnapshot());
			prepareStackMapRecord();
			if (needStackMapRecord) {
				needStackMapRecord = false;
			}
		}
		else {
			throw new ContentException("Duplicate label in the method body!");
		}
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

	private void registerBranch(final long labelId, final boolean shortBrunch) throws ContentException, IOException {
		methodDescriptor.getBody().registerBrunch(labelId,shortBrunch,methodDescriptor.getBody().getStackAndVarRepo().makeStackSnapshot());
	}

	private void registerBranch(final int address, final int placement, final long labelId, final boolean shortBrunch) throws ContentException, IOException {
		methodDescriptor.getBody().registerBrunch(address,placement,labelId,shortBrunch,methodDescriptor.getBody().getStackAndVarRepo().makeStackSnapshot());
	}
	
	private void changeStack(final StackChanges change) throws ContentException, IOException {
		methodDescriptor.getBody().getStackAndVarRepo().processChanges(change);
	}

	private void changeStack(final StackChanges change, final int signature) throws ContentException, IOException {
		methodDescriptor.getBody().getStackAndVarRepo().processChanges(change,signature);
	}

	private void changeStack(final StackChanges change, final int[] signature, final int signatureSize, final int retSignature) throws ContentException, IOException {
		methodDescriptor.getBody().getStackAndVarRepo().processChanges(change,signature,signatureSize,retSignature);
	}

	private int getVarType(final int varDispl) throws ContentException, IOException {
		return methodDescriptor.getBody().getStackAndVarRepo().getVarType(varDispl);
	}
	
	private void prepareStackMapRecord() throws ContentException {
		methodDescriptor.addStackMapRecord();
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
				case OPTION_PUBLIC		: classFlags = addAndCheckDuplicates(classFlags,JavaByteCodeConstants.ACC_PUBLIC,"class"); break;
				case OPTION_FINAL		: classFlags = addAndCheckDuplicates(classFlags,JavaByteCodeConstants.ACC_FINAL,"class"); break;
				case OPTION_ABSTRACT	: classFlags = addAndCheckDuplicates(classFlags,JavaByteCodeConstants.ACC_ABSTRACT,"class"); break;
				case OPTION_SYNTHETIC	: classFlags = addAndCheckDuplicates(classFlags,JavaByteCodeConstants.ACC_SYNTHETIC,"class"); break;
				case OPTION_EXTENDS		:
					if (extendsId != -1) {
						throw new ContentException("Duplicate option 'extends' in the 'class' directive!");
					}
					else {
						int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedName(data,start);
						
						final Class<?>	parent = cdr.getClassDescription(data,startName,endName);

						if ((parent.getModifiers() & JavaByteCodeConstants.ACC_FINAL) != 0 || (parent.getModifiers() & JavaByteCodeConstants.ACC_PRIVATE) != 0) {
							throw new ContentException("Attempt to extends final or  private class ["+new String(data,startName,endName-startName)+"]!"); 
						}
						else {
							extendsId = tree.placeOrChangeName((CharSequence)parent.getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
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

							if ((member.getModifiers() & JavaByteCodeConstants.ACC_INTERFACE) == 0) {
								throw new ContentException("Implements item ["+new String(data,startName,endName-startName)+"] references to the class, not interface!"); 
							}
							else {
								implementsNames.add(tree.placeOrChangeName((CharSequence)member.getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)));
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

		classFlags |= JavaByteCodeConstants.ACC_INTERFACE | JavaByteCodeConstants.ACC_ABSTRACT;
		while (data[start] != '\n') {
			endOption = start = skipSimpleName(data,start);
			switch ((int)staticDirectiveTree.seekName(data,startOption,endOption)) {
				case OPTION_PUBLIC		: classFlags = addAndCheckDuplicates(classFlags,JavaByteCodeConstants.ACC_PUBLIC,"class"); break;
				case OPTION_SYNTHETIC	: classFlags = addAndCheckDuplicates(classFlags,JavaByteCodeConstants.ACC_SYNTHETIC,"class"); break;
				case OPTION_EXTENDS	:
					if (implementsNames != null) {
						throw new ContentException("Duplicate option 'implements' in the 'class' directive!");
					}
					else {
						implementsNames = new ArrayList<>();
						
						do {start++;
							int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedName(data,start);

							final Class<?>	parent = cdr.getClassDescription(data,startName,endName);

							if ((parent.getModifiers() & JavaByteCodeConstants.ACC_INTERFACE) == 0) {
								throw new ContentException("Extends item ["+new String(data,startName,endName-startName)+"] references to the class, not interface!"); 
							}
							else {
								implementsNames.add(tree.placeOrChangeName((CharSequence)parent.getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)));
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
		classFlags |= JavaByteCodeConstants.ACC_PUBLIC;
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
		final int			checkType = CompilerUtils.defineClassType(type);
		
		if (type == void.class) {
			throw new ContentException("Type 'void' is invalid for using with fields"); 
		}
		else {
			final long		typeId = tree.placeOrChangeName((CharSequence)InternalUtils.buildFieldSignature(tree,tree.placeOrChangeName((CharSequence)type.getName(),new NameDescriptor(checkType))),new NameDescriptor(checkType));
			
			start = processOptions(data,InternalUtils.skipBlank(data,start),forEntity,"field",cdr,false,OPTION_PUBLIC,OPTION_PROTECTED,OPTION_PRIVATE,OPTION_STATIC,OPTION_FINAL,OPTION_VOLATILE,OPTION_TRANSIENT,OPTION_SYNTHETIC);
			start = InternalUtils.skipBlank(data,start);
			
			if (data[start] == '=') {	// Initial values. 
				if ((forEntity.options & OPTION_STATIC) == 0) {
					throw new ContentException("Initial values can be typed for static fields only!"); 
				}
				else if (!type.isPrimitive() && type != String.class) {
					throw new ContentException("Initial values can be typed for primitive types or strings only!"); 
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
								final int[]		places = intArray;
								
								if ((UnsafedCharUtils.uncheckedParseUnescapedString(data,start+1,'\"',true,places)) < 0) {
									final StringBuilder	sb = new StringBuilder();
									
									UnsafedCharUtils.uncheckedParseStringExtended(data,start+1,'\"',sb);
									valueId = cc.getConstantPool().asStringDescription(cc.getNameTree().placeOrChangeName(sb,new NameDescriptor(checkType)));
								}
								else {
									valueId = cc.getConstantPool().asStringDescription(cc.getNameTree().placeOrChangeName(data,places[0],places[1]+1,new NameDescriptor(checkType)));
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
							final int[]		intValues = intArray;
							
							UnsafedCharUtils.uncheckedParseSignedInt(data,start,intValues,true);
							valueId = cc.getConstantPool().asIntegerDescription(intValues[0]);
							break;
						case CompilerUtils.CLASSTYPE_FLOAT	:
							final float[]		floatValues = new float[1];
							
							CharUtils.parseSignedFloat(data,start,floatValues,true);
							valueId = cc.getConstantPool().asFloatDescription(floatValues[0]);
							break;
						case CompilerUtils.CLASSTYPE_LONG	:
							final long[]		longValues = longArray;
							
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
					cc.addFieldDescription(forEntity.options,id,typeId,valueId);
				}
			}
			else {
				cc.addFieldDescription(forEntity.options,id,typeId);
			}
			cc.getConstantPool().asFieldRefDescription(joinedClassNameId,id,typeId);
			tree.getCargo(id).nameType = checkType;

			final int		classNameLen = tree.getNameLength(joinedClassNameId), fieldLen = tree.getNameLength(id);
			final char[]	forLongName = new char[classNameLen+1+fieldLen];
			
			tree.getName(joinedClassNameId,forLongName,0);
			forLongName[classNameLen] = '.';
			tree.getName(id,forLongName,classNameLen+1);
			tree.placeName(forLongName,0,forLongName.length,new NameDescriptor(checkType));
		}
	}

	private void processInterfaceFieldDir(final long id, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		final int		checkType = CompilerUtils.defineClassType(type);
		
		if (type == void.class) {
			throw new ContentException("Type 'void' is invalid for using with fields"); 
		}
		else {
			final long	typeId = tree.placeOrChangeName((CharSequence)cdr.getClassDescription(data,startName,endName).getName(),new NameDescriptor(checkType));
			
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
								final int[]		places = intArray;
								
								if ((UnsafedCharUtils.uncheckedParseUnescapedString(data,start+1,'\"',true,places)) < 0) {
									final StringBuilder	sb = new StringBuilder();
									
									UnsafedCharUtils.uncheckedParseStringExtended(data,start+1,'\"',sb);
									valueId = cc.getConstantPool().asStringDescription(cc.getNameTree().placeOrChangeName(sb,new NameDescriptor(checkType)));
								}
								else {
									valueId = cc.getConstantPool().asStringDescription(cc.getNameTree().placeOrChangeName(data,places[0],places[1]-places[0],new NameDescriptor(checkType)));
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
							final int[]		intValues = intArray;
							
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
					cc.addFieldDescription((short)(forEntity.options| JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_STATIC | JavaByteCodeConstants.ACC_FINAL),id,typeId,valueId);
				}
			}
			else {
				cc.addFieldDescription((short)(forEntity.options| JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_STATIC | JavaByteCodeConstants.ACC_FINAL),id,typeId);
			}
			tree.getCargo(id).nameType = checkType;
		}
	}
	
	private void processClassMethodDir(final long id, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		
		if (startName == endName) {
			throw new ContentException("Required return type for the method/constructor is missing!");
		}
		else {
			final Class<?>	type = cdr.getClassDescription(data,startName,endName);
			final int		retType = CompilerUtils.defineClassType(type);
			final long		typeId = tree.placeOrChangeName((CharSequence)type.getName(),new NameDescriptor(retType));
			
			start = processOptions(data,InternalUtils.skipBlank(data,start),forEntity,"method",cdr,false,OPTION_PUBLIC,OPTION_PROTECTED,OPTION_PRIVATE,OPTION_STATIC,OPTION_FINAL,OPTION_SYNCHRONIZED,OPTION_BRIDGE,OPTION_VARARGS,OPTION_NATIVE,OPTION_ABSTRACT,OPTION_SYNTHETIC,OPTION_THROWS,OPTION_BOOTSTRAP);
			if ((classFlags & JavaByteCodeConstants.ACC_ABSTRACT) == 0 && (forEntity.options & JavaByteCodeConstants.ACC_ABSTRACT) != 0) {
				throw new ContentException("Attempt to add abstract method to the non-abstract class!");
			}
			else {
				if (id == classNameId) {	// This is a constructor!
					if (typeId != voidId) {
						throw new ContentException("Constructor method need return void type!");
					}
					else if ((forEntity.options & JavaByteCodeConstants.ACC_STATIC) != 0) {	// This is a <clinit>
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
						throwsList[index] = tree.placeOrChangeName((CharSequence)forEntity.throwsList.get(index).getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
					}
					methodDescriptor = cc.addMethodDescription(forEntity.options,forEntity.specialFlags,methodNameId,typeId,throwsList);
				}
				else {
					methodDescriptor = cc.addMethodDescription(forEntity.options,forEntity.specialFlags,methodNameId,typeId);
				}
			}
		}
	}
	
	private void processInterfaceMethodDir(final long id, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		final int		retType = CompilerUtils.defineClassType(type);
		final long		typeId = tree.placeOrChangeName((CharSequence)type.getName(),new NameDescriptor(retType));
		
		start = processOptions(data,InternalUtils.skipBlank(data,start),forEntity,"method",cdr,false,OPTION_PUBLIC,OPTION_STATIC,OPTION_FINAL,OPTION_SYNCHRONIZED,OPTION_BRIDGE,OPTION_VARARGS,OPTION_NATIVE,OPTION_ABSTRACT,OPTION_SYNTHETIC,OPTION_THROWS);
		
		forEntity.options |= JavaByteCodeConstants.ACC_ABSTRACT | JavaByteCodeConstants.ACC_PUBLIC; 
		if (forEntity.throwsList.size() > 0) {
			final long[] 	throwsList = new long[forEntity.throwsList.size()];
			
			for (int index = 0; index < throwsList.length; index++) {
				throwsList[index] = tree.placeOrChangeName((CharSequence)forEntity.throwsList.get(index).getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
			}
			methodDescriptor = cc.addMethodDescription(forEntity.options,forEntity.specialFlags,id,typeId,throwsList);
		}
		else {
			methodDescriptor = cc.addMethodDescription(forEntity.options,forEntity.specialFlags,id,typeId);
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
			final long	typeId = tree.placeOrChangeName((CharSequence)toCanonicalName(forClass[0]),new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
			
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
			final long	typeId = tree.placeOrChangeName((CharSequence)toCanonicalName(forClass[0]),new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		
			start = processOptions(data,InternalUtils.skipBlank(data,start),forEntity,"var",cdr,false,OPTION_FINAL,OPTION_SYNTHETIC);
			methodDescriptor.addVarDeclaration(forEntity.options,id,typeId);
		}
	}

	private void processStackDir(final char[] data, int start) throws ContentException {
		final  int	startName = start, endName = start = skipSimpleName(data, start);
		
		switch ((int)staticDirectiveTree.seekName(data,startName,endName)) {
			case STACK_OPTIMISTIC :
				methodDescriptor.setStackSize(stackSize4CurrentMethod = MethodBody.STACK_CALCULATION_OPTIMISTIC,addVarTable || addVarTableInMethod);
				break;
			case STACK_PESSIMISTIC :
				methodDescriptor.setStackSize(stackSize4CurrentMethod = MethodBody.STACK_CALCULATION_PESSIMISTIC,addVarTable || addVarTableInMethod);
				break;
			default :
				final long[]	size = longArray;
				
				try{start = UnsafedCharUtils.uncheckedParseNumber(data,startName,size,CharUtils.PREF_INT,true);
					if (size[1] == CharUtils.PREF_INT) {
						methodDescriptor.setStackSize(stackSize4CurrentMethod = (short)size[0],addVarTable || addVarTableInMethod);
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
		if (methodDescriptor.isBootstrap()) {
			if (!methodDescriptor.isStatic()) {
				throw new ContentException("Bootstrap method must be static only!");
			}
			else {
				final long[] 	plist = methodDescriptor.getParametersList();
				
				if (plist.length >= 3) {
					if (!(plist[0] == methodHandlesLookupId && plist[1] == stringId && plist[2] == methodTypeId)) {
						throw new ContentException("Illegal parameters for bootstrap method. The same first three one must be MethodHandles.Lookup,String and MethodType only!");
					}
					else if (methodDescriptor.getReturnedType() != callSiteId) {
						throw new ContentException("Illegal returned type for bootstrap method. Must be CallSite only!");
					}
				}
				else {
					throw new ContentException("Bootstrap method must contain at least 3 parameters!");
				}
			}
		}
	}
	
	private void processPackageDir(final char[] data, int start) throws ContentException {
		final int	endPackage = skipQualifiedName(data,start);
			
		if (start == endPackage) {
			throw new ContentException("Package name is missing!");
		}
		else {
			packageId = tree.placeOrChangeName(data,start,endPackage,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
			skip2line(data,endPackage);
		}
	}

	private void processImportDir(final char[] data, int start) throws ContentException {
		final int		endName = skipQualifiedName(data,start);
		final String	className = new String(data,start,endName - start);  

		try{final int	possibleProtected = InternalUtils.skipBlank(data,endName);
			
			if (UnsafedCharUtils.uncheckedCompare(data,possibleProtected,PROTECTED_KEYWORD,0,PROTECTED_KEYWORD.length)) {
				cdr.addDescription(importClass(className,loader,owner),true);
				start = possibleProtected + PROTECTED_KEYWORD.length;
			}
			else {
				cdr.addDescription(importClass(className,loader,owner),false);
				start = endName;
			}
			skip2line(data,start);
		} catch (ClassNotFoundException e) {
			throw new ContentException("Class description ["+className+"] is unknown in the actual class loader. Test the class name you want to import and/or make it aacessible for the class loader",e);
		}				
	}
	
	private static Class<?> importClass(final String className, final ClassLoader... loaders) throws ClassNotFoundException {
		for (ClassLoader item : loaders) {
			if (item != null) {
				try{return item.loadClass(className);
				} catch (ClassNotFoundException e) {
				}
			}
		}
		return Class.forName(className); 
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
		int		parm[] = intArray, from = start, major = 0, minor = 0;
		
		if (start < end && data[start] >= '0' && data[start] <= '9') {
			start = UnsafedCharUtils.uncheckedParseInt(data,from,parm,true);
			major = parm[0];
			if (data[start] == '.') {
				UnsafedCharUtils.uncheckedParseInt(data,from = start + 1,parm,true);
				minor = parm[0];
			}
			if (major == 1) {
				switch (minor) {
					case 1 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_1,JavaByteCodeConstants.MINOR_1_1);
						break;
					case 2 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_2,JavaByteCodeConstants.MINOR_1_2);
						break;
					case 3 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_3,JavaByteCodeConstants.MINOR_1_3);
						break;
					case 4 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_4,JavaByteCodeConstants.MINOR_1_4);
						break;
					case 5 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_5,JavaByteCodeConstants.MINOR_1_5);
						break;
					case 6 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_6,JavaByteCodeConstants.MINOR_1_6);
						break;
					case 7 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_7,JavaByteCodeConstants.MINOR_1_7);
						break;
					case 8 	:	// backward compatibility...
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_8,JavaByteCodeConstants.MINOR_8);
						break;
					default :
						throw new ContentException("Version number "+major+"."+minor+" is not supported. Only 1.7 and 1.8 are available now!");
				}
			}
			else {
				switch (major) {
					case 8 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_8,JavaByteCodeConstants.MINOR_8);
						break;
					case 9 	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_9,JavaByteCodeConstants.MINOR_9);
//						break;						
					case 10	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_10,JavaByteCodeConstants.MINOR_10);
//						break;						
					case 11	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_11,JavaByteCodeConstants.MINOR_11);
//						break;						
					case 12	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_12,JavaByteCodeConstants.MINOR_12);
//						break;						
					case 13	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_13,JavaByteCodeConstants.MINOR_13);
//						break;						
					case 14	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_14,JavaByteCodeConstants.MINOR_14);
//						break;						
					case 15	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_15,JavaByteCodeConstants.MINOR_15);
//						break;						
					case 16	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_16,JavaByteCodeConstants.MINOR_16);
//						break;						
					case 17	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_17,JavaByteCodeConstants.MINOR_17);
//						break;						
					case 18	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_18,JavaByteCodeConstants.MINOR_18);
//						break;						
					case 19	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_19,JavaByteCodeConstants.MINOR_19);
//						break;						
					case 20	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_20,JavaByteCodeConstants.MINOR_20);
//						break;						
					case 21	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_21,JavaByteCodeConstants.MINOR_21);
//						break;						
					default :
						throw new ContentException("Version number "+major+"."+minor+" is not supported. Only 1.7 and 1.8 are available now!");
				}
			}
			if (major == 1 && minor == 7) {
				cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_7,JavaByteCodeConstants.MINOR_1_7);
			}
			else if (major == 1 && minor == 8) {
				cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_8,JavaByteCodeConstants.MINOR_8);
			}
			else {
				throw new ContentException("Version number "+major+"."+minor+" is not supported. Only 1.7 and 1.8 are available now!");
			}
		}
		else {
			throw new ContentException("Missing version number!");
		}
	}
	
	private void processLineDir(final int lineNo, final char[] data, int start, final int end) throws ContentException, IOException {
		int		parm[] = intArray, from = start;
		
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

	private void processVarTableDir(final int lineNo, final char[] data, int start, final int end) throws ContentException, IOException {
		if (state == ParserState.insideClassBody || state == ParserState.insideBegin) {			
			addVarTableInMethod = true;
		}
		else {
			addVarTable = true;
		}
	}
	
	private void processForwardDir(final int lineNo, final long labelId, final char[] data, int start, final int end) throws ContentException, IOException {
		final int	from = start;
		
		start = skipSignature(data, start);
		final long	signatureId = tree.placeOrChangeName(data, from, start, new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		
		cc.addForward(labelId, signatureId);
	}
	
	private void processSourceDir(final char[] data, int start, final int end) throws ContentException {
		int		from = start;
		
		if (data[start] == '\"') {
			start = skipQuoted(data,start+1,'\"');
			skip2line(data,start+1);
			
			final String	ref = new String(data,from+1,start-from-1);
			final URI		refUrl = URI.create(ref);
			
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
					final long		exceptionId = tree.placeOrChangeName((CharSequence)exception.getName().replace('.','/'),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));

					methodDescriptor.addExceptionRecord((short)tryList.get(0)[0],(short)tryList.get(0)[1],cc.getConstantPool().asClassDescription(exceptionId),(short)getPC());
					from = InternalUtils.skipBlank(data,endException);
				} while (data[from] == ',');
			}
			else {
				methodDescriptor.addExceptionRecord((short)tryList.get(0)[0],(short)tryList.get(0)[1],(short)0,(short)getPC());
			}
			markLabelRequired(false);
			methodDescriptor.getBody().getStackAndVarRepo().loadStackSnapshot(StackAndVarRepo.CATCH_SNAPSHOT);
			prepareStackMapRecord();
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
		final byte	op = (byte)desc.operation; 
		
		putCommand(op);
		changeStack(desc.stackChanges);
		if (desc.uncondBrunch) {
			markLabelRequired(true);
		}
		skip2line(data,start);
	}

	private void processByteIndexCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandAddress) throws IOException, ContentException {
		final long	forResult[] = longArray;
		
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
			if (desc.argumentType != DONT_CHECK_LOCAL_TYPES && getVarType((int)forResult[0]) != desc.argumentType) { 
				throw new ContentException("Incompatible data types for given command and local variable referenced. Var displ is ["+forResult[0]+"]");
			}
		}
		changeStack(desc.stackChanges);
		skip2line(data,start);
	}

	private void processByteValueCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandValue) throws IOException, ContentException {
		final long	forResult[] = longArray;
		
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
		changeStack(desc.stackChanges);
		skip2line(data,start);
	}

	private void processByteIndexAndByteValueCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandAddress) throws IOException, ContentException {
		final long[]	forIndex = longArray, forValue = new long[2];
		boolean			needExpand = false;
		
		start = InternalUtils.skipBlank(data, calculateLocalAddress(data,start,forIndex));
		if (data[start] == ',') {
			start = calculateValue(data,InternalUtils.skipBlank(data,start+1),EvalState.additional,forValue);
		}
		else {
			throw new ContentException("Second mandatory parameter is missing");
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
				throw new ContentException("Calculated value ["+forValue[0]+"] occupies more than 1 byte! Use 'wide' command for those values");
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
		changeStack(desc.stackChanges);
		skip2line(data,start);
	}
	
	private void processByteTypeCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final int	startType = start, endType = start = skipSimpleName(data,start);
		final long	typeId = staticDirectiveTree.seekName(data,startType,endType);

		if (typeId >= T_BASE && typeId <= T_BASE_END) {
			putCommand((byte)desc.operation,(byte)(typeId-T_BASE));
			changeStack(desc.stackChanges);
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
				final int		checkType = CompilerUtils.defineClassType(type);
				final long		typeId = tree.placeOrChangeName((CharSequence)type.getName().replace('.','/'),new NameDescriptor(checkType));
				final short		typeDispl = cc.getConstantPool().asClassDescription(typeId);
		
				putCommand((byte)desc.operation,(byte)((typeDispl >> 8) & 0xFF),(byte)(typeDispl & 0xFF));
				skip2line(data,start);
			} catch (ContentException exc) {
				final long		typeId = cc.getNameTree().seekName(data,startName,endName);
				
				if (typeId >= 0) {
					final NameDescriptor	nd = cc.getNameTree().getCargo(typeId);
					
					if (nd != null) {
						final short			typeDispl = nd.cpIds[JavaByteCodeConstants.CONSTANT_Class];
					
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
		changeStack(desc.stackChanges);
	}

	private void processValueByteIndexCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final int	fromContent = start; 
		short		displ[] = shortArray;
		
		start = processValueShortIndexCommand(data,start,displ);

		if (displ[0] < 0 || displ[0] > 2*Byte.MAX_VALUE) {
			if (desc.operation == staticCommandTree.seekName((CharSequence)"ldc")) {	// ldc can be replaced with ldc_w 
				processValueShortIndexCommand(staticCommandTree.getCargo(staticCommandTree.seekName((CharSequence)"ldc_w")),data,fromContent);
			}
			else {
				throw new ContentException("Calculated value ["+displ[0]+"] is too long for byte index");
			}
		}
		else {
			putCommand((byte)desc.operation,(byte)(displ[0] & 0xFF));
			changeStack(dataTypeToStackChange(displ[1]));
			skip2line(data,start);
		}
	}
	
	private void processValueShortIndexCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		short		displ[] = shortArray;
		
		start = processValueShortIndexCommand(data,start,displ);

		if (displ[0] < 0 || displ[0] > 2*Short.MAX_VALUE) {
			throw new ContentException("Calculated value ["+displ[0]+"] is too long for short index");
		}
		else {
			putCommand((byte)desc.operation,(byte)((displ[0] >> 8) & 0xFF),(byte)(displ[0] & 0xFF));
			changeStack(dataTypeToStackChange(displ[1]));
			skip2line(data,start);
		}
	}

	private static StackChanges dataTypeToStackChange(final short dataType) {
		switch (dataType) {
			case CompilerUtils.CLASSTYPE_REFERENCE	: return StackChanges.pushReference;
			case CompilerUtils.CLASSTYPE_BYTE		: return StackChanges.pushInt;
			case CompilerUtils.CLASSTYPE_SHORT		: return StackChanges.pushInt;
			case CompilerUtils.CLASSTYPE_CHAR		: return StackChanges.pushInt;
			case CompilerUtils.CLASSTYPE_INT		: return StackChanges.pushInt;
			case CompilerUtils.CLASSTYPE_LONG		: return StackChanges.pushLong;
			case CompilerUtils.CLASSTYPE_FLOAT		: return StackChanges.pushFloat;
			case CompilerUtils.CLASSTYPE_DOUBLE		: return StackChanges.pushDouble;
			case CompilerUtils.CLASSTYPE_BOOLEAN	: return StackChanges.pushInt;
			default : throw new IllegalArgumentException(); 
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
						result[1] = CompilerUtils.CLASSTYPE_FLOAT;
					}
					else if (forResult[1] == CharUtils.PREF_INT) {
						displ = cc.getConstantPool().asIntegerDescription((int)(sign*forResult[0]));
						result[1] = CompilerUtils.CLASSTYPE_INT;
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
							result[1] = CompilerUtils.CLASSTYPE_INT;
						}
						else {
							throw new ContentException("Illegal char constant length. Need be exactly one char inside char constant");
						}
					}
					else {
						displ = cc.getConstantPool().asIntegerDescription(data[startChar]);
						result[1] = CompilerUtils.CLASSTYPE_INT;
					}
					start++;
					break;
				case '\"' :
					final int	startString = start + 1, endString = start = skipQuoted(data,startString,'\"');
					
					start++;
					displ = cc.getConstantPool().asStringDescription(cc.getNameTree().placeOrChangeName(data,startString,endString,new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)));
					result[1] = CompilerUtils.CLASSTYPE_REFERENCE;
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
						displ = cc.getConstantPool().asClassDescription(cc.getNameTree().placeOrChangeName(data,startName,endName-CLASS_SUFFIX.length,new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)));
						result[1] = CompilerUtils.CLASSTYPE_REFERENCE;
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
		final long		forResult[] = longArray;
		StackChanges	changes = null;
		long			sign = 1;
		short			displ;

		try{if (data[start] == '-') {
				sign = -1;
				start++;
			}
			start = UnsafedCharUtils.uncheckedParseNumber(data, start, forResult, CharUtils.PREF_LONG|CharUtils.PREF_DOUBLE,true);
			if (forResult[1] == CharUtils.PREF_DOUBLE) {
				displ = cc.getConstantPool().asDoubleDescription(sign*Double.longBitsToDouble(forResult[0]));
				changes = StackChanges.pushDouble;
			}
			else if (forResult[1] == CharUtils.PREF_LONG) {
				displ = cc.getConstantPool().asLongDescription(sign*forResult[0]);
				changes = StackChanges.pushLong;
			}
			else {
				throw new ContentException("Illegal numeric constant size (only long and double are available here). Add length modifier (nnnL, nnnD) to the constant if required");
			}
			if (displ < 0 || displ > 2*Short.MAX_VALUE) {
				throw new ContentException("Calculated value ["+displ+"] is too long for short index");
			}
			else {
				putCommand((byte)desc.operation,(byte)((displ >> 8) & 0xFF),(byte)(displ & 0xFF));
				changeStack(changes);
				skip2line(data,start);
			}
		} catch (NumberFormatException exc) {
			throw new ContentException("Illegal number: "+exc);
		}
	}
	
	private void processShortValueCommand(final CommandDescriptor desc, final char[] data, int start, final boolean expandValue) throws IOException, ContentException {
		final long	forResult[] = longArray;
		
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
		changeStack(desc.stackChanges);
		skip2line(data,start);
	}

	private void processShortIndexAndByteValueCommand(final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		final Class<?>	type = cdr.getClassDescription(data,startName,endName);
		final int		checkType = CompilerUtils.defineClassType(type);
		final long		typeId = tree.placeOrChangeName((CharSequence)type.getName().replace('.','/'),new NameDescriptor(checkType));
		final short		typeDispl = cc.getConstantPool().asClassDescription(typeId);
		final long[]	forValue = longArray;
		
		start = InternalUtils.skipBlank(data,endName);
		if (data[start] == ',') {
			start = calculateValue(data,InternalUtils.skipBlank(data,start+1),EvalState.additional,forValue);
	 		if (forValue[0] <= 0 || forValue[0] >= 2 * Byte.MAX_VALUE) {
				throw new ContentException("Calculated value ["+forValue[0]+"] is too long for byte value");
			}
			else {
				putCommand((byte)desc.operation,(byte)((typeDispl >> 8) & 0xFF),(byte)(typeDispl & 0xFF),(byte)forValue[0]);
				if (desc.operation == 0xc5) { // multianewarray
					changeStack(desc.stackChanges,(int)forValue[0]);
				}
				else {
					changeStack(desc.stackChanges);
				}
				skip2line(data,start);
			}
		}
		else {
			throw new ContentException("Missing comma and dimension parameter");
		}
	}

	private void processShortGlobalIndexCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final int	forResult[] = intArray; 
		
		start = calculateFieldAddressAndSignature(data,start,end,forResult);
		if (forResult[0] <= 0 || forResult[0] > Short.MAX_VALUE) {
			throw new ContentException("Calculated value ["+forResult[0]+"] is too long for short index");
		}
		else {
			putCommand((byte)desc.operation,(byte)((forResult[0] >> 8) & 0xFF),(byte)(forResult[0] & 0xFF));
			changeStack(desc.stackChanges,forResult[1]);
			skip2line(data,start);
		}
	}

	private void processShortBrunchCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final long	forResult[] = longArray;
		
		start = calculateBranchAddress(data,start,forResult);
		
		if (!isLabelExists(forResult[0])) {
			changeStack(desc.stackChanges);
			registerBranch(forResult[0],true);
		}
		else {
			changeStack(desc.stackChanges);
			registerBranch(forResult[0],true);
		}
		putCommand((byte)desc.operation,(byte)0,(byte)0);
		if (desc.uncondBrunch) {
			markLabelRequired(true);
		}
		skip2line(data,start);
	}

	private void processLongBrunchCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final long	forResult[] = longArray;
		
		start = calculateBranchAddress(data,start,forResult);
		if (!isLabelExists(forResult[0])) {
			changeStack(desc.stackChanges);
			registerBranch(forResult[0],false);
		}
		else {
			changeStack(desc.stackChanges);
			registerBranch(forResult[0],false);
		}
		putCommand((byte)desc.operation,(byte)0,(byte)0,(byte)0,(byte)0);
		if (desc.uncondBrunch) {
			markLabelRequired(true);
		}
		skip2line(data,start);
	}

	private int processCallCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final int	forResult[] = intArray, forArgsAndSignatures[] = new int[2];
		
		start = calculateMethodAddressAndSignature(data,start,end,forResult,forArgsAndSignatures);
		if (forResult[0] <= 0 || forResult[0] > 2*Short.MAX_VALUE) {
			throw new ContentException("Calculated value ["+forResult[0]+"] is too long for short index");
		}
		else {
			putCommand((byte)desc.operation,(byte)((forResult[0] >> 8) & 0xFF),(byte)(forResult[0] & 0xFF));
			changeStack(desc.stackChanges,forMethodTypes,forResult[1],forArgsAndSignatures[1]);
			skip2line(data,start);
		}
		return forArgsAndSignatures[0];
	}	

	private void processDynamicCallCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		start = InternalUtils.skipBlank(data, CharUtils.parseName(data, InternalUtils.skipBlank(data, start), intArray));
		final long	bootstrapMethodNameId = cc.getNameTree().seekName(data,intArray[0],intArray[1]);  
		final int	forResult[] = intArray, forArgsAndSignatures[] = new int[2];
		
		if (data[start] == ',') {
			start = calculateMethodAddressAndSignature(data, InternalUtils.skipBlank(data, start + 1), end, forResult, forArgsAndSignatures);
			if (forResult[0] <= 0 || forResult[0] > 2*Short.MAX_VALUE) {
				throw new ContentException("Calculated value ["+forResult[0]+"] is too long for short index");
			}
			else {
			}
		}
		else {
			
		}
		throw new ContentException("Don't use invokedynamic connand! Use direct link to the methods you need instead"); 
	}

	private void processInterfaceCallCommand(final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final int	argsSize = processCallCommand(desc,data,start,end)+1;
		putCommand((byte)(argsSize & 0xFF),(byte)((argsSize >> 8)& 0xFF));
	}

	private void prepareSwitchCommand(final byte opCode, final StackChanges stackChanges) throws ContentException, IOException {
		switchAddress = getPC(); 
		putCommand((byte)opCode);
		changeStack(stackChanges);
		alignPC(); 
		jumps.clear(); 
	}	
	
	private void processJumps(final char[] data, int start, final int end, final boolean explicitValue) throws ContentException {
		final long[]	forLabel = longArray;
		final long[]	forValue = new long[1];
		
		if (explicitValue) {
			start = calculateValue(data,start,EvalState.additional,forValue);
			if (start < end && data[start] == ',') {
				start = InternalUtils.skipBlank(data, calculateBranchAddress(data,UnsafedCharUtils.uncheckedSkipBlank(data,start+1,true),forLabel));
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
//			changeStack(desc.stackChanges);
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
			result[0] = methodDescriptor.getVarDispl(tree.placeOrChangeName(data,startName,endName,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
			return endName;
		}
	}
	
	private int calculateFieldAddressAndSignature(final char[] data, int start, final int end, final int[] result) throws IOException, ContentException {
		int	startName = start, endName = start = skipSimpleName(data,start);
		
		if (data[start] != '.') {
			final long	fieldId = tree.seekName(data,startName,endName);
//			final long	fieldId = tree.placeOrChangeName(data,startName,endName,new NameDescriptor());
			
			if ((result[0] = cc.getConstantPool().asFieldRefDescription(joinedClassNameId,fieldId)) != 0) {
				result[1] = tree.getCargo(fieldId).nameType;
				return start;
			}
		}
		endName = start = skipQualifiedName(data,start);
		final Field		f = cdr.getFieldDescription(data,startName,endName);
		final int		checkType = CompilerUtils.defineClassType(f.getType());
		final long		type = tree.placeOrChangeName((CharSequence)f.getType().getName().replace('.','/'),new NameDescriptor(checkType));
		
		result[0] = cc.getConstantPool().asFieldRefDescription(
							tree.placeOrChangeName((CharSequence)f.getDeclaringClass().getName().replace('.','/'),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
							tree.placeOrChangeName((CharSequence)f.getName(),new NameDescriptor(checkType)),
							tree.placeOrChangeName((CharSequence)InternalUtils.buildFieldSignature(tree,type),new NameDescriptor(checkType))
					);
		result[1] = CompilerUtils.defineClassType(f.getType());
		return start;
	}

	private int calculateMethodAddressAndSignature(final char[] data, int start, final int end, final int[] result, final int[] argsLengthAndRetSignature) throws IOException, ContentException {
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
					final String	signature = new String(data,endName,endSignature-endName);
					
					result[0] = cc.getConstantPool().asMethodRefDescription(
							tree.seekName((CharSequence)classOnly.replace('.','/')),
							tree.seekName((CharSequence)(classOnly.endsWith(methodOnly) ? "<init>" : methodOnly)),
							tree.seekName(data,endName,endSignature)
					);
					while ((result[1] = InternalUtils.methodSignature2Stack(signature,forMethodTypes)) < 0) {
						forMethodTypes = Arrays.copyOf(forMethodTypes,2*forMethodTypes.length);
					}
				}
				else {
					try{final Method	m = cdr.getMethodDescription(data,startName,endSignature);
						final int		retType = CompilerUtils.defineClassType(m.getReturnType());
					
						if (m.getDeclaringClass().isInterface()) {
							result[0] = cc.getConstantPool().asInterfaceMethodRefDescription(
									tree.placeOrChangeName((CharSequence)toCanonicalName(m.getDeclaringClass()).replace('.','/'),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
									tree.placeOrChangeName((CharSequence)m.getName(),new NameDescriptor(retType)),
									tree.placeOrChangeName((CharSequence)CompilerUtils.buildMethodSignature(m),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))
							);
						}
						else {
							result[0] = cc.getConstantPool().asMethodRefDescription(
									tree.placeOrChangeName((CharSequence)toCanonicalName(m.getDeclaringClass()).replace('.','/'),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
									tree.placeOrChangeName((CharSequence)m.getName(),new NameDescriptor(retType)),
									tree.placeOrChangeName((CharSequence)CompilerUtils.buildMethodSignature(m),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))
							);
						}
						argsLengthAndRetSignature[0] = 0;
						for (Parameter item : m.getParameters()) {
							argsLengthAndRetSignature[0] += item.getType() == long.class || item.getType() == double.class ? 2 : 1;
						}
						argsLengthAndRetSignature[1] = InternalUtils.methodSignature2Type(m);
						while ((result[1] = InternalUtils.methodSignature2Stack(m,forMethodTypes)) < 0) {
							forMethodTypes = Arrays.copyOf(forMethodTypes,2*forMethodTypes.length);
						}
					} catch (ContentException exc) {
						final Constructor<?>	c = cdr.getConstructorDescription(data,startName,endSignature);
						
						result[0] = cc.getConstantPool().asMethodRefDescription(
								tree.placeOrChangeName((CharSequence)c.getDeclaringClass().getName().replace('.','/'),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
								tree.placeOrChangeName((CharSequence)"<init>",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)),
								tree.placeOrChangeName((CharSequence)CompilerUtils.buildConstructorSignature(c),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))
						);
						argsLengthAndRetSignature[0] = 0;
						for (Parameter item : c.getParameters()) {
							argsLengthAndRetSignature[0] += item.getType() == long.class || item.getType() == double.class ? 2 : 1;
						}
						argsLengthAndRetSignature[1] = CompilerUtils.CLASSTYPE_VOID;
						while ((result[1] = InternalUtils.constructorSignature2Stack(c,forMethodTypes)) < 0) {
							forMethodTypes = Arrays.copyOf(forMethodTypes,2*forMethodTypes.length);
						}
					}
				}
			}
			else {
				throw new ContentException("Missing method signature!");
			}
		}
		else {
			if (data[start] == '(') {
				final int		startSignature = start, endSignature = start = skipSignature(data,start);
				final String	signature = new String(data,startSignature,endSignature-startSignature);
				final int		checkType = InternalUtils.methodSignature2Type(signature);
				
				result[0] = cc.getConstantPool().asMethodRefDescription(
						joinedClassNameId,
						tree.placeOrChangeName(data,startName,endName,new NameDescriptor(checkType)),
						tree.placeOrChangeName(data,startSignature,endSignature,new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))						
				);
				while ((result[1] = InternalUtils.methodSignature2Stack(signature,forMethodTypes)) < 0) {
					forMethodTypes = Arrays.copyOf(forMethodTypes,2*forMethodTypes.length);
				}
				argsLengthAndRetSignature[0] = result[1];
				argsLengthAndRetSignature[1] = checkType;
			}
			else {
				throw new ContentException("Missing method signature!");
			}
		}
		return start;
	}
	
	private int calculateValue(final char[] data, int start, final EvalState state, final long[] result) throws ContentException {
		long		value[] = null;
		char		symbol;
		
		switch (state) {
			case term				:
				switch (data[start]) {
					case '0' : 
						return UnsafedCharUtils.uncheckedParseLongExtended(data,start,result,true);
					case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						return UnsafedCharUtils.uncheckedParseLong(data,start,result,true);
					case '\'' : 
						char[]		charValue = new char[1];
						start = UnsafedCharUtils.uncheckedParseEscapedChar(data, start + 1, charValue);
						
						if (data[start] == '\'') {
							result[0] = charValue[0];
							return start + 1;
						}
						else {
							throw new ContentException("Unclosed '\'' in the expression");
						}
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
					if (value == null) {
						value = new long[] {0,0};
					}
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
					if (value == null) {
						value = new long[] {0,0};
					}
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
		
		result[0] = cc.getNameTree().placeOrChangeName(data,startName,endName,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		return start;
	}
	
	/*
	 * Utility methods
	 */
	private static short addAndCheckDuplicates(final short source, final short added, final String directive) throws ContentException {
		if ((source & added) != 0) {
			throw new ContentException("Duplicate option ["+Modifier.toString(added)+"] in the ["+directive+"] directive");
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
		long		parsed = 0; 	// Mark parsed options with its sequential numbers in the parameter's list
		int			startOption, endOption, optionCode;
		boolean		validOption;
		
		desc.clear();
		while (data[start] != '\n') {
			startOption = start = InternalUtils.skipBlank(data,start);
			endOption = start = skipSimpleName(data,start);
			if (startOption != endOption) {
				optionCode = (int)staticDirectiveTree.seekName(data,startOption,endOption);
				validOption = false;
				for (int index = 0, maxIndex = availableOptions.length; index < maxIndex; index++) {
					if (availableOptions[index] == optionCode) {
						if ((parsed & (1L << index)) != 0) {
							throw new ContentException("Duplicate option ["+new String(data,startOption,endOption-startOption)+"] for  ["+location+"] descriptor");
						}
						else {
							parsed |= (1L << index);
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
					else if (dd.getType() == DirectiveType.SPECIAL_FLAG) {
						desc.specialFlags |= ((DirectiveSpecialFlag)dd).getOption();
					}
					else if ((dd instanceof DirectiveClassOption) && treatExtendsAsImplements) {
						start = new DirectiveInterfacesOption().processList(data,start,cdr,desc);
					}
					else {
						start = dd.processList(data,start,cdr,desc);
					}
				}
			}
			else {
				break;
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
		final int	result = flags & (JavaByteCodeConstants.ACC_PUBLIC | JavaByteCodeConstants.ACC_PROTECTED | JavaByteCodeConstants.ACC_PRIVATE);
		
		return result == 3 || result == 5 || result == 6 || result == 7;
	}

	private static boolean checkMutualAbstractFinal(final short flags) {
		return (flags & JavaByteCodeConstants.ACC_ABSTRACT) != 0 && (flags & JavaByteCodeConstants.ACC_FINAL) != 0;
	}

	private static boolean checkMutualStaticAbstract(final short flags) {
		return (flags & JavaByteCodeConstants.ACC_ABSTRACT) != 0 && (flags & JavaByteCodeConstants.ACC_STATIC) != 0;
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

	private static int skipSignature(final char[] data, int from) throws ContentException {
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
			staticDirectiveTree.placeOrChangeName((CharSequence)mnemonics,code,desc);
		}
	}

	private static void placeStaticCommand(final int operation, final boolean uncondBrunch, final String mnemonics, final StackChanges stackChanges, final int... awaitedTypes) {
		placeStaticCommand(operation, uncondBrunch, mnemonics, CommandFormat.single, stackChanges, new int[0]);
	}

	private static void placeStaticCommand(final int operation, final boolean uncondBrunch, final String mnemonics, final int argumentType, final StackChanges stackChanges, final int... awaitedTypes) {
		placeStaticCommand(operation, uncondBrunch, mnemonics, CommandFormat.single, argumentType, stackChanges, new int[0]);
	}
	
	private static void placeStaticCommand(final int operation, final boolean uncondBrunch, final String mnemonics, final CommandFormat format, final StackChanges stackChanges, final int... awaitedTypes) {
		if (staticCommandTree.contains(operation)) {
			throw new IllegalArgumentException("Duplicate opcode ["+operation+"]: "+mnemonics+", already exists "+staticCommandTree.getName(operation));
		}
		else {
			staticCommandTree.placeOrChangeName((CharSequence)mnemonics,operation,new CommandDescriptor(operation,uncondBrunch,format,DONT_CHECK_LOCAL_TYPES,stackChanges,awaitedTypes));
		}
	}	

	private static void placeStaticCommand(final int operation, final boolean uncondBrunch, final String mnemonics, final CommandFormat format, final int argumentType, final StackChanges stackChanges, final int... awaitedTypes) {
		if (staticCommandTree.contains(operation)) {
			throw new IllegalArgumentException("Duplicate opcode ["+operation+"]: "+mnemonics+", already exists "+staticCommandTree.getName(operation));
		}
		else {
			staticCommandTree.placeOrChangeName((CharSequence)mnemonics,operation,new CommandDescriptor(operation,uncondBrunch,format,argumentType,stackChanges,awaitedTypes));
		}
	}	
	
	private static CommandDescriptor extractStaticCommand(final String mnemonics) {
		return staticCommandTree.getCargo(staticCommandTree.seekName((CharSequence)mnemonics));
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

	protected synchronized void printDiagnostics(final String text) throws IOException {
		if (diagnostics != null && printAssembler) {
			diagnostics.write(text);
			diagnostics.flush();
		}
	}
	
	private static class EntityDescriptor {
		public short			options;
		public short			specialFlags;
		@SuppressWarnings("unused")
		public Class<?>			extendsItem;
		public List<Class<?>>	implementsList = new ArrayList<>();
		public List<Class<?>>	throwsList = new ArrayList<>();
		
		public void clear() {
			options = 0;
			specialFlags = 0;
			extendsItem = null;
			implementsList.clear();
			throwsList.clear();
		}
	}
	
	private enum DirectiveType {
		OPTION, LIST, SPECIAL_FLAG
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

	private static class DirectiveSpecialFlag extends DirectiveDescriptor {
		private final short		option;
		
		public DirectiveSpecialFlag(short option) {
			super(DirectiveType.SPECIAL_FLAG);
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
		public final int			operation;
		public final boolean		uncondBrunch;
		public final CommandFormat	commandFormat;
		public final int			argumentType;
		public final StackChanges	stackChanges;
		public final int[]			checkedTypes;
		
		public CommandDescriptor(final int operation, final boolean uncondBrunch, final CommandFormat commandFormat, final int argumentType, final StackChanges stackChanges, final int[] checkedTypes) {
			this.operation = operation;
			this.uncondBrunch = uncondBrunch;
			this.commandFormat = commandFormat;
			this.argumentType = argumentType;
			this.stackChanges = stackChanges;
			this.checkedTypes = checkedTypes;
		}

		@Override
		public String toString() {
			return "CommandDescriptor [operation=" + operation + ", uncondBrunch=" + uncondBrunch + ", commandFormat="
					+ commandFormat + ", argumentType=" + argumentType + ", stackChanges=" + stackChanges
					+ ", checkedTypes=" + Arrays.toString(checkedTypes) + "]";
		}
	}
}
