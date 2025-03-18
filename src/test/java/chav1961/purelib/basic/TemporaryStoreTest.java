package chav1961.purelib.basic;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.TemporaryStore.InputOutputPair;

@Tag("OrdinalTestCategory")
public class TemporaryStoreTest {
	public byte[]	content, piece;
	
	@BeforeEach
	public void prepare() {
		content = new byte[1 << 22];
		piece = new byte[1 << 18];
		
		for (int index = 0; index < content.length; index++) {
			content[index] = (byte)index;
		}
		for (int index = 0; index < piece.length; index++) {
			piece[index] = (byte)index;
		}
	}
	
	@Test
	public void basicTest() throws IOException {
		final int	repeats = 10;
		
		try(final TemporaryStore ts = new TemporaryStore()) {
			try(final InputOutputPair	pair = ts.allocate()) {
				try(final OutputStream	os = pair.getOutputStream()) {
					os.write(piece);
					os.flush();
				}
				Assert.assertEquals(pair.getSizeUsed(),piece.length);
				byte[]	read = new byte[piece.length];
				
				try(final InputStream	is = pair.getInputStream()) {
					Assert.assertEquals(read.length,is.read(read));
					Assert.assertEquals((byte)0,read[0]);
					Assert.assertEquals((byte)1,read[1]);
					Assert.assertEquals(-1,is.read(read));
				}
			}

			try(final InputOutputPair	pair = ts.allocate()) {
				try(final OutputStream	os = pair.getOutputStream()) {
					for (int index = 0; index < repeats; index++) {
						os.write(piece);
					}
					os.flush();
				}
				Assert.assertEquals(pair.getSizeUsed(),repeats * piece.length);
				byte[]	read = new byte[piece.length];
				
				try(final InputStream	is = pair.getInputStream()) {
					for (int index = 0; index < repeats; index++) {
						Assert.assertEquals(read.length,is.read(read));
						Assert.assertEquals((byte)0,read[0]);
						Assert.assertEquals((byte)1,read[1]);
					}
					Assert.assertEquals(-1,is.read(read));
				}
			}
			
			try(final InputOutputPair	pair = ts.allocate()) {
				try(final OutputStream	os = pair.getOutputStream()) {
					os.write(content);
					os.flush();
				}
				Assert.assertEquals(pair.getSizeUsed(),content.length);
				byte[]	read = new byte[content.length];
				
				try(final InputStream	is = pair.getInputStream()) {
					Assert.assertEquals(read.length,is.read(read));
					Assert.assertEquals((byte)0,read[0]);
					Assert.assertEquals((byte)1,read[1]);
					Assert.assertEquals(-1,is.read(read));
				}
			}
			
			try(final InputOutputPair	pair = ts.allocate()) {
				try{pair.getInputStream();
					Assert.fail("Mandatory exception was not detected (scenario fail)");
				} catch (IllegalStateException exc) {
				}
				try(final OutputStream	os = pair.getOutputStream()) {
					try{pair.getInputStream();
						Assert.fail("Mandatory exception was not detected (scenario fail)");
					} catch (IllegalStateException exc) {
					}
				}
				try{pair.getOutputStream();
					Assert.fail("Mandatory exception was not detected (scenario fail)");
				} catch (IllegalStateException exc) {
				}
				try(final InputStream	is = pair.getInputStream()) {
					try{pair.getOutputStream();
						Assert.fail("Mandatory exception was not detected (scenario fail)");
					} catch (IllegalStateException exc) {
					}
				}
				try{pair.getOutputStream();
					Assert.fail("Mandatory exception was not detected (scenario fail)");
				} catch (IllegalStateException exc) {
				}
				try{pair.getInputStream();
					Assert.fail("Mandatory exception was not detected (scenario fail)");
				} catch (IllegalStateException exc) {
				}
			}
		}
	}

//	@Test
	public void multiThreadTest() throws IOException, InterruptedException {
		final int				numThreads = 20, numLoops = 100; 
		final CountDownLatch	start = new CountDownLatch(1), end = new CountDownLatch(numThreads);
		final boolean[]			errors = new boolean[numThreads];
		
		try(final TemporaryStore ts = new TemporaryStore()) {
			for (int index = 0; index < numThreads; index++) {
				final int		currentIndex = index;
				final Thread	t = new Thread(()->{
										try {start.await();
											for (int loop = 0; loop < numLoops; loop++) {
												try(final InputOutputPair	pair = ts.allocate()) {
													try(final OutputStream	os = pair.getOutputStream()) {
														os.write(piece);
														os.flush();
													}
													byte[]	read = new byte[piece.length];
													
													try(final InputStream	is = pair.getInputStream()) {
														Assert.assertEquals(read.length,is.read(read));
														Assert.assertEquals((byte)0,read[0]);
														Assert.assertEquals((byte)1,read[1]);
														Assert.assertEquals(-1,is.read(read));
													}
												}
												
												try(final InputOutputPair	pair = ts.allocate()) {
													try(final OutputStream	os = pair.getOutputStream()) {
														os.write(content);
														os.flush();
													}
													byte[]	read = new byte[content.length];
													
													try(final InputStream	is = pair.getInputStream()) {
														Assert.assertEquals(read.length,is.read(read));
														Assert.assertEquals((byte)0,read[0]);
														Assert.assertEquals((byte)1,read[1]);
														Assert.assertEquals(-1,is.read(read));
													}
												}
											}
										} catch (Throwable exc) {
											exc.printStackTrace();
											errors[currentIndex] = true;
										} finally {
											end.countDown();
										}
									}
								);
				
				t.setDaemon(true);
				t.start();
			}
			start.countDown();
			end.await();
			Assert.assertArrayEquals(new boolean[numThreads],errors);
		}
	}

	@AfterEach
	public void unprepare() {
		content = null;
		piece = null;
	}
}
