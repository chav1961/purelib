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
	
	private final SplashScreen 	splash = SplashScreen.getSplashScreen();
	private final Graphics2D	g2d;
	private boolean				needDraw = false, needRect = false;
	private String				caption;
	private long				old, current, total, discrete;

	public JSimpleSplash() throws NullPointerException, EnvironmentException {
		if (splash == null) {
			throw new EnvironmentException("Splash screnn functionality is not available");
		}
		else {
			g2d = splash.createGraphics();
		}
	}
	
	public JSimpleSplash(final URL imageURL) throws IOException, NullPointerException, EnvironmentException {
		if (imageURL == null) {
			throw new NullPointerException("Image URL can't be null"); 
		}
		else if (splash == null) {
			throw new EnvironmentException("Splash screnn functionality is not available");
		}
		else {
			splash.setImageURL(imageURL);
			g2d = splash.createGraphics();
		}
	}

	@Override
	public void close() throws RuntimeException {
		end();
		splash.close();
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
			splash.update();
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
			redraw(g2d);
			splash.update();
		}
	}

	@Override
	public boolean processed(final long processed) {
		current = processed;
		if (current - old > discrete) {
			old = current;
			if (needRect) {
				redraw(g2d);
				splash.update();
			}
		}
		return true;
	}

	@Override
	public void end() {
		this.needDraw = false;
		this.caption = null;
		redraw(g2d);
		splash.update();
	}
	
	public void caption(final String caption) {
		if (caption == null || caption.isEmpty())  {
			throw new IllegalArgumentException("Caption to set can't be null or empty");
		}
		else if (!needDraw) {
			throw new IllegalStateException("Call this method is valid between calling start(...) and end() only");
		}
		else {
			this.caption = caption;
			redraw(g2d);
			splash.update();
		}
	}
	
	
	protected void redraw(final Graphics2D g2d) {
		if (needDraw) {
        	final Color		oldColor =  g2d.getColor();  
			final Rectangle	rect = splash.getBounds();
        	final int		height = Math.max(RECT_HEIGHT, (int) (RECT_PERCENT * rect.height));
			
	        g2d.setComposite(AlphaComposite.Clear);
	        g2d.fillRect(0, 0, rect.width, rect.height);
	        g2d.setPaintMode();
	        g2d.setColor(Color.BLACK);
	        final String	cap = caption;

	        if (cap != null) {
	        	final Font	oldFont = g2d.getFont();
	        	
		        g2d.setColor(STRING_COLOR);
	        	g2d.setFont(STRING_FONT);
	        	g2d.drawString(cap, STRING_X_GAP, rect.height - STRING_Y_GAP - (needRect ? height + RECT_Y_GAP : 0));
	        	g2d.setFont(oldFont);
	        }
	        if (needRect) {
	        	final long	curr = current;
	        	final int	width = rect.width - 2 * RECT_X_GAP;
	        	final int	percent = (int) (100.0 * curr / total);
	        	final int	widthLeft = width * percent / 100; 
	        	final int	widthRight = width - widthLeft;
	        	
		        g2d.setColor(RECT_COMPLETED_COLOR);
		        g2d.fillRect(RECT_X_GAP, rect.height - RECT_Y_GAP - height, widthLeft, height);
		        g2d.setColor(RECT_RETAINED_COLOR);
		        g2d.fillRect(RECT_X_GAP + widthLeft, rect.height - RECT_Y_GAP - height, widthRight, height);
		        g2d.setColor(RECT_BOUND_COLOR);
		        g2d.drawRect(RECT_X_GAP, rect.height - RECT_Y_GAP - height, width, height);
	        }
	        g2d.setColor(oldColor);
		}
	}
}
