package chav1961.purelib.streams.char2byte;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
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
 * @lastUpdate 0.0.3
 */

public class AsmWriter extends Writer {
	private final OutputStream			os;
	private final LineByLineProcessor	lblp = new LineByLineProcessor(new LineByLineProcessorCallback() {
													@Override
													public void processLine(long displacement, int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
														process(displacement,lineNo,data,from,length);
													}
												}
										);
	private final Asm					asm;
	private final boolean				cloned;

	/**
	 * <p>Create assembly writer instance</p>
	 * @param os output stream to write content to
	 * @throws NullPointerException when output stream is null
	 * @throws IOException on any I/O errors
	 */
	public AsmWriter(final OutputStream os) throws NullPointerException, IOException {
		if (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else {
			this.os = os;
			this.asm = new Asm(os);
//			this.diagnostics = null;
			this.cloned = false;
		}
	}

	/**
	 * <p>Create assembly writer instance</p>
	 * @param os output stream to write content to
	 * @param diagnostics dusgnostic stream to send errors to
	 * @throws NullPointerException when any parameters are null
	 * @throws IOException on any I/O errors
	 */
	public AsmWriter(final OutputStream os, final Writer diagnostics) throws NullPointerException, IOException {
		if (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else if (diagnostics == null) {
			throw new NullPointerException("DIagnostics stream can't be null"); 
		}
		else {
			this.os = os;
			this.asm = new Asm(os,diagnostics);
			this.cloned = false;
		}
	}
	
	private AsmWriter(final Asm asm, final OutputStream os) throws NullPointerException, IOException {
		if (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else {
			this.os = os;
			this.asm = asm;
			this.cloned = true;
		}
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
		if (cloned) {
			throw new IllegalStateException("Can't clone stream already cloned");
		}
		else {
			return new AsmWriter(new Asm(asm,os),os);
		}
	}

	@Override
	public void write(final char[] cbuf, final int off, final int len) throws IOException {
		try{lblp.write(cbuf,off,len);
		} catch (SyntaxException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		write(str.toCharArray(),off,len);
	}	
	
	@Override
    public void write(int c) throws IOException {
        write(new char[]{(char) c},0,1);
    }	

	@Override
	public void flush() throws IOException {
		asm.flush();
		os.flush();
	}

	@Override
	public void close() throws IOException {
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
	 * @param clazz clazz to add
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
	
	private void process(final long displacement, final int lineNo, final char[] data, final int from, final int length) throws IOException {
		try{asm.processLine(displacement, lineNo, data, from, length);
		} catch (SyntaxException e) {
			throw new IOException(e.getMessage(),e);
		}
	}
}
