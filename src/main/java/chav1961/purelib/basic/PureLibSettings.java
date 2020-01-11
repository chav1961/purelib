package chav1961.purelib.basic;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.MimeTypeParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.FileSystemURLStreamHandler;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.sql.content.ResultSetFactory;
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
 * @lastUpdate 0.0.3
 */

public class PureLibSettings {
	/**
	 * <p>This logger is used to print put any internal problems in the Pure Library</p>
	 */
	public static final Logger		logger = Logger.getLogger("chav1961.purelib");
	
	/**
	 * <p>Current Java process id</p>
	 */
	public static final long		CURRENT_PID = getCurrentPID();
	
	/**
	 * <p>This is current version of the Pure Library</p>
	 */
	public static final String		CURRENT_VERSION = "0.0.3";


	/**
	 * <p>This is a vendor of the Pure Library</p>
	 */
	public static final String		VENDOR = "A.Chernomyrdin aka chav1961";
	
	/**
	 * <p>This is <b>-D</b> variable name to import content to the settings repository</p>
	 */
	public static final String		SETTINGS_KEY = "purelib.settings.source";

	/**
	 * <p>Default mandatory field background for UI forms</p>
	 */
	public static final String		UI_SWING_MANDATORY_BACKGROUND = "purelib.settings.ui.swing.mandatory_background";

	/**
	 * <p>Default mandatory field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_MANDATORY_FOREGROUND = "purelib.settings.ui.swing.mandatory_foreground";

	/**
	 * <p>Default negative mandatory field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_MANDATORY_FOREGROUND_NEGATIVE = "purelib.settings.ui.swing.mandatory_foreground_negative";

	/**
	 * <p>Default zero mandatory field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_MANDATORY_FOREGROUND_ZERO = "purelib.settings.ui.swing.mandatory_foreground_zero";

	/**
	 * <p>Default positive mandatory field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_MANDATORY_FOREGROUND_POSITIVE = "purelib.settings.ui.swing.mandatory_foreground_positive";
	
	/**
	 * <p>Default selection background in the mandatory field for UI forms</p>
	 */
	public static final String		UI_SWING_MANDATORY_SELECTED = "purelib.settings.ui.swing.mandatory_selected";

	/**
	 * <p>Default selection color in the mandatory field for UI forms</p>
	 */
	public static final String		UI_SWING_MANDATORY_SELECTED_TEXT = "purelib.settings.ui.swing.mandatory_selected_text";
	
	/**
	 * <p>Default optional field background for UI forms</p>
	 */
	public static final String		UI_SWING_OPTIONAL_BACKGROUND = "purelib.settings.ui.swing.optional_background";
	
	/**
	 * <p>Default optional field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_OPTIONAL_FOREGROUND = "purelib.settings.ui.swing.optional_foreground";

	/**
	 * <p>Default optional negative field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_OPTIONAL_FOREGROUND_NEGATIVE = "purelib.settings.ui.swing.optional_foreground_negative";
	
	/**
	 * <p>Default optional zero field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_OPTIONAL_FOREGROUND_ZERO = "purelib.settings.ui.swing.optional_foreground_zero";
	
	/**
	 * <p>Default optional positive field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_OPTIONAL_FOREGROUND_POSITIVE = "purelib.settings.ui.swing.optional_foreground_positive";
	
	/**
	 * <p>Default selection background in the mandatory field for UI forms</p>
	 */
	public static final String		UI_SWING_OPTIOAL_SELECTED = "purelib.settings.ui.swing.optional_selected";

	/**
	 * <p>Default selection color in the mandatory field for UI forms</p>
	 */
	public static final String		UI_SWING_OPTIOAL_SELECTED_TEXT = "purelib.settings.ui.swing.optional_selected_text";
	
	
	/**
	 * <p>Default optional field background for UI forms</p>
	 */
	public static final String		UI_SWING_READONLY_BACKGROUND = "purelib.settings.ui.swing.readonly_background";
	
	/**
	 * <p>Default optional field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_READONLY_FOREGROUND = "purelib.settings.ui.swing.readonly_foreground";

	/**
	 * <p>Default negative mark field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_NEGATIVEMARK_FOREGROUND = "purelib.settings.ui.swing.negativemark_foreground";

	/**
	 * <p>Default positive mark field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_POSITIVEMARK_FOREGROUND = "purelib.settings.ui.swing.positivemark_foreground";
	
	/**
	 * <p>Default zero mark field foreground for UI forms</p>
	 */
	public static final String		UI_SWING_ZEROMARK_FOREGROUND = "purelib.settings.ui.swing.zeromark_foreground";
	
	// TODO:
	public static final String		UI_SWING_DATEPICKER_DAY_NAME_COLOR = "purelib.settings.ui.swing.datepicker_day_name_color";
	public static final String		UI_SWING_DATEPICKER_WEEKEND_NAME_COLOR = "purelib.settings.ui.swing.datepicker_weekend_name_color";
	public static final String		UI_SWING_DATEPICKER_DAY_VALUE_COLOR = "purelib.settings.ui.swing.datepicker_day_value_color";
	public static final String		UI_SWING_DATEPICKER_WEEKEND_VALUE_COLOR = "purelib.settings.ui.swing.datepicker_weekend_name_color";
	
	
	
