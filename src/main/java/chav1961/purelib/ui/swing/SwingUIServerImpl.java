package chav1961.purelib.ui.swing;

import java.io.IOException;
import java.net.URI;

import javax.swing.JFrame;
import javax.swing.text.html.parser.ContentModel;

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.UIServer;
import chav1961.purelib.ui.interfaces.WizardStep;

public class SwingUIServerImpl implements UIServer {
	public static final URI		SERVE = URI.create(UIServer.UI_SERVER_URI_SCHEME+":swing:/");

	private final URI			options;
	private final JFrame		frame;
	
	public SwingUIServerImpl() {
		this.options = null;
		this.frame = null;
	}
	
	private SwingUIServerImpl(final URI options) {
		this.options = options;
		this.frame = new JFrame();
	}
	
	@Override
	public void close() throws IOException {
		if (options != null) {
			frame.setVisible(false);
			frame.dispose();
		}
	}

	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		return URIUtils.canServeURI(resource,SERVE);
	}
	
	@Override
	public UIServer newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (!canServe(resource)) {
			throw new EnvironmentException("Resource URI ["+resource+"] is not supported by the class. Valid URI must be ["+SERVE+"...]");
		}
		else {
			return new SwingUIServerImpl(URI.create(resource.getRawSchemeSpecificPart()));
		}
	}

	@Override
	public void setNavigator(ContentMetadataInterface model, NavigatorStyle style, NavigationCallback callback) throws ContentException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void ask(Localizer localizer, ContentMetadataInterface model, T instance, FormManager<Object, T> formMgr, Completed<T> completed) throws ContentException, IOException, LocalizationException, NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		final AutoBuiltForm<T>	form = new AutoBuiltForm<T>(model,localizer,PureLibSettings.INTERNAL_LOADER,instance,formMgr);
		
	}

	@Override
	public <T, E extends Enum<?>> void wizard(Localizer localizer, ContentMetadataInterface model, T instance, FormManager<Object, T> formMgr, Completed<T> completed, WizardStep<T, E, Object>... steps) throws ContentException, FlowException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void browse(InputStreamGetter content, MimeType contentType, Completed<?> completed) throws ContentException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void edit(InputStreamGetter source, OutputStreamGetter target, MimeType contentType, Completed<?> completed) throws ContentException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <K, T> void showAndEdit(Localizer localizer, ContentMetadataInterface model, T instance, FormManager<K, T> formMgr, ShowStyle style, Completed<T> completed) throws ContentException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ProgressIndicator getProgressIndicator() throws ContentException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UIServer push() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
