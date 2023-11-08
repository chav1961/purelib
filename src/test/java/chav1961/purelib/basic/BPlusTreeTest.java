package chav1961.purelib.basic;

import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.BPlusTree.BPlusTreeContentException;
import chav1961.purelib.basic.LongBPlusTree.LongNodeAccessor;
import chav1961.purelib.basic.interfaces.LongBPlusTreeNode;

public class BPlusTreeTest {
	@Test
	public void basicNodeAccessorTest() throws BPlusTreeContentException {
		final LongBPlusTreeNode<Long, String>		node1 = new TestBPlusTreeNode(), node2 = new TestBPlusTreeNode();
		final LongNodeAccessor<LongBPlusTreeNode<Long, String>>	acc = LongBPlusTree.buildInMemoryNodeAccessor(Long.class, String.class);
		final long		id1 = 1, id2 = 2;
		
		Assert.assertEquals(0, acc.getRootId());
		Assert.assertNull(acc.getContent(id1));

		acc.storeContent(id1, node1);
		Assert.assertEquals(0, acc.getRootId());
		Assert.assertEquals(node1, acc.getContent(id1));
	}
}


class TestBPlusTreeNode implements LongBPlusTreeNode<Long, String> {

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
	public void split(long left, long right) {
		// TODO Auto-generated method stub
		
	}
}