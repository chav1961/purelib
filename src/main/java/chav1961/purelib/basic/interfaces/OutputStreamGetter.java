package chav1961.purelib.basic.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>This interface produces output stream to set content of the manipulated entity on loading</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @last.update 0.0.4
*/
@FunctionalInterface
public interface OutputStreamGetter {
	/**
	 * <p>Get entity content to load</p>
	 * @return content stream. Can't be null
	 * @throws IOException on any I/O errors
	 */
	OutputStream getOutputContent() throws IOException;
	
	/**
	 * <p>Get dummy output stream getter</p>
	 * @return dummy output stream getter. Can't be null
	 */
	static OutputStreamGetter dummy() {
		return new OutputStreamGetter() {
			@Override
			public OutputStream getOutputContent() throws IOException {
				return new OutputStream() {
					@Override
					public void write(int b) throws IOException {
					}
				};
			}
		};
	}
}