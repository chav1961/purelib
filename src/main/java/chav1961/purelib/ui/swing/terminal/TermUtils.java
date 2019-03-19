package chav1961.purelib.ui.swing.terminal;

import java.awt.Color;
import java.awt.Rectangle;

import chav1961.purelib.ui.ColorPair;

public class TermUtils {
//	U+2500 - U+257F - unicode borders
//	U+2580 - U+259F - unicode filling
	
	public static final char	SINGLE_HORIZONTAL = '\u2500';
	public static final char	SINGLE_VERTICAL = '\u2502';
	public static final char	SINGLE_TOPLEFT = '\u250C';
	public static final char	SINGLE_TOPRIGHT = '\u2510';
	public static final char	SINGLE_BOTTOMLEFT = '\u2514';
	public static final char	SINGLE_BOTTOMRIGHT = '\u2518';
	public static final char	SINGLE_VERTICAL_RIGHT = '\u251C';
	public static final char	SINGLE_VERTICAL_LEFT = '\u2524';
	public static final char	SINGLE_HORIZONTAL_BOTTOM = '\u252C';
	public static final char	SINGLE_HORIZONTAL_TOP = '\u2534';
	public static final char	SINGLE_CROSS = '\u253C';
	
	public static final char	DOUBLE_HORIZONTAL = '\u2550';
	public static final char	DOUBLE_VERTICAL = '\u2551';
	public static final char	DOUBLE_TOPLEFT = '\u2554';
	public static final char	DOUBLE_TOPRIGHT = '\u2557';
	public static final char	DOUBLE_BOTTOMLEFT = '\u255A';
	public static final char	DOUBLE_BOTTOMRIGHT = '\u255D';
	public static final char	DOUBLE_VERTICAL_RIGHT = '\u2560';
	public static final char	DOUBLE_VERTICAL_LEFT = '\u2563';
	public static final char	DOUBLE_HORIZONTAL_BOTTOM = '\u2566';
	public static final char	DOUBLE_HORIZONTAL_TOP = '\u2569';
	public static final char	DOUBLE_CROSS = '\u256C';

	public static final char	SINGLE_TOP_DOUBLE_LEFT = '\u2553';
	public static final char	SINGLE_BOTTOM_DOUBLE_LEFT = '\u2559';
	public static final char	SINGLE_TOP_DOUBLE_RIGHT = '\u2556';
	public static final char	SINGLE_BOTTOM_DOUBLE_RIGHT = '\u255C';
	public static final char	DOUBLE_TOP_SINGLE_LEFT = '\u2552';
	public static final char	DOUBLE_BOTTOM_SINGLE_LEFT = '\u2558';
	public static final char	DOUBLE_TOP_SINGLE_RIGHT = '\u2555';
	public static final char	DOUBLE_BOTTOM_SINGLE_RIGHT = '\u255B';
	
