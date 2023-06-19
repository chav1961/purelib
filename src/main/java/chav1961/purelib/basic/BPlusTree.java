package chav1961.purelib.basic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.BPlusTreeNode;
import chav1961.purelib.enumerations.ContinueMode;

class BPlusTree <NodeId,K extends Comparable<? super K>,V> {
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
		Id createIntermediate() throws BPlusTreeConteneException;
		Id createLeaf() throws BPlusTreeConteneException;
		void storeContent(Id id, Content content) throws BPlusTreeConteneException;
		Content removeContent(Id id) throws BPlusTreeConteneException;
	}

	@FunctionalInterface
	public interface WalkerCallback<K,V> {
		ContinueMode process(K key, V value) throws BPlusTreeConteneException;
	}
	
	private final NodeAccessor<BPlusTreeNode<NodeId, K, V>,NodeId>	accessor;
	private final Class<NodeId>	idClass;
	private final Class<K>		keyClass;
	private final Class<V>		valueClass;
	private final V[]			emptyArray;

	public BPlusTree(final Class<NodeId> idClass, final Class<K> keyClass, final Class<V> valueClass, final NodeAccessor<BPlusTreeNode<NodeId,K,V>,NodeId> accessor) throws NullPointerException {
		if (idClass == null) {
			throw new NullPointerException("Id class can't be null");
		}
		else if (keyClass == null) {
			throw new NullPointerException("Key class can't be null");
		}
		else if (valueClass == null) {
			throw new NullPointerException("Value class can't be null");
		}
		else if (accessor == null) {
			throw new NullPointerException("Accessor instance can't be null");
		}
		else {
			this.idClass = idClass;
			this.keyClass = keyClass;
			this.valueClass = valueClass;
			this.accessor = accessor;
			this.emptyArray = (V[]) Array.newInstance(valueClass,0);
		}
	}

	public V get(final K key) throws BPlusTreeConteneException, NullPointerException {
		if (key == null) {
			throw new NullPointerException("Key to get can't be null");
		}
		else {
			BPlusTreeNode<NodeId, K, V>	node = accessor.getContent(accessor.getRootId());
			
			while (node != null && !node.isLeaf()) {
				node = accessor.getContent(node.getIdForKey(key));
			}
			if (node != null && node.containsKey(key)) {
				return node.getValue(key);
			}
			else {
				return null;
			}
		}
	}

	public V[] get(final K keyFrom, final K keyTo, final boolean nearestFrom, final boolean nearestTo) throws BPlusTreeConteneException, NullPointerException {
		if (keyFrom == null) {
			throw new NullPointerException("Key to get from can't be null");
		}
		else if (keyTo == null) {
			throw new NullPointerException("Key to get to can't be null");
		}
		else {
			BPlusTreeNode<NodeId, K, V>	nodeFrom = accessor.getContent(accessor.getRootId()), nodeTo = nodeFrom, currentNode;
			
			while (nodeFrom != null && !nodeFrom.isLeaf()) {
				nodeFrom = accessor.getContent(nodeFrom.getIdForKeyGE(keyFrom));
			}
			while (nodeTo != null && !nodeTo.isLeaf()) {
				nodeTo = accessor.getContent(nodeTo.getIdForKeyLE(keyTo));
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
				currentNode = accessor.getContent(currentNode.getNextSiblingId()); 
			}
			
			final V[]	result = (V[]) Array.newInstance(valueClass,keys.size());
			int			index = 0;
			
			currentNode = nodeFrom;
			for (K item : keys) {
				if (!currentNode.containsKey(item)) {
					currentNode = accessor.getContent(currentNode.getNextSiblingId()); 
				}
				Array.set(result,index++,currentNode.getValue(item));
			}
			keys.clear();
			return result;
		}
	}
	
	public void insert(final K key, final V value) throws BPlusTreeConteneException, NullPointerException {
		if (key == null) {
			throw new NullPointerException("Key to insert can't be null");
		}
		else if (value == null) {
			throw new NullPointerException("Value to insert can't be null");
		}
		else {
			BPlusTreeNode<NodeId, K, V>	node = accessor.getContent(accessor.getRootId());
			
loop:		for (;;) {
				while (node != null && !node.isLeaf()) {
					node = accessor.getContent(node.getIdForKeyGE(key));
				}
				if (node != null) {
					if (!node.canInsert()) {
						final NodeId 	leftId = accessor.createLeaf(), rightId = accessor.createLeaf();
						
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
		
	public V delete(final K key) throws BPlusTreeConteneException, NullPointerException {
		if (key == null) {
			throw new NullPointerException("Key to delete can't be null");
		}
		else {
			BPlusTreeNode<NodeId, K, V>	node = accessor.getContent(accessor.getRootId());
			
			while (node != null && !node.isLeaf()) {
				node = accessor.getContent(node.getIdForKey(key));
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

	public boolean walk(final K keyFrom, final K keyTo, final boolean nearestFrom, final boolean nearestTo, final WalkerCallback<K,V> callback) throws BPlusTreeConteneException, NullPointerException {
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
			BPlusTreeNode<NodeId, K, V>	nodeFrom = accessor.getContent(accessor.getRootId()), nodeTo = nodeFrom, currentNode;
			
			while (nodeFrom != null && !nodeFrom.isLeaf()) {
				nodeFrom = accessor.getContent(nodeFrom.getIdForKeyGE(keyFrom));
			}
			while (nodeTo != null && !nodeTo.isLeaf()) {
				nodeTo = accessor.getContent(nodeTo.getIdForKeyLE(keyTo));
			}
			
			final BPlusTreeConteneException[]	error = new BPlusTreeConteneException[1]; 
			final boolean[]						stop = new boolean[] {false};
			
			currentNode = nodeFrom;
			while (!stop[0] && currentNode.getFirstLeafKey().compareTo(keyTo) >= 0) {
				final BPlusTreeNode<NodeId, K, V>	toCall = currentNode; 
				
				if (currentNode.getLastLeafKey().compareTo(keyTo) <= 0) {
					currentNode.getKeysGE(keyFrom,(key)->{
						if (!stop[0]) {
							try{if (callback.process(key,toCall.getValue(key)) != ContinueMode.CONTINUE) {
									stop[0] = true;
								}
							} catch (BPlusTreeConteneException e) {
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
							} catch (BPlusTreeConteneException e) {
								error[0] = e;
								stop[0] = true;
							}
						}
					});
				}
				currentNode = accessor.getContent(currentNode.getNextSiblingId()); 
			}
			if (error[0] != null) {
				throw error[0];
			}
			else {
				return stop[0];
			}
		}
	}

	public static <K extends Comparable<? super K>,V> BPlusTreeNode<Long,K,V> buildInMemoryBPlusTree(final Class<K> keyClass, final Class<V> valueClass) throws NullPointerException, BPlusTreeConteneException {
		return (BPlusTreeNode<Long, K, V>) new BPlusTree(Long.class,keyClass,valueClass, new InMemoryNodeAccessor<>(keyClass,valueClass)); 
	}
	
	private static class InMemoryNodeAccessor<K extends Comparable<? super K>,V> implements NodeAccessor<BPlusTreeNode<Long,K,V>,Long> {
		private static final int							NODE_CAPACITY = 256;
		
		private final LongIdMap<BPlusTreeNode<Long,K,V>>	content = new LongIdMap(BPlusTreeNode.class); 
		private final Class<K>								keyClass; 
		private final Class<V>								valueClass;
		private final AtomicLong							uniqueId = new AtomicLong(0);
		private final Long									rootId;
		
		InMemoryNodeAccessor(final Class<K> keyClass, final Class<V> valueClass) throws BPlusTreeConteneException {
			this.keyClass = keyClass; 
			this.valueClass = valueClass;
			this.rootId = createLeaf();
		}

		@Override
		public Long getRootId() throws BPlusTreeConteneException {
			return rootId;
		}

		@Override
		public BPlusTreeNode<Long, K, V> getContent(final Long id) throws BPlusTreeConteneException {
			return content.get(id); 
		}

		@Override
		public Long createIntermediate() throws BPlusTreeConteneException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long createLeaf() throws BPlusTreeConteneException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void storeContent(final Long id, final BPlusTreeNode<Long, K, V> content) throws BPlusTreeConteneException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public BPlusTreeNode<Long, K, V> removeContent(final Long id) throws BPlusTreeConteneException {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private static class InMemoryLeafNode<K extends Comparable<? super K>,V> implements BPlusTreeNode<Long,K,V> {
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
		public Long getIdForKey(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getIdForKeyGE(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getIdForKeyLE(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getNextSiblingId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getPrevSiblingId() {
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
		public void split(final Long left, final Long right) {
			// TODO Auto-generated method stub
			
		}
		
	}

	private static class InMemoryIntermediateNode<K extends Comparable<? super K>,V> implements BPlusTreeNode<Long,K,V> {
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
		public Long getIdForKey(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getIdForKeyGE(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getIdForKeyLE(final K key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getNextSiblingId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getPrevSiblingId() {
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
		public void split(final Long left, final Long right) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
