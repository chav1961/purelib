package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
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
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.interfaces.ItemAndSelection;
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
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
 * <p>This class can also be used to manipulate any sort of content, not only Swing visual editors. To use this class, you need create class instance with
 * the {@linkplain #JFileContentManipulator(FileSystemInterface, Localizer, InputStreamGetter, OutputStreamGetter)} constructor and mark 
 * it's modification state programmatically.</p>
 * <p>The class can support more than one file content simultaneously (for example, tabs in multitab editor). Every content is identified by it's unique
 * number. Use these methods:</p>
 * <ul>
 * <li> {@linkplain #appendNewFileSupport()} - to add new file content support and get it's unique number.
 * <li> {@linkplain #removeFileSupport(int)} - to remove file support with the given unique number from this class.
 * <li> {@linkplain #setCurrentFileSupport(int)} - to set current file support. All the methods similar to {@linkplain #getCurrentNameOfTheFile()} will use
 * selected file support for it's functionality
 * <li> {@linkplain #getFileSupportCount()} - get number of the files supported
 * </ul>
 * <p>At the initial state, this class has no any file support. If there is not required to support more than one file, simply call {@linkplain #appendNewFileSupport()}
 * method at once after create instance of this class.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @see JFileSelectionDialog
 * @see FileSystemInterface
 * @see Localizer
 * @since 0.0.3
 * @last.update 0.0.7
 */
public class JFileContentManipulator implements Closeable, LocaleChangeListener, LocalizerOwner {
	private static final String				UNSAVED_TITLE = "JFileContentManipulator.unsaved.title";
	private static final String				UNSAVED_BODY = "JFileContentManipulator.unsaved.body";
	private static final String				UNSAVED_CAPTION = "JFileContentManipulator.unsaved.caption";
	private static final String				UNSAVED_SELECT_ALL = "JFileContentManipulator.unsaved.selectAll";
	private static final String				UNSAVED_DESELECT_ALL = "JFileContentManipulator.unsaved.deselectAll";
	private static final String				PROGRESS_LOADING = "JFileContentManipulator.progress.loading";
	private static final String				PROGRESS_SAVING = "JFileContentManipulator.progress.saving";
	private static final String				LRU_MISSING_TITLE = "JFileContentManipulator.lru.missing.title";
	private static final String				LRU_MISSING = "JFileContentManipulator.lru.missing";	
	private static final int				LRU_LIMIT = 10;
	private static final AtomicInteger		AI = new AtomicInteger();

	private final LightWeightListenerList<FileContentChangeListener>	listeners = new LightWeightListenerList<>(FileContentChangeListener.class);
	private final String				name;
	private final FileSystemInterface	fsi;
	private final Localizer				localizer;
	private final InputStreamGetter		getterIn;
	private final OutputStreamGetter	getterOut;
	private final LRUPersistence		persistence;
	private final List<String>			lru;
	private final List<FileDesc>		files = new ArrayList<>();

	private int							filesIndex = 0;
	private JFrame						owner = null;
	private ProgressIndicator			pi = ProgressIndicator.DUMMY; 
	
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
		component.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent e) {setModificationFlag();}
			@Override public void insertUpdate(DocumentEvent e) {setModificationFlag();}
			@Override public void changedUpdate(DocumentEvent e) {setModificationFlag();}
		});
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
		component.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent e) {setModificationFlag();}
			@Override public void insertUpdate(DocumentEvent e) {setModificationFlag();}
			@Override public void changedUpdate(DocumentEvent e) {setModificationFlag();}
		});
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
	public JFileContentManipulator(final String name, final FileSystemInterface fsi, final Localizer localizer, final InputStreamGetter getterIn, final OutputStreamGetter getterOut, final LRUPersistence persistence) throws NullPointerException, IllegalArgumentException {
		this(name, fsi, localizer, getterIn, getterOut, persistence, new ArrayList<>());
	}	
	
	/**
	 * <p>Constructor of the class</p>
	 * @param name manipulator instance name. Can't be null or empty
	 * @param fsi file system to use as content. Can't be null
	 * @param localizer localizer to use. Can't be null
	 * @param getterIn callback to get content when save
	 * @param getterOut callback to put content when load
	 * @param persistence persistence interface to load/store persistence
	 * @param sharedLRU shared list for persistence. Used with multiple content manipulators
	 * @throws NullPointerException any of parameters are null
	 * @throws IllegalArgumentException manipulator name is null or empty
	 * @since 0.0.7
	 */
	public JFileContentManipulator(final String name, final FileSystemInterface fsi, final Localizer localizer, final InputStreamGetter getterIn, final OutputStreamGetter getterOut, final LRUPersistence persistence, final List<String> sharedLRU) throws NullPointerException, IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(name)) {
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
		else if (sharedLRU == null) {
			throw new NullPointerException("Shared LRU list can't be null");
		}
		else {
			this.name = name;
			this.fsi = fsi;
			this.localizer = localizer;
			this.getterIn = getterIn;
			this.getterOut = getterOut;
			this.persistence = persistence;
			this.lru = sharedLRU;
			try{persistence.loadLRU(name, lru);
			} catch (IOException e) {
				lru.clear();
			}
		}
	}	

	/**
	 * <p>Get owner for all dialog windows from the content manipulator</p>
	 * @return owner for all dialogs. Can be null
	 * @since 0.0.7
	 */
	public JFrame getOwner() {
		return owner;
	}
	
	/**
	 * <p>Set owner for all dialog windows from the content manipulator</p>
	 * @param owner owner for all dialogs. Can be null
	 */
	public void setOwner(final JFrame owner) {
		this.owner = owner;
	}
	
	/**
	 * <p>Set progress indicator to load/store large files</p>
	 * @param pi progress indicator to set. Can't be null. Use {@linkplain ProgressIndicator#DUMMY} field instead of null
	 */
	public void setProgressIndicator(final ProgressIndicator pi) {
		if (pi == null) {
			throw new NullPointerException("Progress indicator can't be null. Use ProgressIndicator.DUMMY field instead"); 
		}
		else {
			this.pi = pi;
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	/**
	 * <p>Commit unsaved changes. This method can be used to decide weather exit application or not</p> 
	 * @return false if 'cancel' option was selected, true otherwise
	 * @throws IOException on any I/O errors
	 * @since 0.0.7
	 */
	public boolean commit() throws IOException {
		int	count = 0;
		
		for(FileDesc item : files) {
			if (item.wasChanged) {
				count++;
			}
		}
		if (count == 1) {
			switch (new JLocalizedOptionPane(localizer).confirm(getOwner(), UNSAVED_BODY, UNSAVED_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION)) {
				case  JOptionPane.YES_OPTION :
					if (saveFile()) {
						
						clearModificationFlag();
					}
				case  JOptionPane.NO_OPTION : 
					return true;
				case  JOptionPane.CANCEL_OPTION : case  JOptionPane.CLOSED_OPTION :
					return false;
				default :
					throw new UnsupportedOperationException("Illegal option from JLocalizedOptionPane.confirm(...)");
			}
		}
		else if (count > 1) {
			final ItemAndSelection<FileDesc>[]		content = ItemAndSelection.of(files);
			final JList<ItemAndSelection<FileDesc>>	list = new JList<>(content);
			final JLabel	caption = new JLabel(localizer.getValue(UNSAVED_CAPTION));
			final JButton	selectAll = new JButton(localizer.getValue(UNSAVED_SELECT_ALL));
			final JButton	deselectAll = new JButton(localizer.getValue(UNSAVED_DESELECT_ALL));
			final JPanel	rightPanel = new JPanel();
			final JPanel	panel = new JPanel(new BorderLayout(5,5));

			selectAll.addActionListener((e)->{
				for (ItemAndSelection<FileDesc> item : content) {
					item.setSelected(true);
				}
				list.setModel(list.getModel());
			});
			deselectAll.addActionListener((e)->{
				for (ItemAndSelection<FileDesc> item : content) {
					item.setSelected(false);
				}
				list.setModel(list.getModel());
			});
			
			rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
			rightPanel.add(selectAll);
			rightPanel.add(deselectAll);
			
			panel.add(caption, BorderLayout.NORTH);
			panel.add(new JScrollPane(list), BorderLayout.CENTER);
			panel.add(rightPanel, BorderLayout.EAST);
			
			list.setCellRenderer(SwingUtils.getCellRenderer(ItemAndSelection.class, new FieldFormat(ItemAndSelection.class), ListCellRenderer.class));

			switch (new JLocalizedOptionPane(localizer).confirm(getOwner(), panel, UNSAVED_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION)) {
				case  JOptionPane.YES_OPTION :
					int	lastSelected = getCurrentFileSupport();
					
					for (ItemAndSelection<FileDesc> item : content) {
						if (item.isSelected()) {
							setCurrentFileSupport(item.getItem().id);
							if (saveFile()) {
								clearModificationFlag();
							}
						}
					}
					setCurrentFileSupport(lastSelected);
				case  JOptionPane.NO_OPTION : 
					return true;
				case  JOptionPane.CANCEL_OPTION : case  JOptionPane.CLOSED_OPTION :
					return false;
				default :
					throw new UnsupportedOperationException("Illegal option from JLocalizedOptionPane.confirm(...)");
			}
		}
		else {
			return true;
		}
	}
	
	@Override
	public void close() throws IOException, UnsupportedOperationException {
		commit();
		persistence.saveLRU(name, getLastUsed());
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
			if (getFileDesc().wasChanged) {
				switch (new JLocalizedOptionPane(localizer).confirm(getOwner(), UNSAVED_BODY, UNSAVED_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION)) {
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
			}
			if (processNew(progress)) {
				clearModificationFlag();
				getFileDesc().currentName = "";
				fireEvent(FileContentChangeType.NEW_FILE_CREATED);
				return true;
			}
			else {
				return false;
			}
		}
	}

	/**
	 * <p>Process 'File'--&gt;'Open' action</p>
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 */
	public boolean openFile() throws IOException {
		return openFile(pi);
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
			if (getFileDesc().wasChanged) {
				switch (new JLocalizedOptionPane(localizer).confirm(getOwner(), UNSAVED_BODY, UNSAVED_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION)) {
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
			}
			for (String item : JFileSelectionDialog.select(getOwner(), localizer, getFileDesc().currentDir.isEmpty() ? fsi : fsi.open(getFileDesc().currentDir)
						, JFileSelectionDialog.OPTIONS_FOR_OPEN | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS | JFileSelectionDialog.OPTIONS_APPEND_EXTENSION
						, getFileDesc().filters)) {
				try(final FileSystemInterface	current = fsi.clone().open(item)) {
					
					progress.start(String.format(localizer.getValue(PROGRESS_LOADING),current.getName()), current.size());
					try(final InputStream			is = current.read()) {
						if (processLoad(item, is, progress)) {
							clearModificationFlag();
							getFileDesc().currentName = item;
							getFileDesc().currentDir = current.open("../").getPath();
							fillLru(getFileDesc().currentName,false);
							fireEvent(FileContentChangeType.FILE_LOADED);
							fireEvent(FileContentChangeType.LRU_LIST_REFRESHED);
							return true;
						}
						else {
							return false;
						}
					}
				} finally {
					progress.end();
				}
			}
			return false;
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
		return openFile(file, pi);
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
							if (processLoad(file, is, progress)) {
								clearModificationFlag();
								getFileDesc().currentName = current.getPath();
								getFileDesc().currentDir = current.open("../").getPath();
								fillLru(getFileDesc().currentName,false);
								fireEvent(FileContentChangeType.FILE_LOADED);
								fireEvent(FileContentChangeType.LRU_LIST_REFRESHED);
								return true;
							}
							else {
								return false;
							}
						}
					} finally {
						progress.end();
					}
				}
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
		return openLRUFile(file, pi);
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
		else if (!getLastUsed().contains(file)) {
			throw new IllegalArgumentException("File to open ["+file+"] not found in the 'last used' list. Use getLastUsed() to get valid names"); 
		}
		else if (progress == null) {
			throw new NullPointerException("Progress indicator can't be null");
		}
		else {
			try(final FileSystemInterface	current = fsi.clone().open(file)) {
				if (!current.exists() || !current.isFile()) {
					if (new JLocalizedOptionPane(localizer).confirm(getOwner(), LRU_MISSING, LRU_MISSING_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						getLastUsed().remove(file);
						fireEvent(FileContentChangeType.LRU_LIST_REFRESHED);
					}
					return false;
				}
				else {
					try{progress.start(String.format(localizer.getValue(PROGRESS_LOADING),current.getName()), current.size());
						try(final InputStream			is = current.read()) {
							if (processLoad(file, is, progress)) {
								clearModificationFlag();
								getFileDesc().currentName = current.getPath();
								getFileDesc().currentDir = current.open("../").getPath();
								fillLru(getFileDesc().currentName,false);
								fireEvent(FileContentChangeType.FILE_LOADED);
								fireEvent(FileContentChangeType.LRU_LIST_REFRESHED);
								return true;
							}
							else {
								return false;
							}
						}
					} finally {
						progress.end();
					}
				}
			}
		}
	}
	
	/**
	 * <p>Process 'File'--&gt;'Save' action</p>
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 */
	public boolean saveFile() throws IOException {
		return saveFile(pi);
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
			if (isFileNew()) {
				return saveFileAs(progress);
			}
			else {
				try(final FileSystemInterface	current = fsi.clone().open(getFileDesc().currentName)) {
					
					if (current.exists()) {
						progress.start(String.format(localizer.getValue(PROGRESS_SAVING),current.getName()), current.size());
					}
					else {
						current.create();
						progress.start(String.format(localizer.getValue(PROGRESS_SAVING),current.getName()));
					}
					try(final OutputStream			os = current.write()) {
						if (processStore(getFileDesc().currentName, os, progress)) {
							clearModificationFlag();
							fillLru(getFileDesc().currentName, true);
							fireEvent(FileContentChangeType.FILE_STORED);
							fireEvent(FileContentChangeType.LRU_LIST_REFRESHED);
							return true;
						}
						else {
							return false;
						}
					}
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
		return saveFileAs(pi);
	}

	/**
	 * <p>Process 'File'--&gt;'Save as...' action</p>
	 * @param progress progress indicator to show progress
	 * @return true if processing was successful
	 * @throws IOException on any I/O errors
	 */
	public boolean saveFileAs(final ProgressIndicator progress) throws IOException {
		for (String item : JFileSelectionDialog.select(getOwner(), localizer, getFileDesc().currentDir.isEmpty() ? fsi : fsi.open(getFileDesc().currentDir), 
					JFileSelectionDialog.OPTIONS_FOR_SAVE | JFileSelectionDialog.OPTIONS_ALLOW_MKDIR | JFileSelectionDialog.OPTIONS_ALLOW_DELETE | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE| JFileSelectionDialog.OPTIONS_APPEND_EXTENSION
					, getFileDesc().filters)) {
			try(final FileSystemInterface	current = fsi.clone().open(item)) {
			
				if (current.exists()) {
					progress.start(String.format(localizer.getValue(PROGRESS_SAVING),current.getName()), current.size());
				}
				else {
					current.create();
					progress.start(String.format(localizer.getValue(PROGRESS_SAVING),current.getName()));
				}
				try(final OutputStream			os = current.write()) {
					if (processStore(item, os, progress)) {
						clearModificationFlag();
						getFileDesc().currentName = item;
						getFileDesc().currentDir = current.open("../").getPath();
						fillLru(getFileDesc().currentName,true);
						fireEvent(FileContentChangeType.FILE_STORED_AS);
						fireEvent(FileContentChangeType.LRU_LIST_REFRESHED);
						return true;
					}
					else {
						return false;
					}
				}

			} finally {
				progress.end();
			}
		}
		return false;
	}

	/**
	 * <p>Get name of the file currently 'loaded'</p>
	 * @return file name. Returns empty string for new content was not saved yet
	 */
	public String getCurrentNameOfTheFile() {
		if (getFileDesc().currentName.isEmpty() || "/".equals(getFileDesc().currentName)) {
			return "";
		}
		else {
			return getFileDesc().currentName.substring(getFileDesc().currentName.lastIndexOf('/')+1);
		}
	}

	/**
	 * <p>Get Path of the file currently 'loaded'</p>
	 * @return file path. Returns empty string for new content was not saved yet
	 */
	public String getCurrentPathOfTheFile() {
		return getFileDesc().currentName;
	}

	/**
	 * <p>Is current file new?</p>
	 * @return if current filer is new (doesn't have any name yet)
	 * @since 0.0.7
	 */
	public boolean isFileNew() {
		return getFileDesc().currentName.isEmpty();	
	}
	
	
	/**
	 * <p>Notify the class that loaded content was modified. The class is used this information to confirm saving</p>
	 */
	public void setModificationFlag() {
		getFileDesc().wasChanged = true;
		fireEvent(FileContentChangeType.MODIFICATION_FLAG_SET);
	}

	/**
	 * <p>Notify the class that loaded content is treated as not modified. The class is used this information to confirm saving</p>
	 */
	public void clearModificationFlag() {
		getFileDesc().wasChanged = false;
		fireEvent(FileContentChangeType.MODIFICATION_FLAG_CLEAR);
	}
	
	/**
	 * <p>Test the file was changed</p>
	 * @return true if yes
	 */
	public boolean wasChanged() {
		if (getFileSupportCount() == 0) {
			return false;
		}
		else {
			return getFileDesc().wasChanged; 
		}
	}

	/**
	 * <p>Get names of last used files</p>
	 * @return list of names. Can be empty but not null.
	 */
	public List<String> getLastUsed() {
		return lru;
	}

	/**
	 * <p>Remove file name from last recently list</p>
	 * @param fileName file name to remove. Can't be null or empty
	 * @throws IllegalArgumentException file name is null or empty
	 * @since 0.0.7
	 */
	public void removeFileNameFromLRU(final String fileName) throws IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(fileName)) {
			throw new IllegalArgumentException("File name to remove can't be null or empty");
		}
		else {
			getLastUsed().remove(fileName);
			fireEvent(FileContentChangeType.LRU_LIST_REFRESHED);
		}			
	}
	
	/**
	 * <p>Add listener for change content events</p>
	 * @param l listener to add
	 * @throws NullPointerException when listener to add is null
	 * @since 0.0.4
	 */
	public void addFileContentChangeListener(final FileContentChangeListener<?> l) throws NullPointerException {
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
	public void removeFileContentChangeListener(final FileContentChangeListener<?> l) throws NullPointerException {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null"); 
		}
		else {
			listeners.removeListener(l);
		}
	}

	/**
	 * <p>Set filters for open/save operations.</p>
	 * @param filters filters to set. Can be empty but not null
	 * @since 0.0.6
	 */
	public void setFilters(final FilterCallback... filters) {
		if (filters == null || Utils.checkArrayContent4Nulls(filters) >= 0) {
			throw new IllegalArgumentException("File filters are null or contains nulls inside");
		}
		else {
			getFileDesc().filters = filters.clone();
		}
	}
	
	/**
	 * <p>Get current filters for open/save operations</p>
	 * @return current filters. Can be empty but not null
	 * @since 0.0.6
	 */
	public FilterCallback[] getFilters() {
		return getFileDesc().filters;
	}
	
	public int getFileSupportCount() {
		return files.size();
	}

	public int appendNewFileSupport() {
		final FileDesc	fd = new FileDesc(); 
		
		files.add(fd);		
		return fd.id;
	}
	
	public int getCurrentFileSupport() {
		return files.get(filesIndex).id;
	}
	
	public void setCurrentFileSupport(final int id) {
		if (id == -1) {
			filesIndex = -1;
		}
		else {
			for(int index = 0; index < files.size(); index++) {
				if (files.get(index).id == id) {
					filesIndex = index;
					fireEvent(FileContentChangeType.FILE_SUPPORT_ID_CHANGED);
					return;
				}
			}
			throw new IllegalArgumentException("File support id ["+id+"] not found in the files list");
		}
	}
	
	public void removeFileSupport(final int id) {
		for(int index = files.size() - 1; index >= 0; index--) {
			if (files.get(index).id == id) {
				if (index == filesIndex && files.size() > 1) {
					throw new IllegalArgumentException("Attempt to remove current file support id ["+id+"]. Change selection firstly");
				}
				else {
					files.remove(index);
					return;
				}
			}
		}
		throw new IllegalArgumentException("File support id ["+id+"] not found in the files list");
	}
	
	protected boolean processNew(final ProgressIndicator progress) throws IOException {
		try(final OutputStream	os = getterOut.getOutputContent()) {
			os.flush();
		}
		return true;
	}
	
	protected boolean processLoad(final String fileName, final InputStream source, final ProgressIndicator progress) throws IOException {
		try(final OutputStream			os = getterOut.getOutputContent()) {

			Utils.copyStream(source, os, progress);
		}
		return true;
	}

	protected boolean processStore(final String fileName, final OutputStream target, final ProgressIndicator progress) throws IOException {
		try(final InputStream			is = getterIn.getInputContent()) {

			Utils.copyStream(is, target, progress);
		}
		return true;
	}

	protected FileDesc getFileDesc() {
		if (filesIndex == -1) {
			throw new IllegalStateException("No file support id was selected eariler"); 
		}
		else if (files.isEmpty()) {
			throw new IllegalStateException("No file support was created earlier. Use JFileContentManipulator.appendNewFileSupport() to create at least one file support"); 
		}
		else {
			return files.get(filesIndex);
		}
	}
	
	static InputStreamGetter buildInputStreamGetter(final JTextComponent component) {
		if (component == null) {
			throw new NullPointerException("Text component can't be null"); 
		}
		else {
			return new InputStreamGetter() {
				@Override
				public InputStream getInputContent() {
					try{return new ByteArrayInputStream(component.getText().getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING));
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
				public OutputStream getOutputContent() {
					return new ByteArrayOutputStream() {
						@Override
						public void close() throws IOException{
							super.close();
							component.setText(this.toString(PureLibSettings.DEFAULT_CONTENT_ENCODING));
						}
					};
				}
			};
		}
	}
	
	private void fillLru(final String name, final boolean fromSave) {
		if (getLastUsed().size() > 0) {	// Avoid filling by repeatable values
			if (getLastUsed().get(0).equals(name) && fromSave) {
				return;
			}
		}
		getLastUsed().remove(name);
		getLastUsed().add(0,name);
		if (getLastUsed().size() > LRU_LIMIT) {
			getLastUsed().remove(getLastUsed().size()-1);
		}
	}
	
	private void fireEvent(final FileContentChangeType type) {
		final FileContentChangedEvent<JFileContentManipulator>	event = new FileContentChangedEvent<>() {
											@Override public FileContentChangeType getChangeType() {return type;}
											@Override public JFileContentManipulator getOwner() {return JFileContentManipulator.this;}
											@Override public int getFileSupportId() {return getCurrentFileSupport();}
										}; 
		listeners.fireEvent((l)->l.actionPerformed(event));
	}
	
	private static class FileDesc {
		private final int			id = AI.incrementAndGet();
		private boolean				wasChanged = false;
		private String				currentName = "<new>", currentDir = "";
		private FilterCallback[]	filters = new FilterCallback[0];
		
		@Override
		public String toString() {
			return currentName;
		}
	}
}
