package chav1961.purelib.streams.char2char;


import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.DefaultLoggerFacade;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

/*
%anchored() <#%+n>#... %raw [<#>#...]\n --> \<h%n\>%raw\</h%n\>
%parsed(1)'  '<' '>' '...\n<=> --> %parsed(1)\<br\>
%raw\n<=>=...\n--> \<h1\>%raw\</h1\>
%raw\n<->-...\n--> \<h2\>%raw\</h2\>
\><%parsed()\n>>... --> \<bq\><%parsed()\n>...\</bq\>
%nested(n) *<<*%+n>*... %parsed()\n>*... --> \<ul\><\<li\>%parsed()\</li\>>...\</ul\>
%nested(n) <+%+n %text\n>+... --> \<ul\><\<li\>%text\</li\>>...\</ul\>
%nested(n) <-%+n %text\n>+... --> \<ul\><\<li\>%text\</li\>>...\</ul\>
<%d %text\n>%d... --> \<ol\><\<li\>%text\</li\>>...\</ol\>
\s'    '<%text\n>'    '... --> \<code\><%text\n>...\</code\>
\s\t<%text\n>\t... --> \<code\><%text\n>...\</code\>
\n\n---<->-...\n --> \<hr/\>
\n\n___<_>_...\n --> \<hr/\>
\n\n***<*>*...\n --> \<hr/\>
\[%name\](%ref "%text") --> ....

%nested(name,maxDepth) - nested construction. Need be the same first
%parsed(N) - parseable content
%number(var) - number 
%name - name
%+name - name with increment
%name:value - name with the given value
\n - end of line
\t - tab
\\ - escaping (for all reserved chars)
' ' - text as is
--> - replacement
<>... - until loop
<>Z... - while loop
{|} - alternatives
[] - options

The set of actions to parse data is:
1. Skip blank chars
2. Skip forward with paired char set (128-bit to mark stop-chars)
3. Skip backward with paired char set (128-bit to mark stop-chars)
4. Extract char group
5. Test char with the given char set (128-bit to mark chars to interest)
6. Test char sequence with the given and/or-tree content
6. Test char sequence with the given char array (8-bit length)(byte,...)
7. Test char sequence with the given template (8-bit template type)
8. Push current cursor
9. Next current char
10. Pass char range to output
11. Pass char range to ordinal name (8-bit nameId)
12. Pass char range to array name (8-bit nameId)
13. Set value to ordinal name (8-bit nameId),(8-bit value)
14. Set value to array name (8-bit nameId)(8-bit value)
15. Increment value of ordinal name (8-bit nameId)
16. Increment value of array name (8-bit nameId)
17. Append element to array name (8-bit nameId)
18. Pass and/or-tree element to output (16-bit elementId)
19. Conditional branch (4-bit branch mask)(16-bit branch)
20. Conditional call (4-bit call mask)(16-bit branch)
21. Compare stack top with the constant (16-bit constant)
22. Seek nearest char sequence

VM for the action is a char[] buffer, integer stack, call stack and flags stack, and and/or-tree to store all expression constants.

Data parsing:
1. Parse all the lines to make parsing tree.
2. Select nested options.
3. Build the char list for the nested options and build a set of rolled variants for the options.
4. Select positioned options (to the start of line and end of line, with or without skipping blank chars).
5. Build the char list for the positioned options and build common char list to the positions.
6. Collect all chars for starting sequences.

*/


public class AbstractChar2CharConvertor implements LineByLineProcessorCallback {
	private static final BitCharSet		RESERVED = new BitCharSet('%','[',']','{','|','}','<','>',':','\r','\n');
	private static final BitCharSet		NAMECHAR = new BitCharSet('_').addRange('a','z').addRange('A','Z').addRange('0','9');

	private static final BitCharSet		MATCH_STOP = new BitCharSet((char)LexType.Ergo.ordinal(),(char)LexType.EOF.ordinal());
	private static final BitCharSet		REPLACE_STOP = new BitCharSet((char)LexType.EOF.ordinal());

