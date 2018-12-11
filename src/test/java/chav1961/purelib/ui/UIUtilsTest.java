package chav1961.purelib.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.DebuggingLocalizer;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfacers.Format;
import chav1961.purelib.ui.swing.SimpleHelpComponentTest;

public class UIUtilsTest {
	private static final Map<String,SubstitutableProperties>	map = new HashMap<>();
	private static final Map<String,String>						help = new HashMap<>();
	
	static {
		final SubstitutableProperties	props = new SubstitutableProperties();
		
		props.putAll(Utils.mkProps("help","uri(help)","help2","uri(help2)"
						,"name1","Localized NAME1","tooltip1","Localized TOOLTIP1"
						,"name2","Localized --- NAME2","tooltip2","Localized --- TOOLTIP2"
						));
		map.put("ru",props);
		map.put("en",props);
		
		try{help.put("help",Utils.fromResource(SimpleHelpComponentTest.class.getResource("helpcontent.cre")));
			help.put("help2",Utils.fromResource(SimpleHelpComponentTest.class.getResource("referencedhelpcontent.cre")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void creLoadingTest() throws IOException {
		Assert.assertEquals(UIUtils.cre2Html("test"),"<html><head></head><body><div class=\"cwr\"><p>test\n</p></div></body></html>");
		Assert.assertEquals(UIUtils.cre2Html("test".toCharArray()),UIUtils.cre2Html("test"));
		
		try{UIUtils.cre2Html((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{UIUtils.cre2Html("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{UIUtils.cre2Html((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{UIUtils.cre2Html(new char[0]);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void fieldExtractionTest() throws IOException, IllegalArgumentException, NullPointerException, LocalizationException, SyntaxException, ContentException {
		final List<LabelAndField<Object,Object>>	result = new ArrayList<>();
		final Object								forLabel = new Object(), forField = new Object(); 

		try(final Localizer	localizer = new DebuggingLocalizer(map,help)) {
			
			UIUtils.collectFields(localizer,Object.class,null,result,
					(loc,id)->{return new Object();},
					(loc,desc,tooltip,initialValue)->{return new Object();});
			Assert.assertEquals(result.size(),0);
	
			final TestForFieldExtraction	instance = new TestForFieldExtraction();
			
			UIUtils.collectFields(localizer,TestForFieldExtraction.class,instance,result,
					(loc,id)->{return forLabel;},
					(loc,desc,tooltip,initialValue)->{return forField;});
			Assert.assertEquals(result.size(),2);
			
			final LabelAndField<Object,Object>			fieldDesc = result.get(0);
			
			Assert.assertEquals(fieldDesc.labelId,"name1");
			Assert.assertEquals(fieldDesc.labelToolTipId,"tooltip1");
			Assert.assertEquals(fieldDesc.label,forLabel);
			Assert.assertEquals(fieldDesc.field,forField);
			Assert.assertEquals(fieldDesc.fieldDesc.field.getName(),"value1");
			Assert.assertEquals(fieldDesc.fieldDesc.getFieldValue(instance),Float.valueOf(1.0f));
			
			try{UIUtils.collectFields(null,TestForFieldExtraction.class,instance,result,
						(loc,id)->{return forLabel;},
						(loc,desc,tooltip,initialValue)->{return forField;});
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{UIUtils.collectFields(localizer,null,instance,result,
						(loc,id)->{return forLabel;},
						(loc,desc,tooltip,initialValue)->{return forField;});
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{UIUtils.collectFields(localizer,TestForFieldExtraction.class,instance,null,
						(loc,id)->{return forLabel;},
						(loc,desc,tooltip,initialValue)->{return forField;});
				Assert.fail("Mandatory exception was not detected (null 4-th argument)");
			} catch (NullPointerException exc) {
			}
			try{UIUtils.collectFields(localizer,TestForFieldExtraction.class,instance,result,
						null,
						(loc,desc,tooltip,initialValue)->{return forField;});
				Assert.fail("Mandatory exception was not detected (null 5-th argument)");
			} catch (NullPointerException exc) {
			}
			try{UIUtils.collectFields(localizer,TestForFieldExtraction.class,instance,result,
						(loc,id)->{return forLabel;},
						null);
				Assert.fail("Mandatory exception was not detected (null 6-st argument)");
			} catch (NullPointerException exc) {
			}
		}
	}
}

class TestForFieldExtraction {
@LocaleResource(value="name1",tooltip="tooltip1")
@Format("10m")
	private final float	value1 = 1.0f;
@LocaleResource(value="name2",tooltip="tooltip2")
	private final float	value2 = 2.0f;

	private final float	value3 = 0.0f;
}
