package chav1961.purelib.sql;

import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chav1961.purelib.basic.Utils;

/**
 * <p>This class implements in-memory {@linkplain Struct} to use in the SQL.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.sql
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public class InMemoryLittleStruct implements Struct {
	private final String				sqlTypeName;
	private final List<AttributePair>	content = new ArrayList<>();
	
	/**
	 * <p>Constructor of the class</p>
	 * @param sqlTypeName type name
	 * @throws IllegalArgumentException when type name is null or empty
	 */
	public InMemoryLittleStruct(final String sqlTypeName) throws IllegalArgumentException {
		if (sqlTypeName == null || sqlTypeName.isEmpty()) {
			throw new IllegalArgumentException("SQL rtype name can't be null or empty");
		}
		else {
			this.sqlTypeName = sqlTypeName;
		}
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param sqlTypeName type name
	 * @param content key/value pairs to store
	 * @throws IllegalArgumentException when type name is null or empty
	 */
	public InMemoryLittleStruct(final String sqlTypeName, final AttributePair... content) throws IllegalArgumentException {
		this(sqlTypeName);
		addPairs(content);
	}

	/**
	 * <p>Add pairs to existent content</p>
	 * @param content key/value pairs to add
	 * @throws NullPointerException when content is null
	 */
	public void addPairs(final AttributePair... content) throws NullPointerException {
		if (content == null) {
			throw new NullPointerException("Content to add can't be null"); 
		}
		else {
next:		for (int index = 0; index < content.length; index++) {
				if (content[index] == null) {
					throw new NullPointerException("Null pair inside content at position ["+index+"]"); 
				}
				else {
					final String	key = content[index].key;
					
					for (int listIndex = 0, maxListIndex = this.content.size(); listIndex < maxListIndex; listIndex++) {
						if (this.content.get(listIndex).key.equals(key)) {
							this.content.set(listIndex,content[index]);
							continue next;
						}
					}
					this.content.add(content[index]);
				}
			}
		}
	}

	/**
	 * <p>Remove keys from content</p>
	 * @param names key names to remove
	 * @throws NullPointerException when key names is null or contains nulls inside
	 * @throws IllegalArgumentException
	 */
	public void removeAttributes(final String... names) throws NullPointerException, IllegalArgumentException {
		if (names == null || Utils.checkArrayContent4Nulls(names) >= 0) {
			throw new NullPointerException("Content to remove can't be null"); 
		}
		else {
			for (int index = 0; index < names.length; index++) {
				if (names[index] == null || names[index].isEmpty()) {
					throw new IllegalArgumentException("Null or empty name inside list at position ["+index+"]"); 
				}
				else {
					for (int listIndex = this.content.size()-1; listIndex >= 0; listIndex--) {
						if (this.content.get(listIndex).key.equals(names[index])) {
							this.content.remove(listIndex);
							break;
						}
					}
				}
			}
		}
	}
	
	@Override
	public String getSQLTypeName() throws SQLException {
		return sqlTypeName;
	}

	@Override
	public Object[] getAttributes() throws SQLException {
		final Object[]	result = new Object[content.size()];
		
		for (int index = content.size()-1; index >= 0; index--) {
			result[index] = content.get(index).value;
		}
		return result;
	}

	@Override
	public Object[] getAttributes(final Map<String, Class<?>> map) throws SQLException {
		if (map == null) {
			throw new NullPointerException("Map can't be null"); 
		}
		else {
			return getAttributes();
		}
	}

	/**
	 * <p>This class describes key/value attributes to store into {@linkplain InMemoryLittleStruct} content</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public static final class AttributePair {
		public final String	key;
		public final Object	value;
		
		/**
		 * <p>Constructor of the class</p>
		 * @param key key of the pair
		 * @param value value of the pair
		 * @throws IllegalArgumentException when key os null or empty
		 */
		public AttributePair(final String key, final Object value) {
			if (key == null || key.isEmpty()) {
				throw new IllegalArgumentException("Attribyte key can't be null or empty"); 
			}
			else {
				this.key = key;
				this.value = value;
			}
		}

		@Override
		public String toString() {
			return "AttributePair [key=" + key + ", value=" + value + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			AttributePair other = (AttributePair) obj;
			if (key == null) {
				if (other.key != null) return false;
			} else if (!key.equals(other.key)) return false;
			if (value == null) {
				if (other.value != null) return false;
			} else if (!value.equals(other.value)) return false;
			return true;
		}
	}
}
