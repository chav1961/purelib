package chav1961.purelib.ui.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;

/**
 * <p>This interface describes persistence fotr LRU list in the content manupulator</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @last.update 0.0.7
 */
public interface LRUPersistence {
	/**
	 * <p>Dummy persistence (to ignore persistence functionality)</p>
	 * @since 0.0.5
	 */
	LRUPersistence		DUMMY = new LRUPersistence(){
							@Override public void loadLRU(List<String> lru) throws IOException {}
							@Override public void saveLRU(List<String> lru) throws IOException {}
						}; 
	/**
	 * <p>Load LRU persistence. Usually is called from the constructor</p>
	 * @param lru list to fill persistent names
	 * @throws IOException on any I/O exceptions
	 */
	void loadLRU(List<String> lru) throws IOException;

	/**
	 * <p>Load LRU persistence. Usually is called from the constructor</p>
	 * @param name persistence name. Can't be null or empty
	 * @param lru list to fill persistent names
	 * @throws IOException on any I/O exceptions
	 * @since 0.0.6
	 */
	default void loadLRU(String name, List<String> lru) throws IOException {
		loadLRU(lru);
	}
	
	/**
	 * <p>Load LRU persistence. Usually is called from the constructor</p>
	 * @return persistence list loaded. Can be empty but not null
	 * @throws IOException on any I/O exceptions
	 * @since 0.0.5
	 */
	default String[] loadLRU() throws IOException {
		final List<String>	result = new ArrayList<>();
		
		loadLRU(result);
		return result.toArray(new String[result.size()]);
	}
	
	/**
	 * <p>Store LRU persistence. Usually is called from the {@linkplain AutoCloseable#close()} methods</p>
	 * @param lru list to store persistent names
	 * @throws IOException on any I/O exceptions
	 */
	void saveLRU(List<String> lru) throws IOException;

	/**
	 * <p>Store LRU persistence. Usually is called from the {@linkplain AutoCloseable#close()} methods</p>
	 * @param name persistence name. Can't be null or empty
	 * @param lru list to store persistent names
	 * @throws IOException on any I/O exceptions
	 * @since 0.0.6
	 */
	default void saveLRU(String name, List<String> lru) throws IOException {
		saveLRU(lru);
	}
	
	/**
	 * <p>Store LRU persistence. Usually is called from the {@linkplain AutoCloseable#close()} methods</p>
	 * @param lru list to store persistent names
	 * @throws IOException on any I/O exceptions
	 * @since 0.0.5
	 */
	default void saveLRU(String... lru) throws IOException {
		saveLRU(Arrays.asList(lru));
	}
	
	/**
	 * <p>Get simple implementation of the interface based on {@linkplain SubstitutableProperties} file content. Properties file content will contains a set of 
	 * keys names &lt;keyPrefix&gt;.1, &lt;keyPrefix&gt;.2 ... &lt;keyPrefix&gt;.N. Their values will contain path to last recently used files. Values of list filled will be always
	 * ordered by it's keys (as suffix numbers, not lexical ordered) in the properties file. Sequence of the keys can have missing items (for example "item.1", "item.3", "item.4" etc)</p>
	 * @param properties properties file. Can't be null. If exists, must be a file with read and write access to it
	 * @param keyPrefix prefix of keys inside properties. Can't be null or empty
	 * @return implementation of {@linkplain LRUPersistence}. Can't be null
	 * @since 0.0.7
	 */
	public static LRUPersistence of(final File properties, final String keyPrefix) throws NullPointerException, IllegalArgumentException {
		if (properties == null) {
			throw new NullPointerException("Properties file can't be null");
		}
		else if (Utils.checkEmptyOrNullString(keyPrefix)) {
			throw new IllegalArgumentException("Key rpefix can't be null or empty string"); 
		}
		else {
			if (properties.exists()) {
				if (properties.isDirectory() || !properties.canRead() || !properties.canWrite()) {
					throw new IllegalArgumentException("Properties file path ["+properties.getAbsolutePath()+"] is not a file or doesn't have read/write access to it"); 
				}
			}
			final Pattern	pattern = Pattern.compile("\\Q"+keyPrefix+"\\E\\.(\\d+)");
			
			return new LRUPersistence() {
				@Override
				public void loadLRU(final List<String> lru) throws IOException {
					if (lru == null) {
						throw new NullPointerException("List to fill loaded values to can't be null");
					}
					else {
						final SubstitutableProperties	props = new SubstitutableProperties();
						
						if (props.tryLoad(properties, PureLibSettings.NULL_LOGGER)) {
							final List<String[]> items = new ArrayList<>();
							
							for (String item : props.availableKeys(pattern)) {
								items.add(new String[] {item, props.getProperty(item)});
							}
							items.sort((o1, o2)-> {
								final Matcher	m1 = pattern.matcher(o1[0]);
								final Matcher	m2 = pattern.matcher(o2[0]);
								
								if (m1.find() && m2.find()) {
									return Integer.valueOf(m1.group(1)) - Integer.valueOf(m2.group(1));
								}
								else {
									return o1[0].compareTo(o2[0]);
								}
							});
							for (String[] item : items) {
								lru.add(item[1]);
							}
						}
					}
				}
				
				@Override
				public void saveLRU(final List<String> lru) throws IOException {
					if (lru == null) {
						throw new NullPointerException("List to get values to store can't be null");
					}
					else {
						final SubstitutableProperties	props = new SubstitutableProperties();
						int	index = 1;
						
						props.tryLoad(properties, PureLibSettings.NULL_LOGGER);
						props.remove(pattern);
						for (String item : lru) {
							props.setProperty(keyPrefix+'.'+index, item);
							index++;
						}
						props.store(properties);
					}
				}
			};
		}
	}
}