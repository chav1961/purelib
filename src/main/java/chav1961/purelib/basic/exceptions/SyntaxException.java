package chav1961.purelib.basic.exceptions;

/**
 * <p>This exception describes any problems on syntax during parsing any kind of data. It's a special form of exception, that
 * contains explicit row and column numbers for better problem localization. It always uses in the case, when we need localize 
 * syntax problems in some entity</p>
 * <p>To support some specific behavior on syntax error throwing, this class contains a set of static methods to simplify creation of
 * {@linkplain SyntaxException} instances. You can use it to build error message description for you instance</p>  
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1 
 * @lastUpdate 0.0.4
 */
public class SyntaxException extends ContentException {
	private static final long serialVersionUID = 8141880743233589596L;
	
	private final long	lineNo, pos;

	/**
	 * <p>Constructor of the class</p>
	 * @param lineNo line inside content where problem was detected
	 * @param pos position inside content where problem was detected
	 * @param message problem description
	 * @param t exception cause
	 */
	public SyntaxException(final long lineNo, final long pos, final String message, final Throwable t) {
		super("Line "+lineNo+", pos "+pos+": "+message,t);
		this.lineNo = lineNo;
		this.pos = pos;
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param lineNo line inside content where problem was detected
	 * @param pos position inside content where problem was detected
	 * @param message problem description
	 */
	public SyntaxException(final long lineNo, final long pos, final String message) {
		super("Line "+lineNo+", pos "+pos+": "+message);
		this.lineNo = lineNo;
		this.pos = pos;
	}

	/**
	 * <p>Get row (line) where problem was detected</p>
	 * @return zero-based line where problem was detected. Is't strongly recommended to use 0 for single-line content 
	 */
	public long getRow() {
		return lineNo;
	}
	
	/**
	 * <p>Get position (column) where problem was detected</p>
	 * @return zero-based column where problem was detected
	 */
	public long getCol() {
		return pos;
	}
	
	/**
	 * <p>Extract string fragment to show in error description as tooltip (location-fragmentSize..location+fragmentSize will be extracted).</p>
	 * @param source string to extract fragment from
	 * @param row row of the fragment
	 * @param col column of the fragment
	 * @param fragmentSize size of the fragment 
	 * @return fragment extracted
	 * @throws IllegalArgumentException if any of parameters are null or out of ranges
	 * @since 0.0.3
	 */
	public static String extractFragment(final String source, final long row, final long col, final int fragmentSize) throws IllegalArgumentException {
		if (source == null || source.isEmpty()) {
			throw new IllegalArgumentException("Source string can't be null or empty");
		}
		else if (row < 0) {
			throw new IllegalArgumentException("Negative row number ["+row+"]");
		}
		else if (col < 0) {
			throw new IllegalArgumentException("Negative column number ["+col+"]");
		}
		else if (fragmentSize <= 0) {
			throw new IllegalArgumentException("Fragment size ["+fragmentSize+"] must be positive");
		}
		else {
			long 	rowCount = 0, columnCount = 0;
			int 	location = -1, index = 0, maxIndex = source.length();
			
			for (; index < maxIndex; index++) {
				if (rowCount >= row && columnCount >= col) {
					location = index;
					break;
				}
				else if (source.charAt(index) == '\n') {
					rowCount++;
					columnCount = 0;
				}
				else {
					columnCount++;
				}
			}
			if (index >= maxIndex) {
				location = index-1;
			}
			
			if (location == -1) {
				throw new IllegalArgumentException("No location (row="+row+",col="+col+") found in th source"); 
			}
			else {
				final int	from = Math.max(location-fragmentSize,0), to = Math.min(location+fragmentSize,source.length());
				
				return source.substring(from,to);
			}
		}
	}

	/**
	 * <p>Extract char array fragment to show in error description as tooltip (location-fragmentSize..location+fragmentSize will be extracted).</p>
	 * @param source char array to extract fragment from
	 * @param row row of the fragment
	 * @param col column of the fragment
	 * @param fragmentSize size of the fragment 
	 * @return fragment extracted
	 * @throws IllegalArgumentException if any of parameters are null or out of ranges
	 * @since 0.0.3
	 */
	public static String extractFragment(final char[] source, final long row, final long col, final int fragmentSize) {
		if (source == null || source.length == 0) {
			throw new IllegalArgumentException("Source string can't be null or empty");
		}
		else if (row < 0) {
			throw new IllegalArgumentException("Negative row number ["+row+"]");
		}
		else if (col < 0) {
			throw new IllegalArgumentException("Negative column number ["+col+"]");
		}
		else if (fragmentSize <= 0) {
			throw new IllegalArgumentException("Fragment size ["+fragmentSize+"] must be positive");
		}
		else {
			long 	rowCount = 0, columnCount = 0;
			int 	location = -1;
			
			for (int index = 0, maxIndex = source.length; index < maxIndex; index++) {
				if (rowCount >= row && columnCount >= col) {
					location = index;
					break;
				}
				else if (source[index] == '\n') {
					rowCount++;
					columnCount = 0;
				}
				else {
					columnCount++;
				}
			}
			if (location == -1) {
				throw new IllegalArgumentException("No location (row="+row+",col="+col+") found in th source"); 
			}
			else {
				final int	from = Math.max(location-fragmentSize,0), to = Math.min(location+fragmentSize,source.length);
				
				return new String(source,from,to-from);
			}
		}
	}

	/**
	 * <p>Decode relative position inside string to row number of line where this position is located</p>
	 * @param source source string to decode location for
	 * @param pos location inside source string
	 * @return zero-based row number detected
	 * @throws NullPointerException when string to detect is null
	 * @throws IllegalArgumentException when position is outside the string bounds
	 * @since 0.0.4
	 */
	public static int toRow(final String source, final int pos) throws IllegalArgumentException, NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else if (pos < 0 || pos > source.length()) {
			throw new IllegalArgumentException("Position in the source string ["+pos+"] outside the range 0.."+(source.length()-1)); 
		}
		else {
			int	rowCount = 0;
			
			for (int index = 0; index < Math.min(pos,source.length()); index++) {
				if (source.charAt(index) == '\n') {
					rowCount++;
				}
			}
			return rowCount;
		}
	}

