package chav1961.purelib.sql;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.sql.interfaces.ORMProvider;
import chav1961.purelib.testing.DatabaseTestCategory;
import chav1961.purelib.testing.TestingUtils;

public class SimpleProviderTest {
	@Category(DatabaseTestCategory.class)
	@Test
	public void basicTest() throws SyntaxException, LocalizationException, ContentException, IOException, SQLException, DebuggingException {
		TestingUtils.prepareDatabase("drop table public.test");
		Assert.assertTrue(TestingUtils.prepareDatabase("create table public.test (f1 integer primary key, f2 varchar(100))"));
		
		try(final Connection	conn = TestingUtils.getTestConnection()) {
			final ContentMetadataInterface	clazzModel = ContentModelFactory.forAnnotatedClass(SimpleProviderRecord.class);
			final ContentMetadataInterface	tableModel = ContentModelFactory.forDBContentDescription(conn.getMetaData(),null,"public","test");
			final String[]					fields = new String[]{"f1","f2"}, primaryKeys = new String[]{"f1"};
			
			try(final SimpleProvider<SimpleProviderRecord>	provider = new SimpleProvider<SimpleProviderRecord>(
																	tableModel.getRoot(), 
																	clazzModel.getRoot(), 
																	SimpleProviderRecord.class, 
																	fields, 
																	primaryKeys){
																		@Override public SimpleProviderRecord newRecord() throws SQLException {return null;}
																		@Override public SimpleProviderRecord duplicateRecord(SimpleProviderRecord rec) throws SQLException {return null;}
				
						}) {
				try(final ORMProvider<SimpleProviderRecord>	associated = provider.associate(conn)) {
				}
				
				try(final ORMProvider<SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord>(null,clazzModel.getRoot(),SimpleProviderRecord.class,fields,primaryKeys){
											@Override public SimpleProviderRecord newRecord() throws SQLException {return null;}
											@Override public SimpleProviderRecord duplicateRecord(SimpleProviderRecord rec) throws SQLException {return null;}}) {
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				} 
				
				try(final ORMProvider<SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord>(tableModel.getRoot(),null,SimpleProviderRecord.class,fields,primaryKeys){
											@Override public SimpleProviderRecord newRecord() throws SQLException {return null;}
											@Override public SimpleProviderRecord duplicateRecord(SimpleProviderRecord rec) throws SQLException {return null;}}) {
					Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
				} catch (NullPointerException exc) {
				}
				
				try(final ORMProvider<SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),null,fields,primaryKeys){
											@Override public SimpleProviderRecord newRecord() throws SQLException {return null;}
											@Override public SimpleProviderRecord duplicateRecord(SimpleProviderRecord rec) throws SQLException {return null;}}) {
					Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
				} catch (NullPointerException exc) {
				}
				
				try(final ORMProvider<SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),SimpleProviderRecord.class,null,primaryKeys){
											@Override public SimpleProviderRecord newRecord() throws SQLException {return null;}
											@Override public SimpleProviderRecord duplicateRecord(SimpleProviderRecord rec) throws SQLException {return null;}}) {
					Assert.fail("Mandatory exception was not detected (null 4-st argument)");
				} catch (IllegalArgumentException exc) {
				}
				try(final ORMProvider<SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),SimpleProviderRecord.class,new String[0],primaryKeys){
											@Override public SimpleProviderRecord newRecord() throws SQLException {return null;}
											@Override public SimpleProviderRecord duplicateRecord(SimpleProviderRecord rec) throws SQLException {return null;}}) {
					Assert.fail("Mandatory exception was not detected (null 4-st argument)");
				} catch (IllegalArgumentException exc) {
				}
				try(final ORMProvider<SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),SimpleProviderRecord.class,new String[]{null},primaryKeys){
											@Override public SimpleProviderRecord newRecord() throws SQLException {return null;}
											@Override public SimpleProviderRecord duplicateRecord(SimpleProviderRecord rec) throws SQLException {return null;}}) {
					Assert.fail("Mandatory exception was not detected (null 4-st argument)");
				} catch (NullPointerException exc) {
				}
				try(final ORMProvider<SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord>(tableModel.getRoot(),clazzModel.getRoot(),SimpleProviderRecord.class,fields,null){
											@Override public SimpleProviderRecord newRecord() throws SQLException {return null;}
											@Override public SimpleProviderRecord duplicateRecord(SimpleProviderRecord rec) throws SQLException {return null;}}) {
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (IllegalArgumentException exc) {
				}
				try(final ORMProvider<SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord>(null,clazzModel.getRoot(),SimpleProviderRecord.class,fields,new String[0]){
											@Override public SimpleProviderRecord newRecord() throws SQLException {return null;}
											@Override public SimpleProviderRecord duplicateRecord(SimpleProviderRecord rec) throws SQLException {return null;}}) {
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try(final ORMProvider<SimpleProviderRecord>	associated = new SimpleProvider<SimpleProviderRecord>(tableModel.getRoot(),null,SimpleProviderRecord.class,fields,new String[]{null}){
											@Override public SimpleProviderRecord newRecord() throws SQLException {return null;}
											@Override public SimpleProviderRecord duplicateRecord(SimpleProviderRecord rec) throws SQLException {return null;}}) {
					Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
				} catch (NullPointerException exc) {
				}
				
				try{provider.associate(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				
			}
		} finally {
			TestingUtils.prepareDatabase("drop table public.test");
		}
	}

	@Category(DatabaseTestCategory.class)
	@Test
	public void readOnlyTest() throws SyntaxException, LocalizationException, ContentException, IOException, SQLException, DebuggingException {
		TestingUtils.prepareDatabase("drop table public.test");
		Assert.assertTrue(TestingUtils.prepareDatabase("create table public.test (f1 integer primary key, f2 varchar(100))"
				,"insert into public.test(f1,f2) values(10,'line 10')"
				,"insert into public.test(f1,f2) values(20,'line 20')"
				,"insert into public.test(f1,f2) values(30,'line 30')"
				));
		
		try(final Connection	conn = TestingUtils.getTestConnection()) {
			final ContentMetadataInterface	clazzModel = ContentModelFactory.forAnnotatedClass(SimpleProviderRecord.class);
			final ContentMetadataInterface	tableModel = ContentModelFactory.forDBContentDescription(conn.getMetaData(),null,"public","test");
			final String[]					fields = new String[]{"f1","f2"}, primaryKeys = new String[]{"f1"};
			final SimpleProviderRecord		rec = new SimpleProviderRecord();
			
			try(final SimpleProvider<SimpleProviderRecord>	provider = new SimpleProvider<SimpleProviderRecord>(
																	tableModel.getRoot(), 
																	clazzModel.getRoot(), 
																	SimpleProviderRecord.class, 
																	fields, 
																	primaryKeys){
																		@Override public SimpleProviderRecord newRecord() throws SQLException {return null;}
																		@Override public SimpleProviderRecord duplicateRecord(SimpleProviderRecord rec) throws SQLException {return null;}
				
						}) {
				try(final ORMProvider<SimpleProviderRecord>	associated = provider.associate(conn)) {
					Assert.assertEquals(3,associated.contentSize());
					Assert.assertEquals(1,associated.contentSize("f1=10"));
					
					try{associated.contentSize(null);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.contentSize("");
						Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.contentSize("URA!!!");
						Assert.fail("Mandatory exception was not detected (illegal 1-st argument syntax)");
					} catch (SQLException exc) {
					}
					
					final int[]	countAndSum = new int[] {0,0};
					
					associated.content(rec,(seq,offset,record)->{
						countAndSum[0]++;
						countAndSum[1] +=record.f1;
						return ContinueMode.CONTINUE;
					});
					Assert.assertArrayEquals(new int[]{3,60},countAndSum);
					
					Arrays.fill(countAndSum,0);
					associated.content(rec,(seq,offset,record)->{
						countAndSum[0]++;
						countAndSum[1] +=record.f1;
						return ContinueMode.CONTINUE;
					},1,2);
					Assert.assertArrayEquals(new int[]{2,50},countAndSum);

					Arrays.fill(countAndSum,0);
					associated.content(rec,(seq,offset,record)->{
						countAndSum[0]++;
						countAndSum[1] +=record.f1;
						return ContinueMode.CONTINUE;
					},"f1 in (10,20)");
					Assert.assertArrayEquals(new int[]{2,30},countAndSum);

					Arrays.fill(countAndSum,0);
					associated.content(rec,(seq,offset,record)->{
						countAndSum[0]++;
						countAndSum[1] +=record.f1;
						return ContinueMode.CONTINUE;
					},"f1 in (10,20)",0,1);
					Assert.assertArrayEquals(new int[]{1,10},countAndSum);

					Arrays.fill(countAndSum,0);
					associated.content(rec,(seq,offset,record)->{
						countAndSum[0]++;
						countAndSum[1] +=record.f1;
						return ContinueMode.CONTINUE;
					},"f1 in (10,20)","f1 desc");
					Assert.assertArrayEquals(new int[]{2,30},countAndSum);

					Arrays.fill(countAndSum,0);
					associated.content(rec,(seq,offset,record)->{
						countAndSum[0]++;
						countAndSum[1] +=record.f1;
						return ContinueMode.CONTINUE;
					},"f1 in (10,30)","f1 desc",0,1);
					Assert.assertArrayEquals(new int[]{1,30},countAndSum);
					
					try{associated.contentSize(null);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.contentSize("");
						Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.contentSize("unknown");
						Assert.fail("Mandatory exception was not detected (illegal syntax in 1-st argument)");
					} catch (SQLException exc) {
					}

					try{associated.content(null,(seq,offset,record)->{
							countAndSum[0]++;
							countAndSum[1] +=record.f1;
							return ContinueMode.CONTINUE;
						});
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					try{associated.content(rec,null);
						Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
					} catch (NullPointerException exc) {
					}
					
					try{associated.content(null,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},1,2);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					try{associated.content(rec,null,1,2);
						Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
					} catch (NullPointerException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},-1,2);
						Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},1,-1);
						Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
					} catch (IllegalArgumentException exc) {
					}
				
					try{associated.content(null,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"f1 in (10,20)");
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					try{associated.content(rec,null,"f1 in (10,20)");
						Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
					} catch (NullPointerException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},null);
						Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"");
						Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"unknown");
						Assert.fail("Mandatory exception was not detected (illegal syntax in 3-rd argument)");
					} catch (SQLException exc) {
					}

					try{associated.content(null,(seq,offset,record)->{
						return ContinueMode.CONTINUE;
						},"f1 in (10,20)",1,1);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					try{associated.content(rec,null,"f1 in (10,20)",1,1);
						Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
					} catch (NullPointerException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},null,1,1);
						Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"",1,1);
						Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"unknown",1,1);
						Assert.fail("Mandatory exception was not detected (illegal syntax in 3-rd argument)");
					} catch (SQLException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"1=1",-1,1);
						Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"1=1",1,-11);
						Assert.fail("Mandatory exception was not detected (5-th argument out of range)");
					} catch (IllegalArgumentException exc) {
					}

					try{associated.content(null,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"f1 in (10,20)","f1");
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					try{associated.content(rec,null,"f1 in (10,20)","f1");
						Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
					} catch (NullPointerException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},null,"f1");
						Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"","f1");
						Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"unknown","f1");
						Assert.fail("Mandatory exception was not detected (illegal syntax in 3-rd argument)");
					} catch (SQLException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"1=1",null);
						Assert.fail("Mandatory exception was not detected (null 4-th argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"1=1","");
						Assert.fail("Mandatory exception was not detected (empty 4-th argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"1=1","unknown");
						Assert.fail("Mandatory exception was not detected (illegal syntax in 4-th argument)");
					} catch (SQLException exc) {
					}

					try{associated.content(null,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"f1 in (10,20)","f1",1,1);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					try{associated.content(rec,null,"f1 in (10,20)","f1",1,1);
						Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
					} catch (NullPointerException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},null,"f1",1,1);
						Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"","f1",1,1);
						Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"unknown","f1",1,1);
						Assert.fail("Mandatory exception was not detected (illegal syntax in 3-rd argument)");
					} catch (SQLException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"1=1",null,1,1);
						Assert.fail("Mandatory exception was not detected (null 4-th argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"1=1","",1,1);
						Assert.fail("Mandatory exception was not detected (empty 4-th argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"1=1","unknown",1,1);
						Assert.fail("Mandatory exception was not detected (illegal syntax in 4-th argument)");
					} catch (SQLException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"1=1","f1",-1,1);
						Assert.fail("Mandatory exception was not detected (5-th argument out of range)");
					} catch (IllegalArgumentException exc) {
					}
					try{associated.content(rec,(seq,offset,record)->{
							return ContinueMode.CONTINUE;
						},"1=1","f1",1,-1);
						Assert.fail("Mandatory exception was not detected (6-th argument out of range)");
					} catch (IllegalArgumentException exc) {
					}
					
				}

				try{provider.contentSize();
					Assert.fail("Mandatory exception was not detected (call without association)");
				} catch (IllegalStateException exc) {
				}
				try{provider.contentSize("1=1");
					Assert.fail("Mandatory exception was not detected (call without association)");
				} catch (IllegalStateException exc) {
				}
				
				try{provider.content(rec,(seq,offset,record)->{
						return ContinueMode.CONTINUE;
					});
					Assert.fail("Mandatory exception was not detected (call without association)");
				} catch (IllegalStateException exc) {
				}
				try{provider.content(rec,(seq,offset,record)->{
						return ContinueMode.CONTINUE;
					},1,1);
					Assert.fail("Mandatory exception was not detected (call without association)");
				} catch (IllegalStateException exc) {
				}
				try{provider.content(rec,(seq,offset,record)->{
						return ContinueMode.CONTINUE;
					},"1=1");
					Assert.fail("Mandatory exception was not detected (call without association)");
				} catch (IllegalStateException exc) {
				}
				try{provider.content(rec,(seq,offset,record)->{
						return ContinueMode.CONTINUE;
					},"1=1",1,1);
					Assert.fail("Mandatory exception was not detected (call without association)");
				} catch (IllegalStateException exc) {
				}
			}
		} finally {
			TestingUtils.prepareDatabase("drop table public.test");
		}
	}

	@Category(DatabaseTestCategory.class)
	@Test
	public void updateTest() throws SyntaxException, LocalizationException, ContentException, IOException, SQLException, DebuggingException {
		TestingUtils.prepareDatabase("drop table public.test");
		Assert.assertTrue(TestingUtils.prepareDatabase("create table public.test (f1 integer primary key, f2 varchar(100))"
				,"insert into public.test(f1,f2) values(10,'line 10')"
				,"insert into public.test(f1,f2) values(20,'line 20')"
				,"insert into public.test(f1,f2) values(30,'line 30')"
				));
		
		try(final Connection	conn = TestingUtils.getTestConnection()) {
			final ContentMetadataInterface	clazzModel = ContentModelFactory.forAnnotatedClass(SimpleProviderRecord.class);
			final ContentMetadataInterface	tableModel = ContentModelFactory.forDBContentDescription(conn.getMetaData(),null,"public","test");
			final String[]					fields = new String[]{"f1","f2"}, primaryKeys = new String[]{"f1"};
			final SimpleProviderRecord		rec = new SimpleProviderRecord();
			final AtomicInteger				ai = new AtomicInteger(100); 
			
			try(final SimpleProvider<SimpleProviderRecord>	provider = new SimpleProvider<SimpleProviderRecord>(
																	tableModel.getRoot(), 
																	clazzModel.getRoot(), 
																	SimpleProviderRecord.class, 
																	fields, 
																	primaryKeys){
											@Override 
											public SimpleProviderRecord newRecord() throws SQLException {
												final SimpleProviderRecord 	newRec = new SimpleProviderRecord();
												
												newRec.f1 = ai.addAndGet(100); 
												
												return newRec;
											}
											
											@Override 
											public SimpleProviderRecord duplicateRecord(final SimpleProviderRecord rec) throws SQLException {
												try{final SimpleProviderRecord 	newRec = (SimpleProviderRecord) rec.clone();
													final int					newKey = ai.addAndGet(100);
												
													newRec.f1 = newKey;
													create(newRec);
													return newRec;
												} catch (CloneNotSupportedException e) {
													throw new SQLException(e.getLocalizedMessage(),e);
												}
											}
							
				}) {
				try(final ORMProvider<SimpleProviderRecord>	associated = provider.associate(conn)) {
					Assert.assertEquals(3,associated.contentSize());
					final SimpleProviderRecord	newRec = associated.newRecord();
					
					newRec.f2 = "test string";
					associated.create(newRec);
					Assert.assertEquals(4,associated.contentSize());
					
					rec.f1 = newRec.f1;
					associated.read(rec);
					Assert.assertEquals("test string",rec.f2);
					
					rec.f2 = "updated string";
					associated.update(rec);
					Assert.assertEquals(4,associated.contentSize());
					
					associated.read(newRec);
					Assert.assertEquals("updated string",newRec.f2);
					
					associated.delete(newRec);
					Assert.assertEquals(3,associated.contentSize());
				}
			}
		} finally {
			TestingUtils.prepareDatabase("drop table public.test");
		}
	}
}


