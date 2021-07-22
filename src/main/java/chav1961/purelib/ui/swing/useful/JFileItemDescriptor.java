package chav1961.purelib.ui.swing.useful;

public class JFileItemDescriptor {
	private final String 	name;
	private final String	path; 
	private final boolean	isDirectory;
	
	JFileItemDescriptor(final String name, final String path, final boolean isDirectory) {
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
		return "JFileItemDescriptor [name=" + name + ", path=" + path + ", isDirectory=" + isDirectory + "]";
	}
}