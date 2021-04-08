package chav1961.purelib.monitoring;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.monitoring.interfaces.LoggerFacadeManagerMBean;
import chav1961.purelib.monitoring.interfaces.MBeanEntity;

public class LoggerFacadeManager implements LoggerFacadeManagerMBean, MBeanEntity {

	@Override
	public String getLoggerFacadeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getObjectName() {
		return PureLibSettings.PURELIB_MBEAN+":type=basic,name=loggerFacades";
	}
}
