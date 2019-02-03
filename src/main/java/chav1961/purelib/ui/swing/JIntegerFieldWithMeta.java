package chav1961.purelib.ui.swing;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.NumberFormatter;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JIntegerFieldWithMeta extends JFormattedTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 	serialVersionUID = -7990739033479280548L;
	
	private final ContentNodeMetadata	metadata;
	private final FieldFormat			format;
	private Object						currentValue;
	
	public JIntegerFieldWithMeta(final ContentNodeMetadata metadata, final FieldFormat format, final JComponentMonitor monitor) throws LocalizationException {
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
			this.format = format;
			
			if (metadata.getFormatAssociated().getFormatMask() != null) {
				setFormatter(new NumberFormatter(new DecimalFormat(metadata.getFormatAssociated().getFormatMask())));
			}
			else {
				final StringBuilder	sb = new StringBuilder("0");
				final int 			len = format.getLength() == 0 ? 15 : format.getLength();
				
				for (int index = 1, maxIndex = len; index < maxIndex; index++) {
					sb.insert(0,"#");
				}
				setFormatter(new NumberFormatter(new DecimalFormat(sb.toString()+";-"+sb.toString(),new DecimalFormatSymbols())));
				setColumns(len+1);
			}
			
			setHorizontalAlignment(JTextField.RIGHT);
			setFont(new Font(getFont().getFontName(),Font.BOLD,getFont().getSize()));
			setAutoscrolls(false);
			
			addComponentListener(new ComponentListener() {
				@Override public void componentResized(ComponentEvent e) {}
				@Override public void componentMoved(ComponentEvent e) {}
				@Override public void componentHidden(ComponentEvent e) {}
				
				@Override
				public void componentShown(ComponentEvent e) {
					try{monitor.process(MonitorEvent.Loading,metadata,JIntegerFieldWithMeta.this);
						currentValue = getText();
					} catch (ContentException exc) {
					}					
				}				
			});
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (!getText().equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JIntegerFieldWithMeta.this);
							currentValue = getText();
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JIntegerFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					currentValue = getText();
					if (format.needSelectOnFocus()) {
						selectAll();
					}
					if (getDocument().getLength() > 0) {
						setCaretPosition(getDocument().getLength()-1);
					}
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JIntegerFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
			});
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try{monitor.process(MonitorEvent.Action,metadata,JIntegerFieldWithMeta.this,e.getActionCommand());
					} catch (ContentException exc) {
					}
				}
			});
			getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"rollback-value");
			getActionMap().put("rollback-value", new AbstractAction(){
				private static final long serialVersionUID = -6372550433958089237L;

				@Override
				public void actionPerformed(ActionEvent e) {
					try{if (monitor.process(MonitorEvent.Rollback,metadata,JIntegerFieldWithMeta.this)) {
							assignValueToComponent(currentValue);
						}
					} catch (ContentException exc) {
					}
				}
			});
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{final boolean	validated = monitor.process(MonitorEvent.Validation,metadata,JIntegerFieldWithMeta.this);

						if (getValue() instanceof BigInteger) {
							setFieldColor(((BigInteger)getValue()).signum());
						}
						else if (getValue() instanceof Byte) {
							setFieldColor((int)Math.signum(((Byte)getValue()).longValue()));
						}
						else if (getValue() instanceof Short) {
							setFieldColor((int)Math.signum(((Short)getValue()).longValue()));
						}
						else if (getValue() instanceof Integer) {
							setFieldColor((int)Math.signum(((Integer)getValue()).longValue()));
						}
						else if (getValue() instanceof Long) {
							setFieldColor((int)Math.signum(((Long)getValue()).longValue()));
						}
						return validated;
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
		return getText();
	}

	@Override
	public void assignValueToComponent(final Object value) {
		if (value == null) {
			throw new NullPointerException("Value to assign can't be null");
		}
		else {
			setValue(value);
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
	
	private void setFieldColor(final int signum) {
		if (format.isHighlighted(signum)) {
			if (signum < 0) {
				setForeground(SwingUtils.NEGATIVEMARK_FOREGROUND);
			}
			else if (signum > 0) {
				setForeground(SwingUtils.POSITIVEMARK_FOREGROUND);
			}
			else {
				setForeground(SwingUtils.ZEROMARK_FOREGROUND);
			}
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		} catch (IOException e) {
			throw new LocalizationException(e);
		}
	}
}
