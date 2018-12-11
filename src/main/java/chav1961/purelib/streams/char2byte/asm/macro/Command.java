package chav1961.purelib.streams.char2byte.asm.macro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.char2byte.asm.Asm;

abstract class Command {
	protected List<Command>	container = new ArrayList<>();
	
	protected Command() {}
	
	abstract CommandType getType();
	abstract Command processCommand(final int lineNo, final int begin, final char[] data, final int from, final int to, final MacroCommand macro) throws SyntaxException;
	
	Command append(final int lineNo, final Command command) throws SyntaxException {
		if (!container.isEmpty()) {
			final Command	prev = container.get(container.size()-1);
			
			if ((prev instanceof BreakCommand) || (prev instanceof ContinueCommand) || (prev instanceof ExitCommand) || (prev instanceof MErrorCommand)) {
				throw new SyntaxException(lineNo,0,"Dead code: previous command was .break, .continue, .merror or .exit");
			}
		}
		container.add(command);
		return this;
	}
}

class MacroCommand extends Command implements Cloneable {
	final int							uniqueG = Asm.AI.incrementAndGet();
	int									uniqueL = 1;
	
	private final char[]				name;
	private AssignableExpressionNode[]	memory = new AssignableExpressionNode[16];
	private int							current = -1;
	private boolean						committed = false;

	MacroCommand(final char[] name) {
		this.name = name;
	}
	
	@Override
	CommandType getType() {
		return CommandType.MACRO;
	}

	@Override
	MacroCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		final ExpressionNode[]	forValue = new ExpressionNode[1]; 
		final int				bounds[] = new int[2];
		boolean 				keyParameters = false;
		
		try{from = InternalUtils.skipBlank(data,from);
			if (Character.isJavaIdentifierStart(data[from])) {	// Parameters are presented!
				from--;
				do {from = InternalUtils.skipBlank(data,CharUtils.parseName(data,InternalUtils.skipBlank(data,from+1),bounds));
					if (data[from] == ':') {
						final char[]	name = Arrays.copyOfRange(data,bounds[0],bounds[1]+1);
						
						from = InternalUtils.skipBlank(data,CharUtils.parseName(data,InternalUtils.skipBlank(data,from+1),bounds));
						if (data[from] == '=') {
							keyParameters = true;
							from = InternalUtils.skipBlank(data,from+1);
							if (data[from] != '\n' && data[from] != ',') {
								from = InternalUtils.parseExpression(InternalUtils.ORDER_OR,lineNo,data,begin,from,this,forValue);
								addDeclaration(new KeyParameter(name,InternalUtils.defineType(data,bounds),forValue[0]));
							}
							else {
								addDeclaration(new KeyParameter(name,InternalUtils.defineType(data,bounds)));
							}
						}
						else if (keyParameters) {
							throw new IllegalArgumentException("Mix of positional and key parameters! All the key parameters must follow all the positional ones"); 
						}
						else {
							addDeclaration(new PositionalParameter(name,InternalUtils.defineType(data,bounds)));
						}
					}
					else {
						throw new SyntaxException(lineNo,from-begin,new String(data,from,to-from)+" "+"Missing (:)"); 
					}
					from = InternalUtils.skipBlank(data,from);
				} while (data[from] == ',');
			}
		} catch (IllegalArgumentException exc) {
			throw new SyntaxException(lineNo,from-begin,new String(data,from,to-from)+" "+exc.getLocalizedMessage()); 
		}
		return this;
	}
	
	char[] getName() {
		return name;
	}

	void addDeclaration(final AssignableExpressionNode item) throws SyntaxException {
		if (committed) {
			throw new IllegalStateException("Macro command declarations were committed already, any addititons are illegal");
		}
		else {
			for (int index = 0; index <= current; index++) {
				if (CharUtils.compare(item.getName(),0,memory[index].getName())) {
					throw new SyntaxException(0,0,"Duplicate parameter or local variable name ["+new String(item.getName())+"]"); 
				}
			}
			if (current >= memory.length - 1) {
				memory = Arrays.copyOf(memory,2*memory.length);
			}
			memory[++current] = item;
			item.setSequentialNumber(current);
		}
	}
	
	AssignableExpressionNode seekDeclaration(final char[] data) {
		for (int index = 0; index <= current; index++) {
			if (CharUtils.compare(data,0,memory[index].getName())) {
				 return memory[index];
			}
		}
		return null;
	}

	void commitDeclarations() {
		if (!committed) {
			memory = Arrays.copyOf(memory,current+1);
			committed = true;
		}
		else {
			throw new IllegalStateException("Macro command declarations were committed already");
		}
	}
	
	AssignableExpressionNode[] getDeclarations() {
		if (!committed) {
			throw new IllegalStateException("Attempt to get declarations from uncommitted MacroCommand instance"); 
		}
		else {
			return memory;
		}
	}
	
	@Override
	public MacroCommand clone() {
		if (!committed) {
			throw new IllegalStateException("Attempt to clone uncommitted MacroCommand instance"); 
		}
		else {
			final MacroCommand	clone = new MacroCommand(getName());
			
			clone.memory = memory.clone();
			clone.current = clone.memory.length-1;
			clone.committed = true;
			for (int index = 0, maxIndex = clone.memory.length; index < maxIndex; index++) {
				clone.memory[index] = clone.memory[index].clone(); 
			}
			return clone;
		}
	}

	@Override
	public String toString() {
		return "MacroCommand [uniqueG=" + uniqueG + ", name=" + Arrays.toString(name) + ", memory=" + Arrays.toString(memory) + ", current=" + current + ", committed=" + committed + "]";
	}
}

