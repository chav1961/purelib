package chav1961.purelib.nanoservice;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.sql.DataSource;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.TemporaryStore;
import chav1961.purelib.basic.TemporaryStore.InputOutputPair;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.MimeParseException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.nanoservice.interfaces.FromBody;
import chav1961.purelib.nanoservice.interfaces.FromHeader;
import chav1961.purelib.nanoservice.interfaces.FromPath;
import chav1961.purelib.nanoservice.interfaces.FromQuery;
import chav1961.purelib.nanoservice.interfaces.HeaderName;
import chav1961.purelib.nanoservice.interfaces.MethodExecutor;
import chav1961.purelib.nanoservice.interfaces.MultipartContent;
import chav1961.purelib.nanoservice.interfaces.NanoService;
import chav1961.purelib.nanoservice.interfaces.Path;
import chav1961.purelib.nanoservice.interfaces.QueryType;
import chav1961.purelib.nanoservice.interfaces.RootPath;
import chav1961.purelib.nanoservice.interfaces.ToBody;
import chav1961.purelib.nanoservice.interfaces.ToHeader;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.byte2byte.ZLibInputStream;
import chav1961.purelib.streams.byte2byte.ZLibOutputStream;
import chav1961.purelib.streams.char2byte.AsmWriter;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;

/**
 * <p>This class is an embedded light-weight WEB server to use in the Java applications. Ordinal, but not the only, use case for it is to support web-styled help
 * system for your application.</p>
 * <p>The most important features of the class are:</p>
 * <ul>
 * <li>support all the six HTLP requests (GET, POST, PUT, DELETE, HEADER and OPTIONS)</li>
 * <li>support static content based on {@linkplain FileSystemInterface} functionality</li>
 * <li>support of embedded Creole/HTML converter to use <b>Creole</b> content for your "site" directly</li>
 * <li>support light-weight <b>RESTFul-styled</b> services for your "site"</li>
 * <li>support <b>https</b> connections</li>
 * <li>support <b>loopback</b> functionality to check your "site" requests</li>
 * </ul>
 * <p>This class functionality is based on the {@linkplain com.sun.net.httpserver.HttpServer}. Web-server can be started, stopper, paused and resumed. With conjunction
 * to {@linkplain NanoServiceManager} it can make hot deployment of your RESTful services. To increase performance, every class deployed wraps with compiled stub 
 * (see {@linkplain AsmWriter}) and doesn't use reflections on service calls in many cases</p> 
 * <p>This class is not thread-safe.</p>
 * @see com.sun.net.httpserver.HttpServer
 * @see <a href="http://www.wikicreole.org/">Creole</a>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @last.update 0.0.6
 */

public class NanoServiceFactory implements Closeable, NanoService, HttpHandler   {
	public static final String 			NANOSERVICE_USE_BUILTIN_SERVER = "nanoserviceUseBuiltinServer";
	public static final String 			NANOSERVICE_PORT = "nanoservicePort";
	public static final String 			NANOSERVICE_ROOT = "nanoserviceRoot";
	public static final String 			NANOSERVICE_LOCALHOST_ONLY = "nanoserviceLocalhostOnly";
	public static final String 			DEFAULT_NANOSERVICE_LOCALHOST_ONLY = "true";
	public static final String 			NANOSERVICE_EXECUTOR_POOL_SIZE = "nanoserviceExecutorPoolSize";
	public static final String 			DEFAULT_NANOSERVICE_EXECUTOR_POOL_SIZE = "10";
	public static final String 			NANOSERVICE_DISABLE_LOOPBACK = "nanoserviceDisableLoopback";
	public static final String 			DEFAULT_NANOSERVICE_DISABLE_LOOPBACK = "true";
	public static final String 			NANOSERVICE_TEMPORARY_CACHE_SIZE = "nanoserviceTemporaryCacheSize";
	public static final String 			NANOSERVICE_CREOLE_PROLOGUE_URI = "nanoserviceCreolePrologueURI";
	public static final String 			NANOSERVICE_CREOLE_EPILOGUE_URI = "nanoserviceCreoleEpilogueURI";
	public static final String 			NANOSERVICE_USE_SSL = "nanoserviceUseSSL";
	public static final String 			NANOSERVICE_USE_KEYSTORE = "nanoserviceUseKeyStore";
	public static final String 			NANOSERVICE_SSL_KEYSTORE = "nanoserviceSSLKeyStore";
	public static final String 			NANOSERVICE_SSL_KEYSTORE_TYPE = "nanoserviceSSLKeyStoreType";
	public static final String 			NANOSERVICE_SSL_KEYSTORE_PASSWD = "nanoserviceSSLKeyStorePasswd";
	public static final String 			NANOSERVICE_USE_TRUSTSTORE = "nanoserviceUseTrustStore";
	public static final String 			NANOSERVICE_SSL_TRUSTSTORE = "nanoserviceSSLTrustStore";
	public static final String 			NANOSERVICE_SSL_TRUSTSTORE_TYPE = "nanoserviceSSLTrustStoreType";
	public static final String 			NANOSERVICE_SSL_TRUSTSTORE_PASSWD = "nanoserviceSSLTrustStorePasswd";
	public static final String 			NANOSERVICE_SSL_TYPE = "TLS";
	public static final String 			NANOSERVICE_KEY_TYPE = "SunX509";

	public static final String 			SYSTEM_SSL_KEYSTORE = "javax.net.ssl.keyStore";
	public static final String 			SYSTEM_SSL_KEYSTORE_TYPE = "javax.net.ssl.keyStoreType";
	public static final String 			DEFAULT_SYSTEM_SSL_KEYSTORE_TYPE = "JKS";
	public static final String 			SYSTEM_SSL_KEYSTORE_PASSWD = "javax.net.ssl.keyStorePassword";
	public static final String 			SYSTEM_SSL_TRUSTSTORE = "javax.net.ssl.trustStore";
	public static final String 			SYSTEM_SSL_TRUSTSTORE_TYPE = "javax.net.ssl.trustStoreType";
	public static final String 			DEFAULT_SYSTEM_SSL_TRUSTSTORE_TYPE = "JKS";
	public static final String 			SYSTEM_SSL_TRUSTSTORE_PASSWD = "javax.net.ssl.trustStorePassword";
	
	public static final String 			HEAD_CONTENT_LENGTH = "Content-Length";
	public static final String 			HEAD_CONTENT_TYPE = "Content-type";
	public static final String 			HEAD_CONTENT_ENCODING = "Content-encoding";
	public static final String 			HEAD_ACCEPT = "Accept"; 	
	public static final String 			HEAD_ACCEPT_CHARSET = "Accept-charset"; 	
	public static final String 			HEAD_ACCEPT_ENCODING = "Accept-encoding";

	public static final String 			HEAD_CONTENT_ENCODING_IDENTITY = "identity";
	public static final String 			HEAD_CONTENT_ENCODING_GZIP = "gzip";
	public static final String 			HEAD_CONTENT_ENCODING_COMPRESS = "compress";
		
	public static final List<Class<?>>	EXCLUDE_CLASSES_4_JSON = Collections.unmodifiableList(Arrays.asList(StringBuilder.class,CharacterTarget.class,JsonStaxPrinter.class,Writer.class,OutputStream.class
																		  ,String.class,CharacterSource.class,JsonStaxParser.class,Reader.class,InputStream.class));

	public static final InputStream		NULL_INPUT = new InputStream(){@Override public int read() throws IOException {return -1;}};

	private static final MimeType[]		CREOLE_DETECTED = new MimeType[] {PureLibSettings.MIME_CREOLE_TEXT};
	private static final MimeType[]		HTML_DETECTED = new MimeType[] {PureLibSettings.MIME_HTML_TEXT};
	
	public interface NanoServiceEnvironment {
		Connection getConnection() throws SQLException;
		LoggerFacade getLogger();
		void fail(int rc, String format, Object... parameters) throws IOException;
		void success(int rc) throws IOException;
	}

	static final String 			MACROS_CONTENT = "macros.txt";
	static final char[] 			LOOPBACK_TEXT = "/loopback".toCharArray();
	
	private final LoggerFacade						facade;
	private final DataSource 						dataSource;
	private final FileSystemInterface 				serviceRoot;
	private final HttpServer						server;
	private final SyntaxTreeInterface<ClassDescriptor>	deployed = new AndOrTree<>();
	private final ReentrantReadWriteLock			deployedLocker = new ReentrantReadWriteLock();
	private final TemplateCache<PathParser>			pathCache = new TemplateCache<>();
	private final TemplateCache<QueryParser>		queryCache = new TemplateCache<>();
	private final TemplateCache<RequestHeadParser>	requestHeaderCache = new TemplateCache<>();
	private final TemplateCache<ResponseHeadSetter>	responseHeaderCache = new TemplateCache<>();
	private final boolean							disableLoopback;
	private final boolean							localhostOnly;
	private final TemporaryStore					tempStore;
	private final SimpleURLClassLoader				urlClassLoader = new SimpleURLClassLoader(new URL[0]);
	private final AsmWriter							writer;
	private final AtomicInteger						uniqueNumber = new AtomicInteger(0);
	@SuppressWarnings("rawtypes")
	private final PrologueEpilogueMaster			prologue, epilogue;
	private final int								executorPoolSize;
	private final boolean							useBuiltinServer;

	private volatile ExecutorService				executorPool;
	private volatile boolean						started = false, paused = false;
	
	public NanoServiceFactory(final LoggerFacade facade, final SubstitutableProperties props) throws NullPointerException, IOException, ContentException, SyntaxException {
		this(facade,props,null);
	}
	
	public NanoServiceFactory(final LoggerFacade facade, final SubstitutableProperties props, final DataSource dataSource) throws NullPointerException, IOException, ContentException, SyntaxException {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (props == null) {
			throw new NullPointerException("Service properties can't be null"); 
		}
		else {
			this.facade = facade;
			this.dataSource = dataSource;
			this.useBuiltinServer = props.getProperty(NANOSERVICE_USE_BUILTIN_SERVER,boolean.class,"true");
			
			try(final LoggerFacade	check = facade.transaction("Microservice init")) {
				boolean wereErrors = false;

				if (useBuiltinServer) {
					if (!props.containsKey(NANOSERVICE_PORT)) {
						wereErrors = true;
						check.message(Severity.error, "Mandatory parameter [%1$s] is missing in the configuration",NANOSERVICE_PORT);
					}
				}
				
				if (!props.containsKey(NANOSERVICE_ROOT)) {
					wereErrors = true;
					check.message(Severity.error, "Mandatory parameter [%1$s] is missing in the configuration",NANOSERVICE_ROOT);
					serviceRoot = null;
				}
				else {
					serviceRoot = FileSystemFactory.createFileSystem(props.getProperty(NANOSERVICE_ROOT,URI.class));
				}
				
				if (props.containsKey(NANOSERVICE_CREOLE_PROLOGUE_URI)) {
					this.prologue = CreoleWriter.getPrologue(MarkupOutputFormat.XML2HTML,props.getProperty(NANOSERVICE_CREOLE_PROLOGUE_URI, URI.class));
				}
				else {
					this.prologue = CreoleWriter.getPrologue(MarkupOutputFormat.XML2HTML);
				}
				
				if (props.containsKey(NANOSERVICE_CREOLE_EPILOGUE_URI)) {
					this.epilogue = CreoleWriter.getEpilogue(MarkupOutputFormat.XML2HTML,props.getProperty(NANOSERVICE_CREOLE_EPILOGUE_URI, URI.class));
				}
				else {
					this.epilogue = CreoleWriter.getEpilogue(MarkupOutputFormat.XML2HTML);
				}
				
				if (!wereErrors) {
					this.disableLoopback = props.getProperty(NANOSERVICE_DISABLE_LOOPBACK, boolean.class, DEFAULT_NANOSERVICE_DISABLE_LOOPBACK);
					this.localhostOnly = props.getProperty(NANOSERVICE_LOCALHOST_ONLY, boolean.class, DEFAULT_NANOSERVICE_LOCALHOST_ONLY);
					this.tempStore = new TemporaryStore(props.getProperty(NANOSERVICE_TEMPORARY_CACHE_SIZE, int.class, ""+TemporaryStore.DEFAULT_BUFFER_SIZE));
					this.executorPoolSize = props.getProperty(NANOSERVICE_EXECUTOR_POOL_SIZE, int.class, DEFAULT_NANOSERVICE_EXECUTOR_POOL_SIZE);
					
					try(final InputStream	is = NanoServiceFactory.class.getResourceAsStream(MACROS_CONTENT);
						final Reader		rdr = new InputStreamReader(is,"UTF-8")) {
						
						writer = new AsmWriter(new ByteArrayOutputStream(), new OutputStreamWriter(System.err, "UTF-8"));
						Utils.copyStream(rdr,writer);
					}
					
					if (useBuiltinServer) {
						if (props.getProperty(NANOSERVICE_USE_SSL, boolean.class,"false")) {
							server = createHttpsServer(props);
						}
						else {
							server = HttpServer.create(new InetSocketAddress(props.getProperty(NANOSERVICE_PORT, int.class)),0);
						}
						server.createContext("/",this);
					}
					else {
						server = null;
					}
					check.rollback();
				}
				else {
					throw new IllegalArgumentException("Error initializing factory (see log for details)"); 
				}
			}
		}
	}

