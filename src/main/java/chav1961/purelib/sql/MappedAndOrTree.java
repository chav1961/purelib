package chav1961.purelib.sql;

import java.io.IOException;
import java.nio.channels.FileChannel;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.sql.interfaces.RawAndOrTreeInterface;

public class MappedAndOrTree implements RawAndOrTreeInterface  {
	public MappedAndOrTree(final LoggerFacade logger, final FileChannel channel) {
		
	}
	
	@Override
	public void placeContent(byte[] content, int from, int length, long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long seekContent(byte[] content, int from, int length) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> void walkContent(byte[] content, int from, int length, boolean backwardWalk, WalkCallback<T> callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean changeContentId(byte[] content, int from, int length, long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long removeContent(byte[] content, int from, int length) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private long allocate(final long size) throws IOException {
		return 0;
	}
	
	private void free(final long address, final long size) throws IOException {
		
	}
}
