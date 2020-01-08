package chav1961.purelib.nanoservice;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.nanoservice.NanoServiceFactory.MethodDescriptor;
import chav1961.purelib.nanoservice.NanoServiceFactory.PathParser;
import chav1961.purelib.nanoservice.NanoServiceFactory.QueryParser;
import chav1961.purelib.nanoservice.NanoServiceFactory.RequestHeadParser;
import chav1961.purelib.nanoservice.NanoServiceFactory.ResponseHeadSetter;
import chav1961.purelib.nanoservice.interfaces.MethodExecutor;
import chav1961.purelib.nanoservice.interfaces.QueryType;
import chav1961.purelib.streams.byte2byte.ZLibInputStream;

@SuppressWarnings("restriction")
public class NanoServiceFactoryTest {
	private static final String			LOOPBACK_RESPONSE = 
											"<html>\n"+
											"<head></head>\n"+
											"<body>\n"+
											"<h3>Loopback info:<h3>\n"+
											"<p>Request method = GET</p>\n"+
											"<p>Path = /loopback, fragment = <missing></p>\n"+
											"<p>Query string = <missing></p>\n"+
											"<p>Request headers:</p>\n"+
											"<table>\n"+
											"<tr><th>Header key</th><th>Value</th></tr>\n"+
											"<tr><td>Accept</td><td>[text/html]</td></tr>\n"+
											"</table>\n"+
											"<p>Response headers:</p>\n"+
											"<table>\n"+
											"<tr><th>Header key</th><th>Value</th></tr>\n"+
											"<tr><td>Content-type</td><td>[text/html]</td></tr>\n"+
											"</table>\n"+
											"</body>\n"+
											"</html>";
	private static final TestProbe[]	TO_BODY_SET = new TestProbe[]{
											// ---------- text/plain
											new TestProbe("http://localhost:1000/pseudo/get/body/OutputStream", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/CreoleWriter", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "test string\n"),
											new TestProbe("http://localhost:1000/pseudo/get/body/Writer", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/CharTarget", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/StringBuilder", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "test string"),
											// ---------- text/html
											new TestProbe("http://localhost:1000/pseudo/get/body/OutputStream", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/html", NanoServiceFactory.HEAD_ACCEPT, "text/html"), 200, "html test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/CreoleWriter", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/html", NanoServiceFactory.HEAD_ACCEPT, "text/html"), 200, "<html><head></head><body><div class=\"cwr\"><p>html test string\n</p></div></body></html>"),
											new TestProbe("http://localhost:1000/pseudo/get/body/Writer", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/html", NanoServiceFactory.HEAD_ACCEPT, "text/html"), 200, "html test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/CharTarget", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/html", NanoServiceFactory.HEAD_ACCEPT, "text/html"), 200, "html test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/StringBuilder", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/html", NanoServiceFactory.HEAD_ACCEPT, "text/html"), 200, "html test string"),
											// ---------- text/xml
											new TestProbe("http://localhost:1000/pseudo/get/body/OutputStream", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "xml test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/CreoleWriter", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "<?xml version=\"1.0\"?><cre:root xmlns:cre=\"http://www.wikicreole.org/\"><cre:div><cre:p>xml test string\n</cre:p></cre:div></cre:root>"),
											new TestProbe("http://localhost:1000/pseudo/get/body/XMLStreamWriter", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "<?xml version=\"1.0\" ?><a b=\"xml test string\">xml test string</a>"),
											new TestProbe("http://localhost:1000/pseudo/get/body/Document", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<Test xmlns=\"https://www.test\">\n<Content>xml test string</Content>\n</Test>\n"),
											new TestProbe("http://localhost:1000/pseudo/get/body/Writer", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "xml test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/CharTarget", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "xml test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/StringBuilder", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "xml test string"),
											// ---------- application/json
											new TestProbe("http://localhost:1000/pseudo/get/body/OutputStream", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "json test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/Writer", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "json test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/CharTarget", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "json test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/StringBuilder", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "json test string"),
											new TestProbe("http://localhost:1000/pseudo/get/body/JsonStaxPrinter", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "\"json test string\""),
											new TestProbe("http://localhost:1000/pseudo/get/body/JsonSerializer", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "{\"content\":\"json test string\",\"message\":null}"),
											// ---------- application/octet-stream
											new TestProbe("http://localhost:1000/pseudo/get/body/OutputStream", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/octet-stream", NanoServiceFactory.HEAD_ACCEPT, "application/octet-stream"), 200, "octet test string"),
											}; 	
	private static final TestProbe[]	FROM_PATH_SET = new TestProbe[]{
											new TestProbe("http://localhost:1000/pseudo/get/path/value/parm2/parm3", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "value"),
											new TestProbe("http://localhost:1000/pseudo/get/path/parm1/value/parm3", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "value"),
											new TestProbe("http://localhost:1000/pseudo/get/path/parm1/parm2/value", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "value"),
											new TestProbe("http://localhost:1000/pseudo/get/path/parm1/parm2/parm3/parm4", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 404, null),
											new TestProbe("http://localhost:1000/pseudo/get/path/parm1/parm2", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 404, null),
											new TestProbe("http://localhost:1000/pseudo/get/path1/parm1", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "asterisk parm1"),
											};
	private static final TestProbe[]	FROM_QUERY_SET = new TestProbe[]{
											new TestProbe("http://localhost:1000/pseudo/get/query", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "NULLNULLNULL"),
											new TestProbe("http://localhost:1000/pseudo/get/query?parm1=value1", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "value1NULLNULL"),
											new TestProbe("http://localhost:1000/pseudo/get/query?parm2=value2&parm3=value3", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "NULLvalue2value3"),
											};
	private static final TestProbe[]	FROM_HEADER_SET = new TestProbe[]{
											new TestProbe("http://localhost:1000/pseudo/get/header", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "NULLNULLNULL"),
											new TestProbe("http://localhost:1000/pseudo/get/header", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain", "parm1", "value1"), 200, "value1NULLNULL"),
											new TestProbe("http://localhost:1000/pseudo/get/header", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain", "parm2", "value2", "parm3", "value3"), 200, "NULLvalue2value3"),
											};
	private static final TestProbe[]	TO_HEADER_SET = new TestProbe[]{
											new TestProbe("http://localhost:1000/pseudo/get/responseheader", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, ""),
											};
	private static final TestProbe[]	FROM_BODY_SET = new TestProbe[]{
											// ---------- text/plain
											new TestProbe("http://localhost:1000/pseudo/post/body/InputStream", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "test string"),
											new TestProbe("http://localhost:1000/pseudo/post/body/Reader", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "test string"),
											new TestProbe("http://localhost:1000/pseudo/post/body/CharSource", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "test string"),
											new TestProbe("http://localhost:1000/pseudo/post/body/String", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/plain", NanoServiceFactory.HEAD_ACCEPT, "text/plain"), 200, "test string"),
											// ---------- text/xml
											new TestProbe("http://localhost:1000/pseudo/post/body/String", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "xml <?xml version=\"1.0\"?><cre:root xmlns:cre=\"http://www.wikicreole.org/\"><cre:div><cre:p>xml test string\n</cre:p></cre:div></cre:root>"),
											new TestProbe("http://localhost:1000/pseudo/post/body/CharSource", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "<?xml version=\"1.0\"?><cre:root xmlns:cre=\"http://www.wikicreole.org/\"><cre:div><cre:p>xml test string\n</cre:p></cre:div></cre:root>"),
											new TestProbe("http://localhost:1000/pseudo/post/body/Reader", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "<?xml version=\"1.0\"?><cre:root xmlns:cre=\"http://www.wikicreole.org/\"><cre:div><cre:p>xml test string\n</cre:p></cre:div></cre:root>"),
											new TestProbe("http://localhost:1000/pseudo/post/body/InputStream", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "<?xml version=\"1.0\"?><cre:root xmlns:cre=\"http://www.wikicreole.org/\"><cre:div><cre:p>xml test string\n</cre:p></cre:div></cre:root>"),
											new TestProbe("http://localhost:1000/pseudo/post/body/Document", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<cre:root xmlns:cre=\"http://www.wikicreole.org/\">\n<cre:div>\n<cre:p>\nxml test string\n</cre:p>\n</cre:div>\n</cre:root>\n"),
											new TestProbe("http://localhost:1000/pseudo/post/body/XMLStreamReader", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "text/xml", NanoServiceFactory.HEAD_ACCEPT, "text/xml"), 200, "<?xml version=\"1.0\"?><cre:root xmlns:cre=\"http://www.wikicreole.org/\"><cre:div><cre:p>xml test string\n</cre:p></cre:div></cre:root>"),
											// ---------- application/json
											new TestProbe("http://localhost:1000/pseudo/post/body/String", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "{\"content\":\"json test string\",\"message\":null}"),
											new TestProbe("http://localhost:1000/pseudo/post/body/CharSource", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "{\"content\":\"json test string\",\"message\":null}"),
											new TestProbe("http://localhost:1000/pseudo/post/body/Reader", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "{\"content\":\"json test string\",\"message\":null}"),
											new TestProbe("http://localhost:1000/pseudo/post/body/InputStream", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "{\"content\":\"json test string\",\"message\":null}"),
											new TestProbe("http://localhost:1000/pseudo/post/body/JsonStaxParser", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "{\"content\":\"json test string\",\"message\":null}"),
											new TestProbe("http://localhost:1000/pseudo/post/body/JsonSerializer", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/json", NanoServiceFactory.HEAD_ACCEPT, "application/json"), 200, "{\"content\":\"json test string\",\"message\":null}"),
											// ---------- application/octet-stream
											new TestProbe("http://localhost:1000/pseudo/post/body/InputStream", Utils.mkProps(NanoServiceFactory.HEAD_CONTENT_TYPE, "application/octet-stream", NanoServiceFactory.HEAD_ACCEPT, "application/octet-stream"), 200, "octet test string"),
											}; 	
	private static int			unique = 0;

	
	private NanoServiceFactory	factory;
	private int					currentPort;
	
	@Before 
	public void prepare() throws Exception {
		currentPort = (int) (1024 + 60000 * Math.random());
		factory = new NanoServiceFactory(new SystemErrLoggerFacade(), new SubstitutableProperties(Utils.mkProps(
												NanoServiceFactory.NANOSERVICE_PORT, ""+currentPort,
												NanoServiceFactory.NANOSERVICE_ROOT, FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./src/test/resources/chav1961/purelib/nanoservice/root/",
												NanoServiceFactory.NANOSERVICE_SSL_KEYSTORE, "./src/test/resources/chav1961/purelib/nanoservice/keystore.jks", 
												NanoServiceFactory.NANOSERVICE_SSL_KEYSTORE_TYPE, "jks", 
												NanoServiceFactory.NANOSERVICE_SSL_KEYSTORE_PASSWD, "test01", 
												NanoServiceFactory.NANOSERVICE_SSL_TRUSTSTORE, "./src/test/resources/chav1961/purelib/nanoservice/cacerts.jks", 
												NanoServiceFactory.NANOSERVICE_SSL_TRUSTSTORE_TYPE, "jks", 
												NanoServiceFactory.NANOSERVICE_SSL_TRUSTSTORE_PASSWD, "test01", 
												NanoServiceFactory.NANOSERVICE_DISABLE_LOOPBACK, "false")
											));
	}

	@After
	public void unprepare() throws Exception {
		factory.close();
	}

	@SuppressWarnings("resource")
	@Test
	public void constructorTest() throws IOException, SyntaxException, ContentException {
		try {new NanoServiceFactory(null, new SubstitutableProperties(Utils.mkProps(
				NanoServiceFactory.NANOSERVICE_PORT, ""+currentPort,
				NanoServiceFactory.NANOSERVICE_ROOT, FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./src/test/resources/chav1961/purelib/nanoservice/root/")
			));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new NanoServiceFactory(new SystemErrLoggerFacade(), null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new NanoServiceFactory(new SystemErrLoggerFacade(), new SubstitutableProperties(Utils.mkProps(
				NanoServiceFactory.NANOSERVICE_ROOT, FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./src/test/resources/chav1961/purelib/nanoservice/root/")
			));
			Assert.fail("Mandatory exception was not detected (mandatory nanoservice port is missing)");
		} catch (IllegalArgumentException exc) {
		}
		try {new NanoServiceFactory(new SystemErrLoggerFacade(), new SubstitutableProperties(Utils.mkProps(
				NanoServiceFactory.NANOSERVICE_PORT, ""+currentPort)
			));
			Assert.fail("Mandatory exception was not detected (mandatory nanoservice root is missing)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	
	@Test
	public void lifeCycleTest() throws IOException {
		factory.start();
		Assert.assertTrue(factory.isStarted());
		Assert.assertFalse(factory.isSuspended());

		try{factory.start();
			Assert.fail("Mandatory exception was not detetced (server already started)");
		} catch (IllegalStateException exc) {
		}
		
		factory.suspend();
		Assert.assertTrue(factory.isStarted());
		Assert.assertTrue(factory.isSuspended());

		try{factory.suspend();
			Assert.fail("Mandatory exception was not detetced (server already suspended)");
		} catch (IllegalStateException exc) {
		}

		factory.resume();
		Assert.assertTrue(factory.isStarted());
		Assert.assertFalse(factory.isSuspended());

		try{factory.resume();
			Assert.fail("Mandatory exception was not detetced (server already resumed)");
		} catch (IllegalStateException exc) {
		}

		factory.stop();
		Assert.assertFalse(factory.isStarted());
		Assert.assertFalse(factory.isSuspended());

		try{factory.stop();
			Assert.fail("Mandatory exception was not detetced (server already stopped)");
		} catch (IllegalStateException exc) {
		}

		try{factory.suspend();
			Assert.fail("Mandatory exception was not detetced (server stopped)");
		} catch (IllegalStateException exc) {
		}
		try{factory.resume();
			Assert.fail("Mandatory exception was not detetced (server stopped)");
		} catch (IllegalStateException exc) {
		}
	}

	@Test
	public void methodDescriptorTest() throws IOException {
		final MethodExecutor	executor = new MethodExecutor() {
									@Override
									public int execute(final QueryType type, final char[] path, final char[] query, final Headers requestHeaders, final Headers responseHeaders, final InputStream is, final OutputStream os) throws IOException, ContentException, FlowException, EnvironmentException {
										return 0;
									}
								};
		final MethodDescriptor	desc1 = new MethodDescriptor("first/*",new QueryType[]{QueryType.GET},new MimeType[]{},new MimeType[]{PureLibSettings.MIME_PLAIN_TEXT},executor);
		final MethodDescriptor	desc2 = new MethodDescriptor("first/{value}/second",new QueryType[]{QueryType.GET},new MimeType[]{PureLibSettings.MIME_PLAIN_TEXT},new MimeType[]{PureLibSettings.MIME_PLAIN_TEXT},executor);
		
		Assert.assertTrue(desc1.canServeQuery(QueryType.GET));
		Assert.assertFalse(desc1.canServeQuery(QueryType.POST));

		Assert.assertFalse(desc1.canServePath("first".toCharArray(),0));
		Assert.assertTrue(desc1.canServePath("first/second".toCharArray(),0));
		
		Assert.assertTrue(desc2.canServePath("first/text/second".toCharArray(),0));
		Assert.assertFalse(desc2.canServePath("first/second".toCharArray(),0));

		Assert.assertTrue(desc1.isCompatibleByMimeTypes(null,PureLibSettings.MIME_PLAIN_TEXT));
		Assert.assertTrue(desc1.isCompatibleByMimeTypes(PureLibSettings.MIME_PLAIN_TEXT,PureLibSettings.MIME_PLAIN_TEXT));
		
		Assert.assertFalse(desc2.isCompatibleByMimeTypes(null,PureLibSettings.MIME_PLAIN_TEXT));
		Assert.assertTrue(desc2.isCompatibleByMimeTypes(PureLibSettings.MIME_PLAIN_TEXT,PureLibSettings.MIME_PLAIN_TEXT));
		Assert.assertFalse(desc2.isCompatibleByMimeTypes(PureLibSettings.MIME_HTML_TEXT,PureLibSettings.MIME_PLAIN_TEXT));
		Assert.assertFalse(desc2.isCompatibleByMimeTypes(PureLibSettings.MIME_PLAIN_TEXT,PureLibSettings.MIME_HTML_TEXT));
	}	
	
	@Test
	public void outputTransportTest() throws IOException {
//		try{factory.start();
//		
//			final URL			url = new URL("http://localhost:"+currentPort+"/test.txt"); 
//			HttpURLConnection	conn = (HttpURLConnection) url.openConnection();
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
//	
//			conn = (HttpURLConnection) url.openConnection();
//			
//			conn.setDefaultUseCaches(false);
//			conn.setUseCaches(false);
//			conn.setDoInput(true);
//			conn.setDoOutput(false);
//			conn.setRequestMethod("GET");
//			conn.setRequestProperty(NanoServiceFactory.HEAD_ACCEPT,PureLibSettings.MIME_PLAIN_TEXT.toString());
//			conn.setRequestProperty(NanoServiceFactory.HEAD_ACCEPT_ENCODING,NanoServiceFactory.HEAD_CONTENT_ENCODING_GZIP);
//			try(final InputStream 		is = conn.getInputStream();
//				final GZIPInputStream	gzis = new GZIPInputStream(is); 
//				final Reader			rdr = new InputStreamReader(gzis);
//				final BufferedReader	brdr = new BufferedReader(rdr)) {
//				
//				Assert.assertEquals(conn.getContentType(),"text/plain");
//				Assert.assertEquals(conn.getContentEncoding(),"gzip");
//				Assert.assertEquals("test string",brdr.readLine());
//			}
//			conn.disconnect();
//	
//			conn = (HttpURLConnection) url.openConnection();
//			
//			conn.setRequestMethod("GET");
//			conn.setRequestProperty(NanoServiceFactory.HEAD_ACCEPT,PureLibSettings.MIME_PLAIN_TEXT.toString());
//			conn.setRequestProperty(NanoServiceFactory.HEAD_ACCEPT_ENCODING,NanoServiceFactory.HEAD_CONTENT_ENCODING_COMPRESS);
//			try(final InputStream 		is = conn.getInputStream();
//				final ZLibInputStream	zlis = new ZLibInputStream(is); 
//				final Reader			rdr = new InputStreamReader(zlis);
//				final BufferedReader	brdr = new BufferedReader(rdr)) {
//				
//				Assert.assertEquals(conn.getContentType(),PureLibSettings.MIME_PLAIN_TEXT.toString());
//				Assert.assertEquals(conn.getContentEncoding(),NanoServiceFactory.HEAD_CONTENT_ENCODING_COMPRESS);
//				Assert.assertEquals("test string",brdr.readLine());
//			}
//			conn.disconnect();
//		} finally {
//			factory.stop();
//		}
	}

	@Test
	public void pathParserTest() throws SyntaxException {
		final char[][]		target = new char[10][];
		final PathParser	pp1 = factory.buildPathParser(unique++,"path");
		
		Assert.assertEquals(0,pp1.parse("path".toCharArray(),target,0));
		Assert.assertEquals(-1,pp1.parse("pat".toCharArray(),target,0));
		Assert.assertEquals(-1,pp1.parse("path1".toCharArray(),target,0));

		final PathParser	pp2 = factory.buildPathParser(unique++,"path/path");
		
		Assert.assertEquals(0,pp2.parse("path/path".toCharArray(),target,0));
		Assert.assertEquals(-1,pp2.parse("pat/path".toCharArray(),target,0));
		Assert.assertEquals(-1,pp2.parse("path1/path".toCharArray(),target,0));
		Assert.assertEquals(-1,pp2.parse("path/pat".toCharArray(),target,0));
		Assert.assertEquals(-1,pp2.parse("path/path1".toCharArray(),target,0));

		final PathParser	pp3 = factory.buildPathParser(unique++,"{value}");
		
		Assert.assertEquals(1,pp3.parse("ten".toCharArray(),target,0));
		Assert.assertArrayEquals("ten".toCharArray(),target[0]);
		Assert.assertEquals(1,pp3.parse("".toCharArray(),target,0));
		Assert.assertArrayEquals("".toCharArray(),target[0]);
		Assert.assertEquals(-1,pp3.parse("ten/".toCharArray(),target,0));
		Assert.assertEquals(-1,pp3.parse("ten/path".toCharArray(),target,0));

		final PathParser	pp4 = factory.buildPathParser(unique++,"path/*");
		
		Assert.assertEquals(-1,pp4.parse("path".toCharArray(),target,0));
		Assert.assertEquals(1,pp4.parse("path/first".toCharArray(),target,0));
		Assert.assertArrayEquals("first".toCharArray(),target[0]);
		Assert.assertEquals(1,pp4.parse("path/first/second".toCharArray(),target,0));
		Assert.assertArrayEquals("first/second".toCharArray(),target[0]);

		final PathParser	pp5 = factory.buildPathParser(unique++,"path/{name}/path/*");
		
		Assert.assertEquals(-1,pp5.parse("path".toCharArray(),target,0));
		Assert.assertEquals(-1,pp5.parse("path/text/pat".toCharArray(),target,0));
		Assert.assertEquals(-1,pp5.parse("path/text/path".toCharArray(),target,0));
		Assert.assertEquals(2,pp5.parse("path/text/path/tail".toCharArray(),target,0));
		Assert.assertArrayEquals("text".toCharArray(),target[0]);
		Assert.assertArrayEquals("tail".toCharArray(),target[1]);

		try{factory.buildPathParser(unique++,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void queryParserTest() throws SyntaxException {
		char[][]			target;
		final QueryParser	qp1 = factory.buildQueryParser(unique++,new String[]{"parm1=","parm2=","parm3="});

		Assert.assertEquals(3,qp1.parse("".toCharArray(),target = new char[3][],0));
		Assert.assertEquals(3,qp1.parse("parm1=value1".toCharArray(),target,0));
		Assert.assertArrayEquals(new char[][]{"value1".toCharArray(),null,null},target);
		Assert.assertEquals(3,qp1.parse("parm2=value2".toCharArray(),target,0));
		Assert.assertArrayEquals(new char[][]{null,"value2".toCharArray(),null},target);
		Assert.assertEquals(3,qp1.parse("parm3=value3".toCharArray(),target,0));
		Assert.assertArrayEquals(new char[][]{null,null,"value3".toCharArray()},target);
		Assert.assertEquals(3,qp1.parse("parm2=value2&parm1=value1".toCharArray(),target,0));
		Assert.assertArrayEquals(new char[][]{"value1".toCharArray(),"value2".toCharArray(),null},target);
		Assert.assertEquals(3,qp1.parse("parm3=value3&parm1=value1".toCharArray(),target,0));
		Assert.assertArrayEquals(new char[][]{"value1".toCharArray(),null,"value3".toCharArray()},target);
		Assert.assertEquals(3,qp1.parse("parm3=value3&parm2=value2".toCharArray(),target,0));
		Assert.assertArrayEquals(new char[][]{null,"value2".toCharArray(),"value3".toCharArray()},target);
		Assert.assertEquals(3,qp1.parse("parm3=value3&parm4=value4".toCharArray(),target,0));
		Assert.assertArrayEquals(new char[][]{null,null,"value3".toCharArray()},target);
		Assert.assertEquals(3,qp1.parse("parm0=value0&parm3=value3".toCharArray(),target,0));
		Assert.assertArrayEquals(new char[][]{null,null,"value3".toCharArray()},target);

		try{factory.buildQueryParser(unique++,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{factory.buildQueryParser(unique++,new String[0]);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{factory.buildQueryParser(unique++,new String[]{null});
			Assert.fail("Mandatory exception was not detected (nulls inside 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{factory.buildQueryParser(unique++,new String[]{""});
			Assert.fail("Mandatory exception was not detected (empties inside 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{factory.buildQueryParser(unique++,new String[]{"a"});
			Assert.fail("Mandatory exception was not detected (too short item inside 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{factory.buildQueryParser(unique++,new String[]{"ab"});
			Assert.fail("Mandatory exception was not detected (item inside 2-nd argument doesn't end with the (=))");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void requestHeaderParserTest() throws SyntaxException {
		char[][]				target;
		final RequestHeadParser	rqp1 = factory.buildRequestHeadParser(unique++,new String[]{"parm1","parm2","parm3"});
		
		Assert.assertEquals(3,rqp1.parse(new Headers(){{put("parm1",Arrays.asList("value1"));}},target = new char[3][],0));
		Assert.assertArrayEquals(new char[][]{"value1".toCharArray(),null,null},target);
		Assert.assertEquals(3,rqp1.parse(new Headers(){{put("parm1",Arrays.asList("value1","value2"));}},target = new char[3][],0));
		Assert.assertArrayEquals(new char[][]{"value1\nvalue2".toCharArray(),null,null},target);
		Assert.assertEquals(3,rqp1.parse(new Headers(){{put("parm2",Arrays.asList("value2"));}},target = new char[3][],0));
		Assert.assertArrayEquals(new char[][]{null,"value2".toCharArray(),null},target);
		Assert.assertEquals(3,rqp1.parse(new Headers(){{put("parm3",Arrays.asList("value3"));}},target = new char[3][],0));
		Assert.assertArrayEquals(new char[][]{null,null,"value3".toCharArray()},target);
		Assert.assertEquals(3,rqp1.parse(new Headers(){{put("parm0",Arrays.asList("value0"));}},target = new char[3][],0));
		Assert.assertArrayEquals(new char[][]{null,null,null},target);

		try{factory.buildQueryParser(unique++,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument");
		} catch (IllegalArgumentException exc) {
		}
		try{factory.buildQueryParser(unique++,new String[0]);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument");
		} catch (IllegalArgumentException exc) {
		}
		try{factory.buildQueryParser(unique++,new String[]{null});
			Assert.fail("Mandatory exception was not detected (nulls inside 2-nd argument");
		} catch (IllegalArgumentException exc) {
		}
		try{factory.buildQueryParser(unique++,new String[]{""});
			Assert.fail("Mandatory exception was not detected (empties inside 2-nd argument");
		} catch (IllegalArgumentException exc) {
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void responseHeaderSetterTest() throws SyntaxException {
//		final ResponseHeadSetter 	rhs1 = factory.buildResponseHeadSetter(unique++,new String[]{"parm1","parm2",""},new Class[]{StringBuilder.class,List.class,ForJson.class});
//		final Object[]				content = new Object[3];
//		final Headers				headers = new Headers();
//		
//		rhs1.prepare(content);
//		Assert.assertTrue(content[0] instanceof StringBuilder);
//		Assert.assertTrue(content[1] instanceof List);
//		Assert.assertTrue(content[2] instanceof ForJson);
//		
//		((StringBuilder)content[0]).append("value1");
//		((List<String>)content[1]).add("value2");
//		((ForJson)content[2]).content = "value3";
//		
//		rhs1.commit(headers,content);
//		
//		Assert.assertEquals("value1",headers.getFirst("parm1"));
//		Assert.assertEquals("value2",headers.getFirst("parm2"));
//		Assert.assertEquals("value3",headers.getFirst("testHeader1"));
//		Assert.assertEquals("null",headers.getFirst("testHeader2"));
	}

	@Test
	public void deploymentTest() throws IOException, ContentException, SyntaxException {
//		final String	path = "/pseudo";
//		SimpleCaller	caller; 
//		
//		try{factory.start();
//			factory.deploy(path,new PseudoPlugin());
//
//			try{factory.deploy(path,new PseudoPlugin());
//				Assert.fail("Mandatory exception was not detected (duplicate deployment to the same address)");
//			} catch (IllegalArgumentException exc) {
//			}
//			
//			caller = new SimpleCaller(URI.create("http://localhost:1000/pseudo/test"), QueryType.GET
//									,Utils.mkProps("key","value"
//									,NanoServiceFactory.HEAD_CONTENT_TYPE,PureLibSettings.MIME_PLAIN_TEXT.toString()
//									,NanoServiceFactory.HEAD_ACCEPT,PureLibSettings.MIME_PLAIN_TEXT.toString()));
//			try(final OutputStream	os = new ByteArrayOutputStream()) {
//				
//				caller.setStreams(null,os);
//				factory.handle(caller);
//				Assert.assertEquals(200,caller.getResponseCode());
//			}
//
//			factory.suspend();
//			
//			caller = new SimpleCaller(URI.create("http://localhost:1000/pseudo/test"), QueryType.GET
//									,Utils.mkProps("key","value"
//									,NanoServiceFactory.HEAD_CONTENT_TYPE,PureLibSettings.MIME_PLAIN_TEXT.toString()
//									,NanoServiceFactory.HEAD_ACCEPT,PureLibSettings.MIME_PLAIN_TEXT.toString()));
//			try(final OutputStream	os = new ByteArrayOutputStream()) {
//				
//				caller.setStreams(null,os);
//				factory.handle(caller);
//				Assert.assertEquals(503,caller.getResponseCode());
//			}
//
//			factory.resume();
//
//			caller = new SimpleCaller(URI.create("http://localhost:1000/pseudo/test"), QueryType.GET
//									,Utils.mkProps("key","value"
//									,NanoServiceFactory.HEAD_CONTENT_TYPE,PureLibSettings.MIME_PLAIN_TEXT.toString()
//									,NanoServiceFactory.HEAD_ACCEPT,PureLibSettings.MIME_PLAIN_TEXT.toString()));
//			try(final OutputStream	os = new ByteArrayOutputStream()) {
//				
//				caller.setStreams(null,os);
//				factory.handle(caller);
//				Assert.assertEquals(200,caller.getResponseCode());
//			}
//			
//			factory.undeploy(path);
//			
//			caller = new SimpleCaller(URI.create("http://localhost:1000/pseudo/test"), QueryType.GET
//									,Utils.mkProps("key","value"
//									,NanoServiceFactory.HEAD_CONTENT_TYPE,PureLibSettings.MIME_PLAIN_TEXT.toString()
//									,NanoServiceFactory.HEAD_ACCEPT,PureLibSettings.MIME_PLAIN_TEXT.toString()));
//			try(final OutputStream	os = new ByteArrayOutputStream()) {
//				
//				caller.setStreams(null,os);
//				factory.handle(caller);
//				Assert.assertEquals(404,caller.getResponseCode());
//			}
//
//			factory.deploy(path,new PseudoPlugin());
//			caller = new SimpleCaller(URI.create("http://localhost:1000/pseudo/test"), QueryType.GET
//								,Utils.mkProps("key","value"
//								,NanoServiceFactory.HEAD_CONTENT_TYPE,PureLibSettings.MIME_PLAIN_TEXT.toString()
//								,NanoServiceFactory.HEAD_ACCEPT,PureLibSettings.MIME_PLAIN_TEXT.toString()));
//			try(final OutputStream	os = new ByteArrayOutputStream()) {
//			
//				caller.setStreams(null,os);
//				factory.handle(caller);
//				Assert.assertEquals(200,caller.getResponseCode());
//			}
//			factory.undeploy(path);
//			
//			try{factory.deploy(null,new PseudoPlugin());
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try{factory.deploy("",new PseudoPlugin());
//				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try{factory.deploy(path,null);
//				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
//			} catch (NullPointerException exc) {
//			}
//
//			try{factory.undeploy(null);
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try{factory.undeploy("");
//				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try{factory.undeploy("/unknown");
//				Assert.fail("Mandatory exception was not detected (path is not deployed yet)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try{factory.undeploy(path);
//				Assert.fail("Mandatory exception was not detected (path was undeployed earlier)");
//			} catch (IllegalArgumentException exc) {
//			}
//			
//		} finally {
//			factory.stop();
//		}
	}

	@Test
	public void getHeadDeleteMethodToBodyTest() throws IOException, ContentException, SyntaxException {
//		final String	path = "/pseudo";
//		
//		try{factory.start();
//			factory.deploy(path,new PseudoPlugin());
//
//			for (TestProbe entity : TO_BODY_SET) {
//				SimpleCaller	caller = new SimpleCaller(entity.uri, QueryType.GET, entity.headers);
//				
//				try(final ByteArrayOutputStream	os = new ByteArrayOutputStream()) {
//					caller.setStreams(null,os);
//					factory.handle(caller);
//					Assert.assertEquals(entity.rc,caller.getResponseCode());
//					Assert.assertEquals(entity.response,os.toString().replace("\r",""));
//				}
//
//				caller = new SimpleCaller(entity.uri, QueryType.HEAD, entity.headers);
//				
//				try(final ByteArrayOutputStream	os = new ByteArrayOutputStream()) {
//					caller.setStreams(null,os);
//					factory.handle(caller);
//					Assert.assertEquals(entity.rc,caller.getResponseCode());
//				}
//				
//				caller = new SimpleCaller(entity.uri, QueryType.DELETE, entity.headers);
//				
//				try(final ByteArrayOutputStream	os = new ByteArrayOutputStream()) {
//					caller.setStreams(null,os);
//					factory.handle(caller);
//					Assert.assertEquals(entity.rc,caller.getResponseCode());
//					Assert.assertEquals(entity.response,os.toString().replace("\r",""));
//				}
//			}
//		} finally {
//			factory.stop();
//		}
	}

	@Test
	public void getMethodFromPathTest() throws IOException, ContentException, SyntaxException {
//		final String	path = "/pseudo";
//		
//		try{factory.start();
//			factory.deploy(path,new PseudoPlugin());
//
//			for (TestProbe entity : FROM_PATH_SET) {
//				final SimpleCaller	caller = new SimpleCaller(entity.uri, QueryType.GET, entity.headers);
//				
//				try(final ByteArrayOutputStream	os = new ByteArrayOutputStream()) {
//					caller.setStreams(null,os);
//					factory.handle(caller);
//					System.err.println(entity);
//					Assert.assertEquals(entity.rc,caller.getResponseCode());
//					if (entity.response != null) {
//						Assert.assertEquals(entity.response,os.toString().replace("\r",""));
//					}
//				}
//			}
//		} finally {
//			factory.stop();
//		}
	}

	@Test
	public void getMethodFromQueryTest() throws IOException, ContentException, SyntaxException {
//		final String	path = "/pseudo";
//		
//		try{factory.start();
//			factory.deploy(path,new PseudoPlugin());
//
//			for (TestProbe entity : FROM_QUERY_SET) {
//				final SimpleCaller	caller = new SimpleCaller(entity.uri, QueryType.GET, entity.headers);
//				
//				try(final ByteArrayOutputStream	os = new ByteArrayOutputStream()) {
//					caller.setStreams(null,os);
//					factory.handle(caller);
//					Assert.assertEquals(entity.rc,caller.getResponseCode());
//					if (entity.response != null) {
//						Assert.assertEquals(entity.response,os.toString().replace("\r",""));
//					}
//				}
//			}
//		} finally {
//			factory.stop();
//		}
	}

	@Test
	public void getMethodFromHeaderTest() throws IOException, ContentException, SyntaxException {
//		final String	path = "/pseudo";
//		
//		try{factory.start();
//			factory.deploy(path,new PseudoPlugin());
//
//			for (TestProbe entity : FROM_HEADER_SET) {
//				final SimpleCaller	caller = new SimpleCaller(entity.uri, QueryType.GET, entity.headers);
//				
//				try(final ByteArrayOutputStream	os = new ByteArrayOutputStream()) {
//					caller.setStreams(null,os);
//					factory.handle(caller);
//					Assert.assertEquals(entity.rc,caller.getResponseCode());
//					if (entity.response != null) {
//						Assert.assertEquals(entity.response,os.toString().replace("\r",""));
//					}
//				}
//			}
//		} finally {
//			factory.stop();
//		}
	}

	@Test
	public void getMethodToHeaderTest() throws IOException, ContentException, SyntaxException {
//		final String	path = "/pseudo";
//		
//		try{factory.start();
//			factory.deploy(path,new PseudoPlugin());
//
//			for (TestProbe entity : TO_HEADER_SET) {
//				final SimpleCaller	caller = new SimpleCaller(entity.uri, QueryType.GET, entity.headers);
//				
//				try(final ByteArrayOutputStream	os = new ByteArrayOutputStream()) {
//					caller.setStreams(null,os);
//					factory.handle(caller);
//					Assert.assertEquals(entity.rc,caller.getResponseCode());
//					if (entity.response != null) {
//						Assert.assertEquals(entity.response,os.toString().replace("\r",""));
//					}
//				}
//			}
//		} finally {
//			factory.stop();
//		}
	}

	@Test
	public void postAndPutMethodFromBodyTest() throws IOException, ContentException, SyntaxException {
//		final String	path = "/pseudo";
//		
//		try{factory.start();
//			factory.deploy(path,new PseudoPlugin());
//
//			for (TestProbe entity : FROM_BODY_SET) {
//				SimpleCaller	caller = new SimpleCaller(entity.uri, QueryType.POST, entity.headers);
//				
//				try(final ByteArrayOutputStream	os = new ByteArrayOutputStream();
//					final InputStream			is = new ByteArrayInputStream(entity.response.getBytes())) {
//					
//					caller.setStreams(is,os);
//					factory.handle(caller);
//					System.err.println(entity);
//					Assert.assertEquals(entity.rc,caller.getResponseCode());
//					Assert.assertEquals(entity.response,os.toString().replace("\r",""));
//				}
//
//				caller = new SimpleCaller(entity.uri, QueryType.PUT, entity.headers);
//				
//				try(final ByteArrayOutputStream	os = new ByteArrayOutputStream();
//					final InputStream			is = new ByteArrayInputStream(entity.response.getBytes())) {
//					
//					caller.setStreams(is,os);
//					factory.handle(caller);
//					System.err.println(entity);
//					Assert.assertEquals(entity.rc,caller.getResponseCode());
//					Assert.assertEquals(entity.response,os.toString().replace("\r",""));
//				}
//			}
//		} finally {
//			factory.stop();
//		}
	}
	
	
	@Test
	public void loopBackTest() throws IOException {
		SimpleCaller	caller;
		
		try{factory.start();

			caller = new SimpleCaller(URI.create("http://localhost:1000/loopback"), QueryType.GET, Utils.mkProps(NanoServiceFactory.HEAD_ACCEPT,PureLibSettings.MIME_HTML_TEXT.toString()));
			try(final ByteArrayOutputStream	os = new ByteArrayOutputStream()) {
				
				caller.setStreams(null,os);
				factory.handle(caller);
				Assert.assertEquals(200,caller.getResponseCode());
				
				try(final InputStream 	is = new ByteArrayInputStream(os.toByteArray());
					final Reader		rdr = new InputStreamReader(is)) {
					final String		readed = Utils.fromResource(rdr);
					
					Assert.assertEquals(LOOPBACK_RESPONSE,readed.replace("\r",""));
					Assert.assertEquals(caller.getResponseHeaders().getFirst(NanoServiceFactory.HEAD_CONTENT_TYPE),PureLibSettings.MIME_HTML_TEXT.toString());
					Assert.assertEquals(caller.getResponseHeaders().getFirst(NanoServiceFactory.HEAD_CONTENT_ENCODING),NanoServiceFactory.HEAD_CONTENT_ENCODING_IDENTITY);
				}
			}
		} finally {
			factory.stop();
		}
	}

	private static class TestProbe {
		final URI			uri;
		final Properties	headers;
		final int			rc;
		final String		response;
		
		public TestProbe(final String uri, final Properties headers, final int rc, final String response) {
			this.uri = URI.create(uri);
			this.headers = headers;
			this.rc = rc;
			this.response = response;
		}

		@Override
		public String toString() {
			return "TestPr [uri=" + uri + ", headers=" + headers + ", rc=" + rc + ", response=" + response + "]";
		}
	}
}


@SuppressWarnings("restriction")
class SimpleCaller extends HttpExchange {
	private final URI					request;
	private final QueryType				type;
	private final Map<String,Object>	attrs = new HashMap<>();
	private final Headers				rqHeaders = new Headers(), respHeaders = new Headers(); 
	private int							code = 0;
	private InputStream					source;
	private OutputStream				target;
	
	SimpleCaller(final URI request, final QueryType type, final Properties content) {
		this.request = request;
		this.type = type;
		for (Entry<Object, Object> item : content.entrySet()) {
			rqHeaders.add(item.getKey().toString(), item.getValue().toString());
		}
	}

	@Override
	public void close() {
	}

	@Override
	public Object getAttribute(final String name) {
		return attrs.get(name);
	}

	@Override
	public HttpContext getHttpContext() {
		return new HttpContext(){
			final Map<String, Object>	empty = new HashMap<>();
			HttpHandler					handler = null;
			
			@Override
			public Map<String, Object> getAttributes() {
				return empty;
			}

			@Override
			public Authenticator getAuthenticator() {
				return new Authenticator(){
					@Override
					public Result authenticate(HttpExchange arg0) {
						return new Authenticator.Success(getPrincipal());
					}
				};
			}

			@Override
			public List<Filter> getFilters() {
				return new ArrayList<>();
			}

			@Override
			public HttpHandler getHandler() {
				return handler;
			}

			@Override
			public String getPath() {
				return request.getPath();
			}

			@Override
			public HttpServer getServer() {
				return null;
			}

			@Override
			public Authenticator setAuthenticator(final Authenticator arg0) {
				return getAuthenticator();
			}

			@Override
			public void setHandler(final HttpHandler handler) {
				this.handler = handler;
			}
		};
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return new InetSocketAddress(1000);
	}

	@Override
	public HttpPrincipal getPrincipal() {
		return new HttpPrincipal("user","realm");
	}

	@Override
	public String getProtocol() {
		return request.getScheme();
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return new InetSocketAddress("localhost",1000);
	}

	@Override
	public InputStream getRequestBody() {
		return source;
	}

	@Override
	public Headers getRequestHeaders() {
		return rqHeaders;
	}

	@Override
	public String getRequestMethod() {
		return type.toString();
	}

	@Override
	public URI getRequestURI() {
		return request;
	}

	@Override
	public OutputStream getResponseBody() {
		return target;
	}

	@Override
	public int getResponseCode() {
		return code;
	}

	@Override
	public Headers getResponseHeaders() {
		return respHeaders;
	}

	@Override
	public void sendResponseHeaders(final int retCode, final long size) throws IOException {
		code = retCode;
	}

	@Override
	public void setAttribute(final String name, final Object value) {
		attrs.put(name, value);
	}

	@Override
	public void setStreams(InputStream source, OutputStream target) {
		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return "SimpleCaller [request=" + request + ", type=" + type + ", attrs=" + attrs + ", rqHeaders=" + rqHeaders + ", respHeaders=" + respHeaders + ", code=" + code + ", source=" + source + ", target=" + target + "]";
	}
}
