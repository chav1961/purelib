package chav1961.purelib.i18n;

import java.net.URI;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;

public class LocalizerStore implements AutoCloseable {
	private final Localizer	localizer;
	private final Localizer	currentLocalizer;
	private final boolean	localizerPushed;
	
	public LocalizerStore(final Localizer parent, final URI child) throws LocalizationException {
		if (parent == null) {
			throw new NullPointerException("Parent can't be null");
		}
		else if (child != null) {
			if (!parent.containsLocalizerHere(child.toString())) {
				this.currentLocalizer = LocalizerFactory.getLocalizer(child);
				this.localizer = parent.push(this.currentLocalizer);
				this.localizerPushed = true;
			}
			else {
				this.localizer = parent;
				this.currentLocalizer = parent;
				this.localizerPushed = false;
			}
		}
		else {
			this.localizer = parent;
			this.currentLocalizer = null;
			this.localizerPushed = false;
		}
	}

	@Override
	public void close() throws RuntimeException {
		if (localizerPushed) {
			try{localizer.pop();
			} catch (LocalizationException e) {
			}
		}
	}
	
	public Localizer getLocalizer() {
		return currentLocalizer == null ? localizer : currentLocalizer;
	}
}
