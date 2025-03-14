package chav1961.purelib.basic;

import java.net.URI;


import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.FSM.FSMCallback;
import chav1961.purelib.basic.StackedFSM.StackAction;
import chav1961.purelib.basic.StackedFSM.StackedFSMCallback;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.logs.AbstractLoggerFacade;

@Tag("OrdinalTestCategory")
public class FSMTest {
	private enum FSMTerminal {
		TermOn, TermOff
	}

	private enum FSMNonTerminal {
		State1, State2, State3, State4
	}

	private enum FSMExit {
		ExitOn, ExitOff, ExitNone
	}
	
	@Test
	public void basicFSMTest() throws FlowException {
		final int	counter[] = new int[] {0,0};
		final FSMCallback<FSMTerminal,FSMNonTerminal,FSMExit,String>	callback = new FSMCallback<FSMTerminal,FSMNonTerminal,FSMExit,String>() {
			@Override
			public void process(FSM<FSMTerminal, FSMNonTerminal, FSMExit, String> fsm, FSMTerminal terminal, FSMNonTerminal fromState, FSMNonTerminal toState, FSMExit[] action, String parameter) throws FlowException {
				Assert.assertEquals(parameter,"test string");
				counter[0]++;
				for (FSMExit item : action) {
					switch (item) {
						case ExitOn		:
							counter[1]++;
							break;
						case ExitOff	:
							counter[1]--;
							break;
						default :
					}
				}
			}
		};  
		final FSM<FSMTerminal,FSMNonTerminal,FSMExit,String>	fsm = new FSM<FSMTerminal,FSMNonTerminal,FSMExit,String>(callback,FSMNonTerminal.State1,
																	new FSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOn,FSMNonTerminal.State2,FSMExit.ExitOn),
																	new FSM.FSMLine<>(FSMNonTerminal.State4,FSMTerminal.TermOff,FSMNonTerminal.State1,FSMExit.ExitNone),
																	new FSM.FSMLine<>(FSMNonTerminal.State3,FSMTerminal.TermOn,FSMNonTerminal.State4,FSMExit.ExitOff),
																	new FSM.FSMLine<>(FSMNonTerminal.State2,FSMTerminal.TermOff,FSMNonTerminal.State3,FSMExit.ExitNone)
																);
		for (FSMTerminal item : new FSMTerminal[]{FSMTerminal.TermOn,FSMTerminal.TermOff,FSMTerminal.TermOn,FSMTerminal.TermOff}) {
			fsm.processTerminal(item,"test string");
		}
		Assert.assertEquals(counter[0],4);
		Assert.assertEquals(counter[1],0);

