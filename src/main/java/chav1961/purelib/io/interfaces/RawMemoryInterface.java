package chav1961.purelib.io.interfaces;

import java.io.IOException;

import chav1961.purelib.io.MappedDataInputOutput;

public interface RawMemoryInterface {
	public static interface RawMemoryClusterInterface {
		@FunctionalInterface
		public static interface WalkerCallback {
			boolean process(RawMemoryClusterInterface owner, long address, long size) throws IOException;
		}
		
		long allocate(long size) throws IOException;
		void free(long address, long size) throws IOException;
		MappedDataInputOutput lock(long address, long size) throws IOException;
		boolean walk(WalkerCallback callback) throws IOException;
	}
	
	long createMemoryCluster(long clusterSize) throws IOException;
	long createMemoryCluster(long clusterSize, long cluterDelta, int maxPiece) throws IOException;
	void dropMemoryCluster(long clusterId) throws IOException;
	RawMemoryClusterInterface useMemoryCluster(long clusterId) throws IOException;
}
