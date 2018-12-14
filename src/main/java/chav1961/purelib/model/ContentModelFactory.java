package chav1961.purelib.model;



import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.xsd.XSDConst;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ORMSerializer;
import chav1961.purelib.ui.interfacers.Format;
import chav1961.purelib.ui.SimpleContentMetadata;
import chav1961.purelib.ui.interfacers.Action;
import chav1961.purelib.ui.interfacers.MultiAction;

public class ContentModelFactory {
	public static final String		APPLICATION_SCHEME_CLASS = "class";	
	public static final String		APPLICATION_SCHEME_TABLE = "table";	
	public static final String		APPLICATION_SCHEME_FIELD = "field";	
	public static final String		APPLICATION_SCHEME_TOP = "top";	
	public static final String		APPLICATION_SCHEME_NAVIGATOR = "navigator";	
	public static final String		APPLICATION_SCHEME_ACTION = "action";	
	
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
			
			dbFactory.setValidating(true);
			dbFactory.setNamespaceAware(true);
			dbFactory.setAttribute(XSDConst.SCHEMA_LANGUAGE,"http://www.w3.org/2001/XMLSchema");
			dbFactory.setAttribute(XSDConst.SCHEMA_SOURCE,XSDConst.class.getResource("XMLDescribedApplication.xsd"));
			
