package chav1961.purelib.sql;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.sql.SQLUtils.ByteArrayInputStreamWithEquals;
import chav1961.purelib.sql.SQLUtils.StringReaderWithEquals;

public class SQLUtilsTest {
	private static final ConversionPairTest[]	PAIRS = {new ConversionPairTest(Boolean.class,Boolean.class,true,false)
													, new ConversionPairTest(Boolean.class,Byte.class,true,false)
													, new ConversionPairTest(Boolean.class,Short.class,true,false)
													, new ConversionPairTest(Boolean.class,Integer.class,true,false)
													, new ConversionPairTest(Boolean.class,Long.class,true,false)
													, new ConversionPairTest(Boolean.class,Float.class,ContentException.class,Boolean.valueOf(false))
													, new ConversionPairTest(Boolean.class,Double.class,ContentException.class,Boolean.valueOf(false))
													, new ConversionPairTest(Boolean.class,BigInteger.class,true,false)
													, new ConversionPairTest(Boolean.class,BigDecimal.class,ContentException.class,Boolean.valueOf(false))
													, new ConversionPairTest(Boolean.class,Blob.class,true,false)
													, new ConversionPairTest(Boolean.class,Clob.class,true,false)
													, new ConversionPairTest(Boolean.class,NClob.class,true,false)
													, new ConversionPairTest(Boolean.class,byte[].class,true,false)
													, new ConversionPairTest(Boolean.class,String.class,true,false)
													, new ConversionPairTest(Boolean.class,Date.class,ContentException.class,Boolean.valueOf(false))
													, new ConversionPairTest(Boolean.class,Time.class,ContentException.class,false)
													, new ConversionPairTest(Boolean.class,Timestamp.class,ContentException.class)
													, new ConversionPairTest(Boolean.class,InputStream.class,true,false)
													, new ConversionPairTest(Boolean.class,Reader.class,true,false)
													, new ConversionPairTest(Boolean.class,OutputStream.class,ContentException.class,Boolean.valueOf(false))
													, new ConversionPairTest(Boolean.class,Writer.class,ContentException.class,Boolean.valueOf(false))
													, new ConversionPairTest(Boolean.class,Object.class,true,false)
													, new ConversionPairTest(Boolean.class,Array.class,true,false)
													, new ConversionPairTest(Boolean.class,Struct.class,ContentException.class,Boolean.valueOf(false))
													, new ConversionPairTest(Boolean.class,RowId.class,ContentException.class,Boolean.valueOf(false))
													, new ConversionPairTest(Boolean.class,SQLXML.class,ContentException.class,Boolean.valueOf(false))
													// -----------------------
													, new ConversionPairTest(Byte.class,Boolean.class,(byte)0,(byte)1)
													, new ConversionPairTest(Byte.class,Byte.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,Short.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,Integer.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,Long.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,Float.class,ContentException.class,(byte)0)
													, new ConversionPairTest(Byte.class,Double.class,ContentException.class,(byte)0)
													, new ConversionPairTest(Byte.class,BigInteger.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,BigDecimal.class,ContentException.class,(byte)0)
													, new ConversionPairTest(Byte.class,Blob.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,Clob.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,NClob.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,byte[].class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,String.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,Date.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,Time.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,Timestamp.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,InputStream.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,Reader.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,OutputStream.class,ContentException.class,(byte)0)
													, new ConversionPairTest(Byte.class,Writer.class,ContentException.class,(byte)0)
													, new ConversionPairTest(Byte.class,Object.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,Array.class,(byte)0,(byte)1,(byte)-1)
													, new ConversionPairTest(Byte.class,Struct.class,ContentException.class,(byte)0)
													, new ConversionPairTest(Byte.class,RowId.class,ContentException.class,(byte)0)
													, new ConversionPairTest(Byte.class,SQLXML.class,ContentException.class,(byte)0)
													// -----------------------
													, new ConversionPairTest(Short.class,Boolean.class,(short)0,(short)1)
													, new ConversionPairTest(Short.class,Byte.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,Short.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,Integer.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,Long.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,Float.class,ContentException.class,(short)0)
													, new ConversionPairTest(Short.class,Double.class,ContentException.class,(short)0)
													, new ConversionPairTest(Short.class,BigInteger.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,BigDecimal.class,ContentException.class,(short)0)
													, new ConversionPairTest(Short.class,Blob.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,Clob.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,NClob.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,byte[].class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,String.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,Date.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,Time.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,Timestamp.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,InputStream.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,Reader.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,OutputStream.class,ContentException.class,(short)0)
													, new ConversionPairTest(Short.class,Writer.class,ContentException.class,(short)0)
													, new ConversionPairTest(Short.class,Object.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,Array.class,(short)0,(short)1,(short)-1)
													, new ConversionPairTest(Short.class,Struct.class,ContentException.class,(short)0)
													, new ConversionPairTest(Short.class,RowId.class,ContentException.class,(short)0)
													, new ConversionPairTest(Short.class,SQLXML.class,ContentException.class,(short)0)
													// -----------------------
													, new ConversionPairTest(Integer.class,Boolean.class,0,1)
													, new ConversionPairTest(Integer.class,Byte.class,0,1,-1)
													, new ConversionPairTest(Integer.class,Short.class,0,1,-1)
													, new ConversionPairTest(Integer.class,Integer.class,0,1,-1)
													, new ConversionPairTest(Integer.class,Long.class,0,1,-1)
													, new ConversionPairTest(Integer.class,Float.class,ContentException.class,0)
													, new ConversionPairTest(Integer.class,Double.class,ContentException.class,0)
													, new ConversionPairTest(Integer.class,BigInteger.class,0,1,-1)
													, new ConversionPairTest(Integer.class,BigDecimal.class,ContentException.class,0)
													, new ConversionPairTest(Integer.class,Blob.class,0,1,-1)
													, new ConversionPairTest(Integer.class,Clob.class,0,1,-1)
													, new ConversionPairTest(Integer.class,NClob.class,0,1,-1)
													, new ConversionPairTest(Integer.class,byte[].class,0,1,-1)
													, new ConversionPairTest(Integer.class,String.class,0,1,-1)
													, new ConversionPairTest(Integer.class,Date.class,0,1,-1)
													, new ConversionPairTest(Integer.class,Time.class,0,1,-1)
													, new ConversionPairTest(Integer.class,Timestamp.class,0,1,-1)
													, new ConversionPairTest(Integer.class,InputStream.class,0,1,-1)
													, new ConversionPairTest(Integer.class,Reader.class,0,1,-1)
													, new ConversionPairTest(Integer.class,OutputStream.class,ContentException.class,0)
													, new ConversionPairTest(Integer.class,Writer.class,ContentException.class,0)
													, new ConversionPairTest(Integer.class,Object.class,0,1,-1)
													, new ConversionPairTest(Integer.class,Array.class,0,1,-1)
													, new ConversionPairTest(Integer.class,Struct.class,ContentException.class,0)
													, new ConversionPairTest(Integer.class,RowId.class,ContentException.class,0)
													, new ConversionPairTest(Integer.class,SQLXML.class,ContentException.class,0)
													// -----------------------
													, new ConversionPairTest(Long.class,Boolean.class,0L,1L)
													, new ConversionPairTest(Long.class,Byte.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,Short.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,Integer.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,Long.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,Float.class,ContentException.class,0L)
													, new ConversionPairTest(Long.class,Double.class,ContentException.class,0L)
													, new ConversionPairTest(Long.class,BigInteger.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,BigDecimal.class,ContentException.class,0L)
													, new ConversionPairTest(Long.class,Blob.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,Clob.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,NClob.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,byte[].class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,String.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,Date.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,Time.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,Timestamp.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,InputStream.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,Reader.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,OutputStream.class,ContentException.class,0L)
													, new ConversionPairTest(Long.class,Writer.class,ContentException.class,0L)
													, new ConversionPairTest(Long.class,Object.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,Array.class,0L,1L,-1L)
													, new ConversionPairTest(Long.class,Struct.class,ContentException.class,0L)
													, new ConversionPairTest(Long.class,RowId.class,ContentException.class,0L)
													, new ConversionPairTest(Long.class,SQLXML.class,ContentException.class,0L)
													// -----------------------
													, new ConversionPairTest(Float.class,Boolean.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,Byte.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,Short.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,Integer.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,Long.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,Float.class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,Double.class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,BigInteger.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,BigDecimal.class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,Blob.class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,Clob.class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,NClob.class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,byte[].class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,String.class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,Date.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,Time.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,Timestamp.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,InputStream.class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,Reader.class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,OutputStream.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,Writer.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,Object.class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,Array.class,0f,1f,-1f)
													, new ConversionPairTest(Float.class,Struct.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,RowId.class,ContentException.class,0f)
													, new ConversionPairTest(Float.class,SQLXML.class,ContentException.class,0f)
													// -----------------------
													, new ConversionPairTest(Double.class,Boolean.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,Byte.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,Short.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,Integer.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,Long.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,Float.class,0.0,1.0,-1.0)
													, new ConversionPairTest(Double.class,Double.class,0.0,1.0,-1.0)
													, new ConversionPairTest(Double.class,BigInteger.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,BigDecimal.class,0.0,1.0,-1.0)
													, new ConversionPairTest(Double.class,Blob.class,0.0,1.0,-1.0)
													, new ConversionPairTest(Double.class,Clob.class,0.0,1.0,-1.0)
													, new ConversionPairTest(Double.class,NClob.class,0.0,1.0,-1.0)
													, new ConversionPairTest(Double.class,byte[].class,0.0,1.0,-1.0)
													, new ConversionPairTest(Double.class,String.class,0.0,1.0,-1.0)
													, new ConversionPairTest(Double.class,Date.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,Time.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,Timestamp.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,InputStream.class,0.0,1.0,-1.0)
			 										, new ConversionPairTest(Double.class,Reader.class,0.0,1.0,-1.0)
													, new ConversionPairTest(Double.class,OutputStream.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,Writer.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,Object.class,0.0,1.0,-1.0)
													, new ConversionPairTest(Double.class,Array.class,0.0,1.0,-1.0)
													, new ConversionPairTest(Double.class,Struct.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,RowId.class,ContentException.class,0.0)
													, new ConversionPairTest(Double.class,SQLXML.class,ContentException.class,0.0)
													// -----------------------
													, new ConversionPairTest(BigInteger.class,Boolean.class,BigInteger.ZERO,BigInteger.ONE)
													, new ConversionPairTest(BigInteger.class,Byte.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,Short.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,Integer.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,Long.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,Float.class,ContentException.class,BigInteger.ZERO)
													, new ConversionPairTest(BigInteger.class,Double.class,ContentException.class,BigInteger.ZERO)
													, new ConversionPairTest(BigInteger.class,BigInteger.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,BigDecimal.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,Blob.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,Clob.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,NClob.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,byte[].class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,String.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,Date.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,Time.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,Timestamp.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,InputStream.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
			 										, new ConversionPairTest(BigInteger.class,Reader.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,OutputStream.class,ContentException.class,BigInteger.ZERO)
													, new ConversionPairTest(BigInteger.class,Writer.class,ContentException.class,BigInteger.ZERO)
													, new ConversionPairTest(BigInteger.class,Object.class,BigInteger.ZERO,BigInteger.ONE,BigInteger.ONE.negate())
													, new ConversionPairTest(BigInteger.class,Array.class,ContentException.class,BigInteger.ZERO)
													, new ConversionPairTest(BigInteger.class,Struct.class,ContentException.class,BigInteger.ZERO)
													, new ConversionPairTest(BigInteger.class,RowId.class,ContentException.class,BigInteger.ZERO)
													, new ConversionPairTest(BigInteger.class,SQLXML.class,ContentException.class,BigInteger.ZERO)
													// -----------------------
													, new ConversionPairTest(BigDecimal.class,Float.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,Double.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,BigInteger.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,BigDecimal.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,Blob.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,Clob.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,NClob.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,byte[].class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,String.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,InputStream.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
			 										, new ConversionPairTest(BigDecimal.class,Reader.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,OutputStream.class,ContentException.class,BigDecimal.ZERO)
													, new ConversionPairTest(BigDecimal.class,Writer.class,ContentException.class,BigDecimal.ZERO)
													, new ConversionPairTest(BigDecimal.class,Object.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,Array.class,BigDecimal.ZERO,BigDecimal.ONE,BigDecimal.ONE.negate())
													, new ConversionPairTest(BigDecimal.class,Struct.class,ContentException.class,BigDecimal.ZERO)
													, new ConversionPairTest(BigDecimal.class,RowId.class,ContentException.class,BigDecimal.ZERO)
													, new ConversionPairTest(BigDecimal.class,SQLXML.class,ContentException.class,BigDecimal.ZERO)
													// -----------------------
													, new ConversionPairTest(byte[].class,Boolean.class,new byte[]{0},new byte[]{1})
													, new ConversionPairTest(byte[].class,Byte.class,new byte[]{0},new byte[]{1},new byte[]{-1})
													, new ConversionPairTest(byte[].class,Short.class,new byte[]{0,0},new byte[]{0,1},new byte[]{-1,-1})
													, new ConversionPairTest(byte[].class,Integer.class,new byte[]{0,0,0,0},new byte[]{0,0,0,1},new byte[]{-1,-1,-1,-1})
													, new ConversionPairTest(byte[].class,Long.class,new byte[]{0,0,0,0,0,0,0,0},new byte[]{0,0,0,0,0,0,0,1},new byte[]{-1,-1,-1,-1,-1,-1,-1,-1})
													, new ConversionPairTest(byte[].class,Float.class,toByteArray(0f),toByteArray(1f),toByteArray(-1f))
													, new ConversionPairTest(byte[].class,Double.class,toByteArray(0.0),toByteArray(1.0),toByteArray(-1.0))
													, new ConversionPairTest(byte[].class,Blob.class,new byte[]{0},new byte[]{1},new byte[]{-1})
													, new ConversionPairTest(byte[].class,Clob.class,new byte[]{0},new byte[]{1},new byte[]{-1})
													, new ConversionPairTest(byte[].class,NClob.class,new byte[]{0},new byte[]{1},new byte[]{-1})
													, new ConversionPairTest(byte[].class,byte[].class,new byte[]{0},new byte[]{1},new byte[]{-1})
													, new ConversionPairTest(byte[].class,String.class,new byte[]{0},new byte[]{1},new byte[]{-1})
													, new ConversionPairTest(byte[].class,InputStream.class,new byte[]{0},new byte[]{1},new byte[]{-1})
			 										, new ConversionPairTest(byte[].class,Reader.class,new byte[]{0},new byte[]{1},new byte[]{-1})
													, new ConversionPairTest(byte[].class,OutputStream.class,ContentException.class,new byte[0])
													, new ConversionPairTest(byte[].class,Writer.class,ContentException.class,new byte[0])
													, new ConversionPairTest(byte[].class,Object.class,new byte[]{0},new byte[]{1},new byte[]{-1})
													, new ConversionPairTest(byte[].class,Array.class,new byte[]{0},new byte[]{1},new byte[]{-1})
													, new ConversionPairTest(byte[].class,Struct.class,ContentException.class,new byte[0])
													, new ConversionPairTest(byte[].class,RowId.class,ContentException.class,new byte[0])
													, new ConversionPairTest(byte[].class,SQLXML.class,ContentException.class,new byte[0])
													// -----------------------
													, new ConversionPairTest(Blob.class,Boolean.class,new InMemoryLittleBlob(new byte[]{0}),new InMemoryLittleBlob(new byte[]{1}))
													, new ConversionPairTest(Blob.class,Byte.class,new InMemoryLittleBlob(new byte[]{0}),new InMemoryLittleBlob(new byte[]{1}),new InMemoryLittleBlob(new byte[]{-1}))
													, new ConversionPairTest(Blob.class,Short.class,new InMemoryLittleBlob(new byte[]{0,0}),new InMemoryLittleBlob(new byte[]{0,1}),new InMemoryLittleBlob(new byte[]{-1,-1}))
													, new ConversionPairTest(Blob.class,Integer.class,new InMemoryLittleBlob(new byte[]{0,0,0,0}),new InMemoryLittleBlob(new byte[]{0,0,0,1}),new InMemoryLittleBlob(new byte[]{-1,-1,-1,-1}))
													, new ConversionPairTest(Blob.class,Long.class,new InMemoryLittleBlob(new byte[]{0,0,0,0,0,0,0,0,}),new InMemoryLittleBlob(new byte[]{0,0,0,0,0,0,0,1}),new InMemoryLittleBlob(new byte[]{-1,-1,-1,-1,-1,-1,-1,-1}))
													, new ConversionPairTest(Blob.class,Float.class,new InMemoryLittleBlob(toByteArray(0f)),new InMemoryLittleBlob(toByteArray(1f)),new InMemoryLittleBlob(toByteArray(-1f)))
													, new ConversionPairTest(Blob.class,Double.class,new InMemoryLittleBlob(toByteArray(0.0)),new InMemoryLittleBlob(toByteArray(1.0)),new InMemoryLittleBlob(toByteArray(-1.0)))
													, new ConversionPairTest(Blob.class,Blob.class,new InMemoryLittleBlob(new byte[]{0}),new InMemoryLittleBlob(new byte[]{1}),new InMemoryLittleBlob(new byte[]{-1}))
													, new ConversionPairTest(Blob.class,Clob.class,new InMemoryLittleBlob(new byte[]{0}),new InMemoryLittleBlob(new byte[]{1}),new InMemoryLittleBlob(new byte[]{-1}))
													, new ConversionPairTest(Blob.class,NClob.class,new InMemoryLittleBlob(new byte[]{0}),new InMemoryLittleBlob(new byte[]{1}),new InMemoryLittleBlob(new byte[]{-1}))
													, new ConversionPairTest(Blob.class,byte[].class,new InMemoryLittleBlob(new byte[]{0}),new InMemoryLittleBlob(new byte[]{1}),new InMemoryLittleBlob(new byte[]{-1}))
													, new ConversionPairTest(Blob.class,String.class,new InMemoryLittleBlob(new byte[]{0}),new InMemoryLittleBlob(new byte[]{1}),new InMemoryLittleBlob(new byte[]{-1}))
													, new ConversionPairTest(Blob.class,InputStream.class,new InMemoryLittleBlob(new byte[]{0}),new InMemoryLittleBlob(new byte[]{1}),new InMemoryLittleBlob(new byte[]{-1}))
			 										, new ConversionPairTest(Blob.class,Reader.class,new InMemoryLittleBlob(new byte[]{0}),new InMemoryLittleBlob(new byte[]{1}),new InMemoryLittleBlob(new byte[]{-1}))
													, new ConversionPairTest(Blob.class,OutputStream.class,ContentException.class,new InMemoryLittleBlob(new byte[]{0}))
													, new ConversionPairTest(Blob.class,Writer.class,ContentException.class,new InMemoryLittleBlob(new byte[]{0}))
													, new ConversionPairTest(Blob.class,Object.class,new InMemoryLittleBlob(new byte[]{0}),new InMemoryLittleBlob(new byte[]{1}),new InMemoryLittleBlob(new byte[]{-1}))
													, new ConversionPairTest(Blob.class,Array.class,new InMemoryLittleBlob(new byte[]{0}),new InMemoryLittleBlob(new byte[]{1}),new InMemoryLittleBlob(new byte[]{-1}))
													, new ConversionPairTest(Blob.class,Struct.class,ContentException.class,new InMemoryLittleBlob(new byte[]{0}))
													, new ConversionPairTest(Blob.class,RowId.class,ContentException.class,new InMemoryLittleBlob(new byte[]{0}))
													, new ConversionPairTest(Blob.class,SQLXML.class,ContentException.class,new InMemoryLittleBlob(new byte[]{0}))
													// -----------------------
													, new ConversionPairTest(Clob.class,Boolean.class,new InMemoryLittleClob("false"),new InMemoryLittleClob("true"))
													, new ConversionPairTest(Clob.class,Byte.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
													, new ConversionPairTest(Clob.class,Short.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
													, new ConversionPairTest(Clob.class,Integer.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
													, new ConversionPairTest(Clob.class,Long.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
													, new ConversionPairTest(Clob.class,Float.class,new InMemoryLittleClob("0.0"),new InMemoryLittleClob("1.0"),new InMemoryLittleClob("-1.0"))
													, new ConversionPairTest(Clob.class,Double.class,new InMemoryLittleClob("0.0"),new InMemoryLittleClob("1.0"),new InMemoryLittleClob("-1.0"))
													, new ConversionPairTest(Clob.class,Blob.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
													, new ConversionPairTest(Clob.class,Clob.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
// parent-child compatible!							, new ConversionPairTest(Clob.class,NClob.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
													, new ConversionPairTest(Clob.class,byte[].class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
													, new ConversionPairTest(Clob.class,String.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
													, new ConversionPairTest(Clob.class,InputStream.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
			 										, new ConversionPairTest(Clob.class,Reader.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
													, new ConversionPairTest(Clob.class,OutputStream.class,ContentException.class,new InMemoryLittleClob())
													, new ConversionPairTest(Clob.class,Writer.class,ContentException.class,new InMemoryLittleClob())
													, new ConversionPairTest(Clob.class,Object.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
													, new ConversionPairTest(Clob.class,Array.class,new InMemoryLittleClob("0"),new InMemoryLittleClob("1"),new InMemoryLittleClob("-1"))
													, new ConversionPairTest(Clob.class,Struct.class,ContentException.class,new InMemoryLittleClob())
													, new ConversionPairTest(Clob.class,RowId.class,ContentException.class,new InMemoryLittleClob())
													, new ConversionPairTest(Clob.class,SQLXML.class,ContentException.class,new InMemoryLittleClob())
													// -----------------------
													, new ConversionPairTest(NClob.class,Boolean.class,new InMemoryLittleNClob("false"),new InMemoryLittleNClob("true"))
													, new ConversionPairTest(NClob.class,Byte.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
													, new ConversionPairTest(NClob.class,Short.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
													, new ConversionPairTest(NClob.class,Integer.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
													, new ConversionPairTest(NClob.class,Long.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
//													, new ConversionPairTest(NClob.class,Float.class,new InMemoryLittleNClob("0.0"),new InMemoryLittleNClob("1.0"),new InMemoryLittleNClob("-1.0"))
//													, new ConversionPairTest(NClob.class,Double.class,new InMemoryLittleNClob("0.0"),new InMemoryLittleNClob("1.0"),new InMemoryLittleNClob("-1.0"))
													, new ConversionPairTest(NClob.class,Blob.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
													, new ConversionPairTest(NClob.class,Clob.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
// parent-child compatible!							, new ConversionPairTest(NClob.class,NClob.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
													, new ConversionPairTest(NClob.class,byte[].class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
													, new ConversionPairTest(NClob.class,String.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
													, new ConversionPairTest(NClob.class,InputStream.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
			 										, new ConversionPairTest(NClob.class,Reader.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
													, new ConversionPairTest(NClob.class,OutputStream.class,ContentException.class,new InMemoryLittleNClob())
													, new ConversionPairTest(NClob.class,Writer.class,ContentException.class,new InMemoryLittleNClob())
													, new ConversionPairTest(NClob.class,Object.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
//													, new ConversionPairTest(NClob.class,Array.class,new InMemoryLittleNClob("0"),new InMemoryLittleNClob("1"),new InMemoryLittleNClob("-1"))
													, new ConversionPairTest(NClob.class,Struct.class,ContentException.class,new InMemoryLittleNClob())
													, new ConversionPairTest(NClob.class,RowId.class,ContentException.class,new InMemoryLittleNClob())
													, new ConversionPairTest(NClob.class,SQLXML.class,ContentException.class,new InMemoryLittleNClob())
													// -----------------------
													, new ConversionPairTest(Date.class,Byte.class,new Date(0), new Date(1), new Date(-1))
													, new ConversionPairTest(Date.class,Short.class,new Date(0), new Date(1), new Date(-1))
													, new ConversionPairTest(Date.class,Integer.class,new Date(0), new Date(1), new Date(-1))
													, new ConversionPairTest(Date.class,Long.class,new Date(0), new Date(1), new Date(-1))
													, new ConversionPairTest(Date.class,byte[].class,new Date(0), new Date(1), new Date(-1))
													, new ConversionPairTest(Date.class,String.class,new Date(0), new Date(1), new Date(-1))
													, new ConversionPairTest(Date.class,OutputStream.class,ContentException.class,new Date(0))
													, new ConversionPairTest(Date.class,Writer.class,ContentException.class,new Date(0))
													, new ConversionPairTest(Date.class,Object.class,new Date(0), new Date(1), new Date(-1))
													, new ConversionPairTest(Date.class,Array.class,new Date(0), new Date(1), new Date(-1))
													, new ConversionPairTest(Date.class,Struct.class,ContentException.class,new Date(0))
													, new ConversionPairTest(Date.class,RowId.class,ContentException.class,new Date(0))
													, new ConversionPairTest(Date.class,SQLXML.class,ContentException.class,new Date(0))
													// -----------------------
													, new ConversionPairTest(Time.class,Byte.class,new Time(0), new Time(1), new Time(-1))
													, new ConversionPairTest(Time.class,Short.class,new Time(0), new Time(1), new Time(-1))
													, new ConversionPairTest(Time.class,Integer.class,new Time(0), new Time(1), new Time(-1))
													, new ConversionPairTest(Time.class,Long.class,new Time(0), new Time(1), new Time(-1))
													, new ConversionPairTest(Time.class,byte[].class,new Time(0), new Time(1), new Time(-1))
													, new ConversionPairTest(Time.class,String.class,new Time(0), new Time(1), new Time(-1))
													, new ConversionPairTest(Time.class,OutputStream.class,ContentException.class,new Time(0))
													, new ConversionPairTest(Time.class,Writer.class,ContentException.class,new Time(0))
													, new ConversionPairTest(Time.class,Object.class,new Time(0), new Time(1), new Time(-1))
													, new ConversionPairTest(Time.class,Array.class,new Time(0), new Time(1), new Time(-1))
													, new ConversionPairTest(Time.class,Struct.class,ContentException.class,new Time(0))
													, new ConversionPairTest(Time.class,RowId.class,ContentException.class,new Time(0))
													, new ConversionPairTest(Time.class,SQLXML.class,ContentException.class,new Time(0))
													// -----------------------
													, new ConversionPairTest(Timestamp.class,Byte.class,new Timestamp(0), new Timestamp(1), new Timestamp(-1))
													, new ConversionPairTest(Timestamp.class,Short.class,new Timestamp(0), new Timestamp(1), new Timestamp(-1))
													, new ConversionPairTest(Timestamp.class,Integer.class,new Timestamp(0), new Timestamp(1), new Timestamp(-1))
													, new ConversionPairTest(Timestamp.class,Long.class,new Timestamp(0), new Timestamp(1), new Timestamp(-1))
													, new ConversionPairTest(Timestamp.class,byte[].class,new Timestamp(0), new Timestamp(1), new Timestamp(-1))
													, new ConversionPairTest(Timestamp.class,String.class,new Timestamp(0), new Timestamp(1), new Timestamp(-1))
													, new ConversionPairTest(Timestamp.class,OutputStream.class,ContentException.class,new Timestamp(0))
													, new ConversionPairTest(Timestamp.class,Writer.class,ContentException.class,new Timestamp(0))
													, new ConversionPairTest(Timestamp.class,Object.class,new Timestamp(0), new Timestamp(1), new Timestamp(-1))
													, new ConversionPairTest(Timestamp.class,Array.class,new Timestamp(0), new Timestamp(1), new Timestamp(-1))
													, new ConversionPairTest(Timestamp.class,Struct.class,ContentException.class,new Timestamp(0))
													, new ConversionPairTest(Timestamp.class,RowId.class,ContentException.class,new Timestamp(0))
													, new ConversionPairTest(Timestamp.class,SQLXML.class,ContentException.class,new Timestamp(0))
													// -----------------------
													, new ConversionPairTest(InputStream.class,Boolean.class,new ByteArrayInputStreamWithEquals(new byte[]{0}),new ByteArrayInputStreamWithEquals(new byte[]{1}))
													, new ConversionPairTest(InputStream.class,Byte.class,new ByteArrayInputStreamWithEquals(new byte[]{0}),new ByteArrayInputStreamWithEquals(new byte[]{1}),new ByteArrayInputStreamWithEquals(new byte[]{-1}))
													, new ConversionPairTest(InputStream.class,Short.class,new ByteArrayInputStreamWithEquals(new byte[]{0,0}),new ByteArrayInputStreamWithEquals(new byte[]{0,1}),new ByteArrayInputStreamWithEquals(new byte[]{-1,-1}))
													, new ConversionPairTest(InputStream.class,Integer.class,new ByteArrayInputStreamWithEquals(new byte[]{0,0,0,0}),new ByteArrayInputStreamWithEquals(new byte[]{0,0,0,1}),new ByteArrayInputStreamWithEquals(new byte[]{-1,-1,-1,-1}))
													, new ConversionPairTest(InputStream.class,Long.class,new ByteArrayInputStreamWithEquals(new byte[]{0,0,0,0,0,0,0,0}),new ByteArrayInputStreamWithEquals(new byte[]{0,0,0,0,0,0,0,1}),new ByteArrayInputStreamWithEquals(new byte[]{-1,-1,-1,-1,-1,-1,-1,-1}))
													, new ConversionPairTest(InputStream.class,Float.class,new ByteArrayInputStreamWithEquals(toByteArray(0f)),new ByteArrayInputStreamWithEquals(toByteArray(1f)),new ByteArrayInputStreamWithEquals(toByteArray(-1f)))
													, new ConversionPairTest(InputStream.class,Double.class,new ByteArrayInputStreamWithEquals(toByteArray(0.0)),new ByteArrayInputStreamWithEquals(toByteArray(1.0)),new ByteArrayInputStreamWithEquals(toByteArray(-1.0)))
													, new ConversionPairTest(InputStream.class,Blob.class,new ByteArrayInputStreamWithEquals(new byte[]{0}),new ByteArrayInputStreamWithEquals(new byte[]{1}),new ByteArrayInputStreamWithEquals(new byte[]{-1}))
													, new ConversionPairTest(InputStream.class,Clob.class,new ByteArrayInputStreamWithEquals(new byte[]{0}),new ByteArrayInputStreamWithEquals(new byte[]{1}),new ByteArrayInputStreamWithEquals(new byte[]{-1}))
													, new ConversionPairTest(InputStream.class,NClob.class,new ByteArrayInputStreamWithEquals(new byte[]{0}),new ByteArrayInputStreamWithEquals(new byte[]{1}),new ByteArrayInputStreamWithEquals(new byte[]{-1}))
													, new ConversionPairTest(InputStream.class,byte[].class,new ByteArrayInputStreamWithEquals(new byte[]{0}),new ByteArrayInputStreamWithEquals(new byte[]{1}),new ByteArrayInputStreamWithEquals(new byte[]{-1}))
													, new ConversionPairTest(InputStream.class,String.class,new ByteArrayInputStreamWithEquals(new byte[]{0}),new ByteArrayInputStreamWithEquals(new byte[]{1}),new ByteArrayInputStreamWithEquals(new byte[]{-1}))
													, new ConversionPairTest(InputStream.class,InputStream.class,new ByteArrayInputStreamWithEquals(new byte[]{0}),new ByteArrayInputStreamWithEquals(new byte[]{1}),new ByteArrayInputStreamWithEquals(new byte[]{-1}))
			 										, new ConversionPairTest(InputStream.class,Reader.class,new ByteArrayInputStreamWithEquals(new byte[]{0}),new ByteArrayInputStreamWithEquals(new byte[]{1}),new ByteArrayInputStreamWithEquals(new byte[]{-1}))
													, new ConversionPairTest(InputStream.class,OutputStream.class,ContentException.class,new ByteArrayInputStreamWithEquals(new byte[0]))
													, new ConversionPairTest(InputStream.class,Writer.class,ContentException.class,new ByteArrayInputStreamWithEquals(new byte[0]))
													, new ConversionPairTest(InputStream.class,Object.class,new ByteArrayInputStreamWithEquals(new byte[]{0}),new ByteArrayInputStreamWithEquals(new byte[]{1}),new ByteArrayInputStreamWithEquals(new byte[]{-1}))
													, new ConversionPairTest(InputStream.class,Array.class,ContentException.class,new ByteArrayInputStreamWithEquals(new byte[0]))
													, new ConversionPairTest(InputStream.class,Struct.class,ContentException.class,new ByteArrayInputStreamWithEquals(new byte[0]))
													, new ConversionPairTest(InputStream.class,RowId.class,ContentException.class,new ByteArrayInputStreamWithEquals(new byte[0]))
													, new ConversionPairTest(InputStream.class,SQLXML.class,ContentException.class,new ByteArrayInputStreamWithEquals(new byte[0]))
													// -----------------------
													, new ConversionPairTest(Reader.class,Boolean.class,new StringReaderWithEquals("false"),new StringReaderWithEquals("true"))
													, new ConversionPairTest(Reader.class,Byte.class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
													, new ConversionPairTest(Reader.class,Short.class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
													, new ConversionPairTest(Reader.class,Integer.class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
													, new ConversionPairTest(Reader.class,Long.class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
													, new ConversionPairTest(Reader.class,Float.class,new StringReaderWithEquals("0.0"),new StringReaderWithEquals("1.0"),new StringReaderWithEquals("-1.0"))
													, new ConversionPairTest(Reader.class,Double.class,new StringReaderWithEquals("0.0"),new StringReaderWithEquals("1.0"),new StringReaderWithEquals("-1.0"))
													, new ConversionPairTest(Reader.class,Blob.class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
													, new ConversionPairTest(Reader.class,Clob.class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
													, new ConversionPairTest(Reader.class,NClob.class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
													, new ConversionPairTest(Reader.class,byte[].class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
													, new ConversionPairTest(Reader.class,String.class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
													, new ConversionPairTest(Reader.class,InputStream.class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
			 										, new ConversionPairTest(Reader.class,Reader.class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
													, new ConversionPairTest(Reader.class,OutputStream.class,ContentException.class,new StringReaderWithEquals(""))
													, new ConversionPairTest(Reader.class,Writer.class,ContentException.class,new StringReaderWithEquals(""))
													, new ConversionPairTest(Reader.class,Object.class,new StringReaderWithEquals("0"),new StringReaderWithEquals("1"),new StringReaderWithEquals("-1"))
													, new ConversionPairTest(Reader.class,Array.class,ContentException.class,new StringReaderWithEquals(""))
													, new ConversionPairTest(Reader.class,Struct.class,ContentException.class,new StringReaderWithEquals(""))
													, new ConversionPairTest(Reader.class,RowId.class,ContentException.class,new StringReaderWithEquals(""))
													, new ConversionPairTest(Reader.class,SQLXML.class,ContentException.class,new StringReaderWithEquals(""))
												};
	
	@Test
	public void prepareMetadataTest() throws SyntaxException {
		final RsMetaDataElement[]	metadata = SQLUtils.prepareMetadata("CHAR:VARCHAR(100)","NUMBER:NUMERIC(15,2)","DATE:DATE"); 

		Assert.assertEquals(metadata.length,3);
		
		Assert.assertEquals(metadata[0].getName(),"CHAR");
		Assert.assertEquals(metadata[0].getDescription(),"CHAR");
		Assert.assertEquals(metadata[0].getTypeName(),"VARCHAR");
		Assert.assertEquals(metadata[0].getType(),Types.VARCHAR);
		Assert.assertEquals(metadata[0].getLength(),100);
		Assert.assertEquals(metadata[0].getFrac(),0);

		Assert.assertEquals(metadata[1].getName(),"NUMBER");
		Assert.assertEquals(metadata[1].getDescription(),"NUMBER");
		Assert.assertEquals(metadata[1].getTypeName(),"NUMERIC");
		Assert.assertEquals(metadata[1].getType(),Types.NUMERIC);
		Assert.assertEquals(metadata[1].getLength(),15);
		Assert.assertEquals(metadata[1].getFrac(),2);

		Assert.assertEquals(metadata[2].getName(),"DATE");
		Assert.assertEquals(metadata[2].getDescription(),"DATE");
		Assert.assertEquals(metadata[2].getTypeName(),"DATE");
		Assert.assertEquals(metadata[2].getType(),Types.DATE);
		Assert.assertEquals(metadata[2].getLength(),0);
		Assert.assertEquals(metadata[2].getFrac(),0);
		
		try{SQLUtils.prepareMetadata((String[])null); 
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata(); 
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata(null,"X:Y"); 
			Assert.fail("Mandatory exception was not detected (null argument in the list)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata("","X:Y"); 
			Assert.fail("Mandatory exception was not detected (empty argument in the list)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{SQLUtils.prepareMetadata(":TYPE"); 
			Assert.fail("Mandatory exception was not detected (name missing)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata("NAME"); 
			Assert.fail("Mandatory exception was not detected (name without type)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata("NAME:"); 
			Assert.fail("Mandatory exception was not detected (name without type)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata("NAME:UNKNOWN"); 
			Assert.fail("Mandatory exception was not detected (unknown type)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata("NAME:UNKNOWN(10)"); 
			Assert.fail("Mandatory exception was not detected (unknown type)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata("NAME:VARCHAR("); 
			Assert.fail("Mandatory exception was not detected (missing length)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata("NAME:VARCHAR(10"); 
			Assert.fail("Mandatory exception was not detected (missing close bracket)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata("NAME:VARCHAR(10,"); 
			Assert.fail("Mandatory exception was not detected (missing fractional)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata("NAME:VARCHAR(10,2"); 
			Assert.fail("Mandatory exception was not detected (missing close bracket)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.prepareMetadata("NAME:VARCHAR(10,20)"); 
			Assert.fail("Mandatory exception was not detected (fractional gtreater than length)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void typeConversionTest() {
		Assert.assertEquals(SQLUtils.typeIdByTypeName(SQLUtils.typeNameByTypeId(Types.VARCHAR)),Types.VARCHAR);
		Assert.assertEquals(SQLUtils.typeIdByTypeName("UNKNOWN"),SQLUtils.UNKNOWN_TYPE);
		Assert.assertNull(SQLUtils.typeNameByTypeId(SQLUtils.UNKNOWN_TYPE));

		try{SQLUtils.typeIdByTypeName(null); 
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SQLUtils.typeIdByTypeName(""); 
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void dataConversionTest() {
		int index = 0;
		
		for (ConversionPairTest item : PAIRS) {
			System.err.println("Test["+index+"]: "+item);
			for (Object value : item.values) {
//				if (index == 277) {
//					int x = 0;
//				}
				try{if (Comparable.class.isAssignableFrom(item.source)) {
						Assert.assertTrue(((Comparable<Object>)SQLUtils.convert(item.source,SQLUtils.convert(item.target,value))).compareTo(value) == 0);
					}
					else if (item.source.isArray() && item.source.getComponentType() == byte.class) {
						Assert.assertArrayEquals((byte[])SQLUtils.convert(item.source,SQLUtils.convert(item.target,value)),(byte[])value);
					}
					else {
						Assert.assertEquals(SQLUtils.convert(item.source,SQLUtils.convert(item.target,value)),value);
					}
					if (item.exception != null) {
						Assert.fail("Mandatory exception was not detected...");
					}
				} catch (ContentException e) {
//					e.printStackTrace();
					if (item.exception == null) {
						Assert.fail("Unwated exception for ["+item+"]: "+e.getMessage());
					}
				}
			}
			index++;
		}
	}

	private static byte[] toByteArray(final long content) {
		return new byte[]{(byte)(content >> 56),(byte)(content >> 48),(byte)(content >> 40),(byte)(content >> 32),(byte)(content >> 24),(byte)(content >> 16),(byte)(content >> 8),(byte)(content >> 0)};
	}
	
	private static byte[] toByteArray(final float value) {
		final int	content = Float.floatToIntBits(value);
		
		return new byte[]{(byte)(content >> 24),(byte)(content >> 16),(byte)(content >> 8),(byte)(content >> 0)};
	}

	private static byte[] toByteArray(final double value) {
		final long	content = Double.doubleToLongBits(value);
		
		return new byte[]{(byte)(content >> 56),(byte)(content >> 48),(byte)(content >> 40),(byte)(content >> 32),(byte)(content >> 24),(byte)(content >> 16),(byte)(content >> 8),(byte)(content >> 0)};
	}
	
	private static class ConversionPairTest {
		final Class<?>			source;
		final Class<?>			target;
		final Object[]			values;
		final Class<?>			exception;
		
		public <T> ConversionPairTest(final Class<T> source, final Class<?> target, final Class<? extends Exception> exception, final T... values) {
			this.source = source;
			this.target = target;
			this.values = values;
			this.exception = exception;
		}

		public <T> ConversionPairTest(final Class<T> source, final Class<?> target, final T... values) {
			this.source = source;
			this.target = target;
			this.values = values;
			this.exception = null;
		}

		@Override
		public String toString() {
			return "ConversionPairTest [source=" + source + ", target=" + target + ", values=" + Arrays.toString(values) + ", exception=" + exception + "]";
		}
	}
}
