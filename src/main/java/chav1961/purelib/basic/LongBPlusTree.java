package chav1961.purelib.basic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LongBPlusTreeNode;
import chav1961.purelib.basic.interfaces.ObjectBPlusTreeNode;
import chav1961.purelib.enumerations.ContinueMode;

class LongBPlusTree <K extends Comparable<? super K>, V> extends BPlusTree<K, V> {
	public interface LongNodeAccessor<Content> {
		long getRootId() throws BPlusTreeContentException;
		Content getContent(long id) throws BPlusTreeContentException;
		long createIntermediate() throws BPlusTreeContentException;
		long createLeaf() throws BPlusTreeContentException;
		void storeContent(long id, Content content) throws BPlusTreeContentException;
		Content removeContent(long id) throws BPlusTreeContentException;
	}
	
	private final LongNodeAccessor<LongBPlusTreeNode<K, V>>	longAccessor;
	private final V[]			emptyArray;

	public LongBPlusTree(final Class<K> keyClass, final Class<V> valueClass, final LongNodeAccessor<LongBPlusTreeNode<K,V>> accessor) throws NullPointerException {
		super(long.class, keyClass, valueClass);
		if (accessor == null) {
			throw new NullPointerException("Accessor instance can't be null");
		}
		else {
			this.longAccessor = null;
			this.emptyArray = (V[]) Array.newInstance(valueClass,0);
		}
	}
	
	public V get(final K key) throws BPlusTreeContentException, NullPointerException {
		if (key == null) {
			throw new NullPointerException("Key to get can't be null");
		}
		else {
			LongBPlusTreeNode<K, V>	node = longAccessor.getContent(longAccessor.getRootId());
			
			while (node != null && !node.isLeaf()) {
				node = longAccessor.getContent(node.getIdForKey(key));
			}
			if (node != null && node.containsKey(key)) {
				return node.getValue(key);
			}
			else {
				return null;
			}
		}
	}

	public V[] get(final K keyFrom, final K keyTo, final boolean nearestFrom, final boolean nearestTo) throws BPlusTreeContentException, NullPointerException {
		if (keyFrom == null) {
			throw new NullPointerException("Key to get from can't be null");
		}
		else if (keyTo == null) {
			throw new NullPointerException("Key to get to can't be null");
		}
		else {
			LongBPlusTreeNode<K, V>	nodeFrom = longAccessor.getContent(longAccessor.getRootId()), nodeTo = nodeFrom, currentNode;
			
			while (nodeFrom != null && !nodeFrom.isLeaf()) {
				nodeFrom = longAccessor.getContent(nodeFrom.getIdForKeyGE(keyFrom));
			}
			while (nodeTo != null && !nodeTo.isLeaf()) {
				nodeTo = longAccessor.getContent(nodeTo.getIdForKeyLE(keyTo));
			}
			final List<K>	keys = new ArrayList<>();
			
			currentNode = nodeFrom;
			while (currentNode.getFirstLeafKey().compareTo(keyTo) >= 0) {
				if (currentNode.getLastLeafKey().compareTo(keyTo) <= 0) {
					currentNode.getKeysGE(keyFrom,(key)->{keys.add(key);});
				}
				else {
					currentNode.getKeysGE(keyFrom,(key)->{if (key.compareTo(keyTo) <= 0) keys.add(key);});
				}
				currentNode = longAccessor.getContent(currentNode.getNextSiblingId()); 
			}
			
			final V[]	result = (V[]) Array.newInstance(getValueClass(),keys.size());
			int			index = 0;
			
			currentNode = nodeFrom;
			for (K item : keys) {
				if (!currentNode.containsKey(item)) {
					currentNode = longAccessor.getContent(currentNode.getNextSiblingId()); 
				}
				Array.set(result,index++,currentNode.getValue(item));
			}
			keys.clear();
			return result;
		}
	}
	
	public void insert(final K key, final V value) throws BPlusTreeContentException, NullPointerException {
		if (key == null) {
			throw new NullPointerException("Key to insert can't be null");
		}
		else if (value == null) {
			throw new NullPointerException("Value to insert can't be null");
		}
		else {
			LongBPlusTreeNode<K, V>	node = longAccessor.getContent(longAccessor.getRootId());
			
loop:		for (;;) {
				while (node != null && !node.isLeaf()) {
					node = longAccessor.getContent(node.getIdForKeyGE(key));
				}
				if (node != null) {
					if (!node.canInsert()) {
						final long	leftId = longAccessor.createLeaf(), rightId = longAccessor.createLeaf();
						
						node.split(leftId, rightId);
						continue loop;
					}
					else {
						node.insert(key, value);
					}
				}
			}
		}
	}
		
