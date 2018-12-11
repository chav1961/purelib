package chav1961.purelib.basic.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import chav1961.purelib.basic.ConsoleCommandManager;

/**
 * <p>This annotation is used to mark a method to call from the Console manager. It describes command line template and help string for the given command processor</p>
 * <p>Annotation contains two parameters:</p>
 * <ul>
 * <li>template - console command template to associate with the given method</li>
 * <li>help - help string for the given console command</li> 
 * </ul>
 * <p>template format can contains:</p>
 * <ul>
 * <li>{@code ${<name>}} - variable part of the command. Command string content matches with the given name will then fill the appropriative argument when calling the command processing method (see )</li>  
 * <li>{@code [<something>]} - optional parameter. Data inside the parameter can't be started with ${...}</li>
 * <li>{@code {<option1>|<option2>|...|<default>}} - case. Data inside cases (except the default case) can't be started with ${...}. The default case can be empty, so this construction will be optional.</li>   
 * <li>{@code <something><seq>...} - repeated parameters. Symbol {@code <seq>} (for example (,) ) is used as a separator and need be exactly one non-blank char. Repeat continuation starts if the symbol {@code <seq>} is detected in the source string</li> 
 * <li>{@code <something>...} - variant of repeated parameters. Differ to previous, repeat continuation starts if template <i>after</i> this construction doesn't match to the source string</li> 
 * <li>{@code \<char>} - escaping. All chars are used in all the previous constructions (include backslash) need be escaped to represents self as-is</li>  
 * <li>any char sequence - represents self as-is. Any sequence of blank symbols in the command line string are reduced to exactly one blank char</li>
 * </ul>
 * 
 * <p>To get examples of using this annotation, see {@link ConsoleCommandManager} class Java code
 * 
 * @see ConsoleCommandManager
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ConsoleCommand {
	String template();
	String help();
}

