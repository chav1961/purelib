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
	
	private final Executor[]	commands;
	
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
			this.commands = new Executor[0];
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
			final List<Object>	stack = new ArrayList<>();
			int	pc = 0;
			
			while (pc < commands.length) {
				pc = commands[pc].execute(stack, getter);
			}
			
			return (Boolean)stack.get(0);
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
				from = buildTree(Depth.SEQUENCE, content, from, node);
				if (content[from].type == LexType.AND) {
					final List<SyntaxNode<NodeType, SyntaxNode>>	list = new ArrayList<>();
					
					list.add((SyntaxNode<NodeType, SyntaxNode>)node.clone());
					do {final SyntaxNode<NodeType, SyntaxNode>	right = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
					
						from = buildTree(Depth.SEQUENCE, content, from + 1, node);
					} while (content[from].type == LexType.AND);
					
					node.type = NodeType.AND;
					node.children = list.toArray(new SyntaxNode[list.size()]); 
				}
				break;
			case SEQUENCE	:
				final List<SyntaxNode<NodeType, SyntaxNode>>	list = new ArrayList<>();
				
				do {
					from = buildTree(Depth.PREFIX, content, from, node);
					list.add((SyntaxNode<NodeType, SyntaxNode>) node.clone());
				} while (content[from].type != LexType.EOF && content[from].type != LexType.OR && content[from].type != LexType.AND);
				if (list.isEmpty()) {
					throw new SyntaxException(from, from, KWD_AND);
				}
				else if (list.size() > 1) {
					node.type = NodeType.SEQUENCE;
					node.children = list.toArray(new SyntaxNode[list.size()]);
				}
				break;
			case PREFIX	:
				switch (content[from].type) {
					case PLUS 	:
						final SyntaxNode<NodeType, SyntaxNode>	plusNode = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
						
						from = buildTree(Depth.SUFFIX, content, from, plusNode);
						node.type = NodeType.PLUS;
						node.children = new SyntaxNode[] {plusNode};
						break;
					case MINUS	:
						final SyntaxNode<NodeType, SyntaxNode>	minusNode = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
						
						from = buildTree(Depth.SUFFIX, content, from, minusNode);
						node.type = NodeType.MINUS;
						node.children = new SyntaxNode[] {minusNode};
						break;
					case NOT	:
						final SyntaxNode<NodeType, SyntaxNode>	notNode = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
						
						from = buildTree(Depth.SUFFIX, content, from, notNode);
						node.type = NodeType.NOT;
						node.children = new SyntaxNode[] {notNode};
						break;
					default :
						from = buildTree(Depth.SUFFIX, content, from, node);
				}
				break;
			case SUFFIX	:
				from = buildTree(Depth.TERM, content, from, node);
				switch (content[from].type) {
					case TILDE	:
						final SyntaxNode<NodeType, SyntaxNode>	tildeNode = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
						
						if (content[from+1].type == LexType.INTEGER) {
							node.type = NodeType.PROXIMITY;
							node.value = content[from+1].constValue;  
							node.children = new SyntaxNode[] {tildeNode};
							from += 2;
						}
						else if (content[from+1].type == LexType.NUMBER) {
							node.type = NodeType.FUZZY;
							node.value = content[from+1].constValue;  
							node.children = new SyntaxNode[] {tildeNode};
							from += 2;
						}
						else {
							node.type = NodeType.FUZZY;
							node.value = Double.doubleToLongBits(0.5);  
							node.children = new SyntaxNode[] {tildeNode};
							from++;
						}
						break;
					case POWER	:
						final SyntaxNode<NodeType, SyntaxNode>	powerNode = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
						
						if (content[from+1].type == LexType.INTEGER) {
							node.type = NodeType.RELEVANCE;
							node.value = content[from+1].constValue;  
							node.children = new SyntaxNode[] {powerNode};
							from += 2;
						}
						else {
							throw new SyntaxException(from, from, KWD_AND);
						}
						break;
					default :
						break;
				}
				break;
			case TERM	:
				switch (content[from].type) {
					case OPEN 		:
						from = buildTree(Depth.OR, content, from + 1, node);
						if (content[from].type == LexType.CLOSE) {
							from++;
						}
						else {
							throw new SyntaxException(from, from, KWD_AND);
						}
						break;
					case OPENB : case OPENF	:
						final SyntaxNode<NodeType, SyntaxNode>	lowNode = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
						final SyntaxNode<NodeType, SyntaxNode>	highNode = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
						final boolean	lowInclusive = content[from].type == LexType.OPENB;
						final NodeType	type;
						
						if (content[from+1].type == LexType.INTEGER && content[from+2].type == LexType.TO && content[from+3].type == LexType.INTEGER) {
							lowNode.type = NodeType.INTEGER;
							lowNode.value = content[from+1].constValue; 
							highNode.type = NodeType.INTEGER;
							highNode.value = content[from+3].constValue;
							type = NodeType.NUM_RANGE;
						}
						else if (content[from+1].type == LexType.TEXT && content[from+2].type == LexType.TO && content[from+3].type == LexType.TEXT) {
							lowNode.type = NodeType.TEXT;
							lowNode.value = content[from+1].constValue; 
							highNode.type = NodeType.TEXT;
							highNode.value = content[from+3].constValue; 
							type = NodeType.TEXT_RANGE;
						}
						else {
							throw new SyntaxException(from, from, KWD_AND);
						}
						if (content[from].type == LexType.CLOSEB || content[from].type == LexType.CLOSEF) {
							final boolean	highInclusive = content[from].type == LexType.CLOSEB; 
							node.type = type;
							node.children = new SyntaxNode[] {lowNode, highNode};
							node.value = (lowInclusive ? 1 : 0) + (highInclusive ? 2 : 0); 
							from++;
						}
						else {
							throw new SyntaxException(from, from, KWD_AND);
						}
						break;
					case TEXT		:
						if (content[from+1].type == LexType.COLON) {
							final SyntaxNode<NodeType, SyntaxNode>	compareNode = (SyntaxNode<NodeType, SyntaxNode>)node.clone();
							final String	field = content[from].content; 
							
							from = buildTree(Depth.TERM, content, from + 2, node);
							node.type = NodeType.EXTRACT;
							node.cargo = field;
							node.children = new SyntaxNode[] {compareNode};
						}
						else {
							node.type = NodeType.TEXT;
							node.cargo = content[from].content;
							from++;
						}
						break;
					case QUOTED		:
						node.type = NodeType.QUOTED;
						node.cargo = content[from].content;
						from++;
						break;
					case WILDCARD	:
						node.type = NodeType.WILDCARD;
						node.cargo = content[from].content;
						from++;
						break;
					case EOF : case OR : case AND :
						break;
					default :
						throw new SyntaxException(from, from, KWD_AND);
				}
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
		SEQUENCE,
		NOT,
		PLUS,
		MINUS,
		PROXIMITY,
		FUZZY,
		RELEVANCE,
		INTEGER,
		NUMBER,
		QUOTED,
		WILDCARD,
		TEXT,
		EXTRACT,
		NUM_RANGE,
		TEXT_RANGE;
	}
	
	static enum Depth {
		OR,
		AND,
		SEQUENCE,
		PREFIX,
		SUFFIX,
		TERM
	}

	static interface Executor {
		int execute(List<Object> content, Function<String, Object> getter);
	}
}
