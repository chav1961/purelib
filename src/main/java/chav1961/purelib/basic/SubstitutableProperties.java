package chav1961.purelib.basic;


import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.concurrent.LightWeightListenerList;

/**
 * <p>This class is an extension of the standard {@link Properties} class to support automatic substitutions and data type conversions 
 * when getting properties. Substitution as any template <b>${name}</b> inside any property value. It substitutes with the value of other
 * property, whose key is 'name'. You can also used one-level depth template <b>${${nameLocation}}</b> whose 'nameLocation' means 'get 
 * key name to substitute from the nameLocation key'. All substitutions are recursive. A source of properties to substitute are own 
 * properties, but one-level depth template can be referenced to {@link System#getProperties()} key set. Maximum substitution depth level
 * is restricted by {@value CharUtils#MAX_SUBST_DEPTH}</p>
 * <p>Since 0.0.6 this class supports {@value #KEY_INCLUDE} operator inside the key/value format configuration file. This operator contains a list of {@linkplain URI}(s) to include
 * key/value pairs into the class content separated by semicolons. URI can be any URI supported by Pure Library. Included content doesn't replace keys already
 * exists in the current file. URI stream to load can also contains {@value #KEY_INCLUDE} operator inside.</p>
 * <p>You can get property content not only as string, but a lot of other classes:</p>
 * <ul>
 * <li>any appropriative {@link Enum} class constant</li>
 * <li>a subset of primitive type value (int, long, float, double, boolean)</li>
 * <li>a subset of wrappers to primitive type value ({@link Integer}, {@link Long}, {@link Float}, {@link Double}, {@link Boolean})</li>
 * <li>a {@link File} or {@link InputStream} instance, if the property value contains valid file name</li>
 * <li>a {@link URL} or {@link URI} instance, if the property value contains valid URL or URI</li>
 * </ul>
 * <p>To use it, simply type:</p>
 * {@code 
 * 		if (subst.getProperty("canUseSomething",boolean.class)) {
 * 			final int amount = subst.getProperty("amountOfSomething",int.class);
 * 		}
 * }
 * 
 * <p>This class is thread-safe.</p>
 *
 * @see Properties
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.6
 */
public class SubstitutableProperties extends Properties {
	private static final long 	serialVersionUID = 4802630950148088823L;
	public static final String	KEY_INCLUDE = ".include";
	public static final File	CURRENT_DIR = new File("./");
	
	private static final String	MESSAGE_FILE_NOT_EXISTS = "SubstitutableProperties.notexists";
	private static final String	MESSAGE_FILE_IS_DIRECTORY = "SubstitutableProperties.isdirectory";
	private static final String	MESSAGE_FILE_CANNOT_READ = "SubstitutableProperties.cannottread";

	/**
	 * <p>This interface describes formats of the input content for {@linkplain SubstitutableProperties#load(InputStream, Format)} and {@linkplain SubstitutableProperties#load(Reader, Format)} methods</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6
	 */
	public static enum Format {
		Ordinal, 
		XML, 
		WindowsStyled
	}

	/**
	 * <p>This interface describes listener for group changes (when {@linkplain SubstitutableProperties#putAll(Map)} method was called). When {@linkplain SubstitutableProperties#put(Object, Object)} or 
	 * {@linkplain SubstitutableProperties#putIfAbsent(Object, Object)} methods were called, then {@linkplain #propertyChange(PropertyChangeEvent)} method will be called. When {@linkplain SubstitutableProperties#putAll(Map)} method was called,
	 * method {@linkplain #propertyChange(PropertyChangeEvent)} will not be called, but only {@linkplain #propertiesChange(PropertyGroupChangeEvent)} will be called. This interface is useful to process 'transactional' changes in the
	 * properties content, where some parameters can be dependent each other.</p>  
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6
	 */
	public static interface PropertyGroupChangeListener extends PropertyChangeListener {
		void propertiesChange(PropertyGroupChangeEvent event);
	}
	
