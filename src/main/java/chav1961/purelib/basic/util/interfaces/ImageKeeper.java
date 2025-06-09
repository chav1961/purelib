package chav1961.purelib.basic.util.interfaces;

import java.awt.Image;
import java.io.Serializable;

/**
 * <p>This interface describes image keeper</p>
 */
public interface ImageKeeper extends Serializable, Cloneable {
	/**
	 * <p>Get image kept</p>
	 * @return image kept. Can't be null.
	 */
	Image getImage();
	
	/**
	 * <p>Set new image</p>
	 * @param image image to set. Can't be null.
	 */
	void setImage(Image image);
	
	/**
	 * <p>Is image modified</p>
	 * @return true if image is modified, false otherwise.
	 */
	public boolean isModified();
	
	/**
	 * <p>Set 'image modified' flag</p>
	 * @param modified 'image modified' flag to set.
	 */
	void setModified(final boolean modified);
	
	/**
	 * <p>Clone current image keeper with image inside</p> 
	 * @return current image clone with cloned image. Can't be null
	 * @throws CloneNotSupportedException never throws in reality
	 */
	Object clone() throws CloneNotSupportedException;
}