		try{fsm.processTerminal(null,"test string");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{new FSM<FSMTerminal,FSMNonTerminal,FSMExit,String>(null,FSMNonTerminal.State1,new FSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOn,FSMNonTerminal.State2,FSMExit.ExitOn)); 
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new FSM<FSMTerminal,FSMNonTerminal,FSMExit,String>(callback,null,new FSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOn,FSMNonTerminal.State2,FSMExit.ExitOn)); 
			Assert.fail("Mandatory exception was not detected (null 2-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new FSM<FSMTerminal,FSMNonTerminal,FSMExit,String>(callback,FSMNonTerminal.State1,(FSM.FSMLine<FSMTerminal,FSMNonTerminal,FSMExit>[])null); 
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new FSM<FSMTerminal,FSMNonTerminal,FSMExit,String>(callback,FSMNonTerminal.State1); 
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new FSM<FSMTerminal,FSMNonTerminal,FSMExit,String>(callback,FSMNonTerminal.State1,(FSM.FSMLine<FSMTerminal,FSMNonTerminal,FSMExit>)null); 
			Assert.fail("Mandatory exception was not detected (null inside items in the 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new FSM<FSMTerminal,FSMNonTerminal,FSMExit,String>(callback,FSMNonTerminal.State2,new FSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOn,FSMNonTerminal.State2,FSMExit.ExitOn)); 
			Assert.fail("Mandatory exception was not detected (non-existent initial state)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new FSM.FSMLine<FSMTerminal,FSMNonTerminal,FSMExit>(null,FSMTerminal.TermOn,FSMNonTerminal.State2,FSMExit.ExitOn); 
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new FSM.FSMLine<FSMTerminal,FSMNonTerminal,FSMExit>(FSMNonTerminal.State1,null,FSMNonTerminal.State2,FSMExit.ExitOn); 
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new FSM.FSMLine<FSMTerminal,FSMNonTerminal,FSMExit>(FSMNonTerminal.State1,FSMTerminal.TermOn,null,FSMExit.ExitOn); 
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new FSM.FSMLine<FSMTerminal,FSMNonTerminal,FSMExit>(FSMNonTerminal.State1,FSMTerminal.TermOn,FSMNonTerminal.State2,(FSMExit[])null); 
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void basicStackedFSMTest() throws FlowException {
		final int	counter[] = new int[] {0,0};
		final StackedFSMCallback<FSMTerminal,FSMNonTerminal,FSMExit,String,String>	callback = new StackedFSMCallback<FSMTerminal,FSMNonTerminal,FSMExit,String,String>() {
			@Override
			public String process(StackedFSM<FSMTerminal, FSMNonTerminal, String, FSMExit, String> fsm, FSMTerminal terminal, FSMNonTerminal fromState, String top, FSMNonTerminal toState, StackAction stack, FSMExit[] action, String parameter) throws FlowException {
				Assert.assertEquals(parameter,"test string");
				counter[0]++;
				for (FSMExit item : action) {
					switch (item) {
						case ExitOn		:
							counter[1]++;
							return "top";
						case ExitOff	:
							counter[1]--;
							return null;
						default :
					}
				}
				return null;
			}
		};  
		final StackedFSM<FSMTerminal,FSMNonTerminal,String,FSMExit,String>	fsm = new StackedFSM<FSMTerminal,FSMNonTerminal,String,FSMExit,String>(
																	callback,FSMNonTerminal.State1,
																	new StackedFSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOn,null,FSMNonTerminal.State2,StackedFSM.StackAction.PUSH,FSMExit.ExitOn),
																	new StackedFSM.FSMLine<>(FSMNonTerminal.State3,FSMTerminal.TermOn,"top",FSMNonTerminal.State4,StackedFSM.StackAction.POP,FSMExit.ExitOff),
																	new StackedFSM.FSMLine<>(FSMNonTerminal.State2,FSMTerminal.TermOff,"top",FSMNonTerminal.State3,StackedFSM.StackAction.NONE,FSMExit.ExitNone),
																	new StackedFSM.FSMLine<>(FSMNonTerminal.State4,FSMTerminal.TermOff,null,FSMNonTerminal.State1,StackedFSM.StackAction.NONE,FSMExit.ExitNone)
																);
		
		for (FSMTerminal item : new FSMTerminal[]{FSMTerminal.TermOn,FSMTerminal.TermOff,FSMTerminal.TermOn,FSMTerminal.TermOff}) {
			fsm.processTerminal(item,"test string");
		}
		Assert.assertEquals(counter[0],4);
		Assert.assertEquals(counter[1],0);
		
		try{fsm.processTerminal(null,"test string");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	
		try{new StackedFSM<FSMTerminal,FSMNonTerminal,String,FSMExit,String>(null,FSMNonTerminal.State1,new StackedFSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOn,null,FSMNonTerminal.State2,StackedFSM.StackAction.NONE,FSMExit.ExitOn)); 
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new StackedFSM<FSMTerminal,FSMNonTerminal,String,FSMExit,String>(callback,null,new StackedFSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOn,null,FSMNonTerminal.State2,StackedFSM.StackAction.NONE,FSMExit.ExitOn)); 
			Assert.fail("Mandatory exception was not detected (null 2-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new StackedFSM<FSMTerminal,FSMNonTerminal,String,FSMExit,String>(callback,FSMNonTerminal.State1,(StackedFSM.FSMLine<FSMTerminal,FSMNonTerminal,String,FSMExit>[])null); 
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new StackedFSM<FSMTerminal,FSMNonTerminal,String,FSMExit,String>(callback,FSMNonTerminal.State1); 
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new StackedFSM<FSMTerminal,FSMNonTerminal,String,FSMExit,String>(callback,FSMNonTerminal.State1,(StackedFSM.FSMLine<FSMTerminal,FSMNonTerminal,String,FSMExit>)null); 
			Assert.fail("Mandatory exception was not detected (null inside items in the 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new StackedFSM<FSMTerminal,FSMNonTerminal,String,FSMExit,String>(callback,FSMNonTerminal.State2,new StackedFSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOn,null,FSMNonTerminal.State2,StackedFSM.StackAction.NONE,FSMExit.ExitOn)); 
			Assert.fail("Mandatory exception was not detected (non-existent initial state)");
		} catch (IllegalArgumentException exc) {
		}

		try{new StackedFSM.FSMLine<FSMTerminal,FSMNonTerminal,String,FSMExit>(null,FSMTerminal.TermOn,null,FSMNonTerminal.State2,StackedFSM.StackAction.NONE,FSMExit.ExitOn); 
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new StackedFSM.FSMLine<FSMTerminal,FSMNonTerminal,String,FSMExit>(FSMNonTerminal.State2,null,null,FSMNonTerminal.State2,StackedFSM.StackAction.NONE,FSMExit.ExitOn); 
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new StackedFSM.FSMLine<FSMTerminal,FSMNonTerminal,String,FSMExit>(FSMNonTerminal.State2,FSMTerminal.TermOn,null,null,StackedFSM.StackAction.NONE,FSMExit.ExitOn); 
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new StackedFSM.FSMLine<FSMTerminal,FSMNonTerminal,String,FSMExit>(FSMNonTerminal.State2,FSMTerminal.TermOn,null,FSMNonTerminal.State2,null,FSMExit.ExitOn); 
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new StackedFSM.FSMLine<FSMTerminal,FSMNonTerminal,String,FSMExit>(FSMNonTerminal.State2,FSMTerminal.TermOn,null,FSMNonTerminal.State2,StackedFSM.StackAction.NONE,(FSMExit[])null); 
			Assert.fail("Mandatory exception was not detected (null 6-th argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void debuggingTest() throws FlowException {
		final StringBuilder		sb = new StringBuilder();
		final LoggerFacade		lf = new AbstractLoggerFacade(){
									@Override
									public boolean canServe(URI resource) throws NullPointerException {
										return false;
									}
						
									@Override
									public LoggerFacade newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
										return this;
									}
			
									@Override
									protected AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root) {
										return this;
									}
									@Override
									protected void toLogger(Severity level, String text, Throwable throwable) {
										sb.append(text).append('\n');
									}
								};
		final FSMCallback<FSMTerminal,FSMNonTerminal,FSMExit,String>	callback = new FSMCallback<FSMTerminal,FSMNonTerminal,FSMExit,String>() {
									@Override public void process(FSM<FSMTerminal, FSMNonTerminal, FSMExit, String> fsm, FSMTerminal terminal, FSMNonTerminal fromState, FSMNonTerminal toState, FSMExit[] action, String parameter) throws FlowException {}
								};  
		final FSM<FSMTerminal,FSMNonTerminal,FSMExit,String>	fsm = new FSM<FSMTerminal,FSMNonTerminal,FSMExit,String>(callback,FSMNonTerminal.State1,
										new FSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOff,FSMNonTerminal.State2,FSMExit.ExitNone),
										new FSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOn,FSMNonTerminal.State2,FSMExit.ExitNone),
										new FSM.FSMLine<>(FSMNonTerminal.State2,FSMTerminal.TermOff,FSMNonTerminal.State1,FSMExit.ExitNone),
										new FSM.FSMLine<>(FSMNonTerminal.State2,FSMTerminal.TermOn,FSMNonTerminal.State1,FSMExit.ExitNone)
									);
		sb.setLength(0);
		
		Assert.assertFalse(fsm.isDebugEnable());
		fsm.debugEnable(lf,false);
		Assert.assertTrue(fsm.isDebugEnable());
		for (FSMTerminal item : new FSMTerminal[]{FSMTerminal.TermOn,FSMTerminal.TermOff,FSMTerminal.TermOff,FSMTerminal.TermOn}) {
			fsm.processTerminal(item,"test string");
		}
		fsm.debugDisable();
		Assert.assertFalse(fsm.isDebugEnable());
		
		Assert.assertEquals(sb.toString(),"FSM current state = State1, terminal = TermOn --> newState = State2, actions = [ExitNone]\n"
										+"FSM current state = State2, terminal = TermOff --> newState = State1, actions = [ExitNone]\n"
										+"FSM current state = State1, terminal = TermOff --> newState = State2, actions = [ExitNone]\n"
										+"FSM current state = State2, terminal = TermOn --> newState = State1, actions = [ExitNone]\n");

		final StackedFSMCallback<FSMTerminal,FSMNonTerminal,FSMExit,String,String>	stackedCallback = new StackedFSMCallback<FSMTerminal,FSMNonTerminal,FSMExit,String,String>() {
			@Override public String process(StackedFSM<FSMTerminal, FSMNonTerminal, String, FSMExit, String> fsm, FSMTerminal terminal, FSMNonTerminal fromState, String top, FSMNonTerminal toState, StackAction stack, FSMExit[] action, String parameter) throws FlowException {return "top";}
		};  
		final StackedFSM<FSMTerminal,FSMNonTerminal,String,FSMExit,String>	stackedFsm = new StackedFSM<FSMTerminal,FSMNonTerminal,String,FSMExit,String>(
				stackedCallback,FSMNonTerminal.State1,
				new StackedFSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOn,null,FSMNonTerminal.State2,StackedFSM.StackAction.PUSH,FSMExit.ExitNone),
				new StackedFSM.FSMLine<>(FSMNonTerminal.State1,FSMTerminal.TermOff,null,FSMNonTerminal.State2,StackedFSM.StackAction.PUSH,FSMExit.ExitNone),
				new StackedFSM.FSMLine<>(FSMNonTerminal.State2,FSMTerminal.TermOn,"top",FSMNonTerminal.State1,StackedFSM.StackAction.POP,FSMExit.ExitNone),
				new StackedFSM.FSMLine<>(FSMNonTerminal.State2,FSMTerminal.TermOff,"top",FSMNonTerminal.State1,StackedFSM.StackAction.POP,FSMExit.ExitNone)
			);

		sb.setLength(0);
		
		Assert.assertFalse(stackedFsm.isDebugEnable());
		stackedFsm.debugEnable(lf,true);
		Assert.assertTrue(stackedFsm.isDebugEnable());
		for (FSMTerminal item : new FSMTerminal[]{FSMTerminal.TermOn,FSMTerminal.TermOff,FSMTerminal.TermOff,FSMTerminal.TermOn}) {
			stackedFsm.processTerminal(item,"test string");
		}
		stackedFsm.debugDisable();
		Assert.assertFalse(stackedFsm.isDebugEnable());
		
		Assert.assertEquals(sb.toString(),"FSM current state = State1, terminal = TermOn --> newState = State2, actions = [ExitNone]\n"
										+"FSM current state = State2, terminal = TermOff --> newState = State1, actions = [ExitNone]\n"
										+"FSM current state = State1, terminal = TermOff --> newState = State2, actions = [ExitNone]\n"
										+"FSM current state = State2, terminal = TermOn --> newState = State1, actions = [ExitNone]\n");
		
	}
}
