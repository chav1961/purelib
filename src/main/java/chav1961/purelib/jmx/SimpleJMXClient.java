package chav1961.purelib.jmx;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class SimpleJMXClient implements Closeable {
	private final JMXConnector			conn;
	private final MBeanServerConnection	mbsc;		
	
	public SimpleJMXClient(final String host, final int port) throws IOException {
		this(URI.create("service:jmx:rmi:///jndi/rmi://" + (host == null ? "localhost" : host) + ":" + port + "/jmxrmi"));
	}	
	
	public SimpleJMXClient(final URI server) throws IOException {
		if (server == null) {
			throw new NullPointerException("Server URI can't be null");
		}
		else {
			final JMXServiceURL 	url = new JMXServiceURL(server.toString());
			
			this.conn = JMXConnectorFactory.connect(url, null);
			this.mbsc = conn.getMBeanServerConnection();		
		}
		
	}

	@Override
	public void close() throws IOException {
		conn.close();
	};
	
	public String[] getServerDomains() throws IOException {
		final String 	domains[] = mbsc.getDomains();
		
		Arrays.sort(domains);
		return domains;
	}

	public String getServerDefaultDomain() throws IOException {
		return mbsc.getDefaultDomain();
	}
	
	public ObjectName[] getServerMBeanNames() throws IOException {
		final Set<ObjectName>	temp = mbsc.queryNames(null, null);
		final ObjectName[]		result = temp.toArray(new ObjectName[temp.size()]);
		
		Arrays.sort(result);
		return result;
	}
	
	public <T> T getMBeanProxy(final ObjectName mbeanName, final Class<T> mBeanClass) throws InstanceNotFoundException, IOException {
		if (mbeanName == null) {
			throw new NullPointerException("mbeanName can't be null");
		}
		else if (mBeanClass == null) {
			throw new NullPointerException("mBeanClass can't be null");
		}
		else {
			final T	mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, mBeanClass, true);
	
			if (mbeanProxy instanceof NotificationListener) {
				mbsc.addNotificationListener(mbeanName, (NotificationListener)mbeanProxy, null, null);
			}
			else {
				mbsc.addNotificationListener(mbeanName, (n,h)->handleNotification(n, h), null, null);
			}
			return mbeanProxy;
		}
	}
	
	protected void handleNotification(final Notification notification, final Object handback) {
		
	}
}
