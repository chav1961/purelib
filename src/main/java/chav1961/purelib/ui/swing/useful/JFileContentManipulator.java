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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangeListener;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangeType;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangedEvent;

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
 * @lastUpdate 0.0.6
 */
public class JFileContentManipulator implements Closeable, LocaleChangeListener {
	private static final String				UNSAVED_TITLE = "JFileContentManipulator.unsaved.title";
	private static final String				UNSAVED_BODY = "JFileContentManipulator.unsaved.body";
	private static final String				PROGRESS_LOADING = "JFileContentManipulator.progress.loading";
	private static final String				PROGRESS_SAVING = "JFileContentManipulator.progress.saving";
	private static final String				LRU_MISSING_TITLE = "JFileContentManipulator.lru.missing.title";
	private static final String				LRU_MISSING = "JFileContentManipulator.lru.missing";	
	private static final int				LRU_LIMIT = 10;

	private final LightWeightListenerList<FileContentChangeListener>	listeners = new LightWeightListenerList<>(FileContentChangeListener.class);
	private final String				name;
	private final FileSystemInterface	fsi;
	private final Localizer				localizer;
	private final InputStreamGetter		getterIn;
	private final OutputStreamGetter	getterOut;
	private final LRUPersistence		persistence;
	private final List<String>			lru = new ArrayList<>();
	
	private boolean		wasChanged = false;
	private String		currentName = "", currentDir = "";
	
