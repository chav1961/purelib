package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;

/**
 * <root>::=(<rule>'\n')...
 * <rule>::='<'@Name'>''::='<right>
 * <right>::={@Empty|<term><right>}
 * <term>::={<mandatory>|'['<right>']'|'{'(<right>'|')...<right>'}'}
 * <mandatory>::={'<'@Name'>'|@FixedNumber|@FloatNumber|@Quotedstring}
 */
public interface RuleBasedParser<NodeType extends Enum<?>, Cargo> {
	SyntaxTreeInterface<Cargo> getNamesTree();
	
	default int skip(char [] content, int from) throws SyntaxException {
		return skip(content, from, getNamesTree());
	}
	
	int skip(char [] content, int from, SyntaxTreeInterface<Cargo> names) throws SyntaxException;
	
	default int parse(char [] content, int from, SyntaxNode<NodeType, SyntaxNode> root) throws SyntaxException {
		return parse(content, from, getNamesTree(), root);
	}
	
	int parse(char [] content, int from, SyntaxTreeInterface<Cargo> names) throws SyntaxException;
	int parse(char [] content, int from, SyntaxTreeInterface<Cargo> names, SyntaxNode<NodeType, SyntaxNode> root) throws SyntaxException;
}
