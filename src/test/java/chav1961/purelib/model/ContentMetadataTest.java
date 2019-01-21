package chav1961.purelib.model;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class ContentMetadataTest {
	@Test
	public void basicTest() {
		final MutableContentNodeMetadata	root = new MutableContentNodeMetadata(
												"rootName", 
												String.class, 
												"root", 
												URI.create(Localizer.LOCALIZER_SCHEME+":memory"), 
												"label", 
												"tooltip", 
												"help", 
												"format", 
												URI.create("app:test"));
		final MutableContentNodeMetadata	child1 = new MutableContentNodeMetadata(
												"child1Name", 
												String.class, 
												"child1", 
												null, 
												"childLabel1", 
												"childTooltip1", 
												"childHelp1", 
												"childFormat1", 
												URI.create("app:child1Test"));
		final MutableContentNodeMetadata	child2 = new MutableContentNodeMetadata(
												"child2Name", 
												String.class, 
												"child2", 
												null, 
												"childLabel2", 
												"childTooltip2", 
												"childHelp2", 
												"childFormat2", 
												URI.create("app:child2Test"));
		final MutableContentNodeMetadata	child11 = new MutableContentNodeMetadata(
												"child11Name", 
												String.class, 
												"child11", 
												null, 
												"childLabel11", 
												"childTooltip11", 
												"childHelp11", 
												"childFormat11", 
												URI.create("app:child11Test"));
		final MutableContentNodeMetadata	child12 = new MutableContentNodeMetadata(
												"child12Name", 
												String.class, 
												"child12", 
												null, 
												"childLabel12", 
												"childTooltip12", 
												"childHelp12", 
												"childFormat12", 
												URI.create("app:child12Test"));
		final MutableContentNodeMetadata	child21 = new MutableContentNodeMetadata(
												"child21Name", 
												String.class, 
												"child21", 
												null, 
												"childLabel21", 
												"childTooltip21", 
												"childHelp21", 
												"childFormat21", 
												URI.create("app:child21Test"));
		final MutableContentNodeMetadata	child22 = new MutableContentNodeMetadata(
												"child22Name", 
												String.class, 
												"child22", 
												null, 
												"childLabel22", 
												"childTooltip22", 
												"childHelp22", 
												"childFormat22", 
												URI.create("app:child22Test"));
		
		root.addChild(child1);		child1.setParent(root);
		child1.addChild(child11);	child11.setParent(child1);
		child1.addChild(child12);	child12.setParent(child1);
		root.addChild(child2);		child2.setParent(root);
		child2.addChild(child21);	child21.setParent(child2);
		child2.addChild(child22);	child22.setParent(child2);
		
		final ContentMetadataInterface		metadata = new SimpleContentMetadata(root);
		
		Assert.assertEquals(root,metadata.getRoot());
		Assert.assertEquals(child1,metadata.byUIPath(URI.create("ui:/root/child1")));
		Assert.assertEquals(child22,metadata.byUIPath(URI.create("ui:/root/child2/child22")));
		
		final int[]		counter = new int[]{0};
		
		metadata.walkDown((mode,appPath,uiPath,node)->{counter[0]++; return ContinueMode.CONTINUE;}, URI.create("ui:/root"));
		Assert.assertEquals(14,counter[0]);

		counter[0] = 0;
		metadata.walkDown((mode,appPath,uiPath,node)->{counter[0]++; return ContinueMode.CONTINUE;}, URI.create("ui:/unknown"));
		Assert.assertEquals(0,counter[0]);

		counter[0] = 0;
		metadata.walkDown((mode,appPath,uiPath,node)->{counter[0]++; return ContinueMode.CONTINUE;}, URI.create("ui:/root/child1"));
		Assert.assertEquals(6,counter[0]);
		
		Assert.assertEquals(child11,metadata.byApplicationPath(URI.create("app:child11Test")));
		
		try{new SimpleContentMetadata(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try{metadata.byApplicationPath(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{metadata.byUIPath(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{metadata.walkDown(null, URI.create("ui:/root/child1"));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{metadata.walkDown((f1,f2,f3,f4)->{return ContinueMode.CONTINUE;}, null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
