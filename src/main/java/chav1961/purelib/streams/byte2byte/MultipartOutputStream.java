package chav1961.purelib.streams.byte2byte;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
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
	private static final String	SPLITTER_PREFIX = "-----------------------------";
	private static final byte[]	COLON = ": ".getBytes();
	private static final byte[]	SEMICOLON = "; ".getBytes();
	private static final byte[]	EQUALS = "=".getBytes();
	private static final byte[]	DQUOTE = "\"".getBytes();
	private static final byte[]	CRLF = "\r\n".getBytes();
	private static final byte[]	SPLITTER_LAST = "--".getBytes();
	
	private static final int	STATE_BEFORE_PART = 0;
	private static final int	STATE_IN_PART = 1;
	private static final int	STATE_AFTER_PART = 2;
	private static final int	STATE_AFTER_FINISH = 3;
	
	private final byte[]		splitter;
	private final Set<String>	names = new HashSet<>();
	private int					state = STATE_BEFORE_PART;
	
	public MultipartOutputStream(final OutputStream os, final byte[] splitter) throws IOException {
		super(os);
		if (splitter == null || splitter.length == 0) {
			throw new IllegalArgumentException("Splitter can't be null or empty array"); 
		}
		else {
			this.splitter = splitter;
		}
	}

	public void putNextEntry(final MultipartEntry entry) throws IOException {
		if (entry == null) {
			throw new NullPointerException("Entry to put can't be null");
		}
		else if (names.contains(entry.getName())) {
			throw new IOException("Duplicate name ["+entry.getName()+"] in the output stream");
		}
		else {
			switch (state) {
				case STATE_BEFORE_PART 	:
					out.write(splitter);
					out.write(CRLF);
					break;
				case STATE_AFTER_PART	:
					names.add(entry.getName());
					for(Entry<String, String> item : entry) {
						if (item.getKey().indexOf(':') == 0) {
							out.write(item.getKey().getBytes());
							out.write(COLON);
							out.write(item.getValue().getBytes());
							for(Entry<String, String> subitem : entry) {
								if (subitem.getKey().startsWith(item.getKey()+':')) {
									out.write(SEMICOLON);
									out.write(subitem.getKey().getBytes());
									out.write(EQUALS);
									out.write(DQUOTE);
									out.write(item.getValue().getBytes());
									out.write(DQUOTE);
								}
							}
							out.write(CRLF);
						}
					}
					out.write(CRLF);
					state = STATE_IN_PART;
					break;
				case STATE_IN_PART 		:
					closeEntry();
					state = STATE_AFTER_PART;
					putNextEntry(entry);
					break;
				case STATE_AFTER_FINISH	:
					throw new IOException("Attempt to create new part after calling finish()"); 
				default :
					throw new UnsupportedOperationException("State ["+state+"] is not supported yet");
			}
		}
	}
	
	public void closeEntry() throws IOException {
		out.write(CRLF);
		out.write(splitter);
		out.write(CRLF);
	}
	
	public void finish() throws IOException {
		out.write(CRLF);
		out.write(splitter);
		out.write(SPLITTER_LAST);
		out.write(CRLF);
		out.flush();
		state = STATE_AFTER_FINISH;
	}
	
	@Override
	public void close() throws IOException {
		finish();
		super.close();
	}
	
	public static byte[] createUniqueSplitter() {
		final UUID	id = UUID.randomUUID();
		
		return (SPLITTER_PREFIX+id.getLeastSignificantBits()+id.getMostSignificantBits()).getBytes();
	}
}
