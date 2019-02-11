package chav1961.purelib.ui.swing.useful;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

/**
 * <p>This class is used to support opening/editing/saving any content in the Swing applications. It implements very popular
 * ability to create new or open existing file and to save it after modification. To use this class, you need:</p>
 * <ul>
 * <li>create this class instance and associate it with any existent {@linkplain JTextComponent} on the Swing form</li>
 * <li>assign {@linkplain ActionListener} to the application menu items to call {@linkplain #newFile()}, {@linkplain #openFile()},
 * {@linkplain #saveFile()} and {@linkplain #saveFileAs()} methods</li>   
 * <li>associate {@linkplain WindowListener} with the application menu items and/or application window to call {@linkplain #close()} method</li>
 * <li>assign {@linkplain DocumentListener} to your {@linkplain JTextComponent} to call {@linkplain #setModificationFlag()} method</li>  
 * <li>enjoy!</li>  
 * </ul>
 * <p>This class can also be used to manipulate any sort of content, not only visual editors. To use this class, you need create class instance with
 * the {@linkplain #JFileContentManipulator(FileSystemInterface, Localizer, InputStreamGetter, OutputStreamGetter)} constructor and mark 
 * it's modification state programmatically.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @see JFileSelectionDialog
 * @see FileSystemInterface
 * @see Localizer
 * @since 0.0.3
 */
public class JFileContentManipulator implements Closeable, LocaleChangeListener {
	private static final String				UNSAVED_TITLE = "JFileContentManipulator.unsaved.title";
	private static final String				UNSAVED_BODY = "JFileContentManipulator.unsaved.body";
	private static final String				PROGRESS_LOADING = "JFileContentManipulator.progress.loading";
	private static final String				PROGRESS_SAVING = "JFileContentManipulator.progress.saving";
	private static final ProgressIndicator	DUMMY = new ProgressIndicator() {
												@Override public void start(String caption) {}
												@Override public void start(String caption, long total) {}
												@Override public boolean processed(long processed) {return true;}
												@Override public void end() {}
											};

