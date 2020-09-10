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
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class PreprocessingXReader extends AbstractPreprocessingReader {
	private static SyntaxTreeInterface<CommandType>	COMMANDS = new AndOrTree<>();
	
	
	private enum CommandType {
		CMD_UNKNOWN, CMD_COMMAND, CMD_DEFINE, CMD_ELSE, CMD_END, CMD_ERROR, CMD_IFDEF, CMD_IFNDEF, CMD_INCLUDE, CMD_STDOUT, CMD_TRANSLATE, CMD_UNDEF, CMD_XCOMMAND, CMD_XTRANSLATE  
	}

	static {
		COMMANDS.placeName("#command",CommandType.CMD_COMMAND);
		COMMANDS.placeName("#define",CommandType.CMD_DEFINE);
		COMMANDS.placeName("#else",CommandType.CMD_ELSE);
		COMMANDS.placeName("#end",CommandType.CMD_END);
		COMMANDS.placeName("#error",CommandType.CMD_ERROR);
		COMMANDS.placeName("#ifdef",CommandType.CMD_IFDEF);
		COMMANDS.placeName("#ifndef",CommandType.CMD_IFNDEF);
		COMMANDS.placeName("#include",CommandType.CMD_INCLUDE);
		COMMANDS.placeName("#stdout",CommandType.CMD_STDOUT);
		COMMANDS.placeName("#translate",CommandType.CMD_TRANSLATE);
		COMMANDS.placeName("#undef",CommandType.CMD_UNDEF);
		COMMANDS.placeName("#xcommand",CommandType.CMD_XCOMMAND);
		COMMANDS.placeName("#xtranslate",CommandType.CMD_XTRANSLATE);
	}
	
	private long		outputMask = ENABLED_OUTPUT_MASK, skipMask = ENABLED_OUTPUT_MASK;
	private boolean		insideMultiline = false;
	private int			currentDepth = -1;
	
	
	/**
	 * <p>Create reader with the nested source and default settings.</p>
	 * @param nestedReader reader to use as content source. Can't be null
	 */
	public PreprocessingXReader(final Reader nestedReader) {
		this(nestedReader,new HashMap<>());
	}
	
	/**
	 * <p>Create reader with the nested source and explicitly typed settings.</p>
	 * @param nestedReader reader to use as content source. Can't be null
	 * @param varsAndOptions explicitly typed options. Can't be null. Use static string keys defined in the class as key names for the map. 
	 * Changing it's content during processing data is not affected on processing behavior, but will take effect on all #include content 
	 */
	public PreprocessingXReader(final Reader nestedReader, final Map<String,Object> varsAndOptions) {
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
	public PreprocessingXReader(final Reader nestedReader, final Map<String,Object> varsAndOptions, final IncludeCallback includeCallback) {
		this(null,nestedReader,varsAndOptions,includeCallback);
	}
	
	protected PreprocessingXReader(final URI nestedReaderURI, final Reader nestedReader, final Map<String,Object> varsAndOptions, final IncludeCallback includeCallback) {
		super(nestedReaderURI, nestedReader, varsAndOptions, includeCallback);
	}	
	
	@Override
	protected void internalProcessLine(final long displacement, final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
//		final int	to = from + length;
//		
//		if (data[from] == '#') {
//			int		start = from++, end;
//			
//			while (from < to && Character.isJavaIdentifierPart(data[from])) {
//				from++;
//			}
//			end = from;
//
//			from = CharUtils.skipBlank(data,from,true);
//			switch (getXCommandType(data,start,end)) {
//				case CMD_IF			: 
//					if (currentDepth >= MAX_DEPTH) {
//						throw new SyntaxException(lineNo,0,"#if nesting depth is more than ["+MAX_DEPTH+"]. Reduce preprocessor expression");
//					}
//					else if (processIf(lineNo,data,from,to)) {
//						currentDepth++;
//						skipMask &= ~(1 << currentDepth);
//					}
//					else {
//						currentDepth++;
//						outputMask &= ~(1 << currentDepth);
//					}
//					break;
//				case CMD_ELSEIF		:
//					if ((skipMask & (1 << currentDepth)) != 0) {
//						if (processIf(lineNo,data,from,to)) {
//							outputMask |= (1 << currentDepth);
//							skipMask &= ~(1 << currentDepth);
//						}
//					}
//					break;
//				case CMD_ELSE		:
//					outputMask ^= (1 << currentDepth);
//					break;
//				case CMD_ENDIF		:
//					if (currentDepth < 0) {
//						throw new SyntaxException(lineNo,0,"#endif without #if");
//					}
//					else {
//						outputMask |= (1 << currentDepth);
//						skipMask |= (1 << currentDepth);
//						currentDepth--;
//					}
//					if (insideMultiline) {
//						putContent(endMultilineComment);
//						putContent(NL);
//						insideMultiline = false;
//					}
//					break;
//				case CMD_DEFINE		:
//					processDefine(lineNo,data,from,to); 
//					break;
//				case CMD_UNDEF		: 
//					processUndef(lineNo,data,from,to);
//					break;
//				case CMD_ERROR		: 
//					processError(lineNo,data,from,to); 
//					break;
//				case CMD_WARNING	: 
//					processWarning(lineNo,data,from,to); 
//					break;
//				case CMD_INCLUDE	: 
//					processInclude(lineNo,data,from,to); 
//					break;
//				default : 
//					errCallback.processError(lineNo,delegateURI != null ? delegateURI.toString() : "","Unknown preprocessor command ["+new String(data,from,to-from)+"].");
//			}
//		}
//		else if (outputMask != ENABLED_OUTPUT_MASK) {
//			switch (getHidingMethod()) {
//				case EXCLUDE				:
//					break;
//				case SINGLE_LINE_COMMENTED	:
//					putContent(inlineComment);
//					putContent(data,from,length);
//					break;
//				case MULTILINE_COMMENTED	:
//					if (!insideMultiline) {
//						insideMultiline = true;
//						putContent(startMultilineComment);
//					}
//					putContent(data,from,length);
//					break;
//				default : throw new UnsupportedOperationException("Hiding method ["+hidingMethod+"] is not supported yet");
//			}
//		}
//		else {
//			if (isInlineSubstitution()) {
//				substitute(data, from, length);
//			}
//			else {
//				putContent(data,from,length);
//			}
//		}
	}

	@Override
	protected AbstractPreprocessingReader newDelegate(URI nestedReaderURI, Reader nestedReader, Map<String, Object> varsAndOptions, IncludeCallback includeCallback) throws IOException, SyntaxException {
		return new PreprocessingXReader(nestedReaderURI, nestedReader, varsAndOptions, includeCallback);
	}
	
	protected CommandType getCommandType(final char[] data, final int start, final int end) {
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

}
