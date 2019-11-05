package chav1961.purelib.ui.swing.useful.svg;

import java.awt.Color;
import java.awt.Point;
import java.awt.Stroke;
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
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.PolygonPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.PolylinePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.RectPainter;

public class SVGParser {
	private static final Map<String,AbstractAttribute>		GRAPHIC_ATTR = new HashMap<>();
	
	static {
	}
	
	public static SVGPainter parse(final InputStream svgXml) throws NullPointerException, ContentException {
		return parse(svgXml,PureLibSettings.NULL_LOGGER);
	}
	
	public static SVGPainter parse(final InputStream svgXml, final LoggerFacade logger) throws NullPointerException, ContentException {
		if (svgXml == null) {
			throw new NullPointerException("SVG input stream can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
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
			
			return buildPainter(doc.getDocumentElement());
		}
		
	}
	
	
	private static SVGPainter buildPainter(final Element root) throws SyntaxException {
		final int[]					widthAndHeight = new int[2];
		final List<AbstractPainter>	primitives = new ArrayList<>();
		
		XMLUtils.walkDownXML(root, (mode,node)->{
			if (mode == NodeEnterMode.ENTER) {
				switch (node.getNodeName()) {
					case "svg"	:
//						<svg height="210" width="500">
						widthAndHeight[0] = XMLUtils.getAttribute(node,"width", int.class);
						widthAndHeight[1] = XMLUtils.getAttribute(node,"height", int.class);
						break;
					case "line"	:
//						  <line x1="0" y1="0" x2="200" y2="200" style="stroke:rgb(255,0,0);stroke-width:2" />
						final Map<String,Object>	lineProps = extractProps(node); 
						final LinePainter 			line = new LinePainter(XMLUtils.getAttribute(node,"x1",float.class)
																,XMLUtils.getAttribute(node,"y1",float.class)
																,XMLUtils.getAttribute(node,"x2",float.class)
																,XMLUtils.getAttribute(node,"y2",float.class)
																,(Color)lineProps.get("color")
																,(Stroke)lineProps.get("stroke"));
						primitives.add(line);
						break;
					case "rect"	:
// 						<rect width="100" height="50" x="0" y="0" rx="0" ry="0"  style="stroke:rgb(255,0,0);stroke-width:2" />
						final Map<String,Object>	rectProps = extractProps(node); 
						final RectPainter 			rect = new RectPainter(XMLUtils.getAttribute(node,"x1",float.class)
																,XMLUtils.getAttribute(node,"y1",float.class)
																,XMLUtils.getAttribute(node,"x2",float.class)
																,XMLUtils.getAttribute(node,"y2",float.class)
																,(Color)rectProps.get("color")
																,(Stroke)rectProps.get("stroke"));
						primitives.add(rect);
						break;
					case "circle"	:
// 						<circle r="50" cx="0" cy="0" style="stroke:rgb(255,0,0);stroke-width:2" />
						final Map<String,Object>	circleProps = extractProps(node); 
						final CirclePainter			circle = new CirclePainter(XMLUtils.getAttribute(node,"cx",float.class)
																,XMLUtils.getAttribute(node,"cy",float.class)
																,XMLUtils.getAttribute(node,"r",float.class)
																,(Color)circleProps.get("color")
																,(Stroke)circleProps.get("stroke"));
						primitives.add(circle);
						break;
					case "ellipse"	:
// 						<ellipse rx="50" ry="25" cx="0" cy="0" style="stroke:rgb(255,0,0);stroke-width:2" />
						final Map<String,Object>	ellipseProps = extractProps(node); 
						final EllipsePainter		ellipse = new EllipsePainter(XMLUtils.getAttribute(node,"cx",float.class)
																,XMLUtils.getAttribute(node,"cy",float.class)
																,XMLUtils.getAttribute(node,"rx",float.class)
																,XMLUtils.getAttribute(node,"ry",float.class)
																,(Color)ellipseProps.get("color")
																,(Stroke)ellipseProps.get("stroke"));
						primitives.add(ellipse);
						break;
					case "polyline"	:
// 						<polyline points="10,10 50,100 81,100 140,10" style="stroke:rgb(255,0,0);stroke-width:2" />
						final Map<String,Object>	polylineProps = extractProps(node); 
						final PolylinePainter		polyline = new PolylinePainter(extractPoints(XMLUtils.getAttribute(node,"points",String.class))
																,(Color)polylineProps.get("color")
																,(Stroke)polylineProps.get("stroke"));
						primitives.add(polyline);
						break;
					case "polygon"	:
// 						<polyline points="10,10 50,100 81,100 140,10" style="stroke:rgb(255,0,0);stroke-width:2" />
						final Map<String,Object>	polygonProps = extractProps(node); 
						final PolygonPainter		polygon = new PolygonPainter(extractPoints(XMLUtils.getAttribute(node,"points",String.class))
																,(Color)polygonProps.get("color")
																,(Stroke)polygonProps.get("stroke"));
						primitives.add(polygon);
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
			from = CharUtils.skipBlank(content,CharUtils.parseFloat(content,from,number,true),false);
			x = number[0];
			if (content[from] == ',') {
				from++;
			}
			else {
				throw new SyntaxException(0,from,"Missing (,)"); 
			}
			from = CharUtils.skipBlank(content,CharUtils.parseFloat(content,CharUtils.skipBlank(content,from,false),number,true),false);
			y = number[0];
			result[index] = new Point2D.Float(x,y); 
		}
		return result; 
	}
}
