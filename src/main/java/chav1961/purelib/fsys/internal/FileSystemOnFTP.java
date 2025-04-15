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
import java.util.function.BiFunction;
import java.util.regex.Matcher;
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
	private static final URI		SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":ftp:/");
	private static final URI		SERVE_S = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":ftp:/");
	private static final String		DESCRIPTION = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_DESCRIPTION_SUFFIX;
	private static final String		VENDOR = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_VENDOR_SUFFIX;
	private static final String		LICENSE = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_SUFFIX;
	private static final String		LICENSE_CONTENT = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_CONTENT_SUFFIX;
	private static final String		HELP = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnFTP.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_HELP_SUFFIX;
	private static final Icon		ICON = new ImageIcon(FileSystemOnXMLReadOnly.class.getResource("xmlIcon.png"));
	private static final int[]		DUMMY = new int[0];

	private static enum ResponseType {
		SUCCESS(true),
		FAILED(false),
		IGNORED(true),
		STARTED(true),
		COMPLETED(true),
		UNKNOWN(false),
		;
		
		private final boolean	successful;
		
		private ResponseType(final boolean successful) {
			this.successful = successful;
		}
		
		public boolean isSuccessful() {
			return successful;
		}
	}
	
	private static enum Command {
		UNKNOWN((R,S)->null, DUMMY, DUMMY),
		USER((R,S)->null, new int[]{331, 332}, new int[] {421, 500, 501, 530}),
		PASS((R,S)->null, new int[]{230}, new int[] {332, 421, 500, 501, 503, 530}, new int[] {202}),
		CWD((R,S)->null, new int[]{250}, new int[] {421, 500, 501, 502, 530, 550}),
		MKD((R,S)->null, new int[]{257}, new int[] {421, 500, 501, 502, 530, 550}),
		STOR((R,S)->null, new int[]{125, 150}, new int[]{226, 250}, new int[] {421, 425, 426, 450, 451, 452, 500, 501, 530, 532, 534, 535, 551, 552, 553}, DUMMY),
		RNFR((R,S)->null, new int[]{350}, new int[] {421, 450, 500, 501, 502, 530, 550}),
		RNTO((R,S)->null, new int[]{250}, new int[] {421, 500, 501, 502, 503, 530, 532, 553}),
		DELE((R,S)->null, new int[]{250}, new int[] {421, 450, 500, 501, 502, 530, 550}),
		NLST((R,S)->null, new int[]{125, 150}, new int[]{226, 250}, new int[] {421, 425, 426, 450, 451, 500, 501, 502, 504, 530, 534, 535, 550}, DUMMY),
		STAT((R,S)->null, new int[]{212, 213}, new int[] {421, 450, 500, 501, 502, 530}, new int[] {211}),
		;
		
		private final BiFunction<Response, String, Object> callback; 
		private final int[] 	success;
		private final int[] 	failed;
		private final int[] 	ignored;
		private final int[] 	start;
		private final int[] 	stop;
		private final boolean	startStop;	
		
		private Command(final BiFunction<Response, String, Object> callback, final int[] success, final int[] failed) {
			this(callback, success, failed, DUMMY);
		}
		
		private Command(final BiFunction<Response, String, Object> callback, final int[] success, final int[] failed, final int[] ignored) {
			this.callback = callback;
			this.success = success;
			this.failed = failed;
			this.ignored = ignored;
			this.start = DUMMY;
			this.stop = DUMMY;
			this.startStop = false;
		}

		private Command(final BiFunction<Response, String, Object> callback, final int[] start, final int[] stop, final int[] failed, final int[] ignored) {
			this.callback = callback;
			this.success = DUMMY;
			this.failed = failed;
			this.ignored = ignored;
			this.start = start;
			this.stop = stop;
			this.startStop = true;
		}

		public BiFunction<Response, String, Object> getCallback() {
			return callback;
		}
		
		public ResponseType getResponseType(final int responseCode) {
			for(int item : success) {
				if (item == responseCode) {
					return ResponseType.SUCCESS;
				}
			}
			for(int item : failed) {
				if (item == responseCode) {
					return ResponseType.FAILED;
				}
			}
			for(int item : ignored) {
				if (item == responseCode) {
					return ResponseType.IGNORED;
				}
			}
			for(int item : start) {
				if (item == responseCode) {
					return ResponseType.STARTED;
				}
			}
			for(int item : stop) {
				if (item == responseCode) {
					return ResponseType.COMPLETED;
				}
			}
			return ResponseType.UNKNOWN;
		}
		
		public boolean isStartStopCommand() {
			return startStop;
		}
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
				this.server.close();
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
		return new Console(lock);
	}

	private Response buildResponse(final String answer, final Command command, final Console console) throws IOException {
		if (command.isStartStopCommand()) {
			return new Response(command, answer, console.sock);
		}
		else {
			return new Response(command, answer);
		}
	}
	
	private class Console implements Closeable {
		private final Lock	lock;
		private Socket		sock = null;
		
		private Console(final Lock lock) {
			this.lock = lock;
		}
		
		Response sendCommand(final Command command, final Object... parameters) throws IOException {
			final String 	message = String.format(FILESYSTEM_URI_SCHEME, parameters);
			serverWr.write(message);
			serverWr.flush();
			return buildResponse(serverBRdr.readLine(), command, this);
		}

		@Override
		public void close() throws IOException {
			lock.unlock();
		}
	}

	private static class Response {
		private static final Pattern	ANSWER = Pattern.compile("(\\d{3,3})\\s+(.*)");
		
		private final Command	command;
		private final int		responseCode;
		private final String	responseString;
		private final Object	responseEntity;
		
		private Response(final Command command, final String responseString) throws IOException {
			final Matcher	m = ANSWER.matcher(responseString);
			
			if (!m.matches()) {
				throw new IOException("Invalid response from FTP server ["+responseString+"]");
			}
			else {
				this.command = command;
				this.responseCode = Integer.valueOf(m.group(1));
				this.responseString = m.group(2);
				this.responseEntity = command.getCallback().apply(this, responseString);
			}
		}

		private Response(final Command command, final String responseString, final Socket socket) throws IOException {
			final Matcher	m = ANSWER.matcher(responseString);
			
			if (!m.matches()) {
				throw new IOException("Invalid response from FTP server ["+responseString+"]");
			}
			else {
				this.command = command;
				this.responseCode = Integer.valueOf(m.group(1));
				this.responseString = m.group(2);
				this.responseEntity = socket;
			}
		}
		
		Command getCommand() {
			return command;
		}
		
		int getResponseCode() {
			return responseCode;
		}
		
		String getResponseString() {
			return responseString;
		}
		
		ResponseType getResponseType() {
			return getCommand().getResponseType(getResponseCode());
		}
		
		boolean isSuccessful() {
			return getResponseType().isSuccessful();
		}
		
		<T> T getResponseEntity() {
			return (T)responseEntity;
		}
		
		InputStream getInputStream() throws IOException {
			if (getResponseEntity() instanceof Socket) {
				final Socket		sock = getResponseEntity();
				final InputStream	is = sock.getInputStream(); 
				
				sock.getOutputStream().close();
				return new InputStream() {
					@Override
					public int read() throws IOException {
						return is.read();
					}
					
					@Override
					public int read(byte[] b) throws IOException {
						return is.read(b);
					}
					
					@Override
					public int read(byte[] b, int off, int len) throws IOException {
						return is.read(b, off, len);
					}
					
					@Override
					public void close() throws IOException {
						super.close();
						sock.close();
					}
				};
			}
			else {
				throw new IOException("Command doesn't support input stream");
			}
		}
		
		OutputStream getOutputStream() throws IOException {
			if (getResponseEntity() instanceof Socket) {
				final Socket		sock = getResponseEntity();
				final OutputStream	os = sock.getOutputStream(); 
				
				sock.getInputStream().close();
				return new OutputStream() {
					@Override
					public void write(int b) throws IOException {
						os.write(b);
					}

					@Override
					public void write(byte[] b) throws IOException {
						os.write(b);
					}
					
					@Override
					public void write(byte[] b, int off, int len) throws IOException {
						os.write(b, off, len);
					}
					
					@Override
					public void close() throws IOException {
						super.close();
						sock.close();
					}

				};
			}
			else {
				throw new IOException("Command doesn't support input stream");
			}
		}
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
