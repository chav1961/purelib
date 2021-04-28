package chav1961.purelib.ui;

import java.awt.Color;
import java.io.Serializable;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class ColorPair implements Serializable {
	private static final long serialVersionUID = 869188177438346062L;
	
	private Color	foreground, background;
	
	public ColorPair(final Color foreground, final Color background) {
		if (foreground == null) {
			throw new NullPointerException("Foreground color can't be null");
		}
		else if (background == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else {
			this.foreground = foreground;
			this.background = background;
		}
	}
	
	public ColorPair(final String val) throws SyntaxException, IllegalArgumentException {		
		if (val == null || val.isEmpty()) {
			throw new IllegalArgumentException("String value can't be null or empty");
		}
		else {
			final Object[]	content = new Object[2];
			
			CharUtils.extract(val.toCharArray(),0,content,'{',CharUtils.ArgumentType.colorRepresentation,',',CharUtils.ArgumentType.colorRepresentation,'}');
			this.foreground = (Color)content[0];
			this.background = (Color)content[0];
		}
	}

	public Color getForeground() {
		return foreground;
	}

	public Color getBackground() {
		return background;
	}
	
	public void setForeground(final Color foreground) {
		if (foreground == null) {
			throw new NullPointerException("Foreground color can't be null");
		}
		else {
			this.foreground = foreground;
		}
	}

	public void setBackground(final Color background) {
		if (background == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else {
			this.background = background;
		}
	}

	@Override
	public String toString() {
		return "ColorPair [foreground=" + foreground + ", background=" + background + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((background == null) ? 0 : background.hashCode());
		result = prime * result + ((foreground == null) ? 0 : foreground.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ColorPair other = (ColorPair) obj;
		if (background == null) {
			if (other.background != null) return false;
		} else if (!background.equals(other.background)) return false;
		if (foreground == null) {
			if (other.foreground != null) return false;
		} else if (!foreground.equals(other.foreground)) return false;
		return true;
	}
}
