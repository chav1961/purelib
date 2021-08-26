package chav1961.purelib.basic;

import java.util.Arrays;
import java.util.Comparator;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.interfaces.FSMProcessor;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

/**
 * <p>This class is an implementation of the Finite State Machine (FSM). It describes the Mile FSM. Every FSM has a FSM jump table, current state and callback associated. The main 
 * method to use for FSM is {@linkplain #processTerminal(Enum, Object)} method. It scans FSM jump table, finds the current state and terminal combination, and changed current FSM state on success.
 * It also calls the callback {@linkplain FSMCallback#process(FSM, Enum, Enum, Enum, Enum[], Object)} method to process associated actions for the given FSM state change.</p>
 *  
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.5
 *
 * @param <Terminal> terminal (in the terms of FSM)
 * @param <NonTerminal> non-terminal (in the terms of FSM)
 * @param <Exit> exit symbol (in the terms of FSM)
 * @param <Parameter> any cargo to pass thru the FSM to FSM callback
 */

public class FSM<Terminal extends Enum<?>,NonTerminal extends Enum<?>,Exit extends Enum<?>,Parameter> implements FSMProcessor<Terminal,Parameter>{
	
	/**
	 * <p>This interface describes a callback to process successful FSM state changes.</p>   
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 * @param <Terminal> terminal (in the terms of FSM)
	 * @param <NonTerminal> non-terminal (in the terms of FSM)
	 * @param <Exit> exit symbol (in the terms of FSM)
	 * @param <Parameter> any cargo to pass thru the FSM to FSM callback
	 */
	@FunctionalInterface
	public interface FSMCallback<Terminal extends Enum<?>,NonTerminal extends Enum<?>,Exit extends Enum<?>,Parameter> {
		/**
		 * <p>Process callback of the FSM</p>
		 * @param fsm fsm instance that calls this method
		 * @param terminal terminal from the {@linkplain FSM#processTerminal(Enum, Object)} method
		 * @param fromState current state of the FSM
		 * @param toState new state of the FSM
		 * @param action action list for the given state jump
		 * @param parameter parameter from the {@linkplain FSM#processTerminal(Enum, Object)} method
		 * @throws FlowException indicate any processing errors
		 */
		void process(FSM<Terminal,NonTerminal,Exit,Parameter> fsm,Terminal terminal,NonTerminal fromState, NonTerminal toState, Exit[] action, Parameter parameter) throws FlowException;
	}

	private final FSMCallback<Terminal,NonTerminal,Exit,Parameter>	callback;
	private final FSMLine<Terminal,NonTerminal,Exit>[]				table;
	private NonTerminal												currentState;
	private LoggerFacade											logger;
	private boolean													debugEnable = false, logFailures = false;
	
	/**
	 * <p>Create FSM by it' description</p>
	 * @param callback FSM callback to use 
	 * @param initialState initial state of the FSM
	 * @param table FSM jump table
	 * @throws NullPointerException if any parameters are null
	 * @throws IllegalArgumentException if any parameters are invalid
	 */
	@SafeVarargs
	public FSM(final FSMCallback<Terminal,NonTerminal,Exit,Parameter> callback, final NonTerminal initialState, final FSMLine<Terminal,NonTerminal,Exit>... table) throws NullPointerException, IllegalArgumentException {
		if (callback == null) {
			throw new NullPointerException("FSM callback can't be null");
		}
		else if (initialState == null) {
			throw new NullPointerException("FSM initial state can't be null");
		}
		else if (table == null || table.length == 0) {
			throw new IllegalArgumentException("FSM jump table can't be null or zero-length array");
		}
		else {
			boolean		wasInitialState = false;
			
			for (int index = 0; index < table.length; index++) {
				if (table[index] == null) {
					throw new NullPointerException("Null element at index ["+index+"] in the FSM table");
				}
				else if (table[index].state == initialState) {
					wasInitialState = true;
				}
			}
			if (!wasInitialState) {
				throw new IllegalArgumentException("No any lines found in the FSM table with the current state = ["+initialState+"]! ");
			}
			else {
				this.callback = callback;
				this.table = table.clone();
				this.currentState = initialState;
				
				Arrays.sort(this.table,	new Comparator<FSMLine<Terminal,NonTerminal,Exit>>(){
					@Override
					public int compare(FSMLine<Terminal, NonTerminal, Exit> o1, FSMLine<Terminal, NonTerminal, Exit> o2) {
						if (o1.state == o2.state) {
							return o1.terminal.ordinal() - o2.terminal.ordinal();
						}
						else {
							return o1.state.ordinal() - o2.state.ordinal();
						}
					}
				});
			}
		}
	}

