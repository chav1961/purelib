package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;
import java.util.Arrays;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.cdb.CompilerUtils;

class StackAndVarRepo {
	static final int			SPECIAL_TYPE_TOP = -1;
	static final int			SPECIAL_TYPE_UNPREPARED = -2;
	static StackSnapshot		CATCH_SNAPSHOT = new StackSnapshot(new int[]{CompilerUtils.CLASSTYPE_REFERENCE},1);  
	
	private static final int	INITIAL_STACK_SIZE = 16;
	private static final int	INITIAL_VARFRAME_SIZE = 16;
	private static final int	INITIAL_VARFRAME_STACK_SIZE = 4;
	
	enum StackChanges {
		none,
		clear,
		swap,	
		
		pop,
		pop2,
		pop3,
		pop4,
		pushInt,
		pushLong,
		pushDouble,
		pushFloat,
		pushReference,
		pushUnprepared,
		
		dup,
		dup_x1,
		dup_x2,
		dup2,
		dup2_x1,
		dup2_x2,
		
		changeDouble2Float,
		changeDouble2Int,
		changeDouble2Long,
		changeFloat2Double,
		changeFloat2Int,
		changeFloat2Long,
		changeLong2Double,
		changeLong2Float,
		changeLong2Int,
		changeInt2Double,
		changeInt2Float,
		changeInt2Long,
		
		popAndPushFloat,
		popAndPushInt,
		popAndPushReference,
		pop2AndPushInt,
		pop2AndPushDouble,
		pop2AndPushReference,
		pop2AndPushFloat,
		pop2AndPushLong,
		pop4AndPushDouble,
		pop4AndPushInt,
		pop4AndPushLong,
		
		pushField,
		pushStatic,
		popField,
		popStatic,
		
		callStaticAndPush,
		callAndPush,
		multiarrayAndPushReference,
	}

