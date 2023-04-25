package chav1961.purelib.images;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import chav1961.purelib.basic.Utils;

public class ImageWrapper implements Cloneable {
	public static final AffineTransform	IDENTITY = new AffineTransform();

	public static enum FilterPostprocessAction {
		SATURATE,
		SCALE_EVERY,
		SCALE_ALL,
		NONE
	}

	public static enum TransformPostprocessAction {
		FILL,
		FILL_PROPORTIONAL,
		NONE
	}
	
	private volatile BufferedImage	image;
	private final Rectangle			imageRect;
	
	
	@FunctionalInterface
	public static interface Int2IntFunction {
		int apply(int x, int y, int value);
	}

	@FunctionalInterface
	public static interface IntArray2FloatFunction {
		void apply(int x, int y, int[] value, float[] result);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param image image to wrap. Can't be null
	 * @throws NullPointerException when image to wrap is null
	 */
	public ImageWrapper(final BufferedImage image) throws NullPointerException {
		if (image == null) {
			throw new NullPointerException("Image can't be null");
		}
		else {
			this.image = image;
			this.imageRect = new Rectangle(0, 0, image.getWidth(), image.getHeight());
		}
	}

//	public ImageWrapper(final int width, final int heght, final int contentType) {
//		
//	}

	/**
	 * <p>Constructor of the class</p>
	 * @param is stream to read image from. Can't be null
	 * @throws NullPointerException when stream to read image from is null
	 * @throws IOException on any I/O errors
	 */
	public ImageWrapper(final InputStream is) throws NullPointerException, IOException {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null");
		}
		else {
			this.image = ImageIO.read(is);
			this.imageRect = new Rectangle(0, 0, image.getWidth(), image.getHeight());
		}
	}
	
	@Override
	public ImageWrapper clone() throws CloneNotSupportedException {
		final BufferedImage	result = new BufferedImage(getWidth(), getHeight(), getImage().getType());
		final Graphics2D	g2d = (Graphics2D) result.getGraphics();
		
		g2d.drawImage((BufferedImage)getImage(), IDENTITY, null);
		g2d.dispose();
		return new ImageWrapper(result);
	}

	/**
	 * <p>Extract rectangle area from the wrapper as a new instance of {@linkplain ImageWrapper}</p>
	 * @param rect area to extract. Can't be null
	 * @return new instance of {@linkplain ImageWrapper} with content extracted. Can't be null
	 * @throws NullPointerException when rectangle is null
	 * @throws IllegalArgumentException when rectangle in not inside the source image
	 */
	public ImageWrapper extract(final Rectangle rect) throws NullPointerException, IllegalArgumentException {
		if (rect == null) {
			throw new NullPointerException("Rectangle can't be null"); 
		}
		else if (!imageRect.contains(rect)) {
			throw new IllegalArgumentException("Rectangle ["+rect+"] is not inside source image rectangle ["+imageRect+"]"); 
		}
		else {
			final BufferedImage	result = new BufferedImage(rect.width, rect.height, getImage().getType());
			final Graphics2D	g2d = (Graphics2D) result.getGraphics();
			
			g2d.drawImage((BufferedImage)getImage(), rect.x, rect.y, rect.width, rect.height, 0, 0, rect.width, rect.height, null);
			g2d.dispose();
			return new ImageWrapper(result);
		}
	}

	/**
	 * <p>Crop image content with rectangle and return the same instance of the image wrapper</p>
	 * @param rect rectangle to crop. Can't be null
	 * @return self
	 * @throws NullPointerException when rectangle is null
	 * @throws IllegalArgumentException when rectangle in not inside the source image
	 */
	public ImageWrapper crop(final Rectangle rect) throws NullPointerException, IllegalArgumentException {
		if (rect == null) {
			throw new NullPointerException("Rectangle can't be null"); 
		}
		else if (!imageRect.contains(rect)) {
			throw new IllegalArgumentException("Rectangle ["+rect+"] is not inside source image rectangle ["+imageRect+"]"); 
		}
		else {
			final BufferedImage	result = new BufferedImage(rect.width, rect.height, getImage().getType());
			final Graphics2D	g2d = (Graphics2D) result.getGraphics();
			
			g2d.drawImage((BufferedImage)getImage(), rect.x, rect.y, rect.width, rect.height, 0, 0, rect.width, rect.height, null);
			g2d.dispose();
			image = result;
			return this;
		}
	}	
	
