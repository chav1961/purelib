package chav1961.purelib.net.interfaces;

public enum DiscoveryEventType{
	START(false, true),
	PING(false, true),
	PONG(true, true),
	SUSPENDED(false, true),
	RESUMED(false, true),
	GET_STATE(false, true),
	STATE(true, true),
	QUERY_INFO(false, false),
	INFO(false, false),
	STOP(false, true);
	
	private final boolean	checkRandom;
	private final boolean	checkTimestamp;
	
	DiscoveryEventType(final boolean checkRandom, final boolean checkTimestamp) {
		this.checkRandom = checkRandom;
		this.checkTimestamp = checkTimestamp;
	}
	
	public boolean needCheckRandom() {
		return checkRandom;
	}

	public boolean needCheckTimestamp() {
		return checkTimestamp;
	}
}