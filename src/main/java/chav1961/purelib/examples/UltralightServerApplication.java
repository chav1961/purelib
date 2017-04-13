package chav1961.purelib.examples;


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.sql.DataSource;

import chav1961.purelib.ultralight.UltralightServerContent;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
/*import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;*/

public class UltralightServerApplication {
/*	private static final String 	JMX_TURNON = "com.sun.management.jmxremote";
	private static final String 	JMX_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";
	private static final String 	JMX_ITEM = "ultralight:name=server";
	private static final String		USE_TEXT = "Use java -cp purelib-NNN.jar chav1961.purelib.ultralight.UltralightServerApplication [-p port] [-f <properties.file>] [-stop]";

	private static final String		PARM_POOL_CLASS = "ds.factory.class";
	private static final String		PARM_POOL_NAME = "ds.name";
	private static final String		PARM_POOL_SERVER = "ds.server";
	private static final String		PARM_POOL_DATABASE = "ds.database";
	private static final String		PARM_POOL_USER = "ds.user";
	private static final String		PARM_POOL_PASSWORD = "ds.password";
	private static final String		PARM_POOL_MAX_CONN = "ds.maxConnections";	
	private static final String		PARM_FILESYSTEM_URL = "fsys.url";

	public static boolean process(final String[] args) {
		if (args == null || args.length == 0) {
			throw new IllegalArgumentException("Argument list can't be null or empty array!");
		}
		else {
			try{final ObjectName 	objName = new ObjectName(JMX_ITEM);
				File				propFile = null;
				int					port = 8000;
				boolean				stop = false;
				
				for (int index = 0; index < args.length; index++) {
					switch (args[index]) {
						case "-p" 		:
							if (index < args.length-1) {
								try{port = Integer.valueOf(args[++index]);
								} catch (NumberFormatException exc) {
									System.err.println("Illegal port number ["+args[index+1]+"] for the [-p] parameter! ["+args[index]+"]. ");
									return false;
								}
							}
							else {
								System.err.println("Port number is missing for the [-p] parameter! ["+args[index]+"]. "+USE_TEXT);
								return false;
							}
							break;
						case "-f" 		:
							if (index < args.length-1) {
								propFile = new File(args[++index]);
								if (!propFile.exists() || !propFile.canRead()) {
									System.err.println("Property file ["+propFile.getAbsolutePath()+"] is not exists or not asccessible. "+USE_TEXT);
									return false;
								}
							}
							else {
								System.err.println("Properties file is missing for the [-f] parameter! ["+args[index]+"]. "+USE_TEXT);
								return false;
							}
							break;
						case "-stop" 	:
							stop = true;
							break;
						default :
							System.err.println("Unknown parameter ["+args[index]+"]! "+USE_TEXT);
							return false;
					}
				}
				
				if (System.getProperties().containsKey(JMX_TURNON)) {
					System.err.println("Server can't be started with ["+JMX_TURNON+"] key typed!");
					return false;
				}
				
				if (!stop) {
					final Properties	props = new Properties();
					
					try(final InputStream	is = UltralightServerApplication.class.getResourceAsStream("default.properties")) {
						props.load(is);
					} catch (IOException e) {
						System.err.println("Internal error "+e.getClass().getName()+": "+e.getMessage());
						return false;
					}
					
					if (propFile != null) {
						try(final InputStream	is = new FileInputStream(propFile)) {
							props.load(is);
						} catch (IOException e) {
							System.err.println("I/O error loading properties from ["+propFile.getAbsolutePath()+"]: "+e.getMessage());
							return false;
						}
					}					
					
					try(final ServerSocket	ss = new ServerSocket(port)){
					} catch (IOException exc) {
						System.err.println("Port ["+port+"] is already busy! Select free port number");
						return false;
					}
					
					final DataSource		ds = initDataSource(props);
					
					try{final MBeanServer 	jmx = ManagementFactory.getPlatformMBeanServer();
						final HttpServer 	server = HttpServer.create(new InetSocketAddress(port), 0);
						final UltralightServerContent	context = new UltralightServerContent(props,ds); 
						final Smooth		smooth = new Smooth(jmx,server,objName,ds,context); 
						
						jmx.registerMBean(smooth,objName);
						server.createContext("/", context);
						server.setExecutor(Executors.newCachedThreadPool());
						server.start();
						return true;
					} catch (IOException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException  e) {
						e.printStackTrace();
						return false;
					}
				}
				else {
					for (VirtualMachineDescriptor item : VirtualMachine.list()) {	// Magic code to make local JMX connection to the working server
						if (item.displayName().startsWith(UltralightServerApplication.class.getName())) {
							VirtualMachine 	vm = null;
							
							try{vm = VirtualMachine.attach(item.id());
						    	final JMXServiceURL		url = new JMXServiceURL(getConnectionAddress(vm));
						    	try(final JMXConnector	conn = JMXConnectorFactory.connect(url)) {
							    	final MBeanServerConnection	mbean = conn.getMBeanServerConnection();
							    	
							    	mbean.invoke(objName,"ping",new Object[0],new String[0]);
						    	}
						    	return true;
							} catch (InstanceNotFoundException e) {
								System.err.println("No any servers found to stop now!");
							} catch (AttachNotSupportedException | IOException | AgentLoadException | AgentInitializationException | MBeanException | ReflectionException e) {
								System.err.println("Internal error "+e.getClass().getName()+": "+e.getMessage());
							} finally {
								if (vm != null) {
									try{vm.detach();} catch (IOException e) {}
								}
							}
							return false;
						}
					}
					System.err.println("No any servers found to stop now!");
					return false;
				}
			} catch (MalformedObjectNameException e) {
				System.err.println("Internal error "+e.getClass().getName()+": "+e.getMessage());
				return false;
			}
		}
	}
	
	private static String getConnectionAddress(final VirtualMachine vm) throws IOException, AgentLoadException, AgentInitializationException {
    	final String 	connectorAddress = vm.getAgentProperties().getProperty(JMX_CONNECTOR_ADDRESS);
    	
    	if (connectorAddress == null) {
    		final String 	agent = vm.getSystemProperties().getProperty("java.home") + File.separator + "lib" + File.separator + "management-agent.jar";
      
    		vm.loadAgent(agent);
    		return vm.getAgentProperties().getProperty(JMX_CONNECTOR_ADDRESS);
    	}
    	else {
    		return connectorAddress;
    	}
	}

	private static DataSource initDataSource(final Properties props) {
		if (props.containsKey(PARM_POOL_CLASS)) {
			try{final Class<DataSource>		cl = (Class<DataSource>) Class.forName(props.getProperty(PARM_POOL_CLASS));
				return (DataSource) cl.getMethod("createDataSource",Properties.class).invoke(0,props);
			} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException exc) {
				throw new IllegalArgumentException("DataSource factory class ["+props.getProperty(PARM_POOL_CLASS)+"]: "+exc.getClass().getSimpleName()+" - "+exc.getMessage()); 
			}
		}
		else {
			return null;
		}
//		source.setDataSourceName(props.getProperty(POOL_NAME));
//		source.setServerName(props.getProperty(POOL_SERVER));
//		source.setDatabaseName(props.getProperty(POOL_DATABASE));
//		source.setUser(props.getProperty(POOL_USER));
//		source.setPassword(props.getProperty(POOL_PASSWORD));
//		source.setMaxConnections(Integer.valueOf(props.getProperty(POOL_MAX_CONN)));
//		return source;
	}
	
	public interface SmoothMBean {
		void ping();
	}
	
	private static class Smooth implements SmoothMBean {
		private final MBeanServer 	jmx;
		private final HttpServer	server;
		private final ObjectName	objName;
		private final DataSource	ds;
		private final UltralightServerContent	context;
		
		Smooth(final MBeanServer jmx, final HttpServer server,  final ObjectName objName, final DataSource ds, final UltralightServerContent context) {
			this.jmx = jmx;			this.server = server;
			this.objName = objName;	this.ds = ds;
			this.context = context;
		}

		@Override
		public void ping() {
			try{jmx.unregisterMBean(objName);
				server.stop(0);
				((Closeable)context).close();
				if (ds != null && (ds instanceof Closeable)) {
					((Closeable)ds).close();
				}
			} catch (MBeanRegistrationException | IOException | InstanceNotFoundException e) {
				System.err.println("Internal error "+e.getClass().getName()+": "+e.getMessage());
				System.exit(128);
			}
		}
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println(USE_TEXT);
			System.exit(128);
		}
		else if (!process(args)) {
			System.exit(128);
		}		
	}
	*/
}
