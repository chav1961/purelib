package chav1961.purelib.ui;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfacers.FormManager;

class UIServer {
	public enum UIServerType {
		Swing, Swing2Tier, WebService
	}
	
	public UIServer(final UIServerType type, final SubstitutableProperties options) {
		
	}
	
	public <T> boolean ask(final Localizer localizer, final T instance, final FormManager<Object,T> formMgr) {
		return false;
	}
}
