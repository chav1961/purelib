package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.NullLoggerFacade;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

/**
 * <p>Standard file selection dialog (see {@linkplain JFileChooser}). Main differences from (see {@linkplain JFileChooser}) are:</p>
 * <ul>
 * <li>This class supports any {@linkplain FileSystemInterface} available, not only local disks</li>  
 * <li>This class is localized and will change all locale-specific information on locale changes</li>  
 * </ul>  
 * <p>Differ to {@linkplain JFileChooser}, this class is a child of the {@linkplain JPanel} and can be embedded into any Swing forms
 * as the part of them. To use it as ordinal dialog window, call it's static methods {@linkplain JFileSelectionDialog#select(Dialog, Localizer, FileSystemInterface, int, FilterCallback...)} and 
 * {@linkplain JFileSelectionDialog#select(Window, Localizer, FileSystemInterface, int, FilterCallback...)}.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @see JFileChooser
 * @see FileSystemInterface
 * @see Localizer
 * @since 0.0.3
 */
public class JFileSelectionDialog extends JPanel implements LocaleChangeListener {
	private static final long 	serialVersionUID = 4285629141818684880L;
	private static final int	ICON_BORDER_WIDTH = 2;
	private static final int	MIN_WIDTH = 400;
	private static final int	MIN_HEIGHT = 270;
	private static final String	ACCEPT_ALL_FILES = "JFileSelectionDialog.filter.acceptAll";
	private static final String	ACCEPT_OPEN = "JFileSelectionDialog.button.open";
	private static final String	ACCEPT_OPEN_TT = "JFileSelectionDialog.button.open.tt";
	private static final String	ACCEPT_SAVE = "JFileSelectionDialog.button.save";
	private static final String	ACCEPT_SAVE_TT = "JFileSelectionDialog.button.save.tt";
	private static final String	CAPTION_OPEN = "JFileSelectionDialog.caption.open";
	private static final String	CAPTION_SAVE = "JFileSelectionDialog.caption.save";
	private static final String	CANCEL = "JFileSelectionDialog.button.cancel";
	private static final String	CANCEL_TT = "JFileSelectionDialog.button.cancel.tt";
	private static final String	FILE_NAME = "JFileSelectionDialog.label.fileName";
	private static final String	FILE_TT = "JFileSelectionDialog.textfield.fileName.tt";
	private static final String	FILTER_LIST = "JFileSelectionDialog.label.filterList";
	private static final String	FILTER_LIST_TT = "JFileSelectionDialog.combobox.filterList.tt";
	private static final String	PARENT_TT = "JFileSelectionDialog.combobox.parent.tt";
	private static final String	LEVEL_UP_TT = "JFileSelectionDialog.button.levelUp.tt";
	private static final String	MK_DIR_TT = "JFileSelectionDialog.button.mkDir.tt";
	private static final String	DELETE_TT = "JFileSelectionDialog.button.delete.tt";
	private static final String	ASK_ENTER_DIR_CAPTION = "JFileSelectionDialog.ask.mkdir.caption";
	private static final String	ASK_ENTER_DIR_MESSAGE = "JFileSelectionDialog.ask.mkdir.message";
	private static final String	ASK_CONFIRM_DELETE_CAPTION = "JFileSelectionDialog.ask.delete.caption";
	private static final String	ASK_CONFIRM_DELETE_MESSAGE = "JFileSelectionDialog.ask.delete.message";
	private static final String	ALREADY_EXISTS_CAPTION = "JFileSelectionDialog.ask.alreadyexists.caption";
	private static final String	ALREADY_EXISTS_MESSAGE = "JFileSelectionDialog.ask.alreadyexists.message";
	private static final String	NOT_EXISTS_CAPTION = "JFileSelectionDialog.ask.notexists.caption";
	private static final String	NOT_EXISTS_MESSAGE = "JFileSelectionDialog.ask.notexists.message";
	private static final Icon	LEVEL_UP_ICON = new ImageIcon(JFileSelectionDialog.class.getResource("levelUp.png"));
	private static final Icon	MKDIR_ICON = new ImageIcon(JFileSelectionDialog.class.getResource("mkdir.png"));
	private static final Icon	DELETE_ICON = new ImageIcon(JFileSelectionDialog.class.getResource("delete.png"));
	private static final Icon	DIR_ICON = new ImageIcon(JFileSelectionDialog.class.getResource("directory.png"));
	private static final Icon	FILE_ICON = new ImageIcon(JFileSelectionDialog.class.getResource("file.png"));
	
