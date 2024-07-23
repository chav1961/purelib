package chav1961.purelib.json;


import java.io.CharArrayReader;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.charsource.ArrayCharSource;
import chav1961.purelib.streams.chartarget.ArrayCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;
import chav1961.purelib.testing.TestingUtils;

public class JsonSerializerTest {
	final static PrintStream	ps = TestingUtils.err();
	
	@BeforeAll
	public static void prepare() {
		ps.println("before");
	}

//	@Tag("PerformanceTestCategory")
//	@Test 
	public void performanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		basicBytePerformanceTest();
		basicShortPerformanceTest();
		basicIntegerPerformanceTest();
		basicLongPerformanceTest();
		basicFloatPerformanceTest();
		basicDoublePerformanceTest();
		basicBooleanPerformanceTest();
		basicCharPerformanceTest();
		basicStringPerformanceTest() ;
		basicEnumPerformanceTest();
		basicBooleanArrayPerformanceTest();
		basicByteArrayPerformanceTest();
		basicCharArrayPerformanceTest();
		basicDoubleArrayPerformanceTest();
		basicFloatArrayPerformanceTest();
		basicIntArrayPerformanceTest();
		basicLongArrayPerformanceTest();
		basicShortArrayPerformanceTest();
		basicRefArrayPerformanceTest();
		basicAnyClassPerformanceTest();		
	}
	
	@Tag("OrdinalTestCategory")
	@Test
	public void basicByteTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]				buffer = new char[1024];
		final JsonSerializer<Byte>	ser = JsonSerializer.buildSerializer(byte.class);
		final Byte[]				result = new Byte[1];
		final StaxCollector			collector = new StaxCollector();
		JsonStaxPrinter				prn;

		Assert.assertEquals(ser.serialize((byte)0,buffer,0,true),1);
		Assert.assertEquals(ser.serialize((byte)100,buffer,0,true),3);
		Assert.assertEquals(ser.serialize((byte)-100,buffer,0,true),4);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize((byte)0,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),1);
		Assert.assertEquals(result[0].intValue(),0);

		Arrays.fill(buffer, ' ');		
		ser.serialize((byte)100,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),3);
		Assert.assertEquals(result[0].intValue(),100);

		Arrays.fill(buffer, ' ');		
		ser.serialize((byte)-100,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),4);
		Assert.assertEquals(result[0].intValue(),-100);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize((byte)0,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).intValue(),0);

		Arrays.fill(buffer, ' ');		
		ser.serialize((byte)100,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).intValue(),100);

		Arrays.fill(buffer, ' ');		
		ser.serialize((byte)-100,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).intValue(),-100);

		prn = collector.getPrinter();
		ser.serialize((byte)0,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).intValue(),0);

		prn = collector.getPrinter();
		ser.serialize((byte)100,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).intValue(),100);

		prn = collector.getPrinter();
		ser.serialize((byte)-100,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).intValue(),-100);
	}

	public void basicBytePerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]				buffer = new char[1024];
		final JsonSerializer<Byte>		ser = JsonSerializer.buildSerializer(byte.class);
		final Byte[]				result = new Byte[1];
		final CharacterTarget		target = new ArrayCharTarget(buffer,0);
		final CharacterSource		source = new ArrayCharSource(buffer,0);
		final StaxCollector			collector = new StaxCollector();
		final long					startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter				prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize((byte)0,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Byte.MIN_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Byte.MAX_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicBytePerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize((byte)0,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Byte.MIN_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Byte.MAX_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicBytePerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize((byte)0,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(Byte.MIN_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(Byte.MAX_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicBytePerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}
	
	@Tag("OrdinalTestCategory")
	@Test
	public void basicShortTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]				buffer = new char[1024];
		final JsonSerializer<Short>	ser = JsonSerializer.buildSerializer(short.class);
		final Short[]				result = new Short[1];
		final StaxCollector			collector = new StaxCollector();
		JsonStaxPrinter				prn;

		Assert.assertEquals(ser.serialize((short)0,buffer,0,true),1);
		Assert.assertEquals(ser.serialize((short)100,buffer,0,true),3);
		Assert.assertEquals(ser.serialize((short)-100,buffer,0,true),4);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize((short)0,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),1);
		Assert.assertEquals(result[0].intValue(),0);

		Arrays.fill(buffer, ' ');		
		ser.serialize((short)100,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),3);
		Assert.assertEquals(result[0].intValue(),100);

		Arrays.fill(buffer, ' ');		
		ser.serialize((short)-100,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),4);
		Assert.assertEquals(result[0].intValue(),-100);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize((short)0,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).intValue(),0);

		Arrays.fill(buffer, ' ');		
		ser.serialize((short)100,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).intValue(),100);

		Arrays.fill(buffer, ' ');		
		ser.serialize((short)-100,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).intValue(),-100);

		prn = collector.getPrinter();
		ser.serialize((short)0,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).intValue(),0);

		prn = collector.getPrinter();
		ser.serialize((short)100,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).intValue(),100);

		prn = collector.getPrinter();
		ser.serialize((short)-100,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).intValue(),-100);
	}

	public void basicShortPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]				buffer = new char[1024];
		final JsonSerializer<Short>		ser = JsonSerializer.buildSerializer(short.class);
		final Short[]				result = new Short[1];
		final CharacterTarget		target = new ArrayCharTarget(buffer,0);
		final CharacterSource		source = new ArrayCharSource(buffer,0);
		final StaxCollector			collector = new StaxCollector();
		final long					startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter				prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize((short)0,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Short.MIN_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Short.MAX_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicShortPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize((short)0,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Short.MIN_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Short.MAX_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicShortPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize((short)0,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(Short.MIN_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(Short.MAX_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicShortPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicIntegerTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]				buffer = new char[1024];
		final JsonSerializer<Integer>	ser = JsonSerializer.buildSerializer(int.class);
		final Integer[]				result = new Integer[1];
		final StaxCollector			collector = new StaxCollector();
		JsonStaxPrinter				prn;

		Assert.assertEquals(ser.serialize(0,buffer,0,true),1);
		Assert.assertEquals(ser.serialize(100,buffer,0,true),3);
		Assert.assertEquals(ser.serialize(-100,buffer,0,true),4);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(0,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),1);
		Assert.assertEquals(result[0].intValue(),0);

		Arrays.fill(buffer, ' ');		
		ser.serialize(100,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),3);
		Assert.assertEquals(result[0].intValue(),100);

		Arrays.fill(buffer, ' ');		
		ser.serialize(-100,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),4);
		Assert.assertEquals(result[0].intValue(),-100);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(0,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).intValue(),0);

		Arrays.fill(buffer, ' ');		
		ser.serialize(100,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).intValue(),100);

		Arrays.fill(buffer, ' ');		
		ser.serialize(-100,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).intValue(),-100);

		prn = collector.getPrinter();
		ser.serialize(0,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).intValue(),0);

		prn = collector.getPrinter();
		ser.serialize(100,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).intValue(),100);

		prn = collector.getPrinter();
		ser.serialize(-100,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).intValue(),-100);
	}

	public void basicIntegerPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]				buffer = new char[1024];
		final JsonSerializer<Integer>	ser = JsonSerializer.buildSerializer(int.class);
		final Integer[]				result = new Integer[1];
		final CharacterTarget		target = new ArrayCharTarget(buffer,0);
		final CharacterSource		source = new ArrayCharSource(buffer,0);
		final StaxCollector			collector = new StaxCollector();
		final long					startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter				prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(0,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Integer.MIN_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Integer.MAX_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicIntegerPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(0,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Integer.MIN_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Integer.MAX_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicIntegerPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(0,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(Integer.MIN_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(Integer.MAX_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicIntegerPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicLongTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]				buffer = new char[1024];
		final JsonSerializer<Long>	ser = JsonSerializer.buildSerializer(long.class);
		final Long[]				result = new Long[1];
		final StaxCollector			collector = new StaxCollector();
		JsonStaxPrinter				prn;

		Assert.assertEquals(ser.serialize(0L,buffer,0,true),1);
		Assert.assertEquals(ser.serialize(100L,buffer,0,true),3);
		Assert.assertEquals(ser.serialize(-100L,buffer,0,true),4);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(0L,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),1);
		Assert.assertEquals(result[0].longValue(),0L);

		Arrays.fill(buffer, ' ');		
		ser.serialize(100L,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),3);
		Assert.assertEquals(result[0].longValue(),100L);

		Arrays.fill(buffer, ' ');		
		ser.serialize(-100L,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),4);
		Assert.assertEquals(result[0].longValue(),-100L);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(0L,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).longValue(),0L);

		Arrays.fill(buffer, ' ');		
		ser.serialize(100L,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).longValue(),100L);

		Arrays.fill(buffer, ' ');		
		ser.serialize(-100L,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).longValue(),-100L);

		prn = collector.getPrinter();
		ser.serialize(0L,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).longValue(),0L);

		prn = collector.getPrinter();
		ser.serialize(100L,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).longValue(),100L);

		prn = collector.getPrinter();
		ser.serialize(-100L,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).longValue(),-100L);
	}

	public void basicLongPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]				buffer = new char[1024];
		final JsonSerializer<Long>		ser = JsonSerializer.buildSerializer(long.class);
		final Long[]				result = new Long[1];
		final CharacterTarget		target = new ArrayCharTarget(buffer,0);
		final CharacterSource		source = new ArrayCharSource(buffer,0);
		final StaxCollector			collector = new StaxCollector();
		final long					startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter				prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(0L,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Long.MIN_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Long.MAX_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicLongPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(0L,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Long.MIN_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Long.MAX_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicLongPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(0L,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(Long.MIN_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(Long.MAX_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicLongPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicFloatTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]				buffer = new char[1024];
		final JsonSerializer<Float>	ser = JsonSerializer.buildSerializer(float.class);
		final Float[]				result = new Float[1];
		final StaxCollector			collector = new StaxCollector();
		JsonStaxPrinter				prn;

		Assert.assertEquals(ser.serialize(0f,buffer,0,true),1);
		Assert.assertEquals(ser.serialize(100f,buffer,0,true),3);
		Assert.assertEquals(ser.serialize(-100f,buffer,0,true),4);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(0f,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),1);
		Assert.assertEquals(result[0].floatValue(),0f,0.0001);

		Arrays.fill(buffer, ' ');		
		ser.serialize(100f,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),3);
		Assert.assertEquals(result[0].floatValue(),100f,0.0001);

		Arrays.fill(buffer, ' ');		
		ser.serialize(-100f,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),4);
		Assert.assertEquals(result[0].floatValue(),-100f,0.0001);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(0f,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).floatValue(),0f,0.0001);

		Arrays.fill(buffer, ' ');		
		ser.serialize(100f,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).floatValue(),100f,0.0001);

		Arrays.fill(buffer, ' ');		
		ser.serialize(-100f,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).floatValue(),-100f,0.0001);

		prn = collector.getPrinter();
		ser.serialize(0f,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).floatValue(),0f,0.0001);

		prn = collector.getPrinter();
		ser.serialize(100f,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).floatValue(),100f,0.0001);

		prn = collector.getPrinter();
		ser.serialize(-100f,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).floatValue(),-100f,0.0001);
	}

	public void basicFloatPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]				buffer = new char[1024];
		final JsonSerializer<Float>	ser = JsonSerializer.buildSerializer(float.class);
		final Float[]				result = new Float[1];
		final CharacterTarget		target = new ArrayCharTarget(buffer,0);
		final CharacterSource		source = new ArrayCharSource(buffer,0);
		final StaxCollector			collector = new StaxCollector();
		final long					startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter				prn;

		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(0f,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Float.MIN_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Float.MAX_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicFloatPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(0f,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Float.MIN_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Float.MAX_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicFloatPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(0f,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(Float.MIN_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(Float.MAX_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicFloatPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicDoubleTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<Double>	ser = JsonSerializer.buildSerializer(double.class);
		final Double[]					result = new Double[1];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize(0.0,buffer,0,true),1);
		Assert.assertEquals(ser.serialize(100.0,buffer,0,true),3);
		Assert.assertEquals(ser.serialize(-100.0,buffer,0,true),4);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(0.0,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),1);
		Assert.assertEquals(result[0].doubleValue(),0.0,0.0001);

		Arrays.fill(buffer, ' ');		
		ser.serialize(100.0,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),3);
		Assert.assertEquals(result[0].doubleValue(),100.0,0.0001);

		Arrays.fill(buffer, ' ');		
		ser.serialize(-100.0,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),4);
		Assert.assertEquals(result[0].doubleValue(),-100.0,0.0001);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(0.0,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).doubleValue(),0.0,0.0001);

		Arrays.fill(buffer, ' ');		
		ser.serialize(100.0,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).doubleValue(),100.0,0.0001);

		Arrays.fill(buffer, ' ');		
		ser.serialize(-100.0,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).doubleValue(),-100.0,0.0001);

		prn = collector.getPrinter();
		ser.serialize(0.0,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).doubleValue(),0.0,0.0001);

		prn = collector.getPrinter();
		ser.serialize(100.0,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).doubleValue(),100.0,0.0001);

		prn = collector.getPrinter();
		ser.serialize(-100.0,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).doubleValue(),-100.0,0.0001);
	}

	public void basicDoublePerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<Double>	ser = JsonSerializer.buildSerializer(double.class);
		final Double[]					result = new Double[1];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final StaxCollector				collector = new StaxCollector();
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(0.0,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Double.MIN_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(Double.MAX_VALUE,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicDoublePerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(0.0,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Double.MIN_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(Double.MAX_VALUE,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicDoublePerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(0.0,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(Double.MIN_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(0.99*Double.MAX_VALUE,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicDoublePerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicBooleanTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<Boolean>	ser = JsonSerializer.buildSerializer(boolean.class);
		final Boolean[]					result = new Boolean[1];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize(true,buffer,0,true),4);
		Assert.assertEquals(ser.serialize(false,buffer,0,true),5);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(true,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),4);
		Assert.assertTrue(result[0]);

		Arrays.fill(buffer, ' ');		
		ser.serialize(false,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),5);
		Assert.assertFalse(result[0]);

		Arrays.fill(buffer, ' ');		
		ser.serialize(true,new ArrayCharTarget(buffer,0));
		Assert.assertTrue(ser.deserialize(new ArrayCharSource(buffer)).booleanValue());

		Arrays.fill(buffer, ' ');		
		ser.serialize(false,new ArrayCharTarget(buffer,0));
		Assert.assertFalse(ser.deserialize(new ArrayCharSource(buffer)).booleanValue());

		prn = collector.getPrinter();
		ser.serialize(true,prn);
		prn.flush();
		Assert.assertTrue(ser.deserialize(collector.getParser()).booleanValue());

		prn = collector.getPrinter();
		ser.serialize(false,prn);
		prn.flush();
		Assert.assertFalse(ser.deserialize(collector.getParser()).booleanValue());
	}

	public void basicBooleanPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<Boolean>	ser = JsonSerializer.buildSerializer(boolean.class);
		final Boolean[]					result = new Boolean[1];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final StaxCollector				collector = new StaxCollector();
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(true,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize(false,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicBooleanPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(true,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize(false,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

		}
		ps.println("basicBooleanPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(true,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize(false,prn);
			prn.flush();
			ser.deserialize(collector.getParser());

		}
		ps.println("basicBooleanPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicCharTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<Character>	ser = JsonSerializer.buildSerializer(char.class);
		final Character[]				result = new Character[1];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize('?',buffer,0,true),3);
		Assert.assertEquals(ser.serialize('\u2040',buffer,0,true),8);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize('?',buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),3);
		Assert.assertEquals(result[0].charValue(),'?');

		Arrays.fill(buffer, ' ');		
		ser.serialize('\u2040',buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),8);
		Assert.assertEquals(result[0].charValue(),'\u2040');

		Arrays.fill(buffer, ' ');		
		ser.serialize('?',new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).charValue(),'?');

		Arrays.fill(buffer, ' ');		
		ser.serialize('\u2040',new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)).charValue(),'\u2040');

		prn = collector.getPrinter();
		ser.serialize('?',prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).charValue(),'?');

		prn = collector.getPrinter();
		ser.serialize('\u2040',prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()).charValue(),'\u2040');
	}

	public void basicCharPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<Character>	ser = JsonSerializer.buildSerializer(char.class);
		final Character[]				result = new Character[1];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final StaxCollector				collector = new StaxCollector();
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize('?',buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize('\u2040',buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicCharPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize('?',target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize('\u2040',target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

		}
		ps.println("basicCharPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize('?',prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize('\u2040',prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicCharPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicStringTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<String>	ser = JsonSerializer.buildSerializer(String.class);
		final String[]					result = new String[1];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize("1234567890",buffer,0,true),12);
		Assert.assertEquals(ser.serialize("1234567890\u2040",buffer,0,true),18);
		Assert.assertEquals(ser.serialize("",buffer,0,true),2);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize("1234567890",buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),12);
		Assert.assertEquals(result[0],"1234567890");

		Arrays.fill(buffer, ' ');		
		ser.serialize("1234567890\u2040",buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),18);
		Assert.assertEquals(result[0],"1234567890\u2040");

		Arrays.fill(buffer, ' ');		
		ser.serialize("",buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),2);
		Assert.assertEquals(result[0],"");
		
		
		Arrays.fill(buffer, ' ');		
		ser.serialize("1234567890",new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)),"1234567890");

		Arrays.fill(buffer, ' ');		
		ser.serialize("1234567890\u2040",new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)),"1234567890\u2040");

		Arrays.fill(buffer, ' ');		
		ser.serialize("",new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)),"");

		
		prn = collector.getPrinter();
		ser.serialize("1234567890",prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()),"1234567890");

		prn = collector.getPrinter();
		ser.serialize("1234567890\u2040",prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()),"1234567890\u2040");

		prn = collector.getPrinter();
		ser.serialize("",prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()),"");
	}

	public void basicStringPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<String>	ser = JsonSerializer.buildSerializer(String.class);
		final String[]					result = new String[1];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final StaxCollector				collector = new StaxCollector();
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize("1234567890",buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);

			buffer[ser.serialize("1234567890\u2040",buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicStringPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize("1234567890",target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

			target.reset();
			ser.serialize("1234567890\u2040",target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);

		}
		ps.println("basicStringPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize("1234567890",prn);
			prn.flush();
			ser.deserialize(collector.getParser());

			prn = collector.getPrinter();
			ser.serialize("1234567890\u2040",prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicStringPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicEnumTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]								buffer = new char[1024];
		final JsonSerializer<MarkupOutputFormat>	ser = JsonSerializer.buildSerializer(MarkupOutputFormat.class);
		final MarkupOutputFormat[]					result = new MarkupOutputFormat[1];
		final StaxCollector							collector = new StaxCollector();
		JsonStaxPrinter								prn;

		Assert.assertEquals(ser.serialize(MarkupOutputFormat.TEXT,buffer,0,true),6);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(MarkupOutputFormat.TEXT,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),6);
		Assert.assertEquals(result[0],MarkupOutputFormat.TEXT);

		Arrays.fill(buffer, ' ');		
		ser.serialize(MarkupOutputFormat.TEXT,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)),MarkupOutputFormat.TEXT);

		prn = collector.getPrinter();
		ser.serialize(MarkupOutputFormat.TEXT,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()),MarkupOutputFormat.TEXT);
	}

	public void basicEnumPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]								buffer = new char[1024];
		final JsonSerializer<MarkupOutputFormat>	ser = JsonSerializer.buildSerializer(MarkupOutputFormat.class);
		final MarkupOutputFormat[]					result = new MarkupOutputFormat[1];
		final CharacterTarget						target = new ArrayCharTarget(buffer,0);
		final CharacterSource						source = new ArrayCharSource(buffer,0);
		final StaxCollector							collector = new StaxCollector();
		final long									startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter								prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(MarkupOutputFormat.TEXT,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicEnumPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(MarkupOutputFormat.TEXT,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicEnumPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(MarkupOutputFormat.TEXT,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicEnumPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicBooleanArrayTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<boolean[]>	ser = JsonSerializer.buildSerializer(boolean[].class);
		final boolean[][]				result = new boolean[1][];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize(new boolean[]{true,false,false,true},buffer,0,true),23);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(new boolean[]{true,false,false,true},buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),23);
		Assert.assertArrayEquals(result[0],new boolean[]{true,false,false,true});

		Arrays.fill(buffer, ' ');		
		ser.serialize(new boolean[]{true,false,false,true},new ArrayCharTarget(buffer,0));
		Assert.assertArrayEquals(ser.deserialize(new ArrayCharSource(buffer)),new boolean[]{true,false,false,true});

		prn = collector.getPrinter();
		ser.serialize(new boolean[]{true,false,false,true},prn);
		prn.flush();
		Assert.assertArrayEquals(ser.deserialize(collector.getParser()),new boolean[]{true,false,false,true});
	}

	public void basicBooleanArrayPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<boolean[]>	ser = JsonSerializer.buildSerializer(boolean[].class);
		final boolean[][]				result = new boolean[1][];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final StaxCollector				collector = new StaxCollector();
		final boolean[]					data = new boolean[]{true,false,false,true};
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(data,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicBooleanArrayPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(data,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicBooleanArrayPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(data,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicBooleanArrayPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicByteArrayTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<byte[]>	ser = JsonSerializer.buildSerializer(byte[].class);
		final byte[][]					result = new byte[1][];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize(new byte[]{Byte.MIN_VALUE, -100, 0, 100, Byte.MAX_VALUE},buffer,0,true),21);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(new byte[]{Byte.MIN_VALUE, -100, 0, 100, Byte.MAX_VALUE},buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),21);
		Assert.assertArrayEquals(result[0],new byte[]{Byte.MIN_VALUE, -100, 0, 100, Byte.MAX_VALUE});

		Arrays.fill(buffer, ' ');		
		ser.serialize(new byte[]{Byte.MIN_VALUE, -100, 0, 100, Byte.MAX_VALUE},new ArrayCharTarget(buffer,0));
		Assert.assertArrayEquals(ser.deserialize(new ArrayCharSource(buffer)),new byte[]{Byte.MIN_VALUE, -100, 0, 100, Byte.MAX_VALUE});

		prn = collector.getPrinter();
		ser.serialize(new byte[]{Byte.MIN_VALUE, -100, 0, 100, Byte.MAX_VALUE},prn);
		prn.flush();
		Assert.assertArrayEquals(ser.deserialize(collector.getParser()),new byte[]{Byte.MIN_VALUE, -100, 0, 100, Byte.MAX_VALUE});
	}

	public void basicByteArrayPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<byte[]>	ser = JsonSerializer.buildSerializer(byte[].class);
		final byte[][]					result = new byte[1][];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final byte[]					data = new byte[]{Byte.MIN_VALUE, -100, 0, 100, Byte.MAX_VALUE};
		final StaxCollector				collector = new StaxCollector();
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(data,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicByteArrayPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(data,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicByteArrayPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(data,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicByteArrayPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicCharArrayTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<char[]>	ser = JsonSerializer.buildSerializer(char[].class);
		final char[][]					result = new char[1][];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize(new char[]{Character.MIN_VALUE, ' ', '\u2040', Character.MAX_VALUE},buffer,0,true),30);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(new char[]{Character.MIN_VALUE, ' ', '\u2040', Character.MAX_VALUE},buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),30);
		Assert.assertArrayEquals(result[0],new char[]{Character.MIN_VALUE, ' ', '\u2040', Character.MAX_VALUE});

		Arrays.fill(buffer, ' ');		
		ser.serialize(new char[]{Character.MIN_VALUE, ' ', '\u2040', Character.MAX_VALUE},new ArrayCharTarget(buffer,0));
		Assert.assertArrayEquals(ser.deserialize(new ArrayCharSource(buffer)),new char[]{Character.MIN_VALUE, ' ', '\u2040', Character.MAX_VALUE});

		prn = collector.getPrinter();
		ser.serialize(new char[]{Character.MIN_VALUE, ' ', '\u2040', Character.MAX_VALUE},prn);
		prn.flush();
		Assert.assertArrayEquals(ser.deserialize(collector.getParser()),new char[]{Character.MIN_VALUE, ' ', '\u2040', Character.MAX_VALUE});
	}

	public void basicCharArrayPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<char[]>	ser = JsonSerializer.buildSerializer(char[].class);
		final char[][]					result = new char[1][];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final StaxCollector				collector = new StaxCollector();
		final char[]					data = new char[]{Character.MIN_VALUE, ' ', '\u2040', Character.MAX_VALUE};
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(data,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicCharArrayPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(data,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicCharArrayPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(data,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicCharArrayPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicDoubleArrayTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<double[]>	ser = JsonSerializer.buildSerializer(double[].class);
		final double[][]				result = new double[1][];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize(new double[]{Double.MIN_VALUE, -100, 0, 100, 0.99*Double.MAX_VALUE},buffer,0,true),62);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(new double[]{Double.MIN_VALUE, -100, 0, 100, 0.99*Double.MAX_VALUE},buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),62);
		Assert.assertArrayEquals(result[0],new double[]{Double.MIN_VALUE, -100, 0, 100, 0.99*Double.MAX_VALUE},0.001*Double.MAX_VALUE);

		Arrays.fill(buffer, ' ');		
		ser.serialize(new double[]{Double.MIN_VALUE, -100, 0, 100, 0.99*Double.MAX_VALUE},new ArrayCharTarget(buffer,0));
		Assert.assertArrayEquals(ser.deserialize(new ArrayCharSource(buffer)),new double[]{Double.MIN_VALUE, -100, 0, 100, 0.99*Double.MAX_VALUE},0.001*Double.MAX_VALUE);

		prn = collector.getPrinter();
		ser.serialize(new double[]{Double.MIN_VALUE, -100, 0, 100, 0.99*Double.MAX_VALUE},prn);
		prn.flush();
		Assert.assertArrayEquals(ser.deserialize(collector.getParser()),new double[]{Double.MIN_VALUE, -100, 0, 100, 0.99*Double.MAX_VALUE},0.001*Double.MAX_VALUE);
	}

	public void basicDoubleArrayPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<double[]>	ser = JsonSerializer.buildSerializer(double[].class);
		final double[][]				result = new double[1][];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		final double[]					data = new double[]{Double.MIN_VALUE, -100, 0, 100, Double.MAX_VALUE};
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(data,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicDoubleArrayPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(data,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicDoubleArrayPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(data,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicDoubleArrayPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicFloatArrayTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<float[]>	ser = JsonSerializer.buildSerializer(float[].class);
		final float[][]					result = new float[1][];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize(new float[]{Float.MIN_VALUE, -100, 0, 100, Float.MAX_VALUE},buffer,0,true),61);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(new float[]{Float.MIN_VALUE, -100, 0, 100, Float.MAX_VALUE},buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),61);
		Assert.assertArrayEquals(result[0],new float[]{Float.MIN_VALUE, -100, 0, 100, Float.MAX_VALUE},0.001f);

		Arrays.fill(buffer, ' ');		
		ser.serialize(new float[]{Float.MIN_VALUE, -100, 0, 100, Float.MAX_VALUE},new ArrayCharTarget(buffer,0));
		Assert.assertArrayEquals(ser.deserialize(new ArrayCharSource(buffer)),new float[]{Float.MIN_VALUE, -100, 0, 100, Float.MAX_VALUE},0.001f);

		prn = collector.getPrinter();
		ser.serialize(new float[]{Float.MIN_VALUE, -100, 0, 100, Float.MAX_VALUE},prn);
		prn.flush();
		Assert.assertArrayEquals(ser.deserialize(collector.getParser()),new float[]{Float.MIN_VALUE, -100, 0, 100, Float.MAX_VALUE},0.001f);
	}

	public void basicFloatArrayPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<float[]>	ser = JsonSerializer.buildSerializer(float[].class);
		final float[][]					result = new float[1][];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		final float[]					data = new float[]{Float.MIN_VALUE, -100, 0, 100, Float.MAX_VALUE};
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(data,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicFloatArrayPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(data,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicFloatArrayPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(data,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicFloatArrayPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicIntArrayTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<int[]>		ser = JsonSerializer.buildSerializer(int[].class);
		final int[][]					result = new int[1][];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize(new int[]{Integer.MIN_VALUE, -100, 0, 100, Integer.MAX_VALUE},buffer,0,true),35);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(new int[]{Integer.MIN_VALUE, -100, 0, 100, Integer.MAX_VALUE},buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),35);
		Assert.assertArrayEquals(result[0],new int[]{Integer.MIN_VALUE, -100, 0, 100, Integer.MAX_VALUE});

		Arrays.fill(buffer, ' ');		
		ser.serialize(new int[]{Integer.MIN_VALUE, -100, 0, 100, Integer.MAX_VALUE},new ArrayCharTarget(buffer,0));
		Assert.assertArrayEquals(ser.deserialize(new ArrayCharSource(buffer)),new int[]{Integer.MIN_VALUE, -100, 0, 100, Integer.MAX_VALUE});

		prn = collector.getPrinter();
		ser.serialize(new int[]{Integer.MIN_VALUE, -100, 0, 100, Integer.MAX_VALUE},prn);
		prn.flush();
		Assert.assertArrayEquals(ser.deserialize(collector.getParser()),new int[]{Integer.MIN_VALUE, -100, 0, 100, Integer.MAX_VALUE});
	}

	public void basicIntArrayPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<int[]>		ser = JsonSerializer.buildSerializer(int[].class);
		final int[][]					result = new int[1][];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		final int[]						data = new int[]{Integer.MIN_VALUE, -100, 0, 100, Integer.MAX_VALUE};
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(data,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicIntArrayPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(data,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicIntArrayPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(data,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicIntArrayPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicLongArrayTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<long[]>	ser = JsonSerializer.buildSerializer(long[].class);
		final long[][]					result = new long[1][];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize(new long[]{Long.MIN_VALUE, -100, 0, 100, Long.MAX_VALUE},buffer,0,true),53);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(new long[]{Long.MIN_VALUE, -100, 0, 100, Long.MAX_VALUE},buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),53);
		Assert.assertArrayEquals(result[0],new long[]{Long.MIN_VALUE, -100, 0, 100, Long.MAX_VALUE});

		Arrays.fill(buffer, ' ');		
		ser.serialize(new long[]{Long.MIN_VALUE, -100, 0, 100, Long.MAX_VALUE},new ArrayCharTarget(buffer,0));
		Assert.assertArrayEquals(ser.deserialize(new ArrayCharSource(buffer)),new long[]{Long.MIN_VALUE, -100, 0, 100, Long.MAX_VALUE});

		prn = collector.getPrinter();
		ser.serialize(new long[]{Long.MIN_VALUE, -100, 0, 100, Long.MAX_VALUE},prn);
		prn.flush();
		Assert.assertArrayEquals(ser.deserialize(collector.getParser()),new long[]{Long.MIN_VALUE, -100, 0, 100, Long.MAX_VALUE});
	}

	public void basicLongArrayPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<long[]>	ser = JsonSerializer.buildSerializer(long[].class);
		final long[][]					result = new long[1][];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		final long[]					data = new long[]{Long.MIN_VALUE, -100, 0, 100, Long.MAX_VALUE};
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(data,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicLongArrayPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(data,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicLongArrayPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(data,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicLongArrayPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicShortArrayTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<short[]>	ser = JsonSerializer.buildSerializer(short[].class);
		final short[][]					result = new short[1][];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize(new short[]{Short.MIN_VALUE, -100, 0, 100, Short.MAX_VALUE},buffer,0,true),25);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(new short[]{Short.MIN_VALUE, -100, 0, 100, Short.MAX_VALUE},buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),25);
		Assert.assertArrayEquals(result[0],new short[]{Short.MIN_VALUE, -100, 0, 100, Short.MAX_VALUE});

		Arrays.fill(buffer, ' ');		
		ser.serialize(new short[]{Short.MIN_VALUE, -100, 0, 100, Short.MAX_VALUE},new ArrayCharTarget(buffer,0));
		Assert.assertArrayEquals(ser.deserialize(new ArrayCharSource(buffer)),new short[]{Short.MIN_VALUE, -100, 0, 100, Short.MAX_VALUE});

		prn = collector.getPrinter();
		ser.serialize(new short[]{Short.MIN_VALUE, -100, 0, 100, Short.MAX_VALUE},prn);
		prn.flush();
		Assert.assertArrayEquals(ser.deserialize(collector.getParser()),new short[]{Short.MIN_VALUE, -100, 0, 100, Short.MAX_VALUE});
	}

	public void basicShortArrayPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<short[]>	ser = JsonSerializer.buildSerializer(short[].class);
		final short[][]					result = new short[1][];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		final short[]					data = new short[]{Short.MIN_VALUE, -100, 0, 100, Short.MAX_VALUE};
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(data,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicShortArrayPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(data,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicShortArrayPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(data,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicShortArrayPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}
	
	@Tag("OrdinalTestCategory")
	@Test
	public void basicRefArrayTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]					buffer = new char[1024];
		final JsonSerializer<String[]>	ser = JsonSerializer.buildSerializer(String[].class);
		final String[][]				result = new String[1][];
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;

		Assert.assertEquals(ser.serialize(new String[]{"", "abcde", "abcde\u2040"},buffer,0,true),26);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(new String[]{"", "abcde", "abcde\u2040"},buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),26);
		Assert.assertArrayEquals(result[0],new String[]{"", "abcde", "abcde\u2040"});

		Arrays.fill(buffer, ' ');		
		ser.serialize(new String[]{"", "abcde", "abcde\u2040"},new ArrayCharTarget(buffer,0));
		Assert.assertArrayEquals(ser.deserialize(new ArrayCharSource(buffer)),new String[]{"", "abcde", "abcde\u2040"});

		prn = collector.getPrinter();
		ser.serialize(new String[]{"", "abcde", "abcde\u2040"},prn);
		prn.flush();
		Assert.assertArrayEquals(ser.deserialize(collector.getParser()),new String[]{"", "abcde", "abcde\u2040"});
	}

	public void basicRefArrayPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]					buffer = new char[1024];
		final JsonSerializer<String[]>	ser = JsonSerializer.buildSerializer(String[].class);
		final String[][]				result = new String[1][];
		final CharacterTarget			target = new ArrayCharTarget(buffer,0);
		final CharacterSource			source = new ArrayCharSource(buffer,0);
		final long						startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		final String[]					data = new String[]{"", "abcde", "abcde\u2040"};
		final StaxCollector				collector = new StaxCollector();
		JsonStaxPrinter					prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(data,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicRefArrayPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(data,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicRefArrayPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(data,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicRefArrayPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}

	@Tag("OrdinalTestCategory")
	@Test
	public void basicAnyClassTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		final char[]									buffer = new char[1024];
		final JsonSerializer<SimpleParentSerializable>	ser = JsonSerializer.buildSerializer(SimpleParentSerializable.class);
		final SimpleParentSerializable[]				result = new SimpleParentSerializable[1];
		final SimpleParentSerializable					source = new SimpleParentSerializable(
															false,new boolean[]{true,false}
															,(byte)100, new byte[]{-100,0,100}
															,'Z',"123".toCharArray()
															,100.0,new double[]{-100.0,0.0,100.0}
															,100.0f,new float[]{-100.0f,0.0f,100.0f}
															,100,new int[]{-100,0,100}
															,100L,new long[]{-100L,0,100L}
															,(short)100,new short[]{(short)-100,0,(short)100}
															,"test",new String[]{"shaize"}
														);
		final StaxCollector								collector = new StaxCollector();
		JsonStaxPrinter									prn;
		
		Assert.assertEquals(ser.serialize(source,buffer,0,true),435);
		
		Arrays.fill(buffer, ' ');		
		ser.serialize(source,buffer,0,true);
		Assert.assertEquals(ser.deserialize(buffer,0,result),435);
		Assert.assertEquals(result[0],source);

		Arrays.fill(buffer, ' ');		
		ser.serialize(source,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(ser.deserialize(new ArrayCharSource(buffer)),source);
		
		
		prn = collector.getPrinter();
		ser.serialize(source,prn);
		prn.flush();
		Assert.assertEquals(ser.deserialize(collector.getParser()),source);

		final JsonSerializer<SimpleChildSerializable>	serChild = JsonSerializer.buildSerializer(SimpleChildSerializable.class);
		final SimpleChildSerializable[]					resultChild = new SimpleChildSerializable[1];
		final SimpleChildSerializable					sourceChild = new SimpleChildSerializable(
															false,new boolean[]{true,false}
															,(byte)100, new byte[]{-100,0,100}
															,'Z',"123".toCharArray()
															,100.0,new double[]{-100.0,0.0,100.0}
															,100.0f,new float[]{-100.0f,0.0f,100.0f}
															,100,new int[]{-100,0,100}
															,100L,new long[]{-100L,0,100L}
															,(short)100,new short[]{(short)-100,0,(short)100}
															,"test",new String[]{"shaize"}
															,true, false, true, false
														);
		final SimpleChildSerializable					sourceChildRestored = new SimpleChildSerializable();
		
		sourceChildRestored.protectedBooleanField = true;
//		sourceChildRestored.privateBooleanField = false;
		sourceChildRestored.packageBooleanField = true;
		sourceChildRestored.transientBooleanField = false;
		
		Assert.assertEquals(serChild.serialize(sourceChild,buffer,0,true),85);
		
		Arrays.fill(buffer, ' ');		
		serChild.serialize(sourceChild,buffer,0,true);
		Assert.assertEquals(serChild.deserialize(buffer,0,resultChild),85);
		Assert.assertEquals(resultChild[0],sourceChildRestored);	// Only serialized part will be checked!

		Arrays.fill(buffer, ' ');		
		serChild.serialize(sourceChild,new ArrayCharTarget(buffer,0));
		Assert.assertEquals(serChild.deserialize(new ArrayCharSource(buffer)),sourceChildRestored);

		prn = collector.getPrinter();
		serChild.serialize(sourceChild,prn);
		prn.flush();
		Assert.assertEquals(serChild.deserialize(collector.getParser()),sourceChildRestored);
	}

	public void basicAnyClassPerformanceTest() throws EnvironmentException, SyntaxException, PrintingException, ContentException, IOException {
		System.gc();
		
		final char[]									buffer = new char[1024];
		final JsonSerializer<SimpleParentSerializable>	ser = JsonSerializer.buildSerializer(SimpleParentSerializable.class);
		final SimpleParentSerializable[]				result = new SimpleParentSerializable[1];
		final CharacterTarget							target = new ArrayCharTarget(buffer,0);
		final CharacterSource							source = new ArrayCharSource(buffer,0);
		final long										startArray = System.nanoTime(), memoryArray = Runtime.getRuntime().freeMemory();
		final SimpleParentSerializable					data = new SimpleParentSerializable(
															false,new boolean[]{true,false}
															,(byte)100, new byte[]{-100,0,100}
															,'Z',"123".toCharArray()
															,100.0,new double[]{-100.0,0.0,100.0}
															,100.0f,new float[]{-100.0f,0.0f,100.0f}
															,100,new int[]{-100,0,100}
															,100L,new long[]{-100L,0,100L}
															,(short)100,new short[]{(short)-100,0,(short)100}
															,"test",new String[]{"shaize"}
														);
		final StaxCollector								collector = new StaxCollector();
		JsonStaxPrinter									prn;
		
		for (int index = 0; index < 10_000_000; index++) {
			buffer[ser.serialize(data,buffer,0,true)] = ' ';
			ser.deserialize(buffer,0,result);
		}
		ps.println("basicAnyClassPerformanceTest on arrays: duration="+((System.nanoTime()-startArray)/1000000)+" msec, "+((memoryArray - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStream = System.nanoTime(), memoryTarget = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			target.reset();
			ser.serialize(data,target);
			target.put(' ');
			source.reset();
			ser.deserialize(source);
		}
		ps.println("basicAnyClassPerformanceTest on source/target: duration="+((System.nanoTime()-startStream)/1000000)+" msec, "+((memoryTarget - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");

		System.gc();
		
		final long					startStax = System.nanoTime(), memoryStax = Runtime.getRuntime().freeMemory();
		
		for (int index = 0; index < 10_000_000; index++) {
			prn = collector.getPrinter();
			ser.serialize(data,prn);
			prn.flush();
			ser.deserialize(collector.getParser());
		}
		ps.println("basicAnyClassPerformanceTest on stax: duration="+((System.nanoTime()-startStax)/1000000)+" msec, "+((memoryStax - Runtime.getRuntime().freeMemory())/(1<<20))+" Mb used");
	}
}

class SimpleParentSerializable {
	public boolean	booleanField, booleanArrayField[];
	public byte		byteField, byteArrayField[];
	public char		charField, charArrayField[];
	public double	doubleField, doubleArrayField[];
	public float	floatField, floatArrayField[];
	public int		intField, intArrayField[];
	public long		longField, longArrayField[];
	public short	shortField, shortArrayField[];
	public String	stringField, stringArrayField[];

	public SimpleParentSerializable(){
	}
	
	public SimpleParentSerializable(boolean booleanField, boolean[] booleanArrayField, byte byteField,
			byte[] byteArrayField, char charField, char[] charArrayField, double doubleField, double[] doubleArrayField,
			float floatField, float[] floatArrayField, int intField, int[] intArrayField, long longField,
			long[] longArrayField, short shortField, short[] shortArrayField, String stringField,
			String[] stringArrayField) {
		this.booleanField = booleanField;
		this.booleanArrayField = booleanArrayField;
		this.byteField = byteField;
		this.byteArrayField = byteArrayField;
		this.charField = charField;
		this.charArrayField = charArrayField;
		this.doubleField = doubleField;
		this.doubleArrayField = doubleArrayField;
		this.floatField = floatField;
		this.floatArrayField = floatArrayField;
		this.intField = intField;
		this.intArrayField = intArrayField;
		this.longField = longField;
		this.longArrayField = longArrayField;
		this.shortField = shortField;
		this.shortArrayField = shortArrayField;
		this.stringField = stringField;
		this.stringArrayField = stringArrayField;
	}

	@Override
	public String toString() {
		return "SimpleParentSerializable [booleanField=" + booleanField + ", booleanArrayField="
				+ Arrays.toString(booleanArrayField) + ", byteField=" + byteField + ", byteArrayField="
				+ Arrays.toString(byteArrayField) + ", charField=" + charField + ", charArrayField="
				+ Arrays.toString(charArrayField) + ", doubleField=" + doubleField + ", doubleArrayField="
				+ Arrays.toString(doubleArrayField) + ", floatField=" + floatField + ", floatArrayField="
				+ Arrays.toString(floatArrayField) + ", intField=" + intField + ", intArrayField="
				+ Arrays.toString(intArrayField) + ", longField=" + longField + ", longArrayField="
				+ Arrays.toString(longArrayField) + ", shortField=" + shortField + ", shortArrayField="
				+ Arrays.toString(shortArrayField) + ", stringField=" + stringField + ", stringArrayField="
				+ Arrays.toString(stringArrayField) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(booleanArrayField);
		result = prime * result + (booleanField ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(byteArrayField);
		result = prime * result + byteField;
		result = prime * result + Arrays.hashCode(charArrayField);
		result = prime * result + charField;
		result = prime * result + Arrays.hashCode(doubleArrayField);
		long temp;
		temp = Double.doubleToLongBits(doubleField);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(floatArrayField);
		result = prime * result + Float.floatToIntBits(floatField);
		result = prime * result + Arrays.hashCode(intArrayField);
		result = prime * result + intField;
		result = prime * result + Arrays.hashCode(longArrayField);
		result = prime * result + (int) (longField ^ (longField >>> 32));
		result = prime * result + Arrays.hashCode(shortArrayField);
		result = prime * result + shortField;
		result = prime * result + Arrays.hashCode(stringArrayField);
		result = prime * result + ((stringField == null) ? 0 : stringField.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SimpleParentSerializable other = (SimpleParentSerializable) obj;
		if (!Arrays.equals(booleanArrayField, other.booleanArrayField)) return false;
		if (booleanField != other.booleanField) return false;
		if (!Arrays.equals(byteArrayField, other.byteArrayField)) return false;
		if (byteField != other.byteField) return false;
		if (!Arrays.equals(charArrayField, other.charArrayField)) return false;
		if (charField != other.charField) return false;
		if (!Arrays.equals(doubleArrayField, other.doubleArrayField)) return false;
		if (Double.doubleToLongBits(doubleField) != Double.doubleToLongBits(other.doubleField)) return false;
		if (!Arrays.equals(floatArrayField, other.floatArrayField)) return false;
		if (Float.floatToIntBits(floatField) != Float.floatToIntBits(other.floatField)) return false;
		if (!Arrays.equals(intArrayField, other.intArrayField)) return false;
		if (intField != other.intField) return false;
		if (!Arrays.equals(longArrayField, other.longArrayField)) return false;
		if (longField != other.longField) return false;
		if (!Arrays.equals(shortArrayField, other.shortArrayField)) return false;
		if (shortField != other.shortField) return false;
		if (!Arrays.equals(stringArrayField, other.stringArrayField)) return false;
		if (stringField == null) { 
			if (other.stringField != null) return false;
		} else if (!stringField.equals(other.stringField)) return false;
		return true;
	}
}

class SimpleChildSerializable extends SimpleParentSerializable implements Serializable {
	private static final long 	serialVersionUID = 1L;
	protected boolean			protectedBooleanField;
	private	boolean				privateBooleanField;
	boolean						packageBooleanField;
	public static boolean		publicStaticBooleanField;
	public transient boolean	transientBooleanField;
	
	public SimpleChildSerializable() {
		super();
	}
	
	public SimpleChildSerializable(boolean booleanField, boolean[] booleanArrayField, byte byteField,
			byte[] byteArrayField, char charField, char[] charArrayField, double doubleField, double[] doubleArrayField,
			float floatField, float[] floatArrayField, int intField, int[] intArrayField, long longField,
			long[] longArrayField, short shortField, short[] shortArrayField, String stringField,
			String[] stringArrayField,
			boolean protectedBooleanField, boolean privateBooleanField,
			boolean packageBooleanField, boolean transientBooleanField) {
		super(booleanField, booleanArrayField, byteField, byteArrayField, charField, charArrayField, doubleField,
			doubleArrayField, floatField, floatArrayField, intField, intArrayField, longField, longArrayField, shortField,
			shortArrayField, stringField, stringArrayField);
		
		this.protectedBooleanField = protectedBooleanField;
		this.privateBooleanField = privateBooleanField;
		this.packageBooleanField = packageBooleanField;
		this.transientBooleanField = transientBooleanField;
	}

	@Override
	public String toString() {
		return "SimpleChildSerializable [protectedBooleanField=" + protectedBooleanField + ", privateBooleanField="
				+ privateBooleanField + ", packageBooleanField=" + packageBooleanField + ", transientBooleanField="
				+ transientBooleanField + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (packageBooleanField ? 1231 : 1237);
		result = prime * result + (privateBooleanField ? 1231 : 1237);
		result = prime * result + (protectedBooleanField ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		SimpleChildSerializable other = (SimpleChildSerializable) obj;
		if (packageBooleanField != other.packageBooleanField) return false;
		if (privateBooleanField != other.privateBooleanField) return false;
		if (protectedBooleanField != other.protectedBooleanField) return false;
		return true;
	}
}

class StaxCollector {
	private final QuickWriter		writer = new QuickWriter();
	private final JsonStaxPrinter	prn = new JsonStaxPrinter(writer);
	private final QuickReader		reader = new QuickReader();
	private final JsonStaxParser	parser;
	
	StaxCollector() throws IOException {
		parser = new JsonStaxParser(reader);
	}
	
	public JsonStaxPrinter getPrinter() throws IOException {
		writer.reset();
		prn.reset();
		return prn;
	}
	
	public JsonStaxParser getParser() throws IOException {
		reader.reset(writer.getBuffer(),writer.size());
		parser.reset();

		if (parser.hasNext()) {
			parser.next();
		}
		return parser;
	}
	
	private static class QuickWriter extends CharArrayWriter {
		char[] getBuffer(){
			return buf;
		}
	}

	private static class QuickReader extends CharArrayReader {
		public QuickReader() {
			super(new char[0]);
		}

		void reset(char[] buffer, int len) {
			this.buf = buffer;
	        this.pos = 0;
	        this.count = len;
		}
	}
}
