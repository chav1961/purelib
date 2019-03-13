package chav1961.purelib.fsys;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.i18n.PureLibLocalizer;

public class FileSystemOnClassLoader extends AbstractFileSystem implements FileSystemInterfaceDescriptor {
	private static final URI	SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":classloader:/");
	private static final String	DESCRIPTION = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnClassLoader.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_DESCRIPTION_SUFFIX;
	private static final String	VENDOR = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnClassLoader.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_VENDOR_SUFFIX;
	private static final String	LICENSE = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnClassLoader.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_SUFFIX;
	private static final String	LICENSE_CONTENT = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnClassLoader.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_CONTENT_SUFFIX;
	private static final String	HELP = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnClassLoader.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_HELP_SUFFIX;
	private static final Icon	ICON = new ImageIcon(FileSystemInMemory.class.getResource("icon.png"));
	
	private final boolean			needClose;

	/**
	 * <p>This constructor is an entry for the SPI service only. Don't use it in any purposes</p> 
	 */
	public FileSystemOnClassLoader(){
		this.needClose = false;
	}
	
	/**
	 * <p>Create the file system for the given file system type and path</p>.  
	 * @param rootPath root directory for the file system. Need be absolute URI with the schema 'fsys:filesystemtype:', for example <code>'fsys:jar:c:/mydir'</code>
	 * @throws IOException if any exception was thrown
	 */
	public FileSystemOnClassLoader(final URI rootPath) throws IOException {
		super(rootPath);
		final Properties	props = new Properties();
		
		if (rootPath.getQuery() != null) {
			try(final StringReader	rdr = new StringReader(rootPath.getQuery().replace('&','\n'))) {
				props.load(rdr);
			}
		}
		this.needClose = true;
	}
	
	private FileSystemOnClassLoader(final FileSystemOnClassLoader another) {
		super(another);
		this.needClose = false;
	}

	@Override
	public boolean canServe(final URI resource) {
		return Utils.canServeURI(resource,SERVE);
	}
	
	@Override
	public FileSystemInterface newInstance(final URI resource) throws EnvironmentException {
		if (!canServe(resource)) {
			throw new EnvironmentException("Resource URI ["+resource+"] is not supported by the class. Valid URI must be ["+SERVE+"...]");
		}
		else {
			try{return new FileSystemOnFileSystem(URI.create(resource.getRawSchemeSpecificPart()));
			} catch (IOException e) {
				throw new EnvironmentException("I/O error creating file system on class loader: "+e.getLocalizedMessage(),e); 
			}
		}
	}
	
	@Override
	public void close() throws IOException {
		if (needClose) {
		}
		super.close();
	}
	
	@Override
	public FileSystemInterface clone() {
		return new FileSystemOnClassLoader(this);
	}
	
	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		if (actualPath == null || actualPath.getPath().isEmpty()) {
			return new ClassLoaderDataWrapperInterface(rootPath);
		}
		else {
			return new ClassLoaderDataWrapperInterface(Utils.appendRelativePath2URI(rootPath,actualPath.getPath()));
		}
	}
	
	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getVersion() {
		return PureLibSettings.CURRENT_VERSION;
	}

	@Override
	public URI getLocalizerAssociated() {
		return PureLibLocalizer.LOCALIZER_SCHEME;
	}

	@Override
	public String getDescriptionId() {
		return DESCRIPTION;
	}

	@Override
	public Icon getIcon() {
		return ICON;
	}
	
	@Override
	public String getVendorId() {
		return VENDOR;
	}

	@Override
	public String getLicenseId() {
		return LICENSE;
	}

	@Override
	public String getLicenseContentId() {
		return LICENSE_CONTENT;
	}

	@Override
	public String getHelpId() {
		return HELP;
	}

	@Override
	public URI getUriTemplate() {
		return SERVE;
	}

	@Override
	public FileSystemInterface getInstance() throws EnvironmentException {
		return this;
	}

	@Override
	public boolean testConnection(final URI connection, final LoggerFacade logger) throws IOException {
		if (connection == null) {
			throw new NullPointerException("Connection to test can't be null");
		}
		else {
			try(final FileSystemInterface	inst  = newInstance(connection)) {
				
				return inst.exists();
			} catch (EnvironmentException e) {
				if (logger != null) {
					logger.message(Severity.error, e, "Error testing connection [%1$s]: %2$s",connection,e.getLocalizedMessage());
				}
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
	}
	
	private static class ClassLoaderDataWrapperInterface implements DataWrapperInterface {
		private final URI			wrapper;
		
		public ClassLoaderDataWrapperInterface(final URI wrapper) {
			this.wrapper = wrapper;
		}

		@Override
		public URI[] list(Pattern pattern) throws IOException {
			final List<URI>		result = new ArrayList<>();
			
            try(final InputStream		is = Thread.currentThread().getContextClassLoader().getResourceAsStream(toValidPath(wrapper));
            	final Reader			rdr = new InputStreamReader(is);
            	final BufferedReader	brdr = new BufferedReader(rdr)) {
            	String	line;
            	
            	while ((line = brdr.readLine()) != null) {
					if (pattern.matcher(line).matches()) {
						result.add(URI.create(line.endsWith("/") ? line.substring(0,line.length()-1) : line));
					}
            	}
            }
            
			final URI[]			returned = result.toArray(new URI[result.size()]);
			result.clear();
			return returned;
		}

		@Override
		public void mkDir() throws IOException {
			throw new IOException("This file system is read-only");
		}

		@Override
		public void create() throws IOException {
			throw new IOException("This file system is read-only");
		}

		@Override
		public void setName(final String name) throws IOException {
			throw new IOException("This file system is read-only");
		}

		@Override
		public void delete() throws IOException {
			throw new IOException("This file system is read-only");
		}

		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			throw new IOException("This file system is read-only");
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return Thread.currentThread().getContextClassLoader().getResourceAsStream(toValidPath(wrapper));
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			final URL		resource = Thread.currentThread().getContextClassLoader().getResource(toValidPath(wrapper));
			
			if (resource != null) {
				try{final File	path = new File(resource.toURI());
				
				return Utils.mkMap(ATTR_SIZE, 0
						, ATTR_NAME, path.getName() == null ? "/" : path.getName().toString()
						, ATTR_LASTMODIFIED, 0
						, ATTR_DIR, !wrapper.getPath().contains(".")
						, ATTR_EXIST, true
						, ATTR_CANREAD, true
						, ATTR_CANWRITE, false
						);
				} catch (URISyntaxException e) {
					throw new IOException(e.getLocalizedMessage());
				}
			}
			else {
				return Utils.mkMap(ATTR_SIZE, 0
						, ATTR_NAME, wrapper.getPath() == null ? "/" : wrapper.getPath()
						, ATTR_LASTMODIFIED, 0
						, ATTR_DIR, false
						, ATTR_EXIST, false
						, ATTR_CANREAD, false
						, ATTR_CANWRITE, false);
			}
			
		}

		@Override 
		public void linkAttributes(Map<String, Object> attributes) throws IOException {
			throw new IOException("This file system is read-only");
		}
		
		private static String toValidPath(final URI path) {
			final String	pathString = path.getPath();
			
			return "/".equals(pathString) ? "" : (pathString.contains(".") ? pathString : pathString+"/").substring(1);			
		}
	}
}
