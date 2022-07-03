package chav1961.purelib.model;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.text.MaskFormatter;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.json.ColorKeeper;
import chav1961.purelib.json.FileKeeper;
import chav1961.purelib.json.ImageKeeper;
import chav1961.purelib.ui.ColorPair;
import chav1961.purelib.ui.interfaces.ItemAndReference;
import chav1961.purelib.ui.interfaces.LongItemAndReference;
import chav1961.purelib.ui.interfaces.LongItemAndReferenceList;
import chav1961.purelib.ui.interfaces.MimeBasedContent;

/**
 * <p>This class describes format string associated with any model entity. Detailed format description see {@linkplain #FieldFormat(Class, String)}</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.5
 */
public class FieldFormat {
	/**
	 * <p>Content type for the given format</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	public enum ContentType {
		BooleanContent,
		StringContent,
		FormattedStringContent,
		PasswordContent,
		IntegerContent,
		NumericContent,
		DateContent,
		TimestampContent,
		EnumContent,
		FileContent,
		URIContent,
		ColorContent,
		ColorPairContent,
		ArrayContent,
		NestedContent,
		ImageContent,
		ForeignKeyRefContent,
		ForeignKeyRefListContent,
		MimeBasedContent,
		Unclassified
	} 
	
	/**
	 * <p>Alignment mode for the given format</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	public enum Alignment {
		LeftAlignment,
		RightAlignment,
		CenterAlignment,
		Ajusted,
		NoMatter
	}
	
	public enum PrintMode {
		SINGLE_TEXT,
		CREOLE_TEXT,
		HTML_TEXT
	}

	private final ContentType	contentType;
	private final Alignment		alignment;
	private final int			length;
	private final int			height;
	private final int			frac;
	private final String		mask;
	private final boolean		isMandatory;
	private final boolean		isReadOnly;
	private final boolean		isReadOnlyOnExistent;
	private final boolean		negativeHighlight;
	private final boolean		zeroHighlight;
	private final boolean		positiveHighlight;
	private final boolean		needSelect;
	private final boolean		useInList;
	private final boolean		useInListAnchored;
	private final boolean		isOutput;
	private final boolean		supportNulls;
	private final boolean		hasLocalEditor;
	private final String		wizardType;
	
	/**
	 * <p>Constructor of the class. Build default format for the given class</p>
	 * @param clazz class to build format for. Can't be null
	 * @throws NullPointerException class is null
	 */
	public FieldFormat(final Class<?> clazz) throws NullPointerException {
		this(clazz,"");
	}
	
	/**
	 * <p>Constructor of the class. Build format for the given class and given format string. Format string can be (in BNC):</p>
	 * <code>
	 * &lt;format&gt; ::= [&lt;mask&gt;]&lt;lengthAndFrac&gt;[&lt;options&gt;]</br>
	 * &lt;mask&gt; ::= '('&lt;{@linkplain MaskFormatter} mask&gt;')'</br>
	 * &lt;lengthAndFrac&gt; ::= &lt;length&gt;['.'&lt;frac&gt;]['*'&lt;height&gt;]</br>
	 * &lt;length&gt; ::= &lt;number&gt;</br>
	 * &lt;frac&gt; ::= &lt;number&gt;</br>
	 * &lt;height&gt; ::= &lt;number&gt;</br>
	 * &lt;options&gt; ::= &lt;option&gt;[&lt;options&gt;]</br>
	 * &lt;option&gt; ::= {'r'|'R'|'l'|'L'|'m'|'n'|'N'|'o'|'z'|'p'|'s'|'&lt;'|'&gt;'|'&lt;&gt;'|'&gt;&lt;'}</br>
	 * </code>
	 * <p>Option codes are:</p>
	 * <ul>
	 * <li><b>r</b> - field is always read-only</li>
	 * <li><b>R</b> - field is read-only except new and duplicated records</li>
	 * <li><b>l</b> - field must be appear in the search list and/or table</li>
	 * <li><b>L</b> - field must be appear in the search list and/or table and must be anchored in it</li>
	 * <li><b>m</b> - field is mandatory to input</li>
	 * <li><b>n</b> - negative field value must be marked</li>
	 * <li><b>N</b> - field accepts <b>null</b> as value</li>
	 * <li><b>o</b> - field is output only (will be read-only and will not catch focus)</li>
	 * <li><b>z</b> - zero field value must be marked</li>
	 * <li><b>p</b> - positive field value must be marked</li>
	 * <li><b>s</b> - all content of the field must be selected when control gets focus</li>
	 * <li><b>d</b> - field requires local editor if available</li>
	 * <li><b>&lt;</b> - field is left-aligned</li>
	 * <li><b>&gt;</b> - field is right-aligned</li>
	 * <li><b>&lt;&gt;</b> - field is adjusted</li>
	 * <li><b>&gt;&lt;</b> - field is center-aligned</li>
	 * </ul>
	 * @param clazz class to build format for. Can't be null
	 * @param format format string. Can't be null
	 * @throws NullPointerException any parameters are null
	 * @throws IllegalArgumentException illegal syntax in the format string
	 * @see https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form
	 */
	public FieldFormat(final Class<?> clazz, final String format) throws NullPointerException, IllegalArgumentException {
		this(clazz, format, "");
	}
	
