package chav1961.purelib.model;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.xsd.XSDConst;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfacers.Action;
import chav1961.purelib.ui.interfacers.Format;
import chav1961.purelib.ui.interfacers.MultiAction;

public class ContentModelFactory {
	public static final String		APPLICATION_SCHEME_CLASS = "class";	
	public static final String		APPLICATION_SCHEME_FIELD = "field";	
	public static final String		APPLICATION_SCHEME_NAVIGATOR = "navigator";	
	public static final String		APPLICATION_SCHEME_ACTION = "action";	

	private static final String		NAMESPACE_PREFIX = "app";
	private static final String		NAMESPACE_VALUE = "http://ui.purelib.chav1961/";
	private static final String		TAG_I18N = NAMESPACE_PREFIX+":i18n";
	
	public static ContentMetadataInterface forAnnotatedClass(final Class<?> clazz) throws NullPointerException, PreparationException {
		if (clazz == null) {
			throw new NullPointerException("Clazz to build model for can't be null"); 
		}
		else {
			final String			localizerResource = clazz.isAnnotationPresent(LocaleResourceLocation.class) ? clazz.getAnnotation(LocaleResourceLocation.class).value() : null;
			final LocaleResource	localeResource = clazz.getAnnotation(LocaleResource.class);
			final List<Action>		actions = new ArrayList<>();
			final List<Field>		fields = new ArrayList<>();
			
			if (clazz.isAnnotationPresent(MultiAction.class)) {
				for (Action item : clazz.getAnnotation(MultiAction.class).value()) {
					actions.add(item);
				}
			}
			else if (clazz.isAnnotationPresent(Action.class)) {
				actions.add(clazz.getAnnotation(Action.class));
			}
			collectFields(clazz,fields);
			
			final MutableContentNodeMetadata	root = new MutableContentNodeMetadata(clazz.getSimpleName()
														, clazz
														, clazz.getCanonicalName()
														, localizerResource == null ? null : URI.create(localizerResource)
														, localeResource != null ? localeResource.value() : clazz.getSimpleName()
														, localeResource != null ? localeResource.tooltip() : null 
														, localeResource != null ? localeResource.help() : null
														, null
														, URI.create(APPLICATION_SCHEME_CLASS+":/"+clazz.getCanonicalName()));
			for (Field f : fields) {
				final Class<?>			type = f.getType();
				final LocaleResource	fieldLocaleResource = f.getAnnotation(LocaleResource.class);
				
				root.addChild(new MutableContentNodeMetadata(clazz.getSimpleName()
								, type
								, type.getCanonicalName()
								, null
								, fieldLocaleResource.value()
								, fieldLocaleResource.tooltip() 
								, fieldLocaleResource.help()
								, f.getAnnotation(Format.class).value()
								, URI.create(APPLICATION_SCHEME_FIELD+":/"+clazz.getCanonicalName()+"/"+f.getName()+":"+APPLICATION_SCHEME_CLASS+":/"+type.getCanonicalName()))
				);
			}
			return new SimpleContentMetadata(root);
		}
	}

