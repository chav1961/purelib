package chav1961.purelib.basic;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;

import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.intern.UnsafedUtils;

/**
 * <p>This class is a sort of fast prefix tree. It class implements {@link SyntaxTreeInterface} interface by the And/Or tree algorithm. And/Or tree is a specific implementation of the well-known 
 * <b><a href="https://en.wikipedia.org/wiki/Trie">compressed trie</a></b> (main difference is that keys are associated with the tree <i>nodes</i>, not <i>edges</i>),
 * and it is optimized to work with character arrays only. Any string parameters in the methods to call used will reduce it's performance and memory, so avoid to use
 * them too often. The preferred use case of the tree is parsing character input content.</p>
 *   
 * <p>All the tree consists of of a nodes of three kind:</p>
 * <ul>
 * <li>OR node 
 * <li>AND node 
 * <li>TERM node 
 * </ul>
 *  
 * <p>OR node contains an ordered array of characters and a parallel array with the references to appropriative child nodes. AND node contains a 'substring' of data stored in the tree.
 * As seeking, so placing new data to the tree operates with the char arrays, not {@link String}. The seeking moves char-by-char on the source character array and also traverses from the
 * tree root to depth according to current character in the source array. When the actual tree node is OR-node, program finds (or <i>not</i> finds) current character from the source in 
 * the OR-node char array and traverses to the child was found. When the actual tree node is AND-node, program compares a slice of source array with the 'substring' in the AND-node.
 * This algorithm guarantees, that no one extra comparison will be made during data seeking.</p>
 * <p>And/Or tree is more quick than usual trees and can work directly with the source char arrays instead of converting them to strings.</p>
 * 
 * <p><b>Performance notes:</b></p>
 * <ul>
 * <li>environment: Intel Celeron 1.5GHz 2-core, 64, Windows 8. Windows performance index=3.2. Java SE 1.8-32. JVM settings -Xmx4096m -Xms4096m -d64</li>
 * <li>testing set: 1 million 64-char scratched strings (see test).</li>
 * </ul>
 * <p>Performance result:</p>
 * <ul>
 * <li>placement of non-existent string ~2.8 microseconds/item</li> 
 * <li>placement of existent string ~1.2 microseconds/item</li> 
 * <li>seeking of existent string ~0.9 microseconds/item</li> 
 * </ul>
 *
 * This class is not thread-safe.
 * 
 * @param <T> any king of data associated with the tree elements
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Trie">Trie</a>
 * @see SyntaxTreeInterface
 * @see chav1961.purelib.basic JUnit tests
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.2
 */
public class AndOrTree <T> implements SyntaxTreeInterface<T> {
	private static final int	TYPE_OR = 0; 
	private static final int	TYPE_AND = 1; 
	private static final int	TYPE_TERM = 2;
	private static final int	RANGE_ALLOCATE = 8;
	private static final int	RANGE_STEP = 64;
	
//	private static final OutputStream	os; 
//	private static final PrintWriter	pw;
//	public static boolean print = false; 
//	
//	static {
//		try {
//			os = new FileOutputStream("c:/tmp/z.txt",true);
//			pw = new PrintWriter(os);
//		} catch (FileNotFoundException e) {
//			throw new PreparationException(e); 
//		}
//	}
	
	private final long			step;
	private final int[]			forPosition = new int[1];
	private Node				root = new OrNode();
	private LongIdMap<Node>		revert = new LongIdMap<>(Node.class);
	private int					maxNameLength = 0;
	private long				actualId, amount = 0;

