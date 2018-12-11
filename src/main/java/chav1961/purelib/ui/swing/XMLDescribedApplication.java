package chav1961.purelib.ui.swing;


import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.xsd.XSDConst;
import chav1961.purelib.fsys.FileSystemURLStreamHandler;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleDescriptor;

public class XMLDescribedApplication {
	private static final String		NAMESPACE_PREFIX = "app";
	private static final String		NAMESPACE_VALUE = "http://ui.purelib.chav1961/";
	
	private static final String		TAG_I18N = NAMESPACE_PREFIX+":i18n";
	private static final String		TAG_MENU = NAMESPACE_PREFIX+":menu";
	private static final String		TAG_SUBMENU = NAMESPACE_PREFIX+":submenu";
	private static final String		TAG_BUILTIN_SUBMENU = NAMESPACE_PREFIX+":builtinSubmenu";
	private static final String		TAG_MENUITEM = NAMESPACE_PREFIX+":item";
	private static final String		TAG_MENUSEPARATOR = NAMESPACE_PREFIX+":separator";
	
	private static final String		ATTR_LOCATION = "location";
	private static final String		ATTR_CAPTION = "caption";
	private static final String		ATTR_TOOLTIP = "tooltip";
	private static final String		ATTR_NAME = "name";
	private static final String		ATTR_ACTION = "action";
	private static final String		ATTR_ENABLED = "enabled";
	private static final String		ATTR_ID = "id";
	private static final String		ATTR_ICON = "icon";

	private static final String		BUILTIN_LANGUAGES = "builtin.languages";
	private static final String		BUILTIN_LOOK_AND_FEEL = "builtin.lookandfeel";
	
	private final Document		doc;
	private final Localizer		localizer;

	public XMLDescribedApplication(final InputStream xmlDescriptor, final LoggerFacade log) throws NullPointerException, EnvironmentException {
		this(xmlDescriptor,null,log);
	}
	
