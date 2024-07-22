package chav1961.purelib.ui.swing.useful;


import java.awt.event.MouseEvent;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.testing.SwingTestingUtils;
import chav1961.purelib.testing.SwingUnitTest;
import chav1961.purelib.ui.swing.useful.JPaginator.PageMoving;
import chav1961.purelib.ui.swing.useful.JPaginator.PageSelectCallback;

public class JPaginatorTest {
	@Tag("OrdinalTestCategory")
	@Test
	public void basicTest() throws LocalizationException {
		final int[]					pages = new int[3];
		final PageSelectCallback	psc = new PageSelectCallback() {
										@Override
										public void selectPage(final int oldPage, final int newPage, final int totalPages) {
											pages[0] = oldPage;
											pages[1] = newPage;
											pages[2] = totalPages;
										}

										@Override
										public String getPageCaption(final int pageNo) {
											return "testSet1";
										}
										
										@Override
										public String getPageTooltip(final int pageNo) {
											return "testSet2";
										}
									};
		final JPanel				page1 = new JPanel(), page2 = new JPanel(), page3 = new JPanel();
		final JPaginator			p = new JPaginator(PureLibSettings.PURELIB_LOCALIZER,true,psc,page1,page2,page3);
		
		Assert.assertEquals(1,p.getCurrentPage());
		Assert.assertEquals(3,p.getTotalPages());

		p.localeChanged(Locale.getDefault(),Locale.getDefault());
		
		p.movePage(PageMoving.LAST);
		Assert.assertEquals(3,p.getCurrentPage());
		Assert.assertArrayEquals(new int[]{1,3,3},pages);
		
		p.movePage(PageMoving.NEXT);
		Assert.assertEquals(3,p.getCurrentPage());
		Assert.assertArrayEquals(new int[]{3,3,3},pages);

		p.movePage(PageMoving.FIRST);
		Assert.assertEquals(1,p.getCurrentPage());
		Assert.assertArrayEquals(new int[]{3,1,3},pages);

		p.movePage(PageMoving.PREV);
		Assert.assertEquals(1,p.getCurrentPage());
		Assert.assertArrayEquals(new int[]{1,1,3},pages);

		p.movePage(PageMoving.NEXT);
		Assert.assertEquals(2,p.getCurrentPage());
		Assert.assertArrayEquals(new int[]{1,2,3},pages);

		p.movePage(PageMoving.PREV);
		Assert.assertEquals(1,p.getCurrentPage());
		Assert.assertArrayEquals(new int[]{2,1,3},pages);

		final JPaginator			p1 = new JPaginator(PureLibSettings.PURELIB_LOCALIZER,true,psc,page1);
		
		Assert.assertEquals(1,p1.getCurrentPage());
		Assert.assertEquals(1,p1.getTotalPages());
		
		p1.movePage(PageMoving.LAST);
		Assert.assertEquals(1,p1.getCurrentPage());
		Assert.assertArrayEquals(new int[]{1,1,1},pages);

		p1.movePage(PageMoving.FIRST);
		Assert.assertEquals(1,p1.getCurrentPage());
		Assert.assertArrayEquals(new int[]{1,1,1},pages);
		
		try{new JPaginator(null,true,psc,page1,page2,page3);
			Assert.fail("Mandatory exception was not detetced (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new JPaginator(PureLibSettings.PURELIB_LOCALIZER,true,null,page1,page2,page3);
			Assert.fail("Mandatory exception was not detetced (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new JPaginator(PureLibSettings.PURELIB_LOCALIZER,true,psc,(JComponent[])null);
			Assert.fail("Mandatory exception was not detetced (null 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new JPaginator(PureLibSettings.PURELIB_LOCALIZER,true,psc);
			Assert.fail("Mandatory exception was not detetced (empty 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new JPaginator(PureLibSettings.PURELIB_LOCALIZER,true,psc,page1,null);
			Assert.fail("Mandatory exception was not detetced (nulls inside 4-st argument)");
		} catch (NullPointerException exc) {
		}

		try{p.movePage(null);
			Assert.fail("Mandatory exception was not detetced (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Tag("UITestCategory")
	@Test
	public void uiTest() throws EnvironmentException, InterruptedException, DebuggingException {
		final int[]					pages = new int[3];
		final PageSelectCallback	psc = new PageSelectCallback() {
										@Override
										public void selectPage(final int oldPage, final int newPage, final int totalPages) {
											pages[0] = oldPage;
											pages[1] = newPage;
											pages[2] = totalPages;
										}

										@Override
										public String getPageCaption(final int pageNo) {
											return "testSet"+pageNo;
										}
										
										@Override
										public String getPageTooltip(final int pageNo) {
											return "testSet"+pageNo;
										}
									};
		final JPanel				page1 = new JPanel(), page2 = new JPanel(), page3 = new JPanel();
		final JTextField			tf1 = new JTextField(), tf2 = new JTextField(), tf3 = new JTextField();   

		tf1.setName("tf1");			page1.add(tf1);//		page1.add(new JLabel("p1"));
		tf2.setName("tf2");			page2.add(tf2);//		page2.add(new JLabel("p2"));
		tf3.setName("tf3");			page3.add(tf3);//		page3.add(new JLabel("p3"));

		final JFrame				root = new JFrame();
		final JPaginator			p = new JPaginator(PureLibSettings.PURELIB_LOCALIZER,true,psc,page1,page2,page3);
		final SwingUnitTest			sut = new SwingUnitTest(root);
		
		root.getContentPane().add(p);
		root.setSize(200,200);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertEquals(1,p.getCurrentPage());
		
		sut.select("tf1").keys("VALUE\n").await();
		Assert.assertEquals(2,p.getCurrentPage());

		sut.select("tf2").keys("VALUE\n").await(); 
		Assert.assertEquals(3,p.getCurrentPage());

		sut.select("tf3").keys("VALUE\n").await();
		Assert.assertEquals(3,p.getCurrentPage());
		
		root.setVisible(false);
	}
}
