package chav1961.purelib.ui.interfaces;

import java.awt.Component;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public interface UIServer extends Closeable, SpiService<UIServer> {
	/**
	 * <p>URI scheme for all the UI implementations</p>
	 */
	public static final String		UI_SERVER_URI_SCHEME = "uis";	
	
	
	@FunctionalInterface
	public interface NavigationCallback {
		void process(ContentMetadataInterface model, URI applicationPath) throws ContentException, FlowException;
	}
	
	public enum NavigatorStyle {
		MENU, TREE
	}
	
	public enum ShowStyle {
		LIST, LIST_AND_DETAIL, DETAIL
	}
	
	@FunctionalInterface
	public interface Completed<T> {
		void completed(ContentMetadataInterface model,T instance);
	}
	
	void setNavigator(ContentMetadataInterface model, NavigatorStyle style, NavigationCallback callback) throws ContentException, IOException;
	<K,T> void ask(Localizer localizer, ContentMetadataInterface model, T instance, FormManager<K,T> formMgr, Completed<T> completed) throws ContentException, LocalizationException, IOException;
	<T,E extends Enum<?>> void wizard(Localizer localizer, ContentMetadataInterface model, T instance, FormManager<Object,T> formMgr, Completed<T> completed, @SuppressWarnings("unchecked") WizardStep<T,E,Component>... steps) throws ContentException, FlowException, IOException;
	void browse(InputStreamGetter content, MimeType contentType, Completed<?> completed) throws ContentException, IOException;
	void edit(InputStreamGetter source, OutputStreamGetter target, MimeType contentType, Completed<?> completed) throws ContentException, IOException;
	<K,T> void showAndEdit(Localizer localizer, ContentMetadataInterface model, T instance, FormManager<K,T> formMgr, ShowStyle style, Completed<T> completed) throws ContentException, IOException;
	ProgressIndicator getProgressIndicator() throws ContentException, IOException;
	UIServer push() throws IOException;
}
