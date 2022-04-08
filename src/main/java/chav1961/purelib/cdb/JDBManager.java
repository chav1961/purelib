package chav1961.purelib.cdb;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.EventRequest;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.cdb.AbstractDebugWatcher.ContentChangeType;
import chav1961.purelib.cdb.AbstractDebugWatcher.StateChangeType;
import chav1961.purelib.cdb.interfaces.DebugWatcher;
import chav1961.purelib.cdb.interfaces.InnerWatchersKeeper;

public class JDBManager extends AbstractDebugWatcher implements AutoCloseable {
	private static final String		SOCKET_CONNECTOR = "com.sun.tools.jdi.SocketAttachingConnector"; 
	private static final String		SHARED_MEMORY_CONNECTOR = "com.sun.tools.jdi.SharedMemoryAttachingConnector"; 
	private static final String		PROCESS_CONNECTOR = "com.sun.tools.jdi.ProcessAttachingConnector"; 

	private final LoggerFacade		logger;
	private final VirtualMachine	vm;
	private final WatchersRepository<VMWatcher>			vmRepo = new WatchersRepository<>(this, VMWatcher.class);
	private final WatchersRepository<ExceptionWatcher>	exceptionRepo = new WatchersRepository<>(this, ExceptionWatcher.class);
	private final WatchersRepository<ClassWatcher>		classRepo = new WatchersRepository<>(this, ClassWatcher.class);
	private final Thread			t = new Thread(()->processEvents());
	private final ContentChangeListener	ccl = this::contentChanged;
	private final StateChangeListener	scl = this::stateChanged;
	
	private static enum VMEvents {
		ACCESS_WATCHPOINT_EVENT(com.sun.jdi.event.AccessWatchpointEvent.class),
		BREAKPOINT_EVENT(com.sun.jdi.event.BreakpointEvent.class),
		CLASS_PREPARE_EVENT(com.sun.jdi.event.ClassPrepareEvent.class),
		CLASS_UNLOAD_EVENT(com.sun.jdi.event.ClassUnloadEvent.class),
		EXCEPTION_EVENT(com.sun.jdi.event.ExceptionEvent.class),
		METHOD_ENTRY_EVENT(com.sun.jdi.event.MethodEntryEvent.class),
		METHOD_EXIT_EVENT(com.sun.jdi.event.MethodExitEvent.class),
		MODIFICATION_WATCHPOINT_EVENT(com.sun.jdi.event.ModificationWatchpointEvent.class),
		MONITOR_CONTENDED_ENTERED_EVENT(com.sun.jdi.event.MonitorContendedEnteredEvent.class),
		MONITOR_CONTENDED_ENTER_EVENT(com.sun.jdi.event.MonitorContendedEnterEvent.class),
		MONITOR_WAITED_EVENT(com.sun.jdi.event.MonitorWaitedEvent.class),
		MONITOR_WAIT_EVENT(com.sun.jdi.event.MonitorWaitEvent.class),
		STEP_EVENT(com.sun.jdi.event.StepEvent.class),
		THREAD_DEATH_EVENT(com.sun.jdi.event.ThreadDeathEvent.class),
		THREAD_START_EVENT(com.sun.jdi.event.ThreadStartEvent.class),
		VM_DEATH_EVENT(com.sun.jdi.event.VMDeathEvent.class),
		VM_DISCONNECT_EVENT(com.sun.jdi.event.VMDisconnectEvent.class),
		VM_START_EVENT(com.sun.jdi.event.VMStartEvent.class),
		WATCHPOINT_EVENT(com.sun.jdi.event.WatchpointEvent.class);
		
		private final Class<? extends Event>	cl;
		
		private VMEvents(final Class<? extends Event> cl) {
			this.cl = cl;
		}
		
		public Class<? extends Event> getEventClass() {
			return cl;
		}
		
		public static <T extends Event> VMEvents valueOf(final T value) {
			if (value == null) {
				throw new NullPointerException("Value can't be null"); 
			}
			else {
				Class<?>	cl = null;
				VMEvents found = null;

				for (Class<?> item : value.getClass().getInterfaces()) {
					if (Event.class.isAssignableFrom(item)) {
						cl = item;
					}
				}
				
				for (VMEvents event : values()) {
					if (event.getEventClass().isAssignableFrom(cl)) {
						if (found == null || found.getEventClass().isAssignableFrom(event.getEventClass())) {
							found = event;
						}
					}
				}
				if (found != null) {
					return found;
				}
				else {
					throw new IllegalArgumentException("Value ["+value+"] has wrong class");
				}
			}
		}
	}
	
	public JDBManager(final LoggerFacade logger, final VirtualMachine vm) {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (vm == null) {
			throw new NullPointerException("Virtual machine can't be null"); 
		}
		else {
			this.logger = logger;
			this.vm = vm;
			this.t.setName("JDI."+this.getClass().getSimpleName());
			this.t.setDaemon(true);
			this.t.start();
			addContentChangeListener(ccl);
		}
	}

	@Override
	public void close() throws RuntimeException {
		this.t.interrupt();
		removeContentChangeListener(ccl);
		vm.dispose();
	}