	/**
	 * <p>Default border color for clicked toolbar button in the UI forms</p>
	 */
	public static final String		UI_SWING_TOOLBAR_CLICK_BORDER_COLOR = "purelib.settings.ui.swing.toolbar_click_border_color";

	/**
	 * <p>Default border color for the tooltips in the UI forms</p>
	 */
	public static final String		UI_SWING_TOOLTIP_BORDER_COLOR = "purelib.settings.ui.swing.tooltip_border_color";

	/**
	 * <p>Allow Unsafe functionality in the Pure Library</p>
	 */
	public static final String		ALLOW_UNSAFE = "purelib.settings.allow.unsafe";
	
	/**
	 * <p>Print expanded macros of the AsmWriter for debugging purposes</p>
	 */
	public static final String		PRINT_EXPANDED_MACROS = "purelib.settings.macros.printExpanded";
	
	/**
	 * <p>MIME for plain text</p>
	 */
	public static final MimeType	MIME_PLAIN_TEXT = buildMime("text","plain");

	/**
	 * <p>MIME for CREOLE text</p>
	 */
	public static final MimeType	MIME_CREOLE_TEXT = buildMime("text","x-wiki.creole");

	/**
	 * <p>MIME for HTML text</p>
	 */
	public static final MimeType	MIME_HTML_TEXT = buildMime("text","html");

	/**
	 * <p>MIME for XML text</p>
	 */
	public static final MimeType	MIME_XML_TEXT = buildMime("text","xml");

	/**
	 * <p>MIME for JSON</p>
	 */
	public static final MimeType	MIME_JSON_TEXT = buildMime("application","json");

	/**
	 * <p>MIME for CSS</p>
	 */
	public static final MimeType	MIME_CSS_TEXT = buildMime("text","css");

	/**
	 * <p>MIME for CSS</p>
	 */
	public static final MimeType	MIME_FAVICON = buildMime("image","webp");
	
	/**
	 * <p>MIME for octet stream</p>
	 */
	public static final MimeType	MIME_OCTET_STREAM = buildMime("application","octet-stream");

	
	public static final String		MODEL_DATA_FLAVOR_NAME = "chav1961.purelib.model.node";
	public static final DataFlavor	MODEL_DATA_FLAVOR = new DataFlavor(ContentNodeMetadata.class, MODEL_DATA_FLAVOR_NAME);	
	
	/**
	 * <p>Null logger facade for any purposes</p>
	 */
	public static final LoggerFacade	NULL_LOGGER = new NullLoggerFacade();

	/**
	 * <p>System.err logger facade for any purposes</p>
	 */
	public static final LoggerFacade	SYSTEM_ERR_LOGGER = new SystemErrLoggerFacade();

	/**
	 * <p>Common-accessible localizer for the Pure Library. This description must be after loggers descriptor because it uses them inside the PureLibLocalizer</p> 
	 */
	public static final Localizer		PURELIB_LOCALIZER = createPureLibLocalizer();
	
	
	/**
	 * <p>Current logger to put purelib debug trace to</p>
	 * @since 0.0.3
	 */
	public static final LoggerFacade	CURRENT_LOGGER = "true".equalsIgnoreCase(System.getProperty("purelib.debug","true")) ? SYSTEM_ERR_LOGGER : NULL_LOGGER;
	
	/**
	 * <p>Shared timer to process common maintenance for any pure library consumers</p>
	 */
	public static final Timer			COMMON_MAINTENANCE_TIMER = new Timer("PureLibMaintenanceTimer",true);

	/**
	 * <p>HTTP port for built-in help</p>
	 */
	public static final String			BUILTIN_HELP_PORT = "purelib.settings.help.port";

	
	/**
	 * <p>This interface describes well-known factories in the Pure Library</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	public interface WellKnownSchema {
		/**
		 * <p>Get schema name</p>
		 * @return schema name. Can't be null or empty
		 */
		String getSchemaName();
		
		/**
		 * <p>Get schema description</p>
		 * @return schema description. Can be null or empty
		 */
		String getDescription();
		
		/**
		 * <p>Get factory class for the given schema</p>
		 * @return factory class for schema. Can't be null
		 */
		Class<?> getFactoryClass();
		
		/**
		 * <p>Does the class supports {@linkplain SpiService} interface</p> 
		 * @return true of does
		 */
		boolean supportsSpiService();
		
