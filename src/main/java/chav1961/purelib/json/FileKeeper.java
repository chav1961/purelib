package chav1961.purelib.json;

import java.io.File;
import java.io.Serializable;

public class FileKeeper implements Serializable {
	private static final long serialVersionUID = -1488055829537063877L;
	
	private String	fileURI = "./";
	
	public FileKeeper() {
	}

	public FileKeeper(final String fileURI) {
		if (fileURI == null || fileURI.isEmpty()) {
			throw new IllegalArgumentException("File uri can't be null or empty"); 
		}
		else {
			this.fileURI = fileURI;
		}
	}
	
	public FileKeeper(final File file) {
		if (file == null) {
			throw new NullPointerException("File instance can't be null or empty"); 
		}
		else {
			this.fileURI = file.getAbsolutePath();
		}
	}

	public String getFileURI() {
		return fileURI;
	}

	public void setFileURI(final String fileURI) {
		if (fileURI == null || fileURI.isEmpty()) {
			throw new IllegalArgumentException("File uri can't be null or empty"); 
		}
		else {
			this.fileURI = fileURI;
		}
	}
	
	public void setFileURI(final File file) {
		if (file == null) {
			throw new NullPointerException("File instance can't be null or empty"); 
		}
		else {
			this.fileURI = file.getAbsolutePath();
		}
	}
	
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
