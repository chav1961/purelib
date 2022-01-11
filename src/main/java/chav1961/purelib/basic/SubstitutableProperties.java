package chav1961.purelib.basic;


import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

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
 * @lastUpdate 0.0.5
 */
public class SubstitutableProperties extends Properties {
	private static final long 	serialVersionUID = 4802630950148088823L;
	private static final String	MESSAGE_FILE_NOT_EXISTS = "SubstitutableProperties.notexists";
	private static final String	MESSAGE_FILE_IS_DIRECTORY = "SubstitutableProperties.isdirectory";
	private static final String	MESSAGE_FILE_CANNOT_READ = "SubstitutableProperties.cannottread";
	private static final String	MESSAGE_FILE_IO_ERROR = "SubstitutableProperties.ioerror";
	
	private static enum Conversions {
		STRING,	INTWRAPPER, LONGWRAPPER, FLOATWRAPPER, DOUBLEWRAPPER, BOOLEANWRAPPER,
		BIGINTEGER, BIGDECIMAL,
		FILE, INPUTSTREAM, URL, URI, COLOR, CHARARRAY
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
								}};

	private final Properties	defaults;
	private final LightWeightListenerList<PropertyChangeListener>	listeners = new LightWeightListenerList<>(PropertyChangeListener.class);
	
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
	
    @Override
    public boolean containsKey(Object key) {
        return !super.containsKey(key) ? defaults.containsKey(key) : true;
    }

    @Override
    public synchronized Object put(final Object key, final Object value) {
    	final Object	oldValue = super.put(key, value);
    	final PropertyChangeEvent	e = new PropertyChangeEvent(this, key.toString(), oldValue, value);  
    	
    	listeners.fireEvent((l)->l.propertyChange(e));
    	return oldValue;
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
						return awaited.cast(new URL(value));
					case URI			:
						return awaited.cast(URI.create(value));
					case COLOR			:
						return awaited.cast(PureLibSettings.colorByName(value,null));
					case CHARARRAY		:
						return awaited.cast(value == null ? null : value.toCharArray());
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
}
