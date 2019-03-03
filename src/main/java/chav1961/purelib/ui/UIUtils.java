package chav1961.purelib.ui;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.List;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.ui.interfacers.Format;

/**
 * <p>This is utility class to support useful methods for UI.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @see CreoleWriter 
 * @since 0.0.2
 */
public class UIUtils {
	private UIUtils(){
	}

	/**
	 * <p>Convert Creole-based character array to HTML string</p>
	 * @param source creole-based character array. Can't be null or empty array
	 * @return html converted
	 * @throws IllegalArgumentException when argument is null or empty array
	 * @throws IOException on any errors in the Creole content
	 */
	public static String cre2Html(final char[] source) throws IllegalArgumentException, IOException {
		if (source == null || source.length == 0) {
			throw new IllegalArgumentException("Source content can't be null or empty array"); 
		}
		else {
			try(final Reader			rdr = new CharArrayReader(source);
				final Writer			wr = new StringWriter()){
				try(final CreoleWriter	cwr = new CreoleWriter(wr,MarkupOutputFormat.XML2HTML)) {
				
					Utils.copyStream(rdr,cwr);
				}
				return wr.toString();
			}
		}
	}

	/**
	 * <p>Convert Creole-based string to HTML string</p>
	 * @param source creole-based string. Can't be null or empty
	 * @return html converted
	 * @throws IllegalArgumentException when argument is null or empty
	 * @throws IOException on any errors in the Creole content
	 */
	public static String cre2Html(final String source) throws IllegalArgumentException, IOException {
		if (source == null || source.isEmpty()) {
			throw new IllegalArgumentException("Source string can't be null or empty"); 
		}
		else {
			try(final Reader			rdr = new StringReader(source);
				final Writer			wr = new StringWriter()){
				try(final CreoleWriter	cwr = new CreoleWriter(wr,MarkupOutputFormat.XML2HTML)) {
				
					Utils.copyStream(rdr,cwr);
				}
				return wr.toString();
			}
		}
	}

	
}
