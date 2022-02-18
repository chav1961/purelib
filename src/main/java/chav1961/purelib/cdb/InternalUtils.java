package chav1961.purelib.cdb;

import java.util.Arrays;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.interfaces.RuleBasedParser;

class InternalUtils {
	private static final char		EOF = '\uFFFF';
	
	private static final SyntaxTreeInterface<Predefines>	PREDEFINED = new AndOrTree<>(1,1);
	
	private static enum Predefines {
		Empty, Name, FixedNumber, FloatNumber, QuotedString, DoubleQuotedString
	}
	
	static {
		for(Predefines item : Predefines.values()) {
			PREDEFINED.placeName(item.name(), item);
		}
	}
	
	static <NodeType extends Enum<?>, Cargo> RuleBasedParser<NodeType, Cargo> buildRuleBasedParser(final Class<NodeType> clazz, final String rule, final SyntaxTreeInterface<Cargo> names, final SimpleURLClassLoader loader) throws SyntaxException {
		final char[]						content = CharUtils.terminateAndConvert2CharArray(rule, EOF);
		final SyntaxTreeInterface<NodeType>	items = new AndOrTree<>(1,1);
		final Lexema						lex = new Lexema();
		final int[]							temp = new int[2];
		
		for(NodeType item : clazz.getEnumConstants()) {
			items.placeName(item.name(), item);
		}
		
		int from = next(content, 0, items, temp, lex); 
		while (lex.type != Lexema.LexType.EOF) {
			
		}
		
		return null;
	}
	
	
	static <NodeType extends Enum<?>> int next(final char[] content, int from, final SyntaxTreeInterface<NodeType> keywords, final int[] temp, final Lexema lex) throws SyntaxException, IllegalArgumentException, NullPointerException {
		int		start = from = CharUtils.skipBlank(content, from, false);
		
		switch (content[from]) {
			case EOF :
				lex.type = Lexema.LexType.EOF;
				return from;
			case '\n' :
				lex.type = Lexema.LexType.NL;
				return from + 1;
			case '(' :
				lex.type = Lexema.LexType.Open;
				return from + 1;
			case '[' :
				lex.type = Lexema.LexType.OpenB;
				return from + 1;
			case '{' :
				lex.type = Lexema.LexType.OpenF;
				return from + 1;
			case '|' :
				lex.type = Lexema.LexType.Alter;
				return from + 1;
			case ']' :
				lex.type = Lexema.LexType.CloseB;
				return from + 1;
			case '}' :
				lex.type = Lexema.LexType.CloseF;
				return from + 1;
			case ':' :
				if (content[from + 1] == ':' && content[from + 2] == '=') {
					lex.type = Lexema.LexType.Ergo;
					return from + 3;
				}
				else {
					lex.type = Lexema.LexType.Colon;
					return from + 1;
				}
			case ')' :
				if (content[from + 1] == '.' && content[from + 2] == '.' && content[from + 3] == '.') {
					lex.type = Lexema.LexType.Repeat;
					return from + 4;
				}
				else {
					lex.type = Lexema.LexType.Colon;
					return from + 1;
				}
			case '@' :
				from = CharUtils.parseName(content, from + 1, temp);
				final long	kw = PREDEFINED.seekNameI(content, temp[0], temp[1] + 1);
				
				if (kw >= 0) {
					lex.type = Lexema.LexType.Predefined;
					lex.predefine = PREDEFINED.getCargo(kw);
					return from;
				}
				else {
					throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), "Unknown predefined name ["+new String(content, temp[0], temp[1] - temp[0])+"]"); 
				}
			case '<' :
				from = CharUtils.parseName(content, from + 1, temp);
				
				if (content[from] == '>') {
					final long	id = keywords.seekNameI(content, temp[0], temp[1] + 1);
					
					if (id >= 0) {
						lex.type = Lexema.LexType.Name;
						return from + 1;
					}
					else {
						throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), "Unknown predefined name ["+new String(content, temp[0], temp[1] - temp[0])+"]"); 
					}
				}
				else {
					throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), "Missing '>'"); 
				}
			case '\'' :
				from = CharUtils.parseUnescapedString(content, from + 1, '\'', true, temp);
				if (content[from] == '\'') {
					if (temp[0] == temp[1]) {
						throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), "Empty sequence is not supported"); 
					}
					else if (temp[0] == temp[1] - 1) {
						lex.type = Lexema.LexType.Char;
						lex.keyword = content[from + 1];
						return from + 1;
					}
					else {
						lex.type = Lexema.LexType.Sequence;
						lex.sequence = Arrays.copyOfRange(content, temp[0], temp[1]);
						return from + 1;
					}
				}
				else {
					throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), "Missing '\''"); 
				}
			default :
				throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), "Unknown char"); 
		}
	}
	
	
	private static class Lexema {
		private static enum LexType {
			Name, Predefined, Open, Close, OpenB, CloseB, OpenF, CloseF, Alter, Ergo, Char, Sequence, Colon, Repeat, NL, EOF 
		}
		
		private LexType		type;
		private Predefines	predefine;
		private long		keyword;
		private char[]		sequence;
	}
}