	private static final char[]			ERGO = "::=".toCharArray();  
	private static final char[]			ENDUNTIL = ">...".toCharArray();  
	private static final char[]			POINTS = "...".toCharArray();  
	private static final char[]			ANCHOR = "anchor()".toCharArray();  
	private static final char[]			PARSED = "parsed(".toCharArray();  
	private static final char[]			NESTED = "nested(".toCharArray();  
	private static final char[]			NUMBER = "number(".toCharArray();  

	private enum LexType {
		Sequence, Name, IncName, Function, StartOption, EndOption, StartAlter, Alter, EndAlter, StartLoop, EndWhile, EndUntil, Ergo, EOF
	}
	
	private enum FuncType {
		Anchor, Parsed, Nested, Number
	}
	
	private enum Operations {
		True, Group, SkipBlank, Recursion, Compare, CompareAndSkip, Extract, ExtractNumber, Increment, If, While, Until, Substitute, AnchoredSubstitute, NestedSubstitute, NestedAnchoredSubstitute
	}

	private final SyntaxTreeInterface<String>	tree = new AndOrTree<String>(1);
	private BitCharSet		common = new BitCharSet(), partial[];  
	private char[]			buffer, lex;
	private int				from, to, lineNo;
	private int				actual, fromLex, toLex, level, parsedId;
	private LexType			actualLex;
	private FuncType		actualFunc;

	public AbstractChar2CharConvertor(final Reader conversionRules) throws IOException, SyntaxException {
		this(conversionRules,new DefaultLoggerFacade());
	}
	
	public AbstractChar2CharConvertor(final Reader conversionRules, final LoggerFacade facade) throws IOException, SyntaxException {
		if (conversionRules == null) {
			throw new IllegalArgumentException("Conversion rules stream can't be null");
		}
		else if (facade == null) {
			throw new IllegalArgumentException("Logger facade can't be null");
		}
		else {
			final List<Expression>	rules = new ArrayList<Expression>();
			
			try(final LoggerFacade	lf = facade.transaction("compile rules")) {
				try(final LineByLineProcessor	lblp = new LineByLineProcessor(
												new LineByLineProcessorCallback(){
													@Override
													public void processLine(final int lineNo, final char[] data, final int from, final int length) throws IOException {
														AbstractChar2CharConvertor.this.lineNo = lineNo;
														AbstractChar2CharConvertor.this.actual = AbstractChar2CharConvertor.this.from = from;
														AbstractChar2CharConvertor.this.to = from + length;
														AbstractChar2CharConvertor.this.buffer = data;
														
														try{rules.add(parse());
														} catch (SyntaxException e) {
															final String	line = new String(data,from,length);
															
															lf.message(LoggerFacade.Severity.error,e,"%1$s%2$s",line,e.getMessage());
															throw new IOException(new String(data,from,length)+'\n'+e.getMessage(),e);
														}
													}													
												})) {
					final char[]				buffer = new char[8192];
					int							len;
					
					while ((len = conversionRules.read(buffer)) > 0) {
						lblp.write(buffer,0,len);
					}
				}
				compile(rules);
				lf.rollback();
			}
		}
	}

	@Override
	public void processLine(final int lineNo, final char[] data, final int from, final int length) throws IOException {
	}
	
