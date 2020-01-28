package chav1961.purelib.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
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
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.model.ModelUtils.ModelComparisonCallback.DifferenceLocalization;
import chav1961.purelib.model.ModelUtils.ModelComparisonCallback.DifferenceType;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.ORMSerializer;
import chav1961.purelib.streams.char2byte.AsmWriter;


/**
 * <p>This utility class supports most of useful operations with models.</p>
 * @see chav1961.purelib.model.interfaces
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class ModelUtils {
	private static final AsmWriter		writer;
	private static final IOException	initExc;
	
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
			IN_TYPE, IN_UI_PATH, IN_LOCALIZER, IN_LABEL, IN_TOOLTIP, IN_HELP, IN_FORMAT, IN_APP_PATH
		}
		
		ContinueMode difference(ContentNodeMetadata left, ContentNodeMetadata right, DifferenceType diffType, Set<DifferenceLocalization> details);
	}
	
	public static <Key,Content> ORMSerializer<Key,Content> buildORMSerializer(final ContentMetadataInterface metadata, final ClassLoader deploy) {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			// TODO:
			return null;
		}
	}

	public static <Key,Content> ORMSerializer<Key,Content> buildResultsetSerializer(final ContentMetadataInterface metadata, final Class<Key> keyClass, final Class<Content> contentClass, final ClassLoader deploy) {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (keyClass == null) {
			throw new NullPointerException("Key class can't be null");
		}
		else if (contentClass == null) {
			throw new NullPointerException("Content class can't be null");
		}
		else if (deploy == null) {
			throw new NullPointerException("Loader to deploy to can't be null");
		}
		else if (initExc != null) {
			throw new PreparationException("Class ["+ModelUtils.class.getCanonicalName()+"]: error during static initialization: ("+initExc.getLocalizedMessage()+"). This class can't be used before solving problems",initExc);
		}
		else {
			// TODO:
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
				final Writer				wr = writer.clone(baos)) {
//				final int					suffix = uniqueSuffix.incrementAndGet();
				
				wr.write(" makeIncludes \""+keyClass.getCanonicalName()+"\",\""+keyClass.getCanonicalName()+"\",\"");
			} catch (IOException e) {
			}
			return null;
		}
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
				((ObjectGetterAndSetter<Object>)gas).set(instance,value);
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
				else if (!(value instanceof Byte)) {
					throw new IllegalArgumentException("Value to assign to primitive byte type must be [Byte], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((ByteGetterAndSetter)gas).set(instance,((Byte)value).byteValue());
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
				else if (!(value instanceof Short)) {
					throw new IllegalArgumentException("Value to assign to primitive short type must be [Short], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((ShortGetterAndSetter)gas).set(instance,((Short)value).shortValue());
				}
			}
			else if (gas instanceof IntGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Integer)) {
					throw new IllegalArgumentException("Value to assign to primitive int type must be [Integer], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((IntGetterAndSetter)gas).set(instance,((Integer)value).intValue());
				}
			}
			else if (gas instanceof LongGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Long)) {
					throw new IllegalArgumentException("Value to assign to primitive int type must be [Long], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((LongGetterAndSetter)gas).set(instance,((Long)value).longValue());
				}
			}
			else if (gas instanceof FloatGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Float)) {
					throw new IllegalArgumentException("Value to assign to primitive float type must be [Float], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((FloatGetterAndSetter)gas).set(instance,((Float)value).floatValue());
				}
			}
			else if (gas instanceof DoubleGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Double)) {
					throw new IllegalArgumentException("Value to assign to primitive double type must be [Double], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((DoubleGetterAndSetter)gas).set(instance,((Double)value).doubleValue());
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
	
	
	public static ContentNodeMetadata clone(final ContentNodeMetadata source) {
		if (source == null) {
			throw new NullPointerException("SOurce node to clone can't be null");
		}
		else {
			return innerClone(source);
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

	public static void compare(final ContentNodeMetadata left, final ContentNodeMetadata right, final ModelComparisonCallback callback) {
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
			innerCompare(left,right,callback,new HashSet<>(),new HashSet<>());
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
			if (!left.getLabelId().equals(right.getLabelId())) {
				details.add(DifferenceLocalization.IN_LABEL);
			}
			if (!left.getLocalizerAssociated().equals(right.getLocalizerAssociated())) {
				details.add(DifferenceLocalization.IN_LOCALIZER);
			}
			if (!left.getApplicationPath().equals(right.getApplicationPath())) {
				details.add(DifferenceLocalization.IN_APP_PATH);
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
