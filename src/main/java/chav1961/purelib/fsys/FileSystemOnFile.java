package chav1961.purelib.fsys;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.i18n.PureLibLocalizer;

/**
 * <p>This class implements the file system interface on the usual file system. The URI to use this class is 
 * <code>URI.create("file:file:path_to_root_directory");</code> (for example <code>URI.create("file:file:./muRootDirectory");</code>)</p>
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface FileSystemInterface
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.3
 */

public class FileSystemOnFile extends AbstractFileSystem implements FileSystemInterfaceDescriptor {
	private static final URI	SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/");
	private static final String	DESCRIPTION = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFile.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_DESCRIPTION_SUFFIX;
	private static final String	VENDOR = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFile.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_VENDOR_SUFFIX;
	private static final String	LICENSE = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFile.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_SUFFIX;
	private static final String	LICENSE_CONTENT = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFile.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_CONTENT_SUFFIX;
	private static final String	HELP = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFile.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_HELP_SUFFIX;
	private static final Icon	ICON = new ImageIcon(FileSystemOnFile.class.getResource("fileIcon.png"));
	
	private	final URI					rootPath;

	/**
	 * <p>This constructor is an entry for the SPI service only. Don't use it in any purposes</p> 
	 */
	public FileSystemOnFile(){
		rootPath = null;
	}
	
	/**
	 * <p>Create the file system for the given directory.</p>  
	 * @param rootPath root directory for the file system. Need be absolute URI with the schema 'file', for example <code>'file://./c:/mydir'</code>
	 * @throws IOException if any exception was thrown
	 */
	public FileSystemOnFile(final URI rootPath) throws IOException {
		super(rootPath);
		if (!rootPath.isAbsolute()) {
			throw new IllegalArgumentException("Root path ["+rootPath+"] is not absolute URI or not contains scheme");
		}
		else if (!rootPath.getScheme().equals("file")) {
			throw new IllegalArgumentException("Root path ["+rootPath+"] not contains 'file:' as scheme");
		}
		else {
			this.rootPath = rootPath.normalize();
		}
	}

	private FileSystemOnFile(final FileSystemOnFile another) {
		super(another);
		this.rootPath = another.rootPath;
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
			try{return new FileSystemOnFile(URI.create(resource.getRawSchemeSpecificPart()));
			} catch (IOException e) {
				throw new EnvironmentException("I/O error creatinf file system on file: "+e.getLocalizedMessage(),e);
			}
		}
	}
	
	@Override
	public FileSystemInterface clone() {
		return new FileSystemOnFile(this);
	}

	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		return new FileDataWrapper(actualPath,rootPath);
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
	
	private static class FileDataWrapper implements DataWrapperInterface {
		private final URI	wrapper;
		
		public FileDataWrapper(final URI wrapper, final URI rootPath) throws UnsupportedEncodingException {
			final String	relative = wrapper.toString().replace(File.pathSeparator,"/"), root = rootPath.toString().replace(File.pathSeparator,"/"); 
			
			if (relative.startsWith("/") && root.endsWith("/")) {
				this.wrapper = URI.create(rootPath+relative.substring(1)).normalize();
			}
			else {
				this.wrapper = URI.create(rootPath.toString()+wrapper.toString()).normalize();
			}
		}

		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			return new FileOutputStream(new File(wrapper.getSchemeSpecificPart()),append);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new FileInputStream(new File(wrapper.getSchemeSpecificPart()));
		}

		@Override
		public URI[] list(final Pattern pattern) throws IOException {
			final List<URI>		result = new ArrayList<>();
			
			new File(wrapper.getSchemeSpecificPart()).listFiles(new FileFilter(){
					@Override
					public boolean accept(final File pathname) {
						if (pattern.matcher(pathname.getName()).matches()) {
							final String uri = wrapper.relativize(pathname.toURI()).toString();
							
							result.add(URI.create(uri.endsWith("/") ? uri.substring(0,uri.length()-1) : uri));
						}
						return false;
					}
				}
			);
			
			final URI[]			returned = result.toArray(new URI[result.size()]);
			result.clear();
			return returned;
		}

		@Override
		public void mkDir() throws IOException {
			if (!new File(wrapper).mkdirs()) {
				throw new IOException("Directory ["+wrapper+"] was not created");
			}
		}

		@Override
		public void create() throws IOException {
			try(final OutputStream os = new FileOutputStream(new File(wrapper.getSchemeSpecificPart()))) {
			}
		}

		@Override
		public void delete() throws IOException {
			if (!new File(wrapper).delete()) {
				throw new IOException("Directory/file ["+wrapper+"] was not deleted");
			}
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			final File	temp = new File(wrapper.getSchemeSpecificPart());
			return Utils.mkMap(ATTR_SIZE, temp.length(), ATTR_NAME, temp.getName(), ATTR_LASTMODIFIED, temp.lastModified(), ATTR_DIR, temp.isDirectory(), ATTR_EXIST, temp.exists(), ATTR_CANREAD, temp.canRead(), ATTR_CANWRITE, temp.canWrite());
		}

		@Override public void linkAttributes(Map<String, Object> attributes) throws IOException {}
		
		@Override
		public void setName(final String name) throws IOException {
			final File	oldFile = new File(wrapper), newFile = new File(oldFile.getParent(),name);
			
			if (!oldFile.renameTo(newFile)) {
				throw new IOException("Directory/file ["+wrapper+"] was not renamed");
			}
		}		
	}
}
