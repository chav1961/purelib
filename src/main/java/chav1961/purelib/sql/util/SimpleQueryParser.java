/*package chav1961.purelib.sql.util;

import java.io.InputStream;

import chav1961.purelib.basic.XMLBasedParser;
import chav1961.purelib.basic.exceptions.SyntaxException;

class SimpleQueryParser extends XMLBasedParser<SimpleQueryExecutor.LexType,SimpleQueryExecutor.LexSubtype,SimpleQueryExecutor.Action,SimpleQueryExecutor.LexDesc>{
	SimpleQueryParser(final InputStream is) throws SyntaxException {
		super(is,SimpleQueryExecutor.LexType.class,SimpleQueryExecutor.LexSubtype.class,SimpleQueryExecutor.Action.class);
	}

	@Override
	public <Action> Object action(final Object content, final Action actionType, final String[] parameters) {
		return content;
	}

	@Override
	public int builtin(final Object content, final String type, final SimpleQueryExecutor.LexDesc[] lexList, final int current) {
		return current;
	}
}
*/