package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.net.URI;

import javax.swing.JComponent;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfacers.Format;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class LogicControlsTest {
	@Test
	public void jColorPickerTest() throws SyntaxException, LocalizationException, PreparationException, ContentException {
		final ContentMetadataInterface 	total = ContentModelFactory.forAnnotatedClass(LogicControlTestRabbit.class);
		final ContentNodeMetadata		metadata = total.byUIPath(URI.create(""));
		final TestMonitor				monitor = new TestMonitor(metadata);
		final JColorPickerWithMeta		cp = new JColorPickerWithMeta(metadata,metadata.getFormatAssociated(), monitor);
		
	}
	
	private static class TestMonitor implements JComponentMonitor {
		private final ContentNodeMetadata	metadata;
		
		public TestMonitor(ContentNodeMetadata metadata) {
			this.metadata = metadata;
		}

		@Override
		public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponent component, final Object... parameters) throws ContentException {
			// TODO Auto-generated method stub
			Assert.assertEquals(this.metadata,metadata);
			
			return true;
		}
	}
}

@LocaleResource(value="value",tooltip="tooltip")
class LogicControlTestRabbit {
	@Format("")
	final Color		currentColor = Color.BLACK;	
}