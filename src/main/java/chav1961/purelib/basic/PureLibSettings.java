package chav1961.purelib.basic;


import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.MimeParseException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.FileSystemURLStreamHandler;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.monitoring.MonitoringManager;
import chav1961.purelib.monitoring.NanoServiceControl;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.sql.content.ResultSetFactory;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.ui.ColorScheme;
import chav1961.purelib.ui.swing.SwingUtils;


/**
 * <p>This class keeps basic settings for the Pure Library. You can use default settings for the Pure Library environment (file <b>purelib.default.properties</b> inside the purelib jar) or
 * can import explicit settings by the <b>-D{@value #SETTINGS_KEY}</b> variable when starting java application. This class must be used by it's singleton instance by calling {@linkplain #instance()}
 * static method. Content got from <b>-D{@value #SETTINGS_KEY}</b> source will be joined with the default settings from <b>purelib.default.properties</b> and has higher priority related to <b>purelib.default.properties</b>.
 * This class also contains few static useful methods to process settings</p>
 * <p>Inner logger in the class is used to print out any internal problems in the Pure Library. It uses standard {@linkplain Logger} functionality. Logger name is <b>"chav1961.purelib"</b>. You can
 * control it by the standard Java logging mechanism</p>
 * <p>Inner {@linkplain Timer} in the class is used to process different sorts of maintenance, required by Pure Library components. Timer works with a daemon thread and needs no action on application terminating</p>    
 * 
 * @see SubstitutableProperties
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.6
 */

public final class PureLibSettings {
	private static final SubstitutableProperties	DEFAULTS = new SubstitutableProperties(System.getProperties()); 
	private static final SubstitutableProperties	PROPS = new SubstitutableProperties(DEFAULTS);

	/**
	 * <p>This enumeration defines current OS for application</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6
	 */
	public static enum CurrentOS {
		WINDOWS, LINUX, MACOS, UNKNOWN
	}

	public static final CurrentOS	CURRENT_OS;
	
	/**
	 * <p>Pure library MBean entry name</p>
	 */
	public static final String		PURELIB_MBEAN = "chav1961.purelib";

	/**
	 * <p>This logger is used to print any internal problems in the Pure Library</p>
	 */
	public static final Logger		logger = Logger.getLogger("chav1961.purelib");
	
	/**
	 * <p>Current Java process id</p>
	 */
	public static final long		CURRENT_PID = getCurrentPID();
	
	/**
	 * <p>This is current version of the Pure Library</p>
	 */
	public static final String		CURRENT_VERSION = "0.0.5";

	/**
	 * <p>This is a vendor of the Pure Library</p>
	 */
	public static final String		VENDOR = "A.Chernomyrdin aka chav1961";
	
	/**
	 * <p>This is <b>-D</b> variable name to import content to the settings repository</p>
	 */
	public static final String		SETTINGS_KEY = "purelib.settings.source";

	/**
	 * <p>This is a key name to use test database connection driver</p>
	 */
	public static final String		TEST_CONNECTION_DRIVER = "purelib.test.connection.driver";
	
	/**
	 * <p>This is a key name to use test database connection string</p>
	 */
	public static final String		TEST_CONNECTION_URI = "purelib.test.connection.uri";
	
	/**
	 * <p>This is a key name to use test database connection user</p>
	 */
	public static final String		TEST_CONNECTION_USER = "purelib.test.connection.user";
	
	/**
	 * <p>This is a key name to use test database connection password</p>
	 */
	public static final String		TEST_CONNECTION_PASSWORD = "purelib.test.connection.password";
	
	/**
	 * <p>This is a key to "Print expanded macros of the AsmWriter" for debugging purposes. Default for the given property is <b>false</b></p>
	 */
	public static final String		SUPPRESS_PRINT_ASSEMBLER = "purelib.settings.assembler.printSuppress";
	
	/**
	 * <p>This is a key to "Print expanded macros of the AsmWriter" for debugging purposes. Default for the given property is <b>false</b></p>
	 */
	public static final String		PRINT_EXPANDED_MACROS = "purelib.settings.macros.printExpanded";

	/**
	 * <p>This is a key to "Print expanded macros of the AsmWriter" for debugging purposes. Default for the given property is <b>false</b></p> 
	 */
	public static final String		HTTP_SERVER_PORT = "purelib.settings.help.port";
	
