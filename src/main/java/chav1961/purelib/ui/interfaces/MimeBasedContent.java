package chav1961.purelib.ui.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import chav1961.purelib.basic.MimeType;

public interface MimeBasedContent {
	MimeType getMimeType();
	boolean isContentFilled();
	long getContentSize() throws IOException;
	void setMimeType(MimeType type) throws IOException;
	InputStream getContent() throws IOException;
	OutputStream putContent() throws IOException;
	void clearContent() throws IOException;
}
