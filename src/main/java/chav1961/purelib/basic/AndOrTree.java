package chav1961.purelib.basic;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;


/**
 * <p>This class implements {@link SyntaxTreeInterface} interface by the And/Or tree algorithm.</p>
 *   
 * <p> The idea of this tree was described by D.Knuth. All the tree consists of of a nodes of two kind:</p>
 * <ul>
 * <li>OR node 
 * <li>AND node 
 * </ul> 
 * <p>OR node contains an ordered array of characters and a parallel array with the references to appropriative child nodes. AND node contains a 'substring' of data stored in the tree.
 * As seeking, so placing new data to the tree operates with the char arrays, not {@link String}. The seeking moves char-by-char on the source character array and also traverses from the
 * tree root to depth according to current character in the source array. When the actual tree node is OR-node, program finds (or <i>not</i> finds) current character from the source in 
 * the OR-node char array and traverses to the child was found. When the actual tree node is AND-node, program compares a slice of source array with the 'substring' in the AND-node.
 * This algorithm guarantees, that no one extra comparison will be made during data seeking.</p>
 * <p>And/Or tree It is more quick than usual trees and can work directly with the source char arrays instead of converting them to strings</p>
 * 
 * @param <T> any king of data associated with the tree elements
 * 
 * @see SyntaxTreeInterface
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class AndOrTree<T> implements SyntaxTreeInterface<T> {
	private static final int		ENUM_STEP = 16;		
	private static final int		TYPE_OR = 0;
	private static final int		TYPE_AND = 1;

	
	private long					actualId, amount = 0;
	private AndOrNode<T>			nameRoot = new OrNode<>();
	private TreeIds<T>				idRoot = new TreeIds<T>();
	
	public AndOrTree(final long fromId) {
		this.actualId = fromId;		
		((OrNode<?>)this.nameRoot).childrenChar = new char[0]; 
	}

	@Override
	public long placeName(final char[] value, final int from, final int to, final T cargo) {
		int		forValue;
		
		if (value == null || (forValue = value.length) == 0) {
			throw new IllegalArgumentException("Value can't be null or empty array"); 
		}
		else if (from < 0 || from >= forValue) {
			throw new IllegalArgumentException("From position ["+from+"] outside the array! Need be in 0.."+value.length); 
		}
		else if (to < 0 || to > forValue) {
			throw new IllegalArgumentException("To position ["+to+"] outside the array! Need be in 0.."+value.length); 
		}
		else if (from > to) {
			throw new IllegalArgumentException("From position ["+from+"] is greatr than to position ["+to+"]"); 
		}
		else {
			final AndOrNode<T>	node = placeName(value,from,to);
			
			if (node.container != null) {
				if (cargo != null) {
					setCargo(node.container.stringId,cargo);
				}
				return node.container.stringId;
			}
			else {
				node.container = new CargoContainer<T>(node,to-from,actualId,cargo);
				placeContainer(actualId,node);
				actualId += ENUM_STEP;
				amount++;
				return node.container.stringId;
			}
		}
	}

	@Override
	public long placeName(final char[] value, final int from, final int to, final long id, final T cargo) {
		int		forValue;
		
		if (value == null || (forValue = value.length) == 0) {
			throw new IllegalArgumentException("Value can't be null or empty array"); 
		}
		else if (from < 0 || from >= forValue) {
			throw new IllegalArgumentException("From position ["+from+"] outside the array! Need be in 0.."+value.length); 
		}
		else if (to < 0 || to > forValue) {
			throw new IllegalArgumentException("To position ["+to+"] outside the array! Need be in 0.."+value.length); 
		}
		else if (from > to) {
			throw new IllegalArgumentException("From position ["+from+"] is greatr than to position ["+to+"]"); 
		}
		else {
			final AndOrNode<T>	node = placeName(value,from,to);
			
			if (node.container != null) {
				if (cargo != null) {
					setCargo(node.container.stringId,cargo);
				}
				return node.container.stringId;
			}
			else {
				node.container = new CargoContainer<T>(node,to-from,id,cargo);
				placeContainer(id,node);
				amount++;
				return node.container.stringId;
			}
		}
	}	
	
	@Override
	public long placeName(final String name, final T cargo) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else {
			final char[]	value = name.toCharArray();
			
			return placeName(value,0,value.length,cargo);
		}
	}
	
	@Override
	public long seekName(final char[] value, final int from, final int to) {
		int		forValue;
		
		if (value == null || (forValue = value.length) == 0) {
			throw new IllegalArgumentException("Value can't be null or empty array"); 
		}
		else if (from < 0 || from >= forValue) {
			throw new IllegalArgumentException("From position ["+from+"] outside the array! Need be in 0.."+value.length); 
		}
		else if (to < 0 || to > forValue) {
			throw new IllegalArgumentException("To position ["+to+"] outside the array! Need be in 0.."+value.length); 
		}
		else if (from > to) {
			throw new IllegalArgumentException("From position ["+from+"] is greatr than to position ["+to+"]"); 
		}
		else {
			final int[]			terminated = new int[1];
			final AndOrNode<T>	node = getName(value,from,to,terminated);
			
			if (node == null) {
				return from - terminated[0] - 1;
			}
			else if (node.container == null || node.container.counter <= 0) {
				return from - terminated[0] - 1;
			}
			else {
				return node.container.stringId;
			}
		}
	}

	@Override
	public long seekName(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name can't be null or empty");
		}
		else {
			return seekName(name.toCharArray(),0,name.length());
		}
	}
	
	
	@Override
	public boolean removeName(final long id) {
		final CargoContainer<T>	descr = getContainer(id);
		
		if (descr != null) {
			if (--descr.counter <= 0) {
				descr.counter = 0;
				return false;
			}
			else {
				return true;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public T getCargo(final long id) {
		final CargoContainer<T>	descr = getContainer(id);
		
		if (descr != null && descr.counter > 0) {
			return descr.cargo;
		}
		else {
			return null;
		}
	}

	@Override
	public void setCargo(final long id, final T cargo) {
		final CargoContainer<T>	descr = getContainer(id);
		
		if (descr != null && descr.counter > 0) {
			descr.cargo = cargo;
		}
		else {
			throw new IllegalArgumentException("Attempt to set cargo for non-existent id ["+id+"]");
		}
	}

	@Override
	public boolean contains(final long id) {
		return getNameLength(id) != -1;
	}
	
	@Override
	public int getNameLength(final long id) {
		final CargoContainer<T>	descr = getContainer(id);
		
		if (descr != null && descr.counter > 0) {
			return descr.sourceLength;
		}
		else {
			return -1;
		}
	}

	@Override
	public String getName(final long id) {
		final CargoContainer<T>	descr = getContainer(id);
		
		if (descr != null && descr.counter > 0) {
			final char[]	result = new char[descr.sourceLength];
			
			getName(id,result,0);
			return new String(result);
		}
		else {
			return null;
		}
	}

	@Override
	public int getName(final long id, final char[] target, final int from) {
		final CargoContainer<T>	descr = getContainer(id);
		
		if (descr != null && descr.counter > 0) {
			if (from + descr.sourceLength <= target.length) {
				AndOrNode<?>	actual = descr.andOrNode;
				int			partSize;
				
				for (int index = from + descr.sourceLength - 1; index >= from;) {
					if (actual.type == TYPE_AND) {
						partSize = ((AndNode<?>)actual).chainChar.length;
						System.arraycopy(((AndNode<?>)actual).chainChar,0,target,index-partSize+1,partSize);
						index -= partSize; 
						target[index--] = ((AndNode<?>)actual).parentChar;
					}
					actual = actual.parent;
				}
				return from + descr.sourceLength;
			}
			else {
				return - (from + descr.sourceLength);
			}
		}
		else {
			return from;
		}
	}

	@Override
	public int compareNames(final long first, final long second) {
		if (first == second) {
			return 0;
		}
		else {
			final String 	firstName = getName(first);
			
			if (firstName == null) {
				return getName(second) == null ? 0 : -1;
			}
			else {
				return firstName.compareTo(getName(second));
			}
		}
	}

	@Override
	public void walk(final Walker walker) {
		if (walker == null) {
			throw new IllegalArgumentException("Walker interface can't be null");
		}
		else {
			walk(idRoot,walker);
		}
	}
	
	private void walk(final TreeIds<T> idRoot, final Walker walker) {
		for (TreeIds<T> item : idRoot.children) {
			if (item != null) {
				walk(item,walker);
			}
			if (idRoot.reference != null) {
				if (idRoot.reference.container != null && idRoot.reference.container.stringId != -1) {
					walker.process(idRoot.reference.container.stringId);
				}
			}
		}
	}

	@Override public long size() {return amount;}

	@Override
	public void clear() {
		try{remove(nameRoot);	remove(idRoot);
		} catch (IOException e) {
		} finally {
			nameRoot = null;	idRoot = null;
			amount = 0;			actualId %= ENUM_STEP; 
		}
	}
	
/*	void debugPrint(final PrintStream ps) {
		ps.println("--- cargos: ");
		debugPrint("",idRoot,ps);
		ps.println("--- tree:");
		debugPrint("",nameRoot,ps);
		ps.println("--- end");
	}
 	
	private void debugPrint(final String blanks, final AndOrNode<T> node, final PrintStream ps) {
		if (node != null) {
			if (node.container != null) {
				if (node.container.andOrNode != node) {
					ps.println(blanks+"ERR: "+node.container+" contains corrupted reference to the tree");
				}
				ps.println(blanks+"name="+getName(node.container.stringId));
			}
			if (node.parent == null && blanks.length() > 0) {
				ps.println(blanks+"ERR: missing parent (type="+node.type+")!");
			}
			switch (node.type) {
				case AndOrNode.TYPE_AND	:
					ps.println(blanks+new String(((AndNode<T>)node).chainChar)+" "+node.container);
					if (((AndNode<T>)node).child != null) {
						debugPrint(blanks+"   ",((AndNode<T>)node).child,ps);
					}
					break;
				case AndOrNode.TYPE_OR	:
					for (int index = 0; index < ((OrNode<T>)node).childrenChar.length; index++) {
						ps.println(blanks+'['+((OrNode<T>)node).childrenChar[index]+']');
						debugPrint(blanks+"   ",((OrNode<T>)node).children[index],ps);
					}
					break;
			}
		}
	}

	private void debugPrint(final String blanks, final TreeIds<T> node, final PrintStream ps) {
		if (node != null) {
			if (node.reference != null) {
				System.err.println(blanks+node.reference);
			}
			if (node.children != null) {
				for (int index = 0; index < node.children.length; index++) {
					if (node.children[index] != null) {
						debugPrint(blanks+"   ",node.children[index],ps);
					}
				}
			}
		}
	}
*/
	
	private AndOrNode<T> placeName(final char[] source, final int from, final int to) {
		AndOrNode<T>	root = nameRoot;
		int				poz = from, place;
		
repeat:	while (poz < to) {
			if (root.type == TYPE_OR) {
				if ((place = Arrays.binarySearch(((OrNode<?>)root).childrenChar,source[poz])) < 0) {	// Place is missing - insert new branch in the tree
					final char[]			newChars = new char[((OrNode<T>)root).childrenChar.length+1];
					final AndOrNode<T>[]	newRefs = new AndOrNode[((OrNode<T>)root).childrenChar.length+1];
					
					if (place != -1) {		// Expand ordered lists in the OR-node
						System.arraycopy(((OrNode<?>)root).childrenChar,0,newChars,0,-place-1);
						System.arraycopy(((OrNode<?>)root).children,0,newRefs,0,-place-1);
					}
					newChars[-place-1] = source[poz];
					if (-place < newChars.length) {
						System.arraycopy(((OrNode<T>)root).childrenChar,-place-1,newChars,-place,newChars.length+place);
						System.arraycopy(((OrNode<T>)root).children,-place-1,newRefs,-place,newRefs.length+place);
					}
					((OrNode<T>)root).childrenChar = newChars;
					((OrNode<T>)root).children = newRefs;
					
					final AndNode<T>		tail = new AndNode<T>();	// Create AND-node and place it to the tree
					
					tail.parent = root;		tail.parentChar = source[poz];
					tail.chainChar = new char[to-poz-1];
					
					if (tail.chainChar.length > 0) {
						System.arraycopy(source,poz+1,tail.chainChar,0,tail.chainChar.length);
					}
					return newRefs[-place-1] = tail;
				}
				else {
					root = ((OrNode<T>)root).children[place];
					poz++;
				}
			}
			else {
				place = poz;
				for (int maxLen = ((AndNode<?>)root).chainChar.length; poz < to && (poz-place) < maxLen; poz++) {
					if (((AndNode<?>)root).chainChar[poz-place] != source[poz]) {	// Comparison failed - cut existent AND node text and insert chained OR and AND node in the cutting
						cutAndNode((AndNode<T>)root,poz-place);						// Cut AND-node and continue placement
						root = ((AndNode<T>)root).child;
						continue repeat;
					}
				}
				if (poz < to) {
					if (((AndNode<?>)root).child == null) {	// This is a leaf - expand tree with chained OR and AND node.
						final OrNode<T>		newOr = new OrNode<T>();
						final AndNode<T>	newAnd = new AndNode<T>();
						
						newOr.childrenChar = new char[]{source[poz]};
						newOr.children = new AndOrNode[]{newAnd};
						newOr.parent = root;
						
						newAnd.parentChar = source[poz];
						newAnd.parent = newOr;
						newAnd.chainChar = new char[to-poz-1];
						
						if (newAnd.chainChar.length > 0) {
							System.arraycopy(source,poz+1,newAnd.chainChar,0,newAnd.chainChar.length);
						}
						((AndNode<T>)root).child = newOr;
						
						return newAnd;
					}
					else {
						root = ((AndNode<T>)root).child;
					}
				}
				else if ((poz-place) < ((AndNode<T>)root).chainChar.length) {	// Truncation of the existent node is needed
					cutAndNode((AndNode<T>)root,poz-place);
					break repeat;
				}
				else {
					break repeat;
				}
			}
		}
		return root;
	}
	
	private void placeContainer(final long id, final AndOrNode<T> cargo) {
		final int[]	ids = id2Shorts(id);

		TreeIds<T>	start = idRoot;
		for (int index = 0; index <= 3; index++) {
			if (start.children == null) {
				start.children = new TreeIds[65536];
			}
			if (start.children[ids[index]] == null) {
				start.children[ids[index]] = new TreeIds<T>();
			}
			start = start.children[ids[index]];
		}
		if (start.reference != null && start.reference.container != null) {
			if (start.reference.container.stringId != cargo.container.stringId) {
				throw new IllegalStateException("Point1 store");
			}
		}
		start.reference = cargo;
	}

	private AndOrNode<T> getName(final char[] source, int from, final int to, final int[] terminated) {
		AndOrNode<T>	root = nameRoot;
		char[]			temp;
		char			midVal, key;
		int				place, maxLen, low, high, mid;
		
repeat:	while (from < to) {
			if (root.type == TYPE_OR) {
				key = source[from];
				temp = ((OrNode<T>)root).childrenChar;				
				low = 0;	high = temp.length-1;

				while (low <= high) {
					midVal = temp[mid = (low + high) >>> 1];

					if (midVal < key) low = mid + 1;
					else if (midVal > key) high = mid - 1;
					else {
						root = ((OrNode<T>)root).children[mid];
						from++;
						continue repeat;
					}
				}
				terminated[0] = from;
				return null;
			}
			else {
				temp = ((AndNode<T>)root).chainChar;
				
				for (place = 0, maxLen = temp.length; from < to && place < maxLen; from++, place++) {
					if (temp[place] != source[from]) {
						terminated[0] = from;
						return null;
					}
				}
				if (from < to) {
					if (((AndNode<T>)root).child == null) {
						terminated[0] = from;
						return null;
					}
					else {
						root = ((AndNode<T>)root).child;
					}
				}
				else {
					terminated[0] = from;
					return place < maxLen ? null : root;
				}
			}
		}
		terminated[0] = from;
		return root;
	}
	
	private CargoContainer<T> getContainer(final long id) {
		final int[]	ids = id2Shorts(id);
		
		TreeIds<T>	start = idRoot;
		for (int index = 0; index <= 2; index++) {
			if (start.children == null) {
				return null;
			}
			else if (start.children[ids[index]] == null) {
				return null;
			}
			else {
				start = start.children[ids[index]];
			}
		}
		if (start.children[ids[3]] != null) {
			if (((AndNode<T>)start.children[ids[3]].reference) != null && ((AndNode<T>)start.children[ids[3]].reference).container != null) {
				if (id != ((AndNode<T>)start.children[ids[3]].reference).container.stringId) {
					throw new IllegalStateException("Err load ref [id="+id+"]: "+(AndNode<T>)start.children[ids[3]].reference);
				}
			}
			return ((AndNode<T>)start.children[ids[3]].reference).container;
		}
		else {
			return null;
		}
	}


	private void cutAndNode(final AndNode<T> node, final int poz) {
		final OrNode<T>		newOr = new OrNode<T>();
		final AndNode<T>	newAnd = new AndNode<T>();
		
		newOr.childrenChar = new char[]{node.chainChar[poz]};
		newOr.children = new AndOrNode[]{newAnd};
		newOr.parent = node;
		
		newAnd.chainChar = new char[node.chainChar.length-poz-1];
		if (newAnd.chainChar.length > 0) {
			System.arraycopy(node.chainChar,poz+1,newAnd.chainChar,0,newAnd.chainChar.length);
		}
		newAnd.parentChar = node.chainChar[poz];
		newAnd.child = node.child;
		newAnd.parent = newOr;
		if ((newAnd.container = node.container) != null) {
			newAnd.container.andOrNode = newAnd;
			placeContainer(node.container.stringId,newAnd);
			node.container = null;
		}
		
		final char[]	newChain = new char[poz];
		if (poz > 0) {
			System.arraycopy(node.chainChar,0,newChain,0,poz);
		}
		
		if (node.child != null) {
			((AndNode<T>)node).child.parent = newAnd;
		}
		node.child = newOr;
		node.chainChar = newChain;
	}

	private void remove(final AndOrNode<T> node) throws IOException {
		if (node != null) {
			switch (node.type) {
				case TYPE_OR	:
					if (((OrNode<T>)node).children != null) {
						for (AndOrNode<T> item : ((OrNode<T>)node).children) {
							remove(item);
						}
					}
					break;
				case TYPE_AND	:
					remove(((AndNode<T>)node).child);
					break;
			}
			node.clear();
		}
	}

	private void remove(final TreeIds<T> node) throws IOException {
		if (node != null ) {
			if (node.children != null) {
				for (TreeIds<T> item : node.children) {
					if (item != null) {
						remove(item);
					}
				}
			}
			node.clear();
		}
	}
	
	private static final int[] id2Shorts(long id) {
		id = id & 0x00FFFFFFFFFFFFFFL;
		return new int[]{(int)((id >> 48) & 0xFFFF),(int)((id >> 32) & 0xFFFF),(int)((id >> 16) & 0xFFFF),(int)(id & 0xFFFF)}; 
	}	
	
	private static class AndOrNode<T> {
		public int					type;
		public AndOrNode<T>			parent;
		public CargoContainer<T>	container;
		
		@Override
		public String toString() {
			return "AndOrNode [type=" + type + ", container=" + container + "]";
		}
		
		public void clear() {
			parent = null;			container = null;
		}
	}
	
	private static class AndNode<T> extends AndOrNode<T> {
		public char					parentChar;
		public char[]				chainChar;
		public AndOrNode<T>			child;
		{type=TYPE_AND;}

		@Override
		public void clear() {
			super.clear();			chainChar = null;
			child = null;
		}
		
		@Override
		public String toString() {
			return "AndNode [parentChar=" + parentChar + ", chainChar=" + Arrays.toString(chainChar) + ", child=" + child + ", container = " + container + "]";
		}
	}

	private static class OrNode<T> extends AndOrNode<T> {
		public char[]				childrenChar;
		public AndOrNode<T>[]		children;
		{type=TYPE_OR;}
		
		@Override
		public void clear() {
			super.clear();			children = null;
			childrenChar = null;
		}
		
		@Override
		public String toString() {
			return "OrNode [childrenChar=" + Arrays.toString(childrenChar) + ", children=" + Arrays.toString(children) + ", container=" + container + "]";
		}
	}
	
	private static class TreeIds<T> {
		public TreeIds<T>[]			children;
		public AndOrNode<T>			reference;
		
		public void clear() {
			children = null;		reference = null;
		}
	}
	
	private static class CargoContainer<T> {
		public final int		sourceLength;
		public int				counter = 1;
		public final long		stringId;
		public T				cargo;
		public AndOrNode<T>		andOrNode;
		
		public CargoContainer(final AndOrNode<T> andOrNode, final int length, final long stringId, final T cargo) {
			this.andOrNode= andOrNode;
			this.stringId = stringId;
			this.cargo = cargo;
			this.sourceLength = length;
		}

		@Override
		public String toString() {
			return "CargoContainer [sourceLength=" + sourceLength + ", stringId=" + stringId + ", counter=" + counter + ", cargo=" + cargo + "]";
		}
	}
}
