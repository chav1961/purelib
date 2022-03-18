package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.swing.DefaultListModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.ui.inner.InternalConstants;
import chav1961.purelib.ui.interfaces.ItemAndSelection;
import chav1961.purelib.ui.interfaces.ReferenceAndComment;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.inner.ReferenceAndCommentEditor;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JReferenceListWithMeta extends JList<ReferenceAndComment> implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long serialVersionUID = 8688598119389158690L;

	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final Localizer				localizer;
	private final JPopupMenu			popup;
	private final JToolBar				toolbar;
	private final InnerTableModel<ReferenceAndComment>	model = new InnerTableModel<>();
	private final ContentMetadataInterface				editorModel;
	private final FlavorListener		flavorListener = (e)->refreshMenu();
	private ReferenceAndComment[]		currentValue, newValue;
	private boolean						invalid = false;
	
	public JReferenceListWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws NullPointerException, IllegalArgumentException, LocalizationException, SyntaxException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			this.metadata = metadata;
			this.localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated());
			this.popup = SwingUtils.toJComponent(InternalConstants.MDI.byUIPath(URI.create("ui:/model/navigation.top.JReferenceListWithMeta.menu")),JPopupMenu.class);
			this.toolbar = SwingUtils.toJComponent(InternalConstants.MDI.byUIPath(URI.create("ui:/model/navigation.top.JReferenceListWithMeta.menu")),JToolBar.class);
			this.toolbar.setFloatable(false);
			InternalUtils.cropToolbarButtonsByIcons(this.toolbar);
			
			try{this.editorModel = ContentModelFactory.forAnnotatedClass(ReferenceAndCommentEditor.class);
			} catch (ContentException e) {
				throw new SyntaxException(0, 0, e.getLocalizedMessage(), e); 
			}
			if (metadata.getFormatAssociated() != null && metadata.getFormatAssociated().getHeight() > 1) {
				setVisibleRowCount(metadata.getFormatAssociated().getHeight());
			}
			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (newValue != currentValue && newValue != null && !Arrays.equals(newValue,currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JReferenceListWithMeta.this);
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JReferenceListWithMeta.this);
					} catch (ContentException exc) {
					} finally {
						Toolkit.getDefaultToolkit().getSystemClipboard().removeFlavorListener(flavorListener);
					}
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JReferenceListWithMeta.this);
					} catch (ContentException exc) { 
					} finally {
						Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(flavorListener);
						refreshMenu();
					}
				}
			});
