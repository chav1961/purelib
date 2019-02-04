package chav1961.purelib.ui.swing;

import java.util.Date;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfacers.Action;
import chav1961.purelib.ui.interfacers.Format;

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/localization")
@LocaleResource(value="titleHelpScreen",tooltip="titleHelpScreen")	
@Action(resource=@LocaleResource(value="calculate",tooltip="calculateTooltip"),actionString="calculate",simulateCheck=true) 
public class PseudoData {
	@LocaleResource(value="titleHelpScreen",tooltip="titleHelpScreen")	
	@Format("10.3ms")
	public Date		date1;
}