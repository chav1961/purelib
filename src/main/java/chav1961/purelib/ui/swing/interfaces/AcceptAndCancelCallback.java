package chav1961.purelib.ui.swing.interfaces;

/**
 * <p>This interface is called when user presses 'accept' or 'cancel' buttons in the dialog.
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @param <T> owner of the callback (for example, JDialog)
 */
@FunctionalInterface
public interface AcceptAndCancelCallback<T> {
	/**
	 * <p>Process pressing button</p>
	 * @param owner owner of the button
	 * @param accept true if the 'accept' button was pressed, false otherwise
	 * @return true if processing is successful
	 */
	boolean process(T owner, boolean accept);
}