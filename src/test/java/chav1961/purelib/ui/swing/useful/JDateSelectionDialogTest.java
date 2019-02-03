package chav1961.purelib.ui.swing.useful;

import java.awt.HeadlessException;

import javax.swing.JOptionPane;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;

public class JDateSelectionDialogTest {

	@Test
	public void test() throws HeadlessException, LocalizationException {
		JOptionPane.showMessageDialog(null,new JDateSelectionDialog(PureLibSettings.PURELIB_LOCALIZER));
	}
}
