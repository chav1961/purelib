package chav1961.purelib.nanoservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class NanoServiceHttpsTest {

	private final File			serverStore = new File(System.getProperty("java.io.tmpdir"),"server.jks"); 
	private final File			clientStore = new File(System.getProperty("java.io.tmpdir"),"client.jks"); 
	private NanoServiceFactory	factory = null;
	private int					currentPort;
	
	@Before 
	public void prepare() throws Exception {
		final KeyStore 		ks = KeyStore.getInstance("JKS");
		final char[] 		pwdArray = "test".toCharArray();
		final PrivateKey	pk = InternalUtils.buildRSAPrivateKey(NanoServiceHttpsTest.class.getResourceAsStream("domain8.pem"));
		
		ks.load(null, pwdArray);
		final Certificate[]	chain = new Certificate[]{
								InternalUtils.buildX509Certificate(NanoServiceHttpsTest.class.getResourceAsStream("domain.crt"))
							};
		ks.setKeyEntry("test",pk,pwdArray,chain);		
		ks.setCertificateEntry("testCert",InternalUtils.buildX509Certificate(NanoServiceHttpsTest.class.getResourceAsStream("domain.crt")));		

		try(final FileOutputStream 	fos = new FileOutputStream(serverStore)) {
			ks.store(fos, pwdArray);
			fos.flush();
		}

		final KeyStore 		ts = KeyStore.getInstance("JKS");
		
		ts.load(null, pwdArray);
		ts.setCertificateEntry("testCert",InternalUtils.buildX509Certificate(NanoServiceHttpsTest.class.getResourceAsStream("domain.crt")));		

		try(final FileOutputStream 	fos = new FileOutputStream(clientStore)) {
			ts.store(fos, pwdArray);
			fos.flush();
		}
		
		currentPort = (int) (1024 + 60000 * Math.random());
		factory = new NanoServiceFactory(new SystemErrLoggerFacade(), new SubstitutableProperties(Utils.mkProps(
												NanoServiceFactory.NANOSERVICE_PORT, ""+currentPort,
												NanoServiceFactory.NANOSERVICE_ROOT, FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./src/test/resources/chav1961/purelib/nanoservice/root/",
												NanoServiceFactory.NANOSERVICE_USE_SSL, "true",
												NanoServiceFactory.NANOSERVICE_DISABLE_LOOPBACK, "false",
												NanoServiceFactory.NANOSERVICE_USE_KEYSTORE, "true",
												NanoServiceFactory.NANOSERVICE_SSL_KEYSTORE, serverStore.getAbsolutePath(),
												NanoServiceFactory.NANOSERVICE_SSL_KEYSTORE_PASSWD, "test",
												NanoServiceFactory.NANOSERVICE_USE_TRUSTSTORE, "true",
												NanoServiceFactory.NANOSERVICE_SSL_TRUSTSTORE, serverStore.getAbsolutePath(),
												NanoServiceFactory.NANOSERVICE_SSL_TRUSTSTORE_PASSWD, "test"
											)));
	}

	@After
	public void unprepare() throws Exception {
		if (factory != null) {
			factory.close();
		}
		clientStore.delete();
		serverStore.delete();
	}

	@Test
	public void test() throws IOException, ContentException, InterruptedException {
		try{factory.start();
			final URL					url = new URL("https://localhost:"+currentPort+"/loopback");
			final HttpsURLConnection	conn = (HttpsURLConnection)url.openConnection();
			final SSLSocketFactory		factory = InternalUtils.buildClientSSLSocketFactory(NanoServiceHttpsTest.class.getResourceAsStream("domain.crt")); 
			
			conn.setSSLSocketFactory(factory);
			System.setProperty("javax.net.debug","all");
			System.out.println("ASAS");
			try(final InputStream	is = conn.getInputStream()) {
				
				Utils.copyStream(is,new OutputStream() {
					@Override public void write(int b) throws IOException {}
				});
			}
		} finally {
			factory.stop();
		}
	}
}
