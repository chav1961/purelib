package chav1961.purelib.concurrent;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.testing.TestingUtils;

@Tag("OrdinalTestCategory")
public class XByteStreamTest {
	final PrintStream	ps = TestingUtils.err();
	
	@Test
	public void basicTest() throws IOException {
		try(final XByteStream	xcs = new XByteStream()) {
			
			final InputStream	rdr = xcs.createInputStream();
			
			try{xcs.createInputStream();
				Assert.fail("Mandatory exception was not detected (duplicate input stream creation)");
			} catch (IOException exc) {
			}
			
			try{xcs.createOutputStream();
				Assert.fail("Mandatory exception was not detected (output stream creation from the same thread)");
			} catch (IOException exc) {
			}
		}

		try(final XByteStream	xcs = new XByteStream()) {
			
			final OutputStream	wr = xcs.createOutputStream();
			
			try{xcs.createOutputStream();
				Assert.fail("Mandatory exception was not detected (duplicate output stream creation)");
			} catch (IOException exc) {
			}

			try{xcs.createInputStream();
				Assert.fail("Mandatory exception was not detected (input stream creation from the same thread)");
			} catch (IOException exc) {
			}
		}
	}

	@Test
	public void lifeCycleTest() throws IOException, DebuggingException, InterruptedException {
		try(final XByteStream	xcs = new XByteStream()) {
			final JUnitExecutor<String,String>	t1Ex = new JUnitExecutor<>(); 
			final Thread		t1 = new Thread(()->{
									ps.println("Writer started");
				
									try(final OutputStream	os = xcs.createOutputStream();
										final Writer		wr = new OutputStreamWriter(os);
										final PrintWriter	pwr = new PrintWriter(wr)) {
										
										for (;;) {
											try {
												t1Ex.waitCommand((cmd,parm)->{
													switch (cmd) {
														case "writeLine"	:
															final String	line = "line";
															
															pwr.println(line);
															ps.println("Write: "+line);
															return "ok";
														case "flush"	:
															pwr.flush();
															return "ok";
														default :
															throw new UnsupportedOperationException("Unsupported command ["+cmd+"]"); 
													}
												});
											} catch (InterruptedException e) {
												break;
											}
										}
									} catch (IOException e) {
										ps.println("Writer I/O error: "+e);
									} finally {
										ps.println("Writer ended");
									}
								});
			
			final JUnitExecutor<String,String>	t2Ex = new JUnitExecutor<>(); 
			final Thread		t2 = new Thread(()->{
									ps.println("Reader started");
									
									try(final InputStream		is = xcs.createInputStream();
										final Reader			rdr = new InputStreamReader(is);
										final BufferedReader	brdr = new BufferedReader(rdr)) {
										
										for (;;) {
											try {
												t2Ex.waitCommand((cmd,parm)->{
													switch (cmd) {
														case "readLine"	:
															final String	line = brdr.readLine(); 
															
															ps.println("Read: "+line);
															return line;
														default :
															throw new UnsupportedOperationException("Unsupported command ["+cmd+"]"); 
													}
												});
											} catch (InterruptedException e) {
												break;
											}
										}
									} catch (IOException e) {
										ps.println("Reader I/O error: "+e);
									} finally {
										ps.println("Reader ended");
									}
								});
			
			t1.setDaemon(true);
			t1.start();
			t2.setDaemon(true);
			t2.start();
			
			// Scenario 1: 
			ps.println("Scenario 1:");
			// - step 1.1: get content for empty pipe (wait state will be detected)
			Assert.assertTrue(t2Ex.execute("readLine",1000));
			Assert.assertFalse(t2Ex.hasResponse());
			// - step 1.2: send string without flush (no wait state)
			Assert.assertTrue(t1Ex.execute("writeLine",1000));
			Assert.assertEquals("ok",t1Ex.getResponse(1000));
			// - step 1.3: flush previous content (wait state can be detected)
			Assert.assertTrue(t1Ex.execute("flush",1000));
			Assert.assertEquals("line",t2Ex.getResponse(1000));
			Assert.assertEquals("ok",t1Ex.getResponse(1000));

			// Scenario 2: 
			ps.println("Scenario 2:");
			// - step 2.1: send and flush string (wait state will be detected)
			Assert.assertTrue(t1Ex.execute("writeLine",1000));
			Assert.assertEquals("ok",t1Ex.getResponse(1000));
			Assert.assertTrue(t1Ex.execute("flush",1000));
			Assert.assertFalse(t1Ex.hasResponse());
			// - step 2.2: read line ();
			Assert.assertTrue(t2Ex.execute("readLine",1000));
			Assert.assertEquals("line",t2Ex.getResponse(1000));
			Assert.assertEquals("ok",t1Ex.getResponse(1000));

			// Scenario 3: 
			ps.println("Scenario 3:");
			// - step 3.1: send and flush string (wait state will be detected) 
			Assert.assertTrue(t1Ex.execute("writeLine",1000));
			Assert.assertEquals("ok",t1Ex.getResponse(1000));
			Assert.assertTrue(t1Ex.execute("flush",1000));
			Assert.assertFalse(t1Ex.hasResponse());
			// - step 3.2: terminate receiver
			t2.interrupt();
			Assert.assertFalse(t1Ex.hasResponse());
			Assert.assertEquals("ok",t1Ex.getResponse(2000));
		}
	}
}
