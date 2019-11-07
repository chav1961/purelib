package chav1961.purelib.ui.swing.useful.svg;

import java.awt.Color;
import java.awt.Font;
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

import chav1961.purelib.basic.interfaces.OnlineFloatGetter;
import chav1961.purelib.basic.interfaces.OnlineObjectGetter;
import chav1961.purelib.basic.interfaces.OnlineStringGetter;

public class SVGPainter {
	interface PrimitivePainter {
		void paint(Graphics2D g2d);
	}
	
	private final Color				background = Color.WHITE;
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
		final double			scaleX = 1.0*fillWidth/getWidth(), scaleY = 1.0*fillHeight/getHeight();  
		
		at.scale(scaleX,scaleY);
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
		private final Line2D						line;
		private final Color							drawColor;
		private final Stroke						stroke;
		
		LinePainter(final float x1, final float y1, final float x2, final float y2, final Color drawColor, final Stroke drawStroke) {
			this.drawColor = drawColor;
			this.stroke = drawStroke;
			this.line = new Line2D.Float(x1, y1, x2, y2);
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
			return "LinePainter [line=" + line + ", drawColor=" + drawColor + ", stroke=" + stroke + "]";
		}
	}

	protected static class DynamicLinePainter extends AbstractPainter {
		private final OnlineFloatGetter				x1, y1, x2, y2;
		private final OnlineObjectGetter<Color>		drawColorGetter;
		private final OnlineObjectGetter<Stroke>	strokeGetter;
		
		DynamicLinePainter(final OnlineFloatGetter x1, final OnlineFloatGetter y1, final OnlineFloatGetter x2, final OnlineFloatGetter y2, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Stroke> drawStrokeGetter) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.drawColorGetter = drawColorGetter;
			this.strokeGetter = drawStrokeGetter;
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			
			g2d.setColor(drawColorGetter.get());
			g2d.setStroke(strokeGetter.get());
			g2d.draw(new Line2D.Float(x1.get(), y1.get(), x2.get(), y2.get()));
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "DynamicLinePainter []";
		}
	}
	
	protected static class RectPainter extends AbstractPainter {
		private final RectangularShape	rect;
		private final Color		drawColor, fillColor;
		private final Stroke	stroke;
		
		RectPainter(final float x, final float y, final float w, final float h, final Color drawColor, final Stroke drawStroke) {
			this.rect = new Rectangle2D.Float(x, y, w, h);
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
		}

		RectPainter(final float x, final float y, final float w, final float h, final float rx, final float ry, final Color drawColor, final Stroke drawStroke) {
			this.rect = new RoundRectangle2D.Float(x, y, w, h, rx, ry);
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
		}

		RectPainter(final float x, final float y, final float w, final float h, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			this.rect = new Rectangle2D.Float(x, y, w, h);
			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = drawStroke;
		}

		RectPainter(final float x, final float y, final float w, final float h, final float rx, final float ry, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			this.rect = new RoundRectangle2D.Float(x, y, w, h, rx, ry);
			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = drawStroke;
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
			return "RectPainter [rect=" + rect + ", drawColor=" + drawColor + ", fillColor=" + fillColor + ", stroke=" + stroke + "]";
		}
	}

	protected static class DynamicRectPainter extends AbstractPainter {
		private final OnlineFloatGetter				x, y, w, h, rx, ry;
		private final OnlineObjectGetter<Color>		drawColor, fillColor;
		private final OnlineObjectGetter<Stroke>	stroke;
		
		DynamicRectPainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> stroke) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.rx = OnlineFloatGetter.forValue(0);
			this.ry = OnlineFloatGetter.forValue(0);
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = stroke;
		}

		DynamicRectPainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineFloatGetter rx, final OnlineFloatGetter ry, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> stroke) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.rx = rx;
			this.ry = ry;
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = stroke;
		}

		DynamicRectPainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Color> fillColor, final OnlineObjectGetter<Stroke> stroke) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.rx = OnlineFloatGetter.forValue(0);
			this.ry = OnlineFloatGetter.forValue(0);
			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = stroke;
		}

		DynamicRectPainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineFloatGetter rx, final OnlineFloatGetter ry, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Color> fillColor, final OnlineObjectGetter<Stroke> stroke) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.rx = rx;
			this.ry = ry;
			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = stroke;
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			final RectangularShape	rect = new RoundRectangle2D.Float(x.get(), y.get(), w.get(), h.get(), rx.get(), ry.get());

			if (fillColor != null) {
				g2d.setColor(fillColor.get());
				g2d.fill(rect);
			}
			g2d.setColor(drawColor.get());
			g2d.setStroke(stroke.get());
			g2d.draw(rect);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "DynamicRectPainter []";
		}
	}
	
	protected static class CirclePainter extends AbstractPainter {
		private final Ellipse2D.Float	ellipse;
		private final Color		drawColor, fillColor;
		private final Stroke	stroke;
		
		CirclePainter(final float x, final float y, final float r, final Color drawColor, final Stroke drawStroke) {
			this.ellipse = new Ellipse2D.Float(x-r, y-r, 2*r, 2*r);
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
		}

		CirclePainter(final float x, final float y, final float r, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			this.ellipse = new Ellipse2D.Float(x-r, y-r, 2*r, 2*r);
			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = drawStroke;
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
			return "CirclePainter [ellipse=" + ellipse + ", drawColor=" + drawColor + ", fillColor=" + fillColor + ", stroke=" + stroke + "]";
		}
	}

	protected static class DynamicCirclePainter extends AbstractPainter {
		private final OnlineFloatGetter				x, y, r;
		private final OnlineObjectGetter<Color>		drawColor, fillColor;
		private final OnlineObjectGetter<Stroke>	stroke;
		
		DynamicCirclePainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter r, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> drawStroke) {
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
			this.x = x;
			this.y = y;
			this.r = r;
		}

		DynamicCirclePainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter r, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Color> fillColor, final OnlineObjectGetter<Stroke> drawStroke) {
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
			final float				radius = r.get();
			final Ellipse2D			ellipse = new Ellipse2D.Float(x.get()-radius, y.get()-radius, 2*radius, 2*radius);
					
			if (fillColor != null) {
				g2d.setColor(fillColor.get());
				g2d.fill(ellipse);
			}
			g2d.setColor(drawColor.get());
			g2d.setStroke(stroke.get());
			g2d.draw(ellipse);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "DynamicCirclePainter []";
		}
	}
	
	protected static class EllipsePainter extends AbstractPainter {
		private final Ellipse2D.Float	ellipse;
		private final Color		drawColor, fillColor;
		private final Stroke	stroke;
		
		EllipsePainter(final float x, final float y, final float rx, final float ry, final Color drawColor, final Stroke drawStroke) {
			this.ellipse = new Ellipse2D.Float(x-rx, y-ry, 2*rx, 2*ry);
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
		}

		EllipsePainter(final float x, final float y, final float rx, final float ry, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			this.ellipse = new Ellipse2D.Float(x-rx, y-ry, 2*rx, 2*ry);
			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = drawStroke;
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
			return "EllipsePainter [ellipse=" + ellipse + ", drawColor=" + drawColor + ", fillColor=" + fillColor + ", stroke=" + stroke + "]";
		}
	}

	protected static class DynamicEllipsePainter extends AbstractPainter {
		private final OnlineFloatGetter				x, y, rx, ry;
		private final OnlineObjectGetter<Color>		drawColor, fillColor;
		private final OnlineObjectGetter<Stroke>	stroke;
		
		DynamicEllipsePainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter rx, final OnlineFloatGetter ry, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> drawStroke) {
			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
			this.x = x;
			this.y = y;
			this.rx = rx;
			this.ry = ry;
		}

		DynamicEllipsePainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter rx, final OnlineFloatGetter ry, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Color> fillColor, final OnlineObjectGetter<Stroke> drawStroke) {
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
			final Color		oldColor = g2d.getColor();
			final Stroke	oldStroke = g2d.getStroke();
			final float		radiusX = rx.get(), radiusY = ry.get();
			final Ellipse2D	ellipse = new Ellipse2D.Float(x.get()-radiusX, y.get()-radiusY, 2*radiusX, 2*radiusY);
			
			if (fillColor != null) {
				g2d.setColor(fillColor.get());
				g2d.fill(ellipse);
			}
			g2d.setColor(drawColor.get());
			g2d.setStroke(stroke.get());
			g2d.draw(ellipse);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "DynamicEllipsePainter []";
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

	protected static class DynamicPolylinePainter extends AbstractPainter {
		private final OnlineObjectGetter<Point2D[]>	points;
		private final OnlineObjectGetter<Color>		drawColor;
		private final OnlineObjectGetter<Stroke>	stroke;
		
		DynamicPolylinePainter(final OnlineObjectGetter<Point2D[]> points, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> drawStroke) {
			this.points = points;
 			this.drawColor = drawColor;
			this.stroke = drawStroke;
		}

		@Override
		public void paint(final Graphics2D g2d) {
			final Color			oldColor = g2d.getColor();
			final Stroke		oldStroke = g2d.getStroke();
			final GeneralPath	path = new GeneralPath();
			final Point2D[] 	pointList = points.get(); 
			
			path.moveTo(pointList[0].getX(),pointList[0].getY());
			for (int index = 1; index < pointList.length; index++) {
				path.lineTo(pointList[index].getX(),pointList[index].getY());
			}
			
			g2d.setColor(drawColor.get());
			g2d.setStroke(stroke.get());
			g2d.draw(path);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "DynamicPolylinePainter []";
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

		PolygonPainter(final Point2D[] points, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			this.path = new GeneralPath();			
			path.moveTo(points[0].getX(),points[0].getY());
			for (int index = 1; index < points.length; index++) {
				path.lineTo(points[index].getX(),points[index].getY());
			}
			path.closePath();
			
 			this.drawColor = drawColor;
			this.fillColor = fillColor;
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

	protected static class DynamicPolygonPainter extends AbstractPainter {
		private final OnlineObjectGetter<Point2D[]>	points;
		private final OnlineObjectGetter<Color>		drawColor, fillColor;
		private final OnlineObjectGetter<Stroke>	stroke;
		
		DynamicPolygonPainter(final OnlineObjectGetter<Point2D[]> points, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> drawStroke) {
			this.points = points;
 			this.drawColor = drawColor;
 			this.fillColor = null;
			this.stroke = drawStroke;
		}

		DynamicPolygonPainter(final OnlineObjectGetter<Point2D[]> points, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Color> fillColor, final OnlineObjectGetter<Stroke> drawStroke) {
			this.points = points;
 			this.drawColor = drawColor;
 			this.fillColor = fillColor;
			this.stroke = drawStroke;
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color			oldColor = g2d.getColor();
			final Stroke		oldStroke = g2d.getStroke();
			final GeneralPath	path = new GeneralPath();
			final Point2D[] 	pointList = points.get(); 
			
			path.moveTo(pointList[0].getX(),pointList[0].getY());
			for (int index = 1; index < pointList.length; index++) {
				path.lineTo(pointList[index].getX(),pointList[index].getY());
			}
			path.closePath();
			
			if (fillColor != null) {
				g2d.setColor(fillColor.get());
				g2d.fill(path);
			}
			
			g2d.setColor(drawColor.get());
			g2d.setStroke(stroke.get());
			g2d.draw(path);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "DynamicPolygonPainter []";
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

		PathPainter(final GeneralPath path, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			this.path = path;
 			this.drawColor = drawColor;
			this.fillColor = fillColor;
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

	protected static class DynamicPathPainter extends AbstractPainter {
		private final OnlineObjectGetter<GeneralPath>	path;
		private final OnlineObjectGetter<Color>			drawColor, fillColor;
		private final OnlineObjectGetter<Stroke>		stroke;
		
		DynamicPathPainter(final OnlineObjectGetter<GeneralPath> path, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> drawStroke) {
			this.path = path;
 			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
		}

		DynamicPathPainter(final OnlineObjectGetter<GeneralPath> path, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Color> fillColor, final OnlineObjectGetter<Stroke> drawStroke) {
			this.path = path;
 			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = drawStroke;
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color			oldColor = g2d.getColor();
			final Stroke		oldStroke = g2d.getStroke();
			final GeneralPath	path2Draw = path.get();
			
			if (fillColor != null) {
				g2d.setColor(fillColor.get());
				g2d.fill(path2Draw);
			}
			g2d.setColor(drawColor.get());
			g2d.setStroke(stroke.get());
			g2d.draw(path2Draw);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "DynamicPathPainter []";
		}
	}
	
	protected static class TextPainter extends AbstractPainter {
		private final float				x, y;
		private final String			text;
		private final Font				font;
		private final Color				drawColor;
		private final AffineTransform	transform;
		
		TextPainter(final float x, final float y, final String text, final Font font, final Color drawColor, final AffineTransform transform) {
			this.x = x;
			this.y = y;
			this.text = text;
			this.font = font;
 			this.drawColor = drawColor;
 			this.transform = transform;
		}

		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Font				oldFont = g2d.getFont();
			final AffineTransform	oldAt = g2d.getTransform(), clone = (AffineTransform)oldAt.clone();
			
			g2d.setColor(drawColor);
			g2d.setFont(font);
			clone.concatenate(transform);
			g2d.setTransform(clone);
			g2d.drawString(text, x, y);
			g2d.setTransform(oldAt);
			g2d.setFont(oldFont);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "TextPainter [x=" + x + ", y=" + y + ", text=" + text + ", font=" + font + ", drawColor=" + drawColor + ", transform=" + transform + "]";
		}
	}

	protected static class DynamicTextPainter extends AbstractPainter {
		private final OnlineFloatGetter						x, y;
		private final OnlineStringGetter					text;
		private final OnlineObjectGetter<Font>				font;
		private final OnlineObjectGetter<Color>				drawColor;
		private final OnlineObjectGetter<AffineTransform>	transform;
		
		DynamicTextPainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineStringGetter text, final OnlineObjectGetter<Font> font, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<AffineTransform> transform) {
			this.x = x;
			this.y = y;
			this.text = text;
			this.font = font;
 			this.drawColor = drawColor;
 			this.transform = transform;
		}

		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Font				oldFont = g2d.getFont();
			final AffineTransform	oldAt = g2d.getTransform(), clone = (AffineTransform)oldAt.clone();
			
			g2d.setColor(drawColor.get());
			g2d.setFont(font.get());
			clone.concatenate(transform.get());
			g2d.setTransform(clone);
			g2d.drawString(text.get(), x.get(), y.get());
			g2d.setTransform(oldAt);
			g2d.setFont(oldFont);
			g2d.setColor(oldColor);			
		}

		@Override
		public String toString() {
			return "DynamicTextPainter []";
		}
	}
}


