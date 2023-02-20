package chav1961.purelib.ui.swing.useful;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.io.IOException;
import java.net.URL;

import chav1961.purelib.basic.interfaces.ProgressIndicator;

/**
 * <p>This class is a splash screen manager. It can show splash screen before main GUI application will be appeared and supports
 * {@linkplain ProgressIndicator} interface to refresh application preparation. To correct working, include <b>SplashScreen-Image</b>
 * option into your manifest.mf file</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class JSimpleSplash implements ProgressIndicator, AutoCloseable {
	private static final int	STRING_X_GAP = 20;
	private static final int	STRING_Y_GAP = 25;
	private static final Color	STRING_COLOR = Color.WHITE;
	private static final Font	STRING_FONT = new Font("Courier", Font.PLAIN, 12);
	private static final int	RECT_X_GAP = 20;
	private static final int	RECT_Y_GAP = 5;
	private static final int	RECT_HEIGHT = 10;
	private static final float	RECT_PERCENT = 0.02f;
	private static final Color	RECT_BOUND_COLOR = Color.BLACK;
	private static final Color	RECT_COMPLETED_COLOR = Color.BLUE;
	private static final Color	RECT_RETAINED_COLOR = Color.WHITE;
	
	private final SplashScreen 	splash;
	private final boolean		available;
	private final Graphics2D	g2d;
	private Color				boundColor = RECT_BOUND_COLOR, completedColor = RECT_COMPLETED_COLOR, retainedColor = RECT_RETAINED_COLOR, stringColor = STRING_COLOR;
	private boolean				needDraw = false, needRect = false, needDrawStage = false, needRectStage = false;
	private String				caption, subcaption;
	private long				old, current, total, discrete;
	private long				oldStage, currentStage, totalStage, discreteStage;

	/**
	 * <p>Show simple splash with default splash screen</p>
	 */
	public JSimpleSplash() {
		boolean			available = false;
		Graphics2D		g2d = null;
		SplashScreen	splash = null;
		
		try{splash = SplashScreen.getSplashScreen();
			
			if (splash == null) {
				available = false;
				g2d = null;
			}
			else {
				available = true;
				g2d = splash.createGraphics();
			}
		} catch (Throwable t) {
			available = false;
			g2d = null;
		}
		this.splash = splash;
		this.available = available;
		this.g2d = g2d;
	}

	/**
	 * <p>Show simple splash with custom splash URL</p>
	 * @param imageURL custom splash url.
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException custom URI is null
	 */
	public JSimpleSplash(final URL imageURL) throws IOException, NullPointerException {
		this.splash = SplashScreen.getSplashScreen();
		
		if (imageURL == null) {
			throw new NullPointerException("Image URL can't be null"); 
		}
		else if (splash == null) {
			this.available = false;
			this.g2d = null;
		}
		else {
			this.available = true;
			this.g2d = splash.createGraphics();
			splash.setImageURL(imageURL);
		}
	}

	@Override
	public void close() throws RuntimeException {
		end();
		if (available && splash.isVisible()) {
			splash.close();
		}
	}
	
	@Override
	public void start(final String caption, long total) {
		if (caption == null || caption.isEmpty())  {
			throw new IllegalArgumentException("Caption to set can't be null or empty");
		}
		else {
			this.caption = caption;
			this.needDraw = true;
			this.needRect = true;
			this.total = total;
			this.discrete = calcDiscrete(total);
			refresh();
		}
	}

	@Override
	public void start(final String caption) {
		if (caption == null || caption.isEmpty())  {
			throw new IllegalArgumentException("Caption to set can't be null or empty");
		}
		else {
			this.caption = caption;
			this.needDraw = true;
			needRect = false;
			refresh();
		}
	}

	@Override
	public void stage(final String caption, final int stage, final int of) {
		if (caption == null || caption.isEmpty())  {
			throw new IllegalArgumentException("Caption to set can't be null or empty");
		}
		else if (!needDraw) {
			throw new IllegalStateException("Stage must be called between calling start(...) and end() methods");
		}
		else {
			this.subcaption = caption;
			this.needDrawStage = true;
			this.needRectStage = false;
			refresh();
		}
	}
	
	@Override
	public void stage(final String caption, final int stage, final int of, final long totalStage) {
		if (caption == null || caption.isEmpty())  {
			throw new IllegalArgumentException("Caption to set can't be null or empty");
		}
		else if (!needDraw) {
			throw new IllegalStateException("Stage must be called between calling start(...) and end() methods");
		}
		else {
			this.subcaption = caption;
			this.needDrawStage = true;
			this.needRectStage = true;
			this.totalStage = total; 
			this.discreteStage = calcDiscrete(total);
			refresh();
		}
	}
	
	@Override
	public boolean processed(final long processed) {
		if (!needDraw) {
			throw new IllegalStateException("Call this method is valid between calling start(...) and end() only");
		}
		else if (needDrawStage) {
			currentStage = processed;
			if (currentStage - oldStage > discreteStage) {
				oldStage = currentStage;
				if (needRect) {
					refresh();
				}
			}
			return true;
		}
		else {
			current = processed;
			if (current - old > discrete) {
				old = current;
				if (needRect) {
					refresh();
				}
			}
			return true;
		}
	}

	@Override
	public void end() {
		this.needDrawStage = false;
		this.subcaption = null;
		this.needDraw = false;
		this.caption = null;
		refresh();
	}

	@Override
	public int endStage() {
		this.needDrawStage = false;
		this.subcaption = null;
		return -1;
	}
	
	@Override
	public void caption(final String caption) {
		if (caption == null || caption.isEmpty())  {
			throw new IllegalArgumentException("Caption to set can't be null or empty");
		}
		else if (!needDraw) {
			throw new IllegalStateException("Call this method is valid between calling start(...) and end() only");
		}
		else {
			this.caption = caption;
			refresh();
		}
	}

	@Override
	public void stageCaption(final String caption) {
		if (caption == null || caption.isEmpty())  {
			throw new IllegalArgumentException("Caption to set can't be null or empty");
		}
		else if (!needDrawStage) {
			throw new IllegalStateException("Call this method is valid between calling stage(...) and endStage() only");
		}
		else {
			this.subcaption = caption;
			refresh();
		}
	}

	/**
	 * <p>Get foreground (string caption) color</p>
	 * @return foreground color. Can't be null
	 */
	public Color getForegroundColor() {
		return stringColor;
	}
	
	/**
	 * <p>Get background (progress background) color</p>
	 * @return background color. Can't be null
	 */
	public Color getBackgroundColor() {
		return retainedColor;
	}
	
	/**
	 * <p>Get processed (progress processed) color</p>
	 * @return progress processed color. Can't be null
	 */
	public Color getForegroundProgressColor() {
		return completedColor;
	}
	
	/**
	 * <p>Get border (progress border) color</p>
	 * @return progress border color.Can't be null
	 */
	public Color getBorderColor() {
		return boundColor;
	}

	/**
	 * <p>Set foreground (string) color.</p>
	 * @param color color to set. Can't be null
	 * @throws NullPointerException color to set is null
	 */
	public void setForegroundColor(final Color color) throws NullPointerException {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			stringColor = color;
		}
	}
	
	/**
	 * <p>Set foreground (progress background) color.</p>
	 * @param color color to set. Can't be null
	 * @throws NullPointerException color to set is null
	 */
	public void setBackgroundColor(final Color color) throws NullPointerException  {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			retainedColor = color;
		}
	}
	
	/**
	 * <p>Set processed (progress processed) color.</p>
	 * @param color color to set. Can't be null
	 * @throws NullPointerException color to set is null
	 */
	public void setForegroundProgressColor(final Color color) throws NullPointerException  {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			completedColor = color;
		}
	}
	
	/**
	 * <p>Set border (progress border) color.</p>
	 * @param color color to set. Can't be null
	 * @throws NullPointerException color to set is null
	 */
	public void setBorderColor(final Color color) throws NullPointerException  {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			boundColor = color;
		}
	}
	
	protected void refresh() {
		if (available) {
			try {
				redraw(g2d);
				splash.update();
			} catch (IllegalStateException exc) {
			}
		}
	}
	
	protected void redraw(final Graphics2D g2d) {
		if (needDraw && g2d != null) {
        	final Color		oldColor =  g2d.getColor();  
			final Rectangle	rect = splash.getBounds();
        	final int		height = Math.max(RECT_HEIGHT, (int) (RECT_PERCENT * rect.height));
	        final String	cap = caption;
	        int				yGap = RECT_Y_GAP;
			
	        g2d.setComposite(AlphaComposite.Clear);
	        g2d.fillRect(0, 0, rect.width, rect.height);
	        g2d.setPaintMode();
	        g2d.setColor(Color.BLACK);
	        
	        if (needDrawStage) {
		        final String	subcap = caption;
	        	
		        if (needRectStage) {
		        	drawRect(g2d,yGap,height,current,total);
		        	yGap += height;
		        }
		        if (subcap != null) {
		        	drawText(g2d, yGap, subcap);
		        	yGap += height + RECT_Y_GAP;
		        }
		        if (needRect) {
		        	drawRect(g2d,yGap,height,current,total);
		        	yGap += height;
		        }
		        if (cap != null) {
		        	drawText(g2d, yGap, cap);
		        }
	        }
	        else {
		        if (needRect) {
		        	
		        	drawRect(g2d,yGap,height,current,total);
		        	yGap += height;
		        	final long	curr = current;
		        	final int	width = rect.width - 2 * RECT_X_GAP;
		        	final int	percent = (int) (100.0 * curr / total);
		        	final int	widthLeft = width * percent / 100; 
		        	final int	widthRight = width - widthLeft;
		        	
			        g2d.setColor(completedColor);
			        g2d.fillRect(RECT_X_GAP, rect.height - RECT_Y_GAP - height, widthLeft, height);
			        g2d.setColor(retainedColor);
			        g2d.fillRect(RECT_X_GAP + widthLeft, rect.height - RECT_Y_GAP - height, widthRight, height);
			        g2d.setColor(boundColor);
			        g2d.drawRect(RECT_X_GAP, rect.height - RECT_Y_GAP - height, width, height);
		        }
		        if (cap != null) {
		        	drawText(g2d, yGap, cap);
		        	final Font	oldFont = g2d.getFont();
		        	
			        g2d.setColor(stringColor);
		        	g2d.setFont(STRING_FONT);
		        	g2d.drawString(cap, STRING_X_GAP, rect.height - STRING_Y_GAP - (needRect ? height + RECT_Y_GAP : 0));
		        	g2d.setFont(oldFont);
		        }
	        }
	        g2d.setColor(oldColor);
		}
	}

	private void drawText(final Graphics2D g2d, final int gap, final String text) {
		final Rectangle	rect = splash.getBounds();
    	final Font		oldFont = g2d.getFont();
    	
        g2d.setColor(stringColor);
    	g2d.setFont(STRING_FONT);
    	g2d.drawString(text, STRING_X_GAP, rect.height - STRING_Y_GAP - gap);
    	g2d.setFont(oldFont);
	}
	
	private void drawRect(final Graphics2D g2d, final int gap, final int height, final long current, final long total) {
		final Rectangle	rect = splash.getBounds();
    	final int		width = rect.width - 2 * RECT_X_GAP;
    	final int		percent = (int) (100.0 * current / total);
    	final int		widthLeft = width * percent / 100; 
    	final int		widthRight = width - widthLeft;
    	
        g2d.setColor(completedColor);
        g2d.fillRect(RECT_X_GAP, rect.height - gap - height, widthLeft, height);
        g2d.setColor(retainedColor);
        g2d.fillRect(RECT_X_GAP + widthLeft, rect.height - gap - height, widthRight, height);
        g2d.setColor(boundColor);
        g2d.drawRect(RECT_X_GAP, rect.height - gap - height, width, height);
	}
	
	private long calcDiscrete(final long total) {
		if (available) {
			final Rectangle	rect = splash.getBounds();
			final int		len = rect.width - 2 * RECT_X_GAP;
			
			return Math.max(total / len, 1);
		}
		else {
			return 1;
		}
	}
}
