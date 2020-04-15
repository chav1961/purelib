package chav1961.purelib.model;

import java.awt.Color;
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
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.streams.char2byte.CompilerUtils;
import chav1961.purelib.ui.ColorPair;

public class FieldFormat {
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
		Unclassified
	} 
	
	public enum Alignment {
		LeftAlignment,
		RightAlignment,
		CenterAlignment,
		Ajusted,
		NoMatter
	}

	private final ContentType	contentType;
	private final Alignment		alignment;
	private final int			length;
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
	
	public FieldFormat(final Class<?> clazz) throws NullPointerException, IllegalArgumentException {
		this(clazz,"");
	}
	
	public FieldFormat(final Class<?> clazz, final String format) throws NullPointerException, IllegalArgumentException {
		if (clazz == null) {
			throw new NullPointerException("Clazz to use in format can't be null");
		}
		else if (format == null) {
			throw new NullPointerException("Format string can't be null");
		}
		else {
			final char[]	data = CharUtils.terminateAndConvert2CharArray(format.trim(),'\n');
			boolean			isMandatory = false, isReadOnly = false, isReadOnlyOnExistent = false, isOutput = false;
			boolean			negativeHighlight = false, zeroHighlight = false, positiveHighlight = false;
			boolean			useInList = false, useInListAnchored = false, needSelect = false;
			boolean			supportNulls = false;
			String			mask = null;
			Alignment		alignment = Alignment.NoMatter;
			int				pos = 0, len = 0, frac = 0, value;
			
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
						}
						continue;	// need skip pos++ after switch!
					default :
						throw new IllegalArgumentException("Format ["+new String(data)+"] at pos ["+pos+"]: illegal char");
				}
				pos++;
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
			this.alignment = alignment;
			this.length = len;
			this.frac = frac;
			this.contentType = defineContentType(clazz,mask);
			this.mask = mask;
		}
	}
	
	public ContentType getContentType() {
		return contentType;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getPrecision() {
		return frac;
	}
	
	public Alignment getAlignment() {
		return alignment;
	}
	
	public String getFormatMask() {
		return mask;
	}
	
	public boolean isMandatory() {
		return isMandatory;
	}

	public boolean isOutput() {
		return isOutput;
	}
	
	public boolean isReadOnly(boolean onCreation) {
		return onCreation ? isReadOnlyOnExistent : isReadOnly;
	}
	
	public boolean isHighlighted(int sign) {
		return sign < 0 ? negativeHighlight : (sign > 0 ? positiveHighlight : zeroHighlight);
	}

	public boolean needSelectOnFocus() {
		return needSelect;
	}
	
	public boolean isUsedInList() {
		return useInList;
	}

	public boolean isAnchored() {
		return useInListAnchored;
	}

	public boolean isNullSupported() {
		return supportNulls;
	}
	
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
		
		return sb.toString();
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
				else if (File.class.isAssignableFrom(clazz) || FileSystemInterface.class.isAssignableFrom(clazz)) {
					return ContentType.FileContent;
				}
				else if (URI.class.isAssignableFrom(clazz)) {
					return ContentType.URIContent;
				}
				else if (Color.class.isAssignableFrom(clazz)) {
					return ContentType.ColorContent;
				}
				else if (ColorPair.class.isAssignableFrom(clazz)) {
					return ContentType.ColorPairContent;
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
