package chav1961.purelib.streams.byte2char;

import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.growablearrays.InOutGrowableCharArray;

public class Jdbc2JsonReader extends Reader {
	private final ResultSet			rs;
	private final ResultSetMetaData	rsmd;
	private final InOutGrowableCharArray	gca = new InOutGrowableCharArray(false);
	private final ReadingFormat[]	formats; 
	private boolean					theSameFirst = true, theSameLast = false;
	private int						cursor = 0; 
	
	public Jdbc2JsonReader(final ResultSet rs) throws IOException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null");
		}
		else {
			this.rs = rs;
			try{this.rsmd = rs.getMetaData();
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
							throw new IOException("Column ["+rsmd.getColumnName(index)+"] has unsuported format ["+rsmd.getColumnTypeName(index)+"]");
					}
				}			
			gca.append("[\n");
			} catch (SQLException e) {
				throw new IOException("Error getting metadata "+e.getLocalizedMessage(),e);
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
		else if (off+len < 0 || off+len >= cbuf.length) {
			throw new IllegalArgumentException("Offset+length ["+(off+len)+"] out of range 0.."+(cbuf.length-1));			
		}
		else {
			if (cursor >= gca.length()) {
				try{if (!rs.next()) {
						if (!theSameLast) {
							gca.append("]\n");
							cursor = 0;
							theSameLast = true;
						}
						else {
							return -1;
						}
					}
					else {
						gca.length(0);
						if (!theSameFirst) {
							gca.append(',');
						}
						gca.print('{');
						for (int index = 1; index <= rsmd.getColumnCount(); index++) {
							if (index != 1) {
								gca.print(',');
							}
							gca.print('\"').print(rsmd.getColumnName(index)).print("\":");
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
						gca.println('}');
						theSameFirst = false;
					}
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
