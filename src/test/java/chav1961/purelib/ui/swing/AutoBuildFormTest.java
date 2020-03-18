package chav1961.purelib.ui.swing;

import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.testing.SwingTestingUtils;
import chav1961.purelib.testing.SwingUnitTest;
import chav1961.purelib.testing.UITestCategory;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.RefreshMode;

public class AutoBuildFormTest {
	private boolean		callListener = false;

	@FunctionalInterface
	private interface CallInterface {
		void process() throws Exception;
	}
	
	@SuppressWarnings("resource")
	@Test
	public void basicTest() throws SyntaxException, LocalizationException, ContentException, MalformedURLException {		
		final PseudoData						pd = new PseudoData();
		final FormManager<Object,PseudoData>	fm = new FormManager<Object,PseudoData>() {
													@Override
													public RefreshMode onField(PseudoData inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
														return RefreshMode.DEFAULT;
													}
										
													@Override
													public LoggerFacade getLogger() {
														return PureLibSettings.CURRENT_LOGGER;
													}
												};
		final ContentMetadataInterface			mdi = ContentModelFactory.forAnnotatedClass(pd.getClass());
		final ActionListener					al = (e)->{callListener = true;};
		
		try(final AutoBuiltForm<PseudoData>		abf = new AutoBuiltForm<>(mdi,PureLibSettings.PURELIB_LOCALIZER,pd,fm)) {
			
			Assert.assertEquals(mdi,abf.getContentModel());
			Assert.assertEquals(fm,abf.getFormManagerAssociated());
			Assert.assertNotNull(abf.getLocalizerAssociated());

			Assert.assertArrayEquals(new String[]{"titleHelpScreen"},abf.getLabelIds());
			Assert.assertArrayEquals(new String[]{"titleHelpScreen"},abf.getModifiableLabelIds());
			
			abf.localeChanged(Locale.getDefault(),Locale.getDefault());
			
			callListener = false;
			abf.addActionListener(al);
			abf.doClick(URI.create("app:action:/PseudoData.calculate"));
			Assert.assertTrue(callListener);
			
			try{abf.addActionListener(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			callListener = false;
			abf.removeActionListener(al);
			abf.doClick(URI.create("app:action:/PseudoData.calculate"));
			Assert.assertFalse(callListener);

			try{abf.removeActionListener(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
 
			try{abf.doClick(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}

		try{new AutoBuiltForm<PseudoData>(null,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.CURRENT_LOGGER,new URL("file:./"),pd,fm,1,true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new AutoBuiltForm<PseudoData>(mdi,null,PureLibSettings.CURRENT_LOGGER,new URL("file:./"),pd,fm,1,true);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new AutoBuiltForm<PseudoData>(mdi,PureLibSettings.PURELIB_LOCALIZER,null,new URL("file:./"),pd,fm,1,true);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new AutoBuiltForm<PseudoData>(mdi,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.CURRENT_LOGGER,new URL("file:./"),null,fm,1,true);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new AutoBuiltForm<PseudoData>(mdi,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.CURRENT_LOGGER,new URL("file:./"),pd,null,1,true);
			Assert.fail("Mandatory exception was not detected (null 6-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new AutoBuiltForm<PseudoData>(mdi,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.CURRENT_LOGGER,new URL("file:./"),pd,fm,0,true);
			Assert.fail("Mandatory exception was not detected (illegal 7-th argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Category(UITestCategory.class)
	@Test
	public void uiTest() throws SyntaxException, ContentException, MalformedURLException, EnvironmentException, InterruptedException, DebuggingException, NullPointerException {		
		final PseudoData						pd = new PseudoData();
		final FormManager<Object,PseudoData>	fm = new FormManager<Object,PseudoData>() {
													@Override
													public RefreshMode onField(PseudoData inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
														return RefreshMode.FIELD_ONLY;
													}
										
													@Override
													public LoggerFacade getLogger() {
														return PureLibSettings.CURRENT_LOGGER;
													}
												};
		final ContentMetadataInterface			mdi = ContentModelFactory.forAnnotatedClass(pd.getClass());
		final ContentNodeMetadata				itemMeta = mdi.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/intValue"))[0];
		final JFrame							root = new JFrame();
		final SwingUnitTest						sut = new SwingUnitTest(root);
		
		pd.date1 = new Date(0);
		pd.file = new File("./"); 
		pd.text = "text"; 
		
		try(final AutoBuiltForm<PseudoData>		abf = new AutoBuiltForm<>(mdi,PureLibSettings.PURELIB_LOCALIZER,pd,fm)) {
			
			root.getContentPane().add(abf);
			root.setVisible(true);
			SwingTestingUtils.syncRequestFocus(root);

			Assert.assertEquals(0,pd.intValue);
			sut.select(URIUtils.removeQueryFromURI(itemMeta.getUIPath()).toString()).keys("123\n");
		} finally {
			root.setVisible(false);
			root.dispose();
		}
		Assert.assertEquals(1230,pd.intValue);
	}	

	@Category(UITestCategory.class)
	@Test
	public void uiStaticTest() throws SyntaxException, ContentException, MalformedURLException, EnvironmentException, InterruptedException, DebuggingException, NullPointerException, InvocationTargetException {		
		final PseudoData						pd = new PseudoData();
		final FormManager<Object,PseudoData>	fm = new FormManager<Object,PseudoData>() {
													@Override
													public RefreshMode onField(PseudoData inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
														return RefreshMode.FIELD_ONLY;
													}
										
													@Override
													public LoggerFacade getLogger() {
														return PureLibSettings.CURRENT_LOGGER;
													}
												};
		final ContentMetadataInterface			mdi = ContentModelFactory.forAnnotatedClass(pd.getClass());
		final JFrame							root = new JFrame();
		
		pd.date1 = new Date(0);
		pd.file = new File("./"); 
		pd.text = "text"; 
		
		try(final AutoBuiltForm<PseudoData>		abf = new AutoBuiltForm<>(mdi,PureLibSettings.PURELIB_LOCALIZER,pd,fm)) {
			
			callUI(()->AutoBuiltForm.ask(root,PureLibSettings.PURELIB_LOCALIZER,abf),root,URI.create(AutoBuiltForm.DEFAULT_OK_BUTTON_NAME));
			callUI(()->AutoBuiltForm.ask(root,PureLibSettings.PURELIB_LOCALIZER,abf,new URI[]{URI.create("app:action:/PseudoData.calculate")}),root,URI.create("app:action:/PseudoData.calculate"));
			callUI(()->AutoBuiltForm.ask(root,PureLibSettings.PURELIB_LOCALIZER,abf,new URI[]{URI.create("app:action:/PseudoData.calculate"),URI.create("app:action:/PseudoData.calculate")}),root,URI.create("app:action:/PseudoData.calculate"));
		} finally {
			root.dispose();
		}
	}

	private static void callUI(final CallInterface runnable, final JFrame form, final URI click) throws InterruptedException, InvocationTargetException, EnvironmentException {
		final SwingUnitTest		sut = new SwingUnitTest(form);
		final CountDownLatch	latch1 = new CountDownLatch(1), latch2 = new CountDownLatch(1);
		
		final Thread	t = new Thread(()->{
								try{runnable.process();
									latch1.countDown();
								} catch (Exception e) {
								} finally {
									latch2.countDown();
								}
						});
		t.start();
		latch1.await(100,TimeUnit.MILLISECONDS);
		sut.await();
		((JButton)SwingUtils.findComponentByName(form,click.toString())).doClick();
		latch2.await(100,TimeUnit.MILLISECONDS);
	}
}
