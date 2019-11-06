package chav1961.purelib.basic;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import chav1961.purelib.basic.CharUtils.ArgumentType;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper.Locker;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.enumerations.StylePropertiesSupported;
import chav1961.purelib.enumerations.StylePropertiesSupported.ContentType;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.interfaces.JsonStaxParserInterface;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class XMLUtils {
	private static final char[]		AVAILABLE_IN_NAMES = {'-'};
	private static final String		PROP_ATTR_NAME_HAS_FIXED = "hasFixed";
	private static final String		PROP_ATTR_NAME_VALUE_TYPE = "valueType";
	private static final String		PROP_ATTR_NAME_INHERITANCE_AVAILABLE = "inheritanceAvailable";
	private static final String		PROP_ATTR_NAME_URL_AVAILABLE = "urlAvailable";
	private static final String		PROP_ATTR_NAME_CHILDREN = "children";
	private static final String		PROP_ATTR_NAME_FORMAT = "format";
	private static final String		PROP_ATTR_NAME_PARSER_CLASS = "parserClass";
	private static final String		PROP_ATTR_NAME_PARSER_METHOD = "parserMethod";

	private static final Object[]	COLOR_HEX_TEMPLATE = {'#',ArgumentType.hexInt};	
	private static final char[]		COLOR_RGBA = "rgba".toCharArray();
	private static final Object[]	COLOR_RGBA_TEMPLATE = {COLOR_RGBA,'(',ArgumentType.ordinalInt,',',ArgumentType.ordinalInt,',',ArgumentType.ordinalInt,',',ArgumentType.ordinalInt,')'};
	private static final char[]		COLOR_RGB = "rgb".toCharArray();
	private static final Object[]	COLOR_RGB_TEMPLATE = {COLOR_RGB,'(',ArgumentType.ordinalInt,',',ArgumentType.ordinalInt,',',ArgumentType.ordinalInt,')'};
	private static final char[]		COLOR_HSLA = "hsla".toCharArray();
	private static final Object[]	COLOR_HSLA_TEMPLATE = {COLOR_HSLA,'(',ArgumentType.ordinalInt,',',ArgumentType.ordinalFloat,'%',',',ArgumentType.ordinalFloat,'%',',',ArgumentType.ordinalFloat,'%',')'};
	private static final char[]		COLOR_HSL = "hsl".toCharArray();
	private static final Object[]	COLOR_HSL_TEMPLATE = {COLOR_HSL,'(',ArgumentType.ordinalInt,',',ArgumentType.ordinalFloat,'%',',',ArgumentType.ordinalFloat,'%',')'};

	private static final SyntaxTreeInterface<StylePropValue<?>[]>		KEYWORDS = new AndOrTree<>();
	private static final SyntaxTreeInterface<StylePropertiesSupported>	STYLES = new AndOrTree<>();
	private static final long											INHERITED;

	@FunctionalInterface
	private interface StyleValueConverter {
		Object convert(String source) throws SyntaxException; 
	}
	
	static {
		INHERITED = KEYWORDS.placeName("inherited",null);
//		for (StylePropertiesSupported item : StylePropertiesSupported.values()) {
//			final ContentType 	ct;
//			
//			switch (ct = item.getContentType()) {
//				case color:
//					addName(KEYWORDS,item,item.getValues());
//					break;
//				case colorOrKeyword		:
//					addName(KEYWORDS,item,item.getValues());
//					break;
//				case distanceOrKeyword	:
//					addName(KEYWORDS,item,item.getValues());
//					break;
//				case functionOrKeyword	:
//					addName(KEYWORDS,item,item.getValues());
//					break;
//				case integerOrKeyword	:
//					addName(KEYWORDS,item,item.getValues());
//					break;
//				case numberOrKeyword	:
//					addName(KEYWORDS,item,item.getValues());
//					break;
//				case stringOrKeyword	:
//					addName(KEYWORDS,item,item.getValues());
//					break;
//				case timeOrKeyword		:
//					addName(KEYWORDS,item,item.getValues());
//					break;
//				case urlOrKeyword		:
//					addName(KEYWORDS,item,item.getValues());
//					break;
//				case value				:
//					addName(KEYWORDS,item,item.getValues());
//					break;
//				case asIs:
//				case compoundChoise:
//				case compoundSequence:
//				case distance:
//				case function:
//				case integer:
//				case number:
//				case string:
//				case subStyle:
//				case time:
//				case url:
//					addName(KEYWORDS,item,item.getValues());
//					break;
//				default:
//					throw new UnsupportedOperationException("Content type ["+ct+"] is not supported yet");
//			}
//			STYLES.placeName(item.name(),item);
//			STYLES.placeName(item.getExternalName(),item);
//		}
	}
	
	@FunctionalInterface
	public interface XMLWalkerCallback {
		ContinueMode process(NodeEnterMode mode, Element node) throws ContentException;
	}
	
	public static ContinueMode walkDownXML(final Element root, final XMLWalkerCallback callback) throws ContentException {
		return walkDownXML(root,-1L,callback);
	}

	public static ContinueMode walkDownXML(final Element root, final long nodeTypes, final XMLWalkerCallback callback) throws ContentException {
		if (root == null) {
			throw new NullPointerException("Root element can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Walker callback can't be null"); 
		}
		else {
			return walkDownXMLInternal(root, nodeTypes, callback); 
		}
	}

	public static <T> T getAttribute(final Element node, final String attribute, final Class<T> awaited) {
		return getAttribute(node,attribute,awaited,null);
	}
	
	public static <T> T getAttribute(final Element node, final String attribute, final Class<T> awaited, final T defaultValue) throws NullPointerException, IllegalArgumentException {
		if (node == null) {
			throw new NullPointerException("Node can't be null");
		}
		else if (attribute == null || attribute.isEmpty()) {
			throw new IllegalArgumentException("Attribute name can't be null or empty");
		}
		else if (!node.hasAttribute(attribute)) {
			return defaultValue;
		}
		else {
			return SubstitutableProperties.convert(attribute,node.getAttribute(attribute),awaited);
		}
	}
	
	public static Properties getAttributes(final Element node) {
		if (node == null) {
			throw new NullPointerException("Node can't be null");
		}
		else {
			final Properties	result = new Properties();
			final NamedNodeMap 	map = node.getAttributes();
			
			for (int index = 0, maxIndex = map.getLength(); index < maxIndex; index++) {
				result.setProperty(map.item(index).getNodeName(),map.item(index).getNodeValue());
			}
			return result;
		}
	}
	
	public static Properties joinAttributes(final Element node, final Properties toJoin, final boolean retainExistent, final boolean assignJoined) throws NullPointerException {
		if (node == null) {
			throw new NullPointerException("Node can't be null");
		}
		else if (toJoin == null) {
			throw new NullPointerException("Properties to join can't be null");
		}
		else {
			final Properties	current = getAttributes(node);
			
			if (retainExistent) {
				for (Entry<Object, Object> item : toJoin.entrySet()) {
					current.putIfAbsent(item.getKey(),item.getValue());
				}
			}
			else {
				current.putAll(toJoin);
			}
			if (assignJoined) {
				for (Entry<Object, Object> item : current.entrySet()) {
					node.setAttribute(item.getKey().toString(),item.getValue().toString());
				}
			}
			return current;
		}
	}
	
	
	/*  Syntax rules:
		<record>::=<selectors><properties>
		<selectors>::=<selectorGroup>[','...]
		<selectorGroup>::=<subtreeSel>[{'>'|'-->'}<subtreeSel>...]
		<subtreeSel>::=<standaloneSel>[{'~'|'+'}<standaloneSel>...]
		<standaloneSel>=<selItem>['&'<selItem>...]
		<selItem>::={'*'|<name>|'#'<id>|'.'<class>|'::'<pseudoelement>|':'<pseudoclass>|<attrExpr>}
		<pseudoElement>::=<name>
		<pseudoClass>::=<name>['('<number>['n']['+'<number>]')']
		<attrExpr>::='['<name>[<operator><operValue>]']'
		<operator>::={'='|'^='|'|='|'*='|'~='|'$='}
		<operValue>::='"'<any>'"'
		<properties>::='{'<property>':'<value>[';'...]'}'
	 */

	public static Map<String,Properties> parseCSS(final String cssContent) throws SyntaxException, IllegalArgumentException {
		if (cssContent == null || cssContent.isEmpty()) {
			throw new IllegalArgumentException("CSS content can't be null or empty string");
		}
		else {
			final Map<String,Properties>	result = new HashMap<>();
			
			parseCSS(cssContent.toCharArray(),0,result);
			return result;
		}		
	}

	public static int parseCSS(final char[] src, final int from, final Map<String,Properties> result) throws SyntaxException, IllegalArgumentException {
		return parseCSS(src,from,result,new int[2]);
	}
	
	static int parseCSS(final char[] src, int from, final Map<String,Properties> result, final int[] nameRange) throws SyntaxException, IllegalArgumentException {
		final int len;
		
		if (src == null || (len = src.length) == 0) {
			throw new IllegalArgumentException("CSS content can't be null or empty array");
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From index ["+from+"] out of range 0.."+(len-1));
		}
		else {
			final List<CSSLex>	lexemas = new ArrayList<>();
			final StringBuilder	sb = new StringBuilder();
			
loop:		while (from < len) {
				if ((from = CharUtils.skipBlank(src,from,false)) >= len) {
					break;
				}
				switch (src[from]) {
					case '/'	:
						if (src[from+1] == '*') {
							final int 	start = from;
							
							while (src[from] != '\0' && !(src[from] == '*' && src[from+1] == '/')) {
								from++;
							}
							if (src[from] != '\0') {
								from += 2;
							}
							else {
								throw new SyntaxException(0,start,"Unclosed comment");
							}
							continue loop;
						}
						else {
							throw new SyntaxException(0,from,"Illegal character in the source content");
						}
					case '#'	:
						from = CharUtils.parseNameExtended(src,from+1,nameRange,AVAILABLE_IN_NAMES);
						if (nameRange[0] == nameRange[1]) {
							throw new SyntaxException(0,from,"Missing Id name");
						}
						else {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.ID,new String(src,nameRange[0],nameRange[1]-nameRange[0]+1)));
							if (src[from] == '.' || src[from] == '#' || src[from] == ':' || src[from] == '[') {
								lexemas.add(new CSSLex(from,CSSLex.CSSLExType.CONCAT));
							}
						}
						break;
					case '.'	:
						from = CharUtils.parseNameExtended(src,from+1,nameRange,AVAILABLE_IN_NAMES);
						if (nameRange[0] == nameRange[1]) {
							throw new SyntaxException(0,from,"Missing class name");
						}
						else {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.CLASS,new String(src,nameRange[0],nameRange[1]-nameRange[0]+1)));
							if (src[from] == '.' || src[from] == '#' || src[from] == ':' || src[from] == '[') {
								lexemas.add(new CSSLex(from,CSSLex.CSSLExType.CONCAT));
							}
						}
						break;
					case ':'	:
						from = CharUtils.parseNameExtended(src,from+1,nameRange,AVAILABLE_IN_NAMES);
						if (nameRange[0] == nameRange[1]) {
							throw new SyntaxException(0,from,"Missing pseudoclass name");
						}
						else {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.PSEUDOCLASS,new String(src,nameRange[0],nameRange[1]-nameRange[0]+1)));
							if (src[from] == '.' || src[from] == '#' || src[from] == ':' || src[from] == '[') {
								lexemas.add(new CSSLex(from,CSSLex.CSSLExType.CONCAT));
							}
						}
						from++;
						break;
					case '@'	:
						from = CharUtils.parseNameExtended(src,from+1,nameRange,AVAILABLE_IN_NAMES);
						if (nameRange[0] == nameRange[1]) {
							throw new SyntaxException(0,from,"Missing attribute name");
						}
						else {
							if (src[nameRange[1]] == '$') {	// Has special means in the CSS
								nameRange[1]--;
								from--;
							}
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.ATTRIBUTE,new String(src,nameRange[0],nameRange[1]-nameRange[0]+1)));
						}
						break;
					case ','	:
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.DIV));
						from++;
						break;
					case '['	:
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.OPENB));
						from++;
						break;
					case ']'	:
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.CLOSEB));
						from++;
						if (src[from] == '.' || src[from] == '#' || src[from] == ':' || src[from] == '[') {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.CONCAT));
						}
						break;
					case '"'	:
						final int 	start = from;
						
						sb.setLength(0);
						from = CharUtils.parseString(src,from+1,'\"',sb);
						if (from >= src.length || src[from] == '\0') {
							throw new SyntaxException(0,start,"Unpaired quotes");
						}
						else {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.STRING,sb.toString()));
						}
						break;
					case '>'	:
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.SEQUENT));
						from++;
						break;
					case '+'	:
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.PLUS));
						from++;
						break;
					case '('	:
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.OPEN));
						from++;
						break;
					case ')'	:
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.CLOSE));
						from++;
						if (src[from] == '.' || src[from] == '#' || src[from] == ':' || src[from] == '[') {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.CONCAT));
						}
						break;
					case '='	:
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.OPER,CSSLex.OPER_EQUALS));
						from++;
						break;
					case '~'	:
						if (src[from+1] != '=') {
							throw new SyntaxException(0,from,"Unknown lexema");
						}
						else {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.OPER,CSSLex.OPER_CONTAINS_VALUE));
							from += 2;
						}
						break;
					case '^'	:
						if (src[from+1] != '=') {
							throw new SyntaxException(0,from,"Unknown lexema");
						}
						else {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.OPER,CSSLex.OPER_STARTSWITH));
							from += 2;
						}
						break;
					case '|'	:
						if (src[from+1] != '=') {
							throw new SyntaxException(0,from,"Unknown lexema");
						}
						else {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.OPER,CSSLex.OPER_STARTS_OR_EQUALS));
							from += 2;
						}
						break;
					case '*'	:
						if (src[from+1] != '=') {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.ASTERISK));
							from++;
						}
						else {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.OPER,CSSLex.OPER_CONTAINS));
							from += 2;
						}
						break;
					case '$'	:
						if (src[from+1] != '=') {
							throw new SyntaxException(0,from,"Unknown lexema");
						}
						else {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.OPER,CSSLex.OPER_ENDSWITH));
							from += 2;
						}
						break;
					case '{'	:
						final Properties	props = new Properties();
						
						from = parseInnerCSS(src,from+1,nameRange,sb,props);
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.PROPS,props));
						break;
					case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						from = CharUtils.parseInt(src,from,nameRange,true);
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.NUMBER,nameRange[0]));
						break;
					default :
						if (Character.isJavaIdentifierStart(src[from])) {
							from = CharUtils.parseName(src,from,nameRange);
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.NAME,new String(src,nameRange[0],nameRange[1]-nameRange[0]+1)));
							if (src[from] == '.' || src[from] == '#' || src[from] == ':' || src[from] == '[') {
								lexemas.add(new CSSLex(from,CSSLex.CSSLExType.CONCAT));
							}
						}
						else {
							throw new SyntaxException(0,from,"Unknown lexema");
						}
				}
			}
			lexemas.add(new CSSLex(from,CSSLex.CSSLExType.EOF));
			
			final CSSLex[]			lex = lexemas.toArray(new CSSLex[lexemas.size()]);
			int						pos = 0;
			
			lexemas.clear();
			do {final SyntaxNode<CSSSyntaxNode,SyntaxNode<CSSSyntaxNode,?>>	node = new SyntaxNode<CSSSyntaxNode,SyntaxNode<CSSSyntaxNode,?>>(0,0,CSSSyntaxNode.RECORD,0L,null);
			
				pos = buildCSSTree(lex,pos,CSSSyntaxNode.RECORD, node);
				try{printCSSTree(node,new PrintWriter(System.err));
				} catch (IOException e) {
					e.printStackTrace();
				}
				result.put(convertCSSTree2XPath((SyntaxNode<CSSSyntaxNode, SyntaxNode<CSSSyntaxNode, ?>>) node.children[0]),(Properties)node.cargo);
			} while (pos < lex.length && lex[pos].type != CSSLex.CSSLExType.EOF);
			
			return from;
		}
	}	
	
	public static <T> Map<String,StylePropValue<T>> parseStyle(final String style) throws SyntaxException {
		if (style == null || style.isEmpty()) {
			throw new IllegalArgumentException("Style can't be null or empty");
		}
		else {
			final Properties			props = new Properties();
			final StringBuilder			sb = new StringBuilder();

			parseInnerCSS((style+"}").toCharArray(),0,new int[2],sb,props);
			
			final Map<String,StylePropValue<T>>	result = new HashMap<>();
			
			for (Map.Entry<Object,Object> item : props.entrySet()) {
				final String	key = ((String)item.getKey());
				final long 		id = STYLES.seekName(key);
				
				if (id >= 0) {
					result.put(key,parseStyleProperty(STYLES.getCargo(id),result,(String)item.getValue()));
				}
				else {
					result.put(key,new StylePropValue<T>(ContentType.asIs,null,(T)item.getValue()));
				}
			}
			
			return result;
		}
	}
	
	public static <T> StylePropValue<T> parseStyleProperty(final StylePropertiesSupported prop, final Map<String,StylePropValue<T>> props, final String content) throws SyntaxException {
		if (prop == null) {
			throw new NullPointerException("Property descriptor can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else if (content == null || content.isEmpty()) {
			throw new IllegalArgumentException("Content can't be null or empty");
		}
		else {
			return parseStyleProperty(prop,props,content,0,content.length());
		}
	}

	public static <T> StylePropValue<T> parseStyleProperty(final StylePropertiesSupported prop, final Map<String,StylePropValue<T>> props, final String content, final int from, final int to) throws SyntaxException {
		if (prop == null) {
			throw new NullPointerException("Property descriptor can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else if (content == null || content.isEmpty()) {
			throw new IllegalArgumentException("Content can't be null or empty");
		}
		else {
			return parseStyleProperty(prop,props,UnsafedUtils.getStringContent(content),from,to);
		}
	}

	public static <T> StylePropValue<T> parseStyleProperty(final StylePropertiesSupported prop, final Map<String,StylePropValue<T>> props, final char[] content, final int from, final int to) throws SyntaxException {
		if (prop == null) {
			throw new NullPointerException("Property descriptor can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content can't be null or empty array");
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length-1));
		}
		else if (to < 0 || to > content.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+(content.length));
		}
		else if (to < from) {
			throw new IllegalArgumentException("To position ["+to+"] is less than from position ["+from+"]");
		}
		else {
			int					end;
			long				id;
			StylePropValue<T>	result;
			
			switch (prop.getContentType()) {
				case asIs				:
					break;
				case colorOrKeyword		:
					if ((id = KEYWORDS.seekName(content,from,to)) >= 0) {
						if (id == INHERITED) {
							return parseStylePropertyInherited(prop,props,content,from,to);
						}
					}
				case color				:
					if ((id = KEYWORDS.seekName(content,from,to)) >= 0) {
						for (StylePropValue<?> item : KEYWORDS.getCargo(id)) {
							if (item.getProp() == prop) {
								return (StylePropValue<T>) item;
							}
						}
					}
					return new StylePropValue<T>(prop.getContentType(),prop,(T)asColor(content));
				case compoundChoise		:
//					for (String item : prop.getValues().getContent()) {
//						
//					}
					break;
				case compoundSequence	:
					break;
				case distanceOrKeyword	:
					if ((result = parseStylePropertyInheritance(prop,props,content,from,to)) !=null) {
						return result;
					}
				case distance			:
					return new StylePropValue<T>(prop.getContentType(),prop,(T)asDistance(new String(content)));
				case functionOrKeyword	:
					if ((id = KEYWORDS.seekName(content,from,to)) >= 0) {
						if (id == INHERITED) {
							return parseStylePropertyInherited(prop,props,content,from,to);
						}
						else {
							for (StylePropValue<?> item : KEYWORDS.getCargo(id)) {
								if (item.getProp() == prop) {
									return (StylePropValue<T>) item;
								}
							}
						}
					}
				case function			:
					break;
				case integerOrKeyword	:
					if ((id = KEYWORDS.seekName(content,from,to)) >= 0) {
						if (id == INHERITED) {
							return parseStylePropertyInherited(prop,props,content,from,to);
						}
						else {
							for (StylePropValue<?> item : KEYWORDS.getCargo(id)) {
								if (item.getProp() == prop) {
									return (StylePropValue<T>) item;
								}
							}
						}
					}
				case integer			:
					final int[]		intResult = new int[1];
					
					if ((end = UnsafedCharUtils.uncheckedParseInt(content,from,intResult,true)) != to) {
						throw new IllegalArgumentException();
					}
					else {
						return (StylePropValue<T>) new StylePropValue<Integer>(ContentType.integer,prop,intResult[0]);
					}
				case numberOrKeyword	:
					if ((id = KEYWORDS.seekName(content,from,to)) >= 0) {
						if (id == INHERITED) {
							return parseStylePropertyInherited(prop,props,content,from,to);
						}
						else {
							for (StylePropValue<?> item : KEYWORDS.getCargo(id)) {
								if (item.getProp() == prop) {
									return (StylePropValue<T>) item;
								}
							}
						}
					}
				case number				:
					final float[]		floatResult = new float[1];
					
					if ((end = UnsafedCharUtils.uncheckedParseFloat(content,from,floatResult,true)) != to) {
						throw new IllegalArgumentException();
					}
					else {
						return (StylePropValue<T>) new StylePropValue<Float>(ContentType.number,prop,floatResult[0]);
					}
				case stringOrKeyword	:
					if ((id = KEYWORDS.seekName(content,from,to)) >= 0) {
						if (id == INHERITED) {
							return parseStylePropertyInherited(prop,props,content,from,to);
						}
						else {
							for (StylePropValue<?> item : KEYWORDS.getCargo(id)) {
								if (item.getProp() == prop) {
									return (StylePropValue<T>) item;
								}
							}
						}
					}
				case string				:
					return (StylePropValue<T>) new StylePropValue<String>(ContentType.string,prop,new String(content,from,to));
				case subStyle			:
					break;
				case timeOrKeyword		:
					if ((id = KEYWORDS.seekName(content,from,to)) >= 0) {
						if (id == INHERITED) {
							return parseStylePropertyInherited(prop,props,content,from,to);
						}
						else {
							for (StylePropValue<?> item : KEYWORDS.getCargo(id)) {
								if (item.getProp() == prop) {
									return (StylePropValue<T>) item;
								}
							}
						}
					}
				case time				:
					break;
				case urlOrKeyword		:
					if ((id = KEYWORDS.seekName(content,from,to)) >= 0) {
						if (id == INHERITED) {
							return parseStylePropertyInherited(prop,props,content,from,to);
						}
						else {
							for (StylePropValue<?> item : KEYWORDS.getCargo(id)) {
								if (item.getProp() == prop) {
									return (StylePropValue<T>) item;
								}
							}
						}
					}
				case url				:
					break;
				case value				:
					if ((id = KEYWORDS.seekName(content,from,to)) >= 0) {
						if (id == INHERITED) {
							return parseStylePropertyInherited(prop,props,content,from,to);
						}
						else {
							for (StylePropValue<?> item : KEYWORDS.getCargo(id)) {
								if (item.getProp() == prop) {
									return (StylePropValue<T>) item;
								}
							}
						}
					}
					break;
				default:
					break;			
			}
			return null;
		}
	}
	
	public static <T> StylePropValue<T> parseStylePropertyInheritance(final StylePropertiesSupported prop, final Map<String,StylePropValue<T>> props, final char[] content, final int from, final int to) throws SyntaxException {
		long	id;
		
		if ((id = KEYWORDS.seekName(content,from,to)) >= 0) {
			if (id == INHERITED) {
				return parseStylePropertyInherited(prop,props,content,from,to);
			}
			else {
				for (StylePropValue<?> item : KEYWORDS.getCargo(id)) {
					if (item.getProp() == prop) {
						return (StylePropValue<T>) item;
					}
				}
			}
		}
		return null;
	}
	
	private static <T> StylePropValue<T> parseStylePropertyInherited(final StylePropertiesSupported prop, final Map<String,StylePropValue<T>> props, final char[] content, final int from, final int to) throws SyntaxException {
		for (StylePropertiesSupported parent : StylePropertiesSupported.values()) {
			if (parent.getContentType() == ContentType.compoundChoise) {
//				for (String item : parent.getValues().getContent()) {
//					if (item.equals(prop.name()) || item.equals(prop.getExternalName())) {
//						final StylePropValue<T> result = parseStyleProperty(parent, props, content, from, to);
//					
//						if (result == null) {
//							return null;
//						}
//						else {
//							return null;
//						}
//					}
//				}
			}
			else if (parent.getContentType() == ContentType.compoundSequence) {
//				for (String item : parent.getValues().getContent()) {
//					if (item.equals(prop.name()) || item.equals(prop.getExternalName())) {
//						final StylePropValue<T> result = parseStyleProperty(parent, props, content, from, to);
//						
//						if (result == null) {
//							return null;
//						}
//						else {
//							return null;
//						}
//					}
//				}
			}
		}
		return null;
	}

	public interface AttributeParser {
		String getAttributeName();
		void process(final Element element);
	}
	
	public static AttributeParser buildAttributeParser(final Reader parserDescription) throws IOException, SyntaxException {
		if (parserDescription == null) {
			throw new NullPointerException("Parser descripion stream can't be null");
		}
		else {
			try(final JsonStaxParserInterface		parser = new JsonStaxParser(parserDescription)) {
				final Map<String,AttributeParser>	collection = new HashMap<>();

				for (JsonStaxParserLexType item : parser) {
					while (item == JsonStaxParserLexType.START_ARRAY) {
						try(final JsonStaxParserInterface	nested = parser.nested()) {
							buildTopAttributeParsers(parser.nested(),collection);
						}
					}
				}
				return null;
			}
		}
	}

	private static ContinueMode walkDownXMLInternal(final Element node, final long nodeTypes, final XMLWalkerCallback callback) throws ContentException {
		ContinueMode	before = null, after = ContinueMode.CONTINUE;
		
		if (node != null && (nodeTypes & (1 << node.getNodeType())) != 0) {
			switch (before = callback.process(NodeEnterMode.ENTER, node)) {
				case CONTINUE		:
					final NodeList	list = node.getChildNodes();
					
					for (int index = 0, maxIndex = list.getLength(); index < maxIndex; index++) {
						final Node	item = list.item(index);
						
						if (item instanceof Element) {
							if ((after = walkDownXMLInternal((Element)item,nodeTypes,callback)) != ContinueMode.CONTINUE) {
								break;
							}
						}
					}
					after = Utils.resolveContinueMode(after,callback.process(NodeEnterMode.EXIT, node));
					break;
				case SKIP_CHILDREN : case STOP :
					after = callback.process(NodeEnterMode.EXIT, node);
					break;
				default:
					throw new IllegalStateException("Unwaited continue mode ["+before+"] for walking down");
			}
			return Utils.resolveContinueMode(before, after);
		}
		else {
			return ContinueMode.CONTINUE;
		}
	}

	static int parseInnerCSS(final char[] src, final int start, final int[] nameRange, final StringBuilder sb, final Properties props) throws SyntaxException {
		int	from = start-1, begin = start;

		try{do {from = CharUtils.parseNameExtended(src, CharUtils.skipBlank(src,from+1,false), nameRange, AVAILABLE_IN_NAMES);
				if (nameRange[0] == nameRange[1]) {
					throw new SyntaxException(0,from,"Property name is missing"); 
				}
				final String	name = new String(src,nameRange[0],nameRange[1]-nameRange[0]+1);
				
				from = CharUtils.skipBlank(src,from,false);
				if (src[from] != ':') {
					throw new SyntaxException(0,from,"Colon (:) is missing"); 
				}
				else {
					from = CharUtils.skipBlank(src,from+1,false);
					if (src[from] == '\"') {
						sb.setLength(0);
						begin = from;
						from = CharUtils.parseString(src,from+1,'\"',sb);
						if (from >= src.length) {
							throw new SyntaxException(0,begin,"Unclosed double quote"); 
						}
						else {
							props.setProperty(name,sb.toString());
							from = CharUtils.skipBlank(src,from+1,false);
						}
					}
					else {
						begin = from;
						while (src[from] != '\0' && src[from] != ';' && src[from] != '}') {
							from++;
						}
						if (src[from] == '\0') {
							throw new SyntaxException(0,begin,"Unclosed '}'"); 
						}
						else {
							while (src[from-1] <= ' ') {	// trunk tailed blanks
								from--;
							}
							props.setProperty(name,new String(src,begin,from-begin));
						}
					}
					if (src[from = CharUtils.skipBlank(src,from,false)] == ';') {
						from = CharUtils.skipBlank(src,from+1,false);
					}
				}
			}  while (src[from] != '\0' && src[from] != '}');
		} catch (IllegalArgumentException exc) {
			throw new SyntaxException(0,begin,exc.getLocalizedMessage(),exc); 
		}
		
		if (src[from] != '}') {
			throw new SyntaxException(0,begin,"Unclosed '}'"); 
		}
		else {
			return from+1;
		}
	}
	
	private static void buildTopAttributeParsers(final JsonStaxParserInterface parser, final Map<String, AttributeParser> collection) throws SyntaxException, IOException {
		String			name = null;
		AttributeParser	ap = null;
		
		for (JsonStaxParserLexType item : parser) {
			if (item == JsonStaxParserLexType.NAME) {
				name = parser.name();
			}
			else if (item == JsonStaxParserLexType.START_OBJECT) {
				try(final JsonStaxParserInterface	nested = parser.nested()) {
					ap = buildTopAttributeParser(parser, collection);
				}
			}
			else {
				throw new SyntaxException(parser.row(),parser.col(),"Only name of starting object can be here");
			}
		}
		if (name == null) {
			throw new SyntaxException(parser.row(),parser.col(),"Attribute name is missing");
		}
		else if (ap == null) {
			throw new SyntaxException(parser.row(),parser.col(),"Attribute description is missing");
		}
		else {
			collection.put(name,ap);
		}
	}
	
	
	private static AttributeParser buildTopAttributeParser(final JsonStaxParserInterface parser, final Map<String, AttributeParser> collection) throws SyntaxException, IOException {
		// TODO Auto-generated method stub
		boolean		hasFixed = false, inheritanceAvailable= false, urlAvailable = false;
		String		valType = null, format = null, parserClass = null, parserMethod = null;
		
		for (JsonStaxParserLexType item : parser) {
			if (item == JsonStaxParserLexType.NAME) {
				final String	name = parser.name();
				
				if (parser.next() != JsonStaxParserLexType.NAME) {
					throw new SyntaxException(parser.row(),parser.col(),"Missing (:)"); 
				}
				switch (name) {
					case PROP_ATTR_NAME_HAS_FIXED				:
						if (parser.next() != JsonStaxParserLexType.BOOLEAN_VALUE) {
							throw new SyntaxException(parser.row(),parser.col(),"Boolean value awaited"); 
						}
						else {
							hasFixed = parser.booleanValue();
						}
						break;
					case PROP_ATTR_NAME_VALUE_TYPE				:
						if (parser.next() != JsonStaxParserLexType.STRING_VALUE) {
							throw new SyntaxException(parser.row(),parser.col(),"String value awaited"); 
						}
						else {
							valType = parser.stringValue();
						}
						break;
					case PROP_ATTR_NAME_INHERITANCE_AVAILABLE	:
						if (parser.next() != JsonStaxParserLexType.BOOLEAN_VALUE) {
							throw new SyntaxException(parser.row(),parser.col(),"Boolean value awaited"); 
						}
						else {
							inheritanceAvailable = parser.booleanValue();
						}
						break;
					case PROP_ATTR_NAME_URL_AVAILABLE			:
						if (parser.next() != JsonStaxParserLexType.BOOLEAN_VALUE) {
							throw new SyntaxException(parser.row(),parser.col(),"Boolean value awaited"); 
						}
						else {
							urlAvailable = parser.booleanValue();
						}
						break;
					case PROP_ATTR_NAME_CHILDREN				:
						if (parser.next() != JsonStaxParserLexType.START_ARRAY) {
							throw new SyntaxException(parser.row(),parser.col(),"Array awaited"); 
						}
						else {
//							urlAvailable = parser.booleanValue();
						}
						break;
					case PROP_ATTR_NAME_FORMAT					:
						if (parser.next() != JsonStaxParserLexType.STRING_VALUE) {
							throw new SyntaxException(parser.row(),parser.col(),"String value awaited"); 
						}
						else {
							format = parser.stringValue();
						}
						break;
					case PROP_ATTR_NAME_PARSER_CLASS			:
						if (parser.next() != JsonStaxParserLexType.STRING_VALUE) {
							throw new SyntaxException(parser.row(),parser.col(),"String value awaited"); 
						}
						else {
							parserClass = parser.stringValue();
						}
						break;
					case PROP_ATTR_NAME_PARSER_METHOD			:
						if (parser.next() != JsonStaxParserLexType.STRING_VALUE) {
							throw new SyntaxException(parser.row(),parser.col(),"String value awaited"); 
						}
						else {
							parserMethod = parser.stringValue();
						}
						break;
					default : 
						throw new SyntaxException(parser.row(),parser.col(),"Unsupported name ["+name+"] in the descriptor"); 
				}
			}
		}
		return null;
	}

	private static int buildCSSTree(final CSSLex[] src, final int start, final CSSSyntaxNode level, final SyntaxNode<CSSSyntaxNode,SyntaxNode<CSSSyntaxNode,?>> node) throws SyntaxException {
		int	from = start;
		
		switch (level) {
			case RECORD			:
				from = buildCSSTree(src,from,CSSSyntaxNode.SELECTORS,node);
				if (src[from].type == CSSLex.CSSLExType.PROPS) {
					final SyntaxNode<CSSSyntaxNode,SyntaxNode<CSSSyntaxNode,?>> subnode = new SyntaxNode<CSSSyntaxNode,SyntaxNode<CSSSyntaxNode,?>>(node); 
					
					node.type = CSSSyntaxNode.RECORD;
					node.value = 0;
					node.cargo = src[from].props;
					node.children = new SyntaxNode[]{subnode};
					node.parent = null;
					from++;
				}
				else {
					throw new SyntaxException(0,src[from].pos,"Properties clause is missing");
				}
				break;
			case SELECTORS		:
				from = buildCSSTree(src,from,CSSSyntaxNode.SELECTOR_GROUP,node);
				if (src[from].type == CSSLex.CSSLExType.DIV) {
					final List<SyntaxNode>	orList = new ArrayList<>();
					
					orList.add(new SyntaxNode(node));					
					do {from = buildCSSTree(src,from,CSSSyntaxNode.SELECTOR_GROUP,node);
						orList.add(new SyntaxNode(node));
					} while (src[from].type == CSSLex.CSSLExType.DIV);
					node.type = CSSSyntaxNode.SELECTORS;
					node.children = orList.toArray(new SyntaxNode[orList.size()]);
				}
				break;
			case SELECTOR_GROUP	:
				from = buildCSSTree(src,from,CSSSyntaxNode.SUBTREE_SEL,node);
				if (src[from].type == CSSLex.CSSLExType.PLUS) {
					final List<SyntaxNode>	orList = new ArrayList<>();
					
					orList.add(new SyntaxNode(node));					
					do {from = buildCSSTree(src,from,CSSSyntaxNode.SUBTREE_SEL,node);
						orList.add(new SyntaxNode(node));
					} while (src[from].type == CSSLex.CSSLExType.PLUS);
					node.type = CSSSyntaxNode.SELECTOR_GROUP;
					node.children = orList.toArray(new SyntaxNode[orList.size()]);
				}
				break;
			case SUBTREE_SEL	:
				from = buildCSSTree(src,from,CSSSyntaxNode.STANDALONE_SEL,node);
				if (src[from].type == CSSLex.CSSLExType.SEQUENT) {
					final List<SyntaxNode>	nestedList = new ArrayList<>();
					
					nestedList.add(new SyntaxNode(node));					
					do {from = buildCSSTree(src,from+1,CSSSyntaxNode.STANDALONE_SEL,node);
						nestedList.add(new SyntaxNode(node));
					} while (src[from].type == CSSLex.CSSLExType.SEQUENT);
					node.type = CSSSyntaxNode.SUBTREE_SEL;
					node.children = nestedList.toArray(new SyntaxNode[nestedList.size()]);
				}
				break;
			case STANDALONE_SEL	:
				from = buildCSSTree(src,from,CSSSyntaxNode.SEL_ITEM,node);
				if (src[from].type == CSSLex.CSSLExType.CONCAT) {
					final List<SyntaxNode>	andList = new ArrayList<>();
					
					andList.add(new SyntaxNode(node));					
					do {from = buildCSSTree(src,from+1,CSSSyntaxNode.SEL_ITEM,node);
						andList.add(new SyntaxNode(node));
					} while (src[from].type == CSSLex.CSSLExType.CONCAT);
					node.type = CSSSyntaxNode.STANDALONE_SEL;
					node.children = andList.toArray(new SyntaxNode[andList.size()]);
				}
				break;
			case SEL_ITEM		:
				node.col = src[from].pos;
				
				switch (src[from].type) {
					case ASTERISK	:
						node.type = CSSSyntaxNode.SEL_ITEM;
						node.value = CSSLex.NODE_ASTERISK;
						from++;
						break;
					case CLASS		:
						node.type = CSSSyntaxNode.SEL_ITEM;
						node.value = CSSLex.NODE_CLASS;
						node.cargo = src[from].strValue;
						from++;
						break;
					case ID			:
						node.type = CSSSyntaxNode.SEL_ITEM;
						node.value = CSSLex.NODE_ID;
						node.cargo = src[from].strValue;
						from++;
						break;
					case NAME		:
						node.type = CSSSyntaxNode.SEL_ITEM;
						node.value = CSSLex.NODE_TAG;
						node.cargo = src[from].strValue;
						from++;
						break;
					case PSEUDOCLASS:
						node.type = CSSSyntaxNode.SEL_ITEM;
						node.value = CSSLex.NODE_PSEUDOCLASS;
						if (src[from+1].type == CSSLex.CSSLExType.OPEN) {
							if (src[from+2].type == CSSLex.CSSLExType.NUMBER && src[from+3].type == CSSLex.CSSLExType.NAME && src[from+4].type == CSSLex.CSSLExType.PLUS && src[from+5].type == CSSLex.CSSLExType.NUMBER && src[from+6].type == CSSLex.CSSLExType.CLOSE) {
								node.cargo = new Object[]{src[from].strValue,src[from+2].intValue,src[from+5].intValue};
								from += 7;
							}
							else if (src[from+2].type == CSSLex.CSSLExType.NUMBER && src[from+3].type == CSSLex.CSSLExType.NAME && src[from+4].type == CSSLex.CSSLExType.CLOSE) {
								node.cargo = new Object[]{src[from].strValue,src[from+2].intValue,0};
								from += 5;
							}
							else if (src[from+2].type == CSSLex.CSSLExType.NUMBER && src[from+3].type == CSSLex.CSSLExType.CLOSE) {
								node.cargo = new Object[]{src[from].strValue,0,src[from+2].intValue};
								from += 4;
							}
							else {
								throw new SyntaxException(0,src[from].pos,"Illegal pseudoclass parameters"); 
							}
						}
						else {
							node.cargo = new Object[]{src[from].strValue};
							from++;
						}
						break;
					case OPENB		:
						if (src[from+1].type == CSSLex.CSSLExType.ATTRIBUTE) {
							if (src[from+2].type == CSSLex.CSSLExType.CLOSEB) {
								node.type = CSSSyntaxNode.SEL_ITEM;
								node.value = CSSLex.NODE_ATTR; 
								node.cargo = new Object[]{src[from+1].strValue};
								from+=3;
							}
							else if (src[from+2].type == CSSLex.CSSLExType.OPER && src[from+3].type == CSSLex.CSSLExType.STRING && src[from+4].type == CSSLex.CSSLExType.CLOSEB) {
								node.type = CSSSyntaxNode.SEL_ITEM;
								node.value = CSSLex.NODE_ATTR; 
								node.cargo = new Object[]{src[from+1].strValue,src[from+2].intValue,src[from+3].strValue};
								from+=5;
							}
							else {
								throw new SyntaxException(0,src[from].pos,"Illegal attribute check"); 
							}
						}
						else {
							throw new SyntaxException(0,src[from].pos,"Illegal attribute check"); 
						}
						break;
					default:
						break;
				}
				break;
			default				:
				break;
		}
		return from;
	}

	private static void printCSSTree(final SyntaxNode<CSSSyntaxNode,SyntaxNode<CSSSyntaxNode,?>> node, final Writer writer) throws IOException {
		printCSSTree(node,writer,0);
	}

	private static void printCSSTree(final SyntaxNode<CSSSyntaxNode,?> node, final Writer writer, final int tabs) throws IOException {
		if (node != null) {
			for (int index = 0; index < tabs; index++) {
				writer.write('\t');
			}
			writer.write(node.type.name());
			writer.write(" : ");
			switch ((int)node.value) {
				case CSSLex.NODE_ASTERISK		: writer.write("*");
				case CSSLex.NODE_TAG			: writer.write("tag");
				case CSSLex.NODE_ID				: writer.write("id");
				case CSSLex.NODE_CLASS			: writer.write("class");
				case CSSLex.NODE_ATTR			: writer.write("attr");
				case CSSLex.NODE_PSEUDOCLASS	: writer.write("pseudoclass");
				default : writer.write(String.valueOf(node.value));
			}
			writer.write(" -> ");
			writer.write(node.cargo != null ? node.cargo.toString() : "null");
			writer.write("\n");
			if (node.children != null) {
				for (SyntaxNode<CSSSyntaxNode, ?> item : node.children) {
					printCSSTree(item,writer,tabs+1);
				}
			}
		}
	}
	
	private static String convertCSSTree2XPath(final SyntaxNode<CSSSyntaxNode,SyntaxNode<CSSSyntaxNode,?>> node) throws SyntaxException {
		switch (node.type) {
			case RECORD			:
				return convertCSSTree2XPath((SyntaxNode<CSSSyntaxNode, SyntaxNode<CSSSyntaxNode, ?>>)node.children[0]); 
			case SELECTORS		:
				final StringBuilder	selSb = new StringBuilder();
				
				for (SyntaxNode<CSSSyntaxNode, ?> item : node.children) {
					selSb.append('|').append(convertCSSTree2XPath((SyntaxNode<CSSSyntaxNode, SyntaxNode<CSSSyntaxNode, ?>>) item));
				}
				return selSb.substring(1);
			case SELECTOR_GROUP	:
				break;
			case SUBTREE_SEL	:
				final StringBuilder	depthSb = new StringBuilder();
				int		depth = 0;
				
				for (SyntaxNode<CSSSyntaxNode, ?> item : node.children) {
					if (depth > 0) {
						depthSb.append(" and ./child[");
					}
					depthSb.append('('+convertCSSTree2XPath((SyntaxNode<CSSSyntaxNode, SyntaxNode<CSSSyntaxNode, ?>>) item)+')');
					depth++;
				}
				while (--depth > 0) {
					depthSb.append(']');
				}
				return depthSb.toString(); 
			case STANDALONE_SEL	:
				final StringBuilder	andSb = new StringBuilder();
				
				for (SyntaxNode<CSSSyntaxNode, ?> item : node.children) {
					andSb.append(" and ").append(convertCSSTree2XPath((SyntaxNode<CSSSyntaxNode, SyntaxNode<CSSSyntaxNode, ?>>) item));
				}
				return andSb.substring(5); 
			case SEL_ITEM		:
				switch ((int)node.value) {
					case CSSLex.NODE_ASTERISK	:
						return "*";
					case CSSLex.NODE_TAG		:
						return "node-name(.)=\'"+node.cargo.toString()+"\'";
					case CSSLex.NODE_ID			:
						return "@id=\'"+node.cargo.toString()+"\'";
					case CSSLex.NODE_CLASS		:
						return "contains(concat(' ',normalize-space(@class),' '),\' "+node.cargo.toString()+" \')";
					case CSSLex.NODE_ATTR		:
						if (((Object[])node.cargo).length == 1) {
							return "boolean(@"+((Object[])node.cargo)[0].toString()+")";
						}
						else {
							switch ((Integer)((Object[])node.cargo)[1]) {
								case CSSLex.OPER_ENDSWITH			:
									return "ends-with(@"+((Object[])node.cargo)[0].toString()+",\'"+((Object[])node.cargo)[2].toString()+"\')";
								case CSSLex.OPER_CONTAINS			:
									return "contains(@"+((Object[])node.cargo)[0].toString()+",\'"+((Object[])node.cargo)[2].toString()+"\')";
								case CSSLex.OPER_CONTAINS_VALUE		:
									return "contains(concat(' ',normalize-space(@"+((Object[])node.cargo)[0].toString()+"),' '),\' "+((Object[])node.cargo)[2].toString()+" \')";
								case CSSLex.OPER_STARTSWITH			:
									return "starts-with(@"+((Object[])node.cargo)[0].toString()+",\'"+((Object[])node.cargo)[2].toString()+"\')";
								case CSSLex.OPER_STARTS_OR_EQUALS	:
									return "(starts-with(@"+((Object[])node.cargo)[0].toString()+",\'"+((Object[])node.cargo)[2].toString()+"\') or @"+((Object[])node.cargo)[0].toString()+"=\'"+((Object[])node.cargo)[2].toString()+"\')";
								case CSSLex.OPER_EQUALS				:
									return "@"+((Object[])node.cargo)[0].toString()+"=\'"+((Object[])node.cargo)[2].toString()+"\'";
								default :
									throw new SyntaxException(node.row,node.col,"Unsupported attribute operation ["+((Object[])node.cargo)[1]+"] in the syntax tree");
							}
						}
					case CSSLex.NODE_PSEUDOCLASS:
						break;
					default :
						throw new SyntaxException(node.row,node.col,"Unsupported node ["+node.value+"] in the syntax tree");
				}
				break;
			default:
				break;
		}
		return null;
	}
	
	private enum CSSSyntaxNode {
		RECORD, SELECTORS, SELECTOR_GROUP, SUBTREE_SEL, STANDALONE_SEL, SEL_ITEM 
	}

	public static boolean isValidColor(final String color) throws SyntaxException {
		if (color == null || color.isEmpty()) {
			throw new IllegalArgumentException("Color content can't be null or empty");
		}
		else {
			return isValidColor(UnsafedUtils.getStringContent(color));
		}		
	}
	
	public static Color asColor(final String color) throws SyntaxException {
		if (color == null || color.isEmpty()) {
			throw new IllegalArgumentException("Color content can't be null or empty");
		}
		else {
			return asColor(UnsafedUtils.getStringContent(color));
		}		
	}
	
	public static boolean isValidColor(final char[] content) throws SyntaxException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Color content can't be null or empty array");
		}
		else {
			final Object[]	values = new Object[4];
			int				from = 0;
			
			if (content[0] == '#') {
				return CharUtils.tryExtract(content,from,COLOR_HEX_TEMPLATE) > 0;
			}
			if (content.length > 4) {
				if (UnsafedCharUtils.uncheckedCompare(content,0,COLOR_RGBA,0,COLOR_RGBA.length)) {
					return CharUtils.tryExtract(content,from,COLOR_RGBA_TEMPLATE) > 0;
				}
				else if (UnsafedCharUtils.uncheckedCompare(content,0,COLOR_HSLA,0,COLOR_HSLA.length)) {
					return CharUtils.tryExtract(content,from,COLOR_HSLA_TEMPLATE) > 0;
				}
			}
			if (content.length > 3) {
				if (UnsafedCharUtils.uncheckedCompare(content,0,COLOR_RGB,0,COLOR_RGB.length)) {
					return CharUtils.tryExtract(content,from,COLOR_RGB_TEMPLATE) > 0;
				}
				else if (UnsafedCharUtils.uncheckedCompare(content,0,COLOR_HSL,0,COLOR_HSL.length)) {
					return CharUtils.tryExtract(content,from,COLOR_HSL_TEMPLATE) > 0;
				}
				else {
					return PureLibSettings.colorByName(new String(content),null) != null; 
				}
			}
			return PureLibSettings.colorByName(new String(content),null) != null; 
		}
	}

	public static Color asColor(final char[] content) throws SyntaxException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Color content can't be null or empty array");
		}
		else {
			final Object[]	values = new Object[4];
			int				from = 0;
			
			if (content[0] == '#') {
				from = CharUtils.extract(content,from,values,COLOR_HEX_TEMPLATE);
				return new Color((Integer)values[0]);
			}
			if (content.length > 4) {
				if (UnsafedCharUtils.uncheckedCompare(content,0,COLOR_RGBA,0,COLOR_RGBA.length)) {
					from = CharUtils.extract(content,from,values,COLOR_RGBA_TEMPLATE);
					return new Color((Integer)values[0],(Integer)values[1],(Integer)values[2],(Integer)values[3]);
				}
				else if (UnsafedCharUtils.uncheckedCompare(content,0,COLOR_HSLA,0,COLOR_HSLA.length)) {
					from = CharUtils.extract(content,from,values,COLOR_HSLA_TEMPLATE);
					final Color 	temp = Color.getHSBColor((Integer)values[0]/256.0f,0.01f*(Float)values[1],0.01f*(Float)values[2]); 
					return new Color(temp.getRed(),temp.getGreen(),temp.getBlue(),Math.min((int)(2.56f*(Float)values[3]),255));
				}
			}
			if (content.length > 3) {
				if (UnsafedCharUtils.uncheckedCompare(content,0,COLOR_RGB,0,COLOR_RGB.length)) {
					from = CharUtils.extract(content,from,values,COLOR_RGB_TEMPLATE);
					return new Color((Integer)values[0],(Integer)values[1],(Integer)values[2]);
				}
				else if (UnsafedCharUtils.uncheckedCompare(content,0,COLOR_HSL,0,COLOR_HSL.length)) {
					from = CharUtils.extract(content,from,values,COLOR_HSL_TEMPLATE);
					return Color.getHSBColor((Integer)values[0]/256.0f,0.01f*(Float)values[1],0.01f*(Float)values[2]);
				}
				else {
					final Color		toRet = PureLibSettings.colorByName(new String(content),null); 
					
					if (toRet != null) {
						return toRet;
					}
					else {
						throw new SyntaxException(0,0,"Color name ["+new String(content)+"] is unknown"); 
					}
				}
			}
			final Color	toRet = PureLibSettings.colorByName(new String(content),null); 
			
			if (toRet != null) {
				return toRet;
			}
			else {
				throw new SyntaxException(0,0,"Color name ["+new String(content)+"] is unknown"); 
			}
		}
	}
	
	public static class Distance {
		private static final int					MAX_CACHEABLE = 128;
		private static final ArgumentType[]			LEXEMAS = {ArgumentType.ordinalInt,ArgumentType.name};
		private static final Map<Units,Distance[]> 	microCache = new EnumMap<>(Units.class);
		private static final LightWeightRWLockerWrapper	locker = new LightWeightRWLockerWrapper();
		
		public enum Units {
			cm, mm, in, px, pt, pc,							// Absolute			
			em, ex, ch, rem, vw, vh, vmin, vmax, percent	// Relative
		}
		
		private final int 	value;
		private final Units	unit;
		
		public Distance(final int value, final Units unit) {
			if (value < 0) {
				throw new IllegalArgumentException("Value ["+value+"] must not be negative");
			}
			else if (unit == null) {
				throw new NullPointerException("Unit type can't be null");
			}
			else {
				this.value = value;
				this.unit = unit;
			}
		}

		public int getValue() {
			return value;
		}

		public Units getUnit() {
			return unit;
		}

		public static boolean idValidDistance(final String value) {
			if (value == null || value.isEmpty()) {
				throw new IllegalArgumentException("Value can't be null or empty");
			}
			else {
				try {
					return CharUtils.tryExtract(UnsafedUtils.getStringContent(value),0,(Object[])LEXEMAS) > 0;
				} catch (SyntaxException e) {
					return false;
				}
			}
		}
		
		public static Distance valueOf(final String value) {
			if (value == null || value.isEmpty()) {
				throw new IllegalArgumentException("Value can't ne null or empty");
			}
			else {
				return valueOf(value.toCharArray());
			}
		}

		public static boolean idValidDistance(final char[] value) {
			if (value == null || value.length == 0) {
				throw new IllegalArgumentException("Value can't be null or empty array");
			}
			else {
				try {
					return CharUtils.tryExtract(value,0,(Object[])LEXEMAS) > 0;
				} catch (SyntaxException e) {
					return false;
				}
			}
		}
		
		public static Distance valueOf(final char[] content) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content can't ne null or empty array");
			}
			else {
				final Object[]	result = new Object[2];
				
				try{CharUtils.extract(content,0,result,(Object[])LEXEMAS);
				} catch (SyntaxException e) {
					throw new IllegalArgumentException("String ["+new String()+"]: error at index ["+e.getCol()+"] ("+e.getLocalizedMessage()+")");
				}
				return valueOf(((Integer)result[0]).intValue(),Units.valueOf(result[1].toString()));
			}
		}
		
		public static Distance valueOf(final int value, final Units unit) {
			if (value < 0) {
				throw new IllegalArgumentException("Value ["+value+"] can't ne negative");
			}
			else if (unit == null) {
				throw new NullPointerException("Unit value can't be null");
			}
			else if (value >= MAX_CACHEABLE) {
				return new Distance(value, unit);
			}
			else {
				try(final Locker	lock = locker.lock(true)) {
					Distance[]		list = microCache.get(unit);
					
					if (list != null && list[value] != null) {
						return list[value];
					}
				}
				
				try(final Locker	lock = locker.lock(false)) {
					Distance[]		list = microCache.get(unit);
					
					if (list == null) {
						microCache.put(unit,list = new Distance[MAX_CACHEABLE]);
					}
					if (list[value] == null) {
						list[value] = new Distance(value, unit);
					}
					return list[value];
				}
			}
		}
		
		@Override
		public String toString() {
			return ""+value+unit;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Distance other = (Distance) obj;
			if (unit != other.unit) return false;
			if (value != other.value) return false;
			return true;
		}
	}

	public static boolean isValidDistance(final String distance) throws SyntaxException {
		if (distance == null || distance.isEmpty()) {
			throw new IllegalArgumentException("Distance string can't be null or empty");
		}
		else {
			return Distance.idValidDistance(distance);
		}
	}	
	
	public static Distance asDistance(final String distance) throws SyntaxException {
		if (distance == null || distance.isEmpty()) {
			throw new IllegalArgumentException("Distance string can't be null or empty");
		}
		else {
			try{return Distance.valueOf(distance);
			} catch (NumberFormatException exc) {
				throw new SyntaxException(0,0,"Distance ["+distance+"] has invalid syntax");
			}
		}
	}	

	public static boolean isValidDistance(final char[] distance) throws SyntaxException {
		if (distance == null || distance.length == 0) {
			throw new IllegalArgumentException("Distance can't be null or empty array");
		}
		else {
			return Distance.idValidDistance(distance);
		}
	}	
	
	public static Distance asDistance(final char[] distance) throws SyntaxException {
		if (distance == null || distance.length == 0) {
			throw new IllegalArgumentException("Distance string can't be null or empty");
		}
		else {
			try{return Distance.valueOf(distance);
			} catch (NumberFormatException exc) {
				throw new SyntaxException(0,0,"Distance ["+new String(distance)+"] has invalid syntax");
			}
		}
	}	
	
	public static class Angle {
		private static final int					MAX_CACHEABLE = 128;
		private static final ArgumentType[]			LEXEMAS = {ArgumentType.ordinalFloat,ArgumentType.name};
		private static final float[][]				KOEFFS = new float[][] {
														//       deg 						grad 						rad 						turn
														/*deg*/ {1.0f, 						90f/100f, 					(float)(Math.PI/180.0f), 	90.0f/360.0f},
														/*grad*/{100f/90f, 					1.0f, 						(float)(Math.PI/200.0f),	100.0f/360.0f},
														/*rad*/ {(float)(180.0f/Math.PI), 	(float)(200.0f/Math.PI), 	1.0f, 						(float)(0.5f/Math.PI)},
														/*turn*/{360.0f/90.0f,				360.0f/100.0f,				(float)(Math.PI/0.5f), 		1.0f},
													};				
		private static final Map<Units,Angle[]> 	microCache = new EnumMap<>(Units.class);
		private static final LightWeightRWLockerWrapper	locker = new LightWeightRWLockerWrapper();
		
		public enum Units {
			deg, grad, rad, turn					// Absolute			
		}
		
		private final float	value;
		private final Units	unit;
		
		public Angle(final float value, final Units unit) {
			if (unit == null) {
				throw new NullPointerException("Unit type can't be null");
			}
			else {
				this.value = value;
				this.unit = unit;
			}
		}

		public float getValue() {
			return value;
		}

		public float getValueAs(final Units unit) {
			if (unit == null) {
				throw new NullPointerException("Unit type can't be null");
			}
			else if (this.unit == unit) {
				return getValue();
			}
			else {
				return KOEFFS[this.unit.ordinal()][unit.ordinal()]*getValue();
			}
		}
		
		public Units getUnit() {
			return unit;
		}

		public static Angle valueOf(final String value) {
			if (value == null || value.isEmpty()) {
				throw new IllegalArgumentException("Value ["+value+"] can't ne null or empty");
			}
			else {
				final char[] 	content = UnsafedUtils.getStringContent(value);
				final Object[]	result = new Object[2];
				
				try{CharUtils.extract(content,0,result,(Object[])LEXEMAS);
				} catch (SyntaxException e) {
					throw new IllegalArgumentException("String ["+value+"]: error at index ["+e.getCol()+"] ("+e.getLocalizedMessage()+")");
				}
				return valueOf(((Float)result[0]).intValue(),Units.valueOf(result[1].toString()));
			}
		}

		public static Angle valueOf(final float value, final Units unit) {
			if (value < 0) {
				throw new IllegalArgumentException("Value ["+value+"] can't ne negative");
			}
			else if (unit == null) {
				throw new NullPointerException("Unit value can't be null");
			}
			else if (value < 0 || value >= MAX_CACHEABLE || value != (float)((int)value)) {
				return new Angle(value, unit);
			}
			else {
				final int	intValue = (int)value;
				
				try(final Locker	lock = locker.lock(true)) {
					Angle[]			list = microCache.get(unit);
					
					if (list != null && list[intValue] != null) {
						return list[intValue];
					}
				}
				
				try(final Locker	lock = locker.lock(false)) {
					Angle[]			list = microCache.get(unit);
					
					if (list == null) {
						microCache.put(unit,list = new Angle[MAX_CACHEABLE]);
					}
					if (list[intValue] == null) {
						list[intValue] = new Angle(value, unit);
					}
					return list[intValue];
				}
			}
		}
		
		@Override
		public String toString() {
			return ""+value+unit;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			result = prime * result + Float.floatToIntBits(value);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Angle other = (Angle) obj;
			if (unit != other.unit) return false;
			if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value)) return false;
			return true;
		}
	}

	public static Angle asAngle(final String angle) throws SyntaxException {
		if (angle == null || angle.isEmpty()) {
			throw new IllegalArgumentException("Angle string can't be null or empty");
		}
		else {
			try{return Angle.valueOf(angle);
			} catch (NumberFormatException exc) {
				throw new SyntaxException(0,0,"Angle ["+angle+"] has invalid syntax");
			}
		}
	}		
	
	public static class Time {
		private static final int					MAX_CACHEABLE = 128;
		private static final ArgumentType[]			LEXEMAS = {ArgumentType.ordinalFloat,ArgumentType.name};
		private static final Map<Units,Time[]> 		microCache = new EnumMap<>(Units.class);
		private static final float[][]				KOEFFS = new float[][] {
														//       msec 						sec
														/*msec*/ {1.0f, 					0.001f},
														/*sec*/  {1000f,	 				1.0f},
													};				
		private static final LightWeightRWLockerWrapper	locker = new LightWeightRWLockerWrapper();
		
		public enum Units {
			msec, sec								// Absolute			
		}
		
		private final float	value;
		private final Units	unit;
		
		public Time(final float value, final Units unit) {
			if (unit == null) {
				throw new NullPointerException("Unit type can't be null");
			}
			else {
				this.value = value;
				this.unit = unit;
			}
		}

		public float getValue() {
			return value;
		}

		public float getValueAs(final Units unit) {
			if (unit == null) {
				throw new NullPointerException("Unit type can't be null");
			}
			else if (this.unit == unit) {
				return getValue();
			}
			else {
				return KOEFFS[this.unit.ordinal()][unit.ordinal()]*getValue();
			}
		}

		public Units getUnit() {
			return unit;
		}

		public static boolean isValidTime(final String value) {
			if (value == null || value.isEmpty()) {
				throw new IllegalArgumentException("Value can't be null or empty");
			}
			else {
				try {
					return CharUtils.tryExtract(UnsafedUtils.getStringContent(value),0,(Object[])LEXEMAS) > 0;
				} catch (SyntaxException e) {
					return false;
				}
			}
		}
		
		public static Time valueOf(final String value) {
			if (value == null || value.isEmpty()) {
				throw new IllegalArgumentException("Value can't ne null or empty");
			}
			else {
				final char[] 	content = UnsafedUtils.getStringContent(value);
				final Object[]	result = new Object[2];
				
				try{CharUtils.extract(content,0,result,(Object[])LEXEMAS);
				} catch (SyntaxException e) {
					throw new IllegalArgumentException("String ["+value+"]: error at index ["+e.getCol()+"] ("+e.getLocalizedMessage()+")");
				}
				return valueOf(((Float)result[0]).floatValue(),Units.valueOf(result[1].toString()));
			}
		}

		public static boolean isValidTime(final char[] time) throws SyntaxException {
			if (time == null || time.length == 0) {
				throw new IllegalArgumentException("Distance can't be null or empty array");
			}
			else {
				try {
					return CharUtils.tryExtract(time,0,(Object[])LEXEMAS) > 0;
				} catch (SyntaxException e) {
					return false;
				}
			}
		}			
		
		public static Time valueOf(final char[] content) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content can't ne null or empty array");
			}
			else {
				final Object[]	result = new Object[2];
				
				try{CharUtils.extract(content,0,result,(Object[])LEXEMAS);
				} catch (SyntaxException e) {
					throw new IllegalArgumentException("String ["+new String(content)+"]: error at index ["+e.getCol()+"] ("+e.getLocalizedMessage()+")");
				}
				return valueOf(((Float)result[0]).floatValue(),Units.valueOf(result[1].toString()));
			}
		}
		
		public static Time valueOf(final float value, final Units unit) {
			if (value < 0) {
				throw new IllegalArgumentException("Value ["+value+"] can't ne negative");
			}
			else if (unit == null) {
				throw new NullPointerException("Unit value can't be null");
			}
			else if (value >= MAX_CACHEABLE || value != (float)((int)value)) {
				return new Time(value, unit);
			}
			else {
				final int	intValue = (int)value;
				
				try(final Locker	lock = locker.lock(true)) {
					Time[]			list = microCache.get(unit);
					
					if (list != null && list[intValue] != null) {
						return list[intValue];
					}
				}
				
				try(final Locker	lock = locker.lock(false)) {
					Time[]			list = microCache.get(unit);
					
					if (list == null) {
						microCache.put(unit,list = new Time[MAX_CACHEABLE]);
					}
					if (list[intValue] == null) {
						list[intValue] = new Time(value, unit);
					}
					return list[intValue];
				}
			}
		}
		
		@Override
		public String toString() {
			return ""+value+unit;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			result = prime * result + Float.floatToIntBits(value);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Time other = (Time) obj;
			if (unit != other.unit) return false;
			if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value)) return false;
			return true;
		}
	}

	public static boolean isValidTime(final String time) throws SyntaxException {
		if (time == null || time.isEmpty()) {
			throw new IllegalArgumentException("Time string can't be null or empty");
		}
		else {
			try{return Time.isValidTime(time);
			} catch (NumberFormatException exc) {
				throw new SyntaxException(0,0,"Time ["+time+"] has invalid syntax");
			}
		}
	}			

	public static boolean isValidTime(final char[] time) throws SyntaxException {
		if (time == null || time.length == 0) {
			throw new IllegalArgumentException("Time string can't be null or empty array");
		}
		else {
			try{return Time.isValidTime(time);
			} catch (NumberFormatException exc) {
				throw new SyntaxException(0,0,"Time ["+new String(time)+"] has invalid syntax");
			}
		}
	}			
	
	public static Time asTime(final String time) throws SyntaxException {
		if (time == null || time.isEmpty()) {
			throw new IllegalArgumentException("Time string can't be null or empty");
		}
		else {
			try{return Time.valueOf(time);
			} catch (NumberFormatException exc) {
				throw new SyntaxException(0,0,"Time ["+time+"] has invalid syntax");
			}
		}
	}			

	public static Time asTime(final char[] time) throws SyntaxException {
		if (time == null || time.length == 0) {
			throw new IllegalArgumentException("Time string can't be null or empty array");
		}
		else {
			try{return Time.valueOf(time);
			} catch (NumberFormatException exc) {
				throw new SyntaxException(0,0,"Time ["+new String(time)+"] has invalid syntax");
			}
		}
	}			
	
	public static class Frequency {
		private static final int					MAX_CACHEABLE = 128;
		private static final ArgumentType[]			LEXEMAS = {ArgumentType.ordinalFloat,ArgumentType.name};
		private static final float[][]				KOEFFS = new float[][] {
														//       Hz 					kHz
														/*Hz*/ 	{1.0f, 					0.001f},
														/*kHz*/	{1000f,	 				1.0f},
													};				
		private static final Map<Units,Frequency[]>	microCache = new EnumMap<>(Units.class);
		private static final LightWeightRWLockerWrapper	locker = new LightWeightRWLockerWrapper();
		
		public enum Units {
			Hz, kHz								// Absolute			
		}
		
		private final float	value;
		private final Units	unit;
		
		public Frequency(final float value, final Units unit) {
			if (value < 0) {
				throw new IllegalArgumentException("Frequency value ["+value+"] can't be negative");
			}
			else if (unit == null) {
				throw new NullPointerException("Unit type can't be null");
			}
			else {
				this.value = value;
				this.unit = unit;
			}
		}

		public float getValue() {
			return value;
		}

		public float getValueAs(final Units unit) {
			if (unit == null) {
				throw new NullPointerException("Unit type can't be null");
			}
			else if (this.unit == unit) {
				return getValue();
			}
			else {
				return KOEFFS[this.unit.ordinal()][unit.ordinal()]*getValue();
			}
		}
		
		public Units getUnit() {
			return unit;
		}

		public static Frequency valueOf(final String value) {
			if (value == null || value.isEmpty()) {
				throw new IllegalArgumentException("Value ["+value+"] can't ne null or empty");
			}
			else {
				final char[] 	content = UnsafedUtils.getStringContent(value);
				final Object[]	result = new Object[2];
				
				try{CharUtils.extract(content,0,result,(Object[])LEXEMAS);
				} catch (SyntaxException e) {
					throw new IllegalArgumentException("String ["+value+"]: error at index ["+e.getCol()+"] ("+e.getLocalizedMessage()+")");
				}
				return valueOf(((Float)result[0]).floatValue(),Units.valueOf(result[1].toString()));
			}
		}

		public static Frequency valueOf(final float value, final Units unit) {
			if (value < 0) {
				throw new IllegalArgumentException("Value ["+value+"] can't ne negative");
			}
			else if (unit == null) {
				throw new NullPointerException("Unit value can't be null");
			}
			else if (value >= MAX_CACHEABLE || value != (float)((int)value)) {
				return new Frequency(value, unit);
			}
			else {
				final int	intValue = (int)value;
				
				try(final Locker	lock = locker.lock(true)) {
					Frequency[]		list = microCache.get(unit);
					
					if (list != null && list[intValue] != null) {
						return list[intValue];
					}
				}
				
				try(final Locker	lock = locker.lock(false)) {
					Frequency[]		list = microCache.get(unit);
					
					if (list == null) {
						microCache.put(unit,list = new Frequency[MAX_CACHEABLE]);
					}
					if (list[intValue] == null) {
						list[intValue] = new Frequency(value, unit);
					}
					return list[intValue];
				}
			}
		}
		
		@Override
		public String toString() {
			return ""+value+unit;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			result = prime * result + Float.floatToIntBits(value);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Frequency other = (Frequency) obj;
			if (unit != other.unit) return false;
			if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value)) return false;
			return true;
		}
	}

	public static Frequency asFrequency(final String freq) throws SyntaxException {
		if (freq == null || freq.isEmpty()) {
			throw new IllegalArgumentException("Frequency string can't be null or empty");
		}
		else {
			try{return Frequency.valueOf(freq);
			} catch (NumberFormatException exc) {
				throw new SyntaxException(0,0,"Frequency ["+freq+"] has invalid syntax");
			}
		}
	}			

	public static class StylePropValue<T> {
		private final StylePropertiesSupported.ContentType	type;
		private final StylePropertiesSupported				prop;
		private final T										value;
		
		public StylePropValue(final ContentType type, final StylePropertiesSupported prop, final T value) {
			this.type = type;
			this.prop = prop;
			this.value = value;
		}

		public StylePropertiesSupported.ContentType getType() {
			return type;
		}

		public StylePropertiesSupported getProp() {
			return prop;
		}

		public T getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "StylePropValue [type=" + type + ", prop=" + prop + ", value=" + value + "]";
		}
	}
	
	
	public static class StylePropertiesStack implements Iterable<Map<String,Object>>{
		private final List<Map<String,Object>>	stack = new ArrayList<>();
		
		public void push(Map<String,Object> values) {
			if (values == null) {
				throw new NullPointerException("Values map can't be null");
			}
			else {
				stack.add(0,values);
			}
		}
		
		public int size() {
			return stack.size();
		}
		
		public Map<String,Object> peek() {
			if (stack.isEmpty()) {
				throw new EmptyStackException();
			}
			else {
				return stack.get(0);
			}
		}
		
		public Map<String,Object> pop() {
			if (stack.isEmpty()) {
				throw new EmptyStackException();
			}
			else {
				return stack.remove(0);
			}
		}

		@Override
		public Iterator<Map<String, Object>> iterator() {
			return stack.iterator();
		}

		@Override
		public String toString() {
			return "StylePropertiesStack [stack=" + stack + "]";
		}
	}

	public static class StylePropertiesTree {
		private static final Map<StylePropertiesSupported,StylePropertyDescription>	TREE = new EnumMap<>(StylePropertiesSupported.class);
		
		
		static {
			
		}
		
		public static boolean isPropertySupported(final String prop) {
			if (prop == null || prop.isEmpty()) {
				throw new IllegalArgumentException("Property string can't be null or empty");
			}
			else {
				return false;
			}
		}

		public static <T> T inferValue(final String prop, final StylePropertiesStack content) {
			if (prop == null || prop.isEmpty()) {
				throw new IllegalArgumentException("Property name can't be null or empty");
			}
			else if (content == null) {
				throw new IllegalArgumentException("Properties stack can't be null");
			}
			else if (!isPropertySupported(prop)) {
				for (Map<String, Object> item : content) {
					if (item.containsKey(prop)) {
						return (T)item.get(prop);
					}
				}
			}
			else {
				final StylePropertyDescription	desc = TREE.get(StylePropertiesSupported.valueOf(prop));
				
				for (Map<String, Object> item : content) {
					if (item.containsKey(prop)) {
						return (T)item.get(prop);
					}
					if (desc.isDetailedProperty()) {
						StylePropertyDescription	masterDesc = TREE.get(desc.getMasterProp());
						
						if (item.containsKey(masterDesc.getType().name())) {
							return (T)item.get(masterDesc.getType().name());
						}
					}
					if (desc.hasContainer()) {
						StylePropertyDescription	masterDesc = TREE.get(desc.getMasterProp());
						
						if (item.containsKey(masterDesc.getType().name())) {
							return (T)item.get(masterDesc.getType().name());
						}
					}
					if (!desc.isInheritanceSupported()) {
						break;
					}
				}
				return null;
			}
			return null;
		}

		public static Map<String,Object> inferAll(final StylePropertiesStack content) {
			return null;
		}		

		class StylePropertyDescription {
			private final StylePropertiesSupported		prop;
			private final boolean						inheritanceSupported, listSupported;
			private final StylePropertiesSupported[]	template;
			private final StylePropertyDescription[]	details;
			
			StylePropertyDescription(final StylePropertiesSupported prop, final boolean inheritanceSupported, final boolean listSupported, final String description, final StylePropertiesSupported... template) {
				if (prop == null) {
					throw new NullPointerException("Styled properties type can't be null");
				}
				else if (description == null || description.isEmpty()) {
					throw new IllegalArgumentException("Description can't be null or empty");
				}
				else if (template == null) {
					throw new NullPointerException("Template list can't be null");
				}
				else {
					this.prop = prop;
					this.inheritanceSupported = inheritanceSupported;
					this.listSupported = listSupported;
					this.template = template;
					this.details = null;
				}
			}
			
			StylePropertyDescription(final StylePropertiesSupported prop, final boolean inheritanceSupported, final boolean listSupported, final String description, final StylePropertyDescription... details) {
				if (prop == null) {
					throw new NullPointerException("Styled properties type can't be null");
				}
				else if (description == null || description.isEmpty()) {
					throw new IllegalArgumentException("Description can't be null or empty");
				}
				else if (details == null) {
					throw new NullPointerException("Details list can't be null");
				}
				else {
					this.prop = prop;
					this.inheritanceSupported = inheritanceSupported;
					this.listSupported = listSupported;
					this.template = null;
					this.details = details;
				}
			}
			
			StylePropertiesSupported getType() {
				return prop;
			}

			boolean isInheritanceSupported() {
				return inheritanceSupported;
			}
			
			boolean isListSupported() {
				return listSupported;
			}
			
			boolean isContainer() {
				return template != null;
			}
			
			boolean isDetailedProperty() {
				return details == null;
			}
			
			StylePropertiesSupported getMasterProp() {
				return null;
			}
			
			boolean hasContainer() {
				return false;
			}

			StylePropertiesSupported getContainerProp() {
				return null;
			}
			
			<T> Class<T> getValueClass() {
				return null;
			}
		}
	}

