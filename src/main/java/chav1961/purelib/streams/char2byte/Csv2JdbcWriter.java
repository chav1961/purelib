package chav1961.purelib.streams.char2byte;

import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;

import chav1961.purelib.basic.growablearrays.GrowableCharArray;

public class Csv2JdbcWriter extends Writer {
	private final GrowableCharArray	gca = new GrowableCharArray(false); 
	
	
	public Csv2JdbcWriter(final PreparedStatement ps, final char splitter) {
		this(ps,splitter,true);
	}
	
	public Csv2JdbcWriter(final PreparedStatement ps, final char splitter, final boolean firstLineIsNames) {
		
	}

	public Csv2JdbcWriter(final PreparedStatement ps, final char splitter, final String... fields) {
	}
	
	public Csv2JdbcWriter(final PreparedStatement ps, final char splitter, final int... indices) {
		this(ps,splitter,true,indices);
	}
	
	public Csv2JdbcWriter(final PreparedStatement ps, final char splitter, final boolean firstLineIsNames, final int... indices) {
		
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if (cbuf == null || cbuf.length == 0) {
			throw new IllegalArgumentException("Buffer can't be null or empty array");
		}
		else if (off < 0 || off >= cbuf.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(cbuf.length-1));
		}
		else if (off+len < 0 || off+len >= cbuf.length) {
			throw new IllegalArgumentException("Offset + length ["+(off+len)+"] out of range 0.."+(cbuf.length-1));
		}
		else {
			gca.append(cbuf,off,off+len);
		}
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws IOException {
		flush();
		gca.clear();
	}
}
