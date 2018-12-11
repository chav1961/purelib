package chav1961.purelib.ui.nanoservice.interfaces;

import java.io.InputStream;

public interface MultipartContent {
	int getPartCount();
	String[] getPartNames();
	InputStream getPartContent(String partName);
}
