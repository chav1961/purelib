package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.DebuggingException;

public interface MethodWrapper {
	Class<?> owner() throws DebuggingException;
	String name() throws DebuggingException;
	String signature() throws DebuggingException;
}
