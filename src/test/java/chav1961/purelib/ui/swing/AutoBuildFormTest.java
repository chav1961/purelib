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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

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
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.RefreshMode;

public class AutoBuildFormTest {
	private boolean		callListener = false;

	@SuppressWarnings("resource")
	@Tag("OrdinalTestCategory")
	@Test
	public void basicTest() throws SyntaxException, LocalizationException, ContentException, MalformedURLException {
		try {
		final PseudoData						pd = new PseudoData();
		final FormManager<Object,PseudoData>	fm = new FormManager<Object,PseudoData>() {
													@Override
													public RefreshMode onField(PseudoData inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
														return RefreshMode.DEFAULT;
													}
										
													@Override
													public LoggerFacade getLogger() {
														return PureLibSettings.CURRENT_LOGGER;
													}
												};
		final ContentMetadataInterface			mdi = ContentModelFactory.forAnnotatedClass(pd.getClass());
		final ActionListener					al = (e)->{callListener = true;};
		
		try(final AutoBuiltForm<PseudoData,?>	abf = new AutoBuiltForm<>(mdi,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.INTERNAL_LOADER,pd,fm)) {
			
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
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}

		try{new AutoBuiltForm<PseudoData,Object>(null,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.CURRENT_LOGGER,PureLibSettings.INTERNAL_LOADER,new URL("file:./"),pd,fm,1,true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new AutoBuiltForm<PseudoData,Object>(mdi,null,PureLibSettings.CURRENT_LOGGER,PureLibSettings.INTERNAL_LOADER,new URL("file:./"),pd,fm,1,true);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new AutoBuiltForm<PseudoData,Object>(mdi,PureLibSettings.PURELIB_LOCALIZER,null,PureLibSettings.INTERNAL_LOADER,new URL("file:./"),pd,fm,1,true);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new AutoBuiltForm<PseudoData,Object>(mdi,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.CURRENT_LOGGER,PureLibSettings.INTERNAL_LOADER,new URL("file:./"),null,fm,1,true);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new AutoBuiltForm<PseudoData,Object>(mdi,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.CURRENT_LOGGER,PureLibSettings.INTERNAL_LOADER,new URL("file:./"),pd,null,1,true);
			Assert.fail("Mandatory exception was not detected (null 6-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new AutoBuiltForm<PseudoData,Object>(mdi,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.CURRENT_LOGGER,PureLibSettings.INTERNAL_LOADER,new URL("file:./"),pd,fm,0,true);
			Assert.fail("Mandatory exception was not detected (illegal 7-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}
	
	@Tag("UITestCategory")
	@Test
	public void uiTest() throws SyntaxException, ContentException, MalformedURLException, EnvironmentException, InterruptedException, DebuggingException, NullPointerException {		
		final PseudoData						pd = new PseudoData();
		final FormManager<Object,PseudoData>	fm = new FormManager<Object,PseudoData>() {
													@Override
													public RefreshMode onField(PseudoData inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
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
		
		try(final AutoBuiltForm<PseudoData,?>	abf = new AutoBuiltForm<>(mdi,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.INTERNAL_LOADER,pd,fm)) {
			
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

	@Tag("UITestCategory")
	@Test
	public void uiStaticTest() throws SyntaxException, ContentException, MalformedURLException, EnvironmentException, InterruptedException, DebuggingException, NullPointerException, InvocationTargetException {		
		final PseudoData						pd = new PseudoData();
		final FormManager<Object,PseudoData>	fm = new FormManager<Object,PseudoData>() {
													@Override
													public RefreshMode onField(PseudoData inst, Object id, String fieldName, Object oldValue, boolean beforeCommit) throws FlowException, LocalizationException {
														return RefreshMode.FIELD_ONLY;
													}
										
													@Override
													public LoggerFacade getLogger() {
														return PureLibSettings.CURRENT_LOGGER;
													}
												};
		final ContentMetadataInterface			mdi = ContentModelFactory.forAnnotatedClass(pd.getClass());
		
		pd.date1 = new Date(0);
		pd.file = new File("./"); 
		pd.text = "text"; 
		
		try(final AutoBuiltForm<PseudoData,?>	abf = new AutoBuiltForm<>(mdi,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.INTERNAL_LOADER,pd,fm)) {
			
			callUI(abf,new URI[0],URI.create(AutoBuiltForm.DEFAULT_OK_BUTTON_NAME));
			callUI(abf,new URI[]{URI.create("app:action:/PseudoData.calculate")},URI.create("ui:/chav1961.purelib.ui.swing.PseudoData/./PseudoData.calculate"));
			callUI(abf,new URI[]{URI.create("app:action:/PseudoData.calculate"),URI.create("app:action:/PseudoData.calculate")},URI.create("ui:/chav1961.purelib.ui.swing.PseudoData/./PseudoData.calculate"));
		}
	}

	private static void callUI(final AutoBuiltForm<PseudoData,?> abf, final URI[] buttons, final URI click) throws InterruptedException, EnvironmentException {
		final CountDownLatch	cdl = new CountDownLatch(1);	// Protection against infinite wait in the test
		final Thread			t = new Thread(()->{
									JDialog	frame = null;
									
									try{frame = new JDialog((JFrame)null, true) {
														@Override
														public void setVisible(final boolean visible) {
															super.setVisible(visible);
															if (visible) {
																final JButton	btn = (JButton)SwingUtils.findComponentByName(this, click.toString());
																
																Assert.assertNotNull(btn);
																SwingUtilities.invokeLater(()->btn.doClick());
															}
														}
												};
													
										AutoBuiltForm.askInternal((JFrame)null, frame, PureLibSettings.PURELIB_LOCALIZER, abf, buttons, "");
									} catch (Exception e) {
										Assert.fail("Exception detected: "+e.getLocalizedMessage());
									} finally {
										cdl.countDown();
										frame.dispose();
									}
								});

		t.setDaemon(true);
		t.start();
		Assert.assertTrue(cdl.await(2000,TimeUnit.MILLISECONDS));
	}
}
