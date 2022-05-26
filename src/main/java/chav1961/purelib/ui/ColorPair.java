package chav1961.purelib.ui;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.json.interfaces.JsonSerializable;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class ColorPair implements Serializable, JsonSerializable {
	private static final long serialVersionUID = 869188177438346062L;
	
	public static final String	F_FOREGROUND = "foreground"; 
	public static final String	F_BACKGROUND = "background"; 
	
	private Color	foreground, background;
	
	public ColorPair(final Color foreground, final Color background) {
		if (foreground == null) {
			throw new NullPointerException("Foreground color can't be null");
		}
		else if (background == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else {
			this.foreground = foreground;
			this.background = background;
		}
	}
	
	public ColorPair(final String val) throws SyntaxException, IllegalArgumentException {		
		if (val == null || val.isEmpty()) {
			throw new IllegalArgumentException("String value can't be null or empty");
		}
		else {
			final Object[]	content = new Object[2];
			
			CharUtils.extract(val.toCharArray(),0,content,'{',CharUtils.ArgumentType.colorRepresentation,',',CharUtils.ArgumentType.colorRepresentation,'}');
			this.foreground = (Color)content[0];
			this.background = (Color)content[0];
		}
	}

	public Color getForeground() {
		return foreground;
	}

	public Color getBackground() {
		return background;
	}
	
	public void setForeground(final Color foreground) {
		if (foreground == null) {
			throw new NullPointerException("Foreground color can't be null");
		}
		else {
			this.foreground = foreground;
		}
	}

	public void setBackground(final Color background) {
		if (background == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else {
			this.background = background;
		}
	}

	@Override
	public void fromJson(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("JSON parser can't be null");
		}
		else {
			Color	_foreground = Color.BLACK, _background = Color.WHITE;
			boolean	foregroundPresents = false, backgroundPresents = false;
			
			if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
loop:			for (JsonStaxParserLexType item : parser) {
					switch (item) {
						case NAME 			:
							switch (parser.name()) {
								case F_FOREGROUND :
									_foreground = checkAndExtractColor(parser, F_FOREGROUND, foregroundPresents);
									foregroundPresents = true;
									break;
								case F_BACKGROUND :
									_background = checkAndExtractColor(parser, F_BACKGROUND, backgroundPresents);
									backgroundPresents = true;
									break;
								default :
									throw new SyntaxException(parser.row(), parser.col(), "Unsupported field name ["+parser.name()+"], only ["+F_FOREGROUND+","+F_BACKGROUND+"] are valid");
							}
							break;
						case END_OBJECT		:
							if (parser.hasNext()) {
								parser.next();
							}
							break loop;
						case LIST_SPLITTER	:
							break;
						default :
							throw new SyntaxException(parser.row(), parser.col(), "Field name or '}' is missing");
					}
				}
				if (!foregroundPresents || !backgroundPresents) {
					final StringBuilder	sb = new StringBuilder();
					
					if (!foregroundPresents) {
						sb.append(',').append(F_FOREGROUND);
					}
					if (!backgroundPresents) {
						sb.append(',').append(F_BACKGROUND);
					}
					throw new SyntaxException(parser.row(), parser.col(), "Mandatory field(s) ["+sb.substring(1)+"] are missing");
				}
				else {
					foreground = _foreground;
					background = _background;
				}
			}
		}
	}

	@Override
	public void toJson(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("JSON printer can't be null"); 
		}
		else {
			printer.startObject().name(F_FOREGROUND).value(PureLibSettings.nameByColor(foreground, "black"))
				.splitter().name(F_BACKGROUND).value(PureLibSettings.nameByColor(background, "black")).endObject();
		}
	}
	
	@Override
	public String toString() {
		return "ColorPair [foreground=" + foreground + ", background=" + background + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((background == null) ? 0 : background.hashCode());
		result = prime * result + ((foreground == null) ? 0 : foreground.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ColorPair other = (ColorPair) obj;
		if (background == null) {
			if (other.background != null) return false;
		} else if (!background.equals(other.background)) return false;
		if (foreground == null) {
			if (other.foreground != null) return false;
		} else if (!foreground.equals(other.foreground)) return false;
		return true;
	}

	private static Color checkAndExtractColor(final JsonStaxParser parser, final String field, final boolean thisPresents) throws SyntaxException, IOException {
		if (thisPresents) {
			throw new SyntaxException(parser.row(), parser.col(), "Duplicate field name ["+field+"]");
		}
		else if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER) {
			switch (parser.next()) {
				case STRING_VALUE 	: return PureLibSettings.colorByName(parser.stringValue(), Color.BLACK);
				default : throw new SyntaxException(parser.row(), parser.col(), "Color value is missing");
			}
		}
		else {
			throw new SyntaxException(parser.row(), parser.col(), "Missing ':'");
		}
	}
}
