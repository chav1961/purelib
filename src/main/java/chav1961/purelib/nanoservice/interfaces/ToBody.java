package chav1961.purelib.nanoservice.interfaces;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.OutputStream;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;

import chav1961.purelib.json.JsonSerializer;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This annotation marks method parameter to keep response body. Type of the parameters must be:</p>
 * <ul>
 * <li> for <b>text/plain</b> or <b>text/html</b> content: {@linkplain OutputStream}, {@linkplain Writer}, {@linkplain CreoleWriter}, {@linkplain CharacterTarget} 
 * <li> for <b>text/xml</b> content: as for <b>text/plain</b> plus {@linkplain Document}, {@linkplain XMLStreamWriter} 
 * <li> for <b>application/json</b> content: as for <b>text/plain</b> plus {@linkplain JsonStaxPrinter}, {@linkplain JsonSerializer} 
 * <li> for any other content: {@linkplain OutputStream}
 * </ul> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface ToBody {
	/**
	 * @return MIME type of output content
	 */
	String mimeType();
}
