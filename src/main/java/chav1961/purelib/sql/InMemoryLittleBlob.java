package chav1961.purelib.sql;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import chav1961.purelib.basic.growablearrays.GrowableByteArray;

/**
 * <p>This class implements in-memory Blob to use in the SQL.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.sql JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public class InMemoryLittleBlob implements Blob {
	protected final GrowableByteArray	gba = new GrowableByteArray(true);

	/**
	 * <p>Create empty Blob</p> 
	 */
	InMemoryLittleBlob() {
	}

	/**
	 * <p>Create Blob with the initial content</p>
	 * @param content initial content of the Blob
	 */
	InMemoryLittleBlob(final byte[] content) {
		if (content == null) {
			throw new NullPointerException("Content array can't be null");
		}
		else {
			gba.append(content);
		}
	}

	@Override
	public long length() throws SQLException {
		return gba.length();
	}

	@Override
	public byte[] getBytes(final long pos, final int length) throws SQLException {
		if (length < 0) {
			throw new IllegalArgumentException("Length ["+length+"] is negative"); 
		}
		else {
			checkPosition(pos,true);
			final int		size = (int) Math.min(length,length()-(pos-1));
			final byte[]	result = new byte[size];
			
			System.arraycopy(gba.toArray(),(int)(pos-1),result,0,size);
			return result;
		}
	}

	@Override
	public InputStream getBinaryStream() throws SQLException {
		return getBinaryStream(1,length());
	}

	@Override
	public long position(final byte[] pattern, final long start) throws SQLException {
		if (pattern == null) {
			throw new NullPointerException("Pattern array can't be null");
		}
		else {
			checkPosition(start,true);
			
			if (length()-(start-1) < pattern.length) {
				return 0;
			}
			else {
				final byte[]	content = gba.toArray();
				
next:			for (int index = (int)(start-1), maxIndex = gba.length()-pattern.length; index < maxIndex; index++) {
					if (content[index] == pattern[0]) {
						for (int subIndex = 1, maxSubIndex = pattern.length; subIndex < maxSubIndex; subIndex++) {
							if (pattern[subIndex] != content[index+subIndex]) {
								continue next;
							}
						}
						return index + 1;
					}
				}
				return 0;
			}
		}
	}

	@Override
	public long position(final Blob pattern, final long start) throws SQLException {
		if (pattern == null) {
			throw new NullPointerException("Pattern Blob can't be null");
		}
		else {
			return position(pattern.getBytes(1,(int)pattern.length()),start);
		}
	}

	@Override
	public int setBytes(final long pos, final byte[] bytes) throws SQLException {
		if (bytes == null) {
			throw new NullPointerException("Array to add can't be null");
		}
		else {
			return setBytes(pos,bytes,0,bytes.length);
		}
	}

	@Override
	public int setBytes(final long pos, final byte[] bytes, final int offset, final int len) throws SQLException {
		if (bytes == null) {
			throw new NullPointerException("Array to add can't be null");
		}
		else if (offset < 0 || offset >= bytes.length) {
			throw new IllegalArgumentException("Offset ["+offset+"] outside the range 0.."+(bytes.length-1)); 
		}
		else if (offset + len < 0 || offset + len > bytes.length) {
			throw new IllegalArgumentException("Offset + len ["+(offset + len)+"] outside the range 0.."+bytes.length); 
		}
		else {
			checkPosition(pos,false);
			if ((pos-1) + len > gba.length()) {
				final int	oldLen = gba.length();
				
				gba.length((int) ((pos-1)+len));
				if (gba.length() > oldLen) {
					final byte[]	content = gba.toArray();
					
					for (int index = oldLen, maxIndex = gba.length(); index < maxIndex; index++) {
						content[index] = 0;
					}
				}
			}
			System.arraycopy(bytes,offset,gba.toArray(),(int)(pos-1),len);
			return 0;
		}
	}

	@Override
	public OutputStream setBinaryStream(final long pos) throws SQLException {
		checkPosition(pos,false);
		return new DirectOutputStream(gba,(int)(pos-1));
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
			gba.length((int)len);
		}
	}

	@Override
	public void free() throws SQLException {
		gba.clear();
	}

	@Override
	public InputStream getBinaryStream(final long pos, final long length) throws SQLException {
		return new ByteArrayInputStream(getBytes(pos,(int)length));
	}

	@Override
	public String toString() {
		return "SimpleLittleBlob [length=" + gba.length() + "]";
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
		private GrowableByteArray	content;
		
		private DirectOutputStream(final GrowableByteArray gba, final int displacement) {
			this.content = gba;
			this.displacement = displacement;
		}
		
		@Override
		public void write(int b) throws IOException {
			if (displacement < content.length()) {
				content.toArray()[displacement++] = (byte)b;
			}
			else {
				content.append((byte)b);
				displacement++;
			}
		}

		@Override
	    public void write(byte b[], int off, int len) throws IOException {
	        if (b == null) {
	            throw new NullPointerException();
	        } 
	        else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
	            throw new IndexOutOfBoundsException();
	        } 
	        else if (len == 0) {
	            return;
	        }
	        if (displacement + len > content.length()) {
	        	content.length(displacement + len);
	        }
	        System.arraycopy(b, off, content.toArray(), displacement, len);
	        displacement += len;
	    }
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((gba == null) ? 0 : gba.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		InMemoryLittleBlob other = (InMemoryLittleBlob) obj;
		if (gba == null) {
			if (other.gba != null) return false;
		} else if (!gba.equals(other.gba)) return false;
		return true;
	}
}
