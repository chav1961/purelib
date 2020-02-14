package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.BPlusTreeNode;

public class BPlusTree <NodeId,K extends Comparable<? super K>,V> {
	private static final int DEFAULT_NODE_CAPACITYFACTOR = 128;

	public static class BPlusTreeConteneException extends ContentException {
		private static final long serialVersionUID = -5557152301088161210L;

		public BPlusTreeConteneException() {
			super();
		}

		public BPlusTreeConteneException(String message, Throwable cause) {
			super(message, cause);
		}

		public BPlusTreeConteneException(String message) {
			super(message);
		}

		public BPlusTreeConteneException(Throwable cause) {
			super(cause);
		}
	}
	
	public interface NodeAccessor<Content,Id> {
		Id getRootId() throws BPlusTreeConteneException;
		Content getContent(Id id) throws BPlusTreeConteneException;
		Id createNewId() throws BPlusTreeConteneException;
		Content createContent(Id id) throws BPlusTreeConteneException;
		void storeContent(Id id, Content content) throws BPlusTreeConteneException;
		Content removeContent(Id id) throws BPlusTreeConteneException;
	}
		
		
		/**
		 * The branching factor for the B+ tree, that measures the capacity of nodes
		 * (i.e., the number of children nodes) for internal nodes in the tree.
		 */
		private int branchingFactor;

		/**
		 * The root node of the B+ tree.
		 */
		private Object root;

		public BPlusTree() {
			this(DEFAULT_NODE_CAPACITYFACTOR);
		}

		public BPlusTree(int branchingFactor) {
			this(branchingFactor,new InMemoryNodeAccessor<,NodeId>());
		}

		public BPlusTree(final int nodeCapacity, final NodeAccessor<BPlusTreeNode<NodeId,K,V>,NodeId> accessor) {
			if (nodeCapacity <= 2) {
				throw new IllegalArgumentException("Illegal branching factor: " + nodeCapacity);
			}
			else {
				this.branchingFactor = nodeCapacity;
//				this.root = new LeafNode<Node<NodeId,K,V>>();
			}
		}
		
		/**
		 * Returns the value to which the specified key is associated, or
		 * {@code null} if this tree contains no association for the key.
		 *
		 * <p>
		 * A return value of {@code null} does not <i>necessarily</i> indicate that
		 * the tree contains no association for the key; it's also possible that the
		 * tree explicitly associates the key to {@code null}.
		 * 
		 * @param key
		 *            the key whose associated value is to be returned
		 * 
		 * @return the value to which the specified key is associated, or
		 *         {@code null} if this tree contains no association for the key
		 */
		public V search(K key) {
			return ((BPlusTreeNode<NodeId, K, V>) root).getValue(key);
		}

		/**
		 * Returns the values associated with the keys specified by the range:
		 * {@code key1} and {@code key2}.
		 * 
		 * @param key1
		 *            the start key of the range
		 * @param policy1
		 *            the range policy, {@link RangePolicy#EXCLUSIVE} or
		 *            {@link RangePolicy#INCLUSIVE}
		 * @param key2
		 *            the end end of the range
		 * @param policy2
		 *            the range policy, {@link RangePolicy#EXCLUSIVE} or
		 *            {@link RangePolicy#INCLUSIVE}
		 * @return the values associated with the keys specified by the range:
		 *         {@code key1} and {@code key2}
		 */
		public List<V> searchRange(K key1, boolean includeKey1, K key2, boolean includeKey2) {
			return ((BPlusTreeNode<NodeId, K, V>) root).getRange(key1, includeKey1, key2, includeKey2);
		}

		/**
		 * Associates the specified value with the specified key in this tree. If
		 * the tree previously contained a association for the key, the old value is
		 * replaced.
		 * 
		 * @param key
		 *            the key with which the specified value is to be associated
		 * @param value
		 *            the value to be associated with the specified key
		 */
		public void insert(K key, V value) {
			((BPlusTreeNode<NodeId, K, V>) root).insertValue(key, value);
		}

		/**
		 * Removes the association for the specified key from this tree if present.
		 * 
		 * @param key
		 *            the key whose association is to be removed from the tree
		 */
		public void delete(K key) {
			((BPlusTreeNode<NodeId,K,V>) root).deleteValue(key);
		}

		public String toString() {
			Queue<List<BPlusTreeNode<NodeId,K,V>>> queue = new LinkedList<List<BPlusTreeNode<NodeId,K,V>>>();
			queue.add(Arrays.asList(((BPlusTreeNode<NodeId,K,V>) root)));
			StringBuilder sb = new StringBuilder();
			while (!queue.isEmpty()) {
				Queue<List<BPlusTreeNode<NodeId,K,V>>> nextQueue = new LinkedList<List<BPlusTreeNode<NodeId,K,V>>>();
				while (!queue.isEmpty()) {
					List<BPlusTreeNode<NodeId,K,V>> nodes = queue.remove();
					sb.append('{');
					Iterator<BPlusTreeNode<NodeId,K,V>> it = nodes.iterator();
					while (it.hasNext()) {
						BPlusTreeNode<NodeId,K,V> node = it.next();
						sb.append(node.toString());
						if (it.hasNext())
							sb.append(", ");
						if (node instanceof BPlusTree.InternalNode)
							nextQueue.add(((InternalNode<NodeId,K,V>) node).children);
					}
					sb.append('}');
					if (!queue.isEmpty())
						sb.append(", ");
					else
						sb.append('\n');
				}
				queue = nextQueue;
			}

			return sb.toString();
		}

