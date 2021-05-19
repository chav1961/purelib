package chav1961.purelib.ui.html.interfaces;


import java.util.Map;

import chav1961.purelib.basic.exceptions.ContentException;

public interface SessionRepo<Id, SC extends SessionContent> extends Iterable<Map.Entry<Id,SessionContent>> {
	public Id newSession(SC content);

	public boolean hasSessionContent(Id sessionId);
	
	public SC getSessionContent(Id sessionId) throws ContentException;

	public SC setSessionContent(Id sessionId) throws ContentException;
	
	public SC removeSessionContent(Id sessionId) throws ContentException;
}
