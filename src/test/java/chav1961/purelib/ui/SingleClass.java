package chav1961.purelib.ui;

import chav1961.purelib.i18n.interfaces.LocaleResource;

public class SingleClass {
	Integer id;
	transient int field0;
	int	field1;
	@LocaleResource(value="field2Name",tooltip="field2Tooltip")
	String field2;
	public boolean field3;

	public SingleClass() {
		this(0,0,1,"field2",true);
	}
	
	public SingleClass(final int id, final int field0, final int field1, final String field2, final boolean field3) {
		this.id = id;
		this.field0 = field0;
		this.field1 = field1;
		this.field2 = field2;
		this.field3 = field3;
	}

	@Override
	public String toString() {
		return "SingleClass [id=" + id + ", field1=" + field1 + ", field2=" + field2 + ", field3=" + field3 + "]";
	}
}
