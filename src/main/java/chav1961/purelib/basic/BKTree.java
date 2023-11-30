package chav1961.purelib.basic;

import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * <p>This class implements Burkhard-Keller tree.</p>
 * 
 * <p>This class is not thread-safe</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @param <Content> content item type inside the tree
 * @param <Cargo> any object associated with the item
 * @see https://en.wikipedia.org/wiki/BK-tree
 */
public class BKTree<Content, Cargo> {
	/**
	 * <p>This interface describes function to calculate metrics (for example, Levenshtein distance) for tree element pairs</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 * @see https://en.wikipedia.org/wiki/Levenshtein_distance
	 * @param <Content> content to calculate metrics
	 */
	@FunctionalInterface
	public static interface BiIntFunction<Content> {
		/**
		 * <p>Calculate metrics between two elements</p>
		 * @param c1 first element to calculate. Can't be null
		 * @param c2 second element to calculate. Can't be null
		 * @return distance calculated. Can't be negative
		 */
		int apply(final Content c1, final Content c2);
	}

	private final Class<Content> 			clazz;
	private final BiIntFunction<Content>	metrics;
	private BKRoot<Content, Cargo>			root;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param clazz content type inside the tree. Can't be null
	 * @param metrics function to calculate metrics. Can't be null
	 */
	public BKTree(final Class<Content> clazz, final BiIntFunction<Content> metrics) throws NullPointerException {
		if (clazz == null) {
			throw new NullPointerException("Content class can't be null"); 
		}
		else if (metrics == null) {
			throw new NullPointerException("Metric function can't be null"); 
		}
		else {
			this.clazz = clazz;
			this.metrics = metrics;
			this.root = null;
		}
	}
	
	/**
	 * <p>Get tree node content type.</p>
	 * @return content class. Can't be null
	 */
	public Class<Content> getContentType() {
		return clazz;
	}
	
	/**
	 * <p>Add item to the tree.</p>
	 * @param content item content to add. Can't be null
	 * @param cargo cargo associated. Can be null
	 * @throws NullPointerException when content is null
	 * @throws IllegalArgumentException on attempt to add duplicate content
	 */
	public void add(final Content content, final Cargo cargo) throws NullPointerException, IllegalArgumentException {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null"); 
		}
		else if (root == null) {
			root = new BKRoot<>(content, 0, cargo);
		}
		else {
			add(root, content, cargo);
		}
	}

	/**
	 * <p>Test the tree contains the given content.</p> 
	 * @param content content to test. Can't be null
	 * @return true if contains, false otherwise
	 * @throws NullPointerException when content is null
	 * @see #BKTree(Class, BiIntFunction)
	 */
	public boolean contains(final Content content) throws NullPointerException {
		if (content == null) {
			throw new NullPointerException("Content to test can't be null"); 
		}
		else {
			return contains(root, content);
		}
	}
	
	/**
	 * <p>Walk all tree items</p>
	 * @param callback function processed on every items. Can't be null
	 * @throws NullPointerException when callback is null
	 */
	public void walk(final BiConsumer<Content, Cargo> callback) throws NullPointerException {
		if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			walk(root, callback);
		}
	}
	
	/**
	 * <p>Wall all tree items with metrics not more than typed. All the tree nodes with metrics less than or equals to max metrics will be processed</p>
	 * @param content content to calculate tree node metrics relative to. Can't be null
	 * @param maxMetrics maximal metric difference between content and current tree node.
	 * @param callback function processed on items found. Can't be null
	 * @throws NullPointerException when content of callback is null
	 * @throws IllegalArgumentException when maxMetrics is negative
	 */
	public void walk(final Content content, final int maxMetrics, final BiConsumer<Content, Cargo> callback) throws NullPointerException, IllegalArgumentException {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (maxMetrics < 0) {
			throw new IllegalArgumentException("Max metrics ["+maxMetrics+"] can't be negative");
		}		
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			forAll(root, content, maxMetrics, callback);
		}
	}

	private void add(final BKRoot<Content, Cargo> root, final Content content, final Cargo cargo) {
		final int 	key = metrics.apply(root.content, content);
		
		if (key == 0) {
			throw new IllegalArgumentException("Duplicate value ["+content+"] to add"); 
		}
		else {
	        int 		low = 0;
	        int 		high = root.children.length - 1;
	
	        while (low <= high) {
	            final int 	mid = (low + high) >>> 1, midVal = root.children[mid].metric;
	
	            if (midVal < key) {
	                low = mid + 1;
	            }
	            else if (midVal > key) {
	            	high = mid - 1;
	            }
	            else {
	            	add(root.children[mid], content, cargo);
	            	return;
	            }
	        }
	        root.children = Arrays.copyOf(root.children, root.children.length + 1);
	        if (low < root.children.length - 1) {
	        	System.arraycopy(root.children, low, root.children, low + 1, root.children.length - low - 1);
	        }
	    	root.children[low] = new BKRoot<>(content, key, cargo);
		}
	}

	private boolean contains(final BKRoot<Content, Cargo> root, final Content content) {
		if (metrics.apply(root.content, content) == 0) {
			return true;
		}
		else {
			for(BKRoot<Content, Cargo> item : root.children) {
				if (contains(item, content)) {
					return true;
				}
			}
			return false;
		}
	}
	
	private void walk(final BKRoot<Content, Cargo> root, final BiConsumer<Content, Cargo> callback) {
		callback.accept(root.content, root.cargo);
		for (BKRoot<Content, Cargo> item : root.children) {
			walk(item, callback);
		}
	}
	
	private void forAll(final BKRoot<Content, Cargo> root, final Content content, final int maxMetrics, final BiConsumer<Content, Cargo> callback) {
		final int 	key = metrics.apply(root.content, content);
		final int	from = find(root.children, key - maxMetrics);
		final int	to = Math.min(root.children.length - 1, find(root.children, key + maxMetrics));
		
		if (key <= maxMetrics) {
			callback.accept(root.content, root.cargo);
		}
		for(int index = from; index <= to; index++) {
			forAll(root.children[index], content, maxMetrics, callback);
		}
	}
	
	private int find(final BKRoot<Content, Cargo>[] root, final int key) {
        int 		low = 0, high = root.length - 1, mid = 0, midVal;
        
        while (low <= high) {
            mid = (low + high) >>> 1;
			midVal = root[mid].metric;

            if (midVal < key) {
                low = mid + 1;
            }
            else if (midVal > key) {
            	high = mid - 1;
            }
            else {
            	return mid;
            }
        }
        return mid;
	}
	
	private static class BKRoot<Content, Cargo> {
		private final Content				content;
		private final int 					metric;			
		private final Cargo					cargo;
		private BKRoot<Content, Cargo>[]	children = new BKRoot[0];
		
		public BKRoot(final Content content, final int metric, final Cargo cargo) {
			this.content = content;
			this.metric = metric;
			this.cargo = cargo;
		}

		@Override
		public String toString() {
			return "BKRoot [content=" + content + ", metric=" + metric + ", cargo=" + cargo + "]";
		}
	}
}
