package chav1961.purelib.basic.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>This interface describes any entity, that has both input and output streams to use.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 */
public interface InputOutputPairInterface extends Closeable {
	/**
	 * <p>Get input stream to use</p>
	 * @return input stream to use. Can't be null. Subsequent calls must return the same result 
	 * @throws IOException on any I/O errors
	 */
	InputStream getInputStream() throws IOException;
	
	/**
	 * <p>Get output stream to use</p>
	 * @return output stream to use. Can't be null. Subsequent calls must return the same result
	 * @throws IOException
	 */
	OutputStream getOutputStream() throws IOException;
}
