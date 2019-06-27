package chav1961.purelib.ui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JEnumFieldWithMeta extends JComboBox<Enum<?>> implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 	serialVersionUID = -7990739033479280548L;
	
	private final ContentNodeMetadata	metadata;
	private final Class<Enum<?>>		clazz;
	private Enum<?>						currentValue;
	
	public JEnumFieldWithMeta(final ContentNodeMetadata metadata, final FieldFormat format, final JComponentMonitor monitor) throws LocalizationException {
		super();
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
			this.clazz = (Class<Enum<?>>) metadata.getType();
			
			for (Enum<?> item : clazz.getEnumConstants()) {
				addItem(item);
			}
			addComponentListener(new ComponentListener() {
				@Override public void componentResized(ComponentEvent e) {}
				@Override public void componentMoved(ComponentEvent e) {}
				@Override public void componentHidden(ComponentEvent e) {}
				
				@Override
				public void componentShown(ComponentEvent e) {
					try{monitor.process(MonitorEvent.Loading,metadata,JEnumFieldWithMeta.this);
						if (getSelectedIndex() < 0) {
							setSelectedIndex(0);
						}
						currentValue = (Enum<?>) getSelectedItem();
					} catch (ContentException exc) {
					}					
				}				
			});
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (getSelectedItem() != null && !getSelectedItem().equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JEnumFieldWithMeta.this);
							currentValue = (Enum<?>) getSelectedItem();
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JEnumFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					currentValue = (Enum<?>) getSelectedItem();
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JEnumFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
			});
			getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"rollback-value");
			getActionMap().put("rollback-value", new AbstractAction(){
				private static final long serialVersionUID = -6372550433958089237L;

				@Override
				public void actionPerformed(ActionEvent e) {
					try{if (monitor.process(MonitorEvent.Rollback,metadata,JEnumFieldWithMeta.this)) {
							assignValueToComponent(currentValue);
						}
					} catch (ContentException exc) {
					}
				}
			});
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JEnumFieldWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});
			setRenderer(new ListCellRenderer<Enum<?>>() {
				@Override
				public Component getListCellRendererComponent(final JList<? extends Enum<?>> list, final Enum<?> value, final int index, final boolean isSelected, final boolean cellHasFocus) {
					if (value == null) {
						return new JLabel("unselected");
					}
					else {
						final JLabel	label = new JLabel();

						label.setOpaque(true);
						label.setBackground(isSelected ? SwingUtils.MANDATORY_SELECTION_BACKGROUND : (format.isMandatory() ? SwingUtils.MANDATORY_BACKGROUND : SwingUtils.OPTIONAL_BACKGROUND));
						label.setForeground(isSelected ? SwingUtils.MANDATORY_SELECTION_FOREGROUND : (format.isMandatory() ? SwingUtils.MANDATORY_FOREGROUND : SwingUtils.OPTIONAL_FOREGROUND));
						if (cellHasFocus) {
							label.setBorder(new LineBorder(SwingUtils.MANDATORY_SELECTION_FOREGROUND));
						}
						try{if (value.getClass().getField(value.name()).isAnnotationPresent(LocaleResource.class)) {
								final LocaleResource	res = value.getClass().getField(value.name()).getAnnotation(LocaleResource.class);
								final Localizer			localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
							
								label.setText(localizer.getValue(res.value()));
								label.setToolTipText(localizer.getValue(res.tooltip()));
							}
							else {
								label.setText(value.name());
							}
						} catch (NoSuchFieldException | LocalizationException | IOException  e) {
							label.setText(value.name());
						}
						return label;
					}
				}
			});
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try{monitor.process(MonitorEvent.Action,metadata,JEnumFieldWithMeta.this,e.getActionCommand());
					} catch (ContentException exc) {
					}
				}
			});

			setBackground(format.isMandatory() ? SwingUtils.MANDATORY_BACKGROUND : SwingUtils.OPTIONAL_BACKGROUND);
			setForeground(format.isMandatory() ? SwingUtils.MANDATORY_FOREGROUND : SwingUtils.OPTIONAL_FOREGROUND);
			switch (format.getAlignment()) {
				case CenterAlignment: setAlignmentX(JTextField.CENTER_ALIGNMENT); break;
				case LeftAlignment	: setAlignmentX(JTextField.LEFT_ALIGNMENT); break;
				case RightAlignment	: setAlignmentX(JTextField.RIGHT_ALIGNMENT); break;
				default: break;
			}
			if (format.isReadOnly(false)) {
				setEditable(false);
			}
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
		return currentValue == null ? null : currentValue.toString();
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return getSelectedItem();
	}

	@Override
	public void assignValueToComponent(final Object value) {
		if (value == null) {
			throw new NullPointerException("Value to assign can't be null");
		}
		else if (!clazz.isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException("Value class to assign must be ["+clazz.getCanonicalName()+"], not ["+value.getClass().getCanonicalName()+"]");
		}
		else {
			setSelectedItem(value);
		}
	}

	@Override
	public Class<?> getValueType() {
		return String.class;
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
}