	public V delete(final K key) throws BPlusTreeContentException, NullPointerException {
		if (key == null) {
			throw new NullPointerException("Key to delete can't be null");
		}
		else {
			LongBPlusTreeNode<K, V>	node = longAccessor.getContent(longAccessor.getRootId());
			
			while (node != null && !node.isLeaf()) {
				node = longAccessor.getContent(node.getIdForKey(key));
			}
			if (node != null) {
				final V	result = node.delete(key);
				
				if (node.canCompact()) {
					node.join();
				}
				return result;
			}
			else {
				return null;
			}
		}
		
	}

	public boolean walk(final K keyFrom, final K keyTo, final boolean nearestFrom, final boolean nearestTo, final WalkerCallback<K,V> callback) throws BPlusTreeContentException, NullPointerException {
		if (keyFrom == null) {
			throw new NullPointerException("Key to get from can't be null");
		}
		else if (keyTo == null) {
			throw new NullPointerException("Key to get to can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Wakler callback can't be null");
		}
		else {
			LongBPlusTreeNode<K, V>	nodeFrom = longAccessor.getContent(longAccessor.getRootId()), nodeTo = nodeFrom, currentNode;
			
			while (nodeFrom != null && !nodeFrom.isLeaf()) {
				nodeFrom = longAccessor.getContent(nodeFrom.getIdForKeyGE(keyFrom));
			}
			while (nodeTo != null && !nodeTo.isLeaf()) {
				nodeTo = longAccessor.getContent(nodeTo.getIdForKeyLE(keyTo));
			}
			
			final BPlusTreeContentException[]	error = new BPlusTreeContentException[1]; 
			final boolean[]						stop = new boolean[] {false};
			
			currentNode = nodeFrom;
			while (!stop[0] && currentNode.getFirstLeafKey().compareTo(keyTo) >= 0) {
				final LongBPlusTreeNode<K, V>	toCall = currentNode; 
				
				if (currentNode.getLastLeafKey().compareTo(keyTo) <= 0) {
					currentNode.getKeysGE(keyFrom,(key)->{
						if (!stop[0]) {
							try{if (callback.process(key,toCall.getValue(key)) != ContinueMode.CONTINUE) {
									stop[0] = true;
								}
							} catch (BPlusTreeContentException e) {
								error[0] = e;
								stop[0] = true;
							}
						}
					});
				}
				else {
					currentNode.getKeysGE(keyFrom,(key)->{
						if (!stop[0] && key.compareTo(keyTo) <= 0) {
							try{if (callback.process(key,toCall.getValue(key)) != ContinueMode.CONTINUE) {
									stop[0] = true;
								}
							} catch (BPlusTreeContentException e) {
								error[0] = e;
								stop[0] = true;
							}
						}
					});
				}
				currentNode = longAccessor.getContent(currentNode.getNextSiblingId()); 
			}
			if (error[0] != null) {
				throw error[0];
			}
			else {
				return stop[0];
			}
		}
	}

	public static <K extends Comparable<? super K>,V> ObjectBPlusTreeNode<Long,K,V> buildInMemoryBPlusTree(final Class<K> keyClass, final Class<V> valueClass) throws NullPointerException, BPlusTreeContentException {
		return (ObjectBPlusTreeNode<Long, K, V>) new LongBPlusTree(keyClass, valueClass, new InMemoryNodeAccessor<>(keyClass,valueClass)); 
	}
	
	public static <K extends Comparable<? super K>,V> InMemoryNodeAccessor<K,V> buildInMemoryNodeAccessor(final Class<K> keyClass, final Class<V> valueClass) throws BPlusTreeContentException {
		return new InMemoryNodeAccessor<>(keyClass,valueClass);
	}
	
	
	private static class InMemoryNodeAccessor<K extends Comparable<? super K>,V> implements LongNodeAccessor<LongBPlusTreeNode<K,V>> {
		private static final int							NODE_CAPACITY = 256;
		
		private final LongIdMap<LongBPlusTreeNode<K,V>>		content = new LongIdMap(LongBPlusTreeNode.class); 
		private final Class<K>								keyClass; 
		private final Class<V>								valueClass;
		private final AtomicLong							uniqueId = new AtomicLong(0);
		private final Long									rootId;
		
		InMemoryNodeAccessor(final Class<K> keyClass, final Class<V> valueClass) throws BPlusTreeContentException {
			this.keyClass = keyClass; 
			this.valueClass = valueClass;
			this.rootId = createLeaf();
		}

		@Override
		public long getRootId() throws BPlusTreeContentException {
			return rootId;
		}

		@Override
		public LongBPlusTreeNode<K, V> getContent(final long id) throws BPlusTreeContentException {
			return content.get(id); 
		}

