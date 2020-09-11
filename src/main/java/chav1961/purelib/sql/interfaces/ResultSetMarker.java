package chav1961.purelib.sql.interfaces;

public interface ResultSetMarker {
	String getResultSetMarker();
	void addResultSetMarkerChangedListener(ResultSetMarkerChangedListener listener);
	void removeResultSetMarkerChangedListener(ResultSetMarkerChangedListener listener);
}
