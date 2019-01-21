package chav1961.purelib.ui.swing.useful;

import java.util.Set;

import javax.swing.JPanel;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ProgressIndicator;

/**
 * <p>This is a swing component for state string at the bottom of the application window. It can show any messages, view short history of them and indicate some
 * long operations with the progress bar. Long operations can be cancelled via the 'cancel' button appeared in the state string. When progress started, any messages
 * will be hidden until the progress operations completed or cancelled and will be restored after.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @see LoggerFacade 
 * @see ProgressIndicator
 * @see JTextPaneHighlighter
 * @since 0.0.3
 */

public class JStateString extends JPanel implements LoggerFacade, ProgressIndicator {
	private static final long serialVersionUID = 5199220144621261938L;

	/**
	 * <p>This lambda-oriented interface will be called on pressing 'cancel' button 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface CancelCallback {
		/**
		 * <p>Process 'cancel' button pressing
		 * @param stage long processing stage number
		 * @param total total planned to process on the current stage
		 * @param processed still processed on the current stage
		 * @return true - cancel processing, false - continue one
		 */
		boolean cancel(int stage, long total, long processed);
	}

	/**
	 * <p>Create ordinal state string with no history and no logging</p>
	 */
	public JStateString() {
		
	}
	
	/**
	 * <p>Create state string with history of the given depth and no logging</p>
	 * @param historyDepth depth of the history. Must be positive
	 */
	public JStateString(final int historyDepth) {
		
	}
	
	/**
	 * <p>Create state string with no history and logging</p>
	 * @param dumpedTo logger to dump all messages were typed in the state string
	 */
	public JStateString(final LoggerFacade dumpedTo) {
		
	}

	/**
	 * <p>Create state string with history of the given depth and logging</p>
	 * @param dumpedTo logger to dump all messages were typed in the state string
	 * @param historyDepth depth of the history. Must be positive
	 */
	public JStateString(final LoggerFacade dumpedTo, final int historyDepth) {
		
	}

	/**
	 * <p>Assign cancel callback before long operation. This callback will be called on pressing 'cancel' button
	 * @param callback
	 */
	public void assignCancelCallback(CancelCallback callback) {
		
	}
	
	@Override
	public void start(String caption, long total) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(String caption) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean processed(long processed) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LoggerFacade message(Severity level, String format, Object... parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade message(Severity level, LoggerCallbackInterface callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade message(Severity level, Throwable exception, String format, Object... parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade message(Severity level, Throwable exception, LoggerCallbackInterface callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLoggedNow(Severity level) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<Reducing> getReducing() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade setReducing(Set<Reducing> reducing) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade setReducing(Reducing... reducing) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade pushReducing(Set<Reducing> reducing) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade pushReducing(Reducing... reducing) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade popReducing() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade transaction(String mark) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rollback() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
