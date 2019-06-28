package chav1961.purelib.sql;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.sql.FilteredReadOnlyResultSet.FilterTree;
import chav1961.purelib.sql.FilteredReadOnlyResultSet.Lexema;

public class ResultSetTests {
	@FunctionalInterface
	interface ExceptionTest {
		void process() throws Throwable;
	}
	
	@Test
	public void resultSetMetaDataTest() throws SQLException, SyntaxException {
		final RsMetaDataElement[]	fields = SQLUtils.prepareMetadata("CHAR:VARCHAR(100)","NUMBER:NUMERIC(15,2)","DATE:DATE");
		final ResultSetMetaData		rsmd = new AbstractResultSetMetaData(fields,true) {
											@Override public String getTableName(int column) throws SQLException {return "table";}
											@Override public String getSchemaName(int column) throws SQLException {return "schema";}
											@Override public String getCatalogName(int column) throws SQLException {return "catalog";}
										};
										
		Assert.assertEquals(rsmd.getColumnCount(),3);
		
		Assert.assertEquals(rsmd.getColumnName(1),"CHAR");
		Assert.assertEquals(rsmd.getColumnLabel(1),"CHAR");
		Assert.assertEquals(rsmd.getColumnType(1),Types.VARCHAR);
		Assert.assertEquals(rsmd.getColumnTypeName(1),"VARCHAR");
		Assert.assertEquals(rsmd.getColumnClassName(1),String.class.getName());
		Assert.assertEquals(rsmd.getColumnDisplaySize(1),100);

		Assert.assertEquals(rsmd.getColumnName(2),"NUMBER");
		Assert.assertEquals(rsmd.getColumnLabel(2),"NUMBER");
		Assert.assertEquals(rsmd.getColumnType(2),Types.NUMERIC);
		Assert.assertEquals(rsmd.getColumnTypeName(2),"NUMERIC");
		Assert.assertEquals(rsmd.getColumnClassName(2),BigDecimal.class.getName());
		Assert.assertEquals(rsmd.getColumnDisplaySize(2),15);

		Assert.assertEquals(rsmd.getColumnName(3),"DATE");
		Assert.assertEquals(rsmd.getColumnLabel(3),"DATE");
		Assert.assertEquals(rsmd.getColumnType(3),Types.DATE);
		Assert.assertEquals(rsmd.getColumnTypeName(3),"DATE");
		Assert.assertEquals(rsmd.getColumnClassName(3),Date.class.getName());
		Assert.assertEquals(rsmd.getColumnDisplaySize(3),0);

		detectException(()->{rsmd.getColumnName(0);},IllegalArgumentException.class,"column name out of range");
		detectException(()->{rsmd.getColumnName(100);},IllegalArgumentException.class,"column name out of range");
		detectException(()->{rsmd.getColumnLabel(0);},IllegalArgumentException.class,"column name out of range");
		detectException(()->{rsmd.getColumnLabel(100);},IllegalArgumentException.class,"column name out of range");
		detectException(()->{rsmd.getColumnType(0);},IllegalArgumentException.class,"column name out of range");
		detectException(()->{rsmd.getColumnType(100);},IllegalArgumentException.class,"column name out of range");
		detectException(()->{rsmd.getColumnTypeName(0);},IllegalArgumentException.class,"column name out of range");
		detectException(()->{rsmd.getColumnTypeName(100);},IllegalArgumentException.class,"column name out of range");
		detectException(()->{rsmd.getColumnClassName(0);},IllegalArgumentException.class,"column name out of range");
		detectException(()->{rsmd.getColumnClassName(100);},IllegalArgumentException.class,"column name out of range");
		detectException(()->{rsmd.getColumnDisplaySize(0);},IllegalArgumentException.class,"column name out of range");
		detectException(()->{rsmd.getColumnDisplaySize(100);},IllegalArgumentException.class,"column name out of range");
		
		try{new AbstractResultSetMetaData(null,true) {
				@Override public String getTableName(int column) throws SQLException {return "table";}
				@Override public String getSchemaName(int column) throws SQLException {return "schema";}
				@Override public String getCatalogName(int column) throws SQLException {return "catalog";}
			};
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Test
	public void arrayContentTest() throws SQLException {
		try(final AbstractContent		content = new ArrayContent(new Object[]{"10",20,true},new Object[]{"100",200,false})) {
			Assert.assertEquals(content.getRowCount(),2);
			Assert.assertEquals(content.getCurrentRow(),0);
			Assert.assertArrayEquals(content.getRow(2),new Object[]{"100",200,false});
			content.setCurrentRow(2);
			Assert.assertEquals(content.getCurrentRow(),2);
	
			detectException(()->{new ArrayContent();},IllegalArgumentException.class,"null or empty array");
			detectException(()->{content.setCurrentRow(-1);},IllegalArgumentException.class,"current row outside the range");
			detectException(()->{content.setCurrentRow(100);},IllegalArgumentException.class,"current row outside the range");
		}
	}

	@Test
	public void streamContentTest() throws SQLException {
		final Object[]	buffer = new Object[3], values = new Object[]{"100",20,true};
		final int[]		counter = new int[]{1};
		
		try(final AbstractContent		content = new StreamContent(buffer,(toFill)->{
				System.arraycopy(values,0,toFill,0,toFill.length);
				return counter[0]-- > 0;
				},
				()->{})) {
			Assert.assertEquals(content.getRowCount(),1);
			Assert.assertEquals(content.getCurrentRow(),0);
			content.setCurrentRow(content.getCurrentRow()+1);
			Assert.assertArrayEquals(content.getRow(content.getCurrentRow()),new Object[]{"100",20,true});
			content.setCurrentRow(content.getCurrentRow()+1);
			Assert.assertEquals(content.getCurrentRow(),2);
			detectException(()->{content.setCurrentRow(content.getCurrentRow()+1);},IllegalArgumentException.class,"current row outside the range");
	
			detectException(()->{new StreamContent(null,(dummy)->{return false;},()->{});},IllegalArgumentException.class,"null or empty array");
			detectException(()->{content.setCurrentRow(-1);},IllegalArgumentException.class,"current row outside the range");
			detectException(()->{content.setCurrentRow(100);},IllegalArgumentException.class,"current row outside the range");
		}
	}
	
	@Test
	public void nullResultSetTest() throws SQLException, SyntaxException {
		final RsMetaDataElement[]	fields = SQLUtils.prepareMetadata("CHAR:VARCHAR(100)","NUMBER:NUMERIC(15,2)","DATE:DATE");
		final ResultSetMetaData		rsmd = new AbstractResultSetMetaData(fields,true) {
										@Override public String getTableName(int column) throws SQLException {return "table";}
										@Override public String getSchemaName(int column) throws SQLException {return "schema";}
										@Override public String getCatalogName(int column) throws SQLException {return "catalog";}
									};
		try(final NullReadOnlyResultSet	rs = new NullReadOnlyResultSet(rsmd,ResultSet.TYPE_FORWARD_ONLY)) {
			Assert.assertFalse(rs.next());
			Assert.assertFalse(rs.next());
			Assert.assertEquals(rs.getMetaData().getColumnCount(),3);
			
			detectException(()->{rs.previous();},SQLException.class,"attempt to move on the forward-only cursor");
			detectException(()->{rs.getString(1);},SQLException.class,"attempt to read from empty cursor");
		}
	}

	@Test
	public void inMemoryResultSetMovingTest() throws SQLException, SyntaxException {
		final RsMetaDataElement[]		fields = SQLUtils.prepareMetadata("CHAR:VARCHAR(100)","NUMBER:NUMERIC(15,2)","DATE:DATE");
		final ResultSetMetaData			rsmd = new AbstractResultSetMetaData(fields,true) {
											@Override public String getTableName(int column) throws SQLException {return "table";}
											@Override public String getSchemaName(int column) throws SQLException {return "schema";}
											@Override public String getCatalogName(int column) throws SQLException {return "catalog";}
										};
		final ArrayContent				content = new ArrayContent(new Object[]{"100",BigDecimal.ONE,new Date(0)},new Object[]{"200",BigDecimal.TEN,new Date(1000)});
		try(final InMemoryReadOnlyResultSet	rs = new InMemoryReadOnlyResultSet(rsmd,ResultSet.TYPE_FORWARD_ONLY,content)) {
			
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
	
			detectException(()->{rs.previous();},SQLException.class,"attempt to move on the forward-only cursor");
			detectException(()->{rs.relative(1);},SQLException.class,"attempt to move on the forward-only cursor");
			detectException(()->{rs.absolute(1);},SQLException.class,"attempt to move on the forward-only cursor");
			detectException(()->{rs.last();},SQLException.class,"attempt to move on the forward-only cursor");
			detectException(()->{rs.first();},SQLException.class,"attempt to move on the forward-only cursor");
			detectException(()->{rs.afterLast();},SQLException.class,"attempt to move on the forward-only cursor");
			detectException(()->{rs.beforeFirst();},SQLException.class,"attempt to move on the forward-only cursor");
		}
		
		try(final InMemoryReadOnlyResultSet	rsScrolled = new InMemoryReadOnlyResultSet(rsmd,ResultSet.TYPE_SCROLL_INSENSITIVE,content)) {
			Assert.assertTrue(rsScrolled.isBeforeFirst());
			Assert.assertFalse(rsScrolled.isFirst());
			Assert.assertFalse(rsScrolled.isLast());
			Assert.assertFalse(rsScrolled.isAfterLast());
	
			Assert.assertTrue(rsScrolled.next());
	
			Assert.assertFalse(rsScrolled.isBeforeFirst());
			Assert.assertTrue(rsScrolled.isFirst());
			Assert.assertFalse(rsScrolled.isLast());
			Assert.assertFalse(rsScrolled.isAfterLast());
	
			Assert.assertTrue(rsScrolled.next());
	
			Assert.assertFalse(rsScrolled.isBeforeFirst());
			Assert.assertFalse(rsScrolled.isFirst());
			Assert.assertTrue(rsScrolled.isLast());
			Assert.assertFalse(rsScrolled.isAfterLast());
	
			Assert.assertFalse(rsScrolled.next());
	
			Assert.assertFalse(rsScrolled.isBeforeFirst());
			Assert.assertFalse(rsScrolled.isFirst());
			Assert.assertFalse(rsScrolled.isLast());
			Assert.assertTrue(rsScrolled.isAfterLast());
			
			Assert.assertTrue(rsScrolled.previous());
	
			Assert.assertFalse(rsScrolled.isBeforeFirst());
			Assert.assertFalse(rsScrolled.isFirst());
			Assert.assertTrue(rsScrolled.isLast());
			Assert.assertFalse(rsScrolled.isAfterLast());
	
			Assert.assertTrue(rsScrolled.previous());
			
			Assert.assertFalse(rsScrolled.isBeforeFirst());
			Assert.assertTrue(rsScrolled.isFirst());
			Assert.assertFalse(rsScrolled.isLast());
			Assert.assertFalse(rsScrolled.isAfterLast());
			
			Assert.assertFalse(rsScrolled.previous());
	
			Assert.assertTrue(rsScrolled.isBeforeFirst());
			Assert.assertFalse(rsScrolled.isFirst());
			Assert.assertFalse(rsScrolled.isLast());
			Assert.assertFalse(rsScrolled.isAfterLast());
	
			Assert.assertTrue(rsScrolled.absolute(2));
	
			Assert.assertFalse(rsScrolled.isBeforeFirst());
			Assert.assertFalse(rsScrolled.isFirst());
			Assert.assertTrue(rsScrolled.isLast());
			Assert.assertFalse(rsScrolled.isAfterLast());
	
			Assert.assertTrue(rsScrolled.relative(-1));
	
			Assert.assertFalse(rsScrolled.isBeforeFirst());
			Assert.assertTrue(rsScrolled.isFirst());
			Assert.assertFalse(rsScrolled.isLast());
			Assert.assertFalse(rsScrolled.isAfterLast());
	
			Assert.assertTrue(rsScrolled.last());
	
			Assert.assertFalse(rsScrolled.isBeforeFirst());
			Assert.assertFalse(rsScrolled.isFirst());
			Assert.assertTrue(rsScrolled.isLast());
			Assert.assertFalse(rsScrolled.isAfterLast());
	
			Assert.assertTrue(rsScrolled.first());
	
			Assert.assertFalse(rsScrolled.isBeforeFirst());
			Assert.assertTrue(rsScrolled.isFirst());
			Assert.assertFalse(rsScrolled.isLast());
			Assert.assertFalse(rsScrolled.isAfterLast());
	
			rsScrolled.afterLast();
	
			Assert.assertFalse(rsScrolled.isBeforeFirst());
			Assert.assertFalse(rsScrolled.isFirst());
			Assert.assertFalse(rsScrolled.isLast());
			Assert.assertTrue(rsScrolled.isAfterLast());
	
			rsScrolled.beforeFirst();
	
			Assert.assertTrue(rsScrolled.isBeforeFirst());
			Assert.assertFalse(rsScrolled.isFirst());
			Assert.assertFalse(rsScrolled.isLast());
			Assert.assertFalse(rsScrolled.isAfterLast());
		}
	}

	@Test
	public void inMemoryResultSetGettingTest() throws SQLException, SyntaxException {
		final RsMetaDataElement[]		fields = SQLUtils.prepareMetadata("CHAR:VARCHAR(100)","NUMBER:NUMERIC(15,2)","DATE:DATE");
		final ResultSetMetaData			rsmd = new AbstractResultSetMetaData(fields,true) {
											@Override public String getTableName(int column) throws SQLException {return "table";}
											@Override public String getSchemaName(int column) throws SQLException {return "schema";}
											@Override public String getCatalogName(int column) throws SQLException {return "catalog";}
										};
		final ArrayContent				content = new ArrayContent(new Object[]{"100",BigDecimal.ONE,new Date(0)},new Object[]{"200",BigDecimal.TEN,new Date(1000)});
		try(final InMemoryReadOnlyResultSet	rs = new InMemoryReadOnlyResultSet(rsmd,ResultSet.TYPE_FORWARD_ONLY,content)) {
			detectException(()->{rs.getString(1);},SQLException.class,"attempt to get data on non-positioned cursor");
	
			Assert.assertTrue(rs.next());
			Assert.assertEquals(rs.getMetaData().getColumnCount(),3);
			Assert.assertEquals(rs.getString(1),"100");
			Assert.assertEquals(rs.getString("CHAR"),"100");
			Assert.assertEquals(rs.getString(2),"1");
			Assert.assertEquals(rs.getString("NUMBER"),"1");
			
			detectException(()->{rs.getString(0);},SQLException.class,"column index out of range");
			detectException(()->{rs.getString(100);},SQLException.class,"column index out of range");
		}
	}

	@Test
	public void filteredResultSetInnerClassesTest() throws SQLException {
		final Object[]	content = new Object[]{100L,200.0,new Date(0),"test"};
		
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConstFilterTree(100L),Number.class,Long.valueOf(100),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConstFilterTree(100.0),Number.class,Double.valueOf(100.0),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConstFilterTree("test"),String.class,"test",content);
	
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.FieldFilterTree(0,Long.class),Long.class,Long.valueOf(100),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.FieldFilterTree(1,Double.class),Double.class,Double.valueOf(200),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.FieldFilterTree(2,Date.class),Date.class,new Date(0),content);

		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConvertFilterTree(new FilteredReadOnlyResultSet.ConstFilterTree(100L),Number.class),Number.class,new Long(100),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConvertFilterTree(new FilteredReadOnlyResultSet.ConstFilterTree(100L),Date.class),Date.class,new Date(100),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConvertFilterTree(new FilteredReadOnlyResultSet.ConstFilterTree(100L),String.class),String.class,"100",content);
		
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConvertFilterTree(new FilteredReadOnlyResultSet.ConstFilterTree(100.0),Number.class),Number.class,new Double(100),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConvertFilterTree(new FilteredReadOnlyResultSet.ConstFilterTree(100.0),Date.class),Date.class,new Date(100),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConvertFilterTree(new FilteredReadOnlyResultSet.ConstFilterTree(100.0),String.class),String.class,"100.0",content);

		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConvertFilterTree(new FilteredReadOnlyResultSet.ConstFilterTree("100"),Number.class),Number.class,new Double(100),content);
