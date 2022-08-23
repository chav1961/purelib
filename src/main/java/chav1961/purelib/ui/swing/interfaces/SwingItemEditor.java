package chav1961.purelib.ui.swing.interfaces;

public interface SwingItemEditor<T, R> {
	boolean canServe(Class<T> class2Edit, Class<R> editorType, Object... options);
	R getEditor(Class<R> editorType, Object... options);
}
