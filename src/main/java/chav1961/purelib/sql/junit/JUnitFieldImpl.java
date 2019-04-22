package chav1961.purelib.sql.junit;

import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.sql.RsMetaDataElement;

class JUnitFieldImpl extends JUnitEntityImpl {
	private final String			name;
	private final RsMetaDataElement	metaData;
	private final GetterAndSetter	gas;
	
	JUnitFieldImpl(final String name, final RsMetaDataElement desc, final GetterAndSetter gas) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Field name can't be null or empty"); 
		}
		else if (desc == null) {
			throw new NullPointerException("Field descriptor for field ["+name+"] can't be null"); 
		}
		else if (gas == null) {
			throw new NullPointerException("Getter and setter for field ["+name+"] can't be null"); 
		}
		else {
			this.name = name;
			this.metaData = desc;
			this.gas = gas;
		}
	}

	@Override
	public JUnitEntityType getType() {
		return JUnitEntityType.FIELD;
	}

	public GetterAndSetter getGas() {
		return gas;
	}
	
	public String getFieldName() {
		return name;
	}
	
	public RsMetaDataElement getMetaData() {
		return metaData;
	}

	@Override
	public String toString() {
		return "JUnitFieldImpl [name=" + name + ", metaData=" + metaData + "]";
	}
}