		@Override
		public long createIntermediate() throws BPlusTreeContentException {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public long createLeaf() throws BPlusTreeContentException {
			final long	newId = uniqueId.getAndIncrement();
			
			storeContent(newId, new InMemoryLeafNode<>(rootId, keyClass, valueClass));
			return newId;
		}

		@Override
		public void storeContent(final long id, final LongBPlusTreeNode<K, V> content) throws BPlusTreeContentException {
			if (id < 0) {
				throw new IllegalArgumentException("Node id ["+id+"] must be greater than or equals 0");
			}
			else if (content == null) {
				throw new IllegalArgumentException("Content to store can't be null");
			}
			else {
				this.content.put(id, content);
			}
		}

		@Override
		public LongBPlusTreeNode<K, V> removeContent(final long id) throws BPlusTreeContentException {
			if (id < 0 || id >= content.maxValue()) {
				throw new IllegalArgumentException("Node id ["+id+"] out of range 0.."+(content.maxValue()-1));
			}
			else {
				return content.remove(id);
			}
		}
	}

	private static class InMemoryLeafNode<K extends Comparable<? super K>,V> implements LongBPlusTreeNode<K,V> {
		private final Class<K>								keyClass; 
		private final Class<V>								valueClass;
		private final Long									nodeId;
		
		InMemoryLeafNode(final Long nodeId, final Class<K> keyClass, final Class<V> valueClass) {
			this.nodeId = nodeId;
			this.keyClass = keyClass;
			this.valueClass = valueClass;
		}

		@Override
		public boolean isLeaf() {
			return true;
		}

		@Override
		public boolean containsKey(final K key) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsKeyGE(final K key) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsKeyLE(final K key) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int getKeysGE(final K key, final Consumer<K> accept) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getKeysLE(final K key, Consumer<K> accept) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public K getFirstLeafKey() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public K getLastLeafKey() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public V getValue(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getIdForKey(final K key) {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public long getIdForKeyGE(final K key) {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public long getIdForKeyLE(final K key) {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public long getNextSiblingId() {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public long getPrevSiblingId() {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public V delete(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void insert(final K key, final V value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean canInsert() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean canCompact() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void join() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void split(final long left, final long right) {
			// TODO Auto-generated method stub
			
		}
		
	}

	private static class InMemoryIntermediateNode<K extends Comparable<? super K>,V> implements LongBPlusTreeNode<K,V> {
		private final Class<K>	keyClass; 
		private final Class<V>	valueClass; 
		private final K[]		keys;
		private final V[]		values;
		int						filled = 0;
		
		InMemoryIntermediateNode(final Class<K> keyClass, final Class<V> valueClass) {
			this.keyClass = keyClass;
			this.valueClass = valueClass;
			this.keys = (K[]) Array.newInstance(keyClass, InMemoryNodeAccessor.NODE_CAPACITY);
			this.values = (V[]) Array.newInstance(valueClass, InMemoryNodeAccessor.NODE_CAPACITY);
		}

		@Override
		public boolean isLeaf() {
			return true;
		}

		@Override
		public boolean containsKey(final K key) {
			return Arrays.binarySearch(keys,0,filled,key) >= 0;
		}

		@Override
		public boolean containsKeyGE(final K key) {
			final int	location = Arrays.binarySearch(keys,0,filled,key);

			if (location >= 0) {
				return true;
			}
			else {
				return 1 - location < filled;
			}
		}

		@Override
		public boolean containsKeyLE(final K key) {
			final int	location = Arrays.binarySearch(keys,0,filled,key);

			if (location >= 0) {
				return true;
			}
			else {
				return 1 - location > 0;
			}
		}

		@Override
		public int getKeysGE(final K key, final Consumer<K> accept) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getKeysLE(final K key, Consumer<K> accept) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public K getFirstLeafKey() {
			if (filled > 0) {
				return keys[0];
			}
			else {
				return null;
			}
		}

		@Override
		public K getLastLeafKey() {
			if (filled > 0) {
				return keys[filled-1];
			}
			else {
				return null;
			}
		}

		@Override
		public V getValue(final K key) {
			final int	location = Arrays.binarySearch(keys,0,filled,key);

			if (location >= 0 && location < filled) {
				return values[location];
			}
			else {
				return null;
			}
		}

		@Override
		public long getIdForKey(final K key) {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public long getIdForKeyGE(final K key) {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public long getIdForKeyLE(final K key) {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public long getNextSiblingId() {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public long getPrevSiblingId() {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public V delete(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void insert(final K key, final V value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean canInsert() {
			return filled < keys.length / 2;
		}

		@Override
		public boolean canCompact() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void join() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void split(final long left, final long right) {
			// TODO Auto-generated method stub
			
		}
		
	}
}