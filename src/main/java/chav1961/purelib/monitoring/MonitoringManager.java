package chav1961.purelib.monitoring;

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.monitoring.interfaces.MBeanEntity;
import chav1961.purelib.monitoring.interfaces.MonitoringManagerMBean;

public class MonitoringManager implements ExecutionControl, MonitoringManagerMBean, MBeanEntity {
	public static final String			PURELIB_MBEAN = "purelib.mbean";
	private final LoggerFacadeManager	lfm = new LoggerFacadeManager();
	private final MBeanServer 			server = ManagementFactory.getPlatformMBeanServer();
	private final ObjectName			monitoringName;
	private final Set<ObjectName>		registered = new HashSet<>();
	private volatile boolean			started = false, suspended = false;
	
	public MonitoringManager() {
		ObjectName	temp;
		
		try{temp = new ObjectName(getObjectName());
		} catch (MalformedObjectNameException e) {
			PureLibSettings.logger.severe("Monitoring manager initialization failure ("+e.getLocalizedMessage()+"). This feature will be disabled");
			temp = null;
		}
		this.monitoringName = temp;
	}	
	
	@Override
	public synchronized void start() throws Exception {
		if (monitoringName == null) {
			throw new EnvironmentException("Monitoring manager feature is disabled due to initialization errors"); 
		}
		else if (!isStarted()) {
			server.registerMBean(this, monitoringName);
			registerServices(registered);
			suspended = false;
			started = true;
		}
	}

	@Override
	public synchronized void suspend() throws Exception {
		if (monitoringName == null) {
			throw new EnvironmentException("Monitoring manager feature is disabled due to initialization errors"); 
		}
		else if (isStarted() && !isSuspended()) {
			unregisterServices(registered);
			suspended = true;
		}
	}

	@Override
	public synchronized void resume() throws Exception {
		if (monitoringName == null) {
			throw new EnvironmentException("Monitoring manager feature is disabled due to initialization errors"); 
		}
		else if (isStarted() && isSuspended()) {
			registerServices(registered);
			suspended = false;
		}
	}

	@Override
	public synchronized void stop() throws Exception {
		if (monitoringName == null) {
			throw new EnvironmentException("Monitoring manager feature is disabled due to initialization errors"); 
		}
		else if (isStarted()) {
			if (!isSuspended()) {
				unregisterServices(registered);
			}
			server.unregisterMBean(monitoringName);
			suspended = false;
			started = false;
		}
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public boolean isSuspended() {
		return suspended;
	}

	@Override
	public void close() throws RuntimeException {
		if (monitoringName != null) {
			try{stop();
			} catch (Exception e) {
			}
		}
		lfm.close();
	}

	@Override
	public String getObjectName() {
		return PURELIB_MBEAN+":type=basic,name=monitoring";
	}
	
	public LoggerFacadeManager getLoggerFacadeManager() {
		return lfm;
	}
	
	void registerServices(final Set<ObjectName> registered) {
		for (MBeanEntity item : new MBeanEntity[] {lfm}) {
			final String			name = item.getObjectName();
			
			try{final ObjectName	obj = new ObjectName(name);
				
				server.registerMBean(item,obj);
				registered.add(obj);
			} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
				PureLibSettings.logger.severe("Monitoring manager start/resume failure ("+e.getLocalizedMessage()+"). Service ["+name+"] will be disabled");
			}
		}
	}

	void unregisterServices(final Set<ObjectName> registered) {
		for (ObjectName item : registered) {
			try{
				server.unregisterMBean(item);
			} catch (MBeanRegistrationException | InstanceNotFoundException e) {
				PureLibSettings.logger.severe("Monitoring manager stop/suspend failure ("+e.getLocalizedMessage()+"). Service ["+item.getCanonicalName()+"] will be disabled");
			}
		}
		registered.clear();
	}
}
