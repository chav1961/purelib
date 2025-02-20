package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Pattern;

import chav1961.purelib.basic.CharUtils.Prescription;
import chav1961.purelib.basic.CharUtils.RelevanceFunction;
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
	
	static RelevanceFunction parse(final CharSequence pattern) throws SyntaxException {
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
				return new RelevanceFunction() {
					@Override
					public int test(final Function<String, CharSequence> callback) {
						final int[]	relevance = new int[1];
						
						if (match(root, callback, relevance)) {
							return relevance[0];
						}
						else {
							return 0;
						}
					}
				};
			}
		}
	}
	
	static void parse(final char[] source, int from, final List<Lexema> target) throws SyntaxException {
		final StringBuilder	sb = new StringBuilder();
		final float[]	forFloats = new float[1];
		final int[]		forInts = new int[1];
		final long[]	forLongs = new long[2];
		
loop:	for(;;) {
			final int	start = from = skipBlank(source, from);
			
			switch (source[from]) {
				case EOF 	:
					target.add(new Lexema(start, LexType.EOF));
					break loop;
				case ':'	:
					target.add(new Lexema(start, LexType.COLON));
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
						final int	proximity;
						
						from = skipBlank(source, CharUtils.parseStringExtended(source, start + 1, '\"', sb));
						if (source[from] == '~') {
							from = CharUtils.parseInt(source, skipBlank(source, from + 1), forInts, true);
							proximity = forInts[0];
						}
						else {
							proximity = 1;
						}
						target.add(new Lexema(start, LexType.PHRASE, proximity, lemmatize(sb)));
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
							target.add(new Lexema(start, LexType.WILDCARD_TERM, Arrays.copyOfRange(source, start, end), 1.0f));
						}
						else {
							for (ReservedWords word : WORDS) {
								if (CharUtils.compareIgnoreCase(source, start, word.word)) { 
									target.add(new Lexema(start, word.type));
									continue loop;
								}
							}
							from = skipBlank(source, from);
							if (source[from] == '~') {
								from = skipBlank(source, from + 1);
								final float	fuzzy;
								
								if (Character.isDigit(source[from])) {
									from = CharUtils.parseFloat(source, from, forFloats, true);
									fuzzy = forFloats[0];
								}
								else {
									fuzzy = 0.5f;
								}
								target.add(new Lexema(start, LexType.SINGLE_TERM, Arrays.copyOfRange(source, start, end), fuzzy));
							}
							else {
								target.add(new Lexema(start, LexType.SINGLE_TERM, Arrays.copyOfRange(source, start, end), 1.0f));
							}
						}
					}
					else {
						throw new SyntaxException(0, start, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_UNKNOWN_LEXEMA));
					}
					break;
			}
		}
	}

	private static int skipBlank(final char[] source, int from) {
		while (source[from] <= ' ' && source[from] != EOF) {
			from++;
		}
		return from;
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
						
						from = buildTerm(source, from + 1, leftValue);
						if (source[from].type == LexType.TO_WORD) {
							from = buildTerm(source, from + 1, rightValue);
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
						termValue.value = source[from].valueN;
						termValue.cargo = source[from].valueL;
						termValue.row = 0;
						termValue.col = source[from].pos;
						from++;
						break;
					case SINGLE_TERM	:
						termValue.type = NodeType.EQUALS;
						termValue.value = source[from].valueN;
						termValue.cargo = source[from].valueC;
						termValue.row = 0;
						termValue.col = source[from].pos;
						from++;
						break;
					case WILDCARD_TERM	:
						termValue.type = NodeType.MATCHES;
						termValue.cargo = Pattern.compile(Utils.fileMask2Regex(new String(source[from].valueC)));
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
	
	private static int buildTerm(final Lexema[] source, int from, final SyntaxNode<NodeType, SyntaxNode<?, ?>> root) throws SyntaxException {
		switch (source[from].type) {
			case FLOAT			:
				root.type = NodeType.EQUALS;
				root.cargo = Float.valueOf(Float.intBitsToFloat(source[from].valueN));
				root.row = 0;
				root.col = source[from].pos;
				from++;
				break;
			case INTEGER		:
				root.type = NodeType.EQUALS;
				root.cargo = source[from].valueN;
				root.row = 0;
				root.col = source[from].pos;
				from++;
				break;
			case SINGLE_TERM	:
				root.type = NodeType.EQUALS;
				root.value = source[from].valueN;
				root.cargo = source[from].valueC;
				root.row = 0;
				root.col = source[from].pos;
				from++;
				break;
			default	:
				throw new SyntaxException(0, source[from].pos, URIUtils.appendFragment2URI(PureLibLocalizer.LOCALIZER_SCHEME_URI, SyntaxException.SE_MISSING_OPERAND));
		}
		return from;
	}

	static boolean match(final SyntaxNode<NodeType, SyntaxNode<?, ?>> root, final CharSequence seq, final int[] relevance) {
		return match(root, (s)->seq, relevance);
	}	
	
	static boolean match(final SyntaxNode<NodeType, SyntaxNode<?, ?>> root, final Function<String, CharSequence> callback, final int[] relevance) {
		final ContentRepo	repo = new ContentRepo(callback);
		
		return match(repo.get(null).lemmas, 0, root, repo, relevance) == TestResult.TRUE;
	}	
	
	private static TestResult match(final Lemma[] lemmas, int from, final SyntaxNode<NodeType, SyntaxNode<?, ?>> root, final ContentRepo repo, final int[] relevance) {
		TestResult	result;
		
		switch (root.type) {
			case AND			:
				for(SyntaxNode<?, ?> item : root.children) {
					if ((result = match(lemmas, from, (SyntaxNode<NodeType, SyntaxNode<?, ?>>) item, repo, relevance)) != TestResult.TRUE) {
						return result; 
					}
				}
				return TestResult.TRUE;
			case BOOST			:
				if ((result = match(lemmas, from, (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.children[0], repo, relevance)) == TestResult.TRUE) {
					relevance[0] += root.value; 
				}
				return result; 
			case EQUALS			:
				return equals(lemmas[from], root.cargo, Float.intBitsToFloat((int)root.value)) ? TestResult.TRUE : TestResult.FALSE;
			case EXTRACT		:
				if (root.cargo == null) {
					return match(lemmas, from, (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.children[0], repo, relevance); 
				}
				else {
					return match(repo.get(root.cargo.toString()).lemmas, 0, (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.children[0], repo, relevance);
				}
			case MANDATORY		:
				for (int index = 0; index < lemmas.length; index++) {
					if (match(lemmas, index, (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.children[0], repo, relevance) == TestResult.TRUE) {
						return TestResult.TRUE;
					}
				}
				return TestResult.ULTIMATE_FALSE;
			case MATCHES		:
				return ((Pattern)root.cargo).matcher(CharUtils.toCharSequence(lemmas[from].valueS, 0, lemmas[from].valueS.length - 1)).matches() ? TestResult.TRUE : TestResult.FALSE;
			case NOT			:
				switch (result = match(lemmas, from, (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.children[0], repo, relevance)) {
					case FALSE	:
						return TestResult.TRUE;
					case TRUE	:
						return TestResult.FALSE;
					case ULTIMATE_FALSE:
						return TestResult.ULTIMATE_FALSE;
					default :
						throw new UnsupportedOperationException("Result type ["+result+"] is not supported yet");
				}
			case PHRASE_EQUALS	:
				return phraseEquals(lemmas, from, (Lemma[])root.cargo, 0, (int)root.value) ? TestResult.TRUE : TestResult.FALSE;
			case PROHIBITED		:
				if ((result = match(lemmas, from, (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.children[0], repo, relevance)) == TestResult.TRUE) { 
					return TestResult.ULTIMATE_FALSE;
				}
				else {
					return TestResult.TRUE;
				}
			case RANGE_MATCHES	:
				return between(lemmas[from], root.children[0].cargo, (root.value & 0x02) != 0, root.children[1].cargo, (root.value & 0x01) != 0) ? TestResult.TRUE : TestResult.FALSE;
			case OR : case SEQUENCE :
				for(int seqIndex = 0; seqIndex < root.children.length; seqIndex++) {
					boolean	found = false;
					
					for(int index = 0; index < lemmas.length-1; index++) {
						switch (result = match(lemmas, index, (SyntaxNode<NodeType, SyntaxNode<?, ?>>) root.children[seqIndex], repo, relevance)) {
							case FALSE	:
								break;
							case TRUE	:
								relevance[0]++;
								found = true;
								break;
							case ULTIMATE_FALSE:
								return TestResult.ULTIMATE_FALSE;
							default:
								throw new UnsupportedOperationException("Result type ["+result+"] is not supported yet");
						}
					}
					if (!found) {
						return TestResult.FALSE;
					}
				}
				return TestResult.TRUE;
			default:
				throw new UnsupportedOperationException("Root node type ["+root.type+"] is not supported yet");
		}
	}

	private static boolean phraseEquals(final Lemma[] lemmas, final int from, final Lemma[] template, final int fromT, final int distance) {
		if (from <= lemmas.length && lemmas[from].compareTo(template[fromT]) == 0) {
			if (fromT >= template.length - 1) {
				return true;
			}
			else {
				for(int index = 1; index <= distance; index++) {
					if (phraseEquals(lemmas, from + index, template, fromT + 1, distance)) {
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
	
	private static boolean equals(final Lemma content, final Object value, final float fuzzy) {
		if ((value instanceof Float) && content.type == LemmaType.FLOAT) {
			return Math.abs(((Float)value).floatValue() - Float.intBitsToFloat((int)content.valueN)) < 0.001; 
		}
		else if ((value instanceof Integer) && content.type == LemmaType.INTEGER) {
			return ((Integer)value).longValue() == content.valueN;
		}
		else if ((value instanceof char[]) && content.type == LemmaType.WORD) {
			if (CharUtils.compare(content.valueS, 0, (char[])value)) {
				return true;
			}
			else if (fuzzy != 1.0f) {
				final Prescription 	pre = CharUtils.calcLevenstain(content.valueS, (char[])value);
				
				return 1.0 * pre.distance / content.valueS.length <= (1 - fuzzy);
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	private static boolean between(final Lemma content, final Object minValue, final boolean minInclusive, final Object maxValue, final boolean maxInclusive) {
		if ((minValue instanceof Float) && content.type == LemmaType.FLOAT) {
			final float	val = Float.intBitsToFloat((int)content.valueN);
			
			return (val > ((Float)minValue).floatValue() || minInclusive && equals(content, minValue, 1.0f))
					&&
				   (val < ((Float)maxValue).floatValue() || maxInclusive && equals(content, maxValue, 1.0f)); 
		}
		else if ((minValue instanceof Integer) && content.type == LemmaType.INTEGER) {
			final long	val = content.valueN;
			
			return (val > ((Integer)minValue).longValue() || minInclusive && equals(content, minValue, 1.0f))
					&&
				   (val < ((Integer)maxValue).longValue() || maxInclusive && equals(content, maxValue, 1.0f)); 
		}
		else if ((minValue instanceof char[]) && content.type == LemmaType.WORD) {
			final char[]	val = content.valueS;
			
			return (CharUtils.compareTo(val, (char[])minValue) > 0 || minInclusive && equals(content, minValue, 1.0f))
					&&
				   (CharUtils.compareTo(val, (char[])maxValue) < 0  || maxInclusive && equals(content, maxValue, 1.0f)); 
		}
		else {
			return false;
		}
	}
	
	static enum LemmaType {
		WORD,
		INTEGER,
		FLOAT,
		PUNCT,
		EOF
	}	
	
	static class Lemma implements Comparable<Lemma>{
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

		@Override
		public int compareTo(final Lemma o) {
			int	delta = o.type.ordinal() - type.ordinal();
			
			if (delta == 0) {
				delta = o.valueN - valueN;
				
				if (delta == 0) {
					if (o.valueS == valueS) {
						return 0;
					}
					else if (o.valueS == null) {
						return -1;
					}
					else if (valueS == null) {
						return 1;
					}
					else {
						return CharUtils.compareTo(o.valueS, valueS);
					}
				}
				else {
					return delta;
				}
			}
			else {
				return delta;
			}
		}
	}
	
	static enum LexType {
		SINGLE_TERM,
		WILDCARD_TERM,
		PHRASE,
		INTEGER,
		FLOAT,
		COLON,
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
		final Lemma[]	valueL;

		Lexema(final int pos, final LexType type) {
			this(pos, type, 0, null, null);
		}

		Lexema(final int pos, final int value) {
			this(pos, LexType.INTEGER, value, null, null);
		}

		Lexema(final int pos, final float value) {
			this(pos, LexType.FLOAT, Float.floatToIntBits(value), null, null);
		}

		Lexema(final int pos, final LexType type, final char[] value, final float fuzzy) {
			this(pos, type, Float.floatToIntBits(fuzzy), value, null);
		}

		Lexema(final int pos, final LexType type, final int prox, final Lemma[] value) {
			this(pos, type, prox, null, value);
		}
		
		Lexema(final int pos, final LexType type, final int valueN, final char[] valueC, final Lemma[] valueL) {
			this.pos = pos;
			this.type = type;
			this.valueN = valueN;
			this.valueC = valueC;
			this.valueL = valueL;
		}

		@Override
		public String toString() {
			return "Lexema [pos=" + pos + ", type=" + type + ", valueN=" + valueN + ", valueC=" + Arrays.toString(valueC) + ", valueL=" + Arrays.toString(valueL) + "]";
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
		RANGE_MATCHES,
		SEQUENCE,
		OR,
		AND,
		NOT,
		ROOT;
	}
	
	static enum TestResult {
		TRUE,
		FALSE,
		ULTIMATE_FALSE
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
	
	private static class ContentRepo {
		private final Function<String,CharSequence>	source;
		private final Content				defaultContent;
		private final Map<String, Content>	content = new HashMap<>();
		
		private ContentRepo(final Function<String,CharSequence> source) {
			this.source = source;
			this.defaultContent = new Content(lemmatize(source.apply(null)));
		}
		
		Content get(final String field) {
			if (field == null) {
				return defaultContent;
			}
			else {
				if (!content.containsKey(field)) {
					content.put(field, new Content(lemmatize(source.apply(null))));
				}
				if (!content.containsKey(field)) {
					throw new IllegalArgumentException("Content field ["+field+"] not found");
				}
				else {
					return content.get(field);
				}
			}
		}
	}
}