	private static final char[]	SINGLE_BOX= {SINGLE_TOPLEFT, SINGLE_BOTTOMLEFT, SINGLE_TOPRIGHT, SINGLE_BOTTOMRIGHT, SINGLE_VERTICAL, SINGLE_VERTICAL, SINGLE_HORIZONTAL, SINGLE_HORIZONTAL};
	private static final char[]	DOUBLE_BOX= {DOUBLE_TOPLEFT, DOUBLE_BOTTOMLEFT, DOUBLE_TOPRIGHT, DOUBLE_BOTTOMRIGHT, DOUBLE_VERTICAL, DOUBLE_VERTICAL, DOUBLE_HORIZONTAL, DOUBLE_HORIZONTAL};
	private static final char[]	SEMIDOUBLE_BOX= {SINGLE_TOPLEFT, DOUBLE_BOTTOM_SINGLE_LEFT, SINGLE_TOP_DOUBLE_RIGHT, DOUBLE_BOTTOMRIGHT, SINGLE_VERTICAL, DOUBLE_VERTICAL, SINGLE_HORIZONTAL, DOUBLE_HORIZONTAL};
	private static final char[]	SINGLE_VERTICAL_LINE = {SINGLE_VERTICAL, SINGLE_VERTICAL, SINGLE_VERTICAL};
	private static final char[]	SINGLE_HORIZONTAL_LINE = {SINGLE_HORIZONTAL, SINGLE_HORIZONTAL, SINGLE_HORIZONTAL};
	private static final char[]	DOUBLE_VERTICAL_LINE = {DOUBLE_VERTICAL, DOUBLE_VERTICAL, DOUBLE_VERTICAL};
	private static final char[]	DOUBLE_HORIZONTAL_LINE = {DOUBLE_HORIZONTAL, DOUBLE_HORIZONTAL, DOUBLE_HORIZONTAL};
	
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
			console.writeAttribute(new Rectangle(1,1,console.getConsoleWidth(),console.getConsoleHeight()),new ColorPair(color,bkGnd));
			console.writeContent(new Rectangle(1,1,console.getConsoleWidth(),console.getConsoleHeight()),' ');
		}
	}
	
	public static void line(final PseudoConsole console, final int xFrom, final int yFrom, final int xTo, final int yTo) {
		line(console,xFrom,yFrom,xTo,yTo,LineStyle.Single);
	}

	public static void line(final PseudoConsole console, final int xFrom, final int yFrom, final int xTo, final int yTo, final LineStyle style) {
		if (style == null) {
			throw new NullPointerException("Line style can't be null");
		}
		else {
			switch (style) {
				case Double	:
					if (xFrom == xTo) {
						line(console,xFrom,yFrom,xTo,yTo,DOUBLE_VERTICAL_LINE);
					}
					else {
						line(console,xFrom,yFrom,xTo,yTo,DOUBLE_HORIZONTAL_LINE);
					}
					break;
				case Single	:
					if (xFrom == xTo) {
						line(console,xFrom,yFrom,xTo,yTo,SINGLE_VERTICAL_LINE);
					}
					else {
						line(console,xFrom,yFrom,xTo,yTo,SINGLE_HORIZONTAL_LINE);
					}
					break;
				default : throw new IllegalArgumentException("Line type ["+style+"] is not supported for ordinal line");
			}
		}
	}

	public static void line(final PseudoConsole console, final int xFrom, final int yFrom, final int xTo, final int yTo, final char[] fillers) {
		if (console == null) {
			throw new NullPointerException("Console to write line to can't be null"); 
		}
		else if (xFrom < 1 || xFrom > console.getConsoleWidth()) {
			throw new IllegalArgumentException("Start x coordinate ["+xFrom+"] out of range 1.."+console.getConsoleWidth()); 
		}
		else if (xTo < 1 || xTo > console.getConsoleWidth()) {
			throw new IllegalArgumentException("End x coordinate ["+xTo+"] out of range 1.."+console.getConsoleWidth()); 
		}
		else if (yFrom < 1 || yFrom > console.getConsoleHeight()) {
			throw new IllegalArgumentException("Start y coordinate ["+yFrom+"] out of range 1.."+console.getConsoleHeight()); 
		}
		else if (yTo < 1 || yTo > console.getConsoleHeight()) {
			throw new IllegalArgumentException("End y coordinate ["+yTo+"] out of range 1.."+console.getConsoleHeight()); 
		}
		else if (fillers == null || fillers.length != 3) {
			throw new IllegalArgumentException("Fillers can't be null and must contain exactly 3 chars"); 
		}
		else if (xFrom != xTo && yFrom != yTo) {
			throw new IllegalArgumentException("Points [,] and [,] describe diagonal line. Only vertical and horizontal lines are available"); 
		}
		else if (xFrom > xTo) {
			line(console,xTo,yFrom,xFrom,yTo,fillers);
		}
		else if (yFrom > yTo) {
			line(console,xFrom,yTo,xTo,yFrom,fillers);
		}
		else if (yFrom == yTo) {
			for (int index = xFrom; index <= xTo; index++) {
				if (index == xFrom) {
					console.writeContent(index, yFrom, fillers[0]);
				}
				else if (index == xTo) {
					console.writeContent(index, yFrom, fillers[2]);
				}
				else {
					console.writeContent(index, yFrom, fillers[1]);
				}
			}
		}
		else {
			for (int index = yFrom; index <= yTo; index++) {
				if (index == yFrom) {
					console.writeContent(xFrom, index, fillers[0]);
				}
				else if (index == yTo) {
					console.writeContent(xFrom, index, fillers[2]);
				}
				else {
					console.writeContent(xFrom, index, fillers[1]);
				}
			}
		}
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
		else if (x < 1 || x > console.getConsoleWidth()) {
			throw new IllegalArgumentException("X coordinate ["+x+"] out of range 1.."+console.getConsoleWidth());
		}
		else if (y < 1 || y > console.getConsoleHeight()) {
			throw new IllegalArgumentException("Y coordinate ["+y+"] out of range 1.."+console.getConsoleHeight());
		}
		else if (x+width < 1 || x+width-1 > console.getConsoleWidth()) {
			throw new IllegalArgumentException("X coordinate + width ["+(x+width)+"] out of range 1.."+console.getConsoleWidth());
		}
		else if (y+height < 1 || y+height-1 > console.getConsoleHeight()) {
			throw new IllegalArgumentException("Y coordinate + height ["+(y+height)+"] out of range 1.."+console.getConsoleHeight());
		}
		else if (fillers == null || !(fillers.length == 8 || fillers.length == 9)) {
			throw new IllegalArgumentException("Fillers array can't be null and must contain either 8 or 9 elements");
		}
		else {
			for (int xIndex = x; xIndex < x+width; xIndex++) {
				for (int yIndex = y; yIndex < y+height; yIndex++) {
					if (xIndex == x && yIndex == y) {
						console.writeContent(xIndex, yIndex, fillers[0]);
					}
					else if (xIndex == x && yIndex == y+height-1) {
						console.writeContent(xIndex, yIndex, fillers[1]);
					}
					else if (xIndex == x+width-1 && yIndex == y) {
						console.writeContent(xIndex, yIndex, fillers[2]);
					}
					else if (xIndex == x+width-1 && yIndex == y+height-1) {
						console.writeContent(xIndex, yIndex, fillers[3]);
					}
					else if (xIndex == x) {
						console.writeContent(xIndex, yIndex, fillers[4]);
					}
					else if (xIndex == x+width-1) {
						console.writeContent(xIndex, yIndex, fillers[5]);
					}
					else if (yIndex == y) {
						console.writeContent(xIndex, yIndex, fillers[6]);
					}
					else if (yIndex == y+height-1) {
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
