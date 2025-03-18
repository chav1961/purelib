package chav1961.purelib.basic.intern;

public interface ObjectBPlusTreeNode<Id,K extends Comparable<? super K>,V> extends BPlusTreeNode<K, V>{
	Id getIdForKey(K key);
	Id getIdForKeyGE(K key);
	Id getIdForKeyLE(K key);
	Id getNextSiblingId();
	Id getPrevSiblingId();
	void split(Id left, Id right);
}