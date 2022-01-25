package chav1961.purelib.streams.byte2byte;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.sql.Time;
import java.sql.Timestamp;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.cdb.CompilerUtils;

public class SQLDataOutputStream extends OutputStream implements DataOutput {
	static final Convertor[]	CONV = new Convertor[] {
											new Convertor(Types.BIT, CompilerUtils.CLASSTYPE_BOOLEAN, null, null),
											new Convertor(Types.BOOLEAN, CompilerUtils.CLASSTYPE_BOOLEAN, null, null),
											new Convertor(Types.TINYINT, CompilerUtils.CLASSTYPE_BYTE, null, null),
											new Convertor(Types.SMALLINT, CompilerUtils.CLASSTYPE_SHORT, null, null),
											new Convertor(Types.INTEGER, CompilerUtils.CLASSTYPE_INT, null, null),
											new Convertor(Types.BIGINT, CompilerUtils.CLASSTYPE_LONG, null, null),
											new Convertor(Types.FLOAT, CompilerUtils.CLASSTYPE_FLOAT, null, null),
											new Convertor(Types.REAL, CompilerUtils.CLASSTYPE_DOUBLE, null, null),
											new Convertor(Types.DOUBLE, CompilerUtils.CLASSTYPE_DOUBLE, null, null),
											new Convertor(Types.NUMERIC, CompilerUtils.CLASSTYPE_REFERENCE, null, null),
											new Convertor(Types.DECIMAL, CompilerUtils.CLASSTYPE_REFERENCE, null, null),
											new Convertor(Types.CHAR, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeChars(t, v.toString()), (t,i)->i.readUTF()),
											new Convertor(Types.VARCHAR, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeChars(t, v.toString()), (t,i)->i.readUTF()),
											new Convertor(Types.LONGVARCHAR, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeChars(t, v.toString()), (t,i)->i.readUTF()),
											new Convertor(Types.DATE, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeDate(t,(Date)v), (t,i)->i.readDate(t)),
											new Convertor(Types.TIME, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeDate(t,(Time)v), (t,i)->i.readDate(t)),
											new Convertor(Types.TIMESTAMP, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeDate(t,(Timestamp)v), (t,i)->i.readDate(t)),
											new Convertor(Types.BINARY, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeRaw(t,(byte[])v), null),
											new Convertor(Types.VARBINARY, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeRaw(t,(byte[])v), null),
											new Convertor(Types.LONGVARBINARY, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeRaw(t,(byte[])v), null),
											new Convertor(Types.NULL, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeNull(t), null),
											new Convertor(Types.JAVA_OBJECT, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeSerial((Serializable)v), null),
											new Convertor(Types.ARRAY, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeArray((Array)v), null),
											new Convertor(Types.BLOB, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeBlob((Blob)v), null),
											new Convertor(Types.CLOB, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeClob(t,(Clob)v), null),
											new Convertor(Types.NCHAR, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeChars(v.toString()), null),
											new Convertor(Types.NVARCHAR, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeChars(v.toString()), null),
											new Convertor(Types.LONGNVARCHAR, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeChars(v.toString()), null),
											new Convertor(Types.NCLOB, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeClob(t,(Clob)v), null),
											new Convertor(Types.SQLXML, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeXML((SQLXML)v), null),
											new Convertor(Types.TIME_WITH_TIMEZONE, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeDate(t,(Time)v), (t,i)->i.readDate(t)),
											new Convertor(Types.TIMESTAMP_WITH_TIMEZONE, CompilerUtils.CLASSTYPE_REFERENCE, (t,v,o)->o.writeDate(t,(Timestamp)v), (t,i)->i.readDate(t)),
//	                public final static int OTHER		= 1111;
//	                public final static int DISTINCT	= 2001;
//	                public final static int STRUCT		= 2002;
//	                public final static int REF			= 2006;
//	            	public final static int DATALINK 	= 70;
//	            	public final static int ROWID 		= -8;
//	            	public static final int REF_CURSOR 	= 2012;
										};
	
	static {
		Arrays.sort(CONV, (o1,o2)->o1.type - o2.type);
	}
	
