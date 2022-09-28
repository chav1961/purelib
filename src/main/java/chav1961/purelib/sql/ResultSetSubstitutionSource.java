package chav1961.purelib.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import chav1961.purelib.basic.CharUtils.SubstitutionSource;

/**
 * <p>This class implements {@linkplain SubstitutionSource substitution source} for {@linkplain ResultSet}</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @see SubstitutionSource
 * @since 0.0.6
 */
public class ResultSetSubstitutionSource implements SubstitutionSource {
	private final ResultSet				rs;
	private final Map<String,Integer>	names = new HashMap<>();
	
	/**
	 * <p>Constructor of the class</p>
	 * @param rs result set to use. Can't be null
	 * @throws NullPointerException argument is null
	 * @throws SQLException on any SQL errors
	 */
	public ResultSetSubstitutionSource(final ResultSet rs) throws NullPointerException, SQLException{
		if (rs == null) {
			throw new NullPointerException("Result set can't be null");
		}
		else {
			final ResultSetMetaData	rsmd = rs.getMetaData();

			this.rs = rs;
			for(int index = 1; index <= rsmd.getColumnCount(); index++) {
				names.put(rsmd.getColumnName(index).toUpperCase(), index);
			}
		}
	}
	
	
	@Override
	public String getValue(final String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to substitute can't be null or empty"); 
		}
		else {
			if (hasKey(key)) {
				try{return rs.getString(names.get(key.toUpperCase()));
				} catch (SQLException e) {
					return "ERROR("+key+"): "+e.getLocalizedMessage();
				}
			}
			else {
				return key;
			}
		}
	}
	
	@Override
	public boolean hasKey(String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to test can't be null or empty"); 
		}
		else {
			return names.containsKey(key.toUpperCase());
		}
	}
}
