package chav1961.purelib.cdb;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

import chav1961.purelib.basic.OrdinalSyntaxTree;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.interfaces.AppDebugInterface;
import chav1961.purelib.cdb.interfaces.InstanceWrapper;
import chav1961.purelib.cdb.interfaces.StackWrapper;
import chav1961.purelib.cdb.interfaces.ThreadWrapper;
import chav1961.purelib.cdb.interfaces.AppDebugInterface.Location;

class AppDebugInterfaceImpl implements AppDebugInterface {
	private final VirtualMachine	vm;
	
	AppDebugInterfaceImpl(final VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public void close() throws DebuggingException {
		// TODO Auto-generated method stub
	}

	@Override
	public Event waitEvent() throws InterruptedException, DebuggingException {
		return waitEvent(EventType.values());
	}

	@Override
	public Event waitEvent(final EventType... eventTypes) throws InterruptedException, DebuggingException {
		if (eventTypes == null) {
			throw new IllegalArgumentException("Event types can't be null");
		}
		else {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public String[] getThreadNames() throws DebuggingException {
		final List<String>	names = new ArrayList<>();
		
		for (ThreadReference item : vm.allThreads()) {
			names.add(item.name());
		}
		return names.toArray(new String[names.size()]);
	}

	@Override
	public String[] getClassNames() throws DebuggingException {
		final List<String>	names = new ArrayList<>();
		
		for (ReferenceType item : vm.allClasses()) {
			names.add(item.name());
		}
		return names.toArray(new String[names.size()]);
	}

	@Override
	public String[] getClassNames(final Package... packages) throws DebuggingException {
		final SyntaxTreeInterface<?> 	ordinal = new OrdinalSyntaxTree<>();	
		final List<String>				names = new ArrayList<>();
		int								minLength = Integer.MAX_VALUE;
		
		for (Package item : packages) {
			ordinal.placeOrChangeName(item.getName(),null);
			minLength = Math.min(minLength,item.getName().length());
		}
		for (ReferenceType item : vm.allClasses()) {
			if (ordinal.seekName(item.name()) <= -minLength) {
				names.add(item.name());
			}
		}
		return names.toArray(new String[names.size()]);
	}

	@Override
	public ThreadWrapper getThread(final String threadName) throws DebuggingException {
		if (threadName == null || threadName.isEmpty()) {
			throw new IllegalArgumentException("Thread name can't be null or empty"); 
		}
		else {
			for (ThreadReference item : vm.allThreads()) {
				if (threadName.equals(item.name())) {
					return null;
				}
			}
			throw new DebuggingException("Attempt to get access to non-existent thread ["+threadName+"]"); 
		}
	}

	@Override
	public InstanceWrapper getClass(final String className) throws DebuggingException {
		if (className == null || className.isEmpty()) {
			throw new IllegalArgumentException("Class name can't be null or empty"); 
		}
		else {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public void removeAllBreakpoints() throws DebuggingException {
		
	}

	@Override
	public void exit(final int exitCode) throws DebuggingException {
		vm.exit(exitCode);
	}
}