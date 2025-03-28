package chav1961.purelib.fsys.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.AbstractFileSystemWithLockService;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.i18n.internal.PureLibLocalizer;

/**
 * <p>This class implements the file system interface on the standard file system mechanism in the Java 1.7 and later.
 * The URI to use this class is <code>URI.create("fsys:filesystem_specific_url");</code> 
 * (for example <code>URI.create("fsys:jar:./myJar.jar");</code>)</p>
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * @see java.nio.file.FileSystem FileSystem 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface FileSystemInterface
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.5
 */
public class FileSystemOnFileSystem extends AbstractFileSystemWithLockService<AutoCloseable, AutoCloseable> implements FileSystemInterfaceDescriptor {
	private static final URI	SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":filesystem:/");
	private static final String	DESCRIPTION = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFileSystem.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_DESCRIPTION_SUFFIX;
	private static final String	VENDOR = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFileSystem.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_VENDOR_SUFFIX;
	private static final String	LICENSE = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFileSystem.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_SUFFIX;
	private static final String	LICENSE_CONTENT = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFileSystem.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_CONTENT_SUFFIX;
	private static final String	HELP = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFileSystem.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_HELP_SUFFIX;
	private static final Icon	ICON = new ImageIcon(FileSystemOnFileSystem.class.getResource("fsysIcon.png"));
	
	private final FileSystem		fs;
	private final boolean			needClose;

	/**
	 * <p>This constructor is an entry for the SPI service only. Don't use it in any purposes</p> 
	 */
	public FileSystemOnFileSystem(){
		this.fs = null;
		this.needClose = false;
	}
	
	/**
	 * <p>Create the file system for the given file system type and path</p>.  
	 * @param rootPath root directory for the file system. Need be absolute URI with the schema 'fsys:filesystemtype:', for example <code>'fsys:jar:c:/mydir'</code>
	 * @throws IOException if any exception was thrown
	 */
	public FileSystemOnFileSystem(final URI rootPath) throws IOException {
		super(rootPath);
		final Properties	props = new Properties();
		
		if (rootPath.getQuery() != null) {
			try(final StringReader	rdr = new StringReader(rootPath.getQuery().replace('&','\n'))) {
				props.load(rdr);
			}
		}

		final Map<String,?> 	env = Collections.emptyMap();
		final URI				ref = URI.create(rootPath.getSchemeSpecificPart());
		FileSystemProvider 		found = null;
		
        for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
            if (ref.getScheme() != null && ref.getScheme().equals(provider.getScheme())) {
            	found = provider;
            	break;
            }
        }
        if (found == null) {
        	throw new IllegalArgumentException("File system for scheme ["+ref.getScheme()+"] is not installed"); 
        }
        else {
        	FileSystem	fs;
        	boolean		needClose;
        	
        	try{fs = found.getFileSystem(ref);
        		needClose = false;
        	} catch (FileSystemNotFoundException exc) {
        		fs = found.newFileSystem(ref, env);
        		needClose = true;
        	}
        	 
			this.fs = fs;
			this.needClose = needClose;
        }
	}
	
	private FileSystemOnFileSystem(final FileSystemOnFileSystem another) {
		super(another);
		this.fs = another.fs;
		this.needClose = false;
	}

	@Override
	public boolean canServe(final URI resource) {
		return URIUtils.canServeURI(resource,SERVE);
	}
	
	@Override
	public FileSystemInterface newInstance(final URI resource) throws EnvironmentException {
		if (!canServe(resource)) {
			throw new EnvironmentException("Resource URI ["+resource+"] is not supported by the class. Valid URI must be ["+SERVE+"...]");
		}
		else {
			try{return new FileSystemOnFileSystem(URI.create(resource.getRawSchemeSpecificPart()));
			} catch (IOException e) {
				throw new EnvironmentException("I/O error creating file system on filesystem: "+e.getLocalizedMessage(),e); 
			}
		}
	}
	
	@Override
	public void close() throws IOException {
		if (needClose) {
			fs.close();
		}
		super.close();
	}
	
	@Override
	public FileSystemInterface clone() {
		return new FileSystemOnFileSystem(this);
	}
	
	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		return new FileSystemDataWrapperInterface(actualPath,fs);
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
		return PureLibLocalizer.LOCALIZER_SCHEME_URI;
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

	@Override
	protected boolean sharedModeCheckRequired() {
		return false;
	}

	@Override
	protected AutoCloseable createLockerSource(String path) throws IOException {
		throw new IOException("Locking mechanism is not oficially supported on file systems but default");
	}

	@Override
	protected AutoCloseable tryCreateLocker(AutoCloseable source, String path, boolean sharedMode) throws IOException {
		throw new IOException("Locking mechanism is not oficially supported on file systems but default");
	}

	@Override
	protected AutoCloseable createLocker(AutoCloseable source, String path, boolean sharedMode) throws IOException {
		throw new IOException("Locking mechanism is not oficially supported on file systems but default");
	}

	private static class FileSystemDataWrapperInterface implements DataWrapperInterface {
		private final URI			wrapper;
		private final FileSystem	fs;
		
		public FileSystemDataWrapperInterface(final URI wrapper, final FileSystem fs) {
			this.wrapper = wrapper;
			this.fs = fs;
		}

		@Override
		public URI[] list(Pattern pattern) throws IOException {
			final List<URI>		result = new ArrayList<>();
			
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(fs.getPath(wrapper2String()))) {
                for (Path path : ds) {
                	final Path	fileName = path.getFileName();
                	
                	if (fileName != null) {
						if (pattern.matcher(fileName.toString()).matches()) {
							final String uri = fileName.toString();
							
							result.add(URI.create(uri.endsWith("/") ? uri.substring(0,uri.length()-1) : uri));
						}
                	}
                }
            }
            
			final URI[]			returned = result.toArray(new URI[result.size()]);
			result.clear();
			return returned;
		}

		@Override
		public void mkDir() throws IOException {
            Files.createDirectory(fs.getPath(wrapper2String()));
		}

		@Override
		public void create() throws IOException {
			Files.newOutputStream(fs.getPath(wrapper2String()),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING).close();
		}

		@Override
		public void setName(final String name) throws IOException {
			final Path	path = fs.getPath(wrapper2String()), newPath = path.resolveSibling(name);
			
			Files.move(path,newPath,StandardCopyOption.REPLACE_EXISTING);
		}

		@Override
		public void delete() throws IOException {
			Files.delete(fs.getPath(wrapper2String()));
		}

		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			return Files.newOutputStream(fs.getPath(wrapper2String()),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return Files.newInputStream(fs.getPath(wrapper2String()),StandardOpenOption.READ);
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			final Path	path = fs.getPath(wrapper2String());
			
			if (Files.exists(path)) {
				return Utils.mkMap(ATTR_SIZE, Files.size(path)
						, ATTR_NAME, path.getFileName() == null ? "/" : path.getFileName().toString()
						, ATTR_ALIAS, path.getFileName() == null ? "/" : path.getFileName().toString()
						, ATTR_LASTMODIFIED, path.getFileName() == null ? 1 : Files.getLastModifiedTime(path).toMillis()
						, ATTR_DIR, Files.isDirectory(path)
						, ATTR_EXIST, Files.exists(path)
						, ATTR_CANREAD, Files.isReadable(path)
						, ATTR_CANWRITE, Files.isWritable(path));
			}
			else {
				return Utils.mkMap(ATTR_SIZE, 0
						, ATTR_NAME, path.getFileName() == null ? "/" : path.getFileName().toString()
						, ATTR_ALIAS, path.getFileName() == null ? "/" : path.getFileName().toString()
						, ATTR_LASTMODIFIED, 0
						, ATTR_DIR, false
						, ATTR_EXIST, false
						, ATTR_CANREAD, false
						, ATTR_CANWRITE, false);
			}
			
		}

		@Override public void linkAttributes(Map<String, Object> attributes) throws IOException {}
		
		private String wrapper2String() {
			return wrapper.toString();
		}

		@Override
		public boolean tryLock(final String path, final boolean sharedMode) throws IOException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void lock(final String path, final boolean sharedMode) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void unlock(final String path, final boolean sharedMode) throws IOException {
			// TODO Auto-generated method stub
			
		}
	}
}
