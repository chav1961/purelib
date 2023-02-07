package chav1961.purelib.i18n;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class KeyCollection {
	protected final SyntaxTreeInterface<String[]>	keysAndValues = new AndOrTree<>();
	protected final SyntaxTreeInterface<URI>		helpRefs = new AndOrTree<>();

	KeyCollection(final Map<String,String> keysAndValues, final Map<String,URI> helpRefs) {
		for (Entry<String, String> item : keysAndValues.entrySet()) {
			this.keysAndValues.placeName((CharSequence)item.getKey(),new String[]{item.getKey(),item.getValue()});
		}
		for (Entry<String, URI> item : helpRefs.entrySet()) {
			this.helpRefs.placeName((CharSequence)item.getKey(),item.getValue());
		}
	}
	
	@Override
	public String toString() {
		return "KeyCollection [keysAndValues=" + keysAndValues + ", helpRefs=" + helpRefs + "]";
	}

	public boolean containsKey(final String key) {
		return keysAndValues.seekName(key) >= 0;
	}

	public String getValue(final String key) {
		return keysAndValues.getCargo(keysAndValues.seekName(key))[1];
	}

	public void setValue(final String key, final String value) {
		keysAndValues.getCargo(keysAndValues.seekName(key))[1] = value;
	}

	public void replaceKey(final String oldKey, final String newKey) {
		final String	value = getValue(oldKey);
		
		removeKey(oldKey);
		keysAndValues.placeName((CharSequence)newKey,new String[]{newKey, value});
	}

	public void addValue(final String key, final String value) {
		final long	id = keysAndValues.seekName(key); 
		
		if (id >= 0) {
			throw new IllegalArgumentException("Duplicaite key ["+key+"] to add");
		}
		else {
			keysAndValues.placeName((CharSequence)value, new String[] {key, value});
		}
	}
	
	public Iterable<String> keys() {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return keysIterator();
			}
		};
	}
	
	public Iterator<String> keysIterator() {
		final List<String>	result = new ArrayList<>();
		
		keysAndValues.walk((name,len,id,cargo)->{
			result.add(cargo[0]);
			return true;
		});
		return result.iterator();
	}

	public boolean containsHelp(final String key) {
		return helpRefs.seekName(key) >= 0;
	}

	public URI getHelpURI(final String key) {
		return helpRefs.getCargo(helpRefs.seekName(key));
	}
	
	public Iterable<String> helps() {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return helpsIterator();
			}
		};
	}
	
	public Iterator<String> helpsIterator() {
		final List<String>	result = new ArrayList<>();
		
		helpRefs.walk((name,len,id,cargo)->{
			result.add(new String(name,0,len));
			return true;
		});
		return result.iterator();
	}
	
	public void removeKey(final String key) {
		final long	id = keysAndValues.seekName(key);
		
		keysAndValues.removeName(id);
	}
}