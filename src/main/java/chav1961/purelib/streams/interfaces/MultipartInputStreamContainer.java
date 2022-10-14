package chav1961.purelib.streams.interfaces;

import java.io.IOException;

public interface MultipartInputStreamContainer<T> extends Iterable<MultipartStreamEntity<T>>{
	String getCurrentPartName() throws IOException;
}
