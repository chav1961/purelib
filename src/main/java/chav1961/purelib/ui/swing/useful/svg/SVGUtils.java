package chav1961.purelib.ui.swing.useful.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Map;

import chav1961.purelib.basic.CSSUtils;
import chav1961.purelib.basic.CSSUtils.Distance;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.ArgumentType;
import chav1961.purelib.basic.CharUtils.SubstitutionSource;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.ConvertorInterface;
import chav1961.purelib.basic.interfaces.OnlineBooleanGetter;
import chav1961.purelib.basic.interfaces.OnlineCharGetter;
import chav1961.purelib.basic.interfaces.OnlineDoubleGetter;
import chav1961.purelib.basic.interfaces.OnlineFloatGetter;
import chav1961.purelib.basic.interfaces.OnlineGetter;
import chav1961.purelib.basic.interfaces.OnlineIntGetter;
import chav1961.purelib.basic.interfaces.OnlineLongGetter;
import chav1961.purelib.basic.interfaces.OnlineObjectGetter;
import chav1961.purelib.basic.interfaces.OnlineStringGetter;

class SVGUtils {
	public static Point2D[] extractPoints(final String source) throws SyntaxException {
		if (source == null || source.isEmpty()) {
			throw new IllegalArgumentException("Source string can't be null or empty");
		}
		else {
			final char[]	content = source.toCharArray();
			final float[]	number = new float[1];
			float			x, y;
			int 			pointCount = 0, from = 0;
			
			for (int index = 0, maxIndex = content.length; index < maxIndex; index++) {
				if (content[index] >= '0' && content[index] <= '9') {
					while (index < maxIndex && (content[index] >= '0' && content[index] <= '9' || content[index] == '.')) {
						index++;
					}
					pointCount++;
				}
			}			
			
			if (pointCount == 0) {
				throw new SyntaxException(0,0,"No any numbers in source string ["+source+"]");
			}
			else if (pointCount % 2 == 1) {
				throw new SyntaxException(0,0,"Odd amount of numbers in the source string ["+source+"]");
			}
			else {
				pointCount = (pointCount + 1) / 2;
				final Point2D[]	result = new Point2D[pointCount];
				
				for (int index = 0; index < pointCount; index++) {
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,false),number,true);
					x = number[0];
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,false),number,true);
					y = number[0];
					result[index] = new Point2D.Float(x,y); 
				}
				return result;
			}
		}
	}

	public static GeneralPath extractCommands(final String source) throws SyntaxException {
		if (source == null || source.isEmpty()) {
			throw new IllegalArgumentException("Source string can't be null or empty");
		}
		else {
			final GeneralPath	result = new GeneralPath();
			final char[]		content = new char[source.length()+1];
			final float[]		number = new float[1];
			final int[]			integer = new int[1];
			double				x = 0, y = 0, xOld = 0, yOld = 0, x1, y1, x2, y2, rx, ry, rotation;
			double				controleX = 0, controleY = 0;
			int 				from = 0, large, sweep, count;
			boolean 			wereSomeCommands = false;
	
			source.getChars(0,content.length-1,content,0);
			content[content.length-1] = '\n';	//  to exclude array bounds check
			
loop:		for (;;) {
				from = CharUtils.skipBlank(content,from,true);
				switch (content[from]) {
					case '\n' :
						break loop;
					case 'm' :
						count = 0;
						x = xOld;
						y = yOld;
						from++;
						do {from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y += number[0];
							if (count++ == 0) {
								result.moveTo(x,y);
							}
							else {
								result.lineTo(x,y);
							}
							from = CharUtils.skipBlank(content,from,true);
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					case 'M' :
						count = 0;
						from++;
						do {from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y = number[0];
							if (count++ == 0) {
								result.moveTo(x,y);
							}
							else {
								result.lineTo(x,y);
							}
							from = CharUtils.skipBlank(content,from,true);
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					case 'l' :
						x = xOld;
						y = yOld;
						from++;
						do {from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y += number[0];
							result.lineTo(x,y);
							from = CharUtils.skipBlank(content,from,true);
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					case 'L' :
						from++;
						do {from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y = number[0];
							result.lineTo(x,y);
							from = CharUtils.skipBlank(content,from,true);
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					case 'h' :
						x = xOld;
						y = yOld;
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
						x += number[0];
						result.lineTo(x, y);
						break;
					case 'H' :
						y = yOld;
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
						x = number[0];
						result.lineTo(x, y);
						break;
					case 'v' :
						y = yOld;
						x = xOld;
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
						y += number[0];
						result.lineTo(x, y);
						break;
					case 'V' :
						x = xOld;
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
						y = number[0];
						result.lineTo(x, y);
						break;
					case 'z' : case 'Z' :
						from++;
						result.closePath();
						x = result.getCurrentPoint().getX();
						y = result.getCurrentPoint().getY();
						break loop;
					case 'a' :
						x = xOld;
						y = yOld;
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
						rx = number[0];
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
						ry = number[0];
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
						rotation = number[0];
						from = CharUtils.parseSignedInt(content,CharUtils.skipBlank(content,from,true),integer,true);
						large = integer[0];
						from = CharUtils.parseSignedInt(content,CharUtils.skipBlank(content,from,true),integer,true);
						sweep = integer[0];
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
						x += number[0];
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
						y += number[0];
						result.append(SVGUtils.computeArc(xOld,yOld,rx,ry,rotation,large != 0,sweep != 0,x,y),true);
						break;
					case 'A' :
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
						rx = number[0];
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
						ry = number[0];
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
						rotation = number[0];
						from = CharUtils.parseSignedInt(content,CharUtils.skipBlank(content,from,true),integer,true);
						large = integer[0];
						from = CharUtils.parseSignedInt(content,CharUtils.skipBlank(content,from,true),integer,true);
						sweep = integer[0];
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
						x = number[0];
						from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
						y = number[0];
						result.append(SVGUtils.computeArc(xOld,yOld,rx,ry,rotation,large != 0,sweep != 0,x,y),true);
						break;
					case 'c' :
						from++;
						do {
							x = x1 = x2 = xOld;
							y = y1 = y2 = yOld;
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x1 += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y1 += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							controleX = x2 += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							controleY = y2 += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y += number[0];
							result.curveTo(x1, y1, x2, y2, x, y);
							from = CharUtils.skipBlank(content,from,true);
							xOld = x;
							yOld = y;
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					case 'C' :
						from++;
						do {
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x1 = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y1 = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							controleX = x2 = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							controleY = y2 = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y = number[0];
							result.curveTo(x1, y1, x2, y2, x, y);
							from = CharUtils.skipBlank(content,from,true);
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					case 'q' :
						from++;
						do {
							x = x1 = xOld;
							y = y1 = yOld;
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x1 += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y1 += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y += number[0];
							result.quadTo(x1, y1, x, y);
							from = CharUtils.skipBlank(content,from,true);
							xOld = x;
							yOld = y;
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					case 'Q' :
						from++;
						do {
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x1 = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y1 = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y = number[0];
							result.quadTo(x1, y1, x, y);
							from = CharUtils.skipBlank(content,from,true);
							xOld = x;
							yOld = y;
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					case 's' :
						from++;
						do {
							x = x2 = xOld;
							y = y2 = yOld;
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x2 += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y2 += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y += number[0];
							result.curveTo(xOld + (xOld - controleX), yOld + (yOld - controleY), x2, y2, x, y);
							from = CharUtils.skipBlank(content,from,true);
							controleX = x2;
							controleY = y2;
							xOld = x;
							yOld = y;
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					case 'S' :
						from++;
						do {
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x2 = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y2 = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y = number[0];
							result.curveTo(xOld + (xOld - controleX), yOld + (yOld - controleY), x2, y2, x, y);
							from = CharUtils.skipBlank(content,from,true);
							controleX = x2;
							controleY = y2;
							xOld = x;
							yOld = y;
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					case 't' :
						from++;
						do {
							x = xOld;
							y = yOld;
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x += number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y += number[0];
							result.quadTo(xOld + (xOld - controleX), yOld + (yOld - controleY), x, y);
							from = CharUtils.skipBlank(content,from,true);
							controleX = x;
							controleY = y;
							xOld = x;
							yOld = y;
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					case 'T' :
						from++;
						do {
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							x = number[0];
							from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
							y = number[0];
							result.quadTo(xOld + (xOld - controleX), yOld + (yOld - controleY), x, y);
							from = CharUtils.skipBlank(content,from,true);
							controleX = x;
							controleY = y;
							xOld = x;
							yOld = y;
						} while (content[from] >= '0' && content[from] <= '9' || content[from] == '-');
						break;
					default :
						throw new SyntaxException(0,from,"Unknown path command ["+content[from]+"] in ["+SyntaxException.extractFragment(source,0,from,10)+"]"); 					
				}
				controleX = xOld = x; 
				controleY = yOld = y;
				wereSomeCommands = true;
			}
			while (content[from] != '\n') {
				if (content[from] > ' ') {
					throw new SyntaxException(0,from,"Dust in the tail of commands ["+SyntaxException.extractFragment(source,0,from,10)+"]"); 					
				}
				else {
					from++;
				}
			}
			if (!wereSomeCommands) {
				throw new SyntaxException(0,from,"No any commands in the command string ["+SyntaxException.extractFragment(source,0,from,10)+"]"); 					
			}
			return result;
		}
	}

	static <T> T convertTo(final Class<T> awaited, final String source) throws SyntaxException {
		if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null");
		}
		else if (source == null || source.isEmpty()) {
			throw new IllegalArgumentException("Source content to convert can't be null or empty");
		}
		else if (awaited.isAssignableFrom(Color.class)) {
			return awaited.cast(CSSUtils.asColor(source));
		}
		else if (awaited.isAssignableFrom(Stroke.class)) {
			final char[]	widthContent = source.toCharArray();
			final float[]	result = new float[1]; 
			
			CharUtils.parseSignedFloat(widthContent,0,result,true);
			return awaited.cast(new BasicStroke(result[0]));
		}
		else if (awaited.isAssignableFrom(AffineTransform.class)) {
			return awaited.cast(CSSUtils.asTransform(source));
		}
		else if (awaited.isAssignableFrom(Point2D[].class)) {
			return awaited.cast(SVGUtils.extractPoints(source));
		}
		else if (awaited.isAssignableFrom(GeneralPath.class)) {
			return awaited.cast(SVGUtils.extractCommands(source));
		}
		else if (awaited.isAssignableFrom(Font.class)) {
			final Object[]			result = new Object[3];
			final ArgumentType[]	LEXEMAS = {ArgumentType.ordinalInt,ArgumentType.name,ArgumentType.simpleTerminatedString};
			
			try{CharUtils.extract(source.toCharArray(),0,result,(Object[])LEXEMAS);
			} catch (SyntaxException e) {
				throw new IllegalArgumentException("String ["+new String()+"]: error at index ["+e.getCol()+"] ("+e.getLocalizedMessage()+")");
			}
			
			return awaited.cast(new Font(result[2].toString(),Font.PLAIN,(int)CSSUtils.asDistance((result[0].toString()+result[1].toString())).getValueAs(Distance.Units.pt)));
		}
		else {
			throw new SyntaxException(0,0,"Conversion ["+source+"] to ["+awaited+"] is not supported");
		}
	}
	
	static String buildFontDescriptor(final Map<String,Object> attributes) throws SyntaxException {
		return new StringBuilder().append(CSSUtils.asDistance(attributes.get("font-size").toString())).append(" \"").append(attributes.get("font-family")).append("\"").toString();
	}
	
	static <T> T extractInstrument(final String propName, final Map<String,Object> attributes, final Class<T> instrumentType) throws SyntaxException {
		switch (propName) {
			case "stroke"	:
				if (attributes.containsKey(propName) && !"none".equalsIgnoreCase(attributes.get(propName).toString())) {
					return instrumentType.cast(convertTo(Color.class,attributes.get(propName).toString()));
				}
				else {
					return instrumentType.cast(Color.BLACK);
				}
			case "fill"	:
				if (attributes.containsKey(propName) && !"none".equalsIgnoreCase(attributes.get(propName).toString())) {
					return instrumentType.cast(convertTo(Color.class,attributes.get(propName).toString()));
				}
				else {
					return null;
				}
			case "stroke-width"	:
				return instrumentType.cast(convertTo(Stroke.class,attributes.get(propName).toString()));
			case "transform"	:
				if (attributes.containsKey(propName) && !"none".equalsIgnoreCase(attributes.get(propName).toString())) {
					return instrumentType.cast(convertTo(AffineTransform.class,attributes.get(propName).toString()));
				}
				else {
					return null;
				}
			case "font"	:
				final String	fontFamily = (String)(attributes.containsKey("font-family") ? attributes.get("font-family").toString() : "Courier");
				final String	fontSize = (String)(attributes.containsKey("font-size") ? attributes.get("font-size").toString() : "12pt");
				final String	fontWeight = (String)(attributes.containsKey("font-weight") ? attributes.get("font-weight").toString() : "normal");
				final String	fontStyle = (String)(attributes.containsKey("font-style") ? attributes.get("font-style").toString() : "normal");
				final int		size = (int)CSSUtils.asDistance(fontSize).getValueAs(CSSUtils.Distance.Units.pt);
				
				return instrumentType.cast(new Font(fontFamily,Font.PLAIN,size));
			default : 
				return null;
		}
	}	

	static boolean hasSubstitutionInside(final CharSequence content) {
		if (content == null) {
			throw new NullPointerException("Content to test can't be null");
		}
		else {
			for (int index = 0, maxIndex = content.length(); index < maxIndex; index++) {
				if (content.charAt(index) == '$' && index < maxIndex - 1 && content.charAt(index+1) == '{') {
					return true;
				}
			}
			return false;
		}
	}
	
	static boolean hasAnySubstitutions(final Map<String,Object> attributes, final String... attributeNames) {
		if (attributes == null) {
			throw new NullPointerException("Attribute map can't be null");
		}
		else if (attributeNames == null || attributeNames.length == 0) {
			throw new IllegalArgumentException("Attribute names can't be null or empty array");
		}
		else if (Utils.checkArrayContent4Nulls(attributeNames) != -1) {
			throw new NullPointerException("Attribute names contain nulls inside");
		}
		else {
			for (String item : attributeNames) {
				if (attributes.containsKey(item) && hasSubstitutionInside(attributes.get(item).toString())) {
					return true;
				}
			}
			return false;
		}
	}
	
	static <T extends OnlineGetter> T buildOnlineGetter(final Class<T> clazz, final String content, final SubstitutionSource ss) {
		if (clazz == null) {
			throw new NullPointerException("Getter class can't be null"); 
		}
		else if (content == null || content.isEmpty()) {
			throw new IllegalArgumentException("Substitution string can't be null or empty"); 
		}
		else if (ss == null) {
			throw new NullPointerException("Substitution source can't be null"); 
		}
		else if (clazz.isAssignableFrom(OnlineIntGetter.class)) {
			if (hasSubstitutionInside(content)) {
				return clazz.cast(new OnlineIntGetter() {
					@Override
					public boolean isImmutable() {
						return false;
					}
					
					@Override
					public int get() {
						try {return Integer.valueOf(CharUtils.substitute(SVGPainter.class.getSimpleName(),content,ss).trim());
						} catch (NumberFormatException exc) {
							return 0;
						}
					}
				});
			}
			else {
				return clazz.cast(OnlineIntGetter.forValue(Integer.valueOf(content.trim())));
			}
		}
		else if (clazz.isAssignableFrom(OnlineLongGetter.class)) {
			if (hasSubstitutionInside(content)) {
				return clazz.cast(new OnlineLongGetter() {
					@Override
					public boolean isImmutable() {
						return false;
					}
					
					@Override
					public long get() {
						try {return Long.valueOf(CharUtils.substitute(SVGPainter.class.getSimpleName(),content,ss).trim());
						} catch (NumberFormatException exc) {
							return 0;
						}
					}
				});
			}
			else {
				return clazz.cast(OnlineLongGetter.forValue(Long.valueOf(content.trim())));
			}
		}
		else if (clazz.isAssignableFrom(OnlineFloatGetter.class)) {
			if (hasSubstitutionInside(content)) {
				return clazz.cast(new OnlineFloatGetter() {
					@Override
					public boolean isImmutable() {
						return false;
					}
					
					@Override
					public float get() {
						try {return Float.valueOf(CharUtils.substitute(SVGPainter.class.getSimpleName(),content,ss));
						} catch (NumberFormatException exc) {
							return 0;
						}
					}
				});
			}
			else {
				return clazz.cast(OnlineFloatGetter.forValue(Float.valueOf(content.trim())));
			}
		}
		else if (clazz.isAssignableFrom(OnlineDoubleGetter.class)) {
			if (hasSubstitutionInside(content)) {
				return clazz.cast(new OnlineDoubleGetter() {
					@Override
					public boolean isImmutable() {
						return false;
					}
					
					@Override
					public double get() {
						try {return Double.valueOf(CharUtils.substitute(SVGPainter.class.getSimpleName(),content,ss));
						} catch (NumberFormatException exc) {
							return 0;
						}
					}
				});
			}
			else {
				return clazz.cast(OnlineDoubleGetter.forValue(Double.valueOf(content.trim())));
			}
		}
		else if (clazz.isAssignableFrom(OnlineBooleanGetter.class)) {
			if (hasSubstitutionInside(content)) {
				return clazz.cast(new OnlineBooleanGetter() {
					@Override
					public boolean isImmutable() {
						return false;
					} 
					
					@Override
					public boolean get() {
						try {return Boolean.valueOf(CharUtils.substitute(SVGPainter.class.getSimpleName(),content,ss));
						} catch (IllegalArgumentException exc) {
							return false;
						}
					}
				});
			}
			else {
				return clazz.cast(OnlineBooleanGetter.forValue(Boolean.valueOf(content.trim())));
			}
		}
		else if (clazz.isAssignableFrom(OnlineCharGetter.class)) {
			if (hasSubstitutionInside(content)) {
				return clazz.cast(new OnlineCharGetter() {
					@Override
					public boolean isImmutable() {
						return false;
					}
					
					@Override
					public char get() {
						final String result = CharUtils.substitute(SVGPainter.class.getSimpleName(),content,ss);
						
						return result.isEmpty() ? ' ' : result.charAt(0);
					}
				});
			}
			else {
				return clazz.cast(OnlineCharGetter.forValue(content.trim().isEmpty() ? ' ' : content.trim().charAt(0)));
			}
		}
		else if (clazz.isAssignableFrom(OnlineStringGetter.class)) {
			if (hasSubstitutionInside(content)) {
				return clazz.cast(new OnlineStringGetter() {
					@Override
					public boolean isImmutable() {
						return false;
					}
					
					@Override
					public String get() {
						return CharUtils.substitute(SVGPainter.class.getSimpleName(),content,ss);
					}
				});
			}
			else {
				return clazz.cast(OnlineStringGetter.forValue(content));
			}
		}
		else {
			throw new UnsupportedOperationException("Interface type ["+clazz.getCanonicalName()+"] is not supported yet");
		}
	}

	static <T> OnlineObjectGetter<T> buildOnlineObjectGetter(final Class<T> clazz, final String content, final SubstitutionSource ss, final ConvertorInterface conv) throws ContentException {
		if (clazz == null) {
			throw new NullPointerException("Getter class can't be null"); 
		}
		else if (content == null || content.isEmpty()) {
			throw new IllegalArgumentException("Substitution string can't be null or empty"); 
		}
		else if (ss == null) {
			throw new NullPointerException("Substitution source can't be null"); 
		}
		else if (conv == null) {
			throw new NullPointerException("Convertor can't be null"); 
		}
		else {
			if (hasSubstitutionInside(content)) {
				return new OnlineObjectGetter<T>() {
					@Override
					public boolean isImmutable() {
						return false;
					}
					
					@Override
					public T get() {
						try{return clazz.cast(conv.convertTo(clazz,CharUtils.substitute(SVGPainter.class.getSimpleName(),content,ss)));
						} catch (ContentException e) {
							return null;
						}
					}
				};
			}
			else {
				return OnlineObjectGetter.forValue(clazz.cast(conv.convertTo(clazz,content)));
			}
		}
	}

	// got from https://github.com/iconfinder/batik/blob/master/sources/org/apache/batik/ext/awt/geom/ExtendedGeneralPath.java
	private static Arc2D computeArc(final double x0, final double y0, final double radx, final double rady, final double rotation, final boolean largeArcFlag, final boolean sweepFlag, final double x, final double y) {
		final double 	dx2 = (x0 - x) / 2.0;
		final double 	dy2 = (y0 - y) / 2.0;
		// Convert angle from degrees to radians
		final double 	angle = (float)Math.toRadians(rotation % 360.0);
		final double 	cosAngle = Math.cos(angle);
		final double 	sinAngle = Math.sin(angle);
		
		double rx = radx; 
		double ry = rady; 

		//
		// Step 1 : Compute (x1, y1)
		//
		double x1 = (cosAngle * dx2 + sinAngle * dy2);
		double y1 = (-sinAngle * dx2 + cosAngle * dy2);
		
		// Ensure radii are large enough
		rx = Math.abs(rx);
		ry = Math.abs(ry);
		double Prx = rx * rx;
		double Pry = ry * ry;
		double Px1 = x1 * x1;
		double Py1 = y1 * y1;
		// check that radii are large enough
		double radiiCheck = Px1/Prx + Py1/Pry, sqrtRadiiCheck;
		
		if (radiiCheck > 1) {
			rx = (sqrtRadiiCheck = Math.sqrt(radiiCheck)) * rx;
			ry = sqrtRadiiCheck * ry;
			Prx = rx * rx;
			Pry = ry * ry;
		}
		
		//
		// Step 2 : Compute (cx1, cy1)
		//
		double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
		double sq = ((Prx*Pry)-(Prx*Py1)-(Pry*Px1)) / ((Prx*Py1)+(Pry*Px1));
		sq = (sq < 0) ? 0 : sq;
		double coef = (sign * Math.sqrt(sq));
		double cx1 = coef * ((rx * y1) / ry);
		double cy1 = coef * -((ry * x1) / rx);
		
		//
		// Step 3 : Compute (cx, cy) from (cx1, cy1)
		//
		double sx2 = (x0 + x) / 2.0;
		double sy2 = (y0 + y) / 2.0;
		double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
		double cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);
		
		//
		// Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
		//
		double ux = (x1 - cx1) / rx;
		double uy = (y1 - cy1) / ry;
		double vx = (-x1 - cx1) / rx;
		double vy = (-y1 - cy1) / ry;
		double p, n;
		// Compute the angle start
		n = Math.sqrt((ux * ux) + (uy * uy));
		p = ux; // (1 * ux) + (0 * uy)
		sign = (uy < 0) ? -1.0 : 1.0;
		double angleStart = Math.toDegrees(sign * Math.acos(p / n));
		
		// Compute the angle extent
		n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
		p = ux * vx + uy * vy;
		sign = (ux * vy - uy * vx < 0) ? -1.0 : 1.0;
		double angleExtent = Math.toDegrees(sign * Math.acos(p / n));
		
		if(!sweepFlag && angleExtent > 0) {
			angleExtent -= 360f;
		} else if (sweepFlag && angleExtent < 0) {
			angleExtent += 360f;
		}
		angleExtent %= 360f;
		angleStart %= 360f;
		
		//
		// We can now build the resulting Arc2D in double
		//
		Arc2D.Double arc = new Arc2D.Double();
		arc.x = cx - rx;
		arc.y = cy - ry;
		arc.width = rx * 2.0;
		arc.height = ry * 2.0;
		arc.start = -angleStart;
		arc.extent = -angleExtent;
		
		return arc;
	}
}
