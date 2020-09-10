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
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.streams.char2char.AbstractPreprocessingReader.IncludeCallback;

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
 * @since 0.0.4
 */
public abstract class AbstractPreprocessingReader extends Reader {
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

	/**
	 * <p>This functional interface describes callback to process include statements for {@link AbstractPreprocessingReader} class</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	@FunctionalInterface
	public interface IncludeCallback {
		/**
		 * <p>Get reader to take data included</p>
		 * @param streamRef URI of the stream.
		 * @return reader to take content from
		 * @throws IOException any I/O exceptions to get access to the given URI
		 */
		Reader getIncludeStream(final URI streamRef) throws IOException;
	}

	/**
	 * <p>This interface describes callback to process #error and #warning message for {@link AbstractPreprocessingReader} class</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public interface ErrorProcessingCallback {
		/**
		 * <p>Process #error message</p>
		 * @param line source zero-based line of the source content.
		 * @param sourceStream URI of included stream or null otherwise
		 * @param message message content to process. Can't be null. All defined preprocessor variables will be substituted in the message content
		 * @throws SyntaxException any syntax exceptions was detected in the message string
		 */
		void processError(final int line, final String sourceStream, final String message) throws SyntaxException;
		
		/**
		 * <p>Process #warning message</p>
		 * @param line source zero-based line of the source content.
		 * @param sourceStream URI of included stream or null otherwise
		 * @param message message content to process. Can't be null. All defined preprocessor variables will be substituted in the message content
		 * @throws SyntaxException any syntax exceptions was detected in the message string
		 */
		void processWarning(final int line, final String sourceStream, final String message) throws SyntaxException;
	}
	
	/**
	 * <p>This enumeration describes method to process content need be excluded from the input stream for {@link AbstractPreprocessingReader} class</p>:
	 * <ul>
	 * <li>{@linkplain #EXCLUDE} - exclude this content from the input stream</li>
	 * <li>{@linkplain #SINGLE_LINE_COMMENTED} - retain this content in the input stream with inline comment prefixed for each line to exclude</li>
	 * <li>{@linkplain #MULTILINE_COMMENTED} - retain this content in the input stream with multi-line comment around all block to exclude</li>
	 * </ul>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public enum HidingMethod {
		EXCLUDE, SINGLE_LINE_COMMENTED, MULTILINE_COMMENTED
	}
	
	protected static final int			DEFAULT_BUFFER_SIZE = 8192;
	protected static final HidingMethod	DEFAULT_HIDING_METHOD = HidingMethod.EXCLUDE;
	protected static final boolean		DEFAULT_IGNORE_CASE = false;
	protected static final boolean		DEFAULT_INLINE_SUBSTITUTION = false;
	protected static final boolean		DEFAULT_RECURSIVE_SUBSTITUTION = false;

	protected static final int			MAX_DEPTH = 63;
	protected static final long			ENABLED_OUTPUT_MASK = 0xFFFFFFFFFFFFFFFFL;
	protected static final char[]		NL = {'\n'};

	private static final Map<Class<?>,Class<?>>	WRAPPERS = new HashMap<>();
	
	static {
		WRAPPERS.put(boolean.class,Boolean.class);
		WRAPPERS.put(byte.class,Byte.class);
		WRAPPERS.put(char.class,Character.class);
		WRAPPERS.put(double.class,Double.class);
		WRAPPERS.put(float.class,Float.class);
		WRAPPERS.put(int.class,Integer.class);
		WRAPPERS.put(long.class,Long.class);
		WRAPPERS.put(short.class,Short.class);
	}
	
	private volatile Reader					nestedReader;
	private final Map<String,Object>		varsAndOptions;
	private final int						bufferSize;
	private final HidingMethod				hidingMethod;
	private final char[]					inlineComment, startMultilineComment, endMultilineComment;
	private final boolean					ignoreCase;
	private final boolean					inlineSubstitution;
	private final boolean					recursiveSubstitution;
	private final ErrorProcessingCallback	errCallback;
	private final IncludeCallback			includeCallback;	
	private final SyntaxTreeInterface<char[]>	definitions = new AndOrTree<>();
	private final LineByLineProcessor		lblp = new LineByLineProcessor((displacement,lineNo,data,from,length)->{internalProcessLine(displacement,lineNo,data,from,length);}); 

	private volatile AbstractPreprocessingReader	delegate = null;
	private volatile URI					delegateURI = null;
	private char[]							sourceBuffer, targetBuffer; 
	private int								targetDispl = 0, targetSize = 0;
	private boolean							closed = false;

	protected AbstractPreprocessingReader(final URI nestedReaderURI, final Reader nestedReader, final Map<String,Object> varsAndOptions, final IncludeCallback includeCallback) {
		if (nestedReader == null) {
			throw new NullPointerException("Nested reader can't be null"); 
		}
		else if (varsAndOptions == null) {
			throw new NullPointerException("Vars and options ref can't be null"); 
		}
		else if (includeCallback == null) {
			throw new NullPointerException("Include callback can't be null"); 
		}
		else {
			this.nestedReader = nestedReader;
			this.delegateURI = nestedReaderURI;
			this.varsAndOptions = varsAndOptions;
			this.bufferSize = getOption(BUFFER_SIZE,int.class,DEFAULT_BUFFER_SIZE);
			this.hidingMethod = getOption(HIDING_METHOD,HidingMethod.class,DEFAULT_HIDING_METHOD);
			this.ignoreCase = getOption(IGNORE_CASE,boolean.class,DEFAULT_IGNORE_CASE);
			this.inlineSubstitution = getOption(INLINE_SUBSTITUTION,boolean.class,DEFAULT_INLINE_SUBSTITUTION);
			this.recursiveSubstitution = getOption(RECURSIVE_SUBSTITUTION,boolean.class,DEFAULT_RECURSIVE_SUBSTITUTION);
			this.errCallback = getOption(ERROR_PROCESSING_CALLBACK,ErrorProcessingCallback.class,new ErrorProcessingCallback(){
										@Override
										public void processError(final int line, final String sourceStream, final String message) throws SyntaxException {
											throw new SyntaxException(line,0,message);
										}
						
										@Override
										public void processWarning(final int line, final String sourceStream, final String message) throws SyntaxException {
										}
									}
								);
			this.includeCallback = includeCallback;
			this.sourceBuffer = new char[this.bufferSize];
			this.targetBuffer = new char[this.bufferSize];
			
			char[]			forInline = new char[0], forStartAndEnd[] = new char[][]{new char[0],new char[0]};  
			for (String item : getOption(COMMENT_SEQUENCE,String.class,"").split("\\n")) {
				final int	content = item.indexOf('\t');
					
				if (content == -1) {
					forInline = item.toCharArray();
				}
				else {
					forStartAndEnd[0] = item.substring(0,content).toCharArray();
					forStartAndEnd[1] = item.substring(content+1).toCharArray();
				}
			}
			this.inlineComment = forInline;
			this.startMultilineComment = forStartAndEnd[0];
			this.endMultilineComment = forStartAndEnd[1];

			switch (getHidingMethod()) {
				case EXCLUDE				:
					break;
				case SINGLE_LINE_COMMENTED	:
					if (this.inlineComment.length == 0) {
						throw new IllegalArgumentException("Hiding method ["+getHidingMethod()+"] requires explicit definition of the inline comment sequence. Use the ["+COMMENT_SEQUENCE+"] key to define it!"); 
					}
					break;
				case MULTILINE_COMMENTED	:
					if (this.startMultilineComment.length == 0 || this.endMultilineComment.length == 0) {
						throw new IllegalArgumentException("Hiding method ["+getHidingMethod()+"] requires explicit definition of the miltiline comment sequence. Use the ["+COMMENT_SEQUENCE+"] key to define it!"); 
					}
					break;
				default : throw new UnsupportedOperationException("Hiding method ["+getHidingMethod()+"] is not supported yet");
			}
		}
	}

	protected abstract void internalProcessLine(final long displacement, final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException;
	protected abstract AbstractPreprocessingReader newDelegate(final URI nestedReaderURI, final Reader nestedReader, final Map<String,Object> varsAndOptions, final IncludeCallback includeCallback) throws IOException, SyntaxException;
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		if (cbuf == null) {
			throw new NullPointerException("Buffer to store data can't be null"); 
		}
		else if (off < 0 || off > cbuf.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(cbuf.length-1));
		}
		else if (off+len < 0 || off+len > cbuf.length) {
			throw new IllegalArgumentException("Offset + length ["+(off+len)+"] out of range 0.."+(cbuf.length-1));
		}
		else if (closed) {
			throw new IllegalStateException("Attempt to read data from closed stream");
		}
		else if (delegate != null) {	// Process #include statement
			final int	readed = delegate.read(cbuf,off,len); 
			
			if (readed <= 0) {
				popReader();
				return read(cbuf,off,len);
			}
			else {
				return readed; 
			}
		}
		else {
			if (targetDispl == targetSize) {
				final int	read;
				
				if ((read = nestedReader.read(sourceBuffer)) > 0) {
					try{lblp.write(sourceBuffer,0,read);
					} catch (SyntaxException e) {
						throw new IOException(e.getMessage(),e);
					}
				}
				else {
					lblp.close();
					if (targetDispl == targetSize) {	// Process the same last string in the lblp!
						return -1;
					}
				}
			}
			if (len < targetSize-targetDispl) {
				System.arraycopy(targetBuffer,targetDispl,cbuf,off,len);
				targetDispl += len;
				return len;
			}
			else {
				final int	realLen = targetSize-targetDispl;
				
				System.arraycopy(targetBuffer,targetDispl,cbuf,off,realLen);
				targetDispl = targetSize;
				return realLen;
			}
		}
	}

	@Override
	public void close() throws IOException {
		closed = true;
		if (delegate != null) {
			popReader();
		}
		lblp.close();
		sourceBuffer = null;
		targetBuffer = null;
	}
	
	protected boolean isIgnoreCase() {
		return ignoreCase;
	}

	protected boolean isInlineSubstitution() {
		return inlineSubstitution;
	}
	
	protected boolean isRecursiveSubstitution() {
		return recursiveSubstitution;
	}
	
	protected ErrorProcessingCallback getErrorProcessingCallback() {
		return errCallback;
	}

	protected IncludeCallback getIncludeCallback() {
		return includeCallback;
	}
	
	protected HidingMethod getHidingMethod() {
		return hidingMethod;
	}
	
	protected char[] getEndMultilineComment() {
		return endMultilineComment;
	}

	protected char[] getStartMultilineComment() {
		return startMultilineComment;
	}
	
	protected char[] getInlineComment() {
		return inlineComment;
	}
	
	protected SyntaxTreeInterface<char[]> getDefinitions() {
		return definitions;
	}

	protected Map<String,Object> getVarsAndOptions() {
		return varsAndOptions;
	}
	
	protected LineByLineProcessor getLineProcessor() {
		return lblp;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getOption(final String optionName, final Class<T> returnedType, final T defaultValue) {
		if (optionName == null || optionName.isEmpty()) {
			throw new IllegalArgumentException("Option name can't be null or empty"); 
		}
		else if (returnedType == null) {
			throw new NullPointerException("Returned type can't be null"); 
		}
		else if (!varsAndOptions.containsKey(optionName)) {
			return defaultValue;
		}
		else {
			final Object	returned = varsAndOptions.get(optionName);
		
			if (returned == null) {
				return defaultValue;
			}
			else if (!returnedType.isAssignableFrom(returned.getClass()) && !(WRAPPERS.containsKey(returnedType) && WRAPPERS.get(returnedType).isAssignableFrom(returned.getClass()))) {
				throw new IllegalArgumentException("Uncompatible types for ["+optionName+"] option: awaited is ["+returnedType+"] awaited, but current is ["+returned.getClass()+"]"); 
			}
			else {
				return (T)returned;
			}
		}
	}

	protected void substitute(final char[] data, final int from, final int length) {	// This method is used to reduce stringbuilder operations
		int		end;
		
		for (int index = from, to = from + length; index < to; index++) {
			if (Character.isJavaIdentifierStart(data[index])) {
				end = index;
				while (end < to && Character.isJavaIdentifierPart(data[end])) {
					end++;
				}
				if (definitions.seekName(data,index,end) >= 0) {
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

	protected void substitute(final StringBuilder sb, final int from) {
		for (int index = from, maxIndex = sb.length(); index < maxIndex; index++) {
			if (Character.isJavaIdentifierStart(sb.charAt(index))) {
				int	to;
				
				for (to = index+1; to < maxIndex && Character.isJavaIdentifierPart(sb.charAt(to)); to++) {
				}

				final long	id = definitions.seekName(sb.substring(index,to));
				
				if (id >= 0) {
					final char[]	value = definitions.getCargo(id);
					final int		delta = value.length - (to - index);
					
					sb.delete(index,to).insert(index,value);
					if (!recursiveSubstitution) {
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

	protected void putContent(final char[] data) {
		putContent(data,0,data.length);
	}
	
	protected void putContent(final char[] data, final int from, final int length) {
		if (targetSize + length >= targetBuffer.length) {
			final char[]	newTarget = new char[2 * targetBuffer.length];
			
			System.arraycopy(targetBuffer,0,newTarget,0,targetSize);
			targetBuffer = newTarget;
		}
		System.arraycopy(data,from,targetBuffer,targetSize,length);
		targetSize += length;
	}

	protected void pushReader(final URI includeURI, final Reader includeStream) throws IOException, SyntaxException {
		getLineProcessor().pushProcessing();
		delegate = newDelegate(includeURI,includeStream,getVarsAndOptions(),getIncludeCallback());
	}
	
	protected void popReader() throws IOException {
		delegate.close();
		delegate = null;
		try{getLineProcessor().popProcessing();
		} catch (SyntaxException exc) {
			throw new IOException(exc.getMessage(),exc);
		}
	}
}
