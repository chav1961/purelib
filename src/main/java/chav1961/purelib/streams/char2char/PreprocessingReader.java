package chav1961.purelib.streams.char2char;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.i18n.internal.PureLibLocalizer;

/**
 * <p>This class implements preprocessing for the reader nested. It supports a set of preprocessor operators:</p>
 * <ul>
 * <li><b>#define</b> and <b>#undef</b> - define and undefine global-known preprocessor variable(s)</li> 
 * <li><b>#if</b>, <b>#elseif</b>, <b>#else</b> and <b>#endif</b> - conditional preprocessing (can be nested)</li> 
 * <li><b>#include</b> - include additional character stream from the given URI (can be nested)</li> 
 * <li><b>#error</b> and <b>#warning</b> - fire error or warning with the given message</li> 
 * <li>substitutions of the preprocessor variables (can be recursive)</li> 
 * </ul>
 * <p>Lines to remove from input stream by preprocessing can be excluded from the input stream on can be commented in the input stream (by inline or multiline user-defined comment sequences)</p>
 * <p>Syntax of the preprocessor operators is:</p>
 * <p><code><b>#define</b> &lt;variable_name&gt; &lt;any_value_including_blank_chars&gt;</code></p>   
 * <p><code><b>#undef</b> &lt;variable_name&gt;</code></p>
 * <p><code><b>#if</b> &lt;expression&gt;</code></p>
 * <p><code>. . .</code></p>
 * <p><code><b>#elseif</b> &lt;expression&gt;</code></p>
 * <p><code>. . .</code></p>
 * <p><code><b>#else</b></code></p>
 * <p><code>. . .</code></p>
 * <p><code><b>#endif</b></code></p>
 * <p><code><b>#include</b> {"&lt;resource_uri&gt;"|&lt;resource_uri&gt;}</code></p>
 * <p><code><b>#warning</b> &lt;any text&gt;</code></p>
 * <p><code><b>#error</b> &lt;any text&gt;</code></p>
 * <p>Syntax of the preprocessor expression is:</p>
 * <p><code>&lt;expression&gt;::=&lt;or_expression&gt;[ <b>||</b> &lt;or_expression&gt;]</code></p>
 * <p><code>&lt;or_expression&gt;::=&lt;and_expression&gt;[ <b>&amp;&amp;</b> &lt;and_expression&gt;]</code></p>
 * <p><code>&lt;and_expression&gt;::=[ <b>!</b> ]&lt;comparison&gt;</code></p>
 * <p><code>&lt;comparison&gt;::={[<b>?</b>]&lt;variable&gt;|&lt;variable&gt;{<b>==</b>|<b>!=</b>|<b>&gt;=</b>|<b>&lt;=</b>|<b>&gt;</b>|<b>&lt;</b>}&lt;value&gt;|<b>(</b>&lt;expression&gt;<b>)</b>}</code></p>
 * <p><code>&lt;value&gt;::=<b>"</b>any_text_except_nl<b>"</b></code></p>
 * <p>To make control for the preprocessor, you can use a set of key-value pairs:</p>
 * <ul>
 * <li>{@link #BUFFER_SIZE} - size of the input buffer (default is 8192)</li>    
 * <li>{@link #HIDING_METHOD} - method of hiding lines, excluding on the preprocessor stage (see {@link #hidingMethod} enumeration)</li>    
 * <li>{@link #INLINE_SUBSTITUTION} - <b>true</b> means substitute of the preprocessor variable's values in the input stream</li>
 * <li>{@link #RECURSIVE_SUBSTITUTION} - <b>true</b> means processing of the values substituted earlier</li>
 * <li>{@link #IGNORE_CASE} - <b>true</b> ignores case of preprocessor commands and variables</li>
 * <li>{@link #ERROR_PROCESSING_CALLBACK} - an interface to process errors &amp; warnings detected. Default throws an {@link java.io.IOException} on errors and ignores warnings</li>
 * <li>{@link #COMMENT_SEQUENCE} - a list of comment sequences, splitted by \n and \t chars.</li>
 * </ul>    
 * <p>Example of the comment sequence for Java language is:</p>
 * <code>//\n/*\t*&#47;</code>
 * <p>The \n character splits one comment sequence from another. The \t character 'represents' content of the multi-line comments. Missing \t character 
 * inside the comment sequence is treated as inline comment sequence. Inline comment can be used as comment sequence inside the preprocessor operators.</p>
 * <p>You can use user-defined lambda-styled callback {@link IncludeCallback} to process #include statements (for example, to get it's content from database). Default uses a standard URL functionality to get content from.</p>
 * <p>The class in not thread-safe</p> 
 * @see java.io.Reader Reader
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.4
 */