	/**
	 * <p>This interface produces input stream to get content of the manipulated entity on saving</p>  
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface InputStreamGetter {
		/**
		 * <p>Get entity content to save</p>
		 * @return content stream. Can't be null
		 */
		InputStream getContent();
	}

	/**
	 * <p>This interface produces output stream to set content of the manipulated entity on loading</p>  
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface OutputStreamGetter {
		/**
		 * <p>Get entity content to load</p>
		 * @return content stream. Can't be null
		 */
		OutputStream getContent();
	}

	private final FileSystemInterface	fsi;
	private final Localizer				localizer;
	private final InputStreamGetter		getterIn;
	private final OutputStreamGetter	getterOut;
	private boolean		wasChanged = false;
	private String		currentName = "";
	
	/**
	 * <p>Constructor of the class</p>
	 * @param fsi file system to use as content. Can't be null
	 * @param localizer localizer to use. Can't be null
	 * @param component any text component on the Swing form. Can't be null
	 * @throws NullPointerException any of parameters are null
	 * @throws IllegalArgumentException text component to associate is invalid
	 */
	public JFileContentManipulator(final FileSystemInterface fsi, final Localizer localizer, final JTextComponent component) throws NullPointerException, IllegalArgumentException {
		this(fsi,localizer,buildInputStreamGetter(component),buildOutputStreamGetter(component));
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param fsi file system to use as content. Can't be null
	 * @param localizer localizer to use. Can't be null
	 * @param getterIn callback to get content when save
	 * @param getterOut callback to put content when load
	 * @throws NullPointerException any of parameters are null
	 * @throws NullPointerException 
	 */
	public JFileContentManipulator(final FileSystemInterface fsi, final Localizer localizer, final InputStreamGetter getterIn, final OutputStreamGetter getterOut) throws NullPointerException {
		if (fsi == null) {
			throw new NullPointerException("File system interface can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null or empty");
		}
		else if (getterIn == null) {
			throw new NullPointerException("Input stream getter can't be null");
		}
		else if (getterOut == null) {
			throw new NullPointerException("Output stream getter can't be null");
		}
		else {
			this.fsi = fsi;
			this.localizer = localizer;
			this.getterIn = getterIn;
			this.getterOut = getterOut;
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() throws IOException {
		if (wasChanged) {
			try{switch (new JLocalizedOptionPane(localizer).confirm(null,UNSAVED_BODY, UNSAVED_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION)) {
					case  JOptionPane.YES_OPTION :
						if (saveFile()) {
							clearModificationFlag();
						}
						break;
					case  JOptionPane.NO_OPTION : case  JOptionPane.CANCEL_OPTION :
						break;
				}
			} catch (LocalizationException e) {
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
	}

	/**
	 * <p>Process 'File'--&gt;'New' action</p>
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 */
	public boolean newFile() throws IOException {
		return newFile(DUMMY);
	}
	
	/**
	 * <p>Process 'File'--&gt;'New' action</p>
	 * @param progress progress indicator to show progress
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException if progress indicator is null
	 */
	public boolean newFile(final ProgressIndicator progress) throws IOException, NullPointerException {
		if (progress == null) {
			throw new NullPointerException("Progress indicator can't be null"); 
		}
		else {
			if (wasChanged) {
				try{switch (new JLocalizedOptionPane(localizer).confirm(null,UNSAVED_BODY, UNSAVED_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION)) {
						case  JOptionPane.YES_OPTION :
							if (!saveFile(progress)) {
								return false;
							}
							break;
						case  JOptionPane.NO_OPTION : 
							break;
						case  JOptionPane.CANCEL_OPTION :
							return false;
					}
				} catch (LocalizationException e) {
					throw new IOException(e.getLocalizedMessage(),e);
				}
			}
			try(final OutputStream	os = getterOut.getContent()) {
				os.flush();
				clearModificationFlag();
				return true;
			}
		}
	}

	/**
	 * <p>Process 'File'--&gt;'Open' action</p>
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 */
	public boolean openFile() throws IOException {
		return openFile(DUMMY);
	}

	/**
	 * <p>Process 'File'--&gt;'Open' action</p>
	 * @param progress progress indicator to show progress
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException if progress indicator is null
	 */
	public boolean openFile(final ProgressIndicator progress) throws IOException, NullPointerException {
		if (progress == null) {
			throw new NullPointerException("Progress indicator can't be null"); 
		}
		else {
			if (wasChanged) {
				try{switch (new JLocalizedOptionPane(localizer).confirm(null,UNSAVED_BODY, UNSAVED_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION)) {
						case  JOptionPane.YES_OPTION :
							if (!saveFile(progress)) {
								return false;
							}
							break;
						case  JOptionPane.NO_OPTION : 
							break;
						case  JOptionPane.CANCEL_OPTION :
							return false;
					}
				} catch (LocalizationException e) {
					throw new IOException(e.getLocalizedMessage(),e);
				}
			}
			try{for (String item : JFileSelectionDialog.select((Dialog)null, localizer, fsi, JFileSelectionDialog.OPTIONS_FOR_OPEN | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS)) {
					try(final FileSystemInterface	current = fsi.clone().open(item)) {
						
						progress.start(String.format(localizer.getValue(PROGRESS_LOADING),current.getName()), current.size());
						try(final InputStream			is = current.read();
							final OutputStream			os = getterOut.getContent()) {
		
							Utils.copyStream(is, os, progress);
							clearModificationFlag();
							currentName = item;
							return true;
						}
					} finally {
						progress.end();
					}
				}
				return false;
			} catch (LocalizationException e) {
				throw new IOException(e);
			}
		}
	}

	/**
	 * <p>Process 'File'--&gt;'Save' action</p>
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 */
	public boolean saveFile() throws IOException {
		return saveFile(DUMMY);
	}

	/**
	 * <p>Process 'File'--&gt;'Save' action</p>
	 * @param progress progress indicator to show progress
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException if progress indicator is null
	 */
	public boolean saveFile(final ProgressIndicator progress) throws IOException, NullPointerException {
		if (progress == null) {
			throw new NullPointerException("Progress indicator can't be null"); 
		}
		else {
			if (currentName.isEmpty()) {
				return saveFileAs(progress);
			}
			else {
				try(final FileSystemInterface	current = fsi.clone().open(currentName)) {
					
					if (current.exists()) {
						progress.start(String.format(localizer.getValue(PROGRESS_SAVING),current.getName()), current.size());
					}
					else {
						progress.start(String.format(localizer.getValue(PROGRESS_SAVING),current.getName()));
					}
					try(final InputStream			is = getterIn.getContent();
						final OutputStream			os = current.write()) {
		
						Utils.copyStream(is, os, progress);
						clearModificationFlag();
						return true;
					}
				} catch (LocalizationException | IllegalArgumentException e) {
					throw new IOException(e);
				} finally {
					progress.end();
				}
			}
		}
	}

	/**
	 * <p>Process 'File'--&gt;'Save as...' action</p>
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 */
	public boolean saveFileAs() throws IOException {
		return saveFileAs(DUMMY);
	}

	/**
	 * <p>Process 'File'--&gt;'Save as...' action</p>
	 * @param progress progress indicator to show progress
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 */
	public boolean saveFileAs(final ProgressIndicator progress) throws IOException {
		try{for (String item : JFileSelectionDialog.select((Dialog)null, localizer, currentName.isEmpty() ? fsi : fsi.open(currentName), JFileSelectionDialog.OPTIONS_FOR_SAVE | JFileSelectionDialog.OPTIONS_ALLOW_MKDIR | JFileSelectionDialog.OPTIONS_ALLOW_DELETE | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE)) {
				try(final FileSystemInterface	current = fsi.clone().open(item)) {
				
					if (current.exists()) {
						progress.start(String.format(localizer.getValue(PROGRESS_SAVING),current.getName()), current.size());
					}
					else {
						current.create();
						progress.start(String.format(localizer.getValue(PROGRESS_SAVING),current.getName()));
					}
					try(final InputStream			is = getterIn.getContent();
						final OutputStream			os = current.write()) {
	
						Utils.copyStream(is, os, progress);
						clearModificationFlag();
						currentName = item;
						return true;
					}
				} finally {
					progress.end();
				}
			}
			return false;
		} catch (LocalizationException e) {
			throw new IOException(e);
		}
	}

	/**
	 * <p>Get name of the file currently 'loaded'</p>
	 * @return file name. Returns empty string for new content was not saved yet
	 */
	public String getCurrentNameOfTheFile() {
		if (currentName.isEmpty() || "/".equals(currentName)) {
			return "";
		}
		else {
			return currentName.substring(currentName.lastIndexOf('/')+1);
		}
	}

	/**
	 * <p>Get Path of the file currently 'loaded'</p>
	 * @return file path. Returns empty string for new content was not saved yet
	 */
	public String getCurrentPathOfTheFile() {
		return currentName;
	}
	
	/**
	 * <p>Notify the class that loaded content was modified. The class is used this information to confirm saving</p>
	 */
	public void setModificationFlag() {
		wasChanged = true;
	}

	/**
	 * <p>Notify the class that loaded content is treated as not modified. The class is used this information to confirm saving</p>
	 */
	public void clearModificationFlag() {
		wasChanged = false;
	}

	static InputStreamGetter buildInputStreamGetter(final JTextComponent component) {
		if (component == null) {
			throw new NullPointerException("Text component can't be null"); 
		}
		else {
			return new InputStreamGetter() {
				@Override
				public InputStream getContent() {
					return new ByteArrayInputStream(component.getText().getBytes());
				}
			};
		}
	}

	static OutputStreamGetter buildOutputStreamGetter(final JTextComponent component) {
		if (component == null) {
			throw new NullPointerException("Text component can't be null"); 
		}
		else {
			return new OutputStreamGetter() {
				@Override
				public OutputStream getContent() {
					return new ByteArrayOutputStream() {
						@Override
						public void close() throws IOException{
							super.close();
							component.setText(this.toString());
						}
					};
				}
			};
		}
	}
}