	/**
	 * <p>Process content of the image wrapped and return the same instance of the image wrapper.</p>
	 * @param f function to process every pixel of the image. Can't be null
	 * @return self
	 * @throws NullPointerException when function is null
	 */
	public ImageWrapper filter(final Int2IntFunction f) throws NullPointerException {
		if (f == null) {
			throw new NullPointerException("Filter function can't be null"); 
		}
		else {
			final int[]	pixels = new int[imageRect.width * imageRect.height];
			
			((BufferedImage)getImage()).getRGB(imageRect.x, imageRect.y, imageRect.width, imageRect.height, pixels, 0, imageRect.width);
			for(int height = 0; height < imageRect.height; height++) {
				for(int width = 0; width < imageRect.width; width++) {
					final int	index = height * imageRect.width + width; 
					
					pixels[index] = f.apply(imageRect.x + width, imageRect.y + height, pixels[index]);
				}
			}
			((BufferedImage)getImage()).setRGB(imageRect.x, imageRect.y, imageRect.width, imageRect.height, pixels, 0, imageRect.width);
			return this;
		}
	}

	/**
	 * <p>Process piece of of the image content wrapped and return the same instance of the image wrapper.</p>
	 * @param rect rectangle piece to process
	 * @param f function to process every pixel of the image. Can't be null
	 * @return self
	 * @throws NullPointerException when function is null
	 * @throws IllegalArgumentException when rectangle in not inside the source image
	 */
	public ImageWrapper filter(final Rectangle rect, final Int2IntFunction f) throws NullPointerException, IllegalArgumentException {
		if (f == null) {
			throw new NullPointerException("Filter function can't be null"); 
		}
		else if (rect == null) {
			throw new NullPointerException("Rectangle can't be null"); 
		}
		else if (!imageRect.contains(rect)) {
			throw new IllegalArgumentException("Rectangle ["+rect+"] is not inside source image rectangle ["+imageRect+"]"); 
		}
		else {
			final int[]	pixels = new int[rect.width * rect.height];
			
			((BufferedImage)getImage()).getRGB(rect.x, rect.y, rect.width, rect.height, pixels, 0, imageRect.width);
			for(int height = 0; height < rect.height; height++) {
				for(int width = 0; width < rect.width; width++) {
					final int	index = height * rect.width + width; 
					
					pixels[index] = f.apply(rect.x + width, rect.y + height, pixels[index]);
				}
			}
			((BufferedImage)getImage()).setRGB(rect.x, rect.y, rect.width, rect.height, pixels, 0, imageRect.width);
			return this;
		}
	}

	/**
	 * <p>Process content of the image wrapped and return the same instance of the image wrapper.</p>
	 * @param filterSize filer size. Must be greater than 0 and must be odd
	 * @param f function to process pixel area of the image. Can't be null
	 * @param action post-process action. Can't be null
	 * @return self
	 * @throws NullPointerException when function or post-process action is null
	 * @throws IllegalArgumentException when filter size is negative or not odd
	 */
	public ImageWrapper filter(final int filterSize, final IntArray2FloatFunction f, final FilterPostprocessAction action) throws NullPointerException, IllegalArgumentException {
		if (filterSize <= 0 || filterSize % 2 == 0) {
			throw new IllegalArgumentException("Filter size must be greater than 0 and must be odd");
		}
		else if (f == null) {
			throw new NullPointerException("Filter function can't be null"); 
		}
		else if (action == null) {
			throw new NullPointerException("Filter postprocess action can't be null"); 
		}
		else {
			final int[]		pixels = new int[imageRect.width * imageRect.height];
			final float[]	forR = new float[pixels.length];
			final float[]	forG = new float[pixels.length];
			final float[]	forB = new float[pixels.length];
			final float[]	forA = new float[pixels.length];
			final int[]		forArea = new int[filterSize * filterSize];
			final float[]	forResult = new float[4];
			
			((BufferedImage)getImage()).getRGB(imageRect.x, imageRect.y, imageRect.width, imageRect.height, pixels, 0, imageRect.width);
			for(int height = 0; height < imageRect.height; height++) {
				for(int width = 0; width < imageRect.width; width++) {
					f.apply(imageRect.x + width, imageRect.y + height, forArea, forResult);
				}
			}
			postprocess(pixels, action, forA, forR, forG, forB);
			((BufferedImage)getImage()).setRGB(imageRect.x, imageRect.y, imageRect.width, imageRect.height, pixels, 0, imageRect.width);
			return this;
		}
	}

