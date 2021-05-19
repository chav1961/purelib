package chav1961.purelib.ui.html.interfaces;

import java.io.IOException;
import java.io.Writer;

public interface HtmlSerializable {
	void serialize(Writer writer) throws IOException;
}
