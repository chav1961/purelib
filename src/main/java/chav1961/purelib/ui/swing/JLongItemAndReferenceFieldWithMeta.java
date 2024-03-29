package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimerTask;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.ColorUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleTimerTask;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.inner.InternalConstants;
import chav1961.purelib.ui.interfaces.ItemAndReference;
import chav1961.purelib.ui.interfaces.LongItemAndReference;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.FunctionalDocumentListener;
import chav1961.purelib.ui.swing.interfaces.FunctionalMouseListener;
import chav1961.purelib.ui.swing.interfaces.FunctionalMouseListener.EventType;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.ComponentKeepedBorder;

public class JLongItemAndReferenceFieldWithMeta extends JTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long serialVersionUID = 5255076724778401819L;

	public static final String 			CHOOSER_NAME = "chooser";
	public static final String 			KEY_TITLE = "JLongItemAndReferenceFieldWithMeta.select.title";
	public static final String 			KEY_FILTER = "JLongItemAndReferenceFieldWithMeta.select.typefilter";
	public static final String 			KEY_NOT_DEFINED = "JLongItemAndReferenceFieldWithMeta.not.defined";
	public static final long			REFRESH_DELAY_MILLISECONDS = 300;

	private static final Class<?>[]		VALID_CLASSES = {ItemAndReference.class};
	
	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final Localizer				localizer;
	private final JButton				callSelect = new JButton(InternalConstants.ICON_TABLE);
	private final Class<?>				contentClass;
	private LongItemAndReference<?>		currentValue, newValue;
	private boolean						invalid = false;
	
	public JLongItemAndReferenceFieldWithMeta(final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
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
			this.localizer = localizer;
			
			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());

			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (newValue != currentValue && newValue != null && !newValue.equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JLongItemAndReferenceFieldWithMeta.this);
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JLongItemAndReferenceFieldWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JLongItemAndReferenceFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					if (format != null && format.needSelectOnFocus()) {
						selectAll();
					}
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JLongItemAndReferenceFieldWithMeta.this);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JLongItemAndReferenceFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
			});
			SwingUtils.assignActionKey(this, WHEN_FOCUSED, SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JLongItemAndReferenceFieldWithMeta.this)) {
						assignValueToComponent(currentValue);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JLongItemAndReferenceFieldWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				} finally {
					JLongItemAndReferenceFieldWithMeta.this.requestFocus();
				}
			}, SwingUtils.ACTION_ROLLBACK);
			if (!Utils.checkEmptyOrNullString(metadata.getHelpId())) {
				SwingUtils.assignActionKey(this, WHEN_FOCUSED, SwingUtils.KS_HELP, (e)->{
					try {
						SwingUtils.showCreoleHelpWindow(JLongItemAndReferenceFieldWithMeta.this, LocalizerFactory.getLocalizer(metadata.getLocalizerAssociated()), metadata.getHelpId());
					} catch (IOException exc) {
						SwingUtils.getNearestLogger(JLongItemAndReferenceFieldWithMeta.this).message(Severity.error, exc, exc.getLocalizedMessage());
					}
				},SwingUtils.ACTION_HELP);
			}
			SwingUtils.assignActionKey(this, WHEN_FOCUSED, SwingUtils.KS_DROPDOWN,(e)->{
				callSelect.doClick();
			},"show-dropdown");
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JLongItemAndReferenceFieldWithMeta.this);
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
				setBackground(ColorUtils.defaultColorScheme().OPTIONAL_BACKGROUND);
				setForeground(ColorUtils.defaultColorScheme().OPTIONAL_FOREGROUND);
				setAlignmentX(JTextField.LEFT_ALIGNMENT);
			}
			
			callSelect.addActionListener((e)->{selectRef();});
			setBorder(new LineBorder(Color.BLACK));
			new ComponentKeepedBorder(0,callSelect).install(this);
			
			setName(name);
			setEditable(false);
			callSelect.setName(name+'/'+CHOOSER_NAME);
			callSelect.setFocusable(false);
			InternalUtils.registerAdvancedTooptip(this);
			fillLocalizedStrings();
			
		}
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public String getRawDataFromComponent() {
		return getText();
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return newValue;
	}

	@Override
	public void assignValueToComponent(Object value) throws ContentException {
		if (value instanceof ItemAndReference) {
			newValue = (LongItemAndReference<?>)value;
			final Object	presentation = newValue.getPresentation(); 
			
			if (presentation != null) {
				setText(presentation.toString());
			}
			else {
				setText(localizer.getValue(KEY_NOT_DEFINED));
			}
		}
		else {
			throw new ContentException("Value is null or doesn't implement ItemAndReference interface"); 
		}
	}

	@Override
	public Class<?> getValueType() {
		return contentClass;
	}

	@Override
	public String standardValidation(final Object value) {
		// TODO Auto-generated method stub
		return null;
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
		if (!b) {
			final boolean old = isEditable();
			
			super.setEditable(b);
			if (repo != null && b != old) {
				repo.fireBooleanPropChange(this, EventChangeType.MODIFIABLE, b);
			}
		}
	}
	
	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(JLongItemAndReferenceFieldWithMeta.this).message(Severity.error, exc, exc.getLocalizedMessage());
		}					
	}
	
	private void selectRef() {
		currentValue.setModelFilter("");
		
		final JPopupTable	popup = new JPopupTable(this, localizer, SwingUtils.getNearestLogger(this), currentValue);
		
		if (popup.select()) {
			newValue.setValue(popup.getSelectedValue());
			try{
				assignValueToComponent(newValue);
			} catch (ContentException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());			
			}
		}
	}

	private void fillLocalizedStrings() {
	}
	
	private static class JPopupTable extends JDialog implements LoggerFacadeOwner {
		private static final long serialVersionUID = 6213096362129076918L;

		private final JLongItemAndReferenceFieldWithMeta	parent;
		private final Localizer								localizer;
		private final LoggerFacade							logger;
		private final LongItemAndReference<?>				record;
		private final JTable								table;
		private final JLabel								seekLabel = new JLabel();
		private final JTextField							seekFilter = new JTextField();
		private TimerTask									tt = null;
		private boolean										selected = false;
		private long										selectedValue = 0;

		public JPopupTable(final JLongItemAndReferenceFieldWithMeta parent, final Localizer localizer, final LoggerFacade logger, final LongItemAndReference<?> record) {
			super((JDialog)null, true);
			this.parent = parent;
			this.localizer = localizer;					
			this.logger = logger;
			this.record = record;
			this.record.setModelFilter("");
			
			this.table = new JTable((TableModel) new TableModel() {
				final TableModel	delegate = record.getModel();
				final int			keyPosition;
				
				{	int	position = -1;
				
					for(int index = 0, maxIndex = delegate.getColumnCount(); index < maxIndex; index++) {
						if (delegate.getColumnName(index).equals(record.getKeyName())) {
							position = index;
							break;
						}
					}
					if (position == -1) {
						throw new IllegalArgumentException("Key name ["+record.getKeyName()+"] is missing in the table model");
					}
					else {
						keyPosition = position;
					}
				}

				@Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
				
				@Override
				public int getRowCount() {
					return delegate.getRowCount();
				}

				@Override
				public int getColumnCount() {
					return delegate.getColumnCount()-1;
				}

				@Override
				public String getColumnName(final int columnIndex) {
					try{if (columnIndex < keyPosition) {
							return localizer.getValue(delegate.getColumnName(columnIndex));
						}
						else {
							return localizer.getValue(delegate.getColumnName(columnIndex + 1));
						}
					} catch (LocalizationException exc) {
						if (columnIndex < keyPosition) {
							return delegate.getColumnName(columnIndex)+"<non-localized>";
						}
						else {
							return delegate.getColumnName(columnIndex + 1)+"<non-localized>";
						}
					}
				}

				@Override
				public Class<?> getColumnClass(final int columnIndex) {
					if (columnIndex < keyPosition) {
						return delegate.getColumnClass(columnIndex);
					}
					else {
						return delegate.getColumnClass(columnIndex + 1);
					}
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return false;
				}

				@Override
				public Object getValueAt(final int rowIndex, final int columnIndex) {
					if (columnIndex < keyPosition) {
						return delegate.getValueAt(rowIndex, columnIndex);
					}
					else {
						return delegate.getValueAt(rowIndex, columnIndex + 1);
					}
				}

				@Override
				public void addTableModelListener(TableModelListener l) {
					delegate.addTableModelListener(l);
				}

				@Override
				public void removeTableModelListener(TableModelListener l) {
					delegate.removeTableModelListener(l);
				}
			});
			SwingUtils.assignActionKey((JComponent)this.getContentPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_EXIT, (e)->{
				selected = false;
				setVisible(false);
			}, SwingUtils.ACTION_EXIT);
			SwingUtils.assignActionKey(table, SwingUtils.KS_ACCEPT, (e)->{
				if (!table.getSelectionModel().isSelectionEmpty()) {
					processSelection();
				}
				else {
					selected = false;
					setVisible(false);
				}
			}, SwingUtils.ACTION_ACCEPT);
			table.addMouseListener((FunctionalMouseListener)(et, e)->{
				if (et == EventType.CLICKED && e.getClickCount() >= 2 && !table.getSelectionModel().isSelectionEmpty()) {
					processSelection();
				}
			});
			
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setColumnSelectionAllowed(false);
			
			final JPanel	seekPanel = new JPanel(new BorderLayout(5,5));

			seekPanel.add(seekLabel, BorderLayout.WEST);
			seekPanel.add(seekFilter, BorderLayout.CENTER);
			
			getContentPane().add(seekPanel, BorderLayout.NORTH);
			getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
			seekFilter.setText(parent.newValue.getModelFilter());
			setTitle(localizer.getValue(KEY_TITLE));
			seekLabel.setText(localizer.getValue(KEY_FILTER));

			seekFilter.getDocument().addDocumentListener((FunctionalDocumentListener)(ct, e)->{
				if (tt != null) {
					tt.cancel();
					tt = null;
				}
				tt = new SimpleTimerTask(()->{
					parent.newValue.setModelFilter(seekFilter.getText());
					tt = null;
				});
				PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt, REFRESH_DELAY_MILLISECONDS);
			});
			
			pack();
		}

		@Override
		public LoggerFacade getLogger() {
			return logger;
		}
		
		public boolean select() {
			final String	filter = record.getModelFilter();
			
			SwingUtils.centerMainWindow(this, 0.5f);
			setVisible(true);
			dispose();
			record.setModelFilter(filter);
			return selected;
		}
		
		public long getSelectedValue() {
			return selectedValue;
		}
		
		private void processSelection() {
			for (int index = 0; index < record.getModel().getColumnCount(); index++) {
				if (record.getModel().getColumnName(index).equals(record.getKeyName())) {
					selectedValue = ((Long)record.getModel().getValueAt(table.getSelectedRow(), index)).longValue();
					selected = true;
					setVisible(false);
				}
			}
		}
	}
}
