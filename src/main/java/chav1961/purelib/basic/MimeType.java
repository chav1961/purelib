package chav1961.purelib.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import chav1961.purelib.basic.exceptions.MimeParseException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

/**
 * <p>This class implements functionality of removed class javax.activation.MimeType. It's functionality is compatible with <a href="https://tools.ietf.org/pdf/rfc2045.pdf">RFC 2045</a> requirements</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @see <a href="https://tools.ietf.org/pdf/rfc2045.pdf">RFC 2045</a>
 * @since 0.0.3
 * @last.update 0.0.7
 */
public class MimeType implements Serializable {
	private static final long 		serialVersionUID = -4429376900886702159L;	
	private static final SyntaxTreeInterface<String>	AVAILABLE_TYPE = new AndOrTree<>();
	private static final char[]		X_PREFIX_LOWERCASE = "x-".toCharArray();
	private static final char[]		X_PREFIX_UPPERCASE = "X-".toCharArray();
	private static final char[]		AVAILABLE_EXTRA_CHARS = "-+".toCharArray();
	private static final String		ASTERISK_SUBTYPE = "*";
	private static final String		APPLICATION_PRIMARY_TYPE = "application";

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for plain text</p>
	 */
	public static final MimeType	MIME_PLAIN_TEXT;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for CREOLE text</p>
	 */
	public static final MimeType	MIME_CREOLE_TEXT;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for MARKDOWN text</p>
	 */
	public static final MimeType	MIME_MARKDOWN_TEXT;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for HTML text</p>
	 */
	public static final MimeType	MIME_HTML_TEXT;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for XML text</p>
	 */
	public static final MimeType	MIME_XML_TEXT;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for JSON</p>
	 */
	public static final MimeType	MIME_JSON_TEXT;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for CSS</p>
	 */
	public static final MimeType	MIME_CSS_TEXT;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for favicon content</p>
	 */
	public static final MimeType	MIME_FAVICON;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for *.png content</p>
	 */
	public static final MimeType	MIME_PNG;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for any image content</p>
	 */
	public static final MimeType	MIME_ANY_IMAGE;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for html form content</p>
	 */
	public static final MimeType	MIME_FORM_URLENCODED;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for html multipart form content</p>
	 */
	public static final MimeType	MIME_MULTIPART_FORM;
	
	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for octet stream</p>
	 */
	public static final MimeType	MIME_OCTET_STREAM;

	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for any stream</p>
	 */
	public static final MimeType	MIME_ANY_STREAM;
	
	/**
	 * <p>Predefined MIMEs in the Pure Library: MIME for any type</p>
	 */
	public static final MimeType	MIME_ANY_TYPE;
	
	
	/**
	 * <p>This enumeration describes format of MIME string to parse</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.4
	 */
	public enum MimeStringFormat {
		/**
		 * <p>Single MIME (for example "text/html")</p> 
		 */
		SINGLE_MIME, 
		/**
		 * <p>MIME list (for example "text/html,* /foo")</p> 
		 */
		MIME_LIST, 
	}
	
	static {
		String	value;			// To reduce amount of repeatable strings in the primary type 
		
		value = "text"; 		AVAILABLE_TYPE.placeName((CharSequence)value,value);
		value = "image"; 		AVAILABLE_TYPE.placeName((CharSequence)value,value);
		value = "audio"; 		AVAILABLE_TYPE.placeName((CharSequence)value,value);
		value = "video"; 		AVAILABLE_TYPE.placeName((CharSequence)value,value);
		value = "application"; 	AVAILABLE_TYPE.placeName((CharSequence)value,value);
		value = "multipart"; 	AVAILABLE_TYPE.placeName((CharSequence)value,value);
		value = "message"; 		AVAILABLE_TYPE.placeName((CharSequence)value,value);
		value = "*"; 			AVAILABLE_TYPE.placeName((CharSequence)value,value);

		MIME_PLAIN_TEXT = buildMime("text","plain");
		MIME_CREOLE_TEXT = buildMime("text","x-wiki.creole");
		MIME_MARKDOWN_TEXT = buildMime("text","markdown");
		MIME_HTML_TEXT = buildMime("text","html");
		MIME_XML_TEXT = buildMime("text","xml");
		MIME_JSON_TEXT = buildMime("application","json");
		MIME_CSS_TEXT = buildMime("text","css");
		MIME_FAVICON = buildMime("image","webp");
		MIME_PNG = buildMime("image","x-png");
		MIME_ANY_IMAGE = buildMime("image","*");
		MIME_FORM_URLENCODED = buildMime("application","x-www-form-urlencoded");
		MIME_MULTIPART_FORM = buildMime("multipart","form-data");
		MIME_OCTET_STREAM = buildMime("application","octet-stream");
		MIME_ANY_STREAM = buildMime("application","*");
		MIME_ANY_TYPE = buildMime("*","*");
	}
	
