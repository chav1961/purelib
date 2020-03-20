package chav1961.purelib.basic;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.sql.SQLUtils;

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
 * }<br>
 * </code>
 * <p>The class supports a set of argument types (marked as ZZZarg in the example above):</p>
 * <ul>
 * <li>{linkplain BooleanArg} - argument-flag</li>
 * <li>{linkplain IntegerArg} - long integer argument</li>
 * <li>{linkplain RealArg} - double or BigDecimal argument</li>
 * <li>{linkplain StringArg} - string argument</li>
 * <li>{linkplain EnumArg} - any enumeration argument</li>
 * <li>{linkplain URIArg} - {@linkplain URI} argument</li>
 * <li>{linkplain StringListArg} - list of string argument(s)</li>
 * <li>{linkplain ConfigArg} - configuration source argument</li>
 * </ul>
 * <p>Any of these arguments can be declared as positional or key-value argument. Positional arguments must be declared before key-value arguments.
 * Positional argument has name to access to it, but doesn't require the name to be typed in the command string. Key-value argument also has a name and the name
 * must be typed in the command string. Order to type key-value arguments in the command string is not important.</p>
 * <p>{@linkplain ConfigArg} argument type is used to store part or all the command line arguments in external data source (file, URI connection etc). When you get value
 * for any argument and it doesn't explicitly typed in the command string, it's value will be extracted from the external data source. External data source must have format
 * compatible with {@linkplain SubstitutableProperties} requirements.</p>
 * <p>This class can be used in multi-threaded environment</p>
 * 
 * @see chav1961.purelib.basic JUnit tests
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.4
 */
public class ArgParser {
	private final char					keyPrefix;
	private final boolean				caseSensitive;
	private final ArgDescription[]		desc;
	private final Map<String,String[]>	pairs;
	private final boolean				hasConfigArg;

	protected ArgParser(final ArgDescription... desc) {
		this('-',true,desc);
	}
	
