package chav1961.purelib.ui;

import chav1961.purelib.basic.exceptions.SyntaxException;

/**
 * <p>This class is used in conjuntion with the {@linkplain AbstractLowLevelFormFactory} class to describe low lever for field formats. Field format is a single char sequence.
 * Every character in the sequence means:</p> 
 * <ul>
 * <li>m - mandatory field. Marks with the color. Requires any non-empty value on input</li>  
 * <li>r - read-only field. Marks with the color. Can't be edited.</li>  
 * <li>R - read-only field for all existent records. Newly created and duplicated record remains the field writable.</li>  
 * <li>n - negative value need be marked by color.</li>  
 * <li>z - zero value need be marked by color.</li>  
 * <li>p - positive value need be marked by color.</li>  
 * <li>l - field need be used in the list representation of the form.</li>  
 * <li>L - field need be used in the list representation of the form. It's column fixes at the left of the list and prevents left/right scrolling</li>  
 * <li>s - select all field content when gain focus</li>  
 * </ul>  
 * @author Alexander Chernomyrdin aka chav1961
 * @see AbstractLowLevelFormFactory 
 * @since 0.0.2
 */

public class FormFieldFormat {
	private int			len = 0;
	private int			frac = 0;
	private boolean		mandatory = false;
	private boolean		readOnly = false;
	private boolean		readOnlyOnExistent = false;
	private boolean		negativeMarked = false;
	private boolean		zeroMarked = false;
	private boolean		positiveMarked = false;
	private boolean		usedInList = false;
	private boolean		usedInListAnchored = false;
	private boolean		selectAllContent = false;
	private String		wizardType = "";	
	private Class<?>	contentType = Object.class;	

	public FormFieldFormat() throws SyntaxException {
	}

	public FormFieldFormat(final String format) throws SyntaxException, NullPointerException {
		if (format == null) {
			throw new NullPointerException("Format string can't be null");
		}
		else {
			parseFormat(format.toCharArray(),0,format.length());
		}
	}
	
	public FormFieldFormat(final char[] format) throws SyntaxException, NullPointerException {
		if (format == null) {
			throw new NullPointerException("Format char sequence can't be null");
		}
		else {
			parseFormat(format,0,format.length);
		}
	}
	
