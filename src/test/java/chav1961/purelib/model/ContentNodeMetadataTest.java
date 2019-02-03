package chav1961.purelib.model;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class ContentNodeMetadataTest {
	@Test
	public void basicTest() {
		final ContentNodeMetadata	metadata = new MutableContentNodeMetadata(
											"nodeName", 
											String.class, 
											"test", 
											URI.create(Localizer.LOCALIZER_SCHEME+":memory"), 
											"label", 
											"tooltip", 
											"help", 
											null, 
											URI.create("app:test"));
		Assert.assertEquals("nodeName",metadata.getName());
		Assert.assertEquals(String.class,metadata.getType());
		Assert.assertEquals(URI.create("ui:/test"),metadata.getUIPath());
		Assert.assertEquals(URI.create("./test"),metadata.getRelativeUIPath());
		Assert.assertEquals(URI.create(Localizer.LOCALIZER_SCHEME+":memory"),metadata.getLocalizerAssociated());
		Assert.assertEquals("label",metadata.getLabelId());
		Assert.assertEquals("tooltip",metadata.getTooltipId());
		Assert.assertEquals("help",metadata.getHelpId());
		Assert.assertNull(metadata.getFormatAssociated());
		Assert.assertEquals(URI.create("app:test"),metadata.getApplicationPath());
		Assert.assertEquals(0,metadata.getChildrenCount());
		Assert.assertNull(metadata.getParent());
		
		try{new MutableContentNodeMetadata(null,String.class,"test",URI.create(Localizer.LOCALIZER_SCHEME+":memory"),"label"
						,"tooltip","help",null,URI.create("test"));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new MutableContentNodeMetadata("",String.class,"test",URI.create(Localizer.LOCALIZER_SCHEME+":memory"),"label"
						,"tooltip","help",null,URI.create("test"));
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new MutableContentNodeMetadata("nodeName",null,"test",URI.create(Localizer.LOCALIZER_SCHEME+":memory"),"label"
						,"tooltip","help",null,URI.create("test"));
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{new MutableContentNodeMetadata("nodeName",String.class,null,URI.create(Localizer.LOCALIZER_SCHEME+":memory"),"label"
						,"tooltip","help",null,URI.create("test"));
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new MutableContentNodeMetadata("nodeName",String.class,"",URI.create(Localizer.LOCALIZER_SCHEME+":memory"),"label"
						,"tooltip","help",null,URI.create("test"));
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new MutableContentNodeMetadata("nodeName",String.class,"test",URI.create("memory"),"label"
						,"tooltip","help",null,URI.create("test"));
			Assert.fail("Mandatory exception was not detected (illegal scheme in 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new MutableContentNodeMetadata("nodeName",String.class,"test",URI.create(Localizer.LOCALIZER_SCHEME+":memory"),null
						,"tooltip","help",null,URI.create("test"));
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new MutableContentNodeMetadata("nodeName",String.class,"test",URI.create(Localizer.LOCALIZER_SCHEME+":memory"),""
						,"tooltip","help",null,URI.create("test"));
			Assert.fail("Mandatory exception was not detected (empty 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new MutableContentNodeMetadata("nodeName",String.class,"test",URI.create(Localizer.LOCALIZER_SCHEME+":memory"),"label"
						,"tooltip","help",null,URI.create("test"));
		Assert.fail("Mandatory exception was not detected (null 9-th argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void extendedTest() {
		final MutableContentNodeMetadata	root = new MutableContentNodeMetadata(
												"nodeName", 
												String.class, 
												"test", 
												URI.create(Localizer.LOCALIZER_SCHEME+":memory"), 
												"label", 
												"tooltip", 
												"help", 
												null, 
												URI.create("app:test"));
		final MutableContentNodeMetadata	child = new MutableContentNodeMetadata(
												"childNodeName", 
												String.class, 
												"child", 
												null, 
												"childLabel", 
												"childTooltip", 
												"childHelp", 
												null, 
												URI.create("app:childTest"));
		
		Assert.assertEquals(0,root.getChildrenCount());

		child.setParent(root);
		
		Assert.assertEquals(root,child.getParent());
		
		root.addChild(child);
		
		Assert.assertEquals(1,root.getChildrenCount());
		for (ContentNodeMetadata item : root) {
			Assert.assertEquals(child,item);
		}
		
		Assert.assertEquals(URI.create(Localizer.LOCALIZER_SCHEME+":memory"),child.getLocalizerAssociated());
		Assert.assertEquals(URI.create("ui:/test/child"),child.getUIPath());
		Assert.assertEquals(URI.create("./child"),child.getRelativeUIPath());
		
		root.removeChild(child);
		Assert.assertEquals(0,root.getChildrenCount());
	}
}
