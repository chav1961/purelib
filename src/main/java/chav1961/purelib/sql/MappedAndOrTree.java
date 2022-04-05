package chav1961.purelib.sql;

import chav1961.purelib.sql.interfaces.RawAndOrTreeInterface;

public class MappedAndOrTree implements RawAndOrTreeInterface  {
	public MappedAndOrTree() {
		
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
}
