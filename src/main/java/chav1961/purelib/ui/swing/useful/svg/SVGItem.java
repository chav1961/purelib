package chav1961.purelib.ui.swing.useful.svg;

import java.awt.Color;
import java.awt.Stroke;

import chav1961.purelib.basic.interfaces.OnlineFloatGetter;
import chav1961.purelib.basic.interfaces.OnlineObjectGetter;

public abstract class SVGItem {
	public static enum SVGItemType {
		LINE,
		RECTANGLE,
		CIRCLE,
		ELLIPSE,
		POLYLINE,
		POLYGON,
		PATH,
		TEXT
	}
	
	private final SVGItemType	type;
	
	protected SVGItem(final SVGItemType type) {
		this.type = type;
	}
	
	public SVGItemType getType() {
		return type;
	}
	
	public abstract SVGPrimitivePainter getPainter();
	
	public static class Line extends SVGItem {
		private final boolean	dynamicParameters;
		private final float		x1, y1, x2, y2;
		private final Color		color;
		private final Stroke	stroke;
		private final OnlineFloatGetter	x1Getter;
		private final OnlineFloatGetter y1Getter;
		private final OnlineFloatGetter x2Getter;
		private final OnlineFloatGetter y2Getter;
		private final OnlineObjectGetter<Color> 	colorGetter;
		private final OnlineObjectGetter<Stroke> 	strokeGetter;
		private final SVGPrimitivePainter	painter;

		public Line(final float x1, final float y1, final float x2, final float y2, final Color drawColor, final Stroke drawStroke) {
			super(SVGItemType.LINE);
			
			if (drawColor == null) {
				throw new NullPointerException("Draw color can't be null"); 
			}
			else if (drawStroke == null) {
				throw new NullPointerException("Draw stroke can't be null"); 
			}
			else {
				this.dynamicParameters = false;
				this.x1 = x1;
				this.y1 = y1;
				this.x2 = x2;
				this.y2 = y2;
				this.color = drawColor;
				this.stroke = drawStroke;
				this.x1Getter = this.y1Getter = this.x2Getter = this.y2Getter = null;
				this.colorGetter = null;
				this.strokeGetter = null;
				this.painter = new SVGPainter.LinePainter(x1, y1, x2, y2, drawColor, drawStroke);
			}
		}

		public Line(final OnlineFloatGetter x1, final OnlineFloatGetter y1, final OnlineFloatGetter x2, final OnlineFloatGetter y2, final OnlineObjectGetter<Color> drawColor, final OnlineObjectGetter<Stroke> drawStroke) {
			super(SVGItemType.LINE);
			
			if (drawColor == null) {
				throw new NullPointerException("Draw color can't be null"); 
			}
			else if (drawStroke == null) {
				throw new NullPointerException("Draw stroke can't be null"); 
			}
			else {
				this.dynamicParameters = true;
				this.x1 = this.y1 = this.x2 = this.y2 = 0;
				this.color = null;
				this.stroke = null;
				this.x1Getter = x1; 
				this.y1Getter = y1;
				this.x2Getter = x2;
				this.y2Getter = y2;
				this.colorGetter = drawColor;
				this.strokeGetter = drawStroke;
				this.painter = new SVGPainter.DynamicLinePainter(x1, y1, x2, y2, drawColor, drawStroke);
			}
		}
		
		public float getX1() {
			return dynamicParameters ? x1Getter.get() : x1;
		}

		public float getY1() {
			return dynamicParameters ? y1Getter.get() : y1;
		}

		public float getX2() {
			return dynamicParameters ? x2Getter.get() : x2;
		}

		public float getY2() {
			return dynamicParameters ? y2Getter.get() : y2;
		}
		
		public Color getColor() {
			return dynamicParameters ? colorGetter.get() : color;
		}
		
		public Stroke getStroke() {
			return dynamicParameters ? strokeGetter.get() : stroke;
		}

		@Override
		public SVGPrimitivePainter getPainter() {
			return painter;
		}
	}

