package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.DebuggingException;

public interface StackWrapper {
	String[] getVarNames() throws DebuggingException;
	VariableWrapper getVar(String name) throws DebuggingException;
}
