package chav1961.purelib.net.playback;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Hashtable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.Line;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;

public class PlaybackURLConnection extends URLConnection implements Closeable {
	private static final String		AUDIO_DEVICE = "audio";
	private static final String		SPEAKER_DEVICE = "speaker";
	private static final String		HEADSET_DEVICE = "headset";
	
	private static final String		RATE_KEY = "rate";
	private static final String		BITS_KEY = "bits";
	private static final String		SIGNED_KEY = "signed";
	private static final String		ENDIAN_KEY = "endian";
	private static final String		CHANNELS_KEY = "channels";
	private static final String		ENCODING_KEY = "encoding";
	private static final String		VOLUME_KEY = "volume";
	
	private static final int[]		AVAILABLE_RATES = {96000, 48000, 44100 , 22050, 16000, 11025, 8000};
	private static final int		DEFAULT_RATE = 44100;
	private static final int[]		AVAILABLE_BITS = {8, 16, 24};
	private static final int		DEFAULT_BITS = 16;
	private static final String[]	AVAILABLE_SIGNED = {"signed", "unsigned"};
	private static final int		DEFAULT_SIGNED = 0;
	private static final String[]	AVAILABLE_ENDIAN = {"big", "little"};
	private static final int		DEFAULT_ENDIAN = 0;
	private static final int[]		AVAILABLE_CHANNELS = {1, 2};
	private static final int		DEFAULT_CHANNELS = 2;
	private static final String[]	AVAILABLE_ENCODING = {"pcm", "ulaw", "alaw", "gsm"};
	private static final int		DEFAULT_ENCODING = 0;
	private static final int		MIN_VOLUME = 0;
	private static final int		MAX_VOLUME = 100;
	private static final int		DEFAULT_VOLUME = 80;

	private final int				rate; 
	private final int				bits; 
	private final int				signed; 
	private final int				endian; 
	private final int				channels; 
	private final int				encoding;
	private final int				volume;
	private final Line.Info			device;
	private final SourceDataLine	lineIn;
	private final int				frameBuffer;
	private final byte[]			frame;
	private volatile boolean		connected = false;
	private volatile OutputStream	os = null;

	PlaybackURLConnection(final URL url) throws IOException {
		super(url);
		final String	host = url.getHost();
		final String	query = url.getQuery();
		
		if (query != null && !query.isEmpty()) {
			final Hashtable<String, String[]> 	keys = URIUtils.parseQuery(query.toLowerCase());

			if (keys.containsKey(RATE_KEY)) {
				int	value = toNumber(RATE_KEY, keys.get(RATE_KEY)[0]);
				checkValue(RATE_KEY,value,AVAILABLE_RATES);
				rate = value;
			}
			else {
				rate = DEFAULT_RATE;
			}
		
			if (keys.containsKey(BITS_KEY)) {
				int	value = toNumber(BITS_KEY, keys.get(BITS_KEY)[0]);
				checkValue(BITS_KEY,value,AVAILABLE_BITS);
				bits = value;
			}
			else {
				bits = DEFAULT_BITS;
			}

			if (keys.containsKey(SIGNED_KEY)) {
				signed = checkValue(SIGNED_KEY,keys.get(SIGNED_KEY)[0],AVAILABLE_SIGNED);
			}
			else {
				signed = DEFAULT_SIGNED;
			}

			if (keys.containsKey(ENDIAN_KEY)) {
				endian = checkValue(ENDIAN_KEY,keys.get(ENDIAN_KEY)[0],AVAILABLE_ENDIAN);
			}
			else {
				endian = DEFAULT_ENDIAN;
			}

			if (keys.containsKey(CHANNELS_KEY)) {
				int	value = toNumber(CHANNELS_KEY, keys.get(CHANNELS_KEY)[0]);
				checkValue(CHANNELS_KEY,value,AVAILABLE_CHANNELS);
				channels = value;
			}
			else {
				channels = DEFAULT_CHANNELS;
			}

			if (keys.containsKey(ENCODING_KEY)) {
				encoding = checkValue(ENCODING_KEY,keys.get(ENCODING_KEY)[0],AVAILABLE_ENCODING);
			}
			else {
				encoding = DEFAULT_ENCODING;
			}

			if (keys.containsKey(VOLUME_KEY)) {
				int	value = toNumber(VOLUME_KEY, keys.get(VOLUME_KEY)[0]);
				
				if (value < MIN_VOLUME || value > MAX_VOLUME) {
					throw new IOException("Volume key value ["+value+"] outsize the range "+MIN_VOLUME+".."+MAX_VOLUME);
				}
				else {
					volume = value;
				}
			}
			else {
				volume = DEFAULT_VOLUME;
			}
		}
		else {
			rate = DEFAULT_RATE;
			bits = DEFAULT_BITS;
			signed = DEFAULT_SIGNED;
			endian = DEFAULT_ENDIAN;
			channels = DEFAULT_CHANNELS;
			encoding = DEFAULT_ENCODING;
			volume = DEFAULT_VOLUME;
		}
		
		final AudioFormat	af = new AudioFormat(rate, bits, channels, signed == 0, endian == 0);

		if (host != null) {
			switch (host) {
				case AUDIO_DEVICE	:
					device = testDevice(host,AudioSystem.getTargetLineInfo(Port.Info.LINE_OUT));
					break;
				case SPEAKER_DEVICE	:
					device = testDevice(host,AudioSystem.getTargetLineInfo(Port.Info.SPEAKER));
					break;
				case HEADSET_DEVICE	:
					device = testDevice(host,AudioSystem.getTargetLineInfo(Port.Info.HEADPHONE));
					break;
				default :
					throw new IOException("Device ["+host+"] is not supported");
			}
			try{this.lineIn = AudioSystem.getSourceDataLine(af);
				this.frameBuffer = lineIn.getBufferSize();
				this.frame = new byte[frameBuffer];
			} catch (LineUnavailableException e) {
				throw new IOException("Audio line ["+host+"] is unavailable: "+e.getLocalizedMessage(),e);
			}
		}
		else {
			throw new IOException("URL ["+url+"] doesn't contain host name");
		}
	}


