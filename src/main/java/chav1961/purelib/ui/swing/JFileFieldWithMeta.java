package chav1961.purelib.ui.swing;

import java.awt.Dialog;
import java.awt.HeadlessException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicArrowButton;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.ComponentKeepedBorder;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;

public class JFileFieldWithMeta extends JTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 			serialVersionUID = 8167088888478756141L;

	public static final String 			CHOOSER_NAME = "chooser";
	
	private static final Class<?>[]		VALID_CLASSES = {File.class, FileSystemInterface.class};
	
	private final ContentNodeMetadata	metadata;
	private final BasicArrowButton		callSelect = new BasicArrowButton(BasicArrowButton.SOUTH);
	private final Class<?>				contentClass;
	private Object						currentValue, newValue;
	private boolean						invalid = false;	
	
	public JFileFieldWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws LocalizationException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else if (!InternalUtils.checkClassTypes(metadata.getType(),VALID_CLASSES)) {
			throw new IllegalArgumentException("Invalid node type for the given control. Only "+Arrays.toString(VALID_CLASSES)+" are available");
		}
		else {
			this.metadata = metadata;
			this.contentClass = metadata.getType();
			
			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated();

			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (newValue != currentValue && newValue != null && !newValue.equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JFileFieldWithMeta.this);
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JFileFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					if (format != null && format.needSelectOnFocus()) {
						selectAll();
					}
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JFileFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JFileFieldWithMeta.this)) {
						assignValueToComponent(currentValue);
					}
				} catch (ContentException exc) {
				} finally {
					JFileFieldWithMeta.this.requestFocus();
				}
			},"rollback-value");
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_DROPDOWN,(e)->{
				callSelect.doClick();
			},"show-dropdown");
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JFileFieldWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});
			if (format != null) {
				if (format.isMandatory()) {
					setBackground(SwingUtils.MANDATORY_BACKGROUND);
					setForeground(SwingUtils.MANDATORY_FOREGROUND);
				}
				else {
					setBackground(SwingUtils.OPTIONAL_BACKGROUND);
					setForeground(SwingUtils.OPTIONAL_FOREGROUND);
				}
				switch (format.getAlignment()) {
					case CenterAlignment: setAlignmentX(JTextField.CENTER_ALIGNMENT); break;
					case LeftAlignment	: setAlignmentX(JTextField.LEFT_ALIGNMENT); break;
					case RightAlignment	: setAlignmentX(JTextField.RIGHT_ALIGNMENT); break;
					default: break;
				}
				if (format.isReadOnly(false)) {
					setEditable(false);
					callSelect.setEnabled(false);
				}
			}
			else {
				setBackground(SwingUtils.OPTIONAL_BACKGROUND);
				setForeground(SwingUtils.OPTIONAL_FOREGROUND);
				setAlignmentX(JTextField.LEFT_ALIGNMENT);
			}
			
			callSelect.addActionListener((e)->{selectFile();});
			new ComponentKeepedBorder(0,callSelect).install(this);
			
			setName(name);
			callSelect.setName(name+'/'+CHOOSER_NAME);
			fillLocalizedStrings();
		}		
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}
	
	@Override
	public String getRawDataFromComponent() {
		try{return currentValue == null ? null : ((currentValue instanceof File) ? ((File)currentValue).getAbsolutePath() : ((FileSystemInterface)currentValue).getPath());
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		try{return newValue == null ? null : ((newValue instanceof File) ? ((File)newValue).getAbsolutePath() : ((FileSystemInterface)newValue).getPath());
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void assignValueToComponent(final Object value) {
		try{if ((value instanceof File) && (standardValidation(((File)value).getAbsolutePath())) == null) {
				newValue = value; 
				setText(((File)value).getName());
				setToolTipText(((File)value).getAbsolutePath());
			}
			else if ((value instanceof FileSystemInterface) && (standardValidation(((FileSystemInterface)value).getPath())) == null) {
				setText(((FileSystemInterface)value).getName());
				setToolTipText(((FileSystemInterface)value).getPath());
				newValue = value; 
			}
			else if ((value instanceof String) && (standardValidation((String)value) == null)) {
				setText(value.toString());
				setToolTipText(value.toString());
				newValue = value; 
			}
			else {
				throw new NullPointerException("Value to assign can't be null and must be File, FileSystemInterface or String type only");
			}
		} catch (IOException e) {
			setText("?????");
		}
	}

	@Override
	public Class<?> getValueType() {
		return contentClass;
	}

	@Override
	public String standardValidation(final String value) {
		if (value == null || value.isEmpty()) {
			return "Null or empty value can't be assigned to file";
		}
		else if (value.startsWith(FileSystemInterface.FILESYSTEM_URI_SCHEME+':')) {
			try{final URI	uri = URI.create(value);
			
				for (FileSystemInterfaceDescriptor item : FileSystemFactory.getAvailableFileSystems()) {
					if (item.getInstance().canServe(uri)) {
						return null;
					}
				}
				throw new IllegalArgumentException("Unknown subscheme or appropriative file system not found");
			} catch (IllegalArgumentException | IOException | EnvironmentException exc) {
				return "Invalid uri value ["+value+"]: "+exc.getLocalizedMessage();
			}
		}
		else if (value.startsWith("file:")) {
			try{final URI	uri = URI.create(value);

				new File(uri);
				return null;
			} catch (IllegalArgumentException exc) {
				return "Invalid uri value ["+value+"]: "+exc.getLocalizedMessage();
			}
		}
		else {
			new File(value);
			return null;
		}
	}

	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}
	
	protected File chooseFile(final Localizer localizer, final File initialFile) throws HeadlessException, LocalizationException {
		final JFileChooser	chooser = new JFileChooser();
		
		if (!getText().isEmpty()) {
			final File		currentPath = new File(getText());
			
			if (currentPath.exists()) {
				if (currentPath.isFile()) {
					chooser.setCurrentDirectory(currentPath.getParentFile());
					chooser.setSelectedFile(currentPath);
				}
				else {
					chooser.setCurrentDirectory(currentPath);
				}
			}
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				return chooser.getSelectedFile();
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	protected FileSystemInterface chooseFileSystem(final Localizer localizer, final FileSystemInterface initialFS) throws HeadlessException, LocalizationException {
		try{for(String item : JFileSelectionDialog.select((Dialog)null, localizer, (FileSystemInterface)getValueFromComponent(), JFileSelectionDialog.OPTIONS_FOR_OPEN | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE)) {
				return ((FileSystemInterface)getValueFromComponent()).open(item);
			}
		} catch (IOException | LocalizationException e) {
		} 
		return null;
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
		setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
	}

	private void selectFile() {
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated());
		
			if (getValueType().isAssignableFrom(File.class)) {
				final File	result = chooseFile(localizer,(File)currentValue);
				
				if (result != null) {
					assignValueToComponent(result);
				}
			}
			else {
				final FileSystemInterface	result = chooseFileSystem(localizer,(FileSystemInterface)currentValue);
				
				if (result != null) {
					assignValueToComponent(result);
				}
			}
		} catch (HeadlessException | LocalizationException e) {
			e.printStackTrace();
		} finally {
			requestFocus();
		}
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
		}					
	}
}