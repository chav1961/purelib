package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleTimerTask;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.inner.InternalConstants;
import chav1961.purelib.ui.interfaces.LongItemAndReference;
import chav1961.purelib.ui.interfaces.LongItemAndReferenceList;
import chav1961.purelib.ui.interfaces.ReferenceAndComment;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.inner.LongItemAndReferenceListImpl;
import chav1961.purelib.ui.swing.inner.ReferenceAndCommentEditor;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.FunctionalDocumentListener;
import chav1961.purelib.ui.swing.interfaces.FunctionalKeyListener;
import chav1961.purelib.ui.swing.interfaces.FunctionalMouseListener;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JLongItemAndReferenceListWithMeta<T> extends JList<LongItemAndReference<T>> implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long serialVersionUID = 8271072422316473652L;

	public static final String 			KEY_TITLE = "JLongItemAndReferenceFieldWithMeta.select.title";
	public static final String 			KEY_FILTER = "JLongItemAndReferenceFieldWithMeta.select.typefilter";
	public static final long			REFRESH_DELAY_MILLISECONDS = 300;

	private static final Class<?>[]		VALID_CLASSES = {LongItemAndReference[].class, LongItemAndReferenceList.class};
	
	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final Localizer				localizer;
	private Class<?>					contentClass;
	private final JPopupMenu			popup;
	private final JToolBar				toolbar;
