package chav1961.purelib.streams.char2byte.asm;


import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.streams.char2byte.asm.LongIdTree.LongIdTreeNode;

class MethodDescriptor implements Closeable {
	private static final char[]					THIS = "this".toCharArray(); 
	private static final String					INIT_STRING = "<init>"; 
	private static final String					CLASS_INIT_STRING = "<init>"; 
	private static final char[]					INIT = INIT_STRING.toCharArray(); 
	private static final char[]					CLASS_INIT = CLASS_INIT_STRING.toCharArray(); 

	final short									accessFlags;
	
	private final List<Long>					parametersList = new ArrayList<>();
	private final List<StackLevel>				pushStack = new ArrayList<>();
	private final List<short[]>					tryTable = new ArrayList<>();	
	private final List<short[]>					varTable = new ArrayList<>();	
	private final List<short[]>					lineTable = new ArrayList<>();	
	private final SyntaxTreeInterface<NameDescriptor>	tree;
	private final ClassConstantsRepo			ccr;
	private final long							classId, methodId, returnedTypeId;
	private final long							longId, doubleId;
	private final short							methodDispl;
	private final long[]						throwsList;
	private final short[]						throwsDispl;
	private final short							exceptionsWord, codeWord, localVariableTableWord, lineNumberTableWord;
	private final boolean						methodBodyAwait;
	
	private MethodBody							body = null;
	private short								varDispl = 0, maxVarDispl = 0;
	private short								signatureDispl;
	private boolean								parametersEnded = false;
	
