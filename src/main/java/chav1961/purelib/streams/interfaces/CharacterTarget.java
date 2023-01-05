package chav1961.purelib.streams.interfaces;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PrintingException;

/**
 * <p>This interface describes char target to use in the different printers.</p>
 * @see chav1961.purelib.streams.chartarget
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.6
 */
public interface CharacterTarget extends Appendable {
	/**
	 * <p>Put symbol into the target</p>
	 * @param symbol symbol to put
	 * @return self
	 * @throws PrintingException any printing exceptions
	 */
	CharacterTarget put(char symbol) throws PrintingException;
	
	/**
	 * <p>Put char array into the target</p>
	 * @param symbols char array to put
	 * @return self
	 * @throws PrintingException any printing exceptions
	 */
	CharacterTarget put(char[] symbols) throws PrintingException;
	
	/**
	 * <p>Put part of char array into the target</p>
	 * @param symbols char array to put
	 * @param from start position to put
	 * @param to end position to put
	 * @return self
	 * @throws PrintingException any printing exceptions
	 */
	CharacterTarget put(char[] symbols, int from, int to) throws PrintingException;
	
	/**
	 * <p>Put string into the target</p>
	 * @param source source to put into the target
	 * @return self
	 * @throws PrintingException any printing exceptions
	 */
	CharacterTarget put(String source) throws PrintingException;
	
	/**
	 * <p>Put part of string into the target</p>
	 * @param source source to put into the target
	 * @param from start position to put
	 * @param to end position to put
	 * @return self
	 * @throws PrintingException any printing exceptions
	 */
	CharacterTarget put(String source, int from, int to) throws PrintingException;
	
	/**
	 * <p>Get count of the total readed data</p>
	 * @return count of the total readed data
	 */
	int totalWritten();
	
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
	
	/**
	 * <p>Reset the character target to refill it</p>
	 * @throws EnvironmentException if the class not supports this method
	 */
	void reset() throws EnvironmentException;
}
