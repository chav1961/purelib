package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.DebuggingException;

public interface ClassWrapper {
	Class<?> contentType() throws DebuggingException;
	String[] getFieldNames() throws DebuggingException;
	FieldWrapper getClassField(String name) throws DebuggingException;
	String[] getMethodNames() throws DebuggingException;
}