	public static class Rectangle extends SVGItem {
		private final boolean	dynamicParameters;
		private final float		x, y, w, h;
		private final float		rx, ry;
		private final Color		color;
		private final Color		fillColor;
		private final Stroke	stroke;
		private final OnlineFloatGetter	xGetter;
		private final OnlineFloatGetter yGetter;
		private final OnlineFloatGetter wGetter;
		private final OnlineFloatGetter hGetter;
		private final OnlineFloatGetter	rxGetter;
		private final OnlineFloatGetter ryGetter;
		private final OnlineObjectGetter<Color> 	colorGetter;
		private final OnlineObjectGetter<Color> 	fillColorGetter;
		private final OnlineObjectGetter<Stroke> 	strokeGetter;
		private final SVGPrimitivePainter	painter;

		public Rectangle(final float x, final float y, final float w, final float h, final Color drawColor, final Stroke drawStroke) {
			this(x, y, w, h, 0, 0, drawColor, null, drawStroke);
		}		

		public Rectangle(final float x, final float y, final float w, final float h, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			this(x, y, w, h, 0, 0, drawColor, fillColor, drawStroke);
		}		

		public Rectangle(final float x, final float y, final float w, final float h, final float rx, final float ry, final Color drawColor, final Stroke drawStroke) {
			this(x, y, w, h, rx, ry, drawColor, null, drawStroke);
		}
		
		public Rectangle(final float x, final float y, final float w, final float h, final float rx, final float ry, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			super(SVGItemType.RECTANGLE);
			if (drawColor == null) {
				throw new NullPointerException("Draw color can't be null"); 
			}
			else if (drawStroke == null) {
				throw new NullPointerException("Draw stroke can't be null"); 
			}
			else {
				this.dynamicParameters = false;
				this.x = x;
				this.y = y;
				this.w = w;
				this.h = h;
				this.rx = rx;
				this.ry = ry;
				this.color = drawColor;
				this.fillColor = fillColor;
				this.stroke = drawStroke;
				this.xGetter = this.yGetter = this.wGetter = this.hGetter = this.rxGetter = this.ryGetter = null;
				this.colorGetter = this.fillColorGetter = null;
				this.strokeGetter = null;
				this.painter = new SVGPainter.RectPainter(x, y, w, h, drawColor, drawStroke);
			}
		}

		public Rectangle(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			this(x, y, w, h, OnlineFloatGetter.forValue(0), OnlineFloatGetter.forValue(0), drawColorGetter, OnlineObjectGetter.<Color>forValue(null), strokeGetter);
		}		
		
		public Rectangle(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Color> fillColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			this(x, y, w, h, OnlineFloatGetter.forValue(0), OnlineFloatGetter.forValue(0), drawColorGetter, fillColorGetter, strokeGetter);
		}		
		
		public Rectangle(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineFloatGetter rx, final OnlineFloatGetter ry, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			this(x, y, w, h, rx, ry, drawColorGetter, null, strokeGetter);
		}		
		
		public Rectangle(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter w, final OnlineFloatGetter h, final OnlineFloatGetter rx, final OnlineFloatGetter ry, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Color> fillColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			super(SVGItemType.RECTANGLE);

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
			else if (rx == null) {
				throw new NullPointerException("RX getter can't be null");
			}
			else if (ry == null) {
				throw new NullPointerException("RY getter can't be null");
			}
			else if (drawColorGetter == null) {
				throw new NullPointerException("Draw color can't be null"); 
			}
			else if (strokeGetter == null) {
				throw new NullPointerException("Draw stroke can't be null"); 
			}
			else {
				this.dynamicParameters = true;
				this.x = this.y = this.w = this.h = 0;
				this.rx = this.ry = 0;
				this.color = this.fillColor = null;
				this.stroke = null;
				this.xGetter = x; 
				this.yGetter = y;
				this.wGetter = w;
				this.hGetter = h;
				this.rxGetter = rx; 
				this.ryGetter = ry;
				this.colorGetter = drawColorGetter;
				this.fillColorGetter = fillColorGetter;
				this.strokeGetter = strokeGetter;
				this.painter = new SVGPainter.DynamicRectPainter(x, y, w, h, rx, ry, drawColorGetter, fillColorGetter, strokeGetter);
			}
		}
		
		public float getX() {
			return dynamicParameters ? xGetter.get() : x;
		}

