package chav1961.purelib.ui.swing.useful;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JTextField;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This class is a simple command history keeper. In can be used to keep all command line strings and undo/redo them. 
 * It can be useful to reduce command input from command line. A set if static methods allow to assign command history to Swing 
 * {@linkplain JTextField} class instance to support command history on it. Undo/redo actions will be supported on this instance
 * by typing "Down"/"Up" buttons on the keyboard</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
public class CommandHistory {
	private final int			maxDepth;
	private final List<String>	commands;
	private int		cursor = 0;
	
	/**
	 * <p>Constructor of the class instance.</p>
	 * @param maxDepth max history depth. O means infinity. Can not be less than 0
	 * @throws maxDepth is less than 0 
	 */
	public CommandHistory(final int maxDepth) throws IllegalArgumentException {
		if (maxDepth < 0) {
			throw new IllegalArgumentException("Max command depth ["+maxDepth+"] must be greater or equals than 0");
		}
		else {
			this.maxDepth = maxDepth;
			this.commands = maxDepth == 0 ? new ArrayList<>() : new ArrayList<>(maxDepth);
		}
	}
	
	/**
	 * <p>Loads command history from external.</p>
	 * @param in reader to load command history from. Can't be null. Every command must occupy separate line in the reader input, 
	 * empty lines will be ignored
	 * @throws NullPointerException reader is null
	 * @throws IOException on any I/O error
	 */
	public void load(final Reader in) throws NullPointerException, IOException {
		if (in == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else {
			final BufferedReader	brdr = new BufferedReader(in);
			String	line;
			
			while((line = brdr.readLine()) != null) {
				if (!Utils.checkEmptyOrNullString(line)) {
					append(line);
				}
			}
		}
	}
	
	/**
	 * <p>Stores command history to external</p>
	 * @param out writer to store command history to. Can't be null. Every command will be terminated by line separator 
	 * @throws NullPointerException writer is null
	 * @throws IOException on any I/O error
	 */
	public void store(final Writer out) throws NullPointerException, IOException {
		if (out == null) {
			throw new NullPointerException("Writer can't be null");
		}
		else {
			for(String item : commands) {
				out.write(item);
				out.write(System.lineSeparator());
			}
			out.flush();
		}
	}
	
	/**
	 * <p>Can undo to previous command</p>
	 * @return true if can undo, false otherwise.
	 */
	public boolean canUndo() {
		return cursor <= commands.size() && cursor > 0;
	}
	
	/**
	 * <p>Undo to previous command</p>
	 * @return previous command text. Can be neither null nor empty
	 * @throws IllegalStateException if can't undo
	 */
	public String undo() throws IllegalStateException {
		if (!canUndo()) {
			throw new IllegalStateException("Undo can't be done because content is too few");
		}
		else {
			return commands.get(--cursor);
		}
	}

	/**
	 * <p>Can redo to next command</p>
	 * @return true if can redo, false otherwise.
	 */
	public boolean canRedo() {
		return cursor >= 0 && cursor < commands.size()-1;
	}
	
	/**
	 * <p>Redo to next command</p>
	 * @return next command text. Can be neither null nor empty
	 * @throws IllegalStateException if can't redo
	 */
	public String redo() throws IllegalStateException {
		if (!canRedo()) {
			throw new IllegalStateException("Redo can't be done because content is too few");
		}
		else {
			return commands.get(++cursor);
		}
	}
	
	/**
	 * <p>Appends new command string to the history. If maxDepth > 0, truncates history to maximum size</p> 
	 * @param command command to append. Can be neither null nor empty
	 */
	public void append(final String command) throws IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(command)) {
			throw new IllegalArgumentException("Command to append can be neither null nor empty");
		}
		else {
			commands.add(command);
			if (maxDepth != 0) {
				while (commands.size() > maxDepth) {
					commands.remove(0);
				}
			}
			cursor = commands.size();
		}
	}
	
	/**
	 * <p>Gets content of the current command</p>
	 * @return content of the current command. Can be neither null nor empty
	 * @throws IllegalStateException if command history is empty
	 */
	public String getCurrentCommand() throws IllegalStateException {
		if (cursor >= commands.size()) {
			throw new IllegalStateException("Current command is not available because content is too few");
		}
		else {
			return commands.get(cursor);
		}
	}

	/**
	 * <p>This interface describes processing of the current command. It is similar to {@linkplain Consumer} interface but allow to throw exceptions in the processing method</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	@FunctionalInterface
	public static interface CommandProcessor {
		/**
		 * <p>Process command.</p>
		 * @param command command to process. Can be neither null nor empty.
		 * @throws Exception any exception thrown during command processing
		 */
		void accept(String command) throws Exception;
	}

	/**
	 * <p>Assigns command history support on the text field component. Depth of command history will be infinity</p>
	 * @param component component to assign support to. Can't be null
	 * @param processor command processor to process current command. Can't be null. Will be called on pressing "Enter" key inside the text component
	 * @return command history instance. Can't be null
	 * @throws NullPointerException when component or processor is null
	 * @throws IllegalArgumentException when max depth is less than 0
	 */
	public static CommandHistory of(final JTextField component, final CommandProcessor processor) throws NullPointerException, IllegalArgumentException {
		return of(component, 0, processor);
	}	
	
	/**
	 * <p>Assigns command history support on the text field component</p>
	 * @param component component to assign support to. Can't be null
	 * @param maxDepth maximum depths for command history. 0 means infinity. Can't be less than 0
	 * @param processor command processor to process current command. Can't be null. Will be called on pressing "Enter" key inside the text component
	 * @return command history instance. Can't be null
	 * @throws NullPointerException when component or processor is null
	 * @throws IllegalArgumentException when max depth is less than 0
	 */
	public static CommandHistory of(final JTextField component, final int maxDepth, final CommandProcessor processor) throws NullPointerException, IllegalArgumentException {
		if (component == null) {
			throw new NullPointerException("Component can't be null");
		}
		else if (maxDepth < 0) {
			throw new IllegalArgumentException("Max command depth ["+maxDepth+"] must be greater or equals than 0");
		}
		else if (processor == null) {
			throw new NullPointerException("Command processor can't be null");
		}
		else {
			final CommandHistory	history = new CommandHistory(maxDepth);
			
			component.addActionListener((e)->{
				final String	cmd = component.getText();
				
				if (!Utils.checkEmptyOrNullString(cmd)) {
					try{
						processor.accept(cmd);
						component.setText("");
						history.append(cmd);
					} catch (Exception exc) {
					}
				}
			});
			SwingUtils.assignActionKey(component, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), (e)->{
				if (history.canUndo()) {
					component.setText(history.undo());
				}
			}, SwingUtils.ACTION_UNDO);
			SwingUtils.assignActionKey(component, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), (e)->{
				if (history.canRedo()) {
					component.setText(history.redo());
				}
			}, SwingUtils.ACTION_REDO);
			return history;
		}
	}
}
