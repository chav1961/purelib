package chav1961.purelib.basic;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.exceptions.ContentException;

/**
 * <p>This class is used to parse and access command line arguments for console-based applications. Recommended template to use it is produce
 * child class for it:</p>
 * <code>
 * . . .<br>
 * class ChildArgParser extends ArgParser {<br>
 * public ChildArgParser(){<br>
 * super(new ZZZarg(...),...);<br>
 * }<br>
 * }<br>
 * . . .<br>
 * static ChildArgParser parser = new ChildArgParser();<br>
 * . . . <br>
 * public static void main(String[] args) {<br>
 * ChildArgParser currentList = parser.parse(args);<br>
 * . . .<br>
 * int value = currentList.getValue("intName",int.class);<br>
 * . . .<br>
 * }</br>
 * </code>
 * <p>This class can be used in multithreaded environment</p>
 * 
 * @see chav1961.purelib.basic JUnit tests
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class ArgParser {
	private final char					keyPrefix;
	private final boolean				caseSensitive;
	private final ArgDescription[]		desc;
	private final Map<String,String[]>	pairs;

	protected ArgParser(final ArgDescription... desc) {
		this('-',true,desc);
	}
	
	protected ArgParser(final char keyPrefix, final boolean caseSensitive, final ArgDescription... desc) {
		if (desc == null || desc.length == 0) {
			throw new IllegalArgumentException("Argument description list can't be null or empty array");
		}
		else {
			final Set<String>	names = new HashSet<>();
			boolean 			prevPosWasList = false;
			
			for (ArgDescription item : desc) {
				if (item == null) {
					throw new NullPointerException("Null value inside argument description list");
				}
				else {
					final String	key = caseSensitive ? item.getName() : item.getName().toLowerCase();
					
					if (names.contains(key)) {
						throw new IllegalArgumentException("Duplicate key name ["+item.getName()+"] in the parameters list");
					}
					else if (item.isPositional() && !item.hasValue()) {
						throw new IllegalArgumentException("Positional parameter ["+item.getName()+"] is marked as has no value!");
					}
					else if (item.isList() && !item.hasValue()) {
						throw new IllegalArgumentException("List value parameter ["+item.getName()+"] is marked as has no value!");
					}
					else if (item.isPositional() && prevPosWasList) {
						throw new IllegalArgumentException("List-specified positional parameter ["+item.getName()+"] has list-specified predecessor!");
					}
					else {
						prevPosWasList = item.isList();
						names.add(item.getName());
					}
				}
			}
			
			this.keyPrefix = keyPrefix;
			this.caseSensitive = caseSensitive;
			this.desc = desc;
			this.pairs = null;
		}
	}

	private ArgParser(final char keyPrefix, final boolean caseSensitive, final ArgDescription[] desc, final Map<String,String[]> pairs) {
		this.keyPrefix = keyPrefix;
		this.caseSensitive = caseSensitive;
		this.desc = desc;
		this.pairs = Collections.unmodifiableMap(pairs);
	}
	
	public ArgParser parse(final String... args) throws ConsoleCommandException {
		if (this.pairs != null) {
			throw new IllegalStateException("Attempt to call parse(...) on parsed instance. This method can be called on 'parent' instance only");
		}
		else if (args == null) {
			throw new NullPointerException("Argument list to parse can't be null");
		}
		else {
			final Map<String,String[]>	pairs = new HashMap<>();
			
			parseParameters(keyPrefix,caseSensitive,desc,args,pairs);
			return new ArgParser(keyPrefix,caseSensitive,desc,pairs);
		}
	}
	
	public <T> T getValue(final String key, final Class<T> awaited) throws ContentException {
		if (this.pairs == null) {
			throw new IllegalStateException("Attempt to call getValue(...) on 'parent' instance. Call parse(...) method and use value returned for this purpose");
		}
		else if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get value for can't be null or empty");
		}
		else if (awaited == null) {
			throw new NullPointerException("Result class awaited can't be null");
		}
		else {
			final String			keyFind = caseSensitive ? key : key.toLowerCase(); 
			final ArgDescription	found = forKey(keyFind,desc,caseSensitive); 
			
			if (pairs.containsKey(keyFind)) {
				return (T)convert(found,awaited,pairs.get(keyFind));
			}
			else {
				
				return (T)convert(found,awaited,found.getDefaultValue());
			}
		}
	}

	public boolean isTyped(final String key) {
		if (this.pairs != null) {
			throw new IllegalStateException("Attempt to call isTyped() on 'parent' instance. Call parse(...) method and use value returned for this purpose");
		}
		else if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get value for can't be null or empty");
		}
		else {
			final String	keyFind = caseSensitive ? key : key.toLowerCase();
			
			return pairs.containsKey(keyFind) || forKey(keyFind,desc,caseSensitive) != null;
		}
	}

	public String getUsage(final String applicationName) {
		if (applicationName == null || applicationName.isEmpty()) {
			throw new IllegalArgumentException("Application name can't be null or empty");
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			
			sb.append("Usage: ").append(applicationName);
			for (ArgDescription item : desc) {
				sb.append(' ');
				if (item.isPositional()) {
					sb.append('<').append(item.getName()).append('>');
				}
				else {
					sb.append(keyPrefix).append(item.getName());
					if (item.hasValue()) {
						sb.append(" <value>");
					}
				}
				if (item.isList()) {
					sb.append("...");
				}
			}
			sb.append('\n');
			for (ArgDescription item : desc) {
				sb.append('\t');
				if (item.isPositional()) {
					sb.append(item.getName()).append(": - ").append(item.getHelpDescriptor()).append('\n');
				}
				else {
					sb.append(keyPrefix).append(item.getName()).append(": - ").append(item.getHelpDescriptor()).append('\n');
				}
			}
			return sb.toString();
		}
	}
	
	static void parseParameters(final char keyPrefix, final boolean caseSensitive, final ArgDescription[] desc, final String[] args, final Map<String, String[]> pairs) throws ConsoleCommandException {
		int 	positional = 0;
		
loop:	for (int index = 0; index < args.length; index++) {
			if (args[index].charAt(0) != keyPrefix) {
				int	found = 0;
				
				for (ArgDescription item : desc) {
					if (item.isPositional()) {
						if (found == positional) {
							if (item.isList()) {
								final List<String>	collection = new ArrayList<>();
								
								index = collectList(args,index,keyPrefix,collection)-1;
								for (String value : collection) {
									item.validate(value);
								}
								pairs.put(item.getName(),collection.toArray(new String[collection.size()]));
							}
							else {
								item.validate(args[index]);
								pairs.put(item.getName(),new String[]{args[index]});
							}
							positional++;
							continue loop;
						}
						else {
							found++;
						}
					}
				}
				throw new ConsoleCommandException("Extra positional parameter ["+args[index]+"] was detected");
			}
			else {
				final String			key = args[index].substring(1);
				final String			keyFind = caseSensitive ? key : key.toLowerCase();
				final ArgDescription	found = forKey(keyFind,desc,caseSensitive);
				
				if (found == null) {
					throw new ConsoleCommandException("Key parameter [-"+key+"] is not supported for the parser");
				}
				else if (found.hasValue()) {
					if (index == args.length-1) {
						throw new ConsoleCommandException("Key parameter [-"+key+"] has no value awaited");
					}
					else if (found.isList()) {
						final List<String>	collection = new ArrayList<>();
						
						index = collectList(args,index+1,keyPrefix,collection)-1;
						for (String value : collection) {
							found.validate(value);
						}
						pairs.put(found.getName(),collection.toArray(new String[collection.size()]));
					}
					else {
						found.validate(args[index+1]);
						pairs.put(found.getName(),new String[]{args[index+1]});
						index++;
					}
				}
				else {
					found.validate("true");
					pairs.put(found.getName(),new String[]{"true"});
				}
			}
		}
		
		final StringBuilder	sb = new StringBuilder();
		
		for (ArgDescription item : desc) {
			if (item.isMandatory()) {
				if (!pairs.containsKey(item.getName())) {
					sb.append(',').append(item.getName());
				}
			}
		}
		if (sb.length() > 0) {
			throw new ConsoleCommandException("Mandatory argument(s) ["+sb.toString().substring(1)+"] are missing in the parameters");
		}
	}

	static int collectList(final String[] args, int from, final char keyPrefix, final List<String> collection) {
		for (;from < args.length; from++) {
			if (args[from].length() == 0 || args[from].charAt(0) != keyPrefix) {
				collection.add(args[from]);
			}
			else {
				break;
			}
		}
		return from;
	}

	@SuppressWarnings("unchecked")
	static <T> T convert(final ArgDescription desc, final Class<T> awaited, final String[] value) throws ContentException {
		if (value != null) {
			if (awaited.isArray()) {
				final Object[]	result = (Object[]) Array.newInstance(awaited.getComponentType(),value.length);
				
				for (int index = 0; index < value.length; index++) {
					Array.set(result,index,desc.getValue(value[index],awaited.getComponentType()));
				}
				return(T)result;
			}
			else if (value.length > 0) {
				return (T)desc.getValue(value[0],awaited);
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	static ArgDescription forKey(final String key, final ArgDescription[] desc, final boolean caseSensitive) {
		for (ArgDescription item : desc) {
			if (!caseSensitive && item.getName().equals(key) || caseSensitive && item.getName().equalsIgnoreCase(key)) {
				return item;
			}
		}
		return null;
	}

	protected interface ArgDescription {
		String getName();
		String getHelpDescriptor();
		<T> T getValue(String value, Class<T> awaited) throws ContentException;
		String[] getDefaultValue();
		boolean isMandatory();
		boolean isPositional();
		boolean isList();
		boolean hasValue();
		void validate(String value) throws ConsoleCommandException;
	}

	protected abstract static class AbstractArg implements ArgDescription {
		private final String	name;
		private final String	helpDescriptor;
		private final boolean	isMandatory;
		private final boolean	isPositional;

		public AbstractArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Argument name can't be null or empty");
			}
			else if (helpDescriptor == null || helpDescriptor.isEmpty()) {
				throw new IllegalArgumentException("Argument help descriptor can't be null or empty");
			}
			else {
				this.name = name;
				this.helpDescriptor = helpDescriptor;
				this.isMandatory = isMandatory;
				this.isPositional = isPositional;
			}
		}

		@Override public abstract <T> T getValue(final String value, final Class<T> awaited) throws ContentException;
		@Override public abstract String[] getDefaultValue();
		@Override public abstract boolean isList();
		@Override public abstract void validate(final String value) throws ConsoleCommandException;
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getHelpDescriptor() {
			return helpDescriptor;
		}

		@Override
		public boolean isMandatory() {
			return isMandatory;
		}

		@Override
		public boolean isPositional() {
			return isPositional;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		@Override
		public String toString() {
			return "AbstractArg [name=" + name + ", helpDescriptor=" + helpDescriptor + ", isMandatory=" + isMandatory + ", isPositional=" + isPositional + "]";
		}
	}

	protected static class BooleanArg extends AbstractArg {
		private final String[]	defaults;	

		public BooleanArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.defaults = new String[]{"false"};
		}

		public BooleanArg(final String name, final boolean isPositional, final String helpDescriptor, final boolean defaultValue) {
			super(name, false, isPositional, helpDescriptor);
			this.defaults = new String[]{String.valueOf(defaultValue)};
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws ContentException {
			if (boolean.class.isAssignableFrom(awaited) || Boolean.class.isAssignableFrom(awaited)) {
				return (T)Boolean.valueOf("true".equalsIgnoreCase(value));
			}
			else {
				throw new ContentException("Argument ["+getName()+"] can be converted to booleans only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
			}
		}

		@Override
		public String[] getDefaultValue() {
			return defaults;
		}

		@Override
		public boolean isList() {
			return false;
		}

		@Override
		public void validate(final String value) throws ConsoleCommandException {
			if (value == null || value.isEmpty()) {
				throw new ConsoleCommandException("Argument ["+getName()+"]: value can't be null or empty");
			}
			else {
				if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
					throw new ConsoleCommandException("Argument ["+getName()+"]: value doesn't have valid boolean: "+value);
				}
			}
		}
		
		@Override
		public boolean hasValue() {
			return isPositional();
		}

		@Override
		public String toString() {
			return "BooleanArg [defaults=" + Arrays.toString(defaults) + ", toString()=" + super.toString() + "]";
		}
	}

	protected static class IntegerArg extends AbstractArg {
		private final String[]	defaults;	

		public IntegerArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.defaults = new String[]{"0"};
		}

		public IntegerArg(final String name, final boolean isPositional, final String helpDescriptor, final long defaultValue) {
			super(name, false, isPositional, helpDescriptor);
			this.defaults = new String[]{String.valueOf(defaultValue)};
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws ContentException {
			if (long.class.isAssignableFrom(awaited) || Long.class.isAssignableFrom(awaited)) {
				return (T)Long.valueOf(value);
			}
			else if (int.class.isAssignableFrom(awaited) || Integer.class.isAssignableFrom(awaited)) {
				return (T)Integer.valueOf(value);
			}
			else if (short.class.isAssignableFrom(awaited) || Short.class.isAssignableFrom(awaited)) {
				return (T)Short.valueOf(value);
			}
			else if (byte.class.isAssignableFrom(awaited) || Byte.class.isAssignableFrom(awaited)) {
				return (T)Byte.valueOf(value);
			}
			else {
				throw new ContentException("Argument ["+getName()+"] can be converted to integer type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
			}
		}

		@Override
		public String[] getDefaultValue() {
			return defaults;
		}

		@Override
		public boolean isList() {
			return false;
		}

		@Override
		public void validate(final String value) throws ConsoleCommandException {
			if (value == null || value.isEmpty()) {
				throw new ConsoleCommandException("Argument ["+getName()+"]: value can't be null or empty");
			}
			else {
				try{Long.valueOf(value).longValue();
				} catch (NumberFormatException exc) {
					throw new ConsoleCommandException("Argument ["+getName()+"]: value doesn't have valid integer: "+value);
				}
			}
		}

		@Override
		public String toString() {
			return "IntegerArg [defaults=" + Arrays.toString(defaults) + ", toString()=" + super.toString() + "]";
		}
	}
	
	protected static class RealArg extends AbstractArg {
		private final String[]	defaults;	

		public RealArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.defaults = new String[]{"0.0"};
		}

		public RealArg(final String name, final boolean isPositional, final String helpDescriptor, final double defaultValue) {
			super(name, false, isPositional, helpDescriptor);
			this.defaults = new String[]{String.valueOf(defaultValue)};
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws ContentException {
			if (double.class.isAssignableFrom(awaited) || Double.class.isAssignableFrom(awaited)) {
				return (T)Double.valueOf(value);
			}
			else if (float.class.isAssignableFrom(awaited) || Float.class.isAssignableFrom(awaited)) {
				return (T)Float.valueOf(value);
			}
			else {
				throw new ContentException("Argument ["+getName()+"] can be converted to real type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
			}
		}

		@Override
		public String[] getDefaultValue() {
			return defaults;
		}

		@Override
		public boolean isList() {
			return false;
		}

		@Override
		public void validate(final String value) throws ConsoleCommandException {
			if (value == null || value.isEmpty()) {
				throw new ConsoleCommandException("Argument ["+getName()+"]: value can't be null or empty");
			}
			else {
				try{Double.valueOf(value).doubleValue();
				} catch (NumberFormatException exc) {
					throw new ConsoleCommandException("Argument ["+getName()+"]: value doesn't have valid real: "+value);
				}
			}
		}

		@Override
		public String toString() {
			return "RealArg [defaults=" + Arrays.toString(defaults) + ", toString()=" + super.toString() + "]";
		}
	}
	
	protected static class StringArg extends AbstractArg {
		private final String[]	defaults;	

		public StringArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.defaults = new String[]{""};
		}

		public StringArg(final String name, final boolean isPositional, final String helpDescriptor, final String defaultValue) {
			super(name, false, isPositional, helpDescriptor);
			this.defaults = new String[]{defaultValue};
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws ContentException {
			if (String.class.isAssignableFrom(awaited)) {
				return (T)value;
			}
			else {
				throw new ContentException("Argument ["+getName()+"] can be converted to string type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
			}
		}

		@Override
		public String[] getDefaultValue() {
			return defaults;
		}

		@Override
		public boolean isList() {
			return false;
		}

		@Override
		public void validate(final String value) throws IllegalArgumentException {
			if (value == null) {
				throw new IllegalArgumentException("Argument ["+getName()+"]: value can't be null");
			}
		}

		@Override
		public String toString() {
			return "StringArg [defaults=" + Arrays.toString(defaults) + ", toString()=" + super.toString() + "]";
		}
	}

	protected static class URIArg extends AbstractArg {
		private final String[]	defaults;	

		public URIArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.defaults = new String[]{""};
		}

		public URIArg(final String name, final boolean isPositional, final String helpDescriptor, final String defaultValue) {
			super(name, false, isPositional, helpDescriptor);
			this.defaults = new String[]{defaultValue};
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws ContentException {
			if (URI.class.isAssignableFrom(awaited)) {
				try{return (T)URI.create(value);
				} catch (IllegalArgumentException exc) {
					throw new ContentException("Argument ["+getName()+"] has invalid URI value ("+exc.getLocalizedMessage()+")"); 
				}
			}
			else if (String.class.isAssignableFrom(awaited)) {
				return (T)value;
			}
			else {
				throw new ContentException("Argument ["+getName()+"] can be converted to string or URI type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
			}
		}

		@Override
		public String[] getDefaultValue() {
			return defaults;
		}

		@Override
		public boolean isList() {
			return false;
		}

		@Override
		public void validate(final String value) throws ConsoleCommandException {
			if (value == null || value.isEmpty()) {
				throw new ConsoleCommandException("Argument ["+getName()+"]: value can't be null or empty");
			}
			else {
				try{URI.create(value);
				} catch (IllegalArgumentException exc) {
					throw new ConsoleCommandException("Argument ["+getName()+"]: value doesn't have valid URI: "+value+" ("+exc.getLocalizedMessage()+")");
				}
			}
		}

		@Override
		public String toString() {
			return "StringArg [defaults=" + Arrays.toString(defaults) + ", toString()=" + super.toString() + "]";
		}
	}
	
	protected static class EnumArg<Type extends Enum<?>> extends AbstractArg {
		private final String[]		defaults;
		private final Class<Type>	enumType;

		public EnumArg(final String name, final Class<Type> enumType, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.enumType = enumType;
			this.defaults = new String[]{enumType.getEnumConstants()[0].name()};
		}

		public EnumArg(final String name, final Class<Type> enumType, final boolean isPositional, final String helpDescriptor, final Type defaultValue) {
			super(name, false, isPositional, helpDescriptor);
			this.enumType = enumType;
			this.defaults = new String[]{defaultValue.name()};
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws ContentException {
			if (enumType.isAssignableFrom(awaited)) {
				try{return (T)enumType.getMethod("valueOf",String.class).invoke(null,value);
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new IllegalArgumentException("Value ["+value+"] is missing in ["+enumType.getCanonicalName()+"] enumeration"); 
				}
			}
			else {
				throw new ContentException("Argument ["+getName()+"] can be converted to enumeration type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
			}
		}

		@Override
		public String[] getDefaultValue() {
			return defaults;
		}

		@Override
		public boolean isList() {
			return false;
		}

		@Override
		public void validate(final String value) throws ConsoleCommandException {
			if (value == null || value.isEmpty()) {
				throw new ConsoleCommandException("Argument ["+getName()+"]: value can't be null or empty");
			}
			else {
				try{enumType.getMethod("valueOf",String.class).invoke(null,value);
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new ConsoleCommandException("Value ["+value+"] is missing in ["+enumType.getCanonicalName()+"] enumeration"); 
				}
			}
		}

		@Override
		public String toString() {
			return "EnumArg [defaults=" + Arrays.toString(defaults) + ", enumType=" + enumType + ", toString()=" + super.toString() + "]";
		}
	}
}
