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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.ExtendedBitCharSet;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
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
	private static final AsmWriter		AW2;
	private static final AtomicInteger	AI = new AtomicInteger();
	
	static {
		for(Predefines item : Predefines.values()) {
			PREDEFINED.placeName((CharSequence)item.name(), item);
		}
		
		try{AW = new AsmWriter(new ByteArrayOutputStream(), new PrintWriter(System.err));
		
			try(final InputStream	is = InternalUtils.class.getResourceAsStream("ruleBasedParserMacros.txt");
				final Reader		rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
				
				Utils.copyStream(rdr, AW);
			}
		} catch (IOException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}

		try{AW2 = new AsmWriter(new ByteArrayOutputStream(), new PrintWriter(System.err));
		
			try(final InputStream	is = InternalUtils.class.getResourceAsStream("ruleBasedParserMacros2.txt");
				final Reader		rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
				
				Utils.copyStream(rdr, AW2);
			}
		} catch (IOException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}
	}

	static <NodeType extends Enum<?>, Cargo> Class<RuleBasedParser<NodeType, Cargo>> buildRuleBasedParser1(final String className, final Class<NodeType> clazz, final String rule, final SimpleURLClassLoader loader, final boolean ignoreCase, final boolean addTrace) throws SyntaxException {
		final char[]									content = CharUtils.terminateAndConvert2CharArray(rule, EOF);
		final SyntaxTreeInterface<NodeType>				items = new AndOrTree<>(1,1);
		final SyntaxTreeInterface<Object>				lexTypes = new AndOrTree<>(AbstractBNFParser2.FIRST_FREE,1);
		final Lexema									lex = new Lexema();
		final int[]										temp = new int[2];
		final SyntaxNode<EntityType, SyntaxNode>		root = (SyntaxNode<EntityType, SyntaxNode>) AbstractBNFParser.TEMPLATE.clone();
		final List<SyntaxNode<EntityType, SyntaxNode>>	rules = new ArrayList<>();
		final GrowableCharArray<GrowableCharArray<?>>	gca = new GrowableCharArray<GrowableCharArray<?>>(false);
		final int										unique = AI.incrementAndGet();
		final String									packageName = className.contains(".") ? className.substring(0, className.lastIndexOf('.')) : "";
		final String									simpleName = className.contains(".") ? className.substring(className.lastIndexOf('.') + 1) : className;
		int		predefMask = 0;

		prepareKeywordsTree(clazz, items);
		
		int from = next(content, 0, items, temp, lex); 
		
		while (lex.type != Lexema.LexType.EOF) {
			final SyntaxNode<EntityType, SyntaxNode>	clone = (SyntaxNode<EntityType, SyntaxNode>)AbstractBNFParser.TEMPLATE.clone(); 
			
			from = parse(content, from, items, temp, lex, clone);
			extractLexSequences(clone, lexTypes);
			predefMask |= extractPredefinedMask(clone);
			rules.add(clone);
		}
		buildHead(unique, packageName, simpleName, clazz, predefMask, gca);
		buildCommonLex(unique, simpleName, clazz, rules, lexTypes, gca, ignoreCase, addTrace);
		buildTestSyntax(unique, clazz, rules, items, gca);
		buildSkipSyntax(unique, clazz, rules, gca);
		buildParseSyntax(unique, clazz, rules, gca);
		buildTail(unique, simpleName, gca);
		System.err.println("Result="+new String(gca.extract()));
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final AsmWriter	aw = AW2.clone(baos)) {
			
				aw.write(gca.extract());
				aw.flush();
			}
			return (Class<RuleBasedParser<NodeType, Cargo>>) loader.createClass(className, baos.toByteArray());
		} catch (IOException e) {
			throw new SyntaxException(0, 0, e.getLocalizedMessage(), e);
		}
	}	

	private static <NodeType extends Enum<?>> void buildHead(final int unique, final String packageName, final String simpleName, final Class<NodeType> clazz, final int predefMask, final GrowableCharArray<GrowableCharArray<?>> gca) {
		gca.append(" printImports ruleEnum=\"").append(clazz.getName()).append("\"");
		if (!packageName.isEmpty()) {
			gca.append(",package=\"").append(packageName).append("\"");
		}
		gca.append('\n');
		gca.append(" BNFParserHeader unique=").append(unique).append(",ruleEnum=\"").append(clazz.getName()).append("\",className=\"").append(simpleName).append("\",predefs=").append(predefMask).append('\n');
	}
	
	private static <NodeType extends Enum<?>> void buildCommonLex(final int unique, final String simpleName, final Class<NodeType> clazz, final List<SyntaxNode<EntityType, SyntaxNode>> rules, final SyntaxTreeInterface<Object> lexTypes, final GrowableCharArray<GrowableCharArray<?>> gca, final boolean ignoreCase, final boolean traceOn) {
		// TODO Auto-generated method stub
		final ExtendedBitCharSet	bcs = new ExtendedBitCharSet();

		collectChars4Switch(rules, bcs);
		final char[]	chars = ignoreCase ? buildLowerAndUpper(bcs.toArray()) : bcs.toArray();
		final char[]	formattedChars = toFormattedCharArray(chars);
		
		gca.append(" BNFParserLexHead unique=").append(unique).append(",className=\"").append(simpleName).append("\"\n");
		gca.append(" BNFParserLexSwitch unique=").append(unique).append(",chars=").append(formattedChars).append('\n');
		int seq = 0;
		for (char symbol : chars) {
			final List<char[]>	sequences = new ArrayList<>();
			
			collectSequences4Char(rules, symbol, sequences);
			if (sequences.size() == 1) {
				for(char[] item : sequences) {
					if (ignoreCase) {
						gca.append(" BNFParserLexSwitchItemIgnoreCase unique=").append(unique).append(",lowChars=\"").append(toFormattedStringArray(item))
							.append("\",seq=").append(seq).append('\n');
					}
					else {
						final long	lexType = lexTypes.seekName(item, 0, item.length);
						
						gca.append(" BNFParserLexSwitchItem unique=").append(unique).append(",chars=\"").append(toFormattedStringArray(item))
							.append("\",seq=").append(seq).append(",lexType=").append(lexType)
							.append(",inline=").append(requireInlineComparison(item, ignoreCase)).append('\n');
					}
					seq++;
				}
			}
			else {
				for(char[] item : sequences) {
					if (ignoreCase) {
						gca.append(" BNFParserLexSwitchItemIgnoreCase unique=").append(unique).append(",lowChars=\"").append(toFormattedStringArray(item))
							.append("\",seq=").append(seq).append('\n');
					}
					else {
						final long	lexType = lexTypes.seekName(item, 0, item.length);
						
						gca.append(" BNFParserLexSwitchItem unique=").append(unique).append(",chars=\"").append(toFormattedStringArray(item))
							.append("\",seq=").append(seq).append(",lexType=").append(lexType).append('\n');
					}
					seq++;
				}
			}
		}
		gca.append(" BNFParserLexSwitchDefault unique=").append(unique).append('\n');
		gca.append(" BNFParserLexTail unique=").append(unique);
		if (traceOn) {
			gca.append(",trace=true");
		}
		gca.append('\n');
	}
	
	private static char[] buildLowerAndUpper(final char[] source) {
		int	count = 0, displ = 0;
		
		for(char item : source) {
			if (Character.isLetter(item)) {
				count+= 2;
			}
			else {
				count++;
			}
		}
		if (count > source.length) {
			final char[]	result = new char[count];
	
			for(char item : source) {
				if (Character.isLetter(item)) {
					result[displ++] = Character.toUpperCase(item);
					result[displ++] = Character.toLowerCase(item);
				}
				else {
					result[displ++] = item;
				}
			}
			Arrays.sort(result);
			return result;
		}
		else {
			return source;
		}
	}

	private static boolean requireInlineComparison(final char[] content, final boolean ignoreCase) {
		if (content.length > 3) {
			return false;
		}
		else if (ignoreCase) {
			for(char item : content) {
				if (Character.isLetter(item)) {
					return false;
				}
			}
			return true;
		}
		else {
			return true;
		}
	}
	
	private static <NodeType extends Enum<?>> void buildTestSyntax(final int unique, final Class<NodeType> clazz, final List<SyntaxNode<EntityType, SyntaxNode>> rules, final SyntaxTreeInterface<NodeType> items, final GrowableCharArray<GrowableCharArray<?>> gca) {
		gca.append(" BNFParserTestHead unique=").append(unique).append(",ruleEnum=\"").append(clazz.getCanonicalName()).append("\"\n");
		gca.append(" BNFParserTestSwitch unique=").append(unique).append(",cardinality=").append(clazz.getEnumConstants().length).append('\n');
		for(NodeType item : clazz.getEnumConstants()) {
			gca.append(" BNFParserTestItem unique=").append(unique).append(",ordinal=").append(item.ordinal()).append('\n');
		}
		gca.append(" BNFParserTestDefault unique=").append(unique).append('\n');
		gca.append(" BNFParserTestTail unique=").append(unique).append('\n');
		for (SyntaxNode<EntityType, SyntaxNode> node : rules) {
			buildTestSyntaxRuleMethod(unique, node, items, gca);
		}
	}

	private static <NodeType extends Enum<?>> void buildTestSyntaxRuleMethod(final int unique, final SyntaxNode<EntityType, SyntaxNode> rule, final SyntaxTreeInterface<NodeType> items, final GrowableCharArray<GrowableCharArray<?>> gca) {
		// TODO Auto-generated method stub
		final int			ordinal = ((Enum)rule.cargo).ordinal();
		final Writer		wr = new PrintWriter(System.err);
		final List<Integer>	uniqueStack = new ArrayList<>();
		final AtomicInteger	uniqueLabel = new AtomicInteger();
		
		gca.append(" BNFParserTestMethodHead unique=").append(unique).append(",ordinal=").append(ordinal).append('\n');
		uniqueStack.add(0,uniqueLabel.incrementAndGet());
		SyntaxNodeUtils.walkDown(rule, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				switch (node.getType()) {
					case Case		:
						uniqueStack.add(0,uniqueLabel.incrementAndGet());
						gca.append(" BNFParserTestCaseBefore unique=").append(unique).append(",trueJump=").append(uniqueStack.get(1)).append('\n');
						break;
					case Sequence : case Char :
						gca.append(" BNFParserTestChar unique=").append(unique).append(",lexType=").append(node.value).append(",falseJump=").append(uniqueStack.get(0)).append('\n');
						break;
					case Name		:
						break;
					case Option		:
						uniqueStack.add(0,uniqueLabel.incrementAndGet());
						gca.append(" BNFParserTestOptionBefore unique=").append(unique).append(",falseJump=").append(uniqueStack.get(0)).append('\n');
						break;
					case Predefined	:
						gca.append(" BNFParserTestChar unique=").append(unique).append(",lexType=").append(-1-((Predefines)node.cargo).ordinal()).append(",falseJump=").append(uniqueStack.get(0)).append('\n');
						break;
					case Repeat		:
						uniqueStack.add(0,uniqueLabel.incrementAndGet());
						gca.append(" BNFParserTestRepeatBefore unique=").append(unique).append(",falseJump=").append(uniqueStack.get(0)).append('\n');
						break;
					case Repeat1	:
						uniqueStack.add(0,uniqueLabel.incrementAndGet());
						gca.append(" BNFParserTestRepeat1Before unique=").append(unique).append(",falseJump=").append(uniqueStack.get(0)).append('\n');
						break;
					case Root		:
						break;
					case Switch		:
						uniqueStack.add(0,uniqueLabel.incrementAndGet());
						gca.append(" BNFParserTestSwitchBefore unique=").append(unique).append(",trueJump=").append(uniqueStack.get(0)).append('\n');
						break;
					case Rule : case Detected :
						break;
					default:
						throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet"); 
				}
			}
			else {
				switch (node.getType()) {
					case Case		:
						gca.append(" BNFParserTestCaseAfter unique=").append(unique).append(",trueJump=").append(uniqueStack.remove(0)).append(",falseJumpTotal=").append(uniqueStack.get(0)).append('\n');
						break;
					case Name		:
						break;
					case Option		:
						gca.append(" BNFParserTestOptionAfter unique=").append(unique).append(",falseJump=").append(uniqueStack.remove(0)).append('\n');
						break;
					case Repeat		:
						gca.append(" BNFParserTestRepeatAfter unique=").append(unique).append(",falseJump=").append(uniqueStack.remove(0)).append('\n');
						break;
					case Repeat1	:
						gca.append(" BNFParserTestRepeat1After unique=").append(unique).append(",falseJump=").append(uniqueStack.remove(0)).append(",falseJumpTotal=").append(uniqueStack.get(0)).append('\n');
						break;
					case Root		:
						break;
					case Switch		:
						gca.append(" BNFParserTestSwitchAfter unique=").append(unique).append(",trueJump=").append(uniqueStack.remove(0)).append(",falseJumpTotal=").append(uniqueStack.get(0)).append('\n');
						break;
					case Sequence : case Char : case Predefined	: case Rule : case Detected :
						break;
					default:
						throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet"); 
				}
			}
			return ContinueMode.CONTINUE;
		});
		gca.append(" BNFParserTestMethodTail unique=").append(unique).append(",ordinal=").append(ordinal).append(",falseJump=").append(uniqueStack.remove(0)).append('\n');
	}
	
	private static <NodeType extends Enum<?>> void buildSkipSyntax(final int unique, final Class<NodeType> clazz, final List<SyntaxNode<EntityType, SyntaxNode>> rules, final GrowableCharArray<GrowableCharArray<?>> gca) {
		gca.append(" BNFParserSkipHead unique=").append(unique).append(",ruleEnum=\"").append(clazz.getCanonicalName()).append("\"\n");
		gca.append(" BNFParserSkipSwitch unique=").append(unique).append(",cardinality=").append(clazz.getEnumConstants().length).append('\n');
		for(NodeType item : clazz.getEnumConstants()) {
			gca.append(" BNFParserSkipItem unique=").append(unique).append(",ordinal=").append(item.ordinal()).append('\n');
			for (SyntaxNode<EntityType, SyntaxNode> node : rules) {
				if (node.getType() == item) {
					buildSkipSyntaxRule(unique, item.ordinal(), node, gca);
				}
			}
		}
		gca.append(" BNFParserSkipDefault unique=").append(unique).append('\n');
		gca.append(" BNFParserSkipTail unique=").append(unique).append('\n');
		for (SyntaxNode<EntityType, SyntaxNode> node : rules) {
			buildSkipSyntaxRuleMethod(unique, node, gca);
		}
	}

	private static <NodeType extends Enum<?>> void buildSkipSyntaxRule(final int unique, final int ordinal, final SyntaxNode<EntityType, SyntaxNode> rule, final GrowableCharArray<GrowableCharArray<?>> gca) {
		gca.append(" BNFParserSkipItemCall unique=").append(unique).append(",ordinal=").append(ordinal).append('\n');
	}

	private static <NodeType extends Enum<?>> void buildSkipSyntaxRuleMethod(final int unique, final SyntaxNode<EntityType, SyntaxNode> rule, final GrowableCharArray<GrowableCharArray<?>> gca) {
		// TODO Auto-generated method stub
		final int	ordinal = ((Enum)rule.cargo).ordinal();
		
		gca.append(" BNFParserSkipMethodHead unique=").append(unique).append(",ordinal=").append(ordinal).append('\n');
		walkRule(rule,(mode,node)->{});
		gca.append(" BNFParserSkipMethodTail unique=").append(unique).append(",ordinal=").append(ordinal).append('\n');
	}
	
	private static <NodeType extends Enum<?>> void buildParseSyntax(final int unique, final Class<NodeType> clazz, final List<SyntaxNode<EntityType, SyntaxNode>> rules, final GrowableCharArray<GrowableCharArray<?>> gca) {
		gca.append(" BNFParserParseHead unique=").append(unique).append(",ruleEnum=\"").append(clazz.getCanonicalName()).append("\"\n");
		gca.append(" BNFParserParseSwitch unique=").append(unique).append(",cardinality=").append(clazz.getEnumConstants().length).append('\n');
		for(NodeType item : clazz.getEnumConstants()) {
			gca.append(" BNFParserParseItem unique=").append(unique).append(",ordinal=").append(item.ordinal()).append('\n');
			for (SyntaxNode<EntityType, SyntaxNode> node : rules) {
				if (node.getType() == item) {
					buildParseSyntaxRule(unique, item.ordinal(), node, gca);
				}
			}
		}
		gca.append(" BNFParserParseDefault unique=").append(unique).append('\n');
		gca.append(" BNFParserParseTail unique=").append(unique).append('\n');
		for (SyntaxNode<EntityType, SyntaxNode> node : rules) {
			buildParseSyntaxRuleMethod(unique, node, gca);
		}
	}

	private static <NodeType extends Enum<?>> void buildParseSyntaxRule(final int unique, final int ordinal, final SyntaxNode<EntityType, SyntaxNode> rule, final GrowableCharArray<GrowableCharArray<?>> gca) {
		gca.append(" BNFParserParseItemCall unique=").append(unique).append(",ordinal=").append(ordinal).append('\n');
	}

	private static <NodeType extends Enum<?>> void buildParseSyntaxRuleMethod(final int unique, final SyntaxNode<EntityType, SyntaxNode> rule, final GrowableCharArray<GrowableCharArray<?>> gca) {
		// TODO Auto-generated method stub
		final int	ordinal = ((Enum)rule.cargo).ordinal();
		
		gca.append(" BNFParserParseMethodHead unique=").append(unique).append(",ordinal=").append(ordinal).append('\n');
		walkRule(rule,(mode,node)->{});
		gca.append(" BNFParserParseMethodTail unique=").append(unique).append(",ordinal=").append(ordinal).append('\n');
	}
	
	private static void buildTail(final int unique, final String simpleName, final GrowableCharArray<GrowableCharArray<?>> gca) {
		gca.append(" BNFParserTail unique=").append(unique).append(",className=\"").append(simpleName).append("\"\n");
	}

	private static void walkRule(final SyntaxNode<EntityType, SyntaxNode> root, final BiConsumer<NodeEnterMode, SyntaxNode<EntityType, SyntaxNode>> callback) {
		switch (root.type) {
			case Char : case Name : case Detected : case Predefined	: case Sequence	:
				callback.accept(NodeEnterMode.ENTER,root);
				break;
			case Option		:
				callback.accept(NodeEnterMode.ENTER, root);
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					walkRule(item, callback);
				}
				callback.accept(NodeEnterMode.EXIT, root);
				break;
			case Repeat		:
				callback.accept(NodeEnterMode.ENTER, root);
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					walkRule(item, callback);
				}
				callback.accept(NodeEnterMode.EXIT, root);
				break;
			case Repeat1		:
				callback.accept(NodeEnterMode.ENTER, root);
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					walkRule(item, callback);
				}
				callback.accept(NodeEnterMode.EXIT, root);
				break;
			case Root		:
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					walkRule(item, callback);
				}
				break;
			case Rule		:
				callback.accept(NodeEnterMode.ENTER, root);
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					walkRule(item, callback);
				}
				callback.accept(NodeEnterMode.EXIT, root);
				break;
			case Switch		:
				callback.accept(NodeEnterMode.ENTER, root);
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					walkRule(item, callback);
				}
				callback.accept(NodeEnterMode.EXIT, root);
				break;
			case Case		:
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					walkRule(item, callback);
				}
				break;
			default:
				throw new UnsupportedOperationException("Node type ["+root.type+"] is not supported yet");
		}
	}

	private static void collectChars4Switch(final List<SyntaxNode<EntityType, SyntaxNode>> rules, final BitCharSet bcs) {
		for (SyntaxNode<EntityType, SyntaxNode> item : rules) {
			SyntaxNodeUtils.walkDown(item, (m, n)->{
				if (m == NodeEnterMode.ENTER) {
					switch (n.getType()) {
						case Char : case Sequence :
							bcs.add(((char[])n.cargo)[0]);
							break;
						default:
							break;
					}
				}
				return ContinueMode.CONTINUE;
			});
		}
	}

	private static void collectSequences4Char(final List<SyntaxNode<EntityType, SyntaxNode>> rules, final char symbol, final List<char[]> sequences) {
		for (SyntaxNode<EntityType, SyntaxNode> item : rules) {
			SyntaxNodeUtils.walkDown(item, (m, n)->{
				if (m == NodeEnterMode.ENTER) {
					switch (n.getType()) {
						case Char : case Sequence :
							if (((char[])n.cargo)[0] == symbol) {
								sequences.add(((char[])n.cargo));
							}
							break;
						default :
							break;
					}
				}
				return ContinueMode.CONTINUE;
			});
		}
		sequences.sort((o1,o2)->o1.length-o2.length);
	}
	
	private static char[] toFormattedCharArray(final char[] array) {
		if (array.length == 0) {
			return new char[] {'{','}'};
		}
		else {
			final char[]	result = new char[4 * array.length + 1];
			
			for(int index = 0; index < array.length; index++) {
				result[4*index] = ',';
				result[4*index+1] = '\"';
				result[4*index+2] =array[index];
				result[4*index+3] = '\"';
			}
			result[0]='{';
			result[result.length-1] = '}';
			return result;
		}
	}

	private static char[] toFormattedStringArray(final char[] array) {
		return array;
	}
	
	static <NodeType extends Enum<?>, Cargo> Class<RuleBasedParser<NodeType, Cargo>> buildRuleBasedParser(final String className, final Class<NodeType> clazz, final String rule, final SimpleURLClassLoader loader, final boolean addTrace) throws SyntaxException {
		final char[]									content = CharUtils.terminateAndConvert2CharArray(rule, EOF);
		final SyntaxTreeInterface<NodeType>				items = new AndOrTree<>(1,1);
		final SyntaxTreeInterface<Object>				lexTypes = new AndOrTree<>(1,1);
		final Lexema									lex = new Lexema();
		final int[]										temp = new int[2];
		final SyntaxNode<EntityType, SyntaxNode>		root = (SyntaxNode<EntityType, SyntaxNode>) AbstractBNFParser.TEMPLATE.clone();
		final List<SyntaxNode<EntityType, SyntaxNode>>	rules = new ArrayList<>();

		prepareKeywordsTree(clazz, items);
		
		int from = next(content, 0, items, temp, lex); 
		
		while (lex.type != Lexema.LexType.EOF) {
			final SyntaxNode<EntityType, SyntaxNode>	clone = (SyntaxNode<EntityType, SyntaxNode>)AbstractBNFParser.TEMPLATE.clone(); 
			
			from = parse(content, from, items, temp, lex, clone);
			extractLexSequences(clone, lexTypes);
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
						buildRuleProcessing(className.substring(0, lastDot), className.substring(lastDot + 1), clazz, root, items, wr, addTrace);
					}
					else {
						buildRuleProcessing("", className, clazz, root, items, wr, addTrace);
					}
				}
				return (Class<RuleBasedParser<NodeType, Cargo>>) loader.createClass(className, baos.toByteArray());
			} catch (IOException e) {
				throw new SyntaxException(0, 0, e.getLocalizedMessage());
			}
		}
	}
	
	private static void extractLexSequences(final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<Object> lexTypes) {
		SyntaxNodeUtils.walkDown(root, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				switch (node.getType()) {
					case Char		:
						node.cargo = new char[] {(char)node.value};
						node.value = lexTypes.placeName((char[])node.cargo, 0, ((char[])node.cargo).length, node);
						break;
					case Sequence	:
						node.value = lexTypes.placeName((char[])node.cargo, 0, ((char[])node.cargo).length, node);
						break;
					default:
				}
			}
			return ContinueMode.CONTINUE;
		});
	}

	private static int extractPredefinedMask(final SyntaxNode<EntityType, SyntaxNode> root) {
		final int[]	mask = new int[] {0};
		
		SyntaxNodeUtils.walkDown(root, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				switch (node.getType()) {
					case Predefined		:
						mask[0] |= 1 << ((Enum)node.cargo).ordinal();
						break;
					default:
				}
			}
			return ContinueMode.CONTINUE;
		});
		return mask[0];
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
					root.cargo = keywords.getCargo(root.value);
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
				if (content[from + 1] == '*') {
					lex.type = Lexema.LexType.Repeat;
					return from + 2;
				}
				else if (content[from + 1] == '+') {
					lex.type = Lexema.LexType.Repeat1;
					return from + 2;
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
					throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), "Unknown predefined name ["+new String(content, temp[0], temp[1] - temp[0] + 1)+"]"); 
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
						throw new SyntaxException(SyntaxException.toRow(content, start), SyntaxException.toCol(content, start), "Unknown predefined name ["+new String(content, temp[0], temp[1] - temp[0] + 1)+"]"); 
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
					else if (lex.type == LexType.Repeat1) {
						subtree.col = from;
						subtree.type = EntityType.Repeat1;
						subtree.value = subtree.type.ordinal();
						subtree.cargo = null;
						list.add(subtree);
						from = next(content, from, keywords, temp, lex);
					}
					else if (lex.type == LexType.Close) {
						for (SyntaxNode item : subtree.children) {
							list.add(item);
						}
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
	
	static <NodeType extends Enum<?>> void printTree(final SyntaxNode<EntityType, ?> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		switch (root.type) {
			case Char		:
				wr.write("="+root.value+" '"+(root.cargo instanceof char[] ? new String((char[])root.cargo) : "")+"'");
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
				wr.write(")* ");
				break;
			case Repeat1		:
				wr.write("(");
				for (SyntaxNode<EntityType, SyntaxNode> item : root.children) {
					printTree(item, keywords, wr);
				}
				wr.write(")+ ");
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
				wr.write("="+root.value+" \""+(root.cargo instanceof char[] ? new String((char[])root.cargo) : "")+"\"");
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

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessing(final String packageName, final String className, final Class<NodeType> clazz, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr,final boolean addTrace) throws IOException, SyntaxException  {
		final Set<SyntaxNode<EntityType, ?>>									callLinks = new HashSet<>();
		final IdentityHashMap<SyntaxNode<EntityType, ?>, FieldAndMethods>		baseLevelProc = new IdentityHashMap<>();
		int[]		uniqueNumer = new int[] {0};
		
		for (SyntaxNode<EntityType, ?> item : root.children) {
			callLinks.add(item);
		}
		
		SyntaxNodeUtils.walkDown(root, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				final int	uniqueId = uniqueNumer[0]++;
				
L:				switch (node.type) {
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
						for (SyntaxNode<EntityType, ?> item : callLinks) {
							if (node.value == item.value) {
								baseLevelProc.put(node, new FieldAndMethods("rule"+keywords.getName(node.value), "testRule"+keywords.getName(node.value), "skipRule"+keywords.getName(node.value), "parseRule"+keywords.getName(node.value)));
								break L;
							}
						}
						throw new IllegalArgumentException(); 
					case Option		:
						baseLevelProc.put(node, new FieldAndMethods("option"+uniqueId, "testOption"+uniqueId, "skipOption"+uniqueId, "parseOption"+uniqueId));
						break;
					case Repeat		:
						baseLevelProc.put(node, new FieldAndMethods("repeat"+uniqueId, "testRepeat"+uniqueId, "skipRepeat"+uniqueId, "parseRepeat"+uniqueId));
						break;
					case Repeat1	:
						baseLevelProc.put(node, new FieldAndMethods("repeat1"+uniqueId, "testRepeat1"+uniqueId, "skipRepeat1"+uniqueId, "parseRepeat1"+uniqueId));
						break;
					case Root		:
						baseLevelProc.put(node, new FieldAndMethods("root"+uniqueId, "testRoot"+uniqueId, "skipRoot"+uniqueId, "parseRoot"+uniqueId));
						break;
					case Rule		:
						baseLevelProc.put(node, new FieldAndMethods("rule"+keywords.getName(node.value), "testRule"+keywords.getName(node.value), "skipRule"+keywords.getName(node.value), "parseRule"+keywords.getName(node.value)));
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
			wr.write(" printImports ruleEnum=\""+CompilerUtils.buildClassPath(clazz)+"\"\n");
		}
		else {
			wr.write(" printImports package=\""+packageName+"\",ruleEnum=\""+CompilerUtils.buildClassPath(clazz)+"\"\n");
		}
		buildRuleProcessingClass(className, root, keywords, wr);
		for (Entry<SyntaxNode<EntityType, ?>, FieldAndMethods> item : baseLevelProc.entrySet()) {
			buildRuleProcessingFields(className, root, item.getKey(), item.getValue(), keywords, wr);
		}
		wr.write(" beforeClinit className=\""+className+"\"\n");
		for (Entry<SyntaxNode<EntityType, ?>, FieldAndMethods> item : baseLevelProc.entrySet()) {
			buildRuleProcessingClinit(className, root, item.getKey(), item.getValue(), keywords, wr);
		}
		wr.write(" afterClinit className=\""+className+"\"\n");
		buildRuleProcessingConstructor(className, root, keywords, wr);
		buildRuleProcessingMethods(className, root, root.children, baseLevelProc, keywords, wr, addTrace);
		buildRuleProcessingRoot(className, root, keywords, baseLevelProc, wr);
		buildRuleProcessingTail(className, root, keywords, wr);
	}


	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingClass(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		wr.write(" beginClassDeclaration className=\""+className+"\"\n");
	}

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingFields(final String className, final SyntaxNode<EntityType, ?> root, final SyntaxNode<EntityType, ?> item, final FieldAndMethods names, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		if (item.type == EntityType.Sequence) {
			wr.write(" declareSequenceField className=\""+className+"\",fieldName=\""+names.field+"\"\n");
		}
	}

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingClinit(final String className, final SyntaxNode<EntityType, ?> root, final SyntaxNode<EntityType, ?> item, final FieldAndMethods names, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		if (item.type == EntityType.Sequence) {
			wr.write(" clinitPrepareSequence className=\""+className+"\",fieldName=\""+names.field+"\",value=\""+new String((char[])item.cargo)+"\"\n");
		}
	}

	private static <NodeType extends Enum<?>, Cargo>  void buildRuleProcessingConstructor(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		wr.write(" prepareConstructor className=\""+className+"\"\n");
	}

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingMethods(final String className, final SyntaxNode<EntityType, ?> root, final SyntaxNode<EntityType, ?>[] children, final IdentityHashMap<SyntaxNode<EntityType, ?>, FieldAndMethods> map, final SyntaxTreeInterface<NodeType> keywords, final Writer wr, final boolean addTrace) throws IOException, SyntaxException {
		final int[]	unique = new int[] {1};
		
		for (SyntaxNode<EntityType, ?> child : root.children) {
			final FieldAndMethods	names = map.get(child);
			
			wr.write(" prepareNameTestMethodStart className=\""+className+"\",name="+root.value+",methodName=\""+names.testMethod+"\"\n");
			
			if ("testRuleNegPart".equals(names.testMethod)) {
				System.err.println("testRuleNegPart");
			}
			if (addTrace) {
				wr.write(" traceExecution \"Start test: "+child.getType()+" "+keywords.getName(child.value)+"\"\n");
			}
			buildTestRuleProcessingMethods(className, child, map, keywords, wr, "", "falseLabel", unique, addTrace);
			wr.write(" prepareNameTestMethodEnd className=\""+className+"\",name="+root.value+",methodName=\""+names.testMethod+"\"\n");

			wr.write(" prepareNameSkipMethodStart className=\""+className+"\",name="+root.value+",methodName=\""+names.skipMethod+"\"\n");
			if (addTrace) {
				wr.write(" traceExecution \"Start skip: "+child.getType()+" "+keywords.getName(child.value)+"\"\n");
			}
			buildTestRuleProcessingMethods(className, child, map, keywords, wr, "", "falseLabel", unique, addTrace);
			wr.write(" prepareNameSkipMethodEnd className=\""+className+"\",name="+root.value+",methodName=\""+names.skipMethod+"\"\n");
			
			wr.write(" prepareNameParseMethodStart className=\""+className+"\",name="+root.value+",methodName=\""+names.parseMethod+"\",ruleClass=\""+CompilerUtils.buildClassPath(child.cargo.getClass())+"\",ruleField=\""+keywords.getName(child.value)+"\"\n");
			if (addTrace) {
				wr.write(" traceExecution \"Start parse: "+child.getType()+" "+keywords.getName(child.value)+"\"\n");
			}
			buildParseRuleProcessingMethods(className, child, map, keywords, wr, "", "falseLabel", unique, addTrace);
			wr.write(" prepareNameParseMethodEnd className=\""+className+"\",name="+root.value+",methodName=\""+names.parseMethod+"\"\n");
		}
	}	

	private static <NodeType extends Enum<?>, Cargo> void buildTestRuleProcessingMethods(final String className, final SyntaxNode<EntityType, ?> root, final IdentityHashMap<SyntaxNode<EntityType, ?>, FieldAndMethods> map, final SyntaxTreeInterface<NodeType> keywords, final Writer wr, final String trueLabel, final String falseLabel, final int[] unique, final boolean addTrace) throws IOException, SyntaxException {
		if (addTrace && root.getType() != EntityType.Detected) {
			wr.write(" traceExecution \"Test "+root.getType()+"\"\n");
		}
		if (root.getType().isCollection()) {
			for (SyntaxNode<EntityType, ?> child : root.children) {
				if (!child.getType().isCollection()) {
					buildTestRuleProcessingMethods(className, child, map, keywords, wr, trueLabel, falseLabel, unique, addTrace);
				}
				else {
					final FieldAndMethods	names = map.get(child);
					
					switch (child.getType()) {
						case Case		:
							buildTestRuleProcessingMethods(className, child, map, keywords, wr, trueLabel, falseLabel, unique, addTrace);
							break;
						case Option		:
							final String	trueOption = trueLabel.isEmpty() ? "" : "trueOption"+unique[0], falseOption = "falseOption"+unique[0];
							
							unique[0]++;
							wr.write(" prepareNameTestMethodOptionStart \n");
							for (SyntaxNode<EntityType, ?> option : child.children) {
								buildTestRuleProcessingMethods(className, option, map, keywords, wr, trueOption, falseOption, unique, addTrace);
							}					
							if (trueLabel.isEmpty()) {
								wr.write(" prepareNameTestMethodOptionEnd falseLabel=\""+falseOption+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
							}
							else {
								wr.write(" prepareNameTestMethodOptionEnd trueLabel=\""+trueOption+"\",falseLabel=\""+falseOption+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
							}
							break;
						case Repeat		:
							final String	trueRepeat = "trueRepeat"+unique[0], falseRepeat = "falseRepeat"+unique[0];
							
							unique[0]++;
							wr.write(" prepareNameTestMethodRepeatStart trueLabel=\""+trueRepeat+"\"\n");
							for (SyntaxNode<EntityType, ?> option : child.children) {
								buildTestRuleProcessingMethods(className, option, map, keywords, wr, "", falseRepeat, unique, addTrace);
							}					
							wr.write(" prepareNameTestMethodRepeatEnd trueLabel=\""+trueRepeat+"\",falseLabel=\""+falseRepeat+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
							break;
						case Repeat1	:
							final String	true1Repeat = "true1Repeat"+unique[0], false1Repeat = "false1Repeat"+unique[0];
							
							unique[0]++;
							wr.write(" prepareNameTestMethodRepeat1Start trueLabel=\""+true1Repeat+"\"\n");
							for (SyntaxNode<EntityType, ?> option : child.children) {
								buildTestRuleProcessingMethods(className, option, map, keywords, wr, "", false1Repeat, unique, addTrace);
							}					
							wr.write(" prepareNameTestMethodRepeat1End trueLabel=\""+true1Repeat+"\",falseLabel=\""+false1Repeat+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
							break;
						case Switch		:
							final String	trueSwitch = trueLabel.isEmpty() ? "trueSwitch"+unique[0] : trueLabel;
//							String	falseSwitch = "falseSwitch"+unique[0];
							
							unique[0]++;
							for (SyntaxNode<EntityType, ?> option : child.children) {
								buildTestRuleProcessingMethods(className, option, map, keywords, wr, trueSwitch, "", unique, addTrace);
//								buildTestRuleProcessingMethods(className, option, map, keywords, wr, trueSwitch, falseSwitch, unique, addTrace);
//								wr.write(falseSwitch+":\n");
//								unique[0]++;
//								falseSwitch = "falseSwitch"+unique[0];
							}					
							wr.write(" prepareNameTestMethodSwitchEnd trueLabel=\""+trueSwitch+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
							break;
						case Detected	:
							break;
						default:
							throw new UnsupportedOperationException("Node type ["+child.getType()+"] is not supported yet");
					}
				}
			}		
		}
		else {
			final FieldAndMethods	names = map.get(root);
			
			switch (root.getType()) {
				case Char		:
					wr.write(" prepareNameTestMethodChar value="+root.value+",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\",addTrace="+addTrace+"\n");
					break;
				case Name		:
					wr.write(" prepareNameTestMethodRule methodName=\""+names.testMethod+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
					break;
				case Predefined	:
					wr.write(" prepareNameTestMethodPredefined predefinedName=\""+((Predefines)root.cargo).name()+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\",addTrace="+addTrace+"\n");
					break;
				case Sequence	:
					wr.write(" prepareNameTestMethodSequence fieldName=\""+names.field+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\",addTrace="+addTrace+"\n");
					break;
				case Detected	:
					break;
				default:
					throw new UnsupportedOperationException("Node type ["+root.type+"] is not supported yet");
			}
		}
	}
	
	private static <NodeType extends Enum<?>, Cargo> void buildParseRuleProcessingMethods(final String className, final SyntaxNode<EntityType, ?> root, final IdentityHashMap<SyntaxNode<EntityType, ?>, FieldAndMethods> map, final SyntaxTreeInterface<NodeType> keywords, final Writer wr, final String trueLabel, final String falseLabel, final int[] unique, final boolean addTrace) throws IOException, SyntaxException {
		if (addTrace) {
			wr.write(" traceExecution \"Parse "+root.getType()+"\"\n");
		}
		if (root.getType().isCollection()) {
			for (SyntaxNode<EntityType, ?> child : root.children) {
				if (!child.getType().isCollection()) {
					buildParseRuleProcessingMethods(className, child, map, keywords, wr, trueLabel, falseLabel, unique, addTrace);
				}
				else {
					final FieldAndMethods	names = map.get(child);
					
					switch (child.getType()) {
						case Case		:
							buildParseRuleProcessingMethods(className, child, map, keywords, wr, trueLabel, falseLabel, unique, addTrace);
							break;
						case Option		:
							final String	trueOption = trueLabel.isEmpty() ? "" : "trueOption"+unique[0], falseOption = "falseOption"+unique[0];
							
							unique[0]++;
							wr.write(" prepareNameTestMethodOptionStart \n");
							for (SyntaxNode<EntityType, ?> option : child.children) {
								buildParseRuleProcessingMethods(className, option, map, keywords, wr, trueOption, falseOption, unique, addTrace);
							}					
							if (trueLabel.isEmpty()) {
								wr.write(" prepareNameTestMethodOptionEnd falseLabel=\""+falseOption+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
							}
							else {
								wr.write(" prepareNameTestMethodOptionEnd trueLabel=\""+trueOption+"\",falseLabel=\""+falseOption+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
							}
							break;
						case Repeat		:
							final String	trueRepeat = "trueRepeat"+unique[0], falseRepeat = "falseRepeat"+unique[0];
							
							unique[0]++;
							wr.write(" prepareNameTestMethodRepeatStart trueLabel=\""+trueRepeat+"\"\n");
							for (SyntaxNode<EntityType, ?> option : child.children) {
								buildParseRuleProcessingMethods(className, option, map, keywords, wr, "", falseRepeat, unique, addTrace);
							}					
							wr.write(" prepareNameTestMethodRepeatEnd trueLabel=\""+trueRepeat+"\",falseLabel=\""+falseRepeat+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
							break;
						case Repeat1	:
							final String	true1Repeat = "trueRepeat"+unique[0], false1Repeat = "falseRepeat"+unique[0];
							
							unique[0]++;
							wr.write(" prepareNameTestMethodRepeat1Start trueLabel=\""+true1Repeat+"\"\n");
							for (SyntaxNode<EntityType, ?> option : child.children) {
								buildParseRuleProcessingMethods(className, option, map, keywords, wr, "", false1Repeat, unique, addTrace);
							}					
							wr.write(" prepareNameTestMethodRepeat1End trueLabel=\""+true1Repeat+"\",falseLabel=\""+false1Repeat+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
							break;
						case Switch		:
							final String	trueSwitch = trueLabel.isEmpty() ? "trueSwitch"+unique[0] : trueLabel;
							String	falseSwitch = "false"+unique[0];
							
							unique[0]++;
							for (SyntaxNode<EntityType, ?> option : child.children) {
								buildParseRuleProcessingMethods(className, option, map, keywords, wr, "", falseSwitch, unique, addTrace);
								wr.write("	goto  "+trueSwitch+"\n");
								wr.write(falseSwitch+":\n");
								unique[0]++;
								falseSwitch = "false"+unique[0];
							}					
							if (trueLabel.isEmpty()) {
								wr.write(" prepareNameTestMethodSwitchEnd trueLabel=\""+trueSwitch+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
							}
							else {
								wr.write(" prepareNameTestMethodSwitchEnd trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
							}
							break;
						case Detected	:
							wr.write(" prepareNameParseMethodDetector className=\""+className+"\",methodName=\""+names.parseMethod+"\",item="+root.value+"\n");
							break;
						case Rule :
							buildParseRuleProcessingMethods(className, child, map, keywords, wr, trueLabel, falseLabel, unique, addTrace);
							break;
						default:
							throw new UnsupportedOperationException("Node type ["+child.getType()+"] is not supported yet");
					}
				}
			}		
		}
		else {
			final FieldAndMethods	names = map.get(root);
			
			switch (root.getType()) {
				case Char		:
					wr.write(" prepareNameTestMethodChar value="+root.value+",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\",addTrace="+addTrace+"\n");
					break;
				case Name		:
					wr.write(" prepareNameParseMethodRule methodName=\""+names.parseMethod+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
					break;
				case Predefined	:
					wr.write(" prepareNameParseMethodPredefined predefinedName=\""+((Predefines)root.cargo).name()+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\"\n");
					break;
				case Sequence	:
					wr.write(" prepareNameTestMethodSequence fieldName=\""+names.field+"\",trueJump=\""+trueLabel+"\",falseJump=\""+falseLabel+"\",addTrace="+addTrace+"\n");
					break;
				case Detected	:
					wr.write(" prepareNameParseMethodDetector className=\""+className+"\",methodName=\""+names.parseMethod+"\",item="+root.value+"\n");
					break;
				default:
					throw new UnsupportedOperationException("Node type ["+root.type+"] is not supported yet");
			}
		}
	}	

	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingRoot(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final IdentityHashMap<SyntaxNode<EntityType, ?>, FieldAndMethods> map, final Writer wr) throws IOException {
		final FieldAndMethods	first = map.get(root.children[0]);
		
		wr.write(" prepareTestInternal className=\""+className+"\",testMethod=\""+first.testMethod+"\"\n");
		wr.write(" prepareSkipInternal className=\""+className+"\",testMethod=\""+first.testMethod+"\",skipMethod=\""+first.skipMethod+"\"\n");
		wr.write(" prepareParseInternal className=\""+className+"\",testMethod=\""+first.testMethod+"\",parseMethod=\""+first.parseMethod+"\"\n");
	}
	
	private static <NodeType extends Enum<?>, Cargo> void buildRuleProcessingTail(final String className, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> keywords, final Writer wr) throws IOException {
		wr.write(" endClassDeclaration className=\""+className+"\"\n");
	}

	static <NodeType extends Enum<?>, Cargo> Class<RuleBasedParser<NodeType, Cargo>> buildRuleBasedParserClass(final String className, final Class<NodeType> clazz, final SyntaxNode<EntityType, SyntaxNode> root, final SyntaxTreeInterface<NodeType> items, final SimpleURLClassLoader loader, final boolean addTrace) throws SyntaxException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			try(final AsmWriter			wr = AW.clone(baos)) {
				
				buildRuleProcessing("", className, clazz, new SyntaxNode<>(0, 0, EntityType.Root, 0, null, root), items, wr, addTrace);
			}
			
			return (Class<RuleBasedParser<NodeType, Cargo>>) loader.createClass(className, baos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
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
			Name, Predefined, Open, Close, OpenB, CloseB, OpenF, CloseF, Alter, Ergo, Char, Sequence, Colon, Repeat, Repeat1, NL, EOF 
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
