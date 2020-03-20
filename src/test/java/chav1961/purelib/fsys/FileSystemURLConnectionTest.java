package chav1961.purelib.fsys;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class FileSystemURLConnectionTest {
	@Before
	public void prepare() {
		new File("./src/test/resources/chav1961/purelib/fsys/content.txt").delete();
	}

	@After
	public void unprepare() {
		new File("./src/test/resources/chav1961/purelib/fsys/content.txt").delete();
	}
	
	@Test
	public void fileAccessTest() throws IOException {
		final URL			url = new URL(null,FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./src/test/resources/chav1961/purelib/fsys#content.txt",new FileSystemURLStreamHandler());
		final URLConnection	connWrite = url.openConnection();
		
		connWrite.setDoOutput(true);
		connWrite.connect();
		try(final OutputStream	os = connWrite.getOutputStream()) {
			os.write("test string".getBytes());
			os.flush();
		}
		
		Assert.assertTrue(new File("./src/test/resources/chav1961/purelib/fsys/content.txt").exists());
		
		final URLConnection	connRead = url.openConnection();
		
		connRead.setDoInput(true);
		connRead.connect();
		try(final InputStream	is = connRead.getInputStream()) {
			final byte[]	buffer = new byte[100];
			final int		len = is.read(buffer);

			Assert.assertEquals(new String(buffer,0,len),"test string");
		}
	}

	@Test
	public void directoryAccessTest() throws IOException {
		final URL			url = new URL(null,FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./src/test/resources/chav1961/purelib/fsys#/",new FileSystemURLStreamHandler());
		final URLConnection	connRead = url.openConnection();
		final String[]		dirContent;
		
		connRead.setDoInput(true);
		connRead.connect();
		try(final InputStream	is = connRead.getInputStream()) {
			final byte[]	buffer = new byte[1024];
			final int		len = is.read(buffer);

			dirContent = new String(buffer,0,len).split("\\n");
		}
		Assert.assertTrue(dirContent.length > 0);
	}

	@Test
	public void exceptionsTest() throws IOException {
		try{new URL(null,"unknown::unknown:any#/",new FileSystemURLStreamHandler()).openConnection();
			Assert.fail("Mandatory exception was not detected (unknown URI schema for the file systems)");
		} catch (IOException exc) {
		}

		try{new URL(null,FileSystemInterface.FILESYSTEM_URI_SCHEME+":unknown:any#/",new FileSystemURLStreamHandler()).openConnection().connect();
			Assert.fail("Mandatory exception was not detected (unknown URI schema for the file system type)");
		} catch (IOException exc) {
		}
		
		final URL	url = new URL(null,FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./src/test/resources/chav1961/purelib/fsys#/",new FileSystemURLStreamHandler());

		try{final URLConnection	conn = url.openConnection();

			conn.setDoOutput(true);
			conn.getOutputStream();
			Assert.fail("Mandatory exception was not detected (calling getOutputStream() before call connect())");
		} catch (IllegalStateException exc) {
		}
		
		try{final URLConnection	conn = url.openConnection();

			conn.setDoInput(false);
			conn.connect();
			Assert.fail("Mandatory exception was not detected (neither input nor output was selected)");
		} catch (IOException exc) {
		}

		try{final URLConnection	conn = url.openConnection();

			conn.setDoOutput(true);
			conn.connect();
			Assert.fail("Mandatory exception was not detected (directory can't be writable)");
		} catch (IOException exc) {
		}

		try{final URLConnection	conn = url.openConnection();

			conn.connect();
			conn.getInputStream().close();
			conn.getInputStream();
			Assert.fail("Mandatory exception was not detected (calling getInputStream() twice)");
		} catch (IllegalStateException exc) {
		}
		
	//	final URL	urlF = new URL(null,FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/src/test/resources/chav1961/purelib/fsys#content.txt",new FileSystemURLStreamHandler());

		try{final URLConnection	conn = url.openConnection();

			conn.setDoOutput(true);
			conn.connect();
			conn.getOutputStream().close();
			conn.getOutputStream();
			Assert.fail("Mandatory exception was not detected (calling getOutputStream() twice)");
		} catch (IOException exc) {
		}
	}
}
