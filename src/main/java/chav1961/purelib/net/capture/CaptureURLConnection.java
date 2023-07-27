package chav1961.purelib.net.capture;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Hashtable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;

public class CaptureURLConnection extends URLConnection implements Closeable {
	private static final String		AUDIO_DEVICE = "audio";
	private static final String		MICROPHONE_DEVICE = "microphone";
	
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

//	private final int				rate; 
//	private final int				bits; 
//	private final int				signed; 
//	private final int				endian; 
//	private final int				channels; 
//	private final int				encoding;
//	private final int				volume;
	private final Line.Info			device;
	private final TargetDataLine	lineOut;
	private final int				frameBuffer;
	private final byte[]			frame;
	private volatile boolean		connected = false;
	private volatile InputStream	is = null;

	CaptureURLConnection(final URL url) throws IOException {
		super(url);
		final String		host = url.getHost();
		final String		query = url.getQuery();
		final AudioFormat	af = buildAudioFormat(query); 
		
//		if (query != null && !query.isEmpty()) {
//			final Hashtable<String, String[]> 	keys = URIUtils.parseQuery(query.toLowerCase());
//
//			if (keys.containsKey(RATE_KEY)) {
//				int	value = toNumber(RATE_KEY, keys.get(RATE_KEY)[0]);
//				checkValue(RATE_KEY,value,AVAILABLE_RATES);
//				rate = value;
//			}
//			else {
//				rate = DEFAULT_RATE;
//			}
//		
//			if (keys.containsKey(BITS_KEY)) {
//				int	value = toNumber(BITS_KEY, keys.get(BITS_KEY)[0]);
//				checkValue(BITS_KEY,value,AVAILABLE_BITS);
//				bits = value;
//			}
//			else {
//				bits = DEFAULT_BITS;
//			}
//
//			if (keys.containsKey(SIGNED_KEY)) {
//				signed = checkValue(SIGNED_KEY,keys.get(SIGNED_KEY)[0],AVAILABLE_SIGNED);
//			}
//			else {
//				signed = DEFAULT_SIGNED;
//			}
//
//			if (keys.containsKey(ENDIAN_KEY)) {
//				endian = checkValue(ENDIAN_KEY,keys.get(ENDIAN_KEY)[0],AVAILABLE_ENDIAN);
//			}
//			else {
//				endian = DEFAULT_ENDIAN;
//			}
//
//			if (keys.containsKey(CHANNELS_KEY)) {
//				int	value = toNumber(CHANNELS_KEY, keys.get(CHANNELS_KEY)[0]);
//				checkValue(CHANNELS_KEY,value,AVAILABLE_CHANNELS);
//				channels = value;
//			}
//			else {
//				channels = DEFAULT_CHANNELS;
//			}
//
//			if (keys.containsKey(ENCODING_KEY)) {
//				encoding = checkValue(ENCODING_KEY,keys.get(ENCODING_KEY)[0],AVAILABLE_ENCODING);
//			}
//			else {
//				encoding = DEFAULT_ENCODING;
//			}
//
//			if (keys.containsKey(VOLUME_KEY)) {
//				int	value = toNumber(VOLUME_KEY, keys.get(VOLUME_KEY)[0]);
//				
//				if (value < MIN_VOLUME || value > MAX_VOLUME) {
//					throw new IOException("Volume key value ["+value+"] outsize the range "+MIN_VOLUME+".."+MAX_VOLUME);
//				}
//				else {
//					volume = value;
//				}
//			}
//			else {
//				volume = DEFAULT_VOLUME;
//			}
//		}
//		else {
//			rate = DEFAULT_RATE;
//			bits = DEFAULT_BITS;
//			signed = DEFAULT_SIGNED;
//			endian = DEFAULT_ENDIAN;
//			channels = DEFAULT_CHANNELS;
//			encoding = DEFAULT_ENCODING;
//			volume = DEFAULT_VOLUME;
//		}
//		
//		final AudioFormat	af = new AudioFormat(rate, bits, channels, signed == 0, endian == 0);

		if (host != null) {
			switch (host) {
				case AUDIO_DEVICE	:
					device = testDevice(host, AudioSystem.getSourceLineInfo(Port.Info.LINE_IN));
					break;
				case MICROPHONE_DEVICE	:
					device = testDevice(host, AudioSystem.getSourceLineInfo(Port.Info.MICROPHONE));
					break;
				default :
					throw new IOException("Device ["+host+"] is not supported");
			}
			try{this.lineOut = AudioSystem.getTargetDataLine(af);
				this.frameBuffer = lineOut.getBufferSize();
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
			try{lineOut.open();
//				final FloatControl	fc = (FloatControl) lineOut.getControl(Type.MASTER_GAIN);
//				
//				fc.setValue(fc.getMinimum() + volume*(fc.getMaximum()-fc.getMinimum())/100);
				lineOut.start();
			
			} catch (LineUnavailableException e) {
				throw new IOException("Audio line is unavailable: "+e.getLocalizedMessage(),e);
			}
			connected = true;

			this.is = new InputStream() {
				final byte[]	buffer = new byte[1];

				@Override
				public int read(final byte[] b, final int off, final int len) throws IOException {
					return lineOut.read(b, off, len);
				}
				
				@Override
				public int read() throws IOException {
					lineOut.read(buffer,0,buffer.length);
					return buffer[0];
				}
			};
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (connected) {
			return new OutputStream() {
				@Override public void write(int b) throws IOException {}
			};
		}
		else {
			throw new IOException("Attempt to get OutputStream before setting connection. Call connect() method before!");
		}
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		if (connected) {
			return is;
		}
		else {
			throw new IOException("Attempt to get InputStream before setting connection. Call connect() method before!");
		}
	}
	
	@Override
	public void close() throws IOException {
		if (connected) {
			connected = false;
			lineOut.stop();
			lineOut.close();
		}
	}

	public static boolean hasDevice(final URL url) {
		if (url == null) {
			throw new NullPointerException("URL to test can't be null"); 
		}
		else {
			try{final String		host = url.getHost();
				final String		query = url.getQuery();
				final AudioFormat	af = buildAudioFormat(query);
				
				if (!Utils.checkEmptyOrNullString(host)) {
					switch (host) {
						case AUDIO_DEVICE	:
							return AudioSystem.getSourceLineInfo(Port.Info.LINE_IN).length > 0;
						case MICROPHONE_DEVICE	:
							return AudioSystem.getSourceLineInfo(Port.Info.MICROPHONE).length > 0;
						default :
							throw new IOException("Device ["+host+"] is not supported");
					}
				}
				else {
					return false;
				}
			} catch (IOException e) {
				return false;
			} 
		}
	}
	
	private static int toNumber(final String key, final String value) throws IOException {
		try{return Integer.valueOf(value);
		} catch (NumberFormatException exc) {
			throw new IOException("Query key ["+key+"] value is not a valid integer ["+value+"]");
		}
	}
	
	private static void checkValue(final String key, final int value, final int[] available) throws IOException {
		for (int item : available) {
			if (value == item) {
				return;
			}
		}
		throw new IOException("Unsupported query key ["+key+"] value ["+value+"], valid  values are "+Arrays.toString(available));
	}

	private static int checkValue(final String key, final String value, final String[] available) throws IOException {
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
	
	private static AudioFormat buildAudioFormat(final String query) throws IOException {
		final int	rate; 
		final int	bits; 
		final int	signed; 
		final int	endian; 
		final int	channels;
		final int	encoding;
		final int	volume;
		
		if (!Utils.checkEmptyOrNullString(query)) {
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
		
		return new AudioFormat(rate, bits, channels, signed == 0, endian == 0);
	}
}
