package chav1961.purelib.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.spi.URLStreamHandlerProvider;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.net.capture.CaptureHandlerProvider;
import chav1961.purelib.net.fsys.FSysHandlerProvider;
import chav1961.purelib.net.playback.PlaybackHandlerProvider;
import chav1961.purelib.net.root.RootHandlerProvider;
import chav1961.purelib.net.self.SelfHandlerProvider;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class URIsTest {
	@Test
	public void spiTest() {
		final Set<Class<URLStreamHandlerProvider>>	providers = new HashSet<>();
		
		for (URLStreamHandlerProvider item : ServiceLoader.load(URLStreamHandlerProvider.class)) {
			providers.add((Class<URLStreamHandlerProvider>)item.getClass());
		}
		Assert.assertEquals(5,providers.size());
		Assert.assertTrue(providers.contains(FSysHandlerProvider.class));		
		Assert.assertTrue(providers.contains(RootHandlerProvider.class));		
		Assert.assertTrue(providers.contains(SelfHandlerProvider.class));		
		Assert.assertTrue(providers.contains(PlaybackHandlerProvider.class));		
		Assert.assertTrue(providers.contains(CaptureHandlerProvider.class));		
	}

//	@Test
	public void audioTest() throws IOException, InterruptedException {
		final URL			url = new URL("playback://speaker");
		final URLConnection	conn = url.openConnection();
		
		conn.connect();
		try(final OutputStream		os = conn.getOutputStream();
			final DataOutputStream	dos = new DataOutputStream(os)) {
			
			for (int index = 0; index < 88200*10; index++) {
				dos.writeShort((short)(Short.MAX_VALUE * Math.sin(index/88.2)));
			}
			dos.flush();
			Thread.sleep(1000);
		}
		conn.getInputStream();
	}

//	@Test
	public void rawAudioTest() throws IOException, InterruptedException {
		final URL			urlIn = new URL("capture://microphone");
		final URLConnection	connIn = urlIn.openConnection();
		final byte[]		content = new byte[100000 * 4];
		int len;
		
		System.err.println("REad...");
		connIn.connect();
		try(final InputStream		is = connIn.getInputStream()) {
			
			len = is.read(content);
		}
		System.err.println("Write "+len+"...");
		
		try(final OutputStream	os = new FileOutputStream("e:/chav1961/sample.pcm")){
			os.write(content,0,len);
			os.flush();
		}
		
		
		final URL			urlOut = new URL("playback://speaker");
		final URLConnection	conn = urlOut.openConnection();
		
		conn.connect();
		try(final InputStream		is= new ByteArrayInputStream(content);
			final DataInputStream	dis = new DataInputStream(is);
			final OutputStream		os = conn.getOutputStream();
			final DataOutputStream	dos = new DataOutputStream(os)) {
			
			for (int index = 0; index < content.length/2; index++) {
				dos.writeShort(dis.readShort());
			}
			dos.flush();
			Thread.sleep(1000);
		}
		conn.getInputStream();
		System.err.println("Stop...");
	}

}