	/**
	 * <p>Constructor of the class. Build format for the given class and given format string. Format string can be (in BNC):</p>
	 * @param clazz class to build format for. Can't be null
	 * @param format format string. Can't be null
	 * @param wizardType wizard type. Can't be null
	 * @throws NullPointerException any parameters are null
	 * @throws IllegalArgumentException illegal syntax in the format string
	 * @see #FieldFormat(Class, String)
	 * @since 0.0.5
	 */
	public FieldFormat(final Class<?> clazz, final String format, final String wizardType) throws NullPointerException, IllegalArgumentException {
		if (clazz == null) {
			throw new NullPointerException("Clazz to use in format can't be null");
		}
		else if (format == null) {
			throw new NullPointerException("Format string can't be null");
		}
		else if (wizardType == null) {
			throw new NullPointerException("Wizard type string can't be null");
		}
		else {
			final char[]	data = CharUtils.terminateAndConvert2CharArray(format.trim(),'\n');
			boolean			isMandatory = false, isReadOnly = false, isReadOnlyOnExistent = false, isOutput = false;
			boolean			negativeHighlight = false, zeroHighlight = false, positiveHighlight = false;
			boolean			useInList = false, useInListAnchored = false, needSelect = false;
			boolean			supportNulls = false, hasLocalEditor = false;
			String			mask = null;
			Alignment		alignment = Alignment.NoMatter;
			int				pos = 0, len = 0, frac = 0, height = 1, value;
			
			if (data[pos] == '(') {	// Parse format mask
				int	depth = 0, start = pos, count = 0;
				
				do {switch (data[pos++]) {
						case '(' :	depth++; break;
						case ')' :	depth--; break;
					}
					count++;
				} while (data[pos] != '\n' && depth > 0);
				mask = new String(data,start+1,count-2);
				try{new MaskFormatter(mask);
				} catch (ParseException e) {
					throw new IllegalArgumentException("Format mask ["+mask+"]: "+e.getLocalizedMessage()); 
				}
			}			
			
			while (data[pos] != '\n') {
				switch (data[pos]) {
					case 'r' : 
						if (isReadOnlyOnExistent) {
							throw new IllegalArgumentException("Format ["+new String(data)+"] as pos ["+pos+"]: mutually exclusive options ('r' and 'R')");
						}
						else {
							isReadOnly = true;
						}
						break;
					case 'R' :
						if (isReadOnly) {
							throw new IllegalArgumentException("Format ["+new String(data)+"] at pos ["+pos+"]: mutually exclusive options ('r' and 'R')");
						}
						else {
							isReadOnlyOnExistent = true; 
						}
						break; 
					case 'l' : 
						if (useInListAnchored) {
							throw new IllegalArgumentException("Format ["+new String(data)+"] at pos ["+pos+"]: mutually exclusive options ('l' and 'L')");
						}
						else {
							useInList = true; 
						}
						break;
					case 'L' : 
						if (useInList) {
							throw new IllegalArgumentException("Format ["+new String(data)+"] at pos ["+pos+"]: mutually exclusive options ('l' and 'L')");
						}
						else {
							useInListAnchored = true; 
						}
						break;
					case 'm' : isMandatory = true; break;
					case 'n' : negativeHighlight = true; break;
					case 'N' : supportNulls = true; break;
					case 'o' : isOutput = true; break;
					case 'z' : zeroHighlight = true; break;
					case 'p' : positiveHighlight = true; break;
					case 's' : needSelect = true; break;
					case 'd' : hasLocalEditor = true; break;
					case '<' :
						if (alignment != Alignment.NoMatter) {
							throw new IllegalArgumentException("Format ["+new String(data)+"] at pos ["+pos+"]: alignment appears more than once");
						}
						else {
							if (data[pos+1] == '>') {
								alignment = Alignment.Ajusted;
								pos++;
							}
							else {
								alignment = Alignment.LeftAlignment;
							}
						}
						break;
					case '>' :
						if (alignment != Alignment.NoMatter) {
							throw new IllegalArgumentException("Format ["+new String(data)+"] at pos ["+pos+"]: alignment appears more than once");
						}
						else {
							if (data[pos+1] == '<') {
								alignment = Alignment.CenterAlignment;
								pos++;
							}
							else {
								alignment = Alignment.RightAlignment;
							}
						}
						break;
					case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						if (len > 0) {
							throw new IllegalArgumentException("Format ["+new String(data)+"] at pos ["+pos+"]: field length appears more than once");
						}
						else {
							value = 0;
							while (data[pos] >= '0' && data[pos] <= '9') {
								value = 10 * value + data[pos++] - '0';
							}
							len = value;
							if (data[pos] == '.') {
								value = 0;
								pos++;
								while (data[pos] >= '0' && data[pos] <= '9') {
									value = 10 * value + data[pos++] - '0';
								}
								frac = value;
							}
							if (frac > 0 && frac >= len - 1) {
								throw new IllegalArgumentException("Format ["+new String(data)+"] at pos ["+pos+"]: frac part is too long");
							}
							if (data[pos] == '*') {
								value = 0;
								pos++;
								while (data[pos] >= '0' && data[pos] <= '9') {
									value = 10 * value + data[pos++] - '0';
								}
								height = value;
							}
						}
						continue;	// need skip pos++ after switch!
					default :
						throw new IllegalArgumentException("Format ["+new String(data)+"] at pos ["+pos+"]: illegal char");
				}
				pos++;
			}
			if (isMandatory && supportNulls) {
				throw new IllegalArgumentException("Mutually exclusizve options 'm' and 'N'");
			}
			this.isMandatory = isMandatory;
			this.isOutput = isOutput;
			this.isReadOnly = isReadOnly;
			this.isReadOnlyOnExistent = isReadOnlyOnExistent;
			this.negativeHighlight = negativeHighlight;
			this.zeroHighlight = zeroHighlight;
			this.positiveHighlight = positiveHighlight;
			this.needSelect = needSelect;
			this.useInList = useInList;
			this.useInListAnchored = useInListAnchored;
			this.supportNulls = supportNulls;
			this.hasLocalEditor = hasLocalEditor;
			this.alignment = alignment;
			this.length = len;
			this.height = height;
			this.frac = frac;
			this.contentType = defineContentType(clazz,mask);
			this.mask = mask;
			this.wizardType = wizardType;
		}
	}
	
