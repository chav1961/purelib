package chav1961.purelib.streams.char2byte.asm;


import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.cdb.JavaByteCodeConstants;
import chav1961.purelib.cdb.JavaClassVersion;
import chav1961.purelib.cdb.JavaByteCodeConstants.JavaAttributeType;
import chav1961.purelib.streams.char2byte.asm.LongIdTree.LongIdTreeNode;
import chav1961.purelib.streams.char2byte.asm.StackAndVarRepo.StackMapRecord;

class MethodDescriptor implements Closeable {
	private static final String					INIT_STRING = "<init>"; 
	private static final String					CLASS_INIT_STRING = "<clinit>";

	final short									accessFlags;
	final short									specialFlags;

	private final JavaClassVersion				version;
	private final List<Long>					parametersList = new ArrayList<>();
	private final List<StackLevel>				pushStack = new ArrayList<>();
	private final List<short[]>					tryTable = new ArrayList<>();	
	private final List<short[]>					varTable = new ArrayList<>();	
	private final List<short[]>					lineTable = new ArrayList<>();	
	private final List<StackMapRecord>			stackMaps = new ArrayList<>();
	private final SyntaxTreeInterface<NameDescriptor>	tree;
	private final ClassConstantsRepo			ccr;
	private final StackAndVarRepo				stackAndVar = new StackAndVarRepo((a,b,c,d)->{});
	private final long							classId, methodId, returnedTypeId;
	private final long							longId, doubleId, thisId;
	private final short							methodDispl;
	private final long[]						throwsList;
	private final short[]						throwsDispl;
	private final short							exceptionsWord, codeWord, localVariableTableWord, lineNumberTableWord, stackMapTableWord;
	private final boolean						methodBodyAwait;
	
	private MethodBody							body = null;
	private short								varDispl = 0, maxVarDispl = 0;
	private short								signatureDispl;
	private boolean								parametersEnded = false, needVarsTable = false, needStackMapTable = false;
	
