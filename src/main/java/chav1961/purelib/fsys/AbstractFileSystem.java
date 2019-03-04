package chav1961.purelib.fsys;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This abstract class implements the most of all {@link FileSystemInterface} functionality.</p>
 * 
 * <p>You can use this class as a basic class for your own file system implementations. To use it, you need override the three methods:</p>
 * <ul>
 * <li>{@link #canServe(URI)} method to test URI scheme this file system implementation can serve (see {@link FileSystemFactory})</li> 
 * <li>{@link #clone()} method to make a quick copy of the file system instance</li> 
 * <li>{@link #createDataWrapper(URI)} method to get direct access to the file system entity.</li> 
 * </ul>
 * 
 * <p>As a good example, see {@link chav1961.purelib.fsys.FileSystemOnFile} class implementation.</p>
 * <p>Don't use the class as a type of variables or parameters in your program. Use the {@link FileSystemInterface} interface instead in all cases</p>  
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface
 * @see chav1961.purelib.fsys.FileSystemOnFile
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public abstract class AbstractFileSystem implements FileSystemInterface {
	public static final String			DEFAULT_ENCODING = "UTF-8";
	
	private static final String			ALL_MASK = ".*";
	private static final Pattern		ALL_MASK_COMPILED = Pattern.compile(ALL_MASK);
	private static final URI			ROOT_URI = URI.create("/");

	protected final URI 				rootPath;
	
	private final Map<String,Object>	emptyMap = new HashMap<String,Object>();
	private final List<URI>				stack = new ArrayList<URI>();
	private final Map<URI,FileSystemInterface>	mounts = new HashMap<URI,FileSystemInterface>();
	
	private	URI							currentPath = null, prevPath = null;
	private Redirection					redirection = null;
	private DataWrapperInterface		prevWrapper = null;
	private boolean						appendMode = false;
	
	public AbstractFileSystem(final URI rootPath) {
		if (rootPath == null) {
			throw new NullPointerException("Root path can't be null");
		}
		else {
			this.rootPath = rootPath;
			this.currentPath = URI.create("/");
		}
	}

	protected AbstractFileSystem() {
		this.rootPath = null;
		this.currentPath = URI.create("/");
	}
	
	protected AbstractFileSystem(final AbstractFileSystem another) {
		this.rootPath = another.rootPath;
		this.currentPath = another.currentPath;
		this.stack.addAll(another.stack);
	}	

	@Override public abstract boolean canServe(final URI uriSchema);	
	@Override public abstract FileSystemInterface newInstance(final URI uriSchema) throws EnvironmentException;	
	@Override public abstract FileSystemInterface clone();
	@Override public abstract DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException;

	/**
	 * <p>Close the file system. This method also closes all mounted file systems (see {@link #mount(FileSystemInterface)} method)</p>  
	 */
	@Override
	public void close() throws IOException {
		for (FileSystemInterface item : mounts.values()) {
			item.close();
		}
		mounts.clear();
		stack.clear();		
	}

	@Override
	public String getName() throws IOException {
		final String	path = getPath();
		
		if ("/".equals(path)) {
			return "/";
		}
		else {
			final int		lastSlash = path.lastIndexOf('/');
			
			return lastSlash >= 0 ? path.substring(lastSlash+1) : path;  
		}
	}

	@Override
	public String getPath() throws IOException {
		return currentPath.toString().isEmpty() ? "/" : currentPath.normalize().toString();
	}

	@Override
	public URI toURI() throws IOException {
		try{return URI.create(FILESYSTEM_URI_SCHEME+":"+rootPath+"#"+getPath());
		} catch (IllegalArgumentException exc) {
			throw new IOException("I/O error converting current path to URI : "+exc.getMessage());
		}
	}
	
	@Override
	public boolean exists() throws IOException {
		return (Boolean)getDataWrapper(currentPath).getAttributes().get(DataWrapperInterface.ATTR_EXIST);
	}

	@Override
	public boolean isFile() throws IOException {
		return exists() && !(Boolean)getDataWrapper(currentPath).getAttributes().get(DataWrapperInterface.ATTR_DIR);
	}

	@Override
	public boolean isDirectory() throws IOException {
		return exists() && (Boolean)getDataWrapper(currentPath).getAttributes().get(DataWrapperInterface.ATTR_DIR);
	}

	@Override
	public boolean canRead() throws IOException {
		return exists() && (Boolean)getDataWrapper(currentPath).getAttributes().get(DataWrapperInterface.ATTR_CANREAD);
	}

	@Override
	public boolean canWrite() throws IOException {
		return exists() && (Boolean)getDataWrapper(currentPath).getAttributes().get(DataWrapperInterface.ATTR_CANWRITE);
	}

	@Override
	public long lastModified() throws IOException {
		if (exists()) {
			return (Long)getDataWrapper(currentPath).getAttributes().get(DataWrapperInterface.ATTR_LASTMODIFIED);
		}
		else {
			return 0;
		}
	}

	@Override
	public long size() throws IOException {
		if (exists() && isFile()) {
			return (Long)getDataWrapper(currentPath).getAttributes().get(DataWrapperInterface.ATTR_SIZE);
		}
		else {
			return 0;
		}
	}

	@Override
	public FileSystemInterface open(final String path) throws IOException {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path can't be null or empty");
		}
		else {
			return open(build(path));
		}
	}

	@Override
	public FileSystemInterface push(final String path) throws IOException {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path can't be null or empty");
		}
		else {
			return push(build(path));
		}
	}

	@Override
	public FileSystemInterface pop() throws IOException {
		if (stack.size() == 0) {
			throw new IllegalStateException("Push stack exhausted! Check program logic");
		}
		else {
			prevPath = currentPath; 
			currentPath = stack.remove(0);
			return this;
		}
	}

	@Override
	public String[] list() throws IOException {
		final List<String>	result = new ArrayList<>();
		list(fsys -> result.add(fsys.getName()));
		
		final String[]		returned = result.toArray(new String[result.size()]);		
		result.clear();
		return returned;
	}

	@Override
	public FileSystemInterface list(final FileSystemListCallbackInterface callback) throws IOException {
		if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			return list(ALL_MASK,callback);
		}
	}

	@Override
	public String[] list(final String mask) throws IOException {
		if (mask == null || mask.isEmpty()) {
			throw new IllegalArgumentException("Mask can't be null or empty");
		}
		else {
			final List<String>	result = new ArrayList<>();
			list(mask,fsys -> result.add(fsys.getPath()));
			
			final String[]		returned = result.toArray(new String[result.size()]);		
			result.clear();
			return returned;
		}
	}	
	
	@Override
	public FileSystemInterface list(final String mask, final FileSystemListCallbackInterface callback) throws IOException {
		if (mask == null || mask.isEmpty()) {
			throw new IllegalArgumentException("Mask can't be null or empty");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			return list(Pattern.compile(mask),callback);
		}
	}

	@Override
	public FileSystemInterface mkDir() throws IOException {
		if (exists()) {
			if (isFile()) {
				throw new IOException("Entity ["+getPath()+"] already exists and is a file, not directory");
			}
		}
		else {
			getDataWrapper(currentPath).mkDir();
		}
		return this;
	}

	@Override
	public FileSystemInterface create() throws IOException {
		if (exists()) {
			if (isDirectory()) {
				throw new IOException("Entity ["+getPath()+"] already exists and is a directory, not directory");
			}
		}
		else {
			getDataWrapper(currentPath).create();
		}
		appendMode = false;		
		return this;
	}

	@Override
	public FileSystemInterface append() throws IOException {
		if (exists()) {
			if (isDirectory()) {
				throw new IOException("Entity ["+getPath()+"] already exists and is a directory, not directory");
			}
		}
		else {
			throw new IOException("Entity ["+getPath()+"] is not exists. Append mode available for the existent files only");
		}
		appendMode = true;		
		return null;
	}

	@Override
	public FileSystemInterface rename(final String name) throws IOException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("New name can't be null or empty");
		}
		else if (name.contains("/") || name.contains(File.separator)) {
			throw new IllegalArgumentException("New name ["+name+"] contains path elements. Use move(...) method for such operation");
		}
		else if (getPath().equals("/")) {
			throw new IOException("Attempt to rename root!");
		}
		else {
			getDataWrapper(currentPath).setName(name);
			open("../"+name);
			return this;
		}
	}

	@Override
	public FileSystemInterface delete() throws IOException {
		if (exists()) {
			if (isFile()) {
				getDataWrapper(currentPath).delete();
			}
			else if (getDataWrapper(currentPath).list(ALL_MASK_COMPILED).length > 0) {
				throw new IOException("Attempt to delete non-empty directory entity ["+getPath()+"]");
			}
			else {
				getDataWrapper(currentPath).delete();
			}
		}
		else {
			throw new IOException("Attempt to delete non-existent entity ["+getPath()+"]");
		}
		return this;
	}

	@Override
	public FileSystemInterface deleteAll() throws IOException {
		if (exists()) {
			if (isDirectory()) {
				for (URI item : getDataWrapper(currentPath).list(ALL_MASK_COMPILED)) {
					push(build(item.toString()))
						.deleteAll()
						.pop();
				}
			}
			delete();
		}
		return this;
	}

	@Override
	public FileSystemInterface mount(final FileSystemInterface another) throws IOException {
		if (another == null) {
			throw new NullPointerException("Another file system can't be null");
		}
		else if (mounts.containsKey(currentPath)) {
			throw new IOException("There is a mound file system at the path ["+getPath()+"]. Use another path for mounting or unmount prevous file system");
		}
		else if (!exists() || !isDirectory()) {
			throw new IOException("Mount path ["+getPath()+"] is not exists or is a file, not directory");
		}
		else {
			mounts.put(currentPath,another);
			redirection = needRedirect(currentPath);
			prevPath = ROOT_URI;
			return this;
		}
	}

	@Override
	public FileSystemInterface unmount() throws IOException {
		if (!mounts.containsKey(currentPath)) {
			throw new IllegalArgumentException("Nothing mound at the path ["+getPath()+"]");
		}
		else {
			redirection = null;
			prevPath = ROOT_URI;
			return mounts.remove(currentPath); 
		}
	}

	@Override
	public boolean isMound() throws IOException {
		return mounts.containsKey(currentPath);
	}

	@Override
	public FileSystemInterface mound() throws IOException {
		return mounts.get(currentPath);
	}
	
	@Override
	public FileSystemInterface join(final FileSystemInterface another) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileSystemInterface unjoin() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isJoined() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	@Override
	public FileSystemInterface copy(final FileSystemInterface another) throws IOException {
		if (another == null) {
			throw new NullPointerException("Another file system can't be null");
		}
		else if (exists()) {
			if (isFile()) {
				try(final OutputStream	os = another.push(getName()).create().write()) {
					copy(os);
				} finally {
					another.pop();
				}
			}
			else {
				for (URI item : getDataWrapper(currentPath).list(ALL_MASK_COMPILED)) {
					((AbstractFileSystem)push(build(item.toString()))).internalCopy(another).pop();
				}
			}
		}
		return this;
	}

	@Override
	public FileSystemInterface move(final FileSystemInterface another) throws IOException {
		copy(another);
		return deleteAll();
	}

	@Override
	public FileSystemInterface copy(final OutputStream stream) throws IOException {
		if (stream == null) {
			throw new NullPointerException("Output stream can't be null");
		}
		else {
			try(final InputStream 	is = read()) {
				Utils.copyStream(is,stream);
			} catch (RuntimeException exc) {
				throw new IOException(exc);
			}
			return this;
		}
	}

	@Override
	public FileSystemInterface copy(final Writer stream) throws IOException {
		if (stream == null) {
			throw new NullPointerException("Writer can't be null");
		}
		else {
			return copy(stream,DEFAULT_ENCODING);
		}
	}

	@Override
	public FileSystemInterface copy(final Writer stream, final String encoding) throws IOException {
		if (stream == null) {
			throw new NullPointerException("Writer can't be null");
		}
		else if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("Encoding can't be null or empty");
		}
		else {
			try(final Reader 	is = charRead(encoding)) {
				Utils.copyStream(is,stream);
			} catch (RuntimeException exc) {
				throw new IOException(exc);
			}
			return this;
		}
	}

	@Override
	public Map<String, Object> getAttributes() throws IOException {
		return getDataWrapper(currentPath).getAttributes();
	}

	@Override
	public InputStream read() throws IOException {
		return read(emptyMap);
	}

	@Override
	public InputStream read(Map<String, Object> attributes) throws IOException {
		if (attributes == null) {
			throw new NullPointerException("Attributes can't be null");
		}
		else {
			return getDataWrapper(currentPath).getInputStream();
		}
	}

	@Override
	public Reader charRead() throws IOException {
		return charRead(emptyMap,DEFAULT_ENCODING);
	}

	@Override
	public Reader charRead(Map<String, Object> attributes) throws IOException {
		if (attributes == null) {
			throw new NullPointerException("Attributes can't be null");
		}
		else {
			return charRead(attributes,DEFAULT_ENCODING);
		}
	}

	@Override
	public Reader charRead(String encoding) throws IOException {
		if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("Encoding can't be null or empty");
		}
		else {
			return charRead(emptyMap,encoding);
		}
	}

	@Override
	public Reader charRead(Map<String, Object> attributes, String encoding) throws IOException {
		if (attributes == null) {
			throw new NullPointerException("Attributes can't be null");
		}
		else if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("Encoding can't be null or empty");
		}
		else {
			return new InputStreamReader(read(attributes),encoding);
		}
	}

	@Override
	public FileSystemInterface setAttributes(final Map<String, Object> attributes) throws IOException {
		if (attributes == null) {
			throw new NullPointerException("Attributes can't be null");
		}
		else {
			return this;
		}
	}

	@Override
	public FileSystemInterface setAttributes(final Object... properties) throws IOException {
		return setAttributes(Utils.mkMap(properties));
	}

	@Override
	public OutputStream write() throws IOException {
		return write(emptyMap);
	}

	@Override
	public OutputStream write(final Map<String, Object> attributes) throws IOException {
		if (attributes == null) {
			throw new NullPointerException("Attributes can't be null");
		}
		else {
			return getDataWrapper(currentPath).getOutputStream(appendMode);
		}
	}

	@Override
	public Writer charWrite() throws IOException {
		return charWrite(emptyMap,DEFAULT_ENCODING);
	}

	@Override
	public Writer charWrite(final Map<String, Object> attributes) throws IOException {
		if (attributes == null) {
			throw new NullPointerException("Attributes can't be null");
		}
		else {
			return charWrite(attributes,DEFAULT_ENCODING);
		}
	}

	@Override
	public Writer charWrite(final String encoding) throws IOException {
		if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("Encoding can't be null or empty");
		}
		else {
			return charWrite(emptyMap,encoding);
		}
	}

	@Override
	public Writer charWrite(final Map<String, Object> attributes, String encoding) throws IOException {
		if (attributes == null) {
			throw new NullPointerException("Attributes can't be null");
		}
		else if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("Encoding can't be null or empty");
		}
		else {
			return new OutputStreamWriter(write(attributes),encoding);
		}
	}

	@Override
	public String toString() {
		return "FileSystemFS [currentPath=" + currentPath + ", appendMode=" + appendMode + "]";
	}

	
	private DataWrapperInterface getDataWrapper(final URI entityPath) throws IOException {
		if (prevPath != null && entityPath.equals(prevPath)) {	// Micro-cache to optimize chained operations
			return prevWrapper;
		}
		else if (redirection != null) {		// Processing mounted file systems
			prevPath = entityPath;
			return prevWrapper = redirection.filesystem.createDataWrapper(URI.create("/"+redirection.rootURI.relativize(entityPath).toString()));
		}
		else {
			prevPath = entityPath;
			return prevWrapper = createDataWrapper(entityPath);
		}
	}
	
	private URI build(final String delta) throws IOException {
		final StringBuilder sb = new StringBuilder();
		final List<String>	result = new ArrayList<String>();
		final String[]		newPart = delta.split("\\/");
		
		if (delta.charAt(0) != '/') {
			result.addAll(Arrays.asList(currentPath.toString().split("\\/")));
		}
		for (int index = 0; index < newPart.length; index++) {
			if (!newPart[index].isEmpty() && !".".equals(newPart[index])) {
				if ("..".equals(newPart[index])) {
					if (result.size() == 0) {
						throw new IllegalArgumentException("Attempt to jump upper than root: current path is ["+getPath()+"], delta is ["+delta+"]");
					}
					result.remove(result.size()-1);
				}
				else {
					result.add(newPart[index]);
				}
			}
		}
		
		for (String item : result) {
			if (item.length() > 0) {
				sb.append('/').append(item);
			}
		}
		return URI.create(sb.toString());
	}

	private FileSystemInterface open(final URI item) {
		currentPath = item.normalize();
		appendMode = false;
		redirection = needRedirect(currentPath);
		return this;
	}

	private Redirection needRedirect(final URI address) {
		if (mounts.size() == 0) {
			return null;
		}
		else if (address.compareTo(ROOT_URI) == 0 || address.toString().isEmpty()) {
			return null;
		}
		else if (mounts.containsKey(address)) {
			return new Redirection(address,mounts.get(address));
		}
		else if (address.toString().equals("/") || address.toString().equals("/../")) {
			return null;
		}
		else {
			return needRedirect(address.resolve("../").normalize());
		}
	}

	private FileSystemInterface push(URI item) {
		stack.add(0,currentPath);
		return open(item);
	}
	
	private FileSystemInterface list(final Pattern mask, final FileSystemListCallbackInterface callback) throws IOException {
		if (exists() && isDirectory()) {
			final AbstractFileSystem	clone = (AbstractFileSystem) clone();
			
			for (URI item : getDataWrapper(currentPath).list(mask)) {
				callback.process(clone.open(item));
			}
		}
		return this;
	}

	private FileSystemInterface internalCopy(final FileSystemInterface another) throws IOException {
		if (exists()) {
			another.push(getName());
			if (isFile()) {
				try(final OutputStream	os = another.create().write()) {
					copy(os);
				}
			}
			else {
				another.mkDir();
				for (URI item : getDataWrapper(currentPath).list(ALL_MASK_COMPILED)) {
					((AbstractFileSystem)push(build(item.toString()))).internalCopy(another).pop();
				}
			}
			another.pop();
		}
		return this;
	}

	private static class Redirection {
		private final URI			rootURI;
		private final FileSystemInterface	filesystem;
		
		public Redirection(URI rootURI, FileSystemInterface filesystem) {
			this.rootURI = rootURI;
			this.filesystem = filesystem;
		}

		@Override
		public String toString() {
			return "Redirection [rootURI=" + rootURI + ", filesystem=" + filesystem + "]";
		}
	}
}
