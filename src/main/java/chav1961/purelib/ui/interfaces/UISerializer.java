package chav1961.purelib.ui.interfaces;

public interface UISerializer {
	public enum SerializerTarget {
		TARGET1, TARGET2 
	}
	
	<T> void serialize(SerializerTarget content, T target);
}
