package chav1961.purelib.nanoservice;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

@Tag("OrdinalTestCategory")
public class HttpsNanoServiceFactoryTest {
	private SubstitutableProperties	props;
	private NanoServiceFactory		factory;
	
	@Before 
	public void prepare() throws Exception {
		props = new SubstitutableProperties(Utils.mkProps(
				NanoServiceFactory.NANOSERVICE_PORT, "0",
				NanoServiceFactory.NANOSERVICE_ROOT, FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./src/test/resources/chav1961/purelib/nanoservice/root/",
				NanoServiceFactory.NANOSERVICE_SSL_KEYSTORE, "./src/test/resources/chav1961/purelib/nanoservice/keystore.jks", 
				NanoServiceFactory.NANOSERVICE_USE_SSL, "true", 
				NanoServiceFactory.NANOSERVICE_USE_KEYSTORE, "true",				
				NanoServiceFactory.NANOSERVICE_SSL_KEYSTORE_TYPE, "jks", 
				NanoServiceFactory.NANOSERVICE_SSL_KEYSTORE_PASSWD, "test01", 
				NanoServiceFactory.NANOSERVICE_USE_TRUSTSTORE, "true",				
				NanoServiceFactory.NANOSERVICE_SSL_TRUSTSTORE, "./src/test/resources/chav1961/purelib/nanoservice/cacerts.jks", 
				NanoServiceFactory.NANOSERVICE_SSL_TRUSTSTORE_TYPE, "jks", 
				NanoServiceFactory.NANOSERVICE_SSL_TRUSTSTORE_PASSWD, "test01", 
				NanoServiceFactory.NANOSERVICE_DISABLE_LOOPBACK, "false")
			);
		factory = new NanoServiceFactory(new SystemErrLoggerFacade(), props);
	}

	@After
	public void unprepare() throws Exception {
		factory.close();
	}

	@Test
	public void connectionTest() throws IOException, KeyManagementException, NoSuchAlgorithmException {
//		try{factory.start();
//			final URL					url = new URL("https://localhost:"+currentPort+"/test.txt"); 
//			final HttpsURLConnection	conn = (HttpsURLConnection) url.openConnection();
//			final SSLContext 			sslContext = NanoServiceFactory.createSSLContext(props);
//			final HostnameVerifier 		hostnameVerifier = new HostnameVerifier() {
//								            public boolean verify(final String s, final SSLSession sslSession) {
//								                return s.equals(sslSession.getPeerHost());
//								            }
//								        };
//			conn.setHostnameVerifier(hostnameVerifier);
//		    conn.setSSLSocketFactory(sslContext.getSocketFactory());			
//			
//			conn.setDefaultUseCaches(false);
//			conn.setUseCaches(false);
//			conn.setDoInput(true);
//			conn.setDoOutput(false);
//			conn.setRequestMethod("GET");
//			conn.setRequestProperty(NanoServiceFactory.HEAD_ACCEPT,PureLibSettings.MIME_PLAIN_TEXT.toString());
//			conn.setRequestProperty(NanoServiceFactory.HEAD_ACCEPT_ENCODING,NanoServiceFactory.HEAD_CONTENT_ENCODING_IDENTITY);
//			try(final InputStream 		is = conn.getInputStream();
//				final Reader			rdr = new InputStreamReader(is);
//				final BufferedReader	brdr = new BufferedReader(rdr)) {
//				
//				Assert.assertEquals(conn.getContentType(),PureLibSettings.MIME_PLAIN_TEXT.toString());
//				Assert.assertEquals(conn.getContentEncoding(),NanoServiceFactory.HEAD_CONTENT_ENCODING_IDENTITY);
//				Assert.assertEquals("test string",brdr.readLine());
//			}
//			conn.disconnect();
//		} finally {
//			factory.stop();
//		}
	}
}
