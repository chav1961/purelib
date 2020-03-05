package chav1961.purelib.ui.swing;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.text.DateFormatter;
import javax.swing.text.MaskFormatter;

import chav1961.purelib.basic.URIUtils;
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
import chav1961.purelib.ui.swing.useful.ComponentKeepedBorder;
import chav1961.purelib.ui.swing.useful.JDateSelectionDialog;

public class JDateFieldWithMeta extends JFormattedTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 	serialVersionUID = -7990739033479280548L;
	
	public static final String 			CHOOSER_NAME = "chooser";
	
	private static final int			DEFAULT_COLUMNS = 10;
	
	private final ContentNodeMetadata	metadata;
	private final BasicArrowButton		callSelect = new BasicArrowButton(BasicArrowButton.SOUTH);
	private	Popup 						window;
	private Date						currentValue = new Date(System.currentTimeMillis());
	
	public JDateFieldWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws LocalizationException {
		super();
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else {
			this.metadata = metadata;

			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated();
			
			if (format != null && format.getFormatMask() != null) {
				try{setFormatter(new MaskFormatter(format.getFormatMask()));
				} catch (ParseException e) {
				}
			}
			else {
				setFormatter(new DateFormatter(DateFormat.getDateInstance(DateFormat.SHORT)));
			}
			setValue(this.currentValue);
			addComponentListener(new ComponentListener() {
				@Override public void componentResized(ComponentEvent e) {}
				@Override public void componentMoved(ComponentEvent e) {}
				@Override public void componentHidden(ComponentEvent e) {}
				
				@Override
				public void componentShown(ComponentEvent e) {
					try{monitor.process(MonitorEvent.Loading,metadata,JDateFieldWithMeta.this);
						currentValue = (Date) getValue();
					} catch (ContentException exc) {
					}					
				}				
			});
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					if (window != null) {
						window.hide();
						window = null;
					}
					try{if (!getValue().equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JDateFieldWithMeta.this);
							currentValue = (Date) getValue();
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JDateFieldWithMeta.this);
					} catch (ContentException exc) {
					}
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					currentValue = (Date) getValue();
					if (format != null && format.needSelectOnFocus()) {
						selectAll();
					}
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JDateFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
			});
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try{monitor.process(MonitorEvent.Action,metadata,JDateFieldWithMeta.this,e.getActionCommand());
					} catch (ContentException exc) {
					}
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JDateFieldWithMeta.this)) {
					assignValueToComponent(currentValue);
				}
				} catch (ContentException exc) {
				}
			},"rollback-value");
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				callSelect.doClick();
			},"show-dropdown");
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JDateFieldWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});

			if (format != null) {
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
				setColumns(format.getLength() == 0 ? DEFAULT_COLUMNS : format.getLength());
			}
			else {
				setBackground(SwingUtils.OPTIONAL_BACKGROUND);
				setAlignmentX(JTextField.RIGHT_ALIGNMENT);
				setColumns(DEFAULT_COLUMNS);
			}
			callSelect.addActionListener((e)->{selectDate();});
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
	
	private void fillLocalizedStrings() throws LocalizationException {
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		} catch (IOException e) {
			throw new LocalizationException(e);
		}
	}

	private void selectDate() {
		if (window != null) {
			window.hide();
			window = null;
		}
		
		try{final Localizer				localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
			final JDateSelectionDialog	dsd = new JDateSelectionDialog(localizer
											,new Date(System.currentTimeMillis())
											,(newDate,needExit)->{
												System.err.println("New date="+newDate+", exit="+needExit);
												assignValueToComponent(newDate);
												if (needExit) {
													window.hide();
													window = null;
													JDateFieldWithMeta.this.requestFocus();
												}												
											}
											);
			final Point					callSelectLocation = callSelect.getLocationOnScreen();
			
			window = PopupFactory.getSharedInstance().getPopup(this,dsd,callSelectLocation.x+callSelect.getWidth()-dsd.getPreferredSize().width,callSelectLocation.y+callSelect.getHeight());
			window.show();
		} catch (IOException | LocalizationException  e) {
//			throw new LocalizationException(e);
		}
	}

}
