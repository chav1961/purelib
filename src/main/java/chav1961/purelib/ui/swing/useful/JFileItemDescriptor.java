package chav1961.purelib.ui.swing.useful;

import java.io.File;
import java.util.Date;

/**
 * <p>This class describes file/directory items in the {@linkplain JFileTree} and {@linkplain JFileList} classes. Class implements 
 * {@linkplain Comparable} interface and orders firstly directories and then files in lexical order. Ordering is case-sensitive</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @see JFileList
 * @see JFileTree
 * @since 0.0.7
 */
public class JFileItemDescriptor implements Comparable<JFileItemDescriptor>, Cloneable {
	private final String 	name;
	private final String	path; 
	private final boolean	isDirectory;
	private final long		size;
	private final Date		lastModified;
	
	public JFileItemDescriptor(final String name, final String path, final boolean isDirectory, final long size, final Date lastModified) {
		this.name = name;
		this.path = path;
		this.isDirectory = isDirectory;
		this.size = size;
		this.lastModified = lastModified;
	}

	/**
	 * <p>Get file/directory name</p>
	 * @return file/directory name. Can't be null or empty
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Get file/directory path</p>
	 * @return file/directory path. Can't be null or empty
	 */
	public String getPath() {
		return path;
	}

	/**
	 * <p>Is this item a directory</p>
	 * @return true if yes
	 */
	public boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * <p>Get file size.</p>
	 * @return file size or 0 on the directory
	 */
	public long getSize() {
		return size;
	}

	/**
	 * <p>Get last modified time stamp</p>
	 * @return last modified time stamp (milliseconds since 01/01/1970)
	 */
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
			return getName().compareTo(o.getName());
		}
		else {
			return isDirectory() ? -1 : 1;
		}
	}

	@Override
	public JFileItemDescriptor clone() throws CloneNotSupportedException {
		return (JFileItemDescriptor)super.clone();
	}
	
	/**
	 * <p>Build file item descriptor from {@linkplain File} instance</p>
	 * @param file file instance to build descriptor from. Can't be null
	 * @return file item descriptor built. Can't be null
	 * @throws NullPointerException when file descriptor is null
	 */
	public static JFileItemDescriptor of(final File file) throws NullPointerException {
		if (file == null) {
			throw new NullPointerException("File descriptor can't be null"); 
		}
		else {
			return new JFileItemDescriptor(file.getName(), file.getAbsoluteFile().toURI().toString(), file.isDirectory(), file.length(), new Date(file.lastModified()));
		}
	}
}