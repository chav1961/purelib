package chav1961.purelib.basic.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation is used to mark a class as console command processor for the Console manager. It associates the command processor class with the command prefix</p>
 * <p>Annotation contains the only parameter - command name to execute. All commands for the given console command processor need be started with this command prefix. 
 * There can be more than one class with the same command prefix in the system, but there can't be more than one method with the same command template (see ) in the
 * same console command manager</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ConsoleCommandPrefix {
	String value();
}