	private LexType next() throws SyntaxException {
		while (buffer[actual] <= ' ' && buffer[actual] != '\n' && buffer[actual] != '\r') {
			actual++;
		}
		if (RESERVED.contains(buffer[actual])) {
			switch (buffer[actual]) {
				case '['	:	actual++; return LexType.StartOption;
				case ']'	:	actual++; return LexType.EndOption;
				case '{'	:	actual++; return LexType.StartAlter;
				case '|'	:	actual++; return LexType.Alter;
				case '}'	:	actual++; return LexType.EndAlter;
				case '<'	:	actual++; return LexType.StartLoop;
				case '%'	:
					final boolean	incremental = buffer[actual+1] == '+' ? ++actual > 0 : false;	// Side effect - move actual to the next char!
					
					actual++;
					if (!incremental) {
						if (compare(buffer,actual,to,ANCHOR)) {
							actual += PARSED.length;		
							actualFunc = FuncType.Anchor;
							return LexType.Function;
						}
						else if (compare(buffer,actual,to,PARSED)) {
							actual += PARSED.length;
							parsedId = readNumber();
							if (buffer[actual] != ')') {
								throw new SyntaxException(lineNo,actual-from,"Unclosed ')' for the %parsed function");
							}
							else {
								actual++;
								actualFunc = FuncType.Parsed;
								return LexType.Function;
							}
						}
						else if (compare(buffer,actual,to,NUMBER)) {
							actual += NUMBER.length;
							readName();
							if (buffer[actual] != ')') {
								throw new SyntaxException(lineNo,actual-from,"Unclosed ')' for the %number function");
							}
							else {
								actual++;	lex = null;
								actualFunc = FuncType.Number;
								return LexType.Function;
							}
						}
						else if (compare(buffer,actual,to,NESTED)) {
							actual += NESTED.length;
							readName();
							if (buffer[actual] == ',') {
								actual++;	
								level = readNumber();	
							}
							else {
								level = 10;
							}
							if (buffer[actual] != ')') {
								throw new SyntaxException(lineNo,actual-from,"Unclosed ')' for the %nested function");
							}
							else {
								actual++;	lex = null;
								actualFunc = FuncType.Nested;
								return LexType.Function;
							}
						}
					}
					
					readName();		lex = null;
					return incremental ? LexType.IncName : LexType.Name;
				case '>'	:
					if (compare(buffer,actual,to,ENDUNTIL)) {
						actual += ENDUNTIL.length;
						return LexType.EndUntil;
					}
					else if (compare(buffer,actual+2,to,POINTS)) {
						fromLex = toLex = actual + 1;
						lex = null;	actual += POINTS.length + 2;
						return LexType.EndWhile;
					}
					else if (buffer[actual+1] == '\'') {
						actual++;
						readQuoted();
						lex = null;
						return LexType.EndWhile;
					}
					else {
						throw new SyntaxException(lineNo,actual-from,"Unknown lexema (>... or >?... awaited)");
					}
				case ':'	:
					if (compare(buffer,actual,to,ERGO)) {
						actual += ERGO.length;
						return LexType.Ergo;
					}
					else {
						throw new SyntaxException(lineNo,actual-from,"Unknown lexema (::= awaited)");
					}
				case '\r' :
					actual++;
				case '\n' :
					return LexType.EOF;
				default : throw new SyntaxException(lineNo,actual-from,"Unsupported special char '"+buffer[actual]+"'");
			}
		}
		else {
			boolean		useStringBuilder = false; 

			fromLex = actual;
			for (int maxIndex = to; actual < maxIndex; actual++) {
				if (buffer[actual] == '\''|| buffer[actual] == '\\') {
					useStringBuilder = true;
					break;
				}
				else if (RESERVED.contains(buffer[actual])) {
					break;
				}
			}
			if (!useStringBuilder) {
				toLex = actual;			lex = null;
				return LexType.Sequence;
			}
			else {
				final StringBuilder	sb = new StringBuilder();
				
				sb.append(buffer,fromLex,actual-fromLex);				
				for (int maxIndex = to; actual < maxIndex; actual++) {
					if (buffer[actual] == '\'') {
						readQuoted();
						sb.append(buffer,fromLex,toLex-fromLex);
						actual--;	// Anti-algorithm for loop increment 
					}
					else if (buffer[actual] == '\\') {
						switch (buffer[++actual]) {
							case '%' : case '[' : case ']' : case '{' : case '|' : case '}' : case '<' : case '>' : case ':' : case '\'' : case '\\' :
								sb.append(buffer[actual]);
								break;
							case 'r'	:
								sb.append('\r');
								break;
							case 'n'	:							
								sb.append('\n');
								break;
							case 't'	:							
								sb.append('\t');
								break;
							default :
								throw new SyntaxException(lineNo,actual-from,"Unsupported escape char '\\"+buffer[actual]+"'");
						}
					}
					else if (RESERVED.contains(buffer[actual])) {
						break;
					}
					else {
						sb.append(buffer[actual]);
					}
				}
				lex = sb.toString().toCharArray();
				return LexType.Sequence;
			}
		}
	}

