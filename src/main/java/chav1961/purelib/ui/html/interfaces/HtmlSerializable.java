package chav1961.purelib.ui.html.interfaces;

import java.io.IOException;
import java.io.Writer;

import chav1961.purelib.json.JsonNode;

public interface HtmlSerializable {
	void serialize(Writer writer) throws IOException;
	
	void processRequest(final JsonNode request, final Writer response) throws IOException; 
}
