package chav1961.purelib.model;


import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.xsd.XSDConst;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.sql.SimpleProvider;
import chav1961.purelib.sql.interfaces.ORMProvider;
import chav1961.purelib.ui.interfaces.Action;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.MultiAction;
import chav1961.purelib.ui.interfaces.RefreshMode;

public class ContentModelFactory {
	public static final String			APPLICATION_SCHEME_CLASS = "class";	
	public static final String			APPLICATION_SCHEME_FIELD = "field";	
	public static final String			APPLICATION_SCHEME_TABLE = "table";	
	public static final String			APPLICATION_SCHEME_COLUMN = "column";	
	public static final String			APPLICATION_SCHEME_ID = "id";	
	public static final String			APPLICATION_SCHEME_NAVIGATOR = "navigator";	
	public static final String			APPLICATION_SCHEME_ACTION = "action";
	public static final String			APPLICATION_SCHEME_BUILTIN_ACTION = "builtin";
	
	public static final String			BUILTIN_LANGUAGE = "builtin.languages";
	public static final String			BUILTIN_STYLE = "style";	

	private static final String			NAMESPACE_PREFIX = "app";
	private static final String			NAMESPACE_VALUE = "http://ui.purelib.chav1961/";
	private static final String			TAG_I18N = NAMESPACE_PREFIX+":i18n";
	
	private static final Set<String>	AVAILABLE_BUILTINS = new HashSet<>();
	
	static {
		AVAILABLE_BUILTINS.add(BUILTIN_LANGUAGE);
		AVAILABLE_BUILTINS.add(BUILTIN_STYLE);
	}
	
