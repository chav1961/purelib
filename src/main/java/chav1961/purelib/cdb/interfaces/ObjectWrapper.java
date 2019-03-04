package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.DebuggingException;

public interface ObjectWrapper {
	Class<?> contentType() throws DebuggingException;
	String[] fields() throws DebuggingException;
	String[] methods() throws DebuggingException;
	Object get(String field) throws DebuggingException;
}
