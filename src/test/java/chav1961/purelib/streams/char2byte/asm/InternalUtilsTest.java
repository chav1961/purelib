package chav1961.purelib.streams.char2byte.asm;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class InternalUtilsTest {
	public InternalUtilsTest(){}

	@Test
	public void softSignatures() throws NoSuchMethodException, SecurityException, NoSuchFieldException {
		final SyntaxTreeInterface<String>	tree = new AndOrTree<String>(1,16);
		final long					field1 = tree.placeName((CharSequence)"java.lang.String",null);
		final long					field2 = tree.placeName((CharSequence)"java.lang.String[]",null);
		final long					field3 = tree.placeName((CharSequence)"int",null);
		final long					returned =  tree.placeName((CharSequence)"void",null);
		
		Assert.assertEquals(InternalUtils.buildFieldSignature(tree,field1),"Ljava/lang/String;");
		Assert.assertEquals(InternalUtils.buildFieldSignature(tree,field2),"[Ljava/lang/String;");
		Assert.assertEquals(InternalUtils.buildFieldSignature(tree,field3),"I");
		
		try{InternalUtils.buildFieldSignature(null,field1);
			Assert.fail("Mandatory exception was not detected(null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.buildFieldSignature(tree,-1);
			Assert.fail("Mandatory exception was not detected(non-existent 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(InternalUtils.buildMethodSignature(tree,true,returned,field1,field2,field3),"(Ljava/lang/String;[Ljava/lang/String;I)V");
		
		try{InternalUtils.buildMethodSignature(null,true,returned);
			Assert.fail("Mandatory exception was not detected(null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.buildMethodSignature(tree,true,-1);
			Assert.fail("Mandatory exception was not detected(non-existent 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.buildMethodSignature(tree,true,returned,-1);
			Assert.fail("Mandatory exception was not detected(non-existent var argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void skipTest() {
		Assert.assertEquals(1,InternalUtils.skipBlank(" \n".toCharArray(),0));
		Assert.assertEquals(5,InternalUtils.skipBlank(" // x\n".toCharArray(),0));
		
		Assert.assertEquals(0,InternalUtils.skipNonBlank(" \n".toCharArray(),0));
		Assert.assertEquals(1,InternalUtils.skipNonBlank("a \n".toCharArray(),0));
	}
	
	public static void testMethod(boolean value1,byte value2,char value3,double val4,float val5,int val6,long val7,String val8,short[][] val9) {}
	
	public static String testField;
}
