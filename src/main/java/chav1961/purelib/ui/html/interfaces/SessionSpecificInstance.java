package chav1961.purelib.ui.html.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

public interface SessionSpecificInstance<Session,Instance> {
	Instance getInstance(Session session) throws ContentException;
}
