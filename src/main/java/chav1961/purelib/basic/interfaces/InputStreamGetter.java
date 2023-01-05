package chav1961.purelib.basic.interfaces;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>This interface produces input stream to get content of the manipulated entity on saving</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @last.update 0.0.4
 */
@FunctionalInterface
public interface InputStreamGetter {
	/**
	 * <p>Get entity content to save</p>
	 * @return content stream. Can't be null
	 * @throws IOException on any I/O errors
	 */
	InputStream getContent() throws IOException;
	
	/**
	 * <p>Get dummy input stream getter</p>
	 * @return dummy input stream getter. Can't be null
	 */
	static InputStreamGetter dummy() {
		return new InputStreamGetter() {
			@Override
			public InputStream getContent() throws IOException {
				return new InputStream() {
					@Override
					public int read() throws IOException {
						return -1;
					}
				};
			}
		};
	}
}