	private Expression parse() throws SyntaxException {
		final List<char[]>	names = new ArrayList<char[]>();
		final Expression	match, replace;
		boolean				anchored = false, nested = false;
		int					maxNesting = 0, nestingName = 0;

preset:	while ((actualLex = next()) == LexType.Function) {
			switch (actualFunc) {
				case Anchor	:	
					anchored = true;
					break;
				case Nested :
					nested = true;
					nestingName = putName(extractText(),names);
					maxNesting = level;
					break;
				case Parsed	:
					break preset;
			}
		}
		
		if (actualLex == LexType.Name) {
			throw new SyntaxException(lineNo,actual-from,"Tempate can't be started with variable part (%"+new String(extractText())+")");
		}
		
		match =  match(names,MATCH_STOP);
		if (actualLex == LexType.Ergo) {
			actualLex = next();	
			replace = match(names,REPLACE_STOP);
			
			if (actualLex == LexType.EOF) {
				names.clear();
				if (anchored) {
					if (nested) {
						return new Expression(Operations.NestedAnchoredSubstitute,maxNesting,nestingName,match,replace);
					}
					else {
						return new Expression(Operations.AnchoredSubstitute,maxNesting,nestingName,match,replace);
					}
				}
				else {
					if (nested) {
						return new Expression(Operations.NestedSubstitute,maxNesting,nestingName,match,replace);
					}
					else {
						return new Expression(Operations.Substitute,maxNesting,nestingName,match,replace);
					}
				}
			}
			else {
				throw new SyntaxException(lineNo,actual-from,"Unparsed tail in the end of expression");
			}
		}
		else {
			throw new SyntaxException(lineNo,actual-from,"Unwaited lexema (::= awaited)");
		}
	}
	
	private Expression match(final List<char[]> names, final BitCharSet terminals) throws SyntaxException {
		final List<Expression>	result = new ArrayList<Expression>();
		
		while(!terminals.contains((char)actualLex.ordinal())) {
			switch (actualLex) {
				case Sequence	:
					result.add(new Expression(Operations.SkipBlank));
					result.add(new Expression(Operations.CompareAndSkip,extractText()));
					actualLex = next();
					break;
				case Name		:
					result.add(new Expression(Operations.Extract,0,putName(extractText(),names)));
					actualLex = next();
					break;
				case IncName	:
					result.add(new Expression(Operations.Increment,0,putName(extractText(),names)));
					actualLex = next();
					break;
				case Function	:
					switch (actualFunc) {
						case Parsed	:
							result.add(new Expression(Operations.Recursion,parsedId,0));
							actualLex = next();
							break;
						case Number	:
							result.add(new Expression(Operations.ExtractNumber,0,putName(extractText(),names)));
							actualLex = next();
							break;
						default :
							throw new SyntaxException(lineNo,actual-from,"Illegal usage of the function in this place. Only %parsed(N) function is available here!");
					}
					break;
				case StartOption:
					if ((actualLex = next()) != LexType.Sequence) {
						throw new SyntaxException(lineNo,actual-from,"Option must be started with the sequence, not variable or function");
					}
					else {
						result.add(new Expression(Operations.SkipBlank));
						result.add(new Expression(Operations.If
								  ,new Expression(Operations.Compare,extractText())
								  ,match(names,terminals.clone().add((char)LexType.EndOption.ordinal())))
						);
						if (actualLex != LexType.EndOption) {
							throw new SyntaxException(lineNo,actual-from,"Missing close bracket ']' for the option");
						}
						else {
							actualLex = next(); 
						}
					}
					break;
				case StartAlter	:
					result.add(new Expression(Operations.SkipBlank));
					final List<Expression>	alters = new ArrayList<Expression>();
					
					do {if ((actualLex = next()) != LexType.Sequence) {
							throw new SyntaxException(lineNo,actual-from,"Alternatives must be started with the sequence, not variable or function");
						}
						else {
							alters.add(new Expression(Operations.Compare,extractText()));
							alters.add(match(names,terminals.clone().add((char)LexType.Alter.ordinal(),(char)LexType.EndAlter.ordinal())));
						}
					} while (actualLex == LexType.Alter);
					if (actualLex != LexType.EndAlter) {
						throw new SyntaxException(lineNo,actual-from,"Missing close bracket '}' for the alternatives");
					}
					else {
						actualLex = next(); 
						result.add(new Expression(Operations.If,alters));
					}
					break;
				case StartLoop	:
					actualLex = next();
					final Expression	body = match(names,terminals.clone().add((char)LexType.EndWhile.ordinal(),(char)LexType.EndUntil.ordinal()));
					switch (actualLex) {
						case EndUntil :
							if ((actualLex = next()) != LexType.Sequence) {
								throw new SyntaxException(lineNo,actual-from,"Alternatives must be started with the sequence, not variable or function");
							}
							else {
								result.add(new Expression(Operations.Until,extractText(),body));
							}
							break;
						case EndWhile :
							result.add(new Expression(Operations.While,extractText(),body));
							actualLex = next();
							break;
						default : throw new SyntaxException(lineNo,actual-from,"Unclosed loop description");
					}
					break;
				default : throw new SyntaxException(lineNo,actual-from,"Unwaited lexema (::= awaited)");
			}
		};
		
		return new Expression(Operations.Group,result);
	}

