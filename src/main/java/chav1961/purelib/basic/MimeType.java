package chav1961.purelib.basic;

import java.awt.datatransfer.MimeTypeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>This class implements functionality of removed class javax.activation.MimeType. It's functionality is compatible with <a href="https://tools.ietf.org/pdf/rfc2045.pdf">RFC 2045</a> requirements</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @see <a href="https://tools.ietf.org/pdf/rfc2045.pdf">RFC 2045</a>
 * @since 0.0.3
 * @lastUpdate 0.0.4
 */
public class MimeType {
	private static final Map<String,String>	AVAILABLE_TYPE = new HashMap<>();
	private static final String				X_PREFIX_LOWERCASE = "x-";
	private static final String				X_PREFIX_UPPERCASE = "X-";
	private static final String				APPLICATION_PRIMARY_TYPE = "application";
	private static final String				ASTERISK_SUBTYPE = "*";
	private static final char[]				AVAILABLE_EXTRA_CHARS = "-".toCharArray();
	
	static {
		String	value;			// To reduce amount of repeatable strings in the primary type 
		
		value = "text"; 					AVAILABLE_TYPE.put(value,value);
		value = "image"; 					AVAILABLE_TYPE.put(value,value);
		value = "audio"; 					AVAILABLE_TYPE.put(value,value);
		value = "video"; 					AVAILABLE_TYPE.put(value,value);
		value = APPLICATION_PRIMARY_TYPE; 	AVAILABLE_TYPE.put(value,value);
		value = "multipart"; 				AVAILABLE_TYPE.put(value,value);
		value = "message"; 					AVAILABLE_TYPE.put(value,value);
	}
	
	private final String		primaryType, subType;
	private final Properties	attr;

	/**
	 * <p>Constructor of the instance</p>
	 * @param primaryType primary mime type
	 * @param subtype mime subtype
	 * @throws MimeTypeParseException on syntax exceptions with mime type component
	 * @throws IllegalArgumentException on null or empty parameters
	 */
	public MimeType(final String primaryType, final String subtype) throws MimeTypeParseException, IllegalArgumentException {
		if (primaryType == null || primaryType.isEmpty()) {
			throw new IllegalArgumentException("Primary MIME type can't be null or empty");
		}
		else if (subtype == null || subtype.isEmpty()) {
			throw new IllegalArgumentException("Primary MIME subtype can't be null or empty");
		}
		else {
			this.primaryType = checkPrimaryType(primaryType);
			this.subType = subtype;
			this.attr = null;
		}
	}

	/**
	 * <p>Constructor of empty instance</p>
	 */
	public MimeType() {
		this.primaryType = APPLICATION_PRIMARY_TYPE;
		this.subType = ASTERISK_SUBTYPE;
		this.attr = null;
	}

	/**
	 * <p>Constructor of the instance from concatenated MIME description</p> 
	 * @param mime mime description
	 * @throws MimeTypeParseException on syntax exceptions with mime type component
	 * @throws IllegalArgumentException on null or empty parameters
	 */
	public MimeType(final String mime) throws MimeTypeParseException, IllegalArgumentException {
		if (mime == null || mime.isEmpty()) {
			throw new IllegalArgumentException("MIME description can't be null or empty");
		}
		else {
			final char[]	content = new char[mime.length()+1];
			final int[]		bounds = new int[2];
			int				from = 0;
			
			mime.getChars(0,content.length-1, content,0);
			content[content.length-1] = '\n';
 
			try {
				from = CharUtils.parseNameExtended(content,CharUtils.skipBlank(content,from,true),bounds,AVAILABLE_EXTRA_CHARS);
				if (bounds[0] == bounds[1]) {
					throw new MimeTypeParseException("Missing primary type name");
				}
				else {
					this.primaryType = checkPrimaryType(new String(content,bounds[0],bounds[1]-bounds[0]+1));
				}
	
				if (content[from = CharUtils.skipBlank(content,from,true)] != '/') {
					throw new MimeTypeParseException("Missing '/' in the MIME description");
				}
				else {
					if (content[from = CharUtils.skipBlank(content,from+1,true)] == '*') {
						this.subType = ASTERISK_SUBTYPE;
						from++;
					}
					else {
						from = CharUtils.parseNameExtended(content,from,bounds,AVAILABLE_EXTRA_CHARS);
						if (bounds[0] == bounds[1]) {
							throw new MimeTypeParseException("Missing subtype name");
						}
						else {
							this.subType = new String(content,bounds[0],bounds[1]-bounds[0]+1);
						}
					}
				}
				
				Properties	props = null;
				
				while (content[from = CharUtils.skipBlank(content,from,true)] == ';') {
					from = CharUtils.skipBlank(content,from+1,true);
					
					if (!Character.isJavaIdentifierStart(content[from])) {
						break;
					}
					else {
						from = CharUtils.parseName(content,from,bounds);
						if (content[from = CharUtils.skipBlank(content,from,true)] != '=') {
							throw new MimeTypeParseException("Missing (=) after "+new String(content,bounds[0],bounds[1]-bounds[0]+1));
						}
						else {
							int		start = from = CharUtils.skipBlank(content,from+1,true), end;
							
							while (content[from] != ';' && content[from] != '\n') {
								from++;
							}
							end = from;
							while (end > start && content[end-1] == ' ') {
								end--;
							}
							if (props == null) {
								props = new Properties();
							}
							props.setProperty(new String(content,bounds[0],bounds[1]-bounds[0]+1),new String(content,start,end-start));
						}
					}
				}
				if (content[from = CharUtils.skipBlank(content,from,true)] != '\n' ) {
					throw new MimeTypeParseException("Unparsed tail at position ["+from+"] ");
				}
				else {
					this.attr = props;
				}
			} catch (IllegalArgumentException exc) {
				throw new MimeTypeParseException(exc.getLocalizedMessage());
			}
		}
	}

	public String getSubType() {
		return subType;
	}

	public String getPrimaryType() {
		return primaryType;
	}

	public Properties getAttr() {
		return attr;
	}
	
	public boolean match(final MimeType targetType) {
		return equals(targetType);
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
		return primaryType + "/" + subType+ (attr != null ? ", attr="+attr : "");
	}
	
	private static String checkPrimaryType(final String primaryType) throws MimeTypeParseException {
		String	availablePrimary = AVAILABLE_TYPE.get(primaryType);
		
		if (availablePrimary != null) {
			return availablePrimary;	// Reduce amount of repeatable strings
		}
		else {
			if (primaryType.startsWith(X_PREFIX_LOWERCASE) || primaryType.startsWith(X_PREFIX_UPPERCASE)) {
				return primaryType;
			}
			else {
				throw new MimeTypeParseException("Unsupported primary MIME type ["+primaryType+"]");
			}
		}
	}
}
