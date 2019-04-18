package chav1961.purelib.ui.swing.useful;

import javax.swing.table.DefaultTableModel;
import javax.swing.text.html.parser.ContentModel;

import chav1961.purelib.sql.interfaces.ORMProvider;
import chav1961.purelib.ui.interfaces.FormManager;

public class ORMProvidedTableModel<Record> extends DefaultTableModel {
	private static final long serialVersionUID = 5818266099123995333L;

	public ORMProvidedTableModel(final ContentModel clazzModel, final ORMProvider<Record,Record> provider, final Record instance, final String[] fields, final FormManager<Object,Record> mgr) {
		
	}

	public void setFilter(final String filter) {
		
	}

	public void setOrdering(final String ordering) {
		
	}

	public void setCurrentLine(int currentRecord) {
		
	}
	
	public void setPageSize(int pageSize) {
		
	}
	
	public void insert() {
		
	}

	public void duplicate(int current) {
		
	}

	public void delete(int current) {
		
	}
	
	public void reload() {
		
	}
}
