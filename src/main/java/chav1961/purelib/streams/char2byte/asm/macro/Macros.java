package chav1961.purelib.streams.char2byte.asm.macro;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.char2byte.asm.ExpressionNodeType;
import chav1961.purelib.streams.char2byte.asm.MacroExecutorInterface;

// 			Syntax of the macros:
//	Name	.macro		&positional,...,&key=[defaultValue],...
//	&Var	.local		[initialValue]
//	&Var	.set		expression
//			.if			expression
//			.elseif 	expression
//			.else		expression
//			.end
//	Label:	.while		expression
//			.end
//	Label:	.for		&parameter = initial to expression [step expression]
//			.end
//			.break		[Label]
//			.continue	[Label]
//			.choise		expression
//			.of			expression
//			.otherwise
//			.end
//			.merror		expression
//			.exit
//	Name	.mend
//
//			Functions:
//	uniqueL()		- locally unique number
//	uniqueG()		- globally unique number. Doesn't change inside current macro call
//	exists(...)		- true if parameter value exists
//	int(...)		- convert value to integer
//	real(...)		- convert value to real
//	str(...)		- convert value to string
//	bool(...)		- convert value to boolean
//
//			Operators:
//	+, - , *, /, % - arithmetical operations
//  # - concatenation
//	==, >=, <=, !=, >, < - comparison operations
////	=[value,from..to,...] - in list
////	~= - match template
//	? : - ternary operation
//	!, &&, || - logical operators
//

public class Macros implements LineByLineProcessorCallback, Closeable {
	private static final SyntaxTreeInterface<Command>	COMMANDS = new AndOrTree<>();
	
	private static final int	CMD_MACRO = 0;
	private static final int	CMD_LOCAL = 1;
	private static final int	CMD_SET = 2;
	private static final int	CMD_IF = 3;
	private static final int	CMD_ELSEIF = 4;
	private static final int	CMD_ELSE = 5;
	private static final int	CMD_ENDIF = 6;
	private static final int	CMD_WHILE = 7;
	private static final int	CMD_ENDWHILE = 8;
	private static final int	CMD_FOR = 9;
	private static final int	CMD_ENDFOR = 10;
	private static final int	CMD_IN = 11;
	private static final int	CMD_TO = 12;
	private static final int	CMD_STEP = 13;
	private static final int	CMD_FOR_ALL = 14;
	private static final int	CMD_ENDFOR_ALL = 15;
	private static final int	CMD_BREAK = 16;
	private static final int	CMD_CONTINUE = 17;
	private static final int	CMD_CHOISE = 18;
	private static final int	CMD_ENDCHOISE = 19;
	private static final int	CMD_OF = 20;
	private static final int	CMD_OTHERWISE = 21;
	private static final int	CMD_ERROR = 22;
	private static final int	CMD_EXIT = 23;
	private static final int	CMD_MEND = 24;
	
	private static final int	FSM_BEFORE_MACRO = 0; 
	private static final int	FSM_DECLARATIONS = 1; 
	private static final int	FSM_IN_CODE = 2; 
	private static final int	FSM_AFTER_MACRO = 3; 
	
	static {
		COMMANDS.placeName(".break",CMD_BREAK,null);
		COMMANDS.placeName(".choise",CMD_CHOISE,null);
		COMMANDS.placeName(".continue",CMD_CONTINUE,null);
		COMMANDS.placeName(".else",CMD_ELSE,null);
		COMMANDS.placeName(".elseif",CMD_ELSEIF,null);
		COMMANDS.placeName(".endchoise",CMD_ENDCHOISE,null);
		COMMANDS.placeName(".endfor",CMD_ENDFOR,null);
		COMMANDS.placeName(".endforall",CMD_ENDFOR_ALL,null);
		COMMANDS.placeName(".endif",CMD_ENDIF,null);
		COMMANDS.placeName(".endwhile",CMD_ENDWHILE,null);
		COMMANDS.placeName(".exit",CMD_EXIT,null);
		COMMANDS.placeName(".for",CMD_FOR,null);
		COMMANDS.placeName(".forall",CMD_FOR_ALL,null);
		COMMANDS.placeName(".if",CMD_IF,null);
		COMMANDS.placeName("in",CMD_IN,null);
		COMMANDS.placeName(".macro",CMD_MACRO,null);
		COMMANDS.placeName(".mend",CMD_MEND,null);
		COMMANDS.placeName(".error",CMD_ERROR,null);
		COMMANDS.placeName(".local",CMD_LOCAL,null);
		COMMANDS.placeName(".of",CMD_OF,null);
		COMMANDS.placeName(".otherwise",CMD_OTHERWISE,null);
		COMMANDS.placeName(".set",CMD_SET,null);
		COMMANDS.placeName("step",CMD_STEP,null);
		COMMANDS.placeName("to",CMD_TO,null);
		COMMANDS.placeName(".while",CMD_WHILE,null);
	}
	
