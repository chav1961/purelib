package chav1961.purelib.sql;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLXML;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import chav1961.purelib.basic.growablearrays.GrowableCharArray;

public class InMemoryLittleSQLXML implements SQLXML {
	private final GrowableCharArray	gca = new GrowableCharArray(true); 		

	public InMemoryLittleSQLXML(){
	}

	public InMemoryLittleSQLXML(final String content) throws SQLException {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			setString(content);
		}
	}
	
	@Override
	public void free() throws SQLException {
		gca.clear();
	}

	@Override
	public InputStream getBinaryStream() throws SQLException {
		return new ByteArrayInputStream(getString().getBytes());
	}

	@Override
	public OutputStream setBinaryStream() throws SQLException {
		return new DirectOutputStream(gca,0);
	}

	@Override
	public Reader getCharacterStream() throws SQLException {
		return new CharArrayReader(gca.toArray(),0,gca.length());
	}

	@Override
	public Writer setCharacterStream() throws SQLException {
		return new DirectWriter(gca,0);
	}

	@Override
	public String getString() throws SQLException {
		return new String(gca.toArray(),0,gca.length());
	}

	@Override
	public void setString(final String value) throws SQLException {
		if (value == null) {
			throw new NullPointerException("Value to set can't be null"); 
		}
		else if (value.isEmpty()) {
			gca.clear();
		}
		else {
			gca.length(value.length());
			value.getChars(0,value.length(),gca.toArray(),0);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Source> T getSource(final Class<T> sourceClass) throws SQLException {
		if (sourceClass == null) {
			throw new NullPointerException("Source class can't be null"); 
		}
		else if (sourceClass.isAssignableFrom(StreamSource.class)) {
			return (T) new StreamSource(getCharacterStream());
		}
		else {
			throw new SQLException("Only StreamSource.class using is supported for the given method");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Result> T setResult(Class<T> resultClass) throws SQLException {
		if (resultClass == null) {
			throw new NullPointerException("Source class can't be null"); 
		}
		else if (resultClass.isAssignableFrom(StreamResult.class)) {
			return (T) new StreamResult(setCharacterStream());
		}
		else {
			throw new SQLException("Only StreamResult.class using is supported for the given method");
		}
	}

	@Override
	public String toString() {
		return "SimpleLittleSQLXML [content=" + gca + "]";
	}

	private static class DirectOutputStream extends OutputStream {
		private int					displacement;
		private GrowableCharArray	content;
		
		private DirectOutputStream(final GrowableCharArray gca, final int displacement) {
			this.content = gca;
			this.displacement = displacement;
		}
		
		@Override
		public void write(int b) throws IOException {
			if (displacement < content.length()) {
				content.toArray()[displacement++] = (char)b;
			}
			else {
				content.append((char)b);
				displacement++;
			}
		}

		@Override
		public void close() throws IOException {
			content.length(displacement);
		}
	}
	
	private static class DirectWriter extends Writer {
		private int					displacement;
		private GrowableCharArray	content;
		
		private DirectWriter(final GrowableCharArray gca, final int displacement) {
			this.content = gca;
			this.displacement = displacement;
		}
		
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			if (cbuf == null) {
				throw new NullPointerException(); 
			}
			else if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }
	        if (displacement + len > content.length()) {
	        	content.length(displacement + len);
	        }
	        System.arraycopy(cbuf,off,content.toArray(),displacement,len);
	        displacement += len;
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void close() throws IOException {
			content.length(displacement);
		}
	}
}
