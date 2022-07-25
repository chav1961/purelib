package chav1961.purelib.model;

import java.awt.Color;
import java.io.Serializable;

public class ColorKeeper implements Serializable {
	private static final long serialVersionUID = -4654301694021015313L;
	
	private int	red = 0, green = 0, blue = 0;
	
	public ColorKeeper() {
	}
	
	public ColorKeeper(final int red, final int green, final int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public ColorKeeper(final Color color) {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			this.red = color.getRed();
			this.green = color.getGreen();
			this.blue = color.getBlue();
		}
	}
	
	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}
	
	public void setColor(final Color color) {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			this.red = color.getRed();
			this.green = color.getGreen();
			this.blue = color.getBlue();
		}
	}
	
	public Color toColor() {
		return new Color(red,green,blue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + blue;
		result = prime * result + green;
		result = prime * result + red;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ColorKeeper other = (ColorKeeper) obj;
		if (blue != other.blue) return false;
		if (green != other.green) return false;
		if (red != other.red) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("#%1$02x%2$02x%3$02x",getRed(),getGreen(),getBlue());
	}
}