class SetCommand extends Command {
	final AssignableExpressionNode	leftPart;
	final ExpressionNode[]			rightPart = new ExpressionNode[1];
	
	SetCommand(final AssignableExpressionNode leftPart) {
		this.leftPart = leftPart;
	}

	@Override
	CommandType getType() {
		return CommandType.SET;
	}

	@Override
	SetCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		InternalUtils.parseExpression(InternalUtils.ORDER_OR,lineNo,data,begin,from,macro,rightPart);
		return this;
	}		
}

class IfContainer extends Command {
	@Override
	CommandType getType() {
		return CommandType.IF;
	}

	@Override
	IfContainer processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		return this;
	}
}

class IfConditionCommand extends Command {
	final ExpressionNode[]	cond = new ExpressionNode[1]; 
	
	@Override
	CommandType getType() {
		return CommandType.IF_CONDITION;
	}

	@Override
	IfConditionCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		from = InternalUtils.parseExpression(InternalUtils.ORDER_OR,lineNo,data,begin,from,macro,cond);
		if (cond[0].getValueType() != ExpressionNodeValue.BOOLEAN) {
			throw new SyntaxException(lineNo,from-begin,"if/elseif expression must return boolean value"); 
		}
		return this;
	}
}

class ElseCommand extends Command {
	@Override
	CommandType getType() {
		return CommandType.ELSE;
	}

	@Override
	ElseCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		return this;
	}
}

abstract class LoopCommand extends Command {
	abstract char[] getLabel();
}

class WhileCommand extends LoopCommand {
	private final char[]	label;
	final ExpressionNode[]	cond = new ExpressionNode[1]; 
	
	WhileCommand() {
		this.label = null;
	}

	WhileCommand(final char[] label) {
		this.label = label;
	}
	
	@Override
	CommandType getType() {
		return CommandType.WHILE;
	}

	@Override
	WhileCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		InternalUtils.parseExpression(InternalUtils.ORDER_OR,lineNo,data,begin,from,macro,cond);
		if (cond[0].getValueType() != ExpressionNodeValue.BOOLEAN) {
			throw new SyntaxException(lineNo,from-begin,"while expression must return boolean value"); 
		}
		return this;
	}

	@Override
	char[] getLabel() {
		return label;
	}
}

class ForCommand extends LoopCommand {
	private static final char[]	TO = "to ".toCharArray();
	private static final char[]	STEP = "step ".toCharArray();
	
	private final char[]		label;
	ExpressionNode[]			parameters = new ExpressionNode[4];
	
	ForCommand() {
		this.label = null;
	}

	ForCommand(final char[] label) {
		this.label = label;
	}
	
	@Override
	CommandType getType() {
		return CommandType.FOR;
	}

	@Override
	ForCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		final int[]		bounds = new int[2];