	public XMLDescribedApplication(final InputStream xmlDescriptor, final Localizer localizer, final LoggerFacade log) throws NullPointerException, EnvironmentException {
		if (xmlDescriptor == null) {
			throw new NullPointerException("XML descriptor stream can't be null");
		}
		else if (log == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else {
			try{final DocumentBuilderFactory	dbFactory = DocumentBuilderFactory.newInstance();
			
				dbFactory.setNamespaceAware(true);
				dbFactory.setValidating(true);
				dbFactory.setAttribute(XSDConst.SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
				dbFactory.setAttribute(XSDConst.SCHEMA_SOURCE, XSDConst.class.getResource("XMLDescribedApplication.xsd").toString());
				
			    final DocumentBuilder 			dBuilder = dbFactory.newDocumentBuilder();
			    
			    dBuilder.setErrorHandler(new ErrorHandler(){
					@Override
					public void error(final SAXParseException exc) throws SAXException {
						log.message(Severity.error, String.format("Line %1$d, col %2$d: %3$s",exc.getLineNumber(),exc.getColumnNumber(),exc.getLocalizedMessage()));
						throw exc;
					}
	
					@Override
					public void fatalError(final SAXParseException exc) throws SAXException {
						error(exc);
					}
	
					@Override
					public void warning(final SAXParseException exc) throws SAXException {
						log.message(Severity.warning, String.format("Line %1$d, col %2$d: %3$s",exc.getLineNumber(),exc.getColumnNumber(),exc.getLocalizedMessage()));
					}
				});
			    
				doc = dBuilder.parse(xmlDescriptor);
				doc.getDocumentElement().normalize();
				
				final XPathFactory 		xPathfactory = XPathFactory.newInstance();
				final XPath 			xpath = xPathfactory.newXPath();
				final NamespaceContext	nsc = new NamespaceContext() {
											@Override
											public Iterator<?> getPrefixes(final String namespaceURI) {
												return null;
											}
											
											@Override
											public String getPrefix(final String namespaceURI) {
												return null;
											}
											
											@Override
											public String getNamespaceURI(final String prefix) {
												if (prefix.equals(NAMESPACE_PREFIX)) {
													return NAMESPACE_VALUE;
												}
												else {
													return null;
												}
											}
										};
				xpath.setNamespaceContext(nsc);
				final XPathExpression	expr = xpath.compile("//"+TAG_I18N);
				final Node 				node = (Node) expr.evaluate(doc, XPathConstants.NODE);
				
				if (node != null) {
					this.localizer = LocalizerFactory.getLocalizer(URI.create(node.getAttributes().getNamedItem(ATTR_LOCATION).getNodeValue()));
				}
				else if (localizer != null) {
					this.localizer = localizer;
				}
				else {
					throw new NullPointerException("Localizer is missing anywhere");
				}
			} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
				throw new EnvironmentException(e.getLocalizedMessage(),e);
			}
		}
	}
	
	public Localizer getLocalizer() {
		return localizer;
	}
	
	public <T> T getEntity(final String id, final Class<T> awaited, final T defaultValue) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("Entity id can't be null or empty string"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null"); 
		}
		else {
			try{final XPathFactory 		xPathfactory = XPathFactory.newInstance();
				final XPath 			xpath = xPathfactory.newXPath();				
				final XPathExpression	expr = xpath.compile("//*[@"+ATTR_ID+"='"+id+"']");
				final NodeList 			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
				
				if (nl == null) {
					return defaultValue;
				}
				else {
					return convert(nl,awaited);
				}
			} catch (XPathExpressionException e) {
				throw new EnvironmentException(e.getLocalizedMessage(),e);
			}			
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T convert(final NodeList nl, final Class<T> awaited) throws EnvironmentException {		
		try{if (JMenuBar.class.isAssignableFrom(awaited) && nl.getLength() == 1 && nl.item(0).getNodeName().equals(TAG_MENU)) {
				return (T) convert2JMenuBar(nl.item(0));
			}
			else if (JPopupMenu.class.isAssignableFrom(awaited) && nl.getLength() == 1 && nl.item(0).getNodeName().equals(TAG_MENU)) {
				return (T) convert2JPopupMenu(nl.item(0));
			}
			else if (JToolBar.class.isAssignableFrom(awaited) && nl.getLength() == 1 && nl.item(0).getNodeName().equals(TAG_MENU)) {
				return (T) convert2JToolBar(nl.item(0));
			}
			else {
				throw new EnvironmentException("Unsupported conversion to ["+awaited+"]: XMLDescribedApplication has no rule to make conversion");
			}
		} catch (IllegalArgumentException | DOMException | IOException exc) {
			throw new EnvironmentException("Error converting to ["+awaited+"]: "+exc.getLocalizedMessage(),exc);
		}
	}

	private JMenuBar convert2JMenuBar(final Node root) throws LocalizationException, IllegalArgumentException, DOMException, IOException {
		final JMenuBar	result = new JLocalizedMenuBar(localizer);
		final NodeList	children = root.getChildNodes();
		
		for (int index = 0; index < children.getLength(); index++) {
			final JComponent	item = convert2Menu(children.item(index)); 
			
			if (item != null) {
				result.add(item);
			}
		}
		return result;
	}

	private JPopupMenu convert2JPopupMenu(final Node root) throws LocalizationException, IllegalArgumentException, DOMException, IOException {
		final JPopupMenu	result = new JPopupMenu();
		final NodeList		children = root.getChildNodes();
		
		for (int index = 0; index < children.getLength(); index++) {
			final JComponent	item = convert2Menu(children.item(index)); 
			
			if (item != null) {
				result.add(item);
			}
		}
		return result;
	}

	private JToolBar convert2JToolBar(final Node root) throws LocalizationException, IllegalArgumentException, DOMException, IOException {
		final JLocalizedToolBar	result = new JLocalizedToolBar(localizer);
		final NodeList			children = root.getChildNodes();
		
		for (int index = 0; index < children.getLength(); index++) {
			final Node		child = children.item(index);
			
			switch (child.getNodeName()) {
				case TAG_MENUITEM		:
					result.add(child);
					break;
				case TAG_MENUSEPARATOR	:
					result.addSeparator();
					break;
				default : 
			}
		}
		return result;
	}

	private JComponent convert2Menu(final Node root) throws LocalizationException, IllegalArgumentException, DOMException, IOException {
		switch (root.getNodeName()) {
			case TAG_SUBMENU			:
				final NodeList		children = root.getChildNodes();
				final JMenu			submenu = new JLocalizedMenu(localizer
											,root.getAttributes().getNamedItem(ATTR_CAPTION).getNodeValue()
											,root.getAttributes().getNamedItem(ATTR_TOOLTIP).getNodeValue());
				
				if (root.getAttributes().getNamedItem(ATTR_NAME) != null) {
					submenu.setName(root.getAttributes().getNamedItem(ATTR_NAME).getNodeValue());
				}
				if (root.getAttributes().getNamedItem(ATTR_ICON) != null) {
					submenu.setIcon(getIcon(URI.create(root.getAttributes().getNamedItem(ATTR_ICON).getNodeValue())));
				}
				for (int index = 0; index < children.getLength(); index++) {
					final JComponent	item = convert2Menu(children.item(index));
					
					if (item != null) {
						submenu.add(item);
					}
				}
				return submenu;
			case TAG_MENUITEM			:
				final JMenuItem		item = new JLocalizedMenuItem(localizer
											,root.getAttributes().getNamedItem(ATTR_CAPTION).getNodeValue()
											,root.getAttributes().getNamedItem(ATTR_TOOLTIP).getNodeValue());
				
				if (root.getAttributes().getNamedItem(ATTR_NAME) != null) {
					item.setName(root.getAttributes().getNamedItem(ATTR_NAME).getNodeValue());
				}
				if (root.getAttributes().getNamedItem(ATTR_ICON) != null) {
					item.setIcon(getIcon(URI.create(root.getAttributes().getNamedItem(ATTR_ICON).getNodeValue())));
				}
				item.setActionCommand(root.getAttributes().getNamedItem(ATTR_ACTION).getNodeValue());
				if (root.getAttributes().getNamedItem(ATTR_ENABLED) != null) {
					item.setEnabled(Boolean.valueOf(root.getAttributes().getNamedItem(ATTR_ENABLED).getNodeValue()));
				}
				return item;
			case TAG_MENUSEPARATOR		:
				return new JSeparator();
			case TAG_BUILTIN_SUBMENU	:
				final JMenu			stdMenu = new JLocalizedMenu(localizer
											,root.getAttributes().getNamedItem(ATTR_CAPTION).getNodeValue()
											,root.getAttributes().getNamedItem(ATTR_TOOLTIP).getNodeValue());
				
				switch (root.getAttributes().getNamedItem(ATTR_NAME).getNodeValue()) {
					case BUILTIN_LANGUAGES		:
						final ButtonGroup	group = new ButtonGroup();
						
						for (LocaleDescriptor desc : localizer.supportedLocales()) {
							final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem() {private static final long serialVersionUID = 1L; @Override public JToolTip createToolTip() {return new SmartToolTip(localizer,this);}};
							
							menuItem.setText(desc.getLanguage());
							menuItem.setToolTipText(desc.getDescription());
							menuItem.setIcon(desc.getIcon());
							menuItem.setSelected(desc.getLanguage().equals(localizer.currentLocale().getLanguage()));
							menuItem.setActionCommand(BUILTIN_LANGUAGES+":"+desc.getLanguage());
							group.add(menuItem);
							stdMenu.add(menuItem);
						}
						return stdMenu;
					case BUILTIN_LOOK_AND_FEEL	:
						return stdMenu;
					default : throw new UnsupportedOperationException("Unsupported standard menu type ["+root.getAttributes().getNamedItem(ATTR_NAME).getNodeValue()+"]");
				}
			default : 
				return null;
		}
	}

	private static Icon getIcon(final URI iconURI) throws IOException {
		if (FileSystemInterface.FILESYSTEM_URI_SCHEME.equals(iconURI.getScheme())) {
			final URL			url = new URL(null,iconURI.toString(),new FileSystemURLStreamHandler());
			final URLConnection	conn = url.openConnection();
			
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				try(final InputStream		is = conn.getInputStream()) {
					
					Utils.copyStream(is,baos);
				}
				
				return new ImageIcon(baos.toByteArray());
			}
		}
		else {
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				try(final InputStream		is = iconURI.toURL().openStream()) {
					
					Utils.copyStream(is,baos);
				}
				
				return new ImageIcon(baos.toByteArray());
			}
		}
	}
	
