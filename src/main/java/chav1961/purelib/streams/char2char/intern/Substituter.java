package chav1961.purelib.streams.char2char.intern;

import chav1961.purelib.basic.exceptions.SyntaxException;

interface Substituter {
	int match(final char[] data, final int from) throws SyntaxException;
	String substitute();
}