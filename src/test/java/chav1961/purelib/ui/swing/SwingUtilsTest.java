package chav1961.purelib.ui.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.DummyLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.testing.OrdinalTestCategory;
import chav1961.purelib.testing.SwingTestingUtils;
import chav1961.purelib.testing.SwingUnitTest;
import chav1961.purelib.testing.UITestCategory;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;


public class SwingUtilsTest {
	@Category(OrdinalTestCategory.class)
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
		
		SwingUtils.addPrefix2ComponentNames(root,"test");
		
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
		Assert.assertEquals("testroot{testsub1{testsub11{}testsub12{}}testsub2{testsub21{}testsub22{}}}",sb.toString());

		SwingUtils.addPrefix2ComponentNames(root,"test");
		
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
		Assert.assertEquals("testroot{testsub1{testsub11{}testsub12{}}testsub2{testsub21{}testsub22{}}}",sb.toString());
		

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

		try{SwingUtils.addPrefix2ComponentNames(null,"");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{SwingUtils.addPrefix2ComponentNames(root,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{SwingUtils.addPrefix2ComponentNames(root,"");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
	}
	
	@Category(OrdinalTestCategory.class)
	@Test
	public void prepareRendererTest() throws LocalizationException, SyntaxException {
		final JComponentMonitor 		monitor = new JComponentMonitor(){
											@Override
											public boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponentInterface component, Object... parameters) throws ContentException {
												return true;
											}
										};

		for (Class<?> item : new Class<?>[]{boolean.class,String.class,int.class,double.class,Date.class,ContinueMode.class,File.class,URI.class}) {
			final FieldFormat			format = new FieldFormat(item,"");
			final ContentNodeMetadata	meta = new MutableContentNodeMetadata("name",item,"test",URI.create(Localizer.LOCALIZER_SCHEME+":xml:file:./src/main/resources/chav1961/purelib/i18n/localization.xml"),"testSet1","testSet2","testSet3",format,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"),null);
			final JComponent			component = SwingUtils.prepareRenderer(meta, PureLibSettings.PURELIB_LOCALIZER, format.getContentType(), monitor);
			
			Assert.assertNotNull(component);
			Assert.assertTrue(component instanceof NodeMetadataOwner);
			Assert.assertTrue(component instanceof JComponentInterface);
		}
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void windowPropertiesTest() throws IOException, URISyntaxException, InterruptedException {
		final Dimension	screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final JDialog	dlg = new JDialog();
		
		SwingUtils.centerMainWindow(dlg);
		Assert.assertTrue(dlg.getWidth() >= screenSize.getWidth()/2);
		Assert.assertTrue(dlg.getHeight() >= screenSize.getHeight()/2);
		Assert.assertTrue(dlg.getX() <= screenSize.getWidth()/4);
		Assert.assertTrue(dlg.getY() <= screenSize.getHeight()/4);
		
		try{SwingUtils.centerMainWindow((JDialog)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.centerMainWindow(dlg,2.0f);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		final JFrame	frame = new JFrame();
		
		SwingUtils.centerMainWindow(frame);
		Assert.assertTrue(frame.getWidth() >= 3*screenSize.getWidth()/4 - 1);
		Assert.assertTrue(frame.getHeight() >= 3*screenSize.getHeight()/4 - 1);
		Assert.assertTrue(frame.getX() <= screenSize.getWidth()/8);
		Assert.assertTrue(frame.getY() <= screenSize.getHeight()/8);
		
		try{SwingUtils.centerMainWindow((JFrame)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.centerMainWindow(frame,2.0f);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(new Point(0,0),SwingUtils.locateRelativeToAnchor(0,0,100,100));
		Assert.assertTrue(SwingUtils.locateRelativeToAnchor(screenSize.width,screenSize.height,100,100).x < screenSize.width);
		Assert.assertTrue(SwingUtils.locateRelativeToAnchor(screenSize.width,screenSize.height,100,100).y < screenSize.height);
	}
	
	
//	@Test
	public void showCreoleHelpWindowTest() throws IOException, URISyntaxException, InterruptedException {
		final JDialog	dlg = new JDialog();
		
		SwingUtils.showCreoleHelpWindow(dlg,SwingUtilsTest.class.getResource("helpcontent.cre").toURI());
		Thread.sleep(5000);
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void prepareHtmlMessageTest() throws IOException, URISyntaxException, InterruptedException, LocalizationException {
		Assert.assertEquals("<html><body><font color=gray>test string</font></body></html>",SwingUtils.prepareHtmlMessage(Severity.debug,"test %1$s","string"));
		Assert.assertEquals("<html><body><font color=red>test string</font></body></html>",SwingUtils.prepareHtmlMessage(Severity.error,"test %1$s","string"));
		Assert.assertEquals("<html><body><font color=black>test string</font></body></html>",SwingUtils.prepareHtmlMessage(Severity.info,"test %1$s","string"));
		Assert.assertEquals("<html><body><font color=red><b>test string</b></font></body></html>",SwingUtils.prepareHtmlMessage(Severity.severe,"test %1$s","string"));
		Assert.assertEquals("<html><body><font color=gray><i>test string</i></font></body></html>",SwingUtils.prepareHtmlMessage(Severity.trace,"test %1$s","string"));
		Assert.assertEquals("<html><body><font color=blue>test string</font></body></html>",SwingUtils.prepareHtmlMessage(Severity.warning,"test %1$s","string"));
		Assert.assertEquals("<html><body><font color=black>test string</font></body></html>",SwingUtils.prepareHtmlMessage(Severity.tooltip,"test string"));
		
		try{SwingUtils.prepareHtmlMessage(null,"test string");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.prepareHtmlMessage(Severity.tooltip,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Category(OrdinalTestCategory.class)
	@Test
	public void refreshLocaleTest() throws IOException, URISyntaxException, InterruptedException, LocalizationException {
		final JFrame		frame = new JFrame();
		final JTextField	field = new JTextField();
		
		frame.getContentPane().add(field);
		SwingUtils.refreshLocale(frame,Locale.getDefault(),Locale.getDefault());
		
		try{SwingUtils.refreshLocale(null,Locale.getDefault(),Locale.getDefault());
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.refreshLocale(frame,null,Locale.getDefault());
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.refreshLocale(frame,Locale.getDefault(),null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void getSignum4ValueTest() throws IOException, URISyntaxException, InterruptedException, LocalizationException {
		Assert.assertEquals(-1,SwingUtils.getSignum4Value(Integer.valueOf(-1)));
		Assert.assertEquals(-1,SwingUtils.getSignum4Value(BigInteger.valueOf(-1)));
		Assert.assertEquals(-1,SwingUtils.getSignum4Value(new BigDecimal(-1)));
		
		try{SwingUtils.getSignum4Value(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.getSignum4Value("100");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument type)");
		} catch (IllegalArgumentException exc) {
		} 
	}
	
	@Category(UITestCategory.class)
	@Test
	public void uiAssignActionKeyTest() throws IOException, URISyntaxException, InterruptedException, EnvironmentException, DebuggingException {
		final JButton			button = new JButton();
		final AtomicBoolean		bool = new AtomicBoolean(false);
		final CountDownLatch	latch = new CountDownLatch(1);
		final JFrame			root = new JFrame();
		final SwingUnitTest		sut = new SwingUnitTest(root);
		final ActionListener	al = (e)->{bool.set(true); latch.countDown();};

		try{button.setName("button");
			root.getContentPane().add(button);
			SwingUtils.assignActionKey(button,SwingUtils.KS_ACCEPT,al,"click");
		
			root.setVisible(true);
			SwingTestingUtils.syncRequestFocus(root);

			Assert.assertFalse(bool.get());
			sut.select("button").keys(SwingUtils.KS_ACCEPT);
			Assert.assertTrue(latch.await(1000,TimeUnit.MILLISECONDS));
			Assert.assertTrue(bool.get());
			
			SwingUtils.removeActionKey(button,SwingUtils.KS_ACCEPT,"click");
			
			root.setVisible(false);
		} finally {
			root.dispose();
		}
		
		try{SwingUtils.assignActionKey(null,SwingUtils.KS_ACCEPT,al,"click");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.assignActionKey(button,null,al,"click");
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.assignActionKey(button,SwingUtils.KS_ACCEPT,null,"click");
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.assignActionKey(button,SwingUtils.KS_ACCEPT,al,null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SwingUtils.assignActionKey(button,SwingUtils.KS_ACCEPT,al,"");
			Assert.fail("Mandatory exception was not detected (empty 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{SwingUtils.removeActionKey(null,SwingUtils.KS_ACCEPT,"click");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.removeActionKey(button,null,"click");
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.removeActionKey(button,SwingUtils.KS_ACCEPT,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{SwingUtils.removeActionKey(button,SwingUtils.KS_ACCEPT,"");
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiAssignExitMethod4MainWindowTest() throws IOException, URISyntaxException, InterruptedException, EnvironmentException, DebuggingException {
		final AtomicBoolean		bool = new AtomicBoolean(false);
		final CountDownLatch	latch = new CountDownLatch(1);
		final JFrame			root = new JFrame();
		final WindowEvent 		queryClose = new WindowEvent(root, WindowEvent.WINDOW_CLOSING);

		try{SwingUtils.assignExitMethod4MainWindow(root,()->{bool.set(true); latch.countDown();});
		
			root.setVisible(true);
			SwingTestingUtils.syncRequestFocus(root);

			Assert.assertFalse(bool.get());
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(queryClose);			
			Assert.assertTrue(latch.await(1000,TimeUnit.MILLISECONDS));
			Assert.assertTrue(bool.get());
		} finally {
			root.dispose();
		}
		
		try{SwingUtils.assignExitMethod4MainWindow(null,()->{bool.set(true); latch.countDown();});
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.assignExitMethod4MainWindow(root,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Category(OrdinalTestCategory.class)
	@Test
	public void annotatedActionListenersTest() throws IOException, URISyntaxException, InterruptedException, EnvironmentException, DebuggingException {
		final AnnotatedWithOnAction	test = new AnnotatedWithOnAction();
		final ActionListener		listener = SwingUtils.buildAnnotatedActionListener(test);

		test.clear();
		Assert.assertFalse(test.wasCall1);
		Assert.assertFalse(test.wasCall2);
		
		listener.actionPerformed(new ActionEvent(this,0,"action1"));

		Assert.assertTrue(test.wasCall1);
		Assert.assertFalse(test.wasCall2);
		
		test.clear();
		listener.actionPerformed(new ActionEvent(this,0,"action2"));
		Assert.assertFalse(test.wasCall1);
		Assert.assertTrue(test.sema.tryAcquire(1000,TimeUnit.MILLISECONDS));
		Assert.assertTrue(test.wasCall2);
		
//		test.clear();
//		listener.actionPerformed(new ActionEvent(this,0,"unknown"));
//		Assert.assertFalse(test.wasCall1);
//		Assert.assertFalse(test.wasCall2);
		
		try{SwingUtils.buildAnnotatedActionListener(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.buildAnnotatedActionListener("test");
			Assert.fail("Mandatory exception was not detected (1-st argument doesn't mark with OnAction annotation)");
		} catch (IllegalArgumentException exc) {
		}

		try{SwingUtils.buildAnnotatedActionListener(test,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		try{SwingUtils.buildAnnotatedActionListener(test,(e)->{},null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}	

	@Category(UITestCategory.class)
	@Test
	public void uiActionListenersTest() throws IOException, URISyntaxException, InterruptedException, EnvironmentException, DebuggingException {
		final AnnotatedWithOnAction	test = new AnnotatedWithOnAction();
		final JFrame				root = new JFrame();
		final JButton				button = new JButton();
		final SwingUnitTest			sut = new SwingUnitTest(root);
		final FormManager<Object,AnnotatedWithOnAction>	mgr = new FormManager<Object,AnnotatedWithOnAction>() {
										@Override
										public RefreshMode onField(final AnnotatedWithOnAction inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
											return RefreshMode.DEFAULT;
										}
							
										@Override
										public RefreshMode onAction(final AnnotatedWithOnAction inst, final Object id, final String actionName, final Object parameter) throws FlowException, LocalizationException {
											inst.call3();
											return RefreshMode.DEFAULT;
										}
										
										@Override
										public LoggerFacade getLogger() {
											return null;
										}
									};

		try{button.setName("button");
			button.setActionCommand("action1");
			root.getContentPane().add(button);	
			
			SwingUtils.assignActionListeners((JComponent)root.getContentPane(),test);
			
			root.setVisible(true);
			SwingTestingUtils.syncRequestFocus(root);

			test.clear();
			Assert.assertFalse(test.wasCall1);
			((JButton)sut.select("button").getLastFound()).doClick();
			Assert.assertTrue(test.wasCall1);

			SwingUtils.assignActionListeners((JComponent)root.getContentPane(),test,mgr);

			button.setActionCommand("action3");
			test.clear();
			Assert.assertFalse(test.wasCall3);
			((JButton)sut.getLastFound()).doClick();
			Assert.assertTrue(test.wasCall3);
			
			root.setVisible(false);
		} finally {
			root.dispose();
		}
		
		try{SwingUtils.assignActionListeners(null,test);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.assignActionListeners((JComponent)root.getContentPane(),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		
		try{SwingUtils.assignActionListeners(null,test,mgr);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.assignActionListeners((JComponent)root.getContentPane(),null,mgr);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.assignActionListeners((JComponent)root.getContentPane(),test,(FormManager<Object,AnnotatedWithOnAction>)null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Category(OrdinalTestCategory.class)
	@Test
	public void getAndPutValuesTest() throws IOException, URISyntaxException, InterruptedException, EnvironmentException, DebuggingException, SyntaxException, NullPointerException, PreparationException, IllegalArgumentException, ContentException {
		final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(PseudoData.class);
		final ContentNodeMetadata		itemMeta = mdi.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/intValue"))[0];
		final PseudoData				instance = new PseudoData();
		final JComponentMonitor			mon = (event, md, component, parameters)->{return true;};
		final JPanel					panel = new JPanel();
		final JIntegerFieldWithMeta		field = new JIntegerFieldWithMeta(itemMeta, mon);
		
		panel.add(field);
		
		Assert.assertEquals("",field.getText());
		instance.intValue = 123;
		SwingUtils.putToScreen(itemMeta,instance,panel);
		Assert.assertEquals("123",field.getText());
		
		Assert.assertEquals(123,instance.intValue);
		field.setValue(456);
		SwingUtils.getFromScreen(itemMeta,panel,instance);
		Assert.assertEquals(456,instance.intValue);
		
		try{SwingUtils.putToScreen(null,instance,panel);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.putToScreen(itemMeta,null,panel);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.putToScreen(itemMeta,"",panel);
			Assert.fail("Mandatory exception was not detected (2-nd argument is not annotated with @LocaleResource)");
		} catch (IllegalArgumentException exc) {
		}
		try{SwingUtils.putToScreen(itemMeta,instance,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		try{SwingUtils.getFromScreen(null,panel,instance);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.getFromScreen(itemMeta,null,instance);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.getFromScreen(itemMeta,panel,null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.getFromScreen(itemMeta,panel,"");
			Assert.fail("Mandatory exception was not detected (3-rd argument is not annotated with @LocaleResource)");
		} catch (IllegalArgumentException exc) {
		}
	}	

	@Category(OrdinalTestCategory.class)
	@Test
	public void navigationBuildTest() throws IOException, URISyntaxException, InterruptedException, EnvironmentException, DebuggingException, SyntaxException, NullPointerException, PreparationException, IllegalArgumentException, ContentException {
		final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(this.getClass().getResourceAsStream("Application.xml"));
		JComponent						value = null;

		Assert.assertTrue((value = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")),JMenuBar.class)) instanceof JMenuBar);
		((LocaleChangeListener)value).localeChanged(Locale.getDefault(),Locale.getDefault());
		Assert.assertTrue((value = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")),JPopupMenu.class)) instanceof JPopupMenu);
		((LocaleChangeListener)value).localeChanged(Locale.getDefault(),Locale.getDefault());
		Assert.assertTrue((value = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.toolbar")),JToolBar.class)) instanceof JToolBar);
		((LocaleChangeListener)value).localeChanged(Locale.getDefault(),Locale.getDefault());
		
		try{SwingUtils.toJComponent(null,JMenuBar.class);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")),JLabel.class);
			Assert.fail("Mandatory exception was not detected (illegal 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
}


