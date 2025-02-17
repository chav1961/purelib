package chav1961.purelib.parsers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.parsers.PatternAndProcessor.OutputProcessor;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.cdb.interfaces.RuleBasedParser;
import chav1961.purelib.i18n.PureLibLocalizer;

class CommandParser {
	static final char[][]		NULL_TERMINALS = new char[0][0];
	static final long			AMP_ID = 0L;
	
	private static BitCharSet	STOP_CHARS = new BitCharSet("\r\n\t\f [];\\=><#");
	private static final char[]	AMP_TEXT = "&".toCharArray();
	private static final RuleBasedParser<Expression, Object>	EXPRESSION;
	
	static {
		try{
			final Class<RuleBasedParser<Expression, Object>>	cl = CompilerUtils.buildRuleBasedParserClass(CommandParser.class.getPackageName()+".ExpressionSkipper", Expression.class, Utils.fromResource(CommandParser.class.getResource("expression.txt")));
			
			EXPRESSION = cl.getConstructor(Class.class,SyntaxTreeInterface.class).newInstance(Expression.class,new AndOrTree<>());
		} catch (SyntaxException | IOException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}
	}

	
	static PatternAndProcessor build(final char[] content, int from, final boolean fourLetter) throws SyntaxException {
		final SyntaxTreeInterface<Long>			names = new AndOrTree<>(1,1);
		final SyntaxNode<NodeType, SyntaxNode> 	root = new SyntaxNode<>(0, 0, NodeType.Root, 0, null);
		final Lexema[]							lex = parse(content, from, names, fourLetter);
		
		from = buildTree(lex, 0, Level.Top, root);
		return new PatternAndSubstitutorImpl(names, root, 0);
	}

	//temp[0] - current pos in the root children
	//temp[1] - from pos in the content
	static boolean identify(final char[] content, int from, final SyntaxTreeInterface<Long> names, final SyntaxNode<NodeType, SyntaxNode> root, final int[] temp, final int[][] markerRanges) throws SyntaxException {
		int		current = 0, total = root.children != null ? root.children.length : 0;
		int		start = from, rangesId = 0;
		long	id;
		
		while (content[from] != '\n' && current < total) {
			from = CharUtils.skipBlank(content, from, true);
			
			switch((NodeType)root.children[current].type) {
				case Sequence	:
					for (SyntaxNode<NodeType, SyntaxNode> item : root.children) {
						switch (item.type) {
							case Mandatory	:	
								if (!identify(content, from, names, item, temp, markerRanges)) {
									temp[0] = current;
									temp[1] = from;
									return false;
								}
								else {
									from = temp[1];
									current++;
									break;
								}
							case Optional	:
								while (identify(content, from, names, item, temp, markerRanges)) {
									from = temp[1];
								}
								break;
							case Sequence	:
								if (identify(content, from, names, item, temp, markerRanges)) {
									from = temp[1];
									current++;
									break;
								}
								else {
									temp[0] = current;
									return false;
								}
							default:
								break;
						}
					}
					break;
				case Mandatory	:
					final List<int[]>	markers = new ArrayList<>();
					
					if (identify(content, from, names, (Lexema)root.children[current].cargo, temp, markers)) {
						from = temp[1];
						current++;
					}
					else {
						temp[0] =  current;
						temp[1] = start;
						return false;
					}
					break;
				case Optional	:
					break;
				default:
					break;
			}
		}
		
		if (current >= total) {
			temp[0] = current;
			temp[1] = from;
			return true;
		}
		else {
			temp[0] = current;
			temp[1] = start;
			return false;
		}
	}

