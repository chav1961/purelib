package com.caverock.androidsvg;

import java.awt.Graphics2D;

import javax.swing.Painter;

import com.caverock.androidsvg.SVG.Rect;

public class Paint implements Painter {
	
	public static class Style {

		public static final String FILL = null;
		public static final String STROKE = null;
		
	}

	public static class Cap {

		public static final String BUTT = null;
		public static final String ROUND = null;
		public static final String SQUARE = null;
		
	}

	public static class Join {

		public static final String MITER = null;
		public static final String ROUND = null;
		public static final String BEVEL = null;
		
	}
	
	public static final int LINEAR_TEXT_FLAG = 0;
	public static final int ANTI_ALIAS_FLAG = 0;
	public static final int SUBPIXEL_TEXT_FLAG = 0;
	public static final int FILTER_BITMAP_FLAG = 0;

	public Paint(Painter fillPaint) {
		// TODO Auto-generated constructor stub
	}

	public Paint() {
		// TODO Auto-generated constructor stub
	}

	public Paint(int i) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void paint(Graphics2D g, Object object, int width, int height) {
		// TODO Auto-generated method stub

	}

	public void setTypeface(String default1) {
		// TODO Auto-generated method stub
		
	}

	public void setFlags(int i) {
		// TODO Auto-generated method stub
		
	}

	public void setStyle(String fill) {
		// TODO Auto-generated method stub
		
	}

	public float getTextSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Shader getShader() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setXfermode(PorterDuffXfermode porterDuffXfermode) {
		// TODO Auto-generated method stub
		
	}

	public void setColorFilter(ColorMatrixColorFilter colorMatrixColorFilter) {
		// TODO Auto-generated method stub
		
	}

	public float measureText(String text) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void getTextBounds(String text, int i, int length, Rect rect) {
		// TODO Auto-generated method stub
		
	}

	public void setStrokeWidth(float floatValue) {
		// TODO Auto-generated method stub
		
	}

	public void setStrokeCap(String butt) {
		// TODO Auto-generated method stub
		
	}

	public void setStrokeJoin(String miter) {
		// TODO Auto-generated method stub
		
	}

	public void setStrokeMiter(Float strokeMiterLimit) {
		// TODO Auto-generated method stub
		
	}

	public void setPathEffect(Object object) {
		// TODO Auto-generated method stub
		
	}

	public void setTextSize(float floatValue) {
		// TODO Auto-generated method stub
		
	}

	public void setTypeface(Typeface font) {
		// TODO Auto-generated method stub
		
	}

	public void setStrikeThruText(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public void setUnderlineText(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public void setColor(int col) {
		// TODO Auto-generated method stub
		
	}

	public void setShader(LinearGradient gr) {
		// TODO Auto-generated method stub
		
	}

	public void setAlpha(int clamp255) {
		// TODO Auto-generated method stub
		
	}

	public void setShader(RadialGradient gr) {
		// TODO Auto-generated method stub
		
	}

	public void getTextPath(String text, int i, int length, float x, float y, Path spanPath) {
		// TODO Auto-generated method stub
		
	}

}
