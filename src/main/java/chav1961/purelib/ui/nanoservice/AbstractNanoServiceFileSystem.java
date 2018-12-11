package chav1961.purelib.ui.nanoservice;

import java.io.IOException;
import java.net.URI;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.AbstractFileSystem;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class AbstractNanoServiceFileSystem extends AbstractFileSystem {
	protected AbstractNanoServiceFileSystem() {
		
	}

	@Override
	public boolean canServe(final URI uriSchema) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FileSystemInterface newInstance(final URI uriSchema) throws EnvironmentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileSystemInterface clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
