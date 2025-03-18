package chav1961.purelib.ui;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormRepresentation;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.Wizard;

@LocaleResourceLocation("test")
public class TotalClass {
	int					id;
	@LocaleResource(value="myCheckBox",tooltip="myCheckBoxTooltip")
	boolean				checkbox;
	@LocaleResource(value="myIntValue",tooltip="myIntValueTooltip")
	long				intvalue;
	double				realvalue;
	BigDecimal			bigDecimalValue;
	Date				dateValue;
	Time				timeValue;
	Timestamp			timestampValue;
	String				textValue;
	@Format("ANNNNNNN")
	String				formattedTextValue;
	String				formattedAreaValue;
	char[]				passwordValue;
	FormRepresentation	enumValue;
	String[]			stringArrayValue;
	@Format("ANNNNNNN")
	List<String>		listArrayValue;
	Map<String,String>	mapValue;
	@Wizard("MyWizard")
	SingleClass			wizardValue;
	SingleClass			keyValuePairValue;
	
	public TotalClass() {
	}
}
