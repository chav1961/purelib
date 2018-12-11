package chav1961.purelib.basic;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.Character.UnicodeBlock;
import java.lang.Character.UnicodeScript;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import chav1961.purelib.basic.CharUtils.CharSubstitutionSource;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class FastPattern {
	private static final BitCharSet		CS_UNIVERSE = new ExtendedBitCharSet().invert();
	private static final BitCharSet		CS_PLUS_DIGIT = new ExtendedBitCharSet().addRange('0','9');
	private static final BitCharSet		CS_MINUS_DIGIT = new ExtendedBitCharSet().invert().removeRange('0','9');
	private static final BitCharSet		CS_PLUS_WORD = new ExtendedBitCharSet().addRange('a','z').addRange('A','Z').addRange('0','9').add('_');
	private static final BitCharSet		CS_MINUS_WORD = new ExtendedBitCharSet().invert().removeRange('a','z').removeRange('A','Z').removeRange('0','9').remove('_');
	private static final BitCharSet		CS_PLUS_SPACE = new ExtendedBitCharSet().add(' ').add('\t').add('\n').add('\u001B').add('\f').add('\r');
	private static final BitCharSet		CS_MINUS_SPACE = new ExtendedBitCharSet().invert().remove(' ').remove('\t').remove('\n').remove('\u001B').remove('\f').remove('\r');
	
	private static final Command		LINE_START = null;
	private static final Command		LINE_END = null;

	private static final char[]			EMPTY_GROUP = {};
	private static final char[]			TERM_GROUP = {')'};
	private static final char[]			LIST_GROUP = {'[','&',']'};
	private static final int			BIT_i = 0;
	private static final int			BIT_d = 1;
	private static final int			BIT_m = 2;
	private static final int			BIT_s = 3;
	private static final int			BIT_u = 4;
	private static final int			BIT_x = 5;
	private static final int			BIT_U = 6;
	
	private static final SyntaxTreeInterface<BitCharSet>	PREDEFINED = new AndOrTree<>();
	
	private static final Pattern		UNICODE_SCRIPT_1 = Pattern.compile("\\{Is([^\\}]*)\\}");
	private static final Pattern		UNICODE_SCRIPT_2 = Pattern.compile("\\{sc\\=([^\\}]*)\\}");
	private static final Pattern		UNICODE_SCRIPT_3 = Pattern.compile("\\{script\\=([^\\}]*)\\}");
	private static final Pattern[]		UNICODE_SCRIPTS = {UNICODE_SCRIPT_1, UNICODE_SCRIPT_2, UNICODE_SCRIPT_3};
	private static final Pattern		UNICODE_BLOCK_1 = Pattern.compile("\\{In([^\\}]*)\\}");
	private static final Pattern		UNICODE_BLOCK_2 = Pattern.compile("\\{blk\\=([^\\}]*)\\}");
	private static final Pattern		UNICODE_BLOCK_3 = Pattern.compile("\\{block\\=([^\\}]*)\\}");
	private static final Pattern[]		UNICODE_BLOCKS = {UNICODE_BLOCK_1, UNICODE_BLOCK_2, UNICODE_BLOCK_3};
	private static final Pattern		UNICODE_CATEGORY_1 = Pattern.compile("\\{gc\\=([^\\}]*)\\}");
	private static final Pattern		UNICODE_CATEGORY_2 = Pattern.compile("\\{general_category\\=([^\\}]*)\\}");
	private static final Pattern[]		UNICODE_CATEGORIES = {UNICODE_CATEGORY_1, UNICODE_CATEGORY_2};

	private static final AtomicInteger	uniqueClassId = new AtomicInteger(1);
	
	static {
		PREDEFINED.placeName("Lower",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.isLowerCase(symbol);}));
		PREDEFINED.placeName("Upper",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.isUpperCase(symbol);}));
		PREDEFINED.placeName("ASCII",ExtendedBitCharSet.buildCharSet((symbol)->{return symbol < 0x7F;}));
		PREDEFINED.placeName("Alpha",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.isLetter(symbol);}));
		PREDEFINED.placeName("Digit",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.isDigit(symbol);}));
		PREDEFINED.placeName("Alnum",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.isLetterOrDigit(symbol);}));
		PREDEFINED.placeName("Punct",ExtendedBitCharSet.buildCharSet((symbol)->{return "!\"#$%&\'()*+,-./:;<=>?@[\\]^_`{|}~".indexOf(symbol) >= 0;}));
		PREDEFINED.placeName("Graph",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.isLetterOrDigit(symbol) || "!\"#$%&\'()*+,-./:;<=>?@[\\]^_`{|}~".indexOf(symbol) >= 0;}));
		PREDEFINED.placeName("Print",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.isLetterOrDigit(symbol) || " !\"#$%&\'()*+,-./:;<=>?@[\\]^_`{|}~".indexOf(symbol) >= 0;}));
		PREDEFINED.placeName("Blank",ExtendedBitCharSet.buildCharSet((symbol)->{return symbol == ' ' || symbol == '\t';}));
		PREDEFINED.placeName("Cntrl",ExtendedBitCharSet.buildCharSet((symbol)->{return symbol <= 0x1F || symbol == 0x7F;}));
		PREDEFINED.placeName("XDigit",ExtendedBitCharSet.buildCharSet((symbol)->{return "0123456789abcdefABCDEF".indexOf(symbol) >= 0;}));
		PREDEFINED.placeName("Space",ExtendedBitCharSet.buildCharSet((symbol)->{return " \t\n\u000B\f\r".indexOf(symbol) >= 0;}));
		
		PREDEFINED.placeName("javaLowerCase",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.isLowerCase(symbol);}));
		PREDEFINED.placeName("javaUpperCase",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.isUpperCase(symbol);}));
		PREDEFINED.placeName("javaWhitespace",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.isWhitespace(symbol);}));
		PREDEFINED.placeName("javaMirrored",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.isMirrored(symbol);}));
		
		PREDEFINED.placeName("Mc",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.COMBINING_SPACING_MARK;}));
		PREDEFINED.placeName("Pc",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.CONNECTOR_PUNCTUATION;}));
		PREDEFINED.placeName("Cc",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.CONTROL;}));
		PREDEFINED.placeName("Sc",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.CURRENCY_SYMBOL;}));
		PREDEFINED.placeName("Pd",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DASH_PUNCTUATION;}));
		PREDEFINED.placeName("Nd",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DECIMAL_DIGIT_NUMBER;}));
		PREDEFINED.placeName("AN",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_ARABIC_NUMBER;}));
		PREDEFINED.placeName("BN",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_BOUNDARY_NEUTRAL;}));
		PREDEFINED.placeName("CS",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR;}));
		PREDEFINED.placeName("EN",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_EUROPEAN_NUMBER;}));
		PREDEFINED.placeName("ES",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR;}));
		PREDEFINED.placeName("ET",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR;}));
		PREDEFINED.placeName("L",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_LEFT_TO_RIGHT;}));
		PREDEFINED.placeName("LRE",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING;}));
		PREDEFINED.placeName("LRO",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE;}));
		PREDEFINED.placeName("NSM",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_NONSPACING_MARK;}));
		PREDEFINED.placeName("ON",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_OTHER_NEUTRALS;}));
		PREDEFINED.placeName("B",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_PARAGRAPH_SEPARATOR;}));
		PREDEFINED.placeName("PDF",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT;}));
		PREDEFINED.placeName("R",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_RIGHT_TO_LEFT;}));
		PREDEFINED.placeName("AL",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;}));
		PREDEFINED.placeName("RLE",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING;}));
		PREDEFINED.placeName("RLO",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE;}));
		PREDEFINED.placeName("S",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_SEGMENT_SEPARATOR;}));
		PREDEFINED.placeName("WS",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.DIRECTIONALITY_WHITESPACE;}));
		PREDEFINED.placeName("Me",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.ENCLOSING_MARK;}));
		PREDEFINED.placeName("Pe",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.END_PUNCTUATION;}));
		PREDEFINED.placeName("Pf",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.FINAL_QUOTE_PUNCTUATION;}));
		PREDEFINED.placeName("Cf",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.FORMAT;}));
		PREDEFINED.placeName("Pi",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.INITIAL_QUOTE_PUNCTUATION;}));
		PREDEFINED.placeName("Nl",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.LETTER_NUMBER;}));
		PREDEFINED.placeName("Zl",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.LINE_SEPARATOR;}));
		PREDEFINED.placeName("Ll",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.LOWERCASE_LETTER;}));
		PREDEFINED.placeName("Sm",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.MATH_SYMBOL;}));
		PREDEFINED.placeName("Lm",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.MODIFIER_LETTER;}));
		PREDEFINED.placeName("Sk",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.MODIFIER_SYMBOL;}));
		PREDEFINED.placeName("Mn",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.NON_SPACING_MARK;}));
		PREDEFINED.placeName("Lo",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.OTHER_LETTER;}));
		PREDEFINED.placeName("No",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.OTHER_NUMBER;}));
		PREDEFINED.placeName("Po",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.OTHER_PUNCTUATION;}));
		PREDEFINED.placeName("So",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.OTHER_SYMBOL;}));
		PREDEFINED.placeName("Zp",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.PARAGRAPH_SEPARATOR;}));
		PREDEFINED.placeName("Co",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.PRIVATE_USE;}));
		PREDEFINED.placeName("Zs",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.SPACE_SEPARATOR;}));
		PREDEFINED.placeName("Ps",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.START_PUNCTUATION;}));
		PREDEFINED.placeName("Cs",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.SURROGATE;}));
		PREDEFINED.placeName("Lt",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.TITLECASE_LETTER;}));
		PREDEFINED.placeName("Cn",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.UNASSIGNED;}));
		PREDEFINED.placeName("Lu",ExtendedBitCharSet.buildCharSet((symbol)->{return Character.getType(symbol) == Character.UPPERCASE_LETTER;}));
	}
	
	private final Pattern	pattern;
	private final Command	command;
	private final String[]	groups;
	
	protected FastPattern(final int minContentLength) {
		this.pattern = null;
		this.groups = null;
		this.command = null;
	}

	protected FastPattern(final Pattern pattern) {
		if (pattern == null) {
			throw new NullPointerException("Pattern can't be null"); 
		}
		else {
			try{final MethodHandles.Lookup 	lookup = MethodHandles.lookup();
				final MethodHandle 			mh = lookup.findVirtual(Pattern.class,"namedGroups",MethodType.methodType(Map.class));
				final Map<String,Integer> 	groupMap = (Map<String,Integer>)mh.invokeExact(pattern);

				this.pattern = pattern;
				this.command = null;
				this.groups = new String[groupMap.size()];
				for (Entry<String, Integer> item : groupMap.entrySet()) {
					this.groups[item.getValue()] = item.getKey(); 
				}
			} catch (Throwable t) {
				throw new IllegalArgumentException("Error preparing pattern: "+t.getLocalizedMessage());
			} 			
		}
	}

	protected FastPattern(final Command command, final boolean compile) {
		if (command == null) {
			throw new NullPointerException("Pattern can't be null"); 
		}
		else {
			try{final Map<String,Integer> 	groupMap = new HashMap<>();

				this.pattern = null;
				this.command = compile ? compile(command) : command;
				this.groups = new String[groupMap.size()];
				for (Entry<String, Integer> item : groupMap.entrySet()) {
					this.groups[item.getValue()] = item.getKey(); 
				}
			} catch (Throwable t) {
				throw new IllegalArgumentException("Error preparing pattern: "+t.getLocalizedMessage(),t);
			} 			
		}
	}
	
	public class MatchedStringItem {
		protected MatchedStringItem(final String[] names, final String[] content) {
			
		}
		
		public int size(){
			return 0;
		}
		
		public String group(final int index) {
			if (index < 0 || index >= size()) {
				throw new ArrayIndexOutOfBoundsException("Group index ["+index+"] out of range. Valid range is 0.."+(size()-1)); 
			}
			else {
				return null;
			}
		}
		
		public String group(final String name) {
			return null;
		}
	}

	public class MatchedCharArrayItem {
		private final MatchedStringItem	delegate;
		private final String[]			names;
		private final char[]			source;
		private final int[][]			ranges;
		
		protected MatchedCharArrayItem(final MatchedStringItem delegate) {
			if (delegate == null) {
				throw new NullPointerException("Delegate can't be null"); 
			}
			else {
				this.delegate = delegate;
				this.names = null;
				this.source = null;
				this.ranges = null;
			}
		}
		
		protected MatchedCharArrayItem(final String[] names, final char[] source, final int[][] ranges) {
			if (names == null) {
				throw new NullPointerException("Names can't be null"); 
			}
			else if (source == null) {
				throw new NullPointerException("Source can't be null"); 
			}
			else if (ranges == null) {
				throw new NullPointerException("Ranges can't be null"); 
			}
			else if (names.length != ranges.length) {
				throw new IllegalArgumentException("Different array size of names ["+names.length+"] and ranges ["+ranges.length+"]"); 
			}
			else {
				for (int index = 0, maxIndex = ranges.length; index < maxIndex; index++) {
					if (ranges[index] == null || ranges[index].length != 2) {
						throw new IllegalArgumentException("Ranges array at index ["+index+"] contains null or element which size != 2"); 
					}
				}
				this.delegate = null;
				this.names = names;
				this.source = source;
				this.ranges = ranges;
			}
		}
		
		public int size(){
			if (delegate != null) {
				return delegate.size();
			}
			else {
				return ranges.length;
			}
		}
		
		public char[] group(final int index) {
			if (index < 0 || index >= size()) {
				throw new ArrayIndexOutOfBoundsException("Group index ["+index+"] out of range. Valid range is 0.."+(size()-1)); 
			}
			else if (delegate != null) {
				return delegate.group(index).toCharArray();
			}
			else {
				final char[]	result = new char[ranges[index][1]];
				
				System.arraycopy(source,ranges[index][1],result,0,ranges[index][1]);
				return result;
			}
		}
		
		public char[] group(final String name) {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Group name can't be null or empty");
			}
			else if (delegate != null) {
				return delegate.group(name).toCharArray();
			}
			else {
				for (int index = 0, maxIndex = names.length; index <  maxIndex; index++) {
					if (name.equals(names[index])) {
						return group(index);
					}
				}
				return null;
			}
		}
	}
	
	public Stream<MatchedCharArrayItem> match(final char[] content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			return match(content,0,content.length);
		}
	}
	
	public Stream<MatchedCharArrayItem> match(final char[] content, final int from, final int len) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From ["+from+"] out of range. Valid range is 0.."+(content.length-1));
		}
		else if (len < 0 || len >= content.length) {
			throw new IllegalArgumentException("Length ["+len+"] out of range. Valid range is 0.."+(content.length-1));
		}
		else if (from + len < 0 || from + len >= content.length) {
			throw new IllegalArgumentException("From + length ["+(from+len)+"] out of range. Valid range is 0.."+(content.length-1));
		}
		else if (pattern != null) {
			return null;
		}
		else {
			final List<MatchedCharArrayItem>	result = new ArrayList<>();
			
			if (match(command,content,from,len)) {
				return result.stream();
			}
			else {
				return null;
			}
		}
	}

	public Stream<MatchedStringItem> match(final String content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			return match(content,0,content.length());
		}
	}
	
	public Stream<MatchedStringItem> match(final String content, final int from, final int len) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (from < 0 || from >= content.length()) {
			throw new IllegalArgumentException("From ["+from+"] out of range. Valid range is 0.."+(content.length()-1));
		}
		else if (len < 0 || len > content.length()) {
			throw new IllegalArgumentException("Length ["+len+"] out of range. Valid range is 0.."+(content.length()-1));
		}
		else if (from + len < 0 || from + len > content.length()) {
			throw new IllegalArgumentException("From + length ["+(from+len)+"] out of range. Valid range is 0.."+(content.length()-1));
		}
		else if (pattern != null) {
			@SuppressWarnings("unused")
			final List<MatchedStringItem>	result = new ArrayList<>();
			final Matcher					m = pattern.matcher(content).region(from,from+len);
			
			if (m.lookingAt()) {
				do {for (int index = 0, maxIndex = m.groupCount(); index < maxIndex; index++) {
					
					}					
				} while (m.find());
			}
			
			return null;
		}
		else if (command != null) {
			final List<MatchedStringItem>	result = new ArrayList<>();
			
			if (match(command,content.toCharArray(),from,len)) {
				return result.stream();
			}
			else {
				return null;
			}
		}
		else {
			throw new IllegalStateException("Neither pattern nor command was prepared in the instance! Check regular expression symtax");
		}
	}
	
	public static FastPattern build(final String expression, final boolean fast) {
		if (expression == null || expression.isEmpty()) {
			throw new PatternSyntaxException("Expression can't be null or empty","",0); 
		}
		else {
			final List<Command>			list = new ArrayList<>();
			final Map<String[],Command>	groups = new HashMap<>();
			
			parse(expression.toCharArray(),0,expression.length(),list,groups,EMPTY_GROUP);
			
			final Command				cmd = new SequenceCommand(list.toArray(new Command[list.size()]));
			
			list.clear();
			return new FastPattern(cmd,fast);
		}
	}
	
	private static int parse(final char[] source, int from, final int to, final List<Command> list, final Map<String[],Command> groups, final char[] terminals) {
		int	start;
		
		while (from < to) {
			switch (source[from]) {
				case '\\' :
					from = buildEscaped(source,from+1,to,list,groups,false);
					break;
				case '[' :
					final ExtendedBitCharSet	set = new ExtendedBitCharSet();
					
					from = buildCharSet(source,from+1,to,set,LIST_GROUP);
					list.add(new CharSetCommand(set));
					break;
				case '.' :
					list.add(new CharSetCommand(CS_UNIVERSE));
					break;
				case '^' :
					list.add(LINE_START);
					break;
				case '$' :
					list.add(LINE_END);
					break;
				case '?' :
					if (list.size() > 0) {
						list.set(list.size()-1,new RepeatableCommand(list.get(list.size()-1),0,1));
					}
					else {
						throw new PatternSyntaxException("[?] without preceeding pattern",new String(source),from);
					}
					break;
				case '*' :
					if (list.size() > 0) {
						if (from < to - 1) {
							switch (source[from + 1]) {
								case '?' :
									list.set(list.size()-1,new QuantorCommand(new RepeatableCommand(list.get(list.size()-1),0,Integer.MAX_VALUE),true));
									from++;
									break;
								case '+' :
									list.set(list.size()-1,new QuantorCommand(new RepeatableCommand(list.get(list.size()-1),0,Integer.MAX_VALUE),false));
									from++;
									break;
								default :
									list.set(list.size()-1,new RepeatableCommand(list.get(list.size()-1),0,Integer.MAX_VALUE));
							}
						}
						else {
							list.set(list.size()-1,new RepeatableCommand(list.get(list.size()-1),0,Integer.MAX_VALUE));
						}
					}
					else {
						throw new PatternSyntaxException("[*] without preceeding pattern",new String(source),from);
					}
					break;
				case '+' :
					if (list.size() > 0) {
						if (from < to - 1) {
							switch (source[from + 1]) {
								case '?' :
									list.set(list.size()-1,new QuantorCommand(new RepeatableCommand(list.get(list.size()-1),1,Integer.MAX_VALUE),true));
									from++;
									break;
								case '+' :
									list.set(list.size()-1,new QuantorCommand(new RepeatableCommand(list.get(list.size()-1),1,Integer.MAX_VALUE),false));
									from++;
									break;
								default :
									list.set(list.size()-1,new RepeatableCommand(list.get(list.size()-1),1,Integer.MAX_VALUE));
							}
						}
						else {
							list.set(list.size()-1,new RepeatableCommand(list.get(list.size()-1),1,Integer.MAX_VALUE));
						}
					}
					else {
						throw new PatternSyntaxException("[+] without preceeding pattern",new String(source),from);
					}
					break;
				case '{' :
					int min = 0, max = 0;
					
					from++;
					while (from < to && source[from] >= '0' && source[from] <= '9') {
						min = 10 * min + source[from] - '0';
					}
					if (from < to && source[from] == ',') {
						from++;
						while (from < to && source[from] >= '0' && source[from] <= '9') {
							max = 10 * min + source[from] - '0';
						}
						if (max == 0) {
							max = Integer.MAX_VALUE;
						}
					}
					else {
						max = Integer.MAX_VALUE;
					}
					if (from < to && source[from] == '}') {
						if (from < to - 1) {
							switch (source[from + 1]) {
								case '?' :
									list.set(list.size()-1,new QuantorCommand(new RepeatableCommand(list.get(list.size()-1),min,max),true));
									from++;
									break;
								case '+' :
									list.set(list.size()-1,new QuantorCommand(new RepeatableCommand(list.get(list.size()-1),min,max),false));
									from++;
									break;
								default :
									list.set(list.size()-1,new RepeatableCommand(list.get(list.size()-1),min,max));
							}
						}
						else {
							list.set(list.size()-1,new RepeatableCommand(list.get(list.size()-1),min,max));
						}
					}
					else {
						throw new PatternSyntaxException("Unpaired brackets: '}' is missing",new String(source),from);
					}
					break;
				case '(' :
					if (from < to - 1 && source[from + 1] == '?') {
						if (from < to - 2) {
							final List<Command>		nested = new ArrayList<>();
							
							switch (source[from + 2]) {
								case '<' :
									if (from < to - 3) {
										switch (source[from + 3]) {
											case '=' :
												from = parse(source,from + 4,to,nested,groups,TERM_GROUP);
												if (from < to && source[from] == ')') {
													list.add(new LookAheadCommand(new SequenceCommand(nested.toArray(new Command[nested.size()])),true,false));
													nested.clear();
													from++;
												}
												else {
													throw new PatternSyntaxException("Unpaired brackets: ')' is missing",new String(source),from);
												}
												break;
											case '!' :
												from = parse(source,from + 4,to,nested,groups,TERM_GROUP);
												if (from < to && source[from] == ')') {
													list.add(new LookAheadCommand(new SequenceCommand(nested.toArray(new Command[nested.size()])),true,true));
													nested.clear();
													from++;
												}
												else {
													throw new PatternSyntaxException("Unpaired brackets: ')' is missing",new String(source),from);
												}
												break;
											default :
												start = from += 4;
												while (from < to && Character.isJavaIdentifierPart(source[from])) {
													from++;
												}
												if (from < to && source[from] == '>') {
													final String	groupName = new String(source,start,from-start); 
															
													from = parse(source,from + 1,to,nested,groups,TERM_GROUP);
													if (from < to && source[from] == ')') {
														final String[]	groupIds = new String[]{String.valueOf(groups.size()),groupName}; 
														final Command	group = new GroupCommand(nested.toArray(new Command[nested.size()]),groupIds);
														
														list.add(group);
														groups.put(groupIds,group);
														nested.clear();
														from++;
													}
													else {
														throw new PatternSyntaxException("Unpaired brackets: ')' is missing",new String(source),from);
													}
												}
												else {
													throw new PatternSyntaxException("Unpaired brackets: '>' is missing",new String(source),from);
												}
										}										
									}
									else {
										throw new PatternSyntaxException("Truncated pattern",new String(source),from);
									}
									break;
								case ':' :
									from = parse(source,from + 2,to,nested,groups,TERM_GROUP);
									if (from < to && source[from] == ')') {
										list.add(new SequenceCommand(nested.toArray(new Command[nested.size()])));
										nested.clear();
										from++;
									}
									else {
										throw new PatternSyntaxException("Unpaired brackets: ')' is missing",new String(source),from);
									}
									break;
								case '=' :
									from = parse(source,from + 3,to,nested,groups,TERM_GROUP);
									if (from < to && source[from] == ')') {
										list.add(new LookAheadCommand(new SequenceCommand(nested.toArray(new Command[nested.size()])),false,false));
										nested.clear();
										from++;
									}
									else {
										throw new PatternSyntaxException("Unpaired brackets: ')' is missing",new String(source),from);
									}
									break;
								case '>' : 
									from = parse(source,from + 3,to,nested,groups,TERM_GROUP);
									if (from < to && source[from] == ')') {
										list.add(new SequenceCommand(nested.toArray(new Command[nested.size()])));
										nested.clear();
										from++;
									}
									else {
										throw new PatternSyntaxException("Unpaired brackets: ')' is missing",new String(source),from);
									}
									break;
								case '!' : 
									from = parse(source,from + 3,to,nested,groups,TERM_GROUP);
									if (from < to && source[from] == ')') {
										list.add(new LookAheadCommand(new SequenceCommand(nested.toArray(new Command[nested.size()])),true,false));
										nested.clear();
										from++;
									}
									else {
										throw new PatternSyntaxException("Unpaired brackets: ')' is missing",new String(source),from);
									}
									break;
								case '-' : case 'i' : case 'd' : case 'm' : case 's' : case 'u' :  case 'x' :  case 'U' :
									int	andFlags = ~0, orFlags = 0;
									
									from += 3;
									while (from < to && !(source[from] == '-' || source[from] == ':' || source[from] == ')')) {
										orFlags |= toFlag(source,from++);
									}
									if (from < to && source[from] == '-') {
										from++;
										while (from < to && !(source[from] == ':' || source[from] == ')')) {
											andFlags &= ~toFlag(source,from++);
										}
										if (from < to && source[from] == ':') {
											from = parse(source,from+1,to,nested,groups,TERM_GROUP);
											if (from < to && source[from] == ')') {
												final Command	group = new FlagsCommand(new SequenceCommand(nested.toArray(new Command[nested.size()])),andFlags,orFlags);
												
												list.add(group);
												nested.clear();
												from++;
											}
											else {
												throw new PatternSyntaxException("Unpaired brackets: ')' is missing",new String(source),from);
											}
										}
										else if (from < to && source[from] == ')') {
											list.add(new FlagsCommand(andFlags,orFlags));
											from++;
										}
										else {
											throw new PatternSyntaxException("Unpaired brackets: ')' is missing",new String(source),from);
										}
									}
									else if (from < to && source[from] == ')') {
										list.add(new FlagsCommand(andFlags,orFlags));
										from++;
									}
									else {
										throw new PatternSyntaxException("Unpaired brackets: ')' is missing",new String(source),from);
									}
									break;
								default :
									throw new PatternSyntaxException("Unwaited char ["+source[from]+"] inside the group",new String(source),from);
							}
						}
					}
					else {
						final List<Command>		nested = new ArrayList<>();
					
						from = parse(source,from,to,nested,groups,TERM_GROUP);
						if (from < to && source[from] == ')') {
							final String[]	groupIds = new String[]{String.valueOf(groups.size()),""}; 
							final Command	group = new GroupCommand(nested.toArray(new Command[nested.size()]),groupIds);
							
							list.add(group);
							groups.put(groupIds,group);
							nested.clear();
							from++;
						}
						else {
							throw new PatternSyntaxException("Unpaired brackets: ')' is missing",new String(source),from);
						}
					}
					break;
				default :
					if (isTerminal(source[from],terminals)) {
						return from;
					}
					else {
						list.add(new CharCommand(source[from]));
						from++;
					}
			}
		}
		return 0;
	}

	private static BitCharSet buildUnicodeBlock(final String group) {
		for (Pattern p : UNICODE_BLOCKS) {
			final Matcher	m = p.matcher(group);
			
			if (m.matches()) {
				try{final String		blockName = m.group(1);
					final UnicodeBlock	block = UnicodeBlock.forName(blockName);
					
					return ExtendedBitCharSet.buildCharSet((symbol)->{return UnicodeBlock.of(symbol).equals(block);});		
				} catch (PatternSyntaxException exc) {
				}
			}
		}
		
		for (Pattern p : UNICODE_SCRIPTS) {
			final Matcher	m = p.matcher(group);
			
			if (m.matches()) {
				try{final String		scriptName = m.group(1);
					final UnicodeScript	script = UnicodeScript.forName(scriptName);
					
					return ExtendedBitCharSet.buildCharSet((symbol)->{return UnicodeScript.of(symbol).equals(script);});		
				} catch (PatternSyntaxException exc) {
				}
			}
		}
		
		for (Pattern p : UNICODE_CATEGORIES) {
			final Matcher	m = p.matcher(group);
			
			if (m.matches()) {
				try{final String		categoryName = m.group(1);

					return get(categoryName.toCharArray(),0,categoryName.length());					
				} catch (PatternSyntaxException exc) {
				}
			}
		}
		
		return null;
	}
	
	private static int buildCharSet(final char[] source, int from, final int to, final BitCharSet set, final char[] terminals) {
		final char[]		result = new char[1];
		final BitCharSet[]	resultCharSet = new BitCharSet[1];
		boolean				negation = false;
		
		if (from < to && source[from] == '^') {
			negation = true;
			from++;
		}
		while (from < to && !isTerminal(source[from],terminals)) {
			if (source[from] == '\\') {
				int		newFrom = parseEscapedChar(source,from+1,to,result);
				
				if (newFrom > from+1) {
					from = newFrom;
					set.add(result[0]);
				}
				else {
					newFrom = parseEscapedCharSet(source,from+1,to,resultCharSet);
					
					if (newFrom > from + 1) {
						from = newFrom;
						set.union(resultCharSet[0]);
						break;
					}
					else {
						throw new PatternSyntaxException("Unparsed escape sequence",new String(source),from);
					}
				}
			}
			else {
				final char	symbol = source[from++];
				
				if (from < to && source[from] == '-') {
					from++;
					if (from < to) {
						set.addRange(symbol,source[from++]);
					}
					else {
						throw new PatternSyntaxException("Truncated pattern",new String(source),from);
					}
				}
				else {
					set.add(symbol);
				}
			}
		}
		if (negation) {
			set.invert();
		}
		if (from < to) {
			switch (source[from]) {
				case '[' :
					final BitCharSet	union = new ExtendedBitCharSet();
					
					from = buildCharSet(source,from+1,to,union,terminals);
					if (from < to && source[from] == ']') {
						set.union(union);
						return ++from;
					}
					else {
						throw new PatternSyntaxException("Unpaired brackets: ']' is missing",new String(source),from);
					}
				case '&' :
					if (from < to - 1 && source[from + 1] == '&') {
						final BitCharSet	intersect = new ExtendedBitCharSet();
						
						from = buildCharSet(source,from+3,to,intersect,terminals);
						if (from < to && source[from] == ']') {
							set.intersect(intersect);
							return ++from;
						}
						else {
							throw new PatternSyntaxException("Unpaired brackets: ']' is missing",new String(source),from);
						}
					}
					else {
						throw new PatternSyntaxException("Unwaited char ["+source[from]+"] inside the set",new String(source),from);
					}
				case ']' :
					return from + 1;
				default :
					throw new PatternSyntaxException("Unwaited char ["+source[from]+"] inside the set",new String(source),from);
			}
		}
		else {
			return from;
		}
	}

	private static int buildEscaped(final char[] source, int from, final int to, final List<Command> list, final Map<String[],Command> groups, final boolean restricted) {
		int	start;		
	
		switch (source[from]) {
			case 'd' : case 'D' : case 's' : case 'S' : case 'w' : case 'W' : case 'p' : case 'b' : case 'B' : case 'A' : case 'z' : case 'Z' :
				final BitCharSet[]	resultCharSet = new BitCharSet[1];
				
				from = parseEscapedCharSet(source,from,to,resultCharSet);
				list.add(new CharSetCommand(resultCharSet[0]));
				return from;
			case 'Q' :
				if (!restricted) {
					start = from += 2;
					while (from < to - 1 && !(source[from] == '\\' && source[from + 1] == 'E')) {
						list.add(new CharCommand(source[from++]));
					}
					if ((source[from] == '\\' && source[from + 1] == 'E')) {
						return from += 2;
					}
					else {
						throw new PatternSyntaxException("Truncated pattern",new String(source),from);
					}
				}
				else {
					throw new PatternSyntaxException("Illegal using of the valid item here",new String(source),from);
				}
			case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
				if (!restricted) {
					int	ref = 0;
					
					while (from < to && source[from] >= '0' && source[from] <= '9') {
						ref = 10 * ref + source[from++] - '0';
					}
					if (ref > groups.size()) {
						throw new PatternSyntaxException("Reference to missing group",new String(source),from);
					}
					else {
						boolean	found = false;
						
						for (Entry<String[], Command> item : groups.entrySet()) {
							if (Integer.valueOf(item.getKey()[0]) == ref) {
								list.add(item.getValue());
								found = true;
								break;
							}
						}
						if (!found) {
							throw new PatternSyntaxException("Reference to missing group",new String(source),from);
						}
						else {
							return from;
						}
					}
				}
				else {
					throw new PatternSyntaxException("Illegal using of the valid item here",new String(source),from);
				}
			case 'k' :
				if (!restricted) {
					if (from < to - 2 && source[from + 2] == '<') {
						start = from;
						for (from = from + 2; from < to && source[from] != '>'; from++) {
						}
						if (source[from] == '>') {
							final String	group = new String(source,start,from);
							boolean	found = false;
							
							for (Entry<String[], Command> item : groups.entrySet()) {
								if (item.getKey()[1].equals(group)) {
									list.add(item.getValue());
									found = true;
								}
							}
							if (!found) {
								throw new PatternSyntaxException("Reference to missing group",new String(source),from);
							}
							else {
								return from;
							}
						}
						else {
							throw new PatternSyntaxException("Unpaired brackets: '>' is missing",new String(source),from);
						}
					}
					else {
						throw new PatternSyntaxException("Unpaired brackets: '<' is missing",new String(source),from);
					}
				}
				else {
					throw new PatternSyntaxException("Illegal using of the valid item here",new String(source),from);
				}
			default :
				final char[]	result = new char[1];
				
				from = parseEscapedChar(source,from,to,result);
				list.add(new CharCommand(result[0]));
				return from;
		}
	}

	private static int parseEscapedChar(final char[] source, int from, final int to, final char[] result) {
		int	digits;		
		
		switch (source[from]) {
			case '\\' :
				result[0] = '\\';
				return from + 1;
			case '0' :
				int	octal = 0;
				
				digits = 3;								
				while (digits > 0 && from < to && source[from] >= '0' && source[from] <= '7') {
					octal = 8 * octal + source[from++] - '0';
					digits--;
				}
				if (octal > 0377) {
					throw new PatternSyntaxException("Too big octal value",new String(source),from);
				}
				else {
					result[0] = (char)octal;
					return from;
				}
			case 'x' : case 'X' :
				if (from < to - 2 && source[from + 2] == '{') {
					int	unicode = 0;
					
					from += 2;
					while (from < to && (source[from] >= '0' && source[from] <= '9' || source[from] >= 'a' && source[from] <= 'z' || source[from] >= 'A' && source[from] <= 'Z')) {
						if (source[from] >= '0' && source[from] <= '9') {
							unicode =  16 * unicode + source[from++] - '0';
						}
						else if (source[from] >= 'A' && source[from] <= 'Z') {
							unicode =  16 * unicode + source[from++] + 10 - 'A';
						}
						else {
							unicode =  16 * unicode + source[from++] + 10 - 'a';
						}
					}
					if (from < to && source[from] == '}') {
						from++;
					}
					else {
						throw new PatternSyntaxException("Unpaired brackets: '}' is missing",new String(source),from);
					}
					if (unicode > Character.MAX_VALUE) {
						throw new PatternSyntaxException("Too big hex value",new String(source),from);
					}
					else {
						result[0] = (char)unicode;
						return from;
					}
				}
				else {
					int	unicode = 0;
					
					from += 2;
					digits = 2;
					while (digits > 0 && from < to && (source[from] >= '0' && source[from] <= '9' || source[from] >= 'a' && source[from] <= 'z' || source[from] >= 'A' && source[from] <= 'Z')) {
						if (source[from] >= '0' && source[from] <= '9') {
							unicode =  16 * unicode + source[from++] - '0';
						}
						else if (source[from] >= 'A' && source[from] <= 'Z') {
							unicode =  16 * unicode + source[from++] + 10 - 'A';
						}
						else {
							unicode =  16 * unicode + source[from++] + 10 - 'a';
						}
						digits--;
					}
					if (from < to && source[from] == '}') {
						from++;
					}
					else {
						throw new PatternSyntaxException("Unpaired brackets: '}' is missing",new String(source),from);
					}
					if (unicode > Character.MAX_VALUE) {
						throw new PatternSyntaxException("Too big hex value",new String(source),from);
					}
					else {
						result[0] = (char)unicode;
						return from;
					}
				}
			case 'u' : case 'U' :
				int	unicode = 0;
				
				from += 2;
				digits = 4;
				while (digits > 0 && from < to && (source[from] >= '0' && source[from] <= '9' || source[from] >= 'a' && source[from] <= 'z' || source[from] >= 'A' && source[from] <= 'Z')) {
					if (source[from] >= '0' && source[from] <= '9') {
						unicode =  16 * unicode + source[from++] - '0';
					}
					else if (source[from] >= 'A' && source[from] <= 'Z') {
						unicode =  16 * unicode + source[from++] + 10 - 'A';
					}
					else {
						unicode =  16 * unicode + source[from++] + 10 - 'a';
					}
					digits--;
				}
				if (unicode > Character.MAX_VALUE) {
					throw new PatternSyntaxException("Too big Unicode value",new String(source),from);
				}
				else {
					result[0] = (char)unicode;
					return from;
				}
			case 't' :
				result[0] = '\u0009';
				return from+1;
			case 'n' :
				result[0] = '\n';
				return from+1;
			case 'r' :
				result[0] = '\r';
				return from+1;
			case 'f' :
				result[0] = '\u000C';
				return from+1;
			case 'a' :
				result[0] = '\u0007';
				return from+1;
			case 'e' :
				result[0] = '\u001B';
				return from+1;
			case 'c' :
				return from;
			default :
				result[0] = source[from];
				return from+1;
		}
	}	

	private static int parseEscapedCharSet(final char[] source, int from, final int to, final BitCharSet[] result) {
		int	start;		
		
		switch (source[from]) {
			case 'd' :
				result[0] = CS_PLUS_DIGIT;
				return from + 1;
			case 'D' :
				result[0] = CS_MINUS_DIGIT;
				return from + 1;
			case 's' :
				result[0] = CS_PLUS_SPACE;
				return from + 1;
			case 'S' :
				result[0] = CS_MINUS_SPACE;
				return from + 1;
			case 'w' :
				result[0] = CS_PLUS_WORD;
				return from + 1;
			case 'W' :
				result[0] = CS_MINUS_WORD;
				return from + 1;
			case 'p' :
				if (from < to - 2 && source[from + 2] == '{') {
					start = from + 2;
					for (from = from + 2; from < to && source[from] != '}'; from++) {
					}
					if (source[from] == '}') {
						final BitCharSet	found = get(source,start,from-1);
						
						if (found == null) {
							final BitCharSet	special = buildUnicodeBlock(new String(source,start,from-1-start));
							
							if (special == null) {
								throw new PatternSyntaxException("Unknown block/category/script/property reference",new String(source),from);
							}
							else {
								result[0] = special;
								return from + 1;
							}
						}
						else {
							result[0] = found;
							return from + 1;
						}
					}
					else {
						throw new PatternSyntaxException("Unpaired brackets: '}' is missing",new String(source),from);
					}
				}
			case 'b' :
				result[0] = CS_MINUS_WORD;
				return from + 1;
			case 'B' :
				result[0] = CS_MINUS_WORD;
				return from + 1;
			case 'A' :
				result[0] = CS_MINUS_WORD;
				return from + 1;
			case 'z' :
				result[0] = CS_MINUS_WORD;
				return from + 1;
			case 'Z' :
				result[0] = CS_MINUS_WORD;
				return from + 1;
			default :
				return from;
		}
	}

	private static int toFlag(final char[] source, final int from) {
		switch (source[from]) {
			case 'i' :	
				return (1 << BIT_i);
			case 'd' : 
				return (1 << BIT_d);
			case 'm' : 
				return (1 << BIT_m);
			case 's' : 
				return (1 << BIT_s);
			case 'u' :  
				return (1 << BIT_u);
			case 'x' :  
				return (1 << BIT_x);
			case 'U' :
				return (1 << BIT_U);
			default :
				throw new PatternSyntaxException("Unknown flag ["+source[from]+"]",new String(source),from);
		}
	}
	
	private static boolean isTerminal(final char symbol, final char[] terminals) {
		for (char item : terminals) {
			if (symbol == item) {
				return true;
			}
		}
		return false;
	}

	private static BitCharSet get(final char[] source, final int from, final int to) {
		final long	id = PREDEFINED.seekName(source,from,to);
		
		if (id >= 0) {
			return PREDEFINED.getCargo(id);
		}
		else {
			return null;
		}
	}

	private static boolean match(final Command command, final char[] content, final int from, final int len) {
		@SuppressWarnings("unused")
		int		newFrom;
	
		try{command.prepare();
			do {if ((newFrom = command.firstCall(content,from,len,0)) > 0) {
					return true;
				}
			} while (command.backtrace());
			command.lastCall();
			return false;
		} finally {
			command.unprepare();
		}
	}

	static Command compile(final Command command) throws IOException, SyntaxException {
		final GrowableCharArray		gca = new GrowableCharArray(true);
		final String				className = "Command"+uniqueClassId.incrementAndGet();
		
		try(final InputStream		is = FastPattern.class.getResourceAsStream("fastpatterntemplates.txt")) {
			final AssemblerTemplateRepo		repo = new AssemblerTemplateRepo(is);
			
			try(final CodeBuilder	cb = new CodeBuilder(repo,className,gca,false)) {
				final int[]			uniqueId = new int[]{1};
				
				command.walk((cmd,cargo)->{cmd.assignUniqueId(cargo[0]++);},uniqueId);
				
				command.prepare(cb);
				command.firstCall(cb);
				command.backtrace(cb);
				command.lastCall(cb);
				command.unprepare(cb);
			}
			
			System.err.println("Ready:\n"+new String(gca.extract()));
			try(final Reader				rdr = gca.getReader()) {
				@SuppressWarnings("unchecked")
				final Class<Command>		clazz = (Class<Command>)new ClassLoaderWrapper().createClass("chav1961.purelib.basic."+className,rdr);
				final Constructor<Command>	c = clazz.getDeclaredConstructor();
				
				c.setAccessible(true);
				return (Command)c.newInstance();
			} catch (InstantiationException | IllegalAccessException | NullPointerException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new IOException(e.getLocalizedMessage(),e); 
			}
		} finally {
			gca.clear();
		}
	}

	private enum CommandType {
		CHAR_COMMAND, EXTENDEDBITCHARSET_COMMAND, REPEATABLE_COMMAND, QUANTOR_COMMAND, LOOKAHEAD_COMMAND, SEQUENCE_COMMAND, FLAG_COMMAND, GROUP_COMMAND
	}
	
	@FunctionalInterface
	private interface CommandWalkCallback<Cargo> {
		void processCommand(Command command, Cargo cargo);
	}
	
	private abstract static class Command implements Cloneable, CharSubstitutionSource {
		protected static final char[]	TEMPL_PACKAGE = "package".toCharArray();
		protected static final char[]	TEMPL_TYPE = "type".toCharArray();
		protected static final char[]	TEMPL_SUBCLASS = "subclass".toCharArray();
		protected static final char[]	TEMPL_FIRST_CALL = "firstCall".toCharArray();
		protected static final char[]	TEMPL_BACKTRACE = "backtrace".toCharArray();

		private int	uniqueId = 0;
		
		protected Command() {}
		
		protected Command(final Command another) {}
		
		@Override
		protected abstract Command clone();
		abstract CommandType getType();
		abstract int firstCall(char[] content, int from, int length, int flags);
		abstract boolean backtrace();
		abstract int firstCall(CodeBuilder builder);
		abstract boolean backtrace(CodeBuilder builder);
		abstract boolean canUseInline();
		abstract <Cargo> void walk(final CommandWalkCallback<Cargo> callback, final Cargo cargo);
		
		void assignUniqueId(final int uniqueId) {
			this.uniqueId = uniqueId;
		}
		
		int getUniqueId() {
			return uniqueId;
		}
		
		void prepare(){}
		void lastCall(){}
		void unprepare(){}

		void prepare(CodeBuilder builder){}
		void lastCall(CodeBuilder builder){}
		void unprepare(CodeBuilder builder){}

		public char[] getValue(final char[] data, final int from, final int to) {
			if (CharUtils.compare(data,from,TEMPL_TYPE)) {
				return getType().toString().toCharArray();
			}
			else {
				return null;
			}
		}
	}

	public abstract static class CompiledCommand extends Command {
		protected CompiledCommand() {}
		
		protected CompiledCommand(final CompiledCommand another) {}
		
		@Override
		int firstCall(CodeBuilder builder) {
			return 0;
		}
		
		@Override
		boolean backtrace(CodeBuilder builder) {
			return false;
		}

		@Override
		boolean canUseInline() {
			return false;
		}
	}
	
	
	private static class CharCommand extends Command {
		private final char	symbol;

		protected CharCommand(final CharCommand another) {
			super(another);
			this.symbol = another.symbol;
		}
		
		@Override
		protected CharCommand clone() {
			return new CharCommand(this);
		}
		
		CharCommand(final char symbol) {
			this.symbol = symbol;
		}

		@Override
		CommandType getType() {
			return CommandType.CHAR_COMMAND;
		}

		@Override
		int firstCall(final char[] content, final int from, final int length, final int flags) {
			if (content[from] == this.symbol) {
				return from+1;
			}
			else {
				return -from;
			}
		}

		@Override
		int firstCall(final CodeBuilder builder) {
			try{builder.getRepo().append(builder.getArray(),"compareChar".toCharArray(),builder.pushSubstitutionSource(new CharSubstitutionSource() {
						@Override
						public char[] getValue(char[] data, int from, int to) {
							if (CharUtils.compare(data,from,"char".toCharArray())) {
								return new char[]{symbol};
							}
							else {
								return CharCommand.super.getValue(data, from, to);
							}
						}
					}
				));
				return 0;
			} finally {
				builder.popSubstitutionSource();
			}
		}
		
		@Override
		boolean backtrace() {
			return false;
		}

		@Override
		boolean backtrace(final CodeBuilder builder) {
			builder.getRepo().append(builder.getArray()," .stack 1\n ldc 0\n ireturn\n");
			return false;
		}
		
		@Override
		boolean canUseInline() {
			return true;
		}

		@Override
		<Cargo> void walk(CommandWalkCallback<Cargo> callback, final Cargo cargo) {
			callback.processCommand(this, cargo);
		}
	}
	
	private static class CharSetCommand extends Command {
		private final BitCharSet	set;

		protected CharSetCommand(final CharSetCommand another) {
			super(another);
			this.set = another.set;
		}
		
		@Override
		protected CharSetCommand clone() {
			return new CharSetCommand(this);
		}
		
		CharSetCommand(final BitCharSet set) {
			this.set = set;
		}
		
		@Override
		CommandType getType() {
			return CommandType.EXTENDEDBITCHARSET_COMMAND;
		}
		
		@Override
		int firstCall(final char[] content, final int from, final int length, final int flags) {
			if (set.contains(content[from])) {
				return from+1;
			}
			else {
				return -from;
			}
		}

		@Override
		int firstCall(final CodeBuilder builder) {
			final int 	labelTrue = builder.getRandom(), labelFalse = builder.getRandom();
			
			builder.getRepo().append(builder.getArray()," .stack 6\n aload_1\n iload_2\n caload\n");
			
			for (char[] item : set.toArrayPairs()) {	// Build char ranges to compare
				if (item.length == 1) {
					builder.getRepo().append(builder.getArray()," dup\n ldc '%1$c'\n if_icmpeq label_%2$d\n",item[0],labelTrue);
				}
				else {
					builder.getRepo().append(builder.getArray()," dup\n ldc '%1$c'\n if_icmplt label_%2$d\n",item[0],labelFalse);
					builder.getRepo().append(builder.getArray()," dup\n ldc '%1$c'\n if_icmple label_%2$d\n",item[1],labelTrue);
				}
			}
			builder.getRepo().append(builder.getArray(),"label_%1$d: pop\n iload_2\n ineg\n ireturn\n",labelFalse);
			builder.getRepo().append(builder.getArray(),"label_%1$d: pop\n iload_2\n ldc 1\n iadd\n ireturn\n",labelTrue);
			return 0;
		}
		
		@Override
		boolean backtrace() {
			return false;
		}

		@Override
		boolean backtrace(final CodeBuilder builder) {
			builder.getRepo().append(builder.getArray()," .stack 1\n ldc 0\n ireturn\n");
			return false;
		}
		
		@Override
		boolean canUseInline() {
			return true;
		}
		
		@Override
		<Cargo> void walk(CommandWalkCallback<Cargo> callback, final Cargo cargo) {
			callback.processCommand(this, cargo);
		}
	}
	
	private static class RepeatableCommand extends Command {
		private final Command	nested;
		private final int		minOccurs, maxOccurs;

		RepeatableCommand(final Command nested, final int minOccurs, final int maxOccurs) {
			this.nested = nested;
			this.minOccurs = minOccurs;
			this.maxOccurs = maxOccurs;
		}

		protected RepeatableCommand(final RepeatableCommand another) {
			super(another);
			this.nested = another.nested;
			this.minOccurs = another.minOccurs;
			this.maxOccurs = another.maxOccurs;
		}
		
		@Override
		protected RepeatableCommand clone() {
			return new RepeatableCommand(this);
		}
		
		@Override
		CommandType getType() {
			return CommandType.REPEATABLE_COMMAND;
		}
		
		@Override
		int firstCall(final char[] content, int from, final int length, final int flags) {
			final int	lastPos = from;
			int			amount;
			
			for (amount = 0; amount < Integer.MAX_VALUE; amount++) {
				if (nested.firstCall(content, from, length, flags) == from) {
					break;
				}
			}
			if (amount >= minOccurs && amount <= maxOccurs) {
				return from;
			}
			else {
				return -lastPos;
			}
		}

		@Override
		int firstCall(final CodeBuilder builder) {
			return 0;
		}
		
		@Override
		boolean backtrace() {
			return false;
		}

		@Override
		boolean backtrace(final CodeBuilder builder) {
			return false;
		}
		
		@Override
		boolean canUseInline() {
			return true;
		}
	
		@Override
		<Cargo> void walk(CommandWalkCallback<Cargo> callback, final Cargo cargo) {
			callback.processCommand(this, cargo);
			nested.walk(callback, cargo);
		}
	}

	private static class QuantorCommand extends Command {
		private final Command	nested;
		private final boolean	optional;

		QuantorCommand(final Command nested, final boolean optional) {
			this.nested = nested;
			this.optional = optional;
		}

		protected QuantorCommand(final QuantorCommand another) {
			super(another);
			this.nested = another.nested;
			this.optional = another.optional;
		}
		
		@Override
		protected QuantorCommand clone() {
			return new QuantorCommand(this);
		}
		
		@Override
		CommandType getType() {
			return CommandType.QUANTOR_COMMAND;
		}
				
		@Override
		int firstCall(final char[] content, int from, final int length, final int flags) {
			final int	lastPos = from;
			int			amount;
			
			for (amount = 0; amount < Integer.MAX_VALUE; amount++) {
				if (nested.firstCall(content, from, length, flags) == from) {
					break;
				}
			}
			return lastPos;
		}

		@Override
		int firstCall(final CodeBuilder builder) {
			return 0;
		}
		
		@Override
		boolean backtrace() {
			return false;
		}

		@Override
		boolean backtrace(final CodeBuilder builder) {
			return false;
		}
		
		@Override
		boolean canUseInline() {
			return true;
		}
		
		@Override
		<Cargo> void walk(CommandWalkCallback<Cargo> callback, final Cargo cargo) {
			callback.processCommand(this, cargo);
			nested.walk(callback, cargo);
		}
	}

	private static class LookAheadCommand extends Command {
		private final Command	nested;
		private final boolean	negation,direction;

		LookAheadCommand(final Command nested, final boolean negation, final boolean direction) {
			this.nested = nested;
			this.negation = negation;
			this.direction = direction;
		}

		protected LookAheadCommand(final LookAheadCommand another) {
			super(another);
			this.nested = another.nested;
			this.negation = another.negation;
			this.direction = another.direction;
		}
		
		@Override
		protected LookAheadCommand clone() {
			return new LookAheadCommand(this);
		}
		
		@Override
		CommandType getType() {
			return CommandType.LOOKAHEAD_COMMAND;
		}
				
		@Override
		int firstCall(final char[] content, int from, final int length, final int flags) {
			final int	lastPos = from, newPos;
			
			if (direction) {
				if ((newPos = nested.firstCall(content,from,length,flags)) == lastPos && negation) {
					return newPos;
				}
				else {
					return -lastPos;
				}
			}
			else {
				if ((newPos = nested.firstCall(content,from,length,flags)) == lastPos && negation) {
					return newPos;
				}
				else {
					return -lastPos;
				}
			}
		}

		@Override
		int firstCall(final CodeBuilder builder) {
			return 0;
		}
		
		@Override
		boolean backtrace() {
			return false;
		}

		@Override
		boolean backtrace(final CodeBuilder builder) {
			return false;
		}
		
		@Override
		boolean canUseInline() {
			return true;
		}
	
		@Override
		<Cargo> void walk(CommandWalkCallback<Cargo> callback, final Cargo cargo) {
			callback.processCommand(this, cargo);
			nested.walk(callback, cargo);
		}
	}

	private static class SequenceCommand extends Command {
		private final Command[]	nested;
		
		SequenceCommand(final Command[] nested) {
			this.nested = nested;
		}

		protected SequenceCommand(final SequenceCommand another) {
			super(another);
			this.nested = another.nested;
		}
		
		@Override
		protected SequenceCommand clone() {
			return new SequenceCommand(this);
		}
		
		@Override
		CommandType getType() {
			return CommandType.SEQUENCE_COMMAND;
		}
				
		@Override
		int firstCall(final char[] content, int from, final int length, final int flags) {
			@SuppressWarnings("unused")
			final int	lastPos = from, lastFlags = 0;
			
			for (int index = 0, maxIndex = nested.length; index < maxIndex; index++) {
				if ((from = nested[index].firstCall(content,from,length,flags)) < 0) {
					return -lastPos;
				}
			}
			return from;
		}

		@Override
		boolean backtrace() {
			return false;
		}

		@Override
		void prepare(final CodeBuilder builder){
			builder.getRepo().append(builder.getArray(),CodeBuilder.SUBCLASS_PART,builder);
		}
		
		@Override
		int firstCall(final CodeBuilder builder) {
			for (int index = 0, maxIndex = nested.length; index < maxIndex; index++) {
				if (nested[index].canUseInline()) {
					nested[index].firstCall(builder);
				}
				else {
					throw new UnsupportedOperationException();
				}
			}
			try{builder.getRepo().append(builder.getArray(),CodeBuilder.FIRST_CALL_PART,builder.pushSubstitutionSource(this));
			} finally {
				builder.popSubstitutionSource();
			}
			return -1;
		}
		
		@Override
		boolean backtrace(final CodeBuilder builder) {
			for (int index = 0, maxIndex = nested.length; index < maxIndex; index++) {
				if (nested[index].canUseInline()) {
					nested[index].backtrace(builder);
				}
				else {
					throw new UnsupportedOperationException();
				}
			}
			builder.getRepo().append(builder.getArray(),CodeBuilder.BACKTRACE_PART,builder);
			return false;
		}
		
		@Override
		void unprepare(CodeBuilder builder){
			builder.getRepo().append(builder.getArray(),CodeBuilder.THEEND_PART,builder);
		}
		
		@Override
		boolean canUseInline() {
			return true;
		}

		@Override
		<Cargo> void walk(CommandWalkCallback<Cargo> callback, final Cargo cargo) {
			callback.processCommand(this, cargo);
			for (Command item : nested) {
				item.walk(callback, cargo);
			}
		}
	}
	
	private static class FlagsCommand extends Command {
		private final Command	nested;
		private final int		andFlags, orFlags;
		
		FlagsCommand(final int andFlags, final int orFlags) {
			this.nested = null;
			this.andFlags = andFlags;
			this.orFlags = orFlags;
		}
		
		FlagsCommand(final Command nested, final int andFlags, final int orFlags) {
			this.nested = nested;
			this.andFlags = andFlags;
			this.orFlags = orFlags;
		}

		protected FlagsCommand(final FlagsCommand another) {
			super(another);
			this.nested = another.nested;
			this.andFlags = another.andFlags;
			this.orFlags = another.orFlags;
		}
		
		@Override
		protected FlagsCommand clone() {
			return new FlagsCommand(this);
		}
		
		@Override
		CommandType getType() {
			return CommandType.FLAG_COMMAND;
		}
				
		@Override
		int firstCall(final char[] content, int from, final int length, final int flags) {
			if (nested == null) {
				return from;
			}
			else {
				return nested.firstCall(content,from,length,(flags & andFlags) | orFlags);
			}
		}

		@Override
		int firstCall(final CodeBuilder builder) {
			return 0;
		}
		
		@Override
		boolean backtrace() {
			return false;
		}

		@Override
		boolean backtrace(final CodeBuilder builder) {
			return false;
		}
		
		@Override
		boolean canUseInline() {
			return true;
		}

		@Override
		<Cargo> void walk(CommandWalkCallback<Cargo> callback, final Cargo cargo) {
			callback.processCommand(this, cargo);
			nested.walk(callback, cargo);
		}
	}

	private static class GroupCommand extends Command {
		private final Command[]	nested;
		private final String[]	groupIds;
		
		GroupCommand(final Command[] nested, final String[] groupIds) {
			this.nested = nested;
			this.groupIds = groupIds;
		}

		protected GroupCommand(final GroupCommand another) {
			super(another);
			this.nested = another.nested;
			this.groupIds = another.groupIds;
		}
		
		@Override
		protected GroupCommand clone() {
			return new GroupCommand(this);
		}
		
		@Override
		CommandType getType() {
			return CommandType.GROUP_COMMAND;
		}
				
		@Override
		int firstCall(final char[] content, int from, final int length, final int flags) {
			@SuppressWarnings("unused")
			final int	lastPos = from, lastFlags = 0;
			
			for (int index = 0, maxIndex = nested.length; index < maxIndex; index++) {
				if ((from = nested[index].firstCall(content,from,length,flags)) < 0) {
					return -from;
				}
			}
			return from;
		}

		@Override
		int firstCall(final CodeBuilder builder) {
			return 0;
		}
		
		@Override
		boolean backtrace() {
			return false;
		}

		@Override
		boolean backtrace(final CodeBuilder builder) {
			return false;
		}
		
		@Override
		boolean canUseInline() {
			return true;
		}

		@Override
		<Cargo> void walk(CommandWalkCallback<Cargo> callback, final Cargo cargo) {
			callback.processCommand(this, cargo);
			for (Command item : nested) {
				item.walk(callback, cargo);
			}
		}
	}

	static class CodeBuilder implements AutoCloseable, CharSubstitutionSource {
		static final char[]						SUBCLASS_PART = "subclass".toCharArray();
		static final char[]						FIRST_CALL_PART = "firstCall".toCharArray();
		static final char[]						BACKTRACE_PART = "backtrace".toCharArray();
		static final char[]						THEEND_PART = "theEnd".toCharArray();
		static final char[]						TEMPL_PACKAGE = "package".toCharArray();
		static final char[]						TEMPL_SUBCLASS = "subclass".toCharArray();
		
		private final AssemblerTemplateRepo					repo;
		private final String						className;
		private final boolean						needPrologAndEpilog;
		private final GrowableCharArray				gca; 
		private final Map<String,CodeBuilder>		advancedMethods = new HashMap<>();
		private final List<CharSubstitutionSource>	subst = new ArrayList<>();
		
		CodeBuilder(final AssemblerTemplateRepo repo, final String className, final GrowableCharArray gca) {
			this(repo,className,gca,true);
		}

		protected CodeBuilder(final AssemblerTemplateRepo repo, final String className, final GrowableCharArray gca, final boolean needPrologAndEpilog) {
			this.repo = repo;
			this.className = className;
			this.gca = gca;
			this.needPrologAndEpilog = needPrologAndEpilog;
			if (needPrologAndEpilog) {
				getRepo().append(getArray(),SUBCLASS_PART,this);
			}
			this.subst.add(new CharSubstitutionSource() {
				@Override
				public char[] getValue(char[] data, int from, int to) {
					if (CharUtils.compare(data,from,TEMPL_PACKAGE)) {
						return "chav1961.purelib.basic".toCharArray();
					}
					else if (CharUtils.compare(data,from,TEMPL_SUBCLASS)) {
						return getClassName().toCharArray();
					}
					else {
						return null;
					}
				}
			});
		}
		
		CodeBuilder appendMethod(final String name) {
			if (needPrologAndEpilog) {
				return new CodeBuilder(getRepo(),getClassName(),getArray(),false);
			}
			else {
				throw new IllegalStateException("Can't create nested methods!"); 
			}
		}
		
		GrowableCharArray getArray() {
			return gca;
		}
		
		AssemblerTemplateRepo getRepo() {
			return repo;
		}

		String getClassName() {
			return className; 
		}
		
		int getRandom() {
			return (int) (Integer.MAX_VALUE * Math.random());
		}
		
		@Override
		public void close() throws IOException {
			if (needPrologAndEpilog) {
				for (Entry<String, CodeBuilder> item : advancedMethods.entrySet()) {
					getArray().append(item.getValue().getArray().extract());
					item.getValue().close();
				}
				getRepo().append(getArray(),THEEND_PART,this);
			}
			if (subst.size() != 1) {
				throw new IllegalStateException("Unpaired calls for pushSubstitutionSource(...)/popSubstitutionSource()");
			}
			else {
				subst.clear();
			}
		}

		@Override
		public char[] getValue(final char[] data, final int from, final int to) {
			char[] result;
			
			for (CharSubstitutionSource item : subst) {
				if ((result = item.getValue(data, from, to)) != null) {
					return result;
				}
			}
			throw new IllegalArgumentException("Unknown substitution ["+new String(data,from,to-from)+"] in the code builder!");
		}
		
		CodeBuilder pushSubstitutionSource(final CharSubstitutionSource css) {
			subst.add(0,css);
			return this;
		}
		
		void popSubstitutionSource() {
			if (subst.size() > 1) {
				subst.remove(0);
			}
			else {
				throw new IllegalStateException("Unpaired calls for pushSubstitutionSource(...)/popSubstitutionSource()");
			}
		}
	}
}
