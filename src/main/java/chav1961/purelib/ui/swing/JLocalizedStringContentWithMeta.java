package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.LocalizedString;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.MutableLocalizedString;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JLocalizedStringContentWithMeta extends JTabbedPane implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long serialVersionUID = 1L;

	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final Localizer				localizer;
	private LocalizedString				currentValue = null, newValue = null;
	private boolean 					editable = false;
	private boolean 					invalid = false;
	
	public JLocalizedStringContentWithMeta (final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (monitor == null) {
			throw new NullPointerException("Component monitor can't be null");
		}
		else {
			this.metadata = metadata;
			this.localizer = localizer;

			int	tabIndex = -1;
			for (int index = 0; index < SupportedLanguages.values().length; index++) {
				final SupportedLanguages	item = SupportedLanguages.values()[index];
				
				if (localizer.currentLocale().getLocale().getLanguage().equals(item.name())) {
					tabIndex = index;
				}
				addTab(item.name(), null, new InnerTab(item.getLocale(), metadata.getFormatAssociated(), monitor));
			}
			if (tabIndex >= 0) {	// Current locale selection
				setSelectedIndex(tabIndex);
			}
			setName(metadata.getName());
			fillLocalizedStrings();
		}
	}

	@Override
	public void addBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null"); 
		}
		else {
			repo.addBooleanPropChangeListener(listener);
		}
	}

	@Override
	public void removeBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null"); 
		}
		else {
			repo.removeBooleanPropChangeListener(listener);
		}
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

	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(final boolean b) {
		final boolean old = isEditable();
		
		if (repo != null && b != old && (currentValue instanceof MutableLocalizedString)) {
			editable = b;
			repo.fireBooleanPropChange(this, EventChangeType.MODIFIABLE, b);
			repaint();
		}
	}

	@Override
	public String getRawDataFromComponent() {
		return newValue != null ? newValue.toString() : null;
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		if (newValue instanceof MutableLocalizedString) {
			for(int index = 0; index < getComponentCount(); index++) {
				final InnerTab	tab = (InnerTab)getComponent(index);
				
				((MutableLocalizedString) newValue).setValue(SupportedLanguages.values()[index].getLocale(), tab.field.getText());
			}
		}
		return newValue;
	}

	@Override
	public void assignValueToComponent(final Object value) throws ContentException {
		if (value == null) {
			throw new NullPointerException("Value to assign can't be null"); 
		}
		else {
			try {
				currentValue = (LocalizedString)value;
				newValue = (LocalizedString) currentValue.clone();
				for(int index = 0; index < getComponentCount(); index++) {
					final InnerTab	tab = (InnerTab)getComponent(index);
					
					tab.field.setText(newValue.getValue(SupportedLanguages.values()[index].getLocale()));
				}
				
				for (SupportedLanguages item : SupportedLanguages.values()) {
					((InnerTab)getComponentAt(item.ordinal())).getComponent().setText(newValue.getValue(item.getLocale()));
					((InnerTab)getComponentAt(item.ordinal())).getComponent().setEditable(value instanceof MutableLocalizedString);
				}
			} catch (CloneNotSupportedException e) {
				throw new ContentException(e); 
			}
		}
	}

	@Override
	public Class<?> getValueType() {
		return LocalizedString.class;
	}

	@Override
	public String standardValidation(final Object value) {
		return null;
	}

	@Override
	public void setInvalid(final boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = (LocalizedString) newValue.clone();
		} catch (ContentException | CloneNotSupportedException exc) {
			SwingUtils.getNearestLogger(this).message(Severity.error, exc,exc.getLocalizedMessage());
		}					
	}
	
	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
	}

	
	private class InnerTab extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private final Locale			lang;
		private final JTextComponent	field;
		
		private InnerTab(final Locale lang, final FieldFormat format, JComponentMonitor monitor) {
			super(new BorderLayout());
			
			this.lang = lang;
			if (format != null && format.getHeight() > 1) {
				field = new JTextArea(); 
				((JTextArea)field).setRows(format.getHeight());
				add(new JScrollPane(field), BorderLayout.CENTER);
			}
			else {
				field = new JTextField();
				add(field, BorderLayout.CENTER);
			}
			
			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			field.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (newValue != currentValue && newValue != null && !newValue.equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JLocalizedStringContentWithMeta.this);
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JLocalizedStringContentWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JLocalizedStringContentWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{
						monitor.process(MonitorEvent.FocusGained, metadata, JLocalizedStringContentWithMeta.this);
						field.getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JLocalizedStringContentWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
					SwingUtilities.invokeLater(()->{
						if (format.needSelectOnFocus()) {
							if (field instanceof JTextArea) {
								((JTextArea)field).selectAll();
							}
							else {
								((JTextField)field).selectAll();
							}
						}
					});
				}
			});
			SwingUtils.assignActionKey(field, WHEN_FOCUSED, SwingUtils.KS_EXIT, (e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JLocalizedStringContentWithMeta.this)) {
						assignValueToComponent(currentValue);
						field.getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JLocalizedStringContentWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				} finally {
					JLocalizedStringContentWithMeta.this.requestFocus();
				}
			}, SwingUtils.ACTION_ROLLBACK);
			field.setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{if (monitor.process(MonitorEvent.Validation,metadata,JLocalizedStringContentWithMeta.this)) {
							newValue = (LocalizedString)getChangedValueFromComponent();
							field.getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(true);
							return true;
						}
						else {
							return false;
						}
					} catch (ContentException e) {
						return false;
					}
				}
			});

			if (InternalUtils.isContentMandatory(metadata)) {
				InternalUtils.prepareMandatoryColor(field);
			}
			else {
				InternalUtils.prepareOptionalColor(field);
			}
			switch (format.getAlignment()) {
				case CenterAlignment: field.setAlignmentX(JTextField.CENTER_ALIGNMENT); break;
				case LeftAlignment	: field.setAlignmentX(JTextField.LEFT_ALIGNMENT); break;
				case RightAlignment	: field.setAlignmentX(JTextField.RIGHT_ALIGNMENT); break;
				default: break;
			}
			if (format.isOutput()) {
				field.setFocusable(false);
			}
			if (format.getLength() != 0) {
				if (field instanceof JTextArea) {
					((JTextArea)field).setColumns(format.getLength());
				}
				else {
					((JTextField)field).setColumns(format.getLength());
				}
			}
		}
		
		public JTextComponent getComponent() {
			return field;
		}
		
		private void refreshContent() {
			field.setText(currentValue.getValue(lang));
		}
	}
}
