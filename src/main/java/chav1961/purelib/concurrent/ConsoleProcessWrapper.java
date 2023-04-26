package chav1961.purelib.concurrent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;

public class ConsoleProcessWrapper implements AutoCloseable, ExecutionControl {
	private final SubstitutableProperties	props;
	private final String[]					commands;
	
	private volatile boolean	started = false, suspended = false, available = false;
	private volatile long		totalInputLines = 0, inputLines = 0;
	private volatile long		totalErrorLines = 0, errorLines = 0;
	
	public ConsoleProcessWrapper(final SubstitutableProperties props, final String... commands) {
		if (props == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else if (commands == null || commands.length == 0 || Utils.checkArrayContent4Nulls(commands, true) >= 0) {
			throw new IllegalArgumentException("Commands are null, empty or contain nulls/empties inside"); 
		}
		else {
			this.props = props;
			this.commands = commands;
		}
	}
	
	@Override
	public synchronized void close() throws IOException {
		// TODO Auto-generated method stub
		if (isStarted()) {
			stop();
		}
	}
	
	@Override
	public synchronized void start() throws IOException {
		if (isStarted()) {
			throw new IllegalStateException("Wrapper is started already"); 
		}
		else {
			// TODO Auto-generated method stub
			available = true;
			started = true;
		}
	}

	@Override
	public synchronized void suspend() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Wrapper is not started yer"); 
		}
		else if (isSuspended()) {
			throw new IllegalStateException("Wrapper is suspended already"); 
		}
		else {
			// TODO Auto-generated method stub
			available = false;
			suspended = true;
		}
	}

	@Override
	public synchronized void resume() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Wrapper is not started yer"); 
		}
		else if (!isSuspended()) {
			throw new IllegalStateException("Wrapper is not suspended yet"); 
		}
		else {
			// TODO Auto-generated method stub
			available = true;
			suspended = false;
		}
	}

	@Override
	public synchronized void stop() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Wrapper is not started yer"); 
		}
		else {
			// TODO Auto-generated method stub
			available = false;
			suspended = false;
			started = false;
		}
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public boolean isSuspended() {
		return suspended;
	}

	public void setErrorWriter(final Writer writer) throws IOException {
		
	}
	
	public Writer process(final char[] content) throws ConsoleCommandException, IOException {
		if (content == null) {
			throw new NullPointerException("Content to process can't be null");
		}
		else {
			return process(content, 0, content.length);
		}
	}

	public Writer process(final char[] content, final int from, final int length) throws ConsoleCommandException, IOException {
		if (content == null) {
			throw new NullPointerException("Content to process can't be null");
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From value ["+from+"] out of range 0.."+(content.length-1)); 
		}
		else if (length < 0 || from + length >= content.length) {
			throw new IllegalArgumentException("Length value ["+from+"] out of range 0.."+(content.length-from)); 
		}
		else if (available) {
			// TODO Auto-generated method stub
			return null;
		}
		else {
			throw new ConsoleCommandException("Process is not available now"); 
		}
	}
	
	public Writer process(final CharSequence seq) throws ConsoleCommandException, IOException {
		if (seq == null) {
			throw new NullPointerException("Char sequence to process can't be null");
		}
		else if (available) {
			// TODO Auto-generated method stub
			return null;
		}
		else {
			throw new ConsoleCommandException("Process is not available now"); 
		}
	}
	
	public Writer process(final Reader rdr) throws ConsoleCommandException, IOException {
		if (rdr == null) {
			throw new NullPointerException("Reader to process can't be null");
		}
		else if (available) {
			// TODO Auto-generated method stub
			return null;
		}
		else {
			throw new ConsoleCommandException("Process is not available now"); 
		}
	}

	protected boolean command(final long totalLineNo, final long lineNo, final char[] content, final int from, final int len) throws ConsoleCommandException, IOException {
		return true;
	}
	
	protected void answer(final long totalLineNo, final long lineNo, final char[] content, final int from, final int len) throws ConsoleCommandException, IOException {
	}

	protected void error(final long totalLineNo, final long lineNo, final char[] content, final int from, final int len) throws ConsoleCommandException, IOException {
	}

	protected String getInputStreamEncoding() {
		return PureLibSettings.DEFAULT_CONTENT_ENCODING;
	}
	
	protected String getErrorStreamEncoding() {
		return PureLibSettings.DEFAULT_CONTENT_ENCODING;
	}
	
	private void processInputStream(final Process process) throws IOException {
		try(final InputStream	is = process.getInputStream();
			final Reader		rdr = new InputStreamReader(is, getInputStreamEncoding());
			final LineByLineProcessor	lblp = new LineByLineProcessor(this::processInputLine)) {
			
			try{lblp.write(rdr);
			} catch (SyntaxException e) {
				throw new IOException(e);
			}
		}
	}
	
	private void processInputLine(long displacement, int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
		try{
			answer(totalInputLines++, inputLines++, data, from, length);
		} catch (ConsoleCommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processErrorStream(final Process process) throws IOException {
		try(final InputStream	is = process.getErrorStream();
			final Reader		rdr = new InputStreamReader(is, getErrorStreamEncoding());
			final LineByLineProcessor	lblp = new LineByLineProcessor(this::processErrorLine)) {
			
			try{lblp.write(rdr);
			} catch (SyntaxException e) {
				throw new IOException(e);
			}
		}
	}

	private void processErrorLine(long displacement, int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
		try{
			error(totalInputLines++, inputLines++, data, from, length);
		} catch (ConsoleCommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