	/**
	 * <p>This is a key to "Show tooltip" for debugging purposes ("standard" and "advanced" are available). Default for the given property is <b>standard</b></p>
	 */
	public static final String		SWING_TOOLTIP_MODE = "purelib.settings.ui.swing.tooptip.mode";

	/**
	 * <p>This is a key to "Show tooltip" initial delay for debugging purposes</p>
	 */
	public static final String		SWING_TOOLTIP_MODE_INITIAL_DELAY = "purelib.settings.ui.swing.tooptip.initialDelay";

	/**
	 * <p>This is a key to "Show tooltip" dismiss delay for debugging purposes</p>
	 */
	public static final String		SWING_TOOLTIP_MODE_DISMISS_DELAY = "purelib.settings.ui.swing.tooptip.dismissDelay";
	
	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for plain text</p>
	 */
	public static final MimeType	MIME_PLAIN_TEXT = buildMime("text","plain");

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for CREOLE text</p>
	 */
	public static final MimeType	MIME_CREOLE_TEXT = buildMime("text","x-wiki.creole");

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for HTML text</p>
	 */
	public static final MimeType	MIME_HTML_TEXT = buildMime("text","html");

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for XML text</p>
	 */
	public static final MimeType	MIME_XML_TEXT = buildMime("text","xml");

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for JSON</p>
	 */
	public static final MimeType	MIME_JSON_TEXT = buildMime("application","json");

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for CSS</p>
	 */
	public static final MimeType	MIME_CSS_TEXT = buildMime("text","css");

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for favicon content</p>
	 */
	public static final MimeType	MIME_FAVICON = buildMime("image","webp");

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for *.png content</p>
	 */
	public static final MimeType	MIME_PNG = buildMime("image","x-png");

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for any image content</p>
	 */
	public static final MimeType	MIME_ANY_IMAGE = buildMime("image","*");

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for html form content</p>
	 */
	public static final MimeType	MIME_FORM_URLENCODED = buildMime("application","x-www-form-urlencoded");

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for html multipart form content</p>
	 */
	public static final MimeType	MIME_MULTIPART_FORM = buildMime("multipart","form-data");
	
	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for octet stream</p>
	 */
	public static final MimeType	MIME_OCTET_STREAM = buildMime("application","octet-stream");

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for any stream</p>
	 */
	public static final MimeType	MIME_ANY_STREAM = buildMime("application","*");
	
	/**
	 * <p>Predefined Data Flavor name for Pure Library models to use them in the Swing applications</p>
	 * @see chav1961.purelibrary.model
	 */	
	public static final String		MODEL_DATA_FLAVOR_NAME = "chav1961.purelib.model.node";

	/**
	 * <p>Predefined Data Flavor name for Pure Library models to use them in the Swing applications</p>
	 */	
	public static final DataFlavor	MODEL_DATA_FLAVOR = new DataFlavor(ContentNodeMetadata.class, MODEL_DATA_FLAVOR_NAME);	
	
	/**
	 * <p>Null logger facade for any purposes</p>
	 */
	public static final LoggerFacade		NULL_LOGGER = new NullLoggerFacade();

	/**
	 * <p>System.err logger facade for any purposes</p>
	 */
	public static final LoggerFacade		SYSTEM_ERR_LOGGER = new SystemErrLoggerFacade();

	/**
	 * <p>Current logger facade to put purelib debug trace to. When <b>-Dpurelib.debug</b>=true, appropriates to {@linkplain PureLibSettings#SYSTEM_ERR_LOGGER}, otherwise appropriates to {@linkplain PureLibSettings#NULL_LOGGER}</p>
	 * @since 0.0.3
	 */
	public static final LoggerFacade		CURRENT_LOGGER = "true".equalsIgnoreCase(System.getProperty("purelib.debug","true")) ? SYSTEM_ERR_LOGGER : NULL_LOGGER;
	
	/**
	 * <p>Common-accessible localizer for the Pure Library. All localizable content of the Pure library is accessible by the localizer.</p>
	 * @see Localizer 
	 */
	// This description must be after loggers descriptor because it uses them inside the PureLibLocalizer
	public static final Localizer			PURELIB_LOCALIZER = createPureLibLocalizer();
	
	/**
	 * <p>Shared timer to process common maintenance for any pure library consumers</p>
	 */
	public static final Timer				COMMON_MAINTENANCE_TIMER = new Timer("PureLibMaintenanceTimer",true);

