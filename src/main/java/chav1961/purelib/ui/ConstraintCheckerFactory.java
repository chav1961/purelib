package chav1961.purelib.ui;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.GettersAndSettersFactory.BooleanGetterAndSetter;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.ui.interfacers.Constraint;
import chav1961.purelib.ui.interfacers.ConstraintChecker;

public class ConstraintCheckerFactory {
	private static final int	PRTY_OR = 8;
	private static final int	PRTY_AND = 7;
	private static final int	PRTY_NOT = 6;
	private static final int	PRTY_COMPARE = 5;
	private static final int	PRTY_CONCAT = 4;
	private static final int	PRTY_ADD = 3;
	private static final int	PRTY_MUL = 2;
	private static final int	PRTY_NEG = 1;
	private static final int	PRTY_TERM = 0;
	
	private enum ExprType {
		Const, Field, Comparison, And, Or, Concat, Addition, Multiplication, Negation, Not
	}
	
	private enum OperType {
		Add, Sub, Mul, Div, Rem, LL, LE, GT, GE, EQ, NE, 
	}
	
	public static <T> ConstraintChecker<T> buildChecker(final Class<T> instanceClass, final Constraint constraint) throws SyntaxException {
		if (instanceClass == null) {
			throw new NullPointerException("Instance class can't be null");
		}
		else if (constraint == null) {
			throw new NullPointerException("Constraint can't be null");
		}
		else {
			final char[]		expr = (constraint.value()+'\n').toCharArray();
			final SyntaxNode[]	node = new SyntaxNode[1];
					
			buildTree(PRTY_OR,expr,0,node);
			
			return new ConstraintChecker<T>() {
				@Override
				public Severity getSeverity() {
					return constraint.severity();
				}
				
				@Override
				public String getMessageId() {
					return constraint.messageId();
				}
				
				@Override
				public String getConstraintExpression() {
					return constraint.value();
				}
				
				@Override
				public boolean check(final T instance) {
					
					try{final Object 	result = calculate(instance,node[0]);
						
						return (result instanceof Boolean) ? ((Boolean)result).booleanValue() : false;
					} catch (ContentException e) {
						return false;
					}
				}
			};
		}
	}
	
	
	private static int buildTree(final int level, final char[] expr, final int from, final SyntaxNode[] result) throws SyntaxException {
		int	pos = from;
		// TODO Auto-generated method stub
		switch (level) {
			case PRTY_OR	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,result));
				if (expr[pos] == '|' && expr[pos+1] == '|') {
					final List<SyntaxNode>	collection = new ArrayList<>();
					
					collection.add(result[0]);
					do {pos = skipBlank(expr,buildTree(level-1,expr,pos+2,result));
						collection.add(result[0]);						
					} while (expr[pos] == '|' && expr[pos+1] == '|');
					result[0] = new SyntaxNode(ExprType.Or,0,null,collection.toArray(new SyntaxNode[collection.size()]));
				}
				break;
			case PRTY_AND	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,result));
				if (expr[pos] == '&' && expr[pos+1] == '&') {
					final List<SyntaxNode>	collection = new ArrayList<>();
					
					collection.add(result[0]);
					do {pos = skipBlank(expr,buildTree(level-1,expr,pos+2,result));
						collection.add(result[0]);						
					} while (expr[pos] == '&' && expr[pos+1] == '&');
					result[0] = new SyntaxNode(ExprType.And,0,null,collection.toArray(new SyntaxNode[collection.size()]));
				}
				break;
			case PRTY_NOT	:
				pos = skipBlank(expr,pos);
				if (expr[pos] == '!') {
					pos = skipBlank(expr,buildTree(level-1,expr,pos+1,result));
					result[0] = new SyntaxNode(ExprType.Not,0,null,result[0]);
				}
				else {
					pos = skipBlank(expr,buildTree(level-1,expr,pos,result));
				}
				break;
			case PRTY_COMPARE	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,result));
			case PRTY_CONCAT	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,result));
				if (expr[pos] == '#') {
					final List<SyntaxNode>	collection = new ArrayList<>();
					
					collection.add(result[0]);
					do {pos = skipBlank(expr,buildTree(level-1,expr,pos+1,result));
						collection.add(result[0]);						
					} while (expr[pos] == '#');
					result[0] = new SyntaxNode(ExprType.Concat,0,null,collection.toArray(new SyntaxNode[collection.size()]));
				}
				break;
			case PRTY_ADD	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,result));
				if (expr[pos] == '+' || expr[pos] == '-') {
					final List<SyntaxNode>	collection = new ArrayList<>();
					final StringBuilder		sb = new StringBuilder('+');
					
					collection.add(result[0]);
					do {pos = skipBlank(expr,buildTree(level-1,expr,pos+1,result));
						collection.add(result[0]);
						sb.append(expr[pos]);
					} while (expr[pos] == '+' || expr[pos] == '-');
					sb.setLength(sb.length()-1);
					result[0] = new SyntaxNode(ExprType.Addition,0,sb.toString().toCharArray(),collection.toArray(new SyntaxNode[collection.size()]));
				}
				break;
			case PRTY_MUL	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,result));
				if (expr[pos] == '*' || expr[pos] == '/' || expr[pos] == '%') {
					final List<SyntaxNode>	collection = new ArrayList<>();
					final StringBuilder		sb = new StringBuilder('*');
					
					collection.add(result[0]);
					do {pos = skipBlank(expr,buildTree(level-1,expr,pos+1,result));
						collection.add(result[0]);
						sb.append(expr[pos]);
					} while (expr[pos] == '*' || expr[pos] == '/' || expr[pos] == '%');
					sb.setLength(sb.length()-1);
					result[0] = new SyntaxNode(ExprType.Multiplication,0,sb.toString().toCharArray(),collection.toArray(new SyntaxNode[collection.size()]));
				}
				break;
			case PRTY_NEG	:
				pos = skipBlank(expr,pos);
				if (expr[pos] == '-') {
					pos = skipBlank(expr,buildTree(level-1,expr,pos+1,result));
					result[0] = new SyntaxNode(ExprType.Negation,0,null,result[0]);
				}
				else {
					pos = skipBlank(expr,buildTree(level-1,expr,pos,result));
				}
				break;
			case PRTY_TERM	:
				pos = skipBlank(expr,pos);
			default :
		}
		return pos;
	}

	private static int skipBlank(final char[] expr, int pos) {
		while (expr[pos] <= ' ' && expr[pos] != '\n') {
			pos++;
		}
		return pos;
	}
	
	private static <T> Object calculate(final T instance, final SyntaxNode node) throws ContentException {
		switch (node.expr) {
			case Addition			:
				final char[]	addOperators = (char[])node.cargo; 
				long			longAddition = 0;
				double			doubleAddition = 0.0;
				boolean			wasDoubleAddition = false;
				int				addIndex = 0;
				
				for (SyntaxNode item : node.children) {
					final Object	result = calculate(instance,item);
					
					if ((result instanceof Double) || (result instanceof Float)) {
						if (!wasDoubleAddition){
							wasDoubleAddition = true;
							doubleAddition = longAddition;
						}
					}
					if (wasDoubleAddition) {
						double	operand = Utils.extractDoubleValue(result);
						
						switch (addOperators[addIndex++]) {
							case '+' : doubleAddition += operand; break;	
							case '-' : doubleAddition -= operand; break;
						}
					}
					else {
						long	operand = Utils.extractLongValue(result);
						
						switch (addOperators[addIndex++]) {
							case '+' : longAddition += operand; break;	
							case '-' : longAddition -= operand; break;
						}
					}
				}
				return wasDoubleAddition ? doubleAddition : longAddition;
			case And				:
				boolean	andResult = true;
				
				for (SyntaxNode item : node.children) {
					final Object	result = calculate(instance,item);
					
					andResult &= (result instanceof Boolean) ? ((Boolean)result).booleanValue() : false; 
				}
				return andResult;
			case Comparison			:
				final Object	left = calculate(instance,node.children[0]), right = calculate(instance,node.children[0]);
				
				
				break;
			case Concat				:
				final StringBuilder	sb = new StringBuilder();
				
				for (SyntaxNode item : node.children) {
					sb.append(calculate(instance,item));
				}
				return sb.toString();
			case Const				:
				return node.cargo;
			case Field				:
				if (node.cargo instanceof BooleanGetterAndSetter) {
					return ((BooleanGetterAndSetter)node.cargo).get(instance);
				}
				else  {
					return 0;
				}
			case Multiplication		:
				final char[]	mulOperators = (char[])node.cargo; 
				long			longMultiplication = 1;
				double			doubleMultiplication = 1.0;
				boolean			wasDoubleMultiplication = false;
				int				mulIndex = 0;
				
				for (SyntaxNode item : node.children) {
					final Object	result = calculate(instance,item);
					
					if ((result instanceof Double) || (result instanceof Float)) {
						if (!wasDoubleMultiplication){
							wasDoubleMultiplication = true;
							doubleMultiplication = longMultiplication;
						}
					}
					if (wasDoubleMultiplication) {
						double	operand = Utils.extractDoubleValue(result);
						
						switch (mulOperators[mulIndex++]) {
							case '*' : doubleMultiplication *= operand; break;	
							case '/' : doubleMultiplication /= operand; break;
							case '%' : doubleMultiplication %= operand; break;
						}
					}
					else {
						long	operand = Utils.extractLongValue(result);
						
						switch (mulOperators[mulIndex++]) {
							case '*' : longMultiplication *= operand; break;	
							case '/' : longMultiplication /= operand; break;
							case '%' : longMultiplication %= operand; break;
						}
					}
				}
				return wasDoubleMultiplication ? doubleMultiplication : longMultiplication;
			case Negation			:
				final Object	negValue = calculate(instance,node.children[0]);
				
				if (negValue instanceof Double) {
					return -((Double)negValue).doubleValue();
				}
				break;
			case Not				:
				final Object	notValue = calculate(instance,node.children[0]);
				
				if (notValue instanceof Boolean) {
					return !((Boolean)notValue).booleanValue();
				}
				else {
					throw new ContentException();
				}
			case Or					:
				boolean	orResult = false;
				
				for (SyntaxNode item : node.children) {
					final Object	result = calculate(instance,item);
					
					orResult |= (result instanceof Boolean) ? ((Boolean)result).booleanValue() : false; 
				}
				return orResult;
			default : throw new UnsupportedOperationException("Node type ["+node.expr+"] is not supported yet");
		}
		return false;
	}

	private static class SyntaxNode {
		ExprType		expr;
		long			value;
		Object			cargo;
		SyntaxNode[]	children;
		
		public SyntaxNode(final ExprType expr, final long value, final Object cargo, final SyntaxNode... children) {
			this.expr = expr;
			this.value = value;
			this.cargo = cargo;
			this.children = children;
		}

		@Override
		public String toString() {
			return "SyntaxNode [expr=" + expr + ", value=" + value + ", cargo=" + cargo + ", children=" + Arrays.toString(children) + "]";
		}
	}
}
