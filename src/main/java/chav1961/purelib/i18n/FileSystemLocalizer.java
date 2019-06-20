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
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.ResourceBundle.Control;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.fsys.FileSystemURLStreamHandler;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;

/**
 * <p>This class is a file-system-based implementation of the {@linkplain Localizer}. It can use any available Pure Library file system to keep localization content. 
 * This class is an Java SPI service and available thru {@linkplain LocalizerFactory}. URI scheme-specific part for it must be well-formed URI with 
 * {@linkplain FileSystemInterface#FILESYSTEM_URI_SCHEME}</p>
 *   
 * @see Localizer
 * @see chav1961.purelib.fsys
 * @see chav1961.purelib.i18n JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public class FileSystemLocalizer extends AbstractLocalizer {
	private static final String		SUBSCHEME = FileSystemInterface.FILESYSTEM_URI_SCHEME;
	private static final URI		SERVE = URI.create(Localizer.LOCALIZER_SCHEME+":"+SUBSCHEME+":/");
	
	private static final String		NEW_LINE_MARK = " \\n";
	
	private final String			resourceAddress;
	private ResourceBundle			bundle;

	public FileSystemLocalizer() throws LocalizationException {
		this.resourceAddress = null;
	}
	
	protected FileSystemLocalizer(final String resourceAddress) throws LocalizationException, NullPointerException {
		if (resourceAddress == null || resourceAddress.isEmpty()) {
			throw new IllegalArgumentException("Resource address can't be null or empty");
		}
		else {
			this.resourceAddress = resourceAddress;
			loadResource(currentLocale().getLocale());
		}
	}

	@Override
	public boolean canServe(final URI localizer) throws NullPointerException {
		return Utils.canServeURI(localizer, SERVE); 
	}

	@Override
	public String getLocalizerId() {
		return "debug:/";
	}

	
	@Override
	public Localizer newInstance(final URI localizer) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (!canServe(localizer)) {
			throw new EnvironmentException("Localizer URI ["+localizer+"] is not supported for the given localizer. Valid URI is ["+SERVE+"...]");
		}
		else {
			return new PropertiesLocalizer(URI.create(URI.create(localizer.getRawSchemeSpecificPart()).getRawSchemeSpecificPart()).getPath()); 
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
	protected void loadResource(final Locale newLocale) throws LocalizationException, NullPointerException {
		if (newLocale == null) {
			throw new IllegalArgumentException("Locale to set can't be null");
		}
		else {
			setBundle(ResourceBundle.getBundle(getResourceAddress(), newLocale, this.getClass().getClassLoader(), new UTF8Control()));
		}
	}

	@Override
	protected String getHelp(final String helpId, final String encoding) throws LocalizationException, IllegalArgumentException {
		if (helpId == null || helpId.isEmpty()) {
			throw new IllegalArgumentException("Help id key can't be null or empty"); 
		}
		else if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("Encoding can't be null or empty"); 
		}
		else {
			final String			resourceLocation =  getResourceAddress()+"/help/"+currentLocale().getLanguage()+"/"+helpId;
			
			try{final URLConnection conn = new URL(null,resourceLocation,new FileSystemURLStreamHandler()).openConnection();
			
		        conn.setDoInput(true);
		        conn.setDoOutput(false);
		        conn.connect();
		        
				try(final InputStream	is = conn.getInputStream()) {
					if (is == null ) {
						throw new IllegalArgumentException("URI reference ["+resourceLocation+"] is not exists"); 
					}
					else {
						try(final Reader	rdr = new InputStreamReader(is,encoding)) {
							return Utils.fromResource(rdr);
						}
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
            final URLConnection 	conn = new URL(null,resourceName,new FileSystemURLStreamHandler()).openConnection();
            
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.connect();
	    	return conn.getInputStream();
	    }
	}
}
