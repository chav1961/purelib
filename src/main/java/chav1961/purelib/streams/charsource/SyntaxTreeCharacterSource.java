package chav1961.purelib.streams.charsource;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.interfaces.CharacterSource;

/**
 * <p>This class implements {@link CharacterSource} interface by syntax trees</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @param <T> syntax tree cargo type
 */
public class SyntaxTreeCharacterSource<T> implements CharacterSource {
	private final char[]	content;
	char					last = ' ';
	int						index;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param tree syntax tree to get content from
	 * @param stringId string id in the syntax tree 
	 * @throws NullPointerException when syntax tree reference is null
	 * @throws IllegalArgumentException when string id is negative of is missing in the syntax tree
	 */
	public SyntaxTreeCharacterSource(final SyntaxTreeInterface<T> tree, final long stringId) throws NullPointerException, IllegalArgumentException {
		if (tree == null) {
			throw new NullPointerException("Syntax tree can't be null");
		}
		else if (stringId < 0) {
			throw new IllegalArgumentException("String id ["+stringId+"] can't be negative");
		}
		else if (!tree.contains(stringId)) {
			throw new IllegalArgumentException("String id ["+stringId+"] is missing in the syntax tree");
		}
		else {
			content = new char[tree.getNameLength(stringId)];
			tree.getName(stringId,content,0);
		}
	}
	
	@Override
	public char next() throws ContentException {
		if (index >= content.length) {
			return last = CharacterSource.EOF;
		}
		else {
			return last = content[index++];
		}
	}

	@Override
	public char last() {
		return last;
	}

	@Override
	public void back() throws ContentException {
		if (index > 0) {
			index--;
		}
	}

	@Override
	public int totalReaded() {
		return index;
	}

	@Override
	public int atRow() {
		return SyntaxException.toRow(content,index);
	}

	@Override
	public int atColumn() {
		return SyntaxException.toCol(content,index);
	}

	@Override
	public void reset() throws EnvironmentException {
		index = 0;
		last = ' ';
	}

}
