package chav1961.purelib.cdb;



import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanType;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ByteType;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharType;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatType;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerType;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.LongType;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.VoidType;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;

import chav1961.purelib.basic.LongIdMap;
import chav1961.purelib.basic.OrdinalSyntaxTree;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.interfaces.AppDebugInterface;
import chav1961.purelib.cdb.interfaces.AppDebugInterface.EventType;
import chav1961.purelib.cdb.interfaces.AppDebugInterface.Location;
import chav1961.purelib.streams.char2byte.CompilerUtils;
import chav1961.purelib.cdb.interfaces.ArrayWrapper;
import chav1961.purelib.cdb.interfaces.ClassWrapper;
import chav1961.purelib.cdb.interfaces.FieldWrapper;
import chav1961.purelib.cdb.interfaces.MethodWrapper;
import chav1961.purelib.cdb.interfaces.ObjectWrapper;
import chav1961.purelib.cdb.interfaces.StackWrapper;
import chav1961.purelib.cdb.interfaces.ThreadWrapper;
import chav1961.purelib.cdb.interfaces.VariableWrapper;

public class DbgClient {
	private static final Map<Integer,String>	STATES = new HashMap<>();

	static {
		STATES.put(ThreadReference.THREAD_STATUS_MONITOR,"monitor");
		STATES.put(ThreadReference.THREAD_STATUS_NOT_STARTED,"not started");
		STATES.put(ThreadReference.THREAD_STATUS_RUNNING,"running");
		STATES.put(ThreadReference.THREAD_STATUS_SLEEPING,"sleeping");
		STATES.put(ThreadReference.THREAD_STATUS_UNKNOWN,"unknown");
		STATES.put(ThreadReference.THREAD_STATUS_WAIT,"wait");
		STATES.put(ThreadReference.THREAD_STATUS_ZOMBIE,"zombie");
	}
	 
	public static AppDebugInterface connectTo(final long pid) throws DebuggingException {
        final VirtualMachineManager 	vmManager = Bootstrap.virtualMachineManager();

        for (AttachingConnector connector : vmManager.attachingConnectors()) {
            if(connector.defaultArguments().containsKey("pid")) {
            	final Map<String,Argument>	args = connector.defaultArguments();
            	
            	args.get("pid").setValue(Long.valueOf(pid).toString());
                try{return new AppDebugInterfaceImpl(connector.attach(args));
				} catch (IOException | IllegalConnectorArgumentsException e) {
			        throw new DebuggingException("Error attaching VM (PID-based):"+e.getLocalizedMessage(),e);
				}
            }
        }
        throw new DebuggingException("No PID-based connectors available in the system");
	}
	
	public static AppDebugInterface connectTo(final InetSocketAddress addr) throws DebuggingException {
        final VirtualMachineManager 	vmManager = Bootstrap.virtualMachineManager();

        for (AttachingConnector connector : vmManager.attachingConnectors()) {
            if(connector.defaultArguments().containsKey("host") && connector.defaultArguments().containsKey("port")) {
            	final Map<String,Argument>	args = connector.defaultArguments();
            	
            	args.get("host").setValue(addr.getHostName());
            	args.get("port").setValue(Integer.valueOf(addr.getPort()).toString());
                try{return new AppDebugInterfaceImpl(connector.attach(args));
				} catch (IOException | IllegalConnectorArgumentsException e) {
			        throw new DebuggingException("Error attaching VM (host-based):"+e.getLocalizedMessage(),e);
				}
            }
        }
        throw new DebuggingException("No host-based connectors available in the system");
	}
	
	
	private static class AppDebugInterfaceImpl implements AppDebugInterface {
		private final VirtualMachine				vm;
		private final LongIdMap<BreakpointRequest>	bp = new LongIdMap<>(BreakpointRequest.class);
		
		AppDebugInterfaceImpl(final VirtualMachine vm) {
			this.vm = vm;
		}

