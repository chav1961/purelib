package chav1961.purelib.ui.swing;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.w3c.dom.Document;

public class SVGPainter {
	public void paint(final Graphics2D g2d) {
		final AffineTransform	oldAt = g2d.getTransform();
		
		g2d.setTransform(oldAt);
	}
	
	public SVGPainter buildPainter(final Document svgDescriptor) {
		return null;
	}
}