	/**
	 * <p>Constructor of the class</p>
	 * @param fsi file system to use as content. Can't be null
	 * @param localizer localizer to use. Can't be null
	 * @param component any text component on the Swing form. Can't be null
	 * @throws NullPointerException any of parameters are null
	 * @throws IllegalArgumentException text component to associate is invalid
	 */
	public JFileContentManipulator(final FileSystemInterface fsi, final Localizer localizer, final JTextComponent component) throws NullPointerException, IllegalArgumentException {
		this(fsi,localizer,buildInputStreamGetter(component),buildOutputStreamGetter(component),LRUPersistence.DUMMY);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param fsi file system to use as content. Can't be null
	 * @param localizer localizer to use. Can't be null
	 * @param getterIn callback to get content when save
	 * @param getterOut callback to put content when load
	 * @throws NullPointerException any of parameters are null
	 */
	public JFileContentManipulator(final FileSystemInterface fsi, final Localizer localizer, final InputStreamGetter getterIn, final OutputStreamGetter getterOut) throws NullPointerException {
		this(fsi,localizer,getterIn,getterOut,LRUPersistence.DUMMY);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param fsi file system to use as content. Can't be null
	 * @param localizer localizer to use. Can't be null
	 * @param component any text component on the Swing form. Can't be null
	 * @param persistence persistence interface to load/store persistence
	 * @throws NullPointerException any of parameters are null
	 * @throws IllegalArgumentException text component to associate is invalid
	 */
	public JFileContentManipulator(final FileSystemInterface fsi, final Localizer localizer, final JTextComponent component, final LRUPersistence persistence) throws NullPointerException, IllegalArgumentException {
		this(fsi,localizer,buildInputStreamGetter(component),buildOutputStreamGetter(component),persistence);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param fsi file system to use as content. Can't be null
	 * @param localizer localizer to use. Can't be null
	 * @param getterIn callback to get content when save
	 * @param getterOut callback to put content when load
	 * @param persistence persistence interface to load/store persistence
	 * @throws NullPointerException any of parameters are null
	 */
	public JFileContentManipulator(final FileSystemInterface fsi, final Localizer localizer, final InputStreamGetter getterIn, final OutputStreamGetter getterOut, final LRUPersistence persistence) throws NullPointerException {
		this("system", fsi, localizer, getterIn, getterOut, persistence);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param name manipulator instance name. Can't be null or empty
	 * @param fsi file system to use as content. Can't be null
	 * @param localizer localizer to use. Can't be null
	 * @param getterIn callback to get content when save
	 * @param getterOut callback to put content when load
	 * @param persistence persistence interface to load/store persistence
	 * @throws NullPointerException any of parameters are null
	 * @throws IllegalArgumentException manipulator name is null or empty
	 * @since 0.0.6
	 */
	public JFileContentManipulator(final String name, final FileSystemInterface fsi, final Localizer localizer, final InputStreamGetter getterIn, final OutputStreamGetter getterOut, final LRUPersistence persistence) throws NullPointerException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Manipulator name can't be null or empty");
		}
		else if (fsi == null) {
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
		else if (persistence == null) {
			throw new NullPointerException("Persistence interface can't be null");
		}
		else {
			this.name = name;
			this.fsi = fsi;
			this.localizer = localizer;
			this.getterIn = getterIn;
			this.getterOut = getterOut;
			this.persistence = persistence;
			try{persistence.loadLRU(name, lru);
			} catch (IOException e) {
				lru.clear();
			}
		}
	}	
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() throws IOException, UnsupportedOperationException {
		if (wasChanged) {
			try{switch (new JLocalizedOptionPane(localizer).confirm(null,UNSAVED_BODY, UNSAVED_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION)) {
					case  JOptionPane.YES_OPTION :
						if (saveFile()) {
							clearModificationFlag();
						}
						break;
					case  JOptionPane.NO_OPTION : 
						break;
					case  JOptionPane.CANCEL_OPTION :
						throw new UnsupportedOperationException("Close rejected");
				}
			} catch (LocalizationException e) {
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
		persistence.saveLRU(name,lru);
	}

	/**
	 * <p>Process 'File'--&gt;'New' action</p>
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 */
	public boolean newFile() throws IOException {
		return newFile(ProgressIndicator.DUMMY);
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
			processNew(progress);
			clearModificationFlag();
			currentName = "";
			fireEvent(FileContentChangeType.NEW_FILE_CREATED);
			return true;
		}
	}

	/**
	 * <p>Process 'File'--&gt;'Open' action</p>
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 */
	public boolean openFile() throws IOException {
		return openFile(ProgressIndicator.DUMMY);
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
			try{for (String item : JFileSelectionDialog.select((Dialog)null, localizer, currentDir.isEmpty() ? fsi : fsi.open(currentDir), JFileSelectionDialog.OPTIONS_FOR_OPEN | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS)) {
					try(final FileSystemInterface	current = fsi.clone().open(item)) {
						
						progress.start(String.format(localizer.getValue(PROGRESS_LOADING),current.getName()), current.size());
						try(final InputStream			is = current.read()) {
							processLoad(item, is, progress);
						}
						clearModificationFlag();
						currentName = item;
						currentDir = current.open("../").getPath();
						fireEvent(FileContentChangeType.FILE_LOADED);
						return true;
					} finally {
						fillLru(currentName,false);
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
	 * <p>Explicitly open file</p>
	 * @param file file to open
	 * @return true of file was successfully opened
	 * @throws IOException on any errors during open
	 * @throws IllegalArgumentException when file name os null or empty
	 */
	public boolean openFile(final String file) throws IOException, IllegalArgumentException {
		return openFile(file,ProgressIndicator.DUMMY);
	}

	/**
	 * <p>Explicitly open file</p>
	 * @param file file to open
	 * @param progress progress indicator to indicate loading
	 * @return true of file was successfully opened
	 * @throws IOException on any errors during open
	 * @throws NullPointerException when progress indicator is null
	 * @throws IllegalArgumentException when file name os null or empty
	 */
	public boolean openFile(final String file, final ProgressIndicator progress) throws IOException, NullPointerException, IllegalArgumentException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty"); 
		}
		else if (progress == null) {
			throw new NullPointerException("Progress indicator can't be null");
		}
		else {
			try(final FileSystemInterface	current = fsi.clone().open(file)) {
				if (!current.exists() || !current.isFile()) {
					return false;
				}
				else {
					try{progress.start(String.format(localizer.getValue(PROGRESS_LOADING),current.getName()), current.size());
						try(final InputStream			is = current.read()) {
							processLoad(file, is, progress);
						}
						clearModificationFlag();
						currentName = current.getPath();
						currentDir = current.open("../").getPath();
						fireEvent(FileContentChangeType.FILE_LOADED);
						return true;
					} finally {
						fillLru(currentName,false);
						progress.end();
					}
				}
			} catch (LocalizationException e) {
				throw new IOException(e);
			}
		}
	}
	
	
	/**
	 * <p>Open file from the LRU list</p>
	 * @param file file to open
	 * @return true of file was successfully opened
	 * @throws IOException on any errors during open
	 * @throws IllegalArgumentException when file name os null or empty
	 */
	public boolean openLRUFile(final String file) throws IOException, IllegalArgumentException {
		return openLRUFile(file,ProgressIndicator.DUMMY);
	}

	/**
	 * <p>Open file from the LRU list</p>
	 * @param file file to open
	 * @param progress progress indicator to indicate loading
	 * @return true of file was successfully opened
	 * @throws IOException on any errors during open
	 * @throws NullPointerException when progress indicator is null
	 * @throws IllegalArgumentException when file name os null or empty
	 */
	public boolean openLRUFile(final String file, final ProgressIndicator progress) throws IOException, NullPointerException, IllegalArgumentException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty"); 
		}
		else if (!lru.contains(file)) {
			throw new IllegalArgumentException("File to open ["+file+"] not found in the 'last used' list. Use getLastUsed() to get valid names"); 
		}
		else if (progress == null) {
			throw new NullPointerException("Progress indicator can't be null");
		}
		else {
			try(final FileSystemInterface	current = fsi.clone().open(file)) {
				if (!current.exists() || !current.isFile()) {
					if (new JLocalizedOptionPane(localizer).confirm(null, LRU_MISSING, LRU_MISSING_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						lru.remove(file);
						fireEvent(FileContentChangeType.LRU_LIST_REFRESHED);
					}
					return false;
				}
				else {
					try{progress.start(String.format(localizer.getValue(PROGRESS_LOADING),current.getName()), current.size());
						try(final InputStream			is = current.read()) {
							processLoad(file, is, progress);
						}
						clearModificationFlag();
						currentName = current.getPath();
						currentDir = current.open("../").getPath();
						fireEvent(FileContentChangeType.FILE_LOADED);
						return true;
					} finally {
						fillLru(currentName,false);
						progress.end();
					}
				}
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
		return saveFile(ProgressIndicator.DUMMY);
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
						current.create();
						progress.start(String.format(localizer.getValue(PROGRESS_SAVING),current.getName()));
					}
					try(final OutputStream			os = current.write()) {
						processStore(currentName, os, progress);
					}
	
					clearModificationFlag();
					fireEvent(FileContentChangeType.FILE_STORED);
					return true;
				} catch (LocalizationException | IllegalArgumentException e) {
					throw new IOException(e);
				} finally {
					fillLru(currentName,true);
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
		return saveFileAs(ProgressIndicator.DUMMY);
	}

	/**
	 * <p>Process 'File'--&gt;'Save as...' action</p>
	 * @param progress progress indicator to show progress
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 */
	public boolean saveFileAs(final ProgressIndicator progress) throws IOException {
		try{for (String item : JFileSelectionDialog.select((Dialog)null, localizer, currentDir.isEmpty() ? fsi : fsi.open(currentDir), JFileSelectionDialog.OPTIONS_FOR_SAVE | JFileSelectionDialog.OPTIONS_ALLOW_MKDIR | JFileSelectionDialog.OPTIONS_ALLOW_DELETE | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE)) {
				try(final FileSystemInterface	current = fsi.clone().open(item)) {
				
					if (current.exists()) {
						progress.start(String.format(localizer.getValue(PROGRESS_SAVING),current.getName()), current.size());
					}
					else {
						current.create();
						progress.start(String.format(localizer.getValue(PROGRESS_SAVING),current.getName()));
					}
					try(final OutputStream			os = current.write()) {
						processStore(item, os, progress);
					}
	
					clearModificationFlag();
					currentName = item;
					currentDir = current.open("../").getPath();
					fireEvent(FileContentChangeType.FILE_STORED_AS);
					return true;
				} finally {
					fillLru(currentName,true);
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
		fireEvent(FileContentChangeType.MODIFICATION_FLAG_SET);
	}

	/**
	 * <p>Notify the class that loaded content is treated as not modified. The class is used this information to confirm saving</p>
	 */
	public void clearModificationFlag() {
		wasChanged = false;
		fireEvent(FileContentChangeType.MODIFICATION_FLAG_CLEAR);
	}
	
	/**
	 * <p>Test the file was changed</p>
	 * @return true if yes
	 */
	public boolean wasChanged() {
		return wasChanged; 
	}

	/**
	 * <p>Get names of last used files</p>
	 * @return list of names. Can be empty but not null.
	 */
	public List<String> getLastUsed() {
		return lru;
	}

	/**
	 * <p>Add listener for change content events</p>
	 * @param l listener to add
	 * @throws NullPointerException when listener to add is null
	 * @since 0.0.4
	 */
	public void addFileContentChangeListener(final FileContentChangeListener l) throws NullPointerException {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null"); 
		}
		else {
			listeners.addListener(l);
		}
	}
	
	/**
	 * <p>Remove listener for change content events</p>
	 * @param l listener to remove
	 * @throws NullPointerException when listener to remove is null
	 * @since 0.0.4
	 */
	public void removeFileContentChangeListener(final FileContentChangeListener l) throws NullPointerException {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null"); 
		}
		else {
			listeners.removeListener(l);
		}
	}

	protected void processNew(final ProgressIndicator progress) throws IOException {
		try(final OutputStream	os = getterOut.getContent()) {
			os.flush();
		}
	}
	
	protected void processLoad(final String fileName, final InputStream source, final ProgressIndicator progress) throws IOException {
		try(final OutputStream			os = getterOut.getContent()) {

			Utils.copyStream(source, os, progress);
		}
	}

	protected void processStore(final String fileName, final OutputStream target, final ProgressIndicator progress) throws IOException {
		try(final InputStream			is = getterIn.getContent()) {

			Utils.copyStream(is, target, progress);
		}
	}
	
	static InputStreamGetter buildInputStreamGetter(final JTextComponent component) {
		if (component == null) {
			throw new NullPointerException("Text component can't be null"); 
		}
		else {
			return new InputStreamGetter() {
				@Override
				public InputStream getContent() {
					try{return new ByteArrayInputStream(component.getText().getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						return new ByteArrayInputStream(component.getText().getBytes());					
					}
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
							component.setText(this.toString("UTF-8"));
						}
					};
				}
			};
		}
	}
	
	private void fillLru(final String name, final boolean fromSave) {
		if (lru.size() > 0) {	// Avoid filling by repeatable values
			if (lru.get(0).equals(name) && fromSave) {
				return;
			}
		}
		lru.remove(currentName);
		lru.add(0,currentName);
		if (lru.size() > LRU_LIMIT) {
			lru.remove(lru.size()-1);
		}
	}
	
	private void fireEvent(final FileContentChangeType type) {
		final FileContentChangedEvent	event = new FileContentChangedEvent() {
											@Override public FileContentChangeType getChangeType() {return type;}
											@Override public JFileContentManipulator getOwner() {return JFileContentManipulator.this;}
										}; 
		listeners.fireEvent((l)->l.actionPerformed(event));
	}
}
