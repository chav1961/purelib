package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.DebuggingException;

public interface ArrayWrapper {
	Class<?> contentType() throws DebuggingException;
	int getLength() throws DebuggingException;
	Object get(int index) throws DebuggingException;
	Object get() throws DebuggingException;
}
