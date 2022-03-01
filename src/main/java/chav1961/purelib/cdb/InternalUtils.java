package chav1961.purelib.cdb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
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
import java.util.concurrent.atomic.AtomicInteger;

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
import chav1961.purelib.cdb.intern.EntityType;
import chav1961.purelib.cdb.intern.Predefines;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.enumerations.StylePropertiesSupported.Keyword;
import chav1961.purelib.streams.char2byte.AsmWriter;

class InternalUtils {
	private static final char			EOF = '\uFFFF';
	
	private static final SyntaxTreeInterface<Predefines>	PREDEFINED = new AndOrTree<>(1,1);
	private static final AsmWriter		AW;
	private static final AtomicInteger	AI = new AtomicInteger();
	
	static {
		for(Predefines item : Predefines.values()) {
			PREDEFINED.placeName(item.name(), item);
		}
		
		try{AW = new AsmWriter(new ByteArrayOutputStream(), new PrintWriter(System.out));
		
			try(final InputStream	is = InternalUtils.class.getResourceAsStream("ruleBasedParserMacros.txt");
				final Reader		rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
				
				Utils.copyStream(rdr, AW);
			}
		} catch (IOException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}
	}
	
	static <NodeType extends Enum<?>, Cargo> Class<RuleBasedParser<NodeType, Cargo>> buildRuleBasedParser(final String className, final Class<NodeType> clazz, final String rule, final SimpleURLClassLoader loader) throws SyntaxException {
		final char[]									content = CharUtils.terminateAndConvert2CharArray(rule, EOF);
		final SyntaxTreeInterface<NodeType>				items = new AndOrTree<>(1,1);
		final Lexema									lex = new Lexema();
		final int[]										temp = new int[2];
		final SyntaxNode<EntityType, SyntaxNode>		root = new SyntaxNode<>(0, 0, EntityType.Root, 0, null);
		final List<SyntaxNode<EntityType, SyntaxNode>>	rules = new ArrayList<>();

		prepareKeywordsTree(clazz, items);
		
		int from = next(content, 0, items, temp, lex); 
		
		while (lex.type != Lexema.LexType.EOF) {
			final SyntaxNode<EntityType, SyntaxNode>	clone = (SyntaxNode<EntityType, SyntaxNode>)AbstractBNFParser.TEMPLATE.clone(); 
			
			from = parse(content, from, items, temp, lex, clone);
			rules.add(clone);
		}
		
		if (rules.isEmpty()) {
			throw new SyntaxException(0, 0, "No any rules detected in the rule string!");
		}
		else {
			root.type = EntityType.Root;
			root.cargo = null;
			root.value = 0;
			root.children = rules.toArray(new SyntaxNode[rules.size()]);

			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				try(final AsmWriter			wr = AW.clone(baos)) {
					
					if (className.contains(".")) {
						final int	lastDot = className.lastIndexOf('.');
						buildRuleProcessing(className.substring(0, lastDot), className.substring(lastDot + 1), root, items, wr);
					}
					else {
						buildRuleProcessing("", className, root, items, wr);
					}
				}
				return (Class<RuleBasedParser<NodeType, Cargo>>) loader.createClass(className, baos.toByteArray());
			} catch (IOException e) {
				throw new SyntaxException(0, 0, e.getLocalizedMessage());
			}
		}
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
				throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Missing '::=' in the rule"); 
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
					if (lex.type == LexType.Colon) {
						from = next(content, from, keywords, temp, lex);
						if (lex.type == LexType.Name) {
							list.add(new SyntaxNode<>(0, from, EntityType.Detected, lex.keyword, null));
							from = next(content, from, keywords, temp, lex);
						}
						else {
							throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Missing name after ':'"); 
						}
					}
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
						subtree.value = subtree.type.ordinal();
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
						subtree.value = subtree.type.ordinal();
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
						subtree.value = subtree.type.ordinal();
						subtree.cargo = null;
						alters.add(subtree);
					} while(lex.type == LexType.Alter);
					
					if (lex.type == LexType.CloseF) {
						subtree = (SyntaxNode<EntityType, SyntaxNode>) root.clone();
						subtree.col = from;
						subtree.type = EntityType.Switch;
						subtree.value = subtree.type.ordinal();
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
					if (lex.type == LexType.Colon) {
						from = next(content, from, keywords, temp, lex);
						if (lex.type == LexType.Name) {
							list.add(new SyntaxNode<>(0, from, EntityType.Detected, lex.keyword, null));
							from = next(content, from, keywords, temp, lex);
						}
						else {
							throw new SyntaxException(SyntaxException.toRow(content, from), SyntaxException.toCol(content, from), "Missing name after ':'"); 
						}
					}
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
			case Detected	:
				wr.write(":<"+(keywords.getName(root.value))+"> ");
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

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessing(final String packageName, final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException, SyntaxException  {
		final IdentityHashMap<SyntaxNode<EntityType, ?>, FieldAndMethods>		baseLevelProc = new IdentityHashMap<>();
		int[]		uniqueNumer = new int[] {0};
		
		SyntaxNodeUtils.walkDown(root, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				final int	uniqueId = uniqueNumer[0]++;
				
				switch (node.type) {
					case Char		:
						baseLevelProc.put(node, new FieldAndMethods("char"+uniqueId, "testChar"+uniqueId, "skipChar"+uniqueId, "parseChar"+uniqueId));
						break;
					case Sequence	:
						baseLevelProc.put(node, new FieldAndMethods("sequence"+uniqueId, "testSequence"+uniqueId, "skipSequence"+uniqueId, "parseSequence"+uniqueId));
						break;
					case Predefined	:
						baseLevelProc.put(node, new FieldAndMethods("predefined"+((Predefines)node.cargo).name()+uniqueId, "testPredefined"+((Predefines)node.cargo).name()+uniqueId, "skipPredefined"+((Predefines)node.cargo).name()+uniqueId, "parsePredefined"+((Predefines)node.cargo).name()+uniqueId));
						uniqueNumer[0]++;
						break;
					case Case		:
						baseLevelProc.put(node, new FieldAndMethods("case"+uniqueId, "testCase"+uniqueId, "skipCase"+uniqueId, "parseCase"+uniqueId));
						break;
					case Name		:
						baseLevelProc.put(node, new FieldAndMethods("name"+keywords.getName(node.value)+uniqueId, "testName"+keywords.getName(node.value)+uniqueId, "skipName"+keywords.getName(node.value)+uniqueId, "parseName"+keywords.getName(node.value)+uniqueId));
						break;
					case Option		:
						baseLevelProc.put(node, new FieldAndMethods("option"+uniqueId, "testOption"+uniqueId, "skipOption"+uniqueId, "parseOption"+uniqueId));
						break;
					case Repeat		:
						baseLevelProc.put(node, new FieldAndMethods("repeat"+uniqueId, "testRepeat"+uniqueId, "skipRepeat"+uniqueId, "parseRepeat"+uniqueId));
						break;
					case Root		:
						baseLevelProc.put(node, new FieldAndMethods("root"+uniqueId, "testRoot"+uniqueId, "skipRoot"+uniqueId, "parseRoot"+uniqueId));
						break;
					case Rule		:
						baseLevelProc.put(node, new FieldAndMethods("rule"+uniqueId, "testRule"+uniqueId, "skipRule"+uniqueId, "parseRule"+uniqueId));
						break;
					case Switch		:
						baseLevelProc.put(node, new FieldAndMethods("switch"+uniqueId, "testSwitch"+uniqueId, "skipSwitch"+uniqueId, "parseSwitch"+uniqueId));
						break;
					case Detected	:
						baseLevelProc.put(node, new FieldAndMethods("detected"+uniqueId, "testDetected"+uniqueId, "skipDetected"+uniqueId, "parseDetected"+uniqueId));
						break;
					default:
						throw new UnsupportedOperationException("Node type ["+root.type+"] is not supported yet");
				}
			}
			return ContinueMode.CONTINUE;
		});

		if (packageName.isEmpty()) {
			wr.write(" printImports\n");
		}
		else {
			wr.write(" printImports package=\""+packageName+"\"\n");
		}
		buildPuleProcessingClass(className, root, keywords, wr);
		for (Entry<SyntaxNode<EntityType, ?>, FieldAndMethods> item : baseLevelProc.entrySet()) {
			buildRuleProcessingFields(className, root, item.getKey(), item.getValue(), keywords, wr);
		}
		wr.write(" beforeClinit className=\""+className+"\"\n");
		for (Entry<SyntaxNode<EntityType, ?>, FieldAndMethods> item : baseLevelProc.entrySet()) {
			buildRuleProcessingClinit(className, root, item.getKey(), item.getValue(), keywords, wr);
		}
		wr.write(" afterClinit className=\""+className+"\"\n");
		buildRuleProcessingConstructor(className, root, keywords, wr);
		buildRuleProcessingMethods(className, root, root.children, baseLevelProc, keywords, wr);
		buildRuleProcessingTail(className, root, keywords, wr);
	}


	private static <NodeType extends Enum<?>, Cargo> void buildPuleProcessingClass(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		// TODO Auto-generated method stub
		wr.write(" beginClassDeclaration className=\""+className+"\"\n");
	}

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingFields(final String className, final SyntaxNode<EntityType, ?> root, final SyntaxNode<EntityType, ?> item, final FieldAndMethods names, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		// TODO Auto-generated method stub
		if (item.type == EntityType.Sequence) {
			wr.write(" declareSequenceField className=\""+className+"\",fieldName=\""+names.field+"\"\n");
		}
	}

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingClinit(final String className, final SyntaxNode<EntityType, ?> root, final SyntaxNode<EntityType, ?> item, final FieldAndMethods names, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		// TODO Auto-generated method stub
		if (item.type == EntityType.Sequence) {
			wr.write(" clinitPrepareSequence className=\""+className+"\",fieldName=\""+names.field+"\",value=\""+new String((char[])item.cargo)+"\"\n");
		}
	}

	private static <NodeType extends Enum<?>, Cargo>  void buildRuleProcessingConstructor(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		// TODO Auto-generated method stub
		wr.write(" prepareConstructor className=\""+className+"\"\n");
	}

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingMethods(final String className, final SyntaxNode<EntityType, ?> root, final SyntaxNode<EntityType, ?>[] children, final IdentityHashMap<SyntaxNode<EntityType, ?>, FieldAndMethods> map, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException, SyntaxException {
		final FieldAndMethods	names = map.get(root);

		if (root.children != null) {
			for (SyntaxNode<EntityType, ?> child : root.children) {
				buildRuleProcessingMethods(className, child, children, map, keywords, wr);
			}
		}
		
inside:	switch (root.type) {
			case Case		:
				wr.write(" prepareCaseTestMethodStart className=\""+className+"\",methodName=\""+names.testMethod+"\"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateTestMethod()) {
						wr.write(" prepareCaseTestMethodItem className=\""+className+"\",methodName=\""+map.get(child).testMethod+"\",skipMethodName=\""+map.get(child).skipMethod+"\"\n");
					}
				}
				wr.write(" prepareCaseTestMethodEnd className=\""+className+"\",methodName=\""+names.testMethod+"\"\n");

				wr.write(" prepareCaseSkipMethodStart className=\""+className+"\",methodName=\""+names.skipMethod+"\",testMethodName=\""+names.testMethod+"\"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateSkipMethod()) {
						wr.write(" prepareCaseSkipMethodItem className=\""+className+"\",methodName=\""+map.get(child).skipMethod+"\"\n");
					}
				}
				wr.write(" prepareCaseSkipMethodEnd className=\""+className+"\",methodName=\""+names.skipMethod+"\"\n");

				wr.write(" prepareCaseParseMethodStart className=\""+className+"\",methodName=\""+names.parseMethod+"\",testMethodName=\""+names.testMethod+"\",makeChildren="+(root.children.length > 1)+"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateParseMethod()) {
						wr.write(" prepareCaseParseMethodItem className=\""+className+"\",methodName=\""+map.get(child).parseMethod+"\",makeChildren="+(root.children.length > 1)+"\n");
					}
				}
				wr.write(" prepareCaseParseMethodEnd className=\""+className+"\",methodName=\""+names.parseMethod+"\",makeChildren="+(root.children.length > 1)+",keyword="+root.value+"\n");
				break;
			case Char		:
				wr.write(" prepareCharTestMethod className=\""+className+"\",fieldValue="+root.value+",methodName=\""+names.testMethod+"\"\n");
				wr.write(" prepareCharSkipMethod className=\""+className+"\",fieldValue="+root.value+",methodName=\""+names.skipMethod+"\"\n");
				wr.write(" prepareCharParseMethod className=\""+className+"\",fieldValue="+root.value+",methodName=\""+names.parseMethod+"\"\n");
				break;
			case Name		:
				for (SyntaxNode<EntityType, ?> item : children) {
					if (item != root && item.value == root.value) {	// Skip self reference
						final FieldAndMethods	references = map.get(item);
						
						wr.write(" prepareNameTestMethod className=\""+className+"\",name="+root.value+",methodName=\""+names.testMethod+"\",referenceMethodName=\""+references.testMethod+"\"\n");
						wr.write(" prepareNameSkipMethod className=\""+className+"\",name="+root.value+",methodName=\""+names.skipMethod+"\",referenceMethodName=\""+references.skipMethod+"\"\n");
						wr.write(" prepareNameParseMethod className=\""+className+"\",name="+root.value+",methodName=\""+names.parseMethod+"\",referenceMethodName=\""+references.parseMethod+"\"\n");
						break inside;
					}
				}
				throw new SyntaxException(0, 0, "Rule item ["+keywords.getName(root.value)+"] is missing in the rule list");
			case Option		:
				wr.write(" prepareOptionTestMethodStart className=\""+className+"\",methodName=\""+names.testMethod+"\"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateTestMethod()) {
						wr.write(" prepareOptionTestMethodItem className=\""+className+"\",methodName=\""+map.get(child).testMethod+"\",skipMethodName=\""+map.get(child).skipMethod+"\"\n");
					}
				}
				wr.write(" prepareOptionTestMethodEnd className=\""+className+"\",methodName=\""+names.testMethod+"\"\n");

				wr.write(" prepareOptionSkipMethodStart className=\""+className+"\",methodName=\""+names.skipMethod+"\",testMethodName=\""+names.testMethod+"\"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateSkipMethod()) {
						wr.write(" prepareOptionSkipMethodItem className=\""+className+"\",methodName=\""+map.get(child).skipMethod+"\"\n");
					}
				}
				wr.write(" prepareOptionSkipMethodEnd className=\""+className+"\",methodName=\""+names.skipMethod+"\"\n");
				
				wr.write(" prepareOptionParseMethodStart className=\""+className+"\",methodName=\""+names.parseMethod+"\",testMethodName=\""+names.testMethod+"\",makeChildren="+(root.children.length > 1)+"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateParseMethod()) {
						wr.write(" prepareOptionParseMethodItem className=\""+className+"\",methodName=\""+map.get(child).parseMethod+"\",makeChildren="+(root.children.length > 1)+"\n");
					}
				}
				wr.write(" prepareOptionParseMethodEnd className=\""+className+"\",methodName=\""+names.parseMethod+"\",makeChildren="+(root.children.length > 1)+",keyword="+root.value+"\n");
				break;
			case Predefined	:
				wr.write(" preparePredefinedTestMethod className=\""+className+"\",name=\""+((Predefines)root.cargo).name()+"\",methodName=\""+names.testMethod+"\"\n");
				wr.write(" preparePredefinedSkipMethod className=\""+className+"\",name=\""+((Predefines)root.cargo).name()+"\",methodName=\""+names.skipMethod+"\"\n");
				wr.write(" preparePredefinedParseMethod className=\""+className+"\",name=\""+((Predefines)root.cargo).name()+"\",methodName=\""+names.parseMethod+"\"\n");
				break;
			case Repeat		:
				wr.write(" prepareRepeatTestMethodStart className=\""+className+"\",methodName=\""+names.testMethod+"\"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateTestMethod()) {
						wr.write(" prepareRepeatTestMethodItem className=\""+className+"\",methodName=\""+map.get(child).testMethod+"\",skipMethodName=\""+map.get(child).skipMethod+"\"\n");
					}
				}
				wr.write(" prepareRepeatTestMethodEnd className=\""+className+"\",methodName=\""+names.testMethod+"\"\n");

				wr.write(" prepareRepeatSkipMethodStart className=\""+className+"\",methodName=\""+names.skipMethod+"\",testMethodName=\""+names.testMethod+"\"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateSkipMethod()) {
						wr.write(" prepareRepeatSkipMethodItem className=\""+className+"\",methodName=\""+map.get(child).skipMethod+"\"\n");
					}
				}
				wr.write(" prepareRepeatSkipMethodEnd className=\""+className+"\",methodName=\""+names.skipMethod+"\"\n");
				
				wr.write(" prepareRepeatParseMethodStart className=\""+className+"\",methodName=\""+names.parseMethod+"\",testMethodName=\""+names.testMethod+"\",makeChildren="+(root.children.length > 1)+"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateParseMethod()) {
						wr.write(" prepareRepeatParseMethodItem className=\""+className+"\",methodName=\""+map.get(child).parseMethod+"\",makeChildren="+(root.children.length > 1)+"\n");
					}
				}
				wr.write(" prepareRepeatParseMethodEnd className=\""+className+"\",methodName=\""+names.parseMethod+"\",makeChildren="+(root.children.length > 1)+",keyword="+root.value+",testMethodName=\""+names.testMethod+"\"\n");
				break;
			case Root		:
				final FieldAndMethods	first = map.get(root.children[0]);
				
				wr.write(" prepareSkipInternal className=\""+className+"\",testMethod=\""+first.testMethod+"\",skipMethod=\""+first.skipMethod+"\"\n");
				wr.write(" prepareParseInternal className=\""+className+"\",testMethod=\""+first.testMethod+"\",parseMethod=\""+first.parseMethod+"\"\n");
				break;
			case Rule		:
				wr.write(" prepareRuleTestMethodStart className=\""+className+"\",methodName=\""+names.testMethod+"\"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateTestMethod()) {
						wr.write(" prepareRuleTestMethodItem className=\""+className+"\",methodName=\""+map.get(child).testMethod+"\",skipMethodName=\""+map.get(child).skipMethod+"\"\n");
					}
				}
				wr.write(" prepareRuleTestMethodEnd className=\""+className+"\",methodName=\""+names.testMethod+"\"\n");
				
				wr.write(" prepareRuleSkipMethodStart className=\""+className+"\",methodName=\""+names.skipMethod+"\",testMethodName=\""+names.testMethod+"\"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateSkipMethod()) {
						wr.write(" prepareRuleSkipMethodItem className=\""+className+"\",methodName=\""+map.get(child).skipMethod+"\"\n");
					}
				}
				wr.write(" prepareRuleSkipMethodEnd className=\""+className+"\",methodName=\""+names.skipMethod+"\"\n");
				
				wr.write(" prepareRuleParseMethodStart className=\""+className+"\",methodName=\""+names.parseMethod+"\",testMethodName=\""+names.testMethod+"\",makeChildren="+(root.children.length > 1)+"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateParseMethod()) {
						wr.write(" prepareRuleParseMethodItem className=\""+className+"\",methodName=\""+map.get(child).parseMethod+"\",makeChildren="+(root.children.length > 1)+"\n");
					}					
				}
				wr.write(" prepareRuleParseMethodEnd className=\""+className+"\",methodName=\""+names.parseMethod+"\",makeChildren="+(root.children.length > 1)+",keyword="+root.value+"\n");
				break;
			case Sequence	:
				wr.write(" prepareSequenceTestMethod className=\""+className+"\",fieldName=\""+names.field+"\",methodName=\""+names.testMethod+"\"\n");
				wr.write(" prepareSequenceSkipMethod className=\""+className+"\",fieldName=\""+names.field+"\",methodName=\""+names.skipMethod+"\",testMethodName=\""+names.testMethod+"\"\n");
				wr.write(" prepareSequenceParseMethod className=\""+className+"\",fieldName=\""+names.field+"\",methodName=\""+names.parseMethod+"\",testMethodName=\""+names.testMethod+"\"\n");
				break;
			case Switch		:
				wr.write(" prepareSwitchTestMethodStart className=\""+className+"\",methodName=\""+names.testMethod+"\"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateTestMethod()) {
						wr.write(" prepareSwitchTestMethodItem className=\""+className+"\",methodName=\""+map.get(child).testMethod+"\"\n");
					}
				}
				wr.write(" prepareSwitchTestMethodEnd className=\""+className+"\",methodName=\""+names.testMethod+"\"\n");
				
				wr.write(" prepareSwitchSkipMethodStart className=\""+className+"\",methodName=\""+names.skipMethod+"\",testMethodName=\""+names.testMethod+"\"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateSkipMethod()) {
						wr.write(" prepareSwitchSkipMethodItem className=\""+className+"\",methodName=\""+map.get(child).skipMethod+"\",testMethodName=\""+map.get(child).testMethod+"\"\n");
					}
				}
				wr.write(" prepareSwitchSkipMethodEnd className=\""+className+"\",methodName=\""+names.skipMethod+"\"\n");

				wr.write(" prepareSwitchParseMethodStart className=\""+className+"\",methodName=\""+names.parseMethod+"\",testMethodName=\""+names.testMethod+"\"\n");
				for (SyntaxNode<EntityType, ?> child : root.children) {
					if (child.type.needCreateParseMethod()) {
						wr.write(" prepareSwitchParseMethodItem className=\""+className+"\",methodName=\""+map.get(child).parseMethod+"\",testMethodName=\""+map.get(child).testMethod+"\"\n");
					}
				}
				wr.write(" prepareSwitchParseMethodEnd className=\""+className+"\",methodName=\""+names.parseMethod+"\"\n");
				break;
			case Detected	:
				wr.write(" prepareDetectedParseMethod className=\""+className+"\",methodName=\""+names.parseMethod+"\",item="+root.value+"\n");
				break;
			default:
				throw new UnsupportedOperationException("Node type ["+root.type+"] is not supported yet");
		}
	}	
	
	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingTail(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		wr.write(" endClassDeclaration className=\""+className+"\"\n");
	}

	static <NodeType extends Enum<?>, Cargo> Class<RuleBasedParser<NodeType, Cargo>> buildRuleBasedParserClass(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> items, final SimpleURLClassLoader loader) throws SyntaxException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			try(final AsmWriter			wr = AW.clone(baos)) {
				
				buildRuleProcessing("", className, new SyntaxNode<>(0, 0, EntityType.Root, 0, null, root), items, wr);
			}
			
			return (Class<RuleBasedParser<NodeType, Cargo>>) loader.createClass(className, baos.toByteArray());
		} catch (IOException e) {
			throw new SyntaxException(0, 0, e.getLocalizedMessage());
		}
	}

	static <NodeType extends Enum<?>> void prepareKeywordsTree(final Class<NodeType> nodeType, final SyntaxTreeInterface<NodeType> tree) {
		for(NodeType item : nodeType.getEnumConstants()) {
			tree.placeName(item.name(), item.ordinal(), item);
		}
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
	
	private static class FieldAndMethods {
		private final String	field;
		private final String	testMethod;
		private final String	skipMethod;
		private final String	parseMethod;
		
		private FieldAndMethods(String field, String testMethod, String skipMethod, String parseMethod) {
			this.field = field;
			this.testMethod = testMethod;
			this.skipMethod = skipMethod;
			this.parseMethod = parseMethod;
		}

		@Override
		public String toString() {
			return "FieldAndMethods [field=" + field + ", testMethod=" + testMethod + ", skipMethod=" + skipMethod + ", parseMethod=" + parseMethod + "]";
		}
	}
}
