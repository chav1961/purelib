package chav1961.purelib.io;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.io.interfaces.RawMemoryInterface;

public class MappedMemoryManager implements RawMemoryInterface, Closeable, Flushable, LoggerFacadeOwner {
	private static final int		TOP_MAGIC = 0xFFEFCDED;
	private static final int		CLUSTER_MAGIC = 0xFFEFCDDE;
	
	private final LoggerFacade		logger;
	private final RandomAccessFile	raf; 
	private final FileChannel		channel;
	private final TopRecord			top;

	public MappedMemoryManager(final LoggerFacade logger, final File file) throws IOException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (file == null) {
			throw new NullPointerException("File channel can't be null"); 
		}
		else {
			this.logger = logger;
			this.raf = new RandomAccessFile(file, "rw");
			this.channel = this.raf.getChannel();
			this.top = TopRecord.load(raf);
		}
	}
	
	public MappedMemoryManager(final LoggerFacade logger, final FileChannel channel) throws IOException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (channel == null) {
			throw new NullPointerException("File channel can't be null"); 
		}
		else {
			this.logger = logger;
			this.raf = null;
			this.channel = channel;
			this.top = TopRecord.load(raf);
		}
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}
	
	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void close() throws IOException {
		if (raf != null) {
			raf.close();
		}
	}
	
	@Override
	public long createMemoryCluster(final long clusterSize) throws IOException {
		return createMemoryCluster(clusterSize, 0, 0);
	}

	@Override
	public long createMemoryCluster(final long clusterSize, final long clusterDelta, final int maxPiece) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dropMemoryCluster(long clusterId) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long allocate(long clusterId, long size) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void free(long clusterId, long address, long size) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public static void prepareMemory(final File	file, final long size) throws IOException {
		try(final OutputStream		os = new FileOutputStream(file);
			final DataOutputStream	dos = new DataOutputStream(os)) {
			final TopRecord			top = new TopRecord(0, 20);
			final byte[]			emptyContent = new byte[8192];
			long					from;

			TopRecord.store(top, dos);
			
			for(from = top.size(); from < size; from += emptyContent.length) {
				dos.write(emptyContent);
			}
			dos.write(emptyContent, 0, (int)(size + emptyContent.length - from));
			dos.flush();
		}
	}

	private static interface RecordInterface {
		long size();
	}
	
	private static class TopRecord implements RecordInterface {
		final int	magic = TOP_MAGIC;
		long		clusterRef;
		long		freeRef;
		
		TopRecord(final long clusterRef, final long freeRef) {
			this.clusterRef = clusterRef;
			this.freeRef = freeRef;
		}

		@Override
		public long size() {
			return 20;
		}
		
		@Override
		public String toString() {
			return "TopRecord [magic=" + magic + ", clusterRef=" + clusterRef + ", freeRef=" + freeRef + "]";
		}
		
		static TopRecord load(final DataInput in) throws IOException {
			final int	magic = in.readInt();
			
			if (magic != TOP_MAGIC) {
				throw new IOException("Illegal magic ["+magic+"] for top record, must be ["+TOP_MAGIC+"]"); 
			}
			else {
				return new TopRecord(in.readLong(), in.readLong());
			}
		}
		
		static void store(final TopRecord rec, final DataOutput out) throws IOException {
			out.writeInt(rec.magic);
			out.writeLong(rec.clusterRef);
			out.writeLong(rec.freeRef);
		}
	}

	private static class ClusterRecord implements RecordInterface {
		final int	magic = CLUSTER_MAGIC;
		long		clusterId;
		long		clusterLocation;
		long		prevClusterLocation;
		long		clusterSize;
		long		clusterDelta;
		int			pieces;

		ClusterRecord(final long clusterId, final long clusterSize) {
			this(clusterId, clusterSize, 0, 0);
		}
		
		ClusterRecord(final long clusterId, final long clusterSize, final long clusterDelta, final int pieces) {
			this.clusterId = clusterId;
			this.clusterLocation = 0;
			this.prevClusterLocation = 0;
			this.clusterSize = clusterSize;
			this.clusterDelta = clusterDelta;
			this.pieces = pieces;
		}

		ClusterRecord(final long clusterId, final long clusterLocation, final long prevClusterLocation, final long clusterSize, final long clusterDelta, final int pieces) {
			this.clusterId = clusterId;
			this.clusterLocation = clusterLocation;
			this.prevClusterLocation = prevClusterLocation;
			this.clusterSize = clusterSize;
			this.clusterDelta = clusterDelta;
			this.pieces = pieces;
		}

		@Override
		public long size() {
			return 48;
		}
		
		@Override
		public String toString() {
			return "ClusterRecord [magic=" + magic + ", clusterId=" + clusterId + ", clusterLocation=" + clusterLocation
					+ ", prevClusterLocation=" + prevClusterLocation + ", clusterSize=" + clusterSize
					+ ", clusterDelta=" + clusterDelta + ", pieces=" + pieces + "]";
		}

		static ClusterRecord load(final DataInput in) throws IOException {
			final int	magic = in.readInt();
			
			if (magic != CLUSTER_MAGIC) {
				throw new IOException("Illegal magic ["+magic+"] for cluster record, must be ["+CLUSTER_MAGIC+"]"); 
			}
			else {
				return new ClusterRecord(in.readLong(), in.readLong(), in.readLong(), in.readLong(),in.readLong(), in.readInt());
			}
		}
		
		static void store(final ClusterRecord rec, final DataOutput out) throws IOException {
			out.writeInt(rec.magic);
			out.writeLong(rec.clusterId);
			out.writeLong(rec.clusterLocation);
			out.writeLong(rec.prevClusterLocation);
			out.writeLong(rec.clusterSize);
			out.writeLong(rec.clusterDelta);
			out.writeInt(rec.pieces);
		}
	}
}
