package chav1961.purelib.basic;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import chav1961.purelib.concurrent.LightWeightRWLockerWrapper;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper.Locker;

/**
 * <p>This class implements {@linkplain InitialContextFactory} interface to get access to shared {@linkplain Map} instance.</p>   
 *
 */
public class SimpleInitialContextFactory implements InitialContextFactory {
	private static final NameParser				PARSER = new SimpleNameParserImpl(); 
	
	private final Map<String, Object>			content = new HashMap<>();
	private final LightWeightRWLockerWrapper	locker = new LightWeightRWLockerWrapper();
	
	public SimpleInitialContextFactory() {
	}

	@Override
	public Context getInitialContext(final Hashtable<?, ?> environment) throws NamingException {
		return new SimpleContextImpl(environment);
	}

	private class SimpleContextImpl implements Context {
		private final SimpleContextImpl		parent;
		private final Name					prefix;
		private final Hashtable<?, ?>		environment;
		private final Map<String, Context>	subcontents = new HashMap<>(); 
		private boolean						closed = false;
		
		private SimpleContextImpl(final Hashtable<?, ?> environment) {
			this.parent = null;
			this.prefix = null;
			this.environment = environment;
		}

		private SimpleContextImpl(final SimpleContextImpl parent, final Name prefix) {
			this.parent = parent;
			this.prefix = prefix;
			this.environment = new Hashtable<>();
		}
		
		@Override
		public Object lookup(final Name name) throws NamingException {
			if (name == null) {
				throw new NullPointerException("Name can't ne null");
			}
			else if (parent != null) {
				return parent.lookup(composeName(name, prefix));
			}
			else {
				final String	str = name.toString();
				
				try(final Locker	l = locker.lock(true)) {
					if (content.containsKey(str)) {
						return content.get(str);
					}
					else {
						return null;
					}
				}
			}
		}

