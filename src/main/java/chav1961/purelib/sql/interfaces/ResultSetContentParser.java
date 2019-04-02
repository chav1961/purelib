package chav1961.purelib.sql.interfaces;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSetMetaData;
import java.util.Hashtable;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.RsMetaDataElement;

public interface ResultSetContentParser {
	boolean canServe(URI request);
	Hashtable<String,String[]> filter(Hashtable<String,String[]> source);
	ResultSetContentParser newInstance(URL access, int resultSetType, RsMetaDataElement[] content, SubstitutableProperties options) throws IOException;
	ResultSetMetaData getMetaData() throws IOException;
	AbstractContent getAccessContent();
}
