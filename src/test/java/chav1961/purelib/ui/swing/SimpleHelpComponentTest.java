package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.junit.Test;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.DebuggingLocalizer;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;

public class SimpleHelpComponentTest {
	private static final Map<String,SubstitutableProperties>	map = new HashMap<>();
	private static final Map<String,String>						help = new HashMap<>();
	
	static {
		final SubstitutableProperties	props = new SubstitutableProperties();
		
		props.putAll(Utils.mkProps("help","uri(help)","help2","uri(help2)"));
		map.put("ru",props);
		map.put("en",props);
		
		try{help.put("help",Utils.fromResource(SimpleHelpComponentTest.class.getResource("helpcontent.cre")));
			help.put("help2",Utils.fromResource(SimpleHelpComponentTest.class.getResource("referencedhelpcontent.cre")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	@Test
	public void basicTest() throws IOException, LocalizationException, ContentException, InterruptedException {
		@SuppressWarnings("resource")
		final Localizer	root = new PureLibLocalizer();
		final JFrame	frame = new JFrame();

		try(final Localizer	localizer = new DebuggingLocalizer(map,help)) {
			
			root.push(localizer);
			frame.setSize(new Dimension(640,480));
			frame.setMinimumSize(new Dimension(640,480));
			frame.setPreferredSize(new Dimension(640,480));
			frame.getContentPane().add(
					new SimpleHelpComponent(localizer,"help")
					,BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
			Thread.sleep(10000);		
		} finally {
			root.pop();
		}
	}

	@Test
	public void empty() {
	}
}
