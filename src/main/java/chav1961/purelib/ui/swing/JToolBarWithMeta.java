package chav1961.purelib.ui.swing;

import java.awt.Container;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JToolBar;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.UIItemState;
import chav1961.purelib.ui.interfaces.UIItemState.AvailableAndVisible;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;


/**
 * <p>This class is a model-driven toolbar. The base model for toolbar is a menu model.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @last.update 0.0.6
 */
public class JToolBarWithMeta extends JToolBar implements NodeMetadataOwner, LocaleChangeListener, BooleanPropChangeListenerSource {
	private static final long 	serialVersionUID = 366031204608808220L;
	private static final String	NAVIGATION_NODE = "./"+Constants.MODEL_NAVIGATION_NODE_PREFIX; 
	private static final String	NAVIGATION_LEAF = "./"+Constants.MODEL_NAVIGATION_LEAF_PREFIX; 
	private static final URI	SEPARATOR = URI.create("./navigation.separator"); 
	
	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final UIItemState			state;

	/**
	 * <p>Constructor of the class</p>
	 * @param metadata metadata from model. Can't be null
	 * @throws LocalizationException on localization errors
	 * @throws ContentException on any content errors
	 * @throws NullPointerException when any parameter is null
	 */
	public JToolBarWithMeta(final ContentNodeMetadata metadata) throws LocalizationException, ContentException, NullPointerException {
		this(metadata, (node) -> AvailableAndVisible.DEFAULT);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param metadata metadata from model. Can't be null
	 * @param state lambda-styled toolbar items enabled/visibility callback. Can' be null
	 * @throws LocalizationException on localization errors
	 * @throws ContentException on any content errors
	 * @throws NullPointerException when any parameter is null
	 */
	public JToolBarWithMeta(final ContentNodeMetadata metadata, final UIItemState state) throws LocalizationException, ContentException, NullPointerException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (state == null) {
			throw new NullPointerException("Item state callback can't be null");
		}
		else {
			this.metadata = metadata;
			this.state = state;
			this.setName(metadata.getName());
			
			final Map<String, ButtonGroup>	fragments = new HashMap<>();
			
			for (ContentNodeMetadata child : metadata) {
				if (child.getRelativeUIPath().toString().startsWith(NAVIGATION_LEAF)) {
					if (child.getApplicationPath() != null && child.getApplicationPath().getFragment() != null) {
						fragments.put(child.getApplicationPath().getFragment(), new ButtonGroup());
					}
				}
			}			
			
			
			for (ContentNodeMetadata child : metadata) {
				if (child.getRelativeUIPath().toString().startsWith(NAVIGATION_NODE)) {
					final JMenuPopupWithMeta	menu = new JMenuPopupWithMeta(child, state);
					final JButton 				btn = new JButtonWithMetaAndActions(child,InternalButtonLAFType.ICON_THEN_TEXT,menu);					
					
					for (ContentNodeMetadata item : child) {
						SwingUtils.toMenuEntity(item,menu);
					}
					
					SwingUtils.buildRadioButtonGroups(menu);
					btn.addActionListener((e)->{
						menu.show(btn,btn.getWidth()/2,btn.getHeight()/2);
					});
					add(btn);
				}
				else if (child.getRelativeUIPath().toString().startsWith(NAVIGATION_LEAF)) {
					if (child.getApplicationPath() != null && URIUtils.parseQuery(child.getApplicationPath()).containsKey("checkable")) {
						add(new JInternalToggleButtonWithMeta(child,InternalButtonLAFType.ICON_THEN_TEXT));
					}
					else if (child.getApplicationPath() != null && child.getApplicationPath().getFragment() != null) {
						final JInternalToggleButtonWithMeta	btn = new JInternalToggleButtonWithMeta(child,InternalButtonLAFType.ICON_THEN_TEXT); 
						
						fragments.get(child.getApplicationPath().getFragment()).add(btn);
						add(btn);
					}
					else {	// Button groups
						add(new JInternalButtonWithMeta(child,InternalButtonLAFType.ICON_THEN_TEXT));
					}
				}
				else if (SEPARATOR.equals(child.getRelativeUIPath())) {
					addSeparator();
				}
			}
			fillLocalizedStrings();
		}
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		for (int index = 0; index < getComponentCount(); index++) {
			SwingUtils.refreshLocale(getComponent(index), oldLocale, newLocale);
		}
		fillLocalizedStrings();
	}

	/**
	 * <p>Notify button state changed</p>
	 */
	public void fireContentChanged() {
		fireContentChanged(getNodeMetadata());
	}

	private void fireContentChanged(final ContentNodeMetadata node) {
		final Container	c = SwingUtils.findComponentByName(this, node.getUIPath().toString());
		
		switch (state.getItemState(node)) {
			case DEFAULT		:
				break;
			case AVAILABLE		:
				c.setVisible(true);
				c.setEnabled(true);
				break;
			case HIDDEN	: case NOTVISIBLE :
				c.setVisible(false);
				c.setEnabled(false);
				break;
			case NOTAVAILABLE : case READONLY :
				c.setVisible(true);
				c.setEnabled(false);
				break;
			default :
				throw new UnsupportedOperationException("Item state ["+state.getItemState(node)+"] is not supported yet");
		}
		for (ContentNodeMetadata item : node) {
			fireContentChanged(item);
		}
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

	private void fillLocalizedStrings() throws LocalizationException {
		if (getNodeMetadata().getTooltipId() != null) {
			setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
		}
	}
}