	private final OutputStream 	nested;

	public SQLDataOutputStream(final OutputStream nested) {
		if (nested == null) {
			throw new NullPointerException("Nested stream can't be null");
		}
		else {
			this.nested = nested;
		}
	}

	@Override
	public void write(final int b) throws IOException {
		nested.write(b);
	}

	@Override
    public void write(byte b[], int off, int len) throws IOException {
    	nested.write(b, off, len);
    }

	@Override
    public void flush() throws IOException {
    	nested.flush();
    }

	@Override
    public void close() throws IOException {
    	nested.close();
    }

	public void write(final int sqlType, final Object value) throws IOException {
		final Convertor	conv = getConvertor(sqlType);
		
		if (conv == null) {
			throw new UnsupportedOperationException("SQL type ["+sqlType+"] is not supported yet"); 
		}
		else {
			switch (conv.classType) {
				case CompilerUtils.CLASSTYPE_REFERENCE	:
					if (value == null) {
						writeNull(sqlType);
					}
					else {
						conv.convOut.process(sqlType, value, this);
					}
					break;
				case CompilerUtils.CLASSTYPE_BYTE		:
					if (value == null) {
						writeNull(sqlType);
					}
					else {
						writeByte(((Number)value).byteValue());
					}
					break;
				case CompilerUtils.CLASSTYPE_SHORT		:
					if (value == null) {
						writeNull(sqlType);
					}
					else {
						writeShort(((Number)value).shortValue());
					}
					break;
				case CompilerUtils.CLASSTYPE_CHAR		:
					if (value == null) {
						writeNull(sqlType);
					}
					else {
						writeChar((Character)value);
					}
					break;
				case CompilerUtils.CLASSTYPE_INT		:
					if (value == null) {
						writeNull(sqlType);
					}
					else {
						writeInt(((Number)value).intValue());
					}
					break;
				case CompilerUtils.CLASSTYPE_LONG		:
					if (value == null) {
						writeNull(sqlType);
					}
					else {
						writeLong(((Number)value).longValue());
					}
					break;
				case CompilerUtils.CLASSTYPE_FLOAT		:
					if (value == null) {
						writeNull(sqlType);
					}
					else {
						writeFloat(((Number)value).floatValue());
					}
					break;
				case CompilerUtils.CLASSTYPE_DOUBLE		:
					if (value == null) {
						writeNull(sqlType);
					}
					else {
						writeDouble(((Number)value).doubleValue());
					}
					break;
				case CompilerUtils.CLASSTYPE_BOOLEAN 	:
					if (value == null) {
						writeNull(sqlType);
					}
					else {
						writeBoolean((Boolean)value);
					}
					break;
				default : throw new UnsupportedOperationException("Class type ["+conv.classType+"] is not supported yet"); 
			}
		}
	}

	public void writeLine() throws IOException {
		writeBytes(Short.MAX_VALUE, 2);
	}
	
	public void writeNull(final int sqlType) throws IOException {
		writeBytes(Types.NULL, 2);
		writeBytes(sqlType, 2);
	}
	
	@Override
	public void writeBoolean(final boolean v) throws IOException {
		writeBytes(Types.BIT, 2);
		writeBytes(v ? 1 : 0, 1);
	}

	@Override
	public void writeByte(final int v) throws IOException {
		writeBytes(Types.TINYINT, 2);
		writeBytes(v, 1);
	}

	@Override
	public void writeShort(final int v) throws IOException {
		writeBytes(Types.SMALLINT, 2);
		writeBytes(v, 2);
	}

	@Override
	public void writeChar(final int v) throws IOException {
		writeBytes(Types.CHAR, 2);
		writeBytes(1, 4);
		writeBytes(v, 2);
	}

	@Override
	public void writeInt(final int v) throws IOException {
		writeBytes(Types.INTEGER, 2);
		writeBytes(v, 4);
	}

	@Override
	public void writeLong(final long v) throws IOException {
		writeBytes(Types.BIGINT, 2);
		writeBytes(v, 8);
	}

