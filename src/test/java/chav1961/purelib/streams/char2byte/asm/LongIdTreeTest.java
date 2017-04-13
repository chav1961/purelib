package chav1961.purelib.streams.char2byte.asm;


import org.junit.Assert;

import org.junit.Test;

public class LongIdTreeTest {
	@Test
	public void lifeCycleTest() {
		final LongIdTree<String>	lit = new LongIdTree<String>(3);
		
		Assert.assertEquals(lit.getRef(1,3,5),0);
		lit.addRef((short)100,1,3,5);
		Assert.assertEquals(lit.getRef(1,3,5),100);
		
		lit.addRef((short)101,1,1,1);
		lit.addRef((short)102,1,10,100);
		lit.addRef((short)103,1,5,3);
		Assert.assertEquals(lit.getRef(1,1,1),101);
		Assert.assertEquals(lit.getRef(1,10,100),102);
		Assert.assertEquals(lit.getRef(1,5,3),103);
		Assert.assertEquals(lit.getRef(1,10,101),0);
		
		Assert.assertNull(lit.getCargo(1,1,1));
		Assert.assertFalse(lit.setCargo("123456",1,2,3));
		Assert.assertTrue(lit.setCargo("123456",1,1,1));
		Assert.assertEquals(lit.getCargo(1,1,1),"123456");
		
		lit.clear();
		Assert.assertEquals(lit.getRef(1,3,5),0);
	}

	@Test
	public void illegalParametersTest() {
		try{new LongIdTree(0);
			Assert.fail("Mandatory exception was not detected (initial parameter outsize the bounds)");
		} catch (IllegalArgumentException exc) {
		}
		try{new LongIdTree(100);
			Assert.fail("Mandatory exception was not detected (initial parameter outsize the bounds)");
		} catch (IllegalArgumentException exc) {
		}

		final LongIdTree	lit = new LongIdTree(3);
		try{lit.getRef(1);
			Assert.fail("Mandatory exception was not detected (different amount of parameters and initial parameter size)");
		} catch (IllegalArgumentException exc) {
		}
		try{lit.addRef((short)0,1);
			Assert.fail("Mandatory exception was not detected (zero ref as parameter)");
		} catch (IllegalArgumentException exc) {
		}		
		try{lit.addRef((short)1,1);
			Assert.fail("Mandatory exception was not detected (different amount of parameters and initial parameter size)");
		} catch (IllegalArgumentException exc) {
		}		
		try{lit.getRef(1);
			Assert.fail("Mandatory exception was not detected (different amount of parameters and initial parameter size)");
		} catch (IllegalArgumentException exc) {
		}		
		try{lit.setCargo(null,1);
			Assert.fail("Mandatory exception was not detected (different amount of parameters and initial parameter size)");
		} catch (IllegalArgumentException exc) {
		}		
		try{lit.getCargo(1);
			Assert.fail("Mandatory exception was not detected (different amount of parameters and initial parameter size)");
		} catch (IllegalArgumentException exc) {
		}		
		lit.clear();
	}
}
