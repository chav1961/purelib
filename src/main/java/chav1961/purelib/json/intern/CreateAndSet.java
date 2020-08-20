package chav1961.purelib.json.intern;

public interface CreateAndSet {
	Object newInstance(final int classId);
	void setValue(final Object instance, final int classAndfieldId, final Object value);
}