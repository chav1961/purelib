package chav1961.purelib.fsys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
 * <p>This class implements the file system interface in the RAM. The URI for this class is not used really, so type any valid URI to pass it in the constructor</p>
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface FileSystemInterface
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.5
 */


public class FileSystemInMemory extends AbstractFileSystem implements FileSystemInterfaceDescriptor {
	public static final URI		SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":memory:/");
	
	private static final String	DESCRIPTION = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemInMemory.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_DESCRIPTION_SUFFIX;
	private static final String	VENDOR = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemInMemory.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_VENDOR_SUFFIX;
	private static final String	LICENSE = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemInMemory.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_SUFFIX;
	private static final String	LICENSE_CONTENT = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemInMemory.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_CONTENT_SUFFIX;
	private static final String	HELP = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemInMemory.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_HELP_SUFFIX;
	private static final Icon	ICON = new ImageIcon(FileSystemInMemory.class.getResource("memoryIcon.png"));
	
	private final Map<String,MemoryDesc>	content;
	private final FileSystemInMemory		another;
	private final boolean					cloned;
	private final InMemoryFileSystemLocker			lock;

	/**
	 * <p>This constructor is an entry for the SPI service only. Don't use it in any purposes</p> 
	 */
	public FileSystemInMemory(){
		this.content = new HashMap<>();
		this.another = null;
		this.cloned = false;
		this.lock = new InMemoryFileSystemLocker(false);
	}

	/**
	 * <p>Create the file system in the memory.</p>  
	 * @param rootPath any valid URI. It is not used really, but need for the compatibility
	 */
	
	public FileSystemInMemory(final URI rootPath) {
		super(rootPath);
		this.content = new HashMap<>();
		content.put("/",new MemoryDesc("/",Utils.mkMap(DataWrapperInterface.ATTR_SIZE, 0L, DataWrapperInterface.ATTR_NAME, "/", DataWrapperInterface.ATTR_LASTMODIFIED, System.currentTimeMillis(), DataWrapperInterface.ATTR_DIR, true, DataWrapperInterface.ATTR_EXIST, true, DataWrapperInterface.ATTR_CANREAD, true, DataWrapperInterface.ATTR_CANWRITE, true)));
		this.another = null;
		this.cloned = false;
		this.lock = new InMemoryFileSystemLocker(false);
	}
	
	protected FileSystemInMemory(final FileSystemInMemory another) {
		super(another);
		this.content = another.content;
		this.another = another;
		this.cloned = true;
		this.lock =  null;
	}

	@Override
	public void close() throws IOException {
		if (!cloned) {
			super.close();
		}
		else {
			another.purgeCurrentDataWrapper();
		}
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
			return new FileSystemInMemory(URI.create(resource.getRawSchemeSpecificPart()));
		}
	}
	
	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		return new MemoryDataWrapper(actualPath);
	}

	@Override
	public FileSystemInterface clone() {
		return new FileSystemInMemory(this);
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
	
	private class MemoryDataWrapper implements DataWrapperInterface {
		private final String	wrapper;
		
		public MemoryDataWrapper(final URI wrapper) {
			this.wrapper = wrapper.toString();
		}

		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			final MemoryDesc	desc = content.get(wrapper);
			
			return new ByteArrayOutputStream(){
				@Override
				public void close() throws IOException {
					desc.content = this.buf;
					desc.attributes.put(DataWrapperInterface.ATTR_SIZE,Long.valueOf(this.count));
					super.close();
				}
			};
		}

		@Override
		public InputStream getInputStream() throws IOException {
			final MemoryDesc	desc = content.get(wrapper);
			
			return new ByteArrayInputStream(desc.content,0,((Long)desc.attributes.get(DataWrapperInterface.ATTR_SIZE)).intValue());
		}

		@Override
		public URI[] list(final Pattern pattern) throws IOException {
			final Set<URI>	result = new HashSet<>();
			final int		startPathLen = wrapper.length();
			
			for(Entry<String, MemoryDesc> item : content.entrySet()) {
				final String	uri = item.getKey();
				
//				if (uri.length() > startPathLen && uri.startsWith(wrapper) && pattern.matcher(uri.substring(startPathLen)).matches()) {
				if (uri.length() > startPathLen && uri.startsWith(wrapper)) {
					String		curPath = uri.substring(startPathLen+1);
					int			trimIndex = curPath.indexOf('/');
					
					if (trimIndex > 0 ) {
						curPath = curPath.substring(0,trimIndex); 
					}
					if (pattern.matcher(curPath).matches()) {
						result.add(URI.create(curPath));
//						result.add(URI.create(uri.endsWith("/") ? uri.substring(startPathLen,uri.length()-1) : uri.substring(startPathLen,uri.length())));
					}
				}
			}
			
			final URI[]			returned = result.toArray(new URI[result.size()]);
			return returned;
		}

		@Override
		public void mkDir() throws IOException {
			content.put(wrapper,new MemoryDesc(wrapper,Utils.mkMap(DataWrapperInterface.ATTR_SIZE, 0L, DataWrapperInterface.ATTR_NAME, getName(), DataWrapperInterface.ATTR_LASTMODIFIED, System.currentTimeMillis(), DataWrapperInterface.ATTR_DIR, true, DataWrapperInterface.ATTR_EXIST, true, DataWrapperInterface.ATTR_CANREAD, true, DataWrapperInterface.ATTR_CANWRITE, true)));
		}

		@Override
		public void create() throws IOException {
			content.put(wrapper,new MemoryDesc(wrapper,Utils.mkMap(DataWrapperInterface.ATTR_SIZE, 0L, DataWrapperInterface.ATTR_NAME, getName(), DataWrapperInterface.ATTR_LASTMODIFIED, System.currentTimeMillis(), DataWrapperInterface.ATTR_DIR, false, DataWrapperInterface.ATTR_EXIST, true, DataWrapperInterface.ATTR_CANREAD, true, DataWrapperInterface.ATTR_CANWRITE, true)));
		}

		@Override
		public void delete() throws IOException {
			content.remove(wrapper);
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			if ("/".equals(wrapper)) {
				return Utils.mkMap(DataWrapperInterface.ATTR_SIZE, 0L, DataWrapperInterface.ATTR_NAME, "/", DataWrapperInterface.ATTR_LASTMODIFIED, 0L, DataWrapperInterface.ATTR_DIR, true, DataWrapperInterface.ATTR_EXIST, true, DataWrapperInterface.ATTR_CANREAD, true, DataWrapperInterface.ATTR_CANWRITE, true);						
			}
			else if (content.containsKey(wrapper)) {
				return content.get(wrapper).attributes;
			}
			else {
				return Utils.mkMap(DataWrapperInterface.ATTR_SIZE, 0L, DataWrapperInterface.ATTR_NAME, getName(), DataWrapperInterface.ATTR_LASTMODIFIED, 0L, DataWrapperInterface.ATTR_DIR, false, DataWrapperInterface.ATTR_EXIST, false, DataWrapperInterface.ATTR_CANREAD, false, DataWrapperInterface.ATTR_CANWRITE, false);						
			}
		}

		@Override public void linkAttributes(Map<String, Object> attributes) throws IOException {}
		
		@Override
		public void setName(final String name) throws IOException {
			final MemoryDesc	desc = content.remove(wrapper);
			final String		key = wrapper.replace("/"+getName(),"/"+name);
			
			desc.attributes.put(DataWrapperInterface.ATTR_NAME, getName());
			content.put(key,desc);
		}

		@Override
		public boolean tryLock(final String path, final boolean sharedMode) throws IOException {
			if (!cloned) {
				return lock.tryLock(path, sharedMode);
			}
			else {
				return another.tryLock(path, sharedMode);
			}
		}

		@Override
		public void lock(final String path, final boolean sharedMode) throws IOException {
			if (!cloned) {
				lock.lock(path, sharedMode);
			}
			else {
				another.lock(path, sharedMode);
			}
		}

		@Override
		public void unlock(final String path, final boolean sharedMode) throws IOException {
			if (!cloned) {
				lock.unlock(path, sharedMode);
			}
			else {
				another.unlock(path, sharedMode);
			}
		}		
	}

	private static class MemoryDesc {
		private static final byte[]	EMPTY_CONTENT = new byte[0];
		
		final String				path;
		final Map<String,Object>	attributes;
		byte[]						content = EMPTY_CONTENT;
		
		public MemoryDesc(final String path, final Map<String,Object> attributes) {
			this.path = path;
			this.attributes = attributes;
		}

		@Override
		public String toString() {
			return "MemoryDesc [path=" + path + ", attributes=" + attributes + ", content=" + Arrays.toString(content) + "]";
		}
	}
}
