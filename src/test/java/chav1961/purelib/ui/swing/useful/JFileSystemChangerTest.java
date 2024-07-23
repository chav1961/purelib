package chav1961.purelib.ui.swing.useful;


import java.io.IOException;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;

@Tag("UITestCategory")
public class JFileSystemChangerTest {
	@Test
	public void test() throws LocalizationException, ContentException, IOException {
		final JFileSystemChanger	jfc = new JFileSystemChanger(PureLibSettings.PURELIB_LOCALIZER, (owner,accept)->true);
		
		JOptionPane.showMessageDialog(null, jfc);
	}
}
