package chav1961.purelib.fsys.bridge;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;

class PureLibFileSystem extends FileSystem {
	private static final Set<String>		ATTRIBUTES = new HashSet<>();
	
	private final PureLibFileSystemProvider	provider;
	private final AtomicBoolean				isClosed = new AtomicBoolean(false);
	
	static {
		
	}
	
	
	PureLibFileSystem(final PureLibFileSystemProvider provider) {
		this.provider = provider;
	}

	@Override
	public FileSystemProvider provider() {
		return provider;
	}

	@Override
	public void close() throws IOException {
		if (!isClosed.getAndSet(true)) {
			// TODO Auto-generated method stub
			
		}
	}

	@Override
	public boolean isOpen() {
		return !isClosed.get();
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSeparator() {
		return "/";
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		final List<Path>	result = new ArrayList<>();
		
		try{for (FileSystemInterfaceDescriptor item : FileSystemFactory.getAvailableFileSystems()) {
				final URI	template = URI.create(item.getUriTemplate().getSchemeSpecificPart());
				
				result.add(getPath(template.getScheme(),template.getSchemeSpecificPart()));
			}
		} catch (IOException e) {
		}
		return result;
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
	public Path getPath(final String first, final String... more) {
		if (first == null || first.isEmpty()) {
			throw new IllegalArgumentException("First part of path can't be null or empty string");
		}
		else if (Utils.checkArrayContent4Nulls(more) >= 0) {
			throw new IllegalArgumentException("More list contains nulls inside!");
		}
		else {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public PathMatcher getPathMatcher(final String syntaxAndPattern) {
		if (syntaxAndPattern == null || syntaxAndPattern.isEmpty()) {
			throw new IllegalArgumentException("Syntax and pattern string can't be null or empty!");
		}
		else {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		throw new UnsupportedOperationException("This file system doesn't support principal service");
	}

	@Override
	public WatchService newWatchService() throws IOException {
		throw new UnsupportedOperationException("This file system doesn't support watcher service");
	}
}
