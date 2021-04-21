package chav1961.purelib.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.growablearrays.InOutGrowableCharArray;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.json.intern.BasicRPCListener;
import chav1961.purelib.streams.char2byte.AsmWriter;
import chav1961.purelib.streams.char2char.PrintWriterWrapper;

public class JsonRPCFactory {
	private static final AtomicInteger	uniqueId = new AtomicInteger();
	private static final AsmWriter 		writer;
	
	static {
		try {
			writer = new AsmWriter(new ByteArrayOutputStream());
			writer.write(Utils.fromResource(JsonRPCFactory.class.getResource("rpcmacros.txt")));
		} catch (IOException e) {
			throw new PreparationException();
		}
	}
	
	@FunctionalInterface
	public interface Transport {
		int process(GrowableCharArray<?> in, InOutGrowableCharArray out);
	}
	
	public static <T> T createClientStub(final Class<T> clientInterface, final Transport transport) throws NullPointerException, IllegalArgumentException, EnvironmentException {
		if (clientInterface == null) {
			throw new NullPointerException("Client interface can't be null"); 
		}
		else if (!clientInterface.isInterface()) {
			throw new IllegalArgumentException("Client interface descriptor is a class, not interface!"); 
		}
		else if (transport == null) {
			throw new NullPointerException("Transport instance can't be null"); 
		}
		else {
			try{final Class<T>	stubClass = internalCreateClientStub(collectMethods(clientInterface),uniqueId.incrementAndGet(),writer); 
				
				return stubClass.getConstructor(Transport.class).newInstance(transport);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | IllegalStateException | IOException e) {
				throw new EnvironmentException("Error creating client stub for ["+clientInterface.getCanonicalName()+"] interface: "+e.getLocalizedMessage(),e);
			}
		}
	}
	
	public static <T> Transport createServerProcessor(final Class<T> clientInterface, final T serverProcessor) throws NullPointerException, IllegalArgumentException, EnvironmentException {
		if (clientInterface == null) {
			throw new NullPointerException("Client interface can't be null"); 
		}
		else if (!clientInterface.isInterface()) {
			throw new IllegalArgumentException("Client interface descriptor is a class, not interface!"); 
		}
		else if (serverProcessor == null) {
			throw new NullPointerException("Server processor instance can't be null"); 
		}
		else {
			try{final Class<T>	stubClass = internalCreateServerStub(collectMethods(clientInterface),uniqueId.incrementAndGet(),writer); 

				return new TransportImpl<T>(stubClass,serverProcessor);
			} catch (SecurityException | IllegalStateException | IOException e) {
				throw new EnvironmentException("Error creating client stub for ["+clientInterface.getCanonicalName()+"] interface: "+e.getLocalizedMessage(),e);
			}
		}
	}

	private static Map<String,MethodDescriptor> collectMethods(final Class<?> cl) {
		final Map<String,MethodDescriptor>	methods = new HashMap<>();  
		
		for (Method m : cl.getMethods()) {
			methods.put(m.getName(),new MethodDescriptor(m));
		}
		return methods;
	}
	
	private static <T> Class<T> internalCreateClientStub(final Map<String, MethodDescriptor> methods, final int unique, final AsmWriter writer) throws NullPointerException, IllegalStateException, IOException {
		// TODO Auto-generated method stub
		try(final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
			try(final AsmWriter				wr = writer.clone(baos);
				final PrintWriterWrapper	pww = new PrintWriterWrapper(wr)) {

				
			}
		}
		return null;
	}

	private static <T> Class<T> internalCreateServerStub(final Map<String, MethodDescriptor> methods, final int unique, final AsmWriter writer) throws NullPointerException, IllegalStateException, IOException {
		// TODO Auto-generated method stub
		try(final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
			try(final AsmWriter				wr = writer.clone(baos);
				final PrintWriterWrapper	pww = new PrintWriterWrapper(wr)) {
				final String				className = "JsonRPCServerStub"+unique; 
			
				pww.println("		.package "+BasicRPCListener.class.getPackageName());
				pww.println("		insertJsonImports");
				pww.println(className+" .class extends "+CompilerUtils.buildClassPath(BasicRPCListener.class));
				pww.println("		insertConstructor name=\""+className+"\"");
				pww.println(className+" .end");
			} catch (PrintingException e) {
				throw new IOException(e.getLocalizedMessage(),e); 
			}
		}
		return null;
	}

	
	private static class MethodDescriptor {
		final char[]		methodName;
		final Class<?>		retType;
		final char[][]		parameters;
		final Class<?>[]	parameterTypes;
		final Class<?>[]	throwables;
		
		MethodDescriptor(final Method m) {
			final int	pCount = m.getParameterCount();
			
			this.methodName = m.getName().toCharArray();
			this.retType = m.getReturnType();
			this.parameters = new char[pCount][];
			this.parameterTypes = new Class<?>[pCount];
			this.throwables = m.getExceptionTypes();
			
			final Parameter[]	parm = m.getParameters(); 
			for (int index = 0; index < pCount; index++) {
				parameters[index] = parm[index].getName().toCharArray();
				parameterTypes[index] =  parm[index].getType();
			}
		}

		@Override
		public String toString() {
			final StringBuilder	sb = new StringBuilder();
			
			for (char[] item : parameters) {
				sb.append(',').append(item);
			}
			
			return "MethodDescriptor [methodName=" + new String(methodName) + ", retType=" + retType
					+ ", parameters=(" + sb.substring(1) + "), parameterTypes="
					+ Arrays.toString(parameterTypes) + ", throws " + Arrays.toString(throwables) + " ]";
		}
	}

	private static class TransportImpl<T> implements Transport {
		TransportImpl(final Class<T> serverStub, final T serverProcessor) {
			
		}

		@Override
		public int process(final GrowableCharArray<?> in, final InOutGrowableCharArray out) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
}