	public FormFieldFormat(final char[] format, final int from, final int length) throws SyntaxException, NullPointerException, IllegalArgumentException {
		if (format == null) {
			throw new NullPointerException("Format char sequence can't be null");
		}
		else if (from < 0 || from >= format.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(format.length-1));
		}
		else if (from+length < 0 || from+length > format.length) {
			throw new IllegalArgumentException("From position + length ["+(from+length)+"] out of range 0.."+format.length);
		}
		else {
			parseFormat(format,from,length);
		}
	}

	public FormFieldFormat(final String format, final String wizardType, final Class<?> contentType) throws SyntaxException, NullPointerException, IllegalArgumentException {
		if (format == null) {
			throw new NullPointerException("Format string can't be null");
		}
		else if (wizardType == null) {
			throw new NullPointerException("Wizard type can't be null. Use empty string instead");
		}
		else if (contentType == null) {
			throw new NullPointerException("Content type can't be null.");
		}
		else {
			parseFormat(format.toCharArray(),0,format.length());
			this.wizardType = wizardType;
			this.contentType = contentType;
		}
	}

	public FormFieldFormat(final char[] format, final String wizardType, final Class<?> contentType) throws SyntaxException, NullPointerException, IllegalArgumentException {
		if (format == null) {
			throw new NullPointerException("Format string can't be null");
		}
		else if (wizardType == null) {
			throw new NullPointerException("Wizard type can't be null. Use empty string instead");
		}
		else if (contentType == null) {
			throw new NullPointerException("Content type can't be null.");
		}
		else {
			parseFormat(format,0,format.length);
			this.wizardType = wizardType;
			this.contentType = contentType;
		}
	}
	
	public FormFieldFormat(final char[] format, final int from, final int length, final String wizardType, final Class<?> contentType) throws SyntaxException, NullPointerException, IllegalArgumentException {
		if (format == null) {
			throw new NullPointerException("Format char sequence can't be null");
		}
		else if (from < 0 || from >= format.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(format.length-1));
		}
		else if (from+length < 0 || from+length > format.length) {
			throw new IllegalArgumentException("From position + length ["+(from+length)+"] out of range 0.."+format.length);
		}
		else if (wizardType == null) {
			throw new NullPointerException("Wizard type can't be null. Use empty string instead");
		}
		else if (contentType == null) {
			throw new NullPointerException("Content type can't be null.");
		}
		else {
			parseFormat(format,from,length);
			this.wizardType = wizardType;
			this.contentType = contentType;
		}
	}
	
	public int getLen() {
		return len;
	}

	public int getFrac() {
		return frac;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public boolean isReadOnlyOnExistent() {
		return readOnlyOnExistent;
	}

	public boolean isNegativeMarked() {
		return negativeMarked;
	}

	public boolean isZeroMarked() {
		return zeroMarked;
	}

	public boolean isPositiveMarked() {
		return positiveMarked;
	}

	public boolean isUsedInList() {
		return usedInList;
	}

	public boolean isUsedInListAnchored() {
		return usedInListAnchored;
	}

	public boolean isSelectAllContent() {
		return selectAllContent;
	}

	public String getWizardType() {
		return wizardType;
	}

	public Class<?> getContentType() {
		return contentType;
	}

	private void parseFormat(final char[] format, final int from, final int length) throws SyntaxException {
		int		index = 0;
		
		while (index < length && format[from+index] >= '0' && format[from+index] <= '9') {
			len = 10 * len + format[from+index] - '0';
			index++;
		}
		
		if (index < length && format[from+index] == '.') {
			index++;
			while (index < length && format[from+index] >= '0' && format[from+index] <= '9') {
				frac = 10 * frac + format[from+index] - '0';
				index++;
			}
		}
		
		if (len < frac) {
			throw new SyntaxException(0,index,"Field length ["+len+"] less than fractional part ["+frac+"]"); 
		}
		
		while (index < length) {
			switch (format[from+index]) {
				case 'm' :	mandatory = true; break;
				case 'r' :	readOnly = true; break;
				case 'R' :	readOnlyOnExistent = true; break;
				case 'n' :	negativeMarked = true; break; 
				case 'z' :	zeroMarked = true; break;
				case 'p' :	positiveMarked = true; break;
				case 'l' :	usedInList = true; break;
				case 'L' :	usedInListAnchored = true; break;
				case 's' :	selectAllContent = true; break;
				default : throw new SyntaxException(0,index,"Illegal char ["+format[from+index]+"] in the format sequence '"+new String(format,from,length)+"'");
			}
			index++;
		}
	}

	@Override
	public String toString() {
		return "FormFieldFormat [len=" + len + ", frac=" + frac + ", mandatory=" + mandatory + ", readOnly=" + readOnly
				+ ", readOnlyOnExistent=" + readOnlyOnExistent + ", negativeMarked=" + negativeMarked + ", zeroMarked="
				+ zeroMarked + ", positiveMarked=" + positiveMarked + ", usedInList=" + usedInList
				+ ", usedInListAnchored=" + usedInListAnchored + ", selectAllContent=" + selectAllContent
				+ ", wizardType=" + wizardType + ", contentType=" + contentType + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + frac;
		result = prime * result + len;
		result = prime * result + (mandatory ? 1231 : 1237);
		result = prime * result + (negativeMarked ? 1231 : 1237);
		result = prime * result + (positiveMarked ? 1231 : 1237);
		result = prime * result + (readOnly ? 1231 : 1237);
		result = prime * result + (readOnlyOnExistent ? 1231 : 1237);
		result = prime * result + (usedInList ? 1231 : 1237);
		result = prime * result + (usedInListAnchored ? 1231 : 1237);
		result = prime * result + (zeroMarked ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FormFieldFormat other = (FormFieldFormat) obj;
		if (frac != other.frac) return false;
		if (len != other.len) return false;
		if (mandatory != other.mandatory) return false;
		if (negativeMarked != other.negativeMarked) return false;
		if (positiveMarked != other.positiveMarked) return false;
		if (readOnly != other.readOnly) return false;
		if (readOnlyOnExistent != other.readOnlyOnExistent) return false;
		if (usedInList != other.usedInList) return false;
		if (usedInListAnchored != other.usedInListAnchored) return false;
		if (zeroMarked != other.zeroMarked) return false;
		return true;
	}
}