public class PreprocessingReader extends AbstractPreprocessingReader {
	/**
	 * <p>Map key for value 'Ignore case of preprocessor commands'. It's value can be <b>true</b> or <b>false</b> only</p>
	 */
	public static final String				IGNORE_CASE = "__ignoreCase__";
	
	/**
	 * <p>Map key for value 'Character buffer size to read content from'. It's value is positive integer. Default is 8192.</p>
	 */
	public static final String				BUFFER_SIZE = "__bufferSize__";
	
	/**
	 * <p>Map key for value 'Hiding method to exclude source content on conditional preprocessing'. Can be instance of {@link HidingMethod} only</p>
	 */
	public static final String				HIDING_METHOD = "__hidingMethod__";
	
	/**
	 * <p>Map key for value 'Support inline substitution'. It's value can be <b>true</b> or <b>false</b> only. If <b>false</b> or missing, no preprocessor variables will be substituted in the input stream content. </p>
	 */
	public static final String				INLINE_SUBSTITUTION = "__inlineSubstitution__";
	
	/**
	 * <p>Map key for value 'Allow parsing of substituted content'. It's value can be <b>true</b> or <b>false</b> only. Default is <b>false</b></p>
	 */
	public static final String				RECURSIVE_SUBSTITUTION = "__recursiveSubstitution__";
	
	/**
	 * <p>Map key for value 'Callback to process #error and #warning preprocessor operators'. Can be implementation of {@link ErrorProcessingCallback} only</p>
	 */
	public static final String				ERROR_PROCESSING_CALLBACK = "__errorProcessingCallback__";
	
	/**
	 * <p>Map key for value 'Character sequence to use as inline and/or multi-line comments'. If presents, can be non-empty string only</p> 
	 */
	public static final String				COMMENT_SEQUENCE = "__commentSequence__";

	private enum CommandType {
		CMD_UNKNOWN, CMD_IF, CMD_ELSEIF, CMD_ELSE, CMD_ENDIF, CMD_DEFINE, CMD_UNDEF, CMD_ERROR, CMD_WARNING, CMD_INCLUDE
	}

	private enum ExprLevel {
		EL_OR, EL_AND, EL_NOT, EL_CMP
	}

	private enum ComparisonType {
		CMP_EQ, CMP_NE, CMP_LT, CMP_LE, CMP_GT, CMP_GE 
	}
	
	protected static final int			DEFAULT_BUFFER_SIZE = 8192;
	protected static final HidingMethod	DEFAULT_HIDING_METHOD = HidingMethod.EXCLUDE;
	protected static final boolean		DEFAULT_IGNORE_CASE = false;
	protected static final boolean		DEFAULT_INLINE_SUBSTITUTION = false;
	protected static final boolean		DEFAULT_RECURSIVE_SUBSTITUTION = false;

	protected static final int			MAX_DEPTH = 63;
	protected static final long			ENABLED_OUTPUT_MASK = 0xFFFFFFFFFFFFFFFFL;
	protected static final char[]			NL = {'\n'};

	private static SyntaxTreeInterface<CommandType>	COMMANDS = new AndOrTree<>();
	private static final Map<Class<?>,Class<?>>	WRAPPERS = new HashMap<>();
	