	/**
	 * <p>Process piece of the image content wrapped and return the same instance of the image wrapper.</p>
	 * @param filterSize filer size. Must be greater than 0 and must be odd
	 * @param rect rectangle piece to process
	 * @param f function to process pixel area of the image. Can't be null
	 * @return self
	 * @throws NullPointerException when function or post-process action is null
	 * @throws IllegalArgumentException when filter size is negative or not odd
	 */
	public ImageWrapper filter(final int filterSize, final Rectangle rect, final IntArray2FloatFunction f, final FilterPostprocessAction action) throws NullPointerException, IllegalArgumentException {
		if (filterSize <= 0 || filterSize % 2 == 0) {
			throw new IllegalArgumentException("Filter size must be greater than 0 and must be odd");
		}
		else if (rect == null) {
			throw new NullPointerException("Rectangle can't be null"); 
		}
		else if (!imageRect.contains(rect)) {
			throw new IllegalArgumentException("Rectangle ["+rect+"] is not inside source image rectangle ["+imageRect+"]"); 
		}
		else if (f == null) {
			throw new NullPointerException("Filter function can't be null"); 
		}
		else if (action == null) {
			throw new NullPointerException("Filter postprocess action can't be null"); 
		}
		else {
			final int[]		pixels = new int[rect.width * rect.height];
			final float[]	forR = new float[pixels.length];
			final float[]	forG = new float[pixels.length];
			final float[]	forB = new float[pixels.length];
			final float[]	forA = new float[pixels.length];
			final int[]		forArea = new int[filterSize * filterSize];
			final float[]	forResult = new float[4];
			
			((BufferedImage)getImage()).getRGB(rect.x, rect.y, rect.width, rect.height, pixels, 0, imageRect.width);
			for(int height = 0; height < rect.height; height++) {
				for(int width = 0; width < rect.width; width++) {
					f.apply(rect.x + width, rect.y + height, forArea, forResult);
				}
			}
			postprocess(pixels, action, forA, forR, forG, forB);
			((BufferedImage)getImage()).setRGB(rect.x, rect.y, rect.width, rect.height, pixels, 0, imageRect.width);
			return this;
		}
	}

	/**
	 * <p>Transform image content wrapped and return the same instance of the image wrapper.</p>
	 * @param at transform to use. Can't be null
	 * @return self
	 * @throws NullPointerException when parameter is null
	 */
	public ImageWrapper transform(final AffineTransform at) throws NullPointerException {
		return transform(at, TransformPostprocessAction.NONE);
	}
	
