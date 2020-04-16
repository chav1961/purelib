package chav1961.purelib.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.BooleanGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ByteGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.CharGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.DoubleGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.FloatGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ShortGetterAndSetter;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.model.ModelUtils.ModelComparisonCallback.DifferenceLocalization;
import chav1961.purelib.model.ModelUtils.ModelComparisonCallback.DifferenceType;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.char2byte.AsmWriter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;


/**
 * <p>This utility class supports most of useful operations with models.</p>
 * @see chav1961.purelib.model.interfaces
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.4
 */
public class ModelUtils {
	private static final AsmWriter		writer;
	private static final IOException	initExc;
	
	public static final String			JSON_METADATA_VERSION = "version";
	public static final String			JSON_METADATA_VERSION_ID = "1.0";
	public static final String			JSON_METADATA_NAME = "name";
	public static final String			JSON_METADATA_TYPE = "type";
	public static final String			JSON_METADATA_LABEL_ID = "labelId";
	public static final String			JSON_METADATA_TOOLTIP_ID = "tooltipId";
	public static final String			JSON_METADATA_HELP_ID = "helpId";
	public static final String			JSON_METADATA_KEYWORDS = "keywords";
	public static final String			JSON_METADATA_ATTACHMENTS = "attachments";
	public static final String			JSON_METADATA_FORMAT = "format";
	public static final String			JSON_METADATA_APPLICATION_PATH = "appUri";
	public static final String			JSON_METADATA_RELATIVE_UI_PATH = "relUri";
	public static final String			JSON_METADATA_ICON = "icon";
	public static final String			JSON_METADATA_LOCALIZER = "localizer";
	
	private static final String[]		JSON_NAMES = {
											JSON_METADATA_VERSION,
											JSON_METADATA_NAME, 
											JSON_METADATA_TYPE, 
											JSON_METADATA_LABEL_ID, 
											JSON_METADATA_TOOLTIP_ID, 
											JSON_METADATA_HELP_ID, 
											JSON_METADATA_KEYWORDS, 
											JSON_METADATA_ATTACHMENTS, 
											JSON_METADATA_FORMAT, 
											JSON_METADATA_APPLICATION_PATH,
											JSON_METADATA_RELATIVE_UI_PATH,
											JSON_METADATA_ICON,
											JSON_METADATA_LOCALIZER
										};
	private static final String[]		JSON_MANDATORY_NAMES = {
											JSON_METADATA_VERSION,
											JSON_METADATA_NAME, 
											JSON_METADATA_TYPE, 
											JSON_METADATA_LABEL_ID, 
											JSON_METADATA_FORMAT, 
											JSON_METADATA_APPLICATION_PATH,
											JSON_METADATA_RELATIVE_UI_PATH
										};

	static {
		AsmWriter	temp;
		IOException	tempExc;
		
		try{temp = new AsmWriter(new OutputStream(){@Override public void write(int b) throws IOException {}},new OutputStreamWriter(System.err));
			try(final InputStream	is = ModelUtils.class.getResourceAsStream("macros.txt");
				final Reader		rdr = new InputStreamReader(is)) {
				
				Utils.copyStream(rdr, temp);
			}
			tempExc = null;
		} catch (IOException exc) {
			temp = null;
			tempExc = exc;
		}
		writer = temp;
		initExc = tempExc;
	}
	
	public interface ModelComparisonCallback {
		public enum DifferenceType {
			INSERTED, DELETED, CHANGED 
		}
		
		public enum DifferenceLocalization {
			IN_TYPE, IN_UI_PATH, IN_LOCALIZER, IN_LABEL, IN_TOOLTIP, IN_HELP, IN_FORMAT, IN_APP_PATH, IN_ICON
		}
		
		ContinueMode difference(ContentNodeMetadata left, ContentNodeMetadata right, DifferenceType diffType, Set<DifferenceLocalization> details);
	}
	
	/**
	 * <p>Convert model tree to tree text</p>
	 * @param metaData model tree to convert
	 * @return tree text converted. Can be empty but not null
	 * @throws NullPointerException when metadata is null
	 */
	public static String toString(final ContentNodeMetadata metaData) {
		if (metaData == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			
			toString("",metaData,sb);
			return sb.toString();
		}
	}
	
