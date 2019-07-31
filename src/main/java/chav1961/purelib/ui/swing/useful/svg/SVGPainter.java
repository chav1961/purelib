package chav1961.purelib.ui.swing.useful.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
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
		final AffineTransform	newAt = pickCoordinates(oldAt);
		
		for (AbstractPainter item : primitives) {
			item.paint(g2d);
		}
		
		g2d.setTransform(oldAt);
	}

	protected AffineTransform pickCoordinates(final AffineTransform oldAt) {
		// TODO Auto-generated method stub
		return oldAt;
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
//		private final float		x, y, rx, ry;
		private final GeneralPath	polyline;
		private final Color		drawColor, fillColor;
		private final Stroke	stroke;
		
		PolylinePainter(float x[], float y[], final Color drawColor, final Stroke drawStroke) {
			this.polyline = new GeneralPath();
 			this.drawColor = drawColor;
			this.fillColor = null;
			this.stroke = drawStroke;
		}
	}


	public static final void arcTo(GeneralPath path, float rx, float ry, float theta, boolean largeArcFlag, boolean sweepFlag, float x, float y) {
        // Ensure radii are valid
        if (rx == 0 || ry == 0) {
                path.lineTo(x, y);
                return;
        }
        // Get the current (x, y) coordinates of the path
        Point2D p2d = path.getCurrentPoint();
        float x0 = (float) p2d.getX();
        float y0 = (float) p2d.getY();
        // Compute the half distance between the current and the final point
        float dx2 = (x0 - x) / 2.0f;
        float dy2 = (y0 - y) / 2.0f;
        // Convert theta from degrees to radians
        theta = (float) Math.toRadians(theta % 360f);

        //
        // Step 1 : Compute (x1, y1)
        //
        float x1 = (float) (Math.cos(theta) * (double) dx2 + Math.sin(theta)
                        * (double) dy2);
        float y1 = (float) (-Math.sin(theta) * (double) dx2 + Math.cos(theta)
                        * (double) dy2);
        // Ensure radii are large enough
        rx = Math.abs(rx);
        ry = Math.abs(ry);
        float Prx = rx * rx;
        float Pry = ry * ry;
        float Px1 = x1 * x1;
        float Py1 = y1 * y1;
        double d = Px1 / Prx + Py1 / Pry;
        if (d > 1) {
                rx = Math.abs((float) (Math.sqrt(d) * (double) rx));
                ry = Math.abs((float) (Math.sqrt(d) * (double) ry));
                Prx = rx * rx;
                Pry = ry * ry;
        }

        //
        // Step 2 : Compute (cx1, cy1)
        //
        double sign = (largeArcFlag == sweepFlag) ? -1d : 1d;
        float coef = (float) (sign * Math
                        .sqrt(((Prx * Pry) - (Prx * Py1) - (Pry * Px1))
                                        / ((Prx * Py1) + (Pry * Px1))));
        float cx1 = coef * ((rx * y1) / ry);
        float cy1 = coef * -((ry * x1) / rx);

        //
        // Step 3 : Compute (cx, cy) from (cx1, cy1)
        //
        float sx2 = (x0 + x) / 2.0f;
        float sy2 = (y0 + y) / 2.0f;
        float cx = sx2
                        + (float) (Math.cos(theta) * (double) cx1 - Math.sin(theta)
                                        * (double) cy1);
        float cy = sy2
                        + (float) (Math.sin(theta) * (double) cx1 + Math.cos(theta)
                                        * (double) cy1);

        //
        // Step 4 : Compute the angleStart (theta1) and the angleExtent (dtheta)
        //
        float ux = (x1 - cx1) / rx;
        float uy = (y1 - cy1) / ry;
        float vx = (-x1 - cx1) / rx;
        float vy = (-y1 - cy1) / ry;
        float p, n;
        // Compute the angle start
        n = (float) Math.sqrt((ux * ux) + (uy * uy));
        p = ux; // (1 * ux) + (0 * uy)
        sign = (uy < 0) ? -1d : 1d;
        float angleStart = (float) Math.toDegrees(sign * Math.acos(p / n));
        // Compute the angle extent
        n = (float) Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1d : 1d;
        float angleExtent = (float) Math.toDegrees(sign * Math.acos(p / n));
        if (!sweepFlag && angleExtent > 0) {
                angleExtent -= 360f;
        } else if (sweepFlag && angleExtent < 0) {
                angleExtent += 360f;
        }
        angleExtent %= 360f;
        angleStart %= 360f;

        Arc2D.Float arc = new Arc2D.Float();
        arc.x = cx - rx;
        arc.y = cy - ry;
        arc.width = rx * 2.0f;
        arc.height = ry * 2.0f;
        arc.start = -angleStart;
        arc.extent = -angleExtent;
        path.append(arc, true);
	}
}


