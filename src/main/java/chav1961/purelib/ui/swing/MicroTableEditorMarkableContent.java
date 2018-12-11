package chav1961.purelib.ui.swing;


import java.lang.reflect.Array;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;


class MicroTableEditorMarkableContent<T> extends AbstractTableModel {
	private static final long 				serialVersionUID = -2608295697992286953L;
	private final Class<T>					contentType;
	private final String[]					columns;
	private final Class<?>[]				columnsClasses;
	private final List<Object[]>			content = new ArrayList<>();

	MicroTableEditorMarkableContent(final Class<T> contentType, final T currentContent, final T availableContent) {
		this(new String[0],contentType,currentContent,availableContent);
	}
	
	MicroTableEditorMarkableContent(final String[] columns, final Class<T> contentType, final T currentContent, final T availableContent) {
		if (columns == null || columns.length == 0) {
			throw new IllegalArgumentException("Columns list can't be null or empty array");
		}			
		else if (contentType == null) {
			throw new NullPointerException("Content class can't be null");
		}			
		else if (availableContent == null) {
			throw new NullPointerException("Available content data can't be null");
		}			
		else {
			final TypeVariable<Class<T>>[] 	parm = contentType.getTypeParameters();

			this.contentType = contentType;
			this.columns = new String[columns.length+1];
			this.columnsClasses = new Class<?>[columns.length+1];
			this.columns[0] = "\u2611";
			this.columnsClasses[0] = Boolean.class;
			
			System.arraycopy(columns,0,this.columns,1,columns.length);
			
			if (Collection.class.isAssignableFrom(contentType)){
				if (columns.length == 1) {
					for (Object item : ((Iterable<?>)availableContent)) {
						content.add(new Object[]{((Collection<?>)currentContent).contains(item),item});
					}
					this.columnsClasses[1] = parm.length > 0 && parm[0] != null ? parm[0].getGenericDeclaration() : Object.class;
				}
				else {
					throw new IllegalArgumentException("Columns list contains ["+columns.length+"] elements, but exactly one is required");
				}
			}
			else if (Map.class.isAssignableFrom(contentType)){
				if (columns.length == 2) {
					for (Entry<?,?> item : ((Map<?,?>)availableContent).entrySet()) {
						content.add(new Object[]{((Map<?,?>)currentContent).containsKey(item.getKey()),item.getKey(),item.getValue()});
					}
					this.columnsClasses[1] = parm.length > 0 && parm[0] != null ? parm[0].getGenericDeclaration() : Object.class;
					this.columnsClasses[2] = parm.length > 1 && parm[1] != null ? parm[1].getGenericDeclaration() : Object.class;
				}
				else {
					throw new IllegalArgumentException("Columns list contains ["+columns.length+"] elements, but exactly two is required");
				}
			}
			else if (contentType.isArray()) {
				if (columns.length == 1) {
					for (int index = 0, maxIndex = Array.getLength(availableContent); index < maxIndex; index++) {
						final Object	item = Array.get(availableContent,index);
						content.add(new Object[]{contains((Object[])currentContent,item),item});
					}
					this.columnsClasses[1] = contentType.getComponentType();
				}
				else {
					throw new IllegalArgumentException("Columns list contains ["+columns.length+"] elements, but exactly one is required");
				}
			}
			else {
				throw new IllegalArgumentException("Content type ["+contentType+"] ia not available for the model. Available is Collection.class or <T>[].class");
			}
		}
	}

	@Override
	public int getRowCount() {
		return content.size();
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public String getColumnName(final int columnIndex) {
		return columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		return columnsClasses[columnIndex];
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return columnIndex == 0;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		return content.get(rowIndex)[columnIndex];
	}

	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		content.get(rowIndex)[columnIndex] = aValue;
		fireTableCellUpdated(rowIndex,columnIndex);
	}

	T getContent() throws InstantiationException, IllegalAccessException {
		if (Collection.class.isAssignableFrom(contentType)){
			final Collection<Object>	result = (Collection<Object>) contentType.newInstance();

			for (Object[] item : content) {
				result.add(item[1]);
			}
			return (T) result;
		}
		else if (Map.class.isAssignableFrom(contentType)){
			final Map<Object,Object>	result = (Map<Object,Object>) contentType.newInstance();

			for (Object[] item : content) {
				result.put(item[1],item[2]);
			}
			return (T) result;
		}
		else if (contentType.isArray()) {
			final Object[]				result = (Object[]) Array.newInstance(contentType.getComponentType(),content.size());
			
			for (int index = 0; index < content.size(); index++) {
				result[index] = content.get(index)[1];
			}
			return (T) result;
		}
		else {
			throw new IllegalArgumentException("Content type ["+contentType+"] ia not available for the model. Available is Collection.class or <T>[].class");
		}
	}
	
	private boolean contains(final Object[] currentContent, final Object entity) {
		for (Object item : currentContent) {
			if (item.equals(entity)) {
				return true;
			}
		}
		return false;
	}
}