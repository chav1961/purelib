package chav1961.purelib.ui.swing.useful.interfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.ui.swing.useful.JFileContentManipulator;

/**
 * <p>This interface describes persistence fotr LRU list in the content manupulator</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.5
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
	 * @param lru list to store persistent names
	 * @throws IOException on any I/O exceptions
	 * @since 0.0.5
	 */
	default void saveLRU(String... lru) throws IOException {
		saveLRU(Arrays.asList(lru));
	}
}