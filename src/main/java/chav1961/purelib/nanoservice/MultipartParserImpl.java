package chav1961.purelib.nanoservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.nanoservice.interfaces.MultipartContent;

/**
 * 
 * @see RFC-2047
 */

class MultipartParserImpl implements MultipartContent {
	private static final char[]	CONTENT_DISPOSITION = "Content-Disposition".toCharArray();
	private static final char[]	MIME_VERSION = "MIME-Version".toCharArray();
	private static final char[]	CONTENT_TYPE = "Content-Type".toCharArray();
	private static final char[]	CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding".toCharArray();
	private static final char[]	CONTENT_ID = "Content-ID".toCharArray();
	private static final char[]	CONTENT_DESCRIPTION = "Content-Description".toCharArray();
	private static final String	PROP_KEYVALUE = "keyValue";
	
	private static final int	TERM_START = 0;
	private static final int	TERM_START_DATA = 1;
	private static final int	TERM_CONTENT_TYPE = 2;
	private static final int	TERM_CONTENT_BASE64 = 3;
	private static final int	TERM_CONTENT_PLAIN = 4;
	private static final int	TERM_CONTENT = 5;
	private static final int	TERM_END_OF_DATA = 6;
	private static final int	TERM_END_OF_FILE = 7;

	private static final int	STATE_INITIAL = 0;
	
	private final Properties	contentKeys = new Properties();
	private final int[]			forInt = new int[2];
	private final StringBuilder	sb = new StringBuilder();
	private char[]				divider = null;
	private int					state = STATE_INITIAL;
	
	MultipartParserImpl(final InputStream is, final String encoding) {
		try(final Reader	rdr = new InputStreamReader(is, encoding == null || encoding.isEmpty() ? PureLibSettings.DEFAULT_CONTENT_ENCODING : encoding);
			final LineByLineProcessor	lblp = new LineByLineProcessor(((displacement, lineNo, data, from, length)->processLine(displacement, lineNo, data, from, length)))) {
			
			lblp.write(rdr);
			lblp.flush();
		} catch (IOException | SyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			automat(TERM_END_OF_FILE);
			divider = null;
		}
	}
	
	@Override
	public int getPartCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getPartNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getPartContent(String partName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPartName(final String partName) {
		if (partName == null || partName.isEmpty()) {
			throw new IllegalArgumentException("Part name can't be null or empty"); 
		}
		else {
			for (String item : getPartNames()) {
				if (item.equals(partName)) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public Properties getPartProperties(final String partName) {
		// TODO Auto-generated method stub
		return null;
	}
	
	void processLine(final long displacement, final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		if (lineNo == 0) {
			divider = Arrays.copyOfRange(data, from, from + length);
			automat(TERM_START);
		}
		else if (CharUtils.compare(data, from, divider)) {
			automat(TERM_END_OF_DATA);
		}
		else if (CharUtils.compareIgnoreCase(data, from, CONTENT_DISPOSITION)) {
			automat(TERM_START_DATA);
			from = parseNamedList(data, lineNo, CharUtils.skipBlank(data, from + CONTENT_DISPOSITION.length, true), contentKeys);
		}
		else if (CharUtils.compareIgnoreCase(data, from, CONTENT_TYPE)) {
			automat(TERM_CONTENT_TYPE);
			from = parseNamedList(data, lineNo, CharUtils.skipBlank(data, from + CONTENT_TYPE.length, true), contentKeys);

			final MimeType[]	content = MimeType.parseMimeList(contentKeys.getProperty(PROP_KEYVALUE));
			
			if (PureLibSettings.MIME_ANY_STREAM.match(content[0]) || PureLibSettings.MIME_ANY_IMAGE.match(content[0])) {
				automat(TERM_CONTENT_BASE64);
			}
			else {
				automat(TERM_CONTENT_PLAIN);
			}
		}
		else if (CharUtils.compareIgnoreCase(data, from, MIME_VERSION)) {
			automat(TERM_START_DATA);
			from = parseNamedList(data, lineNo, CharUtils.skipBlank(data, from + MIME_VERSION.length, true), contentKeys);
		}
		else if (CharUtils.compareIgnoreCase(data, from, CONTENT_TRANSFER_ENCODING)) {
			automat(TERM_START_DATA);
			from = parseNamedList(data, lineNo, CharUtils.skipBlank(data, from + MIME_VERSION.length, true), contentKeys);
		}
		else if (CharUtils.compareIgnoreCase(data, from, CONTENT_ID)) {
			automat(TERM_START_DATA);
			from = parseNamedList(data, lineNo, CharUtils.skipBlank(data, from + MIME_VERSION.length, true), contentKeys);
		}
		else if (CharUtils.compareIgnoreCase(data, from, CONTENT_DESCRIPTION)) {
			automat(TERM_START_DATA);
			from = parseNamedList(data, lineNo, CharUtils.skipBlank(data, from + MIME_VERSION.length, true), contentKeys);
		}
		else {
			automat(TERM_CONTENT);
		}
	}
	
	void automat(final int terminal) {
		switch (state) {
			case STATE_INITIAL	:
				switch (terminal) {
					case TERM_START	:
						break;
					default :
				}
				break;
			default :
		}
	}
	
	int parseNamedList(final char[] data, final int lineNo, int from, final Properties keyValuePairs) throws SyntaxException {
		from = CharUtils.skipBlank(data, from, true);
		
		if (data[from] == ':') {
			from = CharUtils.skipBlank(data, from + 1, true);
			if (Character.isLetter(data[from])) {
				from = CharUtils.skipBlank(data, CharUtils.parseName(data, from, forInt), true);

				keyValuePairs.setProperty(PROP_KEYVALUE, new String(data, forInt[0], forInt[1] - forInt[0]));
				if (data[from] == ';') {
						do {
							from = CharUtils.skipBlank(data, from + 1, true);
							if (Character.isLetter(data[from])) {
								from = CharUtils.skipBlank(data, CharUtils.parseName(data, from, forInt), true);
								
								final String	name = new String(data, forInt[0], forInt[1] - forInt[0]);
								
								if (data[from] == '=') {
									from = CharUtils.skipBlank(data, from + 1, true);
									
									sb.setLength(0);
									if (data[from] == '\"') {
										from = CharUtils.parseStringExtended(data, from, '\"', sb);
										if (data[from] == '\"') {
											from = CharUtils.skipBlank(data, from + 1, true);
											keyValuePairs.setProperty(name, sb.toString());
										}
										else {
											throw new SyntaxException(lineNo, from, "upaired quotes"); 
										}
									}
								}
								else {
									throw new SyntaxException(lineNo, from, "missing '='"); 
								}
							}
							else {
								throw new SyntaxException(lineNo, from, "missing name"); 
							}
						} while (data[from] == ';');
					}						
				}
				else {
					throw new SyntaxException(lineNo, from, "illegal disposition"); 
				}
			}
		else {
			throw new SyntaxException(lineNo, from, "missing name"); 
		}
		return from;
	}
	
	private static class PartDescriptor {
		private final String		partName;
		private final Properties	partProperties = new Properties();
		private final byte[]		partContent;
		
		private PartDescriptor(final String partName, final byte[] partContent) {
			this.partName = partName;
			this.partContent = partContent;
		}
	}
}
