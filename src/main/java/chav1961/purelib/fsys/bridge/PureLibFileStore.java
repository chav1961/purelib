package chav1961.purelib.fsys.bridge;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

import chav1961.purelib.fsys.bridge.PureLibFileSystemProvider.OrdinalFileSystemFileAttributes;
import chav1961.purelib.fsys.bridge.PureLibFileSystemProvider.PureLibFileSystemFileAttributes;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;

class PureLibFileStore extends FileStore {
	private final FileSystemInterfaceDescriptor	desc;
	
	PureLibFileStore(final FileSystemInterfaceDescriptor desc) {
		this.desc = desc;
	}

	@Override
	public String name() {
		return desc.getClassName();
	}

	@Override
	public String type() {
		return desc.getDescriptionId();
	}

	@Override
	public boolean isReadOnly() {
		return desc.isReadOnly();
	}

	@Override
	public long getTotalSpace() throws IOException {
		return 0;
	}

	@Override
	public long getUsableSpace() throws IOException {
		return 0;
	}

	@Override
	public long getUnallocatedSpace() throws IOException {
		return 0;
	}

	@Override
	public boolean supportsFileAttributeView(final Class<? extends FileAttributeView> type) {
		if (type == null) {
			throw new NullPointerException("Attribute type can't be null");
		}
		else {
			return PureLibFileSystemFileAttributes.class.isAssignableFrom(type) || OrdinalFileSystemFileAttributes.class.isAssignableFrom(type); 
		}
	}

	@Override
	public boolean supportsFileAttributeView(final String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Attribute name can't be null or empty");
		}
		else {
			return PureLibFileSystemProvider.ATTRIBUTE_BASIC.equals(name) || PureLibFileSystemProvider.ATTRIBUTE_PURELIB.equals(name);
		}
	}

	@Override
	public <V extends FileStoreAttributeView> V getFileStoreAttributeView(final Class<V> type) {
		return null;
	}

	@Override
	public Object getAttribute(final String attribute) throws IOException {
		if (attribute == null || attribute.isEmpty()) {
			throw new IllegalArgumentException("Attribute name can't be null or empty");
		}
		else {
			final int		setIndex = attribute.indexOf(':'); 
			final String	viewName = setIndex > 0 ? attribute.substring(0,setIndex) : PureLibFileSystemProvider.ATTRIBUTE_BASIC;

			if (PureLibFileSystemProvider.ATTRIBUTE_PURELIB.equals(viewName) || PureLibFileSystemProvider.ATTRIBUTE_BASIC.equals(viewName)) {
				return null;
			}
			else {
				throw new IllegalArgumentException("View name ["+viewName+"] in the attribute ["+attribute+"] is not supported by this file system");
			}
		}
	}
}
