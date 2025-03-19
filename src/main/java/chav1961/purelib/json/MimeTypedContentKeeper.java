package chav1961.purelib.json;

import java.io.Serializable;
import java.util.Arrays;

import chav1961.purelib.basic.MimeType;

/**
 * <p>This class is used to keep MIME typed content. It contains byte array representation of the content and
 * MIME type associated.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public class MimeTypedContentKeeper implements Serializable {
	private static final long 	serialVersionUID = -2548073480706654963L;
	private static final byte[]	EMPTY = new byte[0]; 

	private MimeType	mime = MimeType.MIME_OCTET_STREAM;
	private byte[]		content = EMPTY;
	
	/**
	 * <p>Constructor of the class instance</p>
	 */
	public MimeTypedContentKeeper() {
		this.content = EMPTY;
	}
	
	/**
	 * <p>Constructor of the class instance</p>
	 * @param mime mime type associated. Can't be null.
	 * @param content content to keep. Can't be null but can be empty.
	 * @throws NullPointerException on any parameter is null
	 */
	public MimeTypedContentKeeper(final MimeType mime, final byte[] content) throws NullPointerException {
		if (mime == null) {
			throw new NullPointerException("MIME type can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			this.mime = mime;
			this.content = content;
		}
	}
	
	/**
	 * <p>Get MIME type associated.</p>
	 * @return MIME type associated. Can't be null
	 */
	public MimeType getMimeType() {
		return mime;
	}
	
	/**
	 * <p>Set associated MIME type</p>
	 * @param mime MIME type to set. Can't be null.
	 * @throws NullPointerException mime type to set is null
	 */
	public void setMimeType(final MimeType mime) throws NullPointerException {
		if (mime == null) {
			throw new NullPointerException("MIME type can't be null");
		}
		else {
			this.mime = mime;
		}
	}
	
	/**
	 * <p>Get content</p>
	 * @return content kept. Can't be null but can be empty.
	 */
	public byte[] getContent() {
		return content;
	}
	
	/**
	 * <p>Set content</p>
	 * @param content content to set. Can't be null but can be empty
	 * @throws NullPointerException content to set is null
	 */
	public void setContent(final byte[] content) throws NullPointerException {
		if (content == null) {
			throw new NullPointerException("Content to set can't be null"); 
		}
		else {
			this.content = content;
		}
	}
	
	@Override
	public String toString() {
		return "MimeTypedContentKeeper[MIME=" + mime +", content length = "+content.length+"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(content);
		result = prime * result + ((mime == null) ? 0 : mime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MimeTypedContentKeeper other = (MimeTypedContentKeeper) obj;
		if (!Arrays.equals(content, other.content)) return false;
		if (mime == null) {
			if (other.mime != null) return false;
		} else if (!mime.equals(other.mime)) return false;
		return true;
	}
}
