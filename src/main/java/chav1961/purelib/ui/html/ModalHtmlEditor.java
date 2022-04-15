package chav1961.purelib.ui.html;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Locale;

import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.ModuleExporter;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.html.interfaces.HtmlSerializable;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.UIItemState;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class ModalHtmlEditor<T> implements LocaleChangeListener, AutoCloseable, JComponentMonitor, ModuleExporter, LoggerFacadeOwner, HtmlSerializable {
	private final ContentMetadataInterface 	mdi;
	private final Localizer 				localizer;
	private final LoggerFacade 				logger;
	private final SimpleURLClassLoader 		loader;
	private final URL 						leftIcon;
	private final 							T instance;
	private final FormManager<Object,T> 	formMgr;
	private final int 						numberOfBars;
	private final boolean 					tooltipsOnFocus;
	private final UIItemState 				itemState;
	private volatile boolean				localeChanged = false;
	
	public ModalHtmlEditor(final ContentMetadataInterface mdi, final Localizer localizer, final LoggerFacade logger, final SimpleURLClassLoader loader, final URL leftIcon, final T instance, final FormManager<Object,T> formMgr, final int numberOfBars, final boolean tooltipsOnFocus, final UIItemState itemState) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		if (mdi == null) {
			throw new NullPointerException("Metadata interface can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (loader == null) {
			throw new NullPointerException("Loader can't be null");
		}
		else if (instance == null) {
			throw new NullPointerException("Instance can't be null");
		}
		else if (formMgr == null) {
			throw new NullPointerException("Form manager can't be null");
		}
		else if (numberOfBars < 1) {
			throw new IllegalArgumentException("Bars count must be positive");
		}
		else if (mdi.getRoot().getLocalizerAssociated() == null) {
			throw new IllegalArgumentException("No localizer associated in the metadata model!");
		}
		else if (itemState == null) {
			throw new NullPointerException("Item state monitor can't be null!");
		}
		else {
			this.mdi = mdi;
			this.localizer = localizer;
			this.logger = logger;
			this.loader = loader;
			this.leftIcon = leftIcon;
			this.instance = instance;
			this.formMgr = formMgr;
			this.numberOfBars = numberOfBars;
			this.tooltipsOnFocus = tooltipsOnFocus;
			this.itemState = itemState;
		}		
	}
	
	@Override
	public LoggerFacade getLogger() {
		return logger;
	}

	@Override
	public Module[] getUnnamedModules() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponentInterface component, Object... parameters) throws ContentException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		localeChanged = true;
	}

	@Override
	public boolean refreshRequires() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void serialize(final Writer writer) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void processRequest(final JsonNode request, final Writer response) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
