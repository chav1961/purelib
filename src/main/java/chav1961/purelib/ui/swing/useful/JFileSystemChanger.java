package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.ui.interfaces.Action;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.PureLibStandardIcons;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.AcceptAndCancelCallback;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.useful.DnDManager.DnDInterface;

/**
 * <p>This class implements File system changer window and supports model dialogs with it. </p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public class JFileSystemChanger extends JPanel implements LocaleChangeListener {
	private static final long 		serialVersionUID = 6307351718365525165L;
	private static final String 	ACCEPT = "JFileSystemChanger.button.accept";
	private static final String 	CANCEL = "JFileSystemChanger.button.cancel";
	private static final String 	TEST = "JFileSystemChanger.button.test";
	private static final String 	TEST_CAPTION = "JFileSystemChanger.button.test.caption";
	private static final String 	ACCEPT_TOOLTIP = "JFileSystemChanger.button.accept.tooltip";
	private static final String 	CANCEL_TOOLTIP = "JFileSystemChanger.button.cancel.tooltip";
	private static final String 	TEST_TOOLTIP = "JFileSystemChanger.button.test.tooltip";
	private static final String 	TEST_URI_TOOLTIP = "JFileSystemChanger.test.uri.tooltip";
	private static final String 	ERROR_HEADER = "JFileSystemChanger.message.error.header";
	private static final String 	ERROR_DESCRIPTION = "JFileSystemChanger.message.error.description";
	private static final Icon		TEST_OK = PureLibStandardIcons.SUCCESS.getIcon();
	private static final Icon		TEST_FAILED = PureLibStandardIcons.FAIL.getIcon();
	
	private final Localizer			localizer;
	private final FileSystemDescription			content = new FileSystemDescription();
	private final JLabel			uri2testLabel = new JLabel();
	private final JTextField		uri2test = new JTextField();
	private final JButton 			testButton = new JButton(), acceptButton = new JButton(), cancelButton = new JButton(); 
	private final J2ColumnEditor	editor;
	private final JList<?>			list;
	private URI						testUri = null;
	
	/**
	 * <p>COnstructor of the class</p>
	 * @param localizer localizer to use. Usually {@linkPlain PureLibSettings#PURELIB_LOCALIZER} is enough
	 * @param callback callback to process "OK" and "Cancel" buttons
	 * @throws IOException on any I/O errors
	 * @throws LocalizationException on any localization errors
	 * @throws NullPointerException when any argument is null
	 * @throws IllegalArgumentException on any arguments error
	 * @throws ContentException on any file system content errors
	 */
	public JFileSystemChanger(final Localizer localizer, final AcceptAndCancelCallback<JFileSystemChanger> callback) throws IOException, LocalizationException, NullPointerException, IllegalArgumentException, ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			final JPanel		bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			final JPanel		centerPanel = new JPanel();
			final JPanel		leftPanel = new JPanel(new BorderLayout(2,2));
			final JPanel		leftBottomPanel = new JPanel(new BorderLayout(2,2));
	
			this.localizer = localizer;
			this.list = new JList<>(FileSystemFactory.getAvailableFileSystems());
			this.editor = new J2ColumnEditor(ContentModelFactory.forAnnotatedClass(content.getClass()),content,(c)-> {
				try{if (c.endsWith("showHelp")) {
						SwingUtils.showCreoleHelpWindow(JFileSystemChanger.this,URI.create("self:/#"+Base64.getEncoder().encodeToString(localizer.getValue(content.helpId).getBytes())));
					}
					else if (c.endsWith("showLicense")) {
						SwingUtils.showCreoleHelpWindow(JFileSystemChanger.this,URI.create("self:/#"+Base64.getEncoder().encodeToString(localizer.getValue(content.licenseContentId).getBytes())));
					}
					else if (c.endsWith("copyPasteUri")) {
						uri2test.setText(content.uriTemplate);
					}
					else {
						// TODO:
					}
				} catch (LocalizationException | NullPointerException | IllegalArgumentException | IOException exc) {
					exc.printStackTrace();
				}
			});
			
			this.list.addListSelectionListener((e)->{
				try{fillRecord((FileSystemInterfaceDescriptor)list.getSelectedValue(),content);
					this.testButton.setIcon(null);
				} catch (LocalizationException | ContentException exc) {
					exc.printStackTrace();
				}
			});
			this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.list.setCellRenderer(
				(list, value, index, isSelected, cellHasFocus)-> {
					try{final JLabel	label = new JLabel(localizer.getValue(((FileSystemInterfaceDescriptor)value).getDescriptionId())
												,((FileSystemInterfaceDescriptor)value).getIcon()
												,JLabel.LEFT);
					
						label.setToolTipText(localizer.getValue(((FileSystemInterfaceDescriptor)value).getHelpId()));
						label.setOpaque(true);
						label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
						label.setBorder(cellHasFocus ? new LineBorder(Color.BLACK) : new EmptyBorder(1, 1, 1, 1));
						return label;
					} catch (LocalizationException exc) {
						return new JLabel(value.toString());
					}
				}
			);
			
			this.testButton.addActionListener((e)->{
				try{testUri = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":unknown:/");
				
					try{testUri = URI.create(uri2test.getText());
						try(final FileSystemInterface	fsi = FileSystemFactory.createFileSystem(testUri)) {
							
							this.testButton.setIcon(TEST_OK);
							return;
						} catch (IllegalArgumentException | IOException exc) {
							JOptionPane.showMessageDialog(JFileSystemChanger.this, exc.getLocalizedMessage(), localizer.getValue(ERROR_HEADER), JOptionPane.ERROR_MESSAGE);
						}
					} catch (IllegalArgumentException exc) {
						JOptionPane.showMessageDialog(JFileSystemChanger.this, exc.getLocalizedMessage(), localizer.getValue(ERROR_HEADER), JOptionPane.ERROR_MESSAGE);
					}
				} catch (LocalizationException exc) {
					exc.printStackTrace();
				}
				this.testButton.setIcon(TEST_FAILED);
				testUri = null;
			});
			this.uri2test.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					testButton.setEnabled(e.getLength() != 0);
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					testButton.setEnabled(e.getLength() != 0);
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
					testButton.setEnabled(e.getLength() != 0);				
				}
			});
			this.testButton.setEnabled(false);
			
			leftBottomPanel.add(uri2testLabel,BorderLayout.WEST);
			leftBottomPanel.add(uri2test,BorderLayout.CENTER);
			leftBottomPanel.add(testButton,BorderLayout.EAST);
			
			editor.setPreferredSize(new Dimension(400,170));
			leftPanel.add(editor,BorderLayout.CENTER);
			leftPanel.add(leftBottomPanel,BorderLayout.SOUTH);
			
			centerPanel.setLayout(new GridLayout(1,2,2,2));
			centerPanel.add(leftPanel);
			centerPanel.add(new JScrollPane(this.list));

			if (callback != null) {
				acceptButton.addActionListener((e)->{callback.process(this,true);});
				bottomPanel.add(acceptButton);
				cancelButton.addActionListener((e)->{callback.process(this,false);});
				bottomPanel.add(cancelButton);
			}
			
			setLayout(new BorderLayout(2,2));
			add(centerPanel,BorderLayout.CENTER);
			add(bottomPanel,BorderLayout.SOUTH);
			fillLocalizedStrings();
			list.requestFocusInWindow();
			list.setSelectedIndex(0);
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}
	
	/**
	 * <p>Ask file system selection</p>
	 * @param window parent frame 
	 * @param localizer localizer. Usually {@linkPlain PureLibSettings#PURELIB_LOCALIZER} is enough 
	 * @return file system URI when selected, null otherwise
	 * @throws LocalizationException on any localization errors
	 * @throws ContentException on any content errors
	 * @throws IOException on any I/O errors
	 */
	public static URI ask(final Frame window, final Localizer localizer) throws LocalizationException, SyntaxException, ContentException, IOException {
		return askInternal(window,new JDialog(window,true), localizer);
	}

	/**
	 * <p>Ask file system selection</p>
	 * @param window parent frame 
	 * @param localizer localizer. Usually {@linkPlain PureLibSettings#PURELIB_LOCALIZER} is enough 
	 * @return file system URI when selected, null otherwise
	 * @throws LocalizationException on any localization errors
	 * @throws ContentException on any content errors
	 * @throws IOException on any I/O errors
	 */
	public static URI ask(final Dialog window, final Localizer localizer, final AutoBuiltForm<?> form) throws LocalizationException, SyntaxException, ContentException, IOException {
		return askInternal(window,new JDialog(window,true), localizer);
	}

	private static URI askInternal(final Window parent, final JDialog dlg, final Localizer localizer) throws LocalizationException, SyntaxException, ContentException, IOException {
		final URI[]					result = new URI[] {null};
		final JFileSystemChanger	mgr = new JFileSystemChanger(localizer, (manager,mode)->{
												if (mode) {
													manager.getActionMap().get(SwingUtils.ACTION_ACCEPT).actionPerformed(null);
												}
												dlg.setVisible(false);
											}); 
		
		SwingUtils.assignActionKey(mgr, JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_ACCEPT, (e) -> {
			if (mgr.testUri == null) {
				try{new JLocalizedOptionPane(localizer).message(dlg, ERROR_DESCRIPTION, ERROR_HEADER, JOptionPane.ERROR_MESSAGE);
				} catch (LocalizationException exc) {
					PureLibSettings.logger.severe(exc.getLocalizedMessage());
				}
			}
			else {
				result[0] = mgr.testUri;
				dlg.setVisible(false);
			}
		}, SwingUtils.ACTION_ACCEPT);
		SwingUtils.assignActionKey(mgr, JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_EXIT, (e) -> {
			result[0] = null;
			dlg.setVisible(false);
		}, SwingUtils.ACTION_EXIT);
		
		dlg.getContentPane().add(mgr,BorderLayout.CENTER);
		dlg.pack();
		dlg.setLocationRelativeTo(parent);
		
		dlg.setVisible(true);
		dlg.dispose();
		return result[0];
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		testButton.setText(localizer.getValue(TEST));
		testButton.setToolTipText(localizer.getValue(TEST_TOOLTIP));
		acceptButton.setText(localizer.getValue(ACCEPT));
		acceptButton.setToolTipText(localizer.getValue(ACCEPT_TOOLTIP));
		cancelButton.setText(localizer.getValue(CANCEL));
		cancelButton.setToolTipText(localizer.getValue(CANCEL_TOOLTIP));
		uri2testLabel.setText(localizer.getValue(TEST_CAPTION));
		uri2test.setToolTipText(localizer.getValue(TEST_URI_TOOLTIP));
		((LocaleChangeListener)editor).localeChanged(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
	}

	private void fillRecord(final FileSystemInterfaceDescriptor desc, final FileSystemDescription target) throws LocalizationException, IllegalArgumentException, NullPointerException, ContentException {
		target.className = desc.getClassName();
		target.version = desc.getVersion();
		target.vendorId = localizer.getValue(desc.getVendorId());
		target.licenseId = localizer.getValue(desc.getLicenseId());
		target.licenseContentId = localizer.getValue(desc.getLicenseContentId());
		target.helpId = localizer.getValue(desc.getHelpId());
		target.uriTemplate = desc.getUriTemplate().toString();
		SwingUtils.putToScreen(this.editor.getMetadata().getRoot(),target,this.editor);
	}

	@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":xml:root://chav1961.purelib.ui.swing.useful.JFileSystemChanger/chav1961/purelib/i18n/localization.xml")
	@LocaleResource(value="JFileSystemChanger.descriptor.caption",tooltip="JFileSystemChanger.descriptor.tooltip")
	public static class FileSystemDescription implements FormManager<Object, FileSystemDescription> { 
		@LocaleResource(value="JFileSystemChanger.descriptor.className",tooltip="JFileSystemChanger.descriptor.className.tooltip")
		@Format("r")
		String className = "";
		
		@LocaleResource(value="JFileSystemChanger.descriptor.version",tooltip="JFileSystemChanger.descriptor.version.tooltip")
		@Format("r")
		String version = "";
		
		@LocaleResource(value="JFileSystemChanger.descriptor.description",tooltip="JFileSystemChanger.descriptor.description.tooltip")
		@Format("r")
		@Action(actionString="showHelp",resource=@LocaleResource(tooltip="JFileSystemChanger.descriptor.help.show.tooltip",value="Advanced"))
		String descriptionId = "";
		
		@LocaleResource(value="JFileSystemChanger.descriptor.vendor",tooltip="JFileSystemChanger.descriptor.vendor.tooltip")
		@Format("r")
		String vendorId = "";
		
		@LocaleResource(value="JFileSystemChanger.descriptor.license",tooltip="JFileSystemChanger.descriptor.license.tooltip")
		@Format("r")
		@Action(actionString="showLicense",resource=@LocaleResource(tooltip="JFileSystemChanger.descriptor.license.show.tooltip",value="Advanced"))
		String licenseId = "";

		String helpId = "";
		
		String licenseContentId = "";

		@LocaleResource(value="JFileSystemChanger.descriptor.uriTemplate",tooltip="JFileSystemChanger.descriptor.uriTemplate.tooltip")
		@Format("r")
		@Action(actionString="copyPasteUri",resource=@LocaleResource(tooltip="JFileSystemChanger.descriptor.uriTemplate.copyPaste.tooltip",value="Advanced"))
		String uriTemplate = "";
		
		@Override
		public RefreshMode onField(FileSystemDescription inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
			return RefreshMode.DEFAULT;
		}
		
		@Override
		public LoggerFacade getLogger() {
			return PureLibSettings.CURRENT_LOGGER;
		}
	}
}
