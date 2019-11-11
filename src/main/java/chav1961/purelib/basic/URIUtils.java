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
import java.net.URLConnection;
import java.util.Base64;

import chav1961.purelib.fsys.FileSystemURLStreamHandler;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

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
		else if (uri.getScheme().equals(FileSystemInterface.FILESYSTEM_URI_SCHEME)) {
			final URL			url = new URL(null,uri.getRawSchemeSpecificPart(),new FileSystemURLStreamHandler());
			final URLConnection	conn = url.openConnection();
			
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
				final InputStream			is = conn.getInputStream()) {
				
				Utils.copyStream(is,baos);
				return baos.toByteArray();
			}
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
		else if (FileSystemInterface.FILESYSTEM_URI_SCHEME.equals(uri.getScheme())) {
			final URL			url = new URL(null,uri.getRawSchemeSpecificPart(),new FileSystemURLStreamHandler());
			final URLConnection	conn = url.openConnection();
			
			try(final Writer		wr = new CharArrayWriter();
				final InputStream	is = conn.getInputStream();
				final Reader		rdr = new InputStreamReader(is)) {
				
				Utils.copyStream(rdr,wr);
				return wr.toString().toCharArray();
			}
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
		else if (uri.getScheme().equals(FileSystemInterface.FILESYSTEM_URI_SCHEME)) {
			final URL			url = new URL(null,uri.getRawSchemeSpecificPart(),new FileSystemURLStreamHandler());
			final URLConnection	conn = url.openConnection();
			
			try(final Writer		wr = new CharArrayWriter();
				final InputStream	is = conn.getInputStream();
				final Reader		rdr = new InputStreamReader(is,encoding)) {
				
				Utils.copyStream(rdr,wr);
				return wr.toString().toCharArray();
			}
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
	 */
	public static URI appendRelativePath2URI(final URI uri, final String relativePath) throws NullPointerException, IllegalArgumentException {
		if (uri == null) {
			throw new NullPointerException("Uri to append path to can't be null");
		}
		else if (relativePath == null || relativePath.isEmpty()) {
			throw new IllegalArgumentException("Uri to append path to can't be null");
		}
		else {
			final String	newPath = URI.create(uri.getPath()+(relativePath.charAt(0) == '/' ? "" : "/")+relativePath).normalize().toString();
			String			temp = uri.resolve(newPath).toString();
			
			if (uri.getRawQuery() != null) {
				temp += '?' + uri.getRawQuery();
			}
			if (uri.getRawFragment() != null) {
				temp += '#' + uri.getRawFragment();
			}
			return URI.create(temp);
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
	 * @throws NullPointerException
	 * @since 0.0.3
	 */
	public static String extractQueryFromURI(final URI uri) throws NullPointerException {
		URI		current = uri; 
		String	query = null, previous = uri.toString();
		
		while ((query = current.getQuery()) == null) {
			final String	ssp = current.getSchemeSpecificPart();
			
			if (ssp == null || previous.equals(ssp)) {
				break;
			}
			else {
				previous = ssp;
				current = URI.create(ssp);
			}
		}
		return query;
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
		else if (scheme == null) {
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
	
	public static URI convert2selfURI(final byte[] content) {
		return URI.create("self:/#"+new String(Base64.getEncoder().encode(content)));
	}
	
	public static URI convert2selfURI(final char[] content, final String charSet) throws UnsupportedEncodingException {
		return URI.create("self:/#"+new String(Base64.getEncoder().encode(new String(content).getBytes(charSet)))+"?encoding="+charSet);
//		 CharBuffer charBuffer = CharBuffer.wrap(chars);
//		  ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
//		  byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
//		            byteBuffer.position(), byteBuffer.limit());		
	}

	
}