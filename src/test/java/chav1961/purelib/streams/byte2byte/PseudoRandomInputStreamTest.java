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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class PseudoRandomInputStreamTest {
	private static final int	MAX_VAL = 1 << 16; 
	
	private File	f;
	
	@BeforeEach
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
	
	@Test
	public void unboundedTest() throws IOException {
		try(final FileInputStream			fis = new FileInputStream(f);
			final PseudoRandomInputStream	pris = new PseudoRandomInputStream(fis, PseudoRandomInputStream.UNKNOWN);
			final DataInputStream			dis = new DataInputStream(pris)) {

			Assert.assertEquals(f.length(), pris.length());
			
			for (int index= 0; index < MAX_VAL; index++) {
				Assert.assertEquals(index,dis.readInt());
			}
			
			Assert.assertEquals(f.length(), pris.getFilePointer());
			
			for (int index= 0; index < MAX_VAL; index += 16) {
				pris.seek(index * 4);
				Assert.assertEquals(index,dis.readInt());
			}
		}
	}

	@Test
	public void parentTest() throws IOException {
		try(final FileInputStream			fis = new FileInputStream(f);
			final PseudoRandomInputStream	pris = new PseudoRandomInputStream(fis, PseudoRandomInputStream.UNKNOWN);
			final PseudoRandomInputStream	slice = new PseudoRandomInputStream(pris, 0, f.length());
			final DataInputStream			dis = new DataInputStream(slice)) {

			Assert.assertEquals(f.length(), slice.length());
			
			for (int index= 0; index < MAX_VAL; index++) {
				Assert.assertEquals(index,dis.readInt());
			}
			
			Assert.assertEquals(f.length(), slice.getFilePointer());
			
			for (int index= 0; index < MAX_VAL; index += 16) {
				slice.seek(index * 4);
				Assert.assertEquals(index,dis.readInt());
			}
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
			
			Assert.assertEquals(f.length(), pris.length());
			Assert.assertEquals(f.length(), pris.getFilePointer());
			
			for (int index= 0; index < MAX_VAL; index += 16) {
				pris.seek(index * 4);
				Assert.assertEquals(index,dis.readInt());
			}
		}
	}
	
	@After
	public void unprepare() {
		f.delete();
	}
}
