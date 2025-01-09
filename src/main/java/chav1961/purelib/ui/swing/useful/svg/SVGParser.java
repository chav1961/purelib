package chav1961.purelib.ui.swing.useful.svg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import chav1961.purelib.basic.CSSUtils;
import chav1961.purelib.basic.CharUtils.SubstitutionSource;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.XMLUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.ConvertorInterface;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.OnlineFloatGetter;
import chav1961.purelib.basic.interfaces.OnlineStringGetter;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.enumerations.XSDCollection;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.AbstractPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.CirclePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.DynamicCirclePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.DynamicEllipsePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.DynamicLinePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.DynamicPathPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.DynamicPolygonPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.DynamicPolylinePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.DynamicRectPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.DynamicTextPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.EllipsePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.FillPolicy;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.LinePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.PathPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.PolygonPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.PolylinePainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.RectPainter;
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.TextPainter;

public class SVGParser {
	private static final String		LINE_ATTR_X1 = "x1";
	private static final String		LINE_ATTR_Y1 = "x1";
	private static final String		LINE_ATTR_X2 = "x2";
	private static final String		LINE_ATTR_Y2 = "y2";
	private static final String		LINE_ATTR_STROKE = "stroke";
	private static final String		LINE_ATTR_STROKE_WIDTH = "stroke-width";
	private static final String		LINE_ATTR_STROKE_DASH_ARRAY = "stroke-dasharray";
	private static final String[]	LINE_ATTRIBUTES = {LINE_ATTR_X1,LINE_ATTR_Y1,LINE_ATTR_X2,LINE_ATTR_Y2,LINE_ATTR_STROKE,LINE_ATTR_STROKE_WIDTH,LINE_ATTR_STROKE_DASH_ARRAY};
	
	private static final String		RECT_ATTR_X1 = "x1";
	private static final String		RECT_ATTR_Y1 = "y1";
	private static final String		RECT_ATTR_X2 = "x2";
	private static final String		RECT_ATTR_Y2 = "y2";
	private static final String		RECT_ATTR_STROKE = "stroke";
	private static final String		RECT_ATTR_FILL = "fill";
	private static final String		RECT_ATTR_STROKE_WIDTH = "stroke-width";
	private static final String[]	RECT_ATTRIBUTES = {RECT_ATTR_X1, RECT_ATTR_Y1, RECT_ATTR_X2, RECT_ATTR_Y2, RECT_ATTR_STROKE, RECT_ATTR_FILL, RECT_ATTR_STROKE_WIDTH};

	private static final String		CIRCLE_ATTR_CX = "cx";
	private static final String		CIRCLE_ATTR_CY = "cy";
	private static final String		CIRCLE_ATTR_R = "r";
	private static final String		CIRCLE_ATTR_STROKE = "stroke";
	private static final String		CIRCLE_ATTR_FILL = "fill";
	private static final String		CIRCLE_ATTR_STROKE_WIDTH = "stroke-width";
	private static final String[]	CIRCLE_ATTRIBUTES = {CIRCLE_ATTR_CX, CIRCLE_ATTR_CY, CIRCLE_ATTR_R, CIRCLE_ATTR_STROKE, CIRCLE_ATTR_FILL, CIRCLE_ATTR_STROKE_WIDTH};
	
	private static final String		ELLIPSE_ATTR_CX = "cx";
	private static final String		ELLIPSE_ATTR_CY = "cy";
	private static final String		ELLIPSE_ATTR_RX = "rx";
	private static final String		ELLIPSE_ATTR_RY = "ry";
	private static final String		ELLIPSE_ATTR_STROKE = "stroke";
	private static final String		ELLIPSE_ATTR_FILL = "fill";
	private static final String		ELLIPSE_ATTR_STROKE_WIDTH = "stroke-width";
	private static final String[]	ELLIPSE_ATTRIBUTES = {ELLIPSE_ATTR_CX, ELLIPSE_ATTR_CY, ELLIPSE_ATTR_RX, ELLIPSE_ATTR_RY, ELLIPSE_ATTR_STROKE, ELLIPSE_ATTR_FILL, ELLIPSE_ATTR_STROKE_WIDTH};