		try{from = CharUtils.parseName(data,InternalUtils.skipBlank(data,from),bounds);
		
			final char[]				varName = Arrays.copyOfRange(data,bounds[0],bounds[1]+1);
			AssignableExpressionNode	var;
			
			if ((var = macro.seekDeclaration(varName)) == null) {
				throw new SyntaxException(lineNo,from-begin,"Index variable ["+new String(varName)+"] is not declared");  
			}
			else {
				from = InternalUtils.skipBlank(data,from);
				
				if (data[from] != '=') {
					throw new SyntaxException(lineNo,from-begin,"Missing (=) !");  
				}
				else {
					from = InternalUtils.parseExpression(InternalUtils.ORDER_ADD,lineNo,data,begin,InternalUtils.skipBlank(data,from+1),macro,parameters);
					if (parameters[0].getValueType() != ExpressionNodeValue.INTEGER && parameters[0].getValueType() != ExpressionNodeValue.REAL) {
						throw new SyntaxException(lineNo,from-begin,"Initial value must be numeric!");  
					}
					parameters[1] = parameters[0];
					
					from = InternalUtils.skipBlank(data,from);
					if (!CharUtils.compare(data,from,TO)) {
						throw new SyntaxException(lineNo,from-begin,"Missing 'to'!");  
					}
					else {
						from += TO.length - 1;
					}
	
					from = InternalUtils.parseExpression(InternalUtils.ORDER_ADD,lineNo,data,begin,InternalUtils.skipBlank(data,from),macro,parameters);
					if (parameters[0].getValueType() != ExpressionNodeValue.INTEGER && parameters[0].getValueType() != ExpressionNodeValue.REAL) {
						throw new SyntaxException(lineNo,from-begin,"Terminal value must be numeric!");  
					}
					parameters[2] = parameters[0];
	
					from = InternalUtils.skipBlank(data,from);
					if (CharUtils.compare(data,from,STEP)) {
						from += STEP.length - 1;
						
						from = InternalUtils.parseExpression(InternalUtils.ORDER_ADD,lineNo,data,begin,InternalUtils.skipBlank(data,from),macro,parameters);
						if (parameters[0].getValueType() != ExpressionNodeValue.INTEGER && parameters[0].getValueType() != ExpressionNodeValue.REAL) {
							throw new SyntaxException(lineNo,from-begin,"Step must be numeric!");  
						}
						parameters[3] = parameters[0];
					}
					else if (data[from] == '\n') {
						parameters[3] = new ConstantNode(1);
					}
					else {
						throw new SyntaxException(lineNo,from-begin,"Unparsed tail in the operator!");  
					}
					parameters[0] = var;
					return this;
				}
			}
		} catch (IllegalArgumentException exc) {
			throw new SyntaxException(lineNo,from-begin,exc.getMessage()); 
		}
	}

	@Override
	char[] getLabel() {
		return label;
	}
}


class ForEachCommand extends LoopCommand {
	private static final char[]	IN = "in ".toCharArray();
	private static final char[]	SPLITTED = "splitted ".toCharArray();
	private static final char[]	BY = "by ".toCharArray();
	
	private final char[]		label;
	ExpressionNode[]			parameters = new ExpressionNode[3];
	
	ForEachCommand() {
		this.label = null;
	}

	ForEachCommand(final char[] label) {
		this.label = label;
	}
	
	@Override
	CommandType getType() {
		return CommandType.FOR_EACH;
	}

	@Override
	ForEachCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		final int[]		bounds = new int[2];

		try{from = CharUtils.parseName(data,InternalUtils.skipBlank(data,from),bounds);
		
			final char[]				varName = Arrays.copyOfRange(data,bounds[0],bounds[1]+1);
			AssignableExpressionNode	var;
			
			if ((var = macro.seekDeclaration(varName)) == null) {
				throw new SyntaxException(lineNo,from-begin,"Index variable ["+new String(varName)+"] is not declared");  
			}
			else {
				from = InternalUtils.skipBlank(data,from);
				if (!CharUtils.compare(data,from,IN)) {
					throw new SyntaxException(lineNo,from-begin,"Missing 'in'!");  
				}
				else {
					from += IN.length - 1;
				}
				
				from = InternalUtils.parseExpression(InternalUtils.ORDER_CAT,lineNo,data,begin,InternalUtils.skipBlank(data,from+1),macro,parameters);
				if (parameters[0].getValueType() != ExpressionNodeValue.STRING) {
					throw new SyntaxException(lineNo,from-begin,"Value to split must be string!");  
				}
				parameters[1] = parameters[0];

				from = InternalUtils.skipBlank(data,from);
				if (!CharUtils.compare(data,from,SPLITTED)) {
					throw new SyntaxException(lineNo,from-begin,"Missing 'splitted'!");  
				}
				else {
					from += SPLITTED.length - 1;
				}
				from = InternalUtils.skipBlank(data,from);
				if (!CharUtils.compare(data,from,BY)) {
					throw new SyntaxException(lineNo,from-begin,"Missing 'by'!");  
				}
				else {
					from += BY.length - 1;
				}
				
				from = InternalUtils.parseExpression(InternalUtils.ORDER_CAT,lineNo,data,begin,InternalUtils.skipBlank(data,from+1),macro,parameters);
				if (parameters[0].getValueType() != ExpressionNodeValue.STRING) {
					throw new SyntaxException(lineNo,from-begin,"Splitter value must be string!");  
				}
				parameters[2] = parameters[0];
				parameters[0] = var;
				return this;
			}
		} catch (IllegalArgumentException exc) {
			throw new SyntaxException(lineNo,from-begin,exc.getMessage()); 
		}
	}

	@Override
	char[] getLabel() {
		return label;
	}
}


abstract class ControlCommand extends Command {
	abstract char[] getLabel();
}

class BreakCommand extends ControlCommand {
	private char[]	label;
	
	BreakCommand() {
	}

	@Override
	CommandType getType() {
		return CommandType.BREAK;
	}

