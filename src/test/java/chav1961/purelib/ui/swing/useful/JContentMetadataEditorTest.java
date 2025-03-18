package chav1961.purelib.ui.swing.useful;


import java.awt.Dimension;
import java.net.URI;

import javax.swing.JOptionPane;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

@Tag("UITestCategory")
public class JContentMetadataEditorTest {

	@Test
	public void test() throws LocalizationException, ContentException {
		final JContentMetadataEditor	ed = new JContentMetadataEditor(PureLibSettings.PURELIB_LOCALIZER);
		final URI						localizerURI = URI.create(Localizer.LOCALIZER_SCHEME+":xml:root://chav1961.purelib.ui.swing.useful.JContentMetadataEditorTest/chav1961/purelib/i18n/localization.xml");
		
		ed.setValue(new MutableContentNodeMetadata("name",String.class,"test",localizerURI,"testSet1","testSet2","testSet3",new FieldFormat(String.class),URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/test"), null));
		ed.setPreferredSize(new Dimension(400,270));
		
		JOptionPane.showMessageDialog(null,ed);
		final ContentNodeMetadata		md = ed.getContentNodeMetadataValue();
		
		
	}
}
