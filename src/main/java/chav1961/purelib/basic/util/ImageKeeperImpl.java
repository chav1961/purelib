package chav1961.purelib.basic.util;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.imageio.ImageIO;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.util.interfaces.ImageKeeper;
import chav1961.purelib.streams.byte2char.Byte2HexOutputStream;
import chav1961.purelib.streams.char2byte.Hex2ByteInputStream;

/**
 * <p>This class is used to keep image</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class ImageKeeperImpl implements ImageKeeper {
	private static final long 	serialVersionUID = 8962529989993415955L;
	private static final String	EMPTY_RESOURCE_NAME = "empty.png";
	private static final Image	EMPTY;

	static {
		try{
			EMPTY = ImageIO.read(ImageKeeperImpl.class.getResourceAsStream(EMPTY_RESOURCE_NAME));
		} catch (IOException e) {
			throw new PreparationException("Image ["+EMPTY_RESOURCE_NAME+"] loading failed"); 
		}
	}
	
	private Image	image = EMPTY;
	private boolean	isModified = false;

	/**
	 * <p>Constructor of the class instance</p>
	 */
	public ImageKeeperImpl() {
	}

	/**
	 * <p>Constructor of the class instance</p>
	 * @param image image to keep. Can't be null
	 * @throws NullPointerException Image is null
	 */
	public ImageKeeperImpl(final Image image) throws NullPointerException {
		if (image == null) {
			throw new NullPointerException("File instance can't be null or empty"); 
		}
		else {
			this.image = image;
		}
	}

	/**
	 * <p>Constructor of the class instance</p>
	 * @param imageDump hexadecimal string representation of the image content. Can be neither null nor empty
	 */
	public ImageKeeperImpl(final String imageDump) throws IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(imageDump)) {
			throw new IllegalArgumentException("Image dump can't be null or empty"); 
		}
		else {
			try(final Reader				rdr = new StringReader(imageDump);
				final Hex2ByteInputStream	is = new Hex2ByteInputStream(rdr)) {
				
				this.image = ImageIO.read(is);
			} catch (IOException exc) {
				this.image = EMPTY;
			}
		}
	}
	
	@Override
	public Image getImage() {
		return image;
	}
	
	@Override
	public void setImage(final Image image) {
		if (image == null) {
			throw new NullPointerException("File instance can't be null or empty"); 
		}
		else {
			this.image = image;
		}
	}

	@Override
	public boolean isModified() {
		return this.isModified;
	}
	
	@Override
	public void setModified(final boolean modified) {
		this.isModified = modified;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		try{return super.clone();
		} catch (CloneNotSupportedException exc) {
			return new ImageKeeperImpl();
		}
	}
	
	@Override
	public String toString() {
		try(final Writer	wr = new StringWriter();
			final Byte2HexOutputStream	os = new Byte2HexOutputStream(wr)) {
			
			ImageIO.write((RenderedImage) image, "png", os);
			os.flush();
			
			return wr.toString();
		} catch (IOException e) {
			return super.toString();
		}
	}
}
