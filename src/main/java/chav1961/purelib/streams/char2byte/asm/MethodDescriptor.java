package chav1961.purelib.streams.char2byte.asm;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.exceptions.AsmSyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class MethodDescriptor implements Closeable {
	private static final char[]					THIS = "this".toCharArray(); 
	
	private final List<Long>					parametersList = new ArrayList<>();
	private final List<StackLevel>				pushStack = new ArrayList<>();
	private final List<short[]>					tryTable = new ArrayList<>();	
	private final List<short[]>					varTable = new ArrayList<>();	
	private final List<short[]>					lineTable = new ArrayList<>();	
	private final SyntaxTreeInterface<NameDescriptor>	tree;
	private final ClassConstantsRepo			ccr;
	private final short							accessFlags;
	private final long							methodId, returnedTypeId;
	private final short							methodDispl;
	private final long[]						throwsList;
	private final short[]						throwsDispl;
	private final short							exceptionsWord, codeWord, stackMapTableWord, localVariableTableWord, lineNumberTableWord;
	private final boolean						methodBodyAwait;
	
	private MethodBody							body = null;
	private short								varDispl = 0, maxVarDispl = 0;
	private short								signatureDispl;
	private boolean								parametersEnded = false;
	
	MethodDescriptor(final SyntaxTreeInterface<NameDescriptor> tree, final ClassConstantsRepo ccr, final short accessFlags, final long classId, final long methodId, final long returnTypeId, final long... throwsList) throws IOException {
		final int	tLen = throwsList.length;
		
		this.tree = tree;						this.ccr = ccr;	
		this.accessFlags = accessFlags;			this.methodId = methodId;
		this.returnedTypeId = returnTypeId;		this.throwsList = throwsList;
		this.methodDispl = ccr.asUTF(methodId);	

		this.throwsDispl = new short[tLen];
		for (int index = 0; index < tLen; index++) {
			this.throwsDispl[index] = ccr.asClassDescription(tree.placeName(tree.getName(throwsList[index]).replace('.','/'),null));
		}
		this.exceptionsWord = ccr.asUTF(tree.placeName(Constants.ATTRIBUTE_Exceptions,0,Constants.ATTRIBUTE_Exceptions.length,null));
		this.codeWord = ccr.asUTF(tree.placeName(Constants.ATTRIBUTE_Code,0,Constants.ATTRIBUTE_Code.length,null));
		this.stackMapTableWord = ccr.asUTF(tree.placeName(Constants.ATTRIBUTE_StackMapTable,0,Constants.ATTRIBUTE_StackMapTable.length,null));
		this.localVariableTableWord = ccr.asUTF(tree.placeName(Constants.ATTRIBUTE_LocalVariableTable,0,Constants.ATTRIBUTE_LocalVariableTable.length,null));
		this.lineNumberTableWord =  ccr.asUTF(tree.placeName(Constants.ATTRIBUTE_LineNumberTable,0,Constants.ATTRIBUTE_LineNumberTable.length,null));
		
		this.methodBodyAwait = (accessFlags & (Constants.ACC_NATIVE | Constants.ACC_ABSTRACT)) == 0;
		pushStack.add(0,new StackLevel(varDispl,(short)0));
		if ((accessFlags & Constants.ACC_STATIC) == 0) {
			addParameterDeclaration((short)0,tree.placeName(THIS,0,THIS.length,null),classId);
		}
	}

	@Override
	public void close() throws IOException {
		parametersList.clear();
		pushStack.clear();
		tryTable.clear();	
		varTable.clear();	
		lineTable.clear();	
	}
	
	boolean isAbstract() {
		return (accessFlags & Constants.ACC_ABSTRACT) != 0; 
	}
	
	SyntaxTreeInterface<NameDescriptor> getNameTree(){
		return tree;
	}
	
	void setStackSize(final short stackSize) {
		body = new MethodBody(getNameTree(),stackSize);
	}
	
	void addParameterDeclaration(final short accessFlags, final long parameterId, final long typeId) throws AsmSyntaxException {
		if (parametersEnded) {
			throw new AsmSyntaxException("Parameters declaration need be before any other declarations");
		}
		else {
			addVar(accessFlags,parameterId,typeId);
			parametersList.add(typeId);
		}
	}
	
	void push() throws IOException {
		markEndOfParameters();
		pushStack.add(0,new StackLevel(varDispl,getBody().getPC()));
	}

	void addVarDeclaration(final short accessFlags, final long varId, final long typeId) throws IOException {
		markEndOfParameters();
		addVar(accessFlags,varId,typeId);
	}

	short getVarDispl(final long varId) throws AsmSyntaxException {
		short	result = 0;
		
		for (int index = 0, maxIndex = pushStack.size(); index < maxIndex; index++) {
			if ((result = pushStack.get(index).vars.getRef(varId)) != 0) {
				return (short) (result-1);
			}
		}
		throw new AsmSyntaxException("Variable ["+tree.getName(varId)+"] is not declared anywhere in this method");
	}
	
	void pop() throws IOException {
		if (pushStack.size() == 0) {
			throw new AsmSyntaxException("Pop stack exhausted");
		}
		else {
			final StackLevel 	removed = pushStack.remove(0);
			
			varDispl = removed.varDispl;
			if (methodBodyAwait) {
				removed.vars.walk(nodeTree -> varTable.add(new short[]{removed.pc,(short) (getBody().getPC()-removed.pc),nodeTree.cargo.nameRef,nodeTree.cargo.typeRef,nodeTree.ref}));
			}
			removed.vars.clear();
		}
	}
	
	AbstractMethodBody getBody() throws IOException {
		if (!methodBodyAwait) {
			throw new AsmSyntaxException("Attempt to define method body for abstract or native method");
		}
		else {
			markEndOfParameters();
			return body == null ? body = new MethodBody(getNameTree()) : body;
		}
	}
	
	int getLocalFrameSize() {
		return maxVarDispl;
	}

	void addExceptionRecord(final short fromRange, final short toRange, final short exceptionDescriptor, final short branchAddress) {
		tryTable.add(new short[]{fromRange,toRange,branchAddress,exceptionDescriptor});
	}
	
	void addLineNoRecord(final int lineNo) throws IOException {
		lineTable.add(new short[]{getBody().getPC(),(short)lineNo});
	}
	
	void complete() throws IOException {
		markEndOfParameters();
	}
	
	void dump(final OutputStream os) throws IOException {
		pop();
		try(final DataOutputStream	dos = new DataOutputStream(os)) {
			dos.writeShort(accessFlags);			// Access flags
			dos.writeShort(methodDispl);			// Method name
			dos.writeShort(signatureDispl);			// Method signature
			dos.writeShort((methodBodyAwait ? 1 : 0) + (throwsDispl.length != 0 ? 1 : 0));	// Attribute count
			if (throwsDispl.length != 0) {			// Exceptions present
				dos.writeShort(exceptionsWord);			// Exceptions attribute
				dos.writeInt(2+2*throwsDispl.length);	// Attribute size
				dos.writeShort(throwsList.length);		// Exceptions amount
				for (short item : throwsDispl) {
					dos.writeShort(item);				// Exceptions list
				}
			}
			if (methodBodyAwait) {					// Method body present
				dos.writeShort(codeWord);					// Code attribute
				dos.writeInt(12 + getBody().getCodeSize()	// Attribute size 
						+ (8 * tryTable.size()) 
						+ (6 + 2 + 10 * varTable.size())
						+ (6 + 2 + 4 * lineTable.size())
						);
				dos.writeShort(getBody().getStackSize());	// Stack size
				dos.writeShort(maxVarDispl+1);				// Local variables size
				dos.writeInt(getBody().getCodeSize());		// body size
				dos.flush();
				getBody().dump(os);							// Body content
				
				dos.writeShort(tryTable.size());			// Exception table
				for (short[] item : tryTable) {
					for (short element : item) {
						dos.writeShort(element);
					}
				}
				
				dos.writeShort(2);							// Attributes kind count
				
				dos.writeShort(localVariableTableWord);		// Local variables:
				dos.writeInt(2+10*varTable.size());			// Struct size
				dos.writeShort(varTable.size());			// Amount of variables
				for (short[] item : varTable) {
					for (short element : item) {
						dos.writeShort(element);
					}
				}
				
				dos.writeShort(lineNumberTableWord);		// Line nubers:
				dos.writeInt(2+4*lineTable.size());			// Struct size
				dos.writeShort(lineTable.size());			// Amount of code lines
				for (short[] item : lineTable) {
					for (short element : item) {
						dos.writeShort(element);
					}
				}

			}
		}
	}
	
	private void addVar(final short accessFlags, final long varId, final long typeId) throws AsmSyntaxException {
		if (pushStack.get(0).vars.getRef(varId) != 0) {
			throw new AsmSyntaxException("Duplicate variable name ["+tree.getName(varId)+"] in this block");
		}
		else {
			final String	signature = InternalUtils.buildFieldSignature(tree,typeId);
			final long		signatureId = tree.placeName(signature,null);
			
			pushStack.get(0).vars.addRef((short)(varDispl+1),varId);
			if (++varDispl > maxVarDispl) {
				maxVarDispl = varDispl;
			}
			
			try{pushStack.get(0).vars.setCargo(new VarDesc(ccr.asUTF(varId),ccr.asUTF(signatureId)),varId);
			} catch (IOException e) {
				throw new AsmSyntaxException(e);
			}
		}
	}

	private void markEndOfParameters() throws IOException {
		if (!parametersEnded) {
			final long[]	parm = new long[parametersList.size()];
			
			for (int index = 0, maxIndex = parm.length; index < maxIndex; index++) {
				parm[index] = tree.placeName(tree.getName(parametersList.get(index)).replace('.','/'),null);
			}
			signatureDispl = ccr.asUTF(tree.placeName(InternalUtils.buildMethodSignature(tree,(accessFlags & Constants.ACC_STATIC) != 0,returnedTypeId,parm),null));
			parametersEnded = true;
		}
	}

	private static class StackLevel {
		short				varDispl, pc;
		LongIdTree<VarDesc>	vars = new LongIdTree<>(1);
		
		public StackLevel(final short varDispl, final short pc) {
			this.varDispl = varDispl;
		}
	}
	
	private static class VarDesc {
		short nameRef;
		short typeRef;
		
		public VarDesc(short nameRef, short typeRef) {
			this.nameRef = nameRef;
			this.typeRef = typeRef;
		}
	}
}