	MethodDescriptor(final short majorVersion, final short minorVersion, final SyntaxTreeInterface<NameDescriptor> tree, final ClassConstantsRepo ccr, final short accessFlags, final short specialFlags, final long classId, final long methodId, final long returnTypeId, final long... throwsList) throws IOException, ContentException {
		final int	tLen = throwsList.length;
		
		this.version = new JavaClassVersion(majorVersion, minorVersion);
		this.tree = tree;						
		this.ccr = ccr;	
		this.accessFlags = accessFlags;
		this.specialFlags = specialFlags;
		this.classId = classId;
		this.methodId = methodId;
		this.returnedTypeId = returnTypeId;		
		this.throwsList = throwsList;
		this.methodDispl = ccr.asUTF(methodId);	
		
		this.longId = tree.seekName(LineParser.LONG, 0, LineParser.LONG.length);
		this.doubleId = tree.seekName(LineParser.DOUBLE, 0, LineParser.DOUBLE.length);
		this.thisId = tree.seekName(LineParser.THIS, 0, LineParser.THIS.length);
		
		this.throwsDispl = new short[tLen];
		for (int index = 0; index < tLen; index++) {
			this.throwsDispl[index] = ccr.asClassDescription(tree.placeOrChangeName((CharSequence)tree.getName(throwsList[index]).replace('.','/'), new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
		}
		this.exceptionsWord = ccr.asUTF(tree.placeOrChangeName(JavaByteCodeConstants.ATTRIBUTE_Exceptions, 0, JavaByteCodeConstants.ATTRIBUTE_Exceptions.length, new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
		this.codeWord = ccr.asUTF(tree.placeOrChangeName(JavaByteCodeConstants.ATTRIBUTE_Code, 0, JavaByteCodeConstants.ATTRIBUTE_Code.length, new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
		this.localVariableTableWord = ccr.asUTF(tree.placeOrChangeName(JavaByteCodeConstants.ATTRIBUTE_LocalVariableTable, 0, JavaByteCodeConstants.ATTRIBUTE_LocalVariableTable.length, new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
		this.lineNumberTableWord =  ccr.asUTF(tree.placeOrChangeName(JavaByteCodeConstants.ATTRIBUTE_LineNumberTable, 0, JavaByteCodeConstants.ATTRIBUTE_LineNumberTable.length, new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
		this.stackMapTableWord =  ccr.asUTF(tree.placeOrChangeName(JavaByteCodeConstants.ATTRIBUTE_StackMapTable, 0, JavaByteCodeConstants.ATTRIBUTE_StackMapTable.length, new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)));
		
		this.methodBodyAwait = (accessFlags & (JavaByteCodeConstants.ACC_NATIVE | JavaByteCodeConstants.ACC_ABSTRACT)) == 0;
		pushStack.add(0,new StackLevel(varDispl,(short)0));
		if ((accessFlags & JavaByteCodeConstants.ACC_STATIC) == 0) {
			addParameterDeclaration((short)0, thisId, classId);
		}
		this.needStackMapTable = needStackMapTable(majorVersion,minorVersion);  
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
		return (accessFlags & JavaByteCodeConstants.ACC_ABSTRACT) != 0; 
	}
	
	boolean isStatic() {
		return (accessFlags & JavaByteCodeConstants.ACC_STATIC) != 0; 
	}

	boolean isBootstrap() {
		return (specialFlags & LineParser.SPECIAL_FLAG_BOOTSTRAP) != 0; 
	}
	
	StackLevel getTopStack() {
		return pushStack.get(0);
	}

	long[] getParametersList() {
		return Utils.unwrapArray(parametersList.toArray(new Long[parametersList.size()]));
	}

	long getReturnedType() {
		return returnedTypeId;
	}
	
	SyntaxTreeInterface<NameDescriptor> getNameTree(){
		return tree;
	}
	
	void setStackSize(final short stackSize, final boolean needVarTable) {
		body = new MethodBody(classId, methodId, getNameTree(),this.needVarsTable = needVarTable,stackSize,stackAndVar);
	}
	
	void addParameterDeclaration(final short accessFlags, final long parameterId, final long typeId) throws ContentException, IOException {
		if (parametersEnded) {
			throw new ContentException("Parameters declaration need be before any other declarations");
		}
		else {
			addVar(accessFlags, parameterId, typeId, true);
			parametersList.add(typeId);
		}
	}
	
	void push() throws IOException, ContentException {
		markEndOfParameters();
		pushStack.add(0,new StackLevel(varDispl,getBody().getPC()));
	}

	void addVarDeclaration(final short accessFlags, final long varId, final long typeId) throws ContentException, IOException {
		markEndOfParameters();
		addVar(accessFlags, varId, typeId, false);
	}

	short getVarDispl(final long varId) throws ContentException {
		short	result = 0;
		
		for (int index = 0, maxIndex = pushStack.size(); index < maxIndex; index++) {
			if ((result = pushStack.get(index).vars.getRef(varId)) != 0) {
				return (short)(result-1/* -1 - problem with LongIdMap */);
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
	
	AbstractMethodBody getBody() throws ContentException {
		if (!methodBodyAwait) {
			throw new ContentException("Attempt to define method body for abstract or native method");
		}
		else {
			markEndOfParameters();
			return body == null ? body = new MethodBody(classId, methodId, getNameTree(), false, stackAndVar) : body;
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

	void addStackMapRecord() throws ContentException {
		stackMaps.add(this.getBody().getStackAndVarRepo().createStackMapRecord(this.getBody().getPC()));
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
		if (methodBodyAwait) {						// Method body present
			int		totalAttrubuteSize = 12 + getBody().getCodeSize() + + (8 * tryTable.size());	// Mandatory attributes
			int		stackMapSize = 0;
			boolean	needPrintVarsTable = false, needPrintLineTable = false, needPrintStackMapTable = false;
			short	totalAttrCount = 0;

			if (needVarsTable) {
				totalAttrubuteSize += (2 + 4 + 2 + 10 * varTable.size());
				needPrintVarsTable = true;
				totalAttrCount++;
			}
			if (!lineTable.isEmpty()) {
				totalAttrubuteSize += (2 + 4 + 2 + 4 * lineTable.size()); 
				needPrintLineTable = true;
				totalAttrCount++;
			}
			if (needStackMapTable && !stackMaps.isEmpty()) {
				totalAttrubuteSize += (2 + 4 + 2);
				for (StackMapRecord item : stackMaps) {
					final int	size = item.getRecordSize();
					
					totalAttrubuteSize += size;
					stackMapSize += size;
				}
				needPrintStackMapTable = true;
				totalAttrCount++;
			}
			
			os.writeShort(codeWord);					// Code attribute
			os.writeInt(totalAttrubuteSize);			// Total attribute size
			os.writeShort(getBody().getStackSize());	// Stack size
			os.writeShort(maxVarDispl+1);				// Local variables size
			os.writeInt(getBody().getCodeSize());		// body size
			getBody().dump(os);							// Body content
			
			os.writeShort(tryTable.size());				// Exception table
			for (short[] item : tryTable) {
				for (short element : item) {
					os.writeShort(element);
				}
			}
			
			os.writeShort(totalAttrCount);				// Attributes kind count
			
			if (needPrintVarsTable) {
				os.writeShort(localVariableTableWord);	// Local variables:
				os.writeInt(2+10*varTable.size());		// Struct size
				os.writeShort(varTable.size());			// Amount of variables
				for (short[] item : varTable) {
					for (short element : item) {
						os.writeShort(element);
					}
				}
			}
			
			if (needPrintLineTable) {
				os.writeShort(lineNumberTableWord);		// Line numbers:
				os.writeInt(2+4*lineTable.size());		// Struct size
				os.writeShort(lineTable.size());		// Amount of code lines
				for (short[] item : lineTable) {
					for (short element : item) {
						os.writeShort(element);
					}
				}
			}

			if (needPrintStackMapTable) {
				os.writeShort(stackMapTableWord);		// Stack map:
				os.writeInt(2+stackMapSize);			// Struct size
				os.writeShort(stackMaps.size());		// Amount of stack map entries
				for (StackMapRecord item : stackMaps) {	// Stack map entries
					item.write(os);
				}
			}
		}
	}
	
	private void addVar(final short accessFlags, final long varId, final long typeId, final boolean markAsPrepared) throws ContentException, IOException {
		if (pushStack.get(0).vars.getRef(varId) != 0) {
			throw new ContentException("Duplicate variable name ["+tree.getName(varId)+"] in this block");
		}
		else {
			final String	signature = InternalUtils.buildFieldSignature(tree, typeId);
			final int		currentVarDispl = varDispl, currentVarType = InternalUtils.fieldSignature2Type(signature);
			final long		signatureId;
			final short		typeRef; 
			
			if (signature.startsWith("[")) {
				signatureId = tree.placeOrChangeName((CharSequence)signature,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
				typeRef = ccr.asClassDescription(signatureId);
			}
			else {
				final String	className = tree.getName(typeId).replace('.', '/');
				signatureId = tree.placeOrChangeName((CharSequence)className,new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
				typeRef = ccr.asClassDescription(signatureId);
			}
			
			pushStack.get(0).vars.addRef((short)(currentVarDispl+1/* +1 - problem with LongIdMap!*/),varId);
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
			stackAndVar.addVar(currentVarDispl, currentVarType, typeRef, markAsPrepared);
		}
	}

	private void markEndOfParameters() throws ContentException {
		if (!parametersEnded) {
			final long[]	parm = new long[parametersList.size()];
			
			for (int index = 0, maxIndex = parm.length; index < maxIndex; index++) {
				parm[index] = tree.placeOrChangeName((CharSequence)tree.getName(parametersList.get(index)).replace('.','/'), new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
			}
			final long		signatureId = tree.placeOrChangeName((CharSequence)InternalUtils.buildMethodSignature(tree,(accessFlags & JavaByteCodeConstants.ACC_STATIC) != 0,returnedTypeId,parm), new NameDescriptor(CompilerUtils.CLASSTYPE_VOID)); 
			final int		classLen = tree.getNameLength(classId), methodLen = tree.getNameLength(methodId), signatureLen = getNameTree().getNameLength(signatureId); 
			final char[]	forShortName = new char[methodLen+signatureLen], forLongName = new char[classLen+1+methodLen+signatureLen];

			tree.getName(methodId, forShortName, 0);	// Create and place method with signature in the name tree (short)
			tree.getName(signatureId, forShortName, methodLen);
			tree.placeOrChangeName(forShortName, 0, forShortName.length, new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));

			tree.getName(classId, forLongName, 0);		// Create and place method with signature in the name tree (long)
			forLongName[classLen] = '.';
			tree.getName(methodId, forLongName, classLen+1);
			tree.getName(signatureId, forLongName, classLen+1+methodLen);
			tree.placeOrChangeName(forLongName, 0, forLongName.length, new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));

			final boolean	wasConstructor = UnsafedCharUtils.uncheckedCompare(forShortName, 0, LineParser.CONSTRUCTOR, 0, LineParser.CONSTRUCTOR.length) 
											|| UnsafedCharUtils.uncheckedCompare(forShortName, 0, LineParser.CLASS_CONSTRUCTOR, 0, LineParser.CLASS_CONSTRUCTOR.length);
			
			if (wasConstructor) {	// Special behavior for constructors
				String	className = tree.getName(classId);
				if (className.lastIndexOf('.') >= 0) {
					className = className.substring(className.lastIndexOf('.')+1);
				}
				if ((accessFlags & JavaByteCodeConstants.ACC_STATIC) != 0) {
					tree.placeOrChangeName((CharSequence)new String(forShortName).replace(CLASS_INIT_STRING,className), new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
					tree.placeOrChangeName((CharSequence)new String(forLongName).replace(CLASS_INIT_STRING,className), new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
				}
				else {
					tree.placeOrChangeName((CharSequence)new String(forShortName).replace(INIT_STRING,className),new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
					tree.placeOrChangeName((CharSequence)new String(forLongName).replace(INIT_STRING,className),new NameDescriptor(CompilerUtils.CLASSTYPE_VOID));
				}
			}
			
			try{signatureDispl = ccr.asUTF(signatureId);
			} catch (IOException e) {
				throw new ContentException(e.getMessage(),e);
			}
			parametersEnded = true;
			
			//
		}
	}

	private boolean needStackMapTable(final short majorVersion, final short minorVersion) {
		return JavaAttributeType.StackMapTable.getFromClassVersion().compareTo(new JavaClassVersion(majorVersion, minorVersion)) <= 0;
	}
	
	private static class StackLevel {
		short				varDispl, pc;
		LongIdTree<VarDesc>	vars = new LongIdTree<>(1);
		
		public StackLevel(final short varDispl, final short pc) {
			this.varDispl = varDispl;
		}
	}
	
	static class VarDesc {
		short nameRef;
		short typeRef;
		
		public VarDesc(short nameRef, short typeRef) {
			this.nameRef = nameRef;
			this.typeRef = typeRef;
		}
	}
}
