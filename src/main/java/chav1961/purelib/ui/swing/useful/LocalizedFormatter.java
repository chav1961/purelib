package chav1961.purelib.ui.swing.useful;

import java.util.Arrays;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;

public class LocalizedFormatter {
	private final String	format;
	private final Object[]	parameters;
	
	public LocalizedFormatter(final String format, final Object... parameters) {
		if (format == null || format.isEmpty()) {
			throw new IllegalArgumentException("Format string can't be null or empty"); 
		}
		else {
			this.format = format;
			this.parameters = parameters;
		}
	}
	
	public String toString(final Localizer localizer) throws LocalizationException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (parameters == null || parameters.length == 0) {
			return localizer.getValue(format);
		}
		else {
			return String.format(localizer.currentLocale().getLocale(),localizer.getValue(format),parameters);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + Arrays.deepHashCode(parameters);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		LocalizedFormatter other = (LocalizedFormatter) obj;
		if (format == null) {
			if (other.format != null) return false;
		} else if (!format.equals(other.format)) return false;
		if (!Arrays.deepEquals(parameters, other.parameters)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "LocalizedFormatter [format=" + format + ", parameters=" + Arrays.toString(parameters) + "]";
	}
}