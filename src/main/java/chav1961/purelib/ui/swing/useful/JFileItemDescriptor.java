package chav1961.purelib.ui.swing.useful;

import java.util.Date;

/**
 * 
 *
 */
public class JFileItemDescriptor implements Comparable<JFileItemDescriptor> {
	private final String 	name;
	private final String	path; 
	private final boolean	isDirectory;
	private final long		size;
	private final Date		lastModified;
	
	JFileItemDescriptor(final String name, final String path, final boolean isDirectory, final long size, final Date lastModified) {
		this.name = name;
		this.path = path;
		this.isDirectory = isDirectory;
		this.size = size;
		this.lastModified = lastModified;
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

	public long getSize() {
		return size;
	}

	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "JFileItemDescriptor [name=" + name + ", path=" + path + ", isDirectory=" + isDirectory + ", size=" + size + ", lastModified=" + lastModified + "]";
	}

	@Override
	public int compareTo(final JFileItemDescriptor o) {
		if (o == null) {
			return 1;
		}
		else if (o.isDirectory() == isDirectory()) {
			return o.getName().compareTo(getName());
		}
		else {
			return isDirectory() ? -1 : 1;
		}
	}
}