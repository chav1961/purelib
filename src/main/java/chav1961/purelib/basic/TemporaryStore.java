package chav1961.purelib.basic;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.growablearrays.GrowableByteArray;
import chav1961.purelib.basic.interfaces.InputOutputPairInterface;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.logs.NullLoggerFacade;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class is a store manager for temporary files. It designs to use in the predefined scenario for temporary file :</p>
 * <ul>
 * <li>get {@linkplain InputOutputPair} class instance</li>
 * <li>get {@linkplain OutputStream} from the instance and fill output content to temporary file</li>
 * <li>get {@linkplain InputStream} from the instance and load input content from temporary file</li>
 * <li>close instance</li>
 * </ul>
 * <p>This class is maximized to speed on little files (keeps content in RAM) and minimized memory on large files (uses ZLib compression).
 * Switching from RAM usage and file system usage makes automatically during filling process. To prevent uncontrolled expansion of the RAM
 * used, a maximal limit of the RAM buffer is used.</p>
 * <p>This class implements {@linkplain Closeable} interface and can be user in the <b>try-with-resource</b> statements.</p> 
 * <p>This class can be used in the multi-threaded environment.</p>
 * 
 * @see LoggerFacade
 * @see FileSystemInterface
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public class TemporaryStore implements Closeable {
	/**
	 * <p>Default RAM buffer size to use</p>
	 */
	public static final int				DEFAULT_BUFFER_SIZE = 1 << 20;
	
	private final LoggerFacade 			logger;
	private final FileSystemInterface	root;
	private final int					maxBufferSize;
	private final boolean				needCloseParameters;
	private final AtomicInteger			currentBufferSize = new AtomicInteger(0);
	private final ReusableInstances<InputOutputPair>	pairs = new ReusableInstances<InputOutputPair>(
																()->{return new InputOutputPair();}
																,(InputOutputPair inst)->{
																	inst.toWrite = null;
																	inst.fsi = null; 
																	inst.currentSize = 0;
																	inst.state = InputOutputPair.STATE_INITIAL;
																	inst.useFileSystem = false;
																	inst.gba.clear(); 
																	return inst;
																});	
	

	/**
	 * <p>Constructor of the class. Create class instance with null {@linkplain LoggerFacade logger}, file-based 
	 * {@linkplain FileSystemInterface} filesystem) on "java.io.tmpdir" location and default RAM buffer size</p>
	 * @throws IOException on any I/O errors
	 */
	public TemporaryStore() throws IOException {
		this(DEFAULT_BUFFER_SIZE);
	}

	/**
	 * <p>Constructor of the class. Create class instance with null {@linkplain LoggerFacade logger} and file-based 
	 * {@linkplain FileSystemInterface filesystem} on "java.io.tmpdir" location</p>
	 * @param maxBufferSize max RAM buffer size to use
	 * @throws IllegalArgumentException when buffer size less than default buffer size and greater than Integer.MAX_VALUE / 16
	 * @throws IOException on any I/O errors
	 */
	public TemporaryStore(final int maxBufferSize) throws IllegalArgumentException, IOException {
		if (maxBufferSize < DEFAULT_BUFFER_SIZE || maxBufferSize > Integer.MAX_VALUE >> 4) {
			throw new IllegalArgumentException("Max buffer size ["+maxBufferSize+"] out of range "+DEFAULT_BUFFER_SIZE+".."+(Integer.MAX_VALUE >> 4)); 
		}
		else {
			this.logger = new NullLoggerFacade();
			this.root = new FileSystemOnFile(new File(System.getProperty("java.io.tmpdir")).toURI());
			this.maxBufferSize = DEFAULT_BUFFER_SIZE;
			this.needCloseParameters = true;
		}
	}
	
	/**
	 * <p>Constructor of the class. Create class instance with default RAM buffer size</p>
	 * @param logger logger to send messages to
	 * @param temporaryRoot file system for temporary root
	 * @throws NullPointerException when any parameter is null
	 * @throws IOException on any I/O errors
	 */
	public TemporaryStore(final LoggerFacade logger, final FileSystemInterface temporaryRoot) throws NullPointerException, IOException {
		this(logger,temporaryRoot,DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * <p>Constructor if the class</p>
	 * @param logger logger to send messages to
	 * @param temporaryRoot file system for temporary root
	 * @param maxBufferSize max RAM buffer size to use
	 * @throws NullPointerException when any parameter is null
	 * @throws IllegalArgumentException when buffer size less than default buffer size and greater than Integer.MAX_VALUE / 16
	 * @throws IOException on any I/O errors
	 */
	public TemporaryStore(final LoggerFacade logger, final FileSystemInterface temporaryRoot, final int maxBufferSize) throws NullPointerException, IllegalArgumentException, IOException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (temporaryRoot == null) {
			throw new NullPointerException("Temporary root can't be null"); 
		}
		else if (maxBufferSize < DEFAULT_BUFFER_SIZE || maxBufferSize > Integer.MAX_VALUE >> 4) {
			throw new IllegalArgumentException("Max buffer size ["+maxBufferSize+"] out of range "+DEFAULT_BUFFER_SIZE+".."+(Integer.MAX_VALUE >> 4)); 
		}
		else {
			this.logger = logger;
			this.root = temporaryRoot;
			this.maxBufferSize = maxBufferSize;
			this.needCloseParameters = false;
		}
	}

	@Override
	public void close() throws IOException {
		pairs.close();
		if (needCloseParameters) {
			this.root.close();
			this.logger.close();
		}
	}

	/**
	 * <p>Allocate {@linkplain InputOutputPair} class to support predefined scenario. Allocated instance must be used in the <b>try-with-resource</b> statement
	 * or must be explicitly closed by call {@linkplain InputOutputPair#close()} method. This call frees instance and return it to the instance's pool</p>
	 * <p>{@linkplain InputOutputPair} instance is not reusable. To create another temporary file you need to call {@linkplain #allocate()} again.</p>
	 * @return {@linkplain InputOutputPair} instance to use
	 * @throws IOException on any I/O errors
	 */
	public InputOutputPair allocate() throws IOException {
		final UUID					fileId = UUID.randomUUID();
		final FileSystemInterface	fsi = root.clone().open(String.format("./%016x%016x.tmp",fileId.getMostSignificantBits(),fileId.getLeastSignificantBits())); 
		final InputOutputPair		pair = pairs.allocate();
		
		pair.fsi = fsi;
		return pair;
	}

	/**
	 * <p>This class is used to support predefined scenario for temporary file usage</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 * @last.update 0.0.5
	 */
	public class InputOutputPair implements InputOutputPairInterface {
		private static final int	STATE_INITIAL = 0;
		private static final int	STATE_FILLING = 1;
		private static final int	STATE_FILLED = 2;
		private static final int	STATE_READING = 3;
		private static final int	STATE_READ = 4;
		
		private final GrowableByteArray	gba = new GrowableByteArray(false);
		
		private FileSystemInterface		fsi = null;
		private OutputStream			toWrite = null;
		private InputStream				toRead = null;
		private long					currentSize = 0;
		private int						state = STATE_INITIAL;
		private boolean					useFileSystem = false;

		private final InputStream	is = new InputStream(){
			final byte[]	byte1 = new byte[1]; 
			
			@Override
			public int read() throws IOException {
				final int	rc = read(byte1);
				
				if (rc < 0) {
					return rc;
				}
				else {
					return byte1[0] * 0xFF;
				}
			}
			
		    public int read(final byte b[], final int off, final int len) throws IOException {
		        if (b == null) {
		            throw new NullPointerException("Buffer to read to can't be null");
		        } 
		        else if ((off < 0) || (off > b.length)) {
		            throw new IndexOutOfBoundsException("Offset ["+off+"] out of range 0.."+(b.length-1));
		        }
		        else if ((off + len < 0) || (off + len > b.length)) {
		            throw new IndexOutOfBoundsException("Offset ["+off+"] + length ["+len+"]  out of range 0.."+(b.length-1));
		        } else if (len != 0) {
			        return toRead.read(b,off,len);
		        }
		        else {
		        	return 0;
		        }
		    }

		    public void close() throws IOException {
				if (!(state == STATE_READING || state == STATE_READ)) {
					throw new IllegalStateException("Scenario case fail: getInputStream() must be called exactly after closing output stream got by getOutputStream()"); 
				}
				else {
					state = STATE_READ;
					toRead.close();
				}
		    }
		};
		
		private final OutputStream	os = new OutputStream(){
			@Override
			public void write(int b) throws IOException {
				if (!useFileSystem && !ensureBufferCapacity(1)) {
					switch2File(1);
				}
				if (!useFileSystem) {
					gba.append((byte)b);
				}
				else {
					toWrite.write(b);
				}
			}
			
		    public void write(final byte b[], final int off, final int len) throws IOException {
		        if (b == null) {
		            throw new NullPointerException("Buffer to write from can't be null");
		        } 
		        else if ((off < 0) || (off > b.length)) {
		            throw new IndexOutOfBoundsException("Offset ["+off+"] out of range 0.."+(b.length-1));
		        }
		        else if ((off + len < 0) || (off + len > b.length)) {
		            throw new IndexOutOfBoundsException("Offset ["+off+"] + length ["+len+"]  out of range 0.."+(b.length-1));
		        } else if (len != 0) {
					if (!useFileSystem && !ensureBufferCapacity(len)) {
						switch2File(len);
					} 
					if (!useFileSystem) {
						gba.append(b,off,off+len);
					}
					else {
						toWrite.write(b,off, len);
					}
					currentSize += len;
		        }
		    }

		    public void flush() throws IOException {
				if (useFileSystem) {
					toWrite.flush();
				}
		    }

		    public void close() throws IOException {
				if (!(state == STATE_FILLING || state == STATE_FILLED || state == STATE_READ)) {
					throw new IllegalStateException("Scenario case fail: getInputStream() must be called exactly after closing output stream got by getOutputStream()"); 
				}
				else {
					if (state == STATE_FILLING) {
						state = STATE_FILLED;
					}
					if (useFileSystem) {
						toWrite.close();
					}
				}
		    }
		};

		/**
		 * <p>Get input stream to read data from filled temporary file. Must be call after {@linkplain #getOutputStream()} only
		 * @return input stream to read data from 
		 * @throws IOException on any I/O errors
		 */
		@Override
		public InputStream getInputStream() throws IOException {
			if (state != STATE_FILLED) {
				throw new IllegalStateException("Scenario case fail: getInputStream() must be called exactly after closing output stream got by getOutputStream()"); 
			}
			else {
				state = STATE_READING;
				toRead = useFileSystem ? fsi.read() : gba.getInputStream();
				return is;
			}
		}

		/**
		 * <p>Get output stream to fill temporary file. Must be call before {@linkplain #getInputStream()} only
		 * @return stream to fill data to
		 * @throws IOException on any I/O errors
		 */
		@Override
		public OutputStream getOutputStream() throws IOException {
			if (state != STATE_INITIAL) {
				throw new IllegalStateException("Scenario case fail: getOutputStream() must be called only once and immediately after allocate() calling"); 
			}
			else {
				state = STATE_FILLING;
				currentSize = 0;
				useFileSystem = false;
				return os;
			}
		}

		/**
		 * <p>Get current size of the data filled. Can be used and will be valid at any time</p>
		 * @return current size of the data filled
		 * @throws IOException on any I/O errors
		 */
		public long getSizeUsed() throws IOException {
			return currentSize;
		}
		
		@Override
		public void close() throws IOException {
			gba.clear();
			if (toWrite != null) {
				toWrite.close();
			}
			if (toRead != null) {
				toRead.close();
			}
			if (useFileSystem) {
				fsi.delete();
			}
			fsi.close();
			pairs.free(this);
		}

		private boolean ensureBufferCapacity(final int delta) {
			final int	current = currentBufferSize.getAndAdd(delta);
			final int	tail = maxBufferSize - delta;
			
			return current <= tail;
		}
		
		private void switch2File(int delta) throws IOException {
			toWrite = fsi.create().write();
			try(final InputStream	is = gba.getInputStream()) {
				
				Utils.copyStream(is,toWrite);
			}
			
			gba.clear();
			currentBufferSize.getAndAdd(-delta);
			useFileSystem = true;
		}
	}
	
	static void fillByteArrayWithLong(long value, final byte[] target, final int from) {
		for (int index = 0; index < 8; index++, value >>= 8) {
			target[from+index] = (byte)(value & 0xFF);
		}
	}
}
