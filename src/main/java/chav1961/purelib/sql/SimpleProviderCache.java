package chav1961.purelib.sql;

import java.sql.Connection;
import java.sql.SQLException;

import chav1961.purelib.sql.interfaces.ORMProvider;

public class SimpleProviderCache<Record>  {
	public SimpleProviderCache(final ORMProvider<Record,Record> provider, final Class<Record> clazz) {
		
	}
	
	public int getPageSize() {
		return 0;
	}
	
	public void setPageSize(int count) {
		
	}
	
	public int getCurrent() {
		return 0;
	}
	
	public void setCurrent(int current) {
		
	}
	
	public void markChanged(int current, boolean state) {
		
	}

	public void sync() {
		
	}
	
	public void refresh() {
		
	}



}
