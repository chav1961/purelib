package chav1961.purelib.ui.swing.useful.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
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
import org.w3c.dom.NamedNodeMap;

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
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.TextPainter;

public class SVGParser {
	@FunctionalInterface
	public interface InstrumentGetter<T> {
		T getInstrument(String propName, Map<String,Object> attributes, Class<T> instrumentType) throws ContentException;
	}
	
	public static SVGPainter parse(final InputStream svgXml) throws NullPointerException, ContentException {
		return parse(svgXml,PureLibSettings.NULL_LOGGER,(propName,attributes,instrumentType)->{return SVGUtils.extractInstrument(propName,attributes,instrumentType);});
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
																,(Color)getter.getInstrument("stroke",props,Color.class)
																,(Stroke)getter.getInstrument("stroke-width",props,Stroke.class));
						primitives.add(line);
						break;
					case "rect"	:
// 						<rect width="100" height="50" x="0" y="0" rx="0" ry="0"  style="stroke:rgb(255,0,0);stroke-width:2" />
						final RectPainter 			rect = new RectPainter(XMLUtils.getAttribute(node,"x1",float.class)
																,XMLUtils.getAttribute(node,"y1",float.class)
																,XMLUtils.getAttribute(node,"x2",float.class)
																,XMLUtils.getAttribute(node,"y2",float.class)
																,(Color)getter.getInstrument("stroke",props,Color.class)
																,(Color)getter.getInstrument("fill",props,Color.class)
																,(Stroke)getter.getInstrument("stroke-width",props,Stroke.class));
						primitives.add(rect);
						break;
					case "circle"	:
// 						<circle r="50" cx="0" cy="0" style="stroke:rgb(255,0,0);stroke-width:2" />
						final CirclePainter			circle = new CirclePainter(XMLUtils.getAttribute(node,"cx",float.class)
																,XMLUtils.getAttribute(node,"cy",float.class)
																,XMLUtils.getAttribute(node,"r",float.class)
																,(Color)getter.getInstrument("stroke",props,Color.class)
																,(Color)getter.getInstrument("fill",props,Color.class)
																,(Stroke)getter.getInstrument("stroke-width",props,Stroke.class));
						primitives.add(circle);
						break;
					case "ellipse"	:
// 						<ellipse rx="50" ry="25" cx="0" cy="0" style="stroke:rgb(255,0,0);stroke-width:2" />
						final EllipsePainter		ellipse = new EllipsePainter(XMLUtils.getAttribute(node,"cx",float.class)
																,XMLUtils.getAttribute(node,"cy",float.class)
																,XMLUtils.getAttribute(node,"rx",float.class)
																,XMLUtils.getAttribute(node,"ry",float.class)
																,(Color)getter.getInstrument("stroke",props,Color.class)
																,(Color)getter.getInstrument("fill",props,Color.class)
																,(Stroke)getter.getInstrument("stroke-width",props,Stroke.class));
						primitives.add(ellipse);
						break;
					case "polyline"	:
// 						<polyline points="10,10 50,100 81,100 140,10" style="stroke:rgb(255,0,0);stroke-width:2" />
						final PolylinePainter		polyline = new PolylinePainter(SVGUtils.extractPoints(XMLUtils.getAttribute(node,"points",String.class))
																,(Color)getter.getInstrument("stroke",props,Color.class)
																,(Stroke)getter.getInstrument("stroke-width",props,Stroke.class));
						primitives.add(polyline);
						break;
					case "polygon"	:
// 						<polyline points="10,10 50,100 81,100 140,10" style="stroke:rgb(255,0,0);stroke-width:2" />
						final PolygonPainter		polygon = new PolygonPainter(SVGUtils.extractPoints(XMLUtils.getAttribute(node,"points",String.class))
																,(Color)getter.getInstrument("stroke",props,Color.class)
																,(Color)getter.getInstrument("fill",props,Color.class)
																,(Stroke)getter.getInstrument("stroke-width",props,Stroke.class));
						primitives.add(polygon);
						break;
					case "path"	:
// 						<path d="M 10,30 A 20,20 0,0,1 50,30 A 20,20 0,0,1 90,30 Q 90,60 50,90 Q 10,60 10,30 z" style="stroke:rgb(255,0,0);stroke-width:2" />
						final PathPainter			path = new PathPainter(SVGUtils.extractCommands(XMLUtils.getAttribute(node,"d",String.class))
																,(Color)getter.getInstrument("stroke",props,Color.class)
																,(Color)getter.getInstrument("fill",props,Color.class)
																,(Stroke)getter.getInstrument("stroke-width",props,Stroke.class));
						primitives.add(path);
						break;
					case "text"	:
						final TextPainter			text = new TextPainter(XMLUtils.getAttribute(node,"x",float.class)
																,XMLUtils.getAttribute(node,"y",float.class)
																,node.getTextContent()
																,(Font)getter.getInstrument("font",props,Font.class)
																,(Color)getter.getInstrument("fill",props,Color.class)
																,(AffineTransform)getter.getInstrument("transform",props,AffineTransform.class));
						primitives.add(text);
						break;
				}
			}
			return ContinueMode.CONTINUE; 
		});
		return new SVGPainter(widthAndHeight[0], widthAndHeight[1], primitives.toArray(new AbstractPainter[primitives.size()]));
	}

	private static Map<String, Object> extractProps(final Element node) {
		final Map<String,Object>	result = new HashMap<>();
		final NamedNodeMap			map = node.getAttributes();
		
		if (map != null) {
			for (int index = 0; index < map.getLength(); index++) {
				result.put(map.item(index).getNodeName(),map.item(index).getNodeValue());
			}
		}
		return result;
	}
	
}