//		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConvertFilterTree(new FilteredReadOnlyResultSet.ConstFilterTree("100"),Date.class),Date.class,new Date(100),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ConvertFilterTree(new FilteredReadOnlyResultSet.ConstFilterTree("100"),String.class),String.class,"100",content);
		
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.NegFilterTree(new FilteredReadOnlyResultSet.ConstFilterTree(100L)),Number.class,new Long(-100),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.NegFilterTree(new FilteredReadOnlyResultSet.ConstFilterTree(100.0)),Number.class,new Double(-100),content);

		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ArithmeticFilterTree(Arrays.asList(Lexema.OPER_LOAD,Lexema.OPER_ADD),
						Arrays.asList(new FilteredReadOnlyResultSet.ConstFilterTree(100L)
									 ,new FilteredReadOnlyResultSet.ConstFilterTree(200L))
						),Number.class,new Double(300),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ArithmeticFilterTree(Arrays.asList(Lexema.OPER_LOAD,Lexema.OPER_SUB),
						Arrays.asList(new FilteredReadOnlyResultSet.ConstFilterTree(100L)
									 ,new FilteredReadOnlyResultSet.ConstFilterTree(200L))
						),Number.class,new Double(-100),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ArithmeticFilterTree(Arrays.asList(Lexema.OPER_LOAD,Lexema.OPER_MUL),
						Arrays.asList(new FilteredReadOnlyResultSet.ConstFilterTree(100L)
									 ,new FilteredReadOnlyResultSet.ConstFilterTree(200L))
						),Number.class,new Double(20000),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ArithmeticFilterTree(Arrays.asList(Lexema.OPER_LOAD,Lexema.OPER_DIV),
						Arrays.asList(new FilteredReadOnlyResultSet.ConstFilterTree(100L)
									 ,new FilteredReadOnlyResultSet.ConstFilterTree(200L))
						),Number.class,new Double(0.0),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ArithmeticFilterTree(Arrays.asList(Lexema.OPER_LOAD,Lexema.OPER_MOD),
						Arrays.asList(new FilteredReadOnlyResultSet.ConstFilterTree(100L)
									 ,new FilteredReadOnlyResultSet.ConstFilterTree(200L))
						),Number.class,new Double(100),content);

		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ArithmeticFilterTree(Arrays.asList(Lexema.OPER_LOAD,Lexema.OPER_ADD),
						Arrays.asList(new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
									 ,new FilteredReadOnlyResultSet.ConstFilterTree(200L))
						),Number.class,new Double(300),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ArithmeticFilterTree(Arrays.asList(Lexema.OPER_LOAD,Lexema.OPER_ADD),
						Arrays.asList(new FilteredReadOnlyResultSet.ConstFilterTree(100L)
									 ,new FilteredReadOnlyResultSet.ConstFilterTree(200.0))
						),Number.class,new Double(300),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ArithmeticFilterTree(Arrays.asList(Lexema.OPER_LOAD,Lexema.OPER_SUB),
						Arrays.asList(new FilteredReadOnlyResultSet.ConstFilterTree(100L)
									 ,new FilteredReadOnlyResultSet.ConstFilterTree(200.0))
						),Number.class,new Double(-100),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ArithmeticFilterTree(Arrays.asList(Lexema.OPER_LOAD,Lexema.OPER_MUL),
						Arrays.asList(new FilteredReadOnlyResultSet.ConstFilterTree(100L)
									 ,new FilteredReadOnlyResultSet.ConstFilterTree(200.0))
						),Number.class,new Double(20000),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ArithmeticFilterTree(Arrays.asList(Lexema.OPER_LOAD,Lexema.OPER_DIV),
						Arrays.asList(new FilteredReadOnlyResultSet.ConstFilterTree(100L)
									 ,new FilteredReadOnlyResultSet.ConstFilterTree(200.0))
						),Number.class,new Double(0.5),content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ArithmeticFilterTree(Arrays.asList(Lexema.OPER_LOAD,Lexema.OPER_MOD),
						Arrays.asList(new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
									 ,new FilteredReadOnlyResultSet.ConstFilterTree(200L))
						),Number.class,new Double(100),content);
		
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.CatFilterTree(
							new FilteredReadOnlyResultSet.ConstFilterTree("first")
							, new FilteredReadOnlyResultSet.ConstFilterTree("second")
						),String.class,"firstsecond",content);

		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_EQ,
							new FilteredReadOnlyResultSet.ConstFilterTree("first")
							, new FilteredReadOnlyResultSet.ConstFilterTree("first")
						),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_EQ,
							new FilteredReadOnlyResultSet.ConstFilterTree("first")
							, new FilteredReadOnlyResultSet.ConstFilterTree("second")
						),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_EQ,
						new FilteredReadOnlyResultSet.ConstFilterTree(100L)
						, new FilteredReadOnlyResultSet.ConstFilterTree(100L)
					),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_EQ,
						new FilteredReadOnlyResultSet.ConstFilterTree(100L)
						, new FilteredReadOnlyResultSet.ConstFilterTree(200L)
					),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_EQ,
						new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
						, new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
					),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_EQ,
						new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
						, new FilteredReadOnlyResultSet.ConstFilterTree(200.0)
					),Boolean.class,false,content);
		
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_NE,
						new FilteredReadOnlyResultSet.ConstFilterTree("first")
						, new FilteredReadOnlyResultSet.ConstFilterTree("second")
					),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_NE,
					new FilteredReadOnlyResultSet.ConstFilterTree("first")
					, new FilteredReadOnlyResultSet.ConstFilterTree("first")
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_NE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100L)
					, new FilteredReadOnlyResultSet.ConstFilterTree(100L)
				),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_NE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100L)
					, new FilteredReadOnlyResultSet.ConstFilterTree(200L)
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_NE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
					, new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
				),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_NE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
					, new FilteredReadOnlyResultSet.ConstFilterTree(200.0)
				),Boolean.class,true,content);
		
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LE,
						new FilteredReadOnlyResultSet.ConstFilterTree("first")
						, new FilteredReadOnlyResultSet.ConstFilterTree("first")
					),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LE,
						new FilteredReadOnlyResultSet.ConstFilterTree("first")
						, new FilteredReadOnlyResultSet.ConstFilterTree("any")
					),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100L)
					, new FilteredReadOnlyResultSet.ConstFilterTree(100L)
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100L)
					, new FilteredReadOnlyResultSet.ConstFilterTree(-100L)
				),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
					, new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
					, new FilteredReadOnlyResultSet.ConstFilterTree(-100.0)
				),Boolean.class,false,content);

		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LT,
						new FilteredReadOnlyResultSet.ConstFilterTree("first")
						, new FilteredReadOnlyResultSet.ConstFilterTree("first1")
					),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LT,
						new FilteredReadOnlyResultSet.ConstFilterTree("first")
						, new FilteredReadOnlyResultSet.ConstFilterTree("any")
					),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LT,
					new FilteredReadOnlyResultSet.ConstFilterTree(100L)
					, new FilteredReadOnlyResultSet.ConstFilterTree(101L)
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LT,
					new FilteredReadOnlyResultSet.ConstFilterTree(100L)
					, new FilteredReadOnlyResultSet.ConstFilterTree(-100L)
				),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LT,
					new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
					, new FilteredReadOnlyResultSet.ConstFilterTree(101.0)
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LT,
					new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
					, new FilteredReadOnlyResultSet.ConstFilterTree(-100.0)
				),Boolean.class,false,content);

		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GE,
						new FilteredReadOnlyResultSet.ConstFilterTree("first")
						, new FilteredReadOnlyResultSet.ConstFilterTree("first")
					),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GE,
						new FilteredReadOnlyResultSet.ConstFilterTree("first")
						, new FilteredReadOnlyResultSet.ConstFilterTree("second")
					),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100L)
					, new FilteredReadOnlyResultSet.ConstFilterTree(100L)
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100L)
					, new FilteredReadOnlyResultSet.ConstFilterTree(200L)
				),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
					, new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GE,
					new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
					, new FilteredReadOnlyResultSet.ConstFilterTree(200.0)
				),Boolean.class,false,content);

		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GT,
						new FilteredReadOnlyResultSet.ConstFilterTree("first")
						, new FilteredReadOnlyResultSet.ConstFilterTree("firs")
					),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GT,
						new FilteredReadOnlyResultSet.ConstFilterTree("first")
						, new FilteredReadOnlyResultSet.ConstFilterTree("second")
					),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GT,
					new FilteredReadOnlyResultSet.ConstFilterTree(100L)
					, new FilteredReadOnlyResultSet.ConstFilterTree(99L)
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GT,
					new FilteredReadOnlyResultSet.ConstFilterTree(100L)
					, new FilteredReadOnlyResultSet.ConstFilterTree(101L)
				),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GT,
					new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
					, new FilteredReadOnlyResultSet.ConstFilterTree(99.0)
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_GT,
					new FilteredReadOnlyResultSet.ConstFilterTree(100.0)
					, new FilteredReadOnlyResultSet.ConstFilterTree(101.0)
				),Boolean.class,false,content);

		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LIKE,
					new FilteredReadOnlyResultSet.ConstFilterTree("first")
					, new FilteredReadOnlyResultSet.ConstFilterTree("f?rs%")
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.ComparisonFilterTree(Lexema.OPER_LIKE,
					new FilteredReadOnlyResultSet.ConstFilterTree("first")
					, new FilteredReadOnlyResultSet.ConstFilterTree("s?con%")
				),Boolean.class,false,content);

		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.LogicalFilterTree(Lexema.OPER_NOT,
					new FilteredReadOnlyResultSet.ConstFilterTree(false)
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.LogicalFilterTree(Lexema.OPER_AND,
					new FilteredReadOnlyResultSet.ConstFilterTree(true),
					new FilteredReadOnlyResultSet.ConstFilterTree(true)
				),Boolean.class,true,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.LogicalFilterTree(Lexema.OPER_AND,
					new FilteredReadOnlyResultSet.ConstFilterTree(true),
					new FilteredReadOnlyResultSet.ConstFilterTree(false)
				),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.LogicalFilterTree(Lexema.OPER_OR,
					new FilteredReadOnlyResultSet.ConstFilterTree(false),
					new FilteredReadOnlyResultSet.ConstFilterTree(false)
				),Boolean.class,false,content);
		filteredResultSetInnerClassesTest(new FilteredReadOnlyResultSet.LogicalFilterTree(Lexema.OPER_OR,
					new FilteredReadOnlyResultSet.ConstFilterTree(false),
					new FilteredReadOnlyResultSet.ConstFilterTree(true)
				),Boolean.class,true,content);
	}
	
	private void filteredResultSetInnerClassesTest(final FilterTree tree, final Class<?> awaited, final Object result, final Object[] content) {
		Assert.assertEquals(tree.getResultType(),awaited);
		Assert.assertEquals(tree.get(content),result);
	}

	@Test
	public void filteredResultSetParserTest() throws SQLException, SyntaxException {
		final String	testString = " .,()+-*/=||<<=>>=<>\'const\'100 200.0 field AND BETWEEN IN LIKE NOT OR TO_CHAR TO_DATE TO_NUMBER \0";
		final Lexema	lex = new FilteredReadOnlyResultSet.Lexema(testString.toCharArray());

		for (int type : new int[]{Lexema.LEX_DOT,Lexema.LEX_DIV,Lexema.LEX_OPEN,Lexema.LEX_CLOSE
				,Lexema.LEX_OPER,Lexema.LEX_OPER,Lexema.LEX_OPER,Lexema.LEX_OPER,Lexema.LEX_OPER,Lexema.LEX_OPER
				,Lexema.LEX_OPER,Lexema.LEX_OPER,Lexema.LEX_OPER,Lexema.LEX_OPER,Lexema.LEX_OPER
				,Lexema.LEX_CHAR_CONST,Lexema.LEX_INT_CONST,Lexema.LEX_REAL_CONST,Lexema.LEX_FIELD
				,Lexema.LEX_OPER,Lexema.LEX_OPER,Lexema.LEX_OPER,Lexema.LEX_OPER,Lexema.LEX_OPER,Lexema.LEX_OPER
				,Lexema.LEX_FUNCTION,Lexema.LEX_FUNCTION,Lexema.LEX_FUNCTION}) {
			Assert.assertEquals(lex.lexType,type);
			lex.next();
		}
		
		for (String item : new String[]{" ?\0", "\'\0"}) {
			try{new FilteredReadOnlyResultSet.Lexema(item.toCharArray());
			} catch (SyntaxException exc) {
			}
		}
	}
	
