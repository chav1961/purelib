package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;

class StackRepo {
	private static final int	INITIAL_STACK_SIZE = 16;
	
	enum StackChanges {
		pop2AndPushReference,
		pushReference,
		popAndPushReference,
		clear,
		pop,
		pop2AndPushInt,
		pop3,
		changeRefType,
		changeDouble2Float,
		changeDouble2Int,
		changeDouble2Long,
		pop4AndPushDouble,
		pop2AndPushDouble,
		pop4,
		pop4AndPushInt,
		pushDouble,
		pop2,
		dup,
		dup_x1,
		dup_x2,
		dup2,
		dup2_x1,
		dup2_x2,
		changeFloat2Double,
		changeFloat2Int,
		changeFloat2Long,
		pop2AndPushFloat,
		pushFloat,
		popAndPushFloat,
		popAndPushField,
		pushField,
		pushStatic,
		none,
		changeInt2Double,
		changeInt2Float,
		changeInt2Long,
		pushInt,
		popAndPushInt,
		callAndPush,
		call,
		changeLong2Double,
		changeLong2Float,
		changeLong2Int,
		pop4AndPushLong,
		pop2AndPushLong,
		pushLong,
		pushConst,
		pushBigConst,
		multiarrayAndPushReference,
		pushUnprepared,
		popField,
		popStatic,
		swap	
	}

	@FunctionalInterface
	interface StackChangesCallback {
		void processChanges(final int[] stackContent, final int deletedFrom, final int insertedFrom);
	}
	
	private final StackChangesCallback	callback;
	private int[]						stackContent = new int[INITIAL_STACK_SIZE], stackShadow = new int[INITIAL_STACK_SIZE];
	private int							currentStackTop = 0, maxStackDepth = 0, shadowStackTop; 
	
	StackRepo(final StackChangesCallback callback) {
		this.callback = callback;
	}
	
	void begin() {
		System.arraycopy(stackContent,currentStackTop,stackShadow,0,shadowStackTop = currentStackTop);
	}

	void clear() throws ContentException {
		while (currentStackTop > 0) {
			pop();
		}
	}
	
	void pop() throws ContentException {
		if (currentStackTop <= 0) {
			throw new ContentException("Illegal command usage: stack exhausted");
		}
		else {
			currentStackTop--;
		}
	}
	
	void pushInt() {
		ensureCapacity(1);
		stackContent[currentStackTop++] = CompilerUtils.CLASSTYPE_INT;
	}
	
	void pushLong() {
		ensureCapacity(2);
		stackContent[currentStackTop++] = CompilerUtils.CLASSTYPE_LONG;
		stackContent[currentStackTop++] = CompilerUtils.CLASSTYPE_LONG;
	}
	
	void pushFloat() {
		ensureCapacity(1);
		stackContent[currentStackTop++] = CompilerUtils.CLASSTYPE_FLOAT;
	}
	
	void pushDouble() {
		ensureCapacity(2);
		stackContent[currentStackTop++] = CompilerUtils.CLASSTYPE_DOUBLE;
		stackContent[currentStackTop++] = CompilerUtils.CLASSTYPE_DOUBLE;
	}
	
	void pushReference() {
		ensureCapacity(1);
		stackContent[currentStackTop++] = CompilerUtils.CLASSTYPE_REFERENCE;
	}
	
	void pushUnprepared() {
		ensureCapacity(1);
		stackContent[currentStackTop++] = CompilerUtils.CLASSTYPE_VOID;
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
	}

	void processChanges(final StackChanges changes) throws ContentException {
		begin();
		switch (changes) {
			case changeDouble2Float:
				if (select(0) == CompilerUtils.CLASSTYPE_DOUBLE && select(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(2);
					pushFloat();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value");
				}
				break;
			case changeDouble2Int:
				if (select(0) == CompilerUtils.CLASSTYPE_DOUBLE && select(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop();
					pushInt();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value");
				}
				break;
			case changeDouble2Long:
				if (select(0) == CompilerUtils.CLASSTYPE_DOUBLE && select(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop();
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
				if (select(0) == CompilerUtils.CLASSTYPE_LONG) {
					pop(2);
					pushDouble();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value");
				}
				break;
			case changeLong2Float:
				if (select(0) == CompilerUtils.CLASSTYPE_LONG) {
					pop(2);
					pushFloat();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value");
				}
				break;
			case changeLong2Int:
				if (select(0) == CompilerUtils.CLASSTYPE_LONG) {
					pop(2);
					pushInt();
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value");
				}
				break;
			case changeRefType:
				if (select(0) != CompilerUtils.CLASSTYPE_REFERENCE) {
					throw new ContentException("Illegal command usage: top of stack doesn't contain reference value");
				}
				break;
			case clear:
				clear();
				break;
			case dup:
				push(select(0));
				break;
			case dup2:
				final int	dup2V1 = select(0), dup2V2 = select(-1);
				
				push(dup2V2);
				push(dup2V1);
				break;
			case dup2_x1:
				final int	dup2X1V1 = select(0), dup2X1V2 = select(-1), dup2X1V3 = select(-2);
				
				pop(3);
				push(dup2X1V2);
				push(dup2X1V1);
				push(dup2X1V3);
				push(dup2X1V2);
				push(dup2X1V1);
				break;
			case dup2_x2:
				final int	dup2X2V1 = select(0), dup2X2V2 = select(-1), dup2X2V3 = select(-2), dup2X2V4 = select(-3);
				
				pop(4);
				push(dup2X2V2);
				push(dup2X2V1);
				push(dup2X2V4);
				push(dup2X2V3);
				push(dup2X2V2);
				push(dup2X2V1);
				break;
			case dup_x1	:
				final int	dupX1V1 = select(0), dupX1V2 = select(-1);
				
				pop(2);
				push(dupX1V1);
				push(dupX1V2);
				push(dupX1V1);
				break;
			case dup_x2	:
				final int	dupX2V1 = select(0), dupX2V2 = select(-1), dupX2V3 = select(-2);
				
				pop(3);
				push(dupX2V1);
				push(dupX2V3);
				push(dupX2V2);
				push(dupX2V1);
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
				
				if (val1 == CompilerUtils.CLASSTYPE_DOUBLE || val1 == CompilerUtils.CLASSTYPE_LONG) {
					throw new ContentException("Illegal command usage: double/long value at the top of stack");
				}
				else {
					pop(2);
					push(val2);
					push(val1);
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
				if (select(-signature) != CompilerUtils.CLASSTYPE_INT) {
					throw new ContentException("Illegal command usage: multianewarray command doesn't contain reference type on stack at position [-"+signature+"]");
				}
				pop(signature+1);
				pushReference();
				break;
			case popField	:
				if (signature == CompilerUtils.CLASSTYPE_DOUBLE || signature == CompilerUtils.CLASSTYPE_LONG) {
					pop(3);
				}
				else {
					pop(2);
					pop();
				}
				break;
			case popStatic	:
				if (signature == CompilerUtils.CLASSTYPE_DOUBLE || signature == CompilerUtils.CLASSTYPE_LONG) {
					pop(2);
				}
				else {
					pop();
				}
				break;
			case pushField	:
				pop();
				push(signature);
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

	int getMaxStackDepth() {
		return maxStackDepth;
	}
	
	private void compareStack(final int[] callSignature, final int signatureSize) throws ContentException {
		for (int index = 0; index < signatureSize; index++) {
			if (!typesAreCompatible(select(index-signatureSize+1),callSignature[signatureSize-index])) {
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
