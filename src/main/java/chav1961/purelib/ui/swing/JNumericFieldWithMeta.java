package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JNumericFieldWithMeta extends JFormattedTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 			serialVersionUID = -7990739033479280548L;

	static final int[]					NAVIGATION_KEYS = {
											 KeyEvent.VK_BACK_SPACE			,KeyEvent.VK_TAB
											,KeyEvent.VK_CANCEL				,KeyEvent.VK_CLEAR
											,KeyEvent.VK_PAUSE				,KeyEvent.VK_CAPS_LOCK
											,KeyEvent.VK_ESCAPE				,KeyEvent.VK_PAGE_UP
											,KeyEvent.VK_PAGE_DOWN			,KeyEvent.VK_END
											,KeyEvent.VK_HOME				,KeyEvent.VK_LEFT
											,KeyEvent.VK_UP					,KeyEvent.VK_RIGHT
											,KeyEvent.VK_DOWN				,KeyEvent.VK_KP_UP
											,KeyEvent.VK_KP_DOWN			,KeyEvent.VK_KP_LEFT
											,KeyEvent.VK_KP_RIGHT			,KeyEvent.VK_INSERT
											,KeyEvent.VK_DELETE				,KeyEvent.VK_NUM_LOCK
											,KeyEvent.VK_SCROLL_LOCK		,KeyEvent.VK_CONTROL
											,KeyEvent.VK_SHIFT				,KeyEvent.VK_ALT_GRAPH
											,KeyEvent.VK_ALT				,KeyEvent.VK_META
											,KeyEvent.VK_CONTEXT_MENU
										};
	
	static {
		Arrays.sort(NAVIGATION_KEYS);
	}
	
	private final ContentNodeMetadata	metadata;
	private final FieldFormat			format;
	private String						charsSupported;
	private boolean						invalid = false, inFocusNow = false;
	private String						currentText;
	private Object						currentValue;
	
	public JNumericFieldWithMeta(final ContentNodeMetadata metadata, final FieldFormat format, final JComponentMonitor monitor) throws LocalizationException {
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
			this.currentValue = SwingUtils.getDefaultValue4Class(metadata.getType());

			if (metadata.getFormatAssociated().getFormatMask() != null) {
				final NumberFormatter	formatter = new NumberFormatter(new DecimalFormat(metadata.getFormatAssociated().getFormatMask())); 
				
				formatter.setAllowsInvalid(false);
				setFormatter(formatter);
			}
			else {
				final StringBuilder	sb = new StringBuilder("0");
				final int 			len = format.getLength() == 0 ? 15 : format.getLength();
				final int 			frac = format.getPrecision() == 0 ? 2 : format.getPrecision();
				
				for (int index = 1, maxIndex = len - frac -1; index < maxIndex; index++) {
					sb.insert(0,"#");
				}
				sb.append('.');
				for (int index = 0, maxIndex = frac; index < maxIndex; index++) {
					sb.append("#");
				}
				
				final NumberFormatter	formatter = new NumberFormatter(new DecimalFormat(sb.toString()+";-"+sb.toString(),new DecimalFormatSymbols())); 
				
				formatter.setAllowsInvalid(false);
				setFormatter(formatter);
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
					try{monitor.process(MonitorEvent.Loading,metadata,JNumericFieldWithMeta.this);
						currentValue = getValue();
						currentText = getText();
					} catch (ContentException exc) {
					}					
				}				
			});
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (!getText().equals(currentText)) {
							commitEdit();
							monitor.process(MonitorEvent.Saving,metadata,JNumericFieldWithMeta.this);
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JNumericFieldWithMeta.this);
						prepareContent(currentValue = getValue(),false);
						currentText = getText();
					} catch (ContentException | ParseException  exc) {
					}
					inFocusNow = false;
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					prepareContent(currentValue = getValue(),true);
					currentText = getText();
					try{monitor.process(MonitorEvent.FocusGained,metadata,JNumericFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
					inFocusNow = true;
				}
			});
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try{monitor.process(MonitorEvent.Action,metadata,JNumericFieldWithMeta.this,e.getActionCommand());
					} catch (ContentException exc) {
					}
				}
			});
			getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"rollback-value");
			getActionMap().put("rollback-value", new AbstractAction(){
				private static final long serialVersionUID = -6372550433958089237L;

				@Override
				public void actionPerformed(ActionEvent e) {
					try{if (monitor.process(MonitorEvent.Rollback,metadata,JNumericFieldWithMeta.this)) {
							setInvalid(false);
							assignValueToComponent(currentValue);
							currentText = getText();
						}
					} catch (ContentException exc) {
					}
				}
			});
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{if (monitor.process(MonitorEvent.Validation,metadata,JNumericFieldWithMeta.this)) {
							setInvalid(false);
							return true;
						}
						else {
							setInvalid(true);
							return false;
						}
					} catch (ContentException e) {
						return false;
					}
				}
			});

			setBackground(format.isMandatory() ? SwingUtils.MANDATORY_BACKGROUND : SwingUtils.OPTIONAL_BACKGROUND);
			setSelectionColor(format.isMandatory() ? SwingUtils.MANDATORY_SELECTION_BACKGROUND : SwingUtils.OPTIONAL_SELECTION_BACKGROUND);
			setSelectedTextColor(format.isMandatory() ? SwingUtils.MANDATORY_SELECTION_FOREGROUND : SwingUtils.OPTIONAL_SELECTION_FOREGROUND);
			setFieldColor(0);
			switch (format.getAlignment()) {
				case CenterAlignment: setHorizontalAlignment(JTextField.CENTER); break;
				case LeftAlignment	: setHorizontalAlignment(JTextField.LEFT); break;
				case RightAlignment	: setHorizontalAlignment(JTextField.RIGHT); break;
				default: break;
			}
			if (format.isReadOnly(false)) {
				setEditable(false);
			}
			fillLocalizedStrings();
			setValue(currentValue);
		}		
	}

	
	@Override
	protected void processKeyEvent(final KeyEvent e) {
		if (charsSupported.indexOf(e.getKeyChar()) >= 0 || Arrays.binarySearch(NAVIGATION_KEYS,e.getKeyCode()) >= 0) {
			super.processKeyEvent(e);
		}
		else {
			e.consume();
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
		return currentText == null ? null : currentText;
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return getValue();
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
		return getNodeMetadata().getType();
	}

	@Override
	public String standardValidation(final String value) {
		if (value == null || value.isEmpty()) {
			if (format.isMandatory()) {
				return "Value is mandatory and must be filled!";
			}
		}
		try{final Localizer 			localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated());
			final DecimalFormatSymbols	dfs = new DecimalFormatSymbols(localizer.currentLocale().getLocale());
			final String				test = value.replace(dfs.getDecimalSeparator(),'.').replace(dfs.getMinusSign(),'-');
			
			switch (Utils.defineClassType(getValueType())) {
				case Utils.CLASSTYPE_REFERENCE	:
					if (getValueType().isAssignableFrom(Float.class)) {
						try{Float.parseFloat(test);
							return null;
						} catch (NumberFormatException exc) {
							return "Invalid value ["+value+"]: "+exc.getLocalizedMessage();
						}
					}
					else if (getValueType().isAssignableFrom(Double.class)) {
						try{Double.parseDouble(test);
							return null;
						} catch (NumberFormatException exc) {
							return "Invalid value ["+value+"]: "+exc.getLocalizedMessage();
						}
					}
					else if (getValueType().isAssignableFrom(BigDecimal.class)) {
						try{new BigDecimal(test);
							return null;
						} catch (NumberFormatException exc) {
							return "Invalid value ["+value+"]: "+exc.getLocalizedMessage();
						}
					}
					else {
						return "Unsupported value class ["+getValueType().getCanonicalName()+"] in the field metadata";
					}
				case Utils.CLASSTYPE_FLOAT		:
					try{Float.parseFloat(test);
						return null;
					} catch (NumberFormatException exc) {
						return "Invalid value ["+value+"]: "+exc.getLocalizedMessage();
					}
				case Utils.CLASSTYPE_DOUBLE		:
					try{Double.parseDouble(test);
						return null;
					} catch (NumberFormatException exc) {
						return "Invalid value ["+value+"]: "+exc.getLocalizedMessage();
					}
				default : return "Unsupported value class ["+getValueType().getCanonicalName()+"] in the field metadata";
			}
		} catch (IOException e) {
			return e.getLocalizedMessage();
		}
	}

	@Override
	public void setValue(final Object value) {
		if (value == null) {
			throw new NullPointerException("Value to set can't be null");
		}
		else if (!getValueType().isAssignableFrom(value.getClass()) && !Utils.primitive2Wrapper(getValueType()).isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException("Uncompatible data types: value class ["+value.getClass().getCanonicalName()+"] can't be casted to field class ["+getValueType().getCanonicalName()+"]");
		}
		else {
			super.setValue(value);
			prepareContent(currentValue = value,inFocusNow);
			currentText = getText();
			invalid = false;
		}
	}
	
	@Override
	public void setInvalid(final boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}
	
	private void setFieldColor(final int signum) {
		if (isInvalid()) {
			SwingUtilities.invokeLater(()->{setForeground(Color.RED);});
		}
		else if (format.isHighlighted(signum)) {
			if (signum < 0) {
				SwingUtilities.invokeLater(()->{setForeground(SwingUtils.NEGATIVEMARK_FOREGROUND);});
			}
			else if (signum > 0) {
				SwingUtilities.invokeLater(()->{setForeground(SwingUtils.POSITIVEMARK_FOREGROUND);});
			}
			else {
				SwingUtilities.invokeLater(()->{setForeground(SwingUtils.ZEROMARK_FOREGROUND);});
			}
		}
		else {
			SwingUtilities.invokeLater(()->{setForeground(format.isMandatory() ? SwingUtils.MANDATORY_FOREGROUND : SwingUtils.OPTIONAL_FOREGROUND);});
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
			charsSupported = "0123456789"
								+DecimalFormatSymbols.getInstance(localizer.currentLocale().getLocale()).getDecimalSeparator()
								+DecimalFormatSymbols.getInstance(localizer.currentLocale().getLocale()).getMinusSign();
		} catch (IOException e) {
			throw new LocalizationException(e);
		}
	}
	
	private void prepareContent(final Object value, final boolean fullPreparation) {
		setFieldColor(SwingUtils.getSignum4Value(value));
		if (getDocument().getLength() > 0) {
			SwingUtilities.invokeLater(()->{setCaretPosition(getDocument().getLength());});
		}
		if (format.needSelectOnFocus()) {
			SwingUtilities.invokeLater(()->{selectAll();});
			selectAll();
		}
	}
}
