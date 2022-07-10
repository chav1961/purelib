package chav1961.purelib.ui.swing;

import java.util.Locale;

import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.SwingUtils.InnerActionNode;

class JButtonWithMetaAndActions extends JInternalButtonWithMeta implements InnerActionNode {
	private static final long serialVersionUID = 366031204608808220L;
	
	private final JComponent[]	actionable;

	private JButtonWithMetaAndActions(final ContentNodeMetadata metadata, final JComponent... actionable) {
		super(metadata);
		this.actionable = actionable;
	}
	
	JButtonWithMetaAndActions(final ContentNodeMetadata metadata, final InternalButtonLAFType type, final JComponent... actionable) {
		super(metadata,type);
		this.actionable = actionable;
	}

	@Override
	public JComponent[] getActionNodes() {
		return actionable;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		super.localeChanged(oldLocale, newLocale);
		for (JComponent item : getActionNodes()) {
			SwingUtils.refreshLocale(item,oldLocale, newLocale);
		}
	}
}