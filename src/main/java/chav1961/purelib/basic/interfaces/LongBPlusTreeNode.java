package chav1961.purelib.basic.interfaces;

public interface LongBPlusTreeNode<K extends Comparable<? super K>,V> extends BPlusTreeNode<K, V>{
	long getIdForKey(K key);
	long getIdForKeyGE(K key);
	long getIdForKeyLE(K key);
	long getCurrentId();
	long getNextSiblingId();
	long getPrevSiblingId();
	LongBPlusTreeNode<K,V>[] split(long left, long right);
}