		private abstract class NodeImpl<Id,K extends Comparable<? super K>,V> implements BPlusTreeNode<Id,K,V>{
			List<K> keys;

			public Id getNodeId() {
				return null;
			}
			
			public int keyNumber() {
				return keys.size();
			}

			public abstract V getValue(K key);

			public abstract void deleteValue(K key);

			public abstract void insertValue(K key, V value);

			public abstract K getFirstLeafKey();

			public abstract List<V> getRange(K key1, boolean includeKey1, K key2, boolean includeKey2);

			public abstract void merge(BPlusTreeNode<Id,K,V> sibling);

			public abstract BPlusTreeNode<Id,K,V> split();

			public abstract boolean isOverflow();

			public abstract boolean isUnderflow();

			public String toString() {
				return keys.toString();
			}
		}

		private class InternalNode<Id,K extends Comparable<? super K>,V> extends NodeImpl<Id,K,V> {
			List<BPlusTreeNode<Id,K,V>> children;

			InternalNode() {
				this.keys = new ArrayList<K>();
				this.children = new ArrayList<BPlusTreeNode<Id,K,V>>();
			}

			@Override
			public V getValue(K key) {
				return getChild(key).getValue(key);
			}

			@Override
			public void deleteValue(K key) {
				BPlusTreeNode<Id, K, V> child = getChild(key);
				child.deleteValue(key);
				if (child.isUnderflow()) {
					BPlusTreeNode<Id, K, V> childLeftSibling = getChildLeftSibling(key);
					BPlusTreeNode<Id, K, V> childRightSibling = getChildRightSibling(key);
					BPlusTreeNode<Id, K, V> left = childLeftSibling != null ? childLeftSibling : child;
					BPlusTreeNode<Id, K, V> right = childLeftSibling != null ? child : childRightSibling;
					
					left.merge(right);
					deleteChild(right.getFirstLeafKey());
					if (left.isOverflow()) {
						BPlusTreeNode<Id, K, V> sibling = left.split();
						insertChild(sibling.getFirstLeafKey(), sibling);
					}
					if (((BPlusTreeNode<NodeId, K, V>) root).keyNumber() == 0)
						root = (BPlusTreeNode<NodeId, K, V>) left;
				}
			}

			@Override
			public void insertValue(K key, V value) {
				BPlusTreeNode<Id, K, V> child = getChild(key);
				child.insertValue(key, value);
				if (child.isOverflow()) {
					BPlusTreeNode<Id, K, V> sibling = child.split();
					insertChild(sibling.getFirstLeafKey(), sibling);
				}
				if (((BPlusTreeNode<NodeId, K, V>) root).isOverflow()) {
					BPlusTreeNode<Id, K, V> sibling = split();
					InternalNode<Id,K,V> newRoot = new InternalNode<>();
					newRoot.keys.add(sibling.getFirstLeafKey());
					newRoot.children.add(this);
					newRoot.children.add(sibling);
					root = (BPlusTreeNode<NodeId, K, V>) newRoot;
				}
			}

			@Override
			public K getFirstLeafKey() {
				return children.get(0).getFirstLeafKey();
			}

			@Override
			public List<V> getRange(K key1, boolean includeKey1, K key2, boolean includeKey2) {
				return getChild(key1).getRange(key1, includeKey1, key2, includeKey2);
			}

			@Override
			public void merge(BPlusTreeNode<Id, K, V> sibling) {
				InternalNode<Id,K,V> node = (InternalNode<Id,K,V>) sibling;
				keys.add(node.getFirstLeafKey());
				keys.addAll(node.keys);
				children.addAll(node.children);

			}

			@Override
			public BPlusTreeNode<Id, K, V> split() {
				int from = keyNumber() / 2 + 1, to = keyNumber();
				InternalNode<Id,K,V> sibling = new InternalNode<>();
				sibling.keys.addAll(keys.subList(from, to));
				sibling.children.addAll(children.subList(from, to + 1));

				keys.subList(from - 1, to).clear();
				children.subList(from, to + 1).clear();

				return sibling;
			}

			@Override
			public boolean isOverflow() {
				return children.size() > branchingFactor;
			}

			@Override
			public boolean isUnderflow() {
				return children.size() < (branchingFactor + 1) / 2;
			}

			BPlusTreeNode<Id, K, V> getChild(K key) {
				int loc = Collections.binarySearch(keys, key);
				int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
				return children.get(childIndex);
			}