	@Override
	public void start() throws IOException {
		if (isStarted()) {
			throw new IllegalStateException("Attempt to start server already started"); 
		}
		else {
			if (useBuiltinServer) {
				server.setExecutor(this.executorPool = Executors.newFixedThreadPool(executorPoolSize));
				server.start();
			}
			paused = false;
			started = true;
		}
	}
	
	@Override
	public boolean isStarted() {
		return started;
	}
	
	@Override
	public void suspend() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Attempt to suspend server not started yet"); 
		}
		else if (isSuspended()) {
			throw new IllegalStateException("Attempt to suspend server still suspended"); 
		}
		else {
			paused = true;
		}
	}

	@Override
	public boolean isSuspended() {
		return paused;
	}
	
	@Override
	public void resume() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Attempt to suspend server not started yet"); 
		}
		else if (!isSuspended()) {
			throw new IllegalStateException("Attempt to resume service still running"); 
		}
		else {
			paused = false;
		}
	}

	@Override
	public void stop() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Attempt to stop server already stopped"); 
		}
		else {
			if (useBuiltinServer) {
				server.stop(0);
				executorPool.shutdownNow();
			}
			paused = false;
			started = false;
		}
	}
	
	@Override
	public void close() throws IOException {
		final List<String>	content = new ArrayList<>();
		final ReentrantReadWriteLock.WriteLock	lock = deployedLocker.writeLock(); 

		try(final LoggerFacade	lf = facade.transaction("close nano")) {
			
			try{lock.lockInterruptibly();
				deployed.walk((name,len,id,cargo)->{content.add(new String(name,0,len)); return true;});
				
				for (String item : content) {
					lf.message(Severity.info,"Undeploying [%1$s]...",item);
					try{undeploy(item);
					} catch (Exception exc) {
						lf.message(Severity.info,"Error undeploying [%1$s]: %2$s",item,exc.getLocalizedMessage());
					}
				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			} finally {
				lock.unlock();
			}	
			
			responseHeaderCache.close();
			requestHeaderCache.close();
			queryCache.close();
			pathCache.close();
			tempStore.close();
			urlClassLoader.close();
			writer.close();
			if (serviceRoot != null) {
				serviceRoot.close();
			}
			lf.rollback();
		}	
	}

	@Override
	public void handle(final HttpExchange call) throws IOException {
		final NanoServiceEnvironment	nse = new SimpleHttpServerEnvironment(call,facade);
		final QueryType 				type;
		
		try{type = QueryType.valueOf(call.getRequestMethod());
		} catch (IllegalArgumentException exc) {
			nse.fail(HttpURLConnection.HTTP_BAD_METHOD, "Request method [%1$s] is not supported yet",call.getRequestMethod());
			return;
		}
		
		handle(nse,call.getRemoteAddress(),type,call.getRequestURI(),call.getRequestHeaders(),call.getRequestBody(),call.getResponseHeaders(),call.getResponseBody());
	}
	
	@Override
	public InetSocketAddress getServerAddress() {
		if (server != null) {
			return server.getAddress();
		}
		else {
			return null;
		}
	}
	
	public void handle(final NanoServiceEnvironment env,final InetSocketAddress remoteAddress, final QueryType queryType, final URI requestUri, final Headers requestHeaders, final InputStream requestBody, final Headers responseHeaders, final OutputStream responseBody) throws IOException {
		final String	remoteHost = remoteAddress.getHostName();
		
		if (localhostOnly && !remoteAddress.getAddress().isLoopbackAddress()) {
			env.fail(HttpURLConnection.HTTP_FORBIDDEN,"Illegal source address ["+remoteHost+"], only localhost addresses are currently available");
		}
		else if (!paused) {
			final char[]			path = requestUri.getPath().toCharArray();
			final String			pluginRoot = seekPluginRoot(path, queryType);
			final MethodExecutor	me;

			switch(queryType) {
				case GET	:
					if (pluginRoot == null || (me = seekPlugin(path, queryType, requestHeaders)) == null) {
						try(final FileSystemInterface	fsi  = serviceRoot.clone().open(new String(path))) {
							
							if (!fsi.exists() || !fsi.isFile()) {
								env.fail(HttpURLConnection.HTTP_NOT_FOUND,"Sorry, but resource [%1$s] not exists",fsi.getPath());
							}
							else {
								final MimeType[]			detected = InternalUtils.defineMimeByExtension(fsi.getName());
								final String				streamType = defineStreamType(requestHeaders.get(HEAD_ACCEPT_ENCODING));  
	
								responseHeaders.put(HEAD_CONTENT_ENCODING,Arrays.asList(streamType));
								if (requestHeaders.containsKey(HEAD_ACCEPT)) {
									final List<String>		accepts = requestHeaders.get(HEAD_ACCEPT);
									final MimeType[]		awaited = InternalUtils.buildMime(accepts.toArray(new String[accepts.size()])); 

									if (Arrays.deepEquals(detected,CREOLE_DETECTED) && InternalUtils.mimesIntersect(HTML_DETECTED,awaited)) {
										responseHeaders.put(HEAD_CONTENT_TYPE,Arrays.asList(HTML_DETECTED[0].toString()+"; charset=UTF8"));
										env.success(HttpURLConnection.HTTP_OK);
										
										try(final OutputStream	os = getOutputStream(responseBody,streamType);
											final Writer		wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
											final CreoleWriter	cre = new CreoleWriter(wr,MarkupOutputFormat.XML2HTML,prologue,epilogue)) {
											
											fsi.copy(cre);
										}
									}
									else if (InternalUtils.mimesIntersect(detected,awaited)) {
										responseHeaders.put(HEAD_CONTENT_TYPE,Arrays.asList(detected[0].toString()));
										env.success(HttpURLConnection.HTTP_OK);
										try(final OutputStream	os = getOutputStream(responseBody,streamType)) {
											
											fsi.copy(os);
										}
									}
									else {
										env.fail(HttpURLConnection.HTTP_UNSUPPORTED_TYPE,"URI [%1$s]: Target content %2$s is not compatible with awaited [%3$s]",new String(path),Arrays.toString(detected),requestHeaders.get(HEAD_ACCEPT).get(0));
									}
								}
								else {
									env.success(HttpURLConnection.HTTP_OK);
									try(final OutputStream	os = getOutputStream(responseBody,streamType)) {
										
										fsi.copy(os);
									}
								}
							}
						} catch (MimeParseException | RuntimeException exc) {
							PureLibSettings.CURRENT_LOGGER.message(Severity.error,exc,exc.getLocalizedMessage());
							env.fail(HttpURLConnection.HTTP_INTERNAL_ERROR,"Exception %1$s (%2$s) during processing request", exc.getClass().getSimpleName(), exc.getLocalizedMessage());
						}
					}
					else {
						final String		inputStreamType = defineStreamType(requestHeaders.get(HEAD_CONTENT_ENCODING));  
						final String		outputStreamType = defineStreamType(requestHeaders.get(HEAD_ACCEPT_ENCODING));
						final List<String>	inputContent = requestHeaders.get(HEAD_CONTENT_TYPE); 
						final List<String>	outputContent = requestHeaders.get(HEAD_ACCEPT); 
						
						try(final InputOutputPair	pair = tempStore.allocate()){
							int 	rc = 200;
							
							try(final OutputStream	os = pair.getOutputStream()) {
								final String 		query = URIUtils.extractQueryFromURI(requestUri);
								final String		pathTail = requestUri.getPath().replace(pluginRoot,"");
										
								rc = me.execute(QueryType.GET
											, pathTail.startsWith("/") ? pathTail.substring(1).toCharArray() : pathTail.toCharArray() 
											, query != null ? query.toCharArray() : null
											, (Headers)requestHeaders
											, (Headers)responseHeaders
											, null
											, os);
							}
							
							if (rc >= 100 && rc <= 399) {
								try(final InputStream	is = pair.getInputStream()) {
									responseHeaders.put(HEAD_CONTENT_LENGTH,Arrays.asList(String.valueOf(pair.getSizeUsed())));
									responseHeaders.put(HEAD_CONTENT_ENCODING,Arrays.asList(outputStreamType));
									env.success(rc);
									
									try(final OutputStream 	os = getOutputStream(responseBody,outputStreamType)) {
										Utils.copyStream(is, os);
									}
								}
							}
							else {
								env.fail(rc, "Unsuccessful processing your request");
							}
						} catch (Throwable exc) {
							exc.printStackTrace();
							env.fail(HttpURLConnection.HTTP_INTERNAL_ERROR,"Exception %1$s (%2$s) during processing request", exc.getClass().getSimpleName(), exc.getLocalizedMessage());
						}
					}
					break;
				case POST	:
					if (pluginRoot == null || (me = seekPlugin(path, queryType, requestHeaders)) == null) {
						env.fail(HttpURLConnection.HTTP_BAD_METHOD,"Request method [%1$s] is incompatible with the static content",queryType);
					}
					else {
						final String				inputStreamType = defineStreamType(requestHeaders.get(HEAD_CONTENT_ENCODING));  
						final String				outputStreamType = defineStreamType(requestHeaders.get(HEAD_ACCEPT_ENCODING));
						final List<String>			inputContent = requestHeaders.get(HEAD_CONTENT_TYPE); 
						final List<String>			outputContent = requestHeaders.get(HEAD_ACCEPT); 
						
						try(final InputOutputPair	pair = tempStore.allocate()){
							int 	rc = 200;
							
							try(final OutputStream	os = pair.getOutputStream()) {
								final String		pathTail = requestUri.getPath().replace(pluginRoot,"");
										
								rc = me.execute(QueryType.POST
											, pathTail.startsWith("/") ? pathTail.substring(1).toCharArray() : pathTail.toCharArray() 
											, null
											, requestHeaders
											, responseHeaders
											, requestBody
											, os);
							}
							
							if (rc >= 200 && rc <= 299) {
								try(final InputStream	is = pair.getInputStream()) {
									responseHeaders.put(HEAD_CONTENT_LENGTH,Arrays.asList(String.valueOf(pair.getSizeUsed())));
									responseHeaders.put(HEAD_CONTENT_ENCODING,Arrays.asList(outputStreamType));
									env.success(rc);
									
									try(final OutputStream 	os = getOutputStream(responseBody,outputStreamType)) {
										Utils.copyStream(is, os);
									}
								}
							}
							else {
								env.fail(rc,"Unsuccessful processing your request");
							}
						} catch (Throwable exc) {
							exc.printStackTrace();
							env.fail(HttpURLConnection.HTTP_INTERNAL_ERROR,"Exception %1$s (%2$s) during processing request", exc.getClass().getSimpleName(), exc.getLocalizedMessage());
						}
					}
					break;
				case PUT	:
					if (pluginRoot == null || (me = seekPlugin(path, queryType, requestHeaders)) == null) {
						env.fail(HttpURLConnection.HTTP_BAD_METHOD,"Request method [%1$s] is incompatible with the static content",queryType);
					}
					else {
						final String				inputStreamType = defineStreamType(requestHeaders.get(HEAD_CONTENT_ENCODING));  
						final String				outputStreamType = defineStreamType(requestHeaders.get(HEAD_ACCEPT_ENCODING));
						final List<String>			inputContent = requestHeaders.get(HEAD_CONTENT_TYPE); 
						final List<String>			outputContent = requestHeaders.get(HEAD_ACCEPT); 
						
						try(final InputOutputPair	pair = tempStore.allocate()){
							int 	rc = 200;
							
							try(final OutputStream	os = pair.getOutputStream()) {
								final String		pathTail = requestUri.getPath().replace(pluginRoot,"");
										
								rc = me.execute(QueryType.PUT
											, pathTail.startsWith("/") ? pathTail.substring(1).toCharArray() : pathTail.toCharArray() 
											, null
											, requestHeaders
											, responseHeaders
											, requestBody
											, os);
							}
							
							if (rc >= 200 && rc <= 299) {
								try(final InputStream	is = pair.getInputStream()) {
									responseHeaders.put(HEAD_CONTENT_LENGTH,Arrays.asList(String.valueOf(pair.getSizeUsed())));
									responseHeaders.put(HEAD_CONTENT_ENCODING,Arrays.asList(outputStreamType));
									env.success(rc);
									
									try(final OutputStream 	os = getOutputStream(responseBody,outputStreamType)) {
										Utils.copyStream(is, os);
									}
								}
							}
							else {
								env.fail(rc,"Unsuccessful processing your request");
							}
						} catch (Throwable exc) {
							env.fail(HttpURLConnection.HTTP_INTERNAL_ERROR,"Exception %1$s (%2$s) during processing request", exc.getClass().getSimpleName(), exc.getLocalizedMessage());
						}
					}
					break;
				case DELETE	:
					if (pluginRoot == null || (me = seekPlugin(path, queryType, requestHeaders)) == null) {
						env.fail(HttpURLConnection.HTTP_BAD_METHOD,"Request method [%1$s] is incompatible with the static content",queryType);
					}
					else {
						final String				inputStreamType = defineStreamType(requestHeaders.get(HEAD_CONTENT_ENCODING));  
						final String				outputStreamType = defineStreamType(requestHeaders.get(HEAD_ACCEPT_ENCODING));
						final List<String>			inputContent = requestHeaders.get(HEAD_CONTENT_TYPE); 
						final List<String>			outputContent = requestHeaders.get(HEAD_ACCEPT); 
						
						try(final InputOutputPair	pair = tempStore.allocate()){
							int 	rc = 200;
							
							try(final OutputStream	os = pair.getOutputStream()) {
								final String		pathTail = requestUri.getPath().replace(pluginRoot,"");
										
								rc = me.execute(QueryType.DELETE
											, pathTail.startsWith("/") ? pathTail.substring(1).toCharArray() : pathTail.toCharArray() 
											, null
											, requestHeaders
											, responseHeaders
											, null
											, os);
							}
							
							if (rc >= 200 && rc <= 299) {
								try(final InputStream	is = pair.getInputStream()) {
									responseHeaders.put(HEAD_CONTENT_LENGTH,Arrays.asList(String.valueOf(pair.getSizeUsed())));
									responseHeaders.put(HEAD_CONTENT_ENCODING,Arrays.asList(outputStreamType));
									env.success(rc);
									
									try(final OutputStream 	os = getOutputStream(responseBody,outputStreamType)) {
										Utils.copyStream(is, os);
									}
								}
							}
							else {
								env.fail(rc,"Unsuccessful processing your request");
							}
						} catch (Throwable exc) {
							env.fail(HttpURLConnection.HTTP_INTERNAL_ERROR,"Exception %1$s (%2$s) during processing request", exc.getClass().getSimpleName(), exc.getLocalizedMessage());
						}
					}
					break;
				case HEAD	:
					if (pluginRoot == null || (me = seekPlugin(path, queryType, requestHeaders)) == null) {
						env.fail(HttpURLConnection.HTTP_UNSUPPORTED_TYPE,"Target content [%1$s] is not compatible with awaited [%2$s]",HEAD_CONTENT_TYPE,requestHeaders.get(HEAD_ACCEPT).get(0));
					}
					else {
						final String				inputStreamType = defineStreamType(requestHeaders.get(HEAD_CONTENT_ENCODING));  
						final String				outputStreamType = defineStreamType(requestHeaders.get(HEAD_ACCEPT_ENCODING));
						final List<String>			inputContent = requestHeaders.get(HEAD_CONTENT_TYPE); 
						final List<String>			outputContent = requestHeaders.get(HEAD_ACCEPT); 
						
						try{final String						query = URIUtils.extractQueryFromURI(requestUri);
							final Hashtable<String,String[]>	queryParsed = URIUtils.parseQuery(query == null ? "" : query);
							final String						pathTail = requestUri.getPath().replace(pluginRoot,"");
							int 	rc = 200;
							long	length[] = new long[]{0};
									
							rc = me.execute(QueryType.HEAD
										, pathTail.startsWith("/") ? pathTail.substring(1).toCharArray() : pathTail.toCharArray() 
										, query != null ? query.toCharArray() : null
										, requestHeaders
										, responseHeaders
										, null
										, new OutputStream() {
											@Override
											public void write(int b) throws IOException {
												length[0]++;
											}
										});
							
							if (rc >= 200 && rc <= 299) {
								responseHeaders.put(HEAD_CONTENT_LENGTH,Arrays.asList(String.valueOf(length[0])));
								responseHeaders.put(HEAD_CONTENT_ENCODING,Arrays.asList(outputStreamType));
								env.success(rc);
							}
							else {
								env.fail(rc,"Unsuccessful processing your request");
							}
						} catch (Throwable exc) {
							env.fail(HttpURLConnection.HTTP_INTERNAL_ERROR,"Exception %1$s (%2$s) during processing request", exc.getClass().getSimpleName(), exc.getLocalizedMessage());
						}
					}
					break;
				default		:
					env.fail(HttpURLConnection.HTTP_BAD_METHOD,"Request method [%1$s] is not supported yet",queryType);
			}
			
//			if (call instanceof HttpsExchange) {
//				// TODO Auto-generated method stub
//			    final HttpsExchange 	callS = (HttpsExchange) call; 
//			    final SSLSession 		sess = callS.getSSLSession();
//
//			    //if( sess.getPeerPrincipal() != null) System.out.println(sess.getPeerPrincipal().toString()); // Principal never populated.
//			    
//			    System.out.printf("Responding to host: %s\n",sess.getPeerHost());
//			    callS.getResponseHeaders().set("Content-Type", "text/plain");
//			    callS.sendResponseHeaders(200,0);
//			    String response = "Hello!  You seem trustworthy!\n";
//			    OutputStream os = callS.getResponseBody();
//			    os.write(response.getBytes());
//			    os.close();
//			}
//			else {
//				// TODO Auto-generated method stub
//			    call.getResponseHeaders().set("Content-Type", "text/plain");
//			    call.sendResponseHeaders(200,0);
//			    String response = "Hello!  You seem trustworthy!\n";
//			    OutputStream os = call.getResponseBody();
//			    os.write(response.getBytes());
//			    os.close();
//			}
		}
		else {
			env.fail(HttpURLConnection.HTTP_UNAVAILABLE,"Service paused by operator");
		}
	}

	@Override
	public void deploy(final String path, final Object instance2deploy) throws IOException, ContentException, SyntaxException {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path to deploy can't be null or empty");
		}
		else if (instance2deploy == null) {
			throw new NullPointerException("Class instance to deploy can't be null");
		}
		else {
			final ClassDescriptor	desc = parseClass(instance2deploy, instance2deploy.getClass());
			final ReentrantReadWriteLock.WriteLock	lock = deployedLocker.writeLock(); 

			try {lock.lockInterruptibly();
				final long			id = deployed.seekName(path);
	
				if (id >= 0) {
					if (deployed.getCargo(id) != null) {
						throw new IllegalArgumentException("Path to deploy ["+path+"] contains class already was deployed"); 
					}
					else {
						deployed.setCargo(id,desc); 
					}
				}
				else {
					deployed.placeName(path,desc);
				}
			} catch (InterruptedException e) {
				throw new IOException(e); 
			} finally {
				lock.unlock();
			}
		}
	}
	
	@Override
	public Object undeploy(final String path) throws IOException {
		final ReentrantReadWriteLock.WriteLock	lock = deployedLocker.writeLock(); 
		Object	returned = null;
		
		try {lock.lockInterruptibly();
			final long	id = deployed.seekName(path);
			
			if (id >= 0) {
				if ((returned = deployed.getCargo(id)) != null) {
					deployed.setCargo(id,null);
				}
				else {
					throw new IllegalArgumentException("Attempt to undeploy non-deployed path ["+path+"]");
				}
			}
			else {
				throw new IllegalArgumentException("Attempt to undeploy non-deployed path ["+path+"]");
			}
		} catch (InterruptedException e) {
			throw new IOException(e); 
		} finally {
			lock.unlock();
		}
		return returned;
	}

	@Override
	public FileSystemInterface getServiceRoot() {
		return serviceRoot;
	}

	public static SSLContext createSSLContext(final SubstitutableProperties props) throws IOException, NullPointerException  {
		if (props == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else {
		    try{final SSLContext			sslContext = SSLContext.getInstance(NANOSERVICE_SSL_TYPE);
	        	final KeyManagerFactory		kmf = KeyManagerFactory.getInstance(NANOSERVICE_KEY_TYPE);
		        final TrustManagerFactory	tmf = TrustManagerFactory.getInstance(NANOSERVICE_KEY_TYPE);
		    	final KeyStore 				ks = KeyStore.getInstance(props.getProperty(NANOSERVICE_SSL_KEYSTORE_TYPE, String.class
		    									, System.getProperty(SYSTEM_SSL_KEYSTORE_TYPE, DEFAULT_SYSTEM_SSL_KEYSTORE_TYPE)));
			    final KeyStore 				ts = KeyStore.getInstance(props.getProperty(NANOSERVICE_SSL_TRUSTSTORE_TYPE, String.class
												, System.getProperty(SYSTEM_SSL_TRUSTSTORE_TYPE, DEFAULT_SYSTEM_SSL_TRUSTSTORE_TYPE)));
	
			    if (props.getProperty(NANOSERVICE_USE_KEYSTORE,boolean.class,"false")) {
			        try(final InputStream 	is = props.getProperty(NANOSERVICE_SSL_KEYSTORE, InputStream.class, System.getProperty(SYSTEM_SSL_KEYSTORE))) {
			        	ks.load(is,props.getProperty(NANOSERVICE_SSL_KEYSTORE_PASSWD, char[].class, System.getProperty(SYSTEM_SSL_KEYSTORE_PASSWD)));
			        }
			        kmf.init(ks,props.getProperty(NANOSERVICE_SSL_KEYSTORE_PASSWD, char[].class, System.getProperty(SYSTEM_SSL_KEYSTORE_PASSWD)));
			    }
			    if (props.getProperty(NANOSERVICE_USE_TRUSTSTORE,boolean.class,"false")) {
			        try(final InputStream 	is = props.getProperty(NANOSERVICE_SSL_TRUSTSTORE, InputStream.class, System.getProperty(SYSTEM_SSL_TRUSTSTORE))) {
			        	ts.load(is,props.getProperty(NANOSERVICE_SSL_TRUSTSTORE_PASSWD, char[].class, System.getProperty(SYSTEM_SSL_TRUSTSTORE_PASSWD)));
			        }
			        tmf.init(ts);
			    }
		        sslContext.init(props.getProperty(NANOSERVICE_USE_KEYSTORE,boolean.class,"false") ? kmf.getKeyManagers() : null
		        			  , props.getProperty(NANOSERVICE_USE_TRUSTSTORE,boolean.class,"false") ? tmf.getTrustManagers() : null
		        			  , null);
		        return sslContext;
		    } catch (Exception e) {
		    	throw new IOException(e);
		    }       
		}
	}
	
//	ClassLoaderWrapper getInternalLoader() {
//		return internalLoader;
//	}
	
	private HttpServer createHttpsServer(final SubstitutableProperties props) throws IOException {
		final HttpsServer 		server = HttpsServer.create(new InetSocketAddress(props.getProperty(NANOSERVICE_PORT,int.class)), 0);
		final SSLContext 		sslCon = createSSLContext(props);
		final SSLConfigurator 	authconf = new SSLConfigurator(sslCon);
		    
		server.setHttpsConfigurator(authconf);
	    return server;
	}

	private static InputStream getInputStream(final InputStream sourceStream, final List<String> encodings) throws IOException {
		for (String encoding : encodings) {
			switch (encoding.toLowerCase()) {
				case HEAD_CONTENT_ENCODING_GZIP		: return new GZIPInputStream(sourceStream){@Override public void close() throws IOException {sourceStream.close();}};
				case HEAD_CONTENT_ENCODING_COMPRESS	: return new ZLibInputStream(sourceStream){@Override public void close() throws IOException {sourceStream.close();}};
				default 		: return sourceStream;
			}
		}
		return sourceStream;
	}
	
	private static OutputStream getOutputStream(final OutputStream targetStream, final String encoding) throws IOException {
		switch (encoding.toLowerCase()) {
			case HEAD_CONTENT_ENCODING_GZIP		: return new GZIPOutputStream(targetStream){@Override public void close() throws IOException {finish(); targetStream.close();}};
			case HEAD_CONTENT_ENCODING_COMPRESS	: return new ZLibOutputStream(targetStream){@Override public void close() throws IOException {super.close(); targetStream.close();}};
			default 		: return targetStream;
		}
	}

	private String seekPluginRoot(final char[] charPath, final QueryType qType) {
		final ClassDescriptor	desc;
		final int				prefixLen;

		if (!disableLoopback && UnsafedCharUtils.uncheckedCompare(charPath,0,LOOPBACK_TEXT,0,LOOPBACK_TEXT.length)) {
			return "/loopback";
		}
		else {
			final ReentrantReadWriteLock.ReadLock	lock = deployedLocker.readLock();
			
			try{lock.lockInterruptibly();
				
				final long	id = deployed.seekName(charPath,0,charPath.length);
				
				if (id < 0) {
					if (charPath[(int)(-id-1)] == '/' && id != -1) {
						return new String(charPath,0,(int) (-id-1));
					}
					else {
						return null;
					}
				}
				else {
					return new String(charPath,0,charPath.length);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			} finally {
				lock.unlock();
			}
		}
	}

	private MethodExecutor seekPlugin(final char[] charPath, final QueryType qType, final Map<String,List<String>> requestHeaders) {
		final ClassDescriptor	desc;
		final int				prefixLen;

		if (!disableLoopback && UnsafedCharUtils.uncheckedCompare(charPath,0,LOOPBACK_TEXT,0,LOOPBACK_TEXT.length)) {
			return new Loopback();
		}
		else {
			final ReentrantReadWriteLock.ReadLock	lock = deployedLocker.readLock();
			
			try {lock.lockInterruptibly();
				final long	id = deployed.seekName(charPath,0,charPath.length);
				
				if (id < 0) {
					if (charPath[(int)(-id-1)] == '/' && id != -1) {
						desc = deployed.getCargo(deployed.seekName(charPath,0,(int) (-id-1)));
						prefixLen = (int) (-id-1);
					}
					else {
						desc = null;
						prefixLen = 0;
					}
				}
				else {
					desc = deployed.getCargo(id);
					prefixLen = (int) id;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			} finally {
				lock.unlock();
			}
			
			if (desc != null) {
				try{final List<String>	from = requestHeaders.get(HEAD_CONTENT_TYPE);
					final String		fromMimeString = from !=null ? from.get(0) : PureLibSettings.MIME_OCTET_STREAM.toString();
					final List<String>	to = requestHeaders.get(HEAD_ACCEPT);
					final String		toMimeString = to != null ? to.get(0) : PureLibSettings.MIME_OCTET_STREAM.toString();
					final MimeType		fromMime = fromMimeString != null ? MimeType.parseMimeList(fromMimeString)[0] : PureLibSettings.MIME_OCTET_STREAM;
//										toMime = toMimeString != null ? MimeType.parseMimeList(toMimeString)[0] : PureLibSettings.MIME_OCTET_STREAM;
					
					for (MethodDescriptor item : desc.methods) {
						for (MimeType type : MimeType.parseMimeList(toMimeString != null ? toMimeString : PureLibSettings.MIME_OCTET_STREAM.toString())) {
							if (item.isAppicable(qType, charPath, prefixLen, fromMime, type)) {
								return item.caller;
							}
						}
					}
					return null;
				} catch (MimeParseException e) {
					return null;
				}
			}
			else {
				return null;
			}
		}
	}
	
	/*
	 * Collection of methods to parse annotated plug-in class and build wrapper to it
	 */
	
	ClassDescriptor parseClass(final Object instance, final Class<?> clazz) throws ContentException, SyntaxException, IOException {
		if (!clazz.isAnnotationPresent(RootPath.class)) {
			throw new ContentException("Class ["+clazz.getCanonicalName()+"] is not annotated with @RootPath!");
		}
		else {
			final String					rootPath = clazz.getAnnotation(RootPath.class).value();
			final List<Method>				annotatedMethods = new ArrayList<>();
			final List<MethodDescriptor>	collection = new ArrayList<>();
	
			collectAnnotatedMethods(clazz,annotatedMethods);
			
			try{for (Method m : annotatedMethods) {
					final Path				path = m.getAnnotation(Path.class);
					final MimeType[]		source = InternalUtils.buildMime(collectSourceMimeType(m));
					final MimeType[] 		target = InternalUtils.buildMime(collectTargetMimeType(m));
					final MethodExecutor	caller = buildMethodExecutor(instance, m, path.value(), path.type(), source, target);
		
					collection.add(new MethodDescriptor(path.value(),path.type(),source,target,caller));
				}
			} catch (MimeParseException e) {
				throw new IOException(e.getLocalizedMessage(),e);
			}
			
			return new ClassDescriptor(rootPath.toCharArray(), clazz, collection.toArray(new MethodDescriptor[collection.size()]));
		}
	}

	String[] collectSourceMimeType(final Method m) {
		final StringBuilder	sb = new StringBuilder();
		
		for (Parameter item : m.getParameters()) {
			if (item.isAnnotationPresent(FromBody.class)) {
				sb.append('@').append(item.getAnnotation(FromBody.class).mimeType());
			}
		}
		if (sb.length() == 0) {
			return new String[0];
		}
		else {
			return CharUtils.split(sb.toString().substring(1),'@');
		}
	}

	String[] collectTargetMimeType(final Method m) {
		final StringBuilder	sb = new StringBuilder();
		
		for (Parameter item : m.getParameters()) {
			if (item.isAnnotationPresent(ToBody.class)) {
				sb.append('@').append(item.getAnnotation(ToBody.class).mimeType());
			}
		}
		if (sb.length() == 0) {
			return new String[0];
		}
		else {
			return CharUtils.split(sb.toString().substring(1),'@');
		}
	}
	
	MethodExecutor buildMethodExecutor(final Object instance, final Method m, final String value, final QueryType[] type, final MimeType[] source, final MimeType[] target) throws ContentException, SyntaxException, IOException {
		for (Parameter item : m.getParameters()) {
			int	count = 0;
			
			if (item.isAnnotationPresent(FromPath.class)) {
				count++;
			}
			if (item.isAnnotationPresent(FromQuery.class)) {
				count++;
			}
			if (item.isAnnotationPresent(FromHeader.class)) {
				count++;
			}
			if (item.isAnnotationPresent(FromBody.class)) {
				count++;
			}
			if (item.isAnnotationPresent(ToHeader.class)) {
				count++;
			}
			if (item.isAnnotationPresent(ToBody.class)) {
				count++;
			}
			
			if (count == 0) {
				throw new ContentException("Method ["+m.getName()+"] in the class ["+m.getDeclaringClass().getCanonicalName()+"] has non-annotated parameter ["+item.getName()+"]"); 
			}
			else if (count > 1) {
				throw new ContentException("Method ["+m.getName()+"] in the class ["+m.getDeclaringClass().getCanonicalName()+"] has multiple annotated parameter ["+item.getName()+"]. Only one annotation can be used for it"); 
			}
		}
		
		final MethodExecutor[]	collection = new MethodExecutor[QueryType.values().length];
		MethodExecutor			lastMethod;
		
		for (int index = 0; index < type.length; index++) {
			switch (type[index]) {
				case GET	:
					collection[type[index].ordinal()] = lastMethod = buildGetMethodExecutor(instance, m, value, source, target);
					break;
				case POST	:
					collection[type[index].ordinal()] = lastMethod = buildPostMethodExecutor(instance, m, value, source, target);
					break;
				case PUT	:
					collection[type[index].ordinal()] = lastMethod = buildPutMethodExecutor(instance, m, value, source, target);
					break;
				case DELETE	:
					collection[type[index].ordinal()] = lastMethod = buildDeleteMethodExecutor(instance, m, value, source, target);
					break;
				case HEAD	:
					collection[type[index].ordinal()] = lastMethod = buildGetMethodExecutor(instance, m, value, source, target);
					break;
				default :
					throw new UnsupportedOperationException("Query type ["+type[index]+"] is not implemented yet");
			}
			if (instance instanceof ModuleAccessor) {
				((ModuleAccessor)instance).allowUnnamedModuleAccess(lastMethod.getClass().getModule());
			}
		}
		
		if (type.length == 1) {
			return collection[type[0].ordinal()];
		}
		else {
			return new MethodExecutor(){
				@Override
				public int execute(final QueryType type, final char[] path, final char[] query, final Headers requestHeaders, final Headers responseHeaders, final InputStream is, final OutputStream os) throws IOException, ContentException, FlowException, EnvironmentException {
					if (type == null) {
						throw new NullPointerException("Query type to process can't be null");
					}
					else if (collection[type.ordinal()] == null) {
						throw new IllegalArgumentException("Processing ["+type+"] is not annotated for the given method");
					} 
					else {
						return collection[type.ordinal()].execute(type, path, query, requestHeaders, responseHeaders, is, os);
					}
				}
			};
		}
	}
	
	MethodExecutor buildGetMethodExecutor(final Object instance, final Method m, final String path, final MimeType[] source, final MimeType[] target) throws SyntaxException, ContentException, IOException {
		final String[]		pathNames = collectPathParameters(m)
							, queryNames = collectQueryParameters(m)
							, requestHeaderNames = collectRequestParameters(m)
							, responseHeaderNames = collectResponseParameters(m);
		final AnyKindParserAndSetter forPath = pathNames.length > 0 ? findOrBuildPathParser(path.startsWith("/") ? path.substring(1) : path) : null
							, forQuery = queryNames.length > 0 ? findOrBuildQueryParameters(queryNames) : null 
							, forRequestHeader = requestHeaderNames.length > 0 ? findOrBuildRequestHeadParameters(requestHeaderNames) : null
							, forResponseHeader = responseHeaderNames.length > 0 ? findOrBuildResponseHeadParameters(responseHeaderNames,collectResponseParameterClasses(m)) : null;
		final int			unique = uniqueNumber.incrementAndGet(), totalParameters = pathNames.length + queryNames.length + requestHeaderNames.length; 
		
		final StringBuilder	result = new StringBuilder();
		
		try{result.append("	makeIncludes \"").append(m.getDeclaringClass().getCanonicalName()).append("\"\n")
				  .append(collectAdditionalImports(m))
				  .append(" getExecutorHead \"").append(m.getDeclaringClass().getCanonicalName()).append("\",\"GetExecutor").append(unique).append('\"')
				  		.append(",pathParsed=").append(pathNames.length)
				  		.append(",queryParsed=").append(queryNames.length)
				  		.append(",requestHeadParsed=").append(requestHeaderNames.length)
				  		.append(",totalResponsed=").append(responseHeaderNames.length).append('\n');
			
			for (Parameter item : m.getParameters()) {
				if (item.isAnnotationPresent(FromPath.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromPath.class).value(),pathNames,queryNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(FromQuery.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromQuery.class).value(),pathNames,queryNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(FromHeader.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromHeader.class).value(),pathNames,queryNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(ToHeader.class)) {
					result.append(buildToConvertor(item.getType(),defineParameterIndex(item.getAnnotation(ToHeader.class).value(),responseHeaderNames)));
				}
				else if (item.isAnnotationPresent(ToBody.class)) {
					try{result.append(buildToBodyParameterText(item.getType(),MimeType.parseMimeList(item.getAnnotation(ToBody.class).mimeType())[0]));
					} catch (MimeParseException exc) {
						throw new ContentException(exc);
					} catch (ContentException exc) {
						throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"], method ["+m.getName()+"], parameter ["+item.getName()+"] (annotated with @toBody): "+exc.getLocalizedMessage()); 
					}		
				}
				else if (item.isAnnotationPresent(FromBody.class)) {
					throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"], method ["+m.getName()+"], parameter ["+item.getName()+"] (annotated with @toBody): @FromBody annotation is not applicable for the GET method parameters"); 
				}
			}
			
			result.append('\t').append(CompilerUtils.buildMethodCall(m)).append('\n');
			result.append(" getExecutorMethodTail responsePresent="+(responseHeaderNames.length > 0)+"\n");
			result.append(" getExecutorClassTail \"GetExecutor").append(unique).append("\"\n");
			
			return (MethodExecutor) buildInstance(writer,"GetExecutor" + unique,result.toString(),urlClassLoader)
									.getConstructor(AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,Object.class)
									.newInstance(forPath,forQuery,forRequestHeader,forResponseHeader,instance);
		} catch (VerifyError err) {
			err.printStackTrace();
			throw new ContentException("Error verifying code: "+err.getLocalizedMessage()+"\n compiled content is:\n"+result.toString());
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException exc) {
			exc.printStackTrace();
			throw new ContentException(exc);
		}		
	}

	MethodExecutor buildPostMethodExecutor(final Object instance, final Method m, final String path, final MimeType[] source, final MimeType[] target) throws SyntaxException, ContentException, IOException {
		final String[]		pathNames = collectPathParameters(m)
							, requestHeaderNames = collectRequestParameters(m)
							, responseHeaderNames = collectResponseParameters(m);
		final AnyKindParserAndSetter forPath = pathNames.length > 0 ? findOrBuildPathParser(path.startsWith("/") ? path.substring(1) : path) : null
							, forRequestHeader = requestHeaderNames.length > 0 ? findOrBuildRequestHeadParameters(requestHeaderNames) : null
							, forResponseHeader = responseHeaderNames.length > 0 ? findOrBuildResponseHeadParameters(responseHeaderNames,collectResponseParameterClasses(m)) : null;
		final int			unique = uniqueNumber.incrementAndGet(), totalParameters = pathNames.length + requestHeaderNames.length; 
		
		final StringBuilder	result = new StringBuilder();
	
		try{result.append("	makeIncludes \"").append(m.getDeclaringClass().getCanonicalName()).append("\"\n")
			  .append(collectAdditionalImports(m))
			  .append(" getExecutorHead \"").append(m.getDeclaringClass().getCanonicalName()).append("\",\"PostExecutor").append(unique).append('\"')
			  		.append(",pathParsed=").append(pathNames.length)
			  		.append(",queryParsed=0")
			  		.append(",requestHeadParsed=").append(requestHeaderNames.length)
			  		.append(",totalResponsed=").append(responseHeaderNames.length).append('\n');
	
			for (Parameter item : m.getParameters()) {
				if (item.isAnnotationPresent(FromPath.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromPath.class).value(),pathNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(FromQuery.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromQuery.class).value(),pathNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(FromHeader.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromHeader.class).value(),pathNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(ToHeader.class)) {
					result.append(buildToConvertor(item.getType(),defineParameterIndex(item.getAnnotation(ToHeader.class).value(),responseHeaderNames)));
				}
				else if (item.isAnnotationPresent(ToBody.class)) {
					try{result.append(buildToBodyParameterText(item.getType(),MimeType.parseMimeList(item.getAnnotation(ToBody.class).mimeType())[0]));
					} catch (MimeParseException exc) {
						exc.printStackTrace();
						throw new ContentException(exc);
					} catch (ContentException exc) {
						throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"], method ["+m.getName()+"], parameter ["+item.getName()+"] (annotated with @ToBody): "+exc.getLocalizedMessage(), exc); 
					}		
				}
				else if (item.isAnnotationPresent(FromBody.class)) {
					try{result.append(buildFromBodyParameterText(item.getType(),MimeType.parseMimeList(item.getAnnotation(FromBody.class).mimeType())[0]));
					} catch (MimeParseException exc) {
						throw new ContentException(exc);
					} catch (ContentException exc) {
						exc.printStackTrace();
						throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"], method ["+m.getName()+"], parameter ["+item.getName()+"] (annotated with @FromBody): "+exc.getLocalizedMessage()); 
					}		
				}
			}
	
			result.append('\t').append(CompilerUtils.buildMethodCall(m)).append('\n');
			result.append(" getExecutorMethodTail responsePresent="+(responseHeaderNames.length > 0)+"\n");
			result.append(" getExecutorClassTail \"PostExecutor").append(unique).append("\"\n");
			
			return (MethodExecutor) buildInstance(writer,"PostExecutor" + unique,result.toString(),urlClassLoader)
									.getConstructor(AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,Object.class)
									.newInstance(forPath,null,forRequestHeader,forResponseHeader,instance);
		} catch (VerifyError err) {
			err.printStackTrace();
			throw new ContentException("Error verifying code: "+err.getLocalizedMessage()+"\n compiled content is:\n"+result.toString());
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException exc) {
			exc.printStackTrace();
			throw new ContentException(exc);
		}		
	}

	MethodExecutor buildPutMethodExecutor(final Object instance, final Method m, final String path, final MimeType[] source, final MimeType[] target) throws SyntaxException, ContentException, IOException {
		final String[]		pathNames = collectPathParameters(m)
							, requestHeaderNames = collectRequestParameters(m)
							, responseHeaderNames = collectResponseParameters(m);
		final AnyKindParserAndSetter forPath = pathNames.length > 0 ? findOrBuildPathParser(path.startsWith("/") ? path.substring(1) : path) : null
							, forRequestHeader = requestHeaderNames.length > 0 ? findOrBuildRequestHeadParameters(requestHeaderNames) : null
							, forResponseHeader = responseHeaderNames.length > 0 ? findOrBuildResponseHeadParameters(responseHeaderNames,collectResponseParameterClasses(m)) : null;
		final int			unique = uniqueNumber.incrementAndGet(), totalParameters = pathNames.length + requestHeaderNames.length; 
		
		final StringBuilder	result = new StringBuilder();
		
		try{result.append("	makeIncludes \"").append(m.getDeclaringClass().getCanonicalName()).append("\"\n")
						  .append(collectAdditionalImports(m))
						  .append(" getExecutorHead \"").append(m.getDeclaringClass().getCanonicalName()).append("\",\"PostExecutor").append(unique).append('\"')
						  		.append(",pathParsed=").append(pathNames.length)
						  		.append(",queryParsed=0")
						  		.append(",requestHeadParsed=").append(requestHeaderNames.length)
						  		.append(",totalResponsed=").append(responseHeaderNames.length).append('\n');
		
			for (Parameter item : m.getParameters()) {
				if (item.isAnnotationPresent(FromPath.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromPath.class).value(),pathNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(FromQuery.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromQuery.class).value(),pathNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(FromHeader.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromHeader.class).value(),pathNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(ToHeader.class)) {
					result.append(buildToConvertor(item.getType(),defineParameterIndex(item.getAnnotation(ToHeader.class).value(),responseHeaderNames)));
				}
				else if (item.isAnnotationPresent(ToBody.class)) {
					try{result.append(buildToBodyParameterText(item.getType(),MimeType.parseMimeList(item.getAnnotation(ToBody.class).mimeType())[0]));
					} catch (MimeParseException exc) {
						throw new ContentException(exc);
					} catch (ContentException exc) {
						throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"], method ["+m.getName()+"], parameter ["+item.getName()+"] (annotated with @toBody): "+exc.getLocalizedMessage()); 
					}		
				}
				else if (item.isAnnotationPresent(FromBody.class)) {
					try{result.append(buildFromBodyParameterText(item.getType(),MimeType.parseMimeList(item.getAnnotation(FromBody.class).mimeType())[0]));
					} catch (MimeParseException exc) {
						throw new ContentException(exc);
					} catch (ContentException exc) {
						throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"], method ["+m.getName()+"], parameter ["+item.getName()+"] (annotated with @toBody): "+exc.getLocalizedMessage()); 
					}		
				}
			}
			
			result.append('\t').append(CompilerUtils.buildMethodCall(m)).append('\n');
			result.append(" getExecutorMethodTail responsePresent="+(responseHeaderNames.length > 0)+"\n");
			result.append(" getExecutorClassTail \"PostExecutor").append(unique).append("\"\n");
			
			return (MethodExecutor) buildInstance(writer,"PostExecutor" + unique,result.toString(),urlClassLoader)
									.getConstructor(AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,Object.class)
									.newInstance(forPath,null,forRequestHeader,forResponseHeader,instance);
		} catch (VerifyError err) {
			err.printStackTrace();
			throw new ContentException("Error verifying code: "+err.getLocalizedMessage()+"\n compiled content is:\n"+result.toString());
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException exc) {
			exc.printStackTrace();
			throw new ContentException(exc);
		}		
	}

	MethodExecutor buildDeleteMethodExecutor(final Object instance, final Method m, final String path, final MimeType[] source, final MimeType[] target) throws SyntaxException, ContentException, IOException {
		final String[]		pathNames = collectPathParameters(m)
							, requestHeaderNames = collectRequestParameters(m)
							, responseHeaderNames = collectResponseParameters(m);
		final AnyKindParserAndSetter forPath = pathNames.length > 0 ? findOrBuildPathParser(path.startsWith("/") ? path.substring(1) : path) : null
							, forRequestHeader = requestHeaderNames.length > 0 ? findOrBuildRequestHeadParameters(requestHeaderNames) : null
							, forResponseHeader = responseHeaderNames.length > 0 ? findOrBuildResponseHeadParameters(responseHeaderNames,collectResponseParameterClasses(m)) : null;
		final int			unique = uniqueNumber.incrementAndGet(), totalParameters = pathNames.length + requestHeaderNames.length; 
		
		final StringBuilder	result = new StringBuilder();
		
		try{result.append("	makeIncludes \"").append(m.getDeclaringClass().getCanonicalName()).append("\"\n")
						  .append(collectAdditionalImports(m))
						  .append(" getExecutorHead \"").append(m.getDeclaringClass().getCanonicalName()).append("\",\"PostExecutor").append(unique).append('\"')
						  		.append(",pathParsed=").append(pathNames.length)
						  		.append(",queryParsed=0")
						  		.append(",requestHeadParsed=").append(requestHeaderNames.length)
						  		.append(",totalResponsed=").append(responseHeaderNames.length).append('\n');
		
			for (Parameter item : m.getParameters()) {
				if (item.isAnnotationPresent(FromPath.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromPath.class).value(),pathNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(FromQuery.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromQuery.class).value(),pathNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(FromHeader.class)) {
					result.append(buildFromConvertor(item.getType(),defineParameterIndex(item.getAnnotation(FromHeader.class).value(),pathNames,requestHeaderNames)));
				}
				else if (item.isAnnotationPresent(ToHeader.class)) {
					result.append(buildToConvertor(item.getType(),defineParameterIndex(item.getAnnotation(ToHeader.class).value(),responseHeaderNames)));
				}
				else if (item.isAnnotationPresent(ToBody.class)) {
					try{result.append(buildToBodyParameterText(item.getType(),MimeType.parseMimeList(item.getAnnotation(ToBody.class).mimeType())[0]));
					} catch (MimeParseException exc) {
						throw new ContentException(exc);
					} catch (ContentException exc) {
						throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"], method ["+m.getName()+"], parameter ["+item.getName()+"] (annotated with @toBody): "+exc.getLocalizedMessage()); 
					}		
				}
				else if (item.isAnnotationPresent(FromBody.class)) {
					throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"], method ["+m.getName()+"], parameter ["+item.getName()+"] (annotated with @toBody): @FromBody annotation is not applicable for the DELETE method parameters"); 
				}
			}
			
			result.append('\t').append(CompilerUtils.buildMethodCall(m)).append('\n');
			result.append(" getExecutorMethodTail responsePresent="+(responseHeaderNames.length > 0)+"\n");
			result.append(" getExecutorClassTail \"PostExecutor").append(unique).append("\"\n");
			
			return (MethodExecutor) buildInstance(writer,"PostExecutor" + unique,result.toString(),urlClassLoader)
									.getConstructor(AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,AnyKindParserAndSetter.class,Object.class)
									.newInstance(forPath,null,forRequestHeader,forResponseHeader,instance);
		} catch (VerifyError err) {
			err.printStackTrace();
			throw new ContentException("Error verifying code: "+err.getLocalizedMessage()+"\n compiled content is:\n"+result.toString());
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException exc) {
			exc.printStackTrace();
			throw new ContentException(exc);
		}		
	}
	
	private static int defineParameterIndex(final String name, final String[]... names) {
		int		totalCount = 0;
		
		for (String[] item : names) {
			for (String current : item) {
				if (name.equals(current)) {
					return totalCount;
				}
				else {
					totalCount++;
				}
			}
		}
		return -1;
	}

	private static String buildFromConvertor(final Class<?> type, final int parameterIndex) throws ContentException {
		if (type.isArray()){
			if (type.getComponentType().isPrimitive()) {
				return " getExecutor2PrimitiveArray \""+type.getComponentType().getSimpleName()+"\",parameterIndex="+parameterIndex+"\n";
			}
			else if (String.class.isAssignableFrom(type.getComponentType())) {
				return " getExecutor2StringArray parameterIndex="+parameterIndex+"\n";
			}
			else {
				throw new ContentException("Array of class ["+type.getCanonicalName()+"] is not supported for path/query/request header parameters"); 
			}
		}
		else if (type.isPrimitive()) {
			return " getExecutor2Primitive \""+type.getComponentType().getSimpleName()+"\",parameterIndex="+parameterIndex+"\n";
		}
		else if (UUID.class.isAssignableFrom(type)) {
			return " getExecutor2UUID parameterIndex="+parameterIndex+"\n";
		}
		else if (String.class.isAssignableFrom(type)) {
			return " getExecutor2String parameterIndex="+parameterIndex+"\n";
		}
		else {
			throw new ContentException("Class ["+type.getCanonicalName()+"] is not supported for path/query/request header parameters"); 
		}
	}

	private static String buildToConvertor(final Class<?> type, final int parameterIndex) throws ContentException {
		if (StringBuilder.class.isAssignableFrom(type)) {
			return " getExecutor2StringBuilderOut parameterIndex="+parameterIndex+"\n";
		}
		else if (List.class.isAssignableFrom(type)) {
			return " getExecutor2ListOut parameterIndex="+parameterIndex+"\n";
		}
		else {
			try{type.getConstructor();
				boolean	hasAnnotation = false;
			
				for (Field f : type.getFields()) {
					if (f.isAnnotationPresent(HeaderName.class)) {
						hasAnnotation = true;
					}
				}
				if (!hasAnnotation) {
					throw new ContentException("Class ["+type.getCanonicalName()+"] doesn't have any public fields annotated with @HeaderName");
				}
				else {
					return " getExecutor2ClassOut \""+type.getCanonicalName()+"\",parameterIndex="+parameterIndex+"\n";
				}
			} catch (NoSuchMethodException | SecurityException e) {
				throw new ContentException("Class ["+type.getCanonicalName()+"] doesn't have public default constructor and can't be used for response header parameters"); 
			}
		}
	}
	
	/*
	 * Collection of methods to build byte code for parsing and setting parameters to the plug-in class
	 */

	static Class<?> buildInstance(final AsmWriter writer, final String className, final String content, final SimpleURLClassLoader wrapper) throws SyntaxException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final Writer			wr = writer.clone(baos)) {
			
				wr.write(content);
			}
			return wrapper.createClass(NanoServiceFactory.class.getPackage().getName()+'.'+className,baos.toByteArray());
		} catch (IOException exc) {
			exc.printStackTrace(); 
			throw new SyntaxException(0,0,"Error building parser/setter for ["+NanoServiceFactory.class.getPackage().getName()+'.'+className+"]: "+exc.getLocalizedMessage(),exc);
		}
	}	
	
	AnyKindParserAndSetter findOrBuildPathParser(final String path) throws SyntaxException {
		final String[]	keys = CharUtils.split(path,'/');
				
		synchronized(pathCache) {
			if (pathCache.contains(keys)) {
				return pathCache.get(keys);
			}
			else {
				final PathParser	pp = buildPathParser(uniqueNumber.incrementAndGet(), path);
				
				pathCache.add(pp,keys);
				return pp;
			}
		}
	}

	PathParser buildPathParser(final int uniqueNumber, final String path) throws SyntaxException {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path parameter can't be null or empty string");
		}
		else {
			final String[]	parts = CharUtils.split(path,'/');
			
			if (parts.length == 0) {
				throw new SyntaxException(0,0,"Path ["+path+"] is empty");
			}
			else {
				int		asteriskCount = 0;
				
				for (String item : parts) {
					if ("*".equals(item)) {
						asteriskCount++;
					}
				}
				if (asteriskCount > 1) {
					throw new SyntaxException(0,0,"Path ["+path+"] contains more than one asterisk");
				}
				else if (asteriskCount == 1 && !"*".equals(parts[parts.length-1])) {
					throw new SyntaxException(0,0,"Path ["+path+"]: astrisk should be the same last sign in the path only");
				}
				else {
					try{return (PathParser)buildInstance(writer,"PathParser" + uniqueNumber,buildPathParserText(uniqueNumber,parts),urlClassLoader).newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new SyntaxException(0,0,"Error instantiating path parser for ["+path+"]:"+e.getLocalizedMessage());
					}
				}
			}
		}
	}

	AnyKindParserAndSetter findOrBuildQueryParameters(final String[] queryNames) throws SyntaxException {
		final String[]	keys = new String[queryNames.length];
		
		for (int index = 0, maxIndex = keys.length; index < maxIndex; index++) {
			keys[index] = queryNames[index] + '=';
		}
		
		Arrays.sort(keys);
		synchronized(queryCache) {
			if (queryCache.contains(keys)) {
				return queryCache.get(keys);
			}
			else {
				final QueryParser	qp = buildQueryParser(uniqueNumber.incrementAndGet(),keys);
				
				queryCache.add(qp,keys);
				return qp;
			}
		}
	}

	QueryParser buildQueryParser(final int uniqueNumber, final String[] parameters) throws SyntaxException {
		if (parameters == null || parameters.length == 0) {
			throw new IllegalArgumentException("Parameters can't be null or empty array");
		}
		else {
			for (int index = 0; index < parameters.length; index++) {
				if (parameters[index] == null || parameters[index].isEmpty()) {
					throw new IllegalArgumentException("Parameter in the array at index ["+index+"] is null or empty!");
				}
				else if (parameters[index].length() <= 1 || !parameters[index].endsWith("=")) {
					throw new IllegalArgumentException("Parameter ["+parameters[index]+"] in the array at index ["+index+"] is too short or doesn't end with the (=) sign");
				}
			}
			try{return (QueryParser)buildInstance(writer,"QueryParser" + uniqueNumber,buildQueryParserText(uniqueNumber,parameters),urlClassLoader).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new SyntaxException(0,0,"Error instantiating query parser for ["+Arrays.toString(parameters)+"]:"+e.getLocalizedMessage());
			}
		}
	}

	private AnyKindParserAndSetter findOrBuildRequestHeadParameters(final String[] requestHeaderNames) throws SyntaxException {
		final String[]	keys = requestHeaderNames.clone();
		
		Arrays.sort(keys);
		synchronized(requestHeaderCache) {
			if (requestHeaderCache.contains(keys)) {
				return requestHeaderCache.get(keys);
			}
			else {
				final RequestHeadParser	rhp = buildRequestHeadParser(uniqueNumber.incrementAndGet(),keys);
				
				requestHeaderCache.add(rhp,keys);
				return rhp;
			}
		}
	}

	RequestHeadParser buildRequestHeadParser(final int uniqueNumber, final String[] parameters) throws SyntaxException {
		if (parameters == null || parameters.length == 0) {
			throw new IllegalArgumentException("Parameters can't be null or empty array");
		}
		else {
			for (int index = 0; index < parameters.length; index++) {
				if (parameters[index] == null || parameters[index].isEmpty()) {
					throw new IllegalArgumentException("Parameter in the array at index ["+index+"] is null or empty!");
				}
			}
			try{return (RequestHeadParser)buildInstance(writer,"RequestParser" + uniqueNumber,buildRequestHeadParserText(uniqueNumber,parameters),urlClassLoader).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new SyntaxException(0,0,"Error instantiating request head  parser for ["+Arrays.toString(parameters)+"]:"+e.getLocalizedMessage());
			}
		}
	}

	private AnyKindParserAndSetter findOrBuildResponseHeadParameters(final String[] responseHeaderNames, final Class<?>[] responseHeaderClasses) throws SyntaxException {
		final String[]	keys = responseHeaderNames.clone();
		
		Arrays.sort(keys);
		synchronized(responseHeaderCache) {
			if (responseHeaderCache.contains(keys)) {
				return responseHeaderCache.get(keys);
			}
			else {
				final ResponseHeadSetter	rhp = buildResponseHeadSetter(uniqueNumber.incrementAndGet(),responseHeaderNames,responseHeaderClasses);
				
				responseHeaderCache.add(rhp,keys);
				return rhp;
			}
		}
	}

	ResponseHeadSetter buildResponseHeadSetter(final int uniqueNumber, final String[] parameters, final Class<?>[] parameterClasses) throws SyntaxException {
		if (parameters == null || parameters.length == 0) {
			throw new IllegalArgumentException("Parameters can't be null or empty array");
		}
		else if (parameterClasses == null || parameterClasses.length == 0) {
			throw new IllegalArgumentException("Parameter classes can't be null or empty array");
		}
		else {
			try{return (ResponseHeadSetter)buildInstance(writer,"ResponseSetter" + uniqueNumber,buildResponseHeadSetterText(uniqueNumber,parameters,parameterClasses),urlClassLoader).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new SyntaxException(0,0,"Error instantiating response setter for ["+Arrays.toString(parameters)+"]:"+e.getLocalizedMessage());
			}
		}
	}

	/*
	 * methods to build different parts of calling environment
	 */

	static String buildPathParserText(final int uniqueNumber, final String[] parts) throws SyntaxException {
		final StringBuilder	result = new StringBuilder();
		
		result.append("	makePathIncludes\n").append(" getPathParserClassHead \"PathParser").append(uniqueNumber).append("\"\n");

		for (int index = 0; index < parts.length; index++) {
			if (!parts[index].contains("{") && !"*".equals(parts[index])) {
				result.append("compare").append(index).append(" .field char[] final\n");
			}
		}
		result.append(" getPathParserConstructorHead \"PathParser").append(uniqueNumber).append("\"\n");
		for (int index = 0; index < parts.length; index++) {
			if (!parts[index].contains("{") && !"*".equals(parts[index])) {
				result.append(" preparePathParserClass ").append(index).append(",\"").append(parts[index]).append("\"\n");
			}
		}
		result.append(" getPathParserConstructorTail \"PathParser").append(uniqueNumber).append("\"\n").append(" getPathParserMethodHead\n");
		
		for (int index = 0; index < parts.length - 1; index++) {
			if (parts[index].startsWith("{")) {
				result.append(" getPathParserExtractValue\n");
			}
			else {
				result.append(" getPathParserCompareConstantPath ").append(index).append(",").append(parts[index].length()).append('\n');
			}
			result.append(" getPathParserCompareSlash\n");
		}
		if (parts[parts.length - 1].startsWith("{")) {
			result.append(" getPathParserExtractValue\n");
		}
		else if ("*".equals(parts[parts.length - 1])) {
			result.append(" getPathParserExtractTail\n");
		}
		else {
			result.append(" getPathParserCompareConstantPath ").append(parts.length - 1).append(',').append(parts[parts.length - 1].length()).append('\n');
		}
		
		result.append(" getPathParserMethodTail\n").append(" getPathParserClassTail \"PathParser").append(uniqueNumber).append("\"\n");
		return result.toString();
	}
	
	static String buildQueryParserText(final int uniqueNumber, final String[] parameters) throws SyntaxException {
		final StringBuilder	result = new StringBuilder();
		
		result.append(" makeQueryIncludes\n").append(" getQueryParserClassHead \"QueryParser").append(uniqueNumber).append("\",parameterCount=").append(parameters.length).append('\n')
			  .append(" getQueryParserConstructorHead \"QueryParser").append(uniqueNumber).append("\",parameterCount=").append(parameters.length).append('\n'); 
		for (int index = 0; index < parameters.length; index++) {
			result.append(" prepareQueryParserClass ").append(index).append(",\"").append(parameters[index]).append("\"\n");
		}
		result.append(" getQueryParserConstructorTail \"QueryParser").append(uniqueNumber).append("\"\n")
			  .append(" getQueryParserMethod cleans=").append(parameters.length).append('\n')
			  .append(" getQueryParserClassTail \"QueryParser").append(uniqueNumber).append("\"\n");
		return result.toString();
	}
	
	static String buildRequestHeadParserText(final int uniqueNumber, final String[] parameters) throws SyntaxException {
		final StringBuilder	result = new StringBuilder();
		
		result.append(" makeRequestIncludes\n").append(" getRequestParserClassHead \"RequestParser").append(uniqueNumber).append("\",parameterCount=").append(parameters.length).append('\n')
			  .append(" getRequestParserConstructorHead \"RequestParser").append(uniqueNumber).append("\",parameterCount=").append(parameters.length).append('\n');
		for (int index = 0; index < parameters.length; index++) {
			result.append(" prepareRequestParserClass ").append(index).append(",\"").append(parameters[index]).append("\"\n");
		}
		result.append(" getRequestParserConstructorTail \"RequestParser").append(uniqueNumber).append("\"\n") 
		 		.append(" getRequestParserMethod parameterCount=").append(parameters.length).append('\n')
				.append(" getRequestParserClassTail \"RequestParser").append(uniqueNumber).append("\"\n");
		return result.toString();
	}
	
	
	static String buildResponseHeadSetterText(final int uniqueNumber, final String[] parameters, final Class<?>[] parameterClasses) throws SyntaxException {
		final Set<Class<?>>		simpleClasses = new HashSet<>();
		final List<Class<?>>	orderedClasses = new ArrayList<>();
		final Set<String>		names = new HashSet<>();
		final String[]			tempParameters = parameters.clone();
		
		for (int index = 0; index < parameterClasses.length; index++) {
			if (parameterClasses[index] == null) {
				throw new IllegalArgumentException("Parameter class in the array at index ["+index+"] is null!");
			}
		}
		for (int index = 0; index < parameterClasses.length; index++) {
			final Class<?>	item = parameterClasses[index];
			
			if (item.isPrimitive() || item.isArray() || item.isEnum()) {
				throw new IllegalArgumentException("Parameter class in the array at index ["+index+"] is illegal (it can't be array, primitive type or enumeration)!");
			}
			else if (item == StringBuilder.class) {
				if (parameters[index] == null || parameters[index].isEmpty()) {
					throw new IllegalArgumentException("Parameter in the array at index ["+index+"] is null or empty!");
				}
				else {
					names.add(parameters[index]);
					simpleClasses.add(StringBuilder.class);
					orderedClasses.add(StringBuilder.class);
				}
			}
			else if (List.class.isAssignableFrom(item)) {
				if (parameters[index] == null || parameters[index].isEmpty()) {
					throw new IllegalArgumentException("Parameter in the array at index ["+index+"] is null or empty!");
				}
				else {
					names.add(parameters[index]);
					simpleClasses.add(ArrayList.class);
					orderedClasses.add(ArrayList.class);
				}
			}
			else {
				final StringBuilder	sb = new StringBuilder(); 
				boolean				isAnyFieldAnnotated = false;
				
				for (Field f : item.getDeclaredFields()) {
					if (f.isAnnotationPresent(HeaderName.class)) {
						names.add(f.getAnnotation(HeaderName.class).value());
						isAnyFieldAnnotated = true;
						sb.append(',').append(f.getAnnotation(HeaderName.class).value());
					}
				}
				if (!isAnyFieldAnnotated) {
					throw new IllegalArgumentException("Parameter class in the array at index ["+index+"] doesn't contain any @HeaderName annotations!");
				}
				else {
					tempParameters[index] = sb.toString().substring(1);
					simpleClasses.add(item);
					orderedClasses.add(item);
				}
			}
		}
		
		final StringBuilder	result = new StringBuilder();
		final StringBuilder	additionalClasses = new StringBuilder();

		for (Class<?> item : simpleClasses) {
			additionalClasses.append(',').append(item.getCanonicalName());
		}
		result.append("	makeResponseIncludes \"").append(additionalClasses.toString().substring(1)).append("\"\n")
			  .append(" getResponseSetterClassHead \"ResponseSetter").append(uniqueNumber).append("\",parameterCount=").append(parameters.length).append('\n')
			  .append(" getResponseSetterConstructorHead \"ResponseSetter").append(uniqueNumber).append("\",parameterCount=").append(parameters.length).append('\n');
		for (int index = 0; index < parameters.length; index++) {
			result.append(" prepareResponseSetterClass \"").append(tempParameters[index]).append("\",size=")
				  .append(CharUtils.split(tempParameters[index],',').length).append(",fieldIndex=").append(index).append('\n');
		}
		result.append(" getResponseSetterConstructorTail \"ResponseSetter").append(uniqueNumber).append("\"\n");

		additionalClasses.setLength(0);
		for (Class<?> item : orderedClasses) {
			additionalClasses.append(',').append(item.getCanonicalName());
		}
		result.append(" getResponseSetterPreparator \"").append(additionalClasses.toString().substring(1)).append("\",size=").append(orderedClasses.size()).append('\n')
			  .append(" getResponseSetterCommitterHead \"").append(additionalClasses.toString().substring(1)).append("\",size=").append(orderedClasses.size()).append('\n');
		for (int index = 0, maxIndex = orderedClasses.size(); index < maxIndex; index++) {
			if (StringBuilder.class.isAssignableFrom(orderedClasses.get(index))) {
				result.append(" getResponseSetterCommitterSB parameterIndex=").append(index).append('\n');
			}
			else if (List.class.isAssignableFrom(orderedClasses.get(index))) {
				result.append(" getResponseSetterCommitterList parameterIndex=").append(index).append('\n');
			}
			else {
				int	nameIndex = 0;
				
				result.append(" getResponseSetterCommitterClassStart parameterIndex=").append(index).append('\n');
				for (Field f : orderedClasses.get(index).getFields()) {
					if (f.isAnnotationPresent(HeaderName.class)) {
						result.append(" getResponseSetterCommitterClassStore \"").append(f.getDeclaringClass().getCanonicalName()).append("\",\"").append(f.getName())
							  .append("\",parameterIndex=").append(index).append(",nameIndex=").append(nameIndex).append(",type=\"").append(f.getType().getCanonicalName()).append("\"\n");
						nameIndex++;
					}
				}
				result.append(" getResponseSetterCommitterClassEnd\n");
			}
		}
		result.append(" getResponseSetterCommitterTail size=").append(orderedClasses.size()).append('\n')
			  .append(" getResponseSetterClassTail \"ResponseSetter").append(uniqueNumber).append("\"\n");
		return result.toString();
	}
	
	static String buildFromBodyParameterText(Class<?> itemType, MimeType mimeType) throws ContentException {
		final StringBuilder	sb = new StringBuilder();
		
		if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_PLAIN_TEXT,mimeType)) {
			if (String.class.isAssignableFrom(itemType)) {
				sb.append(" PostString2Stack\n");
			}
			else if (CharacterSource.class.isAssignableFrom(itemType)) {
				sb.append(" PostCharacterSource2Stack\n");
			}
			else if (Reader.class.isAssignableFrom(itemType)) {
				sb.append(" PostReader2Stack\n");
			}
			else if (InputStream.class.isAssignableFrom(itemType)) {
				sb.append(" PostInputStream2Stack\n");
			}
			else {
				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. Only OutputStream, CreoleWriter, Writer, StringBuilder and CharacterTarget are available");
			}
		}
		else if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_XML_TEXT,mimeType)) {
			if (String.class.isAssignableFrom(itemType)) {
				sb.append(" PostString2Stack\n");
			}
			else if (CharacterSource.class.isAssignableFrom(itemType)) {
				sb.append(" PostCharacterSource2Stack\n");
			}
			else if (Reader.class.isAssignableFrom(itemType)) {
				sb.append(" PostReader2Stack\n");
			}
			else if (InputStream.class.isAssignableFrom(itemType)) {
				sb.append(" PostInputStream2Stack\n");
			}
			else if (Document.class.isAssignableFrom(itemType)) {
				sb.append(" PostDocument2Stack\n");
			}
			else if (XMLStreamReader.class.isAssignableFrom(itemType)) {
				sb.append(" PostXMLStreamReader2Stack\n");
			}
			else {
				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. Only OutputStream, CreoleWriter, Writer, StringBuilder and CharacterTarget are available");
			}
		}
		else if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_JSON_TEXT,mimeType)) {
			if (String.class.isAssignableFrom(itemType)) {
				sb.append(" PostString2Stack\n");
			}
			else if (CharacterSource.class.isAssignableFrom(itemType)) {
				sb.append(" PostCharacterSource2Stack\n");
			}
			else if (Reader.class.isAssignableFrom(itemType)) {
				sb.append(" PostReader2Stack\n");
			}
			else if (InputStream.class.isAssignableFrom(itemType)) {
				sb.append(" PostInputStream2Stack\n");
			}
			else if (JsonStaxParser.class.isAssignableFrom(itemType)) {
				sb.append(" PostJsonStaxParser2Stack\n");
			}
			else if (!itemType.isArray() && !itemType.isPrimitive() && !itemType.isEnum()) {
				sb.append(" PostJsonSerializable2Stack \""+itemType.getCanonicalName()+"\",\""+itemType.getSimpleName()+"\"\n");
			}
			else {
				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. It's type should not be array, primitive type or enumeration");
			}
		}
