package chav1961.purelib.monitoring.interfaces;

public interface MBeanEntity extends AutoCloseable {
	String getObjectName();
	void close() throws RuntimeException;
}
