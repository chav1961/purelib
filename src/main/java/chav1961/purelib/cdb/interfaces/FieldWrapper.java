package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.DebuggingException;

public interface FieldWrapper {
	Class<?> contentType() throws DebuggingException;
	String getName() throws DebuggingException;
	Object get() throws DebuggingException;
}
