package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.SyntaxNode;

// https://lucene.apache.org/core/2_9_4/queryparsersyntax.html
public class PseudoLuceneMatcher {
	private static final char	EOF = '\0';
	private static final String	KWD_OR = "OR";
	private static final String	KWD_AND = "AND";
	private static final String	KWD_NOT = "NOT";
	private static final String	KWD_TO = "TO";
	
	public PseudoLuceneMatcher(final String expression) throws IllegalArgumentException, SyntaxException {
		if (Utils.checkEmptyOrNullString(expression)) {
			throw new IllegalArgumentException("Expression can't be null or empty");
		}
		else {
			final char[]		content = CharUtils.terminateAndConvert2CharArray(expression, EOF);
			final List<Lexema>	parsed = new ArrayList<>();
			final SyntaxNode<NodeType, SyntaxNode>	root = new SyntaxNode<>(0, 0, NodeType.ROOT, 0, null); 
			
			parse(content, 0, parsed);
			final int	where = buildTree(Depth.OR, parsed.toArray(new Lexema[parsed.size()]), 0, root); 
		}
	}
	

	public boolean matches(final Map<String, ?> values) {
		if (values == null) {
			throw new NullPointerException("Values can't be null");
		}
		else {
			return matches((s)->values.get(s));
		}
	}
	
	public boolean matches(final Function<String, Object> getter) {
		if (getter == null) {
			throw new NullPointerException("Getter can't be null");
		}
		else {
			return false;
		}
	}

	static void parse(final char[] content, int from, final List<Lexema> parsed) throws SyntaxException {
		final StringBuilder	sb = new StringBuilder();
		final int[]			forNames = new int[2];
		final long[]		forNumbers = new long[2];
		char				temp;
		
loop:	for(;;) {
			while (content[from] <= ' ' && content[from] != EOF) {
				from++;
			}
			final int	start = from;
			
			switch (content[from]) {
				case EOF 	:
					break loop;
				case ':'	:
					parsed.add(new Lexema(from++, LexType.COLON));
					break;
				case '~'	:
					parsed.add(new Lexema(from++, LexType.TILDE));
					break;
				case '('	:
					parsed.add(new Lexema(from++, LexType.OPEN));
					break;
				case ')'	:
					parsed.add(new Lexema(from++, LexType.CLOSE));
					break;
				case '['	:
					parsed.add(new Lexema(from++, LexType.OPENB));
					break;
				case ']'	:
					parsed.add(new Lexema(from++, LexType.CLOSEB));
					break;
				case '{'	:
					parsed.add(new Lexema(from++, LexType.OPENF));
					break;
				case '}'	:
					parsed.add(new Lexema(from++, LexType.CLOSEF));
					break;
				case '^'	:
					parsed.add(new Lexema(from++, LexType.POWER));
					break;
				case '+'	:
					parsed.add(new Lexema(from++, LexType.PLUS));
					break;
				case '-'	:
					parsed.add(new Lexema(from++, LexType.MINUS));
					break;
				case '\"'	:
					from = CharUtils.parseUnescapedString(content, from + 1, '\"', true, forNames);
					parsed.add(new Lexema(start, LexType.QUOTED, new String(content, forNames[0], forNames[1] - forNames[0])));
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					from = CharUtils.parseNumber(content, from, forNumbers, CharUtils.PREF_ANY, false);
					if (forNumbers[1] == CharUtils.PREF_INT || forNumbers[1] == CharUtils.PREF_LONG) {
						parsed.add(new Lexema(start, forNumbers[0]));
					}
					else {
						parsed.add(new Lexema(start, Double.longBitsToDouble(forNumbers[0])));
					}
					break;
				case '\\'	:
					from++;
					break;
				default :
					if (Character.isJavaIdentifierStart(content[from])) {
						boolean	wildCard = false;
						
						sb.setLength(0);
						while (Character.isJavaIdentifierPart(temp = content[from]) || temp == '?' || temp == '*' || temp == '\\') {
							if (temp != '\\') {
								sb.append(temp);
								if (temp == '?' || temp == '*') {
									wildCard = true;
								}
								from++;
							}
							else if (content[from + 1] != EOF) {
								sb.append(content[from + 1]);
								from += 2;
							}
						}
						final String	val = sb.toString();
						
						if (KWD_OR.equalsIgnoreCase(val)) {
							parsed.add(new Lexema(from, LexType.OR));
						}
						else if (KWD_AND.equalsIgnoreCase(val)) {
							parsed.add(new Lexema(from, LexType.AND));
						}
						else if (KWD_NOT.equalsIgnoreCase(val)) {
							parsed.add(new Lexema(from, LexType.NOT));
						}
						else if (KWD_TO.equalsIgnoreCase(val)) {
							parsed.add(new Lexema(from, LexType.TO));
						}
						else if (wildCard) {
							parsed.add(new Lexema(from, LexType.WILDCARD, val));
						}
						else {
							parsed.add(new Lexema(from, LexType.TEXT, val));
						}
					}
					else {
						throw new SyntaxException(0, start, "???");
					}
			}
		}
	}
	