	static {
		COMMANDS.placeName((CharSequence)"#if",CommandType.CMD_IF);
		COMMANDS.placeName((CharSequence)"#elseif",CommandType.CMD_ELSEIF);
		COMMANDS.placeName((CharSequence)"#else",CommandType.CMD_ELSE);
		COMMANDS.placeName((CharSequence)"#endif",CommandType.CMD_ENDIF);
		COMMANDS.placeName((CharSequence)"#define",CommandType.CMD_DEFINE);
		COMMANDS.placeName((CharSequence)"#undef",CommandType.CMD_UNDEF);
		COMMANDS.placeName((CharSequence)"#error",CommandType.CMD_ERROR);
		COMMANDS.placeName((CharSequence)"#warning",CommandType.CMD_WARNING);
		COMMANDS.placeName((CharSequence)"#include",CommandType.CMD_INCLUDE);
		
		WRAPPERS.put(boolean.class,Boolean.class);
		WRAPPERS.put(byte.class,Byte.class);
		WRAPPERS.put(char.class,Character.class);
		WRAPPERS.put(double.class,Double.class);
		WRAPPERS.put(float.class,Float.class);
		WRAPPERS.put(int.class,Integer.class);
		WRAPPERS.put(long.class,Long.class);
		WRAPPERS.put(short.class,Short.class);
	}
	
	private volatile PreprocessingReader	delegate = null;
	private volatile URI					delegateURI = null;
	private long							outputMask = ENABLED_OUTPUT_MASK, skipMask = ENABLED_OUTPUT_MASK;
	private int								currentDepth = -1;
	private boolean							closed = false, insideMultiline = false;

	/**
	 * <p>Create reader with the nested source and default settings.</p>
	 * @param nestedReader reader to use as content source. Can't be null
	 */
	public PreprocessingReader(final Reader nestedReader) {
		this(nestedReader,new HashMap<>());
	}
	
	/**
	 * <p>Create reader with the nested source and explicitly typed settings.</p>
	 * @param nestedReader reader to use as content source. Can't be null
	 * @param varsAndOptions explicitly typed options. Can't be null. Use static string keys defined in the class as key names for the map. 
	 * Changing it's content during processing data is not affected on processing behavior, but will take effect on all #include content 
	 */
	public PreprocessingReader(final Reader nestedReader, final Map<String,Object> varsAndOptions) {
		this(nestedReader, varsAndOptions, new IncludeCallback(){
				@Override
				public Reader getIncludeStream(final URI streamRef) throws IOException {
					final URL			url = streamRef.toURL();
					final InputStream	is = url.openStream();
					
					return new InputStreamReader(is,"UTF-8"){@Override public void close() throws IOException {try{super.close();} finally {is.close();}}};
				}
			}
		);
	}

	/**
	 * <p>Create reader with the nested source, explicitly typed settings and special case to process #include statements</p>
	 * @param nestedReader reader to use as content source. Can't be null
	 * @param varsAndOptions explicitly typed options. Can't be null. Use static string keys defined in the class as key names for the map. 
	 * Changing it's content during processing data is not affected on processing behavior, but will take effect on all #include content 
	 * @param includeCallback include callback to get reader for the given include URI. Can't be null
	 */
	
	public PreprocessingReader(final Reader nestedReader, final Map<String,Object> varsAndOptions, final IncludeCallback includeCallback) {
		this(null,nestedReader,varsAndOptions,includeCallback);
	}
	
	protected PreprocessingReader(final URI nestedReaderURI, final Reader nestedReader, final Map<String,Object> varsAndOptions, final IncludeCallback includeCallback) {
		super(nestedReaderURI, nestedReader, varsAndOptions, includeCallback);
	}

