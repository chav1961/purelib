package chav1961.purelib.model;

import java.io.File;
import java.io.Serializable;

import chav1961.purelib.basic.Utils;

/**
 * <p>This class is used to keep file descriptor</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class FileKeeper implements Serializable {
	private static final long serialVersionUID = -1488055829537063877L;
	
	private String	fileURI = "./";
	
	/**
	 * <p>Constructor of the class instance</p>
	 */
	public FileKeeper() {
	}

	/**
	 * <p>Constructor of the class instance</p>
	 * @param fileURI file URI. Can be neither null nor empty
	 * @throws IllegalArgumentException file URI is null or empty
	 */
	public FileKeeper(final String fileURI) throws IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(fileURI)) {
			throw new IllegalArgumentException("File uri can't be null or empty"); 
		}
		else {
			this.fileURI = fileURI;
		}
	}
	
	/**
	 * <p>Constructor of the class instance</p>
	 * @param file file descriptor to keep. Can't be null
	 * @throws NullPointerException file descriptor is null
	 */
	public FileKeeper(final File file) throws NullPointerException {
		if (file == null) {
			throw new NullPointerException("File instance can't be null or empty"); 
		}
		else {
			this.fileURI = file.getAbsolutePath();
		}
	}

	/**
	 * <p>Get file URI</p>
	 * @return file URI. Can be neither null nor empty
	 */
	public String getFileURI() {
		return fileURI;
	}

	/**
	 * <p>Set file URI.</p>
	 * @param fileURI file URI to set. Can be neither null nor empty
	 * @throws IllegalArgumentException file URI is null or empty
	 */
	public void setFileURI(final String fileURI) throws IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(fileURI)) {
			throw new IllegalArgumentException("File URI can't be null or empty"); 
		}
		else {
			this.fileURI = fileURI;
		}
	}
	
	/**
	 * <p>Set file URI.</p>
	 * @param file file descriptor to set. Can't be null.
	 * @throws NullPointerException file descriptor is null
	 */
	public void setFileURI(final File file) throws NullPointerException {
		if (file == null) {
			throw new NullPointerException("File instance can't be null or empty"); 
		}
		else {
			this.fileURI = file.getAbsolutePath();
		}
	}
	
	/**
	 * <p>Convert file URI to file.</p>
	 * @return File descriptor. Can't be null
	 */
	public File toFile() {
		return new File(fileURI);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileURI == null) ? 0 : fileURI.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FileKeeper other = (FileKeeper) obj;
		if (fileURI == null) {
			if (other.fileURI != null) return false;
		} else if (!new File(fileURI).equals(new File(other.fileURI))) return false;
		return true;
	}

	@Override
	public String toString() {
		return fileURI;
	}
}