	private Command[]				stack = new Command[16];
	private int						stackTop = -1;
	private int						fsmState = FSM_BEFORE_MACRO;
	private MacroCommand			root = null;
	private MacroExecutorInterface	exec;
	
	public Macros() {
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void processLine(final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final int		to = from+length, begin = from;
		char[]			name;
		int				start = from;
		boolean			isLabel = false, hasName = false;
		
		if (data[from] > ' ') {	// Process name (the same first column is non-blank)
			final int[]	bounds = new int[2];
			
			try{from = CharUtils.parseName(data,from,bounds);
				from = InternalUtils.skipBlank(data,from);
				hasName = true;
				if (data[from] == ':') {
					isLabel = true;
					from = InternalUtils.skipBlank(data,from+1);
				}
				name = Arrays.copyOfRange(data,bounds[0],bounds[1]+1);
			} catch (IllegalArgumentException exc) {
				name = null;
			}
		}
		else {
			from = start = InternalUtils.skipBlank(data,from);
			name = null;
		}
		
		try{start = from;
			from = InternalUtils.skipNonBlank(data,from);
			if (from == start) {
				if (fsmState == FSM_DECLARATIONS || fsmState == FSM_IN_CODE) {
					if (fsmState == FSM_DECLARATIONS) {
						((MacroCommand)stack[0]).commitDeclarations();
					}
					stack[stackTop].append(lineNo,new SubstitutionCommand().processCommand(lineNo,begin,data,begin,to,(MacroCommand)stack[0]));
					fsmState = FSM_IN_CODE;
				}
				else {
					throw new SyntaxException(lineNo,from-begin,"macro string outside .macro"); 
				}
			}
			else {
				switch ((int)COMMANDS.seekName(data,start,from)) {
					case CMD_MACRO		:
						if (fsmState == FSM_BEFORE_MACRO) {
							if (!hasName) {
								throw new SyntaxException(lineNo,from-begin,".macro doesn't have name. Name must start from the same first position in the line!"); 
							}
							else if (isLabel) {
								throw new SyntaxException(lineNo,from-begin,".macro has label instead of name! Remove (:)"); 
							}
							else {
								push(new MacroCommand(name).processCommand(lineNo,begin,data,from,to,null));
								fsmState = FSM_DECLARATIONS;
							}
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".macro inside .macro! Possibly .mend was loosed?"); 
						}
						break;
					case CMD_LOCAL		:
						if (fsmState == FSM_DECLARATIONS) {
							if (!hasName) {
								throw new SyntaxException(lineNo,from-begin,".local doesn't have name. Name must start from the same first position in the line!"); 
							}
							else if (isLabel) {
								throw new SyntaxException(lineNo,from-begin,".local has label instead of name! Remove (:)"); 
							}
							else if (stack[stackTop].getType() != CommandType.MACRO) {
								throw new SyntaxException(lineNo,from-begin,".local can't be used inside nested operators!");
							}
							else {
								final int	bounds[] = new int[2];
								
								from = InternalUtils.skipBlank(data,CharUtils.parseName(data,InternalUtils.skipBlank(data,from),bounds));
								if (data[from] == '=') {
									final ExpressionNode[]			value = new ExpressionNode[1];
									final AssignableExpressionNode	var = new LocalVariable(name,InternalUtils.defineType(data,bounds));
									
									InternalUtils.parseExpression(InternalUtils.ORDER_OR,lineNo,data,begin,from+1,(MacroCommand)stack[0],value);
									setInitialValue(var,value[0]);
									((MacroCommand)stack[stackTop]).addDeclaration(var);
								}
								else {
									((MacroCommand)stack[stackTop]).addDeclaration(new LocalVariable(name,InternalUtils.defineType(data,bounds)));
								}
							}
						}
						else {
							throw new SyntaxException(lineNo,from-begin,"all .local declarations must immediately follow .macro entity"); 
						}
						break;
					case CMD_SET		:
						if (fsmState == FSM_DECLARATIONS || fsmState == FSM_IN_CODE) {
							final AssignableExpressionNode	leftPart;
							
							if ((leftPart = ((MacroCommand)stack[0]).seekDeclaration(name)) == null) {
								throw new SyntaxException(lineNo,from-begin,"Undeclared left name in the .set operator."); 
							}
							else {
								if (fsmState == FSM_DECLARATIONS) {
									((MacroCommand)stack[0]).commitDeclarations();
								}
								stack[stackTop].append(lineNo,new SetCommand(leftPart).processCommand(lineNo,begin,data,from,to,(MacroCommand)stack[0]));
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".set operator outside .macro"); 
						}
						break;
					case CMD_IF			:
						if (fsmState == FSM_DECLARATIONS || fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".if doesn't use with name/label"); 
							}
							else {
								if (fsmState == FSM_DECLARATIONS) {
									((MacroCommand)stack[0]).commitDeclarations();
								}
								final Command	ifCond = new IfConditionCommand().processCommand(lineNo,begin,data,from,to,(MacroCommand)stack[0]); 
								
								push(new IfContainer());
								push(ifCond);
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".if operator outside .macro"); 
						}
						break;
					case CMD_ELSEIF		:
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".elseif doesn't use with name/label"); 
							}
							else if (!(stack[stackTop] instanceof IfConditionCommand)) {
								throw new SyntaxException(lineNo,from-begin,".elseif without .if"); 
							}
							else {
								final Command	ifCmd = new IfConditionCommand().processCommand(lineNo,begin,data,from,to,(MacroCommand)stack[0]); 
								
								((IfContainer)stack[stackTop-1]).append(lineNo,pop());
								push(ifCmd);
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".elseif operator outside context"); 
						}
						break;
					case CMD_ELSE		:		
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".else doesn't use with name/label"); 
							}
							else if (!(stack[stackTop] instanceof IfConditionCommand)) {
								throw new SyntaxException(lineNo,from-begin,".elseif without .if"); 
							}
							else {
								((IfContainer)stack[stackTop-1]).append(lineNo,pop());
								push(new ElseCommand());
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".else operator outside context"); 
						}
						break;
					case CMD_ENDIF		:
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".endif doesn't use with name/label"); 
							}
							else if (((stack[stackTop] instanceof IfConditionCommand) || (stack[stackTop] instanceof ElseCommand))&& (stack[stackTop-1] instanceof IfContainer)) {
								stack[stackTop-1].append(lineNo,pop());
								stack[stackTop-1].append(lineNo,pop());
							}
							else if (stack[stackTop] instanceof ForCommand) {
								stack[stackTop-1].append(lineNo,pop());
							}
							else {
								throw new SyntaxException(lineNo,from-begin,".endif operator outside any context"); 
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".endif operator outside any context"); 
						}
						break;
					case CMD_WHILE		:
						if (fsmState == FSM_DECLARATIONS || fsmState == FSM_IN_CODE) {
							if (hasName && !isLabel) {
								throw new SyntaxException(lineNo,from-begin,".while doesn't use with name. Possibly label?"); 
							}
							else if (hasName && isLabel) {
								if (fsmState == FSM_DECLARATIONS) {
									((MacroCommand)stack[0]).commitDeclarations();
								}
								push(new WhileCommand(name).processCommand(lineNo,begin,data,from,to,(MacroCommand)stack[0]));
							}
							else {
								if (fsmState == FSM_DECLARATIONS) {
									((MacroCommand)stack[0]).commitDeclarations();
								}
								push(new WhileCommand().processCommand(lineNo,begin,data,from,to,(MacroCommand)stack[0]));
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".while operator outside .macro"); 
						}
						break;
					case CMD_ENDWHILE		:
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".endwhile doesn't use with name/label"); 
							}
							else if (stack[stackTop] instanceof WhileCommand) {
								stack[stackTop-1].append(lineNo,pop());
							}
							else {
								throw new SyntaxException(lineNo,from-begin,".endwhile operator outside any context"); 
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".endwhile operator outside any context"); 
						}
						break;
					case CMD_FOR		:
						if (fsmState == FSM_DECLARATIONS || fsmState == FSM_IN_CODE) {
							if (hasName && !isLabel) {
								throw new SyntaxException(lineNo,from-begin,".for doesn't use with name. Possibly label?"); 
							}
							else if (hasName && isLabel) {
								if (fsmState == FSM_DECLARATIONS) {
									((MacroCommand)stack[0]).commitDeclarations();
								}
								push(new ForCommand(name).processCommand(lineNo,begin,data,from,to,(MacroCommand)stack[0]));
							}
							else {
								if (fsmState == FSM_DECLARATIONS) {
									((MacroCommand)stack[0]).commitDeclarations();
								}
								push(new ForCommand().processCommand(lineNo,begin,data,from,to,(MacroCommand)stack[0]));
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".for operator outside .macro"); 
						}
						break;
					case CMD_ENDFOR		:
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".endfor doesn't use with name/label"); 
							}
							else if (stack[stackTop] instanceof ForCommand) {
								stack[stackTop-1].append(lineNo,pop());
							}
							else {
								throw new SyntaxException(lineNo,from-begin,".endfor operator outside any context"); 
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".endfor operator outside any context"); 
						}
						break;
					case CMD_FOR_ALL	:
						if (fsmState == FSM_DECLARATIONS || fsmState == FSM_IN_CODE) {
							if (hasName && !isLabel) {
								throw new SyntaxException(lineNo,from-begin,".for doesn't use with name. Possibly label?"); 
							}
							else if (hasName && isLabel) {
								if (fsmState == FSM_DECLARATIONS) {
									((MacroCommand)stack[0]).commitDeclarations();
								}
								push(new ForCommand(name).processCommand(lineNo,begin,data,from,to,(MacroCommand)stack[0]));
							}
							else {
								if (fsmState == FSM_DECLARATIONS) {
									((MacroCommand)stack[0]).commitDeclarations();
								}
								push(new ForEachCommand().processCommand(lineNo,begin,data,from,to,(MacroCommand)stack[0]));
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".for operator outside .macro"); 
						}
						break;
					case CMD_ENDFOR_ALL	:
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".endforall doesn't use with name/label"); 
							}
							else if (stack[stackTop] instanceof ForEachCommand) {
								stack[stackTop-1].append(lineNo,pop());
							}
							else {
								throw new SyntaxException(lineNo,from-begin,".endforall operator outside any context"); 
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".endforall operator outside any context"); 
						}
						break;
					case CMD_BREAK		:
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".break doesn't use with name/label"); 
							}
							else {
								final BreakCommand	cmd = new BreakCommand().processCommand(lineNo,begin,data, InternalUtils.skipBlank(data,from),to,(MacroCommand)stack[0]);
								
								if (cmd.getLabel() != null && !seekLabel(cmd.getLabel())) {
									throw new SyntaxException(lineNo,from-begin,".break: label referenced not found anywhere"); 
								}
								boolean	found = false;
								
								for (int index = stackTop; index > 0; index--) {
									if (stack[index] instanceof LoopCommand) {
										found = true;
										stack[stackTop].append(lineNo,cmd);
										break;
									}
								}
								if (!found) {
									throw new SyntaxException(lineNo,from-begin,".break outside the loop"); 
								}
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".break operator outside context"); 
						}
						break;
					case CMD_CONTINUE	:
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".continue doesn't use with name/label"); 
							}
							else {
								final ContinueCommand	cmd = new ContinueCommand().processCommand(lineNo,begin,data, InternalUtils.skipBlank(data,from),to,(MacroCommand)stack[0]);
								
								if (cmd.getLabel() != null && !seekLabel(cmd.getLabel())) {
									throw new SyntaxException(lineNo,from-begin,".continue: label referenced not found anywhere"); 
								}
								boolean	found = false;
								
								for (int index = stackTop; index > 0; index--) {
									if (stack[index] instanceof LoopCommand) {
										found = true;
										stack[stackTop].append(lineNo,cmd);
										break;
									}
								}
								if (!found) {
									throw new SyntaxException(lineNo,from-begin,".break outside the loop"); 
								}
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".continue operator outside context"); 
						}
						break;
					case CMD_CHOISE		:
						if (fsmState == FSM_DECLARATIONS || fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".choise doesn't use with name/label"); 
							}
							else {
								if (fsmState == FSM_DECLARATIONS) {
									((MacroCommand)stack[0]).commitDeclarations();
								}
								push(new ChoiseContainer().processCommand(lineNo,begin,data,InternalUtils.skipBlank(data,from),to,(MacroCommand)stack[0]));
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".choise operator outside .macro"); 
						}
						break;
					case CMD_ENDCHOISE		:
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".endchoise doesn't use with name/label"); 
							}
							if (((stack[stackTop] instanceof ChoiseConditionCommand) || (stack[stackTop] instanceof OtherwiseCommand))&& (stack[stackTop-1] instanceof ChoiseContainer)) {
								stack[stackTop-1].append(lineNo,pop());
								stack[stackTop-1].append(lineNo,pop());
							}
							else {
								throw new SyntaxException(lineNo,from-begin,".endchoise operator outside any context"); 
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".endchoise operator outside any context"); 
						}
						break;
					case CMD_OF			:
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".of doesn't use with name/label"); 
							}
							else if (stack[stackTop] instanceof ChoiseContainer) {
								final Command	cmd = new ChoiseConditionCommand().processCommand(lineNo,begin,data,InternalUtils.skipBlank(data,from),to,(MacroCommand)stack[0]); 
								
								push(cmd);
							}
							else if (stack[stackTop] instanceof ChoiseConditionCommand) {
								final ChoiseConditionCommand	cmd = new ChoiseConditionCommand().processCommand(lineNo,begin,data,InternalUtils.skipBlank(data,from),to,(MacroCommand)stack[0]); 

								if (((ChoiseContainer)stack[stackTop-1]).expr[0].getValueType() != cmd.value[0].getValueType()) {
									throw new SyntaxException(lineNo,from-begin,"Value type in the .of clause is differ than value type of the .choise expression! Use convert functions!"); 
								}
								else {
									stack[stackTop-1].append(lineNo,pop());
									push(cmd);
								}
							}
							else {
								throw new SyntaxException(lineNo,from-begin,".of without .choise"); 
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".of operator outside context"); 
						}
						break;
					case CMD_OTHERWISE	:
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".otherwise doesn't use with name/label"); 
							}
							else if (!(stack[stackTop] instanceof ChoiseConditionCommand)) {
								throw new SyntaxException(lineNo,from-begin,".otherwise without .choise"); 
							}
							else {
								((ChoiseContainer)stack[stackTop-1]).append(lineNo,pop());
								push(new OtherwiseCommand());
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".otherwise operator outside context"); 
						}
						break;
					case CMD_ERROR		:
						if (fsmState == FSM_DECLARATIONS || fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".merror doesn't use with name/label"); 
							}
							else {
								if (fsmState == FSM_DECLARATIONS) {
									((MacroCommand)stack[0]).commitDeclarations();
								}
								stack[stackTop].append(lineNo,new MErrorCommand().processCommand(lineNo,begin,data,InternalUtils.skipBlank(data,from),to,(MacroCommand)stack[0]));
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".merror operator outside context"); 
						}
						break;
					case CMD_EXIT		:
						if (fsmState == FSM_DECLARATIONS || fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".exit doesn't use with name/label"); 
							}
							else {
								if (fsmState == FSM_DECLARATIONS) {
									((MacroCommand)stack[0]).commitDeclarations();
								}
								stack[stackTop].append(lineNo,new ExitCommand());
							}
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".exit operator outside context"); 
						}
						break;
					case CMD_MEND		:
						if (fsmState == FSM_IN_CODE) {
							if (hasName || isLabel) {
								throw new SyntaxException(lineNo,from-begin,".mend doesn't use with name/label"); 
							}
							else if ((stackTop == 0) && (stack[stackTop] instanceof MacroCommand)){
								root = (MacroCommand)pop();
							}
							else {
								throw new SyntaxException(lineNo,0,".mend: there are ["+stackTop+"] unclosed operators in the macro"); 
							}
							fsmState = FSM_AFTER_MACRO;
						}
						else if (fsmState == FSM_DECLARATIONS) {
							throw new SyntaxException(lineNo,from-begin,".mend: empty .macro body was detected"); 
						}
						else {
							throw new SyntaxException(lineNo,from-begin,".mend operator outside .macro"); 
						}
						break;
					default :	// String to substitute
						if (fsmState == FSM_DECLARATIONS || fsmState == FSM_IN_CODE) {
							if (fsmState == FSM_DECLARATIONS) {
								((MacroCommand)stack[0]).commitDeclarations();
							}
							stack[stackTop].append(lineNo,new SubstitutionCommand().processCommand(lineNo,begin,data,begin,to,(MacroCommand)stack[0]));
							fsmState = FSM_IN_CODE;
						}
						else {
							throw new SyntaxException(lineNo,from-begin,"macro string outside .macro"); 
						}
						break;
				}
			}
		} catch (IllegalArgumentException | CalculationException exc) {
			exc.printStackTrace();
			throw new SyntaxException(lineNo,from-begin,exc.getLocalizedMessage()); 
		}
	}

	public Reader processCall(final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final MacroCommand		cmd = parseCall(lineNo,data,from,length);
		final GrowableCharArray	gca = new GrowableCharArray(false);
		
		try{exec.exec(cmd.getDeclarations(),gca);
		} catch (CalculationException exc) {
			exc.printStackTrace();
			throw new IOException(exc.getLocalizedMessage()); 
		}
		return gca.getReader();
	}

	MacroCommand parseCall(final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final MacroCommand		cmd = root.clone();
		final ExpressionNode[]	val = new ExpressionNode[1]; 
		final int[]				bounds = new int[2];
		int						positional = 0;

		try{from--;
loop:		do {if ((from = InternalUtils.skipCallEntity(data,InternalUtils.skipBlank(data,from+1),bounds)) >= data.length) {
					break;
				}

				switch (data[from]) {
					case '\r' : case '\n' :
						if (bounds[1] >= bounds[0]) {
							if (positional >= root.getDeclarations().length) {
								throw new SyntaxException(lineNo,from,"Too many parameters in the macro call!");
							}
							else if (root.getDeclarations()[positional].getType() != ExpressionNodeType.POSITIONAL_PARAMETER) {
								throw new SyntaxException(lineNo,from,"Too many positional parameters in the macro call!");
							}
							else {
								InternalUtils.parseConstant(data,bounds[0],true,val);
								cmd.getDeclarations()[positional].assign(val[0]);
								positional++;
							}
						}
						break loop;
					case ',' :
						if (bounds[1] >= bounds[0]) {
							if (positional >= root.getDeclarations().length) {
								throw new SyntaxException(lineNo,from,"Too many parameters in the macro call!");
							}
							else if (root.getDeclarations()[positional].getType() != ExpressionNodeType.POSITIONAL_PARAMETER) {
								throw new SyntaxException(lineNo,from,"Too many positional parameters in the macro call!");
							}
							else {
								InternalUtils.parseConstant(data,bounds[0],true,val);
								cmd.getDeclarations()[positional].assign(val[0]);
								positional++;
							}
						}
						continue loop;
					case '=' :
						final char[]	keyName = Arrays.copyOfRange(data,bounds[0],bounds[1]+1);
						final AssignableExpressionNode	key = cmd.seekDeclaration(keyName);
						
						if (key != null && key.getType() == ExpressionNodeType.KEY_PARAMETER) {
							from = InternalUtils.parseConstant(data,InternalUtils.skipBlank(data,from+1),true,val);
							setInitialValue(key,val[0]);
						}
						else {
							throw new SyntaxException(lineNo,from,"Unknown key parameter ["+new String(keyName)+"]");
						}
						break;
					default :
						throw new SyntaxException(lineNo,from,"Illegal symbol in the macro call");
				}
				from = InternalUtils.skipBlank(data,from);
			} while (from < data.length && data[from] == ',');
		} catch (CalculationException | IllegalArgumentException exc) {
			throw new SyntaxException(lineNo,from,exc.getLocalizedMessage(),exc); 
		}
		
		return cmd;
	}
	
	public MacroCommand getRoot() {
		return root;
	}
	
	public void compile(final MacroExecutorInterface executor) {
		this.exec = executor;
	}
	
	public boolean isPrepared() {
		return root != null;
	}
	
	public char[] getName() {
		if (!isPrepared()) {
			throw new IllegalStateException("Attempt to get macro name for unprepared macro"); 
		}
		else {
			return root.getName();
		}
	}
	
	private void push(final Command command) {
		if (stackTop >= stack.length-1) {
			stack = Arrays.copyOf(stack,2*stack.length);
		}
		stack[++stackTop] = command; 
	}
	
	private Command pop() {
		return stack[stackTop--];
	}

	private boolean seekLabel(final char[] label) {
		for (int index = stackTop; index > 0; index--) {
			if (stack[index] instanceof LoopCommand) {
				if (CharUtils.compare(label,0,((LoopCommand)stack[index]).getLabel())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void setInitialValue(final AssignableExpressionNode node, final ExpressionNode value) throws CalculationException {
		switch (node.getValueType()) {
			case INTEGER	:
				switch (value.getValueType()) {
					case INTEGER	: node.assign(value); break;
					case REAL		: node.assign(new ConstantNode((long)value.getDouble())); break;
					case STRING		: node.assign(new ConstantNode(Long.valueOf(new String(value.getString())))); break;
					case BOOLEAN	: throw new CalculationException("Boolean substitution value can't be converted to ["+node.getValueType()+"]");
					default : throw new UnsupportedOperationException("Value ["+node.getValueType()+"] is not supported yet");						
				}
				break;
			case REAL		:
				switch (value.getValueType()) {
					case INTEGER	: node.assign(new ConstantNode((double)value.getLong())); break;
					case REAL		: node.assign(value); break;
					case STRING		: node.assign(new ConstantNode(Double.valueOf(new String(value.getString())))); break;
					case BOOLEAN	: throw new CalculationException("Boolean substitution value can't be converted to ["+node.getValueType()+"]");
					default : throw new UnsupportedOperationException("Value ["+node.getValueType()+"] is not supported yet");						
				}
				break;
			case STRING		:
				switch (value.getValueType()) {
					case INTEGER	: node.assign(new ConstantNode(String.valueOf(value.getLong()).toCharArray())); break;
					case REAL		: node.assign(new ConstantNode(String.valueOf(value.getDouble()).toCharArray())); break;
					case STRING		: node.assign(value); break;
					case BOOLEAN	: node.assign(new ConstantNode(String.valueOf(value.getBoolean()).toCharArray())); break;
					default : throw new UnsupportedOperationException("Value ["+node.getValueType()+"] is not supported yet");						
				}
				break;
			case BOOLEAN	:
				switch (value.getValueType()) {
					case INTEGER	: throw new CalculationException("Integer substitution value can't be converted to ["+node.getValueType()+"]");
					case REAL		: throw new CalculationException("Integer substitution value can't be converted to ["+node.getValueType()+"]");
					case STRING		: node.assign(new ConstantNode(Boolean.valueOf(new String(value.getString())))); break;
					case BOOLEAN	: node.assign(value); break;
					default : throw new UnsupportedOperationException("Value ["+node.getValueType()+"] is not supported yet");						
				}
				break;
			default : throw new UnsupportedOperationException("Value ["+node.getValueType()+"] is not supported yet");						
		}
	}
}
