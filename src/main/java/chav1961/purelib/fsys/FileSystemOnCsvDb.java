package chav1961.purelib.fsys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class implements the file system interface on the usual file system. The URI to use this class is 
 * <code>URI.create("csvdb:jdbc:jdbc_connection_string");</code> (for example <code>URI.create("csvdb:jdbc:postgres://localhost/myDataBase?user=vassya&amp;password=pupkin");</code>)</p>
 * <p>This class views all database content as two-level tree:</p>
 * <code>
 * <b>/</b><br>
 * <b>/schema1</b><br>
 * <b>/schema1/table1</b><br>
 * <b>/schema1/table2</b><br>
 * <b>...</b>
 * <b>/schema1/tableN</b><br>
 * <b>/schema2</b><br>
 * <b>/schema2/table1</b><br>
 * <b>/schema2/table2</b><br>
 * <b>...</b>
 * <b>/schema2/tableN</b><br>
 * <b>...</b>
 * <b>/schemaN</b><br>
 * <b>/schemaN/table1</b><br>
 * <b>/schemaN/table2</b><br>
 * <b>...</b>
 * <b>/schemaN/tableN</b><br>
 * </code>
 * <p>Each database table is a CSV-format 'file' containing CSV-formatted content. CSV format supported see RFC</p> 
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface FileSystemInterface
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

