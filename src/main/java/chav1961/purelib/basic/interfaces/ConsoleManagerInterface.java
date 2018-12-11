package chav1961.purelib.basic.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

import chav1961.purelib.basic.exceptions.ConsoleCommandException;

/**
 * <p>This interface describes a console command manager. It parses command line, search appropriative command line processor and invokes appropriative method in it</p>
 * <p>Every console command manager can get a command line from anywhere, parse it and call <i>command line processors</i> to process parsed command.</p>
 * <p>Any class, marked with the {@linkplain chav1961.purelib.basic.annotations.ConsoleCommandPrefix @ConsoleCommandPrefix} annotation, can be used as the command line processor. Some or every <i>public</i>
 * methods of this class can be <i>marked</i> with the {@linkplain chav1961.purelib.basic.annotations.ConsoleCommand @ConsoleCommand} annotation to associate the method with 
 * the given <i>console command template</i> (description of the templates available see {@linkplain chav1961.purelib.basic.annotations.ConsoleCommand @ConsoleCommand}). To use class for command processing
 * you need create the annotated class instance first, and then <i>deploy</i> it to the appropriative console command manager. An example of using annotations for the command line processor see 
 * {@linkplain chav1961.purelib.basic.ConsoleCommandManager console command manager} implementation</p>
 * 
 * <p>Any console command manager needs check matching the actual command line with the every known command templates of every command line processors had been deployed. If the command line matches some template,
 * console command manager extracts variable parameters from the command line, converts them to the appropriative method parameters (according to 
 * {@linkplain chav1961.purelib.basic.annotations.ConsoleCommandParameter @ConsoleCommandParameter} annotation) and invoke the marked method with parameters extracted. Any marked method can return any data type,
 * but two of them have a special processing in the console command manager:</p>
 * 
 * <ul>
 * <li>String - returned string is printed to the console output as-is</li>
 * <li>boolean or Boolean - produces message "execution successful" or "execution failed" to the console output</li>
 * </ul>
 * 
 * <p>Any other returned types will be ignored.</p>
 * 
 * <p>Any marked method can throws any exceptions when processing console command. All those exceptions will be incapsulated to the {@link chav1961.purelib.basic.exceptions.ConsoleCommandException ConsoleCommandException} exception. You can use
 * {@link chav1961.purelib.basic.exceptions.ConsoleCommandException#getCause() getCause()} method to extract source exception for the longer processing</p>
 * 
 * <p>Any console command manager need be <i>closed</i> after the end of the console command line processing. After closing, no any actions can be made with the console command manager.</p>
 * 
 * @see chav1961.purelib.basic.annotations.ConsoleCommand @ConsoleCommand
 * @see chav1961.purelib.basic.annotations.ConsoleCommandPrefix @ConsoleCommandPrefix
 * @see chav1961.purelib.basic.annotations.ConsoleCommandParameter @ConsoleCommandParameter
 * @see chav1961.purelib.basic.ConsoleCommandManager ConsoleCommandManager
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public interface ConsoleManagerInterface extends Closeable {
	/**
	 * <p>Deploy command line processors to the console command manager</p>
	 * @param ccp console command processors to deploy
	 * @throws IllegalArgumentException any problems with the parameters 
	 * @throws IllegalStateException attempt to manipulate console command manager after close() 
	 */
	void deploy(final Object... ccp);
	
	/**
	 * <p>Undeploy command line processors from the console command manager</p>
	 * @param ccp console command processors to undeploy
	 * @throws IllegalArgumentException any problems with the parameters 
	 * @throws IllegalStateException attempt to manipulate console command manager after close() 
	 */
	void undeploy(final Object... ccp);
	
	/**
	 * <p>Undeploy all command line processors from the console command manager. This method need be mandatory called from the close() method implementation</p>
	 * @throws IllegalStateException attempt to manipulate console command manager after close() 
	 */
	void undeployAll();

	/**
	 * <p>Process command line</p>
	 * @param cmd any command line to process
	 * @return string after command processing 
	 * @throws IllegalArgumentException any problems with the parameters 
	 * @throws IllegalStateException attempt to manipulate console command manager after close() 
	 * @throws ConsoleCommandException any exceptions incapsulated from the invoked method of the command line processor. Use {@link java.lang.Throwable#getCause() getCause()} to get it.
	 */
	String processCmd(final String cmd) throws ConsoleCommandException; 

	/**
	 * <p>Make an infinite loop to the EOF of the input to process console commands</p>
	 * @param in reader to red command lines
	 * @param out console output to print processing results
	 * @throws IllegalArgumentException any problems with the parameters 
	 * @throws IllegalStateException attempt to manipulate console command manager after close() 
	 * @throws IOException any exceptions on the reader or writer.
	 */
	void processCmd(final Reader in, final PrintStream out) throws IOException; 
}
