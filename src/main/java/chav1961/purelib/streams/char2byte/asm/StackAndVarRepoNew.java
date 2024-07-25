package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.cdb.CompilerUtils;

class StackAndVarRepoNew {
	static final int			SPECIAL_TYPE_TOP = -1;
	static final int			SPECIAL_TYPE_NULL = -2;
//	static final int			STACK_AND_VAR_TYPE_INDEX = 0;
//	static final int			STACK_AND_VAR_REFTYPE_INDEX = 1;
	
	private static final int	INTIAL_STACKMAP = 256; 
	private static final int	INTIAL_STACK = 16;
	private static final int	INTIAL_VARS = 16;
	private static final int	INTIAL_NESTING = 16;
	private static final TypeDescriptor	STACK_TOP_DESCRIPTOR = new TypeDescriptor(SPECIAL_TYPE_TOP, (short)0);
	private static final TypeDescriptor	STACK_NULL_DESCRIPTOR = new TypeDescriptor(SPECIAL_TYPE_NULL, (short)0);

	private final ClassContainer		cc;
	private Map<Long, StackSnapshot>	forwards = new HashMap<>(); 
	private StackMapItem[]		stackMap = new StackMapItem[INTIAL_STACKMAP];
	private TypeDescriptor[]	prevStackContent = new TypeDescriptor[INTIAL_STACK];
	private TypeDescriptor[]	stackContent = new TypeDescriptor[INTIAL_STACK];
	private VarDescriptors[]	varContent = new VarDescriptors[INTIAL_NESTING];
	private int					currentStackTop = -1, maxStackDepth = 0;
	private int					currentVarTop = -1, maxVarLength = 0;
	
	StackAndVarRepoNew(final ClassContainer cc) {
		this.cc = cc;
	}
	
	void pushVarFrame(final short codeDispl) {
		ensureVarContentCapacity(1);
		varContent[currentVarTop + 1] = new VarDescriptors(codeDispl, currentVarTop < 0 ? 0 : varContent[currentVarTop].currentVarNumber);
		currentVarTop++;
	}
	
	void popVarFrame() {
		if (currentVarTop < 0) {
			throw new IllegalStateException("Var frame stack exhausted");
		}
		else {
			currentVarTop--;
		}
	}
	
