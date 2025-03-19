package chav1961.purelib.enumerations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.EnumMap;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CSSUtils;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.intern.UnsafedCharUtils;

/**
 * <p>This enumeration describes supported CSS style properties.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public enum StylePropertiesSupported {
		/**
		 * <p>Style properties - background</p>  
		 */
		background("background",true,ContentType.compoundChoise,1,Integer.MAX_VALUE,new ValueListDescriptor(ContentType.subStyle,1,1,"background-attachment","background-color","background-image","background-position","background-repeat")),
		/**
		 * <p>Style properties - background-attachment</p>  
		 */
		background_attachment("background-attachment",true,ContentType.value,new ValueListDescriptor(ContentType.value,0,Integer.MAX_VALUE,"fixed","scroll","local")),
		/**
		 * <p>Style properties - background-clip</p>  
		 */
		background_clip("background-clip",false,ContentType.value,new ValueListDescriptor(ContentType.value,0,Integer.MAX_VALUE,"padding-box","border-box","content-box")),
		/**
		 * <p>Style properties - background-color</p>  
		 */
		background_color("background-color",true,ContentType.color),
		/**
		 * <p>Style properties - background-image</p>  
		 */
		background_image("background-image",true,ContentType.urlOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		/**
		 * <p>Style properties - background-origin</p>  
		 */
		background_origin("background-origin",false,ContentType.value,new ValueListDescriptor(ContentType.value,0,Integer.MAX_VALUE,"padding-box","border-box","content-box")),
		/**
		 * <p>Style properties - background-position</p>  
		 */
		background_position("background-position",true,ContentType.distanceOrKeyword,1,2,new ValueListDescriptor(ContentType.value,1,1,"left","center","right","top","center","bottom")),
		/**
		 * <p>Style properties - background-repeat</p>  
		 */
		background_repeat("background-repeat",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,2,"no-repeat","repeat","repeat-x","repeat-y")),
		/**
		 * <p>Style properties - background-size</p>  
		 */
		background_size("background-size",true,ContentType.distanceOrKeyword,1,2,new ValueListDescriptor(ContentType.value,1,1,"auto","cover","contain")),
		
		/**
		 * <p>Style properties - border</p>  
		 */
		border("border",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		/**
		 * <p>Style properties - border-bottom</p>  
		 */
		border_bottom("border-bottom",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		/**
		 * <p>Style properties - border-bottom-color</p>  
		 */
		border_bottom_color("border-bottom-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		/**
		 * <p>Style properties - border-bottom-left-radius</p>  
		 */
		border_bottom_left_radius("border-bottom-left-radius",false,ContentType.distance,1,2),
		/**
		 * <p>Style properties - border-bottom-right-radius</p>  
		 */
		border_bottom_right_radius("border-bottom-right-radius",false,ContentType.distance,1,2),
		/**
		 * <p>Style properties - border-bottom-style</p>  
		 */
		border_bottom_style("border-bottom-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		/**
		 * <p>Style properties - border-bottom-width</p>  
		 */
		border_bottom_width("border-bottom-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		/**
		 * <p>Style properties - border-collapse</p>  
		 */
		border_collapse("border-collapse",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"collapse","separate")),
		/**
		 * <p>Style properties - border-color</p>  
		 */
		border_color("border-color",true,ContentType.colorOrKeyword,1,4,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		/**
		 * <p>Style properties - border-image</p>  
		 */
		border_image("border-image",false,ContentType.asIs),
		/**
		 * <p>Style properties - border-left</p>  
		 */
		border_left("border-left",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		/**
		 * <p>Style properties - border-left-color</p>  
		 */
		border_left_color("border-left-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		/**
		 * <p>Style properties - border-left-style</p>  
		 */
		border_left_style("border-left-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		/**
		 * <p>Style properties - border-left-width</p>  
		 */
		border_left_width("border-left-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		/**
		 * <p>Style properties - border-radius</p>  
		 */
		border_radius("border-radius",false,ContentType.asIs),
		/**
		 * <p>Style properties - border-right</p>  
		 */
		border_right("border-right",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		/**
		 * <p>Style properties - border-right-color</p>  
		 */
		border_right_color("border-right-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		/**
		 * <p>Style properties - border-right-style</p>  
		 */
		border_right_style("border-right-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		/**
		 * <p>Style properties - border-right-width</p>  
		 */
		border_right_width("border-right-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		/**
		 * <p>Style properties - border-spacing</p>  
		 */
		border_spacing("border-spacing",false,ContentType.distance,1,2),
		/**
		 * <p>Style properties - border-style</p>  
		 */
		border_style("border-style",true,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"border-top-style","border-right-style","border-bottom-style","border-left-style")),
		/**
		 * <p>Style properties - border-top</p>  
		 */
		border_top("border-top",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		/**
		 * <p>Style properties - border-top-color</p>  
		 */
		border_top_color("border-top-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"transparent")),
		/**
		 * <p>Style properties - border-top-left-radius</p>  
		 */
		border_top_left_radius("border-top-left-radius",false,ContentType.distance,1,2),
		/**
		 * <p>Style properties - border-top-right-radius</p>  
		 */
		border_top_right_radius("border-top-right-radius",false,ContentType.distance,1,2),
		/**
		 * <p>Style properties - border-top-style</p>  
		 */
		border_top_style("border-top-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","hidden","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		/**
		 * <p>Style properties - border-top-width</p>  
		 */
		border_top_width("border-top-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		/**
		 * <p>Style properties - border-width</p>  
		 */
		border_width("border-width",true,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"border-top-width","border-right-width","border-bottom-width","border-left-width")),
		
		/**
		 * <p>Style properties - bottom</p>  
		 */
		bottom("bottom",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		/**
		 * <p>Style properties - box-shadow</p>  
		 */
		box_shadow("box-shadow",false,ContentType.asIs),
		/**
		 * <p>Style properties - box-sizing</p>  
		 */
		box_sizing("box-sizing",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"content-box","border-box","padding-box")),
		
		/**
		 * <p>Style properties - caption-side</p>  
		 */
		caption_side("caption-side",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"top","bottom","left","right")),
		/**
		 * <p>Style properties - clear</p>  
		 */
		clear("clear",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"top","none","left","right","both")),
		/**
		 * <p>Style properties - clip</p>  
		 */
		clip("clip",true,ContentType.asIs),
		/**
		 * <p>Style properties - color</p>  
		 */
		color("color",true,ContentType.color),
		
		/**
		 * <p>Style properties - column-count</p>  
		 */
		column_count("column-count",false,ContentType.integerOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		/**
		 * <p>Style properties - column-gap</p>  
		 */
		column_gap("column-gap",false,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"normal")),
		/**
		 * <p>Style properties - column-rule</p>  
		 */
		column_rule("column-rule",false,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"border-width","border-style","border-color")),
		/**
		 * <p>Style properties - column-width</p>  
		 */
		column_width("column-width",false,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		/**
		 * <p>Style properties - columns</p>  
		 */
		columns("columns",false,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"column-width","column-count")),
		/**
		 * <p>Style properties - content</p>  
		 */
		content("content",true,ContentType.asIs),
		
		/**
		 * <p>Style properties - counter-increment</p>  
		 */
		counter_increment("counter-increment",true,ContentType.asIs),
		/**
		 * <p>Style properties - counter-reset</p>  
		 */
		counter_reset("counter-reset",true,ContentType.asIs),
		
		/**
		 * <p>Style properties - cursor</p>  
		 */
		cursor("cursor",true,ContentType.asIs),
		
		/**
		 * <p>Style properties - direction</p>  
		 */
		direction("direction",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"ltr","rtl")),
		/**
		 * <p>Style properties - display</p>  
		 */
		display("display",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"block","inline","inline-block","inline-table","list-item","none","run-in","table","table-caption","table-cell","table-column-group","table-column","table-footer-group","table-header-group","table-row","table-row-group")),

		/**
		 * <p>Style properties - empty-cells</p>  
		 */
		empty_cells("empty-cells",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"show","hide")),
		
		/**
		 * <p>Style properties - float</p>  
		 */
		_float("float",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"left","right","none")),

		/**
		 * <p>Style properties - font</p>  
		 */
		font("font",true,ContentType.asIs),
		/**
		 * <p>Style properties - font-family</p>  
		 */
		font_family("font-family",true,ContentType.string,1,Integer.MAX_VALUE),
		/**
		 * <p>Style properties - font-size</p>  
		 */
		font_size("font-size",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"xx-small","x-small","small","medium","large","x-large","xx-large","larger","smaller")),
		/**
		 * <p>Style properties - font-stretch</p>  
		 */
		font_stretch("font-stretch",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"ultra-condensed","extra-condensed","condensed","semi-condensed","normal","semi-expanded","expanded","extra-expanded","ultra-expanded")),
		/**
		 * <p>Style properties - font-style</p>  
		 */
		font_style("font-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","italic","oblique")),
		/**
		 * <p>Style properties - font-variant</p>  
		 */
		font_variant("font-variant",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","small-caps")),
		/**
		 * <p>Style properties - font-weight</p>  
		 */
		font_weight("font-weight",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"bold","bolder","lighter","normal","100","200","300","400","500","600","700","800","900")),
		
		/**
		 * <p>Style properties - height</p>  
		 */
		height("height",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		/**
		 * <p>Style properties - left</p>  
		 */
		left("left",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		/**
		 * <p>Style properties - letter-spacing</p>  
		 */
		letter_spacing("letter-spacing",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"normal")),
		/**
		 * <p>Style properties - line-height</p>  
		 */
		line_height("line-height",true,ContentType.asIs),
		
		/**
		 * <p>Style properties - list-style</p>  
		 */
		list_style("list-style",true,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"list-style-type","list-style-position","list-style-image")),
		/**
		 * <p>Style properties - list-style-image</p>  
		 */
		list_style_image("list-style-image",true,ContentType.urlOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		/**
		 * <p>Style properties - list-style-position</p>  
		 */
		list_style_position("list-style-position",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"inside","outside")),
		/**
		 * <p>Style properties - list-style-type</p>  
		 */
		list_style_type("list-style-type",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"circle","disc","square","armenian","decimal","decimal-leading-zero","georgian","lower-alpha","lower-greek","lower-latin","lower-roman","upper-alpha","upper-latin","upper-roman","none")),
		
		/**
		 * <p>Style properties - margin</p>  
		 */
		margin("margin",true,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"margin-top","margin-right","margin-bottom","margin-left")),
		/**
		 * <p>Style properties - margin-bottom</p>  
		 */
		margin_bottom("margin-bottom",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		/**
		 * <p>Style properties - margin-left</p>  
		 */
		margin_left("margin-left",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		/**
		 * <p>Style properties - margin-right</p>  
		 */
		margin_right("margin-right",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		/**
		 * <p>Style properties - margin-top</p>  
		 */
		margin_top("margin-top",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		/**
		 * <p>Style properties - max-height</p>  
		 */
		max_height("max-height",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		/**
		 * <p>Style properties - max-width</p>  
		 */
		max_width("max-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		
		/**
		 * <p>Style properties - min-height</p>  
		 */
		min_height("min-height",true,ContentType.distance),
		/**
		 * <p>Style properties - min-width</p>  
		 */
		min_width("min-width",true,ContentType.distance),

		/**
		 * <p>Style properties - opacity</p>  
		 */
		opacity("opacity",false,ContentType.number),
		/**
		 * <p>Style properties - orphans</p>  
		 */
		orphans("orphans",true,ContentType.integer),
		
		/**
		 * <p>Style properties - outline</p>  
		 */
		outline("outline",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.subStyle,1,1,"outline-color","outline-style","outline-width")),
		/**
		 * <p>Style properties - outline-color</p>  
		 */
		outline_color("outline-color",true,ContentType.colorOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"invert")),
		/**
		 * <p>Style properties - outline-offset</p>  
		 */
		outline_offset("outline-offset",true,ContentType.distance),
		/**
		 * <p>Style properties - outline-style</p>  
		 */
		outline_style("outline-style",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","dotted","dashed","solid","double","groove","ridge","inset","outset")),
		/**
		 * <p>Style properties - outline-width</p>  
		 */
		outline_width("outline-width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"thin","medium","thick")),
		
		/**
		 * <p>Style properties - overflow</p>  
		 */
		overflow("overflow",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","hidden","scroll","visible")),
		/**
		 * <p>Style properties - overflow-x</p>  
		 */
		overflow_x("overflow-x",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","hidden","scroll","visible")),
		/**
		 * <p>Style properties - overflow-y</p>  
		 */
		overflow_y("overflow-y",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","hidden","scroll","visible")),
		
		/**
		 * <p>Style properties - padding</p>  
		 */
		padding("padding",true,ContentType.compoundSequence,new ValueListDescriptor(ContentType.subStyle,1,1,"padding-top","padding-right","padding-bottom","padding-left")),
		/**
		 * <p>Style properties - padding-bottom</p>  
		 */
		padding_bottom("padding-bottom",true,ContentType.distance),
		/**
		 * <p>Style properties - padding-left</p>  
		 */
		padding_left("padding-left",true,ContentType.distance),
		/**
		 * <p>Style properties - padding-right</p>  
		 */
		padding_right("padding-right",true,ContentType.distance),
		/**
		 * <p>Style properties - padding-top</p>  
		 */
		padding_top("padding-top",true,ContentType.distance),
		
		/**
		 * <p>Style properties - page-break-after</p>  
		 */
		page_break_after("page-break-after",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"always","auto","avoid","left","right")),
		/**
		 * <p>Style properties - page-break-before</p>  
		 */
		page_break_before("page-break-before",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"always","auto","avoid","left","right")),
		/**
		 * <p>Style properties - page-break-inside</p>  
		 */
		page_break_inside("page-break-inside",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","avoid")),
		
		/**
		 * <p>Style properties - position</p>  
		 */
		position("position",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"absolute","fixed","relative","static")),

		/**
		 * <p>Style properties - quotes</p>  
		 */
		quotes("quotes",true,ContentType.asIs),

		/**
		 * <p>Style properties - resize</p>  
		 */
		resize("resize",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"none","both","horizontal","vertical")),
		/**
		 * <p>Style properties - right</p>  
		 */
		right("right",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		/**
		 * <p>Style properties - tab-size</p>  
		 */
		tab_size("tab-size",false,ContentType.integer),
		/**
		 * <p>Style properties - table-layout</p>  
		 */
		table_layout("table-layout",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"auto","fixed")),
		
		/**
		 * <p>Style properties - text-align</p>  
		 */
		text_align("text-align",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"center","justify","left","right","start","end")),
		/**
		 * <p>Style properties - text-align-last</p>  
		 */
		text_align_last("text-align-last",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"center","justify","left","right","start","end")),
		/**
		 * <p>Style properties - text-decoration</p>  
		 */
		text_decoration("text-decoration",true,ContentType.compoundChoise,new ValueListDescriptor(ContentType.value,1,1,"blink","line-through","overline","underline","none")),
		/**
		 * <p>Style properties - text-decoration-color</p>  
		 */
		text_decoration_color("text-decoration-color",false,ContentType.color),
		/**
		 * <p>Style properties - text-decoration-line</p>  
		 */
		text_decoration_line("text-decoration-line",false,ContentType.asIs),
		/**
		 * <p>Style properties - text-decoration-style</p>  
		 */
		text_decoration_style("text-decoration-style",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"solid","double","dotted","dashed")),
		/**
		 * <p>Style properties - text-indent</p>  
		 */
		text_indent("text-indent",true,ContentType.distance),
		/**
		 * <p>Style properties - text-overflow</p>  
		 */
		text_overflow("text-overflow",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"clip","ellipsis")),
		/**
		 * <p>Style properties - text-shadow</p>  
		 */
		text_shadow("text-shadow",true,ContentType.asIs),
		/**
		 * <p>Style properties - text-transform</p>  
		 */
		text_transform("text-transform",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"capitalize","lowercase","uppercase","none")),
		
		/**
		 * <p>Style properties - top</p>  
		 */
		top("top",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),
		
		/**
		 * <p>Style properties - transform</p>  
		 */
		transform("transform",true,ContentType.functionOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"none")),
		/**
		 * <p>Style properties - transform-origin</p>  
		 */
		transform_origin("transform-origin",true,ContentType.asIs),
		
		/**
		 * <p>Style properties - transition</p>  
		 */
		transition("transition",true,ContentType.asIs),
		/**
		 * <p>Style properties - transition-delay</p>  
		 */
		transition_delay("transition-delay",false,ContentType.time,1,Integer.MAX_VALUE),
		/**
		 * <p>Style properties - transition-duration</p>  
		 */
		transition_duration("transition-duration",false,ContentType.time,1,Integer.MAX_VALUE),
		/**
		 * <p>Style properties - transition-property</p>  
		 */
		transition_property("transition-property",false,ContentType.stringOrKeyword,1,Integer.MAX_VALUE,new ValueListDescriptor(ContentType.value,1,1,"none","all")),
		/**
		 * <p>Style properties - transition-timing-function</p>  
		 */
		transition_timing_function("transition-timing-function",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"ease","ease-in","ease-out","ease-in-out","linear","step-start","step-end","steps","cubic-bezier")),
		
		/**
		 * <p>Style properties - unicode-bidi</p>  
		 */
		unicode_bidi("unicode-bidi",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","embed","bidi-override")),
		
		/**
		 * <p>Style properties - vertical-align</p>  
		 */
		vertical_align("vertical-align",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"baseline","bottom","middle","sub","super","text-bottom","text-top","top")),
		/**
		 * <p>Style properties - visibility</p>  
		 */
		visibility("visibility",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"visible","hidden","collapse")),
		
		/**
		 * <p>Style properties - white-space</p>  
		 */
		white_space("white-space",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","nowrap","pre","pre-line","pre-wrap")),
		/**
		 * <p>Style properties - widows</p>  
		 */
		widows("widows",true,ContentType.integer),
		/**
		 * <p>Style properties - width</p>  
		 */
		width("width",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto")),

		/**
		 * <p>Style properties - word-break</p>  
		 */
		word_break("word-break",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","break-all","keep-all")),
		/**
		 * <p>Style properties - word-spacing</p>  
		 */
		word_spacing("word-spacing",true,ContentType.distanceOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"normal")),
		/**
		 * <p>Style properties - word-wrap</p>  
		 */
		word_wrap("word-wrap",true,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"normal","break-word")),
		
		/**
		 * <p>Style properties - writing-mode</p>  
		 */
		writing_mode("writing-mode",false,ContentType.value,new ValueListDescriptor(ContentType.value,1,1,"lr-tb","rl-tb","tb-rl","bt-rl","tb-lr","bt-lr")),
		
		/**
		 * <p>Style properties - z-index</p>  
		 */
		z_index("z-index",true,ContentType.integerOrKeyword,new ValueListDescriptor(ContentType.value,1,1,"auto"));

	/**
	 * <p>Inherited keyvord value</p> 
	 */
	public static final String	INHERITED_KEYWORD = "inherited";
	
	/**
	 * <p>This enumeration describes supported content type of the CSS style properties.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static enum ContentType {
		/**
		 * <p>Content type - any value</p>
		 */
		value, 
		/**
		 * <p>Content type - integer value</p>
		 */
		integer, 
		/**
		 * <p>Content type - string value</p>
		 */
		string, 
		/**
		 * <p>Content type - string value or keyword</p>
		 */
		stringOrKeyword, 
		/**
		 * <p>Content type - integer value or keyword</p>
		 */
		integerOrKeyword, 
		/**
		 * <p>Content type - number value</p>
		 */
		number, 
		/**
		 * <p>Content type - number value or keyword</p>
		 */
		numberOrKeyword, 
		/**
		 * <p>Content type - color descriptor</p>
		 */
		color, 
		/**
		 * <p>Content type - color descriptor or keyword</p>
		 */
		colorOrKeyword,
		/**
		 * <p>Content type - distance descriptor</p>
		 */
		distance, 
		/**
		 * <p>Content type - distance descriptor or keyword</p>
		 */
		distanceOrKeyword, 
		/**
		 * <p>Content type - time descriptor</p>
		 */
		time, 
		/**
		 * <p>Content type - time descriptor or keyword</p>
		 */
		timeOrKeyword, 
		/**
		 * <p>Content type - URL descriptor</p>
		 */
		url, 
		/**
		 * <p>Content type - URL descriptor or keyword</p>
		 */
		urlOrKeyword, 
		/**
		 * <p>Content type - function</p>
		 */
		function, 
		/**
		 * <p>Content type - function or keyword</p>
		 */
		functionOrKeyword,
		/**
		 * <p>Content type - compound choice</p>
		 */
		compoundChoise, 
		/**
		 * <p>Content type - compound sequence</p>
		 */
		compoundSequence, 
		/**
		 * <p>Content type - sub-style</p>
		 */
		subStyle, 
		/**
		 * <p>Content type - raw data</p>
		 */
		asIs
	}

	/**
	 * <p>This class is a keyword descriptor for the CSS style properties.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static class Keyword {
		/**
		 * <p>Reserver 'inherited' keyword</p>
		 */
		public static final Keyword	INHERITED = new Keyword(StylePropertiesSupported.INHERITED_ID,StylePropertiesSupported.INHERITED_KEYWORD); 
		
		private final long		id;
		private final String	name;
		
		/**
		 * <p>Constructor if the class instance</p>
		 * @param id keyword ID. Must be greater or equals than 0.
		 * @param name keyword name.
		 */
		public Keyword(final long id, final String name) throws IllegalArgumentException {
			if (id < 0) {
				throw new IllegalArgumentException("Keyword ID ["+id+"] must be greater or equals than 0");
			}
			else if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Keyword name can be neither null nor empty");
			}
			else {
				this.id = id;
				this.name = name;
			}
		}

		/**
		 * <p>Get keyword ID.</p>
		 * @return keyword ID. Can't be less than 0.
		 */
		public long getId() {
			return id;
		}
		
		/**
		 * <p>Get keyword name.</p>
		 * @return keyword name. Can be neither null nor empty.
		 */
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
	private static final Object[]		URL_LEXEMAS = {"url".toCharArray(),'(',CharUtils.ArgumentType.simpleTerminatedString,')'};
	
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
    	INHERITED_ID = NAMES.placeName((CharSequence)INHERITED_KEYWORD,null);
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
							return UnsafedCharUtils.uncheckedValidateNumber(value,from,CharUtils.PREF_INT|CharUtils.PREF_LONG,true) >= 0;
						case numberOrKeyword	:
							if (id >= 0) {
								return isKeywordValid(id,this);
							}
						case number	:
							return UnsafedCharUtils.uncheckedValidateNumber(value,from,CharUtils.PREF_ANY,true) >= 0;
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
									
							UnsafedCharUtils.uncheckedParseNumber(value,from,result,CharUtils.PREF_INT|CharUtils.PREF_LONG,true);
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
							
							UnsafedCharUtils.uncheckedParseNumber(value,from,numResult,CharUtils.PREF_ANY,true);
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
				//throw new UnsupportedOperationException("Content type ["+contentType+"] is not supported yet");
				return 0;
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