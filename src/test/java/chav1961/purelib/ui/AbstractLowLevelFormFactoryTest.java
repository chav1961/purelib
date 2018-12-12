package chav1961.purelib.ui;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FormPage;
import chav1961.purelib.ui.interfacers.ControllerAction;
import chav1961.purelib.ui.interfacers.FieldRepresentation;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.FormModel;
import chav1961.purelib.ui.interfacers.FormRepresentation;
import chav1961.purelib.ui.interfacers.Format;
import chav1961.purelib.ui.interfacers.RefreshMode;
import chav1961.purelib.ui.interfacers.Wizard;

public class AbstractLowLevelFormFactoryTest {
	private static final FormManager<String,SingleClass>		FORM_MANAGER = new FormManager<String,SingleClass>(){
																	final LoggerFacade logger = new SystemErrLoggerFacade();
		
																	@Override
																	public RefreshMode onRecord(Action action, SingleClass oldRecord, String oldId, SingleClass newRecord, String newId) throws FlowException {
																		return null;
																	}
															
																	@Override
																	public RefreshMode onField(SingleClass inst, String id, String fieldName, Object oldValue) throws FlowException {
																		return null;
																	}
															
																	@Override
																	public RefreshMode onAction(SingleClass inst, String id, String actionName, Object parameter) throws FlowException {
																		return null;
																	}

																	@Override
																	public LoggerFacade getLogger() {
																		return logger;
																	}
																};
	
