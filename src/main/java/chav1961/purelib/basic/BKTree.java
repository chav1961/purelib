package chav1961.purelib.basic;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class BKTree<Content, Cargo> {
	@FunctionalInterface
	public static interface BiIntFunction<Content> {
		int apply(final Content c1, final Content c2);
	}

	private final Class<Content> 			clazz;
	private final BiIntFunction<Content>	metrics;
	private BKRoot<Content, Cargo>			root;
	
	public BKTree(final Class<Content> clazz, final BiIntFunction<Content> metrics) {
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
	
	public Class<Content> getContentClass() {
		return clazz;
	}
	
	public void add(final Content content, final Cargo cargo) {
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
	
	public void forAll(final Content content, final int maxMetrics, final BiConsumer<Content, Cargo> callback) {
		forAll(root, content, maxMetrics, callback);
	}

	private void add(final BKRoot<Content, Cargo> root, final Content content, final Cargo cargo) {
		final int 	key = metrics.apply(root.content, content);
        int 		low = 0;
        int 		high = root.child.length - 1;

        while (low <= high) {
            final int 	mid = (low + high) >>> 1, midVal = root.child[mid].metric;

            if (midVal < key) {
                low = mid + 1;
            }
            else if (midVal > key) {
            	high = mid - 1;
            }
            else {
            	add(root.child[mid], content, cargo);
            	return;
            }
        }
        root.child = Arrays.copyOf(root.child, root.child.length + 1);
        if (low < root.child.length - 1) {
        	System.arraycopy(root.child, low, root.child, low + 1, root.child.length - low);
        }
    	root.child[low] = new BKRoot<>(content, key, cargo);
	}

	private void forAll(final BKRoot<Content, Cargo> root, final Content content, final int maxMetrics, final BiConsumer<Content, Cargo> callback) {
		final int 	key = metrics.apply(root.content, content);
		final int	from = find(root.child, key - maxMetrics);
		final int	to = find(root.child, key + maxMetrics);
		
		for(int index = from; index <= to; index++) {
			if (root.child[index].metric == key) {
				callback.accept(root.child[index].content, root.child[index].cargo);
			}
			else {
				forAll(root.child[index], content, maxMetrics, callback);
			}
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
		private BKRoot<Content, Cargo>[]	child = new BKRoot[0];
		
		public BKRoot(final Content content, final int metric, final Cargo cargo) {
			this.content = content;
			this.metric = metric;
			this.cargo = cargo;
		}
	}
}
