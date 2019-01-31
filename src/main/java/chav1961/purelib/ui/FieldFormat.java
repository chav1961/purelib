package chav1961.purelib.ui;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class FieldFormat {
	public enum ContentType {
		StringContent,
		FormattedStringContent,
		IntegerContent,
		NumericContent,
		DateContent,
		TimestampContent,
		BooleanContent,
		EnumContent,
		FileContent,
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
	
	public FieldFormat(final Class<?> clazz) throws SyntaxException {
		this(clazz,"");
	}
	
	public FieldFormat(final Class<?> clazz, final String format) throws SyntaxException {
		if (clazz == null) {
			throw new NullPointerException("Clazz to use in format can't be null");
		}
		else if (format == null) {
			throw new NullPointerException("Format string can't be null");
		}
		else {
			final char[]	data = (format.trim()+'\n').toCharArray();
			boolean			isMandatory = false, isReadOnly = false, isReadOnlyOnExistent = false;
			boolean			negativeHighlight = false, zeroHighlight = false, positiveHighlight = false;
			boolean			useInList = false, useInListAnchored = false, needSelect = false;
			Alignment		alignment = Alignment.NoMatter;
			int				pos = 0, len = 0, frac = 0, value;
			
			while (data[pos] != '\n') {
				switch (data[pos]) {
					case 'm' : isMandatory = true; break;
					case 'r' : isReadOnly = true; break;
					case 'R' : isReadOnlyOnExistent = true; break; 
					case 'n' : negativeHighlight = true; break;
					case 'z' : zeroHighlight = true; break;
					case 'p' : positiveHighlight = true; break;
					case 'l' : useInList = true; break;
					case 'L' : useInListAnchored = true; break;
					case 's' : needSelect = true; break;
					case '<' :
						if (data[pos+1] == '>') {
							alignment = Alignment.Ajusted;
							pos++;
						}
						else {
							alignment = Alignment.LeftAlignment;
						}
						break;
					case '>' :
						if (data[pos+1] == '<') {
							alignment = Alignment.CenterAlignment;
							pos++;
						}
						else {
							alignment = Alignment.RightAlignment;
						}
						break;
					case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						value = 0;
						while (Character.isDigit(data[pos])) {
							value = 10 * value + data[pos++] - '0';
						}
						len = value;
						if (data[pos] == '.') {
							value = 0;
							pos++;
							while (Character.isDigit(data[pos])) {
								value = 10 * value + data[pos++] - '0';
							}
							frac = value;
						}
						pos--;
						break;
					default :
						throw new SyntaxException(0,pos,"Format ["+new String(data)+"]: illegal char");
				}
				pos++;
			}
			this.isMandatory = isMandatory;
			this.isReadOnly = isReadOnly;
			this.isReadOnlyOnExistent = isReadOnlyOnExistent;
			this.negativeHighlight = negativeHighlight;
			this.zeroHighlight = zeroHighlight;
			this.positiveHighlight = positiveHighlight;
			this.needSelect = needSelect;
			this.useInList = useInList;
			this.useInListAnchored = useInListAnchored;
			this.alignment = alignment;
			this.length = len;
			this.frac = frac;
			this.contentType = defineContentType(clazz);
			this.mask = buildFormatMask();
		}
	}
	
	public ContentType getContentType() {
		return null;
	}
	
	public int getLength() {
		return 0;
	}
	
	public int getPrecision() {
		return 0;
	}
	
	public Alignment getAlignment() {
		return null;
	}
	
	public String getFormatMask() {
		return null;
	}
	
	public boolean isMandatory() {
		return false;
	}

	public boolean isReadOnly(boolean onCreation) {
		return false;
	}
	
	public boolean isHighlighted(int sign) {
		return false;
	}

	public boolean needSelectOnFocus() {
		return false;
	}
	
	public boolean isUsedInList() {
		return false;
	}

	public boolean isAnchored() {
		return false;
	}

	private String buildFormatMask() {
		// TODO Auto-generated method stub
		return null;
	}

	private ContentType defineContentType(final Class<?> clazz) {
		switch (Utils.defineClassType(clazz)) {
			case Utils.CLASSTYPE_REFERENCE	:
				if (clazz.isEnum()) {
					return ContentType.EnumContent;
				}
				else if (clazz.isArray()) {
					return ContentType.ArrayContent;
				}
				else if (clazz.isAssignableFrom(String.class) || clazz == Character.class) {
					return ContentType.StringContent;
				}
				else if (clazz.isAssignableFrom(BigInteger.class) || clazz == Byte.class || clazz == Short.class || clazz == Integer.class || clazz == Long.class) {
					return ContentType.IntegerContent;
				}
				else if (clazz.isAssignableFrom(BigDecimal.class) || clazz == Float.class || clazz == Double.class) {
					return ContentType.NumericContent;
				}
				else if (clazz.isAssignableFrom(Date.class) || clazz.isAssignableFrom(Calendar.class)) {
					return ContentType.DateContent;
				}
				else if (clazz.isAssignableFrom(Timestamp.class)) {
					return ContentType.TimestampContent;
				}
				else if (clazz.isAssignableFrom(Boolean.class)) {
					return ContentType.BooleanContent;
				}
				else if (clazz.isAssignableFrom(File.class) || clazz.isAssignableFrom(FileSystemInterface.class)) {
					return ContentType.FileContent;
				}
				else  {
					return ContentType.Unclassified;
				}
			case Utils.CLASSTYPE_BYTE : case Utils.CLASSTYPE_SHORT : case Utils.CLASSTYPE_INT : case Utils.CLASSTYPE_LONG :	
				return ContentType.IntegerContent;
			case Utils.CLASSTYPE_FLOAT : case Utils.CLASSTYPE_DOUBLE	:	
				return ContentType.NumericContent;
			case Utils.CLASSTYPE_CHAR	:	
				return ContentType.StringContent;
			case Utils.CLASSTYPE_BOOLEAN:	
				return ContentType.BooleanContent;
			default : throw new UnsupportedOperationException("Class type ["+Utils.defineClassType(clazz)+"] is not supported yet");
		}
	}

	@Override
	public String toString() {
		return "FieldFormat [contentType=" + contentType + ", alignment=" + alignment + ", length=" + length + ", frac="
				+ frac + ", mask=" + mask + ", isMandatory=" + isMandatory + ", isReadOnly=" + isReadOnly
				+ ", isReadOnlyOnExistent=" + isReadOnlyOnExistent + ", negativeHighlight=" + negativeHighlight
				+ ", zeroHighlight=" + zeroHighlight + ", positiveHighlight=" + positiveHighlight + ", needSelect="
				+ needSelect + ", useInList=" + useInList + ", useInListAnchored=" + useInListAnchored + "]";
	}
}
