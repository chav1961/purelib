package chav1961.purelib.sql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.Utils;

public class ArraysLobsAndXMLsTest {
	@Test
	public void lifeCycleArrayTest() throws SQLException, IOException {
		final Array			a = new InMemoryLitteArray(Types.VARCHAR,"123","456","789",null,"123");
		
		Assert.assertEquals(a.getBaseType(),Types.VARCHAR);
		Assert.assertEquals(a.getBaseTypeName(),"VARCHAR");
		Assert.assertArrayEquals((String[])a.getArray(),new String[]{"123","456","789",null,"123"});
		
		final Set<String>	collection = new HashSet<>(), template = new HashSet<>();
		
		try(final ResultSet	rs = a.getResultSet()) {
			int	count = 0, notNull = 0;
			while (rs.next()) {
				final String	value = rs.getString(2);
				
				if (!rs.wasNull()) {
					collection.add(value);
					notNull++;
				}
				count++;
			}
			Assert.assertEquals(count,5);
			Assert.assertEquals(notNull,4);
		}
		template.add("123");
		template.add("456");
		template.add("789");
		Assert.assertEquals(collection,template);

		try{new InMemoryLitteArray(666,"123","456","789",null,"123");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new InMemoryLitteArray(Types.VARCHAR,(Object[])null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{a.getArray(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{a.getArray(0,10,SQLUtils.DEFAULT_CONVERTOR);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{a.getArray(100,10,SQLUtils.DEFAULT_CONVERTOR);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{a.getArray(1,10,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{a.getResultSet(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{a.getResultSet(0,10,SQLUtils.DEFAULT_CONVERTOR);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{a.getResultSet(100,10,SQLUtils.DEFAULT_CONVERTOR);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (ArrayIndexOutOfBoundsException exc) {
		}
		try{a.getResultSet(1,10,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Test
	public void lifeCycleBlobTest() throws SQLException, IOException {
		final Blob		b = new InMemoryLittleBlob();
		
		Assert.assertEquals(b.length(),0);
		
		b.setBytes(1,new byte[]{1,2,3});
		Assert.assertEquals(b.length(),3);
		Assert.assertArrayEquals(b.getBytes(1,1),new byte[]{1});
	 	Assert.assertArrayEquals(b.getBytes(1,3),new byte[]{1,2,3});
		Assert.assertArrayEquals(b.getBytes(1,5),new byte[]{1,2,3});
		
		b.truncate(1);
		Assert.assertEquals(b.length(),1);
		Assert.assertArrayEquals(b.getBytes(1,1),new byte[]{1});
		Assert.assertArrayEquals(b.getBytes(1,3),new byte[]{1});
		Assert.assertArrayEquals(b.getBytes(1,5),new byte[]{1});

		b.setBytes(4,new byte[]{1,2,3,4,5});
		Assert.assertEquals(b.length(),8);
		Assert.assertArrayEquals(b.getBytes(1,8),new byte[]{1,0,0,1,2,3,4,5});
		Assert.assertEquals(b.position(new byte[]{1,2},1),4);
		Assert.assertEquals(b.position(new InMemoryLittleBlob(new byte[]{1,2}),2),4);
		Assert.assertEquals(b.position(new byte[]{1,2},5),0);
		
		int	count = 0;
		try(final InputStream	is = b.getBinaryStream()) {
			while (is.read() >= 0) {
				count++;
			}
		}
		Assert.assertEquals(count, 8);
		
		try(final OutputStream	os = b.setBinaryStream(5)) {
			os.write(new byte[]{10,20,30,40,50,60,70,80});
			os.flush();
		}
		Assert.assertEquals(b.length(), 12);
		
		b.free();
		Assert.assertEquals(b.length(), 0);
		System.err.println(b);
	}

	@Test
	public void exceptionBlobTest() throws SQLException, IOException {
		final Blob		b = new InMemoryLittleBlob(new byte[]{1});
		
		try{new InMemoryLittleBlob(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try{b.getBytes(-100,10);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (SQLException exc) {
		}
		try{b.getBytes(100,10);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (SQLException exc) {
		}
		try{b.getBytes(0,-100);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{b.position((byte[])null,0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{b.position((Blob)null,0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{b.position(new byte[]{10},-100);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (SQLException exc) {
		}
		try{b.position(new byte[]{10},100);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (SQLException exc) {
		}

		try{b.setBytes(-100,new byte[]{1});
			Assert.fail("Mandatory exception was not detected (1-st argument is negative)");
		} catch (SQLException exc) {
		}
		try{b.setBytes(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{b.setBytes(1,null,0,0);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{b.setBytes(1,new byte[]{1},-100,10);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{b.setBytes(1,new byte[]{1},0,100);
			Assert.fail("Mandatory exception was not detected (3-rd and 4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{b.setBinaryStream(-100);
			Assert.fail("Mandatory exception was not detected (1-st th argument out of range)");
		} catch (SQLException exc) {
		}
		try{b.truncate(-100);
			Assert.fail("Mandatory exception was not detected (1-st th argument out of range)");
		} catch (SQLException exc) {
		}
		try{b.truncate(100);
			Assert.fail("Mandatory exception was not detected (1-st th argument out of range)");
		} catch (SQLException exc) {
		}
	}

	@Test
	public void lifeCycleClobTest() throws SQLException, IOException {
		final NClob		b = new InMemoryLittleNClob();
		
		Assert.assertEquals(b.length(),0);
		
		b.setString(1,"123");
		Assert.assertEquals(b.length(),3);
		Assert.assertEquals(b.getSubString(1,1),"1");
	 	Assert.assertEquals(b.getSubString(1,3),"123");
		Assert.assertEquals(b.getSubString(1,5),"123");
		
		b.truncate(1);
		Assert.assertEquals(b.length(),1);
		Assert.assertEquals(b.getSubString(1,1),"1");
		Assert.assertEquals(b.getSubString(1,3),"1");
		Assert.assertEquals(b.getSubString(1,5),"1");

		b.setString(4,"12345");
		Assert.assertEquals(b.length(),8);
		Assert.assertEquals(b.getSubString(1,8),"1  12345");
		Assert.assertEquals(b.position("123",1),4);
		Assert.assertEquals(b.position(new InMemoryLittleNClob("123".toCharArray()),2),4);
		Assert.assertEquals(b.position("123",5),0);
		
		int	count = 0;
		try(final InputStream	is = b.getAsciiStream()) {
			while (is.read() >= 0) {
				count++;
			}
		}
		Assert.assertEquals(count, 8);

		count = 0;
		try(final Reader	is = b.getCharacterStream()) {
			while (is.read() >= 0) {
				count++;
			}
		}
		Assert.assertEquals(count, 8);
		
		try(final OutputStream	os = b.setAsciiStream(5)) {
			os.write(new byte[]{'1','2','3','4','5','6','7','8'});
			os.flush();
		}
		Assert.assertEquals(b.length(), 12);

		try(final Writer	os = b.setCharacterStream(5)) {
			os.write("12345678");
			os.flush();
		}
		Assert.assertEquals(b.length(), 12);
		
		b.free();
		Assert.assertEquals(b.length(), 0);
		
		System.err.println(b);
	}

	@Test
	public void exceptionClobTest() throws SQLException, IOException {
		final NClob		b = new InMemoryLittleNClob("0");
		
		try{new InMemoryLittleNClob((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new InMemoryLittleNClob((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try{b.getSubString(-100,10);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (SQLException exc) {
		}
		try{b.getSubString(100,10);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (SQLException exc) {
		}
		try{b.getSubString(0,-100);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{b.position((String)null,0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{b.position((Clob)null,0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{b.position("1",-100);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (SQLException exc) {
		}
		try{b.position("1",100);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (SQLException exc) {
		}

		try{b.setString(-100,"1");
			Assert.fail("Mandatory exception was not detected (1-st argument is negative)");
		} catch (SQLException exc) {
		}
		try{b.setString(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{b.setString(1,null,0,0);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{b.setString(1,"1",-100,10);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{b.setString(1,"1",0,100);
			Assert.fail("Mandatory exception was not detected (3-rd and 4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void lifeCycleXMLTest() throws SQLException, IOException, XMLStreamException {
		final SQLXML	b = new InMemoryLittleSQLXML();
		
		Assert.assertEquals(b.getString(),"");
		b.setString("123");
		Assert.assertEquals(b.getString(),"123");
		
		try(final InputStream	is = b.getBinaryStream();
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			 
			Utils.copyStream(is,baos);
			Assert.assertEquals(baos.toString(),"123");
		}

		try(final Reader	rdr = b.getCharacterStream();
			final Writer	wr = new StringWriter()) {
			 
			Utils.copyStream(rdr,wr);
			Assert.assertEquals(wr.toString(),"123");
		}
		
		try(final OutputStream	os = b.setBinaryStream()) {
			os.write("456".getBytes());
			os.flush();
		}
		Assert.assertEquals(b.getString(),"456");

		try(final Writer	wr = b.setCharacterStream()) {
			wr.write("789");
			wr.flush();
		}
		Assert.assertEquals(b.getString(),"789");
		
		final XMLOutputFactory 	outFactory = XMLOutputFactory.newInstance();
		final XMLStreamWriter 	writer = outFactory.createXMLStreamWriter(b.setResult(StreamResult.class));
			
		writer.writeStartDocument();
		writer.writeStartElement("document");
		writer.writeStartElement("body");
		writer.writeAttribute("name", "value");
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
			
		writer.flush();
		writer.close();

		final XMLInputFactory	inFactory = XMLInputFactory.newInstance(); 
		final XMLStreamReader 	reader = inFactory.createXMLStreamReader(b.getSource(StreamSource.class));
		
		int	count = 0;
		while (reader.hasNext()) {
			reader.next();
			count++;
		}
		Assert.assertTrue(count > 0);
		System.err.println(b);
	}

	@Test
	public void lifeCycleStructTest() throws SQLException, IOException {
		final InMemoryLittleStruct	s = new InMemoryLittleStruct("name");
		
		Assert.assertEquals(s.getSQLTypeName(),"name");
		Assert.assertEquals(s.getAttributes().length,0);

		s.addPairs(new InMemoryLittleStruct.AttributePair("attr1","value1"),new InMemoryLittleStruct.AttributePair("attr2","value2"),new InMemoryLittleStruct.AttributePair("attr1","value3"));
		Assert.assertEquals(s.getAttributes().length,2);
		Assert.assertEquals(s.getAttributes()[0],"value3");
		Assert.assertEquals(s.getAttributes()[1],"value2");
		
		s.removeAttributes("attr2");
		Assert.assertEquals(s.getAttributes().length,1);
		Assert.assertEquals(s.getAttributes()[0],"value3");
		
		try {new InMemoryLittleStruct(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {new InMemoryLittleStruct(null);			
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try {s.addPairs((InMemoryLittleStruct.AttributePair[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {s.addPairs((InMemoryLittleStruct.AttributePair)null);
			Assert.fail("Mandatory exception was not detected (null inside 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try {s.removeAttributes((String[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {s.removeAttributes((String)null);
			Assert.fail("Mandatory exception was not detected (null inside 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {s.removeAttributes("");
			Assert.fail("Mandatory exception was not detected (null inside 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
