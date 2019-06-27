package chav1961.purelib.ui.swing;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
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
	
	private final ContentNodeMetadata	metadata;
	private final JButton				callSelect = new JButton("...");
	private final Class<?>				contentClass;
	private Object						currentValue, newValue;
	
	public JFileFieldWithMeta(final ContentNodeMetadata metadata, final FieldFormat format, final JComponentMonitor monitor) throws LocalizationException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (format == null) {
			throw new NullPointerException("Format can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else {
			this.metadata = metadata;
			this.contentClass = metadata.getType();
			
			addComponentListener(new ComponentListener() {
				@Override public void componentResized(ComponentEvent e) {}
				@Override public void componentMoved(ComponentEvent e) {}
				@Override public void componentHidden(ComponentEvent e) {}
				
				@Override
				public void componentShown(ComponentEvent e) {
					try{monitor.process(MonitorEvent.Loading,metadata,JFileFieldWithMeta.this);
						currentValue = getText();
					} catch (ContentException exc) {
					}					
				}				
			});
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (!getText().equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JFileFieldWithMeta.this);
							currentValue = getText();
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JFileFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					currentValue = getText();
					if (format.needSelectOnFocus()) {
						selectAll();
					}
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JFileFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
			});
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try{monitor.process(MonitorEvent.Action,metadata,JFileFieldWithMeta.this,e.getActionCommand());
					} catch (ContentException exc) {
					}
				}
			});
			getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"rollback-value");
			getActionMap().put("rollback-value", new AbstractAction(){
				private static final long serialVersionUID = -6372550433958089237L;

				@Override
				public void actionPerformed(ActionEvent e) {
					try{if (monitor.process(MonitorEvent.Rollback,metadata,JFileFieldWithMeta.this)) {
							assignValueToComponent(currentValue);
						}
					} catch (ContentException exc) {
					}
				}
			});
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JFileFieldWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});

			setBackground(format.isMandatory() ? SwingUtils.MANDATORY_BACKGROUND : SwingUtils.OPTIONAL_BACKGROUND);
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
			callSelect.addActionListener((e)->{selectFile();});
			new ComponentKeepedBorder(0,callSelect).install(this);
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
		if (value == null) {
			throw new NullPointerException("Value to assign can't be null");
		}
		else if (value instanceof File) {
			newValue = value; 
			setText(((File)value).getName());
		}
		else if (value instanceof FileSystemInterface) {
			setText(value.toString());
		}
	}

	@Override
	public Class<?> getValueType() {
		return contentClass;
	}

	@Override
	public String standardValidation(final String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInvalid(boolean invalid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInvalid() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		} catch (IOException e) {
			throw new LocalizationException(e);
		}
	}

	private void selectFile() {
		if (getValueType().isAssignableFrom(File.class)) {
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
					assignValueToComponent(chooser.getSelectedFile());
				}
			}
		}
		else {
			
			try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated());
			
				for(String item : JFileSelectionDialog.select((Dialog)null, localizer, (FileSystemInterface)getValueFromComponent(), JFileSelectionDialog.OPTIONS_FOR_OPEN | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE)) {
					((FileSystemInterface)getValueFromComponent()).open(item);
					assignValueToComponent(getValueFromComponent());
					break;
				}
			} catch (IOException | LocalizationException e) {
			} 
		}
	}
}