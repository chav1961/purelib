package chav1961.purelib.concurrent;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import chav1961.purelib.basic.exceptions.DebuggingException;

public class DataPipeTest {

	@Test
	public void basicTest() throws IOException {
		final DataPipe	dp = new DataPipe(int.class);
		
		Assert.assertEquals(int.class, dp.getTypeSupported());
		dp.close();
		dp.close();
		
		try {
			dp.flush();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readBoolean();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readByte();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readChar();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readDouble();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readFloat();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readFully(new byte[1]);
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readInt();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readLine();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readLong();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readShort();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readUnsignedByte();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readUnsignedShort();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.readUTF();
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}

		try {
			dp.write(new byte[1]);
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.write(0);
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.writeBoolean(false);
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.writeByte(0);
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.writeBytes("?");
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.writeChar(0);
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.writeChars("?");
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.writeDouble(0);
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.writeFloat(0);
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.writeInt(0);
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.writeLong(0);
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.writeShort(0);
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
		try {
			dp.writeUTF("?");
			Assert.fail("Mandatory exception was not detected (operation after close)");
		} catch (IOException exc) {
		}
	}

	@Test
	public void lifeCycleTest() throws IOException, DebuggingException, InterruptedException {
		final JUnitExecutor<String,String>	t1Ex = new JUnitExecutor<>(false); 
		final JUnitExecutor<String,String>	t2Ex = new JUnitExecutor<>(false); 
		final AtomicReference<DataPipe>		ref = new AtomicReference<>();
		
		final Thread	sender = new Thread(()->{
									for (;;) {
										try {
											t1Ex.waitCommand((cmd,parm)->{
												try {
													switch (cmd) {
														case "createPipe"	:
															ref.set(new DataPipe(int.class));
															return "ok";
														case "closePipe"	:
															ref.get().close();
															return "ok";
														case "send"	:
															ref.get().writeInt(100);
															return "ok";
														case "flush"	:
															ref.get().flush();
															return "ok";
														default :
															throw new UnsupportedOperationException("Unsupported command ["+cmd+"]"); 
													}
												} catch (Exception exc) {
													return "error "+exc.getClass().getSimpleName();
												}
											});
										} catch (InterruptedException e) {
											break;
										}
									}
								});
		sender.setName("SENDER");
		sender.setDaemon(true);
		sender.start();
		final Thread	receiver = new Thread(()->{
									for (;;) {
										try {
											t2Ex.waitCommand((cmd,parm)->{
												try {
													switch (cmd) {
														case "receive"	:
															return "val="+ref.get().readInt();
														case "closePipe"	:
															ref.get().close();
															return "ok";
														default :
															throw new UnsupportedOperationException("Unsupported command ["+cmd+"]"); 
													}
												} catch (Throwable exc) {
//													exc.printStackTrace();
													return "error "+exc.getClass().getSimpleName();
												}
											});
										} catch (InterruptedException e) {
											break;
										}
									}
								});
		receiver.setName("RECEIVER");
		receiver.setDaemon(true);
		receiver.start();
		
		// Scenario 1:
		// -- step 1.1: Sender create pipe.
		Assert.assertTrue(t1Ex.execute("createPipe", 200));
		Assert.assertEquals("ok", t1Ex.getResponse(1000));
		// -- step 1.2: Receiver read content.
		Assert.assertTrue(t2Ex.execute("receive", 200));
		Assert.assertFalse(t2Ex.hasResponse());
		// -- step 1.3: Sender close pipe.
		Assert.assertTrue(t1Ex.execute("closePipe", 200));
		Assert.assertEquals("ok",t1Ex.getResponse(200));
		// -- step 1.4: receiver crushes.
		Assert.assertEquals("error IOException", t2Ex.getResponse(1000));

		// Scenario 2:
		// -- step 2.1: Sender create pipe.
		Assert.assertTrue(t1Ex.execute("createPipe", 200));
		Assert.assertEquals("ok", t1Ex.getResponse(1000));
		// -- step 2.2: Receiver read content.
		Assert.assertTrue(t2Ex.execute("receive", 200));
		Assert.assertFalse(t2Ex.hasResponse());
		// -- step 2.3: Sender send value.
		Assert.assertTrue(t1Ex.execute("send", 200));
		Assert.assertEquals("ok",t1Ex.getResponse(200));
		// -- step 2.3: Sender flush value.
		Assert.assertTrue(t1Ex.execute("flush", 200));
		Assert.assertEquals("ok",t1Ex.getResponse(200));
		// -- step 1.4: receiver got value.
		Assert.assertEquals("val=100", t2Ex.getResponse(1000));
	}
}
