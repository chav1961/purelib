package chav1961.purelib.basic;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class SequenceIteratorTest {

	@SuppressWarnings("unchecked")
	@Test
	public void basicTest() {
		final List<String>	inst1 = new ArrayList<>(), inst2 = new ArrayList<>();
		final Set<String>	collection = new HashSet<>(), template = new HashSet<>();

		template.clear();	template.add("1");	template.add("2");
		inst1.clear();		inst1.add("1");		inst1.add("2");
		
		collection.clear();	
		for(String item : new WrapperIterable<String>(inst1.iterator())) {
			collection.add(item);
		}
		Assert.assertEquals(collection,template);

		template.clear();	template.add("1");	template.add("2");
		inst1.clear();		inst1.add("2");
		inst2.clear();		inst2.add("1");
		
		collection.clear();	
		for(String item : new WrapperIterable<String>(inst1.iterator(),inst2.iterator())) {
			collection.add(item);
		}
		Assert.assertEquals(collection,template);

		template.clear();	template.add("1");	template.add("2");
		inst1.clear();		
		inst2.clear();		inst2.add("2");		inst2.add("1");
		
		collection.clear();	
		for(String item : new WrapperIterable<String>(inst1.iterator(),inst2.iterator())) {
			collection.add(item);
		}
		Assert.assertEquals(collection,template);

		collection.clear();	
		for(String item : new WrapperIterable<String>(inst1,inst2)) {
			collection.add(item);
		}
		Assert.assertEquals(collection,template);
		
		try{new SequenceIterator<String>((Iterable<String>[])null);
			Assert.fail("Mandatory exception was not detected (null list)");
		} catch (IllegalArgumentException exc) {
		}
		try{new SequenceIterator<String>((Iterator<String>[])null);
			Assert.fail("Mandatory exception was not detected (null list)");
		} catch (IllegalArgumentException exc) {
		}
		try{new SequenceIterator<String>(inst1.iterator(),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument in the list)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	private static class WrapperIterable<Str> implements Iterable<Str> {
		private final Iterator<Str>[]	content;
		
		@SafeVarargs
		WrapperIterable(final Iterator<Str>... content) {
			this.content = content;
		}

		@SafeVarargs
		WrapperIterable(final Iterable<Str>... content) {
			this.content = SequenceIterator.toIterators(content);
		}
		
		@Override
		public Iterator<Str> iterator() {
			return new SequenceIterator<Str>(content);
		}		
	}
}
