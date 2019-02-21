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
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;


class ClassDescriptionRepo {
	private static final String					KIND_CLASS = "class";
	private static final String					KIND_FIELD = "field";
	private static final String					KIND_METHOD = "method";
	private static final String					KIND_CONSTRUCTOR = "constructor";

	private static final int 					LEV_DELETE = 1;
	private static final int 					LEV_REPLACE = 2;
	private static final int 					LEV_INSERT = 3;
	private static final int 					LEV_NONE = 4;
	
	private static final Class<?>[]				PRELOADED_CLASSES = new Class<?>[]{boolean.class,byte.class,char.class,double.class,float.class,int.class,long.class,short.class,void.class,
													Object.class,String.class,Throwable.class,Class.class
												};

	
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
//		stack.add(0,new RepoStack(new AndOrTree<Keeper>(1,16),new AndOrTree<Keeper>(2,16)));
		for (Class<?> item : PRELOADED_CLASSES) {
			addClassDescription(/*stack.get(0).repoShort,*/stack.get(0).repoLong,item,false);
		}
	}

	ClassDescriptionRepo(final Writer diagnostics) throws ContentException {
		this.diagnostics = diagnostics;
		stack.add(0,new RepoStack(new AndOrTree<Keeper>(2,16)));
//		stack.add(0,new RepoStack(new AndOrTree<Keeper>(1,16),new AndOrTree<Keeper>(2,16)));
		for (Class<?> item : PRELOADED_CLASSES) {
			addClassDescription(/*stack.get(0).repoShort,*/stack.get(0).repoLong,item,false);
		}
	}
	
	void addDescription(final Class<?> clazz, final boolean protectedAndPrrivate) throws ContentException {
		if (clazz == null) {
			throw new IllegalArgumentException("Class to add can'tbe null");
		}
		else {
			addClassDescription(/*stack.get(0).repoShort,*/stack.get(0).repoLong,clazz,protectedAndPrrivate);
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
		stack.add(0,new RepoStack(/*new AndOrTree<Keeper>(1,16),*/new AndOrTree<Keeper>(2,16)));
	}

	void pop() {
		if (stack.size() == 0) {
			throw new IllegalStateException("Pop repo stack exhausted");
		}
		else {
			final RepoStack	item = stack.remove(0);
			
//			item.repoShort.clear();
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
//				if ((id = stack.get(index).repoShort.seekName(data,from,to)) >= 0) {
//					if (stack.get(index).repoShort.getCargo(id).content == type) {
//						if (stack.get(index).repoShort.getCargo(id).useCounter > 1) {
//							throw new ContentException("Short name ["+new String(data,from,to-from)+"] is ambigious, use qualified name instead!");
//						}
//						else {
//							return (T) stack.get(index).repoShort.getCargo(id).data;
//						}
//					}
//					else {
//						throw new ContentException("Short name ["+new String(data,from,to-from)+"] is not a "+content+", but ["+stack.get(index).repoShort.getCargo(id).content+"]");
//					}
//				}
//				else if ((id = stack.get(index).repoLong.seekName(data,from,to)) >= 0) {
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
				stack.get(index).repoLong.walk((name,len,nodeId,cargo)->{allMethods.add(new NameAndPrescription(new String(name,0,len),calcLevenstain(methodNameArray,Arrays.copyOfRange(name,0,len)))); return true;});
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
	
	private static void addFieldDescription(/*final SyntaxTreeInterface<Keeper> shortTree,*/ final SyntaxTreeInterface<Keeper> longTree, final String className, final Field f) throws ContentException {
		addAnyDescription(/*shortTree,*/longTree,KIND_FIELD,className+'.'+f.getName(),f.getName(),KeeperContent.IsField,f);
	}

	private static void addMethodDescription(/*final SyntaxTreeInterface<Keeper> shortTree,*/ final SyntaxTreeInterface<Keeper> longTree, final String className, final Method m) throws ContentException {
		addAnyDescription(/*shortTree,*/longTree,KIND_METHOD,className+'.'+m.getName()+InternalUtils.buildSignature(m),m.getName()+InternalUtils.buildSignature(m),KeeperContent.IsMethod,m);
	}

	private static void addConstructorDescription(/*final SyntaxTreeInterface<Keeper> shortTree,*/ final SyntaxTreeInterface<Keeper> longTree, final String className, final Constructor<?> c) throws ContentException {
		addAnyDescription(/*shortTree,*/longTree,KIND_CONSTRUCTOR,className+'.'+c.getDeclaringClass().getSimpleName()+InternalUtils.buildSignature(c),c.getDeclaringClass().getSimpleName()+InternalUtils.buildSignature(c),KeeperContent.isConstructor,c);
		addAnyDescription(/*shortTree,*/longTree,KIND_CONSTRUCTOR,className+".<init>"+InternalUtils.buildSignature(c),c.getDeclaringClass().getSimpleName()+InternalUtils.buildSignature(c),KeeperContent.isConstructor,c);
	}

	private static void addAnyDescription(/*final SyntaxTreeInterface<Keeper> shortTree,*/ final SyntaxTreeInterface<Keeper> longTree, final String entityType, final String qualifiedName, final String simpleName, final KeeperContent context, final Object entity) throws ContentException {
//		long			id;
		
		if (longTree.seekName(qualifiedName) >= 0) {
//		if ((id = longTree.seekName(qualifiedName)) >= 0) {
//			throw new ContentException("Duplicate description for the "+entityType+" ["+new String(qualifiedName)+"] was detected during import");
		}
//		else if ((id = shortTree.seekName(simpleName)) >= 0) {
//			final Keeper	oldKeeper = shortTree.getCargo(id);
//			
//			oldKeeper.useCounter++;
//			longTree.placeName(qualifiedName,oldKeeper);
//		}
		else {
			final Keeper	newKeeper = new Keeper(entity,context);
			
//			shortTree.placeName(simpleName,newKeeper);
			longTree.placeName(qualifiedName,newKeeper);
		}
	}

	// see https://ru.wikibooks.org/wiki/%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D0%B8_%D0%B0%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC%D0%BE%D0%B2/%D0%A0%D0%B5%D0%B4%D0%B0%D0%BA%D1%86%D0%B8%D0%BE%D0%BD%D0%BD%D0%BE%D0%B5_%D0%BF%D1%80%D0%B5%D0%B4%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5 
    private static Prescription calcLevenstain(final char[] str1, final char[] str2) {
		final int 		m = str1.length, n = str2.length;
		final int[][] 	D = new int[m + 1][n + 1];
		final char[][] 	P = new char[m + 1][n + 1];
	
		for (int i = 0; i <= m; i++) {
			D[i][0] = i;
			P[i][0] = 'D';
		}
		for (int i = 0; i <= n; i++) {
			D[0][i] = i;
			P[0][i] = 'I';
		}
	
		for (int i = 1; i <= m; i++) {
	            for (int j = 1; j <= n; j++) {
	                final int cost = str1[i - 1] != str2[j - 1] ? 1 : 0;
	
	                if(D[i][j - 1] < D[i - 1][j] && D[i][j - 1] < D[i - 1][j - 1] + cost) {
	                    D[i][j] = D[i][j - 1] + 1;
	                    P[i][j] = 'I';
	                }
	                else if(D[i - 1][j] < D[i - 1][j - 1] + cost) {
	                    D[i][j] = D[i - 1][j] + 1;
	                    P[i][j] = 'D';
	                }
	                else {
	                    D[i][j] = D[i - 1][j - 1] + cost;
	                    P[i][j] = (cost == 1) ? 'R' : 'M';
	                }
	            }
	        }
	
		final List<int[]> opers = new ArrayList<>();
		int i = m, j = n;
	        
		do {char c = P[i][j];
	            if(c == 'R' || c == 'M') {
	                opers.add(0,new int[]{c == 'M' ? LEV_NONE : LEV_REPLACE,i,j});
	                i --;
	                j --;
	            }
	            else if(c == 'D') {
	                opers.add(0,new int[]{LEV_DELETE,i,j});
	                i --;
	            }
	            else {
	                opers.add(0,new int[]{LEV_INSERT,i,j});
	                j --;
	            }
		} while((i != 0) || (j != 0));
	        
		return new Prescription(D[m][n], opers.toArray(new int[opers.size()][]));
    }

    private static class Prescription {
		public int[][] route;
		public int distance;
	        
		Prescription(int distance, int[][] route) {
			this.distance = distance;
			this.route = route;
		}
	
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Prescription(dist="+distance+") :");
     
            for (int index = 0; index < route.length; index++) {
                sb.append('\n').append(index).append(" : ");
                switch (route[index][0]) {
                    case LEV_DELETE : sb.append("delete "); break;
                    case LEV_REPLACE : sb.append("replace "); break;
                    case LEV_INSERT : sb.append("insert "); break;
                    case LEV_NONE : sb.append("not changed "); break;
                }
                sb.append(' ').append(route[index][1]).append(" and ").append(route[index][2]);
            }
            return sb.toString();
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
//		int					useCounter = 1;

		public Keeper(final Object data, final KeeperContent content) {
			this.content = content;
			this.data = data;
		}
		
		@Override public String toString() {return "Keeper [content=" + content + ", data=" + data + "]";}		
	}
	
	private class RepoStack {
//		final SyntaxTreeInterface<Keeper>	repoShort;
		final SyntaxTreeInterface<Keeper>	repoLong;
		
		RepoStack(SyntaxTreeInterface<Keeper> repoLong) {
//		RepoStack(SyntaxTreeInterface<Keeper> repoShort, SyntaxTreeInterface<Keeper> repoLong) {
//			this.repoShort = repoShort;
			this.repoLong = repoLong;
		}
	}
}
