package chav1961.purelib.ui.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public interface ConstraintChecker<T> {
	public interface ConstraintCheckerCallback<T> {
		ContinueMode processError(T instance, Constraint invalid) throws ContentException;
	}
	
	boolean check(T instance) throws ContentException;
	boolean check(T instance, LoggerFacade logger) throws ContentException;
	boolean check(T instance, ConstraintCheckerCallback<T> callback) throws ContentException;
}
