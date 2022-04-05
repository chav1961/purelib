package chav1961.purelib.sql.interfaces;

import chav1961.purelib.enumerations.ContinueMode;
//https://tproger.ru/translations/how-to-make-a-3d-render-engine-in-java/
public interface RawAndOrTreeInterface {
	public interface WalkCallback<T> {
		ContinueMode process(byte [] content, int from, int len, long id, T parameter);
	}
	
	void placeContent(byte[] content, int from, int length, long id);
	long seekContent(byte[] content, int from, int length);
	<T> void walkContent(byte[] content, int from, int length, boolean backwardWalk, WalkCallback<T> callback);
	boolean changeContentId(byte[] content, int from, int length, long id);
	long removeContent(byte[] content, int from, int length);
}