	/**
	 * <p>Get content type associated with the format</p>
	 * @return content type associated. Can't be null
	 */
	public ContentType getContentType() {
		return contentType;
	}
	
	/**
	 * <p>Get format length typed</p>
	 * @return format length typed, or 0 if didn't typed
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * <p>Get format height typed</p>
	 * @return format height typed, or 1 if didn't typed
	 * @since 0.0.5
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * <p>Get format fractional typed</p>
	 * @return format fractional typed, or 0 if didn't typed 
	 */
	public int getPrecision() {
		return frac;
	}
	
	/**
	 * <p>Get format alignment</p>
	 * @return format alignment. Can't be null
	 */
	public Alignment getAlignment() {
		return alignment;
	}
	
	/**
	 * <p>Get format mask</p>
	 * @return format mask or null if didn't typed
	 */
	public String getFormatMask() {
		return mask;
	}
	
	/**
	 * <p>Is field mandatory</p>
	 * @return true if yes
	 */
	public boolean isMandatory() {
		return isMandatory;
	}

	/**
	 * <p>Is field output</p>
	 * @return true if yes
	 */
	public boolean isOutput() {
		return isOutput;
	}

	/**
	 * <p>Is field read-only</p>
	 * @param onCreation true if field is in new or duplicated record, false if field is in existent record
	 * @return true if yes
	 */
	public boolean isReadOnly(boolean onCreation) {
		return onCreation ? isReadOnlyOnExistent : isReadOnly;
	}
	
