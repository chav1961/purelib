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
import chav1961.purelib.streams.char2byte.asm.StackAndVarRepoNew.TypeDescriptor;
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

	static final NameDescriptor						VOID_DESCRIPTOR = new NameDescriptor(CompilerUtils.CLASSTYPE_VOID);
	
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

	private static final int						TRY_START_PC = 0;
	private static final int						TRY_END_PC = 1;
	private static final int						TRY_STACK_DEPTH = 2;	
	private static final int						TRY_VARFRAME_LENGTH = 3;	
	
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
	
	private static final TypeDescriptor				INT_DESC = new TypeDescriptor(CompilerUtils.CLASSTYPE_INT);
	private static final TypeDescriptor				LONG_DESC = new TypeDescriptor(CompilerUtils.CLASSTYPE_LONG);
	private static final TypeDescriptor				FLOAT_DESC = new TypeDescriptor(CompilerUtils.CLASSTYPE_FLOAT);
	private static final TypeDescriptor				DOUBLE_DESC = new TypeDescriptor(CompilerUtils.CLASSTYPE_DOUBLE);
	
	private static final byte						LDC_OPCODE;
	private static final byte						LDC_W_OPCODE;
	private static final byte						WIDE_OPCODE;
	private static final byte						MULTIANEWARRAY_OPCODE;
	
	private static final TypeDescriptor				ZERO_REF_TYPE = new TypeDescriptor();
	
	private enum EvalState {
		term, unary, multiplicational, additional 
	}
	
	private enum ParserState {
		beforePackage, insideMacros, beforeImport, insideClass, insideInterface, insideInterfaceAbstractMethod, insideClassAbstractMethod, insideClassMethod, insideClassBody, insideBegin, insideBeginCode,insideMethodLookup, insideMethodTable, afterClass
	}
	
	private enum CommandFormat{
		single, byteIndex, extendableByteIndex, byteValue, shortValue, byteType, byteIndexAndByteValue, shortIndexAndByteValue, classShortIndex, valueByteIndex, valueShortIndex, valueShortIndex2, shortBrunch, longBrunch, shortGlobalIndex, call, callDynamic, callInterface, lookupSwitch, tableSwitch, restricted
	}

	private enum RefTypeSource {
		none, command, locaVariable, staticField, instanceField, stack2, returnedValue 
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
		
		placeStaticCommand(0x32,false,RefTypeSource.stack2,"aaload",StackChanges.pop2AndPushReference,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x53,false,RefTypeSource.none,"aastore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x01,false,RefTypeSource.none,"aconst_null",StackChanges.pushNull,StackAndVarRepoNew.SPECIAL_TYPE_NULL);
		placeStaticCommand(0x19,false,RefTypeSource.locaVariable,"aload",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pushReference);
		placeStaticCommand(0x2a,false,RefTypeSource.locaVariable,"aload_0",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pushReference);
		placeStaticCommand(0x2b,false,RefTypeSource.locaVariable,"aload_1",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pushReference);
		placeStaticCommand(0x2c,false,RefTypeSource.locaVariable,"aload_2",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pushReference);
		placeStaticCommand(0x2d,false,RefTypeSource.locaVariable,"aload_3",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pushReference);
		placeStaticCommand(0xbd,false,RefTypeSource.command,"anewarray",CommandFormat.classShortIndex,StackChanges.popAndPushReference,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xb0,true,RefTypeSource.none,"areturn",StackChanges.clear,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xbe,false,RefTypeSource.none,"arraylength",StackChanges.popAndPushInt,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x3a,false,RefTypeSource.none,"astore",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x4b,false,RefTypeSource.none,"astore_0",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x4c,false,RefTypeSource.none,"astore_1",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x4d,false,RefTypeSource.none,"astore_2",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x4e,false,RefTypeSource.none,"astore_3",CompilerUtils.CLASSTYPE_REFERENCE,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xbf,true,RefTypeSource.none,"athrow",StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x33,false,RefTypeSource.stack2,"baload",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x54,false,RefTypeSource.none,"bastore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x10,false,RefTypeSource.none,"bipush",CommandFormat.byteValue,StackChanges.pushInt);
		placeStaticCommand(0x34,false,RefTypeSource.stack2,"caload",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x55,false,RefTypeSource.none,"castore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xc0,false,RefTypeSource.command,"checkcast",CommandFormat.classShortIndex,StackChanges.changeType,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x90,false,RefTypeSource.none,"d2f",StackChanges.changeDouble2Float,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x8e,false,RefTypeSource.none,"d2i",StackChanges.changeDouble2Int,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x8f,false,RefTypeSource.none,"d2l",StackChanges.changeDouble2Long,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x63,false,RefTypeSource.none,"dadd",StackChanges.pop4AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x31,false,RefTypeSource.stack2,"daload",StackChanges.pop2AndPushDouble,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x52,false,RefTypeSource.none,"dastore",StackChanges.pop4,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x98,false,RefTypeSource.none,"dcmpg",StackChanges.pop4AndPushInt,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x97,false,RefTypeSource.none,"dcmpl",StackChanges.pop4AndPushInt,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x0e,false,RefTypeSource.none,"dconst_0",StackChanges.pushDouble);
		placeStaticCommand(0x0f,false,RefTypeSource.none,"dconst_1",StackChanges.pushDouble);
		placeStaticCommand(0x6f,false,RefTypeSource.none,"ddiv",StackChanges.pop4AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x18,false,RefTypeSource.none,"dload",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pushDouble);
		placeStaticCommand(0x26,false,RefTypeSource.none,"dload_0",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pushDouble);
		placeStaticCommand(0x27,false,RefTypeSource.none,"dload_1",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pushDouble);
		placeStaticCommand(0x28,false,RefTypeSource.none,"dload_2",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pushDouble);
		placeStaticCommand(0x29,false,RefTypeSource.none,"dload_3",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pushDouble);
		placeStaticCommand(0x6b,false,RefTypeSource.none,"dmul",StackChanges.pop4AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x77,false,RefTypeSource.none,"dneg",StackChanges.pop2AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x73,false,RefTypeSource.none,"drem",StackChanges.pop4AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0xaf,true,RefTypeSource.none,"dreturn",StackChanges.clear,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x39,false,RefTypeSource.none,"dstore",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pop2,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x47,false,RefTypeSource.none,"dstore_0",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pop2,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x48,false,RefTypeSource.none,"dstore_1",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pop2,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x49,false,RefTypeSource.none,"dstore_2",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pop2,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x4a,false,RefTypeSource.none,"dstore_3",CompilerUtils.CLASSTYPE_DOUBLE,StackChanges.pop2,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x67,false,RefTypeSource.none,"dsub",StackChanges.pop4AndPushDouble,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x59,false,RefTypeSource.none,"dup",StackChanges.dup);
		placeStaticCommand(0x5a,false,RefTypeSource.none,"dup_x1",StackChanges.dup_x1);
		placeStaticCommand(0x5b,false,RefTypeSource.none,"dup_x2",StackChanges.dup_x2);
		placeStaticCommand(0x5c,false,RefTypeSource.none,"dup2",StackChanges.dup2);
		placeStaticCommand(0x5d,false,RefTypeSource.none,"dup2_x1",StackChanges.dup2_x1);
		placeStaticCommand(0x5e,false,RefTypeSource.none,"dup2_x2",StackChanges.dup2_x2);
		placeStaticCommand(0x8d,false,RefTypeSource.none,"f2d",StackChanges.changeFloat2Double,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x8b,false,RefTypeSource.none,"f2i",StackChanges.changeFloat2Int,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x8c,false,RefTypeSource.none,"f2l",StackChanges.changeFloat2Long,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x62,false,RefTypeSource.none,"fadd",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x30,false,RefTypeSource.stack2,"faload",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x51,false,RefTypeSource.none,"fastore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x96,false,RefTypeSource.none,"fcmpg",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x95,false,RefTypeSource.none,"fcmpl",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x0b,false,RefTypeSource.none,"fconst_0",StackChanges.pushFloat);
		placeStaticCommand(0x0c,false,RefTypeSource.none,"fconst_1",StackChanges.pushFloat);
		placeStaticCommand(0x0d,false,RefTypeSource.none,"fconst_2",StackChanges.pushFloat);
		placeStaticCommand(0x6e,false,RefTypeSource.none,"fdiv",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x17,false,RefTypeSource.none,"fload",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pushFloat);
		placeStaticCommand(0x22,false,RefTypeSource.none,"fload_0",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pushFloat);
		placeStaticCommand(0x23,false,RefTypeSource.none,"fload_1",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pushFloat);
		placeStaticCommand(0x24,false,RefTypeSource.none,"fload_2",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pushFloat);
		placeStaticCommand(0x25,false,RefTypeSource.none,"fload_3",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pushFloat);
		placeStaticCommand(0x6a,false,RefTypeSource.none,"fmul",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x76,false,RefTypeSource.none,"fneg",StackChanges.popAndPushFloat,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x72,false,RefTypeSource.none,"frem",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0xae,true,RefTypeSource.none,"freturn",StackChanges.clear,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x38,false,RefTypeSource.none,"fstore",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pop,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x43,false,RefTypeSource.none,"fstore_0",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pop,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x44,false,RefTypeSource.none,"fstore_1",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pop,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x45,false,RefTypeSource.none,"fstore_2",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pop,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x46,false,RefTypeSource.none,"fstore_3",CompilerUtils.CLASSTYPE_FLOAT,StackChanges.pop,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0x66,false,RefTypeSource.none,"fsub",StackChanges.pop2AndPushFloat,CompilerUtils.CLASSTYPE_FLOAT,CompilerUtils.CLASSTYPE_FLOAT);
		placeStaticCommand(0xb4,false,RefTypeSource.instanceField,"getfield",CommandFormat.shortGlobalIndex,StackChanges.pushField);
		placeStaticCommand(0xb2,false,RefTypeSource.staticField,"getstatic",CommandFormat.shortGlobalIndex,StackChanges.pushStatic);
		placeStaticCommand(0xa7,true,RefTypeSource.none,"goto",CommandFormat.shortBrunch,StackChanges.none);
		placeStaticCommand(0xc8,true,RefTypeSource.none,"goto_w",CommandFormat.longBrunch,StackChanges.none);
		placeStaticCommand(0x91,false,RefTypeSource.none,"i2b",StackChanges.none,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x92,false,RefTypeSource.none,"i2c",StackChanges.none,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x87,false,RefTypeSource.none,"i2d",StackChanges.changeInt2Double,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x86,false,RefTypeSource.none,"i2f",StackChanges.changeInt2Float,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x85,false,RefTypeSource.none,"i2l",StackChanges.changeInt2Long,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x93,false,RefTypeSource.none,"i2s",StackChanges.none,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x60,false,RefTypeSource.none,"iadd",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x2e,false,RefTypeSource.stack2,"iaload",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x7e,false,RefTypeSource.none,"iand",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x4f,false,RefTypeSource.none,"iastore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x02,false,RefTypeSource.none,"iconst_m1",StackChanges.pushInt);
		placeStaticCommand(0x03,false,RefTypeSource.none,"iconst_0",StackChanges.pushInt);
		placeStaticCommand(0x04,false,RefTypeSource.none,"iconst_1",StackChanges.pushInt);
		placeStaticCommand(0x05,false,RefTypeSource.none,"iconst_2",StackChanges.pushInt);
		placeStaticCommand(0x06,false,RefTypeSource.none,"iconst_3",StackChanges.pushInt);
		placeStaticCommand(0x07,false,RefTypeSource.none,"iconst_4",StackChanges.pushInt);
		placeStaticCommand(0x08,false,RefTypeSource.none,"iconst_5",StackChanges.pushInt);
		placeStaticCommand(0x6c,false,RefTypeSource.none,"idiv",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9f,false,RefTypeSource.none,"if_icmpeq",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa0,false,RefTypeSource.none,"if_icmpne",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa1,false,RefTypeSource.none,"if_icmplt",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa2,false,RefTypeSource.none,"if_icmpge",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa3,false,RefTypeSource.none,"if_icmpgt",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa4,false,RefTypeSource.none,"if_icmple",CommandFormat.shortBrunch,StackChanges.pop2,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x99,false,RefTypeSource.none,"ifeq",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9a,false,RefTypeSource.none,"ifne",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9b,false,RefTypeSource.none,"iflt",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9c,false,RefTypeSource.none,"ifge",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9d,false,RefTypeSource.none,"ifgt",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x9e,false,RefTypeSource.none,"ifle",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xc7,false,RefTypeSource.none,"ifnonnull",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xc6,false,RefTypeSource.none,"ifnull",CommandFormat.shortBrunch,StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0x84,false,RefTypeSource.none,"iinc",CommandFormat.byteIndexAndByteValue,StackChanges.none);
		placeStaticCommand(0x15,false,RefTypeSource.none,"iload",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_INT,StackChanges.pushInt);
		placeStaticCommand(0x1a,false,RefTypeSource.none,"iload_0",CompilerUtils.CLASSTYPE_INT,StackChanges.pushInt);
		placeStaticCommand(0x1b,false,RefTypeSource.none,"iload_1",CompilerUtils.CLASSTYPE_INT,StackChanges.pushInt);
		placeStaticCommand(0x1c,false,RefTypeSource.none,"iload_2",CompilerUtils.CLASSTYPE_INT,StackChanges.pushInt);
		placeStaticCommand(0x1d,false,RefTypeSource.none,"iload_3",CompilerUtils.CLASSTYPE_INT,StackChanges.pushInt);
		placeStaticCommand(0x68,false,RefTypeSource.none,"imul",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x74,false,RefTypeSource.none,"ineg",StackChanges.popAndPushInt,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xc1,false,RefTypeSource.none,"instanceof",CommandFormat.classShortIndex,StackChanges.popAndPushInt,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xba,false,RefTypeSource.returnedValue,"invokedynamic",CommandFormat.callDynamic,StackChanges.none);
		placeStaticCommand(0xb9,false,RefTypeSource.returnedValue,"invokeinterface",CommandFormat.callInterface,StackChanges.callAndPush);
		placeStaticCommand(0xb7,false,RefTypeSource.returnedValue,"invokespecial",CommandFormat.call,StackChanges.callAndPush);
		placeStaticCommand(0xb8,false,RefTypeSource.returnedValue,"invokestatic",CommandFormat.call,StackChanges.callStaticAndPush);
		placeStaticCommand(0xb6,false,RefTypeSource.returnedValue,"invokevirtual",CommandFormat.call,StackChanges.callAndPush);
		placeStaticCommand(0x80,false,RefTypeSource.none,"ior",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x70,false,RefTypeSource.none,"irem",StackChanges.popAndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xac,true,RefTypeSource.none,"ireturn",StackChanges.clear,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x78,false,RefTypeSource.none,"ishl",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x7a,false,RefTypeSource.none,"ishr",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x36,false,RefTypeSource.none,"istore",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_INT,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x3b,false,RefTypeSource.none,"istore_0",CompilerUtils.CLASSTYPE_INT,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x3c,false,RefTypeSource.none,"istore_1",CompilerUtils.CLASSTYPE_INT,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x3d,false,RefTypeSource.none,"istore_2",CompilerUtils.CLASSTYPE_INT,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x3e,false,RefTypeSource.none,"istore_3",CompilerUtils.CLASSTYPE_INT,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x64,false,RefTypeSource.none,"isub",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x7c,false,RefTypeSource.none,"iushr",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x82,false,RefTypeSource.none,"ixor",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xa8,false,RefTypeSource.none,"jsr",CommandFormat.restricted,StackChanges.none);
		placeStaticCommand(0xc9,false,RefTypeSource.none,"jsr_w",CommandFormat.restricted,StackChanges.none);
		placeStaticCommand(0x8a,false,RefTypeSource.none,"l2d",StackChanges.changeLong2Double,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x89,false,RefTypeSource.none,"l2f",StackChanges.changeLong2Float,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x88,false,RefTypeSource.none,"l2i",StackChanges.changeLong2Int,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x61,false,RefTypeSource.none,"ladd",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x2f,false,RefTypeSource.stack2,"laload",StackChanges.pop2AndPushLong,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x7f,false,RefTypeSource.none,"land",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x50,false,RefTypeSource.none,"lastore",StackChanges.pop2,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x94,false,RefTypeSource.none,"lcmp",StackChanges.pop4AndPushInt,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x09,false,RefTypeSource.none,"lconst_0",StackChanges.pushLong);
		placeStaticCommand(0x0a,false,RefTypeSource.none,"lconst_1",StackChanges.pushLong);
		placeStaticCommand(0x12,false,RefTypeSource.command,"ldc",CommandFormat.valueByteIndex,StackChanges.none);
		placeStaticCommand(0x13,false,RefTypeSource.command,"ldc_w",CommandFormat.valueShortIndex,StackChanges.none);
		placeStaticCommand(0x14,false,RefTypeSource.command,"ldc2_w",CommandFormat.valueShortIndex2,StackChanges.none);
		placeStaticCommand(0x6d,false,RefTypeSource.none,"ldiv",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x16,false,RefTypeSource.none,"lload",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_LONG,StackChanges.pushLong);
		placeStaticCommand(0x1e,false,RefTypeSource.none,"lload_0",CompilerUtils.CLASSTYPE_LONG,StackChanges.pushLong);
		placeStaticCommand(0x1f,false,RefTypeSource.none,"lload_1",CompilerUtils.CLASSTYPE_LONG,StackChanges.pushLong);
		placeStaticCommand(0x20,false,RefTypeSource.none,"lload_2",CompilerUtils.CLASSTYPE_LONG,StackChanges.pushLong);
		placeStaticCommand(0x21,false,RefTypeSource.none,"lload_3",CompilerUtils.CLASSTYPE_LONG,StackChanges.pushLong);
		placeStaticCommand(0x69,false,RefTypeSource.none,"lmul",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x75,false,RefTypeSource.none,"lneg",StackChanges.pop2AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0xab,true,RefTypeSource.none,"lookupswitch",CommandFormat.lookupSwitch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x81,false,RefTypeSource.none,"lor",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x71,false,RefTypeSource.none,"lrem",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0xad,true,RefTypeSource.none,"lreturn",StackChanges.clear,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x79,false,RefTypeSource.none,"lshl",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x7b,false,RefTypeSource.none,"lshr",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x37,false,RefTypeSource.none,"lstore",CommandFormat.extendableByteIndex,CompilerUtils.CLASSTYPE_LONG,StackChanges.pop2,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x3f,false,RefTypeSource.none,"lstore_0",CompilerUtils.CLASSTYPE_LONG,StackChanges.pop2,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x40,false,RefTypeSource.none,"lstore_1",CompilerUtils.CLASSTYPE_LONG,StackChanges.pop2,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x41,false,RefTypeSource.none,"lstore_2",CompilerUtils.CLASSTYPE_LONG,StackChanges.pop2,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x42,false,RefTypeSource.none,"lstore_3",CompilerUtils.CLASSTYPE_LONG,StackChanges.pop2,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x65,false,RefTypeSource.none,"lsub",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0x7d,false,RefTypeSource.none,"lushr",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x83,false,RefTypeSource.none,"lxor",StackChanges.pop4AndPushLong,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepoNew.SPECIAL_TYPE_TOP);
		placeStaticCommand(0xc2,false,RefTypeSource.none,"monitorenter",StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xc3,false,RefTypeSource.none,"monitorexit",StackChanges.pop,CompilerUtils.CLASSTYPE_REFERENCE);
		placeStaticCommand(0xc5,false,RefTypeSource.command,"multianewarray",CommandFormat.shortIndexAndByteValue,StackChanges.multiarrayAndPushReference);
		placeStaticCommand(0xbb,false,RefTypeSource.command,"new",CommandFormat.classShortIndex,StackChanges.pushReference);
		placeStaticCommand(0xbc,false,RefTypeSource.command,"newarray",CommandFormat.byteType,StackChanges.popAndPushReference);
		placeStaticCommand(0x00,false,RefTypeSource.none,"nop",StackChanges.none);
		placeStaticCommand(0x57,false,RefTypeSource.none,"pop",StackChanges.pop);
		placeStaticCommand(0x58,false,RefTypeSource.none,"pop2",StackChanges.pop2);
		placeStaticCommand(0xb5,false,RefTypeSource.none,"putfield",CommandFormat.shortGlobalIndex,StackChanges.popField);
		placeStaticCommand(0xb3,false,RefTypeSource.none,"putstatic",CommandFormat.shortGlobalIndex,StackChanges.popStatic);
		placeStaticCommand(0xa9,true,RefTypeSource.none,"ret",CommandFormat.restricted,StackChanges.none);
		placeStaticCommand(0xb1,true,RefTypeSource.none,"return",StackChanges.clear);
		placeStaticCommand(0x35,false,RefTypeSource.stack2,"saload",StackChanges.pop2AndPushInt,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x56,false,RefTypeSource.none,"sastore",StackChanges.pop3,CompilerUtils.CLASSTYPE_REFERENCE,CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0x11,false,RefTypeSource.none,"sipush",CommandFormat.shortValue,StackChanges.pushInt);
		placeStaticCommand(0x5f,false,RefTypeSource.none,"swap",StackChanges.swap);
		placeStaticCommand(0xaa,true,RefTypeSource.none,"tableswitch",CommandFormat.tableSwitch,StackChanges.pop,CompilerUtils.CLASSTYPE_INT);
		placeStaticCommand(0xc4,false,RefTypeSource.none,"wide",CommandFormat.restricted,StackChanges.none);
		
		LDC_OPCODE = (byte) staticCommandTree.seekName((CharSequence)"ldc");
		LDC_W_OPCODE = (byte) staticCommandTree.seekName((CharSequence)"ldc_w");
		WIDE_OPCODE = (byte) staticCommandTree.seekName((CharSequence)"wide");
		MULTIANEWARRAY_OPCODE = (byte) staticCommandTree.seekName((CharSequence)"multianewarray");
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
	private final long									constructorId, classConstructorId, voidId;
	private final long									callSiteId, methodHandlesLookupId, stringId, methodTypeId;
	private final long									longArray[] = new long[2];	// Temporary arrays to use in different calls
	private final long									longArray2[] = new long[2];
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
	private List<long[]>								tryList = new ArrayList<>();
	private Macros										currentMacros = null;
	private boolean										addLines2Class = true;
	private boolean										addLines2ClassManually = false;
	private boolean										addVarTable = false, addVarTableInMethod = false;
	private TypeDescriptor[]							forMethodTypes = new TypeDescriptor[16];
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
		this.constructorId = tree.placeOrChangeName(CONSTRUCTOR,0,CONSTRUCTOR.length, VOID_DESCRIPTOR);
		this.classConstructorId = tree.placeOrChangeName(CLASS_CONSTRUCTOR,0,CLASS_CONSTRUCTOR.length, VOID_DESCRIPTOR);
		this.voidId = tree.placeOrChangeName(VOID, 0, VOID.length, VOID_DESCRIPTOR);
		this.callSiteId = tree.placeOrChangeName(CALLSITE, 0, CALLSITE.length, VOID_DESCRIPTOR);
		this.methodHandlesLookupId = tree.placeOrChangeName(METHODHANDLESLOOKUP, 0, METHODHANDLESLOOKUP.length, VOID_DESCRIPTOR);
		this.stringId = tree.placeOrChangeName(STRING, 0, STRING.length, VOID_DESCRIPTOR);
		this.methodTypeId = tree.placeOrChangeName(METHODTYPE, 0, METHODTYPE.length, VOID_DESCRIPTOR);
		this.printAssembler = !PureLibSettings.instance().getProperty(PureLibSettings.SUPPRESS_PRINT_ASSEMBLER, boolean.class, "true") && diagnostics != null;
		this.printExpandedMacros = printAssembler && PureLibSettings.instance().getProperty(PureLibSettings.PRINT_EXPANDED_MACROS, boolean.class, "false");		

		tree.placeOrChangeName(TRUE, 0, TRUE.length, VOID_DESCRIPTOR);	
		tree.placeOrChangeName(FALSE, 0, FALSE.length, VOID_DESCRIPTOR);	
		tree.placeOrChangeName(LONG, 0, LONG.length, VOID_DESCRIPTOR);	
		tree.placeOrChangeName(DOUBLE, 0, DOUBLE.length, VOID_DESCRIPTOR);	
		tree.placeOrChangeName(THIS, 0, THIS.length, VOID_DESCRIPTOR);	
		tree.placeOrChangeName(METHODHANDLE, 0, METHODHANDLE.length, VOID_DESCRIPTOR);	
	}
	
	@Override
	public void processLine(final long displacement, final int lineNo, final char[] data, final int from, final int len) throws IOException {
		int		start = from, end = Math.min(from + len, data.length);
		int		startName, endName, startDir, endDir;
		long	id = -1;

		if (diagnostics != null && printAssembler) {
			printDiagnostics(lineNo, data, from, len);
		}
		
		try{
			if (state == ParserState.insideMacros) {	// Redirect macros code into macros
			    currentMacros.processLine(displacement,lineNo,data,from,len);
				
				if (currentMacros.isPrepared()) {
					final String	macroName = new String(currentMacros.getName());
					
					if (macros.seekName((CharSequence)macroName) >= 0) {
						throw CompilerErrors.ERR_DUPLICATE_MACROS.syntaxError(lineNo, macroName);
					}
					else {
						final GrowableCharArray<?>	writer = new GrowableCharArray<>(true), stringRepo = new GrowableCharArray<>(false);
						final String				className = this.getClass().getPackage().getName()+'.'+new String(currentMacros.getName()); 
						
						try{MacroCompiler.compile(className, currentMacros.getRoot(), writer, stringRepo);
							currentMacros.compile(loader.createClass(className,writer).getConstructor(char[].class).newInstance(stringRepo.extract()));
							macros.placeOrChangeName((CharSequence)macroName,currentMacros);
						} catch (CalculationException | InstantiationException | RuntimeException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
							e.printStackTrace();
							throw CompilerErrors.ERR_MACROS_INVOCATION_ERROR.syntaxError(lineNo, className, e.getLocalizedMessage()); 
						} catch (VerifyError | ClassFormatError e) {
							e.printStackTrace();
							throw CompilerErrors.ERR_MACROS_CODE_CORRUPTED.error(className, e.getLocalizedMessage(), new String(writer.extract())); 
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
					throw CompilerErrors.ERR_ILLEGAL_LABEL_ENTRY_NAME.syntaxError(lineNo);
				}
				
				id = tree.placeOrChangeName(data,startName,endName,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
				
				start = InternalUtils.skipBlank(data,start);
				if (data[start] == ':') {
					switch (state) {
						case insideClassBody : case insideBegin : case insideBeginCode :
							putLabel(lineNo, id);
							break;
						default :
							throw CompilerErrors.ERR_BRANCH_LABEL_OUTSIDE_METHOD_BODY.syntaxError(lineNo);
					}
					start++;
					id = -1;
				}
			}
			
			startDir = start = InternalUtils.skipBlank(data,start);
			
			switch (data[start]) {
				case '.' :
					endDir = start = skipSimpleName(data, start + 1);
	
					switch ((int)staticDirectiveTree.seekName(data,startDir,endDir)) {
						case DIR_CLASS	:
							checkLabel(lineNo, id, true);
							switch (state) {
								case beforePackage :
								case beforeImport :
									processClassDir(lineNo, id, data, InternalUtils.skipBlank(data,start));
									state = ParserState.insideClass;
									break;
								case afterClass :
									throw CompilerErrors.ERR_DUPLICATE_CLASS_DIRECTIVE.syntaxError(lineNo);
								default :
									throw CompilerErrors.ERR_NESTED_CLASS_DIRECTIVE.syntaxError(lineNo);
							}						
							break;
						case DIR_INTERFACE:
							checkLabel(lineNo, id, true);
							switch (state) {
								case beforePackage :
								case beforeImport :
									processInterfaceDir(lineNo, id, data, InternalUtils.skipBlank(data,start));
									state = ParserState.insideInterface;
									break;
								case afterClass :
									throw CompilerErrors.ERR_DUPLICATE_CLASS_DIRECTIVE.syntaxError(lineNo);
								default :
									throw CompilerErrors.ERR_NESTED_CLASS_DIRECTIVE.syntaxError(lineNo);
							}						
							break;
						case DIR_FIELD	:
							checkLabel(lineNo, id, true);
							switch (state) {
								case insideClass :
									processClassFieldDir(lineNo, id, data, InternalUtils.skipBlank(data,start));
									break;
								case insideInterface :
									processInterfaceFieldDir(lineNo, id, data, InternalUtils.skipBlank(data,start));
									break;
								case beforePackage :
								case beforeImport :
								case afterClass :
									throw CompilerErrors.ERR_FIELD_DIRECTIVE_OUTSIDE_CLASS.syntaxError(lineNo);
								default :
									throw CompilerErrors.ERR_FIELD_DIRECTIVE_INSIDE_METHOD.syntaxError(lineNo);
							}
							break;
						case DIR_METHOD	:
							checkLabel(lineNo, id, true);
							switch (state) {
								case insideClass :
									processClassMethodDir(lineNo, id, data, InternalUtils.skipBlank(data,start));
									state = methodDescriptor.isAbstract() ? ParserState.insideClassAbstractMethod : ParserState.insideClassMethod;
									break;
								case insideInterface :
									processInterfaceMethodDir(lineNo, id, data, InternalUtils.skipBlank(data,start));
									state = ParserState.insideInterfaceAbstractMethod;
									break;
								case beforePackage :
								case beforeImport :
								case afterClass :
									throw CompilerErrors.ERR_METHOD_DIRECTIVE_OUTSIDE_CLASS.syntaxError(lineNo);
								default :
									throw CompilerErrors.ERR_NESTED_METHOD_DIRECTIVE.syntaxError(lineNo);
							}
							break;
						case DIR_PARAMETER	:
							checkLabel(lineNo, id, true);
							switch (state) {
								case insideClassAbstractMethod :
								case insideInterfaceAbstractMethod :
								case insideClassMethod :
									processParameterDir(lineNo, id, data, InternalUtils.skipBlank(data, start));
									break;
								default :
									throw CompilerErrors.ERR_PARAMETER_DIRECTIVE_OUTSIDE_METHOD.syntaxError(lineNo);
							}
							break;
						case DIR_VAR	:
							checkLabel(lineNo, id, true);
							switch (state) {
								case insideClassBody : case insideBegin :
									processVarDir(lineNo, id, data, InternalUtils.skipBlank(data,start));
									break;
								default :
									throw CompilerErrors.ERR_VAR_DIRECTIVE_OUTSIDE_METHOD.syntaxError(lineNo);
							}
							break;
						case DIR_BEGIN	:
							switch (state) {
								case insideBeginCode :
									state = ParserState.insideBegin;
								case insideBegin : 
									beginLevel++;
									methodDescriptor.push();
									methodDescriptor.getBody().getStackAndVarRepoNew().pushVarFrame((short)getPC());
									break;
								case insideClassMethod :
								case insideClassBody :
									beforeBegin = state;
									state = ParserState.insideBegin;
									beginLevel++;
									methodDescriptor.push();
									methodDescriptor.getBody().getStackAndVarRepoNew().pushVarFrame((short)getPC());
									break;
								default :
									throw CompilerErrors.ERR_BEGIN_DIRECTIVE_OUTSIDE_METHOD.syntaxError(lineNo);
							}
							break;
						case DIR_END	:
							switch (state) {
								case insideClass :
								case insideInterface :
									checkLabel(lineNo, id, true);
									if (id != classNameId) {
										throw CompilerErrors.ERR_UNPAIRED_END_DIRECTIVE.syntaxError(lineNo, "class", tree.getName(id), tree.getName(classNameId));
									}
									else {
										state = ParserState.afterClass;
									}
									break;
								case insideClassAbstractMethod :
									checkLabel(lineNo, id, true);
									if (id != methodNameId) {
										throw CompilerErrors.ERR_UNPAIRED_END_DIRECTIVE.syntaxError(lineNo, "method", tree.getName(id), tree.getName(methodNameId));
									}
									else {
										methodDescriptor.complete();
										addVarTableInMethod = false;
										state = ParserState.insideClass;
									}
									break;
								case insideClassMethod :
									throw CompilerErrors.ERR_METHOD_BODY_IS_MISSING.syntaxError(lineNo);
								case insideClassBody :
									checkLabel(lineNo, id, true);
									if (id != methodNameId && id != classNameId) {
										throw CompilerErrors.ERR_UNPAIRED_END_DIRECTIVE.syntaxError(lineNo, "method", tree.getName(id), tree.getName(methodNameId));
									}
									else if (!areTryBlocksClosed()) {
										throw CompilerErrors.ERR_UNCLOSED_TRY_BLOCK.syntaxError(lineNo, tree.getName(methodNameId));
									}
									else {
										methodDescriptor.complete();
										addVarTableInMethod = false;
										state = ParserState.insideClass;
									}
									break;
								case insideInterfaceAbstractMethod :
									checkLabel(lineNo, id, true);
									if (id != methodNameId) {
										throw CompilerErrors.ERR_UNPAIRED_END_DIRECTIVE.syntaxError(lineNo, "method", tree.getName(id), tree.getName(methodNameId));
									}
									else {
										methodDescriptor.complete();
										addVarTableInMethod = false;
										state = ParserState.insideInterface;
									}
									break;
								case insideBegin : case insideBeginCode :
									if (beginLevel > 0) {
										methodDescriptor.pop();
										methodDescriptor.getBody().getStackAndVarRepoNew().popVarFrame();
										beginLevel--;
										if (stackSize4CurrentMethod == STACK_OPTIMISTIC) {
											methodDescriptor.getBody().getStackAndVarRepoNew().popVarFrame();
										}
									}
									else {
										state = beforeBegin;
										checkLabel(lineNo, id, true);
										if (id != methodNameId) {
											throw CompilerErrors.ERR_UNPAIRED_END_DIRECTIVE.syntaxError(lineNo, "method", tree.getName(id), tree.getName(methodNameId));
										}
										else {
											methodDescriptor.complete();
											addVarTableInMethod = false;
											state = ParserState.insideClass;
										}
									}
									if (beginLevel > 0) {
										prepareStackMapRecord(0);
									}
									break;
								case insideMethodLookup :
									fillLookup(lineNo);	
									jumps.clear();
									state = ParserState.insideBeginCode;
									markLabelRequired(true);
									break;
								case insideMethodTable :
									fillTable(lineNo);	
									jumps.clear();
									state = ParserState.insideBeginCode; 
									markLabelRequired(true);
									break;
								default :
									throw CompilerErrors.ERR_END_DIRECTIVE_OUT_OF_CONTEXT.syntaxError(lineNo);
							}
							skip2line(lineNo, data, start);
							break;
						case DIR_STACK	:
							checkLabel(lineNo, id, false);
							switch (state) {
								case insideClassMethod :
									processStackDir(lineNo, data, InternalUtils.skipBlank(data,start));
									state = ParserState.insideClassBody;
									methodLineNo = 0;
									break;
								case insideClassBody : case insideBegin : case insideBeginCode :
									throw CompilerErrors.ERR_DUPLICATE_STACK_DIRECTIVE.syntaxError(lineNo);
								default :
									throw CompilerErrors.ERR_STACK_DIRECTIVE_OUTSIDE_METHOD.syntaxError(lineNo);
							}
							break;
						case DIR_PACKAGE:
							checkLabel(lineNo, id, false);
							switch (state) {
								case beforePackage :
									processPackageDir(lineNo, data, InternalUtils.skipBlank(data,start));
									state = ParserState.beforeImport;
									break;
								case beforeImport :
									throw CompilerErrors.ERR_DUPLICATE_PACKAGE_DIRECTIVE.syntaxError(lineNo);
								default :
									throw CompilerErrors.ERR_PACKAGE_DIRECTIVE_INSIDE_CLASS.syntaxError(lineNo);
							}
							break;
						case DIR_IMPORT:
							checkLabel(lineNo, id, false);
							switch (state) {
								case beforePackage :
								case beforeImport :
									processImportDir(lineNo, data, InternalUtils.skipBlank(data,start));
									break;
								default :
									throw CompilerErrors.ERR_IMPORT_DIRECTIVE_INSIDE_CLASS.syntaxError(lineNo);
							}
							break;
						case DIR_INCLUDE:
							checkLabel(lineNo, id, false);
							processIncludeDir(lineNo, data, InternalUtils.skipBlank(data,start),end);
							break;
						case DIR_TRY	:
							switch (state) {
								case insideClassBody : case insideBegin : case insideBeginCode :
									pushTryBlock();
									break;
								default :
									throw CompilerErrors.ERR_DIRECTIVE_OUTSIDE_METHOD_BODY.syntaxError(lineNo, new String(data,startDir-1,endDir-startDir+1));
							}
							break;
						case DIR_CATCH	:
							switch (state) {
								case insideClassBody : case insideBegin : case insideBeginCode :
									processCatch(lineNo, data, InternalUtils.skipBlank(data, start), end);
									break;
								default :
									throw CompilerErrors.ERR_DIRECTIVE_OUTSIDE_METHOD_BODY.syntaxError(lineNo, new String(data,startDir-1,endDir-startDir+1));
							}
							break;
						case DIR_END_TRY	:
							switch (state) {
								case insideClassBody : case insideBegin : case insideBeginCode :
									popTryBlock(lineNo);
									break;
								default :
									throw CompilerErrors.ERR_DIRECTIVE_OUTSIDE_METHOD_BODY.syntaxError(lineNo, new String(data,startDir-1,endDir-startDir+1));
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
									throw CompilerErrors.ERR_MACROS_DIRECTIVE_AFTER_PACKAGE.syntaxError(lineNo);
							}
							break;
						case DIR_DEFAULT:
							switch (state) {
								case insideMethodLookup : 
								case insideMethodTable : 
									processJumps(lineNo, data, InternalUtils.skipBlank(data,start), end, false);
									break;
								default :
									throw CompilerErrors.ERR_DIRECTIVE_OUTSIDE_SWITCH.syntaxError(lineNo);
							}
							break;
						case DIR_VERSION	:
							checkLabel(lineNo, id, false);
							processVersionDir(lineNo, data, InternalUtils.skipBlank(data,start),end);
							break;
						case DIR_LINE	:
							checkLabel(lineNo, id, false);
							processLineDir(lineNo, data, InternalUtils.skipBlank(data,start),end);
							break;
						case DIR_VARTABLE	:
							checkLabel(lineNo, id, false);
							processVarTableDir(lineNo, data, InternalUtils.skipBlank(data,start),end);
							break;
						case DIR_FORWARD	:
							checkLabel(lineNo, id, true);
							processForwardDir(lineNo, id, data, InternalUtils.skipBlank(data,start),end);
							break;
						case DIR_SOURCE	:
							checkLabel(lineNo, id, false);
							processSourceDir(lineNo, data, InternalUtils.skipBlank(data,start),end);
							break;
						default :
							throw CompilerErrors.ERR_UNKNOWN_DIRECTIVE.syntaxError(lineNo, new String(data,startDir-1,endDir-startDir+1));
					}
					break;
				case '*' :
					switch (state) {
						case insideClassBody : case insideBegin : case insideBeginCode : 
							endDir = start = skipSimpleName(data,start+1);
			
							switch ((int)staticDirectiveTree.seekName(data,startDir,endDir)) {
								case CMD_LOAD	: processLoadAuto(data,InternalUtils.skipBlank(data,start)); break;
								case CMD_STORE	: processStoreAuto(data,InternalUtils.skipBlank(data,start)); break;
								case CMD_EVAL	: processEvalAuto(data,InternalUtils.skipBlank(data,start)); break;
								case CMD_CALL	: processCallAuto(data,InternalUtils.skipBlank(data,start)); break;
								default : throw CompilerErrors.ERR_UNKNOWN_DIRECTIVE.syntaxError(lineNo, new String(data,startDir-1,endDir-startDir+1));
							}
							break;
						default :
							throw CompilerErrors.ERR_AUTOMATION_OUTSIDE_METHOD_BODY.syntaxError(lineNo);
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
																			try{
																				LineParser.this.processLine(displacement, lineNo, data, from, length);
																			} catch (Exception  exc) {
																				printDiagnostics("");
																				throw exc;
																			}
																		}
																	}  
																: this);
							final Reader	rdr = m.processCall(lineNo, data, possiblyMacroEnd, len - (possiblyMacroEnd - from) + 1)) {

							lbl.write(rdr);
							lbl.flush();
						} catch (SyntaxException exc) {
							printDiagnostics("");
							throw new SyntaxException(lineNo, 0, "Error in ["+new String(m.getName())+"] macros: "+exc.getLocalizedMessage(), exc); 
						} catch (IOException exc) {
							printDiagnostics("");
							exc.printStackTrace();
						} catch (Exception exc) {
							throw exc;
						} finally {
							nestingDepth--;
						}
						
						return;
					}
					
					switch (state) {
						case insideClassBody : case insideBegin : case insideBeginCode : 
							endDir = start = skipSimpleName(data,start);
							
							if (startDir == endDir) {
								throw CompilerErrors.ERR_COMMAND_MNEMONICS_MISSING.syntaxError(lineNo);
							}
							final long	opCode = staticCommandTree.seekName(data,startDir,endDir);
		
							if (opCode >= 0) {
								final CommandDescriptor	desc = staticCommandTree.getCargo(opCode);
								
								if (addLines2Class && !addLines2ClassManually) {
									methodDescriptor.addLineNoRecord(methodLineNo++);
								}
								start = InternalUtils.skipBlank(data,start);

								if (desc.checkedTypes != null && desc.checkedTypes.length > 0) {
									if (!methodDescriptor.getBody().getStackAndVarRepoNew().compareStack(desc.checkedTypes)) {
										throw new SyntaxException(lineNo,0,"Illegal data types at the top of stack to use this command.");
									}
								}								
						
								if (state == ParserState.insideBegin) {
									if (beginLevel > 0) {
										prepareStackMapRecord(0);
									}
									state = ParserState.insideBeginCode;
								}
								
								switch (desc.commandFormat) {
									case single					: processSingleCommand(lineNo, desc, data, start); break; 
									case byteIndex				: processByteIndexCommand(lineNo, desc, data, start,false); break;
									case extendableByteIndex	: processByteIndexCommand(lineNo, desc, data, start, true); break;
									case byteValue				: processByteValueCommand(lineNo, desc, data, start,false); break;
									case shortValue				: processShortValueCommand(lineNo, desc, data, start,false); break;
									case byteType				: processByteTypeCommand(lineNo, desc, data, start); break;
									case byteIndexAndByteValue	: processByteIndexAndByteValueCommand(lineNo, desc, data, start, true); break;
									case shortIndexAndByteValue	: processShortIndexAndByteValueCommand(lineNo, desc, data, start); break;
									case classShortIndex		: processClassShortIndexCommand(lineNo, desc, data, start); break;
									case valueByteIndex			: processValueByteIndexCommand(lineNo, desc, data, start); break;
									case valueShortIndex		: processValueShortIndexCommand(lineNo, desc, data, start); break;
									case valueShortIndex2		: processValueShortIndex2Command(lineNo, desc, data, start); break;
									case shortBrunch			: processShortBrunchCommand(lineNo, desc, data, start, end); break;
									case longBrunch				: processLongBrunchCommand(lineNo, desc, data, start, end); break;
									case shortGlobalIndex		: processShortGlobalIndexCommand(lineNo, desc, data, start, end); break;
									case call					: processCallCommand(lineNo, desc, data, start, end); break;
									case callDynamic			: processDynamicCallCommand(lineNo, desc, data, start, end); break;
									case callInterface			: processInterfaceCallCommand(lineNo, desc, data, start, end); break;
									case lookupSwitch			:
										prepareSwitchCommand((byte)opCode,desc.stackChanges);
										state = ParserState.insideMethodLookup; 
										break;
									case tableSwitch			: 
										prepareSwitchCommand((byte)opCode,desc.stackChanges);
										state = ParserState.insideMethodTable; 
										break;
									case restricted				: 
										throw CompilerErrors.ERR_RESTRICTED_COMMAND.syntaxError(lineNo);
									default : 
										throw new UnsupportedOperationException("Command format ["+desc.commandFormat+"] is not supported yet");
								}
							}
							else {
								throw CompilerErrors.ERR_UNKNOWN_COMMAND_MNEMONICS.syntaxError(lineNo, new String(data,startDir,endDir-startDir));
							}
							break;
						case insideMethodLookup : 
						case insideMethodTable : 
							processJumps(lineNo, data, startDir, end, true);
							break;
						default :
							if (data[start] > ' ') {
								endDir = skipSimpleName(data,start);

								if (endDir == start) {
									throw CompilerErrors.ERR_UNPARSED_LINE_IN_CONTEXT.syntaxError(lineNo, new String(data,from,len-1), state);
								}
								else {
									if (staticCommandTree.seekName(data,start,endDir) >= 0) {
										throw CompilerErrors.ERR_COMMAND_OUTSIDE_METHOD_BODY.syntaxError(lineNo);
									}
									else {
										throw CompilerErrors.ERR_UNPARSED_LINE_IN_CONTEXT.syntaxError(lineNo, new String(data,from,len-1), state);
									}
								}
							}
					}
			}
		} catch (SyntaxException exc) {
			exc.printStackTrace();
			final SyntaxException	synt = new SyntaxException(lineNo, start-from, new String(data,from,len)+exc.getMessage(), exc);
			throw new IOException(synt.getLocalizedMessage(),synt);
		} catch (ContentException exc) {
			exc.printStackTrace();
			final SyntaxException	synt = new SyntaxException(lineNo, start-from, new String(data,from,len)+exc.getMessage(), exc);
			throw new IOException(synt.getLocalizedMessage(),synt);
		}
	}

	private void checkLabel(final int lineNo, final long labelId, final boolean present) throws ContentException {
		if (present) {
			if (labelId == -1) {
				throw CompilerErrors.ERR_MANDATORY_NAME_IS_MISSING.syntaxError(lineNo);
			}
		}
		else {
			if (labelId != -1) {
				throw CompilerErrors.ERR_NAME_IS_NOT_SUPPORTED_HERE.syntaxError(lineNo);
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
	
	private void putLabel(final int lineNo, final long labelId) throws ContentException, IOException {
		if (!isLabelExists(labelId)) {
			prepareStackMapRecord(labelId);
			methodDescriptor.getBody().putLabel(labelId, methodDescriptor.getBody().getStackAndVarRepoNew().makeStackSnapshot());
			if (needStackMapRecord) {
				needStackMapRecord = false;
			}
		}
		else {
			throw CompilerErrors.ERR_DUPLICATE_LABEL_IN_METHOD_BODY.syntaxError(lineNo);
		}
	}

	private int getPC() throws ContentException, IOException {
		return methodDescriptor.getBody().getPC();
	}

	private void putCommandShort(final byte command, final short argument) throws ContentException, IOException {
		methodDescriptor.getBody().putCommand(1, command, (byte)(argument >> 8), (byte)(argument & 0xFF));
	}

	private void putCommandInt(final byte command, final int argument) throws ContentException, IOException {
		methodDescriptor.getBody().putCommand(1, command, (byte)(argument >> 24), (byte)(argument >> 16), (byte)(argument >> 8), (byte)(argument & 0xFF));
	}
	
	private void putCommand(final byte... command) throws ContentException, IOException {
		methodDescriptor.getBody().putCommand(1, command);
	}

	private void alignPC() throws ContentException, IOException {
		methodDescriptor.getBody().alignPC();
	}

	private void registerBranch(final long labelId, final boolean shortBrunch) throws ContentException, IOException {
		methodDescriptor.getBody().registerBrunch(labelId, shortBrunch, methodDescriptor.getBody().getStackAndVarRepoNew().makeStackSnapshot());
	}

	private void registerBranch(final int address, final int placement, final long labelId, final boolean shortBrunch) throws ContentException, IOException {
		methodDescriptor.getBody().registerBrunch(address, placement, labelId, shortBrunch, methodDescriptor.getBody().getStackAndVarRepoNew().makeStackSnapshot());
	}
	
	private void changeStack(final StackChanges change) throws ContentException, IOException {
		changeStackRef(change, (short)0);
	}

	private void changeStackRef(final StackChanges change, final short refType) throws ContentException, IOException {
		methodDescriptor.getBody().getStackAndVarRepoNew().processChanges(methodDescriptor.getBody().getPC(), change, refType);
	}
	
	private void changeStack(final StackChanges change, final int signature) throws ContentException, IOException {
		changeStackRef(change, signature, (short)0);
	}

	private void changeStackRef(final StackChanges change, final int signature, final short refType) throws ContentException, IOException {
		methodDescriptor.getBody().getStackAndVarRepoNew().processChanges(methodDescriptor.getBody().getPC(), change, signature, refType);
	}
	
	private void changeStack(final StackChanges change, final TypeDescriptor[] signature, final int signatureSize, final TypeDescriptor retSignature) throws ContentException, IOException {
		methodDescriptor.getBody().getStackAndVarRepoNew().processChanges(methodDescriptor.getBody().getPC(), change, signature, signatureSize, retSignature);
	}

	private int getVarType(final int varDispl) throws ContentException, IOException {
		return methodDescriptor.getBody().getStackAndVarRepoNew().getVarType(varDispl).dataType;
	}
	
	private void prepareStackMapRecord(final long labelId) throws ContentException {
		methodDescriptor.addStackMapRecord(labelId);
	}
	
	/*
	 * Process directives
	 */
	private void processClassDir(final int lineNo, final long id, final char[] data, int start) throws IOException, ContentException {
		int				startOption = start, endOption;
		long			extendsId = -1;
		List<Long>		implementsNames = null;
		
		while (data[start] != '\n') {
			endOption = start = skipSimpleName(data,start);
			switch ((int)staticDirectiveTree.seekName(data, startOption, endOption)) {
				case OPTION_PUBLIC		: classFlags = addAndCheckDuplicates(lineNo, classFlags, JavaByteCodeConstants.ACC_PUBLIC, "class"); break;
				case OPTION_FINAL		: classFlags = addAndCheckDuplicates(lineNo, classFlags, JavaByteCodeConstants.ACC_FINAL, "class"); break;
				case OPTION_ABSTRACT	: classFlags = addAndCheckDuplicates(lineNo, classFlags, JavaByteCodeConstants.ACC_ABSTRACT, "class"); break;
				case OPTION_SYNTHETIC	: classFlags = addAndCheckDuplicates(lineNo, classFlags, JavaByteCodeConstants.ACC_SYNTHETIC, "class"); break;
				case OPTION_EXTENDS		:
					if (extendsId != -1) {
						throw CompilerErrors.ERR_DUPLICATE_EXTENDS_OPTION.syntaxError(lineNo);
					}
					else {
						int				startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedName(data,start);
						
						if (cdr.hasClassDescription(data,startName,endName)) {
							final Class<?>	parent = cdr.getClassDescription(data, startName, endName);
	
							if ((parent.getModifiers() & JavaByteCodeConstants.ACC_FINAL) != 0 || (parent.getModifiers() & JavaByteCodeConstants.ACC_PRIVATE) != 0) {
								throw CompilerErrors.ERR_EXTENDS_FINAL_OR_PRIVATE_CLASS.syntaxError(lineNo);
							}
							else {
								extendsId = tree.placeOrChangeName((CharSequence)parent.getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
							}
						}
						else {
							throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
						}
					}
					break;
				case OPTION_IMPLEMENTS	:
					if (implementsNames != null) {
						throw CompilerErrors.ERR_DUPLICATE_IMPLEMENTS_OPTION.syntaxError(lineNo);
					}
					else {
						implementsNames = new ArrayList<>();
						
						do {start++;
							int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedName(data,start);

							if (cdr.hasClassDescription(data,startName,endName)) {
								final Class<?>	member = cdr.getClassDescription(data,startName,endName);

								if ((member.getModifiers() & JavaByteCodeConstants.ACC_INTERFACE) == 0) {
									throw CompilerErrors.ERR_CLASS_INSTEAD_OF_INTERFACE.syntaxError(lineNo, new String(data,startName,endName-startName));
								}
								else {
									implementsNames.add(tree.placeOrChangeName((CharSequence)member.getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)));
								}
								start = InternalUtils.skipBlank(data,start);
							}
							else {
								throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
							}
						} while(data[start] == ',');
					}
					break;
				default :
					throw CompilerErrors.ERR_UNSUPPORTED_CLASS_OPTION.syntaxError(lineNo, new String(data,startOption,endOption));
			}
			startOption = start = InternalUtils.skipBlank(data,start);
		}
		
		if  (checkMutualPrivateProtectedPublic(classFlags)) {
			throw CompilerErrors.ERR_MUTUALLY_EXCLUDED_OPTIONS.syntaxError(lineNo, "public/protected/private");
		}
		else if (checkMutualAbstractFinal(classFlags)) {
			throw CompilerErrors.ERR_MUTUALLY_EXCLUDED_OPTIONS.syntaxError(lineNo, "abstract/final");
		}
		else {
			classNameId = id;
			joinedClassNameId = cc.setClassName(classFlags, packageId, id);
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

	private void processInterfaceDir(final int lineNo, final long id, final char[] data, int start) throws IOException, ContentException {
		int				startOption = start, endOption;
		List<Long>		implementsNames = null;

		classFlags |= JavaByteCodeConstants.ACC_INTERFACE | JavaByteCodeConstants.ACC_ABSTRACT;
		while (data[start] != '\n') {
			endOption = start = skipSimpleName(data,start);
			switch ((int)staticDirectiveTree.seekName(data,startOption,endOption)) {
				case OPTION_PUBLIC		: classFlags = addAndCheckDuplicates(lineNo, classFlags, JavaByteCodeConstants.ACC_PUBLIC, "class"); break;
				case OPTION_SYNTHETIC	: classFlags = addAndCheckDuplicates(lineNo, classFlags, JavaByteCodeConstants.ACC_SYNTHETIC, "class"); break;
				case OPTION_EXTENDS	:
					if (implementsNames != null) {
						throw CompilerErrors.ERR_DUPLICATE_EXTENDS_OPTION.syntaxError(lineNo);
					}
					else {
						implementsNames = new ArrayList<>();
						
						do {start++;
							int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedName(data,start);

							if (cdr.hasClassDescription(data,startName,endName)) {
								final Class<?>	parent = cdr.getClassDescription(data,startName,endName);

								if ((parent.getModifiers() & JavaByteCodeConstants.ACC_INTERFACE) == 0) {
									throw CompilerErrors.ERR_CLASS_INSTEAD_OF_INTERFACE.syntaxError(lineNo, new String(data,startName,endName-startName));
								}
								else {
									implementsNames.add(tree.placeOrChangeName((CharSequence)parent.getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)));
								}
								start = InternalUtils.skipBlank(data,start);
							}
							else {
								throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
							}
						} while(data[start] == ',');
					}
					break;
				default :
					throw CompilerErrors.ERR_UNSUPPORTED_CLASS_OPTION.syntaxError(lineNo, new String(data,startOption,endOption));
			}
			startOption = start = InternalUtils.skipBlank(data,start);
		}
		classNameId = id;
		classFlags |= JavaByteCodeConstants.ACC_PUBLIC;
		cc.setClassName(classFlags, packageId, id);
		if (implementsNames != null) {
			for (Long item : implementsNames) {
				cc.addInterfaceName(item);
			}
		}
	}
	
	private void processClassFieldDir(final int lineNo, final long id, final char[] data, int start) throws IOException, ContentException {
		final int			startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		
		if (cdr.hasClassDescription(data,startName,endName)) {
			final Class<?>	type = cdr.getClassDescription(data,startName,endName);
			final int		checkType = CompilerUtils.defineClassType(type);
			
			if (type == void.class) {
				throw CompilerErrors.ERR_VOID_NOT_APPLICABLE.syntaxError(lineNo);
			}
			else {
				final long	typeId = tree.placeOrChangeName((CharSequence)InternalUtils.buildFieldSignature(tree,tree.placeOrChangeName((CharSequence)type.getName(),new NameDescriptor(checkType))),new NameDescriptor(checkType));
				final long	classType = tree.placeOrChangeName((CharSequence)CompilerUtils.buildClassNameSignature(type.getCanonicalName()),new NameDescriptor(checkType));
				
				start = processOptions(lineNo, data, InternalUtils.skipBlank(data,start), forEntity, "field", cdr, false, OPTION_PUBLIC, OPTION_PROTECTED, OPTION_PRIVATE, OPTION_STATIC, OPTION_FINAL, OPTION_VOLATILE, OPTION_TRANSIENT, OPTION_SYNTHETIC);
				start = InternalUtils.skipBlank(data,start);
				
				if (data[start] == '=') {	// Initial values. 
					if ((forEntity.options & OPTION_STATIC) == 0) {
						throw CompilerErrors.ERR_INITIAL_VALUES_FOR_NON_STATIC_FIELD.syntaxError(lineNo);
					}
					else if (!type.isPrimitive() && type != String.class) {
						throw CompilerErrors.ERR_INITIAL_VALUES_FOR_ILLEGAL_FIELD_TYPE.syntaxError(lineNo);
					}
					else {
						final short		valueId;
						
						start = InternalUtils.skipBlank(data,start+1);
						switch (CompilerUtils.defineClassType(type)) {
							case CompilerUtils.CLASSTYPE_REFERENCE	:
								if (data[start] != '\"') {
									throw CompilerErrors.ERR_ILLEGAL_INITIAL_VALUE.syntaxError(lineNo, "java.lang.String");
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
									throw CompilerErrors.ERR_ILLEGAL_INITIAL_VALUE.syntaxError(lineNo, "boolean");
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
				cc.getConstantPool().asFieldRefDescription(joinedClassNameId, id, typeId);
				tree.getCargo(id).nameType = checkType;
				tree.getCargo(id).nameTypeId = classType;

				final int		classNameLen = tree.getNameLength(joinedClassNameId), fieldLen = tree.getNameLength(id);
				final char[]	forLongName = new char[classNameLen+1+fieldLen];
				
				tree.getName(joinedClassNameId,forLongName,0);
				forLongName[classNameLen] = '.';
				tree.getName(id,forLongName,classNameLen+1);
				tree.placeName(forLongName,0,forLongName.length,new NameDescriptor(checkType));
			}
		}
		else {
			throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
		}
	}

	private void processInterfaceFieldDir(final int lineNo, final long id, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		
		if (cdr.hasClassDescription(data,startName,endName)) {
			final Class<?>	type = cdr.getClassDescription(data,startName,endName);
			final int		checkType = CompilerUtils.defineClassType(type);
			
			if (type == void.class) {
				throw CompilerErrors.ERR_VOID_NOT_APPLICABLE.syntaxError(lineNo);
			}
			else {
				final long	typeId = tree.placeOrChangeName((CharSequence)type.getName().replace('.', '.'),new NameDescriptor(checkType));
				final long	classType = tree.placeOrChangeName((CharSequence)CompilerUtils.buildClassNameSignature(type.getCanonicalName()),new NameDescriptor(checkType));
				
				start = processOptions(lineNo, data, InternalUtils.skipBlank(data,start), forEntity, "field", cdr, false, OPTION_PUBLIC, OPTION_STATIC, OPTION_FINAL);
				start = InternalUtils.skipBlank(data,start);
				
				if (data[start] == '=') {	// Initial values.
					if (!type.isPrimitive() && type != String.class) {
						throw CompilerErrors.ERR_INITIAL_VALUES_FOR_ILLEGAL_FIELD_TYPE.syntaxError(lineNo);
					}
					else {
						final short		valueId;
						
						start = InternalUtils.skipBlank(data,start+1);
						switch (CompilerUtils.defineClassType(type)) {
							case CompilerUtils.CLASSTYPE_REFERENCE	:
								if (data[start] != '\"') {
									throw CompilerErrors.ERR_ILLEGAL_INITIAL_VALUE.syntaxError(lineNo, "java.lang.String");
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
									throw CompilerErrors.ERR_ILLEGAL_INITIAL_VALUE.syntaxError(lineNo, "boolean");
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
				tree.getCargo(id).nameTypeId = classType;
			}
		}
		else {
			throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
		}
	}
	
	private void processClassMethodDir(final int lineNo, final long id, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		
		if (startName == endName) {
			throw CompilerErrors.ERR_RETURN_TYPE_IS_MISSING.syntaxError(lineNo);
		}
		else if (!cdr.hasClassDescription(data,startName,endName)) {
			throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
		}
		else {
			final Class<?>	type = cdr.getClassDescription(data,startName,endName);
			final int		retType = CompilerUtils.defineClassType(type);
			final long		typeId = tree.placeOrChangeName((CharSequence)type.getName(),new NameDescriptor(retType));
			
			start = processOptions(lineNo, data, InternalUtils.skipBlank(data,start), forEntity, "method", cdr, false, OPTION_PUBLIC, OPTION_PROTECTED, OPTION_PRIVATE, OPTION_STATIC, OPTION_FINAL, OPTION_SYNCHRONIZED, OPTION_BRIDGE, OPTION_VARARGS, OPTION_NATIVE, OPTION_ABSTRACT, OPTION_SYNTHETIC, OPTION_THROWS, OPTION_BOOTSTRAP);
			if ((classFlags & JavaByteCodeConstants.ACC_ABSTRACT) == 0 && (forEntity.options & JavaByteCodeConstants.ACC_ABSTRACT) != 0) {
				throw CompilerErrors.ERR_ABSTRACT_METHOD_INSIDE_NON_ABSTRACT_CLASS.syntaxError(lineNo);
			}
			else {
				if (id == classNameId) {	// This is a constructor!
					if (typeId != voidId) {
						throw CompilerErrors.ERR_RETURN_TYPE_MUST_BE_VOID.syntaxError(lineNo);
					}
					else if ((forEntity.options & JavaByteCodeConstants.ACC_STATIC) != 0) {	// This is a <clinit>
						if (classConstructorCount == 0) {
							methodNameId = classConstructorId;
							classConstructorCount++;
						}
						else {
							throw CompilerErrors.ERR_DUPLICATE_CLINIT_METHOD.syntaxError(lineNo);
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
					methodDescriptor = cc.addMethodDescription(forEntity.options, forEntity.specialFlags, methodNameId, typeId, throwsList);
				}
				else {
					methodDescriptor = cc.addMethodDescription(forEntity.options, forEntity.specialFlags, methodNameId, typeId);
				}
			}
		}
	}
	
	private void processInterfaceMethodDir(final int lineNo, final long id, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		
		if (cdr.hasClassDescription(data,startName,endName)) {
			final Class<?>	type = cdr.getClassDescription(data,startName,endName);
			final int		retType = CompilerUtils.defineClassType(type);
			final long		typeId = tree.placeOrChangeName((CharSequence)type.getName(),new NameDescriptor(retType));
			
			start = processOptions(lineNo, data, InternalUtils.skipBlank(data,start), forEntity, "method", cdr, false, OPTION_PUBLIC, OPTION_STATIC, OPTION_FINAL, OPTION_SYNCHRONIZED, OPTION_BRIDGE, OPTION_VARARGS, OPTION_NATIVE, OPTION_ABSTRACT, OPTION_SYNTHETIC, OPTION_THROWS);
			
			forEntity.options |= JavaByteCodeConstants.ACC_ABSTRACT | JavaByteCodeConstants.ACC_PUBLIC; 
			if (forEntity.throwsList.size() > 0) {
				final long[] 	throwsList = new long[forEntity.throwsList.size()];
				
				for (int index = 0; index < throwsList.length; index++) {
					throwsList[index] = tree.placeOrChangeName((CharSequence)forEntity.throwsList.get(index).getName(),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
				}
				methodDescriptor = cc.addMethodDescription(forEntity.options, forEntity.specialFlags, id, typeId, throwsList);
			}
			else {
				methodDescriptor = cc.addMethodDescription(forEntity.options, forEntity.specialFlags, id, typeId);
			}
			methodNameId = id;
		}
		else {
			throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
		}
	}

	private void processParameterDir(final int lineNo, final long id, final char[] data, int start) throws ContentException, IOException {
		start = extractClassWithPossibleArray(lineNo, data, start, cdr, forClass);

		if (methodNameId == classConstructorId) {
			throw CompilerErrors.ERR_PARAMETERS_INSIDE_CLINIT.syntaxError(lineNo);
		}
		else if (forClass[0] == void.class) {
			throw CompilerErrors.ERR_VOID_NOT_APPLICABLE.syntaxError(lineNo);
		}
		else {
			final long	typeId = tree.placeOrChangeName((CharSequence)toCanonicalName(forClass[0]),new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
			
			start = processOptions(lineNo, data, InternalUtils.skipBlank(data, start), forEntity, "parameter", cdr, false, OPTION_FINAL, OPTION_SYNTHETIC);
			methodDescriptor.addParameterDeclaration(forEntity.options, id, typeId);
		}
	}

	private void processVarDir(final int lineNo, final long id, final char[] data, int start) throws IOException, ContentException {
		start = extractClassWithPossibleArray(lineNo, data, start, cdr, forClass);
		
		if (forClass[0] == void.class) {
			throw CompilerErrors.ERR_VOID_NOT_APPLICABLE.syntaxError(lineNo);
		}
		else {
			final long	typeId = tree.placeOrChangeName((CharSequence)toCanonicalName(forClass[0]),new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		
			start = processOptions(lineNo, data, InternalUtils.skipBlank(data,start), forEntity, "var", cdr, false, OPTION_FINAL, OPTION_SYNTHETIC);
			methodDescriptor.addVarDeclaration(forEntity.options,id,typeId);
		}
	}

	private void processStackDir(final int lineNo, final char[] data, int start) throws ContentException {
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
						throw CompilerErrors.ERR_ILLEGAL_STACK_DIRECTIVE_PARAMETER.syntaxError(lineNo);
					}
				} catch (NumberFormatException exc) {
					throw CompilerErrors.ERR_ILLEGAL_STACK_DIRECTIVE_PARAMETER.syntaxError(lineNo);
				}
				break;
		}
		skip2line(lineNo, data, start);
		if (methodDescriptor.isBootstrap()) {
			if (!methodDescriptor.isStatic()) {
				throw CompilerErrors.ERR_BOOTSTRAP_METHOD_MUST_BE_STATIC.syntaxError(lineNo);
			}
			else {
				final long[] 	plist = methodDescriptor.getParametersList();
				
				if (plist.length >= 3) {
					if (!(plist[0] == methodHandlesLookupId && plist[1] == stringId && plist[2] == methodTypeId)) {
						throw CompilerErrors.ERR_BOOTSTRAP_METHOD_ILLEGAL_PARAMETERS.syntaxError(lineNo);
					}
					else if (methodDescriptor.getReturnedType() != callSiteId) {
						throw CompilerErrors.ERR_BOOTSTRAP_METHOD_ILLEGAL_RETURNED_TYPE.syntaxError(lineNo);
					}
				}
				else {
					throw CompilerErrors.ERR_BOOTSTRAP_METHOD_TOO_FEW_PARAMETERS.syntaxError(lineNo);
				}
			}
		}
	}
	
	private void processPackageDir(final int lineNo, final char[] data, int start) throws ContentException {
		final int	endPackage = skipQualifiedName(data,start);
			
		if (start == endPackage) {
			throw CompilerErrors.ERR_PACKAGE_NAME_MISSING.syntaxError(lineNo);
		}
		else {
			packageId = tree.placeOrChangeName(data,start,endPackage,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
			skip2line(lineNo, data, endPackage);
		}
	}

	private void processImportDir(final int lineNo, final char[] data, int start) throws ContentException {
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
			skip2line(lineNo, data, start);
		} catch (ClassNotFoundException e) {
			throw CompilerErrors.ERR_CLASS_NOT_FOUND.syntaxError(lineNo, className);
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

	private void processIncludeDir(final int lineNo, final char[] data, int start, final int end) throws ContentException {
		if (start < end && data[start] == '\"') {
			int	startQuoted = start + 1;
			
			start = skipQuoted(lineNo, data, startQuoted, '\"');
			skip2line(lineNo, data, start+1);
			
			final String	ref = new String(data,startQuoted,start-startQuoted-1);
			URL		refUrl;
			
			try{refUrl = new URL(ref);
			} catch (MalformedURLException exc) {
				refUrl = this.getClass().getResource(ref);
			}
			if (refUrl == null) {
				throw CompilerErrors.ERR_INVALID_RESOURCE_URL.syntaxError(lineNo, ref);
			}
			else {
				int innerLineNo = 0;
				
				try(final LineByLineProcessor	lbl = new LineByLineProcessor(this);
					final InputStream		is = refUrl.openStream();
					final Reader			rdr = new InputStreamReader(is)) {

					lbl.write(rdr);
				}
				catch (IOException | SyntaxException exc) {
					throw CompilerErrors.ERR_RESOURCE_URL_ERROR.syntaxError(lineNo, refUrl, innerLineNo, exc.getLocalizedMessage());
				}
			}
		}
		else {
			throw CompilerErrors.ERR_RESOURCE_URL_IS_MISSING.syntaxError(lineNo);
		}
	}

	private void processVersionDir(final int lineNo, final char[] data, int start, final int end) throws ContentException {
		int		parm[] = intArray, from = start, major = 0, minor = 0;
		
		if (start < end && data[start] >= '0' && data[start] <= '9') {
			start = UnsafedCharUtils.uncheckedParseInt(data, from, parm, true);
			major = parm[0];
			if (data[start] == '.') {
				UnsafedCharUtils.uncheckedParseInt(data, from = start + 1, parm, true);
				minor = parm[0];
			}
			if (major == 1) {
				switch (minor) {
					case 1 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_0_2, JavaByteCodeConstants.MINOR_1_0_2);
						break;
					case 2 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_2, JavaByteCodeConstants.MINOR_1_2);
						break;
					case 3 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_3, JavaByteCodeConstants.MINOR_1_3);
						break;
					case 4 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_4, JavaByteCodeConstants.MINOR_1_4);
						break;
					case 5 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_5, JavaByteCodeConstants.MINOR_1_5);
						break;
					case 6 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_6, JavaByteCodeConstants.MINOR_1_6);
						break;
					case 7 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_7, JavaByteCodeConstants.MINOR_1_7);
						break;
					case 8 	:	// backward compatibility...
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_8, JavaByteCodeConstants.MINOR_8);
						break;
					default :
						throw CompilerErrors.ERR_UNSUPPORTED_VERSION_NUMBER.syntaxError(lineNo, major, minor);
				}
			}
			else {
				switch (major) {
					case 8 	:
						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_8, JavaByteCodeConstants.MINOR_8);
						break;
					case 9 	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_9, JavaByteCodeConstants.MINOR_9);
//						break;						
					case 10	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_10, JavaByteCodeConstants.MINOR_10);
//						break;						
					case 11	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_11, JavaByteCodeConstants.MINOR_11);
//						break;						
					case 12	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_12, JavaByteCodeConstants.MINOR_12);
//						break;						
					case 13	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_13, JavaByteCodeConstants.MINOR_13);
//						break;						
					case 14	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_14, JavaByteCodeConstants.MINOR_14);
//						break;						
					case 15	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_15, JavaByteCodeConstants.MINOR_15);
//						break;						
					case 16	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_16, JavaByteCodeConstants.MINOR_16);
//						break;						
					case 17	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_17, JavaByteCodeConstants.MINOR_17);
//						break;						
					case 18	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_18, JavaByteCodeConstants.MINOR_18);
//						break;						
					case 19	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_19, JavaByteCodeConstants.MINOR_19);
//						break;						
					case 20	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_20, JavaByteCodeConstants.MINOR_20);
//						break;						
					case 21	:
//						cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_21, JavaByteCodeConstants.MINOR_21);
//						break;						
					default :
						throw CompilerErrors.ERR_UNSUPPORTED_VERSION_NUMBER.syntaxError(lineNo, major, 0);
				}
			}
			if (major == 1 && minor == 7) {
				cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_1_7, JavaByteCodeConstants.MINOR_1_7);
			}
			else if (major == 1 && minor == 8) {
				cc.changeClassFormatVersion(JavaByteCodeConstants.MAJOR_8, JavaByteCodeConstants.MINOR_8);
			}
			else {
				throw CompilerErrors.ERR_UNSUPPORTED_VERSION_NUMBER.syntaxError(lineNo, major, minor);
			}
		}
		else {
			throw CompilerErrors.ERR_VERSION_NUMBER_IS_MISSING.syntaxError(lineNo);
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
					throw CompilerErrors.ERR_LINES_DIRECTIVE_OUTSIDE_METHOD_BODY.syntaxError(lineNo);
				}
			}
			else {
				throw CompilerErrors.ERR_LINES_DIRECTIVE_WITHOUT_MANUAL.syntaxError(lineNo);
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
					throw CompilerErrors.ERR_LINES_DIRECTIVE_UNKNOWN_OPTION.syntaxError(lineNo);
			}
		}
		else {
			throw CompilerErrors.ERR_LINES_DIRECTIVE_OPTION_IS_MISSING.syntaxError(lineNo);
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
		
		start = skipSignature(lineNo, data, start);
		final long	signatureId = tree.placeOrChangeName(data, from, start, new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
		
		cc.addForward(labelId, signatureId);
	}
	
	private void processSourceDir(final int lineNo, final char[] data, int start, final int end) throws ContentException {
		int		from = start;
		
		if (data[start] == '\"') {
			start = skipQuoted(lineNo, data, start+1, '\"');
			skip2line(lineNo, data, start+1);
			
			final String	ref = new String(data,from+1,start-from-1);
			final URI		refUrl = URI.create(ref);
			
			if (refUrl == null) {
				throw CompilerErrors.ERR_INVALID_RESOURCE_URL.syntaxError(lineNo, ref);
			}
			else {
				cc.setSourceAttribute(refUrl);
			}
		}
		else {
			throw CompilerErrors.ERR_RESOURCE_URL_IS_MISSING.syntaxError(lineNo);
		}
	}
	
	/*
	 * Process try blocks
	 */
	
	private boolean areTryBlocksClosed() {
		return tryList.size() == 0;
	}

	private void pushTryBlock() throws IOException, ContentException {
		tryList.add(0, new long[]{getPC(), -1, methodDescriptor.getBody().getStackAndVarRepoNew().getCurrentStackDepth(), methodDescriptor.getBody().getStackAndVarRepoNew().getCurrentStackDepth()});
	}

	private void processCatch(final int lineNo, final char[] data, int from, final int to) throws IOException, ContentException {
		if (tryList.size() == 0) {
			throw CompilerErrors.ERR_CATCH_DIRECTIVE_OUTSIDE_TYR_BLOCK.syntaxError(lineNo);
		}
		else {
			final short	currentPC = (short)getPC();
			
			if (tryList.get(0)[TRY_END_PC] == -1) {
				tryList.get(0)[TRY_END_PC] = currentPC;
			}
			if (Character.isJavaIdentifierStart(data[from])) {
				from--;
				do {final int		startException = from + 1, endException = skipQualifiedName(data, startException);
				
					if (cdr.hasClassDescription(data, startException, endException)) {
						final Class<?>	exception = cdr.getClassDescription(data, startException, endException);
						final long		exceptionId = tree.placeOrChangeName((CharSequence)exception.getName().replace('.', '/'), new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
						final short		catchClassId = cc.getConstantPool().asClassDescription(exceptionId);

						methodDescriptor.addExceptionRecord((short)tryList.get(0)[TRY_START_PC], (short)tryList.get(0)[TRY_END_PC], catchClassId, (short)getPC());
						methodDescriptor.getBody().getStackAndVarRepoNew().prepareCatch(currentPC, (int)tryList.get(0)[TRY_STACK_DEPTH], (int)tryList.get(0)[TRY_VARFRAME_LENGTH], catchClassId);
						from = InternalUtils.skipBlank(data,endException);
					}
					else {
						throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startException,endException-startException));
					}
				} while (data[from] == ',');
			}
			else {
				methodDescriptor.addExceptionRecord((short)tryList.get(0)[TRY_START_PC], (short)tryList.get(0)[TRY_END_PC], (short)0, (short)getPC());
				methodDescriptor.getBody().getStackAndVarRepoNew().prepareCatch(currentPC, (int)tryList.get(0)[TRY_STACK_DEPTH], (int)tryList.get(0)[TRY_VARFRAME_LENGTH], (short)0);
			}
			markLabelRequired(false);
			prepareStackMapRecord(0);
			skip2line(lineNo, data, from);
		}
	}

	private void popTryBlock(final int lineNo) throws ContentException {
		if (tryList.size() == 0) {
			throw CompilerErrors.ERR_UNPAIRED_ENDTRY_DIRECTIVE.syntaxError(lineNo);
		}
		else {
			tryList.remove(0);
		}
	}
	
	/*
	 * Process commands
	 */
	
	private void processSingleCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final byte	op = (byte)desc.operation; 
		
		putCommand(op);
		if (desc.refTypeSource != RefTypeSource.none) {
			final int 				type = methodDescriptor.getBody().getStackAndVarRepoNew().getCurrentStackDepth() >= 2 ? methodDescriptor.getBody().getStackAndVarRepoNew().selectStackItemType(-1) : 0; 
			final int 				reference = methodDescriptor.getBody().getStackAndVarRepoNew().getCurrentStackDepth() >= 2 ? methodDescriptor.getBody().getStackAndVarRepoNew().selectStackItemRefType(-1) : 0; 
			final TypeDescriptor 	typeDesc = calculateRefType(desc, type, reference);
			
			changeStackRef(desc.stackChanges, typeDesc.dataType, typeDesc.reference);
		}
		else {
			changeStack(desc.stackChanges);
		}
		if (desc.uncondBrunch) {
			markLabelRequired(true);
		}
		skip2line(lineNo, data, start);
	}

	private void processByteIndexCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final boolean expandAddress) throws IOException, ContentException {
		final long	forResult[] = longArray;
		
		start = calculateLocalAddress(data, start, forResult);
		if (forResult[0] < 0 || forResult[0] > methodDescriptor.getLocalFrameSize()) {
			throw CompilerErrors.ERR_CALCULATED_ADDRESS_OUTSIZE_FRAME.syntaxError(lineNo, forResult[0], methodDescriptor.getLocalFrameSize());
		}
		else if (forResult[0] >= 256) {
			if (!expandAddress) {
				throw CompilerErrors.ERR_CALCULATED_ADDRESS_TOO_LONG.syntaxError(lineNo, forResult[0]);
			}
			else {
				putCommand(WIDE_OPCODE, desc.operation, (byte)(forResult[0] >> 8), (byte)forResult[0]);
			}
		}
		else {
			if (forResult[0] <= 3) {	// Optimization to use short forms of the byte code command
				switch (desc.operation) {
					case 0x19	: putCommand((byte)extractStaticCommand(ALOAD_SPECIAL[(int)forResult[0]]).operation); break;		// aload
					case 0x3a	: putCommand((byte)extractStaticCommand(ASTORE_SPECIAL[(int)forResult[0]]).operation); break;		// astore	
					case 0x18	: putCommand((byte)extractStaticCommand(DLOAD_SPECIAL[(int)forResult[0]]).operation); break;		// dload
					case 0x39	: putCommand((byte)extractStaticCommand(DSTORE_SPECIAL[(int)forResult[0]]).operation); break;		// dstore
					case 0x17	: putCommand((byte)extractStaticCommand(FLOAD_SPECIAL[(int)forResult[0]]).operation); break;		// fload	
					case 0x38	: putCommand((byte)extractStaticCommand(FSTORE_SPECIAL[(int)forResult[0]]).operation); break;		// fstore
					case 0x15	: putCommand((byte)extractStaticCommand(ILOAD_SPECIAL[(int)forResult[0]]).operation); break;		// iload
					case 0x36	: putCommand((byte)extractStaticCommand(ISTORE_SPECIAL[(int)forResult[0]]).operation); break;		// istore
					case 0x16	: putCommand((byte)extractStaticCommand(LLOAD_SPECIAL[(int)forResult[0]]).operation); break;		// lload
					case 0x37	: putCommand((byte)extractStaticCommand(LSTORE_SPECIAL[(int)forResult[0]]).operation); break;		// lstore
					default : throw new UnsupportedOperationException("Special command ["+staticCommandTree.getName(desc.operation)+"] is not supprted yet");
				}
			}
			else {
				putCommand((byte)desc.operation, (byte)forResult[0]);
			}
			if (desc.argumentType != DONT_CHECK_LOCAL_TYPES && getVarType((int)forResult[0]) != desc.argumentType) { 
				throw CompilerErrors.ERR_INCOMPATIBLE_DATATYPE_FOR_COMMAND.syntaxError(lineNo, forResult[0]);
			}
		}
		final TypeDescriptor 		type = calculateRefType(desc, (int)forResult[0]);
		final StackAndVarRepoNew	svr = methodDescriptor.getBody().getStackAndVarRepoNew();
		final TypeDescriptor 		stackType = svr.getCurrentStackDepth() > 0 ? svr.makeStackSnapshot().content[svr.getCurrentStackDepth()-1] : null;
		
		if (desc.refTypeSource != RefTypeSource.none) {
			if (type.dataType == CompilerUtils.CLASSTYPE_REFERENCE) {
				changeStackRef(desc.stackChanges, type.dataType, (short)type.reference);
			}
			else {
				changeStack(desc.stackChanges, type.dataType);
			}
		}
		else {
			changeStack(desc.stackChanges);
		}
		if (desc.stackChanges == StackChanges.pop || desc.stackChanges == StackChanges.pop2) {
			try {
				switch (desc.operation) {
					case 0x3a	: // astore
						markAssigned(lineNo, (int)forResult[0], stackType);
						break;
					case 0x39	: // dstore
						markAssigned(lineNo, (int)forResult[0], (TypeDescriptor)DOUBLE_DESC.clone());
						break;
					case 0x38	: // fstore
						markAssigned(lineNo, (int)forResult[0], (TypeDescriptor)FLOAT_DESC.clone());
						break;
					case 0x36	: // istore
						markAssigned(lineNo, (int)forResult[0], (TypeDescriptor)INT_DESC.clone());
						break;
					case 0x37	: // lstore
						markAssigned(lineNo, (int)forResult[0], (TypeDescriptor)LONG_DESC.clone());
						break;
					case 0x4b	: // astore_0
						markAssigned(lineNo, 0, stackType);
						break;
					case 0x47	: // dstore_0
						markAssigned(lineNo, 0, (TypeDescriptor)DOUBLE_DESC.clone());
						break;
					case 0x43	: // fstore_0
						markAssigned(lineNo, 0, (TypeDescriptor)FLOAT_DESC.clone());
						break;
					case 0x3b	: // istore_0
						markAssigned(lineNo, 0, (TypeDescriptor)INT_DESC.clone());
						break;
					case 0x3f	: // lstore_0
						markAssigned(lineNo, 0, (TypeDescriptor)LONG_DESC.clone());
						break;
					case 0x4c	: // astore_1
						markAssigned(lineNo, 1, stackType);
						break;
					case 0x48	: // dstore_1
						markAssigned(lineNo, 1, (TypeDescriptor)DOUBLE_DESC.clone());
						break;
					case 0x44	: // fstore_1
						markAssigned(lineNo, 1, (TypeDescriptor)FLOAT_DESC.clone());
						break;
					case 0x3c	: // istore_1
						markAssigned(lineNo, 1, (TypeDescriptor)INT_DESC.clone());
						break;
					case 0x40	: // lstore_1
						markAssigned(lineNo, 1, (TypeDescriptor)LONG_DESC.clone());
						break;
					case 0x4d	: // astore_2
						markAssigned(lineNo, 2, stackType);
						break;
					case 0x49	: // dstore_2
						markAssigned(lineNo, 2, (TypeDescriptor)DOUBLE_DESC.clone());
						break;
					case 0x45	: // fstore_2
						markAssigned(lineNo, 2, (TypeDescriptor)FLOAT_DESC.clone());
						break;
					case 0x3d	: // istore_2
						markAssigned(lineNo, 2, (TypeDescriptor)INT_DESC.clone());
						break;
					case 0x41	: // lstore_2
						markAssigned(lineNo, 2, (TypeDescriptor)LONG_DESC.clone());
						break;
					case 0x4e	: // astore_3
						markAssigned(lineNo, 3, stackType);
						break;
					case 0x4a	: // dstore_3
						markAssigned(lineNo, 3, (TypeDescriptor)DOUBLE_DESC.clone());
						break;
					case 0x46	: // fstore_3
						markAssigned(lineNo, 3, (TypeDescriptor)FLOAT_DESC.clone());
						break;
					case 0x3e	: // istore_3
						markAssigned(lineNo, 3, (TypeDescriptor)INT_DESC.clone());
						break;
					case 0x42	: // lstore_3
						markAssigned(lineNo, 3, (TypeDescriptor)LONG_DESC.clone());
						break;
					default : throw new UnsupportedOperationException("Special command ["+staticCommandTree.getName(desc.operation)+"] is not supprted yet");
				}
			} catch (CloneNotSupportedException e) {
				throw new ContentException(e);
			}
		}
		skip2line(lineNo, data, start);
	}

	private void markAssigned(final int lineNo, final int varIndex, final TypeDescriptor type) throws ContentException {
		final StackAndVarRepoNew	svr = methodDescriptor.getBody().getStackAndVarRepoNew();
		final TypeDescriptor		declared = svr.getVarType(varIndex);
		
		if (svr.typesAreCompatible(type.dataType, declared.dataType) && 
				(type.dataType == StackAndVarRepoNew.SPECIAL_TYPE_NULL && declared.dataType == CompilerUtils.CLASSTYPE_REFERENCE 
				 || svr.typeRefsAreCompatible(type.reference, declared.reference)
				)) {
			declared.unassigned = false;
		}
		else {
			svr.typesAreCompatible(type.dataType, declared.dataType);
			svr.typeRefsAreCompatible(type.reference, declared.reference);
			throw CompilerErrors.ERR_TYPE_MUST_BE_COMPATIBLE.syntaxError(lineNo);
		}
	}

	private void processByteValueCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final boolean expandValue) throws IOException, ContentException {
		final long	forResult[] = longArray;
		
		start = calculateValue(lineNo, data, start, EvalState.additional, forResult);
		if (forResult[0] < Byte.MIN_VALUE || forResult[0] >= 256) {
			if (!expandValue) {
				throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, forResult[0]);
			}
			else {
				putCommandShort(desc.operation, (short)forResult[0]);
			}
		}
		else {
			if (forResult[0] >= -1 && forResult[0] <= 5) {	// Replace bipush with the special commands
				putCommand(extractStaticCommand(BIPUSH_SPECIAL[(int) (forResult[0]+1)]).operation);
			}
			else {
				putCommand(desc.operation,(byte)forResult[0]);
			}
		}
		if (desc.refTypeSource != RefTypeSource.none) {
			changeStack(desc.stackChanges, calculateRefType(desc).reference);
		}
		else {
			changeStack(desc.stackChanges);
		}
		skip2line(lineNo, data,start);
	}

	private void processByteIndexAndByteValueCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final boolean expandAddress) throws IOException, ContentException {
		final long[]	forIndex = longArray, forValue = longArray2;
		boolean			needExpand = false;
		
		start = InternalUtils.skipBlank(data, calculateLocalAddress(data, start, forIndex));
		if (data[start] == ',') {
			start = calculateValue(lineNo, data, InternalUtils.skipBlank(data,start + 1), EvalState.additional, forValue);
		}
		else {
			throw CompilerErrors.ERR_SECOND_PARAMETER_IS_MISSING.syntaxError(lineNo);
		}

		if (forIndex[0] > methodDescriptor.getLocalFrameSize()) {
			throw CompilerErrors.ERR_CALCULATED_ADDRESS_OUTSIZE_FRAME.syntaxError(lineNo, forIndex[0], methodDescriptor.getLocalFrameSize());
		}
		else if (forIndex[0] >= 256) {
			if (!expandAddress) {
				throw CompilerErrors.ERR_CALCULATED_ADDRESS_TOO_LONG.syntaxError(lineNo, forIndex[0]);
			}
			else {
				needExpand = true;
			}
		}
		if (forValue[0] < Byte.MIN_VALUE || forValue[0] >= 256) {
			if (!expandAddress) {
				throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, forValue[0]);
			}
			else {
				needExpand = true;
			}
		}
		if (needExpand) {
			putCommand(WIDE_OPCODE, desc.operation, (byte)(forIndex[0]>>8),(byte)forIndex[0],(byte)(forValue[0]>>8),(byte)forValue[0]);
		}
		else {
			putCommand(desc.operation, (byte)forIndex[0], (byte)forValue[0]);
		}
		if (desc.refTypeSource != RefTypeSource.none) {
			changeStack(desc.stackChanges, calculateRefType(desc).reference);
		}
		else {
			changeStack(desc.stackChanges);
		}
		skip2line(lineNo, data, start);
	}
	
	private void processByteTypeCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final int	startType = start, endType = start = skipSimpleName(data,start);
		final long	typeId = staticDirectiveTree.seekName(data,startType,endType);

		if (typeId >= T_BASE && typeId <= T_BASE_END) {
			final String		dataType;
			
			putCommand(desc.operation, (byte)(typeId-T_BASE));
			switch ((int)typeId) {
				case T_BOOLEAN	: 
					dataType = "[Z"; 
					break;
				case T_CHAR		: 
					dataType = "[C"; 
					break;
				case T_FLOAT	: 
					dataType = "[F"; 
					break;
				case T_DOUBLE	: 
					dataType = "[D"; 
					break;
				case T_BYTE		: 
					dataType = "[B"; 
					break;
				case T_SHORT	: 
					dataType = "[S"; 
					break;
				case T_INT		: 
					dataType = "[I"; 
					break;
				case T_LONG		: 
					dataType = "[J"; 
					break;
				default : throw new UnsupportedOperationException();
			}
			final long	classNameId = tree.placeOrChangeName((CharSequence)dataType, VOID_DESCRIPTOR);
			final short	classDispl = cc.getConstantPool().asClassDescription(classNameId); 
			
			changeStackRef(desc.stackChanges, CompilerUtils.CLASSTYPE_REFERENCE, classDispl);
			skip2line(lineNo, data, start);
		}
		else {
			throw CompilerErrors.ERR_UNKNOWN_PRIMITIVE_TYPE.syntaxError(lineNo, new String(data,startType,endType-startType));
		}
	}

	private void processClassShortIndexCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		
		if (startName == endName) {
			throw CompilerErrors.ERR_CLASS_NAME_IS_MISSING.syntaxError(lineNo);
		}
		else {
			short		typeDispl;
			
			if (cdr.hasClassDescription(data,startName,endName)) {
				final Class<?>	type = cdr.getClassDescription(data,startName,endName);
				final int		checkType = CompilerUtils.defineClassType(type);
				final long		typeId = tree.placeOrChangeName((CharSequence)type.getName().replace('.','/'),new NameDescriptor(checkType));
				
				typeDispl = cc.getConstantPool().asClassDescription(typeId);
				
				putCommandShort((byte)desc.operation, typeDispl);
				if (desc.refTypeSource != RefTypeSource.none) {
					if ((desc.operation & 0xFF) == 0xbd) {
						final Class<?>	arrType = Array.newInstance(type, 0).getClass();
						final int		checkArrType = CompilerUtils.defineClassType(arrType);
						final long		typeArrId = tree.placeOrChangeName((CharSequence)CompilerUtils.buildClassNameSignature(arrType.getCanonicalName()),new NameDescriptor(checkArrType));
						final short		typeArrDispl = cc.getConstantPool().asClassDescription(typeArrId);
						
						changeStackRef(desc.stackChanges, typeArrDispl);
					}
					else {
						changeStackRef(desc.stackChanges, typeDispl);
					}
				}
				else {
					changeStack(desc.stackChanges);
				}
				skip2line(lineNo, data, start);
			}
			else {
				final long		typeId = cc.getNameTree().seekName(data,startName,endName);
				
				if (typeId >= 0) {
					final NameDescriptor	nd = cc.getNameTree().getCargo(typeId);
					
					if (nd != null) {
						typeDispl = nd.cpIds[JavaByteCodeConstants.CONSTANT_Class];
					
						if (typeDispl != Short.MIN_VALUE) {
							putCommandShort(desc.operation, typeDispl);
							skip2line(lineNo, data, start);
						}
						else {
							throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
						}
					}
					else {
						throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
					}
				}
				else {
					throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
				}
				changeStack(desc.stackChanges, typeDispl);
			}
		}
	}

	private void processValueByteIndexCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final int	fromContent = start; 
		short		displ[] = shortArray;
		
		start = processValueShortIndexCommand(lineNo, data, start, displ);

		if (displ[0] < 0 || displ[0] > 2 * Byte.MAX_VALUE) {
			if (desc.operation == LDC_OPCODE) {	// ldc can be replaced with ldc_w 
				processValueShortIndexCommand(lineNo, staticCommandTree.getCargo(LDC_W_OPCODE), data, fromContent);
			}
			else {
				throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, displ[0]);
			}
		}
		else {
			putCommand(desc.operation, (byte)(displ[0] & 0xFF));
			if (desc.refTypeSource != RefTypeSource.none) {
				final TypeDescriptor 	dataType = calculateRefType(desc, displ[0]);
				
				if (dataType.dataType == CompilerUtils.CLASSTYPE_REFERENCE) {
					changeStackRef(dataTypeToStackChange(displ[1]), dataType.dataType, dataType.reference);
				}
				else {
					changeStack(dataTypeToStackChange(displ[1]), dataType.dataType);
				}
			}
			else {
				changeStack(dataTypeToStackChange(displ[1]));
			}
			skip2line(lineNo, data, start);
		}
	}
	
	private void processValueShortIndexCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		short		displ[] = shortArray;
		
		start = processValueShortIndexCommand(lineNo, data, start, displ);

		if (displ[0] < 0 || displ[0] > 2 * Short.MAX_VALUE) {
			throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, displ[0]);
		}
		else {
			putCommandShort((byte)desc.operation, displ[0]);
			if (desc.refTypeSource != RefTypeSource.none) {
				final TypeDescriptor	type = calculateRefType(desc, displ[0]);
				
				if (type.dataType == CompilerUtils.CLASSTYPE_REFERENCE) {
					changeStackRef(dataTypeToStackChange(displ[1]), type.dataType, type.reference);
				}
				else {
					changeStack(dataTypeToStackChange(displ[1]), type.dataType);
				}
			}
			else {
				changeStack(dataTypeToStackChange(displ[1]));
			}
			skip2line(lineNo, data, start);
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

	private int processValueShortIndexCommand(final int lineNo, final char[] data, int start, final short[] result) throws IOException, ContentException {
		try{switch (data[start]) {
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' : case '-' :
					final long	forResult[] = longArray2;
					final int	sign;
					
					if (data[start] == '-') {
						sign = -1;
						start++;
					}
					else if (data[start] == '+') {
						sign = 1;
						start++;
					}
					else {
						sign = 1;
					}
					start = UnsafedCharUtils.uncheckedParseNumber(data, start, forResult, CharUtils.PREF_INT|CharUtils.PREF_FLOAT, true);
					
					if (forResult[1] == CharUtils.PREF_FLOAT) {
						result[0] = cc.getConstantPool().asFloatDescription(sign * Float.intBitsToFloat((int) forResult[0]));
						result[1] = CompilerUtils.CLASSTYPE_FLOAT;
					}
					else if (forResult[1] == CharUtils.PREF_INT) {
						result[0] = cc.getConstantPool().asIntegerDescription((int)(sign * forResult[0]));
						result[1] = CompilerUtils.CLASSTYPE_INT;
					}
					else {
						throw CompilerErrors.ERR_ILLEGAL_NUMERIC_CONSTANT_SIZE.syntaxError(lineNo);
					}
					break;
				case '\'' :
					final int	startChar = start + 1, endChar = start = skipQuoted(lineNo, data, startChar, '\'');
					
					if (endChar != startChar + 1) {
						if (data[startChar] == '\\') {
							char	value = 0;
							
							for (int index = startChar+1; index < endChar; index++) {
								if (data[index] >= '0' && data[index] <= '7') {
									value = (char) (value * 8 + data[index] - '0');
								}
								else {
									throw CompilerErrors.ERR_ILLEGAL_ESCAPED_CHAR_CONTENT.syntaxError(lineNo);
								}
							}
							result[0] = cc.getConstantPool().asIntegerDescription(value);
							result[1] = CompilerUtils.CLASSTYPE_INT;
						}
						else {
							throw CompilerErrors.ERR_ILLEGAL_CHAR_CONSTANT_LENGTH.syntaxError(lineNo);
						}
					}
					else {
						result[0] = cc.getConstantPool().asIntegerDescription(data[startChar]);
						result[1] = CompilerUtils.CLASSTYPE_INT;
					}
					start++;
					break;
				case '\"' :
					final int	startString = start + 1, endString = start = skipQuoted(lineNo, data, startString, '\"');
					
					start++;
					result[0] = cc.getConstantPool().asStringDescription(cc.getNameTree().placeOrChangeName(data,startString,endString,new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)));
					result[1] = CompilerUtils.CLASSTYPE_REFERENCE;
					break;
				case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' : case 'g' : case 'h' :  case 'i' : case 'j' :
				case 'k' : case 'l' : case 'm' : case 'n' : case 'o' : case 'p' : case 'q' : case 'r' :  case 's' : case 't' :
				case 'u' : case 'v' : case 'w' : case 'x' : case 'y' : case 'z' :
				case 'A' : case 'B' : case 'C' : case 'D' : case 'E' : case 'F' : case 'G' : case 'H' :  case 'I' : case 'J' :
				case 'K' : case 'L' : case 'M' : case 'N' : case 'O' : case 'P' : case 'Q' : case 'R' :  case 'S' : case 'T' :
				case 'U' : case 'V' : case 'W' : case 'X' : case 'Y' : case 'Z' :
					final int	startName = start, endName = start = skipQualifiedName(data,startName);
					
					if (UnsafedCharUtils.uncheckedCompare(data, endName-CLASS_SUFFIX.length, CLASS_SUFFIX, 0, CLASS_SUFFIX.length)) {
						for (int index = startName; index <= endName; index++) {
							if (data[index] == '.') {
								data[index] = '/';
							}
						}
						result[0] = cc.getConstantPool().asClassDescription(cc.getNameTree().placeOrChangeName(data,startName,endName-CLASS_SUFFIX.length,new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)));
						result[1] = CompilerUtils.CLASSTYPE_REFERENCE;
					}
					else {
						throw CompilerErrors.ERR_ILLEGAL_NAME.syntaxError(lineNo);
					}
					break;
				default :
					throw CompilerErrors.ERR_ILLEGAL_LEXEMA.syntaxError(lineNo);
			}
		} catch (NumberFormatException exc) {
			throw CompilerErrors.ERR_ILLEGAL_NUMBER.syntaxError(lineNo, exc.getLocalizedMessage());
		}
		return start;
	}
	
	private void processValueShortIndex2Command(final int lineNo, final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final long		forResult[] = longArray;
		StackChanges	changes = null;
		final long		sign;
		short			displ;

		try{if (data[start] == '-') {
				sign = -1;
				start++;
			}
			else if (data[start] == '+') {
				sign = 1;
				start++;
			}
			else {
				sign = 1;
			}
			start = UnsafedCharUtils.uncheckedParseNumber(data, start, forResult, CharUtils.PREF_LONG|CharUtils.PREF_DOUBLE,true);
			if (forResult[1] == CharUtils.PREF_DOUBLE) {
				displ = cc.getConstantPool().asDoubleDescription(sign * Double.longBitsToDouble(forResult[0]));
				changes = StackChanges.pushDouble;
			}
			else if (forResult[1] == CharUtils.PREF_LONG) {
				displ = cc.getConstantPool().asLongDescription(sign * forResult[0]);
				changes = StackChanges.pushLong;
			}
			else {
				throw CompilerErrors.ERR_ILLEGAL_LONG_NUMERIC_CONSTANT_SIZE.syntaxError(lineNo);
			}
			if (displ < 0 || displ > 2 * Short.MAX_VALUE) {
				throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, displ);
			}
			else {
				putCommandShort((byte)desc.operation, displ);
				if (desc.refTypeSource != RefTypeSource.none) {
					changeStack(changes, calculateRefType(desc, displ).reference);
				}
				else {
					changeStack(changes);
				}
				skip2line(lineNo, data, start);
			}
		} catch (NumberFormatException exc) {
			throw CompilerErrors.ERR_ILLEGAL_NUMBER.syntaxError(lineNo, exc.getLocalizedMessage());
		}
	}
	
	private void processShortValueCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final boolean expandValue) throws IOException, ContentException {
		final long	forResult[] = longArray;
		
		start = calculateValue(lineNo, data, start, EvalState.additional, forResult);
		if (forResult[0] < Short.MIN_VALUE || forResult[0] >= 65536) {
			if (!expandValue) {
				throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, forResult[0]);
			}
			else {
				putCommandInt(desc.operation, (int)forResult[0]);
			}
		}
		else {
			if (forResult[0] >= -1 && forResult[0] <= 5) {	// Replace sipush with the special commands
				putCommand(extractStaticCommand(BIPUSH_SPECIAL[(int)(forResult[0] + 1)]).operation);
			}
			else {
				putCommandShort(desc.operation, (short)forResult[0]);
			}
		}
		if (desc.refTypeSource != RefTypeSource.none) {
			changeStack(desc.stackChanges, calculateRefType(desc).reference);
		}
		else {
			changeStack(desc.stackChanges);
		}
		skip2line(lineNo, data, start);
	}

	private void processShortIndexAndByteValueCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start) throws IOException, ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedNameWithArray(data,start);
		
		if (cdr.hasClassDescription(data,startName,endName)) {
			final Class<?>	type = cdr.getClassDescription(data,startName,endName);
			final int		checkType = CompilerUtils.defineClassType(type);
			final long		typeId = tree.placeOrChangeName((CharSequence)type.getName().replace('.','/'),new NameDescriptor(checkType));
			final short		typeDispl = cc.getConstantPool().asClassDescription(typeId);
			final long[]	forValue = longArray;
			
			start = InternalUtils.skipBlank(data,endName);
			if (data[start] == ',') {
				start = calculateValue(lineNo, data, InternalUtils.skipBlank(data,start+1), EvalState.additional, forValue);
		 		if (forValue[0] <= 0 || forValue[0] >= 2 * Byte.MAX_VALUE) {
					throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, forValue[0]);
				}
				else {
					putCommand(desc.operation, (byte)((typeDispl >> 8) & 0xFF), (byte)(typeDispl & 0xFF), (byte)forValue[0]);
					if (desc.operation == MULTIANEWARRAY_OPCODE) { // multianewarray
						changeStackRef(desc.stackChanges, CompilerUtils.CLASSTYPE_REFERENCE, (short)forValue[0]);
					}
					else {
						changeStack(desc.stackChanges);
					}
					skip2line(lineNo, data, start);
				}
			}
			else {
				throw CompilerErrors.ERR_SECOND_PARAMETER_IS_MISSING.syntaxError(lineNo);
			}
		}
		else {
			throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
		}
	}

	private void processShortGlobalIndexCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final FieldAccessDescriptor	fad = new FieldAccessDescriptor();
		
		start = calculateFieldAddressAndSignature(data,start,end,fad);
		if (fad.fieldReference <= 0 || fad.fieldReference > Short.MAX_VALUE) {
			throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, fad.fieldReference);
		}
		else {
			putCommandShort((byte)desc.operation, fad.fieldReference);
			changeStackRef(desc.stackChanges, fad.valueType.dataType,fad.valueType.reference);
			skip2line(lineNo, data, start);
		}
	}

