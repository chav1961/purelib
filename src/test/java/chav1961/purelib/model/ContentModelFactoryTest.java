package chav1961.purelib.model;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.Action;
import chav1961.purelib.ui.interfaces.Format;

public class ContentModelFactoryTest {
	@Test
	public void xmlDescriptionTest() throws IOException, EnvironmentException {
		try(final InputStream	is = this.getClass().getResourceAsStream("modelTest1.xml")) {
			
			final ContentMetadataInterface 	cmi = ContentModelFactory.forXmlDescription(is);
			
			Assert.assertEquals("root",cmi.getRoot().getName());
			Assert.assertEquals(URI.create("./model"),cmi.getRoot().getRelativeUIPath());
			Assert.assertEquals(URI.create("ui:/model"),cmi.getRoot().getUIPath());
			Assert.assertEquals(URI.create("app:/"),cmi.getRoot().getApplicationPath());
			Assert.assertEquals(URI.create("i18n:prop:chav1961/purelib/i18n/i18n"),cmi.getRoot().getLocalizerAssociated());
			Assert.assertEquals(2,cmi.getRoot().getChildrenCount());
			Assert.assertNull(cmi.getRoot().getFormatAssociated());
			Assert.assertEquals(org.w3c.dom.Document.class,cmi.getRoot().getType());
			Assert.assertEquals(cmi,cmi.getRoot().getOwner());
			Assert.assertNull(cmi.getRoot().getParent());
			Assert.assertEquals("root",cmi.getRoot().getLabelId());
			Assert.assertNull(cmi.getRoot().getTooltipId());
			Assert.assertNull(cmi.getRoot().getHelpId());
			 
			final ContentNodeMetadata	menu = cmi.byUIPath(URI.create("ui:/model/navigation.top.mainMenu"));
			
			Assert.assertNotNull(menu);
			Assert.assertEquals("mainMenu",menu.getName());
			Assert.assertEquals(URI.create("./navigation.top.mainMenu?keyset=mainMenuKeyset"),menu.getRelativeUIPath());
			Assert.assertEquals(URI.create("ui:/model/navigation.top.mainMenu?keyset=mainMenuKeyset"),menu.getUIPath());
			Assert.assertNull(menu.getApplicationPath());
			Assert.assertEquals(URI.create("i18n:prop:chav1961/purelib/i18n/i18n"),menu.getLocalizerAssociated());
			Assert.assertEquals(1,menu.getChildrenCount());
			Assert.assertNull(menu.getFormatAssociated());
			Assert.assertEquals(String.class,menu.getType());
			Assert.assertEquals(cmi,menu.getOwner());
			Assert.assertNotNull(menu.getParent());
			Assert.assertEquals("mainMenu",menu.getLabelId());
			Assert.assertNull(menu.getTooltipId());
			Assert.assertNull(menu.getHelpId());

			final ContentNodeMetadata	item = cmi.byUIPath(URI.create("ui:/model/navigation.top.mainMenu/navigation.node.submenu/navigation.leaf.menuItem1"));
			
			Assert.assertNotNull(item);
			Assert.assertEquals("menuItem1",item.getName());
			Assert.assertEquals(URI.create("./navigation.leaf.menuItem1"),item.getRelativeUIPath());
			Assert.assertEquals(URI.create("ui:/model/navigation.top.mainMenu/navigation.node.submenu/navigation.leaf.menuItem1"),item.getUIPath());
			Assert.assertEquals(URI.create("app:action:/menuItem1Action"),item.getApplicationPath());
			Assert.assertEquals(URI.create("i18n:prop:chav1961/purelib/i18n/i18n"),item.getLocalizerAssociated());
			Assert.assertEquals(0,item.getChildrenCount());
			Assert.assertNull(item.getFormatAssociated());
			Assert.assertEquals(String.class,item.getType());
			Assert.assertEquals(cmi,item.getOwner());
			Assert.assertNotNull(item.getParent());
			Assert.assertEquals("menuItem1Caption",item.getLabelId());
			Assert.assertEquals("menuItem1Tooltip",item.getTooltipId());
			Assert.assertNull(item.getHelpId());

			try{ContentModelFactory.forXmlDescription(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {				
			}
			 
			int	count = 0; 
			for (@SuppressWarnings("unused") ContentNodeMetadata found : cmi.byApplicationPath(URI.create("app:action:/menuItem1Action"))) {
				count++;
			}
			Assert.assertEquals(2,count); 
						
			final int 	countArray[] = new int[]{0};
			cmi.walkDown((mode,applicationPath,uiPath,node)->{countArray[0]++; return ContinueMode.CONTINUE;},URI.create("ui:/model"));
			Assert.assertEquals(16,countArray[0]);
		}

		try(final InputStream	is = this.getClass().getResourceAsStream("modelTest2.xml")) {
			final ContentMetadataInterface 	cmi = ContentModelFactory.forXmlDescription(is);
			
			Assert.assertEquals("class",cmi.getRoot().getName());
			Assert.assertEquals(URI.create("./"+AnnotatedForTest.class.getName()),cmi.getRoot().getRelativeUIPath());
			Assert.assertEquals(URI.create("ui:/"+AnnotatedForTest.class.getName()),cmi.getRoot().getUIPath());
			Assert.assertEquals(URI.create("app:class:/"+AnnotatedForTest.class.getName()),cmi.getRoot().getApplicationPath());
			Assert.assertEquals(URI.create("i18n:prop:chav1961/purelib/i18n/i18n"),cmi.getRoot().getLocalizerAssociated());
			Assert.assertEquals(2,cmi.getRoot().getChildrenCount());
			Assert.assertNull(cmi.getRoot().getFormatAssociated());
			Assert.assertEquals(AnnotatedForTest.class,cmi.getRoot().getType());
			Assert.assertEquals(cmi,cmi.getRoot().getOwner());
			Assert.assertNull(cmi.getRoot().getParent());
			Assert.assertEquals("testLabel",cmi.getRoot().getLabelId());
			Assert.assertEquals("testTooltip",cmi.getRoot().getTooltipId());
			Assert.assertEquals("testHelp",cmi.getRoot().getHelpId());
			
			ContentNodeMetadata		item= cmi.byUIPath(URI.create("ui:/"+AnnotatedForTest.class.getCanonicalName()+"/testSet1/float"));

			Assert.assertNotNull(item);
			Assert.assertEquals("testSet1",item.getName());
			Assert.assertEquals(URI.create("./testSet1/float"),item.getRelativeUIPath());
			Assert.assertEquals(URI.create("ui:/"+AnnotatedForTest.class.getCanonicalName()+"/testSet1/float"),item.getUIPath());
			Assert.assertEquals(URI.create("app:field:/"+AnnotatedForTest.class.getCanonicalName()+"/testSet1"),item.getApplicationPath());
			Assert.assertEquals(URI.create("i18n:prop:chav1961/purelib/i18n/i18n"),cmi.getRoot().getLocalizerAssociated());
			Assert.assertEquals(0,item.getChildrenCount());
			Assert.assertNotNull(item.getFormatAssociated());
			Assert.assertEquals(float.class,item.getType());
			Assert.assertEquals(cmi,item.getOwner());
			Assert.assertNotNull(item.getParent());
			Assert.assertEquals("fieldLabel",item.getLabelId());
			Assert.assertEquals("fieldTooltip",item.getTooltipId());
			Assert.assertEquals("fieldHelp",item.getHelpId());
			Assert.assertNotNull(item.getIcon());
		
			item= cmi.byUIPath(URI.create("ui:/"+AnnotatedForTest.class.getCanonicalName()+"/call/methodAction"));

			Assert.assertNotNull(item);
			Assert.assertEquals("methodAction",item.getName());
			Assert.assertEquals(URI.create("./call/methodAction"),item.getRelativeUIPath());
			Assert.assertEquals(URI.create("ui:/"+AnnotatedForTest.class.getCanonicalName()+"/call/methodAction"),item.getUIPath());
			Assert.assertEquals(URI.create("app:action:/"+AnnotatedForTest.class.getSimpleName()+"/call().methodAction"),item.getApplicationPath());
			Assert.assertEquals(URI.create("i18n:prop:chav1961/purelib/i18n/i18n"),cmi.getRoot().getLocalizerAssociated());
			Assert.assertEquals(0,item.getChildrenCount());
			Assert.assertNull(item.getFormatAssociated());
			Assert.assertEquals(ActionEvent.class,item.getType());
			Assert.assertEquals(cmi,item.getOwner());
			Assert.assertNotNull(item.getParent());
			Assert.assertEquals("methodLabel",item.getLabelId());
			Assert.assertEquals("methodTooltip",item.getTooltipId());
			Assert.assertEquals("methodHelp",item.getHelpId());
			Assert.assertNotNull(item.getIcon());
		}		
		
		try{ContentModelFactory.forXmlDescription(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {				
		}
	}

	@Test
	public void annotatedClassDescriptionTest() throws IOException,PreparationException, SyntaxException, LocalizationException, ContentException {
		final ContentMetadataInterface 	cmi = ContentModelFactory.forAnnotatedClass(AnnotatedForTest.class);

		Assert.assertEquals("class",cmi.getRoot().getName());
		Assert.assertEquals(URI.create("./"+AnnotatedForTest.class.getCanonicalName()),cmi.getRoot().getRelativeUIPath());
		Assert.assertEquals(URI.create("ui:/"+AnnotatedForTest.class.getCanonicalName()),cmi.getRoot().getUIPath());
		Assert.assertEquals(URI.create("app:class:/"+AnnotatedForTest.class.getCanonicalName()),cmi.getRoot().getApplicationPath());
		Assert.assertEquals(URI.create("i18n:prop:chav1961/purelib/i18n/localization"),cmi.getRoot().getLocalizerAssociated());
		Assert.assertEquals(2,cmi.getRoot().getChildrenCount());
		Assert.assertNull(cmi.getRoot().getFormatAssociated());
		Assert.assertEquals(AnnotatedForTest.class,cmi.getRoot().getType());
		Assert.assertEquals(cmi,cmi.getRoot().getOwner());
		Assert.assertNull(cmi.getRoot().getParent());
		Assert.assertEquals("testSet1",cmi.getRoot().getLabelId());
		Assert.assertEquals("testSet2",cmi.getRoot().getTooltipId());
		Assert.assertEquals("testSet1",cmi.getRoot().getHelpId());

		final ContentNodeMetadata	item = cmi.byUIPath(URI.create("ui:/"+AnnotatedForTest.class.getCanonicalName()+"/testSet1/float"));
		
		Assert.assertNotNull(item);
		Assert.assertEquals("testSet1",item.getName());
		Assert.assertEquals(URI.create("./testSet1/float"),item.getRelativeUIPath());
		Assert.assertEquals(URI.create("ui:/"+AnnotatedForTest.class.getCanonicalName()+"/testSet1/float"),item.getUIPath());
		Assert.assertEquals(URI.create("app:field:/"+AnnotatedForTest.class.getCanonicalName()+"/testSet1"),item.getApplicationPath());
		Assert.assertEquals(URI.create("i18n:prop:chav1961/purelib/i18n/localization"),cmi.getRoot().getLocalizerAssociated());
		Assert.assertEquals(0,item.getChildrenCount());
		Assert.assertNotNull(item.getFormatAssociated());
		Assert.assertEquals(float.class,item.getType());
		Assert.assertEquals(cmi,item.getOwner());
		Assert.assertNotNull(item.getParent());
		Assert.assertEquals("testSet1",item.getLabelId());
		Assert.assertEquals("testSet2",item.getTooltipId());
		Assert.assertEquals("",item.getHelpId());

		final int 	countArray[] = new int[]{0};
		cmi.walkDown((mode,applicationPath,uiPath,node)->{countArray[0]++; return ContinueMode.CONTINUE;},URI.create("ui:/"+AnnotatedForTest.class.getCanonicalName()));
		Assert.assertEquals(6,countArray[0]);
	}
}


@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/localization")
@LocaleResource(value="testSet1",tooltip="testSet2",help="testSet1")
@Action(resource=@LocaleResource(value="testSet1",tooltip="testSet2"),actionString="press",simulateCheck=true) 
class AnnotatedForTest {
	@LocaleResource(value="testSet1",tooltip="testSet2")
	@Format("10.3mpzn")
		private float 	testSet1 = 0.0f;
	
		private void call() {}
	
}