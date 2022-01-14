package chav1961.purelib.model;



import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.xsd.XSDConst;
import chav1961.purelib.enumerations.XSDCollection;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.SPIServiceNavigationMember;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.ui.interfaces.Action;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.MultiAction;
import chav1961.purelib.ui.interfaces.RefreshMode;


/**
 * <p>THis class is a factory for most model sources. It can load models from external sources or build models by existent entities.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.5
 */
public class ContentModelFactory {
	private static final String			NAMESPACE_PREFIX = "app";
	private static final String			NAMESPACE_VALUE = "http://ui.purelib.chav1961/";

	private static final String			XML_TAG_APP_ROOT = NAMESPACE_PREFIX+":root";
	private static final String			XML_TAG_APP_I18N = NAMESPACE_PREFIX+":i18n";
	private static final String			XML_TAG_APP_TITLE = NAMESPACE_PREFIX+":title";
	private static final String			XML_TAG_APP_MENU = NAMESPACE_PREFIX+":menu";
	private static final String			XML_TAG_APP_SUBMENU = NAMESPACE_PREFIX+":submenu";
	private static final String			XML_TAG_APP_ITEM = NAMESPACE_PREFIX+":item";	
	private static final String			XML_TAG_APP_SUBMENU_REF = NAMESPACE_PREFIX+":submenuref";
	private static final String			XML_TAG_APP_ITEM_REF = NAMESPACE_PREFIX+":itemref";	
	private static final String			XML_TAG_APP_SEPARATOR = NAMESPACE_PREFIX+":separator";
	private static final String			XML_TAG_APP_PLACEHOLDER = NAMESPACE_PREFIX+":placeholder";
	private static final String			XML_TAG_APP_BUILTIN_SUBMENU = NAMESPACE_PREFIX+":builtinSubmenu";
	private static final String			XML_TAG_APP_KEYSET = NAMESPACE_PREFIX+":keyset";
	private static final String			XML_TAG_APP_KEY = NAMESPACE_PREFIX+":key";	
	
	private static final String			XML_ATTR_ID = "id";
	private static final String			XML_ATTR_NAME = "name"; 
	private static final String			XML_ATTR_REF = "ref"; 
	private static final String			XML_ATTR_TYPE = "type"; 
	private static final String			XML_ATTR_LABEL = "label";
	private static final String			XML_ATTR_CAPTION = "caption";
	private static final String			XML_ATTR_TOOLTIP = "tooltip";
	private static final String			XML_ATTR_HELP = "help";
	private static final String			XML_ATTR_FORMAT = "format";
	private static final String			XML_ATTR_ACTION = "action";
	private static final String			XML_ATTR_KEYSET = "keyset";
	private static final String			XML_ATTR_GROUP = "group";	
	private static final String			XML_ATTR_ICON = "icon";

