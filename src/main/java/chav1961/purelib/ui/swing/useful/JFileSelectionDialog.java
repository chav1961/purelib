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
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
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
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface.FileSystemListCallbackInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.interfaces.PureLibStandardIcons;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.AcceptAndCancelCallback;

/**
 * <p>Standard file selection dialog (see {@linkplain JFileChooser}). Main differences from (see {@linkplain JFileChooser}) are:</p>
 * <ul>
 * <li>This class supports any {@linkplain FileSystemInterface} available, not only local disks</li>  
 * <li>This class is localized and will change all locale-specific information on locale changes</li>  
 * </ul>  
 * <p>Differ to {@linkplain JFileChooser}, this class is a child of the {@linkplain JPanel} and can be embedded into any Swing forms
 * as the part of them. To use it as ordinal dialog window, call it's static methods {@linkplain JFileSelectionDialog#select(Dialog, Localizer, FileSystemInterface, int, FilterCallback...)} and 
 * {@linkplain JFileSelectionDialog#select(Frame, Localizer, FileSystemInterface, int, FilterCallback...)}.</p>
 * <p>This class is not thread-safe</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @see JFileChooser
 * @see FileSystemInterface
 * @see Localizer
 * @since 0.0.3
 * @last.update 0.0.7
 */
public class JFileSelectionDialog extends JPanel implements LocaleChangeListener, LocalizerOwner, LoggerFacadeOwner {
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
	private static final String	ASK_CONFIRM_REPLACEMENT_CAPTION = "JFileSelectionDialog.ask.confirmreplacement.caption";
	private static final String	ASK_CONFIRM_REPLACEMENT_MESSAGE = "JFileSelectionDialog.ask.confirmreplacement.message";
	private static final String	ALREADY_EXISTS_CAPTION = "JFileSelectionDialog.ask.alreadyexists.caption";
	private static final String	ALREADY_EXISTS_MESSAGE = "JFileSelectionDialog.ask.alreadyexists.message";
	private static final String	NOT_EXISTS_CAPTION = "JFileSelectionDialog.ask.notexists.caption";
	private static final String	NOT_EXISTS_MESSAGE = "JFileSelectionDialog.ask.notexists.message";
	private static final String	NOT_ACCEPTED_BY_FILTER_CAPTION = "JFileSelectionDialog.ask.notaccepted.byFilter.caption";
	private static final String	NOT_ACCEPTED_BY_FILTER_MESSAGE = "JFileSelectionDialog.ask.notaccepted.byFilter.message";
	private static final String	FILE_NAME_NOT_FILLED = "JFileSelectionDialog.error.filename.not.filled";
	
	public static final int		OPTIONS_CAN_SELECT_DIR = 1 << 0; 
	public static final int		OPTIONS_CAN_SELECT_FILE = 1 << 1; 
	public static final int		OPTIONS_CAN_MULTIPLE_SELECT = 1 << 2; 
	public static final int		OPTIONS_FILE_MUST_EXISTS = 1 << 3; 
	public static final int		OPTIONS_ALLOW_MKDIR = 1 << 4; 
	public static final int		OPTIONS_ALLOW_DELETE = 1 << 5; 
	public static final int		OPTIONS_FOR_SAVE  = 1 << 6; 
	public static final int		OPTIONS_FOR_OPEN  = 1 << 7; 
	public static final int		OPTIONS_CONFIRM_REPLACEMENT = 1 << 8; 
	public static final int		OPTIONS_NOCHECK_FILTER = 1 << 9; 
	public static final int		OPTIONS_APPEND_EXTENSION = 1 << 10; 

	private static final Comparator<String[]>	ORDER = (s1,s2)->s1[1].compareToIgnoreCase(s2[1]);
	private static final FilterCallback			ALL_CALLBACK = FilterCallback.of(ACCEPT_ALL_FILES, "*");
	