	/**
	 * <p>Key to access HTTP port number for built-in help server in the Pure Library</p>
	 */
	public static final String				BUILTIN_HELP_PORT = "purelib.settings.help.port";

	/**
	 * <p>Monitoring manager for MBean services.</p>
	 * @see chav1961.purelib.monitoring
	 * @since 0.0.4
	 */
	public static final MonitoringManager	MONITORING_MANAGER = new MonitoringManager();

	/**
	 * <p>Common class loader for all on-the-fly classes.</p>
	 * @since 0.0.4
	 */
	public static SimpleURLClassLoader		INTERNAL_LOADER = new SimpleURLClassLoader(new URL[0]);
	
	/**
	 * <p>Default content encoding for library</p>
	 * @since 0.0.5
	 */
	public static final String				DEFAULT_CONTENT_ENCODING = "UTF-8";
	
	/**
	 * <p>System content encoding for library</p>
	 * @since 0.0.6
	 */
	public static final String				SYSTEM_CONTENT_ENCODING = System.getProperty("file.encoding");
	
	/**
	 * <p>UUID to use as 'NULL' UUID value</p>
	 * @since 0.0.6
	 */
	public static final UUID				NULL_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	
	public static final FileSystemInterface	ROOT_FS;
	
	/**
	 * <p>This interface describes well-known factories in the Pure Library. All the factories are accessible via standard Java SPI service</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 * @see <a href="https://docs.oracle.com/javase/9/docs/api/java/util/ServiceLoader.html">SPI Service</a> loader
	 */
	public interface WellKnownSchema {
		/**
		 * <p>Get schema name, that is supported with the given service</p>
		 * @return schema name. Can't be null or empty
		 */
		String getSchemaName();
		
		/**
		 * <p>Get schema description for the given SPI service</p>
		 * @return schema description. Can be null or empty
		 */
		String getDescription();
		
		/**
		 * <p>Get factory class for the given SPI service</p>
		 * @return factory class for schema. Can't be null
		 */
		Class<?> getFactoryClass();
		
		/**
		 * <p>Does the class supports {@linkplain SpiService} interface</p> 
		 * @return true if yes
		 */
		boolean supportsSpiService();
		
		/**
		 * <p>Create service instance by it's URI.</p>
		 * @param <T> service instance to create
		 * @param description uri to pass to service factory
		 * @return instance created
		 * @throws EnvironmentException on creation errors
		 */
		<T> T newInstance(URI description) throws EnvironmentException;
	}	
	
	private static final Map<String,Color>			NAME2COLOR = new HashMap<>(); 
	private static final Map<Color,String>			COLOR2NAME = new HashMap<>();
	private static final ColorScheme				DEFAULT_COLOR_SCHEME; 
	private static final NanoServiceControl			NANOSERVICE_MBEAN;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final WellKnownSchema[]			schemasList = {
															new WellKnownSchemaImpl(Localizer.LOCALIZER_SCHEME, "", LocalizerFactory.class, true, 
																(uri)->{
																	try{return LocalizerFactory.getLocalizer(uri);
																	} catch (LocalizationException e) {
																		throw new EnvironmentException(e); 
																	}
																}),
															new WellKnownSchemaImpl(FileSystemInterface.FILESYSTEM_URI_SCHEME, "", FileSystemFactory.class, true, 
																(uri)->{
																	try{return FileSystemFactory.createFileSystem(uri);
																	} catch (IOException e) {
																		throw new EnvironmentException(e); 
																	}
																}),
															new WellKnownSchemaImpl(ResultSetFactory.RESULTSET_PARSERS_SCHEMA, "", ResultSetFactory.class, false, 
																(uri)->{throw new EnvironmentException("This service doesn't supports SpiService interface");})
														};
	private static final AtomicInteger				HELP_CONTEXT_COUNT = new AtomicInteger();
	private static final Object						HELP_CONTEXT_COUNT_SYNC = new Object();
	private static final List<AutoCloseable>		finalCloseList = new ArrayList<>();
	static volatile NanoServiceFactory		helpServer = null;
	
