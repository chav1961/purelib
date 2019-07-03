package chav1961.purelib.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.UITestInterface;
import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.AbstractLocalizer;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.LocalizerFactory.FillLocalizedContentCallback;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class SwingModelUtils {
	public static <T extends JComponent> T toMenuEntity(final ContentNodeMetadata node, final Class<T> awaited) throws NullPointerException, IllegalArgumentException{
		if (node == null) {
			throw new NullPointerException("Model node can't be null"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null"); 
		}
		else if (!node.getRelativeUIPath().getPath().startsWith("./navigation.top")) {
			throw new IllegalArgumentException("Model node ["+node.getUIPath()+"] can't be converted to ["+awaited.getCanonicalName()+"] class"); 
		}
		else if (awaited.isAssignableFrom(JMenuBar.class)) {
			final JMenuBar	result = new JMenuBarWithMeta(node);
			
			for (ContentNodeMetadata child : node) {
				toMenuEntity(child,result);
			}
			return (T) result;
		}
		else if (awaited.isAssignableFrom(JPopupMenu.class)) {
			final JPopupMenu	result = new JMenuPopupWithMeta(node);
			
			for (ContentNodeMetadata child : node) {
				toMenuEntity(child,result);
			}
			return (T) result;
		}
		else {
			throw new IllegalArgumentException("Illegal awaited class ["+awaited.getCanonicalName()+"], only JMenuBar and JPopupMenu are available"); 
		}
	}

	public static <T extends JComponent> T toToolbar(final ContentNodeMetadata node, final Class<T> awaited) throws NullPointerException, IllegalArgumentException{
		if (node == null) {
			throw new NullPointerException("Model node can't be null"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null"); 
		}
		else if (!node.getRelativeUIPath().getPath().startsWith("./navigation.top")) {
			throw new IllegalArgumentException("Model node ["+node.getUIPath()+"] can't be converted to ["+awaited.getCanonicalName()+"] class"); 
		}
		else if (awaited.isAssignableFrom(JToolBar.class)) {
			final JToolBar	result = new JToolBarWithMeta(node);
			
			for (ContentNodeMetadata child : node) {
				if (child.getRelativeUIPath().toString().startsWith("./navigation.leaf.")) {
					result.add(new JButtonWithMeta(child));
				}
				else if (URI.create("./navigation.separator").equals(child.getRelativeUIPath())) {
					result.addSeparator();
				}
			}
			return (T) result;
		}
		else {
			throw new IllegalArgumentException("Illegal awaited class ["+awaited.getCanonicalName()+"], only JMenuBar and JPopupMenu are available"); 
		}
	}
	
	public static UITestInterface buildTestInterface(final ContentNodeMetadata metadata, final Component uiRoot) {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (uiRoot == null) {
			throw new NullPointerException("UI root can't be null");
		}
		else {
			return new SwingTestManager((Class<Component>)uiRoot.getClass(), uiRoot, metadata);
		}
	}

	public static boolean putToScreen(final ContentNodeMetadata metadata, final Object content, final Container uiRoot) {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (content == null) {
			throw new NullPointerException("Content instance object can't be null"); 
		}
		else if (uiRoot == null) {
			throw new NullPointerException("UI root component can't be null"); 
		}
		else if (!content.getClass().isAnnotationPresent(LocaleResource.class)) {
			throw new IllegalArgumentException("Content instance class ["+content.getClass().getCanonicalName()+"] doesn't annotated with @LocaleResource, and can't be used here"); 
		}
		else if (metadata.getType() != content.getClass()) {
			throw new IllegalArgumentException("Content instance class ["+content.getClass().getCanonicalName()+"] differ to metadata instance class ["+metadata.getType().getCanonicalName()+"]. Use appropriative arguments!"); 
		}
		else {
			metadata.getOwner().walkDown((mode, applicationPath, uiPath, node)->{
				if (mode == NodeEnterMode.ENTER) {
					final Component	component = SwingUtils.findComponentByName(uiRoot,node.getUIPath().toString());
					
					if (component instanceof JComponentInterface) {
						try{((JComponentInterface)component).assignValueToComponent(
									ModelUtils.getValueByGetter(content,
											GettersAndSettersFactory.buildGetterAndSetter(node.getApplicationPath())
											,metadata));
						} catch (ContentException e) {
						}
					}
				}
				return ContinueMode.CONTINUE;
			},metadata.getUIPath());
			return true;
		}
	}
	
	public static boolean getFromScreen(final ContentNodeMetadata metadata, final Component uiRoot, final Object content) {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (content == null) {
			throw new NullPointerException("Content instance object can't be null"); 
		}
		else if (uiRoot == null) {
			throw new NullPointerException("UI root component can't be null"); 
		}
		else if (!content.getClass().isAnnotationPresent(LocaleResource.class)) {
			throw new IllegalArgumentException("Content instance class ["+content.getClass().getCanonicalName()+"] doesn't annotated with @LocaleResource, and can't be used here"); 
		}
		else if (metadata.getType() != content.getClass()) {
			throw new IllegalArgumentException("Content instance class ["+content.getClass().getCanonicalName()+"] differ to metadata instance class ["+metadata.getType().getCanonicalName()+"]. Use appropriative arguments!"); 
		}
		else {
			metadata.getOwner().walkDown((mode, applicationPath, uiPath, node)->{
				if (mode == NodeEnterMode.ENTER) {
					final Component	component = SwingUtils.findComponentByName(uiRoot,node.getUIPath().toString());
					
					if (component instanceof JComponentInterface) {
						try{ModelUtils.setValueBySetter(content,
									((JComponentInterface)component).getChangedValueFromComponent(),
									GettersAndSettersFactory.buildGetterAndSetter(node.getApplicationPath())
									,metadata);
							
						} catch (ContentException e) {
						}
					}
				}
				return ContinueMode.CONTINUE;
			},metadata.getUIPath());
			return true;
		}
	}
	
	private static void toMenuEntity(final ContentNodeMetadata node, final JMenuBar bar) throws NullPointerException, IllegalArgumentException{
		if (node.getRelativeUIPath().getPath().startsWith("./navigation.node.")) {
			final JMenu	menu = new JMenuWithMeta(node);
			
			for (ContentNodeMetadata child : node) {
				toMenuEntity(child,menu);
			}
			bar.add(menu);
		} 
		else if (node.getRelativeUIPath().getPath().startsWith("./navigation.leaf.")) {
			bar.add(new JMenuItemWithMeta(node));
		}
	}

	private static void toMenuEntity(final ContentNodeMetadata node, final JPopupMenu popup) throws NullPointerException, IllegalArgumentException{
		if (node.getRelativeUIPath().getPath().startsWith("./navigation.node.")) {
			final JMenu	menu = new JMenuWithMeta(node);
			
			for (ContentNodeMetadata child : node) {
				toMenuEntity(child,menu);
			}
			popup.add(menu);
		} 
		else if (node.getRelativeUIPath().getPath().startsWith("./navigation.leaf.")) {
			popup.add(new JMenuItemWithMeta(node));
		}
	}
	
	private static void toMenuEntity(final ContentNodeMetadata node, final JMenu menu) throws NullPointerException, IllegalArgumentException{
		if (node.getRelativeUIPath().getPath().startsWith("./navigation.node.")) {
			final JMenu	submenu = new JMenuWithMeta(node);
			
			if (node.getApplicationPath() != null && node.getApplicationPath().toString().contains(ContentModelFactory.APPLICATION_SCHEME_BUILTIN_ACTION)) {
				switch (node.getName()) {
					case ContentModelFactory.BUILTIN_LANGUAGE	:
						AbstractLocalizer.enumerateLocales((lang,langName,icon)->{
							final JMenuItemWithMeta	item = new JMenuItemWithMeta(node);
							
							item.setText(langName);
							item.setIcon(icon);
							item.setActionCommand(node.getApplicationPath()+":"+lang.name());
							submenu.add(item);
						});
						break;
					case ContentModelFactory.BUILTIN_STYLE		:
						for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
							final JMenuItemWithMeta	item = new JMenuItemWithMeta(node);
							
							item.setText(laf.getName());
							item.setActionCommand(node.getApplicationPath()+":"+laf.getClassName());
							submenu.add(item);
						}
						break;
					default : throw new UnsupportedOperationException("Built-in name ["+node.getName()+"] is not suported yet");
				}
			}
			else {
				for (ContentNodeMetadata child : node) {
					toMenuEntity(child,submenu);
				}
				final Set<String>	availableGroups = new HashSet<>(); 
						
				for (int index = 0, maxIndex = submenu.getMenuComponentCount(); index < maxIndex; index++) {
					if (submenu.getMenuComponent(index) instanceof JRadioMenuItemWithMeta) {
						availableGroups.add(((JRadioMenuItemWithMeta)submenu.getMenuComponent(index)).getRadioGroup());
					}
				}
				if (availableGroups.size() > 0) {
					for (String group : availableGroups) {
						final ButtonGroup 	buttonGroup = new ButtonGroup();
						boolean				selected = false;

						for (int index = 0, maxIndex = submenu.getMenuComponentCount(); index < maxIndex; index++) {
							Component	c = submenu.getMenuComponent(index); 
							
							if ((c instanceof JRadioMenuItemWithMeta) && group.equals(((JRadioMenuItemWithMeta)c).getRadioGroup())) {
								buttonGroup.add((JRadioMenuItemWithMeta)c);
								if (!selected) {
									((JRadioMenuItemWithMeta)c).setSelected(selected = true);
								}
							}
						}
					}
				}				
				menu.add(submenu);
			}
		} 
		else if (node.getRelativeUIPath().getPath().startsWith("./navigation.leaf.")) {
			if (node.getApplicationPath().getFragment() != null) {
				final JRadioMenuItemWithMeta	item = new JRadioMenuItemWithMeta(node);
				
				menu.add(item);
			}
			else {
				final JMenuItemWithMeta			item = new JMenuItemWithMeta(node);
				
				menu.add(item);
			}
			
		}
		else if (node.getRelativeUIPath().getPath().startsWith("./navigation.separator")) {
			menu.add(new JSeparator());
		}
	}

	private static class JMenuBarWithMeta extends JMenuBar implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 2873312186080690483L;
		
		private final ContentNodeMetadata	metadata;
		
		private JMenuBarWithMeta(final ContentNodeMetadata metadata) {
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
//			setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
		}
	}

	private static class JMenuPopupWithMeta extends JPopupMenu implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 2873312186080690483L;
		
		private final ContentNodeMetadata	metadata;
		
		private JMenuPopupWithMeta(final ContentNodeMetadata metadata) {
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
//			setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
		}
	}
	
	private static class JMenuItemWithMeta extends JMenuItem implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = -1731094524456032387L;

		private final ContentNodeMetadata	metadata;
		
		private JMenuItemWithMeta(final ContentNodeMetadata metadata) {
			this.metadata = metadata;
			this.setName(metadata.getName());
			this.setActionCommand(metadata.getApplicationPath().getSchemeSpecificPart());
			for (ContentNodeMetadata item : metadata.getOwner().byApplicationPath(metadata.getApplicationPath())) {
				if (item.getRelativeUIPath().toString().startsWith("./keyset.key")) {
					this.setAccelerator(KeyStroke.getKeyStroke(item.getLabelId()));
					break;
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
			setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
			setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
		}
	}

	private static class JRadioMenuItemWithMeta extends JRadioButtonMenuItem implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = -1731094524456032387L;

		private final ContentNodeMetadata	metadata;
		private final String				radioGroup;
		
		private JRadioMenuItemWithMeta(final ContentNodeMetadata metadata) {
			this.metadata = metadata;
			this.radioGroup = metadata.getApplicationPath().getFragment();
			this.setName(metadata.getName());
			this.setActionCommand(metadata.getApplicationPath().getSchemeSpecificPart());
			for (ContentNodeMetadata item : metadata.getOwner().byApplicationPath(metadata.getApplicationPath())) {
				if (item.getRelativeUIPath().toString().startsWith("./keyset.key")) {
					this.setAccelerator(KeyStroke.getKeyStroke(item.getLabelId()));
					break;
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

		public String getRadioGroup() {
			return radioGroup;
		}
		
		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			try{fillLocalizedStrings();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void fillLocalizedStrings() throws LocalizationException, IOException {
			setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
			setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
		}
	}
	
	
	private static class JMenuWithMeta extends JMenu implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 366031204608808220L;
		
		private final ContentNodeMetadata	metadata;
		
		private JMenuWithMeta(final ContentNodeMetadata metadata) {
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
			setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
			setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
		}
	}

	private static class JToolBarWithMeta extends JToolBar implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 366031204608808220L;
		
		private final ContentNodeMetadata	metadata;
		
		private JToolBarWithMeta(final ContentNodeMetadata metadata) {
			this.metadata = metadata;
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

	private static class JButtonWithMeta extends JButton implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 366031204608808220L;
		
		private final ContentNodeMetadata	metadata;
		
		private JButtonWithMeta(final ContentNodeMetadata metadata) {
			this.metadata = metadata;
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
			setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
			setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
		}
	}
}
