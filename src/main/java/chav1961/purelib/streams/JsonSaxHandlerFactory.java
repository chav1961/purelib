package chav1961.purelib.streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.GrowableBooleanArray;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.growablearrays.GrowableDoubleArray;
import chav1961.purelib.basic.growablearrays.GrowableFloatArray;
import chav1961.purelib.basic.growablearrays.GrowableIntArray;
import chav1961.purelib.basic.growablearrays.GrowableLongArray;
import chav1961.purelib.basic.growablearrays.GrowableShortArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.char2byte.AsmWriter;
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
 * next parsing. You can also use this call to fill the <i>predefined</i> class instance with the deserialized data. Also pay attention, that 
 * on building {@link JsonSaxHandler} class with the <b>publicOnly</b> flag, it makes by reflection, but it's runtime not uses reflection, but 
 * on-the-fly built class to create new instances and set it's fields. It radically increases parsing speed, but strongly check visibility of the
 * classes and methods. To avoid problems with the visibility, use only <i>public</i> classes in this case.</p>
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
	 * @param publicOnly use only public classes and fields in the class for deserialization purposes. It's strongly recommended to
	 * turn on this option, because it radically increases parsing performance
	 * @return JsonSaxDeserializer implementation to use in the constructor of the {@link JsonSaxParser}.
	 * @throws ContentException 
	 */
	public static <T> JsonSaxDeserializer<T> buildDeserializer(final Class<T> clazz, final boolean publicOnly) throws ContentException {
		final SyntaxTreeInterface<FieldDesc>	tree = new AndOrTree<FieldDesc>(1,1);
		final ClassDesc							desc = new ClassDesc(clazz,tree.placeName(clazz.getCanonicalName(),null));

		collectFieldNames(clazz,tree,publicOnly);
		buildClassTree(clazz,desc,publicOnly,tree);

		final int			treeDepth = calculateMaxTreeDepth(desc,0)+1;
		final Set<Class>	preventDuplicates = new HashSet<>();
		final long			lastTreeId;

		Class				extra = clazz;
		while (extra.isArray()) {
			extra = extra.getComponentType();
		}
		
		if (publicOnly && !extra.isPrimitive()) {		// Build class for direct access instead of reflections...
			try(final ByteArrayOutputStream		baos = new ByteArrayOutputStream();
				final Writer					wr = new AsmWriter(baos);) {
//				final Writer					wr = new OutputStreamWriter(System.err);) {
				final String					pseudoClassName = JsonSaxHandlerFactory.class.getPackage().getName()+'.'+extra.getSimpleName()+"_serv"; 
				final List<SettingPairs>		settingPairs = new ArrayList<>();				
				
				wr.write(" 				.package "+JsonSaxHandlerFactory.class.getPackage().getName()+'\n');
				wr.write(" 				.import "+JsonSaxHandlerFactory.class.getPackage().getName()+".CreateAndSet\n");
				wr.write(" 				.import "+JsonSaxHandlerFactory.class.getPackage().getName()+".JsonSaxHandlerFactory$PrimitiveCollection\n");
				wr.write(" 				.import java.lang.IllegalArgumentException\n");
				
				preventDuplicates.clear();
				preventDuplicates.add(String.class);			
				printClassCreationImport(wr,desc,preventDuplicates);
				
				wr.write(extra.getSimpleName()+"_serv 	.class public implements "+JsonSaxHandlerFactory.class.getPackage().getName()+".CreateAndSet\n");
				
				wr.write(extra.getSimpleName()+"_serv	.method void public\n");
				wr.write("				.stack 2\n");
				wr.write("				aload_0\n");
				wr.write("				invokespecial java.lang.Object.Object()V\n");
				wr.write("				return\n");
				wr.write(extra.getSimpleName()+"_serv	.end\n");
				wr.write("newInstance 	.method Object public\n");
				wr.write("classId		.parameter int\n");
				wr.write("				.stack 10\n");
				wr.write("				iload_1\n");
				wr.write("				tableswitch\n");
				
				preventDuplicates.clear();
				preventDuplicates.add(String.class);
				printClassCreationLabels(wr,desc,preventDuplicates);
				
				wr.write("				.default Throw\n");
				wr.write("				.end\n");
				wr.write("Throw:		new 	java.lang.IllegalArgumentException\n");
				wr.write("				dup\n");
				wr.write("				ldc_w 	\"Unknown classId value to create instance\"\n");
				wr.write("				invokespecial java.lang.IllegalArgumentException.IllegalArgumentException(Ljava/lang/String;)V\n");
				wr.write("				athrow\n");
				
				preventDuplicates.clear();
				preventDuplicates.add(String.class);
				printClassCreationCode(wr,desc,preventDuplicates);
				
				wr.write("newInstance 	.end\n");			

				preventDuplicates.clear();
				preventDuplicates.add(String.class);
				collectSettingPairs(desc,preventDuplicates,settingPairs,lastTreeId = tree.placeName(" ",null));
				
				wr.write("setValue 			.method void public\n");
				wr.write("instance			.parameter Object final\n");
				wr.write("classAndFieldId	.parameter int final\n");
				wr.write("value				.parameter Object final\n");
				wr.write("					.stack 5\n");
				
				if (settingPairs.size() > 0) {		// Class contains public fields
					wr.write("				iload_2\n");
					wr.write("				tableswitch\n");
					
					printClassSettingsLabels(wr,settingPairs);
					
					wr.write("				.default Unknown\n");
					wr.write("				.end\n");
					wr.write("Unknown:		new 	java.lang.IllegalArgumentException\n");
					wr.write("				dup\n");
					wr.write("				ldc_w 	\"Unknown classId/fieldId combination to set value\"\n");
					wr.write("				invokespecial java.lang.IllegalArgumentException.IllegalArgumentException(Ljava/lang/String;)V\n");
					wr.write("				athrow\n");
					wr.write("Cast:			new 	java.lang.IllegalArgumentException\n");
					wr.write("				dup\n");
					wr.write("				ldc_w 	\"Source primitive type can't be cast to target field type\"\n");
					wr.write("				invokespecial java.lang.IllegalArgumentException.IllegalArgumentException(Ljava/lang/String;)V\n");
					wr.write("				athrow\n");
					
					printClassSettingsCode(wr,settingPairs,tree);
				}
				
				wr.write("				return\n");
				wr.write("setValue 		.end\n");
				
				wr.write(extra.getSimpleName()+"_serv	.end\n");
				wr.flush();
				return new JsonSaxDeserializerImpl<T>(tree,desc,treeDepth,lastTreeId,pseudoClassName,baos.toByteArray());
			} catch (IOException e) {
				e.printStackTrace();
				throw new ContentException(e.getMessage());
			}
		}
		else {
			return new JsonSaxDeserializerImpl<T>(tree,desc,treeDepth,0);
		}
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
						final ClassDesc	itemDesc = new ClassDesc(item.getType(),tree.placeName(item.getType().getCanonicalName(),null),desc);
						
						desc.fields.put(tree.seekName(item.getName()),itemDesc);
						buildClassTree(item.getType(),itemDesc,publicOnly,tree);
					}
				}
			}
			else {
				for (Field item : clazz.getDeclaredFields()) {
					if (!Modifier.isStatic(item.getModifiers()) && !Modifier.isTransient(item.getModifiers())) {
						final ClassDesc	itemDesc = new ClassDesc(item.getType(),tree.placeName(item.getType().getCanonicalName(),null),desc);
						
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

	private static void printClassCreationImport(final Writer wr, final ClassDesc desc, final Set<Class> preventDuplicates) throws IOException {
		if (desc.clazz.isArray()) {
			printClassCreationImport(wr,desc.arrayContent,preventDuplicates);
		}
		else if (!desc.clazz.isPrimitive()) {
			if (!preventDuplicates.contains(desc.clazz)) {
				preventDuplicates.add(desc.clazz);
				wr.write("		.import "+desc.clazz.getCanonicalName()+'\n');
			}
			for (ClassDesc item : desc.fields.values()) {
				printClassCreationImport(wr,item,preventDuplicates);
			}
		}
	}

	private static void printClassCreationLabels(final Writer wr, final ClassDesc desc, final Set<Class> preventDuplicates) throws IOException {
		if (desc.clazz.isArray()) {
			printClassCreationLabels(wr,desc.arrayContent,preventDuplicates);
		}
		else if (!desc.clazz.isPrimitive()) {
			if (!preventDuplicates.contains(desc.clazz)) {
				preventDuplicates.add(desc.clazz);
				wr.write("			"+desc.clazzId+",L"+desc.clazzId+'\n');
			}
			for (ClassDesc item : desc.fields.values()) {
				printClassCreationLabels(wr,item,preventDuplicates);
			}
		}
	}
	
	private static void printClassCreationCode(final Writer wr, final ClassDesc desc, final Set<Class> preventDuplicates) throws IOException {
		if (desc.clazz.isArray()) {
			printClassCreationCode(wr,desc.arrayContent,preventDuplicates);
		}
		else if (!desc.clazz.isPrimitive()) {
			if (!preventDuplicates.contains(desc.clazz)) {
				preventDuplicates.add(desc.clazz);
				wr.write("L"+desc.clazzId+":	new "+desc.clazz.getCanonicalName()+'\n');
				wr.write("			dup\n");
				wr.write("			invokespecial "+desc.clazz.getName()+'.'+desc.clazz.getSimpleName()+"()V\n");
				wr.write("			areturn\n");
			}
			for (ClassDesc item : desc.fields.values()) {
				printClassCreationCode(wr,item,preventDuplicates);
			}
		}
	}

	private static void collectSettingPairs(final ClassDesc desc, final Set<Class> preventDuplicates, final List<SettingPairs> settingPairs, final long lastTreeId) {
		if (desc.clazz.isArray()) {
			collectSettingPairs(desc.arrayContent,preventDuplicates,settingPairs,lastTreeId);
		}
		else {
			if (!preventDuplicates.contains(desc.clazz)) {
				preventDuplicates.add(desc.clazz);
				
				for (ClassDesc item : desc.fields.values()) {
					collectSettingPairs(item,preventDuplicates,settingPairs,lastTreeId);
				}
				for (Entry<Long, ClassDesc> item : desc.fields.entrySet()) {
					settingPairs.add(new SettingPairs(desc.clazzId,desc.clazz,item.getKey().longValue(),item.getValue().clazz,(int)(desc.clazzId * lastTreeId + item.getKey())));
				}
			}
		}
	}

	private static void printClassSettingsLabels(final Writer wr, final List<SettingPairs> settingPairs) throws IOException {
		for (SettingPairs item : settingPairs) {
			wr.write("		"+item.labelId+",L"+item.labelId+'\n');
		}
	}
	

	private static void printClassSettingsCode(final Writer wr, final List<SettingPairs> settingPairs, final SyntaxTreeInterface<FieldDesc> tree) throws IOException {
		for (SettingPairs item : settingPairs) {
			wr.write("L"+item.labelId+": 	aload_1\n");
			wr.write("			checkcast "+tree.getName(item.classId)+"\n");
			wr.write("			aload_3\n");
			switch (Utils.defineClassType(item.targetField)) {
				case Utils.CLASSTYPE_REFERENCE	:
					wr.write("			putfield "+tree.getName(item.classId)+"."+tree.getName(item.fieldId)+"\n");
					break;
				case Utils.CLASSTYPE_BYTE		:
				case Utils.CLASSTYPE_SHORT		:
				case Utils.CLASSTYPE_CHAR		:
				case Utils.CLASSTYPE_INT		:
					wr.write("			checkcast chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection\n");
					wr.write("			dup\n");
					wr.write("			getfield chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection.contentType\n");
					wr.write("			ldc_w "+PrimitiveCollection.CONTENT_LONG+"\n");
					wr.write("			if_icmpne Cast\n");
					wr.write("			getfield chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection.longValue\n");
					wr.write("			l2i\n");
					wr.write("			putfield "+tree.getName(item.classId)+"."+tree.getName(item.fieldId)+"\n");
					break;
				case Utils.CLASSTYPE_LONG		:
					wr.write("			checkcast chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection\n");
					wr.write("			dup\n");
					wr.write("			getfield chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection.contentType\n");
					wr.write("			ldc "+PrimitiveCollection.CONTENT_LONG+"\n");
					wr.write("			if_icmpne Cast\n");
					wr.write("			getfield chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection.longValue\n");
					wr.write("			putfield "+tree.getName(item.classId)+"."+tree.getName(item.fieldId)+"\n");
					break;
				case Utils.CLASSTYPE_FLOAT		:
					wr.write("			checkcast chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection\n");
					wr.write("			dup\n");
					wr.write("			getfield chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection.contentType\n");
					wr.write("			ldc "+PrimitiveCollection.CONTENT_DOUBLE+"\n");
					wr.write("			if_icmpne Cast\n");
					wr.write("			getfield chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection.doubleValue\n");
					wr.write("			d2f\n");
					wr.write("			putfield "+tree.getName(item.classId)+"."+tree.getName(item.fieldId)+"\n");
					break;
				case Utils.CLASSTYPE_DOUBLE		:
					wr.write("			checkcast chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection\n");
					wr.write("			dup\n");
					wr.write("			getfield chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection.contentType\n");
					wr.write("			ldc "+PrimitiveCollection.CONTENT_DOUBLE+"\n");
					wr.write("			if_icmpne Cast\n");
					wr.write("			getfield chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection.doubleValue\n");
					wr.write("			putfield "+tree.getName(item.classId)+"."+tree.getName(item.fieldId)+"\n");
					break;
				case Utils.CLASSTYPE_BOOLEAN	:
					wr.write("			checkcast chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection\n");
					wr.write("			dup\n");
					wr.write("			getfield chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection.contentType\n");
					wr.write("			ldc "+PrimitiveCollection.CONTENT_BOOLEAN+"\n");
					wr.write("			if_icmpne Cast\n");
					wr.write("			getfield chav1961.purelib.streams.JsonSaxHandlerFactory.PrimitiveCollection.booleanValue\n");
					wr.write("			putfield "+tree.getName(item.classId)+"."+tree.getName(item.fieldId)+"\n");
					break;
				default :  throw new UnsupportedOperationException();
			}
			wr.write("			return\n");
		}
	}
	
	private static class FieldDesc {
		Map<Long,Field>		field = new HashMap<>();

		@Override
		public String toString() {
			return "FieldDesc [field=" + field + "]";
		}
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
			return "ClassDesc [class=" + clazz + ", id = " + clazzId + ", fields = " + fields + ", arrayContent=" + arrayContent + "]";
		}
	}
	
	private static class JsonSaxDeserializerImpl<T> implements JsonSaxDeserializer<T> {
		private final SyntaxTreeInterface<FieldDesc>	tree;
		private final ClassDesc							desc;
		private final Object[]							objectStack;
		private final long[]							objectIds;
		private final PrimitiveCollection				forPrimitives = new PrimitiveCollection();
		private final long								lastTreeId;
		
		private InternalClassLoader	cl = new InternalClassLoader(JsonSaxHandlerFactory.class.getClassLoader());
		private CreateAndSet		cs;
		private T					result = null;
		private ClassDesc			actualDesc;
		private int					stackLevel = -1;
		
		public JsonSaxDeserializerImpl(final SyntaxTreeInterface<FieldDesc> tree, final ClassDesc desc, final int stackDepth, final long lastTreeId) throws ContentException {
			this.tree = tree;
			this.desc = desc;
			this.lastTreeId = lastTreeId;
			this.objectStack = new Object[stackDepth];
			this.objectIds = new long[stackDepth];
			this.cs = null;
		}

		public JsonSaxDeserializerImpl(final SyntaxTreeInterface<FieldDesc> tree, final ClassDesc desc, final int stackDepth, final long lastTreeId, final String serviceClass, final byte[] serviceClassBody) throws ContentException {
			this.tree = tree;
			this.desc = desc;
			this.lastTreeId = lastTreeId;
			this.objectStack = new Object[stackDepth];
			this.objectIds = new long[stackDepth];
			try{final Class<CreateAndSet>	clazz = (Class<CreateAndSet>)cl.define(serviceClass,serviceClassBody,0,serviceClassBody.length); 
				this.cs = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new ContentException(e.getMessage());
			}
		}
		
		@Override
		public void startDoc() throws ContentException {
			actualDesc = desc;
		}

		@Override
		public void endDoc() throws ContentException {
			cs = null;
			cl = null;
			
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
							objectStack[++stackLevel] = cs != null ? cs.newInstance((int)actualDesc.clazzId) : actualDesc.clazz.newInstance(); 
						}
					}
					else {
						objectStack[++stackLevel] = cs != null ? cs.newInstance((int)actualDesc.clazzId) : actualDesc.clazz.newInstance();
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
				switch (Utils.defineClassType(actualDesc.arrayContent.clazz)) {
					case Utils.CLASSTYPE_REFERENCE	:
						objectStack[++stackLevel] = new ArrayList<Object>();
						break;
					case Utils.CLASSTYPE_BYTE		:
						objectStack[++stackLevel] = new GrowableByteArray(true);
						break;
					case Utils.CLASSTYPE_SHORT		:
						objectStack[++stackLevel] = new GrowableShortArray(true);
						break;
					case Utils.CLASSTYPE_CHAR		:
						objectStack[++stackLevel] = new GrowableCharArray(true);
						break;
					case Utils.CLASSTYPE_INT		:
						objectStack[++stackLevel] = new GrowableIntArray(true);
						break;
					case Utils.CLASSTYPE_LONG		:
						objectStack[++stackLevel] = new GrowableLongArray(true);
						break;
					case Utils.CLASSTYPE_FLOAT		:
						objectStack[++stackLevel] = new GrowableFloatArray(true);
						break;
					case Utils.CLASSTYPE_DOUBLE		:
						objectStack[++stackLevel] = new GrowableDoubleArray(true);
						break;
					case Utils.CLASSTYPE_BOOLEAN	:
						objectStack[++stackLevel] = new GrowableBooleanArray(true);
						break;
				}
				actualDesc = actualDesc.arrayContent;
			}
		}

		@Override
		public void endArr() throws ContentException {
			switch (Utils.defineClassType(actualDesc.clazz)) {
				case Utils.CLASSTYPE_REFERENCE	:
					objectStack[stackLevel] = ((ArrayList)objectStack[stackLevel]).toArray(); 
					break;
				case Utils.CLASSTYPE_BYTE		:
					final byte[]	byteResult = new byte[((GrowableByteArray)objectStack[stackLevel]).length()];
					
					if (byteResult.length > 0) {
						((GrowableByteArray)objectStack[stackLevel]).read(0,byteResult);
						((GrowableByteArray)objectStack[stackLevel]).clear();
					}
					objectStack[stackLevel] = byteResult;
					break;
				case Utils.CLASSTYPE_SHORT		:
					final short[]	shortResult = new short[((GrowableShortArray)objectStack[stackLevel]).length()];
					
					if (shortResult.length > 0) {
						((GrowableShortArray)objectStack[stackLevel]).read(0,shortResult);
						((GrowableShortArray)objectStack[stackLevel]).clear();
					}
					objectStack[stackLevel] = shortResult;
					break;
				case Utils.CLASSTYPE_CHAR		:
					final char[]	charResult = new char[((GrowableCharArray)objectStack[stackLevel]).length()];
					
					if (charResult.length > 0) {
						((GrowableCharArray)objectStack[stackLevel]).read(0,charResult);
						((GrowableCharArray)objectStack[stackLevel]).clear();
					}
					objectStack[stackLevel] = charResult;
					break;
				case Utils.CLASSTYPE_INT		:
					final int[]		intResult = new int[((GrowableIntArray)objectStack[stackLevel]).length()];
					
					if (intResult.length > 0) {
						((GrowableIntArray)objectStack[stackLevel]).read(0,intResult);
						((GrowableIntArray)objectStack[stackLevel]).clear();
					}
					objectStack[stackLevel] = intResult;
					break;
				case Utils.CLASSTYPE_LONG		:
					final long[]	longResult = new long[((GrowableLongArray)objectStack[stackLevel]).length()];
					
					if (longResult.length > 0) {
						((GrowableLongArray)objectStack[stackLevel]).read(0,longResult);
						((GrowableLongArray)objectStack[stackLevel]).clear();
					}
					objectStack[stackLevel] = longResult;
					break;
				case Utils.CLASSTYPE_FLOAT		:
					final float[]	floatResult = new float[((GrowableFloatArray)objectStack[stackLevel]).length()];
					
					if (floatResult.length > 0) {
						((GrowableFloatArray)objectStack[stackLevel]).read(0,floatResult);
						((GrowableFloatArray)objectStack[stackLevel]).clear();
					}
					objectStack[stackLevel] = floatResult;
					break;
				case Utils.CLASSTYPE_DOUBLE		:
					final double[]	doubleResult = new double[((GrowableDoubleArray)objectStack[stackLevel]).length()];
					
					if (doubleResult.length > 0) {
						((GrowableDoubleArray)objectStack[stackLevel]).read(0,doubleResult);
						((GrowableDoubleArray)objectStack[stackLevel]).clear();
					}
					objectStack[stackLevel] = doubleResult;
					break;
				case Utils.CLASSTYPE_BOOLEAN	:
					final boolean[]	booleanResult = new boolean[((GrowableBooleanArray)objectStack[stackLevel]).length()];
					
					if (booleanResult.length > 0) {
						((GrowableBooleanArray)objectStack[stackLevel]).read(0,booleanResult);
						((GrowableBooleanArray)objectStack[stackLevel]).clear();
					}
					objectStack[stackLevel] = booleanResult;
					break;
			}
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
			try{final Field	f = tree.getCargo(objectIds[stackLevel-1]).field.get(actualDesc.parent.clazzId);
			
				if (cs != null) {
					cs.setValue(objectStack[stackLevel-1],(int)(actualDesc.parent.clazzId*lastTreeId+objectIds[stackLevel-1]),objectStack[stackLevel]);
				}
				else {
					switch (Utils.defineClassType(f.getType())) {
						case Utils.CLASSTYPE_BYTE		:
							if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
								f.setByte(objectStack[stackLevel-1],(byte)((PrimitiveCollection)objectStack[stackLevel]).longValue);
							}
							else {
								final int	oldStackLevel = stackLevel;
								
								stackLevel = -1;
								throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with long value ["+objectStack[oldStackLevel]+"]");
							}
							break;
						case Utils.CLASSTYPE_SHORT		:
							if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
								f.setShort(objectStack[stackLevel-1],(short)((PrimitiveCollection)objectStack[stackLevel]).longValue);
							}
							else {
								final int	oldStackLevel = stackLevel;
								
								stackLevel = -1;
								throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with long value ["+objectStack[oldStackLevel]+"]");
							}
							break;
						case Utils.CLASSTYPE_CHAR		:	
							if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
								f.setChar(objectStack[stackLevel-1],(char)((PrimitiveCollection)objectStack[stackLevel]).longValue);
							}
							else {
								final int	oldStackLevel = stackLevel;
								
								stackLevel = -1;
								throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with long value ["+objectStack[oldStackLevel]+"]");
							}
							break;
						case Utils.CLASSTYPE_INT		:	
							if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
								f.setInt(objectStack[stackLevel-1],(int)((PrimitiveCollection)objectStack[stackLevel]).longValue);
							}
							else {
								final int	oldStackLevel = stackLevel;
								
								stackLevel = -1;
								throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with long value ["+objectStack[oldStackLevel]+"]");
							}
							break;
						case Utils.CLASSTYPE_LONG		:	
							if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
								f.setLong(objectStack[stackLevel-1],((PrimitiveCollection)objectStack[stackLevel]).longValue);
							}
							else {
								final int	oldStackLevel = stackLevel;
								
								stackLevel = -1;
								throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with long value ["+objectStack[oldStackLevel]+"]");
							}
							break;
						case Utils.CLASSTYPE_FLOAT		:	
							if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
								f.setFloat(objectStack[stackLevel-1],((PrimitiveCollection)objectStack[stackLevel]).longValue);
							}
							else if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_DOUBLE) {
								f.setFloat(objectStack[stackLevel-1],(float)((PrimitiveCollection)objectStack[stackLevel]).doubleValue);
							}
							else {
								final int	oldStackLevel = stackLevel;
								
								stackLevel = -1;
								throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with double value ["+objectStack[oldStackLevel]+"]");
							}
							break;
						case Utils.CLASSTYPE_DOUBLE		:	
							if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
								f.setDouble(objectStack[stackLevel-1],((PrimitiveCollection)objectStack[stackLevel]).longValue);
							}
							else if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_DOUBLE) {
								f.setDouble(objectStack[stackLevel-1],((PrimitiveCollection)objectStack[stackLevel]).doubleValue);
							}
							else {
								final int	oldStackLevel = stackLevel;
								
								stackLevel = -1;
								throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with boolean value ["+objectStack[oldStackLevel]+"]");
							}
							break;
						case Utils.CLASSTYPE_BOOLEAN	:	
							if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_BOOLEAN) {
								f.setBoolean(objectStack[stackLevel-1],((PrimitiveCollection)objectStack[stackLevel]).booleanValue);
							}
							else {
								final int	oldStackLevel = stackLevel;
								
								stackLevel = -1;
								throw new ContentException("Json and class conflict: type of field name ["+tree.getName(objectIds[oldStackLevel-1])+"] is incompatible with boolean value ["+objectStack[oldStackLevel]+"]");
							}
							break;
						default	:
							if (f.getType().isArray() && !f.getType().getComponentType().isPrimitive()) {
								f.set(objectStack[stackLevel-1],toArray(f.getType().getComponentType(),(Object[])objectStack[stackLevel]));
							}
							else {
								f.set(objectStack[stackLevel-1],objectStack[stackLevel]);
							}
							break;
					}
				}
				actualDesc = actualDesc.parent;
				stackLevel--;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				stackLevel = -1;
				throw new ContentException(e.getMessage(),e);
			}
		}

		@Override
		public void startIndex(int index) throws ContentException {
		}

		@Override
		public void endIndex() throws ContentException {
			switch (Utils.defineClassType(actualDesc.clazz)) {
				case Utils.CLASSTYPE_REFERENCE	:
					((ArrayList)objectStack[stackLevel - 1]).add(objectStack[stackLevel]);
					break;
				case Utils.CLASSTYPE_BYTE		:
					if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
						((GrowableByteArray)objectStack[stackLevel - 1]).append((byte)((PrimitiveCollection)objectStack[stackLevel]).longValue);
					}
					else {
						throw new ContentException("Incompatible data type for byte array");
					}
					break;
				case Utils.CLASSTYPE_SHORT		:
					if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
						((GrowableShortArray)objectStack[stackLevel - 1]).append((short)((PrimitiveCollection)objectStack[stackLevel]).longValue);
					}
					else {
						throw new ContentException("Incompatible data type for short array");
					}
					break;
				case Utils.CLASSTYPE_CHAR		:
					if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
						((GrowableCharArray)objectStack[stackLevel - 1]).append((char)((PrimitiveCollection)objectStack[stackLevel]).longValue);
					}
					else {
						throw new ContentException("Incompatible data type for char array");
					}
					break;
				case Utils.CLASSTYPE_INT		:
					if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
						((GrowableIntArray)objectStack[stackLevel - 1]).append((int)((PrimitiveCollection)objectStack[stackLevel]).longValue);
					}
					else {
						throw new ContentException("Incompatible data type for int array");
					}
					break;
				case Utils.CLASSTYPE_LONG		:
					if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
						((GrowableLongArray)objectStack[stackLevel - 1]).append(((PrimitiveCollection)objectStack[stackLevel]).longValue);
					}
					else {
						throw new ContentException("Incompatible data type for long array");
					}
					break;
				case Utils.CLASSTYPE_FLOAT		:
					if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
						((GrowableFloatArray)objectStack[stackLevel - 1]).append(((PrimitiveCollection)objectStack[stackLevel]).longValue);
					}
					else if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_DOUBLE) {
						((GrowableFloatArray)objectStack[stackLevel - 1]).append((float)((PrimitiveCollection)objectStack[stackLevel]).doubleValue);
					}
					else {
						throw new ContentException("Incompatible data type for float array");
					}
					break;
				case Utils.CLASSTYPE_DOUBLE		:
					if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_LONG) {
						((GrowableDoubleArray)objectStack[stackLevel - 1]).append(((PrimitiveCollection)objectStack[stackLevel]).longValue);
					}
					else if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_DOUBLE) {
						((GrowableDoubleArray)objectStack[stackLevel - 1]).append(((PrimitiveCollection)objectStack[stackLevel]).doubleValue);
					}
					else {
						throw new ContentException("Incompatible data type for double array");
					}
					break;
				case Utils.CLASSTYPE_BOOLEAN	:
					if ((objectStack[stackLevel] instanceof PrimitiveCollection) && ((PrimitiveCollection)objectStack[stackLevel]).contentType == PrimitiveCollection.CONTENT_BOOLEAN) {
						((GrowableBooleanArray)objectStack[stackLevel - 1]).append(((PrimitiveCollection)objectStack[stackLevel]).booleanValue);
					}
					else {
						throw new ContentException("Incompatible data type for boolean array");
					}
					break;
			}
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
		public void value(final long data) throws ContentException {
			if (stackLevel == -1) {
				throw new ContentException("Single value is not supported for class deserialization");
			}
			else {
				objectStack[++stackLevel] = forPrimitives;
				forPrimitives.contentType = PrimitiveCollection.CONTENT_LONG;
				forPrimitives.longValue = data; 
			}
		}

		@Override
		public void value(final double data) throws ContentException {
			if (stackLevel == -1) {
				throw new ContentException("Single value is not supported for class deserialization");
			}
			else {
				objectStack[++stackLevel] = forPrimitives;
				forPrimitives.contentType = PrimitiveCollection.CONTENT_DOUBLE;
				forPrimitives.doubleValue = data;
			}
		}

		@Override
		public void value(final boolean data) throws ContentException {
			if (stackLevel == -1) {
				throw new ContentException("Single value is not supported for class deserialization");
			}
			else {
				objectStack[++stackLevel] = forPrimitives;
				forPrimitives.contentType = PrimitiveCollection.CONTENT_BOOLEAN;
				forPrimitives.booleanValue = data;
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
				case Utils.CLASSTYPE_SHORT	:
				case Utils.CLASSTYPE_CHAR	:	
				case Utils.CLASSTYPE_INT	:	
				case Utils.CLASSTYPE_LONG	:	
				case Utils.CLASSTYPE_FLOAT	:	
				case Utils.CLASSTYPE_DOUBLE	:	
				case Utils.CLASSTYPE_BOOLEAN:
					return source;
				default :
					final Object[]	refResult = (Object[]) Array.newInstance(componentType,source.length);
					
					for (int index = 0; index < refResult.length; index++) {
						refResult[index] = source[index];
					}
					return refResult;
			}
		}
	}
	
	public static class PrimitiveCollection {
		private static final int	CONTENT_LONG = 0;
		private static final int	CONTENT_DOUBLE = 1;
		private static final int	CONTENT_BOOLEAN = 2;
		
		public int		contentType;
		public long		longValue;
		public double	doubleValue;
		public boolean	booleanValue;
	}
	
	private static class InternalClassLoader extends ClassLoader {
		InternalClassLoader(final ClassLoader parent) {
			super(parent);
		}
		
		public Class define(final String className, final byte[] content, final int from, final int len) {
			return super.defineClass(className,content,from,len);
		}
	}
	
	private static class SettingPairs {
		final long		classId, fieldId;
		final Class<?>	targetClass;
		final Class<?>	targetField;
		final int		labelId;
		
		SettingPairs(final long classId, final Class<?> targetClass, final long fieldId, final Class<?> targetField, final int labelId) {
			this.classId = classId;
			this.fieldId = fieldId;
			this.labelId = labelId;
			this.targetClass = targetClass;
			this.targetField = targetField;
		}

		@Override
		public String toString() {
			return "SettingPairs [classId=" + classId + ", fieldId=" + fieldId + ", targetClass=" + targetClass + ", targetField=" + targetField + ", labelId=" + labelId + "]";
		}
	}
}
