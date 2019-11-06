package chav1961.purelib.ui.swing.useful.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

public class SVGPainter {
	interface PrimitivePainter {
		void paint(Graphics2D g2d);
	}
	
	private final Color				background = null;
	private final AbstractPainter[]	primitives;
	private final int				width, height;
	
	protected SVGPainter(final int width, final int height, final AbstractPainter... primitives) {
		if (width <= 0) {
			throw new IllegalArgumentException("Width ["+width+"] must be positive"); 
		}
		else if (height <= 0) {
			throw new IllegalArgumentException("Height ["+height+"] must be positive"); 
		}
		else if (primitives == null || primitives.length == 0) {
			throw new IllegalArgumentException("Primitive list can't be null or empty"); 
		}
		else {
			this.width = width;
			this.height = height;
			this.primitives = primitives;
		}
	}

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void paint(final Graphics2D g2d, final int fillWidth, final int fillHeight) {
		final AffineTransform	oldAt = g2d.getTransform();

		g2d.setTransform(pickCoordinates(oldAt,fillWidth,fillHeight));
		paint(g2d);
		g2d.setTransform(oldAt);
	}

	public void paint(final Graphics2D g2d) {
		if (background != null) {
			fillBackground(g2d,background);
		}
		
		for (AbstractPainter item : primitives) {
			item.paint(g2d);
		}
	}
	
	protected AffineTransform pickCoordinates(final AffineTransform oldAt, final int fillWidth, final int fillHeight) {
		final AffineTransform	at = new AffineTransform(oldAt);
		final double			scaleX = 1.0*getWidth()/fillWidth, scaleY = 1.0*getHeight()/fillHeight;  
		
		at.scale(scaleX,-scaleY);
		at.translate(0,scaleY);
		return at;
	}

	protected void fillBackground(final Graphics2D g2d, final Color background) {
		final Color			oldColor = g2d.getColor();
		final Rectangle2D	rect = new Rectangle2D.Float(0, 0, getWidth(), getHeight());
		
		g2d.setColor(background);
		g2d.fill(rect);
		g2d.setColor(oldColor);
	}

	protected static class AbstractPainter implements PrimitivePainter {
		@Override public void paint(final Graphics2D g2d) {}		
	}

	protected static class LinePainter extends AbstractPainter {
		private final float		x1, y1, x2, y2;
		private final Line2D.Float	line;
		private final Color		drawColor;
		private final Stroke	stroke;
		
