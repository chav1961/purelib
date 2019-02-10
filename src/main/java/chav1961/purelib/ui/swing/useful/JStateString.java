package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import chav1961.purelib.basic.AbstractLoggerFacade;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper.Locker;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;

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

public class JStateString extends JPanel implements LoggerFacade, ProgressIndicator, LocaleChangeListener {
	private static final long 		serialVersionUID = 5199220144621261938L;
	private static final String		STATESTRING_HISTORY = "JStateString.history";
	private static final Object[]	EMPTY_LIST = new Object[0];
	private static final String		COMMON_PANEL = "commonPanel";
	private static final String		STAGED_PANEL = "stagedPanel";
	private static final int		STATE_PLAIN = 0;
	private static final int		STATE_COMMON = 1;
	private static final int		STATE_STAGED = 2;

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

	private final Localizer			localizer;
	private final LoggerFacade		delegate = new InternalLoggerFacade();
	private final LoggerFacade		dump;
	private final List<Message>		history = new ArrayList<>();
	private final int				maxCapacity;
	private final JLabel			state = new JLabel();
	private final JProgressBar		stage = new JProgressBar();
	private final JProgressBar		step = new JProgressBar();
	private final JProgressBar		common = new JProgressBar();
	private final Icon				viewIcon = new ImageIcon(this.getClass().getResource("levelUp.png"));
	private final JButton			historyView = new JButton(viewIcon);
	private final Icon				cancelIcon = new ImageIcon(this.getClass().getResource("delete.png"));
	private final JButton			cancelStaged = new JButton(cancelIcon);
	private final JButton			cancelCommon = new JButton(cancelIcon);
	private final JPanel			rightPanel = new JPanel(new CardLayout());
	private final LightWeightRWLockerWrapper	locker = new LightWeightRWLockerWrapper();
	private final HistoryTableModel	model;
	private int 					currentState = STATE_PLAIN; 
	private volatile CancelCallback	currentCallback = null;
	private volatile boolean		canceled = false;
	
