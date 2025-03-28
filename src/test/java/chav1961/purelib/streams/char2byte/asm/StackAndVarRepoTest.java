package chav1961.purelib.streams.char2byte.asm;


import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.streams.char2byte.asm.StackAndVarRepo.StackChangesCallback;

@Tag("OrdinalTestCategory")
public class StackAndVarRepoTest {
	@Test
	public void basicTest() throws ContentException {
		final int[]					changes = new int[3];		
		final StackChangesCallback	callback = new StackChangesCallback() {
										@Override
										public void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom) {
											changes[0] = deletedFrom;
											changes[1] = insertedFrom;
											changes[2] = changedFrom; 
										}
									};
		final StackAndVarRepo		repo = new StackAndVarRepo(callback);
		
		repo.processChanges(StackChanges.none);
		Assert.assertArrayEquals(new int[] {-1,-1,-1},changes);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(StackChanges.pushInt);
		Assert.assertArrayEquals(new int[] {-1,1,-1},changes);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));

		repo.processChanges(StackChanges.pop);
		Assert.assertArrayEquals(new int[] {1,-1,-1},changes);
		Assert.assertEquals(0,repo.getCurrentStackDepth());
	
		repo.processChanges(StackChanges.pushFloat);
		Assert.assertArrayEquals(new int[] {-1,1,-1},changes);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(0));

		repo.processChanges(StackChanges.pop);
		Assert.assertArrayEquals(new int[] {1,-1,-1},changes);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(StackChanges.pushReference);
		Assert.assertArrayEquals(new int[] {-1,1,-1},changes);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(0));

		repo.processChanges(StackChanges.pop);
		Assert.assertArrayEquals(new int[] {1,-1,-1},changes);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(StackChanges.pushLong);
		Assert.assertArrayEquals(new int[] {-1,2,-1},changes);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));

		repo.processChanges(StackChanges.pop2);
		Assert.assertArrayEquals(new int[] {2,-1,-1},changes);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(StackChanges.pushDouble);
		Assert.assertArrayEquals(new int[] {-1,2,-1},changes);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-1));

		try{repo.select(-2);
			Assert.fail("Mandatory exception was not detected (stack exhausted)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.pop2);
		Assert.assertArrayEquals(new int[] {2,-1,-1},changes);
		Assert.assertEquals(0,repo.getCurrentStackDepth());
		
		try{repo.processChanges(StackChanges.pop);
			Assert.fail("Mandatory exception was not detected (stack exhausted)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushFloat);
		
		repo.processChanges(StackChanges.swap);
		Assert.assertArrayEquals(new int[] {-1,-1,0},changes);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(-1));
		
		repo.loadStackSnapshot(repo.makeStackSnapshot());		// Test snapshot manipulations
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(-1));
		
		repo.processChanges(StackChanges.clear);
		Assert.assertEquals(0,repo.getCurrentStackDepth());
	}

	@Test
	public void duplicatesTest() throws ContentException {
		final int[]					changes = new int[3];		
		final StackChangesCallback	callback = new StackChangesCallback() {
										@Override
										public void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom) {
											changes[0] = deletedFrom;
											changes[1] = insertedFrom;
											changes[2] = changedFrom; 
										}
									};
		final StackAndVarRepo		repo = new StackAndVarRepo(callback);

		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.dup);
		Assert.assertArrayEquals(new int[] {-1,1,-1},changes);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));
		
		repo.processChanges(StackChanges.dup2);
		Assert.assertArrayEquals(new int[] {-1,2,-1},changes);
		Assert.assertEquals(4,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(-3));
		
		repo.processChanges(StackChanges.pop4);
		
		try {repo.processChanges(StackChanges.dup);
			Assert.fail("Mandatory exception was not detected (empty stack)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pushLong);
		try {repo.processChanges(StackChanges.dup);
			Assert.fail("Mandatory exception was not detected (attempt to push half of long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.dup2);
		Assert.assertArrayEquals(new int[] {-1,2,-1},changes);
		Assert.assertEquals(4,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-3));

		repo.processChanges(StackChanges.pop4);

		repo.processChanges(StackChanges.pushDouble);
		try {repo.processChanges(StackChanges.dup);
			Assert.fail("Mandatory exception was not detected (attempt to push half of double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.dup2);
		Assert.assertArrayEquals(new int[] {-1,2,-1},changes);
		Assert.assertEquals(4,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-1));
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-3));

		repo.processChanges(StackChanges.pop4);
		
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushFloat);
		
		repo.processChanges(StackChanges.dup_x1);
		Assert.assertEquals(3,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(-2));
		
		repo.processChanges(StackChanges.pop3);
		
		repo.processChanges(StackChanges.pushLong);
		try {repo.processChanges(StackChanges.dup_x1);
			Assert.fail("Mandatory exception was not detected (attempt to push half of long)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.pushInt);
		try {repo.processChanges(StackChanges.dup_x1);
			Assert.fail("Mandatory exception was not detected (attempt to insert into long)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.pop3);
		
		repo.processChanges(StackChanges.pushDouble);
		try {repo.processChanges(StackChanges.dup_x1);
			Assert.fail("Mandatory exception was not detected (attempt to push half of double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pushInt);
		try {repo.processChanges(StackChanges.dup_x1);
			Assert.fail("Mandatory exception was not detected (attempt to insert into double)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.pop3);
		
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushFloat);
		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.dup_x2);
		Assert.assertEquals(4,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(-3));
		
		repo.processChanges(StackChanges.pop4);

		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushLong);
		try {repo.processChanges(StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half of long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pushInt);
		try {repo.processChanges(StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pop4);

		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushDouble);
		try {repo.processChanges(StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half of double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pushInt);
		try {repo.processChanges(StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pop4);

		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushFloat);
		repo.processChanges(StackChanges.dup2_x1);
		Assert.assertEquals(5,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(-3));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(-4));
	
		repo.processChanges(StackChanges.pop4);
		repo.processChanges(StackChanges.pop);
		
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.dup2_x1);
		Assert.assertEquals(5,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(-2));
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(-3));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-4));
	
		repo.processChanges(StackChanges.pop4);
		repo.processChanges(StackChanges.pop);
		
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushDouble);
		repo.processChanges(StackChanges.dup2_x1);
		Assert.assertEquals(5,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(-2));
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(-3));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-4));
	
		repo.processChanges(StackChanges.pop4);
		repo.processChanges(StackChanges.pop);

		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pushInt);
		try {repo.processChanges(StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half of long)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.pop);
		repo.processChanges(StackChanges.pushLong);
		try {repo.processChanges(StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pop4);
		
		repo.processChanges(StackChanges.pushDouble);
		repo.processChanges(StackChanges.pushInt);
		try {repo.processChanges(StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half of double)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.pop);
		repo.processChanges(StackChanges.pushDouble);
		try {repo.processChanges(StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pop4);

		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushFloat);
		repo.processChanges(StackChanges.dup2_x2);
		Assert.assertEquals(6,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(-3));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(-4));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(-5));
	
		repo.processChanges(StackChanges.pop4);
		repo.processChanges(StackChanges.pop2);

		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.dup2_x2);
		Assert.assertEquals(6,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(-3));
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(-4));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-5));
	
		repo.processChanges(StackChanges.pop4);
		repo.processChanges(StackChanges.pop2);

		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushDouble);
		repo.processChanges(StackChanges.dup2_x2);
		Assert.assertEquals(6,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(-3));
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(-4));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-5));
	
		repo.processChanges(StackChanges.pop4);
		repo.processChanges(StackChanges.pop2);

		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pushReference);
		try {repo.processChanges(StackChanges.dup2_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pushLong);
		try {repo.processChanges(StackChanges.dup2_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pop4);
		repo.processChanges(StackChanges.pop2);

		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushDouble);
		repo.processChanges(StackChanges.pushReference);
		try {repo.processChanges(StackChanges.dup2_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pushDouble);
		try {repo.processChanges(StackChanges.dup2_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pop4);
		repo.processChanges(StackChanges.pop2);
	}

	@Test
	public void changesTest() throws ContentException {
		final int[]					changes = new int[3];		
		final StackChangesCallback	callback = new StackChangesCallback() {
										@Override
										public void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom) {
											changes[0] = deletedFrom;
											changes[1] = insertedFrom;
											changes[2] = changedFrom; 
										}
									};
		final StackAndVarRepo		repo = new StackAndVarRepo(callback);
		
		repo.processChanges(StackChanges.pushInt);
		
		try {repo.processChanges(StackChanges.changeFloat2Int);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(StackChanges.changeLong2Int);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(StackChanges.changeDouble2Int);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.changeInt2Float);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(0));
		
		repo.processChanges(StackChanges.changeFloat2Int);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));

		repo.processChanges(StackChanges.changeInt2Long);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));

		repo.processChanges(StackChanges.changeLong2Int);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));

		repo.processChanges(StackChanges.changeInt2Double);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-1));

		repo.processChanges(StackChanges.changeDouble2Int);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));

		repo.processChanges(StackChanges.pop);
		repo.processChanges(StackChanges.pushFloat);

		try {repo.processChanges(StackChanges.changeInt2Float);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(StackChanges.changeLong2Float);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(StackChanges.changeDouble2Float);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.changeFloat2Long);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));

		repo.processChanges(StackChanges.changeLong2Float);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(0));

		repo.processChanges(StackChanges.changeFloat2Double);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-1));

		repo.processChanges(StackChanges.changeDouble2Float);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(0));

		repo.processChanges(StackChanges.pop);
		repo.processChanges(StackChanges.pushLong);

		try {repo.processChanges(StackChanges.changeInt2Long);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(StackChanges.changeFloat2Long);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(StackChanges.changeDouble2Long);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		 
		repo.processChanges(StackChanges.changeLong2Double);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-1));

		repo.processChanges(StackChanges.changeDouble2Long);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));
	}

	@Test
	public void popAndPushTest() throws ContentException {
		final int[]					changes = new int[3];		
		final StackChangesCallback	callback = new StackChangesCallback() {
										@Override
										public void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom) {
											changes[0] = deletedFrom;
											changes[1] = insertedFrom;
											changes[2] = changedFrom; 
										}
									};
		final StackAndVarRepo		repo = new StackAndVarRepo(callback);

		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.popAndPushInt);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));
		
		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.popAndPushFloat);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(0));

		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.popAndPushReference);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(0));

		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pop2AndPushInt);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));

		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pop2AndPushFloat);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.select(0));

		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pop2AndPushReference);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(0));

		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pop2AndPushLong);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));

		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pop2AndPushDouble);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-1));

		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pop4AndPushInt);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));

		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pop4AndPushLong);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));

		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.pop4AndPushDouble);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-1));

		repo.processChanges(StackChanges.clear);
	}

	@Test
	public void fieldsAndMultiArrayTest() throws ContentException {
		final int[]					changes = new int[3];		
		final StackChangesCallback	callback = new StackChangesCallback() {
										@Override
										public void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom) {
											changes[0] = deletedFrom;
											changes[1] = insertedFrom;
											changes[2] = changedFrom; 
										}
									};
		final StackAndVarRepo		repo = new StackAndVarRepo(callback);

		repo.processChanges(StackChanges.pushStatic,CompilerUtils.CLASSTYPE_INT);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));
		
		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushStatic,CompilerUtils.CLASSTYPE_LONG);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));
		
		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushStatic,CompilerUtils.CLASSTYPE_DOUBLE);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.select(-1));
		
		repo.processChanges(StackChanges.clear);

		
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.popStatic,CompilerUtils.CLASSTYPE_INT);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.popStatic,CompilerUtils.CLASSTYPE_LONG);
		Assert.assertEquals(0,repo.getCurrentStackDepth());
		
		repo.processChanges(StackChanges.pushDouble);
		repo.processChanges(StackChanges.popStatic,CompilerUtils.CLASSTYPE_DOUBLE);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(StackChanges.pushInt);
		try {repo.processChanges(StackChanges.popStatic,CompilerUtils.CLASSTYPE_LONG);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(StackChanges.popStatic,CompilerUtils.CLASSTYPE_DOUBLE);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushLong);
		try {repo.processChanges(StackChanges.popStatic,CompilerUtils.CLASSTYPE_INT);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(StackChanges.popStatic,CompilerUtils.CLASSTYPE_DOUBLE);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushDouble);
		try {repo.processChanges(StackChanges.popStatic,CompilerUtils.CLASSTYPE_INT);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(StackChanges.popStatic,CompilerUtils.CLASSTYPE_LONG);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushField,CompilerUtils.CLASSTYPE_INT);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));

		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushField,CompilerUtils.CLASSTYPE_LONG);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));

		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushInt);
		try {repo.processChanges(StackChanges.pushField,CompilerUtils.CLASSTYPE_INT);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.clear);

		
		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.popField,CompilerUtils.CLASSTYPE_INT);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushLong);
		repo.processChanges(StackChanges.popField,CompilerUtils.CLASSTYPE_LONG);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushInt);
		try {repo.processChanges(StackChanges.popField,CompilerUtils.CLASSTYPE_LONG);
			Assert.fail("Mandatory exception was not detected (attempt to unload non-long)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(StackChanges.popField,CompilerUtils.CLASSTYPE_DOUBLE);
			Assert.fail("Mandatory exception was not detected (attempt to unload non-double)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushLong);
		try {repo.processChanges(StackChanges.popField,CompilerUtils.CLASSTYPE_INT);
			Assert.fail("Mandatory exception was not detected (attempt to unload half long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushDouble);
		try {repo.processChanges(StackChanges.popField,CompilerUtils.CLASSTYPE_INT);
			Assert.fail("Mandatory exception was not detected (attempt to unload half double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushInt);
		try {repo.processChanges(StackChanges.popField,CompilerUtils.CLASSTYPE_INT);
			Assert.fail("Mandatory exception was not detected (reference near top is missing)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushLong);
		try {repo.processChanges(StackChanges.popField,CompilerUtils.CLASSTYPE_LONG);
			Assert.fail("Mandatory exception was not detected (reference near top is missing)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.clear);

		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.pushDouble);
		try {repo.processChanges(StackChanges.popField,CompilerUtils.CLASSTYPE_DOUBLE);
			Assert.fail("Mandatory exception was not detected (reference near top is missing)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushInt);
		repo.processChanges(StackChanges.multiarrayAndPushReference,1);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.select(0));

		repo.processChanges(StackChanges.clear);
		
		repo.processChanges(StackChanges.pushReference);
		repo.processChanges(StackChanges.pushInt);
		try {repo.processChanges(StackChanges.multiarrayAndPushReference,2);
			Assert.fail("Mandatory exception was not detected (integer dimensions are missing)");
		} catch (ContentException exc) {
		}

		repo.processChanges(StackChanges.clear);
		
		try {repo.processChanges(StackChanges.multiarrayAndPushReference,1);
			Assert.fail("Mandatory exception was not detected (reference is missing)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(StackChanges.clear);
	}

	@Test
	public void callTest() throws ContentException {
		final int[]					changes = new int[3];		
		final StackChangesCallback	callback = new StackChangesCallback() {
										@Override
										public void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom) {
											changes[0] = deletedFrom;
											changes[1] = insertedFrom;
											changes[2] = changedFrom; 
										}
									};
		final StackAndVarRepo		repo = new StackAndVarRepo(callback);

		repo.pushReference();
		repo.pushInt();
		repo.pushLong();
		repo.pushDouble();
		repo.processChanges(StackChanges.callAndPush,new int[] {CompilerUtils.CLASSTYPE_INT,CompilerUtils.CLASSTYPE_LONG,StackAndVarRepo.SPECIAL_TYPE_TOP,CompilerUtils.CLASSTYPE_DOUBLE,StackAndVarRepo.SPECIAL_TYPE_TOP},5,CompilerUtils.CLASSTYPE_LONG);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepo.SPECIAL_TYPE_TOP,repo.select(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.select(-1));
		
		repo.processChanges(StackChanges.clear);
	}

	@Test
	public void varFrameTest() throws ContentException {
		final int[]					changes = new int[3];		
		final StackChangesCallback	callback = new StackChangesCallback() {
										@Override
										public void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom) {
											changes[0] = deletedFrom;
											changes[1] = insertedFrom;
											changes[2] = changedFrom; 
										}
									};
		final StackAndVarRepo		repo = new StackAndVarRepo(callback);

		repo.startVarFrame();
		repo.addVar(0,CompilerUtils.CLASSTYPE_INT, (short)0, true);
		repo.addVar(1,CompilerUtils.CLASSTYPE_LONG, (short)0, true);
		
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.getVarType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.getVarType(1));
		Assert.assertEquals(3,repo.makeVarSnapshot().getLength());
		
		try{repo.addVar(0,CompilerUtils.CLASSTYPE_INT, (short)0, true);
			Assert.fail("Mandatory exception was not detected (redefinition overlay)");
		} catch (ContentException exc) {
		}
		try{repo.addVar(2,CompilerUtils.CLASSTYPE_DOUBLE, (short)0, true);
			Assert.fail("Mandatory exception was not detected (redefinition overlay)");
		} catch (ContentException exc) {
		}
		
		try{repo.getVarType(2);
			Assert.fail("Mandatory exception was not detected (access to half long/double)");
		} catch (ContentException exc) {
		}
		
		try{repo.getVarType(3);
			Assert.fail("Mandatory exception was not detected (access outside the frame)");
		} catch (ContentException exc) {
		}
		 
		repo.stopVarFrame();

		Assert.assertEquals(0,repo.makeVarSnapshot().getLength());

		try{repo.stopVarFrame();
			Assert.fail("Mandatory exception was not detected (stack exhausted)");
		} catch (IllegalStateException exc) {
		}
	}

	@Test
	public void ensuresTest() throws ContentException {
		final int[]					changes = new int[3];		
		final StackChangesCallback	callback = new StackChangesCallback() {
										@Override
										public void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom) {
											changes[0] = deletedFrom;
											changes[1] = insertedFrom;
											changes[2] = changedFrom; 
										}
									};
		final StackAndVarRepo		repo = new StackAndVarRepo(callback);
		
		for (int index = 0; index <= 20; index++) {	// see private fields of StackVarRepo!
			repo.processChanges(StackChanges.pushInt);
		}
		for (int index = 0; index <= 20; index++) {
			Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.select(0));
			repo.processChanges(StackChanges.pop);
		}
		
		repo.startVarFrame();
		for (int index = 0; index <= 20; index++) {
			repo.addVar(index,CompilerUtils.CLASSTYPE_INT, (short)0, true);
		}
		for (int index = 0; index <= 20; index++) {
			Assert.assertEquals(CompilerUtils.CLASSTYPE_INT, repo.getVarType(index));
		}
		repo.stopVarFrame();

		for (int index = 0; index <= 5; index++) {
			repo.startVarFrame();
			repo.addVar(index,CompilerUtils.CLASSTYPE_INT, (short)0, true);
		}

		for (int index = 5; index >= 0; index--) {
			Assert.assertEquals(CompilerUtils.CLASSTYPE_INT, repo.getVarType(index));
			Assert.assertEquals(index+1,repo.makeVarSnapshot().getLength());
			repo.stopVarFrame();
		}
	}
}
