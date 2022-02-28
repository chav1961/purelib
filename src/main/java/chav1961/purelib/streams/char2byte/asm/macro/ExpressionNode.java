package chav1961.purelib.streams.char2byte.asm.macro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.streams.char2byte.asm.AssignableExpressionNodeInterface;
import chav1961.purelib.streams.char2byte.asm.ExpressionNodeInterface;
import chav1961.purelib.streams.char2byte.asm.ExpressionNodeType;

abstract class ExpressionNode implements ExpressionNodeInterface {}

class ConstantNode extends ExpressionNode {
	ExpressionNodeValue	valueType;
	long				longValue;
	double				doubleValue;
	char[] 				stringValue;
	boolean 			booleanValue;
	long[]				longArrayValue;
	double[]			doubleArrayValue;
	char[][]			stringArrayValue;
	boolean[] 			booleanArrayValue;
	
	ConstantNode(final long value) {
		this.valueType = ExpressionNodeValue.INTEGER;
		this.longValue = value;
		this.doubleValue = 0;
		this.stringValue = null;
		this.booleanValue = false;
		this.longArrayValue = null;
		this.doubleArrayValue = null;
		this.stringArrayValue = null;
		this.booleanArrayValue = null;
	}

	ConstantNode(final long... value) {
		this.valueType = ExpressionNodeValue.INTEGER_ARRAY;
		this.longValue = 0;
		this.doubleValue = 0;
		this.stringValue = null;
		this.booleanValue = false;
		this.longArrayValue = value;
		this.doubleArrayValue = null;
		this.stringArrayValue = null;
		this.booleanArrayValue = null;
	}
	
	ConstantNode(final double value) {
		this.valueType = ExpressionNodeValue.REAL; 
		this.doubleValue = value;
		this.longValue = 0;
		this.stringValue = null;
		this.booleanValue = false;
		this.longArrayValue = null;
		this.doubleArrayValue = null;
		this.stringArrayValue = null;
		this.booleanArrayValue = null;
	}

	ConstantNode(final double... value) {
		this.valueType = ExpressionNodeValue.REAL_ARRAY; 
		this.doubleValue = 0;
		this.longValue = 0;
		this.stringValue = null;
		this.booleanValue = false;
		this.longArrayValue = null;
		this.doubleArrayValue = value;
		this.stringArrayValue = null;
		this.booleanArrayValue = null;
	}

	ConstantNode(final char[] value) {
		this.valueType = ExpressionNodeValue.STRING; 
		this.stringValue = value;
		this.longValue = 0;
		this.doubleValue = 0;
		this.booleanValue = false;
		this.longArrayValue = null;
		this.doubleArrayValue = null;
		this.stringArrayValue = null;
		this.booleanArrayValue = null;
	}
	
	ConstantNode(final char[]... value) {
		this.valueType = ExpressionNodeValue.STRING_ARRAY; 
		this.stringValue = null;
		this.longValue = 0;
		this.doubleValue = 0;
		this.booleanValue = false;
		this.longArrayValue = null;
		this.doubleArrayValue = null;
		this.stringArrayValue = value;
		this.booleanArrayValue = null;
	}
	
	ConstantNode(final boolean value) {
		this.valueType = ExpressionNodeValue.BOOLEAN; 
		this.booleanValue = value;
		this.longValue = 0;
		this.doubleValue = 0;
		this.stringValue = null;
		this.longArrayValue = null;
		this.doubleArrayValue = null;
		this.stringArrayValue = null;
		this.booleanArrayValue = null;
	}

	ConstantNode(final boolean... value) {
		this.valueType = ExpressionNodeValue.BOOLEAN_ARRAY; 
		this.booleanValue = false;
		this.longValue = 0;
		this.doubleValue = 0;
		this.stringValue = null;
		this.longArrayValue = null;
		this.doubleArrayValue = null;
		this.stringArrayValue = null;
		this.booleanArrayValue = value;
	}

	ConstantNode(final ExpressionNodeValue valueType, final ExpressionNode... values) throws CalculationException {
		this.valueType = valueType; 
		this.longValue = 0;
		this.doubleValue = 0;
		this.stringValue = null;
		this.booleanValue = false;
		this.longArrayValue = null;
		this.doubleArrayValue = null;
		this.stringArrayValue = null;
		this.booleanArrayValue = null;
		
		switch (valueType) {
			case BOOLEAN_ARRAY	:
				this.booleanArrayValue = new boolean[values.length];
				
				for (int index = 0; index < values.length; index++) {
					this.booleanArrayValue[index] = values[index].getBoolean();
				}
				break;
			case INTEGER_ARRAY	:
				this.longArrayValue = new long[values.length];
				
				for (int index = 0; index < values.length; index++) {
					this.longArrayValue[index] = values[index].getLong();
				}
				break;
			case REAL_ARRAY		:
				this.doubleArrayValue = new double[values.length];
				
				for (int index = 0; index < values.length; index++) {
					this.doubleArrayValue[index] = values[index].getDouble();
				}
				break;
			case STRING_ARRAY	:
				this.stringArrayValue = new char[values.length][];
				
				for (int index = 0; index < values.length; index++) {
					this.stringArrayValue[index] = values[index].getString();
				}
				break;
			default :
				throw new CalculationException("Illegal value type ["+valueType+"] to assign content");
		}
	}
	
	@Override public ExpressionNodeType getType() {return ExpressionNodeType.CONSTANT;}
	@Override public ExpressionNodeValue getValueType() {return valueType;}
	
	@Override 
	public long getLong() throws CalculationException {
		if (valueType == ExpressionNodeValue.INTEGER) {
			return longValue;
		}
		else {
			throw new CalculationException("Attempt to get integer value for ["+valueType+"] constant");
		}
	}
	
	@Override 
	public double getDouble() throws CalculationException {
		if (valueType == ExpressionNodeValue.REAL) {
			return doubleValue;
		}
		else {
			throw new CalculationException("Attempt to get double value for ["+valueType+"] constant");
		}
	}
	
	@Override 
	public char[] getString() throws CalculationException {
		if (valueType == ExpressionNodeValue.STRING) {
			return stringValue;
		}
		else {
			throw new CalculationException("Attempt to get string value for ["+valueType+"] constant");
		}
	}
	
	@Override 
	public boolean getBoolean() throws CalculationException {
		if (valueType == ExpressionNodeValue.BOOLEAN) {
			return booleanValue;
		}
		else {
			throw new CalculationException("Attempt to get boolean value for ["+valueType+"] constant");
		}
	}

	@Override 
	public long getLong(final long index) throws CalculationException {
		if (valueType == ExpressionNodeValue.INTEGER_ARRAY) {
			return longArrayValue[toInt(index)];
		}
		else {
			throw new CalculationException("Attempt to get integer value for ["+valueType+"] constant");
		}
	}
	
	@Override 
	public double getDouble(final long index) throws CalculationException {
		if (valueType == ExpressionNodeValue.REAL_ARRAY) {
			return doubleArrayValue[toInt(index)];
		}
		else {
			throw new CalculationException("Attempt to get double value for ["+valueType+"] constant");
		}
	}
	
	@Override 
	public char[] getString(final long index) throws CalculationException {
		if (valueType == ExpressionNodeValue.STRING_ARRAY) {
			return stringArrayValue[toInt(index)];
		}
		else {
			throw new CalculationException("Attempt to get string value for ["+valueType+"] constant");
		}
	}
	
	@Override 
	public boolean getBoolean(final long index) throws CalculationException {
		if (valueType == ExpressionNodeValue.BOOLEAN_ARRAY) {
			return booleanArrayValue[toInt(index)];
		}
		else {
			throw new CalculationException("Attempt to get boolean value for ["+valueType+"] constant");
		}
	}

	@Override
	public long[] getLongContent() throws CalculationException {
		if (valueType == ExpressionNodeValue.INTEGER_ARRAY) {
			return longArrayValue;
		}
		else {
			throw new CalculationException("Attempt to get long array for ["+valueType+"] constant");
		}
	}

	@Override
	public double[] getDoubleContent() throws CalculationException {
		if (valueType == ExpressionNodeValue.REAL_ARRAY) {
			return doubleArrayValue;
		}
		else {
			throw new CalculationException("Attempt to get double array for ["+valueType+"] constant");
		}
	}

	@Override
	public char[][] getStringContent() throws CalculationException {
		if (valueType == ExpressionNodeValue.STRING_ARRAY) {
			return stringArrayValue;
		}
		else {
			throw new CalculationException("Attempt to get string array for ["+valueType+"] constant");
		}
	}