	MethodDescriptor(final SyntaxTreeInterface<NameDescriptor> tree, final ClassConstantsRepo ccr, final short accessFlags, final long classId, final long methodId, final long returnTypeId, final long... throwsList) throws IOException, ContentException {
		final int	tLen = throwsList.length;
		
		this.tree = tree;						
		this.ccr = ccr;	
		this.accessFlags = accessFlags;
		this.classId = classId;
		this.methodId = methodId;
		this.returnedTypeId = returnTypeId;		
		this.throwsList = throwsList;
		this.methodDispl = ccr.asUTF(methodId);	

		this.longId = tree.seekName("long");
		this.doubleId = tree.seekName("double");
		
		this.throwsDispl = new short[tLen];
		for (int index = 0; index < tLen; index++) {
			this.throwsDispl[index] = ccr.asClassDescription(tree.placeOrChangeName(tree.getName(throwsList[index]).replace('.','/'),new NameDescriptor()));
		}
		this.exceptionsWord = ccr.asUTF(tree.placeOrChangeName(Constants.ATTRIBUTE_Exceptions,0,Constants.ATTRIBUTE_Exceptions.length,new NameDescriptor()));
		this.codeWord = ccr.asUTF(tree.placeOrChangeName(Constants.ATTRIBUTE_Code,0,Constants.ATTRIBUTE_Code.length,new NameDescriptor()));
		this.localVariableTableWord = ccr.asUTF(tree.placeOrChangeName(Constants.ATTRIBUTE_LocalVariableTable,0,Constants.ATTRIBUTE_LocalVariableTable.length,new NameDescriptor()));
		this.lineNumberTableWord =  ccr.asUTF(tree.placeOrChangeName(Constants.ATTRIBUTE_LineNumberTable,0,Constants.ATTRIBUTE_LineNumberTable.length,new NameDescriptor()));
		
		this.methodBodyAwait = (accessFlags & (Constants.ACC_NATIVE | Constants.ACC_ABSTRACT)) == 0;
		pushStack.add(0,new StackLevel(varDispl,(short)0));
		if ((accessFlags & Constants.ACC_STATIC) == 0) {
			addParameterDeclaration((short)0,tree.placeOrChangeName(THIS,0,THIS.length,new NameDescriptor()),classId);
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
		body = new MethodBody(classId, methodId, getNameTree(),stackSize);
	}
	
	void addParameterDeclaration(final short accessFlags, final long parameterId, final long typeId) throws ContentException {
		if (parametersEnded) {
			throw new ContentException("Parameters declaration need be before any other declarations");
		}
		else {
			addVar(accessFlags,parameterId,typeId);
			parametersList.add(typeId);
		}
	}
	
	void push() throws IOException, ContentException {
		markEndOfParameters();
		pushStack.add(0,new StackLevel(varDispl,getBody().getPC()));
	}

	void addVarDeclaration(final short accessFlags, final long varId, final long typeId) throws ContentException, IOException {
		markEndOfParameters();
		addVar(accessFlags,varId,typeId);
	}

	short getVarDispl(final long varId) throws ContentException {
		short	result = 0;
		
		for (int index = 0, maxIndex = pushStack.size(); index < maxIndex; index++) {
			if ((result = pushStack.get(index).vars.getRef(varId)) != 0) {
				return (short) (result-1);
			}
		}
		throw new ContentException("Variable ["+tree.getName(varId)+"] is not declared anywhere in this method");
	}
	
	void pop() throws IOException, ContentException {
		if (pushStack.size() == 0) {
			throw new ContentException("Pop stack exhausted");
		}
		else {
			final StackLevel 	removed = pushStack.remove(0);
			
			varDispl = removed.varDispl;
			if (methodBodyAwait) {
				removed.vars.walk(
						new LongIdTreeWalker<MethodDescriptor.VarDesc>() {
							@Override
							public void process(LongIdTreeNode<VarDesc> nodeTree) throws IOException {
								try{varTable.add(new short[]{removed.pc,(short) (getBody().getPC()-removed.pc),nodeTree.cargo.nameRef,nodeTree.cargo.typeRef,nodeTree.ref});
								} catch (ContentException e) {
									throw new IOException(e.getMessage(),e);
								}
							}
						}
					);
			}
			removed.vars.clear();
		}
	}
	
	AbstractMethodBody getBody() throws IOException, ContentException {
		if (!methodBodyAwait) {
			throw new ContentException("Attempt to define method body for abstract or native method");
		}
		else {
			markEndOfParameters();
			return body == null ? body = new MethodBody(classId, methodId, getNameTree()) : body;
		}
	}
	
	int getLocalFrameSize() {
		return maxVarDispl;
	}

	void addExceptionRecord(final short fromRange, final short toRange, final short exceptionDescriptor, final short branchAddress) {
		tryTable.add(new short[]{fromRange,toRange,branchAddress,exceptionDescriptor});
	}
	
	void addLineNoRecord(final int lineNo) throws IOException, ContentException {
		lineTable.add(new short[]{getBody().getPC(),(short)lineNo});
	}
	
	void complete() throws IOException, ContentException {
		markEndOfParameters();
	}
	
	void dump(final InOutGrowableByteArray os) throws IOException, ContentException {
		pop();
		os.writeShort(accessFlags);			// Access flags
		os.writeShort(methodDispl);			// Method name
		os.writeShort(signatureDispl);			// Method signature
		os.writeShort((methodBodyAwait ? 1 : 0) + (throwsDispl.length != 0 ? 1 : 0));	// Attribute count
		if (throwsDispl.length != 0) {			// Exceptions present
			os.writeShort(exceptionsWord);			// Exceptions attribute
			os.writeInt(2+2*throwsDispl.length);	// Attribute size
			os.writeShort(throwsList.length);		// Exceptions amount
			for (short item : throwsDispl) {
				os.writeShort(item);				// Exceptions list
			}
		}
		if (methodBodyAwait) {					// Method body present
			os.writeShort(codeWord);					// Code attribute
			os.writeInt(12 + getBody().getCodeSize()	// Attribute size 
					+ (8 * tryTable.size()) 
					+ (6 + 2 + 10 * varTable.size())
					+ (6 + 2 + 4 * lineTable.size())
					);
			os.writeShort(getBody().getStackSize());	// Stack size
			os.writeShort(maxVarDispl+1);				// Local variables size
			os.writeInt(getBody().getCodeSize());		// body size
			getBody().dump(os);							// Body content
			
			os.writeShort(tryTable.size());			// Exception table
			for (short[] item : tryTable) {
				for (short element : item) {
					os.writeShort(element);
				}
			}
			
			os.writeShort(2);							// Attributes kind count
			
			os.writeShort(localVariableTableWord);		// Local variables:
			os.writeInt(2+10*varTable.size());			// Struct size
			os.writeShort(varTable.size());			// Amount of variables
			for (short[] item : varTable) {
				for (short element : item) {
					os.writeShort(element);
				}
			}
			
			os.writeShort(lineNumberTableWord);		// Line nubers:
			os.writeInt(2+4*lineTable.size());			// Struct size
			os.writeShort(lineTable.size());			// Amount of code lines
			for (short[] item : lineTable) {
				for (short element : item) {
					os.writeShort(element);
				}
			}

		}
	}
	
	private void addVar(final short accessFlags, final long varId, final long typeId) throws ContentException {
		if (pushStack.get(0).vars.getRef(varId) != 0) {
			throw new ContentException("Duplicate variable name ["+tree.getName(varId)+"] in this block");
		}
		else {
			final String	signature = InternalUtils.buildFieldSignature(tree,typeId);
			final long		signatureId = tree.placeOrChangeName(signature,new NameDescriptor());
			
			pushStack.get(0).vars.addRef((short)(varDispl+1),varId);
			if (typeId == longId || typeId == doubleId) {	// these types occupy 2 cells in the local var table
				varDispl++;
			}
			if (++varDispl > maxVarDispl) {
				maxVarDispl = varDispl;
			}
			
			try{pushStack.get(0).vars.setCargo(new VarDesc(ccr.asUTF(varId),ccr.asUTF(signatureId)),varId);
			} catch (IOException e) {
				throw new ContentException(e);
			}
		}
	}

	private void markEndOfParameters() throws IOException, ContentException {
		if (!parametersEnded) {
			final long[]	parm = new long[parametersList.size()];
			
			for (int index = 0, maxIndex = parm.length; index < maxIndex; index++) {
				parm[index] = tree.placeOrChangeName(tree.getName(parametersList.get(index)).replace('.','/'),new NameDescriptor());
			}
			final long		signatureId = tree.placeOrChangeName(InternalUtils.buildMethodSignature(tree,(accessFlags & Constants.ACC_STATIC) != 0,returnedTypeId,parm),new NameDescriptor()); 
			final int		classLen = tree.getNameLength(classId), methodLen = tree.getNameLength(methodId), signatureLen = getNameTree().getNameLength(signatureId); 
			final char[]	forShortName = new char[methodLen+signatureLen], forLongName = new char[classLen+1+methodLen+signatureLen];

			tree.getName(methodId,forShortName,0);	// Create and place method with signature in the name tree (short)
			tree.getName(signatureId,forShortName,methodLen);
			tree.placeOrChangeName(forShortName,0,forShortName.length,new NameDescriptor());

			
			tree.getName(classId,forLongName,0);	// Create and place method with signature in the name tree (long)
			forLongName[classLen] = '.';
			tree.getName(methodId,forLongName,classLen+1);
			tree.getName(signatureId,forLongName,classLen+1+methodLen);
			tree.placeOrChangeName(forLongName,0,forLongName.length,new NameDescriptor());

			final boolean	wasConstructor = UnsafedCharUtils.uncheckedCompare(forShortName,0,INIT,0,INIT.length) || UnsafedCharUtils.uncheckedCompare(forShortName,0,CLASS_INIT,0,CLASS_INIT.length);
			
			if (wasConstructor) {	// Special behavior for constructors
				String	className = tree.getName(classId);
				if (className.lastIndexOf('.') >= 0) {
					className = className.substring(className.lastIndexOf('.')+1);
				}
				if ((accessFlags & Constants.ACC_STATIC) != 0) {
					tree.placeOrChangeName(new String(forShortName).replace(CLASS_INIT_STRING,className),new NameDescriptor());
					tree.placeOrChangeName(new String(forLongName).replace(CLASS_INIT_STRING,className),new NameDescriptor());
				}
				else {
					tree.placeOrChangeName(new String(forShortName).replace(INIT_STRING,className),new NameDescriptor());
					tree.placeOrChangeName(new String(forLongName).replace(INIT_STRING,className),new NameDescriptor());
				}
			}
			
			signatureDispl = ccr.asUTF(signatureId);
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
