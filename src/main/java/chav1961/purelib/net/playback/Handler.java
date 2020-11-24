package chav1961.purelib.net.playback;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * <p>This class is  handler to support "playback" schema URL. Format of playback URL is:</p>
 * <code><b>playback://</b>&lt;device&gt;?&lt;parameters&gt;</code>
 * <ul>
 * <li>device - play audio device or one of predefined logical device names</li>
 * <li>parameters - audio device parameters</li>
 * </ul>
 * <p>Predefined logical name of the device can be:</p>
 * <ul>
 * <li>audio - any audio device supports playing</li>
 * <li>speaker - any available speaker on your computer</li>
 * <li>headset - any available head set on your computer</li>
 * </ul>
 * <p>Parameters can be:</p>
 * <ul>
 * <li>rate=&lt;number&gt; - audio rate. Available values are 96000, 48000, 44100 (default), 22050, 16000, 11025, 8000</li>
 * <li>bits=&lt;number&gt; - number of bytes in one sample. Available values are 8, 16 (default), 24</li>
 * <li>signed=&lt;choise&gt; - is content signed. Available values are signed (default), unsigned</li>
 * <li>endian=&lt;choise&gt; - is content big or little endial. Available values are big (default), little</li>
 * <li>channels=&lt;number&gt; - number of channels in the stream. Available values are 1, 2 (default)</li>
 * <li>encoding=&lt;choise&gt; - stream encoding. Available values are pcm (default), ulaw, alaw, gsm</li>
 * </ul>
 * @see URLStreamHandler   
 * @see PlaybackHandlerProvider   
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public class Handler extends URLStreamHandler {
	public static final String	PROTOCOL = "playback";
	
	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		if (PROTOCOL.equals(url.getProtocol())) {
			return new PlaybackURLConnection(url);
		}
		else {
			throw new IOException("Illegal URL ["+url+"]: protocol ["+url.getProtocol()+"] is not supported"); 
		}
	}
}
