package chav1961.purelib.ui.swing.terminal;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class Term extends PseudoConsole implements Closeable {
	private static final long 		serialVersionUID = 4321066125437646937L;
	
	private static final long		BLINK_INTERVAL = 500;
	private static final int		TAB_SIZE = 8;
	private static final char[]		CRLF = new char[]{'\r','\n'};
	private static final String		NULL = "null";
	
	private final int				width = 80, height = 25;
	private final boolean			emulateBell = false;
	private final Timer				t = new Timer(true);
	private final TimerTask			tt = new TimerTask(){
										@Override
										public void run() {
											blink();
										}
									};
	private final StringBuilder		sb = new StringBuilder();
	private final List<int[]>		stack = new ArrayList<>();
							
	private int						x = 1, y = 1, escaping = 0;
	private boolean					on = false, blinkNow = false;
	private Color[]					colors = new Color[]{Color.GREEN,Color.BLACK};
	
	public Term(){
		super(80,25);
		t.schedule(tt,BLINK_INTERVAL,BLINK_INTERVAL);
	}

	@Override
	public void close() throws IOException {
		tt.cancel();
		t.cancel();
	}
	
	public Term println() {
		return print(CRLF);
	}
	
	public Term print(final char data) {
		internalPrintChar(data);
		return this;
	}

	public Term println(final char data) {
		return print(data).println();
	}
	
	public Term print(final byte data) {
		return print(Byte.valueOf(data).toString());
	}

	public Term println(final byte data) {
		return print(data).println();
	}
	
	public Term print(final short data) {
		return print(Short.valueOf(data).toString());
	}

	public Term println(final short data) {
		return print(data).println();
	}
	
	public Term print(final int data) {
		return print(Integer.valueOf(data).toString());
	}

	public Term println(final int data) {
		return print(data).println();
	}
	
	public Term print(final long data) {
		return print(Long.valueOf(data).toString());
	}

	public Term println(final long data) {
		return print(data).println();
	}
	
	public Term print(final float data) {
		return print(Float.valueOf(data).toString());
	}

	public Term println(final float data) {
		return print(data).println();
	}
	
	public Term print(final double data) {
		return print(Double.valueOf(data).toString());
	}

	public Term println(final double data) {
		return print(data).println();
	}
	
	public Term print(final boolean data) {
		return print(Boolean.valueOf(data).toString());
	}

	public Term println(final boolean data) {
		return print(data).println();
	}
	
	public Term print(final String data) {
		return print((data == null ? NULL : data).toCharArray());
	}

	public Term println(final String data) {
		return print(data).println();
	}
	
	public Term print(final Object data) {
		return print(data == null ? NULL : data.toString()).println();
	}

	public Term println(final Object data) {
		return print(data).println();
	}
	
	public Term print(final char[] data) {
		if (data == null) {
			throw new IllegalArgumentException("Data can't be null");
		}
		else {
			return print(data,0,data.length);
		}
	}

	public Term println(final char[] data) {
		return print(data).println();
	}
	
	public Term print(final char[] data, final int from, final int len) {
		if (data == null) {
			throw new IllegalArgumentException("Data can't be null");
		}
		else if (from < 0 || from >= data.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(data.length-1));
		}
		else if (len < 0 || len >= data.length) {
			throw new IllegalArgumentException("Length ["+from+"] out of range 0.."+(data.length-1));
		}
		else if (from+len < 0 || from+len >= data.length) {
			throw new IllegalArgumentException("From position + length ["+(from+len)+"] out of range 0.."+(data.length-1));
		}
		else {
			for (int index = from; index < from+len; index++) {
				print(data[index]);
			}
			return this;
		}
	}

	public Term println(final char[] data, final int from, final int len) {
		return print(data,from,len).println();
	}
	
	public int getCursorX() {
		return x;
	}

	public int getCursorY() {
		return y;
	}

	public Term setCursor(int x, int y) {
		if (x < 1) {
			x = 1;
		}
		else if (x > width) {
			x = width;
		}
		if (y < 1) {
			y = 1;
		}
		else if (y > height) {
			y = height;
		}
		return this;
	}
	
	public Term cursorOn() {
		on = true;
		return this;
	}

	public boolean isCursorOn() {
		return on;
	}
	
	public Term cursorOff() {
		on = false;
		return this;
	}
	
	public Term clear() {
		for (int index = 0; index < height; index++) {
			println();
		}
		setCursor(1,1);
		return this;
	}

	private void blink() {
		blinkNow = !blinkNow;
	}
	
	private void internalPrintChar(final char symbol) {
		switch (symbol) {
			case '\r'	: setCursor(1,getCursorY()); break;
			case '\t'	: setCursor(((getCursorX()+TAB_SIZE-1)/TAB_SIZE)*TAB_SIZE,getCursorY()); break;
			case '\f'	: clear(); break;
			case '\b'	: bell(); break;
			case '\n'	:
				if (getCursorY() == height) {
					scroll(true);
				}
				else {
					setCursor(getCursorX(),getCursorY()+1);
				}
				break;
			case 0x1F	:
				escaping = 1;
				sb.setLength(0);
				break;
			default 	:
				switch (escaping) {
					case 0 :
						writeAttribute(getCursorX(),getCursorY(),colors);
						writeContent(getCursorX(),getCursorY(),symbol);
						if (++x > width) {
							x = 1;
							if (++y > height) {
								scroll(true);
								y--;
							}
						}
						break;
					case 1 :
						if (symbol == '[') {
							escaping = 2;
						}
						else {
							escaping = 0;
							writeAttribute(getCursorX(),getCursorY(),colors);
							writeContent(getCursorX(),getCursorY(),symbol);
						}
						break;
					case 2 :
						if (Character.isJavaIdentifierStart(symbol)) {
							escaping = 0;
							processEsc(symbol,sb.toString());
						}
						else {
							sb.append(symbol);
						}
				}
		}
	}

	private void processEsc(final char symbol, final String parameters) {
		switch (symbol) {
			case 'A' : setCursor(getCursorX(),getCursorY()-Integer.valueOf(parameters)); break;
			case 'B' : setCursor(getCursorX(),getCursorY()+Integer.valueOf(parameters)); break;
			case 'C' : setCursor(getCursorX()-Integer.valueOf(parameters),getCursorY()); break;
			case 'D' : setCursor(getCursorX()+Integer.valueOf(parameters),getCursorY()); break;
			case 'E' : setCursor(1,getCursorY()+Integer.valueOf(parameters)); break;
			case 'F' : setCursor(1,getCursorY()-Integer.valueOf(parameters)); break;
			case 'G' : setCursor(Integer.valueOf(parameters),getCursorY()); break;
			case 'f' : case 'H' :
				if (parameters.contains(";")) {
					final String[]	splitted = parameters.split("\\;");
							
					setCursor(Integer.valueOf(nvl(splitted[0],"1")),Integer.valueOf(nvl(splitted[1],"1")));
				}
				else {
					 setCursor(Integer.valueOf(nvl(parameters,"1")),getCursorY());
				}
				break;
			case 'J' :
				pushCursor();
				switch (nvl(parameters,"0").charAt(0)) {
					case '0' :
						fill(width*height - getCursorY()*height - getCursorX());	
						break;
					case '1' :
						setCursor(1,1);
						fill(getCursorY()*height + getCursorX());	
						break;
					case '2' :
						clear();
						break;
				}
				popCursor();
				break;
			case 'K' :
				pushCursor();
				switch (nvl(parameters,"0").charAt(0)) {
					case '0' :
						fill(width-getCursorX());	
						break;
					case '1' :
						setCursor(1,getCursorY());
						fill(getCursorX());	
						break;
					case '2' :
						setCursor(1,getCursorY());
						fill(width);	
						break;
				}
				popCursor();
				break;
			case 'S' :
				for (int index = 0, maxIndex = Integer.valueOf(nvl(parameters,"1")); index < maxIndex; index++) {
					scroll(true);
				}
				break;
			case 'T' :
				for (int index = 0, maxIndex = Integer.valueOf(nvl(parameters,"1")); index < maxIndex; index++) {
					scroll(false);
				}
				break;
			case 'm' :
				for (String item : nvl(parameters,"0").split("\\;")) {
					if (item != null && !item.isEmpty()) {
						processSGR(Integer.valueOf(item));
					}
				}
				break;
			case 's' : pushCursor(); break;
			case 'u' : popCursor(); break;
			case 'h' : cursorOn(); break;
			case 'l' : cursorOff(); break;
			case 'n' :
				throw new UnsupportedOperationException("Esc[6n sequence is not supported!");
			default :
				break;
		}
	}

	private void processSGR(int code) {
		switch (code) {
			case 30 : colors[0] = Color.BLACK; break;
			case 31 : colors[0] = Color.RED; break;
			case 32 : colors[0] = Color.GREEN; break;
			case 33 : colors[0] = Color.YELLOW; break;
			case 34 : colors[0] = Color.BLUE; break;
			case 35 : colors[0] = Color.MAGENTA; break;
			case 36 : colors[0] = Color.CYAN; break;
			case 37 : colors[0] = Color.WHITE; break;
			case 40 : colors[1] = Color.BLACK; break;
			case 41 : colors[1] = Color.RED; break;
			case 42 : colors[1] = Color.GREEN; break;
			case 43 : colors[1] = Color.YELLOW; break;
			case 44 : colors[1] = Color.BLUE; break;
			case 45 : colors[1] = Color.MAGENTA; break;
			case 46 : colors[1] = Color.CYAN; break;
			case 47 : colors[1] = Color.WHITE; break;
		}
	}

	private void bell() {
		if (emulateBell) {
			
		}
	}
	
	private void scroll(boolean up) {
		Rectangle	rect;
		Color[][][]	attrs;
		char[]		content;
		
		if (up) {
			attrs = readAttibute(rect = new Rectangle(1,2,width,height-1));
			content = readContent(rect);
//			writeAttribute(rect = new Rectangle(1,1,width,height-1),attrs);
			writeContent(rect = new Rectangle(1,1,width,height-1),content);
			pushCursor();
			setCursor(1,height);
			fill(width);
			popCursor();
		}
		else {
			attrs = readAttibute(rect = new Rectangle(1,1,width,height-1));
			content = readContent(rect);
//			writeAttribute(rect = new Rectangle(1,1,width,height-1),attrs);
			writeContent(rect = new Rectangle(1,2,width,height-1),content);
			pushCursor();
			setCursor(1,1);
			fill(width);
			popCursor();
		}
	}

	
	private String nvl(final String source, final String defaulValue) {
		return source == null || source.isEmpty() ? defaulValue : source;
	}

	private void pushCursor() {
		stack.add(0,new int[]{getCursorX(),getCursorY()});
	}

	private void popCursor() {
		if (stack.size() > 0) {
			final int[]	data = stack.remove(0);
			
			setCursor(data[0],data[1]);
		}
	}
	
	private void fill(int length) {
		for (int index = 0; index < length; index++) {
			internalPrintChar(' ');
		}
	}
}