	private static final String		POLYLINE_ATTR_POINTS = "points";
	private static final String		POLYLINE_ATTR_STROKE = "stroke";
	private static final String		POLYLINE_ATTR_STROKE_WIDTH = "stroke-width";
	private static final String[]	POLYLINE_ATTRIBUTES = {POLYLINE_ATTR_POINTS, POLYLINE_ATTR_STROKE, POLYLINE_ATTR_STROKE_WIDTH};
	
	private static final String		POLYGON_ATTR_POINTS = "points";
	private static final String		POLYGON_ATTR_STROKE = "stroke";
	private static final String		POLYGON_ATTR_FILL = "fill";
	private static final String		POLYGON_ATTR_STROKE_WIDTH = "stroke-width";
	private static final String[]	POLYGON_ATTRIBUTES = {POLYGON_ATTR_POINTS, POLYGON_ATTR_STROKE, POLYGON_ATTR_FILL, POLYGON_ATTR_STROKE_WIDTH};

	private static final String		PATH_ATTR_D = "d";
	private static final String		PATH_ATTR_STROKE = "stroke";
	private static final String		PATH_ATTR_FILL = "fill";
	private static final String		PATH_ATTR_STROKE_WIDTH = "stroke-width";
	private static final String		PATH_ATTR_TRANSFORM = "transform";
	private static final String[]	PATH_ATTRIBUTES = {PATH_ATTR_D, PATH_ATTR_STROKE, PATH_ATTR_FILL, PATH_ATTR_STROKE_WIDTH, PATH_ATTR_TRANSFORM};

	private static final String		TEXT_ATTR_X = "x";
	private static final String		TEXT_ATTR_Y = "y";
	private static final String		TEXT_ATTR_FONT = "font";
	private static final String		TEXT_ATTR_FILL = "fill";
	private static final String		TEXT_ATTR_TRANSFORM = "transform";
	private static final String[]	TEXT_ATTRIBUTES = {TEXT_ATTR_X, TEXT_ATTR_Y, TEXT_ATTR_FONT, TEXT_ATTR_FILL, TEXT_ATTR_TRANSFORM};
	
	
	@FunctionalInterface
	public interface InstrumentGetter<T> {
		T getInstrument(String propName, Map<String,Object> attributes, Class<T> instrumentType) throws ContentException;
		
		default T getInstrument(String[] propNames, Map<String,Object> attributes, Class<T> instrumentType) throws ContentException {
			return getInstrument(propNames[0], attributes, instrumentType);
		}
	}
	
	public static SVGPainter parse(final InputStream svgXml) throws NullPointerException, ContentException {
		return parse(svgXml,PureLibSettings.CURRENT_LOGGER,(propName,attributes,instrumentType)->{return SVGUtils.extractInstrument(propName,attributes,instrumentType);},(src)->src,FillPolicy.FILL_BOTH);
	}

	public static SVGPainter parse(final InputStream svgXml, final InstrumentGetter getter, final FillPolicy policy) throws NullPointerException, ContentException {
		return parse(svgXml,PureLibSettings.CURRENT_LOGGER,getter,(src)->src,policy);
	}

	public static SVGPainter parse(final InputStream svgXml, final SubstitutionSource ss) throws NullPointerException, ContentException {
		return parse(svgXml,PureLibSettings.CURRENT_LOGGER,(propName,attributes,instrumentType)->{return SVGUtils.extractInstrument(propName,attributes,instrumentType);},(src)->ss.getValue(src),FillPolicy.FILL_BOTH);
	}

