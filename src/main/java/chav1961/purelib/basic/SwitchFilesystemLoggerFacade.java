package chav1961.purelib.basic;

import java.io.PrintWriter;
import java.net.URI;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class SwitchFilesystemLoggerFacade extends AbstractLoggerFacade {
	public static final URI		LOGGER_URI = URI.create(LoggerFacade.LOGGER_SCHEME+":switchfs:/");
	
	private final FileSystemInterface	fsi;
	private final String				nameTemplate;
	private final int					maxNumberOfFiles;
	private final long					maxNumberOfRecords;
	private final long					maxSize;
	private final boolean				rotate;
	
	private PrintWriter		pw;
	
	public SwitchFilesystemLoggerFacade() {
		this.fsi = null;
		this.nameTemplate = null;
		this.maxNumberOfFiles = 0;
		this.maxNumberOfRecords = 0;
		this.maxSize = 0;
		this.rotate = false;
		PureLibSettings.registerAutoCloseable(this);
	}
	
	public SwitchFilesystemLoggerFacade(final FileSystemInterface fsi, final String nameTemplate, final int maxNumberOfFiles, final long maxNumberOfRecords, final long maxSize, final boolean rotate) {
		if (fsi == null) {
			throw new NullPointerException("File system interface can't be null");
		}
		else if (nameTemplate == null || nameTemplate.isEmpty() || !nameTemplate.contains("*")) {
			throw new IllegalArgumentException("Name template can't be null or empty and must contain exactly one '*' inside");
		}
		else {
			this.fsi = fsi;
			this.nameTemplate = nameTemplate;
			this.maxNumberOfFiles = maxNumberOfFiles;
			this.maxNumberOfRecords = maxNumberOfRecords;
			this.maxSize = maxSize;
			this.rotate = rotate;
		}
	}
	
	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		if (resource == null) {
			throw new NullPointerException("Resource can't be null");
		}
		else {
			return URIUtils.canServeURI(resource, LOGGER_URI);
		}
	}

	@Override
	public LoggerFacade newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		if (resource == null) {
			throw new NullPointerException("Resource can't be null");
		}
		else {
			final String				path = resource.getPath();
			return null;
		}
	}

	@Override
	protected AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void toLogger(final Severity level, final String text, final Throwable throwable) {
		// TODO Auto-generated method stub
		if (level != Severity.tooltip) {
			pw.println("System.err.logger["+level+"]: "+text);
			if (throwable != null) {
//				throwable.printStackTrace(ps);
			}
			pw.flush();
		}
	}
}
