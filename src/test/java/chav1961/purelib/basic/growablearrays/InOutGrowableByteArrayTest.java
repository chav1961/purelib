package chav1961.purelib.basic.growablearrays;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
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
			
			Assert.assertEquals(123, gba.readByte());
			Assert.assertTrue(gba.readBoolean());
			Assert.assertEquals('1', gba.readChar());
			Assert.assertEquals("23456789", gba.readLine());
			Assert.assertEquals(-345.6, gba.readDouble(), 0.0001);
			Assert.assertEquals(-345.6, gba.readFloat(), 0.0001);
			Assert.assertEquals(789, gba.readInt());
			Assert.assertEquals(1234, gba.readLong());
			Assert.assertEquals(567, gba.readShort());
			Assert.assertEquals(gba.readUnsignedShort(), 65535);
			Assert.assertEquals("test \u1200 string", gba.readUTF());
			Assert.assertEquals(123, gba.readByte());
			Assert.assertEquals(255, gba.readUnsignedByte());
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

			Assert.assertEquals(123, dis.readByte());
			Assert.assertEquals('1', dis.readChar());
			Assert.assertEquals("23456789", dis.readLine());
			Assert.assertEquals(-345.6, dis.readDouble(), 0.0001);
			Assert.assertEquals(-345.6, dis.readFloat(), 0.0001);
			Assert.assertEquals(789, dis.readInt());
			Assert.assertEquals(1234, dis.readLong());
			Assert.assertEquals(567, dis.readShort());
			Assert.assertEquals("test \u1200 string", dis.readUTF());
			Assert.assertEquals(123, dis.readByte());
		}
		
		gba.reset();
	}
}
