package chav1961.purelib.basic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Test;

public class AsmScriptEngineTest {
	@Test
	public void lifeCycleTest() throws IOException, ScriptException {
		final ScriptEngineManager	mgr = new ScriptEngineManager();
		final ScriptEngine			engine = mgr.getEngineByName("jasm");
		
		Assert.assertNotNull(engine);
		Assert.assertEquals(engine.getFactory().getEngineName(),AsmScriptEngineFactory.ENGINE_FULL_NAME);
		Assert.assertEquals(engine.getFactory().getEngineVersion(),AsmScriptEngineFactory.ENGINE_VERSION);
		Assert.assertEquals(engine.getFactory().getLanguageName(),AsmScriptEngineFactory.LANG_NAME);
		Assert.assertEquals(engine.getFactory().getLanguageVersion(),AsmScriptEngineFactory.LANG_VERSION);
		
		try(final InputStream	is = this.getClass().getResourceAsStream("test.asm");
			final Reader		rdr = new InputStreamReader(is)) {
			final Object		obj = engine.eval(rdr);
			
			Assert.assertEquals(obj,"test string");
		}
	}
}
