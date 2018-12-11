package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.ui.TotalClass;
import chav1961.purelib.ui.UIUtils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.DebuggingLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;
import chav1961.purelib.ui.FormFieldFormat;
import chav1961.purelib.ui.SingleClass;
import chav1961.purelib.ui.interfacers.FormRepresentation;


public class SwingUtilsTest {
	
	@Test
	public void inputComponentsTest() {
		
	}

	@Test
	public void renderersTest() throws SyntaxException {
		final Locale	locale = Locale.forLanguageTag("en");
		FieldDescriptor	fd;
		JComponent		comp;
		
		fd = FieldDescriptor.newInstance("id",new FormFieldFormat("m"),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,null);
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.MANDATORY_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.RIGHT);
		Assert.assertEquals(((JLabel)comp).getText(),"0");

		fd = FieldDescriptor.newInstance("checkbox",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,true);
		
		Assert.assertTrue(comp instanceof JCheckBox);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertTrue(((JCheckBox)comp).isSelected());

		fd = FieldDescriptor.newInstance("intvalue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,100);
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.RIGHT);
		Assert.assertEquals(((JLabel)comp).getText(),"100");

		fd = FieldDescriptor.newInstance("realvalue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,123.456);
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.RIGHT);
		Assert.assertEquals(((JLabel)comp).getText(),"123.456");

		fd = FieldDescriptor.newInstance("bigDecimalValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,new BigDecimal(123.45));
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.RIGHT);
		Assert.assertEquals(((JLabel)comp).getText().substring(1),"123.45");
		