	/**
	 * <p>COnstructor of the class.</p>
	 */
	public AndOrTree() {
		this(1,RANGE_STEP);
	}
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param initialId initial value for automatically generated node ids
	 * @param step step for automatically generated node ids
	 */
	public AndOrTree(final long initialId, final long step) {
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
	public long placeName(final String name, final T cargo) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name to place can't be null or empty");
		}
		else {
			return placeName(name.toCharArray(),0,name.length(),0,cargo,true,true);
		}
	}

	@Override
	public long placeOrChangeName(final String name, final T cargo) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name to place can't be null or empty");
		}
		else {
			return placeName(name.toCharArray(),0,name.length(),0,cargo,true,false);
		}
	}
	
	@Override
	public long placeName(final char[] source, final int from, final int to, final T cargo) {
		return placeName(source,from,to,0,cargo,true,true);
	}

	@Override
	public long placeOrChangeName(final char[] value, final int from, final int to, final T cargo) {
		return placeName(value,from,to,0,cargo,true,false);
	}
	
	@Override
	public long placeName(final char[] source, final int from, final int to, final long id, final T cargo) {
		return placeName(source,from,to,id,cargo,false,true);
	}

	@Override
	public long placeOrChangeName(final char[] source, final int from, final int to, final long id, final T cargo) {
		return placeName(source,from,to,id,cargo,false,false);
	}
	
	@Override
	public long placeName(final String name, final long id, final T cargo) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name to place can't be null or empty");
		}
		else {
			final char[]	content = UnsafedUtils.getStringContent(name);
			
			return placeName(content,0,content.length,id,cargo,false,true);
		}
	}

	@Override
	public long placeOrChangeName(final String name, final long id, final T cargo) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name to place can't be null or empty");
		}
		else {
			final char[]	content = UnsafedUtils.getStringContent(name);
			
			return placeName(content,0,content.length,id,cargo,false,false);
		}
	}
	
	@Override
	public long seekName(final String name) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name to seek can't be null or empty");
		}
		else {
			final char[]	content = UnsafedUtils.getStringContent(name);
			
			return seekName(content, 0, content.length);
		}
	}

	@Override
	public long seekNameI(final String name) {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Name to seek can't be null or empty");
		}
		else {
			final char[]	content = UnsafedUtils.getStringContent(name);
			
			return seekNameI(content, 0, content.length);
		}
	}
	
	@Override
	public long seekName(final char[] source, final int from, final int to) {
		final int	len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source array can't be null or empty");
		}
		else if (from < 0 || from > len) {
			throw new IllegalArgumentException("'from' location ["+from+"] outside the range 0.."+len);
		}
		else if (to < 0 || to > len) {
			throw new IllegalArgumentException("'to' location ["+to+"] outside the range 0.."+len);
		}
		else if (to <= from) {
			throw new IllegalArgumentException("'to' location ["+to+"] not greater than 'from' ["+from+"]");
		}
		else {
			final TermNode	node = (TermNode) seekNameInternal(root,source,from,to,forPosition);

			return node == null ? -forPosition[0]-1 : node.id;
		}
	}

	@Override
	public long seekNameI(final char[] source, final int from, final int to) {
		final int	len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source array can't be null or empty");
		}
		else if (from < 0 || from > len) {
			throw new IllegalArgumentException("'from' location ["+from+"] outside the range 0.."+len);
		}
		else if (to < 0 || to > len) {
			throw new IllegalArgumentException("'to' location ["+to+"] outside the range 0.."+len);
		}
		else if (to <= from) {
			throw new IllegalArgumentException("'to' location ["+to+"] not greater than 'from' ["+from+"]");
		}
		else {
			final TermNode	node = (TermNode) seekNameInternalIgnoreCase(root,source,from,to,forPosition);

			return node == null ? -forPosition[0]-1 : node.id;
		}
	}
	
	
	@Override
	public boolean contains(final long id) {
		if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negtive");
		}
		else {
			return getRevert(id) != null;
		}
	}
	
	@Override
	public int getNameLength(final long id) {
		if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negtive");
		}
		else {
			final Node	node = getRevert(id);
			
			if (node == null) {
				return -1;
			}
			else {
				return ((TermNode)node).nameLen;
			}
		}
	}
	
	@Override
	public String getName(final long id) {
		if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negtive");
		}
		else {
			final Node	node = getRevert(id);
			
			if (node == null) {
				return null;
			}
			else {
				final char[]	result = new char[((TermNode)node).nameLen];
				
				getName(id,result,0);
				return new String(result);
			}
		}
	}
	
	@Override
	public int getName(final long id, final char[] where, final int from) {
		final int	len;
		
		if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negative");
		}
		else if (where == null || (len = where.length) == 0) {
			throw new IllegalArgumentException("where can't be null or empty array");
		}
		else if (from < 0 || from > len) {
			throw new IllegalArgumentException("'from' location ["+from+"] outside the range 0.."+len);
		}
		else {
			final Node	node = getRevert(id);

			if (node == null) {
				return 0;
			}
			else {
				final int	nameLen = ((TermNode)node).nameLen;
				
				if (len-from < nameLen) {
					return -nameLen;
				}
				else {
					fillName(node,where,from+nameLen);
					return from+nameLen;
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getCargo(final long id) {
		if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negtive");
		}
		else {
			final Node	node = getRevert(id);
			
			if (node == null) {
				return null;
			}
			else {
				return (T)((TermNode)node).cargo;
			}
		}
	}

	@Override
	public void setCargo(final long id, final Object cargo) {
		if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negtive");
		}
		else {
			final Node	node = getRevert(id);
			
			if (node != null) {
				((TermNode)node).cargo = cargo;
			}
		}
	}

	@Override
	public boolean removeName(final long id) {
		if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negtive");
		}
		else {
			final Node	node = getRevert(id);
			
			if (node != null) {
				((TermNode)node).id = -1;
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	@Override
	public int compareNames(final long id1, final long id2) {
		if (id1 < 0) {
			throw new IllegalArgumentException("'id' ["+id1+"] need be non-negtive");
		}
		else if (id2 < 0) {
			throw new IllegalArgumentException("'id' ["+id2+"] need be non-negtive");
		}
		else if (id1 == id2) {
			if (getRevert(id1) == null) {
				throw new IllegalArgumentException("Can't compare names because id ["+id1+"] is not exists in the tree");
			}
			else {
				return 0;
			}
		}
		else {
			Node		first = getRevert(id1), second = getRevert(id2);
			
			if (first == null) {
				throw new IllegalArgumentException("Can't compare names because id ["+id1+"] is not exists in the tree");
			}
			else if (second == null) {
				throw new IllegalArgumentException("Can't compare names because id ["+id2+"] is not exists in the tree");
			}
			else {
				Node	temp;
				int		firstDepth = 0, secondDepth = 0, index, delta;
				char	firstChar = 0, secondChar = 0;
				
				for (temp = first; temp != null; temp = temp.parent) {
					firstDepth++;
				}
				for (temp = second; temp != null; temp = temp.parent) {
					secondDepth++;
				}
				
				if (firstDepth < secondDepth) {
					for (index = 0, delta = secondDepth - firstDepth; index < delta; second = second.parent, index++) {
						if (second.type == TYPE_AND) {
							secondChar = ((AndNode)second).chars[0];
						}
					}
					if (first == second) {
						return -1;
					}
				}
				else if (firstDepth > secondDepth) {
					for (index = 0, delta = firstDepth - secondDepth; index < delta; first = first.parent, index++) {
						if (first.type == TYPE_AND) {
							firstChar = ((AndNode)first).chars[0];
						}
					}
					if (first == second) {
						return 1;
					}
				}
				for (index = 0, delta = Math.min(firstDepth,secondDepth); index < delta; first = first.parent, second = second.parent, index++) {
					if (first == second) {
						return firstChar - secondChar;
					}
					if (first.type == TYPE_AND) {
						firstChar = ((AndNode)first).chars[0];
					}
					if (second.type == TYPE_AND) {
						secondChar = ((AndNode)second).chars[0];
					}
				}
			}
			throw new RuntimeException("Internal error, notify developers");
		}
	}

	@Override
	public void walk(final Walker<T> callback) {
		if (callback == null) {
			throw new NullPointerException("Walking callback interface can't be null"); 
		}
		else {
			final char[]	place = new char[maxNameLength];
			
			walk(root,place,0,callback);
		}
	}
	
	@Override
	public long longSize() {
		return amount;
	}
	
	@Override
	public void clear() {
		clear(root);
		root = new OrNode();
		amount = 0;
	}

	private long placeName(final char[] source, final int from, final int to, final long id, final T cargo, final boolean createId, final boolean refreshCargo) {
		final int	len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source array can't be null or empty");
		}
		else if (from < 0 || from > len) {
			throw new IllegalArgumentException("'from' location ["+from+"] outside the range 0.."+len);
		}
		else if (to < 0 || to > len) {
			throw new IllegalArgumentException("'to' location ["+to+"] outside the range 0.."+len);
		}
		else if (to <= from) {
			throw new IllegalArgumentException("'to' location ["+to+"] not greater than 'from' ["+from+"]");
		}
		else if (id < 0) {
			throw new IllegalArgumentException("'id' ["+id+"] need be non-negtive");
		}
		else {
			final TermNode	node = (TermNode) placeNameInternal(source,from,to,false);
			
			if (to-from > maxNameLength) {
				maxNameLength = to - from;
			}
			if (node.id == -1) {	// Newly created node
				if (createId) {
					node.id = actualId;
					actualId += step;
				}
				else {
					node.id = id;
				}
				node.nameLen = to - from;
				placeRevert(node,node.id);
				amount++;
			}
			if (refreshCargo || node.cargo == null) {
				node.cargo = cargo;
			}
			return node.id;
		}
	}
	
	private Node placeNameInternal(final char[] source, int from, final int to, final boolean print) {
		Node		root = this.root, prev = null, newChain;
		AndNode		chain1, chain2, chain3;
		OrNode		chainOr;
		TermNode	chainTerm;
		char		temp[], symbol, midVal;
		int			index, maxIndex, prevIndex = 0, low, high, mid = 0, len, theEnd = Math.min(source.length, to);
		
seek:	for(;;) {
			switch (root.type) {
				case TYPE_OR 	:
//					try{
						symbol = from >= theEnd ? 0 : source[from];
//					} catch (ArrayIndexOutOfBoundsException exc) {
//						symbol = 0;
//					}
//					if (symbol == ':' || symbol < ' ') {
//						final PrintWriter pw = new PrintWriter(System.err);
//						print(pw);
//						pw.flush();
//						int x = 0;
//					}
					temp = ((OrNode)root).chars;
					maxIndex = ((OrNode)root).filled;
					if (print) {
						if (symbol == ':' || symbol < ' ') {
							int x = 0;
						}
						System.err.println("Root: OR {"+symbol+"} for "+Arrays.toString(temp));
					}
					
			        low = 0;	high = maxIndex - 1;
			        while (low <= high) {	// Binary search
			            if ((midVal = temp[mid = (low + high) >>> 1]) < symbol) {
			                low = mid + 1;
			            }
			            else if (midVal > symbol) {
			                high = mid - 1;
			            }
			            else {
							prev = root;
							root = ((OrNode)root).children[prevIndex = mid];
							if (print) {
								System.err.println("Root: OR found at "+mid);
							}
							continue seek;
			            }
			        }
			        
			        mid = low;
					if (maxIndex == temp.length) {		// Expand OR node if needed
						expandOrNode((OrNode)root);
						if (prev == null) {
							this.root = root;
						}
						else if (prev instanceof OrNode) {
							((OrNode)prev).children[prevIndex] = root;
						}
						else if (prev instanceof TermNode) {
							((TermNode)prev).child = root;
						}
						temp = ((OrNode)root).chars;
					}
					
					if ((len = ((OrNode)root).filled - mid) > 0) {
						System.arraycopy(temp,mid,temp,mid+1,len);
						System.arraycopy(((OrNode)root).children,mid,((OrNode)root).children,mid+1,len);
					}
					
					temp[mid] = symbol;			// Place new data in the 'found' cell
					((OrNode)root).children[mid] = newChain = createAndTail(source,from,to);
					newChain.parent = root;
					((OrNode)root).filled++;

					if (print) {
						System.err.println("Root: OR expand ");
					}
					
					return ((AndNode)newChain).child;
				case TYPE_AND 	:
					temp = ((AndNode)root).chars;
					if (print) {
						System.err.println("Root: And for "+Arrays.toString(temp));
					}
					for (index = 0, maxIndex = temp.length; from < to && index < maxIndex; index++, from++) {
						if (temp[index] != source[from]) {	// And strings are different
							if (index == 0) {				// OR node will be in the beginning
								if (print) {
									System.err.println("Root: And +0");
								}
								chainOr = new OrNode();
								chainOr.filled = 2;
								chainOr.parent = root.parent;
								root.parent = chainOr;

								chain1 = createAndTail(source,from,to);
								chain1.parent = chainOr;
								
								if (temp[index] < source[from]) {
									chainOr.chars[0] = temp[index]; 
									chainOr.children[0] = root; 
									chainOr.chars[1] = source[from]; 
									chainOr.children[1] = chain1;
								}
								else {
									chainOr.chars[0] = source[from]; 
									chainOr.children[0] = chain1;
									chainOr.chars[1] = temp[index]; 
									chainOr.children[1] = root; 
								}
								if (prev == null) {
									this.root = chainOr;
								}
								else if (prev instanceof OrNode) {
									((OrNode)prev).children[prevIndex] = chainOr;
								}
								else if (prev instanceof TermNode) {
									((TermNode)prev).child = chainOr;
								}
								root = chainOr;
							}
							else {	// Need cutting strings
								if (print) {
									System.err.println("Root: And +1");
								}
								chain1 = new AndNode(index);
								System.arraycopy(((AndNode)root).chars,0,chain1.chars,0,index);
								chain1.parent = root.parent;
								
								chainOr = new OrNode();
								chain1.child = chainOr;
								chainOr.filled = 2;
								chainOr.parent = chain1;
								
								chain2 = new AndNode(maxIndex-index);
								if ((chain2.child = ((AndNode)root).child) != null) {
									((AndNode)root).child.parent = chain2;
								}
								System.arraycopy(((AndNode)root).chars,index,chain2.chars,0,maxIndex-index);
								chain2.parent = chainOr;
								
								chain3 = createAndTail(source,from,to);
								chain3.parent = chainOr;
								
								if (temp[index] < source[from]) {
									chainOr.chars[0] = temp[index]; 
									chainOr.children[0] = chain2; 
									chainOr.chars[1] = source[from]; 
									chainOr.children[1] = chain3;
								}
								else {
									chainOr.chars[0] = source[from]; 
									chainOr.children[0] = chain3;
									chainOr.chars[1] = temp[index]; 
									chainOr.children[1] = chain2; 
								}
								if (prev == null) {
									this.root = chain1;
								}
								else if (prev instanceof OrNode) {
									((OrNode)prev).children[prevIndex] = chain1;
								}
								else if (prev instanceof TermNode) {
									((TermNode)prev).child = chain1;
								}
								prev = chain1;
								root = chainOr;
							}
							continue seek;
						}
					}
					if (from == to && index < maxIndex) {	// Need cutting strings and insert term inside 
						if (print) {
							System.err.println("Root: And +2");
						}
						chain1 = new AndNode(index);
						System.arraycopy(((AndNode)root).chars,0,chain1.chars,0,index);
						chain1.parent = root.parent;
						
						temp = new char[maxIndex-index];	// Change actual root node!
						System.arraycopy(((AndNode)root).chars,index,temp,0,maxIndex-index);
						((AndNode)root).chars = temp;
						chainTerm = new TermNode(chain1,root);
						root.parent = chainTerm;
						chain1.child = chainTerm;
						
						if (prev == null) {
							this.root = chain1;
						}
						else if (prev instanceof OrNode) {
							((OrNode)prev).children[prevIndex] = chain1;
						}
						else if (prev instanceof TermNode) {
							((TermNode)prev).child = chain1;
						}
						return chainTerm;
					}
					else if (((AndNode)root).child == null) {
						if (print) {
							System.err.println("Root: And +3");
						}
						return ((AndNode)root).child = new TermNode(root,null);
					}
					else {
						prev = root;
						root = ((AndNode)root).child;
					}
					if (print) {
						System.err.println("Root: And +4");
					}
					break;
				case TYPE_TERM	:
					if (from == to) {
						if (print) {
							System.err.println("Term: 1");
						}
						return root;
					}
					else if (((TermNode)root).child == null) {
						((TermNode)root).child = newChain = createAndTail(source,from,to);
						newChain.parent = root;
						if (print) {
							System.err.println("Term: 2");
						}
						return ((AndNode)newChain).child;
					}
					else {
						prev = root; 
						root = ((TermNode)root).child;
					}
					if (print) {
						System.err.println("Term: 3");
					}
					break;
				default			:
					throw new UnsupportedOperationException();
			}
		}
	}

	private static Node seekNameInternal(Node root, final char[] source, int from, final int to, final int[] forPosition) {
		char	temp[], symbol, midVal;
		int		index, maxIndex, low, high, mid;
		
seek:	while (root != null && from < to) {
			switch (root.type) {
				case TYPE_OR 	:
					symbol = source[from];
					temp = ((OrNode)root).chars;
					maxIndex = ((OrNode)root).filled;

			        low = 0;	high = maxIndex - 1;
			        while (low <= high) {	// Binary search
			            if ((midVal = temp[mid = (low + high) >>> 1]) < symbol) {
			                low = mid + 1;
			            }
			            else if (midVal > symbol) {
			                high = mid - 1;
			            }
			            else {
							root = ((OrNode)root).children[mid];
							continue seek;
			            }
			        }
					root = null;
					break seek;
				case TYPE_AND 	:
					temp = ((AndNode)root).chars;
					for (index = 0, maxIndex = temp.length; index < maxIndex && from < to; index++, from++) {
						if (temp[index] != source[from]) {
							root = null;
							break seek;
						}
					}
					if (index < maxIndex) {
						root = null;
						break seek;
					}
					else {
						root = ((AndNode)root).child;
					}
					break;
				case TYPE_TERM	:
					if (from == to) {
						break seek;
					}
					else {
						root = ((TermNode)root).child;
					}
					break;
			}
		}
		forPosition[0] = from;
		if (from == to) {
			if (root instanceof TermNode) {
				return root;
			}
			else if ((root instanceof OrNode) && ((OrNode)root).chars[0] == '\0') {
				return ((AndNode)((OrNode)root).children[0]).child;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	private static Node seekNameInternalIgnoreCase(Node root, final char[] source, int from, final int to, final int[] forPosition) {
		char	temp[], symbol, midVal;
		int		index, maxIndex, low, high, mid;
		
seek:	while (root != null && from < to) {
			switch (root.type) {
				case TYPE_OR 	:
					symbol = Character.toUpperCase(source[from]);
					temp = ((OrNode)root).chars;
					maxIndex = ((OrNode)root).filled;

			        low = 0;	high = maxIndex - 1;
			        while (low <= high) {	// Binary search
			            if ((midVal = Character.toUpperCase(temp[mid = (low + high) >>> 1])) < symbol) {
			                low = mid + 1;
			            }
			            else if (midVal > symbol) {
			                high = mid - 1;
			            }
			            else {
							root = ((OrNode)root).children[mid];
							continue seek;
			            }
			        }
					symbol = Character.toLowerCase(source[from]);
			        low = 0;	high = maxIndex - 1;
			        while (low <= high) {	// Binary search
			            if ((midVal = Character.toLowerCase(temp[mid = (low + high) >>> 1])) < symbol) {
			                low = mid + 1;
			            }
			            else if (midVal > symbol) {
			                high = mid - 1;
			            }
			            else {
							root = ((OrNode)root).children[mid];
							continue seek;
			            }
			        }
					root = null;
					break seek;
				case TYPE_AND 	:
					temp = ((AndNode)root).chars;
					for (index = 0, maxIndex = temp.length; index < maxIndex && from < to; index++, from++) {
						if (Character.toUpperCase(temp[index]) != Character.toUpperCase(source[from]) && Character.toLowerCase(temp[index]) != Character.toLowerCase(source[from])) {
							root = null;
							break seek;
						}
					}
					if (index < maxIndex) {
						root = null;
						break seek;
					}
					else {
						root = ((AndNode)root).child;
					}
					break;
				case TYPE_TERM	:
					if (from == to) {
						break seek;
					}
					else {
						root = ((TermNode)root).child;
					}
					break;
			}
		}
		forPosition[0] = from;
		if (from == to) {
			if (root instanceof TermNode) {
				return root;
			}
			else if ((root instanceof OrNode) && ((OrNode)root).chars[0] == '\0') {
				return ((AndNode)((OrNode)root).children[0]).child;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	public void print(final PrintWriter ps) {
		print(this.root,ps,"");
	}
	
	protected void print(final Node root, final PrintWriter ps, final String prefix) {
		if (root != null) {
			ps.print(prefix);
			switch (root.type) {
				case TYPE_OR 	:
					ps.println("OR: "+Arrays.toString(((OrNode)root).chars)+" "+((OrNode)root).filled);
					for (int index = 0, maxIndex = ((OrNode)root).filled; index < maxIndex; index++) {
						ps.print(prefix);
						ps.println("*OR: {"+((OrNode)root).chars[index]+"}");
						print(((OrNode)root).children[index], ps, prefix+"  ");
					}
					break;
				case TYPE_AND 	:
					ps.println("AND: "+Arrays.toString(((AndNode)root).chars));
					print(((AndNode)root).child, ps, prefix+"  ");
					break;
				case TYPE_TERM	:
					ps.println("TERM: "+((TermNode)root).id+", child "+(((TermNode)root).child != null ? "presents" : "missing"));
					print(((TermNode)root).child, ps, prefix+"  ");
					break;
				default:
					ps.println("ANother!!!");
			}
		}
	}	
	
	@SuppressWarnings("unchecked")
	private boolean walk(final Node root, final char[] place, final int from, final Walker<T> callback) {
		if (root != null) {
			if (root.type == TYPE_OR) {
				for (int index = 0, maxIndex = ((OrNode)root).filled; index < maxIndex; index++) {
					if (!walk(((OrNode)root).children[index],place,from,callback)) {
						return false;
					}
				}
				return true;
			}
			else if (root.type == TYPE_AND) {
				final int	dataLen = ((AndNode)root).chars.length; 
				
				System.arraycopy(((AndNode)root).chars,0,place,from,dataLen);
				return walk(((AndNode)root).child,place,from+dataLen,callback);
			}
			else if (root.type == TYPE_TERM) {
				if (!callback.process(place,from,((TermNode)root).id,(T)((TermNode)root).cargo)) {
					return false;
				}
				else {
					return walk(((TermNode)root).child,place,from,callback);
				}
			}
		}
		return true;
	}

	private void placeRevert(final Node node, final long id) {
		revert.put(id,node);
	}

	private Node getRevert(final long id) {
		return revert.get(id);
	}

	private static void clear(final Node root) {
		if (root != null) {
			if (root.type == TYPE_OR) {
				for (int index = 0, maxIndex = ((OrNode)root).filled; index < maxIndex; index++) {
					clear(((OrNode)root).children[index]);
					((OrNode)root).children[index] = null;
				}
				((OrNode)root).chars = null;
				((OrNode)root).children = null;
			}
			else if (root.type == TYPE_AND) {
				clear(((AndNode)root).child);
				
				((AndNode)root).chars = null; 
				((AndNode)root).child = null; 
			}
			else if (root.type == TYPE_TERM) {
				((TermNode)root).cargo = null;
				((TermNode)root).child = null;
			}
			root.parent = null;
		}
	}
	
	private static void fillName(Node node, final char[] where, int endPoz) {
		while (node != null) {
			if (node.type == TYPE_AND) {
				final int	len = ((AndNode)node).chars.length;
				System.arraycopy(((AndNode)node).chars,0,where,endPoz-len,len);
				endPoz -= len;
			}
			node = node.parent;
		}
	}
	
	private static AndNode createAndTail(final char[] source, final int from, final int to) {
		final int		len = to - from;
		final AndNode	result = new AndNode(len);
		final TermNode	term = new TermNode(result,null);

		System.arraycopy(source,from,result.chars,0,len);
		result.child = term;
		return result;
	}

	private static void expandOrNode(final OrNode root) {
		final int		oldLen = ((OrNode)root).chars.length, newLen = oldLen + RANGE_ALLOCATE;
		final char[]	newChars = new char[newLen];
		final Node[]	newChildren = new Node[newLen];
		
		System.arraycopy(root.chars,0,newChars,0,oldLen);
		System.arraycopy(root.children,0,newChildren,0,oldLen);
		((OrNode)root).chars = newChars;
		((OrNode)root).children = newChildren;
	}

	private static class Node {
		int			type;
		Node 		parent;
	}
	
	private static class OrNode extends Node {
		int			filled;
		char[]		chars;
		Node[]		children;
		
		public OrNode() {
			this(RANGE_ALLOCATE);
		}

		public OrNode(int size) {
			this.type = TYPE_OR; 
			this.filled = 0;
			this.chars = new char[size];
			this.children = new Node[size];
		}
		
		@Override
		public String toString() {
			return "OrNode [filled=" + filled + ", chars=" + Arrays.toString(chars) + ", type=" + type + ", parent=" + parent + "]";
		}
	}

	private static class AndNode extends Node {
		char[]		chars;
		Node		child;
		
		public AndNode(int charSize) {
			this.type = TYPE_AND; 
			this.chars = new char[charSize];
			this.child = null;
		}

		@Override
		public String toString() {
			return "AndNode [chars=" + Arrays.toString(chars) + ", type=" + type + ", parent=" + parent + "]";
		}
	}
	
	private static class TermNode extends Node {
		long		id = -1;
		int			nameLen;
		Object		cargo;
		Node		child;
		
		public TermNode(Node parent, Node child) {
			this.type = TYPE_TERM; 
			this.parent = parent;
			this.child = child;
		}

		@Override
		public String toString() {
			return "TermNode [id=" + id + ", cargo=" + cargo + ", type=" + type + ", parent=" + parent + "]";
		}
	}
}
