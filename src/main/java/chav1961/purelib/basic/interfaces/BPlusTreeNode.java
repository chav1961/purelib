package chav1961.purelib.basic.interfaces;

import java.util.function.Consumer;

public interface BPlusTreeNode<K extends Comparable<? super K>,V> {
	boolean isLeaf();
	
	boolean containsKey(K key);
	
	boolean containsKeyGE(K key);
	boolean containsKeyLE(K key);
	int getKeysGE(K key,Consumer<K> accept);
	int getKeysLE(K key,Consumer<K> accept);
	K getFirstLeafKey();
	K getLastLeafKey();

	V getValue(K key);
	
	V delete(K key);
	void insert(K key, V value);

	boolean canInsert();
	boolean canCompact();
	
	void join();
}
