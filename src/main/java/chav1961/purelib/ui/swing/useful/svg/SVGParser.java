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
	private static final String		LINE_ATTR_Y1 = "y1";
	private static final String		LINE_ATTR_X2 = "x2";
	private static final String		LINE_ATTR_Y2 = "y2";
	private static final String		LINE_ATTR_STROKE = SVGUtils.ATTR_STROKE;
	private static final String		LINE_ATTR_STROKE_WIDTH = SVGUtils.ATTR_STROKE_WIDTH;
	private static final String		LINE_ATTR_STROKE_DASH_ARRAY = SVGUtils.ATTR_STROKE_DASH_ARRAY;
	private static final String[]	LINE_ATTRIBUTES = {LINE_ATTR_X1,LINE_ATTR_Y1,LINE_ATTR_X2,LINE_ATTR_Y2,LINE_ATTR_STROKE,LINE_ATTR_STROKE_WIDTH,LINE_ATTR_STROKE_DASH_ARRAY};
	
	private static final String		RECT_ATTR_X1 = "x1";
	private static final String		RECT_ATTR_Y1 = "y1";
	private static final String		RECT_ATTR_X2 = "x2";
	private static final String		RECT_ATTR_Y2 = "y2";
	private static final String		RECT_ATTR_STROKE = SVGUtils.ATTR_STROKE;
	private static final String		RECT_ATTR_FILL = SVGUtils.ATTR_FILL;
	private static final String		RECT_ATTR_STROKE_WIDTH = SVGUtils.ATTR_STROKE_WIDTH;
	private static final String[]	RECT_ATTRIBUTES = {RECT_ATTR_X1, RECT_ATTR_Y1, RECT_ATTR_X2, RECT_ATTR_Y2, RECT_ATTR_STROKE, RECT_ATTR_FILL, RECT_ATTR_STROKE_WIDTH};

	private static final String		CIRCLE_ATTR_CX = "cx";
	private static final String		CIRCLE_ATTR_CY = "cy";
	private static final String		CIRCLE_ATTR_R = "r";
	private static final String		CIRCLE_ATTR_STROKE = SVGUtils.ATTR_STROKE;
	private static final String		CIRCLE_ATTR_FILL = SVGUtils.ATTR_FILL;
	private static final String		CIRCLE_ATTR_STROKE_WIDTH = SVGUtils.ATTR_STROKE_WIDTH;
	private static final String[]	CIRCLE_ATTRIBUTES = {CIRCLE_ATTR_CX, CIRCLE_ATTR_CY, CIRCLE_ATTR_R, CIRCLE_ATTR_STROKE, CIRCLE_ATTR_FILL, CIRCLE_ATTR_STROKE_WIDTH};
	
	private static final String		ELLIPSE_ATTR_CX = "cx";
	private static final String		ELLIPSE_ATTR_CY = "cy";
	private static final String		ELLIPSE_ATTR_RX = "rx";
	private static final String		ELLIPSE_ATTR_RY = "ry";
	private static final String		ELLIPSE_ATTR_STROKE = SVGUtils.ATTR_STROKE;
	private static final String		ELLIPSE_ATTR_FILL = SVGUtils.ATTR_FILL;
	private static final String		ELLIPSE_ATTR_STROKE_WIDTH = SVGUtils.ATTR_STROKE_WIDTH;
	private static final String[]	ELLIPSE_ATTRIBUTES = {ELLIPSE_ATTR_CX, ELLIPSE_ATTR_CY, ELLIPSE_ATTR_RX, ELLIPSE_ATTR_RY, ELLIPSE_ATTR_STROKE, ELLIPSE_ATTR_FILL, ELLIPSE_ATTR_STROKE_WIDTH};

	private static final String		POLYLINE_ATTR_POINTS = SVGUtils.ATTR_POINTS;
	private static final String		POLYLINE_ATTR_STROKE = SVGUtils.ATTR_STROKE;
	private static final String		POLYLINE_ATTR_STROKE_WIDTH = SVGUtils.ATTR_STROKE_WIDTH;
	private static final String[]	POLYLINE_ATTRIBUTES = {POLYLINE_ATTR_POINTS, POLYLINE_ATTR_STROKE, POLYLINE_ATTR_STROKE_WIDTH};
	
	private static final String		POLYGON_ATTR_POINTS = SVGUtils.ATTR_POINTS;
	private static final String		POLYGON_ATTR_STROKE = SVGUtils.ATTR_STROKE;
	private static final String		POLYGON_ATTR_FILL = SVGUtils.ATTR_FILL;
	private static final String		POLYGON_ATTR_STROKE_WIDTH = SVGUtils.ATTR_STROKE_WIDTH;
	private static final String[]	POLYGON_ATTRIBUTES = {POLYGON_ATTR_POINTS, POLYGON_ATTR_STROKE, POLYGON_ATTR_FILL, POLYGON_ATTR_STROKE_WIDTH};

	private static final String		PATH_ATTR_D = "d";
	private static final String		PATH_ATTR_STROKE = SVGUtils.ATTR_STROKE;
	private static final String		PATH_ATTR_FILL = SVGUtils.ATTR_FILL;
	private static final String		PATH_ATTR_STROKE_WIDTH = SVGUtils.ATTR_STROKE_WIDTH;
	private static final String		PATH_ATTR_TRANSFORM = SVGUtils.ATTR_TRANSFORM;
	private static final String[]	PATH_ATTRIBUTES = {PATH_ATTR_D, PATH_ATTR_STROKE, PATH_ATTR_FILL, PATH_ATTR_STROKE_WIDTH, PATH_ATTR_TRANSFORM};

	private static final String		TEXT_ATTR_X = "x";
	private static final String		TEXT_ATTR_Y = "y";
	private static final String		TEXT_ATTR_FONT = SVGUtils.ATTR_FONT;
	private static final String		TEXT_ATTR_FILL = SVGUtils.ATTR_FILL;
	private static final String		TEXT_ATTR_TRANSFORM = SVGUtils.ATTR_TRANSFORM;
	private static final String[]	TEXT_ATTRIBUTES = {TEXT_ATTR_X, TEXT_ATTR_Y, TEXT_ATTR_FONT, TEXT_ATTR_FILL, TEXT_ATTR_TRANSFORM};
	
	
	@FunctionalInterface
	public interface InstrumentGetter<T> {
		T getInstrument(String propNames, Map<String,Object> attributes, Class<T> instrumentType) throws ContentException;
	}
	
	public static SVGPainter parse(final InputStream svgXml) throws NullPointerException, ContentException {
		return parse(svgXml,PureLibSettings.CURRENT_LOGGER,(propName,attributes,instrumentType)->{return instrumentType.cast(SVGUtils.extractInstrument(propName,attributes,instrumentType));},(src)->src,FillPolicy.FILL_BOTH);
	}

	public static SVGPainter parse(final InputStream svgXml, final InstrumentGetter<?> getter, final FillPolicy policy) throws NullPointerException, ContentException {
		return parse(svgXml,PureLibSettings.CURRENT_LOGGER,getter,(src)->src,policy);
	}

	public static SVGPainter parse(final InputStream svgXml, final SubstitutionSource ss) throws NullPointerException, ContentException {
		return parse(svgXml,PureLibSettings.CURRENT_LOGGER,(propNames,attributes,instrumentType)->{return SVGUtils.extractInstrument(propNames,attributes,instrumentType);},(src)->ss.getValue(src),FillPolicy.FILL_BOTH);
	}

	public static SVGPainter parse(final InputStream svgXml, final LoggerFacade logger, final InstrumentGetter<?> getter, final SubstitutionSource ss, final FillPolicy policy) throws NullPointerException, ContentException {
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
		final CSSUtils.Unit[]		units = new CSSUtils.Unit[1]; 
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
						units[0] = CSSUtils.Unit.detectUnit(XMLUtils.getAttribute(node, "width", String.class, ""), CSSUtils.Unit.PIXEL);
						widthAndHeight[0] = XMLUtils.getAttribute(node, "width", CSSUtils.BaseUnit.PIXEL, 0).intValue();
						widthAndHeight[1] = XMLUtils.getAttribute(node, "height", CSSUtils.BaseUnit.PIXEL, 0).intValue();
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
							primitives.add(new LinePainter(XMLUtils.getAttribute(node,LINE_ATTR_X1,float.class)*units[0].getKoeff()
													,XMLUtils.getAttribute(node,LINE_ATTR_Y1,float.class)*units[0].getKoeff()
													,XMLUtils.getAttribute(node,LINE_ATTR_X2,float.class)*units[0].getKoeff()
													,XMLUtils.getAttribute(node,LINE_ATTR_Y2,float.class)*units[0].getKoeff()
													,(Color)getter.getInstrument(LINE_ATTR_STROKE,props,Color.class)
													,(Stroke)getter.getInstrument(LINE_ATTR_STROKE_WIDTH,props,Stroke.class)) 
							);
						}
						break;
					case "rect"	:
