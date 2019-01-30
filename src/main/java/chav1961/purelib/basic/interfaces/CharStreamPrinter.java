package chav1961.purelib.basic.interfaces;

import java.io.Closeable;
import java.io.Flushable;
import java.io.PrintStream;
import java.io.PrintWriter;

import chav1961.purelib.basic.exceptions.PrintingException;

/**
 * <p>This interface contains useful methods to print different content to character stream. It's functionality is similar to 
 * standard Java {@linkplain PrintStream} and {@linkplain PrintWriter} interface. This interface is oriented to use in the chained
 * operations</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @see PrintStream
 * @see PrintWriter
 * @since 0.0.3
 * @param T - returned type to use for chained operations 
 */

public interface CharStreamPrinter<T> extends Flushable, Closeable {
	/**
	 * <p>Print (CR)NL due to operation system settings</p>
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println() throws PrintingException;
	
	/**
	 * <p>Print single char</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T print(char data) throws PrintingException;
	
	/**
	 * <p>Print single char and (CR)NL due to operation system settings</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println(char data) throws PrintingException;
	
	/**
	 * <p>Print character representation of byte</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T print(byte data) throws PrintingException;
	
	/**
	 * <p>Print character representation of byte and (CR)NL due to operation system settings</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println(byte data) throws PrintingException;
	
	/**
	 * <p>Print character representation of short</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T print(short data) throws PrintingException;
	
	/**
	 * <p>Print character representation of short and (CR)NL due to operation system settings</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println(short data) throws PrintingException;
	
	/**
	 * <p>Print character representation of integer</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T print(int data) throws PrintingException;
	
	/**
	 * <p>Print character representation of integer and (CR)NL due to operation system settings</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println(int data) throws PrintingException;
	
	/**
	 * <p>Print character representation of long</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T print(long data) throws PrintingException;
	
	/**
	 * <p>Print character representation of long and (CR)NL due to operation system settings</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println(long data) throws PrintingException;
	
	/**
	 * <p>Print character representation of float</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T print(float data) throws PrintingException;
	
	/**
	 * <p>Print character representation of float and (CR)NL due to operation system settings</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println(float data) throws PrintingException;
	
	/**
	 * <p>Print character representation of double</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T print(double data) throws PrintingException;
	
	/**
	 * <p>Print character representation of double and (CR)NL due to operation system settings</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println(double data) throws PrintingException;
	
	/**
	 * <p>Print character representation of boolean</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T print(boolean data) throws PrintingException;
	
	/**
	 * <p>Print character representation of boolean and (CR)NL due to operation system settings</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println(boolean data) throws PrintingException;
	
	/**
	 * <p>Print string. Null prints as 'null'</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T print(String data) throws PrintingException;
	
	/**
	 * <p>Print string and (CR)NL due to operation system settings. Null prints as 'null'</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println(String data) throws PrintingException;
	
	/**
	 * <p>Print piece of string. Null prints as 'null'</p> 
	 * @param data data to print
	 * @param from piece beginning
	 * @param len piece length
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @StringIndexOutOfBoundsException if piece of data outside the string area
	 */
	T print(String data, int from, int len) throws PrintingException, StringIndexOutOfBoundsException;
	
	/**
	 * <p>Print piece of string and (CR)NL due to operation system settings. Null prints as 'null'</p>
	 * @param data data to print
	 * @param from piece beginning
	 * @param len piece length
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @StringIndexOutOfBoundsException if piece of data outside the string area
	 */
	T println(String data, int from, int len) throws PrintingException, StringIndexOutOfBoundsException;
	
	/**
	 * <p>Print content of the character array. Null prints as 'null'</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T print(char[] data) throws PrintingException;
	
	/**
	 * <p>Print content of the character array and (CR)NL due to operation system settings. Null prints as 'null'</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println(char[] data) throws PrintingException;
	
	/**
	 * <p>Print piece of the character array content. Null prints as 'null'</p>
	 * @param data data to print
	 * @param from piece beginning
	 * @param len piece length
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @ArrayIndexOutOfBoundsException if piece of data outside the array area
	 */
	T print(char[] data, int from, int len) throws PrintingException, ArrayIndexOutOfBoundsException;
	
	/**
	 * <p>Print piece of the character array content and (CR)NL due to operation system settings. Null prints as 'null'</p>
	 * @param data data to print
	 * @param from piece beginning
	 * @param len piece length
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @ArrayIndexOutOfBoundsException if piece of data outside the array area
	 */
	T println(char[] data, int from, int len) throws PrintingException, ArrayIndexOutOfBoundsException;
	
	/**
	 * <p>Print string representation of the object. Null prints as 'null'</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T print(Object data) throws PrintingException;
	
	/**
	 * <p>Print string representation of the object and (CR)NL due to operation system settings. Null prints as 'null'</p>
	 * @param data data to print
	 * @return self
	 * @throws PrintingException on any printing errors
	 */
	T println(Object data) throws PrintingException;
}
