package chav1961.purelib.streams.char2byte.asm.macro;


import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.streams.char2byte.asm.ExpressionNodeType;

@Tag("OrdinalTestCategory")
public class ExpressionNodeTest {

	@Test
	public void constantTest() throws CalculationException {
		final ConstantNode	intConst = new ConstantNode(123);

		Assert.assertEquals(intConst.getType(),ExpressionNodeType.CONSTANT);
		Assert.assertEquals(intConst.getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(intConst.getLong(),123L);
		Assert.assertEquals(intConst,new ConstantNode(123));
		Assert.assertEquals(intConst.toString(),new ConstantNode(123).toString());
		Assert.assertEquals(intConst.hashCode(),new ConstantNode(123).hashCode());
		
		try{intConst.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intConst.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intConst.getBoolean();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intConst.getLong(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intConst.getDouble(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intConst.getString(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intConst.getBoolean(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}

		final ConstantNode	realConst = new ConstantNode(123.0);
	
		Assert.assertEquals(realConst.getType(),ExpressionNodeType.CONSTANT);
		Assert.assertEquals(realConst.getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(realConst.getDouble(),123.0,0.0001);
		Assert.assertEquals(realConst,new ConstantNode(123.0));
		Assert.assertEquals(realConst.toString(),new ConstantNode(123.0).toString());
		Assert.assertEquals(realConst.hashCode(),new ConstantNode(123.0).hashCode());
		
		try{realConst.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realConst.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realConst.getBoolean();
				Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realConst.getLong(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realConst.getDouble(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realConst.getString(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realConst.getBoolean(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}

		final ConstantNode	stringConst = new ConstantNode("123".toCharArray());
		
		Assert.assertEquals(stringConst.getType(),ExpressionNodeType.CONSTANT);
		Assert.assertEquals(stringConst.getValueType(),ExpressionNodeValue.STRING);
		Assert.assertArrayEquals(stringConst.getString(),"123".toCharArray());
		Assert.assertEquals(stringConst,new ConstantNode("123".toCharArray()));
		Assert.assertEquals(stringConst.toString(),new ConstantNode("123".toCharArray()).toString());
		Assert.assertEquals(stringConst.hashCode(),new ConstantNode("123".toCharArray()).hashCode());
		
		try{stringConst.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringConst.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringConst.getBoolean();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringConst.getLong(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringConst.getDouble(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringConst.getString(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringConst.getBoolean(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}

		final ConstantNode	boolConst = new ConstantNode(true);
		
		Assert.assertEquals(boolConst.getType(),ExpressionNodeType.CONSTANT);
		Assert.assertEquals(boolConst.getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(boolConst.getBoolean(),true);
		Assert.assertEquals(boolConst,new ConstantNode(true));
		Assert.assertEquals(boolConst.toString(),new ConstantNode(true).toString());
		Assert.assertEquals(boolConst.hashCode(),new ConstantNode(true).hashCode());
		
		try{boolConst.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{boolConst.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{boolConst.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{boolConst.getLong(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{boolConst.getDouble(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{boolConst.getString(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{boolConst.getBoolean(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}

		// ------- ARRAYS: --------------------------------------
		final ConstantNode	intArrayConst = new ConstantNode(123,456);

		Assert.assertEquals(intArrayConst.getType(),ExpressionNodeType.CONSTANT);
		Assert.assertEquals(intArrayConst.getValueType(),ExpressionNodeValue.INTEGER_ARRAY);
		Assert.assertEquals(intArrayConst.getLong(0),123L);
		Assert.assertEquals(intArrayConst,new ConstantNode(123,456));
		Assert.assertEquals(intArrayConst.toString(),new ConstantNode(123,456).toString());
		Assert.assertEquals(intArrayConst.hashCode(),new ConstantNode(123,456).hashCode());
		
		try{intArrayConst.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intArrayConst.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intArrayConst.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intArrayConst.getBoolean();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intArrayConst.getDouble(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intArrayConst.getString(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{intArrayConst.getBoolean(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}

		final ConstantNode	realArrayConst = new ConstantNode(123.0,456.0);

		Assert.assertEquals(realArrayConst.getType(),ExpressionNodeType.CONSTANT);
		Assert.assertEquals(realArrayConst.getValueType(),ExpressionNodeValue.REAL_ARRAY);
		Assert.assertEquals(realArrayConst.getDouble(0),123.0,0.001);
		Assert.assertEquals(realArrayConst,new ConstantNode(123.0,456.0));
		Assert.assertEquals(realArrayConst.toString(),new ConstantNode(123.0,456.0).toString());
		Assert.assertEquals(realArrayConst.hashCode(),new ConstantNode(123.0,456.0).hashCode());
		
		try{realArrayConst.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realArrayConst.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realArrayConst.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realArrayConst.getBoolean();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realArrayConst.getLong(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realArrayConst.getString(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{realArrayConst.getBoolean(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		
		final ConstantNode	stringArrayConst = new ConstantNode("123".toCharArray(),"456".toCharArray());

		Assert.assertEquals(stringArrayConst.getType(),ExpressionNodeType.CONSTANT);
		Assert.assertEquals(stringArrayConst.getValueType(),ExpressionNodeValue.STRING_ARRAY);
		Assert.assertArrayEquals(stringArrayConst.getString(0),"123".toCharArray());
		Assert.assertEquals(stringArrayConst,new ConstantNode("123".toCharArray(),"456".toCharArray()));
		Assert.assertEquals(stringArrayConst.toString(),new ConstantNode("123".toCharArray(),"456".toCharArray()).toString());
		Assert.assertEquals(stringArrayConst.hashCode(),new ConstantNode("123".toCharArray(),"456".toCharArray()).hashCode());
		
		try{stringArrayConst.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringArrayConst.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringArrayConst.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringArrayConst.getBoolean();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringArrayConst.getLong(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringArrayConst.getDouble(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{stringArrayConst.getBoolean(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}

		final ConstantNode	booleanArrayConst = new ConstantNode(true,false);

		Assert.assertEquals(booleanArrayConst.getType(),ExpressionNodeType.CONSTANT);
		Assert.assertEquals(booleanArrayConst.getValueType(),ExpressionNodeValue.BOOLEAN_ARRAY);
		Assert.assertEquals(booleanArrayConst.getBoolean(0),true);
		Assert.assertEquals(booleanArrayConst,new ConstantNode(true,false));
		Assert.assertEquals(booleanArrayConst.toString(),new ConstantNode(true,false).toString());
		Assert.assertEquals(booleanArrayConst.hashCode(),new ConstantNode(true,false).hashCode());
		
		try{booleanArrayConst.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{booleanArrayConst.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{booleanArrayConst.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{booleanArrayConst.getBoolean();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{booleanArrayConst.getLong(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{booleanArrayConst.getDouble(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{booleanArrayConst.getString(0);
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
	}
	
	@Test
	public void unaryNodeTest() throws CalculationException {
		final NotNode	not = new NotNode(new ConstantNode(false));
		
		Assert.assertEquals(not.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(not.getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(not.getOperator(),ExpressionNodeOperator.NOT);
		Assert.assertEquals(not.getOperands()[0],new ConstantNode(false));
		Assert.assertEquals(not.getBoolean(),true);
		Assert.assertEquals(not,new NotNode(new ConstantNode(false)));
		Assert.assertEquals(not.toString(),new NotNode(new ConstantNode(false)).toString());
		Assert.assertEquals(not.hashCode(),new NotNode(new ConstantNode(false)).hashCode());

		try{not.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{not.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{not.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		
		final NegNode	negInt = new NegNode(new ConstantNode(123));
		
		Assert.assertEquals(negInt.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(negInt.getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(negInt.getOperator(),ExpressionNodeOperator.NEG);
		Assert.assertEquals(negInt.getOperands()[0],new ConstantNode(123));
		Assert.assertEquals(negInt.getLong(),-123);
		Assert.assertEquals(negInt,new NegNode(new ConstantNode(123)));
		Assert.assertEquals(negInt.toString(),new NegNode(new ConstantNode(123)).toString());
		Assert.assertEquals(negInt.hashCode(),new NegNode(new ConstantNode(123)).hashCode());

		try{negInt.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{negInt.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{negInt.getBoolean();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		
		final NegNode	negReal = new NegNode(new ConstantNode(123.456));
		
		Assert.assertEquals(negReal.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(negReal.getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(negReal.getOperator(),ExpressionNodeOperator.NEG);
		Assert.assertEquals(negReal.getOperands()[0],new ConstantNode(123.456));
		Assert.assertEquals(negReal.getDouble(),-123.456,0.0001);
		Assert.assertEquals(negReal,new NegNode(new ConstantNode(123.456)));
		Assert.assertEquals(negReal.toString(),new NegNode(new ConstantNode(123.456)).toString());
		Assert.assertEquals(negReal.hashCode(),new NegNode(new ConstantNode(123.456)).hashCode());

		try{negReal.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{negReal.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{negReal.getBoolean();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
	}

	@Test
	public void binaryNodeTest() throws CalculationException {
		final ArithmeticNode		arithmInt = new ArithmeticNode(ExpressionNodeOperator.ADD,new ConstantNode(100),new ConstantNode(200));
		final ArithmeticNode		anotherArithmInt = new ArithmeticNode(ExpressionNodeOperator.ADD,new ConstantNode(100),new ConstantNode(200));
		
		Assert.assertEquals(arithmInt.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(arithmInt.getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(arithmInt.getOperator(),ExpressionNodeOperator.ADD);
		Assert.assertArrayEquals(arithmInt.getOperands(),new ExpressionNode[] {new ConstantNode(100),new ConstantNode(200)});
		Assert.assertEquals(arithmInt.getLong(),300);
		Assert.assertEquals(arithmInt,anotherArithmInt);
		Assert.assertEquals(arithmInt.toString(),anotherArithmInt.toString());
		Assert.assertEquals(arithmInt.hashCode(),anotherArithmInt.hashCode());

		try{arithmInt.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{arithmInt.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{arithmInt.getBoolean();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}

		Assert.assertEquals(new ArithmeticNode(ExpressionNodeOperator.SUB,new ConstantNode(100),new ConstantNode(200)).getLong(),-100);
		Assert.assertEquals(new ArithmeticNode(ExpressionNodeOperator.MUL,new ConstantNode(100),new ConstantNode(200)).getLong(),20000);
		Assert.assertEquals(new ArithmeticNode(ExpressionNodeOperator.DIV,new ConstantNode(100),new ConstantNode(200)).getLong(),0);
		Assert.assertEquals(new ArithmeticNode(ExpressionNodeOperator.MOD,new ConstantNode(100),new ConstantNode(200)).getLong(),100);
		
		final ArithmeticNode		arithmReal = new ArithmeticNode(ExpressionNodeOperator.ADD,new ConstantNode(100.5),new ConstantNode(200.5));
		
		Assert.assertEquals(arithmReal.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(arithmReal.getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(arithmReal.getOperator(),ExpressionNodeOperator.ADD);
		Assert.assertArrayEquals(arithmReal.getOperands(),new ExpressionNode[] {new ConstantNode(100.5),new ConstantNode(200.5)});
		Assert.assertEquals(arithmReal.getDouble(),301.0,0.0001);

		try{arithmReal.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{arithmReal.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{arithmReal.getBoolean();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		
		Assert.assertEquals(new ArithmeticNode(ExpressionNodeOperator.SUB,new ConstantNode(100.5),new ConstantNode(200.5)).getDouble(),-100,0.0001);
		Assert.assertEquals(new ArithmeticNode(ExpressionNodeOperator.MUL,new ConstantNode(100.0),new ConstantNode(200.0)).getDouble(),20000,0.0001);
		Assert.assertEquals(new ArithmeticNode(ExpressionNodeOperator.DIV,new ConstantNode(100.0),new ConstantNode(200.0)).getDouble(),0.5,0.0001);
		Assert.assertEquals(new ArithmeticNode(ExpressionNodeOperator.MOD,new ConstantNode(100.0),new ConstantNode(200.0)).getDouble(),100.0,0.0001);

		final ComparisonNode		compInt = new ComparisonNode(ExpressionNodeOperator.EQ,new ConstantNode(100),new ConstantNode(100));
		final ComparisonNode		anotherCompInt = new ComparisonNode(ExpressionNodeOperator.EQ,new ConstantNode(100),new ConstantNode(100));
		
		Assert.assertEquals(compInt.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(compInt.getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(compInt.getOperator(),ExpressionNodeOperator.EQ);
		Assert.assertArrayEquals(compInt.getOperands(),new ExpressionNode[] {new ConstantNode(100),new ConstantNode(100)});
		Assert.assertEquals(compInt.getBoolean(),true);
		Assert.assertEquals(compInt,anotherCompInt);
		Assert.assertEquals(compInt.toString(),anotherCompInt.toString());
		Assert.assertEquals(compInt.hashCode(),anotherCompInt.hashCode());
	
		try{compInt.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{compInt.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{compInt.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.NE,new ConstantNode(100),new ConstantNode(200)).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.LT,new ConstantNode(100),new ConstantNode(200)).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.LE,new ConstantNode(100),new ConstantNode(100)).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.GT,new ConstantNode(200),new ConstantNode(100)).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.GE,new ConstantNode(200),new ConstantNode(200)).getBoolean(),true);

		final ComparisonNode		compReal = new ComparisonNode(ExpressionNodeOperator.EQ,new ConstantNode(100.0),new ConstantNode(100.0));
		
		Assert.assertEquals(compReal.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(compReal.getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(compReal.getOperator(),ExpressionNodeOperator.EQ);
		Assert.assertArrayEquals(compReal.getOperands(),new ExpressionNode[] {new ConstantNode(100.0),new ConstantNode(100.0)});
		Assert.assertEquals(compReal.getBoolean(),true);
	
		try{compReal.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{compReal.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{compReal.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.NE,new ConstantNode(100.0),new ConstantNode(200.0)).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.LT,new ConstantNode(100.0),new ConstantNode(200.0)).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.LE,new ConstantNode(100.0),new ConstantNode(100.0)).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.GT,new ConstantNode(200.0),new ConstantNode(100.0)).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.GE,new ConstantNode(200.0),new ConstantNode(200.0)).getBoolean(),true);

		final ComparisonNode		compStr = new ComparisonNode(ExpressionNodeOperator.EQ,new ConstantNode("100".toCharArray()),new ConstantNode("100".toCharArray()));
		
		Assert.assertEquals(compStr.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(compStr.getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(compStr.getOperator(),ExpressionNodeOperator.EQ);
		Assert.assertArrayEquals(compStr.getOperands(),new ExpressionNode[] {new ConstantNode("100".toCharArray()),new ConstantNode("100".toCharArray())});
		Assert.assertEquals(compStr.getBoolean(),true);
	
		try{compStr.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{compStr.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{compStr.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.NE,new ConstantNode("100".toCharArray()),new ConstantNode("200".toCharArray())).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.LT,new ConstantNode("100".toCharArray()),new ConstantNode("200".toCharArray())).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.LE,new ConstantNode("100".toCharArray()),new ConstantNode("100".toCharArray())).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.GT,new ConstantNode("200".toCharArray()),new ConstantNode("100".toCharArray())).getBoolean(),true);
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.GE,new ConstantNode("200".toCharArray()),new ConstantNode("200".toCharArray())).getBoolean(),true);

		final ComparisonNode		compBool = new ComparisonNode(ExpressionNodeOperator.EQ,new ConstantNode(true),new ConstantNode(true));
		
		Assert.assertEquals(compBool.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(compBool.getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(compBool.getOperator(),ExpressionNodeOperator.EQ);
		Assert.assertArrayEquals(compBool.getOperands(),new ExpressionNode[] {new ConstantNode(true),new ConstantNode(true)});
		Assert.assertEquals(compBool.getBoolean(),true);
	
		try{compBool.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{compBool.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{compBool.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		
		Assert.assertEquals(new ComparisonNode(ExpressionNodeOperator.NE,new ConstantNode(true),new ConstantNode(false)).getBoolean(),true);
	}

	@Test
	public void ternaryNodeTest() throws CalculationException {
		final TernaryOperatorNode	tonTrue = new TernaryOperatorNode(new ConstantNode(true),new ConstantNode(123),new ConstantNode(456));
		final TernaryOperatorNode	anotherTonTrue = new TernaryOperatorNode(new ConstantNode(true),new ConstantNode(123),new ConstantNode(456));
		
		Assert.assertEquals(tonTrue.getType(),ExpressionNodeType.EXPRESSION);		
		Assert.assertEquals(tonTrue.getValueType(),ExpressionNodeValue.INTEGER);		
		Assert.assertEquals(tonTrue.getOperator(),ExpressionNodeOperator.TERNARY);		
		Assert.assertArrayEquals(tonTrue.getOperands(),new ExpressionNode[]{new ConstantNode(true),new ConstantNode(123),new ConstantNode(456)});
		Assert.assertEquals(tonTrue.getLong(),123);
		Assert.assertEquals(tonTrue,anotherTonTrue);
		Assert.assertEquals(tonTrue.toString(),anotherTonTrue.toString());
		Assert.assertEquals(tonTrue.hashCode(),anotherTonTrue.hashCode());
		
		final TernaryOperatorNode	tonFalse = new TernaryOperatorNode(new ConstantNode(false),new ConstantNode(123),new ConstantNode(456));
		
		Assert.assertEquals(tonFalse.getType(),ExpressionNodeType.EXPRESSION);		
		Assert.assertEquals(tonFalse.getValueType(),ExpressionNodeValue.INTEGER);		
		Assert.assertEquals(tonFalse.getOperator(),ExpressionNodeOperator.TERNARY);		
		Assert.assertArrayEquals(tonFalse.getOperands(),new ExpressionNode[]{new ConstantNode(false),new ConstantNode(123),new ConstantNode(456)});
		Assert.assertEquals(tonFalse.getLong(),456);
	}
	
	@Test
	public void listNodeTest() throws CalculationException {
		final OperatorListNode	cat = new CatNode().addOperand(new ConstantNode("123".toCharArray())).addOperand(new ConstantNode("456".toCharArray())).addOperand(new ConstantNode("789".toCharArray()));
		final OperatorListNode	anotherCat = new CatNode().addOperand(new ConstantNode("123".toCharArray())).addOperand(new ConstantNode("456".toCharArray())).addOperand(new ConstantNode("789".toCharArray()));

		Assert.assertEquals(cat.getType(),ExpressionNodeType.EXPRESSION);		
		Assert.assertEquals(cat.getValueType(),ExpressionNodeValue.STRING);		
		Assert.assertEquals(cat.getOperator(),ExpressionNodeOperator.CAT);		
		Assert.assertArrayEquals(cat.getOperands(),new ExpressionNode[]{new ConstantNode("123".toCharArray()),new ConstantNode("456".toCharArray()),new ConstantNode("789".toCharArray())});
		Assert.assertArrayEquals(cat.getString(),"123456789".toCharArray());
		Assert.assertEquals(cat,anotherCat);
		Assert.assertEquals(cat.toString(),anotherCat.toString());
		Assert.assertEquals(cat.hashCode(),anotherCat.hashCode());
				
		try{cat.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{cat.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{cat.getBoolean();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		
		final OperatorListNode	and = new AndNode().addOperand(new ConstantNode(true)).addOperand(new ConstantNode(true)).addOperand(new ConstantNode(true));

		Assert.assertEquals(and.getType(),ExpressionNodeType.EXPRESSION);		
		Assert.assertEquals(and.getValueType(),ExpressionNodeValue.BOOLEAN);		
		Assert.assertEquals(and.getOperator(),ExpressionNodeOperator.AND);		
		Assert.assertArrayEquals(and.getOperands(),new ExpressionNode[]{new ConstantNode(true),new ConstantNode(true),new ConstantNode(true)});
		Assert.assertEquals(and.getBoolean(),true);
				
		try{and.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{and.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{and.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}

		final OperatorListNode	or = new OrNode().addOperand(new ConstantNode(false)).addOperand(new ConstantNode(false)).addOperand(new ConstantNode(false));

		Assert.assertEquals(or.getType(),ExpressionNodeType.EXPRESSION);		
		Assert.assertEquals(or.getValueType(),ExpressionNodeValue.BOOLEAN);		
		Assert.assertEquals(or.getOperator(),ExpressionNodeOperator.OR);		
		Assert.assertArrayEquals(or.getOperands(),new ExpressionNode[]{new ConstantNode(false),new ConstantNode(false),new ConstantNode(false)});
		Assert.assertEquals(or.getBoolean(),false);
				
		try{or.getLong();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{or.getDouble();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
		try{or.getString();
			Assert.fail("Mandatory exception was not detected (type incompatibility)");
		} catch (CalculationException exc) {			
		}
	}
	
	@Test
	public void funcNodeTest() throws CalculationException {
		final FuncExistsNode	exists1 = new FuncExistsNode();
		final FuncExistsNode	anotherExists1 = new FuncExistsNode();
		
		Assert.assertEquals(exists1.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(exists1.getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(exists1.getBoolean(),false);
		Assert.assertEquals(exists1,anotherExists1);
		Assert.assertEquals(exists1.toString(),anotherExists1.toString());
		Assert.assertEquals(exists1.hashCode(),anotherExists1.hashCode());
		
		final FuncExistsNode	exists2 = new FuncExistsNode();
		
		exists2.addOperand(new ConstantNode(123));
		Assert.assertEquals(exists2.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(exists2.getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(exists2.getBoolean(),true);
		
		final FuncToIntNode		toInt1 = new FuncToIntNode();
		
		toInt1.addOperand(new ConstantNode(123));
		Assert.assertEquals(toInt1.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toInt1.getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(toInt1.getLong(),123);

		final FuncToIntNode		toInt2 = new FuncToIntNode();
		
		toInt2.addOperand(new ConstantNode(123.456));
		Assert.assertEquals(toInt2.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toInt2.getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(toInt2.getLong(),123);

		final FuncToIntNode		toInt3 = new FuncToIntNode();
		
		toInt3.addOperand(new ConstantNode("123".toCharArray()));
		Assert.assertEquals(toInt3.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toInt3.getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(toInt3.getLong(),123);
		
		final FuncToRealNode	toReal1 = new FuncToRealNode();
		
		toReal1.addOperand(new ConstantNode(123));
		Assert.assertEquals(toReal1.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toReal1.getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(toReal1.getDouble(),123,0.0001);

		final FuncToRealNode	toReal2 = new FuncToRealNode();
		
		toReal2.addOperand(new ConstantNode(123.456));
		Assert.assertEquals(toReal2.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toReal2.getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(toReal2.getDouble(),123.456,0.0001);

		final FuncToRealNode	toReal3 = new FuncToRealNode();
		
		toReal3.addOperand(new ConstantNode("123".toCharArray()));
		Assert.assertEquals(toReal3.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toReal3.getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(toReal3.getDouble(),123,0.0001);
		
		final FuncToStringNode	toStr1 = new FuncToStringNode();
		
		toStr1.addOperand(new ConstantNode(123));
		Assert.assertEquals(toStr1.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toStr1.getValueType(),ExpressionNodeValue.STRING);
		Assert.assertArrayEquals(toStr1.getString(),"123".toCharArray());
		
		final FuncToStringNode	toStr2 = new FuncToStringNode();
		
		toStr2.addOperand(new ConstantNode(123.0));
		Assert.assertEquals(toStr2.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toStr2.getValueType(),ExpressionNodeValue.STRING);
		Assert.assertArrayEquals(toStr2.getString(),"123.0".toCharArray());
		
		final FuncToStringNode	toStr3 = new FuncToStringNode();
		
		toStr3.addOperand(new ConstantNode("123".toCharArray()));
		Assert.assertEquals(toStr3.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toStr3.getValueType(),ExpressionNodeValue.STRING);
		Assert.assertArrayEquals(toStr3.getString(),"123".toCharArray());
		
		final FuncToStringNode	toStr4 = new FuncToStringNode();
		
		toStr4.addOperand(new ConstantNode(true));
		Assert.assertEquals(toStr4.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toStr4.getValueType(),ExpressionNodeValue.STRING);
		Assert.assertArrayEquals(toStr4.getString(),"true".toCharArray());

		final FuncToStringNode	toStr5 = new FuncToStringNode();
		
		toStr5.addOperand(new ConstantNode(false));
		Assert.assertEquals(toStr5.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toStr5.getValueType(),ExpressionNodeValue.STRING);
		Assert.assertArrayEquals(toStr5.getString(),"false".toCharArray());
		
		final FuncToBooleanNode	toBool1 = new FuncToBooleanNode();
		
		toBool1.addOperand(new ConstantNode("true".toCharArray()));
		Assert.assertEquals(toBool1.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toBool1.getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(toBool1.getBoolean(),true);
		
		final FuncToBooleanNode	toBool2 = new FuncToBooleanNode();
		
		toBool2.addOperand(new ConstantNode(true));
		Assert.assertEquals(toBool2.getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(toBool2.getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(toBool2.getBoolean(),true);
	}
}
