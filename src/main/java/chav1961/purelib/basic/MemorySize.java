package chav1961.purelib.basic;

/**
 * <p>This class is an utility class to work with string representation of memory size. It supports memory size units to parse.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @thread.safe
 */
public class MemorySize implements Comparable<MemorySize> {
	/**
	 * <p>Enumeration to type memory size units</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static enum SizeUnit {
		BYTE("", 1), 
		KBYTE("k", 1024), 
		MBYTE("m", 1024 * 1024), 
		GBYTE("g", 1024 * 1204 * 1024),
		TBYTE("t", 1024 * 1204 * 1024 * 1024);
		
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

	private final long		value;
	private final SizeUnit	unit;

	/**
	 * <p>Constructor of the class</p>
	 * @param value memory value in bytes. Can't be negative
	 * @throws IllegalArgumentException memory value is less than 0
	 */
	public MemorySize(final long value) throws IllegalArgumentException {
		this(value, SizeUnit.BYTE);
	}	
	
	/**
	 * <p>Constructor of the class</p>
	 * @param value memory size in units selected. Can't be negative
	 * @param unit units selected. Can't be null
	 * @throws IllegalArgumentException memory value is less than 0
	 * @throws NullPointerException units parameter is null
	 */
	public MemorySize(final long value, final SizeUnit unit) throws IllegalArgumentException, NullPointerException {
		if (value < 0) {
			throw new IllegalArgumentException("Size value can't be negative");
		}
		else if (unit == null) {
			throw new NullPointerException("Size unit can't be null");
		}
		else {
			this.value = value * unit.getByteScale();
			this.unit = unit;
		}
	}

	@Override
	public int compareTo(final MemorySize o) {
		if (o == null) {
			throw new NullPointerException("Memory size to compare can't be null");
		}
		else {
			final long result = o.getByteSize() - this.getByteSize();
			
			return result < 0 ? -1 : (result > 0 ? 1 : 0);
		}
	}
	
	/**
	 * <p>Get memory size in bytes</p>
	 * @return memory size in bytes
	 */
	public long getByteSize() {
		return value;
	}

	/**
	 * <p>Get memory size in units typed on constructor</p>
	 * @return size in units typed on constructor 
	 */
	public long getSize() {
		return value/unit.getByteScale();
	}
	
	/**
	 * <p>Get memory size units typed on constructor</p>
	 * @return size units typed on constructor. Can't be null
	 */
	public SizeUnit getUnit() {
		return unit;
	}
	
	/**
	 * <p>Get memory size in units selected</p>
	 * @param unit units selected to get memory size. Can't be null
	 * @return memory size in units selected
	 * @throws NullPointerException units parameter is null
	 */
	public long getSize(final SizeUnit unit) {
		if (unit == null) {
			throw new NullPointerException("Size unit can't be null");
		}
		else {
			return getByteSize()/getUnit().getByteScale();
		}
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + (int) (value ^ (value >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MemorySize other = (MemorySize) obj;
		if (unit != other.unit) return false;
		if (value != other.value) return false;
		return true;
	}

	@Override
	public String toString() {
		return "MemorySize [value=" + value + ", unit=" + unit + "]";
	}

	/**
	 * <p>Parse memory size string representation
	 * @param value
	 * @return
	 */
	public static MemorySize valueOf(final CharSequence value) {
		if (Utils.checkEmptyOrNullString(value)) {
			throw new IllegalArgumentException("Value to parse can't be null or empty");
		}
		else {
			final String 	val = CharUtils.toString(value).trim().toLowerCase();
			long			size = 0;
			SizeUnit		unit = SizeUnit.BYTE;
			int				from = 0;
			char			symbol;
			
			while (from < val.length() && Character.isDigit(symbol = val.charAt(from))) {
				size = 10 * size + symbol - '0';
				from++;
			}
			
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
			return new MemorySize(size, unit);
		}
	}

}
