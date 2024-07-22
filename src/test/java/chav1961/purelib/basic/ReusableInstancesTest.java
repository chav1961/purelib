package chav1961.purelib.basic;

import org.junit.Assert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class ReusableInstancesTest {
	@Test
	public void test() {
		try(final ReusableInstances<PseudoInstance>	repo = new ReusableInstances<>(()->{return new PseudoInstance();})) {
				
			final PseudoInstance 	inst = repo.allocate();//, inst2 = repo.allocate();
			
			Assert.assertEquals(inst.field,10);
			
			inst.field = 20;
			repo.free(inst);
			
			final PseudoInstance 	instReused = repo.allocate();
			
			Assert.assertEquals(inst,instReused);
			Assert.assertEquals(instReused.field,inst.field);
			Assert.assertEquals(inst.field,20);
			
			try(final ReusableInstances<PseudoInstance>	var = new ReusableInstances<PseudoInstance>(null)){
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}
	}
	
	private static class PseudoInstance {
		int		field = 10;
		
		public PseudoInstance(){}
	}
}

