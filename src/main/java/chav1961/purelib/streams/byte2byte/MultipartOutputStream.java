package chav1961.purelib.streams.byte2byte;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import chav1961.purelib.streams.MultipartEntry;

/**
 * <p>This class is a wrapper to process "miltipart/form-data" content. It's usage is identical to {@linkplain ZipOutputStream}:</p>
 * <code>
 * MultipartOutputStream mos = mew MultipartOutputStream(&lt;someOutputStream&gt;);
 * mos.putNextEntry(new MultipartEntry("part1",&lt;someProperties&gt;));
 * writePart1Content(mos);
 * // mos.flush(); - optional
 * // mos.closeEntry(); - optional
 * mos.putNextEntry(new MultipartEntry("part2",&lt;someProperties&gt;));
 * writePart2Content(mos);
 * // mos.flush(); - optional
 * // mos.closeEntry(); - optional
 * mos.finish(); // optional, if mos.close() will be called
 * </code> 
 * @see ZipOutputStream
 * @see MultipartEntry 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class MultipartOutputStream extends FilterOutputStream {

	public MultipartOutputStream(final OutputStream os) throws IOException {
		super(os);
	}

	public void putNextEntry(final MultipartEntry entry) throws IOException {
		
	}
	
	public void closeEntry() throws IOException {
		
	}
	
	public void finish() throws IOException {
		
	}
}
