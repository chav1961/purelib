package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This class describes Swing panel representing a command line with history support and localization.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
public class JCommandLine extends JPanel implements LocaleChangeListener {
	private static final long serialVersionUID = 1422958843734212884L;
	
	private static final String LABEL_TEXT = "chav1961.purelib.ui.swing.useful.JCommandLine.command";
	private static final String TEXT_TOOLTIP = "chav1961.purelib.ui.swing.useful.JCommandLine.command.tt";

	/**
     * Functional interface for accepting a command string that may throw a CalculationException.
     */
	@FunctionalInterface
	public static interface ThrowableConsumer {
		void accept(String command) throws CalculationException;
	}
	
    private final Localizer localizer;
	private final JLabel command = new JLabel();
    private final JTextField commandField = new JTextField();
    private final JTextArea commandArea = new JTextArea();
    private final boolean multiline;
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;

    /**
     * <p>Constructor of the class instance</p>
     *
     * @param localizer the localization provider; must not be null
     * @param multiline use multiline command string
     * @param executor  the command executor; must not be null
     * @throws NullPointerException if {@code localizer} or {@code executor} is null
     */
    public JCommandLine(final Localizer localizer, final boolean multiline, final ThrowableConsumer executor) throws NullPointerException {
        super(new BorderLayout(5, 5));
        if (localizer == null) {
        	throw new NullPointerException("Localizer can't be null"); 
        }
        else if (executor == null) {
        	throw new NullPointerException("Executor can't be null"); 
        }
        else {
	        this.localizer = localizer;
	        this.multiline = multiline;
	        
	        if (multiline) {
		        SwingUtils.assignActionKey(commandArea, KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), (e)->prevCommand(), "prevCommand");
		        SwingUtils.assignActionKey(commandArea, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), (e)->nextCommand(), "nextCommand");
		        SwingUtils.assignActionKey(commandArea, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), 
		        	(e)->{
			            final String cmd = !Utils.checkEmptyOrNullString(commandArea.getSelectedText()) 
			            						? commandArea.getSelectedText().trim()
			            						: commandArea.getText().trim();
			            
			            executeCommand(cmd, executor);
			            commandArea.setText("");
		        	}
		        , "executeCommand");
		        add(new JScrollPane(commandArea), BorderLayout.CENTER);
	        }
	        else {
		        SwingUtils.assignActionKey(commandField, KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), (e)->prevCommand(), "prevCommand");
		        SwingUtils.assignActionKey(commandField, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), (e)->nextCommand(), "nextCommand");
		        SwingUtils.assignActionListeners(commandField, (e)->{
		            final String cmd = commandField.getText().trim();
		            
		            executeCommand(cmd, executor);
		            commandField.setText("");
		        });
		        add(commandField, BorderLayout.CENTER);
	        }
	        add(command, BorderLayout.WEST);
	        fillLocalizedStrings();
        }
    }

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	/**
     * <p>Saves the command history to the specified {@code Properties} object with a given prefix.</p>
     *
     * @param props  the {@link Properties} object to save to; must not be null
     * @param prefix the prefix for properties keys; must not be null or empty
     * @throws NullPointerException     if {@code props} is null
     * @throws IllegalArgumentException if {@code prefix} is null or empty
     */
	public void saveHistory(final Properties props, final String prefix) throws NullPointerException, IllegalArgumentException {
    	if (props == null) {
        	throw new NullPointerException("Properties can't be null"); 
    	}
    	else if (Utils.checkEmptyOrNullString(prefix)) {
        	throw new IllegalArgumentException("Prefix string can be neither null nor empty"); 
    	}
    	else {
            for (int index = 0; index < commandHistory.size(); index++) {
                props.setProperty(prefix + ".cmd." + index, commandHistory.get(index));
            }
            props.setProperty(prefix + ".size", String.valueOf(commandHistory.size()));
    	}
    }

	/**
     * <p>Loads command history from the specified {@code Properties} object with a given prefix.</p>
     *
     * @param props  the {@link Properties} object to load from; must not be null
     * @param prefix the prefix for properties keys; must not be null or empty
     * @throws NullPointerException     if {@code props} is null
     * @throws IllegalArgumentException if {@code prefix} is null or empty
     */
	public void loadHistory(final Properties props, final String prefix)  throws NullPointerException, IllegalArgumentException {
    	if (props == null) {
        	throw new NullPointerException("Properties can't be null"); 
    	}
    	else if (Utils.checkEmptyOrNullString(prefix)) {
        	throw new IllegalArgumentException("Prefix string can be neither null nor empty"); 
    	}
    	else {
	        final String sizeStr = props.getProperty(prefix + ".size");
	        
	        if (sizeStr != null) {
		        commandHistory.clear();
	            try {
	                for (int index = 0, maxIndex = Integer.parseInt(sizeStr); index < maxIndex; index++) {
	                    final String cmd = props.getProperty(prefix + ".cmd." + index);
	                    
	                    if (cmd != null) {
	                        commandHistory.add(cmd);
	                    }
	                }
	            } catch (NumberFormatException e) {
	            	SwingUtils.getNearestLogger(this).message(Severity.warning, e, e.getLocalizedMessage());
	            }
		        historyIndex = -1;
	        }
    	}
    }	
	
	private void prevCommand() {
    	if (!commandHistory.isEmpty()) {
	        if (historyIndex < commandHistory.size() - 1) {
	            historyIndex++;
	            final String value = commandHistory.get(commandHistory.size() - 1 - historyIndex);
	            
	            if (multiline) {
		            commandArea.setText(value);
	            }
	            else {
		            commandField.setText(value);
	            }
	        }
    	}
    }

    private void nextCommand() {
    	if (!commandHistory.isEmpty()) {
	        if (historyIndex > 0) {
	            historyIndex--;
	            final String value = commandHistory.get(commandHistory.size() - 1 - historyIndex);
	            
	            if (multiline) {
		            commandArea.setText(value);
	            }
	            else {
		            commandField.setText(value);
	            }
	        } 
	        else {
	            historyIndex = -1;
	            
	            if (multiline) {
		            commandArea.setText("");
	            }
	            else {
		            commandField.setText("");
	            }
	        }
    	}
    }

    private void executeCommand(final String cmd, final ThrowableConsumer executor) {
        if (!cmd.isEmpty()) {
        	try {
        		executor.accept(cmd);
                commandHistory.add(cmd);
                historyIndex = -1;
        	} catch (CalculationException exc) {
        		SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
        	}
        }
    }
    
    private void fillLocalizedStrings() {
		command.setText(localizer.getValue(LABEL_TEXT));
		commandField.setToolTipText(localizer.getValue(TEXT_TOOLTIP));
	}
}