//		else if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_OCTET_STREAM, mimeType)) {
//			if (InputStream.class.isAssignableFrom(itemType)) {
//				sb.append(" PostInputStream2Stack\n");
//			}
//			else {
//				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. Only InputStream can be used");
//			}
//		}
		else if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_MULTIPART_FORM, mimeType)) {
			if (InputStream.class.isAssignableFrom(itemType)) {
				sb.append(" PostInputStream2Stack\n");
			}
			else if (MultipartContent.class.isAssignableFrom(itemType)) {
				sb.append(" PostMultipartContent2Stack\n");
			}
			else {
				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. Only InputStream can be used");
			}
		}
		else if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_ANY_STREAM, mimeType)) {
			if (InputStream.class.isAssignableFrom(itemType)) {
				sb.append(" PostInputStream2Stack\n");
			}
			else {
				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. Only InputStream can be used");
			}
		}
		else {
			throw new ContentException("Unsupported combination of parameter type ["+itemType.getCanonicalName()+"] and annotated MIME type ["+mimeType+"]");
		}
		return sb.toString();
	}
	
	static String buildToBodyParameterText(final Class<?> itemType, final MimeType mimeType) throws ContentException {
		final StringBuilder	sb = new StringBuilder();
		
		if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_PLAIN_TEXT,mimeType)) {
			if (StringBuilder.class.isAssignableFrom(itemType)) {
				sb.append(" StringBuilder2Stack\n");
			}
			else if (CharacterTarget.class.isAssignableFrom(itemType)) {
				sb.append(" CharacterTarget2Stack\n");
			}
			else if (CreoleWriter.class.isAssignableFrom(itemType)) {
				sb.append(" CreoleWriter2Stack \"XML2TEXT\"\n");
			}
			else if (Writer.class.isAssignableFrom(itemType)) {
				sb.append(" Writer2Stack\n");
			}
			else if (OutputStream.class.isAssignableFrom(itemType)) {
				sb.append(" OutputStream2Stack\n");
			}
			else {
				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. Only OutputStream, CreoleWriter, Writer, StringBuilder and CharacterTarget are available");
			}
		}
		else if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_HTML_TEXT,mimeType)) {
			if (StringBuilder.class.isAssignableFrom(itemType)) {
				sb.append(" StringBuilder2Stack\n");
			}
			else if (CharacterTarget.class.isAssignableFrom(itemType)) {
				sb.append(" CharacterTarget2Stack\n");
			}
			else if (CreoleWriter.class.isAssignableFrom(itemType)) {
				sb.append(" CreoleWriter2Stack \"XML2HTML\"\n");
			}
			else if (Writer.class.isAssignableFrom(itemType)) {
				sb.append(" Writer2Stack\n");
			}
			else if (OutputStream.class.isAssignableFrom(itemType)) {
				sb.append(" OutputStream2Stack\n");
			}
			else {
				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. Only OutputStream, CreoleWriter, Writer, StringBuilder and CharacterTarget are available");
			}
		}
		else if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_XML_TEXT,mimeType)) {
			if (StringBuilder.class.isAssignableFrom(itemType)) {
				sb.append(" StringBuilder2Stack\n");
			}
			else if (CharacterTarget.class.isAssignableFrom(itemType)) {
				sb.append(" CharacterTarget2Stack\n");
			}
			else if (Document.class.isAssignableFrom(itemType)) {
				sb.append(" Document2Stack\n");
			}
			else if (XMLStreamWriter.class.isAssignableFrom(itemType)) {
				sb.append(" XMLStreamWriter2Stack\n");
			}
			else if (CreoleWriter.class.isAssignableFrom(itemType)) {
				sb.append(" CreoleWriter2Stack \"XML\"\n");
			}
			else if (Writer.class.isAssignableFrom(itemType)) {
				sb.append(" Writer2Stack\n");
			}
			else if (OutputStream.class.isAssignableFrom(itemType)) {
				sb.append(" OutputStream2Stack\n");
			}
			else {
				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. Only OutputStream, CreoleWriter, Writer, StringBuilder and CharacterTarget are available");
			}
		}
		else if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_JSON_TEXT,mimeType)) {
			if (StringBuilder.class.isAssignableFrom(itemType)) {
				sb.append(" StringBuilder2Stack\n");
			}
			else if (CharacterTarget.class.isAssignableFrom(itemType)) {
				sb.append(" CharacterTarget2Stack\n");
			}
			else if (JsonStaxPrinter.class.isAssignableFrom(itemType)) {
				sb.append(" JsonStaxPrinter2Stack\n");
			}
			else if (Writer.class.isAssignableFrom(itemType)) {
				sb.append(" Writer2Stack\n");
			}
			else if (OutputStream.class.isAssignableFrom(itemType)) {
				sb.append(" OutputStream2Stack\n");
			}
			else if (!itemType.isArray() && !itemType.isPrimitive() && !itemType.isEnum()) {
				sb.append(" JsonSerializable2Stack \""+itemType.getCanonicalName()+"\",\""+itemType.getSimpleName()+"\"\n");
			}
			else {
				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. It's type should not be array, primitive type or enumeration");
			}
		}
		else if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_ANY_IMAGE, mimeType)) {
			if (OutputStream.class.isAssignableFrom(itemType)) {
				sb.append(" OutputStream2Stack\n");
			}
			else {
				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. Only OutputStream can be used");
			}
		}
		else if (InternalUtils.mimesAreCompatible(PureLibSettings.MIME_ANY_STREAM, mimeType)) {
			if (OutputStream.class.isAssignableFrom(itemType)) {
				sb.append(" OutputStream2Stack\n");
			}
			else {
				throw new ContentException("Requested MIME ["+mimeType+"] is not compatible with parameter type ["+itemType.getCanonicalName()+"]. Only OutputStream can be used");
			}
		}
		else {
			InternalUtils.mimesAreCompatible(PureLibSettings.MIME_ANY_STREAM, mimeType);
			throw new ContentException("Unsupported combination of parameter type ["+itemType.getCanonicalName()+"] and annotated MIME type ["+mimeType+"]");
		}
		return sb.toString();
	}

	/*
	 * Utility methods
	 */

	private static String collectAdditionalImports(final Method m) throws ContentException, IOException {
		final StringBuilder	sb = new StringBuilder("; ------- Additional imports\n");
		
		for (Parameter parm : m.getParameters()) {
			try{if (parm.isAnnotationPresent(FromBody.class) && InternalUtils.mimesAreCompatible(InternalUtils.buildMime(parm.getAnnotation(FromBody.class).mimeType()),PureLibSettings.MIME_JSON_TEXT) && !inList(parm.getType(),EXCLUDE_CLASSES_4_JSON)) {
					try{if (!Modifier.isPublic(parm.getType().getConstructor().getModifiers())) {
							throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"] method ["+m.getName()+"] parameter ["+parm.getName()+"] type ["+parm.getType().getCanonicalName()+"] doesnt't have default public constructor to create"); 
						}
						else {
							sb.append(" .import ").append(parm.getType().getCanonicalName()).append('\n');
						}
					} catch (NoSuchMethodException | SecurityException e) {
						throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"] method ["+m.getName()+"] parameter ["+parm.getName()+"] type ["+parm.getType().getCanonicalName()+"] doesnt't have default public constructor to create"); 
					}
				}
				else if (parm.isAnnotationPresent(ToBody.class) && InternalUtils.mimesAreCompatible(InternalUtils.buildMime(new String[]{parm.getAnnotation(ToBody.class).mimeType()}),PureLibSettings.MIME_JSON_TEXT) && !inList(parm.getType(),EXCLUDE_CLASSES_4_JSON)) {
					try{if (!Modifier.isPublic(parm.getType().getConstructor().getModifiers())) {
							throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"] method ["+m.getName()+"] parameter ["+parm.getName()+"] type ["+parm.getType().getCanonicalName()+"] doesnt't have default public constructor to create"); 
						}
						else {
							sb.append(" .import ").append(parm.getType().getCanonicalName()).append('\n');
						}
					} catch (NoSuchMethodException | SecurityException e) {
						throw new ContentException("Class ["+m.getDeclaringClass().getCanonicalName()+"] method ["+m.getName()+"] parameter ["+parm.getName()+"] type ["+parm.getType().getCanonicalName()+"] doesnt't have default public constructor to create"); 
					}
				}
			} catch (MimeParseException e) {
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
		return sb.toString();
	}
	
	private static String[] collectPathParameters(final Method m) {
		final List<String>	result = new ArrayList<>();
		
		for (Parameter item : m.getParameters()) {
			if (item.isAnnotationPresent(FromPath.class)) {
				result.add(item.getAnnotation(FromPath.class).value());
			}
		}
		return result.toArray(new String[result.size()]);
	}

	private static String[] collectQueryParameters(final Method m) {
		final List<String>	result = new ArrayList<>();
		
		for (Parameter item : m.getParameters()) {
			if (item.isAnnotationPresent(FromQuery.class)) {
				result.add(item.getAnnotation(FromQuery.class).value());
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	private static String[] collectRequestParameters(final Method m) {
		final List<String>	result = new ArrayList<>();
		
		for (Parameter item : m.getParameters()) {
			if (item.isAnnotationPresent(FromHeader.class)) {
				result.add(item.getAnnotation(FromHeader.class).value());
			}
		}
		return result.toArray(new String[result.size()]);
	}

	private static String[] collectResponseParameters(final Method m) {
		final List<String>	result = new ArrayList<>();
		
		for (Parameter item : m.getParameters()) {
			if (item.isAnnotationPresent(ToHeader.class)) {
				result.add(item.getAnnotation(ToHeader.class).value());
			}
		}
		return result.toArray(new String[result.size()]);
	}

	private static Class<?>[] collectResponseParameterClasses(final Method m) {
		final List<Class<?>>	result = new ArrayList<>();
		
		for (Parameter item : m.getParameters()) {
			if (item.isAnnotationPresent(ToHeader.class)) {
				result.add(item.getType());
			}
		}
		return result.toArray(new Class<?>[result.size()]);
	}
	
	private static String[] collectInputParameters(final Method m) {
		for (Parameter item : m.getParameters()) {
			if (item.isAnnotationPresent(FromBody.class)) {
				return new String[]{item.getAnnotation(FromBody.class).mimeType()};
			}
		}
		return new String[0];
	}
	
	private static void collectAnnotatedMethods(final Class<?> clazz, final List<Method> annotatedMethods) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.isAnnotationPresent(Path.class)) {
				annotatedMethods.add(m);
			}
		}
		if (clazz != Object.class) {
			collectAnnotatedMethods(clazz.getSuperclass(),annotatedMethods);
		}
	}

	private static String defineStreamType(final List<String> list) {
		if (list == null || list.size() == 0) {
			return HEAD_CONTENT_ENCODING_IDENTITY;
		}
		else {
			for (String item : list) {
				switch (item.toLowerCase()) {
					case HEAD_CONTENT_ENCODING_GZIP		: return HEAD_CONTENT_ENCODING_GZIP;
					case HEAD_CONTENT_ENCODING_COMPRESS	: return HEAD_CONTENT_ENCODING_COMPRESS;
				}
			}
			return HEAD_CONTENT_ENCODING_IDENTITY;
		}
	}

	private static boolean inList(final Class<?> toCheck, final List<Class<?>> list) {
		for (Class<?> item : list) {
			if (item.isAssignableFrom(toCheck)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Internal classes 
	 */
	
	private static class SSLConfigurator extends HttpsConfigurator {
		private SSLConfigurator(final SSLContext sslContext) {
		    super(sslContext);  
		}
	
		@Override
		public void configure(final HttpsParameters params) {
		    final SSLContext 	sslContext = getSSLContext();
		    final SSLParameters	sslParams = sslContext.getDefaultSSLParameters();
		    
		    sslParams.setNeedClientAuth(true); 
		    params.setNeedClientAuth(true);  
		    params.setSSLParameters(sslParams);
		}
	}

	public static class AnyKindParserAndSetter {
		protected void throwError(final String format, final int pos, final Object... parameters) throws SyntaxException {
			throw new SyntaxException(0,pos,parameters.length == 0 ? format : String.format(format,parameters));
		}
	}
	
	public abstract static class PathParser extends AnyKindParserAndSetter {
		public static char[] extractName(final char[] path, int from) {
			final int	start = from, len = path.length;
			
			while (from < len && path[from] != '/') {
				from++;
			}
			return Arrays.copyOfRange(path,start,from);
		}
		
		public abstract int parse(final char[] path, final char[][] target, final int from) throws SyntaxException;
	}
	
	public abstract static class QueryParser extends AnyKindParserAndSetter {
		public static int skipParameter(final char[] query, final int from) {
			if (query == null) {
				return 0;
			}
			else {
				for (int index = from, maxIndex = query.length; index < maxIndex; index++) {
					if (query[index] == '=') {
						return index;
					}
				}
				return 0;
			}
		}

		public static int skipValue(final char[] query, final int from) {
			for (int index = from, maxIndex = query.length; index < maxIndex; index++) {
				if (query[index] == '&') {
					return index;
				}
			}
			return query.length;
		}

		public abstract int parse(final char[] query, final char[][] target, final int from) throws SyntaxException;
	}
	
	public abstract static class RequestHeadParser extends AnyKindParserAndSetter {
		public static char[] list2charArray(final List<String> list) {
			int	totalLen = 0, totalCount = 0;
			
			for (String item : list) {
				totalLen += item.length() + 1;
				totalCount++;
			}
			
			final char[]	result = new char[totalLen-1];
			int				index = 0, count = 0, len;
			
			for (String item : list) {
				item.getChars(0, len = item.length(), result, index);
				index += len + 1;
				if (++count != totalCount) {
					result[index-1] = '\n';
				}
			}
			return result;
		}
		
		public abstract int parse(final Headers headers, final char[][] target, final int from) throws SyntaxException;
	}
	
	public abstract static class ResponseHeadSetter extends AnyKindParserAndSetter {
		public static String[] list2StringArray(final List content) {
			final String[]	result = new String[content.size()];
			
			for (int index = 0, maxIndex = result.length; index < maxIndex; index++) {
				result[index] = content.get(index) == null ? null : content.get(index).toString(); 
			}
			return result;
		}
		
		public static String toString(final Object item) {
			return item == null ? "null" : item.toString();
		}
		
		public abstract int prepare(final Object[] instances) throws SyntaxException;
		public abstract int commit(final Headers header, final Object... instances) throws SyntaxException;
	}
	
	static class ClassDescriptor {
		final char[]				rootPath;
		final Class<?>				clazz;
		final MethodDescriptor[]	methods;
		
		ClassDescriptor(final char[] rootPath, final Class<?> clazz, final MethodDescriptor[] methods) {
			this.rootPath = rootPath;
			this.clazz = clazz;
			this.methods = methods;
		}

		@Override
		public String toString() {
			return "ClassDescriptor [clazz=" + clazz + ", methods=" + Arrays.toString(methods) + "]";
		}
	}
	
	static class MethodDescriptor {
		final String			path;
		final char[][]			pathComponents;
		final int				querySet;
		final MimeType[]		source;
		final MimeType[]		target;
		final MethodExecutor	caller;
		
		MethodDescriptor(final String path, final QueryType[] queryType, final MimeType[] source, final MimeType[] target, final MethodExecutor caller) {
			final String[]		components = CharUtils.split(path,'/');
			
			this.path = path;
			this.source = source;
			this.target = target;
			this.caller = caller;
			this.pathComponents = new char[components.length][];

			int		temp = 0;
			for (QueryType item : queryType) {
				temp |= 1 << item.ordinal();
			}
			this.querySet = temp; 
			
			for (int index = 0; index < this.pathComponents.length; index++) {
				this.pathComponents[index] = components[index].toCharArray();
			}
		}

		boolean isAppicable(final QueryType type, final char[] path, final int fromPath, final MimeType sourceType, final MimeType targetType) {
			return canServeQuery(type) && canServePath(path,fromPath) && isCompatibleByMimeTypes(sourceType,targetType);
		}
		
		boolean canServeQuery(final QueryType type) {
			return (querySet & (1 << type.ordinal())) != 0;
		}
		
		boolean canServePath(final char[] path, final int fromPath) {
			int index = 0, maxIndex = pathComponents.length, from = fromPath, maxFrom = path.length; 
			
loop:		for (; index < maxIndex && from < maxFrom; index++) {
				if (UnsafedCharUtils.uncheckedCompare(path,from,pathComponents[index],0,pathComponents[index].length)) {
					from += pathComponents[index].length + 1;
				}
				else if (pathComponents[index][0] == '{') {
					while (from < maxFrom) {
						if (path[from++] == '/') {
							continue loop;
						}
					}
				}
				else if (pathComponents[index][0] == '*') {
					index = maxIndex;
					from = maxFrom;
				}
				else {
					return false;
				}
			}
			return index >= maxIndex && from >= maxFrom;
		}

		boolean isCompatibleByMimeTypes(final MimeType sourceType, final MimeType targetType) {
			boolean	sourceMatch = false, targetMatch = false;
			
			if (sourceType != null) {
				for (MimeType mime : source) {
					if (mime.match(sourceType)) {
						sourceMatch = true;
						break;
					}
				}
			}
			for (MimeType mime : target) {
				if (mime.match(targetType)) {
					targetMatch = true;
					break;
				}
			}
			return (source.length == 0 || sourceMatch) && targetMatch;
		}
		
		@Override
		public String toString() {
			return "MethodDescriptor [path=" + path + ", pathComponents=" + Arrays.toString(pathComponents) + ", queryType=" + querySet + ", source=" + Arrays.toString(source) + ", target=" + Arrays.toString(target) + ", caller=" + caller + "]";
		}
	}

	private static class Loopback implements MethodExecutor {
		Loopback() {
		}

		@Override
		public int execute(final QueryType type, final char[] path, final char[] query, final Headers requestHeaders, final Headers responseHeaders, final InputStream is, final OutputStream os) throws IOException, ContentException, FlowException, EnvironmentException { 
			try{final StringBuilder sbRequest = new StringBuilder(), sbResponse = new StringBuilder();  
			
				responseHeaders.put(HEAD_CONTENT_TYPE, Arrays.asList(new MimeType("text", "html").toString()));
				requestHeaders.forEach((key,value)->{sbRequest.append("<tr><td>").append(key).append("</td><td>").append(value).append("</td></tr>");});
				responseHeaders.forEach((key,value)->{sbResponse.append("<tr><td>").append(key).append("</td><td>").append(value).append("</td></tr>");});
			
				try(final Writer	wr = new OutputStreamWriter(os)) {
					
					wr.write(CharUtils.substitute("loopback.template",Utils.fromResource(this.getClass().getResource("loopback.template")), 
							(key)->{
								switch (key) {
									case "method"		: return type.toString(); 
									case "path"			: return new String(path);
									case "fragment"		: return "???";
									case "query"		: return query == null ? "" : new String(query);
									case "inHeader"		: return sbRequest.toString();
									case "outHeader"	: return sbResponse.toString();
									default 			: return key;
								}
							}
						)
					);
					wr.flush();
				}
				return HttpURLConnection.HTTP_OK;
			} catch (MimeParseException e) {
				return HttpURLConnection.HTTP_INTERNAL_ERROR;
			}
		}
	}

	private static class SimpleHttpServerEnvironment implements NanoServiceEnvironment {
		private final HttpExchange 	handler;
		private final LoggerFacade	facade;
		private final DataSource	dataSource;
		
		SimpleHttpServerEnvironment(final HttpExchange handler, final LoggerFacade facade) {
			if (handler == null) {
				throw new NullPointerException("Exchange handler can't be null");
			}
			else if (facade == null) {
				throw new NullPointerException("Logger facade can't be null");
			}
			else {
				this.handler = handler;
				this.facade = facade;
				this.dataSource = null;
			}
		}

		SimpleHttpServerEnvironment(final HttpExchange handler, final LoggerFacade facade, final DataSource dataSource) {
			if (handler == null) {
				throw new NullPointerException("Exchange handler can't be null");
			}
			else if (facade == null) {
				throw new NullPointerException("Logger facade can't be null");
			}
			else if (dataSource == null) {
				throw new NullPointerException("Datasource can't be null");
			}
			else {
				this.handler = handler;
				this.facade = facade;
				this.dataSource = dataSource;
			}
		}
		
		@Override
		public void success(final int rc) throws IOException {
			handler.sendResponseHeaders(rc,0);
		}
		
		@Override
		public LoggerFacade getLogger() {
			return facade;
		}
		
		@Override
		public Connection getConnection() throws SQLException {
			if (dataSource == null) {
				throw new IllegalStateException("Data source was not passed to the nanoservice. Using of the connections are illegal"); 
			}
			else {
				return dataSource.getConnection();
			}
		}
		
		@Override
		public void fail(final int rc, final String format, final Object... parameters) throws IOException {
			if (format == null) {
				throw new NullPointerException("Format string can't be null");
			}
			else {
				final byte[] 	answer = (parameters == null || parameters.length == 0 ? format : String.format(format,parameters)).getBytes("UTF-8");
				
				handler.getResponseHeaders().add(HEAD_CONTENT_TYPE, PureLibSettings.MIME_PLAIN_TEXT.toString());
				handler.getResponseHeaders().add(HEAD_CONTENT_LENGTH, String.valueOf(answer.length));
				handler.getResponseHeaders().add(HEAD_CONTENT_ENCODING, HEAD_CONTENT_ENCODING_IDENTITY);
				handler.sendResponseHeaders(rc,0);
				try(final OutputStream	os = handler.getResponseBody()) {
					os.write(answer);
					os.flush();
				}
			}
		}
	};					
}
