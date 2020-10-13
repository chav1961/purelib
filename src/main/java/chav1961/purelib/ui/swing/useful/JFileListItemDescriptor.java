package chav1961.purelib.ui.swing.useful;

class JFileListItemDescriptor {
	final String 	name;
	final String	path; 
	final boolean	isDirectory;
	
	JFileListItemDescriptor(final String name, final String path, final boolean isDirectory) {
		this.name = name;
		this.path = path;
		this.isDirectory = isDirectory;
	}

	@Override
	public String toString() {
		return "ItemDescriptor [name=" + name + ", path=" + path + ", isDirectory=" + isDirectory + "]";
	}
}