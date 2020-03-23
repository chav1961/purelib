package chav1961.purelib.sql;

import chav1961.purelib.i18n.interfaces.LocaleResource;

@LocaleResource(value="value",tooltip="tooltip")
public class SimpleProviderRecord implements Cloneable {
	@LocaleResource(value="value",tooltip="tooltip")
	public int		x;

	@LocaleResource(value="value",tooltip="tooltip")
	public int		f1;

	@LocaleResource(value="value",tooltip="tooltip")
	public String	f2;
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