	public static ContentMetadataInterface forXmlDescription(final InputStream contentDescription) throws NullPointerException, PreparationException {
		if (contentDescription == null) {
			throw new NullPointerException("Content description can't be null");
		}
		else {
			final DocumentBuilderFactory 	dbFactory = DocumentBuilderFactory.newInstance();
			final XPathFactory 				xPathfactory = XPathFactory.newInstance();
			final XPath 					xpath = xPathfactory.newXPath();
			final NamespaceContext			nsc = new NamespaceContext() {
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
			
			dbFactory.setNamespaceAware(true);
			dbFactory.setValidating(true);
			dbFactory.setAttribute(XSDConst.SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
			dbFactory.setAttribute(XSDConst.SCHEMA_SOURCE, XSDConst.class.getResource("XMLDescribedApplication.xsd").toString());
			
			try{final DocumentBuilder 		dBuilder = dbFactory.newDocumentBuilder();
				final Document 				doc = dBuilder.parse(contentDescription);
				
				doc.getDocumentElement().normalize();
				
				final String						localizerResource = (String)xpath.compile("//"+TAG_I18N+"/@location").evaluate(doc,XPathConstants.STRING);
				final MutableContentNodeMetadata	root = new MutableContentNodeMetadata("root"
														, Document.class
														, "model"
														, URI.create(localizerResource)
														, "root"
														, null 
														, null
														, null
														, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"));
				buildSubtree(doc.getDocumentElement(),root);
				
				final SimpleContentMetadata result = new SimpleContentMetadata(root);
				
				root.setOwner(result);
				return result;
			} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
				throw new PreparationException("Error preparing xml metadata: "+e.getLocalizedMessage(),e);
			}
		}
	}	

	public static ContentMetadataInterface forDBContentDescription(final DatabaseMetaData dbDescription, final String catalog, final String schema) throws NullPointerException, PreparationException {
		return null;
	}
	
	private static void collectFields(final Class<?> clazz, final List<Field> fields) {
		if (clazz != null) {
			for (Field f : clazz.getDeclaredFields()) {
				if (f.isAnnotationPresent(LocaleResource.class) || f.isAnnotationPresent(Format.class)) {
					fields.add(f);
				}
			}
			collectFields(clazz.getSuperclass(),fields);
		}		
	}

	private static void buildSubtree(final Element document, final MutableContentNodeMetadata node) {
		MutableContentNodeMetadata	child;
		
//		if (document.getNodeType() == Document.ENTITY_NODE) {
			switch (document.getTagName()) {
				case "app:menu" 			:
					final String	menuId = getAttribute(document,"id");
					final String	keySet = getAttribute(document,"keyset");
					
					child = new MutableContentNodeMetadata(menuId
							, String.class
							, "navigation.top."+menuId+(keySet == null ? "" : "?keyset="+keySet)
							, null
							, menuId
							, null 
							, null
							, null
							, null);
					break;
				case "app:submenu"			:
					final String	submenuName = getAttribute(document,"name");
					final String	submenuCaption = getAttribute(document,"caption");
					final String	submenuTooltip = getAttribute(document,"tooltip");
					
					child = new MutableContentNodeMetadata(submenuName
							, String.class
							, "navigation.node."+submenuName
							, null
							, submenuCaption
							, submenuTooltip 
							, null
							, null
							, null);
					break;
				case "app:item"				:
					final String	itemName = getAttribute(document,"name");
					final String	itemCaption = getAttribute(document,"caption");
					final String	itemTooltip = getAttribute(document,"tooltip");
					final String	itemAction = getAttribute(document,"action");
					
					child = new MutableContentNodeMetadata(itemName
							, String.class
							, "navigation.leaf."+itemName
							, null
							, itemCaption
							, itemTooltip 
							, null
							, null
							, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_ACTION+":/"+itemAction));
					break;
				case "app:separator"		:
					child = new MutableContentNodeMetadata("_"
							, String.class
							, "navigation.separator"
							, null
							, "_"
							, null 
							, null
							, null
							, null);
					break;
				case "app:builtinSubmenu"	:
					final String	builtinName = getAttribute(document,"name");
					final String	builtinCaption = getAttribute(document,"caption");
					final String	builtinTooltip = getAttribute(document,"tooltip");
					final String	builtinAction = getAttribute(document,"name");
					
					child = new MutableContentNodeMetadata(builtinName
							, String.class
							, "navigation.node."+builtinName
							, null
							, builtinCaption
							, builtinTooltip 
							, null
							, null
							, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_ACTION+":/"+builtinAction));
					break;
				case "app:keyset"		:
					final String	keysetName = getAttribute(document,"id");
					
					child = new MutableContentNodeMetadata(keysetName
							, String.class
							, "navigation.keyset."+keysetName
							, null
							, keysetName 
							, null
							, null
							, null
							, null);
					break;
				case "app:key"			:
					final String	keyName = getAttribute(document,"id");
					final String	keyAction = getAttribute(document,"action");
					final String	keyCode = getAttribute(document,"code");
					final String	keyCtrl = getAttribute(document,"ctrl");
					final String	keyShift = getAttribute(document,"shift");
					final String	keyAlt = getAttribute(document,"alt");
					final String	keyLabel = (keyCtrl != null || "true".equals(keyCtrl) ? "ctrl " : "")+(keyShift != null || "true".equals(keyShift) ? "shift " : "")+(keyAlt != null || "true".equals(keyAlt) ? "alt " : "") + keyCode; 
					
					child = new MutableContentNodeMetadata(keyName == null ? keyLabel : keyName
							, String.class
							, "keyset.key."+(keyName == null ? keyLabel.replace(' ','_') : keyName)
							, null
							, keyLabel
							, null 
							, null
							, null
							, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_ACTION+":/"+keyAction));
					break;
				case "app:root"	:	// Top level
					for (int index = 0, maxIndex = ((NodeList)document).getLength(); index < maxIndex; index++) {
						if (((NodeList)document).item(index) instanceof Element) {
							buildSubtree((Element) ((NodeList)document).item(index),node);
						}
					}
					return;
				case "app:i18n"	:	// Was processed at the top
					return;
				default :
					System.err.println("Tag="+document.getTagName());
					return;
			}
			for (int index = 0, maxIndex = ((NodeList)document).getLength(); index < maxIndex; index++) {
				if (((NodeList)document).item(index) instanceof Element) {
					buildSubtree((Element) ((NodeList)document).item(index),child);
				}
			}
			node.addChild(child);
			child.setParent(node);
//		}
	}

	private static String getAttribute(final Element document, final String attribute) {
		final Attr 	attr = document.getAttributeNode(attribute);
		
		return attr == null ? null : attr.getValue();
	}
}