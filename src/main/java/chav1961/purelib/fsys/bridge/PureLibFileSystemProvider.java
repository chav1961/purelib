package chav1961.purelib.fsys.bridge;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class PureLibFileSystemProvider extends FileSystemProvider {
	public PureLibFileSystemProvider() {
		
	}
	
	@Override
	public String getScheme() {
		return FileSystemInterface.FILESYSTEM_URI_SCHEME;
	}

	@Override
	public FileSystem newFileSystem(final URI uri, Map<String, ?> env) throws IOException {
		if (uri == null) {
			throw new NullPointerException("URI for file system can't be null");
		}
		else if (env == null) {
			throw new NullPointerException("Environment map for file system can't be null");
		}
		else if (!FileSystemInterface.FILESYSTEM_URI_SCHEME.equalsIgnoreCase(uri.getScheme())) {
			throw new IOException("File system for ["+uri+"] not created: URI scheme must be ["+FileSystemInterface.FILESYSTEM_URI_SCHEME+"]");
		}
		else {
			return createFileSystem(uri,joinEnvArguments(env,URIUtils.extractQueryFromURI(uri)));
		}
	}

	@Override
	public FileSystem getFileSystem(final URI uri) {
		if (uri == null) {
			throw new NullPointerException("URI for file system can't be null");
		}
		else if (!FileSystemInterface.FILESYSTEM_URI_SCHEME.equalsIgnoreCase(uri.getScheme())) {
			throw new FileSystemNotFoundException("File system for ["+uri+"] not created: URI scheme must be ["+FileSystemInterface.FILESYSTEM_URI_SCHEME+"]");
		}
		else {
			try{return createFileSystem(uri,joinEnvArguments(new HashMap<>(),URIUtils.extractQueryFromURI(uri)));
			} catch (IOException e) {
				throw new FileSystemNotFoundException("File system for ["+uri+"] not found: "+e.getLocalizedMessage());
			}
		}
	}

	@Override
	public Path getPath(final URI uri) {
		if (uri == null) {
			throw new NullPointerException("URI for file system can't be null");
		}
		else if (!FileSystemInterface.FILESYSTEM_URI_SCHEME.equalsIgnoreCase(uri.getScheme())) {
			throw new FileSystemNotFoundException("File system for ["+uri+"] not created: URI scheme must be ["+FileSystemInterface.FILESYSTEM_URI_SCHEME+"]");
		}
		else {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public SeekableByteChannel newByteChannel(final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>... attrs) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(final Path dir, final Filter<? super Path> filter) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createDirectory(final Path dir, final FileAttribute<?>... attrs) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(final Path path) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copy(final Path source, final Path target, final CopyOption... options) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void move(final Path source, final Path target, final CopyOption... options) throws IOException {
		copy(source,target,options);
		delete(source);
	}

	@Override
	public boolean isSameFile(final Path path, final Path path2) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHidden(final Path path) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FileStore getFileStore(final Path path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkAccess(final Path path, final AccessMode... modes) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(final Path path, final Class<V> type, final LinkOption... options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(final Path path, final Class<A> type, final LinkOption... options) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> readAttributes(final Path path, final String attributes, final LinkOption... options) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(final Path path, final String attribute, final Object value, final LinkOption... options) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	static Map<String,?> joinEnvArguments(final Map<String,?> env, final String query) {
		return null;
	}

	FileSystem createFileSystem(final URI uri, Map<String, ?> env) throws IOException {
		return null;
	}
}