	private static final FormManager<String,TotalClass>		TOTAL_FORM_MANAGER = new FormManager<String,TotalClass>(){
																	final LoggerFacade logger = new SystemErrLoggerFacade();
		
																	@Override
																	public RefreshMode onRecord(Action action, TotalClass oldRecord, String oldId, TotalClass newRecord, String newId) throws FlowException {
																		return null;
																	}
															
																	@Override
																	public RefreshMode onField(TotalClass inst, String id, String fieldName, Object oldValue) throws FlowException {
																		return null;
																	}
															
																	@Override
																	public RefreshMode onAction(TotalClass inst, String id, String actionName, Object parameter) throws FlowException {
																		return null;
																	}

																	@Override
																	public LoggerFacade getLogger() {
																		return logger;
																	}
																};
	
//	@Test
	public void basicTest() throws IOException, SyntaxException, URISyntaxException {
		final PseudoLowLevelFormFactory<SingleClass>	f = new PseudoLowLevelFormFactory<SingleClass>(this.getClass().getResource("singleform.txt").toURI(),FormRepresentation.SINGLE_RECORD,SingleClass.class,FORM_MANAGER);

		Assert.assertEquals(f.getPages().length,3);
		Assert.assertEquals(f.getPages()[0].formName,"part1");
		Assert.assertEquals(f.getPages()[0].captionId,"caption1");
		Assert.assertEquals(f.getPages()[0].iconId,URI.create("iconURI1"));
		Assert.assertEquals(f.getPages()[0].tooltipId,"tooltip1");
		Assert.assertEquals(f.getPages()[0].helpId,"help1");
		Assert.assertEquals(f.getPages()[0].fieldNames.size(),1);
		
		Assert.assertEquals(f.getFieldDescriptors().length,3);
		Assert.assertEquals(f.getFieldDescriptors()[0].field.getName(),"field1");
		Assert.assertEquals(f.getFieldDescriptors()[0].fieldFormat,new FormFieldFormat("m".toCharArray()));
		Assert.assertEquals(f.getFieldDescriptors()[0].fieldLen,20);
		Assert.assertEquals(f.getFieldDescriptors()[0].fieldTooltip,"field1");
		Assert.assertEquals(f.getFieldDescriptors()[0].fieldRepresentation,FieldRepresentation.INTVALUE);
		Assert.assertEquals(f.getFieldDescriptors()[0].fieldType,int.class);
		
		try{new PseudoLowLevelFormFactory<SingleClass>(null,FormRepresentation.SINGLE_RECORD,SingleClass.class,FORM_MANAGER);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new PseudoLowLevelFormFactory<SingleClass>(this.getClass().getResource("singleform.txt").toURI(),null,SingleClass.class,FORM_MANAGER);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new PseudoLowLevelFormFactory<SingleClass>(this.getClass().getResource("singleform.txt").toURI(),FormRepresentation.SINGLE_RECORD,null,FORM_MANAGER);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new PseudoLowLevelFormFactory<SingleClass>(this.getClass().getResource("singleform.txt").toURI(),FormRepresentation.SINGLE_RECORD,SingleClass.class,null);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void inheritanceTest() throws IOException, SyntaxException, URISyntaxException {
		final PseudoLowLevelFormFactory<SingleClass>	f = new PseudoLowLevelFormFactory<SingleClass>(this.getClass().getResource("childform.txt").toURI(), new URI[]{this.getClass().getResource("singleform.txt").toURI()},FormRepresentation.SINGLE_RECORD,SingleClass.class,FORM_MANAGER);
		
		Assert.assertEquals(f.getPages().length,3);
		Assert.assertEquals(f.getPages()[0].formName,"part4");
		Assert.assertEquals(f.getPages()[1].formName,"part1");
		Assert.assertEquals(f.getPages()[2].formName,"part3");

		try{new PseudoLowLevelFormFactory<SingleClass>(this.getClass().getResource("childform.txt").toURI(),null,FormRepresentation.SINGLE_RECORD,SingleClass.class,FORM_MANAGER);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new PseudoLowLevelFormFactory<SingleClass>(this.getClass().getResource("childform.txt").toURI(),new URI[]{null},FormRepresentation.SINGLE_RECORD,SingleClass.class,FORM_MANAGER);
			Assert.fail("Mandatory exception was not detected (null item in the 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

//	@Test
	public void fieldDescriptionTest() throws IOException, SyntaxException, URISyntaxException, ContentException {
		final PseudoLowLevelFormFactory<TotalClass>		f = new PseudoLowLevelFormFactory<TotalClass>(this.getClass().getResource("list.txt").toURI(),FormRepresentation.LIST,TotalClass.class,TOTAL_FORM_MANAGER);
		final TotalClass								inst = new TotalClass();
		final SingleClass								assign = new SingleClass();
		
		for (FieldDescriptor item : f.getFieldDescriptors()) {
			switch (item.field.getName()) {
				case "id"					:
					Assert.assertEquals(item.fieldType,int.class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.INTVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat("L".toCharArray()));
					Assert.assertEquals(item.fieldTemplate,"#0");
					Assert.assertEquals(item.fieldTooltip,"id");
					item.setFieldValue(inst,100);
					Assert.assertEquals(item.getFieldValue(inst),new Integer(100));
					break;
				case "checkbox"				:
					Assert.assertEquals(item.fieldType,boolean.class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.BOOLVALUE);
					Assert.assertEquals(item.fieldLen,1);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertNull(item.fieldTemplate);
					Assert.assertEquals(item.fieldTooltip,"myCheckBoxTooltip");
					item.setFieldValue(inst,true);
					Assert.assertEquals(item.getFieldValue(inst),new Boolean(true));
					break;
				case "intvalue"				:
					Assert.assertEquals(item.fieldType,long.class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.INTVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertEquals(item.fieldTemplate,"#0");
					Assert.assertEquals(item.fieldTooltip,"myIntValueTooltip");
					item.setFieldValue(inst,100);
					Assert.assertEquals(item.getFieldValue(inst),new Long(100));
					break;
				case "realvalue"			:
					Assert.assertEquals(item.fieldType,double.class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.REALVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertEquals(item.fieldTemplate,"#0.0#");
					Assert.assertEquals(item.fieldTooltip,"realvalue");
					item.setFieldValue(inst,100.0);
					Assert.assertEquals(item.getFieldValue(inst),new Double(100.0));
					break;
				case "bigDecimalValue"		:
					Assert.assertEquals(item.fieldType,BigDecimal.class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.CURRENCYVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertEquals(item.fieldTemplate,"#0.0#");
					Assert.assertEquals(item.fieldTooltip,"bigDecimalValue");
					item.setFieldValue(inst,new BigDecimal(100.0));
					Assert.assertEquals(item.getFieldValue(inst),new BigDecimal(100.0));
					break;
				case "dateValue"			:
					Assert.assertEquals(item.fieldType,Date.class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.DATEVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertNull(item.fieldTemplate);
					Assert.assertEquals(item.fieldTooltip,"dateValue");
					item.setFieldValue(inst,new Date(0));
					Assert.assertEquals(item.getFieldValue(inst),new Date(0));
					break;
				case "timeValue"			:
					Assert.assertEquals(item.fieldType,Time.class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.DATEVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertNull(item.fieldTemplate);
					Assert.assertEquals(item.fieldTooltip,"timeValue");
					item.setFieldValue(inst,new Time(0));
					Assert.assertEquals(item.getFieldValue(inst),new Time(0));
					break;
				case "timestampValue"		:
					Assert.assertEquals(item.fieldType,Timestamp.class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.DATEVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertNull(item.fieldTemplate);
					Assert.assertEquals(item.fieldTooltip,"timestampValue");
					item.setFieldValue(inst,new Timestamp(0));
					Assert.assertEquals(item.getFieldValue(inst),new Timestamp(0));
					break;
				case "textValue"			:
					Assert.assertEquals(item.fieldType,String.class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.TEXTVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertNull(item.fieldTemplate);
					Assert.assertEquals(item.fieldTooltip,"textValue");
					item.setFieldValue(inst,"test string");
					Assert.assertEquals(item.getFieldValue(inst),"test string");
					break;
				case "formattedTextValue"	:
					Assert.assertEquals(item.fieldType,String.class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.FORMATTEDTEXTVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertEquals(item.fieldTemplate,"ANNNNNNN");
					Assert.assertEquals(item.fieldTooltip,"formattedTextValue");
					item.setFieldValue(inst,"test string");
					Assert.assertEquals(item.getFieldValue(inst),"test string");
					break;
				case "formattedAreaValue"	:
					Assert.assertEquals(item.fieldType,String.class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.TEXTVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertNull(item.fieldTemplate);
					Assert.assertEquals(item.fieldTooltip,"formattedAreaValue");
					item.setFieldValue(inst,"test string");
					Assert.assertEquals(item.getFieldValue(inst),"test string");
					break;
				case "passwordValue"		:
					Assert.assertEquals(item.fieldType,char[].class);
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.PASSWDVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertNull(item.fieldTemplate);
					Assert.assertEquals(item.fieldTooltip,"passwordValue");
					item.setFieldValue(inst,"test string".toCharArray());
					Assert.assertArrayEquals((char[])item.getFieldValue(inst),"test string".toCharArray());
					break;
				case "enumValue"			:
					Assert.assertTrue(Enum.class.isAssignableFrom(item.fieldType));
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.DDLISTVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertNull(item.fieldTemplate);
					Assert.assertEquals(item.fieldTooltip,"enumValue");
					item.setFieldValue(inst,FormRepresentation.LIST);
					Assert.assertEquals(item.getFieldValue(inst),FormRepresentation.LIST);
					break;
				case "stringArrayValue"		:
					Assert.assertTrue(String[].class.isAssignableFrom(item.fieldType));
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.LISTVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertNull(item.fieldTemplate);
					Assert.assertEquals(item.fieldTooltip,"stringArrayValue");
					item.setFieldValue(inst,new String[]{"test string"});
					Assert.assertArrayEquals((String[])item.getFieldValue(inst),new String[]{"test string"});
					break;
				case "listArrayValue"		:
					Assert.assertTrue(Collection.class.isAssignableFrom(item.fieldType));
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.LISTVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertEquals(item.fieldTemplate,"ANNNNNNN");
					Assert.assertEquals(item.fieldTooltip,"listArrayValue");
					item.setFieldValue(inst,Arrays.asList("test string"));
					Assert.assertEquals((Collection)item.getFieldValue(inst),Arrays.asList("test string"));
					break;
				case "mapValue"				:
					Assert.assertTrue(Map.class.isAssignableFrom(item.fieldType));
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.MAPVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertNull(item.fieldTemplate);
					Assert.assertEquals(item.fieldTooltip,"mapValue");
					item.setFieldValue(inst,new HashMap());
					Assert.assertEquals((Map)item.getFieldValue(inst),new HashMap());
					break;
				case "wizardValue"			:
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.WIZARDVALUE);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertEquals(item.fieldTemplate,"MyWizard");
					Assert.assertEquals(item.fieldTooltip,"wizardValue");
					item.setFieldValue(inst,assign);
					Assert.assertEquals(item.getFieldValue(inst),assign);
					break;
				case "keyValuePairValue"	:
					Assert.assertEquals(item.fieldRepresentation,FieldRepresentation.KEYVALUEPAIR);
					Assert.assertEquals(item.fieldLen,10);
					Assert.assertEquals(item.fieldFormat,new FormFieldFormat());
					Assert.assertNull(item.fieldTemplate);
					Assert.assertEquals(item.fieldTooltip,"keyValuePairValue");
					item.setFieldValue(inst,assign);
					Assert.assertEquals(item.getFieldValue(inst),assign);
					break;
				default : Assert.fail("Field "+item.field+" has not code testing in the test");
			}
		}
	}

	@Test
	public void pageDescriptionTest() throws IOException, SyntaxException, URISyntaxException {
		final PseudoLowLevelFormFactory<TotalClass>		f = new PseudoLowLevelFormFactory<TotalClass>(this.getClass().getResource("pages.txt").toURI(),FormRepresentation.SINGLE_RECORD,TotalClass.class,TOTAL_FORM_MANAGER);

		for (FormPage item : f.getPages()) {
			switch (item.formName) {
				case "part1"	:
					Assert.assertEquals(item.captionId,"caption1");
					Assert.assertNull(item.iconId);
					Assert.assertEquals(item.tooltipId,"tooltip1");
					Assert.assertEquals(item.helpId,"help1");
					break;
				case "part2"	:
					Assert.assertEquals(item.captionId,"caption2");
					Assert.assertEquals(item.iconId,URI.create("icon2"));
					Assert.assertEquals(item.tooltipId,"tooltip2");
					Assert.assertEquals(item.helpId,"help2");
					break;
				case "part3"	:
					Assert.assertEquals(item.captionId,"caption3");
					Assert.assertNull(item.iconId);
					Assert.assertEquals(item.tooltipId,"tooltip3");
					Assert.assertNull(item.helpId);
					break;
				case "part4"	:
					Assert.assertEquals(item.captionId,"part4");
					Assert.assertNull(item.iconId);
					Assert.assertEquals(item.tooltipId,"part4");
					Assert.assertEquals(item.helpId,"help4");
					break;
				default : Assert.fail("Page "+item.formName+" has not code testing in the test");
			}
		}
	}
}

class PseudoLowLevelFormFactory<T> extends AbstractLowLevelFormFactory {
	public PseudoLowLevelFormFactory(URI formDescription, FormRepresentation representation, Class<T> rootClass, FormManager<?, T> manager) throws IOException, SyntaxException {
		super(formDescription, representation, rootClass, manager);
	}

	public PseudoLowLevelFormFactory(URI formDescription, URI[] inherited, FormRepresentation representation, Class<T> rootClass, FormManager<?, T> manager) throws IOException, SyntaxException {
		super(formDescription, inherited, representation, rootClass, manager);
	}

	FormPage[] getPages() {
		return pages;
	}
	
	FieldDescriptor[] getFieldDescriptors() {
		return fieldNames;
	}
}