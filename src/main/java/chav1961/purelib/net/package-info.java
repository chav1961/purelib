/**
 * <p>This package contains implementation for all the special URLs in the Pure Library. 
 * It consists a set of sub-packages for all the URLs implemented. URLs implemented are:</p>
 * <ul>
 * <li><b>{@linkplain chav1961.purelib.net.fsys.Handler fsys}</b> URL - URL for read-only access to any file systems in Pure Library.</li> 
 * <li><b>{@linkplain chav1961.purelib.net.root.Handler root}</b> URL - URL for read-only access to any resources inside your application. It's functionality is similar to
 * {@linkplain Class#getResource(String)} method, but this URS can refers to resource statically.</li> 
 * <li><b>{@linkplain chav1961.purelib.net.self.Handler self}</b> URL - URL, containing data directly inside it in Base64-encoded form. This URL is especially useful for debugging purposes</li> 
 * </ul> 
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @see chav1961.purelib.fsys
 * @see chav1961.purelib.basic.URIUtils
 * @see chav1961.purelib.basic.URIUtils#convert2selfURI(byte[])
 * @see chav1961.purelib.basic.URIUtils#convert2selfURI(char[], String)
 * @since 0.0.4
 */
package chav1961.purelib.net;