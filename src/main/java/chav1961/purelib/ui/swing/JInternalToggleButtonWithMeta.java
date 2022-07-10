package chav1961.purelib.ui.swing;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;

class JInternalToggleButtonWithMeta extends JToggleButton implements NodeMetadataOwner, LocaleChangeListener, BooleanPropChangeListenerSource {
	private static final long serialVersionUID = 366031204608808220L;
	
	private final BooleanPropChangeListenerRepo		repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata				metadata;
	private final InternalButtonLAFType				type;

	JInternalToggleButtonWithMeta(final ContentNodeMetadata metadata) {
		this(metadata,InternalButtonLAFType.BOTH);
	}		
	
	JInternalToggleButtonWithMeta(final ContentNodeMetadata metadata, final InternalButtonLAFType type) {
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

	@Override
	public void addBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		repo.addBooleanPropChangeListener(listener);
	}

	@Override
	public void removeBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		repo.removeBooleanPropChangeListener(listener);
	}
	
	@Override
	public void setVisible(final boolean aFlag) {
		final boolean old = isVisible();
		
		super.setVisible(aFlag);
		if (repo != null && aFlag != old) {
			repo.fireBooleanPropChange(this, EventChangeType.VISIBILE, aFlag);
		}
	}
	
	@Override
	public boolean isEnabled() {
		if (getParent() != null) {
			return super.isEnabled() && getParent().isEnabled();
		}
		else {
			return super.isEnabled();
		}
	}
	
	@Override
	public void setEnabled(boolean b) {
		final boolean old = isEnabled();
		
		super.setEnabled(b);
		if (repo != null && b != old) {
			repo.fireBooleanPropChange(this, EventChangeType.ENABLED, b);
		}
	}
	
	@Override
	public void setSelected(boolean b) {
		final boolean old = isSelected();
		
		super.setSelected(b);
		if (repo != null && b != old) {
			repo.fireBooleanPropChange(this, EventChangeType.SELECTED, b);
		}
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
				setIcon(InternalUtils.loadImageIcon(getNodeMetadata().getIcon(), InternalUtils.IconType.ORDINAL));
				setPressedIcon(InternalUtils.loadImageIcon(getNodeMetadata().getIcon(), InternalUtils.IconType.PRESSED));
				setSelectedIcon(InternalUtils.loadImageIcon(getNodeMetadata().getIcon(), InternalUtils.IconType.TOGGLED));
				setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
				break;
			case ICON_INLY		:
				setIcon(InternalUtils.loadImageIcon(getNodeMetadata().getIcon(), InternalUtils.IconType.ORDINAL));
				setPressedIcon(InternalUtils.loadImageIcon(getNodeMetadata().getIcon(), InternalUtils.IconType.PRESSED));
				setSelectedIcon(InternalUtils.loadImageIcon(getNodeMetadata().getIcon(), InternalUtils.IconType.TOGGLED));
				InternalUtils.cropButtonByIcon(this);
				break;
			case ICON_THEN_TEXT	:
				if (getNodeMetadata().getIcon() != null) {
					setIcon(InternalUtils.loadImageIcon(getNodeMetadata().getIcon(), InternalUtils.IconType.ORDINAL));
					setPressedIcon(InternalUtils.loadImageIcon(getNodeMetadata().getIcon(), InternalUtils.IconType.PRESSED));
					setSelectedIcon(InternalUtils.loadImageIcon(getNodeMetadata().getIcon(), InternalUtils.IconType.TOGGLED));
					InternalUtils.cropButtonByIcon(this);
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