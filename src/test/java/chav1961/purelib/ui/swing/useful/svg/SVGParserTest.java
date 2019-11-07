package chav1961.purelib.ui.swing.useful.svg;

import org.junit.Assert;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;

public class SVGParserTest {

	@Test
	public void visualTest() throws IOException, ContentException {
		try(final InputStream	is = SVGParserTest.class.getResourceAsStream("svgtest.SVG")) {
			final JSVGComponent	jc = new JSVGComponent(is);
			
			jc.setPreferredSize(new Dimension(800,600));
			JOptionPane.showMessageDialog(null,jc);
		}
	}
	
}


class JSVGComponent extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private final SVGPainter	painter;
	
	public JSVGComponent(final InputStream is) throws ContentException {
		this.painter = SVGParser.parse(is);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		final Dimension	pref = getPreferredSize();
		
		painter.paint((Graphics2D)g,pref.width,pref.height);
	}
}