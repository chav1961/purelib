package chav1961.purelib.ui;

import java.io.Serializable;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class AbstractInMemoryFormModelTest {
	@Test
	public void constructorsTest() throws NullPointerException, ContentException {
		try{new AbstractInMemoryFormModel<Integer,PseudoData>(null,PseudoData.class,"id"){
				@Override 
				public Integer createUniqueId() throws ContentException {
					return Integer.valueOf(1);
				}
				@Override
				public Class<PseudoData> getInstanceType() {
					return PseudoData.class;
				}
			};
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new AbstractInMemoryFormModel<Integer,PseudoData>(Integer.class,null,"id"){
				@Override 
				public Integer createUniqueId() throws ContentException {
					return Integer.valueOf(1);
				}
				@Override
				public Class<PseudoData> getInstanceType() {
					return PseudoData.class;
				}
			}; 
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new AbstractInMemoryFormModel<Integer,PseudoData>(Integer.class,PseudoData.class,null){
				@Override 
				public Integer createUniqueId() throws ContentException {
					return Integer.valueOf(1);
				}
				@Override
				public Class<PseudoData> getInstanceType() {
					return PseudoData.class;
				}
			};
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new AbstractInMemoryFormModel<Integer,PseudoData>(Integer.class,PseudoData.class,""){
				@Override 
				public Integer createUniqueId() throws ContentException {
					return Integer.valueOf(1);
				}
				@Override
				public Class<PseudoData> getInstanceType() {
					return PseudoData.class;
				}
			};
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new AbstractInMemoryFormModel<Integer,PseudoData>(null,PseudoData.class,"id",new PseudoData[0]){
				@Override 
				public Integer createUniqueId() throws ContentException {
					return Integer.valueOf(1);
				}
				@Override
				public Class<PseudoData> getInstanceType() {
					return PseudoData.class;
				}
			};
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new AbstractInMemoryFormModel<Integer,PseudoData>(Integer.class,null,"id",new PseudoData[0]){
				@Override 
				public Integer createUniqueId() throws ContentException {
					return Integer.valueOf(1);
				}
				@Override
				public Class<PseudoData> getInstanceType() {
					return PseudoData.class;
				}
			};
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new AbstractInMemoryFormModel<Integer,PseudoData>(Integer.class,PseudoData.class,null,new PseudoData[0]){
				@Override 
				public Integer createUniqueId() throws ContentException {
					return Integer.valueOf(1);
				}
				@Override
				public Class<PseudoData> getInstanceType() {
					return PseudoData.class;
				}
			};
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new AbstractInMemoryFormModel<Integer,PseudoData>(Integer.class,PseudoData.class,"",new PseudoData[0]){
				@Override 
				public Integer createUniqueId() throws ContentException {
					return Integer.valueOf(1);
				}
				@Override
				public Class<PseudoData> getInstanceType() {
					return PseudoData.class;
				}
			};
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new AbstractInMemoryFormModel<Integer,PseudoData>(Integer.class,PseudoData.class,"id",null){
				@Override 
				public Integer createUniqueId() throws ContentException {
					return Integer.valueOf(1);
				}
				@Override
				public Class<PseudoData> getInstanceType() {
					return PseudoData.class;
				}
			};
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new AbstractInMemoryFormModel<Integer,PseudoData>(Integer.class,PseudoData.class,"id",new PseudoData[]{null}){
				@Override 
				public Integer createUniqueId() throws ContentException {
					return Integer.valueOf(1);
				}
				@Override
				public Class<PseudoData> getInstanceType() {
					return PseudoData.class;
				}
			};
			Assert.fail("Mandatory exception was not detected (null in the 4-th argument content)");
		} catch (NullPointerException exc) {
		}
	}
	
	
	@Test
	public void basicTest() throws NullPointerException, ContentException {
		final PseudoMemoryFormModel	pmm = new PseudoMemoryFormModel();
		
		Assert.assertEquals(pmm.size(),0);
		Assert.assertEquals(pmm.getCurrentIndex(),-1);
		
		final Integer		pdKey = pmm.createUniqueId();
		final PseudoData	pd = pmm.createInstance(pdKey);
		
		Assert.assertEquals(pmm.size(),1);
		Assert.assertEquals(pmm.getCurrentIndex(),0);
		Assert.assertEquals(pd.id,pdKey);

		try{pmm.createInstance(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{pmm.createInstance(pdKey);
			Assert.fail("Mandatory exception was not detected (dulicate key in the 1-st argument)");
		} catch (ContentException exc) {
		}
		
		pd.array = new int[]{2,3};
		pd.string = "text";
		
		final Integer		pddKey = pmm.createUniqueId();
		final PseudoData	pdd = pmm.duplicateInstance(pdKey,pddKey);
		
		Assert.assertEquals(pmm.size(),2);
		Assert.assertEquals(pmm.getCurrentIndex(),1);
		Assert.assertEquals(pdd.id,pddKey);
		
		Assert.assertEquals(pd.string,pdd.string);
		Assert.assertArrayEquals(pd.array,pdd.array);

		try{pmm.duplicateInstance(null,pddKey);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{pmm.duplicateInstance(pdKey,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{pmm.duplicateInstance(-1,pddKey);
			Assert.fail("Mandatory exception was not detected (1-st argument is non-existent key)");
		} catch (ContentException exc) {
		}
		try{pmm.duplicateInstance(pdKey,pdKey);
			Assert.fail("Mandatory exception was not detected (2-st argument key is already exists)");
		} catch (ContentException exc) {
		}
		
		final PseudoData	found = pmm.getInstance(pddKey), forReplace = new PseudoData();
		
		Assert.assertEquals(pdd,found);
		
		forReplace.id = found.id;
		forReplace.array = new int[]{100,200};
		forReplace.string = "new";
		pmm.updateInstance(pddKey,forReplace);
		
		final PseudoData	foundNew = pmm.getInstance(pddKey);
		
		Assert.assertEquals(pmm.size(),2); 
		Assert.assertEquals(pmm.getCurrentIndex(),1);
		Assert.assertEquals(foundNew.id,pddKey);
	 	
		final PseudoData	deleted = pmm.removeInstance(pddKey);
		
		Assert.assertEquals(pmm.size(),1);
		Assert.assertEquals(pmm.getCurrentIndex(),0);
		Assert.assertEquals(pmm.getCurrentId(),pdKey);
		Assert.assertEquals(deleted.id,pddKey);
		
		Assert.assertNull(pmm.removeInstance(-1));
		
		final Integer		newPdKey = pmm.createUniqueId();
//		final PseudoData	pdInserted = 
				pmm.duplicateInstance(pdKey,newPdKey);
		
		Assert.assertEquals(pmm.size(),2);
		Assert.assertEquals(pmm.getIdByIndex(1),newPdKey);
		Assert.assertEquals(pmm.getIndexById(newPdKey),1);
		
		int	count = 0;
		for (@SuppressWarnings("unused") int id : pmm.contentIds()) {
			count++;
		}
		Assert.assertEquals(count,2);
		Assert.assertEquals(pmm.toArray().length,2);
	}

	@Test
	public void clonedTest() throws NullPointerException, ContentException {
		final PseudoMemoryClonedFormModel	pmcm = new PseudoMemoryClonedFormModel();
		final Integer						key1 = pmcm.createUniqueId(), key2 = pmcm.createUniqueId();
		final PseudoClonedData				data1 = pmcm.createInstance(key1), data2 = pmcm.duplicateInstance(key1,key2);
		
		Assert.assertEquals(pmcm.size(),2);
		Assert.assertEquals(data1.id,key1);
		Assert.assertEquals(data2.id,key2);
		Assert.assertEquals(data1.string,data2.string);
		Assert.assertArrayEquals(data1.array,data2.array);
	}

	@Test
	public void serializableTest() throws NullPointerException, ContentException {
		final PseudoMemorySerializedFormModel	pmsm = new PseudoMemorySerializedFormModel();
		final Integer							key1 = pmsm.createUniqueId(), key2 = pmsm.createUniqueId();
		final PseudoSerializedData				data1 = pmsm.createInstance(key1), data2 = pmsm.duplicateInstance(key1,key2);
		
		Assert.assertEquals(pmsm.size(),2);
		Assert.assertEquals(data1.id,key1);
		Assert.assertEquals(data2.id,key2);
		Assert.assertEquals(data1.string,data2.string);
		Assert.assertArrayEquals(data1.array,data2.array);
		
	}

	@Test
	public void inheritedTest() throws NullPointerException, ContentException {
		final PseudoMemoryChildClonedFormModel	pmcm = new PseudoMemoryChildClonedFormModel();
		final Integer							key1 = pmcm.createUniqueId(), key2 = pmcm.createUniqueId();
		final PseudoClonedChild					data1 = pmcm.createInstance(key1), data2 = pmcm.duplicateInstance(key1,key2);
		
		Assert.assertEquals(pmcm.size(),2);
		Assert.assertEquals(data1.id,key1);
		Assert.assertEquals(data2.id,key2);
		Assert.assertEquals(data1.string,data2.string);
		Assert.assertArrayEquals(data1.array,data2.array);
	}
}


class PseudoMemoryFormModel extends AbstractInMemoryFormModel<Integer,PseudoData> {
	public PseudoMemoryFormModel() throws NullPointerException, ContentException {
		super(Integer.class,PseudoData.class,"id");
	}

	private static int	counter = 0;

	@Override
	public Integer createUniqueId() throws ContentException {
		return counter++;
	}

	@Override
	public Class<PseudoData> getInstanceType() {
		return PseudoData.class;
	}
}

class PseudoMemoryClonedFormModel extends AbstractInMemoryFormModel<Integer,PseudoClonedData> {
	public PseudoMemoryClonedFormModel() throws NullPointerException, ContentException {
		super(Integer.class,PseudoClonedData.class,"id");
	}

	private static int	counter = 0;

	@Override
	public Integer createUniqueId() throws ContentException {
		return counter++;
	}

	@Override
	public Class<PseudoClonedData> getInstanceType() {
		return PseudoClonedData.class;
	}
}

class PseudoMemorySerializedFormModel extends AbstractInMemoryFormModel<Integer,PseudoSerializedData> {
	public PseudoMemorySerializedFormModel() throws NullPointerException, ContentException {
		super(Integer.class,PseudoSerializedData.class,"id");
	}

	private static int	counter = 0;

	@Override
	public Integer createUniqueId() throws ContentException {
		return counter++;
	}

	@Override
	public Class<PseudoSerializedData> getInstanceType() {
		return PseudoSerializedData.class;
	}
}

class PseudoMemoryChildClonedFormModel extends AbstractInMemoryFormModel<Integer,PseudoClonedChild> {
	public PseudoMemoryChildClonedFormModel() throws NullPointerException, ContentException {
		super(Integer.class,PseudoClonedChild.class,"id");
	}

	private static int	counter = 0;

	@Override
	public Integer createUniqueId() throws ContentException {
		return counter++;
	}

	@Override
	public Class<PseudoClonedChild> getInstanceType() {
		return PseudoClonedChild.class;
	}
}

class PseudoData {
	Integer		id;
	String		string;
	int[]		array;
	
	@Override
	public String toString() {
		return "PseudoData [id=" + id + ", string=" + string + ", array=" + Arrays.toString(array) + "]";
	}
}

class PseudoClonedData implements Cloneable {
	Integer		id;
	String		string;
	int[]		array;
	
	@Override
	public String toString() {
		return "PseudoData [id=" + id + ", string=" + string + ", array=" + Arrays.toString(array) + "]";
	}
	
	@Override
	public PseudoClonedData clone() {
		final PseudoClonedData	result = new PseudoClonedData();
		
		result.id = this.id;
		result.array = this.array == null ? null : this.array.clone();
		result.string = this.string;
		return result;
	}
}

class PseudoSerializedData implements Serializable { 
	private static final long serialVersionUID = 9152239024125976652L;
	
	Integer		id;
	String		string;
	int[]		array;
	
	@Override
	public String toString() {
		return "PseudoData [id=" + id + ", string=" + string + ", array=" + Arrays.toString(array) + "]";
	}
}

class PseudoClonedChild extends PseudoClonedData {
	@Override
	public PseudoClonedChild clone() {
		final PseudoClonedChild	result = new PseudoClonedChild();
		
		result.id = this.id;
		result.array = this.array == null ? null : this.array.clone();
		result.string = this.string;
		return result;
	}
}