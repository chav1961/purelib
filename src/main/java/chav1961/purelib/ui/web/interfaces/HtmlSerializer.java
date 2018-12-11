package chav1961.purelib.ui.web.interfaces;

import java.io.IOException;
import java.io.OutputStream;

public interface HtmlSerializer {
	OutputStream toOutputStream() throws IOException;
	void toOutoutStream(OutputStream content) throws IOException;
}