		@Override
		public Object lookup(final String name) throws NamingException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can't be null or empty");
			}
			else {
				return lookup(getNameParser(name).parse(name));
			}
		}

		@Override
		public void bind(final Name name, final Object obj) throws NamingException {
			if (name == null) {
				throw new NullPointerException("Name can't ne null");
			}
			else if (obj == null) {
				throw new NullPointerException("Object to bind can't ne null");
			}
			else if (parent != null) {
				parent.bind(composeName(name, prefix), obj);
			}
			else {
				final String	str = name.toString();
				
				try(final Locker	l = locker.lock(false)) {
					if (content.containsKey(str)) {
						throw new NamingException("Name ["+str+"] is already binded");
					}
					else {
						content.put(str, obj);
					}
				}
			}
		}

		@Override
		public void bind(final String name, Object obj) throws NamingException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can't be null or empty");
			}
			else if (obj == null) {
				throw new NullPointerException("Object to bind can't ne null");
			}
			else {
				bind(getNameParser(name).parse(name), obj);
			}
		}

		@Override
		public void rebind(final Name name, Object obj) throws NamingException {
			if (name == null) {
				throw new NullPointerException("Name can't ne null");
			}
			else if (obj == null) {
				throw new NullPointerException("Object to rebind can't ne null");
			}
			else if (parent != null) {
				parent.rebind(composeName(name, prefix), obj);
			}
			else {
				final String	str = name.toString();
				
				try(final Locker	l = locker.lock(false)) {
					content.put(str, obj);
				}
			}
		}

		@Override
		public void rebind(final String name, Object obj) throws NamingException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can't be null or empty");
			}
			else if (obj == null) {
				throw new NullPointerException("Object to rebind can't ne null");
			}
			else {
				rebind(getNameParser(name).parse(name), obj);
			}
		}

		@Override
		public void unbind(final Name name) throws NamingException {
			if (name == null) {
				throw new NullPointerException("Name can't ne null");
			}
			else if (parent != null) {
				parent.unbind(composeName(name, prefix));
			}
			else {
				final String	str = name.toString();
				
				try(final Locker	l = locker.lock(false)) {
					if (!content.containsKey(str)) {
						throw new NamingException("Name ["+str+"] is not binded yet");
					}
					else {
						content.remove(str);
					}
				}
			}
		}

		@Override
		public void unbind(final String name) throws NamingException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can't be null or empty");
			}
			else {
				unbind(getNameParser(name).parse(name));
			}
		}

		@Override
		public void rename(final Name oldName, final Name newName) throws NamingException {
			if (oldName == null) {
				throw new NullPointerException("Old name can't ne null");
			}
			else if (newName == null) {
				throw new NullPointerException("New name can't ne null");
			}
			else if (parent != null) {
				parent.rename(composeName(oldName, prefix), composeName(newName, prefix));
			}
			else {
				final String	strOld = oldName.toString();
				final String	strNew = newName.toString();
				
				try(final Locker	l = locker.lock(false)) {
					if (!content.containsKey(strOld)) {
						throw new NamingException("Old name ["+strOld+"] is not binded yet");
					}
					else if (content.containsKey(strNew)) {
						throw new NamingException("New name ["+strNew+"] already exists");
					}
					else {
						content.put(strNew,content.remove(strOld));
					}
				}
			}
		}

		@Override
		public void rename(final String oldName, final String newName) throws NamingException {
			if (Utils.checkEmptyOrNullString(oldName)) {
				throw new IllegalArgumentException("Old name can't be null or empty"); 
			}
			else if (Utils.checkEmptyOrNullString(newName)) {
				throw new IllegalArgumentException("New name can't be null or empty"); 
			}
			else {
				rename(getNameParser(oldName).parse(oldName), getNameParser(newName).parse(newName));
			}
		}

		@Override
		public NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {
			if (name == null) {
				throw new NullPointerException("Name can't ne null");
			}
			else if (parent != null) {
				return parent.list(composeName(name, prefix));
			}
			else {
				final ListEnumeration<NameClassPair>	result = new ListEnumeration<>();
				final String	str = name.toString();
				
				try(final Locker	l = locker.lock(true)) {
					for(Entry<String, Object> item : content.entrySet()) {
						if (item.getKey().startsWith(str)) {
							result.add(new NameClassPair(item.getKey(), item.getValue().getClass().getCanonicalName()));
						}
					}
				}
				return result;
			}
		}

		@Override
		public NamingEnumeration<NameClassPair> list(final String name) throws NamingException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can't be null or empty");
			}
			else {
				return list(getNameParser(name).parse(name));
			}
		}

		@Override
		public NamingEnumeration<Binding> listBindings(final Name name) throws NamingException {
			if (name == null) {
				throw new NullPointerException("Name can't ne null");
			}
			else if (parent != null) {
				return parent.listBindings(composeName(name, prefix));
			}
			else {
				final ListEnumeration<Binding>	result = new ListEnumeration<>();
				final String	str = name.toString();
				
				try(final Locker	l = locker.lock(true)) {
					for(Entry<String, Object> item : content.entrySet()) {
						if (item.getKey().startsWith(str)) {
							result.add(new Binding(item.getKey(), item.getValue()));
						}
					}
				}
				return result;
			}
		}

		@Override
		public NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can't be null or empty");
			}
			else {
				return listBindings(getNameParser(name).parse(name));
			}
		}

		@Override
		public void destroySubcontext(final Name name) throws NamingException {
			if (name == null) {
				throw new NullPointerException("Name can't ne null");
			}
			else {
				throw new UnsupportedOperationException("This content doesn't support subcontext destroying"); 
			}
		}

		@Override
		public void destroySubcontext(final String name) throws NamingException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can't be null or empty");
			}
			else {
				destroySubcontext(getNameParser(name).parse(name));
			}
		}

		@Override
		public Context createSubcontext(final Name name) throws NamingException {
			if (name == null) {
				throw new NullPointerException("Name can't ne null");
			}
			else {
				throw new UnsupportedOperationException("This content doesn't support subcontext creating"); 
			}
		}

		@Override
		public Context createSubcontext(final String name) throws NamingException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can't be null or empty");
			}
			else {
				return createSubcontext(getNameParser(name).parse(name));
			}
		}

		@Override
		public Object lookupLink(final Name name) throws NamingException {
			if (name == null) {
				throw new NullPointerException("Name can't ne null");
			}
			else if (parent != null) {
				return parent.lookupLink(composeName(name, prefix));
			}
			else {
				return lookup(name);
			}
		}

		@Override
		public Object lookupLink(final String name) throws NamingException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can't be null or empty");
			}
			else {
				return lookupLink(getNameParser(name).parse(name));
			}
		}

		@Override
		public NameParser getNameParser(final Name name) throws NamingException {
			if (name == null) {
				throw new NullPointerException("Name can't ne null");
			}
			else {
				return PARSER;
			}
		}

		@Override
		public NameParser getNameParser(final String name) throws NamingException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can't be null or empty");
			}
			else {
				return PARSER;
			}
		}

		@Override
		public Name composeName(final Name name, final Name prefix) throws NamingException {
			if (name == null) {
				throw new NullPointerException("Name can't ne null");
			}
			else if (prefix == null) {
				throw new NullPointerException("Prefix can't ne null");
			}
			else {
				return ((Name)prefix.clone()).addAll(name);
			}
		}

		@Override
		public String composeName(final String name, final String prefix) throws NamingException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Name can't be null or empty"); 
			}
			else if (Utils.checkEmptyOrNullString(prefix)) {
				throw new IllegalArgumentException("Prefix can't be null or empty"); 
			}
			else {
				return composeName(getNameParser(name).parse(name), getNameParser(prefix).parse(prefix)).toString();
			}
		}

		@Override
		public Object addToEnvironment(final String propName, final Object propVal) throws NamingException {
			if (Utils.checkEmptyOrNullString(propName)) {
				throw new IllegalArgumentException("Property name can't be null or empty");
			}
			else {
				return ((Hashtable<String,Object>)getEnvironment()).put(propName, propVal);
			}
		}

		@Override
		public Object removeFromEnvironment(final String propName) throws NamingException {
			if (Utils.checkEmptyOrNullString(propName)) {
				throw new IllegalArgumentException("Property name can't be null or empty");
			}
			else {
				return ((Hashtable<String,Object>)getEnvironment()).remove(propName);
			}
		}

		@Override
		public Hashtable<?, ?> getEnvironment() throws NamingException {
			return environment;
		}

		@Override
		public String getNameInNamespace() throws NamingException {
			throw new UnsupportedOperationException("Not implemented yet"); 
		}

		@Override
		public void close() throws NamingException {
			closed = true;
		}
	}
	
	private static class SimpleNameImpl implements Name {
		private static final long serialVersionUID = -7434385499878502072L;

		private List<String>	parts = new ArrayList<>();
		
		private SimpleNameImpl(final List<String> from) {
			this.parts = from;
		}
		
		@Override
		public int size() {
			return parts.size();
		}

		@Override
		public boolean isEmpty() {
			return parts.isEmpty();
		}

		@Override
		public Enumeration<String> getAll() {
			return new Enumeration<String>() {
				int		index = 0;
				@Override public boolean hasMoreElements() {return index < size();}
				@Override public String nextElement() {return get(index++);}
			};
		}

		@Override
		public String get(final int posn) {
			if (posn < 0 || posn >= size()) {
				throw new IllegalArgumentException("Position ["+posn+"] out of range 0.."+(size()-1)); 
			}
			else {
				return parts.get(posn);
			}
		}

		@Override
		public Name getPrefix(final int posn) {
			if (posn < 0 || posn >= size()) {
				throw new IllegalArgumentException("Position ["+posn+"] out of range 0.."+(size()-1)); 
			}
			else {
				final List<String>	temp = new ArrayList<>();
				
				for(int index = 0; index < posn; index++) {
					temp.add(get(index));
				}
				return new SimpleNameImpl(temp);
			}
		}

		@Override
		public Name getSuffix(int posn) {
			if (posn < 0 || posn >= size()) {
				throw new IllegalArgumentException("Position ["+posn+"] out of range 0.."+(size()-1)); 
			}
			else {
				final List<String>	temp = new ArrayList<>();
				
				for(int index = posn; index < size(); index++) {
					temp.add(get(index));
				}
				return new SimpleNameImpl(temp);
			}
		}

		@Override
		public boolean startsWith(final Name n) {
			if (n == null) {
				throw new NullPointerException("Name can't be null");
			}
			else if (n.size() > size()) {
				return false;
			}
			else {
				for (int index = 0, maxIndex = n.size(); index < maxIndex; index++) {
					if (!get(index).equals(n.get(index))) {
						return false;
					}
				}
				return true;
			}
		}

		@Override
		public boolean endsWith(final Name n) {
			if (n == null) {
				throw new NullPointerException("Name can't be null");
			}
			else if (n.size() > size()) {
				return false;
			}
			else {
				for (int index = 0, maxIndex = n.size(); index < maxIndex; index++) {
					if (!get(size()-index).equals(n.get(n.size()-index))) {
						return false;
					}
				}
				return true;
			}
		}

		@Override
		public Name addAll(final Name suffix) throws InvalidNameException {
			if (suffix == null) {
				throw new NullPointerException("Suffix to add can't be null");
			}
			else {
				for(String item : new SequenceIterator<String>(suffix.getAll().asIterator()).toIterable()) {
					parts.add(item);
				}
				return this;
			}
		}

		@Override
		public Name addAll(final int posn, final Name n) throws InvalidNameException {
			if (posn < 0 || posn >= size()) {
				throw new IllegalArgumentException("Position ["+posn+"] out of range 0.."+(size()-1));
			}
			else if (n == null) {
				throw new NullPointerException("Name to add can't be null");
			}
			else {
				for(String item : new SequenceIterator<String>(n.getAll().asIterator()).toIterable()) {
					parts.add(posn, item);
				}
				return this;
			}
		}

		@Override
		public Name add(final String comp) throws InvalidNameException {
			if (comp == null || comp.isEmpty()) {
				throw new NullPointerException("Suffix to add can't be null or empty");
			}
			else {
				parts.add(comp);
				return this;
			}
		}

		@Override
		public Name add(final int posn, final String comp) throws InvalidNameException {
			if (posn < 0 || posn >= size()) {
				throw new IllegalArgumentException("Position ["+posn+"] out of range 0.."+(size()-1));
			}
			else if (Utils.checkEmptyOrNullString(comp)) {
				throw new IllegalArgumentException("Suffix to add can't be null or empty");
			}
			else {
				parts.add(posn, comp);
				return this;
			}
		}

		@Override
		public Object remove(int posn) throws InvalidNameException {
			if (posn < 0 || posn >= size()) {
				throw new IllegalArgumentException("Position ["+posn+"] out of range 0.."+(size()-1));
			}
			else {
				return parts.remove(posn);
			}
		}

		@Override
		public Object clone() {
			return new SimpleNameImpl(new ArrayList<>(parts));
		}

		@Override
		public int compareTo(final Object obj) {
			if (obj instanceof Name) {
				return toString().compareTo(obj.toString());
			}
			else {
				return -1;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((parts == null) ? 0 : parts.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			SimpleNameImpl other = (SimpleNameImpl) obj;
			if (parts == null) {
				if (other.parts != null) return false;
			} else if (!parts.equals(other.parts)) return false;
			return true;
		}

		@Override
		public String toString() {
			final StringBuilder	sb = new StringBuilder();
			
			for(String item : parts) {
				sb.append('/').append(item);
			}
			return sb.substring(1);
		}
	}
	
	private static class SimpleNameParserImpl implements NameParser {
		@Override
		public Name parse(final String name) throws NamingException {
			if (name == null || name.isEmpty()) {
				throw new NamingException("Name to parse is null or empty!");
			}
			else {
				return new SimpleNameImpl(parseName(name));
			}
		}
		
		static List<String> parseName(final String name) {
			final List<String>	result = new ArrayList<>();
			
			for(String item : name.split("/")) {
				result.add(item.trim());
			}
			return result;
		}
	}
	
	private static class ListEnumeration<T> extends ArrayList<T> implements NamingEnumeration<T> {
		private static final long serialVersionUID = -49178302987933587L;
		
		int	index = 0;
		
		@Override
		public boolean hasMoreElements() {
			return index < size();
		}

		@Override
		public T nextElement() {
			return get(index++);
		}

		@Override
		public T next() throws NamingException {
			return nextElement();
		}

		@Override
		public boolean hasMore() throws NamingException {
			return hasMoreElements();
		}

		@Override
		public void close() throws NamingException {
		}
	}
}
