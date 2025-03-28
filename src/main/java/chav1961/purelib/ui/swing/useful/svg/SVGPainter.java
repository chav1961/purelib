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

import chav1961.purelib.basic.CSSUtils.Unit;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.OnlineFloatGetter;
import chav1961.purelib.basic.interfaces.OnlineObjectGetter;
import chav1961.purelib.basic.interfaces.OnlineStringGetter;

public class SVGPainter {
	public enum FillPolicy {
		AS_IS, FILL_MIN, FILL_MAX, FILL_X, FILL_Y, FILL_BOTH
	}
	
	
	interface PrimitivePainter {
		void paint(Graphics2D g2d);
	}

	private final Color				background = Color.WHITE;
	private final int				width, height;
	private final Unit				unit;
	private final FillPolicy		policy;
	private final AbstractPainter[]	primitives;
	
	protected SVGPainter(final int width, final int height, final Unit unit, final FillPolicy policy, final AbstractPainter... primitives) {
		if (width <= 0) {
			throw new IllegalArgumentException("Width ["+width+"] must be positive"); 
		}
		else if (height <= 0) {
			throw new IllegalArgumentException("Height ["+height+"] must be positive"); 
		}
		else if (unit == null) {
			throw new NullPointerException("Unit can't be null"); 
		}
		else if (policy == null) {
			throw new NullPointerException("Fill policy can't be null"); 
		}
		else if (primitives == null || primitives.length == 0) {
			throw new IllegalArgumentException("Primitive list can't be null or empty"); 
		}
		else if (Utils.checkArrayContent4Nulls(primitives) != -1) {
			throw new IllegalArgumentException("Nulls in primitives list"); 
		}
		else {
			this.width = width;
			this.height = height;
			this.unit = unit;
			this.policy = policy;
			this.primitives = primitives;
		}
	}

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Unit getUnit() {
		return unit;
	}
	
	public void paint(final Graphics2D g2d, final int fillWidth, final int fillHeight) {
		final AffineTransform	oldAt = g2d.getTransform();

		g2d.setTransform(pickCoordinates(oldAt, fillWidth, fillHeight));
		paint(g2d);
		g2d.setTransform(oldAt);
	}

	public void paint(final Graphics2D g2d) {
		if (background != null) {
			fillBackground(g2d, background);
		}
		
		for (AbstractPainter item : primitives) {
			item.paint(g2d);
		}
	}
	
	protected AffineTransform pickCoordinates(final AffineTransform oldAt, final int fillWidth, final int fillHeight) {
		final AffineTransform	at = new AffineTransform(oldAt);
		double					scaleX = 1.0, scaleY = 1.0;  
		
		switch (policy) {
			case AS_IS	:
				scaleX = scaleY = 1.0;
				break;
			case FILL_BOTH:
				scaleX = 1.0*fillWidth/getWidth();
				scaleY = 1.0*fillHeight/getHeight();  
				break;
			case FILL_MAX:
				scaleX = 1.0*fillWidth/getWidth();
				scaleY = 1.0*fillHeight/getHeight();
				scaleX = scaleY = Math.max(scaleX,scaleY);
				break;
			case FILL_MIN:
				scaleX = 1.0*fillWidth/getWidth();
				scaleY = 1.0*fillHeight/getHeight();
				scaleX = scaleY = Math.min(scaleX,scaleY);
				break;
			case FILL_X:
				break;
			case FILL_Y:
				break;
			default:
				throw new UnsupportedOperationException("Fill policy ["+policy+"] is not implemented yet");
		}
		
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
			if (drawColor == null) {
				throw new NullPointerException("Draw color can't be null"); 
			}
			else if (drawStroke == null) {
				throw new NullPointerException("Draw stroke can't be null"); 
			}
			else {
				this.drawColor = drawColor;
				this.stroke = drawStroke;
				this.line = new Line2D.Float(x1, y1, x2, y2);
			}
		}