	/**
	 * <p>Decode relative position inside string to row number of line where this position is located</p>
	 * @param source source string to decode location for
	 * @param pos location inside source string
	 * @return zero-based row number detected
	 * @throws NullPointerException when string to detect is null
	 * @throws IllegalArgumentException when position is outside the string bounds
	 * @since 0.0.4
	 */
	public static int toRow(final char[] source, final int pos) throws IllegalArgumentException, NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else if (pos < 0 || pos > source.length) {
			throw new IllegalArgumentException("Position in the source string ["+pos+"] outside the range 0.."+(source.length-1)); 
		}
		else {
			int	rowCount = 0;
			
			for (int index = 0; index < Math.min(pos,source.length); index++) {
				if (source[index] == '\n') {
					rowCount++;
				}
			}
			return rowCount;
		}
	}

	/**
	 * <p>Decode relative position inside string to column number of line where this position is located</p>
	 * @param source source string to decode location for
	 * @param pos location inside source string
	 * @return zero-based column number detected
	 * @throws NullPointerException when string to detect is null
	 * @throws IllegalArgumentException when position is outside the string bounds
	 * @since 0.0.4
	 */
	public static int toCol(final String source, final int pos) throws IllegalArgumentException, NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else if (pos < 0 || pos > source.length()) {
			throw new IllegalArgumentException("Position in the source string ["+pos+"] outside the range 0.."+(source.length()-1)); 
		}
		else {
			int	colCount = 0;
			
			for (int index = 0; index < Math.min(pos,source.length()); index++) {
				if (source.charAt(index) == '\n') {
					colCount = 0;
				}
				else {
					colCount++;
				}
			}
			return colCount;
		}
	}

	/**
	 * <p>Decode relative position inside string to column number of line where this position is located</p>
	 * @param source source string to decode location for
	 * @param pos location inside source string
	 * @return zero-based column number detected
	 * @throws NullPointerException when string to detect is null
	 * @throws IllegalArgumentException when position is outside the string bounds
	 * @since 0.0.4
	 */
	public static int toCol(final char[] source, final int pos) throws IllegalArgumentException, NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else if (pos < 0 || pos > source.length) {
			throw new IllegalArgumentException("Position in the source string ["+pos+"] outside the range 0.."+(source.length-1)); 
		}
		else {
			int	colCount = 0;
			
			for (int index = 0; index < Math.min(pos,source.length); index++) {
				if (source[index] == '\n') {
					colCount = 0;
				}
				else {
					colCount++;
				}
			}
			return colCount;
		}
	}
}
