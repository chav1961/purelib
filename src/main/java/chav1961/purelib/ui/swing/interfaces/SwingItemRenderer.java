package chav1961.purelib.ui.swing.interfaces;

import chav1961.purelib.model.FieldFormat;

public interface SwingItemRenderer<T, R> {
	boolean canServe(Class<T> class2Render, Class<R> rendererType, Object... options);
	R getRenderer(Class<R> rendererType, FieldFormat ff, Object... options);
}
