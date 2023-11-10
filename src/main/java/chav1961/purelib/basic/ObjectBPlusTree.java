package chav1961.purelib.basic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import chav1961.purelib.basic.interfaces.LongBPlusTreeNode;
import chav1961.purelib.basic.interfaces.ObjectBPlusTreeNode;
import chav1961.purelib.enumerations.ContinueMode;

class ObjectBPlusTree <NodeId, K extends Comparable<? super K>, V> extends BPlusTree<K, V> {
	public interface ObjectNodeAccessor<Content,Id> {
		Id getRootId() throws BPlusTreeContentException;
		Content getContent(Id id) throws BPlusTreeContentException;
		Id createIntermediate() throws BPlusTreeContentException;
		Id createLeaf() throws BPlusTreeContentException;
		void storeContent(Id id, Content content) throws BPlusTreeContentException;
		Content removeContent(Id id) throws BPlusTreeContentException;
	}

	private final ObjectNodeAccessor<ObjectBPlusTreeNode<NodeId, K, V>, NodeId>	objectAccessor;
	private final V[]			emptyArray;

	public ObjectBPlusTree(final Class<NodeId> idClass, final Class<K> keyClass, final Class<V> valueClass, final ObjectNodeAccessor<ObjectBPlusTreeNode<NodeId,K,V>,NodeId> accessor) throws NullPointerException {
		super(idClass, keyClass, valueClass);
		if (accessor == null) {
			throw new NullPointerException("Accessor instance can't be null");
		}
		else {
			this.objectAccessor = accessor;
			this.emptyArray = (V[]) Array.newInstance(valueClass,0);
		}
	}

	@Override
	public V get(final K key) throws BPlusTreeContentException, NullPointerException {
		if (key == null) {
			throw new NullPointerException("Key to get can't be null");
		}
		else {
			ObjectBPlusTreeNode<NodeId, K, V>	node = objectAccessor.getContent(objectAccessor.getRootId());
			
			while (node != null && !node.isLeaf()) {
				node = objectAccessor.getContent(node.getIdForKey(key));
			}
			if (node != null && node.containsKey(key)) {
				return node.getValue(key);
			}
			else {
				return null;
			}
		}
	}

	@Override
	public V[] get(final K keyFrom, final K keyTo, final boolean nearestFrom, final boolean nearestTo) throws BPlusTreeContentException, NullPointerException {
		if (keyFrom == null) {
			throw new NullPointerException("Key to get from can't be null");
		}
		else if (keyTo == null) {
			throw new NullPointerException("Key to get to can't be null");
		}
		else {
			ObjectBPlusTreeNode<NodeId, K, V>	nodeFrom = objectAccessor.getContent(objectAccessor.getRootId()), nodeTo = nodeFrom, currentNode;
			
			while (nodeFrom != null && !nodeFrom.isLeaf()) {
				nodeFrom = objectAccessor.getContent(nodeFrom.getIdForKeyGE(keyFrom));
			}
			while (nodeTo != null && !nodeTo.isLeaf()) {
				nodeTo = objectAccessor.getContent(nodeTo.getIdForKeyLE(keyTo));
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
				currentNode = objectAccessor.getContent(currentNode.getNextSiblingId()); 
			}
			
			final V[]	result = (V[]) Array.newInstance(getValueClass(), keys.size());
			int			index = 0;
			
			currentNode = nodeFrom;
			for (K item : keys) {
				if (!currentNode.containsKey(item)) {
					currentNode = objectAccessor.getContent(currentNode.getNextSiblingId()); 
				}
				Array.set(result,index++,currentNode.getValue(item));
			}
			keys.clear();
			return result;
		}
	}
	