//	private void processShortGlobalIndexCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
//		final int	forResult[] = intArray; 
//		
//		start = calculateFieldAddressAndSignature(data,start,end,forResult);
//		if (forResult[0] <= 0 || forResult[0] > Short.MAX_VALUE) {
//			throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, forResult[0]);
//		}
//		else {
//			putCommandShort((byte)desc.operation, (short)forResult[0]);
//			changeStack(desc.stackChanges,forResult[1]);
//			skip2line(lineNo, data, start);
//		}
//	}
	
	private void processShortBrunchCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final long	forResult[] = longArray;
		final short	displ = methodDescriptor.getBody().getPC();
		
		start = calculateBranchAddress(data, start, forResult);
		
		changeStack(desc.stackChanges);
		putCommandShort((byte)desc.operation, (short)0);
		registerBranch(displ, displ + 1, forResult[0], true);
		if (!isLabelExists(forResult[0])) {	// forward brunch
			methodDescriptor.getBody().getStackAndVarRepoNew().markForwardBrunch(forResult[0]);
		}
		if (desc.uncondBrunch) {
			markLabelRequired(true);
		}
		skip2line(lineNo, data, start);
	}

	private void processLongBrunchCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final long	forResult[] = longArray;
		final short	displ = methodDescriptor.getBody().getPC();
		
		start = calculateBranchAddress(data, start, forResult);
		changeStack(desc.stackChanges);
		putCommandInt(desc.operation, 0);
		registerBranch(displ, displ + 1, forResult[0], false);
		if (!isLabelExists(forResult[0])) {	// forward brunch
			methodDescriptor.getBody().getStackAndVarRepoNew().markForwardBrunch(forResult[0]);
		}
		if (desc.uncondBrunch) {
			markLabelRequired(true);
		}
		skip2line(lineNo, data, start);
	}

	private int processCallCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final MethodCallDescriptor	mcd = new MethodCallDescriptor();
