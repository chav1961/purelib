package chav1961.purelib.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class FilteredReadOnlyResultSet extends AbstractReadOnlyResultSet {
	private static final int		LEVEL_TERM = 0;
	private static final int		LEVEL_NEG = 1;
	private static final int		LEVEL_MUL = 2;
	private static final int		LEVEL_ADD = 3;
	private static final int		LEVEL_CAT = 4;
	private static final int		LEVEL_COMPARE = 5;
	private static final int		LEVEL_NOT = 6;
	private static final int		LEVEL_AND = 7;
	private static final int		LEVEL_OR = 8;
	
	private final AbstractContent	innerContent;

	public FilteredReadOnlyResultSet(final ResultSet rs, final String condition) throws SQLException, SyntaxException {
		super(checkResultSet(rs).getMetaData(),checkResultSet(rs).getType());
		this.innerContent = filterContent(rs,condition);
	}

	@Override
	public Statement getStatement() throws SQLException {
		return null;
	}

	@Override
	protected AbstractContent getContent() {
		return innerContent;
	}

	private AbstractContent filterContent(final ResultSet rs, final String condition) throws SQLException, SyntaxException {
		if (condition == null || condition.isEmpty()) {
			throw new IllegalArgumentException("Condition string can't be null or empty");
		}
		else {
			final FilterTree		tree = parseExpression(new Lexema((condition+"\0").toCharArray()),LEVEL_OR,rs.getMetaData());
			final List<Object[]>	result = new ArrayList<>(); 
			
			while (rs.next()) {
				final Object[]		content = toObjectArray(rs); 
				
				if (included(content,tree)) {
					result.add(content);
				}
			}
			final Object[][]		returned = result.toArray(new Object[result.size()][]);
			
			result.clear();
			return new AbstractContent(){
				int currentRow = -1;
				
				@Override
				public int getRowCount() {
					return returned.length;
				}

				@Override
				public int getCurrentRow() {
					return currentRow;
				}

				@Override
				public void setCurrentRow(final int row) {
					this.currentRow = row;
				}

				@Override
				public Object[] getRow(final int rowNum) {
					return returned[rowNum];
				}
			};
		}
	}

	private FilterTree parseExpression(final Lexema lex, final int level, final ResultSetMetaData rsmd) throws SyntaxException, SQLException {
		int			startPos;
		FilterTree	left, right;

		switch (level) {
			case LEVEL_TERM		:
				switch (lex.lexType) {
					case Lexema.LEX_OPEN		:
						lex.next();
						left = parseExpression(lex,LEVEL_OR,rsmd);
						if (lex.lexType == Lexema.LEX_CLOSE) {
							lex.next();
							return left;
						}
						else {
							throw new SyntaxException(0,lex.pos,"Close bracket is missing");
						}
					case Lexema.LEX_INT_CONST	:
						left = new ConstFilterTree(lex.longValue);
						lex.next();
						return left;
					case Lexema.LEX_REAL_CONST	:
						left = new ConstFilterTree(lex.doubleValue);
						lex.next();
						return left;
					case Lexema.LEX_CHAR_CONST	:
						left = new ConstFilterTree(lex.stringValue);
						lex.next();
						return left;
					case Lexema.LEX_FUNCTION	:
						switch (lex.funcType) {
							case Lexema.FUNC_TO_CHAR 	: return buildConversion(lex,String.class,rsmd);
							case Lexema.FUNC_TO_NUMBER	: return buildConversion(lex,Number.class,rsmd);
							case Lexema.FUNC_TO_DATE	: return buildConversion(lex,Date.class,rsmd);
							default : throw new SyntaxException(0,lex.pos,"Unsupported function");
						}
					case Lexema.LEX_FIELD		:
						for (int index = 1; index <= rsmd.getColumnCount(); index++) {
							if (rsmd.getColumnName(index).equalsIgnoreCase(lex.stringValue)) {
								lex.next();
								try{return new FieldFilterTree(index,Class.forName(rsmd.getColumnClassName(index)));
								} catch (ClassNotFoundException e) {
									throw new SyntaxException(0,lex.pos,"Unsupported field type ["+rsmd.getColumnClassName(index)+"] for field ["+lex.stringValue+"]");
								}
							}
						}
					default : throw new SyntaxException(0,lex.pos,"Unwaited lexema - operand is required");
				}
			case LEVEL_NEG		:
				if (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_SUB) {
					lex.next();
					startPos = lex.pos;
					left = parseExpression(lex,level-1,rsmd);
					if (left.getResultType() == Number.class) {
						return new NegFilterTree(left);
					}
					else {
						throw new SyntaxException(0,startPos,"Expression must have number type");
					}
				}
				else if (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_ADD) {
					lex.next();
					return parseExpression(lex,level-1,rsmd);
				}
				else {
					return parseExpression(lex,level-1,rsmd);
				}
			case LEVEL_MUL		:
				startPos = lex.pos;
				left = parseExpression(lex,level-1,rsmd);
				if (lex.lexType == Lexema.LEX_OPER && (lex.operType == Lexema.OPER_MUL || lex.operType == Lexema.OPER_DIV || lex.operType == Lexema.OPER_MOD)) {
					final List<Integer>		operList = new ArrayList<>();
					final List<FilterTree>	argList = new ArrayList<>();

					if (left.getResultType() == Boolean.class) {
						operList.add(Lexema.OPER_LOAD);
						argList.add(left);
						while (lex.lexType == Lexema.LEX_OPER && (lex.operType == Lexema.OPER_ADD || lex.operType == Lexema.OPER_SUB || lex.operType == Lexema.OPER_MOD)) {
							final int operType = lex.operType;
							
							lex.next();
							startPos = lex.pos;
							right = parseExpression(lex,level-1,rsmd);
							if (left.getResultType() == Number.class) {
								operList.add(operType);
								argList.add(right);
							}
							else {
								throw new SyntaxException(0,startPos,"Expression must have number type");
							}
						}
						return new ArithmeticFilterTree(operList,argList);
					}
					else {
						throw new SyntaxException(0,startPos,"Expression must have boolean type");
					}
				}
				else {
					return left;
				}
			case LEVEL_ADD		:
				startPos = lex.pos;
				left = parseExpression(lex,level-1,rsmd);
				if (lex.lexType == Lexema.LEX_OPER && (lex.operType == Lexema.OPER_ADD || lex.operType == Lexema.OPER_SUB)) {
					final List<Integer>		operList = new ArrayList<>();
					final List<FilterTree>	argList = new ArrayList<>();

					if (left.getResultType() == Boolean.class) {
						operList.add(Lexema.OPER_LOAD);
						argList.add(left);
						while (lex.lexType == Lexema.LEX_OPER && (lex.operType == Lexema.OPER_ADD || lex.operType == Lexema.OPER_SUB)) {
							final int operType = lex.operType;
							
							lex.next();
							startPos = lex.pos;
							right = parseExpression(lex,level-1,rsmd);
							if (left.getResultType() == Number.class) {
								operList.add(operType);
								argList.add(right);
							}
							else {
								throw new SyntaxException(0,startPos,"Expression must have number type");
							}
						}
						return new ArithmeticFilterTree(operList,argList);
					}
					else {
						throw new SyntaxException(0,startPos,"Expression must have boolean type");
					}
				}
				else {
					return left;
				}
			case LEVEL_CAT		:
				startPos = lex.pos;
				left = parseExpression(lex,level-1,rsmd);
				if (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_CAT) {
					final List<FilterTree>	list = new ArrayList<>();

					if (left.getResultType() == Boolean.class) {
						list.add(left);
						while (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_CAT) {
							lex.next();
							startPos = lex.pos;
							right = parseExpression(lex,level-1,rsmd);
							if (left.getResultType() == Boolean.class) {
								list.add(right);
							}
							else {
								throw new SyntaxException(0,startPos,"Expression must have boolean type");
							}
						}
						return new CatFilterTree(list);
					}
					else {
						throw new SyntaxException(0,startPos,"Expression must have boolean type");
					}
				}
				else {
					return left;
				}
			case LEVEL_COMPARE	:
				left = parseExpression(lex,level-1,rsmd);
				if (lex.lexType == Lexema.LEX_OPER && (lex.operType == Lexema.OPER_LE || lex.operType == Lexema.OPER_LT || lex.operType == Lexema.OPER_GE || lex.operType == Lexema.OPER_GT || lex.operType == Lexema.OPER_EQ || lex.operType == Lexema.OPER_NE)) {
					final int oper = lex.operType;
					
					startPos = lex.pos;
					lex.next();
					right = parseExpression(lex,level-1,rsmd);
					if (left.getResultType() != right.getResultType()) {
						throw new SyntaxException(0,startPos,"Different content type from the left and the right of the operation, Use converrsion functions");
					}
					else {
						return new ComparisonFilterTree(oper,left,right);
					}
				}
				else if (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_LIKE) {
					startPos = lex.pos;
					lex.next();
					right = parseExpression(lex,level-1,rsmd);
					if (left.getResultType() != right.getResultType()) {
						throw new SyntaxException(0,startPos,"Different content type from the left and the right of the operation, Use converrsion functions");
					}
					else if (left.getResultType() != String.class) {
						throw new SyntaxException(0,startPos,"Non-string types of the operands");
					}
					else {
						return new ComparisonFilterTree(Lexema.OPER_LIKE,left,right);
					}
				}
				else if (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_BETWEEN) {
					startPos = lex.pos;
					lex.next();
					right = parseExpression(lex,level-1,rsmd);
					if (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_AND) {
						lex.next();
						final FilterTree	third = parseExpression(lex,level-1,rsmd);

						if (left.getResultType() != right.getResultType() || left.getResultType() != third.getResultType()) {
							throw new SyntaxException(0,startPos,"Different content type from the left and the right of the operation, Use converrsion functions");
						}
						else {
							return new LogicalFilterTree(Lexema.OPER_AND,new ComparisonFilterTree(Lexema.OPER_GE,left,right),new ComparisonFilterTree(Lexema.OPER_LE,left,third));
						}
					}
					else {
						throw new SyntaxException(0,lex.pos,"AND clause is missing");
					}
				}
				else if (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_IN) {
					final List<FilterTree>	list = new ArrayList<>();
				
					lex.next();
					if (lex.lexType == Lexema.LEX_OPEN) {
						do{lex.next();
							startPos = lex.pos;
							right = parseExpression(lex,level-1,rsmd);
							if (left.getResultType() != right.getResultType()) {
								throw new SyntaxException(0,startPos,"Different content type from the left and the right of the operation, Use converrsion functions");
							}
							else {
								list.add(right);
							}
						} while (lex.lexType == Lexema.LEX_DIV);	
						if (lex.lexType == Lexema.LEX_CLOSE) {
							lex.next();
							final List<FilterTree>	comparisons = new ArrayList<>();
							
							for (FilterTree item : list) {
								comparisons.add(new ComparisonFilterTree(Lexema.OPER_EQ,left,item));
							}
							return new LogicalFilterTree(Lexema.OPER_OR,comparisons);
						}
						else {
							throw new SyntaxException(0,lex.pos,"Close bracket is missing");
						}
					}
					else {
						throw new SyntaxException(0,lex.pos,"Open bracket is missing");
					}
				}
				else {
					return left;
				}
			case LEVEL_NOT		:
				if (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_NOT) {
					lex.next();
					startPos = lex.pos;
					left = parseExpression(lex,level-1,rsmd);
					
					if (left.getResultType() == Boolean.class) {
						return new LogicalFilterTree(Lexema.OPER_NOT,left);
					}
					else {
						throw new SyntaxException(0,startPos,"Expression must have boolean type");
					}
				}
				else {
					return parseExpression(lex,level-1,rsmd);
				}
			case LEVEL_AND		:
				startPos = lex.pos;
				left = parseExpression(lex,level-1,rsmd);
				if (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_AND) {
					final List<FilterTree>	list = new ArrayList<>();

					if (left.getResultType() == Boolean.class) {
						list.add(left);
						while (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_AND) {
							lex.next();
							startPos = lex.pos;
							right = parseExpression(lex,level-1,rsmd);
							if (left.getResultType() == Boolean.class) {
								list.add(right);
							}
							else {
								throw new SyntaxException(0,startPos,"Expression must have boolean type");
							}
						}
						return new LogicalFilterTree(lex.operType,list);
					}
					else {
						throw new SyntaxException(0,startPos,"Expression must have boolean type");
					}
				}
				else {
					return left;
				}
			case LEVEL_OR		:
				startPos = lex.pos;
				left = parseExpression(lex,level-1,rsmd);
				if (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_OR) {
					final List<FilterTree>	list = new ArrayList<>();

					if (left.getResultType() == Boolean.class) {
						list.add(left);
						while (lex.lexType == Lexema.LEX_OPER && lex.operType == Lexema.OPER_OR) {
							lex.next();
							startPos = lex.pos;
							right = parseExpression(lex,level-1,rsmd);
							if (left.getResultType() == Boolean.class) {
								list.add(right);
							}
							else {
								throw new SyntaxException(0,startPos,"Expression must have boolean type");
							}
						}
						return new LogicalFilterTree(lex.operType,list);
					}
					else {
						throw new SyntaxException(0,startPos,"Expression must have boolean type");
					}
				}
				else {
					return left;
				}
			default : throw new UnsupportedOperationException("Expression level ["+level+"] is not supported yet"); 
		}
	}

	private FilterTree buildConversion(final Lexema lex, final Class<?> typeAwaited, final ResultSetMetaData rsmd) throws SyntaxException, SQLException {
		lex.next();
		if (lex.lexType == Lexema.LEX_OPEN) {
			return new ConvertFilterTree(parseExpression(lex,LEVEL_TERM,rsmd),typeAwaited);
		}
		else {
			throw new SyntaxException(0,lex.pos,"Open bracket is missing"); 
		}
	}

	private boolean included(final Object[] content, final FilterTree tree) {
		return ((Boolean)tree.get(content)).booleanValue();
	}

	private Object[] toObjectArray(final ResultSet rs) throws SQLException {
		final Object[]	returned = new Object[rs.getMetaData().getColumnCount()];
		
		for (int index = 1; index <= returned.length; index++) {
			returned[index-1] = rs.getObject(index);
		}
		return returned;
	}

	private static ResultSet checkResultSet(final ResultSet rs) {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null"); 
		}
		else {
			return rs;
		}
	}
	
	static class Lexema {
		public static final int		LEX_EOD = 0;
		public static final int		LEX_DOT = 1;
		public static final int		LEX_DIV = 2;
		public static final int		LEX_OPEN = 3;
		public static final int		LEX_CLOSE = 4;
		public static final int		LEX_OPER = 5;
		public static final int		LEX_INT_CONST = 6;
		public static final int		LEX_REAL_CONST = 7;
		public static final int		LEX_CHAR_CONST = 8;
		public static final int		LEX_FUNCTION = 9;
		public static final int		LEX_FIELD = 10;
		
		public static final int		OPER_LOAD = 0;
		public static final int		OPER_ADD = 1;
		public static final int		OPER_SUB = 2;
		public static final int		OPER_MUL = 3;
		public static final int		OPER_DIV = 4;
		public static final int		OPER_MOD = 5;
		public static final int		OPER_CAT = 6;
		public static final int		OPER_LE = 7;
		public static final int		OPER_LT = 8;
		public static final int		OPER_GE = 9;
		public static final int		OPER_GT = 10;
		public static final int		OPER_EQ = 11;
		public static final int		OPER_NE = 12;
		public static final int		OPER_IN = 13;
		public static final int		OPER_LIKE = 14;
		public static final int		OPER_NOT = 15;
		public static final int		OPER_AND = 16;
		public static final int		OPER_OR = 17;
		public static final int		OPER_BETWEEN = 18;

		public static final int		FUNC_TO_CHAR = 0;
		public static final int		FUNC_TO_NUMBER = 1;
		public static final int		FUNC_TO_DATE = 2;

		private static SyntaxTreeInterface<NameAndLex>	PREDEFINED = new AndOrTree<>();
		private static final String[]	OPERS = {"OPER_LOAD", "OPER_ADD", "OPER_SUB", "OPER_MUL", "OPER_DIV"
												 , "OPER_MOD", "OPER_CAT", "OPER_LE", "OPER_LT", "OPER_GE"
												 , "OPER_GT", "OPER_EQ", "OPER_NE", "OPER_IN", "OPER_LIKE"
												 , "OPER_NOT", "OPER_AND", "OPER_OR", "OPER_BETWEEN"
												};
		private static final String[]	FUNCS = {"FUNC_TO_CHAR", "FUNC_TO_NUMBER", "FUNC_TO_DATE"
		 										};
		
		static {
			PREDEFINED.placeName("AND",new NameAndLex(LEX_OPER,OPER_AND,0));
			PREDEFINED.placeName("BETWEEN",new NameAndLex(LEX_OPER,OPER_BETWEEN,0));
			PREDEFINED.placeName("IN",new NameAndLex(LEX_OPER,OPER_IN,0));
			PREDEFINED.placeName("LIKE",new NameAndLex(LEX_OPER,OPER_LIKE,0));
			PREDEFINED.placeName("NOT",new NameAndLex(LEX_OPER,OPER_NOT,0));
			PREDEFINED.placeName("OR",new NameAndLex(LEX_OPER,OPER_OR,0));
			PREDEFINED.placeName("TO_CHAR",new NameAndLex(LEX_FUNCTION,0,FUNC_TO_CHAR));
			PREDEFINED.placeName("TO_DATE",new NameAndLex(LEX_FUNCTION,0,FUNC_TO_DATE));
			PREDEFINED.placeName("TO_NUMBER",new NameAndLex(LEX_FUNCTION,0,FUNC_TO_NUMBER));
		}
		
		private final char[]		content;
		private final int[]			location = new int[2];
		private final long[]		numbers = new long[2];
		int							pos = 0;
		int							lexType, operType, funcType;
		long						longValue;
		double						doubleValue;
		String						stringValue;
		
		Lexema(final char[] content) throws SyntaxException {
			this.content = content;
			this.lexType = next();
		}
		
		int next() throws SyntaxException {
			while (content[pos] <= ' ' && content[pos] != '\0') {
				pos++;
			}
			switch (content[pos]) {
				case '\0'	: return lexType = LEX_EOD; 
				case '.'	: pos++; return lexType = LEX_DOT; 
				case ','	: pos++; return lexType = LEX_DIV; 
				case '('	: pos++; return lexType = LEX_OPEN; 
				case ')'	: pos++; return lexType = LEX_CLOSE; 
				case '+'	: pos++; operType = OPER_ADD; return lexType = LEX_OPER; 
				case '-'	: pos++; operType = OPER_SUB; return lexType = LEX_OPER; 
				case '*'	: pos++; operType = OPER_MUL; return lexType = LEX_OPER; 
				case '/'	: pos++; operType = OPER_DIV; return lexType = LEX_OPER; 
				case '='	: pos++; operType = OPER_EQ; return lexType = LEX_OPER; 
				case '|'	:
					if (content[pos+1] == '|') {
						pos += 2; operType = OPER_CAT; return lexType = LEX_OPER; 
					}
					else {
						throw new SyntaxException(0,pos,"Unknown operator");
					}
				case '<'	:
					if (content[pos+1] == '=') {
						pos += 2; operType = OPER_LE; return lexType = LEX_OPER; 
					}
					else if (content[pos+1] == '>') {
						pos += 2; operType = OPER_NE; return lexType = LEX_OPER; 
					}
					else {
						pos++; operType = OPER_LT; return lexType = LEX_OPER;
					}
				case '>'	:
					if (content[pos+1] == '=') {
						pos += 2; operType = OPER_GE; return lexType = LEX_OPER; 
					}
					else {
						pos++; operType = OPER_GT; return lexType = LEX_OPER;
					}
				case '\'' :
					final StringBuilder	sb = new StringBuilder();
					final int 			startPos = pos;
					
					try{pos = CharUtils.parseStringExtended(content,pos+1,'\'',sb);
						stringValue = sb.toString();
						return lexType = LEX_CHAR_CONST;
					} catch (IllegalArgumentException exc) {
						throw new SyntaxException(0,startPos,exc.getLocalizedMessage());
					}
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					pos = CharUtils.parseNumber(content,pos,numbers,CharUtils.PREF_ANY,true);
					switch ((int)numbers[1]) {
						case CharUtils.PREF_INT : case CharUtils.PREF_LONG :
							longValue = numbers[0];
							return lexType = LEX_INT_CONST;
						case CharUtils.PREF_FLOAT :
							doubleValue = Float.intBitsToFloat((int)numbers[0]);
							return lexType = LEX_REAL_CONST;
						case CharUtils.PREF_DOUBLE :
							doubleValue = Double.longBitsToDouble(numbers[0]);
							return lexType = LEX_REAL_CONST;
					}
					return 0;
				default :
					if (Character.isJavaIdentifierStart(content[pos])) {
						pos = CharUtils.parseName(content,pos,location);
						
						final String name = new String(content,location[0],location[1]-location[0]+1).toUpperCase();
						final long	id = PREDEFINED.seekName(name);
						
						if (id >= 0) {
							final NameAndLex	found = PREDEFINED.getCargo(id);
							
							operType = found.operType;
							funcType = found.functionType;
							return lexType = found.lexType;
						}
						else {
							stringValue = name;
							return lexType = LEX_FIELD;
						}
					}
					else {
						throw new SyntaxException(0,pos,"Unsuppported symbol");
					}
			}
		}

		@Override
		public String toString() {
			switch (lexType) {
				case LEX_EOD		: return "EOD";
				case LEX_DOT		: return "DOT";
				case LEX_DIV		: return "DIV";
				case LEX_OPEN		: return "OPEN";
				case LEX_CLOSE		: return "CLOSE";
				case LEX_OPER		: return "OPER{"+OPERS[operType]+"}";
				case LEX_INT_CONST	: return "INT{"+longValue+"}";
				case LEX_REAL_CONST	: return "REAL{"+doubleValue+"}";
				case LEX_CHAR_CONST	: return "CHAR{"+stringValue+"}";
				case LEX_FUNCTION	: return "FUNCTION{"+FUNCS[funcType]+"}";
				case LEX_FIELD		: return "FIELD{"+stringValue+"}";
				default 			: return "Unknown lex type";
			}
		}
	}
	
	interface FilterTree {
		Class<?> getResultType();
		Object get(final Object[] content);
	}

	static class LogicalFilterTree implements FilterTree {
		private final int				oper;
		private final List<FilterTree>	content;

		LogicalFilterTree(final int oper, final FilterTree... content) {
			this(oper,Arrays.asList(content));
		}
		
		LogicalFilterTree(final int oper, final List<FilterTree> content) {
			this.oper = oper;
			this.content = content;
		}
		
		@Override
		public Class<?> getResultType() {
			return Boolean.class;
		}

		@Override
		public Object get(final Object[] content) {
			switch (oper) {
				case Lexema.OPER_NOT	:
					return !((Boolean)this.content.get(0).get(content));
				case Lexema.OPER_AND	:
					for (FilterTree item : this.content) {
						if (!((Boolean)item.get(content))) {
							return Boolean.valueOf(false);
						}
					}
					return Boolean.valueOf(true);
				case Lexema.OPER_OR		:
					for (FilterTree item : this.content) {
						if (((Boolean)item.get(content))) {
							return Boolean.valueOf(true);
						}
					}
					return Boolean.valueOf(false);
				default : throw new UnsupportedOperationException("Unsupported logical operation ["+oper+"]"); 
			}
		}

		@Override
		public String toString() {
			return "LogicalFilterTree [oper=" + oper + ", content=" + content + "]";
		}
	}

	static class ComparisonFilterTree implements FilterTree {
		private final int			oper;
		private final FilterTree	left, right;

		ComparisonFilterTree(final int oper, final FilterTree left,final FilterTree right) {
			this.oper = oper;
			this.left = left;
			this.right = right;
		}
		
		public Class<?> getResultType() {
			return Boolean.class;
		}

		@Override
		public Object get(final Object[] content) {
			final Object	leftValue = left.get(content), rightValue = right.get(content); 
			
			switch (oper) {
				case Lexema.OPER_LE		:
					if (leftValue instanceof Date) {
						return ((Date)leftValue).getTime() <= ((Date)rightValue).getTime(); 
					}
					else if (leftValue instanceof Double) {
						return ((Double)leftValue).doubleValue() <= ((Double)rightValue).doubleValue(); 
					}
					else if (leftValue instanceof Long) {
						return ((Long)leftValue).longValue() <= ((Long)rightValue).longValue(); 
					}
					else if (leftValue instanceof String) {
						return ((String)leftValue).compareTo(((String)rightValue)) <= 0; 
					}
					else {
						throw new UnsupportedOperationException("Unsupported comparison operation ["+oper+"]");						
					}
				case Lexema.OPER_LT		:
					if (leftValue instanceof Date) {
						return ((Date)leftValue).getTime() < ((Date)rightValue).getTime(); 
					}
					else if (leftValue instanceof Double) {
						return ((Double)leftValue).doubleValue() < ((Double)rightValue).doubleValue(); 
					}
					else if (leftValue instanceof Long) {
						return ((Long)leftValue).longValue() < ((Long)rightValue).longValue(); 
					}
					else if (leftValue instanceof String) {
						return ((String)leftValue).compareTo(((String)rightValue)) < 0; 
					}
					else {
						throw new UnsupportedOperationException("Unsupported comparison operation ["+oper+"]");						
					}
				case Lexema.OPER_GE		:
					if (leftValue instanceof Date) {
						return ((Date)leftValue).getTime() >= ((Date)rightValue).getTime(); 
					}
					else if (leftValue instanceof Double) {
						return ((Double)leftValue).doubleValue() >= ((Double)rightValue).doubleValue(); 
					}
					else if (leftValue instanceof Long) {
						return ((Long)leftValue).longValue() >= ((Long)rightValue).longValue(); 
					}
					else if (leftValue instanceof String) {
						return ((String)leftValue).compareTo(((String)rightValue)) >= 0; 
					}
					else {
						throw new UnsupportedOperationException("Unsupported comparison operation ["+oper+"]");						
					}
				case Lexema.OPER_GT		:
					if (leftValue instanceof Date) {
						return ((Date)leftValue).getTime() > ((Date)rightValue).getTime(); 
					}
					else if (leftValue instanceof Double) {
						return ((Double)leftValue).doubleValue() > ((Double)rightValue).doubleValue(); 
					}
					else if (leftValue instanceof Long) {
						return ((Long)leftValue).longValue() > ((Long)rightValue).longValue(); 
					}
					else if (leftValue instanceof String) {
						return ((String)leftValue).compareTo(((String)rightValue)) > 0; 
					}
					else {
						throw new UnsupportedOperationException("Unsupported comparison operation ["+oper+"]");						
					}
				case Lexema.OPER_EQ		:
					if (leftValue instanceof Date) {
						return ((Date)leftValue).getTime() == ((Date)rightValue).getTime(); 
					}
					else if (leftValue instanceof Double) {
						return ((Double)leftValue).doubleValue() == ((Double)rightValue).doubleValue(); 
					}
					else if (leftValue instanceof Long) {
						return ((Long)leftValue).longValue() == ((Long)rightValue).longValue(); 
					}
					else if (leftValue instanceof String) {
						return ((String)leftValue).compareTo(((String)rightValue)) == 0; 
					}
					else {
						throw new UnsupportedOperationException("Unsupported comparison operation ["+oper+"]");						
					}
				case Lexema.OPER_NE		:
					if (leftValue instanceof Date) {
						return ((Date)leftValue).getTime() != ((Date)rightValue).getTime(); 
					}
					else if (leftValue instanceof Double) {
						return ((Double)leftValue).doubleValue() != ((Double)rightValue).doubleValue(); 
					}
					else if (leftValue instanceof Long) {
						return ((Long)leftValue).longValue() != ((Long)rightValue).longValue(); 
					}
					else if (leftValue instanceof String) {
						return ((String)leftValue).compareTo(((String)rightValue)) == 0; 
					}
					else {
						throw new UnsupportedOperationException("Unsupported comparison operation ["+oper+"]");						
					}
				case Lexema.OPER_LIKE	:
					return InternalUtils.matchLikeStyledTemplate(leftValue.toString(),rightValue.toString());
				default : throw new UnsupportedOperationException("Unsupported comparison operation ["+oper+"]"); 
			}
		}

		@Override
		public String toString() {
			return "ComparisonFilterTree [oper=" + oper + ", left=" + left + ", right=" + right + "]";
		}
	}

	static class CatFilterTree implements FilterTree {
		private final List<FilterTree>	content;

		CatFilterTree(FilterTree... content) {
			this(Arrays.asList(content));
		}
		
		CatFilterTree(final List<FilterTree> content) {
			this.content = content;
		}
		
		public Class<?> getResultType() {
			return String.class;
		}

		@Override
		public Object get(final Object[] content) {
			final StringBuilder sb = new StringBuilder();
			
			for (FilterTree item : this.content) {
				sb.append(item.get(content).toString());
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			return "CatFilterTree [content=" + content + "]";
		}
	}
	
	static class ArithmeticFilterTree implements FilterTree {
		private final List<Integer>		oper;
		private final List<FilterTree>	content;

		ArithmeticFilterTree(final List<Integer> oper, final List<FilterTree> content) {
			this.oper = oper;
			this.content = content;
		}
		
		@Override
		public Class<?> getResultType() {
			return Number.class;
		}

		@Override
		public Object get(final Object[] content) {
			Long	longResult = null;
			Double	doubleResult = null;
			boolean	wasDouble = false;
			Object	operand;
			
			for (int index = 0; index < oper.size(); index++) {
				operand = this.content.get(index).get(content);
				switch (oper.get(index)) {
					case Lexema.OPER_LOAD 	:
						if (operand instanceof Double) {
							if (!wasDouble) {
								wasDouble = true;
							}
							doubleResult = (Double)operand;
						}
						else {
							longResult = (Long)operand;
						}
						break;
					case Lexema.OPER_ADD 	:
						if (operand instanceof Double) {
							if (!wasDouble) {
								wasDouble = true;
								doubleResult = Double.valueOf(longResult.doubleValue());
							}
							doubleResult += (Double)operand;
						}
						else if (wasDouble) {
							doubleResult += (Long)operand;
						}
						else  {
							longResult += (Long)operand;
						}
						break;
					case Lexema.OPER_SUB 	:
						if (operand instanceof Double) {
							if (!wasDouble) {
								wasDouble = true;
								doubleResult = Double.valueOf(longResult.doubleValue());
							}
							doubleResult -= (Double)operand;
						}
						else if (wasDouble) {
							doubleResult -= (Long)operand;
						}
						else {
							longResult -= (Long)operand;
						}
						break;
					case Lexema.OPER_MUL 	:
						if (operand instanceof Double) {
							if (!wasDouble) {
								wasDouble = true;
								doubleResult = Double.valueOf(longResult.doubleValue());
							}
							doubleResult *= (Double)operand;
						}
						else if (wasDouble) {
							doubleResult *= (Long)operand;
						}
						else {
							longResult *= (Long)operand;
						}
						break;
					case Lexema.OPER_DIV 	:
						if (operand instanceof Double) {
							if (!wasDouble) {
								wasDouble = true;
								doubleResult = Double.valueOf(longResult.doubleValue());
							}
							doubleResult /= (Double)operand;
						}
						else if (wasDouble) {
							doubleResult /= (Long)operand;
						}
						else {
							longResult /= (Long)operand;
						}
						break;
					case Lexema.OPER_MOD 	:
						if (operand instanceof Double) {
							if (!wasDouble) {
								wasDouble = true;
								doubleResult = Double.valueOf(longResult.doubleValue());
							}
							doubleResult %= (Double)operand;
						}
						else if (wasDouble) {
							doubleResult %= (Long)operand;
						}
						else {
							longResult %= (Long)operand;
						}
						break;
					default : throw new UnsupportedOperationException("Unsupported arithmetic operation ["+oper.get(index)+"]"); 
				}
			}
			return wasDouble ? doubleResult : longResult;
		}

		@Override
		public String toString() {
			return "ArithmeticFilterTree [oper=" + oper + ", content=" + content + "]";
		}
	}

	static class NegFilterTree implements FilterTree {
		private final FilterTree	content;

		NegFilterTree(final FilterTree content) {
			this.content = content;
		}
		
		public Class<?> getResultType() {
			return Number.class;
		}

		@Override
		public Object get(final Object[] content) {
			final Object	result = this.content.get(content);
			
			if (result instanceof Long) {
				return -((Long)result).longValue(); 
			}
			else {
				return -((Double)result).doubleValue(); 
			}
		}

		@Override
		public String toString() {
			return "NegFilterTree [content=" + content + "]";
		}
	}

	static class ConvertFilterTree implements FilterTree {
		private final FilterTree	expression;
		private final Class<?>		type;

		ConvertFilterTree(final FilterTree expression, final Class<?> type) {
			this.expression = expression;
			this.type = type;
		}

		@Override
		public Class<?> getResultType() {
			return type;
		}

		@Override
		public Object get(final Object[] content) {
			Object	operand = expression.get(content);
			
			try{if (Date.class.isAssignableFrom(type)) {
						return InternalUtils.convert(Date.class,operand);
				}
				else if (Number.class.isAssignableFrom(type)) {
					return InternalUtils.convert(Number.class,operand);
				}
				else if (String.class.isAssignableFrom(type)) {
					return InternalUtils.convert(String.class,operand);
				}
				else {
					throw new UnsupportedOperationException("Unsupported conversion type ["+type+"]"); 
				}
			} catch (ContentException e) {
				throw new UnsupportedOperationException("Unsupported conversion type ["+type+"]: "+e.getLocalizedMessage()); 
			}
		}

		@Override
		public String toString() {
			return "ConvertFilterTree [expression=" + expression + ", type=" + type + "]";
		}
	}
	
	static class ConstFilterTree implements FilterTree {
		private final Class<?>	type;
		private final Object	value;

		ConstFilterTree(final long value) {
			this.type = Number.class;
			this.value = Long.valueOf(value);
		}

		ConstFilterTree(final double value) {
			this.type = Number.class;
			this.value = Double.valueOf(value);
		}

		ConstFilterTree(final String value) {
			this.type = String.class;
			this.value = value;
		}
		
		ConstFilterTree(final boolean value) {
			this.type = Boolean.class;
			this.value = value;
		}
		
		@Override
		public Class<?> getResultType() {
			return type;
		}

		@Override
		public Object get(final Object[] content) {
			return value;
		}

		@Override
		public String toString() {
			return "ConstFilterTree [type=" + type + ", value=" + value + "]";
		}
	}

	static class FieldFilterTree implements FilterTree {
		private final int		location;
		private final Class<?>	type;

		FieldFilterTree(final int location, final Class<?> type) {
			this.location = location;
			this.type = type;
		}

		@Override
		public Class<?> getResultType() {
			return type;
		}

		@Override
		public Object get(final Object[] content) {
			return content[location];
		}

		@Override
		public String toString() {
			return "FieldFilterTree [location=" + location + ", type=" + type + "]";
		}
	}

	private static class NameAndLex {
		final int	lexType;
		final int	operType;
		final int	functionType;
		
		public NameAndLex(int lexType, int operType, int functionType) {
			this.lexType = lexType;
			this.operType = operType;
			this.functionType = functionType;
		}
	}
}
