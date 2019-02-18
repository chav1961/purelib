package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.DebuggingException;

public interface ObjectWrapper {
	Class<?> contentType() throws DebuggingException;
	ObjectWrapper superValue() throws DebuggingException;
	String[] fields() throws DebuggingException;
	String[] declaredFields() throws DebuggingException;
	String[] methods() throws DebuggingException;
	String[] declaredMethods() throws DebuggingException;
	Object get(String field) throws DebuggingException;
	void set(String field, Object value) throws DebuggingException;
	Object invoke(String methodSignature, Object... parameters) throws DebuggingException;
}
