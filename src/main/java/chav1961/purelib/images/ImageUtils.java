package chav1961.purelib.images;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;

/**
 * <p>This class contains a set of useful methods to process image content</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class ImageUtils {
	private static final int	MULTIPLIER = (65536 + 256 + 1);

	/**
	 * <p>Predefined filter to make gray scale image</p>
	 */
	public static final CustomIntImageFilter1		GRAY_INT = (x, y, values)->(values[0] & 0xFF000000) | ((int) (Math.round((0.3 * ((values[0] & 0xFF0000) >> 16) + 0.59 * ((values[0] & 0xFF00) >> 8) + 0.11 * ((values[0] & 0xFF) >> 0))) & 0xFF) * MULTIPLIER);
	
	/**
	 * <p>Predefined filter to make inverted image</p>
	 */
	public static final CustomIntImageFilter1		INVERT_INT = (x, y, values)->(values[0] & 0xFF000000) | ((values[0] & 0xFFFFFF) ^ 0xFFFFFF);
	
	/**
	 * <p>Predefined filter to replace black points to transparent</p>
	 */
	public static final CustomIntImageFilter1		BLACK_TRANSPARENT_INT = (x, y, values)->(values[0] & 0xFFFFFF) == 0 ? 0 : values[0];
	
	/**
	 * <p>Predefined Sobel X-filter</p>
	 */
	public static final CustomIntImageFilter3		SOBEL_X_INT = (x, y, values, result)->mul(values,new int[]{-1,-2,-1,0,0,0,1,2,1}, result);
	
	/**
	 * <p>Predefined Sobel Y-filter</p>
	 */
	public static final CustomIntImageFilter3		SOBEL_Y_INT = (x, y, values, result)->mul(values,new int[]{-1,0,1,-2,0,2,-1,0,1}, result);
	
	/**
	 * <p>Predefined Sharr X-filter</p>
	 */
	public static final CustomIntImageFilter3		SHARR_X_INT = (x, y, values, result)->mul(values,new int[]{3,10,3,0,0,0,-3,-10,-3}, result);
	
	/**
	 * <p>Predefined Sharr Y-filter</p>
	 */
	public static final CustomIntImageFilter3		SHARR_Y_INT = (x, y, values, result)->mul(values,new int[]{3,0,-3,10,0,-10,3,0,-3}, result);
	
	/**
	 * <p>Predefined range filter</p>
	 */
	public static final CustomIntImageFilter3		RANGE_INT = (x, y, values, result)->mul(values,new int[]{-1,-1,-1,-1,8,-1,-1,-1,-1}, result);
	
	/**
	 * <p>Predefined sharp filter</p>
	 */
	public static final CustomIntImageFilter3		SHARP_INT = (x, y, values, result)->mul(values,new int[]{-1,-4,-1,-4,26,-4,-1,-4,-1}, result);
	
	/**
	 * <p>Predefined smooth filter</p>
	 */
	public static final CustomIntImageFilter3		SMOOTH_INT = (x, y, values, result)->mul(values,new int[]{1,1,1,1,1,1,1,1,1}, result);
	
	/**
	 * <p>Predefined Gaussian smooth filter</p>
	 */
	public static final CustomIntImageFilter3		SMOOTH_GAUSSIAN_INT = (x, y, values, result)->mul(values,new int[]{1,2,1,2,4,2,1,2,1}, result);
	
	/**
	 * <p>Image filter to process</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public static interface CustomImageFilter {
		/**
		 * <p>Get filter size.</p>
		 * @return filter size. If you use filter window 3x3, the return 1, if 5x5 - return 3.
		 */
		int getFilterSize();
	}

	/**
	 * <p>3x3 image filter to process</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public static interface CustomIntImageFilter1 extends CustomImageFilter {
		int process(final int x, final int y, final int[] values);
		
		default int getFilterSize() {
			return 1;
		}
	}
	
	/**
	 * <p>5x5 image filter to process</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public static interface CustomIntImageFilter3 extends CustomImageFilter {
		void process(final int x, final int y, final int[] values, final float[] result);
		
		default int getFilterSize() {
			return 3;
		}
	}

	/**
	 * <p>Process image content with image filter and return processed result as new instance</p>
	 * @param source source image to process. Can't be null
	 * @param filter image filter to process. Can't be null
	 * @return new image instance with content processed. Can't be null
	 * @throws NullPointerException on any parameter is null
	 */
	public static BufferedImage filter(final BufferedImage source, final CustomImageFilter filter) throws NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source image can't be null");
		}
		else if (filter == null) {
			throw new NullPointerException("Image filter can't be null");
		}
		else {
			return filter(source, new Rectangle(0,0,source.getWidth(), source.getHeight()), filter);
		}
	}

	/**
	 * <p>Process rectangular piece of image content with image filter and return processed result as new instance</p>
	 * @param source source image to process. Can't be null
	 * @param rect rectangular piece to process. Can't be null
	 * @param filter image filter to process. Can't be null
	 * @return new image instance with content processed. Can't be null
	 * @throws NullPointerException on any parameter is null
	 * @throws IllegalArgumentException if size of the filter is neither 1 nor 3
	 */
	public static BufferedImage filter(final BufferedImage source, final Rectangle rect, final CustomImageFilter filter) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source image can't be null");
		}
		else if (rect == null) {
			throw new NullPointerException("Rectangle can't be null");
		}
		else if (filter == null) {
			throw new NullPointerException("Image filter can't be null");
		}
		else {
			switch (filter.getFilterSize()) {
				case 1 :
					return filter(source, (CustomIntImageFilter1)filter);
				case 3 :
					return filter(source, rect, (CustomIntImageFilter3)filter);
				default :
					throw new IllegalArgumentException("Filter size ["+filter.getFilterSize()+"] is not supported yet"); 
			}
		}
	}

	/**
	 * <p>Scale image and return new instance scaled</p>
	 * @param source image to scale. Can't be null
	 * @param newWidth new width of the image. Must be greater than 0
	 * @param newHeight new Height of the image. Must be greater than 0.
	 * @return image scaled. Can't be null
	 * @throws NullPointerException on source image is null
	 * @throws IllegalArgumentException on width or height less than or equals 0.
	 */
    public static BufferedImage scale(final BufferedImage source, final int newWidth, final int newHeight) throws NullPointerException, IllegalArgumentException {
    	if (source == null) {
    		throw new NullPointerException("Source image can't be null");
    	}
    	else if (newWidth <= 0) {
    		throw new IllegalArgumentException("New width ["+newWidth+"] must be greater than 0");
    	}
    	else if (newHeight <= 0) {
    		throw new IllegalArgumentException("New height ["+newHeight+"] must be greater than 0");
    	}
    	else {
        	return scale(source, newWidth, newHeight, Image.SCALE_FAST);
    	}
    }	

    /**
	 * <p>Scale image and return new instance scaled</p>
	 * @param source image to scale. Can't be null
	 * @param newWidth new width of the image. Must be greater than 0
	 * @param newHeight new Height of the image. Must be greater than 0.
     * @param scaleType scale type. MUst be one of {@linkplain Image#SCALE_DEFAULT}, {@linkplain Image#SCALE_AREA_AVERAGING}, {@linkplain Image#SCALE_FAST}, {@linkplain Image#SCALE_REPLICATE}, {@linkplain Image#SCALE_SMOOTH} values    
	 * @return image scaled. Can't be null
	 * @throws NullPointerException on source image is null
	 * @throws IllegalArgumentException on width or height less than or equals 0 or unknown scale type
     */
    public static BufferedImage scale(final BufferedImage source, final int newWidth, final int newHeight, final int scaleType) throws NullPointerException, IllegalArgumentException {
    	if (source == null) {
    		throw new NullPointerException("Source image can't be null");
    	}
    	else if (newWidth <= 0) {
    		throw new IllegalArgumentException("New width ["+newWidth+"] must be greater than 0");
    	}
    	else if (newHeight <= 0) {
    		throw new IllegalArgumentException("New height ["+newHeight+"] must be greater than 0");
    	}
    	else if (scaleType != Image.SCALE_AREA_AVERAGING && scaleType != Image.SCALE_DEFAULT && scaleType != Image.SCALE_FAST && scaleType != Image.SCALE_REPLICATE && scaleType != Image.SCALE_SMOOTH) {
    		throw new IllegalArgumentException("Illegal scale type ["+scaleType+"]. Only Image.SCALE_DEFAULT, Image.SCALE_AREA_AVERAGING, Image.SCALE_FAST, Image.SCALE_REPLICATE and Image.SCALE_SMOOTH are available");
    	}
    	else {
            final Image 		scaledImg = source.getScaledInstance(newWidth, newHeight, scaleType);        
            final BufferedImage	resultImg = new BufferedImage(newWidth, newHeight, source.getType());
            
            resultImg.getGraphics().drawImage(scaledImg, 0, 0, null);
            return resultImg;        
    	}
    }

    /**
     * <p>Scale proportionally and center image and return new instance scaled and centered</p>
     * @param source image to scale and center. Can't be null
	 * @param targetWidth new width of the image. Must be greater than 0
	 * @param targetHeight new Height of the image. Must be greater than 0.
     * @param padding image padding. Can't be less than 0
     * @param bgColor background color to fill free area. Can't be null
	 * @return image scaled and centered. Can't be null
	 * @throws NullPointerException on source image of color is null
	 * @throws IllegalArgumentException on width or height less than or equals 0 or padding not greater than 0
     */
    public static BufferedImage scaleAndCenter(final BufferedImage source, final int targetWidth, final int targetHeight, final int padding, final Color bgColor) throws NullPointerException, IllegalArgumentException {
    	if (source == null) {
    		throw new NullPointerException("Source image can't be null");
    	}
    	else if (targetWidth <= 0) {
    		throw new IllegalArgumentException("New width ["+targetWidth+"] must be greater than 0");
    	}
    	else if (targetHeight <= 0) {
    		throw new IllegalArgumentException("New height ["+targetHeight+"] must be greater than 0");
    	}
    	else if (padding < 0) {
    		throw new IllegalArgumentException("Padding ["+padding+"] must be greater or equals than 0");
    	}
    	else {
            final int	imgWidth = source.getWidth();
            final int	imgHeight = source.getHeight();
            int 		xPos, yPos;
            float 		scaleFactor = 0;
            
            if (imgWidth > imgHeight) {
                scaleFactor = imgWidth / (float)(targetWidth-2*padding);
            } 
            else {
                scaleFactor = imgHeight / (float)(targetHeight-2*padding);
            }

            final int	newWidth = (int) (imgWidth / scaleFactor);
            final int	newHeight = (int)(imgHeight / scaleFactor);
            
            final Image 		scaledImg = source.getScaledInstance(newWidth, newHeight, imgWidth);
            final BufferedImage resultImg = new BufferedImage(targetWidth, targetHeight, source.getType());
            
            resultImg.getGraphics().setColor(bgColor);
            resultImg.getGraphics().fillRect(0, 0, targetWidth, targetHeight);
                    
            if (imgWidth > imgHeight) {
                xPos = padding;
                yPos = padding + (targetHeight-2*padding - newHeight) / 2;            
            } 
            else {
                xPos = padding + (targetWidth -2*padding - newWidth) / 2;
                yPos = padding;                                    
            }
            
            resultImg.getGraphics().drawImage(scaledImg, xPos, yPos, null);
                        
            return resultImg;
    	}
    }
    
    /**
     * <p>Center image, crop rectangle from it and return content of rectangle cropped as new image instance</p>
     * @param source image to center and crop. Can't be null
	 * @param targetWidth width of the rectangle cropped. Must be greater than 0
	 * @param targetHeight new Height of the rectangle cropped. Must be greater than 0.
	 * @return image centered and cropped. Can't be null
	 * @throws NullPointerException on source image of color is null
	 * @throws IllegalArgumentException on width or height less than or equals 0 or padding not greater than 0
     */
    public static BufferedImage centerAndCrop(final BufferedImage source, final int targetWidth, final int targetHeight) throws NullPointerException, IllegalArgumentException {
    	if (source == null) {
    		throw new NullPointerException("Source image can't be null");
    	}
    	else if (targetWidth <= 0) {
    		throw new IllegalArgumentException("New width ["+targetWidth+"] must be greater than 0");
    	}
    	else if (targetHeight <= 0) {
    		throw new IllegalArgumentException("New height ["+targetHeight+"] must be greater than 0");
    	}
    	else {
	        final int 	imgWidth = source.getWidth();
	        final int 	imgHeight = source.getHeight();
	        float 		scale = 0;
	
	        
	        if (imgWidth < imgHeight) {
	            scale = imgWidth / (float)targetWidth;
	        } 
	        else {
	            scale = imgHeight / (float)targetHeight;
	        }
	
	        final int	newWidth = (int) (imgWidth / scale);
	        final int	newHeight = (int)(imgHeight / scale);
	        
	        final Image 		scaledImg = source.getScaledInstance(newWidth, newHeight, imgWidth);
	        final BufferedImage	scaledBuffImg = new BufferedImage(newWidth, newHeight, source.getType());

	        scaledBuffImg.getGraphics().drawImage(scaledImg, 0, 0, null);
	        
	        if (imgWidth < imgHeight) {
	            final int xPos = 0;
	            final int yPos = (newHeight - targetHeight) / 2;
	            
	            return scaledBuffImg.getSubimage(xPos, yPos, targetWidth, targetHeight);            
	        } else {
	            final int xPos = (newWidth-targetWidth) / 2;
	            final int yPos = 0;
	            
	            return scaledBuffImg.getSubimage(xPos, yPos, targetWidth, targetHeight);            
	        }
    	}
    }
    
	private static BufferedImage filter(final BufferedImage source, final CustomIntImageFilter1 filter) {
		final BufferedImage	result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		final ImageFilter 	f = new RGBImageFilter() {
										final int[]	content = new int[1];
										
										public int filterRGB(final int x, final int y, final int rgb) {
											content[0] = rgb;
											return filter.process(x, y, content);
										}
							       };
		final Graphics2D	g2d = (Graphics2D) result.getGraphics();
		
		g2d.drawImage(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(source.getSource(), f)), 0, 0, null);
		g2d.dispose();
		return result;		
	}

	private static BufferedImage filter(final BufferedImage source, final Rectangle rectangle, final CustomIntImageFilter3 filter) {
		final int		filterSize = filter.getFilterSize();
		final int		maxX = rectangle.width, maxY = rectangle.height;
		final int		halfSize = filterSize / 2, fstart = - halfSize, fend = halfSize, scanSize = rectangle.width + 2 * halfSize;
		final int[]		pixels = new int[scanSize * (2 * filterSize + source.getHeight())];
		final int[]		area = new int[filterSize * filterSize];
		final float[]	targetR = new float[pixels.length], targetG = new float[pixels.length], targetB = new float[pixels.length];   
		final float[]	temp = new float[3];
		final int[]		target = new int[pixels.length];
		
		source.getRGB(rectangle.x, rectangle.y, rectangle.width, rectangle.height, pixels, halfSize + scanSize * halfSize, scanSize);
		for (int y = 0; y < maxY; y++) {
			for (int x = 0; x < maxX; x++) {
				final int	currentPixel = pixels[(y + halfSize) * scanSize + (x + halfSize)];
				
				for (int fy = fstart; fy < fend; fy++) {
					for (int fx = fstart, index = 0; fx < fend; fx++, index++) {
						final int	effectiveX = halfSize + x + fx; 
						final int	effectiveY = halfSize + y + fy;
						
						area[index] = pixels[effectiveY * scanSize + effectiveX] & 0xFFFFFF;
					}
				}
				filter.process(x, y, area, temp);
				final int	result = (currentPixel & 0xFF000000);
				
				target[(y + halfSize) * scanSize + (x + halfSize)] = result; 
				targetR[(y + halfSize) * scanSize + (x + halfSize)] = temp[0]; 
				targetG[(y + halfSize) * scanSize + (x + halfSize)] = temp[1]; 
				targetB[(y + halfSize) * scanSize + (x + halfSize)] = temp[2]; 
			}
		}
		
		float	minR = targetR[0], maxR = minR;
		float	minG = targetG[0], maxG = minG;
		float	minB = targetB[0], maxB = minB;
		
		for (float item : targetR) {
			minR = Math.min(minR, item);
			maxR = Math.max(maxR, item);
		}
		for (float item : targetG) {
			minG = Math.min(minG, item);
			maxG = Math.max(maxG, item);
		}
		for (float item : targetB) {
			minB = Math.min(minB, item);
			maxB = Math.max(maxB, item);
		}
		final float	min = Math.min(minR, Math.min(minG, minB));
		final float	max = Math.max(maxR, Math.max(maxG, maxB));
		final float scale = 255/(max - min);

		for (int index = 0; index < target.length; index++) {
			final int	result = target[index] 
									| (Math.round((targetR[index] - min) * scale) << 16) & 0xFF0000
									| (Math.round((targetG[index] - min) * scale) << 8) & 0xFF00
									| (Math.round((targetB[index] - min) * scale) << 0) & 0xFF;

			target[index] = result;
		}
		
		final BufferedImage	result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		final Graphics2D	g2d = (Graphics2D) result.getGraphics();
		
		g2d.drawImage(source, 0, 0, null);
		g2d.dispose();
		result.setRGB(rectangle.x, rectangle.y, rectangle.width, rectangle.height, target, halfSize + scanSize * halfSize, scanSize);
		return result;
	}
	
	private static final void mul(final int[] values, final int[] koeffs, final float[] result) {
		for (int color = 0xFF0000, index = 0; index < result.length; color >>= 8, index++) {
			float	temp = 0;
			
			for(int k = 0; k < koeffs.length; k++) {
				temp += ((values[k] & color) >> (8 * index)) * koeffs[k];
			}
			result[index] = temp;
		}
	}
}