	static boolean identify(final char[] content, int from, final SyntaxTreeInterface<Long> names, final Lexema lex, final int[] temp, final List<int[]> markerRanges) throws SyntaxException {
		final int	start = from;
		
		from = CharUtils.skipBlank(content, from, true);
		
		switch (lex.type) {
			case Char				:
				final char[]	template = lex.sequence;

				for(int index = 0; index < template.length; index++) {
					if (template[index] <= ' ') {
						continue;
					}
					else {
						from = CharUtils.skipBlank(content, from, true);
						
						if (content[from] != template[index]) {
							temp[1] = start;
							return false;
						}
						else {
							from++;
						}
					}
				}
				temp[1] = from;
				return true;
			case ExtendedMarker		:
				temp[0] = from;
				from = extractExpression(content, from, NULL_TERMINALS);
				temp[1] = from;
				markerRanges.add(temp.clone());
				return true;
			case Keyword			:
				if (Character.isJavaIdentifierStart(content[from])) {
					from = CharUtils.parseName(content, from, temp);
					final long 	kw = names.seekNameI(content, temp[0], temp[1] + 1);
					
					if (kw == lex.entityId) {
						temp[1] = from;
						return true;
					}
					else {
						return false;
					}
				}
				else {
					return false;
				}
			case ListMarker			:
				from--;
				do {temp[0] = ++from;
					from = extractExpression(content, from, NULL_TERMINALS);
					temp[1] = from;
					markerRanges.add(temp.clone());
					from = CharUtils.skipBlank(content, from, true);
				} while (content[from] == ',');
				return true;
			case RegularMarker		:
				temp[0] = from;
				final SyntaxNode root = new SyntaxNode(0,0,Expression.Rule,0,null);
				
				from = EXPRESSION.parse(content, from, root);
				temp[1] = from;
				markerRanges.add(temp.clone());
				return true;
			case RestrictedMarker	:
				break;
			case WildMarker			:
				temp[0] = from;
				while (content[from] != '\r' && content[from] != '\n') {
					from++;
				}
				temp[1] = from;
				markerRanges.add(temp.clone());
				return true;
			default	: return false;
		}
		return false;
	}	
	
	
	static void upload(final char[] content, final SyntaxNode<NodeType, SyntaxNode> root, final int[] temp, final int[][] markerRanges,final OutputProcessor wr) {
	}
	
