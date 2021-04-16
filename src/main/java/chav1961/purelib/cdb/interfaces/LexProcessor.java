package chav1961.purelib.cdb.interfaces;

import java.util.List;

import chav1961.purelib.basic.exceptions.SyntaxException;

public interface LexProcessor<LexType extends Enum<?>, LI extends LexemaInterface<LexType>> {
	int process(char[] source, int from, List<LI> lexemas) throws SyntaxException;
}