	/**
	 * <p>Transform image content wrapped and return the same instance of the image wrapper.</p>
	 * @param at transform to use. Can't be null
	 * @param action post-transform action. Can't be null
	 * @return self
	 * @throws NullPointerException when any parameter is null
	 */
	public ImageWrapper transform(final AffineTransform at, final TransformPostprocessAction action) throws NullPointerException {
		if (at == null) {
			throw new NullPointerException("Transform can't be null"); 
		}
		else if (action == null) {
			throw new NullPointerException("Transform postprocess action can't be null"); 
		}
		else {
			final GeneralPath	gp = new GeneralPath();
			
			gp.moveTo(imageRect.x, imageRect.y);
			gp.lineTo(imageRect.x + imageRect.width, imageRect.y);
			gp.lineTo(imageRect.x + imageRect.width, imageRect.y + imageRect.height);
			gp.lineTo(imageRect.x, imageRect.y + imageRect.height);
			gp.closePath();
			gp.transform(at);
			
			final Rectangle2D		rect = gp.getBounds2D();
			
			switch (action) {
				case FILL		:
					final BufferedImage		fillResult = new BufferedImage(imageRect.width, imageRect.height, getImage().getType());
					final Graphics2D		fillG2d = (Graphics2D) fillResult.getGraphics();
					final AffineTransform	fillTemp = new AffineTransform(at);
					
					fillTemp.translate(imageRect.x - rect.getX(), imageRect.y - rect.getY());
					fillTemp.scale(rect.getWidth()/imageRect.width, rect.getHeight()/imageRect.height);
					fillG2d.drawImage(getImage(), at, null);
					fillG2d.dispose();
					image = fillResult;
					break;
				case FILL_PROPORTIONAL	:
					final BufferedImage		fillPropResult = new BufferedImage(imageRect.width, imageRect.height, getImage().getType());
					final Graphics2D		fillPropG2d = (Graphics2D) fillPropResult.getGraphics();
					final AffineTransform	fillPropTemp = new AffineTransform(at);
					
					fillPropTemp.translate(imageRect.getCenterX() - rect.getCenterX(), imageRect.getCenterY() - rect.getCenterY());
					final double			xScale = rect.getWidth()/imageRect.width; 
					final double			yScale = rect.getHeight()/imageRect.height;
					final double			minScale = Math.min(xScale, yScale);
					
					fillPropTemp.scale(minScale, minScale);
					fillPropG2d.drawImage(getImage(), at, null);
					fillPropG2d.dispose();
					image = fillPropResult;
					break;
				case NONE		:
					final BufferedImage		noneResult = new BufferedImage((int)rect.getWidth(), (int)rect.getHeight(), getImage().getType());
					final Graphics2D		noneG2d = (Graphics2D) noneResult.getGraphics();
					final AffineTransform	noneTemp = new AffineTransform(at);
					
					noneTemp.translate(imageRect.getCenterX() - rect.getCenterX(), imageRect.getCenterY() - rect.getCenterY());
					
					noneG2d.drawImage(getImage(), noneTemp, null);
					noneG2d.dispose();
					image = noneResult;
					imageRect.setBounds(0, 0, image.getWidth(), image.getHeight());
					break;
				default:
					throw new UnsupportedOperationException("Transform postprocess action ["+action+"] is not supported yet");
			}
			return this;
		}
	}