	@Override
	public boolean[] getBooleanContent() throws CalculationException {
		if (valueType == ExpressionNodeValue.STRING_ARRAY) {
			return booleanArrayValue;
		}
		else {
			throw new CalculationException("Attempt to get boolean array for ["+valueType+"] constant");
		}
	}
	
	@Override 
	public int getSize() throws CalculationException {
		switch (valueType) {
			case BOOLEAN_ARRAY :
				if (booleanArrayValue != null) {
					return booleanArrayValue.length; 
				}
				else {
					throw new CalculationException("Attempt to get size of null"); 
				}
			case INTEGER_ARRAY:
				if (longArrayValue != null) {
					return longArrayValue.length; 
				}
				else {
					throw new CalculationException("Attempt to get size of null"); 
				}
			case REAL_ARRAY:
				if (doubleArrayValue != null) {
					return doubleArrayValue.length; 
				}
				else {
					throw new CalculationException("Attempt to get size of null"); 
				}
			case STRING_ARRAY:
				if (stringArrayValue != null) {
					return stringArrayValue.length; 
				}
				else {
					throw new CalculationException("Attempt to get size of null"); 
				}
			default:
				throw new CalculationException("Attempt to get size of non-array or null value"); 
		}
	}
	
	@Override
	public String toString() {
		switch (valueType) {
			case INTEGER		: return "ConstantNode [valueType=" + valueType + ", " + longValue + "]";
			case REAL			: return "ConstantNode [valueType=" + valueType + ", " + doubleValue+ "]";
			case STRING			: return "ConstantNode [valueType=" + valueType + ", " + (stringValue == null ? "null" : '\"' + new String(stringValue) + '\"') + "]";
			case BOOLEAN		: return "ConstantNode [valueType=" + valueType + ", " + booleanValue + "]";
			case INTEGER_ARRAY	: return "ConstantNode [valueType=" + valueType + ", " + (doubleArrayValue == null ? "null" : Arrays.toString(longArrayValue)) + "]";
			case REAL_ARRAY		: return "ConstantNode [valueType=" + valueType + ", " + (doubleArrayValue == null ? "null" : Arrays.toString(doubleArrayValue)) + "]";
			case STRING_ARRAY	: return "ConstantNode [valueType=" + valueType + ", " + (stringArrayValue == null ? "null" : '\"' + Arrays.toString(toString(stringArrayValue)) + '\"') + "]";
			case BOOLEAN_ARRAY	: return "ConstantNode [valueType=" + valueType + ", " + (booleanArrayValue == null ? "null" : Arrays.toString(booleanArrayValue)) + "]";
			default				: return super.toString();
		}
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(booleanArrayValue);
		result = prime * result + (booleanValue ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(doubleArrayValue);
		long temp;
		temp = Double.doubleToLongBits(doubleValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(longArrayValue);
		result = prime * result + (int) (longValue ^ (longValue >>> 32));
		result = prime * result + Arrays.deepHashCode(stringArrayValue);
		result = prime * result + Arrays.hashCode(stringValue);
		result = prime * result + ((valueType == null) ? 0 : valueType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ConstantNode other = (ConstantNode) obj;
		if (!Arrays.equals(booleanArrayValue, other.booleanArrayValue)) return false;
		if (booleanValue != other.booleanValue) return false;
		if (!Arrays.equals(doubleArrayValue, other.doubleArrayValue)) return false;
		if (Double.doubleToLongBits(doubleValue) != Double.doubleToLongBits(other.doubleValue)) return false;
		if (!Arrays.equals(longArrayValue, other.longArrayValue)) return false;
		if (longValue != other.longValue) return false;
		if (!Arrays.deepEquals(stringArrayValue, other.stringArrayValue)) return false;
		if (!Arrays.equals(stringValue, other.stringValue)) return false;
		if (valueType != other.valueType) return false;
		return true;
	}

	private int toInt(final long index) throws CalculationException {
		if (index < 0) {
			throw new CalculationException("Negative index value ["+index+"] to get array element"); 
		}
		else {
			switch (getValueType()) {
				case BOOLEAN_ARRAY :
					if (index >= booleanArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than boolean array size ["+booleanArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case INTEGER_ARRAY :
					if (index >= longArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than integer array size ["+longArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case REAL_ARRAY :
					if (index >= doubleArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than real array size ["+doubleArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case STRING_ARRAY :
					if (index >= stringArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than string array size ["+stringArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				default:
					throw new CalculationException("Attempt to get array element for type ["+getValueType()+"]"); 
			}
		}
	}
	
	private String[] toString(final char[][] content) {
		final String[]	result = new String[content.length];
		
		for (int index = 0; index < result.length; index++) {
			result[index] = content[index] == null ? "null" : new String(content[index]);
		}
		return result;
	}

}

abstract class AssignableExpressionNode extends ExpressionNode implements AssignableExpressionNodeInterface {
	private int	sequentialNumber = 0;
	
	int getSequentialNumber() {
		return sequentialNumber;
	}
	
	void setSequentialNumber(final int number) {
		this.sequentialNumber = number;
	}
	
	public abstract char[] getName();
	public abstract void assign(final ExpressionNode node) throws CalculationException;
	public abstract void assign(final long value) throws CalculationException;
	public abstract void assign(final double value) throws CalculationException;
	public abstract void assign(final char[] value) throws CalculationException;
	public abstract void assign(final boolean value) throws CalculationException;
	public abstract void assign(final long index, final long value) throws CalculationException;
	public abstract void assign(final long index, final double value) throws CalculationException;
	public abstract void assign(final long index, final char[] value) throws CalculationException;
	public abstract void assign(final long index, final boolean value) throws CalculationException;
	public abstract boolean hasValue();
	
	@Override public AssignableExpressionNode clone() {return null;}
}

class PositionalParameter extends AssignableExpressionNode {
	private final char[]				name;
	private final ExpressionNodeValue	valueType;
	private ExpressionNode				currentValue = null;
	
	PositionalParameter(final char[] name, final ExpressionNodeValue valueType) {
		this.name = name;
		this.valueType = valueType;
	}

	@Override
	public char[] getName() {
		return name;
	}
	
	@Override
	public PositionalParameter clone() {
		return new PositionalParameter(name,valueType);
	}

	@Override
	public void assign(final ExpressionNode node) throws CalculationException {
		if (node != null && node.getValueType() != valueType) {
			throw new CalculationException("Positional parameter ["+getSequentialNumber()+"]: value to assign can't be casted to ["+valueType+"]");
		}
		this.currentValue = node;
	}

	@Override
	public void assign(final long value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.INTEGER) {
			throw new CalculationException("Positional parameter ["+getSequentialNumber()+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).longValue = value; 
		}
	}

	@Override
	public void assign(final double value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.REAL) {
			throw new CalculationException("Positional parameter ["+getSequentialNumber()+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).doubleValue = value; 
		}
	}

	@Override
	public void assign(final char[] value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.STRING) {
			throw new CalculationException("Positional parameter ["+getSequentialNumber()+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).stringValue = value; 
		}
	}

	@Override
	public void assign(final boolean value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.BOOLEAN) {
			throw new CalculationException("Positional parameter ["+getSequentialNumber()+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).booleanValue = value; 
		}
	}

	@Override
	public void assign(final long index, final long value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.INTEGER_ARRAY) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).longArrayValue[toInt(index)] = value; 
		}
	}

	@Override
	public void assign(final long index, final double value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.REAL_ARRAY) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).doubleArrayValue[toInt(index)] = value; 
		}
	}

	@Override
	public void assign(final long index, final char[] value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.STRING) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).stringArrayValue[toInt(index)] = value; 
		}
	}

	@Override
	public void assign(final long index, final boolean value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.BOOLEAN) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).booleanArrayValue[toInt(index)] = value; 
		}
	}
	
	@Override 
	public int getSize() throws CalculationException {
		return currentValue.getSize();
	}
	
	@Override public ExpressionNodeType getType() {return ExpressionNodeType.POSITIONAL_PARAMETER;}
	@Override public ExpressionNodeValue getValueType() {return valueType;}
	@Override public long getLong() throws CalculationException {return currentValue.getLong();}
	@Override public double getDouble() throws CalculationException {return currentValue.getDouble();}
	@Override public char[] getString() throws CalculationException {return currentValue.getString();}
	@Override public boolean getBoolean() throws CalculationException {return currentValue.getBoolean();}
	@Override public boolean hasValue() {return currentValue != null;}
	
	@Override public long getLong(final long index) throws CalculationException {return currentValue.getLong(index);}
	@Override public double getDouble(final long index) throws CalculationException {return currentValue.getDouble(index);}
	@Override public char[] getString(final long index) throws CalculationException {return currentValue.getString(index);}
	@Override public boolean getBoolean(final long index) throws CalculationException {return currentValue.getBoolean(index);}
	@Override public boolean hasValue(final long index) {return currentValue != null;}

	@Override public long[] getLongContent() throws CalculationException {return currentValue.getLongContent();}
	@Override public double[] getDoubleContent() throws CalculationException {return currentValue.getDoubleContent();}
	@Override public char[][] getStringContent() throws CalculationException {return currentValue.getStringContent();}
	@Override public boolean[] getBooleanContent() throws CalculationException {return currentValue.getBooleanContent();}
	
	@Override
	public String toString() {
		return "PositionalParameter [name=" + Arrays.toString(name) + ", currentValue=" + currentValue + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentValue == null) ? 0 : currentValue.hashCode());
		result = prime * result + Arrays.hashCode(name);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PositionalParameter other = (PositionalParameter) obj;
		if (currentValue == null) {
			if (other.currentValue != null) return false;
		} else if (!currentValue.equals(other.currentValue)) return false;
		if (!Arrays.equals(name, other.name)) return false;
		return true;
	}

	private int toInt(final long index) throws CalculationException {
		if (index < 0) {
			throw new CalculationException("Negative index value ["+index+"] to get array element"); 
		}
		else {
			switch (getValueType()) {
				case BOOLEAN_ARRAY :
					if (index >= ((ConstantNode)currentValue).booleanArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than boolean array size ["+((ConstantNode)currentValue).booleanArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case INTEGER_ARRAY :
					if (index >= ((ConstantNode)currentValue).longArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than integer array size ["+((ConstantNode)currentValue).longArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case REAL_ARRAY :
					if (index >= ((ConstantNode)currentValue).doubleArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than real array size ["+((ConstantNode)currentValue).doubleArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case STRING_ARRAY :
					if (index >= ((ConstantNode)currentValue).stringArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than string array size ["+((ConstantNode)currentValue).stringArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				default:
					throw new CalculationException("Attempt to get array element for type ["+getValueType()+"]"); 
			}
		}
	}
}

class KeyParameter extends AssignableExpressionNode {
	private final char[]				name;
	private final ExpressionNodeValue	valueType;
	private final ExpressionNode		defaultValue;
	private ExpressionNode				currentValue;
	
	KeyParameter(final char[] name, final ExpressionNodeValue valueType) {
		this(name,valueType,null);
	}

	KeyParameter(final char[] name, final ExpressionNodeValue valueType, final ExpressionNode initialValue) {
		this.name = name;
		this.valueType = valueType;
		this.defaultValue = this.currentValue = initialValue;
	}
	

	@Override
	public char[] getName() {
		return name;
	}
	
	@Override
	public KeyParameter clone() {
		return new KeyParameter(name,valueType,defaultValue);
	}

	@Override
	public void assign(final ExpressionNode node) throws CalculationException {
		if (node != null && node.getValueType() != valueType) {
			throw new CalculationException("Key parameter ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		this.currentValue = node;
	}
	
	@Override
	public void assign(final long value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.INTEGER) {
			throw new CalculationException("Key parameter ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).longValue = value; 
		}
	}

	@Override
	public void assign(final double value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.REAL) {
			throw new CalculationException("Key parameter ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).doubleValue = value; 
		}
	}

	@Override
	public void assign(final char[] value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.STRING) {
			throw new CalculationException("Key parameter ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).stringValue = value; 
		}
	}

	@Override
	public void assign(final boolean value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.BOOLEAN) {
			throw new CalculationException("Key parameter ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).booleanValue = value; 
		}
	}

	@Override
	public void assign(final long index, final long value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.INTEGER_ARRAY) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).longArrayValue[toInt(index)] = value; 
		}
	}

	@Override
	public void assign(final long index, final double value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.REAL_ARRAY) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).doubleArrayValue[toInt(index)] = value; 
		}
	}

	@Override
	public void assign(final long index, final char[] value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.STRING) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).stringArrayValue[toInt(index)] = value; 
		}
	}

	@Override
	public void assign(final long index, final boolean value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.BOOLEAN) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).booleanArrayValue[toInt(index)] = value; 
		}
	}

	@Override 
	public int getSize() throws CalculationException {
		return currentValue.getSize();
	}
	
	@Override public ExpressionNodeType getType() {return ExpressionNodeType.KEY_PARAMETER;}
	@Override public ExpressionNodeValue getValueType() {return valueType;}
	@Override public long getLong() throws CalculationException {return currentValue.getLong();}
	@Override public double getDouble() throws CalculationException {return currentValue.getDouble();}
	@Override public char[] getString() throws CalculationException {return currentValue.getString();}
	@Override public boolean getBoolean() throws CalculationException {return currentValue.getBoolean();}
	@Override public boolean hasValue() {return currentValue != null;}

	@Override public long getLong(final long index) throws CalculationException {return currentValue.getLong(index);}
	@Override public double getDouble(final long index) throws CalculationException {return currentValue.getDouble(index);}
	@Override public char[] getString(final long index) throws CalculationException {return currentValue.getString(index);}
	@Override public boolean getBoolean(final long index) throws CalculationException {return currentValue.getBoolean(index);}
	@Override public boolean hasValue(final long index) {return currentValue != null;}

	@Override public long[] getLongContent() throws CalculationException {return currentValue.getLongContent();}
	@Override public double[] getDoubleContent() throws CalculationException {return currentValue.getDoubleContent();}
	@Override public char[][] getStringContent() throws CalculationException {return currentValue.getStringContent();}
	@Override public boolean[] getBooleanContent() throws CalculationException {return currentValue.getBooleanContent();}
	
	@Override
	public String toString() {
		return "KeyParameter [name=" + Arrays.toString(name) + ", defaultValue=" + defaultValue + ", currentValue=" + currentValue + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentValue == null) ? 0 : currentValue.hashCode());
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + Arrays.hashCode(name);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		KeyParameter other = (KeyParameter) obj;
		if (currentValue == null) {
			if (other.currentValue != null) return false;
		} else if (!currentValue.equals(other.currentValue)) return false;
		if (defaultValue == null) {
			if (other.defaultValue != null) return false;
		} else if (!defaultValue.equals(other.defaultValue)) return false;
		if (!Arrays.equals(name, other.name)) return false;
		return true;
	}

	private int toInt(final long index) throws CalculationException {
		if (index < 0) {
			throw new CalculationException("Negative index value ["+index+"] to get array element"); 
		}
		else {
			switch (getValueType()) {
				case BOOLEAN_ARRAY :
					if (index >= ((ConstantNode)currentValue).booleanArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than boolean array size ["+((ConstantNode)currentValue).booleanArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case INTEGER_ARRAY :
					if (index >= ((ConstantNode)currentValue).longArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than integer array size ["+((ConstantNode)currentValue).longArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case REAL_ARRAY :
					if (index >= ((ConstantNode)currentValue).doubleArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than real array size ["+((ConstantNode)currentValue).doubleArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case STRING_ARRAY :
					if (index >= ((ConstantNode)currentValue).stringArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than string array size ["+((ConstantNode)currentValue).stringArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				default:
					throw new CalculationException("Attempt to get array element for type ["+getValueType()+"]"); 
			}
		}
	}
}

class LocalVariable extends AssignableExpressionNode {
	private final char[]				name;
	private final ExpressionNodeValue	valueType;
	private final ExpressionNode		initialValue;
	private ExpressionNode				currentValue;
	
	LocalVariable(final char[] name, final ExpressionNodeValue valueType) {
		this(name,valueType,null);
	}

	LocalVariable(final char[] name, final ExpressionNodeValue valueType, final ExpressionNode initialValue) {
		this.name = name;
		this.valueType = valueType;
		this.initialValue = this.currentValue = initialValue;
	}

	@Override
	public char[] getName() {
		return name;
	}
	
	@Override
	public LocalVariable clone() {
		return new LocalVariable(name,valueType,initialValue);
	}

	@Override
	public void assign(final ExpressionNode node) throws CalculationException {
		if (node != null && node.getValueType() != valueType) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		this.currentValue = node;
	}

	@Override
	public void assign(final long value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.INTEGER) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).longValue = value; 
		}
	}

	@Override
	public void assign(final double value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.REAL) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).doubleValue = value; 
		}
	}

	@Override
	public void assign(final char[] value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.STRING) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).stringValue = value; 
		}
	}

	@Override
	public void assign(final boolean value) throws CalculationException {
		if (currentValue == null) {
			assign(new ConstantNode(value));
		}
		else if (valueType != ExpressionNodeValue.BOOLEAN) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).valueType = valueType; 
			((ConstantNode)currentValue).booleanValue = value; 
		}
	}

	@Override
	public void assign(final long index, final long value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.INTEGER_ARRAY) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).longArrayValue[toInt(index)] = value; 
		}
	}

	@Override
	public void assign(final long index, final double value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.REAL_ARRAY) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).doubleArrayValue[toInt(index)] = value; 
		}
	}

	@Override
	public void assign(final long index, final char[] value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.STRING_ARRAY) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).stringArrayValue[toInt(index)] = value; 
		}
	}

	@Override
	public void assign(final long index, final boolean value) throws CalculationException {
		if (currentValue == null || valueType != ExpressionNodeValue.BOOLEAN_ARRAY) {
			throw new CalculationException("Local variable ["+new String(getName())+"]: value to assign can't be casted to ["+valueType+"]");
		}
		else {
			((ConstantNode)currentValue).booleanArrayValue[toInt(index)] = value; 
		}
	}

	@Override 
	public int getSize() throws CalculationException {
		return currentValue.getSize();
	}
	
	@Override public ExpressionNodeType getType() {return ExpressionNodeType.LOCAL_VARIABLE;}
	@Override public ExpressionNodeValue getValueType() {return valueType;}
	@Override public long getLong() throws CalculationException {return currentValue.getLong();}
	@Override public double getDouble() throws CalculationException {return currentValue.getDouble();}
	@Override public char[] getString() throws CalculationException {return currentValue.getString();}
	@Override public boolean getBoolean() throws CalculationException {return currentValue.getBoolean();}
	@Override public boolean hasValue() {return currentValue != null;}

	@Override public long getLong(final long index) throws CalculationException {return currentValue.getLong(index);}
	@Override public double getDouble(final long index) throws CalculationException {return currentValue.getDouble(index);}
	@Override public char[] getString(final long index) throws CalculationException {return currentValue.getString(index);}
	@Override public boolean getBoolean(final long index) throws CalculationException {return currentValue.getBoolean(index);}
	@Override public boolean hasValue(final long index) {return currentValue != null;}

	@Override public long[] getLongContent() throws CalculationException {return currentValue.getLongContent();}
	@Override public double[] getDoubleContent() throws CalculationException {return currentValue.getDoubleContent();}
	@Override public char[][] getStringContent() throws CalculationException {return currentValue.getStringContent();}
	@Override public boolean[] getBooleanContent() throws CalculationException {return currentValue.getBooleanContent();}
	
	@Override
	public String toString() {
		return "LocalVariable [name=" + Arrays.toString(name) + ", initialValue=" + initialValue + ", currentValue=" + currentValue + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentValue == null) ? 0 : currentValue.hashCode());
		result = prime * result + ((initialValue == null) ? 0 : initialValue.hashCode());
		result = prime * result + Arrays.hashCode(name);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		LocalVariable other = (LocalVariable) obj;
		if (currentValue == null) {
			if (other.currentValue != null) return false;
		} else if (!currentValue.equals(other.currentValue)) return false;
		if (initialValue == null) {
			if (other.initialValue != null) return false;
		} else if (!initialValue.equals(other.initialValue)) return false;
		if (!Arrays.equals(name, other.name)) return false;
		return true;
	}

	private int toInt(final long index) throws CalculationException {
		if (index < 0) {
			throw new CalculationException("Negative index value ["+index+"] to get array element"); 
		}
		else {
			switch (getValueType()) {
				case BOOLEAN_ARRAY :
					if (index >= ((ConstantNode)currentValue).booleanArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than boolean array size ["+((ConstantNode)currentValue).booleanArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case INTEGER_ARRAY :
					if (index >= ((ConstantNode)currentValue).longArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than integer array size ["+((ConstantNode)currentValue).longArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case REAL_ARRAY :
					if (index >= ((ConstantNode)currentValue).doubleArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than real array size ["+((ConstantNode)currentValue).doubleArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				case STRING_ARRAY :
					if (index >= ((ConstantNode)currentValue).stringArrayValue.length) {
						throw new CalculationException("Index value ["+index+"] is greater than string array size ["+((ConstantNode)currentValue).stringArrayValue.length+"] to get array element"); 
					}
					else {
						return (int)index;
					}
				default:
					throw new CalculationException("Attempt to get array element for type ["+getValueType()+"]"); 
			}
		}
	}
}

abstract class OperatorNode extends ExpressionNode {
	private final ExpressionNodeOperator	op;
	
	OperatorNode(final ExpressionNodeOperator op) {
		this.op = op;
	}

	abstract ExpressionNode[] getOperands();
	
	@Override public ExpressionNodeType getType() {return ExpressionNodeType.EXPRESSION;}
	ExpressionNodeOperator getOperator() {return op;}
}

abstract class UnaryOperatorNode extends OperatorNode {
	UnaryOperatorNode(final ExpressionNodeOperator op) {
		super(op);
	}
}

class NotNode extends UnaryOperatorNode {
	private final ExpressionNode[]	nested;  

	NotNode(final ExpressionNode nested) {
		super(ExpressionNodeOperator.NOT);
		this.nested = new ExpressionNode[]{nested};
	}

	@Override 
	public int getSize() throws CalculationException {
		throw new CalculationException("Attempt to get size of expression");
	}
	
	@Override ExpressionNode[] getOperands() {return nested;}
	@Override public ExpressionNodeValue getValueType() {return ExpressionNodeValue.BOOLEAN;}
	@Override public long getLong() throws CalculationException {throw new CalculationException("Attempt to get integer value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double getDouble() throws CalculationException {throw new CalculationException("Attempt to get real value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[] getString() throws CalculationException {throw new CalculationException("Attempt to get string value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean getBoolean() throws CalculationException {return !nested[0].getBoolean();}

	@Override public long getLong(final long index) throws CalculationException {throw new CalculationException("Attempt to get integer array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double getDouble(final long index) throws CalculationException {throw new CalculationException("Attempt to get real array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[] getString(final long index) throws CalculationException {throw new CalculationException("Attempt to get string array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean getBoolean(final long index) throws CalculationException {throw new CalculationException("Attempt to get boolean array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}

	@Override public long[] getLongContent() throws CalculationException {throw new CalculationException("Attempt to get long array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double[] getDoubleContent() throws CalculationException {throw new CalculationException("Attempt to get double array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[][] getStringContent() throws CalculationException {throw new CalculationException("Attempt to get string array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean[] getBooleanContent() throws CalculationException {throw new CalculationException("Attempt to get boolean array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	
	@Override
	public String toString() {
		return "NotNode [nested=" + Arrays.toString(nested) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(nested);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		NotNode other = (NotNode) obj;
		if (!Arrays.equals(nested, other.nested)) return false;
		return true;
	}
}

class NegNode extends UnaryOperatorNode {
	private final ExpressionNode[]	nested;  

	NegNode(final ExpressionNode nested) {
		super(ExpressionNodeOperator.NEG);
		this.nested = new ExpressionNode[]{nested};
	}

	@Override 
	public int getSize() throws CalculationException {
		throw new CalculationException("Attempt to get size of expression");
	}
	
	@Override ExpressionNode[] getOperands() {return nested;}
	@Override public ExpressionNodeValue getValueType() {return nested[0].getValueType();}
	@Override public char[] getString() throws CalculationException {throw new CalculationException("Attempt to get string value for ["+getValueType()+"] operator ");}
	@Override public boolean getBoolean() throws CalculationException {throw new CalculationException("Attempt to get string value for ["+getValueType()+"] operator ");}

	@Override 
	public long getLong() throws CalculationException {
		if (nested[0].getValueType() == ExpressionNodeValue.INTEGER) {
			return - nested[0].getLong();
		}
		else {
			throw new CalculationException("Attempt to get integer value for ["+nested[0].getValueType()+"] operator ");
		}
	}
	
	@Override 
	public double getDouble() throws CalculationException {
		if (nested[0].getValueType() == ExpressionNodeValue.REAL) {
			return - nested[0].getDouble();
		}
		else {
			throw new CalculationException("Attempt to get real value for ["+nested[0].getValueType()+"] operator ");
		}
	}

	@Override public long getLong(final long index) throws CalculationException {throw new CalculationException("Attempt to get integer array value for ["+getValueType()+"] operator ");}
	@Override public double getDouble(final long index) throws CalculationException {throw new CalculationException("Attempt to get real array value for ["+getValueType()+"] operator ");}
	@Override public char[] getString(final long index) throws CalculationException {throw new CalculationException("Attempt to get string array value for ["+getValueType()+"] operator ");}
	@Override public boolean getBoolean(final long index) throws CalculationException {throw new CalculationException("Attempt to get boolean array value for ["+getValueType()+"] operator ");}

	@Override public long[] getLongContent() throws CalculationException {throw new CalculationException("Attempt to get long array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double[] getDoubleContent() throws CalculationException {throw new CalculationException("Attempt to get double array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[][] getStringContent() throws CalculationException {throw new CalculationException("Attempt to get string array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean[] getBooleanContent() throws CalculationException {throw new CalculationException("Attempt to get boolean array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	
	@Override
	public String toString() {
		return "NegNode [nested=" + Arrays.toString(nested) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(nested);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		NegNode other = (NegNode) obj;
		if (!Arrays.equals(nested, other.nested)) return false;
		return true;
	}
}

abstract class BinaryOperatorNode extends OperatorNode {
	BinaryOperatorNode(final ExpressionNodeOperator op) {
		super(op);
	}

	@Override
	public String toString() {
		return "BinaryOperatorNode [getOperands()=" + Arrays.toString(getOperands()) + ", getOperator()=" + getOperator() + "]";
	}
}

class ArrayAccessNode extends BinaryOperatorNode {
	private final ExpressionNode[]	node;

	ArrayAccessNode(final ExpressionNode left, final ExpressionNode right) {
		super(ExpressionNodeOperator.ARR_GET);
		this.node = new ExpressionNode[]{left, right};
	}

	@Override ExpressionNode[] getOperands() {return node;}

	@Override 
	public ExpressionNodeValue getValueType() {
		switch (node[0].getValueType()) {
			case BOOLEAN_ARRAY	: return ExpressionNodeValue.BOOLEAN; 
			case INTEGER_ARRAY	: return ExpressionNodeValue.INTEGER;
			case REAL_ARRAY		: return ExpressionNodeValue.REAL;
			case STRING_ARRAY	: return ExpressionNodeValue.STRING;
			default				: return node[0].getValueType();
		}
	}
	
	@Override
	public long getLong() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.INTEGER) {
			return node[0].getLong();
		}
		else {
			throw new CalculationException("Attempt to get long value for ["+node[0].getValueType()+"] operator ");		
		}
	}

	@Override
	public double getDouble() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.REAL) {
			return node[0].getDouble();
		}
		else {
			throw new CalculationException("Attempt to get double value for ["+node[0].getValueType()+"] operator ");		
		}
	}
	
	@Override
	public char[] getString() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.STRING) {
			return node[0].getString();
		}
		else {
			throw new CalculationException("Attempt to get string value for ["+node[0].getValueType()+"] operator ");		
		}
	}

	@Override
	public boolean getBoolean() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.BOOLEAN) {
			return node[0].getBoolean();
		}
		else {
			throw new CalculationException("Attempt to get boolean value for ["+node[0].getValueType()+"] operator ");		
		}
	}

	@Override 
	public int getSize() throws CalculationException {
		return node[0].getSize();
	}
	
	@Override public long getLong(final long index) throws CalculationException {throw new CalculationException("Attempt to get integer array value for ["+getValueType()+"] operator ");}
	@Override public double getDouble(final long index) throws CalculationException {throw new CalculationException("Attempt to get real array value for ["+getValueType()+"] operator ");}
	@Override public char[] getString(final long index) throws CalculationException {throw new CalculationException("Attempt to get string array value for ["+getValueType()+"] operator ");}
	@Override public boolean getBoolean(final long index) throws CalculationException {throw new CalculationException("Attempt to get boolean array value for ["+getValueType()+"] operator ");}
	
	@Override public long[] getLongContent() throws CalculationException {throw new CalculationException("Attempt to get long array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double[] getDoubleContent() throws CalculationException {throw new CalculationException("Attempt to get double array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[][] getStringContent() throws CalculationException {throw new CalculationException("Attempt to get string array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean[] getBooleanContent() throws CalculationException {throw new CalculationException("Attempt to get boolean array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	
	@Override
	public String toString() {
		return "ArrayAccessNode [node=" + Arrays.toString(node) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(node);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ArrayAccessNode other = (ArrayAccessNode) obj;
		if (!Arrays.equals(node, other.node)) return false;
		return true;
	}
}

class ArithmeticNode extends BinaryOperatorNode {
	private final ExpressionNode[]	node;

	ArithmeticNode(final ExpressionNodeOperator op, final ExpressionNode left, final ExpressionNode right) {
		super(op);
		this.node = new ExpressionNode[]{left, right};
	}

	@Override ExpressionNode[] getOperands() {return node;}
	@Override public ExpressionNodeValue getValueType() {return node[0].getValueType();}

	@Override 
	public int getSize() throws CalculationException {
		throw new CalculationException("Attempt to get size of expression");
	}
	
	@Override
	public long getLong() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.INTEGER) {
			switch (getOperator()) {
				case ADD	: return node[0].getLong() + node[1].getLong();
				case SUB 	: return node[0].getLong() - node[1].getLong();
				case MUL 	: return node[0].getLong() * node[1].getLong();
				case DIV 	: return node[0].getLong() / node[1].getLong();
				case MOD 	: return node[0].getLong() % node[1].getLong();
				default : throw new UnsupportedOperationException("Unsupported operator ["+getOperator()+"]"); 
			}
		}
		else {
			throw new CalculationException("Attempt to get long value for ["+getValueType()+"] operator ");		
		}
	}

	@Override
	public double getDouble() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.REAL) {
			switch (getOperator()) {
				case ADD	: return node[0].getDouble() + node[1].getDouble();
				case SUB 	: return node[0].getDouble() - node[1].getDouble();
				case MUL 	: return node[0].getDouble() * node[1].getDouble();
				case DIV 	: return node[0].getDouble() / node[1].getDouble();
				case MOD 	: return node[0].getDouble() % node[1].getDouble();
				default : throw new UnsupportedOperationException("Operator ["+getOperator()+"] is not supported yet"); 
			}
		}
		else {
			throw new CalculationException("Attempt to get real value for ["+getValueType()+"] operator ");		
		}
	}

	@Override public char[] getString() throws CalculationException {throw new CalculationException("Attempt to get string value for ["+getValueType()+"] operator ");}
	@Override public boolean getBoolean() throws CalculationException {throw new CalculationException("Attempt to get boolean value for ["+getValueType()+"] operator ");}
	@Override public long getLong(final long index) throws CalculationException {throw new CalculationException("Attempt to get integer array value for ["+getValueType()+"] operator ");}
	@Override public double getDouble(final long index) throws CalculationException {throw new CalculationException("Attempt to get real array value for ["+getValueType()+"] operator ");}
	@Override public char[] getString(final long index) throws CalculationException {throw new CalculationException("Attempt to get string array value for ["+getValueType()+"] operator ");}
	@Override public boolean getBoolean(final long index) throws CalculationException {throw new CalculationException("Attempt to get boolean array value for ["+getValueType()+"] operator ");}

	@Override public long[] getLongContent() throws CalculationException {throw new CalculationException("Attempt to get long array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double[] getDoubleContent() throws CalculationException {throw new CalculationException("Attempt to get double array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[][] getStringContent() throws CalculationException {throw new CalculationException("Attempt to get string array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean[] getBooleanContent() throws CalculationException {throw new CalculationException("Attempt to get boolean array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	
	@Override
	public String toString() {
		return "ArithmeticNode [node=" + Arrays.toString(node) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(node);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ArithmeticNode other = (ArithmeticNode) obj;
		if (!Arrays.equals(node, other.node)) return false;
		return true;
	}
}


class ComparisonNode extends BinaryOperatorNode {
	private final ExpressionNode[]	node;

	ComparisonNode(final ExpressionNodeOperator op, final ExpressionNode left, final ExpressionNode right) {
		super(op);
		this.node = new ExpressionNode[]{left, right};
	}

	@Override 
	public int getSize() throws CalculationException {
		throw new CalculationException("Attempt to get size of expression");
	}
	
	@Override ExpressionNode[] getOperands() {return node;}
	@Override public ExpressionNodeValue getValueType() {return ExpressionNodeValue.BOOLEAN;}
	
	@Override public long getLong() throws CalculationException {throw new CalculationException("Attempt to get integer value for ["+getValueType()+"] operator ");}
	@Override public double getDouble() throws CalculationException {throw new CalculationException("Attempt to get real value for ["+getValueType()+"] operator ");}
	@Override public char[] getString() throws CalculationException {throw new CalculationException("Attempt to get string value for ["+getValueType()+"] operator ");}
	
	@Override 
	public boolean getBoolean() throws CalculationException {
		switch (getOperator()) {
			case EQ :
				switch (node[0].getValueType()) {
					case INTEGER	: return node[0].getLong() == node[1].getLong();
					case REAL		: return node[0].getDouble() == node[1].getDouble();
					case STRING		: return compareContent(node[0].getString(),node[1].getString()) == 0;
					case BOOLEAN	: return node[0].getBoolean() == node[1].getBoolean();
					default : throw new UnsupportedOperationException("Value type ["+getOperator()+"] is not supported yet"); 
				}
			case NE :
				switch (node[0].getValueType()) {
				case INTEGER	: return node[0].getLong() != node[1].getLong();
				case REAL		: return node[0].getDouble() != node[1].getDouble();
				case STRING		: return compareContent(node[0].getString(),node[1].getString()) != 0;
				case BOOLEAN	: return node[0].getBoolean() != node[1].getBoolean();
				default : throw new UnsupportedOperationException("Value type ["+getOperator()+"] is not supported yet"); 
			}
			case GT :
				switch (node[0].getValueType()) {
				case INTEGER	: return node[0].getLong() > node[1].getLong();
				case REAL		: return node[0].getDouble() > node[1].getDouble();
				case STRING		: return compareContent(node[0].getString(),node[1].getString()) > 0;
				default : throw new UnsupportedOperationException("Value type ["+getOperator()+"] is not supported yet"); 
			}
			case GE :
				switch (node[0].getValueType()) {
				case INTEGER	: return node[0].getLong() >= node[1].getLong();
				case REAL		: return node[0].getDouble() >= node[1].getDouble();
				case STRING		: return compareContent(node[0].getString(),node[1].getString()) >= 0;
				default : throw new UnsupportedOperationException("Value type ["+getOperator()+"] is not supported yet"); 
			}
			case LT :
				switch (node[0].getValueType()) {
				case INTEGER	: return node[0].getLong() < node[1].getLong();
				case REAL		: return node[0].getDouble() < node[1].getDouble();
				case STRING		: return compareContent(node[0].getString(),node[1].getString()) < 0;
				default : throw new UnsupportedOperationException("Value type ["+getOperator()+"] is not supported yet"); 
			}
			case LE :
				switch (node[0].getValueType()) {
				case INTEGER	: return node[0].getLong() <= node[1].getLong();
				case REAL		: return node[0].getDouble() <= node[1].getDouble();
				case STRING		: return compareContent(node[0].getString(),node[1].getString()) <= 0;
				default : throw new UnsupportedOperationException("Value type ["+getOperator()+"] is not supported yet"); 
			}
			default : throw new UnsupportedOperationException("Operator ["+getOperator()+"] is not supported yet"); 
		}
	}

	@Override public long getLong(final long index) throws CalculationException {throw new CalculationException("Attempt to get integer array value for ["+getValueType()+"] operator ");}
	@Override public double getDouble(final long index) throws CalculationException {throw new CalculationException("Attempt to get real array value for ["+getValueType()+"] operator ");}
	@Override public char[] getString(final long index) throws CalculationException {throw new CalculationException("Attempt to get string array value for ["+getValueType()+"] operator ");}
	@Override public boolean getBoolean(final long index) throws CalculationException {throw new CalculationException("Attempt to get boolean array value for ["+getValueType()+"] operator ");}

	@Override public long[] getLongContent() throws CalculationException {throw new CalculationException("Attempt to get long array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double[] getDoubleContent() throws CalculationException {throw new CalculationException("Attempt to get double array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[][] getStringContent() throws CalculationException {throw new CalculationException("Attempt to get string array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean[] getBooleanContent() throws CalculationException {throw new CalculationException("Attempt to get boolean array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	
	private static int compareContent(final char[] left, final char[] right) {
        final int 	len1 = left.length, len2 = right.length, lim = Math.min(len1, len2);
        int			result;

        for (int index = 0; index < lim; index++) {
        	if ((result = left[index] - right[index]) != 0) {
        		return result;
        	}
        }
        return len1 - len2;
	}

	@Override
	public String toString() {
		return "ComparisonNode [node=" + Arrays.toString(node) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(node);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ComparisonNode other = (ComparisonNode) obj;
		if (!Arrays.equals(node, other.node)) return false;
		return true;
	}
}


class TernaryOperatorNode extends OperatorNode {
	private final ExpressionNode[]	node;
	
	TernaryOperatorNode(final ExpressionNode cond, final ExpressionNode onTrue, final ExpressionNode onFalse) {
		super(ExpressionNodeOperator.TERNARY);
		this.node = new ExpressionNode[]{cond,onTrue,onFalse};
	}

	@Override 
	public int getSize() throws CalculationException {
		throw new CalculationException("Attempt to get size of expression");
	}
	
	@Override ExpressionNode[] getOperands() {return node;}
	@Override public ExpressionNodeValue getValueType() {return node[1].getValueType();}

	@Override
	public long getLong() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.INTEGER) {
			return node[0].getBoolean() ? node[1].getLong() : node[2].getLong();
		}
		else {
			throw new CalculationException("Attempt to get integer value for ["+getValueType()+"] type");
		}
	}

	@Override
	public double getDouble() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.INTEGER) {
			return node[0].getBoolean() ? node[1].getDouble() : node[2].getDouble();
		}
		else {
			throw new CalculationException("Attempt to get real value for ["+getValueType()+"] type");
		}
	}

	@Override
	public char[] getString() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.INTEGER) {
			return node[0].getBoolean() ? node[1].getString() : node[2].getString();
		}
		else {
			throw new CalculationException("Attempt to get string value for ["+getValueType()+"] type");
		}
	}

	@Override
	public boolean getBoolean() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.INTEGER) {
			return node[0].getBoolean() ? node[1].getBoolean() : node[2].getBoolean();
		}
		else {
			throw new CalculationException("Attempt to get bool value for ["+getValueType()+"] type");
		}
	}

	@Override public long getLong(final long index) throws CalculationException {throw new CalculationException("Attempt to get integer array value for ["+getValueType()+"] operator ");}
	@Override public double getDouble(final long index) throws CalculationException {throw new CalculationException("Attempt to get real array value for ["+getValueType()+"] operator ");}
	@Override public char[] getString(final long index) throws CalculationException {throw new CalculationException("Attempt to get string array value for ["+getValueType()+"] operator ");}
	@Override public boolean getBoolean(final long index) throws CalculationException {throw new CalculationException("Attempt to get boolean array value for ["+getValueType()+"] operator ");}

	@Override public long[] getLongContent() throws CalculationException {throw new CalculationException("Attempt to get long array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double[] getDoubleContent() throws CalculationException {throw new CalculationException("Attempt to get double array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[][] getStringContent() throws CalculationException {throw new CalculationException("Attempt to get string array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean[] getBooleanContent() throws CalculationException {throw new CalculationException("Attempt to get boolean array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	
	@Override
	public String toString() {
		return "TernaryOperatorNode [node=" + Arrays.toString(node) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(node);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TernaryOperatorNode other = (TernaryOperatorNode) obj;
		if (!Arrays.equals(node, other.node)) return false;
		return true;
	}
}

abstract class OperatorListNode extends OperatorNode {
	private final ExpressionNodeValue		valueType;
	protected final List<ExpressionNode>	operands = new ArrayList<>();

	OperatorListNode(final ExpressionNodeOperator op, final ExpressionNodeValue valueType) {
		super(op);
		this.valueType = valueType;
	}

	@Override public ExpressionNodeValue getValueType() {return valueType;}
	ExpressionNode[] getOperands(){return operands.toArray(new ExpressionNode[operands.size()]);}
	
	OperatorListNode addOperand(final ExpressionNode operand) {
		operands.add(operand);
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operands == null) ? 0 : operands.hashCode());
		result = prime * result + ((valueType == null) ? 0 : valueType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		OperatorListNode other = (OperatorListNode) obj;
		if (operands == null) {
			if (other.operands != null) return false;
		} else if (!operands.equals(other.operands)) return false;
		if (valueType != other.valueType) return false;
		return true;
	}
}

class OrNode extends OperatorListNode {
	OrNode() {
		super(ExpressionNodeOperator.OR,ExpressionNodeValue.BOOLEAN);
	}

	@Override public long getLong() throws CalculationException {throw new CalculationException("Attempt to get integer value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double getDouble() throws CalculationException {throw new CalculationException("Attempt to get real value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[] getString() throws CalculationException {throw new CalculationException("Attempt to get string value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}

	@Override 
	public int getSize() throws CalculationException {
		throw new CalculationException("Attempt to get size of expression");
	}
	
	@Override
	public boolean getBoolean() throws CalculationException {
		for (ExpressionNode item : operands) {
			if (item.getBoolean()) {
				return true;
			}
		}
		return false;
	}

	@Override public long getLong(final long index) throws CalculationException {throw new CalculationException("Attempt to get integer array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double getDouble(final long index) throws CalculationException {throw new CalculationException("Attempt to get real array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[] getString(final long index) throws CalculationException {throw new CalculationException("Attempt to get string array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean getBoolean(final long index) throws CalculationException {throw new CalculationException("Attempt to get boolean array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}

	@Override public long[] getLongContent() throws CalculationException {throw new CalculationException("Attempt to get long array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double[] getDoubleContent() throws CalculationException {throw new CalculationException("Attempt to get double array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[][] getStringContent() throws CalculationException {throw new CalculationException("Attempt to get string array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean[] getBooleanContent() throws CalculationException {throw new CalculationException("Attempt to get boolean array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	
	@Override
	public String toString() {
		return "OrNode [getValueType()=" + getValueType() + ", getOperands()=" + Arrays.toString(getOperands()) + ", getType()=" + getType() + ", getOperator()=" + getOperator() + "]";
	}
}

class AndNode extends OperatorListNode {
	AndNode() {
		super(ExpressionNodeOperator.AND,ExpressionNodeValue.BOOLEAN);
	}

	@Override public long getLong() throws CalculationException {throw new CalculationException("Attempt to get integer value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double getDouble() throws CalculationException {throw new CalculationException("Attempt to get real value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[] getString() throws CalculationException {throw new CalculationException("Attempt to get string value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}

	@Override 
	public int getSize() throws CalculationException {
		throw new CalculationException("Attempt to get size of expression");
	}
	
	@Override
	public boolean getBoolean() throws CalculationException {
		for (ExpressionNode item : operands) {
			if (!item.getBoolean()) {
				return false;
			}
		}
		return true;
	}

	@Override public long getLong(final long index) throws CalculationException {throw new CalculationException("Attempt to get integer array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double getDouble(final long index) throws CalculationException {throw new CalculationException("Attempt to get real array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[] getString(final long index) throws CalculationException {throw new CalculationException("Attempt to get string array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean getBoolean(final long index) throws CalculationException {throw new CalculationException("Attempt to get boolean array value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}

	@Override public long[] getLongContent() throws CalculationException {throw new CalculationException("Attempt to get long array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double[] getDoubleContent() throws CalculationException {throw new CalculationException("Attempt to get double array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[][] getStringContent() throws CalculationException {throw new CalculationException("Attempt to get string array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean[] getBooleanContent() throws CalculationException {throw new CalculationException("Attempt to get boolean array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	
	@Override
	public String toString() {
		return "AndNode [getValueType()=" + getValueType() + ", getOperands()=" + Arrays.toString(getOperands()) + ", getType()=" + getType() + ", getOperator()=" + getOperator() + "]";
	}
}

class CatNode extends OperatorListNode {
	CatNode() {
		super(ExpressionNodeOperator.CAT,ExpressionNodeValue.STRING);
	}

	@Override public long getLong() throws CalculationException {throw new CalculationException("Attempt to get integer value for ["+ExpressionNodeValue.STRING+"] operator ");}
	@Override public double getDouble() throws CalculationException {throw new CalculationException("Attempt to get real value for ["+ExpressionNodeValue.STRING+"] operator ");}
	@Override public boolean getBoolean() throws CalculationException {throw new CalculationException("Attempt to get boolean value for ["+ExpressionNodeValue.STRING+"] operator ");}

	@Override 
	public int getSize() throws CalculationException {
		throw new CalculationException("Attempt to get size of expression");
	}
	
	@Override
	public char[] getString() throws CalculationException {
		int		len = 0;
		
		for (ExpressionNode item : operands) {
			len += item.getString().length;
		}
		
		final char[]	result = new char[len];
		char[]			temp;
		
		len = 0;
		for (ExpressionNode item : operands) {
			temp = item.getString();
			System.arraycopy(temp,0,result,len,temp.length);
			len += temp.length;
		}
		return result;
	}

	@Override public long getLong(final long index) throws CalculationException {throw new CalculationException("Attempt to get integer array value for ["+ExpressionNodeValue.STRING+"] operator ");}
	@Override public double getDouble(final long index) throws CalculationException {throw new CalculationException("Attempt to get real array value for ["+ExpressionNodeValue.STRING+"] operator ");}
	@Override public char[] getString(final long index) throws CalculationException {throw new CalculationException("Attempt to get string array value for ["+ExpressionNodeValue.STRING+"] operator ");}
	@Override public boolean getBoolean(final long index) throws CalculationException {throw new CalculationException("Attempt to get boolean array value for ["+ExpressionNodeValue.STRING+"] operator ");}

	@Override public long[] getLongContent() throws CalculationException {throw new CalculationException("Attempt to get long array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double[] getDoubleContent() throws CalculationException {throw new CalculationException("Attempt to get double array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[][] getStringContent() throws CalculationException {throw new CalculationException("Attempt to get string array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean[] getBooleanContent() throws CalculationException {throw new CalculationException("Attempt to get boolean array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	
	@Override
	public String toString() {
		return "CatNode [getValueType()=" + getValueType() + ", getOperands()=" + Arrays.toString(getOperands()) + ", getType()=" + getType() + ", getOperator()=" + getOperator() + "]";
	}
}


class FuncNode extends OperatorListNode {
	@FunctionalInterface
	interface IntegerCallback {
		long calculate(ExpressionNode[] list) throws CalculationException;
	}
	
	@FunctionalInterface
	interface RealCallback {
		double calculate(ExpressionNode[] list) throws CalculationException;
	}
	
	@FunctionalInterface
	interface StringCallback {
		char[] calculate(ExpressionNode[] list) throws CalculationException;
	}
	
	@FunctionalInterface
	interface BoolCallback {
		boolean calculate(ExpressionNode[] list) throws CalculationException;
	}

	@FunctionalInterface
	interface IntegerArrayCallback {
		long[] calculate(ExpressionNode[] list) throws CalculationException;
	}
	
	@FunctionalInterface
	interface RealArrayCallback {
		double[] calculate(ExpressionNode[] list) throws CalculationException;
	}
	
	@FunctionalInterface
	interface StringArrayCallback {
		char[][] calculate(ExpressionNode[] list) throws CalculationException;
	}
	
	@FunctionalInterface
	interface BoolArrayCallback {
		boolean[] calculate(ExpressionNode[] list) throws CalculationException;
	}
	
	private final Object	callback;

	FuncNode(ExpressionNodeOperator op, ExpressionNodeValue valueType, IntegerCallback callback) {
		super(op, valueType);
		this.callback = callback;
	}
	
	FuncNode(ExpressionNodeOperator op, ExpressionNodeValue valueType, RealCallback callback) {
		super(op, valueType);
		this.callback = callback;
	}
	
	FuncNode(ExpressionNodeOperator op, ExpressionNodeValue valueType, StringCallback callback) {
		super(op, valueType);
		this.callback = callback;
	}

	FuncNode(ExpressionNodeOperator op, ExpressionNodeValue valueType, BoolCallback callback) {
		super(op, valueType);
		this.callback = callback;
	}

	@Override 
	public int getSize() throws CalculationException {
		throw new CalculationException("Attempt to get size of expression");
	}
	
	@Override
	public long getLong() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.INTEGER) {
			return ((IntegerCallback)callback).calculate(getOperands());
		}
		else {
			throw new CalculationException("Attempt to get integer value for ["+getValueType()+"] type");
		}
	}

	@Override
	public double getDouble() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.REAL) {
			return ((RealCallback)callback).calculate(getOperands());
		}
		else {
			throw new CalculationException("Attempt to get real value for ["+getValueType()+"] type");
		}
	}

	@Override
	public char[] getString() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.STRING) {
			return ((StringCallback)callback).calculate(getOperands());
		}
		else {
			throw new CalculationException("Attempt to get string value for ["+getValueType()+"] type");
		}
	}

	@Override
	public boolean getBoolean() throws CalculationException {
		if (getValueType() == ExpressionNodeValue.BOOLEAN) {
			return ((BoolCallback)callback).calculate(getOperands());
		}
		else {
			throw new CalculationException("Attempt to get bool value for ["+getValueType()+"] type");
		}
	}
	
	@Override
	public long getLong(final long index) throws CalculationException {
		if (getValueType() == ExpressionNodeValue.INTEGER_ARRAY) {
			return ((IntegerArrayCallback)callback).calculate(getOperands())[(int)index];
		}
		else {
			throw new CalculationException("Attempt to get integer value for ["+getValueType()+"] type");
		}
	}

	@Override
	public double getDouble(final long index) throws CalculationException {
		if (getValueType() == ExpressionNodeValue.REAL_ARRAY) {
			return ((RealArrayCallback)callback).calculate(getOperands())[(int)index];
		}
		else {
			throw new CalculationException("Attempt to get real value for ["+getValueType()+"] type");
		}
	}

	@Override
	public char[] getString(final long index) throws CalculationException {
		if (getValueType() == ExpressionNodeValue.STRING_ARRAY) {
			return ((StringArrayCallback)callback).calculate(getOperands())[(int)index];
		}
		else {
			throw new CalculationException("Attempt to get string value for ["+getValueType()+"] type");
		}
	}

	@Override
	public boolean getBoolean(final long index) throws CalculationException {
		if (getValueType() == ExpressionNodeValue.BOOLEAN_ARRAY) {
			return ((BoolArrayCallback)callback).calculate(getOperands())[(int)index];
		}
		else {
			throw new CalculationException("Attempt to get bool value for ["+getValueType()+"] type");
		}
	}
	
	@Override public long[] getLongContent() throws CalculationException {throw new CalculationException("Attempt to get long array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double[] getDoubleContent() throws CalculationException {throw new CalculationException("Attempt to get double array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[][] getStringContent() throws CalculationException {throw new CalculationException("Attempt to get string array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean[] getBooleanContent() throws CalculationException {throw new CalculationException("Attempt to get boolean array for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
}

class FuncToIntNode extends FuncNode {
	private static final IntegerCallback	callback = new IntegerCallback() {
												private final long[]	temp = new long[1];
												
												@Override
												public long calculate(ExpressionNode[] list) throws CalculationException {
													switch (list[0].getValueType()) {
														case INTEGER	: 
															return list[0].getLong();
														case REAL		: 
															return (long)list[0].getDouble(); 
														case STRING		: 
															try {
																UnsafedCharUtils.uncheckedParseLong(list[0].getString(),0,temp,true);
																return temp[0];
															} catch (SyntaxException e) {
																throw new CalculationException("Error converting string to long: "+e.getLocalizedMessage());
															}
														case BOOLEAN	: throw new CalculationException("Boolean value can't be converted to integer!");
														default : throw new UnsupportedOperationException("Value type to convert ["+list[0].getValueType()+"] is not supported yet");
													}
												}
											};

	FuncToIntNode() {
		super(ExpressionNodeOperator.F_TO_INT,ExpressionNodeValue.INTEGER, callback);
	}
}

class FuncToRealNode extends FuncNode {
	private static final RealCallback	callback = new RealCallback() {
												private final double[]	temp = new double[1];
												
												@Override
												public double calculate(final ExpressionNode[] list) throws CalculationException {
													switch (list[0].getValueType()) {
														case INTEGER	: 
															return list[0].getLong();
														case REAL		: 
															return list[0].getDouble(); 
														case STRING		: 
															try{
																UnsafedCharUtils.uncheckedParseDouble(list[0].getString(),0,temp,true);
																return temp[0];
															} catch (SyntaxException e) {
																throw new CalculationException("Error converting string to double: "+e.getLocalizedMessage());
															}
														case BOOLEAN	: 
															throw new CalculationException("Boolean value can't be converted to real!");
														default : 
															throw new UnsupportedOperationException("Value type to convert ["+list[0].getValueType()+"] is not supported yet");
													}
												}
											};

	FuncToRealNode() {
		super(ExpressionNodeOperator.F_TO_REAL,ExpressionNodeValue.REAL,callback);
	}
}

class FuncToStringNode extends FuncNode {
	private static final char[]			PURE_FALSE = "false".toCharArray();
	private static final char[]			PURE_TRUE = "true".toCharArray();
	private static final StringCallback	callback = new StringCallback() {
													@Override
													public char[] calculate(final ExpressionNode[] list) throws CalculationException {
														switch (list[0].getValueType()) {
															case INTEGER	: return String.valueOf(list[0].getLong()).toCharArray();
															case REAL		: return String.valueOf(list[0].getDouble()).toCharArray(); 
															case STRING		: return list[0].getString(); 
															case BOOLEAN	: return list[0].getBoolean() ? PURE_TRUE : PURE_FALSE;
															default : throw new UnsupportedOperationException("Value type to convert ["+list[0].getValueType()+"] is not supported yet");
														}
													}
												};

	FuncToStringNode() {
		super(ExpressionNodeOperator.F_TO_STR,ExpressionNodeValue.STRING,callback);
	}
}

class FuncToBooleanNode extends FuncNode {
	private static final char[]			PURE_FALSE = "false".toCharArray();
	private static final char[]			PURE_TRUE = "true".toCharArray();
	private static final BoolCallback	callback = new BoolCallback() {
													@Override
													public boolean calculate(final ExpressionNode[] list) throws CalculationException {
														switch (list[0].getValueType()) {
															case INTEGER	: 
																throw new CalculationException("Integer value can't be converted to boolean!");
															case REAL		: 
																throw new CalculationException("Real value can't be converted to boolean!");
															case STRING		: 
																if (UnsafedCharUtils.uncheckedCompare(list[0].getString(),0,PURE_TRUE,0,PURE_TRUE.length)) {
																	return true;
																}
																else if (UnsafedCharUtils.uncheckedCompare(list[0].getString(),0,PURE_FALSE,0,PURE_FALSE.length)) {
																	return false;
																}
																else {
																	throw new CalculationException("String content ["+new String(list[0].getString())+"] is not a valid boolean value!");
																}
															case BOOLEAN	: 
																return list[0].getBoolean();
															default : 
																throw new UnsupportedOperationException("Value type to convert ["+list[0].getValueType()+"] is not supported yet");
														}
													}
												};

	FuncToBooleanNode() {
		super(ExpressionNodeOperator.F_TO_STR,ExpressionNodeValue.BOOLEAN,callback);
	}
}

class FuncExistsNode extends FuncNode {
	private static final BoolCallback	callback = new BoolCallback() {
													@Override
													public boolean calculate(final ExpressionNode[] list) throws CalculationException {
														if (list.length > 0) {
															switch (list[0].getType()) {
																case KEY_PARAMETER : case POSITIONAL_PARAMETER : case LOCAL_VARIABLE : 
																	return ((AssignableExpressionNode)list[0]).hasValue(); 
																default : 
																	return true;
															}
														}
														else {
															return false;
														}
													}
												};

	FuncExistsNode() {
		super(ExpressionNodeOperator.F_EXISTS,ExpressionNodeValue.BOOLEAN,callback);
	}
}

class FuncLenNode extends FuncNode {
	private static final IntegerCallback	callback = new IntegerCallback() {
													@Override
													public long calculate(final ExpressionNode[] list) throws CalculationException {
														if (list.length > 0) {
															switch (list[0].getType()) {
																case KEY_PARAMETER : case POSITIONAL_PARAMETER : case LOCAL_VARIABLE : 
																	return ((AssignableExpressionNode)list[0]).getSize(); 
																default : 
																	return -1L;
															}
														}
														else {
															return -1L;
														}
													}
												};

	FuncLenNode() {
		super(ExpressionNodeOperator.F_LEN,ExpressionNodeValue.INTEGER,callback);
	}
}

class FuncToListNode extends FuncNode {
	private static final char[]			PURE_FALSE = "false".toCharArray();
	private static final char[]			PURE_TRUE = "true".toCharArray();
	private static final StringCallback	callback = new StringCallback() {
													final GrowableCharArray<GrowableCharArray<?>> gca = new GrowableCharArray<GrowableCharArray<?>>(false);
													@Override
													public char[] calculate(final ExpressionNode[] list) throws CalculationException {
														char	prefix = '{';
														
														gca.length(0);
														switch (list[0].getValueType()) {
															case INTEGER_ARRAY	: 
																break;
															case REAL_ARRAY		: 
																break;
															case STRING_ARRAY	: 
																break;
															case BOOLEAN_ARRAY	:
																break;
															default : throw new UnsupportedOperationException("Value type to convert ["+list[0].getValueType()+"] is not supported yet");
														}
														return gca.extract();
													}
												};

	FuncToListNode() {
		super(ExpressionNodeOperator.F_TO_LIST, ExpressionNodeValue.STRING, callback);
	}
}

class FuncEnvironmentNode extends FuncNode {
	private static final StringCallback	callback = new StringCallback() {
													@Override
													public char[] calculate(final ExpressionNode[] list) throws CalculationException {
														if (list.length > 0) {
															switch (list[0].getValueType()) {
																case STRING : 
																	return  MacroExecutor.environment(((AssignableExpressionNode)list[0]).getString()); 
																default : 
																	throw new CalculationException("Parameter to 'environment(...)' function must be string!");
															}
														}
														else {
															throw new CalculationException("Function 'environment(...)' must contain exactly one parameter in the parameter list!");
														}
													}
												};

	FuncEnvironmentNode() {
		super(ExpressionNodeOperator.F_ENVIRONMENT,ExpressionNodeValue.STRING,callback);
	}
}
