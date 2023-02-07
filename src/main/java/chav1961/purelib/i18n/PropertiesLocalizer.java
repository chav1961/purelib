package chav1961.purelib.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;

/**
 * <p>This class is a wrapper to standard Java {@linkplain ResourceBundle} class. This class is an Java SPI service and available thru 
 * {@linkplain LocalizerFactory}. URI scheme-specific part for it must be:</p>
 * <code>{@value #SUBSCHEME}:/valid_path_to_jar_resource</code>
 *   
 * @see Localizer
 * @see chav1961.purelib.fsys
 * @see chav1961.purelib.i18n JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.7
 * @deprecated
 */
@Deprecated(since="Java 9")
public class PropertiesLocalizer extends AbstractLocalizer {
	private static final String		SUBSCHEME = "prop";
	private static final URI		SERVE = URI.create(Localizer.LOCALIZER_SCHEME+":"+SUBSCHEME+":/");
	
	private static final String		NEW_LINE_MARK = " \\n";

	private String					localizerURI = "unknown:/";
	private final String			resourceAddress;
	private ResourceBundle			bundle;
	
	public PropertiesLocalizer() throws LocalizationException, NullPointerException {
		this.resourceAddress = null;
	}
	
	protected PropertiesLocalizer(final String resourceAddress) throws LocalizationException, NullPointerException {
		if (resourceAddress == null || resourceAddress.isEmpty()) {
			throw new IllegalArgumentException("Resource address can't be null or empty");
		}
		else {
			this.resourceAddress = resourceAddress;
			loadResource(currentLocale().getLocale());
		}
	}

	@Override
	public boolean canServe(final URI localizer) {
		return URIUtils.canServeURI(localizer, SERVE); 
	}

	@Override
	public URI getLocalizerId() {
		return URI.create(localizerURI);
	}
	
	@Override
	public Localizer newInstance(final URI localizer) throws EnvironmentException {
		if (!canServe(localizer)) {
			throw new EnvironmentException("Localizer URI ["+localizer+"] is not supported for the given localizer. Valid URI is ["+SERVE+"...]");
		}
		else {
			final PropertiesLocalizer	result = new PropertiesLocalizer(URI.create(URI.create(localizer.getRawSchemeSpecificPart()).getRawSchemeSpecificPart()).getPath());
			
			result.localizerURI = localizer.toString();
			return result;
		}
	}
	
	@Override
	public Iterable<String> localKeys() {
		final Set<String>			result = new HashSet<>();
		final Enumeration<String>	keys = getBundle().getKeys();
			
		while (keys.hasMoreElements()) {
			result.add(keys.nextElement());
		}
		return result;
	}
 
	@Override
	public String getLocalValue(final String key) throws LocalizationException, IllegalArgumentException {
		String		result = getBundle().getString(key).trim();
		final int	newLine = result.indexOf(NEW_LINE_MARK);
		
		if (newLine >= 0) {
			result = result.replace(NEW_LINE_MARK, "\n");
		}
		return result; 
	}

	@Override
	public String getLocalValue(final String key, final  Locale locale) throws LocalizationException, IllegalArgumentException {
		String		result = getBundle().getString(key).trim();
		final int	newLine = result.indexOf(NEW_LINE_MARK);
		
		if (newLine >= 0) {
			result = result.replace(NEW_LINE_MARK, "\n");
		}
		return result; 
	}
	
	@Override
	public String toString() {
		return "PropertiesLocalizer [resourceAddress="+resourceAddress+", localizerURI="+localizerURI+"]";
	}

	@Override
	protected void loadResource(final Locale newLocale) throws LocalizationException, NullPointerException {
		if (newLocale == null) {
			throw new IllegalArgumentException("Locale to set can't be null");
		}
		else {
			try{setBundle(ResourceBundle.getBundle(getResourceAddress(), newLocale, this.getClass().getClassLoader(), new UTF8Control()));
			} catch (MissingResourceException | UnsupportedOperationException exc) {
				throw new LocalizationException("Resource ["+getResourceAddress()+"], locale ["+newLocale+"]: "+exc.getLocalizedMessage(),exc); 
			}
		}
	}
	
	@Override
	protected String getHelp(final String helpId, final Locale locale, final String encoding) {
		if (helpId == null || helpId.isEmpty()) {
			throw new IllegalArgumentException("Help id key can't be null or empty"); 
		}
		else if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("Encoding can't be null or empty"); 
		}
		else {
			final String	resourceLocation =  "/"+getResourceAddress()+"/help/"+locale.getLanguage()+"/"+helpId;
			
			try(final InputStream		is = PropertiesLocalizer.class.getResourceAsStream(resourceLocation)) {
				if (is == null ) {
					throw new IllegalArgumentException("URI reference ["+resourceLocation+"] is not exists"); 
				}
				else {
					try(final Reader	rdr = new InputStreamReader(is,encoding)) {
						return Utils.fromResource(rdr);
					}
				}
			} catch (IOException e) {
				throw new IllegalArgumentException("URI reference ["+resourceLocation+"]: I/O error while reading ("+e.getMessage()+")"); 
			}
		}
	}

	protected String getResourceAddress() {
		return resourceAddress;
	}
	
	protected ResourceBundle getBundle() {
		return bundle;
	}

	protected void setBundle(final ResourceBundle bundle) {
		this.bundle = bundle;
	}
	
	protected static class UTF8Control extends Control {
		public UTF8Control(){
		}
		
	    public ResourceBundle newBundle (final String baseName, final Locale locale, final String format, final ClassLoader loader, final boolean reload) throws IllegalAccessException, InstantiationException, IOException {
	        final String 	resourceName = toBundleName(baseName, locale)+".properties";
	        
	        try(final InputStream	is = getContentStream(resourceName,loader,reload)) {
	        	if (is == null) {
	        		return null;
	        	}
	        	else {
	        		try(final Reader		rdr = new InputStreamReader(is,DEFAULT_CONTENT_ENCODING)) {
	        			
	                    return new PropertyResourceBundle(rdr);
	        		}
	        	}
            }
	    }
	    
	    private InputStream getContentStream(final String resourceName, final ClassLoader loader, final boolean reload) throws IOException {
	        if (reload) {
	            final URL 	url = loader.getResource(resourceName);
	            
	            if (url != null) {
	                final URLConnection 	conn = url.openConnection();
	                
	                if (conn != null) {
	                    conn.setUseCaches(false);
	                    return conn.getInputStream();
	                }
	            }
	            return null;
	        } else {
	        	return loader.getResourceAsStream(resourceName); 
	        }
	    }
	}

	@Override
	protected boolean isLocaleSupported(final String key, final Locale locale) throws LocalizationException, IllegalArgumentException {
		return true;
	}
}