class FileSystemOnCsvDb extends AbstractFileSystem {
	private static final URI		SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":csvdb:/");
	
	private final URI				connectionString;
	private final Connection		conn;
	private final FileSystemOnCsvDb	parent;
	private int						counter = 0;
	
	/**
	 * <p>This constructor is an entry for the SPI service only. Don't use it in any purposes</p> 
	 */
	public FileSystemOnCsvDb(){
		this.connectionString = null;
		this.conn = null;
		this.parent = null;
	}
	
	/**
	 * <p>Create the file system for the given database connection.  
	 * @param Path remote uri for the remote file system server. Need be absolute URI with the schema 'rmi', for example <code>'rmi://localhost/rmiServerName'</code>. Tail of URI (rmiServerName) 
	 * need be corresponding with the registered RMI server instance name (see {@link RMIFileSystemServer}) 
	 * @throws IOException if any exception was thrown
	 */
	public FileSystemOnCsvDb(final URI rootPath) throws IOException {
		super(rootPath);
		this.connectionString = rootPath;
		this.parent = null;		
		try{this.conn = DriverManager.getConnection(rootPath.getSchemeSpecificPart());
		} catch (SQLException exc) {
			throw new IOException(exc.getMessage());
		}
		this.counter = 1;
	}
	
	private FileSystemOnCsvDb(final FileSystemOnCsvDb another) {
		super(another);
		this.connectionString = another.connectionString;
		this.conn = another.conn;
		
		FileSystemOnCsvDb	root = another;
		while (root.parent != null) {
			root = root.parent;
		}		
		this.parent = root;
		root.counter++;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		try{if (parent != null) {
				if (--parent.counter == 0) {
					parent.conn.close();
				}
			}
			else if (--counter == 0) {
				conn.close();
			}
		} catch (SQLException exc) {
			throw new IOException(exc.getMessage());
		}
	}
	
	@Override
	public boolean canServe(final URI resource) {
		return Utils.canServeURI(resource,SERVE);
	}
	
	@Override
	public FileSystemInterface newInstance(final URI resource) throws EnvironmentException {
		if (!canServe(resource)) {
			throw new EnvironmentException("Resource URI ["+resource+"] is not supported by the class. Valid URI must be ["+SERVE+"...]");
		}
		else {
			return new FileSystemInMemory(URI.create(resource.getRawSchemeSpecificPart()));
		}
	}
	

	@Override
	public FileSystemInterface clone() {
		return new FileSystemOnCsvDb(this);
	}

	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		if ("/".equals(actualPath.getPath())) {
			return new CsvDbDataWrapper4Database(conn);
		}
		else {
			final String[]	parts = actualPath.getPath().substring(1).split("\\/");
			
			if (parts.length == 1 || parts.length == 2 && (parts[1] == null || parts[1].isEmpty())) {
				return new CsvDbDataWrapper4Schemas(conn,actualPath);
			}
			else if (parts.length == 2 || parts.length == 3 && (parts[2] == null || parts[2].isEmpty())) {
				return new CsvDbDataWrapper4Tables(conn,actualPath);
			}
			else {
				return new CsvDbDataWrapper4None(actualPath);
			}
		}
	}

	private static class CsvDbDataWrapper4Database implements DataWrapperInterface {
		private final Connection	conn;
		
		public CsvDbDataWrapper4Database(final Connection conn) {
			this.conn = conn;
		}

		@Override
		public URI[] list(final Pattern pattern) throws IOException {
			final List<URI>		result = new ArrayList<>(); 
			
			try(final ResultSet	rs = conn.getMetaData().getSchemas()) {
				
				while (rs.next()) {
					if (pattern.matcher(rs.getString("TABLE_SCHEM")).matches()) {
						result.add(URI.create("/"+rs.getString("TABLE_SCHEM")+"/"));
					}
				}
				return result.toArray(new URI[result.size()]);
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}

		@Override
		public void mkDir() throws IOException {
			throw new IOException("Can't create new schemas in the database!"); 
		}

		@Override
		public void create() throws IOException {
			throw new IOException("Can't create new schemas in the database!"); 
		}

		@Override
		public void setName(String name) throws IOException {
			throw new IOException("Can't rename the database!"); 
		}

		@Override
		public void delete() throws IOException {
			throw new IOException("Can't delete database!"); 
		}

		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			throw new IOException("This operation can't be used with the database!"); 
		}

		@Override
		public InputStream getInputStream() throws IOException {
			throw new IOException("This operation can't be used with the database!"); 
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			return Utils.mkMap(ATTR_SIZE, 0, ATTR_NAME, "/", ATTR_LASTMODIFIED, 0, ATTR_DIR, true, ATTR_EXIST, true, ATTR_CANREAD, true, ATTR_CANWRITE, false);
		}

		@Override public void linkAttributes(Map<String, Object> attributes) throws IOException {}
	}
	
	private static class CsvDbDataWrapper4Schemas implements DataWrapperInterface {
		private final Connection	conn;
		private final URI			actualPath;
		
		public CsvDbDataWrapper4Schemas(final Connection conn, final URI actualPath) {
			this.conn = conn;
			this.actualPath = actualPath;
		}

		@Override
		public URI[] list(final Pattern pattern) throws IOException {
			final List<URI>		result = new ArrayList<>(); 
			final String[]		parts = actualPath.getPath().substring(1).split("\\/");
			
			try(final ResultSet	rs = conn.getMetaData().getTables(null,parts[0],"%",null)) {
				
				while (rs.next()) {
					if (pattern.matcher(rs.getString("TABLE_NAME")).matches()) {
						result.add(URI.create("/"+rs.getString("TABLE_SCHEM")+"/"+rs.getString("TABLE_NAME")+"/"));
					}
				}
				return result.toArray(new URI[result.size()]);
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}

		@Override
		public void mkDir() throws IOException {
			throw new IOException("Can't create nested schemas in the database!"); 
		}

		@Override
		public void create() throws IOException {
			throw new IOException("Can't create new schemas in the database!"); 
		}

		@Override
		public void setName(final String name) throws IOException {
			throw new IOException("Can't rename database schema!"); 
		}

		@Override
		public void delete() throws IOException {
			throw new IOException("Can't delete database schema!"); 
		}

		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			throw new IOException("This operation can't be used with the database schema!"); 
		}

		@Override
		public InputStream getInputStream() throws IOException {
			throw new IOException("This operation can't be used with the database schema!"); 
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			return Utils.mkMap(ATTR_SIZE, 0, ATTR_NAME, actualPath.getPath(), ATTR_LASTMODIFIED, 0, ATTR_DIR, true, ATTR_EXIST, true, ATTR_CANREAD, true, ATTR_CANWRITE, false);
		}

		@Override public void linkAttributes(Map<String, Object> attributes) throws IOException {}
	}

	private static class CsvDbDataWrapper4Tables implements DataWrapperInterface {
		private final Connection	conn;
		private final URI			actualPath;
		
		public CsvDbDataWrapper4Tables(final Connection conn, final URI actualPath) {
			this.conn = conn;
			this.actualPath = actualPath;
		}
		
		@Override
		public URI[] list(Pattern pattern) throws IOException {
			return null;
		}

		@Override
		public void mkDir() throws IOException {
			throw new IOException("Can't create nested schemas in the current schema or table!"); 
		}

		@Override
		public void create() throws IOException {
			final String[]		parts = actualPath.getPath().substring(1).split("\\/");
			
			try(final Statement	stmt = conn.createStatement()) {
				stmt.executeUpdate("create temporary table "+parts[1]+" (fake char(1))");
			} catch (SQLException e) {
				throw new IOException(e.getMessage());
			}
		}

		@Override
		public void setName(String name) throws IOException {
			final String[]		parts = actualPath.getPath().substring(1).split("\\/");
			
			try(final Statement	stmt = conn.createStatement()) {
				stmt.executeUpdate("alter table "+parts[0]+'.'+parts[1]+" rename to "+name);
			} catch (SQLException e) {
				throw new IOException(e.getMessage());
			}
		}

		@Override
		public void delete() throws IOException {
			final String[]		parts = actualPath.getPath().substring(1).split("\\/");
			
			try(final Statement	stmt = conn.createStatement()) {
				stmt.executeUpdate("drop table "+parts[1]);
			} catch (SQLException e) {
				throw new IOException(e.getMessage());
			}
		}

		@SuppressWarnings("unused")
		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			final String[]	parts = actualPath.getPath().substring(1).split("\\/");
			Statement		stmt; 
			
//			try {stmt = conn.createStatement();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			final String[]		parts = actualPath.getPath().substring(1).split("\\/");
			final StringBuilder	sb = new StringBuilder(); 
			Statement			stmt = null;
			ResultSet			rs = null;
			
			try{stmt = conn.createStatement();
				rs = stmt.executeQuery("select * from "+parts[0]+'.'+parts[1]);
				
				final ResultSetMetaData	rsmd = rs.getMetaData();
				
				for (int index = 1; index <= rsmd.getColumnCount(); index++) {
					sb.append(',').append(rsmd.getColumnName(index));
				}
				final Statement		stmtI = stmt; 
				final ResultSet		rsI = rs; 
				
				return new InputStream() {
					final GrowableByteArray	gba = new GrowableByteArray(true);
					{
						gba.append(sb.append("\r\n").toString().substring(1).getBytes("UTF-8"));
					}
					int		cursor = 0;

					@Override
					public int read(final byte[] target, final int from, final int len) throws IOException {
				        if (target == null) {
				            throw new NullPointerException();
				        } else if (from < 0 || len < 0 || len > target.length - from) {
				            throw new IndexOutOfBoundsException();
				        } else if (len == 0) {
				            return 0;
				        }
				        else {
							if (cursor >= gba.length()) {
								if (!newPieceData()) {
									return -1;
								}
							}
							final int	realLen = Math.min(gba.length()-cursor,len);
							
							System.arraycopy(gba.toArray(),cursor,target,from,realLen);
							cursor += realLen;
							return realLen;
				        }
					}
					
					@Override
					public int read() throws IOException {
						if (cursor >= gba.length()) {
							if (!newPieceData()) {
								return -1;
							}
						}
						return gba.toArray()[cursor++] & 0xFF;		
					}
					
					@Override
					public void close() throws IOException {
						if (rsI != null) {
							try{rsI.close();} catch (SQLException ex) {}
						}
						if (stmtI != null) {
							try{stmtI.close();} catch (SQLException ex) {}
						}
						super.close();
					}
					
					private boolean newPieceData() throws IOException {	// :-)
						try{if (rsI.next()) {
								gba.clear();
								for (int index = 1; index <= rsmd.getColumnCount(); index++) {
									gba.append(prepareString(rsI.getString(index)));
									if (index == rsmd.getColumnCount()) {
										gba.append((byte)'\r').append((byte)'\n');
									}
									else {
										gba.append((byte)',');
									}
								}
								cursor = 0;
								return true;
							}
							else {
								return false;
							}
						} catch (SQLException e) {
							throw new IOException(e.getMessage());
						}
					}

					private byte[] prepareString(final String string) throws UnsupportedEncodingException {
						if (!string.contains(",") && !string.contains("\"") && !string.contains("\n") && !string.contains("\r")) {
							return string.getBytes("UTF-8");
						}
						else if (!string.contains("\"")) {
							return ('\"'+string+'\"').getBytes("UTF-8");
						}
						else {
							return ('\"'+string.replace("\"","\"\"")+'\"').getBytes("UTF-8");
						}
					}
				};
			} catch (SQLException e) {
				if (rs != null) {
					try{rs.close();} catch (SQLException ex) {}
				}
				if (stmt != null) {
					try{stmt.close();} catch (SQLException ex) {}
				}
				throw new IOException(e.getMessage()); 
			}
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			final String[]		parts = actualPath.getPath().substring(1).split("\\/");
			
			try(final ResultSet	rs = conn.getMetaData().getTables(null,parts[0],parts[1],null)) {
				
				while (rs.next()) {
					return Utils.mkMap(ATTR_SIZE, 0, ATTR_NAME, actualPath.getPath(), ATTR_LASTMODIFIED, 0, ATTR_DIR, false, ATTR_EXIST, true, ATTR_CANREAD, true, ATTR_CANWRITE, true);
				}
				return Utils.mkMap(ATTR_SIZE, 0, ATTR_NAME, actualPath.getPath(), ATTR_LASTMODIFIED, 0, ATTR_DIR, false, ATTR_EXIST, false, ATTR_CANREAD, false, ATTR_CANWRITE, false);
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}

		@Override public void linkAttributes(Map<String, Object> attributes) throws IOException {}
	}

	private static class CsvDbDataWrapper4None implements DataWrapperInterface {
		private final URI	actualPath;
		
		public CsvDbDataWrapper4None(final URI actualPath) {
			this.actualPath = actualPath;
		}
		
		@Override public URI[] list(Pattern pattern) throws IOException {return new URI[0];}
		@Override public void mkDir() throws IOException {throw new IOException("Can't create anything in this location!");}
		@Override public void create() throws IOException {throw new IOException("Can't create anything in this location!");}
		@Override public void setName(String name) throws IOException {throw new IOException("Can't rename anything in this location!");}
		@Override public void delete() throws IOException {throw new IOException("Can't delete anything in this location!");}
		@Override public OutputStream getOutputStream(boolean append) throws IOException {throw new IOException("Can't write anything in this location!");}
		@Override public InputStream getInputStream() throws IOException {throw new IOException("Can't read anything in this location!");}
		@Override public void linkAttributes(Map<String, Object> attributes) throws IOException {}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			return Utils.mkMap(ATTR_SIZE, 0, ATTR_NAME, actualPath.getPath(), ATTR_LASTMODIFIED, 0, ATTR_DIR, false, ATTR_EXIST, false, ATTR_CANREAD, false, ATTR_CANWRITE, false);
		}
	}
}
