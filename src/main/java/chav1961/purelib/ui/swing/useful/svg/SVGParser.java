package chav1961.purelib.ui.swing.useful.svg;

import java.awt.Color;
import java.awt.Stroke;
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
import chav1961.purelib.ui.swing.useful.svg.SVGPainter.LinePainter;

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
	
	
	private static SVGPainter buildPainter(final Element root) {
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
}
