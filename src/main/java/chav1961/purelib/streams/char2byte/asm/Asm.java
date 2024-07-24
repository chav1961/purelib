package chav1961.purelib.streams.char2byte.asm;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.char2byte.asm.macro.MacroClassLoader;
import chav1961.purelib.streams.char2byte.asm.macro.Macros;

public class Asm implements LineByLineProcessorCallback, Closeable, Flushable {
	public static final AtomicInteger	AI = new AtomicInteger(1);
	
	private final ClassLoader			owner;
	private final OutputStream			os;
	private final Writer				diagnostics;
	private final ClassContainer		cc;
	private final ClassDescriptionRepo	cdr;
	private final SyntaxTreeInterface<Macros>	macros;	
	private final LineParser			lp;
	private final MacroClassLoader		asmLoader;
	private final boolean				wasCloned;
	private boolean						wasDump = false;
	
	public Asm(final ClassLoader owner, final OutputStream os) throws IOException {
		this.owner = owner;
		this.wasCloned = false;
		this.os = os;
		this.diagnostics = null;
		this.macros = new AndOrTree<>();
		
		try{
			this.cdr = new ClassDescriptionRepo();
			this.cc = new ClassContainer(cdr);
			this.asmLoader = createLoader();
			this.lp = new LineParser(owner,cc,cdr,macros,asmLoader);
		} catch (ContentException e) {
			throw new IOException(e.getMessage(),e);
		}
	}

	public Asm(final ClassLoader owner, final OutputStream os, final Writer diagnostics) throws IOException {
		this.owner = owner;
		this.wasCloned = false;
		this.os = os;
		this.diagnostics = diagnostics;
		this.macros = new AndOrTree<>();	
		
		try{
			this.cdr = new ClassDescriptionRepo(diagnostics);
			this.cc = new ClassContainer(cdr);
			this.asmLoader = createLoader();
			this.lp = new LineParser(owner,cc,cdr,macros,asmLoader,diagnostics);
		} catch (ContentException e) {
			throw new IOException(e.getMessage(),e);
		}
	}
	
	public Asm(final ClassLoader owner, final Asm asm, final OutputStream os) throws IOException {
		this.owner = owner;
		this.wasCloned = true;
		this.os = os;
		this.diagnostics = asm.diagnostics;
		this.cdr = asm.cdr;
		this.cc = new ClassContainer(cdr);
		this.asmLoader = asm.asmLoader;
		this.macros = asm.macros;	
		try{
			this.lp = diagnostics != null ? new LineParser(owner,cc,cdr,macros,asmLoader,diagnostics) : new LineParser(owner,cc,cdr,macros,asmLoader);
		} catch (ContentException e) {
			throw new IOException(e.getMessage(),e);
		}
	}

	@Override
	public void processLine(long displacement, int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
		try{
			lp.processLine(displacement,lineNo, data, from, length);
		} catch (Exception exc) {
			lp.printDiagnostics("");
			throw exc;
		}
	}

	@Override
	public void flush() throws IOException {
		if (!wasDump) {
			wasDump = true;
			try{cc.dump(os);
			} catch (ContentException e) {
				e.printStackTrace();
				throw new IOException(e.getMessage());
			}
			os.flush();
		}
		if (diagnostics != null) {
			diagnostics.flush();
		}
	}
	
	@Override
	public void close() throws IOException {
		flush();
		if (!wasCloned) {
			macros.clear();
			asmLoader.close();
			cc.close();
		}
	}
	
	public String getClassName() {
		return cc.getClassName();
	}
	
	public void importClass(final Class<?> clazz) throws ContentException {
		cdr.addDescription(clazz,false);
	}

	public void importClass(final Class<?> clazz, final String refName) throws ContentException {
		importClass(clazz);
		cdr.addClassReference(refName,clazz.getCanonicalName());
	}
	
	private static MacroClassLoader createLoader() {
		return new MacroClassLoader(Thread.currentThread().getContextClassLoader()); 
	}
}
