package chav1961.purelib.streams.char2byte.asm.macro;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.streams.char2byte.asm.AssignableExpressionNodeInterface;
import chav1961.purelib.streams.char2byte.asm.CompilerUtils;
import chav1961.purelib.streams.char2byte.asm.ExpressionNodeInterface;
import chav1961.purelib.streams.char2byte.asm.ExpressionNodeType;
import chav1961.purelib.streams.char2byte.asm.macro.AssemblerTemplateRepo.NameKeeper;

public class MacroCompiler {
	private static final String		MACROCOMPILER_RESOURCE = "macrocompiler.txt";
	
	private static final char[]		PART_START = "start".toCharArray();
	private static final char[]		PART_FINISH = "finish".toCharArray();
	private static final char[]		PART_SUBSTITUTION_CONST = "substitutionConst".toCharArray();
	private static final char[]		PART_EXTRACT_CHAR_CONST = "extractCharConst".toCharArray();
	private static final char[]		PART_VALIDATE_VAR_VALUE = "validateVarValue".toCharArray();
	private static final char[]		PART_CONCAT_BEFORE = "concatBefore".toCharArray();
	private static final char[]		PART_CONCAT_CALCULATE_LENGTH = "concatCalculateLength".toCharArray();
	private static final char[]		PART_CONCAT_CREATE_ARRAY = "concatCreateArray".toCharArray();	
	private static final char[]		PART_CONCAT_APPEND = "concatAppend".toCharArray();
	private static final char[]		PART_PREPARE_WRAPPER = "prepareWrapper".toCharArray();
	private static final char[]		PART_STORE_INT = "storeInt".toCharArray();
	private static final char[]		PART_STORE_REAL = "storeReal".toCharArray();
	private static final char[]		PART_STORE_STRING = "storeString".toCharArray();
	private static final char[]		PART_STORE_BOOLEAN = "storeBoolean".toCharArray();
	private static final char[]		PART_TERNARY_BEFORE = "ternaryBefore".toCharArray();
	private static final char[]		PART_TERNARY_FALSE = "ternaryFalse".toCharArray();
	private static final char[]		PART_TERNARY_AFTER = "ternaryAfter".toCharArray();
	private static final char[]		PART_COMPARISON = "comparison".toCharArray();
	private static final char[]		PART_OR_TRUE = "orTrue".toCharArray();
	private static final char[]		PART_OR_AFTER = "orAfter".toCharArray();
	private static final char[]		PART_AND_FALSE = "andFalse".toCharArray();
	private static final char[]		PART_AND_AFTER = "andAfter".toCharArray();
	private static final char[]		PART_MERROR_BEFORE = "merrorBefore".toCharArray();
	private static final char[]		PART_MERROR_AFTER = "merrorAfter".toCharArray();
	
	private static final char[]		FROM_INDEX = "fromIndex".toCharArray();
	private static final char[]		TO_INDEX = "toIndex".toCharArray();
	private static final char[]		VAR_INDEX = "varIndex".toCharArray();
	private static final char[]		LABEL_TRUE = "labelTrue".toCharArray();
	private static final char[]		LABEL_FALSE = "labelFalse".toCharArray();
	private static final char[]		SKIP_COMMAND = "skipCommand".toCharArray();
	private static final char[]		COMMAND_IFEQ = "ifeq".toCharArray();
	private static final char[]		COMMAND_IFGE = "ifge".toCharArray();
	private static final char[]		COMMAND_IFGT = "ifgt".toCharArray();
	private static final char[]		COMMAND_IFLE = "ifle".toCharArray();
	private static final char[]		COMMAND_IFLT = "iflt".toCharArray();
	private static final char[]		COMMAND_IFNE = "ifne".toCharArray();

	private static final Method		ME_TO_STRING_L;
	private static final Method		ME_TO_STRING_D;
	private static final Method		ME_TO_STRING_Z;
	private static final Method		ME_COMPARE_STRINGS;
	private static final Method		ME_VALUE_EXISTS;
	private static final Method		ME_TO_BOOLEAN;
	private static final Method		ME_SPLIT;

	private static final Method		GCA_APPEND;

	private static final Method		ENI_GET_LONG;	
	private static final Method		ENI_GET_DOUBLE;	
	private static final Method		ENI_GET_STRING;	
	private static final Method		ENI_GET_BOOLEAN;	

	private static final Method		CU_PARSE_SIGNED_LONG;	
	private static final Method		CU_PARSE_SIGNED_DOUBLE;	
	
