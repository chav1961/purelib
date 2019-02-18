package chav1961.purelib.cdb;


import java.awt.Frame;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.VoidType;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

import chav1961.purelib.basic.OrdinalSyntaxTree;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.interfaces.AppDebugInterface;
import chav1961.purelib.cdb.interfaces.AppDebugInterface.Location;
import chav1961.purelib.cdb.interfaces.ArrayWrapper;
import chav1961.purelib.cdb.interfaces.InstanceWrapper;
import chav1961.purelib.cdb.interfaces.ObjectWrapper;
import chav1961.purelib.cdb.interfaces.StackWrapper;
import chav1961.purelib.cdb.interfaces.ThreadWrapper;
import chav1961.purelib.cdb.interfaces.VariableWrapper;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.streams.char2byte.asm.CompilerUtils;

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
		public Event waitEvent(EventType... eventTypes) throws InterruptedException, DebuggingException {
			// TODO Auto-generated method stub
			return null;
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
						return new ThreadWrapperImpl(vm,item);
					}
				}
			}
			throw new DebuggingException("Thread name ["+threadName+"] is misisng in the debuggee");
		}

		@Override
		public InstanceWrapper getClass(String className) throws DebuggingException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void removeAllBreakpoints() throws DebuggingException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void exit(int exitCode) throws DebuggingException {
			// TODO Auto-generated method stub
			
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
	
	private static Object value2Object(final VirtualMachine vm, final ThreadReference ref, final StackFrame frame, final Class<?> clazz, final Value val) throws ClassNotLoadedException, DebuggingException {
		switch (Utils.defineClassType(clazz)) {
			case Utils.CLASSTYPE_REFERENCE	:
				if (val instanceof StringReference) {
					return ((StringReference)val).value();
				}
				else if (val instanceof ArrayReference) {
					return new ArrayWrapperImpl(vm,ref,frame,clazz,(ArrayReference)val);
				}
				else if (val instanceof ThreadReference) {
					return new ThreadWrapperImpl(vm,(ThreadReference)val);
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
					return new ObjectWrapperImpl(vm,ref,frame,(ObjectReference)val);
				}
			case Utils.CLASSTYPE_BYTE		:
				return ((ByteValue)val).byteValue();
			case Utils.CLASSTYPE_SHORT		:
				return ((ShortValue)val).shortValue();
			case Utils.CLASSTYPE_CHAR		:	
				return ((CharValue)val).charValue();
			case Utils.CLASSTYPE_INT		:
				return ((IntegerValue)val).intValue();
			case Utils.CLASSTYPE_LONG		:	
				return ((LongValue)val).longValue();
			case Utils.CLASSTYPE_FLOAT		:
				return ((FloatValue)val).floatValue();
			case Utils.CLASSTYPE_DOUBLE		:
				return ((DoubleValue)val).doubleValue();
			case Utils.CLASSTYPE_BOOLEAN	:
				return ((BooleanValue)val).booleanValue();
			default : return null;
		}
	}
	
	
	
	private static class ThreadWrapperImpl implements ThreadWrapper {
		private final VirtualMachine	vm;
		private final ThreadReference	ref;
		
		ThreadWrapperImpl(final VirtualMachine vm, final ThreadReference ref) {
			this.vm = vm;
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
					// TODO Auto-generated method stub
					
				}
				@Override
				public void stepInto() throws DebuggingException {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void stepOut() throws DebuggingException {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void run() throws DebuggingException {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void setBreakpoint() throws DebuggingException {
					// TODO Auto-generated method stub
					
				}
			};
		}

		@Override
		public String getCurrentState() throws DebuggingException {
			return STATES.get(ref.status());
		}
		
		@Override
		public Location getCurrentLocation() throws DebuggingException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getStackSize() throws DebuggingException {
			try{return ref.frameCount();
			} catch (IncompatibleThreadStateException e) {
				e.printStackTrace();
				throw new DebuggingException("Incompatible thread state to call: "+e.getLocalizedMessage(),e);
			}
		}

		@Override
		public StackWrapper[] getStackContent() throws DebuggingException {
			try{final List<StackFrame> 	frames = ref.frames();
				final StackWrapper[]	result = new StackWrapper[frames.size()];
				
				for (int index = 0, maxIndex = result.length; index < maxIndex; index++) {
					result[index] = new StackWrapperImpl(vm,ref,frames.get(index));
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
				try{return new StackWrapperImpl(vm,ref,ref.frame(depth));
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
							return new StackWrapperImpl(vm,ref,f);
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
		private final VirtualMachine 	vm;
		private final ThreadReference 	ref;
		private final StackFrame 		frame;
		
		StackWrapperImpl(final VirtualMachine vm, final ThreadReference ref, final StackFrame frame) {
			this.vm = vm;
			this.ref = ref;
			this.frame = frame;
		}

		@Override
		public Object getThis() throws DebuggingException {
			return frame.thisObject();
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
						return new VariableWrapperImpl(vm,ref,frame,variable);
					}
				} catch (AbsentInformationException e) {
					throw new DebuggingException("Missing debugging information (location="+frame.location()+")"); 
				}
			}
		}
	}
	
	private static class VariableWrapperImpl implements VariableWrapper {
		private final VirtualMachine 	vm;
		private final ThreadReference 	ref;
		private final StackFrame 		frame;
		private final LocalVariable 	variable;
		
		VariableWrapperImpl(final VirtualMachine vm, final ThreadReference ref, final StackFrame frame, final LocalVariable variable) {
			this.vm = vm;
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
			
			try{return value2Object(vm,ref,frame,clazz,val);
			} catch (ClassNotLoadedException e) {
				throw new DebuggingException(e);
			}
		}

		@Override
		public void setValue(Object newValue) throws DebuggingException {
			// TODO Auto-generated method stub
//			frame.setValue(variable,);
		}
	}

	private static class ObjectWrapperImpl implements ObjectWrapper {
		private final VirtualMachine 	vm;
		private final ThreadReference 	ref;
		private final StackFrame 		frame;
		private final ObjectReference 	obj;
		
		ObjectWrapperImpl(final VirtualMachine vm, final ThreadReference ref, final StackFrame frame, final ObjectReference obj) {
			this.vm = vm;
			this.ref = ref;
			this.frame = frame;
			this.obj = obj;
		}

		@Override
		public Class<?> contentType() throws DebuggingException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ObjectWrapper superValue() throws DebuggingException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] fields() throws DebuggingException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] declaredFields() throws DebuggingException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] methods() throws DebuggingException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] declaredMethods() throws DebuggingException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object get(String field) throws DebuggingException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void set(String field, Object value) throws DebuggingException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object invoke(String methodSignature, Object... parameters) throws DebuggingException {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	private static class ArrayWrapperImpl implements ArrayWrapper {
		private final VirtualMachine 	vm;
		private final ThreadReference 	ref;
		private final StackFrame 		frame;
		private final Class<?> 			content;
		private final ArrayReference 	obj;
		
		ArrayWrapperImpl(final VirtualMachine vm, final ThreadReference ref, final StackFrame frame, final Class<?> content, final ArrayReference obj) {
			this.vm = vm;
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
			final Object		result = Array.newInstance(contentType(),length);
			
			for (int index = 0; index < length; index++) {
				try{final Object	value =	value2Object(vm,ref,frame,contentType(),obj.getValue(index));
				
					switch (Utils.defineClassType(contentType())) {
						case Utils.CLASSTYPE_REFERENCE	: Array.set(result,index,value); break;
						case Utils.CLASSTYPE_BYTE		: Array.setByte(result,index,((Byte)value).byteValue()); break;
						case Utils.CLASSTYPE_SHORT		: Array.setShort(result,index,((Short)value).shortValue()); break;
						case Utils.CLASSTYPE_CHAR		: Array.setChar(result,index,((Character)value).charValue()); break;	
						case Utils.CLASSTYPE_INT		: Array.setInt(result,index,((Integer)value).intValue()); break;
						case Utils.CLASSTYPE_LONG		: Array.setLong(result,index,((Long)value).longValue()); break;	
						case Utils.CLASSTYPE_FLOAT		: Array.setFloat(result,index,((Float)value).floatValue()); break;
						case Utils.CLASSTYPE_DOUBLE		: Array.setDouble(result,index,((Double)value).doubleValue()); break;
						case Utils.CLASSTYPE_BOOLEAN	: Array.setBoolean(result,index,((Boolean)value).booleanValue()); break;
						default : return null;
					}
				} catch (ClassNotLoadedException e) {
					throw new DebuggingException(e);
				}  
			}
			return result;
		}
		
		@Override
		public void set(int index, Object value) throws DebuggingException {
			// TODO Auto-generated method stub
			
		}
		
	}
}
