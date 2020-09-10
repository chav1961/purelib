package chav1961.purelib.streams.char2char.intern;

import java.util.Arrays;

import chav1961.purelib.basic.exceptions.SyntaxException;

class PPSubst implements Substituter {
	private final int		argNo;
	private final String	format;
	private String[]		parsed = null;
	
	public PPSubst(final String format) {
		if (format == null || format.isEmpty()) {
			throw new IllegalArgumentException("Format can't be null");
		}
		else {
			this.format = format;
			this.argNo = 0;
		}
	}

	public PPSubst(final int argNo, final String format) {
		if (argNo <= 0) {
			throw new IllegalArgumentException("ArgNo need be positive");
		}
		else if (format == null || format.isEmpty()) {
			throw new IllegalArgumentException("Format can't be null");
		}
		else {
			this.argNo = argNo;
			this.format = format;
		}
	}

	@Override
	public int match(final char[] data, int from) throws SyntaxException {
		final int	endData;
		
		if (data == null || (endData = data.length) == 0) {
			throw new IllegalArgumentException("Data can't be null or empty array ");
		}
		else if (from < 0 || from >= endData) {
			throw new IllegalArgumentException("From position ["+from+"] outside the range 0.."+endData);
		}
		else if (argNo == 0) {
			parsed = new String[0];
			return -1;
		}
		else {
			from = Util.skipBlank(data,Util.skipName(data,from));
			
			if (from < endData && data[from] == '(') {
				int			parsedNo = 0;
				
				parsed = new String[argNo]; 
				do {from = Util.skipBlank(data,from+1);
					final int	startParm = from, endParm = from = Util.skipColon(data,from);
					
					if (parsedNo < argNo) {
						parsed[parsedNo++] = new String(data,startParm,endParm-startParm);
					}
					else {
						return -1;
					}
					from = Util.skipBlank(data,from);
				} while (from < endData && data[from] == ',');
				
				return from < endData && data[from] == ')' ? from+1 : -1;
			}
			else {
				return -1;
			}
		}
	}

	@Override
	public String substitute() {
		if (parsed == null) {
			throw new IllegalStateException("Substitute without match! Call match firstly");
		}
		else {
			final String	result = String.format(format,(Object[])parsed);
			
			parsed = null;
			return result;
		}
	}

	@Override
	public String toString() {
		return "PPSubst [argNo=" + argNo + ", format=" + format + ", parsed=" + Arrays.toString(parsed) + "]";
	}
}