	@Override
	public void connect() throws IOException {
		if (!connected) {
			try{lineIn.open();
				final FloatControl	fc = (FloatControl) lineIn.getControl(Type.MASTER_GAIN);
				
				fc.setValue(fc.getMinimum() + volume*(fc.getMaximum()-fc.getMinimum())/100);
				lineIn.start();
			
			} catch (LineUnavailableException e) {
				throw new IOException("Audio line is unavailable: "+e.getLocalizedMessage(),e);
			}
			connected = true;

			this.os = new OutputStream() {
				final GrowableByteArray	gba = new GrowableByteArray(false,18);
				int total = 0;
				
				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					gba.append(b,off,len);
					total += len;
					upload();
				}
				
				@Override
				public void write(int b) throws IOException {
					gba.append((byte)(b & 0xFF));
					total += 1;
					upload();
				}
				
				@Override
				public void flush() throws IOException {
					super.flush();
					ultimateUpload();
					lineIn.flush();
				}
				
				private void upload() {
					final int 	maxIndex = gba.length();
					
					if (maxIndex >= frameBuffer) {
						ultimateUpload();
					}
				}
				
				private void ultimateUpload() {
					final int 	maxIndex = Math.min(gba.length(),frameBuffer);
					int 		index, len;
					
					for (index = 0; index < maxIndex; index += frameBuffer) {
						len = gba.read(index, frame);
						
						int	displ = 0;
						
						while (displ < len) {
							displ += lineIn.write(frame, displ, len-displ);
						}
					}
					if (gba.length() > frameBuffer) {
						len = gba.read(index, frame);
						gba.length(0);
						gba.append(frame,0,len);
					}
					else {
						gba.length(0);
					}
				}
			};
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (connected) {
			return os;
		}
		else {
			throw new IOException("Attempt to get OutputStream before setting connection. Call connect() method before!");
		}
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		if (connected) {
			close();
			return new InputStream() {
				@Override
				public int read() throws IOException {
					return -1;
				}
			};
		}
		else {
			throw new IOException("Attempt to get InputStream before setting connection. Call connect() method before!");
		}
	}
	
	@Override
	public void close() throws IOException {
		if (connected) {
			connected = false;
			lineIn.stop();
			lineIn.close();
		}
	}

	private int toNumber(final String key, final String value) throws IOException {
		try{return Integer.valueOf(value);
		} catch (NumberFormatException exc) {
			throw new IOException("Query key ["+key+"] value is not a valid integer ["+value+"]");
		}
	}
	
	private void checkValue(final String key, final int value, final int[] available) throws IOException {
		for (int item : available) {
			if (value == item) {
				return;
			}
		}
		throw new IOException("Unsupported query key ["+key+"] value ["+value+"], valid  values are "+Arrays.toString(available));
	}

	private int checkValue(final String key, final String value, final String[] available) throws IOException {
		for (int index = 0; index < available.length; index++) {
			if (value.equals(available[index])) {
				return index;
			}
		}
		throw new IOException("Unsupported query key ["+key+"] value ["+value+"], valid  values are "+Arrays.toString(available));
	}

	private Info testDevice(final String device, final Info[] info) throws IOException {
		if (info == null || info.length == 0) {
			throw new IOException("No audio device ["+device+"] was detected in the system");
		}
		else {
			return info[0];
		}
	}
}