//	@Test
	public void filteredResultSetTotalTest() throws SQLException, SyntaxException {
		final ArrayContent				ac = new ArrayContent(new Object[]{100L,200.0,new Date(0),"test1"},new Object[]{150L,250.0,new Date(100),"test2"});
		final AbstractResultSetMetaData	rsmd = new AbstractResultSetMetaData(SQLUtils.prepareMetadata("F1:INTEGER","F2:NUMERIC(10,2)","F3:DATE","F4:VARCHAR(100)"),true){
											@Override public String getSchemaName(int column) throws SQLException {return null;}
											@Override public String getTableName(int column) throws SQLException {return null;}
											@Override public String getCatalogName(int column) throws SQLException {return null;}
										}; 
		
		try(final ResultSet	rs = new InMemoryReadOnlyResultSet(rsmd,ResultSet.TYPE_SCROLL_SENSITIVE,ac)) {
			rs.beforeFirst();
			try(final ResultSet	rsTest = new FilteredReadOnlyResultSet(rs,"F1=100")) {
				Assert.assertEquals(calculateRsLength(rsTest),1);
			}
			try(final ResultSet	rsTest = new FilteredReadOnlyResultSet(rs,"F1=200")) {
				Assert.assertEquals(calculateRsLength(rsTest),0);
			}
			try(final ResultSet	rsTest = new FilteredReadOnlyResultSet(rs,"F1=100 OR F1=150")) {
				Assert.assertEquals(calculateRsLength(rsTest),0);
			}
		}
	}
	
	private int calculateRsLength(final ResultSet rs) throws SQLException {
		int	count = 0;
		
		while (rs.next()) {
			count++;
		}
		return count;
	}

	private void detectException(final ExceptionTest action, final Class<? extends Throwable> awaited, final String message) {
		try{action.process();
			Assert.fail("Mandatory exception was not detected ("+message+")");
		} catch (Throwable t) {
			if (!awaited.isAssignableFrom(t.getClass())) {
				Assert.fail("Unwaited exception ["+t+"] was detected");
			}
		}
	}
}
