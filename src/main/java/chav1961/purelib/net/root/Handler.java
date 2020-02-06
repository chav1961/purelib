package chav1961.purelib.net.root;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import chav1961.purelib.basic.URIUtils;

/**
 * <p>This class is  handler to support "root" schema URL. Format of root URL is:</p>
 * <code><b>root://</b>&lt;className&gt;/&lt;pathTooResource&gt;</code>
 * <ul>
 * <li>className - qualified canonical name of any class in your application. It will be used in {@linkplain Class#forName(String)} call</li>
 * <li>pathToResource - absolute path to your resource from the root of your application</li>
 * </ul>
 * @see URLStreamHandler   
 * @see RootHandlerProvider   
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.4
 */
public class Handler extends URLStreamHandler {
	public static final String	PROTOCOL = "root";
	
	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		try{final URI 			content = url.toURI();
		
			if (URIUtils.containsNestedURI(content)) {
				final URI		nested = URIUtils.extractNestedURI(content);
				final URI		path = URIUtils.extractPathInNestedURI(content);
				
				if (nested.getScheme() != null) {
					final URL		newURL = nested.resolve(path).toURL();
					
					return newURL.openConnection();
				}
				else {
					final Class<?>	clazz = Class.forName(nested.toString());
					final URI		resource = clazz.getResource(clazz.getSimpleName()+".class").toURI();
					
					return resource.resolve(path).toURL().openConnection();
				}				
			}
			else {
				final String	host = url.getHost();
				final String	path = url.getPath();
				final Class<?>	clazz = Class.forName(host);
				final URI		resource = clazz.getResource(clazz.getSimpleName()+".class").toURI();
				
				final StringBuilder	resourcePath = new StringBuilder(resource.toString()).append("/../");
				int				index = 0;
				
				while ((index = host.indexOf('.',index)) > 0) {
					resourcePath.append("../");
					index++;
				}
				resourcePath.append(path.substring(1));
				final URI		location = URI.create(resourcePath.toString()).normalize();
				
				return location.toURL().openConnection();
			}
		} catch (URISyntaxException | ClassNotFoundException exc) {
			throw new IOException("Illegal URL ["+url+"]: "+exc.getLocalizedMessage(),exc); 
		}
	}
}
