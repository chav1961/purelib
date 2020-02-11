package chav1961.purelib.fsys.bridge;

import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

class PureLibPath implements Path {
	private final PureLibFileSystem	fs;
	private final String			subscheme;
	private final String			path;
	private final URI				ref;
	
	PureLibPath(final PureLibFileSystem fs, final String subscheme, final String path) {
		this.fs = fs;
		this.subscheme = subscheme;
		this.path = path;
		this.ref = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+':'+subscheme+':'+path);
	}

	PureLibPath(final PureLibFileSystem fs, final String path) {
		this.fs = fs;
		this.subscheme = null;
		this.path = path;
		this.ref = URI.create(path);
	}
	
	@Override
	public FileSystem getFileSystem() {
		return fs;
	}

	@Override
	public boolean isAbsolute() {
		return ref.isAbsolute();
	}

	@Override
	public Path getRoot() {
		return new PureLibPath(fs,subscheme,PureLibFileSystemProvider.PATH_SPLITTER);
	}

	@Override
	public Path getFileName() {
		final int		pathIndex = path.lastIndexOf(PureLibFileSystemProvider.PATH_SPLITTER);
		final String	fileName = pathIndex >= 0 ? path.substring(pathIndex+1) : "";
		
		return fileName.isEmpty() ? null : new PureLibPath(fs,fileName);
	}

	@Override
	public Path getParent() {
		final URI	resolved = toUri().resolve(URI.create(".."+PureLibFileSystemProvider.PATH_SPLITTER));

		if (resolved.isAbsolute()) {
			final URI	subUri = URI.create(resolved.getSchemeSpecificPart());
			
			return new PureLibPath(fs,subUri.getScheme(),resolved.getPath());
		}
		else {
			return new PureLibPath(fs,resolved.getPath());
		}
	}

	@Override
	public int getNameCount() {
		final String[]	components = CharUtils.split(path,PureLibFileSystemProvider.PATH_SPLITTER);
		
		return isAbsolute() ? components.length + 1 : components.length; 
	}

	@Override
	public Path getName(final int index) {
		if (index < 0 || index >= getNameCount()) {
			throw new IllegalArgumentException("Name index ["+index+"] out of range 0.."+(getNameCount()-1));
		}
		else {
			
		}
		final String[]	components = CharUtils.split(path,PureLibFileSystemProvider.PATH_SPLITTER);
		
		if (isAbsolute()) {
			if (index == 0) {
				return new PureLibPath(fs,PureLibFileSystemProvider.PATH_SPLITTER+FileSystemInterface.FILESYSTEM_URI_SCHEME);
			}
			else if (index == 1) {
				return new PureLibPath(fs,PureLibFileSystemProvider.PATH_SPLITTER+subscheme);
			}
			else {
				return new PureLibPath(fs,PureLibFileSystemProvider.PATH_SPLITTER+components[index-1]);
			}
		}
		else {
			return new PureLibPath(fs,PureLibFileSystemProvider.PATH_SPLITTER+components[index+1]);
		}
	}

	@Override
	public Path subpath(final int beginIndex, final int endIndex) {
		if (beginIndex < 0 || beginIndex >= getNameCount()) {
			throw new IllegalArgumentException("Begin index ["+beginIndex+"] out of range 0.."+(getNameCount()-1));
		}
		else if (endIndex < 0 || endIndex >= getNameCount()) {
			throw new IllegalArgumentException("End index ["+endIndex+"] out of range 0.."+(getNameCount()-1));
		}
		else if (endIndex < beginIndex) {
			throw new IllegalArgumentException("End index ["+endIndex+"] less than begin index ["+beginIndex+"]");
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			
			for (int index = beginIndex; index <= endIndex; index++) {
				sb.append(PureLibFileSystemProvider.PATH_SPLITTER).append(getName(index));
			}
			
			return isAbsolute() ? new PureLibPath(fs,subscheme,sb.toString()) : new PureLibPath(fs,sb.toString());
		}
	}

	@Override
	public boolean startsWith(final Path other) {
		if (other == null) {
			throw new NullPointerException("Path to test can't be null");
		}
		else if (!(other instanceof PureLibPath)) {
			throw new IllegalArgumentException("Other path type ["+other.getClass()+"] is not a ["+this.getClass()+"]");
		}
		else if (isAbsolute() == other.isAbsolute() && getNameCount() >= other.getNameCount()) {
			for (int index = 0, maxIndex = other.getNameCount(); index <  maxIndex; index++) {
				if (!getName(index).equals(other.getName(index))) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean endsWith(Path other) {
		if (other == null) {
			throw new NullPointerException("Path to test can't be null");
		}
		else if (!(other instanceof PureLibPath)) {
			throw new IllegalArgumentException("Other path type ["+other.getClass()+"] is not a ["+this.getClass()+"]");
		}
		else if (isAbsolute() == other.isAbsolute() && getNameCount() >= other.getNameCount()) {
			for (int index = 0, shift = getNameCount()-other.getNameCount(), maxIndex = other.getNameCount(); index <  maxIndex; index++) {
				if (!getName(index+shift).equals(other.getName(index))) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public Path normalize() {
		final URI	norm = ref.normalize();
		
		return isAbsolute() ? new PureLibPath(fs,subscheme,norm.getPath()) : new PureLibPath(fs,norm.getPath());
	}

	@Override
	public Path resolve(final Path other) {
		if (other == null) {
			throw new NullPointerException("Path to resolve can't be null");
		}
		else if (!(other instanceof PureLibPath)) {
			throw new IllegalArgumentException("Other path type ["+other.getClass()+"] is not a ["+this.getClass()+"]");
		}
		else {
			final URI	resolved = toUri().resolve(other.toUri());

			if (resolved.isAbsolute()) {
				final URI	subUri = URI.create(resolved.getSchemeSpecificPart());
				
				return new PureLibPath(fs,subUri.getScheme(),resolved.getPath());
			}
			else {
				return new PureLibPath(fs,resolved.getPath());
			}
		}
	}

	@Override
	public Path relativize(final Path other) {
		if (other == null) {
			throw new NullPointerException("Path to resolve can't be null");
		}
		else if (!(other instanceof PureLibPath)) {
			throw new IllegalArgumentException("Other path type ["+other.getClass()+"] is not a ["+this.getClass()+"]");
		}
		else {
			final URI	relativize = toUri().relativize(other.toUri());

			if (relativize.isAbsolute()) {
				final URI	subUri = URI.create(relativize.getSchemeSpecificPart());
				
				return new PureLibPath(fs,subUri.getScheme(),relativize.getPath());
			}
			else {
				return new PureLibPath(fs,relativize.getPath());
			}
		}
	}

	@Override
	public URI toUri() {
		return ref;
	}

	@Override
	public Path toAbsolutePath() {
		if (isAbsolute()) {
			return this;
		}
		else {
			throw new IOError(new EnvironmentException("Relative path can't be converted to absolute, because there is no default directory in the given file system")); 
		}
	}

	@Override
	public Path toRealPath(final LinkOption... options) throws IOException {
		if (isAbsolute()) {
			return this;
		}
		else {
			throw new IOError(new EnvironmentException("Relative path can't be converted to real path, because there is no default directory in the given file system")); 
		}
	}

	@Override
	public WatchKey register(final WatchService watcher, final Kind<?>[] events, final Modifier... modifiers) throws IOException {
		throw new UnsupportedOperationException("Watch service is not supported for the given path");
	}

	@Override
	public int compareTo(final Path other) {
		if (other == null) {
			throw new NullPointerException("Path to compare can't be null"); 
		}
		else if (!(other instanceof PureLibPath)) {
			throw new ClassCastException("Other path has type ["+other+"], not ["+this.getClass()+"]"); 
		}
		else {
			int	result;
			
			for (int index = 0, maxIndex = Math.min(getNameCount(),other.getNameCount()); index < maxIndex; index++) {
				if ((result = getName(index).compareTo(other.getName(index))) != 0) {
					return result;
				}
			}
			return getNameCount() - other.getNameCount();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ref == null) ? 0 : ref.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PureLibPath other = (PureLibPath) obj;
		if (ref == null) {
			if (other.ref != null) return false;
		} else if (!ref.equals(other.ref)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "PureLibPath [ref=" + ref + "]";
	}
}
