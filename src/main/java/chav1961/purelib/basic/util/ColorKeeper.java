package chav1961.purelib.basic.util;

import java.awt.Color;
import java.io.Serializable;

/**
 * <p>This class is used to keep color components.</p>
 * @since 0.0.3
 */
public class ColorKeeper implements Serializable {
	private static final long serialVersionUID = -4654301694021015313L;
	
	private int	red = 0, green = 0, blue = 0;
	
	/**
	 * <p>Constructor of the class instance. Creates black color keeper.</p>
	 */
	public ColorKeeper() {
	}
	
	/**
	 * <p>Constructor of the class instance</p>
	 * @param red red component of the color.
	 * @param green green component of the color.
	 * @param blue blue component of the color.
	 */
	public ColorKeeper(final int red, final int green, final int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	/**
	 * <p>Constructor of the class instance. </p>
	 * @param color color to extract color components from. Can't be null.
	 */
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
	
	/**
	 * <p>Get red component of the color.
	 * @return red component of the color.
	 */
	public int getRed() {
		return red;
	}

	/**
	 * <p>Set red component of the color</p>
	 * @param red red component of the color.
	 */
	public void setRed(final int red) {
		this.red = red;
	}

	/**
	 * <p>Get green component of the color.
	 * @return green component of the color.
	 */
	public int getGreen() {
		return green;
	}

	/**
	 * <p>Set green component of the color</p>
	 * @param green green component of the color.
	 */
	public void setGreen(final int green) {
		this.green = green;
	}

	/**
	 * <p>Get blue component of the color.
	 * @return blue component of the color.
	 */
	public int getBlue() {
		return blue;
	}

	/**
	 * <p>Set blue component of the color</p>
	 * @param blue green component of the color.
	 */
	public void setBlue(final int blue) {
		this.blue = blue;
	}
	
	/**
	 * <p>Set component of the color from argument</p>
	 * @param color color to extract color components from. Can't be null
	 */
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
	
	/**
	 * <p>Convert color keeper to {@linkplain Color}</p>
	 * @return Color instance converted. Can't be null.
	 */
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
