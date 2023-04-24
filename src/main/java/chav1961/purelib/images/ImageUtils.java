package chav1961.purelib.images;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;

public class ImageUtils {
	private static final int	MULTIPLIER = (65536 + 256 + 1);

	public static final CustomIntImageFilter1		GRAY_INT = (x, y, values)->(values[0] & 0xFF000000) | ((int) (Math.round((0.3 * ((values[0] & 0xFF0000) >> 16) + 0.59 * ((values[0] & 0xFF00) >> 8) + 0.11 * ((values[0] & 0xFF) >> 0))) & 0xFF) * MULTIPLIER);
	public static final CustomIntImageFilter1		INVERT_INT = (x, y, values)->(values[0] & 0xFF000000) | ((values[0] & 0xFFFFFF) ^ 0xFFFFFF);
	public static final CustomIntImageFilter1		BLACK_TRANSPARENT_INT = (x, y, values)->(values[0] & 0xFFFFFF) == 0 ? 0 : values[0];
	public static final CustomIntImageFilter3		SOBEL_X_INT = (x, y, values, result)->mul(values,new int[]{-1,-2,-1,0,0,0,1,2,1}, result);
	public static final CustomIntImageFilter3		SOBEL_Y_INT = (x, y, values, result)->mul(values,new int[]{-1,0,1,-2,0,2,-1,0,1}, result);
	public static final CustomIntImageFilter3		SHARR_X_INT = (x, y, values, result)->mul(values,new int[]{3,10,3,0,0,0,-3,-10,-3}, result);
	public static final CustomIntImageFilter3		SHARR_Y_INT = (x, y, values, result)->mul(values,new int[]{3,0,-3,10,0,-10,3,0,-3}, result);
	public static final CustomIntImageFilter3		RANGE_INT = (x, y, values, result)->mul(values,new int[]{-1,-1,-1,-1,8,-1,-1,-1,-1}, result);
	public static final CustomIntImageFilter3		SHARP_INT = (x, y, values, result)->mul(values,new int[]{-1,-4,-1,-4,26,-4,-1,-4,-1}, result);
	public static final CustomIntImageFilter3		SMOOTH_INT = (x, y, values, result)->mul(values,new int[]{1,1,1,1,1,1,1,1,1}, result);
	public static final CustomIntImageFilter3		SMOOTH_GAUSSIAN_INT = (x, y, values, result)->mul(values,new int[]{1,2,1,2,4,2,1,2,1}, result);
	
	private static final int[]	DELTA_X_3 = {-1, 0, 1, -1, 0, 1, -1, 0, 1};
	private static final int[]	DELTA_Y_3 = {-1, -1, -1, 0, 0, 0, 1, 1, 1};

	@FunctionalInterface
	public static interface CustomImageFilter {
		int getFilterSize();
	}

	@FunctionalInterface
	public static interface CustomIntImageFilter1 extends CustomImageFilter {
		int process(final int x, final int y, final int[] values);
		
		default int getFilterSize() {
			return 1;
		}
	}
	
	@FunctionalInterface
	public static interface CustomIntImageFilter3 extends CustomImageFilter {
		void process(final int x, final int y, final int[] values, final float[] result);
		
		default int getFilterSize() {
			return 3;
		}
	}

	public static BufferedImage filter(final BufferedImage source, final CustomImageFilter filter) {
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
	
	public static BufferedImage filter(final BufferedImage source, final Rectangle rect, final CustomImageFilter filter) {
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