	private static enum Conversions {
		STRING,	INTWRAPPER, LONGWRAPPER, FLOATWRAPPER, DOUBLEWRAPPER, BOOLEANWRAPPER,
		BIGINTEGER, BIGDECIMAL,
		FILE, INPUTSTREAM, URL, URI, UUID, COLOR, CHARARRAY, MEMORY_SIZE
	}

	private static final Map<Class<?>,Class<?>>		WRAPPER = new HashMap<Class<?>,Class<?>>(){private static final long serialVersionUID = 1L;
							   {put(int.class,Integer.class);
							    put(long.class,Long.class);
							    put(float.class,Float.class);
							    put(double.class,Double.class);
							    put(boolean.class,Boolean.class);
								}};
	
	private static final Map<Class<?>,Conversions>	DECODER = new HashMap<Class<?>,Conversions>(){private static final long serialVersionUID = 1L;
							   {put(String.class,Conversions.STRING);
								put(Integer.class,Conversions.INTWRAPPER);
								put(Long.class,Conversions.LONGWRAPPER);
								put(Float.class,Conversions.FLOATWRAPPER);
								put(Double.class,Conversions.DOUBLEWRAPPER);
								put(Boolean.class,Conversions.BOOLEANWRAPPER);
								put(BigInteger.class,Conversions.BIGINTEGER);
								put(BigDecimal.class,Conversions.BIGDECIMAL);
								put(File.class,Conversions.FILE);
								put(InputStream.class,Conversions.INPUTSTREAM);
								put(URL.class,Conversions.URL);
								put(URI.class,Conversions.URI);
								put(Color.class,Conversions.COLOR);
								put(char[].class,Conversions.CHARARRAY);
								put(UUID.class,Conversions.UUID);
								put(MemorySize.class,Conversions.MEMORY_SIZE);
								}};

	private final Properties	defaults;
	private final LightWeightListenerList<PropertyChangeListener>	listeners = new LightWeightListenerList<>(PropertyChangeListener.class);

	/**
	 * <p>Constructor if the class</p>
	 * @param content URI to load content from. Can't be null
	 * @throws IOException on any I/O errors
	 * @since 0.0.6
	 */
	public SubstitutableProperties(final URI content) throws IOException {
		this(loadFromUri(content));
	}
	
	
	/**
	 * <p>Constructor if the class</p>
	 */
	public SubstitutableProperties() {
		this(new Properties());
	}
	
	/**
	 * <p>Constructor of the class. Gets another property class and fill own content with it's content</p> 
	 * @param defaults another properties class to fill own content
	 * @throws NullPointerException when parameter is null
	 */
	public SubstitutableProperties(final Properties defaults) throws NullPointerException {
		if (defaults == null) {
			throw new NullPointerException("Defaults for the properties can't be null");
		}
		else {
			this.defaults = defaults;
		}
	}

	/**
	 * <p>Get all available keys</p> 
	 * @return available keys. Can be empty but not null.
	 * @since 0.0.6
	 */
	public Set<String> availableKeys() {
		return extractKeys(this);
	}
	
    @Override
    public boolean containsKey(Object key) {
        return !super.containsKey(key) ? defaults.containsKey(key) : true;
    }

    @Override
    public synchronized Object put(final Object key, final Object value) {
    	final Object	oldValue = super.put(key, value);
    	
    	if (!Objects.equals(oldValue, value)) {
        	final PropertyChangeEvent	e = new PropertyChangeEvent(this, key.toString(), oldValue, value);  
    		
        	listeners.fireEvent((l)->l.propertyChange(e));
    	}
    	return oldValue;
    }
    
    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
    	final Object	oldValue = super.putIfAbsent(key, value);
    	