	protected void internalProcessLine(final long displacement, final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final int	to = from + length;
		
		if (data[from] == '#') {
			int		start = from++, end;
			
			while (from < to && Character.isJavaIdentifierPart(data[from])) {
				from++;
			}
			end = from;

			from = CharUtils.skipBlank(data,from,true);
			switch (getCommandType(data,start,end)) {
				case CMD_IF			: 
					if (currentDepth >= MAX_DEPTH) {
						throw new SyntaxException(lineNo,0,"#if nesting depth is more than ["+MAX_DEPTH+"]. Reduce preprocessor expression");
					}
					else if (processIf(lineNo,data,from,to)) {
						currentDepth++;
						skipMask &= ~(1 << currentDepth);
					}
					else {
						currentDepth++;
						outputMask &= ~(1 << currentDepth);
					}
					break;
				case CMD_ELSEIF		:
					if ((skipMask & (1 << currentDepth)) != 0) {
						if (processIf(lineNo,data,from,to)) {
							outputMask |= (1 << currentDepth);
							skipMask &= ~(1 << currentDepth);
						}
					}
					break;
				case CMD_ELSE		:
					outputMask ^= (1 << currentDepth);
					break;
				case CMD_ENDIF		:
					if (currentDepth < 0) {
						throw new SyntaxException(lineNo,0,"#endif without #if");
					}
					else {
						outputMask |= (1 << currentDepth);
						skipMask |= (1 << currentDepth);
						currentDepth--;
					}
					if (insideMultiline) {
						putContent(getEndMultilineComment());
						putContent(NL);
						insideMultiline = false;
					}
					break;
				case CMD_DEFINE		:
					processDefine(lineNo,data,from,to); 
					break;
				case CMD_UNDEF		: 
					processUndef(lineNo,data,from,to);
					break;
				case CMD_ERROR		: 
					processError(lineNo,data,from,to); 
					break;
				case CMD_WARNING	: 
					processWarning(lineNo,data,from,to); 
					break;
				case CMD_INCLUDE	: 
					processInclude(lineNo,data,from,to); 
					break;
				default : 
					getErrorProcessingCallback().processError(lineNo,delegateURI != null ? delegateURI.toString() : "","Unknown preprocessor command ["+new String(data,from,to-from)+"].");
			}
		}
		else if (outputMask != ENABLED_OUTPUT_MASK) {
			switch (getHidingMethod()) {
				case EXCLUDE				:
					break;
				case SINGLE_LINE_COMMENTED	:
					putContent(getInlineComment());
					putContent(data,from,length);
					break;
				case MULTILINE_COMMENTED	:
					if (!insideMultiline) {
						insideMultiline = true;
						putContent(getStartMultilineComment());
					}
					putContent(data,from,length);
					break;
				default : throw new UnsupportedOperationException("Hiding method ["+getHidingMethod()+"] is not supported yet");
			}
		}
		else {
			if (isInlineSubstitution()) {
				substitute(data, from, length);
			}
			else {
				putContent(data,from,length);
			}
		}
	}

	private CommandType getCommandType(final char[] data, final int start, final int end) {
		final long	id;
		
		if (isIgnoreCase()) {
			final char[]	command = new char[end-start];
			
			for (int index = 0; index < command.length; index++) {
				command[index] = Character.toLowerCase(data[start+index]);
			}
			id = COMMANDS.seekName(command,0,command.length);
		}
		else {
			id = COMMANDS.seekName(data,start,end);
		}
		
		if (id >= 0) {
			return COMMANDS.getCargo(id);
		}
		else {
			return CommandType.CMD_UNKNOWN;
		}
	}
	
	@Override
	protected void substitute(final char[] data, final int from, final int length) {	// This method is used to reduce stringbuilder operations
		int		end;
		
		for (int index = from, to = from + length; index < to; index++) {
			if (Character.isJavaIdentifierStart(data[index])) {
				end = index;
				while (end < to && Character.isJavaIdentifierPart(data[end])) {
					end++;
				}
				if (getDefinitions().seekName(data,index,end) >= 0) {
					final StringBuilder	sb = new StringBuilder();
					
					putContent(data,from,index-from);
					substitute(sb.append(data,index,to-index),0);
					putContent(sb.toString().toCharArray());
					return;
				}
				else {
					index = end;
				}
			}
		}
		putContent(data,from,length);
	}

