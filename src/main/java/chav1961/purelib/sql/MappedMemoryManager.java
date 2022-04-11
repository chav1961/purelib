package chav1961.purelib.sql;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.channels.FileChannel;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.sql.interfaces.RawMemoryInterface;

public class MappedMemoryManager implements RawMemoryInterface, Closeable, Flushable {
	public MappedMemoryManager(final LoggerFacade logger, final FileChannel channel) throws IOException {
		
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public long createMemoryCluster(final long clusterSize) throws IOException {
		return createMemoryCluster(clusterSize,0,0);
	}

	@Override
	public long createMemoryCluster(final long clusterSize, final long cluterDelta, final int maxPiece) throws IOException {
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

}
