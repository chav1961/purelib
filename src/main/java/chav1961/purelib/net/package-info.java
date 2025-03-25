/**
 * <p>This package contains implementation for all the special URLs in the Pure Library. 
 * It consists a set of sub-packages for all the URLs imp lemented. URLs implemented are:</p>
 * <ul>
 * <li><b>{@linkplain chav1961.purelib.net.capture.Handler capture}</b> URL - URL for capture microphone audio stream.</li> 
 * <li><b>{@linkplain chav1961.purelib.net.fsys.Handler fsys}</b> URL - URL for read-only access to any file systems in Pure Library.</li> 
 * <li><b>{@linkplain chav1961.purelib.net.namingrepo.Handler naming repository}</b> URL - URL for getting access to naming service</li>
 * <li><b>{@linkplain chav1961.purelib.net.playback.Handler playback}</b> URL - URL for playing background audio stream</li>
 * <li><b>{@linkplain chav1961.purelib.net.root.Handler root}</b> URL - URL for read-only access to any resources inside your application. It's functionality is similar to
 * {@linkplain Class#getResource(String)} method, but this URL can refers to resource statically.</li> 
 * <li><b>{@linkplain chav1961.purelib.net.self.Handler self}</b> URL - URL, containing data directly inside it in Base64-encoded form. This URL is especially useful for debugging purposes</li> 
 * </ul> 
 * <p>This package also contains a set of useful classes to support network functionality:</p>
 * <ul>
 * <li>{@linkplain chav1961.purelib.net.AbstractDiscovery AbstractDiscovery} class as template to implement discovery services in applications</li>
 * <li>{@linkplain chav1961.purelib.net.AbstractSelectorBasedDispatcher AbstractSelectorBasedDispatcher} class as template to implement selector-based network router</li>
 * </ul>
 * 
 * @see chav1961.purelib.fsys
 * @see chav1961.purelib.basic.URIUtils
 * @see chav1961.purelib.basic.URIUtils#convert2selfURI(byte[])
 * @see chav1961.purelib.basic.URIUtils#convert2selfURI(char[], String)
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @last.update 0.0.8
 */
package chav1961.purelib.net;