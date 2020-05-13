package chav1961.purelib.streams.char2byte;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.CharStreamPrinter;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.streams.char2byte.asm.Asm;

/**
 * <p>This class is a Java byte code Assembler. It implements as an usual Writer:</p>
 * <code>
 * try(final OutputStream os = ...;<br>
 * 	   final Writer wr = new AsmWriter(os);{<br>
 * 
 * 		wr.write("      .package mypackage\n");<br>
 * 		wr.write("Test  .class public\n");<br>
 * . . .<br>
 * 		wr.write("Test  .end\n");<br>
 *      wr.flush();
 * }<br>
 * </code>
 * 
 * <p>Calling {@linkplain #flush()} method is required, because it starts compilation of the class. Don't call this method before you fully types the class assembler code</p> 
 * 
 * <p>After closing streams, output stream will contain ready-to-use class description, and you can pass it to {@link java.lang.ClassLoader#defineClass(String,byte[],int,int)}
 * protected method to create new class in the JVM. Syntax of the assembler instructions and pseudocommands will be described later</p>
 * 
 * <p>To increase performance, you can use cloned streams to produce multiple set of classes:</p>  
 * <code>
 * try(final OutputStream os = ...;<br>
 * 	   final Writer wr = new AsmWriter(os);{<br>
 * 
 *     try(final OutputStream os1 = ...;<br>
 * 	       final Writer wr1 = wr.clone(os1);{<br>
 * 		
 *   		wr1.write("      .package mypackage\n");<br>
 * 	    	wr1.write("Test1 .class public\n");<br>
 *      . . .<br>
 * 		    wr1.write("Test1 .end\n");<br>
 *      	wr1.flush();
 *    }
 * . . .   
 *     try(final OutputStream os2 = ...;<br>
 * 	       final Writer wr2 = wr.clone(os2);{<br>
 * 		
 *   		wr2.write("      .package mypackage\n");<br>
 * 	    	wr2.write("Test2 .class public\n");<br>
 *      . . .<br>
 * 		    wr2.write("Test2 .end\n");<br>
 *      	wr2.flush();
 *    }
 * }<br>
 * </code>
 * 
 * <p>Cloned streams always inherit all the classes imported by main AsmWriter instance and all the macros loaded by it.</p>
 * 
 * <p>Current version of the class implements Java 1.7-compatible class file format with some restrictions:</p>
 * <ul>
 * <li>byte code commands <b>invokedynamic</b> and <b>wide</b> are not supported in the current version (I have no any plans to support invokedynamic, but wide will be implemented soon)</li>  
 * <li>debugging information (source file, line numbers etc) are not included in the class file</li>  
 * </ul>
 * 
 * <p>Any syntax or semantic errors in the source will produce an {@link java.io.IOException}. The {@link java.io.IOException#getCause()} method will return an instance of {@link SyntaxException} in this case.</p>
 * 
 * <p><b>Don't use</b> this class in the {@link java.io.PrintWriter} or {@link java.io.PrintStream} classes, because these classes always suppress any I/O exceptions on
 * it's methods, so you can loose your assembler error when you use it</p> 
 *    
 * <p>I not guarantee, that this implementation is free of any bugs, and nice to get any feedback about</p>
 * 
 * <p>This class is not thread-safe</p> 
 * 
 * @see java.lang.ClassLoader
 * @see chav1961.purelib.streams JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.4
 */

public class AsmWriter extends Writer implements CharStreamPrinter<AsmWriter> {
	private static final char[]			LINE_SEPARATOR = System.lineSeparator().toCharArray();
	private static final char[]			TRUE = "true".toCharArray();
	private static final char[]			FALSE = "false".toCharArray();
	private static final char[]			NULL = "null".toCharArray();
	
	private final OutputStream			os;
	private final LineByLineProcessor	lblp = new LineByLineProcessor(new LineByLineProcessorCallback() {
													@Override
													public void processLine(long displacement, int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
														process(displacement,lineNo,data,from,length);
													}
												}
										);
	private final ClassLoader			owner;
	private final Asm					asm;
	private final boolean				cloned;
	private boolean						built = false;

	/**
	 * <p>Create assembly writer instance</p>
	 * @param os output stream to write content to
	 * @throws NullPointerException when output stream is null
	 * @throws IOException on any I/O errors
	 */
	public AsmWriter(final OutputStream os) throws NullPointerException, IOException {
		this(AsmWriter.class.getClassLoader(),os);
	}	
	
