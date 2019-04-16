package chav1961.purelib.ui.swing.useful;

import java.io.IOException;
import java.util.Map;
import java.awt.Dialog;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.ui.AbstractWizardStep;
import chav1961.purelib.ui.interfaces.WizardStep;
import chav1961.purelib.ui.interfaces.WizardStep.ErrorProcessing;
import chav1961.purelib.fsys.FileSystemOnFile;

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

//	@Test
//	public void fileSystemSelectionDialogText() throws LocalizationException, IOException, ContentException{
//		JOptionPane.showMessageDialog(null,new JFileSystemChanger(PureLibSettings.PURELIB_LOCALIZER));
//	}

//	@Test
//	public void dialogContainerSimpleText() throws LocalizationException, IOException, ContentException{
//		final JButton					pressButton = new JButton("Press");
//		pressButton.setPreferredSize(new Dimension(100,100));
//		final JDialogContainer<?,?,?>	jdc = new JDialogContainer<>(PureLibSettings.PURELIB_LOCALIZER,(JDialog)null, "testSet1", pressButton);
//		
//		jdc.showDialog();
//	}
	
	@Test
	public void dialogContainerWizardText() throws LocalizationException, IOException, ContentException{
		final ErrorProcessing<String,ContinueMode>	proc = new ErrorProcessing<String,ContinueMode>() {
			@Override public void processError(String content, ContinueMode err, Object... parameters) throws FlowException, LocalizationException {}
			@Override public void processWarning(String content, ContinueMode err, Object... parameters) throws LocalizationException {}
		}; 
		final JDialogContainer<String,ContinueMode,JComponent>	jdc = new JDialogContainer<String,ContinueMode,JComponent>
									(PureLibSettings.PURELIB_LOCALIZER,(JDialog)null,"test",proc,
											new AbstractWizardStep<String, ContinueMode, JComponent>() {
												@Override public StepType getStepType() {return StepType.INITIAL;}
												@Override public String getCaption() {return "testSet1";}
												@Override public JComponent getContent() {return new JLabel("sdds");}
												@Override public void beforeShow(String content, Map<String, Object> temporary, ErrorProcessing<String, ContinueMode> err) throws FlowException {}
												@Override public void afterShow(String content, Map<String, Object> temporary, ErrorProcessing<String, ContinueMode> err) throws FlowException {}
											},
										new AbstractWizardStep<String, ContinueMode, JComponent>() {
											@Override public StepType getStepType() {return StepType.TERM_SUCCESS;}
											@Override public String getCaption() {return "testSet2";}
											@Override public JComponent getContent() {return new JLabel("12345");}
											@Override public void beforeShow(String content, Map<String, Object> temporary, ErrorProcessing<String, ContinueMode> err) throws FlowException {}
											@Override public void afterShow(String content, Map<String, Object> temporary, ErrorProcessing<String, ContinueMode> err) throws FlowException {}
										}
									);
		
		jdc.showDialog();
	}
	
}
