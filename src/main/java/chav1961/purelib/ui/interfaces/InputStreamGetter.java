package chav1961.purelib.ui.interfaces;

import java.io.InputStream;

/**
 * <p>This interface produces input stream to get content of the manipulated entity on saving</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface InputStreamGetter {
	/**
	 * <p>Get entity content to save</p>
	 * @return content stream. Can't be null
	 */
	InputStream getContent();
}