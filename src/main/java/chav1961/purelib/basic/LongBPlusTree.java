package chav1961.purelib.basic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

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

	public LongBPlusTree(final Class<K> keyClass, final Class<V> valueClass, final LongNodeAccessor<LongBPlusTreeNode<K,V>> accessor) throws NullPointerException {
		super(long.class, keyClass, valueClass);
		if (accessor == null) {
			throw new NullPointerException("Accessor instance can't be null");
		}
		else {
			this.longAccessor = accessor;
		}
	}
	
	@Override
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

	@Override
	public V[] get(final K keyFrom, final K keyTo, final boolean nearestFrom, final boolean nearestTo) throws BPlusTreeContentException, NullPointerException {
		if (keyFrom == null) {
			throw new NullPointerException("Key to get from can't be null");
		}
		else if (keyTo == null) {
			throw new NullPointerException("Key to get to can't be null");
		}
		else {
			final List<V>	result = new ArrayList<>();
			
			walk(longAccessor, longAccessor.getRootId(), keyFrom, keyTo, nearestFrom, nearestTo, (k,v)->{result.add(v); return ContinueMode.CONTINUE;});
			return result.toArray((V[])Array.newInstance(getValueClass(), result.size()));
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
			LongBPlusTreeNode<K, V>	node = longAccessor.getContent(longAccessor.getRootId());

			if (node != null) {
				node.insert(key, value);
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
			LongBPlusTreeNode<K, V>	node = longAccessor.getContent(longAccessor.getRootId());
			
			if (node != null) {
				return node.delete(key);
			}
			else {
				return null;
			}
		}
		
	}

	@Override
	public boolean walk(final WalkerCallback<K,V> callback) throws BPlusTreeContentException, NullPointerException {
		if (callback == null) {
			throw new NullPointerException("Wakler callback can't be null");
		}
		else {
			return walk(longAccessor, longAccessor.getRootId(), callback);
		}
	}

	@Override
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
			return walk(longAccessor, longAccessor.getRootId(), keyFrom, keyTo, nearestFrom, nearestTo, callback);
		}
	}
	
	public static <K extends Comparable<? super K>,V> LongBPlusTree<K,V> buildInMemoryBPlusTree(final Class<K> keyClass, final Class<V> valueClass) throws NullPointerException, BPlusTreeContentException {
		return (LongBPlusTree<K, V>) new LongBPlusTree(keyClass, valueClass, new InMemoryNodeAccessor<>(keyClass,valueClass)); 
	}
	
	public static <K extends Comparable<? super K>,V> InMemoryNodeAccessor<K,V> buildInMemoryNodeAccessor(final Class<K> keyClass, final Class<V> valueClass) throws BPlusTreeContentException {
		return new InMemoryNodeAccessor<>(keyClass,valueClass);
	}

	private boolean walk(final LongNodeAccessor<LongBPlusTreeNode<K, V>> longAccessor, final long currentId, final WalkerCallback<K, V> callback) throws BPlusTreeContentException {
		LongBPlusTreeNode<K, V>	node = longAccessor.getContent(currentId);
		
		if (node.isLeaf()) {
			final K	firstKey = node.getFirstLeafKey();
			
			if (firstKey != null) {
				node.getKeysGE(firstKey, (k)->{
					try {
						callback.process(k, node.getValue(k));
					} catch (BPlusTreeContentException e) {
					}
				});
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return walk(longAccessor, node.getPrevSiblingId(), callback) && walk(longAccessor, node.getNextSiblingId(), callback);
		}
	}

	private boolean walk(final LongNodeAccessor<LongBPlusTreeNode<K, V>> longAccessor, final long currentId, final K fromKey, final K toKey, final boolean nearestFrom, final boolean nearestTo, final WalkerCallback<K, V> callback) throws BPlusTreeContentException {
		LongBPlusTreeNode<K, V>	node = longAccessor.getContent(currentId);
		
		if (node.isLeaf()) {
			final K	firstKey = node.getFirstLeafKey();
			final K	lastKey = node.getLastLeafKey();
			
			if (firstKey.compareTo(toKey) <= (nearestTo ? -1 : 0) && lastKey.compareTo(fromKey) >= (nearestFrom ? 1 : 0)) {
				node.getKeysGE(fromKey, (k)->{
					if (k.compareTo(fromKey) >= (nearestFrom ? 1 : 0)) {
						try {
							callback.process(k, node.getValue(k));
						} catch (BPlusTreeContentException e) {
						}
					}
				});
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return walk(longAccessor, node.getPrevSiblingId(), callback) && walk(longAccessor, node.getNextSiblingId(), callback);
		}
	}
	
	private static class InMemoryNodeAccessor<K extends Comparable<? super K>,V> implements LongNodeAccessor<LongBPlusTreeNode<K,V>> {
		static final int									NODE_CAPACITY = 64 * 4;
		static final int									NODE_HIGHWATER = 64 * 3;
		static final int									NODE_LOWATER = 64 * 1;
		
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
			final long	newId = uniqueId.getAndIncrement();
			
			storeContent(newId, new InMemoryLeafNode<>(newId, keyClass, valueClass));
			return newId;
		}

		@Override
		public long createLeaf() throws BPlusTreeContentException {
			final long	newId = uniqueId.getAndIncrement();
			
			storeContent(newId, new InMemoryLeafNode<>(newId, keyClass, valueClass));
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
		private final Class<K>						keyClass; 
		private final Class<V>						valueClass;
		private final Long							nodeId;
		private final TreeMap<K,KeyValuePair<K,V>>	list = new TreeMap();
		
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
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else {
				return list.containsKey(key);
			}
		}

		@Override
		public boolean containsKeyGE(final K key) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else if (list.containsKey(key)) {
				return true;
			}
			else {
				return list.ceilingKey(key) != null; 
			}
		}

		@Override
		public boolean containsKeyLE(final K key) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else if (list.containsKey(key)) {
				return true;
			}
			else {
				return list.floorKey(key) != null; 
			}
		}

		@Override
		public int getKeysGE(final K key, final Consumer<K> accept) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else if (accept == null) {
				throw new NullPointerException("Accept callback can't be null");
			}
			else {
				K	currentKey = list.containsKey(key) ? key : list.higherKey(key);
				int	count = 0;
				
				while (currentKey != null) {
					accept.accept(currentKey);
					currentKey = list.higherKey(currentKey);
					count++;
				}
				return count;
			}
		}

		@Override
		public int getKeysLE(final K key, Consumer<K> accept) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else if (accept == null) {
				throw new NullPointerException("Accept callback can't be null");
			}
			else {
				K	currentKey = list.containsKey(key) ? key : list.lowerKey(key);
				int	count = 0;
				
				while (currentKey != null) {
					accept.accept(currentKey);
					currentKey = list.lowerKey(currentKey);
					count++;
				}
				return count;
			}
		}

		@Override
		public K getFirstLeafKey() {
			if (!list.isEmpty()) {
				return list.firstKey();
			}
			else {
				return null;
			}
		}

		@Override
		public K getLastLeafKey() {
			if (!list.isEmpty()) {
				return list.lastKey();
			}
			else {
				return null;
			}
		}

		@Override
		public V getValue(final K key) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else {
				return list.get(key).value;
			}
		}

		@Override
		public long getIdForKey(final K key) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else if (containsKey(key)) {
				return nodeId;
			}
			else {
				return -1;
			}
		}

		@Override
		public long getIdForKeyGE(final K key) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else if (containsKeyGE(key)) {
				return nodeId;
			}
			else {
				return -1;
			}
		}

		@Override
		public long getIdForKeyLE(final K key) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else if (containsKeyLE(key)) {
				return nodeId;
			}
			else {
				return -1;
			}
		}

		@Override
		public long getCurrentId() {
			return nodeId;
		}
		
		@Override
		public long getNextSiblingId() {
			return -1;
		}

		@Override
		public long getPrevSiblingId() {
			return -1;
		}

		@Override
		public V delete(final K key) {
			if (key == null) {
				throw new NullPointerException("Key to insert can't be null");
			}
			else {
				return list.remove(key).value;
			}
		}

		@Override
		public void insert(final K key, final V value) {
			if (key == null) {
				throw new NullPointerException("Key to insert can't be null");
			}
			else if (value == null) {
				throw new NullPointerException("Value to insert can't be null");
			}
			else {
				list.put(key, new KeyValuePair<>(key, value));
			}
		}

		@Override
		public boolean canInsert() {
			return list.size() <= InMemoryNodeAccessor.NODE_HIGHWATER;
		}

		@Override
		public boolean canCompact() {
			return false;
		}

		@Override
		public void join() {
		}

		@Override
		public LongBPlusTreeNode<K,V>[] split(final long left, final long right) {
			if (left < 0 || right < 0 || left == right) {
				throw new IllegalArgumentException("Left ["+left+"] and right ["+right+"] identifiers can't be negative and must differ each other");
			}
			else {
				final InMemoryLeafNode<K,V>	leftNode = new InMemoryLeafNode<>(left, keyClass, valueClass);
				final InMemoryLeafNode<K,V>	rightNode = new InMemoryLeafNode<>(right, keyClass, valueClass);
				K	currentKey = list.firstKey();
				
				for(int index = 0, maxIndex = list.size(), middle = list.size() / 2; index < maxIndex; index++) {
					if (index < middle) {
						leftNode.insert(currentKey, list.get(currentKey).value);
					}
					else {
						rightNode.insert(currentKey, list.get(currentKey).value);
					}
					currentKey = list.ceilingKey(currentKey);
				}
				return new LongBPlusTreeNode[] {leftNode, rightNode};
			}
		}
	}

	private static class InMemoryIntermediateNode<K extends Comparable<? super K>,V> implements LongBPlusTreeNode<K,V> {
		private final Class<K>	keyClass; 
		private final Class<V>	valueClass; 
		private final TreeMap<K,KeyValuePair<K,LongBPlusTreeNode<K,V>>>	list = new TreeMap();
		private final long		nodeId;
		private long			prevNodeId = -1, nextNodeId = -1;
		
		InMemoryIntermediateNode(final long nodeId, final Class<K> keyClass, final Class<V> valueClass) {
			this.keyClass = keyClass;
			this.valueClass = valueClass;
			this.nodeId = nodeId;
		}

		@Override
		public boolean isLeaf() {
			return false;
		}

		@Override
		public boolean containsKey(final K key) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else if (list.containsKey(key)) {
				return list.get(key).value.containsKey(key);
			}
			else {
				final K	lowerKey = list.floorKey(key);
				
				if (lowerKey != null) {
					return containsKey(lowerKey);
				}
				else {
					return false;
				}
			}
		}

		@Override
		public boolean containsKeyGE(final K key) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else if (list.containsKey(key)) {
				return list.get(key).value.containsKeyGE(key);
			}
			else {
				K	lowerKey = list.floorKey(key);
				
				while (lowerKey != null) {
					if (list.get(lowerKey).value.containsKeyGE(key)) {
						return true;
					}
					else {
						lowerKey = list.ceilingKey(lowerKey);
					}
				}
				return false;
			}
		}

		@Override
		public boolean containsKeyLE(final K key) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else if (list.containsKey(key)) {
				return list.get(key).value.containsKeyLE(key);
			}
			else {
				K	upperKey = list.ceilingKey(key);
				
				while (upperKey != null) {
					if (list.get(upperKey).value.containsKeyLE(key)) {
						return true;
					}
					else {
						upperKey = list.floorKey(upperKey);
					}
				}
				return false;
			}
		}

		@Override
		public int getKeysGE(final K key, final Consumer<K> accept) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else {
				K	currentKey = list.containsKey(key) ? key : list.ceilingKey(key);
				int	count = 0;
				
				while (currentKey != null) {
					list.get(currentKey).value.getKeysGE(key, accept);
					currentKey = list.ceilingKey(currentKey);
					count++;
				}
				return count;
			}
		}

		@Override
		public int getKeysLE(final K key, Consumer<K> accept) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else {
				K	currentKey = list.containsKey(key) ? key : list.floorKey(key);
				int	count = 0;
				
				while (currentKey != null) {
					list.get(currentKey).value.getKeysLE(key, accept);
					currentKey = list.floorKey(currentKey);
					count++;
				}
				return count;
			}
		}

		@Override
		public K getFirstLeafKey() {
			return list.get(list.firstKey()).value.getFirstLeafKey();
		}

		@Override
		public K getLastLeafKey() {
			return list.get(list.lastKey()).value.getLastLeafKey();
		}

		@Override
		public V getValue(final K key) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else if (list.containsKey(key)) {
				return list.get(key).value.getValue(key);
			}
			else {
				K	prev = list.floorKey(key);
				
				if (prev != null) {
					return list.get(prev).value.getValue(key);
				}
				else {
					return null;
				}
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
		public long getCurrentId() {
			return nodeId;
		}
		
		@Override
		public long getNextSiblingId() {
			return prevNodeId;
		}
		
		@Override
		public long getPrevSiblingId() {
			return nextNodeId;
		}

		@Override
		public V delete(final K key) {
			if (key == null) {
				throw new NullPointerException("Key can't be null");
			}
			else {
				K	currentKey = list.containsKey(key) ? key : list.floorKey(key);

				if (currentKey != null) {
					return list.get(currentKey).value.delete(key);
				}
				else {
					return null;
				}
			}
		}

		@Override
		public void insert(final K key, final V value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean canInsert() {
			return list.size() <= InMemoryNodeAccessor.NODE_HIGHWATER;
		}

		@Override
		public boolean canCompact() {
			return list.size() <= InMemoryNodeAccessor.NODE_LOWATER;
		}

		@Override
		public void join() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public LongBPlusTreeNode<K,V>[] split(final long left, final long right) {
			if (left < 0 || right < 0 || left == right) {
				throw new IllegalArgumentException("Left ["+left+"] and right ["+right+"] identifiers can't be negative and must differ each other");
			}
			else {
				final InMemoryIntermediateNode	leftNode = new InMemoryIntermediateNode<>(left, keyClass, valueClass);
				final InMemoryIntermediateNode	rightNode = new InMemoryIntermediateNode<>(right, keyClass, valueClass);
				K	currentKey = list.firstKey();
				
				for(int index = 0, maxIndex = list.size(), middle = list.size() / 2; index < maxIndex; index++) {
					if (index < middle) {
						leftNode.insert(currentKey, list.get(currentKey).value);
					}
					else {
						rightNode.insert(currentKey, list.get(currentKey).value);
					}
					currentKey = list.ceilingKey(currentKey);
				}
				return new LongBPlusTreeNode[] {leftNode, rightNode};
			}
		}
		
		void setNextSiblingId(final long nextId) {
			nextNodeId = nextId;
		}

		void setPrevSiblingId(final long prevId) {
			prevNodeId = prevId;
		}
	}

	private static class KeyValuePair<K extends Comparable<? super K>,V> {
		private final K	key;
		private final V	value;
		
		public KeyValuePair(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return "KeyValuePair [key=" + key + ", value=" + value + "]";
		}
	}
}
