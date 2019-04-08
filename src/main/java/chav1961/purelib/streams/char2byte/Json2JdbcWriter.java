package chav1961.purelib.streams.char2byte;

import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class Json2JdbcWriter extends Writer {
	private static final SubstitutableProperties	EMPTY = new SubstitutableProperties(); 
	
	private final PreparedStatement					ps;
	private final GrowableCharArray					gca = new GrowableCharArray(false);
	private final SyntaxTreeInterface<Object>		names = new AndOrTree<>();

	public Json2JdbcWriter(final PreparedStatement ps, final String... fields) {
		this(ps,EMPTY,fields);
	}
	
	public Json2JdbcWriter(final PreparedStatement ps, final SubstitutableProperties options, final String... fields) {
		if (ps == null) {
			throw new NullPointerException("Prepared statement can't be null");
		}
		else if (options == null) {
			throw new NullPointerException("Options can't be null");
		}
		else if (fields == null || fields.length == 0) {
			throw new IllegalArgumentException("Field list can't be null or empty");
		}
		else {
			this.ps = ps;
			
			for (int index = 0; index < fields.length; index++) {
				if (fields[index] == null || fields[index].isEmpty()) {
					throw new IllegalArgumentException("Null or empty field name at index ["+index+"]");
				}
				else {
					names.placeName(fields[index],index,null);
				}
			}
		}
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