//			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
//				try{if (monitor.process(MonitorEvent.Rollback,metadata,JReferenceListWithMeta.this)) {
//						assignValueToComponent(currentValue);
//					}
//				} catch (ContentException exc) {
//				} finally {
//					JReferenceListWithMeta.this.requestFocus();
//				}
//			},"rollback-value");
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{if (monitor.process(MonitorEvent.Validation,metadata,JReferenceListWithMeta.this)) {
							newValue = (ReferenceAndComment[])getChangedValueFromComponent();
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
			
			prepareSelectedList(metadata);
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		SwingUtils.refreshLocale(popup, localizer.currentLocale().getLocale(), localizer.currentLocale().getLocale());
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	@Override
	public String getRawDataFromComponent() {
		try(final Writer			wr = new StringWriter();
			final JsonStaxPrinter	prn = new JsonStaxPrinter(wr)) {
			boolean		isEmpty = true;

			for (ReferenceAndComment item : model.content) {
				if (isEmpty) {
					isEmpty = false;
					prn.startArray();
				}
				else {
					prn.splitter();
				}
				prn.startObject()
						.name("ref").value(item.getReference().toString())
						.name("comment").value(item.getComment())
				   .endObject();
			}
			if (isEmpty) {
				return "[]";
			}
			else {
				prn.endArray();
				prn.flush();
			}
			return wr.toString();
		} catch (IOException  e) {
			return "[]";
		}
	}

	@Override
	public Object getValueFromComponent() {
		return model.oldContent;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return model.content;
	}

	@Override
	public void assignValueToComponent(final Object value) throws ContentException {
		if (value == null) {
			throw new ContentException("Value to assign can't be null"); 
		}
		else if (value instanceof ReferenceAndComment[]) {
			model.setValue((ReferenceAndComment[]) value);
		}
		else if (value instanceof String) {
			try(final Reader					rdr = new StringReader(value.toString());
				final JsonStaxParser			parser = new JsonStaxParser(rdr)) {
				final JsonNode					root = JsonUtils.loadJsonTree(parser);
				final List<ReferenceAndComment>	result = new ArrayList<>();
				
				if (root.getType() == JsonNodeType.JsonArray) {
					for (JsonNode item : root.children()) {
						if (item.getType() == JsonNodeType.JsonObject) {
							if (JsonUtils.checkJsonMandatories(item, "ref", "comment")) {
								if (JsonUtils.checkJsonFieldTypes(item, "ref/str not null", "comment/str not null")) {
//									result.add(new ItemAndSelection<T>(item.getChild("value"));
								}
								else {
									throw new ContentException("Illegal field types for 'ref' (must be string) and/or 'comment' (must be string) inside some json objects");
								}
							}
							else {
								throw new ContentException("Missing mandatory fields 'ref', 'comment' inside some json objects");
							}
						}
						else {
							throw new ContentException("JSON items inside array are not an objects");
						}
					}
				}
				else {
					throw new ContentException("Top JSON element is not an array");
				}
				assignValueToComponent(result.toArray(new ItemAndSelection[result.size()]));
			} catch (IOException e) {
				throw new ContentException(e);
			}
		}
		else {
			throw new ContentException("Value to assign can be String or ItemAndSelection array only"); 
		}
	}

	@Override
	public Class<?> getValueType() {
		return getNodeMetadata().getType();
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

	@Override
	public void removeNotify() {
        final Container parent = SwingUtilities.getUnwrappedParent(this);
        
		super.removeNotify();
        if (parent instanceof JViewport) {
            final JViewport port = (JViewport) parent;
            final Container gp = port.getParent();
            if (gp instanceof JScrollPane) {
                final JScrollPane 	scrollPane = (JScrollPane)gp;
                final JViewport 	viewport = scrollPane.getViewport();
                
                if (viewport == null || SwingUtilities.getUnwrappedView(viewport) != this) {
                    return;
                }
                else {
                    scrollPane.setColumnHeaderView(null);
                }
            }
        }
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
	public void setEnabled(boolean b) {
		final boolean old = isEnabled();
		
		super.setEnabled(b);
		if (repo != null && b != old) {
			repo.fireBooleanPropChange(this, EventChangeType.ENABLED, b);
		}
	}
	
	private void prepareSelectedList(final ContentNodeMetadata meta) {
		final InnerListModel<ReferenceAndComment>	listModel = new InnerListModel<>();
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setLayout(new BorderLayout());
		setModel(listModel);
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(final TableModelEvent e) {
				switch (e.getType()) {
					case TableModelEvent.DELETE :
						if (e.getFirstRow() == 0 && e.getLastRow() == Integer.MAX_VALUE && e.getColumn() == TableModelEvent.ALL_COLUMNS) {
							listModel.removeAllElements();
						}
						else {
							for (int index = e.getLastRow(), maxIndex = e.getFirstRow(); index >= maxIndex; index--) {
								listModel.remove(index);
							}
						}
						listModel.fireIntervalRemoved(JReferenceListWithMeta.this, e.getFirstRow(), e.getLastRow());
						break;
					case TableModelEvent.INSERT :
						for (int index = e.getFirstRow(), maxIndex = e.getLastRow(); index < maxIndex; index++) {
							listModel.add(index, model.content[index]);
						}
						listModel.fireIntervalAdded(JReferenceListWithMeta.this, e.getFirstRow(), e.getLastRow());
						break;
					case TableModelEvent.UPDATE :
						if (e.getFirstRow() == 0 && e.getLastRow() == Integer.MAX_VALUE && e.getColumn() == TableModelEvent.ALL_COLUMNS) {
							listModel.removeAllElements();
							for (int index = 0; index < model.getRowCount(); index++) {
								listModel.add(index, model.content[index]);
							}
						}
						else {
							for (int index = e.getFirstRow(), maxIndex = Math.min(e.getLastRow(), listModel.getSize()); index < maxIndex; index++) {
								listModel.set(index, model.content[index]);
							}
						}
						listModel.fireContentsChanged(JReferenceListWithMeta.this, e.getFirstRow(), e.getLastRow());
						break;
					default : throw new UnsupportedOperationException(); 
				}
			}
		});

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
		if (InternalUtils.checkMandatory(metadata)) {
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
		final ReferenceAndComment	rac = new ReferenceAndCommentImpl(); 
		
		if (edit(rac)) {
			final ReferenceAndComment[]	temp = Arrays.copyOf(model.content, model.content.length+1);
			
			temp[temp.length-1] = rac;
			model.setValue((ReferenceAndComment[]) temp);
			setSelectedIndex(temp.length-1);
			ensureIndexIsVisible(temp.length-1);
			SwingUtilities.invokeLater(()->requestFocusInWindow());
		}
	}

	@OnAction("action:/menu.edit")
	private void edit() {
		if (getSelectedIndex() >= 0) {
			final int					index = getSelectedIndex();
			final ReferenceAndComment	rac = getModel().getElementAt(index);
			
			if (edit(rac)) {
				model.content[index] = rac;
				model.setValue(model.content);
				setSelectedIndex(index);
				ensureIndexIsVisible(index);
				SwingUtilities.invokeLater(()->requestFocusInWindow());
			}
		}
		else {
			SwingUtilities.invokeLater(()->requestFocusInWindow());
		}
	}
	
	private boolean edit(final ReferenceAndComment rac) {
		try{final ReferenceAndCommentEditor	rce = new ReferenceAndCommentEditor(SwingUtils.getNearestLogger(this));
		
			try(final AutoBuiltForm<ReferenceAndCommentEditor>	abf = new AutoBuiltForm<ReferenceAndCommentEditor>(editorModel,localizer,PureLibSettings.INTERNAL_LOADER,rce,rce)) {
				
				for (Module m : abf.getUnnamedModules()) {
					rce.getClass().getModule().addExports(rce.getClass().getPackageName(),m);
				}
				abf.setPreferredSize(new Dimension(300, 100));
				rce.ref = rac.getReference();
				rce.comment = rac.getComment();
				
				if (AutoBuiltForm.ask((JFrame)null, localizer, abf)) {
					rac.setReference(rce.ref);
					rac.setComment(rce.comment);
					return true;
				}
				else {
					return false;
				}
			}
		} catch (LocalizationException | ContentException e) {
			return false;
		} 
	}

	@OnAction("action:/menu.delete")
	private void delete() {
		if (getSelectedIndex() >= 0) {
			delete(getSelectedIndex());
		}
		SwingUtilities.invokeLater(()->requestFocusInWindow());
	}
	
	private void delete(final int rowIndex) {
		if (rowIndex >= 0) {
			final ReferenceAndComment[]	temp = new ReferenceAndComment[model.content.length-1];
			
			for (int from = 0, to = 0; from < model.content.length; from++) {
				if (from != rowIndex) {
					temp[to++] = model.content[from];
				}
			}
			model.setValue(temp);
			setSelectedIndex(rowIndex);
			ensureIndexIsVisible(rowIndex);
		}
	}

	private void menu(final int rowIndex) {
		if (rowIndex >= 0) {
			final Rectangle2D	rect = getCellBounds(rowIndex, rowIndex);
			popup.show(JReferenceListWithMeta.this, (int)rect.getCenterX(), (int)rect.getCenterY());
		}
	}
	
	@OnAction("action:/menu.copy")
	private void copy() {
		if (getSelectedIndex() >= 0) {
			final ReferenceAndComment	rac = getModel().getElementAt(getSelectedIndex());
			final StringSelection 		ss = new StringSelection(rac.getReference().toString());
			
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		}
		SwingUtilities.invokeLater(()->requestFocusInWindow());
	}
	
	@OnAction("action:/menu.paste")
	private void paste() {
		if (Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor)) {
			final ReferenceAndComment	rac = new ReferenceAndCommentImpl();
			
			try{final Object	obj = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				final String 	value = obj.toString();
							
				try{URI			uri = URI.create(value);
				
					if (uri.getScheme() == null) {
						uri = URI.create("http://"+value);
					}
					rac.setReference(uri);
					rac.setComment("<from clipboard>");
				} catch (IllegalArgumentException exc) {
					rac.setReference(URI.create("https:/"));
					rac.setComment(value);
				}
				
				if (edit(rac)) {
					final ReferenceAndComment[]	temp = Arrays.copyOf(model.content, model.content.length+1);
					
					temp[temp.length-1] = rac;
					model.setValue((ReferenceAndComment[]) temp);
					setSelectedIndex(temp.length-1);
					ensureIndexIsVisible(temp.length-1);
					SwingUtilities.invokeLater(()->requestFocusInWindow());
				}
			} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
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
	
	private void fillLocalizedStrings() {
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
		}					
	}
	
	private void refreshMenu() {
		final boolean	enablePaste = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor);
		
		((JMenuItem)SwingUtils.findComponentByName(popup, "JReferenceListWithMeta.menu.paste")).setEnabled(enablePaste);
		((JButton)SwingUtils.findComponentByName(toolbar, "JReferenceListWithMeta.menu.paste")).setEnabled(enablePaste);
	}
	
	private static class InnerTableModel<T extends ReferenceAndComment> extends DefaultTableModel {
		private static final long 	serialVersionUID = 1L;
		
		private T[]	content = null;
		private T[]	oldContent = null;

		@Override
		public int getRowCount() {
			return content == null ? 0 : content.length;
		}
	
		@Override
		public int getColumnCount() {
			return 2;
		}
	
		@Override
		public String getColumnName(final int columnIndex) {
			switch (columnIndex) {
				case 0 : return "JReferenceListWithMeta.ref";
				case 1 : return "JReferenceListWithMeta.comment";
				default : throw new UnsupportedOperationException(); 
			}
		}
	
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0 : return URI.class;
				case 1 : return String.class;
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
				case 0 : return content[rowIndex].getReference();
				case 1 : return content[rowIndex].getComment();
				default : throw new UnsupportedOperationException(); 
			}
		}
	
		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0 : 
					content[rowIndex].setReference((URI)aValue);
					break;
				case 1 : 
					content[rowIndex].setComment((String)aValue);
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
	
	private static class ReferenceAndCommentImpl implements ReferenceAndComment {
		private URI		reference = URI.create("http:/");
		private String	comment = "";

		@Override
		public URI getReference() {
			return reference;
		}

		@Override
		public void setReference(final URI reference) {
			this.reference = reference;
		}

		@Override
		public String getComment() {
			return comment;
		}

		@Override
		public void setComment(String comment) {
			this.comment = comment;
		}

		@Override
		public int hashCode() {
			return Objects.hash(comment, reference);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ReferenceAndCommentImpl other = (ReferenceAndCommentImpl) obj;
			return Objects.equals(comment, other.comment) && Objects.equals(reference, other.reference);
		}

		@Override
		public String toString() {
			return "ReferenceAndCommentImpl [reference=" + reference + ", comment=" + comment + "]";
		}
	}

}
