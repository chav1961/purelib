package chav1961.purelib.ui.interfaces;

@FunctionalInterface
public interface Filter<Nested> {
	boolean accept(Nested item);
}