	/**
	 * <p>Build model for annotated class. Class to build model for must be annotated with {@linkplain LocaleResourceLocation} and {@linkplain LocaleResource} annotations,
	 * and some of it's fields must be annotated with {@linkplain LocaleResource} and/or {@linkplain Format} annotations</p>
	 * @param clazz class to build model for
	 * @return model built. Can't be null
	 * @throws NullPointerException class to build model for is null
	 * @throws IllegalArgumentException come mandatory annotations are missing in the class
	 * @throws LocalizationException on any localization exceptions
	 * @throws ContentException on any format errors 
	 */
	public static ContentMetadataInterface forAnnotatedClass(final Class<?> clazz) throws NullPointerException, IllegalArgumentException, LocalizationException, ContentException {
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
				final MutableContentNodeMetadata	root = new MutableContentNodeMetadata("class"
															, clazz
															, clazz.getCanonicalName()
															, localizerResource
															, localeResource.value()
															, localeResource.tooltip() 
															, localeResource.help()
															, null
															, ModelUtils.buildUriByClass(clazz)
															, null);
				
				collectFields(clazz,fields,true);
				if (fields.size() == 0) {
					throw new IllegalArgumentException("Class ["+clazz+"] doesn't contain any fields annotated with @LocaleResource or @Format");
				}
				else {
					for (Field f : fields) {
						final Class<?>			type = f.getType();
						final LocaleResource	fieldLocaleResource = f.getAnnotation(LocaleResource.class);
						final MutableContentNodeMetadata	metadata = new MutableContentNodeMetadata(f.getName()
																		, type
																		, f.getName()+"/"+escapeBrackets(type.getCanonicalName())
																		, null
																		, fieldLocaleResource == null ? "?" : fieldLocaleResource.value()
																		, fieldLocaleResource == null ? null : fieldLocaleResource.tooltip() 
																		, fieldLocaleResource == null ? null : fieldLocaleResource.help()
																		, f.isAnnotationPresent(Format.class) 
																				? new FieldFormat(type, f.getAnnotation(Format.class).value(), f.getAnnotation(Format.class).wizardType()) 
																				: null
																		, buildClassFieldApplicationURI(clazz,f)
																		, null
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
					
					collectMethods(clazz,methods,true);
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

	/**
	 * <p>Build model for ordinal class. Differ to {@linkplain #forAnnotatedClass(Class)} method, this model is reduced and contains field descriptors only.
	 * Localizer for the given model will be associated to PureLib localizer, all labels in the model will be replaced with model names.</p> 
	 * @param clazz class to build model for
	 * @return model built. Can't be null
	 * @throws NullPointerException class to build model for is null
	 * @throws IllegalArgumentException come mandatory annotations are missing in the class
	 * @throws LocalizationException on any localization exceptions
	 * @throws ContentException on any format errors 
	 * @since 0.0.4
	 */
	public static ContentMetadataInterface forOrdinalClass(final Class<?> clazz) throws NullPointerException, IllegalArgumentException, LocalizationException, ContentException {
		if (clazz == null) {
			throw new NullPointerException("Clazz to build model for can't be null"); 
		}
		else {
			final URI				localizerResource = PureLibLocalizer.LOCALIZER_SCHEME_URI;
			final List<Field>		fields = new ArrayList<>();
			
			final MutableContentNodeMetadata	root = new MutableContentNodeMetadata("class"
														, clazz
														, clazz.getCanonicalName()
														, localizerResource
														, clazz.getSimpleName()
														, null 
														, null
														, null
														, ModelUtils.buildUriByClass(clazz)
														, null);
			
			collectFields(clazz,fields,false);
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
																	, fieldLocaleResource == null ? f.getName() : fieldLocaleResource.value()
																	, fieldLocaleResource == null ? null : fieldLocaleResource.tooltip() 
																	, fieldLocaleResource == null ? null : fieldLocaleResource.help()
																	, f.isAnnotationPresent(Format.class) 
																			? new FieldFormat(type, f.getAnnotation(Format.class).value(), f.getAnnotation(Format.class).wizardType()) 
																			: null
																	, buildClassFieldApplicationURI(clazz,f)
																	, null
																);
					root.addChild(metadata);
					metadata.setParent(root);
				}
				final SimpleContentMetadata	result = new SimpleContentMetadata(root); 
				
				root.setOwner(result);
				return result;
			}
		}
	}

	/**
	 * Build model by XML description.</p>
	 * @param contentDescription XML-based model descriptor. Can't be null
	 * @return metadata parsed. Can't be null.
	 * @throws NullPointerException on any parameter is null
	 * @throws EnvironmentException on invalid XML
	 * @see XSDCollection.XMLDescribedApplication
	 * @since 0.0.4
	 */
	public static ContentMetadataInterface forXmlDescription(final InputStream contentDescription) throws NullPointerException, EnvironmentException {
		return forXmlDescription(contentDescription, XSDCollection.XMLDescribedApplication);
	}
	
	/**
	 * Build model by XML description.</p>
	 * @param contentDescription XML-based model descriptor. Can't be null
	 * @param contentType content type to parse. Can't be null
	 * @return metadata parsed. Can't be null.
	 * @throws NullPointerException on any parameter is null
	 * @throws EnvironmentException on invalid XML
	 * @see XSDCollection.XMLDescribedApplication
	 * @since 0.0.4
	 */
	public static ContentMetadataInterface forXmlDescription(final InputStream contentDescription, final XSDCollection contentType) throws NullPointerException, EnvironmentException {
		if (contentDescription == null) {
			throw new NullPointerException("Content description can't be null");
		}
		else {
			final DocumentBuilderFactory 	dbFactory = DocumentBuilderFactory.newInstance();
			final XPathFactory 				xPathfactory = XPathFactory.newInstance();
			final XPath 					xpath = xPathfactory.newXPath();
			final NamespaceContext			nsc = new NamespaceContext() {
												@Override
												public Iterator<String> getPrefixes(final String namespaceURI) {
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
			dbFactory.setAttribute(XSDConst.SCHEMA_SOURCE, XSDConst.getResource("XMLDescribedApplication.xsd").toString());
			
			try{final DocumentBuilder 		dBuilder = dbFactory.newDocumentBuilder();
				final Document 				doc = dBuilder.parse(contentDescription);
				final Element				docRoot = doc.getDocumentElement(); 
				
				docRoot.normalize();
				
				final String				localizerResource = (String)xpath.compile("//"+XML_TAG_APP_I18N+"/@location").evaluate(doc,XPathConstants.STRING);
				final String				titleResource = (String)xpath.compile("//"+XML_TAG_APP_TITLE+"/@title").evaluate(doc,XPathConstants.STRING);
				final String				tooltipResource = (String)xpath.compile("//"+XML_TAG_APP_TITLE+"/@tooltip").evaluate(doc,XPathConstants.STRING);
				final String				helpResource = (String)xpath.compile("//"+XML_TAG_APP_TITLE+"/@help").evaluate(doc,XPathConstants.STRING);
				final SimpleContentMetadata result;
				
				switch (doc.getDocumentElement().getTagName()) {
					case "app:root"		:
						final MutableContentNodeMetadata	rootApp = new MutableContentNodeMetadata("root"
																, Document.class
																, "model"
																, URI.create(localizerResource)
																, titleResource == null || titleResource.isEmpty() ? "root" : titleResource  
																, tooltipResource
																, helpResource
																, null
																, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/")
																, null);
						buildSubtree(doc.getDocumentElement(),rootApp);
						
						result = new SimpleContentMetadata(rootApp);
						
						rootApp.setOwner(result);
						break;
					case "app:class"	:
						final String	clazzName = getAttribute(docRoot,XML_ATTR_TYPE);
						final String	clazzLabel = getAttribute(docRoot,XML_ATTR_LABEL);
						final String	clazzTooltip = getAttribute(docRoot,XML_ATTR_TOOLTIP);
						final String	clazzHelp = getAttribute(docRoot,XML_ATTR_HELP);
						
						try{final Class<?> 	clazz = Class.forName(clazzName);
							
							final MutableContentNodeMetadata	rootClass = new MutableContentNodeMetadata("class"
																	, clazz
																	, clazz.getCanonicalName()
																	, URI.create(localizerResource)
																	, clazzLabel
																	, clazzTooltip
																	, clazzHelp
																	, null
																	, ModelUtils.buildUriByClass(clazz)
																	, null);

							appendFields(docRoot,clazz,rootClass);
							appendMethods(docRoot,clazz,rootClass);
							result = new SimpleContentMetadata(rootClass);
							
							rootClass.setOwner(result);
							break;
						} catch (ClassNotFoundException e) {
							throw new EnvironmentException("Error preparing xml metadata: class ["+clazzName+"] mentioned in the XML descriptor is unknown in this JVM environment",e);
						}
					default :
						throw new UnsupportedOperationException("Root tag ["+doc.getDocumentElement().getTagName()+"] is not supported yet"); 
				}
				
				return result;
			} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
				throw new EnvironmentException("Error preparing xml metadata: "+e.getLocalizedMessage(),e);
			}
		}
	}	

	/**
	 * <p>Build model by database metadata.</p>
	 * @param dbDescription database decription. Can't be null
	 * @param catalog database catalog or null
	 * @param schema database schema. Can't be null
	 * @return metadata built. Can't be null.
	 * @throws NullPointerException on any parameter is null
	 * @throws ContentException on any content errors
	 * @see DatabaseMetaData
	 * @since 0.0.4
	 */
	public static ContentMetadataInterface forDBContentDescription(final DatabaseMetaData dbDescription, final String catalog, final String schema) throws NullPointerException, ContentException {
		if (dbDescription == null) {
			throw new NullPointerException("Database description can't be null");
		}
		else if (schema == null || schema.isEmpty()) {
			throw new IllegalArgumentException("Schema name can't be null or empty");
		}
		else if (schema.contains("_") || schema.contains("%")) {
			throw new UnsupportedOperationException("Wildcards in the schema name are not supported yet");
		}
		else {
			try(final ResultSet	rss = dbDescription.getSchemas(catalog, schema)) {
				if (!rss.next()) {
					return null;
				}
			} catch (SQLException e) {
				throw new ContentException(e.getLocalizedMessage());
			}
			
			try(final ResultSet	rs = dbDescription.getTables(catalog, schema, "%", new String[]{"TABLE", "VIEW"})) {
				final MutableContentNodeMetadata	root = new MutableContentNodeMetadata(schema
															, SchemaContainer.class
															, schema
															, null
															, schema
															, schema+".tt" 
															, schema+".help"
															, null
															, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_SCHEMA+":/"+schema)
															, null);

				while (rs.next()) {
					root.addChild(forDBContentDescription(dbDescription, catalog, schema, rs.getString("TABLE_NAME")).getRoot());
				}
				final SimpleContentMetadata result = new SimpleContentMetadata(root);
				
				root.setOwner(result);
				return result;
			} catch (SQLException e) {
				throw new ContentException(e.getLocalizedMessage());
			}
		}
	}	

	/**
	 * <p>Build model by database table metadata.</p>
	 * @param dbDescription database decription. Can't be null
	 * @param catalog database catalog or null
	 * @param schema database schema. Can't be null
	 * @param table database table. Can't be null
	 * @return metadata built. Can't be null.
	 * @throws NullPointerException on any parameter is null
	 * @throws ContentException on any content errors
	 * @see DatabaseMetaData
	 * @since 0.0.4
	 */
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
		else if (table.contains("%")) {
			throw new UnsupportedOperationException("Wildcards in the table name are not supported yet");
		}
		else {
			final String						schemaAndTable = schema+"."+table; 

			try(final ResultSet	rs = dbDescription.getTables(catalog, schema, table, new String[] {"TABLE"})) {
				if (!rs.next()) {
					throw new ContentException("Table ["+schema+'.'+table+"] is missing in the database");
				}				
			} catch (SQLException e) {
				throw new ContentException("Table ["+schema+'.'+table+"]: "+e.getLocalizedMessage(), e);
			}
			
			final MutableContentNodeMetadata	root = new MutableContentNodeMetadata(table
													, TableContainer.class
													, schemaAndTable
													, null
													, schemaAndTable
													, schemaAndTable+".tt" 
													, schemaAndTable+".help"
													, null
													, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_TABLE+":/"+schemaAndTable)
													, null);
			final Map<String,Class<?>>			fieldTypes = new HashMap<>();
			final Map<String,Integer>			primaryKeys = new HashMap<>();
			
			try(final ResultSet	rs = dbDescription.getPrimaryKeys(catalog, schema, table)) {
				while (rs.next()) {
					primaryKeys.put(rs.getString("COLUMN_NAME"),rs.getInt("KEY_SEQ"));
				}				
			} catch (SQLException e) {
				throw new ContentException(e.getLocalizedMessage());
			}
			
			boolean		found = false;
			try(final ResultSet	rs = dbDescription.getColumns(catalog, schema, table, "%")) {
				while (rs.next()) {
					final Class<?>	type = SQLUtils.classByTypeId(rs.getInt("DATA_TYPE"));
					final String	fieldName = rs.getString("COLUMN_NAME");
					final String	label = schemaAndTable+"."+fieldName;
					String			appUri = ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_COLUMN+":/"+schemaAndTable+"/"+fieldName
											+"?seq="+rs.getString("ORDINAL_POSITION")+"&type="+rs.getString("DATA_TYPE");

					if (primaryKeys.containsKey(fieldName)) {
						appUri += "&pkSeq="+primaryKeys.get(fieldName);
					}
					
					final MutableContentNodeMetadata	metadata = new MutableContentNodeMetadata(fieldName
																	, type
																	, fieldName
																	, null
																	, rs.getString("REMARKS") == null ? label : rs.getString("REMARKS")
																	, rs.getString("REMARKS") == null ? label+".tt" : rs.getString("REMARKS")+".tt" 
																	, rs.getString("REMARKS") == null ? label+".help" : rs.getString("REMARKS")+".help"
																	, new FieldFormat(type, buildColumnFormat(rs))
																	, URI.create(appUri)
																	, null
																);
					root.addChild(metadata);
					metadata.setParent(root);
					fieldTypes.put(fieldName,type);
					found = true;
				}
			} catch (SQLException e) {
				throw new ContentException(e.getLocalizedMessage());
			}
			if (!found) {
				throw new ContentException("Table ["+schema+'.'+table+"] is missing in the database");
			}
			try(final ResultSet	rs = dbDescription.getPrimaryKeys(catalog, schema, table)) {
				
				while (rs.next()) {
					final String			columnName = rs.getString("COLUMN_NAME");
					final Class<?>			type = fieldTypes.get(columnName);
					final MutableContentNodeMetadata	metadata = new MutableContentNodeMetadata(rs.getString("COLUMN_NAME")
																	, type
																	, columnName+"/primaryKey"
																	, null
																	, schemaAndTable+"."+columnName
																	, schemaAndTable+"."+columnName+".tt" 
																	, schemaAndTable+"."+columnName+".help"
																	, null
																	, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ID+":/"+schemaAndTable+"/"+columnName)
																	, null
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

	/**
	 * <p>Build model by query content metadata.</p>
	 * @param rsmd query content metadata. Can't be null
	 * @return metadata built. Can't be null.
	 * @throws NullPointerException on any parameter is null
	 * @throws ContentException on any content errors
	 * @see ResultSetMetaData
	 * @since 0.0.4
	 */
	public static ContentMetadataInterface forQueryContentDescription(final ResultSetMetaData rsmd) throws NullPointerException, ContentException {
		if (rsmd == null) {
			throw new NullPointerException("Result set description can't be null");
		}
		else {
			String	schemaAndTable = "";
			
			try{schemaAndTable = rsmd.getSchemaName(1)+"."+rsmd.getTableName(1); 
				
				final MutableContentNodeMetadata	root = new MutableContentNodeMetadata("table"
														, TableContainer.class
														, schemaAndTable
														, null
														, schemaAndTable
														, schemaAndTable+".tt" 
														, schemaAndTable+".help"
														, null
														, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_TABLE+":/"+schemaAndTable)
														, null);
				final Map<String,Class<?>>			fieldTypes = new HashMap<>();
				
				boolean		found = false;
			
				for (int columnIndex = 1; columnIndex <= rsmd.getColumnCount(); columnIndex++) {
					final Class<?>			type = SQLUtils.classByTypeId(rsmd.getColumnType(columnIndex));
					final String			fieldName = rsmd.getColumnName(columnIndex);
					final String			description = rsmd.getColumnLabel(columnIndex);
					final MutableContentNodeMetadata	metadata = new MutableContentNodeMetadata(fieldName
																	, type
																	, fieldName
																	, null
																	, description == null ? schemaAndTable+"."+fieldName : description
																	, description == null ? schemaAndTable+"."+fieldName+".tt" : description+".tt" 
																	, description == null ? schemaAndTable+"."+fieldName+".help" : description+".help"
																	, new FieldFormat(type, buildColumnFormat(rsmd,columnIndex))
																	, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_COLUMN+":/"+schemaAndTable+"/"+fieldName
																			+"?seq="+columnIndex+"&type="+rsmd.getColumnTypeName(columnIndex))
																	, null
																);
					root.addChild(metadata);
					metadata.setParent(root);
					fieldTypes.put(fieldName,type);
					found = true;
				}
				
				final SimpleContentMetadata result = new SimpleContentMetadata(root);
				
				root.setOwner(result);
				return result;
			} catch (SQLException e) {
				throw new ContentException(e.getLocalizedMessage());
			}
		}
	}

	/**
	 * <p>Build model by JSON description.</p>
	 * @param contentDescription JSON content. Can't be null
	 * @return metadata built. Can't be null.
	 * @throws NullPointerException on any parameter is null
	 * @throws IOException on any content errors
	 * @since 0.0.5
	 */
	public static ContentMetadataInterface forJsonDescription(final Reader contentDescription) throws IOException, NullPointerException {
		if (contentDescription == null) {
			throw new NullPointerException("Content description reader can't be null");
		}
		else {
			final JsonStaxParser				parser = new JsonStaxParser(contentDescription);
			
			parser.next();
			
			final MutableContentNodeMetadata	root = ModelUtils.deserializeFromJson(parser);
			final SimpleContentMetadata			result = new SimpleContentMetadata(root); 
			
			root.setOwner(result);
			return result;
		}
	}	
	
	public static <T> ContentMetadataInterface forSPIServiceTree(final Class<T> spiService) throws NullPointerException, PreparationException, ContentException {
		if (spiService == null) {
			throw new NullPointerException("SPI service can't be null"); 
		}
		else {
			// TODO:
			final List<T>	services = new ArrayList<>();
			
			for (T item : ServiceLoader.load(spiService)) {
				services.add(item);
			}
			if (services.isEmpty()) {
				throw new ContentException("No any services for ["+spiService.getCanonicalName()+"] were found");
			}
			else {
				final MutableContentNodeMetadata	root = new MutableContentNodeMetadata("root"
														, String.class
														, Constants.MODEL_NAVIGATION_TOP_PREFIX+".root"
														, null
														, "root"
														, null 
														, null
														, null
														, null
														, null);
				for (T item : services) {
					try{final ContentMetadataInterface	mdi = forAnnotatedClass(item.getClass());
						final ContentNodeMetadata		metadata = mdi.getRoot(); 
						
						final MutableContentNodeMetadata	child = new MutableContentNodeMetadata(metadata.getName()
																, metadata.getType()
																, Constants.MODEL_NAVIGATION_LEAF_PREFIX+'.'+metadata.getName()
																, null
																, metadata.getLabelId()
																, metadata.getTooltipId()
																, null
																, null
																, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION+":/"+metadata.getName())
																, metadata.getIcon());
						final MutableContentNodeMetadata	current = (item instanceof SPIServiceNavigationMember) 
																	? (MutableContentNodeMetadata)buildSPIPluginMenuSubtree(root,((SPIServiceNavigationMember)item).getNavigationURI().getPath().toString()) 
																	: root;
						
						current.addChild(child);
						child.setParent(current);
					} catch (Exception exc) {
					}
				}
				final SimpleContentMetadata result = new SimpleContentMetadata(root);
				
				root.setOwner(result);
				return result;
			}
		}
	}
	
