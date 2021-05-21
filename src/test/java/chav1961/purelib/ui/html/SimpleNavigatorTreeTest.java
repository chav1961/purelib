package chav1961.purelib.ui.html;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class SimpleNavigatorTreeTest {

	@Test
	public void basicTest() throws EnvironmentException, IOException {
		final ContentMetadataInterface		mdi = ContentModelFactory.forXmlDescription(this.getClass().getResourceAsStream("/chav1961/purelib/ui/html/model.xml"));
		final SimpleNavigatorTree<String>	snt = new SimpleNavigatorTree<String>(PureLibSettings.PURELIB_LOCALIZER, mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")));
		final StringWriter					wr = new StringWriter();
		
		snt.serialize(wr);
		wr.flush();
		System.err.println("Wr="+wr);
		
	}
}
