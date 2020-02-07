package chav1961.purelib.basic.growablearrays;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class InOutGrowableByteArrayTest {
	@Test
	public void test() throws IOException {
		try(final InOutGrowableByteArray	gba = new InOutGrowableByteArray(false)) {
			internalTest(gba);
		}
		try(final InOutGrowableByteArray	gba = new InOutGrowableByteArray(true)) {
			internalTest(gba);
		}
	}

	// InOutGrowableByteArray class functionality must be identical to DataOutputStream/DataInputStream.
	@SuppressWarnings("deprecation")
	private void internalTest(final InOutGrowableByteArray gba) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final DataOutputStream		dos = new DataOutputStream(baos)) {

			dos.writeByte(123);
			dos.writeBoolean(true);
			dos.writeChars("1");
			dos.writeBytes("23456789\r\n");
			dos.writeDouble(-345.6);
			dos.writeFloat(-345.6f);
			dos.writeInt(789);
			dos.writeLong(1234);
			dos.writeShort(567);
			dos.writeShort(-1);
			dos.writeUTF("test \u1200 string");
			dos.writeByte(123);
			dos.writeByte(-1);
			dos.flush();
			
			gba.append(baos.toByteArray());
			
			Assert.assertEquals(gba.readByte(),123);
			Assert.assertTrue(gba.readBoolean());
			Assert.assertEquals(gba.readChar(),'1');
			Assert.assertEquals(gba.readLine(),"23456789");
			Assert.assertEquals(gba.readDouble(),-345.6,0.0001);
			Assert.assertEquals(gba.readFloat(),-345.6,0.0001);
			Assert.assertEquals(gba.readInt(),789);
			Assert.assertEquals(gba.readLong(),1234);
			Assert.assertEquals(gba.readShort(),567);
			Assert.assertEquals(65535,gba.readUnsignedShort());
			Assert.assertEquals(gba.readUTF(),"test \u1200 string");
			Assert.assertEquals(gba.readByte(),123);
			Assert.assertEquals(255,gba.readUnsignedByte());
		}
		
		gba.reset();

		gba.writeByte(123);
		gba.writeChars("1");
		gba.writeBytes("23456789\r\n");
		gba.writeDouble(-345.6);
		gba.writeFloat(-345.6f);
		gba.writeInt(789);
		gba.writeLong(1234);
		gba.writeShort(567);
		gba.writeUTF("test \u1200 string");
		gba.writeByte(123);
		
		try(final ByteArrayInputStream	bais = new ByteArrayInputStream(gba.toPlain().toArray());
			final DataInputStream		dis = new DataInputStream(bais)) {

			Assert.assertEquals(dis.readByte(),123);
			Assert.assertEquals(dis.readChar(),'1');
			Assert.assertEquals(dis.readLine(),"23456789");
			Assert.assertEquals(dis.readDouble(),-345.6,0.0001);
			Assert.assertEquals(dis.readFloat(),-345.6,0.0001);
			Assert.assertEquals(dis.readInt(),789);
			Assert.assertEquals(dis.readLong(),1234);
			Assert.assertEquals(dis.readShort(),567);
			Assert.assertEquals(dis.readUTF(),"test \u1200 string");
			Assert.assertEquals(dis.readByte(),123);
		}
		
		gba.reset();
	}
}
