package chav1961.purelib.streams.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

/**
 * <p>This interface describes char source to use in the different parsers.</p>
 * @see chav1961.purelib.streams.charsource
 * @author chav1961
 * @since 0.0.1
 *
 */
public interface CharacterSource {
	/**
	 * <p>The End-Of-Stream constant</p>
	 */
	char	EOF = 0;
	
	/**
	 * <p>Get next char from input stream</p>
	 * @return next char from input stream or {@link #EOF} if input stream was exhausted
	 * @throws ContentException any content exceptions 
	 */
	char next() throws ContentException;
	
	/**
	 * <p>Get last char read</p>
	 * @return last char read or {@link #EOF} if no {@link #next()} call before yet
	 */
	char last();
	
	/**
	 * <p>Get count of the total data read</p>
	 * @return count of the total data read 
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
