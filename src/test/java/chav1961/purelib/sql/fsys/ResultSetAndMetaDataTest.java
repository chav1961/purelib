package chav1961.purelib.sql.fsys;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.sql.AbstractResultSetMetaData;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.InMemoryReadOnlyResultSet;
import chav1961.purelib.sql.InternalUtils;
import chav1961.purelib.sql.NullReadOnlyResultSet;
import chav1961.purelib.sql.InternalUtils;

public class ResultSetAndMetaDataTest {
	@Test
	public void metaDataTest() throws SQLException {
		final ResultSetMetaData	rsmd = new PseudoResultSetMetaData(true,"CHAR:VARCHAR(100)","NUMBER:NUMERIC(10,2)","DATE:DATE");
		
		Assert.assertEquals(rsmd.getColumnCount(),3);
		Assert.assertEquals(rsmd.getColumnName(1),"CHAR");
		Assert.assertEquals(rsmd.getColumnType(1),Types.VARCHAR);
		Assert.assertEquals(rsmd.getColumnLabel(1),"CHAR");
		Assert.assertEquals(rsmd.getColumnTypeName(1),"VARCHAR");
		Assert.assertEquals(rsmd.getColumnDisplaySize(1),100);
		Assert.assertEquals(rsmd.getPrecision(1),100);
		Assert.assertEquals(rsmd.getScale(1),0);
		Assert.assertEquals(rsmd.getColumnClassName(1),String.class.getName());
		Assert.assertFalse(rsmd.isAutoIncrement(1));
		Assert.assertTrue(rsmd.isReadOnly(1));
		Assert.assertFalse(rsmd.isWritable(1));
		Assert.assertFalse(rsmd.isDefinitelyWritable(1));
		Assert.assertFalse(rsmd.isCurrency(1));
		Assert.assertTrue(rsmd.isSigned(1));
		Assert.assertFalse(rsmd.isSearchable(1));
		Assert.assertEquals(rsmd.isNullable(1),ResultSetMetaData.columnNullableUnknown);
		
		Assert.assertTrue(rsmd.isWrapperFor(PseudoResultSetMetaData.class));
		Assert.assertFalse(rsmd.isWrapperFor(String.class));
		Assert.assertEquals(rsmd.unwrap(PseudoResultSetMetaData.class),rsmd);
		
		try{new PseudoResultSetMetaData(true,(String[])null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{rsmd.isSearchable(0);
			Assert.fail("Mandatory exception was not detected (column number outside the range)");
		} catch (IllegalArgumentException exc) {
		}
		try{rsmd.isSearchable(100);
			Assert.fail("Mandatory exception was not detected (column number outside the range)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Test
	public void nullResultSetTest() throws SQLException {
		try(final ResultSet		rs = new NullReadOnlyResultSet(new FSysResultSetMetaData(null,null,null,true,"NAME:VARCHAR(100)"),ResultSet.TYPE_FORWARD_ONLY)) {
			
			Assert.assertEquals(rs.getMetaData().getColumnCount(),1);

			Assert.assertEquals(rs.findColumn("NAME"),1);
			Assert.assertEquals(rs.findColumn("UNKNOWN"),0);
			try{rs.findColumn(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {			
			}
			try{rs.findColumn("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {			
			}
			
			Assert.assertNull(rs.getStatement());
	
			Assert.assertTrue(rs.isBeforeFirst());
			Assert.assertFalse(rs.isAfterLast());
			Assert.assertFalse(rs.isClosed());
			Assert.assertFalse(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			
			Assert.assertFalse(rs.next());
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertTrue(rs.isAfterLast());
			
			try{rs.previous();
				Assert.fail("Mandatory exception was not detected (attempt to move non-forward in forward-only cursor)");
			} catch (SQLException exc) {			
			}
			
			try{rs.getString(1);
				Assert.fail("Mandatory exception was not detected (attempt to read from empty result set)");
			} catch (SQLException exc) {			
			}
		}
		
		try(final ResultSet		rs = new NullReadOnlyResultSet(new FSysResultSetMetaData(null,null,null,true,"NAME:VARCHAR(100)"),ResultSet.TYPE_SCROLL_INSENSITIVE)) {
			
			Assert.assertEquals(rs.getMetaData().getColumnCount(),1);
			Assert.assertNull(rs.getStatement());
	
			Assert.assertTrue(rs.isBeforeFirst());
			Assert.assertFalse(rs.isAfterLast());
			Assert.assertFalse(rs.isClosed());
			Assert.assertFalse(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			
			Assert.assertFalse(rs.next());
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertTrue(rs.isAfterLast());
			
			rs.previous();
			Assert.assertTrue(rs.isBeforeFirst());
			Assert.assertFalse(rs.isAfterLast());
			
			try{rs.getString(1);
				Assert.fail("Mandatory exception was not detected (attempt to read from empty result set)");
			} catch (SQLException exc) {			
			}
		}
	}	

	@Test
	public void inMemoryResultSetTest() throws SQLException, IOException {
		try(final ResultSet		rs = new InMemoryReadOnlyResultSet(new FSysResultSetMetaData(null,null,null,true
																				,"NAME:VARCHAR(100)"
																				,"URL:VARCHAR(100)"
																				,"BOOL:BOOLEAN"
																				,"BYTE:BLOB"
																				,"NULL:VARCHAR(100)")
																		, ResultSet.TYPE_FORWARD_ONLY
																		, new ArrayContent(new Object[]{"100","http://localhost:80",true,new byte[]{1,2,3},null}))) {
			Assert.assertEquals(rs.getMetaData().getColumnCount(),5);
			Assert.assertTrue(rs.isBeforeFirst());
			
			try{rs.getString(1);
				Assert.fail("Mandatory exception was not detected (result set is not positioned)");
			} catch (SQLException exc) {			
			}
			Assert.assertTrue(rs.next());

			Assert.assertNull(rs.getString("NULL"));
			Assert.assertTrue(rs.wasNull());
			
			Assert.assertEquals(rs.getString("NAME"),"100");
			Assert.assertFalse(rs.wasNull());
			
			Assert.assertTrue(rs.getBoolean("BOOL"));
			Assert.assertEquals(rs.getByte("NAME"),(byte)100);
			Assert.assertEquals(rs.getShort("NAME"),(short)100);
			Assert.assertEquals(rs.getInt("NAME"),100);
			Assert.assertEquals(rs.getLong("NAME"),100L);
			Assert.assertEquals(rs.getFloat("NAME"),100.0f,0.0001f);
			Assert.assertEquals(rs.getDouble("NAME"),100.0,0.0001);
			Assert.assertEquals(rs.getBigDecimal("NAME"),new BigDecimal("100"));
			Assert.assertArrayEquals(rs.getBytes("BYTE"),new byte[]{1,2,3});
			Assert.assertEquals(rs.getDate("NAME"),new Date(100));
			Assert.assertEquals(rs.getTime("NAME"),new Time(100));
			Assert.assertEquals(rs.getTimestamp("NAME"),new Timestamp(100));
			Assert.assertArrayEquals(content(rs.getAsciiStream("BYTE")),new byte[]{1,2,3});
			Assert.assertArrayEquals(content(rs.getUnicodeStream("BYTE")),new byte[]{1,2,3});
			Assert.assertArrayEquals(content(rs.getBinaryStream("BYTE")),new byte[]{1,2,3});
			Assert.assertEquals(rs.getObject("NAME"),"100");
			Assert.assertEquals(content(rs.getCharacterStream("NAME")),"100");
			Assert.assertArrayEquals(content(rs.getBlob("BYTE").getBinaryStream()),new byte[]{1,2,3});
			Assert.assertEquals(content(rs.getClob("NAME").getCharacterStream()),"100");
			Assert.assertTrue(rs.getArray("BYTE").getArray() instanceof Blob[]);
			Assert.assertEquals(rs.getURL("URL"),new URL("http://localhost:80"));
			Assert.assertEquals(content(rs.getNClob("NAME").getCharacterStream()),"100");
//			Assert.assertEquals(content(rs.getSQLXML("NAME").getCharacterStream()),"100");
			Assert.assertEquals(rs.getNString("NAME"),"100");
			Assert.assertEquals(content(rs.getNCharacterStream("NAME")),"100");
			
			try{rs.getString(0);
				Assert.fail("Mandatory exception was not detected (column number out of range)");
			} catch (SQLException exc) {
			}
			try{rs.getString(100);
				Assert.fail("Mandatory exception was not detected (column number out of range)");
			} catch (SQLException exc) {
			}
			
			Assert.assertFalse(rs.next());
		}
	}

	@Test
	public void inMemoryScrollableResultSetTest() throws SQLException, IOException {
		try(final ResultSet		rs = new InMemoryReadOnlyResultSet(new FSysResultSetMetaData(null,null,null,true
																				,"NAME:VARCHAR(100)"
																				,"URL:VARCHAR(100)"
																				,"BOOL:BOOLEAN"
																				,"BYTE:BLOB"
																				,"NULL:VARCHAR(100)")
																		, ResultSet.TYPE_SCROLL_SENSITIVE
																		, new ArrayContent(new Object[]{"100","http://localhost:80",true,new byte[]{1,2,3},null},
																						   new Object[]{"200","http://localhost:8080",false,new byte[]{4,5,6},null}
																		))) {
			
			Assert.assertEquals(rs.getMetaData().getColumnCount(),5);
			Assert.assertTrue(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());
			
			Assert.assertTrue(rs.next());
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertTrue(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());

			Assert.assertTrue(rs.next());
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertTrue(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());

			Assert.assertFalse(rs.next());
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertTrue(rs.isAfterLast());
			
			Assert.assertTrue(rs.previous());
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertTrue(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());
			
			Assert.assertTrue(rs.previous());
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertTrue(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());
			
			Assert.assertFalse(rs.previous());
			Assert.assertTrue(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());
			
			Assert.assertTrue(rs.absolute(1));
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertTrue(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());

			Assert.assertTrue(rs.absolute(2));
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertTrue(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());

			rs.beforeFirst();
			Assert.assertTrue(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());

			rs.afterLast();
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertTrue(rs.isAfterLast());

			rs.first();
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertTrue(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());

			rs.last();
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertTrue(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());

			rs.relative(-1);
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertTrue(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());

			rs.relative(-1);
			Assert.assertTrue(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());

			rs.setFetchDirection(ResultSet.FETCH_REVERSE);
			
			rs.relative(-1);
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertTrue(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());

			rs.relative(-1);
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertTrue(rs.isLast());
			Assert.assertFalse(rs.isAfterLast());

			rs.relative(-1);
			Assert.assertFalse(rs.isBeforeFirst());
			Assert.assertFalse(rs.isFirst());
			Assert.assertFalse(rs.isLast());
			Assert.assertTrue(rs.isAfterLast());
			
			rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		}
	}
	
	private Object content(final Reader is) throws IOException {
		try(final StringWriter	wr = new StringWriter()) {
			
			Utils.copyStream(is,wr);
			return wr.toString();
		} finally {
			is.close();
		}
	}

	private byte[] content(final InputStream is) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			Utils.copyStream(is,baos);
			return baos.toByteArray();
		} finally {
			is.close();
		}
	}
}

class PseudoResultSetMetaData extends AbstractResultSetMetaData {
	PseudoResultSetMetaData(final boolean readOnly, final String... columns) {
		super(InternalUtils.prepareMetadata(columns), readOnly);
	}

	@Override public String getSchemaName(int column) throws SQLException {return "SCHEMA";}
	@Override public String getTableName(int column) throws SQLException {return "TABLE";}
	@Override public String getCatalogName(int column) throws SQLException {return "CATALOG";}
}