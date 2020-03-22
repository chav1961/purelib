package chav1961.purelib.ui.swing;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Locale;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
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
import chav1961.purelib.testing.OrdinalTestCategory;
import chav1961.purelib.testing.SwingTestingUtils;
import chav1961.purelib.testing.SwingUnitTest;
import chav1961.purelib.testing.UITestCategory;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.RefreshMode;

public class MarkupBuildFormTest {
	@Category(OrdinalTestCategory.class)
	@Test
	public void basicTest() throws LocalizationException, ContentException, IOException {
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
		final String							desc = Utils.fromResource(this.getClass().getResource("testform.txt"));
		
		try(final MarkupBuiltForm<PseudoData>	mbf = new MarkupBuiltForm<>(mdi,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.CURRENT_LOGGER,desc,pd,fm,true)) {
			
			mbf.localeChanged(Locale.getDefault(),Locale.getDefault());
			
			Assert.assertEquals(PureLibSettings.PURELIB_LOCALIZER,mbf.getLocalizerAssociated());
			Assert.assertEquals(fm,mbf.getFormManagerAssociated());
			Assert.assertEquals(mdi,mbf.getContentModel());
		}		
	}
	
	@Category(UITestCategory.class)
	@Test
	public void uiTest() throws ContentException, EnvironmentException, InterruptedException, DebuggingException, IOException {		
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
		final ContentNodeMetadata				itemMeta = mdi.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD+":/"+PseudoData.class.getCanonicalName()+"/intValue"))[0];
		final String							desc = Utils.fromResource(this.getClass().getResource("testform.txt"));
		final JFrame							root = new JFrame();
		final SwingUnitTest						sut = new SwingUnitTest(root);
		
		pd.date1 = new Date(0);
		pd.file = new File("./"); 
		pd.text = "text"; 
		
		try(final MarkupBuiltForm<PseudoData>	mbf = new MarkupBuiltForm<>(mdi,PureLibSettings.PURELIB_LOCALIZER,PureLibSettings.CURRENT_LOGGER,desc,pd,fm,true)) {
			
			root.getContentPane().add(mbf);
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
}
