/**
 * <p>This package contains all exception definitions for the the library. Most of all exceptions are children of the 
 * {@link java.lang.Exception}. They are used in all the packages of the library.</p>
 * <p>Hierarchy of exceptions in the package is:</p>
 * <ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.PureLibException} - root of all checked exceptions in the Pure Library
 * <ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.CalculationException} - fires on any calculation errors
 * <li>{@linkplain chav1961.purelib.basic.exceptions.ContentException} - fires on any content errors
 * <ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.CommandLineParametersException} - fires on any errors in commands typed
 * <li>{@linkplain chav1961.purelib.basic.exceptions.ConsoleCommandException} - fires on any errors in command lines
 * <li>{@linkplain chav1961.purelib.basic.exceptions.SyntaxException} - fires on any errors when parsing from string representation
 * <ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.MimeParseException} - special type of {@linkplain chav1961.purelib.basic.exceptions.SyntaxException} to parse MIM strings
 * </ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.PrintingException} - fires on any errors when building string representation
 * <li>{@linkplain chav1961.purelib.basic.exceptions.TestException} - fires on any errors on testing
 * </ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.EnvironmentException} - fires on any errors in the application environment
 * <li>{@linkplain chav1961.purelib.basic.exceptions.FlowException} - fires on any errors on data flow processing
 * <li>{@linkplain chav1961.purelib.basic.exceptions.DebuggingException} - fires on any errors on content debugging
 * </ul>
 * <li>{@linkplain java.lang.RuntimeException} - root of all runtime exceptions in the Pure Library
 * <ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.PreparationException} - fires on any initialization errors in any classes (especially in static initializers)
 * </ul>
 * </ul>
 * <p>It's strongly recommended to use all the exceptions in according to their's predefined roles were typed earlier</p>  
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.4
 */
package chav1961.purelib.basic.exceptions;