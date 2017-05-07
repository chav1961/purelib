package chav1961.purelib.streams.char2byte;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.AsmSyntaxException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.char2byte.asm.Asm;

/**
 * <p>This class is a Java byte code Assembler. It implements as an usual Writer:</p>
 * <code>
 * try(final OutputStream os = ...;<br>
 * 	   final Writer wr = new AsmWriter(os);<br>
 * 	   final PrintWriter pw = new PrintWriter(wr)) {<br>
 * 
 * 		pw.println("      .package mypackage");<br>
 * 		pw.println("Test  .class public");<br>
 * . . .<br>
 * 		pw.println("Test  .end");<br>
 * }<br>
 * </code>
 * 
 * <p>After closing streams, output stream will contain ready-to-use class description, and you can pass it to {@link java.lang.ClassLoader#defineClass(String,byte[],int,int)}
 * protected method to create new class in the JVM. Syntax of the assembler instructions and pseudocommands will be described later</p>
 *    
 * <p>I not guarantee, that this implementation is free of any bugs, and nice to get any feedback about</p>
 * 
 * <p>This class is not thread-safe</p> 
 * 
 * @see java.lang.ClassLoader
 * @see chav1961.purelib.streams JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class AsmWriter extends Writer {
	private final OutputStream			os;
	private final LineByLineProcessor	lblp = new LineByLineProcessor((lineNo,data,from,len)->process(lineNo,data,from,len));
	private final Asm					asm;

	public AsmWriter(final OutputStream arg0) throws AsmSyntaxException {
		this.os = arg0;
		this.asm = new Asm(os);
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
	
	
	private void process(final int lineNo, final char[] data, final int from, final int length) throws IOException {
		try{asm.processLine(lineNo, data, from, length);
		} catch (SyntaxException e) {
			throw new IOException(e.getMessage(),e);
		}
	}
}
