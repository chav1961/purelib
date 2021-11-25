package chav1961.purelib.ui.swing.interfaces;

public interface SwingItemRenderer<T, R> {
	boolean canServe(Class<T> class2Render, Class<R> rendererType, Object... options);
	R getRenderer(Class<R> rendererType, Object... options);
}