	static {
		try(final InputStream	is = PureLibSettings.class.getResourceAsStream("/purelib.default.properties")) {
			
			DEFAULTS.load(is);			
		} catch (IOException exc) {
			logger.log(Level.WARNING,"Default properties for the Pure library were not loaded: "+exc.getMessage(),exc);
		}

		try {
			ROOT_FS = FileSystemInterface.Factory.newInstance(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/"));
		} catch (IOException exc) {
			logger.log(Level.SEVERE, "Error preparing settings: "+exc.getMessage(),exc);
			throw new PreparationException("Error preparing settings: "+exc.getLocalizedMessage());
		}
		
		if (System.getProperty(SETTINGS_KEY) != null) {
			try{final URI				uri = URI.create(System.getProperty(SETTINGS_KEY));
				final URL				url = FileSystemInterface.FILESYSTEM_URI_SCHEME.equals(uri.getScheme()) ? new URL(null,uri.toString(),new FileSystemURLStreamHandler()) : uri.toURL();
				final URLConnection		conn = url.openConnection();	
				
				try(final InputStream	is = conn.getInputStream()){
					PROPS.load(is);
				}
			} catch (IllegalArgumentException | IOException exc) {
				logger.log(Level.WARNING,"Properties from the ["+System.getProperty(SETTINGS_KEY)+"] for the Pure library were not loaded: "+exc.getMessage(),exc);
			}
		}
		
		try(final InputStream		is = SwingUtils.class.getResourceAsStream("colors.txt");
			final Reader			rdr = new InputStreamReader(is);
			final BufferedReader	brdr = new BufferedReader(rdr)) {
			String					buffer;
			
			while ((buffer = brdr.readLine()) != null) {
				final int			tab = buffer.indexOf('\t');
				final String		name = buffer.substring(0,tab);
				final Color			color = toRGB(buffer.substring(tab+1));
				
				NAME2COLOR.put(name,color);
				COLOR2NAME.putIfAbsent(color,name);				
			}
		} catch (IOException exc) {
			logger.log(Level.WARNING,"Internal color table for the Pure library was not loaded: "+exc.getMessage(),exc);
		}

		try{final MBeanServer 	server = ManagementFactory.getPlatformMBeanServer();
			final ObjectName 	monitoringName = new ObjectName(PureLibSettings.PURELIB_MBEAN+":type=control,name=monitoring");
			final ObjectName 	httpServerName = new ObjectName(PureLibSettings.PURELIB_MBEAN+":type=control,name=httpServer");
			final boolean		httpControlRequires = Boolean.valueOf(System.getProperties().getProperty("chav1961.purelib.mbean.http.control","false"));
	    
//		     server.registerMBean(MONITORING_MANAGER, monitoringName);
		    if (httpControlRequires) {
			    server.registerMBean(NANOSERVICE_MBEAN = new NanoServiceControl(), httpServerName);
		    }
		    else {
		    	NANOSERVICE_MBEAN = null;
		    }
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
		    throw new PreparationException("Error creating MBean monitoring/httpServer manager: "+e.getLocalizedMessage());
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(()->{stopPureLib();}));
		DEFAULT_COLOR_SCHEME = new ColorScheme();
		
		final String	osName = System.getProperty("os.name").toUpperCase();
		
		if (osName.contains("MAC") || osName.contains("DARWIN")) {
			CURRENT_OS = CurrentOS.MACOS; 
		}
		else if (osName.contains("WINDOWS")) {
			CURRENT_OS = CurrentOS.WINDOWS; 
		}
		else if (osName.contains("NUX")) {
			CURRENT_OS = CurrentOS.LINUX; 
		}
		else {
			CURRENT_OS = CurrentOS.UNKNOWN; 
		}
	}
	
	private PureLibSettings(){}
	
	/**
	 * <p>Static singleton instance to get access to the Pure Library settings. Instance is a {@linkplain SubstitutableProperties} object and supports all it's functionality</p>   
	 * @return singleton instance of the Pure Library settings
	 */
	public static SubstitutableProperties instance() {
		return PROPS;
	}

	public static ColorScheme defaultColorScheme() {
		return DEFAULT_COLOR_SCHEME;
	}
	
	/**
	 * <p>Convert color name to it's {@linkplain Color} representation</p>
	 * @param name name to convert
	 * @param defaultColor default Color instance if name can't be converted
	 * @return Color converted. Can't be null
	 * @throws IllegalArgumentException if color name is null or empty
	 */
	public static Color colorByName(final String name, final Color defaultColor) throws IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Color name can't be null or empty");
		}
		else if (NAME2COLOR.containsKey(name)) {
			return NAME2COLOR.get(name);
		}
		else  if (name.startsWith("#")) {
			return new Color(Integer.parseUnsignedInt(name,1,name.length()-1,16));
		}
		else {
			return defaultColor;
		}
	}
	
	/**
	 * <p>Convert {@linkplain Color} instance to it's symbolic name</p>
	 * @param color color to convert
	 * @param defaultName default name if color can't be converted
	 * @return Color name or default name
	 * @throws NullPointerException if color instance to convert is null
	 */
	public static String nameByColor(final Color color, final String defaultName) throws NullPointerException {
		if (color == null) {
			throw new NullPointerException("Color name can't be null or empty");
		}
		else if (COLOR2NAME.containsKey(color)) {
			return COLOR2NAME.get(color);
		}
		else {
			return defaultName;
		}
	}

	/**
	 * <p>Install help content to the application</p> 
	 * @param helpPath help path in the http://localhost:<port>/<path>
	 * @param helpContent static help content
	 * @throws NullPointerException help when content is null
	 * @throws IllegalArgumentException when help path is null or empty
	 * @throws IllegalStateException if help system is not available
	 * @throws SyntaxException syntax errors in the help content
	 * @throws ContentException errors in the help content
	 * @throws IOException I/O errors on installation
	 * @since 0.0.3
	 */
	public static void installHelpContent(final String helpPath, final FileSystemInterface helpContent) throws SyntaxException, NullPointerException, IllegalArgumentException, IllegalStateException, ContentException, IOException {
		if (Utils.checkEmptyOrNullString(helpPath)) {
			throw new IllegalArgumentException("Help path can't be null or empty"); 
		}
		else if (helpContent == null) {
			throw new NullPointerException("Help content can't be null"); 
		}
		else if (!instance().containsKey(BUILTIN_HELP_PORT)) {
			throw new IllegalStateException("Parameter ["+BUILTIN_HELP_PORT+"] is not defined for application. Built-in help system is not available"); 
		}
		else {
			synchronized (HELP_CONTEXT_COUNT_SYNC) {
				if (helpServer == null) {
					if (!PROPS.containsKey(NanoServiceFactory.NANOSERVICE_PORT)) {
						PROPS.setProperty(NanoServiceFactory.NANOSERVICE_PORT,instance().getProperty(BUILTIN_HELP_PORT));
					}
					if (!PROPS.containsKey(NanoServiceFactory.NANOSERVICE_ROOT)) {
						PROPS.setProperty(NanoServiceFactory.NANOSERVICE_ROOT,FileSystemInterface.FILESYSTEM_URI_SCHEME+":memory:/");
					}
					if (!PROPS.containsKey(NanoServiceFactory.NANOSERVICE_LOCALHOST_ONLY)) {
						PROPS.setProperty(NanoServiceFactory.NANOSERVICE_LOCALHOST_ONLY,"true");
					}
					helpServer = new NanoServiceFactory(new StandardJRELoggerFacade(logger), PROPS);
					helpServer.start();
				}
				try(final FileSystemInterface fsi = helpServer.getServiceRoot().clone().open(helpPath)) {
					if (fsi.exists()) {
						throw new IllegalArgumentException("Help path ["+helpPath+"] already installed");
					}
					else {
						fsi.mkDir();
						helpServer.getServiceRoot().open(helpPath).mount(helpContent).open("/");
					}
				}			
				HELP_CONTEXT_COUNT.incrementAndGet();
			}
		}
	}

	/**
	 * <p>Uninstall help content</p>
	 * @param helpPath help path in the http://localhost:<port>/<path>
	 * @throws NullPointerException help when content is null
	 * @throws IllegalArgumentException when help path is null or empty
	 * @throws IllegalStateException if help system is not available
	 * @throws SyntaxException syntax errors in the help content
	 * @throws ContentException errors in the help content
	 * @throws IOException I/O errors on installation
	 * @since 0.0.3
	 */
	public static void uninstallHelpContent(final String helpPath) throws SyntaxException, NullPointerException, IllegalArgumentException, IllegalStateException, ContentException, IOException {
		if (Utils.checkEmptyOrNullString(helpPath)) {
			throw new IllegalArgumentException("Help path can't be null or empty"); 
		}
		else if (!instance().containsKey(BUILTIN_HELP_PORT)) {
			throw new IllegalStateException("Parameter ["+BUILTIN_HELP_PORT+"] is not defined for application. Built-in help system is not available"); 
		}
		else {
			synchronized (HELP_CONTEXT_COUNT_SYNC) {
				final int	value = HELP_CONTEXT_COUNT.decrementAndGet();
				
				if (value < 0) {
					throw new IllegalArgumentException("Help path ["+helpPath+"] was not deployed earlier"); 
				}
				else {
					try(final FileSystemInterface fsi = helpServer.getServiceRoot().clone().open(helpPath)) {
						if (!fsi.exists()) {
							throw new IllegalArgumentException("Help path ["+helpPath+"] is not installed");
						}
						else {
							helpServer.getServiceRoot().open(helpPath).unmount();
							if (helpServer.getServiceRoot().open(helpPath).exists()) {
								helpServer.getServiceRoot().delete();
							}
							helpServer.getServiceRoot().open("/");
						}
					}			
					if (value == 0) {
						helpServer.stop();
						helpServer = null;
					}
				}
			}
		}
	}
	
	
	/**
	 * <p>Get well-known schemas in the Pure Library</p> 
	 * @return schemas iterable. Can't be null
	 */
	public static Iterable<WellKnownSchema> wellKnownSchemas() {
		return new Iterable<PureLibSettings.WellKnownSchema>() {
			@Override
			public Iterator<WellKnownSchema> iterator() {
				return new Iterator<PureLibSettings.WellKnownSchema>() {
					int	index = 0;
					
					@Override public boolean hasNext() {return index < schemasList.length;}
					@Override public WellKnownSchema next() {return schemasList[index++];}
				};
			}
		};
	}	
	
	/**
	 * <p>Get description about Pure Library</p> 
	 * @return html-typed description. Can't be null
	 * @since 0.0.5
	 * @lastUpdate 0.0.6
	 */
	public static String aboutPureLib() {
		try(final InputStream 	is = PureLibSettings.class.getResourceAsStream("about.cre");
			final Reader		rdr = new InputStreamReader(is);
			final Writer		wr = new StringWriter()) {
			
			try(final CreoleWriter	cwr = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML)) {
			
				Utils.copyStream(rdr, cwr);
			}
			return wr.toString();
		} catch (IOException e) {
			return "I/O error reading 'about' information";
		}
	}

	/**
	 * <p>Register {@linkplain AutoCloseable} instance to process it on system shutdown</p>
	 * @param ac autocloseable to register. Can't be null
	 * @throws NullPointerException when parameter is null
	 * @see #unregisterAutoCloseable(AutoCloseable)
	 * @see #isAutoCloseableRegistered(AutoCloseable)
	 * @since 0.0.6
	 */
	public static void registerAutoCloseable(final AutoCloseable ac) throws NullPointerException {
		if (ac == null) {
			throw new NullPointerException("Autocloseable to register can't be null");
		}
		else {
			synchronized (finalCloseList) {
				finalCloseList.add(ac);
			}
		}
	}

	/**
	 * <p>Unregister {@linkplain AutoCloseable} instance to process it on system shutdown</p>
	 * @param ac autocloseable to unregister. Can't be null
	 * @throws NullPointerException when parameter is null
	 * @see #registerAutoCloseable(AutoCloseable)
	 * @see #isAutoCloseableRegistered(AutoCloseable)
	 * @since 0.0.6
	 */
	public static void unregisterAutoCloseable(final AutoCloseable ac) throws NullPointerException {
		if (ac == null) {
			throw new NullPointerException("Autocloseable to unregister can't be null");
		}
		else {
			synchronized (finalCloseList) {
				finalCloseList.remove(ac);
			}
		}
	}
	
	/**
	 * <p>Is {@linkplain AutoCloseable} registered by {@linkplain #registerAutoCloseable(AutoCloseable)}</p>
	 * @param ac autocloseable to test. Can't be null
	 * @return true if registered
	 * @throws NullPointerException when parameter is null
	 * @see #registerAutoCloseable(AutoCloseable)
	 * @see #unregisterAutoCloseable(AutoCloseable)
	 * @since 0.0.6
	 */
	public boolean isAutoCloseableRegistered(final AutoCloseable ac) throws NullPointerException {
		if (ac == null) {
			throw new NullPointerException("Autocloseable to unregister can't be null");
		}
		else {
			synchronized (finalCloseList) {
				return finalCloseList.contains(ac);
			}
		}
	}
	
	public static void preloadNatives(final URI nativesRoot) throws IOException {
		preloadNatives(nativesRoot, (s)->true);
	}
	
	public static void preloadNatives(final URI nativesRoot, final Predicate<String> tester) throws IOException {
		if (nativesRoot == null) {
			throw new NullPointerException("Natives root can't be null");
		}
		else if (tester == null) {
			throw new NullPointerException("Tester predicate can't be null");
		}
		else {
			try(final InputStream		is = nativesRoot.toURL().openStream();
				final ZipInputStream	zis = new ZipInputStream(is)) {
				
				NativeLoader.loadLibraries(zis, tester);
			}
		}
	}
	
	private static Color toRGB(final String rgb) {
		if (!rgb.isEmpty()) {
			if (rgb.charAt(0) == '#') {
				return new Color((int)Long.parseLong(rgb.substring(1).toUpperCase(),16));
			}
			else {
				final String[]	parts = rgb.split("\\,");
				
				return new Color(Integer.valueOf(parts[0]),Integer.valueOf(parts[1]),Integer.valueOf(parts[2])); 
			}
		}
		else {
			return Color.BLACK;
		}
	}

	private static MimeType buildMime(final String type, final String subtype) {
		try{return new MimeType(type,subtype);
		} catch (MimeParseException e) {
			return new MimeType();
		}
	}
	
	private static Localizer createPureLibLocalizer() {
		try{return new PureLibLocalizer();
		} catch (LocalizationException exc) {
			logger.log(Level.SEVERE,"Pure library localizer can't be initiated: "+exc.getMessage(),exc);
			return null;
		} catch (Throwable exc) {
			exc.printStackTrace();
			throw exc;
		}
	}

	private static long getCurrentPID() {
		return ProcessHandle.current().pid();
	}

	private static void closeAll(final List<AutoCloseable> fcl) {
		synchronized (fcl) {
			for (AutoCloseable item : fcl) {
				try{item.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void stopPureLib() {
		final MBeanServer 	server = ManagementFactory.getPlatformMBeanServer();
				
		closeAll(finalCloseList);
		
		synchronized (HELP_CONTEXT_COUNT_SYNC) {
			if (helpServer != null) {
				try{helpServer.stop();
				} catch (IOException e) {
					logger.log(Level.WARNING,"Builtin help server stop failure: "+e.getLocalizedMessage(),e);
				}
				helpServer = null;
			}
		}
		
		try{final ObjectName 	monitoringName = new ObjectName(PureLibSettings.PURELIB_MBEAN+":type=control,name=monitoring");
			final ObjectName 	httpServerName = new ObjectName(PureLibSettings.PURELIB_MBEAN+":type=control,name=httpServer");
			
	    	try{server.unregisterMBean(monitoringName);
			} catch (MBeanRegistrationException | InstanceNotFoundException e) {
			}
	    	if (NANOSERVICE_MBEAN != null) {
		    	try{server.unregisterMBean(httpServerName);
				} catch (MBeanRegistrationException | InstanceNotFoundException e) {
				}
	    	}
		} catch (MalformedObjectNameException e1) {
		}
		
		MONITORING_MANAGER.close();
		
		if (ROOT_FS != null) {
			try{ROOT_FS.close();
			} catch (IOException e) {
			}
		}
	}

	
	private static class WellKnownSchemaImpl<T> implements WellKnownSchema {
		@FunctionalInterface
		private interface CreationCallback<T> {
			T create(URI uri) throws EnvironmentException;
		}
		
		private final String				schemaName;
		private final String				description;
		private final Class<?>				factoryClass;
		private final boolean				supportsSpi;
		private final CreationCallback<T>	callback;
		
		WellKnownSchemaImpl(final String schemaName, final String description, final Class<?> factoryClass, final boolean supportsSpi, final CreationCallback<T> callback) {
			this.schemaName = schemaName;
			this.description = description;
			this.factoryClass = factoryClass;
			this.supportsSpi = supportsSpi;
			this.callback = callback;
		}

		@Override public String getSchemaName() {return schemaName;}
		@Override public String getDescription() {return description;}
		@Override public Class<?> getFactoryClass() {return factoryClass;}
		@Override public boolean supportsSpiService() {return supportsSpi;}
		@SuppressWarnings({ "unchecked", "hiding" })
		@Override public <T> T newInstance(final URI description) throws EnvironmentException {return (T) callback.create(description);}

		@Override
		public String toString() {
			return "WellKnownSchemaImpl [schemaName=" + schemaName + ", description=" + description + ", factoryClass=" + factoryClass + ", supportsSpi=" + supportsSpi + "]";
		}
	}
	
	// @see https://github.com/scijava/native-lib-loader
	private static class NativeLoader {
		private enum SystemType {
			UNKNOWN(""), 
			LINUX_32("linux_32/"), 
			LINUX_64("linux_64/"), 
			LINUX_ARM("linux_arm/"), 
			LINUX_ARM64("linux_arm64/"), 
			WINDOWS_32("windows_32/"), 
			WINDOWS_64("windows_64/"), 
			WINDOWS_ARM64("windows_arm64/"), 
			OSX_32("osx_32/"), 
			OSX_64("osx_32/"), 
			OSX_PPC("osx_ppc/"), 
			OSX_ARM64("osx_arm64/"), 
			AIX_32("aix_32/"), 
			AIX_64("aix_64/");
			
			private final String	subdir;
			
			private SystemType(final String subdir) {
				this.subdir = subdir;
			}
			
			String getSubdir(){
				return subdir;
			}
		}
		
		private enum ProcType {
			UNKNOWN, 
			INTEL_32, 
			INTEL_64, 
			PPC, 
			PPC_64, 
			ARM, 
			AARCH_64;
		}

		static void loadLibraries(final ZipInputStream zis,final Predicate<String> tester) throws IOException{
			final ProcType		proc = getProcessor();
			final SystemType	sys = getSystemType(proc);
			
			if (proc == ProcType.UNKNOWN || sys == SystemType.UNKNOWN) {
				throw new IOException("Processor/architecture of your computer was not detected correctly"); 
			}
			else {
				final File		tmpDir = new File(System.getProperty("java.io.tmpdir")); 
				ZipEntry		ze;
				
				while ((ze = zis.getNextEntry()) != null) {
					if (ze.getName().startsWith(sys.getSubdir())) {
						final String	name = new File(ze.getName()).getName();
						
						if (tester.test(name)) {
							final File	temp = File.createTempFile("native", ".bin", tmpDir);
							
							temp.deleteOnExit();
							try(final OutputStream	os = new FileOutputStream(temp)) {
								Utils.copyStream(zis, os);
							}
							System.load(temp.getAbsolutePath());
						}
					}
				}
			}
			
		}
		
		private static ProcType getProcessor() {
			final String 	arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
			ProcType 		processor = ProcType.UNKNOWN;

			if (arch.contains("arm")) {
				processor = ProcType.ARM;
			}
			else if (arch.contains("aarch64")) {
				processor = ProcType.AARCH_64;
			}
			else if (arch.contains("ppc")) {
				processor = arch.contains("64") ? ProcType.PPC_64 : ProcType.PPC;
			}
			else if (arch.contains("86") || arch.contains("amd")) {
				processor = arch.contains("64") ? ProcType.INTEL_64 : ProcType.INTEL_32;
			}
			return processor;
		}
		
		public static SystemType getSystemType(final ProcType processor) {
			final String 	name = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
			SystemType		architecture = SystemType.UNKNOWN;
			
			if (ProcType.UNKNOWN != processor) {
				if (name.contains("nix") || name.contains("nux")) {
					switch (processor) {
						case INTEL_32 :
							architecture = SystemType.LINUX_32;
							break;
						case INTEL_64 :
							architecture = SystemType.LINUX_64;
							break;
						case ARM :
							architecture = SystemType.LINUX_ARM;
							break;
						case AARCH_64 :
							architecture = SystemType.LINUX_ARM64;
							break;
						default :
					}
				}
				else if (name.contains("aix")) {
					switch (processor) {
						case PPC :
							architecture = SystemType.AIX_32;
							break;
						case PPC_64 :
							architecture = SystemType.AIX_64;
							break;
						default :
					}
				}
				else if (name.contains("win")) {
					switch (processor) {
						case INTEL_32 :
							architecture = SystemType.WINDOWS_32;
							break;
						case INTEL_64 :
							architecture = SystemType.WINDOWS_64;
							break;
						case AARCH_64 :
							architecture = SystemType.WINDOWS_ARM64;
							break;
						default :
					}
				}
				else if (name.contains("mac")) {
					switch (processor) {
						case INTEL_32 :
							architecture = SystemType.OSX_32;
							break;
						case INTEL_64 :
							architecture = SystemType.OSX_64;
							break;
						case AARCH_64 :
							architecture = SystemType.OSX_ARM64;
							break;
						case PPC :
							architecture = SystemType.OSX_PPC;
							break;
						default :
					}
				}
			}
			return architecture;
		}
	}
}

