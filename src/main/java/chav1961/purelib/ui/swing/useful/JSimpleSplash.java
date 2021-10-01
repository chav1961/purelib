package chav1961.purelib.ui.swing.useful;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.ProgressIndicator;

public class JSimpleSplash implements ProgressIndicator, AutoCloseable {
	private static final int	STRING_X_GAP = 20;
	private static final int	STRING_Y_GAP = 25;
	private static final Color	STRING_COLOR = Color.BLACK;
	private static final Font	STRING_FONT = new Font("Courier", Font.PLAIN, 12);
	private static final int	RECT_X_GAP = 20;
	private static final int	RECT_Y_GAP = 5;
	private static final int	RECT_HEIGHT = 10;
	private static final float	RECT_PERCENT = 0.02f;
	private static final Color	RECT_BOUND_COLOR = Color.BLACK;
	private static final Color	RECT_COMPLETED_COLOR = Color.CYAN;
	private static final Color	RECT_RETAINED_COLOR = Color.WHITE;
	
	private final SplashScreen 	splash;
	private final boolean		available;
	private final Graphics2D	g2d;
	private boolean				needDraw = false, needRect = false, needDrawStage = false, needRectStage = false;
	private String				caption, subcaption;
	private long				old, current, total, discrete;
	private long				oldStage, currentStage, totalStage, discreteStage;

	public JSimpleSplash() throws NullPointerException, EnvironmentException {
		this.splash = SplashScreen.getSplashScreen();
		
		if (splash == null) {
			this.available = false;
			this.g2d = null;
		}
		else {
			this.available = true;
			this.g2d = splash.createGraphics();
		}
	}
	
	public JSimpleSplash(final URL imageURL) throws IOException, NullPointerException, EnvironmentException {
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
		if (available) {
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

	protected void refresh() {
		if (available) {
			redraw(g2d);
			splash.update();
		}
	}
	
	protected void redraw(final Graphics2D g2d) {
		if (needDraw) {
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
//		        	final long	curr = current;
//		        	final int	width = rect.width - 2 * RECT_X_GAP;
//		        	final int	percent = (int) (100.0 * curr / total);
//		        	final int	widthLeft = width * percent / 100; 
//		        	final int	widthRight = width - widthLeft;
//		        	
//			        g2d.setColor(RECT_COMPLETED_COLOR);
//			        g2d.fillRect(RECT_X_GAP, rect.height - RECT_Y_GAP - height, widthLeft, height);
//			        g2d.setColor(RECT_RETAINED_COLOR);
//			        g2d.fillRect(RECT_X_GAP + widthLeft, rect.height - RECT_Y_GAP - height, widthRight, height);
//			        g2d.setColor(RECT_BOUND_COLOR);
//			        g2d.drawRect(RECT_X_GAP, rect.height - RECT_Y_GAP - height, width, height);
		        }
		        if (cap != null) {
		        	drawText(g2d, yGap, cap);
//		        	final Font	oldFont = g2d.getFont();
//		        	
//			        g2d.setColor(STRING_COLOR);
//		        	g2d.setFont(STRING_FONT);
//		        	g2d.drawString(cap, STRING_X_GAP, rect.height - STRING_Y_GAP - (needRect ? height + RECT_Y_GAP : 0));
//		        	g2d.setFont(oldFont);
		        }
	        }
	        g2d.setColor(oldColor);
		}
	}

	private void drawText(final Graphics2D g2d, final int gap, final String text) {
		final Rectangle	rect = splash.getBounds();
    	final Font		oldFont = g2d.getFont();
    	
        g2d.setColor(STRING_COLOR);
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
    	
        g2d.setColor(RECT_COMPLETED_COLOR);
        g2d.fillRect(RECT_X_GAP, rect.height - gap - height, widthLeft, height);
        g2d.setColor(RECT_RETAINED_COLOR);
        g2d.fillRect(RECT_X_GAP + widthLeft, rect.height - gap - height, widthRight, height);
        g2d.setColor(RECT_BOUND_COLOR);
        g2d.drawRect(RECT_X_GAP, rect.height - gap - height, width, height);
	}
	
	private long calcDiscrete(final long total) {
		final Rectangle	rect = splash.getBounds();
		final int		len = rect.width - 2 * RECT_X_GAP;
		
		return Math.max(total / len, 1);
	}
}
