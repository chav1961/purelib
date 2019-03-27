package chav1961.purelib.ui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.BooleanGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ByteGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.CharGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.DoubleGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.FloatGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ShortGetterAndSetter;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.ui.interfaces.Constraint;
import chav1961.purelib.ui.interfaces.ConstraintChecker;

public class ConstraintCheckerFactory {
	static final int	PRTY_OR = 8;
	static final int	PRTY_AND = 7;
	static final int	PRTY_NOT = 6;
	static final int	PRTY_COMPARE = 5;
	static final int	PRTY_CONCAT = 4;
	static final int	PRTY_ADD = 3;
	static final int	PRTY_MUL = 2;
	static final int	PRTY_NEG = 1;
	static final int	PRTY_TERM = 0;

	private static final Map<Class<?>,Map<Class<?>,Class<?>>>	RESOLVER = new HashMap<>();
	
	static {
		Map<Class<?>,Class<?>>	toMapResolved = new HashMap<>();
		
		toMapResolved.put(Byte.class,Long.class);
		toMapResolved.put(Short.class,Long.class);
		toMapResolved.put(Integer.class,Long.class);
		toMapResolved.put(Long.class,Long.class);
		toMapResolved.put(Float.class,Double.class);
		toMapResolved.put(Double.class,Double.class);
		toMapResolved.put(String.class,String.class);
		
		RESOLVER.put(Byte.class,toMapResolved);
		RESOLVER.put(Short.class,toMapResolved);
		RESOLVER.put(Integer.class,toMapResolved);
		RESOLVER.put(Long.class,toMapResolved);
		RESOLVER.put(Float.class,toMapResolved); 
		RESOLVER.put(Double.class,toMapResolved);
		RESOLVER.put(String.class,toMapResolved);
	}
	
	@FunctionalInterface
	private interface Convertor {
		Object convert(Object source) throws ContentException;
	}
	
	enum ExprType {
		Const, Field, Comparison, And, Or, Concat, Addition, Multiplication, Negation, Not
	}
	
	enum OperType {
		LT, LE, GT, GE, EQ, NE, 
	}
	