		@Override
		public void close() throws DebuggingException {
			try{vm.dispose();
			} catch (VMDisconnectedException exc) {
			}
		}

		@Override
		public Event[] waitEvent() throws InterruptedException, DebuggingException {
			return waitEvent(EventType.values());
		}

		@Override
		public Event[] waitEvent(final EventType... eventTypes) throws InterruptedException, DebuggingException {
			final EventSet 			eventSet = vm.eventQueue().remove();
			final List<Event>		result = new ArrayList<>();
			final Set<EventType>	whiteList = new HashSet<>();
			
			whiteList.addAll(Arrays.asList(eventTypes));
			for (com.sun.jdi.event.Event item : eventSet) {
				if (item instanceof BreakpointEvent) {
					result.add(new Event(){
						@Override
						public EventType getType() throws DebuggingException {
							return EventType.BreakPointEvent;
						}

						@Override
						public ThreadWrapper getThreadAssociated() throws DebuggingException {
							return new ThreadWrapperImpl(AppDebugInterfaceImpl.this,((BreakpointEvent) item).thread());
						}

						@Override
						public Location getLocation() throws DebuggingException {
							return new LocationImpl(AppDebugInterfaceImpl.this,((BreakpointEvent) item).location());
						}
						
						@Override
						public String toString() {
							try{return getType()+" : "+getLocation();
							} catch (DebuggingException e) {
								return super.toString();
							}
						}
					});
				}
				else if (item instanceof StepEvent) {
					vm.eventRequestManager().deleteEventRequest(item.request());
					result.add(new Event(){
						@Override
						public EventType getType() throws DebuggingException {
							return EventType.StepEvent;
						}

						@Override
						public ThreadWrapper getThreadAssociated() throws DebuggingException {
							return new ThreadWrapperImpl(AppDebugInterfaceImpl.this,((BreakpointEvent) item).thread());
						}

						@Override
						public Location getLocation() throws DebuggingException {
							return new LocationImpl(AppDebugInterfaceImpl.this,((BreakpointEvent) item).location());
						}
						
						@Override
						public String toString() {
							try{return getType()+" : "+getLocation();
							} catch (DebuggingException e) {
								return super.toString();
							}
						}
					});
				}
			}
			for (int index = result.size()-1; index >= 0; index--) {
				if (!whiteList.contains(result.get(index).getType())) {
					result.remove(index);
				}
			}
			return result.toArray(new Event[result.size()]);
		}

		@Override
		public String[] getThreadNames() throws DebuggingException {
			final List<ThreadReference> refs = vm.allThreads();
			final String[]				names = new String[refs.size()];
			
			for (int index = 0, maxIndex = names.length; index < maxIndex; index++) {
				names[index] = refs.get(index).name();
			}
			return names;
		}

		@Override
		public String[] getClassNames() throws DebuggingException {
			final List<ReferenceType>	refs = vm.allClasses();
			final String[]				names = new String[refs.size()];
			
			for (int index = 0, maxIndex = names.length; index < maxIndex; index++) {
				names[index] = refs.get(index).name();
			}
			return names;
		}

		@Override
		public String[] getClassNames(final Package... packages) throws DebuggingException {
			if (packages == null) {
				throw new NullPointerException("Packages list can't be null");
			}
			else {
				final SyntaxTreeInterface<Object>	tree = new OrdinalSyntaxTree<>();
				final List<String>		result = new ArrayList<>();
				
				for (Package item : packages) {
					tree.placeName(item.getName()+'.',null);
				}
				for (ReferenceType item : vm.allClasses()) {
					if (item == null) {
						throw new NullPointerException("Null values inside package list!");
					}
					else {
						final char[]	name = item.name().toCharArray();
						final long 		id = tree.seekName(name,0,name.length);
						
						if (id < -1 && tree.seekName(name,0,(int)(-id-1)) >= 0) {
							result.add(item.name());
						}
					}
				}
				return result.toArray(new String[result.size()]);
			}
		}

