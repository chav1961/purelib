package chav1961.purelib.ui.swing.terminal;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.interfaces.CharStreamPrinter;

public class Term extends PseudoConsole implements CharStreamPrinter<Term> {
	private static final long 		serialVersionUID = 4321066125437646937L;
	
	private static final long		BLINK_INTERVAL = 500;
	private static final int		TAB_SIZE = 8;
	private static final char[]		CRLF = new char[]{'\r','\n'};
	private static final char[]		TRUE = "true".toCharArray();
	private static final char[]		FALSE = "false".toCharArray();
	private static final String		NULL = "null";
	private static final int		INITIAL_SIZE = 32;
	
	private final boolean			emulateBell = false;
	private final TimerTask			tt = new TimerTask(){
										@Override
										public void run() {
											blink(); 
										}
									};
	private final StringBuilder		sb = new StringBuilder();
	private final List<int[]>		stack = new ArrayList<>();
	private final Color[]			colors = new Color[2];
	private char[]					buffer = new char[INITIAL_SIZE]; 
							
	private int						x = 1, y = 1, escaping = 0;
	private boolean					on = true, blinkNow = false;

	public Term(){
		this(80,25);
	}
	
	public Term(final int width, final int height){
		this(width,height,Color.GREEN,Color.BLACK);
	}

	public Term(final int width, final int height, final Color foreground, final Color background){
		super(width,height);
		if (foreground == null) {
			throw new NullPointerException("Foreground color can't be null"); 
		}
		else if (background == null) {
			throw new NullPointerException("Background color can't be null"); 
		}
		else {
			PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt,BLINK_INTERVAL,BLINK_INTERVAL);
			colors[0] = foreground;
			colors[1] = background;
		}
	}
	
	
	@Override
	public void flush() throws IOException {
	}
	
	@Override
	public void close() throws IOException {
		tt.cancel();
		stack.clear();
	}
	
	@Override
	public Term println() {
		return print(CRLF);
	}
	
	@Override
	public Term print(final char data) {
		internalPrintChar(data);
		return this;
	}

	@Override
	public Term println(final char data) {
		return print(data).println();
	}
	
	@Override
	public Term print(final byte data) {
		return print((long)data);
	}

	@Override
	public Term println(final byte data) {
		return print(data).println();
	}
	
	@Override
	public Term print(final short data) {
		return print((long)data);
	}

	@Override
	public Term println(final short data) {
		return print(data).println();
	}
	
	@Override
	public Term print(final int data) {
		return print((long)data);
	}

	@Override
	public Term println(final int data) {
		return print(data).println();
	}
	
	@Override
	public Term print(final long data) {
		final int	len = CharUtils.printLong(buffer, 0, data, true);
		
		if (len < 0) {
			buffer = Arrays.copyOf(buffer,2*buffer.length);
			return print(data);
		}
		else {
			return print(buffer,0,len);
		}
	}

	@Override
	public Term println(final long data) {
		return print(data).println();
	}
	
	@Override
	public Term print(final float data) {
		return print((double)data);
	}

	@Override
	public Term println(final float data) {
		return print(data).println();
	}
	
	@Override
	public Term print(final double data) {
		final int	len = CharUtils.printDouble(buffer, 0, data, true);
		
		if (len < 0) {
			buffer = Arrays.copyOf(buffer,2*buffer.length);
			return print(data);
		}
		else {
			return print(buffer,0,len);
		}
	}

	@Override
	public Term println(final double data) {
		return print(data).println();
	}
	
	@Override
	public Term print(final boolean data) {
		return print(data ? TRUE : FALSE);
	}

	@Override
	public Term println(final boolean data) {
		return print(data).println();
	}
	
	@Override
	public Term print(final String data) {
		return print((data == null ? NULL : data).toCharArray());
	}

	@Override
	public Term println(final String data) {
		return print(data).println();
	}
	
	@Override
	public Term print(final Object data) {
		return print(data == null ? NULL : data.toString()).println();
	}

	@Override
	public Term println(final Object data) {
		return print(data).println();
	}
	
	@Override
	public Term print(final char[] data) {
		if (data == null) {
			return print(NULL);
		}
		else {
			return print(data,0,data.length);
		}
	}

	@Override
	public Term println(final char[] data) {
		return print(data).println();
	}
	
	@Override
	public Term print(final char[] data, final int from, final int len) {
		if (data == null) {
			throw new IllegalArgumentException("Data can't be null");
		}
		else if (from < 0 || from > data.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(data.length));
		}
		else if (len < 0 || len > data.length) {
			throw new IllegalArgumentException("Length ["+from+"] out of range 0.."+(data.length-1));
		}
		else if (from+len < 0 || from+len > data.length) {
			throw new IllegalArgumentException("From position + length ["+(from+len)+"] out of range 0.."+(data.length-1));
		}
		else {
			for (int index = from; index < from+len; index++) {
				print(data[index]);
			}
			return this;
		}
	}

	@Override
	public Term println(final char[] data, final int from, final int len) {
		return print(data,from,len).println();
	}

	@Override
	public Term print(final String data, final int from, final int len) throws PrintingException, StringIndexOutOfBoundsException {
		if (data == null) {
			return print(NULL);
		}
		else {
			return print(data.substring(from,from+len));
		}
	}

	@Override
	public Term println(final String data, final int from, final int len) throws PrintingException, StringIndexOutOfBoundsException {
		return print(data,from,len).println();
	}

	public Color getForeground() {
		return colors[0];
	}

	public Color getBackground() {
		return colors[1];
	}
	
	public int getCursorX() {
		return x;
	}

	public int getCursorY() {
		return y;
	}

	public Term setCursor(final int x, final int y) {
		if (x < 1) {
			this.x = 1;
		}
		else if (x > getConsoleWidth()) {
			this.x = getConsoleWidth();
		}
		else {
			this.x = x;
		}
		if (y < 1) {
			this.y = 1;
		}
		else if (y > getConsoleHeight()) {
			this.y = getConsoleHeight();
		}
		else {
			this.y = y;
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
		TermUtils.clear(this,colors[0],colors[1]);
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
				if (getCursorY() == getConsoleHeight()) {
					scrollUp(getForeground(),getBackground());
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
						if (++x > getConsoleWidth()) {
							x = 1;
							if (++y > getConsoleHeight()) {
								scrollUp(getForeground(),getBackground());
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
							sb.setLength(0);
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
						fill(getConsoleWidth()-getCursorX()+1 + (getConsoleHeight()-getCursorY())*getConsoleWidth());
						break;
					case '1' :
						final int	oldCursorX = getCursorX(), oldCursorY = getCursorY();
						
						setCursor(1,1);
						fill((oldCursorY-1)*getConsoleHeight() + oldCursorX);	
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
						fill(getConsoleWidth()-getCursorX()+1);	
						break;
					case '1' :
						final int	oldCursor = getCursorX(); 
						
						setCursor(1,getCursorY());
						fill(oldCursor);	
						break;
					case '2' :
						setCursor(1,getCursorY());
						fill(getConsoleWidth());	
						break;
				}
				popCursor();
				break;
			case 'S' :
				for (int index = 0, maxIndex = Integer.valueOf(nvl(parameters,"1")); index < maxIndex; index++) {
					scrollDown(getForeground(),getBackground());
				}
				break;
			case 'T' :
				for (int index = 0, maxIndex = Integer.valueOf(nvl(parameters,"1")); index < maxIndex; index++) {
					scrollUp(getForeground(),getBackground());
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
		for (int index = 0; index < length-1; index++) {
			internalPrintChar(' ');
		}
		writeContent(getCursorX(),getCursorY(),' ');
	}
}