	/**
	 * <p>Create assembly writer instance</p>
	 * @param owner owner of the writer code. All .import directives will be processed from it's content
	 * @param os output stream to write content to
	 * @throws NullPointerException when output stream is null
	 * @throws IOException on any I/O errors
	 * @since 0.0.4
	 */
	public AsmWriter(final ClassLoader owner, final OutputStream os) throws NullPointerException, IOException {
		if (owner == null) {
			throw new NullPointerException("Class lloader owner can't be null"); 
		}
		else if (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else {
			this.owner = owner;
			this.os = os;			
			this.asm = new Asm(owner,os);
			this.cloned = false;
		}
	}

	/**
	 * <p>Create assembly writer instance</p>
	 * @param os output stream to write content to
	 * @param diagnostics diagnostic stream to send errors to
	 * @throws NullPointerException when any parameters are null
	 * @throws IOException on any I/O errors
	 */
	public AsmWriter(final OutputStream os, final Writer diagnostics) throws NullPointerException, IOException {
		this(AsmWriter.class.getClassLoader(),os,diagnostics);
	}
	
	/**
	 * <p>Create assembly writer instance</p>
	 * @param owner owner of the writer code. All .import directives will be processed from it's content
	 * @param os output stream to write content to
	 * @param diagnostics diagnostic stream to send errors to
	 * @throws NullPointerException when any parameters are null
	 * @throws IOException on any I/O errors
	 * @since 0.0.4
	 */
	public AsmWriter(final ClassLoader owner, final OutputStream os, final Writer diagnostics) throws NullPointerException, IOException {
		if (owner == null) {
			throw new NullPointerException("Class lloader owner can't be null"); 
		}
		else if (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else if (diagnostics == null) {
			throw new NullPointerException("DIagnostics stream can't be null"); 
		}
		else {
			this.owner = owner;
			this.os = os;
			this.asm = new Asm(owner,os,diagnostics);
			this.cloned = false;
		}
	}
	
	private AsmWriter(final ClassLoader owner, final Asm asm, final OutputStream os) throws NullPointerException, IOException {
		this.owner = owner == null ? this.getClass().getClassLoader() : owner;
		this.os = os;
		this.asm = asm;
		this.cloned = true;
	}

	/**
	 * <p>Clone writer with all imports and macros were defined earlier. Uses to reduce time and resources for new instances of the {@linkplain AsmWriter}</p>
	 * @param os output stream to write content to
	 * @return new writer instance
	 * @throws NullPointerException when output stream is null
	 * @throws IllegalStateException when output stream is null
	 * @throws IOException on any I/O errors
	 * @since 0.0.2
	 */
	public AsmWriter clone(final OutputStream os) throws NullPointerException, IllegalStateException, IOException {
		return clone(this.getClass().getClassLoader(),os);
	}
	
	/**
	 * <p>Clone writer with all imports and macros were defined earlier. Uses to reduce time and resources for new instances of the {@linkplain AsmWriter}</p>
	 * @param owner owner of the writer code. All .import directives will be processed from it's content
	 * @param os output stream to write content to
	 * @return new writer instance
	 * @throws NullPointerException when output stream is null
	 * @throws IllegalStateException when output stream is null
	 * @throws IOException on any I/O errors
	 * @since 0.0.4
	 */
	public AsmWriter clone(final ClassLoader owner, final OutputStream os) throws NullPointerException, IllegalStateException, IOException {
		if (cloned) {
			throw new IllegalStateException("Can't clone stream already cloned");
		}
		else if (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else {
			return new AsmWriter(owner,new Asm(owner,asm,os),os);
		}
	}

	@Override
	public void write(final char[] cbuf, final int off, final int len) throws IOException {
		if (cbuf == null) {
			throw new NullPointerException("Array to write can't be null"); 
		}
		else if (cbuf.length > 0) {
			try{lblp.write(cbuf,off,len);
			} catch (SyntaxException e) {
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public void write(final String str, final int off, final int len) throws IOException {
		if (str == null) {
			throw new NullPointerException("String to write can't be null"); 
		}
		else if (!str.isEmpty()) {
			write(str.toCharArray(),off,len);
		}
	}	
	
	@Override
    public void write(int c) throws IOException {
        write(new char[]{(char) c},0,1);
    }	

	@Override
	public void flush() throws IOException {
	}

	public void build() throws IOException {
		if (!built) {
			asm.flush();
			os.flush();
			built = true;
		}
	}
	
	@Override
	public void close() throws IOException {
		build();
		asm.close();
	}
	
	/**
	 * <p>Get class name compiled during last write</p>
	 * @return class name compiled. Can't be null
	 */
	public String getClassName() {
		return asm.getClassName();
	}
	
	/**
	 * <p>Add class to imported classes in the assembly compiler</p>
	 * @param clazz class to add
	 * @throws ContentException on duplicated class
	 * @throws NullPointerException when class to add is null
	 */
	public void importClass(final Class<?> clazz) throws ContentException, NullPointerException {
		if (clazz == null) {
			throw new NullPointerException("Class to import can't be null");
		}
		else {
			asm.importClass(clazz);
		}
	}

	/**
	 * <p>Add class to imported classes in the assembly compiler</p>
	 * @param clazz class to add
	 * @param refName reference name to use in '@<class>' clause 
	 * @throws ContentException on duplicated class or reference name
	 * @throws NullPointerException when class to add is null
	 * @throws IllegalArgumentException when reference name is null or empty
	 * @since 0.0.4
	 */
	public void importClass(final Class<?> clazz, final String refName) throws ContentException, NullPointerException, IllegalArgumentException {
		if (clazz == null) {
			throw new NullPointerException("Class to import can't be null");
		}
		else if (refName == null || refName.isEmpty()) {
			throw new IllegalArgumentException("Reference name for class can't be null");
		}
		else {
			asm.importClass(clazz,refName);
		}
	}
	
	private void process(final long displacement, final int lineNo, final char[] data, final int from, final int length) throws IOException {
		try{asm.processLine(displacement, lineNo, data, from, length);
		} catch (SyntaxException e) {
			throw new IOException(e.getMessage(),e);
		}
	}

	@Override
	public AsmWriter println() throws PrintingException {
		try{lblp.write(LINE_SEPARATOR,0,LINE_SEPARATOR.length);
			return this;
		} catch (SyntaxException | IOException e) {
			throw new PrintingException(e.getLocalizedMessage());
		}
	}

	@Override
	public AsmWriter print(final char data) throws PrintingException {
		try{lblp.write(new char[] {data},0,1);
			return this;
		} catch (SyntaxException | IOException e) {
			throw new PrintingException(e.getLocalizedMessage());
		}
	}

	@Override
	public AsmWriter println(final char data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public AsmWriter print(final byte data) throws PrintingException {
		return print((long)data);
	}

	@Override
	public AsmWriter println(final byte data) throws PrintingException {
		return println((long)data);
	}

	@Override
	public AsmWriter print(final short data) throws PrintingException {
		return print((long)data);
	}

	@Override
	public AsmWriter println(final short data) throws PrintingException {
		return println((long)data);
	}

	@Override
	public AsmWriter print(final int data) throws PrintingException {
		return print((long)data);
	}

	@Override
	public AsmWriter println(final int data) throws PrintingException {
		return println((long)data);
	}

	@Override
	public AsmWriter print(final long data) throws PrintingException {
		final char[]	content = new char[20];
		int				len = CharUtils.printLong(content,0,data,true);
		
		try{lblp.write(content,0,len);
			return this;
		} catch (SyntaxException | IOException e) {
			throw new PrintingException(e.getLocalizedMessage());
		}
	}

	@Override
	public AsmWriter println(final long data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public AsmWriter print(final float data) throws PrintingException {
		return print((double)data);
	}

	@Override
	public AsmWriter println(final float data) throws PrintingException {
		return println((double)data);
	}

	@Override
	public AsmWriter print(final double data) throws PrintingException {
		char[]	content = new char[20];
		int		len = CharUtils.printDouble(content,0,data,true);
		
		if (len < 0) {
			content = new char[2 - len];
			len = CharUtils.printDouble(content,0,data,true);
		}
		try{lblp.write(content,0,len);
			return this;
		} catch (SyntaxException | IOException e) {
			throw new PrintingException(e.getLocalizedMessage());
		}
	}

	@Override
	public AsmWriter println(final double data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public AsmWriter print(final boolean data) throws PrintingException {
		return data ? print(TRUE,0,TRUE.length) : print(FALSE,0,FALSE.length);  
	}

	@Override
	public AsmWriter println(final boolean data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public AsmWriter print(final String data) throws PrintingException {
		return data == null ? print(NULL,0,NULL.length) : print(data,0,data.length()) ;
	}

	@Override
	public AsmWriter println(final String data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public AsmWriter print(final String data, final int from, final int len) throws PrintingException, StringIndexOutOfBoundsException {
		if (data == null) {
			return print(NULL,0,NULL.length);
		}
		else {
			return print(data.toCharArray(),from,len);
		}
	}

	@Override
	public AsmWriter println(final String data, final int from, final int len) throws PrintingException, StringIndexOutOfBoundsException {
		print(data,from,len);
		return println();
	}

	@Override
	public AsmWriter print(final char[] data) throws PrintingException {
		return data == null ? print(NULL,0,NULL.length) : print(data,0,data.length) ;
	}

	@Override
	public AsmWriter println(final char[] data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public AsmWriter print(final char[] data, final int from, final int len) throws PrintingException, ArrayIndexOutOfBoundsException {
		if (data == null) {
			return print(NULL,0,NULL.length);
		}
		else {
			try{lblp.write(data,from,len);
				return this;
			} catch (SyntaxException | IOException e) {
				throw new PrintingException(e.getLocalizedMessage());
			}
		}
	}

	@Override
	public AsmWriter println(final char[] data, final int from, final int len) throws PrintingException, ArrayIndexOutOfBoundsException {
		print(data,from,len);
		return println();
	}

	@Override
	public AsmWriter print(final Object data) throws PrintingException {
		return data == null ? print(NULL,0,NULL.length) : print(data.toString());
	}

	@Override
	public AsmWriter println(final Object data) throws PrintingException {
		print(data);
		return println();
	}
}
