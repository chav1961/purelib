package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.interfaces.FSMProcessor;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

/**
 * 
 * <p>This class is an implementation of the Stacked Finite State Machine (FSM). It describes the Mile FSM. Every FSM has a FSM jump table, current state and callback associated. The main 
 * method to use for FSM is {@linkplain #processTerminal(Enum, Object)} method. It scans FSM jump table, finds the current state/terminal/stack combination, and changed current FSM state on success.
 * It also calls the callback {@linkplain StackedFSMCallback#process(StackedFSM, Enum, Enum, Object, Enum, StackAction, Enum[], Object)} method to process associated actions for the given FSM state
 * change.</p>
 *  
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 *
 * @param <Terminal> terminal (in the terms of FSM)
 * @param <NonTerminal> non-terminal (in the terms of FSM)
 * @param <Stack> stack top content (in the terms of FSM)
 * @param <Exit> exit symbol (in the terms of FSM)
 * @param <Parameter> any cargo to pass thru the FSM to FSM callback
 */
public class StackedFSM<Terminal extends Enum<?>,NonTerminal extends Enum<?>,Stack,Exit extends Enum<?>,Parameter> implements FSMProcessor<Terminal,Parameter>{
	/**
	 * <p>Stack action for the FSM jumps</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 *
	 */
	public enum StackAction {
		NONE, PUSH, POP
	}

	/**
	 * <p>This interface describes a callback to process successful stacked FSM state changes.</p>   
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 * @param <Terminal> terminal (in the terms of FSM)
	 * @param <NonTerminal> non-terminal (in the terms of FSM)
	 * @param <Exit> exit symbol (in the terms of FSM)
	 * @param <Stack> stack content type (in the terms of FSM)
	 * @param <Parameter> any cargo to pass thru the FSM to FSM callback
	 */
	@FunctionalInterface
	public interface StackedFSMCallback<Terminal extends Enum<?>,NonTerminal extends Enum<?>,Exit extends Enum<?>,Stack,Parameter> {
		/**
		 * <p>Process callback of the FSM</p>
		 * @param fsm fsm instance that calls this method
		 * @param terminal terminal from the {@linkplain FSM#processTerminal(Enum, Object)} method
		 * @param fromState current state of the FSM
		 * @param top top of the stack
		 * @param toState new state of the FSM
		 * @param stack action to make with the stack
		 * @param action action list for the given state jump
		 * @param parameter parameter from the {@linkplain FSM#processTerminal(Enum, Object)} method
		 * @return stack value to push into stack if the stack action is {@linkplain StackedFSM.StackAction#PUSH} 
		 * @throws FlowException indicate any processing errors
		 */
		Stack process(StackedFSM<Terminal,NonTerminal,Stack,Exit,Parameter> fsm,Terminal terminal,NonTerminal fromState, Stack top, NonTerminal toState, StackAction stack, Exit[] action, Parameter parameter) throws FlowException;
	}
	
	private final StackedFSMCallback<Terminal,NonTerminal,Exit,Stack,Parameter>	callback;
	private final FSMLine<Terminal,NonTerminal,Stack,Exit>[]				table;
	private final List<Stack>												stack = new ArrayList<>();
	private NonTerminal														currentState;
	private LoggerFacade													logger;
	private boolean															debugEnable = false, logFailures = false;
	
