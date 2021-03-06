package chav1961.purelib.streams.byte2char;

import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.growablearrays.InOutGrowableCharArray;

/**
 * <p>This class converts content of JDBC {@linkplain ResultSet} to CSV character stream. Output CSV format is compatible with RFC 4180 requirements.</p>   
 * @see <a href="https://tools.ietf.org/html/rfc4180">RFC 4180</a>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class Jdbc2CsvReader extends Reader {
	private static final char[]		NEWLINE = "\r\n".toCharArray();
	
	private final ResultSet			rs;
	private final char				splitter;
	private final InOutGrowableCharArray	gca = new InOutGrowableCharArray(false);
	private final ReadingFormat[]	formats; 
	private int						cursor;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param rs result set to select content from
	 * @param splitter splitter for the CSV fields
	 * @throws NullPointerException if result set reference is null 
	 * @throws IOException on any I/O errors
	 */
	public Jdbc2CsvReader(final ResultSet rs, final char splitter) throws IOException, NullPointerException {
		this(rs,splitter,true);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param rs result set to select content from
	 * @param splitter splitter for the CSV fields
	 * @param firstLineIsNames insert field names into CSV stream at the same first line of the stream
	 * @throws NullPointerException if result set reference is null 
	 * @throws IOException on any I/O errors
	 */
	public Jdbc2CsvReader(final ResultSet rs, final char splitter, final boolean firstLineIsNames) throws IOException, NullPointerException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null");
		}
		else {
			this.rs = rs;
			this.splitter = splitter;
			
			try{final ResultSetMetaData	rsmd = rs.getMetaData();
			
				this.formats = new ReadingFormat[rsmd.getColumnCount()];
				for (int index = 1; index <= rsmd.getColumnCount(); index++) {
					switch (rsmd.getColumnType(index)) {
						case Types.BOOLEAN	:
						case Types.BIT		:
							this.formats[index-1] = ReadingFormat.AsBoolean;
							break;
						case Types.BIGINT	:
						case Types.INTEGER	:
						case Types.SMALLINT	:
						case Types.TINYINT	:
							this.formats[index-1] = ReadingFormat.AsInteger;
							break;
						case Types.DECIMAL	:
						case Types.DOUBLE	:
						case Types.FLOAT	:
						case Types.NUMERIC	:
						case Types.REAL		:
							this.formats[index-1] = ReadingFormat.AsReal;
							break;
						case Types.CHAR		:
						case Types.CLOB		:
						case Types.LONGNVARCHAR	:
						case Types.LONGVARCHAR	:
						case Types.NCHAR	:
						case Types.NCLOB	:
						case Types.NVARCHAR	:
						case Types.VARCHAR	:
							this.formats[index-1] = ReadingFormat.AsString;
							break;
						case Types.DATE		:
						case Types.TIME		:
						case Types.TIMESTAMP	:
							this.formats[index-1] = ReadingFormat.AsDate;
							break;
						default :
							throw new IOException("Column ["+rsmd.getColumnName(index)+"] has unsupported format ["+rsmd.getColumnTypeName(index)+"]");
					}
					if (firstLineIsNames) {
						if (index != 1) {
							gca.append(splitter);
						}
						gca.append('\"').append(rsmd.getColumnName(index)).append('\"');
					}
				}
				if (firstLineIsNames) {
					gca.append(NEWLINE);
					cursor = 0;
				}
				else {
					cursor = Integer.MAX_VALUE;
				}
			} catch (SQLException e) {
				throw new IOException("Error processing metadata: "+e.getLocalizedMessage(),e);
			}
		}		
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		if (cbuf == null || cbuf.length == 0) {
			throw new IllegalArgumentException("Buffer can't be null or empty array");			
		}
		else if (off < 0 || off >= cbuf.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(cbuf.length-1));			
		}
		else if (off+len < 0 || off+len > cbuf.length) {
			throw new IllegalArgumentException("Offset+length ["+(off+len)+"] out of range 0.."+(cbuf.length-1));			
		}
		else {
			if (cursor >= gca.length()) {
				try{if (!rs.next()) {
						return -1;
					}
					else {
						gca.length(0);
						for (int index = 1; index <= formats.length; index++) {
							if (index != 1) {
								gca.append(splitter);
							}
							switch (formats[index-1]) {
								case AsBoolean		:
									gca.print(rs.getBoolean(index));
									break;
								case AsDate		:
									gca.print(rs.getTimestamp(index).getTime());
									break;
								case AsInteger	:
									gca.print(rs.getLong(index));
									break;
								case AsReal		:
									gca.print(rs.getDouble(index));
									break;
								case AsString	:
									gca.print('\"').print(rs.getString(index)).print('\"');
									break;
								default:
									throw new UnsupportedOperationException("Unsupported column format ["+formats[index-1]+"]");					
							}
						}
						gca.append(NEWLINE);
					}
					cursor = 0;
				} catch (SQLException | PrintingException e) {
					throw new IOException("Error getting record : "+e.getLocalizedMessage(),e);
				}
			}
			final int	currentLen = Math.min(len,gca.length()-cursor);
			
			gca.read(cursor,cbuf,off,off+currentLen);
			cursor += currentLen;
			return currentLen;
		}
	}

	@Override
	public void close() throws IOException {
		gca.clear();
	}
}
