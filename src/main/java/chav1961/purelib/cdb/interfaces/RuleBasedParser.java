package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.cdb.intern.Predefines;

/**
 * <p>This interface describes rule-based parser automatically built by advanced BNF notation. Current implementation of the interface supports a set of rules with
 * the given format:
 * &lt;left&gt;::=&lt;right&gt;\n
 * The same first rule in the rule set is a <b>root</b>rule. Name in the left part of the rules must be one of the enumeration items name (see &lt;NodeType&gt; parameter).
 * Right part of the rule can contain sequence of the items: 
 * <ul>
 * <li>'content' - sequence of the symbols. Must be presents in the input content <b>'as-is'</b></li>
 * <li>@Name - predefined syntax constructs (see below)</li>
 * <li>{?|?|?} - alternatives. One of this must presents in the input content</li>
 * <li>[?] - options. This one can or can not presents in the input content</li>
 * <li>(?)* - repeats. This one can or can not presents in the input content many times</li>
 * <li>(?)+ - mandatory repeats. This one can presents in the input content at least one time</li>
 * <li>&lt;Name&gt; - reference to another rule in the rule set</b></li>
 * <li>'content':&lt;Name&gt; - marker. Creates a special <i>marker</i> node in the in the syntax tree, if the given sequence presents in the input content</li>
 * </ul>
 * <p>Predefined syntax constructs can contain:</p>
 * <ul>
 * <li>@Name - variable name in the traditional format. Will insert node into syntax tree with type={@linkplain Predefines#Name} and value=name id n the syntax tree (see &lt;Cargo&gt; parameter and {@linkplain #getNamesTree()} method</li>
 * <li>@FixedNumber - unsigned (long) integer value. Will insert node into syntax tree with type={@linkplain Predefines#FixedNumber} and value=integer parsed</li>
 * <li>@FloatNumber - unsigned (double) float value. Will insert node into syntax tree with type={@linkplain Predefines#FixedNumber} and value=double parsed. To access double value, use {@linkplain Double#longBitsToDouble(long)} method</li>
 * <li>@QuotedString - string inside apostrophes. String can contain traditional escape sequences. Will insert node into syntax tree with type={@linkplain Predefines#QuotedString} and cargo=char[] of the string content</li>
 * <li>@DoubleQuotedString - string inside double quotes. String can contain traditional escape sequences. Will insert node into syntax tree with type={@linkplain Predefines#DoubleQuotedString} and cargo=char[] of the string content</li>
 * <li>@Empty - nothing. It doesn't require anything in the input content, but inserts a node in the syntax tree. Node type will be {@linkplain Predefines#Empty}</li>
 * </ul>
 * @param <NodeType> any enumeration to mark syntax tree nodes built 
 * @param <Cargo> any associated content in the syntax tree
 * @see Predefines
 * @since 0.0.6
 */
public interface RuleBasedParser<NodeType extends Enum<?>, Cargo> {
	/**
	 * <p>Get names tree. All parsed names (see {@linkplain Predefines#Name}) will be placed in it.</p> 
	 * @return Syntax tree with names. Can't be null
	 */
	SyntaxTreeInterface<Cargo> getNamesTree();

	boolean test(char [] content, int from) throws SyntaxException;
	
	/**
	 * <p>Skip input content according to rules (for example, skip 'arithmetic expression').</p> 
	 * @param content input content to process 
	 * @param from start position to process
	 * @return position after skipping. Can be used in subsequent calls
	 * @throws SyntaxException on any syntax errors in the input content
	 */
	int skip(char [] content, int from) throws SyntaxException;
	
	/**
	 * <p>Parse input content and build syntax tree</p>
	 * @param content input content to process. Can'tbe null
	 * @param from start position to process
	 * @param root root of the syntax node tree. It's children will contain tree built after parsing 
	 * @return position after parsing. Can be used in subsequent calls
	 * @throws SyntaxException on any syntax errors in the input content
	 */
	default int parse(char [] content, int from, SyntaxNode<NodeType, SyntaxNode> root) throws SyntaxException {
		return parse(content, from, getNamesTree(), root);
	}
	
	/**
	 * <p>Parse input content.</p>
	 * @param content input content to process. Can'tbe null
	 * @param from start position to process
	 * @param names syntax tree to store parsed names into. Can'tbe null
	 * @return position after parsing. Can be used in subsequent calls
	 * @throws SyntaxException on any syntax errors in the input content
	 */
	int parse(char [] content, int from, SyntaxTreeInterface<Cargo> names) throws SyntaxException;
	
	/**
	 * <p>Parse input content and build syntax tree</p>
	 * @param content input content to process. Can'tbe null
	 * @param from start position to process
	 * @param names syntax tree to store parsed names into. Can'tbe null
	 * @param root root of the syntax node tree. It's children will contain tree built after parsing 
	 * @return position after parsing. Can be used in subsequent calls
	 * @throws SyntaxException on any syntax errors in the input content
	 */
	int parse(char [] content, int from, SyntaxTreeInterface<Cargo> names, SyntaxNode<NodeType, SyntaxNode> root) throws SyntaxException;
}