    	if (!Objects.equals(oldValue, value)) {
        	final PropertyChangeEvent	e = new PropertyChangeEvent(this, key.toString(), oldValue, value);  
    		
        	listeners.fireEvent((l)->l.propertyChange(e));
    	}
    	return oldValue;
    }
    
    @Override
    public synchronized void putAll(final Map<?, ?> values) {
    	if (values == null) {
    		throw new NullPointerException("Values to put can't be null");
    	}
    	else {
    		final List<PropertyChangeDescriptor> pcd = new ArrayList<>();
    		
    		for (java.util.Map.Entry<?, ?> item : values.entrySet()) {
    			final Object	newValue = item.getValue();
    	    	final Object	oldValue = super.put(item.getKey(), newValue);

    	    	if (!Objects.equals(oldValue, newValue)) {
    	    		pcd.add(new PropertyChangeDescriptor(item.getKey(), oldValue, newValue));
    	    	}
    		}
        	if (!pcd.isEmpty()) {
	        	for (PropertyChangeDescriptor item : pcd) {
	        		final PropertyChangeEvent	e = new PropertyChangeEvent(this, item.propertyName.toString(), item.oldValue, item.newValue);
	            	
	        		listeners.fireEvent((l)->{if (!(l instanceof PropertyGroupChangeListener)) {l.propertyChange(e);}});
	        	}
	        	final PropertyGroupChangeEvent	e = new PropertyGroupChangeEvent(this, pcd.toArray(new PropertyChangeDescriptor[pcd.size()]));
        		listeners.fireEvent((l)->{if (l instanceof PropertyGroupChangeListener) {((PropertyGroupChangeListener)l).propertiesChange(e);}});
        	}
    	}
    }
    
    /**
     * <p>Check all the keys presents in the properties</p>
     * @param keys keys to test. Can't be null
     * @return true of all the keys are presented in the properties
     * @throws NullPointerException on null keys list
     * @throws IllegalArgumentException on null inside keys list
     * @since 0.0.5
     */
    public boolean containsAllKeys(final Object... keys) throws NullPointerException, IllegalArgumentException {
    	if (keys == null) {
    		throw new NullPointerException("Key list to check can't be null");
    	}
    	else if (Utils.checkArrayContent4Nulls(keys) >= 0) {
    		throw new IllegalArgumentException("SOme items in the key list are null");
    	}
    	else {
        	for (Object item : keys) {
        		if (!containsKey(item)) {
        			return false;
        		}
        	}
        	return true;
    	}
    }

    /**
     * <p>Check any key presents in the properties</p>
     * @param keys keys to test. Can't be null
     * @return true of all the keys are presented in the properties
     * @throws NullPointerException on null keys list
     * @throws IllegalArgumentException on null inside keys list
     * @since 0.0.5
     */
    public boolean containsAnyKeys(final Object... keys) throws NullPointerException, IllegalArgumentException {
    	if (keys == null) {
    		throw new NullPointerException("Key list to check can't be null");
    	}
    	else if (Utils.checkArrayContent4Nulls(keys) >= 0) {
    		throw new IllegalArgumentException("SOme items in the key list are null");
    	}
    	else {
        	for (Object item : keys) {
        		if (containsKey(item)) {
        			return true;
        		}
        	}
        	return false;
    	}
    }
    
	@Override 
	public String getProperty(final String key) {
		String	value = super.getProperty(key);
		
		if (value == null) {
			value = defaults.getProperty(key);
		}
		
		return value != null ? CharUtils.substitute(key,value,(key2Subst)->{return super.getProperty(key2Subst);}) : null;
	}

	@Override 
	public String getProperty(final String key, final String defaultValue) {
		final String	value = super.getProperty(key,defaultValue);
		
		return value != null ? CharUtils.substitute(key,value,(key2Subst)->{return super.getProperty(key2Subst);}) : null;
	}
	
	/**
	 * <p>Get property and convert it to requested class</p>
	 * @param <T> class returned
	 * @param key key to get property value for
	 * @param awaited awaited class to convert value to
	 * @return value converted
	 * @throws IllegalArgumentException if requested conversion failed or not supported
	 */
	public <T> T getProperty(final String key, final Class<T> awaited) throws IllegalArgumentException {
		final String	value = getProperty(key);
		
		return value != null ? convert(key,value,awaited) : null;
	}

	/**
	 * <p>Get property and convert it to requested class</p>
	 * @param <T> class returned
	 * @param key key to get property value for
	 * @param awaited awaited class to convert value to
	 * @param defaultValue defaultValue to convert when property is missing
	 * @return value converted
	 * @throws IllegalArgumentException if requested conversion failed or not supported
	 */
	public <T> T getProperty(final String key, final Class<T> awaited, final String defaultValue) throws IllegalArgumentException {
		final String	value = getProperty(key,defaultValue);
		
		return value != null ? convert(key,value,awaited) : null;
	}

	/**
	 * <p>Compare properties content is identical</p>
	 * @param another another properties to compare
	 * @return true if two contents are identical
	 * @throws NullPointerException when another properties is null
	 * @since 0.0.3
	 */
	public boolean theSame(final SubstitutableProperties another) throws NullPointerException {
		if (another == null) {
			throw new NullPointerException("Another properties can't be null");
		}
		else {
			for (String key : extractKeys(this)) {
				if (!another.containsKey(key)) {
					return false;
				}
				else if (!another.getProperty(key).equalsIgnoreCase(getProperty(key))) {
					return false;
				}
			}
			for (String key : extractKeys(another)) {
				if (!this.containsKey(key)) {
					return false;
				}
				else if (!this.getProperty(key).equalsIgnoreCase(another.getProperty(key))) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * <p>Try to load content from file</p>
	 * @param content file to load content from
	 * @return true when loading is successful
	 * @since 0.0.5
	 */
	public boolean tryLoad(final File content) {
		return tryLoad(content, PureLibSettings.CURRENT_LOGGER);
	}
	
	/**
	 * <p>Try to load content from file</p>
	 * @param content file to load content from
	 * @param logger logger to type problems to
	 * @return true when loading is successful
	 * @since 0.0.5
	 */
	public boolean tryLoad(final File content, final LoggerFacade logger) {
		if (content == null) {
			throw new NullPointerException("Content file can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (!content.exists()) {
			logger.message(Severity.warning, ()->PureLibSettings.PURELIB_LOCALIZER.getValue(MESSAGE_FILE_NOT_EXISTS, content.getAbsolutePath()));
			return false;
		}
		else if (content.isDirectory()) {
			logger.message(Severity.warning, ()->PureLibSettings.PURELIB_LOCALIZER.getValue(MESSAGE_FILE_IS_DIRECTORY, content.getAbsolutePath()));
			return false;
		}
		else if (!content.canRead()) {
			logger.message(Severity.warning, ()->PureLibSettings.PURELIB_LOCALIZER.getValue(MESSAGE_FILE_CANNOT_READ, content.getAbsolutePath()));
			return false;
		}
		else {
			try(final InputStream	is = new FileInputStream(content)) {
				load(is);
				return true;
			} catch (IOException e) {
				logger.message(Severity.warning, e, ()->PureLibSettings.PURELIB_LOCALIZER.getValue(MESSAGE_FILE_CANNOT_READ, content.getAbsolutePath()));
				return false;
			}
		}
	}
	
	
	@Override
	public void load(final InputStream is) throws IOException {
		load(is,Format.Ordinal);
	}

	@Override
	public void load(final Reader rdr) throws IOException {
		load(rdr,Format.Ordinal);
	}
	
	/**
	 * <p>Load content from input stream with the format typed</p>
	 * @param is input stream to load content from. Can't be null
	 * @param format input stream format. Can't be null
	 * @throws NullPointerException on any argument is null
	 * @throws IOException on any I/O errors
	 * @since 0.0.6
	 */
	public void load(final InputStream is, final Format format) throws NullPointerException, IOException {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null"); 
		}
		else if (format == null) {
			throw new NullPointerException("Format can't be null"); 
		}
		else {
			switch (format) {
				case Ordinal			:
					super.load(is);
					break;
				case WindowsStyled		:
					load(new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING), format);
					break;
				case XML				:
					loadFromXML(is);
					break;
				default:
					throw new UnsupportedOperationException("Format ["+format+"] is not supported yet"); 
			}
		}
	}

	/**
	 * <p>Load content from reader with the format typed. Don't use this method with {@linkplain Format#XML} </p>
	 * @param rdr reader to load content from. Can't be null
	 * @param format reader content format. Can't be null
	 * @throws NullPointerException on any argument is null
	 * @throws IllegalArgumentException on format value is {@linkplain Format#XML} 
	 * @throws IOException on any I/O errors
	 * @since 0.0.6
	 */
	public void load(final Reader rdr, final Format format) throws NullPointerException, IllegalArgumentException, IOException {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null"); 
		}
		else if (format == null) {
			throw new NullPointerException("Format can't be null"); 
		}
		else {
			switch (format) {
				case Ordinal			:
					super.load(rdr);
					break;
				case WindowsStyled		:
					loadWindowsStyled(rdr);
					break;
				case XML				:
					throw new IllegalArgumentException("Format ["+format+"] is not supported for reader. Use load(InputStream,Format) method instead"); 
				default	:
					throw new UnsupportedOperationException("Format ["+format+"] is not supported yet"); 
			}
		}
	}

	/**
	 * <p>Store properties content into the given file</p> 
	 * @param file2Store file to store content to. Can't be null
	 * @throws NullPointerException parameter is null
	 * @throws IllegalArgumentException parameter is directory or can't be writted
	 * @throws IOException on any I/O errors
	 */
	public void store(final File file2Store) throws NullPointerException, IllegalArgumentException, IOException {
		if (file2Store == null) {
			throw new NullPointerException("File to store content to can't be null");
		}
		else if (file2Store.exists() && (!file2Store.isFile() || !file2Store.canWrite())) {
			throw new IllegalArgumentException("File to store content to [" + file2Store.getAbsolutePath() + "] is not a file or you don't have access rights to store into it");
		}
		else {
			try(final FileOutputStream	fos = new FileOutputStream(file2Store)) {
				store(fos, "");
			}
		}
	}
	
	
	/**
	 * <p>Convert value content to type awaited</p> 
	 * @param <T> converted instance type
	 * @param key key associated with the given value. Uses in diagnostic purposes only. Can be null
	 * @param value value to convert
	 * @param awaited awaited class for value converted
	 * @return value converted or null
	 * @throws NullPointerException if awaited class is null
	 * @throws IllegalArgumentException if requested conversion failed or not supported
	 * @since 0.0.3
	 * @lastUdate 0.0.6
	 */	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T convert(final String key, final String value, final Class<T> awaited) throws NullPointerException, IllegalArgumentException{
		if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null");
		}
		else if (awaited.isEnum()) {
			try{return (T)Enum.valueOf((Class<Enum>)awaited,value);
			} catch (Exception e) {
				throw new IllegalArgumentException("Can't convert value ["+value+"] to enum class ["+awaited+"] for key ["+key+"]: "+e.getMessage());
			}
		}
		else if (awaited.isPrimitive()) {
			if (WRAPPER.containsKey(awaited)) {
				return (T)convert(key,value,WRAPPER.get(awaited));
			}
			else {
				throw new UnsupportedOperationException("Conversion to ["+awaited+"] is not implemented yet");
			}
		}
		else if (DECODER.containsKey(awaited)) {
			try{switch (DECODER.get(awaited)) {
					case STRING			:
						return awaited.cast(value);
					case INTWRAPPER		:
						return awaited.cast(Integer.valueOf(value));
					case LONGWRAPPER	:
						return awaited.cast(Long.valueOf(value));
					case FLOATWRAPPER	:
						return awaited.cast(Float.valueOf(value));
					case DOUBLEWRAPPER	:
						return awaited.cast(Double.valueOf(value));
					case BOOLEANWRAPPER	:
						return awaited.cast(Boolean.valueOf(value));
					case BIGINTEGER		:
						return awaited.cast(new BigInteger(value));
					case BIGDECIMAL		:
						return awaited.cast(new BigDecimal(value));
					case FILE			:
						return awaited.cast(new File(value));
					case INPUTSTREAM	:
						return awaited.cast(new FileInputStream(value));						
					case URL			:
						final URI	tempURI1 = URI.create(value);
						
						if (tempURI1.isAbsolute()) {
							return awaited.cast(tempURI1.toURL());
						}
						else {
							return awaited.cast(new File(CURRENT_DIR, value).toURI().toURL());
						}
					case URI			:
						final URI	tempURI2 = URI.create(value);
						
						if (tempURI2.isAbsolute()) {
							return awaited.cast(tempURI2);
						}
						else {
							return awaited.cast(new File(CURRENT_DIR, value).toURI());
						}
					case COLOR			:
						return awaited.cast(PureLibSettings.colorByName(value,null));
					case CHARARRAY		:
						return awaited.cast(value == null ? null : value.toCharArray());
					case UUID			:
						return awaited.cast(value == null ? null : UUID.fromString(value));
					case MEMORY_SIZE	:
						return awaited.cast(value == null ? null : MemorySize.valueOf(value));
					default :
						throw new UnsupportedOperationException("Conversion to ["+awaited+"] is not implemented yet");
				}		
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("Can't convert value ["+value+"] to URL format for key ["+key+"]");
			} catch (NumberFormatException exc) {
				throw new IllegalArgumentException("Can't convert value ["+value+"] to digital format for key ["+key+"]");
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("Can't convert value ["+value+"] to file input stream format for key ["+key+"]");
			}
		}
		else {
			throw new UnsupportedOperationException("Unsupported class ["+awaited+"] to convert");
		}
	}

	/**
	 * <p>Add property change listener</p> 
	 * @param listener listener to add. Can't be null
	 * @throws NullPointerException if listener is null
     * @since 0.0.5
	 */
	public void addPropertyChangeListener(final PropertyChangeListener listener) throws NullPointerException {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listeners.addListener(listener);
		}
	}
	
	/**
	 * <p>Remove property change listener</p>
	 * @param listener listener to remove. Can't be null
	 * @throws NullPointerException if listener is null
     * @since 0.0.5
	 */
	public void removePropertyChangeListener(final PropertyChangeListener listener) throws NullPointerException {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listeners.addListener(listener);
		}
	}
	
	/**
	 * <p>Create properties from file</p>
	 * @param file file to create properties from. Can't be null 
	 * @return properties created from the file
	 * @throws NullPointerException null file
	 * @throws IllegalArgumentException file not exists, is not a file or is not accessible for you
	 * @throws IOException any I/O errors
	 * @since 0.0.6
	 */
	public static SubstitutableProperties of(final File file) throws IOException, NullPointerException, IllegalArgumentException {
		if (file == null) {
			throw new NullPointerException("File can't be null");
		}
		else if (!file.exists() || !file.isFile() || !file.canRead()) {
			throw new IllegalArgumentException("File ["+file.getAbsolutePath()+"] is not exists, is not a file or is not accessible for you"); 
		}
		else {
			try(final InputStream	is = new FileInputStream(file)) {
				return of(is);
			}
		}
	}

	/**
	 * <p>Create properties from input stream</p>
	 * @param file input stream to create properties from. Can't be null 
	 * @return properties created from the file
	 * @throws NullPointerException null input stream
	 * @throws IOException any I/O errors
	 * @since 0.0.6
	 */
	public static SubstitutableProperties of(final InputStream is) throws IOException {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null");
		}
		else {
			final SubstitutableProperties	props = new SubstitutableProperties();

			props.load(is);
			return props;
		}
	}
	
	protected String extendedGetProperty(final String key) {
		if (containsKey(key)) {
			return super.getProperty(key);
		}
		else if (System.getProperties().containsKey(key)) {
			return System.getProperties().getProperty(key);
		}
		else {
			return System.getenv().get(key);
		}
	}

	private void loadWindowsStyled(final Reader rdr) throws IOException{
		final BufferedReader	brdr = new BufferedReader(rdr);
		String					section = "[]", line;
		
		while ((line = brdr.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty() && line.charAt(0) != '#') {
				if (line.charAt(0) == '[') {
					section = line;
				}
				else {
					final int	equalSign = line.indexOf('=');
					
					if (equalSign > 0) {
						final StringBuilder	sb = new StringBuilder();
						
						CharUtils.parseString(CharUtils.terminateAndConvert2CharArray(line.substring(equalSign + 1),'\n'), 0, '\n', sb);
						setProperty(section+'.'+line.substring(0, equalSign).trim(), sb.toString());
					}
				}
			}
		}
	}
	
	private static Set<String> extractKeys(final SubstitutableProperties props) {
		final Set<String>	result = new HashSet<>();
		
		for (Entry<Object,Object> item : props.entrySet()) {
			result.add(item.getKey().toString());
		}
		for (Entry<Object,Object> item : props.defaults.entrySet()) {
			result.add(item.getKey().toString());
		}
		return result;
	}

	private static Properties loadFromUri(final URI content) throws IOException {
		return loadFromUri(content, new HashSet<>());
	}
	
	private static Properties loadFromUri(final URI content, final Set<URI> processed) throws IOException {
		if (content == null) {
			throw new NullPointerException("URI to load content from can't be null"); 
		}
		else if (processed.contains(content)) {
			throw new IllegalArgumentException("Recursive include for URI ["+content+"], processed URIs are "+processed); 
		}
		else {
			final Properties	props = new Properties();
			
			try (final InputStream	is = content.toURL().openStream()) {
				props.load(is);
			}
			processed.add(content);
			
			if (props.containsKey(KEY_INCLUDE)) {
				final Properties	included = loadFromIncludeList((String)props.remove(KEY_INCLUDE), processed);
				
				included.putAll(props);
				return included;
			}
			else {
				return props;
			}
		}
	}

	private static Properties loadFromIncludeList(final String content, final Set<URI> processed) throws IOException {
		final List<Properties>	collectedProps = new ArrayList<>();
		final Properties		result = new Properties();
		
		for (String item : content.split(";")) {
			final URI	uri = URI.create(item);
			
			if (uri.isAbsolute()) {
				collectedProps.add(loadFromUri(uri, processed));
			}
			else {
				collectedProps.add(loadFromUri(new File(item).getAbsoluteFile().toURI(), processed));
			}
		}
		for (Properties item : collectedProps) {
			result.putAll(item);
		}
		return result;
	}

	/**
	 * <p>This class describes group changes event. It is used in conjunction of the {@linkplain PropertyGroupChangeEvent} interface.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @see PropertyGroupChangeEvent
	 * @see PropertyChangeDescriptor
	 * @since 0.0.6
	 */
	public static class PropertyGroupChangeEvent extends EventObject {
		private static final long serialVersionUID = 7659660893846279186L;
		
		private final PropertyChangeDescriptor[]	properties;
		
		public PropertyGroupChangeEvent(final Object source, final PropertyChangeDescriptor... properties) {
			super(source);
			this.properties = properties;
		}
		
		public PropertyChangeDescriptor[] getChanges() {
			return properties;
		}
	}
	
	/**
	 * <p>This class is a descriptor about individual property change. It is used with conjunction of the {@linkplain PropertyGroupChangeEvent} class.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6
	 */
	public static class PropertyChangeDescriptor implements Serializable {
		private static final long serialVersionUID = -4541840579942022901L;
		
		/**
		 * <p>Name of the property changed</p>
		 */
		public final Object	propertyName;
		
		/**
		 * <p>Old value of the property changed </p>
		 */
		public final Object	oldValue;
		
		/**
		 * <p>New value of the property changed</p>
		 */
		public final Object	newValue;
		
		public PropertyChangeDescriptor(Object propertyName, Object oldValue, Object newValue) {
			this.propertyName = propertyName;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
			result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
			result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PropertyChangeDescriptor other = (PropertyChangeDescriptor) obj;
			if (newValue == null) {
				if (other.newValue != null) return false;
			} else if (!newValue.equals(other.newValue)) return false;
			if (oldValue == null) {
				if (other.oldValue != null) return false;
			} else if (!oldValue.equals(other.oldValue)) return false;
			if (propertyName == null) {
				if (other.propertyName != null) return false;
			} else if (!propertyName.equals(other.propertyName)) return false;
			return true;
		}

		@Override
		public String toString() {
			return "PropertyChangeDescriptor [propertyName=" + propertyName + ", oldValue=" + oldValue + ", newValue=" + newValue + "]";
		}
	}
}
