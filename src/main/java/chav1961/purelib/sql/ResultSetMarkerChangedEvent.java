package chav1961.purelib.sql;

import java.sql.ResultSet;

public class ResultSetMarkerChangedEvent {
	private final String	oldMarker, newMarker;
	private final ResultSet	oldResultSet, newResultSet;
	
	public ResultSetMarkerChangedEvent(final String oldMarker, final ResultSet oldResultSet, final String newMarker, final ResultSet newResultSet) {
		this.oldMarker = oldMarker;
		this.oldResultSet = oldResultSet;
		this.newMarker = newMarker;
		this.newResultSet = newResultSet;
	}
	
	public String getOldMarker() {
		return oldMarker;
	}

	public String getNewMarker() {
		return newMarker;
	}
	
	public ResultSet getOldResultSet() {
		return oldResultSet;
	}

	public ResultSet getNewResultSet() {
		return newResultSet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((newMarker == null) ? 0 : newMarker.hashCode());
		result = prime * result + ((newResultSet == null) ? 0 : newResultSet.hashCode());
		result = prime * result + ((oldMarker == null) ? 0 : oldMarker.hashCode());
		result = prime * result + ((oldResultSet == null) ? 0 : oldResultSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ResultSetMarkerChangedEvent other = (ResultSetMarkerChangedEvent) obj;
		if (newMarker == null) {
			if (other.newMarker != null) return false;
		} else if (!newMarker.equals(other.newMarker)) return false;
		if (newResultSet == null) {
			if (other.newResultSet != null) return false;
		} else if (!newResultSet.equals(other.newResultSet)) return false;
		if (oldMarker == null) {
			if (other.oldMarker != null) return false;
		} else if (!oldMarker.equals(other.oldMarker)) return false;
		if (oldResultSet == null) {
			if (other.oldResultSet != null) return false;
		} else if (!oldResultSet.equals(other.oldResultSet)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResultSetMarkerChangedEvent [oldMarker=" + oldMarker + ", newMarker=" + newMarker + ", oldResultSet=" + oldResultSet + ", newResultSet=" + newResultSet + "]";
	}
}
