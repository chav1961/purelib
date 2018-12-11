package chav1961.purelib.fsys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class implements the file system interface in the RAM. The URI for this class is not used really, so type any valid URI to pass it in the constructor</p>
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface FileSystemInterface
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */


public class FileSystemInMemory extends AbstractFileSystem {
	private static final URI				SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":memory:/");
	
	private final Map<String,MemoryDesc>	content;

	/**
	 * <p>This constructor is an entry for the SPI service only. Don't use it in any purposes</p> 
	 */
	public FileSystemInMemory(){
		this.content = null;
	}

	/**
	 * <p>Create the file system in the memory.</p>  
	 * @param rootPath any valid URI. It is not used really, but need for the compatibility
	 */
	
	public FileSystemInMemory(final URI rootPath) {
		super(rootPath);
		this.content = new HashMap<>();
		content.put("/",new MemoryDesc("/",Utils.mkMap(DataWrapperInterface.ATTR_SIZE, 0L, DataWrapperInterface.ATTR_NAME, "/", DataWrapperInterface.ATTR_LASTMODIFIED, System.currentTimeMillis(), DataWrapperInterface.ATTR_DIR, true, DataWrapperInterface.ATTR_EXIST, true, DataWrapperInterface.ATTR_CANREAD, true, DataWrapperInterface.ATTR_CANWRITE, true)));
	}
	
	private FileSystemInMemory(final FileSystemInMemory another) {
		super(another);
		this.content = another.content;
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
			final List<URI>	result = new ArrayList<>();
			final int		startPathLen = wrapper.length();
			
			for(Entry<String, MemoryDesc> item : content.entrySet()) {
				final String	uri = item.getKey();
				
				if (uri.length() > startPathLen && uri.startsWith(wrapper) && pattern.matcher(uri.substring(startPathLen)).matches()) {
					result.add(URI.create(uri.endsWith("/") ? uri.substring(startPathLen+1,uri.length()-1) : uri.substring(startPathLen+1,uri.length())));
				}
			}
			
			final URI[]			returned = result.toArray(new URI[result.size()]);
			result.clear();
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
			if (content.containsKey(wrapper)) {
				return content.get(wrapper).attributes;
			}
			else {
				return Utils.mkMap(DataWrapperInterface.ATTR_SIZE, 0L, DataWrapperInterface.ATTR_NAME, getName(), DataWrapperInterface.ATTR_LASTMODIFIED, 0, DataWrapperInterface.ATTR_DIR, false, DataWrapperInterface.ATTR_EXIST, false, DataWrapperInterface.ATTR_CANREAD, false, DataWrapperInterface.ATTR_CANWRITE, false);						
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
