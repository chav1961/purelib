package chav1961.purelib.basic.interfaces;

public interface LongBPlusTreeNode<K extends Comparable<? super K>,V> extends BPlusTreeNode<K, V>{
	long getIdForKey(K key);
	long getIdForKeyGE(K key);
	long getIdForKeyLE(K key);
	long getNextSiblingId();
	long getPrevSiblingId();
	void split(long left, long right);
}