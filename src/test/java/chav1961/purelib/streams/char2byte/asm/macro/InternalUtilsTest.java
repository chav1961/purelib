package chav1961.purelib.streams.char2byte.asm.macro;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.char2byte.asm.ExpressionNodeType;
import chav1961.purelib.streams.char2byte.asm.macro.MacroCommand;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class InternalUtilsTest {
	public InternalUtilsTest(){}

	@Test
	public void parseConstantTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		
		Assert.assertEquals(InternalUtils.parseConstant(" \"123\" ".toCharArray(),0,false,false,null,result),6);
		Assert.assertEquals(result[0],new ConstantNode("123".toCharArray()));

		Assert.assertEquals(InternalUtils.parseConstant(" -456 ".toCharArray(),0,false,false,null,result),5);
		Assert.assertEquals(result[0],new ConstantNode(-456));

		Assert.assertEquals(InternalUtils.parseConstant(" 456L ".toCharArray(),0,false,false,null,result),5);
		Assert.assertEquals(result[0],new ConstantNode(456));
		
		Assert.assertEquals(InternalUtils.parseConstant(" 789.05 ".toCharArray(),0,false,false,null,result),7);
		Assert.assertEquals(result[0].getDouble(),new ConstantNode(789.05).getDouble(),0.0001);

		Assert.assertEquals(InternalUtils.parseConstant(" -789.05f ".toCharArray(),0,false,false,null,result),9);
		Assert.assertEquals(result[0].getDouble(),new ConstantNode(-789.05).getDouble(),0.0001);
		
		Assert.assertEquals(InternalUtils.parseConstant(" true ".toCharArray(),0,false,false,null,result),4);
		Assert.assertEquals(result[0],new ConstantNode(true));

		Assert.assertEquals(InternalUtils.parseConstant(" false ".toCharArray(),0,false,false,null,result),5);
		Assert.assertEquals(result[0],new ConstantNode(false));
		
		try{InternalUtils.parseConstant(" tygydym ".toCharArray(),0,false,false,null,result);
			Assert.fail("Mandatory exception was not detected (non-boolean constant)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.parseConstant(" ??? ".toCharArray(),0,false,false,null,result);
			Assert.fail("Mandatory exception was not detected (unknown chars)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.parseConstant(" -true ".toCharArray(),0,false,false,null,result);
			Assert.fail("Mandatory exception was not detected (minus sign invalid)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.parseConstant(" +\"1\" ".toCharArray(),0,false,false,null,result);
			Assert.fail("Mandatory exception was not detected (plus sign invalid)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void parseConstantInExpressionTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		final MacroCommand		cmd = new MacroCommand("MACRO".toCharArray());
		
		cmd.commitDeclarations();
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," \"123\" \n".toCharArray(),0,0,null,result),6);
		Assert.assertEquals(result[0],new ConstantNode("123".toCharArray()));

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," 456 \n".toCharArray(),0,0,null,result),4);
		Assert.assertEquals(result[0],new ConstantNode(456));

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," 456L \n".toCharArray(),0,0,null,result),5);
		Assert.assertEquals(result[0],new ConstantNode(456));
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," 789.05 \n".toCharArray(),0,0,null,result),7);
		Assert.assertEquals(result[0].getDouble(),new ConstantNode(789.05).getDouble(),0.0001);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," 789.05f \n".toCharArray(),0,0,null,result),8);
		Assert.assertEquals(result[0].getDouble(),new ConstantNode(789.05).getDouble(),0.0001);
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," true \n".toCharArray(),0,0,null,result),6);
		Assert.assertEquals(result[0],new ConstantNode(true));

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," false \n".toCharArray(),0,0,null,result),7);
		Assert.assertEquals(result[0],new ConstantNode(false));
		
		try{InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," unknown \n".toCharArray(),0,0,cmd,result);
			Assert.fail("Mandatory exception was not detected (unknown constant name)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void parseVariableTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		final MacroCommand		cmd = new MacroCommand("MACRO".toCharArray());

		cmd.addDeclaration(new LocalVariable("var1".toCharArray(),ExpressionNodeValue.STRING));
		cmd.commitDeclarations();
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," var1 \n".toCharArray(),0,0,cmd,result),6);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.LOCAL_VARIABLE);
		
		try{InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," unknown \n".toCharArray(),0,0,cmd,result);
			Assert.fail("Mandatory exception was not detected (unknown constant name)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void parseFunctionsTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		final MacroCommand		cmd = new MacroCommand("MACRO".toCharArray());

		cmd.addDeclaration(new LocalVariable("var1".toCharArray(),ExpressionNodeValue.STRING));
		cmd.commitDeclarations();
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," uniqueG() \n".toCharArray(),0,0,cmd,result),10);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(result[0].getLong(),cmd.uniqueG);
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," uniqueL() \n".toCharArray(),0,0,cmd,result),10);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(result[0].getLong(),2);
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," exists(true) \n".toCharArray(),0,0,cmd,result),13);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," exists(var1) \n".toCharArray(),0,0,cmd,result),13);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),false);
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," int(\"123\") \n".toCharArray(),0,0,cmd,result),11);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(result[0].getLong(),123);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," real(\"123\") \n".toCharArray(),0,0,cmd,result),12);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(result[0].getDouble(),123.0,0.0001);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," str(123) \n".toCharArray(),0,0,cmd,result),9);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.STRING);
		Assert.assertArrayEquals(result[0].getString(),"123".toCharArray());

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERM,0," bool(\"true\") \n".toCharArray(),0,0,cmd,result),13);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);
	}
	
	@Test
	public void negTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		final MacroCommand		cmd = new MacroCommand("MACRO".toCharArray());
		
		cmd.commitDeclarations();
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_UNARY,0," 123 \n".toCharArray(),0,0,cmd,result),4);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.CONSTANT);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(result[0].getLong(),123);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_UNARY,0," -123 \n".toCharArray(),0,0,cmd,result),5);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(result[0].getLong(),-123);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_UNARY,0," 123.4 \n".toCharArray(),0,0,cmd,result),6);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.CONSTANT);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(result[0].getDouble(),123.4,0.0001);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_UNARY,0," -123.4 \n".toCharArray(),0,0,cmd,result),7);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(result[0].getDouble(),-123.4,0.0001);

		try{InternalUtils.parseExpression(InternalUtils.ORDER_UNARY,0," -\"123\" \n".toCharArray(),0,0,cmd,result);
			Assert.fail("Mandatory exception was not detected (illegal type for minus)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void arithmTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		final MacroCommand		cmd = new MacroCommand("MACRO".toCharArray());
		
		cmd.commitDeclarations();
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_MUL,0," 3*5/5%4 \n".toCharArray(),0,0,cmd,result),9);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(result[0].getLong(),3);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_MUL,0," 3.0*5.0/5.0%4.0 \n".toCharArray(),0,0,cmd,result),17);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(result[0].getDouble(),3.0,0.0001);
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_MUL,0," 3*5/5%4.0 \n".toCharArray(),0,0,cmd,result),11);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(result[0].getDouble(),3.0,0.0001);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_MUL,0," 3*5/5.0%4 \n".toCharArray(),0,0,cmd,result),11);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(result[0].getDouble(),3.0,0.0001);
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_ADD,0," 2+3-4 \n".toCharArray(),0,0,cmd,result),7);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(result[0].getLong(),1);
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_ADD,0," 2.0+3.0-4.0 \n".toCharArray(),0,0,cmd,result),13);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(result[0].getDouble(),1,0.0001);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_ADD,0," 2+3-4.0 \n".toCharArray(),0,0,cmd,result),9);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(result[0].getDouble(),1,0.0001);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_ADD,0," 2+3.0-4 \n".toCharArray(),0,0,cmd,result),9);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.REAL);
		Assert.assertEquals(result[0].getDouble(),1,0.0001);
	}

	@Test
	public void concatTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		final MacroCommand		cmd = new MacroCommand("MACRO".toCharArray());
		
		cmd.commitDeclarations();

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_CAT,0," \"123\"#\"456\"#\"789\" \n".toCharArray(),0,0,cmd,result),19);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.STRING);
		Assert.assertArrayEquals(result[0].getString(),"123456789".toCharArray());

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_CAT,0," 123#\"456\"#789.0#false \n".toCharArray(),0,0,cmd,result),23);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.STRING);
		Assert.assertArrayEquals(result[0].getString(),"123456789.0false".toCharArray());
	}

	@Test
	public void ternaryTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		final MacroCommand		cmd = new MacroCommand("MACRO".toCharArray());
		
		cmd.commitDeclarations();

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERNARY,0," true ? 123 : 456 \n".toCharArray(),0,0,cmd,result),18);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(result[0].getLong(),123);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_TERNARY,0," false ? 123 : 456 \n".toCharArray(),0,0,cmd,result),19);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.INTEGER);
		Assert.assertEquals(result[0].getLong(),456);
	}

	@Test
	public void comparisonTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		final MacroCommand		cmd = new MacroCommand("MACRO".toCharArray());
		
		cmd.commitDeclarations();

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 100 == 100 \n".toCharArray(),0,0,cmd,result),12);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 100 == 200 \n".toCharArray(),0,0,cmd,result),12);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),false);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 100 != 200 \n".toCharArray(),0,0,cmd,result),12);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 100 != 100 \n".toCharArray(),0,0,cmd,result),12);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),false);
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 200 > 100 \n".toCharArray(),0,0,cmd,result),11);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 100 > 200 \n".toCharArray(),0,0,cmd,result),11);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),false);
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 100 >= 100 \n".toCharArray(),0,0,cmd,result),12);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 100 >= 200 \n".toCharArray(),0,0,cmd,result),12);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),false);
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 100 < 200 \n".toCharArray(),0,0,cmd,result),11);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 200 < 100 \n".toCharArray(),0,0,cmd,result),11);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),false);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 100 <= 100 \n".toCharArray(),0,0,cmd,result),12);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_COMPARE,0," 200 <= 100 \n".toCharArray(),0,0,cmd,result),12);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),false);
	}

	@Test
	public void notTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		final MacroCommand		cmd = new MacroCommand("MACRO".toCharArray());
		
		cmd.commitDeclarations();
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_NOT,0," !true \n".toCharArray(),0,0,cmd,result),7);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),false);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_NOT,0," !false \n".toCharArray(),0,0,cmd,result),8);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);
	}

	@Test
	public void logicalTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		final MacroCommand		cmd = new MacroCommand("MACRO".toCharArray());
		
		cmd.commitDeclarations();

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_AND,0," true && true && true \n".toCharArray(),0,0,cmd,result),22);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_AND,0," true && true && false \n".toCharArray(),0,0,cmd,result),23);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),false);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_OR,0," false || false || false \n".toCharArray(),0,0,cmd,result),25);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),false);

		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_OR,0," false || false || true \n".toCharArray(),0,0,cmd,result),24);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);
	}

	@Test
	public void complexParseTest() throws CalculationException, SyntaxException {
		final ExpressionNode[]	result = new ExpressionNode[1];
		final MacroCommand		cmd = new MacroCommand("MACRO".toCharArray());

		cmd.addDeclaration(new LocalVariable("var1".toCharArray(),ExpressionNodeValue.STRING));
		cmd.commitDeclarations();
		
		Assert.assertEquals(InternalUtils.parseExpression(InternalUtils.ORDER_OR,0," 2 * (3 + 5) < 100 && !exists(var1) \n".toCharArray(),0,0,cmd,result),36);
		Assert.assertEquals(result[0].getType(),ExpressionNodeType.EXPRESSION);
		Assert.assertEquals(result[0].getValueType(),ExpressionNodeValue.BOOLEAN);
		Assert.assertEquals(result[0].getBoolean(),true);
	}

	
	
	
	public static void testMethod(boolean value1,byte value2,char value3,double val4,float val5,int val6,long val7,String val8,short[][] val9) {}
	
	public static String testField;
}
