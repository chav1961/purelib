package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;
import java.util.Arrays;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.cdb.CompilerUtils;

class StackAndVarRepoNew {
	static final int			SPECIAL_TYPE_TOP = -1;
	static final int			SPECIAL_TYPE_UNPREPARED = -2;
	private static final int	INTIAL_STACKMAP = 256; 
	private static final int	INTIAL_STACK = 16;
	private static final int	INTIAL_VARS = 16;
	private static final int	INTIAL_NESTING = 16;
	private static final int[]	STACK_TOP_DESCRIPTOR = new int[] {SPECIAL_TYPE_TOP, 0};
	private static final int[]	UNPREPARED_DESCRIPTOR = new int[] {SPECIAL_TYPE_UNPREPARED, 0};
	
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
		void processChanges(final int displ, final int[] stackContent, final int deletedFrom, final int insertedFrom, final int changedFrom);
	}

	private StackMapItem[]				stackMap = new StackMapItem[INTIAL_STACKMAP];
	private int[][]						stackContent = new int[INTIAL_STACK][];
	private VarDescriptors[]			varContent = new VarDescriptors[INTIAL_NESTING];
	private int							currentStackTop = -1, maxStackDepth = 0;
	private int							currentVarTop = -1, maxVarLength = 0;
	
	StackAndVarRepoNew() {
	}
	
	void pushVarFrame(final short codeDispl) {
		ensureVarContentCapacity(1);
		varContent[currentVarTop + 1] = new VarDescriptors(codeDispl, currentVarTop < 0 ? 0 : varContent[currentVarTop].currentVarNumber);
		currentVarTop++;
	}
	
	void popVarFrame() {
	}
	
	int addVar(final int varType, final short classRef) throws ContentException {
		final short	codeDispl = varContent[currentVarTop].codeDispl;
		final int 	varDispl = varContent[currentVarTop].currentVarNumber;
		final int[]	varDesc = new int[] {varType, classRef};
		
		if (varType == CompilerUtils.CLASSTYPE_DOUBLE || varType == CompilerUtils.CLASSTYPE_LONG) {
			stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, varDispl, varDesc);
		}
		else {
			stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, varDispl, varDesc);
		}
		return varContent[currentVarTop].addVar(varDesc);
	}
	
	StackMapRecord calculateStackMap(final StackMapRecord initial, final short codeFrom, final short codeTo) {
		for(int index = codeFrom; index < codeTo; index++) {
			StackMapItem	item = stackMap[index];
			
			while (item != null) {
				
			}
		}
		
		return null;
	}
	
	void clear(final short codeDispl) throws ContentException {
		while (currentStackTop >= 0) {
			pop(codeDispl);
		}
	}
	
	void pop(final short codeDispl) throws ContentException {
		if (currentStackTop < 0) {
			throw new ContentException("Illegal command usage: stack exhausted");
		}
		else {
			currentStackTop--;
		}
	}

	void pop(final short codeDispl, final int size) throws ContentException {
		for (int index = 0; index < size; index++) {
			pop(codeDispl);
		}
	}

	void push(final short codeDispl, final int type, final int refType) {
		switch (type) {
			case CompilerUtils.CLASSTYPE_REFERENCE	: pushReference(codeDispl, refType); break;
			case CompilerUtils.CLASSTYPE_BYTE		: pushInt(codeDispl); break;
			case CompilerUtils.CLASSTYPE_SHORT		: pushInt(codeDispl); break;
			case CompilerUtils.CLASSTYPE_CHAR 		: pushInt(codeDispl); break;
			case CompilerUtils.CLASSTYPE_INT		: pushInt(codeDispl); break;
			case CompilerUtils.CLASSTYPE_FLOAT		: pushFloat(codeDispl); break;	
			case CompilerUtils.CLASSTYPE_BOOLEAN	: pushInt(codeDispl); break;
			case CompilerUtils.CLASSTYPE_LONG 		: pushLong(codeDispl); break;
			case CompilerUtils.CLASSTYPE_DOUBLE		: pushDouble(codeDispl); break;
			case SPECIAL_TYPE_UNPREPARED			: pushUnprepared(codeDispl);	break;
			default :
				throw new UnsupportedOperationException("Variable type ["+type+"] is not supported yet"); 
		}
	}
	
	int select(final int fromTop) throws ContentException {
		if (currentStackTop + fromTop > currentStackTop || currentStackTop + fromTop < 0) {
			throw new ContentException("Illegal command usage: not enought stack content. Stack content is "+prepareStackContent());
		}
		else {
			return stackContent[currentStackTop + fromTop][0];
		}
	}

	int selectRefType(final int fromTop) throws ContentException {
		if (currentStackTop + fromTop > currentStackTop || currentStackTop + fromTop < 0) {
			throw new ContentException("Illegal command usage: not enought stack content. Stack content is "+prepareStackContent());
		}
		else {
			return stackContent[currentStackTop + fromTop][1];
		}
	}
	
	void commit() {
		// TODO:
	}

	void processChanges(final short codeDispl, final StackChanges changes, final int refType) throws ContentException {
		switch (changes) {
			case changeDouble2Float:
				if (select(0) == SPECIAL_TYPE_TOP && select(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(codeDispl, 2);
					pushFloat(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value. Stack content is "+prepareStackContent());
				}
				break;
			case changeDouble2Int:
				if (select(0) == SPECIAL_TYPE_TOP && select(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(codeDispl, 2);
					pushInt(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value. Stack content is "+prepareStackContent());
				}
				break;
			case changeDouble2Long:
				if (select(0) == SPECIAL_TYPE_TOP && select(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(codeDispl, 2);
					pushLong(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value. Stack content is "+prepareStackContent());
				}
				break;
			case changeFloat2Double:
				if (select(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop(codeDispl);
					pushDouble(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeFloat2Int:
				if (select(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop(codeDispl);
					pushInt(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeFloat2Long:
				if (select(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop(codeDispl);
					pushLong(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeInt2Double:
				if (select(0) == CompilerUtils.CLASSTYPE_INT || select(0) == CompilerUtils.CLASSTYPE_BYTE || select(0) == CompilerUtils.CLASSTYPE_SHORT || select(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop(codeDispl);
					pushDouble(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeInt2Float:
				if (select(0) == CompilerUtils.CLASSTYPE_INT || select(0) == CompilerUtils.CLASSTYPE_BYTE || select(0) == CompilerUtils.CLASSTYPE_SHORT || select(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop(codeDispl);
					pushFloat(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeInt2Long:
				if (select(0) == CompilerUtils.CLASSTYPE_INT || select(0) == CompilerUtils.CLASSTYPE_BYTE || select(0) == CompilerUtils.CLASSTYPE_SHORT || select(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop(codeDispl);
					pushLong(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeLong2Double:
				if (select(-1) == CompilerUtils.CLASSTYPE_LONG) {
					pop(codeDispl, 2);
					pushDouble(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value. Stack content is "+prepareStackContent());
				}
				break;
			case changeLong2Float:
				if (select(0) == SPECIAL_TYPE_TOP && select(-1) == CompilerUtils.CLASSTYPE_LONG) {
					pop(codeDispl, 2);
					pushFloat(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value. Stack content is "+prepareStackContent());
				}
				break;
			case changeLong2Int:
				if (select(-1) == CompilerUtils.CLASSTYPE_LONG) {
					pop(codeDispl, 2);
					pushInt(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value. Stack content is "+prepareStackContent());
				}
				break;
			case clear:
				clear(codeDispl);
				break;
			case dup:
				final int	dup = select(0), dupType = selectRefType(0);
				
				if (dup != SPECIAL_TYPE_TOP) {
					push(codeDispl, dup, dupType);
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup2:
				final int	dup2V1 = select(0), dup2V2 = select(-1);
				final int	dup2V1Type = selectRefType(0), dup2V2Type = selectRefType(-1);
				
				if (dup2V2 != SPECIAL_TYPE_TOP) {
					if (dup2V2 == CompilerUtils.CLASSTYPE_LONG || dup2V2 == CompilerUtils.CLASSTYPE_DOUBLE) {
						push(codeDispl, dup2V2, dup2V2Type);
					}
					else {
						push(codeDispl, dup2V2, dup2V2Type);
						push(codeDispl, dup2V1, dup2V1Type);
					}
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup2_x1:
				final int	dup2X1V1 = select(0), dup2X1V2 = select(-1), dup2X1V3 = select(-2);
				final int	dup2X1V1Type = selectRefType(0), dup2X1V2Type = selectRefType(-1), dup2X1V3Type = selectRefType(-2);
				
				if (dup2X1V3 != SPECIAL_TYPE_TOP) {
					pop(codeDispl, 3);
					if (dup2X1V2 == CompilerUtils.CLASSTYPE_LONG || dup2X1V2 == CompilerUtils.CLASSTYPE_DOUBLE) {
						push(codeDispl, dup2X1V2, dup2X1V2Type);
					}
					else {
						push(codeDispl, dup2X1V2, dup2X1V2Type);
						push(codeDispl, dup2X1V1, dup2X1V1Type);
					}
					push(codeDispl, dup2X1V3, dup2X1V3Type);
					if (dup2X1V2 == CompilerUtils.CLASSTYPE_LONG || dup2X1V2 == CompilerUtils.CLASSTYPE_DOUBLE) {
						push(codeDispl, dup2X1V2, dup2X1V2Type);
					}
					else {
						push(codeDispl, dup2X1V2, dup2X1V2Type);
						push(codeDispl, dup2X1V1, dup2X1V1Type);
					}
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup2_x2:
				final int	dup2X2V1 = select(0), dup2X2V2 = select(-1), dup2X2V3 = select(-2), dup2X2V4 = select(-3);
				final int	dup2X2V1Type = selectRefType(0), dup2X2V2Type = selectRefType(-1), dup2X2V3Type = selectRefType(-2), dup2X2V4Type = selectRefType(-3);
				
				if (dup2X2V4 != SPECIAL_TYPE_TOP && dup2X2V3 != SPECIAL_TYPE_TOP && dup2X2V2 != SPECIAL_TYPE_TOP) {
					pop(codeDispl, 4);
					if (dup2X2V2 == CompilerUtils.CLASSTYPE_LONG || dup2X2V2 == CompilerUtils.CLASSTYPE_DOUBLE) {
						push(codeDispl, dup2X2V2, dup2X2V2Type);
					}
					else {
						push(codeDispl, dup2X2V2, dup2X2V2Type);
						push(codeDispl, dup2X2V1, dup2X2V1Type);
					}
					push(codeDispl, dup2X2V4, dup2X2V4Type);
					push(codeDispl, dup2X2V3, dup2X2V3Type);
					if (dup2X2V2 == CompilerUtils.CLASSTYPE_LONG || dup2X2V2 == CompilerUtils.CLASSTYPE_DOUBLE) {
						push(codeDispl, dup2X2V2, dup2X2V2Type);
					}
					else {
						push(codeDispl, dup2X2V2, dup2X2V2Type);
						push(codeDispl, dup2X2V1, dup2X2V1Type);
					}
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup_x1	:
				final int	dupX1V1 = select(0), dupX1V2 = select(-1);
				final int	dupX1V1Type = selectRefType(0), dupX1V2Type = selectRefType(-1);
				
				if (dupX1V1 != SPECIAL_TYPE_TOP && dupX1V2 != SPECIAL_TYPE_TOP) {
					pop(codeDispl, 2);
					push(codeDispl, dupX1V1, dupX1V1Type);
					push(codeDispl, dupX1V2, dupX1V2Type);
					push(codeDispl, dupX1V1, dupX1V1Type);
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup_x2	:
				final int	dupX2V1 = select(0), dupX2V2 = select(-1), dupX2V3 = select(-2);
				final int	dupX2V1Type = selectRefType(0), dupX2V2Type = selectRefType(-1), dupX2V3Type = selectRefType(-2);
				
				if (dupX2V3 != SPECIAL_TYPE_TOP && dupX2V2 != SPECIAL_TYPE_TOP && dupX2V1 != SPECIAL_TYPE_TOP) {
					pop(codeDispl, 3);
					push(codeDispl, dupX2V1, dupX2V1Type);
					push(codeDispl, dupX2V3, dupX2V3Type);
					push(codeDispl, dupX2V2, dupX2V2Type);
					push(codeDispl, dupX2V1, dupX2V1Type);
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case none:
				break;
			case pop:
				pop(codeDispl);
				break;
			case pop2:
				pop(codeDispl, 2);
				break;
			case pop2AndPushDouble:
				pop(codeDispl, 2);
				pushDouble(codeDispl);
				break;
			case pop2AndPushFloat:
				pop(codeDispl, 2);
				pushFloat(codeDispl);
				break;
			case pop2AndPushInt:
				pop(codeDispl, 2);
				pushInt(codeDispl);
				break;
			case pop2AndPushLong:
				pop(codeDispl, 2);
				pushLong(codeDispl);
				break;
			case pop2AndPushReference:
				pop(codeDispl, 2);
				pushReference(codeDispl, refType);
				break;
			case pop3:
				pop(codeDispl, 3);
				break;
			case pop4:
				pop(codeDispl, 4);
				break;
			case pop4AndPushDouble:
				pop(codeDispl, 4);
				pushDouble(codeDispl);
				break;
			case pop4AndPushInt:
				pop(codeDispl, 4);
				pushInt(codeDispl);
				break;
			case pop4AndPushLong:
				pop(codeDispl, 4);
				pushLong(codeDispl);
				break;
			case popAndPushInt:
				pop(codeDispl);
				pushInt(codeDispl);
				break;
			case popAndPushFloat:
				pop(codeDispl);
				pushFloat(codeDispl);
				break;
			case popAndPushReference:
				pop(codeDispl);
				pushReference(codeDispl, refType);
				break;
			case pushDouble:
				pushDouble(codeDispl);
				break;
			case pushFloat:
				pushFloat(codeDispl);
				break;
			case pushInt:
				pushInt(codeDispl);
				break;
			case pushLong:
				pushLong(codeDispl);
				break;
			case pushReference:
				pushReference(codeDispl, refType);
				break;
			case pushUnprepared:
				pushUnprepared(codeDispl);
				break;
			case swap :
				final	int val1 = select(0), val2 = select(-1);
				final	int val1Type = selectRefType(0), val2Type = selectRefType(-1);
				
				if (val2 == CompilerUtils.CLASSTYPE_DOUBLE || val2 == CompilerUtils.CLASSTYPE_LONG) {
					throw new ContentException("Illegal command usage: double/long value at the top of stack. Stack content is "+prepareStackContent());
				}
				else {
					pop(codeDispl, 2);
					push(codeDispl, val1, val1Type);
					push(codeDispl, val2, val2Type);
				}
				break;
			default:
				throw new UnsupportedOperationException(); 
		}
		commit();
	}

	void processChanges(final short codeDispl, final StackChanges changes, final int type, final int refType) throws ContentException {
		switch (changes) {
			case multiarrayAndPushReference	:
				for (int index = 0; index < type; index++) {
					if (select(-index) != CompilerUtils.CLASSTYPE_INT) {
						throw new ContentException("Illegal command usage: multianewarray command contains non-integer dimensions on stack at position ["+(index-type)+"]. Stack content is "+prepareStackContent());
					}
				}
				pop(codeDispl, type);
				pushReference(codeDispl, refType);
				break;
			case popField	:
				if (type == CompilerUtils.CLASSTYPE_DOUBLE || type == CompilerUtils.CLASSTYPE_LONG) {
					if (select(0) == SPECIAL_TYPE_TOP && typesAreCompatible(select(-1), type) && select(-2) == CompilerUtils.CLASSTYPE_REFERENCE) {
						pop(codeDispl, 3);
					}
					else {
						throw new ContentException("Illegal command usage: illegal stack content (double/long and reference awaited). Stack content is "+prepareStackContent());
					}
				}
				else {
					if (typesAreCompatible(select(0),type) && select(-1) == CompilerUtils.CLASSTYPE_REFERENCE) {
						pop(codeDispl, 2);
					}
					else {
						throw new ContentException("Illegal command usage: illegal stack content (non-double/-nonlong and reference awaited). Stack content is "+prepareStackContent());
					}
				}
				break;
			case popStatic	:
				if (type == CompilerUtils.CLASSTYPE_DOUBLE || type == CompilerUtils.CLASSTYPE_LONG) {
					if (select(0) == SPECIAL_TYPE_TOP && typesAreCompatible(select(-1), type)) {
						pop(codeDispl, 2);
					}
					else {
						throw new ContentException("Illegal command usage: attempt to save long/double from non-long/non-doube stack. Stack content is "+prepareStackContent());
					}
				}
				else {
					if (typesAreCompatible(select(0), type)) {
						pop(codeDispl);
					}
					else {
						throw new ContentException("Illegal command usage: incompatible types on static fields and stack top. Stack content is "+prepareStackContent());
					}
				}
				break;
			case pushField	:
				if (select(0) == CompilerUtils.CLASSTYPE_REFERENCE) {
					pop(codeDispl);
					push(codeDispl, type, refType);
				}
				else {
					throw new ContentException("Illegal command usage: reference is missing at the top of stack. Stack content is "+prepareStackContent());
				}
				break;
			case pushStatic	:
				push(codeDispl, type, refType);
				break;
			default:
				throw new UnsupportedOperationException("Stack changes type ["+changes+"] is not supported here"); 
		}
		commit();
	}

	void processChanges(final short codeDispl, final StackChanges changes, final int[][] callSignature, final int signatureSize, final int[] retSignature) throws ContentException {
		switch (changes) {
			case callStaticAndPush	:
				if (retSignature[0] == CompilerUtils.CLASSTYPE_VOID) {
					compareStack(callSignature,signatureSize);
					pop(codeDispl, signatureSize);
				}
				else {
					compareStack(callSignature,signatureSize);
					pop(codeDispl, signatureSize);
					push(codeDispl, retSignature[0], retSignature[1]);
				}
				break;
			case callAndPush	:
				if (retSignature[0] == CompilerUtils.CLASSTYPE_VOID) {
					compareStack(callSignature,signatureSize);
					if (select(-signatureSize) != CompilerUtils.CLASSTYPE_REFERENCE) {
						throw new ContentException("Illegal command usage: non-static invocation requires instance refrerence on the stack. Stack content is "+prepareStackContent());
					}
					else {
						pop(codeDispl, signatureSize + 1);
					}
				}
				else {
					compareStack(callSignature,signatureSize);
					if (select(-signatureSize) != CompilerUtils.CLASSTYPE_REFERENCE) {
						throw new ContentException("Illegal command usage: non-static invocation requires instance refrerence on the stack. Stack content is "+prepareStackContent());
					}
					else {
						pop(codeDispl, signatureSize + 1);
					}
					push(codeDispl, retSignature[0], retSignature[1]);
				}
				break;
			default:
				throw new UnsupportedOperationException("Stack changes type ["+changes+"] is not supported here"); 
		}
		commit();
	}

	int getCurrentStackDepth() {
		return currentStackTop+1;
	}
	
	int getMaxStackDepth() {
		return maxStackDepth;
	}

	void loadStackSnapshot(final StackSnapshot snapshot) {
		ensureStackCapacity(Math.max(0,snapshot.content.length-currentStackTop));
		System.arraycopy(snapshot.content,0,stackContent,0,snapshot.content.length);
		currentStackTop = snapshot.content.length - 1;
	}
	
	boolean compareStack(final int[] content) throws ContentException {
		for (int index = 0, signatureSize = content.length; index < signatureSize; index++) {
			if (!typesAreCompatible(select(index-signatureSize+1),content[index])) {
				return false;
			}
		}
		return true;
	}
	
	private void compareStack(final int[][] callSignature, final int signatureSize) throws ContentException {
		for (int index = 0; index < signatureSize; index++) {
			if (!typesAreCompatible(select(index-signatureSize+1),callSignature[index][0]) || !typeRefsAreCompatible(selectRefType(index-signatureSize+1),callSignature[index][1])) {
				throw new ContentException("Illegal command usage: uncompatible data types on the stack at position [-"+index+"]. "+prepareStackMismatchMessage(stackContent, getCurrentStackDepth(), callSignature, signatureSize));
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

	private boolean typeRefsAreCompatible(final int fromStack, final int fromSignature) {
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
	
	private String prepareStackMismatchMessage(final int[][] stackContent, final int stackSize, final int[][] awaitedContent, final int awaitedContentSize) {
		final StackSnapshot	stack = new StackSnapshot(stackContent,stackSize), awaited = new StackSnapshot(awaitedContent,awaitedContentSize); 
		
		return "Current stack state is: "+stack.toString()+", awaited top of stace is: "+awaited.toString();
	}

	private void ensureStackCapacity(final int delta) {
		if (currentStackTop + delta >= stackContent.length) {
			stackContent = Arrays.copyOf(stackContent, 2 * stackContent.length);
		}
		maxStackDepth = Math.max(maxStackDepth, currentStackTop + delta);
	}
	
	private void ensureVarContentCapacity(final int delta) {
		if (currentVarTop + delta >= varContent.length) {
			varContent = Arrays.copyOf(varContent, 2 * varContent.length);
		}
		maxVarLength = Math.max(maxVarLength, currentVarTop + delta);
	}

	private void ensureStackMapCapacity(final short codeDispl) {
		while (codeDispl > stackMap.length) {
			stackMap = Arrays.copyOf(stackMap, 2 * stackMap.length);
		}
	}

	private void pushInt(final short codeDispl) {
		final int[]	temp = new int[] {CompilerUtils.CLASSTYPE_INT, 0};
		
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, temp);
		ensureStackCapacity(1);
		stackContent[++currentStackTop] = temp;
	}
	
	private void pushLong(final short codeDispl) {
		final int[]	temp = new int[] {CompilerUtils.CLASSTYPE_LONG, 0};
		
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, temp);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, STACK_TOP_DESCRIPTOR);
		ensureStackCapacity(2);
		stackContent[++currentStackTop] = temp;
		stackContent[++currentStackTop] = STACK_TOP_DESCRIPTOR;
	}
	
	private void pushFloat(final short codeDispl) {
		final int[]	temp = new int[] {CompilerUtils.CLASSTYPE_FLOAT, 0};
		
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, temp);
		ensureStackCapacity(1);
		stackContent[++currentStackTop] = temp;
	}
	
	private void pushDouble(final short codeDispl) {
		final int[]	temp = new int[] {CompilerUtils.CLASSTYPE_DOUBLE, 0};
		
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, temp);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, STACK_TOP_DESCRIPTOR);
		ensureStackCapacity(2);
		stackContent[++currentStackTop] = temp;
		stackContent[++currentStackTop] = STACK_TOP_DESCRIPTOR;
	}
	
	private void pushReference(final short codeDispl, final int refType) {
		final int[]	temp = new int[] {CompilerUtils.CLASSTYPE_REFERENCE, refType};
		
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, temp);
		ensureStackCapacity(1);
		stackContent[++currentStackTop] = temp;
	}
	
	private void pushUnprepared(final short codeDispl) {
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, UNPREPARED_DESCRIPTOR);
		ensureStackCapacity(1);
		stackContent[++currentStackTop] = UNPREPARED_DESCRIPTOR;
	}

	static class VarDescriptors {
		private static final int[]	TOP_TYPE = new int[] {SPECIAL_TYPE_TOP, 0};
		
		final short	codeDispl; 
		final int	initialVarNumber; 
		int			currentVarNumber; 
		int[][]		content = new int[INTIAL_VARS][];
		
		public VarDescriptors(final short codeDispl, final int initialVarNumber) {
			this.codeDispl = codeDispl;
			this.currentVarNumber = this.initialVarNumber = initialVarNumber;
		}
		
		int addVar(final int[] desc) {
			final int	ret = currentVarNumber;
			
			if (currentVarNumber - initialVarNumber >= content.length - 1) {
				content = Arrays.copyOf(content, 2 * content.length);
			}
			content[ret] = desc.clone();
			if (desc[0] == CompilerUtils.CLASSTYPE_DOUBLE || desc[0] == CompilerUtils.CLASSTYPE_LONG) {
				content[ret + 1] = TOP_TYPE.clone();
				currentVarNumber += 2; 
			}
			else {
				currentVarNumber++; 
			}
			return ret;
		}
	}
	
	static class StackSnapshot {
		private static final int[][]	EMPTY_CONTENT = new int[0][];
		
		private final int[][]	content;

		StackSnapshot() {
			this.content = EMPTY_CONTENT;
		}		
		
		StackSnapshot(final int[][] stackContent, final int stackSize) {
			this.content = Arrays.copyOf(stackContent, stackSize + 1);
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
			
			for (int[] val : content) {
				sb.append(prefix);
				switch (val[0]) {
					case CompilerUtils.CLASSTYPE_REFERENCE	: sb.append("ref(").append(val[1]).append(")"); break;
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
		private static final int[]		EMPTY_CONTENT = new int[0];
		private static final short[]	EMPTY_TYPES = new short[0];
		
		private final int[]		content;
		private final short[]	types;

		VarSnapshot() {
			this.content = EMPTY_CONTENT;
			this.types = EMPTY_TYPES;
		}		
		
		VarSnapshot(final int[] varContent, final short[] types, final int varSize) {
			this.content = Arrays.copyOf(varContent, varSize + 1);
			this.types = Arrays.copyOf(types, varSize + 1);
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

		public StackMapRecord(final short displ) {
			this.displ = displ;
			this.stack = new StackSnapshot();
			this.vars = new VarSnapshot();
		}		
		
		public StackMapRecord(final short displ, final StackSnapshot stack, final VarSnapshot vars) {
			this.displ = displ;
			this.stack = stack;
			this.vars = vars;
		}

		public StackMapRecord calculateDelta(final StackMapRecord another) {
			return null;
		}
		
		public int getRecordSize() {
			return 0;
//			return    1	// 0xFF byte size 
//					+ 2 // displ size
//					+ 2 // stack content length size
//					+ calculateTypeArraySize(stack.content, null)	// stack content
//					+ 2	// var frame content length size
//					+ calculateTypeArraySize(vars.content, vars.prepared)	// var frame content
//					;
		}
		
		public void write(final InOutGrowableByteArray os) throws IOException {
//			os.writeByte(0xFF);
//			os.writeShort(displ);
//			os.writeShort(0);
/*			
			os.writeShort(vars.content.length);
//			os.writeShort(calculateTypeArraySize(vars.content));
			for (int index = 0; index < vars.content.length; index++) {
				final int	verification = toStackFrameTypes(vars.content[index], vars.prepared[index]); 
				
				os.writeByte(verification);
				if (verification == 7) {
					os.writeShort(vars.types[index]);
				}
			}
*/			
//			os.writeShort(stack.content.length);
//			os.writeShort(calculateTypeArraySize(stack.content));
//			for (int index = 0; index < stack.content.length; index++) {
//				os.writeByte(toStackFrameTypes(stack.content[index], true));	
//				if (stack.content[index] == CompilerUtils.CLASSTYPE_REFERENCE) {
//					os.writeShort(vars.types[index]);
//				}
//			}
		}
		
		@Override
		public String toString() {
			return "StackMapRecord [displ=" + displ + ", stack=" + stack + ", vars=" + vars + "]";
		}
		
		private static int toStackFrameTypes(final int type, final boolean wasPrepared) {
			if (wasPrepared) {
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
			else {
				return 0;
			}
		}
		
		private static int calculateTypeArraySize(final int[] array, final boolean[] prepared) {
			int	result = array.length;
			
			for (int index = 0; index < array.length; index++) {
				if (array[index] == CompilerUtils.CLASSTYPE_REFERENCE && (prepared == null || prepared[index])) {
					result += 2;
				}
			}
//			return result;
			return 0;
		}
	}
}

