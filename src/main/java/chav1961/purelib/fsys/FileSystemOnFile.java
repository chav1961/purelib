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
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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
 * @last.update 0.0.5
 */

public class FileSystemOnFile extends AbstractFileSystemWithLockService<FileChannel,FileLock> implements FileSystemInterfaceDescriptor {
	private static final URI	SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/");
	private static final String	DESCRIPTION = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFile.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_DESCRIPTION_SUFFIX;
	private static final String	VENDOR = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFile.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_VENDOR_SUFFIX;
	private static final String	LICENSE = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFile.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_SUFFIX;
	private static final String	LICENSE_CONTENT = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFile.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_CONTENT_SUFFIX;
	private static final String	HELP = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFile.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_HELP_SUFFIX;
	private static final Icon	ICON = new ImageIcon(FileSystemOnFile.class.getResource("fileIcon.png"));
	private static final URI	ROOT_URI = URI.create("file:/");
	
	private	final URI					rootPath;
	private final boolean				multiRoot;

	/**
	 * <p>This constructor is an entry for the SPI service only. Don't use it in any purposes</p> 
	 */
	public FileSystemOnFile(){
		rootPath = null;
		multiRoot = false;
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
			final File[]	roots = File.listRoots();
			
			if (roots.length > 1) {
				multiRoot = true;
			}
			else {
				multiRoot = roots[0].getPath().replace('\\','/').endsWith(":/");
			}
					
			if (rootPath.getAuthority() != null) {
				if (".".equals(rootPath.getAuthority())) {
					this.rootPath = URI.create(substitutePredefinedValues(rootPath.getPath()));
				}
				else {
					this.rootPath = rootPath.normalize();
				}
			}
			else {
				this.rootPath = rootPath.normalize();
			}
		}
	}

	protected FileSystemOnFile(final FileSystemOnFile another) {
		super(another);
		this.rootPath = another.rootPath;
		this.multiRoot = another.multiRoot;
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
			try{final URI	uriPath = URI.create(resource.getRawSchemeSpecificPart());
				final File	f = new File(uriPath.getRawSchemeSpecificPart()).getAbsoluteFile();
				
				return new FileSystemOnFile(f.toURI());
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
		switch (PureLibSettings.CURRENT_OS) {
			case LINUX		:
				return new LinuxFileDataWrapper(multiRoot,actualPath,rootPath);
			case MACOS		:
				return new MacOSFileDataWrapper(multiRoot,actualPath,rootPath);
			case WINDOWS	:
				return new WindowsFileDataWrapper(multiRoot,actualPath,rootPath);
			case UNKNOWN	:
			default:
				throw new UnsupportedOperationException("Current OS ["+PureLibSettings.CURRENT_OS+"] is not supported");
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
	protected FileChannel createLockerSource(final String path) throws IOException {
		final RootsAndWrapper	raw = new RootsAndWrapper(multiRoot, URI.create(path), rootPath);
		final File				f = new File(raw.wrapper.getSchemeSpecificPart());
		
		if (f.exists() && f.isDirectory()) {
			throw new IOException("Resource ["+raw.wrapper+"] is directory, not file!"); 
		} else if (!f.exists()) {
			Files.write(f.toPath(), new byte[] {0}, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		}
		return new FileOutputStream(f).getChannel();
	}

	@Override
	protected FileLock tryCreateLocker(final FileChannel source, final String path, final boolean sharedMode) throws IOException {
		return source.tryLock(0, 1, sharedMode);
	}

	@Override
	protected FileLock createLocker(FileChannel source, String path, boolean sharedMode) throws IOException {
		return source.lock(0, 1, sharedMode);
	}
	
	private static class WindowsFileDataWrapper implements DataWrapperInterface {
		private final boolean	atRoot;
		private final boolean	atRootItem;
		private final URI		wrapper;
		
		public WindowsFileDataWrapper(final boolean multiRoot, final URI wrapper, final URI rootPath) throws UnsupportedEncodingException {
			final RootsAndWrapper	raw = new RootsAndWrapper(multiRoot, wrapper, rootPath); 
			
			this.wrapper = raw.wrapper;
			this.atRoot = raw.atRoot;
			this.atRootItem = raw.atRootItem;
		}

		@Override
		public OutputStream getOutputStream(final boolean append) throws IOException {
			return new FileOutputStream(getFile(),append);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new FileInputStream(getFile());
		}

		@Override
		public URI[] list(final Pattern pattern) throws IOException {
			if (atRoot) {
				final File[]	roots = File.listRoots();
				final URI[]		returned = new URI[roots.length];
				
				for (int index = 0; index < returned.length; index++) {
					returned[index] = roots[index].toURI();
				}
				return returned;
			}
			else {
				final List<URI>		result = new ArrayList<>();
				
				getFile().listFiles(new FileFilter(){
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
		}

		@Override
		public void mkDir() throws IOException {
			if (!getFile().mkdirs()) {
				throw new IOException("Directory ["+wrapper+"] was not created");
			}
		}

		@Override
		public void create() throws IOException {
			try(final OutputStream os = new FileOutputStream(getFile())) {
			}
		}

		@Override
		public void delete() throws IOException {
			if (!getFile().delete()) {
				throw new IOException("Directory/file ["+wrapper+"] was not deleted");
			}
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			final Map<String, Object>	result;
			
			if (atRoot) {
				result = Utils.mkMap(ATTR_SIZE, 0, 
						ATTR_NAME, "/", 
						ATTR_ALIAS, "/", 
						ATTR_LASTMODIFIED, Long.valueOf(0), 
						ATTR_DIR, true, 
						ATTR_EXIST, true, 
						ATTR_CANREAD, true, 
						ATTR_CANWRITE, true);
			}
			else if (atRootItem) {
				final File	temp = getFile();
				
				result = Utils.mkMap(ATTR_SIZE, 0, 
						ATTR_NAME, temp.getPath().substring(0, 2), 
						ATTR_ALIAS, temp.getPath().substring(0, 2), 
						ATTR_LASTMODIFIED, Long.valueOf(0), 
						ATTR_DIR, true, 
						ATTR_EXIST, true, 
						ATTR_CANREAD, true, 
						ATTR_CANWRITE, true);
			}
			else {
				final File	temp = getFile();
				
				result = Utils.mkMap(ATTR_SIZE, temp.length(), 
						ATTR_NAME, temp.getName(), 
						ATTR_ALIAS, temp.getName(), 
						ATTR_LASTMODIFIED, temp.lastModified(), 
						ATTR_DIR, temp.isDirectory(), 
						ATTR_EXIST, temp.exists(), 
						ATTR_CANREAD, temp.canRead(), ATTR_CANWRITE, temp.canWrite());
			}
			return result;
		}

		@Override 
		public void linkAttributes(final Map<String, Object> attributes) throws IOException {
		}
		
		@Override
		public void setName(final String name) throws IOException {
			final File	oldFile = getFile(), newFile = new File(oldFile.getParent(),name);
			
			if (!oldFile.renameTo(newFile)) {
				throw new IOException("Directory/file ["+wrapper+"] was not renamed");
			}
		}
		
		private File getFile() throws IOException {
			return new File(wrapper.getSchemeSpecificPart());
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

	private static class LinuxFileDataWrapper implements DataWrapperInterface {
		private final boolean	atRoot;
		private final boolean	atRootItem;
		private final URI		wrapper;
		
		public LinuxFileDataWrapper(final boolean multiRoot, final URI wrapper, final URI rootPath) throws UnsupportedEncodingException {
			final RootsAndWrapper	raw = new RootsAndWrapper(multiRoot, wrapper, rootPath); 
			
			this.wrapper = raw.wrapper;
			this.atRoot = raw.atRoot;
			this.atRootItem = raw.atRootItem;
		}

		@Override
		public OutputStream getOutputStream(final boolean append) throws IOException {
			return new FileOutputStream(getFile(),append);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new FileInputStream(getFile());
		}

		@Override
		public URI[] list(final Pattern pattern) throws IOException {
			if (atRoot) {
				final File[]	roots = File.listRoots();
				final URI[]		returned = new URI[roots.length];
				
				for (int index = 0; index < returned.length; index++) {
					returned[index] = roots[index].toURI();
				}
				return returned;
			}
			else {
				final List<URI>		result = new ArrayList<>();
				
				getFile().listFiles(new FileFilter(){
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
		}

		@Override
		public void mkDir() throws IOException {
			if (!getFile().mkdirs()) {
				throw new IOException("Directory ["+wrapper+"] was not created");
			}
		}

		@Override
		public void create() throws IOException {
			try(final OutputStream os = new FileOutputStream(getFile())) {
			}
		}

		@Override
		public void delete() throws IOException {
			if (!getFile().delete()) {
				throw new IOException("Directory/file ["+wrapper+"] was not deleted");
			}
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			final Map<String, Object>	result;
			
			if (atRoot) {
				result = Utils.mkMap(ATTR_SIZE, 0,
						ATTR_NAME, "/", 
						ATTR_ALIAS, "/", 
						ATTR_LASTMODIFIED, Long.valueOf(0), 
						ATTR_DIR, true, 
						ATTR_EXIST, true, 
						ATTR_CANREAD, true, 
						ATTR_CANWRITE, true);
			}
			else if (atRootItem) {
				final File	temp = getFile();
				
				result = Utils.mkMap(ATTR_SIZE, 0, 
						ATTR_NAME, temp.getName(), 
						ATTR_ALIAS, temp.getName(), 
						ATTR_LASTMODIFIED, Long.valueOf(0), 
						ATTR_DIR, true, 
						ATTR_EXIST, true, 
						ATTR_CANREAD, true, 
						ATTR_CANWRITE, true);
			}
			else {
				final File	temp = getFile();
				
				result = Utils.mkMap(ATTR_SIZE, temp.length(), 
						ATTR_NAME, temp.getName(), 
						ATTR_ALIAS, temp.getName(), 
						ATTR_LASTMODIFIED, temp.lastModified(), 
						ATTR_DIR, temp.isDirectory(), 
						ATTR_EXIST, temp.exists(), 
						ATTR_CANREAD, temp.canRead(), 
						ATTR_CANWRITE, temp.canWrite());
			}
			return result;
		}

		@Override 
		public void linkAttributes(final Map<String, Object> attributes) throws IOException {
		}
		
		@Override
		public void setName(final String name) throws IOException {
			final File	oldFile = getFile(), newFile = new File(oldFile.getParent(),name);
			
			if (!oldFile.renameTo(newFile)) {
				throw new IOException("Directory/file ["+wrapper+"] was not renamed");
			}
		}
		
		private File getFile() {
			return new File(wrapper.getSchemeSpecificPart());
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

	private static class MacOSFileDataWrapper implements DataWrapperInterface {
		private final boolean	atRoot;
		private final boolean	atRootItem;
		private final URI		wrapper;
		
		public MacOSFileDataWrapper(final boolean multiRoot, final URI wrapper, final URI rootPath) throws UnsupportedEncodingException {
			final RootsAndWrapper	raw = new RootsAndWrapper(multiRoot, wrapper, rootPath); 
			
			this.wrapper = raw.wrapper;
			this.atRoot = raw.atRoot;
			this.atRootItem = raw.atRootItem;
		}

		@Override
		public OutputStream getOutputStream(final boolean append) throws IOException {
			return new FileOutputStream(getFile(),append);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new FileInputStream(getFile());
		}

		@Override
		public URI[] list(final Pattern pattern) throws IOException {
			if (atRoot) {
				final File[]	roots = File.listRoots();
				final URI[]		returned = new URI[roots.length];
				
				for (int index = 0; index < returned.length; index++) {
					returned[index] = roots[index].toURI();
				}
				return returned;
			}
			else {
				final List<URI>		result = new ArrayList<>();
				
				getFile().listFiles(new FileFilter(){
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
		}

		@Override
		public void mkDir() throws IOException {
			if (!getFile().mkdirs()) {
				throw new IOException("Directory ["+wrapper+"] was not created");
			}
		}

		@Override
		public void create() throws IOException {
			try(final OutputStream os = new FileOutputStream(getFile())) {
			}
		}

		@Override
		public void delete() throws IOException {
			if (!getFile().delete()) {
				throw new IOException("Directory/file ["+wrapper+"] was not deleted");
			}
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			final Map<String, Object>	result;
			
			if (atRoot) {
				result = Utils.mkMap(ATTR_SIZE, 0, 
						ATTR_NAME, "/", 
						ATTR_ALIAS, "/", 
						ATTR_LASTMODIFIED, Long.valueOf(0), 
						ATTR_DIR, true, 
						ATTR_EXIST, true, 
						ATTR_CANREAD, true, 
						ATTR_CANWRITE, true);
			}
			else if (atRootItem) {
				final File	temp = getFile();
				
				result = Utils.mkMap(ATTR_SIZE, 0, 
						ATTR_NAME, temp.getName(), 
						ATTR_ALIAS, temp.getName(), 
						ATTR_LASTMODIFIED, Long.valueOf(0), 
						ATTR_DIR, true, 
						ATTR_EXIST, true, 
						ATTR_CANREAD, true, 
						ATTR_CANWRITE, true);
			}
			else {
				final File	temp = getFile();
				
				result = Utils.mkMap(ATTR_SIZE, temp.length(), 
						ATTR_NAME, temp.getName(), 
						ATTR_ALIAS, temp.getName(), 
						ATTR_LASTMODIFIED, temp.lastModified(), 
						ATTR_DIR, temp.isDirectory(), 
						ATTR_EXIST, temp.exists(), 
						ATTR_CANREAD, temp.canRead(), 
						ATTR_CANWRITE, temp.canWrite());
			}
			return result;
		}

		@Override 
		public void linkAttributes(final Map<String, Object> attributes) throws IOException {
		}
		
		@Override
		public void setName(final String name) throws IOException {
			final File	oldFile = getFile(), newFile = new File(oldFile.getParent(),name);
			
			if (!oldFile.renameTo(newFile)) {
				throw new IOException("Directory/file ["+wrapper+"] was not renamed");
			}
		}
		
		private File getFile() {
			return new File(wrapper.getSchemeSpecificPart());
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
	
	private static class RootsAndWrapper {
		private final boolean	atRoot;
		private final boolean	atRootItem;
		private final URI		wrapper;
		
		public RootsAndWrapper(final boolean multiRoot, final URI wrapper, final URI rootPath) throws UnsupportedEncodingException {
			final String	decodedWrapper = wrapper.isAbsolute() ? wrapper.getRawSchemeSpecificPart() : wrapper.toString();
			final String	relative = decodedWrapper.replace(File.pathSeparator,"/"), root = rootPath.toString().replace(File.pathSeparator,"/"); 

			if (ROOT_URI.equals(rootPath) && multiRoot) {
				if (decodedWrapper.isEmpty() || "/".equals(decodedWrapper)) {
					this.wrapper = rootPath;
					this.atRoot = true;
					this.atRootItem = false;
				}
				else if (decodedWrapper.endsWith(":/")) {
					this.wrapper = toNormalizedURI(rootPath+decodedWrapper.substring(1)); 
					this.atRoot = false;
					this.atRootItem = true;
				}
				else if (decodedWrapper.endsWith(":")) {
					this.wrapper = toNormalizedURI(rootPath+decodedWrapper.substring(1)+'/'); 
					this.atRoot = false;
					this.atRootItem = true;
				}
				else {
					if (relative.startsWith("/") && root.endsWith("/")) {
						this.wrapper = toNormalizedURI(rootPath+relative.substring(1));
						this.atRoot = false;
						this.atRootItem = false;
					}
					else {
						this.wrapper = toNormalizedURI(rootPath.toString()+wrapper.toString());
						this.atRoot = false;
						this.atRootItem = false;
					}
				}
			}
			else if (relative.startsWith("/") && root.endsWith("/")) {
				this.wrapper = toNormalizedURI(rootPath+relative.substring(1));
				this.atRoot = false;
				this.atRootItem = false;
			}
			else {
				this.wrapper = toNormalizedURI(rootPath.toString()+wrapper.toString());
				this.atRoot = false;
				this.atRootItem = false;
			}
		}

		@Override
		public String toString() {
			return "RootsAndWrapper [atRoot=" + atRoot + ", atRootItem=" + atRootItem + ", wrapper=" + wrapper + "]";
		}
		
		private URI toNormalizedURI(final String content) throws UnsupportedEncodingException {
			return URI.create(content).normalize();
		}
 	}
}
