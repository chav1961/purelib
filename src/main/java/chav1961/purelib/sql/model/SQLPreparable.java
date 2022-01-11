package chav1961.purelib.sql.model;

import java.sql.SQLException;

import chav1961.purelib.sql.model.SQLModelUtils.ConnectionGetter;

interface SQLPreparable {
	void prepare(final ConnectionGetter getter) throws SQLException;
	
	boolean isPrepared();
	
	default void unprepare() throws SQLException {}
}
