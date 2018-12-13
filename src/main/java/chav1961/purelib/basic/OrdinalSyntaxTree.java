package chav1961.purelib.basic;

import java.util.Map.Entry;
import java.util.TreeMap;

import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class OrdinalSyntaxTree<Cargo> implements SyntaxTreeInterface<Cargo> {
	private static final int					RANGE_STEP = 64;
	
	private final TreeMap<String,Node<Cargo>>	map = new TreeMap<>();
	private final LongIdMap<Node>				invMap = new LongIdMap<Node>(Node.class);
	private final long							step;
	private long								actualId = 0;
	private int									maxNameLen = 0;

	public OrdinalSyntaxTree() {
		this(1,RANGE_STEP);
	}
	
	public OrdinalSyntaxTree(final long initialId, final long step) {
		if (initialId <= 0) {
			throw new IllegalArgumentException("'initialId' ["+initialId+"] need be positive");
		}
		else if (step <= 0 || step > RANGE_STEP) {
			throw new IllegalArgumentException("'step' ["+step+"] out of range 1.."+RANGE_STEP);
		}
		else {
			this.actualId = initialId;
			this.step = step;
		}
	}
	
	@Override
	public long placeName(final char[] value, final int from, final int to, final Cargo cargo) {
		return placeName(value,from,to,newId(),cargo);
	}

	@Override
	public long placeOrChangeName(final char[] value, final int from, final int to, final Cargo cargo) {
		return placeOrChangeName(value,from,to,newId(),cargo);
	}

	@Override
	public long placeName(final String name, final Cargo cargo) {
		return placeName(name,newId(),cargo);
	}

	@Override
	public long placeOrChangeName(final String name, final Cargo cargo) {
		return placeOrChangeName(name,newId(),cargo);
	}

	@Override
	public long placeName(final char[] value, final int from, final int to, final long id, final Cargo cargo) {
		if (value == null || value.length == 0) {
			throw new IllegalArgumentException("Source array can't be null or empty");
		}
		else if (from < 0 || from >= value.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(value.length-1));
		}
		else if (to < 0 || to > value.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+(value.length));
		}
		else if (to < from) {
			throw new IllegalArgumentException("To position ["+to+"] less than from ["+from+"]");
		}
		else {
			return placeName(new String(value,from,to-from),id,cargo);
		}
	}

	@Override
	public long placeOrChangeName(final char[] source, final int from, final int to, final long id, final Cargo cargo) {
		if (source == null || source.length == 0) {
			throw new IllegalArgumentException("Source array can't be null or empty");
		}
		else if (from < 0 || from >= source.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(source.length-1));
		}
		else if (to < 0 || to >= source.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+(source.length-1));
		}
		else {
			final long	nameId = seekName(source,from,to); 
			
			if (nameId < 0) {
				return placeName(source,from,to,id,cargo);
			}
			else {
				setCargo(id,cargo);
				return nameId;
			}
		}
	}

	@Override
	public long placeName(final String name, final long id, final Cargo cargo) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name to place can't be null or empty");
		}
		else {
			final Node<Cargo>	item = new Node<Cargo>(name,id,cargo); 
			
			map.put(name,item);
			invMap.put(id,item);
			maxNameLen = Math.max(maxNameLen,name.length());
			return id;
		}
	}

	@Override
	public long placeOrChangeName(final String name, final long id, final Cargo cargo) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name to place can't be null or empty");
		}
		else {
			final long	nameId = seekName(name); 
			
			if (nameId < 0) {
				return placeName(name,id,cargo);
			}
			else {
				setCargo(id,cargo);
				return nameId;
			}
		}
	}

	@Override
	public long seekName(final char[] value, final int from, final int to) {
		if (value == null || value.length == 0) {
			throw new IllegalArgumentException("Source array can't be null or empty");
		}
		else if (from < 0 || from >= value.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(value.length-1));
		}
		else if (to < 0 || to > value.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+(value.length));
		}
		else if (to < from) {
			throw new IllegalArgumentException("To position ["+to+"] less than from ["+from+"]");
		}
		else {
			return seekName(new String(value,from,to-from));
		}
	}

	@Override
	public long seekName(final String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Source string can't be null or empty");
		}
		else {
			final Node<Cargo>	node = map.get(name);
			
			if (node != null) {
				return node.id;
			}
			else {
				final Entry<String, Node<Cargo>>	less = map.floorEntry(name);
				final Entry<String, Node<Cargo>>	greater = map.ceilingEntry(name);
				final int	lessLen = less != null ? difference(less.getValue().key,name) : 0;
				final int	greaterLen = greater != null ? difference(name,greater.getValue().key) : 0;
				
				return -1 - Math.max(lessLen, greaterLen);
			}
		}
	}

	@Override
	public boolean removeName(final long id) {
		final Node<Cargo>	node = seekNode(id);
		
		if (node != null) {
			map.remove(node.key);
			invMap.get(id).cargo = null;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public Cargo getCargo(final long id) {
		if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negtive");
		}
		else {
			final Node<Cargo>	node = seekNode(id);
			
			return node == null ? null : node.cargo;
		}
	}

	@Override
	public void setCargo(final long id, final Cargo cargo) {
		if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negtive");
		}
		else {
			final Node<Cargo>	node = seekNode(id);
			
			if (node != null) {
				node.cargo = cargo;
			}
		}
	}

	@Override
	public boolean contains(final long id) {
		return seekNode(id) != null;
	}

	@Override
	public int getNameLength(final long id) {
		if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negtive");
		}
		else {
			final String	name = getName(id);
			
			if (name == null) {
				return -1;
			}
			else {
				return name.length();
			}
		}
	}

	@Override
	public String getName(final long id) {
		if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negtive");
		}
		else {
			final Node<Cargo>	node = seekNode(id);
			
			if (node != null) {
				return node.key;
			}
			else {
				return null;
			}
		}
	}

	@Override
	public int getName(final long id, final char[] target, final int from) {
		if (target == null || target.length == 0) {
			throw new IllegalArgumentException("Target array can't be null or empty");
		}
		else if (from < 0 || from >= target.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(target.length-1));
		}
		else {
			final String	name = getName(id);
			
			if (name == null) {
				return from;
			}
			else {
				if (from+name.length() > target.length) {
					return -(from+name.length());
				}
				else {
					final int	to = from+name.length();
					
					name.getChars(0,name.length(),target,from);
					return to;
				}
			}
		}
	}

	@Override
	public int compareNames(final long first, final long second) {
		final Node<Cargo>	firstNode = seekNode(first), secondNode = seekNode(second);

		if (firstNode != null && secondNode != null) {
			return firstNode.key.compareTo(secondNode.key);
		}
		else {
			return -1;
		}
	}

	@Override
	public void walk(final Walker<Cargo> walker) {
		if (walker == null) {
			throw new NullPointerException("Walker can't be null");
		}
		else {
			final char[]	buffer = new char[maxNameLen];
			
			for (Entry<String, Node<Cargo>> item : map.entrySet()) {
				final int	nameLen = item.getKey().length();
				
				item.getKey().getChars(0,nameLen-1,buffer,0);
				if (!walker.process(buffer,nameLen,item.getValue().id,item.getValue().cargo)) {
					break;
				}
			}
		}
	}

	@Override
	public long size() {
		return map.size();
	}

	@Override
	public void clear() {
		map.clear();
	}

	private long newId() {
		final long	result = actualId;
		
		actualId += step;
		return result;
	}

	private Node<Cargo> seekNode(final long nodeId) {
		final Node<Cargo>	cargo = invMap.get(nodeId);
		
		return cargo;
	}

	private int difference(final String first, final String second) {
		final int	len = Math.min(first.length(),second.length());
		int			diff;
		
		for (int index = 0; index < len; index++) {
			if ((diff = first.charAt(index) - second.charAt(index)) != 0) {
				return index;
			}
		}
		return len;
	}

	private static class Node<Cargo> {
		private long	id;
		private String	key;
		private Cargo	cargo;

		public Node(String key, long id, Cargo cargo) {
			this.key = key;
			this.id = id;
			this.cargo = cargo;
		}
	}
}
