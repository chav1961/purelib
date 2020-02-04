package chav1961.purelib.streams.char2byte.asm;



import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.Prescription;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;


class ClassDescriptionRepo {
	private static final String					KIND_CLASS = "class";
	private static final String					KIND_FIELD = "field";
	private static final String					KIND_METHOD = "method";
	private static final String					KIND_CONSTRUCTOR = "constructor";

	private static final Class<?>[]				PRELOADED_CLASSES = new Class<?>[]{boolean.class,byte.class,char.class,double.class,float.class,int.class,long.class,short.class,void.class,
													Object.class,String.class,Throwable.class,Class.class
												};

	private final SyntaxTreeInterface<char[]>	referenceNames = new AndOrTree<char[]>();
	private final List<RepoStack>				stack = new ArrayList<>();
	@SuppressWarnings("unused")
	private final Writer						diagnostics;

	@FunctionalInterface
	private interface WalkCallback<T> {
		void processEntity(T entity) throws ContentException;
	}
	
	ClassDescriptionRepo() throws ContentException {
		this.diagnostics = null;
		stack.add(0,new RepoStack(new AndOrTree<Keeper>(2,16)));
		for (Class<?> item : PRELOADED_CLASSES) {
			addClassDescription(stack.get(0).repoLong,item,false);
		}
	}

	ClassDescriptionRepo(final Writer diagnostics) throws ContentException {
		this.diagnostics = diagnostics;
		stack.add(0,new RepoStack(new AndOrTree<Keeper>(2,16)));
		for (Class<?> item : PRELOADED_CLASSES) {
			addClassDescription(stack.get(0).repoLong,item,false);
		}
	}
	
	void addClassReference(final String classReference, final String className) throws ContentException {
		if (referenceNames.seekName(classReference) >= 0) {
			throw new ContentException("Duplicate class reference name ["+classReference+"]");
		}
		else {
			referenceNames.placeName(classReference,className.toCharArray());
		}
	}
	
	void addDescription(final Class<?> clazz, final boolean protectedAndPrrivate) throws ContentException {
		if (clazz == null) {
			throw new IllegalArgumentException("Class to add can'tbe null");
		}
		else {
			addClassDescription(stack.get(0).repoLong,clazz,protectedAndPrrivate);
		}
	}
	
