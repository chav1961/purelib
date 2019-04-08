package chav1961.purelib.concurrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.FlowException;

public class XCharStreamTest {
	@Test
	public void basicTest() throws IOException {
		try(final XCharStream	xcs = new XCharStream()) {
			
			final Reader		rdr = xcs.createReader();
			
			try{xcs.createReader();
				Assert.fail("Mandatory exception was not detected (duplicate reader creation)");
			} catch (IOException exc) {
			}
			
			try{xcs.createWriter();
				Assert.fail("Mandatory exception was not detected (reader creation from the same thread)");
			} catch (IOException exc) {
			}
		}

		try(final XCharStream	xcs = new XCharStream()) {
			
			final Writer		wr = xcs.createWriter();
			
			try{xcs.createWriter();
				Assert.fail("Mandatory exception was not detected (duplicate writer creation)");
			} catch (IOException exc) {
			}

			try{xcs.createReader();
				Assert.fail("Mandatory exception was not detected (writer creation from the same thread)");
			} catch (IOException exc) {
			}
		}
	}

	@Test
	public void lifeCycleTest() throws IOException, FlowException, InterruptedException {
		try(final XCharStream	xcs = new XCharStream()) {
			final JUnitExecutor<String,String>	t1Ex = new JUnitExecutor<>(); 
			final Thread		t1 = new Thread(()->{
									System.err.println("Writer started");
				
									try(final Writer	wr = xcs.createWriter();
										final PrintWriter	pwr = new PrintWriter(wr)) {
										
										for (;;) {
											try {
												t1Ex.waitCommand((cmd,parm)->{
													switch (cmd) {
														case "writeLine"	:
															final String	line = "line";
															
															pwr.println(line);
															System.err.println("Write: "+line);
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
										System.err.println("Writer I/O error: "+e);
									} finally {
										System.err.println("Writer ended");
									}
								});
			
			final JUnitExecutor<String,String>	t2Ex = new JUnitExecutor<>(); 
			final Thread		t2 = new Thread(()->{
									System.err.println("Reader started");
									
									try(final Reader	rdr = xcs.createReader();
										final BufferedReader	brdr = new BufferedReader(rdr)) {
										
										for (;;) {
											try {
												t2Ex.waitCommand((cmd,parm)->{
													switch (cmd) {
														case "readLine"	:
															final String	line = brdr.readLine(); 
															
															System.err.println("Read: "+line);
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
										System.err.println("Reader I/O error: "+e);
									} finally {
										System.err.println("Reader ended");
									}
								});
			
			t1.setDaemon(true);
			t1.start();
			t2.setDaemon(true);
			t2.start();
			
			// Scenario 1: 
			System.err.println("Scenario 1:");
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
			System.err.println("Scenario 2:");
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
			System.err.println("Scenario 3:");
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