//	private static void addName(final SyntaxTreeInterface<StylePropValue<?>[]> keywords, final StylePropertiesSupported prop, final ValueListDescriptor values) {
//		final String[]	content = values.getContent(); 
//		
//		for (String item : content) {
//			final StylePropValue<String>	newValue = new StylePropValue<String>(ContentType.value,prop,item);
//			final long						id = keywords.seekName(item);
//			
//			if (id < 0) {
//				keywords.placeName(item,new StylePropValue[]{newValue});
//			}
//			else {
//				final StylePropValue<?>[]	currentList = keywords.getCargo(id);
//				final StylePropValue<?>[]	newList = Arrays.copyOf(currentList,currentList.length+1);  
//						
//				newList[newList.length-1] = newValue; 
//				keywords.setCargo(id, newList);
//			}
//		}
//	}

	private static class CSSLex {
		private static final int	OPER_ENDSWITH = 0;
		private static final int	OPER_CONTAINS = 1;
		private static final int	OPER_CONTAINS_VALUE = 2;
		private static final int	OPER_STARTSWITH = 3;
		private static final int	OPER_STARTS_OR_EQUALS = 4;
		private static final int	OPER_EQUALS = 5;
		
		private static final int	NODE_ASTERISK = 0;
		private static final int	NODE_TAG = 1;
		private static final int	NODE_ID = 2;
		private static final int	NODE_CLASS = 3;
		private static final int	NODE_ATTR = 4;
		private static final int	NODE_PSEUDOCLASS = 5;
		
		private enum CSSLExType {
			ASTERISK, ID, CLASS, PSEUDOCLASS, ATTRIBUTE, DIV, CONCAT, NUMBER, STRING, NAME, OPENB, CLOSEB, OPER, SEQUENT, PLUS, OPEN, CLOSE, PROPS, EOF,   
		}
		
		private final int			pos;
		private final CSSLExType	type;
		private final int			intValue;
		private final String		strValue;
		private final Properties	props;

		CSSLex(final int pos, final CSSLExType type) {
			this(pos,type, 0, null, null);
		}		
		
		CSSLex(final int pos, final CSSLExType type, final String strValue) {
			this(pos,type, 0, strValue, null);
		}		
		
		CSSLex(final int pos, final CSSLExType type, final int intValue) {
			this(pos,type,intValue,null, null);
		}

		CSSLex(final int pos, final CSSLExType type, final Properties props) {
			this(pos,type,0,null,props);
		}
		
		private CSSLex(final int pos, final CSSLExType type, final int intValue, final String strValue, final Properties props) {
			this.pos = pos;
			this.type = type;
			this.intValue = intValue;
			this.strValue = strValue;
			this.props = props;
		}

		@Override
		public String toString() {
			return "CSSLex [pos=" + pos + ", type=" + type + ", intValue=" + intValue + ", strValue=" + strValue + ", props=" + props + "]";
		}
	}
}
