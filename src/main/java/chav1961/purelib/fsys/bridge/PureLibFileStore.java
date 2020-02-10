package chav1961.purelib.fsys.bridge;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

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
		// TODO Auto-generated method stub
		return false;
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
	public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsFileAttributeView(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String attribute) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
