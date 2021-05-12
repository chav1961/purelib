package chav1961.purelib.ui.swing;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
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
	
	private static final Class<?>[]		VALID_CLASSES = {Enum.class};
	
	private final ContentNodeMetadata	metadata;
	private final Class<Enum<?>>		clazz;
	private Enum<?>						currentValue;
	private boolean						invalid = false;
	
	public JEnumFieldWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws LocalizationException {
		super();
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
			this.clazz = (Class<Enum<?>>) metadata.getType();
			
			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
			
			for (Enum<?> item : clazz.getEnumConstants()) {
				addItem(item);
			}
			currentValue = clazz.getEnumConstants()[0];
			InternalUtils.addComponentListener(this,()->callLoad(monitor));
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
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JEnumFieldWithMeta.this)) {
						assignValueToComponent(currentValue);
					}
				} catch (ContentException exc) {
				}
			},"rollback-value");
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
						label.setBackground(isSelected ? PureLibSettings.defaultColorScheme().MANDATORY_SELECTION_BACKGROUND : (format.isMandatory() ? PureLibSettings.defaultColorScheme().MANDATORY_BACKGROUND : PureLibSettings.defaultColorScheme().OPTIONAL_BACKGROUND));
						label.setForeground(isSelected ? PureLibSettings.defaultColorScheme().MANDATORY_SELECTION_FOREGROUND : (format.isMandatory() ? PureLibSettings.defaultColorScheme().MANDATORY_FOREGROUND : PureLibSettings.defaultColorScheme().OPTIONAL_FOREGROUND));
						if (cellHasFocus) {
							label.setBorder(new LineBorder(PureLibSettings.defaultColorScheme().MANDATORY_SELECTION_FOREGROUND));
						}
						try{if (value.getClass().getField(value.name()).isAnnotationPresent(LocaleResource.class)) {
								final LocaleResource	res = value.getClass().getField(value.name()).getAnnotation(LocaleResource.class);
								final Localizer			localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
							
								label.setText(localizer.getValue(res.value()));
								label.setToolTipText(localizer.getValue(res.tooltip()));
							}
							else {
								label.setText(value.name());
								label.setToolTipText(value.name());
							}
						} catch (NoSuchFieldException | LocalizationException e) {
							label.setText(value.name());
						}
						return label;
					}
				}
			});
			
			addActionListener((e)->{
				if (getSelectedItem() != null && !getSelectedItem().equals(currentValue)) {
					try{if (monitor.process(MonitorEvent.Validation,metadata,JEnumFieldWithMeta.this)) {
							monitor.process(MonitorEvent.Saving,metadata,JEnumFieldWithMeta.this);
							currentValue = (Enum<?>) getSelectedItem();
						}
					} catch (ContentException exc) {
					}
				}
			});

			if (format != null) {
				if (format.isMandatory()) {
					setBackground(PureLibSettings.defaultColorScheme().MANDATORY_BACKGROUND);
					setForeground(PureLibSettings.defaultColorScheme().MANDATORY_FOREGROUND);
				}
				else {
					setBackground(PureLibSettings.defaultColorScheme().OPTIONAL_BACKGROUND);
					setForeground(PureLibSettings.defaultColorScheme().OPTIONAL_FOREGROUND);
				}
				switch (format.getAlignment()) {
					case CenterAlignment: setAlignmentX(JTextField.CENTER_ALIGNMENT); break;
					case LeftAlignment	: setAlignmentX(JTextField.LEFT_ALIGNMENT); break;
					case RightAlignment	: setAlignmentX(JTextField.RIGHT_ALIGNMENT); break;
					default: break;
				}
				if (format.isReadOnly(false)) {
					setEditable(false);
				}
			}
			else {
				setBackground(PureLibSettings.defaultColorScheme().OPTIONAL_BACKGROUND);
				setForeground(PureLibSettings.defaultColorScheme().OPTIONAL_FOREGROUND);
				setAlignmentX(JComboBox.LEFT_ALIGNMENT);
			}
			
			setName(name);			
			fillLocalizedStrings();
			InternalUtils.registerAdvancedTooptip(this);
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
		if (value instanceof String) {
			final String	test = value.toString().trim();
			
			for (Enum<?> item : clazz.getEnumConstants()) {
				if (test.equals(item.name())) {
					setSelectedItem(item);
					return;
				}
			}
			throw new IllegalArgumentException("Unknonw string value for enum ["+getValueType().getCanonicalName()+"]");
		}
		else if ((value instanceof Enum) && getValueType().isAssignableFrom(value.getClass())) {
			setSelectedItem((Enum<?>) value);
		}
		else {
			throw new IllegalArgumentException("Value can't be null and value class to assign must be ["+getValueType().getCanonicalName()+"] or String");
		}
	}

	@Override
	public Class<?> getValueType() {
		return clazz;
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
	
	private void fillLocalizedStrings() throws LocalizationException {
		final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
		if (getNodeMetadata().getTooltipId() != null && !getNodeMetadata().getTooltipId().trim().isEmpty()) {
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		}
		repaint();
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,JEnumFieldWithMeta.this);
			if (getSelectedIndex() < 0) {
				setSelectedIndex(0);
			}
			currentValue = (Enum<?>) getSelectedItem();
		} catch (ContentException exc) {
		}					
	}
}

