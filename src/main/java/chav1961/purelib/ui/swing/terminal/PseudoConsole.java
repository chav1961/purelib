package chav1961.purelib.ui.swing.terminal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import chav1961.purelib.ui.ColorPair;

/**
 * <p>This class emulates old-style alphanumeric screen.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class PseudoConsole extends JComponent {
	private static final long 		serialVersionUID = -5613033288319056138L;
	private final Set<ColorPair>	attributesCache = new HashSet<>();
	private final char[][]			content;
	private final ColorPair[][]		attributes;
	private final Font				font = new Font("Courier",Font.PLAIN,1);
	private final int				width, height;
	
	public PseudoConsole(final int width, final int height) {
		if (width <= 0) {
			throw new IllegalArgumentException("Width ["+width+"] need be positive");
		}
		else if (height <= 0) {
			throw new IllegalArgumentException("Height ["+height+"] need be positive");
		}
		else {
			this.width = width;
			this.height = height;
			this.content = new char[width][height];
			this.attributes = new ColorPair[width][height];
			TermUtils.clear(this,Color.GREEN,Color.BLACK);
		}
	}

	public int getConsoleWidth() {
		return width;
	}

	public int getConsoleHeight() {
		return height;
	}
	
	public PseudoConsole writeAttribute(final int x, final int y, final Color color, final Color bkGnd) {
		if (x < 1 || x > width) {
			throw new IllegalArgumentException("X coordinate ["+x+"] out of range 1.."+width);
		}
		else if (y < 1 || y > width) {
			throw new IllegalArgumentException("Y coordinate ["+y+"] out of range 1.."+height);
		}
		else if (color == null) {
			throw new NullPointerException("Color can't be null");
		}
		else if (bkGnd == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else {
			return writeAttribute(x, y, valueOf(color,bkGnd));
		}
	}
	
	public PseudoConsole writeAttribute(final int x, final int y, final ColorPair colors) {
		if (x < 1 || x > width) {
			throw new IllegalArgumentException("X coordinate ["+x+"] out of range 1.."+width);
		}
		else if (y < 1 || y > width) {
			throw new IllegalArgumentException("Y coordinate ["+y+"] out of range 1.."+height);
		}
		else if (colors == null) {
			throw new NullPointerException("Colors attribute can't be null");
		}
		else {
			attributes[x-1][y-1] = valueOf(colors.getForeground(),colors.getBackground());
			refresh();
			return this;
		}
	}

	public PseudoConsole writeAttribute(final Point point, final Color color, final Color bkGnd) {
		if (point == null) {
			throw new NullPointerException("Point coordinate can't be null");
		}
		else if (color == null) {
			throw new NullPointerException("Color can't be null");
		}
		else if (bkGnd == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else {
			return writeAttribute(point,valueOf(color,bkGnd));
		}
	}
	
	public PseudoConsole writeAttribute(final Point point, final ColorPair colors) {
		if (point == null) {
			throw new NullPointerException("Point coordinate can't be null");
		}
		else {
			return writeAttribute(point.x,point.y,colors);
		}
	}

	public PseudoConsole writeAttribute(final Rectangle rect, final Color color, final Color bkGnd) {
		if (rect == null) {
			throw new NullPointerException("Rectangle can't be null");
		}
		else if (color == null) {
			throw new NullPointerException("Color can't be null");
		}
		else if (bkGnd == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else {
			return writeAttribute(rect, valueOf(color, bkGnd));
		}
	}
	
	public PseudoConsole writeAttribute(final Rectangle rect, final ColorPair colors) {
		if (rect == null) {
			throw new NullPointerException("Rectangle can't be null");
		}
		else if (rect.x < 1 || rect.x > width) {
			throw new IllegalArgumentException("Rectangle X coordinate ["+rect.x+"] out of range 1.."+width);
		}
		else if (rect.y < 1 || rect.y > width) {
			throw new IllegalArgumentException("Rectangle Y coordinate ["+rect.y+"] out of range 1.."+height);
		}
		else if (rect.width < 1 || rect.x+rect.width-1 > width) {
			throw new IllegalArgumentException("Rectangle X+width ["+(rect.x+rect.width)+"] out of range 1.."+width);
		}
		else if (rect.height < 1 || rect.y + rect.height-1 > height) {
			throw new IllegalArgumentException("Rectangle Y+height ["+(rect.y+rect.height)+"] out of range 1.."+height);
		}
		else {
			final ColorPair	clone = valueOf(colors.getForeground(),colors.getBackground());
			
			for (int x = rect.x; x < rect.x+rect.width; x++) {
				for (int y = rect.y; y < rect.y+rect.height; y++) {
					writeAttribute(x,y,clone);
				}
			}
			refresh();
			return this;
		}
	}

	public ColorPair readAttribute(final int x, final int y) {
		if (x < 1 || x > width) {
			throw new IllegalArgumentException("X coordinate ["+x+"] out of range 1.."+width);
		}
		else if (y < 1 || y > width) {
			throw new IllegalArgumentException("Y coordinate ["+y+"] out of range 1.."+height);
		}
		else {
			return attributes[x-1][y-1];
		}
	}
	
	public ColorPair readAttribute(final Point point) {
		if (point == null) {
			throw new NullPointerException("Point coordinate can't be null");
		}
		else {
			return readAttribute(point.x,point.y);
		}
	}
	
	public ColorPair[][] readAttribute(final Rectangle rect) {
		if (rect == null) {
			throw new NullPointerException("Rectangle can't be null");
		}
		else if (rect.x < 1 || rect.x > width) {
			throw new IllegalArgumentException("Rectangle X coordinate ["+rect.x+"] out of range 1.."+width);
		}
		else if (rect.y < 1 || rect.y > width) {
			throw new IllegalArgumentException("Rectangle Y coordinate ["+rect.y+"] out of range 1.."+height);
		}
		else if (rect.width < 1 || rect.x+rect.width > width) {
			throw new IllegalArgumentException("Rectangle X+width ["+(rect.x+rect.width)+"] out of range 1.."+width);
		}
		else if (rect.height < 1 || rect.y + rect.height > height) {
			throw new IllegalArgumentException("Rectangle Y+height ["+(rect.y+rect.height)+"] out of range 1.."+height);
		}
		else {
			final ColorPair[][]	result = new ColorPair[rect.width][rect.height];
			
			for (int x = rect.x; x < rect.x+rect.width; x++) {
				for (int y = rect.y; y < rect.y+rect.height; y++) {
					result[x-rect.x][y-rect.y] = readAttribute(x,y);
				}
			}
			return result;
		}
	}

	public PseudoConsole writeContent(final int x, final int y, final char content) {
		if (x < 1 || x > width) {
			throw new IllegalArgumentException("X coordinate ["+x+"] out of range 1.."+width);
		}
		else if (y < 1 || y > height) {
			throw new IllegalArgumentException("Y coordinate ["+y+"] out of range 1.."+height);
		}
		else {
			this.content[x-1][y-1] = content;
			refresh();
			return this;
		}
	}
	
	public PseudoConsole writeContent(final Point point, final char content) {
		if (point == null) {
			throw new NullPointerException("Point coordinate can't be null");
		}
		else {
			return writeContent(point.x,point.y,content);
		}
	}

	public PseudoConsole writeContent(final Rectangle rect, final char content) {
		if (rect == null) {
			throw new IllegalArgumentException("Rectangle can't be null");
		}
		else if (rect.x < 1 || rect.x > width) {
			throw new IllegalArgumentException("Rectangle X coordinate ["+rect.x+"] out of range 1.."+width);
		}
		else if (rect.y < 1 || rect.y > width) {
			throw new IllegalArgumentException("Rectangle Y coordinate ["+rect.y+"] out of range 1.."+height);
		}
		else if (rect.width < 1 || rect.x+rect.width-1 > width) {
			throw new IllegalArgumentException("Rectangle X+width ["+(rect.x+rect.width-1)+"] out of range 1.."+width);
		}
		else if (rect.height < 1 || rect.y + rect.height-1 > height) {
			throw new IllegalArgumentException("Rectangle Y+height ["+(rect.y+rect.height-1)+"] out of range 1.."+height);
		}
		else {
			for (int x = rect.x; x < rect.x+rect.width; x++) {
				for (int y = rect.y; y < rect.y+rect.height; y++) {
					writeContent(x,y,content);
				}
			}
			refresh();
		}
		return this;
	}
	
	public PseudoConsole writeContent(final Rectangle rect, final char[] content) {
		if (rect == null) {
			throw new NullPointerException("Rectangle can't be null");
		}
		else if (rect.x < 1 || rect.x > width) {
			throw new IllegalArgumentException("Rectangle X coordinate ["+rect.x+"] out of range 1.."+width);
		}
		else if (rect.y < 1 || rect.y > width) {
			throw new IllegalArgumentException("Rectangle Y coordinate ["+rect.y+"] out of range 1.."+height);
		}
		else if (rect.width < 1 || rect.x+rect.width > width) {
			throw new IllegalArgumentException("Rectangle X+width ["+(rect.x+rect.width)+"] out of range 1.."+width);
		}
		else if (rect.height < 1 || rect.y + rect.height > height) {
			throw new IllegalArgumentException("Rectangle Y+height ["+(rect.y+rect.height)+"] out of range 1.."+height);
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (content.length > 0) {
			int		index = 0;
			
loop:		for (int y = rect.y; y < rect.y+rect.height; y++) {
				for (int x = rect.x; x < rect.x+rect.width; x++) {
					if (index < content.length) {
						writeContent(x,y,content[index++]);
					}
					else {
						break loop;
					}
				}
			}
			refresh();
		}
		return this;
	}

	public PseudoConsole writeContent(final Rectangle rect, final String content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else if (content.length() != 0) {
			return writeContent(rect,content.toCharArray());
		}
		else {
			return this;
		}
	}

	public char readContent(final int x, final int y) {
		if (x < 1 || x > width) {
			throw new IllegalArgumentException("X coordinate ["+x+"] out of range 1.."+width);
		}
		else if (y < 1 || y > width) {
			throw new IllegalArgumentException("Y coordinate ["+y+"] out of range 1.."+height);
		}
		else {
			return content[x-1][y-1];
		}
	}
	
	public char readContent(final Point point) {
		if (point == null) {
			throw new IllegalArgumentException("Point coordinate can't be null");
		}
		else {
			return readContent(point.x,point.y);
		}
	}
	
	public char[] readContent(final Rectangle rect) {
		if (rect == null) {
			throw new IllegalArgumentException("Rectangle can't be null");
		}
		else if (rect.x < 1 || rect.x > width) {
			throw new IllegalArgumentException("Rectangle X coordinate ["+rect.x+"] out of range 1.."+width);
		}
		else if (rect.y < 1 || rect.y > width) {
			throw new IllegalArgumentException("Rectangle Y coordinate ["+rect.y+"] out of range 1.."+height);
		}
		else if (rect.width < 1 || rect.x+rect.width-1 > width) {
			throw new IllegalArgumentException("Rectangle X+width ["+(rect.x+rect.width)+"] out of range 1.."+width);
		}
		else if (rect.height < 1 || rect.y + rect.height-1 > height) {
			throw new IllegalArgumentException("Rectangle Y+height ["+(rect.y+rect.height)+"] out of range 1.."+height);
		}
		else {
			final char[]	result = new char[rect.width*rect.height];
			int				index = 0;
			
			for (int y = rect.y; y < rect.y+rect.height; y++) {
				for (int x = rect.x; x < rect.x+rect.width; x++) {
					result[index++] = content[x-1][y-1];
				}
			}
			return result;
		}
	}
	
	public void scrollUp(final Color color, final Color bkGnd) {
		if (color == null) {
			throw new NullPointerException("Color can't be null");
		}
		else if (bkGnd == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else {
			final Rectangle	lastLine = new Rectangle(1,height,width,1); 
			
			for (char[] item : content) {
				System.arraycopy(item,1,item,0,item.length-1);
			}
			for (ColorPair[] item : attributes) {
				System.arraycopy(item,1,item,0,item.length-1);
			}
			writeAttribute(lastLine,valueOf(color,bkGnd));
			writeContent(lastLine,' ');
		}
	}

	public void scrollDown(final Color color, final Color bkGnd) {
		if (color == null) {
			throw new NullPointerException("Color can't be null");
		}
		else if (bkGnd == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else {
			final Rectangle	firstLine = new Rectangle(1,1,width,1); 
			
			for (char[] item : content) {
				System.arraycopy(item,0,item,1,item.length-1);
			}
			for (ColorPair[] item : attributes) {
				System.arraycopy(item,0,item,1,item.length-1);
			}
			writeAttribute(firstLine,valueOf(color,bkGnd));
			writeContent(firstLine,' ');
		}
	}

	@Override
	public boolean isOpaque() {
		return false;
	}

	ColorPair valueOf(final Color foreground, final Color background) {
		for (ColorPair item : attributesCache) {
			if (item.getForeground().equals(foreground) && item.getBackground().equals(background)) {
				return	item;
			}
		}
		final ColorPair	newPair = new ColorPair(foreground, background);
		
		attributesCache.add(newPair);
		return newPair;
	}
	
	@Override
	protected void paintComponent(final Graphics g) { 
	    final Graphics2D		g2d = (Graphics2D)g;
	    final AffineTransform	oldAt = g2d.getTransform();
	    final Color				oldColor = g2d.getColor();
	    final Font				oldFont = g2d.getFont();
	    final AffineTransform	at = new AffineTransform(oldAt);
	    final char[]			temp = new char[1];
	    
	    at.scale(1.0*getWidth()/getConsoleWidth(),1.0*getHeight()/getConsoleHeight());
	    g2d.setTransform(at);
	    
	    for (int indexX = 0; indexX < getConsoleWidth(); indexX++) {
		    for (int indexY = 0; indexY < getConsoleHeight(); indexY++) {
		    	g2d.setColor(attributes[indexX][indexY].getBackground());
		    	g2d.fillRect(indexX,indexY,1,1);
		    }
	    }
	    
	    g2d.setFont(font);
	    at.translate(0,1);
	    g2d.setTransform(at);
	    for (int indexX = 0; indexX < getConsoleWidth(); indexX++) {
		    for (int indexY = 0; indexY < getConsoleHeight(); indexY++) {
		    	temp[0] = content[indexX][indexY];
		    	g2d.setColor(attributes[indexX][indexY].getForeground());
		    	g2d.drawChars(temp,0,1,indexX,indexY);
		    }
	    }
	    g2d.setFont(oldFont);
	    g2d.setColor(oldColor);
	    g2d.setTransform(oldAt);
	}  

	private void refresh() {
		repaint();
	}
}
