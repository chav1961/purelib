package chav1961.purelib.streams.interfaces;

import java.io.Flushable;
import java.io.IOException;

public interface MultipartOutputStreamContainer<T> extends Flushable {
	void appendNewEntity(MultipartStreamEntity<T> entity) throws IOException;
	void closeEntity() throws IOException;
}