//		final int				forResult[] = intArray, forArgsAndSignatures[] = new int[2];
		
		start = calculateMethodAddressAndSignature(lineNo, data, start, end, mcd);
		if (mcd.methodReference <= 0 || mcd.methodReference > 2*Short.MAX_VALUE) {
			throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, mcd.methodReference);
		}
		else {
			putCommandShort((byte)desc.operation, mcd.methodReference);
			changeStack(desc.stackChanges, mcd.parametersTypes, mcd.parametersTypes.length, mcd.returnedValueType);
			skip2line(lineNo, data, start);
		}
		return mcd.argumentLength;
	}	

//	private int processCallCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
//		final int				forResult[] = intArray, forArgsAndSignatures[] = new int[2];
//		
//		start = calculateMethodAddressAndSignature(lineNo, data, start, end, forResult, forArgsAndSignatures);
//		if (forResult[0] <= 0 || forResult[0] > 2*Short.MAX_VALUE) {
//			throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, forResult[0]);
//		}
//		else {
//			putCommandShort((byte)desc.operation, (short)forResult[0]);
//			changeStack(desc.stackChanges, forMethodTypes, forResult[1], new TypeDescriptor(forArgsAndSignatures[1], (short)0));
//			skip2line(lineNo, data, start);
//		}
//		return forArgsAndSignatures[0];
//	}	
	
	private void processDynamicCallCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