// 						<rect width="100" height="50" x="0" y="0" rx="0" ry="0"  style="stroke:rgb(255,0,0);stroke-width:2" />
						if (SVGUtils.hasAnySubstitutions(props,RECT_ATTRIBUTES)) {
							primitives.add(new DynamicRectPainter(
														SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,RECT_ATTR_X1,String.class),ss)
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,RECT_ATTR_Y1,String.class),ss)
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,RECT_ATTR_X2,String.class),ss)
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,RECT_ATTR_Y2,String.class),ss)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,RECT_ATTR_STROKE,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,RECT_ATTR_FILL,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,RECT_ATTR_STROKE_WIDTH,String.class),ss,ci)
							));
						} 
						else {
							primitives.add(new RectPainter(XMLUtils.getAttribute(node,RECT_ATTR_X1,float.class)*units[0].getKoeff()
														,XMLUtils.getAttribute(node,RECT_ATTR_Y1,float.class)*units[0].getKoeff()
														,XMLUtils.getAttribute(node,RECT_ATTR_X2,float.class)*units[0].getKoeff()
														,XMLUtils.getAttribute(node,RECT_ATTR_Y2,float.class)*units[0].getKoeff()
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
														SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_CX,String.class),ss)
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_CY,String.class),ss)
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_R,String.class),ss)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_STROKE,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_FILL,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,CIRCLE_ATTR_STROKE_WIDTH,String.class),ss,ci)
							));
						}
						else {
							primitives.add(new CirclePainter(XMLUtils.getAttribute(node,CIRCLE_ATTR_CX,float.class)*units[0].getKoeff()
														,XMLUtils.getAttribute(node,CIRCLE_ATTR_CY,float.class)*units[0].getKoeff()
														,XMLUtils.getAttribute(node,CIRCLE_ATTR_R,float.class)*units[0].getKoeff()
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
														SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_CX,String.class),ss)
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_CY,String.class),ss)
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_RX,String.class),ss)
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_RY,String.class),ss)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_STROKE,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_FILL,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,ELLIPSE_ATTR_STROKE_WIDTH,String.class),ss,ci)
							));
						}
						else {
							primitives.add(new EllipsePainter(XMLUtils.getAttribute(node,ELLIPSE_ATTR_CX,float.class)*units[0].getKoeff()
														,XMLUtils.getAttribute(node,ELLIPSE_ATTR_CY,float.class)*units[0].getKoeff()
														,XMLUtils.getAttribute(node,ELLIPSE_ATTR_RX,float.class)*units[0].getKoeff()
														,XMLUtils.getAttribute(node,ELLIPSE_ATTR_RY,float.class)*units[0].getKoeff()
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
														SVGUtils.buildOnlineObjectGetter(Point2D[].class,XMLUtils.getAttribute(node,POLYLINE_ATTR_POINTS,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,POLYLINE_ATTR_STROKE,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,POLYLINE_ATTR_STROKE_WIDTH,String.class),ss,ci)
							));
						}
						else {
							primitives.add(new PolylinePainter(SVGUtils.extractPoints(XMLUtils.getAttribute(node,POLYLINE_ATTR_POINTS,String.class), units[0].getKoeff())
														,(Color)getter.getInstrument(POLYLINE_ATTR_STROKE,props,Color.class)
														,(Stroke)getter.getInstrument(POLYLINE_ATTR_STROKE_WIDTH,props,Stroke.class))
							);
						}
						break;
					case "polygon"	:
