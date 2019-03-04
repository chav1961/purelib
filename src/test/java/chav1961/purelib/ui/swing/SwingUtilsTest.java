package chav1961.purelib.ui.swing;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.enumerations.ContinueMode;


public class SwingUtilsTest {
	
	@Test
	public void walkingTest() {
		final StringBuilder	sb = new StringBuilder();
		final JPanel		root = new JPanel();
		final JPanel		sub1 = new JPanel(), sub2 = new JPanel();
		final JPanel		sub11 = new JPanel(), sub12 = new JPanel(), sub21 = new JPanel(), sub22 = new JPanel();
	
		root.setName("root");
		sub1.setName("sub1");	sub2.setName("sub2");
		sub11.setName("sub11");	sub12.setName("sub12");	sub21.setName("sub21");	sub22.setName("sub22");
		sub1.add(sub11);		sub1.add(sub12);		sub2.add(sub21);		sub2.add(sub22);
		root.add(sub1);			root.add(sub2);
		
		sb.setLength(0);
		SwingUtils.walkDown(root,(mode,component)->{
			switch (mode) {
				case ENTER	:
					sb.append(component.getName()).append('{');
					break;
				case EXIT	:
					sb.append('}');
					break;
			}
			return	ContinueMode.CONTINUE;
		});
		Assert.assertEquals("root{sub1{sub11{}sub12{}}sub2{sub21{}sub22{}}}",sb.toString());
		
		Assert.assertEquals(sub22,SwingUtils.findComponentByName(root,"sub22"));
		Assert.assertNull(SwingUtils.findComponentByName(root,"unknown"));

		final JMenu			rootMenu = new JMenu("root");
		final JMenu			sub1Menu = new JMenu("sub1"), sub2Menu = new JMenu("sub2");
		final JMenuItem		sub11Menu = new JMenuItem("sub11"), sub12Menu = new JMenuItem("sub12"), sub21Menu = new JMenuItem("sub21"), sub22Menu = new JMenuItem("sub22");

		sub1Menu.add(sub11Menu);	sub1Menu.add(sub12Menu);	sub2Menu.add(sub21Menu);	sub2Menu.add(sub22Menu);
		rootMenu.add(sub1Menu);		rootMenu.add(sub2Menu);
		
		sb.setLength(0);
		SwingUtils.walkDown(rootMenu,(mode,component)->{
			switch (mode) {
				case ENTER	:
					sb.append(((JMenuItem)component).getText()).append('{');
					break;
				case EXIT	:
					sb.append('}');
					break;
			}
			return	ContinueMode.CONTINUE;
		});
		Assert.assertEquals("root{sub1{sub11{}sub12{}}sub2{sub21{}sub22{}}}",sb.toString());
		
		try{SwingUtils.walkDown(null,(mode,component)->{return ContinueMode.CONTINUE;});
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{SwingUtils.walkDown(rootMenu,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {			
		}

		try{SwingUtils.findComponentByName(null,"name");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{SwingUtils.findComponentByName(root,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{SwingUtils.findComponentByName(root,"");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
	}
}