		public float getY() {
			return dynamicParameters ? yGetter.get() : y;
		}

		public float getW() {
			return dynamicParameters ? wGetter.get() : w;
		}

		public float getH() {
			return dynamicParameters ? hGetter.get() : h;
		}

		public float getRx() {
			return dynamicParameters ? rxGetter.get() : rx;
		}

		public float getRy() {
			return dynamicParameters ? ryGetter.get() : ry;
		}

		public Color getColor() {
			return dynamicParameters ? colorGetter.get() : color;
		}

		public Color getFillColor() {
			return dynamicParameters ? (fillColorGetter != null ? fillColorGetter.get() : null) : fillColor;
		}

		public Stroke getStroke() {
			return dynamicParameters ? strokeGetter.get() : stroke;
		}

		@Override
		public SVGPrimitivePainter getPainter() {
			return painter;
		}
	}

	public static class Circle extends SVGItem {
		private final boolean	dynamicParameters;
		private final float		x, y, r;
		private final Color		color;
		private final Color		fillColor;
		private final Stroke	stroke;
		private final OnlineFloatGetter	xGetter;
		private final OnlineFloatGetter yGetter;
		private final OnlineFloatGetter rGetter;
		private final OnlineObjectGetter<Color> 	colorGetter;
		private final OnlineObjectGetter<Color> 	fillColorGetter;
		private final OnlineObjectGetter<Stroke> 	strokeGetter;
		private final SVGPrimitivePainter	painter;

		public Circle(final float x, final float y, final float r, final Color drawColor, final Stroke drawStroke) {
			this(x, y, r, drawColor, null, drawStroke);
		}		
		
		public Circle(final float x, final float y, final float r, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			super(SVGItemType.CIRCLE);
			if (drawColor == null) {
				throw new NullPointerException("Draw color can't be null");
			}
			else if (drawStroke == null) {
				throw new NullPointerException("Stroke can't be null");
			}
			else {
				this.dynamicParameters = false;
				this.x = x;
				this.y = y;
				this.r = r;
				this.color = drawColor;
				this.fillColor = fillColor;
				this.stroke = drawStroke;
				this.xGetter = this.yGetter = this.rGetter = null;
				this.colorGetter = this.fillColorGetter = null;
				this.strokeGetter = null;
				this.painter = new SVGPainter.CirclePainter(x, y, r, drawColor, fillColor, drawStroke);
			}			
		}

		public Circle(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter r, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			this(x, y, r, drawColorGetter, null, strokeGetter);
		}
		
		public Circle(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter r, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Color> fillColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			super(SVGItemType.CIRCLE);
			if (x == null) {
				throw new NullPointerException("X getter can't be null");
			}
			else if (y == null) {
				throw new NullPointerException("Y getter can't be null");
			}
			else if (r == null) {
				throw new NullPointerException("R getter can't be null");
			}
			else if (drawColorGetter == null) {
				throw new NullPointerException("Draw color getter can't be null");
			}
			else if (strokeGetter == null) {
				throw new NullPointerException("Stroke getter can't be null");
			}
			else {
				this.dynamicParameters = true;
				this.x = this.y = this.r = 0;
				this.color = this.fillColor = null;
				this.stroke = null;
				this.xGetter = x;
				this.yGetter = y;
				this.rGetter = r;
				this.colorGetter = drawColorGetter; 
				this.fillColorGetter = fillColorGetter;
				this.strokeGetter = strokeGetter;
				this.painter = new SVGPainter.DynamicCirclePainter(x, y, r, drawColorGetter, fillColorGetter, strokeGetter);
			}
		}

		public float getX() {
			return dynamicParameters ? xGetter.get() : x;
		}

		public float getY() {
			return dynamicParameters ? yGetter.get() : y;
		}

		public float getR() {
			return dynamicParameters ? rGetter.get() : r;
		}

		public Color getColor() {
			return dynamicParameters ? colorGetter.get() : color;
		}

		public Color getFillColor() {
			return dynamicParameters ? (fillColorGetter != null ? fillColorGetter.get() : null) : fillColor;
		}

		public Stroke getStroke() {
			return dynamicParameters ? strokeGetter.get() : stroke;
		}

