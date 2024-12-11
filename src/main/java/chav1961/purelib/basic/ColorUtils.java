package chav1961.purelib.basic;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import chav1961.purelib.ui.ColorScheme;
import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This class is used to manipulate colors.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.9
 */
public class ColorUtils {
	private static final Map<Color,String>			COLOR2NAME = new HashMap<>();
	private static final Map<String,Color>			NAME2COLOR = new HashMap<>();
	private static final ColorScheme				DEFAULT_COLOR_SCHEME; 
	
	static {
		try(final InputStream		is = SwingUtils.class.getResourceAsStream("colors.txt");
			final Reader			rdr = new InputStreamReader(is);
			final BufferedReader	brdr = new BufferedReader(rdr)) {
			String					buffer;
			
			while ((buffer = brdr.readLine()) != null) {
				final int			tab = buffer.indexOf('\t');
				final String		name = buffer.substring(0,tab);
				final Color			color = toRGB(buffer.substring(tab+1));
				
				NAME2COLOR.put(name,color);
				COLOR2NAME.putIfAbsent(color,name);				
			}
		} catch (IOException exc) {
			PureLibSettings.logger.log(Level.WARNING,"Internal color table for the Pure library was not loaded: "+exc.getMessage(),exc);
		}

		DEFAULT_COLOR_SCHEME = new ColorScheme();
	}
	
	/**
	 * <p>Convert color name to it's {@linkplain Color} representation</p>
	 * @param name name to convert
	 * @param defaultColor default Color instance if name can't be converted
	 * @return Color converted. Can't be null
	 * @throws IllegalArgumentException if color name is null or empty
	 */
	public static Color colorByName(final String name, final Color defaultColor) throws IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(name)) {
			throw new IllegalArgumentException("Color name can't be null or empty");
		}
		else if (NAME2COLOR.containsKey(name)) {
			return NAME2COLOR.get(name);
		}
		else  if (name.startsWith("#")) {
			return new Color(Integer.parseUnsignedInt(name,1,name.length()-1,16));
		}
		else {
			return defaultColor;
		}
	}
	
	/**
	 * <p>Convert {@linkplain Color} instance to it's symbolic name</p>
	 * @param color color to convert
	 * @param defaultName default name if color can't be converted
	 * @return Color name or default name
	 * @throws NullPointerException if color instance to convert is null
	 */
	public static String nameByColor(final Color color, final String defaultName) throws NullPointerException {
		if (color == null) {
			throw new NullPointerException("Color name can't be null or empty");
		}
		else if (COLOR2NAME.containsKey(color)) {
			return COLOR2NAME.get(color);
		}
		else {
			return defaultName;
		}
	}

	/**
	 * <p>Get default color scheme.</p>
	 * @return default color scheme. Can't be null
	 */
	public static ColorScheme defaultColorScheme() {
		return DEFAULT_COLOR_SCHEME;
	}
	
	
	private static Color toRGB(final String rgb) {
		if (!rgb.isEmpty()) {
			if (rgb.charAt(0) == '#') {
				return new Color((int)Long.parseLong(rgb.substring(1).toUpperCase(),16));
			}
			else {
				final String[]	parts = rgb.split("\\,");
				
				return new Color(Integer.valueOf(parts[0]),Integer.valueOf(parts[1]),Integer.valueOf(parts[2])); 
			}
		}
		else {
			return Color.BLACK;
		}
	}
}
