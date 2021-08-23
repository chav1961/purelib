package chav1961.purelib.ui.swing;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JToolBar;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.UIItemState.AvailableAndVisible;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.UIItemState;


public class JToolBarWithMeta extends JToolBar implements NodeMetadataOwner, LocaleChangeListener {
	private static final long serialVersionUID = 366031204608808220L;
	
	private final ContentNodeMetadata	metadata;

	public JToolBarWithMeta(final ContentNodeMetadata metadata) throws LocalizationException, ContentException {
		this(metadata, (node) -> AvailableAndVisible.DEFAULT);
	}
	
	public JToolBarWithMeta(final ContentNodeMetadata metadata, final UIItemState state) throws LocalizationException, ContentException {
		UIItemState s;
		this.metadata = metadata;
		this.setName(metadata.getName());
		for (ContentNodeMetadata child : metadata) {
			if (child.getRelativeUIPath().toString().startsWith("./"+Constants.MODEL_NAVIGATION_NODE_PREFIX)) {
				final JMenuPopupWithMeta	menu = new JMenuPopupWithMeta(child, state);
				final JButton 				btn = new JButtonWithMetaAndActions(child,JInternalButtonWithMeta.LAFType.ICON_THEN_TEXT,menu);					
				
				for (ContentNodeMetadata item : child) {
					SwingUtils.toMenuEntity(item,menu);
				}
				
				btn.addActionListener((e)->{
					menu.show(btn,btn.getWidth()/2,btn.getHeight()/2);
				});
				add(btn);
			}
			else if (child.getRelativeUIPath().toString().startsWith("./"+Constants.MODEL_NAVIGATION_LEAF_PREFIX)) {
				add(new JInternalButtonWithMeta(child,JInternalButtonWithMeta.LAFType.ICON_THEN_TEXT));
			}
			else if (URI.create("./navigation.separator").equals(child.getRelativeUIPath())) {
				addSeparator();
			}
		}
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