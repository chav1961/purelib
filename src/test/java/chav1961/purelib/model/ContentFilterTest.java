package chav1961.purelib.model;

import java.io.IOException;

import java.io.InputStream;
import java.net.URI;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

@Tag("OrdinalTestCategory")
public class ContentFilterTest {
	@Test
	public void basicTest() throws IOException, EnvironmentException {
//		try(final InputStream				is = ContentMetadataFilterTest.class.getResourceAsStream("modelTest1.xml")) {
//			final ContentMetadataInterface	nested = ContentModelFactory.forXmlDescription(is);
//			final ContentMetadataFilter		filter1 = new ContentMetadataFilter(nested, ".*", "item.*");
//			final ContentMetadataFilter		filter2 = new ContentMetadataFilter(nested, ".*", "item.*");
//			final int[] 					count = new int[1];
//
//			count[0] = 0;
//			nested.walkDown((a,b,c,d)->{count[0]++; return ContinueMode.CONTINUE;},nested.getRoot().getUIPath());
//			Assert.assertEquals(16,count[0]);
//			
//			Assert.assertTrue(filter1.isAllowed(URI.create("")));
//			Assert.assertFalse(filter1.isAllowed(URI.create("")));
//			Assert.assertFalse(filter1.isAllowed(URI.create("")));
//
//			count[0] = 0;
//			filter1.walkDown((a,b,c,d)->{count[0]++; return ContinueMode.CONTINUE;},nested.getRoot().getUIPath());
//			Assert.assertEquals(0,count[0]);
//
//			Assert.assertTrue(filter2.isAllowed(URI.create("")));
//			Assert.assertFalse(filter2.isAllowed(URI.create("")));
//			Assert.assertFalse(filter2.isAllowed(URI.create("")));
//			
//			count[0] = 0;
//			filter2.walkDown((a,b,c,d)->{count[0]++; return ContinueMode.CONTINUE;},nested.getRoot().getUIPath());
//			Assert.assertEquals(0,count[0]);
//		}
	}

//	@Test
//	public void exceptionsTest() throws IOException, EnvironmentException {
//		try(final InputStream				is = ContentFilterTest.class.getResourceAsStream("modelTest1.xml")) {
//			final ContentMetadataInterface	nested = ContentModelFactory.forXmlDescription(is);
//
//			try{new ContentMetadataFilter(null, Pattern.compile(".*"));
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (NullPointerException exc) {
//			}
//			try{new ContentMetadataFilter(null, Pattern.compile(".*"), Pattern.compile(".*"));
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (NullPointerException exc) {
//			}
//			try{new ContentMetadataFilter(null, new URI[0]);
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (NullPointerException exc) {
//			}
//			try{new ContentMetadataFilter(null, new URI[0], new URI[0]);
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (NullPointerException exc) {
//			}
//	
//			try{new ContentMetadataFilter(nested, (Pattern)null);
//				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try{new ContentMetadataFilter(nested, (URI[])null);
//				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try{new ContentMetadataFilter(nested, new URI[0]);
//				Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try{new ContentMetadataFilter(nested, new URI[]{null});
//				Assert.fail("Mandatory exception was not detected (null inside 2-nd argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//	
//			try{new ContentMetadataFilter(nested, Pattern.compile(".*"), (Pattern)null);
//				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try{new ContentMetadataFilter(nested, new URI[]{URI.create("unknown")}, (URI[])null);
//				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
//			} catch (NullPointerException exc) {
//			}
//			try{new ContentMetadataFilter(nested, new URI[]{URI.create("unknown")}, new URI[0]);
//				Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//			try{new ContentMetadataFilter(nested, new URI[]{URI.create("unknown")}, new URI[]{null});
//				Assert.fail("Mandatory exception was not detected (null inside 3-rd argument)");
//			} catch (IllegalArgumentException exc) {
//			}
//		}
//	}

	@Test
	public void basicNodeFilterTest() throws IOException, EnvironmentException, NullPointerException, IllegalArgumentException, ContentException {
		final ContentMetadataInterface	mdi = ContentModelFactory.forOrdinalClass(NodeClass.class);
		final ContentNodeMetadata		f = new ContentNodeFilter(mdi.getRoot(),(n)->n.getName().startsWith("f"),(n)->n.getName().endsWith("1"),false);
		
		Assert.assertEquals("class",f.getName());
		Assert.assertFalse(f.mounted());
		Assert.assertEquals(NodeClass.class,f.getType());
		Assert.assertEquals("NodeClass",f.getLabelId());
		Assert.assertNull(f.getTooltipId());
		Assert.assertNull(f.getHelpId());
		Assert.assertNull(f.getFormatAssociated());
		Assert.assertEquals(URI.create("app:class:/"+NodeClass.class.getName()),f.getApplicationPath());
		Assert.assertEquals(URI.create("ui:/"+NodeClass.class.getCanonicalName()),f.getUIPath());
		Assert.assertEquals(URI.create("./"+NodeClass.class.getCanonicalName()),f.getRelativeUIPath());
		Assert.assertEquals(PureLibSettings.PURELIB_LOCALIZER.getLocalizerId(),URIUtils.extractSubURI(f.getLocalizerAssociated(),Localizer.LOCALIZER_SCHEME,"xml"));
		Assert.assertNull(f.getIcon());
		Assert.assertNull(f.getParent());
		Assert.assertEquals(mdi,f.getOwner());

		Assert.assertEquals(1,f.getChildrenCount());
		Assert.assertEquals("f2",f.getChild(0).getName());

		final ContentNodeMetadata		f1 = new ContentNodeFilter(mdi.getRoot(),true,(n)->n.getName().startsWith("f"),(n)->n.getName().endsWith("1"),false);

		Assert.assertEquals(1,f1.getChildrenCount());
		Assert.assertEquals("f2",f1.getChild(0).getName());
		
		int		count = 0;
		for (ContentNodeMetadata item : new ContentNodeFilter(mdi.getRoot(),(n)->n.getName().startsWith("f"),(n)->n.getName().endsWith("1"),false)) {
			count++;
		}
		Assert.assertEquals(1,count);

		count = 0;
		for (ContentNodeMetadata item : new ContentNodeFilter(mdi.getRoot(),(n)->n.getName().startsWith("f"),false)) {
			count++;
		}
		Assert.assertEquals(2,count);

		count = 0;
		for (ContentNodeMetadata item : new ContentNodeFilter(mdi.getRoot(),true,(n)->n.getName().startsWith("f"),(n)->n.getName().endsWith("1"),false)) {
			Assert.assertTrue(item instanceof MutableContentNodeMetadata);
			count++;
		}
		Assert.assertEquals(1,count);

		count = 0;
		for (ContentNodeMetadata item : new ContentNodeFilter(mdi.getRoot(),true,(n)->n.getName().startsWith("f"),(n)->n.getName().endsWith("1"),true)) {
			Assert.assertTrue(item instanceof ContentNodeFilter);
			count++;
		}
		Assert.assertEquals(1,count);

		try{new ContentNodeFilter(null,true,(n)->n.getName().startsWith("f"),(n)->n.getName().endsWith("1"),true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new ContentNodeFilter(mdi.getRoot(),true,null,(n)->n.getName().endsWith("1"),true);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new ContentNodeFilter(mdi.getRoot(),true,(n)->n.getName().startsWith("f"),null,true);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
	}

	public class NodeClass {
		public int	f1;
		public int	f2;
		public int	t1;
		public int	t2;
	}
}
