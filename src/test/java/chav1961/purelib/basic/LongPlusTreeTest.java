package chav1961.purelib.basic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.BPlusTree.BPlusTreeContentException;
import chav1961.purelib.basic.LongBPlusTree.LongNodeAccessor;
import chav1961.purelib.basic.interfaces.LongBPlusTreeNode;
import chav1961.purelib.enumerations.ContinueMode;

public class LongPlusTreeTest {
	@Test
	public void basicNodeAccessorTest() throws BPlusTreeContentException {
		final LongBPlusTreeNode<Long, String>		node1 = new TestLongBPlusTreeNode(), node2 = new TestLongBPlusTreeNode();
		final LongNodeAccessor<LongBPlusTreeNode<Long, String>>	acc = LongBPlusTree.buildInMemoryNodeAccessor(Long.class, String.class);
		final long		id1 = acc.createIntermediate(), id2 = acc.createLeaf();
		
		Assert.assertEquals(0, acc.getRootId());
		Assert.assertNotNull(acc.getContent(id1));

		acc.storeContent(id1, node1);
		Assert.assertEquals(0, acc.getRootId());
		Assert.assertEquals(node1, acc.getContent(id1));
		
		acc.storeContent(id2, node2);
		Assert.assertEquals(node2, acc.getContent(id2));
	}

	@Test
	public void lifeCycleTreeTest() throws BPlusTreeContentException {
		final LongBPlusTree<Long, String>	tree = LongBPlusTree.buildInMemoryBPlusTree(Long.class, String.class);
		final Set<String>	content = new HashSet<>();
		final int[]			count = {0};

		tree.walk((k,v)->{
			content.add(v);
			count[0]++; 
			return ContinueMode.CONTINUE;
		});
		Assert.assertEquals(0, count[0]);
		
		tree.insert(100L, "100");

		content.clear();
		count[0] = 0;
		tree.walk((k,v)->{
			content.add(v);
			count[0]++; 
			return ContinueMode.CONTINUE;
		});
		Assert.assertEquals(1, count[0]);
		Assert.assertTrue(content.equals(new HashSet<>(Arrays.asList("100"))));

		tree.insert(200L, "200");

		content.clear();
		count[0] = 0;
		tree.walk((k,v)->{
			content.add(v);
			count[0]++; 
			return ContinueMode.CONTINUE;
		});
		Assert.assertEquals(2, count[0]);
		Assert.assertTrue(content.equals(new HashSet<>(Arrays.asList("100","200"))));
		
		Assert.assertEquals("100", tree.get(100L));
		Assert.assertNull(tree.get(300L));
		Assert.assertNull(tree.get(0L));
		
		Assert.assertArrayEquals(new String[]{"100","200"}, tree.get(100L,200L,true,true));
		
		Assert.assertEquals("100",tree.delete(100L));

		content.clear();
		count[0] = 0;
		tree.walk((k,v)->{
			content.add(v);
			count[0]++; 
			return ContinueMode.CONTINUE;
		});
		Assert.assertEquals(1, count[0]);
		Assert.assertTrue(content.equals(new HashSet<>(Arrays.asList("200"))));
	}
}


class TestLongBPlusTreeNode implements LongBPlusTreeNode<Long, String> {

	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKey(Long key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKeyGE(Long key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKeyLE(Long key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getKeysGE(Long key, Consumer<Long> accept) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getKeysLE(Long key, Consumer<Long> accept) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Long getFirstLeafKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getLastLeafKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValue(Long key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String delete(Long key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(Long key, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canInsert() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canCompact() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void join() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getIdForKey(Long key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getIdForKeyGE(Long key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getIdForKeyLE(Long key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCurrentId() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public long getNextSiblingId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getPrevSiblingId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public LongBPlusTreeNode<Long, String>[] split(long left, long right) {
		// TODO Auto-generated method stub
		return null;
	}
}