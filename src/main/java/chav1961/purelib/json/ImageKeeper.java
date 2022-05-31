package chav1961.purelib.json;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.imageio.ImageIO;

import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.streams.byte2char.Byte2HexOutputStream;
import chav1961.purelib.streams.char2byte.Hex2ByteInputStream;

public class ImageKeeper implements Serializable {
	private static final long 	serialVersionUID = 8962529989993415955L;
	private static final String	EMPTY_RESOURCE_NAME = "empty.png";
	private static final Image	EMPTY;

	static {
		try{EMPTY = ImageIO.read(ImageKeeper.class.getResourceAsStream(EMPTY_RESOURCE_NAME));
		} catch (IOException e) {
			throw new PreparationException("Image ["+EMPTY_RESOURCE_NAME+"] loading failed"); 
		}
	}
	
	private Image	image = EMPTY;
	
	public ImageKeeper() {
	}

	public ImageKeeper(final Image image) {
		if (image == null) {
			throw new NullPointerException("File instance can't be null or empty"); 
		}
		else {
			this.image = image;
		}
	}

	public ImageKeeper(final String imageDump) {
		if (imageDump == null) {
			throw new NullPointerException("Image dump can't be null"); 
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
	
	public Image getImage() {
		return image;
	}
	
	public void setImage(final Image image) {
		if (image == null) {
			throw new NullPointerException("File instance can't be null or empty"); 
		}
		else {
			this.image = image;
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
