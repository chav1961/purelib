package chav1961.purelib.cdb.interfaces;

import java.util.List;

import chav1961.purelib.basic.exceptions.SyntaxException;

/**
 * <p>This interface describes lexema processors.</p>
 * @param <LexType> Lexema type enumeration.
 * @param <LI> Lexema interface descriptor.
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public interface LexProcessor<LexType extends Enum<?>, LI extends LexemaInterface<LexType>> {
	/**
	 * <p>Process source character array and build list of lexemas from it</p>
	 * @param source source to process. Can't be null.
	 * @param from start position to process lexemas. Must be inside the 'source' parameter.
	 * @param lexemas list to get parsed lexemas. Can't be null.
	 * @return end position after processing lexemas.
	 * @throws SyntaxException on any syntax errors.
	 */
	int process(char[] source, int from, List<LI> lexemas) throws SyntaxException;
}
