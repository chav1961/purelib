package chav1961.purelib.streams.byte2byte;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import chav1961.purelib.basic.growablearrays.GrowableByteArray;

public class CipherInputStream extends InputStream {
	private final InputStream	nested;
	private final Cipher		cipher;
	private final GrowableByteArray	gba = new GrowableByteArray(false);
	private int					gbaStart = 0;
	private boolean				atEOF = false;
	
	public CipherInputStream(final InputStream nested, final String cipherAlgorithm, final Key key) throws IOException, NullPointerException, IllegalArgumentException {
		if (nested == null) {
			throw new NullPointerException("Nested output stream can't be null"); 
		}
		else if (cipherAlgorithm == null || cipherAlgorithm.isEmpty()) {
			throw new IllegalArgumentException("Cipher algorithm can't be null or empty"); 
		}
		else if (key == null) {
			throw new NullPointerException("Decrypting key can't be null"); 
		}
		else {
			try{this.nested = nested;
				this.cipher = Cipher.getInstance(cipherAlgorithm);
				this.cipher.init(Cipher.DECRYPT_MODE, key);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
				throw new IOException("Cipher creation failed (algorithm error): "+e.getLocalizedMessage(),e);
			} catch (InvalidKeyException e) {
				throw new IOException("Cipher creation failed (key error): "+e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		return decrypt(b,0,b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return decrypt(b,off,len);
	}
	
	@Override
	public int read() throws IOException {
		final byte[]	content = new byte[1];
		final int		rc = read(content);
		
		return rc <= 0 ? -1 : content[0];
	}

	@Override
	public void close() throws IOException {
		nested.close();
	}
	
	private int decrypt(final byte[] content, final int from, final int len) throws IOException {
		if (content == null) {
			throw new NullPointerException("Content array can't be null");
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From index ["+from+"] out of range 0.."+(content.length-1));
		}
		else if (len < 0 || len > content.length) {
			throw new IllegalArgumentException("Length ["+from+"] out of range 0.."+(content.length-1));
		}
		else if (from + len > content.length) {
			throw new IllegalArgumentException("From index + length ["+(from+len)+"] out of range 0.."+(content.length-1));
		}
		else {
			if (gba.length() > 0) {
				final int 	readed = gba.read(gbaStart,content,from,from+len);
				
				if (readed < len) {
					gba.length(0);
				}
				else {
					gbaStart += readed;
				}
				return readed;
			}
			else if (atEOF) {
				return -1;
			}
			else {
				final int	currentLen = nested.read(content,from,len);
				
				if (currentLen <= 0) {
					atEOF = true;
					
					try{return processDecryptedContent(cipher.doFinal(),content,from,len);
					} catch (IllegalBlockSizeException | BadPaddingException e) {
						throw new IOException(e.getLocalizedMessage(),e);
					}
				}
				else {
					return processDecryptedContent(cipher.update(content,from,len),content,from,len);
				}
			}
		}
	}
	
	private int processDecryptedContent(final byte[] source, final byte[] content, final int from, final int len) {
		if (source.length <= len) {
			System.arraycopy(source,0,content,from,source.length);
			return source.length;
		}
		else {
			System.arraycopy(source,0,content,from,len);
			gba.append(source, from+len, source.length);
			gbaStart = 0;
			return len;
		}
	}
}