	public static <T> ConstraintChecker<T> buildChecker(final Class<T> instanceClass, final Constraint constraint) throws SyntaxException {
		if (instanceClass == null) {
			throw new NullPointerException("Instance class can't be null");
		}
		else if (constraint == null) {
			throw new NullPointerException("Constraint can't be null");
		}
		else {
			final char[]	expr = (constraint.value()+'\n').toCharArray();
			final SyntaxNode<ExprType,?>[]	node = new SyntaxNode[1];
					
			buildTree(PRTY_OR,expr,0,instanceClass,node);
			
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
				public boolean check(final T instance) throws ContentException {
					final Object 	result = calculate(instance,node[0]);
						
					return (result instanceof Boolean) ? ((Boolean)result).booleanValue() : false;
				}
			};
		}
	}
	
	
	static int buildTree(final int level, final char[] expr, final int from, final Class<?> instClass, final SyntaxNode<ExprType,?>[] result) throws SyntaxException {
		int	pos = from;
		
		switch (level) {
			case PRTY_OR	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,instClass,result));
				if (expr[pos] == '|' && expr[pos+1] == '|') {
					final List<SyntaxNode<ExprType,?>>	collection = new ArrayList<>();
					
					collection.add(result[0]);
					do {pos = skipBlank(expr,buildTree(level-1,expr,pos+2,instClass,result));
						collection.add(result[0]);						
					} while (expr[pos] == '|' && expr[pos+1] == '|');
					result[0] = new SyntaxNode(0,pos,ExprType.Or,0,null,collection.toArray(new SyntaxNode[collection.size()]));
				}
				break;
			case PRTY_AND	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,instClass,result));
				if (expr[pos] == '&' && expr[pos+1] == '&') {
					final List<SyntaxNode<ExprType,?>>	collection = new ArrayList<>();
					
					collection.add(result[0]);
					do {pos = skipBlank(expr,buildTree(level-1,expr,pos+2,instClass,result));
						collection.add(result[0]);						
					} while (expr[pos] == '&' && expr[pos+1] == '&');
					result[0] = new SyntaxNode(0,pos,ExprType.And,0,null,collection.toArray(new SyntaxNode[collection.size()]));
				}
				break;
			case PRTY_NOT	:
				pos = skipBlank(expr,pos);
				if (expr[pos] == '!') {
					pos = skipBlank(expr,buildTree(level-1,expr,pos+1,instClass,result));
					result[0] = new SyntaxNode(0,pos,ExprType.Not,0,null,result[0]);
				}
				else {
					pos = skipBlank(expr,buildTree(level-1,expr,pos,instClass,result));
				}
				break;
			case PRTY_COMPARE	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,instClass,result));
				if (expr[pos] == '>' || expr[pos] == '<' || expr[pos] == '=' || expr[pos] == '!') {
					final SyntaxNode<ExprType,?>	left = result[0];
					OperType						oper = null;
					
					switch (expr[pos]) {
						case '>' :
							if (expr[pos+1] == '=') {
								oper = OperType.GE;
								pos += 2;
							}
							else {
								oper = OperType.GT;
								pos++;
							}
							break;
						case '<' :
							if (expr[pos+1] == '=') {
								oper = OperType.LE;
								pos += 2;
							}
							else {
								oper = OperType.LT;
								pos++;
							}
							break;
						case '=' :
							if (expr[pos+1] == '=') {
								oper = OperType.EQ;
								pos += 2;
							}
							else {
								throw new SyntaxException(0,pos, "");
							}
							break;
						case '!' :
							if (expr[pos+1] == '=') {
								oper = OperType.NE;
								pos += 2;
							}
							else {
								throw new SyntaxException(0,pos, "");
							}
							break;
					}
					pos = skipBlank(expr,buildTree(level-1,expr,pos,instClass,result));
					result[0] = new SyntaxNode(0,pos,ExprType.Comparison,0,oper,left,result[0]);
				}
				break;
			case PRTY_CONCAT	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,instClass,result));
				if (expr[pos] == '#') {
					final List<SyntaxNode<ExprType,?>>	collection = new ArrayList<>();
					
					collection.add(result[0]);
					do {pos = skipBlank(expr,buildTree(level-1,expr,pos+1,instClass,result));
						collection.add(result[0]);						
					} while (expr[pos] == '#');
					result[0] = new SyntaxNode(0,pos,ExprType.Concat,0,null,collection.toArray(new SyntaxNode[collection.size()]));
				}
				break;
			case PRTY_ADD	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,instClass,result));
				if (expr[pos] == '+' || expr[pos] == '-') {
					final List<SyntaxNode<ExprType,?>>	collection = new ArrayList<>();
					final StringBuilder					sb = new StringBuilder("+");
					
					collection.add(result[0]);
					do {sb.append(expr[pos]);
						pos = skipBlank(expr,buildTree(level-1,expr,pos+1,instClass,result));
						collection.add(result[0]);
					} while (expr[pos] == '+' || expr[pos] == '-');
					result[0] = new SyntaxNode(0,pos,ExprType.Addition,0,sb.toString().toCharArray(),collection.toArray(new SyntaxNode[collection.size()]));
				}
				break;
			case PRTY_MUL	:
				pos = skipBlank(expr,buildTree(level-1,expr,pos,instClass,result));
				if (expr[pos] == '*' || expr[pos] == '/' || expr[pos] == '%') {
					final List<SyntaxNode<ExprType,?>>	collection = new ArrayList<>();
					final StringBuilder					sb = new StringBuilder("*");
					
					collection.add(result[0]);
					do {sb.append(expr[pos]);
						pos = skipBlank(expr,buildTree(level-1,expr,pos+1,instClass,result));
						collection.add(result[0]);
					} while (expr[pos] == '*' || expr[pos] == '/' || expr[pos] == '%');
					result[0] = new SyntaxNode(0,pos,ExprType.Multiplication,0,sb.toString().toCharArray(),collection.toArray(new SyntaxNode[collection.size()]));
				}
				break;
			case PRTY_NEG	:
				pos = skipBlank(expr,pos);
				if (expr[pos] == '-') {
					pos = skipBlank(expr,buildTree(level-1,expr,pos+1,instClass,result));
					result[0] = new SyntaxNode(0,pos,ExprType.Negation,0,null,result[0]);
				}
				else {
					pos = skipBlank(expr,buildTree(level-1,expr,pos,instClass,result));
				}
				break;
			case PRTY_TERM	:
				pos = skipBlank(expr,pos);
				if (Character.isDigit(expr[pos])) {
					final long[]	values = new long[2];
					
					pos = CharUtils.parseNumber(expr,pos,values,CharUtils.PREF_ANY,true);
					switch ((int)values[1]) {
						case CharUtils.PREF_INT : case CharUtils.PREF_LONG :
							result[0] = new SyntaxNode(0,pos,ExprType.Const,0,Long.valueOf(values[0]));
							break;
						case CharUtils.PREF_FLOAT	:
							result[0] = new SyntaxNode(0,pos,ExprType.Const,0,Double.valueOf(Float.intBitsToFloat((int)values[0])));
							break;
						case CharUtils.PREF_DOUBLE	:
							result[0] = new SyntaxNode(0,pos,ExprType.Const,0,Double.valueOf(Double.longBitsToDouble(values[0])));
							break;
					}
				}
				else if (Character.isJavaIdentifierStart(expr[pos])) {
					final int[]		ranges = new int[2];
					
					pos = CharUtils.parseName(expr,pos,ranges);
					final String	name = new String(expr,ranges[0],ranges[1]-ranges[0]+1);
					
					if ("true".equals(name)) {
						result[0] = new SyntaxNode(0,pos,ExprType.Const,0,Boolean.valueOf(true));
					}
					else if ("false".equals(name)) {
						result[0] = new SyntaxNode(0,pos,ExprType.Const,0,Boolean.valueOf(false));
					}
					else {
						try{final GetterAndSetter	gas = GettersAndSettersFactory.buildGetterAndSetter(instClass,name);
						
							result[0] = new SyntaxNode(0,pos,ExprType.Field,0,gas);
						} catch (ContentException e) {
							throw new SyntaxException(0,pos,"");
						}
					}
				}
				else if (expr[pos] == '\"') {
					final int[]	ranges = new int[2];
					
					pos = CharUtils.parseUnescapedString(expr,pos+1,'\"',true,ranges);
					result[0] = new SyntaxNode(0,pos,ExprType.Const,0,new String(expr,ranges[0],ranges[1]-ranges[0]+1));
				}
				else if (expr[pos] == '(') {
					pos = skipBlank(expr,buildTree(PRTY_OR,expr,pos+1,instClass,result));
					if (expr[pos] == ')') {
						pos++;
					}
					else {
						throw new SyntaxException(0,pos,"");
					}
				}
				else {
					throw new SyntaxException(0,pos,""); 
				}
				break;
			default :
		}
		return pos;
	}

	static <T> Object calculate(final T instance, final SyntaxNode<ExprType,?> node) throws ContentException {
		switch (node.getType()) {
			case Addition			:
				final char[]	addOperators = (char[])node.cargo; 
				long			longAddition = 0;
				double			doubleAddition = 0.0;
				boolean			wasDoubleAddition = false;
				int				addIndex = 0;
				
				for (SyntaxNode<ExprType,?> item : node.children) {
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
				if (wasDoubleAddition) {
					return Double.valueOf(doubleAddition);
				}
				else {
					return Long.valueOf(longAddition);
				}
			case And				:
				boolean	andResult = true;
				
				for (SyntaxNode<ExprType,?> item : node.children) {
					final Object	result = calculate(instance,item);
					
					andResult &= (result instanceof Boolean) ? ((Boolean)result).booleanValue() : false; 
				}
				return andResult;
			case Comparison			:
				Object	left = calculate(instance,node.children[0]), right = calculate(instance,node.children[1]);
				
				if (left.getClass() != right.getClass()) {
					final Class<?>	commonClass = defineCommonClass(left.getClass(),right.getClass());
					left = convert2Class(left,commonClass);
					right = convert2Class(right,commonClass);
				}
				boolean	comparison = false;
				
				switch ((OperType)node.cargo) {
					case EQ		: 
						comparison = left.equals(right); 
						break;
					case GE		:
						if (left instanceof Comparable) {
							comparison = ((Comparable)left).compareTo(right) >= 0;
						}
						else {
							throw new ContentException();
						}
						break;
					case GT		:
						if (left instanceof Comparable) {
							comparison = ((Comparable)left).compareTo(right) > 0;
						}
						else {
							throw new ContentException();
						}
						break;
					case LE		:
						if (left instanceof Comparable) {
							comparison = ((Comparable)left).compareTo(right) <= 0;
						}
						else {
							throw new ContentException();
						}
						break;
					case LT		:
						if (left instanceof Comparable) {
							comparison = ((Comparable)left).compareTo(right) < 0;
						}
						else {
							throw new ContentException();
						}
						break;
					case NE		: 
						comparison = !left.equals(right); 
						break;
					default : throw new UnsupportedOperationException("Operation type ["+node.cargo+"] is not supported yet");
				}
				return comparison;
			case Concat				:
				final StringBuilder	sb = new StringBuilder();
				
				for (SyntaxNode<ExprType,?> item : node.children) {
					sb.append(calculate(instance,item));
				}
				return sb.toString();
			case Const				:
				return node.cargo;
			case Field				:
				if (node.cargo instanceof BooleanGetterAndSetter) {
					return ((BooleanGetterAndSetter)node.cargo).get(instance);
				}
				else if (node.cargo instanceof ByteGetterAndSetter) {
					return ((ByteGetterAndSetter)node.cargo).get(instance);
				}
				else if (node.cargo instanceof ShortGetterAndSetter) {
					return ((ShortGetterAndSetter)node.cargo).get(instance);
				}
				else if (node.cargo instanceof IntGetterAndSetter) {
					return ((IntGetterAndSetter)node.cargo).get(instance);
				}
				else if (node.cargo instanceof LongGetterAndSetter) {
					return ((LongGetterAndSetter)node.cargo).get(instance);
				}
				else if (node.cargo instanceof FloatGetterAndSetter) {
					return ((FloatGetterAndSetter)node.cargo).get(instance);
				}
				else if (node.cargo instanceof DoubleGetterAndSetter) {
					return ((DoubleGetterAndSetter)node.cargo).get(instance);
				}
				else if (node.cargo instanceof CharGetterAndSetter) {
					return ((CharGetterAndSetter)node.cargo).get(instance);
				}
				else  {
					return ((ObjectGetterAndSetter<?>)node.cargo).get(instance);
				}
			case Multiplication		:
				final char[]	mulOperators = (char[])node.cargo; 
				long			longMultiplication = 1;
				double			doubleMultiplication = 1.0;
				boolean			wasDoubleMultiplication = false;
				int				mulIndex = 0;
				
				for (SyntaxNode<ExprType,?> item : node.children) {
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
				if (wasDoubleMultiplication) {
					return Double.valueOf(doubleMultiplication);
				}
				else {
					return Long.valueOf(longMultiplication);
				}
			case Negation			:
				final Object	negValue = calculate(instance,node.children[0]);
				
				if ((negValue instanceof Double) || (negValue instanceof Float)) {
					return -convert2Class(negValue,Double.class);
				}
				else {
					return -convert2Class(negValue,Long.class);
				}
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
				
				for (SyntaxNode<ExprType,?> item : node.children) {
					final Object	result = calculate(instance,item);
					
					orResult |= (result instanceof Boolean) ? ((Boolean)result).booleanValue() : false; 
				}
				return orResult;
			default : throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet");
		}
	}

	private static int skipBlank(final char[] expr, int pos) {
		while (expr[pos] <= ' ' && expr[pos] != '\n') {
			pos++;
		}
		return pos;
	}
	
	private static Class<?> defineCommonClass(final Class<?> class1, final Class<?> class2) throws ContentException {
		if (RESOLVER.containsKey(class1)) {
			if (RESOLVER.get(class1).containsKey(class2)) {
				return RESOLVER.get(class1).get(class2);
			}
		}
		throw new ContentException("Classes ["+class1.getCanonicalName()+"] and ["+class2.getCanonicalName()+"] can't be casted to comparable pair");
	}

	private static <T> T convert2Class(final Object value, final Class<T> target) throws ContentException {
		return SQLUtils.convert(target, value);
	}
}
