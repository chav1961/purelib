package chav1961.purelib.ui.swing;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.UITestInterface;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.LocalizerFactory.FillLocalizedContentCallback;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
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
			
			for (ContentNodeMetadata child : node) {
				toMenuEntity(child,submenu);
			}
			menu.add(submenu);
		} 
		else if (node.getRelativeUIPath().getPath().startsWith("./navigation.leaf.")) {
			final JMenuItemWithMeta	item = new JMenuItemWithMeta(node);
			
			menu.add(item);
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

	private static class JMenuWithMeta extends JMenu implements NodeMetadataOwner, LocaleChangeListener {
		private static final long serialVersionUID = 366031204608808220L;
		
		private final ContentNodeMetadata	metadata;
		
		private JMenuWithMeta(final ContentNodeMetadata metadata) {
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
			setToolTipText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getTooltipId()));
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
