package chav1961.purelib.ui.swing.terminal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JPanel;

import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.FormModel;

/**
 * <p>This class emulates old-style alphanumeric screen.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class PseudoConsole extends JComponent {
	private static final long 	serialVersionUID = -5613033288319056138L;
	
	private final int			width, height;
	private final char[][]		content;
	private final Color[][][]	attributes;
	private final Font			font = new Font("Courier",Font.PLAIN,1);
	
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
			this.attributes = new Color[width][height][2];
			clear(Color.GREEN,Color.BLACK);
			writeContent(1,1,'*');
		}
	}

	public PseudoConsole writeAttribute(final int x, final int y, final Color[] colors) {
		if (x < 1 || x > width) {
			throw new IllegalArgumentException("X coordinate ["+x+"] out of range 1.."+width);
		}
		else if (y < 1 || y > width) {
			throw new IllegalArgumentException("Y coordinate ["+y+"] out of range 1.."+height);
		}
		else if (colors == null || colors.length != 2) {
			throw new IllegalArgumentException("Colors attribute can't be null and must have exactly two elements");
		}
		else if (colors[0] == null || colors[1] == null) {
			throw new IllegalArgumentException("Colors attribute contains null values "+Arrays.toString(colors));
		}
		else {
			attributes[x-1][y-1] = colors.clone();
			refresh();
			return this;
		}
	}
	
	public PseudoConsole writeAttibute(final Point point, final Color[] colors) {
		if (point == null) {
			throw new IllegalArgumentException("Point coordinate can't be null");
		}
		else {
			return writeAttribute(point.x,point.y,colors);
		}
	}
	
	public PseudoConsole writeAttribute(final Rectangle rect, final Color[] colors) {
		if (rect == null) {
			throw new IllegalArgumentException("Rectangle can't be null");
		}
		else if (rect.x < 1 || rect.x > width) {
			throw new IllegalArgumentException("Rectangle X coordinate ["+rect.x+"] out of range 1.."+width);
		}
		else if (rect.y < 1 || rect.y > width) {
			throw new IllegalArgumentException("Rectangle Y coordinate ["+rect.y+"] out of range 1.."+height);
		}
		else if (rect.width < 1 || rect.x+rect.width > width + 1) {
			throw new IllegalArgumentException("Rectangle X+width ["+(rect.x+rect.width)+"] out of range 1.."+width);
		}
		else if (rect.height < 1 || rect.y + rect.height > height + 1) {
			throw new IllegalArgumentException("Rectangle Y+height ["+(rect.y+rect.height)+"] out of range 1.."+height);
		}
		else {
			for (int x = rect.x; x < rect.x+rect.width; x++) {
				for (int y = rect.y; y < rect.y+rect.height; y++) {
					writeAttribute(x,y,colors);
				}
			}
			refresh();
			return this;
		}
	}

	public Color[] readAttribute(final int x, final int y) {
		if (x < 1 || x > width) {
			throw new IllegalArgumentException("X coordinate ["+x+"] out of range 1.."+width);
		}
		else if (y < 1 || y > width) {
			throw new IllegalArgumentException("Y coordinate ["+y+"] out of range 1.."+height);
		}
		else {
			return attributes[x][y].clone();
		}
	}
	
	public Color[] readAttribute(final Point point) {
		if (point == null) {
			throw new IllegalArgumentException("Point coordinate can't be null");
		}
		else {
			return readAttribute(point.x,point.y);
		}
	}
	
	public Color[][][] readAttibute(final Rectangle rect) {
		if (rect == null) {
			throw new IllegalArgumentException("Rectangle can't be null");
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
			final Color[][][]	result = new Color[rect.width][rect.height][];
			
			for (int x = rect.x; x <= rect.x+rect.width; x++) {
				for (int y = rect.y; y <= rect.y+rect.height; y++) {
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
			throw new IllegalArgumentException("Point coordinate can't be null");
		}
		else {
			return writeContent(point.x,point.y,content);
		}
	}
	
	public PseudoConsole writeContent(final Rectangle rect, final char[] content) {
		if (rect == null) {
			throw new IllegalArgumentException("Rectangle can't be null");
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
			throw new IllegalArgumentException("Content can't be null");
		}
		else if (content.length > 0) {
			int		index = 0;
			
loop:		for (int x = rect.x; x < rect.x+rect.width; x++) {
				for (int y = rect.y; y < rect.y+rect.height; y++) {
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
			throw new IllegalArgumentException("Content can't be null");
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
			return content[x][y];
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
		else if (rect.width < 1 || rect.x+rect.width > width) {
			throw new IllegalArgumentException("Rectangle X+width ["+(rect.x+rect.width)+"] out of range 1.."+width);
		}
		else if (rect.height < 1 || rect.y + rect.height > height) {
			throw new IllegalArgumentException("Rectangle Y+height ["+(rect.y+rect.height)+"] out of range 1.."+height);
		}
		else {
			final char[]	result = new char[rect.width*rect.height];
			int				index = 0;
			
			for (int x = rect.x; x <= rect.x+rect.width; x++) {
				for (int y = rect.y; y <= rect.y+rect.height; y++) {
					result[index++] = content[x][y];
				}
			}
			return result;
		}
	}
	
	public PseudoConsole clear(final Color color, final Color bkGnd) {
		if (color  == null) {
			throw new IllegalArgumentException("Color can't be null");
		}
		else if (bkGnd  == null) {
			throw new IllegalArgumentException("Background color can't be null");
		}
		else {
			return writeAttribute(new Rectangle(1,1,width,height),new Color[]{color,bkGnd});
		}
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
	    final Graphics2D		g2d = (Graphics2D)g;
	    final AffineTransform	oldAt = g2d.getTransform();
	    final Color				oldColor = g2d.getColor();
	    final Font				oldFont = g2d.getFont();
	    final AffineTransform	at = new AffineTransform(oldAt);
	    final char[]			temp = new char[1];
	    
	    at.scale(1.0*getWidth()/width,1.0*getHeight()/height);
	    g2d.setTransform(at);
	    
	    for (int indexX = 0; indexX < width; indexX++) {
		    for (int indexY = 0; indexY < height; indexY++) {
		    	g2d.setColor(attributes[indexX][indexY][1]);
		    	g2d.fillRect(indexX,indexY,1,1);
		    }
	    }
	    
	    g2d.setFont(font);
	    at.translate(0.5,1);
	    g2d.setTransform(at);
	    for (int indexX = 0; indexX < width; indexX++) {
		    for (int indexY = 0; indexY < height; indexY++) {
		    	temp[0] = content[indexX][indexY];
		    	g2d.setColor(attributes[indexX][indexY][0]);
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