	static Lexema[] parse(final char[] content, int from, final SyntaxTreeInterface<Long> names, final boolean fourLetter) throws SyntaxException {
		final List<Lexema>	result = new ArrayList<>();
		final int[]			forNames = new int[2];
		int					idCount = 0;  
		boolean				afterErgo = false;
		
		names.placeName(AMP_TEXT, 0, AMP_TEXT.length, AMP_ID);
		
loop:	for(;;) {
			from = CharUtils.skipBlank(content, from, true);
			
			switch (content[from]) {
				case '\r' : case '\n' :
					result.add(new Lexema(Lexema.LexType.EOF));
					break loop;
				case '[' :
					result.add(new Lexema(Lexema.LexType.OpenB));
					break;
				case ']' :
					result.add(new Lexema(Lexema.LexType.CloseB));
					break;
				case ';' :
					if (!afterErgo) {
						throw new SyntaxException(0, from, "Use ';' in the left of '=>'");
					}
					else {
						result.add(new Lexema(Lexema.LexType.Continuation));
					}
					break;
				case '\\' :
					result.add(new Lexema(Lexema.LexType.Char, content[++from]));
					break;
				case '=' :
					if (content[from + 1] == '>') {
						result.add(new Lexema(Lexema.LexType.Ergo));
						afterErgo = true;
						from++;
					}
					else {
						result.add(new Lexema(Lexema.LexType.Char, content[from]));
					}
					break;
				case '#' :
					if (content[from + 1] == '<') {
						if (!afterErgo) {
							throw new SyntaxException(0, from, "Use dumb result marker in the left of '=>'");
						}
						else if (Character.isLetter(content[from + 2])) {
							from = extractName(content, from+2, names, forNames, idCount);
							if (content[from] != '>') {
								throw new SyntaxException(0, from, "Missing '>'");
							}
							else {
								idCount += forNames[0];
								result.add(new Lexema(Lexema.LexType.DumbResultMarker, forNames[1]));
							}
						}
						else {
							
						}
					}
					else {
						result.add(new Lexema(Lexema.LexType.Char, content[from]));
					}
					break;
				case '<' :
					switch (content[from + 1]) {
						case '(' 	:
							if (Character.isLetter(content[from + 2])) {
								from = extractName(content, from + 2, names, forNames, idCount);
								if (content[from] == ')' && content[from + 1] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(afterErgo ? Lexema.LexType.SmartResultMarker : Lexema.LexType.ExtendedMarker, forNames[1]));
									from++;
								}
								else {
									throw new SyntaxException(0, from, "Missing ')>'");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
							break;
						case '*' 	:
							if (afterErgo) {
								throw new SyntaxException(0, from, "Use wild marker in the right of '=>'");
							}
							else if (Character.isLetter(content[from + 2])) {
								from = extractName(content, from + 2, names, forNames, idCount);
								if (content[from] == '*' && content[from + 1] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(Lexema.LexType.WildMarker, forNames[1]));
									from++;
								}
								else {
									throw new SyntaxException(0, from, "Missing '*>'");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
							break;
						case '\"'	:
							if (!afterErgo) {
								throw new SyntaxException(0, from, "Use string result marker in the left of '=>'");
							}
							else if (Character.isLetter(content[from + 2])) {
								from = extractName(content, from + 2, names, forNames, idCount);
								if (content[from] == '\"' && content[from + 1] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(Lexema.LexType.NormalResultMarker, forNames[1]));
									from++;
								}
								else {
									throw new SyntaxException(0, from, "Missing '\">'");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
							break;
						case '{'	:
							if (!afterErgo) {
								throw new SyntaxException(0, from, "Use block result marker in the left of '=>'");
							}
							else if (Character.isLetter(content[from + 2])) {
								from = extractName(content, from + 2, names, forNames, idCount);
								if (content[from] == '}' && content[from + 1] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(Lexema.LexType.BlockResultMarker, forNames[1]));
									from++;
								}
								else {
									throw new SyntaxException(0, from, "Missing '.>'");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
							break;
						case '.'	:
							if (!afterErgo) {
								throw new SyntaxException(0, from, "Use logify result marker in the left of '=>'");
							}
							else if (Character.isLetter(content[from + 2])) {
								from = extractName(content, from + 2, names, forNames, idCount);
								if (content[from] == '.' && content[from + 1] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(Lexema.LexType.BoolResultMarker, forNames[1]));
									from++;
								}
								else {
									throw new SyntaxException(0, from, "Missing '.>'");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
							break;
						default :
							if (Character.isLetter(content[from + 1])) {
								from = extractName(content, from + 1, names, forNames, idCount);
								
								if (content[from] == ',' && content[from + 1] == '.' && content[from + 2] == '.' && content[from + 3] == '.' && content[from + 4] == '>') {
									if (!afterErgo) {
										idCount += forNames[0];
										result.add(new Lexema(Lexema.LexType.ListMarker, forNames[1]));
										from += 4;
									}
									else {
										throw new SyntaxException(0, from, "Use list marker in the right of '=>'");
									}
								}
								else if (content[from] == '>') {
									idCount += forNames[0];
									result.add(new Lexema(afterErgo ? Lexema.LexType.RegularResultMarker : Lexema.LexType.RegularMarker, forNames[1]));
								}
								else if (content[from] == ':') {
									final List<Long>	list = new ArrayList<>();
									final int			nameId = forNames[1];
									
									idCount += forNames[0];
									from = extractList(content, from, names, forNames, idCount, list);
									if (content[from] == '>') {
										if (afterErgo) {
											throw new SyntaxException(0, from, "Use restricted marker in the right of '=>'");
										}
										else {
											result.add(new Lexema(Lexema.LexType.RestrictedMarker, nameId, Utils.unwrapArray(list.toArray(new Long[list.size()]))));
										}
									}
									else {
										throw new SyntaxException(0, from, "Missing '>'");
									}
								}
								else {
									throw new SyntaxException(0, from, "Missing ':'");
								}
							}
							else {
								throw new SyntaxException(0, from, "Missing name");
							}
					}
					break;
				default :
					if (Character.isLetter(content[from])) {
						from = CharUtils.parseName(content, from, forNames) - 1;
						
						long	nameId = names.seekNameI(content, forNames[0], forNames[1] + 1);
						
						if (nameId < 0) {
							nameId = names.placeName(content, forNames[0], forNames[1] + 1, 0L);
						}
						
						result.add(new Lexema(Lexema.LexType.Keyword, nameId));
//						System.err.println("===== add kwd: ["+new String(content,forNames[0],forNames[1]-forNames[0]+1)+"]");
					}
					else if (content[from] > ' ') {
						forNames[0] = from;
						while (!STOP_CHARS.contains(content[from]) && !Character.isJavaIdentifierStart(content[from])) {
							from++;
						}
						result.add(new Lexema(Lexema.LexType.Char, Arrays.copyOfRange(content, forNames[0], from)));
						from--;
//						System.err.println("===== add chars: ["+new String(content,forNames[0],forNames[1]-forNames[0])+"]");
					}
					else {
						throw new SyntaxException(0, from, "Illegal char ["+(0+content[from])+"]");
					}
//					names.walk((c,len,id,car)->{
//						System.err.println(new String(c,0,len));
//						return true;
//					});
//					System.err.println("-----");
			}
			from++;
		}
		return result.toArray(new Lexema[result.size()]);
	}
	
	private static int extractName(final char[] content, int from, final SyntaxTreeInterface<Long> names, final int[] temp, final int newId) {
		from = CharUtils.parseName(content, from, temp);
		
		final long	id = names.seekNameI(content, temp[0], temp[1] + 1);
		
		if (id >= 0) {
			temp[0] = 0;
			temp[1] = names.getCargo(id).intValue();
		}
		else {
			names.placeName(content, temp[0], temp[1] + 1, Long.valueOf(newId));
			temp[0] = 1;
			temp[1] = newId;
		}
		return from;
	}

	private static int extractList(final char[] content, int from, final SyntaxTreeInterface<Long> names, final int[] temp, int newId, final List<Long> list) {
		do {	// colon or div awaited here!
			from = CharUtils.skipBlank(content, from + 1, true);
			if (Character.isLetter(content[from])) {
				from = extractName(content, from, names, temp, newId);
				newId += temp[0];
				list.add(Long.valueOf(temp[1]));
			}
			else if (content[from] == '&') {
				list.add(AMP_ID);
				from++;
			}
			from = CharUtils.skipBlank(content, from, true);
		} while (content[from] == ',');
		
		return from;
	}
	
	private static int extractExpression(final char[] content, int from, final char[]... terminals) throws SyntaxException {
		boolean	inQuotes = false;
		
		for(;;) {
			switch(content[from]) {
				case '\n' : case ']' : case ')' : case '}' :
					return from;
				case '\"' : case '\'' :
					inQuotes = !inQuotes;
					break;
				case '['	:
					if (!inQuotes && content[from = extractExpression(content, from + 1, NULL_TERMINALS)] != ']') {
						throw new SyntaxException(0, from, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_MISSING_CLOSE_SQUARE_BRACKET));
					}
					break;
				case '(' 	:
					if (!inQuotes && content[from = extractExpression(content, from + 1, NULL_TERMINALS)] != ')') {
						throw new SyntaxException(0, from, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_MISSING_CLOSE_BRACKET));
					}
					break;
				case '{' 	: 
					if (!inQuotes && content[from = extractExpression(content, from + 1, NULL_TERMINALS)] != '}') {
						throw new SyntaxException(0, from, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_MISSING_CLOSE_FIGURE_BRACKET));
					}
					break;
				default :
					if (!inQuotes) {
						for (char[] item : terminals) {
							if (content[from] == item[0] && CharUtils.compare(content, from, item)) {
								return from;
							}
						}
					}
			}
			from++;
		}
	}
	
	static int buildTree(final Lexema[] content, int from, final Level level, final SyntaxNode<NodeType, SyntaxNode> root) throws SyntaxException {
		switch (level) {
			case Top	:
				final SyntaxNode<NodeType, SyntaxNode>	left = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
				final SyntaxNode<NodeType, SyntaxNode>	right = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
				
				from = buildTree(content, from, Level.Left, left);
				if (content[from].type == Lexema.LexType.Ergo) {
					from = buildTree(content, from + 1, Level.Right, right);
					root.type = NodeType.Root;
					root.children = new SyntaxNode[]{left, right};
					if (content[from].type == Lexema.LexType.EOF) {
						return from;
					}
					else {
						throw new SyntaxException(0, from, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNPARSED_TAIL));
					}
				}
				else {
					throw new SyntaxException(0, from, "Ergo sign is missing");
				}
			case Left	:
				final List<SyntaxNode<NodeType, SyntaxNode>>	leftContent = new ArrayList<>();
				SyntaxNode<NodeType, SyntaxNode>				leftItem;
				
leftLoop:		for(;;) {
					switch (content[from].type) {
						case Char : case ExtendedMarker : case Keyword : case ListMarker : case RegularMarker : case RestrictedMarker : case WildMarker :
							leftItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
							leftItem.type = NodeType.Mandatory;
							leftItem.cargo = content[from];
							leftContent.add(leftItem);
							break;
						case Continuation		:
							break;
						case OpenB				:
							final List<SyntaxNode<NodeType, SyntaxNode>>	leftOptional = new ArrayList<>();
							
							do {
								leftItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
								from = buildTree(content, from + 1, Level.Left, leftItem);
								if (content[from].type != Lexema.LexType.CloseB) {
									throw new SyntaxException(0, from, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_MISSING_CLOSE_SQUARE_BRACKET)); 
								}
								else {
									leftOptional.add(leftItem);
									from++;
								}
							} while (content[from].type == Lexema.LexType.OpenB);
							leftItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
							leftItem.type = NodeType.Optional;
							leftItem.children = leftOptional.toArray(new SyntaxNode[leftOptional.size()]);
							leftItem.cargo = null;
							leftContent.add(leftItem);
							from--;
							break;
						default :
							break leftLoop;
					}
					from++;
				}
				root.type = NodeType.Sequence;
				root.children = leftContent.toArray(new SyntaxNode[leftContent.size()]);
				break;
			case Right	:
				final List<SyntaxNode<NodeType, SyntaxNode>>	rightContent = new ArrayList<>();
				SyntaxNode<NodeType, SyntaxNode>				rightItem;
				
rightLoop:		for(;;) {
					switch (content[from].type) {
						case Char : case Keyword : case RegularResultMarker : case DumbResultMarker : case NormalResultMarker : case SmartResultMarker : case BlockResultMarker : case BoolResultMarker : case Continuation :
							rightItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
							rightItem.type = NodeType.Mandatory;
							rightItem.cargo = content[from];
							rightContent.add(rightItem);
							break;
						case OpenB				:
							final List<SyntaxNode<NodeType, SyntaxNode>>	rightOptional = new ArrayList<>();
							
							do {
								rightItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
								from = buildTree(content, from + 1, Level.Right, rightItem);
								if (content[from].type != Lexema.LexType.CloseB) {
									throw new SyntaxException(0, from, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_MISSING_CLOSE_SQUARE_BRACKET)); 
								}
								else {
									rightItem.type = NodeType.Optional;
									rightOptional.add(rightItem);
								}
							} while (content[from].type == Lexema.LexType.OpenB);
							rightItem = (SyntaxNode<NodeType, SyntaxNode>) root.clone();
							rightItem.type = NodeType.Optional;
							rightItem.children = rightOptional.toArray(new SyntaxNode[rightOptional.size()]);
							rightContent.add(rightItem);
							break;
						default :
							break rightLoop;
					}
					from++;
				}
				root.type = NodeType.Sequence;
				root.children = rightContent.toArray(new SyntaxNode[rightContent.size()]);
				break;
			default 	:
				throw new UnsupportedOperationException("Level type ["+level+"] is not supported yet"); 
		}
		return from;
	}
	
	static String printTree(final SyntaxNode<NodeType, SyntaxNode> root) {
		final StringBuilder	sb = new StringBuilder();
		
		printTree(root, sb);
		return sb.toString();
	}
	
	private static void printTree(final SyntaxNode<NodeType, SyntaxNode> root, final StringBuilder sb) {
		switch (root.getType()) {
			case Mandatory	:
				sb.append(' ').append(root.cargo).append(' ');
				break;
			case Optional	:
				String	prefix = " [ ";
				for (SyntaxNode<NodeType, SyntaxNode> item : root.children) {
					sb.append(prefix);
					printTree(item,sb);
					prefix = " |,| ";
				}
				sb.append(" ] ");
				break;
			case Sequence	:
				for (SyntaxNode<NodeType, SyntaxNode> item : root.children) {
					printTree(item,sb);
				}
				break;
			case Root		:
				printTree(root.children[0], sb);
				sb.append(" => ");
				printTree(root.children[1], sb);
				break;
			default 		:	
				sb.append("?????");
				break;
		}
	}
	
	static enum NodeType {
		Mandatory, Optional, Sequence, Root
	}

	static enum Level {
		Top, Left, Right
	}
	
	static class Lexema {
		public static enum LexType {
			EOF, Keyword, Char, OpenB, CloseB, Ergo, Continuation,  
			RegularMarker, ListMarker, RestrictedMarker, WildMarker, ExtendedMarker,
			RegularResultMarker, DumbResultMarker, NormalResultMarker, SmartResultMarker, BlockResultMarker, BoolResultMarker 
		}
		
		private final LexType	type;
		private final long		entityId;
		private final char[]	sequence;
		private final long[]	associations;
		
		public Lexema(LexType type) {
			this(type, -1, null);
		}

		public Lexema(LexType type, long entityId) {
			this(type, entityId, null);
		}
		
		public Lexema(LexType type, long entityId, long[] associations) {
			this.type = type;
			this.entityId = entityId;
			this.sequence = null;
			this.associations = associations;
		}

		public Lexema(LexType type, char[] sequence) {
			this.type = type;
			this.sequence = sequence;
			this.entityId = -1;
			this.associations = null;
		}
		
		public LexType getType() {
			return type;
		}
		
		public long getEntityId() {
			return entityId;
		}
		
		public char[] getSequence() {
			return sequence;
		}
		
		public long[] getAssociations() {
			return associations;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(associations);
			result = prime * result + (int) (entityId ^ (entityId >>> 32));
			result = prime * result + Arrays.hashCode(sequence);
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Lexema other = (Lexema) obj;
			if (!Arrays.equals(associations, other.associations)) return false;
			if (entityId != other.entityId) return false;
			if (!Arrays.equals(sequence, other.sequence)) return false;
			if (type != other.type) return false;
			return true;
		}

		@Override
		public String toString() {
			return "Lexema [type=" + type + ", entityId=" + entityId + ", sequence=" + Arrays.toString(sequence) + ", associations=" + Arrays.toString(associations) + "]";
		}
	}
	
	private static class PatternAndSubstitutorImpl implements PatternAndProcessor {
		private final SyntaxTreeInterface<Long> 		names;
		private final SyntaxNode<NodeType, SyntaxNode>	root;
		private final int								markerCount;
		
		private PatternAndSubstitutorImpl(final SyntaxTreeInterface<Long> names, final SyntaxNode<NodeType, SyntaxNode> root, final int markerCount) {
			this.names = names;
			this.root = root;
			this.markerCount = markerCount;
		}

		@Override
		public char[] getKeyword() {
			SyntaxNode	node = root;
			
			while (node.children != null && node.children.length > 0) {
				node = node.children[0];
			}
			return names.getName(((Lexema)node.cargo).entityId).toCharArray();
		}

		@Override
		public int process(final char[] data, final int from, final OutputProcessor writer) throws SyntaxException {
			final int[]		ranges = new int[2];
			final int[][]	markers = new int[markerCount][2]; 
			
			if (identify(data, from, names, root.children[0], ranges, markers)) {
				final int	result = ranges[0]; 
				
				upload(data, root.children[1], ranges, markers, writer);
				return result;
			}
			else {
				return from;
			}
		}
		
	}
}
