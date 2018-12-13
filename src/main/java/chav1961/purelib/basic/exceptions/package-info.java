/**
 * <p>This package contains all exception definitions for the the library. Most of all exceptions are children of the 
 * {@link java.lang.Exception}. They are used in all the packages of the library.</p>
 * <p>Hierarchy of exceptions in the package is:</p>
 * <ul>
 * <li>{@linkplain java.lang.Exception}</li>
 * <ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.CalculationException} - fires on any calculation errors</li>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.ContentException} - fires on any content errors</li>
 * <ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.SyntaxException} - fires on any errors when parsing from string representation</li>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.PrintingException} - fires on any errors when building string representation</li>
 * </ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.EnvironmentException} - fires on any errors in the application environment</li>
 * <ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.CommandLineParametersException} - fires on any errors in commands typed</li>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.ConsoleCommandException} - fires on any errors in command lines</li>
 * </ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.FlowException} - fires on any errors on data flow processing</li>
 * <li>{@linkplain java.lang.RuntimeException}</li>
 * <ul>
 * <li>{@linkplain chav1961.purelib.basic.exceptions.PreparationException} - fires on any initialization errors in any classes</li>
 * </ul>
 * </ul>
 * </ul>
 * <p>It's strongly recommended to use all the exceptions in according to their's predefined roles were typed above</p>  
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1 last update 0.0.3
 */
package chav1961.purelib.basic.exceptions;