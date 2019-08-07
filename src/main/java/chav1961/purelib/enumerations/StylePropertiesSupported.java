package chav1961.purelib.enumerations;

import java.util.Arrays;

public enum StylePropertiesSupported {
		background("background"),
		background_attachment("background-attachment",true,ContentType.value,new ValueListDescriptor(ContentType.value,0,Integer.MAX_VALUE,"fixed","scroll","local")),
		background_clip("background-clip",false,ContentType.value,new ValueListDescriptor(ContentType.value,0,Integer.MAX_VALUE,"padding-box","border-box","content-box")),
		background_color("background-color",true,ContentType.color),
		background_image("background-image",true,ContentType.urlOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		background_origin("background-origin",false,ContentType.value,new ValueListDescriptor(ContentType.value,0,Integer.MAX_VALUE,"padding-box","border-box","content-box")),
		background_position("background-position"),
		background_repeat("background-repeat",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,2,"no-repeat","repeat","repeat-x","repeat-y")),
		background_size("background-size",true,ContentType.distanceOrKeyword,1,2,new ValueListDescriptor(ContentType.value,1,1,"auto","cover","contain")),
		
		border("border"),
		border_bottom("border-bottom",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		border_bottom_color("border-bottom-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		border_bottom_left_radius("border-bottom-left-radius",false,ContentType.distance,1,2),
		border_bottom_right_radius("border-bottom-right-radius",false,ContentType.distance,1,2),
		border_bottom_style("border-bottom-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		border_bottom_width("border-bottom-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		border_collapse("border-collapse",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"collapse","separate")),
		border_color("border-color",true,ContentType.colorOrKeyword,1,4,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		border_image("border-image"),
		border_left("border-left",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		border_left_color("border-left-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		border_left_style("border-left-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		border_left_width("border-left-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		border_radius("border-radius"),
		border_right("border-right",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		border_right_color("border-right-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		border_right_style("border-right-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		border_right_width("border-right-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		border_spacing("border-spacing",false,ContentType.distance,1,2),
		border_style("border-style",true,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"border-top-style","border-right-style","border-bottom-style","border-left-style")),
		border_top("border-top",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		border_top_color("border-top-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		border_top_left_radius("border-top-left-radius",false,ContentType.distance,1,2),
		border_top_right_radius("border-top-right-radius",false,ContentType.distance,1,2),
		border_top_style("border-top-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		border_top_width("border-top-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		border_width("border-width",true,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"border-top-width","border-right-width","border-bottom-width","border-left-width")),
		
		bottom("bottom",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		box_shadow("box-shadow"),
		box_sizing("box-sizing",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"content-box","border-box","padding-box")),
		
		caption_side("caption-side",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"top","bottom","left","right")),
		clear("clear",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"top","none","left","right","both")),
		clip("clip"),
		color("color",true,ContentType.color),
		
		column_count("column-count",false,ContentType.integerOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		column_gap("column-gap",false,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"normal")),
		column_rule("column-rule"),
		column_width("column-width",false,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		columns("columns"),
		content("content"),
		
		counter_increment("counter-increment"),
		counter_reset("counter-reset"),
		
		cursor("cursor"),
		direction("direction",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"ltr","rtl")),
		display("display",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"block","inline","inline-block","inline-table","list-item","none","run-in","table","table-caption","table-cell","table-column-group","table-column","table-footer-group","table-header-group","table-row","table-row-group")),
		empty_cells("empty-cells",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"show","hide")),
		_float("float",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"left","right","none")),

		font("font"),
		font_family("font-family",true,ContentType.string,1,Integer.MAX_VALUE),
		font_size("font-size",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"xx-small","x-small","small","medium","large","x-large","xx-large","larger","smaller")),
		font_stretch("font-stretch",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"ultra-condensed","extra-condensed","condensed","semi-condensed","normal","semi-expanded","expanded","extra-expanded","ultra-expanded")),
		font_style("font-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","italic","oblique")),
		font_variant("font-variant",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","small-caps")),
		font_weight("font-weight",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"bold","bolder","lighter","normal","100","200","300","400","500","600","700","800","900")),
		
		height("height",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		left("left",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		letter_spacing("letter-spacing",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"normal")),
		line_height("line-height"),
		
		list_style("list-style"),
		list_style_image("list-style-image",true,ContentType.urlOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		list_style_position("list-style-position",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"inside","outside")),
		list_style_type("list-style-type",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"circle","disc","square","armenian","decimal","decimal-leading-zero","georgian","lower-alpha","lower-greek","lower-latin","lower-roman","upper-alpha","upper-latin","upper-roman","none")),
		
		margin("margin"),
		margin_bottom("margin-bottom",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		margin_left("margin-left",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		margin_right("margin-right",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		margin_top("margin-top",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		max_height("max-height",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		max_width("max-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		
		min_height("min-height",true,ContentType.distance),
		min_width("min-width",true,ContentType.distance),

		opacity("opacity",false,ContentType.number),
		orphans("orphans",true,ContentType.integer),
		
		outline("outline"),
		outline_color("outline-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"invert")),
		outline_offset("outline-offset",true,ContentType.distance),
		outline_style("outline-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		outline_width("outline-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		
		overflow("overflow"),
		overflow_x("overflow-x",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","hidden","scroll","visible")),
		overflow_y("overflow-y",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","hidden","scroll","visible")),
		
		padding("padding"),
		padding_bottom("padding-bottom",true,ContentType.distance),
		padding_left("padding-left",true,ContentType.distance),
		padding_right("padding-right",true,ContentType.distance),
		padding_top("padding-top",true,ContentType.distance),
		
		page_break_after("page-break-after",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"always","auto","avoid","left","right")),
		page_break_before("page-break-before",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"always","auto","avoid","left","right")),
		page_break_inside("page-break-inside",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","avoid")),
		
		position("position",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"absolute","fixed","relative","static")),
		quotes("quotes"),
		resize("resize",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","both","horizontal","vertical")),
		right("right",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		tab_size("tab-size",false,ContentType.integer),
		table_layout("table-layout",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","fixed")),
		
		text_align("text-align",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"center","justify","left","right","start","end")),
		text_align_last("text-align-last",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"center","justify","left","right","start","end")),
		text_decoration("text-decoration"),
		text_decoration_color("text-decoration-color",false,ContentType.color),
		text_decoration_line("text-decoration-line"),
		text_decoration_style("text-decoration-style",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"solid","double","dotted","dashed")),
		text_indent("text-indent",true,ContentType.distance),
		text_overflow("text-overflow",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"clip","ellipsis")),
		text_shadow("text-shadow"),
		text_transform("text-transform",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"capitalize","lowercase","uppercase","none")),
		
		top("top",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		transform("transform"),
		transform_origin("transform-origin"),
		
		transition("transition"),
		transition_delay("transition-delay",false,ContentType.time,1,Integer.MAX_VALUE),
		transition_duration("transition-duration",false,ContentType.time,1,Integer.MAX_VALUE),
		transition_property("transition-property"),
		transition_timing_function("transition-timing-function",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"ease","ease-in","ease-out","ease-in-out","linear","step-start","step-end","steps","cubic-bezier")),
		
		unicode_bidi("unicode-bidi",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","embed","bidi-override")),
		vertical_align("vertical-align",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"baseline","bottom","middle","sub","super","text-bottom","text-top","top")),
		visibility("visibility",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"visible","hidden","collapse")),
		white_space("white-space",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","nowrap","pre","pre-line","pre-wrap")),
		widows("widows",true,ContentType.integer),
		width("width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),

		word_break("word-break",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","break-all","keep-all")),
		word_spacing("word-spacing",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"normal")),
		word_wrap("word-wrap",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","break-word")),
		
		writing_mode("writing-mode",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"lr-tb","rl-tb","tb-rl","bt-rl","tb-lr","bt-lr")),
		z_index("z-index",true,ContentType.integerOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto"));
	
	public enum ContentType {
		value, integer, 
		string, stringOrKeyword, integerOrKeyword, number, numberOrKeyword, color, colorOrKeyword, distance, distanceOrKeyword, time, timeOrKeyword, url, urlOrKeyword,
		compoundChoise, compoundSequence, subStyle
	}
	
	public static class ValueListDescriptor {
		private final ContentType	type;
		private final int			minOccur, maxOccur;
		private final String[]		content;
		
		ValueListDescriptor(final ContentType type, final int minOccur, final int maxOccur, final String... content) {
			this.type = type;
			this.minOccur = minOccur;
			this.maxOccur = maxOccur;
			this.content = content;
		}

		public ContentType getType() {
			return type;
		}
		
		public int getMinOccur() {
			return minOccur;
		}

		public int getMaxOccur() {
			return maxOccur;
		}
		
		public String[] getContent() {
			return content;
		}
		
		@Override
		public String toString() {
			return "ValueListDescriptor [type=" + type + ", minOccur=" + minOccur + ", maxOccur=" + maxOccur + ", content=" + Arrays.toString(content) + "]";
		}
	}
	
    private final String 				externalName;
    private final boolean				canBeInherted;
    private final ContentType			contentType;
    private final String				masterProperty;
    private final int					minOccurence, maxOccurence;
    private final ValueListDescriptor 	values;

    private StylePropertiesSupported(final String externalName) {
        this.externalName = externalName;
        this.canBeInherted = false;
        this.contentType = null;
        this.masterProperty = null;
        this.values = null;
        this.minOccurence = 1;
        this.maxOccurence = 1;
    }

    private StylePropertiesSupported(final String externalName, final ContentType type) {
    	this(externalName, false, type, 1, 1, null, null);
    }

    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type) {
    	this(externalName, canBeInherited, type, 1, 1, null, null);
    }

    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type, final int minOccurence, final int maxOccurence) {
    	this(externalName, canBeInherited, type, minOccurence, maxOccurence, null, null);
    }
    
    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type, final String masterProperty) {
    	this(externalName, canBeInherited, type, 1, 1, masterProperty, null);
    }
    
    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type, final ValueListDescriptor values) {
    	this(externalName, canBeInherited, type, 1, 1, null, values);
    }

    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type, final int minOccurence, final int maxOccurence, final ValueListDescriptor values) {
    	this(externalName, canBeInherited, type, minOccurence, maxOccurence, null, values);
    }
    
    private StylePropertiesSupported(final String externalName, final ContentType type, final String masterProperty) {
    	this(externalName, false, type, 1, 1, masterProperty, null);
    }
    
    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type, final int minOccurence, final int maxOccurence, final String masterProperty, final ValueListDescriptor values) {
        this.externalName = externalName;
        this.canBeInherted = canBeInherited;
        this.contentType = type;
        this.masterProperty = masterProperty;
        this.values = values;
        this.minOccurence = minOccurence;
        this.maxOccurence = maxOccurence;
    }
    
    public String getExternalName() {
        return externalName;
    }
    
    public boolean canBeInherited() {
    	return canBeInherted;
    }

    public static StylePropertiesSupported forName(final String externalName) {
    	if (externalName == null || externalName.isEmpty()) {
    		throw new IllegalArgumentException("External name can't be null or empty");
    	}
    	else {
	    	for (StylePropertiesSupported item : values()) {
	    		if (externalName.equals(item.getExternalName())) {
	    			return item;
	    		}
	    	}
    		throw new IllegalArgumentException("Unsupported enum value for external name ["+externalName+"]");
    	}
    }
	
}