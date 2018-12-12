package chav1961.purelib.ui.swing;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.junit.Test;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;

public class LocalizedToolBarTest {
//	@Test
	public void basicTest() throws LocalizationException, IllegalArgumentException, NullPointerException, IOException, InterruptedException {
		final Localizer	localizer = LocalizerFactory.getLocalizer(URI.create(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/test"));
		final JFrame	frame = new JFrame();
		
		frame.setSize(new Dimension(640,480));
		frame.setMinimumSize(new Dimension(640,480));
		frame.setPreferredSize(new Dimension(640,480));
		frame.getContentPane().add(
				new PseudoLocalizedToolBar(localizer,Locale.forLanguageTag("ru"),Locale.forLanguageTag("en"))
				,BorderLayout.NORTH);
		frame.pack();
		frame.setVisible(true);
		Thread.sleep(10000);
	}
	
	@Test
	public void empty() {
	}
	
}

class PseudoLocalizedToolBar extends LocalizedToolBar {
	private static final long 	serialVersionUID = 1L;
	private static final String	TOOLTIP_1 = "key1";
	private static final String	TOOLTIP_2 = "key2";
	
	final 	JButton 			button1, button2;
	
	PseudoLocalizedToolBar(final Localizer localizer, final Locale locale1, final Locale locale2) throws LocalizationException, IllegalArgumentException {
		super(localizer);
		add(this.button1 = createButton(new AbstractAction(){private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				try{localizer.setCurrentLocale(locale1);
					button1.setSelected(!button1.isSelected());
				} catch (LocalizationException exc) {
				}
			}
		},this.getClass().getResource("filterGray.png"),this.getClass().getResource("filter.png")
		 ,this.getClass().getResource("filterOnGray.png"),this.getClass().getResource("filterOn.png"),TOOLTIP_1),TOOLTIP_1);
		add(this.button2 = createButton(new AbstractAction(){private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				try{localizer.setCurrentLocale(locale2);
				} catch (LocalizationException exc) {
				}
			}
		},this.getClass().getResource("insertGray.png"),this.getClass().getResource("insert.png"),TOOLTIP_2),TOOLTIP_2);
		localizer.addLocaleChangeListener(this);
	}

	@Test
	public void empty() {
	}
}