	private final String		primaryType, subType;
	private final Properties	attr;

	/**
	 * <p>Constructor of the instance</p>
	 * @param primaryType primary mime type
	 * @param subtype mime subtype
	 * @throws MimeParseException on syntax exceptions with mime type component
	 * @throws IllegalArgumentException on null or empty parameters
	 */
	public MimeType(final String primaryType, final String subtype) throws MimeParseException, IllegalArgumentException {
		this(primaryType,subtype,new Properties());
	}
	
	/**
	 * <p>Constructor of the instance</p>
	 * @param primaryType primary mime type
	 * @param subtype mime subtype
	 * @param attrs MIME type attributes
	 * @throws MimeParseException on syntax exceptions with mime type component
	 * @throws IllegalArgumentException on null or empty parameters
	 */
	public MimeType(final String primaryType, final String subtype, final Properties attrs) throws MimeParseException, IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(primaryType)) {
			throw new IllegalArgumentException("Primary MIME type can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(subtype)) {
			throw new IllegalArgumentException("Primary MIME subtype can't be null or empty");
		}
		else if (attrs == null) {
			throw new IllegalArgumentException("Primary MIME subtype can't be null or empty");
		}
		else {
			this.primaryType = primaryType;
			this.subType = subtype;
			this.attr = attrs;
			checkPrimaryType(primaryType.toCharArray(),0,primaryType.length());
		}
	}

	/**
	 * <p>Constructor of empty instance</p>
	 */
	public MimeType() {
		this.primaryType = APPLICATION_PRIMARY_TYPE;
		this.subType = ASTERISK_SUBTYPE;
		this.attr = new Properties();
	}

	/**
	 * <p>Get primary MIME type</p>
	 * @return primary MIME type. Can't be null
	 */
	public String getPrimaryType() {
		return primaryType;
	}

	/**
	 * <p>Get subtype</p>
	 * @return MIME subtype. Can't be null
	 */
	public String getSubType() {
		return subType;
	}

	/**
	 * <p>Get MIME attributes.</p>
	 * @return MIME attributes. Can be empty, but not null.
	 */
	public Properties getAttr() {
		return attr;
	}
	
	/**
	 * <p>Is MIME type matches another MIME</p>
	 * @param targetType type to match to
	 * @return true if match
	 * @throws NullPointerException on null parameters
	 */
	public boolean match(final MimeType targetType) throws NullPointerException {
		if (targetType == null) {
			throw new NullPointerException("Target type can't be null"); 
		}
		else if ("*".equals(getSubType())) {
			return getPrimaryType().equals(targetType.getPrimaryType());
		}
		else {
			return equals(targetType);
		}
	}
	
	/**
	 * <p>Is MIME type contains in the other type. Type contains when it's primary type equals with another one, and it's subtype is equals or contains with another subtype</p>   
	 * @param another type to test with
	 * @return true if contains
	 * @throws NullPointerException on null parameters
	 * @see #containerOf(MimeType)
	 * @since 0.0.6
	 */
	public boolean containsIn(final MimeType another) {
		if (another == null) {
			throw new NullPointerException("Target type can't be null"); 
		}
		else if (getPrimaryType().equals(another.getPrimaryType())) {
			if (ASTERISK_SUBTYPE.equals(another.getSubType())) {
				return true;
			}
			else {
				for (String item : another.getSubType().split("\\+")) {
					if (item.equals(getSubType())) {
						return true;
					}
				}
				return false;
			}
		}
		else {
			return false;
		}
	}

	/**
	 * <p>Is MIME type a container for another MIME. This is an inversion of {@linkplain #containsIn(MimeType)} function.</p>
	 * @param another type to test with
	 * @return true if it is a container
	 * @throws NullPointerException on null parameters
	 * @see #containsIn(MimeType)
	 * @since 0.0.6
	 */
	public boolean containerOf(final MimeType another) {
		if (another == null) {
			throw new NullPointerException("Target type can't be null"); 
		}
		else {
			return another.containsIn(this);
		}
	}
	
	/**
	 * <p>Parse MIME string and return array of MIMEs parsed. Format of MIME string see {@linkplain #parseMimeList(char[], int, int)}<br>
	 * @param mimeList string contains MIME to parse
	 * @return arrays of MIMEs parsed. Can be empty but not null
	 * @throws IllegalArgumentException on any argument errors 
	 * @throws MimeParseException on any syntax errors in the string to parse
	 * @see #parseMimeList(char[], int, int)
	 */
	public static MimeType[] parseMimeList(final String mimeList) throws IllegalArgumentException, MimeParseException {
		if (Utils.checkEmptyOrNullString(mimeList)) {
			throw new IllegalArgumentException("Mime list can't be null or empty");
		}
		else {
			return parseMimeList(mimeList.toCharArray(),0,mimeList.length());
		}
	}

	/**
	 * <p>Parse MIME string and return array of MIMEs parsed. Format of MIME string is:<br>
	 * <MIME_string>::=<MIME>...<br>
	 * <MIME>::=<MIME_items><MIME_properties><br>
	 * <MIME_items>::=[',']<MIME_item>
	 * <MIME_item>::={<MIME_type>|'*'}'/'{<MIME_subtype>|'*'}[,<MIME_Item>]<br>
	 * <MIME_properties>::=';'<key>=["]<value>["]...<br>
	 * </p>
	 * <p>For example <code>text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,* / *;q=0.8</code></p>
	 * @param mimeList char array contains MIME to parse
	 * @param from from position to parse MIME
	 * @param to to position to parse MIME
	 * @return arrays of MIMEs parsed. Can be empty but not null
	 * @throws IllegalArgumentException on any argument errors 
	 * @throws MimeParseException on any syntax errors in the string to parse
	 */
	public static MimeType[] parseMimeList(final char[] mimeList, int from, final int to) throws IllegalArgumentException, MimeParseException {
		final int	len, begin = from;
		
		if (mimeList == null || (len = mimeList.length) == 0) {
			throw new IllegalArgumentException("Mime list can't be null or empty array");
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len);
		}
		else if (to < 0 || to > len) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+len);
		}
		else if (to <= from) {
			throw new IllegalArgumentException("To position ["+to+"] mut ge greated than from position ["+from+"]");
		}
		else {
			final int[]				bounds = new int[2];
			final List<MimeType>	result = new ArrayList<>();
			
			from--;
			do {
				final List<String[]>	mimes = new ArrayList<>();
				
	 			do {
	 				from++;
	 				if (from < to) {
		 				from = CharUtils.skipBlank(mimeList,from,true);
	 				}
				
					if (from < to && mimeList[from] == '*') {
						bounds[0] = bounds[1] = from++;
					}
					else if (from < to && Character.isJavaIdentifierStart(mimeList[from])) {
						from = CharUtils.parseNameExtended(mimeList,from,bounds,AVAILABLE_EXTRA_CHARS);
					}
					else {
						throw new MimeParseException(0,from,"Missing primary type name in ["+new String(mimeList,begin,to)+"]");
					}
					final String 	primaryType = checkPrimaryType(mimeList,bounds[0],bounds[1]+1);
					final String 	subType;
		
					if (from < to) {
						from = CharUtils.skipBlank(mimeList,from,true);
					}
					if (from < to && mimeList[from] == '/') {
						from++;
						if (from < to) {
							from = CharUtils.skipBlank(mimeList,from,true);
						}
						
						if (from < to && mimeList[from] == '*') {
							subType = ASTERISK_SUBTYPE;
							from++;
						}
						else if (from < to  && Character.isJavaIdentifierStart(mimeList[from])) {
							from = CharUtils.parseNameExtended(mimeList,from,bounds,AVAILABLE_EXTRA_CHARS);
							subType = new String(mimeList,bounds[0],bounds[1]-bounds[0]+1);
						}
						else {
							throw new MimeParseException(0,from,"Missing subtypetype name in ["+new String(mimeList,begin,to)+"]");
						}
					}
					else {
						throw new MimeParseException(0,from,"Missing '/' in ["+new String(mimeList,begin,to)+"]");
					}
					mimes.add(new String[] {primaryType,subType});
					
					if (from < to) {
						from = CharUtils.skipBlank(mimeList,from,true);
					}
				} while (from < to && mimeList[from] == ',');
				
	 			if (from < to && mimeList[from] == ';') {
	 				final Properties	props = new Properties();
	 				
	 				do {
	 					from++;
	 					if (from < to) {
		 					from = CharUtils.skipBlank(mimeList,from,true);
	 					}
	 					if (from < to) {
		 					if (Character.isJavaIdentifierStart(mimeList[from])) {
			 					from = CharUtils.parseNameExtended(mimeList,from,bounds,AVAILABLE_EXTRA_CHARS);
			 					
								final String	name = new String(mimeList,bounds[0],bounds[1]-bounds[0]+1);
								final String	value;
	
								if (from < to) {
				 					if (from < to) {
				 						from = CharUtils.skipBlank(mimeList,from,true);
				 					}
									
						 			if (from < to && mimeList[from] == '=') {
						 				from++;
					 					if (from < to) {
						 					from = CharUtils.skipBlank(mimeList,from,true);
					 					}
										
							 			if (from < to && mimeList[from] == '\"') {
							 				from = CharUtils.parseUnescapedString(mimeList, from+1, '\"', false, bounds);
							 			}
							 			else if (from < to) {
							 				bounds[0] = from;
							 				while (from < to && mimeList[from] > ' ' && mimeList[from] != ';' && mimeList[from] != ',') {
							 					from++;
							 				}
							 				bounds[1] = from-1;
							 			}
							 			else {
											throw new MimeParseException(0,from,"Missing attribute value in ["+new String(mimeList,begin,to)+"]");
							 			}
										value = new String(mimeList,bounds[0],bounds[1]-bounds[0]+1);
									}
									else {
										throw new MimeParseException(0,from,"Missing '=' for attribute value in ["+new String(mimeList,begin,to)+"]");
									}
						 			props.setProperty(name, value);
								}
								else {
									throw new MimeParseException(0,from,"Missing '=' for attribute value in ["+new String(mimeList,begin,to)+"]");
								}
		 					}
		 					else {
								throw new MimeParseException(0,from,"Missing attribute name in ["+new String(mimeList,begin,to)+"]");
		 					}
	 					}
			 			if (from < to) {
							from = CharUtils.skipBlank(mimeList,from,true);
			 			}
	 				} while (from < to && mimeList[from] == ';');
	 				
	 				for (String[] item : mimes) {
	 					final Properties	attrs = new Properties();
	 					
	 					attrs.putAll(props);
	 					result.add(new MimeType(item[0],item[1],attrs));
	 				}
	 				props.clear();
	 			}
	 			else {
	 				for (String[] item : mimes) {
	 					result.add(new MimeType(item[0],item[1]));
	 				}
	 			}
	 			mimes.clear();
	 			if (from < to) {
					from = CharUtils.skipBlank(mimeList,from,true);
	 			}
			} while (from < to && mimeList[from] == ',');
 			
			return result.toArray(new MimeType[result.size()]);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((primaryType == null) ? 0 : primaryType.hashCode());
		result = prime * result + ((subType == null) ? 0 : subType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MimeType other = (MimeType) obj;
		if (primaryType == null) {
			if (other.primaryType != null) return false;
		} else if (!primaryType.equals(other.primaryType)) return false;
		if (subType == null) {
			if (other.subType != null) return false;
		} else if (!subType.equals(other.subType)) return false;
		return true;
	}

	@Override
	public String toString() {
		return primaryType + "/" + subType+ (attr != null && !attr.isEmpty() ? "; attr="+attr : "");
	}
	
	private static String checkPrimaryType(final char[] primaryType, final int from, final int to) throws MimeParseException {
		final long	id = AVAILABLE_TYPE.seekName(primaryType,from,to);

		if (id >= 0) {	// Reduce amount of repeatable strings
			return AVAILABLE_TYPE.getCargo(id);	
		}
		else {
			if (CharUtils.compare(primaryType, from, X_PREFIX_LOWERCASE) || CharUtils.compare(primaryType, from, X_PREFIX_UPPERCASE)) {
				return new String(primaryType,from,to-from);
			}
			else {
				throw new MimeParseException(0,0,"Unsupported primary MIME type ["+new String(primaryType,from,to-from)+"]");
			}
		}
	}

	private static MimeType buildMime(final String string, final String string2) {
		try {
			return new MimeType(string, string2);
		} catch (MimeParseException e) {
			throw new PreparationException(e.getLocalizedMessage());
		}
	}
}
