package chav1961.purelib.cdb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.InternalUtils.Lexema.LexType;
import chav1961.purelib.cdb.interfaces.RuleBasedParser;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.enumerations.StylePropertiesSupported.Keyword;
import chav1961.purelib.streams.char2byte.AsmWriter;

class InternalUtils {
	private static final char		EOF = '\uFFFF';
	
	private static final SyntaxTreeInterface<Predefines>	PREDEFINED = new AndOrTree<>(1,1);
	private static final AsmWriter	AW;
	
	static enum Predefines {
		Empty, Name, FixedNumber, FloatNumber, QuotedString, DoubleQuotedString
	}
	
	static enum EntityType {
		Root, Rule, Char, Sequence, Name, Predefined, Option, Repeat, Switch, Case  
	}
	
	static {
		for(Predefines item : Predefines.values()) {
			PREDEFINED.placeName(item.name(), item);
		}
		
		try{AW = new AsmWriter(new ByteArrayOutputStream(), new PrintWriter(System.err));
		
			try(final InputStream	is = InternalUtils.class.getResourceAsStream("ruleBasedParserMacros.txt");
				final Reader		rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
				
				Utils.copyStream(rdr, AW);
			}
		} catch (IOException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}
	}
	
	static <NodeType extends Enum<?>, Cargo> RuleBasedParser<NodeType, Cargo> buildRuleBasedParser(final Class<NodeType> clazz, final String rule, final SyntaxTreeInterface<Cargo> names, final SimpleURLClassLoader loader) throws SyntaxException {
		final char[]									content = CharUtils.terminateAndConvert2CharArray(rule, EOF);
		final SyntaxTreeInterface<NodeType>				items = new AndOrTree<>(1,1);
		final Lexema									lex = new Lexema();
		final int[]										temp = new int[2];
		final SyntaxNode<EntityType, SyntaxNode>		root = new SyntaxNode<>(0, 0, EntityType.Root, 0, null);
		final List<SyntaxNode<EntityType, SyntaxNode>>	rules = new ArrayList<>();
		
		for(NodeType item : clazz.getEnumConstants()) {
			items.placeName(item.name(), item);
		}
		
		int from = next(content, 0, items, temp, lex); 
		while (lex.type != Lexema.LexType.EOF) {
			from = parse(content, from, items, temp, lex, root);
			rules.add((SyntaxNode<EntityType, SyntaxNode>)root.clone());
		}
		root.type = EntityType.Root;
		root.cargo = null;
		root.value = 0;
		root.children = rules.toArray(new SyntaxNode[rules.size()]);
		
		return null;
	}
	
	
	static <NodeType extends Enum<?>> int parse(final char[] content, int from, final SyntaxTreeInterface<NodeType> keywords, final int[] temp, final Lexema lex, final SyntaxNode<EntityType, SyntaxNode> root) throws SyntaxException {
		long	left;
		
		if (lex.type == LexType.Name) {
			left = lex.keyword;
			from = next(content, from, keywords, temp, lex);
			if (lex.type == LexType.Ergo) {
				from = parseRight(content, next(content, from, keywords, temp, lex), keywords, temp, lex, root);
				if (lex.type == LexType.NL || lex.type == LexType.EOF) {
					if (lex.type == LexType.NL) {
						from = next(content, from, keywords, temp, lex);
					}
					root.type = EntityType.Rule;
					root.value = left;
					root.cargo = null;
					return from;
				}
				else {
					throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Dust in the tail of the rule"); 
				}
			}
			else {
				throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Missng '::=' in the rule"); 
			}
		}
		else {
			throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Missing name in the left part of the rule"); 
		}
	}

	static <NodeType extends Enum<?>> int next(final char[] content, int from, final SyntaxTreeInterface<NodeType> keywords, final int[] temp, final Lexema lex) throws SyntaxException {
		int		start = from = CharUtils.skipBlank(content, from, true);
		
		switch (content[from]) {
			case EOF :
				lex.type = Lexema.LexType.EOF;
				return from;
			case '\r' :
				return next(content, from + 1, keywords, temp, lex);
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
					lex.type = Lexema.LexType.Close;
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
						lex.keyword = id;
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
				try{
					from = CharUtils.parseUnescapedString(content, from + 1, '\'', true, temp);
				} catch (IllegalArgumentException exc) {
					throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), exc.getLocalizedMessage()); 
				}
				