	public static SVGPainter parse(final InputStream svgXml, final LoggerFacade logger, final InstrumentGetter getter, final SubstitutionSource ss, final FillPolicy policy) throws NullPointerException, ContentException {
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
			final Document			doc = XMLUtils.validateAndLoadXML(svgXml, XMLUtils.getPurelibXSDURL(XSDCollection.SVG_full), XMLUtils.ValidationSeverity.FATAL, logger);
			
			doc.normalizeDocument();
			XMLUtils.walkDownXML(doc.getDocumentElement(),(mode,node)->{	// Extract all styles from the content
				if (mode == NodeEnterMode.ENTER && "style".equals(node.getTagName())) {
					if (node.hasAttribute("href")) {
						try{CSSUtils.parseCSS(new String(URIUtils.loadCharsFromURI(XMLUtils.getAttribute(node,"href",URI.class),"UTF-8")));
						} catch (IOException | SyntaxException e) {
							e.printStackTrace();
						}
					}
					else {
						try{CSSUtils.parseCSS(node.getTextContent());
						} catch (SyntaxException e) { 
							e.printStackTrace();
						}
					}
				}
				return ContinueMode.CONTINUE;
			});
			
			return buildPainter(doc.getDocumentElement(),getter,ss,policy);
		}
	}
	
	static SVGPainter buildPainter(final Element root, final InstrumentGetter getter, final SubstitutionSource ss, final FillPolicy policy) throws ContentException {
		final int[]					widthAndHeight = new int[2];
		final List<AbstractPainter>	primitives = new ArrayList<>();
		final ConvertorInterface	ci = new ConvertorInterface() {
										@Override
										public <T> T convertTo(final Class<T> awaited, final Object source) throws ContentException {
											return SVGUtils.convertTo(awaited,source.toString());
										}
									};
		
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
//						<line x1="0" y1="0" x2="200" y2="200" style="stroke:rgb(255,0,0);stroke-width:2" />
						if (SVGUtils.hasAnySubstitutions(props,LINE_ATTRIBUTES)) {
							primitives.add(new DynamicLinePainter(
													SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,LINE_ATTR_X1,String.class),ss)
													,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,LINE_ATTR_Y1,String.class),ss)
													,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,LINE_ATTR_X2,String.class),ss)
													,SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,LINE_ATTR_Y2,String.class),ss)
													,SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,LINE_ATTR_STROKE,String.class),ss,ci)
													,SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,LINE_ATTR_STROKE_WIDTH,String.class),ss,ci)
							));
						}
						else {
							primitives.add(new LinePainter(XMLUtils.getAttribute(node,LINE_ATTR_X1,float.class)
													,XMLUtils.getAttribute(node,LINE_ATTR_Y1,float.class)
													,XMLUtils.getAttribute(node,LINE_ATTR_X2,float.class)
													,XMLUtils.getAttribute(node,LINE_ATTR_Y2,float.class)
													,(Color)getter.getInstrument(LINE_ATTR_STROKE,props,Color.class)
													,(Stroke)getter.getInstrument(new String[]{LINE_ATTR_STROKE_WIDTH, LINE_ATTR_STROKE_DASH_ARRAY},props,Stroke.class)) 
							);
						}
						break;
					case "rect"	:
// 						<rect width="100" height="50" x="0" y="0" rx="0" ry="0"  style="stroke:rgb(255,0,0);stroke-width:2" />
						if (SVGUtils.hasAnySubstitutions(props,RECT_ATTRIBUTES)) {
							primitives.add(new DynamicRectPainter(
//														SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,LINE_ATTR_X1,String.class))
//															? 
														SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,RECT_ATTR_X1,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,LINE_ATTR_X1,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,LINE_ATTR_Y1,String.class))
//															? 
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,RECT_ATTR_Y1,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,LINE_ATTR_Y1,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,LINE_ATTR_X2,String.class))
//															? 
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,RECT_ATTR_X2,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,LINE_ATTR_X2,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,LINE_ATTR_Y2,String.class))
//															? 
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,RECT_ATTR_Y2,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,LINE_ATTR_Y2,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,RECT_ATTR_STROKE,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,RECT_ATTR_STROKE,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(RECT_ATTR_STROKE,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,RECT_ATTR_FILL,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,RECT_ATTR_FILL,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(RECT_ATTR_FILL,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,LINE_ATTR_STROKE_WIDTH,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,RECT_ATTR_STROKE_WIDTH,String.class),ss,ci)
//															: OnlineObjectGetter.<Stroke>forValue((Stroke)getter.getInstrument(RECT_ATTR_STROKE_WIDTH,props,Stroke.class))
							));
						} 
						else {
							primitives.add(new RectPainter(XMLUtils.getAttribute(node,RECT_ATTR_X1,float.class)
														,XMLUtils.getAttribute(node,RECT_ATTR_Y1,float.class)
														,XMLUtils.getAttribute(node,RECT_ATTR_X2,float.class)
														,XMLUtils.getAttribute(node,RECT_ATTR_Y2,float.class)
														,(Color)getter.getInstrument(RECT_ATTR_STROKE,props,Color.class)
														,(Color)getter.getInstrument(RECT_ATTR_FILL,props,Color.class)
														,(Stroke)getter.getInstrument(RECT_ATTR_STROKE_WIDTH,props,Stroke.class))
							);
						}
						break;
					case "circle"	:
