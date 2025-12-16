package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.net.URI;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.AppArgumentsOwner;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;

public class JRichFrame extends JFrame implements LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner, NodeMetadataOwner, AppArgumentsOwner {
	private static final long serialVersionUID = 5758385242098063342L;

	public static final String				PROP_APP_RECTANGLE = "appRectangle";
	
	private static final Pattern			RECT_PATTERN = Pattern.compile("\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*");

	private final ContentMetadataInterface	mdi;
	private final ArgParser					args;
	private final SubstitutableProperties	settings;
	private final Localizer					localizer;
	private final JStateString				state;
	private final JMenuBar					menuBar;
	private final JPanel					content = new JPanel(new BorderLayout(5, 5));
	private boolean 						contentPrepared = false;

	protected JRichFrame(final ContentMetadataInterface mdi, final ArgParser args, final SubstitutableProperties settings) {
		if (mdi == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (args == null) {
			throw new NullPointerException("Applicaiton arguments can't be null");
		}
		else if (settings == null) {
			throw new NullPointerException("Applicaiton settings can't be null");
		}
		else {
			this.mdi = mdi;
			this.args = args;
			this.settings = settings;
			this.localizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
			
			PureLibSettings.PURELIB_LOCALIZER.push(localizer);
			PureLibSettings.PURELIB_LOCALIZER.addLocaleChangeListener(this);
	
			this.menuBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
			setJMenuBar(menuBar);
			this.state = new JStateString(localizer);
			SwingUtils.assignActionListeners(this.menuBar, this);
	
			SwingUtils.assignExitMethod4MainWindow(this, ()->exitApplication());
			add(content, BorderLayout.CENTER);
			contentPrepared = true;
			getContentPane().add(state, BorderLayout.SOUTH);
			
			if (settings.containsKey(PROP_APP_RECTANGLE)) {
				final Matcher	m = RECT_PATTERN.matcher(settings.getProperty(PROP_APP_RECTANGLE));
				
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
	public ContentNodeMetadata getNodeMetadata() {
		return mdi.getRoot();
	}
	
	@Override
	public ArgParser getAppArguments() {
		return args;
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(state, oldLocale, newLocale);
		SwingUtils.refreshLocale(menuBar, oldLocale, newLocale);
		SwingUtils.refreshLocale(content, oldLocale, newLocale);
	}
	
	@Override
	public Container getContentPane() {
		if (content == null || !contentPrepared) {
			return super.getContentPane();
		}
		else {
			return content; 
		}
	}
	
	protected void exitApplication() {
		setVisible(false);
		dispose();
	}
	
	@OnAction("action:builtin:/builtin.languages")
    protected void language(final Hashtable<String,String[]> langs) throws LocalizationException {
		PureLibSettings.PURELIB_LOCALIZER.setCurrentLocale(SupportedLanguages.valueOf(langs.get("lang")[0]).getLocale());
	}	

	protected SubstitutableProperties getSettings() {
		return settings;
	}
}
