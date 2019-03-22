package chav1961.purelib.ui.interfaces;

public interface WizardOf<Nested> {
	Class<Nested> getComponentType();
	void next();
	void prev();
	void finish();
	void cancel();
}