			try{final DocumentBuilder 		dBuilder = dbFactory.newDocumentBuilder();
				final Document 				doc = dBuilder.parse(contentDescription);
				
				doc.getDocumentElement().normalize();
				
				final String						localizerResource = (String)xpath.compile("/app/i18n/@location").evaluate(doc,XPathConstants.STRING);
				final MutableContentNodeMetadata	root = new MutableContentNodeMetadata("root"
														, Document.class
														, "model"
														, URI.create(localizerResource)
														, "root"
														, null 
														, null
														, null
														, URI.create(APPLICATION_SCHEME_TOP+":/"));
				buildSubtree(doc.getDocumentElement(),root);
				return new SimpleContentMetadata(root);
			} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
				throw new PreparationException("Error preparing xml metadata: "+e.getLocalizedMessage(),e);
			}
		}
	}	

	public static ContentMetadataInterface forDBContentDescription(final DatabaseMetaData dbDescription, final String catalog, final String schema) throws NullPointerException, PreparationException {
		return null;
	}

	public static ContentMetadataInterface forDBTableDescription(final DatabaseMetaData dbDescription, final String catalog, final String schema, final String table) throws NullPointerException, PreparationException {
		return null;
	}

	public static ContentMetadataInterface forResultsetDescription(final ResultSetMetaData rsDescription) throws NullPointerException, ContentException {
		if (rsDescription == null) {
			throw new NullPointerException("Metadata description can't be null"); 
		}
		else {
			try{final String	forCatalog = rsDescription.getCatalogName(1) == null ? "" : "/"+rsDescription.getCatalogName(1);
				final String	forSchema = rsDescription.getSchemaName(1) == null ? "" : "/"+rsDescription.getSchemaName(1);
				
				final MutableContentNodeMetadata	root = new MutableContentNodeMetadata(rsDescription.getTableName(1)
														, Object.class
														, rsDescription.getTableName(1)
														, null
														, null
														, null 
														, null
														, null
														, URI.create(APPLICATION_SCHEME_TABLE+':'+forCatalog+forSchema+'/'+rsDescription.getTableName(1)));
				for (int index = 1; index <= rsDescription.getColumnCount(); index++) {
					final MutableContentNodeMetadata	item = new MutableContentNodeMetadata(rsDescription.getColumnName(index)
														, dbType2Class(rsDescription.getColumnType(index))
														, rsDescription.getColumnName(index)
														, null
														, null
														, null 
														, null
														, null
														, URI.create(APPLICATION_SCHEME_FIELD+':'+forCatalog+forSchema+'/'+rsDescription.getTableName(index)+'/'+rsDescription.getColumnName(index)));
					root.addChild(item);
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		}
		return null;
	}
	
	private static Class<?> dbType2Class(final int columnType) {
		switch (columnType) {
			case Types.BIT	: return boolean.class;
			case Types.TINYINT	: return byte.class;
			case Types.SMALLINT	: return short.class;
			case Types.INTEGER	: return int.class;
			case Types.BIGINT	: return long.class;
			case Types.FLOAT	: return float.class;
			case Types.REAL		: return double.class;
			case Types.DOUBLE	: return double.class;
			case Types.NUMERIC	: return BigDecimal.class;
			case Types.DECIMAL	: return BigDecimal.class;
			case Types.CHAR		: return String.class;
			case Types.VARCHAR	: return String.class;
			case Types.LONGVARCHAR	: return String.class;
			case Types.DATE	: return java.sql.Date.class;
			case Types.TIME	: return java.sql.Time.class;
			case Types.TIMESTAMP	: return java.sql.Timestamp.class;
			case Types.BINARY	: return byte[].class;
			case Types.VARBINARY	: return byte[].class;
			case Types.LONGVARBINARY	: return byte[].class;
			case Types.NULL	: return Object.class;
			case Types.OTHER	: return Object.class;
			case Types.JAVA_OBJECT	: return Object.class;
			case Types.DISTINCT	: return Object.class;
			case Types.STRUCT	: return java.sql.Struct.class;
			case Types.ARRAY	: return java.sql.Array.class;
			case Types.BLOB	: return java.sql.Blob.class;
			case Types.CLOB	:  return java.sql.Clob.class;
			case Types.REF	:  return java.sql.Ref.class;
			case Types.DATALINK	: return Object.class;
			case Types.BOOLEAN	: return boolean.class;
			case Types.ROWID	: return java.sql.RowId.class;
			case Types.NCHAR	: return String.class;
			case Types.NVARCHAR	: return String.class;
			case Types.LONGNVARCHAR	: return String.class;
			case Types.NCLOB	: return java.sql.NClob.class;
			case Types.SQLXML	: return java.sql.SQLXML.class;
			case Types.REF_CURSOR	: return java.sql.Ref.class;
			case Types.TIME_WITH_TIMEZONE	: return java.sql.Timestamp.class;
			case Types.TIMESTAMP_WITH_TIMEZONE	: return java.sql.Timestamp.class;
			default : return Object.class;
		}
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
		
		if (document.getNodeType() == Document.ENTITY_NODE) {
			switch (document.getTagName()) {
				case "menu" 			:
					final String	menuId = document.getAttributeNode("id").getValue();
					
					child = new MutableContentNodeMetadata(menuId
							, String.class
							, "navigation.top."+menuId
							, null
							, menuId
							, null 
							, null
							, null
							, null);
					break;
				case "submenu"			:
					final String	submenuName = document.getAttributeNode("name").getValue();
					final String	submenuCaption = document.getAttributeNode("caption").getValue();
					final String	submenuTooltip = document.getAttributeNode("tooltip").getValue();
					
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
				case "item"				:
					final String	itemName = document.getAttributeNode("name").getValue();
					final String	itemCaption = document.getAttributeNode("caption").getValue();
					final String	itemTooltip = document.getAttributeNode("tooltip").getValue();
					final String	itemAction = document.getAttributeNode("action").getValue();
					
					child = new MutableContentNodeMetadata(itemName
							, String.class
							, "navigation.leaf."+itemName
							, null
							, itemCaption
							, itemTooltip 
							, null
							, null
							, URI.create(APPLICATION_SCHEME_ACTION+":/"+itemAction));
					break;
//				case "separator"		:
//				case "builtinSubmenu"	:
				default :
					return;
			}
			for (int index = 0, maxIndex = ((NodeList)document).getLength(); index < maxIndex; index++) {
				buildSubtree((Element) ((NodeList)document).item(index),child);
			}
			node.addChild(child);
		}
	}


	static class ResultSetItrerator<Content> implements Iterable<Content> {
		private final URLClassLoader				loader = new URLClassLoader(new URL[0],Thread.currentThread().getContextClassLoader());
		private final ORMSerializer<Object,Content>	serializer;
		
		public ResultSetItrerator(final Class<Content> content, final ResultSet rs) {
			if (content == null) {
				throw new NullPointerException("Content class can't be null");
			}
			else if (rs == null) {
				throw new NullPointerException("Result set can't be null");
			}
			else {
				this.serializer = null;
				// TODO Auto-generated method stub
			}
		}

		@Override
		public Iterator<Content> iterator() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
