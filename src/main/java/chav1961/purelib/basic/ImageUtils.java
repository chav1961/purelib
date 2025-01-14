package chav1961.purelib.basic;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

import javax.swing.GrayFilter;

/**
 * <p>THis class contains a set of useful methods to manipulate images.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
public class ImageUtils {
	/**
	 * <p>Add transparency to color selected.</p>
	 * @param image image to process. Can't be null.
	 * @param color color to add transparency to. Can't be null.
	 * @param transparency transparency from 0 to 255 (0 - fully transparent)
	 * @return image processed. Can't be null
	 * @throws NullPointerException any parameter is null
	 */
	public static Image addTransparency(final Image image, final Color color, final int transparency) throws NullPointerException {
		if (image == null) {
			throw new NullPointerException("Image to add transaparency can't be null");
		}
		else if (color == null) {
			throw new NullPointerException("Color to make transparent can't be null");
		}
		else {
			final ImageFilter	filter = new RGBImageFilter() {
							        final int	transparentColor = color.getRGB() | 0xFF000000;
							        final int   transparencyPercent = transparency << 24;
					
							        public final int filterRGB(int x, int y, int rgb) {
							        	return (rgb | 0xFF000000) == transparentColor ? transparencyPercent | 0x00FFFFFF & rgb : rgb;
							        }
							    };
			final ImageProducer filteredImgProd = new FilteredImageSource(image.getSource(), filter);
			
			return Toolkit.getDefaultToolkit().createImage(filteredImgProd);
		}
	}

	/**
	 * <p>Replace image color from one to another</p>
	 * @param image image to replace color in. Can't be null.
	 * @param fromColor color to replace. Can't be null.
	 * @param toColor color replacement. Can't be null.
	 * @return image processed. Can't be null
	 * @throws NullPointerException any parameter is null
	 */
	public static Image replaceColor(final Image image, final Color fromColor, final Color toColor) throws NullPointerException {
		if (image == null) {
			throw new NullPointerException("Image to replace color can't be null");
		}
		else if (fromColor == null) {
			throw new NullPointerException("Color to be changed can't be null");
		}
		else if (toColor == null) {
			throw new NullPointerException("Color to change can't be null");
		}
		else {
			final ImageFilter	filter = new RGBImageFilter() {
							        final int 	from = fromColor.getRGB() | 0xFF000000;
							        final int 	to = toColor.getRGB() | 0xFF000000;
					
							        public final int filterRGB(int x, int y, int rgb) {
							        	return (rgb | 0xFF000000) == from ? to : rgb;
							        }
							    };
			final ImageProducer filteredImgProd = new FilteredImageSource(image.getSource(), filter);
			
			return Toolkit.getDefaultToolkit().createImage(filteredImgProd);
		}
	}

	/**
	 * <p>Convert image content to gray scale.</p>
	 * @param image image to convert content for. Can't be null.
	 * @return image processed. Can't be null
	 * @throws NullPointerException any parameter is null
	 */
	public static Image toGrayScale(final Image image) throws NullPointerException {
		if (image == null) {
			throw new NullPointerException("Image to make gray scale for can't be null");
		}
		else {
			final ImageFilter	filter = new GrayFilter(false, 0);
			final ImageProducer filteredImgProd = new FilteredImageSource(image.getSource(), filter);
			
			return Toolkit.getDefaultToolkit().createImage(filteredImgProd);
		}
	}
}
