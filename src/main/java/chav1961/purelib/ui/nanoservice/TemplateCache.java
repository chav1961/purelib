package chav1961.purelib.ui.nanoservice;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

class TemplateCache<T> implements Closeable {
	volatile CacheRecord<T>[]		content = new CacheRecord[0];
	final ReentrantReadWriteLock	lock = new ReentrantReadWriteLock();

	@FunctionalInterface
	interface WalkCallback {
		boolean process(String[] keys);
	}
	
	@Override
	public void close() throws IOException {
		clear();
	}
	
	int size() {
		final ReadLock 	readLock = lock.readLock();
		
		try{readLock.lock();
	        return content.length;
		} finally {
			readLock.unlock();
		}
	}
	
	boolean contains(final String... parameters) {
		if (parameters == null || parameters.length == 0) {
			throw new IllegalArgumentException("Parameters can't be null or empty array");
		}
		else {
			return getInternal(parameters) != null;
		}
	}
	
	void add(final T item, final String... parameters) {
		if (item == null) {
			throw new NullPointerException("Item to add can't be null");
		}
		else if (parameters == null || parameters.length == 0) {
			throw new IllegalArgumentException("Parameters can't be null or empty array");
		}
		else if (!contains(parameters)) {
			final WriteLock 	writeLock = lock.writeLock();
			
			try{writeLock.lock();
				final TemplateCache.CacheRecord<T>[]	temp = Arrays.copyOf(content,content.length+1);
				
				temp[content.length] = new TemplateCache.CacheRecord<T>(parameters,item);
				Arrays.sort(temp);
				content = temp;
			} finally {
				writeLock.unlock();
			}
		}
		else {
			getInternal(parameters).useCounter.incrementAndGet();
		}
	}
	
	T get(final String... parameters) {
		if (parameters == null || parameters.length == 0) {
			throw new IllegalArgumentException("Parameters can't be null or empty array");
		}
		else {
			return getInternal(parameters).value;
		}
	}

	T remove(final String... parameters) {
		if (parameters == null || parameters.length == 0) {
			throw new IllegalArgumentException("Parameters can't be null or empty array");
		}
		else {
			final TemplateCache.CacheRecord<T>	found = getInternal(parameters);
			
			if (found.useCounter.decrementAndGet() <= 0) {
				final WriteLock 	writeLock = lock.writeLock();
				
				try{writeLock.lock();
		        	CacheRecord<T> 	midVal;
			        int 			low = 0, high = content.length - 1, mid, compareResult;
			
			        while (low <= high) {
			            mid = (low + high) >>> 1;
						midVal = content[mid];
						compareResult = midVal.compareTo(parameters); 
			
			            if (compareResult < 0) {
			                low = mid + 1;
			            }
			            else if (compareResult > 0) {
			                high = mid - 1;
			            }
			            else {
			            	if (mid < content.length-1) {
				            	System.arraycopy(content,mid+1,content,mid,content.length-mid-1);
			            	}
			                content = Arrays.copyOf(content,content.length-1);
			                return midVal.value;
			            }
			        }
				} finally {
					writeLock.unlock();
				}
			}
			return found.value;
		}
	}
	
	void clear() {
		final WriteLock 	writeLock = lock.writeLock();
		
		try{writeLock.lock();
			content = new CacheRecord[0];
		} finally {
			writeLock.unlock();
		}
	}

	boolean walk(final WalkCallback callback) {
		if (callback == null) {
			throw new NullPointerException("Callback interface can't be null");
		}
		else {
			final ReadLock 	readLock = lock.readLock();
			
			try{readLock.lock();
				for (CacheRecord<T> item : content) {
					if (!callback.process(item.key)) {
						return false;
					}
				}
				return true;
			} finally {
				readLock.unlock();
			}
		}
	}
	
	private CacheRecord<T> getInternal(final String... parameters) {
		for (int index = 0; index < parameters.length; index++) {
			if (parameters[index] == null || parameters[index].isEmpty()) {
				throw new IllegalArgumentException("Parameter list "+Arrays.toString(parameters)+" contains null or empty value at position ["+index+"]");
			}
		}
		
		final ReadLock 	readLock = lock.readLock();
		
		try{readLock.lock();
        	CacheRecord<T> 	midVal;
	        int 			low = 0, high = content.length - 1, mid, compareResult;
	
	        while (low <= high) {
	            mid = (low + high) >>> 1;
				midVal = content[mid];
				compareResult = midVal.compareTo(parameters); 
	
	            if (compareResult < 0) {
	                low = mid + 1;
	            }
	            else if (compareResult > 0) {
	                high = mid - 1;
	            }
	            else {
	                return midVal;
	            }
	        }
	        return null;
		} finally {
			readLock.unlock();
		}
	}

	private static class CacheRecord<T> implements Comparable<CacheRecord<T>> {
		final String[]		key;
		final T				value;
		final AtomicInteger	useCounter = new AtomicInteger(1);
		
		private CacheRecord(final String[] key, final T value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return "CacheRecord [key=" + Arrays.toString(key) + ", value=" + value + "]";
		}

		@Override
		public int compareTo(final CacheRecord<T> another) {
			if (another != null) {
				return compareTo(another.key);
			}
			else {
				return 1;
			}
		}
		
		public int compareTo(final String[] another) {
			int	result = this.key.length - another.length;
			
			if (result != 0) {
				return result;
			}
			else {
				for (int index = 0; index < this.key.length; index++) {
					if ((result = this.key[index].compareTo(another[index])) != 0) {
						return result;
					}
				}
				return 0;
			}
		}
	}
}