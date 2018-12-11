package chav1961.purelib.ui.interfacers;

public interface FormOf<Nested> {
	Class<Nested> getComponentType();
	void action(final String action);
}
