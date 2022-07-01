package chav1961.purelib.ui.interfaces;

import javax.swing.table.TableModel;

import chav1961.purelib.model.interfaces.NodeMetadataOwner;

public interface ItemAndReference<P> extends Cloneable, NodeMetadataOwner, PresentationOwner<P> {
	String getKeyName();
	Class<?> getKeyClass();
	TableModel getModel();
	String getModelFilter();
	void setModelFilter(final String filter);
	void setModel(TableModel model);
	Object clone() throws CloneNotSupportedException;
}