	static {
		try{final Class<MacroExecutor>				macroExecutorClass = MacroExecutor.class;
			
			ME_TO_STRING_L = macroExecutorClass.getMethod("toString",long.class);
			ME_TO_STRING_D = macroExecutorClass.getMethod("toString",double.class);
			ME_TO_STRING_Z = macroExecutorClass.getMethod("toString",boolean.class);
			ME_COMPARE_STRINGS = macroExecutorClass.getMethod("compareStrings",char[].class,char[].class);
			ME_VALUE_EXISTS = macroExecutorClass.getMethod("valueExists",AssignableExpressionNodeInterface.class);
			ME_TO_BOOLEAN = macroExecutorClass.getMethod("toBoolean",char[].class); 
			ME_SPLIT = macroExecutorClass.getMethod("split",char[].class,char[].class);
			
			final Class<GrowableCharArray>			growableCharArrayClass = GrowableCharArray.class; 

			GCA_APPEND = growableCharArrayClass.getMethod("append",char[].class);
			
			final Class<ExpressionNodeInterface>	expressionNodeInterfaceClass = ExpressionNodeInterface.class;
			
			ENI_GET_LONG = expressionNodeInterfaceClass.getMethod("getLong");
			ENI_GET_DOUBLE = expressionNodeInterfaceClass.getMethod("getDouble");
			ENI_GET_STRING = expressionNodeInterfaceClass.getMethod("getString");
			ENI_GET_BOOLEAN = expressionNodeInterfaceClass.getMethod("getBoolean");
			
			final Class<CharUtils>					charUtilsClass = CharUtils.class;
			
			CU_PARSE_SIGNED_LONG = charUtilsClass.getMethod("parseSignedLong",char[].class,int.class,long[].class,boolean.class);
			CU_PARSE_SIGNED_DOUBLE = charUtilsClass.getMethod("parseSignedDouble",char[].class,int.class,double[].class,boolean.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new PreparationException("MacroCompiler class initialization failed : "+e.getLocalizedMessage(),e);
		}
	}
	
	public static void compile(final String className, final Command command, final GrowableCharArray writer, final GrowableCharArray stringRepo) throws IOException, SyntaxException, CalculationException {
		try(final InputStream			is = MacroCompiler.class.getResourceAsStream(MACROCOMPILER_RESOURCE)) {
			final AssemblerTemplateRepo	repo = new AssemblerTemplateRepo(is);
			final Storage				storage = new Storage(stringRepo); 
			
			repo.getNameKeeper().put("className",className).put("shortClassName",className.substring(className.lastIndexOf('.')+1));
			compile(command,writer,storage,repo,repo.getNameKeeper(),null);
		}
	}
	
	private static void compile(final Command command, final GrowableCharArray writer, final Storage storage, final AssemblerTemplateRepo repo, final NameKeeper callback, final JumpStack jumpStack) throws CalculationException {
		try(final NameKeeper	current = callback.push()) {
			switch (command.getType()) {
				case MACRO			:
					int		mEnd	= storage.uniqueLabel++;
					
					repo.append(writer,PART_START,callback);
					compileSequence(command.container,writer,storage,repo,callback,new JumpStack(mEnd,0,0,command.getType()));
					repo.append(writer,PART_FINISH,callback);
					break;
				case SET			:
					repo.append(writer,PART_PREPARE_WRAPPER,current.put(VAR_INDEX,((SetCommand)command).leftPart.getSequentialNumber()));
					if (((SetCommand)command).leftPart.getValueType() != ExpressionNodeValue.BOOLEAN) {
						compileExpression(((SetCommand)command).rightPart[0],storage,repo,current,writer,0,0);
						storeValue(((SetCommand)command).leftPart,storage,repo,current,writer);
					}
					else {
						final int	trueLabel = storage.uniqueLabel++;
						
						repo.append(writer," iconst_1\n");
						compileExpression(((SetCommand)command).rightPart[0],storage,repo,current,writer,trueLabel,0);
						repo.append(writer," iconst_1\n isub\nlabel"+trueLabel+":\n");
						storeValue(((SetCommand)command).leftPart,storage,repo,current,writer);
					}
					break;
				case IF				:
					final int	labelBreakIf = storage.uniqueLabel++;
					int			labelNext = storage.uniqueLabel++;;
					
					for (Command item : command.container) {
						if (item instanceof IfConditionCommand) {
							compileExpression(((IfConditionCommand)item).cond[0],storage,repo,current,writer,0,labelNext);
							compileSequence(((IfConditionCommand)item).container,writer,storage,repo,current,jumpStack);
							repo.append(writer," goto	label"+labelBreakIf+"\nlabel"+labelNext+":\n");
						}
						else {
							compileSequence(((ElseCommand)item).container,writer,storage,repo,current,jumpStack);
						}
						labelNext = storage.uniqueLabel++;
					}
					repo.append(writer,"label"+labelBreakIf+":\n");
					break;
				case WHILE			:
					final int	labelWhileAgain = storage.uniqueLabel++, labelWhileBreak = storage.uniqueLabel++;

					repo.append(writer,"label"+labelWhileAgain+":\n");
					compileExpression(((WhileCommand)command).cond[0],storage,repo,current,writer,0,labelWhileBreak);
					compileSequence(((WhileCommand)command).container,writer,storage,repo,current
							,((WhileCommand)command).getLabel() != null 
									? new JumpStack(jumpStack,jumpStack.exitLabel,labelWhileBreak,labelWhileAgain,CommandType.WHILE,((WhileCommand)command).getLabel())
									: new JumpStack(jumpStack,jumpStack.exitLabel,labelWhileBreak,labelWhileAgain,CommandType.WHILE)
							);
					repo.append(writer," goto label"+labelWhileAgain+"\nlabel"+labelWhileBreak+":\n");
					break;
				case FOR			:
					final int	labelForAgain = storage.uniqueLabel++, labelForBreak = storage.uniqueLabel++;

					repo.append(writer,PART_PREPARE_WRAPPER,current.put(VAR_INDEX,((AssignableExpressionNode)((ForCommand)command).parameters[0]).getSequentialNumber()));
					compileExpression(((ForCommand)command).parameters[1],storage,repo,current,writer,0,0);
					storeValue(((ForCommand)command).parameters[0],storage,repo,current,writer);
					
					repo.append(writer,"label"+labelForAgain+":\n");
					compileExpression(((ForCommand)command).parameters[0],storage,repo,current,writer,0,0);
					compileExpression(((ForCommand)command).parameters[2],storage,repo,current,writer,0,0);
					buldComparison(((ForCommand)command).parameters[0].getValueType(),ExpressionNodeOperator.LE,storage,repo,callback,writer,0,labelForBreak);
					
					compileSequence(((ForCommand)command).container,writer,storage,repo,current
							,((ForCommand)command).getLabel() != null 
							? new JumpStack(jumpStack,jumpStack.exitLabel,labelForBreak,labelForAgain,CommandType.FOR,((ForCommand)command).getLabel())
							: new JumpStack(jumpStack,jumpStack.exitLabel,labelForBreak,labelForAgain,CommandType.FOR)
					);
					
					repo.append(writer,PART_PREPARE_WRAPPER,current.put(VAR_INDEX,((AssignableExpressionNode)((ForCommand)command).parameters[0]).getSequentialNumber()));
					compileExpression(((ForCommand)command).parameters[0],storage,repo,current,writer,0,0);
					compileExpression(((ForCommand)command).parameters[3],storage,repo,current,writer,0,0);
					switch (((AssignableExpressionNode)((ForCommand)command).parameters[0]).getValueType()) {
						case INTEGER	: repo.append(writer," ladd\n"); break;
						case REAL		: repo.append(writer," dadd\n"); break;
						case STRING	: case BOOLEAN : throw new CalculationException("Illegal data type ["+((AssignableExpressionNode)((ForCommand)command).parameters[0]).getValueType()+"] for loop variable");
						default : throw new UnsupportedOperationException("Data type ["+((AssignableExpressionNode)((ForCommand)command).parameters[0]).getValueType()+"] is not supported yet");
					}
					storeValue(((ForCommand)command).parameters[0],storage,repo,current,writer);
					repo.append(writer," goto label"+labelForAgain+"\nlabel"+labelForBreak+":\n");
					break;
				case FOR_EACH 		:					
					final int	labelForeachBreak = storage.uniqueLabel++, labelForeachAgain = storage.uniqueLabel++, uniqueSuffix = storage.uniqueLabel++; 

					repo.append(writer," .begin\neachArray"+uniqueSuffix+" .var char[][] final\neachArrayIndex"+uniqueSuffix+" .var int\n");
					compileExpression(((ForEachCommand)command).parameters[1],storage,repo,current,writer,0,0);
					compileExpression(((ForEachCommand)command).parameters[2],storage,repo,current,writer,0,0);
					repo.append(writer," " + CompilerUtils.buildMethodCall(ME_SPLIT)+"\n astore eachArray"+uniqueSuffix+"\n iconst_0\n istore eachArrayIndex"+uniqueSuffix+"\n");
					
					repo.append(writer,"label"+labelForeachAgain+":\n");
					repo.append(writer,PART_PREPARE_WRAPPER,current.put(VAR_INDEX,((AssignableExpressionNode)((ForEachCommand)command).parameters[0]).getSequentialNumber()));
					repo.append(writer," aload eachArray"+uniqueSuffix+"\n iload eachArrayIndex"+uniqueSuffix+"\n aaload\n");
					storeValue(((ForEachCommand)command).parameters[0],storage,repo,current,writer);

					compileSequence(((ForEachCommand)command).container,writer,storage,repo,current
							,((ForEachCommand)command).getLabel() != null 
							? new JumpStack(jumpStack,jumpStack.exitLabel,labelForeachBreak,labelForeachAgain,CommandType.FOR_EACH,((ForEachCommand)command).getLabel())
							: new JumpStack(jumpStack,jumpStack.exitLabel,labelForeachBreak,labelForeachAgain,CommandType.FOR_EACH)
					);
					
					repo.append(writer," iinc eachArrayIndex"+uniqueSuffix+",1\n aload eachArray"+uniqueSuffix+"\n arraylength\n iload eachArrayIndex"+uniqueSuffix+"\n if_icmpgt label"+labelForeachAgain+"\n");
					repo.append(writer,"label"+labelForeachBreak+":\n .end\n");
					break;
				case BREAK			:
					if (((BreakCommand)command).getLabel() == null) {
						repo.append(writer," goto label"+jumpStack.breakLabel+"\n");
					}
					else {
						JumpStack	js = jumpStack;
						
						while (js != null) {
							if (CharUtils.compare(((BreakCommand)command).getLabel(),0,js.label)) {
								repo.append(writer," goto label"+jumpStack.breakLabel+"\n");
								break;
							}
							else {
								js = js.parent;
							}
						}
						if (js == null) {
							throw new CalculationException("Break label ["+new String(((BreakCommand)command).getLabel())+"] not found anywhere");
						}
					}
					break;
				case CONTINUE		:
					if (((ContinueCommand)command).getLabel() == null) {
						repo.append(writer," goto label"+jumpStack.continueLabel+"\n");
					}
					else {
						JumpStack	js = jumpStack;
						
						while (js != null) {
							if (CharUtils.compare(((ContinueCommand)command).getLabel(),0,js.label)) {
								repo.append(writer," goto label"+jumpStack.continueLabel+"\n");
								break;
							}
							else {
								js = js.parent;
							}
						}
						if (js == null) {
							throw new CalculationException("Continue label ["+new String(((ContinueCommand)command).getLabel())+"] not found anywhere");
						}
					}
					break;
				case CHOISE			:
					final int	labelBreakChoise = storage.uniqueLabel++;
					int			labelNextChoise = storage.uniqueLabel++;
					
					compileExpression(((ChoiseContainer)command).expr[0],storage,repo,current,writer,0,0);
					for (Command item : command.container) {
						if (item instanceof ChoiseConditionCommand) {
							switch (((ChoiseContainer)command).expr[0].getValueType()) {
								case INTEGER : case REAL : 	 repo.append(writer," dup2\n"); break;
								case STRING : case BOOLEAN : repo.append(writer," dup\n"); break;
								default : throw new UnsupportedOperationException("Data type ["+((ChoiseContainer)command).expr[0].getValueType()+"] is not supported yet");
							}
							compileExpression(((ChoiseConditionCommand)item).value[0],storage,repo,current,writer,0,0);
							buldComparison(((ChoiseContainer)command).expr[0].getValueType(),ExpressionNodeOperator.EQ,storage,repo,callback,writer,0,labelNextChoise);
							compileSequence(((ChoiseConditionCommand)item).container,writer,storage,repo,current,jumpStack);
							repo.append(writer," goto	label"+labelBreakChoise+"\nlabel"+labelNextChoise+":\n");
						}
						else {
							compileSequence(((OtherwiseCommand)item).container,writer,storage,repo,current,jumpStack);
						}
						labelNextChoise = storage.uniqueLabel++;
					}
					switch (((ChoiseContainer)command).expr[0].getValueType()) {
						case INTEGER : case REAL : 	 repo.append(writer,"label"+labelBreakChoise+": pop2\n"); break;
						case STRING : case BOOLEAN : repo.append(writer,"label"+labelBreakChoise+": pop\n"); break;
						default : throw new UnsupportedOperationException("Data type ["+((ChoiseContainer)command).expr[0].getValueType()+"] is not supported yet");
					}
					break;
				case EXIT			:
					repo.append(writer," return\n");
					break;
				case MERROR			:
					repo.append(writer,PART_MERROR_BEFORE,current);
					compileExpression(((MErrorCommand)command).value[0],storage,repo,current,writer,0,0);
					repo.append(writer,PART_MERROR_AFTER,current);
					break;
				case SUBSTITUTION	:	// Special case for substitution strings.
					for (ExpressionNode item : ((SubstitutionCommand)command).subst[0].operands) {
						if (item.getType() == ExpressionNodeType.CONSTANT) {
							final long	location = storage.allocateCharArray(item.getString());
							final int	from = (int)(location >>> 32), to = (int)(location & 0xFFFFFFFF);
							
							repo.append(writer,PART_SUBSTITUTION_CONST,current.put(FROM_INDEX,from).put(TO_INDEX,to));
						}
						else {
							repo.append(writer," aload_2\n");
							compileExpression(item,storage,repo,current,writer,0,0);
							repo.append(writer," "+CompilerUtils.buildMethodCall(GCA_APPEND)+"\n pop\n");
						}
					}
					break;
				case IF_CONDITION : case ELSE : case CHOISE_CONDITION :case OTHERWISE : throw new IllegalStateException("Internal error");
				default : throw new UnsupportedOperationException("Internal error");
			}
		}
	}
	
	static void compileSequence(final List<Command> list, final GrowableCharArray writer, final Storage storage, final AssemblerTemplateRepo repo, final NameKeeper callback, final JumpStack jumpStack) throws CalculationException {
		for (Command item : list) {
			compile(item,writer,storage,repo,callback,jumpStack);
		}
	}

	private static void storeValue(final ExpressionNode node, final Storage storage, final AssemblerTemplateRepo repo, final NameKeeper callback, final GrowableCharArray writer) throws CalculationException {
		switch (node.getValueType()) {
			case INTEGER	: repo.append(writer,PART_STORE_INT,callback); break;
			case REAL		: repo.append(writer,PART_STORE_REAL,callback); break;
			case STRING		: repo.append(writer,PART_STORE_STRING,callback); break;
			case BOOLEAN	: repo.append(writer,PART_STORE_BOOLEAN,callback); break;
			default : throw new UnsupportedOperationException("Data type ["+node.getValueType()+"] is not supported yet");
		}
	}

	static void compileExpression(final ExpressionNode node, final Storage storage, final AssemblerTemplateRepo repo, final NameKeeper callback, final GrowableCharArray writer, final int trueLabel, final int falseLabel) throws CalculationException {
		try(final NameKeeper	current = callback.push()) {
			switch (node.getType()) {
				case CONSTANT				:
					buildConstant(node, storage, repo, callback, writer, trueLabel, falseLabel);
					break;
				case KEY_PARAMETER : case POSITIONAL_PARAMETER : case LOCAL_VARIABLE :
					repo.append(writer,PART_VALIDATE_VAR_VALUE,current.put(VAR_INDEX,((AssignableExpressionNode)node).getSequentialNumber()));
					
					switch ((((AssignableExpressionNode)node).getValueType())) {
						case INTEGER	:
							repo.append(writer," "+CompilerUtils.buildMethodCall(ENI_GET_LONG)+"\n");
							break;
						case REAL		:
							repo.append(writer," "+CompilerUtils.buildMethodCall(ENI_GET_DOUBLE)+"\n");
							break;
						case STRING		:
							repo.append(writer," "+CompilerUtils.buildMethodCall(ENI_GET_STRING)+"\n");
							break;
						case BOOLEAN	:
							repo.append(writer," "+CompilerUtils.buildMethodCall(ENI_GET_BOOLEAN)+"\n");
							if (trueLabel != 0 || falseLabel != 0) {
								if (trueLabel != 0) {
									repo.append(writer," ifne label"+trueLabel+"\n");
									if (falseLabel != 0) {
										repo.append(writer," goto label"+falseLabel+"\n");
									}
								}
								else if (falseLabel != 0) {
									repo.append(writer," ifeq label"+falseLabel+"\n");
								}
							}
							break;
						default : throw new UnsupportedOperationException("Data type ["+node.getValueType()+"] is not supported yet");
					}
					break;
				case EXPRESSION				:
					if (hasConstantsOnly(node)) {
						buildConstant(node,storage,repo,callback,writer,trueLabel,falseLabel);
					}
					else {
						switch (((OperatorNode)node).getOperator()) {
							case OR			:
								final ExpressionNode[] 	orList = ((OperatorNode)node).getOperands();
								
								if (trueLabel == 0 && falseLabel == 0) {	// Convert to bool
									final int			orTrue = storage.uniqueLabel++, orFalse = storage.uniqueLabel++;
									
									for (ExpressionNode item : orList) {
										compileExpression(item,storage,repo,current,writer,0,0);
										repo.append(writer,PART_OR_TRUE,current.put(LABEL_TRUE,orTrue).put(LABEL_FALSE,orFalse));									
									}
									repo.append(writer,PART_OR_AFTER,current.put(LABEL_TRUE,orTrue).put(LABEL_FALSE,orFalse));									
								}
								else {
									final int	orTrue = trueLabel == 0 ? storage.uniqueLabel++ : trueLabel;
									
									for (ExpressionNode item : orList) {
										compileExpression(item,storage,repo,current,writer,orTrue,0);
									}
									if (falseLabel != 0) {
										repo.append(writer," goto label"+falseLabel+'\n');									
									}
									if (trueLabel == 0) {
										repo.append(writer,"label"+orTrue+":\n");									
									}
								}
								break;
							case AND		:
								final ExpressionNode[] 	andList = ((OperatorNode)node).getOperands();
								
								if (trueLabel == 0 && falseLabel == 0) {	// Convert to bool
									final int			andTrue = storage.uniqueLabel++, andFalse = storage.uniqueLabel++;
									
									for (ExpressionNode item : andList) {
										compileExpression(item,storage,repo,current,writer,trueLabel,falseLabel);
										repo.append(writer,PART_AND_FALSE,current.put(LABEL_TRUE,andTrue).put(LABEL_FALSE,andFalse));									
									}
									repo.append(writer,PART_AND_AFTER,current.put(LABEL_TRUE,andTrue).put(LABEL_FALSE,andFalse));									
								}
								else {
									final int	andFalse = falseLabel == 0 ? storage.uniqueLabel++ : falseLabel;
									
									for (ExpressionNode item : andList) {
										compileExpression(item,storage,repo,current,writer,0,andFalse);
									}
									if (trueLabel != 0) {
										repo.append(writer," goto label"+trueLabel+'\n');									
									}
									if (falseLabel == 0) {
										repo.append(writer,"label"+andFalse+":\n");									
									}
								}
								break;
							case NOT		:
								if (trueLabel == 0 && falseLabel == 0) {
									compileExpression(((OperatorNode)node).getOperands()[0],storage,repo,current,writer,trueLabel,falseLabel);
									writer.append("	iconst_1\n isub\n");
								}
								else {
									compileExpression(((OperatorNode)node).getOperands()[0],storage,repo,current,writer,falseLabel,trueLabel);
								}
								break;
							case EQ : case NE :case GE :case GT : case LE : case LT :
								compileExpression(((OperatorNode)node).getOperands()[0],storage,repo,current,writer,trueLabel,falseLabel);
								compileExpression(((OperatorNode)node).getOperands()[1],storage,repo,current,writer,trueLabel,falseLabel);
								buldComparison(((OperatorNode)node).getOperands()[0].getValueType(),((OperatorNode)node).getOperator(),storage,repo,callback,writer,trueLabel,falseLabel);
								break;
							case TERNARY	:
								final int	ternaryTrue = storage.uniqueLabel++, ternaryFalse = storage.uniqueLabel++;  
								
								compileExpression(((OperatorNode)node).getOperands()[0],storage,repo,callback,writer,0,0);
								repo.append(writer,PART_TERNARY_BEFORE,current.put(LABEL_TRUE,ternaryTrue).put(LABEL_FALSE,ternaryFalse));									
								compileExpression(((OperatorNode)node).getOperands()[1],storage,repo,callback,writer,trueLabel,falseLabel);
								repo.append(writer,PART_TERNARY_FALSE,current.put(LABEL_TRUE,ternaryTrue).put(LABEL_FALSE,ternaryFalse));									
								compileExpression(((OperatorNode)node).getOperands()[2],storage,repo,callback,writer,trueLabel,falseLabel);
								repo.append(writer,PART_TERNARY_AFTER,current.put(LABEL_TRUE,ternaryTrue).put(LABEL_FALSE,ternaryFalse));									
								break;
							case CAT		:
								final ExpressionNode[] catList = ((OperatorNode)node).getOperands();
								
								repo.append(writer,PART_CONCAT_BEFORE,current);
								for (ExpressionNode item : catList) {
									compileExpression(item,storage,repo,callback,writer,trueLabel,falseLabel);
									repo.append(writer,PART_CONCAT_CALCULATE_LENGTH,current);									
								}
								repo.append(writer,PART_CONCAT_CREATE_ARRAY,current);									
								for (int index = 0; index < catList.length; index++) {
									repo.append(writer,PART_CONCAT_APPEND,current);									
								}
								writer.append("	aload result\n");
								break;
							case ADD		:
								compileExpression(((OperatorNode)node).getOperands()[0],storage,repo,callback,writer,trueLabel,falseLabel);
								compileExpression(((OperatorNode)node).getOperands()[1],storage,repo,callback,writer,trueLabel,falseLabel);
								switch (node.getValueType()) {
									case INTEGER	:
										writer.append("	ladd\n");
										break;
									case REAL		:
										writer.append("	dadd\n");
										break;
									case STRING	: case BOOLEAN :
										throw new CalculationException("Addition is not applicable for data type ["+node.getValueType()+"]");
									default : throw new UnsupportedOperationException("Data type ["+node.getValueType()+"] is not supported yet");
								}
								break;
							case SUB		:
								compileExpression(((OperatorNode)node).getOperands()[0],storage,repo,callback,writer,trueLabel,falseLabel);
								compileExpression(((OperatorNode)node).getOperands()[1],storage,repo,callback,writer,trueLabel,falseLabel);
								switch (node.getValueType()) {
									case INTEGER	:
										writer.append("	lsub\n");
										break;
									case REAL		:
										writer.append("	dsub\n");
										break;
									case STRING	: case BOOLEAN :
										throw new CalculationException("Substitution is not applicable for data type ["+node.getValueType()+"]");
									default : throw new UnsupportedOperationException("Data type ["+node.getValueType()+"] is not supported yet");
								}
								break;
							case MUL		:
								compileExpression(((OperatorNode)node).getOperands()[0],storage,repo,callback,writer,trueLabel,falseLabel);
								compileExpression(((OperatorNode)node).getOperands()[1],storage,repo,callback,writer,trueLabel,falseLabel);
								switch (node.getValueType()) {
									case INTEGER	:
										writer.append("	lmul\n");
										break;
									case REAL		:
										writer.append("	dmul\n");
										break;
									case STRING	: case BOOLEAN :
										throw new CalculationException("Multiplication is not applicable for data type ["+node.getValueType()+"]");
									default : throw new UnsupportedOperationException("Data type ["+node.getValueType()+"] is not supported yet");
								}
								break;
							case DIV		:
								compileExpression(((OperatorNode)node).getOperands()[0],storage,repo,callback,writer,trueLabel,falseLabel);
								compileExpression(((OperatorNode)node).getOperands()[1],storage,repo,callback,writer,trueLabel,falseLabel);
								switch (node.getValueType()) {
									case INTEGER	:
										writer.append("	ldiv\n");
										break;
									case REAL		:
										writer.append("	ddiv\n");
										break;
									case STRING	: case BOOLEAN :
										throw new CalculationException("Division is not applicable for data type ["+node.getValueType()+"]");
									default : throw new UnsupportedOperationException("Data type ["+node.getValueType()+"] is not supported yet");
								}
								break;
							case MOD		:
								compileExpression(((OperatorNode)node).getOperands()[0],storage,repo,callback,writer,trueLabel,falseLabel);
								compileExpression(((OperatorNode)node).getOperands()[1],storage,repo,callback,writer,trueLabel,falseLabel);
								switch (node.getValueType()) {
									case INTEGER	:
										writer.append("	lrem\n");
										break;
									case REAL		:
										writer.append("	drem\n");
										break;
									case STRING	: case BOOLEAN :
										throw new CalculationException("Remainder is not applicable for data type ["+node.getValueType()+"]");
									default : throw new UnsupportedOperationException("Data type ["+node.getValueType()+"] is not supported yet");
								}
								break;
							case NEG		:
								compileExpression(((NegNode)node).getOperands()[0],storage,repo,callback,writer,trueLabel,falseLabel);
								switch (node.getValueType()) {
									case INTEGER	:
										writer.append("	lneg\n");
										break;
									case REAL		:
										writer.append("	dneg\n");
										break;
									case STRING	: case BOOLEAN :
										throw new CalculationException("Negation is not applicable for data type ["+node.getValueType()+"]");
									default : throw new UnsupportedOperationException("Data type ["+node.getValueType()+"] is not supported yet");
								}
								break;
							case F_UG		:
								writer.append("	iload	uniqueG\n i2l\n");
								break;
							case F_UL		:
								writer.append("	iinc	uniqueL,1\n	iload	uniqueL\n i2l\n");
								break;
							case F_EXISTS	:
								if (((FuncExistsNode)node).operands.get(0) instanceof AssignableExpressionNode) {
									writer.append("	aload_1\n");
									writer.append((" ldc "+((AssignableExpressionNode)((FuncExistsNode)node).operands.get(0)).getSequentialNumber()+"\n"));
									writer.append("	aaload\n");
									writer.append("	"+CompilerUtils.buildMethodCall(ME_VALUE_EXISTS)+"\n");

									if (trueLabel != 0 || falseLabel != 0) {
										if (trueLabel != 0) {
											writer.append((" ifne label"+trueLabel+"\n"));
											if (falseLabel != 0) {
												writer.append((" goto label"+falseLabel+"\n"));
											}
										}
										else if (falseLabel != 0) {
											writer.append((" ifeq label"+falseLabel+"\n"));
										}
									}
								}
								else {
									throw new CalculationException("Parameter for the exists(...) can be variable or macro parameter only!");
								}
								break;
							case F_TO_INT	:
								compileExpression(((FuncToStringNode)node).operands.get(0),storage,repo,current,writer,trueLabel,falseLabel);
								switch (((FuncToStringNode)node).operands.get(0).getValueType()) {
									case INTEGER	:
										break;
									case REAL		:
										writer.append("	dtol\n");
										break;
									case STRING		:
										writer.append("	ldc	0\n");
										writer.append("	getfield longResult\n");
										writer.append("	ldc	0\n");
										writer.append("	"+CompilerUtils.buildMethodCall(CU_PARSE_SIGNED_LONG)+"\n");
										writer.append("	pop\n");
										writer.append("	getfield longResult\n");
										writer.append("	ldc	0\n");
										writer.append("	lload\n");
										break;
									case BOOLEAN	:
										throw new CalculationException("Conversion to int is not applicable for data type ["+node.getValueType()+"]");
									default : throw new UnsupportedOperationException("Data type ["+((FuncToStringNode)node).operands.get(0).getValueType()+"] is not supported yet");
								}
								break;
							case F_TO_REAL	:
								compileExpression(((FuncToStringNode)node).operands.get(0),storage,repo,current,writer,trueLabel,falseLabel);
								switch (((FuncToStringNode)node).operands.get(0).getValueType()) {
									case INTEGER	:
										writer.append("	ltod\n");
										break;
									case REAL		:
										break;
									case STRING		:
										writer.append("	ldc	0\n");
										writer.append("	getfield doubleResult\n");
										writer.append("	ldc	0\n");
										writer.append("	"+CompilerUtils.buildMethodCall(CU_PARSE_SIGNED_DOUBLE)+"\n");
										writer.append("	pop\n");
										writer.append("	getfield doubleResult\n");
										writer.append("	ldc	0\n");
										writer.append("	dload\n");
										break;
									case BOOLEAN	:
										throw new CalculationException("Conversion to double is not applicable for data type ["+node.getValueType()+"]");
									default : throw new UnsupportedOperationException("Data type ["+((FuncToStringNode)node).operands.get(0).getValueType()+"] is not supported yet");
								}
								break;
							case F_TO_STR	:
								compileExpression(((FuncToStringNode)node).operands.get(0),storage,repo,current,writer,trueLabel,falseLabel);
								switch (((FuncToStringNode)node).operands.get(0).getValueType()) {
									case INTEGER	:
										writer.append("	"+CompilerUtils.buildMethodCall(ME_TO_STRING_L)+"\n");
										break;
									case REAL		:
										writer.append("	"+CompilerUtils.buildMethodCall(ME_TO_STRING_D)+"\n");
										break;
									case STRING		:
										break;
									case BOOLEAN	:
										writer.append("	"+CompilerUtils.buildMethodCall(ME_TO_STRING_Z)+"\n");
										break;
									default : throw new UnsupportedOperationException("Data type ["+((FuncToStringNode)node).operands.get(0).getValueType()+"] is not supported yet");
								}
								break;
							case F_TO_BOOL 	:
								compileExpression(((FuncToStringNode)node).operands.get(0),storage,repo,current,writer,trueLabel,falseLabel);
								switch (((FuncToStringNode)node).operands.get(0).getValueType()) {
									case INTEGER : case REAL :
										throw new CalculationException("Conversion to boolean is not applicable for data type ["+node.getValueType()+"]");
									case STRING		:
										writer.append("	"+CompilerUtils.buildMethodCall(ME_TO_BOOLEAN)+"\n");
										break;
									case BOOLEAN	:
										break;
									default : throw new UnsupportedOperationException("Data type ["+((FuncToStringNode)node).operands.get(0).getValueType()+"] is not supported yet");
								}
								break;
							default : throw new UnsupportedOperationException("Expression operator ["+((OperatorListNode)node).getOperator()+"] is not supported yet");						
						}
					}
					break;
				default : throw new UnsupportedOperationException("Expression node ["+node.getType()+"] is not supported yet");
			}
		}
	}

	private static boolean hasConstantsOnly(final ExpressionNode node) {
		switch (node.getType()) {
			case CONSTANT		:
				return true;
			case EXPRESSION 	:
				for (ExpressionNode item : ((OperatorNode)node).getOperands()) {
					if (!hasConstantsOnly(item)) {
						return false;
					}
				}
				if ((node instanceof FuncNode) && ((FuncNode)node).getOperator() == ExpressionNodeOperator.F_UL) {
					return false;	// indeterministic function uniqueL()
				}
				else if ((node instanceof FuncNode) && ((FuncNode)node).getOperator() == ExpressionNodeOperator.F_UG) {
					return false;	// indeterministic function uniqueG()
				}
				else {
					return true;
				}
			case KEY_PARAMETER : case POSITIONAL_PARAMETER : case LOCAL_VARIABLE :
				return false;
			default : throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet");
		}
	}

	private static void buildConstant(final ExpressionNode node, final Storage storage, final AssemblerTemplateRepo repo, final NameKeeper callback, final GrowableCharArray writer, final int trueLabel, final int falseLabel) throws NullPointerException, CalculationException {
		switch (node.getValueType()) {
			case INTEGER	:
				writer.append(("	ldc2_w "+node.getLong()+"L\n"));
				break;
			case REAL		:
				writer.append(("	ldc2_w "+node.getDouble()+"\n"));
				break;
			case STRING		:
				final long	location = storage.allocateCharArray(node.getString());
				final int	from = (int)(location >>> 32), to = (int)(location & 0xFFFFFFFF);
				
				repo.append(writer,PART_EXTRACT_CHAR_CONST,callback.put(FROM_INDEX,from).put(TO_INDEX,to));
				break;
			case BOOLEAN	:
				if (trueLabel == 0 && falseLabel == 0) {
					writer.append((node.getBoolean() ? " iconst_1\n" : " iconst_0\n"));
				}
				else if (node.getBoolean()) {
					if (trueLabel != 0) {
						writer.append((" goto label"+trueLabel+"\n"));
					}
				}
				else {
					if (falseLabel != 0) {
						writer.append((" goto label"+trueLabel+"\n"));
					}
				}
				break;
			default : throw new UnsupportedOperationException("Data type ["+node.getValueType()+"] is not supported yet");
		}
	}

	private static void buldComparison(final ExpressionNodeValue value, final ExpressionNodeOperator operator, final Storage storage, final AssemblerTemplateRepo repo, final NameKeeper callback, final GrowableCharArray writer, final int trueLabel, final int falseLabel) {
		final int	labelTrue = storage.uniqueLabel++, labelFalse = storage.uniqueLabel++;
		
		switch (value) {
			case INTEGER	:
				writer.append(" lcmp\n");
				break;
			case REAL		:
				writer.append(" dcmpg\n");
				break;
			case STRING		:
				writer.append("	"+CompilerUtils.buildMethodCall(ME_COMPARE_STRINGS)+"\n");
				break;
			case BOOLEAN	:
				writer.append(" isub\n");
				break;
			default : throw new UnsupportedOperationException("Data type ["+value+"] is not supported yet");
		}
		if (trueLabel == 0 && falseLabel == 0) {
			switch (operator) {
				case EQ : repo.append(writer,PART_COMPARISON,callback.put(SKIP_COMMAND,COMMAND_IFNE).put(LABEL_FALSE,labelFalse).put(LABEL_TRUE,labelTrue)); break;									
				case NE : repo.append(writer,PART_COMPARISON,callback.put(SKIP_COMMAND,COMMAND_IFEQ).put(LABEL_FALSE,labelFalse).put(LABEL_TRUE,labelTrue)); break;
				case GE : repo.append(writer,PART_COMPARISON,callback.put(SKIP_COMMAND,COMMAND_IFLT).put(LABEL_FALSE,labelFalse).put(LABEL_TRUE,labelTrue)); break;
				case GT : repo.append(writer,PART_COMPARISON,callback.put(SKIP_COMMAND,COMMAND_IFLE).put(LABEL_FALSE,labelFalse).put(LABEL_TRUE,labelTrue)); break;
				case LE : repo.append(writer,PART_COMPARISON,callback.put(SKIP_COMMAND,COMMAND_IFGT).put(LABEL_FALSE,labelFalse).put(LABEL_TRUE,labelTrue)); break;
				case LT : repo.append(writer,PART_COMPARISON,callback.put(SKIP_COMMAND,COMMAND_IFGE).put(LABEL_FALSE,labelFalse).put(LABEL_TRUE,labelTrue)); break;
				default : throw new UnsupportedOperationException("Operator type ["+operator+"] is not supported yet");
			}
		}
		else {
			if (trueLabel != 0) {
				switch (operator) {
					case EQ : writer.append(("	ifeq label"+trueLabel+"\n")); break;									
					case NE : writer.append(("	ifne label"+trueLabel+"\n")); break;
					case GE : writer.append(("	ifge label"+trueLabel+"\n")); break;
					case GT : writer.append(("	ifgt label"+trueLabel+"\n")); break;
					case LE : writer.append(("	ifle label"+trueLabel+"\n")); break;
					case LT : writer.append(("	iflt label"+trueLabel+"\n")); break;
					default : throw new UnsupportedOperationException("Operator type ["+operator+"] is not supported yet");
				}
				if (falseLabel != 0) {
					writer.append(("	goto label"+falseLabel+"\n"));
				}
			}
			else {
				switch (operator) {
					case EQ : writer.append(("	ifne label"+falseLabel+"\n")); break;
					case NE : writer.append(("	ifeq label"+falseLabel+"\n")); break;
					case GE : writer.append(("	iflt label"+falseLabel+"\n")); break;
					case GT : writer.append(("	ifle label"+falseLabel+"\n")); break;
					case LE : writer.append(("	ifgt label"+falseLabel+"\n")); break;
					case LT : writer.append(("	ifge label"+falseLabel+"\n")); break;
					default : throw new UnsupportedOperationException("Operator type ["+operator+"] is not supported yet");
				}
			}
		}
	}

	static class Storage {
		final GrowableCharArray		stringRepo;
		int							uniqueLabel = 1;
		
		Storage(final GrowableCharArray stringRepo) {
			this.stringRepo = stringRepo;
		}
		
		long allocateCharArray(final char[] data) {
			final long	start = stringRepo.length();
			
			stringRepo.append(data);
			return (start << 32) | stringRepo.length();
		}
	}
	
	private static class JumpStack {
		final JumpStack		parent;
		final int			exitLabel;
		final int			breakLabel;
		final int			continueLabel;
		final CommandType	type;
		final char[]		label;

		public JumpStack(final int exitLabel, final int breakLabel, final int continueLabel, final CommandType type) {
			this(exitLabel,breakLabel,continueLabel,type,null);
		}
		
		public JumpStack(final int exitLabel, final int breakLabel, final int continueLabel, final CommandType type, final char[] label) {
			this(null,exitLabel,breakLabel,continueLabel,type,label);
		}

		public JumpStack(final JumpStack parent, final int exitLabel, final int breakLabel, final int continueLabel, final CommandType type) {
			this(parent,exitLabel,breakLabel,continueLabel,type,null);
		}
		
		public JumpStack(final JumpStack parent, final int exitLabel, final int breakLabel, final int continueLabel, final CommandType type, final char[] label) {
			this.parent = parent;
			this.exitLabel = exitLabel;
			this.breakLabel = breakLabel;
			this.continueLabel = continueLabel;
			this.type = type;
			this.label = label;
		}

		@Override
		public String toString() {
			return "JumpStack [parent=" + parent + ", exitLabel=" + exitLabel + ", breakLabel=" + breakLabel + ", continueLabel=" + continueLabel + ", type=" + type + ", label=" + Arrays.toString(label) + "]";
		}
	}
}
