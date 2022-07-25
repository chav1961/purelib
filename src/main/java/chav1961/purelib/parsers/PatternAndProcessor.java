package chav1961.purelib.parsers;

import chav1961.purelib.basic.exceptions.SyntaxException;

interface PatternAndProcessor {
	@FunctionalInterface
	public interface OutputProcessor {
		void process(Object... parameters) throws SyntaxException;
	}
	
	char[] getKeyword();
	int process(char[] data, int from, OutputProcessor writer) throws SyntaxException;
}
