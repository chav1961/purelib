package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;

import chav1961.purelib.basic.CharUtils.CharSubstitutionSource;
import chav1961.purelib.basic.CharUtils.SubstitutionSource;

public class SubstitutableWriter extends Writer {
	public static final String		DEFAULT_START_KEY = "${";
	public static final String		DEFAULT_END_KEY = "}";
	public static final char[]		DEFAULT_START_CHAR_KEY = DEFAULT_START_KEY.toCharArray();
	public static final char[]		DEFAULT_END_CHAR_KEY = DEFAULT_END_KEY.toCharArray();
	
	private static final char[]		EMPTY_CHARS = new char[0];

	private  static final int		STATE_ORDINAL = 0;
	private  static final int		STATE_CAN_BE_START = 1;
	private  static final int		STATE_INSIDE_KEY = 2;
	private  static final int		STATE_CAN_BE_END = 3;
	
	private final Writer			nested;	
	private final char[]			startKey, endKey;
	private final StringBuilder		sb = new StringBuilder();
	private final CharSubstitutionSource	ss;
	private int						currentState = STATE_ORDINAL;
	
	public SubstitutableWriter(final Writer nested, final SubstitutionSource ss) {
		this(nested, ss, DEFAULT_START_KEY, DEFAULT_END_KEY);
	}
	
	public SubstitutableWriter(final Writer nested, final SubstitutionSource ss, final String startKey, final String endKey) {
		if (nested == null) {
			throw new NullPointerException("Nested writer can't be null");
		}
		else if (ss == null) {
			throw new NullPointerException("Substitution source can't be null");
		}
		else if (startKey == null || startKey.isEmpty()) {
			throw new IllegalArgumentException("Start key can't be null or empty");
		}
		else if (endKey == null || endKey.isEmpty()) {
			throw new IllegalArgumentException("End key can't be null or empty");
		}
		else {
			this.nested = nested;
			this.startKey = startKey.toCharArray();
			this.endKey = endKey.toCharArray();
			this.ss = toCharSubstitutionSource(ss);
		}
	}

	public SubstitutableWriter(final Writer nested, final CharSubstitutionSource ss) {
		this(nested, ss, DEFAULT_START_CHAR_KEY, DEFAULT_END_CHAR_KEY);
	}
	
	public SubstitutableWriter(final Writer nested, final CharSubstitutionSource ss, final char[] startKey, final char[] endKey) {
		if (nested == null) {
			throw new NullPointerException("Nested writer can't be null");
		}
		else if (ss == null) {
			throw new NullPointerException("Substitution source can't be null");
		}
		else if (startKey == null || startKey.length == 0) {
			throw new IllegalArgumentException("Start key can't be null or empty array");
		}
		else if (endKey == null || endKey.length == 0) {
			throw new IllegalArgumentException("End key can't be null or empty array");
		}
		else {
			this.nested = nested;
			this.startKey = startKey;
			this.endKey = endKey;
			this.ss = ss;
		}
	}
	
	@Override
	public void write(final char[] cbuf, int off, int len) throws IOException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
        	throw new IndexOutOfBoundsException();
        } else if (len != 0) {

loop:  		for (;;) {
	    		switch (currentState) {
	    			case STATE_ORDINAL 		:
	    	    		final char	possibleStart = startKey[0];
	    	    		
	    				for(int index = off, maxIndex = off+len; index < maxIndex; index++) {
	    					if (cbuf[index] == possibleStart) {
	    						if (index > off) {
	        						nested.write(cbuf, off, index-off);
	    						}
	    						len -= (index-off);
	    						off = index;
	    						currentState = STATE_CAN_BE_START;
	    						continue loop;
	    					}
	    				}
						nested.write(cbuf, off, len);
						break loop;
	    			case STATE_CAN_BE_START	:
	    				final boolean	truncatedStart = startKey.length > len;
	    				
	    				for(int index = off, maxIndex = Math.min(off+startKey.length,off+len); index < maxIndex; index++) {
	    					if (cbuf[index] != startKey[index-off]) {
	    						if (index > off) {
	        						nested.write(cbuf, off, index-off);
	    						}
	    						len -= (index-off);
	    						off = index;
	    						currentState = STATE_ORDINAL;
	    						continue loop;
	    					}
	    				}
	    				if (truncatedStart) {
	    					break loop;
	    				}
	    				else {
    						currentState = STATE_INSIDE_KEY;
    						len -= startKey.length;
    						off += startKey.length;
    						sb.setLength(0);
    						continue loop;
	    				}
	    			case STATE_INSIDE_KEY	:
	    	    		final char	possibleEnd = endKey[0];
	    	    		
	    				for(int index = off, maxIndex = off+len; index < maxIndex; index++) {
	    					if (cbuf[index] == possibleEnd) {
	    						
	    					}
	    				}
	    				break;
	    			case STATE_CAN_BE_END	:
	    				final boolean	truncatedEnd = endKey.length > len;
	    				
	    				for(int index = off, maxIndex = Math.min(off+endKey.length,off+len); index < maxIndex; index++) {
	    					if (cbuf[index] != endKey[index-off]) {
	    						if (index > off) {
	        						nested.write(cbuf, off, index-off);
	    						}
	    						len -= (index-off);
	    						off = index;
	    						currentState = STATE_ORDINAL;
	    						continue loop;
	    					}
	    				}
	    				if (truncatedEnd) {
	    					break loop;
	    				}
	    				else {
    						currentState = STATE_INSIDE_KEY;
    						len -= startKey.length;
    						off += startKey.length;
    						sb.setLength(0);
    						continue loop;
	    				}
	    		}
    		}
        }
	}

	@Override
	public void flush() throws IOException {
		nested.flush();
	}

	@Override
	public void close() throws IOException {
		flush();
		nested.close();
	}

	private static CharSubstitutionSource toCharSubstitutionSource(final SubstitutionSource ss) {
		return new CharSubstitutionSource() {
			@Override
			public char[] getValue(char[] data, int from, int to) {
				final String	result = ss.getValue(new String(data, from, to-from));
				
				return result == null ? EMPTY_CHARS : result.toCharArray();
			}
		};
	}
}