	@Override
	public void writeFloat(final float v) throws IOException {
		writeBytes(Types.FLOAT, 2);
		writeBytes(Float.floatToIntBits(v), 4);
	}

	@Override
	public void writeDouble(final double v) throws IOException {
		writeBytes(Types.DOUBLE, 2);
		writeBytes(Double.doubleToLongBits(v), 8);
	}

	@Override
	public void writeBytes(final String s) throws IOException {
		writeChars(s);
	}

	@Override
	public void writeChars(final String s) throws IOException {
		if (s == null) {
			throw new NullPointerException("String to write can't be null"); 
		}
		else {
			writeBytes(Types.CHAR, 2);
			writeBytes(s.length(), 4);
			for (int index = 0, maxIndex = s.length(); index < maxIndex; index++) {
				writeBytes(s.charAt(index), 2);
			}
		}
	}

	@Override
	public void writeUTF(final String s) throws IOException {
		writeChars(s);
	}

	private void writeBytes(final long value, final int length) throws IOException {
		final byte[]	temp = new byte[8];
		
		for (int index = length, target = 0; index >= 0; index--, target++) {
			temp[target] = (byte) ((value >> (8 * index)) & 0xFF);
		}
		nested.write(temp, 0, length);
	}

	private void writeChars(final int sqlType, final String value) throws IOException {
		writeBytes(sqlType, 2);
		writeBytes(value.length(), 4);
		for (int index = 0, maxIndex = value.length(); index < maxIndex; index++) {
			writeBytes(value.charAt(index), 2);
		}
	}
	
	
	private void writeDate(final int sqlType, final Date date) throws IOException {
		writeBytes(sqlType, 2);
		writeBytes(date.getTime(),8);				
	}

	private void writeRaw(final int sqlType, final byte[] obj) throws IOException {
		writeBytes(sqlType, 2);
		writeBytes(obj.length, 4);
		nested.write(obj);				
	}

	private void writeArray(final Array array) {
		throw new UnsupportedOperationException(); 
	}

	private void writeSerial(final Serializable obj) throws IOException {
		final byte[]	content;
	
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final ObjectOutputStream	oos = new ObjectOutputStream(baos)) {
			
			oos.writeObject(obj);
			oos.flush();
			content = baos.toByteArray();
		}
		writeBytes(Types.JAVA_OBJECT, 2);
		writeBytes(content.length, 4);
		nested.write(content);				
	}
	
	private void writeBlob(final Blob blob) throws IOException {
		writeBytes(Types.BLOB, 2);
		try{writeBytes(blob.length(), 8);
			Utils.copyStream(blob.getBinaryStream(), nested);	 
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
	
	private void writeClob(final int sqlType, final Clob clob) throws IOException {
		writeBytes(sqlType, 2);
		try{writeBytes(clob.length(), 8);
			final Writer	wr = new OutputStreamWriter(nested, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			
			Utils.copyStream(clob.getCharacterStream(), wr);
			wr.flush();	 
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	private void writeXML(final SQLXML xml) {
		throw new UnsupportedOperationException(); 
	}
	
	static Convertor getConvertor(final int type) {
		final Convertor[]	temp = CONV;
		int 				low = 0, high = temp.length - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			int	midVal = temp[mid].type;
			
			if (midVal < type) {
				low = mid + 1;	
			}
			else if (midVal > type) {
				high = mid - 1;
			}
			else {
				return temp[mid];
			}
		}
		return null;
	}

	static class Convertor {
		@FunctionalInterface
		interface OutputConvertor {
			void process(final int sqlType, Object value, final SQLDataOutputStream os) throws IOException;
		}

		@FunctionalInterface
		interface InputConvertor {
			Object process(final int sqlType, final SQLDataInputStream is) throws IOException;
		}
		
		final int				type;
		final int				classType;
		final OutputConvertor	convOut;
		final InputConvertor	convIn;
		
		private Convertor(final int type, final int classType, final OutputConvertor convOut, final InputConvertor convIn) {
			this.type = type;
			this.classType = classType;
			this.convOut = convOut;
			this.convIn = convIn;
		}
	}
}
