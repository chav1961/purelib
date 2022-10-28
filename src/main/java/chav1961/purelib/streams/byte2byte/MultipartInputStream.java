package chav1961.purelib.streams.byte2byte;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import chav1961.purelib.streams.MultipartEntry;

/**
 * <p>This class is a wrapper to process "miltipart/form-data" content. It's usage is identical to {@linkplain ZipInputStream}:</p>
 * <code>
 * MultipartInputStream mis = mew MultipartInputStream(&lt;someInputStream&gt;);
 * MultipartEntry me;
 * while ((me = mis.getNextEntry()) != null) {
 * 		readPartContent(mis);
 * }
 * // mis.close(); - optional
 * </code> 
 * @see ZipInputStream
 * @see MultipartEntry 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class MultipartInputStream extends FilterInputStream {

	public MultipartInputStream(final InputStream nested) throws IOException {
		super(nested);
	}
	
	public MultipartEntry getNextEntry() throws IOException {
		return null;
	}
	
	public void closeEntry() throws IOException {
	}
}
