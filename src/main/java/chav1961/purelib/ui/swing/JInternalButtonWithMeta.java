package chav1961.purelib.ui.swing;

import java.io.IOException;
import java.net.URI;
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
		fillLocalizedStrings();
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	private void fillLocalizedStrings() throws LocalizationException {
		if (getNodeMetadata().getTooltipId() != null) {
			String	keyPrefix = "";

			if (metadata.getOwner() != null && metadata.getApplicationPath() != null) {
				for (ContentNodeMetadata item : metadata.getOwner().byApplicationPath(metadata.getApplicationPath())) {
					if (item.getRelativeUIPath().toString().startsWith("./keyset.key")) {
						keyPrefix = item.getLabelId()+": ";
						break;
					}
				}
			}
			setToolTipText(keyPrefix+LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
		}
		switch (type) {
			case BOTH			:
				setIcon(loadImageIcon(getNodeMetadata().getIcon()));
				setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
				break;
			case ICON_INLY		:
				setIcon(loadImageIcon(getNodeMetadata().getIcon()));
				break;
			case ICON_THEN_TEXT	:
				if (getNodeMetadata().getIcon() != null) {
					setIcon(loadImageIcon(getNodeMetadata().getIcon()));
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
	
	private static ImageIcon loadImageIcon(final URI iconLocation) {
		if (iconLocation == null) {
			return null;
		}
		else {
			try {
				return new ImageIcon(iconLocation.toURL());
			} catch (IOException exc) {
				return null;
			}
		}
	}
}