				if (content[from - 1] == '\'') {
					if (temp[0] > temp[1]) {
						throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), "Empty sequence is not supported"); 
					}
					else if (temp[0] == temp[1]) {
						lex.type = Lexema.LexType.Char;
						lex.keyword = content[temp[0]];
						return from;
					}
					else {
						lex.type = Lexema.LexType.Sequence;
						lex.sequence = Arrays.copyOfRange(content, temp[0], temp[1] + 1);
						return from;
					}
				}
				else {
					throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), "Missing '\''"); 
				}
			default :
				throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), "Unknown char"); 
		}
	}
	
	private static <NodeType extends Enum<?>> int parseRight(final char[] content, int from, final SyntaxTreeInterface<NodeType> keywords, final int[] temp, final Lexema lex, final SyntaxNode<EntityType, SyntaxNode> root) throws SyntaxException {
		final List<SyntaxNode<EntityType, SyntaxNode>>	list = new ArrayList<>();
		SyntaxNode<EntityType, SyntaxNode> 				subtree, altersubtree;
		
loop:	for (;;) {
			switch (lex.type) {
				case Char	:
					list.add(new SyntaxNode<>(0, from, EntityType.Char, lex.keyword, null));
					from = next(content, from, keywords, temp, lex);
					break;
				case Name	:
					list.add(new SyntaxNode<>(0, from, EntityType.Name, lex.keyword, null));
					from = next(content, from, keywords, temp, lex);
					break;
				case Open	:
					from = parseRight(content, next(content, from, keywords, temp, lex), keywords, temp, lex, subtree = (SyntaxNode<EntityType, SyntaxNode>) root.clone());
					if (lex.type == LexType.Repeat) {
						subtree.col = from;
						subtree.type = EntityType.Repeat;
						subtree.value = 0;
						subtree.cargo = null;
						list.add(subtree);
						from = next(content, from, keywords, temp, lex);
					}
					else if (lex.type == LexType.Close) {
						list.addAll((Collection<? extends SyntaxNode<EntityType, SyntaxNode>>) Arrays.asList(subtree.children));
						from = next(content, from, keywords, temp, lex);
					}
					else {
						throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Missing ')' or ')...'"); 
					}
					break;
				case OpenB	:
					from = parseRight(content, next(content, from, keywords, temp, lex), keywords, temp, lex, subtree = (SyntaxNode<EntityType, SyntaxNode>) root.clone());
					if (lex.type == LexType.CloseB) {
						subtree.col = from;
						subtree.type = EntityType.Option;
						subtree.value = 0;
						subtree.cargo = null;
						list.add(subtree);
						from = next(content, from, keywords, temp, lex);
					}
					else {
						throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Missing ']'"); 
					}
					break;
				case OpenF	:
					final List<SyntaxNode<EntityType, SyntaxNode>>	alters = new ArrayList<>();
					do {from = parseRight(content, next(content, from, keywords, temp, lex), keywords, temp, lex, subtree = (SyntaxNode<EntityType, SyntaxNode>) root.clone());
						subtree.type = EntityType.Case;
						subtree.col = from;
						subtree.value = 0;
						subtree.cargo = null;
						alters.add(subtree);
					} while(lex.type == LexType.Alter);
					
					if (lex.type == LexType.CloseF) {
						subtree = (SyntaxNode<EntityType, SyntaxNode>) root.clone();
						subtree.col = from;
						subtree.type = EntityType.Switch;
						subtree.value = 0;
						subtree.cargo = null;
						subtree.children = alters.toArray(new SyntaxNode[alters.size()]);
						list.add(subtree);
						from = next(content, from, keywords, temp, lex);
					}
					else {
						throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Missing '}'"); 
					}
					break;
				case Predefined	:
					list.add(new SyntaxNode<>(0, from, EntityType.Predefined, 0, lex.predefine));
					from = next(content, from, keywords, temp, lex);
					break;
				case Sequence:
					list.add(new SyntaxNode<>(0, from, EntityType.Sequence, 0, lex.sequence));
					from = next(content, from, keywords, temp, lex);
					break;
				default		:
					break loop;
			}
		}
		root.children = list.toArray(new SyntaxNode[list.size()]); 
		return from;
	}
	
	static <NodeType extends Enum<?>> void printTree(final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		switch (root.type) {
			case Char		:
				wr.write("'"+((char)root.value)+"' ");
				break;
			case Name		:
				wr.write("<"+(keywords.getName(root.value))+"> ");
				break;
			case Option		:
				wr.write("[");
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					printTree(item, keywords, wr);
				}
				wr.write("] ");
				break;
			case Predefined	:
				wr.write("@");
				wr.write(root.cargo.toString());
				wr.write(" ");
				break;
			case Repeat		:
				wr.write("(");
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					printTree(item, keywords, wr);
				}
				wr.write(")... ");
				break;
			case Root		:
				wr.write("=== Rules : \r\n");
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					printTree(item, keywords, wr);
				}
				wr.write("=== End rules.\r\n");
				break;
			case Rule		:
				wr.write("<"+(keywords.getName(root.value))+"> ::= ");
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					printTree(item, keywords, wr);
				}
				wr.write("\r\n");
				break;
			case Sequence	:
				wr.write("\""+new String((char[])root.cargo)+"\" ");
				break;
			case Switch		:
				String	prefix = "{";
				
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					wr.write(""+prefix);
					printTree(item, keywords, wr);
					prefix = "| ";
				}
				wr.write("} ");
				break;
			case Case		:
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					printTree(item, keywords, wr);
				}
				break;
			default:
				throw new UnsupportedOperationException("Node type ["+root.type+"] is not supported yet");
		}
	}

	static <NodeType extends Enum<?>, Cargo> void buildRuleProcessing(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException  {
		final IdentityHashMap<SyntaxNode<EntityType, ?>, String[]>		baseLevelProc = new IdentityHashMap<>();
		int[]		uniqueNumer = new int[] {0};
		
		SyntaxNodeUtils.walkDown(root, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				final int	uniqueId = uniqueNumer[0]++;
				
				switch (node.type) {
					case Char		:
						baseLevelProc.put(node, new String[] {"char"+uniqueId, "testChar"+uniqueId, "skipChar"+uniqueId, "parseChar"+uniqueId});
						break;
					case Sequence	:
						baseLevelProc.put(node, new String[] {"sequence"+uniqueId, "testSequence"+uniqueId, "skipSequence"+uniqueId, "parseSequence"+uniqueId});
						break;
					case Predefined	:
						baseLevelProc.put(node, new String[] {"predefined"+((Predefines)node.cargo).name()+uniqueId, "testPredefined"+((Predefines)node.cargo).name()+uniqueId, "skipPredefined"+((Predefines)node.cargo).name()+uniqueId, "parsePredefined"+((Predefines)node.cargo).name()+uniqueId});
						uniqueNumer[0]++;
						break;
					case Case		:
						baseLevelProc.put(node, new String[] {"case"+uniqueId, "testCase"+uniqueId, "skipCase"+uniqueId, "parseCase"+uniqueId});
						break;
					case Name		:
						baseLevelProc.put(node, new String[] {"name"+((Predefines)node.cargo).name()+uniqueId, "testName"+((Predefines)node.cargo).name()+uniqueId, "skipName"+((Predefines)node.cargo).name()+uniqueId, "parseName"+((Predefines)node.cargo).name()+uniqueId});
						break;
					case Option		:
						baseLevelProc.put(node, new String[] {"option"+uniqueId, "testOption"+uniqueId, "skipOption"+uniqueId, "parseOption"+uniqueId});
						break;
					case Repeat		:
						baseLevelProc.put(node, new String[] {"repeat"+uniqueId, "testRepeat"+uniqueId, "skipRepeat"+uniqueId, "parseRepeat"+uniqueId});
						break;
					case Root		:
						baseLevelProc.put(node, new String[] {"root"+uniqueId, "testRoot"+uniqueId, "skipRoot"+uniqueId, "parseRoot"+uniqueId});
						break;
					case Rule		:
						baseLevelProc.put(node, new String[] {"rule"+uniqueId, "testRule"+uniqueId, "skipRule"+uniqueId, "parseRule"+uniqueId});
						break;
					case Switch		:
						baseLevelProc.put(node, new String[] {"switch"+uniqueId, "testSwitch"+uniqueId, "skipSwitch"+uniqueId, "parseSwitch"+uniqueId});
						break;
					default:
						break;
				}
			}
			return ContinueMode.CONTINUE;
		});

		wr.write(" printImports\n");
		buildPuleProcessingClass(className, root, keywords, wr);
		for (Entry<SyntaxNode<EntityType, ?>, String[]> item : baseLevelProc.entrySet()) {
			buildRuleProcessingFields(className, root, item.getKey(), item.getValue(), keywords, wr);
		}
		wr.write(" beforeClinit className=\""+className+"\"\n");
		for (Entry<SyntaxNode<EntityType, ?>, String[]> item : baseLevelProc.entrySet()) {
			buildRuleProcessingClinit(className, root, item.getKey(), item.getValue(), keywords, wr);
		}
		wr.write(" afterClinit className=\""+className+"\"\n");
		buildPuleProcessingConstructor(className, root, keywords, wr);
		for (Entry<SyntaxNode<EntityType, ?>, String[]> item : baseLevelProc.entrySet()) {
			buildRuleProcessingMethods(className, root, item.getKey(), item.getValue(), keywords, wr);
		}
		buildRuleProcessingPublics(className, root, keywords, wr);
		buildRuleProcessingTail(className, root, keywords, wr);
	}


	private static <NodeType extends Enum<?>, Cargo> void buildPuleProcessingClass(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		// TODO Auto-generated method stub
		wr.write(" beginClassDeclaration className=\""+className+"\"\n");
	}

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingFields(final String className, final SyntaxNode<EntityType, ?> root, final SyntaxNode<EntityType, ?> item, final String[] names, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		// TODO Auto-generated method stub
		if (item.type == EntityType.Sequence) {
			wr.write(" declareSequenceField className=\""+className+"\",fieldName=\""+1+"\"\n");
		}
	}

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingClinit(final String className, final SyntaxNode<EntityType, ?> root, final SyntaxNode<EntityType, ?> item, final String[] names, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		// TODO Auto-generated method stub
		if (item.type == EntityType.Sequence) {
			wr.write(" clinitPrepareSequence className=\""+className+"\",fieldName=\""+1+"\",value=\""+new String((char[])item.cargo)+"\"\n");
		}
	}

	private static <NodeType extends Enum<?>, Cargo>  void buildPuleProcessingConstructor(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		// TODO Auto-generated method stub
		wr.write(" prepareConstructor className=\""+className+"\"\n");
	}

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingMethods(final String className, final SyntaxNode<EntityType, ?> root, final SyntaxNode<EntityType, ?> item, final String[] names, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		// TODO Auto-generated method stub
		switch (item.type) {
			case Case		:
				break;
			case Char		:
				wr.write(" prepareCharTestMethod className=\""+className+"\",fieldValue="+item.value+",methodName=\""+names[1]+"\"\n");
				wr.write(" prepareCharSkipMethod className=\""+className+"\",fieldValue="+item.value+",methodName=\""+names[2]+"\"\n");
				wr.write(" prepareCharParseMethod className=\""+className+"\",fieldValue="+item.value+",methodName=\""+names[3]+"\"\n");
				break;
			case Name		:
				break;
			case Option		:
				break;
			case Predefined	:
				break;
			case Repeat		:
				break;
			case Root		:
				break;
			case Rule		:
				break;
			case Sequence	:
				wr.write(" prepareSequenceTestMethod className=\""+className+"\",fieldName=\""+names[0]+"\",methodName=\""+names[1]+"\"\n");
				wr.write(" prepareSequenceSkipMethod className=\""+className+"\",fieldName=\""+names[0]+"\",methodName=\""+names[2]+"\"\n");
				wr.write(" prepareSequenceParseMethod className=\""+className+"\",fieldName=\""+names[0]+"\",methodName=\""+names[3]+"\"\n");
				break;
			case Switch		:
				break;
			default:
				break;
		}
	}

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingPublics(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		// TODO Auto-generated method stub
		wr.write(" preparePublicParse className=\""+className+"\"\n");
	}

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingTail(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		// TODO Auto-generated method stub
		wr.write(" endClassDeclaration className=\""+className+"\"\n");
	}
	
	static class Lexema {
		static enum LexType {
			Name, Predefined, Open, Close, OpenB, CloseB, OpenF, CloseF, Alter, Ergo, Char, Sequence, Colon, Repeat, NL, EOF 
		}
		
		LexType		type;
		Predefines	predefine;
		long		keyword;
		char[]		sequence;
	}
}