	public static ContentMetadataInterface forAnnotatedClass(final Class<?> clazz) throws NullPointerException, PreparationException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		if (clazz == null) {
			throw new NullPointerException("Clazz to build model for can't be null"); 
		}
		else if (!clazz.isAnnotationPresent(LocaleResource.class)) {
			throw new IllegalArgumentException("Class ["+clazz+"] is not annotated with @LocaleResource");
		}
		else {
			final URI				localizerResource = clazz.isAnnotationPresent(LocaleResourceLocation.class) ? URI.create(clazz.getAnnotation(LocaleResourceLocation.class).value()) : null;
			final List<Field>		fields = new ArrayList<>();
			final LocaleResource	localeResource = clazz.getAnnotation(LocaleResource.class);
			
			if (localeResource.value().isEmpty()) {
				throw new IllegalArgumentException("Class ["+clazz+"]: @LocaleResource annotation has empty value");
			}
			else {
				final MutableContentNodeMetadata	root = new MutableContentNodeMetadata(clazz.getSimpleName()
															, clazz
															, clazz.getCanonicalName()
															, localizerResource
															, localeResource.value()
															, localeResource.tooltip() 
															, localeResource.help()
															, null
															, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_CLASS+":/"+clazz.getName()));
				
				collectFields(clazz,fields);
				if (fields.size() == 0) {
					throw new IllegalArgumentException("Class ["+clazz+"] doesn't contain any fields annotated with @LocaleResource or @Format");
				}
				else {
					for (Field f : fields) {
						final Class<?>			type = f.getType();
						final LocaleResource	fieldLocaleResource = f.getAnnotation(LocaleResource.class);
						final MutableContentNodeMetadata	metadata = new MutableContentNodeMetadata(f.getName()
																		, type
																		, f.getName()+"/"+type.getCanonicalName()
																		, null
																		, fieldLocaleResource == null ? "?" : fieldLocaleResource.value()
																		, fieldLocaleResource == null ? "?" : fieldLocaleResource.tooltip() 
																		, fieldLocaleResource == null ? "?" : fieldLocaleResource.help()
																		, f.isAnnotationPresent(Format.class) 
																				? new FieldFormat(type,f.getAnnotation(Format.class).value()) 
																				: null
																		, buildClassFieldApplicationURI(clazz,f)
																	);
						if (f.isAnnotationPresent(MultiAction.class) || f.isAnnotationPresent(Action.class)) {
							collectActions(clazz,f,metadata);
						}
						root.addChild(metadata);
						metadata.setParent(root);
					}
					if (clazz.isAnnotationPresent(MultiAction.class) || clazz.isAnnotationPresent(Action.class)) {
						collectActions(clazz,root);
					}
					
					final List<Method>		methods = new ArrayList<>();
					
					collectMethods(clazz,methods);
					if (methods.size() > 0) {
						for (Method m : methods) {
							if (m.isAnnotationPresent(MultiAction.class) || m.isAnnotationPresent(Action.class)) {
								collectActions(clazz,m,root);
							}
						}
					}
					
					final SimpleContentMetadata	result = new SimpleContentMetadata(root); 
					
					root.setOwner(result);
					return result;
			}
			}
		}
	}

	public static ContentMetadataInterface forXmlDescription(final InputStream contentDescription) throws NullPointerException, EnvironmentException {
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
				throw new EnvironmentException("Error preparing xml metadata: "+e.getLocalizedMessage(),e);
			}
		}
	}	

	public static ContentMetadataInterface forDBContentDescription(final DatabaseMetaData dbDescription, final String catalog, final String schema, final String table) throws NullPointerException, PreparationException, ContentException {
		if (dbDescription == null) {
			throw new NullPointerException("Database description can't be null");
		}
		else if (schema == null || schema.isEmpty()) {
			throw new IllegalArgumentException("Schema name can't be null or empty");
		}
		else if (schema.contains("_") || schema.contains("%")) {
			throw new UnsupportedOperationException("Wildcards in the schema name are not supported yet");
		}
		else if (table == null || table.isEmpty()) {
			throw new IllegalArgumentException("Table name can't be null or empty");
		}
		else if (table.contains("_") || table.contains("%")) {
			throw new UnsupportedOperationException("Wildcards in the table name are not supported yet");
		}
		else {
			final MutableContentNodeMetadata	root = new MutableContentNodeMetadata(table
													, TableContainer.class
													, table
													, null
													, schema+"."+table
													, schema+"."+table+".tt" 
													, schema+"."+table+".help"
													, null
													, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_TABLE+":/"+TableContainer.class.getCanonicalName()));
			
			
			try(final ResultSet	rs = dbDescription.getColumns(catalog, schema, table, "%")) {
				while (rs.next()) {
					final Class<?>			type = SQLUtils.classByTypeId(rs.getInt("DATA_TYPE"));
					final MutableContentNodeMetadata	metadata = new MutableContentNodeMetadata(rs.getString("COLUMN_NAME")
																	, type
																	, rs.getString("TABLE_NAME")+"/"+rs.getString("COLUMN_NAME")
																	, null
																	, rs.getString("REMARKS") == null ? "?" : rs.getString("REMARKS")
																	, rs.getString("REMARKS") == null ? "?" : rs.getString("REMARKS")+".tt" 
																	, rs.getString("REMARKS") == null ? "?" : rs.getString("REMARKS")+".help"
																	, new FieldFormat(type,buildColumnFormat(rs))
																	, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_COLUMN+":/"+table+"/"+rs.getString("COLUMN_NAME")+"?seq="+rs.getString("ORDINAL_POSITION")+"&type"+rs.getString("DATA_TYPE"))
																);
					root.addChild(metadata);
					metadata.setParent(root);
				}
			} catch (SQLException e) {
				throw new ContentException(e.getLocalizedMessage());
			}
			try(final ResultSet	rs = dbDescription.getPrimaryKeys(catalog, schema, table)) {
				
				while (rs.next()) {
					final Class<?>			type = SQLUtils.classByTypeId(rs.getInt("DATA_TYPE"));
					final MutableContentNodeMetadata	metadata = new MutableContentNodeMetadata(rs.getString("PKCOLUMN_NAME")
																	, type
																	, rs.getString("PKTABLE_NAME")+"/"+rs.getString("PKCOLUMN_NAME")
																	, null
																	, "?"
																	, "?" 
																	, "?"
																	, null
																	, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_ID+":/"+table+"/"+rs.getString("PKCOLUMN_NAME"))
																);
					root.addChild(metadata);
					metadata.setParent(root);
				}
			} catch (SQLException e) {
				throw new ContentException(e.getLocalizedMessage());
			}
			final SimpleContentMetadata result = new SimpleContentMetadata(root);
			
			root.setOwner(result);
			return result;
		}
	}

	public static <Record> ORMProvider<Record, Record> buildORMProvider(final ContentNodeMetadata clazz, final ContentNodeMetadata table) throws IOException, ContentException {
		if (clazz == null) {
			throw new NullPointerException("Class metadata can't be null");
		}
		else if (table == null) {
			throw new NullPointerException("Table metadata can't be null");
		}
		else {
			// TODO:
			final Set<String>	tableFields = new HashSet<>(), classFields = new HashSet<>();
			final List<String>	pkFields = new ArrayList<>();
			
			new SimpleContentMetadata(clazz).walkDown((mode, applicationPath, uiPath, node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (applicationPath.toString().contains(APPLICATION_SCHEME_FIELD)) {
						classFields.add(node.getName().toUpperCase());
					}
				}
				return ContinueMode.CONTINUE;
			},clazz.getUIPath());
			new SimpleContentMetadata(table).walkDown((mode, applicationPath, uiPath, node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (applicationPath.toString().contains(APPLICATION_SCHEME_COLUMN)) {
						tableFields.add(node.getName().toUpperCase());
					}
					else if (applicationPath.toString().contains(APPLICATION_SCHEME_ID)) {
						pkFields.add(node.getName().toUpperCase());
					}
				}
				return ContinueMode.CONTINUE;
			},clazz.getUIPath());
			classFields.retainAll(tableFields);
			
			if (classFields.size() == 0) {
				throw new IllegalArgumentException("Class and table has no any intersections by it's fields. At least one field name must be common for them");
			}
			else {
				return new SimpleProvider<Record>(table, clazz, (Class<Record>)clazz.getType(), classFields.toArray(new String[classFields.size()]), pkFields.toArray(new String[pkFields.size()]));
			}
		}
	}

	static URI buildClassFieldApplicationURI(final Class<?> clazz, final Field f) {
		return URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_FIELD+":/"+clazz.getName()+"/"+f.getName());
	}

	static URI buildClassMethodApplicationURI(final Class<?> clazz, final String action) {
		return URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_ACTION+":/"+clazz.getSimpleName()+"."+action);
	}

	private static void buildSubtree(final Element document, final MutableContentNodeMetadata node) throws EnvironmentException {
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
					final String	groupAction = getAttribute(document,"group");
					
					child = new MutableContentNodeMetadata(itemName
							, String.class
							, "navigation.leaf."+itemName
							, null
							, itemCaption
							, itemTooltip 
							, null
							, null
							, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_ACTION+":/"+itemAction+(groupAction != null ? "#"+groupAction : "")));
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
					final String	builtinAction = getAttribute(document,"action");
					
					if (!AVAILABLE_BUILTINS.contains(builtinName)) {
						throw new EnvironmentException("Illegal name ["+builtinName+"] for built-in navigation. Available names are "+AVAILABLE_BUILTINS);						
					}
					else {
						child = new MutableContentNodeMetadata(builtinName
								, String.class
								, "navigation.node."+builtinName
								, null
								, builtinCaption
								, builtinTooltip 
								, null
								, null
								, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_ACTION+":"+APPLICATION_SCHEME_BUILTIN_ACTION+":/"+builtinAction));
					}
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

	private static void collectMethods(final Class<?> clazz, final List<Method> methods) {
		if (clazz != null) {
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.isAnnotationPresent(Action.class)) {
					if (m.getParameterCount() != 0) {
						throw new IllegalArgumentException("Method ["+m+"] annotated with @Action must not have any parameters"); 
					}
					else if (m.getReturnType() != void.class && m.getReturnType() != RefreshMode.class) {
						throw new IllegalArgumentException("Method ["+m+"] annotated with @Action must return void or RefreshMode data type"); 
					}
					else {
						methods.add(m);
					}
				}
			}
			collectMethods(clazz.getSuperclass(),methods);
		}		
	}
	
	private static void collectActions(final Class<?> clazz, final MutableContentNodeMetadata root) throws IllegalArgumentException, NullPointerException, SyntaxException, LocalizationException, ContentException {
		if (clazz != null) {
			if (clazz.isAnnotationPresent(MultiAction.class) || clazz.isAnnotationPresent(Action.class)) {
				for (Action item : clazz.isAnnotationPresent(MultiAction.class) 
								 	? clazz.getAnnotation(MultiAction.class).value() 
								 	: new Action[] {clazz.getAnnotation(Action.class)}) {
					root.addChild(
							new MutableContentNodeMetadata(item.actionString()
											, ActionEvent.class
											, "./"+clazz.getSimpleName()+"."+item.actionString()
											, null
											, item.resource().value()
											, item.resource().tooltip()
											, item.resource().help()
											, null
											, buildClassMethodApplicationURI(clazz,item.actionString())
							)
					);
				}
			}
			collectActions(clazz.getSuperclass(),root);
		}
	}

	private static void collectActions(final Class<?> clazz, final Field field, final MutableContentNodeMetadata root) throws IllegalArgumentException, NullPointerException, SyntaxException, LocalizationException, ContentException {
		if (field.isAnnotationPresent(MultiAction.class) || field.isAnnotationPresent(Action.class)) {
			for (Action item : field.isAnnotationPresent(MultiAction.class) 
							 	? field.getAnnotation(MultiAction.class).value() 
							 	: new Action[] {field.getAnnotation(Action.class)}) {
				root.addChild(
						new MutableContentNodeMetadata(item.actionString()
										, ActionEvent.class
										, "./"+field.getName()+"."+item.actionString()
										, null
										, item.resource().value()
										, item.resource().tooltip()
										, item.resource().help()
										, null
										, buildClassMethodApplicationURI(clazz,field.getName()+"."+item.actionString())
						)
				);
			}
		}
	}

	private static void collectActions(final Class<?> clazz, final Method method, final MutableContentNodeMetadata root) throws IllegalArgumentException, NullPointerException, SyntaxException, LocalizationException, ContentException {
		if (method.isAnnotationPresent(MultiAction.class) || method.isAnnotationPresent(Action.class)) {
			for (Action item : method.isAnnotationPresent(MultiAction.class) 
							 	? method.getAnnotation(MultiAction.class).value() 
							 	: new Action[] {method.getAnnotation(Action.class)}) {
				root.addChild(
						new MutableContentNodeMetadata(item.actionString()
										, ActionEvent.class
										, "./"+method.getName()+"."+item.actionString()
										, null
										, item.resource().value()
										, item.resource().tooltip()
										, item.resource().help()
										, null
										, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+APPLICATION_SCHEME_ACTION+":/"+clazz.getSimpleName()+"/"+method.getName()+"()."+item.actionString())
						)
				);
			}
		}
	}
	
	private static String buildColumnFormat(final ResultSet rs) throws SQLException {
		final StringBuilder	sb = new StringBuilder();
		
		if (rs.getInt("DECIMAL_DIGITS") != 0) {
			if (rs.getInt("COLUMN_SIZE") == 0) {
				sb.append("18.").append(rs.getInt("DECIMAL_DIGITS"));
			}
			else {
				sb.append(rs.getInt("COLUMN_SIZE")).append('.').append(rs.getInt("DECIMAL_DIGITS"));
			}
		}
		else {
			if (rs.getInt("COLUMN_SIZE") == 0) {
				sb.append("30");
			}
			else if (rs.getInt("COLUMN_SIZE") > 50) {
				sb.append("30");
			}
			else {
				sb.append(rs.getInt("COLUMN_SIZE"));
			}
		}
		if (rs.getInt("NULLABLE") != DatabaseMetaData.columnNullable) {
			sb.append("m");
		}
		return sb.toString();
	}
}
