package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.i18n.PureLibLocalizer;

class LuceneStyledMatcher {
	static final char		EOF = '\0';
	private static final ReservedWords[]	WORDS = {
													new ReservedWords("TO", LexType.TO_WORD), 
													new ReservedWords("AND", LexType.AND_WORD), 
													new ReservedWords("OR", LexType.OR_WORD), 
													new ReservedWords("NOT", LexType.NOT_WORD), 
												}; 
	
	static Predicate<Function<String,String>> parse(final String pattern) throws SyntaxException {
		if (pattern == null) {
			throw new NullPointerException("Pattern can't be null");
		}
		else {
			final char[]		content = CharUtils.terminateAndConvert2CharArray(pattern, EOF);
			final List<Lexema>	lexList = new ArrayList<>();

			parse(content, 0, lexList);

			final SyntaxNode<NodeType, SyntaxNode<?,?>>	root = new SyntaxNode<>(0, 0, NodeType.ROOT, 0, null);
			final Lexema[]		lexemas = lexList.toArray(new Lexema[lexList.size()]); 
			final int			stop = buildTree(lexemas, 0, root);
			
			if (lexemas[stop].type != LexType.EOF) {
				throw new SyntaxException(0, lexemas[stop].pos, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNPARSED_TAIL));
			}
			else {
				return null;
			}
		}
	}
	
	static void parse(final char[] source, int from, final List<Lexema> target) throws SyntaxException {
		final StringBuilder	sb = new StringBuilder();
		final long[]		forLongs = new long[2];
		
loop:	for(;;) {
			while (source[from] <= ' ' && source[from] != EOF) {
				from++;
			}
			final int	start = from;
			
			switch (source[from]) {
				case EOF 	:
					target.add(new Lexema(start, LexType.EOF));
					break loop;
				case ':'	:
					target.add(new Lexema(start, LexType.COLON));
					from++;
					break;
				case '~'	:
					target.add(new Lexema(start, LexType.FUZZY));
					from++;
					break;
				case '('	:
					target.add(new Lexema(start, LexType.OPEN));
					from++;
					break;
				case ')'	:
					target.add(new Lexema(start, LexType.CLOSE));
					from++;
					break;
				case '['	:
					target.add(new Lexema(start, LexType.OPENB));
					from++;
					break;
				case ']'	:
					target.add(new Lexema(start, LexType.CLOSEB));
					from++;
					break;
				case '{'	:
					target.add(new Lexema(start, LexType.OPENF));
					from++;
					break;
				case '}'	:
					target.add(new Lexema(start, LexType.CLOSEF));
					from++;
					break;
				case '^'	:
					target.add(new Lexema(start, LexType.BOOST));
					from++;
					break;
				case '+'	:
					target.add(new Lexema(start, LexType.PLUS));
					from++;
					break;
				case '-'	:
					target.add(new Lexema(start, LexType.MINUS));
					from++;
					break;
				case '\\'	:
					if (source[from+1] != EOF) {
						from+= 2;
					}
					else {
						throw new SyntaxException(0, start, "end of data after escape");
					}
					break;
				case '\"'	:
					try {
						from = CharUtils.parseStringExtended(source, start+1, '\"', sb);
						target.add(new Lexema(start, LexType.PHRASE, sb.toString().toCharArray()));
					} catch (IllegalArgumentException exc) {
						throw new SyntaxException(0, start, "unpaired quotas");
					}
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					from = CharUtils.parseNumber(source, from, forLongs, CharUtils.PREF_INT | CharUtils.PREF_FLOAT| CharUtils.PREF_DOUBLE, true);
					switch ((int)forLongs[1]) {
						case CharUtils.PREF_INT 	:
							target.add(new Lexema(start, (int)forLongs[0]));
							break;
						case CharUtils.PREF_FLOAT	:
							target.add(new Lexema(start, Float.intBitsToFloat((int)forLongs[0])));
							break;
						case CharUtils.PREF_DOUBLE	:
							target.add(new Lexema(start, (float)Double.longBitsToDouble(forLongs[0])));
							break;
						default :
					}
					break;
				default :
					if (Character.isLetter(source[from])) {
						boolean	isWildcard = false;
						int		end = from;
						
						while (Character.isLetter(source[end]) || source[end] == '*' || source[end] == '?' || source[end] == '\\') {
							if (source[end] == '*' || source[end] == '?') {
								isWildcard = true;
							}
							else if (source[end] == '\\') {
								end++;
							}
							end++;
						}
						from = end;
						if (isWildcard) {
							target.add(new Lexema(start, LexType.WILDCARD_TERM, Arrays.copyOfRange(source, start, end)));
						}
						else {
							for (ReservedWords word : WORDS) {
								if (CharUtils.compareIgnoreCase(source, start, word.word)) { 
									target.add(new Lexema(start, word.type));
									continue loop;
								}
							}
							target.add(new Lexema(start, LexType.SINGLE_TERM, Arrays.copyOfRange(source, start, end)));
						}
					}
					else {
						throw new SyntaxException(0, start, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNKNOWN_LEXEMA));
					}
					break;
			}
		}
	}

	static int buildTree(final Lexema[] source, final int from, final SyntaxNode<NodeType, SyntaxNode<?, ?>> root) throws SyntaxException {
		return buildTree(Depth.OR, source, from, root);
	}
	
	static Lemma[] lemmatize(final CharSequence seq) {
		final List<Lemma>	lemmas = new ArrayList<>(); 
		int		from = 0, to = seq.length();
	
loop:	for(;;) {
			while (from < to && seq.charAt(from) <= ' ') {
				from++;
			}
			if (from >= to) {
				lemmas.add(new Lemma(from, LemmaType.EOF, 0));
				break loop;
			}
			final int	start = from;
			
			if (Character.isLetter(seq.charAt(start))) {
				int	end = start;
				
				while (end < to && Character.isLetter(seq.charAt(end))) {
					end++;
				}
				lemmas.add(new Lemma(end, LemmaType.WORD, extract(seq, start, end)));
				from = end;
			}
			else if (seq.charAt(start) >= '0' && seq.charAt(start) <= '9') {
				boolean	wasDot = false; 
				int		end = start;

				while (end < to && seq.charAt(end) > ' ' && (seq.charAt(end) >= '0' && seq.charAt(end) <= '9') || seq.charAt(end) == '.') {
					if (seq.charAt(end) == '.') {
						wasDot = true;
					}
					end++;
				}
				final char[]	val = extract(seq, start, end);

				from = end;
				if (wasDot) {
					try {
						lemmas.add(new Lemma(end, LemmaType.FLOAT, Float.parseFloat(new String(val))));
					} catch (NumberFormatException exc) {
						try {
							lemmas.add(new Lemma(end, LemmaType.INTEGER, Integer.parseInt(new String(val))));
						} catch (NumberFormatException exc2) {
							lemmas.add(new Lemma(end, LemmaType.PUNCT, val));
						}
					}
				}
				else {
					try {
						lemmas.add(new Lemma(end, LemmaType.INTEGER, Integer.parseInt(new String(val))));
					} catch (NumberFormatException exc2) {
						lemmas.add(new Lemma(end, LemmaType.PUNCT, val));
					}
				}
			}
			else {
				int	end = start;

				while (end < to && seq.charAt(end) > ' ' && !Character.isLetterOrDigit(seq.charAt(end))) {
					end++;
				}
				lemmas.add(new Lemma(end, LemmaType.PUNCT, extract(seq, start, end)));
				from = end;
			}
		}
		return lemmas.toArray(new Lemma[lemmas.size()]);
	}
	
	private static char[] extract(final CharSequence seq, final int start, final int end) {
		final char[]	result = new char[end-start];
		
		for(int index = start; index < end; index++) {
			result[index-start] = seq.charAt(index);
		}
		return result;
	}

	private static int buildTree(final Depth depth, final Lexema[] source, int from, final SyntaxNode<NodeType, SyntaxNode<?, ?>> root) throws SyntaxException {
		// TODO Auto-generated method stub
		final int	pos = source[from].pos;
		
		switch (depth) {
			case OR			:
				from = buildTree(Depth.AND, source, from, root);
				if (source[from].type == LexType.OR_WORD) {
					final List<SyntaxNode<NodeType, SyntaxNode<?, ?>>>	list = new ArrayList<>();
					SyntaxNode<NodeType, SyntaxNode<?, ?>>	right = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
					
					list.add(right);
					do {
						right = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
						from = buildTree(Depth.AND, source, from + 1, right);
						list.add(right);
					} while (source[from].type == LexType.OR_WORD);
					root.type = NodeType.OR;
					root.children = list.toArray(new SyntaxNode[list.size()]);
				}
				break;
			case AND		:
				from = buildTree(Depth.NOT, source, from, root);
				if (source[from].type == LexType.AND_WORD) {
					final List<SyntaxNode<NodeType, SyntaxNode<?, ?>>>	list = new ArrayList<>();
					SyntaxNode<NodeType, SyntaxNode<?, ?>>	right = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
					
					list.add(right);
					do {
						right = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
						from = buildTree(Depth.NOT, source, from + 1, right);
						list.add(right);
					} while (source[from].type == LexType.AND_WORD);
					root.type = NodeType.AND;
					root.children = list.toArray(new SyntaxNode[list.size()]);
				}
				break;
			case NOT		:
				if (source[from].type == LexType.NOT_WORD) {
					final SyntaxNode<NodeType, SyntaxNode<?, ?>>	notValue = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
					
					from = buildTree(Depth.SEQUENCE, source, from + 1, notValue);
					root.type = NodeType.NOT;
					root.children = new SyntaxNode[] {notValue};
				}
				else {
					from = buildTree(Depth.SEQUENCE, source, from, root);
				}
				break;
			case SEQUENCE	:
				final List<SyntaxNode<NodeType, SyntaxNode<?, ?>>>	list = new ArrayList<>();

				do {
					final SyntaxNode<NodeType, SyntaxNode<?, ?>>	value = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone(); 
					
					from = buildTree(Depth.COMPARE, source, from, value);
					list.add(value);
				} while (source[from].type != LexType.EOF && source[from].type != LexType.AND_WORD && source[from].type != LexType.OR_WORD && source[from].type != LexType.CLOSE);
				root.type = NodeType.SEQUENCE;
				root.children = list.toArray(new SyntaxNode[list.size()]);
				root.row = 0;
				root.col = pos;
				break;
			case COMPARE	:
				boolean	leftRange = false, rightRange = false;
				
				switch (source[from].type) {
					case OPENB	:
						leftRange = true;
					case OPENF	:
						final SyntaxNode<NodeType, SyntaxNode<?, ?>>	leftValue = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone(); 
						final SyntaxNode<NodeType, SyntaxNode<?, ?>>	rightValue = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
						
						from = buildTree(Depth.TERM, source, from + 1, leftValue);
						if (source[from].type == LexType.TO_WORD) {
							from = buildTree(Depth.TERM, source, from + 1, rightValue);
							if (source[from].type == LexType.CLOSEB) {
								rightRange = true;
							}
							else if (source[from].type != LexType.CLOSEF) {
								throw new SyntaxException(0, source[from].pos, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, leftRange ? SyntaxException.SE_MISSING_CLOSE_SQUARE_BRACKET : SyntaxException.SE_MISSING_CLOSE_FIGURE_BRACKET));
							}
							root.type = NodeType.RANGE_MATCHES;
							root.children = new SyntaxNode[] {leftValue, rightValue};
							root.value = (leftRange ? 2 : 0) + (rightRange ? 1 : 0); 
							root.row = 0;
							root.col = pos;
							from++;
						}
						else {
							throw new SyntaxException(0, source[from].pos, "Missing TO word");
						}
						break;
					default :
						from = buildTree(Depth.PREFIX, source, from, root);
				}
				break;
			case PREFIX		:
				switch (source[from].type) {
					case PLUS 	:
						final SyntaxNode<NodeType, SyntaxNode<?, ?>>	plusValue = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone(); 
						
						from = buildTree(Depth.POSTFIX, source, from + 1, plusValue);
						root.type = NodeType.MANDATORY;
						root.children = new SyntaxNode[] {plusValue};
						root.row = 0;
						root.col = pos;
						break;
					case MINUS	:
						final SyntaxNode<NodeType, SyntaxNode<?, ?>>	minusValue = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
						
						from = buildTree(Depth.POSTFIX, source, from + 1, minusValue);
						root.type = NodeType.PROHIBITED;
						root.children = new SyntaxNode[] {minusValue};
						root.row = 0;
						root.col = pos;
						break;
					default :
						from = buildTree(Depth.POSTFIX, source, from, root);
				}
				break;
			case POSTFIX	:
				from = buildTree(Depth.TERM, source, from, root);
				switch (source[from].type) {
					case BOOST:
						if (source[from+1].type == LexType.INTEGER) {
							final SyntaxNode<NodeType, SyntaxNode<?, ?>>	boostValue = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
							
							root.type = NodeType.BOOST;
							root.value = source[from+1].valueN; 
							root.children = new SyntaxNode[] {boostValue};
							root.row = 0;
							root.col = source[from].pos;
							from += 2;
						}
						else {
							throw new SyntaxException(0, source[from+1].pos, "Missing integer");
						}
						break;
					case FUZZY:
						if (source[from+1].type == LexType.INTEGER) {
							final SyntaxNode<NodeType, SyntaxNode<?, ?>>	boostValue = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
							
							root.type = NodeType.PROXIMITY_MATCHES;
							root.value = source[from+1].valueN; 
							root.children = new SyntaxNode[] {boostValue};
							root.row = 0;
							root.col = source[from].pos;
							from += 2;
						}
						else if (source[from+1].type == LexType.FLOAT) {
							final SyntaxNode<NodeType, SyntaxNode<?, ?>>	boostValue = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
							
							root.type = NodeType.FUSSY_MATCHES;
							root.value = source[from+1].valueN; 
							root.children = new SyntaxNode[] {boostValue};
							root.row = 0;
							root.col = source[from].pos;							
							from += 2;
						}
						else {
							final SyntaxNode<NodeType, SyntaxNode<?, ?>>	boostValue = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
							
							root.type = NodeType.FUSSY_MATCHES;
							root.value = Float.floatToIntBits(1.0f); 
							root.children = new SyntaxNode[] {boostValue};
							root.row = 0;
							root.col = source[from].pos;
							from++;
						}
						break;
					default:
						break;				
				}
				break;
			case TERM	:
				final SyntaxNode<NodeType, SyntaxNode<?, ?>>	termValue = (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.clone();
				final String	fieldName;
				
				if (source[from+1].type == LexType.COLON) {
					fieldName = new String(source[from].valueC);
					from += 2;
				}
				else {
					fieldName = null;
				}
				switch (source[from].type) {
					case OPEN	:
						from = buildTree(Depth.OR, source, from + 1, termValue);
						if (source[from].type == LexType.CLOSE) {
							from++;
						}
						else {
							throw new SyntaxException(0, source[from].pos, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_MISSING_CLOSE_BRACKET));							
						}
						break;
					case FLOAT			:
						termValue.type = NodeType.EQUALS;
						termValue.cargo = Float.valueOf(Float.intBitsToFloat(source[from].valueN));
						termValue.row = 0;
						termValue.col = source[from].pos;
						from++;
						break;
					case INTEGER		:
						termValue.type = NodeType.EQUALS;
						termValue.cargo = source[from].valueN;
						termValue.row = 0;
						termValue.col = source[from].pos;
						from++;
						break;
					case PHRASE			:
						termValue.type = NodeType.PHRASE_EQUALS;
						termValue.cargo = source[from].valueC;
						termValue.row = 0;
						termValue.col = source[from].pos;
						from++;
						break;
					case SINGLE_TERM	:
						termValue.type = NodeType.EQUALS;
						termValue.cargo = source[from].valueC;
						termValue.row = 0;
						termValue.col = source[from].pos;
						from++;
						break;
					case WILDCARD_TERM	:
						termValue.type = NodeType.MATCHES;
						termValue.cargo = source[from].valueC;
						termValue.row = 0;
						termValue.col = source[from].pos;
						from++;
						break;
					default	:
						throw new SyntaxException(0, source[from].pos, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_MISSING_OPERAND));
				}
				root.type = NodeType.EXTRACT;
				root.cargo = fieldName;
				root.children = new SyntaxNode[] {termValue};
				root.row = 0;
				root.col = pos;
				break;
			default :
				throw new UnsupportedOperationException("Depth ["+depth+"] is not supported yet");
		}
		return from;
	}
	
	static boolean match(final CharSequence	seq, final SyntaxNode<NodeType, SyntaxNode<?, ?>> root) {
		final Lemma[]	lemmas = lemmatize(seq);
		
		return match(lemmas, 0, root) == lemmas.length;
	}

	static int match(final Lemma[] source, int from, final SyntaxNode<NodeType, SyntaxNode<?, ?>> root) {
		switch (root.type) {
			case AND			:
				break;
			case BOOST			:
				break;
			case EQUALS			:
				break;
			case EXTRACT		:
				break;
			case FUSSY_MATCHES	:
				break;
			case MANDATORY		:
				break;
			case MATCHES		:
				break;
			case NOT			:
				break;
			case OR				:
				break;
			case PHRASE_EQUALS	:
				break;
			case PROHIBITED		:
				break;
			case PROXIMITY_MATCHES:
				break;
			case RANGE_MATCHES	:
				break;
			case SEQUENCE		:
				break;
			default:
				throw new UnsupportedOperationException("Root node type ["+root.type+"] is not supported yet");
		}
		
		return 0;
	}
	
	
	static enum LemmaType {
		WORD,
		INTEGER,
		FLOAT,
		PUNCT,
		EOF
	}	
	
	static class Lemma {
		final int		pos;
		final LemmaType	type;
		final int		valueN;
		final char[]	valueS;

		public Lemma(final int pos, final LemmaType type, final int value) {
			this(pos, type, value, null);
		}

		public Lemma(final int pos, final LemmaType type, final float value) {
			this(pos, type, Float.floatToIntBits(value), null);
		}
		
		public Lemma(final int pos, final LemmaType type, final char[] value) {
			this(pos, type, 0, value);
		}
		
		public Lemma(final int pos, final LemmaType type, final int valueN, final char[] valueS) {
			this.pos = pos;
			this.type = type;
			this.valueN = valueN;
			this.valueS = valueS;
		}

		@Override
		public String toString() {
			return "Lemma [pos=" + pos + ", type=" + type + ", valueN=" + valueN + ", valueS=" + Arrays.toString(valueS) + "]";
		}
	}
	
	static enum LexType {
		SINGLE_TERM,
		WILDCARD_TERM,
		PHRASE,
		INTEGER,
		FLOAT,
		COLON,
		FUZZY,
		OPEN,
		OPENB,
		OPENF,
		CLOSE,
		CLOSEB,
		CLOSEF,
		BOOST,
		TO_WORD,
		OR_WORD,
		AND_WORD,
		NOT_WORD,
		PLUS,
		MINUS,
		EOF
	}
	
	static class Lexema {
		final int		pos;
		final LexType	type;
		final int		valueN;
		final char[]	valueC;

		Lexema(final int pos, final LexType type) {
			this(pos, type, 0, null);
		}

		Lexema(final int pos, final int value) {
			this(pos, LexType.INTEGER, value, null);
		}

		Lexema(final int pos, final float value) {
			this(pos, LexType.FLOAT, Float.floatToIntBits(value), null);
		}

		Lexema(final int pos, final LexType type, final char[] value) {
			this(pos, type, 0, value);
		}
		
		Lexema(final int pos, final LexType type, final int valueN, final char[] valueC) {
			this.pos = pos;
			this.type = type;
			this.valueN = valueN;
			this.valueC = valueC;
		}

		@Override
		public String toString() {
			return "Lexema [pos=" + pos + ", type=" + type + ", valueN=" + valueN + ", valueC=" + Arrays.toString(valueC) + "]";
		}
	}

	static enum NodeType {
		EXTRACT,
		EQUALS,
		PHRASE_EQUALS,
		MANDATORY,
		PROHIBITED,
		BOOST,
		MATCHES,
		FUSSY_MATCHES,
		PROXIMITY_MATCHES,
		RANGE_MATCHES,
		SEQUENCE,
		OR,
		AND,
		NOT,
		ROOT;
	}
	
	private static class ReservedWords {
		final char[]	word;
		final LexType	type;
		
		private ReservedWords(String word, LexType type) {
			this.word = word.toCharArray();
			this.type = type;
		}
	}
	
	private static enum Depth {
		OR,
		AND,
		NOT,
		COMPARE,
		SEQUENCE,
		PREFIX,
		POSTFIX,
		TERM
	}
	
	private static class Content implements Cloneable {
		final Lemma[]	lemmas;
		int				pos;
		
		private Content(final Lemma[] lemmas) {
			this.lemmas = lemmas;
			this.pos = 0;
		}

		@Override
		public Content clone() throws CloneNotSupportedException {
			return (Content) super.clone();
		}
		
		@Override
		public String toString() {
			return "Content [lemmas=" + Arrays.toString(lemmas) + ", pos=" + pos + "]";
		}
	}
}