// 						<circle r="50" cx="0" cy="0" style="stroke:rgb(255,0,0);stroke-width:2" />
						if (SVGUtils.hasAnySubstitutions(props,CIRCLE_ATTRIBUTES)) {
							primitives.add(new DynamicCirclePainter(
//														SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,CIRCLE_ATTR_CX,String.class))
//															? 
														SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_CX,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,CIRCLE_ATTR_CX,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,CIRCLE_ATTR_CY,String.class))
//															? 
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_CY,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,CIRCLE_ATTR_CY,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,CIRCLE_ATTR_R,String.class))
//															? 
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_R,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,CIRCLE_ATTR_R,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,CIRCLE_ATTR_STROKE,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_STROKE,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(CIRCLE_ATTR_STROKE,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,CIRCLE_ATTR_FILL,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_FILL,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(CIRCLE_ATTR_FILL,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,CIRCLE_ATTR_STROKE_WIDTH,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_STROKE_WIDTH,String.class),ss,ci)
//															: OnlineObjectGetter.<Stroke>forValue((Stroke)getter.getInstrument(CIRCLE_ATTR_STROKE_WIDTH,props,Stroke.class))
							));
						}
						else {
							primitives.add(new CirclePainter(XMLUtils.getAttribute(node,CIRCLE_ATTR_CX,float.class)
														,XMLUtils.getAttribute(node,CIRCLE_ATTR_CY,float.class)
														,XMLUtils.getAttribute(node,CIRCLE_ATTR_R,float.class)
														,(Color)getter.getInstrument(CIRCLE_ATTR_STROKE,props,Color.class)
														,(Color)getter.getInstrument(CIRCLE_ATTR_FILL,props,Color.class)
														,(Stroke)getter.getInstrument(CIRCLE_ATTR_STROKE_WIDTH,props,Stroke.class))
							);
						}
						break;
					case "ellipse"	:
// 						<ellipse rx="50" ry="25" cx="0" cy="0" style="stroke:rgb(255,0,0);stroke-width:2" />
						if (SVGUtils.hasAnySubstitutions(props,ELLIPSE_ATTRIBUTES)) {
							primitives.add(new DynamicEllipsePainter(
//														SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,ELLIPSE_ATTR_CX,String.class))
//															? 
														SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_CX,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,ELLIPSE_ATTR_CX,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,ELLIPSE_ATTR_CY,String.class))
//															? 
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_CY,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,ELLIPSE_ATTR_CY,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,ELLIPSE_ATTR_RX,String.class))
//															? 
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_RX,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,ELLIPSE_ATTR_RX,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,ELLIPSE_ATTR_RY,String.class))
//															? 
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_RY,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,ELLIPSE_ATTR_RY,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,ELLIPSE_ATTR_STROKE,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_STROKE,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(ELLIPSE_ATTR_STROKE,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,ELLIPSE_ATTR_FILL,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_FILL,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(ELLIPSE_ATTR_FILL,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,ELLIPSE_ATTR_STROKE_WIDTH,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_STROKE_WIDTH,String.class),ss,ci)
//															: OnlineObjectGetter.<Stroke>forValue((Stroke)getter.getInstrument(ELLIPSE_ATTR_STROKE_WIDTH,props,Stroke.class))
							));
						}
						else {
							primitives.add(new EllipsePainter(XMLUtils.getAttribute(node,ELLIPSE_ATTR_CX,float.class)
														,XMLUtils.getAttribute(node,ELLIPSE_ATTR_CY,float.class)
														,XMLUtils.getAttribute(node,ELLIPSE_ATTR_RX,float.class)
														,XMLUtils.getAttribute(node,ELLIPSE_ATTR_RY,float.class)
														,(Color)getter.getInstrument(ELLIPSE_ATTR_STROKE,props,Color.class)
														,(Color)getter.getInstrument(ELLIPSE_ATTR_FILL,props,Color.class)
														,(Stroke)getter.getInstrument(ELLIPSE_ATTR_STROKE_WIDTH,props,Stroke.class))
							);
						}
						break;
					case "polyline"	:
