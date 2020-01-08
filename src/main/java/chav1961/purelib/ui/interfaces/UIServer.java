package chav1961.purelib.ui.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

import javax.swing.text.html.parser.ContentModel;

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.i18n.interfaces.Localizer;

public interface UIServer extends Closeable {
	@FunctionalInterface
	public interface NavigationCallback {
		void process(ContentModel model, URI applicationPath) throws ContentException, FlowException;
	}
	
	public enum NavigatorStyle {
		MENU, TREE
	}
	
	public enum ShowStyle {
		LIST, LIST_AND_DETAIL, DETAIL
	}
	
	@FunctionalInterface
	public interface Completed<T> {
		void completed(ContentModel model,T instance);
	}
	
	void setNavigator(ContentModel model, NavigatorStyle style, NavigationCallback callback) throws ContentException, IOException;
	<T> void ask(Localizer localizer, ContentModel model, T instance, FormManager<Object,T> formMgr, Completed<T> completed) throws ContentException, IOException;
	<T,E extends Enum<?>> void wizard(Localizer localizer, ContentModel model, T instance, FormManager<Object,T> formMgr, Completed<T> completed, WizardStep<T,E,Object>... steps) throws ContentException, FlowException, IOException;
	void browse(InputStreamGetter content, MimeType contentType, Completed<?> completed) throws ContentException, IOException;
	void edit(InputStreamGetter source, OutputStreamGetter target, MimeType contentType, Completed<?> completed) throws ContentException, IOException;
	<K,T> void showAndEdit(Localizer localizer, ContentModel model, T instance, FormManager<K,T> formMgr, ShowStyle style, Completed<T> completed) throws ContentException, IOException;
	ProgressIndicator getProgressIndicator() throws ContentException, IOException;
	UIServer push() throws IOException;
}