// 						<polyline points="10,10 50,100 81,100 140,10" style="stroke:rgb(255,0,0);stroke-width:2" />
						if (SVGUtils.hasAnySubstitutions(props,POLYGON_ATTRIBUTES)) {
							primitives.add(new DynamicPolygonPainter(
														SVGUtils.buildOnlineObjectGetter(Point2D[].class,XMLUtils.getAttribute(node,POLYGON_ATTR_POINTS,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,POLYGON_ATTR_STROKE,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,POLYGON_ATTR_FILL,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,POLYGON_ATTR_STROKE_WIDTH,String.class),ss,ci)
							));
						}
						else {
							primitives.add(new PolygonPainter(SVGUtils.extractPoints(XMLUtils.getAttribute(node,POLYGON_ATTR_POINTS,String.class), units[0].getKoeff())
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
														SVGUtils.buildOnlineObjectGetter(GeneralPath.class,XMLUtils.getAttribute(node,PATH_ATTR_D,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,PATH_ATTR_STROKE,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,PATH_ATTR_FILL,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Stroke.class,XMLUtils.getAttribute(node,PATH_ATTR_STROKE_WIDTH,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(AffineTransform.class,XMLUtils.getAttribute(node,PATH_ATTR_TRANSFORM,String.class),ss,ci)
							));
						}
						else {
							primitives.add(new PathPainter(SVGUtils.extractCommands(XMLUtils.getAttribute(node,PATH_ATTR_D,String.class), units[0].getKoeff())
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
														SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,TEXT_ATTR_X,String.class),ss)
														, SVGUtils.buildOnlineGetter(OnlineFloatGetter.class,XMLUtils.getAttribute(node,TEXT_ATTR_Y,String.class),ss)
														, SVGUtils.buildOnlineGetter(OnlineStringGetter.class,node.getTextContent(),ss)
														, SVGUtils.buildOnlineObjectGetter(Font.class,SVGUtils.buildFontDescriptor(props),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(Color.class,XMLUtils.getAttribute(node,TEXT_ATTR_FILL,String.class),ss,ci)
														, SVGUtils.buildOnlineObjectGetter(AffineTransform.class,XMLUtils.getAttribute(node,TEXT_ATTR_TRANSFORM,String.class),ss,ci)
							));
						}
						else {
							primitives.add(new TextPainter(XMLUtils.getAttribute(node,TEXT_ATTR_X,float.class)*units[0].getKoeff()
														,XMLUtils.getAttribute(node,TEXT_ATTR_Y,float.class)*units[0].getKoeff()
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
		return new SVGPainter(widthAndHeight[0], widthAndHeight[1], units[0], policy, primitives.toArray(new AbstractPainter[primitives.size()]));
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