		LinePainter(float x1, float y1, float x2, float y2, final Color drawColor, final Stroke drawStroke) {
			this.line = new Line2D.Float(x1, y1, x2, y2);
			this.drawColor = drawColor;
			this.stroke = drawStroke;
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			
			g2d.setColor(drawColor);
			g2d.setStroke(stroke);
			g2d.draw(line);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "LinePainter [x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2 + ", line=" + line + ", drawColor=" + drawColor + ", stroke=" + stroke + "]";
		}
	}

	protected static class RectPainter extends AbstractPainter {
		private final float		x, y, w, h, rx, ry;
		private final RectangularShape	rect;
		private final Color		drawColor, fillColor;
		private final Stroke	stroke;
		
		RectPainter(float x, float y, float w, float h, final Color drawColor, final Stroke drawStroke) {
			this.rect = new Rectangle2D.Float(x, y, w, h);
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.rx = 0;
			this.ry = 0;
		}

		RectPainter(float x, float y, float w, float h, float rx, float ry, final Color drawColor, final Stroke drawStroke) {
			this.rect = new RoundRectangle2D.Float(x, y, w, h, rx, ry);
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.rx = rx;
			this.ry = ry;
		}

		RectPainter(float x, float y, float w, float h, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			this.rect = new Rectangle2D.Float(x, y, w, h);
			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = drawStroke;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.rx = 0;
			this.ry = 0;
		}

		RectPainter(float x, float y, float w, float h, float rx, float ry, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			this.rect = new RoundRectangle2D.Float(x, y, w, h, rx, ry);
			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = drawStroke;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.rx = rx;
			this.ry = ry;
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();

			if (fillColor != null) {
				g2d.setColor(fillColor);
				g2d.fill(rect);
			}
			g2d.setColor(drawColor);
			g2d.setStroke(stroke);
			g2d.draw(rect);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "RectPainter [x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + ", rx=" + rx + ", ry=" + ry + ", rect=" + rect + ", drawColor=" + drawColor + ", fillColor=" + fillColor + ", stroke=" + stroke + "]";
		}
	}
	
	protected static class CirclePainter extends AbstractPainter {
		private final float		x, y, r;
		private final Ellipse2D.Float	ellipse;
		private final Color		drawColor, fillColor;
		private final Stroke	stroke;
		
		CirclePainter(float x, float y, float r, final Color drawColor, final Stroke drawStroke) {
			this.ellipse = new Ellipse2D.Float(x-r, y-r, 2*r, 2*r);
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
			this.x = x;
			this.y = y;
			this.r = r;
		}

		CirclePainter(float x, float y, float r, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			this.ellipse = new Ellipse2D.Float(x-r, y-r, 2*r, 2*r);
			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = drawStroke;
			this.x = x;
			this.y = y;
			this.r = r;
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			
			if (fillColor != null) {
				g2d.setColor(fillColor);
				g2d.fill(ellipse);
			}
			g2d.setColor(drawColor);
			g2d.setStroke(stroke);
			g2d.draw(ellipse);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "CirclePainter [x=" + x + ", y=" + y + ", r=" + r + ", drawColor=" + drawColor + ", fillColor=" + fillColor + ", stroke=" + stroke + "]";
		}
	}

	protected static class EllipsePainter extends AbstractPainter {
		private final float		x, y, rx, ry;
		private final Ellipse2D.Float	ellipse;
		private final Color		drawColor, fillColor;
		private final Stroke	stroke;
		
		EllipsePainter(float x, float y, float rx, float ry, final Color drawColor, final Stroke drawStroke) {
			this.ellipse = new Ellipse2D.Float(x-rx, y-ry, 2*rx, 2*ry);
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
			this.x = x;
			this.y = y;
			this.rx = rx;
			this.ry = ry;
		}

		EllipsePainter(float x, float y, float rx, float ry, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			this.ellipse = new Ellipse2D.Float(x-rx, y-ry, 2*rx, 2*ry);
			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = drawStroke;
			this.x = x;
			this.y = y;
			this.rx = rx;
			this.ry = ry;
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			
			if (fillColor != null) {
				g2d.setColor(fillColor);
				g2d.fill(ellipse);
			}
			g2d.setColor(drawColor);
			g2d.setStroke(stroke);
			g2d.draw(ellipse);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "EllipsePainter [x=" + x + ", y=" + y + ", rx=" + rx + ", ry=" + ry + ", ellipse=" + ellipse + ", drawColor=" + drawColor + ", fillColor=" + fillColor + ", stroke=" + stroke + "]";
		}
	}

	protected static class PolylinePainter extends AbstractPainter {
		private final GeneralPath	path;
		private final Color			drawColor, fillColor;
		private final Stroke		stroke;
		
		PolylinePainter(final Point2D[] points, final Color drawColor, final Stroke drawStroke) {
			this.path = new GeneralPath();			
			path.moveTo(points[0].getX(),points[0].getY());
			for (int index = 1; index < points.length; index++) {
				path.lineTo(points[index].getX(),points[index].getY());
			}
			
 			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
		}

		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			
			g2d.setColor(drawColor);
			g2d.setStroke(stroke);
			g2d.draw(path);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "PolylinePainter [path=" + path + ", drawColor=" + drawColor + ", fillColor=" + fillColor + ", stroke=" + stroke + "]";
		}
	}

	protected static class PolygonPainter extends AbstractPainter {
		private final GeneralPath	path;
		private final Color			drawColor, fillColor;
		private final Stroke		stroke;
		
		PolygonPainter(final Point2D[] points, final Color drawColor, final Stroke drawStroke) {
			this.path = new GeneralPath();			
			path.moveTo(points[0].getX(),points[0].getY());
			for (int index = 1; index < points.length; index++) {
				path.lineTo(points[index].getX(),points[index].getY());
			}
			path.closePath();
			
 			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
		}

		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			
			if (fillColor != null) {
				g2d.setColor(fillColor);
				g2d.fill(path);
			}
			g2d.setColor(drawColor);
			g2d.setStroke(stroke);
			g2d.draw(path);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "PolygonPainter [path=" + path + ", drawColor=" + drawColor + ", fillColor=" + fillColor + ", stroke=" + stroke + "]";
		}
	}

	protected static class PathPainter extends AbstractPainter {
		private final GeneralPath	path;
		private final Color			drawColor, fillColor;
		private final Stroke		stroke;
		
		PathPainter(final GeneralPath path, final Color drawColor, final Stroke drawStroke) {
			this.path = path;
 			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
		}

		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			
			if (fillColor != null) {
				g2d.setColor(fillColor);
				g2d.fill(path);
			}
			g2d.setColor(drawColor);
			g2d.setStroke(stroke);
			g2d.draw(path);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "PathPainter [path=" + path + ", drawColor=" + drawColor + ", fillColor=" + fillColor + ", stroke=" + stroke + "]";
		}
	}
}


