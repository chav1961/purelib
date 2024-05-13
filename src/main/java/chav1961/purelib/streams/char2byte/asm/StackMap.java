package chav1961.purelib.streams.char2byte.asm;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.Utils;

class StackMap implements Cloneable {
	static final int 	ITEM_Top = 0;
	static final int 	ITEM_Integer = 1;
	static final int 	ITEM_Float = 2;
	static final int 	ITEM_Double = 3;
	static final int 	ITEM_Long = 4;
	static final int 	ITEM_Null = 5;
	static final int 	ITEM_UninitializedThis = 6;
	static final int 	ITEM_Object = 7;
	static final int 	ITEM_Uninitialized = 8;
	
	private final int 			displ;
	private final List<int[]> 	stack = new ArrayList<>();
	private final List<int[]> 	vars = new ArrayList<>();
	
	public StackMap(final int displ, final int[]... vars) {
		this(displ);
		if (vars == null || Utils.checkArrayContent4Nulls(vars) >= 0) {
			throw new IllegalArgumentException("Vars list is null or contains nulls inside");
		}
		else {
			for(int[] item : vars) {
				pushVar(item);
			}
		}
	}

	public StackMap(final int displ) {
		this.displ = displ;
	}
	
	public StackMap pushStack(final int[] content) {
		if (content == null || content.length != 2) {
			throw new IllegalArgumentException("Stack content to add can't be null and must contain exactly 2 integers");
		}
		else {
			validateContent(content);
			stack.add(0, content);
			return this;
		}
	}
	
	public StackMap popStack() {
		if (getStackDepth() <= 0) {
			throw new IllegalStateException("Stack exhausted"); 
		}
		else {
			stack.remove(0);
			return this;
		}
	}

	public StackMap popStack(final int depth) {
		if (depth <= 0) {
			throw new IllegalArgumentException("Stack depth ["+depth+"] must be greater than 0");
		}
		else {
			for (int index = 0; index < depth; index++) {
				popStack();
			}
			return this;
		}
	}
	
	public int getStackDepth() {
		return stack.size();
	}
	
	public StackMap pushVar(final int[] content) {
		if (content == null || content.length != 2) {
			throw new IllegalArgumentException("Var content to add can't be null and must contain exactly 2 integers");
		}
		else {
			validateContent(content);
			vars.add(content);
			return this;
		}
	}

	public StackMap popVar() {
		if (getVarCount() <= 0) {
			throw new IllegalStateException("Var list exhausted"); 
		}
		else {
			vars.remove(vars.size()-1);
			return this;
		}
	}

	public StackMap popVars(final int amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("Vars amount ["+amount+"] must be greater than 0");
		}
		else {
			for (int index = 0; index < amount; index++) {
				popVar();
			}
			return this;
		}
	}

	public int getVarCount() {
		return vars.size();
	}
	
	public byte[] dump() throws IOException {
		try(final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final DataOutputStream		dos = new DataOutputStream(baos)) {
			
			dump(dos);
			dos.flush();
			return baos.toByteArray();
		}
	}
	
	public void dump(final DataOutput data) throws IOException {
		data.writeByte(255);
		data.writeChar(displ);
		data.writeChar(vars.size());
		for(int index = 0; index < getVarCount(); index++) {
			switch (vars.get(index)[0]){
				case ITEM_Top				:
					data.writeByte(ITEM_Top);
					break;
				case ITEM_Integer			:
					data.writeByte(ITEM_Integer);
					break;
				case ITEM_Float				:
					data.writeByte(ITEM_Float);
					break;
				case ITEM_Double			:
					data.writeByte(ITEM_Double);
					break;
				case ITEM_Long				:
					data.writeByte(ITEM_Long);
					break;
				case ITEM_Object			:
					data.writeByte(ITEM_Object);
					data.writeByte(vars.get(index)[1]);
					break;
				case ITEM_Null				:
				case ITEM_UninitializedThis	:
				case ITEM_Uninitialized		:
					throw new UnsupportedOperationException("Var type ["+vars.get(index)[0]+"] is not implemented");
				default :
					throw new UnsupportedOperationException("Var type ["+vars.get(index)[0]+"] is not supported");
			}
		}
		data.writeChar(stack.size());		
		for(int index = 0; index < getStackDepth(); index++) {
			switch (stack.get(index)[0]){
				case ITEM_Top				:
					data.writeByte(ITEM_Top);
					break;
				case ITEM_Integer			:
					data.writeByte(ITEM_Integer);
					break;
				case ITEM_Float				:
					data.writeByte(ITEM_Float);
					break;
				case ITEM_Double			:
					data.writeByte(ITEM_Double);
					break;
				case ITEM_Long				:
					data.writeByte(ITEM_Long);
					break;
				case ITEM_Null				:
					data.writeByte(ITEM_Null);
					break;
				case ITEM_Object			:
					data.writeByte(ITEM_Object);
					data.writeByte(vars.get(index)[1]);
					break;
				case ITEM_UninitializedThis	:
				case ITEM_Uninitialized		:
					throw new UnsupportedOperationException("Var type ["+vars.get(index)[0]+"] is not implemented");
				default :
					throw new UnsupportedOperationException("Var type ["+vars.get(index)[0]+"] is not supported");
			}
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		final StackMap	result = new StackMap(displ);
		
		result.stack.addAll(stack);
		result.vars.addAll(vars);
		return result;
	}

	private void validateContent(final int[] content) {
		switch (content[0]){
			case ITEM_Top				:
			case ITEM_Integer			:
			case ITEM_Float				:
			case ITEM_Null				:
			case ITEM_Double			:
			case ITEM_Long				:
			case ITEM_UninitializedThis	:
				if (content[1] != 0) {
					throw new IllegalArgumentException("Content type refers to non-zero constant pool item");
				}
				break;
			case ITEM_Object			:
				if (content[1] <= 0) {
					throw new IllegalArgumentException("Content type refers to zero or negative constant pool item");
				}
				break;
			case ITEM_Uninitialized		:
				if (content[1] <= 0) {
					throw new IllegalArgumentException("Content type has zero or negative displacement");
				}
				break;
			default :
				throw new UnsupportedOperationException("Content type ["+content[0]+"] is not supported");
		}
	}
}
