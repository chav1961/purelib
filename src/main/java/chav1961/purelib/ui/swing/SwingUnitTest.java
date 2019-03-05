package chav1961.purelib.ui.swing;

import java.net.URI;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.exceptions.DebuggingException;

public class SwingUnitTest {
	public SwingUnitTest(final JComponent root) {
		
	}
	
	public JComponent getComponent() {
		return null;
	}
	
	public String getContent() throws DebuggingException {
		return null;
	}
	
	public SwingUnitTest seek(final URI name) throws DebuggingException {
		return this;
	}
	
	public SwingUnitTest mouse(final int x, final int y, final int modifiers) throws DebuggingException {
		return this;
	}
	
	public SwingUnitTest keys(String keys) throws DebuggingException {
		return this;
	}
	
	public SwingUnitTest keys(KeyStroke... keys) throws DebuggingException {
		return this;
	}

	public SwingUnitTest click() throws DebuggingException {
		return this;
	}

	public SwingUnitTest select(Object content) throws DebuggingException {
		return this;
	}
}