	private void compile(final List<Expression> rules) {
		final List<Expression>	ordinal = new ArrayList<Expression>(), anchored = new ArrayList<Expression>();
		int		maxTemplateLen = 0;
		
		for (Expression item : rules) {
			switch (item.op) {
				case AnchoredSubstitute : case NestedAnchoredSubstitute : anchored.add(item); break; 
				case Substitute : case NestedSubstitute : ordinal.add(item); break;
				default : throw new RuntimeException("Internal compiler error: unwaited op ["+item.op+"]");
			}
			maxTemplateLen = Math.max(maxTemplateLen,calculateMaxTemplatLen(item));
		}
		partial = new BitCharSet[maxTemplateLen];  
		
		for (int index = 0; index < partial.length; index++) {	// Extract chars to char sets and build AND/OR tree for sequences
			partial[index] = new BitCharSet();
		}
		for (Expression item : rules) {
			replaceChar2Id(item,common,partial);
		}
		
		compileOrdinal(ordinal);
		compileAnchored(anchored);
	}	
	
	private int calculateMaxTemplatLen(final Expression item) {
		switch (item.op) {
			case If	: case Group :
				int		value = 0;
				for (Expression element : item.parameters) {
					value = Math.max(value,calculateMaxTemplatLen(element));
				}
				return value;
			case SkipBlank : case Recursion : case Extract : case ExtractNumber : case Increment :
				return 0;
			case Compare : case CompareAndSkip : 
				return item.template.length;
			case While : case Until :
				return Math.max(item.template.length,calculateMaxTemplatLen(item.parameters.get(0)));
			case Substitute	: case AnchoredSubstitute : case NestedSubstitute : case NestedAnchoredSubstitute :
				return Math.max(calculateMaxTemplatLen(item.parameters.get(0)),calculateMaxTemplatLen(item.parameters.get(0)));
			default : throw new UnsupportedOperationException();
		}
	}

	private void replaceChar2Id(final Expression item, final BitCharSet common, final BitCharSet[] charSets) {
		switch (item.op) {
			case If	: case Group : case Substitute	: case AnchoredSubstitute : case NestedSubstitute : case NestedAnchoredSubstitute :
				for (Expression element : item.parameters) {
					replaceChar2Id(element,common,charSets);
				}
				break;
			case Compare : case CompareAndSkip : case While : case Until :
				charSets[item.template.length].add(item.template[0]);
				common.add(item.template[0]);
				item.templateId = tree.placeName(item.template,0,item.template.length,null);
				break;
			case SkipBlank : case Recursion : case Extract : case ExtractNumber : case Increment :
				break;
			default : throw new UnsupportedOperationException();
		}
	}

