package chav1961.purelib.basic.intern;

import chav1961.purelib.basic.PureLibSettings;

public class MimetypesFileTypeMap {
	public String getContentType(final String fileName) throws IllegalArgumentException {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty");
		}
		else if (fileName.endsWith(".cre")) {
			return PureLibSettings.MIME_CREOLE_TEXT.toString();
		}
		else if (fileName.endsWith(".css")) {
			return PureLibSettings.MIME_CSS_TEXT.toString();
		}
		else if (fileName.equals("favicon.ico")) {
			return PureLibSettings.MIME_FAVICON.toString();
		}
		else if (fileName.endsWith(".txt")) {
			return PureLibSettings.MIME_PLAIN_TEXT.toString();
		}
		else {
			return PureLibSettings.MIME_OCTET_STREAM.toString();
		}
	}
}