			void deleteChild(K key) {
				int loc = Collections.binarySearch(keys, key);
				if (loc >= 0) {
					keys.remove(loc);
					children.remove(loc + 1);
				}
			}

			void insertChild(K key, BPlusTreeNode<Id, K, V> child) {
				int loc = Collections.binarySearch(keys, key);
				int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
				if (loc >= 0) {
					children.set(childIndex, child);
				} else {
					keys.add(childIndex, key);
					children.add(childIndex + 1, child);
				}
			}

			BPlusTreeNode<Id, K, V> getChildLeftSibling(K key) {
				int loc = Collections.binarySearch(keys, key);
				int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
				if (childIndex > 0)
					return children.get(childIndex - 1);

				return null;
			}

			BPlusTreeNode<Id, K, V> getChildRightSibling(K key) {
				int loc = Collections.binarySearch(keys, key);
				int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
				if (childIndex < keyNumber())
					return children.get(childIndex + 1);

				return null;
			}
		}

		private class LeafNode<Id,K extends Comparable<? super K>,V> extends NodeImpl<Id,K,V> {
			List<V> values;
			LeafNode<Id,K,V> next;

			LeafNode() {
				keys = new ArrayList<K>();
				values = new ArrayList<V>();
			}

			@Override
			public V getValue(K key) {
				int loc = Collections.binarySearch(keys, key);
				return loc >= 0 ? values.get(loc) : null;
			}

			@Override
			public void deleteValue(K key) {
				int loc = Collections.binarySearch(keys, key);
				if (loc >= 0) {
					keys.remove(loc);
					values.remove(loc);
				}
			}

			@Override
			public void insertValue(K key, V value) {
				int loc = Collections.binarySearch(keys, key);
				int valueIndex = loc >= 0 ? loc : -loc - 1;
				if (loc >= 0) {
					values.set(valueIndex, value);
				} else {
					keys.add(valueIndex, key);
					values.add(valueIndex, value);
				}
				if (((BPlusTreeNode<NodeId, K, V>) root).isOverflow()) {
					BPlusTreeNode<Id,K,V> sibling = split();
					InternalNode<Id,K,V> newRoot = new InternalNode<>();
					newRoot.keys.add(sibling.getFirstLeafKey());
					newRoot.children.add(this);
					newRoot.children.add(sibling);
					root = newRoot;
				}
			}

			@Override
			public K getFirstLeafKey() {
				return keys.get(0);
			}

			@Override
			public List<V> getRange(K key1, boolean includeKey1, K key2, boolean includeKey2) {
				List<V> result = new LinkedList<V>();
				LeafNode<Id,K,V> node = this;
				while (node != null) {
					Iterator<K> kIt = node.keys.iterator();
					Iterator<V> vIt = node.values.iterator();
					while (kIt.hasNext()) {
						K key = kIt.next();
						V value = vIt.next();
						int cmp1 = key.compareTo(key1);
						int cmp2 = key.compareTo(key2);
						if (((!includeKey1 && cmp1 > 0) || (includeKey1 && cmp1 >= 0))
								&& ((!includeKey2 && cmp2 < 0) || (includeKey2 && cmp2 <= 0)))
							result.add(value);
						else if ((!includeKey2 && cmp2 >= 0) || (includeKey2 && cmp2 > 0))
							return result;
					}
					node = node.next;
				}
				return result;
			}

			@Override
			public void merge(BPlusTreeNode<Id,K,V> sibling) {
				@SuppressWarnings("unchecked")
				LeafNode<Id,K,V> node = (LeafNode<Id,K,V>) sibling;
				keys.addAll(node.keys);
				values.addAll(node.values);
				next = node.next;
			}

			@Override
			public BPlusTreeNode<Id,K,V> split() {
				LeafNode<Id,K,V> sibling = new LeafNode<>();
				int from = (keyNumber() + 1) / 2, to = keyNumber();
				sibling.keys.addAll(keys.subList(from, to));
				sibling.values.addAll(values.subList(from, to));

				keys.subList(from, to).clear();
				values.subList(from, to).clear();

				sibling.next = next;
				next = sibling;
				return sibling;
			}

			@Override
			public boolean isOverflow() {
				return values.size() > branchingFactor - 1;
			}

			@Override
			public boolean isUnderflow() {
				return values.size() < branchingFactor / 2;
			}
		}
		
	private static class InMemoryNodeAccessor<Id,K extends Comparable<? super K>,V> implements NodeAccessor<BPlusTreeNode<Id,K,V>,Id> {

		@Override
		public Id getRootId() throws BPlusTreeConteneException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BPlusTreeNode<Id, K, V> getContent(Id id) throws BPlusTreeConteneException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Id createNewId() throws BPlusTreeConteneException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BPlusTreeNode<Id, K, V> createContent(Id id) throws BPlusTreeConteneException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void storeContent(Id id, BPlusTreeNode<Id, K, V> content) throws BPlusTreeConteneException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public BPlusTreeNode<Id, K, V> removeContent(Id id) throws BPlusTreeConteneException {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