	/**
	 * <p>Store image content to output stream.</p>
	 * @param os stream to store content to. Can't be null
	 * @param format stream format. Can't be null or empty 
	 * @throws NullPointerException rectangle or output stream argument is null
	 * @throws IllegalArgumentException null or empty string format
	 * @throws IOException on any I/O errors
	 */
	public void store(final OutputStream os, final String format) throws NullPointerException, IllegalArgumentException, IOException {
		if  (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else if (Utils.checkEmptyOrNullString(format)) {
			throw new IllegalArgumentException("Output format can't be null or empty"); 
		}
		else {
			ImageIO.write((RenderedImage)getImage(), format, os);
		}
	}

	/**
	 * <p>Store piece of image content to output stream.</p>
	 * @param rect rectangular piece to store. Can't be null
	 * @param os stream to store content to. Can't be null
	 * @param format stream format. Can't be null or empty 
	 * @throws NullPointerException rectangle or output stream argument is null
	 * @throws IllegalArgumentException null or empty string format or rectangular piece is not inside the source image
	 * @throws IOException on any I/O errors
	 */
	public void store(final Rectangle rect, final OutputStream os, final String format) throws NullPointerException, IllegalArgumentException, IOException {
		if (rect == null) {
			throw new NullPointerException("Rectangle can't be null"); 
		}
		else if (!imageRect.contains(rect)) {
			throw new IllegalArgumentException("Rectangle ["+rect+"] is not inside source image rectangle ["+imageRect+"]"); 
		}
		else if  (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else if (Utils.checkEmptyOrNullString(format)) {
			throw new IllegalArgumentException("Output format can't be null or empty"); 
		}
		else {
			extract(rect).store(os, format);
		}
	}
	
	/**
	 * <p>Get image wrapped.</p>
	 * @return image wrapped. Can't be null
	 */
	public BufferedImage getImage() {
		return image;
	}
	
	/**
	 * <p>Get wrapped image width</p>
	 * @return wrapped image width
	 */
	public int getWidth() {
		return imageRect.width;
	}

	/**
	 * <p>Get wrapped image height.</p>
	 * @return wrapped image height
	 */
	public int getHeight() {
		return imageRect.height;
	}

	protected int saturate(final float val) {
		if (val < 0) {
			return 0;
		}
		else if (val >= 256) {
			return 256;
		}
		else {
			return Math.round(val);
		}
	}

	protected void postprocess(final int[] pixels, final FilterPostprocessAction action, final float[] forA, final float[] forR, final float[] forG, final float[] forB) {
		switch (action) {
			case NONE			:
				for (int index = 0; index < pixels.length; index++) {
					pixels[index] = ((int)forA[index] << 24) | ((int)forR[index] << 16) | ((int)forG[index] << 8) | ((int)forB[index] << 0);  
				}
				break;
			case SATURATE		:
				for (int index = 0; index < pixels.length; index++) {
					pixels[index] = (saturate(forA[index]) << 24) | (saturate(forR[index]) << 16) 
									| (saturate(forG[index]) << 8) | (saturate(forB[index]) << 0);  
				}
				break;
			case SCALE_EVERY	:
				float	alphaDispl = 0, redDispl = 0, greenDispl = 0, blueDispl = 0;
				float	alphaScale = 0, redScale = 0, greenScale = 0, blueScale = 0;
				
				for(float item : forA) {
					if (item < alphaDispl) {
						alphaDispl = item;
					}
					if (item > alphaScale) {
						alphaScale = item;
					}
				}
				if (alphaScale - alphaDispl >= 256) {
					alphaScale = 256 / (alphaScale - alphaDispl); 
				}
				for(float item : forR) {
					if (item < redDispl) {
						redDispl = item;
					}
					if (item > redScale) {
						redScale = item;
					}
				}
				if (redScale - redDispl >= 256) {
					redScale = 256 / (redScale - redDispl); 
				}
				for(float item : forG) {
					if (item < greenDispl) {
						greenDispl = item;
					}
					if (item > greenScale) {
						greenScale = item;
					}
				}
				if (greenScale - greenDispl >= 256) {
					greenScale = 256 / (greenScale - greenDispl); 
				}
				for(float item : forB) {
					if (item < blueDispl) {
						blueDispl = item;
					}
					if (item > blueScale) {
						blueScale = item;
					}
				}
				if (blueScale - blueDispl >= 256) {
					blueScale = 256 / (blueScale - blueDispl); 
				}
				for (int index = 0; index < pixels.length; index++) {
					pixels[index] = (saturate(alphaDispl + forA[index] * alphaScale) << 24) | (saturate(redDispl + forR[index] * redScale) << 16) 
									| (saturate(greenDispl + forG[index] * greenScale) << 8) | (saturate(blueDispl + forB[index] * blueScale) << 0);  
				}
				break;
			case SCALE_ALL	:
				float	min = 0, max = 0;
				
				for(float item : forA) {
					if (item < min) {
						min = item;
					}
					if (item > max) {
						max = item;
					}
				}
				for(float item : forR) {
					if (item < min) {
						min = item;
					}
					if (item > max) {
						max = item;
					}
				}
				for(float item : forG) {
					if (item < min) {
						min = item;
					}
					if (item > max) {
						max = item;
					}
				}
				for(float item : forB) {
					if (item < min) {
						min = item;
					}
					if (item > max) {
						max = item;
					}
				}
				if (max - min >= 256) {
					max = 256 / (max - min); 
				}
				for (int index = 0; index < pixels.length; index++) {
					pixels[index] = (saturate(min + forA[index] * max) << 24) | (saturate(min + forR[index] * max) << 16) 
									| (saturate(min + forG[index] * max) << 8) | (saturate(min + forB[index] * max) << 0);  
				}
				break;
			default :
				throw new UnsupportedOperationException("Postprocess action ["+action+"] is not supported yet"); 
		}
	}
}
