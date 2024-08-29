package chav1961.purelib.json;

import java.io.Serializable;

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;

public class MimeTypedContentKeeper implements Serializable {
	private static final long 	serialVersionUID = -2548073480706654963L;
	private static final byte[]	EMPTY = new byte[0]; 

	private MimeType	mime = MimeType.MIME_OCTET_STREAM;
	private byte[]		content = EMPTY;
	
	public MimeTypedContentKeeper() {
		this.content = EMPTY;
	}
	
	public MimeTypedContentKeeper(final MimeType mime, final byte[] content) {
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
	
	public MimeType getMimeType() {
		return mime;
	}
	
	public void setMimeType(final MimeType mime) {
		if (mime == null) {
			throw new NullPointerException("MIME type can't be null");
		}
		else {
			this.mime = mime;
		}
	}
	
	public byte[] getContent() {
		return content;
	}
	
	public void setContent(final byte[] content) {
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
}
