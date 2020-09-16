package chav1961.purelib.streams.interfaces;

import java.io.IOException;

/**
 * <p>This interface describes output streams with explicit finish operations.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public interface Finishable {
	/**
	 * <p>Finish output stream and commit all required changed</p>
	 * @throws IOException in any error
	 */
	void finish() throws IOException;
	
	/**
	 * <p>Is output stream finished</p>
	 * @return true if yes
	 */
	boolean isFinished();
}
