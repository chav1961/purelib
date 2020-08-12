package chav1961.purelib.basic.interfaces;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>This interface produces output stream to set content of the manipulated entity on loading</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface OutputStreamGetter {
	/**
	 * <p>Get entity content to load</p>
	 * @return content stream. Can't be null
	 * @throws IOException on any I/O errors
	 */
	OutputStream getContent() throws IOException;
}