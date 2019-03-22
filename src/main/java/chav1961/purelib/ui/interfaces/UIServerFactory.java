package chav1961.purelib.ui.interfaces;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;

public interface UIServerFactory {
	UIServer newServer(SubstitutableProperties props) throws ContentException, EnvironmentException;
}
