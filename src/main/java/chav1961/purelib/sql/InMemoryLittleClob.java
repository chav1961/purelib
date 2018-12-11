package chav1961.purelib.sql;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import chav1961.purelib.basic.growablearrays.GrowableCharArray;

/**
 * <p>This class implements in-memory Clob to use in the SQL.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.sql JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public class InMemoryLittleClob implements Clob {
	protected final GrowableCharArray	gca = new GrowableCharArray(true); 

	/**
	 * <p>Create empty Clob</p>
	 */
	InMemoryLittleClob(){
	}

	/**
	 * <p>Create Clob with the initial content</p>
	 * @param content initial content
	 */
	InMemoryLittleClob(final char[] content){
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else{
			gca.append(content);
		}
	}

	/**
	 * <p>Create Clob with the initial content
	 * @param content initial content
	 */
	InMemoryLittleClob(final String content){
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else{
			gca.length(content.length());
			content.getChars(0,content.length(),gca.toArray(),0);
		}
	}
	
	@Override
	public long length() throws SQLException {
		return gca.length();
	}

	@Override
	public String getSubString(final long pos, final int length) throws SQLException {
		if (length < 0) {
			throw new IllegalArgumentException("Length ["+length+"] is negative"); 
		}
		else {
			checkPosition(pos,true);
			final int	currentLen = (int) Math.min(length,length()-(pos-1));
			
			return new String(gca.toArray(),(int)(pos - 1),currentLen);
		}
	}

	@Override
	public Reader getCharacterStream() throws SQLException {
		return getCharacterStream(1,length());
	}

	@Override
	public InputStream getAsciiStream() throws SQLException {
		return new ByteArrayInputStream(getSubString(1,(int)length()).getBytes());
	}

	@Override
	public long position(final String searchstr, final long start) throws SQLException {
		if (searchstr == null) {
			throw new NullPointerException("String to search can't be null");
		}
		else {
			checkPosition(start,true);
			
			if (length()-(start-1) < searchstr.length()) {
				return 0;
			}
			else {
				final char[]	content = gca.toArray(), pattern = searchstr.toCharArray();

next:			for (int index = (int)(start-1), maxIndex = (int) length()-pattern.length; index < maxIndex; index++) {
					if (content[index] == pattern[0]) {
						for (int subIndex = 1, maxSubIndex = pattern.length; subIndex < maxSubIndex; subIndex++) {
							if (pattern[subIndex] != content[index+subIndex]) {
								continue next;
							}
						}
						return index+1;
					}
				}
				return 0;
			}
		}
	}

	@Override
	public long position(final Clob searchstr, final long start) throws SQLException {
		if (searchstr == null) {
			throw new NullPointerException("Clob to search can't be null");
		}
		else {
			return position(searchstr.getSubString(1,(int)searchstr.length()),start);
		}
	}

	@Override
	public int setString(final long pos, final String str) throws SQLException {
		return setString(pos,str,0,str.length());
	}

	@Override
	public int setString(final long pos, final String str, final int offset, final int len) throws SQLException {
		if (str == null) {
			throw new NullPointerException("Array to add can't be null");
		}
		else if (offset < 0 || offset >= str.length()) {
			throw new IllegalArgumentException("Offset ["+offset+"] outside the range 0.."+(str.length()-1)); 
		}
		else if (offset + len < 0 || offset + len > str.length()) {
			throw new IllegalArgumentException("Offset + len ["+(offset + len)+"] outside the range 0.."+str.length()); 
		}
		else {
			checkPosition(pos,false);
			
			if ((pos-1) + len> length()) {
				final int	oldLen = gca.length();
				
				gca.length((int)(pos-1 + len));
				if (gca.length() > oldLen) {
					final char[]	content = gca.toArray();
					
					for (int index = oldLen, maxIndex = gca.length(); index < maxIndex; index++) {
						content[index] = ' ';
					}
				}
			}
			
			str.getChars(0,str.length(),gca.toArray(),(int)(pos-1));
			return 0;
		}
	}
 
	@Override
	public OutputStream setAsciiStream(final long pos) throws SQLException {
		checkPosition(pos,false);
		return new DirectOutputStream(gca,(int)(pos-1));
	}

	@Override
	public Writer setCharacterStream(final long pos) throws SQLException {
		checkPosition(pos,false);
		return new DirectWriter(gca,(int)(pos-1));
	}

	@Override
	public void truncate(final long len) throws SQLException {
		if (len < 0) {
			throw new SQLException("Length ["+len+"] is negative"); 
		}
		else if (len > length()) {
			throw new SQLException("Length to truncate ["+len+"] is greater than current length ["+length()+"]"); 
		}
		else {
			gca.length((int)len);
		}
	}

	@Override
	public void free() throws SQLException {
		gca.clear();
	}

	@Override
	public Reader getCharacterStream(final long pos, final long length) throws SQLException {
		checkPosition(pos,true);
		return new CharArrayReader(gca.toArray(),(int)(pos-1),(int)Math.min(gca.length()-(pos-1),length));
	}

	@Override
	public String toString() {
		return "SimpleLittleClob [length=" + gca.length() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((gca == null) ? 0 : gca.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		InMemoryLittleClob other = (InMemoryLittleClob) obj;
		if (gca == null) {
			if (other.gca != null) return false;
		} else if (!gca.equals(other.gca)) return false;
		return true;
	}

	protected void checkPosition(final long pos, final boolean inside) throws SQLException {
		if (pos < 1) {
			throw new SQLException("Position ["+pos+"] is less than 1"); 
		}
		else if (pos >= Integer.MAX_VALUE) {
			throw new SQLException("Position ["+pos+"] is too long for the given implementation!"); 
		}
		else if (inside && pos > length()) {
			throw new SQLException("Position ["+pos+"] is greater then Blob size ["+length()+"]"); 
		}
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
		}
	}
}
