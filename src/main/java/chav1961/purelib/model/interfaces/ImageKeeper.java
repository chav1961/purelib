package chav1961.purelib.model.interfaces;

import java.awt.Image;
import java.io.Serializable;

public interface ImageKeeper extends Serializable, Cloneable {
	Image getImage();
	void setImage(Image image);
	public boolean isModified();
	void setModified(final boolean modified);
	Object clone() throws CloneNotSupportedException;
}