	@Override
	BreakCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		from = InternalUtils.skipBlank(data,from);
		
		if (Character.isJavaIdentifierStart(data[from])) {
			int	start = from;
			
			from = InternalUtils.skipNonBlank(data,from);
			label = Arrays.copyOfRange(data,start,from);
		}
		return this;
	}

	@Override
	char[] getLabel() {
		return label;
	}
}

class ContinueCommand extends ControlCommand {
	private char[]	label;
	
	ContinueCommand() {
	}

	@Override
	CommandType getType() {
		return CommandType.CONTINUE;
	}

	@Override
	ContinueCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		from = InternalUtils.skipBlank(data,from);
		
		if (Character.isJavaIdentifierStart(data[from])) {
			int	start = from;
			
			from = InternalUtils.skipNonBlank(data,from);
			label = Arrays.copyOfRange(data,start,from);
		}
		return this;
	}

	@Override
	char[] getLabel() {
		return label;
	}
}

class ChoiseContainer extends Command {
	final ExpressionNode[]	expr = new ExpressionNode[1];
	
	ChoiseContainer() {
		
	}

	@Override
	CommandType getType() {
		return CommandType.CHOISE;
	}

	@Override
	ChoiseContainer processCommand(final int lineNo, final int begin, char[] data, int from, int to, final MacroCommand macro) throws SyntaxException {
		InternalUtils.parseExpression(InternalUtils.ORDER_OR,lineNo,data,begin,from,macro,expr);
		return this;
	}
}

class ChoiseConditionCommand extends Command {
	final ExpressionNode[]	value = new ExpressionNode[1];
	
	@Override
	CommandType getType() {
		return CommandType.CHOISE_CONDITION;
	}

	@Override
	ChoiseConditionCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		InternalUtils.parseExpression(InternalUtils.ORDER_OR,lineNo,data,begin,from,macro,value);
		return this;
	}
}

class OtherwiseCommand extends Command {
	@Override
	CommandType getType() {
		return CommandType.OTHERWISE;
	}

	@Override
	OtherwiseCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		return this;
	}
}

class ExitCommand extends Command {
	@Override
	CommandType getType() {
		return CommandType.EXIT;
	}

	@Override
	ExitCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		return this;
	}
}

class MErrorCommand extends Command {
	final ExpressionNode[]	value = new ExpressionNode[1];
	
	@Override
	CommandType getType() {
		return CommandType.MERROR;
	}

	@Override
	MErrorCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		InternalUtils.parseExpression(InternalUtils.ORDER_OR,lineNo,data,begin,from,macro,value);
		if (value[0].getValueType() != ExpressionNodeValue.STRING) {
			throw new SyntaxException(lineNo,from-begin,"merror expression must have string value"); 
		}
		return this;
	}
}


// text&name1&name2.text&name3..text
class SubstitutionCommand extends Command {
	final CatNode[]	subst = new CatNode[1]; 
	
	@Override
	CommandType getType() {
		return CommandType.SUBSTITUTION;
	}

	@Override
	SubstitutionCommand processCommand(final int lineNo, final int begin, final char[] data, int from, final int to, final MacroCommand macro) throws SyntaxException {
		int				startText = from, bounds[] = new int[2];
		boolean			wereAnyAdditions = false;
		
		subst[0] = new CatNode();
		while (from < to && data[from] != '\r' && data[from] != '\n') {
			if (data[from] == '&') {
				if (from > startText) {
					subst[0].addOperand(new ConstantNode(Arrays.copyOfRange(data,startText,from)));
					wereAnyAdditions = true;
				}
				from = CharUtils.parseName(data,from+1,bounds);
				if (data[from] == '.') {
					from++;
				}
				startText = from;
				
				final char[]			varName = Arrays.copyOfRange(data,bounds[0],bounds[1]+1); 
				final ExpressionNode 	var = macro.seekDeclaration(varName);
				
				if (var == null) {
					throw new SyntaxException(lineNo,from-begin,"Substitution variable ["+new String(varName)+"] is not declared");  
				}
				else {
					switch (var.getValueType()) {
						case INTEGER : case REAL : case BOOLEAN	:
							subst[0].addOperand(new FuncToStringNode().addOperand(var));
							wereAnyAdditions = true;
							break;
						case STRING		:
							subst[0].addOperand(var);
							wereAnyAdditions = true;
							break;
						default : throw new UnsupportedOperationException("Value type ["+var.getValueType()+"] is ot supportd yet"); 
					}
				}
			}
			else {
				from++;
			}
		}
		if (from > startText) {
			subst[0].addOperand(new ConstantNode(Arrays.copyOfRange(data,startText,from+1)));
		}
		else if (wereAnyAdditions) {
			subst[0].addOperand(new ConstantNode(new char[]{'\n'}));
		}
		return this;
	}
}
