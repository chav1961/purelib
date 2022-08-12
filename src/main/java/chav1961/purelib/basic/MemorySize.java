package chav1961.purelib.basic;

public class MemorySize {
	public static enum SizeUnit {
		BYTE("", 1), 
		KBYTE("k", 1024), 
		MBYTE("m", 1024 * 1024), 
		GBYTE("g", 1024 * 1204 * 1024);
		
		private final String	suffix;
		private final long		scale;
		
		private SizeUnit(final String suffix, final long scale) {
			this.suffix = suffix;
			this.scale = scale;
		}
		
		public String getUnitSuffix() {
			return suffix;
		}
		
		public long getByteScale() {
			return scale;
		}
	}

	public MemorySize(final long value) {
		this(value, SizeUnit.BYTE);
	}	
	
	private final long		value;
	private final SizeUnit	unit;
	
	public MemorySize(final long value, final SizeUnit unit) {
		if (value < 0) {
			throw new IllegalArgumentException("Size value can't be negative");
		}
		else if (unit == null) {
			throw new IllegalArgumentException("Size unit can't be null");
		}
		else {
			this.value = value * unit.getByteScale();
			this.unit = unit;
		}
	}
	
	public long getByteSize() {
		return value;
	}

	public long getSize() {
		return value/unit.getByteScale();
	}
	
	public long getSize(final SizeUnit unit) {
		return getSize()/getUnit().getByteScale();
	}

	public SizeUnit getUnit() {
		return unit;
	}
	
	public static MemorySize valueOf(final String value) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("Value to parse can't be null or empty");
		}
		else {
			final String 	val = value.trim().toLowerCase();
			int				from = 0;
			
			while (from < val.length() && Character.isDigit(val.charAt(from))) {
				from++;
			}
			final long		result = Long.valueOf(from < val.length()-1 ? val.substring(0, from) : val);
			SizeUnit		unit = SizeUnit.BYTE;
			
			if (from <= val.length()-1) {
				boolean		found = false;
				
				for (SizeUnit item : SizeUnit.values()) {
					if (!item.getUnitSuffix().isEmpty() && val.endsWith(item.getUnitSuffix())) {
						unit = item;
						found = true;
						break;
					}
				}
				if (!found) {
					throw new IllegalArgumentException("Illegal unit suffix in the input ["+value+"]");
				}
			}
			return new MemorySize(result, unit);
		}
	}
}
