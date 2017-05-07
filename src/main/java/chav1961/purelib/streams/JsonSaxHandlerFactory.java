package chav1961.purelib.streams;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.interfaces.JsonSaxDeserializer;
import chav1961.purelib.streams.interfaces.JsonSaxHandler;

/**
 * <p>This Utility class allow you to build {@link JsonSaxHandler} implementation for automatic deserialization of any classes from JSON. 
 * This class is analyzing your class structure by reflection, and build appropriative handler to deserialize it, for example:</p>
 * <code>
 * final JsonSaxDeserializer&lt;MyClass&gt; h = JsonSaxHandlerFactory.buildDeserializer(MyClass.class,true);<br>
 * final JsonSaxParser p = new JsonSaxParser(h);<br>
 * p.parse(...);<br>
 * final MyClass deserialized = h.getInstance();<br>   
 * </code>
 * <p>The {@link JsonSaxHandler} implementation was built is a {@link JsonSaxDeserializer} interface implementation, which uses a list of
 * deserialization rules to process data:</p>
 * <ul>
 * <li>every JSON array reflects to appropriative array in the class you want to deserialise from JSON</li>
 * <li>every JSON object reflects to appropriative instance in the class you want to deserialise from JSON</li>
 * <li>every JSON name reflects to appropriative instance field with the same name in the class you want to deserialise from JSON</li>
 * </ul>
 * <p>Example of the rules:</p>
 * <code>
 * <b>{</b> --&gt; public class MyClass<br>
 * <b>"x" : 10,</b> --&gt; int x;	// x = 10<br>
 * <b>"y" : "test",</b> --&gt; String y;	// y = "test"<br>
 * <b>"z" : [10,20],</b> --&gt; int[] z;	// z = new int[]{10,20}<br>
 * <b>"t" : [30,40],</b> --&gt; double[] t;	// t = new double[]{30,40}<br>
 * <b>"a" : {</b> --&gt; public class MyInnerClass<br>
 * 		<b>"x" : true,</b> --&gt; boolean x; // x = true;<br>
 * 		<b>"y" : null</b> --&gt; Object y; // y = null;<br>
 * 		<b>},</b> --&gt; MyInnerClass a; // a = (see above)}<br>
 * <b>}</b> --&gt; }
 * </code>
 * <p>Static and transient fields are not included in the deserialization process</p>
 * 
 * <p><b>Pay attentiton</b>, that you can call {@link JsonSaxParser#parse(String)} or any other similar method more than once, but all sequential
 * calls will <b>refill</b> the same first deserialized instance was created in the first call. It allow you reduce memory requirements for the 
 * new deserialized instances. To avoid this functionality, always call {@link JsonSaxDeserializer#use(Object) JsonSaxDeserializer.use(null)} before
 * next parsing. You can also use this call to fill the <i>predefined</i> class instance with the deserialized data.</p>
 *     
 * <p>This class is thread-safe</p> 
 * 
 * @see <a href="http://www.rfc-base.org/rfc-7159.html">RFC 7159</a> 
 * @see chav1961.purelib.streams.interfaces.JsonSaxHandler
 * @see chav1961.purelib.streams.interfaces.JsonSaxDeserializer
 * @see chav1961.purelib.streams JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class JsonSaxHandlerFactory {
	private JsonSaxHandlerFactory(){}
	
	/**
	 * <p>Build {@link JsonSaxHandler} for automatic deserialization from JSON to the given class instance</p>
	 * @param <T> class returned
	 * @param clazz class, which instance you want to deserialize json to
	 * @param publicOnly use only public fields in the class for deserialization urposes
	 * @return JsonSaxDeserializer implementation to use in the constructor of the {@link JsonSaxParser}.
	 */
	public static <T> JsonSaxDeserializer<T> buildDeserializer(final Class<T> clazz, final boolean publicOnly) {
		final SyntaxTreeInterface<FieldDesc>	tree = new AndOrTree<FieldDesc>(1,1);
		final ClassDesc							desc = new ClassDesc(clazz,tree.placeName(clazz.getCanonicalName(),null));
		
		collectFieldNames(clazz,tree,publicOnly);
		buildClassTree(clazz,desc,publicOnly,tree);
		
		final int		treeDepth = calculateMaxTreeDepth(desc,0)+1;
		
		return new JsonSaxDeserializerImpl<T>(tree,desc,treeDepth);
	}
	
	private static void collectFieldNames(final Class<?> clazz,final SyntaxTreeInterface<FieldDesc> tree, final boolean publicOnly) {
		if (clazz != null) {
			if (clazz.isArray()) {
				collectFieldNames(clazz.getComponentType(),tree,publicOnly);
			}
			else if (publicOnly) {
				for (Field item : clazz.getFields()) {
					if (!Modifier.isStatic(item.getModifiers()) && !Modifier.isTransient(item.getModifiers())) {
						if (tree.seekName(item.getName()) >= 0) {
							tree.getCargo(tree.seekName(item.getName())).field.put(tree.placeName(clazz.getCanonicalName(),null),item);
						}
						else {
							final FieldDesc	desc = new FieldDesc();
							
							desc.field.put(tree.placeName(clazz.getCanonicalName(),null),item);
							tree.placeName(item.getName(),desc);
						}
						collectFieldNames(item.getType(),tree,publicOnly);
					}
				}
			}
			else {
				for (Field item : clazz.getDeclaredFields()) {
					if (!Modifier.isStatic(item.getModifiers()) && !Modifier.isTransient(item.getModifiers())) {
						if (tree.seekName(item.getName()) >= 0) {
							tree.getCargo(tree.seekName(item.getName())).field.put(tree.placeName(clazz.getCanonicalName(),null),item);
						}
						else {
							final FieldDesc	desc = new FieldDesc();
							
							desc.field.put(tree.placeName(clazz.getCanonicalName(),null),item);
							tree.placeName(item.getName(),desc);
						}
						collectFieldNames(item.getType(),tree,publicOnly);
					}
				}
				collectFieldNames(clazz.getSuperclass(),tree,publicOnly);
			}
		}
	}

	private static void buildClassTree(final Class<?> clazz, final ClassDesc desc, final boolean publicOnly, final SyntaxTreeInterface<?> tree) {
		if (clazz != null) {
			if (clazz.isArray()) {
				desc.arrayContent = new ClassDesc(clazz.getComponentType(),tree.placeName(clazz.getComponentType().getCanonicalName(),null),desc);
				buildClassTree(clazz.getComponentType(),desc.arrayContent,publicOnly,tree);
			}
			else if (publicOnly) {
				for (Field item : clazz.getFields()) {
					if (!Modifier.isStatic(item.getModifiers()) && !Modifier.isTransient(item.getModifiers())) {
						final ClassDesc	itemDesc = new ClassDesc(item.getType(),tree.placeName(clazz.getCanonicalName(),null),desc);
						
						desc.fields.put(tree.seekName(item.getName()),itemDesc);
						buildClassTree(item.getType(),itemDesc,publicOnly,tree);
					}
				}
			}
			else {
				for (Field item : clazz.getDeclaredFields()) {
					if (!Modifier.isStatic(item.getModifiers()) && !Modifier.isTransient(item.getModifiers())) {
						final ClassDesc	itemDesc = new ClassDesc(item.getType(),tree.placeName(clazz.getCanonicalName(),null),desc);
						
						desc.fields.put(tree.seekName(item.getName()),itemDesc);
						buildClassTree(item.getType(),itemDesc,publicOnly,tree);
					}
				}
				buildClassTree(clazz.getSuperclass(),desc,publicOnly,tree);
			}
		}
	}
	
	private static int calculateMaxTreeDepth(final ClassDesc desc, final int actualDepth) {
		if (desc.clazz.isArray()) {
			return calculateMaxTreeDepth(desc.arrayContent,actualDepth + 1);
		}
		else if (desc.clazz.isPrimitive()) {
			return actualDepth + 1;
		}
		else {
			int		level = actualDepth;
			
			for (ClassDesc item : desc.fields.values()) {
				final int	newDepth = calculateMaxTreeDepth(item,actualDepth+1);
				
				if (newDepth > level) {
					level = newDepth;
				}
			}
			return level;
		}
	}
	
	private static class FieldDesc {
		Map<Long,Field>		field = new HashMap<>(); 
	}
	
	private static class ClassDesc {
		private final Map<Long,ClassDesc>	fields = new HashMap<>();
		private final Class<?> 				clazz;
		private final long					clazzId;
		private ClassDesc					arrayContent;
		private ClassDesc					parent;
		
		ClassDesc(final Class<?> clazz, final long clazzId) {
			this.clazz = clazz;
			this.clazzId = clazzId;
		}

		ClassDesc(final Class<?> clazz, final long clazzId, final ClassDesc parent) {
			this.clazz = clazz;
			this.clazzId = clazzId;
			this.parent = parent;
		}

		@Override
		public String toString() {
			return "ClassDesc [fields=" + fields + ", clazz=" + clazz + ", arrayContent=" + arrayContent + "]";
		}
	}
	
	private static class JsonSaxDeserializerImpl<T> implements JsonSaxDeserializer<T> {
		private final SyntaxTreeInterface<FieldDesc>	tree;
		private final ClassDesc							desc;
		private final Object[]							objectStack;
		private final long[]							objectIds;
		
		private T				result = null;
		private ClassDesc		actualDesc;
		private int				stackLevel = -1;
		
		
		public JsonSaxDeserializerImpl(final SyntaxTreeInterface<FieldDesc> tree, final ClassDesc desc, final int stackDepth) {
			this.tree = tree;
			this.desc = desc;
			this.objectStack = new Object[stackDepth];
			this.objectIds = new long[stackDepth];
		}

		@Override
		public void startDoc() throws ContentException {
			actualDesc = desc;
		}

		@Override
		public void endDoc() throws ContentException {
			actualDesc = actualDesc.parent;
			result = (T)objectStack[stackLevel--];
			
			if (result.getClass() != desc.clazz) {
				if (desc.clazz.isArray()) {
					result = (T)toArray(desc.clazz.getComponentType(),(Object[]) result);
				}
				else {
					throw new ContentException("Desrialized class ["+result.getClass().getCanonicalName()+"] is not compatible with awaited class ["+desc.clazz.getCanonicalName()+"]");
				}
			}
		}

		@Override
		public void startObj() throws ContentException {
			try{if (actualDesc.arrayContent != null) {
					stackLevel = -1;
					throw new ContentException("Json and class description conflict: array waiting, but object started");
				}
				else {
					if (stackLevel == -1) {
						if (result != null) {
							objectStack[++stackLevel] = result; 
						}
						else {
							objectStack[++stackLevel] = actualDesc.clazz.newInstance(); 
						}
					}
					else {
						objectStack[++stackLevel] = actualDesc.clazz.newInstance(); 
					}
				}
			} catch (InstantiationException | IllegalAccessException exc) {
				stackLevel = -1;
				throw new ContentException(exc.getMessage());
			}
		}

		@Override
		public void endObj() throws ContentException {
		}

		@Override
		public void startArr() throws ContentException {
			if (actualDesc.arrayContent == null) {
				stackLevel = -1;
				throw new ContentException("Json and class description conflict: object waiting, but array started");
			}
			else {
				if (stackLevel == -1) {
					if (result != null) {
						objectStack[++stackLevel] = result; 
					}
					else {
						objectStack[++stackLevel] = new ArrayList<Object>(); 
					}
				}
				else {
					objectStack[++stackLevel] = new ArrayList<Object>(); 
				}
				actualDesc = actualDesc.arrayContent;
			}
		}

		@Override
		public void endArr() throws ContentException {
			objectStack[stackLevel] = ((ArrayList)objectStack[stackLevel]).toArray(); 
			actualDesc = actualDesc.parent;
		}

		@Override
		public void startName(char[] data, int from, int len) throws ContentException {
			final long	name = tree.seekName(data,from,from+len);
			
			if (name < 0) {
				stackLevel = -1;
				throw new ContentException("Field name ["+new String(data,from,len)+"] is missing in the class "+actualDesc.clazz.getName()); 
			}
			else {
				startName(name);
			}
		}

		@Override public void startName(String name) throws ContentException {}

		@Override
		public void startName(long id) throws ContentException {
			objectIds[stackLevel] = id;
			actualDesc = actualDesc.fields.get(id);
		}

		@Override
		public void endName() throws ContentException {
			try{final Field	f = tree.getCargo(objectIds[stackLevel-1]).field.get(actualDesc.clazzId);
			
				switch (Utils.defineClassType(f.getType())) {
					case Utils.CLASSTYPE_BYTE		:
						if (objectStack[stackLevel] instanceof Long) {
							f.setByte(objectStack[stackLevel-1],(byte)((Long)objectStack[stackLevel]).longValue());
						}
						else {
							final int	oldStackLevel = stackLevel;
							
							stackLevel = -1;
							throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with long value ["+objectStack[oldStackLevel]+"]");
						}
						break;
					case Utils.CLASSTYPE_SHORT		:
						if (objectStack[stackLevel] instanceof Long) {
							f.setShort(objectStack[stackLevel-1],(short)((Long)objectStack[stackLevel]).longValue());
						}
						else {
							final int	oldStackLevel = stackLevel;
							
							stackLevel = -1;
							throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with long value ["+objectStack[oldStackLevel]+"]");
						}
						break;
					case Utils.CLASSTYPE_CHAR		:	
						if (objectStack[stackLevel] instanceof Long) {
							f.setChar(objectStack[stackLevel-1],(char)((Long)objectStack[stackLevel]).longValue());
						}
						else {
							final int	oldStackLevel = stackLevel;
							
							stackLevel = -1;
							throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with long value ["+objectStack[oldStackLevel]+"]");
						}
						break;
					case Utils.CLASSTYPE_INT		:	
						if (objectStack[stackLevel] instanceof Long) {
							f.setInt(objectStack[stackLevel-1],(int)((Long)objectStack[stackLevel]).longValue());
						}
						else {
							final int	oldStackLevel = stackLevel;
							
							stackLevel = -1;
							throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with long value ["+objectStack[oldStackLevel]+"]");
						}
						break;
					case Utils.CLASSTYPE_LONG		:	
						if (objectStack[stackLevel] instanceof Long) {
							f.setLong(objectStack[stackLevel-1],((Long)objectStack[stackLevel]).longValue());
						}
						else {
							final int	oldStackLevel = stackLevel;
							
							stackLevel = -1;
							throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with long value ["+objectStack[oldStackLevel]+"]");
						}
						break;
					case Utils.CLASSTYPE_FLOAT		:	
						if (objectStack[stackLevel] instanceof Long) {
							f.setFloat(objectStack[stackLevel-1],((Long)objectStack[stackLevel]).longValue());
						}
						else if (objectStack[stackLevel] instanceof Double) {
							f.setFloat(objectStack[stackLevel-1],(float)((Double)objectStack[stackLevel]).doubleValue());
						}
						else {
							final int	oldStackLevel = stackLevel;
							
							stackLevel = -1;
							throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with double value ["+objectStack[oldStackLevel]+"]");
						}
						break;
					case Utils.CLASSTYPE_DOUBLE		:	
						if (objectStack[stackLevel] instanceof Long) {
							f.setDouble(objectStack[stackLevel-1],((Long)objectStack[stackLevel]).longValue());
						}
						else if (objectStack[stackLevel] instanceof Double) {
							f.setDouble(objectStack[stackLevel-1],((Double)objectStack[stackLevel]).doubleValue());
						}
						else {
							final int	oldStackLevel = stackLevel;
							
							stackLevel = -1;
							throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with boolean value ["+objectStack[oldStackLevel]+"]");
						}
						break;
					case Utils.CLASSTYPE_BOOLEAN	:	
						if (objectStack[stackLevel] instanceof Boolean) {
							f.setBoolean(objectStack[stackLevel-1],((Boolean)objectStack[stackLevel]).booleanValue());
						}
						else {
							final int	oldStackLevel = stackLevel;
							
							stackLevel = -1;
							throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with boolean value ["+objectStack[oldStackLevel]+"]");
						}
						break;
					default	:
						if (f.getType().isArray()) {
							f.set(objectStack[stackLevel-1],toArray(f.getType().getComponentType(),(Object[])objectStack[stackLevel]));
						}
						else {
							f.set(objectStack[stackLevel-1],objectStack[stackLevel]);
						}
						break;
				}
				if (actualDesc == null) {
					int x = 0;
				}
				actualDesc = actualDesc.parent;
				stackLevel--;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				stackLevel = -1;
				throw new ContentException(e.getMessage());
			}
		}

		@Override
		public void startIndex(int index) throws ContentException {
		}

		@Override
		public void endIndex() throws ContentException {
			((ArrayList)objectStack[stackLevel - 1]).add(objectStack[stackLevel]);
			stackLevel--;
		}

		@Override
		public void value(char[] data, int from, int len) throws ContentException {
			value(new String(data,from,len));
		}

		@Override
		public void value(String data) throws ContentException {
			if (stackLevel == -1) {
				throw new ContentException("Single value is not supported for class deserialization");
			}
			else {
				objectStack[++stackLevel] = data;
			}
		}

		@Override
		public void value(long data) throws ContentException {
			if (stackLevel == -1) {
				throw new ContentException("Single value is not supported for class deserialization");
			}
			else {
				objectStack[++stackLevel] = data;
			}
		}

		@Override
		public void value(double data) throws ContentException {
			if (stackLevel == -1) {
				throw new ContentException("Single value is not supported for class deserialization");
			}
			else {
				objectStack[++stackLevel] = data;
			}
		}

		@Override
		public void value(boolean data) throws ContentException {
			if (stackLevel == -1) {
				throw new ContentException("Single value is not supported for class deserialization");
			}
			else {
				objectStack[++stackLevel] = data;
			}
		}

		@Override
		public void value() throws ContentException {
			if (stackLevel == -1) {
				throw new ContentException("Single value is not supported for class deserialization");
			}
			else {
				objectStack[++stackLevel] = null;
			}
		}

		@Override
		public void use(T instance) {
			result = instance;
		}
		
		@Override
		public T getInstance() {
			return result;
		}

		private Object toArray(final Class<?> componentType, final Object[] source) throws ContentException {
			switch (Utils.defineClassType(componentType)) {
				case Utils.CLASSTYPE_BYTE	:
					final byte[]	byteResult = new byte[source.length];
					
					for (int index = 0; index < byteResult.length; index++) {
						if (source[index] instanceof Long) {
							byteResult[index] = ((Long)source[index]).byteValue();
						}
						else {
							throw new ContentException("Array content for index ["+index+"] contains uncompatible type ["+source[index].getClass().getName()+"] for byte array element");
						}
					}
					return byteResult;
				case Utils.CLASSTYPE_SHORT	:
					final short[]	shortResult = new short[source.length];
					
					for (int index = 0; index < shortResult.length; index++) {
						if (source[index] instanceof Long) {
							shortResult[index] = ((Long)source[index]).shortValue();
						}
						else {
							throw new ContentException("Array content for index ["+index+"] contains uncompatible type ["+source[index].getClass().getName()+"] for short array element");
						}
					}
					return shortResult;
				case Utils.CLASSTYPE_CHAR	:	
					final char[]	charResult = new char[source.length];
					
					for (int index = 0; index < charResult.length; index++) {
						if (source[index] instanceof Long) {
							charResult[index] = (char)((Long)source[index]).intValue();
						}
						else {
							throw new ContentException("Array content for index ["+index+"] contains uncompatible type ["+source[index].getClass().getName()+"] for char array element");
						}
					}
					return charResult;
				case Utils.CLASSTYPE_INT	:	
					final int[]	intResult = new int[source.length];
					
					for (int index = 0; index < intResult.length; index++) {
						if (source[index] instanceof Long) {
							intResult[index] = ((Long)source[index]).intValue();
						}
						else {
							throw new ContentException("Array content for index ["+index+"] contains uncompatible type ["+source[index].getClass().getName()+"] for int array element");
						}
					}
					return intResult;
				case Utils.CLASSTYPE_LONG	:	
					final long[]	longResult = new long[source.length];
					
					for (int index = 0; index < longResult.length; index++) {
						if (source[index] instanceof Long) {
							longResult[index] = ((Long)source[index]).longValue();
						}
						else {
							throw new ContentException("Array content for index ["+index+"] contains uncompatible type ["+source[index].getClass().getName()+"] for long array element");
						}
					}
					return longResult;
				case Utils.CLASSTYPE_FLOAT	:	
					final float[]	floatResult = new float[source.length];
					
					for (int index = 0; index < floatResult.length; index++) {
						if (source[index] instanceof Long) {
							floatResult[index] = ((Long)source[index]).longValue();
						}
						else if (source[index] instanceof Double) {
							floatResult[index] = ((Double)source[index]).floatValue();
						}
						else {
							throw new ContentException("Array content for index ["+index+"] contains uncompatible type ["+source[index].getClass().getName()+"] for float array element");
						}
					}
					return floatResult;
				case Utils.CLASSTYPE_DOUBLE	:	
					final double[]	doubleResult = new double[source.length];
					
					for (int index = 0; index < doubleResult.length; index++) {
						if (source[index] instanceof Long) {
							doubleResult[index] = ((Long)source[index]).longValue();
						}
						else if (source[index] instanceof Double) {
							doubleResult[index] = ((Double)source[index]).doubleValue();
						}
						else {
							throw new ContentException("Array content for index ["+index+"] contains uncompatible type ["+source[index].getClass().getName()+"] for double array element");
						}
					}
					return doubleResult;
				case Utils.CLASSTYPE_BOOLEAN:
					final boolean[]	booleanResult = new boolean[source.length];
					
					for (int index = 0; index < booleanResult.length; index++) {
						if (source[index] instanceof Boolean) {
							booleanResult[index] = ((Boolean)source[index]).booleanValue();
						}
						else {
							throw new ContentException("Array content for index ["+index+"] contains uncompatible type ["+source[index].getClass().getName()+"] for boolean array element");
						}
					}
					return booleanResult;
				default :
					final Object[]	refResult = (Object[]) Array.newInstance(componentType,source.length);
					
					for (int index = 0; index < refResult.length; index++) {
						refResult[index] = source[index];
					}
					return refResult;
			}
		}
	}
}
