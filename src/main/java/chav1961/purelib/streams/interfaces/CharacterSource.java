package chav1961.purelib.streams.interfaces;

import chav1961.purelib.basic.exceptions.SyntaxException;

/**
 * <p>This interface describes char source to use in the parsers</p>
 * @author chav1961
 *
 */
public interface CharacterSource {
	public static char		EOF = 0;

	
	/**
	 * <p>Get next char from input stream</p>
	 * @return next char from input stream
	 * @throws SyntaxException
	 */
	char next() throws SyntaxException;
	
	/**
	 * <p>Get last char readed</p>
	 * @return last char readed or EOF if no next call before yet
	 */
	char last();
	
	/**
	 * <p>Get count of the total readed data</p>
	 * @return count of the total readed data
	 */
	int totalReaded();
	
	/**
	 * <p>Get actual row of the content</p>
	 * @return 1-based actual row of the content. If can't define, 0 will be returned
	 */
	int atRow();
	
	/**
	 * <p>Get actual column of the content</p>
	 * @return 1-based actual column of the content. If can't define, 0 will be returned
	 */
	int atColumn();
}
