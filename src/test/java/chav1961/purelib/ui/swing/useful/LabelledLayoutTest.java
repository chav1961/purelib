package chav1961.purelib.ui.swing.useful;


import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class LabelledLayoutTest {
	@Test
	public void basicTest() {
		final JComponent		parent = new JComponent(){};
		final Insets			ins = parent.getInsets();
		final LabelledLayout	layout = new LabelledLayout(); 

		Assert.assertEquals(0.0,layout.getLayoutAlignmentX(parent),0.0001);
		Assert.assertEquals(0.0,layout.getLayoutAlignmentY(parent),0.0001);
		
		try{layout.addLayoutComponent((JComponent)null,null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{layout.addLayoutComponent(new JLabel(),null);
			Assert.fail("Mandatory exception was not detected (invalid constraint)");
		} catch (IllegalArgumentException exc) {
		}

		try{layout.addLayoutComponent((String)null,new JLabel());
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{layout.addLayoutComponent("unknown",new JLabel());
			Assert.fail("Mandatory exception was not detected (invalid constraint)");
		} catch (IllegalArgumentException exc) {
		}
		try{layout.addLayoutComponent(LabelledLayout.LABEL_AREA,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		try{layout.removeLayoutComponent(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.minimumLayoutSize(parent));
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.preferredLayoutSize(parent));
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.maximumLayoutSize(parent));
		
		try{layout.minimumLayoutSize(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{layout.preferredLayoutSize(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{layout.maximumLayoutSize(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
	}

	@Test
	public void oneLineSizeTest() {
		final JComponent		parent = new JComponent(){};
		final Insets			ins = parent.getInsets();
		final LabelledLayout	layout = new LabelledLayout();
		final JComponent		left = new JComponent(){}, right = new JComponent(){};  
		
		parent.setLayout(layout);
		parent.add(left,LabelledLayout.LABEL_AREA);
		parent.add(right,LabelledLayout.CONTENT_AREA);
		
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.minimumLayoutSize(parent));
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.preferredLayoutSize(parent));
		Assert.assertEquals(new Dimension(32767-(ins.left+ins.right),32767-(ins.top+ins.bottom)),layout.maximumLayoutSize(parent));
		
		left.setPreferredSize(new Dimension(100,200));
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.minimumLayoutSize(parent));
		Assert.assertEquals(new Dimension(100-(ins.left+ins.right),200-(ins.top+ins.bottom)),layout.preferredLayoutSize(parent));
		Assert.assertEquals(new Dimension(32767-(ins.left+ins.right),32767-(ins.top+ins.bottom)),layout.maximumLayoutSize(parent));

		right.setPreferredSize(new Dimension(150,300));
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.minimumLayoutSize(parent));
		Assert.assertEquals(new Dimension(250-(ins.left+ins.right),300-(ins.top+ins.bottom)),layout.preferredLayoutSize(parent));
		Assert.assertEquals(new Dimension(32767-(ins.left+ins.right),32767-(ins.top+ins.bottom)),layout.maximumLayoutSize(parent));
	}

	@Test
	public void multiLineSizeTest() {
		final JComponent		parent = new JComponent(){};
		final Insets			ins = parent.getInsets();
		final LabelledLayout	layout = new LabelledLayout(2,0,0,LabelledLayout.HORIZONTAL_FILLING);
		final JComponent		left1 = new JComponent(){}, right1 = new JComponent(){};  
		final JComponent		left2 = new JComponent(){}, right2 = new JComponent(){};  
		
		parent.setLayout(layout);
		parent.add(left1,LabelledLayout.LABEL_AREA);
		parent.add(right1,LabelledLayout.CONTENT_AREA);
		parent.add(left2,LabelledLayout.LABEL_AREA);
		parent.add(right2,LabelledLayout.CONTENT_AREA);
		
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.minimumLayoutSize(parent));
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.preferredLayoutSize(parent));
		Assert.assertEquals(new Dimension(32767-(ins.left+ins.right),32767-(ins.top+ins.bottom)),layout.maximumLayoutSize(parent));
		
		left1.setPreferredSize(new Dimension(100,200));
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.minimumLayoutSize(parent));
		Assert.assertEquals(new Dimension(100-(ins.left+ins.right),200-(ins.top+ins.bottom)),layout.preferredLayoutSize(parent));
		Assert.assertEquals(new Dimension(32767-(ins.left+ins.right),32767-(ins.top+ins.bottom)),layout.maximumLayoutSize(parent));

		right1.setPreferredSize(new Dimension(150,300));
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.minimumLayoutSize(parent));
		Assert.assertEquals(new Dimension(250-(ins.left+ins.right),300-(ins.top+ins.bottom)),layout.preferredLayoutSize(parent));
		Assert.assertEquals(new Dimension(32767-(ins.left+ins.right),32767-(ins.top+ins.bottom)),layout.maximumLayoutSize(parent));

		left2.setPreferredSize(new Dimension(100,200));
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.minimumLayoutSize(parent));
		Assert.assertEquals(new Dimension(250-(ins.left+ins.right),500-(ins.top+ins.bottom)),layout.preferredLayoutSize(parent));
		Assert.assertEquals(new Dimension(32767-(ins.left+ins.right),32767-(ins.top+ins.bottom)),layout.maximumLayoutSize(parent));

		right2.setPreferredSize(new Dimension(150,300));
		Assert.assertEquals(new Dimension(0-(ins.left+ins.right),0-(ins.top+ins.bottom)),layout.minimumLayoutSize(parent));
		Assert.assertEquals(new Dimension(250-(ins.left+ins.right),600-(ins.top+ins.bottom)),layout.preferredLayoutSize(parent));
		Assert.assertEquals(new Dimension(32767-(ins.left+ins.right),32767-(ins.top+ins.bottom)),layout.maximumLayoutSize(parent));
	}

	@Test
	public void oneLineArrangeTest() {
		final JComponent		parent = new JComponent(){};
		final LabelledLayout	layout = new LabelledLayout();
		final JComponent		left1 = new JLabel("left1"), right1 = new JTextField();  
		final JComponent		left2 = new JLabel("left2"), right2 = new JTextField();  
		
		parent.setLayout(layout);
		parent.add(left1,LabelledLayout.LABEL_AREA);
		parent.add(right1,LabelledLayout.CONTENT_AREA);
		parent.add(left2,LabelledLayout.LABEL_AREA);
		parent.add(right2,LabelledLayout.CONTENT_AREA);
		
		parent.setSize(800, 600);
		layout.layoutContainer(parent);
		Assert.assertEquals(new Rectangle(0,0,25,300),left1.getBounds());
		Assert.assertEquals(new Rectangle(25,0,775,300),right1.getBounds());
		Assert.assertEquals(new Rectangle(0,300,25,300),left2.getBounds());
		Assert.assertEquals(new Rectangle(25,300,775,300),right2.getBounds());

		parent.setSize(20, 20);
		layout.layoutContainer(parent);
		Assert.assertEquals(new Rectangle(0,0,25,20),left1.getBounds());
		Assert.assertEquals(new Rectangle(25,0,-5,20),right1.getBounds());
		Assert.assertEquals(new Rectangle(0,20,25,20),left2.getBounds());
		Assert.assertEquals(new Rectangle(25,20,-5,20),right2.getBounds());
	}

	@Test
	public void multiLineArrangeTest() {
		final JComponent		parent = new JComponent(){};
		final LabelledLayout	layout = new LabelledLayout(2,0,0,LabelledLayout.HORIZONTAL_FILLING);
		final JComponent		left1 = new JLabel("left1"), right1 = new JTextField();  
		final JComponent		left2 = new JLabel("left2"), right2 = new JTextField();  
		
		parent.setLayout(layout);
		parent.add(left1,LabelledLayout.LABEL_AREA);
		parent.add(right1,LabelledLayout.CONTENT_AREA);
		parent.add(left2,LabelledLayout.LABEL_AREA);
		parent.add(right2,LabelledLayout.CONTENT_AREA);
		
		parent.setSize(800, 600);
		layout.layoutContainer(parent);
		Assert.assertEquals(new Rectangle(0,0,25,20),left1.getBounds());
		Assert.assertEquals(new Rectangle(25,0,375,20),right1.getBounds());
		Assert.assertEquals(new Rectangle(400,0,25,20),left2.getBounds());
		Assert.assertEquals(new Rectangle(425,0,375,20),right2.getBounds());
	}

//	@Test
	public void visualTest() {
		final LabelledLayout	layout = new LabelledLayout(1,5,5,LabelledLayout.VERTICAL_FILLING);
		final JPanel			panel = new JPanel(layout);
		final JLabel			left1 = new JLabel("left1"), left2 = new JLabel("left2 text");
		final JTextField		right1 = new JTextField(), right2 = new JTextField();  
		
		right1.setColumns(20);
		right2.setColumns(30);
		panel.add(left1,LabelledLayout.LABEL_AREA);
		panel.add(right1,LabelledLayout.CONTENT_AREA);
		panel.add(left2,LabelledLayout.LABEL_AREA);
		panel.add(right2,LabelledLayout.CONTENT_AREA);
		panel.setPreferredSize(new Dimension(800,600));
		panel.setSize(800,600);
		
		JOptionPane.showMessageDialog(null,panel);
	}

}
