package chav1961.purelib.basic;


import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * <p>This class is an extension of the standard {@link Properties} class to support automatic substitutions and data type conversions 
 * when getting properties. Substitution as any template <b>${name}</b> inside any property value. It substitutes with the value of other
 * property, whose key is 'name'. You can also used one-level depth template <b>${${nameLocation}}</b> whose 'nameLocation' means 'get 
 * key name to substitute from the nameLocation key'. All substitutions are recursive. A source of properties to substitute are own 
 * properties, but one-level depth template can be referenced to {@link System#getProperties()} key set. Maximum substitution depth level
 * is restricted by 16</p>
 * <p>You can get property content not only as string, but a lot of other classes:</p>
 * <ul>
 * <li>any appropriative {@link Enum} class constant</li>
 * <li>a subset of primitive type value (int, long, float, double, boolean)</li>
 * <li>a subset of wrappers to primitive type value ({@link Integer}, {@link Long}, {@link Float}, {@link Double}, {@link Boolean})</li>
 * <li>a {@link File} or {@link InputStream} instance, if the property value contains valid file name</li>
 * <li>a {@link URL} or {@link URI} instance, if the property value contains valid URL or URI</li>
 * </ul>
 * <p>To use it, simply type:</p>
 * <code>
 * if (subst.getProperty("canUseSomething",boolean.class)) {<br>
 * 		int amount = subst.getProperty("amountOfSomething",int.class);<br>
 * }<br>
 * </code>
 * 
 * <p>This class is thread-safe.</p>
 *
 * @see Properties
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1 last update 0.0.3
 */
public class SubstitutableProperties extends Properties {
	private static final long 	serialVersionUID = 4802630950148088823L;
	
	private static enum Conversions {
		STRING,	INTWRAPPER, LONGWRAPPER, FLOATWRAPPER, DOUBLEWRAPPER, BOOLEANWRAPPER,
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
								put(File.class,Conversions.FILE);
								put(InputStream.class,Conversions.INPUTSTREAM);
								put(URL.class,Conversions.URL);
								put(URI.class,Conversions.URI);
								put(Color.class,Conversions.COLOR);
								put(char[].class,Conversions.CHARARRAY);
								}};

	public SubstitutableProperties() {}
	
	/**
	 * <p>Constructor of the class. Gets another property class and fill own content with it's content</p> 
	 * @param defaults another properties class to fill own content
	 */
	public SubstitutableProperties(final Properties defaults) throws NullPointerException {
		if (defaults == null) {
			throw new NullPointerException("Defaults for the properties can't be null");
		}
		else {
			putAll(defaults);
		}
	}

	@Override 
	public String getProperty(final String key) {
		final String	value = super.getProperty(key);
		
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
	 */
	public <T> T getProperty(final String key, final Class<T> awaited) {
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
	 */
	public <T> T getProperty(final String key, final Class<T> awaited, final String defaultValue) {
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
			for (Entry<Object, Object> item : this.entrySet()) {
				final String	key = (String)item.getKey();
				
				if (!another.containsKey(key)) {
					return false;
				}
				else if (!another.getProperty(key).equalsIgnoreCase(getProperty(key))) {
					return false;
				}
			}
			for (Entry<Object, Object> item : another.entrySet()) {
				final String	key = (String)item.getKey();
				
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
	
	protected String extendedGetProperty(final String key) {
		if (containsKey(key)) {
			return super.getProperty(key);
		}
		else {
			return System.getProperties().getProperty(key);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T convert(final String key, final String value, final Class<T> awaited) {
		if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null");
		}
		else if (awaited.isEnum()) {
			try{return awaited.cast(awaited.getMethod("valueOf",String.class).invoke(null,value));
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
}
