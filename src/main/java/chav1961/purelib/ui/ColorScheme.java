package chav1961.purelib.ui;

import java.awt.Color;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.interfaces.PureLibColorScheme;

public class ColorScheme {
	public final Color	MANDATORY_BACKGROUND;
	public final Color	MANDATORY_FOREGROUND;
	public final Color	MANDATORY_FOREGROUND_NEGATIVE;
	public final Color	MANDATORY_FOREGROUND_ZERO;
	public final Color	MANDATORY_FOREGROUND_POSITIVE;
	public final Color	MANDATORY_SELECTION_BACKGROUND;
	public final Color	MANDATORY_SELECTION_FOREGROUND;
	public final Color	OPTIONAL_BACKGROUND;
	public final Color	OPTIONAL_FOREGROUND;
	public final Color	OPTIONAL_FOREGROUND_NEGATIVE;
	public final Color	OPTIONAL_FOREGROUND_ZERO;
	public final Color	OPTIONAL_FOREGROUND_POSITIVE;
	public final Color	OPTIONAL_SELECTION_BACKGROUND;
	public final Color	OPTIONAL_SELECTION_FOREGROUND;
	public final Color	READONLY_BACKGROUND;
	public final Color	READONLY_FOREGROUND;

	public final Color	NEGATIVEMARK_FOREGROUND;
	public final Color	POSITIVEMARK_FOREGROUND;
	public final Color	ZEROMARK_FOREGROUND;
	
	public final Color	DATEPICKER_DAY_NAME_COLOR; 
	public final Color	DATEPICKER_WEEKEND_NAME_COLOR;
	public final Color	DATEPICKER_DAY_VALUE_COLOR;
	public final Color	DATEPICKER_WEEKEND_VALUE_COLOR;
			
	public final Color	TOOLTIP_BORDER_COLOR;

	public final Color	SELECTED_TABLE_LINE_COLOR = Color.WHITE;
	public final Color	UNSELECTED_TABLE_LINE_COLOR = new Color(224,224,224);
	public final Color	UNSELECTED_TABLE_LINE_ODD_COLOR = new Color(224,224,224);
	public final Color	UNSELECTED_TABLE_LINE_EVEN_COLOR = new Color(224,224,224);
	
	public ColorScheme() {
		this.MANDATORY_BACKGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_BACKGROUND,Color.class,"LightCyan3");
		this.MANDATORY_FOREGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_FOREGROUND,Color.class,"blue");
		this.MANDATORY_FOREGROUND_NEGATIVE = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_FOREGROUND_NEGATIVE,Color.class,"red");
		this.MANDATORY_FOREGROUND_ZERO = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_FOREGROUND_ZERO,Color.class,"blue");
		this.MANDATORY_FOREGROUND_POSITIVE = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_FOREGROUND_POSITIVE,Color.class,"green");
		this.MANDATORY_SELECTION_BACKGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_SELECTED,Color.class,"blue");
		this.MANDATORY_SELECTION_FOREGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_SELECTED_TEXT,Color.class,"white");
		this.OPTIONAL_BACKGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.OPTIONAL_BACKGROUND,Color.class,"white");
		this.OPTIONAL_FOREGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.OPTIONAL_FOREGROUND,Color.class,"black");
		this.OPTIONAL_FOREGROUND_NEGATIVE = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_FOREGROUND_NEGATIVE,Color.class,"red");
		this.OPTIONAL_FOREGROUND_ZERO = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_FOREGROUND_ZERO,Color.class,"blue");
		this.OPTIONAL_FOREGROUND_POSITIVE = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_FOREGROUND_POSITIVE,Color.class,"green");
		this.OPTIONAL_SELECTION_BACKGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_SELECTED,Color.class,"blue");
		this.OPTIONAL_SELECTION_FOREGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.MANDATORY_SELECTED_TEXT,Color.class,"white");
		this.READONLY_BACKGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.READONLY_BACKGROUND,Color.class,"LightGray");
		this.READONLY_FOREGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.READONLY_FOREGROUND,Color.class,"black");

		this.NEGATIVEMARK_FOREGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.NEGATIVEMARK_FOREGROUND,Color.class,"red");
		this.POSITIVEMARK_FOREGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.POSITIVEMARK_FOREGROUND,Color.class,"green");
		this.ZEROMARK_FOREGROUND = PureLibSettings.instance().getProperty(PureLibColorScheme.ZEROMARK_FOREGROUND,Color.class,"brown");
		
		this.DATEPICKER_DAY_NAME_COLOR = PureLibSettings.instance().getProperty(PureLibColorScheme.DATEPICKER_DAY_NAME_COLOR,Color.class,"blue"); 
		this.DATEPICKER_WEEKEND_NAME_COLOR = PureLibSettings.instance().getProperty(PureLibColorScheme.DATEPICKER_WEEKEND_NAME_COLOR,Color.class,"red");
		this.DATEPICKER_DAY_VALUE_COLOR = PureLibSettings.instance().getProperty(PureLibColorScheme.DATEPICKER_DAY_VALUE_COLOR,Color.class,"black");
		this.DATEPICKER_WEEKEND_VALUE_COLOR = PureLibSettings.instance().getProperty(PureLibColorScheme.DATEPICKER_WEEKEND_VALUE_COLOR,Color.class,"red");
				
		this.TOOLTIP_BORDER_COLOR = PureLibSettings.instance().getProperty(PureLibColorScheme.TOOLTIP_BORDER_COLOR,Color.class,"IndianRed");
	}
}
