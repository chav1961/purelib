package chav1961.purelib.ui.swing;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.LabelAndField;
import chav1961.purelib.ui.UIUtils;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.FormModel;
import chav1961.purelib.ui.interfacers.RecordFormManager;

class MicroTableEditorEditableContent<T> extends AbstractTableModel implements LocaleChangeListener {
	private static final long 								serialVersionUID = 2404601693653185155L;
	
	private final Localizer									localizer;	
	private final FormManager<Object,T>						formManager;
	private final FormModel<Object,T>						formModel;
	private final List<LabelAndField<JLabel,JComponent>>	retained = new ArrayList<>();

	MicroTableEditorEditableContent(final Localizer localizer, final FormManager<Object,T> formManager, final FormModel<Object,T> formModel, final String[] columns) throws IllegalArgumentException, NullPointerException, LocalizationException, SyntaxException, ContentException{
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}			
		else if (formModel == null) {
			throw new NullPointerException("Form model can't be null");
		}			
		else if (columns == null || columns.length == 0) {
			throw new IllegalArgumentException("Columns list can't be null or empty array");
		}			
		else {
			final List<LabelAndField<JLabel,JComponent>>	list = new ArrayList<>();
			
			UIUtils.collectFields(localizer,formModel.getInstanceType(),null,list
									, (loc,id)->{return new JLabel(loc.getValue(id));}
									, (loc,desc,tooltip,initial)->{return null;}
									);
			for (LabelAndField<JLabel, JComponent> item : list) {
				for (String name : columns) {
					if (name.equals(item.fieldDesc.field.getName())) {
						this.retained.add(item);
						break;
					}
				}
			}
			this.localizer = localizer;
			this.formModel = formModel;
			this.formManager = formManager;
		}
	}

	@Override
	public int getRowCount() {
		return formModel.size();
	}

	@Override
	public int getColumnCount() {
		return retained.size();
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		try{return retained.get(columnIndex).fieldDesc.getFieldValue(formModel.getInstance(formModel.getIdByIndex(rowIndex)));
		} catch (ContentException e) {
			return null;
		}
	}

	@Override
	public String getColumnName(final int columnIndex) {
		try{return localizer.getValue(retained.get(columnIndex).labelId);
		} catch (LocalizationException  e) {
			return retained.get(columnIndex).labelId;
		}
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		return retained.get(columnIndex).fieldDesc.fieldType;
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return formModel.getOperationsSupported().size() > 0;
	}

	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		try{final Object	oldId = formModel.getIdByIndex(rowIndex);
			final T			oldInstance = (T)formModel.getInstance(oldId);
			final Object	oldValue = getValueAt(rowIndex,columnIndex);
			
			retained.get(columnIndex).fieldDesc.setFieldValue(formModel.getInstance(formModel.getIdByIndex(rowIndex)),aValue);
			switch (formManager.onField(oldInstance,oldId,retained.get(columnIndex).fieldDesc.field.getName(),oldValue)) {
				case REJECT		:
					retained.get(columnIndex).fieldDesc.setFieldValue(formModel.getInstance(formModel.getIdByIndex(rowIndex)),oldValue);
					break;
				case NONE:
					break;
				case FIELD_ONLY	: case RECORD_ONLY : 
					fireTableChanged(new TableModelEvent(this,rowIndex));
					break;
				case TOTAL:
					fireTableChanged(new TableModelEvent(this));
					break;
				default:
					break;
			}
		} catch (ContentException | LocalizationException | FlowException e) {
			formManager.getLogger().message(Severity.error,e,e.getLocalizedMessage());
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fireTableChanged(new TableModelEvent(this,TableModelEvent.HEADER_ROW));
	}
	
	public void insertRow() {
		try{final Object	key = formModel.createUniqueId();
			final T			value = formModel.createInstance(key);
			final int		index = formModel.getIndexById(key);
			
			switch (formManager.onRecord(RecordFormManager.Action.INSERT,null,null,value,key)) {
				case REJECT			:
					formModel.removeInstance(key);
					break;
				case FIELD_ONLY : case RECORD_ONLY	:
					fireTableChanged(new TableModelEvent(this,index,index,TableModelEvent.ALL_COLUMNS,TableModelEvent.INSERT));
					break;
				case NONE			:
					break;
				case TOTAL			:
					fireTableChanged(new TableModelEvent(this,TableModelEvent.HEADER_ROW));
					break;
				default:
					break;
			}
		} catch (ContentException | LocalizationException | FlowException e) {
			formManager.getLogger().message(Severity.error,e,e.getLocalizedMessage());
		}
	}

	public void duplicateRow(final int rowIndex) {
		try{final Object	oldId = formModel.getIdByIndex(rowIndex), newId = formModel.createUniqueId();
			final T			oldValue = formModel.getInstance(oldId), newValue = formModel.duplicateInstance(oldId,newId);
			final int		index = formModel.getIndexById(newId);
			
			switch (formManager.onRecord(RecordFormManager.Action.DUPLICATE,oldValue,oldId,newValue,newId)) {
				case REJECT			:
					formModel.removeInstance(newId);
					break;
				case FIELD_ONLY : case RECORD_ONLY	:
					fireTableChanged(new TableModelEvent(this,index,index,TableModelEvent.ALL_COLUMNS,TableModelEvent.INSERT));
					break;
				case NONE			:
					break;
				case TOTAL			:
					fireTableChanged(new TableModelEvent(this));
					break;
				default:
					break;
			}
		} catch (ContentException | LocalizationException | FlowException e) {
			formManager.getLogger().message(Severity.error,e,e.getLocalizedMessage());
		}
	}
	
	public void deleteRow(final int rowIndex) {
		try{final Object	oldId = formModel.getIdByIndex(rowIndex), newId = formModel.createUniqueId();
			final T			oldValue = formModel.getInstance(oldId), newValue = formModel.duplicateInstance(oldId,newId);
			final int		index = formModel.getIndexById(newId);
			
			switch (formManager.onRecord(RecordFormManager.Action.DUPLICATE,oldValue,oldId,newValue,newId)) {
				case REJECT			:
					break;
				case FIELD_ONLY : case RECORD_ONLY	: case NONE :
					formModel.removeInstance(formModel.getInstance(formModel.getIdByIndex(rowIndex)));
					fireTableChanged(new TableModelEvent(this,rowIndex,rowIndex,TableModelEvent.ALL_COLUMNS,TableModelEvent.DELETE));
					break;
				case TOTAL			:
					fireTableChanged(new TableModelEvent(this));
					fireTableChanged(new TableModelEvent(this));
					break;
				default:
					break;
			}
		} catch (ContentException | LocalizationException | FlowException e) {
			formManager.getLogger().message(Severity.error,e,e.getLocalizedMessage());
		}
	}
	
	public boolean checkContent() {
		try{for (Object item : formModel.contentIds()) {
				switch (formManager.onRecord(RecordFormManager.Action.CHECK,null,null,formModel.getInstance(item),item)) {
					case REJECT			:
						return false;
					default:
						break;
				}
			}
			return true;
		} catch (ContentException | LocalizationException | FlowException e) {
			formManager.getLogger().message(Severity.error,e,e.getLocalizedMessage());
			return false;
		}
	}
}