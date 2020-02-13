package chav1961.purelib.basic.interfaces;

import java.util.List;

public interface BPlusTreeNode<Id,K extends Comparable<? super K>,V> {
	Id getNodeId();
	
	int keyNumber();

	V getValue(K key);

	void deleteValue(K key);

	void insertValue(K key, V value);

	K getFirstLeafKey();

	List<V> getRange(K key1, boolean includeKey1, K key2, boolean includeKey2);

	void merge(BPlusTreeNode<Id,K,V> sibling);

	BPlusTreeNode<Id,K,V> split();

	boolean isOverflow();

	boolean isUnderflow();
}