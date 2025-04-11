package chav1961.purelib.fsys.internal;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.AbstractFileSystem;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.i18n.internal.PureLibLocalizer;

public class FileSystemOnFTP extends AbstractFileSystem implements FileSystemInterfaceDescriptor {
	private static final URI	SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":ftp:/");
	private static final URI	SERVE_S = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":ftp:/");
	private static final String	DESCRIPTION = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_DESCRIPTION_SUFFIX;
	private static final String	VENDOR = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_VENDOR_SUFFIX;
	private static final String	LICENSE = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_SUFFIX;
	private static final String	LICENSE_CONTENT = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_CONTENT_SUFFIX;
	private static final String	HELP = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_HELP_SUFFIX;
	private static final Icon	ICON = new ImageIcon(FileSystemOnXMLReadOnly.class.getResource("xmlIcon.png"));

	private static enum Command {
		UNKNOWN,
		USER,
		PASS,
		CWD,
		MKD,
		STOR,
		RNFR,
		RNTO,
		DELE,
		NLST,
		STAT,
	}
	
	private final FileSystemOnFTP	parent;
	private final Socket			server;
	private final Reader			serverRdr;
	private final BufferedReader	serverBRdr;
	private final Writer			serverWr;
	private final Lock				lock = new ReentrantLock();
	
	public FileSystemOnFTP(){
		this.parent = null;
		this.server = null;
		this.serverRdr = null;
		this.serverBRdr = null;
		this.serverWr = null;
	}	
	
	public FileSystemOnFTP(final FileSystemOnFTP parent, final URI rootPath) throws IOException {
		super(rootPath);
		final String	userPass = rootPath.getAuthority();

		if (Utils.checkEmptyOrNullString(userPass) || !userPass.matches("[a-zA-Z0-9]+/[a-zA-Z0-9]+")) {
			throw new IOException("Illegal URI ["+rootPath+"] - authority is missing or has invalid format");
		}
		else {
			this.server = new Socket(rootPath.getHost(), rootPath.getPort());

			try(final Console	c = lockConsole()) {
				final String[]	pieces = userPass.split("/");
				final Response	r1 = c.sendCommand(Command.USER, pieces[0].trim());
				
				if (r1.isSuccessful()) {
					final Response	r2 = c.sendCommand(Command.PASS, pieces[1].trim());
					
					if (!r2.isSuccessful()) {
						this.server.close();
						throw new IOException(r1.getResponseString());
					}
					else {
						this.parent = parent;
						this.serverRdr = new InputStreamReader(server.getInputStream());
						this.serverBRdr = new BufferedReader(serverRdr);
						this.serverWr = new OutputStreamWriter(server.getOutputStream());
					}
				}
				else {
					this.server.close();
					throw new IOException(r1.getResponseString());
				}
			} catch (InterruptedException exc) {
				Thread.currentThread().interrupt();
				throw new IOException(exc);
			}
		}
	}

	private FileSystemOnFTP(final FileSystemOnFTP parent) throws IOException {
		super(parent);
		this.parent = parent;
		this.server = null;
		this.serverRdr = null;
		this.serverBRdr = null;
		this.serverWr = null;
	}

	@Override
	public void close() throws IOException {
		if (server != null) {
			server.close();
		}
		super.close();
	}
	
	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getVersion() {
		return PureLibSettings.CURRENT_VERSION;
	}

	@Override
	public URI getLocalizerAssociated() {
		return PureLibLocalizer.LOCALIZER_SCHEME_URI;
	}

	@Override
	public String getDescriptionId() {
		return DESCRIPTION;
	}

	@Override
	public String getVendorId() {
		return VENDOR;
	}

	@Override
	public Icon getIcon() {
		return ICON;
	}

	@Override
	public String getLicenseId() {
		return LICENSE;
	}

	@Override
	public String getLicenseContentId() {
		return LICENSE_CONTENT;
	}

	@Override
	public String getHelpId() {
		return HELP;
	}

	@Override
	public URI getUriTemplate() {
		return SERVE;
	}

	@Override
	public FileSystemInterface getInstance() throws EnvironmentException {
		return this;
	}

	@Override
	public boolean testConnection(final URI connection, final LoggerFacade logger) throws IOException {
		if (connection == null) {
			throw new NullPointerException("Connection to test can't be null");
		}
		else {
			try(final FileSystemInterface	inst  = newInstance(connection)) {
				
				return inst.exists();
			} catch (EnvironmentException e) {
				if (logger != null) {
					logger.message(Severity.error, e, "Error testing connection [%1$s]: %2$s",connection,e.getLocalizedMessage());
				}
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public boolean canServe(final URI resource) {
		if (resource == null) {
			throw new NullPointerException("Resource can't be null");
		}
		else {
			return URIUtils.canServeURI(resource,SERVE) || URIUtils.canServeURI(resource,SERVE_S);
		}
	}

	@Override
	public FileSystemInterface newInstance(final URI uriSchema) throws EnvironmentException {
		if (uriSchema == null) {
			throw new NullPointerException("URI schema can't be null");
		}
		else if (canServe(uriSchema)) {
			try {
				return new FileSystemOnFTP(this, uriSchema);
			} catch (IOException e) {
				throw new EnvironmentException(e);
			}
		}
		else {
			throw new IllegalArgumentException("URI scheme ["+uriSchema+"] can't be served by this class");
		}
	}

	@Override
	public FileSystemInterface clone() {
		try {
			return new FileSystemOnFTP(this);
		} catch (IOException e) {
			throw new EnvironmentException(e);
		}
	}

	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		return new FTPDataWrapper(actualPath);
	}

	private Console lockConsole() throws InterruptedException {
		lock.lockInterruptibly();
		return new Console() {

			@Override
			public void close() throws IOException {
				lock.unlock();
			}

			@Override
			public Response sendCommand(final Command command, final Object... parameters) throws IOException {
				final String 	message = String.format(FILESYSTEM_URI_SCHEME, parameters);
				serverWr.write(message);
				serverWr.flush();
				return buildResponse(serverBRdr.readLine(), command);
			}

		};
	}

	private Response buildResponse(final String answer, final Command command) {
		// TODO Auto-generated method stub
		return new Response() {
			@Override public Command getCommand() {return command;}
			@Override public String getResponseString() {return answer;}

			@Override
			public <T> T getResponseEntity() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isSuccessful() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public OutputStream getOutputStream() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
	
	private static interface Console extends Closeable {
		Response sendCommand(final Command command, final Object... parameters) throws IOException;
	}

	private static interface Response {
		Command getCommand();
		String getResponseString();
		<T> T getResponseEntity();
		boolean isSuccessful();
		InputStream getInputStream() throws IOException;
		OutputStream getOutputStream() throws IOException;
	}

	private class FTPDataWrapper implements DataWrapperInterface {
		private final URI		wrapperURI;
		
		public FTPDataWrapper(final URI wrapper) {
			this.wrapperURI = wrapper;
		}

		@Override
		public boolean tryLock(final String path, final boolean sharedMode) throws IOException {
			return sharedMode;
		}

		@Override
		public void lock(final String path, final boolean sharedMode) throws IOException {
			if (!sharedMode) {
				throw new IOException("Exclusuve locks are not supported on this file system");
			}
		}

		@Override
		public void unlock(final String path, final boolean sharedMode) throws IOException {
		}

		@Override
		public URI[] list(final Pattern pattern) throws IOException {
			try(final Console	c = lockConsole()) {
				final Response	r1 = c.sendCommand(Command.NLST, getPath());
				
				if (r1.isSuccessful()) {
					try(final InputStream		is = r1.getInputStream();
						final Reader			rdr = new InputStreamReader(is)) {
						final List<URI>			result = new ArrayList<>();
						
						for(String item : Utils.fromResource(rdr).split("\n")) {
							if (pattern.matcher(item).matches()) {
								result.add(wrapperURI.resolve(item.trim()));
							}
						}
						return result.toArray(new URI[result.size()]);
					}
				}
				else {
					throw new IOException(r1.getResponseString()); 
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException(e);
			}
		}

		@Override
		public void mkDir() throws IOException {
			try(final Console	c = lockConsole()) {
				final Response	r1 = c.sendCommand(Command.CWD, getParenPath());
				
				if (r1.isSuccessful()) {
					final Response	r2 = c.sendCommand(Command.MKD, getName());
					
					if (!r2.isSuccessful()) {
						throw new IOException(r2.getResponseString());
					}
				}
				else {
					throw new IOException(r1.getResponseString());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException(e);
			}
		}

		@Override
		public void create() throws IOException {
			try(final Console	c = lockConsole()) {
				final Response	r1 = c.sendCommand(Command.CWD, getParenPath());
				
				if (r1.isSuccessful()) {
					final Response	r2 = c.sendCommand(Command.STOR, getName());
					
					if (r2.isSuccessful()) {
						try(final OutputStream	os = r2.getOutputStream()) {
							Utils.copyStream(new InputStream(){
								@Override
								public int read() throws IOException {
									return -1;
								}}, os);
						}
					}
					else {
						throw new IOException(r2.getResponseString());
					}
				}
				else {
					throw new IOException(r1.getResponseString());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException(e);
			}
		}

		@Override
		public void setName(final String name) throws IOException {
			try(final Console	c = lockConsole()) {
				final Response	r1 = c.sendCommand(Command.CWD, getParenPath());
				
				if (r1.isSuccessful()) {
					final Response	r2 = c.sendCommand(Command.RNFR, getName());
					
					if (r2.isSuccessful()) {
						final Response	r3 = c.sendCommand(Command.RNTO, name);
						
						if (!r3.isSuccessful()) {
							throw new IOException(r3.getResponseString());
						}
					}
					else {
						throw new IOException(r2.getResponseString());
					}
				}
				else {
					throw new IOException(r1.getResponseString());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException(e);
			}
		}

		@Override
		public void delete() throws IOException {
			try(final Console	c = lockConsole()) {
				final Response	r1 = c.sendCommand(Command.CWD, getParenPath());
				
				if (r1.isSuccessful()) {
					final Response	r2 = c.sendCommand(Command.DELE, getName());
					
					if (!r2.isSuccessful()) {
						throw new IOException(r2.getResponseString());
					}
				}
				else {
					throw new IOException(r1.getResponseString());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException(e);
			}
		}

		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			try(final Console	c = lockConsole()) {
				final Response	r1 = c.sendCommand(Command.CWD, getPath());
				
				if (r1.isSuccessful()) {
					return Utils.<Object>mkMap(
							//ATTR_SIZE, temp.length(), 
							ATTR_NAME, getName(), 
							ATTR_ALIAS, getName(), 
							//ATTR_LASTMODIFIED, temp.lastModified(), 
							//ATTR_DIR, temp.isDirectory(), 
							//ATTR_CANREAD, temp.canRead(), 
							//ATTR_CANWRITE, temp.canWrite(),
							ATTR_EXIST, true 
							);
				}
				else {
					return Utils.<Object>mkMap(ATTR_SIZE, 0, 
							ATTR_NAME, getName(), 
							ATTR_ALIAS, getName(), 
							ATTR_LASTMODIFIED, 0, 
							ATTR_DIR, false, 
							ATTR_EXIST, false, 
							ATTR_CANREAD, false,
							ATTR_CANWRITE, false);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException(e);
			}
		}

		@Override
		public void linkAttributes(final Map<String, Object> attributes) throws IOException {
		}
		
		private File parseFileDescriptor(final String desc) {
			return null;
		}

		private String getParenPath() {
			return null;
		}
	}	
}