	Class<?> getClassDescription(final char[] data, final int from, final int to) throws ContentException {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("Data char array can't be null or zero length");
		}
		else if (to <= data.length && data[to-1] == ']') {	// Dynamic building classes for the array type
			int		index, depth = 0;
			
			for (index = to-1; index > from && (data[index] == '[' || data[index] == ']'); index--) {
				if (data[index] == '[') {
					depth++;
				}
			}
			final Class<?>	temp = getDescription(data,from,index+1,KIND_CLASS,KeeperContent.IsClass,Class.class);
			
			return Array.newInstance(temp,new int[depth]).getClass();
		}
		else {
			return getDescription(data,from,to,KIND_CLASS,KeeperContent.IsClass,Class.class);
		}
	}

	Field getFieldDescription(final char[] data, final int from, final int to) throws ContentException {
		return getDescription(data,from,to,KIND_FIELD,KeeperContent.IsField,Field.class);
	}

	Method getMethodDescription(final char[] data, final int from, final int to) throws ContentException {
		return getDescription(data,from,to,KIND_METHOD,KeeperContent.IsMethod,Method.class);
	}

	Constructor<?> getConstructorDescription(final char[] data, final int from, final int to) throws ContentException {
		return getDescription(data,from,to,KIND_CONSTRUCTOR,KeeperContent.isConstructor,Constructor.class);
	}
	
	void push() {
		stack.add(0,new RepoStack(new AndOrTree<Keeper>(2,16)));
	}

	void pop() {
		if (stack.size() == 0) {
			throw new IllegalStateException("Pop repo stack exhausted");
		}
		else {
			final RepoStack	item = stack.remove(0);
			
			item.repoLong.clear();
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getDescription(final char[] data, final int from, final int to, final String content, final KeeperContent type, final Class<T> result) throws ContentException {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("Data char array cant be null or zero length");
		}
		else if (from < 0 || from >= data.length) {
			throw new IllegalArgumentException("From location ["+from+"] outside the bounds 0.."+data.length);
		}
		else if (to < 0 || to > data.length) {
			throw new IllegalArgumentException("To location ["+to+"] outside the bounds 0.."+data.length);
		}
		else {
			long	id;

			for (int index = stack.size()-1; index >= 0; index--) {
				if ((id = stack.get(index).repoLong.seekName(data,from,to)) >= 0) {
					if (stack.get(index).repoLong.getCargo(id).content == type) {
						return (T) stack.get(index).repoLong.getCargo(id).data;
					}
					else {
						throw new ContentException("Qualified name ["+new String(data,from,to-from)+"] is not a "+content+", but ["+stack.get(index).repoLong.getCargo(id).content+"]");
					}
				}
			}
			
			// Search the most similar name in the list and throw exception
			final List<NameAndPrescription>	allMethods = new ArrayList<>();	
			final char[]					methodNameArray = Arrays.copyOfRange(data,from,to);
			final String					methodName = new String(methodNameArray);
			
			for (int index = stack.size()-1; index >= 0; index--) {
				stack.get(index).repoLong.walk((name,len,nodeId,cargo)->{allMethods.add(new NameAndPrescription(new String(name,0,len),CharUtils.calcLevenstain(methodNameArray,Arrays.copyOfRange(name,0,len)))); return true;});
			}				
			final NameAndPrescription[]		list = allMethods.toArray(new NameAndPrescription[allMethods.size()]);

			Arrays.sort(list, new Comparator<NameAndPrescription>(){
												@Override
												public int compare(NameAndPrescription o1, NameAndPrescription o2) {
													return o1.prescription.distance - o2.prescription.distance;
												}
											});
			allMethods.clear();
			
			throw new ContentException(" Name ["+methodName+"] is unknown or illegal, use import directive to load it's description.\nPossibly, ["+list[0].name+"] you need?");
		}
	}

	private static void addClassDescription(/*final SyntaxTreeInterface<Keeper> shortTree,*/ final SyntaxTreeInterface<Keeper> longTree, final Class<?> clazz, final boolean protectedAndPrrivate) throws ContentException {
		final String	clazzName = clazz.getName();
		
		if (clazzName.contains("$")) {
			final String	simpleName = clazzName.substring(clazzName.indexOf('$')+1);
			
			walkFields(clazz,new WalkCallback<Field>(){
				@Override
				public void processEntity(Field entity) throws ContentException {
					addFieldDescription(/*shortTree,*/longTree,clazzName,entity);
				}
			}, protectedAndPrrivate, longTree);
			walkMethods(clazz,new WalkCallback<Method>(){
				@Override
				public void processEntity(Method entity) throws ContentException {
					addMethodDescription(/*shortTree,*/longTree,clazzName,entity);
				}
			}, protectedAndPrrivate, longTree);
			for (Constructor<?> c : clazz.getDeclaredConstructors()) {
				addConstructorDescription(/*shortTree,*/longTree,clazzName,c);
			}
			addAnyDescription(/*shortTree,*/longTree,KIND_CLASS,clazzName,simpleName,KeeperContent.IsClass,clazz);	// Important! Class must be added AFTER it's components
		}
		else {
			walkFields(clazz,new WalkCallback<Field>(){
				@Override
				public void processEntity(Field entity) throws ContentException {
					addFieldDescription(/*shortTree,*/longTree,clazzName,entity);
				}
			}, protectedAndPrrivate, longTree);
			walkMethods(clazz,new WalkCallback<Method>(){
				@Override
				public void processEntity(Method entity) throws ContentException {
					addMethodDescription(/*shortTree,*/longTree,clazzName,entity);
				}
			}, protectedAndPrrivate, longTree);
			for (Constructor<?> c : clazz.getDeclaredConstructors()) {
				addConstructorDescription(/*shortTree,*/longTree,clazzName,c);
			}
			addAnyDescription(/*shortTree,*/longTree,KIND_CLASS,clazzName,clazz.getSimpleName(),KeeperContent.IsClass,clazz);	// Important! Class must be added AFTER it's components
		}
	}

	private static void walkFields(final Class<?> node, final WalkCallback<Field> callback, final boolean processPublicAndPrivate, final SyntaxTreeInterface<Keeper> longTree) throws ContentException {
		if (node != null && longTree.seekName(node.getCanonicalName() != null ? node.getCanonicalName() : node.getName()) < 0) {
			for (final Field f : node.getDeclaredFields()) {
				if (Modifier.isPublic(f.getModifiers()) || processPublicAndPrivate) {
					callback.processEntity(f);
				}
			}
			if (node.isInterface()) {
				for (Class<?> implementer : node.getInterfaces()) {
					walkFields(implementer,callback,true,longTree);
				}
			}
			else {
				walkFields(node.getSuperclass(),callback,processPublicAndPrivate,longTree);
			}
		}
	}

	private static void walkMethods(final Class<?> node, final WalkCallback<Method> callback, final boolean processPublicAndPrivate, final SyntaxTreeInterface<Keeper> longTree) throws ContentException {
		if (node != null && longTree.seekName(node.getCanonicalName() != null ? node.getCanonicalName() : node.getName()) < 0) {
			for (final Method m : node.getDeclaredMethods()) {
				if (Modifier.isPublic(m.getModifiers()) || processPublicAndPrivate) {
					callback.processEntity(m);
				}
			}
			if (node.isInterface()) {
				for (Class<?> implementer : node.getInterfaces()) {
					walkMethods(implementer,callback,true,longTree);
				}
			}
			else {
				walkMethods(node.getSuperclass(),callback,processPublicAndPrivate,longTree);
			}
		}
	}
	
	private static void addFieldDescription(final SyntaxTreeInterface<Keeper> longTree, final String className, final Field f) throws ContentException {
		addAnyDescription(longTree,KIND_FIELD,className+'.'+f.getName(),f.getName(),KeeperContent.IsField,f);
	}

	private static void addMethodDescription(final SyntaxTreeInterface<Keeper> longTree, final String className, final Method m) throws ContentException {
		addAnyDescription(longTree,KIND_METHOD,className+'.'+m.getName()+InternalUtils.buildSignature(m),m.getName()+InternalUtils.buildSignature(m),KeeperContent.IsMethod,m);
	}

	private static void addConstructorDescription(final SyntaxTreeInterface<Keeper> longTree, final String className, final Constructor<?> c) throws ContentException {
		addAnyDescription(longTree,KIND_CONSTRUCTOR,className+'.'+c.getDeclaringClass().getSimpleName()+InternalUtils.buildSignature(c),c.getDeclaringClass().getSimpleName()+InternalUtils.buildSignature(c),KeeperContent.isConstructor,c);
		addAnyDescription(longTree,KIND_CONSTRUCTOR,className+".<init>"+InternalUtils.buildSignature(c),c.getDeclaringClass().getSimpleName()+InternalUtils.buildSignature(c),KeeperContent.isConstructor,c);
	}

	private static void addAnyDescription(final SyntaxTreeInterface<Keeper> longTree, final String entityType, final String qualifiedName, final String simpleName, final KeeperContent context, final Object entity) throws ContentException {
		if (longTree.seekName(qualifiedName) >= 0) {
		}
		else {
			final Keeper	newKeeper = new Keeper(entity,context);
			
			longTree.placeName(qualifiedName,newKeeper);
		}
	}


    private static class NameAndPrescription {
    	public final String			name;
    	public final Prescription	prescription;
    	
		public NameAndPrescription(final String name, final Prescription prescription) {
			this.name = name;
			this.prescription = prescription;
		}

		@Override
		public String toString() {
			return "NameAndPrescription [name=" + name + ", prescription=" + prescription + "]";
		}
    }
    
	private enum KeeperContent {
		IsClass, IsField, IsMethod, isConstructor
	}
	
	private static class Keeper {
		final KeeperContent	content;
		final Object		data;

		public Keeper(final Object data, final KeeperContent content) {
			this.content = content;
			this.data = data;
		}
		
		@Override public String toString() {return "Keeper [content=" + content + ", data=" + data + "]";}		
	}
	
	private class RepoStack {
		final SyntaxTreeInterface<Keeper>	repoLong;
		
		RepoStack(SyntaxTreeInterface<Keeper> repoLong) {
			this.repoLong = repoLong;
		}
	}
}
