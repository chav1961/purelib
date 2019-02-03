package chav1961.purelib.ui.swing;

import java.net.URI;
import java.sql.Date;

import javax.swing.JOptionPane;

import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class VisualControlsTest {
	@Test
	public void basicTest() {
	}

	@Test
	public void test() throws PreparationException, SyntaxException, LocalizationException, ContentException {
		final ContentMetadataInterface	metadata = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final JDateFieldWithMeta	item = new JDateFieldWithMeta(
													metadata.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+ContentModelFactory.APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/date1"))[0]
													, new FieldFormat(Date.class,"")
													, (event,meta,component,parameters)->{return true;}													
													);
		
		JOptionPane.showMessageDialog(null,item);
	}

}