// 						<polyline points="10,10 50,100 81,100 140,10" style="stroke:rgb(255,0,0);stroke-width:2" />
						if (SVGUtils.hasAnySubstitutions(props,POLYLINE_ATTRIBUTES)) {
							primitives.add(new DynamicPolylinePainter(
//														SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,POLYLINE_ATTR_POINTS,String.class))
//															? 
														SVGUtils.buildOnlineObjectGetter(Point2D[].class,XMLUtils.getAttribute(node,POLYLINE_ATTR_POINTS,String.class),ss,ci)
//															: OnlineObjectGetter.<Point2D[]>forValue((Point2D[])getter.getInstrument(POLYLINE_ATTR_POINTS,props,Point2D[].class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,POLYLINE_ATTR_STROKE,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,POLYLINE_ATTR_STROKE,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(POLYLINE_ATTR_STROKE,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,POLYLINE_ATTR_STROKE_WIDTH,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,POLYLINE_ATTR_STROKE_WIDTH,String.class),ss,ci)
//															: OnlineObjectGetter.<Stroke>forValue((Stroke)getter.getInstrument(POLYLINE_ATTR_STROKE_WIDTH,props,Stroke.class))
							));
						}
						else {
							primitives.add(new PolylinePainter(SVGUtils.extractPoints(XMLUtils.getAttribute(node,POLYLINE_ATTR_POINTS,String.class))
														,(Color)getter.getInstrument(POLYLINE_ATTR_STROKE,props,Color.class)
														,(Stroke)getter.getInstrument(POLYLINE_ATTR_STROKE_WIDTH,props,Stroke.class))
							);
						}
						break;
					case "polygon"	:
// 						<polyline points="10,10 50,100 81,100 140,10" style="stroke:rgb(255,0,0);stroke-width:2" />
						if (SVGUtils.hasAnySubstitutions(props,POLYGON_ATTRIBUTES)) {
							primitives.add(new DynamicPolygonPainter(
//														SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,POLYGON_ATTR_POINTS,String.class))
//															? 
														SVGUtils.buildOnlineObjectGetter(Point2D[].class,XMLUtils.getAttribute(node,POLYGON_ATTR_POINTS,String.class),ss,ci)
//															: OnlineObjectGetter.<Point2D[]>forValue((Point2D[])getter.getInstrument(POLYGON_ATTR_POINTS,props,Point2D[].class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,POLYGON_ATTR_STROKE,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,POLYGON_ATTR_STROKE,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(POLYGON_ATTR_STROKE,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,POLYGON_ATTR_FILL,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,POLYGON_ATTR_FILL,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(POLYGON_ATTR_FILL,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,POLYGON_ATTR_STROKE_WIDTH,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,POLYGON_ATTR_STROKE_WIDTH,String.class),ss,ci)
//															: OnlineObjectGetter.<Stroke>forValue((Stroke)getter.getInstrument(POLYGON_ATTR_STROKE_WIDTH,props,Stroke.class))
							));
						}
						else {
							primitives.add(new PolygonPainter(SVGUtils.extractPoints(XMLUtils.getAttribute(node,POLYGON_ATTR_POINTS,String.class))
														,(Color)getter.getInstrument(POLYGON_ATTR_STROKE,props,Color.class)
														,(Color)getter.getInstrument(POLYGON_ATTR_FILL,props,Color.class)
														,(Stroke)getter.getInstrument(POLYGON_ATTR_STROKE_WIDTH,props,Stroke.class))
							);
						}
						break;
					case "path"	:
