package chav1961.purelib.fsys;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class implements the file system interface on the usual file system. The URI to use this class is 
 * <code>URI.create("file:file:path_to_root_directory");</code> (for example <code>URI.create("file:file:./muRootDirectory");</code>)</p>
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface FileSystemInterface
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1 last updated 0.0.2
 */

public class FileSystemOnFile extends AbstractFileSystem {
	private static final URI			SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/");
	
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
		return Utils.canServeURI(resource,SERVE);
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
	
	private static class FileDataWrapper implements DataWrapperInterface {
		private final URI	wrapper;
		
		public FileDataWrapper(final URI wrapper, final URI rootPath) {
			this.wrapper = URI.create(rootPath.toString()+wrapper.toString()).normalize();
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
