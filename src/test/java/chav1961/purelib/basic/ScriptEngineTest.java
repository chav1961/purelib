package chav1961.purelib.basic;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.MimeParseException;

@Tag("OrdinalTestCategory")
public class ScriptEngineTest {
	@Test
	public void basicFactoryTest() throws MimeParseException {
		final ScriptEngineFactory	factory = new PseudoScriptEngineFactory("name","1.0",Arrays.asList(toMime("text/plain")),"lang","2.0",Arrays.asList("lang1","lang2"));
		
		Assert.assertEquals("name",factory.getEngineName());
		Assert.assertEquals("name",factory.getParameter(ScriptEngine.ENGINE));
		Assert.assertEquals("1.0",factory.getEngineVersion());
		Assert.assertEquals("1.0",factory.getParameter(ScriptEngine.ENGINE_VERSION));
		Assert.assertEquals(Arrays.asList("text/plain"),factory.getMimeTypes());
		Assert.assertEquals("lang",factory.getLanguageName());
		Assert.assertEquals("lang",factory.getParameter(ScriptEngine.LANGUAGE));
		Assert.assertEquals("lang",factory.getParameter(ScriptEngine.NAME));
		Assert.assertEquals("2.0",factory.getLanguageVersion());
		Assert.assertEquals("2.0",factory.getParameter(ScriptEngine.LANGUAGE_VERSION));
		Assert.assertEquals(Arrays.asList("lang1","lang2"),factory.getNames());
		
		try{new PseudoScriptEngineFactory(null,"1.0",Arrays.asList(toMime("text/plain")),"lang","2.0",Arrays.asList("lang1","lang2"));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PseudoScriptEngineFactory("","1.0",Arrays.asList(toMime("text/plain")),"lang","2.0",Arrays.asList("lang1","lang2"));
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{new PseudoScriptEngineFactory("name","1.0",null,"lang","2.0",Arrays.asList("lang1","lang2"));
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PseudoScriptEngineFactory("name","1.0",Arrays.asList(),"lang","2.0",Arrays.asList("lang1","lang2"));
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new PseudoScriptEngineFactory("name","1.0",Arrays.asList(toMime("text/plain")),null,"2.0",Arrays.asList("lang1","lang2"));
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PseudoScriptEngineFactory("name","1.0",Arrays.asList(toMime("text/plain")),"","2.0",Arrays.asList("lang1","lang2"));
			Assert.fail("Mandatory exception was not detected (empty 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{new PseudoScriptEngineFactory("name","1.0",Arrays.asList(toMime("text/plain")),"lang",null,Arrays.asList("lang1","lang2"));
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PseudoScriptEngineFactory("name","1.0",Arrays.asList(toMime("text/plain")),"lang","",Arrays.asList("lang1","lang2"));
			Assert.fail("Mandatory exception was not detected (empty 4-th argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{new PseudoScriptEngineFactory("name","1.0",Arrays.asList(toMime("text/plain")),"lang","2.0",null);
			Assert.fail("Mandatory exception was not detected (null 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PseudoScriptEngineFactory("name","1.0",Arrays.asList(toMime("text/plain")),"lang","2.0",Arrays.asList());
			Assert.fail("Mandatory exception was not detected (empty 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{factory.getParameter(null); 
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{factory.getParameter(""); 
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{factory.getParameter("unknown"); 
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
		} catch (UnsupportedOperationException exc) {
		}
	}

	@Test
	public void basicEngineTest() throws MimeParseException {
		final ScriptEngineFactory	factory = new PseudoScriptEngineFactory("name","1.0",Arrays.asList(toMime("text/plain")),"lang","2.0",Arrays.asList("lang1","lang2"));
		final ScriptEngine			engine = factory.getScriptEngine();
		final Bindings				b1 = engine.createBindings(), b2 = engine.createBindings(); 
				
		Assert.assertEquals(factory, engine.getFactory());
		
		Assert.assertNotNull(engine.getBindings(ScriptContext.GLOBAL_SCOPE));
		Assert.assertNotNull(engine.getBindings(ScriptContext.ENGINE_SCOPE));
		engine.setBindings(b1,ScriptContext.GLOBAL_SCOPE);
		engine.setBindings(b2,ScriptContext.ENGINE_SCOPE);
		Assert.assertEquals(b1,engine.getBindings(ScriptContext.GLOBAL_SCOPE));
		Assert.assertEquals(b2,engine.getBindings(ScriptContext.ENGINE_SCOPE));

		try{engine.getBindings(666);
			Assert.fail("Mandatory exception was not detected (unknown scope in the given engine context)");
		} catch (IllegalArgumentException exc) {
		}
		try{engine.setBindings(null,ScriptContext.ENGINE_SCOPE);
			Assert.fail("Mandatory exception was not detected (null 1-st argument for ENGINE_SCOPE)");
		} catch (NullPointerException exc) {
		}
		try{engine.setBindings(b1,666);
			Assert.fail("Mandatory exception was not detected (unknown scope in the given engine context)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertNotNull(engine.getContext());
		Assert.assertEquals(b1,engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE));
		Assert.assertEquals(b2,engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE));
		Assert.assertEquals(new HashSet<Integer>(){{addAll(Arrays.asList(ScriptContext.GLOBAL_SCOPE,ScriptContext.ENGINE_SCOPE));}}
							,new HashSet<Integer>(){{addAll(engine.getContext().getScopes());}});

		Assert.assertNull(engine.getContext().getAttribute("attr",ScriptContext.GLOBAL_SCOPE));
		engine.getContext().setAttribute("attr","test",ScriptContext.GLOBAL_SCOPE);
		Assert.assertEquals("test",engine.getContext().getAttribute("attr",ScriptContext.GLOBAL_SCOPE));
		engine.getContext().removeAttribute("attr",ScriptContext.GLOBAL_SCOPE);
		Assert.assertNull(engine.getContext().getAttribute("attr",ScriptContext.GLOBAL_SCOPE));

		try{engine.getContext().getAttribute(null,ScriptContext.GLOBAL_SCOPE);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{engine.getContext().getAttribute("",ScriptContext.GLOBAL_SCOPE);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{engine.getContext().getAttribute("attr",666);
			Assert.fail("Mandatory exception was not detected (unknown scope in the given engine context)");
		} catch (IllegalArgumentException exc) {
		}

		try{engine.getContext().setAttribute(null,"test",ScriptContext.GLOBAL_SCOPE);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{engine.getContext().setAttribute("","test",ScriptContext.GLOBAL_SCOPE);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{engine.getContext().setAttribute("attr","test",666);
			Assert.fail("Mandatory exception was not detected (unknown scope in the given engine context)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{engine.getContext().removeAttribute(null,ScriptContext.GLOBAL_SCOPE);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{engine.getContext().removeAttribute("",ScriptContext.GLOBAL_SCOPE);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{engine.getContext().removeAttribute("attr",666);
			Assert.fail("Mandatory exception was not detected (unknown scope in the given engine context)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertNull(engine.getContext().getAttribute("attr"));
		engine.getContext().setAttribute("attr","test",ScriptContext.ENGINE_SCOPE);
		Assert.assertEquals("test",engine.getContext().getAttribute("attr"));
		engine.getContext().removeAttribute("attr",ScriptContext.ENGINE_SCOPE);
		Assert.assertEquals(-1,engine.getContext().getAttributesScope("attr"));

		try{engine.getContext().getAttribute(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{engine.getContext().getAttribute("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{engine.getContext().getAttributesScope(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{engine.getContext().getAttributesScope("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertNull(engine.get("attr")); 
		engine.put("attr","test");
		Assert.assertEquals("test",engine.get("attr"));

		try{engine.get(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{engine.get("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{engine.put(null,"test");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{engine.put("","test");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	
		Assert.assertNotNull(engine.getContext().getReader());
		Assert.assertNotNull(engine.getContext().getErrorWriter());
		Assert.assertNotNull(engine.getContext().getWriter());
		
		final Reader	rdr = new StringReader("");
		final Writer	wr1 = new StringWriter(), wr2 = new StringWriter();
		
		engine.getContext().setReader(rdr);
		engine.getContext().setWriter(wr1);
		engine.getContext().setErrorWriter(wr2);
		Assert.assertEquals(rdr,engine.getContext().getReader());
		Assert.assertEquals(wr1,engine.getContext().getWriter());
		Assert.assertEquals(wr2,engine.getContext().getErrorWriter());
		
		try{engine.getContext().setReader(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{engine.getContext().setWriter(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{engine.getContext().setErrorWriter(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{engine.setContext(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

//	@Test
	public void evalEngineTest() throws MimeParseException, ScriptException, IOException {
		final ScriptEngineFactory	factory = new PseudoScriptEngineFactory("name","1.0",Arrays.asList(toMime("text/plain")),"lang","2.0",Arrays.asList("lang1","lang2"));
		final ScriptEngine			engine = factory.getScriptEngine();
		final String				script = ScriptExec.class.getCanonicalName(); 
		
		engine.eval(script);
		
		try{engine.eval((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{engine.eval((String)null,engine.getContext());
			Assert.fail("Mandatory exception was not detected (null 1-th argument)");
		} catch (NullPointerException exc) {
		}
		try{engine.eval(script,(ScriptContext)null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{engine.eval((String)null,engine.getBindings(ScriptContext.GLOBAL_SCOPE));
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try(final Reader	rdr = new StringReader(script)) {
			engine.eval(rdr);

			try{engine.eval((Reader)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{engine.eval((Reader)null,engine.getContext());
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{engine.eval(rdr,(ScriptContext)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{engine.eval((Reader)null,engine.getBindings(ScriptContext.GLOBAL_SCOPE));
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

//	@Test
	public void specificEngineTest() throws MimeParseException, ScriptException, IOException {
		final ScriptEngineFactory	factory = new PseudoScriptEngineFactory("name","1.0",Arrays.asList(toMime("text/plain")),"lang","2.0",Arrays.asList("lang1","lang2"));
		final PseudoScriptEngine	engine = (PseudoScriptEngine)factory.getScriptEngine();
		final Class<?>				cl  = ScriptExec.class;
		final Manifest				mf = new Manifest();
		
		mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,"1.0");
		mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS,cl.getCanonicalName());
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final InputStream		is = cl.getResourceAsStream(cl.getSimpleName()+".class");
				
				final JarOutputStream	jos = new JarOutputStream(baos,mf)) {
				final JarEntry			je = new JarEntry(cl.getCanonicalName().replace('.','/')+".class") {{setMethod(JarEntry.DEFLATED);}};
	
				jos.putNextEntry(je);
				Utils.copyStream(is,jos);
				jos.closeEntry();
				jos.finish();
				jos.flush();
			}
			Assert.assertEquals(0,engine.getFileSystem().list().length);
			engine.download(URIUtils.convert2selfURI(baos.toByteArray()));
			Assert.assertEquals(1,engine.getFileSystem().list().length);
		}		
	}
	
	private static MimeType toMime(final String mime) throws MimeParseException {
		return MimeType.parseMimeList(mime)[0];
	}
} 

class PseudoScriptEngineFactory extends AbstractScriptEngineFactory {
	PseudoScriptEngineFactory(final String engineName, final String engineVersion, final List<MimeType> engineMIMEs, final String language, String languageVersion, final List<String> languageSynonyms) {
		super(engineName, engineVersion, engineMIMEs, language, languageVersion, languageSynonyms);
	}

	@Override public String getMethodCallSyntax(final String obj, final String m, final String... args) {return "";}
	@Override public String getOutputStatement(final String toDisplay) {return "";}
	@Override public String getProgram(final String... statements) {return "";}

	@Override
	public ScriptEngine getScriptEngine() {
		return new PseudoScriptEngine(this);
	}
}

class PseudoScriptEngine extends AbstractScriptEngine {
	private boolean		wasInBefore = false, wasInLine = false, wasInAfter = false;
	private String		name;
	
	PseudoScriptEngine(final ScriptEngineFactory factory) {
		super(factory);
	}

	@Override
	protected Reader beforeCompile(final Reader reader, final OutputStream os) throws IOException {
		wasInBefore = true;
		return reader;
	}
	
	@Override
	protected void processLineInternal(final long displacement, final int lineNo, final char[] data, final int from, final int length)  {
		name = new String(data,from,length).trim();
		wasInLine = true;
	}

	@Override
	protected void afterCompile(final Reader reader, final OutputStream os) throws IOException {
		try{
			final Class<?>	cl = Class.forName(name);
			final Manifest	m = new Manifest();
			
			m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,"1.0");
			m.getMainAttributes().put(Attributes.Name.MAIN_CLASS,cl.getCanonicalName());
			
			final JarOutputStream	jos = new JarOutputStream(os,m);
			final ZipEntry			ze = new ZipEntry(cl.getCanonicalName().replace('.','/')+".class") {{setMethod(ZipEntry.DEFLATED);}};
	
			jos.putNextEntry(ze);
			try(final InputStream	is = this.getClass().getResourceAsStream(cl.getSimpleName()+".class")) {
				Utils.copyStream(is, jos);
			}
			jos.closeEntry();
			jos.finish();
			jos.flush();
			wasInAfter = true;
		} catch (ClassNotFoundException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}
}
