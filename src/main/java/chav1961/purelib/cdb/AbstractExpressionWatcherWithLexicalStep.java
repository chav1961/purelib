package chav1961.purelib.cdb;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.interfaces.LexemaInterface;

public abstract class AbstractExpressionWatcherWithLexicalStep<LexType extends Enum<?>, Lexema extends LexemaInterface<LexType>, Node extends SyntaxNode<LexType,?>> extends AbstractExpressionWatcher<LexType, Node> {
	protected final Class<Lexema>	lexClass;
	
	protected AbstractExpressionWatcherWithLexicalStep(final Class<LexType> lexTypeClass, final Class<Lexema> lexClass) {
		super(lexTypeClass);
		this.lexClass = lexClass;
	}

	@Override
	protected int parse(final char[] expression, final int from, final Node root) throws SyntaxException, NullPointerException, IllegalArgumentException {
		final List<Lexema>	lexemas = new ArrayList<>();
		final int			result = parse(expression,from,lexemas);
		final Lexema[]		lexList = lexemas.toArray((Lexema[])Array.newInstance(lexClass, lexemas.size()));
		final int			theEnd = parse(lexList,0,root);
		
		if (theEnd < lexList.length-1) {
			throw new SyntaxException(lexList[theEnd].getRow(),lexList[theEnd].getColumn(),"Unparsed tail in the lexemas"); 
		}
		else {
			return result;
		}
	}

	protected abstract int parse(char[] expression, int from, List<Lexema> lexemas) throws SyntaxException, NullPointerException, IllegalArgumentException;
	protected abstract int parse(Lexema[] lexemas, int from, Node root) throws SyntaxException, NullPointerException, IllegalArgumentException;
}
