package chav1961.purelib.ui.swing;

import javax.swing.JToolTip;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import chav1961.purelib.i18n.interfaces.Localizer;

class CreoleBasedTableHeader extends JTableHeader {
	private static final long serialVersionUID = 1457547339689685811L;
	
	private final Localizer	localizer;
	
	public CreoleBasedTableHeader(final Localizer localizer, final TableColumnModel model) {
		super(model);
		this.localizer = localizer;
	}
	
	@Override 
	public JToolTip createToolTip() {
		return new SmartToolTip(localizer,this);
	}
}