package chav1961.purelib.streams.byte2byte;


import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public class MappedStreamsTest {
	File	f;
	
	@Before
	public void prepare()throws IOException {
		f = File.createTempFile("test", ".bin");
	}

	@After
	public void unprepare() {
		f.delete();
	}
	
	@Test
	public void lifeCycleTest() throws IOException {
//		final long	start1 = System.currentTimeMillis();
		
		try(final OutputStream	os = new MappedOutputStream(f);
			final DataOutputStream	dos = new DataOutputStream(os, ByteOrder.BIG_ENDIAN)) {
			
			for(int index = 0; index < 1 << 26; index++) {
				dos.writeInt(index);
			}
			
			try{os.write(null, 0, 1);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{os.write(new byte[1], -1, 1);
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{os.write(new byte[1], 10, 1);
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{os.write(new byte[1], 0, 0);
				Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{os.write(new byte[1], 0, 10);
				Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
		}
//		System.err.println("File written in "+(System.currentTimeMillis()-start1)+" msec");
		
		try{new MappedOutputStream(null).close();
			Assert.fail("Mandatory exception was not detected (null 1-st argument");
		} catch (NullPointerException exc) {
		}
//		final long	start2 = System.currentTimeMillis();
		
		try(final InputStream	is = new MappedInputStream(f);
			final DataInputStream	dis = new DataInputStream(is, ByteOrder.BIG_ENDIAN)) {
			
			for(int index = 0; index < 1 << 26; index++) {
				Assert.assertEquals(index, dis.readInt());
			}
			
			try{is.read(null, 0, 1);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{is.read(new byte[1], -1, 1);
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{is.read(new byte[1], 10, 1);
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{is.read(new byte[1], 0, 0);
				Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try{is.read(new byte[1], 0, 10);
				Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
		}
//		System.err.println("File read in "+(System.currentTimeMillis()-start2)+" msec");

		try{new MappedInputStream(null).close();;
			Assert.fail("Mandatory exception was not detected (null 1-st argument");
		} catch (NullPointerException exc) {
		}
	}
}
