package chav1961.purelib.basic.interfaces;

import java.io.Reader;

import chav1961.purelib.basic.CharUtils.CharSubstitutionSource;
import chav1961.purelib.basic.CharUtils.SubstitutionSource;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.PrintingException;

/**
 * <p>This interface used especially to print code content to output stream.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @see CharStreamPrinter
 * @since 0.0.7
 * @param <T> - returned type to use for chained operations 
 */
public interface CodeCharStreamPrinter<T> extends CharStreamPrinter<T> {
	/**
	 * <p>Print formatted string</p>
	 * @param format format string. Can't be null or empty
	 * @param parameters format parameters
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @throws IllegalArgumentException format string is null or empty
	 */
	T print(String format, Object... parameters) throws PrintingException, IllegalArgumentException;
	
	/**
	 * <p>Print formatted string and nl</p>
	 * @param format format string. Can't be null or empty
	 * @param parameters format parameters
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @throws IllegalArgumentException format string is null or empty
	 */
	T println(String format, Object... parameters) throws PrintingException, IllegalArgumentException;

	/**
	 * <p>Begin to add '\t' char to the beginning of all lines. Can be nested</p> 
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @see #leave()
	 */
	T enter() throws PrintingException;
	
	/**
	 * <p>Stop to add '\t' char to the beginning of all lines. Can be nested</p>
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @throws IllegalStateException leave() exhausted previous {@linkplain #enter()} tabs
	 * @see #enter()
	 */
	T leave() throws PrintingException, IllegalStateException;
	
	/**
	 * <p>Print reader content to output</p>
	 * @param rdr reader to print it's content. Can't be null
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @throws NullPointerException reader is null
	 */
	T print(Reader rdr) throws PrintingException, NullPointerException;
	
	/**
	 * <p>Print reader content to output and print nl</p>
	 * @param rdr reader to print it's content. Can't be null
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @throws NullPointerException reader is null
	 */
	T println(Reader rdr) throws PrintingException, NullPointerException;
	
	/**
	 * <p>Print reader content to output with substitutions. Substitutes all ${varName} markers with it's values</p>
	 * @param rdr reader to print it's content. Can't be null
	 * @param src substitution source for print. Can't be null
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @throws NullPointerException any argument is null
	 * @see SubstitutableProperties
	 */
	T print(Reader rdr, SubstitutionSource src) throws PrintingException, NullPointerException;
	
	/**
	 * <p>Print reader content to output with substitutions and print nl. Substitutes all ${varName} markers with it's values</p>
	 * @param rdr reader to print it's content. Can't be null
	 * @param src substitution source for print. Can't be null
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @throws NullPointerException any argument is null
	 * @see SubstitutableProperties
	 */
	T println(Reader rdr, SubstitutionSource src) throws PrintingException, NullPointerException;
	
	/**
	 * <p>Print reader content to output with substitutions. Substitutes all ${varName} markers with it's values</p>
	 * @param rdr reader to print it's content. Can't be null
	 * @param src substitution source for print. Can't be null
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @throws NullPointerException any argument is null
	 * @see SubstitutableProperties
	 */
	T print(Reader rdr, CharSubstitutionSource src) throws PrintingException, NullPointerException;

	/**
	 * <p>Print reader content to output with substitutions and print nl. Substitutes all ${varName} markers with it's values</p>
	 * @param rdr reader to print it's content. Can't be null
	 * @param src substitution source for print. Can't be null
	 * @return self
	 * @throws PrintingException on any printing errors
	 * @throws NullPointerException any argument is null
	 * @see SubstitutableProperties
	 */
	T println(Reader rdr, CharSubstitutionSource src) throws PrintingException, NullPointerException;
}