	private static final Iterator<String>		NULL_ITERATOR = new Iterator<String>() {
													@Override public boolean hasNext() {return false;}
													@Override public String next() {return null;}
												};
	private static final Iterable<String>		NULL_ITERABLE = new Iterable<String>() {
													@Override public Iterator<String> iterator() {return NULL_ITERATOR;}
												};
	
	/**
	 * <p>This interface is analog of {@linkplain FilterCallback}</p>  
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 * @last.update 0.0.7
	 */
	public interface FilterCallback {
		/**
		 * <p>Get filter masks
		 * @return filter masks (with '?' and '*' wildcards). Can be empty, but not null
		 */
		String[] getFileMask();
		
		/**
		 * <p>Get filter name. String will be used 'as-is', you should make yourself any localization you need</p> 
		 * @return filter name. Can't be null
		 */
		String getFilterName();
		
		/**
		 * <p>Get preferred file extension for the given filter type</p>
		 * @return preferred extension or null if not defined<
		 */
		default String getPreferredFileExtension() {
			return null;
		}
		
		/**
		 * <p>Accept file system interface item with the given filter.</p>
		 * @param item file system item to test
		 * @return true if the item must be include into the list, false otherwise
		 * @throws IOException when I/O errors during processing
		 */
		boolean accept(final FileSystemInterface item) throws IOException;

		/**
		 * <p>Accept file system interface item with the given filter.</p>
		 * @param item file system item to test
		 * @return true if the item must be include into the list, false otherwise
		 * @throws IOException when I/O errors during processing
		 */
		boolean accept(final File item) throws IOException;
		
		/**
		 * <p>Create simple filter callback implementation</p>
		 * @param name filter name. Can't be null or empty
		 * @param mask list of filter masks. Can't be empty
		 * @return filter callback implementation. Can't be null
		 * @throws IllegalArgumentException on any argument errors
		 * @since 0.0.5
		 * @lastUpdate 0.0.6
		 */
		static FilterCallback of(final String name, final String... mask) throws IllegalArgumentException {
			return ofWithExtension(name, null, mask);
		}		
		
