package chav1961.purelib.net.interfaces;

import chav1961.purelib.net.AbstractDiscovery;
import chav1961.purelib.net.DiscoveryEvent;

@FunctionalInterface
public interface DiscoveryListener {
	void processEvent(DiscoveryEvent event);
}