	@SafeVarargs
	public StackedFSM(final StackedFSMCallback<Terminal,NonTerminal,Exit,Stack,Parameter> callback, final NonTerminal initialState, final FSMLine<Terminal,NonTerminal,Stack,Exit>... table) throws NullPointerException, IllegalArgumentException {
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
				
				Arrays.sort(this.table,	new Comparator<FSMLine<Terminal,NonTerminal,Stack,Exit>>(){
					@Override
					public int compare(FSMLine<Terminal, NonTerminal, Stack, Exit> o1, FSMLine<Terminal, NonTerminal, Stack, Exit> o2) {
						if (o1.state == o2.state) {
							if (o1.terminal == o2.terminal) {
								return o1.stack.hashCode() - o2.stack.hashCode();
							}
							else {
								return o1.terminal.ordinal() - o2.terminal.ordinal();
							}
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
			FSMLine<Terminal,NonTerminal,Stack,Exit>	item;
			int 		low = 0, high = table.length - 1, mid;
	
	        while (low <= high) {
	            mid = (low + high) >>> 1;
				item = table[mid];
	
				if (item.state == current) {
					if (item.terminal == terminal) {
						if (compareStack(item.stack,(stack.size() == 0 ? null : stack.get(0))) == 0) {
					        if (debugEnable && logger.isLoggedNow(Severity.trace)) {
					        	logger.message(Severity.trace,"FSM current state = %1$s, terminal = %2$s --> newState = %3$s, actions = %4$s",current,terminal,item.newState,Arrays.toString(item.actions));
					        }
					        currentState = item.newState;
					        switch (item.stackAction) {
					        	case NONE	:
					        		callback.process(this,terminal,current,null,currentState,StackAction.NONE,item.actions,parameter);
					        		break;
					        	case PUSH	:
							        stack.add(0,callback.process(this,terminal,current,null,currentState,StackAction.PUSH,item.actions,parameter));
							        break;
					        	case POP	:
					        		if (stack.size() == 0) {
					        			throw new FlowException("Stack excausted: terminal = "+terminal+", current state = "+currentState);
					        		}
					        		else {
					        			stack.remove(0);
						        		callback.process(this,terminal,current,null,currentState,StackAction.POP,item.actions,parameter);
					        		}
					        }
					        return;
						}
						else if (compareStack(item.stack,(stack.size() == 0 ? null : stack.get(0))) == 0) {
			                low = mid + 1;
						}
						else {
			                high = mid - 1;
						}
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

	private int compareStack(final Stack left, final Stack right) {
		if (left == null && right == null) {
			return 0;
		}
		else if (left == null && right != null) {
			return -1;
		}
		else if (left != null && right == null) {
			return 1;
		}
		else {
			return left.hashCode() - right.hashCode();
		}
	}
	
	/**
	 * <p>This class describes one line of the stacked FSM jump table.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 * @param <Terminal> terminal (in the terms of FSM)
	 * @param <NonTerminal> non-terminal (in the terms of FSM)
	 * @param <Stack> top stack symbol (in the terms of FSM)
	 * @param <Exit> exit symbol (in the terms of FSM)
	 */
	public static class FSMLine<Terminal,NonTerminal,Stack,Exit> {
		public final NonTerminal	state; 
		public final Terminal		terminal; 
		public final Stack			stack;
		public final NonTerminal	newState;
		public final StackAction	stackAction;
		public final Exit[]			actions;
		
		/**
		 * <p>Create one line of the FSM jump table</p>
		 * @param state current FSM state
		 * @param terminal terminal
		 * @param stack stack content (can be null)
		 * @param newState new FSM state
		 * @param stackAction stack action
		 * @param actions actions list for the given jump
		 * @throws NullPointerException when any parameters except 'stack' are null
		 */
		@SafeVarargs
		public FSMLine(final NonTerminal state, final Terminal terminal, final Stack stack, final NonTerminal newState, final StackAction stackAction, final Exit... actions) throws NullPointerException {
			if (state == null) {
				throw new NullPointerException("FSM state can't be null");
			}
			else if (terminal == null) {
				throw new NullPointerException("FSM terminal can't be null");
			}
			else if (newState == null) {
				throw new NullPointerException("FSM new state can't be null");
			}
			else if (stackAction == null) {
				throw new NullPointerException("FSM stack action can't be null");
			}
			else if (actions == null) {
				throw new NullPointerException("FSM actions can't be null");
			}
			else {
				this.state = state;
				this.terminal = terminal;
				this.stack = stack;
				this.newState = newState;
				this.stackAction = stackAction;
				this.actions = actions;
			}
		}

		@Override
		public String toString() {
			return "FSMLine [state=" + state + ", terminal=" + terminal + ", stack=" + stack + ", newState=" + newState + ", stackAction=" + stackAction + ", actions=" + Arrays.toString(actions) + "]";
		}
	}
}