// 						<path d="M 10,30 A 20,20 0,0,1 50,30 A 20,20 0,0,1 90,30 Q 90,60 50,90 Q 10,60 10,30 z" style="stroke:rgb(255,0,0);stroke-width:2" />
						if (SVGUtils.hasAnySubstitutions(props,PATH_ATTRIBUTES)) {
							primitives.add(new DynamicPathPainter(
//														SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,PATH_ATTR_D,String.class))
//															? 
														SVGUtils.buildOnlineObjectGetter(GeneralPath.class,XMLUtils.getAttribute(node,PATH_ATTR_D,String.class),ss,ci)
//															: OnlineObjectGetter.<GeneralPath>forValue((GeneralPath)getter.getInstrument(PATH_ATTR_D,props,GeneralPath.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,PATH_ATTR_STROKE,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,PATH_ATTR_STROKE,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(PATH_ATTR_STROKE,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,PATH_ATTR_STROKE,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,PATH_ATTR_FILL,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(PATH_ATTR_FILL,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,PATH_ATTR_STROKE_WIDTH,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,PATH_ATTR_STROKE_WIDTH,String.class),ss,ci)
//															: OnlineObjectGetter.<Stroke>forValue((Stroke)getter.getInstrument(PATH_ATTR_STROKE_WIDTH,props,Stroke.class))
														, SVGUtils.buildOnlineObjectGetter(AffineTransform.class,XMLUtils.getAttribute(node,PATH_ATTR_TRANSFORM,String.class),ss,ci)
							));
						}
						else {
							primitives.add(new PathPainter(SVGUtils.extractCommands(XMLUtils.getAttribute(node,PATH_ATTR_D,String.class))
														,(Color)getter.getInstrument(PATH_ATTR_STROKE,props,Color.class)
														,(Color)getter.getInstrument(PATH_ATTR_FILL,props,Color.class)
														,(Stroke)getter.getInstrument(PATH_ATTR_STROKE_WIDTH,props,Stroke.class)
														,(AffineTransform)getter.getInstrument(PATH_ATTR_TRANSFORM,props,AffineTransform.class))
							);
						}
						break;
					case "text"	:
						if (SVGUtils.hasAnySubstitutions(props,TEXT_ATTRIBUTES) || SVGUtils.hasSubstitutionInside(node.getTextContent())) {
							primitives.add(new DynamicTextPainter(
//														SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,TEXT_ATTR_X,String.class))
//															? 
														SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,TEXT_ATTR_X,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,TEXT_ATTR_X,float.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,TEXT_ATTR_Y,String.class))
//															? 
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,TEXT_ATTR_Y,String.class),ss)
//															: OnlineFloatGetter.forValue(XMLUtils.getAttribute(node,TEXT_ATTR_Y,float.class))
//														,SVGUtils.hasSubstitutionInside(node.getTextContent())
//															? 
														, SVGUtils.buildOnlineGetter(OnlineStringGetter.class,node.getTextContent(),ss)
//															: OnlineStringGetter.forValue(node.getTextContent())
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,TEXT_ATTR_FONT,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Font.class,SVGUtils.buildFontDescriptor(props),ss,ci)
//														, SVGUtils.buildOnlineObjectGetter(Font.class,XMLUtils.getAttribute(node,TEXT_ATTR_FONT,String.class),ss,ci)
//															: OnlineObjectGetter.<Font>forValue((Font)getter.getInstrument(TEXT_ATTR_FONT,props,Font.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,TEXT_ATTR_FILL,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,TEXT_ATTR_FILL,String.class),ss,ci)
//															: OnlineObjectGetter.<Color>forValue((Color)getter.getInstrument(TEXT_ATTR_FILL,props,Color.class))
//														,SVGUtils.hasSubstitutionInside(XMLUtils.getAttribute(node,TEXT_ATTR_TRANSFORM,String.class))
//															? 
														, SVGUtils.buildOnlineObjectGetter(AffineTransform.class,XMLUtils.getAttribute(node,TEXT_ATTR_TRANSFORM,String.class),ss,ci)
//															: OnlineObjectGetter.<AffineTransform>forValue((AffineTransform)getter.getInstrument(TEXT_ATTR_TRANSFORM,props,AffineTransform.class))
							));
						}
						else {
							primitives.add(new TextPainter(XMLUtils.getAttribute(node,TEXT_ATTR_X,float.class)
														,XMLUtils.getAttribute(node,TEXT_ATTR_Y,float.class)
														,node.getTextContent()
														,(Font)getter.getInstrument(TEXT_ATTR_FONT,props,Font.class)
														,(Color)getter.getInstrument(TEXT_ATTR_FILL,props,Color.class)
														,(AffineTransform)getter.getInstrument(TEXT_ATTR_TRANSFORM,props,AffineTransform.class))
							);
						}
						break;
				}
			}
			return ContinueMode.CONTINUE; 
		});
		return new SVGPainter(widthAndHeight[0], widthAndHeight[1], policy, primitives.toArray(new AbstractPainter[primitives.size()]));
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
