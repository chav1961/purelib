package chav1961.purelib.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SubstitutableProperties extends Properties {
	private static final long 	serialVersionUID = 4802630950148088823L;
	private static final int	MAX_SUBST_DEPTH = 16;
	
	private static enum Conversions {
		STRING,	INTWRAPPER, LONGWRAPPER, FLOATWRAPPER, DOUBLEWRAPPER, BOOELANWRAPPER,
		FILE, INPUTSTREAM, URL, URI
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
								put(Boolean.class,Conversions.BOOELANWRAPPER);
								put(File.class,Conversions.FILE);
								put(InputStream.class,Conversions.INPUTSTREAM);
								put(URL.class,Conversions.URL);
								put(URI.class,Conversions.URI);
								}};
	
	public SubstitutableProperties(){
		super();
	}
	
	public SubstitutableProperties(final Properties defaults) {
		super(defaults);
	}

	@Override 
	public String getProperty(final String key) {
		final String	value = super.getProperty(key);
		
		return value != null ? substitution(key,value,0) : null;
	}

	@Override 
	public String getProperty(final String key, final String defaultValue) {
		final String	value = super.getProperty(key,defaultValue);
		
		return value != null ? substitution(key,value,0) : null;
	}
	
	public <T> T getProperty(final Class<T> awaited, final String key) {
		final String	value = getProperty(key);
		
		return value != null ? convert(key,value,awaited) : null;
	}

	public <T> T getProperty(final Class<T> awaited, final String key, final String defaultValue) {
		final String	value = getProperty(key,defaultValue);
		
		return value != null ? convert(key,value,awaited) : null;
	}

	private String substitution(final String key, final String value, final int substDepth) {
		if (value == null) {
			return null;
		}
		else if (value.indexOf('$') == -1) {
			return value;
		}
		else if (substDepth >= MAX_SUBST_DEPTH) {
			throw new IllegalArgumentException("Too deep substitution was detected (more tham "+MAX_SUBST_DEPTH+") for key ["+key+"]=["+value+"]. Possibly you have a resursion in the substitution way!"); 
		}
		else {
			final StringBuilder		sb = new StringBuilder();
			int 					from = 0, dollarPos, len = value.length(), bracketCount, startName, endName;
			boolean					wasDollar;
			
			while ((dollarPos = value.indexOf('$',from)) >= 0) {
				sb.append(value,from,dollarPos);
				
				bracketCount = startName = endName = 0;
				wasDollar = false;
				
				if (value.charAt(dollarPos + 1) != '{') {
					throw new IllegalArgumentException("Unpaired {} in the key ["+key+"]=["+value+"]");
				}
				
end:			for (int index = dollarPos + 1; index < len; index++) {
					switch (value.charAt(index)) {
						case '{' 	:
							if (bracketCount++ == 0) {
								startName = index + 1;
							};
							break;
						case '}' 	:
							if (--bracketCount == 0) {
								endName = index;
								break end;
							}
							break;
						case '$'	:
							wasDollar = true;
							break;
					}
				}
				if (bracketCount != 0) {
					throw new IllegalArgumentException("Unpaired {} in the key ["+key+"]=["+value+"]");
				}
				else if (startName > endName) {
					throw new IllegalArgumentException("Empty ${} in the key ["+key+"]=["+value+"]");
				}
				else {
					String	keyName = value.substring(startName,endName);
					
					if (wasDollar) {
						sb.append(substitution(key,keyName,substDepth+1));
					}
					else {
						sb.append(substitution(keyName,extendedGetProperty(keyName),substDepth+1));
					}
					from = endName + 1;
				}
			}
			return sb.append(value,from,len).toString();
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
			throw new IllegalArgumentException("Awaied class can't be null");
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
					case BOOELANWRAPPER	:
						return awaited.cast(Boolean.valueOf(value));
					case FILE			:
						return awaited.cast(new File(value));
					case INPUTSTREAM	:
						return awaited.cast(new FileInputStream(value));						
					case URL			:
						return awaited.cast(new URL(value));
					case URI			:
						return awaited.cast(URI.create(value));
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
