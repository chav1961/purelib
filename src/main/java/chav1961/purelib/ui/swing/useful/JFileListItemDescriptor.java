package chav1961.purelib.ui.swing.useful;

public class JFileListItemDescriptor {
	private final String 	name;
	private final String	path; 
	private final boolean	isDirectory;
	
	JFileListItemDescriptor(final String name, final String path, final boolean isDirectory) {
		this.name = name;
		this.path = path;
		this.isDirectory = isDirectory;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	@Override
	public String toString() {
		return "ItemDescriptor [name=" + name + ", path=" + path + ", isDirectory=" + isDirectory + "]";
	}
}