	int addVar(final int varType, final short classRef, final boolean unassigned) throws ContentException {
		final short				codeDispl = varContent[currentVarTop].codeDispl;
		final int 				varDispl = varContent[currentVarTop].currentVarNumber;
		final TypeDescriptor	varDesc = new TypeDescriptor(varType, varType == CompilerUtils.CLASSTYPE_REFERENCE ?  classRef : 0, unassigned);
		
		if (varType == CompilerUtils.CLASSTYPE_DOUBLE || varType == CompilerUtils.CLASSTYPE_LONG) {
			stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, varDispl, varDesc);
		}
		else {
			stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, varDispl, varDesc);
		}
		return varContent[currentVarTop].addVar(varDesc);
	}
	
	StackMapRecord calculateStackMap(final StackMapRecord initial, final short codeFrom, final short codeTo) {
		final StackMapItem 			current = new StackMapItem(null, 0, 0);
		final List<TypeDescriptor>	stack = new ArrayList<>();
		final List<TypeDescriptor>	vars = new ArrayList<>();
		
		for(int displ = codeFrom; displ < codeTo; displ++) {
			StackMapItem	item = stackMap[displ];
			boolean 		changed = false;
			
			while (item != null) {
				if (item.stackDelta > current.stackDelta) {
					stack.add(item.stackChanges);
					changed = true;
				}
				else if (item.stackDelta < current.stackDelta) {
					for(int index = item.stackDelta; index < current.stackDelta; index++) {
						stack.remove(stack.size()-1);
					}
					changed = true;
				}
				else {
					
				}
				if (item.varIndex > current.varIndex) {
					vars.add(item.varChanges);
					changed = true;
				}
				else if (item.varIndex < current.varIndex) {
					for(int index = item.varIndex; index < current.varIndex; index++) {
						vars.remove(vars.size()-1);
					}
					changed = true;
				}
				else {
					
				}
				item = item.next;
			}
			if (changed) {
				stackMap[displ] = null;
				stack.clear();
				vars.clear();
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

	void push(final short codeDispl, final int type, final short refType) {
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
			default :
				throw new UnsupportedOperationException("Variable type ["+type+"] is not supported yet"); 
		}
	}
	
	int selectStackItemType(final int fromTop) throws ContentException {
		if (currentStackTop + fromTop > currentStackTop || currentStackTop + fromTop < 0) {
			throw new ContentException("Illegal command usage: not enought stack content. Stack content is "+prepareStackContent());
		}
		else {
			return stackContent[currentStackTop + fromTop].dataType;
		}
	}

	short selectStackItemRefType(final int fromTop) throws ContentException {
		if (currentStackTop + fromTop > currentStackTop || currentStackTop + fromTop < 0) {
			throw new ContentException("Illegal command usage: not enought stack content. Stack content is "+prepareStackContent());
		}
		else {
			return stackContent[currentStackTop + fromTop].reference;
		}
	}

	int selectVarType(final int varDispl) throws ContentException {
		if (varDispl < 0 || varDispl > varContent[currentVarTop].currentVarNumber) {
			throw new ContentException("Illegal command usage: var index ["+varDispl+"] out of range 0.."+varContent[currentVarTop].currentVarNumber);
		}
		else {
			for(int index = 0; index < currentVarTop; index++) {
				if (varDispl >= varContent[index].initialVarNumber && varDispl <= varContent[index].currentVarNumber) {
					return varContent[index].content[varDispl - varContent[index].initialVarNumber].dataType;
				}
			}
			throw new IllegalArgumentException();
		}
	}

	short selectVarRefType(final int varDispl) throws ContentException {
		if (varDispl < 0 || varDispl > varContent[currentVarTop].currentVarNumber) {
			throw new ContentException("Illegal command usage: var index ["+varDispl+"] out of range 0.."+varContent[currentVarTop].currentVarNumber);
		}
		else {
			for(int index = 0; index < currentVarTop; index++) {
				if (varDispl >= varContent[index].initialVarNumber && varDispl <= varContent[index].currentVarNumber) {
					return varContent[index].content[varDispl - varContent[index].initialVarNumber].reference;
				}
			}
			throw new IllegalArgumentException();
		}
	}
	
	void processChanges(final short codeDispl, final StackChanges changes) throws ContentException {
		processChanges(codeDispl, changes, (short)0);
	}
	
	void processChanges(final short codeDispl, final StackChanges changes, final short refType) throws ContentException {
		switch (changes) {
			case changeDouble2Float:
				if (selectStackItemType(0) == SPECIAL_TYPE_TOP && selectStackItemType(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(codeDispl, 2);
					pushFloat(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value. Stack content is "+prepareStackContent());
				}
				break;
			case changeDouble2Int:
				if (selectStackItemType(0) == SPECIAL_TYPE_TOP && selectStackItemType(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(codeDispl, 2);
					pushInt(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value. Stack content is "+prepareStackContent());
				}
				break;
			case changeDouble2Long:
				if (selectStackItemType(0) == SPECIAL_TYPE_TOP && selectStackItemType(-1) == CompilerUtils.CLASSTYPE_DOUBLE) {
					pop(codeDispl, 2);
					pushLong(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain double value. Stack content is "+prepareStackContent());
				}
				break;
			case changeFloat2Double:
				if (selectStackItemType(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop(codeDispl);
					pushDouble(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeFloat2Int:
				if (selectStackItemType(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop(codeDispl);
					pushInt(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeFloat2Long:
				if (selectStackItemType(0) == CompilerUtils.CLASSTYPE_FLOAT) {
					pop(codeDispl);
					pushLong(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeInt2Double:
				if (selectStackItemType(0) == CompilerUtils.CLASSTYPE_INT || selectStackItemType(0) == CompilerUtils.CLASSTYPE_BYTE || selectStackItemType(0) == CompilerUtils.CLASSTYPE_SHORT || selectStackItemType(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop(codeDispl);
					pushDouble(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeInt2Float:
				if (selectStackItemType(0) == CompilerUtils.CLASSTYPE_INT || selectStackItemType(0) == CompilerUtils.CLASSTYPE_BYTE || selectStackItemType(0) == CompilerUtils.CLASSTYPE_SHORT || selectStackItemType(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop(codeDispl);
					pushFloat(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeInt2Long:
				if (selectStackItemType(0) == CompilerUtils.CLASSTYPE_INT || selectStackItemType(0) == CompilerUtils.CLASSTYPE_BYTE || selectStackItemType(0) == CompilerUtils.CLASSTYPE_SHORT || selectStackItemType(0) == CompilerUtils.CLASSTYPE_CHAR) {
					pop(codeDispl);
					pushLong(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain float value. Stack content is "+prepareStackContent());
				}
				break;
			case changeLong2Double:
				if (selectStackItemType(-1) == CompilerUtils.CLASSTYPE_LONG) {
					pop(codeDispl, 2);
					pushDouble(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value. Stack content is "+prepareStackContent());
				}
				break;
			case changeLong2Float:
				if (selectStackItemType(0) == SPECIAL_TYPE_TOP && selectStackItemType(-1) == CompilerUtils.CLASSTYPE_LONG) {
					pop(codeDispl, 2);
					pushFloat(codeDispl);
				}
				else {
					throw new ContentException("Illegal command usage: top of stack doesn't contain long value. Stack content is "+prepareStackContent());
				}
				break;
			case changeLong2Int:
				if (selectStackItemType(-1) == CompilerUtils.CLASSTYPE_LONG) {
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
				final int	dup = selectStackItemType(0);
				final short	dupType = selectStackItemRefType(0);
				
				if (dup == SPECIAL_TYPE_NULL) {
					pushNull(codeDispl);
				}
				else if (dup != SPECIAL_TYPE_TOP) {
					push(codeDispl, dup, dupType);
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup2:
				final int	dup2V1 = selectStackItemType(0), dup2V2 = selectStackItemType(-1);
				final short	dup2V1Type = selectStackItemRefType(0), dup2V2Type = selectStackItemRefType(-1);
				
				if (dup2V2 != SPECIAL_TYPE_TOP) {
					if (dup2V2 == CompilerUtils.CLASSTYPE_LONG || dup2V2 == CompilerUtils.CLASSTYPE_DOUBLE) {
						push(codeDispl, dup2V2, dup2V2Type);
					}
					else {
						if (dup2V2 == SPECIAL_TYPE_NULL) {
							pushNull(codeDispl);
						}
						else {
							push(codeDispl, dup2V2, dup2V2Type);
						}
						if (dup2V1 == SPECIAL_TYPE_NULL) {
							pushNull(codeDispl);
						}
						else {
							push(codeDispl, dup2V1, dup2V1Type);
						}
					}
				}
				else {
					throw new ContentException("Illegal command usage: attempt to duplicate half of long/double value. Stack content is "+prepareStackContent());
				}
				break;
			case dup2_x1:
				final int	dup2X1V1 = selectStackItemType(0), dup2X1V2 = selectStackItemType(-1), dup2X1V3 = selectStackItemType(-2);
				final short	dup2X1V1Type = selectStackItemRefType(0), dup2X1V2Type = selectStackItemRefType(-1), dup2X1V3Type = selectStackItemRefType(-2);
				
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
				final int	dup2X2V1 = selectStackItemType(0), dup2X2V2 = selectStackItemType(-1), dup2X2V3 = selectStackItemType(-2), dup2X2V4 = selectStackItemType(-3);
				final short	dup2X2V1Type = selectStackItemRefType(0), dup2X2V2Type = selectStackItemRefType(-1), dup2X2V3Type = selectStackItemRefType(-2), dup2X2V4Type = selectStackItemRefType(-3);
				
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
				final int	dupX1V1 = selectStackItemType(0), dupX1V2 = selectStackItemType(-1);
				final short	dupX1V1Type = selectStackItemRefType(0), dupX1V2Type = selectStackItemRefType(-1);
				
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
				final int	dupX2V1 = selectStackItemType(0), dupX2V2 = selectStackItemType(-1), dupX2V3 = selectStackItemType(-2);
				final short	dupX2V1Type = selectStackItemRefType(0), dupX2V2Type = selectStackItemRefType(-1), dupX2V3Type = selectStackItemRefType(-2);
				
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
			case swap :
				final int	val1 = selectStackItemType(0), val2 = selectStackItemType(-1);
				final short	val1Type = selectStackItemRefType(0), val2Type = selectStackItemRefType(-1);
				
				if (val2 == CompilerUtils.CLASSTYPE_DOUBLE || val2 == CompilerUtils.CLASSTYPE_LONG) {
					throw new ContentException("Illegal command usage: double/long value at the top of stack. Stack content is "+prepareStackContent());
				}
				else {
					pop(codeDispl, 2);
					push(codeDispl, val1, val1Type);
					push(codeDispl, val2, val2Type);
				}
				break;
			case changeType	:
				pop(codeDispl);
				pushReference(codeDispl, refType);
				break;
			case pushNull	:
				pushNull(codeDispl);
				break;
			default:
				throw new UnsupportedOperationException("Stack changes type ["+changes+"] is not supported here"); 
		}
//		commit();
	}

	void processChanges(final short codeDispl, final StackChanges changes, final int type, final short refType) throws ContentException {
		switch (changes) {
			case multiarrayAndPushReference	:
				for (int index = 0; index < type; index++) {
					if (selectStackItemType(-index) != CompilerUtils.CLASSTYPE_INT) {
						throw new ContentException("Illegal command usage: multianewarray command contains non-integer dimensions on stack at position ["+(index-type)+"]. Stack content is "+prepareStackContent());
					}
				}
				pop(codeDispl, type);
				pushReference(codeDispl, refType);
				break;
			case popField	:
				if (type == CompilerUtils.CLASSTYPE_DOUBLE || type == CompilerUtils.CLASSTYPE_LONG) {
					if (selectStackItemType(0) == SPECIAL_TYPE_TOP && typesAreCompatible(selectStackItemType(-1), type) && selectStackItemType(-2) == CompilerUtils.CLASSTYPE_REFERENCE) {
						pop(codeDispl, 3);
					}
					else {
						throw new ContentException("Illegal command usage: illegal stack content (double/long and reference awaited). Stack content is "+prepareStackContent());
					}
				}
				else {
					if (typesAreCompatible(selectStackItemType(0),type) && selectStackItemType(-1) == CompilerUtils.CLASSTYPE_REFERENCE) {
						pop(codeDispl, 2);
					}
					else {
						throw new ContentException("Illegal command usage: illegal stack content (non-double/-nonlong and reference awaited). Stack content is "+prepareStackContent());
					}
				}
				break;
			case popStatic	:
				if (type == CompilerUtils.CLASSTYPE_DOUBLE || type == CompilerUtils.CLASSTYPE_LONG) {
					if (selectStackItemType(0) == SPECIAL_TYPE_TOP && typesAreCompatible(selectStackItemType(-1), type)) {
						pop(codeDispl, 2);
					}
					else {
						throw new ContentException("Illegal command usage: attempt to save long/double from non-long/non-doube stack. Stack content is "+prepareStackContent());
					}
				}
				else {
					if (typesAreCompatible(selectStackItemType(0), type)) {
						pop(codeDispl);
					}
					else {
						throw new ContentException("Illegal command usage: incompatible types on static fields and stack top. Stack content is "+prepareStackContent());
					}
				}
				break;
			case pushField	:
				if (selectStackItemType(0) == CompilerUtils.CLASSTYPE_REFERENCE) {
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
			case pushReference:
				pushReference(codeDispl, refType);
				break;
			case popAndPushReference:
				pop(codeDispl);
				pushReference(codeDispl, refType);
				break;
			case pushInt	:
				pushInt(codeDispl);
				break;
			case pushFloat	:
				pushFloat(codeDispl);
				break;
			case pushLong	:
				pushLong(codeDispl);
				break;
			case pushDouble	:
				pushDouble(codeDispl);
				break;
			case pop2AndPushReference:
				pop(codeDispl, 2);
				pushReference(codeDispl, refType);
				break;
			case pop2AndPushInt	:
				pop(codeDispl, 2);
				pushInt(codeDispl);
				break;
			default:
				throw new UnsupportedOperationException("Stack changes type ["+changes+"] is not supported here"); 
		}
//		commit();
	}

	void processChanges(final short codeDispl, final StackChanges changes, final TypeDescriptor[] callSignature, final int signatureSize, final TypeDescriptor retSignature) throws ContentException {
		switch (changes) {
			case callStaticAndPush	:
				if (retSignature.dataType == CompilerUtils.CLASSTYPE_VOID) {
					compareStack(callSignature, signatureSize);
					pop(codeDispl, signatureSize);
				}
				else {
					compareStack(callSignature, signatureSize);
					pop(codeDispl, signatureSize);
					push(codeDispl, retSignature.dataType, retSignature.reference);
				}
				break;
			case callAndPush	:
				if (retSignature.dataType == CompilerUtils.CLASSTYPE_VOID) {
					compareStack(callSignature,signatureSize);
					if (selectStackItemType(-signatureSize) != CompilerUtils.CLASSTYPE_REFERENCE) {
						throw new ContentException("Illegal command usage: non-static invocation requires instance refrerence on the stack. Stack content is "+prepareStackContent());
					}
					else {
						pop(codeDispl, signatureSize + 1);
					}
				}
				else {
					compareStack(callSignature,signatureSize);
					if (selectStackItemType(-signatureSize) != CompilerUtils.CLASSTYPE_REFERENCE) {
						throw new ContentException("Illegal command usage: non-static invocation requires instance refrerence on the stack. Stack content is "+prepareStackContent());
					}
					else {
						pop(codeDispl, signatureSize + 1);
					}
					push(codeDispl, retSignature.dataType, retSignature.reference);
				}
				break;
			default:
				throw new UnsupportedOperationException("Stack changes type ["+changes+"] is not supported here"); 
		}
//		commit();
	}

	void markForwardBrunch(final long labelId) {
		forwards.put(labelId, new StackSnapshot(stackContent, currentStackTop));
	}
	
	void loadForwardSnapshot(final long labelId) {
	}
	
	int getCurrentStackDepth() {
		return currentStackTop+1;
	}
	
	int getMaxStackDepth() {
		return maxStackDepth;
	}

	TypeDescriptor getVarType(final int varDispl) throws ContentException {
		for(int index = currentVarTop; index >= 0; index--) {
			final VarDescriptors	desc = varContent[index]; 
			
			if (desc.initialVarNumber <= varDispl && desc.currentVarNumber > varDispl) {
				return desc.content[varDispl - desc.initialVarNumber];
			}
		}
		throw new ContentException("Var displacement ["+varDispl+"] outside the range");
	}
	
	StackSnapshot makeStackSnapshot() {
		return new StackSnapshot(stackContent, currentStackTop); 
	}

	VarSnapshot makeVarSnapshot() {
		return new VarSnapshot(collectVarDescriptors()); 
	}

	TypeDescriptor[] collectVarDescriptors() {
		final List<TypeDescriptor> vars = new ArrayList<>();
		
		for(int index = 0; index <= currentVarTop; index++) {
			final VarDescriptors 	desc = varContent[index];
			
			for(int descIndex = 0; descIndex < desc.currentVarNumber - desc.initialVarNumber; descIndex++) {
				vars.add(desc.content[descIndex]);
			}
		}
		return vars.toArray(new TypeDescriptor[vars.size()]);
	}
	
	StackMapRecord createStackMapRecord(final short displ, final long labelId) {
		final TypeDescriptor[] varTypes = collectVarDescriptors();
		
		if (forwards.containsKey(labelId)) {
			loadStackSnapshot(forwards.get(labelId));
		}
		return new StackMapRecord(displ, new StackSnapshot(stackContent, currentStackTop), new VarSnapshot(varTypes));
	}

	
	void prepareCatch(final short codeDispl, final int stackDepth, final int varFrameLength, final short catchType) throws ContentException {
		while (currentStackTop > stackDepth) {
			pop(codeDispl);
		}
		push(codeDispl, CompilerUtils.CLASSTYPE_REFERENCE, catchType);
	}	
	
	void loadStackSnapshot(final StackSnapshot snapshot) {
		ensureStackCapacity(Math.max(0,snapshot.content.length-currentStackTop));
		System.arraycopy(snapshot.content,0,stackContent,0,snapshot.content.length);
		currentStackTop = snapshot.content.length - 1;
	}
	
	boolean compareStack(final int[] content) throws ContentException {
		for (int index = 0, signatureSize = content.length; index < signatureSize; index++) {
			if (!typesAreCompatible(selectStackItemType(index-signatureSize+1), content[index])) {
				return false;
			}
		}
		return true;
	}

	boolean typesAreCompatible(final int fromStack, final int fromSignature) {
		if (fromStack != fromSignature) {
			if (fromStack == SPECIAL_TYPE_NULL && fromSignature == CompilerUtils.CLASSTYPE_REFERENCE) {
				return true;
			}
			else {
				return fromStack == CompilerUtils.CLASSTYPE_INT && (fromSignature == CompilerUtils.CLASSTYPE_BYTE || fromSignature == CompilerUtils.CLASSTYPE_SHORT || fromSignature == CompilerUtils.CLASSTYPE_CHAR || fromSignature == CompilerUtils.CLASSTYPE_INT || fromSignature == CompilerUtils.CLASSTYPE_BOOLEAN);
			}
		}
		else {
			return true;
		}
	}

	boolean typeRefsAreCompatible(final short fromStack, final short fromSignature) throws ContentException {
		if (fromStack == fromSignature) {
			return true;
		}
		else if (fromStack == 0 || fromSignature == 0) {
			return false;
		}
		else {
			return typeRefsAreCompatible(InternalUtils.classSignature2ClassName(InternalUtils.displ2String(cc, fromStack)), InternalUtils.classSignature2ClassName(InternalUtils.displ2String(cc, fromSignature)));
		}
	}

	
	private void compareStack(final TypeDescriptor[] callSignature, final int signatureSize) throws ContentException {
		for (int index = 0; index < signatureSize; index++) {
			if (!typesAreCompatible(selectStackItemType(index - signatureSize + 1), callSignature[index].dataType) || !typeRefsAreCompatible(selectStackItemRefType(index - signatureSize + 1), callSignature[index].reference)) {
				typesAreCompatible(selectStackItemType(index - signatureSize + 1), callSignature[index].dataType);
				typeRefsAreCompatible(selectStackItemRefType(index - signatureSize + 1), callSignature[index].reference);
				throw new ContentException("Illegal command usage: uncompatible data types on the stack at position ["+index+"]. " 
							+ InternalUtils.prepareStackMismatchMessage(stackContent, getCurrentStackDepth(), callSignature, signatureSize)
						);
			}
		}
	}

	private boolean typeRefsAreCompatible(final String fromStack, final String fromSignature) throws ContentException {
		if (fromStack.charAt(0) == '[' && fromSignature.charAt(0) == '[') {
			return typeRefsAreCompatible(fromStack.substring(1), fromSignature.substring(1));
		}
		else {
			final boolean	arrayInStack = fromStack.charAt(0) == '[', arrayInSignature = fromSignature.charAt(0) == '[';
			
			if (!arrayInStack && !arrayInSignature) {
				if (cc.getDescriptionRepo().hasClassDescription(fromStack) && cc.getDescriptionRepo().hasClassDescription(fromSignature)) {
					return cc.getDescriptionRepo().getClassDescription(fromSignature).isAssignableFrom(cc.getDescriptionRepo().getClassDescription(fromStack));
				}
				else {
					return false;
				}
			}
			else {
				return Object.class.getName().equals(fromSignature);
			}
		}
		
	}	
	
	private String prepareStackContent() {
		return new StackSnapshot(stackContent, getCurrentStackDepth()).toString();
	}
	
//	private static String prepareStackMismatchMessage(final TypeDescriptor[] stackContent, final int stackSize, final TypeDescriptor[] awaitedContent, final int awaitedContentSize) {
//		final StackSnapshot	stack = new StackSnapshot(stackContent, stackSize), awaited = new StackSnapshot(awaitedContent, awaitedContentSize); 
//		
//		return "Current stack state is: "+stack.toString()+", awaited top of stack is: "+awaited.toString();
//	}

	private void ensureStackCapacity(final int delta) {
		if (currentStackTop + delta >= stackContent.length) {
			stackContent = Arrays.copyOf(stackContent, 2 * stackContent.length);
			prevStackContent = Arrays.copyOf(prevStackContent, 2 * prevStackContent.length);
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
		while (codeDispl >= stackMap.length) {
			stackMap = Arrays.copyOf(stackMap, 2 * stackMap.length);
		}
	}

	void pushInt(final short codeDispl) {
		final TypeDescriptor	temp = new TypeDescriptor(CompilerUtils.CLASSTYPE_INT, (short)0);
		
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, temp);
		pushStack(temp);
	}
	
	void pushLong(final short codeDispl) {
		final TypeDescriptor	temp = new TypeDescriptor(CompilerUtils.CLASSTYPE_LONG, (short)0);
		
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, temp);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, STACK_TOP_DESCRIPTOR);
		pushStack2(temp);
	}
	
	void pushFloat(final short codeDispl) {
		final TypeDescriptor	temp = new TypeDescriptor(CompilerUtils.CLASSTYPE_FLOAT, (short)0);
		
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, temp);
		pushStack(temp);
	}
	
	void pushDouble(final short codeDispl) {
		final TypeDescriptor	temp = new TypeDescriptor(CompilerUtils.CLASSTYPE_DOUBLE, (short)0);
		
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, temp);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, STACK_TOP_DESCRIPTOR);
		pushStack2(temp);
	}
	
	void pushReference(final short codeDispl, final short refType) {
		final TypeDescriptor	temp = new TypeDescriptor(CompilerUtils.CLASSTYPE_REFERENCE, refType);
		
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, temp);
		pushStack(temp);
	}

	void pushNull(final short codeDispl) {
		final TypeDescriptor	nullDesc = new TypeDescriptor(SPECIAL_TYPE_NULL, (short)0);
				
		ensureStackMapCapacity(codeDispl);
		stackMap[codeDispl] = new StackMapItem(stackMap[codeDispl], codeDispl, 0, null, 1, nullDesc);
		pushStack(nullDesc);
	}
	
	private void pushStack(final TypeDescriptor value) {
		ensureStackCapacity(1);
		stackContent[++currentStackTop] = value;
	}

	private void pushStack2(final TypeDescriptor value) {
		ensureStackCapacity(2);
		stackContent[++currentStackTop] = value;
		stackContent[++currentStackTop] = STACK_TOP_DESCRIPTOR;
	}

	static class TypeDescriptor implements Cloneable {
		int		dataType;
		short	reference;
		boolean	unassigned = false;

		TypeDescriptor() {
			this.dataType = 0;
			this.reference = 0;
			this.unassigned = false;
		}
		
		public TypeDescriptor(final int dataType) {
			if (dataType == CompilerUtils.CLASSTYPE_REFERENCE) {
				throw new IllegalArgumentException("Referenced data type must contain reference id. Use another constructor here");
			}
			else {
				this.dataType = dataType;
				this.reference = 0;
				this.unassigned = false;
			}
		}
		
		public TypeDescriptor(final int dataType, final short reference) {
			if (dataType == CompilerUtils.CLASSTYPE_REFERENCE && reference == 0) {
				throw new IllegalArgumentException("Referenced data type must contain reference id");
			}
			else if (dataType != CompilerUtils.CLASSTYPE_REFERENCE && reference != 0) {
				throw new IllegalArgumentException("Primitive data type can't contain non-zero reference id");
			}
			else {
				this.dataType = dataType;
				this.reference = reference;
				this.unassigned = false;
			}
		}

		public TypeDescriptor(final int dataType, final short reference, final boolean unassigned) {
			if (dataType == CompilerUtils.CLASSTYPE_REFERENCE && reference == 0) {
				throw new IllegalArgumentException("Referenced data type must contain reference id");
			}
			else if (dataType != CompilerUtils.CLASSTYPE_REFERENCE && reference != 0) {
				throw new IllegalArgumentException("Primitive data type can't contain non-zero reference id");
			}
			else {
				this.dataType = dataType;
				this.reference = reference;
				this.unassigned = unassigned;
			}
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}

		@Override
		public int hashCode() {
			return Objects.hash(dataType, reference, unassigned);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			TypeDescriptor other = (TypeDescriptor) obj;
			return dataType == other.dataType && reference == other.reference && unassigned == other.unassigned;
		}

		@Override
		public String toString() {
			final String	dataTypeName;
			
			switch (dataType) {
				case CompilerUtils.CLASSTYPE_REFERENCE	:
					dataTypeName = "reference";
					break;
				case CompilerUtils.CLASSTYPE_BYTE		:
					dataTypeName = "byte";
					break;
				case CompilerUtils.CLASSTYPE_SHORT		:
					dataTypeName = "short";
					break;
				case CompilerUtils.CLASSTYPE_CHAR		:	
					dataTypeName = "char";
					break;
				case CompilerUtils.CLASSTYPE_INT		:	
					dataTypeName = "int";
					break;
				case CompilerUtils.CLASSTYPE_LONG		:	
					dataTypeName = "long";
					break;
				case CompilerUtils.CLASSTYPE_FLOAT		:	
					dataTypeName = "float";
					break;
				case CompilerUtils.CLASSTYPE_DOUBLE		:	
					dataTypeName = "double";
					break;
				case CompilerUtils.CLASSTYPE_BOOLEAN	:	
					dataTypeName = "boolean";
					break;
				case CompilerUtils.CLASSTYPE_VOID		:	
					dataTypeName = "void";
					break;
				case SPECIAL_TYPE_TOP					:
					dataTypeName = "top";
					break;
				case SPECIAL_TYPE_NULL					:
					dataTypeName = "null";
					break;
				default :
					dataTypeName = ""+dataType;
					break;
			}
			return "TypeDescriptor [dataType=" + dataTypeName + ", reference=" + reference + ", unassigned=" + unassigned + "]";
		}
	}
	
	static class VarDescriptors {
		final short			codeDispl; 
		final int			initialVarNumber; 
		int					currentVarNumber; 
		TypeDescriptor[]	content = new TypeDescriptor[INTIAL_VARS];
		
		public VarDescriptors(final short codeDispl, final int initialVarNumber) {
			this.codeDispl = codeDispl;
			this.currentVarNumber = this.initialVarNumber = initialVarNumber;
		}
		
		int addVar(final TypeDescriptor desc) {
			try {
				final int	index = currentVarNumber - initialVarNumber;
				final int	ret = currentVarNumber;
				
				if (currentVarNumber - initialVarNumber >= content.length - 1) {
					content = Arrays.copyOf(content, 2 * content.length);
				}
				content[index] = (TypeDescriptor) desc.clone();
				if (desc.dataType == CompilerUtils.CLASSTYPE_DOUBLE || desc.dataType == CompilerUtils.CLASSTYPE_LONG) {
					content[index + 1] = (TypeDescriptor) STACK_TOP_DESCRIPTOR.clone();
					currentVarNumber += 2; 
				}
				else {
					currentVarNumber++; 
				}
				return ret;
			} catch (CloneNotSupportedException exc) {
				throw new RuntimeException(exc);
			}
		}

		@Override
		public String toString() {
			return "VarDescriptors [codeDispl=" + codeDispl + ", initialVarNumber=" + initialVarNumber + ", currentVarNumber=" + currentVarNumber + ", content=" 
					+ Arrays.deepToString(Arrays.copyOf(content, currentVarNumber - initialVarNumber)) + "]";
		}
	}
	
	static class StackSnapshot {
		private static final TypeDescriptor[]	EMPTY_CONTENT = new TypeDescriptor[0];
		
		final TypeDescriptor[]	content;

		StackSnapshot() {
			this.content = EMPTY_CONTENT;
		}		
		
		StackSnapshot(final TypeDescriptor[] stackContent, final int stackSize) {
			this.content = Arrays.copyOf(stackContent, stackSize + 1);
		}

		int getLength() {
			return content.length;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.deepHashCode(content);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			StackSnapshot other = (StackSnapshot) obj;
			if (!Arrays.deepEquals(content, other.content)) return false;
			return true;
		}

		@Override
		public String toString() {
			final StringBuilder	sb = new StringBuilder();
			String				prefix= "";
			
			sb.append("StackSnapshot [");
			
			for (TypeDescriptor val : content) {
				sb.append(prefix);
				if (val == null) {
					sb.append("NULL");
				}
				else {
					switch (val.dataType) {
						case CompilerUtils.CLASSTYPE_REFERENCE	: sb.append("ref(").append(val.reference).append(")"); break;
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
						case SPECIAL_TYPE_NULL 					: sb.append("null"); break;
						default 								: sb.append(val); break;
					}
				}
				prefix = ",";
			}
			
			return  sb.append("]").toString();
		}
	}
		
	
	static class VarSnapshot {
		private static final TypeDescriptor[]	EMPTY_CONTENT = new TypeDescriptor[0];
		
		private final TypeDescriptor[]	content;

		VarSnapshot() {
			this.content = EMPTY_CONTENT;
		}		

		VarSnapshot(final TypeDescriptor[] varContent) {
			this.content = new TypeDescriptor[varContent.length];
					
			for(int index = 0; index < content.length; index++) {
				try{
					this.content[index] = (TypeDescriptor) varContent[index].clone();
				} catch (CloneNotSupportedException e) {
				}
			}
		}
		
		
		VarSnapshot(final TypeDescriptor[] varContent, final int varSize) {
			this.content = Arrays.copyOf(varContent, varSize);
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
			
			for (TypeDescriptor val : content) {
				sb.append(prefix);
				switch (val.dataType) {
					case CompilerUtils.CLASSTYPE_REFERENCE	: sb.append("ref(").append(val.reference).append(")"); break;
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
			if (stack == null) {
				throw new NullPointerException("Stack snapshot can't be null");
			}
			else if (vars == null) {
				throw new NullPointerException("Var snapshot can't be null");
			}
			else {
				this.displ = displ;
				this.stack = stack;
				this.vars = vars;
			}
		}

		public int getRecordSize() {
			return    1	// 0xFF byte size 
					+ 2 // displ size
					+ 2 // stack content length size
					+ calculateStackTypeArraySize(stack.content)	// stack content (type TOP excluded)
					+ 2	// var frame content length size
					+ calculateVarTypeArraySize(vars.content)		// var frame content
					;
		}
		
		public int write(final InOutGrowableByteArray os, final int delta) throws IOException {
			os.writeByte(0xFF);
			os.writeShort(displ - delta);

			os.writeShort(vars.content.length);
			for (int index = 0; index < vars.content.length; index++) {
				final int	verification = toVarFrameTypes(vars.content[index]); 
				
				if (vars.content[index].unassigned) {
					os.writeByte(StackMap.ITEM_Top);
				}
				else {
					os.writeByte(verification);
					if (verification == StackMap.ITEM_Object) {
						os.writeShort(vars.content[index].reference);
					}
				}
			}
			
			os.writeShort(calculateStackTypeCount(stack.content));			// Exclude TOP type from stack items amount
			for (int index = 0; index < stack.content.length; index++) {
				if (stack.content[index].dataType != SPECIAL_TYPE_TOP) {	// Skip TOP type to print stack content
					os.writeByte(toStackFrameTypes(stack.content[index]));	
					if (stack.content[index].dataType == CompilerUtils.CLASSTYPE_REFERENCE) {
						os.writeShort(stack.content[index].reference);
					}
				}
			}
			
			return displ + 1;
		}
		
		@Override
		public String toString() {
			return "StackMapRecord [displ=" + displ + ", stack=" + stack + ", vars=" + vars + "]";
		}
		
		private static int toStackFrameTypes(final TypeDescriptor type) {
			switch (type.dataType) {
				case CompilerUtils.CLASSTYPE_REFERENCE	: 
					return StackMap.ITEM_Object;
				case CompilerUtils.CLASSTYPE_BOOLEAN : case CompilerUtils.CLASSTYPE_BYTE : case CompilerUtils.CLASSTYPE_SHORT :
				case CompilerUtils.CLASSTYPE_CHAR : case CompilerUtils.CLASSTYPE_INT :
					return StackMap.ITEM_Integer;	
				case CompilerUtils.CLASSTYPE_LONG		: 
					return StackMap.ITEM_Long;	
				case CompilerUtils.CLASSTYPE_FLOAT		: 
					return StackMap.ITEM_Float;	
				case CompilerUtils.CLASSTYPE_DOUBLE		: 
					return StackMap.ITEM_Double;	
				case SPECIAL_TYPE_TOP					: 
					return StackMap.ITEM_Top;
				case SPECIAL_TYPE_NULL					: 
					return StackMap.ITEM_Null;
				default : 
					throw new UnsupportedOperationException("Type ["+type.dataType+"] is not supported yet");
			}
		}

		private static int toVarFrameTypes(final TypeDescriptor type) {
			if (type.unassigned) {
				return StackMap.ITEM_Top;
			}
			else {
				switch (type.dataType) {
					case CompilerUtils.CLASSTYPE_REFERENCE	: 
						return StackMap.ITEM_Object;
					case CompilerUtils.CLASSTYPE_BOOLEAN : case CompilerUtils.CLASSTYPE_BYTE : case CompilerUtils.CLASSTYPE_SHORT :
					case CompilerUtils.CLASSTYPE_CHAR : case CompilerUtils.CLASSTYPE_INT :
						return StackMap.ITEM_Integer;	
					case CompilerUtils.CLASSTYPE_LONG		: 
						return StackMap.ITEM_Long;	
					case CompilerUtils.CLASSTYPE_FLOAT		: 
						return StackMap.ITEM_Float;	
					case CompilerUtils.CLASSTYPE_DOUBLE		: 
						return StackMap.ITEM_Double;	
					case SPECIAL_TYPE_TOP					: 
						return StackMap.ITEM_Top;
					case SPECIAL_TYPE_NULL					: 
						return StackMap.ITEM_Null;
					default : 
						throw new UnsupportedOperationException("Type ["+type.dataType+"] is not supported yet");
				}
			}
		}
		
		private static int calculateStackTypeArraySize(final TypeDescriptor[] array) {
			int	result = array.length;
			
			for (int index = 0; index < array.length; index++) {
				if (array[index].dataType == CompilerUtils.CLASSTYPE_REFERENCE && !array[index].unassigned) {
					if (array[index].reference != 0) {
						result += 2;
					}
				}
				else if (array[index].dataType == SPECIAL_TYPE_TOP) {
					result--;
				}
			}
			return result;
		}

		private static int calculateStackTypeCount(final TypeDescriptor[] array) {
			int	result = array.length;
			
			for (int index = 0; index < array.length; index++) {
				if (array[index].dataType == SPECIAL_TYPE_TOP) {
					result--;
				}
			}
			return result;
		}
		
		private static int calculateVarTypeArraySize(final TypeDescriptor[] array) {
			int	result = array.length;
			
			for (int index = 0; index < array.length; index++) {
				if (array[index].dataType == CompilerUtils.CLASSTYPE_REFERENCE && !array[index].unassigned) {
					result += 2;
				}
			}
			return result;
		}
	}

}

