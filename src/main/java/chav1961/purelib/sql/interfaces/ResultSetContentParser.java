package chav1961.purelib.sql.interfaces;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSetMetaData;

import chav1961.purelib.sql.AbstractContent;

public interface ResultSetContentParser {
	boolean canServe(URI request);
	ResultSetContentParser newInstance(URL access, URI request) throws IOException;
	ResultSetMetaData getMetaData() throws IOException;
	AbstractContent getAccessContent();
}
