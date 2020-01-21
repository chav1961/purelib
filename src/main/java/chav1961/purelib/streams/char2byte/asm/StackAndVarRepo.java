package chav1961.purelib.streams.char2byte.asm;

import java.util.Arrays;

import chav1961.purelib.basic.exceptions.ContentException;

class StackAndVarRepo {
	static final int			SPECIAL_TYPE_TOP = -1;
	static final int			SPECIAL_TYPE_UNPREPARED = -2;
	
	private static final int	INITIAL_STACK_SIZE = 16;
	
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
		
		call,
		callAndPush,
		multiarrayAndPushReference,
	}

	@FunctionalInterface
	interface StackChangesCallback {
		void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom);
	}

	@FunctionalInterface
	interface VarChangesCallback {
		void processChanges(final short[][] varContent, final boolean[] changes);
	}
	
	private final StackChangesCallback	stackCallback;
	private final VarChangesCallback	varCallback;
	private final short[][]				varsContent = new short[Short.MAX_VALUE][];
	private final boolean[]				varsChanges = new boolean[Short.MAX_VALUE];
	private int[]						stackContent = new int[INITIAL_STACK_SIZE], stackShadow = new int[INITIAL_STACK_SIZE];
	private int							currentStackTop = -1, maxStackDepth = 0, shadowStackTop;
	private boolean						varChangesDetected = false;
	
	StackAndVarRepo(final StackChangesCallback stackCallback, final VarChangesCallback varCallback) {
		this.stackCallback = stackCallback;
		this.varCallback = varCallback;
	}
	
	void addVar(final short methodPC, final short varDispl, final int varType) {
		varsContent[methodPC] = new short[] {varDispl, (short)varType};
		varsChanges[methodPC] = true;
		varChangesDetected = true;
	}
	
	void changeVar(final short methodPC, final short varDispl, final int newVarType) throws ContentException {
		if (varsContent[methodPC][1] == CompilerUtils.CLASSTYPE_LONG || varsContent[methodPC][1] == CompilerUtils.CLASSTYPE_DOUBLE) {
			if (newVarType != CompilerUtils.CLASSTYPE_LONG  && newVarType != CompilerUtils.CLASSTYPE_DOUBLE) {
				throw new ContentException("Attempt to store non-long or non-double content to long/double var"); 
			}
			else {
				varsContent[methodPC][1] = (short)newVarType;
				varsChanges[methodPC] = true;
				varChangesDetected = true;
			}
		}
		else {
			if (newVarType == CompilerUtils.CLASSTYPE_LONG  && newVarType == CompilerUtils.CLASSTYPE_DOUBLE) {
				varsContent[methodPC][1] = (short)newVarType;
				varsChanges[methodPC] = true;
				varChangesDetected = true;
			}
			else {
				throw new ContentException("Attempt to store long or double content to non-long/non-double var"); 
			}
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

	private void pushAny(final int type) {
		ensureCapacity(1);
		stackContent[++currentStackTop] = type;
	}
	
	void pushInt() {
		ensureCapacity(1);
		stackContent[++currentStackTop] = CompilerUtils.CLASSTYPE_INT;
	}
	
	void pushLong() {
		ensureCapacity(2);
		stackContent[++currentStackTop] = CompilerUtils.CLASSTYPE_LONG;
		stackContent[++currentStackTop] = SPECIAL_TYPE_TOP;
	}
	
	void pushFloat() {
		ensureCapacity(1);
		stackContent[++currentStackTop] = CompilerUtils.CLASSTYPE_FLOAT;
	}
	
	void pushDouble() {
		ensureCapacity(2);
		stackContent[++currentStackTop] = CompilerUtils.CLASSTYPE_DOUBLE;
		stackContent[++currentStackTop] = SPECIAL_TYPE_TOP;
	}
	
	void pushReference() {
		ensureCapacity(1);
		stackContent[++currentStackTop] = CompilerUtils.CLASSTYPE_REFERENCE;
	}
	
	void pushUnprepared() {
		ensureCapacity(1);
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
			throw new ContentException("Illegal command usage: not enought stack content");
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
		if (varChangesDetected) {
			varCallback.processChanges(varsContent,varsChanges);
		}
		varChangesDetected = false;
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
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value");
				}
				break;
			case changeDouble2Int:
				if (select(0) == SPECIAL_TYPE_TOP && select(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(2);
					pushInt();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value");
				}
				break;
			case changeDouble2Long:
				if (select(0) == SPECIAL_TYPE_TOP && select(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(2);
					pushLong();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value");
				}
				break;
			case changeFloat2Double:
				if (select(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop();
					pushDouble();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value");
				}
				break;
			case changeFloat2Int:
				if (select(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop();
					pushInt();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value");
				}
				break;
			case changeFloat2Long:
				if (select(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop();
					pushLong();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value");
				}
				break;
			case changeInt2Double:
				if (select(0) == CompilerUtils.CLASSTYPE_INT || select(0) == CompilerUtils.CLASSTYPE_BYTE || select(0) == CompilerUtils.CLASSTYPE_SHORT || select(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop();
					pushDouble();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value");
				}
				break;
			case changeInt2Float:
				if (select(0) == CompilerUtils.CLASSTYPE_INT || select(0) == CompilerUtils.CLASSTYPE_BYTE || select(0) == CompilerUtils.CLASSTYPE_SHORT || select(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop();
					pushFloat();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value");
				}
				break;
			case changeInt2Long:
				if (select(0) == CompilerUtils.CLASSTYPE_INT || select(0) == CompilerUtils.CLASSTYPE_BYTE || select(0) == CompilerUtils.CLASSTYPE_SHORT || select(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop();
					pushLong();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value");
				}
				break;
			case changeLong2Double:
				if (select(-1) == CompilerUtils.CLASSTYPE_LONG) {
					pop(2);
					pushDouble();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value");
				}
				break;
			case changeLong2Float:
				if (select(0) == SPECIAL_TYPE_TOP && select(-1) == CompilerUtils.CLASSTYPE_LONG) {
					pop(2);
					pushFloat();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value");
				}
				break;
			case changeLong2Int:
				if (select(-1) == CompilerUtils.CLASSTYPE_LONG) {
					pop(2);
					pushInt();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value");
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
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value");
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
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value");
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
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value");
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
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value");
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
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value");
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
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value");
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
					throw new ContentException("Illegal command usage: double/long value at the top of stack");
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
						throw new ContentException("Illegal command usage: multianewarray command contains non-integer dimensions on stack at position ["+(index-signature)+"]");
					}
				}
				if (select(-signature) != CompilerUtils.CLASSTYPE_REFERENCE) {
					throw new ContentException("Illegal command usage: multianewarray command doesn't contain reference type on stack at position [-"+signature+"]");
				}
				pop(signature+1);
				pushReference();
				break;
			case popField	:
				if (signature == CompilerUtils.CLASSTYPE_DOUBLE || signature == CompilerUtils.CLASSTYPE_LONG) {
					if (select(0) == SPECIAL_TYPE_TOP && select(-1) == signature && select(-2) == CompilerUtils.CLASSTYPE_REFERENCE) {
						pop(3);
					}
					else {
						throw new ContentException("Illegal command usage: illegal stack content (double/long and reference awaited)");
					}
				}
				else {
					if (select(0) == signature && select(-1) == CompilerUtils.CLASSTYPE_REFERENCE) {
						pop(2);
					}
					else {
						throw new ContentException("Illegal command usage: illegal stack content (non-double/-nonlong and reference awaited)");
					}
				}
				break;
			case popStatic	:
				if (signature == CompilerUtils.CLASSTYPE_DOUBLE || signature == CompilerUtils.CLASSTYPE_LONG) {
					if (select(0) == SPECIAL_TYPE_TOP && select(-1) == signature) {
						pop(2);
					}
					else {
						throw new ContentException("Illegal command usage: attempt to save long/double from non-long/non-doube stack");
					}
				}
				else {
					if (select(0) == signature) {
						pop();
					}
					else {
						throw new ContentException("Illegal command usage: incompatible types on static fields and stack top");
					}
				}
				break;
			case pushField	:
				if (select(0) != CompilerUtils.CLASSTYPE_REFERENCE) {
					throw new ContentException("Illegal command usage: any reference on the top of stack is missing");
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
			case call			:
				if (retSignature != CompilerUtils.CLASSTYPE_VOID) {
					throw new ContentException("Illegal command usage: method call returns non-void");
				}
				else {
					compareStack(callSignature,signatureSize);
					pop(signatureSize);
				}
				break;
			case callAndPush	:
				if (retSignature == CompilerUtils.CLASSTYPE_VOID) {
					throw new ContentException("Illegal command usage: method call returns void");
				}
				else {
					compareStack(callSignature,signatureSize);
					pop(signatureSize);
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
	
	private void compareStack(final int[] callSignature, final int signatureSize) throws ContentException {
		for (int index = 0; index < signatureSize; index++) {
			if (!typesAreCompatible(select(index-signatureSize+1),callSignature[index])) {
				throw new ContentException("Illegal command usage: uncompatible data types on the stack at position [-"+index+"]");
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

	private void ensureCapacity(final int delta) {
		if (currentStackTop + delta >= stackContent.length) {
			stackContent = Arrays.copyOf(stackContent,2*stackContent.length);
			stackShadow = Arrays.copyOf(stackShadow,2*stackShadow.length);
		}
		maxStackDepth = Math.max(maxStackDepth,currentStackTop + delta);
	}	
}
