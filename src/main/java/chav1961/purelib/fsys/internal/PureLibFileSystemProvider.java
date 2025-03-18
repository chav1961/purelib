package chav1961.purelib.fsys.internal;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class PureLibFileSystemProvider extends FileSystemProvider {
	private static final Map<String,?>	 EMPTY_ENV = new HashMap<>();

	@Override
	public String getScheme() {
		return FileSystemInterface.FILESYSTEM_URI_SCHEME;
	}

	@Override
	public FileSystem newFileSystem(final URI uri, Map<String, ?> env) throws IOException {
		if (uri == null) {
			throw new NullPointerException("File system URI to create/get can't be null");
		}
		else {
			return new PureLibFileSystem(this,FileSystemFactory.createFileSystem(uri));  
		}
	}

	@Override
	public FileSystem getFileSystem(final URI uri) {
		try{
			return newFileSystem(uri,EMPTY_ENV);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public Path getPath(final URI uri) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		
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
	
	public FileSystemInterface getWrappedFileSystem() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private static class PureLibFileSystem extends FileSystem {
		private final FileSystemProvider	fsp;
		private final FileSystemInterface	fsi;
		private final AtomicBoolean			closed = new AtomicBoolean(false);
		
		PureLibFileSystem(final FileSystemProvider provider, final FileSystemInterface wrappedFS) {
			this.fsp = provider;
			this.fsi = wrappedFS;
		}

		@Override
		public FileSystemProvider provider() {
			return fsp;
		}

		@Override
		public void close() throws IOException {
			if (!closed.getAndSet(true)) {
				fsi.close();
			}
		}

		@Override
		public boolean isOpen() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isReadOnly() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public String getSeparator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<Path> getRootDirectories() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<FileStore> getFileStores() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> supportedFileAttributeViews() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Path getPath(String first, String... more) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PathMatcher getPathMatcher(String syntaxAndPattern) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UserPrincipalLookupService getUserPrincipalLookupService() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public WatchService newWatchService() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
