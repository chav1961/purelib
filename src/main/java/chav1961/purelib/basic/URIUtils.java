package chav1961.purelib.basic;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Hashtable;

/**
 * <p>This class contains implementation of the useful actions with the URIs.</p> 
 * 
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class URIUtils {
	/**
	 * <p>Does the resource URI can be served by the given URI resource template</p> 
	 * @param uri uri to check
	 * @param template resource template to compare with. Template must be absolute and contains valid scheme and subScheme
	 * @return true if can
	 * @throws NullPointerException if any argument ia null
	 * @throws IllegalArgumentException if template doesn't appropriates with the template constraints
	 */
	public static boolean canServeURI(final URI uri, final URI template) throws NullPointerException, IllegalArgumentException {
		if (uri == null) {
			throw new NullPointerException("URI to test can't be null");
		}
		else if (template == null) {
			throw new NullPointerException("Template URI can't be null");
		}
		else if (!template.isAbsolute() || template.getScheme() == null || template.getRawSchemeSpecificPart() == null) {
			throw new IllegalArgumentException("Template URI ["+template+"] isn't absolute or doesn't contain scheme or scheme-specific part");
		}
		else if (!uri.isAbsolute() || !template.getScheme().equalsIgnoreCase(uri.getScheme()) || uri.getRawSchemeSpecificPart() == null) {
			return false;
		}
		else {
			final URI	subUri = URI.create(uri.getRawSchemeSpecificPart()), subTemplate =  URI.create(template.getRawSchemeSpecificPart());
			
			if (!subTemplate.isAbsolute() || subTemplate.getScheme() == null) {
				throw new IllegalArgumentException("Template URI ["+template+"]: uri subScheme isn't absolute or doesn't contain subScheme or subScheme-specific part");
			}
			else {
				return subTemplate.getScheme().equalsIgnoreCase(subUri.getScheme());
			}
		}
	}
	
	/**
	 * <p>Load bytes from the given URI.</p>
	 * @param uri uri to load bytes from
	 * @return bytes loaded
	 * @throws NullPointerException when uri is null
	 * @throws IOException on any I/O errors
	 */
	public static byte[] loadBytesFromURI(final URI uri) throws NullPointerException, IOException {
		if (uri == null) {
			throw new NullPointerException("URI to load data from can't be null");  
		}
		else {
			final URL			url = uri.toURL();
			
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
				final InputStream			is = url.openStream()) {
				
				Utils.copyStream(is,baos);
				return baos.toByteArray();
			}
		}
	}

	/**
	 * <p>Load chars from the given URI.</p>
	 * @param uri uri to load bytes from
	 * @return chars loaded
	 * @throws NullPointerException when uri is null
	 * @throws IOException on any I/O errors
	 */
	public static char[] loadCharsFromURI(final URI uri) throws NullPointerException, IOException {
		if (uri == null) {
			throw new NullPointerException("URI to load data from can't be null");  
		}
		else {
			final URL				url = uri.toURL();
			
			try(final Writer		wr = new CharArrayWriter();
				final InputStream	is = url.openStream();
				final Reader		rdr = new InputStreamReader(is)) {
				
				Utils.copyStream(rdr,wr);
				return wr.toString().toCharArray();
			}
		}
	}

	/**
	 * <p>Load chars from the given URI.</p>
	 * @param uri uri to load bytes from
	 * @param encoding content encoding
	 * @return chars loaded
	 * @throws NullPointerException when uri is null
	 * @throws IOException on any I/O errors
	 */
	public static char[] loadCharsFromURI(final URI uri, final String encoding) throws NullPointerException, IllegalArgumentException, IOException {
		if (uri == null) {
			throw new NullPointerException("URI to load data from can't be null");  
		}
		else if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("Content encoding can't be null or empty string");  
		}
		else {
			final URL				url = uri.toURL();
			
			try(final Writer		wr = new CharArrayWriter();
				final InputStream	is = url.openStream();
				final Reader		rdr = new InputStreamReader(is,encoding)) {
				
				Utils.copyStream(rdr,wr);
				return wr.toString().toCharArray();
			}
		}
	}

	/**
	 * <p>Test that given URI contains nested URI. NEsted URI is any URI in the scheme-specific part, terminated with exclamation mark (!)</p>
	 * @param uri uri to test
	 * @return true if the given uri contains nested URI
	 * @throws NullPointerException if uri is null
	 * @since 0.0.2
	 */
	public static boolean containsNestedURI(final URI uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("Uri can't be null");
		}
		else {
			final String 		path = uri.getPath();
			
			if (path != null && !path.isEmpty()) {
				return path.lastIndexOf('!') >= 0;
			}
			else if (path == null) {
				final String	specific = uri.getSchemeSpecificPart();
				
				return specific.lastIndexOf('!') >= 0;
			}
			else {
				return false;
			}
		}
	}
	
	/**
	 * <p>Extract nested URI from the given URI</p>
	 * @param uri uri to extract nested URI from
	 * @return URI extracted or null if no nested URI inside the given uri instance
	 * @throws NullPointerException if uri is null
	 * @see #containsNestedURI(URI)
	 * @since 0.0.2
	 */
	public static URI extractNestedURI(final URI uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("Uri can't be null");
		}
		else if (containsNestedURI(uri)) {
			final String 	path = uri.getPath();
			
			if (path != null && !path.isEmpty()) {
				final int 	tail = path.lastIndexOf('!');
				
				return URI.create(path.substring(0,tail));
			}
			else if (path == null) {
				final String	specific = uri.getSchemeSpecificPart();
				
				return URI.create(specific.substring(0,specific.lastIndexOf('!')));
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	/**
	 * <p>Extract path in the nested uri. Path in the nested URI is all path content after last exclamation mark (!)</p>
	 * @param uri uri to extract path from
	 * @return path extracted or null if no nested URI inside the given uri instance
	 * @throws NullPointerException if uri is null
	 * @see #containsNestedURI(URI)
	 * @since 0.0.2
	 */
	public static URI extractPathInNestedURI(final URI uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("Uri can't be null");
		}
		else if (containsNestedURI(uri)) {
			final String 	path = uri.getPath();
			
			if (path != null && !path.isEmpty()) {
				final int 	tail = path.lastIndexOf('!');
				
				return URI.create(path.substring(tail+1));
			}
			else if (path == null) {
				final String	specific = uri.getSchemeSpecificPart();
				
				return URI.create(specific.substring(specific.lastIndexOf('!')+1));
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	/**
	 * <p>Append relative path to path inside URI.</p>
	 * @param uri uri to append relative path to
	 * @param relativePath path to append
	 * @return new resolved uri
	 * @throws NullPointerException if uri is null
	 * @throws IllegalArgumentException if relative path is null or empty
	 * @see https://tools.ietf.org/html/rfc3986
	 * @since 0.0.3
	 * @lastUpdate 0.0.4
	 */
	public static URI appendRelativePath2URI(final URI uri, final String relativePath) throws NullPointerException, IllegalArgumentException {
		if (uri == null) {
			throw new NullPointerException("Uri to append path to can't be null");
		}
		else if (relativePath == null || relativePath.isEmpty()) {
			throw new IllegalArgumentException("Uri to append path to can't be null");
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			URI					parse = uri;
			
			while (parse.isAbsolute()) {
				sb.append(parse.getScheme()).append(':');
				parse = URI.create(parse.getSchemeSpecificPart());
			}
			if (parse.getHost() != null) {
				sb.append("//").append(parse.getHost());
			}
			if (parse.getPath() != null) {
				sb.append(URI.create(parse.getPath()+(relativePath.charAt(0) == '/' ? relativePath : '/' + relativePath)).normalize());
			}
			final String	fragment = uri.getFragment(), query = uri.getQuery();
			
			if (fragment != null || query != null) {
				if (fragment == null) {
					sb.append('?').append(query);
				}
				else if (query == null) {
					sb.append('#').append(fragment);
				}
				else if (fragment.endsWith(query)) {
					sb.append('#').append(fragment);
				}
				else {
					sb.append('#').append(fragment).append('?').append(query);
				}
			}
			
			return URI.create(sb.toString());
		}
	}
	
	/**
	 * <p>Remove query string from URI</p>
	 * @param uri uri to remove query from
	 * @return uri with query removed. If query is missing, returns source uri
	 * @throws NullPointerException if uri is null
	 * @see https://tools.ietf.org/html/rfc3986
	 * @since 0.0.3
	 */
	public static URI removeQueryFromURI(final URI uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("Uri to remove query from can't be null");
		}
		else {
			final String		str = uri.toString();
			
			if (str.contains("?")) {
				if (uri.getFragment() != null) {
					final String	frag = str.substring(str.lastIndexOf('#'));
					
					return URI.create(str.substring(0,str.lastIndexOf('?'))+frag);
				}
				else {
					return URI.create(str.substring(0,str.lastIndexOf('?')));
				}
			}
			else {
				return uri;
			}
		}
	}

	/**
	 * <p>Extract query string from multi-schemed URI</p>
	 * @param uri uri to extract query from
	 * @return query extracted or null if missing
	 * @throws NullPointerException when URI is null
	 * @since 0.0.3
	 */
	public static String extractQueryFromURI(final URI uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("URI to extract query from can't be null");
		}
		else {
			final String	content = uri.toString();
			final int		index = content.lastIndexOf('?');
			
			if (index == -1) {
				return null;
			}
			else {
				return content.substring(index+1);
			}
		}
	}
	
	/**
	 * <p>Test weather URI has the given scheme at any depth</p>
	 * @param uri uri to test
	 * @param scheme scheme to detect
	 * @return true if the given scheme presents at any depth, otherwise false
	 * @since 0.0.3
	 */
	public static boolean hasSubScheme(final URI uri, final String scheme) {
		if (uri == null) {
			throw new NullPointerException("URI to test can't be null");
		}
		else if (scheme == null || scheme.isEmpty()) {
			throw new IllegalArgumentException("Scheme string can'ty be null or empty");
		}
		else if (!uri.isAbsolute()) {
			return false;
		}
		else {
			if (scheme.equalsIgnoreCase(uri.getScheme())) {
				return true;
			}
			else {
				return hasSubScheme(URI.create(uri.getSchemeSpecificPart()), scheme);
			}
		}
	}

	/**
	 * <p>Extract sub-scheme content from URI. For example:</p>
	 * <code>
	 * URI uri = URI.create("scheme1:scheme2:scheme3://path");</br>
	 * extractSubURI(uri,"scheme1","scheme2") returns URI.create("scheme3://path")</br> 
	 * </code> 
	 * @param uri uri to extract sub-scheme from
	 * @param schemas sequence of schemas to extract content from. Use wildcard '*' for any subscheme to extract
	 * @return URI extracted
	 * @throws NullPointerException if any parameters are null
	 * @throws IllegalArgumentException if schemas list is empty or contains nulls, or sequence of sub-schemas is missing in the uri
	 * @since 0.0.4
	 */
	public static URI extractSubURI(final URI uri, final String... schemas) throws IllegalArgumentException, NullPointerException {
		int		nullPos;
		
		if (uri == null) {
			throw new NullPointerException("URI to extract from can't be null");
		}
		else if (!uri.isAbsolute()) {
			throw new IllegalArgumentException("URI is not absolute to extract sub URI from");
		}
		else if (schemas == null || schemas.length == 0) {
			throw new IllegalArgumentException("Schemas sequence can't be null or empty");
		}
		else if ((nullPos = Utils.checkArrayContent4Nulls(schemas)) != -1) {
			throw new IllegalArgumentException("NUll agrument inside schemas sequence at index ["+nullPos+"]");
		}
		else {
			URI	current = uri;
			
			for (String scheme : schemas) {
				if (scheme.equals(current.getScheme()) || "*".equals(scheme)) {
					current = URI.create(current.getSchemeSpecificPart());
				}
				else {
					throw new IllegalArgumentException("Error extracting subScheme ["+scheme+"] from URI : URI ["+uri+"] doesn't contain it at requested position ("+Arrays.toString(schemas)+" awaited)");
				}
			}
			return current;
		}
	}
	
	
	/**
	 * <p>Parse query string from uri</p>
	 * @param uri uri to parse query string
	 * @return key/value pair from parsed query. Can be empty but not null
	 * @throws NullPointerException when uri is null
	 * @throws link IllegalArgumentException when query contains syntax errors
	 * @since 0.0.3
	 */
	public static Hashtable<String,String[]> parseQuery(final URI uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("Uri to parsre can't be null"); 
		}
		else {
			final String	query = URIUtils.extractQueryFromURI(uri);
			
			if (query != null && !query.isEmpty()) {
				return parseQuery(query);
			}
			else {
				return new Hashtable<>();
			}
		}
	}

	/**
	 * <p>Parse query string (usually from uri query)</p>
	 * @param query query string without preceding '?'
	 * @return key/value pair from parsed query. Can be empty but not null
	 * @throws NullPointerException when uri is null
	 * @throws link IllegalArgumentException when query contains syntax errors
	 * @since 0.0.3
	 */
	public static Hashtable<String,String[]> parseQuery(final String query) throws NullPointerException, IllegalArgumentException {
		if (query == null) {
			throw new NullPointerException("Query to parsre can't be null"); 
		}
		else if (query.isEmpty()) {
			return new Hashtable<>();
		}
		else {
			final Hashtable<String,String[]>	result = new Hashtable<>();
			
			for (String item : CharUtils.split(query,'&')) {
				final int	index = item.indexOf('=');

				if (index > 0) {
					final String	key = item.substring(0,index), value = item.substring(index+1);  
					
					if (result.containsKey(key)) {
						final String[]	content = result.get(key), newContent = Arrays.copyOf(content,content.length+1);
						
						newContent[newContent.length-1] = value;
						result.put(key,newContent);
					}
					else {
						result.put(key,new String[]{value});
					}
				}
				else {
					throw new IllegalArgumentException("Query item ["+item+"] doesn't contain equals sign");
				}
			}
			return result;
		}
	}
	
	/**
	 * <p>Build 'self' URI from content</p>
	 * @param content content to build 'self' URI for
	 * @return 'self' URI built
	 * @throws NullPointerException when content is null
	 * @see chav1961.purelib.new.self.Handler
	 * @since 0.0.3
	 */
	public static URI convert2selfURI(final byte[] content) throws NullPointerException {
		if (content == null) {
			throw new NullPointerException("Content for URI can't be null");
		}
		else {
			return URI.create("self:/#"+new String(Base64.getEncoder().encode(content)));
		}
	}
	
	/**
	 * <p>Build 'self' URI from content</p>
	 * @param content content to build 'self' URI for
	 * @param charSet character set for the content to use 
	 * @return 'self' URI built with '?encoding=ZZZ' query string
	 * @throws NullPointerException when content is null
	 * @throws IllegalArgumentException when encoding is null, empty or unknown
	 * @see chav1961.purelib.new.self.Handler
	 * @since 0.0.3
	 */
	public static URI convert2selfURI(final char[] content, final String charSet) throws NullPointerException, IllegalArgumentException {
		if (content == null) {
			throw new NullPointerException("Content for URI can't be null");
		}
		else if (charSet == null || charSet.isEmpty()) {
			throw new IllegalArgumentException("Char set for for URI can't be null or empty");
		}
		else {
			try {
				return URI.create("self:/#"+new String(Base64.getEncoder().encode(new String(content).getBytes(charSet)))+"?encoding="+charSet);
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException(e.getLocalizedMessage(),e);
			}
		}
	}
}