		@Override
		public void paint(final Graphics2D g2d) {
			if (g2d == null) {
				throw new NullPointerException("Graphic to draw can't be null"); 
			}
			else {
				final Color				oldColor = g2d.getColor();
				final Stroke			oldStroke = g2d.getStroke();
				
				g2d.setColor(drawColor);
				g2d.setStroke(stroke);
				g2d.draw(line);
				g2d.setStroke(oldStroke);
				g2d.setColor(oldColor);			
			}
		}

		@Override
		public String toString() {
			return "LinePainter [line=" + line + ", drawColor=" + drawColor + ", stroke=" + stroke + "]";
		}
	}

	protected static class DynamicLinePainter extends AbstractPainter {
		private final OnlineFloatGetter				x1, y1, x2, y2;
		private final Line2D						line;
		private final OnlineObjectGetter<Color>		drawColorGetter;
		private final Color							drawColor;
		private final OnlineObjectGetter<Stroke>	strokeGetter;
		private final Stroke						stroke;
		
		DynamicLinePainter(final OnlineFloatGetter x1, final OnlineFloatGetter y1, final OnlineFloatGetter x2, final OnlineFloatGetter y2, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Stroke> drawStrokeGetter) {
			if (x1 == null) {
				throw new NullPointerException("X1 getter can't be null");
			}
			else if (y1 == null) {
				throw new NullPointerException("Y1 getter can't be null");
			}
			else if (x2 == null) {
				throw new NullPointerException("X2 getter can't be null");
			}
			else if (y2 == null) {
				throw new NullPointerException("Y2 getter can't be null");
			}
			else if (drawColorGetter == null) {
				throw new NullPointerException("Draw color getter can't be null");
			}
			else if (drawStrokeGetter == null) {
				throw new NullPointerException("Stroke getter can't be null");
			}
			else {
				if (x1.isImmutable() && y1.isImmutable() && x2.isImmutable() && y2.isImmutable()) {
					this.line = new Line2D.Float(x1.get(), y1.get(), x2.get(), y2.get());
					this.x1 = this.y1 = this.x2 = this.y2 = null;    
				}
				else {
					this.line = null;
					this.x1 = x1;
					this.y1 = y1;
					this.x2 = x2;
					this.y2 = y2;
				}
				if (drawColorGetter.isImmutable()) {
					this.drawColor = drawColorGetter.get(); 
					this.drawColorGetter = null;
				}
				else {
					this.drawColor = null;
					this.drawColorGetter = drawColorGetter;
				}
				if (drawStrokeGetter.isImmutable()) {
					this.stroke = drawStrokeGetter.get();
					this.strokeGetter = null;
				}
				else {
					this.stroke = null;
					this.strokeGetter = drawStrokeGetter;
				}
			}
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			if (g2d == null) {
				throw new NullPointerException("Graphic to draw can't be null"); 
			}
			else {
				final Color				oldColor = g2d.getColor();
				final Stroke			oldStroke = g2d.getStroke();
				
				g2d.setColor(drawColor != null ? drawColor : drawColorGetter.get());
				g2d.setStroke(stroke != null ? stroke : strokeGetter.get());
				g2d.draw(line != null ? line : new Line2D.Float(x1.get(), y1.get(), x2.get(), y2.get()));
				g2d.setStroke(oldStroke);
				g2d.setColor(oldColor);
			}
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
			if (drawColor == null) {
				throw new NullPointerException("Draw color can't be null"); 
			}
			else if (drawStroke == null) {
				throw new NullPointerException("Draw stroke can't be null"); 
			}
			else {
				this.rect = new Rectangle2D.Float(x, y, w, h);
				this.drawColor = drawColor;
				this.fillColor = null;
				this.stroke = drawStroke;
			}
		}

		RectPainter(final float x, final float y, final float w, final float h, final float rx, final float ry, final Color drawColor, final Stroke drawStroke) {
			if (drawColor == null) {
				throw new NullPointerException("Draw color can't be null"); 
			}
			else if (drawStroke == null) {
				throw new NullPointerException("Draw stroke can't be null"); 
			}
			else {
				this.rect = new RoundRectangle2D.Float(x, y, w, h, rx, ry);
				this.drawColor = drawColor;
				this.fillColor = null;
				this.stroke = drawStroke;
			}
		}

		RectPainter(final float x, final float y, final float w, final float h, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			if (drawColor == null) {
				throw new NullPointerException("Draw color can't be null"); 
			}
			else if (drawStroke == null) {
				throw new NullPointerException("Draw stroke can't be null"); 
			}
			else {
				this.rect = new Rectangle2D.Float(x, y, w, h);
				this.drawColor = drawColor;
				this.fillColor = fillColor;
				this.stroke = drawStroke;
			}
		}

		RectPainter(final float x, final float y, final float w, final float h, final float rx, final float ry, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			if (drawColor == null) {
				throw new NullPointerException("Draw color can't be null"); 
			}
			else if (drawStroke == null) {
				throw new NullPointerException("Draw stroke can't be null"); 
			}
			else {
				this.rect = new RoundRectangle2D.Float(x, y, w, h, rx, ry);
				this.drawColor = drawColor;
				this.fillColor = fillColor;
				this.stroke = drawStroke;
			}
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
		private final RectangularShape				shape;
		private final OnlineObjectGetter<Color>		drawColorGetter, fillColorGetter;
		private final Color							drawColor, fillColor;		 
		private final OnlineObjectGetter<Stroke>	strokeGetter;
		private final Stroke						stroke;
		
		DynamicRectPainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> stroke) {
			this(x,y,w,h,OnlineFloatGetter.forValue(0),OnlineFloatGetter.forValue(0),drawColor,null,stroke);
		}

		DynamicRectPainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineFloatGetter rx, final OnlineFloatGetter ry, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> stroke) {
			this(x,y,w,h,rx,ry,drawColor,null,stroke);
		}

		DynamicRectPainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Color> fillColor, final OnlineObjectGetter<Stroke> stroke) {
			this(x,y,w,h,OnlineFloatGetter.forValue(0),OnlineFloatGetter.forValue(0),drawColor,fillColor,stroke);
		}

		DynamicRectPainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineFloatGetter rx, final OnlineFloatGetter ry, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Color> fillColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			if (x == null) {
				throw new NullPointerException("X getter can't be null");
			}
			else if (y == null) {
				throw new NullPointerException("Y getter can't be null");
			}
			else if (w == null) {
				throw new NullPointerException("W getter can't be null");
			}
			else if (h == null) {
				throw new NullPointerException("H getter can't be null");
			}
			else if (drawColorGetter == null) {
				throw new NullPointerException("Draw color getter can't be null");
			}
			else if (strokeGetter == null) {
				throw new NullPointerException("Stroke getter can't be null");
			}
			else {
				if (x.isImmutable() && y.isImmutable() && w.isImmutable() && h.isImmutable()) {
					this.shape = new RoundRectangle2D.Float(x.get(), y.get(), w.get(), h.get(), rx.get(), ry.get());
					this.x = this.y = this.w = this.h = this.rx = this.ry = null;
				}
				else {
					this.shape = null;
					this.x = x;
					this.y = y;
					this.w = w;
					this.h = h;
					this.rx = rx;
					this.ry = ry;
				}
				if (drawColorGetter.isImmutable()) {
					this.drawColor = drawColorGetter.get(); 
					this.drawColorGetter = null;
				}
				else {
					this.drawColor = null;
					this.drawColorGetter = drawColorGetter;
				}
				if (fillColorGetter != null) {
					if (fillColorGetter.isImmutable()) {
						this.fillColor = fillColorGetter.get(); 
						this.fillColorGetter = null;
					}
					else {
						this.fillColor = null;
						this.fillColorGetter = fillColorGetter;
					}
				}
				else {
					this.fillColor = null;
					this.fillColorGetter = null;
				}
				if (strokeGetter.isImmutable()) {
					this.stroke = strokeGetter.get();
					this.strokeGetter = null;
				}
				else {
					this.stroke = null;
					this.strokeGetter = strokeGetter;
				}
			}
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			final RectangularShape	rect = shape != null ? shape : new RoundRectangle2D.Float(x.get(), y.get(), w.get(), h.get(), rx.get(), ry.get());

			if (fillColor != null) {
				g2d.setColor(fillColor);
				g2d.fill(rect);
			}
			else if (fillColorGetter != null) {
				g2d.setColor(fillColorGetter.get());
				g2d.fill(rect);
			}
			g2d.setColor(drawColor != null ? drawColor : drawColorGetter.get());
			g2d.setStroke(stroke != null ? stroke : strokeGetter.get());
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
		private final Ellipse2D						ellipse;
		private final OnlineObjectGetter<Color>		drawColorGetter, fillColorGetter;
		private final Color							drawColor, fillColor;
		private final OnlineObjectGetter<Stroke>	strokeGetter;
		private final Stroke						stroke;
		
		DynamicCirclePainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter r, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> drawStroke) {
			this(x, y, r, drawColor, null, drawStroke);
		}

		DynamicCirclePainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter r, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Color> fillColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			if (x.isImmutable() && y.isImmutable() && r.isImmutable()) {
				final float				radius = r.get();
				
				this.ellipse = new Ellipse2D.Float(x.get()-radius, y.get()-radius, 2*radius, 2*radius);
				this.x = this.y = this.r = null;
			}
			else {
				this.ellipse = null;
				this.x = x;
				this.y = y;
				this.r = r;
			}
			if (drawColorGetter.isImmutable()) {
				this.drawColor = drawColorGetter.get(); 
				this.drawColorGetter = null;
			}
			else {
				this.drawColor = null;
				this.drawColorGetter = drawColorGetter;
			}
			if (fillColorGetter != null) {
				if (fillColorGetter.isImmutable()) {
					this.fillColor = fillColorGetter.get(); 
					this.fillColorGetter = null;
				}
				else {
					this.fillColor = null;
					this.fillColorGetter = fillColorGetter;
				}
			}
			else {
				this.fillColor = null;
				this.fillColorGetter = null;
			}
			if (strokeGetter.isImmutable()) {
				this.stroke = strokeGetter.get();
				this.strokeGetter = null;
			}
			else {
				this.stroke = null;
				this.strokeGetter = strokeGetter;
			}
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			final float				radius = r.get();
			final Ellipse2D			ell = ellipse != null ? ellipse : new Ellipse2D.Float(x.get()-radius, y.get()-radius, 2*radius, 2*radius);
					
			if (fillColor != null) {
				g2d.setColor(fillColor);
				g2d.fill(ell);
			}
			else if (fillColorGetter != null) {
				g2d.setColor(fillColorGetter.get());
				g2d.fill(ell);
			}
			g2d.setColor(drawColor != null ? drawColor : drawColorGetter.get());
			g2d.setStroke(stroke != null ? stroke : strokeGetter.get());
			g2d.draw(ell);
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
		private final Ellipse2D						ellipse;
		private final OnlineObjectGetter<Color>		drawColorGetter, fillColorGetter;
		private final Color							drawColor, fillColor;
		private final OnlineObjectGetter<Stroke>	strokeGetter;
		private final Stroke						stroke;
		
		DynamicEllipsePainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter rx, final OnlineFloatGetter ry, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> drawStroke) {
			this(x, y, rx, ry, drawColor, null, drawStroke);
		}

		DynamicEllipsePainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter rx, final OnlineFloatGetter ry, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Color> fillColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			if (x.isImmutable() && y.isImmutable() && rx.isImmutable() && ry.isImmutable()) {
				final float		radiusX = rx.get(), radiusY = ry.get();
				
				this.ellipse = new Ellipse2D.Float(x.get()-radiusX, y.get()-radiusY, 2*radiusX, 2*radiusY);
				this.x = this.y = this.rx = this.ry = null;    
			}
			else {
				this.ellipse = null;
				this.x = x;
				this.y = y;
				this.rx = rx;
				this.ry = ry;
			}
			if (drawColorGetter.isImmutable()) {
				this.drawColor = drawColorGetter.get(); 
				this.drawColorGetter = null;
			}
			else {
				this.drawColor = null;
				this.drawColorGetter = drawColorGetter;
			}
			if (fillColorGetter != null) {
				if (fillColorGetter.isImmutable()) {
					this.fillColor = fillColorGetter.get(); 
					this.fillColorGetter = null;
				}
				else {
					this.fillColor = null;
					this.fillColorGetter = fillColorGetter;
				}
			}
			else {
				this.fillColor = null;
				this.fillColorGetter = null;
			}
			if (strokeGetter.isImmutable()) {
				this.stroke = strokeGetter.get();
				this.strokeGetter = null;
			}
			else {
				this.stroke = null;
				this.strokeGetter = strokeGetter;
			}
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			final float				radiusX = rx.get(), radiusY = ry.get();
			final Ellipse2D			ell = ellipse != null ? ellipse : new Ellipse2D.Float(x.get()-radiusX, y.get()-radiusY, 2*radiusX, 2*radiusY);
					
			if (fillColor != null) {
				g2d.setColor(fillColor);
				g2d.fill(ell);
			}
			else if (fillColorGetter != null) {
				g2d.setColor(fillColorGetter.get());
				g2d.fill(ell);
			}
			g2d.setColor(drawColor != null ? drawColor : drawColorGetter.get());
			g2d.setStroke(stroke != null ? stroke : strokeGetter.get());
			g2d.draw(ell);
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
		private final OnlineObjectGetter<Point2D[]>	pointsGetter;
		private final Point2D[]						points;
		private final OnlineObjectGetter<Color>		drawColorGetter;
		private final Color							drawColor;
		private final OnlineObjectGetter<Stroke>	strokeGetter;
		private final Stroke						stroke;

		DynamicPolylinePainter(final OnlineObjectGetter<Point2D[]> pointsGetter, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			if (pointsGetter.isImmutable()) {
				this.points = pointsGetter.get();
				this.pointsGetter = null;
			}
			else {
				this.points = null;
				this.pointsGetter = pointsGetter;
			}
			if (drawColorGetter.isImmutable()) {
				this.drawColor = drawColorGetter.get(); 
				this.drawColorGetter = null;
			}
			else {
				this.drawColor = null;
				this.drawColorGetter = drawColorGetter;
			}
			if (strokeGetter.isImmutable()) {
				this.stroke = strokeGetter.get();
				this.strokeGetter = null;
			}
			else {
				this.stroke = null;
				this.strokeGetter = strokeGetter;
			}
		}

		@Override
		public void paint(final Graphics2D g2d) {
			final Color			oldColor = g2d.getColor();
			final Stroke		oldStroke = g2d.getStroke();
			final GeneralPath	path = new GeneralPath();
			final Point2D[] 	pointList = points != null ? points : pointsGetter.get(); 
			
			path.moveTo(pointList[0].getX(),pointList[0].getY());
			for (int index = 1; index < pointList.length; index++) {
				path.lineTo(pointList[index].getX(),pointList[index].getY());
			}
			
			g2d.setColor(drawColor != null ? drawColor : drawColorGetter.get());
			g2d.setStroke(stroke != null ? stroke : strokeGetter.get());
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
		private final OnlineObjectGetter<Point2D[]>	pointsGetter;
		private final Point2D[]						points;
		private final OnlineObjectGetter<Color>		drawColorGetter, fillColorGetter;
		private final Color							drawColor, fillColor;
		private final OnlineObjectGetter<Stroke>	strokeGetter;
		private final Stroke						stroke;
		
		DynamicPolygonPainter(final OnlineObjectGetter<Point2D[]> points, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> drawStroke) {
			this(points,drawColor,null,drawStroke);
		}

		DynamicPolygonPainter(final OnlineObjectGetter<Point2D[]> pointsGetter, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Color> fillColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			if (pointsGetter.isImmutable()) {
				this.points = pointsGetter.get();
				this.pointsGetter = null;
			}
			else {
				this.points = null;
				this.pointsGetter = pointsGetter;
			}
			if (drawColorGetter.isImmutable()) {
				this.drawColor = drawColorGetter.get(); 
				this.drawColorGetter = null;
			}
			else {
				this.drawColor = null;
				this.drawColorGetter = drawColorGetter;
			}
			if (fillColorGetter != null) {
				if (fillColorGetter.isImmutable()) {
					this.fillColor = fillColorGetter.get(); 
					this.fillColorGetter = null;
				}
				else {
					this.fillColor = null;
					this.fillColorGetter = fillColorGetter;
				}
			}
			else {
				this.fillColor = null;
				this.fillColorGetter = null;
			}
			if (strokeGetter.isImmutable()) {
				this.stroke = strokeGetter.get();
				this.strokeGetter = null;
			}
			else {
				this.stroke = null;
				this.strokeGetter = strokeGetter;
			}
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color			oldColor = g2d.getColor();
			final Stroke		oldStroke = g2d.getStroke();
			final GeneralPath	path = new GeneralPath();
			final Point2D[] 	pointList = points != null ? points : pointsGetter.get(); 
			
			path.moveTo(pointList[0].getX(),pointList[0].getY());
			for (int index = 1; index < pointList.length; index++) {
				path.lineTo(pointList[index].getX(),pointList[index].getY());
			}
			path.closePath();
			
			if (fillColor != null) {
				g2d.setColor(fillColor);
				g2d.fill(path);
			}
			else if (fillColorGetter != null) {
				g2d.setColor(fillColorGetter.get());
				g2d.fill(path);
			}
			
			g2d.setColor(drawColor != null ? drawColor : drawColorGetter.get());
			g2d.setStroke(stroke != null ? stroke : strokeGetter.get());
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
		private final GeneralPath		path;
		private final Color				drawColor, fillColor;
		private final Stroke			stroke;
		private final AffineTransform	transform;
		
		PathPainter(final GeneralPath path, final Color drawColor, final Stroke drawStroke, final AffineTransform transform) {
			this.path = path;
 			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
			this.transform = transform;
		}

		PathPainter(final GeneralPath path, final Color drawColor, final Color fillColor, final Stroke drawStroke, final AffineTransform transform) {
			this.path = path;
 			this.drawColor = drawColor;
			this.fillColor = fillColor;
			this.stroke = drawStroke;
			this.transform = transform;
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			final AffineTransform	oldTransform = g2d.getTransform();
			final AffineTransform	newTransform = new AffineTransform(oldTransform);
			
			newTransform.concatenate(transform);
			g2d.setTransform(newTransform);
			if (fillColor != null) {
				g2d.setColor(fillColor);
				g2d.fill(path);
			}
			g2d.setColor(drawColor);
			g2d.setStroke(stroke);
			g2d.draw(path);
			g2d.setTransform(oldTransform);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);
			
		}

		@Override
		public String toString() {
			return "PathPainter [path=" + path + ", drawColor=" + drawColor + ", fillColor=" + fillColor + ", stroke=" + stroke + "]";
		}
	}

	protected static class DynamicPathPainter extends AbstractPainter {
		private final OnlineObjectGetter<GeneralPath>		pathGetter;
		private final GeneralPath							path;
		private final OnlineObjectGetter<Color>				drawColorGetter, fillColorGetter;
		private final Color									drawColor, fillColor;
		private final OnlineObjectGetter<Stroke>			strokeGetter;
		private final Stroke								stroke;
		private final OnlineObjectGetter<AffineTransform>	transformGetter;
		private final AffineTransform						transform;
		
		DynamicPathPainter(final OnlineObjectGetter<GeneralPath> path, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> drawStroke, final OnlineObjectGetter<AffineTransform> drawTransformGetter) {
			this(path,drawColor,null,drawStroke,drawTransformGetter);
		}

		DynamicPathPainter(final OnlineObjectGetter<GeneralPath> pathGetter, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Color> fillColorGetter, final OnlineObjectGetter<Stroke> strokeGetter, final OnlineObjectGetter<AffineTransform> drawTransformGetter) {
			if (pathGetter.isImmutable()) {
				this.path = pathGetter.get();
				this.pathGetter = null;
			}
			else {
				this.path = null;
				this.pathGetter = pathGetter;
			}
			if (drawColorGetter.isImmutable()) {
				this.drawColor = drawColorGetter.get(); 
				this.drawColorGetter = null;
			}
			else {
				this.drawColor = null;
				this.drawColorGetter = drawColorGetter;
			}
			if (fillColorGetter != null) {
				if (fillColorGetter.isImmutable()) {
					this.fillColor = fillColorGetter.get(); 
					this.fillColorGetter = null;
				}
				else {
					this.fillColor = null;
					this.fillColorGetter = fillColorGetter;
				}
			}
			else {
				this.fillColor = null;
				this.fillColorGetter = null;
			}
			if (strokeGetter.isImmutable()) {
				this.stroke = strokeGetter.get();
				this.strokeGetter = null;
			}
			else {
				this.stroke = null;
				this.strokeGetter = strokeGetter;
			}
			if (drawTransformGetter.isImmutable()) {
				this.transform = drawTransformGetter.get();
				this.transformGetter = null;
			}
			else {
				this.transform = null;
				this.transformGetter = drawTransformGetter;
			}
		}
		
		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Stroke			oldStroke = g2d.getStroke();
			final AffineTransform	oldTransform = g2d.getTransform();
			final AffineTransform	newTransform = new AffineTransform(oldTransform);
			final GeneralPath		path2Draw = path != null ? path : pathGetter.get();

			if (transform != null) {
				newTransform.concatenate(transform);
			}
			else {
				newTransform.concatenate(transformGetter.get());
			}
			g2d.setTransform(newTransform);
			if (fillColor != null) {
				g2d.setColor(fillColor);
				g2d.fill(path2Draw);
			}
			else if (fillColorGetter != null) {
				g2d.setColor(fillColorGetter.get());
				g2d.fill(path2Draw);
			}
			
			g2d.setColor(drawColor != null ? drawColor : drawColorGetter.get());
			g2d.setStroke(stroke != null ? stroke : strokeGetter.get());
			g2d.draw(path2Draw);
			g2d.setTransform(oldTransform);
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
		private final OnlineStringGetter					textGetter;
		private final String								text;
		private final OnlineObjectGetter<Font>				fontGetter;
		private final Font									font;
		private final OnlineObjectGetter<Color>				drawColorGetter;
		private final Color									drawColor;
		private final OnlineObjectGetter<AffineTransform>	transformGetter;
		private final AffineTransform						transform;
		
		DynamicTextPainter(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineStringGetter textGetter, final OnlineObjectGetter<Font> fontGetter, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<AffineTransform> transformGetter) {
			this.x = x;
			this.y = y;
			if (textGetter.isImmutable()) {
				this.text = textGetter.get();
				this.textGetter = null;
			}
			else {
				this.text = null;
				this.textGetter = textGetter;
			}
			if (fontGetter.isImmutable()) {
				this.font = fontGetter.get();
				this.fontGetter = null;
			}
			else {
				this.font = null;
				this.fontGetter = fontGetter;
			}
			if (drawColorGetter.isImmutable()) {
				this.drawColor = drawColorGetter.get(); 
				this.drawColorGetter = null;
			}
			else {
				this.drawColor = null;
				this.drawColorGetter = drawColorGetter;
			}
			if (transformGetter.isImmutable()) {
				this.transform = transformGetter.get(); 
				this.transformGetter = null;
			}
			else {
				this.transform = null;
				this.transformGetter = transformGetter;
			}
		}

		@Override
		public void paint(final Graphics2D g2d) {
			final Color				oldColor = g2d.getColor();
			final Font				oldFont = g2d.getFont();
			final AffineTransform	oldAt = g2d.getTransform(), clone = (AffineTransform)oldAt.clone();
			
			g2d.setColor(drawColor != null ? drawColor : drawColorGetter.get());
			g2d.setFont(font != null ? font : fontGetter.get());
			clone.concatenate(transform != null ? transform : transformGetter.get());
			g2d.setTransform(clone);
			g2d.drawString(text != null ? text : textGetter.get(), x.get(), y.get());
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


