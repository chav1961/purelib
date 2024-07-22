package chav1961.purelib.sql;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.Format;


@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":xml:root://chav1961.purelib.sql.PseudoRecord/chav1961/purelib/i18n/localization.xml")
@LocaleResource(value="testSet1",tooltip="testSet1")
public class PseudoRecord {
	@Format("")
	String 	field1;
	@Format("")
	byte	field2;
	@Format("")
	short	field3;
	@Format("")
	char	field4;
	@Format("")
	int		field5;
	@Format("")
	long	field6;
	@Format("")
	float	field7;
	@Format("")
	double	field8;
	@Format("")
	boolean	field9;
}
