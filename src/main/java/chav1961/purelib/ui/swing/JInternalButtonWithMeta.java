package chav1961.purelib.ui.swing;

import java.io.IOException;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

class JInternalButtonWithMeta extends JButton implements NodeMetadataOwner, LocaleChangeListener {
	private static final long serialVersionUID = 366031204608808220L;
	
	protected enum LAFType {
		TEXT_ONLY, ICON_INLY, BOTH, ICON_THEN_TEXT
	}
	
	private final ContentNodeMetadata	metadata;
	private final JInternalButtonWithMeta.LAFType				type;

	JInternalButtonWithMeta(final ContentNodeMetadata metadata) {
		this(metadata,LAFType.BOTH);
	}		
	
	JInternalButtonWithMeta(final ContentNodeMetadata metadata, final JInternalButtonWithMeta.LAFType type) {
		this.metadata = metadata;
		this.type = type;
		this.setName(metadata.getName());
		this.setActionCommand(metadata.getApplicationPath() != null ? metadata.getApplicationPath().getSchemeSpecificPart() : "action:/"+metadata.getName());
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
		switch (type) {
			case BOTH			:
				if (getNodeMetadata().getIcon() != null) {
					setIcon(new ImageIcon(getNodeMetadata().getIcon().toURL()));
				}
				setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
				break;
			case ICON_INLY		:
				if (getNodeMetadata().getIcon() != null) {
					setIcon(new ImageIcon(getNodeMetadata().getIcon().toURL()));
				}
				break;
			case ICON_THEN_TEXT	:
				if (getNodeMetadata().getIcon() != null) {
					setIcon(new ImageIcon(getNodeMetadata().getIcon().toURL()));
					break;
				}
				// break doesn't need!
			case TEXT_ONLY		:
				setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
				break;
			default:
				throw new UnsupportedOperationException("LAF type ["+type+"] is not supported yet"); 
		}
	}
}