	@Override
	public final void processTerminal(final Terminal terminal, final Parameter parameter) throws NullPointerException, FlowException {
		if (terminal == null) {
			throw new NullPointerException("Terminal can't be null!"); 
		}
		else {
			final NonTerminal	current = currentState;
			FSMLine<Terminal,NonTerminal,Exit>	item;
			int 		low = 0, high = table.length - 1, mid;
	
	        while (low <= high) {
	            mid = (low + high) >>> 1;
				item = table[mid];
	
				if (item.state == current) {
					if (item.terminal == terminal) {
				        if (debugEnable && logger.isLoggedNow(Severity.trace)) {
				        	logger.message(Severity.trace,"FSM current state = %1$s, terminal = %2$s --> newState = %3$s, actions = %4$s",current,terminal,item.newState,Arrays.toString(item.actions));
				        }
				        currentState = item.newState;
				        if (item.actions != null) {  
					        callback.process(this,terminal,current,currentState,item.actions,parameter);
				        }
				        return;
					}
					else if (item.terminal.ordinal() < terminal.ordinal()) {
		                low = mid + 1;
					}
					else {
		                high = mid - 1;
					}
				}
				else if (item.state.ordinal() < current.ordinal()) {
	                low = mid + 1;
				}
				else {
	                high = mid - 1;
				}
	        }
	        if (debugEnable && logFailures && logger.isLoggedNow(Severity.trace)) {
	        	logger.message(Severity.trace,"FSM failure: current state = %1$s, terminal = %2$s",current,terminal);
	        }
		}
	}
	
	@Override
	public void debugEnable(final LoggerFacade logger, final boolean logFailures) throws NullPointerException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else {
			this.logger = logger;
			this.logFailures = logFailures;
			this.debugEnable = true;
		}
	}

	@Override
	public void debugDisable() {
		debugEnable = false;
	}
	
	@Override
	public boolean isDebugEnable() {
		return debugEnable;
	}
	
	/**
	 * <p>Get current state of the FSM.</p>
	 * @return current state of the FSM. Can't be null
	 * @since 0.0.5
	 */
	public NonTerminal getCurrentState() {
		return currentState;
	}
	
	/**
	 * <p>This class describes one line of the FSM jump table.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 * @param <Terminal> terminal (in the terms of FSM)
	 * @param <NonTerminal> non-terminal (in the terms of FSM)
	 * @param <Exit> exit symbol (in the terms of FSM)
	 */
	public static class FSMLine<Terminal,NonTerminal,Exit> {
		public final NonTerminal	state; 
		public final Terminal		terminal; 
		public final NonTerminal	newState; 
		public final Exit[]			actions;

		/**
		 * <p>Create one line of the FSM jump table</p>
		 * @param state current FSM state
		 * @param terminal terminal
		 * @param newState new FSM state
		 * @param actions actions list for the given jump
		 */
		@SafeVarargs
		public FSMLine(final NonTerminal state, final Terminal terminal, final NonTerminal newState, final Exit... actions) {
			if (state == null) {
				throw new NullPointerException("State can't ne null");
			}
			else if (terminal == null) {
				throw new NullPointerException("Terminal can't ne null");
			}
			else if (newState == null) {
				throw new NullPointerException("New state can't ne null");
			}
			else if (actions == null) {
				throw new NullPointerException("Actions can't be null");
			}
			else {
				this.state = state;
				this.terminal = terminal;
				this.newState = newState;
				this.actions = actions.length > 0 ? actions.clone() : null; 	// Protection of the problem - zero-length parameter in the FSMLine constructor produces Object[0], not Exit[0] array!
			}
		}

		@Override
		public String toString() {
			return "FSMLine [state=" + state + ", terminal=" + terminal + ", newState=" + newState + ", actions=" + Arrays.toString(actions) + "]";
		}
	}
}
