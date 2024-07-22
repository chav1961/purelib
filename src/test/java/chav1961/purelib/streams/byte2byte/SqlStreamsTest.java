package chav1961.purelib.streams.byte2byte;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.streams.byte2byte.SQLDataOutputStream.Convertor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;

@Tag("OrdinalTestCategory")
public class SqlStreamsTest {
	
	@Test
	public void primitiveTest() throws IOException {
		try(final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
			try(final SQLDataOutputStream	sqlo = new SQLDataOutputStream(baos)) {
				
				sqlo.writeBoolean(true);
				sqlo.writeByte(123);
				sqlo.writeChar('z');
				sqlo.writeChars("test string");
				sqlo.writeUTF("test string");
				sqlo.writeDouble(123.456);
				sqlo.writeFloat(123.456f);
				sqlo.writeInt(123);
				sqlo.writeLong(123L);
				sqlo.writeNull(Types.ARRAY);
				sqlo.writeShort(123);
				sqlo.writeLine();
				sqlo.flush();
			}
			
			try(final InputStream			bais = new ByteArrayInputStream(baos.toByteArray());
				final SQLDataInputStream	sqli = new SQLDataInputStream(bais)) {
				final Object[]				place = new Object[1];
				
				Assert.assertTrue(sqli.readBoolean());
				Assert.assertEquals(123, sqli.readByte());
				Assert.assertEquals('z', sqli.readChar());
				Assert.assertEquals("test string", sqli.readLine());
				Assert.assertEquals("test string", sqli.readUTF());
				Assert.assertEquals(123.456, sqli.readDouble(), 0.0001);
				Assert.assertEquals(123.456f, sqli.readFloat(), 0.000f);
				Assert.assertEquals(123, sqli.readInt());
				Assert.assertEquals(123L, sqli.readLong());
				Assert.assertEquals(Types.ARRAY, sqli.read(place));
				Assert.assertEquals(123, sqli.readShort());
				Assert.assertEquals(Short.MAX_VALUE, sqli.read(place));
			}
		}
	}

	@Test
	public void referencedTest() throws IOException, ContentException, NullPointerException {
		identityTest(Types.NUMERIC, BigDecimal.TEN, BigDecimal.TEN);
		identityTest(Types.DECIMAL, BigDecimal.TEN, BigDecimal.TEN);
		
		final String	test = "test string";
		
		identityTest(Types.CHAR, test, test);
		identityTest(Types.VARCHAR, test, test);
		identityTest(Types.LONGVARCHAR, test, test);
		identityTest(Types.NCHAR, test, test);
		identityTest(Types.NVARCHAR, test, test);
		identityTest(Types.LONGNVARCHAR, test, test);
		identityTest(Types.JAVA_OBJECT, test, test);
		
		final long		time = System.currentTimeMillis();
		
		identityTest(Types.DATE, new Date(time), new Date(time));
		identityTest(Types.TIME, new Time(time), new Time(time));
		identityTest(Types.TIMESTAMP, new Timestamp(time), new Timestamp(time));
		identityTest(Types.TIME_WITH_TIMEZONE, new Time(time), new Time(time));
		identityTest(Types.TIMESTAMP_WITH_TIMEZONE, new Timestamp(time), new Timestamp(time));
		
		final byte[]	bin = test.getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING);
		
		identityTest(Types.BINARY, bin, bin);
		identityTest(Types.VARBINARY, bin, bin);
		identityTest(Types.LONGVARBINARY, bin, bin);
		identityTest(Types.BLOB, SQLUtils.convert(Blob.class, bin), SQLUtils.convert(Blob.class, bin));
		identityTest(Types.CLOB, SQLUtils.convert(Clob.class, test), SQLUtils.convert(Clob.class, test));
		identityTest(Types.NCLOB, SQLUtils.convert(NClob.class, test), SQLUtils.convert(NClob.class, test));
		
