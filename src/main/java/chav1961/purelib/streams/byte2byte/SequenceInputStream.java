package chav1961.purelib.streams.byte2byte;

import java.io.IOException;
import java.io.InputStream;

import chav1961.purelib.basic.Utils;

/**
 * <p>This class is used to make input from lot of concatenated input streams. It starts reading from the same first input stream. When EOF will be detected,
 * it switches to the second input stream etc. It also call {@linkplain #switchNext(int)} method before switching. You can override this method to insert some
 * <i>divizor bytes</i> between input streams.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class SequenceInputStream extends InputStream {
	private final InputStream[]	content;
	private final boolean		closeAll;
	private volatile int		current = 0;
	private volatile byte[]		tail = new byte[0];
	private volatile int		currentTail = 0;
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param sources source streams to concatenate. Can't me empty list and can't contains nulls inside
	 * @throws IllegalArgumentException
	 */
	public SequenceInputStream(final InputStream... sources) throws IllegalArgumentException {
		this(false, sources);
	}
	
	/**
	 * <p>
	 * @param closeAll
	 * @param sources
	 * @throws IllegalArgumentException
	 */
	public SequenceInputStream(final boolean closeAll, final InputStream... sources) throws IllegalArgumentException {
		if (sources == null || sources.length == 0 || Utils.checkArrayContent4Nulls(sources) >= 0) {
			throw new IllegalArgumentException("Input stream list is null, empty or contains nulls inside");
		}
		else {
			this.closeAll = closeAll;
			this.content = sources.clone();
		}
	}
	
	@Override
	public int read() throws IOException {
		if (currentTail < tail.length) {
			return tail[currentTail++];
		}
		else if (current >= content.length) {
			return -1;
		}
		else {
			final int	result = content[current].read();
			
			if (result == -1) {
				tail = switchNext(current);
				currentTail = 0;
				if (closeAll) {
					content[current].close();
				}
				current++;
				return read();
			}
			else {
				return result;
			}
		}
	}

	/**
	 * <p>Process switching of the streams. Can be used to insert some divizor bytes between streams</p> 
	 * @param current current stream to detect EOF. Exactly ordered to constructor parameters order
	 * @return byte array to insert between streams. Can be zero-length array but not null
	 * @throws IOException on any I/O errors
	 */
	protected byte[] switchNext(final int current) throws IOException {
		return new byte[0];
	}
}
