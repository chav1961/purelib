package chav1961.purelib.basic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.script.Bindings;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.char2byte.AsmWriter;

class AsmScriptEngine extends AbstractScriptEngine {
	private AsmWriter				asmWriter = null;
	private byte[]					content = null;
	private Class<?>				clazz = null;
	private SimpleURLClassLoader	scl = new SimpleURLClassLoader(new URL[0]);
	
	AsmScriptEngine(final ScriptEngineFactory factory) throws IOException {
		super(factory);
	}

	@Override
	public void upload(final OutputStream target) throws ScriptException, NullPointerException, IllegalStateException {
		if (target == null) {
			throw new NullPointerException("Target stream can't be null");
		}
		else if (clazz == null) {
			throw new IllegalStateException("Uploade is unavailable, because no any code were compiled or download yet");
		}
		else {
			try{target.write(content);
				target.flush();
			} catch (IOException e) {
				throw new ScriptException(e);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void download(final InputStream source) throws ScriptException {
		if (source == null) {
			throw new NullPointerException("Source stream can't be null");
		}
		else {
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				Utils.copyStream(source,baos);
				content = baos.toByteArray();
				clazz = scl.createClass(content);
			} catch (IOException e) {
				throw new ScriptException(e);
			}
		}
	}

	@Override
	public void close() throws IOException {
		content = null;
		clazz = null;
		if (asmWriter != null) {
			asmWriter.close();
			asmWriter = null;
		}
		super.close();
	}
	
	@Override
	protected Reader beforeCompile(final Reader reader, final OutputStream os) throws NullPointerException, IOException {
		asmWriter = new AsmWriter(os);
		return reader;
	}
	
	@Override
	protected void processLineInternal(final long displacement, final int lineNo, final char[] data, final int from, final int length) throws IOException, SyntaxException {
		asmWriter.write(data,from,length);
	}

	@Override
	protected void afterCompile(final Reader reader, final OutputStream os) throws IOException {
		asmWriter.flush();
		asmWriter.close();
		asmWriter = null;
	}

	protected Object executeInternal(final String[] parameters, final Bindings bindings) throws ScriptException {
		try{final Method	m = clazz.getMethod("main",Bindings.class,String[].class);
			
			return m.invoke(null,bindings,parameters);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exc) {
			throw new ScriptException(exc); 
		}
	}
}
