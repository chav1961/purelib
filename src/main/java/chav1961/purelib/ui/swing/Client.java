package chav1961.purelib.ui.swing;

import java.net.URI;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.JsonRPCFactory.Transport;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.useful.JStateString;

public class Client implements LocaleChangeListener {
	private final JFrame		frame;
	private final Localizer		localizer;
	private final Transport		transport;
	private final JMenuBar		menu;
	private final JStateString	stateString;
	
	public Client(final ContentMetadataInterface mdi, final Transport transport) throws LocalizationException, NullPointerException {
		if (mdi == null) {
			throw new NullPointerException("Metadata interface can't be null");
		}
		else if (transport == null) {
			throw new NullPointerException("Transport can't be null");
		}
		else {
			this.frame = new JFrame();
			this.transport = transport;
			this.localizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
			this.menu = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")),JMenuBar.class);
			this.stateString = new JStateString(this.localizer,10,true);
			
			PureLibSettings.PURELIB_LOCALIZER.push(this.localizer);
			localizer.addLocaleChangeListener(this);
			
			stateString.setAutomaticClearTime(Severity.error,1,TimeUnit.MINUTES);
			stateString.setAutomaticClearTime(Severity.warning,15,TimeUnit.SECONDS);
			stateString.setAutomaticClearTime(Severity.info,5,TimeUnit.SECONDS);
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}
}
