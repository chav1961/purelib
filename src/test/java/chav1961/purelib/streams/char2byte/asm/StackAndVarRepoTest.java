package chav1961.purelib.streams.char2byte.asm;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.streams.char2byte.asm.StackAndVarRepo.StackChangesCallback;
import chav1961.purelib.streams.char2byte.asm.StackAndVarRepo.VarChangesCallback;

public class StackAndVarRepoTest {
	@Test
	public void basicTest() {
		final StackChangesCallback	callback = new StackChangesCallback() {
										@Override
										public void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom) {
											// TODO Auto-generated method stub
										}
									};
		final VarChangesCallback	varCallback = new VarChangesCallback() {
										@Override
										public void processChanges(short[][] varContent, boolean[] changes) {
											// TODO Auto-generated method stub
										}
									};
		final StackAndVarRepo		repo = new StackAndVarRepo(callback,varCallback); 
	}
}
