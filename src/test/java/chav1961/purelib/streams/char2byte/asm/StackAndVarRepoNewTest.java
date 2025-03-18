package chav1961.purelib.streams.char2byte.asm;


import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.streams.char2byte.asm.StackAndVarRepoNew.TypeDescriptor;

@Tag("OrdinalTestCategory")
public class StackAndVarRepoNewTest {
	private static final short	ZERO = 0;
	
	@Test
	public void basicTest() throws ContentException {
		final StackAndVarRepoNew	repo = new StackAndVarRepoNew(null);
		
		repo.processChanges(ZERO, StackChanges.none);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(ZERO, StackChanges.pushInt);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.pop);
		Assert.assertEquals(0,repo.getCurrentStackDepth());
	
		repo.processChanges(ZERO, StackChanges.pushFloat);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT, repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.pop);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(ZERO, StackChanges.pushReference, CompilerUtils.CLASSTYPE_REFERENCE, (short)1);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE, repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.pop);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(ZERO, StackChanges.pushLong);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));

		repo.processChanges(ZERO, StackChanges.pop2);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(ZERO, StackChanges.pushDouble);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-1));

		try{repo.selectStackItemType(-2);
			Assert.fail("Mandatory exception was not detected (stack exhausted)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.pop2);
		Assert.assertEquals(0,repo.getCurrentStackDepth());
		
		try{repo.processChanges(ZERO, StackChanges.pop);
			Assert.fail("Mandatory exception was not detected (stack exhausted)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushFloat);
		
		repo.processChanges(ZERO, StackChanges.swap);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(-1));
		
		repo.loadStackSnapshot(repo.makeStackSnapshot());		// Test snapshot manipulations
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(-1));
		
		repo.processChanges(ZERO, StackChanges.clear);
		Assert.assertEquals(0,repo.getCurrentStackDepth());
	}

	@Test
	public void duplicatesTest() throws ContentException {
		final StackAndVarRepoNew	repo = new StackAndVarRepoNew(null);

		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.dup);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));
		
		repo.processChanges(ZERO, StackChanges.dup2);
		Assert.assertEquals(4,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(-3));
		
		repo.processChanges(ZERO, StackChanges.pop4);
		
		try {repo.processChanges(ZERO, StackChanges.dup);
			Assert.fail("Mandatory exception was not detected (empty stack)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pushLong);
		try {repo.processChanges(ZERO, StackChanges.dup);
			Assert.fail("Mandatory exception was not detected (attempt to push half of long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.dup2);
		Assert.assertEquals(4,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-3));

		repo.processChanges(ZERO, StackChanges.pop4);

		repo.processChanges(ZERO, StackChanges.pushDouble);
		try {repo.processChanges(ZERO, StackChanges.dup);
			Assert.fail("Mandatory exception was not detected (attempt to push half of double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.dup2);
		Assert.assertEquals(4,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-1));
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-3));

		repo.processChanges(ZERO, StackChanges.pop4);
		
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushFloat);
		
		repo.processChanges(ZERO, StackChanges.dup_x1);
		Assert.assertEquals(3,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(-2));
		
		repo.processChanges(ZERO, StackChanges.pop3);
		
		repo.processChanges(ZERO, StackChanges.pushLong);
		try {repo.processChanges(ZERO, StackChanges.dup_x1);
			Assert.fail("Mandatory exception was not detected (attempt to push half of long)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.pushInt);
		try {repo.processChanges(ZERO, StackChanges.dup_x1);
			Assert.fail("Mandatory exception was not detected (attempt to insert into long)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.pop3);
		
		repo.processChanges(ZERO, StackChanges.pushDouble);
		try {repo.processChanges(ZERO, StackChanges.dup_x1);
			Assert.fail("Mandatory exception was not detected (attempt to push half of double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pushInt);
		try {repo.processChanges(ZERO, StackChanges.dup_x1);
			Assert.fail("Mandatory exception was not detected (attempt to insert into double)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.pop3);
		
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushFloat);
		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		repo.processChanges(ZERO, StackChanges.dup_x2);
		Assert.assertEquals(4,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.selectStackItemType(-3));
		
		repo.processChanges(ZERO, StackChanges.pop4);

		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushLong);
		try {repo.processChanges(ZERO, StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half of long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pushInt);
		try {repo.processChanges(ZERO, StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pop4);

		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushDouble);
		try {repo.processChanges(ZERO, StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half of double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pushInt);
		try {repo.processChanges(ZERO, StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pop4);

		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushFloat);
		repo.processChanges(ZERO, StackChanges.dup2_x1);
		Assert.assertEquals(5,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.selectStackItemType(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(-3));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(-4));
	
		repo.processChanges(ZERO, StackChanges.pop4);
		repo.processChanges(ZERO, StackChanges.pop);
		
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.dup2_x1);
		Assert.assertEquals(5,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(-2));
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(-3));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-4));
	
		repo.processChanges(ZERO, StackChanges.pop4);
		repo.processChanges(ZERO, StackChanges.pop);
		
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushDouble);
		repo.processChanges(ZERO, StackChanges.dup2_x1);
		Assert.assertEquals(5,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(-2));
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(-3));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-4));
	
		repo.processChanges(ZERO, StackChanges.pop4);
		repo.processChanges(ZERO, StackChanges.pop);

		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pushInt);
		try {repo.processChanges(ZERO, StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half of long)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.pop);
		repo.processChanges(ZERO, StackChanges.pushLong);
		try {repo.processChanges(ZERO, StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pop4);
		
		repo.processChanges(ZERO, StackChanges.pushDouble);
		repo.processChanges(ZERO, StackChanges.pushInt);
		try {repo.processChanges(ZERO, StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half of double)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.pop);
		repo.processChanges(ZERO, StackChanges.pushDouble);
		try {repo.processChanges(ZERO, StackChanges.dup_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pop4);

		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushFloat);
		repo.processChanges(ZERO, StackChanges.dup2_x2);
		Assert.assertEquals(6,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.selectStackItemType(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.selectStackItemType(-3));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(-4));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(-5));
	
		repo.processChanges(ZERO, StackChanges.pop4);
		repo.processChanges(ZERO, StackChanges.pop2);

		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.dup2_x2);
		Assert.assertEquals(6,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.selectStackItemType(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.selectStackItemType(-3));
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(-4));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-5));
	
		repo.processChanges(ZERO, StackChanges.pop4);
		repo.processChanges(ZERO, StackChanges.pop2);

		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		repo.processChanges(ZERO, StackChanges.pushDouble);
		repo.processChanges(ZERO, StackChanges.dup2_x2);
		Assert.assertEquals(6,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-1));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.selectStackItemType(-2));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.selectStackItemType(-3));
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(-4));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-5));
	
		repo.processChanges(ZERO, StackChanges.pop4);
		repo.processChanges(ZERO, StackChanges.pop2);

		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		try {repo.processChanges(ZERO, StackChanges.dup2_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pushLong);
		try {repo.processChanges(ZERO, StackChanges.dup2_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pop4);
		repo.processChanges(ZERO, StackChanges.pop2);

		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		repo.processChanges(ZERO, StackChanges.pushDouble);
		repo.processChanges(ZERO, StackChanges.pushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		try {repo.processChanges(ZERO, StackChanges.dup2_x2);
			Assert.fail("Mandatory exception was not detected (attempt to push half double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pushDouble);
		try {repo.processChanges(ZERO, StackChanges.dup2_x2);
			Assert.fail("Mandatory exception was not detected (attempt to insert into double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pop4);
		repo.processChanges(ZERO, StackChanges.pop2);
	}

	@Test
	public void changesTest() throws ContentException {
		final StackAndVarRepoNew	repo = new StackAndVarRepoNew(null);
		
		repo.processChanges(ZERO, StackChanges.pushInt);
		
		try {repo.processChanges(ZERO, StackChanges.changeFloat2Int);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(ZERO, StackChanges.changeLong2Int);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(ZERO, StackChanges.changeDouble2Int);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.changeInt2Float);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(0));
		
		repo.processChanges(ZERO, StackChanges.changeFloat2Int);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.changeInt2Long);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));

		repo.processChanges(ZERO, StackChanges.changeLong2Int);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.changeInt2Double);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-1));

		repo.processChanges(ZERO, StackChanges.changeDouble2Int);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.pop);
		repo.processChanges(ZERO, StackChanges.pushFloat);

		try {repo.processChanges(ZERO, StackChanges.changeInt2Float);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(ZERO, StackChanges.changeLong2Float);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(ZERO, StackChanges.changeDouble2Float);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.changeFloat2Long);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));

		repo.processChanges(ZERO, StackChanges.changeLong2Float);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.changeFloat2Double);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-1));

		repo.processChanges(ZERO, StackChanges.changeDouble2Float);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.pop);
		repo.processChanges(ZERO, StackChanges.pushLong);

		try {repo.processChanges(ZERO, StackChanges.changeInt2Long);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(ZERO, StackChanges.changeFloat2Long);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(ZERO, StackChanges.changeDouble2Long);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		 
		repo.processChanges(ZERO, StackChanges.changeLong2Double);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-1));

		repo.processChanges(ZERO, StackChanges.changeDouble2Long);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));
	}

	@Test
	public void popAndPushTest() throws ContentException {
		final StackAndVarRepoNew	repo = new StackAndVarRepoNew(null);

		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.popAndPushInt);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));
		
		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.popAndPushFloat);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.popAndPushReference, CompilerUtils.CLASSTYPE_REFERENCE, (short)1);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pop2AndPushInt);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pop2AndPushFloat);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_FLOAT,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pop2AndPushReference,CompilerUtils.CLASSTYPE_REFERENCE,(short)1);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pop2AndPushLong);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));

		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pop2AndPushDouble);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-1));

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pop4AndPushInt);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pop4AndPushLong);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));

		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.pop4AndPushDouble);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-1));

		repo.processChanges(ZERO, StackChanges.clear);
	}

	@Test
	public void fieldsAndMultiArrayTest() throws ContentException {
		final StackAndVarRepoNew	repo = new StackAndVarRepoNew(null);

		repo.processChanges(ZERO, StackChanges.pushStatic,CompilerUtils.CLASSTYPE_INT, ZERO);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));
		
		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushStatic,CompilerUtils.CLASSTYPE_LONG, ZERO);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));
		
		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushStatic,CompilerUtils.CLASSTYPE_DOUBLE, ZERO);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_DOUBLE,repo.selectStackItemType(-1));
		
		repo.processChanges(ZERO, StackChanges.clear);

		
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.popStatic,CompilerUtils.CLASSTYPE_INT, ZERO);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.popStatic,CompilerUtils.CLASSTYPE_LONG, ZERO);
		Assert.assertEquals(0,repo.getCurrentStackDepth());
		
		repo.processChanges(ZERO, StackChanges.pushDouble);
		repo.processChanges(ZERO, StackChanges.popStatic,CompilerUtils.CLASSTYPE_DOUBLE, ZERO);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(ZERO, StackChanges.pushInt);
		try {repo.processChanges(ZERO, StackChanges.popStatic,CompilerUtils.CLASSTYPE_LONG, ZERO);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(ZERO, StackChanges.popStatic,CompilerUtils.CLASSTYPE_DOUBLE, ZERO);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushLong);
		try {repo.processChanges(ZERO, StackChanges.popStatic,CompilerUtils.CLASSTYPE_INT, ZERO);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(ZERO, StackChanges.popStatic,CompilerUtils.CLASSTYPE_DOUBLE, ZERO);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushDouble);
		try {repo.processChanges(ZERO, StackChanges.popStatic,CompilerUtils.CLASSTYPE_INT, ZERO);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(ZERO, StackChanges.popStatic,CompilerUtils.CLASSTYPE_LONG, ZERO);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushReference, CompilerUtils.CLASSTYPE_REFERENCE, (short)1);
		repo.processChanges(ZERO, StackChanges.pushField, CompilerUtils.CLASSTYPE_INT, ZERO);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushReference, CompilerUtils.CLASSTYPE_REFERENCE, (short)1);
		repo.processChanges(ZERO, StackChanges.pushField,CompilerUtils.CLASSTYPE_LONG, ZERO);
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushInt);
		try {repo.processChanges(ZERO, StackChanges.pushField,CompilerUtils.CLASSTYPE_INT, ZERO);
			Assert.fail("Mandatory exception was not detected (illegal stack top value)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushReference, CompilerUtils.CLASSTYPE_REFERENCE, (short)1);
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.popField,CompilerUtils.CLASSTYPE_INT, ZERO);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushReference, CompilerUtils.CLASSTYPE_REFERENCE, (short)1);
		repo.processChanges(ZERO, StackChanges.pushLong);
		repo.processChanges(ZERO, StackChanges.popField,CompilerUtils.CLASSTYPE_LONG, ZERO);
		Assert.assertEquals(0,repo.getCurrentStackDepth());

		repo.processChanges(ZERO, StackChanges.pushReference, CompilerUtils.CLASSTYPE_REFERENCE, (short)1);
		repo.processChanges(ZERO, StackChanges.pushInt);
		try {repo.processChanges(ZERO, StackChanges.popField,CompilerUtils.CLASSTYPE_LONG, ZERO);
			Assert.fail("Mandatory exception was not detected (attempt to unload non-long)");
		} catch (ContentException exc) {
		}
		try {repo.processChanges(ZERO, StackChanges.popField,CompilerUtils.CLASSTYPE_DOUBLE, ZERO);
			Assert.fail("Mandatory exception was not detected (attempt to unload non-double)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushReference, CompilerUtils.CLASSTYPE_REFERENCE, (short)1);
		repo.processChanges(ZERO, StackChanges.pushLong);
		try {repo.processChanges(ZERO, StackChanges.popField,CompilerUtils.CLASSTYPE_INT, ZERO);
			Assert.fail("Mandatory exception was not detected (attempt to unload half long)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushReference, CompilerUtils.CLASSTYPE_REFERENCE, (short)1);
		repo.processChanges(ZERO, StackChanges.pushDouble);
		try {repo.processChanges(ZERO, StackChanges.popField,CompilerUtils.CLASSTYPE_INT, ZERO);
			Assert.fail("Mandatory exception was not detected (attempt to unload half double)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushInt);
		try {repo.processChanges(ZERO, StackChanges.popField,CompilerUtils.CLASSTYPE_INT, ZERO);
			Assert.fail("Mandatory exception was not detected (reference near top is missing)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushLong);
		try {repo.processChanges(ZERO, StackChanges.popField,CompilerUtils.CLASSTYPE_LONG, ZERO);
			Assert.fail("Mandatory exception was not detected (reference near top is missing)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.clear);

		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.pushDouble);
		try {repo.processChanges(ZERO, StackChanges.popField,CompilerUtils.CLASSTYPE_DOUBLE, ZERO);
			Assert.fail("Mandatory exception was not detected (reference near top is missing)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushInt);
		repo.processChanges(ZERO, StackChanges.multiarrayAndPushReference, 1, (short)1);
		Assert.assertEquals(1,repo.getCurrentStackDepth());
		Assert.assertEquals(CompilerUtils.CLASSTYPE_REFERENCE, repo.selectStackItemType(0));

		repo.processChanges(ZERO, StackChanges.clear);
		
		repo.processChanges(ZERO, StackChanges.pushReference, CompilerUtils.CLASSTYPE_REFERENCE, (short)1);
		repo.processChanges(ZERO, StackChanges.pushInt);
		try {repo.processChanges(ZERO, StackChanges.multiarrayAndPushReference, 2, (short)1);
			Assert.fail("Mandatory exception was not detected (integer dimensions are missing)");
		} catch (ContentException exc) {
		}

		repo.processChanges(ZERO, StackChanges.clear);
		
		try {repo.processChanges(ZERO, StackChanges.multiarrayAndPushReference, 1, (short)1);
			Assert.fail("Mandatory exception was not detected (reference is missing)");
		} catch (ContentException exc) {
		}
		
		repo.processChanges(ZERO, StackChanges.clear);
	}

	@Test
	public void callTest() throws ContentException {
		final StackAndVarRepoNew	repo = new StackAndVarRepoNew(null);

		repo.pushReference(ZERO, (short)1);
		repo.pushInt(ZERO);
		repo.pushLong(ZERO);
		repo.pushDouble(ZERO);
		repo.processChanges(ZERO, StackChanges.callAndPush,
				new TypeDescriptor[] {
						new TypeDescriptor(CompilerUtils.CLASSTYPE_INT),
						new TypeDescriptor(CompilerUtils.CLASSTYPE_LONG),
						new TypeDescriptor(StackAndVarRepoNew.SPECIAL_TYPE_TOP),
						new TypeDescriptor(CompilerUtils.CLASSTYPE_DOUBLE),
						new TypeDescriptor(StackAndVarRepoNew.SPECIAL_TYPE_TOP)
				}, 5, new TypeDescriptor(CompilerUtils.CLASSTYPE_LONG));
		Assert.assertEquals(2,repo.getCurrentStackDepth());
		Assert.assertEquals(StackAndVarRepoNew.SPECIAL_TYPE_TOP,repo.selectStackItemType(0));
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.selectStackItemType(-1));
		
		repo.processChanges(ZERO, StackChanges.clear);
	}

	@Test
	public void varFrameTest() throws ContentException {
		final StackAndVarRepoNew	repo = new StackAndVarRepoNew(null);

		repo.pushVarFrame(ZERO);
		repo.addVar(CompilerUtils.CLASSTYPE_INT, ZERO, false);
		repo.addVar(CompilerUtils.CLASSTYPE_LONG, ZERO, false);
		
		Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.getVarType(0).dataType);
		Assert.assertEquals(CompilerUtils.CLASSTYPE_LONG,repo.getVarType(1).dataType);
		Assert.assertEquals(3,repo.makeVarSnapshot().getLength());
		
		try{repo.getVarType(3);
			Assert.fail("Mandatory exception was not detected (access outside the frame)");
		} catch (ContentException exc) {
		}
		 
		repo.popVarFrame();

		Assert.assertEquals(0,repo.makeVarSnapshot().getLength());

		try{repo.popVarFrame();
			Assert.fail("Mandatory exception was not detected (stack exhausted)");
		} catch (IllegalStateException exc) {
		}
	}

	@Test
	public void ensuresTest() throws ContentException {
		final int[]					changes = new int[3];		
		final StackAndVarRepoNew	repo = new StackAndVarRepoNew(null);
		
		for (int index = 0; index <= 20; index++) {	// see private fields of StackVarRepo!
			repo.processChanges(ZERO, StackChanges.pushInt);
		}
		for (int index = 0; index <= 20; index++) {
			Assert.assertEquals(CompilerUtils.CLASSTYPE_INT,repo.selectStackItemType(0));
			repo.processChanges(ZERO, StackChanges.pop);
		}
		
		repo.pushVarFrame(ZERO);
		for (int index = 0; index <= 20; index++) {
			repo.addVar(CompilerUtils.CLASSTYPE_INT, ZERO, false);
		}
		for (int index = 0; index <= 20; index++) {
			Assert.assertEquals(CompilerUtils.CLASSTYPE_INT, repo.getVarType(index).dataType);
		}
		repo.popVarFrame();

		for (int index = 0; index <= 5; index++) {
			repo.pushVarFrame(ZERO);
			repo.addVar(CompilerUtils.CLASSTYPE_INT, ZERO, false);
		}

		for (int index = 5; index >= 0; index--) {
			Assert.assertEquals(CompilerUtils.CLASSTYPE_INT, repo.getVarType(index).dataType);
			Assert.assertEquals(index+1, repo.makeVarSnapshot().getLength());
			repo.popVarFrame();
		}
	}
}
