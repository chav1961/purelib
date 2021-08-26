package chav1961.purelib.ui.swing;


import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.UIItemState;

class JMenuPopupWithMeta extends JPopupMenu implements NodeMetadataOwner, LocaleChangeListener {
	private static final long serialVersionUID = 2873312186080690483L;
	
	private final ContentNodeMetadata	metadata;
	private final UIItemState 			state;
	
	JMenuPopupWithMeta(final ContentNodeMetadata metadata, final UIItemState state) {
		this.metadata = metadata;
		this.state = state;
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
			for (int index = 0, maxIndex = this.getComponentCount(); index < maxIndex; index++) {
				final Component	item = this.getComponent(index);
				
				if (item instanceof LocaleChangeListener) {
					((LocaleChangeListener)item).localeChanged(oldLocale, newLocale);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void show(final Component invoker, final int x, final int y) {
		final Window				frame = SwingUtilities.getWindowAncestor(invoker);
		
		getNodeMetadata().getOwner().walkDown((mode, applicationPath, uiPath, node) -> {
			if (mode == NodeEnterMode.ENTER && applicationPath != null && URIUtils.canServeURI(applicationPath, SwingUtils.MODEL_REF_URI)) {
				final Container		item = SwingUtils.findComponentByName(this,node.getName());
				final Container		ref = SwingUtils.findComponentByName(frame,node.getLabelId());
				
				if ((item instanceof JMenuItem) && (ref instanceof JMenuItem)) {
					((JMenuItem)item).setText(((JMenuItem)ref).getText());
					((JMenuItem)item).setIcon(((JMenuItem)ref).getIcon());
					((JMenuItem)item).setToolTipText(((JMenuItem)ref).getToolTipText());
					((JMenuItem)item).setEnabled(((JMenuItem)ref).isEnabled());
					((JMenuItem)item).addActionListener((e)->((JMenuItem)ref).doClick());
				}
				else if ((item instanceof JMenu) && (ref instanceof JMenu)) {
					((JMenu)item).setText(((JMenu)ref).getText());
					((JMenu)item).setIcon(((JMenu)ref).getIcon());
					((JMenu)item).setToolTipText(((JMenu)ref).getToolTipText());
					((JMenu)item).setEnabled(((JMenu)ref).isEnabled());
				}
			}
			return ContinueMode.CONTINUE;
		},getNodeMetadata().getUIPath());
		prepareState();
		super.show(invoker, x, y);
	}

	private void prepareState() {
		SwingUtils.walkDown(this, (mode, node)-> {
			if (mode == NodeEnterMode.ENTER) {
				if (node instanceof NodeMetadataOwner) {
					switch (state.getItemState(((NodeMetadataOwner)node).getNodeMetadata())) {
						case DEFAULT : 
							return ContinueMode.CONTINUE;
						case AVAILABLE : case READONLY : 
							node.setVisible(true);
							node.setEnabled(true);
							return ContinueMode.CONTINUE;
						case NOTAVAILABLE	:
							node.setVisible(true);
							node.setEnabled(false);
							return ContinueMode.SKIP_CHILDREN;
						case NOTVISIBLE		:
							node.setVisible(false);
							node.setEnabled(false);
							return ContinueMode.SKIP_CHILDREN;
						default : throw new UnsupportedOperationException("Available type ["+state.getItemState(((NodeMetadataOwner)node).getNodeMetadata())+"] is not supported yet");
					}
				}
			}
			return ContinueMode.CONTINUE;
		});
	}
	
	private void fillLocalizedStrings() throws LocalizationException, IOException {
		final String	ttId = getNodeMetadata().getTooltipId();
		
		if (ttId != null && !ttId.isEmpty()) {
			setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(ttId));
		}
	}
}