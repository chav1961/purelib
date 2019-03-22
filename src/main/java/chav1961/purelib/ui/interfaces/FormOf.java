package chav1961.purelib.ui.interfaces;

public interface FormOf<Nested> {
	Class<Nested> getComponentType();
	void action(final String action);
}
