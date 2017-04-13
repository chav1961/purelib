package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;

class LongIdTree <T> {
	private static final int[]	SCALES = new int[]{1,3,5,7,11};
	
	private final int			idSize;
	private LongIdTreeNode<T>			node = null;
	
	LongIdTree(final int idSize) {
		if (idSize <= 0 || idSize > SCALES.length) {
			throw new IllegalArgumentException("Declaredparameters size ["+idSize+"] is outside the range 1.."+SCALES.length);
		}
		else {
			this.idSize = idSize;
		}
	}
	
	short getRef(final long... parameters) {
		if (parameters.length != idSize) {
			throw new IllegalArgumentException("Parameters size ["+parameters.length+"] differ with declared size ["+idSize+"]");
		}
		else {
			return getRef(node,getHash(parameters),parameters);
		}
	}

	void addRef(final short newRef, final long... parameters) {
		if (newRef == 0) {
			throw new IllegalArgumentException("Zero newRef is a special marker and can't be used for new nodes!");
		}
		else if (parameters.length != idSize) {
			throw new IllegalArgumentException("Parameters size ["+parameters.length+"] differ with declared size ["+idSize+"]");
		}
		else if (node == null) {
			node = new LongIdTreeNode<T>(newRef,parameters);
		}
		else {
			addRef(node,newRef,getHash(parameters),parameters);
		}
	}

	T getCargo(final long... parameters) {
		if (parameters.length != idSize) {
			throw new IllegalArgumentException("Parameters size ["+parameters.length+"] differ with declared size ["+idSize+"]");
		}
		else {
			return getCargo(node,getHash(parameters),parameters);
		}
	}

	
	boolean setCargo(final T cargo, final long... parameters) {
		if (parameters.length != idSize) {
			throw new IllegalArgumentException("Parameters size ["+parameters.length+"] differ with declared size ["+idSize+"]");
		}
		else {
			return setCargo(node,cargo,getHash(parameters),parameters);
		}
	}
	
	
	void walk(final LongIdTreeWalker<T> callback) throws IOException {
		walk(node,callback);
	}
	
	void clear() {
		clear(node);
		node = null;
	}
	
	private static short getRef(LongIdTreeNode<?> node, final long hash, final long[] keys) {
		if (node != null) {
			if (node.hash < hash) {
				return getRef(node.left,hash,keys);
			}
			else if (node.hash > hash) {
				return getRef(node.right,hash,keys);
			}
			else {
				while (node.hash == hash) {
					if (compare(node.keys,keys)) {
						return node.ref;
					}
					else {
						node = node.right;
					}
				}
				return getRef(node,hash,keys);
			}
		}
		else {
			return 0;
		}
	}

	private static <T> void addRef(final LongIdTreeNode<T> node, final short newRef, final long hash, final long[] parameters) {
		if (node.hash < hash) {
			if (node.left == null) {
				node.left = new LongIdTreeNode<T>(newRef,parameters);
			}
			else {
				addRef(node.left,newRef,hash,parameters);
			}
		}
		else if (node.hash > hash) {
			if (node.right == null) {
				node.right = new LongIdTreeNode<T>(newRef,parameters);
			}
			else {
				addRef(node.right,newRef,hash,parameters);
			}
		}
		else {
			final LongIdTreeNode<T> newNode = new LongIdTreeNode<T>(newRef,parameters);
			
			newNode.right = node.right;
			node.right = newNode;
		}
	}

	private static <T> T getCargo(LongIdTreeNode<T> node, final long hash, final long[] keys) {
		if (node != null) {
			if (node.hash < hash) {
				return getCargo(node.left,hash,keys);
			}
			else if (node.hash > hash) {
				return getCargo(node.right,hash,keys);
			}
			else {
				while (node.hash == hash) {
					if (compare(node.keys,keys)) {
						return node.cargo;
					}
					else {
						node = node.right;
					}
				}
				return getCargo(node,hash,keys);
			}
		}
		else {
			return null;
		}
	}

	private static <T> boolean setCargo(LongIdTreeNode<T> node, final T cargo, final long hash, final long[] keys) {
		if (node != null) {
			if (node.hash < hash) {
				return setCargo(node.left,cargo,hash,keys);
			}
			else if (node.hash > hash) {
				return setCargo(node.right,cargo,hash,keys);
			}
			else {
				while (node.hash == hash) {
					if (compare(node.keys,keys)) {
						node.cargo = cargo;
						return true;
					}
					else {
						node = node.right;
					}
				}
				return setCargo(node,cargo,hash,keys);
			}
		}
		else {
			return false;
		}
	}

	private static <T> void walk(final LongIdTreeNode<T> node, final LongIdTreeWalker<T> callback) throws IOException {
		if (node != null) {
			walk(node.left,callback);
			callback.process(node);
			walk(node.right,callback);
		}
	}

	
	private static <T> void clear(final LongIdTreeNode<T> node) {
		if (node != null) {
			clear(node.left);	node.left = null;
			clear(node.right);	node.right = null;
		}
	}
	
	private static long getHash(final long... keys) {
		long		hash = 1;
		
		for (int index = 0, maxIndex = keys.length; index < maxIndex; index++) {
			hash *= SCALES[index] * keys[index];
		}
		return hash;
	}

	private static boolean compare(final long[] left, final long[] right) {
		for (int index = 0, maxIndex = left.length; index < maxIndex; index++) {
			if (left[index] != right[index]) {
				return false;
			}
		}
		return true;
	}
	
	static class LongIdTreeNode<T> {
		final long		hash;
		final long[]	keys;
		final short		ref;
		LongIdTreeNode<T>		left = null, right = null;
		T				cargo = null;
		
		public LongIdTreeNode(short ref, final long... keys) {
			this.hash = getHash(keys);
			this.keys = keys;
			this.ref = ref;
		}
	}
}
