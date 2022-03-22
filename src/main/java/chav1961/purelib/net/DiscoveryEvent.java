package chav1961.purelib.net;

import java.util.EventObject;

import chav1961.purelib.net.interfaces.DiscoveryEventType;
import chav1961.purelib.net.interfaces.MediaItemDescriptor;

public class DiscoveryEvent extends EventObject {
	private static final long serialVersionUID = 5986987465176119261L;

	private final DiscoveryEventType	type;
	private final MediaItemDescriptor	desc;
	
	public DiscoveryEvent(final DiscoveryEventType eventType, final MediaItemDescriptor desc, final Object source) {
		super(source);
		this.type = eventType;
		this.desc = desc;
	}

	public DiscoveryEventType getEventType() {
		return type;
	}
	
	public MediaItemDescriptor getDescriptor() {
		return desc;
	}

	@Override
	public String toString() {
		return "DiscoveryEvent [type=" + type + ", desc=" + desc + "]";
	}
}