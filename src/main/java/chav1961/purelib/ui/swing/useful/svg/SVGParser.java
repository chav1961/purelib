package chav1961.purelib.ui.swing.useful.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.XMLUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.enumerations.XSDCollection;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.AbstractPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.CirclePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.EllipsePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.LinePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.PathPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.PolygonPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.PolylinePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.RectPainter;

public class SVGParser {
	@FunctionalInterface
	public interface InstrumentGetter<T> {
		T getInstrument(String propName, Map<String,Object> attributes, Class<T> instrumentType);
	}
	
	public static SVGPainter parse(final InputStream svgXml) throws NullPointerException, ContentException {
		return parse(svgXml,PureLibSettings.NULL_LOGGER,(propName,attributes,instrumentType)->{return extractInstrument(propName,attributes,instrumentType);});
	}

	public static SVGPainter parse(final InputStream svgXml, final InstrumentGetter getter) throws NullPointerException, ContentException {
		return parse(svgXml,PureLibSettings.NULL_LOGGER,getter);
	}
	
	public static SVGPainter parse(final InputStream svgXml, final LoggerFacade logger, final InstrumentGetter getter) throws NullPointerException, ContentException {
		if (svgXml == null) {
			throw new NullPointerException("SVG input stream can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (getter == null) {
			throw new NullPointerException("Instrument getter can't be null");
		}
		else {
			final Document			doc = Utils.validateAndLoadXML(svgXml,Utils.getPurelibXSD(XSDCollection.SVG),logger);
			
			doc.normalizeDocument();
			XMLUtils.walkDownXML(doc.getDocumentElement(),(mode,node)->{	// Extract all styles from the content
				if (mode == NodeEnterMode.ENTER && "style".equals(node.getTagName())) {
					if (node.hasAttribute("href")) {
						try{XMLUtils.parseCSS(new String(Utils.loadCharsFromURI(XMLUtils.getAttribute(node,"href",URI.class),"UTF-8")));
						} catch (IOException | SyntaxException e) {
							e.printStackTrace();
						}
					}
					else {
						try{XMLUtils.parseCSS(node.getTextContent());
						} catch (SyntaxException e) {
							e.printStackTrace();
						}
					}
				}
				return ContinueMode.CONTINUE;
			});
			
			return buildPainter(doc.getDocumentElement(),getter);
		}
	}
	
	private static SVGPainter buildPainter(final Element root, final InstrumentGetter getter) throws ContentException {
		final int[]					widthAndHeight = new int[2];
		final List<AbstractPainter>	primitives = new ArrayList<>();
		
		XMLUtils.walkDownXML(root, (mode,node)->{
			if (mode == NodeEnterMode.ENTER) {
				final Map<String,Object>	props = extractProps(node); 
				switch (node.getNodeName()) {
					case "svg"	:
//						<svg height="210" width="500">
						widthAndHeight[0] = XMLUtils.getAttribute(node,"width", int.class);
						widthAndHeight[1] = XMLUtils.getAttribute(node,"height", int.class);
						break;
					case "line"	:
//						  <line x1="0" y1="0" x2="200" y2="200" style="stroke:rgb(255,0,0);stroke-width:2" />
						final LinePainter 			line = new LinePainter(XMLUtils.getAttribute(node,"x1",float.class)
																,XMLUtils.getAttribute(node,"y1",float.class)
																,XMLUtils.getAttribute(node,"x2",float.class)
																,XMLUtils.getAttribute(node,"y2",float.class)
																,(Color)getter.getInstrument("color",props,Color.class)
																,(Stroke)getter.getInstrument("stroke",props,Stroke.class));
						primitives.add(line);
						break;
					case "rect"	:
// 						<rect width="100" height="50" x="0" y="0" rx="0" ry="0"  style="stroke:rgb(255,0,0);stroke-width:2" />
						final RectPainter 			rect = new RectPainter(XMLUtils.getAttribute(node,"x1",float.class)
																,XMLUtils.getAttribute(node,"y1",float.class)
																,XMLUtils.getAttribute(node,"x2",float.class)
																,XMLUtils.getAttribute(node,"y2",float.class)
																,(Color)getter.getInstrument("color",props,Color.class)
																,(Stroke)getter.getInstrument("stroke",props,Stroke.class));
						primitives.add(rect);
						break;
					case "circle"	:
// 						<circle r="50" cx="0" cy="0" style="stroke:rgb(255,0,0);stroke-width:2" />
						final CirclePainter			circle = new CirclePainter(XMLUtils.getAttribute(node,"cx",float.class)
																,XMLUtils.getAttribute(node,"cy",float.class)
																,XMLUtils.getAttribute(node,"r",float.class)
																,(Color)getter.getInstrument("color",props,Color.class)
																,(Stroke)getter.getInstrument("stroke",props,Stroke.class));
						primitives.add(circle);
						break;
					case "ellipse"	:
// 						<ellipse rx="50" ry="25" cx="0" cy="0" style="stroke:rgb(255,0,0);stroke-width:2" />
						final EllipsePainter		ellipse = new EllipsePainter(XMLUtils.getAttribute(node,"cx",float.class)
																,XMLUtils.getAttribute(node,"cy",float.class)
																,XMLUtils.getAttribute(node,"rx",float.class)
																,XMLUtils.getAttribute(node,"ry",float.class)
																,(Color)getter.getInstrument("color",props,Color.class)
																,(Stroke)getter.getInstrument("stroke",props,Stroke.class));
						primitives.add(ellipse);
						break;
					case "polyline"	:
// 						<polyline points="10,10 50,100 81,100 140,10" style="stroke:rgb(255,0,0);stroke-width:2" />
						final PolylinePainter		polyline = new PolylinePainter(extractPoints(XMLUtils.getAttribute(node,"points",String.class))
																,(Color)getter.getInstrument("color",props,Color.class)
																,(Stroke)getter.getInstrument("stroke",props,Stroke.class));
						primitives.add(polyline);
						break;
					case "polygon"	:
// 						<polyline points="10,10 50,100 81,100 140,10" style="stroke:rgb(255,0,0);stroke-width:2" />
						final PolygonPainter		polygon = new PolygonPainter(extractPoints(XMLUtils.getAttribute(node,"points",String.class))
																,(Color)getter.getInstrument("color",props,Color.class)
																,(Stroke)getter.getInstrument("stroke",props,Stroke.class));
						primitives.add(polygon);
						break;
					case "path"	:
// 						<path d="M 10,30 A 20,20 0,0,1 50,30 A 20,20 0,0,1 90,30 Q 90,60 50,90 Q 10,60 10,30 z" style="stroke:rgb(255,0,0);stroke-width:2" />
						final PathPainter			path = new PathPainter(extractCommands(XMLUtils.getAttribute(node,"d",String.class))
																,(Color)getter.getInstrument("color",props,Color.class)
																,(Stroke)getter.getInstrument("stroke",props,Stroke.class));
						primitives.add(path);
						break;
				}
			}
			return ContinueMode.CONTINUE; 
		});
		return new SVGPainter(widthAndHeight[0], widthAndHeight[1], primitives.toArray(new AbstractPainter[primitives.size()]));
	}

	private static Map<String, Object> extractProps(final Element node) {
		// TODO Auto-generated method stub
		final Map<String,Object>	result = new HashMap<>();
		return null;
	}
	
	private static Point2D[] extractPoints(final String source) throws SyntaxException {
		final char[]	content = source.toCharArray();
		final float[]	number = new float[1];
		float			x, y;
		int 			pointCount = 0, from = 0;
		
		for (char symbol : content) {
			if (symbol == ',') {
				pointCount++;
			}
		}
		
		final Point2D[]	result = new Point[pointCount];
		
		for (int index = 0; index < pointCount; index++) {
			from = CharUtils.skipBlank(content,CharUtils.parseSignedFloat(content,from,number,true),false);
			x = number[0];
			if (content[from] == ',') {
				from++;
			}
			else {
				throw new SyntaxException(0,from,"Missing (,)"); 
			}
			from = CharUtils.skipBlank(content,CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,false),number,true),false);
			y = number[0];
			result[index] = new Point2D.Float(x,y); 
		}
		return result; 
	}

	private static GeneralPath extractCommands(final String source) throws ContentException {
		final char[]		content = new char[source.length()+1];
		final float[]		number = new float[1];
		final int[]			integer = new int[1];
		final GeneralPath	result = new GeneralPath();
		float				x, y, xOld = 0, yOld = 0, x1, y1, x2, y2, rx, ry, rotation, xPrev, yPrev;
		int 				from = 0, large, sweep;

		source.getChars(0,source.length(),content,0);
		content[content.length-1] = '\n';
		
loop:	for (;;) {
			from = CharUtils.skipBlank(content,from,true);
			x = x1 = x2 = 0;
			y = y1 = y2 = 0;
			switch (content[from]) {
				case '\n' :
					break loop;
				case 'm' :
					x = xOld;
					y = yOld;
				case 'M' :
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
					x += number[0];
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
					y += number[0];
					result.moveTo(x, y);
					break;
				case 'l' :
					x = xOld;
					y = yOld;
				case 'L' :
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
					x += number[0];
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
					y += number[0];
					result.lineTo(x, y);
					break;
				case 'h' :
					x = xOld;
				case 'H' :
					y = yOld;
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
					x += number[0];
					result.lineTo(x, y);
					break;
				case 'v' :
					y = yOld;
				case 'V' :
					x = xOld;
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
					y += number[0];
					result.lineTo(x, y);
					break;
				case 'z' : case 'Z' :
					result.closePath();
					break loop;
				case 'c' :
					x = x1 = x2 = xOld;
					y = y1 = y2 = yOld;
				case 'C' :
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
					x1 += number[0];
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
					y1 += number[0];
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
					x2 += number[0];
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
					y2 += number[0];
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
					x += number[0];
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
					y += number[0];
					result.curveTo(x1, y1, x2, y2, x, y);
					break;
				case 's' :
					x = x1 = x2 = xOld;
					y = y1 = y2 = yOld;
				case 'S' :
					break;
				case 'q' :
					x = x1 = x2 = xOld;
					y = y1 = y2 = yOld;
				case 'Q' :
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from+1,true),number,true);
					x1 += number[0];
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
					y1 += number[0];
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
					x += number[0];
					from = CharUtils.parseSignedFloat(content,CharUtils.skipBlank(content,from,true),number,true);
					y += number[0];
					result.quadTo(x1, y1, x, y);
					break;
				case 't' :
					x = x1 = x2 = xOld;
					y = y1 = y2 = yOld;
				case 'T' :
					break;
				case 'a' :
					x = x1 = x2 = xOld;
					y = y1 = y2 = yOld;
				case 'A' :
					xPrev = x;
					yPrev = y;
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
					result.append(computeArc(xPrev,yPrev,rx,ry,rotation,large != 0,sweep != 0,x,y),true);
					break;
				default :
					throw new SyntaxException(0,from,"Unknown path command in ["+SyntaxException.extractFragment(source,0,from,10)+"]"); 					
			}
			xOld = (float)result.getCurrentPoint().getX(); 
			yOld = (float)result.getCurrentPoint().getY();
		}
		return result;
	}

	// got from https://github.com/iconfinder/batik/blob/master/sources/org/apache/batik/ext/awt/geom/ExtendedGeneralPath.java
	private static Arc2D computeArc(final float x0, final float y0, final float radx, final float rady, final float rotation, final boolean largeArcFlag, final boolean sweepFlag, final float x, final float y) {
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
		// We can now build the resulting Arc2D in float
		//
		Arc2D.Float arc = new Arc2D.Float();
		arc.x = (float)(cx - rx);
		arc.y = (float)(cy - ry);
		arc.width = (float)(rx * 2.0);
		arc.height = (float)(ry * 2.0);
		arc.start = (float)-angleStart;
		arc.extent = (float)-angleExtent;
		
		return arc;
	}

	private static <T> T extractInstrument(final String propName, final Map<String,Object> attributes, final Class<T> instrumentType) {
		switch (propName) {
			case "color"	:
				return instrumentType.cast(Color.BLACK);
			case "stroke"	:
				return instrumentType.cast(new BasicStroke(1.0f));
			default : 
				return null;
		}
	}	
}
