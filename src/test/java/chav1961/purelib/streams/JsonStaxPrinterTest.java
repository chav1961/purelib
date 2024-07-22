package chav1961.purelib.streams;


import org.junit.Assert;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class JsonStaxPrinterTest {
	@FunctionalInterface
	private interface Callback {
		void process(final JsonStaxPrinter prn) throws IOException;
	}
	
	@Test
	public void basicTest() throws IOException {
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.value(100);}),"100");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.value(123.0);}),"123");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.value(true);}),"true");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.value(false);}),"false");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.nullValue();}),"null");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.array(new boolean[]{true,false});}),"[true,false]");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.array(new byte[]{-100,0,100});}),"[-100,0,100]");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.array("test".toCharArray());}),"[\"t\",\"e\",\"s\",\"t\"]");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.array(new double[]{-100.0,0.0,100.0});}),"[-100,0,100]");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.array(new float[]{-100.0f,0.0f,100.0f});}),"[-100,0,100]");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.array(new int[]{-100,0,100});}),"[-100,0,100]");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.array(new long[]{-100L,0L,100L});}),"[-100,0,100]");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.array(new short[]{-100,0,100});}),"[-100,0,100]");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.array(new String[]{"test1",null,"test2"});}),"[\"test1\",null,\"test2\"]");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.startObject().name("key1").value("value1").splitter().name("key2").value("value2").endObject();}),"{\"key1\":\"value1\",\"key2\":\"value2\"}");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.startArray().endArray();}),"[]");
		Assert.assertEquals(processElement((JsonStaxPrinter prn)->{prn.startObject().endObject();}),"{}");
	}

	@Test
	public void lifeCycleTest() throws IOException {
		try{processElement((JsonStaxPrinter prn)->{prn.value(true).value(false);});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.value(100).value(-100);});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.value(100.0).value(-100.0);});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.value("100").value("200");});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.array(new boolean[]{true,false}).array(new boolean[]{true,false});});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.array("test".toCharArray()).array("test".toCharArray());});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.array(new double[]{-100,100}).array(new double[]{-100,100});});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.array(new float[]{-100,100}).array(new float[]{-100,100});});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.array(new int[]{-100,100}).array(new int[]{-100,100});});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.array(new long[]{-100,100}).array(new long[]{-100,100});});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.array(new short[]{-100,100}).array(new short[]{-100,100});});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.array(new String[]{"test1","test2"}).array(new String[]{"test1","test2"});});
			Assert.fail("Mandatory exception was not detected (two sequential values)");
		} catch (IOException exc) {
		}

		try{processElement((JsonStaxPrinter prn)->{prn.splitter();});
			Assert.fail("Mandatory exception was not detected (unwaited splitter)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.value(true).splitter();});
			Assert.fail("Mandatory exception was not detected (unwaited splitter)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startArray().splitter();});
			Assert.fail("Mandatory exception was not detected (unwaited splitter)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startObject().splitter();});
			Assert.fail("Mandatory exception was not detected (unwaited splitter)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startArray().value(100).splitter().splitter();});
			Assert.fail("Mandatory exception was not detected (unwaited splitter)");
		} catch (IOException exc) {
		}
		
		try{processElement((JsonStaxPrinter prn)->{prn.startObject().endArray();});
			Assert.fail("Mandatory exception was not detected (unpaired start/end structures)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startArray().endObject();});
			Assert.fail("Mandatory exception was not detected (unpaired start/end structures)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startObject().endObject().endObject();});
			Assert.fail("Mandatory exception was not detected (pair stack exhausted)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startArray().endArray().endArray();});
			Assert.fail("Mandatory exception was not detected (pair stack exhausted)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startArray();});
			Assert.fail("Mandatory exception was not detected (unclosed array pairs)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startObject();});
			Assert.fail("Mandatory exception was not detected (unclosed object pairs)");
		} catch (IOException exc) {
		}

		try{processElement((JsonStaxPrinter prn)->{prn.startObject().value(100).endObject();});
			Assert.fail("Mandatory exception was not detected (name is missing)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startObject().name("key1").endObject();});
			Assert.fail("Mandatory exception was not detected (value is missing)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startObject().name("key1").name("key2").endObject();});
			Assert.fail("Mandatory exception was not detected (value is missing)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startObject().name("key1").value("value1").value("value2").endObject();});
			Assert.fail("Mandatory exception was not detected (splitter is missing)");
		} catch (IOException exc) {
		}
		try{processElement((JsonStaxPrinter prn)->{prn.startObject().name("key1").value("value1").name("key2").value("value2").endObject();});
			Assert.fail("Mandatory exception was not detected (splitter is missing)");
		} catch (IOException exc) {
		}
	}

	@Test
	public void exceptionsTest() throws IOException {
		try(final JsonStaxPrinter p = new JsonStaxPrinter(null)){
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try(final JsonStaxPrinter p = new JsonStaxPrinter(new StringWriter(),0)) {
			Assert.fail("Mandatory exception was not detected (too short buffer)");
		} catch (IllegalArgumentException exc) {
		}

		try(final Writer			wr = new StringWriter();
			final JsonStaxPrinter	prn = new JsonStaxPrinter(wr)) {

			try{prn.startObject().name(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{prn.endObject().startObject().name(null,0,0);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{prn.endObject().startObject().name(new char[0],0,0);
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{prn.endObject().startObject().name(new char[100],-1,0);
				Assert.fail("Mandatory exception was not detected (2-nd argument outside the range)");
			} catch (IllegalArgumentException exc) {
			}
			try{prn.endObject().startObject().name(1000);
				Assert.fail("Mandatory exception was not detected (invalid call - trr was not passed to the constructor)");
			} catch (IllegalStateException exc) {
			}
			
			prn.endObject();
		}
	}
	
	private String processElement(final Callback callback) throws IOException {
		try(final Writer				wr = new StringWriter()) {
			try(final JsonStaxPrinter	prn = new JsonStaxPrinter(wr)) {
				callback.process(prn);
				prn.flush();
			}
			
			return wr.toString();
		}
	}
}
