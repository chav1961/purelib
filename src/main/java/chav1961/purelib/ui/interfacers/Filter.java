package chav1961.purelib.ui.interfacers;

@FunctionalInterface
public interface Filter<Nested> {
	boolean accept(Nested item);
}
