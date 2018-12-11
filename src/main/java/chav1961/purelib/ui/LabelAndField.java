package chav1961.purelib.ui;

import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;

public class LabelAndField<LabelType,FieldType> {
	public final LabelType			label;
	public final String				labelId;
	public final String				labelToolTipId;
	public final FieldType			field;
	public final FieldDescriptor	fieldDesc;
	
	public LabelAndField(final LabelType label, final String labelId, final String labelToolTipId, final FieldType field, final FieldDescriptor fieldDesc) {
		this.label = label;
		this.labelId = labelId;
		this.labelToolTipId = labelToolTipId;
		this.field = field;
		this.fieldDesc = fieldDesc;
	}

	@Override
	public String toString() {
		return "LabelAndField [label=" + label + ", labelId=" + labelId + ", labelToolTipId=" + labelToolTipId + ", field=" + field + ", fieldDesc=" + fieldDesc + "]";
	}
}