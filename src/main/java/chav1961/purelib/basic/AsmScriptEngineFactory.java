package chav1961.purelib.basic;

import java.awt.datatransfer.MimeTypeParseException;
import java.util.Arrays;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import chav1961.purelib.basic.exceptions.MimeParseException;
import chav1961.purelib.basic.interfaces.BasicScriptEngineController;
import chav1961.purelib.streams.char2byte.AsmWriter;

/**
 * <p>This class implements {@link ScriptEngineFactory} functionality for the Java Byte code Macro Assembler (see {@linkplain AsmWriter}).
 * This class is an SPI service and can be accessed via the {@link ScriptEngineManager} functionality. Language name for the {@link ScriptEngineManager}
 * is <b>"javaasm"</b> or <b>"jasm"</b>. To execute compiled assembler immediately, it need contains a <b>public static</b> Object main({@linkplain Bindings},String[])
 * method in it. String[] parameters are exactly {@linkplain BasicScriptEngineController#execute(String...)} parameters. Using this class thru 
 * {@linkplain ScriptEngine#eval(String)} method, this String[] parameters are exactly <b>new</b> String[0].</p> 
 * 
 * @see ScriptEngineManager
 * @see ScriptEngineFactory
 * @see ScriptEngine
 * @see BasicScriptEngineController
 * @see AsmWriter
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.4
 */

public class AsmScriptEngineFactory extends AbstractScriptEngineFactory {
	public static final String		ENGINE_FULL_NAME = "Java Byte Code Macro Assembler";
	public static final String		ENGINE_VERSION = PureLibSettings.CURRENT_VERSION;
	public static final String		LANG_NAME = "JavaByteCodeAssembler";
	public static final String		LANG_VERSION = PureLibSettings.CURRENT_VERSION;
	
	public AsmScriptEngineFactory() throws MimeParseException {
		super(ENGINE_FULL_NAME,ENGINE_VERSION,Arrays.asList(new MimeType("text","plain")),LANG_NAME,LANG_VERSION,Arrays.asList("javaasm","jasm"));
	}

	@Override
	public String getMethodCallSyntax(final String obj, final String m, final String... args) {
		return null;
	}

	@Override
	public String getOutputStatement(final String toDisplay) {
		return null;
	}

	@Override
	public String getProgram(final String... statements) {
		final StringBuilder	sb = new StringBuilder();
		
		for (String item : statements) {
			sb.append(item).append('\n');
		}
		return sb.toString();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new AsmScriptEngine(this);
	}
}
