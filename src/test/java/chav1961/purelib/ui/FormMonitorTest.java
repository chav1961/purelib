package chav1961.purelib.ui;


import java.net.URI;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.TestException;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.Action;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@Tag("OrdinalTestCategory")
public class FormMonitorTest {
	@Test
	public void seekAndCallTest() throws Exception {
		final SeekAndCallTest	sact = new SeekAndCallTest();
		
		try{FormMonitor.seekAndCall(sact,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/callMethod1().test"));
			Assert.fail("Mandatory exception was not detected (Test exception awaited)");
		} catch (TestException  exc) {
		}
		
		Assert.assertEquals(RefreshMode.DEFAULT,FormMonitor.seekAndCall(sact,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/callMethod2().test")));
		Assert.assertEquals(RefreshMode.EXIT,FormMonitor.seekAndCall(sact,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/callMethod3().test")));
		Assert.assertEquals(RefreshMode.DEFAULT,FormMonitor.seekAndCall(sact,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/unknown().test")));

		Assert.assertEquals(RefreshMode.DEFAULT,FormMonitor.seekAndCall(new String(),URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/unknown().test")));
		try{FormMonitor.seekAndCall(sact,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/callMethod4().test"));
			Assert.fail("Mandatory exception was not detected (Returned type of the methos is neither void nor RefreshMode)");
		} catch (IllegalArgumentException exc) {
		}
	}

}

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":xml:file:./src/test/resources/chav1961/purelib/i18n/test.xml")
@LocaleResource(value="key1",tooltip="key2",help="key3")
@Action(resource=@LocaleResource(value="key2",tooltip="key2"),actionString="press",simulateCheck=true) 
class SeekAndCallTest {
	@LocaleResource(value="key1",tooltip="key2")
	@Format("10.3mpzn")
		private float 	testSet1 = 0.0f;
	

	void callMethod1() throws TestException {
		throw new TestException(); 
	}

	void callMethod2() {}
	
	RefreshMode callMethod3() {
		return RefreshMode.EXIT;
	}

	int callMethod4() {
		return 0;
	}
}