	@Override
	protected void substitute(final StringBuilder sb, final int from) {
		for (int index = from, maxIndex = sb.length(); index < maxIndex; index++) {
			if (Character.isJavaIdentifierStart(sb.charAt(index))) {
				int	to;
				
				for (to = index+1; to < maxIndex && Character.isJavaIdentifierPart(sb.charAt(to)); to++) {
				}

				final long	id = getDefinitions().seekName((CharSequence)sb.substring(index,to));
				
				if (id >= 0) {
					final char[]	value = getDefinitions().getCargo(id);
					final int		delta = value.length - (to - index);
					
					sb.delete(index,to).insert(index,value);
					if (!isRecursiveSubstitution()) {
						index += delta;
					}
					maxIndex = sb.length();
				}
				else {
					index = to;
				}
			}
		}
	}

	private long extractTail(final char[] data, int from, final int to) {
		int		startValue = from, endValue = to - 1;
		
		startValue = from;
		if (getInlineComment().length != 0) {
loop:		while (from < to) {		// Seek available inline comment in the definition string 
				if (data[from] == getInlineComment()[0]) {
					for (int index = 1, maxIndex = getInlineComment().length; index < maxIndex; index++) {
						if (data[from+index] != getInlineComment()[index]) {
							from++;
							continue loop;
						}
					}
					break;
				}
				else {
					from++;
				}
			}
			endValue = from;
		}
		while (endValue > from && data[endValue] <= ' ') {	// truncate tail blank(s)
			endValue--;
		}
		return (((long)startValue) << 32) | endValue;
	}

