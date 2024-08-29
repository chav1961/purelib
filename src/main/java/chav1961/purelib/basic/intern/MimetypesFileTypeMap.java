package chav1961.purelib.basic.intern;

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;

public class MimetypesFileTypeMap {
	public String getContentType(final String fileName) throws IllegalArgumentException {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty");
		}
		else if (fileName.endsWith(".cre")) {
			return MimeType.MIME_CREOLE_TEXT.toString();
		}
		else if (fileName.endsWith(".css")) {
			return MimeType.MIME_CSS_TEXT.toString();
		}
		else if (fileName.equals("favicon.ico")) {
			return MimeType.MIME_FAVICON.toString();
		}
		else if (fileName.endsWith(".txt")) {
			return MimeType.MIME_PLAIN_TEXT.toString();
		}
		else if (fileName.endsWith(".html")) {
			return MimeType.MIME_HTML_TEXT.toString();
		}
		else {
			return MimeType.MIME_OCTET_STREAM.toString();
		}
	}
}
