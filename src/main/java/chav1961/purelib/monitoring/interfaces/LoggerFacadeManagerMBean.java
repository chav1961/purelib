package chav1961.purelib.monitoring.interfaces;

public interface LoggerFacadeManagerMBean extends AutoCloseable {
	String getLoggerFacadeName();
	void close() throws RuntimeException;
}