		@Override
		public ThreadWrapper getThread(String threadName) throws DebuggingException {
			if (threadName == null || threadName.isEmpty()) {
				throw new IllegalArgumentException("Thread name can't be null or empty");
			}
			else {
				for (ThreadReference item :vm.allThreads()) {
					if (threadName.equals(item.name())) {
						return new ThreadWrapperImpl(this,item);
					}
				}
			}
			throw new DebuggingException("Thread name ["+threadName+"] is misisng in the debuggee");
		}

		@Override
		public ClassWrapper getClass(final String className) throws DebuggingException {
			if (className == null || className.isEmpty()) {
				throw new IllegalArgumentException("Class name can't be null or empty");
			}
			else {
				for (ReferenceType item : vm.allClasses()) {
					if (className.equals(item.name())) {
						return new ClassWrapperImpl(this,(ClassType)item);
					}
				}
			}
			throw new DebuggingException("Class name ["+className+"] is misisng in the debuggee");
		}

		@Override
		public void removeAllBreakpoints() throws DebuggingException {
			for (long id = 0, maxId = bp.maxValue(); id <= maxId; id++) {
				bp.remove(id);
			}
		}

		@Override
		public void exit(int exitCode) throws DebuggingException {
			vm.exit(exitCode);
		}

		@Override
		public void removeBreakpoint(final int breakPoint) throws DebuggingException {
			if (breakPoint < 0) {
				throw new IllegalArgumentException("Break point id ["+breakPoint+"] can't be negative");
			}
			else if (!bp.contains(breakPoint)) {
				throw new IllegalArgumentException("Break point id ["+breakPoint+"] is missing in the breakpoint list");
			}
			else {
				final BreakpointRequest	rq = bp.remove(breakPoint);
				
				rq.disable();
				vm.eventRequestManager().deleteEventRequest(rq);
			}
		}
	}
	
