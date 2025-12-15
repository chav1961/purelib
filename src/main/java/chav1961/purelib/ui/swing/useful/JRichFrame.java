package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;

public class JRichFrame extends JFrame implements LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner {
	private static final long serialVersionUID = 5758385242098063342L;

	public static final String				PROP_APP_RECTANGLE = "appRectangle";
	
	private static final Pattern			RECT_PATTERN = Pattern.compile("\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*");

	private final ContentMetadataInterface	mdi;
	private final Localizer					localizer;
	private final JStateString				state;
	private final JMenuBar					menuBar;
	private final JPanel					contentPane = new JPanel(new BorderLayout(5, 5));

	protected JRichFrame(final ContentMetadataInterface mdi, final SubstitutableProperties args, final SubstitutableProperties props) {
		this.mdi = mdi;
		this.localizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
		
		PureLibSettings.PURELIB_LOCALIZER.push(localizer);
		PureLibSettings.PURELIB_LOCALIZER.addLocaleChangeListener(this);

		this.menuBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
		setJMenuBar(menuBar);
		this.state = new JStateString(localizer);

		SwingUtils.assignExitMethod4MainWindow(this, ()->exitApplication());
		getContentPane().add(contentPane, BorderLayout.CENTER);
		getContentPane().add(state, BorderLayout.SOUTH);
		fillLocalizedStrings();
		
		if (props.containsKey(PROP_APP_RECTANGLE)) {
			final Matcher	m = RECT_PATTERN.matcher(props.getProperty(PROP_APP_RECTANGLE));
			
			if (m.find()) {
				final int	x = Integer.valueOf(m.group(1));
				final int	y = Integer.valueOf(m.group(2));
				final int	width = Integer.valueOf(m.group(3));
				final int	height = Integer.valueOf(m.group(4));
				
				setBounds(x, y, width, height);
				setPreferredSize(new Dimension(width, height));
			}
			else {
				SwingUtils.centerMainWindow(this, 0.85f);
			}
		}
		else {
			SwingUtils.centerMainWindow(this, 0.85f);
		}		
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	@Override
	public LoggerFacade getLogger() {
		return state;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}
	
	@Override
	public Container getContentPane() {
		if (contentPane == null) {
			return super.getContentPane();
		}
		else {
			return contentPane; 
		}
	}
	
	protected void exitApplication() {
		
	}

	protected void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}
}