		@Override
		public SVGPrimitivePainter getPainter() {
			return painter;
		}
	}

	public static class Ellipse extends SVGItem {
		private final boolean	dynamicParameters;
		private final float		x, y, rx, ry;
		private final Color		color;
		private final Color		fillColor;
		private final Stroke	stroke;
		private final OnlineFloatGetter	xGetter;
		private final OnlineFloatGetter yGetter;
		private final OnlineFloatGetter rxGetter;
		private final OnlineFloatGetter ryGetter;
		private final OnlineObjectGetter<Color> 	colorGetter;
		private final OnlineObjectGetter<Color> 	fillColorGetter;
		private final OnlineObjectGetter<Stroke> 	strokeGetter;
		private final SVGPrimitivePainter	painter;

		public Ellipse(final float x, final float y, final float rx, final float ry, final Color drawColor, final Stroke drawStroke) {
			this(x, y, rx, ry, drawColor, null, drawStroke);
		}		
		
		public Ellipse(final float x, final float y, final float rx, final float ry, final Color drawColor, final Color fillColor, final Stroke drawStroke) {
			super(SVGItemType.ELLIPSE);
			if (drawColor == null) {
				throw new NullPointerException("Draw color can't be null");
			}
			else if (drawStroke == null) {
				throw new NullPointerException("Stroke can't be null");
			}
			else {
				this.dynamicParameters = false;
				this.x = x;
				this.y = y;
				this.rx = rx;
				this.ry = ry;
				this.color = drawColor;
				this.fillColor = fillColor;
				this.stroke = drawStroke;
				this.xGetter = this.yGetter = this.rxGetter = this.ryGetter = null;
				this.colorGetter = this.fillColorGetter = null;
				this.strokeGetter = null;
				this.painter = new SVGPainter.EllipsePainter(x, y, rx, ry, drawColor, fillColor, drawStroke);
			}			
		}

		public Ellipse(final OnlineFloatGetter x, final OnlineFloatGetter y, final OnlineFloatGetter rx, final OnlineFloatGetter ry, final OnlineObjectGetter<Color> drawColorGetter, final OnlineObjectGetter<Color> fillColorGetter, final OnlineObjectGetter<Stroke> strokeGetter) {
			super(SVGItemType.ELLIPSE);
			if (x == null) {
				throw new NullPointerException("X getter can't be null");
			}
			else if (y == null) {
				throw new NullPointerException("Y getter can't be null");
			}
			else if (rx == null) {
				throw new NullPointerException("RX getter can't be null");
			}
			else if (ry == null) {
				throw new NullPointerException("RY getter can't be null");
			}
			else if (drawColorGetter == null) {
				throw new NullPointerException("Draw color getter can't be null");
			}
			else if (strokeGetter == null) {
				throw new NullPointerException("Stroke getter can't be null");
			}
			else {
				this.dynamicParameters = false;
				this.x = this.y = this.rx = this.ry = 0;
				this.color = this.fillColor = null;
				this.stroke = null;
				this.xGetter = x; 
				this.yGetter = y;
				this.rxGetter = rx;
				this.ryGetter = ry;
				this.colorGetter = drawColorGetter; 
				this.fillColorGetter = fillColorGetter;
				this.strokeGetter = strokeGetter;
				this.painter = new SVGPainter.DynamicEllipsePainter(x, y, rx, ry, drawColorGetter, fillColorGetter, strokeGetter);
			}
		}
		
		public float getX() {
			return dynamicParameters ? xGetter.get() : x;
		}

		public float getY() {
			return dynamicParameters ? yGetter.get() : y;
		}

		public float getRx() {
			return dynamicParameters ? rxGetter.get() : rx;
		}

		public float getRy() {
			return dynamicParameters ? ryGetter.get() : ry;
		}

		public Color getColor() {
			return dynamicParameters ? colorGetter.get() : color;
		}

		public Color getFillColor() {
			return dynamicParameters ? (fillColorGetter != null ? fillColorGetter.get() : null) : fillColor;
		}

		public Stroke getStroke() {
			return dynamicParameters ? strokeGetter.get() : stroke;
		}

		@Override
		public SVGPrimitivePainter getPainter() {
			return painter;
		}
	}
}
