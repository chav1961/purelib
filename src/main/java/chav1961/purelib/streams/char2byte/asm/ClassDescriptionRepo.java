package chav1961.purelib.streams.char2byte.asm;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.AsmSyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;


class ClassDescriptionRepo {
	private static final String				KIND_CLASS = "class";
	private static final String				KIND_FIELD = "field";
	private static final String				KIND_METHOD = "method";
	private static final String				KIND_CONSTRUCTOR = "constructor";
	
	private final SyntaxTreeInterface<Keeper>		repoShort = new AndOrTree<Keeper>(1);
	private final SyntaxTreeInterface<Keeper>		repoLong = new AndOrTree<Keeper>(2);

	ClassDescriptionRepo() throws AsmSyntaxException {
	}
	
	void addDescription(final Class<?> clazz) throws AsmSyntaxException {
		if (clazz == null) {
			throw new IllegalArgumentException("Class to add can'tbe null");
		}
		else {
			addClassDescription(repoShort,repoLong,clazz);
		}
	}
	
	Class<?> getClassDescription(final char[] data, final int from, final int to) throws AsmSyntaxException {
		return getDescription(data,from,to,KIND_CLASS,KeeperContent.IsClass,Class.class);
	}

	Field getFieldDescription(final char[] data, final int from, final int to) throws AsmSyntaxException {
		return getDescription(data,from,to,KIND_FIELD,KeeperContent.IsField,Field.class);
	}

	Method getMethodDescription(final char[] data, final int from, final int to) throws AsmSyntaxException {
		return getDescription(data,from,to,KIND_METHOD,KeeperContent.IsMethod,Method.class);
	}

	Constructor<?> getConstructorDescription(final char[] data, final int from, final int to) throws AsmSyntaxException {
		return getDescription(data,from,to,KIND_CONSTRUCTOR,KeeperContent.isConstructor,Constructor.class);
	}
	
	private <T> T getDescription(final char[] data, final int from, final int to, final String content, final KeeperContent type, final Class<T> result) throws AsmSyntaxException {
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
			
			if ((id = repoShort.seekName(data,from,to)) != -1) {
				if (repoShort.getCargo(id).content == type) {
					if (repoShort.getCargo(id).useCounter > 1) {
						throw new AsmSyntaxException("Short name ["+new String(data,from,to-from)+"] is ambigious, use qualified name instead!");
					}
					else {
						return (T) repoShort.getCargo(id).data;
					}
				}
				else {
					throw new AsmSyntaxException("Short name ["+new String(data,from,to-from)+"] is not a "+content+", but ["+repoShort.getCargo(id).content+"]");
				}
			}
			else if ((id = repoLong.seekName(data,from,to)) != -1) {
				if (repoLong.getCargo(id).content == type) {
					return (T) repoLong.getCargo(id).data;
				}
				else {
					throw new AsmSyntaxException("Qualified name ["+new String(data,from,to-from)+"] is not a "+content+", but ["+repoLong.getCargo(id).content+"]");
				}
			}
			else {
				throw new AsmSyntaxException(content+" name ["+new String(data,from,to-from)+"] is unknown, use import directive to load it's description");
			}
		}
	}

	private static void addClassDescription(final SyntaxTreeInterface<Keeper> shortTree, final SyntaxTreeInterface<Keeper> longTree, final Class<?> clazz) throws AsmSyntaxException {
		addAnyDescription(shortTree,longTree,"class",clazz.getPackage() != null ? clazz.getPackage().getName() : "",clazz.getSimpleName(),KeeperContent.IsClass,clazz);
		for (Field f : clazz.getFields()) {
			addFieldDescription(shortTree,longTree,clazz.getCanonicalName(),f);
		}
		for (Method m : clazz.getMethods()) {
			addMethodDescription(shortTree,longTree,clazz.getCanonicalName(),m);
		}
		for (Constructor<?> c : clazz.getConstructors()) {
			addConstructorDescription(shortTree,longTree,clazz.getCanonicalName(),c);
		}
	}

	private static void addFieldDescription(final SyntaxTreeInterface<Keeper> shortTree, final SyntaxTreeInterface<Keeper> longTree, final String className, final Field f) throws AsmSyntaxException {
		addAnyDescription(shortTree,longTree,"field",className,f.getName(),KeeperContent.IsField,f);
	}

	private static void addMethodDescription(final SyntaxTreeInterface<Keeper> shortTree, final SyntaxTreeInterface<Keeper> longTree, final String className, final Method m) throws AsmSyntaxException {
		addAnyDescription(shortTree,longTree,"method",className,m.getName()+InternalUtils.buildSignature(m),KeeperContent.IsMethod,m);
	}

	private static void addConstructorDescription(final SyntaxTreeInterface<Keeper> shortTree, final SyntaxTreeInterface<Keeper> longTree, final String className, final Constructor<?> c) throws AsmSyntaxException {
		addAnyDescription(shortTree,longTree,"constructor",className,c.getDeclaringClass().getSimpleName()+InternalUtils.buildSignature(c),KeeperContent.isConstructor,c);
	}

	private static void addAnyDescription(final SyntaxTreeInterface<Keeper> shortTree, final SyntaxTreeInterface<Keeper> longTree, final String entityType, final String className, final String entityName, final KeeperContent context, final Object entity) throws AsmSyntaxException {
		final char[]	simpleName = entityName.toCharArray(), qualifiedName = (className+'.'+entityName).toCharArray();
		long			id;
		
		if (longTree.seekName(qualifiedName,0,qualifiedName.length) != -1) {
			throw new AsmSyntaxException("Duplicate description for the "+entityType+" ["+new String(qualifiedName)+"] was detected during import");
		}
		else if ((id = shortTree.seekName(simpleName,0,simpleName.length)) != -1) {
			final Keeper	oldKeeper = shortTree.getCargo(id);
			
			oldKeeper.useCounter++;
			longTree.placeName(qualifiedName,0,qualifiedName.length,oldKeeper);
		}
		else {
			final Keeper	newKeeper = new Keeper(entity,context);
			
			shortTree.placeName(simpleName,0,simpleName.length,newKeeper);
			longTree.placeName(qualifiedName,0,qualifiedName.length,newKeeper);
		}
	}
	
	private enum KeeperContent {
		IsClass, IsField, IsMethod, isConstructor
	}
	
	private static class Keeper {
		final KeeperContent	content;
		final Object		data;
		int					useCounter = 1;

		public Keeper(final Object data, final KeeperContent content) {
			this.content = content;
			this.data = data;
		}
		
		@Override public String toString() {return "Keeper [content=" + content + ", data=" + data + "]";}		
	}
}
