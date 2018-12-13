package chav1961.purelib.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.BasicScriptEngineController;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.fsys.FileSystemInMemory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class implements basic functionality for the {@link ScriptEngine} interface. It's functionality is oriented to use with the
 * pre-compiled languages, so it has a set of specific protected methods for it:</p>
 * <ul>
 * <li>{@linkplain #beforeCompile(Reader, OutputStream)} method for preparing compilation process and/or pre-processing input content</li>
 * <li>{@linkplain #processLineInternal(int, char[], int, int)} abstract method for line-by-line compilation input content</li>
 * <li>{@linkplain #afterCompile(Reader, OutputStream)} abstract method for creation compiled output and terminating compilation</li>
 * </ul>
 * <p>This class also contains implementation of {@linkplain BasicScriptEngineController} interface, oriented to use with the standard jars content.
 * To start application code form the jars, one of the jar classes must contain a <b>public static</b> Object main({@linkplain Bindings},String[]) method.
 * It's location need be typed in the META-INF/manifest.mf file as a standard {@linkplain Attributes.Name#MAIN_CLASS} key. To implement your own executable 
 * code managing, you need to override {@linkplain BasicScriptEngineController#download(InputStream)} and
 * {@linkplain BasicScriptEngineController#download(InputStream)} methods from {@linkplain BasicScriptEngineController#download(InputStream)} interface
 * and {@linkplain #executeInternal(String[], Bindings)} method of this class. The {@linkplain #executeInternal(String[], Bindings)} method is a <i>kernel</i>
 * of the execution mechanism in this implementation, and all others call it to execute code</p>  
 * 
 * <p>The good examples of the child implementation for the given class is {@link AsmScriptEngine} class in this package.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see ScriptEngine
 * @see AsmScriptEngine
 * @see BasicScriptEngineController
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public abstract class AbstractScriptEngine implements ScriptEngine, BasicScriptEngineController, Closeable {
	private static final int			NON_EXISTENT = -1;

	protected Writer					writer = new OutputStreamWriter(System.out);
	protected Writer					errorWriter = new OutputStreamWriter(System.err);
	protected Reader					reader = new InputStreamReader(System.in);
	
	private final ScriptEngineFactory	factory;
	private final LineByLineProcessorCallback	callback = new LineByLineProcessorCallback() {
													@Override
													public void processLine(int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
														processLineInternal(lineNo,data,from,length);
													}
												};
	private final FileSystemInterface	fs = new FileSystemInMemory();
	private ScriptContext				currentContext = new DefaultScriptContext(reader,writer,errorWriter);
	private InternalClassLoader			loader = null;
	private String						mainClass = null;

	protected AbstractScriptEngine(final ScriptEngineFactory factory) {
		if (factory == null) {
			throw new NullPointerException("Script engine factory can't be null");
		}
		else {
			this.factory = factory;
		}
	}

	protected abstract void processLineInternal(final int lineNo, final char[] data, final int from, final int length) throws IOException, SyntaxException;
	protected abstract void afterCompile(final Reader reader, final OutputStream os) throws IOException;
	
	@Override
	public Object eval(final String script, final ScriptContext context) throws ScriptException {
		if (script == null || script.isEmpty()) {
			throw new NullPointerException("Script string can't be null or empty");
		}
		else {
			try(final Reader	rdr = new StringReader(script)) {
				return eval(rdr,context);
			} catch (IOException e) {
				throw new ScriptException(e);
			}
		}
	}

	@Override
	public Object eval(final Reader reader, final ScriptContext context) throws ScriptException {
		if (reader == null) {
			throw new NullPointerException("Script reader can't be null");
		}
		else if (context == null) {
			throw new NullPointerException("Script context can't be null");
		}
		else {
			final Bindings	bindings = new DefaultBindings();
			
			bindings.putAll(context.getBindings(ScriptContext.GLOBAL_SCOPE));
			bindings.putAll(context.getBindings(ScriptContext.ENGINE_SCOPE));
			return eval(reader,bindings);
		}
	}

	@Override
	public Object eval(final String script) throws ScriptException {
		if (script == null || script.isEmpty()) {
			throw new NullPointerException("Script string can't be null or empty");
		}
		else {
			return eval(script,getContext());
		}
	}

	@Override
	public Object eval(final Reader reader) throws ScriptException {
		if (reader == null) {
			throw new NullPointerException("Script reader can't be null");
		}
		else {
			return eval(reader,getContext());
		}
	}

	@Override
	public Object eval(final String script, final Bindings n) throws ScriptException {
		if (script == null || script.isEmpty()) {
			throw new NullPointerException("Script string can't be null or empty");
		}
		else {
			try(final Reader	rdr = new StringReader(script)) {
				return eval(rdr,n);
			} catch (IOException e) {
				throw new ScriptException(e);
			}
		}
	}

	@Override
	public Object eval(final Reader reader, final Bindings n) throws ScriptException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			compile(reader,baos);
			try(final InputStream	is = new ByteArrayInputStream(baos.toByteArray());) {
				
				download(is);
			}
			return executeInternal(new String[0],n);
		} catch (IOException | SyntaxException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public void put(final String key, final Object value) {
		getContext().setAttribute(key,value,ScriptContext.ENGINE_SCOPE);
	}

	@Override
	public Object get(final String key) {
		return getContext().getAttribute(key);
	}

	@Override
	public Bindings getBindings(final int scope) {
		return getContext().getBindings(scope);
	}

	@Override
	public void setBindings(final Bindings bindings, final int scope) {
		getContext().setBindings(bindings, scope);
	}

	@Override
	public Bindings createBindings() {
		return new DefaultBindings();
	}

	@Override
	public ScriptContext getContext() {
		return currentContext;
	}

	@Override
	public void setContext(final ScriptContext context) {
		if (context == null) {
			throw new NullPointerException("Script context can't be null");
		}
		else {
			currentContext = context;
		}
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}

	@Override
	public Object execute(final String... parameters) throws ScriptException {
		if (parameters == null){
			throw new NullPointerException("Parameters list can' t be null");
		}
		else {
			final Bindings	b = new DefaultBindings();
			
			b.putAll(getBindings(ScriptContext.GLOBAL_SCOPE));
			b.putAll(getBindings(ScriptContext.ENGINE_SCOPE));
			
			return executeInternal(parameters,b);
		}
	}

	@Override
	public void upload(final URI target) throws ScriptException {
		if (target == null) {
			throw new NullPointerException("Target URI can't be null");
		}
		else {
			try{final URLConnection		conn = target.toURL().openConnection();
			
				conn.setDoInput(false);
				conn.setDoOutput(true);
				try(final OutputStream	os = conn.getOutputStream()) {
					
					upload(os);
				}
			} catch (IOException e) {
				throw new ScriptException(e);
			}
		}		
	}

	@Override
	public void upload(final OutputStream target) throws ScriptException, NullPointerException, IllegalStateException {
		if (target == null) {
			throw new NullPointerException("Target stream cant be null");
		}
		else if (mainClass == null) {
			throw new IllegalStateException("Uploade is unavailable, because no any code were compiled or download yet");
		}
		else {
			final Manifest	manifest = new Manifest();
			
			manifest.getMainAttributes().putValue(Attributes.Name.MAIN_CLASS.toString(),mainClass);
			try(final JarOutputStream		jos = new JarOutputStream(target,manifest);
				final FileSystemInterface	fsi = fs.clone().open("/")) {
				
				upload(fsi,jos);
			} catch (IOException e) {
				throw new ScriptException(e);
			}
		}
	}

	@Override
	public void download(final URI source) throws ScriptException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null");
		}
		else {
			try(final InputStream	is = source.toURL().openStream()) {
				
				download(is);
			} catch (IOException e) {
				throw new ScriptException(e);
			}
		}
	}

	@Override
	public void download(final InputStream source) throws ScriptException {
		final InternalClassLoader	newLoader = new InternalClassLoader(this.getClass().getClassLoader(),fs);
		
		try(final JarInputStream	jis = new JarInputStream(source,true){@Override public void close(){}}) {
			final Manifest			manifest = jis.getManifest();
			
			if (manifest.getAttributes(Attributes.Name.MAIN_CLASS.toString()) == null) {
				throw new IOException("Main-class attribute is missing in the source JAR input");
			}
			else {
				JarEntry				je;

				try(final FileSystemInterface	fsi = fs.clone().open("/")) {
					for (String item : fsi.list()) {
						try(final FileSystemInterface	element = fsi.clone().open("./"+item)) {
							element.deleteAll();
						}
					}
				}
				while ((je = jis.getNextJarEntry()) != null) {
					if (je.isDirectory()) {
						try(final FileSystemInterface	fsi = fs.clone().open(je.getName())) {
							fsi.mkDir();
						}
					}
					else {
						try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
							Utils.copyStream(jis,baos);
							
							try(final FileSystemInterface	fsi = fs.clone().open(je.getName()).create()) {
								try(final OutputStream		os = fs.clone().open(je.getName()).create().write()) {
									baos.writeTo(os);
									os.flush();
								}
							}
							if (je.getName().endsWith(".class")) {
								newLoader.prepareClass(je.getName(),baos.toByteArray());
							}
						}
					}
				}
				loader.close();
				loader = newLoader;
				mainClass = manifest.getAttributes(Attributes.Name.MAIN_CLASS.toString()).toString();
			}
		} catch (IOException e) {
			newLoader.close();
			throw new ScriptException(e);
		}
	}
	
	
	@Override
	public void close() throws IOException {
		loader.close();
		fs.close();
	}

	protected ScriptEngineFactory getEngineFactory() {
		return factory;
	}
	
	protected Reader beforeCompile(final Reader reader, final OutputStream os) throws IOException {
		return reader;
	}
	
	protected Object executeInternal(final String[] parameters, final Bindings bindings) throws ScriptException {
		try{final Class<?>	clazz = loader.loadClass(mainClass);
			final Method	m = clazz.getMethod("main",Bindings.class,String[].class);
			
			return m.invoke(null,bindings,parameters);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exc) {
			throw new ScriptException(exc); 
		}
	}

	private void compile(final Reader reader, final OutputStream os) throws IOException, SyntaxException {
		try(final LineByLineProcessor		lblp = new LineByLineProcessor(callback)) {
			
			lblp.write(beforeCompile(reader,os));
		}
		afterCompile(reader,os);
		
	}

	private void upload(final FileSystemInterface fsi, final JarOutputStream jos) throws IOException {
		final JarEntry	entry = new JarEntry(fsi.getPath());
		
		entry.setMethod(JarEntry.DEFLATED);
		if (fsi.isDirectory()) {
			jos.putNextEntry(entry);
			jos.closeEntry();
			for (String item : fsi.list()) {
				try(final FileSystemInterface	entity = fsi.clone().open("./"+item)) {
					upload(entity,jos);
				}
			}
		}
		else {
			jos.putNextEntry(entry);
			fsi.copy(jos);
			jos.closeEntry();
		}
	}
	
	
	private static class DefaultBindings extends HashMap<String,Object> implements Bindings {
		private static final long serialVersionUID = -2491675201928839009L;
	}
	
	private static class DefaultScriptContext implements ScriptContext {
		private Writer		writer;
		private Writer		errorWriter;
		private Reader		reader;
		private Bindings[]	bindings = new Bindings[]{new DefaultBindings(),new DefaultBindings()};
		
		DefaultScriptContext(final Reader reader, final Writer writer, final Writer errorWriter) {
			this.reader = reader;
			this.writer = writer;
			this.errorWriter = errorWriter;
		}

		@Override
		public void setBindings(final Bindings bindings, final int scope) {
			if (bindings == null) {
				throw new NullPointerException("Bindings to set can't be null");
			}
			else {
				switch (scope) {
					case ScriptContext.GLOBAL_SCOPE :
						this.bindings[0] = bindings;
						break;
					case ScriptContext.ENGINE_SCOPE :
						this.bindings[1] = bindings;
						break;
					default : throw new IllegalArgumentException("Scope ["+scope+"] os not known in the engine");
				}
			}
		}

		@Override
		public Bindings getBindings(final int scope) {
			switch (scope) {
				case ScriptContext.GLOBAL_SCOPE : return this.bindings[0];
				case ScriptContext.ENGINE_SCOPE : return this.bindings[1];
				default : throw new IllegalArgumentException("Scope ["+scope+"] os not known in the engine");
			}
		}

		@Override
		public void setAttribute(final String name, final Object value, final int scope) {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Attribute name can't be null or empty");
			}
			else {
				getBindings(scope).put(name,value);
			}
		}

		@Override
		public Object getAttribute(final String name, final int scope) {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Attribute name can't be null or empty");
			}
			else {
				return getBindings(scope).get(name);
			}
		}

		@Override
		public Object removeAttribute(final String name, final int scope) {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Attribute name can't be null or empty");
			}
			else {
				return getBindings(scope).remove(name);
			}
		}

		@Override
		public Object getAttribute(final String name) {
			final int	scope = getAttributesScope(name);
			
			if (scope == NON_EXISTENT) {
				return null;
			}
			else {
				return getAttribute(name,scope);
			}
		}

		@Override
		public int getAttributesScope(final String name) {
			if (name == null || name.isEmpty()) {
				throw new IllegalArgumentException("Attribute name can't be null or empty");
			}
			else {
				return bindings[1].containsKey(name) 
							? ScriptContext.ENGINE_SCOPE 
							: (bindings[0].containsKey(name) 
									? ScriptContext.GLOBAL_SCOPE
									: NON_EXISTENT);
			}
		}

		@Override
		public Writer getWriter() {
			return writer;
		}

		@Override
		public Writer getErrorWriter() {
			return errorWriter;
		}

		@Override
		public void setWriter(final Writer writer) {
			if (writer == null) {
				throw new NullPointerException("Writer to set can't be null");
			}
			else {
				this.writer = writer;
			}
		}

		@Override
		public void setErrorWriter(final Writer writer) {
			if (writer == null) {
				throw new NullPointerException("Writer to set can't be null");
			}
			else {
				this.errorWriter = writer;
			}
		}

		@Override
		public Reader getReader() {
			return reader;
		}

		@Override
		public void setReader(final Reader reader) {
			if (writer == null) {
				throw new NullPointerException("Reader to set can't be null");
			}
			else {
				this.reader = reader;
			}
		}

		@Override
		public List<Integer> getScopes() {
			return Arrays.asList(ScriptContext.GLOBAL_SCOPE,ScriptContext.ENGINE_SCOPE);
		}
	}
	
	private static class InternalClassLoader extends ClassLoader implements Closeable {
		private InternalClassLoader(final ClassLoader parent, final FileSystemInterface fsi) {
			super(parent);
		}
		
		public void prepareClass(final String className, final byte[] content) {
			
		}
		
		
		@Override
		public void close() {
			// TODO Auto-generated method stub
			
		}
	}
}