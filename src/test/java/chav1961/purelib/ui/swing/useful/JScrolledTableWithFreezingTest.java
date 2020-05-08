package chav1961.purelib.ui.swing.useful;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.junit.Assert;
import org.junit.Test;

public class JScrolledTableWithFreezingTest {

	@Test
	public void visualTest() {
		final DefaultTableModel				dtm = new DefaultTableModel(new String[] {"col1","col2","col3","col4","col5"}, 20);
		final JScrolledTableWithFreezing	stf = new JScrolledTableWithFreezing(dtm,2);
		
		JOptionPane.showMessageDialog(null,stf);
	}
}