	public static URI buildUriByClassAndField(final Class<?> clazz, final String fieldName) throws IllegalArgumentException, NullPointerException {
		if (clazz == null) {
			throw new NullPointerException("Class to build URI for can't be null");
		}
		else if (fieldName == null || fieldName.isEmpty()) {
			throw new IllegalArgumentException("Field name to build URI for can't be null or empty");
		}
		else {
			Class<?>	temp = clazz;
			
			while (temp != null) {
				for (Field f : temp.getDeclaredFields()) {
					if (f.getName().equals(fieldName)) {
						return ContentModelFactory.buildClassFieldApplicationURI(clazz,f);					
					}
				}
				temp = temp.getSuperclass();
			}
			throw new IllegalArgumentException("Field name ["+fieldName+"] not found in the class ["+clazz.getCanonicalName()+"]");
		}
	}

	public static URI buildUriByClassAndMethod(final Class<?> clazz, final String methodName, final Class<?>... parameters) {
		if (clazz == null) {
			throw new NullPointerException("Class to build URI for can't be null");
		}
		else if (methodName == null || methodName.isEmpty()) {
			throw new IllegalArgumentException("Method name to build URI for can't be null or empty");
		}
		else if (parameters == null) {
			throw new IllegalArgumentException("Parameter's list can' be null");
		}
		else if (Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Null inside parameters at position ["+Utils.checkArrayContent4Nulls(parameters)+"]");
		}
		else {
			Class<?>	temp = clazz;
			
			while (temp != null) {
				for (Method m : temp.getDeclaredMethods()) {
					if (m.getName().equals(methodName) && Arrays.deepEquals(m.getParameterTypes(),parameters)) {
						return ContentModelFactory.buildClassMethodApplicationURI(clazz,m.getName());					
					}
				}
				temp = temp.getSuperclass();
			}
			throw new IllegalArgumentException("Method name ["+methodName+"] not found in the class ["+clazz.getCanonicalName()+"]");
		}
	}