//	private final InnerTableModel<LongItemAndReference<T>>	model = new InnerTableModel<>();
	private LongItemAndReferenceList<T>	currentValue = new LongItemAndReferenceListImpl(), newValue = new LongItemAndReferenceListImpl();
	private boolean						invalid = false;
	
	public JLongItemAndReferenceListWithMeta(final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException {
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
			
			this.popup = SwingUtils.toJComponent(InternalConstants.MDI.byUIPath(URI.create("ui:/model/navigation.top.JLongItemAndReferenceListWithMeta.menu")),JPopupMenu.class);
			this.toolbar = SwingUtils.toJComponent(InternalConstants.MDI.byUIPath(URI.create("ui:/model/navigation.top.JLongItemAndReferenceListWithMeta.menu")),JToolBar.class);
			this.toolbar.setFloatable(false);
			InternalUtils.cropToolbarButtonsByIcons(this.toolbar);
			
			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());

			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				private Border	oldBorder;
				
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (newValue != currentValue && newValue != null && !newValue.equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JLongItemAndReferenceListWithMeta.this);
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JLongItemAndReferenceListWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JLongItemAndReferenceListWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					} finally {
						setBorder(oldBorder);
					}
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{oldBorder = getBorder();
						setBorder(InternalUtils.getFocusedBorder());
						
						monitor.process(MonitorEvent.FocusGained,metadata,JLongItemAndReferenceListWithMeta.this);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JLongItemAndReferenceListWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
			});
			SwingUtils.assignActionKey(this, WHEN_FOCUSED, SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JLongItemAndReferenceListWithMeta.this)) {
						assignValueToComponent(currentValue);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JLongItemAndReferenceListWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				} finally {
					JLongItemAndReferenceListWithMeta.this.requestFocus();
				}
			}, SwingUtils.ACTION_ROLLBACK);
			SwingUtils.assignActionKey(this, WHEN_FOCUSED, SwingUtils.KS_DROPDOWN,(e)->{
//				callSelect.doClick();
			},"show-dropdown");
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JLongItemAndReferenceListWithMeta.this);
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
			}
			else {
				setBackground(PureLibSettings.defaultColorScheme().OPTIONAL_BACKGROUND);
				setForeground(PureLibSettings.defaultColorScheme().OPTIONAL_FOREGROUND);
				setAlignmentX(JTextField.LEFT_ALIGNMENT);
			}
			
			setName(name);
			setModel(new InnerListModel<LongItemAndReference<T>>(currentValue));
			refresh();
			InternalUtils.registerAdvancedTooptip(this);
			prepareSelectedList(metadata);
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
		final StringBuilder	sb = new StringBuilder();
		
		for (LongItemAndReference<?> item : newValue) {
			sb.append(',').append(item.getValue());
		}
		return sb.substring(1);
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		if (contentClass != newValue.getClass()) {
			if (LongItemAndReference[].class.isAssignableFrom(contentClass)) {
				return newValue.toArray((LongItemAndReference[])Array.newInstance(contentClass.getComponentType(), newValue.size()));
			}
			else {
				throw new SyntaxException(0, 0, "Illegal class ["+contentClass+"] to convert");
			}
		}
		else {
			return newValue;
		}
	}

	@Override
	public void assignValueToComponent(Object value) throws ContentException {
		if (value instanceof LongItemAndReference[]) {
			newValue.clear();
			for (LongItemAndReference<T> item : ((LongItemAndReference[])value)) {
				newValue.add(item);
			}
			if (newValue.isEmpty()) {
				throw new ContentException("Value to assign must contain at least one element in the array"); 
			}
			else {
				contentClass = value.getClass();
				refresh();
			}
		}
		else if (value instanceof LongItemAndReferenceList) {
			newValue.clear();
			for (LongItemAndReference<T> item : ((LongItemAndReferenceList<T>)value)) {
				newValue.add(item);
			}
			if (newValue.isEmpty()) {
				throw new ContentException("Value to assign must contain at least one element in the list"); 
			}
			else {
				contentClass = value.getClass();
				refresh();
			}
		}
		else {
			throw new ContentException("Value is null or doesn't implement LongItemAndReferenceList interface or is not a LongItemAndReference array"); 
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
	public void addNotify() {
        final Container parent = SwingUtilities.getUnwrappedParent(this);
        
		super.addNotify();
        if (parent instanceof JViewport) {
            final JViewport port = (JViewport) parent;
            final Container gp = port.getParent();
            
            if (gp instanceof JScrollPane) {
                final JScrollPane scrollPane = (JScrollPane)gp;
                final JViewport viewport = scrollPane.getViewport();
                
                if (viewport == null || SwingUtilities.getUnwrappedView(viewport) != this) {
                    return;
                }
                else {
                    scrollPane.setColumnHeaderView(toolbar);
                }
            }
        }
	}
	
	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue.clear();
			for (LongItemAndReference<T> item : newValue) {
				currentValue.add(item);
			}
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(JLongItemAndReferenceListWithMeta.this).message(Severity.error, exc, exc.getLocalizedMessage());
		}					
	}

	private void prepareSelectedList(final ContentNodeMetadata meta) {
		final InnerListModel<LongItemAndReference<T>>	listModel = new InnerListModel<>(currentValue);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setLayout(new BorderLayout());
		setModel(listModel);

		SwingUtils.assignActionKey(this, SwingUtils.KS_COPY, (e)->copy(), SwingUtils.ACTION_COPY);
		SwingUtils.assignActionKey(this, SwingUtils.KS_PASTE, (e)->paste(), SwingUtils.ACTION_PASTE);
		SwingUtils.assignActionKey(this, SwingUtils.KS_INSERT, (e)->insert(), SwingUtils.ACTION_INSERT);
		SwingUtils.assignActionKey(this, SwingUtils.KS_ACCEPT, (e)->edit(), SwingUtils.ACTION_ACCEPT);
		SwingUtils.assignActionKey(this, SwingUtils.KS_DELETE, (e)->delete(this.getSelectedIndex()), SwingUtils.ACTION_DELETE);
		SwingUtils.assignActionKey(this, SwingUtils.KS_CONTEXTMENU, (e)->menu(this.getSelectedIndex()), SwingUtils.ACTION_CONTEXTMENU);
		
		SwingUtils.assignActionListeners(popup, this);
		SwingUtils.assignActionListeners(toolbar, this);
		addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				final int	index = locationToIndexExactly(e.getPoint());
				
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (index >= 0 && e.getClickCount() >= 2) {
						edit();
					}
				}
				else if (e.getButton() == MouseEvent.BUTTON3 && index >= 0) {
					menu(index);
				}
			}
		});
		addMouseMotionListener(new MouseMotionListener() {
			private final Cursor	oldCursor = getCursor();
			
			@Override public void mouseDragged(MouseEvent e) {}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				final int	index = locationToIndexExactly(e.getPoint());
			
				if (index >= 0) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				else {
					setCursor(oldCursor);
				}
			}
		});
		if (InternalUtils.isContentMandatory(metadata)) {
			InternalUtils.prepareMandatoryColor(this);
		}
		else {
			InternalUtils.prepareOptionalColor(this);
		}
		try {
			setCellRenderer(SwingUtils.getCellRenderer(meta, ListCellRenderer.class));
		} catch (EnvironmentException e) {
			throw new IllegalArgumentException("No rendered found for ["+meta.getType().getCanonicalName()+"] in the list");
		}
		
	}

	@OnAction("action:/menu.insert")
	private void insert() {
		try{final LongItemAndReference<T>	liar = newItem();
		
			if (edit(liar)) {
				((DefaultListModel)getModel()).addElement(liar);
				newValue.add(liar);
				setSelectedIndex(getModel().getSize() - 1);
				ensureIndexIsVisible(getModel().getSize() - 1);
				SwingUtilities.invokeLater(()->requestFocusInWindow());
			}
		} catch (ContentException e) {
			SwingUtils.getNearestLogger(this).message(Severity.warning, e, e.getLocalizedMessage());
		} 
	}

	@OnAction("action:/menu.edit")
	private void edit() {
		if (getSelectedIndex() >= 0) {
			final int						index = getSelectedIndex();
			final LongItemAndReference<T>	liar = getModel().getElementAt(index);
			
			if (edit(liar)) {
				((DefaultListModel)getModel()).set(index, liar);
				SwingUtilities.invokeLater(()->requestFocusInWindow());
			}
		}
		else {
			SwingUtilities.invokeLater(()->requestFocusInWindow());
		}
	}
	
	private boolean edit(final LongItemAndReference<T> rac) {
		try{final JPopupTable<T>	popupTable = new JPopupTable(this, localizer, SwingUtils.getNearestLogger(this), rac);

			if (popupTable.select()) {
				rac.setValue(popupTable.getSelectedValue());
				return true;
			}
			else {
				return false;
			}
		} catch (LocalizationException e) {
			return false;
		} 
	}

	@OnAction("action:/menu.delete")
	private void delete() {
		if (getSelectedIndex() >= 0 && getModel().getSize() > 1) {
			delete(getSelectedIndex());
			newValue.remove(getSelectedIndex());
		}
		SwingUtilities.invokeLater(()->requestFocusInWindow());
	}
	
	private void delete(final int rowIndex) {
		if (rowIndex >= 0) {
			if (((DefaultListModel<?>)getModel()).getSize() > 1) {
				((DefaultListModel<?>)getModel()).remove(rowIndex);
				
				final int	selection = Math.min(rowIndex, getModel().getSize() - 1);
				
				setSelectedIndex(selection);
				ensureIndexIsVisible(selection);
			}
			else {
				SwingUtils.getNearestLogger(this).message(Severity.warning, "Can't delete from list, at lest one item must present");
			}
		}
	}

	private void menu(final int rowIndex) {
		if (rowIndex >= 0) {
			final Rectangle2D	rect = getCellBounds(rowIndex, rowIndex);
			popup.show(JLongItemAndReferenceListWithMeta.this, (int)rect.getCenterX(), (int)rect.getCenterY());
		}
	}
	
	@OnAction("action:/menu.copy")
	private void copy() {
		if (getSelectedIndex() >= 0) {
			final LongItemAndReference<T>	liar = getModel().getElementAt(getSelectedIndex());
			final StringSelection 			ss = new StringSelection(String.valueOf(liar.getValue()));
			
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		}
		SwingUtilities.invokeLater(()->requestFocusInWindow());
	}
	
	@OnAction("action:/menu.paste")
	private void paste() {
		if (Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try{final LongItemAndReference<T>	liar = newItem();
				final Object					obj = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				final String 					value = obj.toString();
							
				try{liar.setValue(Long.valueOf(value));
				} catch (NumberFormatException exc) {
					liar.setValue(-1);
				}
				
				if (edit(liar)) {
					((DefaultListModel)getModel()).addElement(liar);
					setSelectedIndex(getModel().getSize() - 1);
					ensureIndexIsVisible(getModel().getSize() - 1);
					SwingUtilities.invokeLater(()->requestFocusInWindow());
				}
			} catch (HeadlessException | UnsupportedFlavorException | ContentException | IOException e) {
				SwingUtils.getNearestLogger(this).message(Severity.warning, e, e.getLocalizedMessage());
			} 
		}
	}
	
	private int locationToIndexExactly(final Point point) {
		final int		index = locationToIndex(point);
		final Rectangle	rect = getCellBounds(index, index);
		
		if (rect != null && rect.contains(point.x, point.y)) {
			return index;
		}
		else {
			return -1;
		}
	}
	
	private LongItemAndReference<T> newItem() throws ContentException {
		try{return (LongItemAndReference<T>)getModel().getElementAt(0).clone();
		} catch (CloneNotSupportedException e) {
			throw new ContentException(e);
		}
	}
	
	private void fillLocalizedStrings() {
	}

	private void refresh() {
		setModel(new InnerListModel<>(newValue));
	}

	private static class InnerTableModel<T extends LongItemAndReference<?>> extends DefaultTableModel {
		private static final long 	serialVersionUID = 1L;
		
		private T[]	content = null;
		private T[]	oldContent = null;

		@Override
		public int getRowCount() {
			return content == null ? 0 : content.length;
		}
	
		@Override
		public int getColumnCount() {
			return 1;
		}
	
		@Override
		public String getColumnName(final int columnIndex) {
			switch (columnIndex) {
				case 0 : return "JReferenceListWithMeta.ref";
				default : throw new UnsupportedOperationException(); 
			}
		}
	
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0 : return Long.class;
				default : throw new UnsupportedOperationException(); 
			}
		}
	
		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0 : return true;
				case 1 : return true;
				default : throw new UnsupportedOperationException(); 
			}
		}
	
		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0 : return content[rowIndex].getValue(rowIndex);
				default : throw new UnsupportedOperationException(); 
			}
		}
	
		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0 : 
					content[rowIndex].setValue((Long)aValue);
					break;
				default : throw new UnsupportedOperationException(); 
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}
		
		public void setValue(final T[] content) {
			this.oldContent = this.content;
			this.content = content;
			fireTableDataChanged();
		}
	};
	
	private static class InnerListModel<T> extends DefaultListModel<T> {
		private static final long 	serialVersionUID = 1L;

		private InnerListModel(final LongItemAndReferenceList<?> list) {
			int	index = 0;
			
			for(Object item : list) {
				add(index++, (T)item);
			}
		}
		
		@Override
		public void fireIntervalAdded(Object source, int index0, int index1) {
			super.fireIntervalAdded(source, index0, index1);
		}
		
		@Override
		public void fireIntervalRemoved(Object source, int index0, int index1) {
			super.fireIntervalRemoved(source, index0, index1);
		}
		
		@Override
		public void fireContentsChanged(Object source, int index0, int index1) {
			super.fireContentsChanged(source, index0, index1);
		}
	};
	
	private static class JPopupTable<T> extends JDialog implements LoggerFacadeOwner {
		private static final long serialVersionUID = 6213096362129076918L;

		private final JLongItemAndReferenceListWithMeta	parent;
		private final Localizer							localizer;
		private final LoggerFacade						logger;
		private final LongItemAndReference<T>			record;
		private final JTable							table;
		private final JLabel							seekLabel = new JLabel();
		private final JTextField						seekFilter = new JTextField();
		private TimerTask								tt = null;
		private boolean									selected = false;
		private long									selectedValue = 0;

		public JPopupTable(final JLongItemAndReferenceListWithMeta parent, final Localizer localizer, final LoggerFacade logger, final LongItemAndReference<T> record) {
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
				if (et == FunctionalMouseListener.EventType.CLICKED && e.getClickCount() >= 2 && !table.getSelectionModel().isSelectionEmpty()) {
					processSelection();
				}
			});
			table.addKeyListener((FunctionalKeyListener)(et,e)->{
				if (et == FunctionalKeyListener.EventType.TYPED) {
					seekFilter.setText(seekFilter.getText()+e.getKeyChar());
					seekFilter.setCaretPosition(seekFilter.getText().length());
					seekFilter.requestFocusInWindow();
				}
			});
			
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setColumnSelectionAllowed(false);
			
			final JPanel	seekPanel = new JPanel(new BorderLayout(5,5));

			seekPanel.add(seekLabel, BorderLayout.WEST);
			seekPanel.add(seekFilter, BorderLayout.CENTER);
			
			getContentPane().add(seekPanel, BorderLayout.NORTH);
			getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
			setTitle(localizer.getValue(KEY_TITLE));
			seekLabel.setText(localizer.getValue(KEY_FILTER));

			seekFilter.getDocument().addDocumentListener((FunctionalDocumentListener)(ct, e)->{
				if (tt != null) {
					tt.cancel();
					tt = null;
				}
				tt = new SimpleTimerTask(()->{
					record.setModelFilter(seekFilter.getText());
					tt = null;
				});
				PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt, REFRESH_DELAY_MILLISECONDS);
			});
			pack();
			SwingUtilities.invokeLater(()->{
				table.requestFocusInWindow();
				table.getSelectionModel().addSelectionInterval(0,0);
			});
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
