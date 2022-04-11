package chav1961.purelib.sql.interfaces;

import java.io.IOException;

public interface RawMemoryInterface {
	long createMemoryCluster(long clusterSize) throws IOException;
	long createMemoryCluster(long clusterSize, long cluterDelta, int maxPiece) throws IOException;
	void dropMemoryCluster(long clusterId) throws IOException;
	long allocate(long clusterId, long size) throws IOException;
	void free(long clusterId, long address, long size) throws IOException;
}
