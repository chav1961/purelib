package chav1961.purelib.streams.byte2byte;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;

public class FragmentedStreamTest {

	private int			index;
	private byte[][]	content;
	
	@Test
	public void fragmentedInputStreamTest() {
	}

	@Test
	public void fragmentedOuputStreamTest() throws IOException {
		index = 0;
		content = new byte[][] {new byte[2], new byte[3], new byte[4]};
		
		try(final OutputStream	os = new FragmentedOutputStream() {
									@Override
									protected boolean morePieces() throws IOException {
										switch (index) {
											case 0	:
												Assert.assertEquals(this,append(content[index++]));
												
												try{append(null);
													Assert.fail("Mandatory exception was not detected (null 1-st argument)");
												} catch (NullPointerException exc) {
												}
												return true;
											case 1	:
												Assert.assertEquals(this,append(content[index++]).append(content[index], 0, content[index].length));
												
												try{append(null,0,1);
													Assert.fail("Mandatory exception was not detected (null 1-st argument)");
												} catch (NullPointerException exc) {
												}
												try{append(content[index],content[index].length,1);
													Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
												} catch (IllegalArgumentException exc) {
												}
												try{append(content[index],0,content[index].length+1);
													Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
												} catch (IllegalArgumentException exc) {
												}
												index++;
												return true;
											default	:
												return false;
										}
									}
								}) {
			os.write('1');
			os.write("23456789".getBytes());
			Assert.assertEquals("123456789", new String(content[0]) + new String(content[1]) + new String(content[2]));
			Assert.assertEquals(0, ((FragmentedOutputStream)os).getLastPieceFill());
			
			try{os.write("12345".getBytes());
				Assert.fail("Mandatory exception was not detected (storage exhausted)");
			} catch (IOException exc) {
			}
		}
		
		try{new FragmentedOutputStream(null) {@Override protected boolean morePieces() throws IOException {return false;}};
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try{new FragmentedOutputStream(null, 0, 0) {@Override protected boolean morePieces() throws IOException {return false;}};
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new FragmentedOutputStream(new byte[1], 1, 1) {@Override protected boolean morePieces() throws IOException {return false;}};
			Assert.fail("Mandatory exception was not detected (2-nd argumentout of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{new FragmentedOutputStream(new byte[1], 0, 2) {@Override protected boolean morePieces() throws IOException {return false;}};
			Assert.fail("Mandatory exception was not detected (3-rd argumentout of range)");
		} catch (IllegalArgumentException exc) {
		}

		try{new FragmentedOutputStream(){@Override protected boolean morePieces() throws IOException {return false;}}.append(null, 0, 0);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new FragmentedOutputStream() {@Override protected boolean morePieces() throws IOException {return false;}}.append(new byte[1], 1, 1);
			Assert.fail("Mandatory exception was not detected (2-nd argumentout of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{new FragmentedOutputStream() {@Override protected boolean morePieces() throws IOException {return false;}}.append(new byte[1], 0, 2);
			Assert.fail("Mandatory exception was not detected (3-rd argumentout of range)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
