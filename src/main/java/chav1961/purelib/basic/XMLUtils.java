package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public class XMLUtils {
	private static final char[]	AVAILABLE_IN_NAMES = {'-'};

	@FunctionalInterface
	public interface XMLWalkerCallback {
		ContinueMode process(NodeEnterMode mode, Element node);
	}
	
	public static ContinueMode walkDownXML(final Element root, final XMLWalkerCallback callback) throws NullPointerException {
		return walkDownXML(root,-1L,callback);
	}

	public static ContinueMode walkDownXML(final Element root, final long nodeTypes, final XMLWalkerCallback callback) throws NullPointerException {
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
	
	
	public static Map<String,Properties> parseCSS(final String cssContent) throws SyntaxException, IllegalArgumentException {
		if (cssContent == null || cssContent.isEmpty()) {
			throw new IllegalArgumentException("CSS content can't be null or empty");
		}
		else {
			final List<CSSLex>	lexemas = new ArrayList<>();
			final StringBuilder	sb = new StringBuilder();
			final int[]			nameRange = new int[2];
			final char[]		src = (cssContent+'\0').toCharArray();
			int					from = 0;
			
loop:		for (;;) {
				switch (src[from = CharUtils.skipBlank(src,from,false)]) {
					case '\0' 	:
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.EOF));
						break loop;
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
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.ID,new String(src,nameRange[0],nameRange[1]-nameRange[0])));
						}
						from++;
						break;
					case '.'	:
						from = CharUtils.parseNameExtended(src,from+1,nameRange,AVAILABLE_IN_NAMES);
						if (nameRange[0] == nameRange[1]) {
							throw new SyntaxException(0,from,"Missing class name");
						}
						else {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.CLASS,new String(src,nameRange[0],nameRange[1]-nameRange[0])));
						}
						from++;
						break;
					case ':'	:
						from = CharUtils.parseNameExtended(src,from+1,nameRange,AVAILABLE_IN_NAMES);
						if (nameRange[0] == nameRange[1]) {
							throw new SyntaxException(0,from,"Missing pseudoclass name");
						}
						else {
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.PSEUDOCLASS,new String(src,nameRange[0],nameRange[1]-nameRange[0])));
						}
						from++;
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
							from++;
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
						
						from = parseInnerCSS(src,from,nameRange,sb,props);
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.PROPS,props));
						break;
					case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						from = CharUtils.parseInt(src,from,nameRange,true);
						lexemas.add(new CSSLex(from,CSSLex.CSSLExType.NUMBER,nameRange[0]));
						break;
					default :
						if (Character.isJavaIdentifierStart(src[from])) {
							from = CharUtils.parseName(src,from,nameRange);
							lexemas.add(new CSSLex(from,CSSLex.CSSLExType.NAME,new String(src,nameRange[0],nameRange[1]-nameRange[0])));
						}
						else {
							throw new SyntaxException(0,from,"Unknown lexema");
						}
				}
			}
			final CSSLex[]	lex = lexemas.toArray(new CSSLex[lexemas.size()]);
			int				pos = 0;
			
			lexemas.clear();
			return null;
		}
	}	
	
	private static ContinueMode walkDownXMLInternal(final Element node, final long nodeTypes, final XMLWalkerCallback callback) {
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

	private static int parseInnerCSS(final char[] src, final int start, final int[] nameRange, final StringBuilder sb, final Properties props) throws SyntaxException {
		int	from = start, begin;
		
		do {from = CharUtils.parseNameExtended(src, CharUtils.skipBlank(src,from+1,false), nameRange, AVAILABLE_IN_NAMES);
			if (nameRange[0] == nameRange[1]) {
				throw new SyntaxException(0,from,"Property name is missing"); 
			}
			final String	name = new String(src,nameRange[0],nameRange[1]-nameRange[0]);
			
			from = CharUtils.skipBlank(src,from+1,false);
			if (src[from] == ':') {
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
			}
		}  while (src[from] != '\0' && src[from] != '}');
		
		if (src[from] != '}') {
			throw new SyntaxException(0,begin,"Unclosed '}'"); 
		}
		else {
			return from+1;
		}
	}

	private static int buildCSSTree(final CSSLex src, final int from, final CSSSyntaxNode level, final SyntaxNode<CSSSyntaxNode,?> node) {
		switch (level) {
		
		}
		return 0;
	}

	private enum CSSSyntaxNode {
		RECORD, OR, AND, 
	}
	
	private static class CSSLex {
		private static final int	OPER_ENDSWITH = 0;
		private static final int	OPER_CONTAINS = 1;
		private static final int	OPER_CONTAINS_VALUE = 2;
		private static final int	OPER_STARTSWITH = 3;
		private static final int	OPER_STARTS_OR_EQUALS = 4;
		private static final int	OPER_EQUALS = 5;
		
		private enum CSSLExType {
			ASTERISK, ID, CLASS, PSEUDOCLASS, DIV, NUMBER, STRING, NAME, OPENB, CLOSEB, OPER, SEQUENT, PLUS, OPEN, CLOSE, PROPS, EOF,   
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
