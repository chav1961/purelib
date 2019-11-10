package chav1961.purelib.enumerations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CSSUtils;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.XMLUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public enum StylePropertiesSupported {
		background("background",true,ContentType.compoundChoise,1,Integer.MAX_VALUE,new ValueListDescriptor(ContentType.subStyle,1,1,"background-attachment","background-color","background-image","background-position","background-repeat")),
		background_attachment("background-attachment",true,ContentType.value,new ValueListDescriptor(ContentType.value,0,Integer.MAX_VALUE,"fixed","scroll","local")),
		background_clip("background-clip",false,ContentType.value,new ValueListDescriptor(ContentType.value,0,Integer.MAX_VALUE,"padding-box","border-box","content-box")),
		background_color("background-color",true,ContentType.color),
		background_image("background-image",true,ContentType.urlOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		background_origin("background-origin",false,ContentType.value,new ValueListDescriptor(ContentType.value,0,Integer.MAX_VALUE,"padding-box","border-box","content-box")),
		background_position("background-position",true,ContentType.distanceOrKeyword,1,2,new ValueListDescriptor(ContentType.value,1,1,"left","center","right","top","center","bottom")),
		background_repeat("background-repeat",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,2,"no-repeat","repeat","repeat-x","repeat-y")),
		background_size("background-size",true,ContentType.distanceOrKeyword,1,2,new ValueListDescriptor(ContentType.value,1,1,"auto","cover","contain")),
		
		border("border",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		border_bottom("border-bottom",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		border_bottom_color("border-bottom-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		border_bottom_left_radius("border-bottom-left-radius",false,ContentType.distance,1,2),
		border_bottom_right_radius("border-bottom-right-radius",false,ContentType.distance,1,2),
		border_bottom_style("border-bottom-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		border_bottom_width("border-bottom-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		border_collapse("border-collapse",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"collapse","separate")),
		border_color("border-color",true,ContentType.colorOrKeyword,1,4,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		border_image("border-image",false,ContentType.asIs),
		border_left("border-left",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		border_left_color("border-left-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		border_left_style("border-left-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		border_left_width("border-left-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		border_radius("border-radius",false,ContentType.asIs),
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
		
		box_shadow("box-shadow",false,ContentType.asIs),
		box_sizing("box-sizing",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"content-box","border-box","padding-box")),
		
		caption_side("caption-side",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"top","bottom","left","right")),
		clear("clear",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"top","none","left","right","both")),
		clip("clip",true,ContentType.asIs),
		color("color",true,ContentType.color),
		
		column_count("column-count",false,ContentType.integerOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		column_gap("column-gap",false,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"normal")),
		column_rule("column-rule",false,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		column_width("column-width",false,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		columns("columns",false,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"column-width","column-count")),
		content("content",true,ContentType.asIs),
		
		counter_increment("counter-increment",true,ContentType.asIs),
		counter_reset("counter-reset",true,ContentType.asIs),
		
		cursor("cursor",true,ContentType.asIs),
		direction("direction",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"ltr","rtl")),
		display("display",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"block","inline","inline-block","inline-table","list-item","none","run-in","table","table-caption","table-cell","table-column-group","table-column","table-footer-group","table-header-group","table-row","table-row-group")),
		empty_cells("empty-cells",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"show","hide")),
		_float("float",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"left","right","none")),

		font("font",true,ContentType.asIs),
		font_family("font-family",true,ContentType.string,1,Integer.MAX_VALUE),
		font_size("font-size",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"xx-small","x-small","small","medium","large","x-large","xx-large","larger","smaller")),
		font_stretch("font-stretch",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"ultra-condensed","extra-condensed","condensed","semi-condensed","normal","semi-expanded","expanded","extra-expanded","ultra-expanded")),
		font_style("font-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","italic","oblique")),
		font_variant("font-variant",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","small-caps")),
		font_weight("font-weight",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"bold","bolder","lighter","normal","100","200","300","400","500","600","700","800","900")),
		
		height("height",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		left("left",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		letter_spacing("letter-spacing",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"normal")),
		line_height("line-height",true,ContentType.asIs),
		
		list_style("list-style",true,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"list-style-type","list-style-position","list-style-image")),
		list_style_image("list-style-image",true,ContentType.urlOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		list_style_position("list-style-position",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"inside","outside")),
		list_style_type("list-style-type",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"circle","disc","square","armenian","decimal","decimal-leading-zero","georgian","lower-alpha","lower-greek","lower-latin","lower-roman","upper-alpha","upper-latin","upper-roman","none")),
		
		margin("margin",true,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"margin-top","margin-right","margin-bottom","margin-left")),
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
		
		outline("outline",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"outline-color","outline-style","outline-width")),
		outline_color("outline-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"invert")),
		outline_offset("outline-offset",true,ContentType.distance),
		outline_style("outline-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		outline_width("outline-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		
		overflow("overflow",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","hidden","scroll","visible")),
		overflow_x("overflow-x",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","hidden","scroll","visible")),
		overflow_y("overflow-y",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","hidden","scroll","visible")),
		
		padding("padding",true,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"padding-top","padding-right","padding-bottom","padding-left")),
		padding_bottom("padding-bottom",true,ContentType.distance),
		padding_left("padding-left",true,ContentType.distance),
		padding_right("padding-right",true,ContentType.distance),
		padding_top("padding-top",true,ContentType.distance),
		
		page_break_after("page-break-after",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"always","auto","avoid","left","right")),
		page_break_before("page-break-before",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"always","auto","avoid","left","right")),
		page_break_inside("page-break-inside",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","avoid")),
		
		position("position",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"absolute","fixed","relative","static")),
		quotes("quotes",true,ContentType.asIs),
		resize("resize",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","both","horizontal","vertical")),
		right("right",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		tab_size("tab-size",false,ContentType.integer),
		table_layout("table-layout",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","fixed")),
		
		text_align("text-align",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"center","justify","left","right","start","end")),
		text_align_last("text-align-last",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"center","justify","left","right","start","end")),
		text_decoration("text-decoration",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.value,1,1,"blink","line-through","overline","underline","none")),
		text_decoration_color("text-decoration-color",false,ContentType.color),
		text_decoration_line("text-decoration-line",false,ContentType.asIs),
		text_decoration_style("text-decoration-style",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"solid","double","dotted","dashed")),
		text_indent("text-indent",true,ContentType.distance),
		text_overflow("text-overflow",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"clip","ellipsis")),
		text_shadow("text-shadow",true,ContentType.asIs),
		text_transform("text-transform",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"capitalize","lowercase","uppercase","none")),
		
		top("top",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		transform("transform",true,ContentType.functionOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		transform_origin("transform-origin",true,ContentType.asIs),
		
		transition("transition",true,ContentType.asIs),
		transition_delay("transition-delay",false,ContentType.time,1,Integer.MAX_VALUE),
		transition_duration("transition-duration",false,ContentType.time,1,Integer.MAX_VALUE),
		transition_property("transition-property",false,ContentType.stringOrKeyword,1,Integer.MAX_VALUE,new ValueListDescriptor(ContentType.value,1,1,"none","all")),
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

	public static final String	INHERITED_KEYWORD = "inherited";
	
	public enum ContentType {
		value, integer, 
		string, stringOrKeyword, integerOrKeyword, number, numberOrKeyword, color, colorOrKeyword,
		distance, distanceOrKeyword, time, timeOrKeyword, url, urlOrKeyword, function, functionOrKeyword,
		compoundChoise, compoundSequence, subStyle, asIs
	}

	public static class Keyword {
		public static final Keyword	INHERITED = new Keyword(StylePropertiesSupported.INHERITED_ID,StylePropertiesSupported.INHERITED_KEYWORD); 
		
		private final long		id;
		private final String	name;
		
		public Keyword(long id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public long getId() {
			return id;
		}
		
		public String getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return "Keyword [id=" + id + ", name=" + name + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (id ^ (id >>> 32));
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Keyword other = (Keyword) obj;
			if (id != other.id) return false;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			return true;
		}
	}
	
	private static class ValueListDescriptor {
		private final ContentType	type;
		private final int			minOccur, maxOccur;
		private final String[]		content;
		
		ValueListDescriptor(final ContentType type, final int minOccur, final int maxOccur, final String... content) {
			this.type = type;
			this.minOccur = minOccur;
			this.maxOccur = maxOccur;
			if (content == null) {
				this.content = content;
			}
			else {
				this.content = content;
			}
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

	private static final SyntaxTreeInterface<StylePropertiesSupported[]>	NAMES = new AndOrTree<>();
	private static final long			INHERITED_ID;
	private static final long[]			EMPTY_CONTENT = new long[0];
	private static final Object[]		URL_LEXEMAS = {"url",'(',CharUtils.ArgumentType.name,')'};
	
    private final String 				externalName;
    private final boolean				canBeInherited;
    private final ContentType			contentType;
    private final String				masterProperty;
    private final int					minOccurence, maxOccurence;
    private final ValueListDescriptor 	values;
    private final long[]				valueIds;
    private final Keyword[]				valueKeywords;
    private final Object[]				obj2Use = new Object[1];
    
    static {
    	INHERITED_ID = NAMES.placeName(INHERITED_KEYWORD,null);
    	advancedPreparation();
    }

    private StylePropertiesSupported(final String externalName) {
        this.externalName = externalName;
        this.canBeInherited = false;
        this.contentType = null;
        this.masterProperty = null;
        this.values = null;
        this.minOccurence = 1;
        this.maxOccurence = 1;
        this.valueIds = new long[0];
        this.valueKeywords = new Keyword[0];
    }

	private StylePropertiesSupported(final String externalName, final ContentType type) {
    	this(externalName, false, type, 1, 1, null, new ValueListDescriptor(type,0,0));
    }

    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type) {
    	this(externalName, canBeInherited, type, 1, 1, null, new ValueListDescriptor(type,0,0));
    }

    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type, final int minOccurence, final int maxOccurence) {
    	this(externalName, canBeInherited, type, minOccurence, maxOccurence, null, new ValueListDescriptor(type,0,0));
    }
    
    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type, final String masterProperty) {
    	this(externalName, canBeInherited, type, 1, 1, masterProperty, new ValueListDescriptor(type,0,0));
    }
    
    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type, final ValueListDescriptor values) {
    	this(externalName, canBeInherited, type, 1, 1, null, values);
    }

    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type, final int minOccurence, final int maxOccurence, final ValueListDescriptor values) {
    	this(externalName, canBeInherited, type, minOccurence, maxOccurence, null, values);
    }
    
    private StylePropertiesSupported(final String externalName, final ContentType type, final String masterProperty) {
    	this(externalName, false, type, 1, 1, masterProperty, new ValueListDescriptor(type,0,0));
    }

    private StylePropertiesSupported(final String externalName, final boolean canBeInherited, final ContentType type, final int minOccurence, final int maxOccurence, final String masterProperty, final ValueListDescriptor values) {
        this.externalName = externalName;
        this.canBeInherited = canBeInherited;
        this.contentType = type;
        this.masterProperty = masterProperty;
        this.values = values;
        this.minOccurence = minOccurence;
        this.maxOccurence = maxOccurence;
        this.valueIds = new long[values.content.length];
        this.valueKeywords = new Keyword[calculateAmountOfKeywords(contentType,values)];
    }
    
	public String getExternalName() {
        return externalName;
    }
    
    public boolean canBeInherited() {
    	return canBeInherited;
    }

    public boolean canUseKeywords() {
    	return valueKeywords.length > 0;
    }
    
    public ContentType getContentType() {
		return contentType;
	}

    public int getMinOccurence() {
		return minOccurence;
	}

    public int getMaxOccurence() {
		return maxOccurence;
	}

    public Keyword[] getKeywordsSupported() {
    	return this.valueKeywords;
    }
    
    public StylePropertiesSupported[] getInheritance() {
    	return null;
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
	
	public boolean isValidValue(final String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("Value can't be null or empty"); 
		}
		else {
			return isValidValue(value.toCharArray());
		}
	}

	public boolean isValidValue(final char[] value) {
		if (value == null || value.length == 0) {
			throw new IllegalArgumentException("Value can't be null or empty array"); 
		}
		else {
			try {
				final long	id = NAMES.seekName(value,0,value.length);
				final int	from = 0;
				
				if (id == INHERITED_ID) {
					return canBeInherited();
				}
				else {
					switch (getContentType()) {
						case asIs	:
							return true;
						case colorOrKeyword	:
							if (id >= 0) {
								return isKeywordValid(id,this);
							}
						case color	:
							return CSSUtils.isValidColor(value);
						case compoundChoise	:
							break;
						case compoundSequence	:
							break;
						case distanceOrKeyword	:
							if (id >= 0) {
								return isKeywordValid(id,this);
							}
						case distance	:
							return CSSUtils.isValidDistance(value);
						case functionOrKeyword	:
							if (id >= 0) {
								return isKeywordValid(id,this);
							}
						case function	:
							break;
						case integerOrKeyword	:
							if (id >= 0) {
								return isKeywordValid(id,this);
							}
						case integer	:
							return CharUtils.validateNumber(value,from,CharUtils.PREF_INT|CharUtils.PREF_LONG,true) >= 0;
						case numberOrKeyword	:
							if (id >= 0) {
								return isKeywordValid(id,this);
							}
						case number	:
							return CharUtils.validateNumber(value,from,CharUtils.PREF_ANY,true) >= 0;
						case stringOrKeyword	:
							if (id >= 0) {
								return isKeywordValid(id,this);
							}
						case string	:
							break;
						case subStyle	:
							break;
						case timeOrKeyword	:
							if (id >= 0) {
								return isKeywordValid(id,this);
							}
						case time	:
							return CSSUtils.isValidTime(value);
						case urlOrKeyword	:
							if (id >= 0) {
								return isKeywordValid(id,this);
							}
						case url	:
							if (CharUtils.extract(value,0,obj2Use,URL_LEXEMAS) > 0) {
								try {
									new URI((String)obj2Use[0]);
									return true;
								} catch (URISyntaxException e) {
									return false;
								}							
							}
							else {
								return false;
							}
						case value	:
							return id >= 0;
						default :
							throw new UnsupportedOperationException("Content type ["+getContentType()+"] is not supported yet"); 
					}
				}
			} catch (SyntaxException e) {
				return false;
			}
			return false;
		}
	}

	public <T> T forValue(final String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("Value can't be null or empty");
		}
		else {
			return forValue(value.toCharArray());
		}
	}

	public StylePropertiesSupported getAggregate() {
		return getAggregateProperty(this);
	}
	
	public StylePropertiesSupported[] getDetails() {
		return getProperties4Aggregate(this);
	}
	
	public EnumMap<StylePropertiesSupported,String> split(final String value) {
		return null;
	}
	
	public String join(final EnumMap<StylePropertiesSupported,String> content) {
		return null;
	}
	
	public <T> T forValue(final char[] value) {
		if (value == null || value.length == 0) {
			throw new IllegalArgumentException("Value can't be null or empty array");
		}
		else {
			try {
				final long	id = NAMES.seekName(value,0,value.length);
				final int	from = 0;
				
				if (id == INHERITED_ID) {
					return (T) Keyword.INHERITED;
				}
				else {
					switch (getContentType()) {
						case asIs	:
							return null;
						case colorOrKeyword	:
							if (id >= 0) {
								return seekKeyword(id);
							}
						case color	:
							return (T) CSSUtils.asColor(value);
						case compoundChoise	:
							break;
						case compoundSequence	:
							break;
						case distanceOrKeyword	:
							if (id >= 0) {
								return seekKeyword(id);
							}
						case distance	:
							return (T) CSSUtils.asDistance(value);
						case functionOrKeyword	:
							if (id >= 0) {
								return null;
							}
						case function	:
							break;
						case integerOrKeyword	:
							if (id >= 0) {
								return seekKeyword(id);
							}
						case integer	:
							final long[]	result = new long[2];
									
							CharUtils.parseNumber(value,from,result,CharUtils.PREF_INT|CharUtils.PREF_LONG,true);
							if (result[1] == CharUtils.PREF_INT || result[1] == CharUtils.PREF_LONG) {
								return (T) Long.valueOf(result[0]);
							}
							else {
								throw new IllegalArgumentException("Value [] contains invalid number");
							}
						case numberOrKeyword	:
							if (id >= 0) {
								return seekKeyword(id);
							}
						case number	:
							final long[]	numResult = new long[2];
							
							CharUtils.parseNumber(value,from,numResult,CharUtils.PREF_ANY,true);
							if (numResult[1] == CharUtils.PREF_INT || numResult[1] == CharUtils.PREF_LONG) {
								return (T) Double.valueOf(numResult[0]);
							}
							else if (numResult[1] == CharUtils.PREF_FLOAT) {
								return (T) Double.valueOf(Float.intBitsToFloat((int)numResult[0]));
							}
							else {
								return (T) Double.valueOf(Double.longBitsToDouble(numResult[0]));
							}
						case stringOrKeyword	:
							if (id >= 0) {
								return null;
							}
						case string	:
							break;
						case subStyle	:
							break;
						case timeOrKeyword	:
							if (id >= 0) {
								return seekKeyword(id);
							}
						case time	:
							return (T) CSSUtils.asTime(value);
						case urlOrKeyword	:
							if (id >= 0) {
								return null;
							}
						case url	:
							if (CharUtils.extract(value,0,obj2Use,URL_LEXEMAS) > 0) {
								try {
									new URI((String)obj2Use[0]);
									return null;
								} catch (URISyntaxException e) {
									return null;
								}							
							}
							else {
								return null;
							}
						case value	:
							return null;
						default :
							throw new UnsupportedOperationException("Content type ["+getContentType()+"] is not supported yet"); 
					}
				}
			} catch (SyntaxException e) {
				return null;
			}
			return null;
		}
	}	

	public static StylePropertiesSupported getAggregateProperty(final StylePropertiesSupported property) {
		if (property == null) {
			throw new NullPointerException("Property name can't be null");
		}
		else {
			switch (property) {
				case _float:
					break;
				case background:
					break;
				case background_attachment:
					break;
				case background_clip:
					break;
				case background_color:
					break;
				case background_image:
					break;
				case background_origin:
					break;
				case background_position:
					break;
				case background_repeat:
					break;
				case background_size:
					break;
				case border:
					break;
				case border_bottom:
					break;
				case border_bottom_color:
					break;
				case border_bottom_left_radius:
					break;
				case border_bottom_right_radius:
					break;
				case border_bottom_style:
					break;
				case border_bottom_width:
					break;
				case border_collapse:
					break;
				case border_color:
					break;
				case border_image:
					break;
				case border_left:
					break;
				case border_left_color:
					break;
				case border_left_style:
					break;
				case border_left_width:
					break;
				case border_radius:
					break;
				case border_right:
					break;
				case border_right_color:
					break;
				case border_right_style:
					break;
				case border_right_width:
					break;
				case border_spacing:
					break;
				case border_style:
					break;
				case border_top:
					break;
				case border_top_color:
					break;
				case border_top_left_radius:
					break;
				case border_top_right_radius:
					break;
				case border_top_style:
					break;
				case border_top_width:
					break;
				case border_width:
					break;
				case bottom:
					break;
				case box_shadow:
					break;
				case box_sizing:
					break;
				case caption_side:
					break;
				case clear:
					break;
				case clip:
					break;
				case color:
					break;
				case column_count:
					break;
				case column_gap:
					break;
				case column_rule:
					break;
				case column_width:
					break;
				case columns:
					break;
				case content:
					break;
				case counter_increment:
					break;
				case counter_reset:
					break;
				case cursor:
					break;
				case direction:
					break;
				case display:
					break;
				case empty_cells:
					break;
				case font			: return null;
				case font_family	: return font;
				case font_size		: return font;
				case font_stretch	: return font;
				case font_style		: return font;
				case font_variant	: return font;
				case font_weight	: return font;
				case height:
					break;
				case left:
					break;
				case letter_spacing:
					break;
				case line_height:
					break;
				case list_style:
					break;
				case list_style_image:
					break;
				case list_style_position:
					break;
				case list_style_type:
					break;
				case margin:
					break;
				case margin_bottom:
					break;
				case margin_left:
					break;
				case margin_right:
					break;
				case margin_top:
					break;
				case max_height:
					break;
				case max_width:
					break;
				case min_height:
					break;
				case min_width:
					break;
				case opacity:
					break;
				case orphans:
					break;
				case outline:
					break;
				case outline_color:
					break;
				case outline_offset:
					break;
				case outline_style:
					break;
				case outline_width:
					break;
				case overflow:
					break;
				case overflow_x:
					break;
				case overflow_y:
					break;
				case padding:
					break;
				case padding_bottom:
					break;
				case padding_left:
					break;
				case padding_right:
					break;
				case padding_top:
					break;
				case page_break_after:
					break;
				case page_break_before:
					break;
				case page_break_inside:
					break;
				case position:
					break;
				case quotes:
					break;
				case resize:
					break;
				case right:
					break;
				case tab_size:
					break;
				case table_layout:
					break;
				case text_align:
					break;
				case text_align_last:
					break;
				case text_decoration:
					break;
				case text_decoration_color:
					break;
				case text_decoration_line:
					break;
				case text_decoration_style:
					break;
				case text_indent:
					break;
				case text_overflow:
					break;
				case text_shadow:
					break;
				case text_transform:
					break;
				case top:
					break;
				case transform:
					break;
				case transform_origin:
					break;
				case transition:
					break;
				case transition_delay:
					break;
				case transition_duration:
					break;
				case transition_property:
					break;
				case transition_timing_function:
					break;
				case unicode_bidi:
					break;
				case vertical_align:
					break;
				case visibility:
					break;
				case white_space:
					break;
				case widows:
					break;
				case width:
					break;
				case word_break:
					break;
				case word_spacing:
					break;
				case word_wrap:
					break;
				case writing_mode:
					break;
				case z_index:
					break;
				default :
					throw new UnsupportedOperationException("Property name ["+property+"] is not supported yet");
			}
		}
		return null;
	}

	public static StylePropertiesSupported[] getProperties4Aggregate(final StylePropertiesSupported aggregateProperty) {
		if (aggregateProperty == null) {
			throw new NullPointerException("Aggregate property name can't be null");
		}
		else {
			switch (aggregateProperty) {
				case _float:
					break;
				case background:
					break;
				case background_attachment:
					break;
				case background_clip:
					break;
				case background_color:
					break;
				case background_image:
					break;
				case background_origin:
					break;
				case background_position:
					break;
				case background_repeat:
					break;
				case background_size:
					break;
				case border:
					break;
				case border_bottom:
					break;
				case border_bottom_color:
					break;
				case border_bottom_left_radius:
					break;
				case border_bottom_right_radius:
					break;
				case border_bottom_style:
					break;
				case border_bottom_width:
					break;
				case border_collapse:
					break;
				case border_color:
					break;
				case border_image:
					break;
				case border_left:
					break;
				case border_left_color:
					break;
				case border_left_style:
					break;
				case border_left_width:
					break;
				case border_radius:
					break;
				case border_right:
					break;
				case border_right_color:
					break;
				case border_right_style:
					break;
				case border_right_width:
					break;
				case border_spacing:
					break;
				case border_style:
					break;
				case border_top:
					break;
				case border_top_color:
					break;
				case border_top_left_radius:
					break;
				case border_top_right_radius:
					break;
				case border_top_style:
					break;
				case border_top_width:
					break;
				case border_width:
					break;
				case bottom:
					break;
				case box_shadow:
					break;
				case box_sizing:
					break;
				case caption_side:
					break;
				case clear:
					break;
				case clip:
					break;
				case color:
					break;
				case column_count:
					break;
				case column_gap:
					break;
				case column_rule:
					break;
				case column_width:
					break;
				case columns:
					break;
				case content:
					break;
				case counter_increment:
					break;
				case counter_reset:
					break;
				case cursor:
					break;
				case direction:
					break;
				case display:
					break;
				case empty_cells:
					break;
				case font			: return new StylePropertiesSupported[]{font_family, font_size, font_stretch, font_style, font_variant, font_weight};
				case font_family	: return null;
				case font_size		: return null;
				case font_stretch	: return null;
				case font_style		: return null;
				case font_variant	: return null;
				case font_weight	: return null;
				case height:
					break;
				case left:
					break;
				case letter_spacing:
					break;
				case line_height:
					break;
				case list_style:
					break;
				case list_style_image:
					break;
				case list_style_position:
					break;
				case list_style_type:
					break;
				case margin:
					break;
				case margin_bottom:
					break;
				case margin_left:
					break;
				case margin_right:
					break;
				case margin_top:
					break;
				case max_height:
					break;
				case max_width:
					break;
				case min_height:
					break;
				case min_width:
					break;
				case opacity:
					break;
				case orphans:
					break;
				case outline:
					break;
				case outline_color:
					break;
				case outline_offset:
					break;
				case outline_style:
					break;
				case outline_width:
					break;
				case overflow:
					break;
				case overflow_x:
					break;
				case overflow_y:
					break;
				case padding:
					break;
				case padding_bottom:
					break;
				case padding_left:
					break;
				case padding_right:
					break;
				case padding_top:
					break;
				case page_break_after:
					break;
				case page_break_before:
					break;
				case page_break_inside:
					break;
				case position:
					break;
				case quotes:
					break;
				case resize:
					break;
				case right:
					break;
				case tab_size:
					break;
				case table_layout:
					break;
				case text_align:
					break;
				case text_align_last:
					break;
				case text_decoration:
					break;
				case text_decoration_color:
					break;
				case text_decoration_line:
					break;
				case text_decoration_style:
					break;
				case text_indent:
					break;
				case text_overflow:
					break;
				case text_shadow:
					break;
				case text_transform:
					break;
				case top:
					break;
				case transform:
					break;
				case transform_origin:
					break;
				case transition:
					break;
				case transition_delay:
					break;
				case transition_duration:
					break;
				case transition_property:
					break;
				case transition_timing_function:
					break;
				case unicode_bidi:
					break;
				case vertical_align:
					break;
				case visibility:
					break;
				case white_space:
					break;
				case widows:
					break;
				case width:
					break;
				case word_break:
					break;
				case word_spacing:
					break;
				case word_wrap:
					break;
				case writing_mode:
					break;
				case z_index:
					break;
				default :
					throw new UnsupportedOperationException("Property name ["+aggregateProperty+"] is not supported yet");
			}
		}
		return null;
	}

	
	public static EnumMap<StylePropertiesSupported,String> disassemble(final StylePropertiesSupported prop, final String value) {
		return null;
	}	
	
	public static String assemble(final StylePropertiesSupported prop, final EnumMap<StylePropertiesSupported,String> values) {
		return null;
	}	
	
	private static void advancedPreparation() {
		for (StylePropertiesSupported item : values()) {
			parseValues(item.values,item.valueIds,item);
			if (item.canUseKeywords()) {
				parseKeywords(item.values,item.valueKeywords);
			}
		}
	}
	
	private static void parseValues(final ValueListDescriptor values, final long[] result, final StylePropertiesSupported item) {
    	for (int index = 0; index < result.length; index++) {
    		if ((result[index] = NAMES.seekName(values.content[index])) < 0) {
    			result[index] = NAMES.placeName(values.content[index],new StylePropertiesSupported[] {item});
    		}
    		else {
    			final StylePropertiesSupported[]	cargo = NAMES.getCargo(result[index]);
    			final StylePropertiesSupported[]	newCargo = Arrays.copyOf(cargo,cargo.length+1);
    			
    			newCargo[newCargo.length-1] = item;
        		NAMES.setCargo(result[index], newCargo);
    		}
    	}
	}

	private static void parseKeywords(final ValueListDescriptor values, final Keyword[] result) {
    	for (int index = 0; index < values.getContent().length; index++) {
    		final long	id = NAMES.seekName(values.getContent()[index]);
    		result[index] = new Keyword(id,NAMES.getName(id));
    	}
	}
	
	private static int calculateAmountOfKeywords(final ContentType contentType, final ValueListDescriptor values) {
		int		result = 0;
		
		switch (contentType) {
			case asIs : case color : case compoundChoise : case compoundSequence : case distance : case function : case integer :
			case number : case string : case subStyle : case time : case url : case value :
				break;
			case colorOrKeyword : case distanceOrKeyword : case functionOrKeyword : case integerOrKeyword : case numberOrKeyword : case stringOrKeyword :
			case timeOrKeyword : case urlOrKeyword :
				result += values.content.length;
				break;
			default	:
				throw new UnsupportedOperationException("Content type ["+contentType+"] is not supported yet");
		}
		return result;
	}
	
    private <T> T seekKeyword(final long id) {
		for (Keyword item : getKeywordsSupported()) {
			if (item.getId() == id) {
				return (T)item;
			}
		}
    	return null;
	}
	
    private boolean isKeywordValid(final long id, final StylePropertiesSupported stylePropertiesSupported) {
    	if (id >= 0) {
			final StylePropertiesSupported[]	cargo = NAMES.getCargo(id);
			
			if (cargo == null) {
				return false;
			}
			else {
				for (StylePropertiesSupported item : cargo) {
					if (item == this) {
						return true;
					}
				}
				return false;
			}
    	}
    	else {
    		return false;
    	}
	}
}