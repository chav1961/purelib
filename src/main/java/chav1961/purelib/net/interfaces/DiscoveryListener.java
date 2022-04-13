package chav1961.purelib.net.interfaces;

import chav1961.purelib.net.DiscoveryEvent;

@FunctionalInterface
public interface DiscoveryListener {
	default boolean filterEvent(DiscoveryEvent event) {
		return true;
	}
	
	void processEvent(DiscoveryEvent event);
}