	static class JLocalizedMenu extends JMenu implements LocaleChangeListener {
		private static final long serialVersionUID = 1L;
		
		final Localizer	localizer;
		final String	textId;
		final String	tooltipId;
		
		JLocalizedMenu(final Localizer localizer,final String textId, final String tooltipId) throws LocalizationException {
			this.localizer = localizer;
			this.textId = textId;
			this.tooltipId = tooltipId;
			fillLocalizedString(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
		}
		
		@Override 
		public JToolTip createToolTip() {
			return new SmartToolTip(localizer,this);
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			fillLocalizedString(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
			
			for (int index = 0, maxIndex = getMenuComponentCount(); index < maxIndex; index++) {
				final Component	item = getMenuComponent(index);
				
				if (item instanceof LocaleChangeListener) {
					((LocaleChangeListener)item).localeChanged(oldLocale, newLocale);
				}
			}
		}

		private void fillLocalizedString(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			this.setText(localizer.getValue(textId));
			if (tooltipId != null && !tooltipId.isEmpty()) {
				this.setToolTipText(localizer.getValue(tooltipId));
			}
		}
	}

	static class JLocalizedMenuItem extends JMenuItem implements LocaleChangeListener {
		private static final long serialVersionUID = 1L;
		
		final Localizer	localizer;
		final String	textId;
		final String	tooltipId;
		
		JLocalizedMenuItem(final Localizer localizer,final String textId, final String tooltipId) throws LocalizationException {
			this.localizer = localizer;
			this.textId = textId;
			this.tooltipId = tooltipId;
			fillLocalizedString(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
		}
		
		@Override 
		public JToolTip createToolTip() {
			return new SmartToolTip(localizer,this);
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			fillLocalizedString(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
		}

		private void fillLocalizedString(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			this.setText(localizer.getValue(textId));
			if (tooltipId != null && !tooltipId.isEmpty()) {
				this.setToolTipText(localizer.getValue(tooltipId));
			}
		}
	}

	private class JLocalizedMenuBar extends JMenuBar implements LocaleChangeListener {
		private static final long serialVersionUID = 1L;
		
		private final Localizer	localizer;
		
		private JLocalizedMenuBar(final Localizer localizer) throws LocalizationException {
			this.localizer = localizer;
		}
		
		@Override 
		public JToolTip createToolTip() {
			return new SmartToolTip(localizer,this);
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			for (int index = 0, maxIndex = getComponentCount(); index < maxIndex; index++) {
				final Component	item = getComponent(index);
				
				if (item instanceof LocaleChangeListener) {
					((LocaleChangeListener)item).localeChanged(oldLocale, newLocale);
				}
			}
		}
	}

	private class JLocalizedToolBar extends LocalizedToolBar {
		private static final long serialVersionUID = 1L;
		
		private JLocalizedToolBar(final Localizer localizer) throws LocalizationException {
			super(localizer);
		}
		
		public void add(final Node node) throws LocalizationException, DOMException, MalformedURLException {
			switch (node.getNodeName()) {
				case TAG_MENUITEM		:
					final JButton		item;
					
					if (node.getAttributes().getNamedItem(ATTR_ICON) != null) {
						item = createButton(null,node.getAttributes().getNamedItem(ATTR_ACTION).getNodeValue(),new URL(node.getAttributes().getNamedItem(ATTR_ICON).getNodeValue()),new URL(node.getAttributes().getNamedItem(ATTR_ICON).getNodeValue()),node.getAttributes().getNamedItem(ATTR_TOOLTIP).getNodeValue());
					}
					else {
						item = new JButton();
						
						item.setText(localizer.getValue(node.getAttributes().getNamedItem(ATTR_CAPTION).getNodeValue()));
						item.setToolTipText(localizer.getValue(node.getAttributes().getNamedItem(ATTR_TOOLTIP).getNodeValue()));
						item.setActionCommand(node.getAttributes().getNamedItem(ATTR_ACTION).getNodeValue());
					}
					if (node.getAttributes().getNamedItem(ATTR_NAME) != null) {
						item.setName(node.getAttributes().getNamedItem(ATTR_NAME).getNodeValue());
					}
					
					if (node.getAttributes().getNamedItem(ATTR_ENABLED) != null) {
						item.setEnabled(Boolean.valueOf(node.getAttributes().getNamedItem(ATTR_ENABLED).getNodeValue()));
					}
					add(item,node.getAttributes().getNamedItem(ATTR_TOOLTIP).getNodeValue());
					break;
				default :
			}
		}
	}
}
