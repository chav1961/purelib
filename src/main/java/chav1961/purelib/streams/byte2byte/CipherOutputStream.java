package chav1961.purelib.streams.byte2byte;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import chav1961.purelib.streams.interfaces.Finishable;

public class CipherOutputStream extends OutputStream implements Finishable {
	private final OutputStream	nested;
	private final Cipher		cipher;
	private boolean				finished = false;
	
	public CipherOutputStream(final OutputStream nested, final String cipherAlgorithm, final Key key) throws IOException, NullPointerException, IllegalArgumentException {
		if (nested == null) {
			throw new NullPointerException("Nested output stream can't be null"); 
		}
		else if (cipherAlgorithm == null || cipherAlgorithm.isEmpty()) {
			throw new IllegalArgumentException("Cipher algorithm can't be null or empty"); 
		}
		else if (key == null) {
			throw new NullPointerException("Encrypting key can't be null"); 
		}
		else {
			try{this.nested = nested;
				this.cipher = Cipher.getInstance(cipherAlgorithm);
				this.cipher.init(Cipher.ENCRYPT_MODE, key);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
				throw new IOException("Cipher creation failed (algorithm error): "+e.getLocalizedMessage(),e);
			} catch (InvalidKeyException e) {
				throw new IOException("Cipher creation failed (key error): "+e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public void finish() throws IOException {
		if (!finished) {
			try{finished = true;
				nested.write(cipher.doFinal());
				nested.flush();
			} catch (IllegalBlockSizeException | BadPaddingException e) {
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public void close() throws IOException {
		finish();
		nested.close();
	}
	
	@Override
	public boolean isFinished() {
		return finished;
	}

	@Override
	public void write(byte[] b) throws IOException {
		if (isFinished()) {
			throw new IllegalStateException("Attempt to write data after calling finish() method");
		}
		else {
			encrypt(b,0,b.length);
		}
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (isFinished()) {
			throw new IllegalStateException("Attempt to write data after calling finish() method");
		}
		else {
			encrypt(b,off,len);
		}
	}
	
	@Override
	public void flush() throws IOException {
		super.flush();
	}
	
	@Override
	public void write(int b) throws IOException {
		if (isFinished()) {
			throw new IllegalStateException("Attempt to write data after calling finish() method");
		}
		else {
			encrypt(new byte[]{(byte)b},0,1);
		}
	}

	private void encrypt(final byte[] content, final int from, final int len) throws IOException {
		if (content == null) {
			throw new NullPointerException("Content array can't be null");
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From index ["+from+"] out of range 0.."+(content.length-1));
		}
		else if (len < 0 || len >= content.length) {
			throw new IllegalArgumentException("Length ["+from+"] out of range 0.."+(content.length-1));
		}
		else if (from + len >= content.length) {
			throw new IllegalArgumentException("From index + length ["+(from+len)+"] out of range 0.."+(content.length-1));
		}
		else {
			nested.write(cipher.update(content,from,len));
		}
	}
}