	/**
	 * <p>Create ordinal state string with no history and no logging</p>
	 * @param localizer localizer to use in messages and progress indicators
	 * @throws NullPointerException
	 */
	public JStateString(final Localizer localizer) throws NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			this.model = new HistoryTableModel(localizer, history);
			this.maxCapacity = 0;
			this.dump = null;
			prepareControls();
			fillLocalizedStrings();
		}
	}
	
	/**
	 * <p>Create state string with history of the given depth and no logging</p>
	 * @param localizer localizer to use in messages and progress indicators
	 * @param historyDepth depth of the history. Must be positive
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 */
	public JStateString(final Localizer localizer, final int historyDepth) throws NullPointerException, IllegalArgumentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (historyDepth <= 0) {
			throw new IllegalArgumentException("History depth ["+historyDepth+"] must be positive");
		}
		else {
			this.localizer = localizer;
			this.model = new HistoryTableModel(localizer, history);
			this.maxCapacity = historyDepth;
			this.dump = null;
			prepareControls();
			fillLocalizedStrings();
		}
	}
	
	/**
	 * <p>Create state string with no history and logging</p>
	 * @param localizer localizer to use in messages and progress indicators
	 * @param dumpedTo logger to dump all messages were typed in the state string
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 */
	public JStateString(final Localizer localizer, final LoggerFacade dumpedTo) throws NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (dumpedTo == null) {
			throw new NullPointerException("Logger dump can't be null");
		}
		else {
			this.localizer = localizer;
			this.model = new HistoryTableModel(localizer, history);
			this.maxCapacity = 0;
			this.dump = dumpedTo;
			prepareControls();
			fillLocalizedStrings();
		}
	}

	/**
	 * <p>Create state string with history of the given depth and logging</p>
	 * @param dumpedTo logger to dump all messages were typed in the state string
	 * @param historyDepth depth of the history. Must be positive
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 */
	public JStateString(final Localizer localizer, final LoggerFacade dumpedTo, final int historyDepth) throws NullPointerException, IllegalArgumentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (dumpedTo == null) {
			throw new NullPointerException("Logger dump can't be null");
		}
		else if (historyDepth <= 0) {
			throw new IllegalArgumentException("History depth ["+historyDepth+"] must be positive");
		}
		else {
			this.localizer = localizer;
			this.model = new HistoryTableModel(localizer, history);
			this.maxCapacity = historyDepth;
			this.dump = dumpedTo;
			prepareControls();
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}; 
	
	/**
	 * <p>Assign cancel callback before long operation. This callback will be called on pressing 'cancel' button
	 * @param callback. Null turned off canceling
	 */
	public void assignCancelCallback(final CancelCallback callback) {
		try(final Locker item = locker.lock(false)) {
			this.currentCallback = callback;
		}
	}
	
	@Override
	public void start(final String caption, final long total) throws IllegalStateException {
		if (currentState != STATE_PLAIN) {
			throw new IllegalStateException("Attempt to open nested progress. Use staged progressing instead");
		}
		else if (caption == null) {
			throw new NullPointerException("Caption string can't be null");
		}
		else {
			if (!caption.isEmpty()) {
				common.setStringPainted(true);
				try{common.setString(localize(caption));
				} catch (LocalizationException e) {
					common.setString(caption);
				}
			}
			else {
				common.setStringPainted(false);
			}
			common.setMinimum(0);
			common.setMaximum((int)total);
			common.setIndeterminate(total <= 0);
			common.setValue(0);
			currentState = STATE_COMMON;
			((CardLayout)rightPanel.getLayout()).show(rightPanel, COMMON_PANEL);
			rightPanel.setVisible(true);
		}
	}

	@Override
	public void start(final String caption) {
		if (currentState != STATE_PLAIN) {
			throw new IllegalStateException("Attempt to open nested progress. Use staged progressing instead");
		}
		else if (caption == null) {
			throw new NullPointerException("Caption string can't be null");
		}
		else {
			if (!caption.isEmpty()) {
				common.setStringPainted(true);
				try{common.setString(localize(caption));
				} catch (LocalizationException e) {
					common.setString(caption);
				}
			}
			else {
				common.setStringPainted(false);
			}
			common.setMinimum(0);
			common.setMaximum(0);
			common.setIndeterminate(true);
			common.setValue(0);
			currentState = STATE_COMMON;
			((CardLayout)rightPanel.getLayout()).show(rightPanel, COMMON_PANEL);
			rightPanel.setVisible(true);
		}
	}

	@Override
	public void stage(final String caption, final int stageNo, final int of, final long total) {
		if (currentState == STATE_PLAIN) {
			throw new IllegalStateException("Attempt to stage non-progressed. Use staged progressing instead");
		}
		else if (caption == null) {
			throw new NullPointerException("Caption string can't be null");
		}
		else {
			if (currentState == STATE_COMMON) {
				if (common.isStringPainted()) {
					stage.setStringPainted(true);
					stage.setString(common.getString());
				}
				else {
					stage.setStringPainted(false);
				}
				stage.setMinimum(0);
				stage.setMaximum(of);
				stage.setIndeterminate(false);
				stage.setValue(stageNo);
				currentState = STATE_STAGED;
				((CardLayout)rightPanel.getLayout()).show(rightPanel, STAGED_PANEL);
			}
			if (!caption.isEmpty()) {
				step.setStringPainted(true);
				try{step.setString(localize(caption));
				} catch (LocalizationException e) {
					step.setString(caption);
				}
			}
			else {
				step.setStringPainted(false);
			}
			step.setMinimum(0);
			step.setMaximum((int)total);
			step.setIndeterminate(total <= 0);
			step.setValue(0);
			canceled = false;
		}
	}

	@Override
	public void stage(final String caption, final int stageNo, final int of) {
		if (currentState == STATE_PLAIN) {
			throw new IllegalStateException("Attempt to stage non-progressed. Use staged progressing instead");
		}
		else if (caption == null) {
			throw new NullPointerException("Caption string can't be null");
		}
		else {
			if (currentState == STATE_COMMON) {
				if (common.isStringPainted()) {
					stage.setStringPainted(true);
					stage.setString(common.getString());
				}
				else {
					stage.setStringPainted(false);
				}
				stage.setMinimum(0);
				stage.setMaximum(of);
				stage.setIndeterminate(false);
				stage.setValue(stageNo);
				currentState = STATE_STAGED;
				((CardLayout)rightPanel.getLayout()).show(rightPanel, STAGED_PANEL);
			}
			if (!caption.isEmpty()) {
				step.setStringPainted(true);
				try{step.setString(localize(caption));
				} catch (LocalizationException e) {
					step.setString(caption);
				}
			}
			else {
				step.setStringPainted(false);
			}
			step.setMinimum(0);
			step.setMaximum(0);
			step.setIndeterminate(true);
			step.setValue(0);
			canceled = false;
		}
	}
	
	@Override
	public boolean processed(final long processed) {
		if (currentState == STATE_PLAIN) {
			throw new IllegalStateException("Attempt to stage non-progressed. Use staged progressing instead");
		}
		else if (currentState == STATE_COMMON) {
			common.setValue((int)processed);
			return !canceled;
		}
		else {
			step.setValue((int)processed);
			return !canceled;
		}
	}

	@Override
	public int endStage() {
		return -1;
	}
	
	@Override
	public void end() {
		rightPanel.setVisible(false);
		currentState = STATE_PLAIN;
	}
	
	@Override
	public LoggerFacade message(final Severity level, final String format, final Object... parameters) {
		try{delegate.message(level, localize(format), parameters);
		} catch (LocalizationException e) {
			delegate.message(level, format, parameters);		}
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final LoggerCallbackInterface callback) {
		delegate.message(level, callback);
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final Throwable exception, final String format, final Object... parameters) {
		try{delegate.message(level, exception, localize(format), parameters);
		} catch (LocalizationException e) {
			delegate.message(level, exception, format, parameters);		
		}
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final Throwable exception, final LoggerCallbackInterface callback) {
		delegate.message(level, exception, callback);
		return this;
	}

	@Override
	public boolean isLoggedNow(final Severity level) {
		return delegate.isLoggedNow(level);
	}

	@Override
	public Set<Reducing> getReducing() {
		return delegate.getReducing();
	}

	@Override
	public LoggerFacade setReducing(final Set<Reducing> reducing) {
		delegate.setReducing(reducing);
		return this;
	}

	@Override
	public LoggerFacade setReducing(final Reducing... reducing) {
		delegate.setReducing(reducing);
		return this;
	}

	@Override
	public LoggerFacade pushReducing(final Set<Reducing> reducing) {
		delegate.pushReducing(reducing);
		return this;
	}

	@Override
	public LoggerFacade pushReducing(final Reducing... reducing) {
		delegate.pushReducing(reducing);
		return this;
	}

	@Override
	public LoggerFacade popReducing() {
		delegate.popReducing();
		return this;
	}

	@Override
	public LoggerFacade transaction(final String mark) {
		return delegate.transaction(mark);
	}

	@Override
	public void rollback() {
		delegate.rollback();
	}

	@Override
	public void close() {
		delegate.close();
	}

	private void prepareControls() {
		final SpringLayout	springStaged = new SpringLayout();
		final JPanel		stagedPanel = new JPanel(springStaged);
		final SpringLayout	springCommon = new SpringLayout();
		final JPanel		commonPanel = new JPanel(springCommon);
		
		historyView.setPreferredSize(new Dimension(viewIcon.getIconWidth()+2,viewIcon.getIconHeight()+2));
		cancelCommon.setPreferredSize(new Dimension(cancelIcon.getIconWidth()+2,cancelIcon.getIconHeight()+2));
		cancelStaged.setPreferredSize(new Dimension(cancelIcon.getIconWidth()+2,cancelIcon.getIconHeight()+2));
		common.setPreferredSize(new Dimension(100,cancelIcon.getIconHeight()+2));
		stage.setPreferredSize(new Dimension(100,cancelIcon.getIconHeight()+2));
		
		commonPanel.add(common);
		commonPanel.add(cancelCommon);
		
		commonPanel.setPreferredSize(new Dimension(100,20));
		stagedPanel.setPreferredSize(new Dimension(100,20));
		
		springCommon.putConstraint(SpringLayout.NORTH, common, 0, SpringLayout.NORTH, commonPanel);
		springCommon.putConstraint(SpringLayout.SOUTH, common, 0, SpringLayout.SOUTH, commonPanel);
		springCommon.putConstraint(SpringLayout.WEST, common, 0, SpringLayout.WEST, commonPanel);
		springCommon.putConstraint(SpringLayout.NORTH, cancelCommon, 0, SpringLayout.NORTH, commonPanel);
		springCommon.putConstraint(SpringLayout.SOUTH, cancelCommon, 0, SpringLayout.SOUTH, commonPanel);
		springCommon.putConstraint(SpringLayout.EAST, cancelCommon, 0, SpringLayout.EAST, commonPanel);
		springCommon.putConstraint(SpringLayout.EAST, common, 0, SpringLayout.WEST, cancelCommon);
		
		stagedPanel.add(stage);
		stagedPanel.add(step);
		stagedPanel.add(cancelStaged);
		
		springStaged.putConstraint(SpringLayout.NORTH, stage, 0, SpringLayout.NORTH, stagedPanel);
		springStaged.putConstraint(SpringLayout.SOUTH, stage, 0, SpringLayout.SOUTH, stagedPanel);
		springStaged.putConstraint(SpringLayout.WEST, stage, 0, SpringLayout.WEST, stagedPanel);
		springStaged.putConstraint(SpringLayout.NORTH, step, 0, SpringLayout.NORTH, stagedPanel);
		springStaged.putConstraint(SpringLayout.SOUTH, step, 0, SpringLayout.SOUTH, stagedPanel);
		springStaged.putConstraint(SpringLayout.WEST, step, 0, SpringLayout.EAST, stage);
		springStaged.putConstraint(SpringLayout.NORTH, cancelStaged, 0, SpringLayout.NORTH, stagedPanel);
		springStaged.putConstraint(SpringLayout.SOUTH, cancelStaged, 0, SpringLayout.SOUTH, stagedPanel);
		springStaged.putConstraint(SpringLayout.EAST, cancelStaged, 0, SpringLayout.EAST, stagedPanel);
		springStaged.putConstraint(SpringLayout.EAST, step, 0, SpringLayout.WEST, cancelStaged);

		rightPanel.add(commonPanel, COMMON_PANEL);
		rightPanel.add(stagedPanel, STAGED_PANEL);
		((CardLayout)rightPanel.getLayout()).show(rightPanel,COMMON_PANEL);
		rightPanel.setVisible(false);

		if (maxCapacity > 0) {
			final JPanel		innerPanel = new JPanel();
			final GroupLayout 	layout = new GroupLayout(innerPanel);

			innerPanel.setLayout(layout);
			layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(state).addComponent(rightPanel));
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           .addComponent(state)
			           .addComponent(rightPanel)
			           )
			);
			
			setLayout(new BorderLayout(2,2));
			add(innerPanel,BorderLayout.CENTER);
			add(historyView,BorderLayout.EAST);
		}
		else {
			final GroupLayout 	layout = new GroupLayout(this);
			
			setLayout(layout);
			layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(state).addComponent(rightPanel));
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           .addComponent(state)
			           .addComponent(rightPanel)
			           )
			);
		}
		cancelCommon.addActionListener((e)->{
			try(final Locker item = locker.lock(true)) {
				if (currentCallback != null) {
					canceled = currentCallback.cancel(currentState == STATE_COMMON ? 0 : stage.getValue()
								,currentState == STATE_COMMON ? common.getMaximum() : step.getMaximum()
								,currentState == STATE_COMMON ? common.getValue() : step.getValue());
				}
			}
		});
		cancelStaged.addActionListener((e)->{
			try(final Locker item = locker.lock(true)) {
				if (currentCallback != null) {
					canceled = currentCallback.cancel(currentState == STATE_COMMON ? 0 : stage.getValue()
								,currentState == STATE_COMMON ? common.getMaximum() : step.getMaximum()
								,currentState == STATE_COMMON ? common.getValue() : step.getValue());
				}
			}
		});
		historyView.addActionListener((e)->{viewHistory();});
	}

	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}

	private String localize(final String source) throws LocalizationException {
		if (localizer.containsKey(source)) {
			return localizer.getValue(source);
		}
		else {
			return source;
		}
	}
	
	private void viewHistory() {
		final JTable		table = new JTable(model);
		final Point			location = historyView.getLocationOnScreen();
		final JScrollPane	pane = new JScrollPane(table);
		final Dimension		parentSize = getParent() != null ? getParent().getSize() : new Dimension(400,200); 
		
		pane.setPreferredSize(new Dimension(parentSize.width/2,parentSize.height/2));
		
		final Popup 		window = PopupFactory.getSharedInstance().getPopup(this.getParent(),pane,location.x-parentSize.width/2,location.y-parentSize.height/2);

		table.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(SwingUtils.KS_EXIT,SwingUtils.ACTION_EXIT);
		table.getActionMap().put(SwingUtils.ACTION_EXIT,new AbstractAction() {private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				window.hide();
			}
		});
		window.show();
	}

	private static class Message {
		final Severity	severity;
		final Throwable	exception;
		final String	message;
		final Object[]	parameters;
		
		public Message(Severity severity, Throwable exception, String message, Object[] parameters) {
			this.severity = severity;
			this.exception = exception;
			this.message = message;
			this.parameters = parameters;
		}

		@Override
		public String toString() {
			return "Message [severity=" + severity + ", exception=" + exception + ", message=" + message + ", parameters=" + Arrays.toString(parameters) + "]";
		}
	}
	
	private class InternalLoggerFacade extends AbstractLoggerFacade {
		private InternalLoggerFacade() {
		}

		private InternalLoggerFacade(final String mark, final Class<?> root) {
			super(mark,root);
		}

		@Override
		protected AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root) {
			return new InternalLoggerFacade(mark,root);
		}

		@Override
		protected void toLogger(final Severity level, final String text, final Throwable throwable) {
			final Message	message = new Message(level, throwable, text, EMPTY_LIST);
			
			synchronized (history) {
				history.add(0,message);
				while (history.size() > maxCapacity + 1) {
					history.remove(history.size()-1);
				}
			}
			if (dump != null) {
				dump.message(level, throwable, text);
			}
			switch (level) {
				case debug	:
					state.setText("<html><body><font color='gray'>"+text+"</font></body></html>");
					break;
				case error	:
					state.setText("<html><body><font color='red'><b>"+text+"</b></font></body></html>");
					break;
				case info	:
					state.setText("<html><body><font color='black'>"+text+"</font></body></html>");
					break;
				case severe	:
					state.setText("<html><body><font color='red'><b><u>"+text+"</u></b></font></body></html>");
					break;
				case trace	:
					state.setText("<html><body><font color='lightgray'>"+text+"</font></body></html>");
					break;
				case warning:
					state.setText("<html><body><font color='blue'>"+text+"</font></body></html>");
					break;
				default:
					break;
			}
		}
	}

	private static class HistoryTableModel extends DefaultTableModel implements LocaleChangeListener {
		private static final long serialVersionUID = 8285270537680442874L;
		
		private final Localizer 		localizer;
		private final List<Message> 	history;
		
		HistoryTableModel(final Localizer localizer, final List<Message> history) {
			this.localizer = localizer;
			this.history = history;
		}

		@Override
		public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
			fireTableStructureChanged();
		}
		
		@Override
		public int getRowCount() {
			if (history != null) {
				synchronized (history) {
					return history.size();
				}
			}
			else {
				return 0;
			}
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public String getColumnName(int columnIndex) {
			try{return localizer.getValue(STATESTRING_HISTORY);
			} catch (LocalizationException e) {
				return STATESTRING_HISTORY;
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			synchronized (history) {
				return history.get(rowIndex).message;
			}
		}
	}
}