	/**
	 * <p>Is field need highlighting</p>
	 * @param sign sign to test highlighting for
	 * @return true if need
	 */
	public boolean isHighlighted(int sign) {
		return sign < 0 ? negativeHighlight : (sign > 0 ? positiveHighlight : zeroHighlight);
	}

	/**
	 * <p>Does field select it's content on focus</p> 
	 * @return true if yes
	 */
	public boolean needSelectOnFocus() {
		return needSelect;
	}
	
	/**
	 * <p>Is field used in list (indifferent to anchored)</p>
	 * @return true if yes
	 */
	public boolean isUsedInList() {
		return useInList || useInListAnchored;
	}

	/**
	 * <p>Is field used in anchored list</p>
	 * @return true if yes
	 */
	public boolean isAnchored() {
		return useInListAnchored;
	}

	/**
	 * <p>Is field support nulls</p>
	 * @return true if yes
	 */
	public boolean isNullSupported() {
		return supportNulls;
	}

	/**
	 * <p>Is field can have local editor</p>
	 * @return true if yes
	 */
	public boolean hasLocalEditor() {
		return hasLocalEditor;
	}
	
	/**
	 * <p>Get wizard type</p>
	 * @return Wizard type. Can be empty, but not null
	 * @since 0.0.5
	 */
	public String getWizardType() {
		return wizardType;
	}
	
	/**
	 * <p>Convert format to source format string</p>
	 * @return format string. Can't be null
	 * @lastUpdate 0.0.5
	 */
	public String toFormatString() {
		final StringBuilder	sb = new StringBuilder();
		
		if (mask != null) {
			sb.append('(').append(mask).append(')');
		}
		if (length > 0) {
			sb.append(length);
			if (frac > 0) {
				sb.append('.').append(frac);
			}
		}
		if (isMandatory) {
			sb.append('m');
		}
		if (isReadOnly || isReadOnlyOnExistent) {
			sb.append(isReadOnly ? 'r' : 'R');
		}
		if (useInList || useInListAnchored) {
			sb.append(useInList ? 'l' : 'L');
		}
		if (negativeHighlight) {
			sb.append('n');
		}
		if (zeroHighlight) {
			sb.append('z');
		}
		if (positiveHighlight) {
			sb.append('z');
		}
		if (supportNulls) {
			sb.append('N');
		}
		switch (alignment) {
			case Ajusted		: sb.append("<>"); break;
			case CenterAlignment: sb.append("><"); break;
			case LeftAlignment	: sb.append("<"); break;
			case RightAlignment	: sb.append(">"); break;
			case NoMatter		: break;
			default : throw new UnsupportedOperationException("Alignment ["+alignment+"] is not supported yet");
		}
		if (isOutput) {
			sb.append('o');
		}
		if (needSelect) {
			sb.append('s');
		}

		if (!wizardType.isEmpty()) {
			sb.append(" wizard=").append(wizardType);
		}
		return sb.toString();
	}

