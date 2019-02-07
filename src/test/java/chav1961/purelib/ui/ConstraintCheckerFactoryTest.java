package chav1961.purelib.ui;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.GettersAndSettersFactory.ByteGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.ui.ConstraintCheckerFactory.SyntaxNode;
import chav1961.purelib.ui.interfacers.Constraint;
import chav1961.purelib.ui.interfacers.ConstraintChecker;

public class ConstraintCheckerFactoryTest {
	@Test
	public void termCompilationTest() throws SyntaxException {
		final SyntaxNode[]	result = new SyntaxNode[1]; 
				
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_TERM,"1\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Const,result[0].expr);
		Assert.assertEquals(Long.valueOf(1),result[0].cargo);

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_TERM,"1.0\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Const,result[0].expr);
		Assert.assertEquals(Double.valueOf(1.0),result[0].cargo);

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_TERM,"\"test\"\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Const,result[0].expr);
		Assert.assertEquals("test",result[0].cargo);

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_TERM,"true\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Const,result[0].expr);
		Assert.assertEquals(Boolean.valueOf(true),result[0].cargo);

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_TERM,"false\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Const,result[0].expr);
		Assert.assertEquals(Boolean.valueOf(false),result[0].cargo);

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_TERM,"b1\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Field,result[0].expr);
		Assert.assertTrue(result[0].cargo instanceof ByteGetterAndSetter);
		
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_TERM,"s7\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Field,result[0].expr);
		Assert.assertTrue(result[0].cargo instanceof ObjectGetterAndSetter);
	}

	@Test
	public void unaryCompilationTest() throws SyntaxException {
		final SyntaxNode[]	result = new SyntaxNode[1]; 
				
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_NEG,"-1\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Negation,result[0].expr);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Const,result[0].children[0].expr);

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_NOT,"!false\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Not,result[0].expr);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Const,result[0].children[0].expr);
	}

	@Test
	public void binaryCompilationTest() throws SyntaxException {
		final SyntaxNode[]	result = new SyntaxNode[1]; 
				
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_MUL,"2*3/4%5\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Multiplication,result[0].expr);
		Assert.assertEquals(4,((char[])result[0].cargo).length);
		Assert.assertEquals(4,result[0].children.length);

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_ADD,"2+3-4\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Addition,result[0].expr);
		Assert.assertEquals(3,((char[])result[0].cargo).length);
		Assert.assertEquals(3,result[0].children.length);

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_CONCAT,"\"a\"#\"b\"\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Concat,result[0].expr);
		Assert.assertEquals(2,result[0].children.length);

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_AND,"true&&false\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.And,result[0].expr);
		Assert.assertEquals(2,result[0].children.length);
	
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"true||false\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(ConstraintCheckerFactory.ExprType.Or,result[0].expr);
		Assert.assertEquals(2,result[0].children.length);
	}

	@Test
	public void termCalcTest() throws ContentException {
		final SyntaxNode[]		result = new SyntaxNode[1];
		final PseudoClass4Test	inst = new PseudoClass4Test();
				
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"1\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Long.valueOf(1),ConstraintCheckerFactory.calculate(inst,result[0]));
		
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"1.0\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Double.valueOf(1),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"\"test\"\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals("test",ConstraintCheckerFactory.calculate(inst,result[0]));
		
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"true\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"false\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(false),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"b1\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Byte.valueOf((byte)1),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"s2\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Short.valueOf((short)2),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"i3\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Integer.valueOf(3),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"l4\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Long.valueOf(4),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"f5\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Float.valueOf(5),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"d6\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Double.valueOf(6),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"s7\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals("test",ConstraintCheckerFactory.calculate(inst,result[0]));
	}

	@Test
	public void unaryCalcTest() throws ContentException {
		final SyntaxNode[]		result = new SyntaxNode[1];
		final PseudoClass4Test	inst = new PseudoClass4Test();
				
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"-1\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Long.valueOf(-1),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"-1.5\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Double.valueOf(-1.5),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"!false\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"!true\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(false),ConstraintCheckerFactory.calculate(inst,result[0]));
	}

	@Test
	public void binaryCalcTest() throws ContentException {
		final SyntaxNode[]		result = new SyntaxNode[1];
		final PseudoClass4Test	inst = new PseudoClass4Test();
				
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"4*6/8%4\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Long.valueOf(3),ConstraintCheckerFactory.calculate(inst,result[0]));
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"4*6/8.0%4\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Double.valueOf(3),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"2+5-3\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Long.valueOf(4),ConstraintCheckerFactory.calculate(inst,result[0]));
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"2+5-3.0\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Double.valueOf(4),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"\"te\"#\"st\"\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals("test",ConstraintCheckerFactory.calculate(inst,result[0]));
		
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"true&&false\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(false),ConstraintCheckerFactory.calculate(inst,result[0]));
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"true&&true\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"false||false\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(false),ConstraintCheckerFactory.calculate(inst,result[0]));
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"false||true\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));
	}
	
	@Test
	public void comparisonCalcTest() throws ContentException {
		final SyntaxNode[]		result = new SyntaxNode[1];
		final PseudoClass4Test	inst = new PseudoClass4Test();
				
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"2>3\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(false),ConstraintCheckerFactory.calculate(inst,result[0]));
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"3>2\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"2>=4\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(false),ConstraintCheckerFactory.calculate(inst,result[0]));
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"4>=2\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"3<2\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(false),ConstraintCheckerFactory.calculate(inst,result[0]));
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"2<3\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));
		
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"4<=2\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(false),ConstraintCheckerFactory.calculate(inst,result[0]));
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"2<=4\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"2==3\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(false),ConstraintCheckerFactory.calculate(inst,result[0]));
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"2==2\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"2!=2\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(false),ConstraintCheckerFactory.calculate(inst,result[0]));
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"2!=3\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));

		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"\"2\"==2.0\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));
		ConstraintCheckerFactory.buildTree(ConstraintCheckerFactory.PRTY_OR,"s2==\"2\"\n".toCharArray(),0,PseudoClass4Test.class,result);
		Assert.assertEquals(Boolean.valueOf(true),ConstraintCheckerFactory.calculate(inst,result[0]));
	}	

	@Test
	public void fullTest() throws ContentException, NoSuchFieldException {
		final PseudoClass4Test	inst = new PseudoClass4Test();
		final Constraint		constr = inst.getClass().getDeclaredField("s7").getAnnotation(Constraint.class);
		final ConstraintChecker<PseudoClass4Test>	checker = (ConstraintChecker<PseudoClass4Test>) ConstraintCheckerFactory.buildChecker(inst.getClass(),constr);
		
		Assert.assertEquals("s7==\"test\"",checker.getConstraintExpression());
		Assert.assertEquals(ConstraintChecker.MSG_TEXT,checker.getMessageId());
		Assert.assertEquals(Severity.severe,checker.getSeverity());
		Assert.assertTrue(checker.check(inst));
	}
}

class PseudoClass4Test {
	byte	b1 = 1;
	short	s2 = 2;
	int		i3 = 3;
	long	l4 = 4L;
	float	f5 = 5.0f;
	double	d6 = 6.0;
	@Constraint(value="s7==\"test\"",severity=Severity.severe,messageId=ConstraintChecker.MSG_TEXT)
	String	s7 = "test";
}