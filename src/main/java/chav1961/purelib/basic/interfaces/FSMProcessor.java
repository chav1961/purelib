package chav1961.purelib.basic.interfaces;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.FSM.FSMCallback;
import chav1961.purelib.basic.exceptions.FlowException;

/**
 * <p>This interface describes any Finite State Machine (FSM) processor.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 *
 * @param <Terminal> terminal (in the terms of FSM)
 * @param <Parameter> any cargo to pass thru the FSM to FSM callback
 */
public interface FSMProcessor<Terminal extends Enum<?>,Parameter> {
	/**
	 * <p>Process terminal with the FSM
	 * @param terminal terminal to process
	 * @param parameter parameter to pass to {@linkplain FSMCallback#process(FSM, Enum, Enum, Enum, Enum[], Object)} method. Can be null
	 * @throws NullPointerException if terminal is null
	 * @throws FlowException on any processing exceptions
	 */
	void processTerminal(final Terminal terminal, final Parameter parameter) throws NullPointerException, FlowException;
	
	/**
	 * <p>Turn on debug mode for the given FSM</p>
	 * @param logger logger to trace FSM to
	 * @param logFailures log failed combinations of the terminal and the current FSM state
	 * @throws NullPointerException if logger facade is null
	 */
	void debugEnable(final LoggerFacade logger, final boolean logFailures) throws NullPointerException;
	
	/**
	 * <p>Turn off debug mode of the current FSM</p>
	 */
	void debugDisable();
	
	/**
	 * <p>Is debug mode on for the given FSM</p>
	 * @return true if debug mode is on
	 */
	boolean isDebugEnable();
}
