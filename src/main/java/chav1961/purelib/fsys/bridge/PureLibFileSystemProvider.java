package chav1961.purelib.fsys.bridge;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;

public class PureLibFileSystemProvider extends FileSystemProvider {
	public static final Filter<? super Path> 	ALL_CONTENT = (p)->true;
	public static final String					ATTRIBUTE_PURELIB = "purelib";
	public static final String					ATTRIBUTE_BASIC = "basic";

	public static final String					ATTR_LASTMODIFIED_TIME = "lastModifiedTime";
	public static final String					ATTR_LASTACCESS_TIME = "lastAccessTime";
	public static final String					ATTR_CREATION_TIME = "creationTime";
	public static final String					ATTR_SIZE = "size";
	public static final String					ATTR_ISREGULARFILE = "isRegularFile";
	public static final String					ATTR_ISDIRECTORY = "isDirectory";
	public static final String					ATTR_ISSYMBOLICLINK = "isSymbolicLink";
	public static final String					ATTR_ISOTHER = "isOther";
	public static final String					ATTR_FILEKEY = "fileKey";

	static final String							PATH_SPLITTER = "/";
	
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
		else if (uri.isAbsolute()) {
			try(final	FileSystemInterface	fsi = FileSystemFactory.createFileSystem(uri)) {
				final String			subscheme = URI.create(uri.getSchemeSpecificPart()).getScheme();
				final URI				root = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+':'+subscheme+":/");
				final PureLibFileSystem	plfs = new PureLibFileSystem(this); 

				return new PureLibPath(plfs,subscheme,uri.getPath());
			} catch (IOException e) {
				throw new FileSystemNotFoundException("URI path ["+uri+"] doesn't relay to any known file systems");
			}
		}
		else {
			throw new IllegalArgumentException("URI path ["+uri+"] isn't absolute, but file system doesn't support default directory option");
		}
	}

	@Override
	public SeekableByteChannel newByteChannel(final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>... attrs) throws IOException {
		if (path == null) {
			throw new NullPointerException("Path to get byte channel can't be null"); 
		}
		else if (!(path instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to get byte channel ["+path+"] is not a Pure library path"); 
		}
		else if (!path.isAbsolute()) {
			throw new IOException("Path to get byte channel ["+path+"] is not absolute, but this file system doesn't support defult directory option"); 
		}
		else {
			throw new UnsupportedOperationException("This file system doesn't support channels to acces content");
		}
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(final Path dir, final Filter<? super Path> filter) throws IOException {
		if (dir == null) {
			throw new NullPointerException("Path to dreate directory can't be null"); 
		}
		else if (!(dir instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to create directory ["+dir+"] is not a Pure library path"); 
		}
		else if (!dir.isAbsolute()) {
			throw new IOException("Path to create directory ["+dir+"] is not absolute, but this file system doesn't support defult directory option"); 
		}
		else if (filter == null) {
			throw new NullPointerException("Filter for directory stream can't be null. Use "+this.getClass().getSimpleName()+".ALL_CONTENT field to extract ALL content of the directory"); 
		}
		else {
			final URI		uri = dir.toUri();
			final URI		root = dir.getRoot().toUri();
			final String	subscheme = URI.create(root.getSchemeSpecificPart()).getScheme();
			
			try(final FileSystemInterface	fsi = FileSystemFactory.createFileSystem(root).open(uri.getPath())) {
				if (fsi.exists() && fsi.isDirectory()) {
					final PureLibFileSystem	plfs = new PureLibFileSystem(this); 
					final List<Path>		content = new ArrayList<>();
					
					for (String item : fsi.list()) {
						final Path	p = new PureLibPath(plfs,subscheme,fsi.getPath()+PATH_SPLITTER+item);
						if (filter.accept(p)) {
							content.add(p);
						}
					}
					return new DirectoryStream<Path>() {
						@Override public void close() throws IOException {content.clear();}
						@Override public Iterator<Path> iterator() {return content.iterator();}
					};
				}
				else {
					throw new NotDirectoryException("Path ["+dir+"] to build directory stream for is missing or is not a directory");
				}
			}
		}
	}

	@Override
	public void createDirectory(final Path dir, final FileAttribute<?>... attrs) throws IOException {
		if (dir == null) {
			throw new NullPointerException("Path to create directory can't be null"); 
		}
		else if (!(dir instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to create directory ["+dir+"] is not a Pure library path"); 
		}
		else if (!dir.isAbsolute()) {
			throw new IOException("Path to create directory ["+dir+"] is not absolute, but this file system doesn't support defult directory option"); 
		}
		else {
			final URI	uri = dir.toUri();
			final URI	root = dir.getRoot().toUri();
			
			try(final FileSystemInterface	fsi = FileSystemFactory.createFileSystem(root).open(uri.getPath())) {
				fsi.mkDir();
			}
		}
	}

	@Override
	public void delete(final Path path) throws IOException {
		if (path == null) {
			throw new NullPointerException("Path to delete can't be null"); 
		}
		else if (!(path instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to delete ["+path+"] is not a Pure library path"); 
		}
		else {
			final URI	uri = path.toUri();
			final URI	root = path.getRoot().toUri();
			
			try(final FileSystemInterface	fsi = FileSystemFactory.createFileSystem(root).open(uri.getPath())) {
				fsi.deleteAll();
			}
		}
	}

	@Override
	public void copy(final Path source, final Path target, final CopyOption... options) throws IOException {
		if (source == null) {
			throw new NullPointerException("Source path can't be null"); 
		}
		else if (target == null) {
			throw new NullPointerException("Target path can't be null"); 
		}
		else if (!(source instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to copy from ["+source+"] is not a Pure library path"); 
		}
		else if (!(target instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to copy to ["+target+"] is not a Pure library path"); 
		}
		else {
			final URI	sourceUri = source.toUri(), targetUri = source.toUri(); 

			try(final FileSystemInterface	fsiSource = FileSystemFactory.createFileSystem(sourceUri);
				final FileSystemInterface	fsiTarget = FileSystemFactory.createFileSystem(targetUri)) {
				
				fsiSource.copy(fsiTarget);
			}
		}
	}

	@Override
	public void move(final Path source, final Path target, final CopyOption... options) throws IOException {
		copy(source,target,options);
		delete(source);
	}

	@Override
	public boolean isSameFile(final Path path1, final Path path2) throws IOException {
		if (path1 == null) {
			throw new NullPointerException("Path 1 to compare can't be null"); 
		}
		else if (path2 == null) {
			throw new NullPointerException("Path 2 to comapre can't be null"); 
		}
		else if (!(path1 instanceof PureLibPath) || !(path2 instanceof PureLibPath)) {
			return false; 
		}
		else {
			return path1.toUri().normalize().equals(path2.toUri().normalize());
		}
	}

	@Override
	public boolean isHidden(final Path path) throws IOException {
		if (path == null) {
			throw new NullPointerException("Path to test can't be null"); 
		}
		else if (!(path instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to test ["+path+"] is not a Pure library path"); 
		}
		else {
			return false;
		}
	}

	@Override
	public FileStore getFileStore(final Path path) throws IOException {
		if (path == null) {
			throw new NullPointerException("Path to test can't be null"); 
		}
		else if (!(path instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to test ["+path+"] is not a Pure library path"); 
		}
		else {
			final URI	root = path.getRoot().toUri();

			for (FileSystemInterfaceDescriptor item : FileSystemFactory.getAvailableFileSystems()){
				if (item.getUriTemplate().equals(root)) {
					return new PureLibFileStore(item);
				}
			}
			throw new IOException("File store for URI ["+root+"] is missing");
		}
	}

	@Override
	public void checkAccess(final Path path, final AccessMode... modes) throws IOException {
		int	nullIndex;
		
		if (path == null) {
			throw new NullPointerException("Path to check access can't be null"); 
		}
		else if (!(path instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to check access ["+path+"] is not a Pure library path"); 
		}
		else if (!path.isAbsolute()) {
			throw new IOException("Path to check acces ["+path+"] is not absolute, but this file system doesn't support defult directory option"); 
		}
		else if ((nullIndex = Utils.checkArrayContent4Nulls(modes)) >= 0) {
			throw new IllegalArgumentException("Access mode list contains nulls inside at index ["+nullIndex+"]!"); 
		}
		else {
			final URI	uri = path.toUri();
			final URI	root = path.getRoot().toUri();
			
			try(final FileSystemInterface	fsi = FileSystemFactory.createFileSystem(root).open(uri.getPath())) {
				boolean		result = fsi.exists();
						
				if (!result) {
					throw new NoSuchFileException("Path ["+path+"] is missing"); 
				}
				for (AccessMode item : modes) {
					switch (item) {
						case EXECUTE	: result = false; break;
						case READ		: result &= fsi.canRead(); break;
						case WRITE		: result &= fsi.canWrite(); break;
						default	: throw new UnsupportedOperationException(); 
					}
				}
				if (!result) {
					throw new AccessDeniedException("Path ["+path+"] diesn't support access requested "+Arrays.toString(modes)); 
				}
			}
		}
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(final Path path, final Class<V> type, final LinkOption... options) {
		try{final PureLibFileSystemFileAttributes	fa = readAttributes(path,PureLibFileSystemFileAttributes.class,options); 
			
			return (V) fa.getAttributeView();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(final Path path, final Class<A> type, final LinkOption... options) throws IOException {
		if (path == null) {
			throw new NullPointerException("Path to test can't be null"); 
		}
		else if (!(path instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to test ["+path+"] is not a Pure library path"); 
		}
		else if (type == null) {
			throw new NullPointerException("Type to extract can't be null or empty"); 
		}
		else {
			final URI	uri = path.toUri();
			final URI	root = path.getRoot().toUri();
			
			try(final FileSystemInterface		fsi = FileSystemFactory.createFileSystem(root).open(uri.getPath())) {
				if (fsi.exists()) {
					if (type == PureLibFileSystemFileAttributes.class) {
						return (A) new PureLibFileSystemFileAttributes(fsi.getAttributes());
					}
					else {
						return (A) new OrdinalFileSystemFileAttributes(fsi.getAttributes());
					}
				}
				else {
					throw new IOException("Path ["+path+"] points to missing or unaccesible item in the file system");
				}
			}
		}
	}

	@Override
	public Map<String, Object> readAttributes(final Path path, final String attributes, final LinkOption... options) throws IOException {
		if (path == null) {
			throw new NullPointerException("Path to test can't be null"); 
		}
		else if (!(path instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to test ["+path+"] is not a Pure library path"); 
		}
		else if (attributes == null || attributes.isEmpty()) {
			throw new IllegalArgumentException("Attributes to extract can't be null or empty"); 
		}
		else {
			final int		setIndex = attributes.indexOf(':'); 
			final String	viewName = setIndex > 0 ? attributes.substring(0,setIndex) : ATTRIBUTE_BASIC;
			final String	attr = setIndex > 0 ? attributes.substring(setIndex+1) : attributes;

			if (ATTRIBUTE_PURELIB.equals(viewName) || ATTRIBUTE_BASIC.equals(viewName)) {
				final URI	uri = path.toUri();
				final URI	root = path.getRoot().toUri();
				
				try(final FileSystemInterface		fsi = FileSystemFactory.createFileSystem(root).open(uri.getPath())) {
					if (fsi.exists()) {
						final Map<String,Object>	attrList = fsi.getAttributes();
						final Map<String,Object>	result = new HashMap<>();
						
						for (String item : CharUtils.split(attr,',')) {
							switch (item) {
								case DataWrapperInterface.ATTR_SIZE : /*case ATTR_SIZE :*/
									result.put(item,attrList.get(DataWrapperInterface.ATTR_SIZE));
									break;
								case DataWrapperInterface.ATTR_NAME			:
									result.put(item,attrList.get(DataWrapperInterface.ATTR_NAME));
									break;
								case DataWrapperInterface.ATTR_LASTMODIFIED	: case ATTR_LASTMODIFIED_TIME : case ATTR_LASTACCESS_TIME : case ATTR_CREATION_TIME :
									result.put(item,attrList.get(DataWrapperInterface.ATTR_LASTMODIFIED));
									break;
								case DataWrapperInterface.ATTR_DIR : case ATTR_ISREGULARFILE : case ATTR_ISDIRECTORY :
									result.put(item,attrList.get(DataWrapperInterface.ATTR_DIR));
									break;
								case DataWrapperInterface.ATTR_EXIST		:
									result.put(item,attrList.get(DataWrapperInterface.ATTR_EXIST));
									break;
								case DataWrapperInterface.ATTR_CANREAD		:
									result.put(item,attrList.get(DataWrapperInterface.ATTR_CANREAD));
									break;
								case DataWrapperInterface.ATTR_CANWRITE		:
									result.put(item,attrList.get(DataWrapperInterface.ATTR_CANWRITE));
									break;
								case ATTR_ISSYMBOLICLINK : case ATTR_ISOTHER : 
									result.put(item,false);
									break;
								case ATTR_FILEKEY :
									break;
								default : throw new IllegalArgumentException("Unsupported attribute name ["+item+"] in the attribute list ["+attributes+"]");
							}
						}
						return result;
					}
					else {
						throw new IOException("Path ["+path+"] points to missing or unaccesible item in the file system");
					}
				}
			}
			else {
				throw new IllegalArgumentException("View name ["+viewName+"] in the attributes ["+attributes+"] is not supported by this file system");
			}
		}
	}

	@Override
	public void setAttribute(final Path path, final String attribute, final Object value, final LinkOption... options) throws IOException {
		if (path == null) {
			throw new NullPointerException("Path to test can't be null"); 
		}
		else if (!(path instanceof PureLibPath)) {
			throw new IllegalArgumentException("Path to test ["+path+"] is not a Pure library path"); 
		}
		else if (attribute == null || attribute.isEmpty()) {
			throw new IllegalArgumentException("Attribute name to set can't be null or empty"); 
		}
		else if (value == null) {
			throw new IllegalArgumentException("Attribute value to set can't be null"); 
		}
		else {
			final URI	uri = path.toUri();
			final URI	root = path.getRoot().toUri();
			
			try(final FileSystemInterface		fsi = FileSystemFactory.createFileSystem(root).open(uri.getPath())) {
				if (fsi.exists()) {
					final Map<String,Object>	attr = fsi.getAttributes();
					
					switch (attribute) {
						case DataWrapperInterface.ATTR_LASTMODIFIED	:
							if (value instanceof Number) {
								attr.put(DataWrapperInterface.ATTR_LASTMODIFIED,((Number)value).longValue());
							}
							else {
								throw new IllegalArgumentException("Illegal value for attribute name ["+attribute+"]: must be number");
							}
							break;
						case DataWrapperInterface.ATTR_CANREAD		:
							if (value instanceof Boolean) {
								attr.put(DataWrapperInterface.ATTR_CANREAD,((Boolean) value).booleanValue());
							}
							else {
								throw new IllegalArgumentException("Illegal value for attribute name ["+attribute+"]: must be boolean");
							}
							break;
						case DataWrapperInterface.ATTR_CANWRITE		:
							if (value instanceof Boolean) {
								attr.put(DataWrapperInterface.ATTR_CANWRITE,((Boolean) value).booleanValue());
							}
							else {
								throw new IllegalArgumentException("Illegal value for attribute name ["+attribute+"]: must be boolean");
							}
							break;
						default : throw new IllegalArgumentException("Unsupported attribute name ["+attribute+"]");
					}
					fsi.setAttributes(attr);
				}
				else {
					throw new IOException("Path ["+path+"] points to missing or unaccesible item in the file system");
				}
			}
		}
	}
	
	static Map<String,?> joinEnvArguments(final Map<String,?> env, final String query) {
		return null;
	}

	FileSystem createFileSystem(final URI uri, Map<String, ?> env) throws IOException {
		return null;
	}
	
	public static class PureLibFileSystemFileAttributes implements BasicFileAttributes {
		private final Map<String,Object>	attributes;
		
		PureLibFileSystemFileAttributes(final Map<String,Object> attributes) {
			this.attributes = attributes;
		}

		@Override
		public FileTime lastModifiedTime() {
			return FileTime.fromMillis(((Number)attributes.get(DataWrapperInterface.ATTR_LASTMODIFIED)).longValue());
		}

		@Override
		public FileTime lastAccessTime() {
			return lastModifiedTime();
		}

		@Override
		public FileTime creationTime() {
			return lastModifiedTime();
		}

		@Override
		public boolean isRegularFile() {
			return ((Boolean)attributes.get(DataWrapperInterface.ATTR_EXIST)) && !((Boolean)attributes.get(DataWrapperInterface.ATTR_DIR));
		}

		@Override
		public boolean isDirectory() {
			return ((Boolean)attributes.get(DataWrapperInterface.ATTR_EXIST)) && ((Boolean)attributes.get(DataWrapperInterface.ATTR_DIR));
		}

		@Override
		public boolean isSymbolicLink() {
			return false;
		}

		@Override
		public boolean isOther() {
			return false;
		}

		@Override
		public long size() {
			return (Long)attributes.get(DataWrapperInterface.ATTR_SIZE);
		}

		@Override
		public Object fileKey() {
			return null;
		}
		
		BasicFileAttributeView getAttributeView() {
			return new BasicFileAttributeView() {
				@Override
				public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
					throw new IOException("Implementation restriction: don't call this method directly, use setAttribute(...) method in the file system provider"); 
				}
				
				@Override
				public BasicFileAttributes readAttributes() throws IOException {
					return PureLibFileSystemFileAttributes.this;
				}
				
				@Override
				public String name() {
					return ATTRIBUTE_PURELIB;
				}
			};
		}
	}

	public static class OrdinalFileSystemFileAttributes implements BasicFileAttributes {
		private final Map<String,Object>	attributes;
		
		OrdinalFileSystemFileAttributes(final Map<String,Object> attributes) {
			this.attributes = attributes;
		}

		@Override
		public FileTime lastModifiedTime() {
			return FileTime.fromMillis(((Number)attributes.get(DataWrapperInterface.ATTR_LASTMODIFIED)).longValue());
		}

		@Override
		public FileTime lastAccessTime() {
			return lastModifiedTime();
		}

		@Override
		public FileTime creationTime() {
			return lastModifiedTime();
		}

		@Override
		public boolean isRegularFile() {
			return ((Boolean)attributes.get(DataWrapperInterface.ATTR_EXIST)) && !((Boolean)attributes.get(DataWrapperInterface.ATTR_DIR));
		}

		@Override
		public boolean isDirectory() {
			return ((Boolean)attributes.get(DataWrapperInterface.ATTR_EXIST)) && ((Boolean)attributes.get(DataWrapperInterface.ATTR_DIR));
		}

		@Override
		public boolean isSymbolicLink() {
			return false;
		}

		@Override
		public boolean isOther() {
			return false;
		}

		@Override
		public long size() {
			return (Long)attributes.get(DataWrapperInterface.ATTR_SIZE);
		}

		@Override
		public Object fileKey() {
			return null;
		}
		
		BasicFileAttributeView getAttributeView() {
			return new BasicFileAttributeView() {
				@Override
				public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
					throw new IOException("Implementation restriction: don't call this method directly, use setAttribute(...) method in the file system provider"); 
				}
				
				@Override
				public BasicFileAttributes readAttributes() throws IOException {
					return OrdinalFileSystemFileAttributes.this;
				}
				
				@Override
				public String name() {
					return ATTRIBUTE_BASIC;
				}
			};
		}
	}
}