	static URI buildClassFieldApplicationURI(final Class<?> clazz, final Field f) {
		if (Modifier.isPublic(f.getModifiers())) {
			return URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+clazz.getName()+"/"+f.getName());
		}
		else if (Modifier.isProtected(f.getModifiers())) {
			return URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+clazz.getName()+"/"+f.getName()+"?visibility=protected");
		}
		else if (Modifier.isPrivate(f.getModifiers())) {
			return URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+clazz.getName()+"/"+f.getName()+"?visibility=private");
		}
		else {
			return URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+clazz.getName()+"/"+f.getName()+"?visibility=package");
		}
	}

	static URI buildClassMethodApplicationURI(final Class<?> clazz, final String action) {
		return URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION+":/"+clazz.getSimpleName()+"."+action);
	}

	private static void appendFields(final Element root, final Class<?> clazz, final MutableContentNodeMetadata rootClass) throws EnvironmentException {
		final NodeList	list = root.getChildNodes();
		
		for (int index = 0, maxIndex = list.getLength(); index < maxIndex; index++) {
			final Node	item = list.item(index);
			
			if ("app:field".equals(item.getNodeName())) {
				final String		fieldName = getAttribute((Element)item,XML_ATTR_NAME);
				final String		fieldLabel = getAttribute((Element)item,XML_ATTR_LABEL);
				final String		fieldTooltip = getAttribute((Element)item,XML_ATTR_TOOLTIP);
				final String		fieldHelp = getAttribute((Element)item,XML_ATTR_HELP);
				final String		fieldFormat = getAttribute((Element)item,XML_ATTR_FORMAT);
				final String		fieldIcon = getAttribute((Element)item,XML_ATTR_ICON);
				
				try{final Field		f = seekField(clazz,fieldName);
					final Class<?>	type = f.getType();
						
					final MutableContentNodeMetadata	metadata = new MutableContentNodeMetadata(f.getName()
															, type
															, f.getName()+"/"+type.getCanonicalName()
															, null
															, fieldLabel == null ? "?" : fieldLabel
															, fieldTooltip 
															, fieldHelp
															, fieldFormat == null ? null : new FieldFormat(type, fieldFormat)
															, buildClassFieldApplicationURI(clazz,f)
															, fieldIcon == null ? null : URI.create(fieldIcon)
														);
					rootClass.addChild(metadata);
					metadata.setParent(rootClass);
				} catch (NoSuchFieldException exc) {
					throw new EnvironmentException("Error preparing xml metadata: class ["+clazz.getCanonicalName()+"] doesn't contain field ["+fieldName+"]");
				}
			}
		}
	}

	private static Field seekField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException {
		if (clazz != null) {
			for (Field item : clazz.getDeclaredFields()) {
				if (fieldName.equals(item.getName())) {
					return item;
				}
			}
			return seekField(clazz.getSuperclass(),fieldName);
		}
		else {
			throw new NoSuchFieldException("Field ["+fieldName+"] is missing in the class");
		}
	}

	private static void appendMethods(final Element root, final Class<?> clazz, final MutableContentNodeMetadata rootClass) throws EnvironmentException {
		final NodeList	list = root.getChildNodes();
		
		for (int index = 0, maxIndex = list.getLength(); index < maxIndex; index++) {
			final Node	item = list.item(index);
			
			if ("app:action".equals(item.getNodeName())) {
				final String		methodName = getAttribute((Element)item,XML_ATTR_NAME);
				final String		methodAction = getAttribute((Element)item,XML_ATTR_ACTION);
				final String		methodLabel = getAttribute((Element)item,XML_ATTR_LABEL);
				final String		methodTooltip = getAttribute((Element)item,XML_ATTR_TOOLTIP);
				final String		methodHelp = getAttribute((Element)item,XML_ATTR_HELP);
				final String		methodIcon = getAttribute((Element)item,XML_ATTR_ICON);
				
				try{final Method	m = seekMethod(clazz,methodName);
						
					final MutableContentNodeMetadata	metadata = new MutableContentNodeMetadata(methodAction
															, ActionEvent.class
															, m.getName()+"/"+methodAction
															, null
															, methodLabel == null ? "?" : methodLabel 
															, methodTooltip
															, methodHelp
															, null
															, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION+":/"+clazz.getSimpleName()+"/"+m.getName()+"()."+methodAction)
															, methodIcon == null ? null : URI.create(methodIcon)
											);
					rootClass.addChild(metadata);
					metadata.setParent(rootClass);
				} catch (NoSuchMethodException exc) {
					throw new EnvironmentException("Error preparing xml metadata: class ["+clazz.getCanonicalName()+"] doesn't contain method ["+methodName+"()]");
				}
			}
		}
	}
	
	private static Method seekMethod(final Class<?> clazz, final String methodName) throws NoSuchMethodException {
		if (clazz != null) {
			for (Method item : clazz.getDeclaredMethods()) {
				if (item.getParameterCount() == 0 && methodName.equals(item.getName())) {
					return item;
				}
			}
			return seekMethod(clazz.getSuperclass(),methodName);
		}
		else {
			throw new NoSuchMethodException("Method ["+methodName+"()] is missing in the class");
		}
	}

	private static void buildSubtree(final Element document, final MutableContentNodeMetadata node) throws EnvironmentException {
		MutableContentNodeMetadata	child;
		
		switch (document.getTagName()) {
			case XML_TAG_APP_MENU		:
				final String	menuId = getAttribute(document,XML_ATTR_ID);
				final String	keySet = getAttribute(document,XML_ATTR_KEYSET);
				final String	menuIcon = getAttribute(document,XML_ATTR_ICON);
				
				child = new MutableContentNodeMetadata(menuId
						, String.class
						, Constants.MODEL_NAVIGATION_TOP_PREFIX+'.'+menuId+(keySet == null ? "" : "?keyset="+keySet)
						, null
						, menuId
						, null 
						, null
						, null
						, null
						, menuIcon == null || menuIcon.isEmpty() ? null : URI.create(menuIcon));
				break;
			case XML_TAG_APP_SUBMENU	:
				final String	submenuName = getAttribute(document,XML_ATTR_NAME);
				final String	submenuCaption = getAttribute(document,XML_ATTR_CAPTION);
				final String	submenuTooltip = getAttribute(document,XML_ATTR_TOOLTIP);
				final String	submenuIcon = getAttribute(document,XML_ATTR_ICON);
				
				child = new MutableContentNodeMetadata(submenuName
						, String.class
						, Constants.MODEL_NAVIGATION_NODE_PREFIX+'.'+submenuName
						, null
						, submenuCaption
						, submenuTooltip 
						, null
						, null
						, null
						, submenuIcon == null || submenuIcon.isEmpty() ? null : URI.create(submenuIcon));
				break;
			case XML_TAG_APP_SUBMENU_REF	:
				final String	submenuRefName = getAttribute(document,XML_ATTR_NAME);
				final String	submenuRef = getAttribute(document,XML_ATTR_REF);
				
				child = new MutableContentNodeMetadata(submenuRefName
						, String.class
						, Constants.MODEL_NAVIGATION_NODE_PREFIX+'.'+submenuRefName
						, null
						, submenuRef
						, null 
						, null
						, null
						, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_REF+":/"+submenuRef)
						, null);
				break;
			case XML_TAG_APP_ITEM		:
				final String	itemName = getAttribute(document,XML_ATTR_NAME);
				final String	itemCaption = getAttribute(document,XML_ATTR_CAPTION);
				final String	itemTooltip = getAttribute(document,XML_ATTR_TOOLTIP);
				final String	itemHelp = getAttribute(document,XML_ATTR_HELP);
				final String	itemAction = getAttribute(document,XML_ATTR_ACTION);
				final String	groupAction = getAttribute(document,XML_ATTR_GROUP);
				final String	itemIcon = getAttribute(document,XML_ATTR_ICON);
				
				child = new MutableContentNodeMetadata(itemName
						, String.class
						, Constants.MODEL_NAVIGATION_LEAF_PREFIX+'.'+itemName
						, null
						, itemCaption
						, itemTooltip 
						, itemHelp
						, null
						, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION+":/"+itemAction+(groupAction != null ? "#"+groupAction : ""))
						, itemIcon == null || itemIcon.isEmpty() ? null : URI.create(itemIcon));
				break;
			case XML_TAG_APP_ITEM_REF	:
				final String	itemRefName = getAttribute(document,XML_ATTR_NAME);
				final String	itemRef = getAttribute(document,XML_ATTR_REF);
				
				child = new MutableContentNodeMetadata(itemRefName
						, String.class
						, Constants.MODEL_NAVIGATION_LEAF_PREFIX+"."+itemRefName
						, null
						, itemRef
						, null 
						, null
						, null
						, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_REF+":/"+itemRef)
						, null);
				break;
			case XML_TAG_APP_SEPARATOR	:
				child = new MutableContentNodeMetadata("_"
						, String.class
						, Constants.MODEL_NAVIGATION_SEPARATOR
						, null
						, "_"
						, null 
						, null
						, null
						, null
						, null);
				break;
			case XML_TAG_APP_PLACEHOLDER	:
				child = null;
				break;
			case XML_TAG_APP_BUILTIN_SUBMENU	:
				final String	builtinName = getAttribute(document,XML_ATTR_NAME);
				final String	builtinCaption = getAttribute(document,XML_ATTR_CAPTION);
				final String	builtinTooltip = getAttribute(document,XML_ATTR_TOOLTIP);
				final String	builtinAction = getAttribute(document,XML_ATTR_ACTION);
				
				if (!Constants.MODEL_AVAILABLE_BUILTINS.contains(builtinName)) {
					throw new EnvironmentException("Illegal name ["+builtinName+"] for built-in navigation. Available names are "+Constants.MODEL_AVAILABLE_BUILTINS);						
				}
				else {
					child = new MutableContentNodeMetadata(builtinName
							, String.class
							, Constants.MODEL_NAVIGATION_NODE_PREFIX+'.'+builtinName
							, null
							, builtinCaption
							, builtinTooltip 
							, null
							, null
							, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION+":"+Constants.MODEL_APPLICATION_SCHEME_BUILTIN_ACTION+":/"
										+(builtinAction == null ? builtinName : builtinAction)
							  )
							, null);
				}
				break;
			case XML_TAG_APP_KEYSET	:
				final String	keysetName = getAttribute(document,XML_ATTR_ID);
				
				child = new MutableContentNodeMetadata(keysetName
						, String.class
						, Constants.MODEL_NAVIGATION_KEYSET_PREFIX+'.'+keysetName
						, null
						, keysetName 
						, null
						, null
						, null
						, null
						, null);
				break;
			case XML_TAG_APP_KEY	:
				final String	keyName = getAttribute(document,XML_ATTR_ID);
				final String	keyAction = getAttribute(document,XML_ATTR_ACTION);
				final String	keyCode = getAttribute(document,"code");
				final String	keyCtrl = getAttribute(document,"ctrl");
				final String	keyShift = getAttribute(document,"shift");
				final String	keyAlt = getAttribute(document,"alt");
				final String	keyLabel = (keyCtrl != null || "true".equals(keyCtrl) ? "ctrl " : "")+(keyShift != null || "true".equals(keyShift) ? "shift " : "")+(keyAlt != null || "true".equals(keyAlt) ? "alt " : "") + keyCode; 
				
				child = new MutableContentNodeMetadata(keyName == null ? keyLabel : keyName
						, String.class
						, Constants.MODEL_KEYSET_KEY_PREFIX+'.'+(keyName == null ? keyLabel.replace(' ','_') : keyName)
						, null
						, keyLabel
						, null 
						, null
						, null
						, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION+":/"+keyAction)
						, null);
				break;
			case XML_TAG_APP_ROOT	:	// Top level
				for (int index = 0, maxIndex = ((NodeList)document).getLength(); index < maxIndex; index++) {
					if (((NodeList)document).item(index) instanceof Element) {
						buildSubtree((Element) ((NodeList)document).item(index),node);
					}
				}
				return;
			case XML_TAG_APP_I18N	:	// Was processed at the top
				return;
			default :
				throw new UnsupportedOperationException("Tag ["+document.getTagName()+"] is not supported yet"); 
		}
		if (child != null) {
			for (int index = 0, maxIndex = ((NodeList)document).getLength(); index < maxIndex; index++) {
				if (((NodeList)document).item(index) instanceof Element) {
					buildSubtree((Element) ((NodeList)document).item(index),child);
				}
			}
			node.addChild(child);
			child.setParent(node);
		}
	}

	private static String getAttribute(final Element document, final String attribute) {
		final Attr 	attr = document.getAttributeNode(attribute);
		
		return attr == null ? null : attr.getValue();
	}

	private static void collectFields(final Class<?> clazz, final List<Field> fields, final boolean annotatedOnly) {
		if (clazz != null) {
			for (Field f : clazz.getDeclaredFields()) {
				if (!annotatedOnly || f.isAnnotationPresent(LocaleResource.class) || f.isAnnotationPresent(Format.class)) {
					fields.add(f);
				}
			}
			collectFields(clazz.getSuperclass(),fields,annotatedOnly);
		}		
	}

	private static void collectMethods(final Class<?> clazz, final List<Method> methods, final boolean annotatedOnly) {
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
				else if (!annotatedOnly) {
					methods.add(m);
				}
			}
			collectMethods(clazz.getSuperclass(),methods,annotatedOnly);
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
											, null
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
										, null
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
										, URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION+":/"+clazz.getSimpleName()+"/"+method.getName()+"()."+item.actionString())
										, null
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

	private static String buildColumnFormat(final ResultSetMetaData rsmd,final int columnIndex) throws SQLException {
		final StringBuilder	sb = new StringBuilder();
		
		if (rsmd.getScale(columnIndex) != 0) {
			if (rsmd.getPrecision(columnIndex) == 0) {
				sb.append("18.").append(rsmd.getScale(columnIndex));
			}
			else {
				sb.append(rsmd.getPrecision(columnIndex)).append('.').append(rsmd.getScale(columnIndex));
			}
		}
		else {
			if (rsmd.getPrecision(columnIndex) == 0) {
				sb.append("30");
			}
			else if (rsmd.getPrecision(columnIndex) > 50) {
				sb.append("30");
			}
			else {
				sb.append(rsmd.getPrecision(columnIndex));
			}
		}
		if (rsmd.isNullable(columnIndex) == ResultSetMetaData.columnNoNulls) {
			sb.append("m");
		}
		return sb.toString();
	}
	
	private static ContentNodeMetadata buildSPIPluginMenuSubtree(final ContentNodeMetadata root, final String path) {
		if (path.isEmpty()) {
			return root;
		}
		else {
			final int		slashIndex = path.indexOf('/');
			final String	prefix = slashIndex == -1 ? path : path.substring(0,slashIndex), suffix = slashIndex == -1 ? "" : path.substring(slashIndex+1);
			
			for (ContentNodeMetadata item : root) {
				if (prefix.equals(item.getName())) {
					return buildSPIPluginMenuSubtree(item,suffix);
				}				
			}
			
			final MutableContentNodeMetadata	child = new MutableContentNodeMetadata(prefix
													, String.class
													, Constants.MODEL_NAVIGATION_NODE_PREFIX+'.'+prefix
													, null
													, prefix
													, null 
													, null
													, null
													, null
													, null);
			((MutableContentNodeMetadata)root).addChild(child);
			child.setParent(root);
			
			return buildSPIPluginMenuSubtree(root,path);
		}
	}

	private static String escapeBrackets(final String canonicalName) {
		if (canonicalName.indexOf('[') < 0) {
			return canonicalName;
		}
		else {
			return canonicalName.replace("[","%5B").replace("]","%5D");
		}
	}
}
