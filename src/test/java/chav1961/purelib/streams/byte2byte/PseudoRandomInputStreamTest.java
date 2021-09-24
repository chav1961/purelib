package chav1961.purelib.streams.byte2byte;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PseudoRandomInputStreamTest {
	private static final int	MAX_VAL = 1 << 20; 
	
	private File	f;
	
	@Before
	public void prepare() throws IOException {
		f = File.createTempFile("pris", ".tmp");
		
		try(final FileOutputStream	fos = new FileOutputStream(f);
			final DataOutputStream	dos = new DataOutputStream(fos)) {
			for (int index= 0; index < MAX_VAL; index++) {
				dos.writeInt(index);
			}
			dos.flush();
		}
	}
	
//	@Test
	public void unboundedTest() throws IOException {
		try(final FileInputStream			fis = new FileInputStream(f);
			final PseudoRandomInputStream	pris = new PseudoRandomInputStream(fis, PseudoRandomInputStream.UNKNOWN);
			final DataInputStream			dis = new DataInputStream(pris)) {
			
			for (int index= 0; index < MAX_VAL; index++) {
				Assert.assertEquals(index,dis.readInt());
			}
			
			Assert.assertEquals(f.length(), pris.getFileLength());
			Assert.assertEquals(f.length(), pris.getFilePointer());
			
			pris.setFilePointer(0);
			Assert.assertEquals(0,dis.readInt());
		}
	}

	@Test
	public void boundedTest() throws IOException {
		try(final FileInputStream			fis = new FileInputStream(f);
			final PseudoRandomInputStream	pris = new PseudoRandomInputStream(fis, f.length());
			final DataInputStream			dis = new DataInputStream(pris)) {
			
			for (int index= 0; index < MAX_VAL; index++) {
				Assert.assertEquals(index,dis.readInt());
			}
			
			Assert.assertEquals(f.length(), pris.getFileLength());
			Assert.assertEquals(f.length(), pris.getFilePointer());
			
			pris.setFilePointer(0);
			Assert.assertEquals(0,dis.readInt());
		}
	}
	
	@After
	public void unprepare() {
		f.delete();
	}
}
