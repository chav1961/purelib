package chav1961.purelib.sql.model;

import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.interfaces.DomainType;

class FieldAccessor {
	final String				fieldName;
	final ContentNodeMetadata	classModel;
	final ContentNodeMetadata	tableModel;
	final GetterAndSetter		gas;
	final DomainType			domain;
	final boolean				isMandatory;
	final int					pkSeq;
	
	FieldAccessor(ContentNodeMetadata classModel, ContentNodeMetadata tableModel, GetterAndSetter gas, DomainType domain, boolean isMandatory, int pkSeq) {
		this.fieldName = classModel.getName();
		this.classModel = classModel;
		this.tableModel = tableModel;
		this.gas = gas;
		this.domain = domain;
		this.isMandatory = isMandatory;
		this.pkSeq = pkSeq;
	}

	@Override
	public String toString() {
		return "FieldAccessor [fieldName=" + fieldName + ", classModel=" + classModel + ", tableModel=" + tableModel + ", domain=" + domain + ", isMandatory=" + isMandatory + ", pkSeq=" + pkSeq + "]";
	}
}