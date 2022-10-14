package chav1961.purelib.streams.interfaces;

import java.io.IOException;
import java.util.Hashtable;

public interface MultipartStreamEntity<T> {
	String getPartName() throws IOException;
	Hashtable<String, T> getPartProperties() throws IOException;
}