//		start = InternalUtils.skipBlank(data, CharUtils.parseName(data, InternalUtils.skipBlank(data, start), intArray));
//		final long	bootstrapMethodNameId = cc.getNameTree().seekName(data,intArray[0],intArray[1]);  
//		final int	forResult[] = intArray, forArgsAndSignatures[] = new int[2];
//		
//		if (data[start] == ',') {
//			start = calculateMethodAddressAndSignature(lineNo, data, InternalUtils.skipBlank(data, start + 1), end, forResult, forArgsAndSignatures);
//			if (forResult[0] <= 0 || forResult[0] > 2*Short.MAX_VALUE) {
//				throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, forResult[0]);
//			}
//			else {
//			}
//		}
//		else {
//			
//		}
		throw new ContentException("Don't use invokedynamic connand! Use direct link to the methods you need instead"); 
	}

	private void processInterfaceCallCommand(final int lineNo, final CommandDescriptor desc, final char[] data, int start, final int end) throws IOException, ContentException {
		final int	argsSize = processCallCommand(lineNo, desc, data, start, end) + 1;
		
		putCommand((byte)(argsSize & 0xFF),(byte)((argsSize >> 8)& 0xFF));
	}

	private void prepareSwitchCommand(final byte opCode, final StackChanges stackChanges) throws ContentException, IOException {
		switchAddress = getPC(); 
		putCommand(opCode);
		changeStack(stackChanges);
		alignPC(); 
		jumps.clear(); 
	}	
	
	private void processJumps(final int lineNo, final char[] data, int start, final int end, final boolean explicitValue) throws ContentException {
		final long[]	forLabel = longArray;
		final long[]	forValue = new long[1];
		
		if (explicitValue) {
			start = calculateValue(lineNo, data, start, EvalState.additional, forValue);
			if (start < end && data[start] == ',') {
				start = InternalUtils.skipBlank(data, calculateBranchAddress(data,UnsafedCharUtils.uncheckedSkipBlank(data,start+1,true),forLabel));
			}
			else {
				throw CompilerErrors.ERR_SECOND_PARAMETER_IS_MISSING.syntaxError(lineNo);
			}
		}
		else {
			forValue[0] = DEFAULT_MARK;
			start = InternalUtils.skipBlank(data, calculateBranchAddress(data,start,forLabel));
		}
		if ((forValue[0] < Integer.MIN_VALUE  || forValue[0] > Integer.MAX_VALUE) && forValue[0] != DEFAULT_MARK) {
			throw CompilerErrors.ERR_CALCULATED_VALUE_TOO_LONG.syntaxError(lineNo, forValue[0]);
		}
		else {
			if (jumps.containsKey(forValue[0])) {
				throw CompilerErrors.ERR_DUPLICATE_SWITCH_VALUE.syntaxError(lineNo, forValue[0] == DEFAULT_MARK ? "default" : forValue[0]);
			}
			else {
				jumps.put(forValue[0],forLabel[0]);
			}
			skip2line(lineNo, data, start);
		}
	}

	private void fillTable(final int lineNo) throws IOException, ContentException {
		if (jumps.size() == 0) {
			throw CompilerErrors.ERR_MISSING_JUMPS_IN_SWITCH.syntaxError(lineNo);
		}
		else if (!jumps.containsKey(DEFAULT_MARK)) {
			throw CompilerErrors.ERR_MISSING_DEFAULT_JUMP_IN_SWITCH.syntaxError(lineNo);
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
				throw CompilerErrors.ERR_SPARSE_TABLE_IN_SWITCH.syntaxError(lineNo);
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

	private void fillLookup(final int lineNo)  throws IOException, ContentException {
		if (jumps.size() == 0) {
			throw CompilerErrors.ERR_MISSING_JUMPS_IN_SWITCH.syntaxError(lineNo);
		}
		else if (!jumps.containsKey(DEFAULT_MARK)) {
			throw CompilerErrors.ERR_MISSING_DEFAULT_JUMP_IN_SWITCH.syntaxError(lineNo);
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

	private TypeDescriptor calculateRefType(final CommandDescriptor desc, final int... parameters) throws ContentException, IOException {
		// TODO Auto-generated method stub
		switch (desc.refTypeSource) {
			case command		:
				switch (desc.operation) {
					case (byte)0xbd :	// anewarray
						return methodDescriptor.getBody().getStackAndVarRepoNew().getVarType(parameters[0]);
					case (byte)0xc0 :	// checkcast
						return methodDescriptor.getBody().getStackAndVarRepoNew().getVarType(0);
					case 0x12 :	// ldc
						if (cc.getConstantPool().getItemType((short)parameters[0]) == JavaByteCodeConstants.CONSTANT_Class) {
							final long	className = cc.getNameTree().placeOrChangeName((CharSequence)"java/lang/Class", new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
							final short	classId = cc.getConstantPool().asClassDescription(className);
							
							return new TypeDescriptor(CompilerUtils.CLASSTYPE_REFERENCE, classId);
						}
						else if (cc.getConstantPool().getItemType((short)parameters[0]) == JavaByteCodeConstants.CONSTANT_String) {
							final long	className = cc.getNameTree().placeOrChangeName((CharSequence)"java/lang/String", new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
							final short	classId = cc.getConstantPool().asClassDescription(className);
							
							return new TypeDescriptor(CompilerUtils.CLASSTYPE_REFERENCE, classId);
						}
						else {
							return new TypeDescriptor(CompilerUtils.CLASSTYPE_REFERENCE, (short)parameters[0]);
						}
					case 0x13 :	// ldc_w
						if (cc.getConstantPool().getItemType((short)parameters[0]) == JavaByteCodeConstants.CONSTANT_Class) {
							final long	className = cc.getNameTree().placeOrChangeName((CharSequence)"java/lang/Class", new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
							final short	classId = cc.getConstantPool().asClassDescription(className);
							
							return new TypeDescriptor(CompilerUtils.CLASSTYPE_REFERENCE, classId);
						}
						else if (cc.getConstantPool().getItemType((short)parameters[0]) == JavaByteCodeConstants.CONSTANT_String) {
							final long	className = cc.getNameTree().placeOrChangeName((CharSequence)"java/lang/String", new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE));
							final short	classId = cc.getConstantPool().asClassDescription(className);
							
							return new TypeDescriptor(CompilerUtils.CLASSTYPE_REFERENCE, classId);
						}
						else {
							return new TypeDescriptor(CompilerUtils.CLASSTYPE_REFERENCE, (short)parameters[0]);
						}
					case 0x14 :	// ldc2_w
						return new TypeDescriptor(CompilerUtils.CLASSTYPE_REFERENCE, (short)parameters[0]);
					case (byte)0xc5 :	// multianewarray
						return methodDescriptor.getBody().getStackAndVarRepoNew().getVarType(3);
					case (byte)0xbb :	// new
						return new TypeDescriptor(CompilerUtils.CLASSTYPE_REFERENCE, (short)parameters[0]);
					case (byte)0xbc :	// newarray
						return methodDescriptor.getBody().getStackAndVarRepoNew().getVarType(3);
					default :
						throw new UnsupportedOperationException("Ref type source ["+desc.refTypeSource+"] is not supported yet");
				}
			case staticField	:
				break;
			case instanceField	:
				break;
			case locaVariable	:
				switch (desc.operation) {
					case 0x19 :	// aload N
						return methodDescriptor.getBody().getStackAndVarRepoNew().getVarType(parameters[0]);
					case 0x2a :	// aload_0
						return methodDescriptor.getBody().getStackAndVarRepoNew().getVarType(0);
					case 0x2b :	// aload_1
						return methodDescriptor.getBody().getStackAndVarRepoNew().getVarType(1);
					case 0x2c :	// aload_2
						return methodDescriptor.getBody().getStackAndVarRepoNew().getVarType(2);
					case 0x2d :	// aload_3
						return methodDescriptor.getBody().getStackAndVarRepoNew().getVarType(3);
					default :
						throw new UnsupportedOperationException("Ref type source ["+desc.refTypeSource+"] is not supported yet");
				}
			case returnedValue	:
				break;
			case stack2			:
				final String	className = InternalUtils.displ2String(cc, (short)parameters[1]);
				
				if (className.length() > 2) {	// Referenced type
					final String	loadedClassName = className.charAt(1) == '[' ? className.substring(1) : InternalUtils.classSignature2ClassName(className.substring(1)).replace('.', '/');
					final long		classDescr = cc.getNameTree().placeOrChangeName((CharSequence)loadedClassName, VOID_DESCRIPTOR);
					final short		classDispl = cc.getConstantPool().asClassDescription(classDescr);
					
					return new TypeDescriptor(parameters[0], classDispl);
				}
				else {
					return new TypeDescriptor(className.isEmpty() ? InternalUtils.fieldSignature2Type(className.substring(1)) : CompilerUtils.CLASSTYPE_VOID);
				}
			case none			:
				return ZERO_REF_TYPE;
			default:
				throw new UnsupportedOperationException("Ref type source ["+desc.refTypeSource+"] is not supported yet");
		}
		return ZERO_REF_TYPE;
	}

	
	
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
	
//	private int calculateFieldAddressAndSignature(final char[] data, int start, final int end, final int[] result) throws IOException, ContentException {
//		int	startName = start, endName = start = skipSimpleName(data,start);
//		
//		if (data[start] != '.') {
//			final long	fieldId = tree.seekName(data,startName,endName);
//			
//			if ((result[0] = cc.getConstantPool().asFieldRefDescription(joinedClassNameId,fieldId)) != 0) {
//				result[1] = tree.getCargo(fieldId).nameType;
//				return start;
//			}
//		}
//		endName = start = skipQualifiedName(data,start);
//		final Field		f = cdr.getFieldDescription(data,startName,endName);
//		final int		checkType = CompilerUtils.defineClassType(f.getType());
//		final long		type = tree.placeOrChangeName((CharSequence)f.getType().getName().replace('.','/'),new NameDescriptor(checkType));
//		
//		result[0] = cc.getConstantPool().asFieldRefDescription(
//							tree.placeOrChangeName((CharSequence)f.getDeclaringClass().getName().replace('.','/'),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
//							tree.placeOrChangeName((CharSequence)f.getName(),new NameDescriptor(checkType)),
//							tree.placeOrChangeName((CharSequence)InternalUtils.buildFieldSignature(tree,type),new NameDescriptor(checkType))
//					);
//		result[1] = CompilerUtils.defineClassType(f.getType());
//		return start;
//	}

	private int calculateFieldAddressAndSignature(final char[] data, int start, final int end, final FieldAccessDescriptor desc) throws IOException, ContentException {
		int	startName = start, endName = start = skipSimpleName(data,start);
		
		if (data[start] != '.') {
			desc.fieldId = tree.seekName(data,startName,endName);
			
			if ((desc.fieldReference = cc.getConstantPool().asFieldRefDescription(joinedClassNameId, desc.fieldId)) != 0) {
				final NameDescriptor	nd = tree.getCargo(desc.fieldId); 
				final int 				classType = nd.nameType;
				
				if (classType == CompilerUtils.CLASSTYPE_REFERENCE) {
					final long	classTypeId = nd.nameTypeId;
					final short	displ = cc.getConstantPool().asClassDescription(classTypeId);
					
					desc.valueType = new TypeDescriptor(classType, displ);
				}
				else {
					desc.valueType = new TypeDescriptor(classType);
				}
				return start;
			}
		}
		endName = start = skipQualifiedName(data,start);
		final Field		f = cdr.getFieldDescription(data,startName,endName);
		final int		checkType = CompilerUtils.defineClassType(f.getType());
		final long		type = tree.placeOrChangeName((CharSequence)f.getType().getName().replace('.','/'),new NameDescriptor(checkType));
		final int 		classType = CompilerUtils.defineClassType(f.getType());
		
		desc.fieldReference = cc.getConstantPool().asFieldRefDescription(
							tree.placeOrChangeName((CharSequence)f.getDeclaringClass().getName().replace('.','/'),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
							tree.placeOrChangeName((CharSequence)f.getName(),new NameDescriptor(checkType)),
							tree.placeOrChangeName((CharSequence)InternalUtils.buildFieldSignature(tree, type), new NameDescriptor(checkType))
						);
		
		if (classType ==  CompilerUtils.CLASSTYPE_REFERENCE) {
			final long	classNameId = tree.placeOrChangeName((CharSequence)f.getType().getName().replace('.', '/'), new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)); 
			final short	displ = cc.getConstantPool().asClassDescription(classNameId);
			
			desc.valueType = new TypeDescriptor(classType, displ);
		}
		else {
			desc.valueType = new TypeDescriptor(classType);
		}
		return start;
	}
	
//	private int calculateMethodAddressAndSignature(final int lineNo, final char[] data, int start, final int end, final int[] result, final int[] argsLengthAndRetSignature) throws IOException, ContentException {
//		int			startName = start, endName = start = skipSimpleName(data, start);
//		
//		if (data[start] == '.') {
//			endName = start = skipQualifiedName(data, startName);
//			
//			if (data[start] == '(') {
//				final int			endSignature = start = skipSignature(lineNo, data, start);
//				final long			possibleId = cc.getNameTree().seekName(data, startName, endSignature);
//				
//				if (possibleId >= 0) {
//					final String	classAndMethod = new String(data,startName,endName-startName);
//					final String	classOnly = classAndMethod.substring(0,classAndMethod.lastIndexOf('.'));
//					final String	methodOnly = classAndMethod.substring(classAndMethod.lastIndexOf('.')+1);
//					final String	signature = new String(data, endName, endSignature-endName);
//					
//					result[0] = cc.getConstantPool().asMethodRefDescription(
//							tree.seekName((CharSequence)classOnly.replace('.','/')),
//							tree.seekName((CharSequence)(classOnly.endsWith(methodOnly) ? "<init>" : methodOnly)),
//							tree.seekName(data,endName,endSignature)
//					);
//					while ((result[1] = InternalUtils.methodSignature2Stack(signature, cc.getConstantPool(), forMethodTypes)) < 0) {
//						forMethodTypes = Arrays.copyOf(forMethodTypes,2*forMethodTypes.length);
//					}
//				}
//				else {
//					try{final Method	m = cdr.getMethodDescription(data,startName,endSignature);
//						final int		retType = CompilerUtils.defineClassType(m.getReturnType());
//					
//						if (m.getDeclaringClass().isInterface()) {
//							result[0] = cc.getConstantPool().asInterfaceMethodRefDescription(
//									tree.placeOrChangeName((CharSequence)toCanonicalName(m.getDeclaringClass()).replace('.','/'),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
//									tree.placeOrChangeName((CharSequence)m.getName(),new NameDescriptor(retType)),
//									tree.placeOrChangeName((CharSequence)CompilerUtils.buildMethodSignature(m),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))
//							);
//						}
//						else {
//							result[0] = cc.getConstantPool().asMethodRefDescription(
//									tree.placeOrChangeName((CharSequence)toCanonicalName(m.getDeclaringClass()).replace('.','/'),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
//									tree.placeOrChangeName((CharSequence)m.getName(),new NameDescriptor(retType)),
//									tree.placeOrChangeName((CharSequence)CompilerUtils.buildMethodSignature(m),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))
//							);
//						}
//						argsLengthAndRetSignature[0] = 0;
//						for (Parameter item : m.getParameters()) {
//							argsLengthAndRetSignature[0] += item.getType() == long.class || item.getType() == double.class ? 2 : 1;
//						}
//						argsLengthAndRetSignature[1] = InternalUtils.methodSignature2Type(m);
//						while ((result[1] = InternalUtils.methodSignature2Stack(m, cc.getConstantPool(), forMethodTypes)) < 0) {
//							forMethodTypes = Arrays.copyOf(forMethodTypes,2*forMethodTypes.length);
//						}
//					} catch (ContentException exc) {
//						final Constructor<?>	c = cdr.getConstructorDescription(data,startName,endSignature);
//						
//						result[0] = cc.getConstantPool().asMethodRefDescription(
//								tree.placeOrChangeName((CharSequence)c.getDeclaringClass().getName().replace('.','/'),new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
//								tree.placeOrChangeName((CharSequence)"<init>",new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)),
//								tree.placeOrChangeName((CharSequence)CompilerUtils.buildConstructorSignature(c), new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))
//						);
//						argsLengthAndRetSignature[0] = 0;
//						for (Parameter item : c.getParameters()) {
//							argsLengthAndRetSignature[0] += item.getType() == long.class || item.getType() == double.class ? 2 : 1;
//						}
//						argsLengthAndRetSignature[1] = CompilerUtils.CLASSTYPE_VOID;
//						while ((result[1] = InternalUtils.constructorSignature2Stack(c, cc.getConstantPool(), forMethodTypes)) < 0) {
//							forMethodTypes = Arrays.copyOf(forMethodTypes, 2*forMethodTypes.length);
//						}
//					}
//				}
//			}
//			else {
//				throw CompilerErrors.ERR_METHOD_SIGNATURE_IS_MISSING.syntaxError(lineNo);
//			}
//		}
//		else {
//			if (data[start] == '(') {
//				final int		startSignature = start, endSignature = start = skipSignature(lineNo, data, start);
//				final String	signature = new String(data,startSignature,endSignature-startSignature);
//				final int		checkType = InternalUtils.methodSignature2Type(signature);
//				
//				result[0] = cc.getConstantPool().asMethodRefDescription(
//						joinedClassNameId,
//						tree.placeOrChangeName(data,startName,endName,new NameDescriptor(checkType)),
//						tree.placeOrChangeName(data,startSignature,endSignature,new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))						
//				);
//				while ((result[1] = InternalUtils.methodSignature2Stack(signature, cc.getConstantPool(), forMethodTypes)) < 0) {
//					forMethodTypes = Arrays.copyOf(forMethodTypes,2*forMethodTypes.length);
//				}
//				argsLengthAndRetSignature[0] = result[1];
//				argsLengthAndRetSignature[1] = checkType;
//			}
//			else {
//				throw CompilerErrors.ERR_METHOD_SIGNATURE_IS_MISSING.syntaxError(lineNo);
//			}
//		}
//		return start;
//	}

	private int calculateMethodAddressAndSignature(final int lineNo, final char[] data, int start, final int end, final MethodCallDescriptor desc) throws IOException, ContentException {
		int			startName = start, endName = start = skipSimpleName(data,start);
		
		if (data[start] == '.') {
			endName = start = skipQualifiedName(data,startName);
			
			if (data[start] == '(') {
				final int			endSignature = start = skipSignature(lineNo, data, start);
				final long			possibleId = cc.getNameTree().seekName(data,startName,endSignature);
				
				if (possibleId >= 0) {
					final String	classAndMethod = new String(data, startName, endName - startName);
					final String	classOnly = classAndMethod.substring(0, classAndMethod.lastIndexOf('.'));
					final String	methodOnly = classAndMethod.substring(classAndMethod.lastIndexOf('.') + 1);
					final String	signature = new String(data, endName, endSignature - endName);
					final String	returned = signature.substring(signature.indexOf(')')+1);
					final int		returnedType = InternalUtils.fieldSignature2Type(returned);

					desc.methodReference = cc.getConstantPool().asMethodRefDescription(
							tree.seekName((CharSequence)classOnly.replace('.','/')),
							desc.methodId = tree.seekName((CharSequence)(classOnly.endsWith(methodOnly) ? "<init>" : methodOnly)),
							desc.signatureId = tree.seekName(data,endName,endSignature)
					);
					int argCount = 0;
					while ((argCount = InternalUtils.methodSignature2Stack(signature, cc.getConstantPool(), forMethodTypes)) < 0) {
						forMethodTypes = Arrays.copyOf(forMethodTypes,2*forMethodTypes.length);
					}
					desc.parametersTypes = argCount == 0 ? new TypeDescriptor[0] : Arrays.copyOf(forMethodTypes, argCount);
					desc.argumentLength = 0;
					for(TypeDescriptor item  : desc.parametersTypes) {
						desc.argumentLength += item.dataType == CompilerUtils.CLASSTYPE_DOUBLE || item.dataType == CompilerUtils.CLASSTYPE_LONG ? 2 : 1; 
					}
					if (returnedType == CompilerUtils.CLASSTYPE_REFERENCE) {
						final long	typeName = tree.placeOrChangeName((CharSequence)returned, VOID_DESCRIPTOR);
						final short	displ = cc.getConstantPool().asClassDescription(typeName);
						
						desc.returnedValueType = new TypeDescriptor(returnedType, displ);
					}
					else {
						desc.returnedValueType = new TypeDescriptor(returnedType);
					}
				}
				else {
					try{final Method	m = cdr.getMethodDescription(data,startName,endSignature);
						final int		returnedType = CompilerUtils.defineClassType(m.getReturnType());
					
						if (m.getDeclaringClass().isInterface()) {
							desc.methodReference = cc.getConstantPool().asInterfaceMethodRefDescription(
									tree.placeOrChangeName((CharSequence)toCanonicalName(m.getDeclaringClass()).replace('.','/'), new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
									desc.methodId = tree.placeOrChangeName((CharSequence)m.getName(), new NameDescriptor(returnedType)),
									desc.signatureId = tree.placeOrChangeName((CharSequence)CompilerUtils.buildMethodSignature(m), new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))
							);
						}
						else {
							desc.methodReference = cc.getConstantPool().asMethodRefDescription(
									tree.placeOrChangeName((CharSequence)toCanonicalName(m.getDeclaringClass()).replace('.','/'), new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
									desc.methodId = tree.placeOrChangeName((CharSequence)m.getName(), new NameDescriptor(returnedType)),
									desc.signatureId = tree.placeOrChangeName((CharSequence)CompilerUtils.buildMethodSignature(m), new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))
							);
						}
						desc.argumentLength = 0;
						for (Parameter item : m.getParameters()) {
							desc.argumentLength += item.getType() == long.class || item.getType() == double.class ? 2 : 1;
						}
						if (returnedType == CompilerUtils.CLASSTYPE_REFERENCE) {
							final String	retClass = m.getReturnType().getName().replace('.', '/');
							final long		typeName = tree.placeOrChangeName((CharSequence)retClass, VOID_DESCRIPTOR);
							final short		displ = cc.getConstantPool().asClassDescription(typeName);
							
							desc.returnedValueType = new TypeDescriptor(returnedType, displ);
						}
						else {
							desc.returnedValueType = new TypeDescriptor(returnedType);
						}
						int argCount = 0;
						while ((argCount = InternalUtils.methodSignature2Stack(m, cc.getConstantPool(), forMethodTypes)) < 0) {
							forMethodTypes = Arrays.copyOf(forMethodTypes,2*forMethodTypes.length);
						}
						desc.parametersTypes = argCount == 0 ? new TypeDescriptor[0] : Arrays.copyOf(forMethodTypes, argCount);
					} catch (ContentException exc) {
						final Constructor<?>	c = cdr.getConstructorDescription(data,startName,endSignature);
						
						desc.methodReference = cc.getConstantPool().asMethodRefDescription(
								tree.placeOrChangeName((CharSequence)c.getDeclaringClass().getName().replace('.','/'), new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE)),
								desc.methodId = tree.placeOrChangeName((CharSequence)"<init>", new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)),
								desc.signatureId = tree.placeOrChangeName((CharSequence)CompilerUtils.buildConstructorSignature(c), new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))
						);
						desc.argumentLength = 0;
						for (Parameter item : c.getParameters()) {
							desc.argumentLength += item.getType() == long.class || item.getType() == double.class ? 2 : 1;
						}
						desc.returnedValueType = new TypeDescriptor(CompilerUtils.CLASSTYPE_VOID);
						int argCount = 0;
						while ((argCount = InternalUtils.constructorSignature2Stack(c, cc.getConstantPool(), forMethodTypes)) < 0) {
							forMethodTypes = Arrays.copyOf(forMethodTypes, 2*forMethodTypes.length);
						}
						desc.parametersTypes = argCount == 0 ? new TypeDescriptor[0] : Arrays.copyOf(forMethodTypes, argCount);
					}
				}
			}
			else {
				throw new ContentException("Missing method signature!");
			}
		}
		else {
			if (data[start] == '(') {
				final int		startSignature = start, endSignature = start = skipSignature(lineNo, data, start);
				final String	signature = new String(data,startSignature,endSignature-startSignature);
				final int		checkType = InternalUtils.methodSignature2Type(signature);
				final String	returned = signature.substring(signature.indexOf(')')+1);
				final int		returnedType = InternalUtils.fieldSignature2Type(returned);
				
				desc.methodReference = cc.getConstantPool().asMethodRefDescription(
						joinedClassNameId,
						desc.methodId = tree.placeOrChangeName(data,startName,endName, new NameDescriptor(checkType)),
						desc.signatureId = tree.placeOrChangeName(data,startSignature,endSignature, new NameDescriptor(CompilerUtils.CLASSTYPE_REFERENCE))						
				);
				int argCount = 0;
				while ((argCount = InternalUtils.methodSignature2Stack(signature, cc.getConstantPool(), forMethodTypes)) < 0) {
					forMethodTypes = Arrays.copyOf(forMethodTypes, 2*forMethodTypes.length);
				}
				desc.parametersTypes = argCount == 0 ? new TypeDescriptor[0] : Arrays.copyOf(forMethodTypes, argCount);
				if (returnedType == CompilerUtils.CLASSTYPE_REFERENCE) {
					final long		typeName = tree.placeOrChangeName((CharSequence)returned, VOID_DESCRIPTOR);
					final short		displ = cc.getConstantPool().asClassDescription(typeName);
					
					desc.returnedValueType = new TypeDescriptor(returnedType, displ);
				}
				else {
					desc.returnedValueType = new TypeDescriptor(returnedType);
				}
			}
			else {
				throw new ContentException("Missing method signature!");
			}
		}
		return start;
	}
	
	private int calculateValue(final int lineNo, final char[] data, int start, final EvalState state, final long[] result) throws ContentException {
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
							throw CompilerErrors.ERR_UNCLOSED_QUOTA.syntaxError(lineNo);
						}
					case '(' :
						if (data[start = InternalUtils.skipBlank(data,calculateValue(lineNo, data, InternalUtils.skipBlank(data,start+1), EvalState.additional, result))]==')') {
							return InternalUtils.skipBlank(data,start+1);
						}
						else {
							throw CompilerErrors.ERR_UNCLOSED_BRACKET.syntaxError(lineNo);
						}
					default :
						if (Character.isJavaIdentifierStart(data[start])) {
							start = skipSimpleName(data,start);
						}
						throw CompilerErrors.ERR_NON_CONSTANT_VALUE.syntaxError(lineNo);
				}
			case unary				:
				if (data[start] == '-') {
					start = InternalUtils.skipBlank(data,calculateValue(lineNo, data, InternalUtils.skipBlank(data,start+1), EvalState.term, result));
					result[0] = -result[0];
				}
				else {
					start = InternalUtils.skipBlank(data,calculateValue(lineNo, data, start, EvalState.term, result));
				}
				return start;
			case multiplicational	:
				start = InternalUtils.skipBlank(data,calculateValue(lineNo, data, start, EvalState.unary, result));
				while ((symbol = data[start]) == '*' || symbol == '/' || symbol == '%') {
					if (value == null) {
						value = new long[] {0,0};
					}
					start = InternalUtils.skipBlank(data,calculateValue(lineNo, data, InternalUtils.skipBlank(data,start+1), EvalState.unary, value));
					switch (symbol) {
						case '*' : result[0] *= value[0]; break;
						case '/' : result[0] /= value[0]; break;
						case '%' : result[0] %= value[0]; break;
					}
				}
				return start;
			case additional			:
				start = InternalUtils.skipBlank(data,calculateValue(lineNo, data, start, EvalState.multiplicational, result));
				while ((symbol = data[start]) == '+' || symbol == '-') {
					if (value == null) {
						value = new long[] {0,0};
					}
					start = InternalUtils.skipBlank(data,calculateValue(lineNo, data, InternalUtils.skipBlank(data,start+1), EvalState.multiplicational, value));
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
	private static short addAndCheckDuplicates(final int lineNo, final short source, final short added, final String directive) throws ContentException {
		if ((source & added) != 0) {
			throw CompilerErrors.ERR_DUPLICATE_OPTION.syntaxError(lineNo, Modifier.toString(added), directive);
		}
		else {
			return (short) (source | added);
		}
	}

	private static int extractClass(final int lineNo, final char[] data, int start, final ClassDescriptionRepo cdr, final Class<?>[] result) throws ContentException {
		final int		startName = start = InternalUtils.skipBlank(data,start), endName = start = skipQualifiedName(data,start);
		
		if (cdr.hasClassDescription(data,startName,endName)) {
			result[0] = cdr.getClassDescription(data,startName,endName);
			return InternalUtils.skipBlank(data,start);
		}
		else {
			throw CompilerErrors.ERR_CLASS_IS_NOT_DECLARED.syntaxError(lineNo, new String(data,startName,endName-startName));
		}
	}
	
	private static int extractClassWithPossibleArray(final int lineNo, final char[] data, int start, final ClassDescriptionRepo cdr, final Class<?>[] result) throws ContentException {
		start = extractClass(lineNo, data, start, cdr, result);
		
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
				throw CompilerErrors.ERR_UNCLOSED_BRACKET.syntaxError(lineNo);
			}
			else {
				result[0] = Array.newInstance(result[0],new int[dimension]).getClass();
			}
		}
		return start;
	}

	private static int processOptions(final int lineNo, final char[] data, int start, final EntityDescriptor desc, final String location, final ClassDescriptionRepo cdr, final boolean treatExtendsAsImplements, final int... availableOptions) throws ContentException {
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
							throw CompilerErrors.ERR_DUPLICATE_OPTION.syntaxError(lineNo, new String(data,startOption,endOption-startOption), location);
						}
						else {
							parsed |= (1L << index);
							validOption = true;
							break;
						}
					}
				}
				
				if (!validOption) {
					throw CompilerErrors.ERR_INVALID_OPTION.syntaxError(lineNo, new String(data,startOption,endOption-startOption), location);
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
						start = new DirectiveInterfacesOption().processList(lineNo, data, start, cdr, desc);
					}
					else {
						start = dd.processList(lineNo, data, start, cdr, desc);
					}
				}
			}
			else {
				break;
			}
		}
		
		if  (checkMutualPrivateProtectedPublic(desc.options)) {
			throw CompilerErrors.ERR_MUTUALLY_EXCLUDED_OPTIONS.syntaxError(lineNo, "public/protected/private", location);
		}
		else if  (checkMutualAbstractFinal(desc.options)) {
			throw CompilerErrors.ERR_MUTUALLY_EXCLUDED_OPTIONS.syntaxError(lineNo, "abstract/final", location);
		}
		else if  (checkMutualStaticAbstract(desc.options)) {
			throw CompilerErrors.ERR_MUTUALLY_EXCLUDED_OPTIONS.syntaxError(lineNo, "static/abstract", location);
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

	private static int skipQuoted(final int lineNo, final char[] data, int from, final char terminal) throws ContentException {
		while (data[from] != '\n' && data[from] != terminal) {
			from++;
		}
		if (data[from] == terminal) {
			return from;
		}
		else {
			throw CompilerErrors.ERR_UNCLOSED_QUOTA.syntaxError(lineNo);
		}
	}

	private static int skipSignature(final int lineNo, final char[] data, int from) throws ContentException {
		while (data[from] <= ' ' && data[from] != '\n') {
			from++;
		}
		
		if (data[from] == '(') {
			from++;
			while(data[from] != ')' && data[from] != '\n') {
				from = skipSignature(lineNo, data, from);
			}
			
			if (data[from] == ')') {
				return skipSignature(lineNo, data, from+1);
			}
			else {
				throw CompilerErrors.ERR_UNCLOSED_BRACKET.syntaxError(lineNo);
			}
		}
		else {
			switch (data[from]) {
				case '['	:
					while (data[from] == '[') {
						from++;
					}
					return skipSignature(lineNo, data, from);
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
						throw CompilerErrors.ERR_SEMICOLON_IS_MISSING.syntaxError(lineNo);
					}
				default :
					throw CompilerErrors.ERR_ILLEGAL_SIGNATURE_SYMBOL.syntaxError(lineNo, data[from]);
			}
		}
	}

	private static void skip2line(final int lineNo, final char[] data, int from) throws ContentException {
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
			throw CompilerErrors.ERR_UNPARSED_TAIL.syntaxError(lineNo);
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

	private static void placeStaticCommand(final int operation, final boolean uncondBrunch, final RefTypeSource refTypeSource, final String mnemonics, final StackChanges stackChanges, final int... awaitedTypes) {
		placeStaticCommand(operation, uncondBrunch, refTypeSource, mnemonics, CommandFormat.single, stackChanges, new int[0]);
	}

	private static void placeStaticCommand(final int operation, final boolean uncondBrunch, final RefTypeSource refTypeSource, final String mnemonics, final int argumentType, final StackChanges stackChanges, final int... awaitedTypes) {
		placeStaticCommand(operation, uncondBrunch, refTypeSource, mnemonics, CommandFormat.single, argumentType, stackChanges, new int[0]);
	}
	
	private static void placeStaticCommand(final int operation, final boolean uncondBrunch, final RefTypeSource refTypeSource, final String mnemonics, final CommandFormat format, final StackChanges stackChanges, final int... awaitedTypes) {
		if (staticCommandTree.contains(operation)) {
			throw new IllegalArgumentException("Duplicate opcode ["+operation+"]: "+mnemonics+", already exists "+staticCommandTree.getName(operation));
		}
		else {
			staticCommandTree.placeOrChangeName((CharSequence)mnemonics,operation,new CommandDescriptor(operation,uncondBrunch,refTypeSource,format,DONT_CHECK_LOCAL_TYPES,stackChanges,awaitedTypes));
		}
	}	

	private static void placeStaticCommand(final int operation, final boolean uncondBrunch, final RefTypeSource refTypeSource, final String mnemonics, final CommandFormat format, final int argumentType, final StackChanges stackChanges, final int... awaitedTypes) {
		if (staticCommandTree.contains(operation)) {
			throw new IllegalArgumentException("Duplicate opcode ["+operation+"]: "+mnemonics+", already exists "+staticCommandTree.getName(operation));
		}
		else {
			staticCommandTree.placeOrChangeName((CharSequence)mnemonics,operation,new CommandDescriptor(operation,uncondBrunch,refTypeSource,format,argumentType,stackChanges,awaitedTypes));
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

	protected void printDiagnostics(final int lineNo, final char[] content, final int from, final int len) throws IOException {
		final StringBuilder	sb = new StringBuilder();
		
		switch (state) {
			case afterClass					:
			case beforeImport				:
			case beforePackage				:
				sb.append(content, from, len);
				break;
			case insideBegin				:
			case insideBeginCode			:
			case insideClassBody			:
			case insideClassMethod			:
				try{final int	pc = methodDescriptor.getBody().getPC();
				
					sb.append(pc).append(":\t").append(content, from, len);
				} catch (ContentException e) {
					sb.append('\t').append(content, from, len);
				}
				break;
			case insideClass				:
			case insideClassAbstractMethod	:
			case insideInterface			:
			case insideInterfaceAbstractMethod	:
				sb.append('\t').append('\t').append(content, from, len);
				break;
			case insideMacros				:
				sb.append('\t').append('>').append(content, from, len);
				break;
			case insideMethodLookup			:
			case insideMethodTable			:
				sb.append('\t').append('\t').append('\t').append(content, from, len);
				break;
			default:
				throw new UnsupportedOperationException("State ["+state+"] is not supported yet");
		}
		printDiagnostics(sb.toString());
	}

	protected synchronized void printDiagnostics(final String content) throws IOException {
		if (diagnostics != null) {
			if (!content.isEmpty()) {
				diagnostics.write(content);
			}
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
		
		public abstract int processList(final int lineNo, final char[] data, final int from, final ClassDescriptionRepo cdr, final EntityDescriptor result) throws ContentException; 
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
		public int processList(final int lineNo, final char[] data, final int from, final ClassDescriptionRepo cdr, final EntityDescriptor result) throws ContentException {
			return from;
		}
	}

	private static class DirectiveMarker extends DirectiveDescriptor {
		public DirectiveMarker() {
			super(DirectiveType.OPTION);
		}

		@Override
		public int processList(final int lineNo, final char[] data, final int from, final ClassDescriptionRepo cdr, final EntityDescriptor result) throws ContentException {
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
		public int processList(final int lineNo, final char[] data, final int from, final ClassDescriptionRepo cdr, final EntityDescriptor result) throws ContentException {
			return from;
		}
	}
	
	private static class DirectiveInterfacesOption extends DirectiveDescriptor {
		public DirectiveInterfacesOption() {
			super(DirectiveType.LIST);
		}

		@Override
		public int processList(final int lineNo, final char[] data, int from, final ClassDescriptionRepo cdr, final EntityDescriptor result) throws ContentException {
			final Class<?>[]	forClass = new Class<?>[1];

			from--;
			do {from = extractClass(lineNo, data, from+1, cdr, forClass);
				if (!forClass[0].isInterface()) {
					throw CompilerErrors.ERR_MUST_BE_INTERFACE.syntaxError(lineNo, forClass[0].getName());
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
		public int processList(final int lineNo, final char[] data, int from, final ClassDescriptionRepo cdr, final EntityDescriptor result) throws ContentException {
			final Class<?>[]	forClass = new Class<?>[1];
			
			from = extractClass(lineNo, data, from, cdr, forClass);
			if (forClass[0].isInterface()) {
				throw CompilerErrors.ERR_MUST_NOT_BE_INTERFACE.syntaxError(lineNo, forClass[0].getName());
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
		public int processList(final int lineNo, final char[] data, int from, final ClassDescriptionRepo cdr, final EntityDescriptor result) throws ContentException {
			final Class<?>[]	forClass = new Class<?>[1];

			from--;
			do {from = extractClass(lineNo, data, from+1, cdr, forClass);
				if (!Throwable.class.isAssignableFrom(forClass[0])) {
					throw CompilerErrors.ERR_MUST_BE_COMPATIBLE.syntaxError(lineNo, forClass[0].getName(), Throwable.class.getName());
				}
				else {
					result.throwsList.add(forClass[0]);
				}
			} while (data[from] == ',');
			return from;
		}
	}

	private static class FieldAccessDescriptor {
		long				fieldId;
		short				fieldReference;
		long				signatureId;
		TypeDescriptor		valueType;
		@Override
		public String toString() {
			return "FieldAccessDescriptor [fieldId=" + fieldId + ", fieldReference=" + fieldReference + ", signatureId="
					+ signatureId + ", valueType=" + valueType + "]";
		}
	}
	
	private static class MethodCallDescriptor {
		long				methodId;
		short				methodReference;
		long				signatureId;
		int					argumentLength;
		TypeDescriptor[]	parametersTypes;
		TypeDescriptor		returnedValueType;
		
		@Override
		public String toString() {
			return "MethodCallDescriptor [methodId=" + methodId + ", methodReference=" + methodReference
					+ ", signatureId=" + signatureId
					+ ", argumentLength=" + argumentLength + ", parametersTypes=" + Arrays.toString(parametersTypes)
					+ ", returnedValueType=" + returnedValueType + "]";
		}
	}
	
	private static class CommandDescriptor {
		public final byte			operation;
		public final boolean		uncondBrunch;
		public final RefTypeSource	refTypeSource;
		public final CommandFormat	commandFormat;
		public final int			argumentType;
		public final StackChanges	stackChanges;
		public final int[]			checkedTypes;
		
		public CommandDescriptor(final int operation, final boolean uncondBrunch, final RefTypeSource refTypeSource, final CommandFormat commandFormat, final int argumentType, final StackChanges stackChanges, final int[] checkedTypes) {
			this.operation = (byte)operation;
			this.uncondBrunch = uncondBrunch;
			this.refTypeSource = refTypeSource;
			this.commandFormat = commandFormat;
			this.argumentType = argumentType;
			this.stackChanges = stackChanges;
			this.checkedTypes = checkedTypes;
		}

		@Override
		public String toString() {
			return "CommandDescriptor [operation=0x" + Integer.toHexString(operation) + ", uncondBrunch=" + uncondBrunch + ", refTypeSource="
					+ refTypeSource + ", commandFormat=" + commandFormat + ", argumentType=" + argumentType
					+ ", stackChanges=" + stackChanges + ", checkedTypes=" + Arrays.toString(checkedTypes) + "]";
		}
	}
}
