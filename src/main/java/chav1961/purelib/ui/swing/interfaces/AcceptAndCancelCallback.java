package chav1961.purelib.ui.swing.interfaces;

/**
 * <p>This interface is called when user presses 'accept' or 'cancel' buttons in the dialog.
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface AcceptAndCancelCallback {
	/**
	 * <p>Process pressing button</p>
	 * @param accept true if the 'accept' button was pressed, false otherwise
	 */
	void process(boolean accept);
}