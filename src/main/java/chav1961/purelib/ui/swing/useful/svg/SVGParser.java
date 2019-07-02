package chav1961.purelib.ui.swing.useful.svg;

import java.io.InputStream;

import org.w3c.dom.Document;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.XSDCollection;

public class SVGParser {
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
			final GrowableByteArray	gba = new GrowableByteArray(false); 
			
			doc.normalizeDocument();
			Utils.walkDownXML(doc.getDocumentElement(),(mode,node)->{
				return ContinueMode.CONTINUE;
			});
		}
		
		return null;
	}
}
