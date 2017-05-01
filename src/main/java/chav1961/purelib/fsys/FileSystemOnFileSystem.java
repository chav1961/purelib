package chav1961.purelib.fsys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class implements the file system interface on the standard file system mechanism in the Java 1.7 and later.</p>
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * @see java.nio.file.FileSystem FileSystem 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface FileSystemInterface
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class FileSystemOnFileSystem extends AbstractFileSystem {
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
		if (rootPath == null) {
			throw new IllegalArgumentException("Root path can't be null");
		}
		else {
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
	            if (ref.getScheme().equals(provider.getScheme())) {
	            	found = provider;
	            }
	        }
	        if (found == null) {
	        	throw new IllegalArgumentException("File system for scheme ["+ref.getScheme()+"] is not installed"); 
	        }
	        else {
				this.fs = found.newFileSystem(Paths.get(ref.getSchemeSpecificPart()),env);
				this.needClose = true;
	        }
		}
	}
	
	private FileSystemOnFileSystem(final FileSystemOnFileSystem another) {
		super(another);
		this.fs = another.fs;
		this.needClose = false;
	}

	@Override
	public boolean canServe(final String uriSchema) {
		return "fsys".equals(uriSchema);
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
			
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(fs.getPath(wrapper.toString()))) {
                for (Path path : ds) {
					if (pattern.matcher(path.getFileName().toString()).matches()) {
						final String uri = path.getFileName().toString();
						
						result.add(URI.create(uri.endsWith("/") ? uri.substring(0,uri.length()-1) : uri));
					}
                }
            }
            
			final URI[]			returned = result.toArray(new URI[result.size()]);
			result.clear();
			return returned;
		}

		@Override
		public void mkDir() throws IOException {
            Files.createDirectory(fs.getPath(wrapper.toString()));
		}

		@Override
		public void create() throws IOException {
			Files.newOutputStream(fs.getPath(wrapper.toString()),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING).close();
		}

		@Override
		public void setName(final String name) throws IOException {
			final Path	path = fs.getPath(wrapper.toString()), newPath = path.resolveSibling(name);
			
			Files.move(path,newPath,StandardCopyOption.REPLACE_EXISTING);
		}

		@Override
		public void delete() throws IOException {
			Files.delete(fs.getPath(wrapper.toString()));
		}

		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			return Files.newOutputStream(fs.getPath(wrapper.toString()),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return Files.newInputStream(fs.getPath(wrapper.toString()),StandardOpenOption.READ);
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			final Path	path = fs.getPath(wrapper.toString());
			
			if (Files.exists(path)) {
				return Utils.mkMap(ATTR_SIZE, Files.size(path)
						, ATTR_NAME, path.getFileName() == null ? "/" : path.getFileName().toString()
						, ATTR_LASTMODIFIED, path.getFileName() == null ? 1 : Files.getLastModifiedTime(path).toMillis()
						, ATTR_DIR, Files.isDirectory(path)
						, ATTR_EXIST, Files.exists(path)
						, ATTR_CANREAD, Files.isReadable(path)
						, ATTR_CANWRITE, Files.isWritable(path));
			}
			else {
				return Utils.mkMap(ATTR_SIZE, 0
						, ATTR_NAME, path.getFileName() == null ? "/" : path.getFileName().toString()
						, ATTR_LASTMODIFIED, 0
						, ATTR_DIR, false
						, ATTR_EXIST, false
						, ATTR_CANREAD, false
						, ATTR_CANWRITE, false);
			}
			
		}

		@Override public void linkAttributes(Map<String, Object> attributes) throws IOException {}
	}

}
