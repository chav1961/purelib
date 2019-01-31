package chav1961.purelib.ui.swing;

import java.awt.Component;
import java.net.URI;

import chav1961.purelib.basic.exceptions.TestException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.interfaces.UITestInterface;

class SwingTestManager implements UITestInterface {
	SwingTestManager(final Class<Component> instanceClass, final Component uiRoot, final ContentNodeMetadata metadata) {
		
	}

	@Override
	public UITestInterface find(URI control) throws TestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UITestInterface find(ContentNodeMetadata metadata) throws TestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getControlType() throws TestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentNodeMetadata getMetadata() throws TestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue() throws TestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UITestInterface setValue(Object value) throws TestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validateValue(Object value) throws TestException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public UITestInterface commit() throws TestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UITestInterface acceptValue(Object value) throws TestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UITestInterface click() throws TestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UITestInterface go() throws TestException {
		// TODO Auto-generated method stub
		return null;
	}
}