		fd = FieldDescriptor.newInstance("dateValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,new Date(0));
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),"1/1/70");

		fd = FieldDescriptor.newInstance("timeValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,new Time(0));
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),new Date(0).getHours()+":00 AM");

		fd = FieldDescriptor.newInstance("timestampValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,new Timestamp(0));
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),"1/1/70 "+new Date(0).getHours()+":00 AM");

		fd = FieldDescriptor.newInstance("textValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,"1234567");
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),"1234567");

		fd = FieldDescriptor.newInstance("passwordValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,new char[]{'1'});
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),"<hidden>");

		fd = FieldDescriptor.newInstance("enumValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,FormRepresentation.LIST);
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getHorizontalTextPosition(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),FormRepresentation.LIST.toString());

		fd = FieldDescriptor.newInstance("stringArrayValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,new String[]{"test"});
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getHorizontalTextPosition(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),"1 items");

		fd = FieldDescriptor.newInstance("listArrayValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,Arrays.asList("test"));
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getHorizontalTextPosition(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),"1 items");

		fd = FieldDescriptor.newInstance("listArrayValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,Arrays.asList("test"));
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getHorizontalTextPosition(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),"1 items");

		fd = FieldDescriptor.newInstance("mapValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,new HashMap<Object,Object>());
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getHorizontalTextPosition(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),"0 items");

		fd = FieldDescriptor.newInstance("wizardValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,new SingleClass());
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getHorizontalTextPosition(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),new SingleClass().toString());

		fd = FieldDescriptor.newInstance("keyValuePairValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellRendererComponent(locale,fd,new SingleClass());
		
		Assert.assertTrue(comp instanceof JLabel);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JLabel)comp).getHorizontalAlignment(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getHorizontalTextPosition(),JLabel.LEFT);
		Assert.assertEquals(((JLabel)comp).getText(),new SingleClass().toString());
	}

	@Test
	public void editorsTest() throws IllegalArgumentException, NullPointerException, SyntaxException, LocalizationException {
		final Localizer	localizer = new DebuggingLocalizer(new HashMap<>());
		
		localizer.setCurrentLocale(Locale.forLanguageTag("en"));
		FieldDescriptor	fd;
		JComponent		comp;
		
		fd = FieldDescriptor.newInstance("id",new FormFieldFormat("m"),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,123);
		
		Assert.assertTrue(comp instanceof JFormattedTextField);
		Assert.assertEquals(comp.getBackground(),SwingUtils.MANDATORY_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.MANDATORY_FOREGROUND);
		Assert.assertEquals(((JFormattedTextField)comp).getHorizontalAlignment(),JFormattedTextField.RIGHT);
		Assert.assertEquals(((JFormattedTextField)comp).getValue(),Integer.valueOf("123"));
		
		fd = FieldDescriptor.newInstance("checkbox",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,true);
		
		Assert.assertTrue(comp instanceof JCheckBox);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertTrue(((JCheckBox)comp).isSelected());

		fd = FieldDescriptor.newInstance("intvalue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,123L);
		
		Assert.assertTrue(comp instanceof JFormattedTextField);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JFormattedTextField)comp).getHorizontalAlignment(),JFormattedTextField.RIGHT);
		Assert.assertEquals(((JFormattedTextField)comp).getValue(),Long.valueOf("123"));

		fd = FieldDescriptor.newInstance("realvalue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,123.0);
		
		Assert.assertTrue(comp instanceof JFormattedTextField);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JFormattedTextField)comp).getHorizontalAlignment(),JFormattedTextField.RIGHT);
		Assert.assertEquals(((JFormattedTextField)comp).getValue(),Double.valueOf("123"));

		fd = FieldDescriptor.newInstance("bigDecimalValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,new BigDecimal(123));
		
		Assert.assertTrue(comp instanceof JFormattedTextField);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JFormattedTextField)comp).getHorizontalAlignment(),JFormattedTextField.RIGHT);
		Assert.assertEquals(((JFormattedTextField)comp).getValue(),new BigDecimal(123));

		fd = FieldDescriptor.newInstance("dateValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,new Date(0));
		
		Assert.assertTrue(comp instanceof JFormattedTextField);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JFormattedTextField)comp).getHorizontalAlignment(),JFormattedTextField.LEFT);
		Assert.assertEquals(((JFormattedTextField)comp).getValue(),new Date(0));

		fd = FieldDescriptor.newInstance("timeValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,new Time(0));
		
		Assert.assertTrue(comp instanceof JFormattedTextField);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JFormattedTextField)comp).getHorizontalAlignment(),JFormattedTextField.LEFT);
		Assert.assertEquals(((JFormattedTextField)comp).getValue(),new Time(0));

		fd = FieldDescriptor.newInstance("timestampValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,new Timestamp(0));
		
		Assert.assertTrue(comp instanceof JFormattedTextField);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JFormattedTextField)comp).getHorizontalAlignment(),JFormattedTextField.LEFT);
		Assert.assertEquals(((JFormattedTextField)comp).getValue(),new Timestamp(0));

		fd = FieldDescriptor.newInstance("textValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,"test");
		
		Assert.assertTrue(comp instanceof JTextField);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JTextField)comp).getHorizontalAlignment(),JTextField.LEFT);
		Assert.assertEquals(((JTextField)comp).getText(),"test");

		fd = FieldDescriptor.newInstance("formattedTextValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,"test");
		
		Assert.assertTrue(comp instanceof JTextField);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JTextField)comp).getHorizontalAlignment(),JTextField.LEFT);
		Assert.assertEquals(((JTextField)comp).getText(),"test");

		fd = FieldDescriptor.newInstance("passwordValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,"test");
		
		Assert.assertTrue(comp instanceof JPasswordField);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JPasswordField)comp).getHorizontalAlignment(),JPasswordField.LEFT);

		fd = FieldDescriptor.newInstance("enumValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,FormRepresentation.LIST);
		
		Assert.assertTrue(comp instanceof JComboBox);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JComboBox<?>)comp).getSelectedItem(),FormRepresentation.LIST);

		fd = FieldDescriptor.newInstance("stringArrayValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,new String[]{"test"});
		
		Assert.assertTrue(comp instanceof JList);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertArrayEquals(((JList<?>)comp).getSelectedValuesList().toArray(),new String[]{"test"});

		fd = FieldDescriptor.newInstance("listArrayValue",new FormFieldFormat(),TotalClass.class);
		comp = SwingUtils.prepareCellEditorComponent(localizer,fd,Arrays.asList("test"));
		
		Assert.assertTrue(comp instanceof JList);
		Assert.assertEquals(comp.getBackground(),SwingUtils.OPTIONAL_BACKGROUND);
		Assert.assertEquals(comp.getForeground(),SwingUtils.OPTIONAL_FOREGROUND);
		Assert.assertEquals(((JList<?>)comp).getSelectedValuesList(),Arrays.asList("test"));
	}

	@Test
	public void localesTest() {
		
	}

	@Test
	public void helpTest() {
		
	}
}