	public static URI buildUriByTableAndColumn(final String table, final String column) {
		return null;
	}
	
	
	/**
	 * <p>Get value from instance by getters and setters</p>
	 * @param instance instance to get value from
	 * @param gas getter and setter to access to instance field
	 * @param metadata mode meta data for the given getter and setter
	 * @return value extracted
	 * @throws NullPointerException if any parameters are null
	 * @throws ContentException on any errors while getting value
	 */
	public static Object getValueByGetter(final Object instance, final GetterAndSetter gas, final ContentNodeMetadata metadata) throws NullPointerException, ContentException {
		if (instance == null) {
			throw new NullPointerException("Object to get value from can't be null");
		}
		else if (gas == null) {
			throw new NullPointerException("Getter&setter can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			if (gas instanceof ObjectGetterAndSetter<?>) {
				return ((ObjectGetterAndSetter<?>)gas).get(instance);
			}
			else if (gas instanceof BooleanGetterAndSetter) {
				return Boolean.valueOf(((BooleanGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof ByteGetterAndSetter) {
				return Byte.valueOf(((ByteGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof CharGetterAndSetter) {
				return Character.valueOf(((CharGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof ShortGetterAndSetter) {
				return Short.valueOf(((ShortGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof IntGetterAndSetter) {
				return Integer.valueOf(((IntGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof LongGetterAndSetter) {
				return Long.valueOf(((LongGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof FloatGetterAndSetter) {
				return Float.valueOf(((FloatGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof DoubleGetterAndSetter) {
				return Double.valueOf(((DoubleGetterAndSetter)gas).get(instance));
			}
			else {
				throw new UnsupportedOperationException("Getter&setter type ["+gas.getClass().getSimpleName()+"] is not supported yet");
			}
		}
	}

	/**
	 * <p>Get value from instance by it's application URI</p>
	 * @param instance instance to get value from
	 * @param applicationURI URI of the field to extract
	 * @param metadata mode meta data for the given getter and setter
	 * @return value extracted
	 * @throws NullPointerException if any parameters are null
	 * @throws ContentException on any errors while getting value
	 */
	public static Object getValueByGetter(final Object instance, final URI applicationURI, final ContentNodeMetadata metadata) throws NullPointerException, ContentException {
		if (instance == null) {
			throw new NullPointerException("Object to get value from can't be null");
		}
		else if (applicationURI == null) {
			throw new NullPointerException("Application URI can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			return getValueByGetter(instance,GettersAndSettersFactory.buildGetterAndSetter(applicationURI,Thread.currentThread().getContextClassLoader()),metadata);
		}
	}	
	
	/**
	 * <p>Store value into instance by getters and setters</p>
	 * @param instance instance to store value to
	 * @param value value to store
	 * @param gas getter and setter to access to instance field
	 * @param metadata mode meta data for the given getter and setter
	 * @throws NullPointerException if any parameters are null
	 * @throws IllegalArgumentException if value to store is incompatible with target setter 
	 * @throws ContentException on any errors while setting value
	 */
	
	public static void setValueBySetter(final Object instance, final Object value, final GetterAndSetter gas, final ContentNodeMetadata metadata) throws ContentException, NullPointerException, IllegalArgumentException {
		if (instance == null) {
			throw new NullPointerException("Object to get value from can't be null");
		}
		else if (gas == null) {
			throw new NullPointerException("Getter&setter can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			if (gas instanceof ObjectGetterAndSetter) {
				@SuppressWarnings("unchecked")
				final ObjectGetterAndSetter<Object>	setter = (ObjectGetterAndSetter<Object>)gas;
				
				setter.set(instance,value);
			}
			else if (gas instanceof BooleanGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Boolean)) {
					throw new IllegalArgumentException("Value to assign to primitive boolean type must be [Boolean], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((BooleanGetterAndSetter)gas).set(instance,((Boolean)value).booleanValue());
				}
			}
			else if (gas instanceof ByteGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Number)) {
					throw new IllegalArgumentException("Value to assign to primitive int type must be [Number], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((ByteGetterAndSetter)gas).set(instance,((Number)value).byteValue());
				}
			}
			else if (gas instanceof CharGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Character)) {
					throw new IllegalArgumentException("Value to assign to primitive char type must be [Character], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((CharGetterAndSetter)gas).set(instance,((Character)value).charValue());
				}
			}
			else if (gas instanceof ShortGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Number)) {
					throw new IllegalArgumentException("Value to assign to primitive int type must be [Number], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((ShortGetterAndSetter)gas).set(instance,((Number)value).shortValue());
				}
			}
			else if (gas instanceof IntGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Number)) {
					throw new IllegalArgumentException("Value to assign to primitive int type must be [Number], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((IntGetterAndSetter)gas).set(instance,((Number)value).intValue());
				}
			}
			else if (gas instanceof LongGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Number)) {
					throw new IllegalArgumentException("Value to assign to primitive int type must be [Number], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((LongGetterAndSetter)gas).set(instance,((Number)value).longValue());
				}
			}
			else if (gas instanceof FloatGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Number)) {
					throw new IllegalArgumentException("Value to assign to primitive int type must be [Number], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((FloatGetterAndSetter)gas).set(instance,((Number)value).floatValue());
				}
			}
			else if (gas instanceof DoubleGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Number)) {
					throw new IllegalArgumentException("Value to assign to primitive int type must be [Number], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((DoubleGetterAndSetter)gas).set(instance,((Number)value).doubleValue());
				}
			}
			else {
				throw new UnsupportedOperationException("Getter&setter type ["+gas.getClass().getSimpleName()+"] is not supported yet");
			}
		}
	}

	/**
	 * <p>Store value into instance by application URI</p>
	 * @param instance instance to store value to
	 * @param value value to store
	 * @param applicationURI URI of the field to store
	 * @param metadata mode meta data for the given getter and setter
	 * @throws NullPointerException if any parameters are null
	 * @throws IllegalArgumentException if value to store is incompatible with target setter 
	 * @throws ContentException on any errors while setting value
	 */
	public static void setValueBySetter(final Object instance, final Object value, final URI applicationURI, final ContentNodeMetadata metadata) throws NullPointerException, ContentException {
		if (instance == null) {
			throw new NullPointerException("Object to get value from can't be null");
		}
		else if (applicationURI == null) {
			throw new NullPointerException("Application URI can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			setValueBySetter(instance,value,GettersAndSettersFactory.buildGetterAndSetter(applicationURI,Thread.currentThread().getContextClassLoader()),metadata);
		}
	}	
	
	/**
	 * <p>Clone metadata node</p>
	 * @param source node to clone
	 * @return node cloned. Can't be null
	 * @throws NullPointerException when node to clone is null
	 * @since 0.0.4
	 */
	public static ContentNodeMetadata clone(final ContentNodeMetadata source) throws NullPointerException {
		if (source == null) {
			throw new NullPointerException("SOurce node to clone can't be null");
		}
		else {
			return innerClone(source);
		}
	}
	
	/**
	 * <p>Serialize metadata node to JSON format</p>
	 * @param metadata node to serialize
	 * @param printer JSON printer to serialize node to
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException when any parameter is null
	 * @since 0.0.4
	 */
	public static void serializeToJson(final ContentNodeMetadata metadata, final JsonStaxPrinter printer) throws NullPointerException, IOException {
		if (metadata == null) {
			throw new NullPointerException("Metadata to serialize can't be null"); 
		}
		else if (printer == null) {
			throw new NullPointerException("Printer to serialize to can't be null"); 
		}
		else {
			printer.startObject();
				printer.name(JSON_METADATA_VERSION).value(JSON_METADATA_VERSION_ID);
				printer.splitter().name(JSON_METADATA_NAME).value(metadata.getName());
				printer.splitter().name(JSON_METADATA_TYPE).value(metadata.getType().getCanonicalName());
				printer.splitter().name(JSON_METADATA_LABEL_ID).value(metadata.getLabelId());
				if (metadata.getTooltipId() != null) {
					printer.splitter().name(JSON_METADATA_TOOLTIP_ID).value(metadata.getTooltipId());
				}
				if (metadata.getHelpId() != null) {
					printer.splitter().name(JSON_METADATA_HELP_ID).value(metadata.getHelpId());
				}
				if (metadata.getKeywords() != null && metadata.getKeywords().length > 0) {
					boolean	splitterRequired = false;
					
					printer.splitter().name(JSON_METADATA_KEYWORDS).startArray();
					for (String item : metadata.getKeywords()) {
						if (splitterRequired) {
							printer.splitter();
						}
						if (item == null) {
							printer.nullValue();
						}
						else {
							printer.value(item);
						}
						splitterRequired = true;
					}
					printer.endArray();				
				}
				if (metadata.getAttachments() != null && metadata.getAttachments().length > 0) {
					boolean	splitterRequired = false;
					
					printer.splitter().name(JSON_METADATA_ATTACHMENTS).startArray();
					for (String item : metadata.getAttachments()) {
						if (splitterRequired) {
							printer.splitter();
						}
						if (item == null) {
							printer.nullValue();
						}
						else {
							printer.value(item);
						}
						splitterRequired = true;
					}
					printer.endArray();				
				}
				printer.splitter().name(JSON_METADATA_FORMAT).value(metadata.getFormatAssociated().toFormatString());
				if (metadata.getApplicationPath() != null) {
					printer.splitter().name(JSON_METADATA_APPLICATION_PATH).value(metadata.getApplicationPath().toString());
				}
				printer.splitter().name(JSON_METADATA_RELATIVE_UI_PATH).value(metadata.getRelativeUIPath().toString());
				if (metadata.getLocalizerAssociated() != null) {
					printer.splitter().name(JSON_METADATA_LOCALIZER).value(metadata.getLocalizerAssociated().toString());
				}
				if (metadata.getIcon() != null) {
					printer.splitter().name(JSON_METADATA_ICON).value(metadata.getIcon().toString());
				}
			printer.endObject();
		}
	}

	/**
	 * <p>Deserialize content metadata from JSON</p>
	 * @param parser parser to deserialize node from
	 * @return node deserialized. Can't be null
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException when any parameter is null
	 * @since 0.0.4
	 */
	public static MutableContentNodeMetadata deserializeFromJson(final JsonStaxParser parser) throws IOException {
		return deserializeFromJson(parser,Thread.currentThread().getContextClassLoader());
	}	
	
	/**
	 * <p>Deserialize content metadata from JSON</p>
	 * @param parser parser to deserialize node from
	 * @param loader class loader to seek node type in
	 * @return node deserialized. Can't be null
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException when any parameter is null
	 * @since 0.0.4
	 */
	public static MutableContentNodeMetadata deserializeFromJson(final JsonStaxParser parser, final ClassLoader loader) throws IOException {
		if (parser == null) {
			throw new NullPointerException("Parser to deserialize from can't be null"); 
		}
		else if (loader == null) {
			throw new NullPointerException("Class laoder to deserialize with can't be null"); 
		}
		else {
			final Map<String,Object>	pairs = new HashMap<>();
			List<String>				values = null;
			String						name = null;
			int							currentState = 0;
			
			for (String item : JSON_NAMES) {
				pairs.put(item,null);
			}
			
loop:		for(;;) {
				final JsonStaxParserLexType 	lex = parser.current();
				
				switch (currentState) {
					case 0 :	// before '{'
						if (lex == JsonStaxParserLexType.START_OBJECT) {
							currentState = 1;
							break;
						}
						else {
							throw new IOException(new SyntaxException(parser.row(),parser.col(),"Missing '{' before content node descriptor"));
						}
					case 1 :	// Name awaited
						if (lex == JsonStaxParserLexType.NAME) {
							name = parser.name();
							if (!pairs.containsKey(name)) {
								throw new IOException(new SyntaxException(parser.row(),parser.col(),"Unsupported name ["+name+"] in the content node descriptor"));
							}
							currentState = 2;
							break;
						}
						else {
							throw new IOException(new SyntaxException(parser.row(),parser.col(),"Unwaited name"));
						}
					case 2 :	// Name splitter awaited
						if (lex == JsonStaxParserLexType.NAME_SPLITTER) {
							currentState = 3;
							break;
						}
						else {
							throw new IOException(new SyntaxException(parser.row(),parser.col(),"Name splitter ':' is missing"));
						}
					case 3 :	// Value awaited
						switch (lex) {
							case BOOLEAN_VALUE	:
								pairs.put(name,parser.booleanValue());
								break;
							case INTEGER_VALUE	:
								pairs.put(name,parser.intValue());
								break;
							case REAL_VALUE		:
								pairs.put(name,parser.realValue());
								break;
							case STRING_VALUE	:
								pairs.put(name,parser.stringValue());
								break;
							case NULL_VALUE		:
								break;
							case START_ARRAY	:
								if (JSON_METADATA_KEYWORDS.contentEquals(name) || JSON_METADATA_ATTACHMENTS.contentEquals(name)) {
									if (values == null) {
										values = new ArrayList<>();
									}
									else {
										values.clear();
									}
									currentState = 5;
									continue;
								}
							default :
								throw new IOException(new SyntaxException(parser.row(),parser.col(),"Missing value for name"));
						}
						currentState = 4;
						break;
					case 4 :	// ',' awaited
						if (lex == JsonStaxParserLexType.LIST_SPLITTER) {
							currentState = 1;
						}
						else if (lex == JsonStaxParserLexType.END_OBJECT) {
							break loop;
						}
						else {
							throw new IOException(new SyntaxException(parser.row(),parser.col(),"Neither ',' nor '}' found"));
						}
						break;
					case 5 :	// Strings or nulls inside array
						switch (lex) {
							case END_ARRAY		:
								pairs.put(name,values.toArray(new String[values.size()]));
								currentState = 4;
								break;
							case NULL_VALUE		:
								values.add(null);
								break;
							case STRING_VALUE	:
								values.add(parser.stringValue());
								break;
							default:
								break;
						}
					default :
						throw new UnsupportedOperationException("Internal error");
				}
				if (parser.hasNext()) {
					parser.next();
				}
			}
			if (parser.current() == JsonStaxParserLexType.END_OBJECT && parser.hasNext()) {
				parser.next();
			}
			
			if (pairs.containsKey(JSON_METADATA_VERSION) && pairs.get(JSON_METADATA_VERSION) != null) {
				switch (pairs.get(JSON_METADATA_VERSION).toString()) {
					case JSON_METADATA_VERSION_ID :
						List<String>	mandatories = null;
						
						for (String item : JSON_MANDATORY_NAMES) {
							if (pairs.get(item) == null) {
								if (mandatories == null) {
									mandatories = new ArrayList<>();
								}
								mandatories.add(item);
							}
						}
						if (mandatories != null) {
							throw new IOException(new SyntaxException(parser.row(),parser.col(),"Serialized content version ["+JSON_METADATA_VERSION+"]- some mandatory fields are missing: "+mandatories));
						}
						
						try{pairs.replace(JSON_METADATA_TYPE,loader.loadClass(pairs.get(JSON_METADATA_TYPE).toString()));
						} catch (ClassNotFoundException e) {
							throw new IOException(new SyntaxException(parser.row(),parser.col(),"Serialized content version ["+JSON_METADATA_VERSION+"], field ["+JSON_METADATA_TYPE+"]: class ["+pairs.get(JSON_METADATA_TYPE)+"] not found in the class loader passed"));
						}
						
						for (Entry<String, Object> item : pairs.entrySet().toArray(new Entry[pairs.size()])) {
							if (item.getValue() != null) {
								switch (item.getKey()) {
									case JSON_METADATA_FORMAT			:
										try{pairs.replace(JSON_METADATA_FORMAT,new FieldFormat((Class<?>)pairs.get(JSON_METADATA_TYPE),item.getValue().toString()));
										} catch (IllegalArgumentException exc) {
											throw new IOException(new SyntaxException(parser.row(),parser.col(),"Serialized content version ["+JSON_METADATA_VERSION+"], field ["+item.getKey()+"]: illegal field format ["+item.getValue()+"] - "+exc.getLocalizedMessage()));
										}
										break;
									case JSON_METADATA_APPLICATION_PATH	:
									case JSON_METADATA_ICON				:
									case JSON_METADATA_LOCALIZER		:
										try {pairs.replace(item.getKey(),URI.create(item.getValue().toString()));											
										} catch (IllegalArgumentException exc) {
											throw new IOException(new SyntaxException(parser.row(),parser.col(),"Serialized content version ["+JSON_METADATA_VERSION+"], field ["+item.getKey()+"]: illegal URI format ["+item.getValue()+"] - "+exc.getLocalizedMessage()));
										}
										break;
								}
							}
						}
						
						final MutableContentNodeMetadata	result = new MutableContentNodeMetadata(
																		(String)pairs.get(JSON_METADATA_NAME), 
																		(Class<?>)pairs.get(JSON_METADATA_TYPE),
																		(String)pairs.get(JSON_METADATA_RELATIVE_UI_PATH), 
																		(URI)pairs.get(JSON_METADATA_LOCALIZER),
																		(String)pairs.get(JSON_METADATA_LABEL_ID), 
																		(String)pairs.get(JSON_METADATA_TOOLTIP_ID), 
																		(String)pairs.get(JSON_METADATA_HELP_ID), 
																		(FieldFormat)pairs.get(JSON_METADATA_FORMAT), 
																		(URI)pairs.get(JSON_METADATA_APPLICATION_PATH),
																		(URI)pairs.get(JSON_METADATA_ICON)
																	);
		
						return result;						
					default : 
						throw new IOException(new SyntaxException(parser.row(),parser.col(),"Unsupported version ["+pairs.get(JSON_METADATA_VERSION)+"] of content node metadata serialization format"));
				}
			}
			else {
				throw new IOException(new SyntaxException(parser.row(),parser.col(),"Missing mandatory field ["+JSON_METADATA_VERSION+"] in the serialization content"));
			}
		}
	}
	
	private static ContentNodeMetadata innerClone(final ContentNodeMetadata source) {
		final MutableContentNodeMetadata	result = new MutableContentNodeMetadata(source.getName(),
													source.getType(),
													source.getRelativeUIPath().toString(),
													source.getLocalizerAssociated(),
													source.getLabelId(),
													source.getTooltipId(),
													source.getHelpId(),
													source.getFormatAssociated(),
													source.getApplicationPath(),
													source.getIcon());
		for (ContentNodeMetadata item : source) {
			result.addChild(innerClone(item));
		}
		return result;
	}

	/**
	 * <p>Compare two nodes item-by item and process callback on changes</p>
	 * @param left let node to compare
	 * @param right right node to compare
	 * @param callback callback to process changes detected 
	 * @return true if no changes were detected or all detected changes are accepted. Changes are accepted when callback always returned {@linkplain ContinueMode#CONTINUE}.
	 * @throws NullPointerException if any argument is null
	 * @since 0.0.4
	 */
	public static boolean compare(final ContentNodeMetadata left, final ContentNodeMetadata right, final ModelComparisonCallback callback) {
		if (left == null) {
			throw new NullPointerException("Left node to compare can't be null");
		}
		else if (right == null) {
			throw new NullPointerException("Right node to compare can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback to compare can't be null");
		}
		else {
			return innerCompare(left,right,callback,new HashSet<>(),new HashSet<>());
		}
	}	
	
	private static boolean innerCompare(final ContentNodeMetadata left, final ContentNodeMetadata right, final ModelComparisonCallback callback, final Set<DifferenceLocalization> details, final Set<String> rightNames) {
		if (left != null && right == null) {
			details.clear();
			return callback.difference(left,right,DifferenceType.DELETED,details) == ContinueMode.CONTINUE; 
		}
		else if (left == null && right != null) {
			details.clear();
			return callback.difference(left,right,DifferenceType.INSERTED,details) == ContinueMode.CONTINUE; 
		}
		else if (left.getName().equals(right.getName())) {
			details.clear();
			if (!(left.getType().isAssignableFrom(right.getType()) || left.getType().isAssignableFrom(right.getType()))) {
				details.add(DifferenceLocalization.IN_TYPE);
			}
			if (!left.getRelativeUIPath().equals(right.getRelativeUIPath())) {
				details.add(DifferenceLocalization.IN_UI_PATH);
			}
			if (!Objects.equals(left.getLocalizerAssociated(),right.getLocalizerAssociated())) {
				details.add(DifferenceLocalization.IN_LOCALIZER);
			}
			if (!left.getLabelId().equals(right.getLabelId())) {
				details.add(DifferenceLocalization.IN_LABEL);
			}
			if (!Objects.equals(left.getTooltipId(),right.getTooltipId())) {
				details.add(DifferenceLocalization.IN_TOOLTIP);
			}
			if (!Objects.equals(left.getHelpId(),right.getHelpId())) {
				details.add(DifferenceLocalization.IN_HELP);
			}
			if (!Objects.equals(left.getFormatAssociated(),right.getFormatAssociated())) {
				details.add(DifferenceLocalization.IN_FORMAT);
			}
			if (!Objects.equals(left.getApplicationPath(),right.getApplicationPath())) {
				details.add(DifferenceLocalization.IN_APP_PATH);
			}
			if (!Objects.equals(left.getIcon(),right.getIcon())) {
				details.add(DifferenceLocalization.IN_APP_PATH);
			}
			if (callback.difference(left,right,DifferenceType.CHANGED,details) == ContinueMode.CONTINUE) {
				final Set<String>	leftNames = new HashSet<>();
				rightNames.clear();
				
				for (ContentNodeMetadata item : left) {
					leftNames.add(item.getName());
				}
				for (ContentNodeMetadata item : right) {
					rightNames.add(item.getName());
				}
				leftNames.retainAll(rightNames);
				
				for (ContentNodeMetadata item : left) {
					if (!leftNames.contains(item.getName())) {
						details.clear();
						if (callback.difference(left,null,DifferenceType.DELETED,details) != ContinueMode.CONTINUE) {
							return false;
						}
					}
				}
				
				for (ContentNodeMetadata item : right) {
					if (!leftNames.contains(item.getName())) {
						details.clear();
						if (callback.difference(null,right,DifferenceType.INSERTED,details) != ContinueMode.CONTINUE) {
							return false;
						}
					}
				}
				
				for (ContentNodeMetadata itemLeft : left) {
					if (leftNames.contains(itemLeft.getName())) {
						for (ContentNodeMetadata itemRight : right) {
							if (itemLeft.getName().equals(itemRight.getName())) {
								details.clear();
								if (!innerCompare(itemLeft,itemRight,callback,details,rightNames)) {
									return false;
								}
							}
						}
					}
				}
				return true;
			}
			else {
				return false;
			}
		}
		else {
			details.clear();
			return callback.difference(left,null,DifferenceType.DELETED,details) == ContinueMode.CONTINUE && callback.difference(null,right,DifferenceType.INSERTED,details) == ContinueMode.CONTINUE; 
		}
	}

	private static void toString(final String prefix, final ContentNodeMetadata node, final StringBuilder sb) {
		sb.append(prefix).append(node.getRelativeUIPath()).append('\n');
		sb.append(prefix).append('\t').append(node.getName()).append(", app=").append(node.getApplicationPath()).append('\n');
		for (ContentNodeMetadata item : node) {
			toString(prefix+'\t',item,sb);
		}
	}
}