	protected ArgParser(final char keyPrefix, final boolean caseSensitive, final ArgDescription... desc) {
		if (desc == null || desc.length == 0) {
			throw new IllegalArgumentException("Argument description list can't be null or empty array");
		}
		else if (Utils.checkArrayContent4Nulls(desc) >= 0) {
			throw new IllegalArgumentException("Nulls inside desc argument");
		}
		else {
			final Set<String>	names = new HashSet<>();
			boolean 			prevPosWasList = false, hasConfig = false; 
			
			for (ArgDescription item : desc) {
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
					if (item instanceof ConfigArg) {
						hasConfig = true;
					}
				}
			}
			
			this.keyPrefix = keyPrefix;
			this.caseSensitive = caseSensitive;
			this.desc = desc;
			this.pairs = null;
			this.hasConfigArg = hasConfig;
		}
	}

	private ArgParser(final char keyPrefix, final boolean caseSensitive, final ArgDescription[] desc, final boolean hasConfigArg, final Map<String,String[]> pairs) {
		this.keyPrefix = keyPrefix;
		this.caseSensitive = caseSensitive;
		this.desc = desc;
		this.hasConfigArg = hasConfigArg;
		this.pairs = Collections.unmodifiableMap(pairs);
	}

	public ArgParser parse(final String... args) throws CommandLineParametersException {
		return parse(false,false,args);
	}
	
	public ArgParser parse(final boolean ignoreExtra, final boolean ignoreUnknown, final String... args) throws CommandLineParametersException {
		if (this.pairs != null) {
			throw new IllegalStateException("Attempt to call parse(...) on parsed instance. This method can be called on 'parent' instance only");
		}
		else if (args == null) {
			throw new NullPointerException("Argument list to parse can't be null");
		}
		else {
			final Map<String,String[]>	pairs = new HashMap<>();
			
			parseParameters(ignoreExtra,ignoreUnknown,keyPrefix,caseSensitive,desc,hasConfigArg,args,pairs);
			return new ArgParser(keyPrefix,caseSensitive,desc,hasConfigArg,pairs);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getValue(final String key, final Class<T> awaited) throws CommandLineParametersException {
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
			
			if (found == null) {
				if (awaited == String.class) {
					return pairs.get(keyFind) == null ? null : (T)pairs.get(keyFind)[0].toString();
				}
				else {
					throw new IllegalArgumentException("Unknown of undeclared key ["+key+"]: only String.class can be used for awaited class");
				}
			}
			else if (pairs.containsKey(keyFind)) {
				return (T)convert(found,awaited,pairs.get(keyFind));
			}
			else {
				
				return (T)convert(found,awaited,found.getDefaultValue());
			}
		}
	}

	public boolean isTyped(final String key) {
		if (this.pairs == null) {
			throw new IllegalStateException("Attempt to call isTyped() on 'parent' instance. Call parse(...) method and use value returned for this purpose");
		}
		else if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get value for can't be null or empty");
		}
		else {
			final String	keyFind = caseSensitive ? key : key.toLowerCase();
			
			return pairs.containsKey(keyFind);// || forKey(keyFind,desc,caseSensitive) != null;
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
	
	static void parseParameters(final boolean ignoreExtra, final boolean ignoreUnknown, final char keyPrefix, final boolean caseSensitive, final ArgDescription[] desc, final boolean hasConfigArg, final String[] args, final Map<String, String[]> pairs) throws CommandLineParametersException {
		final Set<String>	names =  new HashSet<>();
		ArgDescription		confArg = null;
		int 				positional = 0;
		
loop:	for (int index = 0; index < args.length; index++) {
			if (args[index].isEmpty() || args[index].charAt(0) != keyPrefix) {
				int	found = 0;
				
				for (ArgDescription item : desc) {
					if (hasConfigArg) {
						names.add(item.getName());
						if (item instanceof ConfigArg) {
							confArg = item;
						}
					}
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
				if (!ignoreExtra) {
					throw new CommandLineParametersException("Extra positional parameter ["+args[index]+"] was detected");
				}
			}
			else {
				final String			key = args[index].substring(1);
				final String			keyFind = caseSensitive ? key : key.toLowerCase();
				final ArgDescription	found = forKey(keyFind,desc,caseSensitive);
				
				if (found == null) {
					if (!ignoreUnknown) {
						throw new CommandLineParametersException("Key parameter [-"+key+"] is not supported for the parser");
					}
				}
				else if (found.hasValue()) {
					if (index == args.length-1) {
						throw new CommandLineParametersException("Key parameter [-"+key+"] has no value awaited");
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
			throw new CommandLineParametersException("Mandatory argument(s) ["+sb.toString().substring(1)+"] are missing in the parameters");
		}
		
		if (hasConfigArg && confArg != null && (pairs.containsKey(confArg.getName()) || confArg.getDefaultValue() != null)) {
			final SubstitutableProperties	sp = new SubstitutableProperties();
			final String					confSource = (pairs.containsKey(confArg.getName()) ? pairs.get(confArg.getName()) : confArg.getDefaultValue())[0];
			final URI						sourceURI = confArg.getValue(confSource,URI.class);
			
			try(final InputStream	is = (sourceURI.isAbsolute() ? sourceURI : URI.create("file:"+sourceURI.toString())).toURL().openStream()) {
				sp.load(is);
			} catch (IOException e) {
				throw new CommandLineParametersException("I/O error reading configuration source ["+confSource+"] : "+e.getLocalizedMessage());
			}
			if (!ignoreUnknown) {
				for (Entry<Object,Object> item : sp.entrySet()) {
					if (!names.contains(item.getKey().toString())) {
						sb.append(',').append(item.getKey().toString());
					}
				}
				if (sb.length() > 0) {
					throw new CommandLineParametersException("Configuration source ["+confSource+"] contains unknown parameters ["+sb.substring(1)+"]");
				}
			}
			for (Entry<Object,Object> item : sp.entrySet()) {
				pairs.putIfAbsent(item.getKey().toString(),new String[]{sp.getProperty(item.getKey().toString())});
			}
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
	static <T> T convert(final ArgDescription desc, final Class<T> awaited, final String[] value) throws CommandLineParametersException {
		if (value != null) {
			if (awaited.isArray()) {
				final Object[]	result = (Object[]) Array.newInstance(awaited.getComponentType(),value.length);
				
				for (int index = 0; index < value.length; index++) {
					Array.set(result,index,desc.getValue(value[index],awaited.getComponentType()));
				}
				return(T)result;
			}
			else if (value.length > 0 && value[0] != null) {
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
		<T> T getValue(String value, Class<T> awaited) throws CommandLineParametersException;
		String[] getDefaultValue();
		boolean isMandatory();
		boolean isPositional();
		boolean isList();
		boolean hasValue();
		void validate(String value) throws CommandLineParametersException;
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

		@Override public abstract <T> T getValue(final String value, final Class<T> awaited) throws CommandLineParametersException;
		@Override public abstract String[] getDefaultValue();
		@Override public abstract boolean isList();
		@Override public abstract void validate(final String value) throws CommandLineParametersException;
		
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
		private static final Set<Class<?>>	SUPPORTED_CONVERSIONS = new HashSet<>();
		
		static {
			SUPPORTED_CONVERSIONS.add(boolean.class);
			SUPPORTED_CONVERSIONS.add(Boolean.class);
			SUPPORTED_CONVERSIONS.add(String.class);
		}
		
		private final String[]	defaults;	

		public BooleanArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.defaults = new String[]{"false"};
		}

		public BooleanArg(final String name, final boolean isPositional, final String helpDescriptor, final boolean defaultValue) {
			super(name, false, isPositional, helpDescriptor);
			this.defaults = new String[]{String.valueOf(defaultValue)};
		}
		
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws CommandLineParametersException {
			try{if (SUPPORTED_CONVERSIONS.contains(awaited)) {
					return (T)SQLUtils.convert(awaited, value);
				}
				else {
					throw new CommandLineParametersException("Argument ["+getName()+"] can be converted to boolean or string type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
				}
			} catch (ContentException e) {
				throw new CommandLineParametersException("Error converting argument ["+getName()+"] value ["+value+"] to ["+awaited.getCanonicalName()+"] type"); 
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
		public void validate(final String value) throws CommandLineParametersException {
			if (value == null || value.isEmpty()) {
				throw new CommandLineParametersException("Argument ["+getName()+"]: value can't be null or empty");
			}
			else {
				if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
					throw new CommandLineParametersException("Argument ["+getName()+"]: value doesn't have valid boolean: "+value);
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
		private static final Set<Class<?>>	SUPPORTED_CONVERSIONS = new HashSet<>();
		
		static {
			SUPPORTED_CONVERSIONS.add(long.class);
			SUPPORTED_CONVERSIONS.add(Long.class);
			SUPPORTED_CONVERSIONS.add(int.class);
			SUPPORTED_CONVERSIONS.add(Integer.class);
			SUPPORTED_CONVERSIONS.add(short.class);
			SUPPORTED_CONVERSIONS.add(Short.class);
			SUPPORTED_CONVERSIONS.add(byte.class);
			SUPPORTED_CONVERSIONS.add(Byte.class);
			SUPPORTED_CONVERSIONS.add(String.class);
		}
		
		private final String[]	defaults;
		private final long[][]	ranges;

		public IntegerArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			this(name,isMandatory,isPositional,helpDescriptor,new long[][]{new long[]{Long.MIN_VALUE,Long.MAX_VALUE}});
		}

		public IntegerArg(final String name, final boolean isPositional, final String helpDescriptor, final long defaultValue) {
			this(name,isPositional,helpDescriptor,defaultValue,new long[][]{new long[]{Long.MIN_VALUE,Long.MAX_VALUE}});
		}

		public IntegerArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor, final long[][] availableRanges) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.defaults = new String[]{"0"};
			this.ranges = availableRanges; 
		}
		
		public IntegerArg(final String name, final boolean isPositional, final String helpDescriptor, final long defaultValue, final long[][] availableRanges) {
			super(name, false, isPositional, helpDescriptor);
			this.defaults = new String[]{String.valueOf(defaultValue)};
			this.ranges = availableRanges; 
		}
		
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws CommandLineParametersException {
			try{if (SUPPORTED_CONVERSIONS.contains(awaited)) {
					return (T)SQLUtils.convert(awaited, value);
				}
				else {
					throw new CommandLineParametersException("Argument ["+getName()+"] can be converted to integer or string type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
				}
			} catch (ContentException e) {
				throw new CommandLineParametersException("Error converting argument ["+getName()+"] value ["+value+"] to ["+awaited.getCanonicalName()+"] type"); 
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
		public void validate(final String value) throws CommandLineParametersException {
			if (value == null || value.isEmpty()) {
				throw new CommandLineParametersException("Argument ["+getName()+"]: value can't be null or empty");
			}
			else {
				try{final long	longValue = Long.valueOf(value).longValue();
				
					for (long[] range : ranges) {
						if (range[0] <= longValue && longValue <= range[1]) {
							return;
						}
					}
					final StringBuilder	sb = new StringBuilder();
					
					for (long[] range : ranges) {
						if (range[0] == range[1]) {
							sb.append(',').append(range[0]);
						}
						else {
							sb.append(',').append(range[0]).append("..").append(range[1]);
						}
					}
					throw new CommandLineParametersException("Argument ["+getName()+"]: value ["+longValue+"] out of range. Available values are "+sb.delete(0, 0).toString());
				} catch (NumberFormatException exc) {
					throw new CommandLineParametersException("Argument ["+getName()+"]: value doesn't have valid integer: "+value);
				}
			}
		}

		@Override
		public String toString() {
			return "IntegerArg [defaults=" + Arrays.toString(defaults) + ", toString()=" + super.toString() + "]";
		}
	}
	
	protected static class RealArg extends AbstractArg {
		private static final Set<Class<?>>	SUPPORTED_CONVERSIONS = new HashSet<>();
		
		static {
			SUPPORTED_CONVERSIONS.add(float.class);
			SUPPORTED_CONVERSIONS.add(Float.class);
			SUPPORTED_CONVERSIONS.add(double.class);
			SUPPORTED_CONVERSIONS.add(Double.class);
			SUPPORTED_CONVERSIONS.add(String.class);
		}
		
		private final String[]	defaults;	

		public RealArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.defaults = new String[]{"0.0"};
		}

		public RealArg(final String name, final boolean isPositional, final String helpDescriptor, final double defaultValue) {
			super(name, false, isPositional, helpDescriptor);
			this.defaults = new String[]{String.valueOf(defaultValue)};
		}
		
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws CommandLineParametersException {
			try{if (SUPPORTED_CONVERSIONS.contains(awaited)) {
					return (T)SQLUtils.convert(awaited, value);
				}
				else {
					throw new CommandLineParametersException("Argument ["+getName()+"] can be converted to floating or string type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
				}
			} catch (ContentException e) {
				throw new CommandLineParametersException("Error converting argument ["+getName()+"] value ["+value+"] to ["+awaited.getCanonicalName()+"] type"); 
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
		public void validate(final String value) throws CommandLineParametersException {
			if (value == null || value.isEmpty()) {
				throw new CommandLineParametersException("Argument ["+getName()+"]: value can't be null or empty");
			}
			else {
				try{Double.valueOf(value).doubleValue();
				} catch (NumberFormatException exc) {
					throw new CommandLineParametersException("Argument ["+getName()+"]: value doesn't have valid real: "+value);
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
		public <T> T getValue(final String value, final Class<T> awaited) throws CommandLineParametersException {
			if (String.class.isAssignableFrom(awaited)) {
				return (T)value;
			}
			else {
				throw new CommandLineParametersException("Argument ["+getName()+"] can be converted to string type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
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
		public void validate(final String value) throws CommandLineParametersException {
			if (value == null) {
				throw new CommandLineParametersException("Argument ["+getName()+"]: value can't be null");
			}
		}

		@Override
		public String toString() {
			return "StringArg [defaults=" + Arrays.toString(defaults) + ", toString()=" + super.toString() + "]";
		}
	}

	protected static class URIArg extends AbstractArg {
		private static final Set<Class<?>>	SUPPORTED_CONVERSIONS = new HashSet<>();
		
		static {
			SUPPORTED_CONVERSIONS.add(URI.class);
			SUPPORTED_CONVERSIONS.add(String.class);
		}
		
		private final String[]	defaults;	

		public URIArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.defaults = new String[]{""};
		}

		public URIArg(final String name, final boolean isPositional, final String helpDescriptor, final String defaultValue) {
			super(name, false, isPositional, helpDescriptor);
			this.defaults = new String[]{defaultValue};
		}
		
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws CommandLineParametersException {
			try{if (SUPPORTED_CONVERSIONS.contains(awaited)) {
					return (T)SQLUtils.convert(awaited, value);
				}
				else {
					throw new CommandLineParametersException("Argument ["+getName()+"] can be converted to URI or string type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
				}
			} catch (ContentException e) {
				throw new CommandLineParametersException("Error converting argument ["+getName()+"] value ["+value+"] to ["+awaited.getCanonicalName()+"] type"); 
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
		public void validate(final String value) throws CommandLineParametersException {
			if (value == null || value.isEmpty()) {
				throw new CommandLineParametersException("Argument ["+getName()+"]: value can't be null or empty");
			}
			else {
				try{URI.create(value);
				} catch (IllegalArgumentException exc) {
					throw new CommandLineParametersException("Argument ["+getName()+"]: value doesn't have valid URI: "+value+" ("+exc.getLocalizedMessage()+")");
				}
			}
		}

		@Override
		public String toString() {
			return "StringArg [defaults=" + Arrays.toString(defaults) + ", toString()=" + super.toString() + "]";
		}
	}
	
	protected static class EnumArg<Type extends Enum<Type>> extends AbstractArg {
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
		public <T> T getValue(final String value, final Class<T> awaited) throws CommandLineParametersException {
			if (enumType.isAssignableFrom(awaited)) {
				return (T)Enum.valueOf((Class<Type>)awaited,value);
			}
			else {
				throw new CommandLineParametersException("Argument ["+getName()+"] can be converted to enumeration ["+enumType.getSimpleName()+"] only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
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
		public void validate(final String value) throws CommandLineParametersException {
			if (value == null || value.isEmpty()) {
				throw new CommandLineParametersException("Argument ["+getName()+"]: value can't be null or empty");
			}
			else {
				try{Enum.valueOf(enumType,value);
				} catch (IllegalArgumentException e) {
					throw new CommandLineParametersException("Value ["+value+"] is invalid or missing in ["+enumType.getCanonicalName()+"] enumeration"); 
				}
			}
		}

		@Override
		public String toString() {
			return "EnumArg [defaults=" + Arrays.toString(defaults) + ", enumType=" + enumType + ", toString()=" + super.toString() + "]";
		}
	}

	protected static class StringListArg extends AbstractArg {
		private final String[]	defaults;	

		public StringListArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.defaults = new String[0];
		}

		public StringListArg(final String name, final boolean isPositional, final String helpDescriptor, final String... defaultValue) {
			super(name, false, isPositional, helpDescriptor);
			this.defaults = defaultValue;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws CommandLineParametersException {
			if (String.class.isAssignableFrom(awaited)) {
				return (T)value;
			}
			else {
				throw new CommandLineParametersException("Argument ["+getName()+"] can be converted to string type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
			}
		}

		@Override
		public String[] getDefaultValue() {
			return defaults;
		}

		@Override
		public boolean isList() {
			return true;
		}

		@Override
		public void validate(final String value) throws IllegalArgumentException {
			if (value == null) {
				throw new IllegalArgumentException("Argument ["+getName()+"]: value can't be null");
			}
		}

		@Override
		public String toString() {
			return "StringListArg [defaults=" + Arrays.toString(defaults) + ", toString()=" + super.toString() + "]";
		}
	}

	protected static class ConfigArg extends URIArg {
		private static final Set<Class<?>>	SUPPORTED_CONVERSIONS = new HashSet<>();
		
		static {
			SUPPORTED_CONVERSIONS.add(URI.class);
			SUPPORTED_CONVERSIONS.add(String.class);
		}
		
		private final String[]	defaults;	

		public ConfigArg(final String name, final boolean isMandatory, final boolean isPositional, final String helpDescriptor) {
			super(name, isMandatory, isPositional, helpDescriptor);
			this.defaults = new String[0];
		}

		public ConfigArg(final String name, final boolean isPositional, final String helpDescriptor, final String defaultValue) {
			super(name, false, isPositional, helpDescriptor);
			this.defaults = new String[]{defaultValue};
		}
		
		@Override
		public <T> T getValue(final String value, final Class<T> awaited) throws CommandLineParametersException {
			try{if (SUPPORTED_CONVERSIONS.contains(awaited)) {
					return (T)SQLUtils.convert(awaited, value);
				}
				else {
					throw new CommandLineParametersException("Argument ["+getName()+"] can be converted to URI or string type only, conversion to ["+awaited.getCanonicalName()+"] is not supported"); 
				}
			} catch (ContentException e) {
				throw new CommandLineParametersException("Error converting argument ["+getName()+"] value ["+value+"] to ["+awaited.getCanonicalName()+"] type"); 
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
		public void validate(final String value) throws CommandLineParametersException {
			if (value == null || value.isEmpty()) {
				throw new CommandLineParametersException("Argument ["+getName()+"]: value can't be null or empty");
			}
			else {
				try{URI.create(value);
				} catch (IllegalArgumentException exc) {
					throw new CommandLineParametersException("Argument ["+getName()+"]: value doesn't have valid URI: "+value+" ("+exc.getLocalizedMessage()+")");
				}
			}
		}

		@Override
		public String toString() {
			return "ConfigArg [defaults=" + Arrays.toString(defaults) + ", toString()=" + super.toString() + "]";
		}
	}
}