		identityTest(Types.BOOLEAN, true, true);
		identityTest(Types.BOOLEAN, null, null);
		identityTest(Types.TINYINT, (byte)123, (byte)123);
		identityTest(Types.TINYINT, null, null);
		identityTest(Types.SMALLINT, (short)123, (short)123);
		identityTest(Types.SMALLINT, null, null);
		identityTest(Types.INTEGER, 123, 123);
		identityTest(Types.INTEGER, null, null);
		identityTest(Types.BIGINT, 123L, 123L);
		identityTest(Types.BIGINT, null, null);
		identityTest(Types.FLOAT, 123.456f, 123.456f);
		identityTest(Types.FLOAT, null, null);
		identityTest(Types.DOUBLE, 123.456, 123.456);
		identityTest(Types.DOUBLE, null, null);
	}

	@Test
	public void iterableTest() throws IOException, ContentException, NullPointerException {
		final List<AwaitedValues>		av = new ArrayList<>();
		
		try(final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
			try(final SQLDataOutputStream	sqlo = new SQLDataOutputStream(baos)) {
				
				av.add(new AwaitedValues(Types.BOOLEAN, true));
				sqlo.writeBoolean(true);
				
				av.add(new AwaitedValues(Types.TINYINT, (byte)123));
				sqlo.writeByte(123);
				
				av.add(new AwaitedValues(Types.CHAR, new String("z")));
				sqlo.writeChar('z');
				
				av.add(new AwaitedValues(Types.CHAR, "test string"));
				sqlo.writeChars("test string");
				
				av.add(new AwaitedValues(Types.DOUBLE, 123.456));
				sqlo.writeDouble(123.456);
				
				av.add(new AwaitedValues(Types.FLOAT, 123.456f));
				sqlo.writeFloat(123.456f);
				
				av.add(new AwaitedValues(Types.INTEGER, 123));
				sqlo.writeInt(123);
				
				av.add(new AwaitedValues(Types.BIGINT, 123L));
				sqlo.writeLong(123L);
				
				av.add(new AwaitedValues(Types.ARRAY, null));
				sqlo.writeNull(Types.ARRAY);
				
				av.add(new AwaitedValues(Types.SMALLINT, (short)123));
				sqlo.writeShort(123);

				av.add(new AwaitedValues(Types.NUMERIC, BigDecimal.TEN));
				sqlo.write(Types.NUMERIC, BigDecimal.TEN);

				av.add(new AwaitedValues(Types.JAVA_OBJECT, "test string"));
				sqlo.write(Types.JAVA_OBJECT, "test string");
				
				av.add(new AwaitedValues(SQLDataInputStream.EOL, null));
				sqlo.writeLine();
				sqlo.flush();
			}
			
			try(final InputStream			bais = new ByteArrayInputStream(baos.toByteArray());
				final SQLDataInputStream	sqli = new SQLDataInputStream(bais)) {
				final Object[]				place = new Object[1];
				int		index = 0;

				for (int item : sqli.content(place)) {
					Assert.assertEquals(av.get(index).type, item);
					Assert.assertEquals(av.get(index).value, place[0]);
					index++;
				}
				Assert.assertEquals(av.size(), index);
			}
		}
	}	
	
	private <T> void identityTest(final int sqlType, final T source, final T result) throws IOException {
		try(final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
			try(final SQLDataOutputStream	sqlo = new SQLDataOutputStream(baos)) {
				
				sqlo.write(sqlType,source);
				sqlo.writeLine();
				sqlo.flush();
			}
			
			try(final InputStream			bais = new ByteArrayInputStream(baos.toByteArray());
				final SQLDataInputStream	sqli = new SQLDataInputStream(bais)) {
				final Object[]				place = new Object[1];
				
				Assert.assertEquals(sqlType, sqli.read(place));
				if (source instanceof byte[]) {
					Assert.assertArrayEquals((byte[])source, (byte[])place[0]);
				}
				else {
					Assert.assertEquals(result, place[0]);
				}
				Assert.assertEquals(SQLDataInputStream.EOL, sqli.read(place));
			}
		}
	}
	
	private static class AwaitedValues {
		private final int		type;
		private final Object	value;
		
		public AwaitedValues(int type, Object value) {
			this.type = type;
			this.value = value;
		}
	}
}