		/**
		 * <p>Create simple filter callback implementation</p>
		 * @param name filter name. Can't be null or empty
		 * @param preferredExtension preferred extension for the given file. Can be null or empty if not required
		 * @param mask list of filter masks. Can't be empty
		 * @return filter callback implementation. Can't be null
		 * @throws IllegalArgumentException on any argument errors
		 * @since 0.0.7
		 */
		static FilterCallback ofWithExtension(final String name, final String preferredExtension, final String... mask) throws IllegalArgumentException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Filter name can't be null or empty");
			}
			else if (mask == null || mask.length == 0 || Utils.checkArrayContent4Nulls(mask, true) >= 0) {
				throw new IllegalArgumentException("Mask is null/empty or contains nulls/empties inside");
			}
			else {
				final Pattern[]	p = new Pattern[mask.length];
				
				for (int index = 0; index < p.length; index++) {
					p[index] = Pattern.compile(Utils.fileMask2Regex(mask[index]));
				}
				
				return new FilterCallback() {
					@Override public String[] getFileMask() {return mask;}
					@Override public String getFilterName() {return name;}
					@Override public String getPreferredFileExtension() {return preferredExtension;}

					@Override
					public boolean accept(final FileSystemInterface item) throws IOException {
						if (item.isFile()) {
							return accept(item.getName());
						}
						else {
							return true;
						}
					}
					
					@Override
					public boolean accept(final File item) throws IOException {
						return accept(item.getName());
					}
					
					private boolean accept(final String name) {
						for (Pattern pItem : p) {
							if (pItem.matcher(name).find()) {
								return true;
							}
						}
						return false;
					}
				}; 
			}
		}
	}
	
	private final Localizer			localizer;
	private final JComboBox<String>	parent = new JComboBox<>();
	private final JButton			mkDir = new JWrappedButton(PureLibStandardIcons.NEW_DIR.getIcon());
	private final JButton			delete = new JWrappedButton(PureLibStandardIcons.REMOVE.getIcon());
	private final JButton			levelUp = new JWrappedButton(PureLibStandardIcons.LEVEL_UP.getIcon());
	private final JList<String[]>	content = new JList<>();
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
												getLogger().message(Severity.error,exc,"Error changing file system location to parent: "+exc.getLocalizedMessage());
											}
										}
									};
									
	private boolean					forOpen = true, canSelectDir = false, canSelectFile = false, canUseDeletion = true;
	private FileSystemInterface 	currentNode = null;
	private AcceptAndCancelCallback<JFileSelectionDialog> callback;

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use with the class. Can't be null. It's strongly recommended to use {@linkplain PureLibSettings#PURELIB_LOCALIZER} 
	 * localizer to call the constructor</p>
	 * @throws LocalizationException in any localization errors
	 * @throws NullPointerException if any parameter is null
	 */
	public JFileSelectionDialog(final Localizer localizer) throws LocalizationException, NullPointerException {
		super(new BorderLayout());
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			this.filter.setRenderer(SwingUtils.getCellRenderer(FilterCallback.class, new FieldFormat(FilterCallback.class), ListCellRenderer.class, localizer));
			
			fileName.setColumns(30);
			
			final SpringLayout		topSpring = new SpringLayout();
			final JPanel			topPanel = new JPanel(topSpring);
			final JPanel			topRightPanel = new JPanel(new GridLayout(1,3,5,0));
			
			topRightPanel.add(levelUp);
			topRightPanel.add(mkDir);
			topRightPanel.add(delete);
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
					getLogger().message(Severity.error,e1,"Error changing file system location to parent: "+e1.getLocalizedMessage());
				}
			});
			mkDir.addActionListener((e)->{
				String	answer = null;
				
				try{if ((answer = JOptionPane.showInputDialog(this,localizer.getValue(ASK_ENTER_DIR_MESSAGE),localizer.getValue(ASK_ENTER_DIR_CAPTION),JOptionPane.QUESTION_MESSAGE)) != null) {
						try (final FileSystemInterface	fsi = currentNode.clone().open(answer)) {
							if (fsi.exists()) {
								JOptionPane.showMessageDialog(this, new LocalizedFormatter(ALREADY_EXISTS_MESSAGE,answer), localizer.getValue(ALREADY_EXISTS_CAPTION),JOptionPane.QUESTION_MESSAGE);
							}
							else {
								fsi.mkDir();
							}
						}
						fillCurrentState(currentNode);
					}
				} catch (HeadlessException | LocalizationException | IllegalArgumentException | IOException e1) {
					getLogger().message(Severity.error,e1,"Error creating directory ["+answer+"] : "+e1.getLocalizedMessage());
				}
			});
			delete.addActionListener((e)->{
				final Set<String>	forDeletion = new HashSet<>();
				
				for (String[] item : content.getSelectedValuesList()) {
					forDeletion.add(item[0]);
				}
				try{if (forDeletion.size() > 0 && new JLocalizedOptionPane(localizer).confirm(this, new LocalizedFormatter(ASK_CONFIRM_DELETE_MESSAGE,forDeletion),ASK_CONFIRM_DELETE_CAPTION,JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						for (String item : forDeletion) {
							try (final FileSystemInterface	fsi = currentNode.clone().open(item)) {
								fsi.deleteAll();
							}
						}
						fillCurrentState(currentNode);
					}
				} catch (HeadlessException | LocalizationException | IllegalArgumentException | IOException e1) {
					getLogger().message(Severity.error,e1,"Error deleting selected list : "+e1.getLocalizedMessage());
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
			
			assignEnterAndEscape(this);
			assignEnterAndEscape(fileName);
			accept.addActionListener((e)->{
				selectAndAccept();
			});
			cancel.addActionListener((e)->{
				cancel();
			});
			
			bottomPanel.setMinimumSize(new Dimension(MIN_WIDTH,4*MIN_HEIGHT/16));
			bottomPanel.setPreferredSize(new Dimension(MIN_WIDTH,4*MIN_HEIGHT/16));
			
			final JScrollPane	scroll = new JScrollPane(content); 
			
			content.setModel(new DefaultListModel<String[]>());
			content.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			content.setVisibleRowCount(-1);
			content.setCellRenderer(new ListCellRenderer<String[]>() {
				@Override
				public Component getListCellRendererComponent(JList<? extends String[]> list, String[] value, int index, boolean isSelected, boolean cellHasFocus) {
					final JLabel	result = new JLabel(URLDecoder.decode(value[1], Charset.forName(PureLibSettings.DEFAULT_CONTENT_ENCODING)));
					
					try(final FileSystemInterface	node = currentNode.clone().open(value[0]);) {
						
						if (node.isDirectory()) {
							result.setIcon(PureLibStandardIcons.DIRECTORY.getIcon());
							if (isSelected && canSelectDir) {
								result.setOpaque(true);
								result.setForeground(list.getSelectionForeground());
								result.setBackground(list.getSelectionBackground());
							}
						}
						else {
							result.setIcon(PureLibStandardIcons.FILE.getIcon());
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
						getLogger().message(Severity.error,e,"Error querying file system node ["+value+"]: "+e.getLocalizedMessage());
					}
					
					result.setPreferredSize(new Dimension(MIN_WIDTH/3,MIN_HEIGHT/16));
					result.setToolTipText(value[0]);
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
							
							try (final FileSystemInterface	fsi = currentNode.clone().open(content.getModel().getElementAt(index)[0])) {
								
								if (fsi.isDirectory()) {
									newLocation = fsi.getName();
								}
								else {
									content.setSelectedIndex(index);
									fileName.setText(content.getSelectedValue()[0]);
									selectAndAccept();
								}
							} catch (IOException exc) {
								getLogger().message(Severity.error,exc,"Error querying file system node ["+content.getModel().getElementAt(index)+"]: "+exc.getLocalizedMessage());
							}
							if (newLocation != null) {
								try{fillCurrentState(currentNode.open(newLocation));
								} catch (IOException exc) {
									getLogger().message(Severity.error,exc,"Error opening file system node ["+newLocation+"]: "+exc.getLocalizedMessage());
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

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}
	
	@Override
	public LoggerFacade getLogger() {
		return SwingUtils.getNearestLogger(getParent());
	}
	
	/**
	 * <p>Select file(s) or directory(ies) from the current file system</p>
	 * @param node file system to select from. Can't be null. Selection always begins from the current file system point. If user press 'accept', current file system point is committed, else remains unchanged.
	 * @param options options to select (see class constants). All the constants you need must be inclusive ORed with '|'
	 * @param callback process pressing of the 'accept' and 'cancel' buttons. Can't be null.  
	 * @param filters file filters.
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException if any of the parameters is null
	 * @throws IllegalArgumentException if options contains incompatible flags
	 * @since 0.0.7
	 */
	public void select(final FileSystemInterface node, final int options, final AcceptAndCancelCallback<JFileSelectionDialog> callback, final FilterCallback... filters) throws IOException, NullPointerException, IllegalArgumentException {
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
			this.callback = (options & OPTIONS_FOR_SAVE) != 0 && (options & OPTIONS_CONFIRM_REPLACEMENT) != 0  
					? new AcceptAndCancelCallback<JFileSelectionDialog>() {
							@Override
							public boolean process(final JFileSelectionDialog owner, final boolean accept) {
								if (accept) {
									if (checkReplacement(node,getSelection((options & OPTIONS_APPEND_EXTENSION) == OPTIONS_APPEND_EXTENSION))) {
										return callback.process(owner, accept);
									}
									else {
										return false;
									}
								}
								else {
									return callback.process(owner, accept);
								}
							}
						}
					: callback;
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

			if (filters.length == 0) {
				filter.addItem(ALL_CALLBACK);
			}
			else {
				for (FilterCallback item : filters) {
					filter.addItem(item);
				}
			}
			filter.setSelectedIndex(0);
			filter.addActionListener((e)->{
				fillCurrentState(currentNode);
			});
			fillCurrentState(currentNode);
			if ((options & OPTIONS_FOR_OPEN) != 0) {
				content.requestFocusInWindow();
			}
			else {
				fileName.requestFocusInWindow();
			}
		}
	}

	/**
	 * <p>Get selected list. Can be called for pressing 'accept' only, otherwise returned content is unpredictable  
	 * @return selected content. Can be empty, but not null
	 */
	public Iterable<String> getSelection() {
		return getSelection(false);
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
		return selectInternal(new JDialog(window,true), localizer, PureLibSettings.CURRENT_LOGGER, node, options, filter);
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
		return selectInternal(new JDialog(window,true), localizer, PureLibSettings.CURRENT_LOGGER, node, options, filter);
	}

	/**
	 * <p>Show modal dialog to select files and return selected list</p>
	 * @param window parent window to use. Can be null
	 * @param localizer localizer to use (see {@linkplain #JFileSelectionDialog(Localizer)}
	 * @param logger logger to print messages to. Can't be null.
	 * @param node file system to select file from (see {@linkplain #select(FileSystemInterface, int, AcceptAndCancelCallback, FilterCallback...)}
	 * @param options options to select
	 * @param filter file filter
	 * @return selected files. If the 'cancel' was pressed, will be empty
	 * @throws IOException on any I/O errors
	 * @throws LocalizationException on any localization errors
	 * @since 0.0.5
	 */
	public static Iterable<String> select(final Dialog window, final Localizer localizer, final LoggerFacade logger, final FileSystemInterface node, final int options, final FilterCallback... filter) throws IOException, LocalizationException {
		return selectInternal(new JDialog(window,true), localizer, logger, node, options, filter);
	}
	
	/**
	 * <p>Show modal dialog to select files and return selected list</p>
	 * @param window parent window to use. Can be null
	 * @param localizer localizer to use (see {@linkplain #JFileSelectionDialog(Localizer)}
	 * @param logger logger to print messages to. Can't be null.
	 * @param node file system to select file from (see {@linkplain #select(FileSystemInterface, int, AcceptAndCancelCallback, FilterCallback...)}
	 * @param options options to select
	 * @param filter file filter
	 * @return selected files. If the 'cancel' was pressed, will be empty
	 * @throws IOException on any I/O errors
	 * @throws LocalizationException on any localization errors
	 * @since 0.0.5
	 */
	public static Iterable<String> select(final Frame window, final Localizer localizer, final LoggerFacade logger, final FileSystemInterface node, final int options, final FilterCallback... filter) throws IOException, LocalizationException {
		return selectInternal(new JDialog(window,true), localizer, logger, node, options, filter);
	}

	protected boolean checkReplacement(final FileSystemInterface node, final Iterable<String> selection) {
		for (String item : selection) {
			try(final FileSystemInterface	fsi = node.clone().open(item)) {
				if (fsi.exists() && new JLocalizedOptionPane(localizer).confirm(this, new LocalizedFormatter(ASK_CONFIRM_REPLACEMENT_MESSAGE, item), ASK_CONFIRM_REPLACEMENT_CAPTION, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
					return false;
				}
			} catch (IOException e) {
				PureLibSettings.CURRENT_LOGGER.message(Severity.debug, e, e.getLocalizedMessage());
				return false;
			}
		}
		return true;
	}

	protected void selectAndAccept() {
		if (callback != null && callback.process(JFileSelectionDialog.this, true)) {
			callback = null;
		}
	}

	private Iterable<String> getSelection(final boolean appendExtension) {
		final List<String>	selected = new ArrayList<>();
		
		for (String[] item : content.getSelectedValuesList()) {
			try (final FileSystemInterface	fsi = currentNode.clone().open(item[0])) {
				if (fsi.isDirectory() && canSelectDir || fsi.isFile() && canSelectFile) {
					selected.add(fsi.getPath());
				}
			} catch (IOException e) {
				getLogger().message(Severity.error,e,"Error getting current file system node ["+item+"] state: "+e.getLocalizedMessage());
			}
		}
		if (selected.size() == 0) {
			final FilterCallback 	filterValue = (FilterCallback) filter.getSelectedItem(); 
			String					item = fileName.getText().trim();

			if (!item.isEmpty()) {
				if (appendExtension 
						&& !Utils.checkEmptyOrNullString(filterValue.getPreferredFileExtension())
						&& !item.endsWith('.'+filterValue.getPreferredFileExtension())) {
					item += '.'+filterValue.getPreferredFileExtension();
				}
				
				try(final FileSystemInterface	fsi = currentNode.clone().open(item)) {
					selected.add(fsi.getPath());
				} catch (IOException e) {
					getLogger().message(Severity.error,e,"Error getting current file system node ["+item+"] state: "+e.getLocalizedMessage());
				}
			}
			return selected;
		}
		else {
			return selected;
		}
	}

	private static Iterable<String> selectInternal(final JDialog dlg, final Localizer localizer, final LoggerFacade logger, final FileSystemInterface node, final int options, final FilterCallback... filter) throws IOException, LocalizationException {
		final JFileSelectionDialog	select = new JFileSelectionDialog(localizer);
		@SuppressWarnings("unchecked")
		final Iterable<String>[]	result = new Iterable[] {NULL_ITERABLE}; 
		
		try(final FileSystemInterface	fsi = node.clone()) {
			if ((options & OPTIONS_FOR_OPEN) == OPTIONS_FOR_OPEN) {
				dlg.setTitle(localizer.getValue(CAPTION_OPEN));
				select.select(fsi, options,
						(owner,accept)->{
							if (accept) {
								if (select.checkInput(select.fileName, options)) {
									result[0] = select.getSelection((options & OPTIONS_APPEND_EXTENSION) == OPTIONS_APPEND_EXTENSION);
								}
								else {
									return false;
								}
							}
							else {
								result[0] = Arrays.asList();
							}
							dlg.setVisible(false);
							dlg.dispose();
							return true;
						}, filter);
			}
			else {
				dlg.setTitle(localizer.getValue(CAPTION_SAVE));
				if (fsi.isFile()) {
					final String	fileName = fsi.getName();
					
					select.select(fsi.open("../"), options,
							(owner,accept)->{
								if (accept) {
									if (select.checkInput(select.fileName, options)) {
										result[0] = select.getSelection((options & OPTIONS_APPEND_EXTENSION) == OPTIONS_APPEND_EXTENSION);
									}
									else {
										return false;
									}
								}
								else {
									result[0] = Arrays.asList();
								}
								dlg.setVisible(false);
								dlg.dispose();
								return true;
							}, filter);
					select.fileName.setText(fileName);
				}
				else {
					select.select(fsi, options,
							(owner,accept)->{
								if (accept) {
									if (select.checkInput(select.fileName, options)) {
										result[0] = select.getSelection((options & OPTIONS_APPEND_EXTENSION) == OPTIONS_APPEND_EXTENSION);
									}
									else {
										return false;
									}
								}
								else {
									result[0] = Arrays.asList();
								}
								dlg.setVisible(false);
								dlg.dispose();
								return true;
							}, filter);
				}
			}
			dlg.getContentPane().add(select);
			dlg.pack();
			dlg.setLocationRelativeTo(null);
			localizer.addLocaleChangeListener(select);
			select.content.requestFocusInWindow();
			dlg.setVisible(true);
			localizer.removeLocaleChangeListener(select);
		}
		return result[0];
	}
	
	private void cancel() {
		if (callback != null) {
			callback.process(this,false);
			callback = null;
		}				
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
			final List<String[]>	forDirs = new ArrayList<>();
			final List<String[]>	forFiles = new ArrayList<>();
			
			((DefaultListModel<String[]>)content.getModel()).clear();
			current.list(new FileSystemListCallbackInterface() {
				@Override
				public ContinueMode process(final FileSystemInterface item) throws IOException {
					if (item.isDirectory()) {
						forDirs.add(new String[] {item.getName(), item.getAlias()});
					}
					else if (currentFilter.accept(item)) {
						forFiles.add(new String[] {item.getName(), item.getAlias()});
					}
					return ContinueMode.CONTINUE;
				}
			});
			forDirs.sort(ORDER);
			((DefaultListModel<String[]>)content.getModel()).addAll(forDirs);
			forFiles.sort(ORDER);
			((DefaultListModel<String[]>)content.getModel()).addAll(forFiles);
			
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
			getLogger().message(Severity.error,e,"Error filling current file system state: "+e.getLocalizedMessage());
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

	private void assignEnterAndEscape(final JComponent component) {
		SwingUtils.assignActionKey(component, JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_ACCEPT, (e) -> selectAndAccept(), SwingUtils.ACTION_ACCEPT);
		SwingUtils.assignActionKey(component, JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_EXIT, (e) -> cancel(), SwingUtils.ACTION_EXIT);
	}

	private boolean checkInput(final JTextComponent input, final int options) {
		String	fileName = input.getText().trim();
		
		if ((options & OPTIONS_APPEND_EXTENSION) != 0 
				&& !Utils.checkEmptyOrNullString(((FilterCallback)filter.getSelectedItem()).getPreferredFileExtension())
				&& !fileName.endsWith('.'+((FilterCallback)filter.getSelectedItem()).getPreferredFileExtension())) {
			fileName += '.'+((FilterCallback)filter.getSelectedItem()).getPreferredFileExtension();
		}
		
		if (!fileName.isEmpty()) {
			try{final String[]	selection = currentNode.list(Utils.fileMask2Regex(fileName));
			
				if (selection == null || selection.length == 0) {
					new JLocalizedOptionPane(localizer).message(JFileSelectionDialog.this, new LocalizedFormatter(NOT_EXISTS_MESSAGE,fileName), NOT_EXISTS_CAPTION, JOptionPane.ERROR_MESSAGE);
					
					return false;
				}
				else if ((options & OPTIONS_NOCHECK_FILTER) != 0) {
					return true;
				}
				else {
					try{if (!((FilterCallback)filter.getSelectedItem()).accept(new File(fileName))) {
							new JLocalizedOptionPane(localizer).message(JFileSelectionDialog.this, new LocalizedFormatter(NOT_ACCEPTED_BY_FILTER_MESSAGE,fileName), NOT_ACCEPTED_BY_FILTER_CAPTION, JOptionPane.ERROR_MESSAGE);
							
							return false;
						}
						else {
							return true;
						}
					} catch (IOException e) {
						getLogger().message(Severity.error,e,"Error processing file name content ["+content+"]: "+e.getLocalizedMessage());
						return false;
					}
				}
			} catch (IOException | HeadlessException | LocalizationException  e) {
				getLogger().message(Severity.error,e,"Error processing file name content ["+content+"]: "+e.getLocalizedMessage());
				return false;
			}
		}
		else {
			getLogger().message(Severity.error, FILE_NAME_NOT_FILLED);
			return false;
		}
	}
	
	private static class JWrappedButton extends JButton {
		private static final long serialVersionUID = 1L;

		public JWrappedButton(Icon icon) {
			super(icon);
			setPreferredSize(new Dimension(icon.getIconWidth() - ICON_BORDER_WIDTH, icon.getIconHeight() - ICON_BORDER_WIDTH));
		}
	}
}