	/**
	 * <p>Print value to string representation by the given format</p>
	 * @param value value to print
	 * @param mode mode to print
	 * @return String printed. Can't be null
	 * @throws NullPointerException when any parameter is null
	 * @throws SyntaxException on syntax errors during print
	 * @since 0.0.4
	 */
	public String print(final Object value, final PrintMode mode) throws NullPointerException, SyntaxException {
		if (value == null) {
			throw new NullPointerException("Value to print can't be null"); 
		}
		else if (mode == null) {
			throw new NullPointerException("Print mode can't be null"); 
		}
		else {
			String	result = "";
			
			switch (mode) {
				case CREOLE_TEXT	:
					return "{{{"+print(value,PrintMode.SINGLE_TEXT)+"}}}";
				case HTML_TEXT		:
					break;
				case SINGLE_TEXT	:
					if (value instanceof Number) {
						if (getFormatMask() != null) {
							try{result = new MaskFormatter(getFormatMask()).valueToString(value);
							} catch (ParseException e) {
								throw new SyntaxException(0,0,"Print format ["+getFormatMask()+"], error: "+e.getLocalizedMessage());
							}
						}
						else if (getPrecision() > 0) {
							result = String.format("%1$"+getLength()+"."+getPrecision()+"f",value);
						}
						else {
							if ((value instanceof BigDecimal) || (value instanceof Double) || (value instanceof Float)) {
								result = String.format("%1$"+getLength()+".0f",value);
							}
							else {
								result = String.format("%1$"+getLength()+"d",value);
							}
						}
					}
					else {
						result = value.toString();
					}
					break;
				default	:
					throw new UnsupportedOperationException("Print mode ["+mode+"] is not supported yet");
			}
			switch (getAlignment()) {
				case RightAlignment		:
					if (result.length() < getLength()) {
						result = new String(CharUtils.space(getLength()-result.length()))+result; 
					}
					break;
				case CenterAlignment	:
					if (result.length() < getLength()) {
						final String	bound = new String(CharUtils.space((getLength()-result.length())/2));
						
						return bound+result+bound;
					}
					break;
				case LeftAlignment		:
				case Ajusted			:
					if (result.length() < getLength()) {
						result = result+new String(CharUtils.space(getLength()-result.length())); 
					}
					break;
				case NoMatter			:
					if (getLength() >= 0) {
						if (result.length() > getLength()) {
							result = result.substring(0,getLength());
						}
					}
					break;
				default	:
					throw new UnsupportedOperationException("Alignment ["+getAlignment()+"] is not supported yet");
			}
			return result;
		}
	}

	
	static ContentType defineContentType(final Class<?> clazz, final String mask) {
		switch (CompilerUtils.defineClassType(clazz)) {
			case CompilerUtils.CLASSTYPE_REFERENCE	:
				if (clazz.isEnum()) {
					return ContentType.EnumContent;
				}
				else if (clazz.isArray()) {
					return clazz.getComponentType() == char.class ? ContentType.PasswordContent : ContentType.ArrayContent;
				}
				else if (String.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)) {
					if (mask == null) {
						return ContentType.StringContent;
					}
					else {
						return ContentType.FormattedStringContent;
					}
				}
				else if (BigInteger.class.isAssignableFrom(clazz) || clazz == Byte.class || clazz == Short.class || clazz == Integer.class || clazz == Long.class) {
					return ContentType.IntegerContent;
				}
				else if (BigDecimal.class.isAssignableFrom(clazz) || clazz == Float.class || clazz == Double.class) {
					return ContentType.NumericContent;
				}
				else if (Timestamp.class.isAssignableFrom(clazz)) {
					return ContentType.TimestampContent;
				}
				else if (Date.class.isAssignableFrom(clazz) || Calendar.class.isAssignableFrom(clazz)) {
					return ContentType.DateContent;
				}
				else if (Boolean.class.isAssignableFrom(clazz)) {
					return ContentType.BooleanContent;
				}
				else if (File.class.isAssignableFrom(clazz) || FileSystemInterface.class.isAssignableFrom(clazz) || FileKeeper.class.isAssignableFrom(clazz)) {
					return ContentType.FileContent;
				}
				else if (URI.class.isAssignableFrom(clazz)) {
					return ContentType.URIContent;
				}
				else if (Color.class.isAssignableFrom(clazz) || ColorKeeper.class.isAssignableFrom(clazz)) {
					return ContentType.ColorContent;
				}
				else if (ColorPair.class.isAssignableFrom(clazz)) {
					return ContentType.ColorPairContent;
				}
				else if (Image.class.isAssignableFrom(clazz) || ImageKeeper.class.isAssignableFrom(clazz)) {
					return ContentType.ImageContent;
				}
				else if (LongItemAndReference.class.isAssignableFrom(clazz)) {
					return ContentType.ForeignKeyRefContent;
				}
				else if (LongItemAndReferenceList.class.isAssignableFrom(clazz)) {
					return ContentType.ForeignKeyRefListContent;
				}
				else if (MimeBasedContent.class.isAssignableFrom(clazz)) {
					return ContentType.MimeBasedContent;
				}
				else  {
					return ContentType.Unclassified;
				}
			case CompilerUtils.CLASSTYPE_BYTE : case CompilerUtils.CLASSTYPE_SHORT : case CompilerUtils.CLASSTYPE_INT : case CompilerUtils.CLASSTYPE_LONG :	
				return ContentType.IntegerContent;
			case CompilerUtils.CLASSTYPE_FLOAT : case CompilerUtils.CLASSTYPE_DOUBLE	:	
				return ContentType.NumericContent;
			case CompilerUtils.CLASSTYPE_CHAR	:	
				return ContentType.StringContent;
			case CompilerUtils.CLASSTYPE_BOOLEAN:	
				return ContentType.BooleanContent;
			default : throw new UnsupportedOperationException("Class type ["+CompilerUtils.defineClassType(clazz)+"] is not supported yet");
		}
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alignment == null) ? 0 : alignment.hashCode());
		result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result + frac;
		result = prime * result + (isMandatory ? 1231 : 1237);
		result = prime * result + (isOutput ? 1231 : 1237);
		result = prime * result + (isReadOnly ? 1231 : 1237);
		result = prime * result + (isReadOnlyOnExistent ? 1231 : 1237);
		result = prime * result + length;
		result = prime * result + ((mask == null) ? 0 : mask.hashCode());
		result = prime * result + (needSelect ? 1231 : 1237);
		result = prime * result + (negativeHighlight ? 1231 : 1237);
		result = prime * result + (positiveHighlight ? 1231 : 1237);
		result = prime * result + (supportNulls ? 1231 : 1237);
		result = prime * result + (useInList ? 1231 : 1237);
		result = prime * result + (useInListAnchored ? 1231 : 1237);
		result = prime * result + (zeroHighlight ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FieldFormat other = (FieldFormat) obj;
		if (alignment != other.alignment) return false;
		if (contentType != other.contentType) return false;
		if (frac != other.frac) return false;
		if (isMandatory != other.isMandatory) return false;
		if (isOutput != other.isOutput) return false;
		if (isReadOnly != other.isReadOnly) return false;
		if (isReadOnlyOnExistent != other.isReadOnlyOnExistent) return false;
		if (length != other.length) return false;
		if (mask == null) {
			if (other.mask != null) return false;
		} else if (!mask.equals(other.mask)) return false;
		if (needSelect != other.needSelect) return false;
		if (negativeHighlight != other.negativeHighlight) return false;
		if (positiveHighlight != other.positiveHighlight) return false;
		if (supportNulls != other.supportNulls) return false;
		if (useInList != other.useInList) return false;
		if (useInListAnchored != other.useInListAnchored) return false;
		if (zeroHighlight != other.zeroHighlight) return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldFormat [contentType=" + contentType + ", alignment=" + alignment + ", length=" + length + ", frac="
				+ frac + ", mask=" + mask + ", isMandatory=" + isMandatory + ", isReadOnly=" + isReadOnly
				+ ", isReadOnlyOnExistent=" + isReadOnlyOnExistent + ", negativeHighlight=" + negativeHighlight
				+ ", zeroHighlight=" + zeroHighlight + ", positiveHighlight=" + positiveHighlight + ", needSelect="
				+ needSelect + ", useInList=" + useInList + ", useInListAnchored=" + useInListAnchored + ", isOutput="
				+ isOutput + ", supportNulls=" + supportNulls + "]";
	}
}
