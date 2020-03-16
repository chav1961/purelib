package chav1961.purelib.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

/**
 * <p>This is an useful utility class to support a lot of popular operations on streams.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public class StreamsUtil {
	private static final Map<String,Map<String,WrapperCreator>>	MIME_CONVERTORS = new HashMap<>();
	
	static {
		Map<String,WrapperCreator>	temp;
		
		temp = new HashMap<>();
		temp.put("text/plain",(nested,source,target)->{return new WriterWrapper(nested);});
		temp.put("text/html",(nested,source,target)->{return new WriterWrapper(nested);});
		MIME_CONVERTORS.put("text/plain",temp);
		
		temp = new HashMap<>();
		temp.put("text/plain",(nested,source,target)->{return new CreoleWriter(nested,MarkupOutputFormat.XML2TEXT);});
		temp.put("text/html",(nested,source,target)->{return new CreoleWriter(nested,MarkupOutputFormat.XML2HTML);});
		MIME_CONVERTORS.put("text/x-wiki.creole",temp);			
	} 

	@FunctionalInterface
	private interface WrapperCreator {
		Writer create(Writer nested, MimeType source, MimeType target) throws IOException;
	}
	
	private StreamsUtil() {
	}

	/**
	 * <p>Create Writer wrapper to convert it's nested content to format required</p>
	 * @param targetWriter writer to upload converted content to
	 * @param from MIME type of source content
	 * @param to MIME type of target content
	 * @return Writer wrapper
	 * @throws NullPointerException on any parameter is null
	 * @throws IOException on any I/O errors
	 */
	public static Writer getStreamClassForOutput(final Writer targetWriter, final MimeType from, final MimeType to) throws NullPointerException, IOException {
		if (from == null) {
			throw new NullPointerException("MIME from can't be null");
		} 
		else if (to == null) {
			throw new NullPointerException("MIME to can't be null");
		}
		else {
			final String	fromString = from.getPrimaryType()+"/"+from.getSubType(), toString = to.getPrimaryType()+"/"+to.getSubType();
			
			if (MIME_CONVERTORS.containsKey(fromString) && MIME_CONVERTORS.get(fromString).containsKey(toString)) {
				return MIME_CONVERTORS.get(fromString).get(toString).create(targetWriter,from,to);
			}
			else {
				return new WriterWrapper(targetWriter);
			}
		}
	}
	
	/**
	 * <p>Copy JSON content from parser to printer</p>
	 * @param source source StAX parser
	 * @param target target StAX printer.
	 * @throws NullPointerException any of parameters is null
	 * @throws IOException any I/O errors
	 * @throws SyntaxException any JSON format errors
	 */
	public static void copyJsonStax(final JsonStaxParser source, final JsonStaxPrinter target) throws NullPointerException, IOException, SyntaxException {
		copyJsonStax(source,target,true);
	}	
	
	/**
	 * <p>Copy JSON content from parser to printer</p>
	 * @param source source StAX parser
	 * @param target target StAX printer.
	 * @param totalCopy make total copy when true. Make copy from current position to the end of current element only when false. 
	 * @throws NullPointerException any of parameters is null
	 * @throws IOException any I/O errors
	 * @throws SyntaxException any JSON format errors
	 * @since 0.0.4
	 */
	public static void copyJsonStax(final JsonStaxParser source, final JsonStaxPrinter target, final boolean totalCopy) throws NullPointerException, IOException, SyntaxException {
		if (source == null) {
			throw new NullPointerException("Source parser can't be null");
		}
		else if (target == null) {
			throw new NullPointerException("Target printer can't be null");
		}
		else {
			int		depth = 0;
			
loop:		for (JsonStaxParserLexType item : source) {
				switch (item) {
					case BOOLEAN_VALUE	: target.value(source.booleanValue()); break;
					case END_ARRAY		: 
						target.endArray();
						depth--; 
						if (!totalCopy && depth <= 0) {
							break loop;
						}
						else {
							break;
						}
					case END_OBJECT		: 
						target.endObject(); 
						depth--; 
						if (!totalCopy && depth <= 0) {
							break loop;
						}
						else {
							break;
						}
					case INTEGER_VALUE	: target.value(source.intValue()); break;
					case NAME			: target.name(source.name()); break;
					case NULL_VALUE		: target.nullValue(); break;
					case REAL_VALUE		: target.value(source.realValue()); break;
					case START_ARRAY	: target.startArray(); depth++; break;
					case START_OBJECT	: target.startObject(); depth++; break;
					case STRING_VALUE	: target.value(source.stringValue()); break;
					case LIST_SPLITTER	: target.splitter(); break;
					case ERROR			:
						throw new SyntaxException(source.row(),source.col(),source.getLastError().getLocalizedMessage());
					default				: break;
				}
			}
			target.flush();
		}
	}
	
	/**
	 * <p>Load Creole content into string</p>
	 * @param source Creole content source
	 * @param format String format to convert Creole to
	 * @return Creole content loaded
	 * @throws NullPointerException if any parameters are null
	 * @throws IOException on I/O errors durung content processing
	 * @see CreoleWriter
	 * @since 0.0.4
	 */
	public static String loadCreoleContent(final URL source, final MarkupOutputFormat format) throws NullPointerException, IOException {
		if (source == null) {
			throw new NullPointerException("Source URL can't be null");
		}
		else if (format == null) {
			throw new NullPointerException("Output format can't be null");
		}
		else {
			try(final InputStream	is = source.openStream();
				final Reader		rdr = new InputStreamReader(is);
				final Writer		wr = new StringWriter()) {
				
				try(final Writer	cwr = new CreoleWriter(wr,format)) {
				
					Utils.copyStream(rdr,cwr);
				}
				return wr.toString();
			}
		}
	}
}
