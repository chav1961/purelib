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
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper.Locker;
import chav1961.purelib.io.interfaces.RawMemoryInterface;

public class MappedMemoryManager implements RawMemoryInterface, Closeable, Flushable, LoggerFacadeOwner {
	private static final int	TOP_MAGIC = 0xFFEFCDED;
	private static final int	CLUSTER_MAGIC = 0xFFEFCDDE;
	
	private final LoggerFacade						logger;
	private final RandomAccessFile					raf; 
	private final FileChannel						channel;
	private final Charset							charset;
	private final TopRecord							top;
	private final LightWeightRWLockerWrapper		clusterWrapper = new LightWeightRWLockerWrapper();
	private final List<ClusterRecordAndLocation>	clusters = new ArrayList<>();
	private volatile long 							lastRemovedCluster = -1;
	
	public MappedMemoryManager(final LoggerFacade logger, final File file, final Charset charset) throws IOException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (file == null) {
			throw new NullPointerException("File channel can't be null"); 
		}
		else if (charset == null) {
			throw new NullPointerException("Charset can't be null"); 
		}
		else {
			this.logger = logger;
			this.raf = new RandomAccessFile(file, "rw");
			this.channel = this.raf.getChannel();
			this.charset = charset;
			this.top = TopRecord.load(getMap(0,TopRecord.sizeOf()));
			if (this.top.clusterRef != 0) {
				preloadClusters();
			}
		}
	}
	
	public MappedMemoryManager(final LoggerFacade logger, final FileChannel channel, final Charset charset) throws IOException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (channel == null) {
			throw new NullPointerException("File channel can't be null"); 
		}
		else if (charset == null) {
			throw new NullPointerException("Charset can't be null"); 
		}
		else {
			this.logger = logger;
			this.raf = null;
			this.channel = channel;
			this.charset = charset;
			this.top = TopRecord.load(getMap(0,TopRecord.sizeOf()));
			if (this.top.clusterRef != 0) {
				preloadClusters();
			}
		}
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}
	
	@Override
	public void flush() throws IOException {
		channel.force(true);
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
		final long		ref = top.freeRefHigh-ClusterRecord.sizeOf();
		
		try(final Locker l = clusterWrapper.lock(false)) {
			if (top.clusterRef == 0) {
				try(final MappedDataInputOutput	mdioCluster = getMap(ref, ClusterRecord.sizeOf());
					final MappedDataInputOutput	mdioTop = getMap(0, TopRecord.sizeOf())) {
					
					final ClusterRecord	cr = new ClusterRecord(0, clusterSize, clusterDelta, maxPiece);
					
					ClusterRecord.store(cr, mdioCluster);
					top.clusterRef = ref;
					top.freeRefHigh = ref;
					TopRecord.store(top, mdioTop);
					clusters.add(new ClusterRecordAndLocation(cr, ref));
					return clusters.size()-1;
				}
			}
			else {
				try(final MappedDataInputOutput	mdioCluster = getMap(ref, ClusterRecord.sizeOf());
					final MappedDataInputOutput	mdioClusterOld = getMap(getLastClusterRef(), ClusterRecord.sizeOf());
					final MappedDataInputOutput	mdioTop = getMap(clusters.size(), TopRecord.sizeOf())) {
					
					final ClusterRecord	cr = new ClusterRecord(newClusterId(), clusterSize, clusterDelta, maxPiece);
					final ClusterRecord	crOld = getLastCluster();
					
					ClusterRecord.store(cr, mdioCluster);
					crOld.prevClusterLocation = ref;
					ClusterRecord.store(crOld, mdioClusterOld);
					
					top.clusterRef = ref;
					top.freeRefHigh = ref;
					TopRecord.store(top, mdioTop);
					clusters.add(new ClusterRecordAndLocation(cr, ref));
					return clusters.size()-1;
				}
			}
		} finally {
			flush();
		}
	}

	@Override
	public void dropMemoryCluster(final long clusterId) throws IOException {
		// TODO Auto-generated method stub
		lastRemovedCluster = clusterId;
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
			final TopRecord			top = new TopRecord(0, TopRecord.sizeOf(), size, size);
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

	private void preloadClusters() throws IOException {
		long	ref = top.clusterRef;
		
		do {
			try(final MappedDataInputOutput	mdioCluster = getMap(ref, ClusterRecord.sizeOf())) {
				final ClusterRecord			clr = ClusterRecord.load(mdioCluster);
				
				clusters.add(new ClusterRecordAndLocation(clr, ref));
				ref = clr.prevClusterLocation;
			}
		} while (ref != 0);  
	}

	private long newClusterId() {
		long	clusterId = 0;
		
		for (ClusterRecordAndLocation item : clusters) {
			if (item.record.clusterId > clusterId) {
				clusterId = item.record.clusterId; 
			}
		}
		return Math.max(clusterId, lastRemovedCluster + 1);
	}
	
	private MappedDataInputOutput getMap(final long address, final long size) throws IOException {
		return new MappedDataInputOutput(channel.map(MapMode.READ_WRITE, address, size), charset, (m)->{});
	}

	private ClusterRecord getLastCluster() {
		for (ClusterRecordAndLocation item: clusters) {
			if (item.record.prevClusterLocation == 0) {
				return item.record;
			}
		}
		throw new IllegalArgumentException();
	}

	private long getLastClusterRef() {
		for (ClusterRecordAndLocation item: clusters) {
			if (item.record.prevClusterLocation == 0) {
				return item.location;
			}
		}
		throw new IllegalArgumentException();
	}

	
	private static interface RecordInterface {
		long size();
	}
	
	private static class TopRecord implements RecordInterface {
		final int	magic = TOP_MAGIC;
		long		clusterRef;
		long		freeRefLow;
		long		freeRefHigh;
		long		size;
		
		TopRecord(final long clusterRef, final long freeRefLow, final long freeRefHigh, final long size) {
			this.clusterRef = clusterRef;
			this.freeRefLow = freeRefLow;
			this.freeRefHigh = freeRefHigh;
			this.size = size;
		}

		@Override
		public long size() {
			return sizeOf();
		}

		@Override
		public String toString() {
			return "TopRecord [magic=" + magic + ", clusterRef=" + clusterRef + ", freeRefLow=" + freeRefLow + ", freeRefHigh=" + freeRefHigh + ", size=" + size + "]";
		}

		static TopRecord load(final DataInput in) throws IOException {
			final int	magic = in.readInt();
			
			if (magic != TOP_MAGIC) {
				throw new IOException("Illegal magic ["+magic+"] for top record, must be ["+TOP_MAGIC+"]"); 
			}
			else {
				return new TopRecord(in.readLong(), in.readLong(), in.readLong(), in.readLong());
			}
		}
		
		static void store(final TopRecord rec, final DataOutput out) throws IOException {
			out.writeInt(rec.magic);
			out.writeLong(rec.clusterRef);
			out.writeLong(rec.freeRefLow);
			out.writeLong(rec.freeRefHigh);
			out.writeLong(rec.size);
		}
		
		static int sizeOf() {
			return 36;
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
			return sizeOf();
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
		
		static int sizeOf() {
			return 48;
		}
	}
	
	private static class ClusterRecordAndLocation {
		private ClusterRecord	record;
		private long			location;
		
		ClusterRecordAndLocation(ClusterRecord record, long location) {
			this.record = record;
			this.location = location;
		}

		@Override
		public String toString() {
			return "ClusterRecordAndLocation [record=" + record + ", location=" + location + "]";
		}
	}
}
