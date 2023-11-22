package chav1961.purelib.ui.swing.useful;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;

public class JFreezableTableTest {
	@Test
	@Ignore
	public void test() {
		final TableModel		model = new DefaultTableModel(new Integer[][] {new Integer[] {1,2,3}, new Integer[] {4,5,6}}, new String[] {"c1","c2","c3"});
		final JFreezableTable	jft = new JFreezableTable(model, "c1");
		
		JOptionPane.showMessageDialog(null, new JScrollPane(jft));
		
	}
}
