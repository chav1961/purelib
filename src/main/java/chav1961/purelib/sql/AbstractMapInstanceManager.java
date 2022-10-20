package chav1961.purelib.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.sql.interfaces.InstanceManager;

public abstract class AbstractMapInstanceManager<Key> implements InstanceManager<Key, Map<String, Object>>{
	private final String				keyName;
	private final Class<?>				keyType;
	private final String[]				names;
	private final boolean				isReadOnly;
	
	protected AbstractMapInstanceManager(final boolean isReadOnly, final String keyName, final Class<?> keyType, final String... names) {
		if (keyName == null || keyName.isEmpty()) {
			throw new IllegalArgumentException("Key name can't be null or empty");
		}
		else if (keyType == null) {
			throw new IllegalArgumentException("Key type can't be null or empty");
		}
		else if (names == null || names.length == 0 || Utils.checkArrayContent4Nulls(names, true) >= 0) {
			throw new IllegalArgumentException("Name list is null, empty or contains nulls/empties inside");
		}
		else if (!Set.of(names).contains(keyName)) {
			throw new IllegalArgumentException("Key name ["+keyName+"] is missing in the name list "+Arrays.toString(names));
		}
		else {
			this.isReadOnly = isReadOnly;
			this.keyName = keyName;
			this.keyType = keyType;
			this.names = names;
		}
	}

	@Override public abstract Key newKey() throws SQLException;
	@Override public abstract Key extractKey(final ResultSet rs) throws SQLException;
	@Override public abstract void storeInstance(PreparedStatement ps, Map<String, Object> inst, boolean update) throws SQLException;
	protected abstract Object cloneValue(final Object item2Clone) throws CloneNotSupportedException;

	@Override
	public void close() throws SQLException {
	}
	
	@Override
	public Class<?> getKeyType() {
		return keyType;
	}

	@Override
	public Class<?> getInstanceType() {
		return Map.class;
	}

	@Override
	public boolean isReadOnly() {
		return isReadOnly;
	}

	@Override
	public Map<String, Object> newInstance() throws SQLException {
		final Map<String, Object>	content = new HashMap<>();
		
		for(String item : names) {
			content.put(item, null);
		}
		return content;
	}

	@Override
	public Map<String, Object> clone(final Map<String, Object> inst) throws SQLException {
		final Map<String, Object>	clone = new HashMap<>();
		
		for (Entry<String, Object> item : inst.entrySet()) {
			try{clone.put(item.getKey(), cloneValue(item.getValue()));
			} catch (CloneNotSupportedException e) {
				throw new SQLException("Item ["+item.getKey()+"] is not cloneable", e);
			}
		}
		return clone;
	}

	@Override
	public Key extractKey(final Map<String, Object> inst) throws SQLException {
		if (inst == null) {
			throw new NullPointerException("Instance to extract key from can't be null"); 
		}
		else if (!inst.containsKey(keyName)) {
			throw new SQLException("Key name ["+keyName+"] is missing in the instance");
		}
		else {
			return (Key) inst.get(keyName);
		}
	}

	@Override
	public void assignKey(final Map<String, Object> inst, final Key key) throws SQLException {
		if (inst == null) {
			throw new NullPointerException("Instance to assign key to can't be null"); 
		}
		else if (key == null) {
			throw new NullPointerException("Key value to assign can't be null"); 
		}
		else {
			inst.put(keyName, key);
		}
	}


	@Override
	public void loadInstance(final ResultSet rs, final Map<String, Object> inst) throws SQLException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null"); 
		}
		else if (inst == null) {
			throw new NullPointerException("Instance to load content can't be null"); 
		}
		else {
			for (Entry<String, Object> item : inst.entrySet()) {
				item.setValue(rs.getObject(item.getKey()));
			}
		}
	}

	@Override
	public void storeInstance(final ResultSet rs, final Map<String, Object> inst, final boolean update) throws SQLException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null"); 
		}
		else if (inst == null) {
			throw new NullPointerException("Instance to store content can't be null"); 
		}
		else {
			for (Entry<String, Object> item : inst.entrySet()) {
				rs.updateObject(item.getKey(), item.getValue());
			}
		}
	}

	@Override
	public <T> T get(final Map<String, Object> inst, final String name) throws SQLException {
		if (inst == null) {
			throw new NullPointerException("Instance to get content from can't be null"); 
		}
		else if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else if (!inst.containsKey(name)) {
			throw new IllegalArgumentException("Name ["+name+"] is missing in the instance"); 
		}
		else {
			return (T)inst.get(name);
		}
	}

	@Override
	public <T> InstanceManager<Key, Map<String, Object>> set(final Map<String, Object> inst, final String name, final T value) throws SQLException {
		if (inst == null) {
			throw new NullPointerException("Instance to get content from can't be null"); 
		}
		else if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else if (!inst.containsKey(name)) {
			throw new IllegalArgumentException("Name ["+name+"] is missing in the instance"); 
		}
		else {
			inst.put(name, value);
			return this;
		}
	}
}