	static int buildTree(final Depth depth, final Lexema[] content, int from, final SyntaxNode<NodeType, SyntaxNode> node) throws SyntaxException {
		switch (depth) {
			case OR		:
				from = buildTree(Depth.AND, content, from, node);
				if (content[from].type == LexType.OR) {
					final List<SyntaxNode<NodeType, SyntaxNode>>	list = new ArrayList<>();
					
					list.add((SyntaxNode<NodeType, SyntaxNode>)node.clone());
					do {final SyntaxNode<NodeType, SyntaxNode>	right = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
					
						from = buildTree(Depth.AND, content, from + 1, node);
					} while (content[from].type == LexType.OR);
					
					node.type = NodeType.OR;
					node.children = list.toArray(new SyntaxNode[list.size()]); 
				}
				break;
			case AND	:
				from = buildTree(Depth.NOT, content, from, node);
				if (content[from].type == LexType.AND) {
					final List<SyntaxNode<NodeType, SyntaxNode>>	list = new ArrayList<>();
					
					list.add((SyntaxNode<NodeType, SyntaxNode>)node.clone());
					do {final SyntaxNode<NodeType, SyntaxNode>	right = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
					
						from = buildTree(Depth.NOT, content, from + 1, node);
					} while (content[from].type == LexType.AND);
					
					node.type = NodeType.AND;
					node.children = list.toArray(new SyntaxNode[list.size()]); 
				}
				break;
			case NOT	:
				if (content[from].type == LexType.NOT) {
					final SyntaxNode<NodeType, SyntaxNode>	operand = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
					
					from = buildTree(Depth.COLON, content, from, operand);
					node.type = NodeType.NOT;
					node.cargo = operand;
				}
				else {
					from = buildTree(Depth.COLON, content, from, node);
				}
				break;
			case COLON	:
				from = buildTree(Depth.RANGE, content, from, node);
				if (content[from].type == LexType.COLON) {
					if (node.getType() == NodeType.VALUE) {
						final SyntaxNode<NodeType, SyntaxNode>	right = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
						
						from = buildTree(Depth.RANGE, content, from + 1, right);
						node.type = NodeType.CALCULATE;
						node.children = new SyntaxNode[] {right};
					}
					else {
						throw new SyntaxException(from, from, KWD_AND);
					}
				}
				else {
					final SyntaxNode<NodeType, SyntaxNode>	right = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
					
					node.type = NodeType.CALCULATE;
					node.cargo = "text";
					node.children = new SyntaxNode[] {right}; 
				}
				break;
			case RANGE	:
				if (content[from].type == LexType.OPENB) {
					final SyntaxNode<NodeType, SyntaxNode>	left = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
					
					from = buildTree(Depth.TERM, content, from + 1, left);
					if (content[from].type == LexType.TO) {
						final SyntaxNode<NodeType, SyntaxNode>	right = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
						
						from = buildTree(Depth.TERM, content, from + 1, right);
						if (content[from].type == LexType.CLOSEB) {
							from++;
							node.type = NodeType.RANGE_DATE;
							node.children = new SyntaxNode[] {left, right};
						}
						else {
							throw new SyntaxException(from, from, KWD_AND);
						}
					}
				}
				else if (content[from].type == LexType.OPENF) {
					final SyntaxNode<NodeType, SyntaxNode>	left = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
					
					from = buildTree(Depth.TERM, content, from + 1, left);
					if (content[from].type == LexType.TO) {
						final SyntaxNode<NodeType, SyntaxNode>	right = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
						
						from = buildTree(Depth.TERM, content, from + 1, right);
						if (content[from].type == LexType.CLOSEF) {
							from++;
							node.type = NodeType.RANGE_STRING;
							node.children = new SyntaxNode[] {left, right};
						}
						else {
							throw new SyntaxException(from, from, KWD_AND);
						}
					}
				}
				else {
					from = buildTree(Depth.PREFIX, content, from, node);
				}
				break;
			case PREFIX	:
				if (content[from].type == LexType.PLUS) {
					
				}
				else if (content[from].type == LexType.MINUS) {
					
				}
				else {
					from = buildTree(Depth.POSTFIX, content, from, node);
				}
				break;
			case POSTFIX:
				from = buildTree(Depth.TERM, content, from, node);
				if (content[from].type == LexType.TILDE) {
					
				}
				else if (content[from].type == LexType.POWER) {
					
				}
				break;
			case TERM	:
				break;
			default:
				throw new UnsupportedOperationException("Depth ["+depth+"] is not supported yet");
		}
		return from;
	}
	
	
	static enum LexType {
		EOF,
		INTEGER,
		NUMBER,
		OPEN,
		CLOSE,
		OPENB,
		CLOSEB,
		OPENF,
		CLOSEF,
		PLUS,
		MINUS,
		COLON,
		TILDE,
		POWER,
		QUOTED,
		WILDCARD,
		TEXT,
		OR,
		AND,
		NOT,
		TO;
	}

	static class Lexema {
		private final int		pos;
		private final LexType	type;
		private final long		constValue;
		private final String	content;

		public Lexema(final int pos, final LexType type) {
			super();
			this.pos = pos;
			this.type = type;
			this.constValue = 0;
			this.content = null;
		}
		
		public Lexema(final int pos, final long constValue) {
			super();
			this.pos = pos;
			this.type = LexType.INTEGER;
			this.constValue = constValue;
			this.content = null;
		}
		
		public Lexema(final int pos, final double constValue) {
			super();
			this.pos = pos;
			this.type = LexType.NUMBER;
			this.constValue = Double.doubleToLongBits(constValue);
			this.content = null;
		}
		
		public Lexema(final int pos, final LexType type, final String content) {
			super();
			this.pos = pos;
			this.type = type;
			this.constValue = 0;
			this.content = content;
		}
	}

	static enum NodeType {
		ROOT,
		OR,
		AND,
		NOT,
		CALCULATE,
		RANGE_DATE,
		RANGE_STRING,
		PROXIMITY,
		FUZZY,
		REGEX,
		VALUE,
		NUMBER,
		MANDATORY,
		EXCLUDE;
	}
	
	static enum Depth {
		OR,
		AND,
		NOT,
		COLON,
		RANGE,
		PREFIX,
		POSTFIX,
		TERM
	}
}
