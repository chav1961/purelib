package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.DebuggingException;

public interface InstanceWrapper {
	String[] getFieldNames() throws DebuggingException;
	FieldWrapper getInstanceField(String name) throws DebuggingException;
	FieldWrapper getClassField(String name) throws DebuggingException;
	String[] getMethodNames() throws DebuggingException;
	String[] getMethodSignatures(String methodName) throws DebuggingException;
	MethodWrapper getInstanceMethod(String name,String signature) throws DebuggingException;
	MethodWrapper getClassMethod(String name,String signature) throws DebuggingException;
	ClassWrapper getInstanceClass() throws DebuggingException;
}
