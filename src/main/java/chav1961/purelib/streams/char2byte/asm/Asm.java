package chav1961.purelib.streams.char2byte.asm;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

import chav1961.purelib.basic.exceptions.AsmSyntaxException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;

public class Asm implements LineByLineProcessorCallback, Closeable, Flushable {
	private final OutputStream		os;
	private final ClassContainer	cc = new ClassContainer();
	private final LineParser		lp;
	private boolean					wasDump = false;
	
	public Asm(final OutputStream os) throws AsmSyntaxException {
		this.os = os;
		this.lp = new LineParser(cc);
	}
	
	@Override
	public void processLine(int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
		lp.processLine(lineNo, data, from, length);
	}

	@Override
	public void flush() throws IOException {
		if (!wasDump) {
			wasDump = true;
			cc.dump(os);
			os.flush();
		}
	}
	
	@Override
	public void close() throws IOException {
		flush();
	}
}
