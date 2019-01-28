package chav1961.purelib.basic.interfaces;

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

public interface CharStreamPrinter<T> {
	T println() throws PrintingException;
	T print(char data) throws PrintingException;
	T println(char data) throws PrintingException;
	T print(byte data) throws PrintingException;
	T println(byte data) throws PrintingException;
	T print(short data) throws PrintingException;
	T println(short data) throws PrintingException;
	T print(int data) throws PrintingException;
	T println(int data) throws PrintingException;
	T print(long data) throws PrintingException;
	T println(long data) throws PrintingException;
	T print(float data) throws PrintingException;
	T println(float data) throws PrintingException;
	T print(double data) throws PrintingException;
	T println(double data) throws PrintingException;
	T print(boolean data) throws PrintingException;
	T println(boolean data) throws PrintingException;
	T print(String data) throws PrintingException;
	T println(String data) throws PrintingException;
	T print(String data, int from, int len) throws PrintingException;
	T println(String data, int from, int len) throws PrintingException;
	T print(char[] data) throws PrintingException;
	T println(char[] data) throws PrintingException;
	T print(char[] data, int from, int len) throws PrintingException;
	T println(char[] data, int from, int len) throws PrintingException;
	T print(Object data) throws PrintingException;
	T println(Object data) throws PrintingException;
}