	public InnerWatchersKeeper<VMWatcher> accessVMWatchers() {
		return vmRepo;
	}
	
	public InnerWatchersKeeper<ClassWatcher> accessClassWatchers() {
		return classRepo;
	}

	public InnerWatchersKeeper<ExceptionWatcher> accessExceptionWatchers() {
		return exceptionRepo;
	}
	
	@Override
	protected <T extends Event> void processEvent(final T event) {
		switch (VMEvents.valueOf(event)) {
			case CLASS_PREPARE_EVENT : case CLASS_UNLOAD_EVENT	:
				for(ClassWatcher item : accessClassWatchers()) {
					if (item.isStarted() && !item.isSuspended()) {
						item.processEvent(event);
					}
				}
				break;
			case METHOD_ENTRY_EVENT : case METHOD_EXIT_EVENT : 
				for(ClassWatcher item : accessClassWatchers()) {
					if (item.isStarted() && !item.isSuspended()) {
						for(MethodWatcher method : item.getMethods()) {
							if (method.isStarted() && !method.isSuspended()) {
								method.processEvent(event);
							}
						}
					}
				}
				break;
			case BREAKPOINT_EVENT : case STEP_EVENT :
				for(ClassWatcher item : accessClassWatchers()) {
					if (item.isStarted() && !item.isSuspended()) {
						for(MethodWatcher method : item.getMethods()) {
							if (method.isStarted() && !method.isSuspended()) {
								for(BreakpointWatcher bp : method) {
									if (bp.isStarted() && !bp.isSuspended()) {
										bp.processEvent(event);
									}
								}
							}
						}
					}
				}
				break;
			case WATCHPOINT_EVENT : case ACCESS_WATCHPOINT_EVENT : case MODIFICATION_WATCHPOINT_EVENT	:
				for(ClassWatcher item : accessClassWatchers()) {
					if (item.isStarted() && !item.isSuspended()) {
						for(FieldWatcher field : item.getFields()) {
							if (field.isStarted() && !field.isSuspended()) {
								field.processEvent(event);
							}
						}
					}
				}
				break;
			case MONITOR_CONTENDED_ENTERED_EVENT : case MONITOR_CONTENDED_ENTER_EVENT : case MONITOR_WAITED_EVENT : case MONITOR_WAIT_EVENT :
				for(ClassWatcher item : accessClassWatchers()) {
					if (item.isStarted() && !item.isSuspended()) {
						for(FieldWatcher field : item.getFields()) {
							if (field.isStarted() && !field.isSuspended()) {
								for(MonitorWatcher mon : field) {
									if (mon.isStarted() && !mon.isSuspended()) {
										mon.processEvent(event);
									}
								}
							}
						}
					}
				}
				break;
			case VM_DISCONNECT_EVENT : case VM_DEATH_EVENT	:
				t.interrupt();
			case VM_START_EVENT				: 
				for(VMWatcher item : accessVMWatchers()) {
					if (item.isStarted() && !item.isSuspended()) {
						item.processEvent(event);
					}
				}
				break;
			case THREAD_START_EVENT	: case THREAD_DEATH_EVENT	:
				for(VMWatcher item : accessVMWatchers()) {
					if (item.isStarted() && !item.isSuspended()) {
						for(ThreadWatcher thread : item) {
							if (thread.isStarted() && !thread.isSuspended()) {
								thread.processEvent(event);
							}
						}
					}
				}
				break;
			case EXCEPTION_EVENT			:
				for(ExceptionWatcher item : accessExceptionWatchers()) {
					if (item.isStarted() && !item.isSuspended()) {
						item.processEvent(event);
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	protected void prepareEventRequests(VirtualMachine vm) {
		throw new UnsupportedOperationException("This method is not supported for the class");
	}

	@Override
	protected void enableEventRequests(VirtualMachine vm) {
		throw new UnsupportedOperationException("This method is not supported for the class");
	}

	@Override
	protected void disableEventRequests(VirtualMachine vm) {
		throw new UnsupportedOperationException("This method is not supported for the class");
	}

	@Override
	protected void unprepareEventRequests(VirtualMachine vm) {
		throw new UnsupportedOperationException("This method is not supported for the class");
	}

	private void processEvents() {
        try{EventSet eventSet = null;
        
			while ((eventSet = vm.eventQueue().remove()) != null) {
			    for (Event event : eventSet) {
			    	if (isStarted() && !isSuspended()) {
			    		processEvent(event);
			    	}
			    }
		        vm.resume();
			}
		} catch (InterruptedException e) {
		} finally {
			try{stop();
			} catch (Exception exc) {
			}
		}
	}
	
	private void contentChanged(final DebugWatcher owner, final ContentChangeType type, final DebugWatcher item) {
		if (item instanceof AbstractDebugWatcher) {
			switch (type) {
				case CONTENT_ADDED		:
					((AbstractDebugWatcher)item).addStateChangeListener(scl);
					break;
				case CONTENT_REMOVED	:
					((AbstractDebugWatcher)item).removeStateChangeListener(scl);
					break;
				default	:
					throw new UnsupportedOperationException("Content change type ["+type+"] is not supported yet"); 
			}
		}
	}

	private void stateChanged(final StateChangeType type, final DebugWatcher item) {
		if (item instanceof AbstractDebugWatcher) {
			switch (type) {
				case STARTED	:
					((AbstractDebugWatcher)item).prepareEventRequests(vm);
					((AbstractDebugWatcher)item).enableEventRequests(vm);
					break;
				case SUSPENDED	:
					((AbstractDebugWatcher)item).disableEventRequests(vm);
					break;
				case RESUMED	:
					((AbstractDebugWatcher)item).enableEventRequests(vm);
					break;
				case STOPPED	:
					((AbstractDebugWatcher)item).unprepareEventRequests(vm);
					break;
				default:
					throw new UnsupportedOperationException("State change type ["+type+"] is not supported yet"); 
			}
		}
	}
	
	public static VirtualMachine launchApplication(final ProcessBuilder builder, final int timeout) throws IOException, IllegalConnectorArgumentsException {
		if (builder == null) {
			throw new NullPointerException("Process builder can't be null"); 
		}
    	else if (timeout < 0) {
    		throw new IllegalArgumentException("Timeout [timeout] can't be negative"); 
    	}
    	else {
    		final List<String>	commands = builder.command();

    		commands.add(1, "-Xdebug");
    		commands.add(1, "-Xrunjdwp:server=y");
    		builder.command(commands);
    		
    		final Process	p = builder.start();
    		
    		return attach2Application(p.pid(), timeout);
    	}
	}
	
    public static VirtualMachine attach2Application(final InetSocketAddress addr, final int timeout) throws IOException, IllegalConnectorArgumentsException {
    	if (addr == null) {
    		throw new NullPointerException("Inet address to attach can't be null"); 
    	}
    	else if (timeout < 0) {
    		throw new IllegalArgumentException("Timeout [timeout] can't be negative"); 
    	}
    	else {
    		for (AttachingConnector con : Bootstrap.virtualMachineManager().attachingConnectors()) {
    		    if (SOCKET_CONNECTOR.equals(con.getClass().getName())) {
    		    	final Map<String, Connector.Argument> 	arguments = con.defaultArguments();

    		    	for (Entry<String, Connector.Argument> item : arguments.entrySet()) {
    		    		switch (item.getKey()) {
    		    			case "host" 	:
    		    				item.getValue().setValue(addr.getHostName());
    		    				break;
    		    			case "port" 	: 
    		    				item.getValue().setValue(""+addr.getPort());
    		    				break;
    		    			case "timeout"	: 
    		    				item.getValue().setValue(""+timeout);
    		    				break;
    		    		}
    		    	}
    		    	return con.attach(arguments);
    		    }
    		}
    		throw new IOException("No TCP connectors available in the system");
    	}
    }

    public static VirtualMachine attach2Application(final long pid, final int timeout) throws IOException, IllegalConnectorArgumentsException {
    	if (pid <= 0) {
    		throw new IllegalArgumentException("Process id ["+pid+"] must be positive"); 
    	}
    	else if (timeout < 0) {
    		throw new IllegalArgumentException("Timeout [timeout] can't be negative"); 
    	}
    	else {
    		for (AttachingConnector con : Bootstrap.virtualMachineManager().attachingConnectors()) {
    		    if (PROCESS_CONNECTOR.equals(con.getClass().getName())) {
    		    	final Map<String, Connector.Argument> 	arguments = con.defaultArguments();

    		    	for (Entry<String, Connector.Argument> item : arguments.entrySet()) {
    		    		switch (item.getKey()) {
    		    			case "pid" 	:
    		    				item.getValue().setValue(""+pid);
    		    				break;
    		    			case "timeout"	: 
    		    				item.getValue().setValue(""+timeout);
    		    				break;
    		    		}
    		    	}
    		    	return con.attach(arguments);
    		    }
    		}
    		throw new IOException("No process connectors available in the system");
    	}
    }

    public static VirtualMachine attach2Application(final String name, final int timeout) throws IOException, IllegalConnectorArgumentsException {
    	if (name == null || name.isEmpty()) {
    		throw new IllegalArgumentException("Shared name to attach can't be null"); 
    	}
    	else if (timeout < 0) {
    		throw new IllegalArgumentException("Timeout [timeout] can't be negative"); 
    	}
    	else {
    		for (AttachingConnector con : Bootstrap.virtualMachineManager().attachingConnectors()) {
    		    if (SHARED_MEMORY_CONNECTOR.equals(con.getClass().getName())) {
    		    	final Map<String, Connector.Argument> 	arguments = con.defaultArguments();

    		    	for (Entry<String, Connector.Argument> item : arguments.entrySet()) {
    		    		switch (item.getKey()) {
    		    			case "name" 	:
    		    				item.getValue().setValue(name);
    		    				break;
    		    			case "timeout"	: 
    		    				item.getValue().setValue(""+timeout);
    		    				break;
    		    		}
    		    	}
    		    	return con.attach(arguments);
    		    }
    		}
    		throw new IOException("No shared memory connectors available in the system");
    	}
    }
}
