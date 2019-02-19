package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.DebuggingException;

public interface StackWrapper {
	ObjectWrapper getThis() throws DebuggingException;
	String[] getVarNames() throws DebuggingException;
	VariableWrapper getVar(String name) throws DebuggingException;
}
