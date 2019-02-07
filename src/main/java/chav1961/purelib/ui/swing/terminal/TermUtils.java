package chav1961.purelib.ui.swing.terminal;

import java.awt.Color;
import java.awt.Rectangle;

public class TermUtils {
	private static final char[]	SINGLE_BOX= "".toCharArray();
	private static final char[]	DOUBLE_BOX= "".toCharArray();
	private static final char[]	SEMIDOUBLE_BOX= "".toCharArray();

//	U+2500 - U+257F - символы рамок
//	U+2580 - U+259F - символы заполнения
	
	public enum LineStyle {
		Single, Double, SemiDouble
	}
	
	public static void clear(final PseudoConsole console, final Color color, final Color bkGnd) {
		if (console == null) {
			throw new NullPointerException("Console can't be null");
		}
		else if (color  == null) {
			throw new NullPointerException("Color can't be null");
		}
		else if (bkGnd  == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else {
			console.writeAttribute(new Rectangle(1,1,console.getWidth(),console.getHeight()),new Color[]{color,bkGnd});
			console.writeContent(new Rectangle(1,1,console.getWidth(),console.getHeight()),' ');
		}
	}
	
	public static void line(final PseudoConsole console, final int xFrom, final int yFrom, final int xTo, final int yTo) {
		
	}

	public static void line(final PseudoConsole console, final int xFrom, final int yFrom, final int xTo, final int yTo, final LineStyle style) {
		
	}

	public static void line(final PseudoConsole console, final int xFrom, final int yFrom, final int xTo, final int yTo, final char[] fillers) {
		
	}
	
	public static void box(final PseudoConsole console, final int x, final int y, final int width, final int height) {
		box(console, x, y, width, height, LineStyle.SemiDouble);
	}

	public static void box(final PseudoConsole console, final int x, final int y, final int width, final int height, final LineStyle style) {
		if (style == null) {
			throw new NullPointerException("Box style can't be null"); 
		}
		else {
			switch (style) {
				case Double		: box(console, x, y, width, height, DOUBLE_BOX); break;
				case SemiDouble	: box(console, x, y, width, height, SEMIDOUBLE_BOX); break;
				case Single		: box(console, x, y, width, height, SINGLE_BOX); break;
				default	: throw new UnsupportedOperationException("Box style ["+style+"] is not supported yet"); 
			}
		}
	}
	
	public static void box(final PseudoConsole console, final int x, final int y, final int width, final int height, final char[] fillers) {
		if (console == null) {
			throw new NullPointerException("Console to fill can't be null");
		}
		else if (x < 1 || x > console.getWidth()) {
			throw new IllegalArgumentException("X coordinate ["+x+"] out of range 1.."+console.getWidth());
		}
		else if (y < 1 || y > console.getHeight()) {
			throw new IllegalArgumentException("Y coordinate ["+y+"] out of range 1.."+console.getHeight());
		}
		else if (x+width < 1 || x+width > console.getWidth()) {
			throw new IllegalArgumentException("X coordinate + width ["+(x+width)+"] out of range 1.."+console.getWidth());
		}
		else if (y+height < 1 || y+height > console.getHeight()) {
			throw new IllegalArgumentException("Y coordinate + height ["+(y+height)+"] out of range 1.."+console.getHeight());
		}
		else if (fillers == null || !(fillers.length == 8 || fillers.length == 9)) {
			throw new IllegalArgumentException("Fillers array can't be null and must contain either 8 or 9 elements");
		}
		else {
			for (int xIndex = x; xIndex <= x+width; xIndex++) {
				for (int yIndex = y; yIndex <= y+height; yIndex++) {
					if (xIndex == x && yIndex == y) {
						console.writeContent(xIndex, yIndex, fillers[0]);
					}
					else if (xIndex == x && yIndex == y+height) {
						console.writeContent(xIndex, yIndex, fillers[1]);
					}
					else if (xIndex == x+width && yIndex == y) {
						console.writeContent(xIndex, yIndex, fillers[2]);
					}
					else if (xIndex == x+width && yIndex == y+height) {
						console.writeContent(xIndex, yIndex, fillers[3]);
					}
					else if (xIndex == x) {
						console.writeContent(xIndex, yIndex, fillers[4]);
					}
					else if (xIndex == x+width) {
						console.writeContent(xIndex, yIndex, fillers[5]);
					}
					else if (yIndex == y) {
						console.writeContent(xIndex, yIndex, fillers[6]);
					}
					else if (yIndex == y+height) {
						console.writeContent(xIndex, yIndex, fillers[7]);
					}
					else if (fillers.length == 9) {
						console.writeContent(xIndex, yIndex, fillers[8]);
					}
				}
			}
		}
	}
}
