package chav1961.purelib.ui.swing;

import java.awt.Dialog;
import java.awt.HeadlessException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.FileKeeper;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.inner.InternalConstants;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.ComponentKeepedBorder;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;

public class JFileFieldWithMeta extends JTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long 			serialVersionUID = 8167088888478756141L;

	public static final String 			CHOOSER_NAME = "chooser";
	
	private static final Class<?>[]		VALID_CLASSES = {File.class, FileSystemInterface.class, FileKeeper.class};
	
	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final JButton				callSelect = new JButton(InternalConstants.ICON_FOLDER);
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
			throw new IllegalArgumentException("Invalid node type ["+metadata.getType().getCanonicalName()+"] for the given control. Only "+Arrays.toString(VALID_CLASSES)+" are available");
		}
		else {
			this.metadata = metadata;
			this.contentClass = metadata.getType();
			
			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());

			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (newValue != currentValue && newValue != null && !newValue.equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JFileFieldWithMeta.this);
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JFileFieldWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JFileFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					if (format != null && format.needSelectOnFocus()) {
						selectAll();
					}
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JFileFieldWithMeta.this);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JFileFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JFileFieldWithMeta.this)) {
						assignValueToComponent(currentValue);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JFileFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				} finally {
					JFileFieldWithMeta.this.requestFocus();
				}
			}, SwingUtils.ACTION_ROLLBACK);
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
				if (InternalUtils.isContentMandatory(metadata)) {
					InternalUtils.prepareMandatoryColor(this);
				}
				else {
					InternalUtils.prepareOptionalColor(this);
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
				setBackground(PureLibSettings.defaultColorScheme().OPTIONAL_BACKGROUND);
				setForeground(PureLibSettings.defaultColorScheme().OPTIONAL_FOREGROUND);
				setAlignmentX(JTextField.LEFT_ALIGNMENT);
			}
			
			callSelect.addActionListener((e)->{selectFile();});
			new ComponentKeepedBorder(0,callSelect).install(this);
			
			setName(name);
			callSelect.setName(name+'/'+CHOOSER_NAME);
			callSelect.setFocusable(false);
			InternalUtils.registerAdvancedTooptip(this);
			setTransferHandler(new FileTransferHandler(this));
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
		try{if (currentValue == null) {
				return null;
			}
			else if (currentValue instanceof File) {
				return ((File)currentValue).getAbsolutePath();
			}
			else if (currentValue instanceof FileSystemInterface) {
				return ((FileSystemInterface)currentValue).getPath();
			}
			else {
				return ((FileKeeper)currentValue).getFileURI();
			}
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
		final String	currentText = getText();
			
		if (getValueType().isAssignableFrom(File.class)) {
			return newValue = new File(currentText);
		}
		else if (getValueType().isAssignableFrom(FileKeeper.class)) {
			return newValue = new FileKeeper(currentText);
		}
		else if (getValueType().isAssignableFrom(FileSystemInterface.class)) {
			throw new UnsupportedOperationException();
		}
		else {
			return null;
		}
	}

	@Override
	public void assignValueToComponent(final Object value) {
		try{if (value instanceof File) {
				newValue = value; 
				setText(((File)value).getAbsolutePath());
				setToolTipText(((File)value).getAbsolutePath());
			}
			else if (value instanceof FileSystemInterface) {
				setText(((FileSystemInterface)value).getPath());
				setToolTipText(((FileSystemInterface)value).getPath());
				newValue = value; 
			}
			else if (value instanceof FileKeeper) {
				setText(value.toString());
				setToolTipText(value.toString());
				newValue = value; 
			}
			else if ((value instanceof String)) {
				final String	val = value.toString();
				
				if (val.isEmpty()) {
					throw new IllegalArgumentException("Null or empty value can't be assigned to file");
				}
				else if (val.startsWith(FileSystemInterface.FILESYSTEM_URI_SCHEME+':')) {
					try{final URI	uri = URI.create(val);
						boolean		served = false;
					
						for (FileSystemInterfaceDescriptor item : FileSystemFactory.getAvailableFileSystems()) {
							if (item.getInstance().canServe(uri)) {
								served = true;
								break;
							}
						}
						if (!served) {
							throw new IllegalArgumentException("Unknown subscheme ["+uri+"] or appropriative file system not found");
						}
					} catch (IllegalArgumentException | IOException | EnvironmentException exc) {
						throw new IllegalArgumentException("Invalid uri value ["+value+"]: "+exc.getLocalizedMessage());
					}
				}
				else if (val.startsWith("file:")) {
					try{new File(URI.create(val));
					} catch (IllegalArgumentException exc) {
						throw new IllegalArgumentException("Invalid uri value ["+value+"]: "+exc.getLocalizedMessage());
					}
				}
				
				setText(val);
				setToolTipText(val);
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
	public String standardValidation(final Object val) {
		if (val == null) {
			if (InternalUtils.checkNullAvailable(getNodeMetadata())) {
				return null;
			}
			else {
				return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_NULL_VALUE);
			}
		}
		else {
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
	
	@Override
	public void addBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		repo.addBooleanPropChangeListener(listener);
	}

	@Override
	public void removeBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		repo.removeBooleanPropChangeListener(listener);
	}
	
	@Override
	public void setVisible(final boolean aFlag) {
		final boolean old = isVisible();
		
		super.setVisible(aFlag);
		if (repo != null && aFlag != old) {
			repo.fireBooleanPropChange(this, EventChangeType.VISIBILE, aFlag);
		}
	}
	
	@Override
	public boolean isEnabled() {
		if (getParent() != null) {
			return super.isEnabled() && getParent().isEnabled();
		}
		else {
			return super.isEnabled();
		}
	}
	
	@Override
	public void setEnabled(boolean b) {
		final boolean old = isEnabled();
		
		super.setEnabled(b);
		if (repo != null && b != old) {
			repo.fireBooleanPropChange(this, EventChangeType.ENABLED, b);
		}
	}
	
	@Override
	public void setEditable(boolean b) {
		final boolean old = isEditable();
		
		super.setEditable(b);
		if (repo != null && b != old) {
			repo.fireBooleanPropChange(this, EventChangeType.MODIFIABLE, b);
		}
	}

	protected File chooseFile(final Localizer localizer, final File initialFile) throws HeadlessException, LocalizationException {
		return InternalUtils.chooseFile(this, localizer, initialFile);
	}

	protected FileSystemInterface chooseFileSystem(final Localizer localizer, final FileSystemInterface initialFile) throws HeadlessException, LocalizationException {
		return InternalUtils.chooseFileSystem(this, localizer, initialFile);
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
		if (getNodeMetadata().getTooltipId() != null && !getNodeMetadata().getTooltipId().trim().isEmpty()) {
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		}
	}

	private void selectFile() {
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated());
		
			if (getValueType().isAssignableFrom(File.class)) {
				final File	result = chooseFile(localizer,(File)currentValue);
				
				if (result != null) {
					assignValueToComponent(result);
					getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(true);
				}
			}
			else if (getValueType().isAssignableFrom(FileKeeper.class)) {
				final File	result = chooseFile(localizer,((FileKeeper)currentValue).toFile());
				
				if (result != null) {
					assignValueToComponent(result);
					getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(true);
				}
			}
			else {
				final FileSystemInterface	result = InternalUtils.chooseFileSystem(this, localizer, (FileSystemInterface)currentValue);
				
				if (result != null) {
					assignValueToComponent(result);
					getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(true);
				}
			}
		} catch (HeadlessException exc) {
			SwingUtils.getNearestLogger(JFileFieldWithMeta.this).message(Severity.error, exc, exc.getLocalizedMessage());
		} finally {
			requestFocus();
		}
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(JFileFieldWithMeta.this).message(Severity.error, exc, exc.getLocalizedMessage());
		}					
	}
	
	private static class FileTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 608229074222584792L;

		private final JFileFieldWithMeta	owner;
		
		private FileTransferHandler(final JFileFieldWithMeta owner) {
			super(null);
			this.owner = owner;
		}
		
		@Override
		public boolean canImport(final TransferSupport support) {
			if (support.isDrop() && (support.getSourceDropActions() & COPY) == COPY && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		        support.setDropAction(COPY);
		        return true;
			}
			else {
				return false;
			}
		}
		
		@Override
		public boolean importData(final TransferSupport support) {
			try{final List<File>	content = (List<File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			
				owner.assignValueToComponent(content.get(0));
				return true;
			} catch (UnsupportedFlavorException | IOException e) {
				SwingUtils.getNearestLogger(owner).message(Severity.error, e, "Error pasting files");
				return false;
			}
		}
	}
}