	public static final int		OPTIONS_CAN_SELECT_DIR = 1 << 0; 
	public static final int		OPTIONS_CAN_SELECT_FILE = 1 << 1; 
	public static final int		OPTIONS_CAN_MULTIPLE_SELECT = 1 << 2; 
	public static final int		OPTIONS_FILE_MUST_EXISTS = 1 << 3; 
	public static final int		OPTIONS_ALLOW_MKDIR = 1 << 4; 
	public static final int		OPTIONS_ALLOW_DELETE = 1 << 5; 
	public static final int		OPTIONS_FOR_SAVE  = 1 << 6; 
	public static final int		OPTIONS_FOR_OPEN  = 1 << 7; 
	
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

	/**
	 * <p>This interface is analog of {@linkplain   
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	public interface FilterCallback {
		/**
		 * <p>Get filter masks
		 * @return filter masks. Can be empty, but not null
		 */
		String[] getFileMask();
		
		/**
		 * <p>Get filter name. String will be used 'as-is', you should make yourself any localization you need</p> 
		 * @return
		 */
		String getFilterName();
		
		/**
		 * <p>Accept file system interface item with the given filter.</p>
		 * @param item file system item to test
		 * @return true if the item must be include into the list, false otherwise
		 * @throws IOException
		 */
		boolean accept(final FileSystemInterface item) throws IOException;
	}
	
	private final Localizer			localizer;
	private final LoggerFacade		logger;
	private final JComboBox<String>	parent = new JComboBox<>();
	private final JButton			mkDir = new JButton(MKDIR_ICON);
	private final JButton			delete = new JButton(DELETE_ICON);
	private final JButton			levelUp = new JButton(LEVEL_UP_ICON);
	private final JList<String>		content = new JList<>();
	private final JLabel			fileNameLabel = new JLabel();
	private final JTextField		fileName = new JTextField();
	private final JLabel			filterLabel = new JLabel();
	private final JComboBox<FilterCallback>	filter = new JComboBox<>();
	private final JButton			accept = new JButton();
	private final JButton			cancel = new JButton();
	private final ActionListener	forParent = new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											try{final StringBuilder	sb = new StringBuilder();
													
												for (int index = 1, maxIndex = parent.getSelectedIndex(); index <= maxIndex; index++) {
													sb.append('/').append(parent.getModel().getElementAt(index).trim());
												}
												fillCurrentState(currentNode.open(sb.length() > 0 ? sb.toString() : "/"));
											} catch (IOException exc) {
												logger.message(Severity.error,exc,"Error changing file system location to parent: "+exc.getLocalizedMessage());
											}
										}
									};
									
	private boolean					forOpen = true, canSelectDir = false, canSelectFile = false, canUseDeletion = true;
	private FileSystemInterface 	currentNode = null;
	private AcceptAndCancelCallback callback;

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use with the class. Can't be null. It's strongly recommended to use {@linkplain PureLibSettings#PURELIB_LOCALIZER} 
	 * localizer to call the constructor</p>
	 * @throws LocalizationException in any localization errors
	 * @throws NullPointerException if any parameter is null
	 */
	public JFileSelectionDialog(final Localizer localizer) throws LocalizationException {
		this(localizer,new NullLoggerFacade());
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use with the class. Can't be null. It's strongly recommended to use {@linkplain PureLibSettings#PURELIB_LOCALIZER} 
	 * localizer to call the constructor</p>
	 * @param logger logger to print errors into. Can't be null. Toy can use this parameter to build total log with the rest of your application 
	 * @throws LocalizationException in any localization errors
	 * @throws NullPointerException if any parameter is null
	 */
	public JFileSelectionDialog(final Localizer localizer, final LoggerFacade logger) throws LocalizationException, NullPointerException {
		super(new BorderLayout());
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else {
			this.localizer = localizer;
			this.logger = logger;
			
			fileName.setColumns(30);
			
			final SpringLayout		topSpring = new SpringLayout();
			final JPanel			topPanel = new JPanel(topSpring);
			final JPanel			topRightPanel = new JPanel(new GridLayout(1,3,5,0));
			
			topRightPanel.add(levelUp);		levelUp.setPreferredSize(new Dimension(LEVEL_UP_ICON.getIconWidth()-ICON_BORDER_WIDTH,LEVEL_UP_ICON.getIconHeight()-ICON_BORDER_WIDTH));
			topRightPanel.add(mkDir);		mkDir.setPreferredSize(new Dimension(MKDIR_ICON.getIconWidth()-ICON_BORDER_WIDTH,MKDIR_ICON.getIconHeight()-ICON_BORDER_WIDTH));
			topRightPanel.add(delete);		delete.setPreferredSize(new Dimension(DELETE_ICON.getIconWidth()-ICON_BORDER_WIDTH,DELETE_ICON.getIconHeight()-ICON_BORDER_WIDTH));
			topPanel.add(parent);
			topPanel.add(topRightPanel);
			topSpring.putConstraint(SpringLayout.NORTH, parent, 5, SpringLayout.NORTH, topPanel);
			topSpring.putConstraint(SpringLayout.SOUTH, parent, -5, SpringLayout.SOUTH, topPanel);
			topSpring.putConstraint(SpringLayout.WEST, parent, 5, SpringLayout.WEST, topPanel);
			topSpring.putConstraint(SpringLayout.NORTH, topRightPanel, 5, SpringLayout.NORTH, topPanel);
			topSpring.putConstraint(SpringLayout.SOUTH, topRightPanel, -5, SpringLayout.SOUTH, topPanel);
			topSpring.putConstraint(SpringLayout.EAST, topRightPanel, -5, SpringLayout.EAST, topPanel);
			topSpring.putConstraint(SpringLayout.EAST, parent, -5, SpringLayout.WEST, topRightPanel);

			levelUp.addActionListener((e)->{
				try{fillCurrentState(currentNode.open("../"));
				} catch (IOException e1) {
					logger.message(Severity.error,e1,"Error changing file system location to parent: "+e1.getLocalizedMessage());
				}
			});
			mkDir.addActionListener((e)->{
				String	answer = null;
				
				try{if ((answer = JOptionPane.showInputDialog(this,localizer.getValue(ASK_ENTER_DIR_MESSAGE),localizer.getValue(ASK_ENTER_DIR_CAPTION),JOptionPane.QUESTION_MESSAGE)) != null) {
						try (final FileSystemInterface	fsi = currentNode.clone().open(answer)) {
							if (fsi.exists()) {
								JOptionPane.showMessageDialog(this, String.format(localizer.getValue(ALREADY_EXISTS_MESSAGE),answer), localizer.getValue(ALREADY_EXISTS_CAPTION),JOptionPane.QUESTION_MESSAGE);
							}
							else {
								fsi.mkDir();
							}
						}
						fillCurrentState(currentNode);
					}
				} catch (HeadlessException | LocalizationException | IllegalArgumentException | IOException e1) {
					logger.message(Severity.error,e1,"Error creating directory ["+answer+"] : "+e1.getLocalizedMessage());
				}
			});
			delete.addActionListener((e)->{
				final Set<String>	forDeletion = new HashSet<>();
				
				for (String item : content.getSelectedValuesList()) {
					forDeletion.add(item);
				}
				try{if (forDeletion.size() > 0 && JOptionPane.showConfirmDialog(this, String.format(localizer.getValue(ASK_CONFIRM_DELETE_MESSAGE),forDeletion),localizer.getValue(ASK_CONFIRM_DELETE_CAPTION),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
						for (String item : forDeletion) {
							try (final FileSystemInterface	fsi = currentNode.clone().open(item)) {
								fsi.deleteAll();
							}
						}
						fillCurrentState(currentNode);
					}
				} catch (HeadlessException | LocalizationException | IllegalArgumentException | IOException e1) {
					logger.message(Severity.error,e1,"Error deleting selected list : "+e1.getLocalizedMessage());
				}
			});
			
			topPanel.setMinimumSize(new Dimension(MIN_WIDTH,2*MIN_HEIGHT/16));
			topPanel.setPreferredSize(new Dimension(MIN_WIDTH,2*MIN_HEIGHT/16));
			
			final SpringLayout		bottomSpring = new SpringLayout();
			final JPanel			bottomPanel = new JPanel(bottomSpring);
			final JPanel			bottomNamesPanel = new JPanel(new GridLayout(2,1,0,5));
			final JPanel			bottomValuesPanel = new JPanel(new GridLayout(2,1,0,5));
			final JPanel			bottomButtonsPanel = new JPanel(new GridLayout(2,1,0,5));
			
			bottomNamesPanel.add(fileNameLabel);
			bottomNamesPanel.add(filterLabel);
			bottomValuesPanel.add(fileName);
			bottomValuesPanel.add(filter);
			bottomButtonsPanel.add(accept);
			bottomButtonsPanel.add(cancel);
			bottomPanel.add(bottomNamesPanel);
			bottomPanel.add(bottomValuesPanel);
			bottomPanel.add(bottomButtonsPanel);
			bottomSpring.putConstraint(SpringLayout.NORTH, bottomNamesPanel, 5, SpringLayout.NORTH, bottomPanel);
			bottomSpring.putConstraint(SpringLayout.SOUTH, bottomNamesPanel, -5, SpringLayout.SOUTH, bottomPanel);
			bottomSpring.putConstraint(SpringLayout.WEST, bottomNamesPanel, 5, SpringLayout.WEST, bottomPanel);
			bottomSpring.putConstraint(SpringLayout.NORTH, bottomButtonsPanel, 5, SpringLayout.NORTH, bottomPanel);
			bottomSpring.putConstraint(SpringLayout.SOUTH, bottomButtonsPanel, -5, SpringLayout.SOUTH, bottomPanel);
			bottomSpring.putConstraint(SpringLayout.EAST, bottomButtonsPanel, -5, SpringLayout.EAST, bottomPanel);
			bottomSpring.putConstraint(SpringLayout.NORTH, bottomValuesPanel, 5, SpringLayout.NORTH, bottomPanel);
			bottomSpring.putConstraint(SpringLayout.SOUTH, bottomValuesPanel, -5, SpringLayout.SOUTH, bottomPanel);
			bottomSpring.putConstraint(SpringLayout.WEST, bottomValuesPanel, 5, SpringLayout.EAST, bottomNamesPanel);
			bottomSpring.putConstraint(SpringLayout.EAST, bottomValuesPanel, -5, SpringLayout.WEST, bottomButtonsPanel);

			filter.setRenderer(new ListCellRenderer<FilterCallback>() {
				@Override
				public Component getListCellRendererComponent(JList<? extends FilterCallback> list, FilterCallback value, int index, boolean isSelected, boolean cellHasFocus) {
					final JLabel	result = new JLabel(value.getFilterName()+" "+Arrays.toString(value.getFileMask()));
					
					if (isSelected) {
						result.setOpaque(true);
						result.setForeground(list.getSelectionForeground());
						result.setBackground(list.getSelectionBackground());
					}
					if (cellHasFocus) {
						result.setBorder(new LineBorder(Color.BLACK));
					}
					return result;
				}
			});
			accept.addActionListener((e)->{
				callback.process(true);
			});
			cancel.addActionListener((e)->{
				callback.process(false);
			});
			
			bottomPanel.setMinimumSize(new Dimension(MIN_WIDTH,4*MIN_HEIGHT/16));
			bottomPanel.setPreferredSize(new Dimension(MIN_WIDTH,4*MIN_HEIGHT/16));
			
			final JScrollPane	scroll = new JScrollPane(content); 
			
			content.setModel(new DefaultListModel<String>());
			content.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			content.setVisibleRowCount(-1);
			content.setCellRenderer(new ListCellRenderer<String>() {
				@Override
				public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
					final JLabel	result = new JLabel(value);
					
					try(final FileSystemInterface	node = currentNode.clone().open(value);) {
						
						if (node.isDirectory()) {
							result.setIcon(DIR_ICON);
							if (isSelected && canSelectDir) {
								result.setOpaque(true);
								result.setForeground(list.getSelectionForeground());
								result.setBackground(list.getSelectionBackground());
							}
						}
						else {
							result.setIcon(FILE_ICON);
							if (isSelected && canSelectFile) {
								result.setOpaque(true);
								result.setForeground(list.getSelectionForeground());
								result.setBackground(list.getSelectionBackground());
							}
						}
						if (cellHasFocus) {
							result.setBorder(new LineBorder(Color.BLACK));
						}
					} catch (IOException e) {
						logger.message(Severity.error,e,"Error querying file system node ["+value+"]: "+e.getLocalizedMessage());
					}
					
					result.setPreferredSize(new Dimension(MIN_WIDTH/3,MIN_HEIGHT/16));
					result.setToolTipText(value);
					return result;
				}
			});
			content.addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				
				@Override 
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() > 1) {
						final int	index = content.locationToIndex(e.getPoint());
						
						if (index >= 0) {
							String	newLocation = null;
							
							try (final FileSystemInterface	fsi = currentNode.clone().open(content.getModel().getElementAt(index))) {
								
								if (fsi.isDirectory()) {
									newLocation = fsi.getName();
								}
							} catch (IOException e1) {
								logger.message(Severity.error,e1,"Error querying file system node ["+content.getModel().getElementAt(index)+"]: "+e1.getLocalizedMessage());
							}
							if (newLocation != null) {
								try{fillCurrentState(currentNode.open(newLocation));
								} catch (IOException e1) {
									logger.message(Severity.error,e1,"Error opening file system node ["+newLocation+"]: "+e1.getLocalizedMessage());
								}
							}
						}
					}
				}
			});
			content.addListSelectionListener((e)->{
				delete.setEnabled(canUseDeletion && !content.isSelectionEmpty());
			});
			scroll.setMinimumSize(new Dimension(MIN_WIDTH,10*MIN_HEIGHT/16));
			scroll.setPreferredSize(new Dimension(MIN_WIDTH,10*MIN_HEIGHT/16));
			add(topPanel,BorderLayout.NORTH);
			add(scroll,BorderLayout.CENTER);
			add(bottomPanel,BorderLayout.SOUTH);
			
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	/**
	 * <p>Select file(s) or directory(ies) from the current file system</p>
	 * @param node filesystem to select from. Can't be null. Selection always begins from the current file system point. If user press 'accept', current file system point is committed, else remains unchanged.
	 * @param options options to select (see class constants). All the constants you need must be ORed with '|'
	 * @param callback process pressing of the 'accept' and 'cancel' buttons. Can't be null.  
	 * @param filters file filters.
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException if any of the parameters is null
	 * @throws IllegalArgumentException if options contains incompatible flags
	 */
	public void select(final FileSystemInterface node, final int options, final AcceptAndCancelCallback callback, final FilterCallback... filters) throws IOException, NullPointerException, IllegalArgumentException {
		if (node == null) {
			throw new NullPointerException("Current file system node can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Accept/cancel callback can't be null");
		}
		else if (filter == null) {
			throw new NullPointerException("Filter callback can't be null");
		}
		else if ((options & OPTIONS_FOR_OPEN) == 0 && (options & OPTIONS_FOR_SAVE) == 0) {
			throw new IllegalArgumentException("Neither OPTIONS_FOR_OPEN nor OPTIONS_FOR_SAVE was typed! Type one of these");
		}
		else {
			this.forOpen = (options & OPTIONS_FOR_OPEN) != 0;
			this.currentNode = node;
			this.callback = callback;
			this.canUseDeletion = (options & OPTIONS_ALLOW_DELETE) != 0;
			this.delete.setEnabled(false);
			this.mkDir.setEnabled((options & OPTIONS_ALLOW_MKDIR) != 0);
			this.levelUp.setEnabled(!"/".equals(node.getPath()));
			if ((options & OPTIONS_CAN_MULTIPLE_SELECT) != 0) {
				this.content.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			}
			else {
				this.content.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			}
			this.canSelectDir = (options & OPTIONS_CAN_SELECT_DIR) != 0;
			this.canSelectFile = (options & OPTIONS_CAN_SELECT_FILE) != 0;
			
			filter.addItem(new FilterCallback() {
				@Override
				public String[] getFileMask() {
					return new String[] {"*.*"};
				}

				@Override
				public String getFilterName() {
					try{return localizer.getValue(ACCEPT_ALL_FILES);
					} catch (LocalizationException e) {
						logger.message(Severity.error,e,"Error getting filter name ["+ACCEPT_ALL_FILES+"]: "+e.getLocalizedMessage());
						return ACCEPT_ALL_FILES;
					}
				}

				@Override
				public boolean accept(final FileSystemInterface item) throws IOException {
					return true;
				}
			});	
			for (FilterCallback item : filters) {
				filter.addItem(item);
			}
			filter.setSelectedIndex(0);
			filter.addActionListener((e)->{
				fillCurrentState(currentNode);
			});
			if ((options & OPTIONS_FILE_MUST_EXISTS) != 0) {
				fileName.setInputVerifier(new InputVerifier() {
					@Override
					public boolean verify(final JComponent input) {
						try{final String[]	selection = currentNode.list(Utils.fileMask2Regex(((JTextComponent)input).getText()));
						
							if (selection == null || selection.length == 0) {
								JOptionPane.showMessageDialog(JFileSelectionDialog.this, String.format(localizer.getValue(NOT_EXISTS_MESSAGE),((JTextComponent)input).getText()), localizer.getValue(NOT_EXISTS_CAPTION),JOptionPane.ERROR_MESSAGE);
								return false;
							}
							else {
								return true;
							}
						} catch (IOException | HeadlessException | LocalizationException  e) {
							logger.message(Severity.error,e,"Error processing file name content ["+((JTextComponent)input).getText()+"]: "+e.getLocalizedMessage());
							return false;
						}
					}
				});
			}
			else {
				fileName.setInputVerifier(new InputVerifier() {
					@Override
					public boolean verify(final JComponent input) {
						return true;
					}
				});
			}
			fillCurrentState(currentNode);
		}
	}

	/**
	 * <p>Get selected list. Can be called for pressing 'accept' only, otherwise returned content is unpredictable  
	 * @return selected content. Can be empty, but not null
	 */
	public Iterable<String> getSelection() {
		final List<String>	selected = new ArrayList<>();
		
		for (String item : content.getSelectedValuesList()) {
			try (final FileSystemInterface	fsi = currentNode.clone().open(item)) {
				if (fsi.isDirectory() && canSelectDir || fsi.isFile() && canSelectFile) {
					selected.add(fsi.getPath());
				}
			} catch (IOException e) {
				logger.message(Severity.error,e,"Error getting current file system node ["+item+"] state: "+e.getLocalizedMessage());
			}
		}
		return selected;
	}

	/**
	 * <p>Show modal dialog to select files and return selected list</p>
	 * @param window parent window to use. Can be null
	 * @param localizer localizer to use (see {@linkplain #JFileSelectionDialog(Localizer)}
	 * @param node file system to select file from (see {@linkplain #select(FileSystemInterface, int, AcceptAndCancelCallback, FilterCallback...)}
	 * @param options options to select
	 * @param filter file filter
	 * @return selected files. If the 'cancel' was pressed, will be empty
	 * @throws IOException on any I/O errors
	 * @throws LocalizationException on any localization errors
	 */
	public static Iterable<String> select(final Dialog window, final Localizer localizer, final FileSystemInterface node, final int options, final FilterCallback... filter) throws IOException, LocalizationException {
		final JDialog				dlg = new JDialog(window,true);
		final JFileSelectionDialog	select = new JFileSelectionDialog(localizer);
		@SuppressWarnings("unchecked")
		final Iterable<String>[]	result = new Iterable[] {null}; 
		
		try(final FileSystemInterface	fsi = node.clone()) {
			select.select(fsi, options,
					(accept)->{
						if (accept) {
							result[0] = select.getSelection();
						}
						else {
							result[0] = Arrays.asList();
						}
						dlg.setVisible(false);
						dlg.dispose();
					}, filter);
			dlg.getContentPane().add(select);
			dlg.pack();
			dlg.setLocationRelativeTo(window);
			localizer.addLocaleChangeListener(select);
			dlg.setVisible(true);
			localizer.removeLocaleChangeListener(select);
		}
		return result[0];
	}

	/**
	 * <p>Show modal dialog to select files and return selected list</p>
	 * @param window parent window to use. Can be null
	 * @param localizer localizer to use (see {@linkplain #JFileSelectionDialog(Localizer)}
	 * @param node file system to select file from (see {@linkplain #select(FileSystemInterface, int, AcceptAndCancelCallback, FilterCallback...)}
	 * @param options options to select
	 * @param filter file filter
	 * @return selected files. If the 'cancel' was pressed, will be empty
	 * @throws IOException on any I/O errors
	 * @throws LocalizationException on any localization errors
	 */
	public static Iterable<String> select(final Frame window, final Localizer localizer, final FileSystemInterface node, final int options, final FilterCallback... filter) throws IOException, LocalizationException {
		final JDialog				dlg = new JDialog(window,true);
		final JFileSelectionDialog	select = new JFileSelectionDialog(localizer);
		@SuppressWarnings("unchecked")
		final Iterable<String>[]	result = new Iterable[] {null}; 
		
		try(final FileSystemInterface	fsi = node.clone()) {
			select.select(fsi, options,
					(accept)->{
						if (accept) {
							result[0] = select.getSelection();
						}
						else {
							result[0] = Arrays.asList();
						}
						dlg.setVisible(false);
						dlg.dispose();
					}, filter);
			dlg.getContentPane().add(select);
			dlg.pack();
			dlg.setLocationRelativeTo(window);
			localizer.addLocaleChangeListener(select);
			dlg.setVisible(true);
			localizer.removeLocaleChangeListener(select);
		}
		return result[0];
	}
	
	private void fillCurrentState(final FileSystemInterface current) {
		try{final DefaultComboBoxModel<String>	parentModel = ((DefaultComboBoxModel<String>)parent.getModel()); 
			final String[] 		parts = CharUtils.split(current.getPath(),'/');
			final StringBuilder	prefix = new StringBuilder("  ");
			
			parent.removeActionListener(forParent);
			parentModel.removeAllElements();
			parentModel.addElement("/");
			for (int index = 1; index < parts.length; index++) {
				if (!parts[index].isEmpty()) {
					parentModel.addElement(prefix+parts[index]);
				}
				prefix.append("   ");
			}
			parent.setSelectedIndex(parent.getModel().getSize()-1);
			parent.addActionListener(forParent);
			
			final FilterCallback	currentFilter = (FilterCallback) filter.getSelectedItem();
			
			((DefaultListModel<String>)content.getModel()).clear();
			for (String item : current.list()) {
				try(final FileSystemInterface	fsi = currentNode.clone().open(item)) {
					if (fsi.isDirectory() || currentFilter.accept(fsi)) {
						((DefaultListModel<String>)content.getModel()).addElement(item);
					}
				}
			}
			levelUp.setEnabled(!"/".equals(current.getPath()));
			if (forOpen) {
				accept.setText(localizer.getValue(ACCEPT_OPEN));
				accept.setToolTipText(localizer.getValue(ACCEPT_OPEN_TT));
			}
			else {
				accept.setText(localizer.getValue(ACCEPT_SAVE));
				accept.setToolTipText(localizer.getValue(ACCEPT_SAVE_TT));
			}
		} catch (IOException | LocalizationException  e) {
			logger.message(Severity.error,e,"Error filling current file system state: "+e.getLocalizedMessage());
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		cancel.setText(localizer.getValue(CANCEL));
		cancel.setToolTipText(localizer.getValue(CANCEL_TT));
		accept.setText(localizer.getValue(ACCEPT_OPEN));
		accept.setToolTipText(localizer.getValue(ACCEPT_OPEN_TT));
		fileNameLabel.setText(localizer.getValue(FILE_NAME));
		fileName.setToolTipText(localizer.getValue(FILE_TT));
		filterLabel.setText(localizer.getValue(FILTER_LIST));
		filter.setToolTipText(localizer.getValue(FILTER_LIST_TT));
		levelUp.setToolTipText(localizer.getValue(LEVEL_UP_TT));
		mkDir.setToolTipText(localizer.getValue(MK_DIR_TT));
		delete.setToolTipText(localizer.getValue(DELETE_TT));
		parent.setToolTipText(localizer.getValue(PARENT_TT));
	}

	public static class SimpleFileFilter implements FilterCallback {
		private final String	filterName;
		private final String[]	masks;
		private final Pattern[]	regexMasks;
		
		public SimpleFileFilter(final String filterName, final String... masks) {
			if (filterName == null || filterName.isEmpty()) {
				throw new IllegalArgumentException("Filter name can't be null or empty"); 
			}
			else if (masks == null || masks.length == 0) {
				throw new IllegalArgumentException("File masks can't be null or empty list"); 
			}
			else {
				for (int index = 0; index < masks.length; index++) {
					if (masks[index] == null || masks[index].isEmpty()) {
						throw new IllegalArgumentException("NUll or empty file mask at index ["+index+"] in mask lists"); 
					}
				}
				this.filterName = filterName;
				this.masks = masks;
				this.regexMasks = new Pattern[masks.length];
				for (int index = 0; index < masks.length; index++) {
					this.regexMasks[index] = Pattern.compile(Utils.fileMask2Regex(masks[index]));
				}
			}
		}

		@Override
		public String[] getFileMask() {
			return masks;
		}

		@Override
		public String getFilterName() {
			return filterName;
		}

		@Override
		public boolean accept(final FileSystemInterface item) throws IOException {
			for (Pattern template : regexMasks) {
				if (template.matcher(item.getName()).matches()) {
					return true;
				}
			}
			return false;
		}
	}
}
