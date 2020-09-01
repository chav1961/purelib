package chav1961.purelib.nanoservice.interfaces;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;

import chav1961.purelib.json.JsonSerializer;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.interfaces.CharacterSource;

/**
 * <p>This annotation marks method parameter containing request body. Type of the parameters must be:</p>
 * <ul>
 * <li> for <b>text/plain</b> or <b>text/html</b> content: {@linkplain String}, {@linkplain Reader}, {@linkplain InputStream}, {@linkplain CharacterSource} 
 * <li> for <b>text/xml</b> content: as for <b>text/plain</b> plus {@linkplain Document}, {@linkplain XMLStreamReader} 
 * <li> for <b>application/json</b> content: as for <b>text/plain</b> plus {@linkplain JsonStaxParser}, {@linkplain JsonSerializer} 
 * <li> for any other content: {@linkplain InputStream}
 * </ul> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface FromBody {
	/**
	 * @return MIME type of input content
	 */
	String mimeType();
}
