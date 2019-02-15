package chav1961.purelib.cdb;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.cdb.interfaces.AppDebugInterface;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.VirtualMachine;


public class DbgClient {
	public static AppDebugInterface connectTo(final InetSocketAddress addr) throws DebuggingException {
        try{final VirtualMachine	vm = new Connections().connect(addr.getHostName(),addr.getPort());
        
        	return new AppDebugInterfaceImpl(vm);
        } catch (Exception exc){
            throw new DebuggingException(exc.getLocalizedMessage(),exc);
        }
	}
	
	static class Connections {
	    public VirtualMachine connect(String host,int port) throws IOException {
	        String strPort = Integer.toString(port);
	        AttachingConnector connector = getConnector();
	        try {
	            VirtualMachine vm = connect(connector,host, strPort);
	            return vm;
	        } catch (IllegalConnectorArgumentsException e) {
	            throw new IllegalStateException(e);
	        }
	    }
	
	    private AttachingConnector getConnector() {
	        final VirtualMachineManager 	vmManager = Bootstrap.virtualMachineManager();

	        for (Connector connector : vmManager.attachingConnectors()) {
	            if("com.sun.jdi.SocketAttach".equals(connector.name())) {
	                return (AttachingConnector) connector;
	            }
	        }
	        throw new IllegalStateException();
	    }

	    private VirtualMachine connect(AttachingConnector connector,String host,String port) throws IllegalConnectorArgumentsException, IOException {
	        Map<String, Connector.Argument> args = connector.defaultArguments();
	        
	        Connector.Argument portArg = args.get("port");
	        portArg.setValue(port);
	        Connector.Argument addressArg = args.get("hostname");
	        addressArg.setValue(host);

	        return connector.attach(args);
	    }
	}
	
}
