package chav1961.purelib.ui.swing.useful;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;

public class JVisualsTest {
//	@Test
//	public void dateSelectionDialogText() throws LocalizationException {
//		JOptionPane.showMessageDialog(null,new JDateSelectionDialog(PureLibSettings.PURELIB_LOCALIZER));
//	}

//	@Test
//	public void fileSelectionDialogText() throws LocalizationException, IOException {
//		try(final FileSystemInterface	fsi = new FileSystemOnFile(new File("./").toURI())) {
//			JFileSelectionDialog.select((Dialog)null,PureLibSettings.PURELIB_LOCALIZER,fsi,JFileSelectionDialog.OPTIONS_FOR_OPEN|JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE);
//		}
//	}

	@Test
	public void fileSystemSelectionDialogText() throws LocalizationException, IOException, ContentException{
		JOptionPane.showMessageDialog(null,new JFileSystemChanger(PureLibSettings.PURELIB_LOCALIZER));
	}
	
}
