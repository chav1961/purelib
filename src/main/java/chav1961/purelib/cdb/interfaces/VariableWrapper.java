package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.DebuggingException;

public interface VariableWrapper {
	String getName() throws DebuggingException;
	Class<?> getType() throws DebuggingException;
	Object getValue() throws DebuggingException;
	void setValue(Object newValue) throws DebuggingException;
}
