package chav1961.purelib.ui.swing;

import java.io.IOException;
import java.util.Locale;

import javax.swing.JToolBar;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class JToolBarWithMeta extends JToolBar implements NodeMetadataOwner, LocaleChangeListener {
	private static final long serialVersionUID = 366031204608808220L;
	
	private final ContentNodeMetadata	metadata;
	
	public JToolBarWithMeta(final ContentNodeMetadata metadata) {
		this.metadata = metadata;
		this.setName(metadata.getName());
		try{fillLocalizedStrings();
		} catch (IOException | LocalizationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		try{fillLocalizedStrings();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fillLocalizedStrings() throws LocalizationException, IOException {
		if (getNodeMetadata().getTooltipId() != null) {
			setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
		}
	}
}