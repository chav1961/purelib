package chav1961.purelib.monitoring.interfaces;

public interface MonitoringManagerMBean {
	void suspend() throws Exception;
	void resume() throws Exception;
	boolean isSuspended();
}