		/**
		 * <p>Create service instance by it's URI</p>
		 * @param description uri to pass to service factory
		 * @return instance created.
		 * @throws EnvironmentException on creation errors
		 */
		<T> T newInstance(URI description) throws EnvironmentException;
	}	
	
	private static final Map<String,Color>			NAME2COLOR = new HashMap<>(); 
	private static final Map<Color,String>			COLOR2NAME = new HashMap<>();
	private static final SubstitutableProperties	defaults = new SubstitutableProperties(System.getProperties()); 
	private static final SubstitutableProperties	props = new SubstitutableProperties(defaults);
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final WellKnownSchema[]			schemasList = {
															new WellKnownSchemaImpl(Localizer.LOCALIZER_SCHEME, "", LocalizerFactory.class, true, 
																(uri)->{
																	try{return LocalizerFactory.getLocalizer(uri);
																	} catch (IOException e) {
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
	private static final AtomicInteger				helpContextCount = new AtomicInteger();
	private static final Object						helpContextCountSync = new Object();
	private static volatile NanoServiceFactory		helpServer = null;
	
	static {
		try(final InputStream	is = PureLibSettings.class.getResourceAsStream("/purelib.default.properties")) {
			
			defaults.load(is);			
		} catch (IOException exc) {
			logger.log(Level.WARNING,"Default properties for the Pure library were not loaded: "+exc.getMessage(),exc);
		}
		
		if (System.getProperty(SETTINGS_KEY) != null) {
			try{final URI				uri = URI.create(System.getProperty(SETTINGS_KEY));
				final URL				url = FileSystemInterface.FILESYSTEM_URI_SCHEME.equals(uri.getScheme()) ? new URL(null,uri.toString(),new FileSystemURLStreamHandler()) : uri.toURL();
				final URLConnection		conn = url.openConnection();	
				
				try(final InputStream	is = conn.getInputStream()){
					props.load(is);
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
		
		Runtime.getRuntime().addShutdownHook(new Thread(()->{stopPureLib();}));
	}
	
	private PureLibSettings(){}
	
	/**
	 * <p>Static singleton instance to get access to the purelib settings. Instance is a {@linkplain SubstitutableProperties} object and supports all it's functionality</p>   
	 * @return singleton instance of the Pure Library settings
	 */
	public static SubstitutableProperties instance() {
		return props;
	}

	/**
	 * <p>Convert color name to it's {@linkplain Color} representation</p>
	 * @param name name to convert
	 * @param defaultColor default Color instance if name can't be converted
	 * @return Color converted. Can't be null
	 * @throws IllegalArgumentException if color name is null or empty
	 */
	public static Color colorByName(final String name, final Color defaultColor) throws IllegalArgumentException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Color name can't be null or empty");
		}
		else if (NAME2COLOR.containsKey(name)) {
			return NAME2COLOR.get(name);
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
		if (helpPath == null || helpPath.isEmpty()) {
			throw new IllegalArgumentException("Help path can't be null or empty"); 
		}
		else if (helpContent == null) {
			throw new NullPointerException("Help content can't be null"); 
		}
		else if (!instance().containsKey(BUILTIN_HELP_PORT)) {
			throw new IllegalStateException("Parameter ["+BUILTIN_HELP_PORT+"] is not defined for application. Built-in help system is not available"); 
		}
		else {
			synchronized (helpContextCountSync) {
				if (helpServer == null) {
					if (!props.containsKey(NanoServiceFactory.NANOSERVICE_PORT)) {
						props.setProperty(NanoServiceFactory.NANOSERVICE_PORT,instance().getProperty(BUILTIN_HELP_PORT));
					}
					if (!props.containsKey(NanoServiceFactory.NANOSERVICE_ROOT)) {
						props.setProperty(NanoServiceFactory.NANOSERVICE_ROOT,FileSystemInterface.FILESYSTEM_URI_SCHEME+":memory:/");
					}
					if (!props.containsKey(NanoServiceFactory.NANOSERVICE_LOCALHOST_ONLY)) {
						props.setProperty(NanoServiceFactory.NANOSERVICE_LOCALHOST_ONLY,"true");
					}
					helpServer = new NanoServiceFactory(new StandardJRELoggerFacade(logger), props);
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
				helpContextCount.incrementAndGet();
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
		if (helpPath == null || helpPath.isEmpty()) {
			throw new IllegalArgumentException("Help path can't be null or empty"); 
		}
		else if (!instance().containsKey(BUILTIN_HELP_PORT)) {
			throw new IllegalStateException("Parameter ["+BUILTIN_HELP_PORT+"] is not defined for application. Built-in help system is not available"); 
		}
		else {
			synchronized (helpContextCountSync) {
				final int	value = helpContextCount.decrementAndGet();
				
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
		} catch (MimeTypeParseException e) {
			return new MimeType();
		}
	}
	
	private static Localizer createPureLibLocalizer() {
		try{return new PureLibLocalizer();
		} catch (LocalizationException exc) {
			logger.log(Level.SEVERE,"Pure library localizer can't be initiated: "+exc.getMessage(),exc);
			return null;
		}
	}

	private static long getCurrentPID() {
		final String	name = ManagementFactory.getRuntimeMXBean().getName(); 
		
		return Long.valueOf(name.substring(0,name.indexOf('@')));
	}

	private static void stopPureLib() {
		synchronized (helpContextCountSync) {
			if (helpServer != null) {
				try{helpServer.stop();
				} catch (IOException e) {
					logger.log(Level.WARNING,"Builtin help server stop failure: "+e.getLocalizedMessage(),e);
				}
				helpServer = null;
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
}