	private boolean calculate(final int lineNo, final ExprLevel level, final char[] data, int[] current, final int to) throws SyntaxException {
		boolean		result;
		
		current[0] = CharUtils.skipBlank(data,current[0],true);
		switch (level) {
			case EL_OR 	:
				if (calculate(lineNo,ExprLevel.EL_AND,data,current,to)) {
					return true;
				}
				else {
					current[0] = CharUtils.skipBlank(data,current[0],true);
					if (data[current[0]] == '|' && data[current[0]+1] == '|') {
						current[0] += 2;
						return calculate(lineNo,ExprLevel.EL_OR,data,current,to);
					}
					else {
						return false;
					}
				}
			case EL_AND	:
				if (!calculate(lineNo,ExprLevel.EL_NOT,data,current,to)) {
					return false;
				}
				else {
					current[0] = CharUtils.skipBlank(data,current[0],true);
					if (data[current[0]] == '&' && data[current[0]+1] == '&') {
						current[0] += 2;
						return calculate(lineNo,ExprLevel.EL_AND,data,current,to);
					}
					else {
						return true;
					}
				}
			case EL_NOT :
				if (data[current[0]] == '!') {
					current[0] += 1;
					return !calculate(lineNo,ExprLevel.EL_CMP,data,current,to);
				}
				else {
					return calculate(lineNo,ExprLevel.EL_CMP,data,current,to);
				}				
			case EL_CMP	:
				switch (data[current[0]]) {
					case '('	:
						current[0] = CharUtils.skipBlank(data,current[0]+1,true);
						result = calculate(lineNo,ExprLevel.EL_OR,data,current,to);
						if (data[current[0]] == ')') {
							current[0]++;
						}
						else {
							throw new SyntaxException(lineNo, current[0], URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_MISSING_CLOSE_BRACKET));
						}
						break;
					case '?'	:
						current[0] = CharUtils.skipBlank(data,current[0]+1,true);
						result = extractValue(lineNo,data,current,to) != null;
						break;
					default :
						if (Character.isJavaIdentifierStart(data[current[0]])) {
							final char[]			left = extractValue(lineNo,data,current,to);
							final ComparisonType	oper;
							
							current[0] = CharUtils.skipBlank(data,current[0],true);
							switch (data[current[0]]) {
								case '=' :
									if (data[current[0]+1] == '=') {
										oper = ComparisonType.CMP_EQ;
										current[0] += 2;
									}
									else {
										throw new SyntaxException(lineNo,current[0],"Unknown comparison operator");
									}
									break;
								case '!' :
									if (data[current[0]+1] == '=') {
										oper = ComparisonType.CMP_NE;
										current[0] += 2;
									}
									else {
										throw new SyntaxException(lineNo,current[0],"Unknown comparison operator");
									}
									break;
								case '<' :
									if (data[current[0]+1] == '=') {
										oper = ComparisonType.CMP_LE;
										current[0] += 2;
									}
									else {
										oper = ComparisonType.CMP_LT;
										current[0] += 1;
									}
									break;
								case '>' :
									if (data[current[0]+1] == '=') {
										oper = ComparisonType.CMP_GE;
										current[0] += 2;
									}
									else {
										oper = ComparisonType.CMP_GT;
										current[0] += 1;
									}
									break;
								default  : throw new SyntaxException(lineNo,current[0],"Unknown comparison operator");
							}
							current[0] = CharUtils.skipBlank(data,current[0],true);

							int			location[] = new int[2];
							long		mul = 1, val[] = new long[1];
						
							switch (data[current[0]]) {
								case '\"' :
									try{current[0] = UnsafedCharUtils.uncheckedParseUnescapedString(data,current[0]+1,'\"',false,location);
										result = compare(left,oper,data,location[0],location[1]+1);
									} catch (IllegalArgumentException exc) {
										throw new SyntaxException(lineNo,current[0],"Unpaired quotas in the string constant");
									}
									break;
								case '-' :
									mul = -1;
									current[0]++;
									current[0] = CharUtils.skipBlank(data,current[0],true);
								case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
									current[0] = CharUtils.parseLongExtended(data,current[0],val,false);
									result = compare(left,oper,val[0] * mul);
									break;
								default :
									throw new SyntaxException(lineNo,current[0],"Illegal operand. \"<string>\" or [-]<number> awaited instead of '"+data[current[0]]+"'");
							}
						}
						else {
							throw new SyntaxException(lineNo,current[0],"Illegal operand. <Name>, '?'<Name> or '('<expression>')' awaited");
						}
				}
				return result;
			default : throw new UnsupportedOperationException("Expression level ["+level+"] is not supported yet"); 
		}
	}

	private boolean compare(final char[] content, final ComparisonType oper, final long value) {
		final long	left[] = new long[1]; 
				
		try{CharUtils.parseLongExtended(content,0,left,false);
			switch (oper) {
				case CMP_EQ : return left[0] == value;
				case CMP_NE : return left[0] != value;
				case CMP_GT : return left[0] > value;
				case CMP_LT : return left[0] <= value;
				case CMP_GE : return left[0] >= value;
				case CMP_LE : return left[0] <= value;
				default : throw new UnsupportedOperationException("Comparison operator ["+oper+"] is not supported yet"); 
			}
		} catch (SyntaxException exc) {
			return oper == ComparisonType.CMP_NE;
		}
	}

	private boolean compare(final char[] content, final ComparisonType oper, final char[] data, final int from, final int to) {
		int		result = 0;
		
		for (int index = 0, minLen = Math.min(content.length,to-from); index < minLen; index++) {
			if ((result  = content[index] - data[from + index]) != 0) {
				break;
			}
		}
		if (result == 0) {
			result = content.length - (to-from);
		}
		switch (oper) {
			case CMP_EQ : return result == 0;
			case CMP_NE : return result != 0;
			case CMP_GT : return result > 0;
			case CMP_LT : return result <= 0;
			case CMP_GE : return result >= 0;
			case CMP_LE : return result <= 0;
			default : throw new UnsupportedOperationException("Comparison operator ["+oper+"] is not supported yet"); 
		}
	}

	private char[] extractValue(final int lineNo, final char[] data, final int[] current, final int to) throws SyntaxException {
		final int[]		location = new int[2];
		
		current[0] = UnsafedCharUtils.uncheckedParseName(data,current[0],location);
		
		if (location[0] == location[1]) {
			throw new SyntaxException(lineNo,0,"Name is missing in the expression");
		}
		else {
			final long		id = getDefinitions().seekName(data,location[0],location[1]+1);
			
			if (id >= 0) {
				return getDefinitions().getCargo(id);
			}
			else {
				return null;
			}
		}
	}

	private boolean processIf(final int lineNo, final char[] data, int from, final int to) throws SyntaxException {
		final int[]		current = {from};
		final boolean	result = calculate(lineNo,ExprLevel.EL_OR,data,current,to);
		
		if (current[0] < to) {
			final long		tail = extractTail(data,current[0],to);
			
			if (data[(int) (tail >> 32)] > ' ' && !(getInlineComment().length > 0 && UnsafedCharUtils.uncheckedCompare(data,(int) (tail >> 32),getInlineComment(),0,getInlineComment().length))) {
				throw new SyntaxException(lineNo, 0, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNPARSED_TAIL)); 
			}
		}
		return result;
	}
	
	private void processDefine(final int lineNo, final char[] data, int from, final int to) throws SyntaxException {
		int		location[] = new int[2];

		from = UnsafedCharUtils.uncheckedParseName(data,from,location);
		from = CharUtils.skipBlank(data,from,true);
		
		final long		valueLocation = extractTail(data,from,to), id = getDefinitions().seekName(data,location[0],location[1]+1);
		final int		startValue = (int) (valueLocation >> 32), endValue = (int) (valueLocation & 0xFFFFFFFF); 
		final char[]	cargo = new char[endValue-startValue+1];

		System.arraycopy(data,startValue,cargo,0,cargo.length);
		if (id >= 0) {
			getDefinitions().setCargo(id,cargo);
		}
		else {
			getDefinitions().placeName(data,location[0],location[1]+1,cargo);
		}
	}
	
	private void processUndef(final int lineNo, final char[] data, int from, final int to) throws SyntaxException {
		int		location[] = new int[2];

		UnsafedCharUtils.uncheckedParseName(data,from,location);
		final long	id = getDefinitions().seekName(data,location[0],location[1]+1);
		
		if (id >= 0) {
			getDefinitions().removeName(id);
		}
	}

	private void processError(final int lineNo, final char[] data, int from, final int to) throws SyntaxException {
		final long		valueLocation = extractTail(data,from,to);
		final int		startValue = (int) (valueLocation >> 32), endValue = (int) (valueLocation & 0xFFFFFFFF); 
		
		getErrorProcessingCallback().processError(lineNo,delegateURI != null ? delegateURI.toString() : "",new String(data,startValue,endValue-startValue+1));
	}

	private void processWarning(final int lineNo, final char[] data, int from, final int to) throws SyntaxException {
		final long		valueLocation = extractTail(data,from,to);
		final int		startValue = (int) (valueLocation >> 32), endValue = (int) (valueLocation & 0xFFFFFFFF); 
		
		getErrorProcessingCallback().processWarning(lineNo,delegateURI != null ? delegateURI.toString() : "",new String(data,startValue,endValue-startValue+1));
	}

	private void processInclude(final int lineNo, final char[] data, int from, final int to) throws IOException, SyntaxException {
		final int		location[] = new int[2];
		
		if (data[from] == '\"') {
			UnsafedCharUtils.uncheckedParseUnescapedString(data,from+1,'\"',false,location);
		}
		else if (data[from] == '<') {
			UnsafedCharUtils.uncheckedParseUnescapedString(data,from+1,'>',false,location);
		}
		else {
			final long		valueLocation = extractTail(data,from,to);
			
			location[0] = (int) (valueLocation >> 32);
			location[1] = (int) (valueLocation & 0xFFFFFFFF); 
		}
		
		try{final URI		includeURI = URI.create(new String(data,location[0],location[1]-location[0]+1));
			pushReader(includeURI,getIncludeCallback().getIncludeStream(includeURI));
		} catch (IllegalArgumentException exc) {
			throw new IOException("I/O error: "+exc.getLocalizedMessage()); 
		}
	}

	@Override
	protected AbstractPreprocessingReader newDelegate(final URI nestedReaderURI, final Reader nestedReader, final Map<String, Object> varsAndOptions, final IncludeCallback includeCallback) throws IOException, SyntaxException {
		return new PreprocessingReader(nestedReaderURI, nestedReader, varsAndOptions, includeCallback);
	}
}