	@FunctionalInterface
	interface StackChangesCallback {
		void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom);
	}

	private final StackChangesCallback	stackCallback;
	private int[]						stackContent = new int[INITIAL_STACK_SIZE], stackShadow = new int[INITIAL_STACK_SIZE];
	private int							currentStackTop = -1, maxStackDepth = 0, shadowStackTop;
	private int[]						varFrames = new int[INITIAL_VARFRAME_STACK_SIZE];
	private int[]						varContent = new int[INITIAL_VARFRAME_SIZE];
	private int							lastVarFrameDispl = -1, lastVarFrameStackDispl = -1;
	private short						previousStackMapDispl = 0;
	
	StackAndVarRepo(final StackChangesCallback stackCallback) {
		this.stackCallback = stackCallback;
		Arrays.fill(varContent,SPECIAL_TYPE_UNPREPARED);
	}
	
	void startVarFrame() {
		ensureVarFrameStackCapacity();
		varFrames[++lastVarFrameStackDispl] = lastVarFrameDispl;
	}
	
	void stopVarFrame() {
		if (lastVarFrameStackDispl < 0) {
			throw new IllegalStateException("Var frame stack exhausted");
		}
		else {
			lastVarFrameDispl = varFrames[lastVarFrameStackDispl--];
			Arrays.fill(varContent,Math.max(lastVarFrameDispl+1,0),varContent.length,SPECIAL_TYPE_UNPREPARED);
		}
	}
	
	void addVar(final int varDispl, final int varType) throws ContentException {
		if (varType == CompilerUtils.CLASSTYPE_DOUBLE || varType == CompilerUtils.CLASSTYPE_LONG) {
			ensureVarFrameCapacity(varDispl+1);
			if (varContent[varDispl] == SPECIAL_TYPE_UNPREPARED && varContent[varDispl+1] == SPECIAL_TYPE_UNPREPARED) {
				varContent[varDispl] = varType;
				varContent[varDispl+1] = SPECIAL_TYPE_TOP;
			}
			else {
				throw new ContentException("variable overlay redefinition were detected at frame location ["+varDispl+"]");
			}
		}
		else {
			ensureVarFrameCapacity(varDispl);
			if (varContent[varDispl] == SPECIAL_TYPE_UNPREPARED) {
				varContent[varDispl] = varType;
			}
			else {
				throw new ContentException("variable overlay redefinition were detected at frame location ["+varDispl+"]");
			}
		}
	}

	int getVarType(final int varDispl) throws ContentException {
		final int	result = varContent[varDispl];
		
		if (result == SPECIAL_TYPE_TOP) {
			throw new ContentException("Attempt to access half double/long of local variable at frame location ["+varDispl+"]");
		}
		else if (result == SPECIAL_TYPE_UNPREPARED) {
			throw new ContentException("Attempt to access local variable outside the frame: ["+varDispl+"]");
		}
		else {
			return result;
		}
	}
	
	void begin() {
		if (currentStackTop >= 0) {
			System.arraycopy(stackContent,0,stackShadow,0,stackContent.length);
		}
		shadowStackTop = currentStackTop;
	}

	void clear() throws ContentException {
		while (currentStackTop >= 0) {
			pop();
		}
	}
	
	void pop() throws ContentException {
		if (currentStackTop < 0) {
			throw new ContentException("Illegal command usage: stack exhausted");
		}
		else {
			currentStackTop--;
		}
	}

	void pushInt() {
		ensureStackCapacity(1);
		stackContent[++currentStackTop] = CompilerUtils.CLASSTYPE_INT;
	}
	
	void pushLong() {
		ensureStackCapacity(2);
		stackContent[++currentStackTop] = CompilerUtils.CLASSTYPE_LONG;
		stackContent[++currentStackTop] = SPECIAL_TYPE_TOP;
	}
	
	void pushFloat() {
		ensureStackCapacity(1);
		stackContent[++currentStackTop] = CompilerUtils.CLASSTYPE_FLOAT;
	}
	
	void pushDouble() {
		ensureStackCapacity(2);
		stackContent[++currentStackTop] = CompilerUtils.CLASSTYPE_DOUBLE;
		stackContent[++currentStackTop] = SPECIAL_TYPE_TOP;
	}
	
	void pushReference() {
		ensureStackCapacity(1);
		stackContent[++currentStackTop] = CompilerUtils.CLASSTYPE_REFERENCE;
	}
	
	void pushUnprepared() {
		ensureStackCapacity(1);
		stackContent[++currentStackTop] = SPECIAL_TYPE_UNPREPARED;
	}
	
	void pop(final int size) throws ContentException {
		for (int index = 0; index < size; index++) {
			pop();
		}
	}
	
	void push(final int type) {
		switch (type) {
			case CompilerUtils.CLASSTYPE_REFERENCE	: pushReference();	break;
			case CompilerUtils.CLASSTYPE_BYTE		: pushInt();		break;
			case CompilerUtils.CLASSTYPE_SHORT		: pushInt(); 		break;
			case CompilerUtils.CLASSTYPE_CHAR 		: pushInt();		break;
			case CompilerUtils.CLASSTYPE_INT		: pushInt();		break;
			case CompilerUtils.CLASSTYPE_FLOAT		: pushFloat();		break;	
			case CompilerUtils.CLASSTYPE_BOOLEAN	: pushInt();		break;
			case CompilerUtils.CLASSTYPE_LONG 		: pushLong();		break;
			case CompilerUtils.CLASSTYPE_DOUBLE		: pushDouble();		break;
			case SPECIAL_TYPE_UNPREPARED			: pushUnprepared();	break;
			default :
				throw new UnsupportedOperationException(); 
		}
	}
	
	int select(final int fromTop) throws ContentException {
		if (currentStackTop + fromTop > currentStackTop || currentStackTop + fromTop < 0) {
			throw new ContentException("Illegal command usage: not enought stack content. Stack content is "+prepareStackContent());
		}
		else {
			return stackContent[currentStackTop + fromTop];
		}
	}
	
	void commit() {
		final int	maxIndex = Math.min(currentStackTop,shadowStackTop);
		int			index;
		
		for (index = 0; index < maxIndex; index++) {
			if (!typesAreCompatible(stackContent[index],stackShadow[index])) {
				break;
			}
		}
		int insertedFrom = -1, deletedFrom = -1, changedFrom = -1;
		
		if (index < maxIndex) {
			changedFrom = index;
		}
		if (currentStackTop > shadowStackTop) {
			insertedFrom = currentStackTop - shadowStackTop;
		}
		else if (currentStackTop < shadowStackTop) {
			deletedFrom = shadowStackTop - currentStackTop;
		}
		stackCallback.processChanges(stackContent,deletedFrom,insertedFrom,changedFrom);
	}

	void processChanges(final StackChanges changes) throws ContentException {
		begin();
		switch (changes) {
			case changeDouble2Float:
				if (select(0) == SPECIAL_TYPE_TOP && select(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(2);
					pushFloat();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value. Stack content is "+prepareStackContent());
				}
				break;
			case changeDouble2Int:
				if (select(0) == SPECIAL_TYPE_TOP && select(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(2);
					pushInt();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value. Stack content is "+prepareStackContent());
				}
				break;
			case changeDouble2Long:
				if (select(0) == SPECIAL_TYPE_TOP && select(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(2);
					pushLong();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value. Stack content is "+prepareStackContent());
				}
				break;
			case changeFloat2Double:
				if (select(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop();
					pushDouble();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeFloat2Int:
				if (select(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop();
					pushInt();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeFloat2Long:
				if (select(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop();
					pushLong();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeInt2Double:
				if (select(0) == CompilerUtils.CLASSTYPE_INT || select(0) == CompilerUtils.CLASSTYPE_BYTE || select(0) == CompilerUtils.CLASSTYPE_SHORT || select(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop();
					pushDouble();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeInt2Float:
				if (select(0) == CompilerUtils.CLASSTYPE_INT || select(0) == CompilerUtils.CLASSTYPE_BYTE || select(0) == CompilerUtils.CLASSTYPE_SHORT || select(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop();
					pushFloat();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeInt2Long:
				if (select(0) == CompilerUtils.CLASSTYPE_INT || select(0) == CompilerUtils.CLASSTYPE_BYTE || select(0) == CompilerUtils.CLASSTYPE_SHORT || select(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop();
					pushLong();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeLong2Double:
				if (select(-1) == CompilerUtils.CLASSTYPE_LONG) {
					pop(2);
					pushDouble();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value. Stack content is "+prepareStackContent());
				}
				break;
			case changeLong2Float:
				if (select(0) == SPECIAL_TYPE_TOP && select(-1) == CompilerUtils.CLASSTYPE_LONG) {
					pop(2);
					pushFloat();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value. Stack content is "+prepareStackContent());
				}
				break;
			case changeLong2Int:
				if (select(-1) == CompilerUtils.CLASSTYPE_LONG) {
					pop(2);
					pushInt();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value. Stack content is "+prepareStackContent());
				}
				break;
			case clear:
				clear();
				break;
			case dup:
				final int	dup = select(0);
				
				if (dup != SPECIAL_TYPE_TOP) {
					push(dup);
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup2:
				final int	dup2V1 = select(0), dup2V2 = select(-1);
				
				if (dup2V2 != SPECIAL_TYPE_TOP) {
					if (dup2V2 == CompilerUtils.CLASSTYPE_LONG || dup2V2 == CompilerUtils.CLASSTYPE_DOUBLE) {
						push(dup2V2);
					}
					else {
						push(dup2V2);
						push(dup2V1);
					}
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup2_x1:
				final int	dup2X1V1 = select(0), dup2X1V2 = select(-1), dup2X1V3 = select(-2);
				
				if (dup2X1V3 != SPECIAL_TYPE_TOP) {
					pop(3);
					if (dup2X1V2 == CompilerUtils.CLASSTYPE_LONG || dup2X1V2 == CompilerUtils.CLASSTYPE_DOUBLE) {
						push(dup2X1V2);
					}
					else {
						push(dup2X1V2);
						push(dup2X1V1);
					}
					push(dup2X1V3);
					if (dup2X1V2 == CompilerUtils.CLASSTYPE_LONG || dup2X1V2 == CompilerUtils.CLASSTYPE_DOUBLE) {
						push(dup2X1V2);
					}
					else {
						push(dup2X1V2);
						push(dup2X1V1);
					}
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup2_x2:
				final int	dup2X2V1 = select(0), dup2X2V2 = select(-1), dup2X2V3 = select(-2), dup2X2V4 = select(-3);
				
				if (dup2X2V4 != SPECIAL_TYPE_TOP && dup2X2V3 != SPECIAL_TYPE_TOP && dup2X2V2 != SPECIAL_TYPE_TOP) {
					pop(4);
					if (dup2X2V2 == CompilerUtils.CLASSTYPE_LONG || dup2X2V2 == CompilerUtils.CLASSTYPE_DOUBLE) {
						push(dup2X2V2);
					}
					else {
						push(dup2X2V2);
						push(dup2X2V1);
					}
					push(dup2X2V4);
					push(dup2X2V3);
					if (dup2X2V2 == CompilerUtils.CLASSTYPE_LONG || dup2X2V2 == CompilerUtils.CLASSTYPE_DOUBLE) {
						push(dup2X2V2);
					}
					else {
						push(dup2X2V2);
						push(dup2X2V1);
					}
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup_x1	:
				final int	dupX1V1 = select(0), dupX1V2 = select(-1);
				
				if (dupX1V1 != SPECIAL_TYPE_TOP && dupX1V2 != SPECIAL_TYPE_TOP) {
					pop(2);
					push(dupX1V1);
					push(dupX1V2);
					push(dupX1V1);
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup_x2	:
				final int	dupX2V1 = select(0), dupX2V2 = select(-1), dupX2V3 = select(-2);
				
				if (dupX2V3 != SPECIAL_TYPE_TOP && dupX2V2 != SPECIAL_TYPE_TOP && dupX2V1 != SPECIAL_TYPE_TOP) {
					pop(3);
					push(dupX2V1);
					push(dupX2V3);
					push(dupX2V2);
					push(dupX2V1);
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case none:
				break;
			case pop:
				pop();
				break;
			case pop2:
				pop(2);
				break;
			case pop2AndPushDouble:
				pop(2);
				pushDouble();
				break;
			case pop2AndPushFloat:
				pop(2);
				pushFloat();
				break;
			case pop2AndPushInt:
				pop(2);
				pushInt();
				break;
			case pop2AndPushLong:
				pop(2);
				pushLong();
				break;
			case pop2AndPushReference:
				pop(2);
				pushReference();
				break;
			case pop3:
				pop(3);
				break;
			case pop4:
				pop(4);
				break;
			case pop4AndPushDouble:
				pop(4);
				pushDouble();
				break;
			case pop4AndPushInt:
				pop(4);
				pushInt();
				break;
			case pop4AndPushLong:
				pop(4);
				pushLong();
				break;
			case popAndPushInt:
				pop();
				pushInt();
				break;
			case popAndPushFloat:
				pop();
				pushFloat();
				break;
			case popAndPushReference:
				pop();
				pushReference();
				break;
			case pushDouble:
				pushDouble();
				break;
			case pushFloat:
				pushFloat();
				break;
			case pushInt:
				pushInt();
				break;
			case pushLong:
				pushLong();
				break;
			case pushReference:
				pushReference();
				break;
			case pushUnprepared:
				pushUnprepared();
				break;
			case swap :
				final	int val1 = select(0), val2 = select(-1);
				
				if (val2 == CompilerUtils.CLASSTYPE_DOUBLE || val2 == CompilerUtils.CLASSTYPE_LONG) {
					throw new ContentException("Illegal command usage: double/long value at the top of stack. Stack content is "+prepareStackContent());
				}
				else {
					pop(2);
					push(val1);
					push(val2);
				}
				break;
			default:
				throw new UnsupportedOperationException(); 
		}
		commit();
	}

	void processChanges(final StackChanges changes, final int signature) throws ContentException {
		begin();
		switch (changes) {
			case multiarrayAndPushReference	:
				for (int index = 0; index < signature; index++) {
					if (select(-index) != CompilerUtils.CLASSTYPE_INT) {
						throw new ContentException("Illegal command usage: multianewarray command contains non-integer dimensions on stack at position ["+(index-signature)+"]. Stack content is "+prepareStackContent());
					}
				}
				pop(signature);
				pushReference();
				break;
			case popField	:
				if (signature == CompilerUtils.CLASSTYPE_DOUBLE || signature == CompilerUtils.CLASSTYPE_LONG) {
					if (select(0) == SPECIAL_TYPE_TOP && typesAreCompatible(select(-1),signature) && select(-2) == CompilerUtils.CLASSTYPE_REFERENCE) {
						pop(3);
					}
					else {
						throw new ContentException("Illegal command usage: illegal stack content (double/long and reference awaited). Stack content is "+prepareStackContent());
					}
				}
				else {
					if (typesAreCompatible(select(0),signature) && select(-1) == CompilerUtils.CLASSTYPE_REFERENCE) {
						pop(2);
					}
					else {
						throw new ContentException("Illegal command usage: illegal stack content (non-double/-nonlong and reference awaited). Stack content is "+prepareStackContent());
					}
				}
				break;
			case popStatic	:
				if (signature == CompilerUtils.CLASSTYPE_DOUBLE || signature == CompilerUtils.CLASSTYPE_LONG) {
					if (select(0) == SPECIAL_TYPE_TOP && typesAreCompatible(select(-1),signature)) {
						pop(2);
					}
					else {
						throw new ContentException("Illegal command usage: attempt to save long/double from non-long/non-doube stack. Stack content is "+prepareStackContent());
					}
				}
				else {
					if (typesAreCompatible(select(0),signature)) {
						pop();
					}
					else {
						throw new ContentException("Illegal command usage: incompatible types on static fields and stack top. Stack content is "+prepareStackContent());
					}
				}
				break;
			case pushField	:
				if (select(0) != CompilerUtils.CLASSTYPE_REFERENCE) {
					throw new ContentException("Illegal command usage: any reference on the top of stack is missing. Stack content is "+prepareStackContent());
				}
				else {
					pop();
					push(signature);
				}
				break;
			case pushStatic	:
				push(signature);
				break;
			default:
				throw new UnsupportedOperationException(); 
		}
		commit();
	}

	void processChanges(final StackChanges changes, final int[] callSignature, final int signatureSize, final int retSignature) throws ContentException {
		begin();
		switch (changes) {
			case callStaticAndPush	:
				if (retSignature == CompilerUtils.CLASSTYPE_VOID) {
					compareStack(callSignature,signatureSize);
					pop(signatureSize);
				}
				else {
					compareStack(callSignature,signatureSize);
					pop(signatureSize);
					push(retSignature);
				}
				break;
			case callAndPush	:
				if (retSignature == CompilerUtils.CLASSTYPE_VOID) {
					compareStack(callSignature,signatureSize);
					if (select(-signatureSize) != CompilerUtils.CLASSTYPE_REFERENCE) {
						throw new ContentException("Illegal command usage: non-static invocation requires instance refrerence on the stack. Stack content is "+prepareStackContent());
					}
					else {
						pop(signatureSize+1);
					}
				}
				else {
					compareStack(callSignature,signatureSize);
					if (select(-signatureSize) != CompilerUtils.CLASSTYPE_REFERENCE) {
						throw new ContentException("Illegal command usage: non-static invocation requires instance refrerence on the stack. Stack content is "+prepareStackContent());
					}
					else {
						pop(signatureSize+1);
					}
					push(retSignature);
				}
				break;
			default:
				throw new UnsupportedOperationException(); 
		}
		commit();
	}

	int getCurrentStackDepth() {
		return currentStackTop+1;
	}
	
	int getMaxStackDepth() {
		return maxStackDepth;
	}

	StackSnapshot makeStackSnapshot() {
		return new StackSnapshot(stackContent,currentStackTop); 
	}

	VarSnapshot makeVarSnapshot() {
		return new VarSnapshot(varContent,lastVarFrameDispl); 
	}
	
	StackMapRecord createStackMapRecord(final short displ) {
		final StackMapRecord	result = new StackMapRecord((short)(displ-previousStackMapDispl),makeStackSnapshot(),makeVarSnapshot());
		
		previousStackMapDispl = (short)(displ+1);
		return result;
	}
	
	void loadStackSnapshot(final StackSnapshot snapshot) {
		ensureStackCapacity(Math.max(0,snapshot.content.length-currentStackTop));
		System.arraycopy(snapshot.content,0,stackContent,0,snapshot.content.length);
		currentStackTop = snapshot.content.length - 1;
		begin();
	}
	
	boolean compareStack(final int[] content) throws ContentException {
		for (int index = 0, signatureSize = content.length; index < signatureSize; index++) {
			if (!typesAreCompatible(select(index-signatureSize+1),content[index])) {
				return false;
			}
		}
		return true;
	}
	
	private void compareStack(final int[] callSignature, final int signatureSize) throws ContentException {
		for (int index = 0; index < signatureSize; index++) {
			if (!typesAreCompatible(select(index-signatureSize+1),callSignature[index])) {
				throw new ContentException("Illegal command usage: uncompatible data types on the stack at position [-"+index+"]. "+prepareStackMismatchMessage(stackContent,getCurrentStackDepth(),callSignature,signatureSize));
			}
		}
	}

	private boolean typesAreCompatible(final int fromStack, final int fromSignature) {
		if (fromStack != fromSignature) {
			return fromStack == CompilerUtils.CLASSTYPE_INT && (fromSignature == CompilerUtils.CLASSTYPE_BYTE || fromSignature == CompilerUtils.CLASSTYPE_SHORT || fromSignature == CompilerUtils.CLASSTYPE_CHAR || fromSignature == CompilerUtils.CLASSTYPE_INT || fromSignature == CompilerUtils.CLASSTYPE_BOOLEAN);
		}
		else {
			return true;
		}
	}

	private String prepareStackContent() {
		return new StackSnapshot(stackContent,getCurrentStackDepth()).toString();
	}
	
	private String prepareStackMismatchMessage(final int[] stackContent, final int stackSize, final int[] awaitedContent, final int awaitedContentSize) {
		final StackSnapshot	stack = new StackSnapshot(stackContent,stackSize), awaited = new StackSnapshot(awaitedContent,awaitedContentSize); 
		
		return "Current stack state is: "+stack.toString()+", awaited top of stace is: "+awaited.toString();
	}

	private void ensureStackCapacity(final int delta) {
		if (currentStackTop + delta >= stackContent.length) {
			stackContent = Arrays.copyOf(stackContent,2*stackContent.length);
			stackShadow = Arrays.copyOf(stackShadow,2*stackShadow.length);
		}
		maxStackDepth = Math.max(maxStackDepth,currentStackTop + delta);
	}

	private void ensureVarFrameStackCapacity() {
		if (lastVarFrameStackDispl >= varFrames.length-1) {
			varFrames = Arrays.copyOf(varFrames,2*varFrames.length);
		}
	}

	private void ensureVarFrameCapacity(final int varLocation) {
		lastVarFrameDispl = Math.max(lastVarFrameDispl,varLocation);
		if (lastVarFrameDispl >= varContent.length-1) {
			varContent = Arrays.copyOf(varContent,2*varContent.length);
			Arrays.fill(varContent,lastVarFrameDispl,varContent.length,SPECIAL_TYPE_UNPREPARED);
		}
	}
	
	static class StackSnapshot {
		private final int[]	content;
		
		StackSnapshot(final int[] stackContent, final int stackSize) {
			this.content = Arrays.copyOf(stackContent,stackSize+1);
		}

		int getLength() {
			return content.length;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(content);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			StackSnapshot other = (StackSnapshot) obj;
			if (!Arrays.equals(content, other.content)) return false;
			return true;
		}

		@Override
		public String toString() {
			final StringBuilder	sb = new StringBuilder();
			String				prefix= "";
			
			sb.append("StackSnapshot [");
			
			for (int val : content) {
				sb.append(prefix);
				switch (val) {
					case CompilerUtils.CLASSTYPE_REFERENCE	: sb.append("ref"); break;
					case CompilerUtils.CLASSTYPE_BYTE		: sb.append("byte"); break;
					case CompilerUtils.CLASSTYPE_SHORT		: sb.append("short"); break;
					case CompilerUtils.CLASSTYPE_CHAR		: sb.append("char"); break;	
					case CompilerUtils.CLASSTYPE_INT		: sb.append("int"); break;	
					case CompilerUtils.CLASSTYPE_LONG		: sb.append("long"); break;
					case CompilerUtils.CLASSTYPE_FLOAT		: sb.append("float"); break;
					case CompilerUtils.CLASSTYPE_DOUBLE		: sb.append("double"); break;
					case CompilerUtils.CLASSTYPE_BOOLEAN	: sb.append("boolean"); break;
					case CompilerUtils.CLASSTYPE_VOID		: sb.append("void"); break;
					case SPECIAL_TYPE_TOP 					: sb.append("top"); break;
					case SPECIAL_TYPE_UNPREPARED			: sb.append("unprepared"); break;
					default 								: sb.append(val); break;
				}
				prefix = ",";
			}
			
			return  sb.append("]").toString();
		}
	}
	
	static class VarSnapshot {
		private final int[]	content;
		
		VarSnapshot(final int[] varContent, final int stackSize) {
			this.content = Arrays.copyOf(varContent,stackSize+1);
		}

		int getLength() {
			return content.length;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(content);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			VarSnapshot other = (VarSnapshot) obj;
			if (!Arrays.equals(content, other.content)) return false;
			return true;
		}

		@Override
		public String toString() {
			final StringBuilder	sb = new StringBuilder();
			String				prefix= "";
			
			sb.append("VarSnapshot [");
			
			for (int val : content) {
				sb.append(prefix);
				switch (val) {
					case CompilerUtils.CLASSTYPE_REFERENCE	: sb.append("ref"); break;
					case CompilerUtils.CLASSTYPE_BYTE		: sb.append("byte"); break;
					case CompilerUtils.CLASSTYPE_SHORT		: sb.append("short"); break;
					case CompilerUtils.CLASSTYPE_CHAR		: sb.append("char"); break;	
					case CompilerUtils.CLASSTYPE_INT		: sb.append("int"); break;	
					case CompilerUtils.CLASSTYPE_LONG		: sb.append("long"); break;
					case CompilerUtils.CLASSTYPE_FLOAT		: sb.append("float"); break;
					case CompilerUtils.CLASSTYPE_DOUBLE		: sb.append("double"); break;
					case CompilerUtils.CLASSTYPE_BOOLEAN	: sb.append("boolean"); break;
					case CompilerUtils.CLASSTYPE_VOID		: sb.append("void"); break;
					case SPECIAL_TYPE_TOP 					: sb.append("top"); break;
					case SPECIAL_TYPE_UNPREPARED			: sb.append("unprepared"); break;
					default 								: sb.append(val); break;
				}
				prefix = ",";
			}
			
			return  sb.append("]").toString();
		}
	}
	
	static class StackMapRecord {
		private final short			displ;
		private final StackSnapshot	stack;
		private final VarSnapshot	vars;
		
		public StackMapRecord(final short displ, final StackSnapshot stack, final VarSnapshot vars) {
			super();
			this.displ = displ;
			this.stack = stack;
			this.vars = vars;
		}
		
		public int getRecordSize() {
			return    1	// 0xFF byte size 
					+ 2 // displ size
					+ 2 // stack content length size
					+ calculateTypeArraySize(stack.content)	// stack content
					+ 2	// var frame content length size
					+ calculateTypeArraySize(vars.content)	// var frame content
					;
		}
		
		public void write(final InOutGrowableByteArray os) throws IOException {
			os.writeByte(0xFF);
			os.writeShort(displ);
			os.writeShort(calculateTypeArraySize(vars.content));
			for (int item : vars.content) {
				os.writeByte(toStackFrameTypes(item));
				if (item == CompilerUtils.CLASSTYPE_REFERENCE) {
					os.writeShort(0);
				}
			}
			os.writeShort(calculateTypeArraySize(stack.content));
			for (int item : stack.content) {
				os.writeByte(toStackFrameTypes(item));	
				if (item == CompilerUtils.CLASSTYPE_REFERENCE) {
					os.writeShort(0);
				}
			}
		}
		
		@Override
		public String toString() {
			return "StackMapRecord [displ=" + displ + ", stack=" + stack + ", vars=" + vars + "]";
		}
		
		private static int toStackFrameTypes(final int type) {
			switch (type) {
				case CompilerUtils.CLASSTYPE_REFERENCE	: 
					return 7;
				case CompilerUtils.CLASSTYPE_BOOLEAN : case CompilerUtils.CLASSTYPE_BYTE : case CompilerUtils.CLASSTYPE_SHORT :
				case CompilerUtils.CLASSTYPE_CHAR : case CompilerUtils.CLASSTYPE_INT :
					return 1;	
				case CompilerUtils.CLASSTYPE_LONG		: 
					return 4;	
				case CompilerUtils.CLASSTYPE_FLOAT		: 
					return 2;	
				case CompilerUtils.CLASSTYPE_DOUBLE		: 
					return 3;	
				case SPECIAL_TYPE_TOP					: 
					return 0;
				default : throw new UnsupportedOperationException();
			}
		}
		
		private static int calculateTypeArraySize(final int[] array) {
			int	result = array.length;
			
			for (int item : array) {
				if (item == CompilerUtils.CLASSTYPE_REFERENCE) {
					result += 2;
				}
			}
			return result;
		}
	}
}
