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
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class implements the file system interface on the usual file system.</p>
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface IFileSystem
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class FileSystemOnFile extends AbstractFileSystem {
	private	final URI					rootPath;

	public FileSystemOnFile(){
		rootPath = null;
	}
	
	/**
	 * <p>Create the file system for the given directory.  
	 * @param rootPath root directory for the file system. Need be absolute URI with the schema 'file', for example <code>'file://./c:/mydir'</code>
	 * @throws IOException if any exception was thrown
	 */
	public FileSystemOnFile(final URI rootPath) throws IOException {
		if (rootPath == null) {
			throw new IllegalArgumentException("Root path can't be null");
		}
		else if (!rootPath.isAbsolute()) {
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
	public boolean canServe(final String uriSchema) {
		return "file".equals(uriSchema);
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
			return new FileOutputStream(new File(wrapper),append);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new FileInputStream(new File(wrapper));
		}

		@Override
		public URI[] list(final Pattern pattern) throws IOException {
			final List<URI>		result = new ArrayList<>();
			
			new File(wrapper).listFiles(new FileFilter(){
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
			new File(wrapper).mkdirs();
		}

		@Override
		public void create() throws IOException {
			try(final OutputStream os = new FileOutputStream(new File(wrapper))) {
			}
		}

		@Override
		public void delete() throws IOException {
			new File(wrapper).delete();
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			final File	temp = new File(wrapper);
			return Utils.mkMap(ATTR_SIZE, temp.length(), ATTR_NAME, temp.getName(), ATTR_LASTMODIFIED, temp.lastModified(), ATTR_DIR, temp.isDirectory(), ATTR_EXIST, temp.exists(), ATTR_CANREAD, temp.canRead(), ATTR_CANWRITE, temp.canWrite());
		}

		@Override public void linkAttributes(Map<String, Object> attributes) throws IOException {}
		
		@Override
		public void setName(final String name) throws IOException {
			final File	oldFile = new File(wrapper), newFile = new File(oldFile.getParent(),name);
			
			oldFile.renameTo(newFile);
		}		
	}
}
