package chav1961.purelib.streams.char2byte.asm.macro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
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
	
	ConstantNode(final long value) {
		this.valueType = ExpressionNodeValue.INTEGER;
		this.longValue = value;
		this.doubleValue = 0;
		this.stringValue = null;
		this.booleanValue = false;
	}

	ConstantNode(final double value) {
		this.valueType = ExpressionNodeValue.REAL; 
		this.doubleValue = value;
		this.longValue = 0;
		this.stringValue = null;
		this.booleanValue = false;
	}

	ConstantNode(final char[] value) {
		this.valueType = ExpressionNodeValue.STRING; 
		this.stringValue = value;
		this.longValue = 0;
		this.doubleValue = 0;
		this.booleanValue = false;
	}
	
	ConstantNode(final boolean value) {
		this.valueType = ExpressionNodeValue.BOOLEAN; 
		this.booleanValue = value;
		this.longValue = 0;
		this.doubleValue = 0;
		this.stringValue = null;
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
	public String toString() {
		switch (valueType) {
			case INTEGER	: return "ConstantNode [valueType=" + valueType + ", " + longValue + "]";
			case REAL		: return "ConstantNode [valueType=" + valueType + ", " + doubleValue+ "]";
			case STRING		: return "ConstantNode [valueType=" + valueType + ", " + (stringValue == null ? "null" : '\"' + new String(stringValue) + '\"') + "]";
			case BOOLEAN	: return "ConstantNode [valueType=" + valueType + ", " + booleanValue + "]";
			default			: return super.toString();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (booleanValue ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(doubleValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (longValue ^ (longValue >>> 32));
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
		if (booleanValue != other.booleanValue) return false;
		if (Double.doubleToLongBits(doubleValue) != Double.doubleToLongBits(other.doubleValue)) return false;
		if (longValue != other.longValue) return false;
		if (!Arrays.equals(stringValue, other.stringValue)) return false;
		if (valueType != other.valueType) return false;
		return true;
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
	
	abstract char[] getName();
	public abstract void assign(final ExpressionNode node) throws CalculationException;
	public abstract void assign(final long value) throws CalculationException;
	public abstract void assign(final double value) throws CalculationException;
	public abstract void assign(final char[] value) throws CalculationException;
	public abstract void assign(final boolean value) throws CalculationException;
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
	char[] getName() {
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
	
	@Override public ExpressionNodeType getType() {return ExpressionNodeType.POSITIONAL_PARAMETER;}
	@Override public ExpressionNodeValue getValueType() {return valueType;}
	@Override public long getLong() throws CalculationException {return currentValue.getLong();}
	@Override public double getDouble() throws CalculationException {return currentValue.getDouble();}
	@Override public char[] getString() throws CalculationException {return currentValue.getString();}
	@Override public boolean getBoolean() throws CalculationException {return currentValue.getBoolean();}
	@Override public boolean hasValue() {return currentValue != null;}

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
	char[] getName() {
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
	
	@Override public ExpressionNodeType getType() {return ExpressionNodeType.KEY_PARAMETER;}
	@Override public ExpressionNodeValue getValueType() {return valueType;}
	@Override public long getLong() throws CalculationException {return currentValue.getLong();}
	@Override public double getDouble() throws CalculationException {return currentValue.getDouble();}
	@Override public char[] getString() throws CalculationException {return currentValue.getString();}
	@Override public boolean getBoolean() throws CalculationException {return currentValue.getBoolean();}
	@Override public boolean hasValue() {return currentValue != null;}

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
}

class LocalVariable extends AssignableExpressionNode {
	private final char[]			name;
	private final ExpressionNodeValue	valueType;
	private final ExpressionNode	initialValue;
	private ExpressionNode			currentValue;
	
	LocalVariable(final char[] name, final ExpressionNodeValue valueType) {
		this(name,valueType,null);
	}

	LocalVariable(final char[] name, final ExpressionNodeValue valueType, final ExpressionNode initialValue) {
		this.name = name;
		this.valueType = valueType;
		this.initialValue = this.currentValue = initialValue;
	}
	

	@Override
	char[] getName() {
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
	
	@Override public ExpressionNodeType getType() {return ExpressionNodeType.LOCAL_VARIABLE;}
	@Override public ExpressionNodeValue getValueType() {return valueType;}
	@Override public long getLong() throws CalculationException {return currentValue.getLong();}
	@Override public double getDouble() throws CalculationException {return currentValue.getDouble();}
	@Override public char[] getString() throws CalculationException {return currentValue.getString();}
	@Override public boolean getBoolean() throws CalculationException {return currentValue.getBoolean();}
	@Override public boolean hasValue() {return currentValue != null;}

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

	@Override ExpressionNode[] getOperands() {return nested;}
	@Override public ExpressionNodeValue getValueType() {return ExpressionNodeValue.BOOLEAN;}
	@Override public long getLong() throws CalculationException {throw new CalculationException("Attempt to get integer value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public double getDouble() throws CalculationException {throw new CalculationException("Attempt to get real value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public char[] getString() throws CalculationException {throw new CalculationException("Attempt to get string value for ["+ExpressionNodeValue.BOOLEAN+"] operator ");}
	@Override public boolean getBoolean() throws CalculationException {return !nested[0].getBoolean();}

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

class ArithmeticNode extends BinaryOperatorNode {
	private final ExpressionNode[]	node;

	ArithmeticNode(final ExpressionNodeOperator op, final ExpressionNode left, final ExpressionNode right) {
		super(op);
		this.node = new ExpressionNode[]{left, right};
	}

	@Override ExpressionNode[] getOperands() {return node;}
	@Override public ExpressionNodeValue getValueType() {return node[0].getValueType();}
	
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
	public boolean getBoolean() throws CalculationException {
		for (ExpressionNode item : operands) {
			if (item.getBoolean()) {
				return true;
			}
		}
		return false;
	}

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
	public boolean getBoolean() throws CalculationException {
		for (ExpressionNode item : operands) {
			if (!item.getBoolean()) {
				return false;
			}
		}
		return true;
	}

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
}

class FuncToIntNode extends FuncNode {
	private static final IntegerCallback	callback = new IntegerCallback() {
												private final long[]	temp = new long[1];
												
												@Override
												public long calculate(ExpressionNode[] list) throws CalculationException {
													switch (list[0].getValueType()) {
														case INTEGER	: return list[0].getLong();
														case REAL		: return (long)list[0].getDouble(); 
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
														case INTEGER	: return list[0].getLong();
														case REAL		: return list[0].getDouble(); 
														case STRING		: 
															try{
																UnsafedCharUtils.uncheckedParseDouble(list[0].getString(),0,temp,true);
																return temp[0];
															} catch (SyntaxException e) {
																throw new CalculationException("Error converting string to double: "+e.getLocalizedMessage());
															}
														case BOOLEAN	: throw new CalculationException("Boolean value can't be converted to real!");
														default : throw new UnsupportedOperationException("Value type to convert ["+list[0].getValueType()+"] is not supported yet");
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
															case INTEGER	: throw new CalculationException("Integer value can't be converted to boolean!");
															case REAL		: throw new CalculationException("Real value can't be converted to boolean!");
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
															case BOOLEAN	: return list[0].getBoolean();
															default : throw new UnsupportedOperationException("Value type to convert ["+list[0].getValueType()+"] is not supported yet");
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
																case KEY_PARAMETER : case POSITIONAL_PARAMETER : case LOCAL_VARIABLE : return ((AssignableExpressionNode)list[0]).hasValue(); 
																default : return true;
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

