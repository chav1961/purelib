package chav1961.purelib.ui.swing;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JFormattedTextFieldWithMeta extends JFormattedTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 	serialVersionUID = -7990739033479280548L;
	
	private static final Class<?>[]		VALID_CLASSES = {String.class};
	
	private final ContentNodeMetadata	metadata;
	private final MaskFormatter			formatter;
	private String						currentValue, newValue;
	private boolean						invalid = false;
	
	public JFormattedTextFieldWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws LocalizationException, SyntaxException {
		super();
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else if (!InternalUtils.checkClassTypes(metadata.getType(),VALID_CLASSES)) {
			throw new IllegalArgumentException("Invalid node type for the given control. Only "+Arrays.toString(VALID_CLASSES)+" are available");
		}
		else if (metadata.getFormatAssociated() == null) {
			throw new IllegalArgumentException("Metadata doesn't contain field format!"); 
		}
		else {
			this.metadata = metadata;
			
			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated();
			
			try{this.formatter = new MaskFormatter(format.getFormatMask());
				setFormatterFactory(new DefaultFormatterFactory(formatter));
			} catch (ParseException e) {
				throw new SyntaxException(0,0,"Illegal format mask ["+format.getFormatMask()+"]: "+e.getLocalizedMessage());
			}
			setFocusable(true);
			enableEvents(AWTEvent.FOCUS_EVENT_MASK|AWTEvent.COMPONENT_EVENT_MASK);
			addComponentListener(new ComponentListener() {
				@Override public void componentResized(ComponentEvent e) {}
				@Override public void componentMoved(ComponentEvent e) {}
				@Override public void componentHidden(ComponentEvent e) {}
				
				@Override
				public void componentShown(ComponentEvent e) {
					try{monitor.process(MonitorEvent.Loading,metadata,JFormattedTextFieldWithMeta.this);
						currentValue = getText();
					} catch (ContentException exc) {
					}					
				}				
			});
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					System.err.println("LOST=====");
					try{commitEdit();
						if (!getText().equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JFormattedTextFieldWithMeta.this);
							currentValue = getText();
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JFormattedTextFieldWithMeta.this);
					} catch (ContentException | ParseException exc) {
						exc.printStackTrace();
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					System.err.println("Gained=====");
					currentValue = getText();
					if (format.needSelectOnFocus()) {
						selectAll();
					}
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JFormattedTextFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
			});
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try{monitor.process(MonitorEvent.Action,metadata,JFormattedTextFieldWithMeta.this,e.getActionCommand());
					} catch (ContentException exc) {
					}
				}
			});
			getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"rollback-value");
			getActionMap().put("rollback-value", new AbstractAction(){
				private static final long serialVersionUID = -6372550433958089237L;

				@Override
				public void actionPerformed(ActionEvent e) {
					try{if (monitor.process(MonitorEvent.Rollback,metadata,JFormattedTextFieldWithMeta.this)) {
							assignValueToComponent(currentValue);
						}
					} catch (ContentException exc) {
					}
				}
			});
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JFormattedTextFieldWithMeta.this);
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
			}
			if (format.getLength() != 0) {
				setColumns(format.getLength());
			}
			
			setName(name);
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
		return currentValue;
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return getText();
	}

	@Override
	public void assignValueToComponent(final Object value) {
		if (value == null) {
			throw new NullPointerException("Value to assign can't be null");
		}
		else {
			setText(value.toString());
		}
	}

	@Override
	public Class<?> getValueType() {
		return String.class;
	}

	@Override
	public String standardValidation(final String value) {
		if (value == null) {
			return "Null value can't ne assigned to string";
		}
		else {
			try{formatter.stringToValue(value);
				return null;
			} catch (ParseException e) {
				return e.getLocalizedMessage();
			}
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
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		} catch (IOException e) {
			throw new LocalizationException(e);
		}
	}
}