	@Override
	public void insert(final K key, final V value) throws BPlusTreeContentException, NullPointerException {
		if (key == null) {
			throw new NullPointerException("Key to insert can't be null");
		}
		else if (value == null) {
			throw new NullPointerException("Value to insert can't be null");
		}
		else {
			ObjectBPlusTreeNode<NodeId, K, V>	node = objectAccessor.getContent(objectAccessor.getRootId());
			
loop:		for (;;) {
				while (node != null && !node.isLeaf()) {
					node = objectAccessor.getContent(node.getIdForKeyGE(key));
				}
				if (node != null) {
					if (!node.canInsert()) {
						final NodeId 	leftId = objectAccessor.createLeaf(), rightId = objectAccessor.createLeaf();
						
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
		
	@Override
	public void update(K key, V value) throws BPlusTreeContentException, NullPointerException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public V delete(final K key) throws BPlusTreeContentException, NullPointerException {
		if (key == null) {
			throw new NullPointerException("Key to delete can't be null");
		}
		else {
			ObjectBPlusTreeNode<NodeId, K, V>	node = objectAccessor.getContent(objectAccessor.getRootId());
			
			while (node != null && !node.isLeaf()) {
				node = objectAccessor.getContent(node.getIdForKey(key));
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

	public boolean walk(final WalkerCallback<K,V> callback) throws BPlusTreeContentException, NullPointerException {
		return false;
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
			ObjectBPlusTreeNode<NodeId, K, V>	nodeFrom = objectAccessor.getContent(objectAccessor.getRootId()), nodeTo = nodeFrom, currentNode;
			
			while (nodeFrom != null && !nodeFrom.isLeaf()) {
				nodeFrom = objectAccessor.getContent(nodeFrom.getIdForKeyGE(keyFrom));
			}
			while (nodeTo != null && !nodeTo.isLeaf()) {
				nodeTo = objectAccessor.getContent(nodeTo.getIdForKeyLE(keyTo));
			}
			
			final BPlusTreeContentException[]	error = new BPlusTreeContentException[1]; 
			final boolean[]						stop = new boolean[] {false};
			
			currentNode = nodeFrom;
			while (!stop[0] && currentNode.getFirstLeafKey().compareTo(keyTo) >= 0) {
				final ObjectBPlusTreeNode<NodeId, K, V>	toCall = currentNode; 
				
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
				currentNode = objectAccessor.getContent(currentNode.getNextSiblingId()); 
			}
			if (error[0] != null) {
				throw error[0];
			}
			else {
				return stop[0];
			}
		}
	}

	public static <NodeId, K extends Comparable<? super K>,V> ObjectBPlusTreeNode<Long,K,V> buildInMemoryBPlusTree(final Class<NodeId> nodeClass, final Class<K> keyClass, final Class<V> valueClass) throws NullPointerException, BPlusTreeContentException {
		return (ObjectBPlusTreeNode<Long, K, V>) new ObjectBPlusTree(Long.class, keyClass,valueClass, new InMemoryNodeAccessor<>(nodeClass,keyClass,valueClass)); 
	}
	
	public static <NodeId, K extends Comparable<? super K>,V> InMemoryNodeAccessor<NodeId,K,V> buildInMemoryNodeAccessor(final Class<NodeId> nodeClass, final Class<K> keyClass, final Class<V> valueClass) throws BPlusTreeContentException {
		return new InMemoryNodeAccessor<>(nodeClass,keyClass,valueClass);
	}
	
	
	private static class InMemoryNodeAccessor<NodeId, K extends Comparable<? super K>,V> implements ObjectNodeAccessor<ObjectBPlusTreeNode<NodeId,K,V>, NodeId> {
		private static final int							NODE_CAPACITY = 256;
		
		private final Map<NodeId,ObjectBPlusTreeNode<NodeId,K,V>>	content = new HashMap<>(); 
		private final Class<NodeId>							nodeClass; 
		private final Class<K>								keyClass; 
		private final Class<V>								valueClass;
		private final AtomicLong							uniqueId = new AtomicLong(0);
		private final NodeId								rootId;
		
		InMemoryNodeAccessor(final Class<NodeId> nodeClass, final Class<K> keyClass, final Class<V> valueClass) throws BPlusTreeContentException {
			this.nodeClass = nodeClass; 
			this.keyClass = keyClass; 
			this.valueClass = valueClass;
			this.rootId = createLeaf();
		}

		@Override
		public NodeId getRootId() throws BPlusTreeContentException {
			return rootId;
		}

		@Override
		public ObjectBPlusTreeNode<NodeId, K, V> getContent(final NodeId id) throws BPlusTreeContentException {
			return content.get(id); 
		}

		@Override
		public NodeId createIntermediate() throws BPlusTreeContentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public NodeId createLeaf() throws BPlusTreeContentException {
			final NodeId	newId = (NodeId)Long.valueOf(uniqueId.getAndIncrement());
			
			storeContent(newId, new InMemoryLeafNode<>(nodeClass, keyClass, valueClass));
			return (NodeId)newId;
		}

		@Override
		public void storeContent(final NodeId id, final ObjectBPlusTreeNode<NodeId, K, V> content) throws BPlusTreeContentException {
			if (id == null) {
				throw new NullPointerException("Node id can't be null");
			}
			else if (content == null) {
				throw new IllegalArgumentException("Content to store can't be null");
			}
			else {
				this.content.put(id, content);
			}
		}

		@Override
		public ObjectBPlusTreeNode<NodeId, K, V> removeContent(final NodeId id) throws BPlusTreeContentException {
			if (id == null) {
				throw new NullPointerException("Node id can't be null");
			}
			else {
				return content.remove(id);
			}
		}
	}

	private static class InMemoryLeafNode<NodeId, K extends Comparable<? super K>,V> implements ObjectBPlusTreeNode<NodeId,K,V> {
		private final Class<K>		keyClass; 
		private final Class<V>		valueClass;
		private final Class<NodeId>	nodeId;
		
		InMemoryLeafNode(final Class<NodeId> nodeId, final Class<K> keyClass, final Class<V> valueClass) {
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
		public NodeId getIdForKey(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public NodeId getIdForKeyGE(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public NodeId getIdForKeyLE(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public NodeId getNextSiblingId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public NodeId getPrevSiblingId() {
			// TODO Auto-generated method stub
			return null;
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
		public void split(final NodeId left, final NodeId right) {
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
		public long getCurrentId() {
			// TODO Auto-generated method stub
			return 0;
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
		public LongBPlusTreeNode<K,V>[] split(final long left, final long right) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