	private void compileAnchored(final List<Expression> anchored) {
		// TODO Auto-generated method stub
	}

	private void compileOrdinal(final List<Expression> ordinal) {
		// TODO Auto-generated method stub
	}

	private int seek(final char[] data, final int from, final int to, final long[] item) {
		long	result;
		
		for (int index = from; index < to; index++) {
			if (common.contains(data[index])) {
				for (int len = partial.length-1; len > 0; len--) {
					if (partial[len].contains(data[index])) {
						if ((result = tree.seekName(data,index,index+len)) != -1) {
							item[0] = result;
							return index;
						}
					}
				}
			}
		}
		item[0] = -1;
		return to;
	}
	
	private static boolean compare(final char[] buffer, int from, int to, final char[] template) {
		int	maxIndex = template.length;
		
		if (from + maxIndex > to) {
			return false;
		}
		else {
			for (int index = 0; index < maxIndex; from++, index++) {
				if (buffer[from] != template[index]) {
					return false;
				}
			}
			return true;
		}
	}

	private static boolean exactCompare(final char[] buffer, final char[] template) {
		if (buffer.length != template.length) {
			return false;
		}
		else {
			for (int index = 0, maxIndex = template.length; index < maxIndex; index++) {
				if (buffer[index] != template[index]) {
					return false;
				}
			}
			return true;
		}
	}
	
	private int readNumber() {
		int	result = 0;
		
		while (buffer[actual] >= '0' && buffer[actual] <= '9') {
			result = 10 * result + buffer[actual] - '0';
			actual++;
		}
		return result;
	}

	private void readName() throws SyntaxException {
		fromLex = actual;
		while (NAMECHAR.contains(buffer[actual])) {
			actual++;
		}
		toLex = actual;
		if (toLex == fromLex) {
			throw new SyntaxException(lineNo,actual-from,"Missing variable name");
		}
	}	

	private void readQuoted() throws SyntaxException {
		fromLex = ++actual;
		while (buffer[actual] != '\'' && buffer[actual] != '\n') {
			actual++;
		}
		if (buffer[actual] != '\'') {
			throw new SyntaxException(lineNo,actual-from,"Unclosed quote");
		}
		else {
			toLex = actual++;
		}
	}	
	
	private char[] extractText() {
		if (lex != null) {
			return lex;
		}
		else {
			final char[]	result = new char[toLex-fromLex];
			
			System.arraycopy(buffer,fromLex,result,0,result.length);
			return result;
		}
	}
	
	private static int putName(final char[] lex, final List<char[]> names) {
		for (int index = 0; index < names.size(); index++) {
			if (exactCompare(lex,names.get(index))) {
				return -1 - index;
			}
		}
		names.add(lex);
		return names.size();
	}
	
	private static class Expression {
		private Operations			op;
		private int					nestintgLevel = 0, nestingName = 0;
		private char[]				template;
		private long				templateId = 0;
		private List<Expression>	parameters = new ArrayList<Expression>();

		Expression(final Operations op, final char[] template, final Expression... parameters) {
			this.op = op;			this.template = template;
			this.parameters.addAll(Arrays.asList(parameters));
		}

		Expression(final Operations op, final Expression... parameters) {
			this.op = op;			this.parameters.addAll(Arrays.asList(parameters));
		}

		Expression(final Operations op, final int nestingLevel, final int nestingName, final Expression... parameters) {
			this.op = op;			this.parameters.addAll(Arrays.asList(parameters));
			this.nestintgLevel = nestingLevel;
			this.nestingName = nestingName;					
		}
		
		Expression(final Operations op, final List<Expression> parameters) {
			this.op = op;			this.parameters.addAll(parameters);
		}

		@Override
		public String toString() {
			return "Expression [op=" + op + ", nestintgLevel=" + nestintgLevel + ", nestingName=" + nestingName + ", template=" + Arrays.toString(template) + ", parameters=" + parameters + "]";
		}
	}
}