	private static Class<?> type2Class(final Type type) throws ClassNotLoadedException, DebuggingException {
		if (type instanceof BooleanType) {
			return boolean.class;
		}
		else if (type instanceof ByteType) {
			return byte.class;
		}
		else if (type instanceof CharType) {
			return char.class;
		}
		else if (type instanceof ShortType) {
			return short.class;
		}
		else if (type instanceof IntegerType) {
			return int.class;
		}
		else if (type instanceof LongType) {
			return long.class;
		}
		else if (type instanceof FloatType) {
			return float.class;
		}
		else if (type instanceof DoubleType) {
			return double.class;
		}
		else if (type instanceof VoidType) {
			return void.class;
		}
		else if (type instanceof ClassType) {
			try{return  Thread.currentThread().getContextClassLoader().loadClass(type.name());
			} catch (ClassNotFoundException e) {
				throw new DebuggingException("Class [] can't be defined in current environment: "+e.getLocalizedMessage(),e);
			}			
		}
		else if (type instanceof ArrayType) {	// Resolve array to class
			int		depth = 0;
			Type	innerType = type;
			
			do {innerType = ((ArrayType)type).componentType();
				depth++;
			} while (innerType instanceof ArrayType);
			Class<?>	clazz = type2Class(innerType);
			
			if (clazz != null) {
				for (int index = 0; index < depth; index++) {
					clazz = Array.newInstance(clazz,0).getClass();
				}
				return clazz;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	private static Object value2Object(final AppDebugInterfaceImpl adi, final ThreadReference ref, final StackFrame frame, final Class<?> clazz, final Value val) throws ClassNotLoadedException, DebuggingException {
		switch (CompilerUtils.defineClassType(clazz)) {
			case CompilerUtils.CLASSTYPE_REFERENCE	:
				if (val instanceof StringReference) {
					return ((StringReference)val).value();
				}
				else if (val instanceof ArrayReference) {
					return new ArrayWrapperImpl(adi,ref,frame,clazz,(ArrayReference)val);
				}
				else if (val instanceof ThreadReference) {
					return new ThreadWrapperImpl(adi,(ThreadReference)val);
				}
				else if (val instanceof ThreadGroupReference) {
					throw new UnsupportedOperationException(); 
				}
				else if (val instanceof ClassObjectReference) {
					throw new UnsupportedOperationException(); 
				}
				else if (val instanceof ClassLoaderReference) {
					throw new UnsupportedOperationException(); 
				}
				else {
					return new ObjectWrapperImpl(adi,ref,frame,((ObjectReference)val).referenceType(),(ObjectReference)val);
				}
			case CompilerUtils.CLASSTYPE_BYTE		:
				return ((ByteValue)val).byteValue();
			case CompilerUtils.CLASSTYPE_SHORT		:
				return ((ShortValue)val).shortValue();
			case CompilerUtils.CLASSTYPE_CHAR		:	
				return ((CharValue)val).charValue();
			case CompilerUtils.CLASSTYPE_INT		:
				return ((IntegerValue)val).intValue();
			case CompilerUtils.CLASSTYPE_LONG		:	
				return ((LongValue)val).longValue();
			case CompilerUtils.CLASSTYPE_FLOAT		:
				return ((FloatValue)val).floatValue();
			case CompilerUtils.CLASSTYPE_DOUBLE		:
				return ((DoubleValue)val).doubleValue();
			case CompilerUtils.CLASSTYPE_BOOLEAN	:
				return ((BooleanValue)val).booleanValue();
			default : return null;
		}
	}
	
	private static class ThreadWrapperImpl implements ThreadWrapper {
		private final AppDebugInterfaceImpl	adi;
		private final ThreadReference		ref;
		
		ThreadWrapperImpl(final AppDebugInterfaceImpl adi, final ThreadReference ref) {
			this.adi = adi;
			this.ref = ref;
		}
		
		@Override
		public DebugExecutionControl getExecutionControl() throws DebuggingException {
			return new DebugExecutionControl() {
				@Override public void suspend() throws DebuggingException {ref.suspend();}
				@Override public void stop() throws DebuggingException {throw new IllegalStateException("Stop method can't be used for debugging connections");}
				@Override public void start() throws DebuggingException {throw new IllegalStateException("Start method can't be used for debugging connections");}
				@Override public void resume() throws DebuggingException {ref.resume();}
				@Override public boolean isSuspended() {return ref.isSuspended();}
				@Override public boolean isStarted() {return true;}
				
				@Override
				public void step() throws DebuggingException {
					final StepRequest	rq = adi.vm.eventRequestManager().createStepRequest(ref, StepRequest.STEP_LINE, StepRequest.STEP_OVER);
					
					rq.addCountFilter(1);
					rq.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					rq.enable();
				}
				
				@Override
				public void stepInto() throws DebuggingException {
					final StepRequest	rq = adi.vm.eventRequestManager().createStepRequest(ref, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
					
					rq.addCountFilter(1);
					rq.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					rq.enable();
				}
				
				@Override
				public void stepOut() throws DebuggingException {
					final StepRequest	rq = adi.vm.eventRequestManager().createStepRequest(ref, StepRequest.STEP_LINE, StepRequest.STEP_OUT);
					
					rq.addCountFilter(1);
					rq.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					rq.enable();
				}
				
				@Override
				public int setBreakpoint(final Class<?> clazz, final Method m, final int line) throws DebuggingException {
					if (clazz == null) {
						throw new NullPointerException("Class ref can't be null");
					}
					else if (m == null) {
						throw new NullPointerException("Method ref can't be null");
					}
					else if (line <= 0) {
						throw new IllegalArgumentException("Line number ["+line+"] must be positive");
					}
					else {
						final String	className = clazz.getCanonicalName();
						final String	method = CompilerUtils.buildMethodPath(m)+CompilerUtils.buildMethodSignature(m);
						
						for (ReferenceType item : adi.vm.allClasses()) {
							if (className.equals(item.name())) {
								for ( com.sun.jdi.Method innerItem : ((ClassType)item).allMethods()) { 
									final String	methodSignature = innerItem.declaringType().name()+"."+innerItem.name()+innerItem.signature();
									
									if (method.equals(methodSignature)) {
										try{for (com.sun.jdi.Location lines : innerItem.allLineLocations()) {
												if (lines.lineNumber() == line) {
													final BreakpointRequest	bpr = adi.vm.eventRequestManager().createBreakpointRequest(lines);
													final long				bpId = adi.bp.firstFree();
													
													bpr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
													bpr.enable();
													adi.bp.put(bpId,bpr);
													return (int)bpId;
												}
											}
										} catch (AbsentInformationException e) {
										}
									}								
								}
							}
						}
						throw new DebuggingException("Class ["+clazz+"] method ["+m+"] line ["+line+"] not found");
					}
				}
			};
		}

		@Override
		public String getCurrentState() throws DebuggingException {
			return STATES.get(ref.status());
		}
		
		@Override
		public Location getCurrentLocation() throws DebuggingException {
			try{return new LocationImpl(adi,ref.frame(0).location());
			} catch (IncompatibleThreadStateException e) {
				throw new DebuggingException("Incompatible thread state to call: "+e.getLocalizedMessage(),e);
			}
		}

		@Override
		public int getStackSize() throws DebuggingException {
			try{return ref.frameCount();
			} catch (IncompatibleThreadStateException e) {
				throw new DebuggingException("Incompatible thread state to call: "+e.getLocalizedMessage(),e);
			}
		}

		@Override
		public StackWrapper[] getStackContent() throws DebuggingException {
			try{final List<StackFrame> 	frames = ref.frames();
				final StackWrapper[]	result = new StackWrapper[frames.size()];
				
				for (int index = 0, maxIndex = result.length; index < maxIndex; index++) {
					result[index] = new StackWrapperImpl(adi,ref,frames.get(index));
				}
				return result;
			} catch (IncompatibleThreadStateException e) {
				throw new DebuggingException("Incompatible thread state to call: "+e.getLocalizedMessage(),e);
			}
		}

		@Override
		public StackWrapper getStackContent(final int depth) throws DebuggingException {
			if (depth < 0 || depth >= getStackSize()) {
				throw new IllegalArgumentException("Stack depth ["+depth+"] out of range 0.."+(getStackSize()-1));
			}
			else {
				try{return new StackWrapperImpl(adi,ref,ref.frame(depth));
				} catch (IncompatibleThreadStateException e) {
					throw new DebuggingException("Incompatible thread state to call: "+e.getLocalizedMessage(),e);
				}
			}
		}

		@Override
		public StackWrapper getStackContent(final String methodSignature) throws DebuggingException {
			if (methodSignature == null || methodSignature.isEmpty()) {
				throw new IllegalArgumentException("Method signature can't be null or empty");
			}
			else {
				for (int index = 0, maxIndex = getStackSize(); index < maxIndex; index++) {
					try{final StackFrame	f = ref.frame(index);
						final String		signature = f.location().method().signature();
						final String		clazz = f.location().method().declaringType().name();
						final String		method = f.location().method().name();
				
						if (methodSignature.equals(clazz+'.'+method+signature)) {
							return new StackWrapperImpl(adi,ref,f);
						}
					} catch (IncompatibleThreadStateException e) {
						throw new DebuggingException("Incompatible thread state to call: "+e.getLocalizedMessage(),e);
					}
				}
				throw new DebuggingException("Method with signature ["+methodSignature+"] is missing in stack trace calls");
			}
		}

		@Override
		public StackWrapper getStackContent(final Method method) throws DebuggingException {
			if (method == null) {
				throw new NullPointerException("Method can't be null");
			}
			else {
				return getStackContent(CompilerUtils.buildMethodPath(method)+CompilerUtils.buildMethodSignature(method));
			}
		}
	}
	
	private static class StackWrapperImpl implements StackWrapper {
		private final AppDebugInterfaceImpl	adi;
		private final ThreadReference 	ref;
		private final StackFrame 		frame;
		
		StackWrapperImpl(final AppDebugInterfaceImpl adi, final ThreadReference ref, final StackFrame frame) {
			this.adi = adi;
			this.ref = ref;
			this.frame = frame;
		}

		@Override
		public ObjectWrapper getThis() throws DebuggingException {
			final ObjectReference	obj = frame.thisObject();

			if (obj == null) {
				return null;
			}
			else {
				return new ObjectWrapperImpl(adi, ref, frame, obj.referenceType(), obj);
			}
		}
		
		@Override
		public String[] getVarNames() throws DebuggingException {
			try{final List<LocalVariable> 	vars = frame.visibleVariables();
				final String[]				names = new String[vars.size()];
				
				for (int index = 0, maxIndex = names.length; index < maxIndex; index++) {
					names[index] = vars.get(index).name();
				}			
				return names;
			} catch (AbsentInformationException exc) {
				throw new DebuggingException("Missing debugging information (location="+frame.location()+")"); 
			}
		}

		@Override
		public VariableWrapper getVar(final String name) throws DebuggingException {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Variable name can't be null or empty"); 
			}
			else {
				try{final LocalVariable 	variable = frame.visibleVariableByName(name);
				
					if (variable == null) {
						throw new DebuggingException("Missing variable ["+name+"]");
					}
					else {
						return new VariableWrapperImpl(adi,ref,frame,variable);
					}
				} catch (AbsentInformationException e) {
					throw new DebuggingException("Missing debugging information (location="+frame.location()+")"); 
				}
			}
		}
	}
	
	private static class VariableWrapperImpl implements VariableWrapper {
		private final AppDebugInterfaceImpl	adi;
		private final ThreadReference 		ref;
		private final StackFrame 			frame;
		private final LocalVariable 		variable;
		
		VariableWrapperImpl(final AppDebugInterfaceImpl adi, final ThreadReference ref, final StackFrame frame, final LocalVariable variable) {
			this.adi = adi;
			this.ref = ref;
			this.frame = frame;
			this.variable = variable;
		}

		@Override
		public String getName() throws DebuggingException {
			return variable.name();
		}

		@Override
		public Class<?> getType() throws DebuggingException {
			try{return type2Class(variable.type());
			} catch (ClassNotLoadedException e) {
				throw new DebuggingException(e);
			}
		}

		@Override
		public Object getValue() throws DebuggingException {
			final Class<?>	clazz = getType();
			final Value		val = frame.getValue(variable);
			
			try{return value2Object(adi,ref,frame,clazz,val);
			} catch (ClassNotLoadedException e) {
				throw new DebuggingException(e);
			}
		}
	}

	private static class ObjectWrapperImpl implements ObjectWrapper {
		private final AppDebugInterfaceImpl	adi;
		private final ThreadReference 		ref;
		private final StackFrame 			frame;
		private final ReferenceType			type;
		private final ObjectReference 		obj;
		
		ObjectWrapperImpl(final AppDebugInterfaceImpl adi, final ThreadReference ref, final StackFrame frame, final ReferenceType type, final ObjectReference obj) {
			this.adi = adi;
			this.ref = ref;
			this.frame = frame;
			this.type = type;
			this.obj = obj;
		}

		@Override
		public Class<?> contentType() throws DebuggingException {
			try{return type2Class(type.classObject().type());
			} catch (ClassNotLoadedException e) {
				throw new DebuggingException(e);
			}
		}

		@Override
		public String[] fields() throws DebuggingException {
			final List<Field> 	fields = type.visibleFields();
			final String[]		names = new String[fields.size()];
			
			for (int index = 0; index < names.length; index++) {
				names[index] = fields.get(index).name();
			}
			return names;
		}

		@Override
		public String[] methods() throws DebuggingException {
			final List<com.sun.jdi.Method>	methods = type.visibleMethods();
			final String[]		names = new String[methods.size()];
			
			for (int index = 0; index < names.length; index++) {
				names[index] = methods.get(index).name()+methods.get(index).signature();
			}
			return names;
		}

		@Override
		public Object get(final String field) throws DebuggingException {
			if (field == null || field.isEmpty()) {
				throw new IllegalArgumentException("Field can't be null or empty");
			}
			else {
				try{final ReferenceType	fieldType = type.fieldByName(field).declaringType();
					
					return value2Object(adi,ref,frame,type2Class(fieldType),obj.getValue(type.fieldByName(field)));
				} catch (ClassNotLoadedException e) {
					throw new DebuggingException(e);
				}
			}
		}
	}
	
	private static class ArrayWrapperImpl implements ArrayWrapper {
		private final AppDebugInterfaceImpl	adi;
		private final ThreadReference 		ref;
		private final StackFrame 			frame;
		private final Class<?> 				content;
		private final ArrayReference 		obj;
		
		ArrayWrapperImpl(final AppDebugInterfaceImpl adi, final ThreadReference ref, final StackFrame frame, final Class<?> content, final ArrayReference obj) {
			this.adi = adi;
			this.ref = ref;
			this.frame = frame;
			this.content = content;
			this.obj = obj;
		}

		@Override
		public Class<?> contentType() throws DebuggingException {
			return content.getComponentType();
		}

		@Override
		public int getLength() throws DebuggingException {
			return obj.length();
		}

		@Override
		public Object get(int index) throws DebuggingException {
			return obj.getValue(index);
		}

		@Override
		public Object get() throws DebuggingException {
			final int			length = getLength();
			final Class<?>		type = contentType();
			final Object		result = Array.newInstance(type.isPrimitive() || String.class.isAssignableFrom(type) ? type : ObjectWrapper.class,length);
			
			for (int index = 0; index < length; index++) {
				try{final Object	value =	value2Object(adi,ref,frame,contentType(),obj.getValue(index));
				
					switch (CompilerUtils.defineClassType(contentType())) {
						case CompilerUtils.CLASSTYPE_REFERENCE	: Array.set(result,index,value); break;
						case CompilerUtils.CLASSTYPE_BYTE		: Array.setByte(result,index,((Byte)value).byteValue()); break;
						case CompilerUtils.CLASSTYPE_SHORT		: Array.setShort(result,index,((Short)value).shortValue()); break;
						case CompilerUtils.CLASSTYPE_CHAR		: Array.setChar(result,index,((Character)value).charValue()); break;	
						case CompilerUtils.CLASSTYPE_INT		: Array.setInt(result,index,((Integer)value).intValue()); break;
						case CompilerUtils.CLASSTYPE_LONG		: Array.setLong(result,index,((Long)value).longValue()); break;	
						case CompilerUtils.CLASSTYPE_FLOAT		: Array.setFloat(result,index,((Float)value).floatValue()); break;
						case CompilerUtils.CLASSTYPE_DOUBLE		: Array.setDouble(result,index,((Double)value).doubleValue()); break;
						case CompilerUtils.CLASSTYPE_BOOLEAN	: Array.setBoolean(result,index,((Boolean)value).booleanValue()); break;
						default : return null;
					}
				} catch (ClassNotLoadedException e) {
					throw new DebuggingException(e);
				}  
			}
			return result;
		}
	}

	private static class ClassWrapperImpl implements ClassWrapper {
		private final AppDebugInterfaceImpl	adi;
		private final ClassType 			clazz;
		
		ClassWrapperImpl(final AppDebugInterfaceImpl adi, final ClassType clazz) {
			this.adi = adi;
			this.clazz = clazz;
		}

		@Override
		public String[] getFieldNames() throws DebuggingException {
			final List<Field> 	fields = clazz.visibleFields();
			final String[]		names = new String[fields.size()];
			
			for (int index = 0; index < names.length; index++) {
				names[index] = fields.get(index).name();
			}
			return names;
		}

		@Override
		public FieldWrapper getClassField(final String name) throws DebuggingException {
			final Field	f = clazz.fieldByName(name);
			
			if (!f.isStatic()) {
				return null;
			}
			else {
				return new FieldWrapperImpl(adi, clazz, f);
			}
		}

		@Override
		public String[] getMethodNames() throws DebuggingException {
			final List<com.sun.jdi.Method>	methods = clazz.visibleMethods();
			final String[]		names = new String[methods.size()];
			
			for (int index = 0; index < names.length; index++) {
				names[index] = methods.get(index).name()+methods.get(index).signature();
			}
			return names;
		}

		@Override
		public Class<?> contentType() throws DebuggingException {
			try{return Thread.currentThread().getContextClassLoader().loadClass(clazz.signature().substring(1).replace('/','.').replace(";",""));
			} catch (ClassNotFoundException e) {
				throw new DebuggingException(e); 
			}
		}
	}

	private static class FieldWrapperImpl implements FieldWrapper {
		private final AppDebugInterfaceImpl	adi;
		private final ClassType 			clazz;
		private final Field 				f;
		
		FieldWrapperImpl(final AppDebugInterfaceImpl adi, final ClassType clazz, final Field f) {
			this.adi = adi;
			this.clazz = clazz;
			this.f = f;
		}

		@Override
		public Class<?> contentType() throws DebuggingException {
			try{return type2Class(f.type());
			} catch (ClassNotLoadedException e) {
				throw new DebuggingException(e);
			}
		}


		@Override
		public String getName() throws DebuggingException {
			return f.name();
		}

		@Override
		public Object get() throws DebuggingException {
			try{return value2Object(adi, null, null, contentType(), clazz.getValue(f));
			} catch (ClassNotLoadedException e) {
				throw new DebuggingException(e);
			}
		}
	}
	
	private static class LocationImpl implements Location {
		private final AppDebugInterfaceImpl	adi;
		private final com.sun.jdi.Location	location;
		
		LocationImpl(final AppDebugInterfaceImpl adi, final com.sun.jdi.Location location) {
			this.adi = adi;
			this.location = location;
		}

		public ClassWrapper getClassInside() {
			final com.sun.jdi.Method	m = location.method();
			
			return new ClassWrapperImpl(adi,(ClassType)m.declaringType());
		}

		@Override
		public MethodWrapper getMethodInside() {
			final com.sun.jdi.Method	m = location.method();
			
			return new MethodWrapper() {
				@Override
				public String signature() throws DebuggingException {
					return m.signature();
				}
				
				@Override
				public Class<?> owner() throws DebuggingException {
					try{return type2Class(m.declaringType());
					} catch (ClassNotLoadedException e) {
						throw new DebuggingException(e);
					}
				}
				
				@Override
				public String name() throws DebuggingException {
					return m.name();
				}
			};
		}

		@Override
		public String getSourcePath() throws DebuggingException {
			final com.sun.jdi.Method	m = location.method();
			
			try{return m.declaringType().sourceName();
			} catch (AbsentInformationException e) {
				throw new DebuggingException("Incompatible thread state to call: "+e.getLocalizedMessage(),e);
			}
		}

		@Override
		public int getLineNo() {
			return location.lineNumber();
		}

		@Override
		public String toString() {
			try{return "LocationImpl ["+getClassInside().contentType()+":"+getMethodInside().name()+getMethodInside().signature()+" at line "+getLineNo()+"]";
			} catch (DebuggingException e) {
				return super.toString();
			}
		}
	}
}
