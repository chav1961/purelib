package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.net.URI;

import javax.swing.JComponent;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class LogicControlsTest {
	@Test
	public void jColorPickerTest() throws SyntaxException, LocalizationException, PreparationException, ContentException {
		final ContentMetadataInterface 	total = ContentModelFactory.forAnnotatedClass(LogicControlTestRabbit.class);
		final ContentNodeMetadata		metadata = total.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+LogicControlTestRabbit.class.getName()+"/currentColor"))[0];
		final TestMonitor				monitor = new TestMonitor(metadata);
		final JColorPickerWithMeta		cp = new JColorPickerWithMeta(metadata,metadata.getFormatAssociated(), monitor);
		
	}
	
	private static class TestMonitor implements JComponentMonitor {
		private final ContentNodeMetadata	metadata;
		
		public TestMonitor(ContentNodeMetadata metadata) {
			this.metadata = metadata;
		}

		@Override
		public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) throws ContentException {
			Assert.assertEquals(this.metadata,metadata);
			
			return true;
		}
	}
}

@LocaleResourceLocation("i18n:xml:file:./src/main/resources/chav1961/purelib/i18n/localization.xml")
@LocaleResource(value="value",tooltip="tooltip")
class LogicControlTestRabbit {
	@Format("m")
	final Color		currentColor = Color.BLACK;	
}